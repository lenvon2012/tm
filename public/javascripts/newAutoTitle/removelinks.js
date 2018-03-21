
var TM = TM || {};

((function ($, window) {


    TM.RemoveLinks = TM.RemoveLinks || {};
    var RemoveLinks = TM.RemoveLinks;


    RemoveLinks.init = RemoveLinks.init || {};
    RemoveLinks.init = $.extend({
        doInit: function(container) {

            RemoveLinks.container = container;


            TM.ItemLinks.init.doInit(container.find(".item-link-list-div"));
            TM.LinksConfig.init.doInit(container.find(".all-link-config-div"));

            RemoveLinks.initQtip.doInit();


            var tabObjs = container.find(".remove-links-tab-btn");

            tabObjs.click(function() {
                var tabObj = $(this);
                if (tabObj.hasClass("select-tab")) {
                    return;
                }

                container.find(".tab-target-div").hide();

                tabObjs.removeClass("select-tab");
                tabObj.addClass("select-tab");

                var targetDivCss = tabObj.attr("target");
                container.find("." + targetDivCss).show();

                if (targetDivCss == 'item-link-list-div') {
                    TM.ItemLinks.show.doShow();
                } else if (targetDivCss == 'all-link-config-div') {
                    TM.LinksConfig.show.doShow();
                } else {
                    alert('系统出现异常，选项类型出错，请联系我们！');
                }
            });


            TM.ItemLinks.show.doShow();

        },
        getContainer: function() {
            return RemoveLinks.container;
        }
    }, RemoveLinks.init);



    RemoveLinks.initQtip = RemoveLinks.initQtip || {};
    RemoveLinks.initQtip = $.extend({
        doInit: function() {

            var initCallback = function(btnObj, text) {
                btnObj.qtip({
                    content: {
                        text: text
                    },
                    position: {
                        //at: "top center",
                        //my: 'top left'
                        corner: {
                            tooltip: 'bottomMiddle',
                            target: 'topMiddle'
                        }
                    },
                    show: {
                        /*when: {
                         event: 'mouseover'
                         },*/
                        ready:false
                    },
                    style: {
                        border: {
                            width: 3,
                            radius: 5
                        },
                        padding: 10,
                        textAlign: 'center',
                        tip: true,
                        name: 'cream'
                    }
                });
            };


            var container = RemoveLinks.init.getContainer();
            initCallback(container.find(".remove-select-item-btn"),
                '选中宝贝后，删除这些宝贝详情页中的外链。');

            initCallback(container.find(".remove-all-item-btn"),
                '删除<span class="bold">当前搜索结果</span>下所有宝贝详情页中的外链。');

            initCallback(container.find(".set-select-keep-btn"),
                '选中的链接都是有用的，我要在详情页中保留这些链接。');

            initCallback(container.find(".set-all-keep-btn"),
                '<span class="bold">当前搜索结果</span>下的链接都是有用的，我要在详情页中保留这些链接。');

            initCallback(container.find(".set-select-remove-btn"),
                '选中的链接都是没用的，都是需要从详情页中去除的。');

            initCallback(container.find(".set-all-remove-btn"),
                '<span class="bold">当前搜索结果</span>下的链接都是没用的，都是需要从详情页中去除的。');

        }
    }, RemoveLinks.initQtip);


})(jQuery, window));




