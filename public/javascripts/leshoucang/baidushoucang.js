((function ($, window) {
    var Shoucang = Shoucang || {};
    TM.shoucang = Shoucang;
    Shoucang.addToBaidu = Shoucang.addToBaidu || {};
    Shoucang.addToBaidu = $.extend({
        add : function(params){
            var params = $.extend({
                title : "淘宝服务",
                url : "fuwu.taobao.com",
                content : "测试用例"
            },params);
            var it = params.title.replace("&amp;","");
            var content = params.content.replace("&amp;","");
            var url = 'http://cang.baidu.com/do/add?it='+it+'&iu='+params.url+'&dc='+content+'&fr=ien#nw=1';
            window.open(url,'_blank','scrollbars=no,width=600,height=450,left=75,top=20,status=no,resizable=yes');

        },
        remove : function(params){
            var params = $.extend({
                title : "淘宝服务",
                url : "fuwu.taobao.com",
                content : "测试用例"
            },params);
            var url = 'http://cang.baidu.com/do/cm?iid=7ed8e96fd964b6eae12bdd5b&ct=7';
            window.open(url,'_blank','scrollbars=no,width=600,height=450,left=75,top=20,status=no,resizable=yes');
        }
    },Shoucang.addToBaidu);

    Shoucang.Init = Shoucang.Init || {};
    Shoucang.Init = $.extend({
        init : function(){
            Shoucang.addToBaidu.add();
        }
    },Shoucang.Init);

    Shoucang.Itemlist = Shoucang.Itemlist || {};
    Shoucang.Itemlist = $.extend({
        getItems : function(container){
            $('.tmNav').find('span').click(function(){
                if($(this).hasClass("selected")){}
                else {
                    $('.tmNav').find('.selected').removeClass("selected");
                    $(this).addClass("selected");
                }
                if($(this).find('a').attr("tag") == "faved"){
                    container.empty();
                    container.append(Shoucang.Itemlist.fetchFaved());
                    $('.paging').hide();
                } else if ($(this).find('a').attr("tag") == "unfaved"){
                    $('.paging').fadeIn(1000);
                    container.empty();
                    container.append(Shoucang.Itemlist.fetchUnfaved());
                }
            });
            $('.tmNav').find('span').eq(0).trigger("click");
        },
        fetchFaved :function(){
            var items = $('<div class="shoucangItems"></div>');
            var table = $('<table class="shoucangTable"></table>');
            table.append($('<thead><tr><td class="itemcheck"><input type="checkbox" tag="checkAll" class="checkAll"></td><td class="itemimg">宝贝图片</td><td class="itemtitle">宝贝标题</td></tr></thead>'));      //<td class="operation">操作</td> is removed
            table.find(".checkAll").click(function(){
                $('input[tag="subCheck"]').attr("checked",this.checked);
            });
            $.get("/BaiduFav/getFaved",function(items){
                if(items.length > 0){
                    var tbody = $('<tbody></tbody>');
                    $(items).each(function(i,item){
                        var numIid = item.numIid;
                        var title = item.title;
                        var url = "http://item.taobao.com/item.htm?id=" + numIid;
                        var href = 'http://cang.baidu.com/do/rm?it='+title+'&iu='+url+'&dc='+title+'&fr=ien#nw=1';
                        tbody.append($('<tr numIid="'+item.numIid+'"><td class="itemcheck"><input type="checkbox" tag="subCheck" class="subCheckBox"></td><td class="itemimg"><a target="_blank" href="http://item.taobao.com/item.htm?id='+item.numIid+'"><img style="width: 50px;height: 50px;" src="'+item.picPath+'"></a></td><td class="itemtitle">'+item.title+'</td></tr>')); //<td class="operation"><span class="opertaionSpan">取消收藏</span></td> is removed
                    });
                    tbody.find(".subCheckBox").click(function(){
                        var $subBox = $("input[tag='subCheck']");
                        $(".checkAll").attr("checked",$subBox.length == $("input[tag='subCheck']:checked").length ? true : false);
                    });
                    tbody.appendTo(table);
                    tbody.find('.opertaionSpan').click(function(){
                        /*$.ajax({
                         url:'http://cang.baidu.com/do/cm',
                         dataType: 'jsonp',
                         data:{iid:'d8b908f120cf49c2534054c9',ct:7},
                         })*/

                        /*var numIid = $(this).parent().parent().attr("numIid");
                         var title = $(this).parent().parent().find('.itemtitle').html();
                         var url = "http://item.taobao.com/item.htm?id=" + numIid;
                         Shoucang.addToBaidu.delete({
                         title : title,
                         url : url,
                         content : title
                         });
                         $.post("/BaiduFav/removeFaved",{numIids:numIid+","},function(){

                         });*/
                    });
                }
            });
            items.append(table);
            items.append($('<div class="addItems">添加宝贝</div>'));
            items.find('.addItems').click(function(){
                multiOpByURL.createChoose.createOrRefleshCommsDiv({"itemsURL":"/BaiduFav/getunfaved","actionURL":"/BaiduFav/addFav","pn":1,"px":8,"enableSearch":true});
            });
            return items;
        },
        fetchUnfaved : function(){
            var items = $('<div class="unfavedItems"></div>');
            var table = $('<table class="unfavedTable"></table>');
            table.append($('<thead><tr><td class="itemcheck"><input type="checkbox" tag="checkAll" class="checkAll"></td><td class="itemimg">宝贝图片</td><td class="itemtitle">宝贝标题</td><td class="operation">操作</td></tr></thead>'));
            table.find(".checkAll").click(function(){
                $('input[tag="subCheck"]').attr("checked",this.checked);
            });
            $('.paging').tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount:1,
                ajax: {
                    on: true,
                    dataType: 'json',
                    url: "/BaiduFav/getunfaved",
                    callback:function(data){
                        $('.checkAll').attr("checked",false);
                        if(data.res.length > 0){
                            var tbody = table.find('tbody');
                            if(tbody.length > 0){
                                tbody.empty();
                            } else {
                                var tbody = $('<tbody></tbody>');
                            }
                            $(data.res).each(function(i,item){
                                tbody.append($('<tr numIid="'+item.id+'"><td class="itemcheck"><input type="checkbox" tag="subCheck" class="subCheckBox"></td><td class="itemimg"><a target="_blank" href="http://item.taobao.com/item.htm?id='+item.id+'"><img style="width: 50px;height: 50px;" src="'+item.picURL+'"></a></td><td class="itemtitle">'+item.title+'</td><td class="operation"><span class="opertaionSpan">添加收藏</span></td></tr>'));
                            });
                            tbody.find(".subCheckBox").click(function(){
                                var $subBox = $("input[tag='subCheck']");
                                $(".checkAll").attr("checked",$subBox.length == $("input[tag='subCheck']:checked").length ? true : false);
                            });
                            tbody.appendTo(table);
                            tbody.find('.opertaionSpan').click(function(){
                                var numIid = $(this).parent().parent().attr("numIid");
                                var title = $(this).parent().parent().find('.itemtitle').html();
                                var url = "http://item.taobao.com/item.htm?id=" + numIid;
                                Shoucang.addToBaidu.add({
                                    title : title,
                                    url : url,
                                    content : title
                                });
                                $.post("/BaiduFav/addFav",{numIids:numIid+","},function(){

                                });
                            });
                        }
                    }
                }

            });
            items.append(table);
            items.append($('<div class="addItems">添加宝贝</div>'));
            items.find('.addItems').click(function(){
                multiOpByURL.createChoose.createOrRefleshCommsDiv({"itemsURL":"/BaiduFav/getunfaved","actionURL":"/BaiduFav/addFav","pn":1,"px":8,"enableSearch":true});
            });
            return items;
        }
    },Shoucang.Itemlist);
})(jQuery, window));


