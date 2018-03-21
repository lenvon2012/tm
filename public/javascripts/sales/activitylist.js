var TM = TM || {};



((function ($, window) {

    /**
     * 一些针对新版打折的操作
     * @type {*}
     */
    TM.ActivityList = TM.ActivityList || {};

    var ActivityList = TM.ActivityList;


    ActivityList.event = ActivityList.event || {};
    ActivityList.event = $.extend({

        initNewActivityEvent: function(activityJson, trObj) {



            if (ActivityList.util.isOldActivity(activityJson) == true) {
                return;
            }

            if (ActivityList.util.isDiscountActivity(activityJson) || ActivityList.util.isNewDiscountActivity(activityJson)) {

                trObj.find('.activity-type-span').html('[打折]');

                trObj.find(".reviseItem").unbind().click(function() {

                    var href="/Sales/modifyPromotion?activityId=" + activityJson.id;
                    location.href = href;
                    return false;
                });
                trObj.find(".addItem").unbind().click(function(){
                    var href="/Sales/addPromotion?activityId=" + activityJson.id;
                    location.href = href;
                    return false;
                }) ;
                trObj.find(".reviseAct").unbind().click(function(){
                    var href="/Sales/modifySales?activityId=" + activityJson.id;
                    location.href = href;
                    return false;
                }) ;
                trObj.find(".restart-activity-btn").unbind().click(function() {
                    var href="/Sales/restartDiscount?activityId=" + activityJson.id;
                    location.href = href;
                    return false;
                });
            } else if (ActivityList.util.isMjsActivity(activityJson) == true) {
                trObj.find('.activity-type-span').html('[满减]');

                trObj.find(".reviseItem").unbind().click(function() {
                    var href="/Sales/mjsItemSelect?activityId=" + activityJson.id;
                    location.href = href;
                    return false;
                });
                trObj.find(".addItem").unbind().click(function(){
                    var href="/Sales/mjsItemAdd?activityId=" + activityJson.id;
                    location.href = href;
                    return false;
                }) ;
                trObj.find(".reviseAct").unbind().click(function(){
                    //修改活动
                    var href="/Sales/mjsUpdateActivity?activityId=" + activityJson.id;
                    location.href = href;
                    return false;
                }) ;
                trObj.find('.restart-activity-btn').unbind("click").click(function(){
                    // 重启活动
                    var href="/Sales/mjsRestartActivity?activityId=" + activityJson.id;
                    location.href = href;
                    return false;
                });


            } else if(ActivityList.util.isShopMjsActivity(activityJson) == true) {

                trObj.find('.activity-type-span').html('[全店满减]');

                trObj.find('.reviseItem').hide();
                trObj.find('.addItem').hide();
                trObj.find(".reviseAct").unbind().click(function(){
                    //修改活动
                    var href="/Sales/shopMjsUpdateActivity?activityId=" + activityJson.id;
                    location.href = href;
                    return false;
                });
                trObj.find('.restart-activity-btn').unbind("click").click(function(){
                    // 重启活动
                    var href="/Sales/mjsRestartShopActivity?activityId=" + activityJson.id;
                    location.href = href;
                    return false;
                });


            } else if(ActivityList.util.isShopDiscountActivity(activityJson) == true) {

                trObj.find('.activity-type-span').html('[全店打折]');

                trObj.find('.reviseItem').hide();
                trObj.find('.addItem').hide();
                trObj.find(".reviseAct").unbind().click(function(){
                    //修改活动
                    var href="/Sales/modifyShopDiscount?activityId=" + activityJson.id;
                    location.href = href;
                    return false;
                });

                trObj.find('.restart-activity-btn').unbind("click").click(function(){
                    var href="/Sales/restartShopDiscount?activityId=" + activityJson.id;
                    location.href = href;
                    return false;
                });

            } else {

                alert("系统出现异常，活动类型出错，请联系我们！");

            }



            trObj.find(".deleteAct").unbind().click(function(){
                if(confirm('确实要结束该活动吗？结束的活动，可以在已结束活动列表找到。') == false) {
                    return;
                }

                $.ajax({
                    url : '/umpactivity/cancelActivity',
                    data : {tmActivityId: activityJson.id},
                    type : 'post',
                    success : function(dataJson) {

                        if (TM.UmpUtil.util.checkIsW2AuthError(dataJson) == true) {
                            return;
                        }

                        if (TM.UmpUtil.util.judgeAjaxResult(dataJson) == false) {
                            return;
                        }

                        var resultJson = TM.UmpUtil.util.getAjaxResultJson(dataJson);


                        TM.UmpUtil.errors.showErrors($("body"), resultJson, function() {

                            //如果没有失败
                            //window.location.href ="/TaoDiscount/index";
                            //location.reload();
                            //return;
                            TM.OnActivity.show.doRefresh();

                        }, function() {


                        });


                    }
                });
            }) ;
            trObj.find(".deleteUnAct").unbind().click(function(){

                if(confirm('确实要删除该活动吗？删除后将无法恢复。') == false) {
                    return;
                }

                $.ajax({
                    url : '/umpactivity/deleteActivity',
                    data : {tmActivityId: activityJson.id},
                    type : 'post',
                    success : function(dataJson) {

                        if (TM.UmpUtil.util.judgeAjaxResult(dataJson) == false) {
                            return;
                        }
                        //location.reload();
                        /*TM.Alert.load("删除成功", 400, 300, function(){
                         $('.result-table tbody tr[item-id="'+activityJson.id+'"]').remove();
                         });*/

                        alert("活动删除成功！");
                        TM.EndActivity.show.doRefresh();

                    }
                });
            }) ;


            trObj.find(".reload-tmpl-btn").attr("title", "为活动中所有宝贝重新导入满就送模板");
            trObj.find(".reload-tmpl-btn").unbind().click(function(){

                if(confirm('确实要为活动中所有宝贝重新导入满就送模板？') == false) {
                    return;
                }

                $.ajax({
                    url : '/umpmjs/reloadMsjActivityTmpl',
                    data : {tmActivityId: activityJson.id},
                    type : 'post',
                    success : function(dataJson) {

                        if (TM.UmpUtil.util.judgeAjaxResult(dataJson) == false) {
                            return;
                        }
                        alert("系统提交成功，模板正在生成，稍后请进入宝贝详情页查看！");

                    }
                });
            }) ;


            trObj.find(".remove-tmpl-btn").attr("title", "删除活动中所有宝贝的满就送模板");

            trObj.find(".remove-tmpl-btn").unbind().click(function(){

                if(confirm('确实要删除活动中所有宝贝的满就送模板？') == false) {
                    return;
                }

                $.ajax({
                    url : '/umpmjs/removeMsjActivityTmpl',
                    data : {tmActivityId: activityJson.id},
                    type : 'post',
                    success : function(dataJson) {

                        if (TM.UmpUtil.util.judgeAjaxResult(dataJson) == false) {
                            return;
                        }
                        alert("系统提交成功，模板正在删除，稍后请进入宝贝详情页查看！");

                    }
                });
            }) ;

        }

    }, ActivityList.event);


    ActivityList.row = ActivityList.row || {};
    ActivityList.row = $.extend({
        addMoreOpBtnsDiv: function(activityJson, trObj) {

            var moreBtnDivHtml = '';

            if (ActivityList.util.isOldActivity(activityJson) == true
                || ActivityList.util.isDiscountActivity(activityJson) == true) {

                moreBtnDivHtml = '' +
                    '<div class="more-op-btns-div">' +
                    '   <a  href="javascript:void(0);" class="haoniu-base-btn reviseAct">修改活动</a>' +
                    '   <a  href="javascript:void(0);" class="haoniu-base-btn deleteAct">结束活动</a>' +
                    '</div> ' +
                    '' +
                    '';
            } else if (ActivityList.util.isMjsActivity(activityJson) == true) {
                moreBtnDivHtml = '' +
                    '<div class="more-op-btns-div">' +
                    '   <a  href="javascript:void(0);" class="haoniu-base-btn reviseAct">修改活动</a>' +
                    '   <a  href="javascript:void(0);" class="haoniu-base-btn deleteAct">结束活动</a>' +
                    '   <div style="padding-top: 10px;">' +
                    '       <a  href="javascript:void(0);" class="haoniu-base-btn reload-tmpl-btn">重导模板</a>' +
                    '       <a  href="javascript:void(0);" class="haoniu-base-btn remove-tmpl-btn">删除模板</a>' +
                    '   </div>' +
                    '</div> ' +
                    '' +
                    '';
            } else if (ActivityList.util.isShopMjsActivity(activityJson) == true) {
                moreBtnDivHtml = '' +
                    '<div class="more-op-btns-div">' +
                    '   <a  href="javascript:void(0);" class="haoniu-base-btn reload-tmpl-btn">重导模板</a>' +
                    '   <a  href="javascript:void(0);" class="haoniu-base-btn remove-tmpl-btn">删除模板</a>' +
                    '</div> ' +
                    '' +
                    '';
            } else {
                return;
            }



            var moreBtnDivObj = $(moreBtnDivHtml);


            trObj.find(".activity-more-op-btn").append(moreBtnDivObj);


            trObj.find(".activity-more-op-btn").mouseover(function() {

                //计算位置
                var left = $(this).width() + $(this).offset().left - moreBtnDivObj.width();
                var top = $(this).height() + $(this).offset().top-107;


                moreBtnDivObj.css("top", top + "px");
                moreBtnDivObj.css("left", left + 'px');

                moreBtnDivObj.show();
            });
            trObj.find(".activity-more-op-btn").mouseout(function() {
                moreBtnDivObj.hide();
            });

            moreBtnDivObj.mouseout(function() {
                moreBtnDivObj.show();
            });

            moreBtnDivObj.mouseover(function() {
                moreBtnDivObj.hide();
            });
        },
        newAddMoreOpBtnsDiv: function(activityJson, trObj) {

            var moreBtnDivHtml = '';

            if (ActivityList.util.isMjsActivity(activityJson) == true) {
                moreBtnDivHtml = '' +
                    '<div class="new-more-op-btns-div">' +
                    '   <a  href="javascript:void(0);" class="more-base-btn deleteAct">结束活动</a>' +
                    '   <a  href="javascript:void(0);" class="more-base-btn reload-tmpl-btn">重导模板</a>' +
                    '   <a  href="javascript:void(0);" class="more-base-btn remove-tmpl-btn">删除模板</a>' +
                    '</div> ' +
                    '' +
                    '';
            } else {
                return;
            }



            var moreBtnDivObj = $(moreBtnDivHtml);


            trObj.find(".activity-more-op-btn").append(moreBtnDivObj);


            trObj.find(".activity-more-op-btn").mouseover(function() {

                //计算位置
                var left = $(this).width() + $(this).offset().left - moreBtnDivObj.width() + 1;
                var top = $(this).height() + $(this).offset().top-107;

                moreBtnDivObj.css("top", top + "px");
                moreBtnDivObj.css("left", left + 'px');

                moreBtnDivObj.show();
            });
            trObj.find(".activity-more-op-btn").mouseout(function() {
                moreBtnDivObj.hide();
            });

            moreBtnDivObj.mouseout(function() {
                moreBtnDivObj.show();
            });

            moreBtnDivObj.mouseover(function() {
                moreBtnDivObj.hide();
            });
        }
    }, ActivityList.row);


    ActivityList.util = ActivityList.util || {};
    ActivityList.util = $.extend({

        isOldActivity: function(activityJson) {

            if (activityJson.activityType <= 0) {
                return true;
            } else {
                return false;
            }
        },
        //包邮打折
        isDiscountActivity: function(activityJson) {
            if (activityJson.activityType == 1) {
                return true;
            } else {
                return false;
            }
        },
        //满就送
        isMjsActivity: function(activityJson) {
            if (activityJson.activityType == 2) {
                return true;
            } else {
                return false;
            }
        },
        //全店满就送
        isShopMjsActivity: function(activityJson) {
            if (activityJson.activityType == 4) {
                return true;
            } else {
                return false;
            }
        },
        //全店打折
        isShopDiscountActivity: function(activityJson) {
            if (activityJson.activityType == 8) {
                return true;
            } else {
                return false;
            }
        },
        //包邮打折
        isNewDiscountActivity: function(activityJson) {
            if (activityJson.activityType == 16) {
                return true;
            } else {
                return false;
            }
        },

    }, ActivityList.util);


})(jQuery,window));

