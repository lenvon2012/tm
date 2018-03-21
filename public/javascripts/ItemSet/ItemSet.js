
$(document).ready(function() {
    ItemSet.init.doInit();
});

var ItemSet = ItemSet || {};

/**
 * 记录分页的信息
 * @type {*}
 */
ItemSet.pageInfo = ItemSet.pageInfo || {};
ItemSet.pageInfo = {
    currentPage: 1,
    totalPage: 1,
    pageSize: 10
};

/**
 * 初始化
 * @type {*}
 */
ItemSet.init = ItemSet.init || {};
ItemSet.init = $.extend({
    doInit: function() {
        ItemSet.query.doQuery();
        $(".ItemSearchBlock .searchText").keydown(function(event) {
            if (event.keyCode == 13) {//按回车
                $(".ItemSearchBlock #titleSearchBtn").click();
            }
        });
        $(".ItemSearchBlock #titleSearchBtn").click(function() {
            var title = $(".ItemSearchBlock .searchText").val();
            ItemSet.query.ruleJson.title = title;
            ItemSet.query.doQuery();
        });

        //返回列表按钮的事件
        $(".ItemListBlock .returnListBtn").click(function() {
            ItemSet.showItems.returnBigTable();
        });
        $(".ItemListBlock .returnListBtn").mouseover(function() {
            $(".ItemListBlock .returnListBtn").addClass("blueImgBtn");
            $(".ItemListBlock .returnListBtn").removeClass("whiteImgBtn");
        });
        $(".ItemListBlock .returnListBtn").mouseout(function() {
            $(".ItemListBlock .returnListBtn").addClass("whiteImgBtn");
            $(".ItemListBlock .returnListBtn").removeClass("blueImgBtn");
        });
    }
}, ItemSet.init);

/**
 * 查询
 * @type {*}
 */
ItemSet.query = ItemSet.query || {};
ItemSet.query = $.extend({
    ruleJson: {
        title: ''
    },
    //startPage是分页查询后，初始化显示第几页，isPrev指的是，是不是点击了上一个宝贝按钮而导致重新分页
    doQuery: function(startPage, isPrev) {
        var isSetBigTable = true;
        var opt = {};
        if (startPage === undefined || startPage == null) {
            isSetBigTable = true;
        } else {
            isSetBigTable = false;
            opt.startPage = startPage;
        }

        var ruleJson = ItemSet.query.ruleJson;
        opt = $.extend({
            countUrl: "/itemset/queryItemCount",
            dataUrl: "/itemset/queryItems",
            pageSize: ItemSet.pageInfo.pageSize,
            getRuleData: function(isCurrentPage, currentPage) {//isCurrentPage判断是否需要当前页的条件
                ItemSet.pageInfo.currentPage = currentPage;
                var data = {};
                data.pn = currentPage;
                data.s = ruleJson.title;
                data.ps = ItemSet.pageInfo.pageSize;
                return data;
            },
            parseTotalCount: function(resultJson) {
                var totalPage = Math.ceil(resultJson / ItemSet.pageInfo.pageSize);
                ItemSet.pageInfo.totalPage = totalPage;
                return resultJson;
            },
            queryCallback: function(dataJson) {
                ItemSet.showItems.show(dataJson, isSetBigTable, isPrev);
                isSetBigTable = true;
            }
        }, opt);

        $(".ItemListBlock .pagingDiv").setPaging(opt);
    }
}, ItemSet.query);

/**
 * 显示宝贝列表
 * @type {*}
 */
