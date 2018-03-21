
var TM = TM || {};

((function ($, window) {

    TM.RestartPromotion = TM.RestartPromotion || {};

    var RestartPromotion = TM.RestartPromotion || {};

    RestartPromotion.init = RestartPromotion.init || {};
    RestartPromotion.init = $.extend({

        doInit: function(container) {

            RestartPromotion.container = container;

            var isAddItem = true;
            var isRestartActivity = true;

            TM.PromotionCommon.init.doInit(isAddItem, isRestartActivity);

            var paramData = {};
            paramData.tmActivityId = TM.UmpUtil.util.getTMActivityId(container);

            $.ajax({
                url : '/umppromotion/findDazheActivity',
                data : paramData,
                type : 'post',
                success : function(dataJson) {


                    if (TM.UmpUtil.util.judgeAjaxResult(dataJson) == false) {

                        location.href = "/taodiscount/index";

                        return;
                    }


                    var activityJson = TM.UmpUtil.util.getAjaxResultJson(dataJson);

                    if (activityJson.nowActive == true) {
                        location.href = "/taodiscount/index";
                        return;
                    }

                    container.find(".add-item-header").html("重启促销商品 &nbsp;(当前活动：" + activityJson.activityDescription + ")");

                    RestartPromotion.init.doOtherInit();

                }
            });


        },
        doOtherInit: function() {

            var container = RestartPromotion.init.getContainer();

            var paramData = {};
            paramData.tmActivityId = TM.UmpUtil.util.getTMActivityId(container);

            $.ajax({
                url : '/umppromotion/queryPromotionRestartNumIids',
                data : paramData,
                type : 'post',
                success : function(dataJson) {


                    if (TM.UmpUtil.util.judgeAjaxResult(dataJson) == false) {

                        location.href = "/taodiscount/index";

                        return;
                    }

                    var noActiveNumIidArray = TM.UmpUtil.util.getAjaxResultJson(dataJson);

                    if (noActiveNumIidArray === undefined || noActiveNumIidArray == null || noActiveNumIidArray.length <= 0) {
                        noActiveNumIidArray = [];
                    }

                    TM.UmpSelectItem.result.selectNumIidArray = noActiveNumIidArray;


                    //选宝贝部分的初始化，包括事件
                    TM.PromotionSelectItem.init.doInit(container.find(".select-item-container"), {});

                    //设置折扣部分的初始化，包括事件
                    TM.SetDiscount.init.doInit(container.find(".set-discount-container"));

                    //上一步，下一步按钮事件
                    TM.AddPromotionCommon.event.initNextStepEvent(container);
                    TM.AddPromotionCommon.event.initPrevStepEvent(container);


                }
            });





        },
        getContainer: function() {
            return RestartPromotion.container;
        }

    }, RestartPromotion.init);



})(jQuery,window));






