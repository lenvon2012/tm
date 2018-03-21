var TM = TM || {};
((function ($, window) {

    TM.SkinBatch = TM.SkinBatch || {};

    var SkinBatch = TM.SkinBatch;

    /**
     * 初始化
     * @type {*}
     */
    SkinBatch.init = SkinBatch.init || {};
    SkinBatch.init = $.extend({
        doInit: function(container) {
            SkinBatch.container = container;
        }
    }, SkinBatch.init);

    /**
     * 操作失败的日志
     * @type {*}
     */
    SkinBatch.error = SkinBatch.error || {};
    SkinBatch.error = $.extend({
        showErrors: function(errorJsonArray) {
            if (errorJsonArray === undefined || errorJsonArray == null || errorJsonArray.length == 0){
                alert("操作成功！");
                SkinBatch.show.refresh();
                return ;
            }
            alert("操作有误，请查看最下方的错误列表！");
            SkinBatch.container.find(".error-item-div").show();
            $(errorJsonArray).each(function(index, errorJson) {
                var trObj = SkinBatch.error.createRow(index, errorJson);
                SkinBatch.container.find(".error-item-table").find("tbody").append(trObj);
            });
        },
        createRow: function(index, errorJson) {
            var itemJson = errorJson.item;
            var errorResult = errorJson.errorMessage;
            var html = '' +
                '<tr>' +
                '   <td><a class="item-link" target="_blank"><img class="item-img" /> </a> </td>' +
                '   <td><a class="item-link" target="_blank"><span class="item-title"></span> </a> </td>' +
                '   <td><span class="error-intro"></span> </td>' +
                '   <td><span class="op-time"></span> </td>' +
                '</tr>' +
                '' +
                '' +
                '';
            var trObj = $(html);
            var url = "http://auction2.paipai.com/" + itemJson.itemCode;
            trObj.find(".item-link").attr("href", url);
            trObj.find(".item-img").attr("src", itemJson.picLink);
            trObj.find(".item-title").html(itemJson.itemName);
            trObj.find(".error-intro").html(errorResult);

            var theDate = new Date();
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
            var timeStr = year + "-" + month+"-"+date+" "+hour+":"+minutes+ ":" + second;
            trObj.find(".op-time").html(timeStr);

            return trObj;
        }
    }, SkinBatch.error);

})(jQuery,window));