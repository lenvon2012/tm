

var TM = TM || {};

((function ($, window) {

    TM.WindowAdmin = TM.WindowAdmin || {};
    var WindowAdmin = TM.WindowAdmin;

    WindowAdmin.init = WindowAdmin.init || {};
    WindowAdmin.init = $.extend({

        doInit: function(container) {

            WindowAdmin.container = container;

            WindowAdmin.init.initTaobaoCategory();

            WindowAdmin.switchOp.initSwitch();
            WindowAdmin.config.initWindowConfig();
            WindowAdmin.status.initWindowStatus();


            TM.WindowList.init.doInit(container.find('.show-window-list-div'));
            TM.WindowLogs.init.doInit(container.find('.window-logs-div'));
            TM.MustWindow.init.doInit(container.find('.must-window-div'));
            TM.ExcludeWindow.init.doInit(container.find('.exclude-window-div'));


            var tabBtnObjs = container.find('.window-tab');

            tabBtnObjs.unbind().click(function() {
                tabBtnObjs.removeClass("select-tab");

                var thisBtnObj = $(this);
                thisBtnObj.addClass("select-tab");

                container.find(".window-tab-target-div").hide();

                var targetDivCss = thisBtnObj.attr('targetDiv');
                var targetDivObj = container.find('.' + targetDivCss);
                targetDivObj.show();

                if (targetDivCss == 'show-window-list-div') {
                    TM.WindowList.show.doShow();
                } else if (targetDivCss == 'window-logs-div') {
                    TM.WindowLogs.show.doShow();
                } else if (targetDivCss == 'must-window-div') {
                    TM.MustWindow.show.doShow();
                } else if (targetDivCss == 'exclude-window-div') {
                    TM.ExcludeWindow.show.doShow();
                }

            });


            container.find('.window-tab.select-tab').click();


        },
        initTaobaoCategory: function() {

            var container = WindowAdmin.init.getContainer();

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
        getContainer: function() {
            return WindowAdmin.container;
        }

    }, WindowAdmin.init);



    WindowAdmin.switchOp = WindowAdmin.switchOp || {};
    WindowAdmin.switchOp = $.extend({
        initSwitch: function() {
            var container = WindowAdmin.init.getContainer();

            $.ajax({
                url : "/windows/isOn",
                data : {},
                type : 'post',
                success : function(dataJson) {

                    if (TM.AutoTitleUtil.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }

                    var isOn = TM.AutoTitleUtil.util.getAjaxResultJson(dataJson);
                    var spanObj = container.find('.window-status-span');
                    var btnObj = container.find('.switch-window-btn');

                    if (isOn == true) {
                        spanObj.removeClass('red-bold');
                        spanObj.addClass('green-bold');
                        spanObj.html('当前自动橱窗已开启');
                        btnObj.removeClass('long-green-btn');
                        btnObj.addClass('wide-yellow-btn');
                        btnObj.html('关闭自动橱窗');
                        btnObj.unbind().click(function() {

                            if (confirm("确定要关闭自动橱窗？") == false) {
                                return;
                            }

                            $.ajax({
                                url : "/windows/turn",
                                data : {isOn: false},
                                type : 'post',
                                success : function(dataJson) {


                                    if (TM.AutoTitleUtil.util.judgeAjaxResult(dataJson) == false) {
                                        alert('橱窗关闭失败，请联系我们！')
                                        return;
                                    }
                                    //alert('橱窗关闭成功！')
                                    WindowAdmin.switchOp.initSwitch();
                                }
                            });


                        });
                    } else {
                        spanObj.removeClass('green-bold');
                        spanObj.addClass('red-bold');
                        spanObj.html('当前自动橱窗尚未开启');
                        btnObj.removeClass('wide-yellow-btn');
                        btnObj.addClass('long-green-btn');
                        btnObj.html('开启自动橱窗');

                        btnObj.unbind().click(function() {

                            $.ajax({
                                url : "/windows/turn",
                                data : {isOn: true},
                                type : 'post',
                                success : function(dataJson) {


                                    if (TM.AutoTitleUtil.util.judgeAjaxResult(dataJson) == false) {
                                        alert('橱窗关闭失败，请联系我们！')
                                        return;
                                    }
                                    //alert('橱窗关闭成功！')
                                    WindowAdmin.switchOp.initSwitch();
                                }
                            });
                        });
                    }


                }
            });
        }
    }, WindowAdmin.switchOp);



    WindowAdmin.config = WindowAdmin.config || {};
    WindowAdmin.config = $.extend({
        initWindowConfig: function() {
            var container = WindowAdmin.init.getContainer();

            WindowAdmin.config.showConfig();

            container.find('.save-config-btn').unbind().click(function() {

                var config = {};
                config.priorSaleNum = container.find('.sales-order-input').val();
                if (config.priorSaleNum > 0) {
                    config.enableSaleNum = true;
                } else {
                    config.enableSaleNum = false;
                }

                config.minInstockNum = container.find('.min-stock-input').val();
                if (config.minInstockNum > 0) {
                    config.enableInstockNum = true;
                } else {
                    config.enableInstockNum = false;
                }

                if (confirm('确定要修改自动橱窗配置？') == false) {
                    return;
                }
                $.ajax({
                    url : "/windows/submitNewConfig",
                    data : {config: config},
                    type : 'post',
                    success : function(dataJson) {
                        if (TM.AutoTitleUtil.util.judgeAjaxResult(dataJson) == false) {
                            return;
                        }

                        alert('橱窗配置修改成功！');

                        WindowAdmin.config.showConfig();
                    }
                });
            });

        },
        showConfig: function() {
            var container = WindowAdmin.init.getContainer();


            $.ajax({
                url : "/windows/getConfig",
                data : {},
                type : 'post',
                success : function(dataJson) {
                    if(dataJson == "获取优先推荐销量数出错，请联系客服") {
                        alert(dataJson);
                        return;
                    }

                    var configJson = dataJson;
                    container.find('.sales-order-input').val(configJson.priorSaleNum);
                    container.find('.min-stock-input').val(configJson.minInstockNum);
                }
            });
        }
    }, WindowAdmin.config);


    WindowAdmin.status = WindowAdmin.status || {};
    WindowAdmin.status = $.extend({
        initWindowStatus: function() {
            var container = WindowAdmin.init.getContainer();

            WindowAdmin.status.showWindowStatus();

            container.find('.re-execute-window-btn').unbind().click(function() {

                if (confirm('确定要重新推荐橱窗？') == false) {
                    return;
                }
                $.ajax({
                    url : "/windows/immediateRecommend",
                    data : {},
                    type : 'post',
                    success : function(dataJson) {

                        alert('橱窗重新推荐成功！');
                        window.location.reload();
                    }
                });

            });

        },
        showWindowStatus: function() {
            var container = WindowAdmin.init.getContainer();

            $.ajax({
                url : "/windows/base",
                data : {},
                type : 'post',
                success : function(dataJson) {
                    var statusJson = dataJson;

                    container.find('.total-window-num').html(statusJson.totalWindowCount);
                    container.find('.used-window-num').html(statusJson.onShowItemCount);
                    container.find('.left-window-num').html(statusJson.remainWindowCount);
                    container.find('.sales-item-num').html(statusJson.onSaleCount);
                    container.find('.must-window-item-num').html(statusJson.mustCount);
                    container.find('.exclude-window-item-num').html(statusJson.excludeCount);

                }
            });

        },
        refreshWindowStatus: function() {
            WindowAdmin.status.showWindowStatus();
        }
    }, WindowAdmin.status);


})(jQuery, window));



((function ($, window) {

    TM.WindowList = TM.WindowList || {};
    var WindowList = TM.WindowList;

    WindowList.init = WindowList.init || {};
    WindowList.init = $.extend({
        doInit: function(container) {

            WindowList.container = container;

            TM.AutoTitleUtil.util.setSortTdEvent(container, function() {
                WindowList.show.doShow();
            });
        },
        getContainer: function() {
            return WindowList.container;
        }
    }, WindowList.init);



    WindowList.show = WindowList.show || {};
    WindowList.show = $.extend({
        doShow: function() {

            var container = WindowList.init.getContainer();

            var paramData = WindowList.show.getParamData();

            if (paramData === undefined || paramData == null) {
                return;
            }

            $.ajax({
                url : "/windows/listOnItems",
                data : {},
                type : 'post',
                success : function(dataJson) {


                    var tbodyObj = container.find('.window-list-table tbody');
                    tbodyObj.html('');

                    var windowJsonArray = dataJson;
                    if (windowJsonArray === undefined || windowJsonArray == null) {
                        windowJsonArray = [];
                    }
                    $(windowJsonArray).each(function(index, windowJson) {
                        if (windowJson.onShowWindowReason == '必推宝贝') {
                            windowJson.onShowWindowReason = '必推荐的宝贝';
                        } else if (windowJson.onShowWindowReason == '销量优先') {
                            windowJson.onShowWindowReason = '高销量的宝贝';
                        } else if (windowJson.onShowWindowReason == '下架时间优先') {
                            windowJson.onShowWindowReason = '快下架的宝贝';
                        }
                    });

                    var trObjs = $('#windowListRowTmpl').tmpl(windowJsonArray);

                    tbodyObj.append(trObjs);

                    trObjs.find('.cancel-window-btn').unbind().click(function() {

                        var btnObj = $(this);

                        var numIid = btnObj.attr('numIid');
                        if (confirm('确定要取消该宝贝的橱窗推荐？') == false) {
                            return;
                        }

                        $.ajax({
                            url : "/windows/dropOnShow",
                            data : {numIid: numIid},
                            type : 'post',
                            success : function(dataJson) {
                                if (TM.AutoTitleUtil.util.judgeAjaxResult(dataJson) == false) {
                                    return;
                                }

                                alert('橱窗取消成功！');
                                WindowList.show.doRefresh();
                                TM.WindowAdmin.status.refreshWindowStatus();
                            }
                        });

                    });


                }
            });
        },
        doRefresh: function() {
            WindowList.show.doShow();
        },
        getParamData: function() {
            var container = WindowList.init.getContainer();

            var paramData = {};

            TM.AutoTitleUtil.util.addSortParams(container, paramData);

            return paramData;

        }
    }, WindowList.show);



})(jQuery, window));





((function ($, window) {

    TM.WindowLogs = TM.WindowLogs || {};
    var WindowLogs = TM.WindowLogs;

    WindowLogs.init = WindowLogs.init || {};
    WindowLogs.init = $.extend({
        doInit: function(container) {

            WindowLogs.container = container;

            container.find(".window-logs-search-text").unbind().keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    container.find(".search-window-logs-btn").click();
                }
            });
            container.find(".search-window-logs-btn").unbind().click(function() {
                WindowLogs.show.doShow();
            });

        },
        getContainer: function() {
            return WindowLogs.container;
        }
    }, WindowLogs.init);



    WindowLogs.show = WindowLogs.show || {};
    WindowLogs.show = $.extend({
        doShow: function() {

            var container = WindowLogs.init.getContainer();

            var paramData = WindowLogs.show.getParamData();

            if (paramData === undefined || paramData == null) {
                return;
            }

            container.find(".log-paging-div").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: paramData,
                    dataType: 'json',
                    url: '/windows/listOpLogs',
                    callback: function(dataJson){

                        var tbodyObj = container.find('.window-logs-table tbody');
                        tbodyObj.html('');


                        var logJsonArray = dataJson.res;

                        if (logJsonArray === undefined || logJsonArray == null || logJsonArray.length <= 0) {
                            tbodyObj.html('<tr><td colspan="5" style="padding: 10px 0px;">当前暂无橱窗推荐日志！</td> </tr>');
                            return;
                        }


                        var trObjs = $('#windowLogRowTmpl').tmpl(logJsonArray);

                        tbodyObj.append(trObjs);


                    }
                }

            });

        },
        getParamData: function() {
            var container = WindowLogs.init.getContainer();

            var paramData = {};

            paramData.title = container.find('.window-logs-search-text').val();

            return paramData;

        }
    }, WindowLogs.show);



})(jQuery, window));





