var CommChoose = CommChoose || {};

CommChoose.createChoose=CommChoose.createChoose||{};
CommChoose.createChoose= $.extend({
    createOrRefleshCommsDiv:function(callback, url, excludeNumIids){
        CommChoose.Init.init(callback, url, excludeNumIids);
        if($("#itemChooseDiv").length>0){
            $("#itemChooseDiv").css("display","block");
            var top =  $(document).scrollTop() + 10;
            $("#itemChooseDiv").css("top",top + "px");
            CommChoose.fillComms.getCommsList();
        }
        else {
            var itemChooseDiv=$('<div id="itemChooseDiv"></div>');
            itemChooseDiv.css("left",($(window).width()-850)/2+"px");
            itemChooseDiv.css("display","block");
            itemChooseDiv.append($('<b class="b1"></b>'));
            itemChooseDiv.append($('<b class="b2 d1"></b>'));
            itemChooseDiv.append($('<b class="b3 d1"></b>'));
            itemChooseDiv.append($('<b class="b4 d1"></b>'));

            var outDiv=$('<div class="b d1 k"></div>');
            outDiv.append(CommChoose.createChoose.createItemChooseContent());
            itemChooseDiv.append(outDiv);

            itemChooseDiv.append($('<b class="b4b d1"></b>'));
            itemChooseDiv.append($('<b class="b3b d1"></b>'));
            itemChooseDiv.append($('<b class="b2b d1"></b>'));
            itemChooseDiv.append($('<b class="b1b d1"></b>'));

            itemChooseDiv.appendTo($('body'));
            var top =  $(document).scrollTop() + 10;
            itemChooseDiv.css("top",top + "px");
        }


        /*
         $("#itemChooseDiv").css("left",($(window).width()-850)/2+"px");
         // $("#itemChooseDiv").css("left",($(window).width()-800)/2+"px");
         $("#itemChooseDiv").css("display","block");
         CommChoose.fillComms.getCommsList();    */
    },
    createItemChooseContent:function(){
        var itemChooseContent=$('<div id="itemChooseContent"></div>');
        itemChooseContent.append(CommChoose.createChoose.createItemChooseSearch());
        itemChooseContent.append(CommChoose.createChoose.createAddOrDetele());
        itemChooseContent.append($('<div class="clear"></div>'));
        itemChooseContent.append(CommChoose.createChoose.createCommArea());
        itemChooseContent.append(CommChoose.createChoose.createItemBottom());
        return itemChooseContent;
    },
    createItemChooseSearch:function(){
        var itemChooseSearch=$('<div id="itemChooseSearch"></div>');
        itemChooseSearch.append($('<span id="titleSearchSpan">&nbsp;宝贝标题:</span>'));
        itemChooseSearch.append($('<input type="text" id="ItemSearch" />'));
        //itemChooseSearch.append($('<img src="/public/images/btns/search1.png" class="ItemSearchBtn">'));
        itemChooseSearch.append($('<a href="javascript:void(0);" class="ItemSearchBtn tmbtn sky-blue-btn" ><span><span>立即搜索</span></span></a>'));
        itemChooseSearch.append($('<a href="javascript:void(0);" class="reflesh tmbtn sky-blue-btn" ><span><span>刷新</span></span></a>'));
        itemChooseSearch.append($('<img src="/public/images/close.png" id="itemChooseDivClose">'));
        return itemChooseSearch;
    },
    createAddOrDetele:function(){
        var addOrDetele=$('<div id="addOrDetele"></div>');
        addOrDetele.append($('<a href="javascript:void(0);" id="selectPageAll"><span class="tmbtn long-sky-blue-btn" style="margin-left: 2px;margin-top: 5px;"><span>全选本页宝贝</span></span></a>'));
        addOrDetele.append($('<a href="javascript:void(0);" id="unSelectPageAll"><span class="tmbtn long-sky-blue-btn" style="margin-top: 5px;"><span>取消本页宝贝</span></span></a>'));
        addOrDetele.append($('<a href="javascript:void(0);" id="unSelectAll"><span class="tmbtn long-sky-blue-btn" style="margin-top: 5px;"><span>取消所有宝贝</span></span></a>'));
        addOrDetele.append($('<p  id="PaginationItem" class="paginationItem" style="margin-top: -13px;padding-top: 0;"></p>'));
        return addOrDetele;
    },
    createCommArea:function(){
        var commArea=$('<div id="commArea"></div>');
        CommChoose.fillComms.getCommsList();
        return commArea;
    },
    createItemBottom:function(){
        var itemBottom=$('<div id="itemBottom"></div>');
        itemBottom.append($('&nbsp;&nbsp; <span id="selectOK" class="tmbtn sky-blue-btn">选择完成</span>'));
        itemBottom.append($(' &nbsp;&nbsp;<span id="selected">已选中宝贝数:</span><span id="selectNum">0</span>'));
        return itemBottom;
    }
},CommChoose.createChoose);

