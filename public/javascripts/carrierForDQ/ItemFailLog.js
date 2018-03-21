/**
 * Created by Administrator on 2015/10/24.
 */
var ItemFailLog = ItemFailLog || {};

(function(){
	ItemFailLog = $.extend({
		init: function(){
			ItemFailLog.getSmsSendLog(1);
			ItemFailLog.even.searchClick();
			ItemFailLog.even.selectClick();
			ItemFailLog.even.subTaskTypeBtn();
		},
		getSmsSendLog: function(pn){
			var sendBeginTime = $(".begin-time").val();
			var sendEndTime = $(".end-time").val();
			var errorInfoKey = $("#error-msg-key").val();
			var taskId = $("#task-id").val();
			var numiid = $("#numiid").val();
			var userId = $("#userId").val();
			var taskStatus = $("#task-status").val();
			var subTaskType = $("#sub-task-type").val();
			var cid = $("#cid").val();
			var data = {};
			data.pn = pn;
			data.startTime = sendBeginTime;
			data.endTime = sendEndTime;
			data.msg = errorInfoKey;
			data.taskId = taskId;
			data.numiid = numiid;
			data.userId = userId;
			data.taskStatus = taskStatus;
			data.subTaskType = subTaskType;
			data.cid = cid;

			ItemFailLog.util.queryAjax('/itemcarrierForDQ/failTaskList', data, function(respData){
				var dataShow = $(".sui-table tbody");
				dataShow.empty();
				if(!respData){
					dataShow.html('出错了！！！');
					return;
				}
				var smsSendLogs = respData.res;
				if(smsSendLogs == null || smsSendLogs.length <= 0){
					dataShow.html('<td colspan="10" style="text-align: center; font-size: 16px;"><span style="color:red;">暂无符合条件的数据！</span></td>');
					return;
				}
				// 重新设定分页
				$("#sms_total_count").empty().html(respData.count);
				$('.sui-pagination').pagination('updateItemsCount',respData.count);
                $('.sui-pagination').pagination('goToPage', pn);

                var html = '';
				$.each(smsSendLogs, function(i, item){
					item = ItemFailLog.parseDataAS(item);
                    var trObj = $('#templetRow').tmpl(item);
                    dataShow.append(trObj);
				});

                ItemFailLog.even.operationBtn();
			});
		},
		parseDataAS: function(item){
			var result = item || {};
			result.createTs = new Date(item.createTs).format('yyyy-MM-dd hh:mm:ss');
			if (item.type == 1) {
                result.type = "淘宝";
            } else if (item.type == 2) {
				result.type = "1688";
			} else {
                result.type = "~";
			}


			return result;
		},
		parseDateForNull : function (data) {
			return data ? data : "~";
        },
		rebootItem : function (id) {
			ItemFailLog.util.queryAjax("/itemcarrierForDQ/rebootById", {ids:id}, function (resp) {
                $.alert('<div style="text-align: center;font-size: 16px;line-height: 100px;">'+ resp.message +'</div>');
                ItemFailLog.getSmsSendLog(1);
            });
        },
        delItem : function (id) {
			ItemFailLog.util.queryAjax("/itemcarrierForDQ/delById", {ids:id}, function (resp) {
                $.alert('<div style="text-align: center;font-size: 16px;line-height: 100px;">'+ resp.message +'</div>');
                ItemFailLog.getSmsSendLog(1);
            });
        }
	});

	ItemFailLog.even = $.extend({
		searchClick: function(){
			$(".search").click(function(){
				ItemFailLog.getSmsSendLog(1);
			});
			$(".reboot-checked").click(function () {
				var subTaskIds = new Array();
                $(".operate-check").each(function () {
					if($(this).parent().hasClass("checked")) {
						subTaskIds.push($(this).parents("tr").attr("subTaskId"));
					}
                });

                ItemFailLog.util.queryAjax("/itemcarrierForDQ/rebootById", {ids : subTaskIds}, function (response) {
                    $.alert('<div style="text-align: center;font-size: 16px;line-height: 100px;">'+ response.message +'</div>');
                    ItemFailLog.getSmsSendLog(1);
                });

            });

		},
		selectClick : function () {
			$("#task-status").change(function () {
                ItemFailLog.getSmsSendLog(1);
            });
        },
		operationBtn : function () {
			$(".reboot-btn").click(function () {
				var tr = $(this).parents("tr");
				var id = tr.attr("subTaskId");
				ItemFailLog.rebootItem(id);
            });
			$(".del-btn").click(function () {
				var tr = $(this).parents("tr");
				var id = tr.attr("subTaskId");
				ItemFailLog.delItem(id);
            });
			$(".all-operate-check").unbind().click(function () {
				if($(this).parent().hasClass("checked")) {
                    $(".operate-check").each(function(){
                        if($(this).parent().hasClass("checked")) {
                            $(this).parent().trigger("click");
                        }
                    });
				} else {
                    $(".operate-check").each(function(){
                        if(!$(this).parent().hasClass("checked")) {
                            $(this).parent().trigger("click");
                        }
                    });
				}
            });
			$(".operate-check").unbind().click(function(){
                if($(this).parent().hasClass("checked")) {
                    if($(".all-operate-check").parent().hasClass("checked")) {
                        $(".all-operate-check").parent().trigger("click");
                    };
                }
            });
        },
		subTaskTypeBtn : function () {
			$("#sub-task-type").change(function () {
                ItemFailLog.getSmsSendLog(1);
            });
        }
	});

	ItemFailLog.util = $.extend({
		queryAjax: function(url, data, callFun){
			$.ajax({
				type: "post",
				url: url,
				data: data,
				cache: false,
				success: function(data) {
					callFun(data);
				},
				error: function(e) {
					ItemFailLog.util.tipAlert(e.responseText);
				}
			});
		},
		tipAlert: function(message){
			$.alert('<div style="text-align: center;font-size: 16px;line-height: 100px;">'+ message +'</div>');
		}
	});

	ItemFailLog.page = $.extend({
		initPage : function(){
			// 初始化分页
			$('.sui-pagination').pagination({
				// 总页数
				itemsCount: 0,
				// 一页显示的条数
				pageSize: 10,
				// 是否展示总页数和跳转控制器
				showCtrl: true,
				// 要显示多少个页码
				displayPage: 5,
				// 点击分页
				onSelect: function(num){
					ItemFailLog.getSmsSendLog(num);
				}
			});
		}
	});

	$(document).ready(function(){
		ItemFailLog.init();
		ItemFailLog.page.initPage();
	});

})();