((function ($, window) {

    TM.MustWindow = TM.MustWindow || {};
    var MustWindow = TM.MustWindow;

    MustWindow.init = MustWindow.init || {};
    MustWindow.init = $.extend({
        doInit: function(container) {

            MustWindow.container = container;

            container.find(".must-search-text").unbind().keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    container.find(".search-must-window-btn").click();
                }
            });
            container.find(".search-must-window-btn").unbind().click(function() {
                MustWindow.show.doShow();
            });
            container.find("select").unbind().change(function() {
                MustWindow.show.doShow();
            });

            TM.AutoTitleUtil.util.setSortTdEvent(container, function() {
                MustWindow.show.doShow();
            });


            container.find(".all-item-check").click(function() {
                var isChecked = $(this).is(":checked");
                var checkObjs = container.find(".item-checkbox");
                checkObjs.attr("checked", isChecked);
            });



            var getCheckedNumIidArray = function() {
                var numIidArray = [];
                var checkObjs = container.find('.item-checkbox:checked');
                checkObjs.each(function() {
                    numIidArray.push($(this).attr('numIid'));
                });

                return numIidArray;
            }

            container.find('.batch-add-must-btn').unbind().click(function() {
                var numIidArray = getCheckedNumIidArray();


                MustWindow.submit.addMustItems(numIidArray);

            });

            container.find('.batch-cancel-must-btn').unbind().click(function() {
                var numIidArray = getCheckedNumIidArray();


                MustWindow.submit.cancelMustItems(numIidArray);

            });


        },
        getContainer: function() {
            return MustWindow.container;
        }
    }, MustWindow.init);



    MustWindow.show = MustWindow.show || {};
    MustWindow.show = $.extend({
        targetCurrentPage: 1,
        doShow: function() {

            MustWindow.show.doSearch(1);
        },
        doRefresh: function() {
            MustWindow.show.doSearch(MustWindow.show.targetCurrentPage);
        },
        doSearch: function(currentPage) {

            var container = MustWindow.init.getContainer();

            if (currentPage < 1) {
                currentPage = 1;
            }

            MustWindow.show.targetCurrentPage = currentPage;

            var paramData = MustWindow.show.getParamData();

            if (paramData === undefined || paramData == null) {
                return;
            }

            container.find(".must-paging-div").tmpage({
                currPage: currentPage,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: paramData,
                    dataType: 'json',
                    url: '/windowsui/queryMustItems',
                    callback: function(dataJson){

                        MustWindow.show.targetCurrentPage = dataJson.pn;//记录当前页

                        var tbodyObj = container.find('.must-window-table tbody');
                        tbodyObj.html('');


                        var windowJsonArray = dataJson.res;

                        if (windowJsonArray === undefined || windowJsonArray == null || windowJsonArray.length <= 0) {
                            tbodyObj.html('<tr><td colspan="6" style="padding: 10px 0px;">当前暂无满足条件的宝贝！</td> </tr>');
                            return;
                        }

                        $(windowJsonArray).each(function(index, windowJson) {

                            var trObj = MustWindow.row.createRow(index, windowJson);

                            tbodyObj.append(trObj);
                        });



                    }
                }

            });

        },
        getParamData: function() {
            var container = MustWindow.init.getContainer();

            var paramData = {};

            paramData.title = container.find('.must-search-text').val();

            paramData.tbCid = container.find(".tb-category-select").val();
            paramData.sellerCid = container.find(".seller-category-select").val();

            paramData.mustStatus = container.find(".must-status-select").val();

            TM.AutoTitleUtil.util.addSortParams(container, paramData);

            return paramData;

        }
    }, MustWindow.show);


    MustWindow.row = MustWindow.row || {};
    MustWindow.row = $.extend({
        createRow: function(index, windowJson) {

            var itemJson = windowJson.item;
            var numIid = itemJson.numIid;
            if (numIid === undefined || numIid == null || numIid <= 0) {
                numIid = itemJson.id;
                itemJson.numIid = itemJson.id;
            }

            var trObj = $('#mustWindowRowTmpl').tmpl(itemJson);

            var opTdObj = trObj.find('.op-td');

            if (windowJson.exclude == true) {
                opTdObj.html('已加入排除宝贝');
                trObj.find('.item-checkbox').attr('disabled', 'disabled');
                trObj.find('.item-checkbox').removeClass('item-checkbox');
            } else {
                if (windowJson.must == true) {
                    var btnObj = $('<span class="tmbtn yellow-btn cancel-must-btn">取消必推</span> ')
                    opTdObj.html(btnObj);
                    btnObj.unbind().click(function() {
                        var numIid = itemJson.numIid;
                        var numIidArray = [numIid];
                        MustWindow.submit.cancelMustItems(numIidArray);
                    });
                } else {
                    var btnObj = $('<span class="tmbtn short-green-btn add-must-btn">加入必推</span> ')
                    opTdObj.html(btnObj);
                    btnObj.unbind().click(function() {
                        var numIid = itemJson.numIid;
                        var numIidArray = [numIid];
                        MustWindow.submit.addMustItems(numIidArray);
                    });
                }
            }

            return trObj;
        }
    }, MustWindow.row);


    MustWindow.submit = MustWindow.submit || {};
    MustWindow.submit = $.extend({

        addMustItems: function(numIidArray) {


            if (numIidArray === undefined || numIidArray == null || numIidArray.length <= 0) {
                alert('请先选择要加入必推的宝贝！');
                return;
            }

            if (confirm('确定要加入必推宝贝？') == false) {
                return;
            }


            var numIids = numIidArray.join(',');

            $.ajax({
                url : "/windows/addMustItemsWithFilter",
                data : {numIids: numIids},
                type : 'post',
                success : function(dataJson) {
                    if (TM.AutoTitleUtil.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }

                    alert('必推宝贝添加成功！');
                    MustWindow.show.doRefresh();
                    TM.WindowAdmin.status.refreshWindowStatus();
                }
            });

        },
        cancelMustItems: function(numIidArray) {


            if (numIidArray === undefined || numIidArray == null || numIidArray.length <= 0) {
                alert('请先选择要取消必推的宝贝！');
                return;
            }

            if (confirm('确定要取消必推宝贝？') == false) {
                return;
            }

            var numIids = numIidArray.join(',');

            $.ajax({
                url : "/windows/removeMustItems",
                data : {numIids: numIids},
                type : 'post',
                success : function(dataJson) {
                    if (TM.AutoTitleUtil.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }

                    alert('必推宝贝取消成功！');
                    MustWindow.show.doRefresh();
                    TM.WindowAdmin.status.refreshWindowStatus();
                }
            });
        }

    }, MustWindow.submit);



})(jQuery, window));





