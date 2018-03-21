

var TM = TM || {};

((function ($, window) {

    TM.AddDiscount = TM.AddDiscount || {};

    var AddDiscount = TM.AddDiscount || {};

    AddDiscount.init = AddDiscount.init || {};
    AddDiscount.init = $.extend({

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

                    AddDiscount.init.doWithNowTime(container, nowTime);

                }
            });



        },
        doWithNowTime: function(container, nowTime) {


            AddDiscount.container = container;

            var description = AddDiscount.init.getDefaultDescription(nowTime);

            container.find('.activity-description').val(description);

            var startTimeObj = container.find('.start-date');
            var endTimeObj = container.find('.end-date');

            startTimeObj.val(TM.DiscountCommon.util.parseLongToDate(nowTime));
            var monthMillis = 30 * 24 * 60 * 60 * 1000;
            var endTime = nowTime + monthMillis;
            endTimeObj.val(TM.DiscountCommon.util.parseLongToDate(endTime));


            TM.DiscountCommon.params.originEndTime = endTime;
            TM.DiscountCommon.init.doInit(container, true, nowTime);


            container.find(".submit-add-activity-btn").click(function() {

                var paramData = TM.DiscountCommon.params.getSubmitParams();
                if (paramData == null) {
                    return;
                }
/*

                if (confirm("确定要创建限时打折活动？") == false) {
                    return;
                }
*/

                $.ajax({
                    url : "/umpactivity/createDazheActivity",
                    data : paramData,
                    type : 'post',
                    success : function(dataJson) {

                        if (TM.UmpUtil.util.judgeAjaxResult(dataJson) == false) {
                            return;
                        }

                        var activityId = TM.UmpUtil.util.getAjaxResultJson(dataJson);

                        alert("限时打折活动创建成功，下一步请选择要加入活动的宝贝！");

                        TM.Loading.init.show();

                        location.href = '/umppromotion/addpromotion?activityId=' + activityId;
                    }
                });

            });


        },
        getDefaultDescription: function(nowTime) {
            var theDate = new Date(nowTime);
            var year = theDate.getFullYear();
            var month = theDate.getMonth() + 1;//js从0开始取
            var date = theDate.getDate();
            var hour = theDate.getHours();
            var minutes = theDate.getMinutes();
            var second = theDate.getSeconds();

            var description = month+"月"+date+"日" + "限时打折";

            return description;
        },
        getContainer: function() {
            return AddDiscount.container;
        }

    }, AddDiscount.init);


})(jQuery,window));

