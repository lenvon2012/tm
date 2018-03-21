
var TM = TM || {};

((function ($, window) {

    TM.AddPromotion = TM.AddPromotion || {};

    var AddPromotion = TM.AddPromotion || {};

    AddPromotion.init = AddPromotion.init || {};
    AddPromotion.init = $.extend({

        doInit: function(container) {

            AddPromotion.container = container;

            var isAddItem = true;
            var isRestartActivity = false;

            TM.PromotionCommon.init.doInit(isAddItem, isRestartActivity);

            var paramData = {};
            paramData.tmActivityId = TM.UmpUtil.util.getTMActivityId(container);
            paramData.tms = TM._tms;

            $.ajax({
                url : TM.serverPath+'/PhoneItem/findDazheActivity',
                data : paramData,
                type : 'post',
                datatype: 'JSONP',
                success : function(dataJson) {
                    AddPromotion.init.doOtherInit();
                },
                error:function(){console.log('error')}
            });


        },
        doOtherInit: function() {

            var container = AddPromotion.init.getContainer();

            //选宝贝部分的初始化，包括事件
            TM.PromotionSelectItem.init.doInit(container.find(".select-item-container"), {});

            //设置折扣部分的初始化，包括事件
            TM.NewDiscount.init.doInit(container.find(".set-discount-container"));

            //上一步，下一步按钮事件
            TM.AddPromotionCommon.event.initNextStepEvent(container);
            TM.AddPromotionCommon.event.initPrevStepEvent(container);

        },
        getContainer: function() {
            return AddPromotion.container;
        }

    }, AddPromotion.init);









})(jQuery,window));






((function ($, window) {

    TM.NewDiscount = TM.NewDiscount || {};

    var NewDiscount = TM.NewDiscount;

    NewDiscount.init = NewDiscount.init || {};
    NewDiscount.init = $.extend({

        doInit: function(container) {

            NewDiscount.container = container;


            var isRestartActivity = false;
            TM.AddPromotionCommon.init.doInit(container, isRestartActivity);

            var isAddItem = true;

            TM.PromotionCommon.event.initBatchDiscountEvent(container, isAddItem);

            TM.AddPromotionCommon.event.initSearchEvent();


            NewDiscount.event.initSubmitEvent();

        },
        getContainer: function() {
            return NewDiscount.container;
        }

    }, NewDiscount.init);




    NewDiscount.event = NewDiscount.event || {};
    NewDiscount.event = $.extend({

        initSubmitEvent: function() {

            var container = NewDiscount.init.getContainer();


            container.find(".submit-discount-btn").unbind().click(function() {

                var isAddItem = true;

                var errorSetCallback = function() {

                    container.find(".search-text-input").val("");

                    container.find(".item-type-select .error-activity-item-option").click();

                }

                var paramData = TM.PromotionCommon.params.getSubmitParams(container, errorSetCallback, isAddItem);

                if (paramData === undefined || paramData == null) {
                    return;
                }
                var minDiscount = paramData.minDiscount / 100;
                if (confirm("您当前设置了" + paramData.itemNum + "个宝贝的折扣，其中最低折扣为" + minDiscount
                    + "折，确定要提交到淘宝？") == false) {
                    return;
                }



                $.ajax({
//                    url : 'http://localhost:9999/PhoneItem/submitAddPromotions',
                    url : TM.serverPath+'/PhoneItem/submitAddPromotions'+TM._tms,
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
//                            window.location.href ="/Sales/index";
                            return;

                        }, function() {

                            //如果有宝贝失败，要刷新列表，同时要取消全局变量SelectItems.....
                            //或者 保留SelectItems，但判断已经promotion的宝贝，就去掉select标志。。。

                            var successNumIidArray = resultJson.successNumIidSet;

                            var isDeleteOption = false;
                            TM.AddPromotionCommon.reload.doRefreshAfterSubmit(successNumIidArray, isDeleteOption);
                        });
                    }
                });

            });
        }
    }, NewDiscount.event);



})(jQuery,window));


