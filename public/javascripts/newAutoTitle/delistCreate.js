var TM = TM || {};

((function($, window){
    TM.delistChooseManager = TM.delistChooseManager || {};
    var delistChooseManager = TM.delistChooseManager;
//    delistChooseManager.notDelistNumIids = [];
    delistChooseManager.init = delistChooseManager.init || {};


    delistChooseManager.init = $.extend({
        doInit:function(container){
            delistChooseManager.init.initHead();
            delistChooseManager.container = container;
            delistChooseManager.initTab(container.find(".delist-choose"));
            delistChooseManager.initSpan(container.find('.delist-choose'));
            container.find('.category-item').trigger("click");
            delistChooseManager.init.initPlanname();

            delistChooseManager.initStepTwo();
        },
        getContainer:function(){
            return delistChooseManager.container;
        },
        initPlanname:function(){
            var time = new Date().formatYMDHMS();
            $('.input-text').val("自动上架计划  " + time);
        },
        initHead:function(){
            $('.nav_bar').find('.header-nav').removeClass('current');
            $('.nav_bar').find('.auto-delist').addClass("current");
        }
    }, delistChooseManager.init);

    delistChooseManager.initTab = function(container){

        var cont = delistChooseManager.init.getContainer().find('.delist-choose-show');
        TM.delistALL.init.doInit(cont);
        TM.delistCategory.init.doInit(cont);
        TM.delistSingle.init.doInit(cont);
        container.find(".category-tab").click(function(){
            var targetDiv = $(this).attr('tarDiv');
            if(targetDiv == "all-item-container"){
                cont.find('.delist-choose-container').removeClass('current');
                cont.find('.all-item-container').addClass('current');
                $('.excludeitembtn').removeClass("hide");
                cont.attr("tarDiv","all-item-container");
            }else if(targetDiv == "seperate-category-container"){
                cont.find('.delist-choose-container').removeClass('current');
                cont.find('.seperate-category-container').addClass('current');
                $('.excludeitembtn').removeClass("hide");
                cont.attr("tarDiv", "seperate-category-container");
            }else if(targetDiv == "single-item-container"){
                cont.find('.delist-choose-container').removeClass('current');
                cont.find('.single-item-container').addClass('current');
                $('.excludeitembtn').addClass("hide");
                cont.attr("tarDiv", "single-item-container");
            }
        });
    };

    delistChooseManager.initSpan = function(container){
        container.find('span.choose-span').click(function(){
            var tarRadio = $(this).attr("tarDiv");
            container.find('.' + tarRadio).trigger("click");
        });

    };
    delistChooseManager.initStepTwo = function(){
        $('.excludeitembtn').click(function(){
//            if($('.delist-choose-show').attr('tarDiv') == "seperate-category-container" && $('.seperate-category-container .taobaoitem input:checked').length == 0 && $('.seperate-category-container .selleritem input:checked').length == 0){
//                alert('请选择类目');
//                return;
//            }

            var planName = $('.input-text').val().trim();
            if(planName == ""){
                TM.Alert.load("亲，计划标题不能为空的哦~");
            }else {
                var params={},delistCateIds, selfCateIds, i,url;
                params.title = planName;
                if($('.delist-choose-show').attr('tarDiv') == "seperate-category-container"){
                    params.delistCateIds = [];
                    params.selfCateIds = [];
                    params.isFilterGoodSalesItem = $('.seperate-category-container tbody input[name="first10sale"]:checked').attr("status");
                    params.isAutoAddNewItem = $('.seperate-category-container tbody input[name="addnewitem"]:checked').attr("status");
                    delistCateIds = $('.seperate-category-container .taobaoitem input:checked');
                    selfCateIds = $('.seperate-category-container .selleritem input:checked');
                    for(i = 0; i < delistCateIds.length; ++i){
                        params.delistCateIds.push($(delistCateIds[i]).attr('cid'));
                    }
                    for(i = 0; i < selfCateIds.length; ++i){
                        params.selfCateIds.push($(selfCateIds[i]).attr('cid'));
                    }
                    params.delistCateIds = params.delistCateIds.join(',');
                    params.selfCateIds= params.selfCateIds.join(',');
                    url = "/DelistPlan/setCateConfig";
                }else if($('.delist-choose-show').attr('tarDiv') == "all-item-container"){
                    params.isFilterGoodSalesItem = $('.all-item-container tbody input[name="first10saleother"]:checked').attr("status");
                    params.isAutoAddNewItem = $('.all-item-container tbody input[name="addnewitemother"]:checked').attr("status");
                    url = "/DelistPlan/setDelistConfig";
                }
                $.ajax({
                    type:'post',
                    url:url,
                    data:params,
                    success:function(data){
                        if(data == null || data.results <= 0){
                            TM.Alert.load("配置计划失败");
                            return;
                        }
                        var planId;
                        planId = data.results;
                        window.location.href = '/newAutoTitle/delistExclude?planId=' + planId;
                    }
                })
            }
        });
    }
})(jQuery, window));

