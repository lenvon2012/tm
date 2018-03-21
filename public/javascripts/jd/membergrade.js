var TM = TM || {};

((function ($, window) {
    TM.MemberGrade = TM.MemberGrade || {};
    var MemberGrade = TM.MemberGrade;

    MemberGrade.init = MemberGrade.init || {};
    MemberGrade.init = $.extend({
        doInit: function(container) {
            MemberGrade.container = container;

            $.ajax({
                url : '/JDMemberGrade/queryMemberGrade',
                data : {},
                type : 'post',
                success : function(dataJson) {
                    if (TM.MyCrmBase.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    var gradeJson = dataJson.res;
                    if (gradeJson === undefined || gradeJson == null) {
                        container.find(".trade-amount").val("0.00");
                        container.find(".trade-count").val("0");
                    } else {
                        container.find(".normal-trade-amount").val(gradeJson.normalTradeAmount);
                        container.find(".normal-trade-count").val(gradeJson.normalTradeCount);

                        container.find(".advance-trade-amount").val(gradeJson.advanceTradeAmount);
                        container.find(".advance-trade-count").val(gradeJson.advanceTradeCount);

                        container.find(".vip-trade-amount").val(gradeJson.vipTradeAmount);
                        container.find(".vip-trade-count").val(gradeJson.vipTradeCount);

                        container.find(".god-trade-amount").val(gradeJson.godTradeAmount);
                        container.find(".god-trade-count").val(gradeJson.godTradeCount);
                    }
                }
            });

            container.find(".modify-grade-config-btn").click(function() {
                MemberGrade.submit.doSetGrade();
            });
        }
    }, MemberGrade.init);


    MemberGrade.submit = MemberGrade.submit || {};
    MemberGrade.submit = $.extend({
        doSetGrade: function() {
            if (confirm("确定要修改客户等级设置？") == false) {
                return;
            }

            var params = {};
            var container = MemberGrade.container;
            params.normalTradeAmount = container.find(".normal-trade-amount").val();
            params.normalTradeCount = container.find(".normal-trade-count").val();

            params.advanceTradeAmount = container.find(".advance-trade-amount").val();
            params.advanceTradeCount = container.find(".advance-trade-count").val();

            params.vipTradeAmount = container.find(".vip-trade-amount").val();
            params.vipTradeCount = container.find(".vip-trade-count").val();

            params.godTradeAmount = container.find(".god-trade-amount").val();
            params.godTradeCount = container.find(".god-trade-count").val();

            $.ajax({
                url : '/JDMemberGrade/setMemberGrade',
                data : params,
                type : 'post',
                success : function(dataJson) {
                    if (TM.MyCrmBase.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    location.reload();
                }
            });

        }
    }, MemberGrade.submit);


})(jQuery,window));


((function ($, window) {
    TM.MyCrmBase = TM.MyCrmBase || {};
    var MyCrmBase = TM.MyCrmBase;

    MyCrmBase.util = MyCrmBase.util || {};
    MyCrmBase.util = $.extend({
        judgeAjaxResult: function(dataJson) {
            var message = dataJson.msg;
            if (message === undefined || message == null || message == "") {

            } else {
                alert(message);
            }
            return dataJson.isOk;
        }
    }, MyCrmBase.util);



})(jQuery,window));