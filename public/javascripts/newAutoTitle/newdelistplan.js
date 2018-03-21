/**
 * Created by uttp on 6/20/14.
 */
var TM = TM || {};

((function($, window){
    TM.delistChooseManager = TM.delistChooseManager || {};
    var delistChooseManager = TM.delistChooseManager;
    delistChooseManager.planId = 0;
    delistChooseManager.notDelistNumIids = [];
    delistChooseManager.init = delistChooseManager.init || {};
    delistChooseManager.chooses = delistChooseManager.chooses || {};


    delistChooseManager.init = $.extend({
        doInit:function(container){
            delistChooseManager.container = container;
            delistChooseManager.initTab(container.find(".delist-choose"));
            delistChooseManager.initSpan(container.find('.delist-choose'));
            container.find('.all-item').trigger("click");
            container.find('.all-item').prop('checked', true);
            delistChooseManager.init.initPlanname();

            delistChooseManager.initStepTwo();
        },
        getContainer:function(){
            return delistChooseManager.container;
        },
        initPlanname:function(){
            var time = new Date().formatYMDHMS();

            $('.input-text').val("淘掌柜上架计划  " + time);
        },
        createPlan:function(){
            var planName = $('.input-text').val();
            if(planName == ""){
                TM.Alert.load("亲，计划标题不能为空的哦~");
            }else {
                $.ajax({
                    type:'post',
                    url:'/DelistPlan/createDelistPlan',
                    data:{
                        title:planName,
                        planId:delistChooseManager.planId
                    },
                    success:function(data){
                        if (TM.DelistBase.util.judgeAjaxResult(data) == false) {
                            return;
                        }
                        delistChooseManager.planId = data.results;
                    }
                })
            }
        }
    }, delistChooseManager.init);

    delistChooseManager.initTab = function(container){
        container.find(".category-tab").click(function(){
            container.find(".category-tab").prop("checked", false);
            $(this).prop("checked", true);
            delistChooseManager.chooses = {};
            var targetDiv = $(this).attr('tarDiv');
            var cont = delistChooseManager.init.getContainer().find('.delist-choose-show');
            delistChooseManager.notDelistNumIids = [];
            if(targetDiv == "all-item-container"){
                $('.steptwobtn').removeClass("hide")
                $('.steptwo').attr("tarDiv","all-item-container");
                TM.delistALL.init.doInit(cont, delistChooseManager.chooses);
            }else if(targetDiv == "seperate-category-container"){
                $('.steptwobtn').removeClass("hide")
                $('.steptwo').attr("tarDiv", "seperate-category-container");
                TM.delistCategory.init.doInit(cont, delistChooseManager.chooses);
            }else if(targetDiv == "single-item-container"){
                $('.steptwobtn').addClass("hide");
                TM.delistSingle.init.doInit(cont, delistChooseManager.chooses);
            }
        });
    };

    delistChooseManager.initSpan = function(container){
        container.find('span.choose-span').click(function(){
            var tarRadio = $(this).attr("tarDiv");
            console.log(tarRadio);
            container.find('.' + tarRadio).trigger("click");
        });

    };
    delistChooseManager.initStepTwo = function(){
        $('.steptwobtn').click(function(){
        	if($('.steptwo').attr('tarDiv') == "seperate-category-container" && $('.seperate-category-container .taobaoitem input:checked').length == 0 && $('.seperate-category-container .selleritem input:checked').length == 0){
        		alert('请选择类目');
            	return;
            }
            $('.delist-container').addClass("hide");
            $('.steptwo').removeClass('hide');
            delistChooseManager.init.createPlan();
            
            TM.stepTwo.init.doInit();
        });
    }
})(jQuery, window));

((function($, window){
    TM.delistALL = TM.delistALL || {};
    var delistAll = TM.delistALL;

    delistAll.init = delistAll.init || {};
    delistAll.init = $.extend({
        doInit:function(container, chooses) {
            delistAll.chooses = chooses;
            container.find('.delist-choose-container').removeClass('current');
            container.find('.all-item-container').addClass('current');
        }
    }, delistAll.init);
})(jQuery, window));


