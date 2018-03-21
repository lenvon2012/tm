var TM = TM || {};
((function ($, window) {
    TM.CarrierTaskList = TM.CarrierTaskList || {};
    var CarrierTaskList = TM.CarrierTaskList;

    CarrierTaskList.init = CarrierTaskList.init || {};
    CarrierTaskList.init = $.extend({
        doInit: function(container) {
            CarrierTaskList.container = container;
            CarrierTaskList.show.doShow();
        },
        getContainer: function() {
            return CarrierTaskList.container;
        }

    }, CarrierTaskList.init);

    CarrierTaskList.util = CarrierTaskList.util || {};
    CarrierTaskList.util = $.extend({
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
        }

    }, CarrierTaskList.util);

    CarrierTaskList.show = CarrierTaskList.show || {};
    CarrierTaskList.show = $.extend({
        currentPage: 1,
        historyCurrentPage: 1,
        doShow: function() {
            CarrierTaskList.show.doSearch(1);
            CarrierTaskList.show.doHistorySearch(1);
        },
        doRefresh: function() {
            CarrierTaskList.show.doSearch(CarrierTaskList.show.currentPage);
            CarrierTaskList.show.doHistorySearch(CarrierTaskList.show.historyCurrentPage);
        },

        doSearch: function(currentPage) {

            if (currentPage < 1) {
                currentPage = 1;
            }
            CarrierTaskList.show.currentPage = currentPage;

            var container = CarrierTaskList.init.getContainer().find(".current-admin-div");
            var param = {};
            CarrierTaskList.util.addSortParams(container, param);
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
                    url: '/itemCarrier/taskList',
                    callback: function (dataJson) {
                        CarrierTaskList.show.currentPage = dataJson.pn;
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

                        $('.detail-btn').each(function () {
                            $(this).click(function () {
                                var id = $(this).attr('taskid');
                                if (id == undefined) {
                                    return;
                                }
                                CarrierTaskList.show.doSearchSubTask(id);
                            });

                        });
                        
                        $('.cancel-btn').each(function () {
                            $(this).click(function () {
                                var id = $(this).attr('taskid');
                                if (id == undefined) {
                                    return;
                                }
                                CarrierTaskList.show.doCancelSubTask(id);
                            });

                        });
                    }
                }
            })
        },

        doHistorySearch: function(historyCurrentPage) {

            if (historyCurrentPage < 1) {
                historyCurrentPage = 1;
            }
            CarrierTaskList.show.historyCurrentPage = historyCurrentPage;
            var container = CarrierTaskList.init.getContainer().find(".history-admin-div");
            var param = {};
            CarrierTaskList.util.addSortParams(container, param);
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
                    url: '/itemCarrier/historyTaskList',
                    callback: function(dataJson){

                        CarrierTaskList.show.historyCurrentPage = dataJson.pn;
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

                        $('.detail-btn').each(function() {
                            $(this).click(function() {
                                var id = $(this).attr('taskid');
                                if (id == undefined) {
                                    return;
                                }
                                CarrierTaskList.show.doSearchSubTask(id);
                            });
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
            + '{{else status == 2}} <span style="margin-top: 30px;display:block; ">{{= errorMsg}}</span>'
            + '{{else status == 4}} <span style="margin-top: 30px;display:block; ">{{= errorMsg}}</span>{{else}}'
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
        
        doCancelSubTask: function(id) {
            $.ajax({
                url: "/itemcarrier/cancelTask",
                data: {taskId : id},
                type: "POST",
                dataType: "JSON",
                success: function (data) {
                    if (data.success) {
                        alert("任务取消成功");
                        location.reload();
                    } else {
                        alert(data.message);
                    }
                }
            });
        }
    }, CarrierTaskList.show);


})(jQuery,window));