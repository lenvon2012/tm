
var TM = TM || {};

((function ($, window) {

    TM.RestartShopDiscount = TM.RestartShopDiscount || {};

    var RestartShopDiscount = TM.RestartShopDiscount || {};

    RestartShopDiscount.init = RestartShopDiscount.init || {};
    RestartShopDiscount.init = $.extend({

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

                    RestartShopDiscount.init.doWithNowTime(container, nowTime);

                }
            });



        },
        doWithNowTime: function(container, nowTime) {


            RestartShopDiscount.container = container;

            var activityId = TM.UmpUtil.util.getTMActivityId(container);


            $.ajax({
                url : "/umpactivity/queryActivity",
                data : {tmActivityId: activityId},
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

                    container.find('.activity-description').val(activityJson.activityDescription);

                    var startTimeObj = container.find('.start-date');
                    var endTimeObj = container.find('.end-date');


                    startTimeObj.val(TM.DiscountCommon.util.parseLongToDate(nowTime));
                    var monthMillis = 30 * 24 * 60 * 60 * 1000;
                    var endTime = nowTime + monthMillis;
                    endTimeObj.val(TM.DiscountCommon.util.parseLongToDate(endTime));


                    TM.DiscountCommon.params.originEndTime = endTime;

                    var discountRate = 9.99;
                    var shopDiscountParam = activityJson.shopDiscountParam;
                    if (shopDiscountParam === undefined || shopDiscountParam == null) {

                    } else {
                        discountRate = shopDiscountParam.discountRate / 100;
                    }
                    container.find('.shop-discount-input').val(discountRate);

                    TM.DiscountCommon.init.doInit(container, true, nowTime);

                }
            });



            container.find(".submit-restart-activity-btn").click(function() {

                var paramData = TM.DiscountCommon.params.getSubmitParams();
                if (paramData == undefined || paramData == null) {
                    return;
                }
                paramData = TM.DiscountCommon.params.addDazheParam(paramData);
                if (paramData == undefined || paramData == null) {
                    return;
                }

                paramData.tmActivityId = activityId;

                if (confirm("您当前设置的全店折扣是" + (paramData.discountRate / 100) + "折，确定要重启全店打折活动？") == false) {
                    return;
                }

                $.ajax({
                    url : "/umpactivity/restartShopActivity",
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
                            window.location.href ="/TaoDiscount/index";
                            return;

                        }, function() {

                            //如果有失败的宝贝，就停留在当页

                        });


                    }
                });
            });


        },
        getContainer: function() {
            return RestartShopDiscount.container;
        }

    }, RestartShopDiscount.init);


})(jQuery,window));