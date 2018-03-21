var TM = TM || {};
((function ($, window) {

    TM.Diagnose = TM.Diagnose || {};

    var Diagnose = TM.Diagnose;

    Diagnose.init = Diagnose.init || {};
    Diagnose.init = $.extend({
        doInit : function(container){
            Diagnose.container = container;
            Diagnose.init.initSearchDiv();
        },
        initSearchDiv : function(){
            $.get("/paipaidiscount/sellerCatCount",function(data){
                var sellerCat = $('.category-select');
                sellerCat.empty();
                /*if(!data ||data.res.length == 0){
                    sellerCat.hide();
                }*/

                var cat = $('<option catId = "0">所有分类</option>');
                sellerCat.append(cat);
                for(var i=0;i<data.res.length;i++) {
                    if(data.res[i].count <= 0){
                        continue;
                    }
                    var item_cat=data.res[i];
                    var option = $('<option></option>');
                    option.attr("catId",item_cat.cid);
                    option.html(item_cat.name);
                    sellerCat.append(option);
                }
            });
            Diagnose.container.find(".category-select").change(function() {
                Diagnose.show.doShow();
            });
            Diagnose.container.find(".state-select").change(function() {
                Diagnose.show.doShow();
            });
            Diagnose.container.find(".search-text").keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    Diagnose.container.find(".search-btn").click();
                }
            });
            Diagnose.container.find(".search-btn").click(function() {
                Diagnose.show.doShow();
            });
            Diagnose.show.doShow();
        }
    },Diagnose.init)

    Diagnose.show = Diagnose.show || {};
    Diagnose.show.orderData = {
        asc: "asc",
        desc: "desc"
    };
    Diagnose.show = $.extend({
        currentPage: 1,
        ruleData: {
            orderProp: '',      //排序的属性
            orderType: Diagnose.show.orderData.asc    //排序的类型，升序还是降序
        },
        doShow : function(currentPage){
            var ruleData = Diagnose.show.getQueryRule();
            var itemTbodyObj = Diagnose.container.find(".item-table").find("tbody");
            itemTbodyObj.html("");
            if (currentPage === undefined || currentPage == null || currentPage <= 0)
                currentPage = 1;
            Diagnose.container.find(".paging").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: ruleData,
                    dataType: 'json',
                    url: '/PaiPaiManage/queryItemsWithDiag',
                    callback: function(dataJson){
                        Diagnose.show.currentPage = dataJson.pn;//记录当前页
                        itemTbodyObj.html("");
                        var itemArray = dataJson.res;
                        //SkinBatch.container.find(".select-all-item").attr("checked", true);
                        $(itemArray).each(function(index, itemJson) {
                            itemTbodyObj.append(Diagnose.row.createRow(index, itemJson));
                            itemTbodyObj.append(Diagnose.row.createDetailRow(index, itemJson));
                        });
                        Diagnose.Event.setItemTbodyEvent(itemTbodyObj);
                    }
                }

            });
        },
        getQueryRule: function() {
            var ruleData = {};
            var title = Diagnose.container.find(".search-text").val();
            var state = Diagnose.container.find(".state-select").val();
            var catId = Diagnose.container.find(".category-select option:selected").attr("catid");

            ruleData.title = title;
            ruleData.state = state;
            ruleData.catId = catId;

            return ruleData;
        }

    },Diagnose.show)

    Diagnose.row = Diagnose.row || {};
    Diagnose.row = $.extend({
        createRow : function(index, itemJson){
            var href = "http://auction1.paipai.com/" + itemJson.itemCode;
            var newTitleId = "newTitleRemainObj" + index;
            var row = '<tr class="itemRow" iid="'+itemJson.itemCode+'">' +
                '<td><a href="'+href+'" target="_blank"><img style="width: 80px;height: 80px;" src="'+itemJson.picLink+'" alt="'+itemJson.itemName+'"></a></td>' +
                '<td><a href="'+href+'" target="_blank"><span class="itemTitle">'+itemJson.itemName+'</span></a></td>' +
                '<td><textarea style="text-align: center;width: 96%;" class="newTitle">'+itemJson.itemName+'</textarea>' +
                '   <div class="inlineblock" id="'+newTitleId+'"></div><span style="margin-left: 10px;" class="tmbtn sky-blue-btn save-new-title">保存新标题</span>' +
                '</td>' +
                '<td><span class="itemPrice">￥'+itemJson.itemPrice/100+'</span></td>' +
                '<td><span class="itemScore">~</span></td>' +
                '<td><span class="itemDiagnoseDetail tmbtn wide-sky-blue-btn">查看诊断</span></td>' +
                '</tr>'
            var rowObj = $(row);
            rowObj.find('.newTitle').inputlimitor({
                limit: 60,
                boxId: newTitleId,
                remText: '<span class="twelve" >剩余字数:</span><span class="oldRemainLength">%n</span>',
                limitText: '/ %n'
            });
            return rowObj;
        },
        createDetailRow : function(index, itemJson){
            var detailRow = '<tr class="hidden itemDetailRow" iid="'+itemJson.itemCode+'">' +
                '<td colspan="9">' +
                '<div class="tabDiv" style="width:100%;margin:0px auto 10px auto;">' +
                '<ul class="clearfix" iid="'+itemJson.itemCode+'">' +
                '<li class=""><a targetcls="diagResultBlock" href="javascript:void(0);" class="">诊断结果</a></li>' +
                '<li class=""><a targetcls="hotWordsBlock" href="javascript:void(0);" class="">热词搜索</a></li>' +
                '<li class=""><a targetcls="promoteWordsBlock" href="javascript:void(0);" class="">促销词</a></li>' +
                '<li class=""><a targetcls="keyWordsBlock" href="javascript:void(0);" class="">关键词</a></li>' +
                '<li class=""><a targetcls="prosListBlock" href="javascript:void(0);" class="">属性列表</a></li>' +
                '<li class=""><a targetcls="longtailBlock" href="javascript:void(0);" class="">长尾词</a></li>' +
                '</ul>' +
                '</div>' +
                '<div class="liTargetDiv"></div>' +
                '</td>' +
                '</tr>';
            return $(detailRow);
        }

    },Diagnose.row)

    Diagnose.Event = Diagnose.Event || {};
    Diagnose.Event = $.extend({
        setItemTbodyEvent : function(itemTbodyObj){
            itemTbodyObj.find('.itemDiagnoseDetail').unbind('click').click(function(){
                if($(this).text() == "查看诊断") {
                    $(this).text("收起诊断");
                    var iid = $(this).parent().parent().attr("iid");
                    var toShow = itemTbodyObj.find('.itemDetailRow[iid="'+iid+'"]');
                    toShow.show();
                    if(toShow.find('.select').length == 0){
                        toShow.find('a[targetcls="diagResultBlock"]').parent().trigger('click');
                    }
                } else {
                    $(this).text("查看诊断");
                    var iid = $(this).parent().parent().attr("iid");
                    itemTbodyObj.find('.itemDetailRow[iid="'+iid+'"]').hide();
                }
            });
            itemTbodyObj.find('.tabDiv li').unbind('click').click(function(){
                if($(this).find('a').hasClass('select')){
                    return;
                }
                $(this).parent().find('.select').removeClass('select');
                $(this).find('a').addClass('select');
                var target = $(this).find('a').attr("targetcls");
                var targetDiv = $(this).parent().parent().parent().find('.liTargetDiv');
                switch(target){
                    case "diagResultBlock":
                        Diagnose.Detail.renderDiagResultBlock(targetDiv);break;
                    case "hotWordsBlock":
                        Diagnose.Detail.renderBusSearchBlock(targetDiv);break;
                    case "promoteWordsBlock":
                        Diagnose.Detail.renderPromoteWordsBlock(targetDiv);break;
                    case "keyWordsBlock":
                        Diagnose.Detail.renderKeyWordsBlock(targetDiv);break;
                    case 'prosListBlock':
                        Diagnose.Detail.renderProsListBlock(targetDiv);break;
                    case 'longtailBlock':
                        Diagnose.Detail.renderLongtailBlock(targetDiv);break;
                    default:
                        Diagnose.Detail.renderDiagResultBlock(targetDiv);break;
                }
            });
            itemTbodyObj.find('.save-new-title').unbind('click').click(function(){
                var iid = $(this).parent().parent().attr('iid');
                var newTitle = $(this).parent().find('.newTitle').val();
                $.post('/PaiPaiManage/rename',{itemCode:iid, title:newTitle},function(data){
                    if(data == "rename success!") {
                        itemTbodyObj.find('.itemRow[iid="'+iid+'"]').find('.itemTitle').text(newTitle);
                        TM.Alert.load("修改成功");
                    } else {
                        TM.Alert.load("修改失败");
                    }

                });
            });
            itemTbodyObj.find('.newTitle').keyup();
            itemTbodyObj.find('.limitorBox').hide();
        }

    },Diagnose.Event)

    Diagnose.Detail = Diagnose.Detail || {};
    Diagnose.Detail = $.extend({
        renderDiagResultBlock : function(targetDiv){
            targetDiv.empty();
            var iid = targetDiv.parent().parent().attr('iid');
            var title = targetDiv.parent().parent().parent().find('.itemRow[iid="'+iid+'"]').find('.itemTitle').text();
            $.post('/PaiPaiManage/singleDiag',{title : title, itemCode : iid}, function(data){
                targetDiv.parent().parent().parent().find('.itemRow[iid="'+iid+'"]').find('.itemScore').text(data.score);
                targetDiv.append(QueryCommodity.commodityDiv.createDetail(data));
                // TM.Alert.load(QueryCommodity.commodityDiv.createDetail(data),780,520,function(){},false,"诊断结果: "+title);
            });
            targetDiv.show();
        },
        renderBusSearchBlock : function(container, order, sort, s){
            if (order === undefined || order == '') {
                order = 'pv';
            }
            if (sort === undefined || sort == '') {
                sort = 'desc';
            }
            container.empty();
            var iid = container.parent().parent().attr('iid');
            var title = container.parent().parent().parent().find('.itemRow[iid="'+iid+'"]').find('.itemTitle').text();
            $.post("/PaiPaiManage/estimateKeyword",{title:title},function(data){
                var searchbtn = $('<input type="text" id="bus-search-text" style="margin-right: 15px;"><span class="tmbtn sky-blue-btn search-bus-now">立即搜索</span>');
                container.append(searchbtn);
                var estimate = $('<div class="titlesplits"></div>');
                $(data).each(function(i,word){
                    estimate.append($('<span class="baseblock">'+word+'</span>'));
                });
                container.append(estimate);


                var bustable = $('<table class="bussearch" style="text-align: center;width: 100%;"><thead><tr style="text-align: center;" align="center" class="tableRow">' +
                    '<th style="width: 240px;">关键词</th>' +
                    '<th class="bus-search-sort-th" style="width: 80px;cursor: pointer;">搜索指数<span class="inlineblock sort Desc" sort="pv"></span></th>' +
                    '<th class="bus-search-sort-th" style="width: 80px;cursor: pointer;">点击量<span class="inlineblock sort Asc" sort="click"></span></th>' +
                    '<th class="bus-search-sort-th" style="width: 80px;cursor: pointer;">宝贝数<span class="inlineblock sort Asc" sort="scount"></span></th>' +
                    '<th>转化率</th>' +
                    '<th class="bus-search-sort-th" style="width: 80px;cursor: pointer;">性价比<span class="inlineblock sort Asc" sort="score"></span><span class="question" content="搜索量/宝贝数，搜索量除以宝贝数的指标,表示关键词的性价比"></span></th>' +
                    '<th>行业平均出价</th>' +
                    // '<th style="width: 120px;">添加到我的词库</th>' +
                    '</tr></thead><tbody id="bus-search-tbody"></tbody></table>');

                container.append($('<div style="height:30px;text-align: center;" class="bussearch-pagingArea"></div>'));
                container.append(bustable);
                container.append($('<div style="height:30px;text-align: center;" class="bussearch-pagingArea"></div>'));
                var pageList = container.find('.bussearch-pagingArea');
                container.find('.search-bus-now').click(function(){
                    var word = container.find('#bus-search-text').val();
                    Diagnose.Detail.bussearch(container, pageList, bustable, order, sort, word);
                });
                //container.find('.search-bus-now').trigger('click');
                container.find('.bus-search-sort-th').click(function(){
                    var order = $(this).find('.sort').attr('sort');
                    var sort = "";
                    if($(this).find('.sort').hasClass('Desc')){
                        $(this).find('.sort').removeClass('Desc');
                        $(this).find('.sort').addClass('Asc');
                        sort = "asc";
                    } else {
                        $(this).find('.sort').removeClass('Asc');
                        $(this).find('.sort').addClass('Desc');
                        sort = 'desc';
                    }
                    var s = container.find('input').val();
                    Diagnose.Detail.bussearch(container,pageList,bustable,order,sort,s);
                });
                container.find('.baseblock').click(function(){
                    container.find('input').val($(this).text());
                    container.find('.search-bus-now').trigger("click");
                });
                container.find('.baseblock').eq(0).trigger("click");
                container.show();
            });
        },
        bussearch:function (container, pageList, bustable, order, sort, s) {
            if (order === undefined || order == '') {
                order = 'pv';
            }
            if (sort === undefined || sort == '') {
                sort = 'desc';
            }

            pageList.tmpage({
                currPage:1,
                pageSize:10,
                pageCount:1,
                ajax:{
                    on:true,
                    dataType:'json',
                    url:"/PaiPaiManage/busSearch",
                    param:{order:order, sort:sort, word:s},
                    callback:function (data) {
                        bustable.find('#bus-search-tbody').empty();
                        if (data != null) {
                            if (data.res.length > 0) {
                                $(data.res).each(function (i, myword) {
                                    if (myword.word != "") {
                                        var pv = myword.pv > 0 ? myword.pv : "~";
                                        var click = myword.click > 0 ? myword.click : "~";
                                        var scount = myword.scount > 0 ? myword.scount : "~";
                                        var score = myword.score > 0 ? myword.score : "~";
                                        bustable.find('#bus-search-tbody').append($('<tr><td class="word-content">' + genKeywordSpan.gen({"text":myword.word, "callback":"", "enableStyleChange":true, "spanClass":'addTextWrapperSmall'}) + '</td><td>' + pv + '</td><td>' + click + '</td><td>' + scount + '</td><td>' + myword.transRate + '</td><td>' + score + '</td><td>' + myword.bidPrice + '</td></tr>'))
                                    }
                                })
                                bustable.find('#bus-search-tbody').find('tr:even').addClass('even');
                                // add to mywords
                                /*bustable.find('.add-to-mywords span').click(function () {
                                 $.post('/KeyWords/addMyWord', {word:$(this).parent().parent().find('.word-content').text()}, function (data) {
                                 TM.Alert.load(data, 400, 300, function () {
                                 }, "", "添加到词库");
                                 });
                                 });*/

                                // add to mylexicon words
                                /*bustable.find('.add-to-mywords span').click(function () {
                                 $.ajax({
                                 url:"/Words/addmylexicon",
                                 data:{wordId:$(this).parent().parent().find('.word-content').text()},
                                 success:function(data){
                                 TM.Alert.load(data);
                                 }
                                 });
                                 });*/
                                bustable.find('.addTextWrapperSmall').click(function () {
                                    Diagnose.util.putIntoTitle($(this).text(), $(this), container);
                                });
                            } else {
                                bustable.find('#bus-search-tbody').append($('<td colspan="9"><p style="font-size: 16px;text-align: center;font-family: 微软雅黑;">亲，请输入关键词搜索</p></td>'));
                            }

                        }
                    }
                }
            });
        },
        renderProsListBlock : function(container){
            container.empty();
            //container.find('.searchLongtailWords').click(function(){
            var iid = container.parent().parent().attr('iid');
            $.get("/PaiPaiManage/props",{itemCode:iid}, function(data){
                var table = container.find('.propsTable');
                if(table.length == 0){
                    table = $('<table class="propsTable" style="text-align: center;vertical-align: middle;font-size: 12px;"></table>');
                }
                table.empty();
                /*table.append($('<thead ><th style="width: 200px;">属性列表</th><th style="width: 580px;">属性内容</th></thead>'));
                 $(data).each(function(i,word){
                 table.append($('<tr style="height: 30px;"><td class="word">'+word.key+'</td><td class="searchMore" style="cursor: pointer;">'+genKeywordSpan.gen({"text":word.value,"callback":autoTitle.util.putIntoTitle,"enableStyleChange":true})+'</td></tr>'));
                 });*/
                var trsize = 3;
                var tmpTr;
                $(data).each(function(i,word){
                    if(i%trsize == 0){
                        var trObj = $('<tr style="height: 30px;width: 100%;"></tr>');
                        tmpTr = trObj;
                        table.append(tmpTr);
                    }
                    tmpTr.append($('<td class="word" style="width: 258px;"><span class="inlineblock" style="width: 98px;">'+word.key+':</span>'+genKeywordSpan.gen({"text":word.value,"callback":Diagnose.util.putIntoTitle,"enableStyleChange":true})+'</td>'));
                });
                table.find('span').css("margin","5px 0 5px 0");
                table.find('span').click(function(){
                    Diagnose.util.putIntoTitle($(this).text(),$(this),container);
                });

                container.append(table);
            });
        },
        renderLongtailBlock : function(container){
            container.empty();
            var iid = container.parent().parent().attr('iid');
            var title = container.parent().parent().parent().find('.itemRow[iid="'+iid+'"]').find('.itemTitle').text();
            container.append($('<div style="margin-left: 30px;"><span style="font-size: 13px;color: #2796F9;font-weight: bold;margin-right: 10px;">长尾词根：</span><input style="margin-right: 15px;width: 300px;"><a href="javascript:void(0);" class="searchLongtailWords tmbtn sky-blue-btn" style="display: inline-block;">立即搜索</a></div>'));
            $.post("/PaiPaiManage/estimateKeyword",{title:title},function(data){
                var estimate = $('<div class="titlesplits"></div>');
                $(data).each(function(i,word){
                    estimate.append($('<span class="baseblock">'+word+'</span>'));
                });
                container.append(estimate);
                container.find('.baseblock').click(function(){
                    container.find('input').val($(this).text());
                    container.find('.searchLongtailWords').trigger("click");
                });
                container.find('.baseblock').eq(0).trigger("click");
            });
            container.find('.searchLongtailWords').click(function(){
                var word = container.find('input').val();
                $.post("/PaiPaiManage/longTail",{s:word,title:title}, function(data){
                    var table = container.find('.longtailTable');
                    if(table.length == 0){
                        table = $('<table class="longtailTable" style="text-align: center;vertical-align: middle;font-size: 12px;"></table>');
                    }
                    table.empty();
                    table.append($('<thead ><th style="width: 480px;">长尾词列表</th><th style="width: 300px;">获取相关长尾词</th></thead>'));
                    $(data).each(function(i,word){
                        table.append($('<tr style="height: 30px;" word="'+word+'"><td class="word">'+genKeywordSpan.gen({"text":word,"callback":Diagnose.util.putIntoTitle,"enableStyleChange":true})+'</td><td class="searchMore" style="cursor: pointer;">查看更多</td></tr>'));
                    });
                    table.find('span').css("margin","5px 0 5px 0");
                    table.find('span').click(function(){
                        Diagnose.util.putIntoTitle($(this).text(),$(this),container);
                    });
                    table.find('.searchMore').click(function(){
                        container.find('input').val($(this).parent().attr("word"));
                        container.find('.searchLongtailWords').trigger('click');
                    });
                    container.append(table);

                });
            });
        },
        renderKeyWordsBlock:function (container) {
            container.empty();
            container.append($('<div style="margin-left: 30px;"><span style="font-size: 13px;color: #2796F9;font-weight: bold;margin-right: 10px;">关键词：</span><input style="margin-right: 15px;width: 300px;" value="袜子"><a href="javascript:void(0);" class="searchKeywords tmbtn sky-blue-btn" style="display: inline-block;">立即搜索</a></div>'));

            container.find('.searchKeywords').click(function () {
                container.find('.blank0').remove();
                container.find('.pagingArea').remove();
                container.find('.table-contaier').remove();
                var word = container.find('input').val();
                var iid = container.parent().parent().attr('iid');
                var title = container.parent().parent().parent().find('.itemRow[iid="' + iid + '"]').find('.itemTitle').text();
                var pageList = container.find('.pagingArea');
                if(pageList.length == 0) {
                    pageList = $('<div style="height:30px;text-align: center;" class="pagingArea"></div><div class="blank0" style="3px;"></div>');
                }
                pageList.empty();
                var pageListtop = pageList.clone();
                pageList.empty();
                pageListtop.empty();
                container.append(pageList);
                container.append($('<div class="table-contaier"></div>'));
                container.append(pageListtop);
                container.find('.pagingArea').tmpage({
                    currPage: 1,
                    pageSize: 10,
                    pageCount:1,
                    ajax: {
                        on: true,
                        dataType: 'json',
                        url: "/PaiPaiManage/searchKeywords",
                        param:{s:word,title:title},
                        callback:function(data){
                            if(!data.isOk){
                                // TODO, no res...
                            }
                            var table = container.find('.keywordsTable');
                            if(table.length == 0){
                                table = $('<table class="keywordsTable" style="text-align: center;vertical-align: middle;font-size: 12px;"></table>');
                            }
                            table.empty();
                            table.append($('<thead ><th style="width: 380px;">关键词列表</th><th style="width: 100px;">热卖指数</th><th style="width: 100px;">搜索指数</th><th style="width: 100px;">竞争指数</th><th style="width: 100px;">获取相关推荐词</th></thead>'));
                            $(data.res).each(function(i,word){
                                table.append($('<tr word="'+word.word+'"><td class="word">'+genKeywordSpan.gen({"text":word.word,"callback":Diagnose.util.putIntoTitle,"enableStyleChange":true})+'</td><td>'+word.pv+'</td><td>'+word.click+'</td><td>'+word.competition+'</td><td class="searchMore" style="cursor: pointer;">查看更多</td></tr>'));
                            });
                            table.find('span').css("margin","5px 0 5px 0");
                            table.find('tr:odd').addClass('bgwhite');
                            table.find('span').click(function(){
                                Diagnose.util.putIntoTitle($(this).text(),$(this),container);
                            });
                            table.find('.searchMore').click(function(){
                                container.find('input').val($(this).parent().attr("word"));
                                container.find('.searchKeywords').trigger('click');
                            });
                            container.find('.table-contaier').append(table);
                            container.append('<div class="blank0" style="height:8px;"></div>');
                        }
                    }
                });
                container.show();
            });

            var iid = container.parent().parent().attr('iid');
            var title = container.parent().parent().parent().find('.itemRow[iid="'+iid+'"]').find('.itemTitle').text();
            $.post("/PaiPaiManage/estimateKeyword",{title:title},function(data){
                var estimate = $('<div class="titlesplits"></div>');
                $(data).each(function(i,word){
                    estimate.append($('<span class="baseblock">'+word+'</span>'));
                });
                container.append(estimate);
                container.find('.baseblock').click(function(){
                    container.find('input').val($(this).text());
                    container.find('.searchKeywords').click();
                });
                container.find('.baseblock:eq(0)').click();
            });
        },
        renderPromoteWordsBlock : function(targetDiv){
            targetDiv.empty();

            $.ajax({
                url: '/PaiPaiManage/getPromoteWords',
                dataType: 'json',
                type: 'post',
                data: {},
                error: function() {
                },
                success: function (promoteArray) {
                    var rowCount = 5;
                    var count = 0;
                    var html = [];

                    html.push('<table class="promoteWordsTable">');
                    html.push('<tbody>');
                    html.push('<tr>')
                    $(promoteArray).each(function(index, promoteWord) {
                        var spanObj = genKeywordSpan.gen({"text":promoteWord,"callback":Diagnose.util.putIntoTitle,"enableStyleChange":true});
                        if(count++ == rowCount){
                            count = 1;
                            html.push('</tr></tr>');
                        }
                        html.push('<td>'+spanObj+'</td>');
                    });
                    html.push('</tbody>');
                    html.push('</table>');
                    var table = $(html.join(''));
                    table.find('span').click(function(){
                        Diagnose.util.putIntoTitle($(this).text(),$(this),targetDiv);
                    });

                    $(table).appendTo(targetDiv);
                    //ModifyTitle.util.hideLoading();
                }
            });
            targetDiv.show();
        }

    },Diagnose.Detail)

    Diagnose.util = Diagnose.util || {};
    Diagnose.util = $.extend({
        checkItemsCountLimitOrNot : function(){
            return (parseInt($.cookie("userItemsCount")) - parseInt($.cookie("itemCountLimit"))) > 0 ? true : false;
        },
        getParams : function(){
            var params = {};
            var status = $("#itemsStatus option:selected").attr("tag");
            switch(status){
                case "onsale":params.status=0;break;
                case "instock" : params.status=1;break;
                default : params.status=2;break;
            }

            var catId = $('#itemsCat option:selected').attr("catId");
            params.catId = catId;

            var cid = $('#taobaoCat option:selected').attr("catId");
            params.cid = cid;

            var sort = $('#itemsSortBy option:selected').attr("tag");
            switch(sort){
                case "sortByScoreUp" : params.sort=1;break;
                case "sortByScoreDown" : params.sort=2;break;
                case "sortBySaleCountUp" : params.sort=3;break;
                case "sortBySaleCountDown" : params.sort=4;break;
                default : params.sort=1;break;
            }

            params.lowBegin = $('#lowScore').val();
            params.topEnd = $('#highScore').val();
            params.s = $('#searchWord').val();
            return params;
        },
        modifyTitle : function(numIid, newTitle,oldTitleInput){
            var params = {};
            params.numIid = numIid;
            params.title = newTitle;
            $.post("/Titles/rename",params,function(res){
                if(!res){
                    TM.Alert.load("标题修改失败！请您稍后重试哟!");
                }else if(res.ok){
                    TM.Alert.load("标题修改成功！");
                    if(oldTitleInput.val()!=newTitle) {
                        oldTitleInput.val(newTitle);
                        oldTitleInput.trigger("keyup");
                    }
                }else{
                    TM.Alert.load(res.msg);
                }
            });
        },
        putIntoTitle : function(text,spanObj,container){
            var iid = container.parent().parent().attr('iid');
            var newTitle = container.parent().parent().parent().find('.itemRow[iid="'+iid+'"]').find('.newTitle');
            if(Diagnose.util.countCharacters(newTitle.val()+text) > 60) {
                spanObj.qtip({
                    content: {
                        text: "标题长度将超过字数限制，请先删减标题后再添加~"
                    },
                    position: {
                        at: "center left "
                    },
                    show: {
                        when: false,
                        ready:true
                    },
                    hide: {
                        delay:1000
                    },
                    style: {
                        name:'cream'
                    }
                });
            }
            else {
                var start = {}, end = {};
                start.left = spanObj.offset().left+"px";
                start.top = spanObj.offset().top+"px";
                end.left = container.parent().parent().parent().find('.itemRow[iid="'+iid+'"]').find('.newTitle').offset().left+"px";
                end.top = container.parent().parent().parent().find('.itemRow[iid="'+iid+'"]').find('.newTitle').offset().top+"px";
                Diagnose.util.flyFromTo(start,end,spanObj,function(){
                    /*var dthis = newTitle[0];
                     if(document.selection){
                     dthis.focus();
                     var fus = document.selection.createRange();
                     fus.text = text;
                     dthis.focus();
                     }
                     else if(dthis.selectionStart || dthis.selectionStart == '0'){
                     var start = dthis.selectionStart;
                     var end =dthis.selectionEnd;
                     dthis.value = dthis.value.substring(0, start) + text + dthis.value.substring(end, dthis.value.length);
                     }
                     else{this.value += text; this.focus();}*/
                    newTitle.val(newTitle.val()+text);
                    newTitle.trigger("keyup");
                    container.parent().parent().find(".oldTitleRemainWordsNum").html(30-newTitle.val().length);
                })



                spanObj.qtip({
                    content: {
                        text: "已添加至标题尾部哟"
                    },
                    position: {
                        at: "center left "
                    },
                    show: {
                        when: false,
                        ready:true
                    },
                    hide: {
                        delay:1000
                    },
                    style: {
                        name:'cream'
                    }
                });
            }
        },
        flyToTitle : function($this,$targrt){
            var start = {}, end = {};
            start.left = $this.offset().left+"px";
            start.top = $this.offset().top+"px";
            end.left = $targrt.offset().left+"px";
            end.top = $targrt.offset().top+"px";
            Diagnose.util.flyFromTo(start,end,$this,null)
        },
        flyFromTo : function(start,end,flyObj,callback){
            //var img = $('<span id="fly-from-to-img" class="inlineblock" style="z-index:200001;position: absolute;top:'+start.top+';left: '+start.left+'"></span>');
            var obj = flyObj.clone();
            obj.css("position","absolute");
            obj.css('left',start.left);
            obj.css('top',start.top);
            obj.appendTo($('body'));

            obj.animate({top:end.top,left:end.left},1500, function(){
                obj.fadeOut(1000,function(){
                    obj.remove();
                });
                callback && callback();
            });
        },
        createBatchOPResult : function(data){
            if(!data || data.length==0 || data.failNum == 0) {
//            TM.Alert.load("批量应用推荐标题成功"+((!data||data.length==0)?0:data.successNum)+"个，失败0个~,点击确定刷新数据",400,300,TM.sync);
                TM.Alert.loadDetail("<div class='largp'><p>批量推荐标题成功"+((!data||data.length==0)?0:data.successNum)+
                    "个，失败0个~,点击确定刷新页面</p><p>若您对标题优化的结果不满意,您可以稍后在<b>左侧导航标题还原中心</b>进行还原操作,非常感谢</p><p>若有问题,也欢迎联系我们的客服,非常感谢</p></div>",
                    850,350,function(){window.location.hash='score-desc';$('.gopage-submit').click();},'推荐成功');
                return;
            }

            var multiModifyArea = $('.multiModifyArea');
            if(multiModifyArea.length == 0) {
                var multiModifyArea=$('<div class="multiModifyArea"></div>');
            }
            multiModifyArea.empty();
            multiModifyArea.css("display","block");
            multiModifyArea.css("left",(screen.width-1000)/2 + 200 +"px");

            //var tableDiv =  multiModifyArea.find('#tableDiv');
            //if(tableDiv.length==0) {
            var tableDiv=$('<div id="tableDiv"></div>');
            // }
            // tableDiv.empty();
            var tableObj=MultiModify.createErrTable.createTableObj(data);
            tableDiv.append(tableObj);
            multiModifyArea.append(tableDiv);
            //var exitBatchOPMsg = multiModifyArea.find('.exitBatchOPMsg');
            //if(exitBatchOPMsg.length>0) {
            //   exitBatchOPMsg.remove();
            // }
            var successNum = $('<span >'+'批量推荐标题成功:'+'<span class="successNum">'+data.successNum+'</span>'+'</span>') ;
            var failNum = $('<span >'+'批量推荐标题失败:'+'<span class="failNum">'+data.failNum+'</span>'+'</span>') ;
            multiModifyArea.append(successNum);
            multiModifyArea.append(failNum);
            multiModifyArea.append($('<div style="width:600px;display: inline-block;"></div>'));
            multiModifyArea.append($('<div class="exitBatchOPMsg"><span ></span></div>'));
            multiModifyArea.find('.exitBatchOPMsg').click(function(){
                multiModifyArea.hide();
            });

            multiModifyArea.appendTo($("body"));
        },
        updateRecomTitle : function(data){
            $('.singleDiagRes').each(function(){
                if(data[$(this).attr("numiid")] !== undefined){
                    $(this).find('.newTitleContent').val((data[$(this).attr("numiid")]));
                    $(this).find('.newTitleContent').removeClass('WaitContent');
                }
            });

            //alert(data)
        },
        updateGuanfangRecomTitle : function(data){
            $('.singleDiagRes').each(function(){
                if(data[$(this).attr("numiid")] !== undefined){
                    $(this).find('.fenxiaoTitleContent').val((data[$(this).attr("numiid")]));
                    $(this).find('.fenxiaoTitleContent').removeClass('WaitContent');
                }
            });

            //alert(data)
        },
        countCharacters : function(str){
            var totalCount = 0;
            for (var i=0; i<str.length; i++) {
                var c = str.charCodeAt(i);
                if ((c >= 0x0001 && c <= 0x007e) || (0xff60<=c && c<=0xff9f)) {
                    totalCount++;
                }else {
                    totalCount+=2;
                }
            }
            return totalCount;
        },
        createRenameHistoryTbody : function (results) {
            var tbody = $('<tbody></tbody>');
            $(results).each(function(i,result){
                tbody.append('<tr numIid="'+result.numIid+'">' +
                    '<td class="rename-history-oldtitle">'+result.oldTitle+'</td>'+
                    '<td class="rename-history-newtitle">'+result.newTitle+'</td>'+
                    '<td><span class="set-old-title-back tmbtn sky-blue-btn">还原</span></td>'+
                    '</tr>');
            });
            tbody.find('.set-old-title-back').click(function(){
                var oldTitle = $(this).parent().parent().find('.rename-history-oldtitle').text();
                var numIid = $(this).parent().parent().attr("numIid");
                var data = {};
                data.numIid = numIid;
                data.title = oldTitle;
                //弹出loading动画
                //ModifyTitle.util.showLoading();
                $.ajax({
                    url: '/Titles/rename',
                    dataType: 'json',
                    type: 'post',
                    data: data,
                    error: function() {
                        alert("标题修改失败！");
                    },
                    success: function (res) {
                        if(!res){
                            alert("标题修改失败！请您稍后重试哟!");
                        }else if(res.ok){
                            //诊断新标题
                            //Diagnose.newTitle.doDiagnose(params, originScore);
                            TM.Alert.loadDetail("亲，标题修改成功，点击确定刷新",400,300,function(){
                                location.reload();
                            })
                        }else{
                            alert(res.msg);
                        }

                        // ModifyTitle.util.hideLoading();
                    }
                });
            });
            return tbody;
        }
    },Diagnose.util);

})(jQuery,window));