
var TM = TM || {};

((function ($, window) {

    TM.ModifyShopDiscount = TM.ModifyShopDiscount || {};

    var ModifyShopDiscount = TM.ModifyShopDiscount || {};

    ModifyShopDiscount.init = ModifyShopDiscount.init || {};
    ModifyShopDiscount.init = $.extend({

        doInit: function(container) {
            $.ajax({
                url : "/umpactivity/getServerNowTime",
                data : {},
                type : 'post',
                success : function(dataJson) {


                    if (TM.UmpUtil.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }


                    var nowTimeStr = TM.UmpUtil.util.getAjaxResultJson(dataJson);

                    var nowDate = new Date(nowTimeStr);
                    var nowTime = nowDate.getTime();

                    ModifyShopDiscount.init.doWithNowTime(container, nowTime);

                }
            });



        },
        doWithNowTime: function(container, nowTime) {


            ModifyShopDiscount.container = container;

            var activityId = TM.UmpUtil.util.getTMActivityId(container);


            $.ajax({
                url : "/umpactivity/queryActivity",
                data : {tmActivityId: activityId},
                type : 'post',
                success : function(dataJson) {

                    if (TM.UmpUtil.util.judgeAjaxResult(dataJson) == false) {

                        location.href = "/Sales/index";

                        return;
                    }

                    var activityJson = TM.UmpUtil.util.getAjaxResultJson(dataJson);
                    container.find('.activity-description').val(activityJson.activityDescription);

                    container.find('.start-date').val(activityJson.activityStartTimeString);
                    container.find('.end-date').val(activityJson.activityEndTimeString);

                    container.find('.activity-title-input').val(activityJson.activityTitle);

                    var discountRate = 9.99;
                    var shopDiscountParam = activityJson.shopDiscountParam;
                    if (shopDiscountParam === undefined || shopDiscountParam == null) {

                    } else {
                        discountRate = shopDiscountParam.discountRate / 100;
                    }
                    container.find('.shop-discount-input').val(discountRate);

                    TM.DiscountCommon.init.doInit(container, false, nowTime);

                    container.find('.self-put').click();
                }
            });



            container.find(".submit-modify-activity-btn").click(function() {

                var paramData = TM.DiscountCommon.params.getSubmitParams();
                if (paramData == undefined || paramData == null) {
                    return;
                }
                paramData = TM.DiscountCommon.params.addDazheParam(paramData);
                if (paramData == undefined || paramData == null) {
                    return;
                }

                paramData.tmActivityId = activityId;

                if (confirm("您当前设置的全店折扣是" + (paramData.discountRate / 100) + "折，确定要修改全店打折活动？") == false) {
                    return;
                }

                $.ajax({
                    url : "/umpactivity/updateShopActivity",
                    data : paramData,
                    type : 'post',
                    success : function(dataJson) {

                        if (TM.UmpUtil.util.checkIsW2AuthError(dataJson) == true) {
                            return;
                        }

                        if (TM.UmpUtil.util.judgeAjaxResult(dataJson) == false) {
                            return;
                        }


                        var modifyResJson = TM.UmpUtil.util.getAjaxResultJson(dataJson);


                        TM.UmpUtil.errors.showErrors(container, modifyResJson, function() {

                            //如果没有失败
                            window.location.href ="/Sales/index";
                            return;

                        }, function() {

                            //如果有失败的宝贝，就停留在当页

                        });


                    }
                });
            });


        },
        getContainer: function() {
            return ModifyShopDiscount.container;
        }

    }, ModifyShopDiscount.init);


})(jQuery,window));