var TM = TM || {};

((function ($, window) {
    TM.ActivityAdmin = TM.ActivityAdmin || {};

    var ActivityAdmin = TM.ActivityAdmin;


    ActivityAdmin.init = ActivityAdmin.init || {};
    ActivityAdmin.init = $.extend({

        doInit: function(container) {

            ActivityAdmin.container = container;

            TM.OnActivity.init.doInit(container.find(".on-activity-div"));

            TM.EndActivity.init.doInit(container.find(".end-activity-div"));


            var activityTypeBtnObjs = container.find(".activity-type-btn");

            activityTypeBtnObjs.click(function() {

                var thisBtnObj = $(this);

                activityTypeBtnObjs.removeClass("active");

                thisBtnObj.addClass("active");

                container.find(".activity-list-div").hide();

                var targetCss = thisBtnObj.attr("targetDiv");
                container.find("." + targetCss).show();

                if (ActivityAdmin.util.isSearchOnActivity() == true) {
                    TM.OnActivity.show.doShow();
                } else if (ActivityAdmin.util.isSearchEndActivity() == true) {
                    TM.EndActivity.show.doShow();
                }

            });


            TM.OnActivity.show.doShow();

        },
        getContainer: function() {
            return ActivityAdmin.container;
        }

    }, ActivityAdmin.init);



    ActivityAdmin.event = ActivityAdmin.event || {};
    ActivityAdmin.event = $.extend({
        initBtnEvents: function(trObj, activityJson) {
            if (TM.ActivityList.util.isOldActivity(activityJson) == true) {

                trObj.find(".restart-activity-btn").remove();

                trObj.find(".reviseItem").click(function() {
                    var href="/taodiscount/revise_item?activityId="+activityJson.id;
                    trObj.find(".reviseItem").attr("href", href);
                });
                trObj.find(".addItem").unbind().click(function(){
                    var href="/taodiscount/add_Item?activityId="+activityJson.id;
                    trObj.find(".addItem").attr("href", href);
                }) ;
                trObj.find(".reviseAct").unbind().click(function(){
                    var href="/taodiscount/revise_Act?activityId="+activityJson.id;
                    trObj.find(".reviseAct").attr("href", href);
                }) ;
                trObj.find(".deleteAct").unbind().click(function(){
                    if(confirm('确实要结束该活动吗?')){
                        $.ajax({
                            url : '/taodiscount/deleteActivity',
                            data : {id:activityJson.id},
                            type : 'post',
                            success : function(data) {
                                if(data == null || data.length == 0){
                                    alert("结束活动成功");
                                    TM.OnActivity.show.doRefresh();
                                }
                                else {
                                    alert(data.msg);

                                }

                            }
                        });
                    }
                }) ;
                trObj.find(".deleteUnAct").unbind().click(function(){
                    $.ajax({
                        url : '/taodiscount/deleteUnactive',
                        data : {id:activityJson.id},
                        type : 'post',
                        success : function(data) {
                            if(data == null || data.length == 0){
                                alert("活动删除成功！");
                                TM.EndActivity.show.doRefresh();
                            }
                            else {
                                alert(data.msg);
                            }
                        }
                    });
                }) ;
            } else {
                TM.ActivityList.event.initNewActivityEvent(activityJson, trObj);
            }
        }
    }, ActivityAdmin.event);


    ActivityAdmin.util = ActivityAdmin.util || {};
    ActivityAdmin.util = $.extend({
        getParamData: function() {
            var container = ActivityAdmin.init.getContainer();

            var paramData = {};

            paramData.isactive = ActivityAdmin.util.getSearchActivityType();

            return paramData;
        },
        getSearchActivityType: function() {
            var container = ActivityAdmin.init.getContainer();
            var activityTypeObj = container.find(".activity-type-btn.active");

            var activityType = activityTypeObj.attr("activityType");

            return activityType;

        },
        isSearchOnActivity: function() {
            var activityType = ActivityAdmin.util.getSearchActivityType();
            if (activityType == 1) {
                return true;
            } else {
                return false;
            }
        },
        isSearchEndActivity: function() {
            var activityType = ActivityAdmin.util.getSearchActivityType();
            if (activityType == 2) {
                return true;
            } else {
                return false;
            }
        },
        formatActivityJson: function(activityJson) {
            //activityJson.startTimeStr = ActivityAdmin.util.parseLongToDate(activityJson.activityStartTime);
            //activityJson.endTimeStr = ActivityAdmin.util.parseLongToDate(activityJson.activityEndTime);

            if (TM.ActivityList.util.isOldActivity(activityJson) == true) {
                activityJson.activityTypeStr = '打折';
            } else if (TM.ActivityList.util.isDiscountActivity(activityJson) == true) {
                activityJson.activityTypeStr = '打折';
            } else if (TM.ActivityList.util.isNewDiscountActivity(activityJson) == true) {
                activityJson.activityTypeStr = '打折';
            } else if (TM.ActivityList.util.isMjsActivity(activityJson) == true) {
                activityJson.activityTypeStr = '满就送';
            } else if (TM.ActivityList.util.isShopDiscountActivity(activityJson) == true) {
                activityJson.activityTypeStr = '全店打折';
            } else if (TM.ActivityList.util.isShopMjsActivity(activityJson) == true) {
                activityJson.activityTypeStr = '全店满就送';
            }
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
            var timeStr = month+"月"+date+"日 "+hour+":"+minutes;//+ ":" + second;
            return timeStr;
        }
    }, ActivityAdmin.util);


})(jQuery,window));



