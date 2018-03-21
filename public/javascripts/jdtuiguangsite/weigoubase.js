var TM = TM || {};
((function ($, window) {
    TM.WeigouBase = TM.WeigouBase || {};

    var WeigouBase = TM.WeigouBase;

    WeigouBase.init = WeigouBase.init || {};
    WeigouBase.init = $.extend({
        doInit: function() {
            WeigouBase.init.setSelectedNav();
            WeigouBase.event.doInit();

            var searchTitle = WeigouBase.util.getItemSearchParam();
            if (searchTitle === undefined || searchTitle == null || searchTitle == "") {

            } else {
                $(".item-search-input").removeClass("item-search-tip-color");
                $(".item-search-input").val(searchTitle);
            }

        },
        setSelectedNav: function() {
            TM.gcs('/js/jquery.url.js', function () {
                var topCatId = WeigouBase.util.getSearchCat();
                var navObj = $('.nav-wrapper');
                if (topCatId === undefined || topCatId == null) {
                    navObj.find(".first-page-link").addClass('selected-nav');
                } else {
                    navObj.find('a').each(function(){
                        var anchor = $(this);
                        var tempParams = $.url(anchor.attr('href'));
                        if(tempParams.param('catid') == topCatId){
                            navObj.find('a').removeClass('selected-nav');
                            anchor.addClass('selected-nav');
                        }
                    });
                }
            });
        }
    }, WeigouBase.init);


    WeigouBase.event = WeigouBase.event || {};
    WeigouBase.event = $.extend({
        doInit: function() {
            $(".item-search-input").click(function() {
                if ($(this).hasClass("item-search-tip-color")) {
                    $(this).removeClass("item-search-tip-color");
                    $(this).val("");
                }
            });
            $(".item-search-input").blur(function() {
                if ($(this).val() == "") {
                    $(this).addClass("item-search-tip-color");
                    $(this).val("请输入宝贝关键词，轻松淘宝贝");
                }
            });
            $(".item-search-btn").click(function() {

                var action = "/JDPromoteSite/weigou";
                //numIid
                var numIid = WeigouBase.util.getNumIidParam();
                if (numIid === undefined || numIid == null || numIid <= 0) {

                } else {
                    if (action.indexOf("?") >= 0) {
                        action += "&numIid=" + numIid;
                    } else {
                        action += "?numIid=" + numIid;
                    }
                }
                var catId = WeigouBase.util.getSearchCat();
                if (catId === undefined || catId == null) {

                } else {
                    if (action.indexOf("?") >= 0) {
                        action += "&catid=" + catId;
                    } else {
                        action += "?catid=" + catId;
                    }
                }
                var title = $(".item-search-input").val();
                if ($(".item-search-input").hasClass("item-search-tip-color"))
                    title = "";
                if (title == "") {

                } else {
                    if (action.indexOf("?") >= 0) {
                        action += "&search=" + encodeURI(title);
                    } else {
                        action += "?search=" + encodeURI(title);
                    }
                }
                location.href = action;
            });

            $(".item-search-input").keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    $(".item-search-btn").click();
                }
            });
        }
    }, WeigouBase.event);


    WeigouBase.util = WeigouBase.util || {};
    WeigouBase.util = $.extend({
        judgeAjaxResult: function(dataJson) {
            var message = dataJson.msg;
            if (message === undefined || message == null || message == "") {

            } else {
                alert(message);
            }
            return dataJson.isOk;
        },
        getSearchCat: function() {
            var href = window.location.href;
            var params = $.url(href);
            var topCatId = params.param('catid') || params.param('catId');
            //if (topCatId === undefined || topCatId == null)
            //    topCatId = 0;

            return topCatId;
        },
        getNumIidParam: function() {
            var href = window.location.href;
            var params = $.url(href);
            var numIid = params.param('numIid');
            if (numIid === undefined || numIid == null)
                numIid = 0;

            return numIid;
        },
        getItemSearchParam: function() {
            var href = window.location.href;
            var params = $.url(href);
            var searchTitle = params.param('search');
            if (searchTitle === undefined || searchTitle == null)
                searchTitle = "";
            else {
                searchTitle =decodeURI(searchTitle);//解码
            }
            return searchTitle;
        },
        createLoadingDiv: function() {

        }
    }, WeigouBase.util);


})(jQuery,window));



