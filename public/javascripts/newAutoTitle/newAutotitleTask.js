var TM = TM || {};
((function ($, window) {

    TM.NewAutoTitleTask = TM.NewAutoTitleTask || {};

    var NewAutoTitleTask = TM.NewAutoTitleTask;

    NewAutoTitleTask.init = NewAutoTitleTask.init || {};
    NewAutoTitleTask.init = $.extend({
        doInit: function(container) {

            NewAutoTitleTask.container = container;

            NewAutoTitleTask.show.doShow();

            NewAutoTitleTask.timer.initTimer();

            container.find(".ctr-refresh-btn").click(function() {

                if ($(this).hasClass("ison")) {
                    $(this).html("开始刷新");
                    $(this).removeClass("ison");
                    $(this).addClass("isoff");
                    NewAutoTitleTask.timer.isStopRefresh = true;
                } else {
                    $(this).html("停止刷新");
                    $(this).removeClass("isoff");
                    $(this).addClass("ison");
                    NewAutoTitleTask.timer.isStopRefresh = false;
                }

            });

        }
    }, NewAutoTitleTask.init);


    NewAutoTitleTask.show = NewAutoTitleTask.show || {};
    NewAutoTitleTask.show = $.extend({
        currentPage: 1,
        doShow: function() {
            NewAutoTitleTask.show.doQuery(1);
        },
        doRefresh: function() {
            NewAutoTitleTask.timer.setOnEdit();
            NewAutoTitleTask.show.doQuery(NewAutoTitleTask.show.currentPage);
        },
        doQuery: function(currentPage) {

            //NewAutoTitleTask.show.doInitCurrentTable(currentPage);
            NewAutoTitleTask.show.doInitTable(currentPage);

        },
        doInitTable: function(currentPage){
            if (currentPage < 1) {
                currentPage = 1;
            }

            NewAutoTitleTask.show.currentPage = currentPage;
            var tbodyObj = NewAutoTitleTask.container.find(".history-task-table").find("tbody");
            var unFinishedTaskCount = NewAutoTitleTask.container.find('.unFinishedTaskCount');

            NewAutoTitleTask.container.find(".paging-div").tmpage({
                currPage: currentPage,
                pageSize: 10,
                pageCount:1,
                ajax: {
                    on: true,
                    param: {},
                    dataType: 'json',
                    url: "/titletaskop/findTasks",
                    callback:function(dataJson){

                        $(document).unbind('ajaxStart');

                        var taskJsonList = dataJson.res;
                        tbodyObj.html("");
                        if(!taskJsonList || taskJsonList.length == 0){
                            tbodyObj.html("<tr><td colspan='5' style='background:white;'>暂无已完成的任务</td></tr>");
                            unFinishedTaskCount.text(0);
                            NewAutoTitleTask.timer.finishOnEdit();
                            return;
                        }
                        unFinishedTaskCount.text(dataJson.msg);
                        var html = "";

                        $(taskJsonList).each(function(index, taskJson) {
                            var createTime = NewAutoTitleTask.util.timeToStr(taskJson.createTime);
                            var taskType = NewAutoTitleTask.util.parseTaskType(taskJson);
                            var taskStatus = NewAutoTitleTask.util.parseTaskStatus(taskJson);
                            var taskMsg = NewAutoTitleTask.util.parseTaskMessage(taskJson);
                            var finishedTime = NewAutoTitleTask.util.timeToStr(taskJson.finishedTime);
                            var opLogId = (taskJson&&taskJson.titleOpId)?taskJson.titleOpId:0;
                            var trHtml = '' +
                                '<tr>' +
                                '   <td>' + createTime + '</td>' +
                                '   <td>' + taskType + '</td>' +
                                '   <td>' + taskMsg + '</td>' +
                                '   <td>' + taskStatus + '</td>' +
                                '   <td>' + finishedTime + '</td>';

                            /*if (taskJson.status == 8) {
                                trHtml +='<td><a class="tmbtn sky-blue-btn" href="/newAutoTitle/goTitleRestore">任务结果</a></td>';
                            } else {
                                trHtml +='<td>-</td>';
                            }
*/
                            if(taskJson.type == 2){
                                if(taskJson.status==8){
                                    trHtml +='<td><a class="tmbtn sky-blue-btn" href="/wireless/down?taskId='+taskJson.id+'">点击下载</a></td>'
                                }else{
                                    trHtml +='<td>-</td>'
                                }

                            }else if(taskJson.type==3){

                                if(!taskJson.results || taskJson.results.indexOf('{')<0){
                                    trHtml +='<td>-</td>';
                                }else {
                                    var verObj = eval("(" + taskJson.results + ")");
                                    if (taskJson.status == 8) {
                                        var arr = [];
                                        arr.push('<td>');
                                        for (var key in verObj) {
                                            var value = verObj[key];
                                            arr.push('<a target="_self" class="tmbtn sky-blue-btn" href="/wireless/down?taskId=');
                                            arr.push(taskJson.id + '&ver=' + key + '">点击下载' + '' + '</a>');
                                            arr.push('<div class="blank0" style="height:5px"></div>')
                                        }
                                        arr.push('</td>')
                                        var joined = arr.join('');
                                        trHtml += joined;
                                    } else {
                                        trHtml += '<td>-</td>';
                                    }
                                }
                            }else{
                                trHtml +='<td>-</td>';
                            }


                            trHtml = trHtml +'</tr>';
                            html += trHtml;
                        });
                        tbodyObj.html(html);
                        NewAutoTitleTask.timer.finishOnEdit();
                    }
                }
            });
        }
    }, NewAutoTitleTask.show);


    NewAutoTitleTask.timer = NewAutoTitleTask.timer || {};
    NewAutoTitleTask.timer = $.extend({
        isOnEdit: false,
        isStopRefresh: false,
        leftSecond: 10,
        initTimer: function() {
            NewAutoTitleTask.container.find(".refresh-second").html(NewAutoTitleTask.timer.leftSecond);
            //1秒执行一次
            setInterval(function() {
                NewAutoTitleTask.timer.doForTimer();
            }, 1000);
        },
        doForTimer: function() {
            if (NewAutoTitleTask.timer.isOnEdit == true || NewAutoTitleTask.timer.isStopRefresh == true) {
                return;
            }
            NewAutoTitleTask.timer.leftSecond--;
            if (NewAutoTitleTask.timer.leftSecond <= 0) {
                NewAutoTitleTask.timer.leftSecond = 0;
                NewAutoTitleTask.timer.setOnEdit();
                NewAutoTitleTask.show.doRefresh();
            }

            NewAutoTitleTask.container.find(".refresh-second").html(NewAutoTitleTask.timer.leftSecond);

        },

        setOnEdit: function() {
            NewAutoTitleTask.timer.isOnEdit = true;
        },
        finishOnEdit: function() {

            NewAutoTitleTask.timer.leftSecond = 10;

            NewAutoTitleTask.container.find(".refresh-second").html(NewAutoTitleTask.timer.leftSecond);


            NewAutoTitleTask.timer.isOnEdit = false;



        }

    }, NewAutoTitleTask.timer);



    NewAutoTitleTask.util = NewAutoTitleTask.util || {};

    NewAutoTitleTask.util = $.extend({
        timeToStr: function(time) {

            if (time <= 0) {
                return '-';
            }

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

            switch (type) {
                case 1 :
                    return "一键自动标题";
                case 2 :
                    return "批量生成手机详情页";
                case 3 :
                    return "全店一键生成详情页";
                default :
                    return "系统异常，请联系我们！";
            }

        },
        parseTaskStatus: function(taskJson) {

            var status = taskJson.status;

            var type = taskJson.type;

            if (status == 1 || status == 2) {
                return "任务等待中。。";
            } else if (status == 4) {
                return '当前已完成：&nbsp;<span style="color: #a10000; font-size: 16px; font-weight: bold;">' + taskJson.progress + '%</span>';
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
                if(taskJson.type > 1){
                    return '';
                }else{
                    return taskMsg;
                }
            }
        }
    }, NewAutoTitleTask.util);





})(jQuery,window));