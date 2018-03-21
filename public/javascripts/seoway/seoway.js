((function ($, window) {
    var SEO = SEO || {};
    SEO.main = SEO.main || {};
    var me = SEO.main;
    SEO.init = function(){
        me.sellercatInput = $("#sellercat");
        me.itemcatInput = $("#itemcat");
        me.statusInput = $("#approvalstatus");
        $.get('/SeoWay/sellerCats',function(list){
            me.sellercatInput.empty();
            $.each(list, function(i, elem){
               me.sellercatInput.append('<option value="'+elem.cid+'">'+elem.name +'('+elem.count+')</option>');
            });
        });
        $.get('/SeoWay/itemCats',function(list){
            me.itemcatInput.empty();
            $.each(list, function(i, elem){
                me.itemcatInput.append('<option value="'+elem.cid+'">'+elem.name +'('+elem.count+')</option>');
            });
        });
    }
})(jQuery, window));