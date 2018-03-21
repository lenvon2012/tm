
var TM = TM ||{};

((function ($, window) {
    TM.SearchHistory= TM.SearchHistory || {};

    TM.SearchHistory = $.extend({
        init: function(container){

//            TM.SearchHistory.bindSearchBtn(container);

//            TM.SearchHistory.bindSearchHistoryBtn(container);
        	
        	TM.SearchHistory.querySearchHistory(container);
        },

        bindSearchBtn: function(container){
            $(".do-search-btn").click(function(){
                var word = $(".query-text").val();
                var sort = $("input[name='sort']:checked").val();
                var pages = $(".pages-select").val();
                var minPrice = $('input.priceInput[name="minPrice"]').val();
                var maxPrice =  $('input.priceInput[name="maxPrice"]').val();

                TM.SearchHistory.itemWords(container, word, sort, pages);
            });

            $('.query-text').keydown(function(event) {
                if (event.keyCode == "13") {//keyCode=13是回车键
                    $(".do-search-btn").click();
                }
            });
        },

        bindSearchHistoryBtn: function(container){
            $(".search-history-btn").click(function(){
                TM.SearchHistory.querySearchHistory(container);
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
            detailHead.html('<tr><th>宝贝标题</th><th style="width:250px;">宝贝标题</th><th>关键词</th><th>当前排名</th><th>排名方式</th></tr>');
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
                        'params.word':word,
                        'params.order':sort,
                        'params.pageNum':pages,
                        'params.minPrice':minPrice,
                        'params.maxPrice':maxPrice
                    },
                    dataType: 'json',
                    url: '/home/doSearchNow',
                    callback: function(data){
                        if(!data || !data.res || data.res.length == 0){
                            detailTbody.append($('<tr><td colspan="5" style="font-size:14px;line-height: 40px;">抱歉，未找到您店铺的宝贝哦！</td></tr>'));
                        } else {
                            var res = data.res;
                            var rows = TM.RenderData.renderSearchWordTable(data, detailTbody);
                        }
                        container.find(".itemwordsholder").show();
                    }
                }
            });
        },

        querySearchHistory: function(container) {
            var detailHead = container.find(".detail-head");
            detailHead.html('<tr><th>宝贝主图</th><th style="width:250px;">宝贝标题</th><th>关键词</th><th>当前排名</th><th>排名方式</th><th>查询地区</th><th>查询时间</th></tr>');
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
                            detailTbody.append($('<tr><td colspan="7" style="font-size:14px;line-height: 40px;">抱歉，亲还没有查询过哦！</td></tr>'));
                        } else {
                            var res = data.res;
                            var rows = TM.RenderData.renderSearchHistoryTable(data, detailTbody);
                        }
                        container.find(".itemwordsholder").show();
                    }
                }
            });
        }
    }, TM.SearchHistory );

})(jQuery, window));


((function ($, window) {
    TM.RenderData = TM.RenderData || {};

    TM.RenderData.getRenderDataRankPageDisplay = function(rank){
        if(!rank || rank <= 0) {
            return "无排名";
        }

        var  pn = Math.floor((rank - 1) / 44) + 1;
        var  po = (rank - 1) % 44 + 1;
        var res = '第'+pn+'页 第'+po+"位";
        return res;
    }

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
        var sortName = "综合";
        if(sort == "default"){
            sortName = "综合";
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
        } else if(sort == "手机查排名") {
            sortName = "手机查排名";
        }
        return sortName;
    }

    TM.RenderData.renderSearchWordTable = function(data, tbody){
        var arr = [];

        var sortName = TM.RenderData.getSortName(data.msg);

        $.each(data.res, function(i ,elem){
            arr.push('<tr>');
            arr.push('<td><a href="http://item.taobao.com/item.htm?id='+elem.numIid+'" class="thumbnail" target="_blank"><img alt="" src="'+elem.picPath+'_60x60.jpg" style="width:60px;height:60px;" /></a></td>')
            arr.push('<td class="item-title"><a href="http://item.taobao.com/item.htm?id='+elem.numIid+'" target="_blank">'+elem.title+'</a></td>');
            arr.push('<td class="bluekeyword">'+elem.keyword+'</td>');
            arr.push('<td style="font-weight: bold;">'+TM.RenderData.getRenderDataRankPageDisplay(elem.rank)+'</td>');
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

            var searchType =  TM.RenderData.getSortName(elem.sort);
            var rank;
            var queryArea;
            var hotArea = "";
            
            if(elem.rank > 100000) {
            	elem.rank = elem.rank - 100000;
            	hotArea = '<br><span class="inlineblock" style="width: 75px; height: 23px; line-height: 23px; color: #fff; background-color: #f40;">掌柜热卖</span>'
            }

            if(searchType == "手机查排名") {
                rank = TM.RenderData.getMobileRenderDataRankPageDisplay(elem.rank);
                queryArea = "-";
            }else {
                rank = TM.RenderData.getRenderDataRankPageDisplay(elem.rank);
                queryArea = elem.area;

            }

            if (queryArea === undefined || queryArea == null) {
                queryArea = "默认";
            }

            arr.push('<tr>');
            arr.push('<td><a href="http://item.taobao.com/item.htm?id='+elem.numIid+'" class="thumbnail" target="_blank"><img alt="" src="'+elem.picPath+'_60x60.jpg" style="width:60px;height:60px;" /></a></td>')
            arr.push('<td class="item-title"><a href="http://item.taobao.com/item.htm?id='+elem.numIid+'" target="_blank">'+elem.title+'</a></td>');
            arr.push('<td class="bluekeyword">'+elem.word+'</td>');
            arr.push('<td style="font-weight: bold;">'+rank+hotArea+'</td>');
            arr.push('<td>'+searchType+'</td>');
            arr.push('<td class="queryArea">'+queryArea+'</td>');
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
