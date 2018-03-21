var TM = TM || {};
((function ($, window) {
    TM.CommentStatus = TM.CommentStatus || {};

    var CommentStatus = TM.CommentStatus;

    /**
     * 初始化
     * @type {*}
     */
    CommentStatus.init = CommentStatus.init || {};
    CommentStatus.init = $.extend({
        doInit: function(container) {
//            var html = CommentStatus.init.createHtml();
//            container.html(html);
            CommentStatus.container = container;

            container.find(".search-btn").click(function() {
                CommentStatus.show.doShow();
            });
            
            container.find('.update-btn').click(function(){
            	$.get("/items/syncTradeRates", function(data){
            		if(!data.success) {
            			TM.Alert.load('<br><p style="font-size:14px">同步太频繁啦！请5分钟后再来试试吧~</p>',400,230,function(){
            				
            			});
            			return;
            		}
//            		CommentStatus.show.doShow();
            		TM.Alert.load('<p style="font-size:14px">亲，评价同步成功,点击确定刷新页面</p>',400,230,function(){
                        window.location.reload();
                    });
            	});
            });
//            container.find(".end-time-text").keydown(function(event) {
//                if (event.keyCode == 13) {//按回车
//                    container.find(".search-btn").click();
//                }
//            });
            container.find(".start-time-text").datepicker({
            	maxDate : "d",
            	onClose : function(selectedDate) {
    			if(selectedDate != null && selectedDate.length > 0){
    				$(".end-time-text").datepicker("option", "minDate", selectedDate);
    			}
    		}});
            container.find(".end-time-text").datepicker({
            	maxDate : "d",
            	onClose : function(selectedDate) {
    			if(selectedDate != null && selectedDate.length > 0){
    				$(".start-time-text").datepicker("option", "maxDate", selectedDate);
    			}
    		}});
            CommentStatus.show.initSearchParams();
            CommentStatus.show.doShow();
        }
    }, CommentStatus.init);

    CommentStatus.show = CommentStatus.show || {};
    CommentStatus.show = $.extend({
        doShow: function() {
            var ruleData = CommentStatus.show.getQueryRule();
            var tbodyObj = CommentStatus.container.find(".skincomment-table").find("tbody");
            $.post('/SkinComment/queryStatus', ruleData, function(data){
            	$('#rows').empty();
                if (data == undefined || data.size == 0) {
                	$('#rows').html('<td colspan="10">亲，请选择正确的时间段哦~</td>');
                } else {
                	$('#tplRow').tmpl(data,{
                	    dataArrayIndex: function(item) {
                	        return $.inArray(item, data);
                	    }
                	}).appendTo('#rows');
                }

                $.post('/skincomment/queryGoodRate', ruleData, function(list){
                    TM.Chart.renderChart(list);
                });
            });
            
        },
        refresh: function() {
            CommentStatus.show.doShow();
        },
        getQueryRule: function() {
            var ruleData = {};
            var startTime = CommentStatus.container.find(".start-time-text").val();
            var endTime = CommentStatus.container.find(".end-time-text").val();
            ruleData.startTime = startTime;
            ruleData.endTime = endTime;
            return ruleData;
        },
        initSearchParams : function(){
            var now = new Date();
			var lastMonth = new Date();
			lastMonth.setDate(now.getDate() - 30);
			$(".start-time-text").val(lastMonth.format("yyyy-MM-dd"));
            $(".end-time-text").val(now.format("yyyy-MM-dd"));
        }
    }, CommentStatus.show);

})(jQuery,window));

((function ($, window) {
    TM.Chart = TM.Chart || {};

    TM.Chart.renderChart = function(data){
        var days = TM.Chart.genDays();
        var rates = TM.Chart.genRates(data);
//        console.info(days);
//        console.info(rates);

        var start = TM.Chart.parseDate($(".start-time-text").val());
        var end = TM.Chart.parseDate($(".end-time-text").val());

        var step = Math.round(days.length / 10);
//        console.info(step);
        var minY = TM.Chart.smallest(rates) - 0.1;

        var chart = new Highcharts.Chart({
            chart : {
                renderTo : 'goodRate-charts',
                defaultSeriesType: 'line' //图表类型line(折线图)
            },
            credits : {
                enabled: false   //右下角不显示LOGO
            },
            title: {
                text: '店铺综合好评率'
            }, //图表标题
            xAxis: {  //x轴
                categories: days,   //['六天前', '五天前', '四天前', '三天前', '大前天',  '前天', '昨天'], //x轴标签名称
                title: '日期',
                gridLineWidth: 1, //设置网格宽度为1
                lineWidth: 2,  //基线宽度
                labels:{step: step, y:26}  //x轴标签位置：距X轴下方26像素
            },
            yAxis: [{  //y轴
//                startOnTick: false,
//                endOnTick: false,
//                min: minY,
                title: {text: '好评率'}, //标题
                lineWidth: 2 //基线宽度
            }, {
//                min: minY,
                title: {text: '好评率'}, //标题
                opposite: true,
                lineWidth: 2 //基线宽度
            }],
            plotOptions:{ //设置数据点
                line:{
                    dataLabels:{
                        enabled:false  //在数据点上显示对应的数据值
                    },
                    enableMouseTracking: true //取消鼠标滑向触发提示框
                }
            },
            tooltip: {
                useHTML: true,
                formatter: function () {                 //当鼠标悬置数据点时的格式化提示
                    return '<div style="line-height: 20px;">日期: <b>' + this.x + '</b>&nbsp;<br><b>' + this.series.name + ': </b><span style="color:red;">' + Highcharts.numberFormat(this.y, 2) + "%</span></div>";
                }
            },
            series: [
                {  //数据列
                    name: '好评率',
                    data: rates,
                    yAxis:0
                }
            ]
        });
    }

    TM.Chart.genRates = function(data) {
        var res = [];
        var start = TM.Chart.parseDate($(".start-time-text").val());
        var end = TM.Chart.parseDate($(".end-time-text").val());

        var k = 0;
        var tmp = 100;
        if(data && data.length > 0) {
            tmp = data[0].goodRate;
        }
        for(k=0; k < data.length - 1 && start.getTime() > data[k].ts; k++){};

        for(var i=0; start.getTime() <= end.getTime();i++){
            if(k < data.length){
                var d = new Date(data[k].ts).formatYMS();
                if(start.formatYMS() == d){
                    tmp = data[k++].goodRate;
                    res.push(tmp);
                    start.setDate(start.getDate() + 1);
                    continue;
                }
            }
            res.push(tmp);
            start.setDate(start.getDate() + 1);
        }
        return res;
    }

    TM.Chart.genDays = function() {
        var days = [];
        var start = TM.Chart.parseDate($(".start-time-text").val());
        var end = TM.Chart.parseDate($(".end-time-text").val());

        for(var i=0; start.getTime() <= end.getTime();i++){
            days.push(start.format("MM/dd"));
            start.setDate(start.getDate() + 1);
        }
        return days;
    }

    TM.Chart.smallest = function(array){
        return Math.min.apply( Math, array );
    }

    TM.Chart.largest =function(array){
        return Math.max.apply( Math, array );
    }

    TM.Chart.parseDate = function(str){
        str=str.split('-');
        var date = new Date(str[0], str[1]-1, str[2]);
        return date;
    }

})(jQuery, window));