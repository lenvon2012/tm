var TM = TM || {};
((function ($, window) {
    TM.SearchComment = TM.SearchComment || {};

    var SearchComment = TM.SearchComment;

    SearchComment.init = SearchComment.init || {};
    SearchComment.init = $.extend({
        doInit: function(container) {
            SearchComment.container = container;
            $(".tm-search-btn").unbind();
            $(".tm-search-btn").click(function() {
                var userNick = $(".tb-user-nick").val();
                SearchComment.search.doSearch(userNick);
            });
            $(".tb-user-nick").keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    $(".tm-search-btn").click();
                }
            });

            TM.SearchBase.util.searchFromUrl();
            TM.SearchBase.util.searchFromNickInput();
            //var html = SearchComment.search.createHtml();
            //SearchComment.container.html(html);
        }
    }, SearchComment.init);

    /*SearchComment.detail = SearchComment.detail || {};
    SearchComment.detail = $.extend({
        doInit: function(container, userId, userNick) {
            SearchComment.init.doInit(container);
            $(".tb-user-nick").val(userNick);
            SearchComment.search.searchByUserId(userId, userNick);
        }
    }, SearchComment.detail);*/

    SearchComment.search = SearchComment.search || {};
    SearchComment.search = $.extend({
        doSearch: function(userNick) {
            if (userNick === undefined || userNick == null || userNick == "") {
                alert("请先输入淘宝账号");
                return;
            }
            SearchComment.container.html("");
            var data = {};
            data.userNick = userNick;
            $.ajax({
                url : '/CatSearchComment/doQueryUserId',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    if (TM.SearchBase.util.judgeAjaxResult(dataJson) == false)
                        return;
                    var userId = dataJson.res;
                    SearchComment.search.searchByUserId(userId, userNick);
                }
            });


        },
        searchByUserId: function(userId, userNick) {
            var html = SearchComment.search.createHtml();
            var creditObj = $(html);
            SearchComment.container.html(creditObj);
            $(".search-wrapper").css("padding-top", "10px");
            SearchComment.baseInfo.doShow(creditObj, userId, userNick);
            SearchComment.list.doShow(userId);

            creditObj.find(".comment-radio").click(function() {
                SearchComment.list.doShow(userId);
            });

            creditObj.find(".comment-radio-span").click(function() {
                $(this).parent().find(".comment-radio").attr("checked", true);
                SearchComment.list.doShow(userId);
            });

            creditObj.find(".comment-check").click(function() {
                SearchComment.list.doShow(userId);
            });

            creditObj.find(".comment-check-span").click(function() {
                var isChecked = $(this).parent().find(".comment-check").is(":checked");
                if (isChecked == true)
                    isChecked = false;
                else
                    isChecked = true;
                $(this).parent().find(".comment-check").attr("checked", isChecked);
                SearchComment.list.doShow(userId);
            });
        },
        createHtml: function() {
            var html = '';
            html = '' +
                '<div style="margin-bottom: 0px;text-align: left;">' +
                '   <table>' +
                '       <tbody>' +
                '       <tr>' +
                '           <td><span style="" class="tb-user-type"></span> </td>' +
                '           <td>' +
                '               <a class="rate-link-url" target="_blank">' +
                '                   <span style="" class="tb-user-nick-span"></span> ' +
                '               </a>' +
                '           </td>' +
                '           <td style="padding-left: 7px;padding-top: 6px;">' +
                '               <a class="ww-im-link" target="_blank">' +
                '                   <img class="ww-im-image"/> ' +
                '               </a>' +
                '           </td>' +
                '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '   <table style="margin-top: 10px;" class="base-info-table">' +
                '       <tbody>' +
                '       <tr>' +
                //'           <td style="width: 80px;"><span class="tip-dot">注册时间：</span> </td>' +
                //'           <td style="width: 170px;" class="register-time blue-color bold"></td>' +
                '           <td style="width: 80px;"><span class="tip-dot">实名认证：</span> </td>' +
                '           <td style="width: 170px;" class="user-verify"></td>' +
                '           <td style="width: 80px;">&nbsp;</td>' +
                '           <td style="width: 170px;">&nbsp;</td>' +
                '       </tr>' +
                '       <tr>' +
                '           <td><span class="tip-dot">店铺信息：</span> </td>' +
                '           <td class="shop-info"></td>' +
                '           <td><span class="tip-dot">所在地区：</span> </td>' +
                '           <td class="location"></td>' +
                '       </tr>' +
                /*'       <tr>' +
                '           <td><span class="tip-dot">其他详情：</span> </td>' +
                '           <td colspan="3">' +
                '               <a class="detail-info rate-link-url"></a>' +
                '           </td>' +
                '       </tr>' +*/
                '       </tbody>' +
                '   </table>' +
                '   <div style="margin-top: 10px;" class="separator"></div> ' +
                '   <table  style="margin-top: 10px;" class="base-info-table">' +
                '       <tbody>' +
                '       <tr>' +
                '           <td style="width: 80px;"><span class="tip-dot bold">买家信用：</span></td>' +
                '           <td style="width: 170px;">' +
                '               <span class="buyer-credit blue-color bold"></span>点' +
                '               <img class="credit-img" /> ' +
                '           </td>' +
                '           <td style="width: 80px;"><span class="tip-dot">好 评 率：</span></td>' +
                '           <td style="width: 170px;" class="good-credit-rate"></td>' +
                '       </tr>' +
                '       <tr class="seller-tr" style="display: none;">' +
                '           <td colspan="4" style="color: #a10000;">' +
                '               * 此帐号已开通店铺，买家信用明细无法查询' +
                '           </td>' +
                '       </tr> ' +
                '       <tr class="buyer-tr">' +
                '           <td><span class="tip-dot">最近一周：</span></td>' +
                '           <td><span class="week-credit blue-color bold"></span>点&nbsp;&nbsp;&nbsp;&nbsp;<span class="buyer-safety bold"></span> </td> ' +
                '           <td><span class="tip-dot">最近一月：</span></td>' +
                '           <td><span class="month-credit blue-color bold"></span>点</td> ' +
                '       </tr>' +
                '       <tr class="buyer-tr trade-alert-tr" style="display: none;">' +
                '           <td colspan="4" style="color: #a10000;font-weight: bold;">' +
                '               * 注意：此帐号一周交易超过20笔，可能已经被小二注意' +
                '           </td>' +
                '       </tr> ' +
                '       <tr class="buyer-tr">' +
                '           <td><span class="tip-dot">最近半年：</span></td>' +
                '           <td><span class="half-year-credit blue-color bold"></span>点</td> ' +
                '           <td><span class="tip-dot">半年以前：</span></td>' +
                '           <td><span class="other-credit blue-color bold"></span>点</td> ' +
                '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '   <div style="margin-top: 10px;" class="separator"></div> ' +
                '   <table style="margin-top: 10px;">' +
                '       <tbody>' +
                '       <tr>' +
                '           <td class="bold">最近半年给他人的评价：</td>' +
                '           <td>' +
                '               <input type="radio" name="comment-radio" class="comment-radio" value="1" checked="checked"/><span class="type-span comment-radio-span">全部</span> ' +
                '           </td>' +
                '           <td style="padding-left: 10px;">' +
                '               <input type="radio" name="comment-radio" class="comment-radio" value="2"/><span class="type-span comment-radio-span">好评</span> ' +
                '           </td>' +
                '           <td style="padding-left: 10px;">' +
                '               <input type="radio" name="comment-radio" class="comment-radio" value="3"/><span class="type-span comment-radio-span">中评</span> ' +
                '           </td>' +
                '           <td style="padding-left: 10px;">' +
                '               <input type="radio" name="comment-radio" class="comment-radio" value="4"/><span class="type-span comment-radio-span">差评</span> ' +
                '           </td>' +
                '           <td style="padding-left: 20px;">' +
                '               <input type="checkbox" class="comment-check" value="4" checked="checked"/><span class="type-span comment-check-span">有评价内容</span> ' +
                '           </td>' +
                '       </tr>' +
                '       </tbody>' +
                '   </table> ' +
                '   <div class="paging-div" style="float: right;margin-top: 10px;"></div> ' +
                '   <div class="blank0"></div> ' +
                '   <table class="result-table comment-table" style="width: 100%;">' +
                '       <thead>' +
                '       <tr>' +
                '           <td style="width: 10%;">类型</td>' +
                '           <td style="width: 40%;">评价内容</td>' +
                '           <td style="width: 20%;">被评价的账号</td>' +
                '           <td style="width: 30%;">被评价的宝贝</td>' +
                '       </tr>' +
                '       </thead>' +
                '       <tbody>' +
                '       </tbody>' +
                '   </table> ' +
                '   <div class="paging-div" style="float: right;margin-top: 5px;"></div> ' +
                '   <div class="blank0"></div> ' +
                '</div>' +
                '';

            return html;
        }
    }, SearchComment.search);


    SearchComment.baseInfo = SearchComment.baseInfo || {};
    SearchComment.baseInfo = $.extend({
        doShow: function(creditObj, userId, userNick) {
            var data = {};
            data.userId = userId;
            data.userNick = userNick;
            $.ajax({
                url : '/CatSearchComment/doQueryUserCredit',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    if (TM.SearchBase.util.judgeAjaxResult(dataJson) == false)
                        return;
                    var creditJson = dataJson.res;
                    SearchComment.baseInfo.setCreditValue(creditObj, creditJson);


                }
            });

        },
        setCreditValue: function(creditObj, creditJson) {
            if (creditJson.seller == true) {
                creditObj.find(".tb-user-type").html("淘宝卖家：");
                var shopHtml = '' +
                    '<a class="shop-link" target="_blank">进入' + creditJson.userNick + '的店铺</a> ' +
                    '';
                var shopLinkObj = $(shopHtml);
                var shopLink = "http://store.taobao.com/shop/view_shop.htm?user_number_id=" + creditJson.userId;
                shopLinkObj.attr("href", shopLink);
                creditObj.find(".shop-info").html(shopLinkObj);

                creditObj.find(".buyer-tr").remove();
                creditObj.find(".seller-tr").show();
            } else {
                creditObj.find(".tb-user-type").html("淘宝买家：");
                creditObj.find(".shop-info").html("暂时无店铺");

                creditObj.find(".seller-tr").remove();
                if (creditJson.weekCredit.totalTradeNum > 20) {
                    creditObj.find(".buyer-safety").html("不正常");
                    creditObj.find(".buyer-safety").addClass("red-color");
                    creditObj.find(".trade-alert-tr").show();
                } else {
                    creditObj.find(".trade-alert-tr").remove();
                    creditObj.find(".buyer-safety").html("正常");
                    creditObj.find(".buyer-safety").addClass("green-color");
                }
                creditObj.find(".good-credit-rate").html(creditJson.goodCreditRate);
                creditObj.find(".week-credit").html(creditJson.weekCredit.totalCreditNum);
                creditObj.find(".month-credit").html(creditJson.monthCredit.totalCreditNum);
                creditObj.find(".half-year-credit").html(creditJson.halfYearCredit.totalCreditNum);
                creditObj.find(".other-credit").html(creditJson.otherCredit.totalCreditNum);

            }
            creditObj.find(".tb-user-nick-span").html(creditJson.userNick);
            //var rateHref = "http://rate.taobao.com/user-rate-" + creditJson.userId + ".htm";
            var rateHref = "javascript:void(0);"
            creditObj.find(".rate-link-url").attr("href", rateHref);
            //creditObj.find(".detail-info").html(rateHref);

            //旺旺
            var imLink = "http://www.taobao.com/webww/ww.php?ver=3&amp;touid=" + encodeURI(creditJson.userNick) + "&amp;siteid=cntaobao&amp;status=1&amp;charset=utf-8";
            creditObj.find(".ww-im-link").attr("href", imLink);
            var imImgSrc = "/img/catunion/online.gif";

            creditObj.find(".ww-im-image").attr("src", imImgSrc);

            //creditObj.find(".register-time").html(creditJson.registerTime);
            var verify = creditJson.verify;
            if (verify == true) {
                creditObj.find(".user-verify").html("支付宝实名认证");
                creditObj.find(".user-verify").addClass("green-color");
            } else {
                creditObj.find(".user-verify").html("暂无认证");
                creditObj.find(".user-verify").addClass("red-color");
            }

            creditObj.find(".location").html(creditJson.location);

            var totalCredit = creditJson.totalCredit;
            creditObj.find(".buyer-credit").html(totalCredit);
            var creditImg = SearchComment.util.getCreditImg(creditJson);
            if (creditImg === undefined || creditImg == null || creditImg == "") {
                creditObj.find(".credit-img").remove();
            } else {
                creditObj.find(".credit-img").attr("src", creditImg);
            }

        }
    }, SearchComment.baseInfo);


    SearchComment.list = SearchComment.list || {};
    SearchComment.list = $.extend({
        doShow: function(userId) {
            var data = {};
            data.userId = userId;
            var commentType = SearchComment.container.find(".comment-radio:checked").val();
            data.commentType = commentType;
            var hasContent = SearchComment.container.find(".comment-check").is(":checked");
            data.hasContent = hasContent;

            SearchComment.paging.doPaging(data, 1, 0);

        },
        createTipTr: function() {//提示没有评论
            var html = '' +
                '<tr>' +
                '   <td colspan="4" style="font-size: 16px;">' +
                '       当 前 暂 无 评 论' +
                '   </td>' +
                '</tr>' +
                '';
            var trObj = $(html);
            return trObj;
        },
        creatTrObj: function(index, commentJson) {
            var html = '' +
                '<tr>' +
                '   <td><img class="comment-img" /> </td>' +
                '   <td style="text-align: left; font-size: 12px;">' +
                '       <div class="comment-content"></div>' +
                '       <div style="color: #999; margin-top: 10px;" class="comment-time"></div> ' +
                '   </td>' +
                '   <td>' +
                '       <div><a class="seller-link seller-title" target="_blank"></a> </div>' +
                '       <img class="seller-level-img" /> ' +
                '   </td>' +
                '   <td style="text-align: left; font-size: 12px;">' +
                '       <div><a class="item-link item-title" target="_blank"></a></div>' +
                '       <div style="margin-top: 10px;">￥<span class="item-price"></span>元 </div>' +
                '   </td>' +
                '</tr>' +
                '';

            var trObj = $(html);
            var itemLink = "http://item.taobao.com/item.htm?id=" + commentJson.auction.aucNumId;
            trObj.find(".item-link").attr("href", itemLink);
            trObj.find(".item-price").html(commentJson.auction.auctionPrice);
            trObj.find(".item-title").html(commentJson.auction.title);

            trObj.find(".comment-content").html(commentJson.content);
            trObj.find(".comment-time").html(commentJson.date);

            var commentImgSrc = "";
            if (commentJson.rate == 1) {
                commentImgSrc = "/img/catunion/good-comment.gif";
            } else if (commentJson.rate == 0) {
                commentImgSrc = "/img/catunion/normal-comment.gif";
            } else if (commentJson.rate == -1) {
                commentImgSrc = "/img/catunion/bad-comment.gif";
            }
            trObj.find(".comment-img").attr("src", commentImgSrc);

            trObj.find(".seller-link").attr("href", commentJson.user.nickUrl);
            trObj.find(".seller-title").html(commentJson.user.nick);

            var displayRatePic = commentJson.user.displayRatePic;
            if (displayRatePic === undefined || displayRatePic == null || displayRatePic == "") {
                trObj.find(".seller-level-img").remove();
            } else {
                var userLevelImgSrc = "http://a.tbcdn.cn/sys/common/icon/rank/" + commentJson.user.displayRatePic;
                trObj.find(".seller-level-img").attr("src", userLevelImgSrc);
            }

            return trObj;
        }
    }, SearchComment.list);



    SearchComment.util = SearchComment.util || {};
    SearchComment.util = $.extend({
        getCreditImg: function(creditJson) {
            return creditJson.creditImgSrc;
        }
    }, SearchComment.util);


    SearchComment.paging = SearchComment.paging || {};
    SearchComment.paging = $.extend({
        doPaging: function(queryData, currentPage, maxPage) {
            queryData.pn = currentPage;
            SearchComment.container.find(".comment-table").find("tbody").html("");
            $.ajax({
                url : '/CatSearchComment/doQueryCommentList',
                data : queryData,
                type : 'post',
                success : function(dataJson) {
                    SearchComment.container.find(".comment-table").find("tbody").html("");
                    if (TM.SearchBase.util.judgeAjaxResult(dataJson) == false)
                        return;

                    var resultJson = dataJson.res;
                    if (maxPage < resultJson.maxPage) {
                        maxPage = resultJson.maxPage;
                    }
                    var callback = function(tempPage, jq) {
                        if (currentPage == tempPage + 1)
                            return;
                        queryData.pn = tempPage + 1;
                        SearchComment.paging.doPaging(queryData, tempPage + 1, maxPage);
                    };

                    var totalCount = maxPage * 40;
                    SearchComment.paging.initPagination(totalCount, callback, currentPage - 1);

                    var commentJsonArray = resultJson.rateListDetail;
                    if (commentJsonArray === undefined || commentJsonArray == null || commentJsonArray.length <= 0) {
                        var trObj = SearchComment.list.createTipTr();
                        SearchComment.container.find(".comment-table").find("tbody").append(trObj);
                    } else {
                        $(commentJsonArray).each(function(index, commentJson) {
                            var trObj = SearchComment.list.creatTrObj(index, commentJson);
                            SearchComment.container.find(".comment-table").find("tbody").append(trObj);
                        });
                    }
                }
            });
        },
        initPagination: function(totalCount, callback, currentPage) {
            SearchComment.container.find(".paging-div").pagination(totalCount, {
                num_display_entries : 4, // 主体页数
                num_edge_entries : 1, // 边缘页数
                callback : callback,
                link_to: 'javascript:void(0);',
                //link_to: '#',
                current_page: currentPage,
                items_per_page : 40,// 每页显示多少项
                prev_text : "&lt上一页",
                next_text : "下一页&gt"
            });
        }
    }, SearchComment.paging);


})(jQuery,window));