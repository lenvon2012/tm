
var TM = TM ||{};

((function ($, window) {
    TM.MobileRank= TM.MobileRank || {};

    TM.MobileRank = $.extend({
        init: function(container){

            TM.MobileRank.bindSearchBtn(container);

            TM.MobileRank.showRecentSearchWords();

//            TM.MobileRank.bindSearchHistoryBtn(container);

//            TM.MobileRank.bindSearchCatType();
        },

        bindSearchCatType: function(){
            $('.second-type').find('span').click(function(){
                if(!$(this).hasClass("selected")){
                    // update tmnav selected type
                    $('.trade-type').find('.selected').removeClass('selected');
                    $(this).addClass("selected");
                    // init the search param to original
                    location.href=$(this).find("label").attr("href");
                }
            });
        },

        bindSearchBtn: function(container){
            var doSearch = function(){
                var word = $(".query-text").val();
                var sort = $("input[name='sort']:checked").val();
                var pages = $(".pages-select").val();
                var minPrice = $('input.priceInput[name="minPrice"]').val();
                var maxPrice = $('input.priceInput[name="maxPrice"]').val();

                TM.MobileRank.itemWords(container, word, sort, pages, minPrice, maxPrice);
            }

            $('.priceInput').keyup(function(){
                var oThis = $(this);
                var text = oThis.val();
                var replace = text.replace(/[^0-9\.]/g,'');
                if(replace.length == text.length){
                    return;
                }else{
                    oThis.val(replace);
                }
            })

            $(".do-search-btn").click(function(){
                doSearch();
            });

            $('.query-text').keydown(function(event) {
                if (event.keyCode == "13") {//keyCode=13是回车键
                    doSearch();
                }
            });
            $('.priceInput').keydown(function(event){
                if (event.keyCode == "13") {//keyCode=13是回车键
                    doSearch();
                }
            })
        },

        bindSearchHistoryBtn: function(container){
            $(".search-history-btn").click(function(){
                TM.MobileRank.querySearchHistory(container);
            });
        },

        genShopItems: function(){
            $.get("/TxmHome/findUserItems", function(data){
                $(".selectItem").empty();
                $(".selectItem").append($('<option tag="all" >全店宝贝</option>'));
                $(data).each(function(i,item){
                    $(".selectItem").append($('<option tag="item" numIid="'+item.id+'" pic="'+item.picURL+'" >'+item.title+'</option>'));
                });
                $(".selectItem").trigger('change');
            });
        },

        itemWords: function(container, word, sort, pages, minPrice, maxPrice) {
            var detailHead = container.find(".detail-head");
            detailHead.html('<tr><th>宝贝主图</th><th style="width:250px;">宝贝标题</th><th>关键词</th><th>当前排名</th><th>排名方式</th></tr>');
            var detailTbody = container.find(".detail-tbody");
            detailTbody.empty();
            var foot = container.find('.itemwordsfoot');
            foot.tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param:{
                        'params.word':word.trim(),
                        'params.order':sort.trim(),
                        'params.pageNum':pages,
                        'params.minPrice':minPrice,
                        'params.maxPrice':maxPrice
                    },
                    dataType: 'json',
                    url: '/home/doMobileRank',
                    callback: function(data){
                        if(!data || !data.res || data.res.length == 0){
                            detailTbody.append($('<tr><td colspan="5" style="font-size:14px;line-height: 40px;">抱歉，未找到您店铺的宝贝哦！</td></tr>'));
                        } else {
                            var res = data.res;
                            var rows = TM.RenderData.renderSearchWordTable(data, detailTbody);
                        }
                        container.find(".itemwordsholder").show();

                        TM.MobileRank.showRecentSearchWords();
                    }
                }
            });
        },

        querySearchHistory: function(container) {
            var detailHead = container.find(".detail-head");
            detailHead.html('<tr><th>宝贝标题</th><th style="width:250px;">宝贝标题</th><th>关键词</th><th>当前排名</th><th>排名方式</th><th>查询时间</th></tr>');
            var detailTbody = container.find(".detail-tbody");
            detailTbody.empty();
            var foot = container.find('.itemwordsfoot');
            foot.tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    dataType: 'json',
                    url: '/home/querySearchHistory',
                    callback: function(data){
                        if(!data || !data.res || data.res.length == 0){
                            detailTbody.append($('<tr><td colspan="6" style="font-size:14px;line-height: 40px;">抱歉，亲还没有查询过哦！</td></tr>'));
                        } else {
                            var res = data.res;
                            var rows = TM.RenderData.renderSearchHistoryTable(data, detailTbody);
                        }
                        container.find(".itemwordsholder").show();
                    }
                }
            });
        },
        showRecentSearchWords: function() {

            var recentTrObj = $(".recent-search-tr");
            var recentTdObj = recentTrObj.find(".recent-search-td");


            $.ajax({
                url : '/home/queryLatestSearchWords',
                data : {searchType: 2},
                type : 'post',
                success : function(dataJson) {
                    var searchWordArray = dataJson.results;
                    recentTdObj.html("");

                    if (searchWordArray === undefined || searchWordArray == null || searchWordArray.length <= 0) {
                        recentTrObj.hide();
                        return;
                    }

                    var allHtml = '';
                    var allWordNum = 0;
                    $(searchWordArray).each(function(index, searchWord) {
                        var showWord = searchWord;
                        if (showWord.length > 8) {
                            showWord = showWord.substring(0, 6) + "..";
                        }
                        allWordNum += showWord.length;

                        if (allWordNum >= 44) {
                            return false;
                        }
                        var wordHtml = '' +
                            '<div class="div-inline" style="padding-right: 10px;"><span class="recent-word" title="' + searchWord + '">' + showWord + '</span> </div>' +
                            '';

                        allHtml += wordHtml;

                    });

                    recentTdObj.html(allHtml);

                    recentTrObj.show();

                    recentTdObj.find(".recent-word").click(function() {
                        var word = $(this).attr("title");
                        $('.query-text').val(word);
                        $(".do-search-btn").click();
                    });
                }
            });
        }
    }, TM.MobileRank );

})(jQuery, window));


