var TM = TM || {};
((function ($, window) {


    TM.shopHourDiag = TM.shopHourDiag || {};

    var shopHourDiag = TM.shopHourDiag;

    shopHourDiag.init = shopHourDiag.init || {};
    shopHourDiag.init = $.extend({
        doInit : function(container) {
            shopHourDiag.init.Container = container;
            var datMills = 24* 3600 * 1000;

            if(new Date().getTime() - parseInt(TM.firstLoginTime) < 8 * datMills) {
                $('.fourteen-day').hide();
            }
            // 检查用户是否进入未满一天
            if((new Date().getTime() - parseInt(TM.firstLoginTime)) < datMills) {
                container.find('.searchDiagTip').show();
            }
            // 设置相关事件
            shopHourDiag.Event.setEvent(container);
        }
    }, shopHourDiag.init);

    shopHourDiag.Diag = shopHourDiag.Diag || {};
    shopHourDiag.Diag = $.extend({
        shopWordsDiag : function(targetDiv, thedate, orderBy) {
            var refreshCharts = true;
            if(thedate === undefined || thedate == null) {
                thedate = new Date().getTime();
            }
            if(orderBy === undefined || orderBy == null) {
                orderBy = "thehour";
            } else {
                refreshCharts = false;
            }
            var table = shopHourDiag.init.Container.find('.word-diag-result-table');

            $.post("/Diag/getShopHourViewAndTrade", {thedate : thedate, orderBy : orderBy}, function(data){
                table.find('tbody').empty();
                if(data === undefined || data == null) {
                    table.find('tbody').append($('<tr class="no-data" style="height: 45px;"><td colspan="20">此时段暂无数据</td></tr>'));
                    return;
                }
                if(data.length == 0) {
                    table.find('tbody').append($('<tr class="no-data" style="height: 45px;"><td colspan="20">此时段暂无数据</td></tr>'));
                    return;
                }

                $(data).each(function(i, src){
                    var tranRate;
                    if(src.uv == "~" || parseInt(src.uv) == 0) {
                        tranRate = "0.00%";
                    } else {
                        tranRate = new Number(parseInt(src.alipay_trade_num)/parseInt(src.uv)).toPercent(2);
                    }
                    var srcName = parseInt(src.visit_platform) == 1 ? "无线端" : "PC端";
                    table.find('tbody').append($('<tr class="word-tr"><td class="">'+src.thehour+'</td><td>'+srcName+'</td><td>'+src.pv+'</td><td>'+src.uv+'</td>' +
                        '<td>'+src.view_repeat_num+'</td><td>'+src.alipay_trade_num+'</td><td>'+src.alipay_trade_amt+'</td><td>'+src.alipay_winner_num+'</td>' +
                        '<td>'+tranRate+'</td></tr>'));
                });
            });

            if(refreshCharts) {
                $.post("/Diag/getShopAllHourViewAndTrade", {thedate : thedate}, function(data){
                    if(data === undefined || data == null) {
                        return;
                    }
                    var pvArr = [], uvArr = [], tradeNumArr = [], tradeAmountArr = [], hourArr = [];
                    /*$(data).each(function(i,values){
                        hourArr.push(values.thehour)
                        pvArr.push(parseInt(values.pv));
                        uvArr.push(parseInt(values.uv));
                        tradeNumArr.push(parseInt(values.alipay_trade_num));
                        tradeAmountArr.push(parseFloat(values.alipay_trade_amt));
                    });*/
                    $.each(data,function(key,values){
                        hourArr.push(key);
                        pvArr.push(parseInt(values.pv));
                        uvArr.push(parseInt(values.uv));
                        tradeNumArr.push(parseInt(values.alipay_trade_num));
                        tradeAmountArr.push(parseFloat(values.alipay_trade_amt));
                    });
                    chart = new Highcharts.Chart({
                        chart : {
                            renderTo : 'shopHourCharts',
                            defaultSeriesType: 'line' //图表类型line(折线图)
                        },
                        credits : {
                            enabled: false   //右下角不显示LOGO
                        },
                        title: {
                            text: "店铺小时流量销量"
                        }, //图表标题
                        xAxis: {  //x轴
                            categories: hourArr, //x轴标签名称
                            gridLineWidth: 0, //设置网格宽度为1
                            lineWidth: 2,  //基线宽度
                            labels:{
                                y:20   //x轴标签位置：距X轴下方26像素
                                /*rotation: -45   //倾斜度*/
                            }
                        },
                        yAxis: [{  //y轴
                            title: {text: '店铺小时流量销量'}, //标题
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
                            }
                        ]
                    });
                });
            }

        },
        createItemDiagTable : function(){
            var table = shopHourDiag.Diag.createTableHtml();

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
            var table = shopHourDiag.init.Container.find('.word-diag-result-table');
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
    }, shopHourDiag.Diag);

    shopHourDiag.Event = shopHourDiag.Event || {};
    shopHourDiag.Event = $.extend({
        setEvent : function(container) {
            shopHourDiag.Event.setIntervalEvent();
            shopHourDiag.Event.setOrderByEvent();
            shopHourDiag.Event.setQuickSearchEvent();
        },
        setQuickSearchEvent : function(){
            shopHourDiag.init.Container.find('.quickSearch').unbind("click").click(function(){
                if($(this).hasClass("curr")) {
                    return;
                }
                shopHourDiag.init.Container.find('.quickSearch').removeClass("curr");
                $(this).addClass("curr");
                var thedate = new Date().getTime() - parseInt($(this).attr("tag")) * 24 * 3600 * 1000;
                shopHourDiag.Diag.shopWordsDiag(shopHourDiag.init.Container.find('.diag-result-div'),
                    thedate);
            });
        },
        setOrderByEvent : function(){
            shopHourDiag.init.Container.find('.word-diag-result-table thead .orderTd').click(function(){
                if(shopHourDiag.init.Container.find('.interval-tr .curr').length > 0) {
                    var select = shopHourDiag.init.Container.find('.interval-tr .curr');
                    var thedate = new Date().getTime() - parseInt(select.attr("tag")) * 24 * 3600 * 1000;
                } else {
                    var thedate = parseInt(Date.parse($('#startTimeInput').val().toString().replace("-","/")));
                }

                var orderBy = $(this).find('.sort').attr("sort");
                shopHourDiag.Diag.shopWordsDiag(shopHourDiag.init.Container.find('.diag-result-div'),
                    thedate, orderBy);
            });
        },
        setIntervalEvent : function(){
            var curr = new Date().getTime();
            var dayMillis = 24 * 3600 * 1000;
            var startTimeInput = $("#startTimeInput");

            startTimeInput.datepicker();
            startTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
            startTimeInput.val(new Date(curr - dayMillis).formatYMS());

            $('#startTimeInput').unbind("change").change(function(){
                shopHourDiag.init.Container.find('.quickSearch').removeClass("curr");
                var thedate = parseInt(Date.parse($('#startTimeInput').val().toString().replace("-","/")));
                shopHourDiag.Diag.shopWordsDiag(shopHourDiag.init.Container.find('.diag-result-div'),
                    thedate);
            });
            $('#startTimeInput').trigger("change");
        }
    }, shopHourDiag.Event);

    shopHourDiag.util = shopHourDiag.util || {};
    shopHourDiag.util = $.extend({

    }, shopHourDiag.util);

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