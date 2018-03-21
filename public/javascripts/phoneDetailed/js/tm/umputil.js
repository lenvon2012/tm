var TM = TM || {};
((function ($, window) {
    TM.UmpUtil = TM.UmpUtil || {};

    //服务器路径，用来和服务器交互的http url
    TM.serverPath="http://t.taovgo.com";
    //TM.serverPath="http://x.tobti.com:9000";
//    TM._tms="?_tms=61016189297fadfd1d33c36be11abc00af4928b89732a1579742176";
    TM._tms="";

    var UmpUtil = TM.UmpUtil;


    UmpUtil.util = UmpUtil.util || {};
    UmpUtil.util = $.extend({
        judgeAjaxResult: function(dataJson) {

            //TM.UmpUtil.util.checkW2Auth(dataJson);

            var message = dataJson.msg;

            if (message === undefined || message == null || message == "") {
                message = dataJson.message;
            }

            if (message === undefined || message == null || message == "") {

            } else {
                alert("1111111111111111111")
                alert(message);
            }

            var isSuccess = dataJson.isOk;
            if (isSuccess === undefined) {
                isSuccess = dataJson.success;
            }

            return isSuccess;
        },
        getAjaxResultJson: function(dataJson) {

            var resultJson = dataJson.res;

            if (resultJson === undefined) {
                resultJson = dataJson.result;
            }

            return resultJson;
        },
        checkIsW2AuthError: function(dataJson) {

            if (dataJson.success != false) {
                return false;
            }
            var authorizeUrl = dataJson.taobaoAuthorizeUrl;
            if (authorizeUrl === undefined || authorizeUrl == null || authorizeUrl == "") {
                return false;
            }

            //alert("亲，您的促销授权已过期，请点击确定后，重新授权！")

            //location.href = authorizeUrl;

            UmpUtil.util.showAuthDialog(authorizeUrl, function() {});

            return true;
        },
        isIEBrowser: function() {
            if ($.browser.msie && ($.browser.version == "6.0")) {
                return true;
            } else {
                return false;
            }
        },
        getMinDiscount: function(modifyPromotionArray) {
            var minDiscount = 1000;

            $(modifyPromotionArray).each(function(index, promotionJson) {

                if (promotionJson.discountRate < minDiscount) {
                    minDiscount = promotionJson.discountRate;
                }


            });

            return minDiscount;
        },
        showAuthDialog: function(authorizeUrl, callback) {

            if (callback === undefined || callback == null) {
                callback = function() {};
            }

            $(".promotion-errors-dialog").remove();

            var html = '' +
                '<div class="promotion-errors-dialog" style="text-align: center;">' +
                '   <div style="padding: 20px 10px; font-size: 16px;">亲，您的促销授权已过期，请点击下面按钮到新页面授权后再回来提交！</div>' +
                //'<a href="/TaoDiscount/reShouquan" target="_blank" class="orangeBtn">去淘宝授权</a>' +
                '   <a class="user-auth-link" href="' + authorizeUrl + '" target="_blank" style="color: blue; font-weight:bold; font-size: 16px;">去淘宝授权>></a>' +
                '</div> ' +
                '';


            var dialogObj = $(html);



            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:300,
                width:400,
                title:'重新授权',
                autoOpen: false,
                resizable: false,
                buttons:{

                }
            });

            dialogObj.find('.user-auth-link').click(function() {

                dialogObj.dialog('close');

                UmpUtil.util.showConfrimDialog('完成授权后，请您再重新点击提交一次！', callback, callback);

            });

            dialogObj.dialog("open");
        },
        showConfrimDialog: function(msg, okCallback, cancelCallback) {

            if (okCallback === undefined || okCallback == null) {
                okCallback = function() {};
            }

            if (cancelCallback === undefined || cancelCallback == null) {
                cancelCallback = function() {};
            }

            $(".promotion-confirm-dialog").remove();


            var html = '' +
                '<div class="promotion-confirm-dialog" style="text-align: center;">' +
                '   <div style="padding: 20px 10px; font-size: 16px;">' + msg + '</div>' +
                '</div> ' +
                '';


            var dialogObj = $(html);



            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:200,
                width:300,
                title:'提示',
                autoOpen: false,
                resizable: false,
                buttons:{
                    '确定':function(){
                        okCallback();
                        $(this).dialog('close');
                    },
                    '取消':function(){
                        cancelCallback();
                        $(this).dialog('close');
                    }
                }
            });

            dialogObj.dialog("open");
        },
        getMaxPromotionNum: function() {

            return 150;
        },
        getTMActivityId: function(container) {
            return container.find(".activityId").val();
        },
        bindTextChangeEvent: function(textObj, callback) {

            var bindEventType = '';

            if ($.browser.msie) {
                textObj.keyup(function() {
                    callback && callback();
                });
            } else {
                textObj.bind('input', function() {
                    callback && callback();
                });
            }
            //alert(bindEventType);




        }
    }, UmpUtil.util);


    UmpUtil.errors = UmpUtil.errors || {};
    UmpUtil.errors = $.extend({

        showErrors: function(container, resultJson, noErrorCallback, hasErrorCallback) {

            if (noErrorCallback === undefined || noErrorCallback == null) {
                noErrorCallback = function() {};
            }
            if (hasErrorCallback === undefined || hasErrorCallback == null) {
                hasErrorCallback = function() {};
            }


            if (resultJson === undefined || resultJson == null) {
                noErrorCallback();
                return;
            }

            UmpUtil.errors.showErrorsWithoutAlertMessage(container, resultJson, noErrorCallback, hasErrorCallback);


            UmpUtil.errors.showErrorMessage(resultJson);


        },
        showErrorMessage: function(resultJson) {
            var message = resultJson.message;

            resultJson.message = '';

            if (message === undefined || message == null || message == "") {

            } else {
                alert("22222222222222")
                alert(message);
            }


        },
        showErrorsWithoutAlertMessage: function(container, resultJson, noErrorCallback, hasErrorCallback) {

            //先显示mainError
            var mainErrorType = resultJson.mainErrorType;
            var mainErrorMessage = resultJson.mainErrorMessage;

            //mainErrorType = 4;

            if (mainErrorType > 0) {

                //数据库失败
                if (mainErrorType == 1) {
                    hasErrorCallback();

                } else if (mainErrorType == 2) {
                    //授权
                    UmpUtil.util.showAuthDialog("/PhoneItem/newReShouquan", hasErrorCallback);
                } else if (mainErrorType == 4) {
                    //最低折扣
                    UmpUtil.errors.showMinDiscountError(mainErrorMessage, hasErrorCallback);
                } else {
                    //其他错误。。。
                    hasErrorCallback();
                }


                return;
            } else {

                UmpUtil.errors.showItemErrorList(container, resultJson, noErrorCallback, hasErrorCallback);

            }


        },
        showMinDiscountError: function(mainErrorMessage, hasErrorCallback) {

            $(".promotion-errors-dialog").remove();

            var shopMinDiscount = 0;

            var html = '' +
                '<div class="promotion-errors-dialog" style="text-align: center;font-size: 16px;">' +
                '   <div style="padding: 20px 10px 10px 10px;font-size: 16px;">' +
                //'       您在淘宝后台店铺的最低折扣是<span class="shop-min-discount" style="color: #a10000;">' + shopMinDiscount + '</span>折，' +
                //'而您有宝贝设置的折扣小于了最低折扣' +
                '       亲，您有宝贝设置的折扣小于淘宝后台店铺的最低折扣！' +
                '   </div> ' +
                '   请<a href="http://smf.taobao.com/smf_tab.htm?module=rmgj" target="_blank" class="modify-shop-zhekou-btn" style="color: #a10000;font-size: 20px;">点此到淘宝修改店铺最低折扣</a>后，才可继续提交。' +
                '</div> ' +
                '';


            var dialogObj = $(html);


            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:300,
                width:400,
                title:'修改淘宝店铺最低折扣',
                autoOpen: false,
                resizable: false,
                buttons:{

                }
            });


            dialogObj.find('.modify-shop-zhekou-btn').click(function() {

                dialogObj.dialog('close');

                UmpUtil.util.showConfrimDialog('是否已修改店铺最低折扣？', hasErrorCallback, hasErrorCallback);

            });

            dialogObj.dialog("open");


        },
        showItemErrorList: function(container, resultJson, noErrorCallback, hasErrorCallback) {
            var errorJsonArray = resultJson.errorList;

            if (errorJsonArray === undefined || errorJsonArray == null || errorJsonArray.length <= 0) {
                UmpUtil.errors.showErrorMessage(resultJson);
                noErrorCallback();
                return;
            }

            $(".promotion-errors-dialog").remove();

            var html='' +
                '<div class="promotion-errors-dialog busSearch" style="margin-top: 10px;">' +
                '   <span class="error-tip-span" style="font-size: 22px; display: block;">宝贝操作失败列表：</span> ' +
                '   <table class="error-item-table list-table">' +
                '       <thead>' +
                '       <tr>' +
                '           <td style="width: 20%;">宝贝图片</td>' +
                '           <td style="width: 40%;">标题</td>' +
                '           <td style="width: 40%;">失败说明</td>' +
                '       </tr>' +
                '       </thead>' +
                '       <tbody></tbody>' +
                '   </table> ' +
                '</div>' +
                '';
            var dialogObj = $(html);

            var allTrHtml = '';

            $(errorJsonArray).each(function(index, errorJson) {

                var trHtml = UmpUtil.errors.createErrorRowHtml(index, errorJson);

                allTrHtml += trHtml;
            });

            dialogObj.find(".error-item-table tbody").html(allTrHtml);



            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:500,
                width:780,
                title:'宝贝错误列表',
                autoOpen: false,
                resizable: false,
                buttons:{'返回活动列表':function() {
                    window.location.href="/TaoDiscount/index";
                },'取消':function(){
                    hasErrorCallback();
                    dialogObj.dialog('close');
                }}
            });

            dialogObj.dialog("open");


            return;
        },
        createErrorRowHtml: function(index, errorJson) {

            var itemJson = errorJson.item;
            var errorMsg = errorJson.errorMsg;

            if (itemJson === undefined || itemJson == null) {

                var trHtml = '' +
                    '<tr>' +
                    '   <td></td>' +
                    '   <td></td>' +
                    '   <td><span class="error-intro">' + errorMsg + '</span> </td>' +
                    '</tr>' +
                    '';
                return trHtml;
            }

            if (itemJson.numIid === undefined || itemJson.numIid == null || itemJson.numIid <= 0) {
                itemJson.numIid = itemJson.id;
            }

            var itemHref = "http://item.taobao.com/item.htm?id=" + itemJson.numIid;
            var title = itemJson.title;

            var trHtml = '' +
                '<tr>' +
                '   <td>' +
                '       <a class="item-link" target="_blank" href="' + itemHref + '">' +
                '           <img class="item-img" style="width: 60px;height: 60px;" src="' + itemJson.picURL + '_120x120.jpg" />' +
                '       </a> ' +
                '   </td>' +
                '   <td><a class="item-link" target="_blank" href="' + itemHref + '"><span class="item-title">' + title + '</span> </a> </td>' +
                '   <td><span class="error-intro">' + errorMsg + '</span> </td>' +
                '</tr>' +
                '';


            return trHtml;
        }

    }, UmpUtil.errors);


})(jQuery,window));

