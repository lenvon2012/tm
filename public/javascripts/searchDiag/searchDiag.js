var TM = TM || {};
((function ($, window) {


    TM.searchDiag = TM.searchDiag || {};

    var searchDiag = TM.searchDiag;

    searchDiag.init = searchDiag.init || {};
    searchDiag.init = $.extend({
        doInit : function(container) {
            searchDiag.init.Container = container;
            var interval = searchDiag.util.getDaysBetween();
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
			// $("." + interval + "-day").attr("checked", "checked");
            // 暂时隐藏店铺诊断信息，等待御膳房商家接口权限
            // 诊断日期
            container.find('.diag-date').html(new Date(new Date().getTime() - 24 * 3600 * 1000).formatYMS());
            // 先生成uv与转化率数组
            searchDiag.init.genFirstInUVAndTranrate(interval, new Date().getTime());
            //

            // 生成最近诊断过的宝贝列表
            searchDiag.init.initRecentlyDiagedItems();
            // 设置相关事件
            searchDiag.Event.setEvent(container, interval);
        },
        initRecentlyDiagedItems : function(){
            $.post("/Diag/getRecentlyDiagedItems", function(data){
                if(data === undefined || data ==  null) {
                    return;
                }
                if(data.length > 0) {
                    $(data).each(function(i, item){
                        $('.recently-diaged-item-images').append($('<img numIid="'+item.numIid+'" class="recently-item" src="'+item.picPath+'" />'));
                    });
                    $('.recently-diaged-item-images img').click(function(){
                        var numIid = $(this).attr("numIid");
                        var interval = searchDiag.util.getInterval2();
                        var endTime = searchDiag.util.getEndTime2();
                        searchDiag.Diag.itemDiag( searchDiag.init.Container.find('.diag-result-div'), numIid, interval, endTime);
                    });
                } else {
                    $('.recently-diaged-item-images').html("暂无最近诊断宝贝");
                }


            });
        },
        genFirstInUVAndTranrate : function(interval, endTime){
            var uvArr = [];
            interval = parseInt(interval);
            $.post("/Diag/lastWeekUV",{interval:interval, endTime:endTime}, function(data){
                if(data === undefined || data == null) {
                    uvArr = [0,0,0,0,0,0,0];
                } else {
                    $.each(data,function(key,values){
                        if(parseInt(key) <= interval) {
                            if(values == null) {
                                uvArr[interval - parseInt(key)] = 0;
                            } else {
                                uvArr[interval - parseInt(key)] = parseInt(values);
                            }
                        }

                    });
                }
                // 生成昨日店铺UV 数据
                searchDiag.init.genYesterDayUv(uvArr, interval);
                // 生成UV表格
                searchDiag.init.genUVChart(uvArr, interval);

                // 在获取uvArr的基础上，再获取成交量，构造转化率图表
                var tranRateArr = [];
                $.post("/Diag/lastWeekTrade", {interval:interval, endTime:endTime}, function(data){
                    if(data === undefined || data == null) {
                        tranRateArr = [0,0,0,0,0,0,0];
                    } else {
                        $.each(data,function(key,values){
                            if(values == null) {
                                tranRateArr[interval - parseInt(key)] = 0;
                            } else {
                                var tranrate = uvArr[interval - parseInt(key)] == 0 ? 0 : parseInt(values) / uvArr[interval - parseInt(key)];
                                tranRateArr[interval - parseInt(key)] = tranrate;
                            }
                        });
                    }
                    // 生成昨日店铺转化率数据
                    searchDiag.init.genYesterDayTrarate(tranRateArr, interval);
                    // 生成tranrate表格
                    searchDiag.init.genTranrateChart(tranRateArr, interval);
                });
            });
        },
        genUVAndTranrate : function(interval, endTime){
            var uvArr = [];
            interval = parseInt(interval);
            $.post("/Diag/lastWeekUV",{interval:interval, endTime:endTime}, function(data){
                if(data === undefined || data == null) {
                    uvArr = [0,0,0,0,0,0,0];
                } else {
                    $.each(data,function(key,values){
                        if(parseInt(key) <= interval) {
                            if(values == null) {
                                uvArr[interval - parseInt(key)] = 0;
                            } else {
                                uvArr[interval - parseInt(key)] = parseInt(values);
                            }
                        }

                    });
                }
                // 生成昨日店铺UV 数据
                //searchDiag.init.genYesterDayUv(uvArr, interval);
                // 生成UV表格
                searchDiag.init.genUVChart(uvArr, interval);

                // 在获取uvArr的基础上，再获取成交量，构造转化率图表
                var tranRateArr = [];
                $.post("/Diag/lastWeekTrade", {interval:interval, endTime:endTime}, function(data){
                    if(data === undefined || data == null) {
                        tranRateArr = [0,0,0,0,0,0,0];
                    } else {
                        $.each(data,function(key,values){
                            if(values == null) {
                                tranRateArr[interval - parseInt(key)] = 0;
                            } else {
                                var tranrate = uvArr[interval - parseInt(key)] == 0 ? 0 : parseInt(values) / uvArr[interval - parseInt(key)];
                                tranRateArr[interval - parseInt(key)] = tranrate;
                            }
                        });
                    }
                    // 生成昨日店铺转化率数据
                    //searchDiag.init.genYesterDayTrarate(tranRateArr, interval);
                    // 生成tranrate表格
                    searchDiag.init.genTranrateChart(tranRateArr, interval);
                });
            });
        },
        genYesterDayUv : function(uvArr, interval){
            interval = parseInt(interval);
            var yesterdayUv = parseInt(uvArr[interval - 1]);
            var beforeYesterdayUv = parseInt(uvArr[interval - 2]);
            $('.shop-diag-uv').text(yesterdayUv);
            if(parseInt(beforeYesterdayUv) <= 0 && parseInt(yesterdayUv) <= 0) {
                $('.uv-up-down-percent').text(0);
            } else if(parseInt(beforeYesterdayUv) <= 0) {
                $('.uv-up-down-percent').text("∞");
            } else if(parseInt(yesterdayUv) <= 0) {
                $('.uv-up-or-down').removeClass("up");
                $('.uv-up-or-down').addClass("down");
                $('.uv-up-down-percent').text("∞");
            } else {
                if(beforeYesterdayUv > yesterdayUv) {
                    $('.uv-up-or-down').removeClass("up");
                    $('.uv-up-or-down').addClass("down");
                    var downPercent = new Number((beforeYesterdayUv - yesterdayUv) / beforeYesterdayUv).toPercent(2);
                    $('.uv-up-down-percent').text(downPercent);
                } else {
                    var downPercent = new Number((yesterdayUv - beforeYesterdayUv) / beforeYesterdayUv).toPercent(2);
                    $('.uv-up-down-percent').text(downPercent);
                }
            }

        },
        genYesterDayTrarate : function(tranRateArr, interval){
            interval = parseInt(interval);
            var yesterdayUv = parseFloat(tranRateArr[interval - 1]);
            var beforeYesterdayUv = parseFloat(tranRateArr[interval - 2]);
            $('.shop-diag-tranrate').text(new Number(yesterdayUv).toPercent(2));
            if(beforeYesterdayUv <= 0.0 && yesterdayUv <= 0.0) {
                $('.tranrate-up-down-percent').text(0);
            } else if(beforeYesterdayUv <= 0.0) {
                $('.tranrate-up-down-percent').text("∞");
            } else if(yesterdayUv <= 0.0) {
                $('.tranrate-up-or-down').removeClass("up");
                $('.tranrate-up-or-down').addClass("down");
                $('.tranrate-up-down-percent').text("∞");
            } else {
                if(beforeYesterdayUv > yesterdayUv) {
                    $('.tranrate-up-or-down').removeClass("up");
                    $('.tranrate-up-or-down').addClass("down");
                    var downPercent = new Number((beforeYesterdayUv - yesterdayUv) / beforeYesterdayUv).toPercent(2);
                    $('.tranrate-up-down-percent').text(downPercent);
                } else {
                    var downPercent = new Number((yesterdayUv - beforeYesterdayUv) / beforeYesterdayUv).toPercent(2);
                    $('.tranrate-up-down-percent').text(downPercent);
                }
            }

        },
        genUVChart : function(uvArray, interval){
            var dayArr =  searchDiag.util.genDayArr(interval);
            chart = new Highcharts.Chart({
                chart : {
                    renderTo : 'uv-chart',
                    defaultSeriesType: 'line' //图表类型line(折线图)
                },
                credits : {
                    enabled: false   //右下角不显示LOGO
                },
                title: {
                    text: 'UV趋势'
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
                    title: {text: 'UV'}, //标题
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
                        name: 'UV',
                        data: uvArray,
                        yAxis:0
                    }
                ]
            });
        },
        genTranrateChart : function(tranrateArray, interval){
            var dayArr =  searchDiag.util.genDayArr(interval);
            chart = new Highcharts.Chart({
                chart : {
                    renderTo : 'tranrate-chart',
                    defaultSeriesType: 'line' //图表类型line(折线图)
                },
                credits : {
                    enabled: false   //右下角不显示LOGO
                },
                title: {
                    text: '转化率趋势'
                }, //图表标题
                xAxis: {  //x轴
                    categories: dayArr, //x轴标签名称
                    gridLineWidth: 0, //设置网格宽度为1
                    lineWidth: 2,  //基线宽度
                    labels:{
                        y:20   //x轴标签位置：距X轴下方26像素
                        /*rotation: -45//倾斜度*/
                    }

                },
                yAxis: [{  //y轴
                    title: {text: '转化率'}, //标题
                    lineWidth: 0 //基线宽度
                }],
                plotOptions:{ //设置数据点
                    line:{
                        dataLabels:{
                            enabled:false  //在数据点上显示对应的数据值
                        },
                        enableMouseTracking: true //取消鼠标滑向触发提示框
                    }
                },
                series: [
                    {  //数据列
                        name: '转化率',
                        data: tranrateArray,
                        yAxis:0
                    }
                ],
                tooltip:{
                    formatter: function() {  //格式化提示框的内容样式
                        /*return '<b>'+ this.series.name +'</b><br/>'+
                            this.x +': '+ this.y +'°C';*/
                        return new Number(this.y).toPercent(2);
                    }
                }
            });
        }
    }, searchDiag.init);

    searchDiag.Diag = searchDiag.Diag || {};
    searchDiag.Diag = $.extend({
        genLastWeekUV : function(){
            $.post("/Diag/lastWeekUV", function(data){
                if(data === undefined || data == null) {
                    return uvArr;
                }
                var uvArr = [];
                $.each(data,function(key,values){
                    if(values == null) {
                        uvArr[parseInt(key) - 1] = null;
                    } else {
                        uvArr[parseInt(key) - 1] = parseInt(values);
                    }
                });
                return uvArr;
            });

        },
        itemDiag : function(targetDiv, numIid, interval, endTime, orderBy) {
            if(parseInt(numIid) != parseInt(searchDiag.init.Container.find('.diaging-item-href').attr("numIid"))) {
                $.get("/Diag/getItemInfo",{numIid:numIid}, function(data){
                    if(data === undefined || data == null) {
                        TM.Alert.load("宝贝诊断出错,请重试或联系客服");
                        return;
                    }
                    if(data.success == false) {
                        TM.Alert.load(data.message);
                        return;
                    }
                    var href = "http://item.taobao.com/item.htm?id=" + data.id;
                    searchDiag.init.Container.find('.diaging-item-href').attr("href", href);
                    searchDiag.init.Container.find('.diaging-item-href').attr("numIid", numIid);
                    searchDiag.init.Container.find('.diaging-item-href img').attr("src", data.picURL);
                    searchDiag.init.Container.find('.item-title-content').text(data.name);
                    searchDiag.init.Container.find('#to-diag-item-href').val(href);
                });
            }

            searchDiag.Diag.itemWordDiag(targetDiv, numIid, interval, endTime, orderBy);
        },
        itemWordDiag : function(targetDiv, numIid, interval, endTime, orderBy) {
            if(interval == null) {
                interval = 7;
            }
            if(endTime == null) {
                endTime = new Date().getTime();
            }
            if(orderBy === undefined || orderBy == null) {
                orderBy = "impression";
            }
            //targetDiv.empty();
            //var table = searchDiag.Diag.createTableHtml();
            var table = searchDiag.init.Container.find('.word-diag-result-table');
            targetDiv.parent().find('.word-diag-paging').tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: {numIid:numIid, interval:interval, endTime:endTime, orderBy : orderBy},
                    dataType: 'json',
                    url: '/Diag/diagItem',
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
                                table.find('tr.no-data').remove();
                                var tranrate = parseInt(word.uv) == 0 ? "0.00%" : new Number(word.alipay_winner_num / word.uv).toPercent(2);
                                var clickRate = parseInt(word.pv) == 0 ? "0.00%" : new Number(word.click / word.pv).toPercent(2);
                                table.find('tbody').append($('<tr class="word-tr"><td class="word-td">'+word.word+'</td><td>'+word.pv+'</td><td>'+word.click+'</td><td>'+clickRate+'</td><td>'+word.uv+'</td>' +
                                    '<td>'+word.alipay_winner_num+'</td><td>'+word.alipay_auction_num+'</td><td>'+new Number(word.alipay_trade_amt).toFixed(2)+'</td><td>'+tranrate+'</td></tr>'))
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
        shopDiag : function(targetDiv) {
            targetDiv.empty();
            var table = searchDiag.Diag.createTableHtml();
            targetDiv.parent().find('.word-diag-paging').tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: "",
                    dataType: 'json',
                    url: '/Diag/getWordDiagIndos',
                    callback: function(data){
                        if(data === undefined || data == null) {
                            return;
                        }
                        if(data.res == null){
                            return;
                        }
                        table.find('tbody .word-tr').remove();
                        $(data.res).each(function(i, word){
                            table.find('tbody').append($('<tr class="word-tr"><td>'+word.word+'</td><td>'+word.searchRank+'</td><td>'+word.impression+'</td><td>'+word.aclick+'</td><td>'+new Number(word.ctr).toPercent(2)+'</td><td>'+word.pv+'</td><td>'+word.uv+'</td>' +
                                '<td>'+word.deep+'</td><td>'+word.bounceRate+'</td><td>'+word.tradeUserCount+'</td><td>'+word.tradeCount+'</td><td>'+word.tradeAmount+'</td><td>'+word.tranrate+'</td></tr>'))
                        });
                    }
                }
            });
            targetDiv.append(table);
        },
        createItemDiagTable : function(){
            var table = searchDiag.Diag.createTableHtml();

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
            var table = searchDiag.Diag.createTableHtml();
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
                '<tr class="word-diag-result-table-th"><td>展现量</td><td>点击量</td><td>点击率</td><td>访客数</td>' +
                '<td>成交人数</td><td>成交件数</td><td>成交金额</td><td>成交转化率</td></tr>' +
                '</tbody></table>';
            return $(html);
        }
    }, searchDiag.Diag);

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

    searchDiag.Event = searchDiag.Event || {};
    searchDiag.Event = $.extend({
        setEvent : function(container, interval) {
            searchDiag.Event.setOpTabClickEvent(container);
            searchDiag.Event.setDiagByHrefBtnClick();
            searchDiag.Event.setIntervalEvent2(interval);
            searchDiag.Event.selectItemToDiagClickEvent();
            searchDiag.Event.setOrderByEvent();
        },
        setOrderByEvent : function(){
            searchDiag.init.Container.find('.word-diag-result-table-th .orderTd').click(function(){
                var interval = searchDiag.util.getInterval2();
                var endTime = searchDiag.util.getEndTime2();
                var orderBy = $(this).find('.sort').attr("sort");
                var numIid = searchDiag.init.Container.find('.diaging-item-href').attr("numIid");
                searchDiag.Diag.itemDiag(searchDiag.init.Container.find('.diag-result-div'), numIid,
                    interval, endTime, orderBy);
            });
        },
        setOpTabClickEvent : function(container) {
            container.find('.opTabWrapper .opTab').unbind('click').click(function(){
                if($(this).hasClass("selected")) {
                    return;
                }
                container.find('.opTabWrapper .selected').removeClass("selected");
                $(this).addClass("selected");
                var target = $(this).attr("target");
                switch (target){
                    case "item" :
                        searchDiag.Diag.itemDiag(container.find('.diag-result-div'));break;
                    case "shop" :
                        searchDiag.Diag.shopDiag(container.find('.diag-result-div'));break;
                    default :
                        searchDiag.Diag.itemDiag(container.find('.diag-result-div'));break;
                }
            });
            container.find('.opTabWrapper .opTab').eq(0).trigger("click");
        },
        setDiagByHrefBtnClick : function(){
            $('.diag-by-href').click(function(){
                var href = $('#to-diag-item-href').val();
                if(href == "") {
                    TM.Alert.load("请先输入宝贝链接");
                    return;
                }
                var numIid = searchDiag.util.getNumIidByHref(href);
                if(numIid== "") {
                    TM.Alert.load("您输入的宝贝链接格式不符");
                    return;
                }
                var interval = searchDiag.util.getInterval2();
                var endTime = searchDiag.util.getEndTime2();
                searchDiag.Diag.itemDiag(searchDiag.init.Container.find('.diag-result-div'), numIid, interval, endTime);
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
                searchDiag.init.Container.find('.interval-tr .interval[value="0"]').trigger("click");
            });
            endTimeInput.unbind('change').change(function(){
                searchDiag.init.Container.find('.interval-tr .interval[value="0"]').trigger("click");
            });
            searchDiag.init.Container.find('.interval-tr .interval').unbind('click').click(function(){
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
                //searchDiag.init.genUVAndTranrate(interval, endTime);
                searchDiag.Diag.itemDiag(searchDiag.init.Container.find('.diag-result-div'),
                    searchDiag.init.Container.find('.diaging-item-href').attr("numIid"),
                    interval, endTime);
            });
        },
        setIntervalEvent2 : function (initInterval) {
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

                searchDiag.Diag.itemDiag(searchDiag.init.Container.find('.diag-result-div'),
                    searchDiag.init.Container.find('.diaging-item-href').attr("numIid"),
                    interval, endTime);
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
                "startDate": moment().subtract(initInterval, 'days'),//默认查询最近(initInterval)天
                "endDate": moment().subtract(1, 'days'),
                "maxDate": moment().subtract(1, 'days')
            })
        },
        selectItemToDiagClickEvent : function(){
            searchDiag.init.Container.find('.select-item-to-diag').unbind('click').click(function(){
                multiOpByURL.createChoose.createOrRefleshCommsDiv({
                    "itemsURL":"/Diag/chooseItems",
                    "pn":1,"ps":8,"enableSearch":true,
                    callbackFun:function(numIid){
                        var interval = searchDiag.util.getInterval2();
                        var endTime = searchDiag.util.getEndTime2();
                        searchDiag.Diag.itemDiag(searchDiag.init.Container.find('.diag-result-div'), numIid, interval, endTime);
                    }
                });
            });
        }
    }, searchDiag.Event);

    searchDiag.util = searchDiag.util || {};
    searchDiag.util = $.extend({
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
            var value = parseInt(searchDiag.init.Container.find('.interval-tr .interval:checked').val());
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
    }, searchDiag.util);

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