
var TM = TM || {};
((function ($, window) {

    TM.SkinBatch = TM.SkinBatch || {};

    var SkinBatch = TM.SkinBatch;

    /**
     * 初始化
     * @type {*}
     */
    SkinBatch.init = SkinBatch.init || {};
    SkinBatch.init = $.extend({
        doInit: function(container, isModifyPrice) {

            SkinBatch.init.isModifyPrice = isModifyPrice;

            var html = SkinBatch.init.createHtml();
            container.html(html);
            SkinBatch.container = container;

            SkinBatch.init.initSearchDiv();
            SkinBatch.init.initBatchOpDiv();
            SkinBatch.init.initItemContainer();
            //第一次执行
            SkinBatch.show.doShow();
        },
        createHtml: function() {
            var html = '' +
                '<div class="search-container"></div> ' +
                '<div class="batchop-div" style="text-align: left;"></div>' +
                '<div class="paging-div"></div>' +
                '<div class="items-container"></div> ' +
                '<div class="paging-div"></div>' +
                '<div class="batchop-div" style="text-align: left;"></div>' +
                '<div class="error-item-div" style="margin-top: 10px;">' +
                '   <span class="error-tip-span">宝贝操作失败列表：</span> ' +
                '   <table class="error-item-table list-table">' +
                '       <thead>' +
                '       <tr>' +
                '           <td style="width: 15%;">宝贝图片</td>' +
                '           <td style="width: 45%;">标题</td>' +
                '           <td style="width: 20%;">失败说明</td>' +
                '           <td style="width: 15%;">操作时间</td>' +
                '       </tr>' +
                '       </thead>' +
                '       <tbody class="error-item-tbody"></tbody>' +
                '   </table> ' +
                '</div>' +
                '';

            return html;
        },
        //搜索的div
        initSearchDiv: function() {
            var html = '' +
                '   <table>' +
                '       <tbody>' +
                '       <tr>' +
                '           <td style="padding-left: 10px;"><span>宝贝标题：</span><input type="text" class="search-text" /> </td>' +
                '           <td style="padding-left: 20px;"><span>分类：</span><select class="search-select category-select"><option value="0" selected="selected">所有分类</option> </select> </td>' +
                '           <td style="padding-left: 20px;">' +
                '               <span>宝贝状态：</span>' +
                '               <select class="search-select state-select">' +
                '                   <option value="OnSale" selected="selected">在架宝贝</option> ' +
                '                   <option value="InStock">待上架宝贝</option> ' +
                '                   <option value="All">所有宝贝</option> ' +
                '               </select> ' +
                '           </td>' +
                '           <td style="padding-left: 20px;">' +
                '               <span class="tmbtn sky-blue-btn search-btn">搜索宝贝</span> ' +
                '           </td>' +
                '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '';
            SkinBatch.container.find(".search-container").html(html);

            //类目
            $.get("/home/getSellerCat",function(data){
                var categorySelectObj = SkinBatch.container.find(".category-select");
                for (var i = 0; i < data.length; i++) {
                    //if (data[i].parentCid == 0) {//一级类目
                    var optionObj = $('<option></option>');
                    optionObj.attr("value", data[i].cid);
                    optionObj.html(data[i].name);
                    categorySelectObj.append(optionObj);
                    //}
                }
            });

            //添加事件
            SkinBatch.container.find(".category-select").change(function() {
                SkinBatch.show.doShow();
            });
            SkinBatch.container.find(".state-select").change(function() {
                SkinBatch.show.doShow();
            });
            SkinBatch.container.find(".search-text").keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    SkinBatch.container.find(".search-btn").click();
                }
            });
            SkinBatch.container.find(".search-btn").click(function() {
                SkinBatch.show.doShow();
            });

        },
        //功能按钮
        initBatchOpDiv: function() {
            var html = '' +
                '   <table style="">' +
                '       <tbody>' +
                '           <tr>';
            if (SkinBatch.init.isModifyPrice != true) {
                html += '               <td style="padding-left: 0px;"><span class="tmbtn sky-blue-btn batch-list-btn">批量上架</span> </td>' +
                        '               <td><span class="tmbtn short-green-btn batch-delist-btn">批量下架</span> </td>' +
                        '               <td><span class="tmbtn sky-blue-btn batch-shop-list-btn">全店上架</span> </td>' +
                        '               <td><span class="tmbtn short-green-btn batch-shop-delist-btn">全店下架</span> </td>';
            } else {
                html += '               <td><span class="tmbtn yellow-btn batch-modify-price-btn">批量改价</span> </td>';
            }

            html += '' +
                '           </tr>' +
                '       </tbody>' +
                '   </table>' +
                '';
            SkinBatch.container.find(".batchop-div").html(html);

            var getCheckedItems = function() {
                var checkObjs = SkinBatch.container.find(".item-checkbox:checked");
                var numIidArray = [];
                checkObjs.each(function() {
                    numIidArray[numIidArray.length] = $(this).attr("numIid");
                });
                return numIidArray;
            };
            //添加事件
            SkinBatch.container.find(".batch-list-btn").click(function() {
                var numIidArray = getCheckedItems();
                if (numIidArray.length == 0) {
                    alert("请先选择要上架的宝贝");
                    return;
                }
                if (confirm("确定要上架" + numIidArray.length + "个宝贝?") == false) {
                    return;
                }
                SkinBatch.submit.doListing(numIidArray);
            });
            SkinBatch.container.find(".batch-delist-btn").click(function() {
                var numIidArray = getCheckedItems();
                if (numIidArray.length == 0) {
                    alert("请先选择要下架的宝贝");
                    return;
                }
                if (confirm("确定要下架" + numIidArray.length + "个宝贝?") == false) {
                    return;
                }
                SkinBatch.submit.doDeListing(numIidArray);
            });
            SkinBatch.container.find(".batch-shop-list-btn").click(function() {
                if (confirm("确定要上架所有仓库中的宝贝?") == false) {
                    return;
                }
                SkinBatch.submit.doShopListing();
            });
            SkinBatch.container.find(".batch-shop-delist-btn").click(function() {
                if (confirm("确定要下架全店宝贝么?请谨慎操作!") == false) {
                    return;
                }
                SkinBatch.submit.doShopDeListing();
            });
            SkinBatch.container.find(".batch-modify-price-btn").click(function() {
                var numIidArray = getCheckedItems();
                if (numIidArray.length <= 0) {
                    alert("请先选择要修改价格的宝贝");
                    return;
                }
                SkinBatch.submit.doModifyItems(numIidArray);
            });
        },
        //宝贝表格
        initItemContainer: function() {
            var html = '' +
                '<table class="item-table list-table busSearch">' +
                '   <thead>' +
                '   <tr>' +
                '       <td style="width: 8%;"><input type="checkbox" class="select-all-item width17" /> </td>' +
                '       <td style="width: 13%;">宝贝主图</td>' +
                '       <td style="width: 30%;">标题</td>' +
                '       <td style="width: 7%;"><div class="sort-td sort-up" orderBy="salesCount">销量</div></td>' +
                '       <td style="width: 12%;"><div class="sort-td sort-up" orderBy="delist">下架时间</div></td>' +
                '       <td style="width: 15%;">价格</td>' +
                '       <td style="width: 15%;">状态</td>' +
            /*if (SkinBatch.init.isModifyPrice != true) {
                html += '       <td style="width: 15%;">状态</td>';
            } else {
                html += '       <td style="width: 15%;">价格</td>';
            }

            html += "" +*/
                '   </tr>' +
                '   </thead>' +
                '   <tbody class="item-list-tbody"></tbody>' +
                '</table>' +
                '';

            SkinBatch.container.find(".items-container").html(html);

            //设置事件
            SkinBatch.container.find(".select-all-item").click(function() {
                var isChecked = $(this).is(":checked");
                var checkObjs = SkinBatch.container.find(".item-checkbox");
                checkObjs.attr("checked", isChecked);
            });


            var sortTdObjs = SkinBatch.container.find(".sort-td");
            sortTdObjs.click(function() {
                if ($(this).hasClass("sort-up")) {
                    $(this).removeClass("sort-up");
                    $(this).addClass("sort-down");
                } else {
                    $(this).removeClass("sort-down");
                    $(this).addClass("sort-up");
                }
                sortTdObjs.removeClass("current-sort");
                $(this).addClass("current-sort");
                SkinBatch.show.doShow();
            });
        }
    }, SkinBatch.init);

    /**
     * 查询
     * @type {*}
     */
    SkinBatch.show = SkinBatch.show || {};
    SkinBatch.show.orderData = {
        asc: "asc",
        desc: "desc"
    };
    SkinBatch.show = $.extend({
        currentPage: 1,
        doShow: function(currentPage) {
            var ruleData = SkinBatch.show.getQueryRule();
            var itemTbodyObj = SkinBatch.container.find(".item-table").find("tbody.item-list-tbody");
            itemTbodyObj.html("");
            //alert(currentPage);
            if (currentPage === undefined || currentPage == null || currentPage <= 0)
                currentPage = 1;
            SkinBatch.container.find(".paging-div").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: ruleData,
                    dataType: 'json',
                    url: '/skinbatch/queryItems',
                    callback: function(dataJson){
                        SkinBatch.show.currentPage = dataJson.pn;//记录当前页
                        itemTbodyObj.html("");
                        var itemArray = dataJson.res;
                        //SkinBatch.container.find(".select-all-item").attr("checked", true);
                        $(itemArray).each(function(index, itemJson) {
                            var trObj = SkinBatch.row.createRow(index, itemJson);
                            itemTbodyObj.append(trObj);
                        });
                    }
                }

            });
        },
        refresh: function() {
            SkinBatch.show.doShow(SkinBatch.show.currentPage);
        },
        getQueryRule: function() {
            var ruleData = {};
            var title = SkinBatch.container.find(".search-text").val();
            var state = SkinBatch.container.find(".state-select").val();
            var catId = SkinBatch.container.find(".category-select").val();


            var orderObj = SkinBatch.container.find(".current-sort");
            var orderBy = "";
            var isDesc = false;

            if (orderObj.length <= 0) {
                orderBy = "";
            } else {
                orderBy = orderObj.attr("orderBy");
                if (orderObj.hasClass("sort-down"))
                    isDesc = true;
                else
                    isDesc = false;
            }

            ruleData.orderProp = orderBy;
            if (isDesc == false) {
                ruleData.orderType = SkinBatch.show.orderData.asc;
            } else {
                ruleData.orderType = SkinBatch.show.orderData.desc;
            }

            ruleData.title = title;
            ruleData.state = state;
            ruleData.catId = catId;

            return ruleData;
        }
    }, SkinBatch.show);


    /**
     * 显示一行宝贝
     * @type {*}
     */
    SkinBatch.row = SkinBatch.row || {};
    SkinBatch.row = $.extend({
        createRow: function(index, itemJson) {
            var html = '' +
                '<tr class="modify-price-tr">' +
                '   <td><input type="checkbox" class="item-checkbox width17" /></td>' +
                '   <td><a class="item-link" target="_blank"><img class="item-img" /></a> </td>' +
                '   <td><a class="item-link" target="_blank"><span class="item-title" style="color: #333;"></span></a> </td>' +
                '   <td><span class="sale-count"></span> </td>' +
                '   <td><p>' + itemJson.delistWeekDay + '</p><p style="padding-top: 5px;">' + itemJson.delistHHmmss + '</p></td>' +
                //'   <td><span class="inventory"></span> </td>' +
                '   <td>' +
                '       <div class="item-price"></div> ';

            if (SkinBatch.init.isModifyPrice == true) {
                html += "" +
                    '       <div class="tmbtn yellow-btn item-modify-price-btn">修改价格</div>' +
                    "";
            }
            html += "" +
                '   </td>' +
                '   <td>' +
                '       <div class="item-state"></div> ';

            if (SkinBatch.init.isModifyPrice != true) {
                html += "" +
                    '       <div class="tmbtn sky-blue-btn item-delist-btn"></div>' +
                    "";
            }
            html += "" +
                '   </td>' +
                '</tr>' +
                '' +
                '';

            var trObj = $(html);
            /*if (itemJson.id === undefined || itemJson.id == null || itemJson.id == 0) {

             } */

            var numIid = itemJson.numIid;
            if (numIid === undefined || numIid == null || numIid <= 0) {
                itemJson.numIid = itemJson.id;
                numIid = itemJson.id;
            }

            var url = "http://item.taobao.com/item.htm?id=" + itemJson.numIid;
            trObj.find(".item-checkbox").attr("numIid", itemJson.numIid);
            trObj.find(".item-checkbox").each(function() {
                this.itemJson = itemJson;
            });
            trObj.find(".item-link").attr("href", url);
            trObj.find(".item-img").attr("src", itemJson.picURL);
            trObj.find(".item-title").html(itemJson.title);
            trObj.find(".sale-count").html(itemJson.salesCount);
            trObj.find(".inventory").html(itemJson.quantity);
            trObj.find(".item-price").html("￥" + itemJson.price);
            var state = "";
            if (itemJson.status == 0) {
                state = "在仓库";
            } else if (itemJson.status == 1) {
                state = "在架上";
            }
            trObj.find(".item-state").html(state);
            var btnObj = trObj.find(".item-delist-btn");
            if (itemJson.status == 0) {
                btnObj.html("立即上架");
                btnObj.click(function() {
                    var itemCheckObj = $(this).parents("tr.modify-price-tr").find(".item-checkbox");
                    var numIid = itemCheckObj.attr("numIid");
                    var numIidArray = [];
                    numIidArray[numIidArray.length] = numIid;
                    if (confirm("确定上架宝贝：" + itemJson.title + "？") == false)
                        return;
                    SkinBatch.submit.doListing(numIidArray);
                });
            } else if (itemJson.status == 1) {
                btnObj.html("立即下架");
                btnObj.click(function() {
                    var itemCheckObj = $(this).parents("tr.modify-price-tr").find(".item-checkbox");
                    var numIid = itemCheckObj.attr("numIid");
                    var numIidArray = [];
                    numIidArray[numIidArray.length] = numIid;
                    if (confirm("确定下架宝贝：" + itemJson.title + "？") == false)
                        return;

                    SkinBatch.submit.doDeListing(numIidArray);
                });
            }
            trObj.find(".item-modify-price-btn").click(function() {
                var itemCheckObj = $(this).parents("tr.modify-price-tr").find(".item-checkbox");
                var numIid = itemCheckObj.attr("numIid");
                var itemJson = itemCheckObj[0].itemJson;
                SkinBatch.submit.doModifyOneItem(numIid, itemJson);
            });


            var checkCallback = function(checkObj) {
                var isChecked = checkObj.is(":checked");
                if (isChecked == false) {
                    SkinBatch.container.find(".select-all-item").attr("checked", false);
                } else {
                    var checkObjs = SkinBatch.container.find(".item-checkbox");
                    var flag = true;
                    checkObjs.each(function() {
                        if ($(this).is(":checked") == false)
                            flag = false;
                    });
                    SkinBatch.container.find(".select-all-item").attr("checked", flag);
                }
            }
            trObj.find(".item-checkbox").click(function() {
                checkCallback($(this));
            });
            /*trObj.find(".item-title").click(function() {
             var checkObj = $(this).parents("tr.modify-price-tr").find(".item-checkbox");
             var isChecked = checkObj.is(":checked");
             if (isChecked == true)
             checkObj.attr("checked", false);
             else
             checkObj.attr("checked", true);
             checkCallback(checkObj);
             });*/


            return trObj;

        }
    }, SkinBatch.row);

    /**
     * 提交批量操作的功能
     * @type {*}
     */
    SkinBatch.submit = SkinBatch.submit || {};
    SkinBatch.submit = $.extend({
        // 批量上架
        doListing: function(numIidArray) {
            var data = {};
            data.numIidList = numIidArray;
            $.ajax({
                url : '/skinbatch/doBatchListing',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    if (SkinBatch.util.checkAjaxSuccess(dataJson) == false)
                        return;
                    SkinBatch.error.showErrors(dataJson.res, true);
                    SkinBatch.show.refresh();
                }
            });
        },
        // 批量下架
        doDeListing: function(numIidArray) {
            var data = {};
            data.numIidList = numIidArray;
            $.ajax({
                url : '/skinbatch/doBatchDeListing',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    if (SkinBatch.util.checkAjaxSuccess(dataJson) == false)
                        return;
                    SkinBatch.error.showErrors(dataJson.res, true);
                    SkinBatch.show.refresh();
                }
            });
        },
        // 全店上架
        doShopListing: function() {
            $.ajax({
                url : '/skinbatch/doShopListing',
                data : {},
                type : 'post',
                success : function(dataJson) {
                    if (SkinBatch.util.checkAjaxSuccess(dataJson) == false)
                        return;
                    SkinBatch.error.showErrors(dataJson.res, true);
                    SkinBatch.show.refresh();
                }
            });
        },
        // 全店下架
        doShopDeListing: function() {
            $.ajax({
                url : '/skinbatch/doShopDeListing',
                data : {},
                type : 'post',
                success : function(dataJson) {
                    if (SkinBatch.util.checkAjaxSuccess(dataJson) == false)
                        return;
                    SkinBatch.error.showErrors(dataJson.res, true);
                    SkinBatch.show.refresh();
                }
            });
        },
        doModifyOneItem: function(numIid, itemJson) {
            /*$.ajax({
                url : '/skinbatch/checkAuth',
                data : {},
                type : 'post',
                success : function(dataJson) {
                    if (SkinBatch.util.checkAjaxSuccess(dataJson) == false) {
                        SkinBatch.submit.checkAuth(dataJson);
                        return;
                    }
                    SkinBatch.skuPrice.doModifyOneItem(numIid, itemJson);
                }
            });*/
            SkinBatch.skuPrice.doModifyOneItem(numIid, itemJson);

        },
        doModifyItems: function(numIidArray) {
            /*$.ajax({
                url : '/skinbatch/checkAuth',
                data : {},
                type : 'post',
                success : function(dataJson) {
                    if (SkinBatch.util.checkAjaxSuccess(dataJson) == false) {
                        SkinBatch.submit.checkAuth(dataJson);
                        return;
                    }
                    SkinBatch.skuPrice.doModifyItems(numIidArray);
                }
            });*/

            SkinBatch.skuPrice.doModifyItems(numIidArray);
        },
        checkAuth: function(dataJson) {
            /*var authorizedUrl = dataJson.taobaoAuthorizeUrl;
            if (authorizedUrl === undefined || authorizedUrl == null || authorizedUrl == "") {

            } else {


                alert("亲，您改价的权限已过期，请重新授权！");


                location.href = authorizedUrl;
            }
            return;*/


            SkinBatch.util.checkIsW2AuthError(dataJson);

        }
    }, SkinBatch.submit);



    SkinBatch.skuPrice = SkinBatch.skuPrice || {};
    SkinBatch.skuPrice = $.extend({

        doModifyOneItem: function(numIid, itemJson) {
            $(".modify-price-dialog").remove();

            var href = "http://item.taobao.com/item.htm?id=" + numIid;

            var html = '' +
                '<div class="modify-price-dialog" style="padding-top: 20px;">' +
                '   <div style="padding-bottom: 20px;">宝贝标题：' +
                '       <a target="_blank" href="' + href + '"><span style="color: #f60;">' + itemJson.title + '</span></a>' +
                '   </div>' +
                '   <table style="margin: 0 auto;">' +
                '      <tbody>' +
                '      <tr>' +
                '           <td>' +
                '               <input type="radio" name="modify-price-type-radio" class="modify-price-type-radio" checked="checked" targetDiv="direct-modify-price-div"/><span class="modify-price-type-span">直接修改价格</span> ' +
                '           </td>' +
                '           <td style="padding-left: 10px;">' +
                '               <input type="radio" name="modify-price-type-radio" class="modify-price-type-radio" targetDiv="modify-price-byscale-div"/><span class="modify-price-type-span">宝贝比例改价</span> ' +
                '           </td>' +
                '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '   <div style="height: 10px;"></div> ' +
                '   <div class="direct-modify-price-div modify-div">' +
                '       <table class="direct-price-table" style="border-collapse: collapse;text-align: center;margin: 0 auto;">' +
                '           <tbody>' +
                '           <tr>' +
                '               <td style="color: #f60;text-align: right;">一口价：</td>' +
                '               <td style="padding-left: 10px;text-align: left;">新的价格为&nbsp;<input type="text" class="item-new-price-input" value="' + itemJson.price + '" style="width: 70px;"/>&nbsp;元</td>' +
                '           </tr>' +
                '           </tbody>' +
                '       </table>' +
                '   </div> ' +
                '   <div class="modify-price-byscale-div modify-div" style="display: none;">' +
                '       <div style="color: #a10000;padding-bottom: 10px;">(根据比例修改价格，如输入110，则价格上涨10%；如输入90，则价格下降10%。)</div>' +
                '       <table class="scale-price-table" style="border-collapse: collapse;text-align: center;margin: 0 auto;">' +
                '           <tbody>' +
                '           <tr>' +
                '               <td style="color: #f60;text-align: right;">一口价：</td>' +
                '               <td style="padding-left: 10px;text-align: left;">新的价格为<span class="red">原价</span>的&nbsp;<input type="text" value="100" class="scale-new-price-input"  style="width: 70px;"/>&nbsp;% </td>' +
                '           </tr>' +
                '           </tbody>' +
                '       </table>' +
                '   </div> ' +
                '</div>';

            var dialogObj = $(html);
            var showDivCallback = function() {
                dialogObj.find(".modify-div").hide();
                var divCss = dialogObj.find(".modify-price-type-radio:checked").attr("targetDiv");
                dialogObj.find("." + divCss).show();
            };
            dialogObj.find(".modify-price-type-span").click(function() {
                $(this).parent().find(".modify-price-type-radio").attr("checked", true);
                showDivCallback();
            });
            dialogObj.find(".modify-price-type-radio").click(function() {
                showDivCallback();
            });


            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:500,
                width:650,
                title:'修改一个宝贝价格',
                autoOpen: false,
                resizable: false,
                buttons:{'确定':function() {
                    var divCss = dialogObj.find(".modify-price-type-radio:checked").attr("targetDiv");

                    if (divCss == "direct-modify-price-div") {
                        SkinBatch.skuPrice.directEditItemPrice(numIid, itemJson, dialogObj, dialogObj.find(".direct-modify-price-div"));
                    } else if (divCss == "modify-price-byscale-div") {
                        SkinBatch.skuPrice.scaleOneItemPrice(numIid, itemJson, dialogObj, dialogObj.find(".modify-price-byscale-div"));
                    } else {
                        alert("系统出现了一些意外，请联系我们！");
                    }


                },'取消':function(){
                    $(this).dialog('close');
                }}
            });

            SkinBatch.skuPrice.initItemSkuPrices(numIid, dialogObj);

            dialogObj.dialog("open");



        },
        doModifyItems: function(numIidArray) {

            $(".modify-price-dialog").remove();

            var html = '' +
                '<div class="modify-price-dialog" style="padding-top: 20px;">' +
                '   <div style="color: #a10000;padding-bottom: 10px;">(根据比例修改价格，如输入110，则价格上涨10%；如输入90，则价格下降10%。)</div>' +
                '   <table class="scale-price-table" style="border-collapse: collapse;text-align: center;margin: 0 auto;">' +
                '       <tbody>' +
                '       <tr>' +
                '           <td>宝贝根据比例改价：</td>' +
                '           <td style="padding-left: 10px;">新的价格为<span class="red">原价</span>的&nbsp;<input type="text" class="item-price-scale-input"  style="width: 100px;" value="100"/>&nbsp;% </td>' +
                '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '</div>';


            var dialogObj = $(html);

            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:300,
                width:500,
                title:'批量修改宝贝价格',
                autoOpen: false,
                resizable: false,
                buttons:{'确定':function() {
                    SkinBatch.skuPrice.scaleSomeItemsPrice(numIidArray, dialogObj);

                },'取消':function(){
                    $(this).dialog('close');
                }}
            });

            dialogObj.dialog("open");
        },
        directEditItemPrice: function(numIid, itemJson, dialogObj, priceObj) {
            var itemPriceStr = priceObj.find(".item-new-price-input").val();
            if (itemPriceStr == "") {
                alert("请先输入新的一口价！");
                return;
            }
            if (isNaN(itemPriceStr)) {
                alert(itemPriceStr + "不是正确的数字格式！");
                return;
            }

            var skuObjs = priceObj.find(".sku-new-price-input");
            var skuJsonArray = [];

            for (var i = 0; i < skuObjs.length; i++) {
                var skuObj = $(skuObjs.get(i));
                var properties = skuObj.attr("properties");
                var propertyNames = skuObj.attr("propertyNames");
                var quantity = skuObj.attr("quantity");
                var originSkuPrice = skuObj.attr("originSkuPrice");
                var skuParameter = skuObj.val();
                if (skuParameter == "") {
                    alert("请先输入新的sku价格！");
                    return;
                }
                if (isNaN(skuParameter)) {
                    alert(skuParameter + "不是正确的数字格式！");
                    return;
                }
                var json = '{"properties":"' + properties + '","propertyNames":"' + propertyNames
                    + '","skuParameter":"' + skuParameter + '","quantity":"' + quantity
                    + '","originSkuPrice":"' + originSkuPrice + '"}';
                skuJsonArray[skuJsonArray.length] = json;

            }


            if (confirm("确定要修改宝贝(" + itemJson.title + ")的价格？") == false) {
                return;
            }

            var paramData = {};
            paramData.numIid = numIid;
            paramData.itemPriceStr = itemPriceStr;
            paramData.skuPriceJson = '[' + skuJsonArray.join(",") + ']';

            $.ajax({
                url : '/SkuPriceEdit/directModifyItemPrice',
                data : paramData,
                type : 'post',
                success : function(dataJson) {
                    SkinBatch.skuPrice.ajaxCallback(dialogObj, dataJson, false);
                }
            });

        },
        scaleOneItemPrice: function(numIid, itemJson, dialogObj, priceObj) {
            var itemScaleStr = priceObj.find(".scale-new-price-input").val();
            if (itemScaleStr == "") {
                alert("请先输入新的一口价的百分比！");
                return;
            }
            if (isNaN(itemScaleStr)) {
                alert(itemScaleStr + "不是正确的数字格式！");
                return;
            }

            var skuObjs = priceObj.find(".sku-scale-input");
            var skuJsonArray = [];

            for (var i = 0; i < skuObjs.length; i++) {
                var skuObj = $(skuObjs.get(i));
                var properties = skuObj.attr("properties");
                var propertyNames = skuObj.attr("propertyNames");
                var quantity = skuObj.attr("quantity");
                var originSkuPrice = skuObj.attr("originSkuPrice");
                var skuParameter = skuObj.val();
                if (skuParameter == "") {
                    alert("请先输入sku价格的百分比！");
                    return;
                }
                if (isNaN(skuParameter)) {
                    alert(skuParameter + "不是正确的数字格式！");
                    return;
                }
                var json = '{"properties":"' + properties + '","propertyNames":"' + propertyNames
                    + '","skuParameter":"' + skuParameter + '","quantity":"' + quantity
                    + '","originSkuPrice":"' + originSkuPrice + '"}';
                skuJsonArray[skuJsonArray.length] = json;

            }
            var confirmMsg = "确定要通过价格百分比的方式修改宝贝(" + itemJson.title + ")的价格？";
            if (confirm(confirmMsg) == false) {
                return;
            }

            var paramData = {};
            paramData.numIid = numIid;
            paramData.itemScaleStr = itemScaleStr;
            paramData.skuScaleJson = '[' + skuJsonArray.join(",") + ']';

            $.ajax({
                url : '/SkuPriceEdit/scaleOneItemPrice',
                data : paramData,
                type : 'post',
                success : function(dataJson) {
                    SkinBatch.skuPrice.ajaxCallback(dialogObj, dataJson, false);
                }
            });

        },
        scaleSomeItemsPrice: function(numIidList, dialogObj) {
            var priceScaleStr = dialogObj.find(".item-price-scale-input").val();
            if (priceScaleStr == "") {
                alert("请先输入价格的百分比！");
                return;
            }
            if (isNaN(priceScaleStr)) {
                alert(priceScaleStr + "不是正确的数字格式！");
                return;
            }

            if (confirm("确定要修改" + numIidList.length + "个宝贝的价格？") == false) {
                return;
            }

            var paramData = {};
            paramData.numIidList = numIidList;
            paramData.priceScaleStr = priceScaleStr;

            $.ajax({
                url : '/SkuPriceEdit/scaleSomeItemsPrice',
                data : paramData,
                type : 'post',
                success : function(dataJson) {
                    SkinBatch.skuPrice.ajaxCallback(dialogObj, dataJson, true);
                }
            });

        },
        ajaxCallback: function(dialogObj, dataJson, showDialog) {
            if (SkinBatch.util.checkAjaxSuccess(dataJson) == false) {
                SkinBatch.submit.checkAuth(dataJson);
                return;
            }
            SkinBatch.error.showErrors(dataJson.res, showDialog);

            if (showDialog == true || dataJson.res.length <= 0) {

                dialogObj.dialog('close');

            }
            SkinBatch.show.refresh();
        },
        initItemSkuPrices: function(numIid, dialogObj) {

            var paramData = {numIid: numIid};

            //获取宝贝的sku价格
            $.ajax({
                url : '/SkuPriceEdit/getItemSkuPrices',
                data : paramData,
                type : 'post',
                success : function(dataJson) {
                    if (SkinBatch.util.checkAjaxSuccess(dataJson) == false) {
                        return;
                    }
                    var skuArray = dataJson.res;
                    if (skuArray === undefined || skuArray == null) {
                        skuArray = [];
                    }

                    var allPriceHtml = '';
                    var allScaleHtml = '';

                    $(skuArray).each(function(index, skuJson) {
                        var priceTrHtml = '' +
                            '<tr>' +
                            '   <td style="text-align: right;">' + skuJson.propertyNames + '：</td>' +
                            '   <td style="padding-left: 10px;text-align: left;">' +
                            '       新的价格为&nbsp;<input type="text" class="sku-new-price-input" value="' + skuJson.price + '" quantity="' + skuJson.quantity + '" properties="' + skuJson.properties + '" propertyNames="' + skuJson.propertyNames + '" originSkuPrice="' + skuJson.price + '" style="width: 70px;" />&nbsp;元' +
                            '       (<span style="color: #f60;">' + skuJson.quantity + '</span> 库存)' +
                            '   </td>' +
                            '</tr>' +
                            '';
                        allPriceHtml += priceTrHtml;

                        var scaleTrHtml = '' +
                            '<tr>' +
                            '   <td style="text-align: right;">' + skuJson.propertyNames + '：</td>' +
                            '   <td style="padding-left: 10px;text-align: left;">' +
                            '       新的价格为<span class="red">原价</span>的&nbsp;<input type="text" class="sku-scale-input" quantity="' + skuJson.quantity + '" properties="' + skuJson.properties + '" propertyNames="' + skuJson.propertyNames + '" originSkuPrice="' + skuJson.price + '" value="100" style="width: 70px;"/>&nbsp;% ' +
                            '       (<span style="color: #f60;">' + skuJson.quantity + '</span> 库存)' +
                            '   </td>' +
                            '</tr>' +
                            '';
                        allScaleHtml += scaleTrHtml;

                    });

                    dialogObj.find(".direct-price-table tbody").append(allPriceHtml);
                    dialogObj.find(".scale-price-table tbody").append(allScaleHtml);
                }
            });

        }
    }, SkinBatch.skuPrice);


    /**
     * 操作失败的日志
     * @type {*}
     */
    SkinBatch.error = SkinBatch.error || {};
    SkinBatch.error = $.extend({
        showErrors: function(errorJsonArray, showDialog) {
            if (errorJsonArray === undefined || errorJsonArray == null || errorJsonArray.length == 0)
                return;
            SkinBatch.container.find(".error-item-div").show();
            $(errorJsonArray).each(function(index, errorJson) {
                var trObj = SkinBatch.error.createRow(index, errorJson);
                SkinBatch.container.find(".error-item-table").find("tbody.error-item-tbody").append(trObj);
            });


            if (showDialog == true) {
                SkinBatch.error.createErrorDialog(errorJsonArray);
            }



        },
        createErrorDialog: function(errorJsonArray) {

            $(".error-modify-item-dialog").remove();

            var dialogHtml = '' +
                '<div class="error-modify-item-dialog" style="margin-top: 10px;">' +
                '   <div style="text-align: left"><span class="error-tip-span">宝贝操作失败列表：</span></div>  ' +
                '   <table class="diag-error-item-table list-table">' +
                '       <thead>' +
                '       <tr>' +
                '           <td style="width: 15%;">宝贝图片</td>' +
                '           <td style="width: 45%;">标题</td>' +
                '           <td style="width: 20%;">失败说明</td>' +
                '           <td style="width: 15%;">操作时间</td>' +
                '       </tr>' +
                '       </thead>' +
                '       <tbody class="error-item-tbody"></tbody>' +
                '   </table> ' +
                '</div>' +
                '';



            var dialogObj = $(dialogHtml);


            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:500,
                width:800,
                title:'宝贝错误列表',
                autoOpen: false,
                resizable: false,
                buttons:{'确定':function() {

                    $(this).dialog('close');
                },'取消':function(){
                    $(this).dialog('close');
                }}
            });

            dialogObj.dialog("open");

            $(errorJsonArray).each(function(index, errorJson) {
                var trObj = SkinBatch.error.createRow(index, errorJson);
                dialogObj.find(".diag-error-item-table").find("tbody.error-item-tbody").append(trObj);
            });
        },
        createRow: function(index, errorJson) {
            var itemJson = errorJson.itemPlay;
            if (itemJson === undefined || itemJson == null) {
                itemJson = {};
            }
            var errorMsg = errorJson.message;
            var html = '' +
                '<tr>' +
                '   <td><a class="item-link" target="_blank"><img class="item-img" /> </a> </td>' +
                '   <td><a class="item-link" target="_blank"><span class="item-title"></span> </a> </td>' +
                '   <td><span class="error-intro"></span> </td>' +
                '   <td><span class="op-time"></span> </td>' +
                '</tr>' +
                '' +
                '' +
                '';
            var trObj = $(html);
            var url = "http://item.taobao.com/item.htm?id=" + itemJson.id;
            trObj.find(".item-link").attr("href", url);
            trObj.find(".item-img").attr("src", itemJson.picURL);
            trObj.find(".item-title").html(itemJson.title);
            trObj.find(".error-intro").html(errorMsg);

            var theDate = new Date();
            var theDate = new Date();
            var year = theDate.getFullYear();
            var month = theDate.getMonth() + 1;//js从0开始取
            var date = theDate.getDate();
            var hour = theDate.getHours();
            var minutes = theDate.getMinutes();
            var second = theDate.getSeconds();

            if (month < 10) {
                month = "0" + month;
            }
            if (date < 10) {
                date = "0" + date;
            }
            if (hour < 10) {
                hour = "0" + hour;
            }
            if (minutes < 10) {
                minutes = "0" + minutes;
            }
            if (second < 10) {
                second = "0" + second;
            }
            var timeStr = year + "-" + month+"-"+date+" "+hour+":"+minutes+ ":" + second;
            trObj.find(".op-time").html(timeStr);

            return trObj;
        }
    }, SkinBatch.error);

    /**
     * 工具类
     * @type {*}
     */
    SkinBatch.util = SkinBatch.util || {};
    SkinBatch.util = $.extend({
        //判断ajax是否返回成功的状态
        checkAjaxSuccess: function(dataJson) {
            if (dataJson.message === undefined || dataJson.message == null || dataJson.message == "") {

            } else {
                alert(dataJson.message);
            }
            return dataJson.success;
        },
        checkIsW2AuthError: function(dataJson) {

            if (dataJson.success != false) {
                return false;
            }
            var authorizeUrl = dataJson.taobaoAuthorizeUrl;
            if (authorizeUrl === undefined || authorizeUrl == null || authorizeUrl == "") {
                return false;
            }

            //alert("亲，您的促销授权已过期，请点击确定后，重新授权！")

            //location.href = authorizeUrl;

            SkinBatch.util.showAuthDialog(authorizeUrl, function() {});

            return true;
        },
        showAuthDialog: function(authorizeUrl, callback) {

            if (callback === undefined || callback == null) {
                callback = function() {};
            }

            $(".promotion-errors-dialog").remove();

            var html = '' +
                '<div class="promotion-errors-dialog" style="text-align: center;">' +
                '   <div style="padding: 20px 10px; font-size: 16px;">亲，您的促销授权已过期，请点击下面按钮到新页面授权后再回来提交！</div>' +
                //'<a href="/TaoDiscount/reShouquan" target="_blank" class="orangeBtn">去淘宝授权</a>' +
                '   <a class="user-auth-link" href="' + authorizeUrl + '" target="_blank" style="color: blue; font-weight:bold; font-size: 16px;">去淘宝授权>></a>' +
                '</div> ' +
                '';


            var dialogObj = $(html);



            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:300,
                width:400,
                title:'重新授权',
                autoOpen: false,
                resizable: false,
                buttons:{

                }
            });

            dialogObj.find('.user-auth-link').click(function() {

                dialogObj.dialog('close');

                SkinBatch.util.showConfrimDialog('完成授权后，请您再重新点击提交一次！', callback, callback);

            });

            dialogObj.dialog("open");
        },
        showConfrimDialog: function(msg, okCallback, cancelCallback) {

            if (okCallback === undefined || okCallback == null) {
                okCallback = function() {};
            }

            if (cancelCallback === undefined || cancelCallback == null) {
                cancelCallback = function() {};
            }

            $(".promotion-confirm-dialog").remove();


            var html = '' +
                '<div class="promotion-confirm-dialog" style="text-align: center;">' +
                '   <div style="padding: 20px 10px; font-size: 16px;">' + msg + '</div>' +
                '</div> ' +
                '';


            var dialogObj = $(html);



            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:200,
                width:300,
                title:'提示',
                autoOpen: false,
                resizable: false,
                buttons:{
                    '确定':function(){
                        okCallback();
                        $(this).dialog('close');
                    },
                    '取消':function(){
                        cancelCallback();
                        $(this).dialog('close');
                    }
                }
            });

            dialogObj.dialog("open");
        }
    }, SkinBatch.util);

})(jQuery,window));