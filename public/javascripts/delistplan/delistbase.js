var TM = TM || {};

((function ($, window) {

    TM.DelistBase = TM.DelistBase || {};

    var DelistBase = TM.DelistBase;

    DelistBase.util = DelistBase.util || {};
    DelistBase.util = $.extend({
        judgeAjaxResult: function(resultJson) {
            var msg = resultJson.message;
            if (msg === undefined || msg == null || msg == "") {

            } else {
                alert(msg);
            }
            return resultJson.success;
        },
        parseSecondTime: function(millis) {

            var theDate = new Date();
            var offset = theDate.getTimezoneOffset() * 1000 * 60;


            var baseOffset = -480;//东8区

            var realTime = millis - baseOffset * 1000 * 60 + offset;

            theDate = new Date(realTime);

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
        }
    }, DelistBase.util);


})(jQuery,window));