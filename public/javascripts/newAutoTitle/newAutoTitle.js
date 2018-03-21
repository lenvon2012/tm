((function ($, window) {
    TM.newAutoTitle = TM.newAutoTitle || {}
    var newAutoTitle = TM.newAutoTitle;

    newAutoTitle.Init = newAutoTitle.Init || {};
    newAutoTitle.Init = $.extend({
        init : function(container){
            newAutoTitle.Init.container = container;
            newAutoTitle.Init.initSearchArea();
            newAutoTitle.Event.setBatchRecommendEvent();
            newAutoTitle.Init.initDiagArea();

        },

        initSearchArea : function(){
            $.get("/items/sellerCatCount",function(data){
                var sellerCat = $('#itemsCat');
                sellerCat.empty();
                if(!data || data.length == 0){
                    sellerCat.hide();
                }

                var exist = false;
                var cat = $('<option>自定义类目</option>');
                sellerCat.append(cat);
                for(var i=0;i<data.length;i++) {
                    if(data[i].count <= 0){
                        continue;
                    }

                    exist = true;
                    var option = $('<option></option>');
                    option.attr("catId",data[i].id);
                    option.html(data[i].name+"("+data[i].count+")");
                    sellerCat.append(option);
                }
                if(!exist){
                    sellerCat.hide();
                }
            });
            $.get("/items/itemCatCount",function(data){
                var taobaoCat = $('#taobaoCat');
                taobaoCat.empty();
                if(!data || data.length == 0){
                    taobaoCat.hide();
                }

                var exist = false;
                var cat = $('<option>淘宝类目</option>');
                taobaoCat.append(cat);
                for(var i=0;i<data.length;i++) {
                    if(data[i].count <= 0){
                        continue;
                    }

                    exist = true;
                    var option = $('<option></option>');
                    option.attr("catId",data[i].id);
                    option.html(data[i].name+"("+data[i].count+")");
                    taobaoCat.append(option);
                }
                if(!exist){
                    taobaoCat.hide();
                }
            });
            newAutoTitle.Init.container.find('#goSearchItems').unbind('click')
                .click(function(){
                    //如果文本框内的值还是默认值就不搜索
                    var textValue=$.trim($("#searchWord").val());
                    if (textValue == "输入关键词搜索商品"||textValue=="") {
                        return;
                    }

                    newAutoTitle.ItemsDiag.getItemsDiag();
                });
        },
        initDiagArea : function(){
            $.get("/Home/firstSync",function(){
                newAutoTitle.ItemsDiag.getItemsDiag();
            });

        }
    },newAutoTitle.Init);

    newAutoTitle.ItemsDiag = newAutoTitle.ItemsDiag || {};
    newAutoTitle.ItemsDiag = $.extend({
        getItemsDiag : function(){
            $('#items').empty();
            var table = $('<div></div>');

            var bottom = $('.newAutoTitleBottom');
            var params = $.extend({
                "s" : "",
                "status" : 2,
                "catId" : null,
                "cid" : null,
                "sort" : 1,
                "lowBegin" : 0,
                "ps" : 5,
                "topEnd" : 100
            },newAutoTitle.util.getParams());

            bottom.tmpage({
                currPage: 1,
                pageSize: 5,
                pageCount:1,
                useSmallPageSize: true,
                ajax: {
                    param : params,
                    on: true,
                    dataType: 'json',
                    url: "/Titles/getItemsWithDiagResultAndLstOptimise",
                    callback:function(data){
                        newAutoTitle.ItemsDiag.createItemsWithDiagResult(data.res);
                        newAutoTitle.Event.initEvent();

                    }
                }

            });
        },
        createItemsWithDiagResult : function(res){
            var tbody = $('.datalist tbody');
            tbody.empty();
            if(res === undefined || res == null || res.length <= 0) {
                return;
            }
            $(res).each(function(i, item){
                item.lastOptimiseDay = (item.lastOptimiseTs == null) ? "未优化过" :
                    (Math.floor((new Date().getTime() - item.lastOptimiseTs) / (24 * 3600000))) == 0 ? "今天" :
                        (Math.floor((new Date().getTime() - item.lastOptimiseTs) / (24 * 3600000)) + "天前");
            });
            tbody.append($('#itemRow').tmpl(res));

        }
    },newAutoTitle.ItemsDiag);

    newAutoTitle.util = newAutoTitle.util || {};
    newAutoTitle.util = $.extend({
        checkItemsCountLimitOrNot : function(){
            return (parseInt($.cookie("userItemsCount")) - parseInt($.cookie("itemCountLimit"))) > 0 ? true : false;
        },
        getParams : function(){
            var params = {};
            var status = $("#itemStatus option:selected").val();
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
        putIntoTitle : function(text,spanObj,target){
            var container = target.parent().parent().parent();
            var newTitle = container.find('.title_input').val();
            if(newAutoTitle.util.countCharacters(newTitle+text) > 60) {
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
                end.left = container.find(".title_input").offset().left+"px";
                end.top = container.find(".title_input").offset().top+"px";
                newAutoTitle.util.flyFromTo(start,end,spanObj,function(){

                    container.find(".title_input").val(newTitle+text);
                    container.find(".title_input").trigger("keyup");
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
            newAutoTitle.util.flyFromTo(start,end,$this,null)
        },
        flyFromTo : function(start,end,flyObj,callback){
            var obj = flyObj.clone();
            obj.css("position","absolute");
            obj.css('left',start.left);
            obj.css('top',start.top);
            obj.css('z-index',10000);
            obj.appendTo($('body'));

            obj.animate({top:end.top,left:end.left},1500, function(){
                obj.fadeOut(1000,function(){
                    obj.remove();
                });
                callback && callback();
            });
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
        }
    },newAutoTitle.util);

    newAutoTitle.diagDetail = newAutoTitle.diagDetail || {};
    newAutoTitle.diagDetail = $.extend({
        createDiagInfo : function(numIid) {
            if(numIid === undefined || numIid == null || numIid <= 0) {
                return;
            }
            var detaileRow = newAutoTitle.Init.container.find('.detailRow[numIid="'+numIid+'"]');
            var detailContainer = detaileRow.find('.detailRowContainer');
            var info = {};
            $.post("/Titles/estimateSearchWordByNumIid", {numIid : numIid}, function(words) {
                if(words === undefined || words == null || words.length <= 0) {
                    info = newAutoTitle.diagDetail.getItemInfo(numIid);
                } else {
                    $(words).each(function(i, word){
                        word.newScore = (word.scount > 0) ? Math.floor(parseInt(word.pv) / parseInt(word.scount)) : 0;
                    });
                    info = newAutoTitle.diagDetail.getItemInfo(numIid, words);
                }
                var itemDiagInfo = $('#itemDiagInfo').tmpl(info);
                itemDiagInfo.find('.original_test_key .key_list2 table tbody').append($('#estimateWordsTbody').tmpl(words));
                detailContainer.empty();
                detailContainer.append(itemDiagInfo);
                newAutoTitle.Event.setItemDiagEvent(itemDiagInfo);
                detaileRow.show();
            })


        },
        createOptimise : function(numIid){
            if(numIid === undefined || numIid == null || numIid <= 0) {
                return;
            }
            var detaileRow = newAutoTitle.Init.container.find('.detailRow[numIid="'+numIid+'"]');
            var detailContainer = detaileRow.find('.detailRowContainer');
            var title = newAutoTitle.Init.container.find('.itemRow[numIid="'+numIid+'"] .titleTd a').attr("name");
            var cid = newAutoTitle.Init.container.find('.itemRow[numIid="'+numIid+'"]').attr("cid");
            var optimise = $('#optimise').tmpl({title:title, numIid: numIid, cid: cid});
            detailContainer.empty();
            detailContainer.append(optimise);
            newAutoTitle.Event.setOptimiseEvent(optimise);
            detaileRow.show();
        },
        getItemInfo : function(numIid, words){
            var info = {};
            info.numIid = numIid;
            var itemRow = newAutoTitle.Init.container.find('.itemRow[numIid="'+numIid+'"]');
            info.title = itemRow.find('.titleTd a').attr("name");
            info.score = itemRow.find('.scoreTd span').text();
            info.titleLength = newAutoTitle.util.countCharacters(info.title);
            if(words === undefined || words == null || words.length <= 0) {
                info.estimateWords = [];
            } else {
                info.estimateWords = words;
            }
            return info;
        }
    },newAutoTitle.diagDetail);

    newAutoTitle.Event = newAutoTitle.Event || {};
    newAutoTitle.Event = $.extend({
        setBatchRecommendEvent : function(){
            $('.batchAllUseRecommend').click(function(){
                /*if(!TM.isAutoOnekeyOK){
                    TM.Alert.load("亲，您的宝贝总数(<span style='color: red;margin: 0 8px 0 px;'>"+TM.totalCount+"</span>)超过亲订购版本限制的宝贝数(<span style='color: red;margin: 0 8px 0 px;'>"+TM.userVersionCount+"</span>)，请升级后重试");
                    return;
                }*/
                $.get('/items/itemCatCount',function(itemCatCount){
                    $.get('/items/sellerCatCount',function(sellerCatCount){
                        newAutoTitle.Event.batchRecommend(itemCatCount,sellerCatCount,0);
                    })
                });
            });
            $('.batchAllUseGuanfang').click(function(){
                /*if(!TM.isAutoOnekeyOK){
                    TM.Alert.load("亲，您的宝贝总数(<span style='color: red;margin: 0 8px 0 px;'>"+TM.totalCount+"</span>)超过亲订购版本限制的宝贝数(<span style='color: red;margin: 0 8px 0 px;'>"+TM.userVersionCount+"</span>)，请升级后重试");
                    return;
                }*/
                $.get('/items/itemCatCount',function(itemCatCount){
                    $.get('/items/sellerCatCount',function(sellerCatCount){
                        newAutoTitle.Event.batchRecommend(itemCatCount,sellerCatCount,1);
                    })
                });
            });
            $('.batchAllUseGuanfangRecomm').click(function(){
                /*if(!TM.isAutoOnekeyOK){
                    TM.Alert.load("亲，您的宝贝总数(<span style='color: red;margin: 0 8px 0 px;'>"+TM.totalCount+"</span>)超过亲订购版本限制的宝贝数(<span style='color: red;margin: 0 8px 0 px;'>"+TM.userVersionCount+"</span>)，请升级后重试");
                    return;
                }*/
                $.get('/items/itemCatCount',function(itemCatCount){
                    $.get('/items/sellerCatCount',function(sellerCatCount){
                        newAutoTitle.Event.batchRecommend(itemCatCount,sellerCatCount,2);
                    })
                });
            });
        },

        batchRecommend : function(itemCatCount, sellerCatCount, recMode){

            var idArr = [];
            var titles = [];
            $('.ui-dialog').remove();
            $('.tmAlertDetail').remove();

            var htmls = [];
//        htmls.push("<form id='autorecommendopt' style='text-align: center;' ><table style='margin: 0 auto;'>");
            var pushCatNames = function(res, name, rowClass){
                if(res && res.length > 0){
                    htmls.push('<tr class="'+rowClass+'"><td>'+name+':</td><td><select><option cid="0">所有分类</option>');
                    $.each(res, function(i, elem){
                        if(elem.count > 0){
                            htmls.push('<option cid="'+elem.id+'">'+elem.name+'('+elem.count+')'+'</option>');
                        }
                    });
                    htmls.push('</select></td></tr>');
                }
            }

            pushCatNames(itemCatCount, '淘宝类目', 'itemCat');
            pushCatNames(sellerCatCount, '自定义类目', 'sellerCat');
            var opt = $(htmls.join(''));
            //   }

            var content = $('<div></div>');
            content.append($('#shopRecTmpl').tmpl());
            content.find('tbody').prepend(opt);



            TM.Alert.loadDetail(content,700,390,function(){
                $.ajax({
                    url : '/titles/batchChangeAll',
                    type : 'post',
                    data:{
                        sellerCatId : content.find('.sellerCat option:selected').attr('cid'),
                        itemCatId : content.find('.itemCat option:selected').attr('cid'),
                        status : content.find('.status  option:selected').attr('value'),
                        recMode : recMode,
                        'opt.fixedStart' : content.find('input[name=fixedStart]').val(),
                        'opt.allSale' : content.find('.allsale option:selected').attr('value'),
                        'opt.keepBrand' : content.find('input[name=brand]:checked').attr('value'),
                        'opt.keepSerial' : content.find('input[name=serialNum]:checked').attr('value'),
                        'opt.mustExcluded' : content.find('input[name=mustExcluded]').val(),
                        'opt.toAddPromote' : content.find('input[name=promotewords]:checked').attr('value'),
                        'opt.noColor' : content.find('input[name=noColor]:checked').attr('value'),
                        'opt.noNumber' : content.find('input[name=noColor]:checked').attr('value')
                    },
                    timeout: 200000,
                    success : function(data){
                        //autoTitle.util.createBatchOPResult(data)

                        if (data.success == false) {
                            var message = data.message;
                            if (message === undefined || message == null || message == "") {
                                message = "自动标题任务提交失败，请联系我们！";
                            }
                            alert(data.message);
                            return;
                        } else {
                            //alert("系统提交了自动标题的后台任务，请进入任务中心查看");

                            TM.Alert.showDialog('由于宝贝数量过多，系统已为您提交了自动标题的<span style="font-weight: bold; color: #a10000;">后台任务</span>，是否立即进入<span style="font-weight: bold; color: #a10000;">任务中心</span>查看？',
                                400,300,function(){
                                    location.href = "/newAutotitle/titletask";
                                },function(){},"提示");
                        }

                    }
                });
            },'全店标题优化选项');
        },
        initEvent : function(){
            newAutoTitle.Event.setItemTheadSortEvent();
            newAutoTitle.Event.setItemScoreClickEvent();
            newAutoTitle.Event.setOptimiseClickEvent();
        },
        setItemTheadSortEvent : function(){
            newAutoTitle.Init.container.find('#itemScoreThead').unbind('click')
                .click(function(){
                    if($(this).find('#scoresort').hasClass("sort_down")) {
                        $(this).find('#scoresort').removeClass("sort_down");
                        $(this).find('#scoresort').addClass("sort_up");
                        newAutoTitle.Init.container.find('#itemsSortBy option[tag="sortByScoreUp"]').attr("selected", true);
                        newAutoTitle.Init.container.find('#goSearchItems').trigger("click");
                    } else {
                        $(this).find('#scoresort').removeClass("sort_up");
                        $(this).find('#scoresort').addClass("sort_down");
                        newAutoTitle.Init.container.find('#itemsSortBy option[tag="sortByScoreDown"]').attr("selected", true);
                        newAutoTitle.Init.container.find('#goSearchItems').trigger("click");
                    }
                });
        },
        setItemScoreClickEvent : function() {
            newAutoTitle.Init.container.find('.itemScore').unbind('click')
                .click(function(){
                    var numIid = $(this).attr("numIid");
                    newAutoTitle.diagDetail.createDiagInfo(numIid);
                });

        },
        setOptimiseClickEvent : function() {
            newAutoTitle.Init.container.find('.btn_strengthen').unbind('click')
                .click(function(){
                    var numIid = $(this).parent().attr("numIid");
                    newAutoTitle.diagDetail.createOptimise(numIid);
                });
        },
        setItemDiagEvent : function(itemDiagInfo){
            itemDiagInfo.find('.w_close_btn').unbind('click')
                .click(function(){
                    $(this).parent().parent().parent().parent().hide();
                });
            itemDiagInfo.find('.btn_strengthen').unbind('click')
                .click(function(){
                    var numIid = $(this).attr("numIid");
                    newAutoTitle.diagDetail.createOptimise(numIid);
                });
        },
        setOptimiseEvent : function(optimise){
            optimise.find('.w_close_btn').unbind('click')
                .click(function(){
                    $(this).parent().parent().parent().parent().hide();
                });
            optimise.find('.title_test_btn').unbind('click')
                .click(function(){
                    var numIid = $(this).parent().parent().attr('numIid');
                    newAutoTitle.diagDetail.createDiagInfo(numIid);
                });
            optimise.find('.auto_create_btn').unbind('click')
                .click(function(){
                    var numIid = $(this).parent().parent().attr('numIid');
                    var $this = $(this);
                    $.get("/Titles/getRecommendByNumIid", {numIid: numIid}, function(data){
                        if(data === undefined || data == null) {
                            return;
                        }
                        $this.parent().parent().find('.title_input_li input').val(data);
                    });
                });
            optimise.find('.save_current_title').unbind('click')
                .click(function(){
                    var numIid = $(this).parent().parent().attr('numIid');
                    var newTitle = $(this).parent().parent().find('.title_input_li input').val();
                    $.get("/Titles/rename", {numIid: numIid, title: newTitle}, function(data){
                        if(data === undefined || data == null) {
                            TM.Alert.load("修改标题失败，请重试或联系客服");
                            return;
                        }
                        if(data.success == false) {
                            TM.Alert.load("修改标题失败，请重试或联系客服");
                            return;
                        }
                        $(this).parent().parent().find('.title_input_li input').val(newTitle);
                    });
                });
            optimise.find('.opTab').unbind('click')
                .click(function(){
                    if($(this).hasClass('current')) {
                        return;
                    }
                    optimise.find('.current').removeClass('current');
                    $(this).addClass('current');
                    var tag = optimise.find('.current').attr('tag');
                    var target = optimise.find('.tab_1_div');
                    var params = {};
                    params.numIid = optimise.attr("numIid");
                    params.title = optimise.attr("title");
                    params.cid = optimise.attr("cid")
                    switch (tag) {
                        // 推荐标题
                        case "recommend_title" :
                            newAutoTitle.Words.initRecommendTitle(target, params);
                            break;
                        // 类目热搜词
                        case "cat_top" :
                            newAutoTitle.Words.initCatTopWords(target, params);
                            break;
                        // 促销词
                        case "promotion" :
                            newAutoTitle.Words.initPromotionWords(target, params);
                            break;
                        // etao Top词
                        case "topkey" :
                            newAutoTitle.Words.initTopKeyWords(target, params);
                            break;
                        // 直通车热词
                        case "bussearch" :
                            newAutoTitle.Words.initBusSearchWords(target, params);
                            break;
                        // 宝贝属性词
                        case "item_props" :
                            newAutoTitle.Words.initItemPropsWords(target, params);
                            break;
                        // 历史标题记录
                        case "history_rename" :
                            newAutoTitle.Words.initHistoryRenameWords(target, params);
                            break;
                        default :
                            newAutoTitle.Words.initCatTopWords(target, params);
                            break;
                    }

                });
            optimise.find('.opTab').eq(0).trigger("click");
        },
        setAddToTitleClick : function(optimise){
            optimise.find('span.addTextWrapper').unbind('click')
                .click(function(){

                });
        }
    },newAutoTitle.Event);

    newAutoTitle.Fly = $.extend({
        // user like this

        fly : function($this){
            var start = {}, end = {};
            start.left = $this.offset().left+"px";
            start.top = $this.offset().top+"px";
            end.left = $('#task-going-count').offset().left+"px";
            end.top = $('#task-going-count').offset().top+"px";
            autoTitle.Fly.flyFromTo(start,end,function(){$('#task-going-count').html(parseInt($('#task-going-count').html())+1)})
        },
        flyFromTo : function(start,end,callback){
            $('#fly-from-to-img').remove();
            var img = $('<img id="fly-from-to-img" src="/img/favicon.png" style="z-index:200001;width: 16px;height: 16px;position: absolute;top:'+start.top+';left: '+start.left+'"/>');
            img.appendTo($('body'));

            img.animate({top:end.top,left:end.left},1500, function(){
                img.fadeOut(1000);
                callback && callback();
            });
        },
        centerFly: function() {
            var start = {};
            var end = {};

            var windowWidth = $(window).width();
            var windowHeight = $(window).height();
            var scrollLeft = $(document).scrollLeft();
            var scrollTop = $(document).scrollTop();

            var left = (windowWidth) / 2 + scrollLeft;
            var top = (windowHeight) / 2 + scrollTop;
            start.left = left + "px";
            start.top = top + "px";

            end.left = $('#task-going-count').offset().left+"px";
            end.top = $('#task-going-count').offset().top+"px";
            newAutoTitle.Fly.flyFromTo(start,end,function(){$('#task-going-count').html(parseInt($('#task-going-count').html())+1)})
        }
    },newAutoTitle.Fly);

    newAutoTitle.Words = newAutoTitle.Words || {};
    newAutoTitle.Words = $.extend({
        initRecommendTitle : function(target, params){
            target.empty();
            target.append($('#recommendTitleTmpl').tmpl());
            RecommendTitle.init.doInit(target, params);
        },
        initCatTopWords : function(target, params){
            target.empty();
            target.append($('#catTopWords').tmpl());
            CatTopWord.init.doInit(target.find('.cat-top-word-container'), params.itemId, target);
        },
        initPromotionWords : function(target, params){
            target.empty();
            target.append($('#promoteWordsTmpl').tmpl());
            PromoteWord.init.doInit(target);
        },
        initTopKeyWords : function(target, params){
            target.empty();
            target.append($('#blackHorseWords').tmpl());
            $.get("/Home/getBaseIdFromCid", {numIid: params.itemId}, function(data){
                if(data === undefined || data == null) {
                    topKey.init.doInit(target);
                    return;
                }
                if(data.success == false) {
                    topKey.init.doInit(target);
                    return;
                }
                var cid = parseInt(data.message);
                topKey.init.doInit(target, cid);
            });
        },
        initBusSearchWords : function(target, params, order, sort, s){
            target.empty();

            newAutoTitle.Words.genCWords(target, params);

            target.append($('#busSearchTmpl').tmpl());
            target.find(".busSearchText").keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    target.find(".busSearchBtn").click();
                }
            });
            target.find(".busSearchBtn").click(function() {
                var searchText = target.find(".busSearchText").val();
                if (searchText == "") {
                    //alert("请先输入查询条件");
                    //return;
                }
                BusSearch.init.search(target, params, order, sort, searchText);
            });

            BusSearch.init.search(target, params, order, sort, s);
        },
        initItemPropsWords : function(target, params){
            target.empty();
            target.append($('#itemPropsTmpl').tmpl());
            ItemProp.init.doInit(target, params.numIid)
        },
        initHistoryRenameWords : function(target, params){
            target.empty();
            target.append($('#renameHistoryTmpl').tmpl());
            RenameHistory.init.doInit(target, params);
        },
        genCWords : function(target, params) {
            $.post("/titles/getCWords",{title:params.title, cid:params.cid},function(estimateWords){
                var parseWords = target.find('.parseWords');
                parseWords.empty();
                for(var i = 0; i < estimateWords.length; i++) {
                    parseWords.append(genKeywordSpan.gen({
                        "text":estimateWords[i],
                        "callback":null,
                        spanClass:'baseblock',
                        enableStyleChange:true,
                        addBtn : false,
                        enableShadow:false
                    }));
                }
                parseWords.append("<span class='clearfix'></span>")

                target.find('.longTailBlock .baseblock').click(function(){
                    target.find('.longTailSearchText').val($(this).text());
                    target.find('.longTailSearchBtn').trigger("click");
                });
                target.find('.recommendWordBlock .baseblock').click(function(){
                    target.find('.recommendSearchText').val($(this).text());
                    target.find('.recommendSearchBtn').trigger("click");
                });
                target.find('.busSearchBlock .baseblock').click(function(){
                    target.find('.busSearchText').val($(this).text());
                    target.find('.busSearchBtn').trigger("click");
                });
                target.find('.busSearchBlock .baseblock:eq(0)').click();
                target.find('.longTailBlock .baseblock:eq(0)').click();
                target.find('.recommendWordBlock .baseblock:eq(0)').click();
            });
        }
    },newAutoTitle.Words);

    var RenameHistory = RenameHistory || {};
    RenameHistory.init = RenameHistory.init || {};

    RenameHistory.init = $.extend({
        doInit : function(target, params) {
            target.find('.rename-history-pagging').tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount:1,
                ajax: {
                    on: true,
                    dataType: 'json',
                    url: "/titles/renameHistory",
                    param:{numIid : params.numIid},
                    callback:function(data){
                        if(!data) {
                            TM.Alert.load("亲，获取数据出错啦，请重试或联系客服");
                        } else {
                            if(data.res.length > 0) {
                                target.find('.renameHistoryTable').find('tbody').remove();
                                target.find('.renameHistoryTable').append(RenameHistory.init.createRenameHistoryTbody(data.res));
                            }

                        }
                    }
                }
            });
        },
        createRenameHistoryTbody : function (results) {
            var tbody = $('<tbody></tbody>');
            $(results).each(function(i,result){
                var even = "";
                if(i%2 == 0){
                    even = "even";
                }
                tbody.append('<tr class="'+even+'" numIid="'+result.numIid+'">' +
                    '<td class="rename-history-oldtitle">'+result.oldTitle+'</td>'+
                    '<td class="rename-history-newtitle">'+result.newTitle+'</td>'+
                    '<td><span class="set-old-title-back tmbtn yellow-btn">还原</span></td>'+
                    '</tr>');
            });
            tbody.find('.set-old-title-back').click(function(){
                var oldTitle = $(this).parent().parent().find('.rename-history-oldtitle').text();
                var numIid = $(this).parent().parent().attr("numIid");
                var data = {};
                data.numIid = numIid;
                data.title = oldTitle;
                //弹出loading动画
                ModifyTitle.util.showLoading();
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

                        ModifyTitle.util.hideLoading();
                    }
                });
            });
            return tbody;
        }
    },RenameHistory.init);

    var ItemProp = ItemProp || {};

    ItemProp.init = ItemProp.init || {};

    ItemProp.init = $.extend({
        doInit: function(target, itemId) {
            $.get("/Titles/props",{numIid:itemId}, function(data){
                var table = target.find('.propTable');
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
                    tmpTr.append($('<td class="word" style="width: 258px;"><span class="inlineblock" style="width: 98px;">'+word.key+':</span>'+genKeywordSpan.gen({"text":word.value,"callback":"","enableStyleChange":true})+'</td>'));
                });
                table.find('span').css("margin","5px 0 5px 0");
                table.find('span.addTextWrapper').click(function(){
                    newAutoTitle.util.putIntoTitle($(this).text(),$(this), target);
                });
            });
        }
    }, ItemProp.init);

    var RecommendTitle = RecommendTitle || {};

    RecommendTitle.init = RecommendTitle.init || {};

    RecommendTitle.init = $.extend({
        doInit: function(target, params) {
            $.get('/Titles/getRecommends', {numIids: params.numIid}, function(data){
                if(data === undefined || data == null || data.length <= 0) {
                    target.find('.recommendTitleTable tr .recommendTitleContent').val("亲, 暂无推荐标题，请重试或联系客服");
                    return;
                }
                target.find('.recommendTitleTable tr .recommendTitleContent').val(data[0].title);
            });
            $.get("/Items/fenxiaoTitles", {numIids: params.numIid}, function(data){
                if(data === undefined || data == null || data.res.length <= 0) {
                    target.find('.recommendTitleTable tr .fenxiaoTitleContent').val("亲, 暂无推荐标题，请重试或联系客服");
                    target.find('.recommendTitleTable tr .fenxiaoRecommendTitleContent').val("亲, 暂无推荐标题，请重试或联系客服");
                    return;
                }
                if(data.res[0] == null) {
                    target.find('.recommendTitleTable tr .fenxiaoTitleContent').val("亲, 暂无推荐标题，请重试或联系客服");
                    target.find('.recommendTitleTable tr .fenxiaoRecommendTitleContent').val("亲, 暂无推荐标题，请重试或联系客服");
                    return;
                }

                target.find('.recommendTitleTable tr .fenxiaoTitleContent').val(data.res[0].originTitle);
                target.find('.recommendTitleTable tr .fenxiaoRecommendTitleContent').val(data.res[0].title);
                target.find('.recommendTitleTable .fenxiaoRecommendTitleTr').show();
                target.find('.recommendTitleTable .fenxiaoTitleTr').show();
            });
            RecommendTitle.init.setUserRecommendTitleClickEvent(target, params);
        },
        setUserRecommendTitleClickEvent : function(target, params){
            // 使用推荐标题
            target.find('.useRecommendTitle').unbind('click')
                .click(function(){
                    var numIid = params.numIid
                    var newTitle = $(this).parent().parent().find('.recommendTitleContent').val();
                    $.get("/Titles/rename", {numIid: numIid, title: newTitle}, function(data){
                        if(data === undefined || data == null) {
                            TM.Alert.load("修改标题失败，请重试或联系客服");
                            return;
                        }
                        if(data.success == false) {
                            TM.Alert.load("修改标题失败，请重试或联系客服");
                            return;
                        }
                        newAutoTitle.Init.container.find('.detailRow[numIid="'+params.numIid+'"] .title_input').val(newTitle);
                        newAutoTitle.Init.container.find('.itemRow[numIid="'+params.numIid+'"] .titleTd a').text(newTitle);
                        newAutoTitle.Init.container.find('.itemRow[numIid="'+params.numIid+'"] .titleTd a').attr("name", newTitle);
                    });
                });

            // 使用分销标题
            target.find('.useFenxiaoTitle').unbind('click')
                .click(function(){
                    var numIid = params.numIid
                    var newTitle = $(this).parent().parent().find('.fenxiaoTitleContent').val();
                    $.get("/Titles/rename", {numIid: numIid, title: newTitle}, function(data){
                        if(data === undefined || data == null) {
                            TM.Alert.load("修改标题失败，请重试或联系客服");
                            return;
                        }
                        if(data.success == false) {
                            TM.Alert.load("修改标题失败，请重试或联系客服");
                            return;
                        }
                        newAutoTitle.Init.container.find('.detailRow[numIid="'+params.numIid+'"] .title_input').val(newTitle);
                        newAutoTitle.Init.container.find('.itemRow[numIid="'+params.numIid+'"] .titleTd a').text(newTitle);
                        newAutoTitle.Init.container.find('.itemRow[numIid="'+params.numIid+'"] .titleTd a').attr("name", newTitle);
                    });
                });

            // 使用分销推荐标题
            target.find('.useFenxiaoRecommendTitle').unbind('click')
                .click(function(){
                    var numIid = params.numIid
                    var newTitle = $(this).parent().parent().find('.fenxiaoRecommendTitleContent').val();
                    $.get("/Titles/rename", {numIid: numIid, title: newTitle}, function(data){
                        if(data === undefined || data == null) {
                            TM.Alert.load("修改标题失败，请重试或联系客服");
                            return;
                        }
                        if(data.success == false) {
                            TM.Alert.load("修改标题失败，请重试或联系客服");
                            return;
                        }
                        newAutoTitle.Init.container.find('.detailRow[numIid="'+params.numIid+'"] .title_input').val(newTitle);
                        newAutoTitle.Init.container.find('.itemRow[numIid="'+params.numIid+'"] .titleTd a').text(newTitle);
                        newAutoTitle.Init.container.find('.itemRow[numIid="'+params.numIid+'"] .titleTd a').attr("name", newTitle);
                    });
                });
        }
    }, RecommendTitle.init);

    var PromoteWord = PromoteWord || {};

    PromoteWord.init = PromoteWord.init || {};

    PromoteWord.init = $.extend({
        doInit: function(target) {
            target.find(".promoteBlock").html("");
            var data = {};
            $.ajax({
                url: '/titles/getPromoteWords',
                dataType: 'json',
                type: 'post',
                data: data,
                error: function() {
                },
                success: function (promoteArray) {
                    var container = target.find('.promoteBlock');
                    var rowCount = 5;
                    var count = 0;
                    var html = [];

                    html.push('<table class="promoteWordsTable">');
                    html.push('<tbody>');
                    html.push('<tr>')
                    $(promoteArray).each(function(index, promoteWord) {
                        var spanObj = genKeywordSpan.gen({"text":promoteWord,"callback":newAutoTitle.util.putIntoTitle,"enableStyleChange":true});
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
                        newAutoTitle.util.putIntoTitle($(this).text(),$(this), target);
                    });

                    $(table).appendTo(container);
                }
            });
        },
        createWordSpan: function(promoteWord) {
            var spanObj = $("<span></span>");
            spanObj.html(promoteWord);
            return spanObj;
        }
    }, PromoteWord.init);

    var CatTopWord = CatTopWord || {};


    CatTopWord.init = CatTopWord.init || {};
    CatTopWord.init = $.extend({
        doInit: function(container, numIid, target) {
            CatTopWord.container = container;

            container.find(".first-cat-select").change(function() {
                CatTopWord.category.initSecondCatSelect([0, 0, 0], target);
                $("body").focus();//在ie6下使其失去焦点
            });
            container.find(".second-cat-select").change(function() {
                CatTopWord.category.initThirdCatSelect([0, 0, 0], target);
                $("body").focus();//在ie6下使其失去焦点
            });
            container.find(".third-cat-select").change(function() {
                CatTopWord.show.doShow();
                $("body").focus();//在ie6下使其失去焦点
            });

            CatTopWord.init.initUserCatInfo(numIid, target);


            var sortTdObjs = container.find(".sort-td");
            sortTdObjs.click(function() {
                if ($(this).hasClass("sort-up")) {
                    $(this).removeClass("sort-up");
                    $(this).addClass("sort-down");
                } else {
                    $(this).removeClass("sort-down");
                    $(this).addClass("sort-up");
                }
                sortTdObjs.removeClass("current-sort");
                $(this).addClass("current-sort");
                CatTopWord.show.doShow();
            });

        },
        initUserCatInfo: function(numIid, target) {

            var container = CatTopWord.init.getContainer();
            var catNameObj = container.find(".catName");

            if (numIid === undefined || numIid == null) {
                numIid = 0;
            }

            $.ajax({
                url : '/CatTopWord/findUserMostCat',
                data : {numIid: numIid},
                type : 'post',
                success : function(dataJson) {

                    /*if (CatTopWord.util.judgeAjaxResult(dataJson) == false) {

                     }*/

                    var catJsonArray = CatTopWord.util.getAjaxResult(dataJson);
                    var userCatIdArray = [];
                    if (catJsonArray === undefined || catJsonArray == null || catJsonArray.length <= 0) {
                        catJsonArray = [];
                    }

                    var catNames = '';
                    if(catJsonArray.length <= 0) {
                        catNameObj.hide();
                    } else {
                        $(catJsonArray).each(function(index, catJson) {
                            if (index > 0) {
                                catNames = " > " + catNames;
                            }
                            catNames = catJson.name + catNames;
                            userCatIdArray[catJsonArray.length - index - 1] = catJson.cid;
                        });

                        catNameObj.find(".cat-name-span").html(catNames);
                        catNameObj.show();
                    }



                    for (var i = 0; i < 3; i++) {
                        if (userCatIdArray.length < 3) {
                            userCatIdArray[userCatIdArray.length] = 0;
                        } else {
                            break;
                        }
                    }

                    CatTopWord.category.initFirstCatSelect(userCatIdArray, target);

                    catNameObj.find(".cat-name-span").unbind().click(function() {
                        CatTopWord.category.initFirstCatSelect(userCatIdArray, target);
                    });
                }
            });
        },
        getContainer: function() {
            return CatTopWord.container;
        }
    }, CatTopWord.init);


    CatTopWord.category = CatTopWord.category || {};
    CatTopWord.category = $.extend({
        initFirstCatSelect: function(userCatIdArray, target) {

            var container = CatTopWord.init.getContainer();

            var firstCatSelectObj = container.find(".first-cat-select");


            $.ajax({
                url : '/cattopword/findLevel1',
                data : {},
                type : 'post',
                success : function(dataJson) {

                    firstCatSelectObj.html("");

                    if (CatTopWord.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    var catJsonArray = CatTopWord.util.getAjaxResult(dataJson);
                    if (catJsonArray === undefined || catJsonArray == null || catJsonArray.length <= 0) {
                        catJsonArray = [];
                        firstCatSelectObj.html('<option value="0" selected="selected">暂无类目，请联系我们</option>');

                    } else {
                        var allHtml = '';
                        $(catJsonArray).each(function(index, catJson) {
                            allHtml += '<option value="' + catJson.cid + '" >' + catJson.name + '</option>';
                        });

                        firstCatSelectObj.html(allHtml);

                        //默认选中类目
                        var targetCid = CatTopWord.category.getSelectCid(userCatIdArray, 0);
                        CatTopWord.category.doSelectOption(firstCatSelectObj, targetCid);
                    }

                    //触发二级类目
                    CatTopWord.category.initSecondCatSelect(userCatIdArray, target);

                }
            });

        },
        getSelectCid: function(userCatIdArray, selectIndex) {
            if (userCatIdArray === undefined || userCatIdArray == null || userCatIdArray.length <= 0) {
                return 0;
            }
            if (userCatIdArray.length < selectIndex + 1) {
                return 0;
            }
            return userCatIdArray[selectIndex];
        },
        doSelectOption: function(selectObj, targetCid) {
            if (targetCid === undefined || targetCid == null || targetCid <= 0) {
                selectObj.find('option:eq(0)').attr("selected", true);
            } else {
                var selectOptionObj = selectObj.find('option[value="' + targetCid + '"]');
                if (selectOptionObj.length > 0) {
                    selectOptionObj.attr("selected", true);
                } else {
                    selectObj.find('option:eq(0)').attr("selected", true);
                }
            }
        },
        initSecondCatSelect: function(userCatIdArray, target) {
            var container = CatTopWord.init.getContainer();

            var firstCatSelectObj = container.find(".first-cat-select");
            var parentCid = firstCatSelectObj.val();

            var secondCatSelectObj = container.find(".second-cat-select");


            $.ajax({
                url : '/cattopword/findLevel2or3',
                data : {parentCid: parentCid},
                type : 'post',
                success : function(dataJson) {
                    secondCatSelectObj.html("");
                    if (CatTopWord.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    var catJsonArray = CatTopWord.util.getAjaxResult(dataJson);
                    if (catJsonArray === undefined || catJsonArray == null || catJsonArray.length <= 0) {
                        catJsonArray = [];
                        secondCatSelectObj.html('<option value="0">暂无类目</option>');

                    } else {
                        var allHtml = '';
                        $(catJsonArray).each(function(index, catJson) {
                            allHtml += '<option value="' + catJson.cid + '" >' + catJson.name + '</option>';
                        });

                        secondCatSelectObj.html(allHtml);

                        //默认选中类目
                        var targetCid = CatTopWord.category.getSelectCid(userCatIdArray, 1);
                        CatTopWord.category.doSelectOption(secondCatSelectObj, targetCid);
                    }

                    //触发三级类目
                    CatTopWord.category.initThirdCatSelect(userCatIdArray, target);

                }
            });
        },
        initThirdCatSelect: function(userCatIdArray, target) {
            var container = CatTopWord.init.getContainer();

            var secondCatSelectObj = container.find(".second-cat-select");
            var parentCid = secondCatSelectObj.val();

            var thirdCatSelectObj = container.find(".third-cat-select");


            $.ajax({
                url : '/cattopword/findLevel2or3',
                data : {parentCid: parentCid},
                type : 'post',
                success : function(dataJson) {
                    thirdCatSelectObj.html("");
                    if (CatTopWord.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    var catJsonArray = CatTopWord.util.getAjaxResult(dataJson);
                    if (catJsonArray === undefined || catJsonArray == null || catJsonArray.length <= 0) {
                        catJsonArray = [];
                        thirdCatSelectObj.html('<option value="0">暂无类目</option>');

                    } else {
                        var allHtml = '';
                        $(catJsonArray).each(function(index, catJson) {
                            allHtml += '<option value="' + catJson.cid + '" >' + catJson.name + '</option>';
                        });

                        thirdCatSelectObj.html(allHtml);

                        //默认选中类目
                        var targetCid = CatTopWord.category.getSelectCid(userCatIdArray, 2);
                        CatTopWord.category.doSelectOption(thirdCatSelectObj, targetCid);
                    }

                    CatTopWord.show.doShow(target);

                }
            });
        }
    }, CatTopWord.category);


    CatTopWord.show = CatTopWord.show || {};
    CatTopWord.show = $.extend({
        doShow: function(target) {

            var paramData = CatTopWord.show.getParamData();
            if (paramData == null) {
                return;
            }

            var container = CatTopWord.init.getContainer();

            var tbodyObj = container.find(".catWordTable tbody.cat-top-word-tbody");
            tbodyObj.html("");

            container.find(".paging-div").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount:1,
                //isJsonp: true,
                ajax: {
                    param : paramData,
                    on: true,
                    dataType: 'json',
                    url: "/cattopword/findCatTopWords",
                    //url: "http://chedao.tobti.com/commons/findCatTopWords",
                    //url: "http://localhost:9000/commons/findCatTopWords",
                    callback:function(dataJson){

                        var wordJsonArray = dataJson.res;

                        tbodyObj.html("");

                        if (wordJsonArray === undefined || wordJsonArray == null || wordJsonArray.length <= 0) {

                            var html = '<tr><td colspan="8" style="height:40px;">亲，当前类目下暂无数据！</td></tr>';

                            tbodyObj.html(html);

                            return;

                        } else {
                            var trHtmlArray = [];
                            $(wordJsonArray).each(function(index, wordJson) {

                                var trHtml = CatTopWord.show.createRowHtml(index, wordJson);

                                trHtmlArray[trHtmlArray.length] = trHtml;
                            });

                            tbodyObj.html(trHtmlArray.join(""));

                            tbodyObj.find(".add-word-btn").unbind().click(function() {
                                var btnObj = $(this);
                                var trObj = btnObj.parent().parent();
                                if (trObj.hasClass("top-word-tr") == false) {
                                    alert("系统出现异常，找不到关键词，请联系我们！");
                                    return;
                                }
                                var targetWord = trObj.find(".word-td").html();

                                $.post('/KeyWords/addMyWord',{word:targetWord},function(data){
                                    TM.Alert.load(data,400, 300, function(){}, "", "已成功添加到词库", 3000);
                                });
                            });
                            tbodyObj.find('span.addTextWrapper').unbind("click")
                                .click(function(){
                                    newAutoTitle.util.putIntoTitle($(this).text(),$(this), target);
                            });
                        }




                    }
                }

            });


        },
        createRowHtml: function(index, wordJson) {

            var pv = "-";
            var click = "-";
            var ctr = "-";
            var price = '-';
            if (wordJson.pv > 0) {
                pv = parseInt(wordJson.pv) > 10000 ? new Number(wordJson.pv).toTenThousand(1) : wordJson.pv;
                click = parseInt(wordJson.click) > 10000 ? new Number(wordJson.click).toTenThousand(1) : wordJson.click;
                ctr = (wordJson.ctrInt / 10000).toPercent(2);
                price = '￥' + (wordJson.price / 100).toFixed(2);
            }

            var itemCount = "-";
            if (wordJson.itemCount > 0) {
                itemCount = parseInt(wordJson.itemCount) > 10000 ? new Number(wordJson.itemCount).toTenThousand(1) : wordJson.itemCount;
            }

            var score = "-";
            if (wordJson.pv > 0 && wordJson.itemCount > 0) {
                score = Math.round(wordJson.pv / wordJson.itemCount).toFixed(0);
            }


            var trCss = '';
            if (index % 2 == 1) {
                trCss = 'even';
            }

            var trHtml = '' +
                '<tr class="top-word-tr ' + trCss + '">' +
                '   <td class="word-td">' + genKeywordSpan.gen({"text":wordJson.word,"callback":newAutoTitle.util.putIntoTitle,"enableStyleChange":true}) + '</td>' +
                '   <td style="">' + pv + '</td>' +
                '   <td style="">' + click + '</td>' +
                '   <td style="">' + ctr + '</td>' +
                '   <td style="">' + itemCount + '</td>' +
                '   <td style="">' + score + '</td>' +
                '   <td style="">' + price + '</td>' +
                '   <td style=""><span class="tmbtn sky-blue-btn add-word-btn">添加到词库</span>' +
                '</tr>' +
                '';

            return trHtml;

        },
        getParamData: function() {
            var paramData = {};

            var container = CatTopWord.init.getContainer();
            var firstCatSelectObj = container.find(".first-cat-select");
            var secondCatSelectObj = container.find(".second-cat-select");
            var thirdCatSelectObj = container.find(".third-cat-select");


            paramData.firstCid = firstCatSelectObj.val();
            paramData.secondCid = secondCatSelectObj.val();
            paramData.thirdCid = thirdCatSelectObj.val();

            var orderObj = container.find(".current-sort");
            var orderBy = "";
            var isDesc = false;

            if (orderObj.length <= 0) {
                orderBy = "pv";
            } else {
                orderBy = orderObj.attr("orderBy");
                if (orderObj.hasClass("sort-down"))
                    isDesc = true;
                else
                    isDesc = false;
            }

            paramData.orderBy = orderBy;
            paramData.isDesc = isDesc;

            return paramData;

        }
    }, CatTopWord.show);


    CatTopWord.util = CatTopWord.util || {};
    CatTopWord.util = $.extend({
        judgeAjaxResult: function(resultJson) {
            var msg = resultJson.message;
            if (msg === undefined || msg == null || msg == "") {
                msg = resultJson.msg;
            }
            if (msg === undefined || msg == null || msg == "") {

            } else {
                alert(msg);
            }
            var isSuccess = resultJson.success;
            if (isSuccess === undefined || isSuccess == null) {
                isSuccess = resultJson.isOk;
            }

            return isSuccess;
        },
        getAjaxResult: function(resultJson) {
            var json = resultJson.results;
            if (json === undefined || json == null) {
                json = resultJson.res;
            }

            return json;
        }
    }, CatTopWord.util);

    var BusSearch = BusSearch || {};
    BusSearch.init = BusSearch.init || {};
    BusSearch.init = $.extend({
        search : function(target, params, order, sort, s){
            if(order === undefined || order == ''){
                order = 'pv';
            }
            if(sort === undefined || sort == ''){
                sort = 'desc';
            }
            if(s === undefined ){
                s = '';
            }
            target.find('.bus-search-pagging').tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount:1,
                ajax: {
                    on: true,
                    dataType: 'json',
                    url: "/words/busSearch",
                    param:{numIid:params.numIid,order:order,sort:sort,word:s},
                    callback:function(data){
                        target.find('.bus-search-tbody').empty();
                        if(data != null){
                            if(data.res.length > 0) {
                                $(data.res).each(function(i,myword){
                                    if(myword.word != "") {
                                        if(parseInt(myword.pv) <= 0) {
                                            return;
                                        }
                                        var pv = parseInt(myword.pv) > 10000 ? new Number(myword.pv).toTenThousand(2) : myword.pv;
                                        var click = myword.click > 10000 ? new Number(myword.click).toTenThousand(2) : (myword.click > 0 ? myword.click : 0);
                                        var scount = myword.scount > 10000 ? new Number(myword.scount).toTenThousand(2) : (myword.scount > 0 ? myword.scount : "~");
                                        var score = parseInt(myword.scount) <= 0 ? 0 : Math.floor(parseInt(myword.pv) / parseInt(myword.scount));
                                        var clickRate = myword.pv > 0 ? new Number(myword.click * 1.0 / myword.pv).toPercent(2) : "~";
                                        target.find('.bus-search-tbody').append($('<tr><td class="word-content">'+genKeywordSpan.gen({"text":myword.word,"callback":"","enableStyleChange":true,"spanClass":'addTextWrapperSmall'})+'</td><td>'+pv+'</td><td>'+click+'</td><td>'+scount+'</td><td>'+clickRate+'</td><td>'+myword.transRate+'</td><td>'+score+'</td><td>'+myword.bidPrice+'</td><td class="add-to-mywords" style="cursor: pointer;"><span class="addTextWrapper tmbtn long-yellow-btn" style="width: 112px;margin: 0;border: 0;padding:0;">添加到词库</span></td></tr>'))
                                    }
                                })
                                target.find('.bus-search-tbody').find('tr:even').addClass('even');
                                target.find(".bussearch tbody").find('.add-to-mywords span').click(function(){
                                    $.post('/KeyWords/addMyWord',{word:$(this).parent().parent().find('.word-content').text()},function(data){
                                        TM.Alert.load(data,400, 300, function(){}, "", "添加到词库", 3000);
                                    });
                                });
                                target.find(".bussearch tbody").find('.addTextWrapperSmall').click(function(){
                                    //newAutoTitle.util.putIntoTitle($(this).text(),$(this));
                                });
                                target.find('span.addTextWrapperSmall').click(function(){
                                    newAutoTitle.util.putIntoTitle($(this).text(),$(this), target);
                                });
                            } else{
                                target.find('.bus-search-tbody').append($('<td colspan="7"><p style="font-size: 16px;text-align: center;font-family: 微软雅黑;">亲，没有搜索到相关的直通车热词呢</p></td>'));
                            }

                        }
                    }
                }
            });
        }
    },BusSearch.init);

    newAutoTitle.Fly = $.extend({
        // user like this

        fly : function($this){
            var start = {}, end = {};
            start.left = $this.offset().left+"px";
            start.top = $this.offset().top+"px";
            end.left = $('#task-going-count').offset().left+"px";
            end.top = $('#task-going-count').offset().top+"px";
            newAutoTitle.Fly.flyFromTo(start,end,function(){$('#task-going-count').html(parseInt($('#task-going-count').html())+1)})
        },
        flyFromTo : function(start,end,callback){
            $('#fly-from-to-img').remove();
            var img = $('<img id="fly-from-to-img" src="/img/favicon.png" style="z-index:200001;width: 16px;height: 16px;position: absolute;top:'+start.top+';left: '+start.left+'"/>');
            img.appendTo($('body'));

            img.animate({top:end.top,left:end.left},1500, function(){
                img.fadeOut(1000);
                callback && callback();
            });
        },
        centerFly: function() {
            var start = {};
            var end = {};

            var windowWidth = $(window).width();
            var windowHeight = $(window).height();
            var scrollLeft = $(document).scrollLeft();
            var scrollTop = $(document).scrollTop();

            var left = (windowWidth) / 2 + scrollLeft;
            var top = (windowHeight) / 2 + scrollTop;
            start.left = left + "px";
            start.top = top + "px";

            end.left = $('#task-going-count').offset().left+"px";
            end.top = $('#task-going-count').offset().top+"px";
            newAutoTitle.Fly.flyFromTo(start,end,function(){$('#task-going-count').html(parseInt($('#task-going-count').html())+1)})
        }
    },newAutoTitle.Fly);

    TM.newAutoTitle = newAutoTitle;



    //jQuery回车键提交
    $("#searchWord").keyup(function(event){
        if (event.which == 13) {
            $("#goSearchItems").trigger("click");
        }
    })

    //做搜索框的提示功能
    var searchText=$("#searchWord");
    searchText.click(function () {
        if ($.trim($(this).val()) == "输入关键词搜索商品") {
            searchText.val("")
        }
    }).blur(function () {
        if ($.trim($(this).val()) == "") {
            searchText.val("输入关键词搜索商品")
        }
    });


})(jQuery, window));