ItemSet.showItems = ItemSet.showItems || {};
ItemSet.showItems = $.extend({
    show: function(itemArr, isSetBigTable, isPrev) {
        if (isSetBigTable == true)
            ItemSet.showItems.returnBigTable();
        $(".ItemListDiv .bigTable tbody").html("");
        $(".ItemListDiv .smallTable tbody").html("");
        $(itemArr).each(function(index, itemJson) {
            var bigTrObj = ItemSet.bigTable.createBigRow(itemJson);
            $(".ItemListDiv .bigTable tbody").append(bigTrObj);
            var smallTrObj = ItemSet.smallTable.createSmallRow(itemJson);
            $(".ItemListDiv .smallTable tbody").append(smallTrObj);
            //设置事件
            ItemSet.bigTable.setEvent(bigTrObj, smallTrObj);
            var itemIndex = (ItemSet.pageInfo.currentPage - 1) * ItemSet.pageInfo.pageSize + index;
            smallTrObj.attr("itemIndex", itemIndex);
            ItemSet.smallTable.setEvent(smallTrObj);
        });
        if (isSetBigTable == false) {
            var trArray = $(".ItemListDiv .smallTable tbody").find("tr");
            if (trArray.length > 0) {
                if (isPrev == true) {
                    $(trArray[trArray.length - 1]).click();
                } else {
                    $(trArray[0]).click();
                }

            }

        }
    },
    //恢复大的列表
    returnBigTable: function() {
        $(".ItemListBlock .ItemOpDiv").hide();
        $(".ItemListBlock .returnListBtn").hide();
        $(".ItemListBlock .ItemListDiv").removeClass("ItemSmallList");
        $(".ItemListBlock .ItemListDiv").addClass("ItemBigList");
        $(".ItemListDiv .smallTable").hide();
        $(".ItemListDiv .bigTable").show();
    }
}, ItemSet.showItems);


/**
 * 显示宝贝的表格
 * @type {*}
 */
ItemSet.bigTable = ItemSet.bigTable || {};
ItemSet.bigTable = $.extend({
    createBigRow: function(itemJson) {
        var trHtml = "<tr>" +
            "<td class='imgTd'>" +
            "   <a href='javascript:void(0);' target='_blank' class='itemPageLink'>" +
            "       <img class='itemImg' src='' />" +
            "   </a>" +
            "</td>" +
            "<td class='titleTd'>" +
            "   <span class='itemTitle'></span>" +
            "   <a href='javascript:void(0);' target='_blank'  class='itemPageLink'><span class='whiteImgBtn'>查看宝贝</span></a>" +
            "</td> " +
            "<td class='itemOpTd'><span class='blueImgBtn itemDetailBtn'>宝贝详细</span></td>" +
            "</tr>";
        var trObj = $(trHtml);
        var url = "http://item.taobao.com/item.htm?id=" + itemJson.id;
        //链接
        trObj.find('.itemPageLink').attr("href", url);
        //图片
        trObj.find(".itemImg").attr("src", itemJson.pic);
        //标题
        trObj.find(".itemTitle").html(itemJson.name);

        return trObj;
    },
    setEvent: function(bigTrObj, smallTrObj) {
        bigTrObj.find(".itemDetailBtn").click(function() {
            $(".ItemListBlock .ItemListDiv").removeClass("ItemBigList");
            $(".ItemListBlock .ItemListDiv").addClass("ItemSmallList");
            $(".ItemListDiv .bigTable").hide();
            $(".ItemListDiv .smallTable").show();
            $(".ItemListBlock .ItemOpDiv").show();
            $(".ItemListBlock .returnListBtn").show();

            smallTrObj.click();
        });
    }
}, ItemSet.bigTable);

/**
 * 缩小后的左边列表
 * @type {*}
 */
ItemSet.smallTable = ItemSet.smallTable || {};
ItemSet.smallTable = $.extend({
    createSmallRow: function(itemJson) {
        var trHtml = "<tr class='stNormalTr'>" +
            "<td class='imgTd'><img class='itemImg' src='' /></td>" +
            "</tr>";
        var trObj = $(trHtml);
        trObj.find(".itemImg").attr("src", itemJson.pic);

        trObj[0].itemJson = itemJson;

        return trObj;
    },
    setEvent: function(trObj) {
        trObj.click(function() {
            if (trObj.hasClass("stSelectTr"))
                return;
            var itemJson = this.itemJson;
            $(".ItemListDiv .smallTable tbody tr").removeClass("stSelectTr");
            $(this).removeClass("normalTr");
            $(this).addClass("stSelectTr");
            var itemIndex = parseInt($(this).attr("itemIndex"));
            ItemOpHandler.init.doInit(itemJson, itemIndex);
        });
    }
}, ItemSet.smallTable);


/**
 * 点击一下宝贝，然后右边出现宝贝的详情
 * @type {*}
 */
var ItemOpHandler = ItemOpHandler || {};

ItemOpHandler.itemJson = null;

