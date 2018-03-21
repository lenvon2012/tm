var TM = TM || {};
((function ($, window) {


    TM.areaDiag = TM.areaDiag || {};

    var areaDiag = TM.areaDiag;

    var colorMap=['#FF0000','#E03E3E','#FF5200','#FF8500','#FF9900',
        '#FFB800','#FFD600','#FFF500','#E0FF00','#EEFC8B'];

    areaDiag.init = areaDiag.init || {};
    areaDiag.init = $.extend({
        doInit : function(container) {
            areaDiag.init.Container = container;
            var datMills = 24* 3600 * 1000;
            var interval = areaDiag.util.getDaysBetween();
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
            areaDiag.Event.setEvent(container);
        }
    }, areaDiag.init);

    areaDiag.Diag = areaDiag.Diag || {};
    areaDiag.Diag = $.extend({
        showMap : function(targetDiv, interval, endTime){
            if(interval == null) {
                interval = 7;
            }
            if(endTime == null) {
                endTime = new Date().getTime();
            }
            $.get("/Diag/getAllAreasViewsAndTrades", {interval:interval, endTime:endTime}, function(data){
                if(data === undefined || data == null) {
                    return;
                }
                if(data.success != undefined && !data.success){
                    return;
                }
                var dataStatus=[];
                $.each(data,function(key,values){
                    var tmp = '';
                    switch(parseInt(values.provinceId)){
                        case	267776: tmp = 'HAI'; break;// 海南
                        case	266752: tmp = 'JXI'; break;// 江西
                        case	264960: tmp = 'ANH'; break;// 安徽
                        case	270336: tmp = 'SHA'; break;// 陕西
                        case	264192: tmp = 'SCH'; break;// 四川
                        case	268032: tmp = 'HUB'; break;// 湖北
                        case	268288: tmp = 'HUN'; break;// 湖南
                        case	264704: tmp = 'NXA'; break;// 宁夏
                        case	269568: tmp = 'GUI'; break;// 贵州
                        case	269312: tmp = 'TIB'; break;// 西藏
                        case	269056: tmp = 'FUJ'; break;// 福建
                        case	262912: tmp = 'YUN'; break;// 云南
                        case	270592: tmp = 'QIH'; break;// 青海
                        case	268800: tmp = 'GAN'; break;// 甘肃
                        case	267264: tmp = 'HEN'; break;// 河南
                        case	265728: tmp = 'GUD'; break;// 广东
                        case	268544: tmp = 'MAC'; break;// 澳门
                        case	265984: tmp = 'GXI'; break;// 广西
                        case	263168: tmp = 'NMG'; break;// 内蒙古
                        case	269824: tmp = 'LIA'; break;// 辽宁
                        case	265472: tmp = 'SHX'; break;// 山西
                        case	267008: tmp = 'HEB'; break;// 河北
                        case	263936: tmp = 'JIL'; break;// 吉林
                        case	265216: tmp = 'SHD'; break;// 山东
                        case	266240: tmp = 'XIN'; break;// 新疆
                        case	263680: tmp = 'TAI'; break;// 台湾
                        case	271104: tmp = 'HLJ'; break;// 黑龙江
                        case	270848: tmp = 'HKG'; break;// 香港
                        case    263424: tmp = 'BEJ'; break;// 北京
                        case    266496: tmp = 'JSU'; break;// 江苏
                        case    267520: tmp = 'ZHJ'; break;// 浙江
                        case    262656: tmp = 'SHH'; break;// 上海
                        case    270080: tmp = 'CHQ'; break;// 重庆
                        case    264448: tmp = 'TAJ'; break;// 天津
                    }
                    var tranrate;
                    if(parseInt(values.uv) > 0) {
                        tranrate = new Number((parseInt(values.alipay_trade_num) / parseInt(values.uv))).toPercent(2)
                    } else {
                        tranrate = "0.00%";
                    }
                    var des= '<br />浏览数:<span>'+ values.pv +'</span>' +
                            '<br />访客数:<span>'+ values.uv +'</span>' +
                            '<br />支付宝付款订单数:<span>'+ values.alipay_trade_num +'</span>' +
                            '<br />转化率:<span>'+ tranrate +'</span>';
                    dataStatus.push({cha:tmp,name:values.provinceName,des:des});
                });
                $('#chinaMapDiv').empty();
                var mapContainer = $('<div class="mapContainer" style="width: 600px;height: 100%;margin: 0 auto;"></div>');
                mapContainer.vectorMap({ map: 'china_zh',
                    color: "#EDF0EC", //地图颜色
                    backgroundColor:"#fcfcfc",
                    onLabelShow: function (event, label, code) {//动态显示内容
                        $.each(dataStatus, function (i, items) {
                            if (code == items.cha) {
                                label.html(items.name + items.des);
                                label.css("position","absolute");
                                label.css('box-shadow','0 1px 1px #444');
                                label.css('border-radius','2px');
                                label.css('background-color','#DEF0E6');
                                label.css('padding','2px 2px 2px 2px');
                                label.css("font-size","12px");
                                label.css("font-weight","bold");
                            }

                        });
                    }
                });

                $.each(dataStatus, function (i, items) {
                    if(i<10){
                        if(items.cha.length>0){
                            var josnStr = "{" + items.cha + ":'"+colorMap[i]+"'}";
                            mapContainer.vectorMap('set', 'colors', eval('(' + josnStr + ')'));
                        }
                    }
                });
                $('#chinaMapDiv').append(mapContainer);
                $(window).trigger('resize');
            });
        },
        shopWordsDiag : function(targetDiv, interval, endTime, orderBy) {
            if(interval == null) {
                interval = 7;
            }
            if(endTime == null) {
                endTime = new Date().getTime();
            }
            if(orderBy === undefined || orderBy == null) {
                orderBy = "impression";
            }
            var table = areaDiag.init.Container.find('.word-diag-result-table');
            areaDiag.init.Container.find('.area-diag-pagging').tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: {interval:interval, endTime:endTime},
                    dataType: 'json',
                    url: '/Diag/getAreasViewsAndTradesPaging',
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
                                table.find('tbody').append($('<tr class="word-tr"><td class="word-td">'+word.regionName+'</td><td>'+word.pv+'</td><td>'+word.uv+'</td><td>'+word.view_repeat_num+'</td><td>'+word.alipay_trade_num+'</td>' +
                                    '<td>'+word.alipay_auction_num+'</td><td>'+new Number(word.alipay_trade_amt).toFixed(2)+'</td><td>'+word.alipay_winner_num+'</td></tr>'))
                            });
                        } else {
                            table.find('tr.no-data').remove();
                            table.find('tbody').append($('<tr class="no-data" style="height: 45px;"><td colspan="20">此时段暂无数据</td></tr>'));
                        }


                    }
                }
            });
            /*$.post("/Diag/getAreasViewsAndTrades", {interval:interval, endTime:endTime}, function(data){
                if(data === undefined || data == null) {
                    return;
                }
                if(data.success == false) {
                    TM.Alert.load(data.message);
                    return;
                }
                if(data == null){
                    return;
                }
                table.find('tbody .word-tr').remove();
                var totalUv = 0, totalTrade = 0;
                if(data.length > 0) {
                    $(data).each(function(i, word){
                        var tranrate = parseInt(word.uv) == 0 ? "0.00%" : new Number(word.alipay_winner_num / word.uv).toPercent(2);
                        table.find('tbody').append($('<tr class="word-tr"><td class="word-td">'+word.regionId+'</td><td>'+word.pv+'</td><td>'+word.uv+'</td><td>'+word.view_repeat_num+'</td><td>'+word.alipay_trade_num+'</td>' +
                            '<td>'+word.alipay_auction_num+'</td><td>'+word.alipay_trade_amt+'</td><td>'+word.alipay_winner_num+'</td></tr>'))
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
            });*/

            targetDiv.append(table);

        },
        createItemDiagTable : function(){
            var table = areaDiag.Diag.createTableHtml();

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
            var table = areaDiag.init.Container.find('.word-diag-result-table');
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
    }, areaDiag.Diag);

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

    areaDiag.Event = areaDiag.Event || {};
    areaDiag.Event = $.extend({
        setEvent : function(container) {
            areaDiag.Event.setIntervalEvent2();
            areaDiag.Event.setOrderByEvent();
        },
        setOrderByEvent : function(){
            areaDiag.init.Container.find('.word-diag-result-table-th .orderTd').click(function(){
                var interval = areaDiag.util.getInterval2();
                var endTime = areaDiag.util.getEndTime2();
                var orderBy = $(this).find('.sort').attr("sort");
                areaDiag.Diag.shopWordsDiag(areaDiag.init.Container.find('.diag-result-div'),
                    interval, endTime, orderBy);
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
                areaDiag.init.Container.find('.interval-tr .interval[value="0"]').trigger("click");
            });
            endTimeInput.unbind('change').change(function(){
                areaDiag.init.Container.find('.interval-tr .interval[value="0"]').trigger("click");
            });
            areaDiag.init.Container.find('.interval-tr .interval').unbind('click').click(function(){
                var endTime, interval;
                var val = parseInt($(this).val());
                switch (val) {
	                case 1 :
	                    endTime = new Date().getTime();
	                    interval = 1;
	                    break;
                    case 3 :
                        endTime = new Date().getTime();
                        interval = 3;
                        break;
                    case 7 :
                        endTime = new Date().getTime();
                        interval = 7;
                        break;
                    case 14 :
                        endTime = new Date().getTime();
                        interval = 14;
                        break;
                    case 0 :
                        endTime = parseInt(Date.parse(endTimeInput.val().toString().replace("-","/")));
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
                // 生成表格
                areaDiag.Diag.shopWordsDiag(areaDiag.init.Container.find('.diag-result-div'),
                    interval, endTime);
                // 生成地图
                areaDiag.Diag.showMap(areaDiag.init.Container.find('#chinaMapDiv'),
                    interval, endTime)
            });
            areaDiag.init.Container.find('.interval-tr .interval:checked').trigger('click');
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

                // 生成表格
                areaDiag.Diag.shopWordsDiag(areaDiag.init.Container.find('.diag-result-div'),
                    interval, endTime);
                // 生成地图
                areaDiag.Diag.showMap(areaDiag.init.Container.find('#chinaMapDiv'),
                    interval, endTime)
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
    }, areaDiag.Event);

    areaDiag.util = areaDiag.util || {};
    areaDiag.util = $.extend({
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
        getInterval : function(){
            var value = parseInt(areaDiag.init.Container.find('.interval-tr .interval:checked').val());
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
        }
    }, areaDiag.util);

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