/**
 * 热卖推荐
 */
((function ($, window) {
    TM.WeigouHot = TM.WeigouHot || {};

    var WeigouHot = TM.WeigouHot;

    WeigouHot.init = WeigouHot.init || {};
    WeigouHot.init = $.extend({
        isUseMasonry: false,
        doInit: function(container, isUseMasonry) {
            WeigouHot.container = container;
            WeigouHot.init.isUseMasonry = isUseMasonry;

            WeigouHot.util.initScrollEvent();
            WeigouHot.show.firstShow();
        },
        createHtml: function() {
            var html = '' +
                '' +
                '';

            return html;
        }
    }, WeigouHot.init);


    WeigouHot.show = WeigouHot.show || {};
    WeigouHot.show = $.extend({
        isOnLoading: false,//是否已经在加载
        firstShow: function() {
            var href = window.location.href;
            var params = $.url(href);
            var numIid = params.param('numIid');
            var data = {};
            if (numIid === undefined || numIid == null) {

            } else {
                data.numIid = numIid;
            }
            WeigouHot.show.doShow(data, '/JDPromoteSite/randomUserItems');
        },
        scrollShow: function() {
            var data = {};
            WeigouHot.show.doShow(data, '/JDPromoteSite/randomItems');
        },
        doShow: function(data, url) {
            if (WeigouHot.show.isOnLoading == true)
                return;
            WeigouHot.show.isOnLoading = true;
            WeigouHot.loading.show();

            data = data || {};
            data.title = TM.WeigouBase.util.getItemSearchParam();

            var topCatId = TM.WeigouBase.util.getSearchCat();
            if (topCatId === undefined || topCatId == null) {
                data.topCatId = 0;
            } else {
                data.topCatId = topCatId;
            }

            data.status = 2;//热卖推荐

            var tbodyObj = WeigouHot.container.find(".hot-recommend-table").find("tbody");

            $.ajax({
                url : url,
                data : data,
                type : 'post',
                success : function(dataJson) {
                    if (TM.WeigouBase.util.judgeAjaxResult(dataJson) == false)
                        return;
                    var itemArray = dataJson.res;
                    if (itemArray === undefined || itemArray == null || itemArray.length <= 0) {
                        WeigouHot.util.removeScrollEvent();
                    } else {
                        WeigouHot.util.checkItemArray(itemArray);
                        $(itemArray).each(function(index, itemJson) {
                            var itemObj = WeigouHot.item.doInit(index, itemJson);
                            tbodyObj.append(itemObj);
                        });
                    }

                    WeigouHot.show.doFinishJob();
                }
            });
        },
        doFinishJob: function() {//加载结束后，做一些工作

            WeigouHot.loading.hide();

            WeigouHot.paging.addMasonryTime();

            WeigouHot.show.isOnLoading = false;
        }
    }, WeigouHot.show);

    WeigouHot.item = WeigouHot.item || {};
    WeigouHot.item = $.extend({
        doInit: function(index, itemJson) {
            var html = WeigouHot.item.createHtml();
            var itemObj = $(html);
            var numIid = itemJson.numIid;
            if (numIid === undefined || numIid == null) {
                numIid = itemJson.id;
                itemJson.numIid = numIid;
            }
            //var href = "http://item.taobao.com/item.htm?id=" + itemJson.numIid;
            var href = "/JDPromoteSite/detail?numIid=" + itemJson.numIid;
            var topCatId = TM.WeigouBase.util.getSearchCat();
            if (topCatId === undefined || topCatId == null) {

            } else {
                href += "&catid=" + topCatId;
            }
            itemObj.find(".item-href").attr("href", href);
            itemObj.find(".item-href").attr("target", "_blank");
            itemObj.find(".item-img").attr("src", itemJson.picPath);

            var skuMinPrice = itemJson.skuMinPrice/100;
            var originPrice = itemJson.price/100;
            var priceDivObj = itemObj.find(".item-price-div");
            if (skuMinPrice === undefined || skuMinPrice == null || skuMinPrice <= 0 || skuMinPrice >= originPrice) {
                priceDivObj.html('￥&nbsp;<span class="item-price" style="font-weight: bold;"></span>&nbsp;元 ');
                itemObj.find(".item-price").html(itemJson.price/100);
            } else {
                var html = '' +
                    '<div style="color: #999; font-size: 12px;text-decoration:line-through;">' +
                    '   ￥&nbsp;<span class="origin-item-price"></span>&nbsp;元' +
                    '</div> ' +
                    '<div>' +
                    '   ￥&nbsp;<span class="item-price" style="font-weight: bold;"></span>&nbsp;元 ' +
                    '</div> ' +
                    '';
                priceDivObj.html(html);
                itemObj.find(".origin-item-price").html(itemJson.price/100);
                itemObj.find(".item-price").html(itemJson.skuMinPrice/100);
            }
            itemObj.find(".item-title").html(itemJson.title);
            return itemObj;
        },
        createHtml: function() {
            var html = '' +
                '<tr>' +
                '   <td><a class="item-href" target="_blank"><img class="item-img" style="width: 90px;height: 90px;"/> </a></td>' +
                '   <td style="text-align: left; padding-left: 5px;">' +
                '       <div style="display: block; margin-bottom: 5px;">' +
                '           <a class="item-title item-href item-link"></a>' +
                '       </div> ' +
                '       <div class="item-price-div"></div> ' +
                '       ' +
                '   </td>' +
                '</tr>' +
                '';

            return html;
        }
    }, WeigouHot.item);

    WeigouHot.util = WeigouHot.util || {};
    WeigouHot.util = $.extend({
        hasInitedScrollEvent: false,
        isRemoveScroll: true,
        initScrollEvent: function() {
            if (WeigouHot.init.isUseMasonry == false)
                return;
            if (WeigouHot.util.hasInitedScrollEvent == false) {
                $(window).scroll(function() {
                    if (WeigouHot.util.isRemoveScroll == true)
                        return;
                    var tableObj = WeigouHot.container.find(".hot-recommend-table");
                    var top = tableObj.offset().top;
                    var height = tableObj.height();
                    //var docHeight = $(document).height();
                    var windowHeight = $(window).height();
                    var scrollTop = $(window).scrollTop();
                    if (windowHeight + scrollTop >= top + height - 250) {
                        WeigouHot.show.scrollShow();
                    }
                });
                WeigouHot.util.hasInitedScrollEvent = true;
            }

            WeigouHot.util.isRemoveScroll = false;

        },
        removeScrollEvent: function() {
            WeigouHot.util.isRemoveScroll = true;
        },
        checkItemArray: function(itemArray) {
            if (itemArray === undefined || itemArray == null || itemArray.length <= 0) {
                WeigouHot.util.removeScrollEvent();
                return itemArray;
            } else if (itemArray.length < 20) {
                WeigouHot.util.removeScrollEvent();
                return itemArray;
            } else {
                WeigouHot.util.initScrollEvent();
                return itemArray;
            }

        },
        setContainerMarginBottom: function() {

        }
    }, WeigouHot.util);

    WeigouHot.loading = WeigouHot.loading || {};
    WeigouHot.loading = $.extend({
        show: function() {
            var divObj = WeigouHot.container.find(".loading-div");
            divObj.show();
        },
        hide: function() {
            var divObj = WeigouHot.container.find(".loading-div");
            divObj.hide();
        }
    }, WeigouHot.loading);


    WeigouHot.paging = WeigouHot.paging || {};
    WeigouHot.paging = $.extend({
        MaxMasonryTimes: 10,
        curMasonryTime: 0,
        addMasonryTime: function() {
            WeigouHot.paging.curMasonryTime++;
            if (WeigouHot.paging.curMasonryTime >= WeigouHot.paging.MaxMasonryTimes) {
                WeigouHot.util.removeScrollEvent();
            }
        }
    }, WeigouHot.paging);

})(jQuery,window));


/**
 * 体验版
 */
((function ($, window) {
    TM.WeigouTry = TM.WeigouTry || {};

    var WeigouTry = TM.WeigouTry;

    WeigouTry.init = WeigouTry.init || {};
    WeigouTry.init = $.extend({
        doInit: function(tryAdObj) {
            tryAdObj.find(".close-btn").click(function() {
                tryAdObj.remove();
            });

            tryAdObj.show();
            var bottom = -300;
            var timer = setInterval(function() {
                if (bottom >= 0) {
                    clearInterval(timer);
                    return;
                }
                bottom += 20;
                tryAdObj.css("bottom", bottom + "px");
            }, 100);

        }
    }, WeigouTry.init);



})(jQuery,window));
