var TM = TM || {};
((function ($, window) {
    TM.SpreadItems = TM.SpreadItems || {};

    var SpreadItems = TM.SpreadItems;

    SpreadItems.init = SpreadItems.init || {};
    SpreadItems.init = $.extend({
        doInit: function(container) {
            SpreadItems.container = container;

            var html = SpreadItems.init.createHtml();
            container.html(html);

            container.find(".search-btn").click(function() {
                SpreadItems.search.doSearch();
            });
            container.find(".item-title-input").keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    container.find(".search-btn").click();
                }
            });
            container.find(".category-select").change(function() {
                SpreadItems.search.doSearch();
            });
            container.find(".status-select").change(function() {
                SpreadItems.search.doSearch();
            });
            container.find(".sort-select").change(function() {
                SpreadItems.search.doSearch();
            });

            container.find(".search-btn").click();

            SpreadItems.init.initSpreadNum();
            SpreadItems.init.initCategory();

            container.find(".all-item-check").click(function() {
                var isChecked = $(this).is(":checked");
                container.find(".item-table").find(".item-check").attr("checked", isChecked);
            });

            SpreadItems.event.initBatchEvent();
        },
        createHtml: function() {
            var html = '' +
                '<div class="spread-num-div">' +
                '   <div style="font-weight: bold; font-size: 16px;">当前推广位详情：</div>' +
                '   <table class="base-info-table" style="margin-top: 10px; margin-left: 30px;">' +
                '       <tbody>' +
                '       <tr>' +
                '           <td><span class="tip-dot">总的推广位：</span></td>' +
                '           <td style="width: 70px;"><span class="all-spread-num spread-num"></span>&nbsp;个</td>' +
                '           <td><span class="tip-dot">已用推广位：</span></td>' +
                '           <td style="width: 70px;"><span class="used-spread-num spread-num"></span>&nbsp;个</td>' +
                '           <td><span class="tip-dot">剩余推广位：</span></td>' +
                '           <td style="width: 70px;"><span class="remain-spread-num spread-num"></span>&nbsp;个</td>' +
                //'           <td class="show-detail-td"><a href="javascript:void(0);" class="show-detail-spread-info inlineblock">查看详情>></a></td>' +
                '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '</div> ' +
                '<div class="separator" style="margin-top: 10px;"></div> ' +
                '<div style="text-align: center;margin-top: 10px;">' +
                '   <table class="spread-op-table" style="margin: 0 auto;">' +
                '       <tbody>' +
                '       <tr>' +
                '           <td><span class="batch-spread-btn spread-op-btn">选中设置推广</span></td>' +
                '           <td><span class="batch-suspend-btn spread-op-btn">选中暂停推广</span></td>' +
                '           <td><span class="batch-delete-btn spread-op-btn">选中取消推广</span></td>' +
                '           <td><span class="shop-spread-btn spread-op-btn" style="margin-left: 70px;">全店智能推广</span></td>' +
                '           <td><span class="shop-suspend-btn spread-op-btn">全店暂停推广</span></td>' +
                '           <td><span class="shop-delete-btn spread-op-btn">全店取消推广</span></td>' +
                '       </tr>' +
                '       </tbody> ' +
                '   </table> ' +
                '</div>' +
                '<div class="separator" style="margin-top: 10px;"></div> ' +
                '<table class="base-info-table" style="margin-top: 10px;margin-left: 30px;">' +
                '   <tbody>' +
                '   <tr>' +
                '       <td>类目：</td>' +
                '       <td>' +
                '           <select class="category-select" style="width: 120px; border: 1px solid #999999;">' +
                '               <option selected="selected" value="0">全部</option> ' +
                '           </select> ' +
                '       </td>' +
                '       <td style="padding-left: 10px;">状态：</td>' +
                '       <td>' +
                '           <select class="status-select" style="width: 80px; border: 1px solid #999999;">' +
                '               <option selected="selected" value="0">全部</option> ' +
                '               <option value="1">已推广</option> ' +
                '               <option value="2">未推广</option> ' +
                '           </select> ' +
                '       </td>' +
                '       <td style="padding-left: 10px;">排序：</td>' +
                '       <td>' +
                '           <select class="sort-select" style="width: 90px; border: 1px solid #999999;">' +
                '               <option selected="selected" value="0">默认</option> ' +
                '               <option value="1">销量降序</option> ' +
                '               <option value="2">销量升序</option> ' +
                '           </select> ' +
                '       </td>' +
                '       <td style="padding-left: 10px;">宝贝标题：</td>' +
                '       <td><input type="input" class="item-title-input" style="width: 200px; border: 1px solid #999999;" /> </td>' +
                '       <td style="padding-left: 20px;"><span class="basebtn search-btn">查 询</span> </td>' +
                '   </tr>' +
                '   </tbody>' +
                '</table> ' +
                '<div class="paging-div" style="text-align: center; font-size: 14px;"></div> ' +
                '<table class="result-table item-table" style="width: 100%; margin-top: 10px;">' +
                '   <thead>' +
                '       <td style="width: 8%;"><input type="checkbox" class="all-item-check" /> </td>' +
                '       <td style="width: 15%;">宝贝图片</td>' +
                '       <td style="width: 47%;">宝贝详情</td>' +
                '       <td style="width: 13%;">状态</td>' +
                '       <td style="width: 17%;">操作</td>' +
                '   </thead>' +
                '   <tbody></tbody>' +
                '</table> ' +
                '<div class="paging-div" style="text-align: center; font-size: 14px; margin-top: 10px;"></div> ' +
                '';

            return html;
        },
        initSpreadNum: function() {
            $.ajax({
                url : '/SpreadItem/querySpreadInfo',
                data : {},
                type : 'post',
                success : function(dataJson) {
                    if (TM.SpreadBase.util.checkAjaxSuccess(dataJson) == false)
                        return;
                    var spreadInfo = dataJson.res;
                    SpreadItems.container.find(".all-spread-num").html(spreadInfo.totalNum);
                    SpreadItems.container.find(".used-spread-num").html(spreadInfo.usedNum);
                    SpreadItems.container.find(".remain-spread-num").html(spreadInfo.totalNum - spreadInfo.usedNum);
                }
            });
        },
        initCategory: function() {
            $.ajax({
                url : '/items/sellerCatCount',
                data : {},
                type : 'post',
                success : function(dataJson) {
                    var categoryArray = dataJson;
                    if (categoryArray === undefined || categoryArray == null || categoryArray.length <= 0)
                        return;
                    $(categoryArray).each(function(index, categoryJson) {
                        if (categoryJson.count > 0) {
                            var option = '<option value="' + categoryJson.id + '" >' + categoryJson.name + '(' + categoryJson.count + ')</option>';
                            SpreadItems.container.find(".category-select").append(option);
                        }

                    });
                }
            });
        }
    }, SpreadItems.init);



    SpreadItems.search = SpreadItems.search || {};
    SpreadItems.search = $.extend({
        currentRuleData: {},
        currentPage: 1,
        doSearch: function() {
            var ruleData = SpreadItems.search.getRuleData();
            SpreadItems.search.doShow(1, ruleData);
        },
        doRefresh: function() {
            SpreadItems.search.doShow(SpreadItems.search.currentPage, SpreadItems.search.currentRuleData);
        },
        doShow: function(currentPage, ruleData) {
            if (currentPage < 1)
                currentPage = 1;
            SpreadItems.search.currentPage = currentPage;
            SpreadItems.search.currentRuleData = ruleData;

            var tbodyObj = SpreadItems.container.find(".item-table").find("tbody");
            SpreadItems.container.find(".paging-div").tmpage({
                currPage: currentPage,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: ruleData,
                    dataType: 'json',
                    url: '/SpreadItem/queryItems',
                    callback: function(dataJson){
                        tbodyObj.html("");
                        var itemArray = dataJson.res;
                        $(itemArray).each(function(index, blackJson) {
                            var trObj = SpreadItems.row.createRow(index, blackJson);
                            tbodyObj.append(trObj);
                        });
                    }
                }

            });

        },
        getRuleData: function() {
            var ruleData = {};
            var title = SpreadItems.container.find(".item-title-input").val();
            ruleData.title = title;
            var cid = SpreadItems.container.find(".category-select").val();
            ruleData.cid = cid;
            var status = SpreadItems.container.find(".status-select").val();
            ruleData.status = status;
            var sort = SpreadItems.container.find(".sort-select").val();
            ruleData.sort = sort;
            return ruleData;
        }
    }, SpreadItems.search);


    SpreadItems.row = SpreadItems.row || {};
    SpreadItems.row = $.extend({
        createRow: function(index, itemJson) {
            var html = SpreadItems.row.createHtml();
            var trObj = $(html);
            trObj.find(".item-check").attr("numIid", itemJson.numIid);
            var href = "http://item.taobao.com/item.htm?id=" + itemJson.numIid;
            trObj.find(".item-href").attr("href", href);
            trObj.find(".item-href").attr("target", "_blank");
            trObj.find(".item-img").attr("src", itemJson.picURL);
            trObj.find(".item-title").html(itemJson.title);
            trObj.find(".item-price").html("￥&nbsp;" + itemJson.price + "&nbsp;元");
            trObj.find(".item-trade").html(itemJson.salesCount);

            var numIidList = [];
            numIidList[numIidList.length] = itemJson.numIid;
            if ((itemJson.spreadLevel & 1) > 0) {
                if (itemJson.spreadStatus == 1) {
                    trObj.find(".status-td").html("已推广");
                    trObj.find(".op-td").html('<span class="itemopbtn suspend-btn">暂停</span>')
                    trObj.find(".suspend-btn").click(function() {
                        SpreadItems.batchSubmit.suspendSpread(numIidList);
                    });
                } else {
                    trObj.find(".status-td").html("暂停推广");
                    trObj.find(".op-td").html('<span class="itemopbtn spread-btn">恢复</span>')
                    trObj.find(".spread-btn").click(function() {
                        SpreadItems.batchSubmit.addSpread(numIidList);
                    });
                }
                trObj.find(".status-td").css("color", "#a10000");
                trObj.find(".status-td").css("font-weight", "bold");

                trObj.find(".op-td").append('&nbsp;&nbsp;|&nbsp;&nbsp;');
                trObj.find(".op-td").append('<span class="itemopbtn delete-btn" style="margin-top: 10px;">取消</span>')
                trObj.find(".delete-btn").click(function() {
                    SpreadItems.batchSubmit.deleteSpread(numIidList);
                });
            } else {
                trObj.find(".status-td").html("未推广");
                trObj.find(".op-td").html('<span class="itemopbtn spread-btn">点击推广>></span>');
                trObj.find(".spread-btn").click(function() {
                    SpreadItems.batchSubmit.addSpread(numIidList);
                });
            }



            trObj.find(".item-check").click(function() {
                var isChecked = $(this).is(":checked");
                var allCheckObj = SpreadItems.container.find(".all-item-check");
                if (isChecked == false) {
                    allCheckObj.attr("checked", false);
                } else {
                    var checkedNum = SpreadItems.container.find(".item-table").find(".item-check:checked").length;
                    var allNum = SpreadItems.container.find(".item-table").find(".item-check").length;
                    if (checkedNum == allNum) {
                        allCheckObj.attr("checked", true);
                    }
                }

            });
            return trObj;
        },
        createHtml: function() {
            var html = '' +
                '<tr>' +
                '   <td class="result-td"><input class="item-check" type="checkbox" /> </td>' +
                '   <td class="result-td"><a class="item-href"><img class="item-img" style="width: 110px; height: 100px;" /></a> </td>' +
                '   <td class="result-td">' +
                '       <table class="item-detail-table" >' +
                '           <tbody>' +
                '           <tr>' +
                '               <td style="font-weight: bold;width: 50px;">标题：</td>' +
                '               <td style="text-align: left;"><a class="item-href item-link item-title"></a></td>' +
                '           </tr>' +
                '           <tr>' +
                '               <td style="font-weight: bold;">价格：</td>' +
                '               <td style="text-align: left;" class="item-price"></td>' +
                '           </tr>' +
                '           <tr>' +
                '               <td style="font-weight: bold;">销量：</td>' +
                '               <td style="text-align: left;" class="item-trade"></td>' +
                '           </tr>' +
                '           </tbody>' +
                '       </table> ' +
                '   </td>' +
                '   <td class="result-td status-td"></td>' +
                '   <td class="result-td op-td"></td> ' +
                '</tr>' +
                '';

            return html;
        }
    }, SpreadItems.row);


    SpreadItems.event = SpreadItems.event || {};
    SpreadItems.event = $.extend({
        initBatchEvent: function() {
            var getCheckedNumIidList = function() {
                var checkedObjs = SpreadItems.container.find(".item-table").find(".item-check:checked");
                var numIidList = [];
                checkedObjs.each(function() {
                    numIidList[numIidList.length] = $(this).attr("numIid");
                });
                return numIidList;
            }
            SpreadItems.container.find(".batch-spread-btn").click(function() {
                var numIidList = getCheckedNumIidList();
                SpreadItems.batchSubmit.addSpread(numIidList);
            });
            SpreadItems.container.find(".batch-suspend-btn").click(function() {
                var numIidList = getCheckedNumIidList();
                SpreadItems.batchSubmit.suspendSpread(numIidList);
            });
            SpreadItems.container.find(".batch-delete-btn").click(function() {
                var numIidList = getCheckedNumIidList();
                SpreadItems.batchSubmit.deleteSpread(numIidList);
            });
            //全店
            SpreadItems.container.find(".shop-spread-btn").click(function() {
                SpreadItems.batchSubmit.shopSpread();
            });
            SpreadItems.container.find(".shop-suspend-btn").click(function() {
                SpreadItems.batchSubmit.shopSuspend();
            });
            SpreadItems.container.find(".shop-delete-btn").click(function() {
                SpreadItems.batchSubmit.shopDelete();
            });
        }
    }, SpreadItems.event);

    SpreadItems.batchSubmit = SpreadItems.batchSubmit || {};
    SpreadItems.batchSubmit = $.extend({
        addSpread: function(numIidList) {
            if (numIidList === undefined || numIidList == null || numIidList.length <= 0) {
                alert("请先选择要推广的宝贝");
                return;
            }
            var data = {};
            data.numIidList = numIidList;
            $.ajax({
                url : '/spreadItem/doSpreadItems',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    if (TM.SpreadBase.util.checkAjaxSuccess(dataJson) == false)
                        return;
                    SpreadItems.batchSubmit.refreshPage();
                }
            });
        },
        suspendSpread: function(numIidList) {
            if (numIidList === undefined || numIidList == null || numIidList.length <= 0) {
                alert("请先选择要暂停推广的宝贝");
                return;
            }
            var data = {};
            data.numIidList = numIidList;
            $.ajax({
                url : '/spreadItem/suspendSpread',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    if (TM.SpreadBase.util.checkAjaxSuccess(dataJson) == false)
                        return;
                    SpreadItems.batchSubmit.refreshPage();
                }
            });
        },
        deleteSpread: function(numIidList) {
            if (numIidList === undefined || numIidList == null || numIidList.length <= 0) {
                alert("请先选择要取消推广的宝贝");
                return;
            }
            if (confirm("确定要取消宝贝推广？") == false)
                return;
            var data = {};
            data.numIidList = numIidList;
            $.ajax({
                url : '/spreadItem/deleteSpread',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    if (TM.SpreadBase.util.checkAjaxSuccess(dataJson) == false)
                        return;
                    SpreadItems.batchSubmit.refreshPage();
                }
            });
        },
        shopSpread: function() {

        },
        shopSuspend: function() {
            var data = {};
            $.ajax({
                url : '/spreadItem/shopSuspend',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    if (TM.SpreadBase.util.checkAjaxSuccess(dataJson) == false)
                        return;
                    SpreadItems.batchSubmit.refreshPage();
                }
            });
        },
        shopDelete: function() {
            var data = {};
            if (confirm("确定要全店取消宝贝推广？") == false)
                return;
            $.ajax({
                url : '/spreadItem/shopDelete',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    if (TM.SpreadBase.util.checkAjaxSuccess(dataJson) == false)
                        return;
                    SpreadItems.batchSubmit.refreshPage();
                }
            });
        },
        refreshPage: function() {
            SpreadItems.init.initSpreadNum();
            SpreadItems.search.doSearch();
        }
    }, SpreadItems.batchSubmit);

})(jQuery,window));