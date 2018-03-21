/**
 * Created by uttp on 6/20/14.
 */
var TM = TM || {};

((function($, window){
    TM.delistChooseManager = TM.delistChooseManager || {};
    var delistChooseManager = TM.delistChooseManager;
    delistChooseManager.init = delistChooseManager.init || {};
    delistChooseManager.chooses = delistChooseManager.chooses || {};


    delistChooseManager.init = $.extend({
        doInit:function(container){
            delistChooseManager.container = container;
            delistChooseManager.initTab(container.find(".delist-choose"));
            container.find('.all-item').click();
            container.find('.all-item').attr('checked', true);
        },
        getContainer:function(){
            return delistChooseManager.container;
        }
    }, delistChooseManager.init);

    delistChooseManager.initTab = function(container){
        container.find(".category-tab").click(function(){
            container.find(".category-tab").attr("checked", false);
            $(this).attr("checked", true);
            delistChooseManager.chooses = {};
            var targetDiv = $(this).attr('tarDiv');
            var cont = delistChooseManager.init.getContainer().find('.delist-choose-show');
            if(targetDiv == "all-item-container"){
                TM.delistALL.init.doInit(cont, delistChooseManager.chooses);
            }else if(targetDiv == "seperate-category-container"){
                TM.delistCategory.init.doInit(cont, delistChooseManager.chooses);
            }else if(targetDiv == "single-item-container"){
                TM.delistSingle.init.doInit(cont, delistChooseManager.chooses);
            }
        });
    }
})(jQuery, window));

((function($, window){
    TM.delistALL = TM.delistALL || {};
    var delistAll = TM.delistALL;
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

    delistCategory.init = $.extend({
        doInit:function(container, chooses){
            delistCategory.chooses = chooses;
            container.find('.delist-choose-container').removeClass('current');
            delistCategory.container = container.find('.seperate-category-container');
            delistCategory.container.addClass('current');

            delistCategory.initCat();
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

                var rows = $('#catTmpl').tmpl(data);
                delistCategory.container.find('#itemCatContainer').empty();
                delistCategory.container.find('#itemCatContainer').append(rows);
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

                var rows = $('#catTmpl').tmpl(data);
                delistCategory.container.find('#sellerCatContainer').empty();
                delistCategory.container.find('#sellerCatContainer').append(rows);
            }
        })
    }
})(jQuery, window));


((function($, window){
    TM.delistSingle = TM.delistSingle || {};
    var delistSingle = TM.delistSingle;

    delistSingle.init = $.extend({
        doInit:function(container, chooses){
            delistSingle.chooses = chooses;
            container.find('.delist-choose-container').removeClass('current');
            container.find('.single-item-container').addClass('current');
        }
    }, delistSingle.init);
})(jQuery, window));