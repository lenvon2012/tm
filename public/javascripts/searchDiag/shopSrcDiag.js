var TM = TM || {};
((function ($, window) {


    TM.shopSrcDiag = TM.shopSrcDiag || {};

    var shopSrcDiag = TM.shopSrcDiag;

    shopSrcDiag.init = shopSrcDiag.init || {};
    shopSrcDiag.init = $.extend({
        doInit : function(container) {
            shopSrcDiag.init.Container = container;
            var interval = shopSrcDiag.util.getDaysBetween();
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
            shopSrcDiag.Event.setEvent(container);
        }
    }, shopSrcDiag.init);

    shopSrcDiag.Diag = shopSrcDiag.Diag || {};
    shopSrcDiag.Diag = $.extend({
        shopWordsDiag : function(targetDiv, interval, endTime) {
            if(interval == null) {
                interval = 7;
            }
            if(endTime == null) {
                endTime = new Date().getTime();
            }
            var table = shopSrcDiag.init.Container.find('.word-diag-result-table');

            $.post("/Diag/getPCShopSource", {interval : interval, endTime : endTime}, function(data){
                table.find('tbody').empty();
                if(data === undefined || data == null) {
                    table.find('tbody').append($('<tr class="no-data" style="height: 45px;"><td colspan="20">此时段暂无数据</td></tr>'));
                    return;
                }
                if(data.success != undefined && !data.success){
                    TM.Alert.load(data.message);
                    table.find('tbody').append($('<tr class="no-data" style="height: 45px;"><td colspan="20">'+data.message+'</td></tr>'));
                    return;
                }
                if(data.length == 0) {
                    table.find('tbody').append($('<tr class="no-data" style="height: 45px;"><td colspan="20">此时段暂无数据</td></tr>'));
                    return;
                }
                $(data).each(function(i, src){
                    var bounceCount, bounceRate;
                    if(src.bounce_count == "~") {
                        bounceCount = "~";
                        bounceRate = "~";
                    } else {
                        if(parseInt(src.bounce_count) > parseInt(src.pv)) {
                            bounceCount = src.pv;
                        } else {
                            bounceCount = src.bounce_count;
                        }
                    }
                    bounceRate = (bounceCount == "~") ? "~" : new Number(bounceCount/src.pv).toPercent(2);
                    var tranRate;
                    if(src.uv == "~" || parseInt(src.uv) == 0) {
                        tranRate = "0.00%";
                    } else {
                        tranRate = new Number(parseInt(src.alipay_trade_num)/parseInt(src.uv)).toPercent(2);
                    }
                    var redBoldClass = "", srcName = src.srcName, topLevelSrc = "";
                    if(parseInt(src.parentSrcId) == 0) {
                        redBoldClass = "redBoldClass";
                        topLevelSrc = "topLevelSrc";
                        srcName = "<span class='fold-class-wrapper'><img class='fold-class' src='/img/promoteimages/pic8.gif'>"+src.srcName+"</span>";
                        src.parentSrcName = "~"
                    }
                    table.find('tbody').append($('<tr parentsrcid="'+src.parentSrcId+'" srcid="'+src.srcId+'" class="word-tr '+topLevelSrc+'"><td class="'+redBoldClass+'">'+srcName+'</td><td>'+src.parentSrcName+'</td><td>'+src.uv+'</td><td>'+src.pv+'</td>' +
                        '<td>'+src.alipay_trade_num+'</td><td>'+src.alipay_auction_num+'</td><td>'+src.alipay_winner_num+'</td><td>'+new Number(src.alipay_trade_amt).toFixed(2)+'</td>' +
                        '<td>'+tranRate+'</td></tr>'));
                });
                table.find('.fold-class-wrapper').unbind("click").click(function(){
                    var img = $(this).find('img');
                    if(img.attr("src") == "/img/promoteimages/pic8.gif") {
                        img.attr("src", "/img/promoteimages/pic15.gif");
                        var srcId = $(this).parent().parent().attr("srcid");
                        table.find('tr[parentsrcid="'+srcId+'"]').hide();
                    } else {
                        img.attr("src", "/img/promoteimages/pic8.gif");
                        var srcId = $(this).parent().parent().attr("srcid");
                        table.find('tr[parentsrcid="'+srcId+'"]').show();
                    }
                });
            });

        },
        createItemDiagTable : function(){
            var table = shopSrcDiag.Diag.createTableHtml();

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
            var table = shopSrcDiag.init.Container.find('.word-diag-result-table');
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
    }, shopSrcDiag.Diag);

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

    shopSrcDiag.Event = shopSrcDiag.Event || {};
    shopSrcDiag.Event = $.extend({
        setEvent : function(container) {
            shopSrcDiag.Event.setIntervalEvent2();
            shopSrcDiag.Event.setOrderByEvent();
        },
        setOrderByEvent : function(){
            shopSrcDiag.init.Container.find('.word-diag-result-table-th .orderTd').click(function(){
                var interval = shopSrcDiag.util.getInterval2();
                var endTime = shopSrcDiag.util.getEndTime2();
                var orderBy = $(this).find('.sort').attr("sort");
                shopSrcDiag.Diag.shopWordsDiag(shopSrcDiag.init.Container.find('.diag-result-div'),
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
                shopSrcDiag.init.Container.find('.interval-tr .interval[value="0"]').trigger("click");
            });
            endTimeInput.unbind('change').change(function(){
                shopSrcDiag.init.Container.find('.interval-tr .interval[value="0"]').trigger("click");
            });
            shopSrcDiag.init.Container.find('.interval-tr .interval').unbind('click').click(function(){
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
                        endTime = curr - dayMillis;
                        interval = 7;
                        break;
                }
                shopSrcDiag.Diag.shopWordsDiag(shopSrcDiag.init.Container.find('.diag-result-div'),
                    interval, endTime);
            });
            shopSrcDiag.init.Container.find('.interval-tr .interval:checked').trigger('click');
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

                shopSrcDiag.Diag.shopWordsDiag(shopSrcDiag.init.Container.find('.diag-result-div'), interval, endTime);
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
    }, shopSrcDiag.Event);

    shopSrcDiag.util = shopSrcDiag.util || {};
    shopSrcDiag.util = $.extend({
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
            var value = parseInt(shopSrcDiag.init.Container.find('.interval-tr .interval:checked').val());
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
    }, shopSrcDiag.util);

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