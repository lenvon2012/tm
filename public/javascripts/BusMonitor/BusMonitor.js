
/**
 * 直通车监控
 * @type {*}
 */
var BusMonitor = BusMonitor || {};

BusMonitor.init = BusMonitor.init || {};

BusMonitor.targetObj = null;
BusMonitor.targetNumIid = null;

BusMonitor.monitorType = {
    RealTime: 0,//实时监控
    ClickCount: 1//点击统计
};

BusMonitor.currentType = BusMonitor.monitorType.RealTime;//确定现在是在哪个tab

BusMonitor.init = $.extend({
    doInit: function(targetObj, numIid) {
        targetObj.html("");
        BusMonitor.targetObj = targetObj;
        BusMonitor.targetNumIid = numIid;
        BusMonitor.element.create(targetObj);
        BusMonitor.realTime.show();
    },
    isSingleItem: function() {
        var numIid = BusMonitor.targetNumIid;
        if (numIid === undefined || numIid == null)
            return false;
        return true;
    }
}, BusMonitor.init);

/**
 * 创建页面上的元素
 * @type {*}
 */
BusMonitor.element = BusMonitor.element || {};

BusMonitor.element = $.extend({
    create: function(targetObj) {
        var html = "<div class=\"BusMonitorBlock\">"
            + "<div class=\"RealTimeDiv\">"
            + "   <div class=\"OperationDiv\">"
            + "        <span class=\"chartTitle\">"
            + "        直通车实时点击量监控"
            + "        </span>"
            + "       <span class='blueImgBtn refreshRealTime' style='margin-left: 10px;'>刷新</span> "
            + "        <div style=\"float: right;\">"
            + "            <span class=\"blueImgBtn realTimeBtn\">直通车监控</span>"
            + "            <span class=\"whiteImgBtn countBtn\">直通车统计</span>"
            + "        </div>"
            + "        <div class=\"blank0\" style=\"height: 10px;\"></div>"


            + "        <select class=\"monitorSelect realTimeSelect\">"
            + "            <option value=\"1\">今日</option>"
            + "            <option value=\"2\" selected=\"selected\">两天比较</option>"
            + "            <option value=\"3\">三天比较</option>"
            + "            <option value=\"4\">四天比较</option>"
            + "        </select>"
            + "        <div class=\"blank0\"></div>"
            + "    </div>"
            + "    <div class=\"HourChartDiv\">"
            + "    </div>"
            + "    </div>"
            + "    <div class=\"ClickCountDiv\">"
            + "    <div class=\"OperationDiv\">"
            + "        <span class=\"chartTitle\">"
            + "        直通车点击量统计"
            + "        </span>"
            + "       <span class='blueImgBtn refreshClickCount' style='margin-left: 10px;'>刷新</span> "
            + "        <div style=\"float: right;\">"
            + "            <span class=\"whiteImgBtn realTimeBtn\">直通车监控</span>"
            + "            <span class=\"blueImgBtn countBtn\">直通车统计</span>"
            + "        </div>"
            + "        <div class=\"blank0\" style=\"height: 10px;\"></div>"

            + "        <select class=\"monitorSelect countSelect\">"
            + "            <option value=\"10\" selected=\"selected\">10天</option>"
            + "            <option value=\"20\">20天</option>"
            + "            <option value=\"30\">30天</option>"
            + "            <option value=\"40\">40天</option>"
            + "        </select>"
            + "        <div class=\"blank0\"></div>"
            + "    </div>"
            + "    <div class=\"DayChartDiv\">"
            + "    </div>"
            + "    </div>"
            + "    </div>";

        targetObj.html(html);
        targetObj.find(".ClickCountDiv").hide();
        //设置事件
        targetObj.find(".refreshRealTime").click(function() {
            BusMonitor.realTime.hourQuery();
        });
        targetObj.find(".refreshClickCount").click(function() {
            BusMonitor.clickCount.dayQuery();
        });

        BusMonitor.element.setRealTimeDivEvent(targetObj, targetObj.find(".RealTimeDiv"));
        BusMonitor.element.setClickCountDivEvent(targetObj, targetObj.find(".ClickCountDiv"));
    },
    //实时监控的tab
    setRealTimeDivEvent: function(targetObj, realTimeDivObj) {
        //按钮
        var countBtn = realTimeDivObj.find(".countBtn");
        countBtn.mouseover(function() {
            $(this).removeClass("whiteImgBtn");
            $(this).addClass("blueImgBtn");
        });
        countBtn.mouseout(function() {
            $(this).removeClass("blueImgBtn");
            $(this).addClass("whiteImgBtn");
        });
        countBtn.click(function() {
            BusMonitor.clickCount.show();
        });
        //下拉框
        var selectObj = realTimeDivObj.find(".realTimeSelect");
        selectObj.change(function() {
            BusMonitor.realTime.hourQuery();
        });
    },
    //直通车统计的tab
    setClickCountDivEvent: function(targetObj, countDivObj) {
        //按钮
        var realTimeBtn = countDivObj.find(".realTimeBtn");
        realTimeBtn.mouseover(function() {
            $(this).removeClass("whiteImgBtn");
            $(this).addClass("blueImgBtn");
        });
        realTimeBtn.mouseout(function() {
            $(this).removeClass("blueImgBtn");
            $(this).addClass("whiteImgBtn");
        });
        realTimeBtn.click(function() {
            BusMonitor.realTime.show();
        });
        //下拉框
        var selectObj = countDivObj.find(".countSelect");
        selectObj.change(function() {
            BusMonitor.clickCount.dayQuery();
        });
    }
}, BusMonitor.element);



