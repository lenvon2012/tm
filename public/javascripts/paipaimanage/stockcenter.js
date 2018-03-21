
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
        doInit: function(container) {
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
                '<div class="batchop-div"></div>' +
                '<div class="paging-div"></div>' +
                '<div class="items-container"></div> ' +
                '<div class="paging-div"></div>' +
                '<div class="batchop-div"></div>' +
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
                '       <tbody></tbody>' +
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
                '           <td style="padding-left: 20px;"><span>分类：</span><select class="category-select"><option value="0" selected="selected">所有分类</option> </select> </td>' +
                '           <td style="padding-left: 20px;">' +
                '               <span>宝贝状态：</span>' +
                '               <select class="state-select">' +
                '                   <option value="IS_FOR_SALE" selected="selected">在架宝贝</option> ' +
                '                   <option value="IS_IN_STORE">待上架宝贝</option> ' +
                '                   <option value="ALL">所有宝贝</option> ' +
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
            $.get("/paipaidiscount/sellerCatCount",function(data){
                var sellerCat = $('.category-select');
                sellerCat.empty();
                if(!data ||data.res.length == 0){
                    sellerCat.hide();
                }

                var cat = $('<option catId = "0">所有分类</option>');
                sellerCat.append(cat);
                for(var i=0;i<data.res.length;i++) {
                    if(data.res[i].count <= 0){
                        continue;
                    }
                    var item_cat=data.res[i];
                    var option = $('<option></option>');
                    option.attr("catId",item_cat.cid);
                    option.html(item_cat.name);
                    sellerCat.append(option);
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
                '   <table>' +
                '       <tbody>' +
                '           <tr>' +
//                '               <td><span class="tmbtn sky-blue-btn batch-list-btn">批量上架</span> </td>' +
//                '               <td><span class="tmbtn short-green-btn batch-delist-btn">批量下架</span> </td>' +
                '               <td><span class="tmbtn yellow-btn batch-modify-stock-btn">批量修改库存</span> </td>' +
                '           </tr>' +
                '       </tbody>' +
                '   </table>' +
                '';
            SkinBatch.container.find(".batchop-div").html(html);

            var getCheckedItems = function() {
                var checkObjs = SkinBatch.container.find(".item-checkbox:checked");
                var skuIdArray = [];
                checkObjs.each(function() {
                    skuIdArray[skuIdArray.length] = $(this).attr("skuId");
                });
                return skuIdArray;
            };
            //添加事件

            SkinBatch.container.find(".batch-modify-stock-btn").click(function() {
                var skuIdArray = getCheckedItems();
                if (skuIdArray.length == 0) {
                    alert("请先选择要修改库存的宝贝");
                    return;
                }
                var checkObjs = SkinBatch.container.find(".item-checkbox:checked");
                var itemJson = checkObjs[0].itemJson;
                SkinBatch.submit.modifyPriceParam.skuIdList = skuIdArray;
                SkinBatch.submit.modifyPriceParam.itemJson = itemJson;
                SkinBatch.submit.doModifyStock();
            });
        },
        //宝贝表格
        initItemContainer: function() {
            var html = '' +
                '<table class="item-table list-table busSearch">' +
                '   <thead>' +
                '   <tr>' +
                '       <td style="width: 5%;"><input type="checkbox" class="select-all-item width17" /> </td>' +
                '       <td style="width: 13%;">宝贝主图</td>' +
                '       <td style="width: 40%;">标题</td>' +
                '       <td style="width: 16%;">销售属性</td>' +
                //'       <td style="width: 7%;">库存</td>' +
                //'       <td style="width: 10%;">价格</td>' +
                '       <td style="width: 10%;">销量</td>' +
                '       <td style="width: 16%;">库存</td>' +
                '   </tr>' +
                '   </thead>' +
                '   <tbody></tbody>' +
                '</table>' +
                '';

            SkinBatch.container.find(".items-container").html(html);

            //设置事件
            SkinBatch.container.find(".select-all-item").click(function() {
                var isChecked = $(this).is(":checked");
                var checkObjs = SkinBatch.container.find(".item-checkbox");
                checkObjs.attr("checked", isChecked);
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
        ruleData: {
            orderProp: '',      //排序的属性
            orderType: SkinBatch.show.orderData.asc    //排序的类型，升序还是降序
        },
        doShow: function(currentPage) {
            var ruleData = SkinBatch.show.getQueryRule();
            var itemTbodyObj = SkinBatch.container.find(".item-table").find("tbody");
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
                    url: '/PaiPaiManage/queryStocks',
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
            SkinBatch.container.find(".error-item-div").hide();
        },
        getQueryRule: function() {
            var ruleData = {};
            var title = SkinBatch.container.find(".search-text").val();
            var state = SkinBatch.container.find(".state-select").val();
            var catId = SkinBatch.container.find(".category-select option:selected").attr("catid");

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
        createRow: function(index, QueryStock) {
            var html = '' +
                '<tr>' +
                '   <td><input type="checkbox" class="item-checkbox width17" /></td>' +
                '   <td><a class="item-link" target="_blank"><img class="item-img" /></a> </td>' +
                '   <td><a class="item-link" target="_blank"><span class="item-title" style="color: #333;"></span></a> </td>' +
                '   <td><span class="stock-attr" ></span></td>' +
                '   <td><span class="sale-count"></span></td>' +
                //'   <td><span class="inventory"></span> </td>' +
                '   <td>' +
                '       <div class="item-stock"></div> ' +
                '       <div class="tmbtn yellow-btn item-modify-stock-btn">修改库存</div>' +
                '   </td>' +
                '</tr>' +
                '' +
                '';
            var trObj = $(html);
            /*if (itemJson.id === undefined || itemJson.id == null || itemJson.id == 0) {

             } */
            var itemJson=QueryStock.stock;
            var itemName=QueryStock.itemName;
            var skuId = itemJson.skuId;

            var url = "http://auction1.paipai.com/" + itemJson.itemCode;
            trObj.find(".item-checkbox").attr("skuId", itemJson.skuId);
            trObj.find(".item-checkbox").each(function() {
                this.itemJson = itemJson;
            });
            trObj.find(".item-link").attr("href", url);
            trObj.find(".item-img").attr("src", itemJson.picLink);
            trObj.find(".item-title").html(itemName);
            trObj.find(".stock-attr").html(itemJson.stockAttr);
            trObj.find(".sale-count").html(itemJson.soldNum);
//            trObj.find(".inventory").html(itemJson.quantity);
            trObj.find(".item-stock").html( itemJson.num);

            trObj.find(".item-modify-stock-btn").click(function() {
                var itemCheckObj = $(this).parents("tr").find(".item-checkbox");
                var skuId = itemCheckObj.attr("skuId");
                var skuIdArray = [];
                skuIdArray[skuIdArray.length] = skuId;
                var itemJson = null;
                itemCheckObj.each(function() {
                    itemJson = this.itemJson;
                });

                SkinBatch.submit.modifyPriceParam.skuIdList = skuIdArray;
                SkinBatch.submit.modifyPriceParam.itemJson = itemJson;
                SkinBatch.submit.doModifyStock();
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

            return trObj;

        }
    }, SkinBatch.row);

    /**
     * 提交批量操作的功能
     * @type {*}
     */
    SkinBatch.submit = SkinBatch.submit || {};
    SkinBatch.submit = $.extend({

        modifyPriceParam: {
            skuIdList: [],
            itemJson: {}
        },
        //修改价格
        doModifyStock: function() {
            var dialogObj = $(".modify-stock-dialog");

            if (dialogObj.length == 0) {
                var html = '' +
                    '<div class="modify-stock-dialog">' +
                    '   <div class="direct-modify-price-div modify-div">' +
                    '       <span>请输入新库存数量：</span>' +
                    '       <input type="text" class="new-stock-text" /> ' +
                    '       <span>元</span>' +
                    '   </div> ' +
                    '</div>';

                dialogObj = $(html);
                var ajaxSuccessCallback = function(dataJson) {
//                    if (SkinBatch.util.checkAjaxSuccess(dataJson) == false) {
//                        return;
//                    }
                    dialogObj.dialog('close');
                    SkinBatch.error.showErrors(dataJson.res);
                };

                var directModifyStock = function(skuIdArray, itemJson) {
                    var newStock = dialogObj.find(".new-stock-text").val();
                    if (newStock === undefined || newStock == null || newStock == "") {
                        alert("请先填写要修改的库存数量");
                        return;
                    }
                    if(isNaN(newStock)) {
                        alert("请先填写正确的数字格式，库存必须为正整数");
                        return;
                    }
                    var confirmStr =  "确定要修改" + skuIdArray.length + "个宝贝的库存数量？";
                    if (confirm(confirmStr) == false) {
                        return;
                    }
                    var data = {};
                    data.skuIdList = skuIdArray;
                    data.newStock = newStock;

                    $.ajax({
                        url : '/PaiPaiManage/doModifyStock',
                        data : data,
                        type : 'post',
                        //async: false,//不是异步的话，window.open就不会被浏览器拦截了
                        success : function(dataJson) {
                            ajaxSuccessCallback(dataJson);
                        }
                    });
                };

                $("body").append(dialogObj);
                dialogObj.dialog({
                    modal: true,
                    bgiframe: true,
                    height:270,
                    width:320,
                    title:'修改宝贝库存',
                    autoOpen: false,
                    resizable: false,
                    buttons:{'确定':function() {
                        var skuIdArray = SkinBatch.submit.modifyPriceParam.skuIdList;
                        var itemJson = SkinBatch.submit.modifyPriceParam.itemJson;
                        directModifyStock(skuIdArray, itemJson);
                    },'取消':function(){
                        $(this).dialog('close');
                    }}
                });
            }
            dialogObj.find(".new-stock-text").val(SkinBatch.submit.modifyPriceParam.itemJson.num);
            dialogObj.dialog("open");

        }
    }, SkinBatch.submit);

    /**
     * 操作失败的日志
     * @type {*}
     */
    SkinBatch.error = SkinBatch.error || {};
    SkinBatch.error = $.extend({
        showErrors: function(errorJsonArray) {
            if (errorJsonArray === undefined || errorJsonArray == null || errorJsonArray.length == 0){
                alert("操作成功！");
                SkinBatch.show.refresh();
                return ;
            }
            alert("操作有误，请查看最下方的错误列表！");
            SkinBatch.container.find(".error-item-div").show();
            $(errorJsonArray).each(function(index, errorJson) {
                var trObj = SkinBatch.error.createRow(index, errorJson);
                SkinBatch.container.find(".error-item-table").find("tbody").append(trObj);
            });
        },
        createRow: function(index, errorJson) {
            var itemJson = errorJson.item;
            var errorResult = errorJson.errorMessage;
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
            var url = "http://auction2.paipai.com/" + itemJson.itemCode;
            trObj.find(".item-link").attr("href", url);
            trObj.find(".item-img").attr("src", itemJson.picLink);
            trObj.find(".item-title").html(itemJson.itemName);
            trObj.find(".error-intro").html(errorResult);

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
        }
    }, SkinBatch.util);

})(jQuery,window));
