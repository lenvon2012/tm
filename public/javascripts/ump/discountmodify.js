
var TM = TM || {};

((function ($, window) {

    TM.ModifyDiscount = TM.ModifyDiscount || {};

    var ModifyDiscount = TM.ModifyDiscount || {};

    ModifyDiscount.init = ModifyDiscount.init || {};
    ModifyDiscount.init = $.extend({

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

                    ModifyDiscount.init.doWithNowTime(container, nowTime);

                }
            });



        },
        doWithNowTime: function(container, nowTime) {


            ModifyDiscount.container = container;

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
                    container.find('.activity-description').val(activityJson.activityDescription);

                    container.find('.start-date').val(activityJson.activityStartTimeString);
                    container.find('.end-date').val(activityJson.activityEndTimeString);

                    container.find('.activity-title-input').val(activityJson.activityTitle);

                    TM.DiscountCommon.init.doInit(container, false, nowTime);

                    container.find('.self-put').click();
                }
            });



            container.find(".submit-modify-activity-btn").click(function() {

                var paramData = TM.DiscountCommon.params.getSubmitParams();
                if (paramData == null) {
                    return;
                }



                paramData.tmActivityId = activityId;

                if (confirm("确定要修改限时打折活动？") == false) {
                    return;
                }

                $.ajax({
                    url : "/umpactivity/updateDazheActivity",
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


                        //location.reload();
                    }
                });

            });


        },
        getContainer: function() {
            return ModifyDiscount.container;
        }

    }, ModifyDiscount.init);


})(jQuery,window));