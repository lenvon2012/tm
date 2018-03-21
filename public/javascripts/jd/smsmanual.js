
var TM = TM || {};
((function ($, window) {
    TM.JDSmsManual = TM.JDSmsManual || {};
    var JDSmsManual = TM.JDSmsManual;
    JDSmsManual.Init = JDSmsManual.Init || {};
    JDSmsManual.Init = $.extend({
        init : function(){
            JDSmsManual.Event.setStaticEvent();
        }
    },JDSmsManual.Init);

    JDSmsManual.Event = JDSmsManual.Event || {};
    JDSmsManual.Event = $.extend({
        setStaticEvent : function(){
            JDSmsManual.Event.setNumberInputEvent();
            JDSmsManual.Event.setChongzhiBtnClickEvent();
            JDSmsManual.Event.setTxtNumbersFocusEvent();
            JDSmsManual.Event.setTxtSmsContentChangeEvent();
            JDSmsManual.Event.setTxtSmsContentBlurEvent();
            JDSmsManual.Event.setFilierCheckonClickEvent();
        },
        setNumberInputEvent : function(){
            $('input[name="radList"]').click(function(){
                if($(this).val() == 0){
                    $('#fileTr').fadeOut(100);
                    $('#MobileTr').fadeIn(1000);
                    $('#MobileNum').fadeIn(1000);
                } else if($(this).val() == 1){
                    $('#fileTr').fadeIn(100);
                    $('#MobileTr').fadeOut(1000);
                    $('#MobileNum').fadeOut(1000);
                }
            });
        },
        setChongzhiBtnClickEvent : function(){
            $('.chongzhi-now').click(function(){
                TM.Alert.load("亲，该功能还在开发中~");
            });
        },
        setTxtNumbersFocusEvent : function(){
            $('#txtNumbers').focus();
            $('#txtNumbers').keypress(function(e){
                if(e.which == 13){
                    var content = $('#txtNumbers').val();
                    var length = content.split('\n').length;
                    $('#mobileCount').text(length);
                }
            });
        },
        setTxtSmsContentChangeEvent : function(){
            $('#txtSmsContent').inputlimitor({
                limit: 250,
                boxId: "remainCount",
                remText: '剩余字数<span class="newRemainLength">%n</span>',
                limitText: '/%n字(包含签名)'
            });
            $("#txtSmsContent").keyup();//这样初始化标题剩余长度
        },
        setTxtSmsContentBlurEvent : function(){
            $('#txtSmsContent').blur(function(){
                var smscount =  Math.ceil(JDSmsManual.Util.countCharacters($(this).val())/136);
                $('#smscount').text(smscount);
            });
        },
        setFilierCheckonClickEvent : function(){
            $('#checkon').click(function(){
                if($('#checkon:checked').length == 1){
                    $('#selecttime').attr("disabled",false);
                } else {
                    $('#selecttime').attr("disabled",true);
                }
            });
        }
    },JDSmsManual.Event);

    JDSmsManual.Util = JDSmsManual.Util || {};
    JDSmsManual.Util = $.extend({
        countCharacters : function(str){
            var totalCount = 0;
            for (var i=0; i<str.length; i++) {
                var c = str.charCodeAt(i);
                if ((c >= 0x0001 && c <= 0x007e) || (0xff60<=c && c<=0xff9f)) {
                    totalCount++;
                }else {
                    totalCount+=2;
                }
            }
            return totalCount;
        }
    },JDSmsManual.Util);
})(jQuery,window));