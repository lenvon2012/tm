
var TM = TM || {};

((function ($, window) {

    TM.AutoTitleAdmin = TM.AutoTitleAdmin || {};

    var AutoTitleAdmin = TM.AutoTitleAdmin;

    AutoTitleAdmin.init = AutoTitleAdmin.init || {};
    AutoTitleAdmin.init = $.extend({
        doInit: function(container) {

            AutoTitleAdmin.container = container;

            AutoTitleAdmin.init.initTaobaoCategory();
            AutoTitleAdmin.init.initSellerCategory();


            container.find(".search-text").unbind().keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    container.find(".search-btn").click();
                }
            });
            container.find(".score-text").unbind().keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    container.find(".search-btn").click();
                }
            });
            container.find(".search-btn").unbind().click(function() {
                AutoTitleAdmin.show.doShow();
            });
            container.find("select").unbind().change(function() {
                AutoTitleAdmin.show.doShow();
            });

            TM.AutoTitleUtil.util.setSortTdEvent(container, function() {
                AutoTitleAdmin.show.doShow();
            });

            AutoTitleAdmin.show.doShow();



            container.find('.all-recommend-btn').unbind().click(function() {
                AutoTitleAdmin.submit.doRecommendTitles();
            });

            container.find('.all-guanfang-btn').unbind().click(function() {
                AutoTitleAdmin.submit.doGuanfangTitles();
            });

            container.find('.all-guanfang-recommend-btn').unbind().click(function() {
                AutoTitleAdmin.submit.doGuanfangRecommendTitles();
            });

        },
        initTaobaoCategory: function() {

            var container = AutoTitleAdmin.init.getContainer();

            $.get("/items/itemCatCount",function(data){
                var catSelectObj = container.find('.tb-category-select');

                var catJsonArray = data;

                var allOptionHtml = '';

                if (catJsonArray === undefined || catJsonArray == null || catJsonArray.length <= 0) {
                    return;
                }

                $(catJsonArray).each(function(index, catJson) {
                    if (catJson.count <= 0){
                        return;
                    }
                    allOptionHtml += '' +
                        '<option value="' + catJson.id + '">' + catJson.name + '</option>' +
                        '';
                });

                catSelectObj.append(allOptionHtml);
            });

        },
        initSellerCategory: function() {

            var container = AutoTitleAdmin.init.getContainer();

            $.get("/items/sellerCatCount",function(data){
                var catSelectObj = container.find('.seller-category-select');

                var catJsonArray = data;

                var allOptionHtml = '';

                if (catJsonArray === undefined || catJsonArray == null || catJsonArray.length <= 0) {
                    return;
                }

                $(catJsonArray).each(function(index, catJson) {
                    if (catJson.count <= 0){
                        return;
                    }
                    allOptionHtml += '' +
                        '<option value="' + catJson.id + '">' + catJson.name + '</option>' +
                        '';
                });

                catSelectObj.append(allOptionHtml);
            });


        },
        getContainer: function() {
            return AutoTitleAdmin.container;
        }
    }, AutoTitleAdmin.init);


    AutoTitleAdmin.show = AutoTitleAdmin.show || {};
    AutoTitleAdmin.show = $.extend({
        targetCurrentPage: 1,
        currentParamData: null,
        totalCount: 0,
        doShow: function() {

            AutoTitleAdmin.show.doSearch(1);
        },
        doRefresh: function() {
            AutoTitleAdmin.show.doSearch(AutoTitleAdmin.show.targetCurrentPage);
        },
        doSearch: function(currentPage) {

            var container = AutoTitleAdmin.init.getContainer();

            if (currentPage < 1) {
                currentPage = 1;
            }

            AutoTitleAdmin.show.targetCurrentPage = currentPage;

            var paramData = AutoTitleAdmin.show.getParamData();

            if (paramData === undefined || paramData == null) {
                return;
            }

            AutoTitleAdmin.show.currentParamData = paramData;

            container.find(".paging-div").tmpage({
                currPage: currentPage,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: paramData,
                    dataType: 'json',
                    url: '/TitlesUI/searchItemsByRules',
                    callback: function(dataJson){

                        AutoTitleAdmin.show.targetCurrentPage = dataJson.pn;//记录当前页
                        AutoTitleAdmin.show.totalCount = dataJson.count;


                        var tbodyObj = container.find('.item-table tbody.item-tbody');
                        tbodyObj.html('');


                        var itemJsonArray = dataJson.res;

                        if (itemJsonArray === undefined || itemJsonArray == null || itemJsonArray.length <= 0) {
                            tbodyObj.html('<tr><td colspan="6" style="padding: 10px 0px;">当前暂无满足条件的宝贝！</td> </tr>');
                            return;
                        }

                        $(itemJsonArray).each(function(index, itemJson) {
                            var numIid = itemJson.numIid;
                            if (numIid === undefined || numIid == null || numIid <= 0) {
                                numIid = itemJson.id;
                                itemJson.numIid = itemJson.id;
                            }
                            var lastOptimiseTs = itemJson.lastOptimiseTs;
                            if (lastOptimiseTs != null && lastOptimiseTs > 0) {
                                itemJson.lastOptimiseTsStr = new Date(lastOptimiseTs).formatYMDMS();
                            } else {
                                itemJson.lastOptimiseTsStr = '';
                            }
                            itemJson.itemLink = 'http://item.taobao.com/item.htm?id=' + itemJson.id;
                        });


                        var trObjs = $('#itemTitleRowTmpl').tmpl(itemJsonArray);

                        var btnObjs = trObjs.find('.optimize-item-btn');

                        AutoTitleAdmin.event.initShowOptimizeEvent(btnObjs);

                        tbodyObj.append(trObjs);

                        //$(btnObjs.get(0)).click();


                    }
                }

            });

        },
        getParamData: function() {
            var container = AutoTitleAdmin.init.getContainer();

            var paramData = {};

            paramData.title = container.find('.search-text').val();

            paramData.tbCid = container.find(".tb-category-select").val();
            paramData.sellerCid = container.find(".seller-category-select").val();

            paramData.itemStatus = container.find(".state-select").val();

            paramData.startScore = container.find('.start-score-text').val();
            paramData.endScore = container.find('.end-score-text').val();

            TM.AutoTitleUtil.util.addSortParams(container, paramData);

            return paramData;

        }
    }, AutoTitleAdmin.show);



    AutoTitleAdmin.event = AutoTitleAdmin.event || {};
    AutoTitleAdmin.event = $.extend({
        initShowOptimizeEvent: function(btnObjs) {
            btnObjs.unbind().click(function() {

                var thisBtnObj = $(this);

                var numIid = thisBtnObj.attr('numIid');
                var trObj = AutoTitleAdmin.event.getParentTrObj(thisBtnObj);
                var optimizeTrObj = AutoTitleAdmin.event.getOptimizeTrObj(thisBtnObj);
                var title = trObj.find('.item-title').html();

                var optConfig = {numIid: numIid, title: title};
                optimizeTrObj.showOptimizeDiv(optConfig);

                thisBtnObj.html('收起诊断');
                //thisBtnObj.removeClass('wide-sky-blue-btn');
                //thisBtnObj.addClass('wide-yellow-btn');

                AutoTitleAdmin.event.initCloseOptimizeEvent(thisBtnObj);
            });
        },
        initCloseOptimizeEvent: function(btnObjs) {
            btnObjs.unbind().click(function() {

                var thisBtnObj = $(this);

                var numIid = thisBtnObj.attr('numIid');
                var optimizeTrObj = AutoTitleAdmin.event.getOptimizeTrObj(thisBtnObj);
                optimizeTrObj.closeOptimizeDiv();

                thisBtnObj.html('诊断优化标题');
                thisBtnObj.removeClass('wide-yellow-btn');
                thisBtnObj.addClass('wide-sky-blue-btn');

                AutoTitleAdmin.event.initShowOptimizeEvent(thisBtnObj);
            });
        },
        getParentTrObj: function(thisBtnObj) {
            var trObj = thisBtnObj.parents('.item-tr');
            return trObj;
        },
        getOptimizeTrObj: function(thisBtnObj) {

            var trObj = AutoTitleAdmin.event.getParentTrObj(thisBtnObj);

            var optimizeTrObj = trObj.next();

            return optimizeTrObj;

        }
    }, AutoTitleAdmin.event);



    AutoTitleAdmin.submit = AutoTitleAdmin.submit || {};
    AutoTitleAdmin.submit = $.extend({
        doRecommendTitles: function() {
            AutoTitleAdmin.submit.doTitlesWithRecMode(0);
        },
        doGuanfangTitles: function() {
            AutoTitleAdmin.submit.doTitlesWithRecMode(1);
        },
        doGuanfangRecommendTitles: function() {
            AutoTitleAdmin.submit.doTitlesWithRecMode(2);
        },
        doTitlesWithRecMode: function(recMode) {

            $('.recommend-item-titles-dialog-div').remove();

            if (AutoTitleAdmin.show.totalCount > 0) {

            } else {
                alert('当前搜索条件的不存在宝贝，请换个条件再试！');
                return;
            }


            var dialogObj = $('#recommendTitleDialogTmpl').tmpl({});

            dialogObj.find('.radio-span').unbind().click(function() {
                $(this).parent().find('input[type="radio"]').click();
            });

            var dialogTitle = '一键自动标题';
            if (recMode == 0) {
                dialogTitle = '全部搜索宝贝推荐标题';
            } else if (recMode == 1) {
                dialogTitle = '全部搜索宝贝官方标题';
            } else if (recMode == 2) {
                dialogTitle = '全部搜索宝贝官方推荐标题';
            }

            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:400,
                width:600,
                title: dialogTitle,
                autoOpen: false,
                resizable: false,
                buttons:{
                    '确定': function() {

                        var submitParam = AutoTitleAdmin.submit.getSubmitParams(dialogObj, recMode);

                        if (submitParam === undefined || submitParam == null) {
                            return;
                        }

                        var totalCount = AutoTitleAdmin.show.totalCount;
                        if (confirm('当前搜索结果共' + totalCount + '个宝贝，确定要对这些宝贝进行标题优化？') == false) {
                            return;
                        }

                        $.ajax({
                            url : "/titles/batchChangeAll",
                            data : submitParam,
                            type : 'post',
                            success : function(dataJson) {


                                if (TM.AutoTitleUtil.util.judgeAjaxResult(dataJson) == false) {
                                    return;
                                }

                                var resHtml = '系统为您提交了<span style="font-weight: bold; color: #a10000;">优化宝贝标题的任务</span>，可能需要一段时间的执行，是否进入任务中心查看任务状态？';

                                TM.Alert.showDialog(resHtml, 400,300, function(){
                                        location.href = "/newAutotitle/titletask";
                                    }, function () {}, "提示");

                                dialogObj.dialog('close');
                            }
                        });


                    },'取消':function() {

                        dialogObj.dialog('close');
                    }
                }
            });

            dialogObj.dialog('open');

        },
        getSubmitParams: function(dialogObj, recMode) {
            var paramData = {};

            var searchParams = AutoTitleAdmin.show.currentParamData;
            if (searchParams === undefined || searchParams == null) {
                alert('系统异常，搜索条件出错！');
                return null;
            }

            paramData.sellerCatId = searchParams.sellerCid;
            paramData.itemCatId = searchParams.tbCid;
            if (searchParams.itemStatus == 0) {
                paramData.status = 1;
            } else if (searchParams.itemStatus == 1) {
                paramData.status = 0;
            } else {
                paramData.status = 2;
            }
            paramData.recMode = recMode;

            paramData.startScore = searchParams.startScore;
            paramData.endScore = searchParams.endScore;
            paramData.title = searchParams.title;
            paramData.newSearchRule = true;

            paramData['opt.allSale'] = dialogObj.find('input[name="sales-config-radio"]:checked').val();
            paramData['opt.keepBrand'] = dialogObj.find('input[name="brand-config-radio"]:checked').val();
            paramData['opt.keepSerial'] = dialogObj.find('input[name="serial-config-radio"]:checked').val();
            paramData['opt.noNumber'] = dialogObj.find('input[name="number-config-radio"]:checked').val();
            paramData['opt.noColor'] = dialogObj.find('input[name="color-config-radio"]:checked').val();
            paramData['opt.toAddPromote'] = dialogObj.find('input[name="promote-config-radio"]:checked').val();

            paramData['opt.fixedStart'] = dialogObj.find('input[name="fixedStart"]').val();
            paramData['opt.mustExcluded'] = dialogObj.find('input[name="mustExcluded"]').val();


            return paramData;


        }
    }, AutoTitleAdmin.submit);


})(jQuery,window));

