var TM = TM || {};
((function ($, window) {
    TM.AutoTitleUtil = TM.AutoTitleUtil || {};

    var AutoTitleUtil = TM.AutoTitleUtil;

    AutoTitleUtil.Fly = $.extend({
        flyFromTo : function(start,end,flyObj,callback){
            var obj = flyObj.clone();
            obj.css("position","absolute");
            obj.css('left',start.left);
            obj.css('top',start.top);
            obj.css('z-index',10000);
            obj.appendTo($('body'));

            obj.animate({top:end.top,left:end.left},1500, function(){
                obj.fadeOut(1000,function(){
                    obj.remove();
                });
                callback && callback();
            });
        }
    },AutoTitleUtil.Fly);


    AutoTitleUtil.util = AutoTitleUtil.util || {};
    AutoTitleUtil.util = $.extend({
        judgeAjaxResult: function(dataJson) {

            //TM.AutoTitleUtil.util.checkW2Auth(dataJson);

            var message = dataJson.msg;

            if (message === undefined || message == null || message == "") {
                message = dataJson.message;
            }

            if (message === undefined || message == null || message == "") {

            } else {
                alert(message);
            }

            var isSuccess = dataJson.isOk;
            if (isSuccess === undefined) {
                isSuccess = dataJson.success;
            }
            if (isSuccess === undefined) {
                isSuccess = dataJson.ok;
            }

            return isSuccess;
        },
        getAjaxResultJson: function(dataJson) {

            var resultJson = dataJson.res;

            if (resultJson === undefined) {
                resultJson = dataJson.result;
            }

            if (resultJson === undefined) {
                resultJson = dataJson.results;
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

            AutoTitleUtil.util.showAuthDialog(authorizeUrl, function() {});

            return true;
        },
        isIEBrowser: function() {
            if ($.browser.msie && ($.browser.version == "6.0")) {
                return true;
            } else {
                return false;
            }
        },
        showAuthDialog: function(authorizeUrl, callback) {

            if (callback === undefined || callback == null) {
                callback = function() {};
            }

            $(".fenxiao-errors-dialog").remove();

            var html = '' +
                '<div class="fenxiao-errors-dialog" style="text-align: center;">' +
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

                AutoTitleUtil.util.showConfrimDialog('是否已完成授权？', callback, callback);

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

            $(".fenxiao-confirm-dialog").remove();


            var html = '' +
                '<div class="fenxiao-confirm-dialog" style="text-align: center;">' +
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
        setSortTdEvent: function(container, callback) {
            var sortTdObjs = container.find(".sort-td");
            sortTdObjs.unbind().click(function() {
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
        }
    }, AutoTitleUtil.util);


    AutoTitleUtil.errors = AutoTitleUtil.errors || {};
    AutoTitleUtil.errors = $.extend({

        showErrors: function(resultJson, noErrorCallback, hasErrorCallback) {

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

            AutoTitleUtil.errors.showErrorsWithoutAlertMessage(resultJson, noErrorCallback, hasErrorCallback);

            AutoTitleUtil.errors.showErrorMessage(resultJson);



        },
        showErrorMessage: function(resultJson) {
            var message = resultJson.message;

            resultJson.message = '';

            if (message === undefined || message == null || message == "") {

            } else {
                alert(message);
            }


        },
        showErrorsWithoutAlertMessage: function(resultJson, noErrorCallback, hasErrorCallback) {

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
                    AutoTitleUtil.util.showAuthDialog("/umppromotion/newReShouquan", hasErrorCallback);
                } else {
                    //其他错误。。。
                    hasErrorCallback();
                }

                return;
            } else {

                AutoTitleUtil.errors.showItemErrorList(resultJson, noErrorCallback, hasErrorCallback);

            }


        },
        showItemErrorList: function(resultJson, noErrorCallback, hasErrorCallback) {
            var errorJsonArray = resultJson.errorMsgList;

            if (errorJsonArray === undefined || errorJsonArray == null || errorJsonArray.length <= 0) {
                AutoTitleUtil.errors.showErrorMessage(resultJson);
                noErrorCallback();
                return;
            }

            $(".fenxiao-errors-dialog").remove();

            var html='' +
                '<div class="fenxiao-errors-dialog busSearch" style="margin-top: 10px;">' +
                '<div class="fenxiao-errors-dialog busSearch" style="margin-top: 10px;">' +
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

                var trHtml = AutoTitleUtil.errors.createErrorRowHtml(index, errorJson);

                allTrHtml += trHtml;
            });

            dialogObj.find(".error-item-table tbody").html(allTrHtml);



            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:600,
                width:780,
                title:'宝贝错误列表',
                autoOpen: false,
                resizable: false,
                buttons:{
                    '确定':function(){
                        hasErrorCallback();
                        dialogObj.dialog('close');
                    },
                    '取消':function(){
                        hasErrorCallback();
                        dialogObj.dialog('close');
                    }
                }
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

    }, AutoTitleUtil.errors);


})(jQuery,window));

