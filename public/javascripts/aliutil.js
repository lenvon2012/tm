
var Ali = Ali || {};

((function ($, window) {
    //因为会多次加载js，清空之前的数据
    Ali.AliUtil = {};

    //加入公会联系的QQ号码
    Ali.kefuQQ=3046673473;

    Ali.AliUtil = Ali.AliUtil || {};
    var AliUtil = Ali.AliUtil;

    AliUtil.util = AliUtil.util || {};
    AliUtil.util = $.extend({
        //是否是尚未登录的状态
        judgeIsNeedWarnLogin: function(dataJson) {
            var needWarnLogin = dataJson.needWarnLogin;

            if (needWarnLogin === undefined || needWarnLogin == false) {
                return false;
            }

            return needWarnLogin;

        },
        judgeAjaxResult: function(dataJson, isShowMessage, goLoginPage) {

            if (goLoginPage === undefined || goLoginPage == false || goLoginPage != true) {
                goLoginPage = false;
            }


            if (isShowMessage == false) {
                goLoginPage = true;
            } else {
                var message = dataJson.msg;

                if (message === undefined || message == null || message == "") {
                    message = dataJson.message;
                }

                if (message === undefined || message == null || message == "") {

                } else {
                    AliUtil.util.alert(message);
                }
            }

            if (AliUtil.util.judgeIsNeedWarnLogin(dataJson) == true) {
                if (goLoginPage == true) {
                    AliUtil.redir.gotoLoginPage();
                }

                return false;
            }

            return AliUtil.util.getAjaxSuccess(dataJson);

        },
        getAjaxSuccess: function(dataJson) {
            var isSuccess = dataJson.isOk;
            if (isSuccess === undefined) {
                isSuccess = dataJson.success;
            }

            return isSuccess;
        },
        checkLoginWithCallback: function(isShowMessage, callback, goLoginPage) {
            $.ajax({
                url : "/ALUserOp/checkIsUserLogin",
                data : {},
                type : 'post',
                success : function(dataJson) {

                    if (Ali.AliUtil.util.judgeAjaxResult(dataJson, isShowMessage, goLoginPage) == false) {

                        return;
                    }

                    callback && callback();
                }
            });
        },
        getAjaxResultJson: function(dataJson) {

            var resultJson = dataJson.res;

            if (resultJson === undefined) {
                resultJson = dataJson.result;
            }

            return resultJson;
        },
        alert: function(message) {
            //Ali.Alert.showDialog(message, 400, 300, function() {}, function() {}, '提示');
            alert(message);
        },
        confirmCall: function(message, callback) {
            Ali.Alert.showDialog(message, 350, 250, callback, function() {}, '提示');
        },
        setSortTdEvent: function(container, callback) {
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
                callback();
            });

        },
        addSortParams: function(container, paramData) {

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
        },
        checkIsFromWeb: function() {
            var isFromWeb = Ali.isFromWeb;
            if (isFromWeb == true) {
                return true;
            } else {
                return false;
            }
        },
        isUseBalance: function() {
            return false;
        },
        canMissionComplain: function(missionJson, isFromBuyer) {

            return AliUtil.MissionStatusUtil.canMissionComplain(missionJson, isFromBuyer);
        },
        isCanComplain: function() {
            return true;
        },
        getWhiteWangwangLimitCredit: function() {
            return 1000000001;//白号单
        },
        isWhiteWangwangMission: function(missionJson) {
            if (missionJson.requiredMaxLevel >= Ali.AliUtil.util.getWhiteWangwangLimitCredit()) {
                return true;
            } else {
                return false;
            }
        },
        setChattingTypeIfNeed: function(missionJson) {
            if (missionJson.needChatting != true) {
                return;
            }
            if (missionJson.chattingType > 0) {
                return;
            }
            missionJson.chattingType = 1;
            return;
        },
        setViewOtherMinuteIfNeed: function(missionJson) {
            if (missionJson.viewOtherNum > 0) {
                if (missionJson.viewOtherMinute > 0) {
                    return;
                }
                missionJson.viewOtherMinute = 2;
                return;
            }

        },
        isUndefinedOrNull:function(){
            var curEle=$(this);
            return this.isUndefined()||this.isNull();
        }
    }, AliUtil.util);


    AliUtil.MissionStatusUtil = AliUtil.MissionStatusUtil || {};
    AliUtil.MissionStatusUtil = $.extend({
        canDeleteMission: function(missionJson, isFromBuyer) {

            if (isFromBuyer == false) {
                return missionJson.canDeleteMission;
            } else {
                return false;
            }
        },
        canMissionComplain: function(missionJson, isFromBuyer) {

            if (isFromBuyer == false) {
                return missionJson.canSellerComplain;
            } else {
                return missionJson.canBuyerComplain;
            }
        },
        canRemoveComplain: function(missionJson, isFromBuyer) {

            if (isFromBuyer == false) {
                return missionJson.canRemoveSellerComplain;
            } else {
                return missionJson.canRemoveBuyerComplain;
            }
        },
        canMissionSuspend: function(missionJson, isFromBuyer) {
            if (isFromBuyer == false) {
                return missionJson.canSuspend;
            } else {
                return false;
            }

        },
        canRemoveSuspend: function(missionJson, isFromBuyer) {
            if (isFromBuyer == false) {
                return missionJson.canRemoveSuspend;
            } else {
                return false;
            }
        },
        canMissionComment: function(missionJson, isFromBuyer) {
            if (isFromBuyer == false) {
                return missionJson.canSellerComment;
            } else {
                return missionJson.canBuyerComment;
            }

        },
        canCancelAcceptMission: function(missionJson, isFromBuyer) {
            if (isFromBuyer == false) {
                return missionJson.canSellerCancelMission;
            } else {
                return missionJson.canBuyerCancelMission;
            }
        },
        canCheckMission: function(missionJson, isFromBuyer) {
            if (isFromBuyer == false) {
                return missionJson.canSellerCheckMission;
            } else {
                return missionJson.canBuyerCheckMission;
            }
        }

    }, AliUtil.MissionStatusUtil);



    AliUtil.redir = AliUtil.redir || {};
    AliUtil.redir = $.extend({

        gotoFirstPage: function() {
            location.href = '/webmission/index';
        },
        gotoLoginPage: function() {
            location.href = '/ALLogin/index';
        },
        afterLoginSuccess: function(redirectURL) {
            AliUtil.redir.gotoTargetUrl(redirectURL);

        },
        gotoTargetUrl: function(redirectURL) {
            if(redirectURL === undefined || redirectURL == null || redirectURL == '') {
                location.href = '/';
            } else {
                location.href = redirectURL;
            }
        },
        gotoWaitingMissionTab: function() {
            location.href = '/missionhall/waitingMissions';
        },
        gotoMyAcceptTab: function() {
            location.href = '/missionhall/myAcceptMissions';
        },
        gotoMyPublishTab: function() {
            location.href = '/missionhall/myPublishMissionsNew';
        },
        gotoPublishMissionTab: function(missionId) {
            var link = '/missionhall/publishMissionNew2';
            if (missionId === undefined || missionId == null || missionId <= 0) {

            } else {
                link += "?missionId=" + missionId;
            }
            location.href = link;
        },
        gotoAcceptMissonDetail: function(missionId) {
            location.href = '/missionhall/acceptMissionDetail?missionId=' + missionId;
        },
        getSelectContainer: function() {

            if (AliUtil.util.checkIsFromWeb() == true) {
                return $("body");
            } else {
                var container = FrameWorkTabs.init.getContainer();
                var selectTabObj = container.find(".tab-btn-ul .tab-btn.select");

                var targetDivCss = selectTabObj.attr("targetDiv");
                var targetDivObj = container.find("." + targetDivCss);

                return targetDivObj;
            }


        }
    }, AliUtil.redir);


    AliUtil.missionDetail = AliUtil.missionDetail || {};
    AliUtil.missionDetail = $.extend({
        showWaitingDetail: function(container, parentMissionId) {
            location.href = '/missionhall/waitingMissionDetail?parentMissionId=' + parentMissionId;
        },
        showAcceptDetail: function(container, missionId) {
            location.href = '/missionhall/acceptMissionDetail?missionId=' + missionId;
        },
        showPublishDetail: function(container, missionId) {
            location.href = '/missionhall/publishMissionDetail?missionId=' + missionId;
        }
    }, AliUtil.missionDetail);


})(jQuery,window));