
var TM = TM || {};
((function ($, window) {
    TM.Xiaoqingxin = TM.Xiaoqingxin || {};
    var Xiaoqingxin = TM.Xiaoqingxin;
    Xiaoqingxin.Init = Xiaoqingxin.Init || {};
    Xiaoqingxin.Init = $.extend({
        doInit : function(container){
            container.empty();
            var allcategory  = $('<div class="all-category"></div>');
            $.get('/Xiaoqingxin/allCats',function(data){
                if(data.isOk){
                    $(data.res).each(function(i,catItems){
                         switch (i){
                             case 0 : allcategory.append(Xiaoqingxin.genCatItems.genCat("clothes-female","女装","/img/xiaoqingxin/4f79caf7d7c44.jpg","/xiaoqingxin/catitem?catid=1",catItems));break;
                             case 1 : allcategory.append(Xiaoqingxin.genCatItems.genCat("clothes-male","男装","/img/xiaoqingxin/4f79caf7d7c44.jpg","/xiaoqingxin/catitem?catid=2",catItems));break;
                             case 2 : allcategory.append(Xiaoqingxin.genCatItems.genCat("shoes","鞋子","/img/xiaoqingxin/4f79caf7d7c44.jpg","/xiaoqingxin/catitem?catid=3",catItems));break;
                             case 3 : allcategory.append(Xiaoqingxin.genCatItems.genCat("bags","包包","/img/xiaoqingxin/4f79caf7d7c44.jpg","/xiaoqingxin/catitem?catid=4",catItems));break;
                             case 4 : allcategory.append(Xiaoqingxin.genCatItems.genCat("beauty","美容","/img/xiaoqingxin/4f79caf7d7c44.jpg","/xiaoqingxin/catitem?catid=5",catItems));break;
                             case 5 : allcategory.append(Xiaoqingxin.genCatItems.genCat("house","家居","/img/xiaoqingxin/4f79caf7d7c44.jpg","/xiaoqingxin/catitem?catid=6",catItems));break;
                             case 6 : allcategory.append(Xiaoqingxin.genCatItems.genCat("mother-baby","母婴","/img/xiaoqingxin/4f79caf7d7c44.jpg","/xiaoqingxin/catitem?catid=7",catItems));break;
                             case 7 : allcategory.append(Xiaoqingxin.genCatItems.genCat("digit","数码","/img/xiaoqingxin/4f79caf7d7c44.jpg","/xiaoqingxin/catitem?catid=8",catItems));break;
                             case 8 : allcategory.append(Xiaoqingxin.genCatItems.genCat("food","美食","/img/xiaoqingxin/4f79caf7d7c44.jpg","/xiaoqingxin/catitem?catid=9",catItems));break;
                             case 9 : allcategory.append(Xiaoqingxin.genCatItems.genCat("suiyitao","随意淘","/img/xiaoqingxin/4f79caf7d7c44.jpg","/xiaoqingxin/catitem?catid=10",catItems));break;
                         }
                    });
                    container.append(allcategory);
                }
            });
        },
        catShow : function(container) {
            Xiaoqingxin.container = container;
            var html = TM.Xiaoqingxin.Init.createHtml();
            container.html(html);
            Xiaoqingxin.masonryObj = container.find(".masonry-div");
            TM.Xiaoqingxin.Init.initMasonry();
            TM.Xiaoqingxin.util.initScrollEvent();
            TM.Xiaoqingxin.show.firstShow();
        },
        createHtml : function(){
            var html = '' +
                '<div class="masonry-div" style="margin: 0 auto;"></div> ' +
                '<div class="loading-div" style="text-align: center; padding: 10px 0px;display: none;">' +
                '   <img src="/img/taoweigou/loading3.gif" />' +
                '</div> ' +
                '<div class="bottom-paging-div"></div> ' +
                '';

            return html;
        },
        initMasonry: function() {
            Xiaoqingxin.masonryObj.masonry({
                itemSelector: '.item-div',
                columnWidth: 230,
                isFitWidth: true
                //isAnimated: true
            });
        }
    },Xiaoqingxin.Init);

    Xiaoqingxin.show = Xiaoqingxin.show || {};
    Xiaoqingxin.show = $.extend({
        isOnLoading: false,//是否已经在加载
        firstShow: function() {
            Xiaoqingxin.masonryObj.html("");
            Xiaoqingxin.masonryObj.masonry("destroy");
            Xiaoqingxin.Init.initMasonry();

            var numIid = TM.Xiaoqingxin.util.getNumIidParam();
            var data = {};
            if (numIid === undefined || numIid == null) {

            } else {
                data.numIid = numIid;
            }
            Xiaoqingxin.show.doShow(data, '/shopping/randomUserItems');
        },
        scrollShow: function() {
            var data = {};
            Xiaoqingxin.show.doShow(data, '/shopping/randomItems');
        },
        doShow: function(data, url) {
            if (Xiaoqingxin.show.isOnLoading == true)
                return;
            Xiaoqingxin.show.isOnLoading = true;
            data = data || {};
            data.title = TM.Xiaoqingxin.util.getItemSearchParam();
            Xiaoqingxin.loading.show();

            var topCatId = TM.Xiaoqingxin.util.getSearchCat();
            if (topCatId === undefined || topCatId == null) {
                data.topCatId = 0;
            } else {
                data.topCatId = topCatId;
            }
            $.ajax({
                url : url,
                data : data,
                type : 'post',
                success : function(dataJson) {
                    if (TM.Xiaoqingxin.util.judgeAjaxResult(dataJson) == false)
                        return;
                    var itemArray = dataJson.res;
                    var divObj = $("<div class='clearfix' style='margin: 0 auto;height: 0px;overflow: hidden;'></div>");
                    Xiaoqingxin.container.append(divObj);

                    if (itemArray === undefined || itemArray == null || itemArray.length <= 0) {
                        Xiaoqingxin.util.removeScrollEvent();
                    } else {
                        itemArray = Xiaoqingxin.util.checkItemArray(itemArray);
                        $(itemArray).each(function(index, itemJson) {
                            var itemObj = Xiaoqingxin.item.doInit(itemJson);
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
                        Xiaoqingxin.masonryObj.append(itemObjs).masonry('appended', itemObjs);
                        Xiaoqingxin.show.doFinishJob(divObj);
                    });

                }
            });
        },
        doFinishJob: function(divObj) {//加载结束后，做一些工作
            divObj.remove();
            Xiaoqingxin.util.setContainerMarginBottom();
            Xiaoqingxin.loading.hide();

            Xiaoqingxin.paging.addMasonryTime();

            Xiaoqingxin.show.isOnLoading = false;
        }
    }, Xiaoqingxin.show);

    Xiaoqingxin.loading = Xiaoqingxin.loading || {};
    Xiaoqingxin.loading = $.extend({
        show: function() {
            var divObj = Xiaoqingxin.container.find(".loading-div");
            divObj.show();
        },
        hide: function() {
            var divObj = Xiaoqingxin.container.find(".loading-div");
            divObj.hide();
        }
    }, Xiaoqingxin.loading);

    Xiaoqingxin.item = Xiaoqingxin.item || {};
    Xiaoqingxin.item = $.extend({
        doInit: function(itemJson) {
            var html = Xiaoqingxin.item.createHtml();
            var numIid = itemJson.numIid;
            if (numIid === undefined || numIid == null) {
                numIid = itemJson.id;
                itemJson.numIid = numIid;
            }

            var itemObj = $(html);
            var href = "http://item.taobao.com/item.htm?id=" + itemJson.numIid;
            //var href = "/taoweigou/detail?numIid=" + itemJson.numIid;
            var topCatId = TM.Xiaoqingxin.util.getSearchCat();
            if (topCatId === undefined || topCatId == null) {

            } else {
                href += "&catid=" + topCatId;
            }
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
    }, Xiaoqingxin.item);

    Xiaoqingxin.paging = Xiaoqingxin.paging || {};
    Xiaoqingxin.paging = $.extend({
        MaxMasonryTimes: 10,
        curMasonryTime: 0,
        addMasonryTime: function() {
            Xiaoqingxin.paging.curMasonryTime++;
            if (Xiaoqingxin.paging.curMasonryTime >= Xiaoqingxin.paging.MaxMasonryTimes) {
                Xiaoqingxin.util.removeScrollEvent();

                var divObj = Xiaoqingxin.container.find(".bottom-next-page-div");
                if (divObj.length <= 0) {
                    var html = '' +
                        '<div class="bottom-next-page-div" style="text-align: center; padding: 20px 0px;">' +
                        '   <a href="javascript:void(0);" class="next-page-btn">进入下一页 ></a> ' +
                        '</div> ' +
                        '';
                    divObj = $(html);
                    divObj.find(".next-page-btn").click(function() {
                        /*Xiaoqingxin.paging.curMasonryTime = 0;
                         divObj.hide();
                         Xiaoqingxin.show.firstShow();*/
                        location.reload();
                    });
                    Xiaoqingxin.container.find(".bottom-paging-div").html(divObj);
                }
                //alert(divObj.length);
                divObj.show();
            }
        }
    }, Xiaoqingxin.paging);

    Xiaoqingxin.genCatItems = Xiaoqingxin.genCatItems || {};
    Xiaoqingxin.genCatItems = $.extend({
        genCat : function(classname, catname, picURL, preUrl, catItems){
            //var cat = $('<div class="'+classname+' category"></div>');
            //return cat;
            if(catItems.length == 0) {
                return;
            }
            var htmls = '' +
                '<div class="'+classname+' category">' +
                '    <div class="category-head"></div>' +
                '    <div class="category-body">' +
                '        <table class="category-body-title">' +
                '            <tr class="share_title">' +
                '                <td style="width: 150px;">' +
                '                   <em class="inlineblock">' +
                '                        <a target="_blank" href="'+preUrl+'">' +
                '                            <span>分享</span>' + catname +
                '                        </a>' +
                '                    </em>' +
                '               </td>' +
                '               <td style="width: 150px;">' +
                '                    <span class="st_key inlineblock">' +
                '                        <a target="_blank" href="'+preUrl+'">热门</a>' +
                '                        <a target="_blank" href="'+preUrl+'">最新</a>' +
                '                        <span>|</span>' +
                '                    </span>' +
                '                </td>' +
                '                <td>' +
                '                    <span class="more fr inlineblock">' +
                '                        <a href="'+preUrl+'">更多&gt;&gt;</a>' +
                '                    </span>' +
                '                </td>' +
                '            </tr>' +
                '        </table>' +
                '        <table class="category-body-content">' +
                '            <tr>' +
                '                <td class="category-body-content-img">' +
                '                    <a href="'+preUrl+'" target="_blank">' +
                '                        <img src="'+picURL+'" width="185" height="330">' +
                '                    </a>' +
                '                </td>' +
                '                <td class="category-body-content-items">' +
                '';
            $(catItems).each(function(i,item){
                 htmls += Xiaoqingxin.genCatItems.genItem(i,item);
            });
            htmls += '' +
                '                </td>' +
                '           </tr>' +
                '        </table>' +
                '   </div>' +
                '   <div class="category-foot"></div>' +
                '</div> ' +
                '';

            return htmls;
        },
        genItem : function(i,item){
            var itemtype = "normal-item";
            var imgwidth = 150;
            var href = "http://item.taobao.com/item.htm?id=" + item.numIid;
            var item;
            if(i ==3 || i == 4){
                itemtype = "long-item";
                imgwidth = 235;
            }
            item = '' +
                '<div class="inlineblock '+itemtype+'">' +
                '    <a href="'+href+'" target="_blank" style="display:block;width:'+imgwidth+'px;height:160px;overflow:hidden;">' +
                '        <img class="img lazyload" src="'+item.picPath+'"  style="display: block; margin-top: 0px; margin-left: -5px; width: '+imgwidth+'px; height: 160px;">' +
                '    </a>' +
                '    <a class="trsp_bg w150" href="javascript:void(0)" target="_blank" style="height: 30px;">' +
                '        <h4>' +
                '            <span class="likeit fl inlineblock">' +
                '                <b class="nums red">0</b>' +
                '            </span>' +
                '            <span class="f12 fr inlineblock" style="font-size: 12px;">'+item.bigCatName+'</span>' +
                '        </h4>' +
                '    </a>' +
                '</div>' +
                '';
            return item;
        }
    },Xiaoqingxin.genCatItems);

    Xiaoqingxin.util = Xiaoqingxin.util || {};
    Xiaoqingxin.util = $.extend({
        getModuleHtml : function() {
            var html = '' +
                '<div class="clothes category">' +
                '    <div class="category-head"></div>' +
                '    <div class="category-body">' +
                '        <table class="category-body-title">' +
                '            <tr class="share_title">' +
                '                <td style="width: 150px;">' +
                '                   <em class="inlineblock">' +
                '                        <a target="_blank" href="javascript:void(0)">' +
                '                            <span>分享</span>清新服饰' +
                '                        </a>' +
                '                    </em>' +
                '               </td>' +
                '               <td style="width: 150px;">' +
                '                    <span class="st_key inlineblock">' +
                '                        <a target="_blank" href="javascript:void(0)">热门</a>' +
                '                        <a target="_blank" href="javascript:void(0)">最新</a>' +
                '                        <span>|</span>' +
                '                    </span>' +
                '                </td>' +
                '                <td>' +
                '                    <span class="more fr inlineblock">' +
                '                        <a href="javascript:void(0)">更多&gt;&gt;</a>' +
                '                    </span>' +
                '                </td>' +
                '            </tr>' +
                '        </table>' +
                '        <table class="category-body-content">' +
                '        </table>' +
                '   </div>' +
                '   <div class="category-foot"></div>' +
                '</div> ' +
                '';

            return html;
        },
        hasInitedScrollEvent: false,
        isRemoveScroll: true,
        initScrollEvent: function() {
            if (Xiaoqingxin.util.hasInitedScrollEvent == false) {
                $(window).scroll(function() {
                    if (Xiaoqingxin.util.isRemoveScroll == true)
                        return;
                    var masonryObj = Xiaoqingxin.masonryObj;
                    var top = masonryObj.offset().top;
                    var height = masonryObj.height();
                    //var docHeight = $(document).height();
                    var windowHeight = $(window).height();
                    var scrollTop = $(window).scrollTop();
                    if (windowHeight + scrollTop >= top + height - 250) {
                        Xiaoqingxin.show.scrollShow();
                    }
                });
                Xiaoqingxin.util.hasInitedScrollEvent = true;
            }
            Xiaoqingxin.util.isRemoveScroll = false;
        },
        removeScrollEvent: function() {
            Xiaoqingxin.util.isRemoveScroll = true;
        },
        checkItemArray: function(itemArray) {
            if (itemArray === undefined || itemArray == null || itemArray.length <= 0) {
                Xiaoqingxin.util.removeScrollEvent();
                return itemArray;
            } else if (itemArray.length < 20) {
                Xiaoqingxin.util.removeScrollEvent();
                return itemArray;
            } else {
                Xiaoqingxin.util.initScrollEvent();
                return itemArray;
            }

        },
        setContainerMarginBottom: function() {

        },
        getNumIidParam: function() {
            var href = window.location.href;
            var params = $.url(href);
            var numIid = params.param('numIid');
            if (numIid === undefined || numIid == null)
                numIid = 0;

            return numIid;
        },
        getSearchCat: function() {
            var href = window.location.href;
            var params = $.url(href);
            var topCatId = params.param('catid') || params.param('catId');
            //if (topCatId === undefined || topCatId == null)
            //    topCatId = 0;

            return topCatId;
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
        judgeAjaxResult: function(dataJson) {
            var message = dataJson.msg;
            if (message === undefined || message == null || message == "") {

            } else {
                alert(message);
            }
            return dataJson.isOk;
        }
    },Xiaoqingxin.util);
})(jQuery,window));