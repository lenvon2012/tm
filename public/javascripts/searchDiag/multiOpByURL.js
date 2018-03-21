/**
 * css:  <link rel="stylesheet" media="screen" href="@{'/public/stylesheets/commChoose/commChoose.css'}">
 * js:   $.getScript('/js/jquery.pagination.js'
 * multiOpByURL.createChoose.createOrRefleshCommsDiv(options);
 * options : {
 *    itemsURL:'',      //URL to list items
 *    pn:1,
 *    ps:8,
 *    enableSearch:true    //enable or disable to search by keys
 * }
 * @type {*}
 */

var multiOpByURL = multiOpByURL || {};

((function ($, window) {
    multiOpByURL.createChoose=multiOpByURL.createChoose||{};
    multiOpByURL.createChoose= $.extend({
        createOrRefleshCommsDiv:function(options){

            options = $.extend({
                itemsURL:'',
                pn:1,
                ps:8,
                enableSearch:true,
                relation:{
                    isRelation:false,
                    relationPlanId: "",
                    newName:"",
                    modelId:0
                },
                waterMark:{
                    waterCallback:""
                },
                callbackFun:"",
                actionCallback: null
            }, options);

            options.numIidList = '';

            if($("#itemChooseDiv").length>0){
                $("#itemChooseDiv").css("display","block");
                var top =  $(document).scrollTop() + 10;
                $("#itemChooseDiv").css("top",top + "px");
                multiOpByURL.fillComms.getCommsList(options);
            }
            else {
                var itemChooseDiv=$('<div id="itemChooseDiv"></div>');
                itemChooseDiv.css("left",($(window).width()-850)/2+"px");
                itemChooseDiv.css("display","block");
                var top =  $(document).scrollTop() + 10;
                itemChooseDiv.css("top",top + "px");
                itemChooseDiv.append($('<b class="b1"></b>'));
                itemChooseDiv.append($('<b class="b2 d1"></b>'));
                itemChooseDiv.append($('<b class="b3 d1"></b>'));
                itemChooseDiv.append($('<b class="b4 d1"></b>'));

                var outDiv=$('<div class="b d1 k"></div>');
                outDiv.append(multiOpByURL.createChoose.createItemChooseContent(options));
                itemChooseDiv.append(outDiv);

                itemChooseDiv.append($('<b class="b4b d1"></b>'));
                itemChooseDiv.append($('<b class="b3b d1"></b>'));
                itemChooseDiv.append($('<b class="b2b d1"></b>'));
                itemChooseDiv.append($('<b class="b1b d1"></b>'));

                itemChooseDiv.appendTo($('body'));
                multiOpByURL.Event.setCunstomEvent(options);
            }
        },
        createItemChooseContent:function(options){
            var itemChooseContent=$('<div id="itemChooseContent"></div>');
            itemChooseContent.append(multiOpByURL.createChoose.createItemChooseSearch(options));
            itemChooseContent.append(multiOpByURL.createChoose.createAddOrDetele(options));
            itemChooseContent.append($('<div class="clear"></div>'));
            itemChooseContent.append(multiOpByURL.createChoose.createCommArea(options));
            itemChooseContent.append(multiOpByURL.createChoose.createItemBottom(options));
            return itemChooseContent;
        },
        createItemChooseSearch:function(options){
            var itemChooseSearch=$('<div id="itemChooseSearch"></div>');
            itemChooseSearch.append($('<span id="titleSearchSpan">&nbsp;宝贝标题:</span>'));
            if(options.enableSearch)
                itemChooseSearch.append($('<input type="text" id="ItemSearch"/>'));
            else
                itemChooseSearch.append($('<input type="text" id="ItemSearch" disabled="disabled"/>'));
            //itemChooseSearch.append($('<img src="/public/images/btns/search1.png" class="ItemSearchBtn">'));
            itemChooseSearch.append($('<a href="javascript:void(0);" class="ItemSearchBtn tmbtn sky-blue-btn" ><span><span>立即搜索</span></span></a>'));
            itemChooseSearch.append($('<a href="javascript:void(0);" class="reflesh tmbtn sky-blue-btn" ><span><span>刷新</span></span></a>'));
            itemChooseSearch.append($('<img src="/public/images/close.png" id="itemChooseDivClose">'));
            return itemChooseSearch;
        },
        createAddOrDetele:function(options){
            var addOrDetele=$('<div id="addOrDetele"></div>');
           /* addOrDetele.append($('<a href="javascript:void(0);" id="selectPageAll"><span style="margin-left: 2px;margin-top: 5px;" class="tmbtn long-sky-blue-btn"><span>全选本页宝贝</span></span></a>'));
            addOrDetele.append($('<a href="javascript:void(0);" id="unSelectPageAll"><span style="margin-top: 5px;" class="tmbtn long-sky-blue-btn"><span>取消本页宝贝</span></span></a>'));
            addOrDetele.append($('<a href="javascript:void(0);" id="unSelectAll"><span style="margin-top: 5px;" class="tmbtn long-sky-blue-btn"><span>取消所有宝贝</span></span></a>'));*/
            addOrDetele.append($('<p  id="PaginationItems" class="paginationItems"></p>'));
            return addOrDetele;
        },
        createCommArea:function(options){
            var commArea=$('<div id="commArea"></div>');
            multiOpByURL.fillComms.getCommsList(options);
            return commArea;
        },
        createItemBottom:function(options){
            var itemBottom=$('<div id="itemBottom"></div>');
            //itemBottom.append($('&nbsp;&nbsp; <img id="selectOK" src="/public/images/btns/selectOK.png"/>'));
            itemBottom.append($('&nbsp;&nbsp; <span id="selectOK" class="tmbtn sky-blue-btn">选择完成</span>'));
            itemBottom.append($('&nbsp;&nbsp; <span id="closeWindow" class="tmbtn sky-blue-btn">关闭窗口</span>'));
            /*itemBottom.append($(' &nbsp;&nbsp;<span id="selected">已选中宝贝数:</span><span id="selectNum">0</span>'));*/
            return itemBottom;
        }
    },multiOpByURL.createChoose);

    multiOpByURL.fillComms = multiOpByURL.fillComms || {};
    multiOpByURL.fillComms= $.extend({
        getCommsList:function(options){
            var data = {};
            if("/Relation/getRelatedItems"==options.itemsURL)  {
                data.planId=  options.relation.relationPlanId;
            }
            data.s=$("#ItemSearch").val();
            data.pn=options.pn;
            data.ps=options.ps;
            var callback = function(currentPage, jq){
                multiOpByURL.fillComms.findComms(currentPage, jq,options);
            }
            $.ajax({
                url : options.itemsURL,
                data : data,
                type : 'get',
                success : function(data) {
                    var totalCount = data.pnCount*data.ps;
                    var per_page = data.ps;
                    multiOpByURL.fillComms.initPagination(totalCount, per_page, 1,callback);
                }
            });
        },
        initPagination:function(totalCount, per_page, currentPage,callback){
            currentPage--;
            $("#PaginationItems").pagination(totalCount, {
                num_display_entries : 3, // 主体页数
                num_edge_entries : 2, // 边缘页数
                current_page: currentPage,
                callback : callback,
                items_per_page : per_page,// 每页显示多少项
                prev_text : "&lt上一页",
                next_text : "下一页&gt"
            });
        },
        findComms:function(currentPage, jq, options){
            var data = {};
            if("/Relation/getRelatedItems"==options.itemsURL)  {
                data.planId=  options.relation.relationPlanId;
            }
            data.s=$("#ItemSearch").val();
            data.pn=currentPage+1;
            data.ps=options.ps;
            $.ajax({
                //async : false,
                url : options.itemsURL,
                data : data,
                type : 'post',
                success : function(data) {
                    multiOpByURL.fillComms.fill(data.res,options);
                    multiOpByURL.Event.setDynamicEvent(options);
                }
            });
        },
        fill:function(comms,options){

            var commArea=$("#commArea");
            commArea.empty();
            var commInf;
            if(comms.length > 0){
                for(var i=0;i<comms.length;i++){
                    commInf=multiOpByURL.fillComms.createCommInf(comms[i],options);
                    commArea.append(commInf);
                }
            }

        },
        createCommInf:function(comm, options){
            var commInf=$('<div class="commInf"></div>');
            var oneComm=multiOpByURL.fillComms.createOneComm(comm, options);
            commInf.append(oneComm);
            return commInf;
        },
        createOneComm:function(comm, options){
            var oneComm=$('<div class="oneComm" style="border: 2px solid #aaa;"></div>');
            oneComm.attr("numIid",comm.id);
            var commMask=multiOpByURL.fillComms.createCommMask(comm, options);
            var commLink=multiOpByURL.fillComms.createCommImg(comm, options);
            var commTitle=multiOpByURL.fillComms.createCommTitle(comm, options);
            var commPrice=multiOpByURL.fillComms.createCommPrice(comm, options);
            oneComm.append(commMask);
            oneComm.append(commLink);
            oneComm.append(commTitle);
            oneComm.append(commPrice);
            //if(multiOpByURL.rule.numIidList.search(comm.id)>=0){
            if(options.numIidList.search(comm.id)>=0){
                oneComm.css("border-color","#277ED0 ");
                oneComm.find(".commMask").css("background-color","#277ED0 ");
            }
            return oneComm;
        },
        createCommMask:function(comm, options){
            var commMask=$('<div class="commMask" style="background-color: #aaa;"></div>');
            //if(multiOpByURL.rule.numIidList.search(comm.id)<0)
            if(options.numIidList.search(comm.id)<0)
                commMask.append("点击图片添加");
            else commMask.append("已选中");
            return commMask;
        },
        createCommImg:function(comm, options){
            var imgObj=$('<img class="commImg" src="" />');
            imgObj.attr("src",comm.picURL);
            return imgObj;
        },
        createCommTitle:function(comm, options){
            var linkDiv=$('<div class="linkDiv"></div>');
            var aObj=$('<a class="commTitle" href="#" target="_blank"></a>');
            var url="http://item.taobao.com/item.htm?id=" + comm.id;
            aObj.attr("href",url);
            aObj.html(comm.name.substr(0,22)+"...");
            linkDiv.append(aObj);
            return linkDiv;
        },
        createCommPrice:function(comm, options){
            var price=$(' <span class="commPriceSpan"></span>');
            price.append(comm.price+"元");
            return price;
        }
    },multiOpByURL.fillComms);

    multiOpByURL.Event = multiOpByURL.Event ||{};
    multiOpByURL.Event= $.extend({
        setDynamicEvent:function(options){
            multiOpByURL.Event.setCommImgEvent(options);
        },
        setCunstomEvent:function(options){
            multiOpByURL.Event.setItemSearchBtnEvent(options);
            multiOpByURL.Event.setSelectPageAllEvent(options);
            multiOpByURL.Event.setRefleshEvent(options);
            multiOpByURL.Event.setItemChooseDivCloseEvent(options);
            multiOpByURL.Event.setUnSelectPageAllEvent(options);
            multiOpByURL.Event.setUnSelectAllEvent(options);
            multiOpByURL.Event.setSelectOKEvent(options);
            multiOpByURL.Event.setItemChooseDivScrollEvent(options);
        },
        setItemChooseDivScrollEvent : function(data){
            /*$(window).scroll(function(){
             $('#itemChooseDiv').css('top', $(document).scrollTop()+10);
             });*/
        },
        setItemsChooseSpanEvent:function(options){
            $("#itemsChooseSpan").click(function(){
                multiOpByURL.createChoose.createOrRefleshCommsDiv(options);
                /*    $("#itemChooseDiv").css("left",($(window).width()-850)/2+"px");
                 // $("#itemChooseDiv").css("left",($(window).width()-800)/2+"px");
                 $("#itemChooseDiv").css("display","block");
                 multiOpByURL.fillComms.getCommsList(); */
            });
        },

        setCommImgEvent:function(options){
            $(".commImg").click(function(){
                var commMask = $(this).parent().find(".commMask");
                if(commMask.html()=="点击图片添加"){
                    // 取消已经选中的
                    $('.commMask').html("点击图片添加");
                    $('.commMask').css("background-color","#aaa");
                    $('.commMask').parent().css("border-color","#aaa");
                    // 设置当前选中的
                    commMask.html("已选中");
                    commMask.css("background-color","#277ED0");
                    commMask.parent().css("border-color","#277ED0");
                    options.numIidList = commMask.parent().attr("numIid");

                }
                else if(commMask.html()=="已选中"){
                    commMask.html("点击图片添加");
                    commMask.css("background-color","#aaa");
                    commMask.parent().css("border-color","#aaa");
                    options.numIidList="";
                }
            });
        },

        setItemChooseDivCloseEvent:function(options){
            $('#itemChooseDivClose').click(function(){
                //multiOpByURL.rule.numIidList="";
                options.numIidList="";
                $("#itemChooseDiv").remove();
            });
            $('#closeWindow').click(function(){
                //multiOpByURL.rule.numIidList="";
                options.numIidList="";
                $("#itemChooseDiv").remove();
            });
        },

        setItemSearchBtnEvent:function(options){
            $(".ItemSearchBtn").click(function(){
                multiOpByURL.fillComms.getCommsList(options);
            });
        },

        setRefleshEvent:function(options){
            $(".reflesh").click(function(){
                multiOpByURL.fillComms.getCommsList(options);
            });
        },
        setSelectPageAllEvent:function(options){
            $("#selectPageAll").click(function(){
                $(".commMask").html("已选中");
                $(".commMask").css("background-color","#277ED0 ");
                $(".oneComm").css("border-color","#277ED0 ");
                $(".oneComm").each(function(){
                    // if(multiOpByURL.rule.numIidList.search($(this).attr("numIid"))<0)
                    //     multiOpByURL.rule.numIidList = multiOpByURL.rule.numIidList+$(this).attr("numIid")+",";
                    if(options.numIidList.search($(this).attr("numIid"))<0)
                        options.numIidList = options.numIidList+$(this).attr("numIid")+",";
                });
                //$("#selectNum").html(multiOpByURL.rule.numIidList.split(",").length-1);
                $("#selectNum").html(options.numIidList.split(",").length-1);
            });
        },
        setUnSelectPageAllEvent:function(options){
            $("#unSelectPageAll").click(function(){
                $(".commMask").html("点击图片添加");
                $(".commMask").css("background-color","#aaa");
                $(".oneComm").css("border-color","#aaa");
                $(".oneComm").each(function(){
                    // if(multiOpByURL.rule.numIidList.search($(this).attr("numIid"))>=0)
                    //     multiOpByURL.rule.numIidList = multiOpByURL.rule.numIidList.replace(($(this).attr("numIid")+","),"");
                    if(options.numIidList.search($(this).attr("numIid"))>=0)
                        options.numIidList = options.numIidList.replace(($(this).attr("numIid")+","),"");
                });
                //$("#selectNum").html(multiOpByURL.rule.numIidList.split(",").length-1);
                $("#selectNum").html(options.numIidList.split(",").length-1);
            });
        },
        setUnSelectAllEvent:function(options){
            $("#unSelectAll").click(function(){
                $(".commMask").html("点击图片添加");
                $(".commMask").css("background-color","#aaa");
                $(".oneComm").css("border-color","#aaa");
                $(".oneComm").each(function(){
                    //   multiOpByURL.rule.numIidList = "";
                    options.numIidList = "";
                });
                $("#selectNum").html(0);
            });
        },
        setSelectOKEvent:function(options){
            $("#selectOK").click(function(){
                $("#itemChooseDiv").remove();
                options.callbackFun(options.numIidList);
            });
        }

    },multiOpByURL.Event);

    multiOpByURL.Init=multiOpByURL.Init||{};
    multiOpByURL.Init= $.extend({
        init:function(){
            multiOpByURL.rule.numIidList="";
            //multiOpByURL.Event.setItemsChooseSpanEvent();
        }
    },multiOpByURL.Init);

    multiOpByURL.rule = multiOpByURL.rule || {};

    multiOpByURL.rule = $.extend({
        numIidList:""
    }, multiOpByURL.rule);

    $(document).ready(function(){
        multiOpByURL.Init.init();
    });

})(jQuery, window));
