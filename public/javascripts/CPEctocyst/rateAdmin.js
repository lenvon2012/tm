var TM = TM || {};

((function ($, window) {
	
	TM.RateAdmin = TM.RateAdmin || {};
	var RateAdmin = TM.RateAdmin;

	RateAdmin.init = RateAdmin.init || {};
	RateAdmin.init = $.extend({
		doInit: function(container) {
			RateAdmin.container = container;

			RateAdmin.event.initStaffSelectorSeach();
			RateAdmin.event.initSearchParams();
			RateAdmin.event.init();
		},
		getContainer: function() {
			return RateAdmin.container;
		}
	}, RateAdmin.init);
	
	
	RateAdmin.show = RateAdmin.show || {};
	RateAdmin.show = $.extend({
		doShow: function(){
			var container = RateAdmin.init.getContainer();
			
			var paramData = RateAdmin.util.getParamData();
			
			var tbodyObj = container.find(".rate-admin-table").find("tbody.rate-admin-tbody");
			
			$.ajax({
				type: "POST",
				url: '/CPEctocyst/queryStatus',
				data: paramData,
				cache: false,
				dataType: "json",
				success: function(dataJson){
					tbodyObj.html("");
					
					if (dataJson == null || dataJson.length <= 0) {
						
						container.find('.rate-admin-table').append('<tr class="no_data" style="border: 1px solid #C0DAEC; height: 40px;"><td colspan="10" style="text-align: center; font-size: 16px;"><span style="color:red;">亲，暂无符合条件的数据！</span></td></tr>');
						
					} else {
						$(dataJson).each(function(index, rateAdminJson) {
							
							var trObj = RateAdmin.row.createRow(index, rateAdminJson);
							
							tbodyObj.append(trObj);
						});
					}
				}
			});
		}
	}, RateAdmin.show);
	
	
	RateAdmin.row = RateAdmin.row || {};
	RateAdmin.row = $.extend({
		createRow: function(index, rateAdminJson) {
			RateAdmin.util.formatResultJson(rateAdminJson, index);
			
			var trObj = $('#tplRow').tmpl(rateAdminJson);

			return trObj;
		}
	}, RateAdmin.row);


	RateAdmin.event = RateAdmin.event || {};
	RateAdmin.event = $.extend({
		initStaffSelectorSeach: function(){
			// 如果是外包Chief账号登陆，则显示客服搜索下拉框
			if(TM.isCPEctocystAdmin()) {
				// 这里用ajax取客服列表，并生成 subStaffSelector
				$.get("/CPEctocyst/getAllSubStaffs", function(data){
					if(data === undefined || data == null) {
						return;
					}
					if(data.success == false) {
						return;
					}
					if(data.length == 0) {
						return;
					}
					var html = '<select class="kefuSearchSelector">';
					var name = RateAdmin.container.find('.name').attr("value");
					$(data).each(function(i, kefu){
						if(kefu.name == name) {
							html += '<option value="'+kefu.name+'" selected="selected">'+kefu.name+'</option>';
						} else {
							html += '<option value="'+kefu.name+'">'+kefu.name+'</option>';
						}
					});
					html += '</select>';
					RateAdmin.container.find('.staffSelectorSearchWrapper').append($(html));
					RateAdmin.container.find('.staffSelectorSearchWrapper').show();
					
					RateAdmin.show.doShow();
				});
			} else {
				RateAdmin.show.doShow();
			}
		},
		initSearchParams : function(){
			var now = new Date();
			var lastMonth = new Date();
			lastMonth.setDate(now.getDate() - 30);
			$(".start-time-text").val(lastMonth.format("yyyy-MM-dd"));
			$(".end-time-text").val(now.format("yyyy-MM-dd"));
		},
		init: function name() {
			$(".search-btn").unbind().click(function() {
				RateAdmin.show.doShow();
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
	}, RateAdmin.event);


	RateAdmin.util = RateAdmin.util || {};
	RateAdmin.util = $.extend({
		getParamData: function() {
			var container = RateAdmin.init.getContainer();

			var paramData = {};

			var startTime = container.find('.start-time-text').val();
			var endTime = container.find('.end-time-text').val();
			var staffName = container.find('.kefuSearchSelector').val();

			paramData.startTime = startTime;
			paramData.endTime = endTime;
			paramData.staffName = staffName;

			return paramData;
		},
		formatResultJson: function(rateAdminJson, index) {
			rateAdminJson.index = index;
		},
	}, RateAdmin.util);

})(jQuery,window));