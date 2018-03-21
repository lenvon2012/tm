
var TM = TM || {};

((function ($, window) {

    TM.ModifyPromotion = TM.ModifyPromotion || {};

    var ModifyPromotion = TM.ModifyPromotion || {};

    ModifyPromotion.init = ModifyPromotion.init || {};
    ModifyPromotion.init = $.extend({

        doInit: function(container) {


            ModifyPromotion.container = container;

            var isRestartActivity = false;

            TM.PromotionCommon.init.doInit(ModifyPromotion.init.getIsAddItem(), isRestartActivity);

            var paramData = {};
            paramData.tmActivityId = TM.UmpUtil.util.getTMActivityId(container);

            $.ajax({
                url : '/umppromotion/findDazheActivity',
                data : paramData,
                type : 'post',
                success : function(dataJson) {


                    if (TM.UmpUtil.util.judgeAjaxResult(dataJson) == false) {

                        location.href = "/Sales/index";

                        return;
                    }

                    var activityJson = TM.UmpUtil.util.getAjaxResultJson(dataJson);


                    container.find(".modify-item-header").html("修改促销商品 &nbsp;(当前活动：" + activityJson.activityDescription + ")");

                    ModifyPromotion.init.doOtherInit();

                }
            });


        },
        doOtherInit: function() {

            var container = ModifyPromotion.init.getContainer();


            TM.PromotionCommon.event.initBatchDiscountEvent(container, ModifyPromotion.init.getIsAddItem());

            ModifyPromotion.event.initSearchEvent();

            ModifyPromotion.event.initSubmitEvent();


            ModifyPromotion.reload.doReload();

        },
        getContainer: function() {
            return ModifyPromotion.container;
        },
        getIsAddItem: function() {
            return false;
        }

    }, ModifyPromotion.init);


    ModifyPromotion.reload = ModifyPromotion.reload || {};
    ModifyPromotion.reload = $.extend({
        doReload: function() {
            ModifyPromotion.reload.initActivityPromotionCount();

            //先清空页面，然后再刷新宝贝
            ModifyPromotion.show.doClearPageAndRefresh();
        },
        //删除之后，提交之后，都要调用
        doRefreshAfterSubmit: function(successNumIidArray) {
            if (successNumIidArray === undefined || successNumIidArray == null || successNumIidArray.length <= 0) {
                successNumIidArray = [];
            }


            //
            TM.PromotionCommon.result.removeSomeItemsFromModifyArray(successNumIidArray);


            //刷新item-type-select列表
            ModifyPromotion.reload.initActivityPromotionCount();

            //先清空页面，然后再刷新宝贝
            ModifyPromotion.show.doClearPageAndRefresh();
        },
        initActivityPromotionCount: function() {
            var container = ModifyPromotion.init.getContainer();



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

                    var totalPromotionNum = TM.UmpUtil.util.getAjaxResultJson(dataJson);

                    var itemTypeSelectObj = container.find(".item-type-select");


                    var currentSelectType = itemTypeSelectObj.find(".item-type-text").attr("itemType");

                    if (currentSelectType == 1) {
                        itemTypeSelectObj.find(".item-type-text").html('活动中所有宝贝(' + totalPromotionNum + '个)');
                    } else if (currentSelectType == 8) {
                        itemTypeSelectObj.find(".item-type-text").html('参数错误的宝贝');
                    }

                    itemTypeSelectObj.find('.all-activity-item-option').html('活动中所有宝贝(' + totalPromotionNum + '个)');
                    itemTypeSelectObj.find('.error-activity-item-option').html('参数错误的宝贝');
                }
            });
        }
    }, ModifyPromotion.reload);


    ModifyPromotion.show = ModifyPromotion.show || {};
    ModifyPromotion.show = $.extend({
        targetCurrentPage: 1,
        doShow: function() {
            var container = ModifyPromotion.init.getContainer();
            TM.PromotionCommon.result.addPageModifyPromotions(container, ModifyPromotion.init.getIsAddItem());
            ModifyPromotion.show.doSearchWithPage(1, false);
        },
        doClearPageAndRefresh: function() {
            var container = ModifyPromotion.init.getContainer();
            //var tbodyObj = container.find(".promotion-table");

            //tbodyObj.html("");

            ModifyPromotion.show.doSearchWithPage(ModifyPromotion.show.targetCurrentPage, false);
        },
        doSearchWithPage: function(currentPage, isAddPageModifyPromotions) {

            if (currentPage < 1) {
                currentPage = 1;
            }

            ModifyPromotion.show.targetCurrentPage = currentPage;

            var container = ModifyPromotion.init.getContainer();
            var tbodyObj = container.find(".promotion-table");


            var paramData = ModifyPromotion.show.getSearchParams();

            if (paramData == null) {
                return;
            }



            container.find(".paging-div").tmpage({
                currPage: currentPage,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: paramData,
                    dataType: 'json',
                    url: '/umppromotion/findExistPromotions',
                    callback: function(dataJson){

                        if (isAddPageModifyPromotions == true) {
                            TM.PromotionCommon.result.addPageModifyPromotions(container, ModifyPromotion.init.getIsAddItem());
                        } else {
                            //只是第一次不获取页面数据，下一次点击下一页的时候，还是要获取的
                            isAddPageModifyPromotions = true;
                        }

                        ModifyPromotion.show.targetCurrentPage = dataJson.pn;

                        tbodyObj.html("");

                        var itemJsonArray = dataJson.res;

                        //如果没有数据
                        if(itemJsonArray===undefined||itemJsonArray===null||itemJsonArray.length<=0){
                                  var nullHtml='<div class="null-html">暂时没有符合要求的数据</div>';
                                  tbodyObj.html(nullHtml);
                                  return;
                        }


                        $(itemJsonArray).each(function(index, itemJson) {
                            var trObj = TM.PromotionCommon.row.createCommonRow(index, itemJson, ModifyPromotion.init.getIsAddItem());

                            ModifyPromotion.event.initDeleteEvent(trObj);

                            tbodyObj.append(trObj);
                        });

                    }
                }

            });

        },
        getSearchParams: function() {


            var container = ModifyPromotion.init.getContainer();

            var paramData = TM.PromotionCommon.params.getSimpleSearchParams(container);

            if (paramData == null) {
                return null;
            }


            paramData.itemType = container.find(".item-type-text").attr("itemType");

            if (paramData.itemType == 8) {

                var errorNumIidArray = TM.PromotionCommon.result.getErrorModifyNumIids(false);


                paramData.targetNumIids = errorNumIidArray.join(',');
            }



            return paramData;
        }
    }, ModifyPromotion.show);


    ModifyPromotion.event = ModifyPromotion.event || {};
    ModifyPromotion.event = $.extend({
        initSearchEvent: function() {

            var container = ModifyPromotion.init.getContainer();

            var searchCall = function() {
                ModifyPromotion.show.doShow();
            }

            TM.PromotionCommon.event.initSearchEvent(container, searchCall);


            container.find('.item-type-select').mousemove(function(){
                container.find('.item-type-list').show();
            });
            container.find('.item-type-select').mouseout(function(){
                container.find('.item-type-list').hide();
            });
            container.find('.item-type-option').click(function(){
                container.find('.item-type-text').html($(this).html());
                container.find('.item-type-text').attr("itemType", $(this).attr("itemType"));
                searchCall();
            });

        },
        initDeleteEvent: function(trObj) {
            trObj.find(".delete-promotion").unbind().click(function(){
                if (confirm("确定要从活动中删除该宝贝？" ) == false) {
                    return;
                }

                var container = ModifyPromotion.init.getContainer();

                var activityId = TM.UmpUtil.util.getTMActivityId(container);

                var targetNumIid = trObj.attr("numIid");
                var numIids = targetNumIid + "";

                $.ajax({
                    url : '/umppromotion/deletePromotions',
                    data : {tmActivityId: activityId, numIids: numIids},
                    type : 'post',
                    success : function(dataJson) {

                        if (TM.UmpUtil.util.checkIsW2AuthError(dataJson) == true) {
                            return;
                        }

                        if (TM.UmpUtil.util.judgeAjaxResult(dataJson) == false) {
                            return;
                        }

                        var resultJson = TM.UmpUtil.util.getAjaxResultJson(dataJson);

                        //删除成功后一定要刷新页面，不然如果之前是选中这个宝贝的。。。
                        //如果不是刷新页面的话，那么在刷新列表的时候，如果有些宝贝折扣设置出错，doShow那里是不会执行下去的。。。

                        TM.UmpUtil.errors.showErrors(container, resultJson, function() {

                            //如果没有失败，那么就要刷新列表，刷新的时候，还要不能影响其他宝贝
                            //在刷新之前，要把当前页选中的宝贝记录下来

                            TM.PromotionCommon.result.addPageModifyPromotions(container, ModifyPromotion.init.getIsAddItem());

                            var successNumIidArray = [targetNumIid];

                            ModifyPromotion.reload.doRefreshAfterSubmit(successNumIidArray);



                        }, function() {

                            //如果失败了，不变
                            //清空，刷新列表。。
                            //ModifyPromotion.show.doRefreshAndClearSelectInfo();
                            /*
                             TM.PromotionCommon.result.addPageModifyPromotions(container, ModifyPromotion.init.getIsAddItem());

                             var successNumIidArray = [targetNumIid];

                             ModifyPromotion.reload.doRefreshAfterSubmit(successNumIidArray);*/
                        });


                    }
                });
            });

        },
        initSubmitEvent: function() {

            var container = ModifyPromotion.init.getContainer();

            container.find(".StepBtn2").unbind().click(function() {

                var errorSetCallback = function() {

                    TM.PromotionCommon.params.clearSearchRules(container);

                    container.find(".item-type-select .error-activity-item-option").click();

                }

                var paramData = TM.PromotionCommon.params.getSubmitParams(container, errorSetCallback,
                    ModifyPromotion.init.getIsAddItem());

                if (paramData === undefined || paramData == null) {
                    return;
                }

                var minDiscount = paramData.minDiscount / 100;
                if (confirm("您当前设置了" + paramData.itemNum + "个宝贝的折扣，其中最低折扣为" + minDiscount
                    + "折，确定要提交到淘宝？") == false) {
                    return;
                }

                /*if (confirm("您当前修改了" + paramData.itemNum + "个宝贝的折扣，确定要提交到淘宝？") == false) {
                 return;
                 }*/

                $.ajax({
                    url : '/umppromotion/submitUpdatePromotions',
                    data : paramData,
                    type : 'post',
                    success : function(dataJson) {

                        if (TM.UmpUtil.util.checkIsW2AuthError(dataJson) == true) {
                            return;
                        }

                        if (TM.UmpUtil.util.judgeAjaxResult(dataJson) == false) {
                            return;
                        }



                        var resultJson = TM.UmpUtil.util.getAjaxResultJson(dataJson);


                        TM.UmpUtil.errors.showErrors(container, resultJson, function() {

                            //如果没有失败
                            window.location.href ="/Sales/index";
                            return;

                        }, function() {

                            //如果有宝贝失败，要刷新列表，同时要取消全局变量SelectItems.....
                            //或者 保留SelectItems，但判断已经promotion的宝贝，就去掉select标志。。。

                            var successNumIidArray = resultJson.successNumIidSet;

                            ModifyPromotion.reload.doRefreshAfterSubmit(successNumIidArray);
                        });
                    }
                });

            });
        }
    }, ModifyPromotion.event);




})(jQuery,window));
