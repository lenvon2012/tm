

var TM = TM || {};

((function ($, window) {

    TM.SellerCatItemsOneKey = TM.SellerCatItemsOneKey || {};
    var SellerCatItemsOneKey = TM.SellerCatItemsOneKey;

    SellerCatItemsOneKey.init = SellerCatItemsOneKey.init || {};
    SellerCatItemsOneKey.init = $.extend({
        doInit: function(container) {
            SellerCatItemsOneKey.container = container;

            container.find(".seller-cat-item-btn").unbind().click(function() {
                SellerCatItemsOneKey.dialog.showSellerCatDialog();
            });
        },
        getContainer: function() {
            return SellerCatItemsOneKey.container;
        }
    }, SellerCatItemsOneKey.init);


    SellerCatItemsOneKey.dialog = SellerCatItemsOneKey.dialog || {};
    SellerCatItemsOneKey.dialog = $.extend({

        showSellerCatDialog: function() {

            $('.seller-cat-item-dialog-div').remove();

            var dialogObj = $("#sellerCatItemDialogTmpl").tmpl({});

            SellerCatItemsOneKey.dialog.initTbCatSelect(dialogObj);
            SellerCatItemsOneKey.dialog.initSellerCatSelect(dialogObj);
            SellerCatItemsOneKey.dialog.initTargetSellerCatSelect(dialogObj);

            dialogObj.find('.check-span').unbind().click(function() {
                $(this).parent().find('input[type="checkbox"]').click();
            });

            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:400,
                width:600,
                title: '一键分类宝贝',
                autoOpen: false,
                resizable: false,
                buttons:{
                    '确定': function() {

                        SellerCatItemsOneKey.submit.doSubmitSellerCat(dialogObj);

                    },'取消':function() {

                        dialogObj.dialog('close');
                    }
                }
            });

            dialogObj.dialog('open');
        },
        initTbCatSelect: function(dialogObj) {

            var selectObj = dialogObj.find('.tb-item-cat-select');

            $.get("/items/itemCatCount",function(data){

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

                selectObj.append(allOptionHtml);
            });
        },
        initSellerCatSelect: function(dialogObj) {

            var selectObj = dialogObj.find('.seller-item-cat-select');

            $.get("/items/sellerCatCount",function(data){

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

                selectObj.append(allOptionHtml);

            });
        },
        initTargetSellerCatSelect: function(dialogObj) {

            var selectObj = dialogObj.find('.target-seller-item-cat-select');

            $.get("/SellerCatItems/getShopSellerCats",function(data){

                var catJsonArray = data;

                var allOptionHtml = '';

                if (catJsonArray === undefined || catJsonArray == null || catJsonArray.length <= 0) {
                    return;
                }

                $(catJsonArray).each(function(index, catJson) {

                    allOptionHtml += '' +
                        '<option value="' + catJson.cid + '">' + catJson.name + '</option>' +
                        '';
                });

                selectObj.append(allOptionHtml);

            });
        }

    }, SellerCatItemsOneKey.dialog);


    SellerCatItemsOneKey.submit = SellerCatItemsOneKey.submit || {};
    SellerCatItemsOneKey.submit = $.extend({
        doSubmitSellerCat: function(dialogObj) {

            var paramData = SellerCatItemsOneKey.submit.getParamData(dialogObj);

            if (paramData === undefined || paramData == null) {
                return;
            }

            $.ajax({
                url : "/SellerCatItems/countSubmitItems",
                data : paramData,
                type : 'post',
                success : function(dataJson) {


                    if (TM.AutoTitleUtil.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }

                    var itemCount = TM.AutoTitleUtil.util.getAjaxResultJson(dataJson);
                    if (confirm('确定要一键分类' + itemCount + '个宝贝？') == false) {
                        return;
                    }

                    $.ajax({
                        url : "/SellerCatItems/setSellerCatItems",
                        data : paramData,
                        type : 'post',
                        success : function(dataJson) {


                            if (TM.AutoTitleUtil.util.judgeAjaxResult(dataJson) == false) {
                                return;
                            }

                            var updateRes = TM.AutoTitleUtil.util.getAjaxResultJson(dataJson);

                            TM.AutoTitleUtil.errors.showErrors(updateRes, function() {


                                dialogObj.dialog('close');

                                return;

                            }, function() {

                                dialogObj.dialog('close');
                                return;
                            });


                        }
                    });

                }
            });




        },
        getParamData: function(dialogObj) {

            var paramData = {};

            paramData.title = dialogObj.find('.search-item-title-input').val();
            paramData.itemStatus = dialogObj.find('.item-status-select').val();
            paramData.tbCid = dialogObj.find('.tb-item-cat-select').val();
            paramData.sellerCid = dialogObj.find('.seller-item-cat-select').val();

            paramData.targetSellerCid = dialogObj.find('.target-seller-item-cat-select').val();

            if (paramData.targetSellerCid === undefined || paramData.targetSellerCid == null
                || paramData.targetSellerCid == "" || paramData.targetSellerCid <= 0) {
                alert('请先选择目标的店铺分类！');
                return null;
            }

            if (paramData.sellerCid == paramData.targetSellerCid) {
                alert('目标店铺分类不能与原来店铺分类相同！');
                return null;
            }

            paramData.isRemoveOriginSellerCat = dialogObj.find('.remove-origin-cat-check').is(':checked');

            return paramData;
        }
    }, SellerCatItemsOneKey.submit);


})(jQuery, window));
