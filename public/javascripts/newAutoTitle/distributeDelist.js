var TM = TM || {};

((function ($, window) {


    TM.DistributeDelist = TM.DistributeDelist || {};
    var DistributeDelist = TM.DistributeDelist;

    DistributeDelist.init = DistributeDelist.init || {};
    DistributeDelist.init = $.extend({
        doInit:function (container, planId, isEdit) {
            DistributeDelist.init.planId = planId;
            DistributeDelist.container = container;
            DistributeDelist.init.initHead();
            // 这里其实应该传入一个planId，然后对该plan的宝贝进行默认分布
            if(isEdit == false) {
                $.post("/DelistPlan/calcuDistribute", {planId: planId, weeks: "1,2,3,4,5,6,7", hours: "9,10,11,14,15,16,19,20,21,22"}, function (data) {
                    if (data === undefined || data == null) {
                        TM.Alert.load("获取计划信息出错");
                        return;
                    }
                    if (data.length <= 0) {
                        TM.Alert.load("获取计划信息出错");
                        return;
                    }

                    var planDistributeTable = $('#planDistributeTable').tmpl(DistributeDelist.util.getHourDistribute(24, data[24]));
                    $(data).each(function (i, hour) {
                        if (i < 24) {
                            var hourDis = DistributeDelist.util.getHourDistribute(i, hour, false);
                            planDistributeTable.find('tbody').append($('#planDistributeTableTr').tmpl(hourDis));
                        }

                    });

                    DistributeDelist.container.find('.planDistributeTableWrapper').append(planDistributeTable);
                    DistributeDelist.Event.setEvent();
                });
            }else{
                $.post("/DelistPlan/queryDetailDistribute", {planId: planId}, function (data) {
                    if (data === undefined || data == null) {
                        TM.Alert.load("获取计划信息出错");
                        return;
                    }
                    if (data.length <= 0) {
                        TM.Alert.load("获取计划信息出错");
                        return;
                    }

                    var planDistributeTable = $('#planDistributeTable').tmpl(DistributeDelist.util.getHourDistribute(24, data[24]));
                    $(data).each(function (i, hour) {
                        if (i < 24) {
                            var hourDis = DistributeDelist.util.getHourDistribute(i, hour, true);
                            planDistributeTable.find('tbody').append($('#planDistributeTableTr').tmpl(hourDis));
                        }

                    });

                    DistributeDelist.container.find('.planDistributeTableWrapper').append(planDistributeTable);
                    DistributeDelist.Event.setEvent();
                });
            }

        },
        initHead:function(){
            $('.nav_bar').find('.header-nav').removeClass('current');
            $('.nav_bar').find('.auto-delist').addClass("current");
        }
    }, DistributeDelist.init);

    DistributeDelist.util = DistributeDelist.util || {};
    DistributeDelist.util = $.extend({
        getHourDistribute:function (index, hour, isEdit) {
            var hourDis = {week_1:0, week_2:0, week_3:0, week_4:0, week_5:0, week_6:0, week_7:0};
            if (hour === undefined || hour == null) {
                return hourDis;
            }
            if (hour.length <= 0) {
                return hourDis;
            }


            var count = 0;
            $(hour).each(function (i, num) {
                if (parseInt(num) < 0) {
                    num = 0;
                }
                if (hourDis.toforbiden == false) {
                    hourDis["week_" + (i + 1)] = "~";
                    count = 0;
                    return;
                }
                hourDis["week_" + (i + 1)] = num;
                count += parseInt(num);
            });
//            if(isEdit) {
//                if(count > 0) {
//                    hourDis.toforbiden = true;
//                    hourDis.timeSlotForbidden = "";
//                } else {
//                    hourDis.toforbiden = false;
//                    hourDis.timeSlotForbidden = "timeSlotForbidden";
//                    $(hour).each(function (i, num) {
//                        hourDis["week_" + (i + 1)] = "~";
//
//                    });
//                }
//            } else {
//                if (index == 9 || index == 10 || index == 11 || index == 14 || index == 15 || index == 16 || index == 19 || index == 21 || index == 20 || index == 22 || index == 24) {
//                    hourDis.toforbiden = true;
//                    hourDis.timeSlotForbidden = "";
//                } else {
//                    hourDis.toforbiden = false;
//                    hourDis.timeSlotForbidden = "timeSlotForbidden";
//                }
//            }
            if(!isEdit) {
                if (index == 9 || index == 10 || index == 11 || index == 14 || index == 15 || index == 16 || index == 19 || index == 21 || index == 20 || index == 22 || index == 24) {
                    hourDis.toforbiden = true;
                    hourDis.timeSlotForbidden = "";
                } else {
                    hourDis.toforbiden = false;
                    hourDis.timeSlotForbidden = "timeSlotForbidden";
                }
            }
            if(count > 0) {
                hourDis.toforbiden = true;
                hourDis.timeSlotForbidden = "";
            } else {
                hourDis.toforbiden = false;
                hourDis.timeSlotForbidden = "timeSlotForbidden";
                $(hour).each(function (i, num) {
                    hourDis["week_" + (i + 1)] = "~";

                });
            }


            hourDis.text = index + "~" + (index + 1) + "点";
            hourDis.hourCount = count;
            if (index == 24) {
                DistributeDelist.planTotalItems = count;
                DistributeDelist.container.find('.planConfig .planTotalItems').text(count);
                DistributeDelist.container.find('.planConfig .planDistributedItems').text(count);
            }

            return hourDis;

        },
        clearRow:function (row) {
            var rowCount = 0;
            row.find('.timeSlotTd .numSpan').each(function(){
                if($(this).text() == "~") {
                    return;
                }
                rowCount += parseInt($(this).text());
            });
            row.find('.timeSlotTd .numSpan').text("~");
            row.find('.timeSlotTd').addClass("timeSlotForbidden");
            DistributeDelist.util.updatePlanConfig(rowCount);
            DistributeDelist.util.updateHourCount(row, 0);
        },
        clearColumn: function(val){
            var columnCount = 0;
            DistributeDelist.container.find('.timeSlotTd[value="'+val+'"] .numSpan').each(function(){
                if($(this).text() == "~") {
                    return;
                }
                columnCount += parseInt($(this).text());
            });
            DistributeDelist.container.find('.timeSlotTd[value="'+val+'"] .numSpan').text("~");
            DistributeDelist.container.find('.timeSlotTd[value="'+val+'"]').addClass("timeSlotForbidden");
            DistributeDelist.util.updatePlanConfig(columnCount);
            DistributeDelist.util.updateWeekDayCount(val, 0);
        },
        activeRow: function(row){
            row.find('.timeSlotTd .numSpan').text("0");
            row.find('.timeSlotTd').removeClass("timeSlotForbidden");
        },
        activeColumn: function(val){
            DistributeDelist.container.find('.timeSlotTd[value="'+val+'"] .numSpan').text("0");
            DistributeDelist.container.find('.timeSlotTd[value="'+val+'"]').removeClass("timeSlotForbidden");
        },
        updatePlanConfig: function(count) {
            if(isNaN(count)) {
                return;
            }
            DistributeDelist.container.find('.planDistributedItems').text(parseInt(DistributeDelist.container.find('.planDistributedItems').text()) - count);
            DistributeDelist.container.find('.planUnDistributedItems').text(parseInt(DistributeDelist.container.find('.planUnDistributedItems').text()) + count);
        },
        updateWeekDayCount: function(value, count){
            DistributeDelist.container.find('.weekDayCount[value="'+value+'"]').text(count);
        },
        updateHourCount: function(row, count){
            row.find('.hourCount').text(count);
        },
        rowAdd: function(row){

            var addCount = row.find('.timeSlotForbidden').length - 7;
            if(0 - addCount > parseInt(DistributeDelist.container.find('.planUnDistributedItems').text())) {
                TM.Alert.load("当前未分配宝贝数<span style='padding: 0 10px;font-weight: bold;color: red;font-size: 16px;'>"+DistributeDelist.container.find('.planUnDistributedItems').text()+"</span>不足以为该行批量加1个宝贝");
                return;
            }
            row.find('.timeSlotTd .numSpan').each(function(){
                if($(this).text() == "~") {
                    return;
                }
                $(this).text(parseInt($(this).text()) + 1);
            });
            DistributeDelist.util.updatePlanConfig(addCount);
            DistributeDelist.util.updateHourCount(row, parseInt(row.find('.hourCount').text()) - addCount);
        },
        columnAdd: function(val){
            var addCount = DistributeDelist.container.find('.timeSlotForbidden[value="'+val+'"]').length - 24;
            if(0 - addCount > parseInt(DistributeDelist.container.find('.planUnDistributedItems').text())) {
                TM.Alert.load("当前未分配宝贝数<span style='padding: 0 10px;font-weight: bold;color: red;font-size: 16px;'>"+DistributeDelist.container.find('.planUnDistributedItems').text()+"</span>不足以为该列批量加1个宝贝");
                return;
            }
            var weekDayCount = 0;
            DistributeDelist.container.find('.timeSlotTd[value="'+val+'"] .numSpan').each(function(){
                if($(this).text() == "~") {
                    return;
                }
                var oldNum = parseInt($(this).text());
                $(this).text(oldNum + 1);
                weekDayCount += oldNum + 1;
            });
            DistributeDelist.util.updatePlanConfig(addCount);
            DistributeDelist.util.updateWeekDayCount(val, weekDayCount);
        },
        rowMinus: function(row){

            var minusCount = 0;

            row.find('.timeSlotTd .numSpan').each(function(){
                if($(this).text() == "~") {
                    return;
                }
                var oldNum = parseInt($(this).text());
                if(oldNum > 0) {
                    $(this).text(oldNum - 1);
                    minusCount++;
                }

            });
            DistributeDelist.util.updatePlanConfig(minusCount);
            DistributeDelist.util.updateHourCount(row, parseInt(row.find('.hourCount').text()) - minusCount);
        },
        columnMinus: function(val){
            var minusCount = 0;
            var weekDayCount = 0;
            DistributeDelist.container.find('.timeSlotTd[value="'+val+'"] .numSpan').each(function(){
                if($(this).text() == "~") {
                    return;
                }
                var oldNum = parseInt($(this).text());
                if(oldNum > 0) {
                    $(this).text(oldNum - 1);
                    weekDayCount += oldNum - 1;
                    minusCount++;
                }


            });
            DistributeDelist.util.updatePlanConfig(minusCount);
            DistributeDelist.util.updateWeekDayCount(val, weekDayCount);
        }
    }, DistributeDelist.util);

    DistributeDelist.Event = DistributeDelist.Event || {};
    DistributeDelist.Event = $.extend({
        setEvent:function () {
            DistributeDelist.container.find('.inDistribute').unbind('click').click(function () {
                if ($(this).hasClass("toforbiden")) {
                    var timeText = $(this).parent().attr("timeText");
                    if (confirm("禁止分布将会清零" + timeText + "时间段宝贝，确定要禁止分布该时间段?")) {
                        $(this).parent().find('.batchOperation').hide();
                        $(this).removeClass("toforbiden");
                        $(this).text("点我开启分配");
                        // 如果是按行修改
                        if ($(this).attr("isWeekBatch") == "true") {
                            DistributeDelist.util.clearRow($(this).parent().parent());
                        }
                        // 如果是按照列修改
                        else {
                            var val = $(this).attr("value");
                            DistributeDelist.util.clearColumn(val);
                        }
                    }

                } else {
                    $(this).parent().find('.batchOperation').show();
                    $(this).addClass("toforbiden");
                    $(this).text("点我禁止分配");
                    // 如果是按行修改
                    if ($(this).attr("isWeekBatch") == "true") {
                        DistributeDelist.util.activeRow($(this).parent().parent());
                    }
                    // 如果是按照列修改
                    else {
                        var val = $(this).attr("value");
                        DistributeDelist.util.activeColumn(val);
                    }
                }

            });

            DistributeDelist.container.find('.timeSlotTd').unbind("hover").hover(function () {
                if ($(this).hasClass("timeSlotForbidden")) {
                    return;
                }
                $(this).find('.numSpan').hide();
                $(this).find('.editManual .editManualInput').val($(this).find('.numSpan').text());
                $(this).find('.editManual').show();
            }, function () {
                if ($(this).hasClass("timeSlotForbidden")) {
                    return;
                }
                $(this).find('.numSpan').show();
                $(this).find('.editManual').hide();
            });

            DistributeDelist.container.find('.batchAdd').unbind('click').click(function(){
                // 如果是按行加1
                if ($(this).attr("isWeekBatch") == "true") {
                    DistributeDelist.util.rowAdd($(this).parent().parent().parent());
                }
                // 如果是按列加1
                else {
                    var val = $(this).parent().attr("value");
                    DistributeDelist.util.columnAdd(val);
                }
            });

            DistributeDelist.container.find('.batchMinus').unbind('click').click(function(){
                // 如果是按行减1
                if ($(this).attr("isWeekBatch") == "true") {
                    DistributeDelist.util.rowMinus($(this).parent().parent().parent());
                }
                // 如果是按列减1
                else {
                    var val = $(this).parent().attr("value");
                    DistributeDelist.util.columnMinus(val);
                }
            });

            DistributeDelist.container.find('.editManual .editManualInput').unbind('blur').blur(function(){
                var inputVal = $(this).val();
                var originVal = parseInt($(this).parent().parent().find('.numSpan').text());
                var interval = parseInt($(this).val()) - originVal;

                if(isNaN(inputVal) || inputVal == "") {
                    TM.Alert.load("亲，请输入数字", 400, 300, function(){
                        $(this).val(originVal);
                    });
                } else if(parseInt(inputVal) < 0) {
                    TM.Alert.load("亲，数字需大于0", 400, 300, function(){
                        $(this).val(originVal);
                    });
                } else if(interval > parseInt(DistributeDelist.container.find('.planUnDistributedItems').text())) {
                    TM.Alert.load("亲，当前未设置宝贝数不足以支持亲输入的数字", 400, 300, function(){
                        $(this).val(originVal);
                    });
                } else {
                    $(this).parent().parent().find('.numSpan').text(inputVal);

                    // 更新计划分配与未分配宝贝数量
                    DistributeDelist.util.updatePlanConfig(0 - interval);

                    // 更新列总数
                    var value = $(this).parent().parent().attr("value");
                    DistributeDelist.util.updateWeekDayCount(value, parseInt(DistributeDelist.container.find('.weekDayCount[value="'+value+'"]').text()) + interval);

                    // 更新行总数
                    var row = $(this).parent().parent().parent();
                    DistributeDelist.util.updateHourCount(row, parseInt(row.find('.hourCount').text()) + interval);
                }

            });

            DistributeDelist.container.find('.finishDelistPlan').unbind('click').click(function(){
                if(parseInt(DistributeDelist.container.find('.planUnDistributedItems')) > 0) {
                    TM.Alert.load("亲，当前计划还有剩余宝贝未进行分配，请分配完成后再点击提交");
                    return;
                }
                if(parseInt(DistributeDelist.container.find('.planUnDistributedItems')) < 0) {
                    TM.Alert.load("亲，您分配的宝贝总数超过当前计划允许宝贝数，请重新分配后再点击提交");
                    return;
                }
                if(confirm("亲，确认提交当前宝贝分布?") == false) {
                    return;
                }
                var hourRates = [];
                for(var i = 0; i < 7; i++){
                    DistributeDelist.container.find('.timeSlotTd[value="'+((i+6)%7 + 1)+'"]').each(function(){
                        if($(this).hasClass("timeSlotForbidden")) {
                            hourRates.push(0);
                            return;
                        }
                        var num = parseInt($(this).find('.numSpan').text());

                        hourRates.push(num);
                    });
                }
                $.post('/DelistPlan/reDistribute',{planId:DistributeDelist.init.planId, hourRates:hourRates.join(","), isTurnOn: true}, function(data){
                    if(data === undefined || data == null) {
                        TM.Alert.load("计划创建失败，请重试或联系客服");
                        return;
                    }
                    if(data.success == false){
                        TM.Alert.load("计划创建失败，请重试或联系客服");
                        return;
                    }
                    TM.Alert.load("计划创建成功", 400, 300, function(){
                        // 这里做跳转动作
                        window.location.href = "/newAutoTitle/delistPlans";
                    });
                })
            });

        }, updateWeekDayCount: function(value, count){
            DistributeDelist.container.find('.weekDayCount[value="'+value+'"]').text(count);
        },
        updateHourCount: function(row, count){
            row.find('.hourCount').text(count);
        }
    }, DistributeDelist.Event);
})(jQuery, window));