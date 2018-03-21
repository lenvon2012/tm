var TM = TM || {};
((function ($, window) {


    TM.shopViewTrade = TM.shopViewTrade || {};

    var shopViewTrade = TM.shopViewTrade;

    shopViewTrade.init = shopViewTrade.init || {};
    shopViewTrade.init = $.extend({
        doInit : function(container) {
            shopViewTrade.init.Container = container;
            var interval = shopViewTrade.util.getDaysBetween();
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
            shopViewTrade.Event.setEvent(container);
        }
    }, shopViewTrade.init);

    shopViewTrade.Diag = shopViewTrade.Diag || {};
    shopViewTrade.Diag = $.extend({

        createItemDiagTable : function(){
            var table = shopViewTrade.Diag.createTableHtml();

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
            var table = shopViewTrade.init.Container.find('.word-diag-result-table');
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
        },
        shopViewTrade : function(platform, interval, endTime){
            // 默认全店
            if(platform === undefined || platform == null) {
                platform = 0;
            }
            if(interval == null) {
                interval = 7;
            }
            if(endTime == null) {
                endTime = new Date().getTime();
            }
            $.get("/Diag/shopPCWirelessViewTrade", {platform : platform, interval: interval, endTime: endTime}, function(data){
                if(data === undefined || data == null) {
                    shopViewTrade.Diag.refreshShopViewTradeInfo();
                }
                if(data.success == false) {
                    TM.Alert.load(data.message);
                    shopViewTrade.Diag.refreshShopViewTradeInfo();
                    return;
                }
                var viewTrade = data[0];
                shopViewTrade.Diag.setShopViewTradeInfo(viewTrade);
                $.get("/Diag/getShopPCBounceCount", {interval: 1, endTime: new Date().getTime() - 24 * 3600 * 1000}, function(bounceCount){
                    if(bounceCount === undefined || bounceCount == null) {
                        shopViewTrade.Diag.setBounceRate("0.00%");
                        return;
                    }
                    if(bounceCount.success == false) {
                        shopViewTrade.Diag.setBounceRate("0.00%");
                        return;
                    }
                    var boucceRate = "0.00%";
                    if(viewTrade === undefined || viewTrade == null) {

                    } else {
                        boucceRate = parseInt(viewTrade.pv) == 0 ? 0 : new Number(parseInt(bounceCount) * 1.0 / parseInt(viewTrade.pv)).toPercent(2);
                    }
                    shopViewTrade.Diag.setBounceRate(boucceRate);
                });
                shopViewTrade.Diag.refreshShopHighCharts(data[1]);
            });
        },
        refreshShopViewTradeInfo : function(){
            shopViewTrade.init.Container.find('.shop-pc-online-info .shop-pc-online-info-table .key-value-tr .value').text("0");
        },
        setShopViewTradeInfo : function(data){
            if(data === undefined || data == null) {
                return;
            }
            if(data.success == false) {
                return;
            }
            shopViewTrade.init.Container.find('.shop-pc-online-info .shop-pc-online-info-table div.pv').text(data.pv);
            shopViewTrade.init.Container.find('.shop-pc-online-info .shop-pc-online-info-table div.uv').text(data.uv);
            shopViewTrade.init.Container.find('.shop-pc-online-info .shop-pc-online-info-table div.viewRepeat').text(data.view_repeat_num);
            shopViewTrade.init.Container.find('.shop-pc-online-info .shop-pc-online-info-table div.tradeRepeat').text(data.trade_repeat_num);
            shopViewTrade.init.Container.find('.shop-pc-online-info .shop-pc-online-info-table div.alipayTradeNum').text(data.alipay_trade_num);
            shopViewTrade.init.Container.find('.shop-pc-online-info .shop-pc-online-info-table div.alipayItemNum').text(data.alipay_auction_num);
            shopViewTrade.init.Container.find('.shop-pc-online-info .shop-pc-online-info-table div.alipayTradeAmount').text(Math.floor(data.alipay_trade_amt));
            shopViewTrade.init.Container.find('.shop-pc-online-info .shop-pc-online-info-table div.alipayUserNum').text(data.alipay_winner_num);
            var accessDeepth = parseInt(data.uv) > 0 ? new Number(data.pv / data.uv).toFixed(2) : 0;
            var tradeRate = parseInt(data.uv) > 0 ? new Number(data.alipay_winner_num / data.uv).toPercent(2) : "0.00%";
            var viewRepeatRate = parseInt(data.uv) > 0 ? new Number(data.view_repeat_num / data.uv).toPercent(2) : "0.00%";
            $('.shop-pc-online-info-table div.accessDeepth').text(accessDeepth);
            $('.shop-pc-online-info-table div.tradeRate').text(tradeRate);
            $('.shop-pc-online-info-table div.viewRepeatRate').text(viewRepeatRate);
        },
        setBounceRate : function(bounceRate){
            $('.shop-pc-online-info-table div.bounceRate').text(bounceRate);
        },
        refreshShopHighCharts : function(map){
            if(map === undefined || map == null) {
                return;
            }
            var pvArr = [], uvArr = [], tradeNumArr = [], tradeAmountArr = [], dayArr = [], tranRateArr = [], searchTranRateArr = [];
            $.each(map,function(key,values){
                if(values === undefined || values == null) {
                    // 啥都不做
                } else {
                    dayArr.push(key)
                    pvArr.push(parseInt(values.pv));
                    uvArr.push(parseInt(values.uv));
                    tradeNumArr.push(parseInt(values.alipay_trade_num));
                    tradeAmountArr.push(parseFloat(new Number(values.alipay_trade_amt).toFixed(2)));
                    var tranRate = parseInt(values.uv) == 0 ? 0.00 : parseFloat(new Number(values.alipay_winner_num / values.uv).toFixed(4));
                    tranRateArr.push(tranRate);
                    var searchTranRate = parseInt(values.searchUv) == 0 ? 0.00 : parseFloat(new Number(values.search_alipay_winner_num / values.searchUv).toFixed(4));
                    searchTranRateArr.push(searchTranRate);
                }

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
                    text: "店铺流量销量趋势"
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
                        title: {text: '流量销量数据'}, //标题
                        lineWidth: 0 //基线宽度
                    },
                    {
                        title: {text: '成交转化率数据'}, //标题
                        lineWidth: 0, //基线宽度
                        opposite:true
                    }
                ],
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
                        name: '浏览量趋势',
                        data: pvArr,
                        yAxis:0
                    },
                    {  //数据列
                        name: '访客数趋势',
                        data: uvArr,
                        yAxis:0
                    },
                    {  //数据列
                        name: '支付宝成交订单数趋势',
                        data: tradeNumArr,
                        yAxis:0
                    },
                    {  //数据列
                        name: '支付宝成交金额趋势',
                        data: tradeAmountArr,
                        yAxis:0
                    },
                    {  //数据列
                        name: '成交转化率',
                        data: tranRateArr,
                        yAxis:1
                    },
                    {  //数据列
                        name: '站内搜索转化率',
                        data: searchTranRateArr,
                        yAxis:1
                    }
                ]
            });
        },
        initTable: function(interval, endTime){
			// 初始化表格
			$.ajax({
				type:"GET",
				url:"/Diag/shopView",
				data:{
					interval: interval,
					endTime: endTime
				},
				success:function(data){
					if(data.success != undefined && !data.success){
						TM.Alert.load(data.message);
						return;
					}
					var keyArr = new Array();
					$.each(data, function(key, values){
						keyArr.push(key);
					});
					$('#view_item_show').empty();
					for(var i = keyArr.length; i>0; i--){
						var keyVal = keyArr[i-1];
						var values = data[keyVal];
						var itemCollectionRate = values.uv == 0 ? '0.00%' : new Number(values.itemCollectNum / values.uv).toPercent(2);
						var itemCartRate = values.uv == 0 ? '0.00%' : new Number(values.itemCartNum / values.uv).toPercent(2);
						var html = '<tr class="app-word-diag-result-table-th">' +
							'<td>'+ values.dataTime +'</td>' +
							'<td>'+ values.pv +'</td>' +
							'<td>'+ values.uv +'</td>' +
							'<td>'+ Math.round(values.alipayTradeAmt * 100)/100 +'</td>' +
							'<td>'+ values.alipayAuctionNum +'</td>' +
							'<td>'+ values.alipayTradeNum +'</td>' +
							'<td>'+ values.tradeRate +'</td>' +
							'<td>'+ values.entranceNum +'</td>' +
							'<td>'+ values.itemCollectNum +'</td>' +
							'<td>'+ itemCollectionRate +'</td>' +
							'<td>'+ values.itemCartNum +'</td>' +
							'<td>'+ itemCartRate +'</td>' +
							'<td>'+ values.searchUv +'</td>' +
							'<td>'+ values.pcUv +'</td>' +
							'</tr>';
						$('#view_item_show').append(html);
					}
				}
			});
		}
    }, shopViewTrade.Diag);

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

    shopViewTrade.Event = shopViewTrade.Event || {};
    shopViewTrade.Event = $.extend({
        setEvent : function(container) {
            shopViewTrade.Event.setOpTabClickEvent(container);
            shopViewTrade.Event.setIntervalEvent2();
        },
        setOpTabClickEvent : function(container) {
            container.find('.opTabWrapper .opTab').unbind('click').click(function(){
                if($(this).hasClass("selected")) {
                    return;
                }
                container.find('.opTabWrapper .selected').removeClass("selected");
                $(this).addClass("selected");
                var target = $(this).attr("target");
                var interval = shopViewTrade.util.getInterval2();
                var endTime = shopViewTrade.util.getEndTime2();
                switch (target){
                    case "shop" :
                        shopViewTrade.Diag.shopViewTrade(0, interval, endTime);break;
                        shopViewTrade.Diag.initTable(interval, endTime);
                    case "pc" :
                        shopViewTrade.Diag.shopViewTrade(1, interval, endTime);break;
                    case "wireless" :
                        shopViewTrade.Diag.shopViewTrade(2, interval, endTime);break;
                    default :
                        shopViewTrade.Diag.shopViewTrade(0, interval, endTime);break;
                }
            });
            container.find('.opTabWrapper .opTab').eq(0).trigger("click");
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
                shopViewTrade.init.Container.find('.interval-tr .interval[value="0"]').trigger("click");
            });
            endTimeInput.unbind('change').change(function(){
                shopViewTrade.init.Container.find('.interval-tr .interval[value="0"]').trigger("click");
            });
            shopViewTrade.init.Container.find('.interval-tr .interval').unbind('click').click(function(){
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
                var platform = shopViewTrade.util.getPlatform();
                shopViewTrade.Diag.shopViewTrade(platform, interval, endTime);
                shopViewTrade.Diag.initTable(interval, endTime);
            });
            shopViewTrade.init.Container.find('.interval-tr .interval:checked').trigger('click');
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

                var platform = shopViewTrade.util.getPlatform();
                shopViewTrade.Diag.shopViewTrade(platform, interval, endTime);
                shopViewTrade.Diag.initTable(interval, endTime);
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
    }, shopViewTrade.Event);

    shopViewTrade.util = shopViewTrade.util || {};
    shopViewTrade.util = $.extend({
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
            var target = shopViewTrade.init.Container.find('.shop-pc-online-info .opTabWrapper .selected').attr("target");
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
            var value = parseInt(shopViewTrade.init.Container.find('.interval-tr .interval:checked').val());
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
            var value = parseInt(shopViewTrade.init.Container.find('.interval-tr .interval:checked').val());
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
            var dayArr =  shopViewTrade.util.genDayArr(interval);
            var pvArray = shopViewTrade.util.genPvArr(interval, pvMap);
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
    }, shopViewTrade.util);

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
    Date.prototype.formatYMDM = function () {
        return this.format("yyyy/MM/dd hh:mm")
    }

})(jQuery,window));