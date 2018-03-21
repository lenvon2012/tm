
var TM = TM ||{};

((function ($, window) {
    TM.MainSearch= TM.MainSearch || {};

    TM.MainSearch.init = function(container){

        TM.MainSearch.registerBtn(container);

        TM.MainSearch.bindSelectItem(container);

        TM.MainSearch.genShopItems(container);

    }

    TM.MainSearch.registerBtn = function(container){
        $(".do-search-btn").click(function(){
            var input = $(".query-text").val();
            if(input == "") {
                input = $(".selectItem").find("option:selected").attr("numIid");
            } else {
                container.find(".iteminfo").hide();
            }
            if (input == "") {
                return;
            }

            TM.MainSearch.itemWords(container, input);
        });

        $('.query-text').keydown(function(event) {
            if (event.keyCode == "13") {//keyCode=13是回车键
                $(".do-search-btn").click();
            }
        });
    }

    TM.MainSearch.bindSelectItem = function(container){
        $(".selectItem").unbind();
        $(".selectItem").change(function(){
            $(".query-text").val("");
            container.find(".iteminfo").hide();
            var selectItem = $(".selectItem").find("option:selected");
            var numIid = selectItem.attr("numIid");
            if(selectItem.attr("tag") == "item") {
                var picPath = selectItem.attr("pic");
                var title = selectItem.text();
                var itemInfo = TM.Subway.genItemTable(numIid,picPath,title,TM.name);
                var tbody = container.find(".iteminfo-tbody");
                tbody.empty();
                tbody.append(itemInfo);
                container.find(".iteminfo").show();
            }

            TM.MainSearch.itemWords(container, numIid);
        });
    }

    TM.MainSearch.genShopItems = function(){
        $.get("/TxmHome/findUserItems", function(data){
            $(".selectItem").empty();
            $(".selectItem").append($('<option tag="all" >全店宝贝</option>'));
            $(data).each(function(i,item){
                $(".selectItem").append($('<option tag="item" numIid="'+item.id+'" pic="'+item.picURL+'" >'+item.title+'</option>'));
            });
            $(".selectItem").trigger('change');
        });
    }

    TM.MainSearch.itemWords = function(container, input) {
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
                    numIid:input
                },
                dataType: 'json',
                url: '/TxmHome/searchWords',
                callback: function(data){
                    if(!data || !data.res || data.res.length == 0){
                        detailTbody.append($('<tr><td colspan="4" style="font-size:14px;line-height: 40px;">抱歉，未找到对应的数据！</td></tr>'));
                    } else {
                        var res = data.res;
                        var rows = TM.Subway.renderSearchWordTable(res, detailTbody);
                    }
                    container.find(".itemwordsholder").show();
                }
            }
        });
    }

})(jQuery, window));

((function ($, window) {
    TM.Subway = TM.Subway || {};

    TM.Subway.renderItemTable = function(res, tbody){
        var row = '<tr><td style="width:150px;">' +
            '<a href="http://item.taobao.com/item.htm?id='+res.numIid+'" class="thumbnail" target="_blank"><img alt="" src="'+res.picPath+'_80x80.jpg" /></a>' +
            '</td><td style="width:400px;">'+res.title+'</td>' +
            '<td style="width:200px;">'+res.wangwang+'</td></tr>';

        tbody.append($(row));
    }

    TM.Subway.genItemTable = function(numIid, picPath, title, wangwang){
        var row = '<tr><td style="width:150px;">' +
            '<a href="http://item.taobao.com/item.htm?id='+numIid+'" class="thumbnail" target="_blank"><img alt="" src="'+picPath+'_80x80.jpg" /></a>' +
            '</td><td style="width:400px;">'+title+'</td>' +
            '<td style="width:200px;">'+wangwang+'</td></tr>';
        return row;
    }

    TM.Subway.renderAdWordTable = function(res, tbody){
        var arr = [];

        $.each(res, function(i ,elem){
            arr.push('<tr>');
            arr.push('<td class="bidword">'+elem.id.keyword+'</td>');
            arr.push('<td>'+elem.rank+'('+TM.Subway.getSubwayRankPageDisplay(elem.rank)+')</td>');
            arr.push('<td>'+((100-elem.rank)*13+4)+'</td>')
            arr.push('</tr>');
        });


        var rows = $(arr.join(''));
        tbody.empty();
        tbody.append(rows);
        tbody.find('tr:odd').addClass('odd');
        return rows;
    }

    TM.Subway.getSubwayRankPageDisplay = function(rank){
        var  pn = Math.floor((rank - 1) / 13) + 1;
        var  po = (rank - 1) % 13 + 1;
        var res = '第'+pn+'页';
        if(po <= 8){
            res += '右侧第'+po+"位";
        }else{
            res += "下方第"+(po - 8)+"位";
        }
        return res;
    }

    TM.Subway.renderSearchWordTable = function(res, tbody){
        var arr = [];

        $.each(res, function(i ,elem){
            arr.push('<tr>');
            arr.push('<td class="ms-title"><a href="http://item.taobao.com/item.htm?id='+elem.numIid+'" target="_blank">'+elem.title+'</a></td>');
            arr.push('<td class="bluekeyword">'+elem.keyword+'</td>');
            arr.push('<td>'+elem.rank+'</td>');
            arr.push('<td>'+((100-elem.rank)*13+4)+'</td>');
            arr.push('</tr>');
        });


        var rows = $(arr.join(''));
        tbody.empty();
        tbody.append(rows);
        tbody.find('tr:odd').addClass('odd');
        return rows;
    }

})(jQuery, window));
