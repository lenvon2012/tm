
((function ($, window) {

    $(document).ready(function () {
        var leftTdObj = $(".nav-table-left-td");

        var asideObj = leftTdObj.find(".aside");
        asideObj.html("");

        var html = '' +
            '<ul class="nav nav-list-fix">' +
            '   <li class="nav-header" style="padding-bottom:3px;border-bottom:1px solid #ccc;">行业数据分析</li>' +
            '   <li class="aside-hot-tag award-li ">' +
            '       <div class="wrapper ">' +
            '           <a class="clearfix" href="/delistsearch/index" style="color: #000000;">' +
            '               <span class="aside-icon"></span>行业上下架分析' +
            '           </a>' +
            '       </div>' +
            '   </li>' +
            /*'   <li class="aside-hot-tag award-li ">' +
            '       <div class="wrapper ">' +
            '           <a class="clearfix" href="/delistsearch/item" style="color: #000000;">' +
            '               <span class="aside-icon"></span>宝贝上下架查询' +
            '           </a>' +
            '       </div>' +
            '   </li>' +*/
            '   <li class="aside-hot-tag award-li ">' +
            '       <div class="wrapper ">' +
            '           <a class="clearfix" href="/searchindustry/index" style="color: #000000;">' +
            '               <span class="aside-icon"></span>行业宝贝价格分析' +
            '           </a>' +
            '       </div>' +
            '   </li>' +
            '</ul>' +
            '' +
            '';

        asideObj.html(html);

        asideObj.show();

        leftTdObj.show();
    });

})(jQuery,window));



((function ($, window) {

    TM.IndustryBase = TM.IndustryBase || {};
    TM.IndustryBase = $.extend({
        judgeAjaxResult: function(resultJson) {
            var msg = resultJson.message;
            if (msg === undefined || msg == null || msg == "") {
                msg = resultJson.msg;
            }
            if (msg === undefined || msg == null || msg == "") {

            } else {
                alert(msg);
            }
            var isSuccess = resultJson.success;
            if (isSuccess === undefined || isSuccess == null) {
                isSuccess = resultJson.isOk;
            }

            return isSuccess;
        },
        getAjaxResult: function(resultJson) {
            var json = resultJson.results;
            if (json === undefined || json == null) {
                json = resultJson.res;
            }
            if(!json && resultJson && resultJson.length > 0){
                json = resultJson;
            }

            return json;
        },
        parseRelativeTime: function(relativeTime) {
            var secondMillis = 1000;
            var minuteMillis = 60 * secondMillis;
            var hourMillis = minuteMillis * 60;
            var dayMillis = 24 * hourMillis;


            if (relativeTime < 0) {
                return "-";
            }

            //星期
            var dayArray = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
            var dayIndex = Math.floor(relativeTime / dayMillis);

            if (dayIndex < 0 || dayIndex >= dayArray.length) {
                return "-";
            }

            relativeTime = relativeTime - dayIndex * dayMillis;

            //小时
            var hourIndex = Math.floor(relativeTime / hourMillis);
            relativeTime = relativeTime - hourIndex * hourMillis;

            //分
            var minuteIndex = Math.floor(relativeTime / minuteMillis);
            relativeTime = relativeTime - minuteIndex * minuteMillis;

            //秒
            var secondIndex = Math.floor(relativeTime / secondMillis);

            if (hourIndex < 10) {
                hourIndex = "0" + hourIndex;
            }
            if (minuteIndex < 10) {
                minuteIndex = "0" + minuteIndex;
            }
            if (secondIndex < 10) {
                secondIndex = "0" + secondIndex ;
            }

            var timeStr = dayArray[dayIndex] + " " + hourIndex + ":" + minuteIndex + ":" + secondIndex;
            return timeStr;

        }

    }, TM.IndustryBase);

})(jQuery,window));