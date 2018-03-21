var TM = TM || {};
((function ($, window) {


    TM.viewItemShow = TM.viewItemShow || {};

    var viewItemShow = TM.viewItemShow;

    viewItemShow.init = viewItemShow.init || {};
    viewItemShow.init = $.extend({
        doInit : function(container) {
        	viewItemShow.init.initTable();
        	viewItemShow.init.setItemViewTradeEvent();
        },
        initTable: function(interval, endTime){
        	if(interval == null || interval == "") {
        		var interval = viewItemShow.init.getDaysBetween();
        	}
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
		},
		setItemViewTradeEvent: function(){
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
				$('.interval-tr .interval[value="0"]').trigger("click");
			});
			endTimeInput.unbind('change').change(function(){
				$('.interval-tr .interval[value="0"]').trigger("click");
			});

			// 设置时间单选框事件
			$('.interval-tr .interval').unbind('click').click(function(){
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
				};
				viewItemShow.init.initTable(interval, endTime);
			});
			// 点击input文字相当于点击input
			// QueryCommodity.Event.setIntervalSpanClickEvent(viewTradeDiv);
		},
		getDaysBetween : function(){
        	var hourMills = 3600 * 1000;
        	var dayMills = 24 * 3600 * 1000;
            var nowTime = new Date().getTime();
            var firstLoginTime = parseInt(TM.firstLoginTime);
            var limitTime = parseInt((firstLoginTime / dayMills)) * dayMills + dayMills + 8 * hourMills;
            
            var interval = (nowTime - limitTime) / dayMills + 1;

            return interval;
        }
    }, viewItemShow.init);
    
})(jQuery,window));