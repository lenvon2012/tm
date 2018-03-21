/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 3/31/13
 * Time: 11:21 PM
 * To change this template use File | Settings | File Templates.
 */
((function ($, window) {
    var Popsites = Popsites || {};
    Popsites.Init = Popsites.Init || {};
    Popsites.Init = $.extend({
        init : function(container){
            var table = $('<table class="coolsites"></table>');
            var thead = $('<thead><th>序号</th><th>合作站点</th></thead>');
        }
    },Popsites.Init);
})(jQuery, window));
