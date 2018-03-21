

((function ($, window) {

    TM.AddPromotionCommon = TM.AddPromotionCommon || {};

    var AddPromotionCommon = TM.AddPromotionCommon;

    AddPromotionCommon.init = AddPromotionCommon.init || {};
    AddPromotionCommon.init = $.extend({

        doInit: function(container, isRestartActivity) {

            AddPromotionCommon.container = container;
            AddPromotionCommon.isRestartActivity = isRestartActivity;



        },
        getContainer: function() {
            return AddPromotionCommon.container;
        },
        getIsRestartActivity: function() {
            return AddPromotionCommon.isRestartActivity;
        }

    }, AddPromotionCommon.init);


    /**
     * 选择宝贝后点击设置折扣
     * @type {*}
     */
    AddPromotionCommon.reload = AddPromotionCommon.reload || {};
    AddPromotionCommon.reload = $.extend({
        doReload: function() {

            //这个要在initActivityPromotionCount之前，因为initActivityPromotionCount要显示错误个数
            AddPromotionCommon.reload.syncSelectItems();

            AddPromotionCommon.reload.initActivityPromotionCount();

            /*AddPromotionCommon.show.targetCurrentPage = 1;
            var container = AddPromotionCommon.init.getContainer();
            var tbodyObj = container.find(".promotion-table");

            tbodyObj.html("");*/

            //先清空页面，然后再刷新宝贝
            AddPromotionCommon.show.doClearPageAndRefresh();

        },
        //删除之后，提交之后，都要调用
        doRefreshAfterSubmit: function(successNumIidArray, isDeleteOption) {
            if (successNumIidArray === undefined || successNumIidArray == null || successNumIidArray.length <= 0) {
                successNumIidArray = [];
            }


            //
            TM.PromotionCommon.result.removeSomeItemsFromModifyArray(successNumIidArray);

            //删除选中的宝贝
            TM.UmpSelectItem.result.removeSomeSelectNumIids(successNumIidArray);

            //刷新item-type-select列表


            if (isDeleteOption == false) {
                AddPromotionCommon.reload.initActivityPromotionCount();

            } else {
                var container = AddPromotionCommon.init.getContainer();
                var itemTypeSelectObj = container.find(".item-type-select");
                var existPromotionNum = itemTypeSelectObj.find('.exist-activity-item-option').attr("existPromotionNum");

                existPromotionNum = parseInt(existPromotionNum);
                AddPromotionCommon.reload.refreshItemTypeSelect(existPromotionNum);
            }



            AddPromotionCommon.show.doClearPageAndRefresh();
        },
        //选择宝贝后，有些宝贝可能是之前选择的，也设置了折扣，但现在又不选了，这时要把它从PromotionCommon的selectItem中去掉
        syncSelectItems: function() {

            var selectNumIidArray = TM.UmpSelectItem.result.getSelectNumIidArray();

            TM.PromotionCommon.result.removeNotExistItems(selectNumIidArray);

        },
        initActivityPromotionCount: function() {
            var container = AddPromotionCommon.init.getContainer();



            var paramData = {};
            paramData.tmActivityId = TM.UmpUtil.util.getTMActivityId(container);


            $.ajax({
                url : '/umppromotion/countActivityActivePromotions',
                data : paramData,
                type : 'post',
                success : function(dataJson) {


                    if (TM.UmpUtil.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }

                    var existPromotionNum = TM.UmpUtil.util.getAjaxResultJson(dataJson);

                    AddPromotionCommon.reload.refreshItemTypeSelect(existPromotionNum);
                }
            });
        },
        refreshItemTypeSelect: function(existPromotionNum) {
            var container = AddPromotionCommon.init.getContainer();

            var itemTypeSelectObj = container.find(".item-type-select");
            var selectNumIidArray = TM.UmpSelectItem.result.getSelectNumIidArray();
            var newPromotionNum = selectNumIidArray.length;

            var totalPromotionNum = newPromotionNum + existPromotionNum;

            var currentSelectType = itemTypeSelectObj.find(".item-type-text").attr("itemType");

            if (currentSelectType == 1) {
                itemTypeSelectObj.find(".item-type-text").html('活动中所有宝贝(' + totalPromotionNum + '个)');
            } else if (currentSelectType == 2) {
                itemTypeSelectObj.find(".item-type-text").html('活动中原有宝贝(' + existPromotionNum + '个)');
            } else if (currentSelectType == 4) {
                itemTypeSelectObj.find(".item-type-text").html('活动中新增宝贝(' + newPromotionNum + '个)');
            } else if (currentSelectType == 8) {
                itemTypeSelectObj.find(".item-type-text").html('参数错误的宝贝');
            }

            var existOptionObj = itemTypeSelectObj.find('.exist-activity-item-option');

            existOptionObj.attr("existPromotionNum", existPromotionNum);
            existOptionObj.html('活动中原有宝贝(' + existPromotionNum + '个)');
            itemTypeSelectObj.find('.new-activity-item-option').html('活动中新增宝贝(' + newPromotionNum + '个)');
            itemTypeSelectObj.find('.all-activity-item-option').html('活动中所有宝贝(' + totalPromotionNum + '个)');
            itemTypeSelectObj.find('.error-activity-item-option').html('参数错误的宝贝');

        }
    }, AddPromotionCommon.reload);



    AddPromotionCommon.show = AddPromotionCommon.show || {};
    AddPromotionCommon.show = $.extend({
        targetCurrentPage: 1,
        doShow: function() {
            var container = AddPromotionCommon.init.getContainer();
            var isAddItem = true;
            TM.PromotionCommon.result.addPageModifyPromotions(container, isAddItem);
            AddPromotionCommon.show.doSearchWithPage(1, false);
        },
        doClearPageAndRefresh: function() {
            var container = AddPromotionCommon.init.getContainer();
            //var tbodyObj = container.find(".promotion-table");

            //tbodyObj.html("");

            AddPromotionCommon.show.doSearchWithPage(AddPromotionCommon.show.targetCurrentPage, false);
        },
        doSearchWithPage: function(currentPage, isAddPageModifyPromotions) {

            if (currentPage < 1) {
                currentPage = 1;
            }

            AddPromotionCommon.show.targetCurrentPage = currentPage;

            var container = AddPromotionCommon.init.getContainer();
            var tbodyObj = container.find(".promotion-table");


            var paramData = AddPromotionCommon.show.getSearchParams();

            if (paramData === undefined || paramData == null) {
                return;
            }

            if (AddPromotionCommon.init.getIsRestartActivity() == true) {
                paramData.isRestartActivity = true;
            } else {
                paramData.isRestartActivity = false;
            }




            container.find(".promotion-paging-div").tmpage({
                currPage: currentPage,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: paramData,
                    dataType: 'json',
                    url: '/umppromotion/queryPromotionSelectedItems',
                    callback: function(dataJson){

                        var isAddItem = true;

                        if (isAddPageModifyPromotions == true) {
                            TM.PromotionCommon.result.addPageModifyPromotions(container, isAddItem);
                        } else {
                            //只是第一次不获取页面数据，下一次点击下一页的时候，还是要获取的
                            isAddPageModifyPromotions = true;
                        }


                        AddPromotionCommon.show.targetCurrentPage = dataJson.pn;

                        //要先addPageModifyPromotions，再清空tbodyObj
                        tbodyObj.html("");

                        var itemJsonArray = dataJson.res;


                        //还有一种情况就是res为空的时候，要提示用户没有数据
                        if(itemJsonArray===null||itemJsonArray.length===0)
                        var nullHtml="<div class='null-html'>暂时没有符合要求的数据</div>";
                        $(nullHtml).appendTo(tbodyObj);

                        $(itemJsonArray).each(function(index, itemJson) {
                            var trObj = TM.PromotionCommon.row.createCommonRow(index, itemJson, isAddItem);

                            AddPromotionCommon.event.initDeleteEvent(trObj);

                            tbodyObj.append(trObj);
                        });

                    }
                }

            });

        },
        getSearchParams: function() {
            var container = AddPromotionCommon.init.getContainer();



            var paramData = {};

            paramData.tmActivityId = TM.UmpUtil.util.getTMActivityId(container);
            paramData.title = container.find('.search-text-input').val();
            paramData.itemType = container.find(".item-type-text").attr("itemType");

            if (paramData.itemType == 1 || paramData.itemType == 4) {

                var selectNumIidArray = TM.UmpSelectItem.result.getSelectNumIidArray();

                paramData.selectNumIids = selectNumIidArray.join(',');
            } else if (paramData.itemType == 8) {

                var errorNumIidArray = TM.PromotionCommon.result.getErrorModifyNumIids(true);


                paramData.selectNumIids = errorNumIidArray.join(',');
            }



            return paramData;
        }
    }, AddPromotionCommon.show);


    AddPromotionCommon.event = AddPromotionCommon.event || {};
    AddPromotionCommon.event = $.extend({
        initNextStepEvent: function(outContainer) {

            var container = outContainer;
            container.find(".goto-set-discount-btn").click(function() {

                var selectNumIidArray = TM.UmpSelectItem.result.getSelectNumIidArray();

                if (selectNumIidArray === undefined || selectNumIidArray == null || selectNumIidArray.length <= 0) {
                    alert("请先选择要添加的宝贝！");
                    return;
                }


                container.find(".first-step-header").removeClass("bold-header");

                container.find(".second-step-header").addClass("bold-header");

                container.find(".select-item-container").hide();

                container.find(".set-discount-container").show();

                //重新加载数据
                TM.AddPromotionCommon.reload.doReload();


            });

        },
        initPrevStepEvent: function(outContainer) {

            var container = outContainer;

            container.find(".goto-choose-item-btn").click(function() {

                var isAddItem = true;

                TM.PromotionCommon.result.addPageModifyPromotions(container, isAddItem);


                container.find(".first-step-header").addClass("bold-header");

                container.find(".second-step-header").removeClass("bold-header");

                container.find(".set-discount-container").hide();

                container.find(".select-item-container").show();

                //重新加载数据
                TM.PromotionSelectItem.reload.doReload();


            });

        },
        initSearchEvent: function() {

            var searchCallback = function() {
                AddPromotionCommon.show.doShow();
            }


            var container = AddPromotionCommon.init.getContainer();

            container.find('.search-text-input').keyup(function(event) {
                var lable_key = container.find('.search-text-input').val();
                if (!lable_key) {
                    container.find('.combobox-label-item').show();
                } else {
                    container.find('.combobox-label-item').hide();
                }
            });

            container.find('.search-text-input').keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    searchCallback();
                }
            });

            container.find('.search-btn').unbind().click(function(){
                searchCallback();
            });

            container.find('.item-type-select').mousemove(function(){
                container.find('.item-type-list').show();
            });
            container.find('.item-type-select').mouseout(function(){
                container.find('.item-type-list').hide();
            });
            container.find('.item-type-option').click(function(){
                container.find('.item-type-text').html($(this).html());
                container.find('.item-type-text').attr("itemType", $(this).attr("itemType"));
                searchCallback();
            });



        },
        initDeleteEvent: function(trObj) {

            trObj.find(".delete-promotion").unbind().click(function() {

                var numIid = trObj.attr("numIid");
                if (confirm("确定要从促销活动中移除该宝贝？") == false) {
                    return;
                }

                var isAddItem = true;
                var container = AddPromotionCommon.init.getContainer();

                TM.PromotionCommon.result.addPageModifyPromotions(container, isAddItem);

                var successNumIidArray = [numIid];

                var isDeleteOption = true;
                AddPromotionCommon.reload.doRefreshAfterSubmit(successNumIidArray, isDeleteOption);


            });




        }
    }, AddPromotionCommon.event);



})(jQuery,window));
