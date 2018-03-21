var TM = TM || {};

((function ($, window) {
    TM.AccountAdmin = TM.AccountAdmin || {};

    var AccountAdmin = TM.AccountAdmin;

    AccountAdmin.init = AccountAdmin.init || {};
    AccountAdmin.init = $.extend({
        doInit: function(container) {
            AccountAdmin.init.container = container;


            AccountAdmin.show.doShow(container.find(".bind-account-table"), 1);

            TM.WeiboList.init.doInit(container.find(".weibo-list-div"), 1);

        },
        getContainer: function() {
            return AccountAdmin.init.container;
        }
    }, AccountAdmin.init);


    AccountAdmin.show = AccountAdmin.show || {};
    AccountAdmin.show = $.extend({
        doShow: function(accountsObj, accountType) {

            accountsObj.find("tbody.bind-account-tbody").children("tr").hide();

            var paramData = {};
            paramData.accountType = accountType;

            $.ajax({
                url : '/AccountAdmin/queryAccountsByType',
                data : paramData,
                type : 'post',
                success: function (dataJson) {
                    if (TM.DptgBase.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    var accountArray = dataJson.res;
                    if (accountArray === undefined || accountArray == null) {
                        accountArray = [];
                    }
                    //没有绑定任何帐号
                    if (AccountAdmin.show.isNoBindAccounts(accountArray) == true) {
                        AccountAdmin.account.showNoBindTr(accountsObj, accountType);
                        return;
                    }
                    var mainAccount = AccountAdmin.show.getMainAccount(accountArray);
                    //大号没有绑定
                    if (mainAccount == null || mainAccount.binding == false) {
                        AccountAdmin.account.showMainNoBindTr(accountsObj, accountType);
                    } else {
                        AccountAdmin.account.showMainBindTr(accountsObj, mainAccount, accountType);
                    }

                    var slaveAccount = AccountAdmin.show.getSlaveAccount(accountArray);
                    //小号没有绑定
                    if (slaveAccount == null || slaveAccount.binding == false) {
                        AccountAdmin.account.showSlaveNoBindTr(accountsObj, accountType);
                    } else {
                        AccountAdmin.account.showSlaveBindTr(accountsObj, slaveAccount, accountType);
                    }
                }
            });
        },
        isNoBindAccounts: function(accountArray) {
            if (accountArray === undefined || accountArray == null || accountArray.length <= 0) {
                return true;
            }

            for (var i = 0; i < accountArray.length; i++) {
                if (accountArray[i].binding == true) {
                    return false;
                }
            }

            return true;

        },
        getMainAccount: function(accountArray) {
            if (accountArray === undefined || accountArray == null || accountArray.length <= 0) {
                return null;
            }
            for (var i = 0; i < accountArray.length; i++) {
                if (accountArray[i].mainAccount == true) {
                    return accountArray[i];
                }

            }
            return null;
        },
        getSlaveAccount: function(accountArray) {
            if (accountArray === undefined || accountArray == null || accountArray.length <= 0) {
                return null;
            }
            for (var i = 0; i < accountArray.length; i++) {
                if (accountArray[i].mainAccount == false) {
                    return accountArray[i];
                }
            }
            return null;
        }
    }, AccountAdmin.show);


    AccountAdmin.account = AccountAdmin.account || {};
    AccountAdmin.account = $.extend({
        showNoBindTr: function(accountsObj, accountType) {
            accountsObj.find(".no-binding-tr").show();

            var bindBtnObj = accountsObj.find(".no-binding-tr .binding-all-btn");

            bindBtnObj.unbind().click(function() {
                var allFunction = 4;
                AccountAdmin.account.doBindAccount(accountType, allFunction);
            });

        },
        showMainNoBindTr: function(accountsObj, accountType) {
            var trObj = accountsObj.find(".main-no-account-tr");
            trObj.show();
            trObj.find(".binding-main-account-btn").unbind().click(function() {
                var func = 1;
                AccountAdmin.account.doBindAccount(accountType, func);
            });

        },
        showMainBindTr: function(accountsObj, mainAccount, accountType) {
            var trObj = accountsObj.find(".main-account-tr");
            trObj.show();

            trObj.find(".account-img").attr("src", mainAccount.headImgUrl);
            trObj.find(".account-main-page").attr("href", "http://weibo.com/u/" + mainAccount.accountId + "/home");

            trObj.find(".account-name").html(mainAccount.aliasName);
            trObj.find(".fans-num").html(mainAccount.fansNum);
            var contribution = mainAccount.contribution;
            contribution = Math.round(contribution * 10);
            contribution = contribution / 10;
            trObj.find(".contribution-span").html(contribution);
            trObj.find(".new-fans-num").html(mainAccount.addFansNum);
            trObj.find(".new-forward-num").html(mainAccount.newForwardNum);

            trObj.find(".main-expire-span").html(mainAccount.expireTimeStr);
            if (mainAccount.outOfDate == true) {
                trObj.find(".re-bind-div").show();
                trObj.find(".re-bind-btn").unbind().click(function() {
                    var func = 1;
                    if (confirm("确定要对帐号重新授权？") == false) {
                        return;
                    }
                    AccountAdmin.account.doBindAccount(accountType, func);
                });
            }


            trObj.find(".change-bind-btn").unbind().click(function() {
                var func = 1;
                if (confirm("确定要切换帐号绑定？") == false) {
                    return;
                }
                AccountAdmin.account.doBindAccount(accountType, func);
            });
            trObj.find(".cancel-bind-btn").unbind().click(function() {
                AccountAdmin.account.unBindAccount(mainAccount.id, accountType);
            });
        },
        showSlaveNoBindTr: function(accountsObj, accountType) {
            var trObj = accountsObj.find(".slave-no-account-tr");
            trObj.show();
            trObj.find(".binding-slave-account-btn").unbind().click(function() {
                var func = 2;
                AccountAdmin.account.doBindAccount(accountType, func);
            });
        },
        showSlaveBindTr: function(accountsObj, slaveAccount, accountType) {
            var trObj = accountsObj.find(".slave-account-tr");
            trObj.show();

            trObj.find(".account-img").attr("src", slaveAccount.headImgUrl);
            trObj.find(".account-main-page").attr("href", "http://weibo.com/u/" + slaveAccount.accountId + "/home");

            trObj.find(".account-name").html(slaveAccount.aliasName);
            trObj.find(".attention-num").html(slaveAccount.attentionNum);
            //trObj.find(".today-attention-num").html(0);
            trObj.find(".new-attention-num").html(slaveAccount.newAttentionNum);
            trObj.find(".new-forward-num").html(slaveAccount.newForwardNum);


            trObj.find(".slave-expire-span").html(slaveAccount.expireTimeStr);
            if (slaveAccount.outOfDate == true) {
                trObj.find(".re-bind-div").show();
                trObj.find(".re-bind-btn").unbind().click(function() {
                    var func = 2;
                    if (confirm("确定要对帐号重新授权？") == false) {
                        return;
                    }
                    AccountAdmin.account.doBindAccount(accountType, func);
                });
            }


            trObj.find(".change-bind-btn").unbind().click(function() {
                var func = 2;
                if (confirm("确定要切换帐号绑定？") == false) {
                    return;
                }
                AccountAdmin.account.doBindAccount(accountType, func);
            });
            trObj.find(".cancel-bind-btn").unbind().click(function() {
                AccountAdmin.account.unBindAccount(slaveAccount.id, accountType);
            });

            AccountAdmin.account.showAttentionInfo(accountsObj, accountType);
        },
        showAttentionInfo: function(accountsObj, accountType) {
            var paramData = {};
            paramData.accountType = accountType;

            $.ajax({
                url : '/AccountAdmin/queryTodayAttention',
                data : paramData,
                type : 'post',
                success: function (dataJson) {
                    if (TM.DptgBase.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    var attentionJson = dataJson.res;
                    accountsObj.find(".today-attention-num").html(attentionJson.attentionNum);
                    accountsObj.find(".today-forward-num").html(attentionJson.forwardNum);
                }
            });
        },
        doBindAccount: function(accountType, func) {

            var paramData = {};
            paramData.accountType = accountType;
            paramData.function = func;

            $.ajax({
                url : '/AccountAdmin/getBindAccountUrl',
                data : paramData,
                type : 'post',
                success: function (dataJson) {
                    if (TM.DptgBase.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    //var container = AccountAdmin.init.getContainer();
                    //container.html("");
                    $(document).unbind("ajaxStop");

                    var bindUrl = dataJson.res;
                    location.href = bindUrl;
                }
            });
        },
        unBindAccount: function(accountId, accountType) {
            if (confirm("确定要取消帐号绑定？") == false) {
                return;
            }
            var paramData = {};
            paramData.accountId = accountId;
            paramData.accountType = accountType;

            $.ajax({
                url : '/AccountAdmin/unBindAccount',
                data : paramData,
                type : 'post',
                success: function (dataJson) {
                    if (TM.DptgBase.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    location.reload();
                }
            });
        }

    }, AccountAdmin.account);





})(jQuery,window));





((function ($, window) {

    TM.WeiboList = TM.WeiboList || {};

    var WeiboList = TM.WeiboList;

    WeiboList.init = WeiboList.init || {};
    WeiboList.init = $.extend({
        doInit: function(container, accountType) {
            WeiboList.init.container = container;

            WeiboList.show.doShow(accountType);

            container.find(".show-more-btn").click(function() {
                var btnObj = $(this);
                var offset = btnObj.attr("offset");
                WeiboList.show.doAppendWeibos(accountType, offset);
            });
        },
        getContainer: function() {
            return WeiboList.init.container;
        }
    }, WeiboList.init);


    WeiboList.show = WeiboList.show || {};
    WeiboList.show = $.extend({
        doShow: function(accountType) {
            var container = WeiboList.init.getContainer();
            var tbodyObj = container.find("tbody.weibo-list-tbody");
            tbodyObj.html("");

            WeiboList.show.doAppendWeibos(accountType, 0);
        },
        doRefresh: function(accountType) {
            //WeiboList.show.doShow(accountType);

            location.reload();
        },
        doAppendWeibos: function(accountType, offset) {
            var container = WeiboList.init.getContainer();
            var tbodyObj = container.find(".weibo-list-tbody");

            var paramData = {};
            paramData.accountType = accountType;
            paramData.offset = offset;

            $.ajax({
                url : '/AccountAdmin/queryWeiboList',
                data : paramData,
                type : 'post',
                success: function (dataJson) {
                    if (TM.DptgBase.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }

                    var resultJson = dataJson.res;

                    var offset = resultJson.offset;
                    var weiboJsonList = resultJson.weiboList;
                    container.find(".show-more-btn").attr("offset", offset);

                    if (weiboJsonList.length < 20) {
                        container.find(".show-more-btn").hide();
                    }

                    var allRowHtml = '';
                    $(weiboJsonList).each(function(index, weiboJson) {
                        var rowHtml = WeiboList.row.createRow(index, weiboJson);
                        allRowHtml += rowHtml;
                    });

                    var trObjs = $(allRowHtml);

                    var getTrObj = function(btnObj) {
                        return btnObj.parents("tr.weibo-tr");
                    }

                    trObjs.find(".forward-weibo-btn").unbind().click(function() {
                        var btnObj = $(this);
                        var trObj = getTrObj(btnObj);

                        trObj.find(".forward-msg-div").show();


                    });
                    trObjs.find(".cancel-forward-btn").unbind().click(function() {
                        var btnObj = $(this);
                        var trObj = getTrObj(btnObj);

                        trObj.find(".forward-msg-div").hide();
                    });

                    trObjs.find(".ensure-forward-btn").unbind().click(function() {
                        var btnObj = $(this);
                        var trObj = getTrObj(btnObj);

                        WeiboList.event.forwardWeibo(trObj, accountType);
                    });
                    trObjs.find(".friend-account-btn").unbind().click(function() {
                        if (confirm("确定要关注该微博？") == false) {
                            return;
                        }
                        var btnObj = $(this);
                        var trObj = getTrObj(btnObj);
                        var checkObj = trObj.find(".weibo-check");
                        var friendAccountId = checkObj.attr("accountId");
                        var friendUserId = checkObj.attr("friendUserId");

                        WeiboList.event.friendWeibos(friendUserId, friendAccountId, accountType);
                    });


                    tbodyObj.append(trObjs);


                    if (tbodyObj.find("tr.weibo-tr").length >= 80) {
                        container.find(".show-more-btn").hide();
                    }
                }
            });
        }
    }, WeiboList.show);

    WeiboList.event = WeiboList.event || {};
    WeiboList.event = $.extend({
        forwardWeibo: function(trObj, accountType) {
            if (confirm("确定要转发该条微博？") == false) {
                return;
            }
            var forwardMsg = trObj.find(".forward-msg-input").html();
            var checkObj = trObj.find(".weibo-check");
            var weiboId = checkObj.attr("weiboId");
            var friendAccountId = checkObj.attr("accountId");
            var friendUserId = checkObj.attr("friendUserId");

            var paramData = {};
            paramData.accountType = accountType;
            paramData.forwardMsg = forwardMsg;
            paramData.weiboId = weiboId;
            paramData.friendAccountId = friendAccountId;
            paramData.friendUserId = friendUserId;


            $.ajax({
                url : '/AccountAdmin/checkForwardWeibo',
                data : {accountType: accountType},
                type : 'post',
                success: function (dataJson) {
                    if (TM.DptgBase.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }

                    $.ajax({
                        url : '/AccountAdmin/forwardWeibo',
                        data : paramData,
                        type : 'post',
                        success: function (dataJson) {
                            if (TM.DptgBase.util.judgeAjaxResult(dataJson) == false) {
                                return;
                            }
                            alert("微博转发成功！");
                            WeiboList.show.doRefresh(accountType);
                        }
                    });


                }
            });

        },
        friendWeibos: function(friendUserId, friendAccountIds, accountType) {

            var paramData = {};
            paramData.accountType = accountType;
            paramData.friendUserId = friendUserId;
            paramData.friendAccountIds = friendAccountIds;


            $.ajax({
                url : '/AccountAdmin/checkFriendWeibo',
                data : {accountType: accountType},
                type : 'post',
                success: function (dataJson) {
                    if (TM.DptgBase.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }

                    $.ajax({
                        url : '/AccountAdmin/friendWeibos',
                        data : paramData,
                        type : 'post',
                        success: function (dataJson) {
                            if (TM.DptgBase.util.judgeAjaxResult(dataJson) == false) {
                                return;
                            }

                            WeiboList.show.doRefresh(accountType);
                        }
                    });


                }
            });

        }
    }, WeiboList.event);


    WeiboList.row = WeiboList.row || {};
    WeiboList.row = $.extend({
        createRow: function(index, weiboJson) {

            var weiboHref = 'http://weibo.com/u/' + weiboJson.accountId;

            //weiboJson.headImgUrl = "http://tp4.sinaimg.cn/3971980935/50/22867614303/1";

            var rowHtml = '' +
                '<tr class="weibo-tr">' +
                '   <td><input type="checkbox" class="weibo-check" friendUserId="' + weiboJson.userId + '" ' +
                ' weiboId="' + weiboJson.weiboId + '" accountId="' + weiboJson.accountId + '" /> </td>' +
                '   <td>' +
                '       <table style="border-collapse: collapse;width: 100%;" class="weibo-detail-table">' +
                '           <tbody>' +
                '           <tr>' +
                '               <td rowspan="3" style="vertical-align: top;width: 20%;">' +
                '                   <a href="' + weiboHref + '" target="_blank">' +
                '                       <img class="weibo-head-img" src="' + weiboJson.headImgUrl + '" style="width: 80px;height: 80px;" />' +
                '                   </a>' +
                '               </td>' +
                '               <td style="width: 80%; text-align: left;">' +
                '                   <a href="' + weiboHref + '" target="_blank">' +
                '                       <span class="weibo-name-span" style="font-weight: bold;">' + weiboJson.accountName + '</span> ' +
                '                   </a>' +
                '               </td>' +
                '           </tr>' +
                '           <tr>' +
                '               <td style="text-align: left;padding-top: 10px;">' +
                '                   <div style="">' + weiboJson.contentHtml + '</div>' +
                '               </td>' +
                '           </tr>' +
                '           <tr>' +
                '               <td style="text-align: left;padding-top: 10px;">' +
                '                   <div style="color: #6cbae4;">' + weiboJson.publishTsStr + '</div>' +
                '               </td>' +
                '           </tr>' +
                '           </tbody>' +
                '       </table> ' +
                '       <div class="forward-msg-div" style="display: none;">' +
                '           <div style="border: #ccc solid 1px; margin: 0 auto;padding: 10px;text-align: center;">' +
                '               <table class="forward-msg-table" style="margin: 0 auto; border-collapse: collapse;">' +
                '                   <tbody>' +
                '                   <tr>' +
                '                       <td><textarea rows="5" cols="45" class="forward-msg-input"></textarea></td>' +
                '                       <td style="padding-left: 20px;">' +
                '                           <div class="tmbtn blue-short-btn ensure-forward-btn">确定</div>' +
                '                           <div style="height: 10px;"></div>' +
                '                           <div class="tmbtn red-short-btn cancel-forward-btn">取消</div>' +
                '                       </td>' +
                '                   </tr>' +
                '                   </tbody>' +
                '               </table>' +
                '           </div> ' +
                '            ' +
                '       </div>' +
                '   </td>' +
                '   <td>' +
                '       <div class="tmbtn short-green-btn forward-weibo-btn">转发微博</div> ' +
                '       <div style="height: 10px;"></div> ' +
                '       <div class="tmbtn yellow-btn friend-account-btn" style="">关注帐号</div> ' +
                '   </td>' +
                '</tr>' +
                '' +
                '';

            return rowHtml;
        }
    }, WeiboList.row);

})(jQuery,window));