var TM = TM || {};

((function ($, window) {
	
	TM.ShopScore = TM.ShopScore || {};
	var ShopScore = TM.ShopScore;

	ShopScore.init = ShopScore.init || {};
	ShopScore.init = $.extend({
		doInit: function(container) {
			ShopScore.container = container;

			ShopScore.event.initSearchParams();
			ShopScore.event.init();

			ShopScore.show.doShow();
		},
		getContainer: function() {
			return ShopScore.container;
		}
	}, ShopScore.init);
	
	
	ShopScore.show = ShopScore.show || {};
	ShopScore.show = $.extend({
		doShow: function(){
			var container = ShopScore.init.getContainer();
			
			var paramData = ShopScore.util.getParamData();
			
			var tbodyObj = container.find(".shop-score-table").find("tbody.shop-score-tbody");
			
			$.ajax({
				type: "POST",
				url: '/SkinComment/getShopScore',
				data: paramData,
				cache: false,
				dataType: "json",
				success: function(dataJson){
					tbodyObj.html("");
					
					if (dataJson == null || dataJson.length <= 0) {
						
						container.find('.shop-score-table').append('<tr class="no_data" style="border: 1px solid #C0DAEC; height: 40px;"><td colspan="4" style="text-align: center; font-size: 16px;"><span style="color:red;">亲，暂无符合条件的数据！</span></td></tr>');
						
					} else {
						$(dataJson).each(function(index, shopScoreJson) {
							
							var trObj = ShopScore.row.createRow(index, shopScoreJson);
							
							tbodyObj.append(trObj);
						});
						
						ShopScore.event.refreshShopScoreHighCharts(dataJson)
					}
				}
			});
		}
	}, ShopScore.show);
	
	
	ShopScore.row = ShopScore.row || {};
	ShopScore.row = $.extend({
		createRow: function(index, shopScoreJson) {
			ShopScore.util.formatShopScoreJson(shopScoreJson, index);

			var trObj = $('#tplRow').tmpl(shopScoreJson);

			return trObj;
		}
	}, ShopScore.row);


	ShopScore.event = ShopScore.event || {};
	ShopScore.event = $.extend({
		initSearchParams : function(){
			var now = new Date();
			var lastMonth = new Date();
			lastMonth.setDate(now.getDate() - 2);
			$(".start-time-text").val(lastMonth.format("yyyy-MM-dd"));
			$(".end-time-text").val(now.format("yyyy-MM-dd"));
		},
		init: function name() {
			$(".search-btn").unbind().click(function() {
				ShopScore.show.doShow();
			});
			
			$(".start-time-text").datepicker({
				maxDate : "d",
				onClose : function(selectedDate) {
					if(selectedDate != null && selectedDate.length > 0){
						$(".end-time-text").datepicker("option", "minDate", selectedDate);
					}
			}});
			$(".end-time-text").datepicker({
				maxDate : "d",
				onClose : function(selectedDate) {
					if(selectedDate != null && selectedDate.length > 0){
						$(".start-time-text").datepicker("option", "maxDate", selectedDate);
					}
			}});
		},
		refreshShopScoreHighCharts : function(dataJson){
			if (dataJson == null || dataJson.length <= 0) {
				return;
			}
			
			var dayArr = [], itemScoreArr = [], serviceScoreArr = [], deliveryScoreArr = [];
			
			$(dataJson).each(function(index, shopScoreJson) {
				dayArr.push(shopScoreJson.timeStr)
				itemScoreArr.push(parseFloat(new Number(shopScoreJson.itemScore).toFixed(1)));
				serviceScoreArr.push(parseFloat(new Number(shopScoreJson.serviceScore).toFixed(1)));
				deliveryScoreArr.push(parseFloat(new Number(shopScoreJson.deliveryScore).toFixed(1)));
			});
			
			chart = new Highcharts.Chart({
				chart : {
					renderTo : 'shop-score-charts',
					defaultSeriesType: 'line' //图表类型line(折线图)
				},
				credits : {
					enabled: false   //右下角不显示LOGO
				},
				title: {
					text: "店铺动态评分趋势图"
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
						title: {text: '店铺动态评分'}, //左标题
						lineWidth: 0 //基线宽度
					},
//					{
//						title: {text: '成交转化率数据'}, //右标题
//						lineWidth: 0, //基线宽度
//						opposite:true
//					}
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
						name: '商品描述评分',
						data: itemScoreArr,
						yAxis:0
					},
					{  //数据列
						name: '服务态度评分',
						data: serviceScoreArr,
						yAxis:0
					},
					{  //数据列
						name: '发货速度评分',
						data: deliveryScoreArr,
						yAxis:0
					}
				]
			});
		},
	}, ShopScore.event);


	ShopScore.util = ShopScore.util || {};
	ShopScore.util = $.extend({
		getParamData: function() {
			var container = ShopScore.init.getContainer();

			var paramData = {};

			var timeZone = 8 * 60 * 60 * 1000;
			var startTimeObj = container.find('.start-time-text');
			var endTimeObj = container.find('.end-time-text');
			var startDate = new Date(startTimeObj.val());
			var endDate = new Date(endTimeObj.val());
			var startTime = startDate.getTime() - timeZone ;
			var endTime = endDate.getTime() - timeZone;

			paramData.startTime = startTime;
			paramData.endTime = endTime;

			return paramData;
		},
		formatShopScoreJson: function(shopScoreJson, index) {
			shopScoreJson.index = index;
			
//			if(ShopScoreJson.status == "1") {
//				ShopScoreJson.statusStr = "等待审核";
//			} else if(ShopScoreJson.status == "2") {
//				ShopScoreJson.statusStr = "审核通过";
//			} else if(ShopScoreJson.status == "3") {
//				ShopScoreJson.statusStr = "审核拒绝";
//			} else {
//				ShopScoreJson.statusStr = "状态未知"
//			}
		},
		parseLongToDate:function(ts) {
			var theDate = new Date();
			theDate.setTime(ts);
			var year = theDate.getFullYear();
			var month = theDate.getMonth() + 1;//js从0开始取
			var date = theDate.getDate();
			var hour = theDate.getHours();
			var minutes = theDate.getMinutes();
			var second = theDate.getSeconds();

			if (month < 10) {
				month = "0" + month;
			}
			if (date < 10) {
				date = "0" + date;
			}
			if (hour < 10) {
				hour = "0" + hour;
			}
			if (minutes < 10) {
				minutes = "0" + minutes;
			}
			if (second < 10) {
				second = "0" + second;
			}
			var timeStr = year+"-"+month+"-"+date+" "+hour+":"+minutes+":"+second;
			return timeStr;
		}
	}, ShopScore.util);

})(jQuery,window));