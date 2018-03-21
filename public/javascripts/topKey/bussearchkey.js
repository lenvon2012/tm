
var TM = TM || {};
((function ($, window) {
    TM.BusSearchKey = TM.BusSearchKey || {};
    var BusSearchKey = TM.BusSearchKey;

    BusSearchKey.Init = BusSearchKey.Init || {};
    BusSearchKey.Init = $.extend({
        init : function(order, sort,s){
            BusSearchKey.Event.setStaticEvent();
            $(".busSearchBlock #busSearchBtn").click(function() {
                var searchText = $(".busSearchBlock #busSearchText").val();
                if (searchText == "") {
                    //alert("请先输入查询条件");
                    //return;
                }
                BusSearchKey.Init.search(order, sort,searchText);
            });
            $('#busSearchText').keydown(function(event) {
                if (event.keyCode == "13") {//keyCode=13是回车键
                    $(".busSearchBlock #busSearchBtn").trigger('click')
                }
            });
            BusSearchKey.Init.search(order, sort,s);
        },
        search : function(order, sort, s){
            if(order === undefined || order == ''){
                order = 'pv';
            }
            if(sort === undefined || sort == ''){
                sort = 'desc';
            }
            $('.bus-search-pagging').tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount:1,
                ajax: {
                    on: true,
                    dataType: 'json',
                    url: "/words/busSearch",
                    param:{order:order,sort:sort,word:s},
                    callback:function(data){
                        $('#bus-search-tbody').empty();
                        if(data != null){
                            if(data.res.length > 0) {
                                $(data.res).each(function(i,myword){
                                    if(myword.word != "") {
                                        if(parseInt(myword.pv) <= 0) {
                                            return;
                                        }

                                        var pv = parseInt(myword.pv) > 10000 ? new Number(myword.pv).toTenThousand(2) : myword.pv;
                                        var click = myword.click > 10000 ? new Number(myword.click).toTenThousand(2) : (myword.click > 0 ? myword.click : 0);
                                        var scount = myword.scount > 10000 ? new Number(myword.scount).toTenThousand(2) : (myword.scount > 0 ? myword.scount : "~");
                                        var score = (myword.scount > 0) ? Math.floor(parseInt(myword.pv) / parseInt(myword.scount)) : 0;
                                        
                                        myword.word = myword.word.replace("2013", "2016");
                                        myword.word = myword.word.replace("2014", "2016");
                                        myword.word = myword.word.replace("2015", "2016");
                                        myword.word = myword.word.replace("2016", "2017");
                                        
                                        //var ctr = myword.score
                                        $('#bus-search-tbody').append($('<tr><td class="word-content">'
                                        		+myword.word
                                        		+'</td><td>'+pv+'</td>' +
                                                '<td>'+click+'</td>' +
                                                '<td>'+myword.clickRate+'</td>' +
                                                '<td>' +myword.transRate+'</td>'+
                                                '<td>'+myword.bidPrice+'</td>' +
                                                '<td>'+myword.catName+'</td>' +
                                        		'<td class="add-to-mywords" style="cursor: pointer;">' +
                                                '<span class="tmbtn sky-blue-btn">添加到词库</span></td></tr>'))
                                    }
                                })
                                $('#bus-search-tbody').find('tr:even').addClass('even');
                                $(".bussearch tbody").find('.add-to-mywords span').click(function(){
                                    var targetWord = $(this).parent().parent().find('.word-content').text();
                                    var targetPv = -1;
                                    var targetPrice = -1;
                                    $(data.res).each(function(index, wordJson){
                                        if(wordJson.word == targetWord){
                                            targetPv = wordJson.pv;
                                            targetPrice = wordJson.price;
                                        }
                                    });
                                    $.post('/KeyWords/addMyWord',{word:targetWord, pv:targetPv, price:targetPrice},function(data){
                                        TM.Alert.load(data,400, 300, function(){}, "", "添加到词库");
                                    });
                                });
                            } else{
                                $('#bus-search-tbody').append($('<td colspan="9"><p style="font-size: 16px;text-align: center;font-family: 微软雅黑;">亲，请输入关键词搜索</p></td>'));
                            }

                        }
                    }
                }
            });
        }

    },BusSearchKey.Init);

    BusSearchKey.Util = BusSearchKey.Util || {};
    BusSearchKey.Util = $.extend({

    },BusSearchKey.Util);

    BusSearchKey.Event = BusSearchKey.Event || {};
    BusSearchKey.Event = $.extend({
        setStaticEvent : function(){
            $('.bus-search-sort-th').click(function(){
                var order = $(this).find('.sort').attr('sort');
                var sort = "";
                if($(this).find('.sort').hasClass('Desc')){
                    $(this).find('.sort').removeClass('Desc');
                    $(this).find('.sort').addClass('Asc');
                    sort = "asc";
                } else {
                    $(this).find('.sort').removeClass('Asc');
                    $(this).find('.sort').addClass('Desc');
                    sort = 'desc';
                }
                var s = $('#busSearchText').val();
                TM.BusSearchKey.Init.search(order,sort,s);
            });
        }
    },BusSearchKey.Event);
})(jQuery,window));


