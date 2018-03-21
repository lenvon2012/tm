var TM = TM || {};

((function ($, window) {

    TM.SellerCatItems = TM.SellerCatItems || {};
    var SellerCatItems = TM.SellerCatItems;

    SellerCatItems.init = SellerCatItems.init || {};
    SellerCatItems.init = $.extend({

        doInit: function(container) {

            SellerCatItems.container = container;

            SellerCatItems.init.initTaobaoCategory();
            SellerCatItems.init.initSellerCategory();


            container.find('.seller-cat-select-item-btn').unbind().click(function() {
                SellerCatItems.submit.submitSellerCat(true);
            });

            container.find('.seller-cat-all-item-btn').unbind().click(function() {
                SellerCatItems.submit.submitSellerCat(false);
            });


            //添加事件
            container.find(".tb-category-select").change(function() {
                SellerCatItems.show.doShow();
            });
            container.find(".seller-category-select").change(function() {
                SellerCatItems.show.doShow();
            });
            container.find(".state-select").change(function() {
                SellerCatItems.show.doShow();
            });
            container.find(".search-text").keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    container.find(".search-btn").click();
                }
            });
            container.find(".search-btn").click(function() {
                SellerCatItems.show.doShow();
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
                SellerCatItems.show.doShow();
            });



            container.find(".all-item-check").click(function() {
                var isChecked = $(this).is(":checked");
                var checkObjs = container.find(".item-checkbox");
                checkObjs.attr("checked", isChecked);
            });


            SellerCatItems.show.doShow();

        },
        initTaobaoCategory: function() {

            var container = SellerCatItems.init.getContainer();

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

            var container = SellerCatItems.init.getContainer();

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
            return SellerCatItems.container;
        }

    }, SellerCatItems.init);


    SellerCatItems.show = SellerCatItems.show || {};
    SellerCatItems.show = $.extend({
        targetCurrentPage: 1,
        currentParamData: null,
        totalCount: 0,
        doShow: function() {
            SellerCatItems.show.doSearch(1);
        },
        doRefresh: function() {
            SellerCatItems.show.doSearch(SellerCatItems.show.targetCurrentPage);
        },
        doSearch: function(currentPage) {

            if (currentPage < 1) {
                currentPage = 1;
            }

            SellerCatItems.show.targetCurrentPage = currentPage;

            var paramData = SellerCatItems.show.getParamData();
            if (paramData === undefined || paramData == null) {
                return;
            }

            SellerCatItems.show.currentParamData = paramData;

            var container = SellerCatItems.init.getContainer();

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
                    url: '/sellercatitems/searchItemsByRules',
                    callback: function(dataJson){
                        SellerCatItems.show.targetCurrentPage = dataJson.pn;//记录当前页
                        SellerCatItems.show.totalCount = dataJson.count;

                        tbodyObj.html('');


                        var itemArray = dataJson.res;

                        if (itemArray === undefined || itemArray == null || itemArray.length <= 0) {
                            return;
                        }

                        $.get("/SellerCatItems/getShopSellerCats",function(sellerCatArray){

                            var sellerCatMap = {};
                            $(sellerCatArray).each(function(index, sellerCatJson) {

                                sellerCatMap[sellerCatJson.cid] = sellerCatJson.name;

                            });

                            $(itemArray).each(function(index, itemJson) {
                                var trObj = SellerCatItems.row.createRow(index, itemJson, sellerCatMap);
                                tbodyObj.append(trObj);
                            });


                        });



                    }
                }

            });

        },
        getParamData: function() {

            var paramData = {};
            var container = SellerCatItems.init.getContainer();

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
            paramData.itemStatus = status;
            paramData.tbCid = catId;
            paramData.sellerCid = sellerCatId;

            return paramData;

        }
    }, SellerCatItems.show);


    SellerCatItems.row = SellerCatItems.row || {};
    SellerCatItems.row = $.extend({
        createRow: function(index, itemJson, sellerCatMap) {
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

            var trObj = $('#sellerCatItemRow').tmpl(itemJson);

            var sellerCatHtml = SellerCatItems.row.getSellerCatHtml(itemJson, sellerCatMap);
            trObj.find('.item-seller-cats-td').html(sellerCatHtml);

            var container = SellerCatItems.init.getContainer();

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
        },
        getSellerCatHtml: function(itemJson, sellerCatMap) {

            var sellerCids = itemJson.itemSellerCids;
            if (sellerCids === undefined || sellerCids == null || sellerCids == "" || sellerCids == '-1') {
                return '尚未分类';
            }

            var sellerCidArray = sellerCids.split(',');

            var htmlArray = [];

            $(sellerCidArray).each(function(index, cid) {
                if (cid == '') {
                    return;
                }
                var name = sellerCatMap[cid];
                if (name === undefined || name == null || name == '') {
                    return;
                }

                htmlArray.push('<div style="padding: 5px 0px;">' + name + '</div>');
            });

            return htmlArray.join('');
        }
    }, SellerCatItems.row);



    SellerCatItems.submit = SellerCatItems.submit || {};
    SellerCatItems.submit = $.extend({
        submitSellerCat: function(isSelectItems) {

            $('.seller-cat-item-dialog-div').remove();
            var container = SellerCatItems.init.getContainer();


            if (isSelectItems == true) {
                var itemCount = container.find('.item-checkbox:checked').length;
                if (itemCount <= 0) {
                    alert('请先选择要分类的宝贝！');
                    return;
                }
            } else {
                var itemCount = SellerCatItems.show.totalCount;
                if (itemCount <= 0) {
                    alert('当前没有需要分类的宝贝！');
                    return;
                }
            }

            var dialogObj = $("#sellerCatItemDialogTmpl").tmpl({});

            SellerCatItems.submit.initTargetSellerCatSelect(dialogObj);

            var dialogTitle = isSelectItems == true ? '分类选中宝贝' : '分类所有搜索宝贝';

            dialogObj.find('.check-span').unbind().click(function() {
                $(this).parent().find('input[type="checkbox"]').click();
            });

            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:250,
                width:400,
                title: dialogTitle,
                autoOpen: false,
                resizable: false,
                buttons:{
                    '确定': function() {

                        var submitParam = SellerCatItems.submit.getSubmitParams(isSelectItems, dialogObj);

                        if (submitParam === undefined || submitParam == null) {
                            return;
                        }

                        if (isSelectItems == true) {
                            if (confirm('确定要对' + submitParam.selectNumIidCount + '个选中的宝贝进行分类？') == false) {
                                return;
                            }
                        } else {
                            if (confirm('当前搜索结果共' + submitParam.selectNumIidCount + '个宝贝，确定要对所有宝贝进行分类？') == false) {
                                return;
                            }
                        }

                        $.ajax({
                            url : "/SellerCatItems/setSellerCatItems",
                            data : submitParam,
                            type : 'post',
                            success : function(dataJson) {


                                if (TM.AutoTitleUtil.util.judgeAjaxResult(dataJson) == false) {
                                    return;
                                }

                                var updateRes = TM.AutoTitleUtil.util.getAjaxResultJson(dataJson);

                                TM.AutoTitleUtil.errors.showErrors(updateRes, function() {


                                    SellerCatItems.show.doRefresh();

                                    dialogObj.dialog('close');

                                    return;

                                }, function() {

                                    SellerCatItems.show.doRefresh();

                                    dialogObj.dialog('close');

                                    return;
                                });



                            }
                        });


                    },'取消':function() {

                        dialogObj.dialog('close');
                    }
                }
            });

            dialogObj.dialog('open');



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
        },
        getSubmitParams: function(isSelectItems, dialogObj) {

            var paramData = {};

            var container = SellerCatItems.init.getContainer();

            if (isSelectItems == true) {
                var checkObjs = container.find('.item-checkbox:checked');
                if (checkObjs.length <= 0) {
                    alert('请先选择要分类的宝贝！');
                    return null;
                }
                var selectNumIidArray = [];

                checkObjs.each(function() {
                    var numIid = $(this).attr('numIid');
                    selectNumIidArray[selectNumIidArray.length] = numIid;
                });

                paramData.selectNumIids = selectNumIidArray.join(',');
                paramData.selectNumIidCount = selectNumIidArray.length;
            } else {

                paramData = $.extend({}, paramData, SellerCatItems.show.currentParamData);

                paramData.selectNumIidCount = SellerCatItems.show.totalCount;
            }

            paramData.isSelectItems = isSelectItems;

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
    }, SellerCatItems.submit);

})(jQuery, window));