((function ($, window) {


    TM.ItemLinks = TM.ItemLinks || {};
    var ItemLinks = TM.ItemLinks;

    var pn = 1;

    ItemLinks.init = ItemLinks.init || {};
    ItemLinks.init = $.extend({
        doInit: function(container) {

            ItemLinks.container = container;

            ItemLinks.init.initTaobaoCategory();
            ItemLinks.init.initSellerCategory();

            //添加事件
            container.find(".tb-category-select").change(function() {
                ItemLinks.show.doShow();
            });
            container.find(".seller-category-select").change(function() {
                ItemLinks.show.doShow();
            });
            container.find(".state-select").change(function() {
                ItemLinks.show.doShow();
            });
            container.find(".search-text").keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    container.find(".search-btn").click();
                }
            });
            container.find(".search-btn").click(function() {
                ItemLinks.show.doShow();
            });

            container.find(".all-item-check").click(function() {
                var isChecked = $(this).is(":checked");
                var checkObjs = container.find(".item-checkbox");
                checkObjs.attr("checked", isChecked);
            });


            container.find(".remove-select-item-btn").click(function() {

                var checkedObjs = container.find(".item-checkbox:checked");
                var selectNumIidArray = [];

                checkedObjs.each(function() {
                    var checkObj = $(this);
                    selectNumIidArray[selectNumIidArray.length] = checkObj.attr("numIid");


                });

                ItemLinks.submit.removeSelectLinks(selectNumIidArray);

            });

            container.find(".remove-all-item-btn").click(function() {

                ItemLinks.submit.removeAllLinks();

            });

            container.find(".checkLinkBtn").click(function() {
                ItemLinks.init.RmConfirmDialog();
            });


        },
        RmConfirmDialog: function(){
            var dialogObj = $(".dialog-div");
            dialogObj.empty();
            var html = '' +
                '<div class="dialog-div" style="background:#fff;">' +
                '   <table class="busSearch" style="margin-top: 10px;">' +
                '       <thead><tr>' +
//            '           <td>宝贝主图</td>' +
//            '           <td>宝贝标题</td>' +
                '           <th style="width:630px;font-size:14px;">发现外链</th>' +
                '           <th><div><input type="checkbox" tag="rmCheckAll" class="rmCheckAll" id="rmCheckAll" style="width:13px;" /><label class="remove-all-label" for="rmCheckAll">去除</label></div></div></th>' +
                '       </tr></thead>' +
                '       <tbody class="dialog-tbody">' +
                '       </tbody>' +
                '   </table>' +
                '   <div class="loading-div" style="text-align: center;padding: 20px;font-weight: bold;color: red;"><img src="/public/images/fenxiao/loading.gif" /><span class="loading-text">正在载入中</span></div>' +
                '</div> ' +
                '';

            dialogObj = $(html);

            $("body").append(dialogObj);

            var title = "选择外链处理方式";
            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:450,
                width:750,
                title: title,
                autoOpen: false,
                resizable: false,
                buttons:{'确定':function() {
                    var ids = "";
                    $('.rmSubCheck:checked').each(function(){
                        var id = $(this).parent().attr('tag');
                        ids += id + ",";
                    });
                    $.post("/fenxiao/updateLinkAction", {ids:ids, actionType: 1}, function(data){
                        if(data.success){
                            window.location.reload();
                        }
                    });
                },'取消':function(){
                    $(this).dialog('close');
                }}
            });

            dialogObj.find(".rmCheckAll").click(function(){
                $('input[tag="rmSubCheck"]').attr("checked",this.checked);
            });

            ItemLinks.init.checkLinksReq(dialogObj);
        },
        checkLinksReq: function(dialogObj){
            $.get("/fenxiao/checkLinks", {pn: pn}, function(data){
                var tbody = dialogObj.find(".dialog-tbody");

                if(data.res && data.res.length > 0) {
                    $(data.res).each(function(i,one){
                        var chk = one.action == 1 ? 'checked="checked" ' : '';
                        tbody.append('<tr><td><a href="'+one.link+'" title="'+one.link+'" target="_blank"><div style="word-break: break-all;">'+one.link+'</div></a></td>' +
                            '<td tag="'+one.id+'" style="text-align:center;"><input style="width:13px;" type="checkbox" tag="rmSubCheck" class="rmSubCheck" '+chk+'/></td></tr>');
//                        '<td tag="'+one.id+'"><button class="remove-btn btn btn-danger">去除</button><button class="ignore-btn btn btn-info" style="margin-left:5px;">忽略</button></td></tr>');
                    });
                }

                dialogObj.find('.rmSubCheck').click(function(){
                    var $subBox = dialogObj.find("input[tag='rmSubCheck']");
                    dialogObj.find(".rmCheckAll").attr("checked",$subBox.length == dialogObj.find("input[tag='rmSubCheck']:checked").length ? true : false);
                });

//            tbody.find(".remove-btn").click(function(){
//                var id = $(this).parent().attr("tag");
//                var cur = $(this).parent().parent();
//                $.post("/fenxiao/updateLinkAction", {id:id, actionType: 1}, function(data){
//                    if(data.success){
//                        if(cur.parent().find("tr").length == 1){
//                            tbody.append('<tr><td colspan="2" style="text-align: center;font-size: 14px;line-height: 40px;">设置完成，请点击确定开始</td></tr>');
//                        }
//                        cur.remove();
//                    }
//                });
//            });
//            tbody.find(".ignore-btn").click(function(){
//                var id = $(this).parent().attr("tag");
//                var cur = $(this).parent().parent();
//                $.post("/fenxiao/updateLinkAction", {id:id, actionType: 0}, function(data){
//                    if(data.success){
//                        if(cur.parent().find("tr").length == 1){
//                            tbody.append('<tr><td colspan="2" style="text-align: center;font-size: 14px;line-height: 40px;">设置完成，请点击确定开始</td></tr>');
//                        }
//                        cur.remove();
//                    }
//                });
//            });
                dialogObj.dialog('open');
                TM.Loading.init.hidden();

                if(data.count == 10){
                    pn++;
                    ItemLinks.init.checkLinksReq(dialogObj);
                    dialogObj.find(".loading-text").html("正在处理第" + data.pn * 10 + "到" + (data.pn+1)*10 +"个宝贝");
                } else {
                    dialogObj.find(".loading-div").hide();

                    var l = dialogObj.find(".dialog-tbody tr").length;
                    if(l == 0){
                        dialogObj.find(".dialog-tbody").append('<tr><td colspan="3" style="text-align: center;padding: 20px;font-size: 14px;color: blue;">恭喜您，没有发现外链哦</td></tr>');
                    }
                }
            });
        },
        initTaobaoCategory: function() {

            var container = ItemLinks.init.getContainer();

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

            var container = ItemLinks.init.getContainer();

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
            return ItemLinks.container;
        }
    }, ItemLinks.init);


    ItemLinks.show = ItemLinks.show || {};
    ItemLinks.show = $.extend({
        targetCurrentPage: 1,
        currentParamData: null,
        doShow: function() {
            ItemLinks.show.doSearch(1);
        },
        doRefresh: function() {
            ItemLinks.show.doSearch(ItemLinks.show.targetCurrentPage);
        },
        doSearch: function(currentPage) {

            if (currentPage < 1) {
                currentPage = 1;
            }

            ItemLinks.show.targetCurrentPage = currentPage;

            var paramData = ItemLinks.show.getParamData();
            if (paramData === undefined || paramData == null) {
                return;
            }

            ItemLinks.show.currentParamData = paramData;

            var container = ItemLinks.init.getContainer();

            var tbodyObj = container.find('.item-table .item-tbody');

            tbodyObj.html('');

            container.find(".item-paging-div").tmpage({
                currPage: currentPage,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: paramData,
                    dataType: 'json',
                    url: '/removelinks/searchLinkItems',
                    callback: function(dataJson){
                        ItemLinks.show.targetCurrentPage = dataJson.pn;//记录当前页

                        tbodyObj.html('');


                        var itemArray = dataJson.res;

                        if (itemArray === undefined || itemArray == null || itemArray.length <= 0) {
                            tbodyObj.html("");
                            tbodyObj.html('<tr><td colspan="4" style="padding: 10px 0px;">亲，您当前宝贝详情页中不存在外链。</td> </tr>');
                            return;
                        }


                        $(itemArray).each(function(index, itemJson) {

                            ItemLinks.row.initItemProperty(index, itemJson);
                        });

                        var trObjArray = $('#linkItemRow').tmpl(itemArray);

                        tbodyObj.html(trObjArray);
                    }
                }

            });

        },
        getParamData: function() {

            var paramData = {};
            var container = ItemLinks.init.getContainer();

            var title = container.find(".search-text").val();
            var status = container.find(".state-select").val();
            var catId = container.find(".tb-category-select").val();
            var sellerCatId = container.find(".seller-category-select").val();


            paramData.title = title;
            paramData.status = status;
            paramData.catId = catId;
            paramData.sellerCatId = sellerCatId;

            return paramData;

        }
    }, ItemLinks.show);


    ItemLinks.row = ItemLinks.row || {};
    ItemLinks.row = $.extend({
        initItemProperty: function(index, itemJson) {
            var numIid = itemJson.numIid;
            if (numIid === undefined || numIid == null || numIid <= 0) {
                numIid = itemJson.id;
                itemJson.numIid = numIid;
            }

            itemJson.itemLink = "http://item.taobao.com/item.htm?id=" + itemJson.numIid;

            var linkJsonArray = [];

            var linkActionMap = itemJson.linkActionMap;
            if (linkActionMap === undefined || linkActionMap == null) {
                linkActionMap = {};
            }
            for (var linkUrl in linkActionMap) {
                var linkJson = {};
                linkJson.linkUrl = linkUrl;
                linkJson.action = linkActionMap[linkUrl];

                linkJsonArray[linkJsonArray.length] = linkJson;
            }

            itemJson.linkJsonArray = linkJsonArray;

        }
    }, ItemLinks.row);


    ItemLinks.submit = ItemLinks.submit || {};
    ItemLinks.submit = $.extend({
        removeSelectLinks: function(selectNumIidArray) {
            if (selectNumIidArray === undefined || selectNumIidArray == null || selectNumIidArray.length <= 0) {
                alert("请先选择要删除外链的宝贝！");
                return;
            }

            if (confirm("确定要删除选中宝贝的外链？") == false) {
                return;
            }

            var numIids = selectNumIidArray.join(",");

            $.ajax({
                url : "/removelinks/doRemoveSelectItemLinks",
                data : {numIids: numIids},
                type : 'post',
                success : function(dataJson) {

                    if (TM.AutoTitleUtil.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }

                    var removeRes = TM.AutoTitleUtil.util.getAjaxResultJson(dataJson);

                    TM.AutoTitleUtil.errors.showErrors(removeRes, function() {

                        ItemLinks.show.doRefresh();

                        return;

                    }, function() {

                        ItemLinks.show.doRefresh();

                        return;
                    });

                }
            });
        },
        removeAllLinks: function() {

            if (confirm("确定要删除当前搜索结果下所有宝贝的外链？") == false) {
                return;
            }

            var submitParam = {};

            submitParam = $.extend({}, submitParam, ItemLinks.show.currentParamData);

            $.ajax({
                url : "/removelinks/doRemoveAllItemLinks",
                data : submitParam,
                type : 'post',
                success : function(dataJson) {

                    if (TM.AutoTitleUtil.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }

                    var removeRes = TM.AutoTitleUtil.util.getAjaxResultJson(dataJson);

                    TM.AutoTitleUtil.errors.showErrors(removeRes, function() {

                        ItemLinks.show.doRefresh();

                        return;

                    }, function() {

                        ItemLinks.show.doRefresh();

                        return;
                    });

                }
            });

        }
    }, ItemLinks.submit);



})(jQuery, window));