((function($, window){
    TM.delistALL = TM.delistALL || {};
    var delistAll = TM.delistALL;

    delistAll.init = delistAll.init || {};
    delistAll.init = $.extend({
        doInit:function(container) {

        }
    }, delistAll.init);
})(jQuery, window));


((function($, window){
    TM.delistCategory = TM.delistCategory || {};
    var delistCategory = TM.delistCategory;

    delistCategory.init = delistCategory.init || {};
    delistCategory.init = $.extend({
        doInit:function(container){
            delistCategory.container = container.find('.seperate-category-container');

            delistCategory.initCat();
            delistCategory.initTree.doInit();
        },
        getContainer:function(){
            return delistCategory.container;
        }
    }, delistCategory.init);

    delistCategory.initCat = function(){
        $.ajax({
            type:'get',
            url:"/items/itemCatStatusCount",
            data:{},
            dataType:'json',
            success:function(data){
                if(data == null || data.length ==0){
                    return;
                }
                var dataJson = {};
                dataJson.data = data;

                dataJson.len = data.length;

                var rows = $('#taobaoitem-tmpl').tmpl(dataJson);
                delistCategory.container.find('.taobaoitem').empty();
                delistCategory.container.find('.taobaoitem').append(rows);
            }
        });

        $.ajax({
            type:'get',
            url:'/items/sellerCatStatusCount',
            data:{},
            dataType:'json',
            success:function(data){
                if(data == null || data.length == 0){
                    return;
                }
                var dataJson = {};
                dataJson.data = data;

                dataJson.len = data.length;

                var rows = $('#selleritem-tmpl').tmpl(dataJson);
                delistCategory.container.find('.selleritem').empty();
                delistCategory.container.find('.selleritem').append(rows);
            }
        })
    };

    delistCategory.initTree = delistCategory.initTree || {};
    delistCategory.initTree = $.extend({
        doInit:function(){
            delistCategory.initTree.initTreeHead();
            delistCategory.initTree.initCheckBox();
        },
        initTreeHead:function(){
            $(".tree-head").toggle(function(){
                var holder = $(this).parent().next();
                if($(this).hasClass("head-row")) {
                    $(this).removeClass("head-row").addClass("head-row-changed");
                }else if($(this).hasClass("head-row-second")){
                    $(this).removeClass("head-row-second").addClass("head-row-second-changed");
                }
                holder.addClass("hide");
            }, function(){
                var holder = $(this).parent().next();
                if($(this).hasClass("head-row-changed")) {
                    $(this).removeClass("head-row-changed").addClass("head-row");
                }else if($(this).hasClass("head-row-second-changed")){
                    $(this).removeClass("head-row-second-changed").addClass("head-row-second");
                }
                holder.removeClass("hide");
            })
        },
        initCheckBox:function(){
            $(".head-box").click(function(){
                if($(this).attr("checked") == "checked"){
                    if($(this).hasClass("taobaobox-head")){
                        $(".taobaobox-child").prop("checked", true);
                    }else if($(this).hasClass("sellerbox-head")){
                        $(".sellerbox-child").prop("checked", true);
                    }
                }else{
                    if($(this).hasClass("taobaobox-head")){
                        $(".taobaobox-child").prop("checked", false);
                    }else if($(this).hasClass("sellerbox-head")){
                        $(".sellerbox-child").prop("checked", false);
                    }
                }
            });

            $('.sellerbox-child, .taobaobox-child').live('click',function(){
                if($(this).hasClass('taobaobox-child') && $('.taobaobox-head').attr('checked') == 'checked'){
                    $('.taobaobox-head').prop('checked', false);
                }else if($(this).hasClass('sellerbox-child') && $('.sellerbox-head').attr('checked') == 'checked'){
                    $('.sellerbox-head').prop('checked', false);
                }
            });
        }
    }, delistCategory.initTree);
})(jQuery, window));


