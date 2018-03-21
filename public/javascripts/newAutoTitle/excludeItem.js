/**
 * Created by uttp on 7/10/14.
 */

var TM = TM || {};

((function($, window){
    TM.delistExclude = TM.delistExclude || {};
    var delistExclude = TM.delistExclude;
    delistExclude.init = delistExclude.init || {};
    delistExclude.init = $.extend({
        doInit:function(container, planId, isEdit){
            delistExclude.init.initHead();
            delistExclude.planId = planId;
            delistExclude.isEdit = isEdit;
            delistExclude.init.initCate();
            delistExclude.init.initBtn();
            delistExclude.init.initExclude();
            delistExclude.init.initSearchBtn();
            delistExclude.init.initSelectAll();
            delistExclude.init.initNext();
            $('.searchBtn').trigger('click');
        },
        initSearchBtn:function(){
            $('.searchText').keyup(function(event){
                if(event.which == 13){
                    $('.searchBtn').trigger('click');
                }
            })
        },
        initHead:function(){
            $('.nav_bar').find('.header-nav').removeClass('current');
            $('.nav_bar').find('.auto-delist').addClass("current");
        },
        initSelectAll:function(){

            $('.search-container .selectPageAll').click(function(){
                $('.exclude-table .exclude-sign:not([class="hide"])').trigger('click');
            })

            $('.search-container .unselectPageAll').click(function(){
                $('.exclude-table .back-sign:not([class="hide"])').trigger('click');
            })
        },
        initNext:function(){
            $('.next').unbind().click(function(){
                $.ajax({
                    type:'post',
                    url:'/DelistPlan/configExcludeItems',
                    data:{planId:delistExclude.planId},
                    success:function(data){
                        if (TM.DelistBase.util.judgeAjaxResult(data) == false) {
                            return;
                        }
                        if(delistExclude.isEdit == false)
                            window.location.href='/newAutoTitle/distributeDelist?planId=' + delistExclude.planId;
                        else window.location.href='/newAutoTitle/distributeDelist?planId=' + delistExclude.planId+"&isEdit=true";
                    }
                })
            })
        },
        initCate:function(){
            $.get("/Items/itemCatCount", function(data){
                if(data == null || data.length == 0){
                    TM.Alert.load("淘宝类目不存在");
                }
                var item,
                    container = $(".delistexclude .search-container .taobaocat");
                container.empty();
                container.append("<option>所有淘宝类目</option>");
                for(item in data){
                    if(data[item].count > 0)
                        container.append("<option catid="+data[item].id +">"+data[item].name + "("+data[item].count +")" +"</option>");
                }
                delistExclude.init.initTaoBaoCatChange();
            });
            $.get("/Items/sellerCatCount", function(data){
                if(data == null || data.length == 0){
                    TM.Alert.load("用户店铺分类不存在");
                }
                var item,
                    container = $(".delistexclude .search-container .sellercat");
                container.empty();
                container.append("<option>所有店铺分类</option>");
                for(item in data){
                    if(data[item].count > 0)
                        container.append("<option catid="+data[item].id + ">"+data[item].name + "("+data[item].count +")" +"</option>");
                }
                delistExclude.init.initSellerCatChange();
            });
        },
        initTaoBaoCatChange:function(){
            $('.delistexclude .search-container .taobaocat').change(function(){
                $('.searchBtn').trigger('click');
            })
        },
        initSellerCatChange:function(){
            $('.delistexclude .search-container .sellercat').change(function(){
                $('.searchBtn').trigger('click');
            })
        },
        initBtn:function(){
            $('.searchBtn').click(function(){
                var params={},itemCats, sellerCats, i;
                params.s = $('.searchText').val();
                params.planId=delistExclude.planId;
                params.cid=$(".delistexclude .search-container .taobaocat option:selected").attr("catid");
                params.sellerCid=$(".delistexclude .search-container .sellercat option:selected").attr("catid");
                $('.delistexclude').find('.excludepaging').tmpage({
                    currPage: 1,
                    pageSize: 10,
                    pageCount: 1,
                    ajax: {
                        on:true,
                        param:params,
                        dataType:'json',
                        url: "/DelistPlan/getAllItems",
                        callback: function(data){
                            var i;
                            var tbody = $(".exclude-table tbody");
                            tbody.empty();
                            if(data == null || data.res == null || data.res.length == 0){
                                return;
                            }
                            for(i = 0; i < data.res.length; ++i){
                                data.res[i].item.listTime = new Date(data.res[i].item.listTime).formatYMDHMS();
                            }
                            var rows = $("#excludeitem-tmpl").tmpl(data.res);
                            tbody.append(rows);
                        }
                    }
                });
            })
        },
        initExclude:function(){
            $('.exclude-table .exclude-sign, .exclude-table .back-sign').live('click', function(){
                var i = 0, j = 0, numiid;
                if($(this).hasClass('exclude-sign')) {
                    $(this).addClass('hide');
                    $(this).siblings().removeClass('hide');
                }else if($(this).hasClass('back-sign')){
                    $(this).parent().addClass('hide');
                    $(this).parent().siblings().removeClass('hide');
                }
                if($(this).hasClass('exclude-sign')){
                    numiid = $(this).parent().parent().attr('numiid');
                    $.post("/DelistPlan/addNoAutoListItems", {planId:delistExclude.planId, numIids:numiid}, function(resultJson){
                        if (TM.DelistBase.util.judgeAjaxResult(resultJson) == false) {
                            return;
                        }
                    });
                }else if($(this).hasClass('back-sign')){
                    numiid = $(this).parent().parent().parent().attr('numiid');
                    $.post("/DelistPlan/removeNoDelist", {planId:delistExclude.planId, numIids:numiid},function(resultJson){
                        if (TM.DelistBase.util.judgeAjaxResult(resultJson) == false) {
                            return;
                        }
                    });
                }
            })
        }
    }, delistExclude.init);
})(jQuery, window));
