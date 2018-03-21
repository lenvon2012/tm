 var ShowRandomPopularizedItems = ShowRandomPopularizedItems || {};
 ShowRandomPopularizedItems.Init = ShowRandomPopularizedItems.Init || {};
 ShowRandomPopularizedItems.Init = $.extend({
     init : function(numIid){
         var showRandomPopularitedItemsDiv = $('.showRandomPopularitedItemsDiv');
         showRandomPopularitedItemsDiv.empty();
         ShowRandomPopularizedItems.ShowItems.show(showRandomPopularitedItemsDiv,numIid);
         setInterval("ShowRandomPopularizedItems.util.updateRandom()",30000);
     }
 },ShowRandomPopularizedItems.Init);

 ShowRandomPopularizedItems.ShowItems = ShowRandomPopularizedItems.ShowItems || {};
 ShowRandomPopularizedItems.ShowItems = $.extend({
     show : function(showRandomPopularitedItemsDiv,numIid){
         $.post("/Application/getRandomPopularitedItems",{numIid:numIid},function(data){
             showRandomPopularitedItemsDiv.append(ShowRandomPopularizedItems.ShowItems.createItemsDiv(data));
             showRandomPopularitedItemsDiv.fadeIn(2000);
         });
     },
     createItemsDiv : function(data){
         var itemsDiv = $('<div class="randomItemsDiv"></div>');
         for (var i = 0; i < data.length; i++) {
             itemsDiv.append(ShowRandomPopularizedItems.ShowItems.createRandomItem(data[i]));
         }
         return itemsDiv;
     },
     createRandomItem : function(item){
         var randomItem = $('<div class="RandomItem"></div>');
         randomItem.append(ShowRandomPopularizedItems.ShowItems.createPic(item));
         randomItem.append(ShowRandomPopularizedItems.ShowItems.createTitle(item));
         randomItem.append(ShowRandomPopularizedItems.ShowItems.createPrice(item));
         randomItem.append(ShowRandomPopularizedItems.ShowItems.createBuyBtn(item));
         return randomItem;
     },
     createPic : function(item){
         var aObj = $('<a target="_blank"></a>');
         var url = "http://item.taobao.com/item.htm?id=" + item.numIid;
         aObj.attr("href",url);
         var imgObj = $('<img width="148px" height="148px;" class="randomItemImg">');
         imgObj.attr("src",item.picPath);
         aObj.append(imgObj);
         return aObj;
     },
     createTitle : function(item){
         var title = $('<div class="randomItemTitleDiv"></div>');
         title.append($('<span class="randomItemTitleSpan">' + item.title + '</span>'));
         return title;
     },
     createPrice : function(item){
         var skuMinPrice = item.skuMinPrice;
         var originPrice = item.price;
         if (skuMinPrice === undefined || skuMinPrice == null || skuMinPrice <= 0 || skuMinPrice >= originPrice) {
             var priceObj = $('<div class="randomItemPriceDiv">价格:</div>');
             priceObj.append($('<span class="randomItemPriceSpan">￥' + item.price + '</span>'));
             return priceObj;
         } else {
             var priceObj = $('<div class="randomItemPriceDiv" style="">价格:</div>');
             priceObj.append($('<span class="randomItemPriceSpan" style="font-size: 12px;text-decoration:line-through; color: #999;">' + item.price + '</span>'));
             //priceObj.append('&nbsp;');
             priceObj.append('<span class="item-new-price" style="font-size: 12px;color: #a10000;font-weight: bold;">￥' + item.skuMinPrice + '</span>');
             return priceObj;
         }

     },
     createBuyBtn : function(item){
         var aObj = $('<a target="_blank"></a>');
         var url = "http://item.taobao.com/item.htm?id=" + item.numIid;
         aObj.attr("href",url);
         var buy = $('<div class="randomItemBuyBtn"></div>');
         aObj.append(buy);
         return aObj;
     }
 },ShowRandomPopularizedItems.ShowItems);

 ShowRandomPopularizedItems.util = ShowRandomPopularizedItems.util || {};
 ShowRandomPopularizedItems.util = $.extend({
     updateRandom : function(){
         var showRandomPopularitedItemsDiv = $('.showRandomPopularitedItemsDiv');
         showRandomPopularitedItemsDiv.fadeOut(2000,function(){
             //ShowRandomPopularizedItems.Init.init();
             showRandomPopularitedItemsDiv.empty();
             ShowRandomPopularizedItems.ShowItems.show(showRandomPopularitedItemsDiv);
         });
     }
 },ShowRandomPopularizedItems.util);
