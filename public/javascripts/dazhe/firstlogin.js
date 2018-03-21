/**
 * Created with IntelliJ IDEA.
 * User: haoyongzh
 * Date: 13-10-3
 * Time: 下午4:53
 * To change this template use File | Settings | File Templates.
 */
var TM = TM || {};
((function ($, window) {
    TM.FirstLogin = TM.FirstLogin || {};

    var FirstLogin = TM.FirstLogin;

    FirstLogin.init = FirstLogin.init || {};
    FirstLogin.init = $.extend({
        doInit: function(container) {
            FirstLogin.container = container;
            FirstLogin.init.checkFirst();
        },
        checkFirst : function(){
            $.get("/paipaidiscount/isfirstlogin",function(data){
                  if(data.res==true){
                      TM.Alert.load('<p style="font-size:14px">亲，系统检测到您是第一次登陆，请点击确定，系统会开始为您同步商品，库存和订单数据' +
                          '！可能需要一段时间，请耐心等待！</p>',300,230,function(){
                          FirstLogin.init.showFirst();
                      });
                  }
            });
        },
        showFirst  : function(){
            FirstLogin.container.html();
            var html1='<div class="WaitingForLoadingDiv" style="display: none;font-size: 16px;width:500px;font-weight: bolder;color: #ffffff;box-shadow: 0 1px 1px #444;    border-radius: 10px;background-color: #262626;padding: 20px 10px 10px 10px;z-index: 200001">' +

            '亲~系统正在为您努力加载宝贝数据...请您稍等片刻...<br />     <br />'+
                '<img src="/img/catunion/loading.gif" alt="正在加载" />'+
                '</div>';
            $.get('/paipaiitems/sync',function(){
            });
            $.get('/paipaiitems/tradeSync',function(){
            });
        }

    },FirstLogin.init);

})(jQuery,window));