((function ($, window) {
    TM.OnActivity = TM.OnActivity || {};

    var OnActivity = TM.OnActivity;

    OnActivity.init = OnActivity.init || {};
    OnActivity.init = $.extend({
        doInit: function(container) {
            OnActivity.container = container;


        },
        getContainer: function() {

            return OnActivity.container;
        }
    }, OnActivity.init);




    OnActivity.show = OnActivity.show || {};
    OnActivity.show = $.extend({
        targetCurrentPage: 1,
        doShow: function() {
            OnActivity.show.doSearch(1);
        },
        doRefresh: function() {
            OnActivity.show.doSearch(OnActivity.show.targetCurrentPage);
        },
        doSearch: function(currentPage) {
            if (currentPage < 1) {
                currentPage = 1;
            }

            OnActivity.show.targetCurrentPage = currentPage;

            var container = OnActivity.init.getContainer();

            var paramData = OnActivity.show.getParamData();
            if (paramData === undefined || paramData == null) {
                return;
            }

            var tbodyObj = container.find(".on-activity-table").find("tbody.on-activity-tbody");

            container.find(".on-paging-div").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: paramData,
                    dataType: 'json',
                    url: '/taodiscount/listActivity',
                    callback: function(dataJson){

                        OnActivity.show.targetCurrentPage = dataJson.pn;

                        var activityJsonArray = dataJson.res;

                        tbodyObj.html("");

                        if (activityJsonArray == null || activityJsonArray.length <= 0) {

                            container.find(".new-activity").show();
                            container.find(".f-huodong").show();
                            container.find(".item-table").hide();
                            container.find(".paging-div").hide();
                        } else {
                            container.find(".new-activity").hide();
                            container.find(".f-huodong").hide();
                            container.find(".item-table").show();
                            container.find(".paging-div").show();


                            $(activityJsonArray).each(function(index, activityJson) {
                                var trObj = OnActivity.row.createRow(index, activityJson);

                                tbodyObj.append(trObj);
                            });
                        }
                    }
                }
            });

        },
        getParamData: function() {


            return TM.ActivityAdmin.util.getParamData();
        }

    }, OnActivity.show);


    OnActivity.row = OnActivity.row || {};
    OnActivity.row = $.extend({

        createRow: function(index, activityJson) {
            TM.ActivityAdmin.util.formatActivityJson(activityJson);



            var trObj = $('#onActivityRow').tmpl(activityJson);

            if (TM.ActivityList.util.isOldActivity(activityJson) == true
                || TM.ActivityList.util.isDiscountActivity(activityJson) == true
                || TM.ActivityList.util.isNewDiscountActivity(activityJson) == true) {

                trObj.find(".tmpl-btn-div").remove();
                trObj.find(".more-op-btn-div").remove();

            } else if (TM.ActivityList.util.isMjsActivity(activityJson) == true) {

                trObj.find(".activity-op-btn-div").remove();
                trObj.find(".tmpl-btn-div").remove();
                TM.ActivityList.row.newAddMoreOpBtnsDiv(activityJson, trObj);

            } else if (TM.ActivityList.util.isShopDiscountActivity(activityJson) == true) {

                trObj.find(".item-op-btn-div").remove();
                trObj.find(".more-op-btn-div").remove();
                trObj.find(".tmpl-btn-div").remove();

            } else if (TM.ActivityList.util.isShopMjsActivity(activityJson) == true) {

                trObj.find(".item-op-btn-div").remove();
                trObj.find(".more-op-btn-div").remove();

            }



            if (activityJson.activityExecutedMillis > 0) {
                trObj.find('.execute-time-div').html('已进行' + OnActivity.row.getTimeStr(activityJson.activityExecutedMillis) + '');
            } else {
                trObj.find('.execute-time-div').html('<span style="color: #a10000;">(活动尚未开始)</span> ');
            }

            if (activityJson.activityLeftMillis > 0) {
                trObj.find('.left-time-div').html('剩余' + OnActivity.row.getTimeStr(activityJson.activityLeftMillis) + '');
            } else {
                trObj.find('.left-time-div').html('<span style="color: #a10000;">(活动已结束)</span> ');
            }


            TM.ActivityAdmin.event.initBtnEvents(trObj, activityJson);

            return trObj;
        },
        getTimeStr: function(millis) {

            var secondMillis = 1000;
            var minuteMillis = secondMillis * 60;
            var hourMillis = minuteMillis * 60;
            var dayMillis = hourMillis * 24;

            var timeStr = '';

            var dayIndex = Math.floor(millis / dayMillis);
            millis = millis - dayIndex * dayMillis;
            if (dayIndex > 0) {
                timeStr += '<span class="red-bold">' + dayIndex + '天</span>';
            }

            var hourIndex = Math.floor(millis / hourMillis);
            millis = millis - hourIndex * hourMillis;
            if (hourIndex > 0) {
                timeStr += '<span class="red-bold">' + hourIndex + '小时</span>';
            }

            if (dayIndex > 0) {
                return timeStr;
            }

            var minuteIndex = Math.floor(millis / minuteMillis);
            millis = millis - minuteIndex * minuteMillis;
            if (minuteIndex > 0) {
                timeStr += '<span class="red-bold">' + minuteIndex + '分</span>';
            }
            if (hourIndex > 0) {
                return timeStr;
            }


            var secondIndex = Math.floor(millis / secondMillis);
            millis = millis - secondIndex * secondMillis;
            if (secondIndex > 0) {
                timeStr += '<span class="red-bold">' + secondIndex + '秒</span>';
            }


            return timeStr;
        }
    }, OnActivity.row);


})(jQuery,window));





