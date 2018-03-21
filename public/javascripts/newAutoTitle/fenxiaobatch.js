
var TM = TM || {};

((function ($, window) {

    TM.FenxiaoBatch = TM.FenxiaoBatch || {}
    var FenxiaoBatch = TM.FenxiaoBatch;

    FenxiaoBatch.init = FenxiaoBatch.init || {};
    FenxiaoBatch.init = $.extend({

        doInit: function(container) {

            FenxiaoBatch.container = container;

            FenxiaoBatch.init.initTaobaoCategory();
            FenxiaoBatch.init.initSellerCategory();

            FenxiaoBatch.initQtip.doInit();

            container.find(".min-retail-price-add-btn").click(function() {
                FenxiaoBatch.submitPrice.showLowRetailAddDialog();
            });
            container.find(".max-retail-price-minus-btn").click(function() {
                FenxiaoBatch.submitPrice.showHighRetailMinusDialog();
            });
            container.find(".cost-price-add-btn").click(function() {
                FenxiaoBatch.submitPrice.showCostAddDialog();
            });
            container.find(".origin-price-add-btn").click(function() {
                FenxiaoBatch.submitPrice.showOriginAddDialog();
            });
            container.find(".origin-price-minus-btn").click(function() {
                FenxiaoBatch.submitPrice.showOriginMinusDialog();
            });



            //添加事件
            container.find(".tb-category-select").change(function() {
                FenxiaoBatch.show.doShow();
            });
            container.find(".seller-category-select").change(function() {
                FenxiaoBatch.show.doShow();
            });
            container.find(".state-select").change(function() {
                FenxiaoBatch.show.doShow();
            });
            container.find(".search-text").keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    container.find(".search-btn").click();
                }
            });
            container.find(".search-btn").click(function() {
                FenxiaoBatch.show.doShow();
            });


            var sortTdObjs = container.find(".sort-td");
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
                FenxiaoBatch.show.doShow();
            });



            container.find(".all-item-check").click(function() {
                var isChecked = $(this).is(":checked");
                var checkObjs = container.find(".item-checkbox");
                checkObjs.attr("checked", isChecked);
            });

            container.find(".target-item-type-span").click(function() {
                var radioObj = $(this).parent().find("input[name='target-item-type-radio']");
                radioObj.click();
            });


            FenxiaoBatch.show.doShow();

        },
        initTaobaoCategory: function() {

            var container = FenxiaoBatch.init.getContainer();

            $.get("/items/itemCatCount",function(data){
                var catSelectObj = container.find('.tb-category-select');

                var catJsonArray = data;

                var allOptionHtml = '';

                if (catJsonArray === undefined || catJsonArray == null || catJsonArray.length <= 0) {
                    return;
                }

                $(catJsonArray).each(function(index, catJson) {
                    if (catJson.count <= 0){
                        return;
                    }
                    allOptionHtml += '' +
                        '<option value="' + catJson.id + '">' + catJson.name + '</option>' +
                        '';
                });

                catSelectObj.append(allOptionHtml);
            });

        },
        initSellerCategory: function() {

            var container = FenxiaoBatch.init.getContainer();

            $.get("/items/sellerCatCount",function(data){
                var catSelectObj = container.find('.seller-category-select');

                var catJsonArray = data;

                var allOptionHtml = '';

                if (catJsonArray === undefined || catJsonArray == null || catJsonArray.length <= 0) {
                    return;
                }

                $(catJsonArray).each(function(index, catJson) {
                    if (catJson.count <= 0){
                        return;
                    }
                    allOptionHtml += '' +
                        '<option value="' + catJson.id + '">' + catJson.name + '</option>' +
                        '';
                });

                catSelectObj.append(allOptionHtml);
            });


        },
        getContainer: function() {
            return FenxiaoBatch.container;
        }

    }, FenxiaoBatch.init);


    FenxiaoBatch.show = FenxiaoBatch.show || {};
    FenxiaoBatch.show = $.extend({
        targetCurrentPage: 1,
        currentParamData: null,
        doShow: function() {
            FenxiaoBatch.show.doSearch(1);
        },
        doRefresh: function() {
            FenxiaoBatch.show.doSearch(FenxiaoBatch.show.targetCurrentPage);
        },
        doSearch: function(currentPage) {

            if (currentPage < 1) {
                currentPage = 1;
            }

            FenxiaoBatch.show.targetCurrentPage = currentPage;

            var paramData = FenxiaoBatch.show.getParamData();
            if (paramData === undefined || paramData == null) {
                return;
            }

            FenxiaoBatch.show.currentParamData = paramData;

            var container = FenxiaoBatch.init.getContainer();

            var tbodyObj = container.find('.item-table .item-tbody');

            tbodyObj.html('');

            container.find(".paging-div").tmpage({
                currPage: currentPage,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: paramData,
                    dataType: 'json',
                    url: '/fenxiaobatch/searchFenxiaoItems',
                    callback: function(dataJson){
                        FenxiaoBatch.show.targetCurrentPage = dataJson.pn;//记录当前页
                        container.find(".search-item-num").html(dataJson.count);
                        tbodyObj.html('');


                        var itemArray = dataJson.res;

                        if (itemArray === undefined || itemArray == null || itemArray.length <= 0) {
                            tbodyObj.html('<tr><td colspan="7" style="padding: 10px 0px;">当前暂无满足条件的分销宝贝！</td> </tr>');
                            return;
                        }

                        $(itemArray).each(function(index, itemJson) {
                            var trObj = FenxiaoBatch.row.createRow(index, itemJson);
                            tbodyObj.append(trObj);
                        });



                    }
                }

            });

        },
        getParamData: function() {

            var paramData = {};
            var container = FenxiaoBatch.init.getContainer();

            var title = container.find(".search-text").val();
            var status = container.find(".state-select").val();
            var catId = container.find(".tb-category-select").val();
            var sellerCatId = container.find(".seller-category-select").val();


            var orderObj = container.find(".current-sort");
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

            paramData.orderBy = orderBy;
            paramData.isDesc = isDesc;

            paramData.title = title;
            paramData.status = status;
            paramData.catId = catId;
            paramData.sellerCatId = sellerCatId;

            return paramData;

        }
    }, FenxiaoBatch.show);


    FenxiaoBatch.row = FenxiaoBatch.row || {};
    FenxiaoBatch.row = $.extend({
        createRow: function(index, itemJson) {
            var numIid = itemJson.numIid;
            if (numIid === undefined || numIid == null || numIid <= 0) {
                numIid = itemJson.id;
                itemJson.numIid = numIid;
            }

            itemJson.itemLink = "http://item.taobao.com/item.htm?id=" + itemJson.numIid;
            itemJson.priceStr = itemJson.price.toFixed(2);
            var statusStr = "";
            if (itemJson.status == 0) {
                statusStr = "在仓库";
            } else if (itemJson.status == 1) {
                statusStr = "在架上";
            }
            itemJson.statusStr = statusStr;

            var trObj = $('#fenxiaoItemRow').tmpl(itemJson);

            var container = FenxiaoBatch.init.getContainer();

            var checkCallback = function(checkObj) {
                var isChecked = checkObj.is(":checked");
                if (isChecked == false) {
                    container.find(".all-item-check").attr("checked", false);
                } else {
                    var checkObjs = container.find(".item-checkbox");
                    var isAllCheck = true;
                    checkObjs.each(function() {
                        if ($(this).is(":checked") == false) {
                            isAllCheck = false;
                            return false;
                        }
                    });
                    container.find(".all-item-check").attr("checked", isAllCheck);
                }
            }
            trObj.find(".item-checkbox").click(function() {
                checkCallback($(this));
            });

            return trObj;
        }
    }, FenxiaoBatch.row);


    FenxiaoBatch.initQtip = FenxiaoBatch.initQtip || {};
    FenxiaoBatch.initQtip = $.extend({
        doInit: function() {

            var initCallback = function(btnObj, text) {
                btnObj.qtip({
                    content: {
                        text: text
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
                        /*when: {
                            event: 'mouseover'
                        },*/
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
            };


            var container = FenxiaoBatch.init.getContainer();
            initCallback(container.find(".min-retail-price-add-btn"),
                '新的宝贝价格，是在分销商最低零售价的基础上进行加价操作。');

            initCallback(container.find(".max-retail-price-minus-btn"),
                '新的宝贝价格，是在分销商最高零售价的基础上进行减价操作。');

            initCallback(container.find(".cost-price-add-btn"),
                '新的宝贝价格，是在采购价的基础上进行加价操作，保证一定的利润。');

            initCallback(container.find(".origin-price-add-btn"),
                '新的宝贝价格，是在原来价格的基础上进行加价操作。');

            initCallback(container.find(".origin-price-minus-btn"),
                '新的宝贝价格，是在原来价格的基础上进行减价操作。');

        }
    }, FenxiaoBatch.initQtip);






    FenxiaoBatch.submitPrice = FenxiaoBatch.submitPrice || {};
    FenxiaoBatch.submitPrice = $.extend({

        showLowRetailAddDialog: function() {
            FenxiaoBatch.priceCommon.showDialog({
                tmplId: 'lowRetailPriceDialogTmpl',
                dialogTitle: '根据最低零售价加价'
            });
        },
        showHighRetailMinusDialog: function() {
            FenxiaoBatch.priceCommon.showDialog({
                tmplId: 'highRetailPriceDialogTmpl',
                dialogTitle: '根据最高零售价减价'
            });
        },
        showCostAddDialog: function() {
            FenxiaoBatch.priceCommon.showDialog({
                tmplId: 'costPriceDialogTmpl',
                dialogTitle: '根据采购价加价'
            });
        },
        showOriginAddDialog: function() {
            FenxiaoBatch.priceCommon.showDialog({
                tmplId: 'originPriceAddDialogTmpl',
                dialogTitle: '根据宝贝原价加价'
            });
        },
        showOriginMinusDialog: function() {
            FenxiaoBatch.priceCommon.showDialog({
                tmplId: 'originPriceMinusDialogTmpl',
                dialogTitle: '根据宝贝原价减价'
            });
        }

    }, FenxiaoBatch.submitPrice);



    FenxiaoBatch.priceCommon = FenxiaoBatch.priceCommon || {};
    FenxiaoBatch.priceCommon = $.extend({
        dialogObjMap: {},
        showDialog: function(typeJson) {

            var tmplId = typeJson.tmplId;

            var dialogObj = FenxiaoBatch.priceCommon.dialogObjMap[tmplId];

            if (dialogObj === undefined || dialogObj == null || dialogObj.length != 1) {
                dialogObj = FenxiaoBatch.priceCommon.createCommonDialogObj(typeJson);
                FenxiaoBatch.priceCommon.dialogObjMap[tmplId] = dialogObj;
            }
            dialogObj.dialog("open");
        },
        createCommonDialogObj: function(typeJson) {
            //1.先设置加价幅度
            //5.最低利润
            //2.sku价格
            //3.如果失败，超出最高价
            //4.设置小数点

            var tmplId = typeJson.tmplId;
            var dialogTitle = typeJson.dialogTitle;


            var dialogCss = tmplId + '-dialog-div';
            $("." + dialogCss).remove();


            var innerObj = $("#" + tmplId).tmpl({});
            var dialogObj = $('<div class="' + dialogCss + '"></div>');
            dialogObj.html(innerObj);

            FenxiaoBatch.priceCommon.initRadioEvent(dialogObj);



            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:550,
                width:650,
                title: dialogTitle,
                autoOpen: false,
                resizable: false,
                buttons:{
                    '确定': function() {

                        var submitParam = FenxiaoBatch.priceCommon.getCommonSubmitParam(dialogObj);
                        if (submitParam === undefined || submitParam == null) {
                            return;
                        }

                        if (FenxiaoBatch.priceCommon.confirmSubmit(submitParam) != true) {
                            return;
                        }

                        $.ajax({
                            url : "/fenxiaobatch/submitFenxiaoItemPrice",
                            data : submitParam,
                            type : 'post',
                            success : function(dataJson) {

                                if (TM.AutoTitleUtil.util.checkIsW2AuthError(dataJson) == true) {
                                    return;
                                }

                                if (TM.AutoTitleUtil.util.judgeAjaxResult(dataJson) == false) {
                                    return;
                                }


                                var modifyPriceRes = TM.AutoTitleUtil.util.getAjaxResultJson(dataJson);


                                TM.AutoTitleUtil.errors.showErrors(modifyPriceRes, function() {


                                    dialogObj.dialog('close');

                                    FenxiaoBatch.show.doRefresh();

                                    return;

                                }, function() {

                                    dialogObj.dialog('close');

                                    FenxiaoBatch.show.doRefresh();

                                    return;
                                });


                            }
                        });

                    },'取消':function() {

                        dialogObj.dialog('close');
                    }
                }
            });

            return dialogObj;
        },
        confirmSubmit: function(submitParam) {
            var confirmMsg = '';
            var config = submitParam.config;

            if (config.modifyType == 'lowRetailPercentAdd') {
                confirmMsg = '您设置的加价百分比是' + config.modifyParameter
                    + '%';
            } else if (config.modifyType == 'lowRetailFixedAdd') {
                confirmMsg = '您设置的加价金额是' + config.modifyParameter
                    + '元';
            } else if (config.modifyType == 'highRetailPercentMinus') {
                confirmMsg = '您设置的减价百分比是' + config.modifyParameter
                    + '%';
            } else if (config.modifyType == 'highRetailFixedMinus') {
                confirmMsg = '您设置的减价金额是' + config.modifyParameter
                    + '元';
            } else if (config.modifyType == 'costPricePercentAdd') {
                confirmMsg = '您设置的加价百分比是' + config.modifyParameter
                    + '%';
            } else if (config.modifyType == 'costPriceFixedAdd') {
                confirmMsg = '您设置的加价金额是' + config.modifyParameter
                    + '元';
            } else if (config.modifyType == 'originPricePercentAdd') {
                confirmMsg = '您设置的加价百分比是' + config.modifyParameter
                    + '%';
            } else if (config.modifyType == 'originPriceFixedAdd') {
                confirmMsg = '您设置的加价金额是' + config.modifyParameter
                    + '元';
            } else if (config.modifyType == 'originPricePercentMinus') {
                confirmMsg = '您设置的减价百分比是' + config.modifyParameter
                    + '%';
            } else if (config.modifyType == 'originPriceFixedMinus') {
                confirmMsg = '您设置的减价金额是' + config.modifyParameter
                    + '元';
            } else {
                alert('改价类型出错，请联系我们！');
                return false;
            }


            if (FenxiaoBatch.util.isSelectItemType(submitParam.itemType)) {
                confirmMsg += '，确定要修改' + submitParam.selectNumIidCount + '个选中宝贝的价格？';
            } else if (FenxiaoBatch.util.isAllSearchItemType(submitParam.itemType)) {
                confirmMsg += '，确定要修改所有满足搜索条件的宝贝的价格？';
            } else {
                alert('改价宝贝类型出错，请联系我们！');
                return false;
            }


            if (confirm(confirmMsg) == false) {
                return false;
            } else {
                return true;
            }
        },
        getCommonSubmitParam: function(dialogObj) {

            var container = FenxiaoBatch.init.getContainer();
            var itemType = container.find('input[name="target-item-type-radio"]:checked').val();

            var submitParam = {};

            if (FenxiaoBatch.util.isSelectItemType(itemType)) {

                var selectNumIidArray = [];
                var checkObjs = container.find(".item-checkbox:checked");
                if (checkObjs.length <= 0) {
                    alert("请先选择要改价的宝贝！");
                    return null;
                }
                checkObjs.each(function() {
                    var numIid = $(this).attr('numIid');
                    selectNumIidArray[selectNumIidArray.length] = numIid;
                });

                submitParam.selectNumIids = selectNumIidArray.join(',');
                submitParam.selectNumIidCount = selectNumIidArray.length;

            } else if (FenxiaoBatch.util.isAllSearchItemType(itemType)) {

                submitParam = $.extend({}, submitParam, FenxiaoBatch.show.currentParamData);

            } else {
                alert("系统异常，宝贝类型出错，请联系我们！");
                return null;
            }


            submitParam.itemType = itemType;


            var config = FenxiaoBatch.priceCommon.getPriceConfig(dialogObj);
            if (config === undefined || config == null) {
                return null;
            }

            submitParam.config = config;

            return submitParam;

        },
        getPriceConfig: function(dialogObj) {


            var modifyPriceTypeObj = dialogObj.find('input.modify-price-type-radio:checked');
            if (modifyPriceTypeObj.length <= 0) {
                alert('请先选择改价类型！');
                return null;
            }

            var modifyParameterDivCss = modifyPriceTypeObj.attr("target");
            var modifyParameter = dialogObj.find('.' + modifyParameterDivCss).find('.modify-parameter-input').val();

            if (modifyParameter === undefined || modifyParameter == null || modifyParameter == ''
                    || isNaN(modifyParameter) || modifyParameter < 0) {

                alert("请先输入正确的改价幅度，不能为空，且必须为数字，且不能小于0！");
                return null;
            }

            var priceConfig = {};
            priceConfig.modifyType = modifyPriceTypeObj.val();
            priceConfig.modifyParameter = modifyParameter;
            priceConfig.skuEditType = dialogObj.find('input.sku-config-radio:checked').val();

            var minProfit = dialogObj.find('.min-profit-input').val();
            if (minProfit === undefined || minProfit == null || minProfit == ''
                    || isNaN(minProfit) || minProfit < 0) {

                alert("请先输入正确的最低利润，不能为空，且必须为数字，且不能小于0！");
                return null;
            }

            priceConfig.minProfit = minProfit;

            priceConfig.overType = dialogObj.find('input.overflow-config-radio:checked').val();
            priceConfig.decimalType = dialogObj.find('input.dot-config-radio:checked').val();


            return priceConfig;
        },
        initRadioEvent: function(dialogObj) {

            var clickCallback = function(radioObj) {
                var targetDivCss = radioObj.attr("target");
                var targetGroupCss = radioObj.attr("targetGroup");

                if (targetDivCss === undefined || targetDivCss == null || targetDivCss == "") {
                    return;
                }
                if (targetGroupCss === undefined || targetGroupCss == null || targetGroupCss == "") {
                    return;
                }

                dialogObj.find('.' + targetGroupCss).hide();
                dialogObj.find('.' + targetDivCss).show();
            }

            dialogObj.find(".radio-span").click(function() {
                var radioObj = $(this).parent().find('input[type="radio"]');
                radioObj.attr("checked", true);
                clickCallback(radioObj);
            });
            dialogObj.find('input[type="radio"]').click(function() {
                var radioObj = $(this);
                clickCallback(radioObj);
            });
        }

    }, FenxiaoBatch.priceCommon);



    FenxiaoBatch.util = FenxiaoBatch.util || {};
    FenxiaoBatch.util = $.extend({
        isSelectItemType: function(itemType) {
            return itemType == 'selectedItems';
        },
        isAllSearchItemType: function(itemType) {
            return itemType == 'allSearchItem';
        }
    }, FenxiaoBatch.util);


})(jQuery, window));