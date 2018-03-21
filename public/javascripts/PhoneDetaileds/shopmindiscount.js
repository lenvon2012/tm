

var TM = TM || {};

((function ($, window) {

    TM.ShopMinDiscount = TM.ShopMinDiscount || {};

    var ShopMinDiscount = TM.ShopMinDiscount || {};

    ShopMinDiscount.init = ShopMinDiscount.init || {};
    ShopMinDiscount.init = $.extend({

        doInit: function(container) {

            ShopMinDiscount.container = container;

            var html = '' +
                '<table style="border-collapse: collapse;" class="shop-min-discount-table">' +
                '   <tbody>' +
                '   <tr>' +
                '       <td>当前您的店铺最低折扣为:&nbsp;<span class="shop-discount-span"></span></td>' +
                '       <td style="padding-left: 20px;">' +
                '           <a href="javascript:void(0);" class="link-btn sync-shop-discount-btn">重新同步</a> ' +
                '       </td>' +
                //'       <td style="padding-left: 5px;">|</td>' +
                '       <td style="padding-left: 10px;">' +
                '           <a href="http://smf.taobao.com/smf_tab.htm?module=rmgj" target="_blank" class="link-btn modify-shop-discount-btn">修改</a> ' +
                '       </td>' +
                /*'       <td style="padding-left: 20px;">' +
                '           (如果宝贝折扣低于店铺最低折扣，则宝贝详情页不会显示打折)' +
                '       </td>' +*/
                '   </tr>' +
                '   </tbody>' +
                '</table> ' +
                '' +
                '';

            container.html(html);

            ShopMinDiscount.init.showMinDiscount();

            container.find(".sync-shop-discount-btn").unbind().click(function() {

                $.ajax({
                    url : "/umpactivity/syncShopMinDiscount",
                    data : {},
                    type : 'post',
                    success : function(dataJson) {


                        if (TM.UmpUtil.util.checkIsW2AuthError(dataJson) == true) {
                            return;
                        }

                        if (TM.UmpUtil.util.judgeAjaxResult(dataJson) == false) {
                            //return;
                        }


                        //重新刷新
                        ShopMinDiscount.init.showMinDiscount();
                    }
                });

            });

        },
        showMinDiscount: function() {

            var container = ShopMinDiscount.init.getContainer();

            $.ajax({
                url : "/umpactivity/findShopMinDiscount",
                data : {},
                type : 'post',
                success : function(dataJson) {


                    if (TM.UmpUtil.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }


                    var shopDiscount = TM.UmpUtil.util.getAjaxResultJson(dataJson);


                    if (shopDiscount === undefined || shopDiscount == null || shopDiscount <= 0) {
                        shopDiscount = '-';
                    } else {
                        shopDiscount = shopDiscount / 100;
                    }

                    container.find(".shop-discount-span").html(shopDiscount + '&nbsp;折');

                }
            });

        },
        getContainer: function() {
            return ShopMinDiscount.container;
        }

    }, ShopMinDiscount.init);


})(jQuery,window));

