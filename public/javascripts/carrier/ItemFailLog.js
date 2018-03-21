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
			var goodUrl = $("#good-url").val();
			var publisher = $("#publisher").val();
			var taskStatus = $("#task-status").val();
			var subTaskType = $("#sub-task-type").val();
			var data = {};
			data.pn = pn;
			data.startTime = sendBeginTime;
			data.endTime = sendEndTime;
			data.errorInfoKey = errorInfoKey;
			data.taskId = taskId;
			data.goodUrl = goodUrl;
			data.publisher = publisher;
			data.taskStatus = taskStatus;
			data.subTaskType = subTaskType;

			ItemFailLog.util.queryAjax('/itemcarrier/failTaskList', data, function(respData){
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
				var btn = data.taskStatus == 1 ? '' : '<button class="sui-btn btn-primary reboot-btn">重启</button>';
				$.each(smsSendLogs, function(i, item){
					var btnHtml = btn;
					var result = ItemFailLog.parseDataAS(item);
                    if(btn != '' && item.subTaskType == "$1688复制") {
                        btnHtml = '<button class="sui-btn btn-primary reboot-btn disabled">重启</button>';
					}

					html += '<tr dataId = '+item.id+'><td><label class="checkbox-pretty inline"><input type="checkbox" class="operate-check"><span></span></label><td>'+ItemFailLog.parseDateForNull(item.taskId)+ '</td><td>'+ItemFailLog.parseDateForNull(item.id)+ '</td><td>'+ItemFailLog.parseDateForNull(item.subTaskType)+ '</td><td>' +ItemFailLog.parseDateForNull(item.babyTitle)+'</td><td>'+ItemFailLog.parseDateForNull(item.url)+'</td>'+
					'<td>'+ItemFailLog.parseDateForNull(item.errorMsg)+'</td><td>'+ItemFailLog.parseDateForNull(item.publisher)+'</td><td>'+ItemFailLog.parseDateForNull(result.createTs)+'</td><td>'+ItemFailLog.parseDateForNull(result.pullTs)+'</td>' +
						'<td>'+btnHtml+'</td></tr>';
				});
				dataShow.html(html);
                ItemFailLog.even.operationBtn();
			});
		},
		parseDataAS: function(item){
			var result = result || {};
			result.createTs = new Date(item.createTs).formatYMSHMS();
			result.pullTs = new Date(item.pullTs).formatYMSHMS();

			return result;
		},
		parseDateForNull : function (data) {
			return data ? data : "~";
        },
		rebootItem : function (id) {
			ItemFailLog.util.queryAjax("/itemcarrier/rebootById", {ids:id}, function (resp) {
                $.alert('<div style="text-align: center;font-size: 16px;line-height: 100px;">'+ resp.msg +'</div>');
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
						subTaskIds.push($(this).parents("tr").attr("dataId"));
					}
                });

                ItemFailLog.util.queryAjax("/itemcarrier/rebootById", {ids : subTaskIds}, function (response) {
                    $.alert('<div style="text-align: center;font-size: 16px;line-height: 100px;">'+ response.msg +'</div>');
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
				var id = tr.attr("dataId");
				ItemFailLog.rebootItem(id);
            });
			$(".reboot-btn.disabled").unbind();
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

