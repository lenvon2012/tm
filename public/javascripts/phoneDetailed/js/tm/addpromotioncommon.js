

((function ($, window) {

    TM.AddPromotionCommon = TM.AddPromotionCommon || {};

    var AddPromotionCommon = TM.AddPromotionCommon;

    AddPromotionCommon.init = AddPromotionCommon.init || {};
    AddPromotionCommon.init = $.extend({

        doInit: function(container, isRestartActivity) {

            AddPromotionCommon.container = container;
            AddPromotionCommon.isRestartActivity = isRestartActivity;



        },
        getContainer: function() {
            return AddPromotionCommon.container;
        },
        getIsRestartActivity: function() {
            return AddPromotionCommon.isRestartActivity;
        }

    }, AddPromotionCommon.init);


    /**
     * 选择宝贝后点击设置折扣
     * @type {*}
     */
    AddPromotionCommon.reload = AddPromotionCommon.reload || {};
    AddPromotionCommon.reload = $.extend({
        doReload: function() {

            //这个要在initActivityPromotionCount之前，因为initActivityPromotionCount要显示错误个数
            AddPromotionCommon.reload.syncSelectItems();

            AddPromotionCommon.reload.initActivityPromotionCount();

            /*AddPromotionCommon.show.targetCurrentPage = 1;
            var container = AddPromotionCommon.init.getContainer();
            var tbodyObj = container.find(".promotion-table");

            tbodyObj.html("");*/

            //先清空页面，然后再刷新宝贝
            AddPromotionCommon.show.doClearPageAndRefresh();

        },
        //删除之后，提交之后，都要调用
        doRefreshAfterSubmit: function(successNumIidArray, isDeleteOption) {
            if (successNumIidArray === undefined || successNumIidArray == null || successNumIidArray.length <= 0) {
                successNumIidArray = [];
            }


            //
            TM.PromotionCommon.result.removeSomeItemsFromModifyArray(successNumIidArray);

            //删除选中的宝贝
            TM.UmpSelectItem.result.removeSomeSelectNumIids(successNumIidArray);

            //刷新item-type-select列表


            if (isDeleteOption == false) {
                AddPromotionCommon.reload.initActivityPromotionCount();

            } else {
                var container = AddPromotionCommon.init.getContainer();
                var itemTypeSelectObj = container.find(".item-type-select");
                var existPromotionNum = itemTypeSelectObj.find('.exist-activity-item-option').attr("existPromotionNum");

                existPromotionNum = parseInt(existPromotionNum);
                AddPromotionCommon.reload.refreshItemTypeSelect(existPromotionNum);
            }



            AddPromotionCommon.show.doClearPageAndRefresh();
        },
        //选择宝贝后，有些宝贝可能是之前选择的，也设置了折扣，但现在又不选了，这时要把它从PromotionCommon的selectItem中去掉
        syncSelectItems: function() {
            var selectNumIidArray = TM.UmpSelectItem.result.getSelectNumIidArray();
            TM.PromotionCommon.result.removeNotExistItems(selectNumIidArray);
        },
        initActivityPromotionCount: function() {
            var container = AddPromotionCommon.init.getContainer();
            var paramData = {};
            paramData.tmActivityId = TM.UmpUtil.util.getTMActivityId(container);

            $.ajax({
//                url : 'http://localhost:9999/PhoneItem/countActivityActivePromotions',
                url : TM.serverPath+'/PhoneItem/countActivityActivePromotions'+TM._tms,
                data : paramData,
                type : 'post',
                success : function(dataJson) {


                    if (TM.UmpUtil.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }

                    var existPromotionNum = TM.UmpUtil.util.getAjaxResultJson(dataJson);

                    AddPromotionCommon.reload.refreshItemTypeSelect(existPromotionNum);
                }
            });
        },
        refreshItemTypeSelect: function(existPromotionNum) {
            var container = AddPromotionCommon.init.getContainer();

            var itemTypeSelectObj = container.find(".item-type-select");
            var selectNumIidArray = TM.UmpSelectItem.result.getSelectNumIidArray();
            var newPromotionNum = selectNumIidArray.length;

            var totalPromotionNum = newPromotionNum + existPromotionNum;

            var currentSelectType = itemTypeSelectObj.find(".item-type-text").attr("itemType");

            if (currentSelectType == 1) {
                itemTypeSelectObj.find(".item-type-text").html('活动中所有宝贝(' + totalPromotionNum + '个)');
            } else if (currentSelectType == 2) {
                itemTypeSelectObj.find(".item-type-text").html('活动中原有宝贝(' + existPromotionNum + '个)');
            } else if (currentSelectType == 4) {
                itemTypeSelectObj.find(".item-type-text").html('活动中新增宝贝(' + newPromotionNum + '个)');
            } else if (currentSelectType == 8) {
                itemTypeSelectObj.find(".item-type-text").html('参数错误的宝贝');
            }

            var existOptionObj = itemTypeSelectObj.find('.exist-activity-item-option');

            existOptionObj.attr("existPromotionNum", existPromotionNum);
            existOptionObj.html('活动中原有宝贝(' + existPromotionNum + '个)');
            itemTypeSelectObj.find('.new-activity-item-option').html('活动中新增宝贝(' + newPromotionNum + '个)');
            itemTypeSelectObj.find('.all-activity-item-option').html('活动中所有宝贝(' + totalPromotionNum + '个)');
            itemTypeSelectObj.find('.error-activity-item-option').html('参数错误的宝贝');

        }
    }, AddPromotionCommon.reload);



    AddPromotionCommon.show = AddPromotionCommon.show || {};
    AddPromotionCommon.show = $.extend({
        targetCurrentPage: 1,
        doShow: function() {
            var container = AddPromotionCommon.init.getContainer();
            var isAddItem = true;
            TM.PromotionCommon.result.addPageModifyPromotions(container, isAddItem);
            AddPromotionCommon.show.doSearchWithPage(1, false);
        },
        doClearPageAndRefresh: function() {
            var container = AddPromotionCommon.init.getContainer();
            //var tbodyObj = container.find(".promotion-table");

            //tbodyObj.html("");

            AddPromotionCommon.show.doSearchWithPage(AddPromotionCommon.show.targetCurrentPage, false);
        },
        doSearchWithPage: function(currentPage, isAddPageModifyPromotions) {

            if (currentPage < 1) {
                currentPage = 1;
            }

            AddPromotionCommon.show.targetCurrentPage = currentPage;

            var container = AddPromotionCommon.init.getContainer();
            var tbodyObj = container.find(".promotion-table");


            var paramData = AddPromotionCommon.show.getSearchParams();

            if (paramData === undefined || paramData == null) {
                return;
            }

            if (AddPromotionCommon.init.getIsRestartActivity() == true) {
                paramData.isRestartActivity = true;
            } else {
                paramData.isRestartActivity = false;
            }




            container.find(".promotion-paging-div").tmpage({
                currPage: currentPage,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: paramData,
                    dataType: 'json',
//                    url: 'http://localhost:9999/PhoneItem/queryPromotionSelectedItems',
                    url: TM.serverPath+'/PhoneItem/queryPromotionSelectedItems'+TM._tms,
                    callback: function(dataJson){

                        var isAddItem = true;

                        if (isAddPageModifyPromotions == true) {
                            TM.PromotionCommon.result.addPageModifyPromotions(container, isAddItem);
                        } else {
                            //只是第一次不获取页面数据，下一次点击下一页的时候，还是要获取的
                            isAddPageModifyPromotions = true;
                        }


                        AddPromotionCommon.show.targetCurrentPage = dataJson.pn;

                        //要先addPageModifyPromotions，再清空tbodyObj
                        tbodyObj.html("");

                        var itemJsonArray = dataJson.res;


                        //还有一种情况就是res为空的时候，要提示用户没有数据
                        if(itemJsonArray===null||itemJsonArray.length===0)
                        var nullHtml="<div class='null-html'>暂时没有符合要求的数据</div>";
                        $(nullHtml).appendTo(tbodyObj);

                        $(itemJsonArray).each(function(index, itemJson) {
                            var trObj = TM.PromotionCommon.row.createCommonRow(index, itemJson, isAddItem);

                            AddPromotionCommon.event.initDeleteEvent(trObj);

                            tbodyObj.append(trObj);
                        });

                    }
                }

            });

        },
        getSearchParams: function() {
            var container = AddPromotionCommon.init.getContainer();



            var paramData = {};

            paramData.tmActivityId = TM.UmpUtil.util.getTMActivityId(container);
            paramData.title = container.find('.search-text-input').val();
            paramData.itemType = container.find(".item-type-text").attr("itemType");

            if (paramData.itemType == 1 || paramData.itemType == 4) {

                var selectNumIidArray = TM.UmpSelectItem.result.getSelectNumIidArray();

                paramData.selectNumIids = selectNumIidArray.join(',');
            } else if (paramData.itemType == 8) {

                var errorNumIidArray = TM.PromotionCommon.result.getErrorModifyNumIids(true);


                paramData.selectNumIids = errorNumIidArray.join(',');
            }



            return paramData;
        }
    }, AddPromotionCommon.show);

    //进度 已用时 预计还需要
    //总数量 待生成 成功 失败  进度
//    count=0,wait=0,successNum=0,failureNum=0,proportion=0;
    //status    0 未开始     1正在生成       2完成
//    status=0;
    AddPromotionCommon.event = AddPromotionCommon.event || {};
    AddPromotionCommon.event = $.extend({

        initNextStepEvent: function(outContainer) {

            var container = outContainer;




            container.find(".item-all").click(function(){

//                status=2;
//                TM.AddPromotionCommon.event.report();
//                return;

                if(!confirm("确定要一键全店生成手机详情页吗？")){
                    return;
                }
                container.find(".create-info").slideDown(1000,function(){
                    //生成手机详情页
                    TM.AddPromotionCommon.event.report();
                    setTimeout(function(){
                        var data=TM.AddPromotionCommon.event.createAllPhones();
                        console.log(data);
                        TM.AddPromotionCommon.event.setInfoBoard(data);

//                        TM.AddPromotionCommon.event.isInitApp();
                    },2000)
                });

            });
        },
        //生成手机详情页
        createPhones:function(){
            var selectNumIidStr = TM.UmpSelectItem.result.getSelectNumIidStr();
            var selectNumIidArray = TM.UmpSelectItem.result.getSelectNumIidArray();

            if (selectNumIidStr === undefined || selectNumIidStr == null || selectNumIidStr.length <= 0) {
                alert("请先选择要添加的宝贝！");
                return;
            }

            if(!confirm("当前选中了"+selectNumIidArray.length+"  个宝贝，确定生成手机详情页吗？")){
                return;
            }

            //点击之后直接将 ID list发送到后台进行生成
            console.log("Id list :");
            console.log(selectNumIidStr);

            //生成手机详情页
            var data = TM.AddPromotionCommon.event.createTmpls(selectNumIidStr);
            return data;
        },
        //安装手机详情页的教程
        initHelp:function(){
            //安装手机详情页教程
            var container = TM.AddPromotion.init.getContainer();

            container.find(".now-installation").click(function(){
             var content=container.find("#help-html").html();
//                TM.Alert.loadDetail(content,800,550,function(){
//                    return true;
//                },"测试一下");
                $('#myModal').modal({
                    "keyboard":true,
                    "show":true
                });
            });
        },
        createTmpls:function(selectNumIidStr){
            var jsonData;

            //isFilter
            var isFilter=$("#isFilter").attr("checked");
            if(isFilter==undefined){
                var filter=false;
            }else{
                var filter=true;
            }

            $.ajax({
//                "url":"http://localhost:9999/PhoneDetaileds/createData",
                "url":TM.serverPath+"/PhoneDetaileds/createData"+TM._tms,
                "type":"post",
                "data":{
                    "commodityIds":selectNumIidStr,
                    "isFilter":filter
                },
                "dataType":"json",
                "async":false,
                "success":function(data){
                    jsonData= data;
                },
                "error":function(){
                     alert("生成手机详情页模板出错，请联系我们.");
                }

            });

            return jsonData;
        },
        createAllPhones:function(){
            var jsonData;

            var isFilter=$("#isFilter").attr("checked");
            if(isFilter==undefined){
                var filter=false;
            }else{
                var filter=true;
            }


            $.ajax({
//                "url":"http://localhost:9999/PhoneDetaileds/createAllPhones",
                "url":TM.serverPath+"/PhoneDetaileds/createAllPhones"+TM._tms,
                "type":"post",
                data:{
                    "isFilter":filter
                },
                "dataType":"json",
                "async":false,
                "success":function(data){
                    jsonData= data;
                },
                "error":function(){
                    alert("一键全店生成手机详情页出错，请联系我们.");
                }

            });

            return jsonData;
        },
        //设置信息板
        setInfoBoard:function(data){
            //总数量
            count=data.count;
            if(data===undefined||data==null||proportion===100){
                status=2;
                TM.AddPromotionCommon.event.report();
                return;
            }else {

                successNum += data.successNum;
                failureNum += data.failureNum;
                wait += count - successNum - failureNum < 0 ? 0 : count - successNum - failureNum;
                //进度     数量÷总数×100=百分比
                proportion = (count - wait) / count * 100;

                if (proportion === 100) {
                    status = 2;
                    TM.AddPromotionCommon.event.report();
                    return;
                }

                TM.AddPromotionCommon.event.report();
            }
        },
        //开始安装手机详情页
        start:function(){

        },
        //判断是否安装插件
        isInitApp:function(){
            var mimetype = navigator.mimeTypes;
            //这里面的key就是你的插件的type
//            var mimetype = navigator.mimeTypes["text/uri-list"];//这里面的key就是你的插件的type

            console.log("mimetype is ---:");
            console.log(mimetype);
            $(mimetype).each(function(i,e){
                console.log(i+ '  ');
                console.log(e.type);
                console.log(e.description);
            });
            if(mimetype)
            {
                var plugin = mimetype.enabledPlugin;

                if(plugin)
                {
                    console.log("已经安装");
                }
            }
            else
            {
                console.log("还未安装");
            }
        },
        hasPlugin: function (name) {
            name = name.toLowerCase();
            for (var i = 0; i < navigator.plugins.length; i++) {
                console.log(navigator.plugins[i].name);
                if (navigator.plugins[i].name.toLowerCase().indexOf(name) > -1) {
                    return true;
                }
            }
            return false;
        },
        handleTmpl:function(pn,ps){

            //getTmpls
            var data= TM.AddPromotionCommon.event.getTmpls(pn,ps);
            //总数量
            count=data.count;
            if(data===undefined||data==null||data.res.length===0||proportion===100){
                  status=2;
                  TM.AddPromotionCommon.event.report();
                  return;
            }else{



                var ids=TM.AddPromotionCommon.event.resFilterIds(data.res);
                var createData=TM.AddPromotionCommon.event.createTmpls(ids);
                console.log("createData is:");
                console.log(createData);

//              count=0,wait=0,successNum=0,failureNum=0;
                successNum+=createData.successNum;
                failureNum+=createData.failureNum;
                wait+=count-successNum-failureNum<0?0:count-successNum-failureNum;
                //进度     数量÷总数×100=百分比
                proportion=(count-wait)/count*100

                if(proportion===100){
                    status=2;
                    TM.AddPromotionCommon.event.report();
                    return;
                }

                TM.AddPromotionCommon.event.report();

                setTimeout(function(){
                    TM.AddPromotionCommon.event.handleTmpl(++pn,ps);
                },8000);

//                TM.AddPromotionCommon.event.handleTmpl(++pn,ps);
            }


        },
        resFilterIds:function(res){
            var ids="";
            $(res).each(function(i,e){
               ids+=e.numIid+",";
            });

            //去掉最后的一个 逗号
            ids = ids.substring(0, ids.length - 1);
            return ids;
        },
        //重载模板生成信息牌上的信息
        report:function(){
            //                count=0,wait=0,successNum=0,failureNum=0;

            var container = TM.AddPromotion.init.getContainer();

            $(".count-num").html(count);
            container.find(".wait-num").html(wait);
            container.find(".successNum-num").html(successNum);
            container.find(".failureNum-num").html(failureNum);
            container.find(".proportion").html(proportion+" %");

            //标题栏的状态
            if(status==2){
                $(".load-img").css("background","url('http://img01.taobaocdn.com/imgextra/i1/763789825/TB2SqWobVXXXXXIXXXXXXXXXXXX_!!763789825.png')");
                $(".load-txt").html("亲，恭喜你，手机详情页生成完成啦。本次成功生成 "+successNum+"个。&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href='#' class='cBlue now-installation'>立即安装</a>").removeClass("cRed").addClass("cfont");

                TM.AddPromotionCommon.event.initHelp();

            }else if(status==1){
                $(".load-img").css("background","url('http://img01.taobaocdn.com/imgextra/i1/758262754/TB2S1FVbVXXXXbeXpXXXXXXXXXX_!!758262754.gif')");
                $(".load-txt").html("正在生成手机详情页，你可以泡杯咖啡，休息一下。").removeClass("cGreen").addClass("cRed");
            }

        },
        getTmpls:function(pn,ps){
            var jsonData;

            $.ajax({
//                "url":"http://localhost:9999/PhoneDetaileds/getItems",
                "url":TM.serverPath+"/PhoneDetaileds/getItems"+TM._tms,
                "type":"post",
                "data":{
                    "pn":pn,
                    "ps":ps
                },
                "dataType":"json",
                "async":false,
                "success":function(data){
                    jsonData= data;
                },
                "error":function(){
                    alert("生成手机详情页模板出错，请联系我们.");
                }

            });

            return jsonData;
        },
        initPrevStepEvent: function(outContainer) {

            var container = outContainer;

            container.find(".goto-choose-item-btn").click(function() {

                var isAddItem = true;

                TM.PromotionCommon.result.addPageModifyPromotions(container, isAddItem);


                container.find(".first-step-header").addClass("bold-header");

                container.find(".second-step-header").removeClass("bold-header");

                container.find(".set-discount-container").hide();

                container.find(".select-item-container").show();

                //重新加载数据
                TM.PromotionSelectItem.reload.doReload();


            });

        },
        initSearchEvent: function() {

            var searchCallback = function() {
                AddPromotionCommon.show.doShow();
            }


            var container = AddPromotionCommon.init.getContainer();

            container.find('.search-text-input').keyup(function(event) {
                var lable_key = container.find('.search-text-input').val();
                if (!lable_key) {
                    container.find('.combobox-label-item').show();
                } else {
                    container.find('.combobox-label-item').hide();
                }
            });

            container.find('.search-text-input').keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    searchCallback();
                }
            });

            container.find('.search-btn').unbind().click(function(){
                searchCallback();
            });

            container.find('.item-type-select').mousemove(function(){
                container.find('.item-type-list').show();
            });
            container.find('.item-type-select').mouseout(function(){
                container.find('.item-type-list').hide();
            });
            container.find('.item-type-option').click(function(){
                container.find('.item-type-text').html($(this).html());
                container.find('.item-type-text').attr("itemType", $(this).attr("itemType"));
                searchCallback();
            });



        },
        initDeleteEvent: function(trObj) {

            trObj.find(".delete-promotion").unbind().click(function() {

                var numIid = trObj.attr("numIid");
                if (confirm("确定要从促销活动中移除该宝贝？") == false) {
                    return;
                }

                var isAddItem = true;
                var container = AddPromotionCommon.init.getContainer();

                TM.PromotionCommon.result.addPageModifyPromotions(container, isAddItem);

                var successNumIidArray = [numIid];

                var isDeleteOption = true;
                AddPromotionCommon.reload.doRefreshAfterSubmit(successNumIidArray, isDeleteOption);


            });




        }
    }, AddPromotionCommon.event);



})(jQuery,window));