((function($, window){
    TM.delistCategory = TM.delistCategory || {};
    var delistCategory = TM.delistCategory;

    delistCategory.init = delistCategory.init || {};
    delistCategory.init = $.extend({
        doInit:function(container, chooses){
            container.find('input[type="checkbox"]').prop("checked", false);
            delistCategory.chooses = chooses;
            container.find('.delist-choose-container').removeClass('current');
            delistCategory.container = container.find('.seperate-category-container');
            delistCategory.container.addClass('current');

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

    delistSingle.init = delistSingle.init || {};
    delistSingle.init = $.extend({
        doInit:function(container, chooses){
            delistSingle.chooses = chooses;
            container.find('.delist-choose-container').removeClass('current');
            container.find('.single-item-container').addClass('current');
            delistSingle.init.initSearchBtn();
            delistSingle.init.initConfigBtn();
            delistSingle.show.doInit();
            delistSingle.show.initExclude();

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
                TM.delistChooseManager.init.createPlan();
                var planName = $('.input-text').val();
                if(planName == ""){
                    TM.Alert.load("亲，计划标题不能为空的哦~");
                }else {
                    $.ajax({
                        type:'post',
                        url:'/DelistPlan/createDelistPlan',
                        data:{
                            title:planName,
                            planId:TM.delistChooseManager.planId
                        },
                        success:function(data){
                            if (TM.DelistBase.util.judgeAjaxResult(data) == false) {
                                return;
                            }
                            var params = {
                            }
                            TM.delistChooseManager.planId = data.results;
                            params.planId = TM.delistChooseManager.planId;
                            params.itemStatusRule = 1;
                            params.selfCateIds = "";
                            params.delistCateIds = "";
                            params.notDelistNumIids = TM.delistChooseManager.notDelistNumIids.join(',');
                            params.isFilterGoodSalesItem = false
                            $.ajax({
                                type:'post',
                                url:'/DelistPlan/setDelistPlanConfig',
                                data:params,
                                success:function(data){
                                    if (TM.DelistBase.util.judgeAjaxResult(data) == false) {
                                        return;
                                    }
                                    window.location.href='/newAutoTitle/distributeDelist?planId=' + TM.delistChooseManager.planId;
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
                            var rows = $("#singleitem-tmpl").tmpl(data.res);

                            tbody.append(rows);
                            var items = tbody.find('.tableHead');
                            for(var i = 0; i < items.length; ++i){
                                for(var j = 0; j < TM.delistChooseManager.notDelistNumIids.length; ++j){
                                    if($(items[i]).attr('numiid')  == TM.delistChooseManager.notDelistNumIids[j]){
                                        $(items[i]).find('.exclude-sign').click();
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
            $('.single-item-container .exclude-sign, .single-item-container .back-sign').live('click', function(){
                var i = 0, j = 0, numiid, temp;
                $(this).addClass('hide');
                $(this).siblings().removeClass('hide');
                if($(this).hasClass('exclude-sign')){
                    numiid = $(this).parent().parent().attr('numiid');
                    for(i = 0; i < TM.delistChooseManager.notDelistNumIids.length; ++i){
                        if(TM.delistChooseManager.notDelistNumIids[i] == numiid){
                            break;
                        }
                    }
                    if(i == TM.delistChooseManager.notDelistNumIids.length) {
                        TM.delistChooseManager.notDelistNumIids.push($(this).parent().parent().attr('numiid'));
                    }
                }else if($(this).hasClass('back-sign')){
//                    TM.delistChooseManager.notDelistNumIids.remove($(this).parent().parent().attr('numiid'));
                    temp = $(this).parent().parent().attr('numiid');
                    for(i=0;i < TM.delistChooseManager.notDelistNumIids.length; ++i){
                        if(temp == TM.delistChooseManager.notDelistNumIids[i]){
                            TM.delistChooseManager.notDelistNumIids.splice(i, 1);
                        }
                    }
                }
            })
        }
    },delistSingle.show);

})(jQuery, window));

((function($, window){
    TM.stepTwo = TM.stepTwo || {};
    var stepTwo = TM.stepTwo;
    stepTwo.init = stepTwo.init || {};

    stepTwo.init = $.extend({
        doInit:function(){
            stepTwo.init.initBtn();
            stepTwo.init.doShow();
            stepTwo.init.initExclude();
            stepTwo.init.initSearchBtn();
            stepTwo.init.initNext();
        },
        initSearchBtn:function(){
          $('.searchTextStepTwo').keyup(function(event){
              if(event.which == 13){
                  $('.searchBtnStepTwo').trigger('click');
              }
          })
        },
        doShow:function(){
            var params={},itemCats, sellerCats, i,url;
            params.s = $('.searchTextStepTwo').val();
            if($('.steptwo').attr('tarDiv') == "seperate-category-container") {
                params.itemCats = [];
                params.sellerCats = [];
                itemCats = $('.seperate-category-container .taobaoitem input:checked');
                sellerCats = $('.seperate-category-container .selleritem input:checked');
                for(i = 0; i < itemCats.length; ++i){
                    params.itemCats.push($(itemCats[i]).attr('cid'));
                }
                for(i = 0; i < sellerCats.length; ++i){
                    params.sellerCats.push($(sellerCats[i]).attr('cid'));
                }
                params.itemCats = params.itemCats.join(',');
                params.sellerCats = params.sellerCats.join(',');
                url = "/DelistPlan/chooseItemsCategory";
            }else if($('.steptwo').attr('tarDiv') == "all-item-container"){
                params.excludeNumIids = "";
                url = "/DelistPlan/chooseItemsTMPagingerNew";
            }
            $('.steptwo-exclude').find('.steptwo-excludepaging').tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on:true,
                    param:params,
                    dataType:'json',
                    url: url,
                    callback: function(data){
                        var i,j;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
                        var tbody = $(".exclude tbody");
                        tbody.empty();
                        if(data == null || data.res == null || data.res.length == 0){
                            return;
                        }
                        for(i = 0; i < data.res.length; ++i){
                            data.res[i].listTime = new Date(data.res[i].listTime).formatYMDHMS();
                        }
                        var rows = $("#singleitem-tmpl").tmpl(data.res);

                        tbody.append(rows);

                        var items = tbody.find('.tableHead');
                        for(i = 0; i < items.length; ++i){
                            for(j = 0; j < TM.delistChooseManager.notDelistNumIids.length; ++j){
                                if($(items[i]).attr('numiid')  == TM.delistChooseManager.notDelistNumIids[j]){
                                    $(items[i]).find('.exclude-sign').click();
                                    break;
                                }
                            }
                        }
                    }
                }
            });

        },
        initNext:function(){
            $('.next').click(function(){
                var params = {
                }

                params.planId = TM.delistChooseManager.planId;
                params.itemStatusRule = 1;
                params.selfCateIds = [];
                params.delistCateIds = [];
                params.notDelistNumIids = TM.delistChooseManager.notDelistNumIids.join(',');
                params.isFilterGoodSalesItem = false;
                params.isAutoAddNewItem = false;

                if($('.steptwo').attr('tarDiv') == "all-item-container"){
                    params.isFilterGoodSalesItem = $('.all-item-container tbody input[name="first10sale"]:checked').attr("status");
                    params.isAutoAddNewItem = $('.all-item-container tbody input[name="addnewitem"]:checked').attr("status")
                }else if($('.steptwo').attr('tarDiv') == "seperate-category-container"){
                    var taobaocats = $('.seperate-category-container .taobaoitem input:checked'),
                        selfcats = $('.seperate-category-container .selleritem input:checked'), i;
//                    if(taobaocats.length == 0){
//                        console.log("cao");
//                        $('<p>错误！请选择淘宝类目</p>').dialog();
//                        return;
//                    }
                    for(i = 0; i < taobaocats.length; ++i){
                        params.delistCateIds.push($(taobaocats[i]).attr('cid'));
                    }

                    for(i = 0;i < selfcats.length; ++i){
                        params.selfCateIds.push($(selfcats[i]).attr('cid'));
                    }
                }
                params.selfCateIds = params.selfCateIds.join(',');
                params.delistCateIds = params.delistCateIds.join(',');

                $.ajax({
                    type:'post',
                    url:'/DelistPlan/setDelistPlanConfig',
                    data:params,
                    success:function(data){
                        if (TM.DelistBase.util.judgeAjaxResult(data) == false) {
                            return;
                        }
                        window.location.href='/newAutoTitle/distributeDelist?planId=' + TM.delistChooseManager.planId;
                    }
                })
            })
        },
        initBtn:function(){
            $('.back').click(function(){
                $('.steptwo').addClass('hide');
                $('.delist-container').removeClass('hide');

            });
            $('.searchBtnStepTwo').click(function(){
                stepTwo.init.doShow();
            })
        },
        initExclude:function(){
            $('.steptwo-exclude .exclude-sign, .steptwo-exclude .back-sign').live('click', function(){
                var i = 0, j = 0, numiid, temp;
                $(this).addClass('hide');
                $(this).siblings().removeClass('hide');
                if($(this).hasClass('exclude-sign')){
                    numiid = $(this).parent().parent().attr('numiid');
                    for(i = 0; i < TM.delistChooseManager.notDelistNumIids.length; ++i){
                        if(TM.delistChooseManager.notDelistNumIids[i] == numiid){
                            break;
                        }
                    }
                    if(i == TM.delistChooseManager.notDelistNumIids.length) {
                        TM.delistChooseManager.notDelistNumIids.push($(this).parent().parent().attr('numiid'));
                    }
                }else if($(this).hasClass('back-sign')){
//                    TM.delistChooseManager.notDelistNumIids.remove($(thNis).parent().parent().attr('numiid'));
                    temp = $(this).parent().parent().attr('numiid');
                    for(i=0;i < TM.delistChooseManager.notDelistNumIids.length; ++i){
                        if(temp == TM.delistChooseManager.notDelistNumIids[i]){
                            TM.delistChooseManager.notDelistNumIids.splice(i, 1);
                        }
                    }
                }
            })
        }
    }, stepTwo.init);
})(jQuery, window));