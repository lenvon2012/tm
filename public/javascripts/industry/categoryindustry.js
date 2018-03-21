

((function ($, window) {
    TM.CategoryIndustry = TM.CategoryIndustry || {};

    var CategoryIndustry = TM.CategoryIndustry;

    CategoryIndustry.init = CategoryIndustry.init || {};
    CategoryIndustry.init = $.extend({
        doInit: function(container) {
            CategoryIndustry.container = container;

            container.find(".first-category-select").change(function() {
                CategoryIndustry.category.initSecondCatSelect(0);
                $("body").focus();//在ie6下使其失去焦点
            });
            container.find(".second-category-select").change(function() {
                CategoryIndustry.catProps.doShow();
                $("body").focus();//在ie6下使其失去焦点
            });


            CategoryIndustry.category.initFirstCatSelect();

        },
        getContainer: function() {
            return CategoryIndustry.container;
        }
    }, CategoryIndustry.init);


    CategoryIndustry.category = CategoryIndustry.category || {};
    CategoryIndustry.category = $.extend({

        initFirstCatSelect: function() {

            var container = CategoryIndustry.init.getContainer();

            var firstCatSelectObj = container.find(".first-category-select");
            firstCatSelectObj.html("");

            $.ajax({
                url : '/categoryindustry/findFirstLevelCat',
                data : {},
                type : 'post',
                success : function(dataJson) {

                    if (CategoryIndustry.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    var catJsonArray = dataJson.results;
                    if (catJsonArray === undefined || catJsonArray == null || catJsonArray.length <= 0) {
                        catJsonArray = [];
                        firstCatSelectObj.html('<option value="0">暂无类目，请联系我们</option>');
                        return;
                    }

                    var allHtml = '';
                    $(catJsonArray).each(function(index, catJson) {
                        allHtml += '<option value="' + catJson.cid + '" >' + catJson.name + '</option>';
                    });

                    firstCatSelectObj.html(allHtml);

                    //显示卖家的类目
                    CategoryIndustry.category.showUserCid(firstCatSelectObj);

                }
            });

        },
        showUserCid: function(firstCatSelectObj) {

            $.ajax({
                url : '/categoryindustry/findUserMostCid',
                data : {},
                type : 'post',
                success : function(dataJson) {

                    if (CategoryIndustry.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    var catIdList = dataJson.results;
                    if (catIdList === undefined || catIdList == null || catIdList.length <= 0) {
                        catIdList = [];
                        //默认选中第一个
                        firstCatSelectObj.find('option:eq(0)').attr("selected", true);
                        CategoryIndustry.category.initSecondCatSelect(0);
                        return;
                    }

                    var userFirstCid = catIdList[0];
                    var userSecondCid = 0;
                    if (catIdList.length >= 2) {
                        userSecondCid = catIdList[1];
                    }

                    if (userFirstCid === undefined || userFirstCid == null || userFirstCid <= 0) {
                        firstCatSelectObj.find('option:eq(0)').attr("selected", true);
                        CategoryIndustry.category.initSecondCatSelect(0);
                    } else {
                        var selectOptionObj = firstCatSelectObj.find('option[value="' + userFirstCid + '"]');
                        if (selectOptionObj.length > 0) {
                            selectOptionObj.attr("selected", true);
                            CategoryIndustry.category.initSecondCatSelect(userSecondCid);
                        } else {
                            firstCatSelectObj.find('option:eq(0)').attr("selected", true);
                            CategoryIndustry.category.initSecondCatSelect(0);
                        }
                    }


                }
            });
        },
        initSecondCatSelect: function(userCid) {

            if (userCid === undefined || userCid == null || userCid <= 0) {
                userCid = 0;
            }

            var container = CategoryIndustry.init.getContainer();

            var secondCatSelectObj = container.find(".second-category-select");
            secondCatSelectObj.html("");

            var paramData = {};

            var firstCatSelectObj = container.find(".first-category-select");
            var parentCid = firstCatSelectObj.val();
            if (parentCid === undefined || parentCid == null || parentCid == "") {
                alert("请先选择一个一级类目！");
                return;
            }

            paramData.parentCid = parentCid;

            $.ajax({
                url : '/categoryindustry/findChildCats',
                data : paramData,
                type : 'post',
                success : function(dataJson) {

                    if (CategoryIndustry.util.judgeAjaxResult(dataJson) == false) {
                        secondCatSelectObj.addClass("no-cat");

                        CategoryIndustry.catProps.doShow();
                        return;
                    }
                    var catJsonArray = dataJson.results;
                    if (catJsonArray === undefined || catJsonArray == null || catJsonArray.length <= 0) {
                        catJsonArray = [];
                        secondCatSelectObj.html('<option value="0">当前暂无二级类目</option>');
                        secondCatSelectObj.addClass("no-cat");

                        CategoryIndustry.catProps.doShow();
                        return;
                    }

                    var allHtml = '';
                    $(catJsonArray).each(function(index, catJson) {
                        allHtml += '<option value="' + catJson.cid + '" >' + catJson.name + '</option>';
                    });

                    secondCatSelectObj.html(allHtml);
                    secondCatSelectObj.removeClass("no-cat");


                    if (userCid <= 0) {
                        secondCatSelectObj.find('option:eq(0)').attr("selected", true);
                    } else {
                        var selectOptionObj = secondCatSelectObj.find('option[value="' + userCid + '"]');
                        if (selectOptionObj.length > 0) {
                            selectOptionObj.attr("selected", true);
                        } else {
                            secondCatSelectObj.find('option:eq(0)').attr("selected", true);
                        }
                    }

                    CategoryIndustry.catProps.doShow();

                }
            });
        },
        getCurrentCid: function() {
            var container = CategoryIndustry.init.getContainer();

            var firstCatSelectObj = container.find(".first-category-select");
            var secondCatSelectObj = container.find(".second-category-select");

            var currentCid = 0;

            if (secondCatSelectObj.hasClass("no-cat")) {
                currentCid = firstCatSelectObj.val();
            } else {
                currentCid = secondCatSelectObj.val();
            }

            if (currentCid === undefined || currentCid == null || currentCid == "") {
                return 0;
            } else {
                return currentCid;
            }

        }

    }, CategoryIndustry.category);


    CategoryIndustry.catProps = CategoryIndustry.catProps || {};
    CategoryIndustry.catProps = $.extend({
        doShow: function() {
            var paramData = {};
            var currentCid = CategoryIndustry.category.getCurrentCid();
            if (currentCid <= 0) {
                alert("请先选择一个类目！");
                return;
            }
            paramData.cid = currentCid;

            var container = CategoryIndustry.init.getContainer();

            var attrDivObj = container.find(".cat-attr-div");
            attrDivObj.html("");

            CategoryIndustry.wordBase.clearResult();

            $.ajax({
                url : '/categoryindustry/queryCategoryProps',
                data : paramData,
                type : 'post',
                success : function(dataJson) {

                    if (CategoryIndustry.util.judgeAjaxResult(dataJson) == false) {

                        return;
                    }
                    var attrJsonArray = dataJson.results;

                    if (attrJsonArray === undefined || attrJsonArray == null || attrJsonArray.length <= 0) {
                        attrJsonArray = [];
                        attrDivObj.html("该类目下没有属性，请换个类目尝试一下！");

                        return;
                    }


                    var allHtml = '';
                    $(attrJsonArray).each(function(index, attrJson) {
                        allHtml += '<span pid="' + attrJson.pid + '" class="cat-attr-span baseblock">' + attrJson.pname + '</span> ';
                    });

                    attrDivObj.html(allHtml);

                    var attrBtnObjs = attrDivObj.find(".cat-attr-span");

                    attrBtnObjs.unbind().click(function() {
                        attrBtnObjs.removeClass("attr-select");

                        var selectAttrObj = $(this);
                        selectAttrObj.addClass("attr-select");

                        var pName = selectAttrObj.html();
                        CategoryIndustry.wordBase.doShow(pName);
                    });
                }
            });

        }
    }, CategoryIndustry.catProps);


    CategoryIndustry.wordBase = CategoryIndustry.wordBase || {};
    CategoryIndustry.wordBase = $.extend({
        clearResult: function() {
            var container = CategoryIndustry.init.getContainer();

            var wordDivObj = container.find(".wordbase-container");

            wordDivObj.html("");

            wordDivObj.hide();
        },
        doShow: function(pName) {
            var container = CategoryIndustry.init.getContainer();

            var wordDivObj = container.find(".wordbase-container");

            var html = '' +
                '<table class="list-table wordbase-table">' +
                '   <thead>' +
                '   <tr>' +
                '       <td style="width: 10%;">序号</td>' +
                '       <td style="width: 30%;">' + pName + '</td>' +
                '       <td style="width: 20%;"><div class="sort-td sort-down current-sort" orderBy="pv">展现量</div></td>' +
                '       <td style="width: 40%;">展现比例</td>' +
                '   </tr>' +
                '   </thead>' +
                '   <tbody class="wordbase-tbody"></tbody>' +
                '</table>' +
                '';
            wordDivObj.html(html);

            var sortTdObjs = wordDivObj.find(".wordbase-table .sort-td");

            sortTdObjs.click(function() {
                if ($(this).hasClass("sort-up")) {
                    $(this).removeClass("sort-up");
                    $(this).addClass("sort-down");
                } else {
                    $(this).removeClass("sort-down");
                    $(this).addClass("sort-up");
                }
                sortTdObjs.removeClass("current-sort");
                $(this).addClass("current-sort");
                CategoryIndustry.wordBase.ajaxShow();
            });


            CategoryIndustry.wordBase.ajaxShow();
        },
        ajaxShow: function() {
            var container = CategoryIndustry.init.getContainer();

            var wordDivObj = container.find(".wordbase-container");

            var tbodyObj = wordDivObj.find("table.wordbase-table tbody.wordbase-tbody");
            tbodyObj.html("");

            wordDivObj.show();

            var paramData = {};
            var currentCid = CategoryIndustry.category.getCurrentCid();
            if (currentCid <= 0) {
                alert("请先选择一个类目！");
                return;
            }
            paramData.cid = currentCid;
            var pid = container.find(".cat-attr-span.attr-select").attr("pid");
            if (pid === undefined || pid == null || pid == "") {
                alert("请先选择一个属性！");
                return;
            }
            paramData.pid = pid;

            var orderObj = wordDivObj.find(".current-sort");
            var orderBy = "";
            var isDesc = false;

            if (orderObj.length <= 0) {
                orderBy = "pv";
            } else {
                orderBy = orderObj.attr("orderBy");
                if (orderObj.hasClass("sort-down"))
                    isDesc = true;
                else
                    isDesc = false;
            }

            paramData.orderBy = orderBy;
            paramData.isDesc = isDesc;

            $.ajax({
                url : '/categoryindustry/queryPropWordBase',
                data : paramData,
                type : 'post',
                success : function(dataJson) {

                    if (CategoryIndustry.util.judgeAjaxResult(dataJson) == false) {

                        return;
                    }
                    var wordJsonArray = dataJson.results;

                    if (wordJsonArray === undefined || wordJsonArray == null || wordJsonArray.length <= 0) {
                        wordJsonArray = [];
                    }

                    var maxPv = 0;
                    $(wordJsonArray).each(function(index, wordJson) {
                        var pv = wordJson.pv;
                        if (maxPv < pv) {
                            maxPv = pv;
                        }
                    });

                    var allHtml = '';
                    $(wordJsonArray).each(function(index, wordJson) {
                        var trHtml = CategoryIndustry.wordBase.createRow(index, wordJson, maxPv);
                        allHtml += trHtml;
                    });

                    tbodyObj.html(allHtml);

                }
            });
        },
        createRow: function(index, wordJson, maxPv) {

            var pvRateWidth = 250;
            var pv = wordJson.pv;

            if (maxPv > 0) {
                if (pv > 0) {
                    pvRateWidth = Math.ceil(pvRateWidth * pv / maxPv);
                } else {
                    pvRateWidth = 1;
                }
            }

            var trHtml = '' +
                '<tr>' +
                '   <td>' + (index + 1) + '</td>' +
                '   <td>' + wordJson.vname + '</td>' +
                '   <td>' + wordJson.pv + '</td>' +
                '   <td style="text-align: left;"><div class="wordbase-rate-div" style="width: ' + pvRateWidth + 'px;"></div> </td>' +
                '</tr>' +
                '';

            return trHtml;
        }
    }, CategoryIndustry.wordBase);


    CategoryIndustry.util = CategoryIndustry.util || {};
    CategoryIndustry.util = $.extend({
        judgeAjaxResult: function(resultJson) {
            var msg = resultJson.message;
            if (msg === undefined || msg == null || msg == "") {

            } else {
                alert(msg);
            }
            return resultJson.success;
        }
    }, CategoryIndustry.util);

})(jQuery,window));