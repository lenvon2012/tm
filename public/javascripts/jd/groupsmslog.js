
var TM = TM || {};
((function ($, window) {
    TM.JDGroupSmsLog = TM.JDGroupSmsLog || {};
    var JDGroupSmsLog = TM.JDGroupSmsLog;
    JDGroupSmsLog.Init = JDGroupSmsLog.Init || {};
    JDGroupSmsLog.Init = $.extend({
        init : function(){
            JDGroupSmsLog.Event.setStaticEvent();
        }
    },JDGroupSmsLog.Init);

    JDGroupSmsLog.Event = JDGroupSmsLog.Event || {};
    JDGroupSmsLog.Event = $.extend({
        setStaticEvent : function(){
            JDGroupSmsLog.Event.setDatePickerEvent();
        },
        setDatePickerEvent : function(){
            $("#txtLeftTime").datepicker();
            $("#txtRightTime").datepicker();
        }
    },JDGroupSmsLog.Event);
})(jQuery,window));