

TM.AdminUtil = TM.AdminUtil || {};
((function ($, window) {

    /**
     * namespace
     * @type {*}
     */
    var AdminUtil = TM.AdminUtil;


    AdminUtil.common = AdminUtil.common || {};

    /**
     * 初始化
     * @type {*}
     */
    AdminUtil.common = $.extend({
        //判断ajax返回结果
        judgeAjaxResult: function(dataJson) {

            var msg = dataJson.message;
            if (msg === undefined || msg == null || msg == "")
                ;
            else
                alert(msg);
            return dataJson.success;
        }
    }, AdminUtil.common);


})(jQuery, window));