((function($, window){
    TM.delistSingle = TM.delistSingle || {};
    var delistSingle = TM.delistSingle;
    delistSingle.chooseItems = [];
    delistSingle.init = delistSingle.init || {};
    delistSingle.init = $.extend({
        doInit:function(container){
            delistSingle.init.initSearchBtn();
            delistSingle.init.initConfigBtn();
            delistSingle.show.doInit();
            delistSingle.show.initExclude();
            delistSingle.show.initSelectAll();

            $('.delist-searchbtn').click();
        },
        initSearchBtn:function(){
            $('.searchText').keyup(function(event){
                if(event.which == 13){
                    $('.delist-searchbtn').trigger('click');
                }
            })
        },
        initConfigBtn:function(){
            $('.configitems').click(function(){
                var planName = $('.input-text').val();
                if(planName == ""){
                    TM.Alert.load("亲，计划标题不能为空的哦~");
                }else if(delistSingle.chooseItems.length == 0){
                    TM.Alert.load("亲，请选择需要自动上下架的宝贝")
                }else {
                    $.ajax({
                        type:'post',
                        url:'/DelistPlan/createDelistPlan',
                        data:{
                            title:planName,
                            planId:0
                        },
                        success:function(data){
                            var planId;
                            if(data == null || data.results <= 0){
                                TM.Alert.load("创建计划失败！");
                                return;
                            }
                            planId = data.results;
                            delistSingle.chooseItems = delistSingle.chooseItems.join(',');
                            $.ajax({
                                type:'post',
                                url:'/DelistPlan/setUserSelectNumIids',
                                data:{
                                    planId:planId,
                                    numIids:delistSingle.chooseItems
                                },
                                success:function(data){
                                    if (data == null || data.success != true) {
                                        TM.Alert.load("选择自动上下架宝贝失败！");
                                        return;
                                    }
                                    window.location.href='/newAutoTitle/distributeDelist?planId=' + planId;
                                }
                            })
                        }
                    })
                }
            })
        }
    }, delistSingle.init);


    delistSingle.show = $.extend({
        doInit:function(){
            delistSingle.show.initSearch();
        },
        showContentInit:function(){

        },
        initSearch:function() {
            $(".delist-searchbtn").unbind().click(function () {
                var searchText = $('.searchText').val();
                $('.single-item-container').find('.manualPaging').tmpage({
                    currPage: 1,
                    pageSize: 10,
                    pageCount: 1,
                    ajax: {
                        param:{
                            s:searchText,
                            excludeNumIids:""
                        },
                        on:true,
                        dataType:'json',
                        url: '/DelistPlan/findRemainItemsNew',
                        callback: function(data){
                            var tbody = $(".manualTable tbody");
                            tbody.empty();
                            if(data == null || data.res == null || data.res.length ==0){
                                return;
                            }
                            for(var i = 0; i < data.res.length; ++i){
                                data.res[i].listTime = new Date(data.res[i].listTime).formatYMDHMS();
                            }
                            var rows = $("#singlechooseitem-tmpl").tmpl(data.res);

                            tbody.append(rows);
                            var items = tbody.find('.tableHead');
                            for(var i = 0; i < items.length; ++i){
                                for(var j = 0; j < delistSingle.chooseItems.length; ++j){
                                    if($(items[i]).attr('numiid')  == delistSingle.chooseItems[j]){
                                        $(items[i]).find('.choose-sign').click();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                });
            })
        },
        doShowItems:function() {
            var searchTitle = $(".searchText").val();

        },
        initExclude:function(){
            $('.single-item-container .choose-sign, .single-item-container .back-sign').live('click', function(){
                var i = 0, j = 0, numiid, temp;
                if($(this).hasClass("choose-sign")) {
                    $(this).addClass('hide');
                    $(this).siblings().removeClass('hide');
                }else if($(this).hasClass('back-sign')){
                    $(this).parent().addClass('hide');
                    $(this).parent().siblings().removeClass('hide');
                }
                if($(this).hasClass('choose-sign')){
                    numiid = $(this).parent().parent().attr('numiid');
                    for(i = 0; i < delistSingle.chooseItems.length; ++i){
                        if(delistSingle.chooseItems[i] == numiid){
                            break;
                        }
                    }
                    if(i == delistSingle.chooseItems.length) {
                        delistSingle.chooseItems.push($(this).parent().parent().attr('numiid'));
                    }
                }else if($(this).hasClass('back-sign')){
                    temp = $(this).parent().parent().parent().attr('numiid');
                    for(i=0;i < delistSingle.chooseItems.length; ++i){
                        if(temp == delistSingle.chooseItems[i]){
                            delistSingle.chooseItems.splice(i, 1);
                        }
                    }
                }
            })
        },
        initSelectAll:function(){
            $('.single-item-container .selectPageAll').click(function(){
                $('.single-item-container .choose-sign:not([class="hide"])').trigger('click');
            })

            $('.single-item-container .unselectPageAll').click(function(){
                $('.single-item-container .back-sign:not([class="hide"])').trigger('click');
            })
        }
    },delistSingle.show);

})(jQuery, window));
