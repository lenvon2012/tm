
((function ($, window) {

    TM.DiscountCommon = TM.DiscountCommon || {};

    var DiscountCommon = TM.DiscountCommon || {};

    DiscountCommon.init = DiscountCommon.init || {};
    DiscountCommon.init = $.extend({

        doInit: function(container, isCreateActivity) {

            DiscountCommon.container = container;

            DiscountCommon.params.isModifyEndTimeWhileStartTimeChanges = isCreateActivity;
            DiscountCommon.params.isCreateActivity = isCreateActivity;

            //设置event
            DiscountCommon.event.initTimeEvent();

            DiscountCommon.event.initNormalEvent();



            //检查
            DiscountCommon.checker.checkerTime();
            DiscountCommon.checker.checkActivityTitle();
            DiscountCommon.checker.checkDescription();

            DiscountCommon.checker.checkActivityDiscountRate();
        },
        getContainer: function() {
            return DiscountCommon.container;
        }

    }, DiscountCommon.init);

    DiscountCommon.event = DiscountCommon.event || {};
    DiscountCommon.event = $.extend({
        initTimeEvent: function() {

            var container = DiscountCommon.init.getContainer();

            var startTimeObj = container.find('.start-date');
            var endTimeObj = container.find('.end-date');


            var initEndTimePicker = function(minEndDate) {
                endTimeObj.datetimepicker({
                    beforeShow: function() {
                        setTimeout(function(){
                            $('.ui-datepicker').css('z-index', 999999);
                        }, 0);
                    },
                    minDate: minEndDate,
                    onClose: function(input, inst) {

                        var endTimeDate = endTimeObj.datetimepicker('getDate');
                        if (!endTimeDate) {
                            DiscountCommon.checker.checkerTime();
                            DiscountCommon.params.isModifyEndTimeWhileStartTimeChanges = false;
                            return;
                        }
                        if (endTimeDate.getTime() != DiscountCommon.params.originEndTime) {
                            DiscountCommon.params.isModifyEndTimeWhileStartTimeChanges = false;
                        }

                        DiscountCommon.params.originEndTime = endTimeDate.getTime();

                        DiscountCommon.checker.checkerTime();
                    }
                });

            }


            startTimeObj.datetimepicker({
                beforeShow: function() {
                    setTimeout(function(){
                        $('.ui-datepicker').css('z-index', 999999);
                    }, 0);
                },
                minDate: new Date(),
                onClose: function(input, inst) {
                    var startTimeDate = startTimeObj.datetimepicker('getDate');
                    if (!startTimeDate) {
                        DiscountCommon.checker.checkerTime();
                        return;
                    }

                    endTimeObj.datetimepicker('destroy');
                    initEndTimePicker(startTimeDate);

                    if (DiscountCommon.params.isModifyEndTimeWhileStartTimeChanges == true) {

                        var monthMillis = 30 * 24 * 60 * 60 * 1000;

                        var endTime = startTimeDate.getTime() + monthMillis;

                        var endDate = new Date(endTime);
                        endTimeObj.datetimepicker('setDate', endDate);

                        DiscountCommon.params.originEndTime = endDate.getTime();
                    }

                    DiscountCommon.checker.checkerTime();
                }
            });

            initEndTimePicker(new Date());


        },
        initNormalEvent: function() {

            var container = DiscountCommon.init.getContainer();

            container.find(".row-tip").each(function() {
                $(this).attr("normal", $(this).html());
            });

            var descInputObj = container.find(".activity-description");
            var startTimeObj = container.find(".start-date");
            var endTimeObj = container.find(".end-date");
            var titlePutObj = container.find(".activity-title-input");
            var discountRateObj = container.find('.shop-discount-input');



            descInputObj.keyup(function(event) {
                DiscountCommon.checker.checkDescription();
            });
            descInputObj.blur(function() {
                DiscountCommon.checker.checkDescription();
            });

            titlePutObj.keyup(function(event) {
                DiscountCommon.checker.checkActivityTitle();
            });
            titlePutObj.blur(function() {
                DiscountCommon.checker.checkActivityTitle();
            });

            startTimeObj.keyup(function(event) {
                DiscountCommon.checker.checkerTime();
            });
            endTimeObj.keyup(function(event) {
                DiscountCommon.checker.checkerTime();
            });


            discountRateObj.keyup(function(event) {
                DiscountCommon.checker.checkActivityDiscountRate();
            });
            discountRateObj.blur(function() {
                DiscountCommon.checker.checkActivityDiscountRate();
            });



            titlePutObj.hide();

            var selectPutObj = container.find(".select-put");
            var selfPutObj = container.find('.self-put');
            var titleSelectObj = container.find(".activity-title-select");

            selectPutObj.hide();
            selfPutObj.click(function(){
                titleSelectObj.hide();
                selfPutObj.hide();
                titlePutObj.show();
                selectPutObj.show();
                titlePutObj.addClass("target-activity-title") ;
                titleSelectObj.removeClass("target-activity-title") ;

                DiscountCommon.checker.checkActivityTitle();
            });
            selectPutObj.click(function(){
                //titlePutObj.val("");
                titlePutObj.hide();
                selectPutObj.hide();
                titleSelectObj.show();
                selfPutObj.show();
                titleSelectObj.addClass("target-activity-title") ;
                titlePutObj.removeClass("target-activity-title") ;

                titlePutObj.removeClass("input-error");


                DiscountCommon.checker.checkActivityTitle();
            });


        }
    }, DiscountCommon.event);

    DiscountCommon.params = DiscountCommon.params || {};
    DiscountCommon.params = $.extend({

        isModifyEndTimeWhileStartTimeChanges: true,
        originEndTime: 0,
        isCreateActivity: true,

        getSubmitParams: function() {

            var container = DiscountCommon.init.getContainer();

            var startTimeObj = container.find('.start-date');
            var endTimeObj = container.find('.end-date');
            var descObj = container.find(".activity-description");
            var titleObj = container.find(".target-activity-title");


            var startDate = startTimeObj.datetimepicker('getDate');
            var endDate = endTimeObj.datetimepicker('getDate');



            if (!startDate) {
                alert("请先设置活动的起始时间！");
                return null;
            }
            if (!endDate) {
                alert("请先设置活动的结束时间！");
                return null;
            }

            if (endDate.getTime() <= startDate.getTime()) {
                alert("活动结束时间必须大于开始时间！");
                return null;
            }

            var errorObjs = container.find(".input-error");

            if(errorObjs.length > 0) {
                alert("当前您的设置还存在错误，请修改后再提交!");
                return null;
            }

            var paramData = {};
            paramData.startTimeStr = startTimeObj.val();
            paramData.endTimeStr = startTimeObj.val();
            paramData.title = titleObj.val();
            paramData.description = descObj.val();


            //不知道为什么，不这样，ie下在ajax的时候title参数传过去是空的。。。。
            if (paramData.title == "限时促销") {
                paramData.title = "限时促销";
            } else if (paramData.title == "新春特惠") {
                paramData.title = "新春特惠";
            } else if (paramData.title == "团购价") {
                paramData.title = "团购价";
            } else if (paramData.title == "亏本清仓") {
                paramData.title = "亏本清仓";
            } else if (paramData.title == "新品上市") {
                paramData.title = "新品上市";
            }



            return paramData;


        },
        addDazheParam: function(paramData) {

            var errorMsg = DiscountCommon.checker.checkActivityDiscountRate();

            if (errorMsg == '') {

            } else {
                alert(errorMsg);
                return null;
            }

            var container = DiscountCommon.init.getContainer();

            var discountObj = container.find('.shop-discount-input');

            var discountRate = discountObj.val();

            discountRate = DiscountCommon.util.formatDiscountRate(discountRate);

            paramData.discountRate = discountRate;

            return paramData;

        }

    }, DiscountCommon.params);


    DiscountCommon.checker = DiscountCommon.checker || {};
    DiscountCommon.checker = $.extend({
        checkerTime: function() {

            var container = DiscountCommon.init.getContainer();

            var startTimeObj = container.find('.start-date');
            var endTimeObj = container.find('.end-date');


            var startDate = startTimeObj.datetimepicker('getDate');
            var endDate = endTimeObj.datetimepicker('getDate');

            if (!startDate) {
                DiscountCommon.util.showError(startTimeObj, '活动开始时间格式不正确');
            } else {
                DiscountCommon.util.showOriginTip(startTimeObj);
            }

            if (!endDate) {
                DiscountCommon.util.showError(endTimeObj, '活动结束时间格式不正确');

            } else {

                DiscountCommon.util.showTip(endTimeObj, '');

                if (!startDate) {
                    DiscountCommon.util.showOriginTip(endTimeObj);
                } else {

                    if (endDate.getTime() <= startDate.getTime()) {
                        DiscountCommon.util.showError(endTimeObj, '活动结束时间必须大于开始时间');
                    } else {
                        var activityTime = endDate.getTime() - startDate.getTime();
                        var activityDay = Math.round(activityTime / 86400000);
                        DiscountCommon.util.showTip(endTimeObj, "促销活动结束时间，当前活动将持续 <span class='activity-interal-day'>" + activityDay + "</span> 天");
                    }

                }

            }

        },
        checkDescription: function() {

            var container = DiscountCommon.init.getContainer();

            var descObj = container.find(".activity-description");

            var description = $.trim(descObj.val());
            if(description.length < 2) {
                var text = '请至少输入2个字';
                DiscountCommon.util.showError(descObj, text);
            } else if (description.length > 30) {
                var text = '最多只能输入30个字，您已超出';
                DiscountCommon.util.showError(descObj, text);
            } else {
                DiscountCommon.util.showOriginTip(descObj);
            }
        },
        checkActivityTitle: function() {
            var container = DiscountCommon.init.getContainer();

            var titleObj = container.find(".target-activity-title");

            var reg = /^(\w|[\u4E00-\u9FA5]|_)*$/;

            var title = $.trim(titleObj.val());
            if(title.length <= 0) {
                var text = '请先输入宝贝促销标签';
                DiscountCommon.util.showError(titleObj, text);
            } else if (title.length > 5) {
                var text = '促销标签最多5个字，您已超出';
                DiscountCommon.util.showError(titleObj, text);
            } else {
                if (!reg.test(title)) {
                    var text = '标签只允许出现中英文、数字、下划线!';
                    DiscountCommon.util.showError(titleObj, text);
                } else {
                    DiscountCommon.util.showOriginTip(titleObj);
                }
            }


        },
        checkActivityDiscountRate: function() {
            var container = DiscountCommon.init.getContainer();

            var discountObj = container.find('.shop-discount-input');

            if (discountObj.length <= 0) {
                return;
            }

            var discountRate = discountObj.val();

            var errorMsg = '';

            if (discountRate == "") {
                errorMsg = "请先输入全店宝贝的折扣！";
            } else if (isNaN(discountRate)) {
                errorMsg = "全店宝贝的折扣必须是数字！";
            } else {
                discountRate = DiscountCommon.util.formatDiscountRate(discountRate) / 100;
                if (discountRate <= 0) {
                    errorMsg = "全店宝贝的折扣必须大于0！";
                } else if (discountRate >= 10) {
                    errorMsg = "全店宝贝的折扣必须小于10！";
                } else {
                    errorMsg = '';
                }
            }

            if (errorMsg == '') {
                DiscountCommon.util.showOriginTip(discountObj);
            } else {
                DiscountCommon.util.showError(discountObj, errorMsg);
            }

            return errorMsg;
        }
    }, DiscountCommon.checker);




    DiscountCommon.util = DiscountCommon.util || {};
    DiscountCommon.util = $.extend({
        formatDiscountRate: function(discountRate) {
            if (discountRate == "") {
                return 1000;
            } else if (isNaN(discountRate)) {
                return 1000;
            } else if (discountRate <= 0) {
                return 1000;
            } else if (discountRate >= 10) {
                return 1000;
            } else {
                //四舍五入
                discountRate = Math.round(discountRate * 100);

                return discountRate;
            }
        },
        showError: function(inputObj, errorMsg) {
            var tipObj = inputObj.parent().find('.row-tip');
            inputObj.addClass("input-error");
            tipObj.html(errorMsg);
            tipObj.addClass("input-error");
        },
        showOriginTip: function(inputObj) {
            var tipObj = inputObj.parent().find('.row-tip');
            inputObj.removeClass("input-error");
            tipObj.html(tipObj.attr("normal"));
            tipObj.removeClass("input-error");
        },
        showTip: function(inputObj, tipMsg) {
            var tipObj = inputObj.parent().find('.row-tip');
            inputObj.removeClass("input-error");
            tipObj.html(tipMsg);
            tipObj.removeClass("input-error");
        },
        parseLongToDate: function(ts) {
            var theDate = new Date();
            theDate.setTime(ts);
            var year = theDate.getFullYear();
            var month = theDate.getMonth() + 1;//js从0开始取
            var date = theDate.getDate();
            var hour = theDate.getHours();
            var minutes = theDate.getMinutes();
            var second = theDate.getSeconds();

            if (month < 10) {
                month = "0" + month;
            }
            if (date < 10) {
                date = "0" + date;
            }
            if (hour < 10) {
                hour = "0" + hour;
            }
            if (minutes < 10) {
                minutes = "0" + minutes;
            }
            if (second < 10) {
                second = "0" + second;
            }
            var timeStr = year+"-"+ month+"-"+date+" "+hour+":"+minutes+ ":" + second;
            return timeStr;
        }
    }, DiscountCommon.util);




})(jQuery,window));