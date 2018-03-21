var TM = TM || {};
((function ($, window) {
    TM.Wangke = TM.Wangke || {};
    var Wangke = TM.Wangke;
    Wangke.Init = Wangke.Init || {};
    Wangke.Init = $.extend({
        init : function(){
            Wangke.Event.setStaticEvent();
            TM.WangkeSearch.Init.init('pv','desc','女装');
        }
    },Wangke.Init);

    Wangke.Event = Wangke.Event || {};
    Wangke.Event = $.extend({
        setStaticEvent : function(){
            Wangke.Event.setSuperSearchEvent();
            Wangke.Event.setSearchNowEvent();
            Wangke.Event.setSuperSearchNowEvent();
        },
        setSuperSearchEvent : function(){
            $("#tosupersearch").click(function(){
                $(".nor_search").slideUp("fast");
                $(".sup_search").slideDown("fast");
            });
            $("#tonorsearch").click(function(){
                $(".nor_search").slideDown("fast");
                $(".sup_search").slideUp("fast");
            });
        },
        setSearchNowEvent : function(){
            $('#serch_now').click(function(){
                var s = $('#search_key').val();
                TM.WangkeSearch.Init.search("", "", s, "", "");
            });
        },
        setSuperSearchNowEvent : function(){
            $('#search_now_super').click(function(){
                var s = $('#all_cd').val();
                // 设置包含词
                var contain = "", exclude = "";
                if($('#baohan_cd1').val() != ''){
                    contain += $('#baohan_cd1').val() + ",";
                }
                if($('#baohan_cd2').val() != ''){
                    contain += $('#baohan_cd2').val() + ",";
                }
                if($('#baohan_cd3').val() != ''){
                    contain += $('#baohan_cd3').val() + ",";
                }

                // 设置排除词
                var contain = "", exclude = "";
                if($('#buhan_cd1').val() != ''){
                    exclude += $('#buhan_cd1').val() + ",";
                }
                if($('#buhan_cd2').val() != ''){
                    exclude += $('#buhan_cd2').val() + ",";
                }
                if($('#buhan_cd3').val() != ''){
                    exclude += $('#buhan_cd3').val() + ",";
                }
                TM.WangkeSearch.Init.search("", "", s, contain, exclude);
            });
        }
    },Wangke.Event);

    TM.WangkeSearch = TM.WangkeSearch || {};
    var WangkeSearch = TM.WangkeSearch;

    WangkeSearch.Init = WangkeSearch.Init || {};
    WangkeSearch.Init = $.extend({
        init : function(order, sort, s, contain, exclude){
            WangkeSearch.Event.setStaticEvent();
            $(".busSearchBlock #busSearchBtn").click(function() {
                var searchText = $(".busSearchBlock #busSearchText").val();
                if (searchText == "") {
                    //alert("请先输入查询条件");
                    //return;
                }
                WangkeSearch.Init.search(order, sort, searchText, contain, exclude);
            });
            WangkeSearch.Init.search(order, sort, s, contain, exclude);
        },
        search : function(order, sort, s, contain, exclude){
            if(order === undefined || order == ''){
                order = 'pv';
            }
            if(sort === undefined || sort == ''){
                sort = 'desc';
            }
            if(contain === undefined || contain == ''){
                contain = '';
            }
            if(exclude === undefined || exclude == ''){
                exclude = '';
            }
            $('.bus-search-pagging').tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount:1,
                ajax: {
                    on: true,
                    dataType: 'json',
                    url: "/words/wangkeSearch",
                    param:{order:order,sort:sort,word:s,contain:contain,exclude:exclude},
                    callback:function(data){
                        $('#bus-search-tbody').empty();
                        var isEven = false;
                        if(data != null){
                            if(data.res.length > 0) {
                                $(data.res).each(function(i,myword){
                                    if(myword.word != "") {
                                        var even = "";
                                        if(isEven) {
                                            even = "even";
                                            isEven = false;
                                        } else {
                                            isEven = true;
                                        }
                                        $('#bus-search-tbody').append($('<tr class="'+even+'"><td class="word-content">'+genKeywordSpan.gen({"text":myword.word,"callback":"","enableStyleChange":true,"spanClass":'addTextWrapperSmall'})+'</td><td>'+myword.pv+'</td><td>'+myword.click+'</td><td>'+myword.scount+'</td><td>'+myword.transRate+'</td><td>'+myword.score+'</td><td>'+myword.bidPrice+'</td><td class="add-to-mywords" style="cursor: pointer;"><span class="addTextWrapper btn btn-info">添加到词库</span></td></tr>'))
                                    }
                                })
                                $(".bussearch tbody").find('.add-to-mywords span').click(function(){
                                    $.post('/KeyWords/addMyWord',{word:$(this).parent().parent().find('.word-content').text()},function(data){
                                        TM.Alert.load(data,400, 300, function(){}, "", "添加到词库", 3000);
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

    },WangkeSearch.Init);

    WangkeSearch.Util = WangkeSearch.Util || {};
    WangkeSearch.Util = $.extend({

    },WangkeSearch.Util);

    WangkeSearch.Event = WangkeSearch.Event || {};
    WangkeSearch.Event = $.extend({
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
                TM.WangkeSearch.Init.search(order,sort,s,"","");
            });
        }
    },WangkeSearch.Event);
})(jQuery,window));






