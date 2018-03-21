
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

                '           <td style="padding-left: 20px;"><span></span><select class="category-select"><option value="0" selected="selected">所有分类</option> </select> </td>' +
                '           <td style="padding-left: 20px;">' +
                '               <span></span>' +
                '               <select class="state-select">' +
                '                   <option value="OnSale" selected="selected">在售宝贝</option> ' +
                '                   <option value="InStock">仓库中宝贝</option> ' +
                '                   <option value="All">所有宝贝</option> ' +
                '               </select> ' +
                '           </td>' +
                '           <td style="padding-left: 10px;"><span>宝贝标题：</span><input type="text" class="search-text" /> </td>' +
                '           <td style="padding-left: 20px;">' +
                '               <span class="tmbtn blue-btn search-btn">搜索宝贝</span> ' +
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
                '   <table>' +
                '       <tbody>' +
                '           <tr>' +
                '               <td><span class="tmbtn sky-blue-btn batch-list-btn">选中上架</span> </td>' +
                '               <td><span class="tmbtn short-green-btn batch-delist-btn">选中下架</span> </td>' +
                '               <td><span class="tmbtn yellow-btn batch-modify-price-btn">选中改价</span> </td>' +
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
            SkinBatch.container.find(".batch-modify-price-btn").click(function() {
                var numIidArray = getCheckedItems();
                if (numIidArray.length == 0) {
                    alert("请先选择要修改价格的宝贝");
                    return;
                }
                var checkObjs = SkinBatch.container.find(".item-checkbox:checked");
                var itemJson = checkObjs[0].itemJson;
                SkinBatch.submit.modifyPriceParam.numIidList = numIidArray;
                SkinBatch.submit.modifyPriceParam.itemJson = itemJson;
                SkinBatch.submit.doModifyPrice();
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
                '       <td style="width: 42%;">标题</td>' +
                '       <td style="width: 7%;">销量</td>' +
                //'       <td style="width: 7%;">库存</td>' +
                //'       <td style="width: 10%;">价格</td>' +
                '       <td style="width: 15%;">价格</td>' +
                '       <td style="width: 15%;">状态</td>' +
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
                '<tr>' +
                '   <td><input type="checkbox" class="item-checkbox width17" /></td>' +
                '   <td><a class="item-link" target="_blank"><img class="item-img" /></a> </td>' +
                '   <td><a class="item-link" target="_blank"><span class="item-title" style="color: #333;"></span></a> </td>' +
                '   <td><span class="sale-count"></span> </td>' +
                //'   <td><span class="inventory"></span> </td>' +
                '   <td>' +
                '       <div class="item-price"></div> ' +
                '       <div class="tmbtn yellow-btn item-modify-price-btn">修改价格</div>' +
                '   </td>' +
                '   <td>' +
                '       <div class="item-state"></div> ' +
                '       <div class="tmbtn sky-blue-btn item-delist-btn"></div>' +
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
                state = "待上架";
            } else if (itemJson.status == 1) {
                state = "在架上";
            }
            trObj.find(".item-state").html(state);
            var btnObj = trObj.find(".item-delist-btn");
            if (itemJson.status == 0) {
                btnObj.html("立即上架");
                btnObj.click(function() {
                    var itemCheckObj = $(this).parents("tr").find(".item-checkbox");
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
                    var itemCheckObj = $(this).parents("tr").find(".item-checkbox");
                    var numIid = itemCheckObj.attr("numIid");
                    var numIidArray = [];
                    numIidArray[numIidArray.length] = numIid;
                    if (confirm("确定下架宝贝：" + itemJson.title + "？") == false)
                        return;

                    SkinBatch.submit.doDeListing(numIidArray);
                });
            }
            trObj.find(".item-modify-price-btn").click(function() {
                var itemCheckObj = $(this).parents("tr").find(".item-checkbox");
                var numIid = itemCheckObj.attr("numIid");
                var numIidArray = [];
                numIidArray[numIidArray.length] = numIid;
                var itemJson = null;
                itemCheckObj.each(function() {
                    itemJson = this.itemJson;
                });

                SkinBatch.submit.modifyPriceParam.numIidList = numIidArray;
                SkinBatch.submit.modifyPriceParam.itemJson = itemJson;
                SkinBatch.submit.doModifyPrice();
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
                var checkObj = $(this).parents("tr").find(".item-checkbox");
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
        //批量上架
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
                    SkinBatch.error.showErrors(dataJson.res);
                    SkinBatch.show.refresh();
                }
            });
        },
        //批量下架
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
                    SkinBatch.error.showErrors(dataJson.res);
                    SkinBatch.show.refresh();
                }
            });
        },
        modifyPriceParam: {
            numIidList: [],
            itemJson: {}
        },
        //修改价格
        doModifyPrice: function() {

            $.ajax({
                url : '/skinbatch/checkAuth',
                data : {},
                type : 'post',
                success : function(dataJson) {
                    if (SkinBatch.util.checkAjaxSuccess(dataJson) == false) {
                        SkinBatch.submit.checkAuth(dataJson);
                        return;
                    }

                    var dialogObj = $(".modify-price-dialog");

                    if (dialogObj.length == 0) {
                        var html = '' +
                            '<div class="modify-price-dialog">' +
                            '   <table style="margin: 0 auto;">' +
                            '       <tbody>' +
                            '      <tr>' +
                            '           <td>' +
                            '               <input type="radio" name="modify-price-type-radio" class="modify-price-type-radio" checked="checked" targetDiv="direct-modify-price-div"/><span class="modify-price-type-span">直接修改价格</span> ' +
                            '           </td>' +
                            '           <td style="padding-left: 10px;">' +
                            '               <input type="radio" name="modify-price-type-radio" class="modify-price-type-radio" targetDiv="modify-price-byscale-div"/><span class="modify-price-type-span">宝贝比例加价</span> ' +
                            '           </td>' +
                            '       </tr>' +
                            '       </tbody>' +
                            '   </table>' +
                            '   <div class="direct-modify-price-div modify-div">' +
                            '       <span>请输入新价格：</span>' +
                            '       <input type="text" class="new-price-text" /> ' +
                            '       <span>元</span>' +
                            '   </div> ' +
                            '   <div class="modify-price-byscale-div modify-div" style="display: none;">' +
                            '       <span>请输入加价比例：</span>' +
                            '       <input type="text" class="new-price-byscale-text" value="0" style="width: 100px;"/><br /> ' +
                            '       <span style="color: #a10000;">(如输入0.1，则价格上涨10%；如输入-0.1，则价格下降10%。)</span>' +
                            '   </div> ' +
                            '</div>';

                        dialogObj = $(html);
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

                        var ajaxSuccessCallback = function(dataJson) {
                            if (SkinBatch.util.checkAjaxSuccess(dataJson) == false) {
                                SkinBatch.submit.checkAuth(dataJson);
                                return;
                            }
                            dialogObj.dialog('close');
                            SkinBatch.error.showErrors(dataJson.res);
                            SkinBatch.show.refresh();
                        };

                        var directModifyPrice = function(numIidArray, itemJson) {
                            var newPrice = dialogObj.find(".new-price-text").val();
                            if (newPrice === undefined || newPrice == null || newPrice == "") {
                                alert("请先填写要修改的价格");
                                return;
                            }
                            var confirmStr = "";
                            if (numIidArray.length == 1) {
                                confirmStr = "确定修改宝贝：" + itemJson.title + "的价格？";
                            } else {
                                confirmStr = "确定要修改" + numIidArray.length + "个宝贝的价格？";
                            }
                            if (confirm(confirmStr) == false) {
                                return;
                            }
                            var data = {};
                            data.numIidList = numIidArray;
                            data.newPriceStr = newPrice;

                            $.ajax({
                                url : '/skinbatch/doModifyPrice',
                                data : data,
                                type : 'post',
                                async: false,//不是异步的话，window.open就不会被浏览器拦截了
                                success : function(dataJson) {
                                    ajaxSuccessCallback(dataJson);
                                }
                            });
                        };

                        var modifyPriceByScale = function(numIidArray, itemJson) {
                            var priceScaleStr = dialogObj.find(".new-price-byscale-text").val();
                            if (priceScaleStr === undefined || priceScaleStr == null || priceScaleStr == "") {
                                alert("请先填写要加价的比例");
                                return;
                            }
                            var confirmStr = "";
                            if (numIidArray.length == 1) {
                                confirmStr = "确定修改宝贝：" + itemJson.title + "的价格？";
                            } else {
                                confirmStr = "确定要修改" + numIidArray.length + "个宝贝的价格？";
                            }
                            var priceScale = parseFloat(priceScaleStr);
                            if (priceScale == NaN) {
                                alert("请先输入正确的加价比例格式");
                                return;
                            }
                            if (priceScale < 0) {
                                confirmStr += "宝贝价格下降" + priceScale * 100 + "%";
                            } else {
                                confirmStr += "宝贝价格上涨" + priceScale * 100 + "%";
                            }

                            if (confirm(confirmStr) == false) {
                                return;
                            }
                            var data = {};
                            data.numIidList = numIidArray;
                            data.priceScaleStr = priceScaleStr;

                            $.ajax({
                                url : '/skinbatch/doModifyPriceByScale',
                                data : data,
                                type : 'post',
                                async: false,//不是异步的话，window.open就不会被浏览器拦截了
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
                            title:'修改宝贝价格',
                            autoOpen: false,
                            resizable: false,
                            buttons:{'确定':function() {
                                var divCss = dialogObj.find(".modify-price-type-radio:checked").attr("targetDiv");
                                var numIidArray = SkinBatch.submit.modifyPriceParam.numIidList;
                                var itemJson = SkinBatch.submit.modifyPriceParam.itemJson;

                                if (divCss == "direct-modify-price-div") {
                                    directModifyPrice(numIidArray, itemJson);
                                } else if (divCss == "modify-price-byscale-div") {
                                    modifyPriceByScale(numIidArray, itemJson);
                                } else {
                                    alert("系统出现了一些意外，请联系我们！");
                                }

                            },'取消':function(){
                                $(this).dialog('close');
                            }}
                        });
                    }
                    dialogObj.find(".new-price-text").val(SkinBatch.submit.modifyPriceParam.itemJson.price);
                    dialogObj.dialog("open");
                }
            });


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

    /**
     * 操作失败的日志
     * @type {*}
     */
    SkinBatch.error = SkinBatch.error || {};
    SkinBatch.error = $.extend({
        showErrors: function(errorJsonArray) {
            if (errorJsonArray === undefined || errorJsonArray == null || errorJsonArray.length == 0)
                return;
            SkinBatch.container.find(".error-item-div").show();
            $(errorJsonArray).each(function(index, errorJson) {
                var trObj = SkinBatch.error.createRow(index, errorJson);
                SkinBatch.container.find(".error-item-table").find("tbody").append(trObj);
            });
        },
        createRow: function(index, errorJson) {
            var itemJson = errorJson.itemPlay;
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