((function ($, window) {

    TM.ExcludeWindow = TM.ExcludeWindow || {};
    var ExcludeWindow = TM.ExcludeWindow;

    ExcludeWindow.init = ExcludeWindow.init || {};
    ExcludeWindow.init = $.extend({
        doInit: function(container) {

            ExcludeWindow.container = container;

            container.find(".exclude-search-text").unbind().keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    container.find(".search-exclude-window-btn").click();
                }
            });
            container.find(".search-exclude-window-btn").unbind().click(function() {
                ExcludeWindow.show.doShow();
            });
            container.find("select").unbind().change(function() {
                ExcludeWindow.show.doShow();
            });

            TM.AutoTitleUtil.util.setSortTdEvent(container, function() {
                ExcludeWindow.show.doShow();
            });


            container.find(".all-item-check").click(function() {
                var isChecked = $(this).is(":checked");
                var checkObjs = container.find(".item-checkbox");
                checkObjs.attr("checked", isChecked);
            });



            var getCheckedNumIidArray = function() {
                var numIidArray = [];
                var checkObjs = container.find('.item-checkbox:checked');
                checkObjs.each(function() {
                    numIidArray.push($(this).attr('numIid'));
                });

                return numIidArray;
            }

            container.find('.batch-add-exclude-btn').unbind().click(function() {
                var numIidArray = getCheckedNumIidArray();


                ExcludeWindow.submit.addExcludeItems(numIidArray);

            });

            container.find('.batch-cancel-exclude-btn').unbind().click(function() {
                var numIidArray = getCheckedNumIidArray();


                ExcludeWindow.submit.cancelExcludeItems(numIidArray);

            });


        },
        getContainer: function() {
            return ExcludeWindow.container;
        }
    }, ExcludeWindow.init);



    ExcludeWindow.show = ExcludeWindow.show || {};
    ExcludeWindow.show = $.extend({
        targetCurrentPage: 1,
        doShow: function() {

            ExcludeWindow.show.doSearch(1);
        },
        doRefresh: function() {
            ExcludeWindow.show.doSearch(ExcludeWindow.show.targetCurrentPage);
        },
        doSearch: function(currentPage) {

            var container = ExcludeWindow.init.getContainer();

            if (currentPage < 1) {
                currentPage = 1;
            }

            ExcludeWindow.show.targetCurrentPage = currentPage;

            var paramData = ExcludeWindow.show.getParamData();

            if (paramData === undefined || paramData == null) {
                return;
            }

            container.find(".exclude-paging-div").tmpage({
                currPage: currentPage,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: paramData,
                    dataType: 'json',
                    url: '/windowsui/queryExcludeItems',
                    callback: function(dataJson){

                        ExcludeWindow.show.targetCurrentPage = dataJson.pn;//记录当前页

                        var tbodyObj = container.find('.exclude-window-table tbody');
                        tbodyObj.html('');


                        var windowJsonArray = dataJson.res;

                        if (windowJsonArray === undefined || windowJsonArray == null || windowJsonArray.length <= 0) {
                            tbodyObj.html('<tr><td colspan="6" style="padding: 10px 0px;">当前暂无满足条件的宝贝！</td> </tr>');
                            return;
                        }

                        $(windowJsonArray).each(function(index, windowJson) {

                            var trObj = ExcludeWindow.row.createRow(index, windowJson);

                            tbodyObj.append(trObj);
                        });



                    }
                }

            });

        },
        getParamData: function() {
            var container = ExcludeWindow.init.getContainer();

            var paramData = {};

            paramData.title = container.find('.exclude-search-text').val();

            paramData.tbCid = container.find(".tb-category-select").val();
            paramData.sellerCid = container.find(".seller-category-select").val();

            paramData.excludeStatus = container.find(".exclude-status-select").val();

            TM.AutoTitleUtil.util.addSortParams(container, paramData);

            return paramData;

        }
    }, ExcludeWindow.show);


    ExcludeWindow.row = ExcludeWindow.row || {};
    ExcludeWindow.row = $.extend({
        createRow: function(index, windowJson) {

            var itemJson = windowJson.item;
            var numIid = itemJson.numIid;
            if (numIid === undefined || numIid == null || numIid <= 0) {
                numIid = itemJson.id;
                itemJson.numIid = itemJson.id;
            }

            var trObj = $('#excludeWindowRowTmpl').tmpl(itemJson);

            var opTdObj = trObj.find('.op-td');

            if (windowJson.must == true) {
                opTdObj.html('已加入必推宝贝');
                trObj.find('.item-checkbox').attr('disabled', 'disabled');
                trObj.find('.item-checkbox').removeClass('item-checkbox');
            } else {
                if (windowJson.exclude == true) {
                    var btnObj = $('<span class="tmbtn yellow-btn cancel-exclude-btn">取消排除</span> ')
                    opTdObj.html(btnObj);
                    btnObj.unbind().click(function() {
                        var numIid = itemJson.numIid;
                        var numIidArray = [numIid];
                        ExcludeWindow.submit.cancelExcludeItems(numIidArray);
                    });
                } else {
                    var btnObj = $('<span class="tmbtn short-green-btn add-exclude-btn">加入排除</span> ')
                    opTdObj.html(btnObj);
                    btnObj.unbind().click(function() {
                        var numIid = itemJson.numIid;
                        var numIidArray = [numIid];
                        ExcludeWindow.submit.addExcludeItems(numIidArray);
                    });
                }
            }

            return trObj;
        }
    }, ExcludeWindow.row);


    ExcludeWindow.submit = ExcludeWindow.submit || {};
    ExcludeWindow.submit = $.extend({

        addExcludeItems: function(numIidArray) {


            if (numIidArray === undefined || numIidArray == null || numIidArray.length <= 0) {
                alert('请先选择要加入排除的宝贝！');
                return;
            }

            if (confirm('确定要加入排除宝贝？') == false) {
                return;
            }


            var numIids = numIidArray.join(',');

            $.ajax({
                url : "/windows/addExcludeItemsWithFilter",
                data : {numIids: numIids},
                type : 'post',
                success : function(dataJson) {
                    if (TM.AutoTitleUtil.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }

                    alert('排除宝贝添加成功！');
                    ExcludeWindow.show.doRefresh();
                    TM.WindowAdmin.status.refreshWindowStatus();
                }
            });

        },
        cancelExcludeItems: function(numIidArray) {


            if (numIidArray === undefined || numIidArray == null || numIidArray.length <= 0) {
                alert('请先选择要取消排除的宝贝！');
                return;
            }

            if (confirm('确定要取消排除宝贝？') == false) {
                return;
            }

            var numIids = numIidArray.join(',');

            $.ajax({
                url : "/windows/removeExcludeItems",
                data : {numIids: numIids},
                type : 'post',
                success : function(dataJson) {
                    if (TM.AutoTitleUtil.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }

                    alert('排除宝贝取消成功！');
                    ExcludeWindow.show.doRefresh();
                    TM.WindowAdmin.status.refreshWindowStatus();
                }
            });
        }

    }, ExcludeWindow.submit);



})(jQuery, window));