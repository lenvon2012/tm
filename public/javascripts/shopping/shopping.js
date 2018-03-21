var TM = TM || {};
((function ($, window) {
    TM.Shopping = TM.Shopping || {};

    var Shopping = TM.Shopping;

    Shopping.init = Shopping.init || {};
    Shopping.init = $.extend({
        showColumnNum: 0,
        doInit: function(container) {
            Shopping.container = container;

            Shopping.init.showColumnNum = TM.ShopBase.init.showColumnNum;

            var masonryObj = $("<div class='masonry-div' style='margin: 0 auto;'></div> ");
            container.append(masonryObj);
            Shopping.init.initMasonry();

            Shopping.util.initScrollEvent();
            Shopping.show.firstShow();
            Shopping.search.initEvent();

        },
        initMasonry: function() {
            Shopping.container.find(".masonry-div").masonry({
                itemSelector: '.item-div',
                columnWidth: 230,
                isFitWidth: true
                //isAnimated: true
            });
        },
        test: function() {
            var itemArray = [];
            var picArray = [
                "http://img03.taobaocdn.com/bao/uploaded/i3/11396021414937848/T1G2R7XtXcXXXXXXXX_!!0-item_pic.jpg_310x310.jpg",
                "http://img01.taobaocdn.com/bao/uploaded/i1/16370032550588088/T12xNnXAVeXXXXXXXX_!!0-item_pic.jpg_190x190.jpg",
                "http://img01.taobaocdn.com/bao/uploaded/i1/10700021228714699/T1H3l1XsdhXXXXXXXX_!!0-item_pic.jpg"
            ];
            for (var i = 0; i < 3; i++) {
                var itemJson = {};
                itemJson.picPath = picArray[i];
                itemArray[itemArray.length] = itemJson;
            }
            return itemArray;
        }
    }, Shopping.init);

    Shopping.search = Shopping.search || {};
    Shopping.search = $.extend({
        initEvent: function() {
            $(".item-search-input").click(function() {
                if ($(this).hasClass("search-tip")) {
                    $(this).removeClass("search-tip");
                    $(this).val("");
                }
            });
            $(".item-search-input").blur(function() {
                if ($(this).val() == "") {
                    $(this).addClass("search-tip");
                    $(this).val("输入宝贝标题，轻松搜索宝贝");
                }
            });
            $(".item-search-btn").click(function() {
                var catId = TM.ShopBase.util.getSearchCat();
                var action = "/shopping/index";
                if (catId === undefined || catId == null) {

                } else {
                    action += "?topcat=" + catId;
                }
                var title = $(".item-search-input").val();
                if ($(".item-search-input").hasClass("search-tip"))
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
    }, Shopping.search);


    Shopping.show = Shopping.show || {};
    Shopping.show = $.extend({
        isOnLoading: false,//是否已经在加载
        firstShow: function() {
            Shopping.masonry.isFollowOrder = true;
            Shopping.container.find(".masonry-div").html("");
            Shopping.container.find(".masonry-div").masonry("destroy");
            Shopping.init.initMasonry();

            var href =   window.location.href;
            var params = $.url(href);
            var numIid = params.param('numIid');
            var data = {};
            if (numIid === undefined || numIid == null) {

            } else {
                data.numIid = numIid;
            }
            Shopping.masonry.isFollowOrder = true;
            Shopping.show.doShow(data, '/shopping/randomUserItems');
        },
        scrollShow: function() {
            var data = {};
            Shopping.show.doShow(data, '/shopping/randomItems');
        },
        doShow: function(data, url) {
            if (Shopping.show.isOnLoading == true)
                return;
            Shopping.show.isOnLoading = true;
            data = data || {};
            if ($(".item-search-input").hasClass("search-tip")) {

            } else {
                data.title = $(".item-search-input").val();
            }
            //alert(data.title);
            //TM.Loading.init.show();
            Shopping.loading.show();
            var topCatId = TM.ShopBase.util.getSearchCat();
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
                    Shopping.container.append(divObj);
                    //itemArray = Shopping.init.test();
                    if (itemArray === undefined || itemArray == null || itemArray.length <= 0) {
                        Shopping.util.removeScrollEvent();
                    } else {

                        itemArray = Shopping.util.fulfillItemArray(itemArray);
                        $(itemArray).each(function(index, itemJson) {
                            var itemObj = Shopping.item.doInit(itemJson);
                            divObj.append(itemObj);
                        });
                    }

                    //瀑布流
                    Shopping.masonry.doMasonry(divObj);
                }
            });
           /* $.ajax({
                url : url,
                data : data,
                type : 'post',
                success : function(dataJson) {
                    if (TM.ShopBase.util.judgeAjaxResult(dataJson) == false)
                        return;
                    var itemArray = dataJson.res;
                    var divObj = $("<div class='clearfix' style='margin: 0 auto;height: 0px;overflow: hidden;'></div>");
                    Shopping.container.append(divObj);
                    //itemArray = Shopping.init.test();
                    if (itemArray === undefined || itemArray == null || itemArray.length <= 0) {
                        Shopping.util.removeScrollEvent();
                    } else {

                        itemArray = Shopping.util.fulfillItemArray(itemArray);
                        $(itemArray).each(function(index, itemJson) {
                            var itemObj = Shopping.item.doInit(itemJson);
                            divObj.append(itemObj);
                        });
                    }

                    //瀑布流
                    Shopping.masonry.doMasonry(divObj);

                }
            });*/
        }
    }, Shopping.show);


    Shopping.masonry = Shopping.masonry || {};
    Shopping.masonry = $.extend({
        isFollowOrder: false,
        EachWaitMillis: 200,
        MaxWaitLoadTimes: 100,//最多等20s，等待图片加载
        doMasonry: function(divObj) {
            //Shopping.masonry.createItemRows(0, null, divObj);
            Shopping.masonry.newCreateItemRows(divObj);
        },
        newCreateItemRows: function(divObj) {
            /*var itemObjArray = divObj.find(".item-div");
            var index = 0;
            var totalCount = itemObjArray.length;

            $(itemObjArray).each(function() {
                var itemObj = $(this);
                itemObj.imagesLoaded(function() {
                    Shopping.container.find(".masonry-div").append($(this)).masonry('appended', $(this));
                    index++;
                    if (index >= totalCount)
                        Shopping.masonry.doFinishJob(divObj);
                });
            });*/

            divObj.imagesLoaded(function() {
                var itemObjs = divObj.find(".item-div");
                itemObjs.each(function() {
                    if ($(this).width() > 190) {
                        $(this).css("width", "190px");
                    }
                });
                Shopping.container.find(".masonry-div").append(itemObjs).masonry('appended', itemObjs);
                Shopping.masonry.doFinishJob(divObj);
            });
        },
        createItemRows: function(waitIndex, curRowObj, divObj) {
            var itemObjArray = divObj.find(".item-div");
            if (itemObjArray === undefined || itemObjArray == null || itemObjArray.length <= 0) {
                Shopping.masonry.doFinishJob(divObj);
                return;
            }
            //创建行对象
            if (curRowObj === undefined || curRowObj == null || curRowObj.length <= 0) {
                curRowObj = $("<div class='clearfix' style='margin: 0 auto;overflow: hidden;'></div>");
            }
            //加一个item
            var isFollowOrder = Shopping.masonry.isFollowOrder;
            var targetItemObj = null;
            if (isFollowOrder == false) {
                for (var itemIndex = 0; itemIndex < itemObjArray.length; itemIndex++) {
                    var itemObj = $(itemObjArray.get(itemIndex));
                    var flag = Shopping.masonry.checkItemObj(itemObj);
                    if (flag == true) {
                        targetItemObj = itemObj;
                        break;
                    }
                 }
            } else {
                //按照顺序来
                var itemObj = $(itemObjArray.get(0));//width: 180px;
                var flag = Shopping.masonry.checkItemObj(itemObj);
                if (flag == true) {
                    targetItemObj = itemObj;
                }
            }

            if (targetItemObj == null) {
                waitIndex++;
                //等待超时
                if (waitIndex > Shopping.masonry.MaxWaitLoadTimes) {
                    Shopping.masonry.doFinishJob(divObj);
                    return;
                }
                //等待
                setTimeout(function() {
                    Shopping.masonry.createItemRows(waitIndex, curRowObj, divObj);
                }, Shopping.masonry.EachWaitMillis);

                return;
            } else {
                /*curRowObj.append(targetItemObj);
                var showColumnNum = Shopping.init.showColumnNum;
                if (curRowObj.find(".item-div").length >= showColumnNum) {
                    Shopping.container.append(curRowObj);
                    curRowObj.masonry({
                        itemSelector: '.item-div',
                        columnWidth: 220,
                        isFitWidth: true
                    });
                    curRowObj = null;//新起一行
                }*/
                Shopping.container.find(".masonry-div").append(targetItemObj).masonry('appended', targetItemObj);
                Shopping.masonry.createItemRows(waitIndex, curRowObj, divObj);
            }

        },
        checkItemObj: function(itemObj) {
            var imgWidth = itemObj.find(".item-img").width();
            var imgHeight = itemObj.find(".item-img").height();
            if (imgWidth > 0 && imgHeight > 0) {
                var newWidth = 190;
                var newHeight = imgHeight * 190 / imgWidth;
                itemObj.find(".item-img").css("width", newWidth + "px");
                itemObj.find(".item-img").css("height", newHeight + "px");
                return true;
            } else
                return false;

        },
        doFinishJob: function(divObj) {//加载结束后，做一些工作
            divObj.remove();
            Shopping.util.setContainerMarginBottom();
            //TM.Loading.init.hidden();
            Shopping.loading.hide();

            Shopping.paging.addMasonryTime();

            Shopping.show.isOnLoading = false;
            Shopping.masonry.isFollowOrder = false;
        }
    }, Shopping.masonry);


    Shopping.item = Shopping.item || {};
    Shopping.item = $.extend({
        doInit: function(itemJson) {
            var html = Shopping.item.createHtml();

            var itemObj = $(html);
            var href = "http://item.taobao.com/item.htm?id=" + itemJson.numIid;
            itemObj.find(".item-href").attr("href", href);
            itemObj.find(".item-href").attr("target", "_blank");
            itemObj.find(".item-img").attr("src", itemJson.picPath + "_190x190.jpg");

            var skuMinPrice = itemJson.skuMinPrice;
            var originPrice = itemJson.price;
            if (skuMinPrice === undefined || skuMinPrice == null || skuMinPrice <= 0 || skuMinPrice >= originPrice) {
                itemObj.find(".item-price-div").html('价格：<span class="item-price" style="color: #a10000; font-weight: bold; font-size: 14px;"></span>&nbsp;元 ');
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
                    '           折扣价：' +
                    '       </td>' +
                    '       <td>' +
                    '           <span class="item-new-price" style="color: #a10000; font-weight: bold; font-size: 14px;"></span>&nbsp;元' +
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
    }, Shopping.item);

    Shopping.util = Shopping.util || {};
    Shopping.util = $.extend({
        hasInitedScrollEvent: false,
        initScrollEvent: function() {
            if (Shopping.util.hasInitedScrollEvent == true) {
                return;
            }
            Shopping.util.hasInitedScrollEvent = true;
            $(window).scroll(function() {
                var docHeight = $(document).height();
                var windowHeight = $(window).height();
                var scrollTop = $(window).scrollTop();
                if (windowHeight + scrollTop >= docHeight - 300) {
                    Shopping.show.scrollShow();
                }
            });

        },
        removeScrollEvent: function() {
            $(window).unbind("scroll");
            Shopping.util.hasInitedScrollEvent = false;
        },
        fulfillItemArray: function(itemArray) {
            if (itemArray === undefined || itemArray == null || itemArray.length <= 0) {
                Shopping.util.removeScrollEvent();
                return itemArray;
            } else if (itemArray.length < 20) {
                Shopping.util.removeScrollEvent();
                return itemArray;
            } else {
                Shopping.util.initScrollEvent();
                return itemArray;
            }

            /*var length = itemArray.length;
            for (var i = length; i < 20; i++) {
                var random = Shopping.util.getRandom(length - 1);
                itemArray[itemArray.length] = itemArray[random];
            }
            var resultArray = [];
            for (var i = 0; i < 20 && i < itemArray.length; i++) {
                resultArray[resultArray.length] = itemArray[i];
            }

            return itemArray;
            */
        },
        getRandom: function(max) {
            if (max <= 0)
                return 0;
            var random = Math.random() * max;
            random = Math.round(random);
            if (random > max)
                random = max;

            return random;
        },
        doSleep: function(millis) {
            var nowTime = new Date().getTime();
            while (true) {
                if (new Date().getTime() - millis >= nowTime)
                    return;
            }
        },
        setContainerMarginBottom: function() {
            /*var docHeight = $(document).height();
            var windowHeight = $(window).height();
            var scrollTop = $(window).scrollTop();
            alert(windowHeight);
            alert(docHeight);
            alert(scrollTop);
            var container = Shopping.container;
            alert(docHeight - windowHeight);
            if (docHeight - windowHeight < 30) {
                alert(1);
                var top = container.offset().top;
                var height = container.height();
                var marginBottom = 30 + windowHeight - top - height;
                Shopping.container.css("margin-bottom", marginBottom + "px");
            } else {
                Shopping.container.css("margin-bottom", "0px");
            }*/
        }
    }, Shopping.util);

    Shopping.loading = Shopping.loading || {};
    Shopping.loading = $.extend({
        show: function() {
            var divObj = $(".bottom-loading-div");
            if (divObj.length <= 0) {
                var html = '' +
                    '<div class="bottom-loading-div" style="text-align: center; padding: 20px 0px;">' +
                    '   <img src="/img/aituiguang/loading3.gif" />' +
                    '</div> ' +
                    '';
                divObj = $(html);
                $(".bottom-wrapper").append(divObj);
            }
            divObj.show();
        },
        hide: function() {
            var divObj = $(".bottom-loading-div");
            divObj.hide();
        }
    }, Shopping.loading);


    Shopping.paging = Shopping.paging || {};
    Shopping.paging = $.extend({
        MaxMasonryTimes: 10,
        curMasonryTime: 0,
        addMasonryTime: function() {
            Shopping.paging.curMasonryTime++;
            if (Shopping.paging.curMasonryTime >= Shopping.paging.MaxMasonryTimes) {
                Shopping.util.removeScrollEvent();

                var divObj = $(".bottom-next-page-div");
                if (divObj.length <= 0) {
                    var html = '' +
                        '<div class="bottom-next-page-div" style="text-align: center; padding: 20px 0px;">' +
                        '   <a href="javascript:void(0);" class="next-page-btn">进入下一页 ></a> ' +
                        '</div> ' +
                        '';
                    divObj = $(html);
                    divObj.find(".next-page-btn").click(function() {
                        Shopping.paging.curMasonryTime = 0;
                        divObj.hide();
                        Shopping.show.firstShow();
                    });
                    $(".bottom-wrapper").append(divObj);
                }
                //alert(divObj.length);
                divObj.show();
            }
        }
    }, Shopping.paging);

})(jQuery,window));