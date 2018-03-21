

var TM = TM || {};

((function ($, window) {

    TM.AddShopDiscount = TM.AddShopDiscount || {};

    var AddShopDiscount = TM.AddShopDiscount || {};

    AddShopDiscount.init = AddShopDiscount.init || {};
    AddShopDiscount.init = $.extend({

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

                    AddShopDiscount.init.doWithNowTime(container, nowTime);

                }
            });



        },
        doWithNowTime: function(container, nowTime) {


            AddShopDiscount.container = container;

            var description = AddShopDiscount.init.getDefaultDescription(nowTime);

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
                if (paramData == undefined || paramData == null) {
                    return;
                }
                paramData = TM.DiscountCommon.params.addDazheParam(paramData);
                if (paramData == undefined || paramData == null) {
                    return;
                }

                if (confirm("您当前设置的全店折扣是" + (paramData.discountRate / 100) + "折，确定要创建全店打折活动？") == false) {
                    return;
                }

                $.ajax({
                    url : "/umpactivity/createShopActivity",
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
        getDefaultDescription: function(nowTime) {
            var theDate = new Date(nowTime);
            var year = theDate.getFullYear();
            var month = theDate.getMonth() + 1;//js从0开始取
            var date = theDate.getDate();
            var hour = theDate.getHours();
            var minutes = theDate.getMinutes();
            var second = theDate.getSeconds();

            var description = month+"月"+date+"日" + "全店打折";

            return description;
        },
        getContainer: function() {
            return AddShopDiscount.container;
        }

    }, AddShopDiscount.init);


})(jQuery,window));

