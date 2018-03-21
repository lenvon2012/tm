
var TM = TM || {};

((function ($, window) {

    TM.PromotionSelectItem = TM.PromotionSelectItem || {};


    var PromotionSelectItem = TM.PromotionSelectItem;


    //初始化默认的参数
    PromotionSelectItem.init = PromotionSelectItem.init || {};
    PromotionSelectItem.init = $.extend({
        doInit: function(container, userParams) {

            if (userParams === undefined || userParams == null) {
                userParams = {};
            }

            PromotionSelectItem.container = container;

            var promotionParams = {};


            promotionParams.container = container;
            promotionParams.pagingObj = container.find(".item-paging-div");
            promotionParams.tbodyObj = container.find(".item-select-tbody");
            promotionParams.searchUrl = '/umppromotion/searchPromotionAddItems';

            promotionParams.initSearchEvent = function(searchCall) {
                TM.PromotionCommon.event.initSearchEvent(container, searchCall);
            }

            promotionParams.initAllChooseEvent = function(allChooseCall, cancelChooseCall) {

                container.find(".unchoose").hide();
                container.find('.choose').unbind().click(function(){
                    container.find(".choose").hide();
                    container.find(".unchoose").show();

                    allChooseCall();
                });
                container.find('.unchoose').unbind().click(function(){
                    container.find(".unchoose").hide();
                    container.find(".choose").show();

                    cancelChooseCall();
                })
            }

            promotionParams.ajaxDataGetCall = function() {
                var paramData = TM.PromotionCommon.params.getSimpleSearchParams(container);

                return paramData;
            }




            promotionParams.clickCallback = function() {

                var selectArray = TM.UmpSelectItem.result.getSelectNumIidArray();

                var originNum = container.find(".already-item-num").attr("originNum");

                var nowNum = parseInt(originNum) + selectArray.length;

                container.find(".already-item-num").html(nowNum);

            }

            promotionParams.isCanbeAddMore = function(isAlert) {

                var nowNum = container.find(".already-item-num").html();

                var maxPromotionNum = TM.UmpUtil.util.getMaxPromotionNum();

                if (parseInt(nowNum) >= maxPromotionNum) {

                    if (isAlert == true) {
                        alert("亲，一个活动最多可以添加" + maxPromotionNum + "个宝贝，当前您已经达到上限。")
                    }

                    return false;
                }

                return true;

            }

            userParams = $.extend({}, promotionParams, userParams);


            TM.UmpSelectItem.init.doInit(container, userParams);


            PromotionSelectItem.reload.doReload();

        },
        getContainer: function() {
            return PromotionSelectItem.container;
        }

    }, PromotionSelectItem.init);


    PromotionSelectItem.reload = PromotionSelectItem.reload || {};
    PromotionSelectItem.reload = $.extend({
        doReload: function() {

            PromotionSelectItem.reload.initExistPromotionsCount();

            //刷新宝贝
            TM.UmpSelectItem.show.doRefresh();

        },
        initExistPromotionsCount: function() {

            var container = PromotionSelectItem.init.getContainer();

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



                    var promotionCount = TM.UmpUtil.util.getAjaxResultJson(dataJson);


                    container.find(".already-item-num").attr("originNum", promotionCount);

                    var selectArray = TM.UmpSelectItem.result.getSelectNumIidArray();
                    promotionCount += selectArray.length;

                    container.find(".already-item-num").html(promotionCount);
                }
            });

        }
    }, PromotionSelectItem.reload);




})(jQuery,window));

