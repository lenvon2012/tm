var TM = TM || {};
((function ($, window) {

    TM.FenXiang = TM.FenXiang || {};

    var FenXiang = TM.FenXiang;

    /**
     * 初始化
     * @type {*}
     */
    FenXiang.init = FenXiang.init || {};
    FenXiang.init = $.extend({
        doInit: function(container) {
            FenXiang.container = container;
            var numIid=FenXiang.container.find(".numIid").val();
            var itemURL="http://item.taobao.com/item.htm?id=" + numIid;
            var title=FenXiang.container.find(".title").val();
            FenXiang.container.find(".statuses").val(title+"     宝贝链接："+itemURL);
            $.ajax({
                url : '/Popularize/isBD',
                data : {},
                type : 'post',
                success : function(data) {
                    FenXiang.init.setEvent(data);
                    FenXiang.init.fenxiang(data);
                }
            });

        } ,
        setEvent:function(data){
            if(TM.isSweiboBD==true){
                FenXiang.container.find(".sweiboBD").html("解除");
                FenXiang.container.find(".sweiboBD").click(function(){
                    $.ajax({
                        url : '/Popularize/removeWeiboBD',
                        data : {},
                        type : 'post',
                        success : function(data) {
                        }
                    })
                })
            }
            else {
                FenXiang.container.find(".sweiboBD").html("绑定");
                FenXiang.container.find(".sweiboBD").click(function(){
                    $.ajax({
                        url : '/Popularize/weibobangding',
                        data : {},
                        type : 'post',
                        success : function(data) {
                        }
                    })
                })
            }
        },
        fenxiang: function(data){
            FenXiang.container.find(".fenxiang").click(function(){
                var statuses=  FenXiang.container.find(".statuses").val();
                var title=FenXiang.container.find(".title").val();
                var picURL=FenXiang.container.find(".picURL").val();
                if(TM.isSweiboBD==true){
                    $.ajax({
                        url : '/Popularize/weibofenxiang',
                        data : {statuses:statuses,title:title,picURL:picURL},
                        type : 'post',
                        success : function(data) {
                            alert("新浪微博分享成功");
                        }
                    })
                }
            })
        }
    },FenXiang.init);

})(jQuery,window));