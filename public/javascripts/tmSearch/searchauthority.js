var TM = TM || {};
((function ($, window) {
    TM.SearchAuthority = TM.SearchAuthority || {};

    var SearchAuthority = TM.SearchAuthority;

    SearchAuthority.init = SearchAuthority.init || {};
    SearchAuthority.init = $.extend({
        doInit: function(container) {
            SearchAuthority.container = container;

            $(".tm-search-btn").unbind();
            $(".tm-search-btn").click(function() {
                var userNick = $(".tb-user-nick").val();
                SearchAuthority.search.doSearch(userNick);
            });
            $(".tb-user-nick").keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    $(".tm-search-btn").click();
                }
            });

            TM.SearchBase.util.searchFromUrl();
            TM.SearchBase.util.searchFromNickInput();

/*
            var html = SearchAuthority.search.createHtml();
            SearchAuthority.container.html(html);
            $(".search-wrapper").css("padding-top", "10px");
            SearchAuthority.container.find(".tb-user-nick-span").html("");

            var itemArray = [];
            var tbodyObj = SearchAuthority.container.find(".authority-table").find("tbody");
            for (var i = 0; i < 2000; i++) {
                itemArray[i] = {};
            }
            $(itemArray).each(function(index, itemJson) {
                var trObj = SearchAuthority.row.createRow(index, itemJson);
                tbodyObj.append(trObj);
            });
            SearchAuthority.container.find(".authority-table").find("tbody").find(".item-img").lazyload({});

*/

        }
    }, SearchAuthority.init);

    SearchAuthority.search = SearchAuthority.search || {};
    SearchAuthority.search = $.extend({
        doSearch: function(userNick) {
            if (userNick === undefined || userNick == null || userNick == "") {
                alert("请先输入淘宝账号");
                return;
            }
            SearchAuthority.container.html("");
            var data = {};
            data.userNick = userNick;
            $.ajax({
                url : '/CatSearchAuthority/doQueryAuthority',
                data : data,
                type : 'post',
                success : function(dataJson) {
                    if (TM.SearchBase.util.judgeAjaxResult(dataJson) == false)
                        return;
                    var itemArray = dataJson.res;
                    if (itemArray === undefined || itemArray == null || itemArray.length <= 0) {
                        alert("暂无该账号的记录");
                        return;
                    }

                    var html = SearchAuthority.search.createHtml();
                    SearchAuthority.container.html(html);
                    $(".search-wrapper").css("padding-top", "10px");
                    SearchAuthority.container.find(".tb-user-nick-span").html(userNick);


                    var tbodyObj = SearchAuthority.container.find(".authority-table").find("tbody");

                    var callback = function() {
                        tbodyObj.html("");

                        /*var radioObj = SearchAuthority.container.find(".authority-radio:checked");
                        if (radioObj.length != 1)
                            return;

                        var type = radioObj.val();
                        var targetItemArray = [];
                        if (type == "1") {
                            for (var i = 0; i < itemArray.length; i++) {
                                targetItemArray[targetItemArray.length] = itemArray[i];
                            }
                        } else if (type == "2") {
                            for (var i = 0; i < itemArray.length; i++) {
                                if (itemArray[i].diff < 0)
                                    targetItemArray[targetItemArray.length] = itemArray[i];
                            }
                        }*/

                        var targetItemArray = itemArray;
                        $(targetItemArray).each(function(index, itemJson) {
                            var trObj = SearchAuthority.row.createRow(index, itemJson);
                            tbodyObj.append(trObj);
                        });
                        tbodyObj.find(".item-img").lazyload({});
                    }

                    callback();

                    SearchAuthority.container.find(".authority-radio").click(function() {
                        callback();
                    });
                    SearchAuthority.container.find(".authority-radio-span").click(function() {
                        $(this).parent().find(".authority-radio").attr("checked", true);
                        callback();
                    });
                }
            });

        },
        createHtml: function() {
            var html = '' +
                '<div style="margin: 0 auto; text-align: left;">' +
                '   <table>' +
                '       <tbody>' +
                '       <tr>' +
                '           <td><span style="" class="tb-user-type">淘宝卖家：</span> </td>' +
                '           <td>' +
                '               <a class="rate-link-url" target="_blank">' +
                '                   <span style="" class="tb-user-nick-span"></span> ' +
                '               </a>' +
                '           </td>' +
                /*'           <td style="padding-left: 15px;padding-top: 6px;">' +
                '               <a href="javascript:void(0);" class="shop-link">[进入店铺]</a> ' +
                '           </td>' +*/
                '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                /*'   <table style="margin-top: 10px;" class="base-info-table">' +
                '       <tbody>' +
                '       <tr>' +
                //'           <td style="width: 80px;"><span class="tip-dot">注册时间：</span> </td>' +
                //'           <td style="width: 170px;" class="register-time blue-color bold"></td>' +
                '           <td style="width: 80px;"><span class="tip-dot">宝贝个数：</span> </td>' +
                '           <td style="width: 170px;">' +
                '               共&nbsp;<span class="item-num-span" style=" color: #a10000;font-weight: bold;"></span>&nbsp;个 ' +
                '           </td>' +
                '           <td style="width: 95px;"><span class="tip-dot">卖家好评率：</span></td>' +
                '           <td style="width: 170px;" class="seller-credit"></td>' +
                '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '   <table class="base-info-table">' +
                '       <tbody>' +
                '       <tr>' +
                '           <td style="width: 120px;"><span class="tip-dot">宝贝与描述相符：</span> </td>' +
                '           <td class="item-similar-rate" style="width: 130px;"></td>' +
                '           <td style="width: 95px;"><span class="tip-dot">同行业比较：</span> </td>' +
                '           <td style="width: 170px;">' +
                '               <span class="industry-compare-rate" style=""></span>' +
                '           </td>' +
                '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '   <div style="margin-top: 10px;" class="separator"></div> ' +
                '   <table style="margin-top: 10px;">' +
                '       <tbody>' +
                '       <tr>' +
                '           <td class="bold">宝贝降权状态：</td>' +
                '           <td>' +
                '               <input type="radio" name="authority-radio" class="authority-radio" value="1" checked="checked"/><span class="type-span authority-radio-span">全部宝贝</span> ' +
                '           </td>' +
                '           <td style="padding-left: 10px;">' +
                '               <input type="radio" name="authority-radio" class="authority-radio" value="2"/><span class="type-span authority-radio-span">有降权的宝贝</span> ' +
                '           </td>' +
                '       </tr>' +
                '       </tbody>' +
                '   </table> ' +*/
                '   ' +
                '   <table class="result-table authority-table" style="width: 100%;margin-top: 15px;">' +
                '       <thead>' +
                '       <tr>' +
                '           <td style="width: 10%;">序号</td>' +
                //'           <td style="width: 10%;">图片</td>' +
                '           <td style="width: 30%;">宝贝名称</td>' +
                '           <td style="width: 17%;">销量</td>' +
                '           <td style="width: 12%;">价格</td>' +
                '           <td style="width: 12%;">权重差值</td>' +
                '           <td style="width: 19%;">权重状态</td>' +
                '       </tr>' +
                '       </thead>' +
                '       <tbody>' +
                '       </tbody>' +
                '   </table> ' +
                '</div> ' +
                '';

            return html;
        }
    }, SearchAuthority.search);

    SearchAuthority.row = SearchAuthority.row || {};
    SearchAuthority.row = $.extend({
        createRow: function(index, itemJson) {

            var html = SearchAuthority.row.createHtml(index, itemJson);
            var trObj = $(html);


            var stateObj = trObj.find(".authority-state");
            if (itemJson.diff < 0) {
                stateObj.html("此宝贝可能被降权");
                stateObj.css("font-weight", "bold");
                stateObj.css("color", "#a10000");
            } else {
                stateObj.html("正常");
            }

            return trObj;
        },
        createHtml: function(index, itemJson) {
            var trClass = "";
            if (index % 2 == 1) {
                trClass = "even-tr";
            }

            var href = "http://item.taobao.com/item.htm?id=" + itemJson.numIid;
            var html = '' +
                '<tr class="' + trClass + '">' +
                '   <td class="item-number-td" style="font-size: 22px;">' + (index + 1) + '</td>' +
                /*'   <td><a class="item-href" href="' + href + '" target="_blank">' +
                '       <img class="item-img" data-original="' + itemJson.itemImgPath + '" style="width: 70px; height: 70px;"/> ' +
                '   </a> </td>' +*/
                '   <td><a class="item-link item-href" href="' + href + '" target="_blank" style="font-size: 14px;">' + itemJson.itemTitle + '</a></td>' +
                '   <td style="">最近成交&nbsp;<span class="sale-count" style="color: #a10000;font-weight: bold;">' + itemJson.saleCount + '</span>&nbsp;笔 </td>' +
                '   <td class="price-td">￥' + itemJson.itemPrice / 100 + '</td> ' +
                '   <td class="authority-weight" style="color: #a10000; font-size: 22px; ">' + itemJson.diff + '</td> ' +
                '   <td class="authority-state" style=""></td> ' +
                '</tr>' +
                '';

            return html;
        }
    }, SearchAuthority.row);


})(jQuery,window));