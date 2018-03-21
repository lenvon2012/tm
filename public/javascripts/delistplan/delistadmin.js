var TM = TM || {};

((function ($, window) {

    TM.DelistAdmin = TM.DelistAdmin || {};

    var DelistAdmin = TM.DelistAdmin;

    DelistAdmin.init = DelistAdmin.init || {};
    DelistAdmin.init = $.extend({
        doInit: function(container) {
            DelistAdmin.container = container;

            DelistAdmin.show.doShow();
        }
    }, DelistAdmin.init);


    DelistAdmin.show = DelistAdmin.show || {};
    DelistAdmin.show = $.extend({
        doShow: function() {
            var container = DelistAdmin.container;
            $.ajax({
                type: "post",
                url: "/delistplan/queryDelistPlanList",
                data: {},
                success: function(dataJson){
                    if (TM.DelistBase.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }

                    var planJsonArray = dataJson.results;
                    var tbodyObj = container.find(".delist-plan-table tbody");


                    if (planJsonArray === undefined || planJsonArray == null || planJsonArray.length <= 0) {
                        var trHtml = '' +
                            '<tr class="no-delist-plan-tr">' +
                            '   <td colspan="7">您尚未创建上下架计划，立即点击创建吧！</td>' +
                            '</tr> ' +
                            '';
                        tbodyObj.html(trHtml);
                    } else {
                        tbodyObj.html("");
                        $(planJsonArray).each(function(index, planJson) {
                            var trObj = DelistAdmin.row.createRow(index, planJson);
                            tbodyObj.append(trObj);
                        });
                    }
                }
            });
        }
    }, DelistAdmin.show);


    DelistAdmin.row = DelistAdmin.row || {};
    DelistAdmin.row = $.extend({
        createRow: function(index, planJson) {

            var createTime = TM.DelistBase.util.parseSecondTime(planJson.createTime);
            var updateTime = TM.DelistBase.util.parseSecondTime(planJson.updateTime);
            var status = "计划已开启";

            var html = '' +
                '<tr>' +
                '   <td>' + (index + 1) + '</td>' +
                '   <td>' + planJson.title + '</td>' +
                '   <td>' + createTime + '</td>' +
                '   <td>' + updateTime + '</td>' +
                '   <td>' + status + '</td>' +
                '   <td></td>' +
                '</tr>' +
                '';

            var trObj = $(html);
            return trObj;
        }
    }, DelistAdmin.row);


})(jQuery,window));