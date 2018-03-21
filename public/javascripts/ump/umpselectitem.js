
var TM = TM || {};

((function ($, window) {

    TM.UmpSelectItem = TM.UmpSelectItem || {};


    var UmpSelectItem = TM.UmpSelectItem;


    UmpSelectItem.defaultParams = {

        container: null,
        pagingObj: null,
        tbodyObj: null,
        searchUrl: '',
        initSearchEvent: function(searchCall) {

        },
        initAllChooseEvent: function(allChooseCall, cancelChooseCall) {

        },
        ajaxDataGetCall: function() {

        },
        clickCallback: function() {

        },
        isCanbeAddMore: function(isAlert) {

        }

    }


    UmpSelectItem.init = UmpSelectItem.init || {};
    UmpSelectItem.init = $.extend({


        /**
         * 这里只负责初始化，具体显示show 还需要手动调用一次
         * @param container
         * @param userParams
         */
        doInit: function(container, userParams) {

            //
            userParams = $.extend({}, UmpSelectItem.defaultParams, userParams);

            userParams.container = container;

            UmpSelectItem.userParams = userParams;

            //初始化搜索事件
            var searchCall = function() {
                UmpSelectItem.show.doShow();
            }

            userParams.initSearchEvent(searchCall);

            userParams.initAllChooseEvent(function() {
                UmpSelectItem.event.selectAllItems();
            }, function() {
                UmpSelectItem.event.unSelectAllItems();
            })




        },
        getUserParams: function() {
            return UmpSelectItem.userParams;
        }

    }, UmpSelectItem.init);


    UmpSelectItem.event = UmpSelectItem.event || {};
    UmpSelectItem.event = $.extend({

        selectAllItems: function() {
            var userParams = UmpSelectItem.init.getUserParams();

            var tbodyObj = userParams.tbodyObj;
            var itemTdObjs = tbodyObj.find("td.normal-item");

            itemTdObjs.each(function() {

                var itemTdObj = $(this);

                if (itemTdObj.hasClass("checked-item") == false) {
                    if (userParams.isCanbeAddMore(false) == false) {
                        return;
                    }
                } else {
                    return;
                }

                itemTdObj.addClass("checked-item");
                itemTdObj.find(".select-status-td").html("已选择");

                UmpSelectItem.result.addSelectNumIid(itemTdObj.attr("numIid"));

                userParams.clickCallback();
            });



        },
        unSelectAllItems: function() {
            var userParams = UmpSelectItem.init.getUserParams();

            var tbodyObj = userParams.tbodyObj;
            var itemTdObjs = tbodyObj.find("td.normal-item");

            itemTdObjs.each(function() {

                var itemTdObj = $(this);

                if (itemTdObj.hasClass("checked-item") == false) {
                    return;
                }

                itemTdObj.removeClass("checked-item");
                itemTdObj.find(".select-status-td").html("尚未加入活动");

                UmpSelectItem.result.removeSelectNumIid(itemTdObj.attr("numIid"));
            });

            userParams.clickCallback();
        },
        initNormalItemTdEvent: function(itemTdObjs) {


            itemTdObjs.unbind();

            itemTdObjs.hover(function() {
                var itemTdObj = $(this);

                if (itemTdObj.hasClass("checked-item")) {
                    return;
                }

                itemTdObj.addClass('hover-item');
                itemTdObj.find(".select-status-td").html("点击加入活动");
            }, function() {
                var itemTdObj = $(this);
                itemTdObj.removeClass('hover-item');

                if (itemTdObj.hasClass("checked-item")) {
                    return;
                }


                itemTdObj.find(".select-status-td").html("尚未加入活动");
            });


            var changeCheckStatus = function(itemTdObj) {

                var userParams = UmpSelectItem.init.getUserParams();

                if (itemTdObj.hasClass("checked-item")) {
                    itemTdObj.removeClass("checked-item");
                    itemTdObj.find(".select-status-td").html("尚未加入活动");

                    UmpSelectItem.result.removeSelectNumIid(itemTdObj.attr("numIid"));

                } else {

                    if (userParams.isCanbeAddMore(true) == false) {
                        return;
                    }

                    itemTdObj.addClass("checked-item")
                    itemTdObj.find(".select-status-td").html("已选择");

                    UmpSelectItem.result.addSelectNumIid(itemTdObj.attr("numIid"));


                }



                userParams.clickCallback();

            }

            itemTdObjs.click(function() {
                var itemTdObj = $(this);

                changeCheckStatus(itemTdObj);

            });

            //点了查看宝贝，但不要影响原来的选中状态
            itemTdObjs.find(".select-item-link").unbind().click(function() {
                var itemTdObj = $(this).parents(".normal-item");
                changeCheckStatus(itemTdObj);
            });

        }

    }, UmpSelectItem.event);


    UmpSelectItem.show = UmpSelectItem.show || {};
    UmpSelectItem.show = $.extend({

        targetCurrentPage: 1,
        doShow: function() {
            UmpSelectItem.show.doSearchPage(1);
        },
        doRefresh: function() {
            UmpSelectItem.show.doSearchPage(UmpSelectItem.show.targetCurrentPage);
        },
        doSearchPage: function(currentPage) {

            UmpSelectItem.show.targetCurrentPage = currentPage;

            var userParams = UmpSelectItem.init.getUserParams();
            var pagingObj = userParams.pagingObj;
            var tbodyObj = userParams.tbodyObj;

            var searchUrl = userParams.searchUrl;
            var ajaxParamData = userParams.ajaxDataGetCall();

            if (ajaxParamData == null) {
                return;
            }

            pagingObj.tmpage({
                currPage: currentPage,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: ajaxParamData,
                    dataType: 'json',
                    url: searchUrl,
                    callback: function(dataJson){
                        UmpSelectItem.show.targetCurrentPage = dataJson.pn;

                        tbodyObj.html("");

                        var itemJsonArray = dataJson.res;

                        var allTrHtml = '';
                        
                        var allSelect = true;

                        $(itemJsonArray).each(function(index, itemJson) {

                            if (index % 2 == 0) {
                                allTrHtml += '<tr>';
                            }

                            var tdHtml = UmpSelectItem.item.createItemHtml(index, itemJson);
                            allTrHtml += tdHtml;

                            if (index % 2 == 1) {
                                allTrHtml += '</tr>';
                            }
                            
                            if(UmpSelectItem.result.selectNumIidArray.indexOf(itemJson.numIid.toString()) < 0) {
                            	allSelect = false;
                            }
                        });
                        
                        // 处理本页全选按钮状态
                        if(allSelect) {
                        	$(".choose").hide();
                        	$(".unchoose").show();
                        } else {
                        	$(".choose").show();
                        	$(".unchoose").hide();
                        }

                        if (itemJsonArray.length % 2 == 1) {
                            var tdHtml = UmpSelectItem.item.createItemHtml(1, null);
                            allTrHtml += tdHtml;
                            allTrHtml += '</tr>';
                        }

                        tbodyObj.html(allTrHtml);


                        var itemTdObjs = tbodyObj.find("td.normal-item");

                        UmpSelectItem.event.initNormalItemTdEvent(itemTdObjs);
                    }
                }

            });

        }

    }, UmpSelectItem.show);


    UmpSelectItem.item = UmpSelectItem.item || {};
    UmpSelectItem.item = $.extend({

        createItemHtml: function(index, itemJson) {

            if (itemJson === undefined || itemJson == null) {
                return '<td>&nbsp;</td>'
            }

            var tdClass = '';
            var promotionStatus = '';


            if (index % 2 == 0) {
                tdClass += ' item-left-td ';
            } else {
                tdClass += ' item-right-td ';
            }
            if (itemJson.hasPromotion == true) {
                if (itemJson.isthisActivity == true) {
                    tdClass += ' my-activity-item ';
                    promotionStatus = '已加入本次活动';
                } else {
                    tdClass += ' other-activity-item ';
                    promotionStatus = '已加入其他活动';
                }

                //从选中数组中删除
                UmpSelectItem.result.removeSelectNumIid(itemJson.numIid);

            } else {
                if (UmpSelectItem.result.isInSelectArray(itemJson.numIid) == true) {
                    tdClass += ' normal-item checked-item ';
                    promotionStatus = '已选择';
                } else {
                    tdClass += ' normal-item ';
                    promotionStatus = '尚未加入活动';
                }

            }


            var itemHref = "http://item.taobao.com/item.htm?id=" + itemJson.numIid;

            var priceStr = UmpSelectItem.item.priceToString(itemJson.price);

            var itemHtml = '' +
                '<td class="item-select-td ' + tdClass + '" numIid="' + itemJson.numIid + '">' +
                '   <div class="single-item-out-div"> ' +
                '   <div class="single-item-inner-div"> ' +
                '   <table class="single-item-table">' +
                '       <tr>' +
                '           <td class="select-img-td" rowspan="2"><img class="select-item-img" src="' + itemJson.picURL + '_80x80.jpg" /></td>' +
                '           <td class="select-title-td">' + itemJson.title + '</td>' +
                '           <td class="select-status-td" rowspan="2">' + promotionStatus + '</td>' +
                '       </tr>' +
                '       <tr>' +
                '           <td class="select-price-td">' +
                '               <span class="select-price-span">￥' + priceStr + '</span>&nbsp;&nbsp;&nbsp;&nbsp;' +
                '               <a target="_blank" title="点击进入宝贝详情页" href="' + itemHref + '" class="select-item-link">查看宝贝</a>' +
                '           </td>' +
                '       </tr>' +
                '   </table>' +
                '   </div>' +
                '   </div>' +
                '</td>' +
                '';

            return itemHtml;

        },
        priceToString: function(price) {
            price = price.toFixed(2);

            return price;

        }

    }, UmpSelectItem.item);


    UmpSelectItem.result = UmpSelectItem.result || {};
    UmpSelectItem.result = $.extend({

        selectNumIidArray: [],
        getSelectNumIidArray: function() {

            return UmpSelectItem.result.selectNumIidArray;

        },
        isInSelectArray: function(numIid) {
            for (var i = 0; i < UmpSelectItem.result.selectNumIidArray.length; i++) {

                if (UmpSelectItem.result.selectNumIidArray[i] == numIid) {
                    return true;
                }
            }
            return false;
        },
        addSelectNumIid: function(numIid) {
            UmpSelectItem.result.removeSelectNumIid(numIid);
            UmpSelectItem.result.selectNumIidArray[UmpSelectItem.result.selectNumIidArray.length] = numIid;
        },
        removeSelectNumIid: function(numIid) {

            for (var i = 0; i < UmpSelectItem.result.selectNumIidArray.length; i++) {

                if (UmpSelectItem.result.selectNumIidArray[i] == numIid) {
                    UmpSelectItem.result.selectNumIidArray.splice(i, 1);
                    return;
                }

            }

        },
        removeSomeSelectNumIids: function(numIidArray) {

            for (var i = 0; i < numIidArray.length; i++) {
                UmpSelectItem.result.removeSelectNumIid(numIidArray[i]);
            }

        }

    }, UmpSelectItem.result);




})(jQuery,window));





