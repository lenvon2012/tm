((function ($, window) {
    var Shoucang = Shoucang || {};
    TM.shoucang = Shoucang;
    Shoucang.addToBaidu = Shoucang.addToBaidu || {};
    Shoucang.addToBaidu = $.extend({
        add : function(params){
            var params = $.extend({
                title : "",
                url : "",
                content : "测试用例"
            },params);
            var url = 'http://cang.baidu.com/do/add?it='+params.title+'&iu='+params.url+'&dc='+params.content+'&fr=ien#nw=1';
            window.open(url,'_blank','scrollbars=no,width=600,height=450,left=75,top=20,status=no,resizable=yes');

        },
        remove : function(params){
            var params = $.extend({
                title : "",
                url : "",
                content : ""
            },params);
            var url = 'http://cang.baidu.com/do/cm?iid=7ed8e96fd964b6eae12bdd5b&ct=7';
            window.open(url,'_blank','scrollbars=no,width=600,height=450,left=75,top=20,status=no,resizable=yes');
        }
    },Shoucang.addToBaidu);


})(jQuery, window));