CommChoose.fillComms = CommChoose.fillComms || {};
CommChoose.fillComms= $.extend({
    getCommsList:function(){
        var data = {};
        var ruleJson =CommChoose.rule.getRuleJson();
        data.s=ruleJson.ItemSearch;
        data.pn=ruleJson.pn;
        data.ps=ruleJson.ps;
        data.excludeNumIids = CommChoose.rule.excludeNumIids;
        $.ajax({
            url : CommChoose.rule.url,
            data : data,
            type : 'post',
            success : function(data) {
                var totalCount = data.totalPnCount*ruleJson.ps;
                var per_page = ruleJson.ps;
                CommChoose.fillComms.initPagination(totalCount, per_page, 1);

            }
        });
    },
    initPagination:function(totalCount, per_page, currentPage){
        currentPage--;
        $("#PaginationItem").pagination(totalCount, {
            num_display_entries : 3, // 主体页数
            num_edge_entries : 2, // 边缘页数
            current_page: currentPage,
            callback : CommChoose.fillComms.findComms,
            items_per_page : per_page,// 每页显示多少项
            prev_text : "&lt上一页",
            next_text : "下一页&gt"
        });
    },
    findComms:function(currentPage, jq){
        var ruleJson = CommChoose.rule.getRuleJson();
        ruleJson.pn = currentPage+1;
        var data = {};
        data.s=ruleJson.ItemSearch;
        data.pn=ruleJson.pn;
        data.ps=ruleJson.ps;
        data.excludeNumIids = CommChoose.rule.excludeNumIids;
        $.ajax({
            //async : false,
            url : CommChoose.rule.url,
            data : data,
            type : 'post',
            success : function(data) {
                CommChoose.fillComms.fill(data.res);
                CommChoose.Event.setEvent();
            }
        });
    },
    fill:function(comms){

        var commArea=$("#commArea");
        commArea.empty();
        var commInf;
        for(var i=0;i<comms.length;i++){
            commInf=CommChoose.fillComms.createCommInf(comms[i]);
            commArea.append(commInf);
        }
    },
    createCommInf:function(comm){
        var commInf=$('<div class="commInf"></div>');
        var oneComm=CommChoose.fillComms.createOneComm(comm);
        commInf.append(oneComm);
        return commInf;
    },
    createOneComm:function(comm){
        var oneComm=$('<div class="oneComm"></div>');
        oneComm.attr("numIid",comm.id);
        var commMask=CommChoose.fillComms.createCommMask(comm);
        var commLink=CommChoose.fillComms.createCommImg(comm);
        var commTitle=CommChoose.fillComms.createCommTitle(comm);
        var commPrice=CommChoose.fillComms.createCommPrice(comm);
        oneComm.append(commMask);
        oneComm.append(commLink);
        oneComm.append(commTitle);
        oneComm.append(commPrice);
        if(CommChoose.rule.numIidList.search(comm.id)>=0){
            oneComm.css("border-color","blue");
            oneComm.find(".commMask").css("background-color","blue");
        }
        return oneComm;
    },
    createCommMask:function(comm){
        var commMask=$('<div class="commMask"></div>');
        if(CommChoose.rule.numIidList.search(comm.id)<0)
            commMask.append("点击图片添加");
        else commMask.append("已选中");
        return commMask;
    },
    createCommImg:function(comm){
        var imgObj=$('<img class="commImg" style="cursor: pointer;" src="" />');
        imgObj.attr("src",comm.picURL);
        return imgObj;
    },
    createCommTitle:function(comm){
        var linkDiv=$('<div class="linkDiv"></div>');
        var aObj=$('<a class="commTitle" href="#" target="_blank"></a>');
        var url="http://item.taobao.com/item.htm?id=" + comm.id;
        aObj.attr("href",url);
        aObj.html(comm.name.substr(0,22)+"...");
        linkDiv.append(aObj);
        return linkDiv;
    },
    createCommPrice:function(comm){
        var price=$(' <span class="commPriceSpan"></span>');
        price.append(comm.price+"元");
        return price;
    }
},CommChoose.fillComms);

