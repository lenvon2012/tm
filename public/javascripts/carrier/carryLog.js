
var carryLog = carryLog || {};

(function() {

	carryLog.init = $.extend({
		doInit : function() {
			$(".carry-sub-lists").removeClass("hidden");
			
			carryLog.show.showTime();
			carryLog.show.doShow(1);
			carryLog.show.initPage();

			carryLog.event.init();
		}
	});
	
	carryLog.show = $.extend({
		initPage : function(){
			// 初始化分页
			$('.sui-pagination').pagination({
				// 总条数
				itemsCount: 0,
				// 一页显示的条数
				pageSize: 10,
				// 是否展示总页数和跳转控制器
				showCtrl: true,
				// 要显示多少个页码
				displayPage: 5,
				// 点击分页
				onSelect: function(num){
					carryLog.show.doShow(num);
				}
			});
		},
		showTime : function() {
			var end = new Date().getTime();
			var start = end - 6 * 24 * 60 *60 * 1000;
			$(".end_time").val(carryLog.util.parseLongToDate(end, true) + " 23:59");
			$(".start_time").val(carryLog.util.parseLongToDate(start, true) + " 00:00");
		},
		doShow : function(pn) {
			var paramData = carryLog.util.getParamData();
			paramData.pn = pn;
			
			var tbodyObj = $(".carry_log_table").find("tbody.carry_log_tbody");
			
			$.ajax({
				type: "POST",
				url: '/Kits/getCarryLog',
				data: paramData,
				cache: false,
				dataType: "json",
				success: function(dataJson) {
					var itemJsonArray = dataJson.res;
					
					tbodyObj.html("");
					
					if (itemJsonArray == null || itemJsonArray.length <= 0) {
						
						$('.carry_log_table').append('<tr class="no_data" style="border: 1px solid #C0DAEC; height: 40px;"><td colspan="6" style="text-align: center; font-size: 16px;"><span style="color:red;">亲，暂无符合条件的记录！</span></td></tr>');
						
					} else {
						// 重新设定总页数
						$('.sui-pagination').pagination('updateItemsCount', dataJson.count);
						$('.sui-pagination').pagination('goToPage', pn);
						
						$(itemJsonArray).each(function(index, logJson) {
							
							var trObj = carryLog.row.createRow(index, logJson);
							
							tbodyObj.append(trObj);
						});
					}
				},
				error: function () {
					$.alert("数据获取异常，请联系客服！")
				}
			});
		}
	});
	
	carryLog.row = $.extend({
		createRow : function(index, logJson) {
			carryLog.util.formatLogJson(logJson);

			var trObj = $('#logRow').tmpl(logJson);

			carryLog.event.initBtnEvents(trObj, logJson);

			return trObj;
		}
	});
	
	carryLog.event = $.extend({
		init: function name() {
			$(".search_input").unbind().keydown(function(event) {
				if (event.keyCode == 13) {	//按回车
					carryLog.show.doShow(1);
				}
			});

			$(".to_search_btn").unbind().click(function() {
				carryLog.show.doShow(1);
			});
			
			$(".task_type_select").unbind().change(function() {
				carryLog.show.doShow(1);
			});
			
			$(".task_status_select").unbind().change(function() {
				carryLog.show.doShow(1);
			});
		},
		initBtnEvents: function(trObj, logJson) {
			trObj.find(".re_start_btn").unbind().click(function() {
				var id = $(this).attr("id");
				
				$.ajax({
					type: "POST",
					url: '/Kits/reStartById',
					data: {id : id},
					cache: false,
					dataType: "json",
					success: function(dataJson) {
						$.alert(dataJson.message);
					},
					error: function () {
						$.alert("数据获取异常，请联系客服！")
					}
				});
			});
		}
	});

	carryLog.util = $.extend({
		getParamData : function() {
			var paramData = {};

			var startTime = $(".start_time").val().trim();
			var endTime = $(".end_time").val().trim();
			var originItem = $(".origin_item").val().trim();
			var resultMsg = $(".result_msg").val().trim();
			var taskType = $(".task_type_select").val().trim();
			var taskStatus = $(".task_status_select").val().trim();

			paramData.startTime = startTime;
			paramData.endTime = endTime;
			paramData.originItem = originItem;
			paramData.resultMsg = resultMsg;
			paramData.subTaskType = taskType;
			paramData.taskStatus = taskStatus;
			
			return paramData;
		},
		formatLogJson :function(logJson) {
			logJson.createTsStr = carryLog.util.parseLongToDate(logJson.createTs, false);
			if(logJson.subTaskType != null && logJson.subTaskType != "") {
				logJson.subTaskType = logJson.subTaskType.replace(/[$]/g, "");
			}
			if(logJson.url != null && logJson.url != "") {
				logJson.url = logJson.url.replace(/\/\/m/g, "//detail");
			}
		},
		parseLongToDate : function(ts, isShort) {
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
			
			if(isShort) {
				timeStr = year+"-"+month+"-"+date;
			}
			return timeStr;
		}
	});

	$(document).ready(function() {
		carryLog.init.doInit();
	});
	
})();