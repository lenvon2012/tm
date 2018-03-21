((function ($, window) {

    var wordexport = wordexport || {};

    wordexport.Init = wordexport.Init || {};
    wordexport.Init = $.extend({
        init : function(){
            wordexport.Init.initSearchArea();
            wordexport.Init.initDiagArea();
            //wordexport.Init.initYingxiaoDialog();
        },
        initYingxiaoDialog : function(){
            $.ajax({
                url : '/popularize/getUserInfo',
                data : {},
                type : 'post',
                success : function(dataJson) {
                    $.get('/OPUserInterFace/showXufeiOrNot',function(data){
                        if(data == "show"){
                            wordexport.Init.showXufei(dataJson.version);
                        }
                    });
                }
            });
        },
        showXufei : function(version){
            var key = "500以下版本";
            if(version <= 20){
                key = "500以下版本";
            } else {
                key = "500以上版本";
            }
            $.get("/OPUserInterFace/xufeishowed",function(data){
                if(data == "unshowed"){
                    var hour = new Date().getHours();
                    var remain;
                    if(hour <= 8){
                        remain = 12;
                    } else if(hour <= 12){
                        remain = 9;
                    } else if(hour <= 16){
                        remain = 6;
                    } else if(hour <= 20){
                        remain = 3;
                    } else if(hour <= 22){
                        remain = 2;
                    } else {
                        remain = 1;
                    }
                    $.ajax({
                        type:'GET',
                        url :'/OPUserInterFace/freeLink',
                        dataType:'text',
                        data :{key:key},
                        success :function(data){
                            if(data == "找不到用户名" || data=="用户名为空" || data=="找不到营销链接" || data=="获取营销链接出错"){
                                //TM.Alert.load(data);
                            } else {
                                var html = ''+
                                    '<p style="text-align:center;font-size: 60px;color: red;font-weight: bold;margin: 20px 0px;">年中大促</p>'+
                                    '<p style="text-align:center;font-size:50px;font-weight: bold;margin: 20px 0px;">恭喜你获得免费半年 奖励</p>'+
                                    '<p style="text-align:center;font-size:50px;font-weight: bold;margin: 20px 0px;">点击以下链接获取哦亲</p>'+
                                    '<p style="text-align:center;margin: 20px 0px;" class="free-link"><a target="_blank" href="'+data+'"><span class="free-link" style="font-size: 35px;font-weight: bold;">'+data+'</span></a></p>'+
                                    '<p style="text-align:center;">备注：此活动每天仅限15人，剩余<span class="remain" style="font-size: 60px;font-weight: bold;">'+remain+'</span>人</p>'+
                                    '';
                                var content = $(html);
                                /*content.find('.free-link').click(function(){
                                 $('.ui-dialog ').hide();
                                 $('.ui-widget-overlay').hide();
                                 });*/
                                var redshow = function(){
                                    content.find('.remain').toggleClass('red');
                                    content.find('.free-link').toggleClass('red');
                                }
                                setInterval(redshow,300);
                                TM.Alert.loadDetail(content,1000,650,function(){
                                    return true;
                                },"年中大促")
                            }
                        }
                    })
                    // no show any more
                    $.get('/OPUserInterFace/setShowed',function(data){
                        return true;
                    });
                }
            });
        },
        isFengxiao : function(isFenxiao){
            wordexport.ItemsDiag.getItemsDiag(false);
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
        },
        initDiagArea : function(){
            $.get("/Home/firstSync",function(){
                $.getScript("/Status/user",function(data){
                    wordexport.Init.isFengxiao();
                    // wordexport.ItemsDiag.getItemsDiag(true);
                })
            });

        }
    },wordexport.Init);

    wordexport.ItemsDiag = wordexport.ItemsDiag || {};
    wordexport.ItemsDiag = $.extend({
        getItemsDiag : function(isFengxiao){
            $('.diagResultArea').empty();
            $('.autoTitleBottom').remove();
            var table = $('<div></div>');
            var bottom = $('<div class="autoTitleBottom" style="text-align: center;"></div>');
            var params = $.extend({
                "s":"",
                "status":2,
                "catId":null,
                "sort":1,
                "lowBegin":0,
                "ps":5,
                "topEnd":100
            },wordexport.util.getParams());
            if(window.location.hash == '#score-desc'){
                params.sort = 2;
                window.location.hash = "";
            }
            bottom.tmpage({
                currPage: 1,
                pageSize: 5,
                pageCount:1,
                useSmallPageSize: true,
                ajax: {
                    param : params,
                    on: true,
                    dataType: 'json',
                    url: "/Titles/getItemsWithDiagResult",
                    callback:function(data){
                        $('.diagResultArea').empty();
                        $('.autoTitleDiv').find(".clear").remove();
                        $('.autoTitleDiv').find(".wordexportBottom").remove();
                        $('.diagResultArea').append(wordexport.ItemsDiag.createItemsWithDiagResult(data.res,isFengxiao));
                        $('.autoTitleDiv').append($('<div class="clear"></div>'));
                        $('.autoTitleDiv').append(bottom);
                        wordexport.Event.initEvent();
                    }
                }

            });
        },
        createItemsWithDiagResult : function(res,isFengxiao){
            var itemsWithDiagResult = $('<div class="itemsWithDiagResult"></div>');
            itemsWithDiagResult.append(wordexport.ItemsDiag.createDiagBody(res,isFengxiao));
            return itemsWithDiagResult;
        },
        createDiagBody : function(results,isFengxiao){
            var body = $('<div class="diagBody"></div>');
            for(var i = 0; i<results.length;i++) {
                var item = results[i];
                body.append(wordexport.ItemsDiag.createSingleDiagRes(item,i,isFengxiao));
            }
            return body;
        },
        createSingleDiagRes : function(result,i,isFengxiao){
            var singleDiagRes = $('<div class="singleDiagRes" numIid="'+result.id+'"></div>');
            singleDiagRes.append(wordexport.ItemsDiag.createDiagBriefInf(result,i,isFengxiao));
            //singleDiagRes.append(wordexport.ItemsDiag.createShowDetailTag(result));
            singleDiagRes.append(wordexport.ItemsDiag.createDiagDetailInf(result));
            // singleDiagRes.append($('<br />'));
            return singleDiagRes;
        },
        createDiagBriefInf : function(result,i,isFengxiao){
            var briefInf = $('<div class="diagBriefInf"></div>');
            var ulObj = $('<ul class="singleDiagULObj"></ul>');
            ulObj.attr("numIid",result.numIid);
            ulObj.append(wordexport.ItemsDiag.createItemImgLiObj(result));
            ulObj.append(wordexport.ItemsDiag.createTitleLiObj(result,i,isFengxiao));
            ulObj.append(wordexport.ItemsDiag.createOpLiObj(result,i,isFengxiao));
            briefInf.append(ulObj);
            return briefInf;
        },
        createOpLiObj : function(){
            var liObjSelect = $('<li class="fl" style="margin-top: 14px;" ></li>');
            liObjSelect.append($('<span class="btn btn-info lookup-hotwords">查看热词</span>'));
            return  liObjSelect;
        },
        createCheckboxLiObj : function(result){
            var liObjSelect = $('<li class="checkBoxLi fl" style="" ></li>');
            liObjSelect.append($('<input type="checkbox" style="width:14px;" name="subCheck" class="subCheckBox" numIid="'+result.id+'">'));
            return  liObjSelect;
        },
        createItemImgLiObj : function(result){
            var liObjimg = $('<li class=" fl" style="width:100px;text-align: center;" ></li>');

            var aObj = $('<a target="_blank"></a>');
            var url = "http://item.taobao.com/item.htm?id=" + result.id;
            aObj.attr("href",url);
            var imgObj = $('<img style="width:60px;height:60px;border: 1px solid #5DB0F9;">');
            imgObj.attr("src",result.picURL);
            aObj.append(imgObj);
            liObjimg.append(aObj);
//        liObjimg.append("");
            return liObjimg;
        },
        createTitleLiObj : function(result,i,isFengxiao){
            var liObj = $('<li class=" fl" style="width:550px;margin-top: 13px;" ></li>');
            liObj.append(wordexport.ItemsDiag.createOldTitle(result,i));
            return liObj;
        },
        createOldTitle : function(result,i){
            var oldTitle = $('<div class="oldTitle" style="font-family: 微软雅黑;">宝贝标题：</div>');
            var oldTitleContent = $('<input type="text" style="width: 450px;" class="oldTitleContent" id="oldTitleRemainObjInput'+i+'">');
            oldTitle.append(oldTitleContent);
            oldTitleContent.val(result.title);
            return oldTitle;
        },
        createStatusLiObj : function(result){
            var liObj = $('<li class=" fl" style="width:5px;text-align: center;line-height: 100px;" ></li>');
            var span = $('<span></span>');
            span.html(1);
            liObj.append(span);
            return liObj;
        },
        createSalesCountLiObj : function(result){
            var liObj = $('<li class=" fl" style="width:95px;text-align: center;line-height: 100px;" ></li>');
            var span = $('<span></span>');
//        span.html('月销量');
            liObj.append('<div style="height:32px;line-height: 32px;">月销量<span>'+(result.tradeItemNum>10000?(result.tradeItemNum/10000 + '万'):result.tradeItemNum)+'</span>件</div>');
            liObj.append("<div>"+(result.status > 0 ?"在售":"在库") +"</div>");
            return liObj;
        },
        createShowDetailTag : function(result){
            var showDetailTag = $('<div class="clear" style="height: 20px;margin-bottom:10px"></div>');
            var leftLint = $('<div class="leftLine"></div>');
            var tagImg = $('<a class="expandButton showDetailTag left spreadBtn" numIid="'+result.id+'"></a>');
            var rightLine = $('<div class="rightLine"></div>');
            showDetailTag.append(leftLint);
            showDetailTag.append(tagImg);
            showDetailTag.append(rightLine);
            return showDetailTag;
        },
        createDiagDetailInf : function(result){
            var detailInf = $('<div class="diagDetailInf recommendDiv rowDiv" numIid="'+result.id+'"></div>');
            return detailInf;
        }
    },wordexport.ItemsDiag);

    wordexport.ItemDetail = wordexport.ItemDetail || {};
    ((function ($, window) {
        var me = wordexport.ItemDetail;

        me.init = function(container, numIid){

            if(container.find('.tabDiv').length > 0){
                return;
            }

            var tabHolder = $('<div class="tabDiv" style="width:100%;margin:0px auto 10px auto;"></div>');
            var contentHolder = $('<div class="liTargetDiv" style="text-align: center;"></div>');

            var arr = [];
            arr.push('<ul class="clearfix" iid="'+numIid+'">');
            arr.push('<li><a targetcls="hotWordsExport" href="javascript:void(0);">热词导出</a></li>');
            arr.push('</ul>');

            tabHolder.append($(arr.join('')));

            tabHolder.find('a').click(function(){
                var tabClicked = $(this);
                if(tabClicked.hasClass(('select'))){
                    return;
                }
                var con = tabClicked.parent().parent();
                var itemId = con.attr('iid');
                contentHolder.empty();
                switch(tabClicked.attr('targetcls')){
                    case 'hotWordsExport':
                        me.initHotWordsExport(contentHolder,itemId);
                        break;
                }

                tabHolder.find('.select').removeClass('select');
                tabClicked.addClass('select');
            });

            container.append(tabHolder);
            container.append(contentHolder);

            tabHolder.find('a:eq(0)').trigger('click');

        }

        me.initHotWordsExport = function(container, numIid) {
            container.empty();
            var href="/Items/downloadWords?numIid=" + numIid;
            var exportAll = $('<span class="exportAllHotWords commbutton btntext6"><a style="text-decoration: none;color: #ffffff;">导出全部热词</a></span>');
            exportAll.find("a").attr("href",href);
            container.append(exportAll);
            var wordsTable = $('<table class="wordsTable"></table>');
            wordsTable.append($('<thead><tr><td class="wordContent">热词</td><td class="wordPrice">行业出价</td><td class="wordClick">点击数</td></tr></thead>'));
            container.append(wordsTable);
            $.post("/Items/getWords",{numIid : numIid}, function(data){
                var tbody = $('<tbody></tbody>')
                var showLength = data.length > 20 ? 20 : data.length;
                for(var i = 0; i < showLength; i++) {
                    var trObj = $('<tr class="wordTr"><td class="wordContent">'+genKeywordSpan.gen({"text":data[i].word,"callback":wordexport.util.putIntoTitle,"enableStyleChange":true})+'</td><td class="wordPrice">'+data[i].price/100+'</td><td class="wordClick">'+data[i].click+'</td></tr>');
                    tbody.append(trObj);
                }
                tbody.find('span').click(function(){
                    wordexport.util.putIntoTitle($(this).text(),$(this),container);
                });
                container.append(tbody);
            });
        }

    })(jQuery, window));





    wordexport.util = wordexport.util || {};
    wordexport.util = $.extend({
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
            var newTitle = container.parent().parent().find(".oldTitleContent");
            if(wordexport.util.countCharacters(newTitle.val()+text) > 60) {
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
                newTitle.val(newTitle.val()+text);
                newTitle.trigger("keyup");
                container.parent().parent().find(".oldTitleRemainWordsNum").html(30-newTitle.val().length);
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
        createBatchOPResult : function(data){
            if(!data || data.length==0 || data.failNum == 0) {
//            TM.Alert.load("批量应用推荐标题成功"+((!data||data.length==0)?0:data.successNum)+"个，失败0个~,点击确定刷新数据",400,300,TM.sync);
                TM.Alert.loadDetail("<p>批量推荐标题成功"+((!data||data.length==0)?0:data.successNum)+
                    "个，失败0个~,点击确定刷新页面</p><p>若您对标题优化的结果不满意,您可以稍后在<b>左侧导航标题还原中心</b>进行还原操作,非常感谢</p><p>若有问题,也欢迎联系我们的客服,非常感谢</p>",
                    850,350,function(){window.location.hash='score-desc';TM.sync()},'推荐成功');
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
                TM.sync();
            });

            multiModifyArea.appendTo($("body"));
        },
        updateRecomTitle : function(data){
            $('.singleDiagRes').each(function(){
                if(data[$(this).attr("numiid")] !== undefined){
                    $(this).find('.newTitleContent').val((data[$(this).attr("numiid")]));
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
                    '<td><span class="set-old-title-back">还原</span></td>'+
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
    },wordexport.util);


    wordexport.Event = wordexport.Event || {};
    wordexport.Event = $.extend({
        initEvent : function(){
            wordexport.Event.setCheckAllEvent();
            wordexport.Event.setSubcheckEvent();
            wordexport.Event.setShowDetailTagEvent();
            wordexport.Event.setLookupHotwordsEvent();
            wordexport.Event.setSaveCurrentTitleEvent();
            wordexport.Event.setSaveRecommendTitleEvent();
            wordexport.Event.setSaveFenxiaoTitleEvent();
            wordexport.Event.setGOSearchEvent();
            wordexport.Event.setCloseWarmNoticeEvent();
            wordexport.Event.setSelectBatch();
            wordexport.Event.setAllUseRecommendBatch();
            wordexport.Event.setAllUseGuanfangBatch();
            wordexport.Event.setAllUseGuanfangRecomBatch();
            wordexport.Event.setDiagCurrentTitle();
        },
        setCheckAllEvent : function(){
            $("#checkAllItems").click(function(){
                $('input[name="subCheck"]').attr("checked",this.checked);
            });
        },
        setSubcheckEvent : function(){
            $(".subCheckBox").click(function(){
                var $subBox = $("input[name='subCheck']");
                $("#checkAllItems").attr("checked",$subBox.length == $("input[name='subCheck']:checked").length ? true : false);
            });
        },
        setLookupHotwordsEvent : function(){
            $('.lookup-hotwords').click(function(){
                if($(this).html() == "查看热词")  {
                    $(this).html("收起查看");
                    var  detail = $(this).parent().parent().parent().parent().find(".diagDetailInf");
                    wordexport.ItemDetail.init(detail,detail.attr('numIid'));
                    detail.fadeIn(300);
                }
                else {
                    $(this).html('查看热词')
                    $(this).parent().parent().parent().parent().find(".diagDetailInf").fadeOut(300);
                }
            });
            $('.lookup-hotwords:eq(0)').trigger('click');
        },
        setShowDetailTagEvent : function(){
            $('.showDetailTag').click(function(){
                if($(this).hasClass("expandButton"))  {
                    $(this).removeClass("expandButton");
                    $(this).addClass("collapseButton");
                    var  detail = $(this).parent().parent().find(".diagDetailInf");
//                cyheckbo(detail);
                    wordexport.ItemDetail.init(detail,detail.attr('numIid'));
                    detail.fadeIn(300);

//            .css("display","block");
                }
                else if ($(this).hasClass("collapseButton")) {
                    $(this).removeClass("collapseButton");
                    $(this).addClass("expandButton");
                    $(this).parent().parent().find(".diagDetailInf").fadeOut(300);
                }
            });
            $('.showDetailTag:eq(0)').trigger('click');
        },
        setSaveCurrentTitleEvent : function(){
            $('.saveCurrentTitle').click(function(){
                var currentTitle = $(this).parent().parent().parent().parent().find('.oldTitleContent').val();
                if(wordexport.util.countCharacters(currentTitle) > 60) {
                    alert("您添加的标题已超过淘宝字数限制，请删减后再提交");
                }else {
                    var numIid = $(this).parent().parent().parent().parent().attr("numIid");
                    wordexport.util.modifyTitle(numIid,currentTitle,$(this).parent().parent().parent().parent().find('.oldTitleContent'));
                }

                /*params.numIid = $(this).parent().parent().parent().parent().attr("numIid");
                 params.title = currentTitle;
                 $.post("/Titles/rename",params,function(res){
                 if(!res){
                 TM.Alert.load("标题修改失败！请您稍后重试哟!");
                 }else if(res.ok){
                 TM.Alert.load("标题修改成功！");
                 }else{
                 TM.Alert.load(res.msg);
                 }
                 });*/
            });
        },
        setDiagCurrentTitle : function(){
            $('.diagCurrentTitle').click(function(){
                var oldTitle = $(this).parent().parent().find('.oldTitleContent').val();
                if(wordexport.util.countCharacters(oldTitle) > 60) {
                    alert("您添加的标题已超过淘宝字数限制，请删减后再提交");
                } else {
                    /*if($(this).parent().parent().parent().parent().parent().parent().find('.diagDetailInf ').css("display")=="none"){
                     $(this).parent().parent().parent().parent().parent().parent().find('.showDetailTag').trigger("click");
                     } else {*/
                    /*if($(this).parent().parent().parent().parent().parent().parent().find('a[targetcls="diagResultBlock"]').hasClass("select")){
                     var contentHolder = $(this).parent().parent().parent().parent().parent().parent().find('.liTargetDiv');
                     var numIid = $(this).parent().parent().parent().parent().parent().parent().attr("numiid");
                     wordexport.ItemDetail.initDiag(contentHolder, numIid);
                     } else {
                     $(this).parent().parent().parent().parent().parent().parent().find('a[targetcls="diagResultBlock"]').trigger("click");
                     }*/
                    var contentHolder = $(this).parent().parent().parent().parent().parent().parent().find('.liTargetDiv');
                    var numIid = $(this).parent().parent().parent().parent().parent().parent().attr("numiid");
                    var title = $(this).parent().parent().find('.oldTitleContent').val();
                    var titleScoreNumber = $(this).parent().find(".titleScoreNumber");
                    $.post('/titles/singleDiag',{numIid : numIid, title : title}, function(data){
                        titleScoreNumber.html(data.score);
                        /*if(wordexport.util.countCharacters(title) > 40){
                         // titleScoreNumber.empty();
                         // titleScoreNumber.append($('<span class="titleScoreNumber">&nbsp;良好</span>'));
                         } else {
                         // titleScoreNumber.empty();
                         // titleScoreNumber.append($('<span class="titleScoreNumber">&nbsp;不够理想</span>'));
                         }*/

                        TM.Alert.load(QueryCommodity.commodityDiv.createDetail(data),820,550,function(){},false,"诊断结果: "+title);
                    });

                }

            });
        },
        setSaveRecommendTitleEvent : function(){
            $('.saveRecommendTitle').click(function(){
                var recommendTitle = $(this).parent().parent().parent().parent().find('.newTitleContent').val();
                if(wordexport.util.countCharacters(recommendTitle) > 60) {
                    alert("您添加的标题已超过淘宝字数限制，请删减后再提交");
                } else {
                    var numIid = $(this).parent().parent().parent().parent().attr("numIid");
                    wordexport.util.modifyTitle(numIid,recommendTitle,$(this).parent().parent().parent().parent().find('.oldTitleContent'));
                }
            });
        },
        setSaveFenxiaoTitleEvent : function(){
            $('.saveFenxiaoTitle').click(function(){
                var fenxiaoTitle = $(this).parent().parent().parent().parent().find('.fenxiaoTitleContent').val();
                if(wordexport.util.countCharacters(fenxiaoTitle) > 60) {
                    alert("官方标题已超过淘宝字数限制，请删减后再提交");
                } else {
                    var numIid = $(this).parent().parent().parent().parent().attr("numIid");
                    wordexport.util.modifyTitle(numIid,fenxiaoTitle,$(this).parent().parent().parent().parent().find('.oldTitleContent'));
                }
            });
        },
        setGOSearchEvent : function(){
            $('#goSearchItems').click(function(){
                wordexport.ItemsDiag.getItemsDiag($.cookie("isFenxiao"));
            });
        },
        setCloseWarmNoticeEvent : function(){
            $("#closeImg").click(function(){
                if($("#warmNotice ol").css("display")=="block")
                {
                    $("#warmNotice ol").slideUp('normal');
                    $("#closeImg").attr("src","http://img02.taobaocdn.com/imgextra/i2/1039626382/T2S5O1XoJXXXXXXXXX_!!1039626382.gif");
                }
                else if($("#warmNotice ol").css("display")=="none")
                {
                    $("#warmNotice ol").slideDown('normal');
                    $("#closeImg").attr("src","http://img03.taobaocdn.com/imgextra/i3/1039626382/T2No11XjNXXXXXXXXX_!!1039626382.gif");
                }
            });
        },
        setSelectBatch : function(){
            $('.batchSelectUseRecommend').click(function(){
                var idArr = [];
                var titles = [];
                $.each($(".itemsWithDiagResult").find(".subCheckBox:checked"),function(i, input){
                    var oThis = $(input);
                    idArr.push(oThis.attr('numIid'));
                    titles.push(oThis.parent().parent().parent().find(".newTitleContent").val());
                });
                //timeout: 30000s
                $.ajax({
                    url : '/titles/batchChange',
                    data:{numIids:idArr.join(','),titles:titles.join(',:,')},
                    timeout: 200000,
                    success : function(data){
                        wordexport.util.createBatchOPResult(data);
                    }
                });
            });
        },
        setAllUseRecommendBatch : function(){


//        $('.batchAllUseRecommend').click(function(){
//            wordexport.Event.batchRecommend();
//            if(confirm("点击确定,进行全店一个键自动标题优化")){
//                $.get('/titles/batchChangeALl',function(data){
////                    wordexport.util.createBatchOPResult(data);
//
//                })
//            }
//        });
            $('.batchAllUseRecommend').click(function(){
                $.get('/items/itemCatCount',function(itemCatCount){
                    $.get('/items/sellerCatCount',function(sellerCatCount){
                        wordexport.Event.batchRecommend(itemCatCount,sellerCatCount,0);
                    })
                });
            });
        },
        setAllUseGuanfangBatch : function() {
            $('.batchAllUseGuanfang').click(function(){
                $.get('/items/itemCatCount',function(itemCatCount){
                    $.get('/items/sellerCatCount',function(sellerCatCount){
                        wordexport.Event.batchRecommend(itemCatCount,sellerCatCount,1);
                    })
                });
            });
        },
        setAllUseGuanfangRecomBatch : function() {
            $('.batchAllUseGuanfangRecomm').click(function(){
                $.get('/items/itemCatCount',function(itemCatCount){
                    $.get('/items/sellerCatCount',function(sellerCatCount){
                        wordexport.Event.batchRecommend(itemCatCount,sellerCatCount,2);
                    })
                });
            });
        },
        advancedBatchRecommend : function(itemCatCount, sellerCatCount){
            var idArr = [];
            var titles = [];

            var opt = $("#autorecommendopt");
            opt.empty();
            // if(!opt || opt.length ==0){
            var htmls = [];
            htmls.push("<form id='autorecommendopt' style='text-align: center;' ><table style='margin: 0 auto;'>");
            htmls.push('<tr class="allsale" ><td>优化宝贝是否不限销量:</td><td><select ><option value="false">否.只优化没有销量的宝贝</option><option name="sale" value="true" >是,不限销量</option></select></td></tr>')
            htmls.push('<tr><td>是否保留标题中的品牌:</td><td><input type="radio" name="brand" value="true" checked="checked">是<input type="radio" name="brand" value="false">否</option></select></td></div>')
            htmls.push('<tr><td>是否保留货号:</td><td><input type="radio" name="serialNum" value="true" checked="checked">是<input type="radio" name="serialNum" value="false">否</td></div>')
            htmls.push('<tr><td>标题头部固定词:</td><td><input type="text" name="fixedStart" style="width:200px"></td></div>')
            htmls.push('<tr><td>标题不包含以下词(以<span style="color: red;">空格</span>分割):</td><td><input type="text" name="mustExcluded" style="width:200px"></td></div>')
            htmls.push('<tr><td>是否添加促销词:</td><td><input type="radio" name="promotewords" value="true" checked="checked">是<input type="radio" name="promotewords" value="false">否</td></div>')
            htmls.push('</table></form>');
            opt = $(htmls.join(''));
            // }


            TM.Alert.loadDetail(opt,800,570,function(){
                var idArr = [];
                $.each($(".itemsWithDiagResult").find(".subCheckBox"),function(i, input){
                    var oThis = $(input);
                    idArr.push(oThis.attr('numIid'));
                });
                $.ajax({
                    url : '/titles/advancedBatchRecommend',
                    type : 'post',
                    data:{
                        numIids : idArr.join(","),

                        'opt.fixedStart' : opt.find('input[name=fixedStart]').val(),
                        'opt.allSale' : opt.find('.allsale option:selected').attr('value'),
                        'opt.keepBrand' : opt.find('input[name=brand]:checked').attr('value'),
                        'opt.keepSerial' : opt.find('input[name=serialNum]:checked').attr('value'),
                        'opt.mustExcluded' : opt.find('input[name=mustExcluded]').val(),
                        'opt.toAddPromote' : opt.find('input[name=promotewords]:checked').attr('value')
                    },
                    timeout: 200000,
                    success : function(data){
                        //wordexport.util.createBatchOPResult(data)
                        wordexport.util.updateRecomTitle(data);
                    }
                });
            },'全店标题优化选项');
        },
        batchRecommend : function(itemCatCount, sellerCatCount, recMode){

            var idArr = [];
            var titles = [];
            $('.ui-dialog').remove();
            $('.tmAlertDetail').remove();
            //var opt = $("#autorecommendopt");

            //  if(!opt || opt.length ==0){
            var htmls = [];
            htmls.push("<form id='autorecommendopt' style='text-align: center;' ><table style='margin: 0 auto;'>");


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
            htmls.push('<tr class="status"><td>在架状态:</td><td><select><option value="2">所有宝贝</option><option value="1">在售宝贝</option><option value="0">下架宝贝</option></td></select></div>')
            htmls.push('<tr class="allsale" ><td>优化宝贝是否不限销量:</td><td><select ><option value="false">否.只优化没有销量的宝贝</option><option name="sale" value="true" >是,不限销量</option></select></td></tr>')
            htmls.push('<tr><td>是否保留标题中的品牌:</td><td><input type="radio" name="brand" value="true" checked="checked">是<input type="radio" name="brand" value="false">否</option></select></td></div>')
            htmls.push('<tr><td>是否保留货号:</td><td><input type="radio" name="serialNum" value="true" checked="checked">是<input type="radio" name="serialNum" value="false">否</td></div>')
            htmls.push('<tr><td>标题头部固定词:</td><td><input type="text" name="fixedStart" style="width:200px"></td></div>')
            htmls.push('<tr><td>标题不包含以下词(以<span style="color: red;">空格</span>分割):</td><td><input type="text" name="mustExcluded" style="width:200px"></td></div>')
            htmls.push('<tr><td>是否添加促销词:</td><td><input type="radio" name="promotewords" value="true" checked="checked">是<input type="radio" name="promotewords" value="false">否</td></div>')
            htmls.push('</table></form>');
            var opt = $(htmls.join(''));
            //   }


            TM.Alert.loadDetail(opt,800,570,function(){
                $.ajax({
                    url : '/titles/batchChangeAll',
                    type : 'post',
                    data:{
                        sellerCatId : opt.find('.sellerCat option:selected').attr('cid'),
                        itemCatId : opt.find('.itemCat option:selected').attr('cid'),
                        status : opt.find('.status  option:selected').attr('value'),
                        recMode : recMode,
                        'opt.fixedStart' : opt.find('input[name=fixedStart]').val(),
                        'opt.allSale' : opt.find('.allsale option:selected').attr('value'),
                        'opt.keepBrand' : opt.find('input[name=brand]:checked').attr('value'),
                        'opt.keepSerial' : opt.find('input[name=serialNum]:checked').attr('value'),
                        'opt.mustExcluded' : opt.find('input[name=mustExcluded]').val(),
                        'opt.toAddPromote' : opt.find('input[name=promotewords]:checked').attr('value')
                    },
                    timeout: 200000,
                    success : function(data){
                        wordexport.util.createBatchOPResult(data)
                    }
                });
            },'全店标题优化选项');
        }
    },wordexport.Event);

    TM.wordexport = wordexport;

})(jQuery, window));
