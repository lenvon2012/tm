var TM = TM || {};
((function ($, window) {


    TM.shopSearchWords = TM.shopSearchWords || {};

    var shopSearchWords = TM.shopSearchWords;

    shopSearchWords.init = shopSearchWords.init || {};
    shopSearchWords.init = $.extend({
        doInit : function(container) {
            shopSearchWords.init.Container = container;
            var interval = shopSearchWords.util.getDaysBetween();
			if(interval < 1) {
				$('.searchDiagTip').show();
				return;
			} else if (interval < 3 ) {
				interval = 1;
			} else if (interval < 7) {
				interval = 3;
			} else {
				interval = 7;
			}
			$("." + interval + "-day").attr("checked", "checked");
            // 设置相关事件
            shopSearchWords.Event.setEvent(container);
        }
    }, shopSearchWords.init);

    shopSearchWords.Diag = shopSearchWords.Diag || {};
    shopSearchWords.Diag = $.extend({
        shopWordsDiag : function(targetDiv, interval, endTime, orderBy, isDesc) {
            if(interval == null) {
                interval = 7;
            }
            if(endTime == null) {
                endTime = new Date().getTime();
            }
            if(orderBy === undefined || orderBy == null) {
                orderBy = "impression";
            }

            var table = shopSearchWords.init.Container.find('.word-diag-result-table');
            targetDiv.parent().find('.word-diag-paging').tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: {interval:interval, endTime:endTime, orderBy : orderBy, isDesc:isDesc},
                    dataType: 'json',
                    url: '/Diag/shopWordsDiag',
                    callback: function(data){
                        if(data === undefined || data == null) {
                            return;
                        }
                        if(data.success == false) {
                            TM.Alert.load(data.message);
                            return;
                        }
                        if(data.res == null){
                            return;
                        }
                        table.find('tbody .word-tr').remove();
                        var totalUv = 0, totalTrade = 0;
                        if(data.res.length > 0) {
                            $(data.res).each(function(i, word){
                                var tranrate = parseInt(word.uv) == 0 ? "0.00%" : new Number(word.alipay_winner_num / word.uv).toPercent(2);
                                var clickrate = parseInt(word.pv) == 0 ? "0.00%" : new Number(word.click / word.pv).toPercent(2);
                                table.find('tbody').append($('<tr class="word-tr"><td class="word-td">'+word.word+'</td><td>'+word.pv+'</td><td>'+word.click+'</td><td>'+clickrate+'</td><td>'+word.uv+'</td>' +
                                    '<td>'+word.alipay_winner_num+'</td><td>'+word.alipay_auction_num+'</td><td>'+new Number(word.alipay_trade_amt).toFixed(2)+'</td><td>'+tranrate+'</td><td><span title="查看展现量趋势" class="inlineblock pvTrend"></span></td></tr>'))
                            });
                            table.find('tbody .pvTrend').unbind('click').click(function(){
                                var word = $(this).parent().parent().find('.word-td').text();
                                $.post("/Diag/shopWordTrend", {interval:interval, endTime:endTime, word:word}, function(data){
                                    if(data === undefined || data == null) {
                                        TM.Alert.load("查不到该关键词数据，请重试或联系客服");
                                    }
                                    shopSearchWords.util.drawChart(data, interval, word);
                                });
                            });
                        } else {
                            table.find('tr.no-data').remove();
                            table.find('tbody').append($('<tr class="no-data" style="height: 45px;"><td colspan="20">此时段暂无数据</td></tr>'));
                        }

                        if(data.msg != null) {
                            var totalAtt = data.msg.split(",");
                            var totalUv = parseInt(totalAtt[0]);
                            var totalTrade = parseInt(totalAtt[1]);
                            $('.search-trade-tranrate').text(totalUv == 0 ? "0%" : new Number(totalTrade / totalUv).toPercent(2));
                        } else {
                            $('.search-trade-tranrate').text("0.00%");
                        }

                    }
                }
            });


            targetDiv.append(table);

        },
        createItemDiagTable : function(){
            var table = shopSearchWords.Diag.createTableHtml();

            /*$.get("/Diag/getWordDiagIndos", function(data){
             if(data === undefined || data == null) {
             return;
             }
             if(data.res == null){
             return;
             }
             $(data.res).each(function(i, word){
             table.find('tbody').append($('<tr><td>'+word.word+'</td><td>'+word.searchRank+'</td><td>'+word.impression+'</td><td>'+word.aclick+'</td><td>'+new Number(word.ctr).toPercent(2)+'</td><td>'+word.pv+'</td><td>'+word.uv+'</td>' +
             '<td>'+word.deep+'</td><td>'+word.bounceRate+'</td><td>'+word.tradeUserCount+'</td><td>'+word.tradeCount+'</td><td>'+word.tradeAmount+'</td><td>'+word.tranrate+'</td></tr>'))
             });
             });*/
            return table;
        },
        createShopDiagTable : function(){
            var table = shopSearchWords.init.Container.find('.word-diag-result-table');
            $.get("/Diag/getWordDiagIndos", function(data){
                if(data === undefined || data == null) {
                    return;
                }
                if(data.res == null){
                    return;
                }
                $(data.res).each(function(i, word){
                    table.find('tbody').append($('<tr><td>'+word.word+'</td><td>'+word.searchRank+'</td><td>'+word.impression+'</td><td>'+word.aclick+'</td><td>'+new Number(word.ctr).toPercent(2)+'</td><td>'+word.pv+'</td><td>'+word.uv+'</td>' +
                        '<td>'+word.deep+'</td><td>'+word.bounceRate+'</td><td>'+word.tradeUserCount+'</td><td>'+word.tradeCount+'</td><td>'+word.tradeAmount+'</td><td>'+word.tranrate+'</td></tr>'))
                });
            });
            return table;
        },
        createTableHtml : function(){
            var html = '<table class="word-diag-result-table busSearch"><tbody>' +
                '<tr class="word-diag-result-table-th"><td rowspan="2">关键词</td><td colspan="3">展现数据</td><td colspan="4">引流数据</td><td colspan="4">转化数据(直接成交)</td></tr>' +
                '<tr class="word-diag-result-table-th"><td>展现量<span class="inlineblock sort Desc" sort="impression"></span></td><td>点击量<span class="inlineblock sort Desc" sort="click"></span></td><td>点击率</td><td>访客数<span class="inlineblock sort Desc" sort="uv"></td>' +
                '<td>成交人数<span class="inlineblock sort Desc" sort="alipay_winner_num"></span></td><td>成交件数<span class="inlineblock sort Desc" sort="alipay_trade_num"></span></td><td>成交金额<span class="inlineblock sort Desc" sort="alipay_trade_amt"></td><td>成交转化率<span class="inlineblock sort Desc" sort="alipay_auction_num"></td></tr>' +
                '</tbody></table>';
            return $(html);
        }

    }, shopSearchWords.Diag);

    //配置dateRangePicker插件
    var locale = {
        "format": 'YYYY-MM-DD',
        "separator": " - ",
        "applyLabel": "确定",
        "cancelLabel": "取消",
        "fromLabel": "起始时间",
        "toLabel": "结束时间'",
        "customRangeLabel": "自定义",
        "weekLabel": "W",
        "daysOfWeek": ["日", "一", "二", "三", "四", "五", "六"],
        "monthNames": ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
        "firstDay": 1,
    };

    shopSearchWords.Event = shopSearchWords.Event || {};
    shopSearchWords.Event = $.extend({
        setEvent : function(container) {
            shopSearchWords.Event.setIntervalEvent2();
            shopSearchWords.Event.setOrderByEvent();
        },
        setOrderByEvent : function(){
            shopSearchWords.init.Container.find('.word-diag-result-table-th .orderTd').click(function(){
                var interval = shopSearchWords.util.getInterval2();
                var endTime = shopSearchWords.util.getEndTime2();
                var orderBy = $(this).find('.sort').attr("sort");
                var isDesc = $(this).hasClass('desc');
                isDesc ? $(this).removeClass('desc') : $(this).addClass('desc')
                shopSearchWords.Diag.shopWordsDiag(shopSearchWords.init.Container.find('.diag-result-div'),
                    interval, endTime, orderBy, isDesc);
            });
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
                shopSearchWords.init.Container.find('.interval-tr .interval[value="0"]').trigger("click");
            });
            endTimeInput.unbind('change').change(function(){
                shopSearchWords.init.Container.find('.interval-tr .interval[value="0"]').trigger("click");
            });
            shopSearchWords.init.Container.find('.interval-tr .interval').unbind('click').click(function(){
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
                shopSearchWords.Diag.shopWordsDiag(shopSearchWords.init.Container.find('.diag-result-div'),
                    interval, endTime, 'pv', true);
            });
            shopSearchWords.init.Container.find('.interval-tr .interval:checked').trigger('click');
        },
        setIntervalEvent2 : function () {
            var dayMillis = 24 * 3600 * 1000;
            var date_range_picker = $("#date-range-picker");
            date_range_picker.unbind('change').change(function () {
                var endTime, interval;
                var startTimeInput, endTimeInput;
                startTimeInput = date_range_picker.val().split(locale.separator)[0];
                endTimeInput = date_range_picker.val().split(locale.separator)[1];

                endTime = parseInt(Date.parse(endTimeInput.toString().replace("-","/")));
                if(endTime > new Date().getTime()) {
                    TM.Alert.load("截止时间请勿超过当前时间");
                    return;
                }
                var startTime = parseInt(Date.parse(startTimeInput.toString().replace("-","/")));
                if(endTime < startTime) {
                    TM.Alert.load("截止时间请勿小于开始时间");
                    return;
                }
                interval = Math.floor((endTime - startTime) / dayMillis) + 1;

                shopSearchWords.Diag.shopWordsDiag(shopSearchWords.init.Container.find('.diag-result-div'),
                    interval, endTime, 'pv', true);
            });
            date_range_picker.daterangepicker({
                "locale": locale,
                "ranges" : {
                    '最近1天': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
                    '最近3天': [moment().subtract(3, 'days'), moment().subtract(1, 'days')],
                    '最近7天': [moment().subtract(7, 'days'), moment().subtract(1, 'days')],
                    '最近14天': [moment().subtract(14, 'days'), moment().subtract(1, 'days')]
                },
                "opens":"right",
                "startDate": moment().subtract(7, 'days'),//默认最近7天
                "endDate": moment().subtract(1, 'days'),//默认最近7天
                "maxDate": moment().subtract(1, 'days')
            })
        }
    }, shopSearchWords.Event);

    shopSearchWords.util = shopSearchWords.util || {};
    shopSearchWords.util = $.extend({
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
        getNumIidByHref : function(url)
        {
            var paraString = url.substring(url.indexOf("?")+1,url.length).split("&");
            var paraObj = {}
            for (i=0; j=paraString[i]; i++){
                paraObj[j.substring(0,j.indexOf("=")).toLowerCase()] = j.substring(j.indexOf("=")+1,j.length);
            }
            var returnValue = paraObj["id".toLowerCase()];
            if(typeof(returnValue)=="undefined"){
                return "";
            }else{
                return returnValue;
            }
        },
        getPlatform : function(){
            var target = shopSearchWords.init.Container.find('.shop-pc-online-info .opTabWrapper .selected').attr("target");
            switch (target){
                case "shop" :
                    return 0;
                case "pc" :
                    return 1;
                case "wireless" :
                    return 2;
                default :
                    return 0;
            }
        },
        getInterval : function(){
            var value = parseInt(shopSearchWords.init.Container.find('.interval-tr .interval:checked').val());
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
        getInterval2 : function () {
            var dayMillis = 24 * 3600 * 1000;
            var date_range_picker = $("#date-range-picker");
            var endTime, interval;
            var startTimeInput, endTimeInput;
            startTimeInput = date_range_picker.val().split(locale.separator)[0];
            endTimeInput = date_range_picker.val().split(locale.separator)[1];
            endTime = parseInt(Date.parse(endTimeInput.toString().replace("-","/")));

            if(endTime > new Date().getTime()) {
                TM.Alert.load("截止时间请勿超过当前时间");
                return;
            }
            var startTime = parseInt(Date.parse(startTimeInput.toString().replace("-","/")));
            if(endTime < startTime) {
                TM.Alert.load("截止时间请勿小于开始时间");
                return;
            }
            interval = Math.floor((endTime - startTime) / dayMillis) + 1;

            return interval;
        },
        getEndTime : function(){
            var value = parseInt(appShopSearchWords.init.Container.find('.interval-tr .interval:checked').val());
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
        getEndTime2 : function () {
            var dayMillis = 24 * 3600 * 1000;
            var date_range_picker = $("#date-range-picker");
            var endTime;
            var startTimeInput, endTimeInput;
            startTimeInput = date_range_picker.val().split(locale.separator)[0];
            endTimeInput = date_range_picker.val().split(locale.separator)[1];

            endTime = parseInt(Date.parse(endTimeInput.toString().replace("-","/")));
            if(endTime > new Date().getTime()) {
                TM.Alert.load("截止时间请勿超过当前时间");
                return;
            }
            var startTime = parseInt(Date.parse(startTimeInput.toString().replace("-","/")));
            if(endTime < startTime) {
                TM.Alert.load("截止时间请勿小于开始时间");
                return;
            }

            return endTime;
        },
        getDaysBetween : function(){
        	var hourMills = 3600 * 1000;
        	var dayMills = 24 * 3600 * 1000;
        	var timeZoneDiff = 8 * hourMills;
            var nowTime = new Date().getTime();
            var firstLoginTime = parseInt(TM.firstLoginTime);
            var limitTime = parseInt(((firstLoginTime + timeZoneDiff) / dayMills)) * dayMills  - timeZoneDiff + dayMills + timeZoneDiff;
            
            var interval = parseInt((nowTime - limitTime) / dayMills) + 1;

            return interval;
        },
        drawChart : function(pvMap, interval, word){
            var dayArr =  shopSearchWords.util.genDayArr(interval);
            var pvArray = shopSearchWords.util.genPvArr(interval, pvMap);
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
    }, shopSearchWords.util);

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