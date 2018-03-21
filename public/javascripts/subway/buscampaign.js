var TM = TM || {};

((function ($, window) {

    TM.BusCampaign = TM.BusCampaign || {};

    var BusCampaign = TM.BusCampaign;

    BusCampaign.init = BusCampaign.init || {};
    BusCampaign.init = $.extend({
        doInit: function(container) {
            BusCampaign.container = container;

            $.ajax({
                url : "/BusCampaign/checkAuth",
                data : {},
                type : 'post',
                error: function() {
                },
                success: function (dataJson) {
                    if (TM.SubwayBase.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }

                    BusCampaign.init.initEvent();

                    BusCampaign.init.initBalance();

                    BusCampaign.userRpt.doShow(7);
                    BusCampaign.planRpt.doShow(7);
                }
            });
        },
        initBalance: function() {
            $.ajax({
                url : "/BusCampaign/queryUserBalance",
                data : {},
                type : 'post',
                error: function() {
                },
                success: function (dataJson) {
                    if (TM.SubwayBase.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }

                    var balance = dataJson.results;
                    BusCampaign.container.find(".user-balance-span").html(balance);
                }
            });
        },
        initEvent: function() {
            var container = BusCampaign.container;

            container.find(".rpt-day-select").change(function() {
                var timeLength = $(this).val();
                BusCampaign.userRpt.doShow(timeLength);
            });

            container.find(".campaign-rpt-btn").click(function() {
                var timeLength = $(this).attr("day");
                BusCampaign.planRpt.doShow(timeLength);
            });

        }
    }, BusCampaign.init);


    BusCampaign.userRpt = BusCampaign.userRpt || {};
    BusCampaign.userRpt = $.extend({
        doShow: function(timeLength) {

            var rptObj = BusCampaign.container.find(".shop-rpt-div");

            $.ajax({
                url : "/BusCampaign/userDataRpt",
                data : {timeLength: timeLength},
                type : 'post',
                error: function() {
                },
                success: function (dataJson) {
                    if (TM.SubwayBase.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }

                    rptObj.find(".rpt-day-span").html(timeLength);

                    var rptJsonArray = dataJson.results;
                    if (rptJsonArray === undefined || rptJsonArray == null || rptJsonArray <= 1) {
                        alert("系统出现异常，获取店铺数据失败，请联系我们！");
                        return;
                    }
                    BusCampaign.userRpt.showTotalRpt(rptObj, rptJsonArray[0]);

                    BusCampaign.userRpt.showRptChart(rptJsonArray);
                }
            });


        },
        showTotalRpt: function(rptObj, totalRptJson) {
            rptObj.find(".total-cost-span").html(totalRptJson.cost);
            rptObj.find(".total-pay-amount-span").html(totalRptJson.payamount);
            rptObj.find(".total-pay-count-span").html(totalRptJson.paycount);
            rptObj.find(".total-click-span").html(totalRptJson.aclick);
            rptObj.find(".total-ctr-span").html(TM.SubwayBase.util.formatToPercent(totalRptJson.ctr));

            var clickConversation = 0;
            if (totalRptJson.aclick > 0) {
                clickConversation = (totalRptJson.paycount) / totalRptJson.aclick;
            }
            rptObj.find(".total-ccr-span").html(TM.SubwayBase.util.formatToPercent(clickConversation));

            rptObj.find(".total-cpc-span").html(totalRptJson.cpc.toFixed(2));

            rptObj.find(".total-roi-span").html(TM.SubwayBase.util.formatToPercent(totalRptJson.roi));
        },
        showRptChart: function(rptJsonArray) {


            var costArray = [];
            var payAmountArray = [];
            var payCountArray = [];
            var clickArray = [];
            var ctrArray = [];
            var ccrArray = [];
            var cpcArray = [];
            var roiArray = [];

            var xArray = [];

            if (rptJsonArray.length < 9) {
                return;
            }

            var isShowData = false;
            if (rptJsonArray.length == 9) {
                isShowData = true;
            }

            var remainDot = function(number) {
                var newNumber = Math.round(number * 10000) / 10000;
                return newNumber;
            }

            $(rptJsonArray).each(function(index, rptJson) {
                if (index < 2) {
                    return;
                }


                costArray[index - 2] = rptJson.cost;
                payAmountArray[index - 2] = rptJson.payamount;
                payCountArray[index - 2] = rptJson.paycount;
                clickArray[index - 2] = rptJson.aclick;
                ctrArray[index - 2] = remainDot(rptJson.ctr);

                var clickConversation = 0;
                if (rptJson.aclick > 0) {
                    clickConversation = (rptJson.paycount) / rptJson.aclick;
                }
                ccrArray[index - 2] = remainDot(clickConversation);
                cpcArray[index - 2] = remainDot(rptJson.cpc);
                roiArray[index - 2] = remainDot(rptJson.roi);

                var dayStr = rptJson.day;//格式：2013-08-23
                dayStr = dayStr.substring(dayStr.indexOf("-") + 1);
                //dayStr = dayStr.replace("-", ".");
                //dayStr = dayStr.replace("0", "");

                xArray[index - 2] = dayStr;

            });


            var chart = new Highcharts.Chart({
                chart : {
                    renderTo : 'shop-rpt-chart',
                    defaultSeriesType: 'line' //图表类型line(折线图)
                },
                credits : {
                    enabled: false   //右下角不显示LOGO
                },
                title: {
                    text: '店铺趋势'
                }, //图表标题
                xAxis: {  //x轴
                    categories: xArray, //x轴标签名称
                    gridLineWidth: 0, //设置网格宽度为1
                    lineWidth: 1,  //基线宽度
                    labels:{y:16}  //x轴标签位置：距X轴下方26像素
                },
                yAxis: [{  //y轴
                    title: {text: '店铺数据'}, //标题
                    lineWidth: 1 //基线宽度
                }, {
                    title: {text: '店铺数据'}, //标题
                    opposite: true,
                    lineWidth: 1 //基线宽度
                }],
                plotOptions:{ //设置数据点
                    line:{
                        dataLabels:{
                            enabled: isShowData  //在数据点上显示对应的数据值
                        },
                        enableMouseTracking: true //取消鼠标滑向触发提示框
                    }
                },
                series: [
                    {  //数据列
                        name: '总花费',
                        data: costArray,
                        yAxis:0
                    },
                    {  //数据列
                        name: '成交额',
                        data: payAmountArray,
                        yAxis:0
                    },
                    {  //数据列
                        name: '成交数',
                        data: payCountArray,
                        yAxis:0
                    },
                    {  //数据列
                        name: '点击量',
                        data: clickArray,
                        yAxis:0
                    }/*,
                    {  //数据列
                        name: '点击率',
                        data: ctrArray,
                        yAxis:1
                    },
                    {  //数据列
                        name: '点击转化率',
                        data: ccrArray,
                        yAxis:1
                    },
                    {  //数据列
                        name: '平均点击花费',
                        data: cpcArray,
                        yAxis:1
                    },
                    {  //数据列
                        name: '投资转化率',
                        data: roiArray,
                        yAxis:1
                    }*/
                ]
            });



        }
    }, BusCampaign.userRpt);




    BusCampaign.planRpt = BusCampaign.planRpt || {};
    BusCampaign.planRpt = $.extend({
        doShow: function(timeLength) {
            var dayStr = "";
            if (timeLength == 1) {
                dayStr = "一";
            } else if (timeLength == 3) {
                dayStr = "三";
            } else if (timeLength == 7) {
                dayStr = "七";
            } else {
                dayStr = timeLength;
            }

            var container = BusCampaign.container;

            var rptObj = container.find(".campaign-rpt-div");

            $.ajax({
                url : "/BusCampaign/queryRptCampaigns",
                data : {timeLength: timeLength},
                type : 'post',
                error: function() {
                },
                success: function (dataJson) {
                    if (TM.SubwayBase.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    rptObj.html("");


                    var planJsonArray = dataJson.results;
                    if (planJsonArray === undefined || planJsonArray == null || planJsonArray.length <= 0) {
                        var errorHtml = '<div style="padding-top: 20px; font-weight: bold; font-size: 20px; margin: 0 auto;">您当前不存在推广计划！</div> ';
                        rptObj.html(errorHtml);
                        return;
                    }

                    $(planJsonArray).each(function(index, planJson) {
                        var campaignObj = BusCampaign.planRpt.createOneCampaign(index, planJson, dayStr);
                        rptObj.append(campaignObj);
                    });
                }
            });

        },
        createOneCampaign: function(index, planJson, dayStr) {
            var planHtml = BusCampaign.container.find("#campaign-rpt-template-div").html();
            var planObj = $(planHtml);

            planObj.find(".campaign-rpt-day").html(dayStr);

            var onlineStatus = "";
            if (planJson.onlineStatus == "ON") {
                onlineStatus = "已推广";
            } else {
                onlineStatus = "未推广"
            }
            planObj.find(".campaign-title").html(planJson.title + "(" + onlineStatus + ")");

            planObj.find(".online-status").html(onlineStatus);
            planObj.find(".campaign-budget").html(planJson.budget);
            planObj.find(".campaign-impression").html(planJson.impressions);
            planObj.find(".campaign-click").html(planJson.aclick);
            planObj.find(".campaign-ctr").html(TM.SubwayBase.util.formatToPercent(planJson.ctr));
            planObj.find(".campaign-cost").html(planJson.cost);
            planObj.find(".campaign-cpc").html(planJson.cpc);
            planObj.find(".campaign-pay-amount").html(planJson.payamount);
            planObj.find(".campaign-pay-count").html(planJson.paycount);
            planObj.find(".campaign-fav-count").html(planJson.favshopcount + planJson.favitemcount);
            planObj.find(".campaign-roi").html(TM.SubwayBase.util.formatToPercent(planJson.roi));

            return planObj;
        }
    }, BusCampaign.planRpt);




})(jQuery,window));