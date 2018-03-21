var TM = TM || {};
((function ($, window) {


    TM.shopEvent = TM.shopEvent || {};

    var shopEvent = TM.shopEvent;

    shopEvent.init = shopEvent.init || {};
    shopEvent.init = $.extend({
        doInit : function(container) {
            shopEvent.init.Container = container;
            var dayMills = 24* 3600 * 1000;
            if(new Date().getTime() - parseInt(TM.firstLoginTime) < 8 * dayMills) {
                $('.fourteen-day').hide();
            }
            // 检查用户是否进入未满一天
            if((new Date().getTime() - parseInt(TM.firstLoginTime)) < dayMills) {
                container.find('.searchDiagTip').show();
            }
            // 设置相关事件
            shopEvent.Event.setEvent(container);
        }
    }, shopEvent.init);

    shopEvent.Diag = shopEvent.Diag || {};
    shopEvent.Diag = $.extend({
        shopViewTrade : function(interval, endTime){
            if(interval == null) {
                interval = 7;
            }
            if(endTime == null) {
                endTime = new Date().getTime();
            }
            $.get("/Diag/shopTranrate", {platform : 0, interval: interval, endTime: endTime}, function(data){
                if(data === undefined || data == null) {
                    TM.Alert.load("未找到对应数据");
                }
                if(data.success == false) {
                    TM.Alert.load("未找到对应数据");
                }
                shopEvent.Diag.refreshShopHighCharts(data);
            });
        },
        refreshShopHighCharts : function(map){
            if(map === undefined || map == null) {
                return;
            }
            var tranrateArr = [], dayArr = [];
            $.each(map,function(key,values){
                dayArr.push(key);
                var tranrate = parseInt(values.pv) == 0 ? 0 : parseFloat(new Number(100 * parseInt(values.uv) / parseInt(values.pv)).toFixed(2));
                tranrateArr.push(tranrate);
            });
            chart = new Highcharts.Chart({
                chart : {
                    renderTo : 'shop-pv-uv-trade-trend-chart',
                    defaultSeriesType: 'line' //图表类型line(折线图)
                },
                credits : {
                    enabled: false   //右下角不显示LOGO
                },
                title: {
                    text: "店铺转化率趋势"
                }, //图表标题
                xAxis: {  //x轴
                    categories: dayArr, //x轴标签名称
                    gridLineWidth: 0, //设置网格宽度为1
                    lineWidth: 2,  //基线宽度
                    labels:{
                        y:20   //x轴标签位置：距X轴下方26像素
                        /*rotation: -45   //倾斜度*/
                    }
                },
                yAxis: {  //y轴
                    title: {text: '转化率数据'}, //标题
                    lineWidth: 0, //基线宽度
                    labels : {
                        formatter : function() {//设置纵坐标值的样式
                            return this.value + '%';
                        }
                    }
                },
                tooltip: {
                    enabled: true,
                    formatter: function() {
                        return "日期:<span style='margin:0 2px;'>" + this.x + "</span><br>" + "转化率:<span style='margin:0 2px;'>" + this.y+"</span>%";
                    }
                },
                plotOptions:{ //设置数据点
                    line:{
                        dataLabels:{
                            enabled:true  //在数据点上显示对应的数据值
                        },
                        enableMouseTracking: true //取消鼠标滑向触发提示框
                    }
                },
                series: [
                    {  //数据列
                        name: '转化率趋势',
                        data: tranrateArr,
                        yAxis:0,
                        dataLabels: {
                            enabled: true,
                            formatter: function () {
                                return this.y+"%";
                            }
                        }
                    }

                ]
            });
        }
    }, shopEvent.Diag);

    shopEvent.Event = shopEvent.Event || {};
    shopEvent.Event = $.extend({
        setEvent : function(container) {
            shopEvent.Event.setIntervalEvent();
        },
        setIntervalEvent : function(){
            var curr = new Date().getTime();
            var dayMillis = 24 * 3600 * 1000;
            var startTimeInput = $("#startTimeInput");
            var endTimeInput = $("#endTimeInput");
            startTimeInput.datepicker();
            startTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
            startTimeInput.val(new Date(curr - 7 * dayMillis).formatYMS());

            endTimeInput.datepicker();
            endTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
            endTimeInput.val(new Date(curr).formatYMS());

            startTimeInput.unbind('change').change(function(){
                shopEvent.init.Container.find('.interval-tr .interval[value="0"]').trigger("click");
            });
            endTimeInput.unbind('change').change(function(){
                shopEvent.init.Container.find('.interval-tr .interval[value="0"]').trigger("click");
            });
            shopEvent.init.Container.find('.interval-tr .interval').unbind('click').click(function(){
                var endTime, interval;
                var val = parseInt($(this).val());
                switch (val) {
                    case 1 :
                        endTime = curr - dayMillis;
                        interval = 1;
                        break;
                    case 3 :
                        endTime = curr - dayMillis;
                        interval = 3;
                        break;
                    case 7 :
                        endTime = curr - dayMillis;
                        interval = 7;
                        break;
                    case 14 :
                        endTime = curr - dayMillis;
                        interval = 14;
                        break;
                    case 0 :
                        if(endTimeInput.val() == new Date().formatYMS()) {
                            endTime = curr - dayMillis;
                        } else {
                            endTime = parseInt(Date.parse(endTimeInput.val().toString().replace("-","/")));
                        }


                        if(endTime > new Date().getTime()) {
                            TM.Alert.load("截止时间请勿超过当前时间");
                            endTimeInput.val(new Date(curr).formatYMS());
                            return;
                        }
                        var startTime = parseInt(Date.parse(startTimeInput.val().toString().replace("-","/")));
                        if(endTime < startTime) {
                            TM.Alert.load("截止时间请勿小于开始时间");
                            return;
                        }
                        interval = Math.floor((endTime - startTime) / dayMillis) + 1;
                        break;
                    default :
                        endTime = new Date().getTime();
                        interval = 7;
                        break;
                }
                shopEvent.Diag.shopViewTrade(interval, endTime);
            });
            shopEvent.init.Container.find('.interval-tr .interval:checked').trigger('click');
        }
    }, shopEvent.Event);

    shopEvent.util = shopEvent.util || {};
    shopEvent.util = $.extend({
        genDayArr : function(interval) {
            interval = parseInt(interval);
            var dayArr = [];
            var now = new Date().getTime();
            var dayMills = 24 *  3600 * 1000;
            for(var i = interval; i > 0; i--) {
                dayArr[interval - i] = new Date(now - i * dayMills).formatMS()
            }
            return dayArr;
        },
        getInterval : function(){
            var value = parseInt(shopEvent.init.Container.find('.interval-tr .interval:checked').val());
            var dayMillis = 24 * 3600 * 1000;
            switch (value) {
                case 1 :
                    return 1;
                case 3 :
                    return 3;
                case 7 :
                    return 7;
                case 14 :
                    return 14;
                case 0 :
                    var endTimeInput = $("#endTimeInput");
                    var startTimeInput = $("#startTimeInput");
                    var curr = new Date().getTime();
                    var endTime = parseInt(new Date(Date.parse(endTimeInput.val())).getTime());
                    if(endTime > new Date().getTime()) {
                        TM.Alert.load("截止时间请勿超过当前时间");
                        endTimeInput.val(new Date(curr).formatYMS());
                        return;
                    }
                    var startTime = parseInt(new Date(Date.parse(startTimeInput.val())).getTime());
                    if(endTime < startTime) {
                        TM.Alert.load("截止时间请勿小于开始时间");
                        return;
                    }
                    return Math.floor((endTime - startTime) / dayMillis) + 1;
                default :
                    return 7;
            }
        },
        getEndTime : function(){
            var value = parseInt(shopEvent.init.Container.find('.interval-tr .interval:checked').val());
            var curr = new Date().getTime();
            var dayMills = 24 * 3600 * 1000;
            switch (value) {
                case 1 :
                    return curr - dayMills;
                case 3 :
                    return curr - dayMills;
                case 7 :
                    return curr - dayMills;
                case 14 :
                    return curr - dayMills;
                case 0 :
                    var endTimeInput = $("#endTimeInput");
                    var endTime = parseInt(new Date(Date.parse(endTimeInput.val())).getTime());
                    if(endTime > new Date().getTime()) {
                        return curr;
                    } else {
                        return endTime;
                    }
                default :
                    return curr - dayMills;
            }
        },
        drawChart : function(pvMap, interval, word){
            var dayArr =  shopEvent.util.genDayArr(interval);
            var pvArray = shopEvent.util.genPvArr(interval, pvMap);
            var html = $('#pv-trend-chart-tmpl').clone();
            $('#pv-trend-chart').remove();
            html.attr("id", "pv-trend-chart");
            html.show();
            $('body').append(html);
            chart = new Highcharts.Chart({
                chart : {
                    renderTo : 'pv-trend-chart',
                    defaultSeriesType: 'line' //图表类型line(折线图)
                },
                credits : {
                    enabled: false   //右下角不显示LOGO
                },
                title: {
                    text: word
                }, //图表标题
                xAxis: {  //x轴
                    categories: dayArr, //x轴标签名称
                    gridLineWidth: 0, //设置网格宽度为1
                    lineWidth: 2,  //基线宽度
                    labels:{
                        y:20   //x轴标签位置：距X轴下方26像素
                        /*rotation: -45   //倾斜度*/
                    }
                },
                yAxis: [{  //y轴
                    title: {text: '展现量'}, //标题
                    lineWidth: 0 //基线宽度
                }],
                plotOptions:{ //设置数据点
                    line:{
                        dataLabels:{
                            enabled:true  //在数据点上显示对应的数据值
                        },
                        enableMouseTracking: true //取消鼠标滑向触发提示框
                    }
                },
                series: [
                    {  //数据列
                        name: '展现量趋势',
                        data: pvArray,
                        yAxis:0
                    }
                ]
            });
            TM.Alert.loadDetail(html, 1000, 530);
        },
        genDayArr : function(interval) {
            interval = parseInt(interval);
            var dayArr = [];
            var now = new Date().getTime();
            var dayMills = 24 *  3600 * 1000;
            for(var i = interval; i > 0; i--) {
                dayArr[interval - i] = new Date(now - i * dayMills).formatMS()
            }
            return dayArr;
        },
        genPvArr : function(interval, pvMap){
            interval = parseInt(interval);
            var pvArr = [];
            var now = new Date().getTime();
            var dayMills = 24 *  3600 * 1000;
            for(var i = interval; i > 0; i--) {
                var dayIndex = new Date(now - i * dayMills).formatNewYMS();
                pvArr[interval - i] = pvMap[dayIndex] == null ? 0  : parseInt(pvMap[dayIndex]);
            }
            return pvArr;
        }
    }, shopEvent.util);

    Date.prototype.format = function(format) //author: meizz
    {
        var o = {
            "M+" : this.getMonth()+1, //month
            "d+" : this.getDate(),    //day
            "h+" : this.getHours(),   //hour
            "m+" : this.getMinutes(), //minute
            "s+" : this.getSeconds(), //second
            "q+" : Math.floor((this.getMonth()+3)/3),  //quarter
            "S" : this.getMilliseconds() //millisecond
        }
        if(/(y+)/.test(format)) format=format.replace(RegExp.$1,
            (this.getFullYear()+"").substr(4 - RegExp.$1.length));
        for(var k in o)if(new RegExp("("+ k +")").test(format))
            format = format.replace(RegExp.$1,
                RegExp.$1.length==1 ? o[k] :
                    ("00"+ o[k]).substr((""+ o[k]).length));
        return format;
    }

    Date.prototype.formatYMS = function(){
        return this.format('yyyy-MM-dd');
    }
    Date.prototype.formatNewYMS = function(){
        return this.format('yyyyMMdd');
    }
    Date.prototype.formatMS = function(){
        return this.format('MM月dd');
    }
    Date.prototype.formatYMSHMS = function(){
        return this.format('yyyy-MM-dd hh:mm:ss');
    }
    Date.prototype.formatYMSH = function(){
        return this.format('yyyyMMddhh');
    }

})(jQuery,window));