
var TM = TM || {};

((function ($, window) {

    TM.PromotionCommon = TM.PromotionCommon || {};

    var PromotionCommon = TM.PromotionCommon || {};

    PromotionCommon.init = PromotionCommon.init || {};
    PromotionCommon.init = $.extend({
        doInit: function(isAddItem, isRestartActivity) {
            PromotionCommon.isAddItem = isAddItem;
            if (isRestartActivity === undefined || isRestartActivity == null) {
                isRestartActivity = false;
            }
            PromotionCommon.isRestartActivity = isRestartActivity;
        },
        getIsAddItem: function() {
            return PromotionCommon.isAddItem;
        },
        getIsRestartActivity: function() {
            return PromotionCommon.isRestartActivity;
        }
    }, PromotionCommon.init);


    PromotionCommon.event = PromotionCommon.event || {};
    PromotionCommon.event = $.extend({

        initSearchEvent: function(container, searchCallback) {

            container.find('.guanjianci').unbind();
            container.find('.fRange').unbind();
            container.find('.fRl-ico-pu').unbind();
            container.find('.fRl-ico-pd').unbind();
            container.find('.fRl-ico-su').unbind();
            container.find('.fRl-ico-sd').unbind();
            container.find('.fRl-ico-df').unbind();
            container.find('.fSellercat').unbind();
            container.find('.fItemcat').unbind();
            container.find('.fDiscount').unbind();
            container.find('.fD-ico-dis').unbind();
            container.find('.fD-ico-undis').unbind();
            container.find('.fD-ico-all').unbind();
            container.find('.guanjianci-select').unbind();
            container.find('.item-status-select').unbind();


            container.find('.guanjianci').keyup(function(event) {
                var lable_key = container.find('.guanjianci').val();
                if (!lable_key) {
                    container.find('.combobox-label-item').show();
                } else {
                    container.find('.combobox-label-item').hide();
                }
            });

            container.find('.guanjianci').keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    searchCallback();
                }
            });
            container.find('.guanjianci-select').click(function(){
                searchCallback();
            });




            container.find('.fRange').mousemove(function(){
                container.find('.fR-list').show();
            });
            container.find('.fRange').mouseout(function(){
                container.find('.fR-list').hide();
            });
            container.find('.fRl-ico-pu').click(function(){
                container.find('.fR-text').html("↑ 价格从低到高");
                container.find('.fR-text').attr("order","pu");
                searchCallback();
            });
            container.find('.fRl-ico-pd').click(function(){
                container.find('.fR-text').html("↓ 价格从高到低");
                container.find('.fR-text').attr("order","pd");
                searchCallback();
            });
            container.find('.fRl-ico-su').click(function(){
                container.find('.fR-text').html("↑ 下架时间");
                container.find('.fR-text').attr("order","su");
                searchCallback();
            });
            container.find('.fRl-ico-sd').click(function(){
                container.find('.fR-text').html("↓ 下架时间");
                container.find('.fR-text').attr("order","sd");
                searchCallback();
            });
            container.find('.fRl-ico-df').click(function(){
                container.find('.fR-text').html("默认排序");
                container.find('.fR-text').attr("order","df");
                searchCallback();
            });

            container.find('.fSellercat').mousemove(function(){
                container.find('.fS-list').show();
            });
            container.find('.fSellercat').mouseout(function(){
                container.find('.fS-list').hide();
            });

            container.find('.fItemcat').mousemove(function(){
                container.find('.fI-list').show();
            });
            container.find('.fItemcat').mouseout(function(){
                container.find('.fI-list').hide();
            });

            container.find('.fDiscount').mousemove(function(){
                container.find('.fD-list').show();
            });
            container.find('.fDiscount').mouseout(function(){
                container.find('.fD-list').hide();
            });
            container.find('.fD-ico-dis').click(function(){
                container.find('.fD-text').html("已参加活动");
                container.find('.fD-text').attr("isDis","dis");
                searchCallback();
            });
            container.find('.fD-ico-undis').click(function(){
                container.find('.fD-text').html("未参加活动");
                container.find('.fD-text').attr("isDis","undis");
                searchCallback();
            });
            container.find('.fD-ico-all').click(function(){
                container.find('.fD-text').html("所有宝贝");
                container.find('.fD-text').attr("isDis","all");
                searchCallback();
            });


            container.find('.item-status-select').mousemove(function(){
                container.find('.item-status-list').show();
            });
            container.find('.item-status-select').mouseout(function(){
                container.find('.item-status-list').hide();
            });
            container.find('.item-status-option').click(function(){
                container.find('.item-status-text').html($(this).html());
                container.find('.item-status-text').attr("itemStatus", $(this).attr("itemStatus"));
                searchCallback();
            });




            $.get("/items/sellerCatCount",function(data){
                var sellerCat = container.find('#sellerCat');
                sellerCat.empty();
                if(!data || data.length == 0){
                    sellerCat.hide();
                }
                var exist = false;
                var cat = $('<li class="all-seller-cat-option"><a href="javascript:void(0);">所有类目</a></li>');
                cat.click(function(){
                    container.find('.fS-text').html('<a href="javascript:void(0);">所有类目</a>');
                    searchCallback();
                });
                sellerCat.append(cat);
                for(var i=0;i<data.length;i++) {
                    if(data[i].count <= 0){
                        continue;
                    }
                    exist = true;
                    var li_option = $('<li></li>');
                    var option = $('<a href="javascript:void(0);"></a>');
                    option.attr("catId",data[i].id);
                    //option.html(data[i].name+"("+data[i].count+")");

                    if (PromotionCommon.init.getIsAddItem() == true) {
                        option.html(data[i].name+"("+data[i].count+")");
                    } else {
                        option.html(data[i].name);
                    }

                    option.click(function(){
                        container.find('.fS-text').html($(this).parent().html());
                        searchCallback();
                    });
                    li_option.append(option);
                    sellerCat.append(li_option);

                }
            });
            $.get("/items/itemCatCount",function(data){
                var sellerCat = $('#itemCat');
                sellerCat.empty();
                if(!data || data.length == 0){
                    sellerCat.hide();
                }

                var exist = false;
                var cat = $('<li class="all-item-cat-option"><a href="javascript:void(0);">所有类目</a></li>');
                cat.click(function(){
                    container.find('.fI-text').html('<a href="javascript:void(0);">所有类目</a>');
                    searchCallback();
                });
                sellerCat.append(cat);
                for(var i=0;i<data.length;i++) {
                    if(data[i].count <= 0){
                        continue;
                    }

                    exist = true;
                    var li_option = $('<li></li>');
                    var option = $('<a href="javascript:void(0);"></a>');
                    option.attr("catId",data[i].id);

                    if (PromotionCommon.init.getIsAddItem() == true) {
                        option.html(data[i].name+"("+data[i].count+")");
                    } else {
                        option.html(data[i].name);
                    }


                    option.click(function(){
                        container.find('.fI-text').html($(this).parent().html());
                        searchCallback();
                    });
                    li_option.append(option);
                    sellerCat.append(li_option);
                }
            });

        },

        initBatchDiscountEvent: function(container, isAddItem) {

            container.find('.doDazhe').mousemove(function(){
                container.find('.tip-content').html("点击变为打折按钮后，本页所有宝贝将使用打折模式！（对于多个价格的宝贝建议使用打折模式）");
                container.find('.tspy').show();
            });
            container.find('.doDazhe').mouseout(function(){
                container.find('.tspy').hide();
            });
            container.find('.doJianjia').mousemove(function(){
                container.find('.tip-content').html("点击变为减价按钮后，本页所有宝贝将使用减价模式！（适合抹去分角操作）");
                container.find('.tspy').show();
            });
            container.find('.doJianjia').mouseout(function(){
                container.find('.tspy').hide();
            });
            container.find(".dofen").mousemove(function(){
                container.find('.tip-content').html('抹去分的宝贝将保留一位小数，且促销模式将切换到<span style="color: #a10000;">减价模式</span>，但不会操作有<span style="color: #a10000;">多个价格</span>的宝贝！');
                container.find('.tspy').addClass("fen-tspy");
                container.find('.tspy').show();
            });
            container.find('.dofen').mouseout(function(){
                container.find('.tspy').hide();
                container.find('.tspy').removeClass("fen-tspy");
            });
            container.find(".dofenjiao").mousemove(function(){
                container.find('.tip-content').html('抹去分角的宝贝将不保留小数，且促销模式将切换到<span style="color: #a10000;">减价模式</span>，但不会操作有<span style="color: #a10000;">多个价格</span>的宝贝！');
                container.find('.tspy').addClass("fenjiao-tspy");
                container.find('.tspy').show();
            });
            container.find('.dofenjiao').mouseout(function(){
                container.find('.tspy').hide();
                container.find('.tspy').removeClass("fenjiao-tspy");
            });


            //批量修改，打折
            TM.UmpUtil.util.bindTextChangeEvent(container.find('.piliang'), function() {
                var piliang = container.find('.piliang').val();
                container.find(".item").each(function(index, itemTrEle){

                    var itemTrObj = $(itemTrEle);

                    if (PromotionCommon.util.isItemCanbeEdit(itemTrObj, isAddItem) == true) {
                        itemTrObj.find('.item-dis').val(piliang);
                        PromotionCommon.discountRes.calcuByDiscount(piliang, itemTrObj);
                    }
                });
            });

            TM.UmpUtil.util.bindTextChangeEvent(container.find('.piliang-jianjia'), function() {
                var piliang = container.find('.piliang-jianjia').val();
                container.find(".item").each(function(index, itemTrEle){

                    var itemTrObj = $(itemTrEle);

                    if (PromotionCommon.util.isItemCanbeEdit(itemTrObj, isAddItem) == true) {
                        itemTrObj.find('.item-jianjia').val(piliang);
                        PromotionCommon.discountRes.calcuByDecrease(piliang, itemTrObj);
                    }
                });
            });

            //抹零操作
            container.find('.dofen').click(function(){
                container.find(".item").each(function(index, itemTrEle){

                    var itemTrObj = $(itemTrEle);
                    var disPriceObj = itemTrObj.find('.item-disprice');

                    var disprice = disPriceObj.val();

                    if (PromotionCommon.util.isItemCanbeEdit(itemTrObj, isAddItem) == true
                            && PromotionCommon.util.isItemHasSkuPrice(itemTrObj) == false) {
                        //if (disprice) {
                        //disprice = PromotionCommon.discountRes.formatResultPrice(disprice, 1);

                        var originDisPrice = disprice;

                        disprice = PromotionCommon.discountRes.removePricePoint(disprice, 1);

                        if (disprice != 0 && disprice == originDisPrice) {
                            //return;
                        }

                        //设为减价模式
                        if (disprice != 0) {
                            PromotionCommon.discountType.setDecreaseType(itemTrObj);
                        }

                        disPriceObj.val(disprice);
                        PromotionCommon.discountRes.calDiscountByResultPrice(disprice, itemTrObj);
                        //}
                    }
                });
            });
            container.find('.dofenjiao').click(function(){
                container.find(".item").each(function(index, itemTrEle){

                    var itemTrObj = $(itemTrEle);
                    var disPriceObj = itemTrObj.find('.item-disprice');

                    var disprice = disPriceObj.val();


                    if (PromotionCommon.util.isItemCanbeEdit(itemTrObj, isAddItem) == true
                            && PromotionCommon.util.isItemHasSkuPrice(itemTrObj) == false) {
                        //if (disprice) {

                        var originDisPrice = disprice;

                        //disprice = PromotionCommon.discountRes.formatResultPrice(disprice, 0);
                        disprice = PromotionCommon.discountRes.removePricePoint(disprice, 0);

                        if (disprice != 0 && disprice == originDisPrice) {
                            //return;
                        }

                        //设为减价模式
                        if (disprice != 0) {
                            PromotionCommon.discountType.setDecreaseType(itemTrObj);
                        }
                        disPriceObj.val(disprice);

                        PromotionCommon.discountRes.calDiscountByResultPrice(disprice, itemTrObj);
                        //}
                    }
                });
            });


            //打折模式
            container.find('.doDazhe').hide();
            //container.find('.doJianjia').hide();
            container.find('.doDazhe').click(function(){
                if (confirm("确定本页所有宝贝全部变为打折模式？" ) == false) {
                    return;
                }
                container.find('.doDazhe').hide();
                container.find('.doJianjia').show();
                container.find(".item").each(function(index, itemTrEle){
                    var itemTrObj = $(itemTrEle);

                    if (PromotionCommon.util.isItemCanbeEdit(itemTrObj, isAddItem) == true) {

                        PromotionCommon.discountType.setDiscountType(itemTrObj);

                        PromotionCommon.discountRes.showResSkuPrice(itemTrObj);
                    }
                })
            });
            container.find('.doJianjia').click(function(){
                if (confirm("确定本页所有宝贝全部变为减价模式？") == false) {
                    return;
                }

                container.find('.doJianjia').hide();
                container.find('.doDazhe').show();
                container.find(".item").each(function(index, itemTrEle){
                    var itemTrObj = $(itemTrEle);

                    if (PromotionCommon.util.isItemCanbeEdit(itemTrObj, isAddItem) == true) {

                        PromotionCommon.discountType.setDecreaseType(itemTrObj);

                        PromotionCommon.discountRes.showResSkuPrice(itemTrObj);
                    }
                })
            });


        }

    }, PromotionCommon.event);


    PromotionCommon.result = PromotionCommon.result || {};
    PromotionCommon.result = $.extend({
        modifyPromotionArray: [],
        addPageModifyPromotions: function(container, isAddItem) {

            var isRestartActivity = PromotionCommon.init.getIsRestartActivity();

            container.find(".item").each(function(index, itemTrEle) {

                var itemTrObj = $(itemTrEle);


                var numIid = itemTrObj.attr("numIid");

                //如果是重启活动，那么就要记录下所有的数据
                if (isRestartActivity == true) {

                    var promotionJson = PromotionCommon.result.getNewPromotionJson(itemTrObj);

                    if (PromotionCommon.util.isItemCanbeEdit(itemTrObj, isAddItem) == true) {

                        promotionJson.canbeEdit = true;
                    } else {

                        promotionJson.canbeEdit = false;
                    }

                    promotionJson.numIid = numIid;
                    PromotionCommon.result.addToModifyArray(promotionJson);

                    //console.info(promotionJson);
                    //console.info(TM.PromotionCommon.result.getAllModifyPromotionArray());
                    //console.info(TM.UmpSelectItem.result.getSelectNumIidArray());
                } else {
                    if (PromotionCommon.util.isItemCanbeEdit(itemTrObj, isAddItem) == true) {

                        //这里即使是错误了，也要先加到ItemArray中

                        var newPromotionJson = PromotionCommon.result.getNewPromotionJson(itemTrObj);
                        newPromotionJson.numIid = numIid;

                        var originPromotionJson = PromotionCommon.result.getOriginPromotionJson(itemTrObj);

                        if (newPromotionJson.promotionType == originPromotionJson.promotionType
                            && newPromotionJson.discountRate == originPromotionJson.discountRate
                            && newPromotionJson.decreaseAmount == originPromotionJson.decreaseAmount) {

                            PromotionCommon.result.removeFromModifyArray(numIid);
                        } else {
                            PromotionCommon.result.addToModifyArray(newPromotionJson);
                        }


                    } else{

                        PromotionCommon.result.removeFromModifyArray(numIid);
                    }
                }


            })
        },
        getAllModifyPromotionArray: function() {
            return PromotionCommon.result.modifyPromotionArray;
        },
        getNeedSubmitPromotions: function(isAddItem) {

            if (isAddItem == false) {
                return PromotionCommon.result.modifyPromotionArray;
            }


            var submitPromotionArray = [];


            for (var i = 0; i < PromotionCommon.result.modifyPromotionArray.length; i++){

                var modifyPromotionJson = PromotionCommon.result.modifyPromotionArray[i];

                if (isAddItem == true) {
                    if (modifyPromotionJson.discountRate == 1000) {
                        continue;
                    }
                }

                submitPromotionArray[submitPromotionArray.length] = modifyPromotionJson;
            }

            return submitPromotionArray;
        },
        getNewPromotionJson: function(itemTrObj) {
            var promotionJson = {};

            promotionJson.promotionType = PromotionCommon.discountType.getPromotionType(itemTrObj);
            var discountRate = itemTrObj.find(".item-dis").val();
            promotionJson.discountRate = Math.round(PromotionCommon.discountRes.formatDiscountRate(discountRate * 100, 0));
            var discountAmount = itemTrObj.find(".item-jianjia").val();
            promotionJson.decreaseAmount = Math.round(PromotionCommon.discountRes.formatDecreaseAmount(discountAmount * 100, 0));

            return promotionJson;
        },
        getOriginPromotionJson: function(itemTrObj) {
            var originPromotionJson = {};

            originPromotionJson.promotionType = itemTrObj.attr("originPromotionType");
            originPromotionJson.discountRate = itemTrObj.attr("originDiscount");
            originPromotionJson.decreaseAmount = itemTrObj.attr("originDecrease");

            return originPromotionJson;
        },
        addToModifyArray: function(promotionJson) {
            PromotionCommon.result.removeFromModifyArray(promotionJson.numIid)

            PromotionCommon.result.modifyPromotionArray.push(promotionJson);

        },
        removeFromModifyArray : function(numIid){
            for (var i = 0; i < PromotionCommon.result.modifyPromotionArray.length; i++){
                if (PromotionCommon.result.modifyPromotionArray[i].numIid == numIid){
                    PromotionCommon.result.modifyPromotionArray.splice(i, 1);
                    return true;
                }
                //console.info(PromotionCommon.result.modifyPromotionArray[i].numIid + "-----" + numIid);
            }
            return false;
        },
        removeSomeItemsFromModifyArray: function(numIidArray) {

            for (var i = 0; i < numIidArray.length; i++) {
                PromotionCommon.result.removeFromModifyArray(numIidArray[i]);
            }

        },
        removeNotExistItems: function(existNumIidArray) {

            var needDeleteNumIidArray = [];

            for (var i = 0; i < PromotionCommon.result.modifyPromotionArray.length; i++){

                var numIid = PromotionCommon.result.modifyPromotionArray[i].numIid;

                var isExist = false;

                for (var j = 0; j < existNumIidArray.length; j++) {
                    if (numIid == existNumIidArray[j]){
                        isExist = true;
                        break;
                    }
                }

                if (isExist == false) {
                    needDeleteNumIidArray[needDeleteNumIidArray.length] = numIid;
                    continue;
                }

            }

            PromotionCommon.result.removeSomeItemsFromModifyArray(needDeleteNumIidArray);

        },
        recoverFromModifyItem: function(promotionJson) {

            var modifyPromotionArray = PromotionCommon.result.modifyPromotionArray;


            for (var i = 0; i < modifyPromotionArray.length; i++){
                if (modifyPromotionArray[i].numIid == promotionJson.numIid){

                    var targetJson = modifyPromotionArray[i];

                    promotionJson.promotionType = targetJson.promotionType;
                    promotionJson.discountRate = targetJson.discountRate;
                    promotionJson.decreaseAmount = targetJson.decreaseAmount;

                    return true;

                }
            }

            return false;


        },
        getErrorModifyNumIids: function(isAddItem) {

            var errorNumIidArray = [];

            for (var i = 0; i < PromotionCommon.result.modifyPromotionArray.length; i++) {

                var modifyPromotion = PromotionCommon.result.modifyPromotionArray[i];

                var discountRate = modifyPromotion.discountRate / 100;

                var checkJson = PromotionCommon.discountRes.checkDiscountResult(discountRate, modifyPromotion.promotionType, isAddItem);

                if (checkJson.isRight == false) {
                    errorNumIidArray[errorNumIidArray.length] = modifyPromotion.numIid;
                }
            }

            return errorNumIidArray;
        }
    }, PromotionCommon.result);


    PromotionCommon.params = PromotionCommon.params || {};
    PromotionCommon.params = $.extend({
        selectItems: [],
        //搜索的参数，在选宝贝和修改宝贝折扣的时候共用
        getSimpleSearchParams: function(container) {

            var paramData = {};
            paramData.tmActivityId = TM.UmpUtil.util.getTMActivityId(container);
            paramData.title = container.find('.guanjianci').val();
            paramData.cid = container.find(".fI-text a").attr("catid");
            paramData.sellerCid = container.find(".fS-text a").attr("catid");
            paramData.order = container.find(".fR-text").attr("order");
            paramData.isDis = container.find(".fD-text").attr("isDis");

            paramData.itemStatus = container.find(".item-status-text").attr("itemStatus");

            return paramData;
        },
        clearSearchRules: function(container) {
            container.find(".guanjianci").val("");
            var allItemCatOption = container.find("#itemCat .all-item-cat-option");
            container.find(".fI-text").html(allItemCatOption.html());

            var allSellerCatOption = container.find("#sellerCat .all-seller-cat-option");
            container.find(".fS-text").html(allItemCatOption.html());


        },
        getSubmitParams: function(container, errorSetCallback, isAddItem) {

            //加入本页选中的宝贝
            PromotionCommon.result.addPageModifyPromotions(container, isAddItem);


            var modifyPromotionArray = PromotionCommon.result.getNeedSubmitPromotions(isAddItem);

            if (modifyPromotionArray.length <= 0) {
                if (isAddItem == true) {
                    alert("亲，您尚未设置任何宝贝的折扣，没有需要提交的宝贝！！");
                } else {
                    alert("亲，您没有修改任何宝贝的折扣，没有需要提交的宝贝！！");
                }

                return null;
            }

            var errorNumIidArray = PromotionCommon.result.getErrorModifyNumIids(isAddItem);

            if (errorNumIidArray.length > 0) {
                alert("亲，您有" + errorNumIidArray.length + "个宝贝折扣设置错误，请修改后再提交！");
                //errorSetCallback();

                return null;
            }


            var jsonArray = [];

            $(modifyPromotionArray).each(function(index, promotionJson) {

                var json = '{';
                json += '"numIid":' + promotionJson.numIid + ',';
                json += '"promotionType":"' + promotionJson.promotionType + '",';
                json += '"decreaseAmount":' + promotionJson.decreaseAmount + ',';
                json += '"discountRate":' + promotionJson.discountRate;
                json += '}';

                jsonArray[jsonArray.length] = json;


            });


            var paramData = {};

            paramData.minDiscount = TM.UmpUtil.util.getMinDiscount(modifyPromotionArray);

            paramData.itemNum = jsonArray.length;

            paramData.paramsJson = '[' + jsonArray.join(",") + ']';

            paramData.tmActivityId = TM.UmpUtil.util.getTMActivityId(container);

            return paramData;


        }
    }, PromotionCommon.params);


    PromotionCommon.row = PromotionCommon.row || {};
    PromotionCommon.row = $.extend({

        createCommonRow: function(index, itemJson, isAddItem) {

            if (itemJson.hasPromotion == true) {
                itemJson.hasPromotionStr = "Promoted";
            } else {
                itemJson.hasPromotionStr = "UnPromoted";
            }
            if (itemJson.hasSkuPrices == true) {
                itemJson.hasSkuPricesStr = "hasSkuPrices";
            } else {
                itemJson.hasSkuPricesStr = "noSkuPrices";
            }

            var outId = itemJson.outId;
            if (outId === undefined || outId == null || outId == "" || outId <= 0) {
                itemJson.outId = '';
            }


            itemJson.itemLink = "http://item.taobao.com/item.htm?id=" + itemJson.numIid;

            var trHtml = $('#promotionRow').tmpl(itemJson);

            //之前可能有修改过折扣，恢复，要在生成html之后，不然就无法保留下原始的折扣数据
            PromotionCommon.result.recoverFromModifyItem(itemJson);


            var trObj = $(trHtml);

            PromotionCommon.discountType.initPromotionType(trObj, itemJson);

            PromotionCommon.discountRes.initDiscountResult(trObj, itemJson);


            if (itemJson.fenxiao == true) {
                trObj.find(".fenxiao-price-div").show();
            } else {

                if (itemJson.hasSkuPrices == true) {
                    trObj.find(".sku-price-div").show();
                } else {
                    var outId = itemJson.outId;
                    if (outId === undefined || outId == null || outId == "" || outId <= 0) {

                    } else {
                        trObj.find(".out-id-div").show();
                    }

                }

                //trObj.find(".fenxiao-price-div").show();
                //trObj.find(".fenxiao-price").html('￥999.99');
            }

            if (itemJson.hasSkuPrices == true) {
                trObj.find(".discount-price-span").hide();
                trObj.find(".sku-result-span").show();

                //if (TM.UmpUtil.util.isIEBrowser() == false) {
                trObj.find('.sku-result-span').hover(function() {

                    var skuPriceArray = itemJson.skuPriceList;
                    if (skuPriceArray === undefined || skuPriceArray == null || skuPriceArray.length <= 0) {
                        return;
                    }

                    $(".qtip-cream").remove();

                    var skuResPriceHtml = PromotionCommon.discountRes.createAllResSkuPriceHtml(skuPriceArray, trObj);

                    trObj.find('.sku-result-span').qtip({
                        content: {
                            text: skuResPriceHtml
                        },
                        position: {
                            //at: "top center",
                            //my: 'top left'
                            corner: {
                                tooltip: 'bottomMiddle',
                                target: 'topMiddle'
                            }
                        },
                        show: {
                            when: 'click',
                            ready:false
                        },
                        style: {
                            border: {
                                width: 3,
                                radius: 5
                            },
                            padding: 10,
                            textAlign: 'center',
                            tip: true,
                            name: 'cream'
                        }
                    });
                    trObj.find('.sku-result-span').click();
                }, function() {

                });
                //}


            }


            //PromotionCommon.util.initItemDiscountInfo(itemJson, trObj);

            var isItemCanbeEdit = PromotionCommon.util.isItemCanbeEdit(trObj, isAddItem);

            if (isItemCanbeEdit == false) {
                if (itemJson.isthisActivity == true){
                    trObj.addClass("thisActivity");
                    trObj.find(".activity-promotion-tip").html('已参加本次活动');

                } else {
                    trObj.addClass("disabled");
                    trObj.find(".activity-promotion-tip").html('已参加其他活动');
                }
                trObj.find("input").attr("readonly", "readonly");

                trObj.find(".delete-btn-div").remove();

            } else {

            }

            if (isItemCanbeEdit == false) {
                return trObj;
            }

            trObj.find(".Model-dazhe").click(function() {
                PromotionCommon.discountType.setDecreaseType(trObj);
                PromotionCommon.discountRes.showResSkuPrice(trObj);
            });

            trObj.find(".Model-jianjia").click(function() {
                PromotionCommon.discountType.setDiscountType(trObj);

                PromotionCommon.discountRes.showResSkuPrice(trObj);
            });


            TM.UmpUtil.util.bindTextChangeEvent(trObj.find(".item-dis"), function() {
                var dis = trObj.find(".item-dis").val();

                PromotionCommon.discountRes.calcuByDiscount(dis, trObj);
            });

            TM.UmpUtil.util.bindTextChangeEvent(trObj.find(".item-jianjia"), function() {
                var jianjia = trObj.find(".item-jianjia").val();

                PromotionCommon.discountRes.calcuByDecrease(jianjia, trObj);
            });

            TM.UmpUtil.util.bindTextChangeEvent(trObj.find(".item-disprice"), function() {
                var dis = trObj.find(".item-disprice").val();
                PromotionCommon.discountRes.calDiscountByResultPrice(dis, trObj);
            });




            return trObj;

        }

    }, PromotionCommon.row);


    //打折结果
    PromotionCommon.discountRes = PromotionCommon.discountRes || {};
    PromotionCommon.discountRes = $.extend({

        initDiscountResult: function(itemTrObj, itemJson) {
            /*if (itemJson.discountRate <= 0 && itemJson.decreaseAmount <= 0) {
                return;
            }*/

            if (PromotionCommon.discountType.isDiscountTypeByItemJson(itemJson)) {

                var discountRate = itemJson.discountRate;

                var discount = discountRate / 100;

                itemTrObj.find('.item-dis').val(discount);

                PromotionCommon.discountRes.calcuByDiscount(discount, itemTrObj);

                var newPromotionJson = PromotionCommon.result.getNewPromotionJson(itemTrObj);

                //因为itemJson里是没有存decreaseAmount的，而且即使存下来，如果宝贝原价变了，这个也是要变的。。。。。
                itemTrObj.attr("originDecrease", newPromotionJson.decreaseAmount);

            } else if (PromotionCommon.discountType.isDecreaseTypeByItemJson(itemJson)) {

                var decreaseAmount = itemJson.decreaseAmount;
                var jianjia = decreaseAmount / 100;
                itemTrObj.find('.item-jianjia').val(jianjia);
                PromotionCommon.discountRes.calcuByDecrease(jianjia, itemTrObj);

                var newPromotionJson = PromotionCommon.result.getNewPromotionJson(itemTrObj);

                itemTrObj.attr("originDiscount", newPromotionJson.discountRate);

            } else {


                //alert("系统异常，促销类型出错，请联系我们！");
            }
        },
        formatDiscountRate: function(discountRate, pointNum) {

            if (discountRate == "" || isNaN(discountRate)) {
                return 0;
            }

            //小数点位数
            if (pointNum === undefined || pointNum == null || isNaN(pointNum)) {
                pointNum = 2;
            }


            var divider = Math.pow(10, pointNum);


            discountRate = Math.round(discountRate * divider) / divider;


            return discountRate;
        },
        formatDecreaseAmount: function(decreaseAmount, pointNum) {

            if (decreaseAmount == "" || isNaN(decreaseAmount)) {
                return 0;
            }

            //小数点位数
            if (pointNum === undefined || pointNum == null || isNaN(pointNum)) {
                pointNum = 2;
            }


            var divider = Math.pow(10, pointNum);


            decreaseAmount = Math.round(decreaseAmount * divider) / divider;
            return decreaseAmount;
        },
        removePricePoint: function(resultPrice, remainPointNum) {
            if (resultPrice == "" || isNaN(resultPrice)) {
                return 0;
            }
            //小数点位数
            if (remainPointNum === undefined || remainPointNum == null || isNaN(remainPointNum)) {
                remainPointNum = 0;
            }

            var divider = Math.pow(10, remainPointNum);

            resultPrice = Math.floor(resultPrice * divider) / divider;

            return resultPrice;

        },
        formatResultPrice: function(resultPrice, pointNum) {

            if (resultPrice == "" || isNaN(resultPrice)) {
                return 0;
            }

            //小数点位数
            if (pointNum === undefined || pointNum == null || isNaN(pointNum)) {
                pointNum = 2;
            }


            var divider = Math.pow(10, pointNum);

            resultPrice = Math.round(resultPrice * divider) / divider;

            return resultPrice;
        },
        calcuByDiscount: function(discountRate, itemTrObj) {

            var discountObj = itemTrObj.find('.item-dis');
            var decreaseObj = itemTrObj.find('.item-jianjia');
            var resPriceObj = itemTrObj.find('.item-disprice');
            var oldPrice = itemTrObj.attr("oldPrice");
            oldPrice = parseFloat(oldPrice);


            //discountObj.val(discountRate);

            var formatDiscountRate = PromotionCommon.discountRes.formatDiscountRate(discountRate, 2);


            var resPrice = Math.round((oldPrice * formatDiscountRate / 10) * 100) / 100;
            var decreaseAmount = Math.round((oldPrice - resPrice) * 100) / 100;


            resPriceObj.val(resPrice);
            decreaseObj.val(decreaseAmount);

            PromotionCommon.discountRes.showResSkuPrice(itemTrObj);

            PromotionCommon.discountRes.showResultError(discountRate, decreaseAmount, resPrice, oldPrice,
                discountObj, decreaseObj, resPriceObj, itemTrObj);

        },
        calcuByDecrease: function(decreaseAmount, itemTrObj) {

            var discountObj = itemTrObj.find('.item-dis');
            var decreaseObj = itemTrObj.find('.item-jianjia');
            var resPriceObj = itemTrObj.find('.item-disprice');
            var oldPrice = itemTrObj.attr("oldPrice");
            oldPrice = parseFloat(oldPrice);


            //decreaseObj.val(decreaseAmount);

            var formatDecreaseAmount = PromotionCommon.discountRes.formatDecreaseAmount(decreaseAmount, 2);

            var resPrice = Math.round((oldPrice - formatDecreaseAmount) * 100) / 100;
            var discountRate = Math.round((resPrice / oldPrice) * 1000) / 100;

            resPriceObj.val(resPrice);
            discountObj.val(discountRate);

            PromotionCommon.discountRes.showResSkuPrice(itemTrObj);

            PromotionCommon.discountRes.showResultError(discountRate, decreaseAmount, resPrice, oldPrice,
                discountObj, decreaseObj, resPriceObj, itemTrObj);

        },
        calDiscountByResultPrice: function(resPrice, itemTrObj) {

            var discountObj = itemTrObj.find('.item-dis');
            var decreaseObj = itemTrObj.find('.item-jianjia');
            var resPriceObj = itemTrObj.find('.item-disprice');
            var oldPrice = itemTrObj.attr("oldPrice");
            oldPrice = parseFloat(oldPrice);


            //resPriceObj.val(resPrice);

            var formatResultPrice = PromotionCommon.discountRes.formatResultPrice(resPrice, 2);

            var discountRate = Math.round((resPrice / oldPrice) * 1000) / 100;
            var decreaseAmount = Math.round((oldPrice - resPrice) * 100) / 100;


            decreaseObj.val(decreaseAmount);
            discountObj.val(discountRate);

            PromotionCommon.discountRes.showResSkuPrice(itemTrObj);


            PromotionCommon.discountRes.showResultError(discountRate, decreaseAmount, resPrice, oldPrice,
                discountObj, decreaseObj, resPriceObj, itemTrObj);

        },
        //修改折扣，修改促销类型都要调用这个的
        showResSkuPrice: function(itemTrObj) {
            if (PromotionCommon.util.isItemHasSkuPrice(itemTrObj) == false) {
                return;
            }

            var oldMinSkuPrice = itemTrObj.attr("minSkuPrice");
            var oldMaxSkuPrice = itemTrObj.attr("maxSkuPrice");

            oldMinSkuPrice = parseFloat(oldMinSkuPrice);
            oldMaxSkuPrice = parseFloat(oldMaxSkuPrice);

            var newMinSkuPrice = 0;
            var newMaxSkuPrice = 0;

            if (PromotionCommon.discountType.isDiscountType(itemTrObj) == true) {

                var discountRate = itemTrObj.find('.item-dis').val();

                var formatDiscountRate = PromotionCommon.discountRes.formatDiscountRate(discountRate, 2);

                newMinSkuPrice = Math.round((oldMinSkuPrice * formatDiscountRate / 10) * 100) / 100;
                newMaxSkuPrice = Math.round((oldMaxSkuPrice * formatDiscountRate / 10) * 100) / 100;
            } else {
                var decreaseAmount = itemTrObj.find('.item-jianjia').val();

                var formatDecreaseAmount = PromotionCommon.discountRes.formatDecreaseAmount(decreaseAmount, 2);

                newMinSkuPrice = Math.round((oldMinSkuPrice - formatDecreaseAmount) * 100) / 100;
                newMaxSkuPrice = Math.round((oldMaxSkuPrice - formatDecreaseAmount) * 100) / 100;
            }

            itemTrObj.find(".sku-result-span").html("多个结果：￥" + newMinSkuPrice + "-" + newMaxSkuPrice);

        },
        createAllResSkuPriceHtml: function(skuPriceArray, itemTrObj) {

            var allTrHtml = '';

            var isDiscountType = PromotionCommon.discountType.isDiscountType(itemTrObj);
            var discountRate = itemTrObj.find('.item-dis').val();
            var formatDiscountRate = PromotionCommon.discountRes.formatDiscountRate(discountRate, 2);

            var decreaseAmount = itemTrObj.find('.item-jianjia').val();
            var formatDecreaseAmount = PromotionCommon.discountRes.formatDecreaseAmount(decreaseAmount, 2);

            $(skuPriceArray).each(function(index, skuPriceJson) {
                var originSkuPrice = skuPriceJson.skuPrice;
                var resSkuPrice = 0;
                if (isDiscountType == true) {
                    resSkuPrice = Math.round((originSkuPrice * formatDiscountRate / 10) * 100) / 100;
                } else {
                    resSkuPrice = Math.round((originSkuPrice - formatDecreaseAmount) * 100) / 100;
                }

                allTrHtml += '' +
                    '<tr>' +
                    '   <td class="sku-prop-td">' + skuPriceJson.skuProps + '</td> ' +
                    '   <td style="padding-left: 20px;"><span style="color: #a10000">' + resSkuPrice.toFixed(2) + '</span>&nbsp;元</td> ' +
                    '</tr>' +
                    '';

            });

            var html = '' +
                '<table class="sku-discount-table">' +
                '   <tbody>' + allTrHtml + '</tbody>' +
                '</table> ' +
                '';

            return html;

        },
        showResultError: function(
                discountRate, decreaseAmount, resPrice, oldPrice,
                discountObj, decreaseObj, resPriceObj, itemTrObj) {


            var isAddItem = PromotionCommon.init.getIsAddItem();

            var errorMsgObj = itemTrObj.find(".activity-promotion-tip");

            var promotionType = PromotionCommon.discountType.getPromotionType(itemTrObj);

            var checkJson = PromotionCommon.discountRes.checkDiscountResult(discountRate, promotionType, isAddItem);


            if (checkJson.isRight == true) {

                discountObj.removeClass("discount-error");
                decreaseObj.removeClass("discount-error");
                resPriceObj.removeClass("discount-error");
            } else {
                discountObj.addClass("discount-error");
                decreaseObj.addClass("discount-error");
                resPriceObj.addClass("discount-error");
            }

            errorMsgObj.html(checkJson.errorMsg);

        },
        checkDiscountResult: function(discountRate, promotionType, isAddItem) {

            var errorMsg = '';

            if (promotionType == "discount" || promotionType == "decrease") {
                if (discountRate == "") {
                    errorMsg = "折扣不能为空";
                } else if (isNaN(discountRate)) {
                    errorMsg = "只能输入数字";
                } else if (discountRate <= 0) {
                    errorMsg = "折扣必须大于0";
                } else if (discountRate == 10) {
                    if (isAddItem == true) {
                        errorMsg = "";
                    } else {
                        errorMsg = "折扣必须小于10";
                    }
                } else if (discountRate > 10) {
                    errorMsg = "折扣必须小于10";
                } else {
                    errorMsg = "";
                }

                if (errorMsg == '') {
                    return {isRight: true, errorMsg: errorMsg};
                } else {
                    return {isRight: false, errorMsg: errorMsg};
                }
            } else {
                return {isRight: false, errorMsg: "折扣类型出错"};
            }


        }
    }, PromotionCommon.discountRes);



    //打折类型
    PromotionCommon.discountType = PromotionCommon.discountType || {};
    PromotionCommon.discountType = $.extend({
        isDiscountTypeByItemJson: function(itemJson) {
            if (itemJson.promotionType == "discount") {
                return true;
            } else {
                return false;
            }
        },
        isDecreaseTypeByItemJson: function(itemJson) {
            if (itemJson.promotionType == "decrease") {
                return true;
            } else {
                return false;
            }
        },
        isDiscountType: function(itemTrObj) {
            var promotionType = itemTrObj.attr("promotionType");

            return promotionType == "discount";
        },
        isDecreaseType: function(itemTrObj) {
            var promotionType = itemTrObj.attr("promotionType");

            return promotionType == "decrease";
        },
        initPromotionType: function(itemTrObj, itemJson) {
            if (PromotionCommon.discountType.isDecreaseTypeByItemJson(itemJson)) {
                PromotionCommon.discountType.setDecreaseType(itemTrObj);
            } else {
                PromotionCommon.discountType.setDiscountType(itemTrObj);
            }
        },
        getPromotionType: function(itemTrObj) {
            var promotionType = itemTrObj.attr("promotionType");
            return promotionType;
        },
        setDiscountType: function(itemTrObj) {

            itemTrObj.find(".Model-dazhe").show();
            itemTrObj.find(".Model-jianjia").hide();

            itemTrObj.find(".discount-type-span").addClass("font-bold");
            itemTrObj.find(".decrease-type-span").removeClass("font-bold");

            itemTrObj.find(".item-dis").removeClass("discount-not-use-type");
            itemTrObj.find(".item-jianjia").addClass("discount-not-use-type");

            itemTrObj.attr("promotionType", "discount");
        },
        setDecreaseType: function(itemTrObj) {

            itemTrObj.find(".Model-jianjia").show();
            itemTrObj.find(".Model-dazhe").hide();

            itemTrObj.find(".discount-type-span").removeClass("font-bold");
            itemTrObj.find(".decrease-type-span").addClass("font-bold");

            itemTrObj.find(".item-dis").addClass("discount-not-use-type");
            itemTrObj.find(".item-jianjia").removeClass("discount-not-use-type");

            itemTrObj.attr("promotionType", "decrease");
        }

    }, PromotionCommon.discountType);





    PromotionCommon.util = PromotionCommon.util || {};
    PromotionCommon.util = $.extend({

        isItemCanbeEdit: function(itemTrObj, isAddItem) {

            if (isAddItem != true) {
                return true;
            }

            var hasPromotion = itemTrObj.attr("hasPromotion");

            if (hasPromotion == "Promoted") {
                return false;
            } else {
                return true;
            }
        },
        isItemHasSkuPrice: function(itemTrObj) {


            var hasSkuPrice = itemTrObj.attr("hasSkuPrices");

            if (hasSkuPrice == "hasSkuPrices") {
                return true;
            } else {
                return false;
            }

        }

    }, PromotionCommon.util);




})(jQuery,window));
