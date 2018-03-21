var TM = TM || {};

((function ($, window) {
    TM.WeigouList = TM.WeigouList || {};

    var WeigouList = TM.WeigouList;

    WeigouList.init = WeigouList.init || {};
    WeigouList.init = $.extend({
        isUseMasonry: true,
        doInit: function(container, isUseMasonry) {
            WeigouList.container = container;
            if (isUseMasonry === undefined || isUseMasonry == null) {

            } else {
                WeigouList.init.isUseMasonry = isUseMasonry;
            }


            WeigouList.masonryObj = container.find(".masonry-div");

            WeigouList.init.initMasonry();
            WeigouList.util.initScrollEvent();
            //WeigouList.show.firstShow();


        },
        initMasonry: function() {
            WeigouList.masonryObj.masonry({
                itemSelector: '.item-div',
                columnWidth: 230,
                isFitWidth: true
                //isAnimated: true
            });
        }
    }, WeigouList.init);


    WeigouList.show = WeigouList.show || {};
    WeigouList.show = $.extend({
        isOnLoading: false,//是否已经在加载
        firstShow: function() {
            WeigouList.masonryObj.html("");
            WeigouList.masonryObj.masonry("destroy");
            WeigouList.init.initMasonry();

            var numIid = TM.WeigouBase.util.getNumIidParam();
            var data = {};
            if (numIid === undefined || numIid == null) {

            } else {
                data.numIid = numIid;
            }
            WeigouList.show.doShow(data, '/shopping/randomUserItems');
        },
        scrollShow: function() {
            var data = {};
            WeigouList.show.doShow(data, '/shopping/randomItems');
        },
        doShow: function(data, url) {
            if (WeigouList.show.isOnLoading == true)
                return;
            WeigouList.show.isOnLoading = true;
            data = data || {};
            data.title = TM.WeigouBase.util.getItemSearchParam();
            WeigouList.loading.show();

            var topCatId = TM.WeigouBase.util.getSearchCat();
            if (topCatId === undefined || topCatId == null) {
                data.topCatId = 0;
            } else {
                data.topCatId = topCatId;
            }
            $.ajax({
                dataType: 'jsonp',
                url: 'http://121.199.28.225/shopping/innerRandomItems?topCatId=' + data.topCatId + '&title=' + data.title,
                jsonpCallback: "success_jsonpCallback",
                success:function(dataJson){
                    if (dataJson.length <= 0)
                        return;
                    var itemArray = dataJson;
                    var divObj = $("<div class='clearfix' style='margin: 0 auto;height: 0px;overflow: hidden;'></div>");
                    WeigouList.container.append(divObj);

                    if (itemArray === undefined || itemArray == null || itemArray.length <= 0) {
                        WeigouList.util.removeScrollEvent();
                    } else {
                        itemArray = WeigouList.util.checkItemArray(itemArray);
                        $(itemArray).each(function(index, itemJson) {
                            var itemObj = WeigouList.item.doInit(itemJson);
                            divObj.append(itemObj);
                        });
                    }
                    divObj.imagesLoaded(function() {
                        var itemObjs = divObj.find(".item-div");
                        itemObjs.each(function() {
                            if ($(this).width() > 190) {
                                $(this).css("width", "190px");
                            }
                        });
                        WeigouList.masonryObj.append(itemObjs).masonry('appended', itemObjs);
                        WeigouList.show.doFinishJob(divObj);
                    });
                }
            });

            /*$.ajax({
                url : url,
                data : data,
                type : 'post',
                success : function(dataJson) {
                    if (TM.WeigouBase.util.judgeAjaxResult(dataJson) == false)
                        return;
                    var itemArray = dataJson.res;
                    var divObj = $("<div class='clearfix' style='margin: 0 auto;height: 0px;overflow: hidden;'></div>");
                    WeigouList.container.append(divObj);

                    if (itemArray === undefined || itemArray == null || itemArray.length <= 0) {
                        WeigouList.util.removeScrollEvent();
                    } else {
                        itemArray = WeigouList.util.checkItemArray(itemArray);
                        $(itemArray).each(function(index, itemJson) {
                            var itemObj = WeigouList.item.doInit(itemJson);
                            divObj.append(itemObj);
                        });
                    }
                    divObj.imagesLoaded(function() {
                        var itemObjs = divObj.find(".item-div");
                        itemObjs.each(function() {
                            if ($(this).width() > 190) {
                                $(this).css("width", "190px");
                            }
                        });
                        WeigouList.masonryObj.append(itemObjs).masonry('appended', itemObjs);
                        WeigouList.show.doFinishJob(divObj);
                    });

                }
            });*/
        },
        doFinishJob: function(divObj) {//加载结束后，做一些工作
            divObj.remove();
            WeigouList.util.setContainerMarginBottom();
            WeigouList.loading.hide();

            WeigouList.paging.addMasonryTime();

            WeigouList.show.isOnLoading = false;
        }
    }, WeigouList.show);


    WeigouList.item = WeigouList.item || {};
    WeigouList.item = $.extend({
        doInit: function(itemJson) {
            var html = WeigouList.item.createHtml();
            var numIid = itemJson.numIid;
            if (numIid === undefined || numIid == null) {
                numIid = itemJson.id;
                itemJson.numIid = numIid;
            }

            var itemObj = $(html);
            //var href = "http://item.taobao.com/item.htm?id=" + itemJson.numIid;
            var href = "/taoweigou/detail?numIid=" + itemJson.numIid;
            var topCatId = TM.WeigouBase.util.getSearchCat();
            if (topCatId === undefined || topCatId == null) {

            } else {
                href += "&catid=" + topCatId;
            }

            href += "&ll=2185480046";

            itemObj.find(".item-href").attr("href", href);
            itemObj.find(".item-href").attr("target", "_blank");
            itemObj.find(".item-img").attr("src", itemJson.picPath + "_190x190.jpg");

            var skuMinPrice = itemJson.skuMinPrice;
            var originPrice = itemJson.price;
            if (skuMinPrice === undefined || skuMinPrice == null || skuMinPrice <= 0 || skuMinPrice >= originPrice) {
                itemObj.find(".item-price-div").html('价格：<span class="item-price" style="font-weight: bold; font-size: 14px;"></span>&nbsp;元 ');
                itemObj.find(".item-price").html(itemJson.price);
            } else {
                var priceHtml = '' +
                    '<table>' +
                    '   <tbody>' +
                    '   <tr>' +
                    '       <td>' +
                    '           原价：' +
                    '       </td>' +
                    '       <td style="text-decoration:line-through;">' +
                    '           <span class="item-origin-price"></span>&nbsp;元' +
                    '       </td>' +
                    '   </tr>' +
                    '   <tr>' +
                    '       <td style="">' +
                    '           优惠价：' +
                    '       </td>' +
                    '       <td>' +
                    '           <span class="item-new-price item-price" style="font-weight: bold; font-size: 14px;"></span>&nbsp;元' +
                    '       </td>' +
                    '   </tr>' +
                    '   </tbody>' +
                    '</table>' +
                    '';
                itemObj.find(".item-price-div").html(priceHtml);
                itemObj.find(".item-origin-price").html(itemJson.price);
                itemObj.find(".item-new-price").html(itemJson.skuMinPrice);
            }

            itemObj.find(".item-title").html(itemJson.title);
            itemObj.find(".recommend-reason-div").remove();
            return itemObj;
        },
        createHtml: function() {
            var html = '' +
                '<div class="item-div" style="width: 190px; padding: 10px 10px 0px 10px; background: #ffffff;">' +
                '   <div class="item-img-div" style="">' +
                '       <a class="item-href" target="_blank"> ' +
                '           <img style="" class="item-img" />' +
                '       </a>' +
                '   </div> ' +
                '   <div style="font-size: 12px; text-align: left; padding-top: 5px; padding-bottom: 10px;" class="item-price-div">' +
                '       ' +
                '   </div>' +
                '   <div style="font-size: 12px; text-align: left;padding-bottom: 10px;">' +
                '       <a class="item-href item-link item-title" style="line-height: 16px;"></a>' +
                '   </div>' +
                '   <div style="font-size: 12px; text-align: left;padding-bottom: 10px;" class="recommend-reason-div">' +
                '       <strong>推荐理由：</strong><span class="recommend-reason"></span> ' +
                '   </div>' +
                '</div> ' +
                '';

            return html;
        }
    }, WeigouList.item);

    WeigouList.util = WeigouList.util || {};
    WeigouList.util = $.extend({
        hasInitedScrollEvent: false,
        isRemoveScroll: true,
        initScrollEvent: function() {
            if (WeigouList.init.isUseMasonry == false) {

                return;
            }
            if (WeigouList.util.hasInitedScrollEvent == false) {
                $(window).scroll(function() {
                    if (WeigouList.util.isRemoveScroll == true)
                        return;
                    var masonryObj = WeigouList.masonryObj;
                    var top = masonryObj.offset().top;
                    var height = masonryObj.height();
                    //var docHeight = $(document).height();
                    var windowHeight = $(window).height();
                    var scrollTop = $(window).scrollTop();
                    if (windowHeight + scrollTop >= top + height - 250) {
                        WeigouList.show.scrollShow();
                    }
                });
                WeigouList.util.hasInitedScrollEvent = true;
            }
            WeigouList.util.isRemoveScroll = false;
        },
        removeScrollEvent: function() {
            WeigouList.util.isRemoveScroll = true;
        },
        checkItemArray: function(itemArray) {
            if (itemArray === undefined || itemArray == null || itemArray.length <= 0) {
                WeigouList.util.removeScrollEvent();
                return itemArray;
            } else if (itemArray.length < 20) {
                WeigouList.util.removeScrollEvent();
                return itemArray;
            } else {
                WeigouList.util.initScrollEvent();
                return itemArray;
            }

        },
        setContainerMarginBottom: function() {

        }
    }, WeigouList.util);

    WeigouList.loading = WeigouList.loading || {};
    WeigouList.loading = $.extend({
        show: function() {
            var divObj = WeigouList.container.find(".loading-div");
            divObj.show();
        },
        hide: function() {
            var divObj = WeigouList.container.find(".loading-div");
            divObj.hide();
        }
    }, WeigouList.loading);


    WeigouList.paging = WeigouList.paging || {};
    WeigouList.paging = $.extend({
        MaxMasonryTimes: 10,
        curMasonryTime: 0,
        addMasonryTime: function() {
            WeigouList.paging.curMasonryTime++;
            if (WeigouList.paging.curMasonryTime >= WeigouList.paging.MaxMasonryTimes) {
                WeigouList.util.removeScrollEvent();

                var divObj = WeigouList.container.find(".bottom-next-page-div");
                if (divObj.length <= 0) {
                    var html = '' +
                        '<div class="bottom-next-page-div" style="text-align: center; padding: 20px 0px;">' +
                        '   <a href="javascript:void(0);" class="next-page-btn">进入下一页 ></a> ' +
                        '</div> ' +
                        '';
                    divObj = $(html);
                    divObj.find(".next-page-btn").click(function() {
                        /*WeigouList.paging.curMasonryTime = 0;
                         divObj.hide();
                         WeigouList.show.firstShow();*/
                        location.reload();
                    });
                    WeigouList.container.find(".bottom-paging-div").html(divObj);
                }
                //alert(divObj.length);
                divObj.show();
            }
        }
    }, WeigouList.paging);


})(jQuery,window));