ItemOpHandler.init = ItemOpHandler.init || {};
ItemOpHandler.init = $.extend({
    doInit: function(itemJson, itemIndex) {
        ItemOpHandler.itemJson = itemJson;
        $(".ItemDescDiv").find(".curItemTitle").html(itemJson.name);
        var url = "http://item.taobao.com/item.htm?id=" + itemJson.id;
        $(".ItemDescDiv").find(".itemPageLink").attr("href", url);

        ItemOpHandler.jumpBtn.setEvent(itemIndex);

        ItemOpPlugin.init.doInit(itemJson);
    }
}, ItemOpHandler.init);

/**
 * 上一个宝贝，下一个宝贝的按钮
 * @type {*}
 */
ItemOpHandler.jumpBtn = ItemOpHandler.jumpBtn || {};
ItemOpHandler.jumpBtn = $.extend({
    setEvent: function(itemIndex) {
        $(".ItemDescDiv").find(".prevItemBtn").unbind();
        $(".ItemDescDiv").find(".nextItemBtn").unbind();
        var currentPage = ItemSet.pageInfo.currentPage;
        var totalPage = ItemSet.pageInfo.totalPage;
        ItemOpHandler.jumpBtn.prevItemEvent(itemIndex);
        ItemOpHandler.jumpBtn.nextItemEvent(itemIndex);
    },
    prevItemEvent: function(itemIndex) {
        if (itemIndex <= 0) {
            $(".ItemDescDiv").find(".prevItemBtn").addClass("whiteImgBtn");
            $(".ItemDescDiv").find(".prevItemBtn").removeClass("blueImgBtn");
        } else {
            $(".ItemDescDiv").find(".prevItemBtn").addClass("blueImgBtn");
            $(".ItemDescDiv").find(".prevItemBtn").removeClass("whiteImgBtn");
            var prevItemIndex = itemIndex - 1;
            var prevItemTr = $(".ItemListDiv .smallTable tbody").find("tr[itemIndex='" + prevItemIndex + "']");
            if (prevItemTr.length > 0) {
                $(".ItemDescDiv").find(".prevItemBtn").click(function() {
                    prevItemTr.click();
                });
            } else {
                var currentPage = ItemSet.pageInfo.currentPage;
                if (currentPage > 1) {
                    $(".ItemDescDiv").find(".prevItemBtn").click(function() {
                        ItemSet.query.doQuery(currentPage - 1, true);
                    });
                }
            }
        }
    },
    nextItemEvent: function(itemIndex) {
        var nextItemIndex = itemIndex + 1;
        var nextItemTr = $(".ItemListDiv .smallTable tbody").find("tr[itemIndex='" + nextItemIndex + "']");
        var currentPage = ItemSet.pageInfo.currentPage;
        var totalPage = ItemSet.pageInfo.totalPage;
        if (nextItemTr.length == 0 && currentPage == totalPage) {
            $(".ItemDescDiv").find(".nextItemBtn").addClass("whiteImgBtn");
            $(".ItemDescDiv").find(".nextItemBtn").removeClass("blueImgBtn");
            return;
        } else {
            $(".ItemDescDiv").find(".nextItemBtn").addClass("blueImgBtn");
            $(".ItemDescDiv").find(".nextItemBtn").removeClass("whiteImgBtn");
            if (nextItemTr.length > 0) {
                $(".ItemDescDiv").find(".nextItemBtn").click(function() {
                    nextItemTr.click();
                });
            } else {
                $(".ItemDescDiv").find(".nextItemBtn").click(function() {
                    ItemSet.query.doQuery(currentPage + 1, false);
                });
            }
        }
    }
}, ItemOpHandler.jumpBtn);


/**
 * 宝贝功能插件
 * @type {*}
 */
var ItemOpPlugin = ItemOpPlugin || {};

ItemOpPlugin.load = {
    isBusMonitorLoad: false
};


ItemOpPlugin.init = ItemOpPlugin.init || {};
ItemOpPlugin.init = $.extend({
    doInit: function(itemJson) {
        if (ItemOpPlugin.load.isBusMonitorLoad == false) {
            $.getScript('/js/highcharts.js',function(){
                $.getScript('/js/BusMonitor/BusMonitor.js',function(){
                    ItemOpPlugin.load.isBusMonitorLoad = true;
                    BusMonitor.init.doInit($(".targetOpDiv"), itemJson.id);
                });
            });
        } else {
            BusMonitor.init.doInit($(".targetOpDiv"), itemJson.id);
        }


    }
}, ItemOpPlugin.init);

