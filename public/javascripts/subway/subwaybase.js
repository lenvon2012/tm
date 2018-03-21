var TM = TM || {};

((function ($, window) {

    TM.SubwayBase = TM.SubwayBase || {};

    var SubwayBase = TM.SubwayBase;

    SubwayBase.util = SubwayBase.util || {};
    SubwayBase.util = $.extend({
        judgeAjaxResult: function(resultJson) {
            var msg = resultJson.message;
            if (msg === undefined || msg == null || msg == "") {

            } else {
                alert(msg);
            }
            return resultJson.success;
        },
        formatToPercent: function(num){
            var posLength = 2;
            return ( Math.round(num * Math.pow(10, posLength + 2)) / Math.pow(10, posLength) ).toFixed(posLength) + '%';
        },
        judgeUserBusVersion: function(callback) {

            $.ajax({
                type: "post",
                url: "/buscampaign/queryUser",
                data: {},
                success: function(dataJson){
                    if (TM.SubwayBase.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }

                    var userJson = dataJson.results;

                    if (userJson.version < 30) {//开车版

                        var html = '' +
                            '<div style="font-size: 18px;padding-top: 20px;"> ' +
                            '   亲，该页面当前只允许培训版用户使用，您可以' +
                            '   <a href="http://fuwu.taobao.com/ser/detail.htm?spm=a1z13.1113643.51048019.45.O2X5J0&service_code=ts-1820059&tracelog=category&scm=1215.1.1.51048019&ppath=&labels=" target="_blank" style="color: #a10000;font-weight: bold;font-size: 18px;">立即升级</a> ' +
                            '</div>';

                        TM.Alert.showDialog(html, 400, 300, function() {}, function() {}, "提示");
                    } else {
                        $('.contract-kefu').show();
                        callback();
                    }
                }
            });



        }
    }, SubwayBase.util);


})(jQuery,window));