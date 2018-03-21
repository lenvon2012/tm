
var TM = TM || {};
((function ($, window) {
    TM.JDSmsManualLog = TM.JDSmsManualLog || {};
    var JDSmsManualLog = TM.JDSmsManualLog;
    JDSmsManualLog.Init = JDSmsManualLog.Init || {};
    JDSmsManualLog.Init = $.extend({
        init : function(){
            JDSmsManualLog.Event.setStaticEvent();
        },
        createLogTable : function(data) {

        }
    },JDSmsManualLog.Init);

    JDSmsManualLog.Event = JDSmsManualLog.Event || {};
    JDSmsManualLog.Event = $.extend({
        setStaticEvent : function(){
            JDSmsManualLog.Event.setDatePickerEvent();
            JDSmsManualLog.Event.setSearchLogBtnClickEvent();
        },
        setDatePickerEvent : function(){
            $("#txtLeftTime").datepicker();
            $("#txtRightTime").datepicker();
        },
        setSearchLogBtnClickEvent : function(){
            $('#search-log').click(function(){
                var txtLeftTime = $('#txtLeftTime').val();
                var txtRightTime = $('#txtRightTime').val();
                if(txtLeftTime == ""){
                    TM.Alert.load("亲，请选择查询起始时间段");
                } else {

                             $('.sms-manual-log-paging').tmpage({
                                 currPage: 1,
                                 pageSize: 10,
                                 pageCount: 1,
                                 ajax: {
                                     on: true,
                                     param: {txtLeftTime:txtLeftTime,txtRightTime:txtRightTime},
                                     dataType: 'json',
                                     url: '/JDServer/smsmanuallogData',
                                     callback: function(dataJson){
                                         if(!dataJson || !dataJson.isOk){
                                             TM.Alert.load("数据获取发生错误，请重试或联系客服");
                                         } else {
                                             var trObjs = JDSmsManualLog.Init.createLogTable(dataJson);
                                         }
                                     }
                                 }

                             });


                }
            });
            var left_date = new Date(new Date().getTime()-7*24*3600*1000).format("yyyy-MM-dd");
            $('#txtLeftTime').val(left_date);
            $('#search-log').trigger("click");
        }
    },JDSmsManualLog.Event);
})(jQuery,window));