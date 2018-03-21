
var TM = TM || {};

((function ($, window) {

    TM.RestartDiscount = TM.RestartDiscount || {};

    var RestartDiscount = TM.RestartDiscount || {};

    RestartDiscount.init = RestartDiscount.init || {};
    RestartDiscount.init = $.extend({

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

                    RestartDiscount.init.doWithNowTime(container, nowTime);

                }
            });



        },
        doWithNowTime: function(container, nowTime) {


            RestartDiscount.container = container;

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

                    if (activityJson.nowActive == true) {
                        location.href = "/Sales/index";

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

                    TM.DiscountCommon.init.doInit(container, true, nowTime);
                }
            });



            container.find(".submit-restart-activity-btn").click(function() {

                var paramData = TM.DiscountCommon.params.getSubmitParams();
                if (paramData == null) {
                    return;
                }


                paramData.tmActivityId = activityId;


                $.ajax({
                    url : "/umpactivity/restartDazheActivity",
                    data : paramData,
                    type : 'post',
                    success : function(dataJson) {
                        /*

                         if (TM.UmpUtil.util.checkIsW2AuthError(dataJson) == true) {
                         return;
                         }
                         */

                        if (TM.UmpUtil.util.judgeAjaxResult(dataJson) == false) {
                            return;
                        }
                        alert("下一步请选择要重启的宝贝！");

                        window.location.href ="/Sales/restartPromotion?activityId=" + activityId;
                    }
                });

            });


        },
        getContainer: function() {
            return RestartDiscount.container;
        }

    }, RestartDiscount.init);


})(jQuery,window));