var TM = TM || {};
((function ($, window) {

    TM.TaskAdmin = TM.TaskAdmin || {};

    var TaskAdmin = TM.TaskAdmin;

    TaskAdmin.init = TaskAdmin.init || {};
    TaskAdmin.init = $.extend({
        doInit: function(container) {

            TaskAdmin.container = container;

            TaskAdmin.show.doShow();

            TaskAdmin.timer.initTimer();

            container.find(".ctr-refresh-btn").click(function() {

                if ($(this).hasClass("ison")) {
                    $(this).html("开始刷新");
                    $(this).removeClass("ison");
                    $(this).addClass("isoff");
                    TaskAdmin.timer.isStopRefresh = true;
                } else {
                    $(this).html("停止刷新");
                    $(this).removeClass("isoff");
                    $(this).addClass("ison");
                    TaskAdmin.timer.isStopRefresh = false;
                }

            });

        }
    }, TaskAdmin.init);


    TaskAdmin.show = TaskAdmin.show || {};
    TaskAdmin.show = $.extend({
        currentPage: 1,
        doShow: function() {
            TaskAdmin.show.doQuery(1);
        },
        doRefresh: function() {
            TaskAdmin.timer.setOnEdit();
            TaskAdmin.show.doQuery(TaskAdmin.show.currentPage);
        },
        doQuery: function(currentPage) {

            TaskAdmin.show.doInitCurrentTable(currentPage);


        },
        doInitCurrentTable: function(currentPage) {

            var tbodyObj = TaskAdmin.container.find(".current-task-table").find("tbody");

            $.ajax({
                type: "post",
                url: "/titletaskop/findUnFinishedTasks",
                data: {},
                success: function(dataJson){


                    TaskAdmin.show.doInitHistoryTable(currentPage);


                    var taskJsonList = dataJson;
                    tbodyObj.html("");

                    var html = "";

                    $(taskJsonList).each(function(index, taskJson) {
                        var createTime = TaskAdmin.util.timeToStr(taskJson.createTime);
                        var taskType = TaskAdmin.util.parseTaskType(taskJson);
                        var taskStatus = TaskAdmin.util.parseTaskStatus(taskJson);



                        var taskId = taskJson.taskId;
                        if (taskId === undefined || taskId == null || taskId <= 0) {
                            taskId = taskJson.id;
                            taskJson.taskId = taskJson.id;
                        }



                        var trHtml = '' +
                            '<tr>' +
                            '   <td>' + createTime + '</td>' +
                            '   <td>' + taskType + '</td>' +
                            '   <td>' + taskStatus + '</td>' +
                            '</tr>' +
                            '';

                        html += trHtml;
                    });

                    tbodyObj.html(html);


                }
            });
        },
        doInitHistoryTable: function(currentPage) {

            if (currentPage < 1) {
                currentPage = 1;
            }

            TaskAdmin.show.currentPage = currentPage;



            var tbodyObj = TaskAdmin.container.find(".history-task-table").find("tbody");

            TaskAdmin.container.find(".paging-div").tmpage({
                currPage: currentPage,
                pageSize: 10,
                pageCount:1,
                ajax: {
                    on: true,
                    param: {},
                    dataType: 'json',
                    url: "/titletaskop/findFinishedTasks",
                    callback:function(dataJson){
                        var taskJsonList = dataJson.res;
                        tbodyObj.html("");

                        var html = "";

                        $(taskJsonList).each(function(index, taskJson) {
                            var createTime = TaskAdmin.util.timeToStr(taskJson.createTime);
                            var taskType = TaskAdmin.util.parseTaskType(taskJson);
                            var taskStatus = TaskAdmin.util.parseTaskStatus(taskJson);

                            var taskMsg = TaskAdmin.util.parseTaskMessage(taskJson);


                            var finishedTime = TaskAdmin.util.timeToStr(taskJson.finishedTime);

                            var opLogId = (taskJson&&taskJson.titleOpId)?taskJson.titleOpId:0;
                            var trHtml = '' +
                                '<tr>' +
                                '   <td>' + createTime + '</td>' +
                                '   <td>' + taskType + '</td>' +
                                '   <td>' + taskStatus + '</td>' +
                                '   <td>' + taskMsg + '</td>' +
                                '   <td>' + finishedTime + '</td>' +
                                '   <td><a class="tmbtn sky-blue-btn" href="/autotitle/recover?id='+opLogId+'">任务结果</a></td>' +
                                '</tr>' +
                                '';

                            html += trHtml;
                        });


                        tbodyObj.html(html);

                        TaskAdmin.timer.finishOnEdit();
                    }
                }
            });

        }
    }, TaskAdmin.show);


    TaskAdmin.timer = TaskAdmin.timer || {};
    TaskAdmin.timer = $.extend({
        isOnEdit: false,
        isStopRefresh: false,
        leftSecond: 10,
        initTimer: function() {
            TaskAdmin.container.find(".refresh-second").html(TaskAdmin.timer.leftSecond);
            //1秒执行一次
            setInterval(function() {
                TaskAdmin.timer.doForTimer();
            }, 1000);
        },
        doForTimer: function() {
            if (TaskAdmin.timer.isOnEdit == true || TaskAdmin.timer.isStopRefresh == true) {
                return;
            }
            TaskAdmin.timer.leftSecond--;
            if (TaskAdmin.timer.leftSecond <= 0) {
                TaskAdmin.timer.leftSecond = 0;
                TaskAdmin.timer.setOnEdit();
                TaskAdmin.show.doRefresh();
            }

            TaskAdmin.container.find(".refresh-second").html(TaskAdmin.timer.leftSecond);

        },

        setOnEdit: function() {
            TaskAdmin.timer.isOnEdit = true;
        },
        finishOnEdit: function() {

            TaskAdmin.timer.leftSecond = 10;

            TaskAdmin.container.find(".refresh-second").html(TaskAdmin.timer.leftSecond);


            TaskAdmin.timer.isOnEdit = false;



        }

    }, TaskAdmin.timer);



    TaskAdmin.util = TaskAdmin.util || {};

    TaskAdmin.util = $.extend({
        timeToStr: function(time) {
            var theDate = new Date(time);
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
                second = "0" + second ;
            }

            var timeStr = year+"-"+month+"-"+date+" "+hour+":"+minutes+":"+second;
            return timeStr;
        },
        parseTaskType: function(taskJson) {
            var type = taskJson.type;

            if (type == 1) {
                return "全店自动推荐标题";
            } else {
                return "系统异常，请联系我们！";
            }

        },
        parseTaskStatus: function(taskJson) {

            var status = taskJson.status;

            var type = taskJson.type;

            if (status == 1 || status == 2) {
                return "任务等待中。。";
            } else if (status == 4) {
                return '任务执行中，当前已完成：&nbsp;<span style="color: #a10000; font-size: 16px; font-weight: bold;">' + taskJson.progress + '%</span>';
            } else if (status == 8) {
                return "任务已完成";
            } else if (status == 16) {
                return '<span style="color: #a10000;">任务执行失败，请重试或联系我们！</span> ';
            } else if (status == 64) {
                return "任务已被删除";
            } else {
                return "系统异常，请联系我们！";
            }

        },
        parseTaskMessage: function(taskJson) {
            var taskMsg = taskJson.message;
            if (taskMsg === undefined || taskMsg == null || taskMsg == '') {
                taskMsg = "-";
            }

            if (taskJson.status == 16) {
                return '<span style="color: #a10000;">' + taskMsg + '</span>';
            } else {
                return taskMsg;
            }
        }
    }, TaskAdmin.util);





})(jQuery,window));