/**
 * 直通车实时监控
 * @type {*}
 */
BusMonitor.realTime = BusMonitor.realTime || {};

BusMonitor.realTime = $.extend({
    show: function() {
        var targetObj = BusMonitor.targetObj;
        targetObj.find(".ClickCountDiv").hide();
        targetObj.find(".RealTimeDiv").show();
        BusMonitor.currentType = BusMonitor.monitorType.RealTime;
        BusMonitor.realTime.hourQuery();
    },
    hourQuery: function() {
        var data = {};
        var numIid = BusMonitor.targetNumIid;
        if (BusMonitor.init.isSingleItem())
            data.numIid = numIid;
        var dayNumStr = BusMonitor.targetObj.find(".realTimeSelect").val();
        data.dayNum =  parseInt(dayNumStr);
        $.ajax({
            url: '/busmonitor/busHourMonitor',
            dataType: 'json',
            type: 'post',
            data: data,
            error: function() {
                return;
            },
            success: function (json) {
                BusMonitor.realTime.createChart(json);
                Loading.init.hidden();
            }
        });
    },
    createChart: function(json) {
        var targetObj = BusMonitor.targetObj;
        var title = "直通车实时点击量监控";
        var xArray = ["0", "1","2", "3","4", "5","6", "7","8", "9",
            "10", "11","12", "13","14", "15","16", "17","18", "19",
            "20", "21","22", "23"
        ];
        var series = [];
        var dayNum = BusMonitor.util.getMapSize(json);
        for (var i = dayNum - 1; i >= 0; i--) {
            var lineValue = {};
            lineValue.data = [];
            var yArr = json[i];
            lineValue.data = yArr;
            if (i == 0)
                lineValue.name = "今日点击量";
            else if (i == 1)
                lineValue.name = "昨日点击量";
            else
                lineValue.name = i + "天前点击量";
            series[series.length] = lineValue;
        }
        BusMonitor.util.showChart(targetObj.find(".HourChartDiv"), xArray, series);
    }
}, BusMonitor.realTime);


/**
 * 直通车点击统计
 * @type {*}
 */
BusMonitor.clickCount = BusMonitor.clickCount || {};

BusMonitor.clickCount = $.extend({
    show: function() {
        var targetObj = BusMonitor.targetObj;
        targetObj.find(".RealTimeDiv").hide();
        targetObj.find(".ClickCountDiv").show();
        BusMonitor.currentType = BusMonitor.monitorType.ClickCount;
        BusMonitor.clickCount.dayQuery();
    },
    dayQuery: function() {
        var data = {};
        var numIid = BusMonitor.targetNumIid;
        if (BusMonitor.init.isSingleItem())
            data.numIid = numIid;
        var dayNumStr = BusMonitor.targetObj.find(".countSelect").val();
        var dayNum =  parseInt(dayNumStr);
        data.dayNum = dayNum;
        $.ajax({
            url: '/busmonitor/busDayMonitor',
            dataType: 'json',
            type: 'post',
            data: data,
            error: function() {
                return;
            },
            success: function (json) {
                BusMonitor.clickCount.createChart(json, dayNum);
                Loading.init.hidden();
            }
        });
    },
    createChart: function(json, dayNum) {
        var targetObj = BusMonitor.targetObj;
        var endDay = json.endDay;
        var year = parseInt(endDay.substring(0, 4));
        var month = parseInt(endDay.substring(4, 6));
        var day = parseInt(endDay.substring(6));
        var xArray = [];
        for (var i = 0; i < dayNum; i++) {
            var tempDay = BusMonitor.util.addDay(year, month, day, i + 1 - dayNum);
            xArray[xArray.length] = tempDay;
        }
        var series = [];
        var lineValue = {};
        lineValue.data = json.dayArr;
        series[series.length] = lineValue;
        BusMonitor.util.showChart(targetObj.find(".DayChartDiv"), xArray, series);
    }
}, BusMonitor.clickCount);


BusMonitor.util = BusMonitor.util || {};

BusMonitor.util = $.extend({
    getMapSize: function(jsonMap) {
        if (jsonMap === undefined || jsonMap == null)
            return 0;
        var size = 0;
        for (var key in jsonMap) {
            size++;
        }
        return size;
    },
    //在当前时间，加上dayNum天
    addDay: function(year, month, day, dayNum) {
        var myDate = new Date();
        myDate.setFullYear(year);
        myDate.setMonth(month);
        myDate.setDate(day + dayNum);
        return myDate.getDate();
    },
    showChart: function(charDivObj, xArray, series) {
        var chart = new Highcharts.Chart({
            chart: {
                renderTo: charDivObj[0]
            },
            title: {
                text: "",
                margin: 30,
                style: {
                    fontWeight: 'bold',
                    color: '#5128ff',
                    fontSize: '20px',
                    fontFamily: '微软雅黑'
                }
            },
            credits: {
                enabled: false
            },
            legend: {
                itemStyle: {
                    color: '#5452ff',
                    fontWeight: 'bold'
                },
                margin: 20
            },
            xAxis: {
                categories: xArray
            },
            yAxis: [{
                min: 0,
                title: {
                    text: '直通车点击量',
                    style: {
                        color: '#5128ff',
                        fontWeight: 'bold',
                        fontSize: '14px'
                    }
                },
                gridLineColor: '#a193ff'
            }],
            series: series
        });
    }
}, BusMonitor.util);