((function ($, window) {

    TM.SetDiscount = TM.SetDiscount || {};

    var SetDiscount = TM.SetDiscount;

    SetDiscount.init = SetDiscount.init || {};
    SetDiscount.init = $.extend({

        doInit: function(container) {

            SetDiscount.container = container;

            var isRestartActivity = true;
            TM.AddPromotionCommon.init.doInit(container, isRestartActivity);

            var isAddItem = true;

            TM.PromotionCommon.event.initBatchDiscountEvent(container, isAddItem);

            TM.AddPromotionCommon.event.initSearchEvent();


            SetDiscount.event.initSubmitEvent();

        },
        getContainer: function() {
            return SetDiscount.container;
        }

    }, SetDiscount.init);



    SetDiscount.params = SetDiscount.params || {};
    SetDiscount.params = $.extend({

        getSubmitParams: function(errorSetCallback) {

            var container = SetDiscount.init.getContainer();

            var isAddItem = true;

            //加入本页选中的宝贝
            TM.PromotionCommon.result.addPageModifyPromotions(container, isAddItem);


            var modifyPromotionArrayBak = TM.PromotionCommon.result.getAllModifyPromotionArray();

            var modifyPromotionArray = [];
            for (var i = 0; i < modifyPromotionArrayBak.length; i++) {
                modifyPromotionArray[modifyPromotionArray.length] = modifyPromotionArrayBak[i];
            }

            if (modifyPromotionArray.length <= 0) {
                alert("亲，您尚未设置任何宝贝的折扣，没有需要提交的宝贝！！");

                return null;
            }

            var errorNumIidArray = TM.PromotionCommon.result.getErrorModifyNumIids(isAddItem);

            if (errorNumIidArray.length > 0) {
                alert("亲，您有" + errorNumIidArray.length + "个宝贝折扣设置错误，请修改后再提交！");
                //errorSetCallback();

                return null;
            }

            //然后再获取选中的宝贝
            var selectNumIidArrayBak = TM.UmpSelectItem.result.getSelectNumIidArray();

            var selectNumIidArray = [];
            for (var i = 0; i < selectNumIidArrayBak.length; i++) {
                selectNumIidArray[selectNumIidArray.length] = selectNumIidArrayBak[i];
            }

            //其中有些折扣为0的宝贝要去掉，已加入活动的宝贝要去掉
            var notNeedNumIidArray = SetDiscount.params.findNotNeedSubmitNumIids(modifyPromotionArray);

            for (var i = 0; i < notNeedNumIidArray.length; i++) {
                var notNeedNumIid = notNeedNumIidArray[i];

                for (var j = 0; j < modifyPromotionArray.length; j++) {
                    var modifyPromotion = modifyPromotionArray[j];
                    if (modifyPromotion.numIid == notNeedNumIid) {
                        modifyPromotionArray.splice(j, 1);
                        break;
                    }
                }
                for (var j = 0; j < selectNumIidArray.length; j++) {
                    var selectNumIid = selectNumIidArray[j];
                    if (selectNumIid == notNeedNumIid) {
                        selectNumIidArray.splice(j, 1);
                        break;
                    }
                }
            }

            if (selectNumIidArray.length <= 0) {
                alert("亲，您尚未设置宝贝的折扣，没有需要提交的宝贝！！");

                return null;
            }


            var jsonArray = [];

            $(modifyPromotionArray).each(function(index, promotionJson) {

                var json = '{';
                json += '"numIid":' + promotionJson.numIid + ',';
                json += '"promotionType":"' + promotionJson.promotionType + '",';
                json += '"decreaseAmount":' + promotionJson.decreaseAmount + ',';
                json += '"discountRate":' + promotionJson.discountRate;
                json += '}';

                jsonArray[jsonArray.length] = json;

            });


            var paramData = {};

            paramData.minDiscount = TM.UmpUtil.util.getMinDiscount(modifyPromotionArray);

            paramData.itemNum = selectNumIidArray.length;

            paramData.paramsJson = '[' + jsonArray.join(",") + ']';

            paramData.selectNumIids = selectNumIidArray.join(",");

            paramData.tmActivityId = TM.UmpUtil.util.getTMActivityId(container);

            return paramData;

        },
        findNotNeedSubmitNumIids: function(modifyPromotionArray) {

            //其中有些折扣为0的宝贝要去掉，已加入活动的宝贝要去掉

            var notNeedNumIidArray = [];

            for (var i = 0; i < modifyPromotionArray.length; i++) {
                var modifyPromotion = modifyPromotionArray[i];

                if (modifyPromotion.canbeEdit == false) {
                    notNeedNumIidArray[notNeedNumIidArray.length] = modifyPromotion.numIid;
                    continue;
                }
                var discountRate = modifyPromotion.discountRate;

                if (discountRate <= 0 || discountRate >= 1000) {
                    notNeedNumIidArray[notNeedNumIidArray.length] = modifyPromotion.numIid;
                    continue;
                }
            }

            return notNeedNumIidArray;
        }

    }, SetDiscount.params);



    SetDiscount.event = SetDiscount.event || {};
    SetDiscount.event = $.extend({

        initSubmitEvent: function() {

            var container = SetDiscount.init.getContainer();


            container.find(".submit-discount-btn").unbind().click(function() {

                var errorSetCallback = function() {

                    container.find(".search-text-input").val("");

                    container.find(".item-type-select .error-activity-item-option").click();

                }

                var paramData = SetDiscount.params.getSubmitParams(errorSetCallback);

                if (paramData === undefined || paramData == null) {
                    return;
                }
                /*if (confirm("您当前设置了" + paramData.itemNum + "个宝贝的折扣，确定要提交到淘宝？") == false) {
                    return;
                }*/

                var minDiscount = paramData.minDiscount / 100;
                if (confirm("您当前设置了" + paramData.itemNum + "个宝贝的折扣，其中最低折扣为" + minDiscount
                    + "折，确定要提交到淘宝？") == false) {
                    return;
                }

                $.ajax({
                    url : '/umppromotion/submitRestartPrmotions',
                    data : paramData,
                    type : 'post',
                    success : function(dataJson) {

                        if (TM.UmpUtil.util.checkIsW2AuthError(dataJson) == true) {
                            return;
                        }

                        if (TM.UmpUtil.util.judgeAjaxResult(dataJson) == false) {
                            return;
                        }



                        var resultJson = TM.UmpUtil.util.getAjaxResultJson(dataJson);


                        TM.UmpUtil.errors.showErrors(container, resultJson, function() {

                            //如果没有失败
                            window.location.href ="/TaoDiscount/index";
                            return;

                        }, function() {

                            //如果有宝贝失败，要刷新列表，同时要取消全局变量SelectItems.....
                            //或者 保留SelectItems，但判断已经promotion的宝贝，就去掉select标志。。。

                            var successNumIidArray = resultJson.successNumIidSet;

                            var isDeleteOption = false;
                            TM.AddPromotionCommon.reload.doRefreshAfterSubmit(successNumIidArray, isDeleteOption);
                        });
                    }
                });

            });
        }
    }, SetDiscount.event);



})(jQuery,window));