((function ($, window) {
    TM.EndActivity = TM.EndActivity || {};

    var EndActivity = TM.EndActivity;

    EndActivity.init = EndActivity.init || {};
    EndActivity.init = $.extend({
        doInit: function(container) {
            EndActivity.container = container;


        },
        getContainer: function() {

            return EndActivity.container;
        }
    }, EndActivity.init);




    EndActivity.show = EndActivity.show || {};
    EndActivity.show = $.extend({
        targetCurrentPage: 1,
        doShow: function() {
            EndActivity.show.doSearch(1);
        },
        doRefresh: function() {
            EndActivity.show.doSearch(EndActivity.show.targetCurrentPage);
        },
        doSearch: function(currentPage) {
            if (currentPage < 1) {
                currentPage = 1;
            }

            EndActivity.show.targetCurrentPage = currentPage;

            var container = EndActivity.init.getContainer();

            var paramData = EndActivity.show.getParamData();
            if (paramData === undefined || paramData == null) {
                return;
            }

            var tbodyObj = container.find(".end-activity-table").find("tbody.end-activity-tbody");

            container.find(".end-paging-div").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: paramData,
                    dataType: 'json',
                    url: '/taodiscount/listActivity',
                    callback: function(dataJson){

                        EndActivity.show.targetCurrentPage = dataJson.pn;

                        var activityJsonArray = dataJson.res;

                        tbodyObj.html("");

                        $(activityJsonArray).each(function(index, activityJson) {
                            var trObj = EndActivity.row.createRow(index, activityJson);

                            tbodyObj.append(trObj);
                        });
                    }
                }
            });

        },
        getParamData: function() {


            return TM.ActivityAdmin.util.getParamData();
        }

    }, EndActivity.show);


    EndActivity.row = EndActivity.row || {};
    EndActivity.row = $.extend({

        createRow: function(index, activityJson) {
            TM.ActivityAdmin.util.formatActivityJson(activityJson);

            var trObj = $('#endActivityRow').tmpl(activityJson);

            TM.ActivityAdmin.event.initBtnEvents(trObj, activityJson);

            return trObj;
        }
    }, EndActivity.row);


})(jQuery,window));