CommChoose.Event = CommChoose.Event ||{};
CommChoose.Event= $.extend({
    setEvent:function(){
        CommChoose.Event.setCommImgEvent();
        CommChoose.Event.setItemChooseDivCloseEvent();
        CommChoose.Event.setItemSearchBtnEvent();
        CommChoose.Event.setSelectPageAllEvent();
        CommChoose.Event.setRefleshEvent();
        CommChoose.Event.setUnSelectPageAllEvent();
        CommChoose.Event.setUnSelectAllEvent();
        CommChoose.Event.setSelectOKEvent();
    },
    setItemsChooseSpanEvent:function(){
        $("#itemsChooseSpan").click(function(){
            CommChoose.createChoose.createOrRefleshCommsDiv();
        /*    $("#itemChooseDiv").css("left",($(window).width()-850)/2+"px");
            // $("#itemChooseDiv").css("left",($(window).width()-800)/2+"px");
            $("#itemChooseDiv").css("display","block");
            CommChoose.fillComms.getCommsList(); */
        });
    },

    setCommImgEvent:function(){
        $(".commImg").click(function(){
            var commMask = $(this).parent().find(".commMask");
            if(commMask.html()=="点击图片添加"){
                commMask.html("已选中");
                commMask.css("background-color","blue");
                commMask.parent().css("border-color","blue");
                CommChoose.rule.numIidList = CommChoose.rule.numIidList+commMask.parent().attr("numIid")+",";
            }
            else if(commMask.html()=="已选中"){
                commMask.html("点击图片添加");
                commMask.css("background-color","grey");
                commMask.parent().css("border-color","grey");
                CommChoose.rule.numIidList=CommChoose.rule.numIidList.replace((commMask.parent().attr("numIid")+","),"");
            }
            $("#selectNum").html(CommChoose.rule.numIidList.split(",").length-1);
        });
    },
    setItemChooseDivCloseEvent:function(){
        $('#itemChooseDivClose').click(function(){
            $("#itemChooseDiv").css("display","none");
        });
    },
    setItemSearchBtnEvent:function(){
        $(".ItemSearchBtn").click(function(){
            CommChoose.fillComms.getCommsList();
        });
    },
    setRefleshEvent:function(){
        $(".reflesh").click(function(){
            CommChoose.fillComms.getCommsList();
        });
    },
    setSelectPageAllEvent:function(){
        $("#selectPageAll").click(function(){
            $(".commMask").html("已选中");
            $(".commMask").css("background-color","blue");
            $(".oneComm").css("border-color","blue");
            $(".oneComm").each(function(){
                if(CommChoose.rule.numIidList.search($(this).attr("numIid"))<0)
                    CommChoose.rule.numIidList = CommChoose.rule.numIidList+$(this).attr("numIid")+",";
            });
            $("#selectNum").html(CommChoose.rule.numIidList.split(",").length-1);
        });
    },
    setUnSelectPageAllEvent:function(){
        $("#unSelectPageAll").click(function(){
            $(".commMask").html("点击图片添加");
            $(".commMask").css("background-color","grey");
            $(".oneComm").css("border-color","grey");
            $(".oneComm").each(function(){
                if(CommChoose.rule.numIidList.search($(this).attr("numIid"))>=0)
                    CommChoose.rule.numIidList = CommChoose.rule.numIidList.replace(($(this).attr("numIid")+","),"");
            });
            $("#selectNum").html(CommChoose.rule.numIidList.split(",").length-1);
        });
    },
    setUnSelectAllEvent:function(){
        $("#unSelectAll").click(function(){
            $(".commMask").html("点击图片添加");
            $(".commMask").css("background-color","grey");
            $(".oneComm").css("border-color","grey");
            $(".oneComm").each(function(){
                CommChoose.rule.numIidList = "";
            });
            $("#selectNum").html(0);
        });
    },
    setSelectOKEvent:function(){
        $("#selectOK").unbind('click').click(function(){

            if($('#selectNum').html()=="0"){
                alert("亲，您还没有选择任何宝贝哦");
            }
            $("#itemChooseDiv").css("display","none");
            CommChoose.rule.callback && CommChoose.rule.callback();
        });
    }

},CommChoose.Event);

CommChoose.Init=CommChoose.Init||{};
CommChoose.Init= $.extend({
    init:function(callback, url, excludeNumIids){
        CommChoose.rule.numIidList="";
        if(url === undefined || url == null){
            CommChoose.rule.url = "/items/list"
        } else {
            CommChoose.rule.url=url;
        }
        if(excludeNumIids === undefined || excludeNumIids == null){
            CommChoose.rule.excludeNumIids = ""
        } else {
            CommChoose.rule.excludeNumIids=excludeNumIids;
        }
        $('#selectNum').text(0);
        CommChoose.rule.callback = callback;
        //CommChoose.Event.setItemsChooseSpanEvent();
    }
},CommChoose.Init);

CommChoose.rule = CommChoose.rule || {};

CommChoose.rule = $.extend({
    numIidList:"",
    getRuleJson: function() {
        var ruleJson = {
            pn: 1,
            ps: 10//每页条数
        };


        var ItemSearch = $("#ItemSearch").val();
        if (ItemSearch != null && ItemSearch != "")
            ruleJson.ItemSearch = ItemSearch;


        return ruleJson;
    }
}, CommChoose.rule);

/*$(document).ready(function(){
    CommChoose.Init.init();
});*/