((function ($, window) {
    TM.RenderData = TM.RenderData || {};

    TM.RenderData.getMobileRenderDataRankPageDisplay = function(rank){
        if(!rank || rank <= 0) {
            return "无排名";
        }

        var  pn = Math.floor((rank - 1) / 30) + 1;
        var  po = (rank - 1) % 20 + 1;
        var res = '第'+pn+'页 第'+po+"位";
        return res;
    }

    TM.RenderData.getSortName = function(sort){
        var sortName = "默认";
        if(sort == "default"){
            sortName = "默认";
        } else if(sort == "renqi-desc"){
            sortName = "人气";
        } else if(sort == "sale-desc"){
            sortName = "销量";
        } else if(sort == "credit-desc"){
            sortName = "信用";
        } else if(sort == "old_starts"){
            sortName = "最新";
        } else if(sort == "price-asc"){
            sortName = "价格";
        }
        return sortName;
    }

    TM.RenderData.renderSearchWordTable = function(data, tbody){
        var arr = [];

        var sortName = TM.RenderData.getSortName(data.msg);

        $.each(data.res, function(i ,elem){
            arr.push('<tr>');
            arr.push('<td><a href="'+elem.mobileUrl+'" class="thumbnail" target="_blank"><img alt="" src="'+elem.picPath+'_60x60.jpg" style="width:60px;height:60px;" /></a></td>')
            arr.push('<td class="item-title"><a href='+elem.mobileUrl+'" target="_blank">'+elem.title+'</a></td>');
            arr.push('<td class="bluekeyword">'+elem.keyword+'</td>');
            if(elem.rank > 100000) {
            	arr.push('<td style="font-weight: bold;">'+TM.RenderData.getMobileRenderDataRankPageDisplay(elem.rank - 100000)+'<br><span class="inlineblock" style="width: 55px; height: 23px; line-height: 23px; color: #fff; background-color: #f40;">掌柜热卖</span></td>');
            } else {
            	arr.push('<td style="font-weight: bold;">'+TM.RenderData.getMobileRenderDataRankPageDisplay(elem.rank)+'</td>');
            }
            arr.push('<td>'+sortName+'</td>');
            arr.push('</tr>');
        });

        var rows = $(arr.join(''));
        tbody.empty();
        tbody.append(rows);
        tbody.find('tr:odd').addClass('odd');
        return rows;
    }

    TM.RenderData.renderSearchHistoryTable = function(data, tbody){
        var arr = [];

        $.each(data.res, function(i ,elem){
            arr.push('<tr>');
            arr.push('<td><a href="http://item.taobao.com/item.htm?id='+elem.numIid+'" class="thumbnail" target="_blank"><img alt="" src="'+elem.picPath+'_60x60.jpg" style="width:60px;height:60px;" /></a></td>')
            arr.push('<td class="item-title"><a href="http://item.taobao.com/item.htm?id='+elem.numIid+'" target="_blank">'+elem.title+'</a></td>');
            arr.push('<td class="bluekeyword">'+elem.word+'</td>');
            arr.push('<td style="font-weight: bold;">'+TM.RenderData.getRenderDataRankPageDisplay(elem.rank)+'</td>');
            arr.push('<td>'+TM.RenderData.getSortName(elem.sort)+'</td>');
            arr.push('<td>'+new Date(elem.ts).format("yyyy-MM-dd hh:mm:ss")+'</td>');
            arr.push('</tr>');
        });

        var rows = $(arr.join(''));
        tbody.empty();
        tbody.append(rows);
        tbody.find('tr:odd').addClass('odd');
        return rows;
    }

})(jQuery, window));
