

var TM = TM || {};

((function ($, window) {

    TM.AddDiscount = TM.AddDiscount || {};

    var AddDiscount = TM.AddDiscount || {};

    AddDiscount.init = AddDiscount.init || {};
    AddDiscount.init = $.extend({

        doInit: function(container) {

            AddDiscount.container = container;

            var description = AddDiscount.init.getDefaultDescription();

            container.find('.activity-description').val(description);

            var startTimeObj = container.find('.start-date');
            var endTimeObj = container.find('.end-date');

            var nowTime = new Date();
            startTimeObj.val(TM.DiscountCommon.util.parseLongToDate(nowTime.getTime()));
            var monthMillis = 30 * 24 * 60 * 60 * 1000;
            var endTime = nowTime.getTime() + monthMillis;
            endTimeObj.val(TM.DiscountCommon.util.parseLongToDate(endTime));



            TM.DiscountCommon.params.originEndTime = endTime;
            TM.DiscountCommon.init.doInit(container, true);


            container.find(".submit-add-activity-btn").click(function() {

                var paramData = TM.DiscountCommon.params.getSubmitParams();
                if (paramData == null) {
                    return;
                }


                $.ajax({
                    url : "/umpactivity/createDazheActivity",
                    data : paramData,
                    type : 'post',
                    success : function(dataJson) {

                        if (TM.UmpUtil.util.judgeAjaxResult(dataJson) == false) {
                            return;
                        }

                        var activityId = TM.UmpUtil.util.getAjaxResultJson(dataJson);

                        //alert("促销活动创建成功，下一步请加入宝贝！");

                        TM.Loading.init.show();

                        window.location.href = '/home/tbtDazheItems?activityId=' + activityId;
                        return true;
                    }
                });

            });


        },
        getDefaultDescription: function() {
            var theDate = new Date();
            var year = theDate.getFullYear();
            var month = theDate.getMonth() + 1;//js从0开始取
            var date = theDate.getDate();
            var hour = theDate.getHours();
            var minutes = theDate.getMinutes();
            var second = theDate.getSeconds();

            var description = month+"月"+date+"日" + "分销促销活动";

            return description;
        },
        getContainer: function() {
            return AddDiscount.container;
        }

    }, AddDiscount.init);


})(jQuery,window));

