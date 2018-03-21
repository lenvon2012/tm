var TM = TM || {};
((function ($, window) {
    TM.SearchSaleCount = TM.SearchSaleCount || {};

    var SearchSaleCount = TM.SearchSaleCount;

    SearchSaleCount.init = SearchSaleCount.init || {};
    SearchSaleCount.init = $.extend({
        doInit: function(container) {
            SearchSaleCount.container = container;

            $(".tm-search-btn").unbind();
            $(".tm-search-btn").click(function() {
                var userNick = $(".tb-user-nick").val();
                SearchSaleCount.search.doSearch(userNick);
            });
            $(".tb-user-nick").keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    $(".tm-search-btn").click();
                }
            });

            TM.SearchBase.util.searchFromUrl();
            TM.SearchBase.util.searchFromNickInput();
        }
    }, SearchSaleCount.init);


    SearchSaleCount.search = SearchSaleCount.search || {};
    SearchSaleCount.search = $.extend({
        doSearch: function(userNick) {
            if (userNick === undefined || userNick == null || userNick == "") {
                alert("请先输入淘宝账号");
                return;
            }
            SearchSaleCount.container.html("");

            SearchSaleCount.sales.doShow(userNick);
        }
    }, SearchSaleCount.search);





    SearchSaleCount.sales = SearchSaleCount.sales || {};
    SearchSaleCount.sales = $.extend({
        doShow: function(userNick) {
            var data = {};
            data.userNick = userNick;
            $.ajax({
                url : '/CatSearchSaleCount/doQuerySaleCount',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    if (TM.SearchBase.util.judgeAjaxResult(dataJson) == false)
                        return;
                    var html = SearchSaleCount.sales.createHtml();
                    SearchSaleCount.container.html(html);
                    $(".search-wrapper").css("padding-top", "10px");

                    var shopJson = dataJson.res;
                    SearchSaleCount.container.find(".tb-user-nick-span").html(shopJson.nick);

                    var shopHref = "http://shop" + shopJson.shopId + ".taobao.com/";
                    SearchSaleCount.container.find(".shop-href").attr("href", shopHref);
                    SearchSaleCount.container.find(".shop-img").attr("src", shopJson.shopImgPath);
                    SearchSaleCount.container.find(".shop-title").html(shopJson.shopName);

                    SearchSaleCount.container.find(".shop-item-num").html(shopJson.itemNum);
                    SearchSaleCount.container.find(".month-sale-amount").html(shopJson.saleAmount);
                    SearchSaleCount.container.find(".mount-trade-num").html(shopJson.saleNum);

                    var perTradeCost = 0;
                    if (shopJson.saleNum > 0) {
                        perTradeCost = shopJson.saleAmount / shopJson.saleNum;
                        perTradeCost = Math.round(perTradeCost * 100) / 100;
                    }
                    SearchSaleCount.container.find(".per-trade-cost").html(perTradeCost);

                    var priceRangeJsonArray = null;
                    var tempJsonStr = shopJson.priceRangeJson;
                    if (tempJsonStr === undefined || tempJsonStr == null || tempJsonStr == "") {

                    } else {
                        var code = 'priceRangeJsonArray = ' + tempJsonStr;
                        try {
                            eval(code);
                        } catch (e) {

                        }
                    }
                    if (priceRangeJsonArray != null && priceRangeJsonArray.length > 0) {
                        SearchSaleCount.priceList.doShow(priceRangeJsonArray);
                    } else {

                    }

                    SearchSaleCount.credit.doShow(shopJson);
                }
            });
        },
        createHtml: function() {
            var html = '' +
                '<div style="margin: 0 auto; text-align: left;">' +
                '   <table>' +
                '       <tbody>' +
                '       <tr>' +
                '           <td><span style="" class="tb-user-type">淘宝卖家：</span> </td>' +
                '           <td>' +
                '               <a class="rate-link-url shop-href" target="_blank">' +
                '                   <span style="" class="tb-user-nick-span"></span> ' +
                '               </a>' +
                '           </td>' +
                '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '   <table style="margin-top: 10px;" class="base-info-table">' +
                '       <tbody>' +
                '       <tr>' +
                '           <td style="padding-right: 20px;">' +
                '               <a href="javascript:void(0);" class="shop-href" target="_blank">' +
                '                   <img class="shop-img" style="width: 100px; height: 100px;" /> ' +
                '               </a>' +
                '           </td>' +
                '           <td>' +
                '               <table class="base-info-table">' +
                '                   <tbody>' +
                '                   <tr>' +
                '                       <td><span class="tip-dot">卖家信用：</span> </td>' +
                '                       <td>' +
                '                           <span class="seller-credit blue-color bold"></span>点' +
                '                           <img class="seller-credit-img" /> ' +
                '                       </td>' +
                '                       <td><span class="tip-dot">好评率：</span> </td>' +
                '                       <td class="good-credit-rate"></td>' +
                '                   </tr>' +
                '                   <tr>' +
                '                       <td><span class="tip-dot">店铺宝贝数：</span> </td>' +
                '                       <td>' +
                '                           共&nbsp;<span class="shop-item-num" style=" color: #a10000;font-weight: bold;"></span>&nbsp;件宝贝' +
                '                       </td>' +
                '                       <td style="width: 110px;"><span class="tip-dot">月销售额预估：</span> </td>' +
                '                       <td style="width: 150px;">' +
                '                           ￥&nbsp;<span class="month-sale-amount" style=" color: #a10000;font-weight: bold;"></span>&nbsp;元' +
                '                       </td>' +
                '                   </tr>' +
                '                   <tr>' +
                '                       <td style="width: 100px;"><span class="tip-dot">宝贝销售量：</span></td>' +
                '                       <td style="width: 150px;" class="">' +
                '                           月销&nbsp;<span class="mount-trade-num" style=" color: #a10000;font-weight: bold;"></span>&nbsp;笔' +
                '                       </td>' +
                '                       <td><span class="tip-dot">客单价：</span> </td>' +
                '                       <td>' +
                '                           ￥&nbsp;<span class="per-trade-cost" style=" color: #a10000;font-weight: bold;"></span>&nbsp;元' +
                '                       </td>' +
                '                   </tr>' +
                '                   </tbody>' +
                '               </table> ' +
                '           </td>' +
                '       </tr>' +
                '' +
                '' +
                /*'           <td style="width: 80px;"><span class="tip-dot">月销售额：</span> </td>' +
                 '           <td style="width: 170px;">' +
                 '               ￥&nbsp;<span class="month-sale-amount" style=" color: #a10000;font-weight: bold;"></span>&nbsp;元' +
                 '           </td>' +
                 '           <td style="width: 95px;"><span class="tip-dot">宝贝销售量：</span></td>' +
                 '           <td style="width: 170px;" class="">' +
                 '               月销&nbsp;<span class="mount-trade-num" style=" color: #a10000;font-weight: bold;"></span>&nbsp;笔' +
                 '           </td>' +
                 '       </tr>' +
                 '       <tr>' +
                 '           <td style="width: 80px;"><span class="tip-dot">客单价：</span> </td>' +
                 '           <td style="width: 170px;" colspan="3">' +
                 '               ￥&nbsp;<span class="per-trade-cost" style=" color: #a10000;font-weight: bold;"></span>&nbsp;元' +
                 '           </td>' +
                 '       </tr>' +*/
                '       </tbody>' +
                '   </table>' +
                '   <table style="margin-top: 10px;" class="base-info-table">' +
                '       <tbody>' +
                '       <tr>' +
                '           <td style="width: 120px;"><span class="tip-dot">宝贝与描述相符：</span> </td>' +
                '           <td style="width: 90px;">' +
                '               <span class="describe-score" style=" color: #a10000;font-weight: bold;"></span> ' +
                '           </td>' +
                '           <td style="width: 120px;"><span class="tip-dot">卖家的服务态度：</span></td>' +
                '           <td style="width: 90px;" class="">' +
                '               <span class="service-score" style=" color: #a10000;font-weight: bold;"></span> ' +
                '           </td>' +
                '           <td style="width: 120px;"><span class="tip-dot">卖家发货的速度：</span> </td>' +
                '           <td style="width: 90px;" colspan="3">' +
                '               <span class="delivery-score" style=" color: #a10000;font-weight: bold;"></span> ' +
                '           </td>' +
                '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '   <div style="margin-top: 10px;" class="separator"></div> ' +
                '   <table class="result-table price-table" style="width: 100%; text-align: center; margin-top: 15px;">' +
                '       <thead>' +
                '       <tr>' +
                '           <td style="width: 20%;">商品数量</td>' +
                '           <td style="width: 20%;">价格区间（元）</td>' +
                '           <td style="width: 30%;">30天销售量（件）</td>' +
                '           <td style="width: 30%;">30天销售额（元）</td>' +
                '       </tr>' +
                '       </thead>' +
                '       <tbody>' +
                '       </tbody>' +
                '   </table> ' +
                '' +
                '</div> ' +
                '';

            return html;
        }
    }, SearchSaleCount.sales);


    SearchSaleCount.credit = SearchSaleCount.credit || {};
    SearchSaleCount.credit = $.extend({
        doShow: function(shopJson) {
            var data = {};
            data.userId = shopJson.userId;
            data.userNick = shopJson.nick;
            $.ajax({
                url : '/CatSearchSaleCount/doQuerySellerCredit',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    if (TM.SearchBase.util.judgeAjaxResult(dataJson) == false)
                        return;
                    var rateJson = dataJson.res;

                    //信用
                    if (rateJson.seller != true) {
                        SearchSaleCount.container.find(".seller-credit-img").remove();
                        return;
                    }

                    SearchSaleCount.container.find(".seller-credit").html(rateJson.sellerCredit);
                    if (rateJson.sellerCreditImg === undefined || rateJson.sellerCreditImg == null || rateJson.sellerCreditImg == "") {
                        SearchSaleCount.container.find(".seller-credit-img").remove();
                    } else
                        SearchSaleCount.container.find(".seller-credit-img").attr("src", rateJson.sellerCreditImg);

                    SearchSaleCount.container.find(".good-credit-rate").html(rateJson.goodCreditRate);

                    SearchSaleCount.container.find(".describe-score").html(rateJson.describeScore.score);
                    SearchSaleCount.container.find(".service-score").html(rateJson.serviceScore.score);
                    SearchSaleCount.container.find(".delivery-score").html(rateJson.deliveryScore.score);

                }
            });
        }
    }, SearchSaleCount.credit);


    SearchSaleCount.priceList = SearchSaleCount.priceList || {};
    SearchSaleCount.priceList = $.extend({
        doShow: function(priceRangeJsonArray) {
            SearchSaleCount.container.find(".price-table").find("tbody").html("");
            $(priceRangeJsonArray).each(function(index, priceJson) {
                var trObj = SearchSaleCount.priceList.createTrObj(index, priceJson);
                SearchSaleCount.container.find(".price-table").find("tbody").append(trObj);
            });
        },
        createTrObj: function(index, priceJson) {
            var html = '' +
                '<tr>' +
                '   <td class="item--price-num-td"></td>' +
                '   <td class="item-price-range-td"></td>' +
                '   <td class="item-sale-num-td"></td>' +
                '   <td class="item-sale-amount-td"></td>' +
                '</tr>' +
                '';
            var trObj = $(html);
            trObj.find(".item--price-num-td").html(priceJson.itemNum);
            trObj.find(".item-price-range-td").html("￥" + priceJson.startPrice + "--" + priceJson.endPrice);
            trObj.find(".item-sale-num-td").html(priceJson.saleNum);
            trObj.find(".item-sale-amount-td").html(priceJson.saleAmount);

            return trObj;

        }
    }, SearchSaleCount.priceList);


})(jQuery,window));