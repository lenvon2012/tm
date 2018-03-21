var TM = TM || {};

((function ($, window) {

    TM.BusPreKey = TM.BusPreKey || {};

    var BusPreKey = TM.BusPreKey;

    BusPreKey.init = BusPreKey.init || {};
    BusPreKey.init = $.extend({
        doInit: function(container) {
            BusPreKey.container = container;

            BusPreKey.init.initCategory();

            container.find(".search-btn").click(function() {
                BusPreKey.show.doShow();
            });
            container.find(".search-input").keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    container.find(".search-btn").click();
                }
            });
            container.find(".category-select").change(function() {
                BusPreKey.show.doShow();
            });
            container.find(".status-select").change(function() {
                BusPreKey.show.doShow();
            });


            BusPreKey.show.doShow();

        },
        initCategory: function() {
            $.ajax({
                url : "/items/sellerCatCount",
                data : {},
                type : 'post',
                error: function() {
                },
                success: function (dataJson) {
                    var selectObj = BusPreKey.container.find(".category-select");

                    var catJsonArray = dataJson;
                    if (catJsonArray === undefined || catJsonArray == null || catJsonArray.length <= 0) {
                        return;
                    }


                    var selectHtml = '';

                    $(catJsonArray).each(function(index, catJson) {
                        selectHtml += '<option value="' + catJson.id + '">' + catJson.name + '(' + catJson.count + ')</option> ';
                    });

                    selectObj.append(selectHtml);

                }
            });
        }
    }, BusPreKey.init);


    BusPreKey.show = BusPreKey.show || {};
    BusPreKey.show = $.extend({
        doShow: function() {

            var paramData = BusPreKey.show.getParamData();

            if (paramData == null) {
                return;
            }

            var tbodyObj = BusPreKey.container.find(".item-table tbody.item-tbody");

            tbodyObj.html("");

            BusPreKey.container.find(".paging-div").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount:1,
                ajax: {
                    param : paramData,
                    on: true,
                    dataType: 'json',
                    url: "/buscampaign/queryItems",
                    callback:function(dataJson){

                        var itemJsonArray = dataJson.res;

                        tbodyObj.html("");
                        $(itemJsonArray).each(function(index, itemJson) {
                            BusPreKey.show.appendItemTrObj(tbodyObj, index, itemJson);
                        });


                    }
                }

            });

        },
        getParamData: function() {
            var container = BusPreKey.container;

            var paramData = {};
            paramData.title = container.find(".search-input").val();
            paramData.catId = container.find(".category-select").val();
            paramData.state = container.find(".status-select").val();
            return paramData;
        },
        appendItemTrObj: function(tbodyObj, index, itemJson) {

            var numIid = itemJson.numIid;
            if (numIid === undefined || numIid == null || numIid <= 0) {
                numIid = itemJson.id;
                itemJson.numIid = itemJson.id;
            }

            var itemUrl = "http://item.taobao.com/item.htm?id=" + itemJson.numIid;

            var itemHtml = '' +
                '<tr class="item-tr">' +
                '   <td><a target="_blank" class="item-href"><img class="item-img" /> </a></td>' +
                '   <td><a target="_blank" class="item-href"><span class="item-title"></span> </a></td>' +
                '   <td><span class="item-price"></span> </td>' +
                '   <td><span class="tmbtn wide-yellow-btn pre-key-btn">预览宝贝关键词</span> </td>' +
                '</tr>' +
                '' +
                '';

            var keywordHtml = '' +
                '<tr class="pre-keyword-tr" style="display: none;">' +
                '   <td colspan="4" style="padding: 5px 10px; border-bottom: 1px solid #ccc;">' +
                '       <div class="keyword-paging" style="text-align: center;margin: 5px 0px;"></div> ' +
                '       <table class="list-table pre-keyword-table">' +
                '           <thead>' +
                '           <tr>' +
                '               <td style="width: 20%;">关键词</td> ' +
                '               <td style="width: 20%;"><div class="sort-td sort-up" orderBy="pv">展现量</div> </td> ' +
                '               <td style="width: 20%;"><div class="sort-td sort-up" orderBy="click">行业平均点击</div></td> ' +
                '               <td style="width: 20%;"><div class="sort-td sort-up" orderBy="price">行业平均出价(元)</div></td> ' +
                '               <td style="width: 20%;"><div class="sort-td sort-up" orderBy="competition">竞争度</div></td> ' +
                '           </tr>' +
                '           </thead>' +
                '           <tbody></tbody>' +
                '       </table> ' +
                '       <div class="keyword-paging" style="text-align: center;margin: 5px 0px;"></div> ' +
                '   </td> ' +
                '</tr> ' +
                '';

            var keywordTrObj = $(keywordHtml);

            var itemTrObj = $(itemHtml);

            tbodyObj.append(itemTrObj);
            tbodyObj.append(keywordTrObj);


            itemTrObj.find(".item-img").attr("src", itemJson.picURL);
            itemTrObj.find(".item-href").attr("href", itemUrl);
            itemTrObj.find(".item-price").html("￥" + itemJson.price);
            itemTrObj.find(".item-title").html(itemJson.title);

            itemTrObj.find(".pre-key-btn").click(function() {

                var preBtnObj = $(this);
                if (preBtnObj.hasClass("word-has-showed")) {
                    keywordTrObj.hide();
                    preBtnObj.removeClass("word-has-showed")
                    preBtnObj.html("预览宝贝关键词");

                } else {

                    keywordTrObj.show();

                    if (keywordTrObj.hasClass("word-has-inited") == false) {
                        BusPreKey.keyword.doShow(keywordTrObj, itemJson.numIid);
                        keywordTrObj.addClass("word-has-inited");
                    }

                    preBtnObj.addClass("word-has-showed");
                    preBtnObj.html("隐藏宝贝关键词");
                }


            });



            keywordTrObj.find(".sort-td").attr("title", "点击进行排序");
            keywordTrObj.find(".sort-td").click(function() {

                if ($(this).hasClass("sort-up")) {
                    $(this).removeClass("sort-up");
                    $(this).addClass("sort-down");
                } else {
                    $(this).removeClass("sort-down");
                    $(this).addClass("sort-up");
                }
                keywordTrObj.find(".sort-td").removeClass("current-sort");
                $(this).addClass("current-sort");

                BusPreKey.keyword.doShow(keywordTrObj, itemJson.numIid);
                keywordTrObj.addClass("word-has-inited");
            });




        }
    }, BusPreKey.show);


    BusPreKey.keyword = BusPreKey.keyword || {};
    BusPreKey.keyword = $.extend({

        doShow: function(keywordContainer, numIid) {

            var orderObj = keywordContainer.find(".current-sort");
            var orderBy = "";
            var isDesc = true;

            if (orderObj.length <= 0) {
                orderBy = "cpc";
            } else {
                orderBy = orderObj.attr("orderBy");
                if (orderObj.hasClass("sort-down"))
                    isDesc = true;
                else
                    isDesc = false;
            }



            keywordContainer.find(".keyword-paging").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount:1,
                isJsonp: true,
                ajax: {
                    param : {},
                    on: true,
                    dataType: 'json',
                    url: "http://chedao.taovgo.com/commons/tmrecommend?numIid=" + numIid + "&orderBy=" + orderBy + "&isDesc=" + isDesc,
                    callback:function(dataJson){

                        var keywordJsonArray = dataJson.res;
                        var tbodyObj = keywordContainer.find(".pre-keyword-table tbody");

                        tbodyObj.html("");

                        var trHtmlArray = [];
                        $(keywordJsonArray).each(function(index, keywordJson) {
                            var trHtml = '' +
                                '<tr class="keyword-result-tr">' +
                                '   <td>' + keywordJson.word + '</td>' +
                                '   <td style="">' + keywordJson.pv + '</td>' +
                                '   <td style="">' + keywordJson.click + '</td>' +
                                '   <td style="background: #dceee4;">' + keywordJson.price / 100 + '</td>' +
                                '   <td style="">' + keywordJson.competition + '</td>' +
                                '</tr>' +
                                '';

                            trHtmlArray[trHtmlArray.length] = trHtml;
                        });

                        tbodyObj.html(trHtmlArray.join(""));

                    }
                }

            });


        }
    }, BusPreKey.keyword);


})(jQuery,window));