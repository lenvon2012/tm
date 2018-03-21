var TM = TM || {};
((function ($, window) {
    TM.AdminCarrierTaskList = TM.AdminCarrierTaskList || {};
    var AdminCarrierTaskList = TM.AdminCarrierTaskList;

    AdminCarrierTaskList.init = AdminCarrierTaskList.init || {};
    AdminCarrierTaskList.init = $.extend({
        doInit: function(container) {
            AdminCarrierTaskList.container = container;
            AdminCarrierTaskList.event.setEvent();
        },
        getContainer: function() {
            return AdminCarrierTaskList.container;
        }

    }, AdminCarrierTaskList.init);

    AdminCarrierTaskList.util = AdminCarrierTaskList.util || {};
    AdminCarrierTaskList.util = $.extend({
        addSortParams: function(container, paramData) {

            var orderObj = container.find(".current-sort");
            var orderBy = "";
            var isDesc = false;

            if (orderObj.length <= 0) {
                orderBy = "";
            } else {
                orderBy = orderObj.attr("orderBy");
                if (orderObj.hasClass("sort-down"))
                    isDesc = true;
                else
                    isDesc = false;
            }

            paramData.orderBy = orderBy;
            paramData.isDesc = isDesc;

            paramData.userNick = $("#search-input").val();

        }

    }, AdminCarrierTaskList.util);

    AdminCarrierTaskList.show = AdminCarrierTaskList.show || {};
    AdminCarrierTaskList.show = $.extend({
        currentPage: 1,
        historyCurrentPage: 1,
        doShow: function() {
            AdminCarrierTaskList.show.doSearch(1);
            AdminCarrierTaskList.show.doHistorySearch(1);
        },
        doRefresh: function() {
            AdminCarrierTaskList.show.doSearch(AdminCarrierTaskList.show.currentPage);
            AdminCarrierTaskList.show.doHistorySearch(AdminCarrierTaskList.show.historyCurrentPage);
        },

        doSearch: function(currentPage) {

            if (currentPage < 1) {
                currentPage = 1;
            }
            AdminCarrierTaskList.show.currentPage = currentPage;

            var container = AdminCarrierTaskList.init.getContainer().find(".current-admin-div");
            var param = {};
            AdminCarrierTaskList.util.addSortParams(container, param);
            if (param.orderBy == '') {
                param.isDesc = true;
            }
            var tbodyObj = container.find(".current-task-table tbody.current-task-body");
            container.find(".current-paging-div").tmpage({
                currPage: currentPage,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: param,
                    dataType: 'json',
                    url: '/itemCarrier/adminTaskList',
                    callback: function (dataJson) {
                        AdminCarrierTaskList.show.currentPage = dataJson.pn;
                        var orderArray = dataJson.res;

                        tbodyObj.html("");
                        if (orderArray === undefined || orderArray == null || orderArray.length <= 0) {
                            var trHtml = '' +
                                '<tr><td colspan="12" style="text-align: center;padding: 8px 0;">' +
                                "没有查询到数据" +
                                '</td></tr>' +
                                '';

                            tbodyObj.html(trHtml);

                            return;
                        }

                        var trObj = $("#myTaskTrTmpl").tmpl(orderArray);

                        tbodyObj.html(trObj);


                        $(".queryDetail").click(function () {
                            var id = $(this).attr('taskid');
                            if (id == undefined) {
                                return;
                            }
                            AdminCarrierTaskList.show.doSearchSubTask(id);
                        });
                        $(".deleteTask").click(function () {
                            var id = $(this).attr('taskid');
                            if (id == undefined) {
                                return;
                            }
                            var taskStatus = $(this).attr('taskStatus');
                            AdminCarrierTaskList.show.doDeleteTask(id, taskStatus);
                        });
                        $(".rebootTask").click(function () {
                            var id = $(this).attr('taskid');
                            if (id == undefined) {
                                return;
                            }
                            var taskStatus = $(this).attr('taskStatus');
                            AdminCarrierTaskList.show.doRebootTask(id, taskStatus);
                        });
                    }
                }
            })
        },

        doHistorySearch: function(historyCurrentPage) {

            if (historyCurrentPage < 1) {
                historyCurrentPage = 1;
            }
            AdminCarrierTaskList.show.historyCurrentPage = historyCurrentPage;
            var container = AdminCarrierTaskList.init.getContainer().find(".history-admin-div");
            var param = {};
            AdminCarrierTaskList.util.addSortParams(container, param);
            if (param.orderBy == '') {
                param.isDesc = true;
            }
            var tbodyObj = container.find(".history-task-table tbody.history-task-body");
            container.find(".history-paging-div").tmpage({
                currPage: historyCurrentPage,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: param,
                    dataType: 'json',
                    url: '/itemCarrier/adminHistoryTaskList',
                    callback: function(dataJson){

                        AdminCarrierTaskList.show.historyCurrentPage = dataJson.pn;
                        var orderArray = dataJson.res;

                        tbodyObj.html("");
                        if (orderArray === undefined || orderArray == null || orderArray.length <= 0) {
                            var trHtml = '' +
                                '<tr><td colspan="12" style="text-align: center;padding: 8px 0;">' +
                                "没有查询到数据" +
                                '</td></tr>' +
                                '';

                            tbodyObj.html(trHtml);

                            return;
                        }

                        var trObj = $("#myHistoryTaskTrTmpl").tmpl(orderArray);
                        tbodyObj.html(trObj);

                        $('.historyQueryDetail').click(function() {
                            var id = $(this).attr('taskid');
                            if (id == undefined) {
                                return;
                            }
                            AdminCarrierTaskList.show.doSearchSubTask(id);
                        });
                    }
                }
            });
        },

        doSearchSubTask: function(id) {
            var tableDiv = $('<div class="sub-task-dialog" style="">'
            + '<table class="list-table task-detail-table" style="">'
            + '<thead><tr>'
            + '<td class="headtd" style="width: 200px;">宝贝主图</td>'
            + '<td class="headtd" style="width: 200px;">任务状态</td>'
            + '<td class="headtd" style="width: 500px;">任务信息</td></thead>'
            + '<tbody class="task-detail-body"></tbody></table>'
            + '<div class="paging" style="text-align: center;margin-top: 10px; margin-bottom: 5px;"></div>'
            + '<script type="text/x-jquery-tmpl" id="mySubtaskTrTmpl"><tr><td>'
            + '<span><img src={{= imgStr}} style="width:80px; height:80px;"></span></td><td><span>{{= statusStr}}</span></td><td><span style="font-size: small;display:block; ">{{= babyTitle}}</span>{{if status == 1}}'
            + '<span style="margin-top: 30px;display:block; ">宝贝链接为:<a href={{= errorMsg}} target="_blank">{{= errorMsg}}</a></span>'
            + '{{else status == 2}} <span style="margin-top: 30px;display:block; ">{{= errorMsg}}</span>{{else}}'
            + '<span style="margin-top: 30px;display:block; ">等待中</span>{{/if}}</td></script>');
            TM.Alert.loadDetail(tableDiv,780, 670, null,"子任务详情");
            var table = $('.sub-task-dialog');
            table.find('.paging').tmpage({
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: {"taskId": id},
                    dataType: 'json',
                    url: '/itemCarrier/showTaskDetail',
                    callback: function (dataJson) {
                        var dialogTable = table.find(".task-detail-table tbody.task-detail-body");
                        var orderArray = dataJson.res;

                        dialogTable.html("");
                        if (orderArray === undefined || orderArray == null || orderArray.length <= 0) {
                            var trHtml = '' +
                                '<tr><td colspan="12" style="text-align: center;padding: 8px 0;">' +
                                "没有查询到数据" +
                                '</td></tr>' +
                                '';

                            dialogTable.html(trHtml);

                            return;
                        }

                        var trObj = $("#mySubtaskTrTmpl").tmpl(orderArray);

                        dialogTable.html(trObj);
                    }
                }
            })
        },

        doDeleteTask : function (id, taskStatus) {
            if (!confirm("确认删除任务")) return;

            $.get("/itemCarrier/deleteTask?taskId="+id, function (data) {
                if (data && !data.success) {
                    TM.Alert.load("删除任务失败，请重试");
                }
                $("#search-btn").trigger("click");
            });
        },

        doRebootTask : function (id, taskStatus) {
            if (taskStatus == 0) {

            } else if (taskStatus == 1 && confirm("已获取宝贝信息，等待复制宝贝 确认重启任务？")) {

            } else if (taskStatus == 2 && confirm("正在复制宝贝 确认重启任务？")) {

            } else if (taskStatus == 4 && confirm("任务已完成 确认重启任务？")) {

            } else {
                return;
            }
            $.get("/itemCarrier/rebootTask?taskId="+id, function (data) {
                if (data && !data.success) {
                    TM.Alert.load("重启任务失败，请重试");
                }
                $("#search-btn").trigger("click");
            });
        }
    }, AdminCarrierTaskList.show);

    AdminCarrierTaskList.event = AdminCarrierTaskList.event || {};
    AdminCarrierTaskList.event = $.extend({
        setEvent : function () {
           AdminCarrierTaskList.event.setSearchButtonClickEvent();
        },
        setSearchButtonClickEvent : function () {
            $("#search-btn").click(function () {
                if ($("#search-input").val().trim() === "") {
                    TM.Alert.load("请输入旺旺");
                    return;
                }
                AdminCarrierTaskList.show.doShow();
            });
            document.onkeydown=function(event){
                var e = event || window.event || arguments.callee.caller.arguments[0];
                if(e && e.keyCode==13){ // enter 键
                    $("#search-btn").trigger("click");
                }
            };

        }
    }, AdminCarrierTaskList.event);


})(jQuery,window));