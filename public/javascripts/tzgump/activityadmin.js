var TM = TM || {};
((function ($, window) {
    TM.ActivityAdmin = TM.ActivityAdmin || {};

    var ActivityAdmin = TM.ActivityAdmin;

    ActivityAdmin.init = ActivityAdmin.init || {};
    ActivityAdmin.init = $.extend({
        doInit: function(container) {
            ActivityAdmin.container = container;

            container.find(".create-activity-btn").click(function() {
                window.location.href = '/tzgumpactivity/adddiscount';
                return true;
            });

            var tabBtnObjs = container.find(".activity-tab-btn");
            tabBtnObjs.click(function() {
                tabBtnObjs.removeClass("selected");
                $(this).addClass("selected");

                ActivityAdmin.load.doLoad();
            });

            ActivityAdmin.load.doLoad();

        },
        getContainer: function() {
            return ActivityAdmin.container;
        }
    }, ActivityAdmin.init);


    ActivityAdmin.load = ActivityAdmin.load || {};
    ActivityAdmin.load = $.extend({
        doLoad: function() {
            var container = ActivityAdmin.init.getContainer();

            var selectTabObj = container.find(".activity-tab-btn.selected");

            if (selectTabObj.attr("tag") == "Active") {
                container.find(".unactive-activity-div").hide();
                container.find(".active-activity-div").show();

                ActivityAdmin.show.doShow(true);

            } else {
                container.find(".active-activity-div").hide();
                container.find(".unactive-activity-div").show();

                ActivityAdmin.show.doShow(false);
            }
        },
        doReload: function() {
            ActivityAdmin.load.doLoad();
        }
    }, ActivityAdmin.load);


    ActivityAdmin.show = ActivityAdmin.show || {};
    ActivityAdmin.show = $.extend({
        isActive: true,
        doShow: function(isActive) {
            ActivityAdmin.show.doSearch(isActive);
        },
        doSearch: function(isActive) {

            ActivityAdmin.show.isActive = isActive;

            var container = ActivityAdmin.init.getContainer();

            var tbodyObj = null;
            if (isActive == true) {
                tbodyObj = container.find(".active-activity-div .activity-table .activity-tbody");
            } else {
                tbodyObj = container.find(".unactive-activity-div .activity-table .activity-tbody");
            }


            tbodyObj.find(".no-activity-tr").hide();
            tbodyObj.find(".activity-row").remove();

            var paramData = {};
            if (isActive == true) {
                paramData.isactive = 1;
            } else {
                paramData.isactive = 2;
            }

            $.ajax({
                type: "post",
                url: "/taodiscount/listActivity",
                data: paramData,
                success: function(dataJson){
                    if (TM.UmpUtil.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    tbodyObj.find(".no-activity-tr").hide();
                    tbodyObj.find(".activity-row").remove();

                    var activityJsonArray = TM.UmpUtil.util.getAjaxResultJson(dataJson);

                    if (activityJsonArray === undefined || activityJsonArray == null || activityJsonArray.length <= 0) {
                        tbodyObj.find(".no-activity-tr").show();
                        return;
                    }

                    $(activityJsonArray).each(function(index, activityJson) {
                        var trObj = ActivityAdmin.row.createRow(index, activityJson, isActive);
                        tbodyObj.append(trObj);
                    });
                }
            });


        }
    }, ActivityAdmin.show);



    ActivityAdmin.row = ActivityAdmin.row || {};
    ActivityAdmin.row = $.extend({
        createRow: function(index, activityJson, isActive) {

            var trObj = null;
            if (isActive == true) {
                trObj = $("#activeActivityRow").tmpl(activityJson);
            } else {
                trObj = $("#unActiveActivityRow").tmpl(activityJson);
            }

            trObj.find(".add-item-btn").click(function() {
                window.location.href = '/home/tbtDazheItems?activityId=' + activityJson.id;
                return true;
            });
            trObj.find(".modify-activity-btn").click(function() {
                window.location.href = '/tzgumpactivity/modifydiscount?activityId=' + activityJson.id;
                return true;
            });


            trObj.find(".cancel-activity-btn").unbind().click(function(){
                if(confirm('确实要结束该活动吗？结束的活动，可以在已结束活动列表找到。') == false) {
                    return;
                }

                $.ajax({
                    url : '/TzgUmpActivity/cancelActivity',
                    data : {tmActivityId: activityJson.id},
                    type : 'post',
                    success : function(dataJson) {

                        if (TM.UmpUtil.util.checkIsW2AuthError(dataJson) == true) {
                            return;
                        }

                        if (TM.UmpUtil.util.judgeAjaxResult(dataJson) == false) {
                            return;
                        }

                        var resultJson = TM.UmpUtil.util.getAjaxResultJson(dataJson);


                        TM.UmpUtil.errors.showErrors($("body"), resultJson, function() {

                            //如果没有失败
                            //window.location.href ="/TaoDiscount/index";
                            //location.reload();

                            ActivityAdmin.load.doReload();

                            return;

                        }, function() {


                        });


                    }
                });
            }) ;
            trObj.find(".delete-activity-btn").unbind().click(function(){

                if(confirm('确实要删除该活动吗？删除后将无法恢复。') == false) {
                    return;
                }

                $.ajax({
                    url : '/umpactivity/deleteActivity',
                    data : {tmActivityId: activityJson.id},
                    type : 'post',
                    success : function(dataJson) {

                        if (TM.UmpUtil.util.judgeAjaxResult(dataJson) == false) {
                            return;
                        }
                        alert("活动删除成功！");
                        trObj.remove();

                    }
                });
            }) ;

            return trObj;

        }
    }, ActivityAdmin.row);


})(jQuery,window));