((function ($, window) {


    TM.LinksConfig = TM.LinksConfig || {};
    var LinksConfig = TM.LinksConfig;


    LinksConfig.init = LinksConfig.init || {};
    LinksConfig.init = $.extend({
        doInit: function(container) {

            LinksConfig.container = container;

            //添加事件
            container.find(".link-search-text").keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    container.find(".link-search-btn").click();
                }
            });
            container.find(".link-search-btn").click(function() {
                LinksConfig.show.doShow();
            });

            container.find(".all-link-check").click(function() {
                var isChecked = $(this).is(":checked");
                var checkObjs = container.find(".link-checkbox");
                checkObjs.attr("checked", isChecked);
            });


            var selectSubmitCallback = function(actionType) {

                var checkedObjs = container.find(".link-checkbox:checked");
                var selectLinkIdArray = [];

                checkedObjs.each(function() {
                    var checkObj = $(this);
                    selectLinkIdArray[selectLinkIdArray.length] = checkObj.attr("linkId");


                });

                LinksConfig.submit.modifySelectConfig(selectLinkIdArray, actionType);

            }

            container.find(".set-select-keep-btn").click(function() {
                selectSubmitCallback(0);
            });
            container.find(".set-select-remove-btn").click(function() {
                selectSubmitCallback(1);
            });

            container.find(".set-all-keep-btn").click(function() {
                LinksConfig.submit.modifyAllConfig(0);
            });
            container.find(".set-all-remove-btn").click(function() {
                LinksConfig.submit.modifyAllConfig(1);
            });

        },
        getContainer: function() {
            return LinksConfig.container;
        }
    }, LinksConfig.init);


    LinksConfig.show = LinksConfig.show || {};
    LinksConfig.show = $.extend({
        targetCurrentPage: 1,
        currentParamData: null,
        doShow: function() {
            LinksConfig.show.doSearch(1);
        },
        doRefresh: function() {
            LinksConfig.show.doSearch(LinksConfig.show.targetCurrentPage);
        },
        doSearch: function(currentPage) {

            if (currentPage < 1) {
                currentPage = 1;
            }

            LinksConfig.show.targetCurrentPage = currentPage;

            var paramData = LinksConfig.show.getParamData();
            if (paramData === undefined || paramData == null) {
                return;
            }

            LinksConfig.show.currentParamData = paramData;

            var container = LinksConfig.init.getContainer();

            var tbodyObj = container.find('.link-table .link-tbody');

            //tbodyObj.html('');

            container.find(".link-paging-div").tmpage({
                currPage: currentPage,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: paramData,
                    dataType: 'json',
                    url: '/removelinks/searchAllLinks',
                    callback: function(dataJson){
                        LinksConfig.show.targetCurrentPage = dataJson.pn;//记录当前页

                        //tbodyObj.html('');


                        var linkArray = dataJson.res;

                        if (linkArray === undefined || linkArray == null || linkArray.length <= 0) {
                            tbodyObj.html("");
                            tbodyObj.html('<tr><td colspan="3" style="padding: 10px 0px;">亲，您当前宝贝详情页中不存在外链。</td> </tr>');
                            return;
                        }


                        var trObjArray = $('#linkConfigRow').tmpl(linkArray);

                        var submitCallback = function(btnObj, actionType) {

                            var linkId = btnObj.attr("linkId");

                            var selectLinkIdArray = [linkId];
                            LinksConfig.submit.modifySelectConfig(selectLinkIdArray, actionType);
                        }

                        trObjArray.find(".set-link-remove-btn").click(function() {


                            var btnObj = $(this);
                            submitCallback(btnObj, 1);
                        });
                        trObjArray.find(".set-link-keep-btn").click(function() {


                            var btnObj = $(this);
                            submitCallback(btnObj, 0);
                        });


                        trObjArray.find(".link-checkbox").click(function() {
                            var checkObj = $(this);
                            var isChecked = checkObj.is(":checked");
                            if (isChecked == false) {
                                container.find(".all-link-check").attr("checked", false);
                            } else {
                                var checkObjs = container.find(".link-checkbox");
                                var isAllCheck = true;
                                checkObjs.each(function() {
                                    if ($(this).is(":checked") == false) {
                                        isAllCheck = false;
                                        return false;
                                    }
                                });
                                container.find(".all-link-check").attr("checked", isAllCheck);
                            }
                        });

                        tbodyObj.html(trObjArray);
                    }
                }

            });

        },
        getParamData: function() {

            var paramData = {};
            var container = LinksConfig.init.getContainer();

            var link = container.find(".link-search-text").val();

            paramData.link = link;

            return paramData;

        }
    }, LinksConfig.show);


    LinksConfig.submit = LinksConfig.submit || {};
    LinksConfig.submit = $.extend({
        modifySelectConfig: function(selectLinkIdArray, actionType) {
            if (selectLinkIdArray === undefined || selectLinkIdArray == null || selectLinkIdArray.length <= 0) {
                alert("请先选择要配置的链接！");
                return;
            }

            if (actionType == 0) {
                if (confirm("确定要将链接配置成保留状态？") == false) {
                    return;
                }
            } else {
                if (confirm("确定要将链接配置成去除状态？") == false) {
                    return;
                }
            }


            var selectIds = selectLinkIdArray.join(",");

            $.ajax({
                url : "/removelinks/updateLinkAction",
                data : {ids: selectIds, actionType: actionType},
                type : 'post',
                success : function(dataJson) {

                    if (TM.AutoTitleUtil.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }


                    LinksConfig.show.doRefresh();

                }
            });
        },
        modifyAllConfig: function(actionType) {

            if (actionType == 0) {
                if (confirm("确定要将所有链接配置成保留状态？") == false) {
                    return;
                }
            } else {
                if (confirm("确定要将所有链接配置成去除状态？") == false) {
                    return;
                }
            }

            var submitParam = {actionType: actionType};

            submitParam = $.extend({}, submitParam, LinksConfig.show.currentParamData);

            $.ajax({
                url : "/removelinks/updateAllLinkAction",
                data : submitParam,
                type : 'post',
                success : function(dataJson) {

                    if (TM.AutoTitleUtil.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }


                    LinksConfig.show.doRefresh();

                }
            });
        }
    }, LinksConfig.submit);


})(jQuery, window));

