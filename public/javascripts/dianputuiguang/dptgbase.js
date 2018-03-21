var TM = TM || {};
((function ($, window) {
    TM.DptgBase = TM.DptgBase || {};

    var DptgBase = TM.DptgBase;

    DptgBase.init = DptgBase.init || {};
    DptgBase.init = $.extend({
        doInit: function() {

        }
    }, DptgBase.init);

    DptgBase.util = DptgBase.util || {};
    DptgBase.util = $.extend({
        judgeAjaxResult: function(dataJson) {
            var message = dataJson.msg;
            if (message === undefined || message == null || message == "") {
                message = dataJson.message;
            }
            if (message === undefined || message == null || message == "") {

            } else {
                alert(message);
            }
            var isSuccess = dataJson.isOk;
            if (isSuccess === undefined || isSuccess == null) {
                isSuccess = dataJson.success;
            }

            return isSuccess;
        },
        getPopularizedUrl: function(itemJson) {
            //var showHref = "http://www.iailegou.com/index.php?a=index&m=item&id=" + itemJson.vgItemId;
            //return showHref;

            var href = "/taoweigou/detail?numIid=" + itemJson.numIid;
            href += "&ll=2185480046";

            return href;
        },
        createXufei : function(level, timeLeft){
            if(level <= -1) {
                return $('<div class="xufei inlineblock ie6fixedBR" style="display: block;">亲,您的服务将在<span style="color: red;font-size: 32px;">'+timeLeft+'</span>天后到期，请尽快续订哦!!' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/xW3Blhy" target="_blank">12个月/<span style="font-size: 24px;color: white;">3</span>元</a></span>' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/SA2Blhy" target="_blank">6个月/<span style="font-size: 24px;color: white;">3</span>元</a></span>' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/XF1Blhy" target="_blank">3个月/<span style="font-size: 24px;color: white;">1</span>元</a></span>' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/tL7Blhy" target="_blank">1个月/<span style="font-size: 24px;color: white;">0.5</span>元</a></span>' +
                    '</div>');

            } else if(level <= 1) {
                return $('<div class="xufei inlineblock ie6fixedBR" style="display: block;">亲,您的服务将在<span style="color: red;font-size: 32px;">'+timeLeft+'</span>天后到期，请尽快续订哦!!' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/xuuAlhy" target="_blank">12个月/<span style="font-size: 24px;color: white;">80</span>元</a></span>' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/jYtAlhy" target="_blank">6个月/<span style="font-size: 24px;color: white;">40</span>元</a></span>' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/mhtAlhy" target="_blank">3个月/<span style="font-size: 24px;color: white;">20</span>元</a></span>' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/j1tAlhy" target="_blank">1个月/<span style="font-size: 24px;color: white;">10</span>元</a></span>' +
                    '</div>');
            } else if(level <= 20) {
                return $('<div class="xufei inlineblock ie6fixedBR" style="display: block;">亲,您的服务将在<span style="color: red;font-size: 32px;">'+timeLeft+'</span>天后到期，请尽快续订哦!!' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/86sAlhy" target="_blank">12个月/<span style="font-size: 24px;color: white;">120</span>元</a></span>' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/pHsAlhy" target="_blank">6个月/<span style="font-size: 24px;color: white;">80</span>元</a></span>' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/AkrAlhy" target="_blank">3个月/<span style="font-size: 24px;color: white;">40</span>元</a></span>' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/jrqAlhy" target="_blank">1个月/<span style="font-size: 24px;color: white;">25</span>元</a></span>' +
                    '</div>');
            } else if(level <= 30) {
                return $('<div class="xufei inlineblock ie6fixedBR" style="display: block;">亲,您的服务将在<span style="color: red;font-size: 32px;">'+timeLeft+'</span>天后到期，请尽快续订哦!!' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/wtpAlhy" target="_blank">12个月/<span style="font-size: 24px;color: white;">250</span>元</a></span>' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/S2qAlhy" target="_blank">6个月/<span style="font-size: 24px;color: white;">160</span>元</a></span>' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/PfpAlhy" target="_blank">3个月/<span style="font-size: 24px;color: white;">80</span>元</a></span>' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/ZZoAlhy" target="_blank">1个月/<span style="font-size: 24px;color: white;">40</span>元</a></span>' +
                    '</div>');
            } else if(level <= 40) {
                return $('<div class="xufei inlineblock ie6fixedBR" style="display: block;">亲,您的服务将在<span style="color: red;font-size: 32px;">'+timeLeft+'</span>天后到期，请尽快续订哦!!' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/L1nAlhy" target="_blank">12个月/<span style="font-size: 24px;color: white;">480</span>元</a></span>' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/sQnAlhy" target="_blank">6个月/<span style="font-size: 24px;color: white;">240</span>元</a></span>' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/q5nAlhy" target="_blank">3个月/<span style="font-size: 24px;color: white;">120</span>元</a></span>' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/7EmAlhy" target="_blank">1个月/<span style="font-size: 24px;color: white;">68</span>元</a></span>' +
                    '</div>');
            } else if(level <= 50) {
                return $('<div class="xufei inlineblock ie6fixedBR" style="display: block;">亲,您的服务将在<span style="color: red;font-size: 32px;">'+timeLeft+'</span>天后到期，请尽快续订哦!!' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/zEkAlhy" target="_blank">12个月/<span style="font-size: 24px;color: white;">1000</span>元</a></span>' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/6kjAlhy" target="_blank">6个月/<span style="font-size: 24px;color: white;">580</span>元</a></span>' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/HniAlhy" target="_blank">3个月/<span style="font-size: 24px;color: white;">300</span>元</a></span>' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/NRjAlhy" target="_blank">1个月/<span style="font-size: 24px;color: white;">158</span>元</a></span>' +
                    '</div>');
            } else  {
                return $('<div class="xufei inlineblock ie6fixedBR" style="display: block;">亲,您的服务将在<span style="color: red;font-size: 32px;">'+timeLeft+'</span>天后到期，请尽快续订哦!!' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/gqeAlhy" target="_blank">12个月/<span style="font-size:24px;color: white;">1500</span>元</a></span>' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/ACeAlhy" target="_blank">6个月/<span style="font-size: 24px;color: white;">800</span>元</a></span>' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/lrdAlhy" target="_blank">3个月/<span style="font-size: 24px;color: white;">400</span>元</a></span>' +
                    '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/wZdAlhy" target="_blank">1个月/<span style="font-size: 24px;color: white;">228</span>元</a></span>' +
                    '</div>');
            }
        }
    }, DptgBase.util);

    DptgBase.xufei = DptgBase.xufei || {};
    DptgBase.xufei = $.extend({
        showXufei : function(level) {
            if(level != null) {
                $.getScript("/Status/js",function(data){
                    if(parseInt(TM.timeLeft) < 15) {
                        $('body').append(DptgBase.util.createXufei(level,TM.timeLeft));
                    }
                });
            }
        }
    },DptgBase.xufei);
})(jQuery,window));