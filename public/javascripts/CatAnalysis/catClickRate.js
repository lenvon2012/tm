
var TM = TM || {};
((function ($, window) {
    TM.catClickRate = TM.catClickRate || {};
    var catClickRate = TM.catClickRate;

    catClickRate.Init = catClickRate.Init || {};
    catClickRate.Init = $.extend({
        doInit : function(container){
            catClickRate.container = container;

            container.find(".first-cat-select").change(function() {
                catClickRate.category.initSecondCatSelect([0, 0, 0]);
                $("body").focus();//在ie6下使其失去焦点
            });
            container.find(".second-cat-select").change(function() {
                catClickRate.category.initThirdCatSelect([0, 0, 0]);
                $("body").focus();//在ie6下使其失去焦点
            });
            container.find(".third-cat-select").change(function() {
                catClickRate.show.doShow();
                $("body").focus();//在ie6下使其失去焦点
            });

            container.find(".sortTd").unbind('click').click(function(){
                var orderBy = $(this).find('span').attr("sort");
                if($(this).find('span').hasClass("Desc")) {
                    $(this).find('span').removeClass("Desc");
                    $(this).find('span').addClass("Asc");
                } else {
                    $(this).find('span').removeClass("Asc");
                    $(this).find('span').addClass("Desc");
                }
                var sort = $(this).find('span').hasClass("Desc") ? "desc" : "asc";
                catClickRate.show.doShow(orderBy, sort)
            });
            catClickRate.Init.initUserCatInfo();


        },
        initUserCatInfo: function(numIid) {

            var container = catClickRate.Init.getContainer();

            $.ajax({
                url : '/CatTopWord/findUserMostCat',
                data : {numIid: 0},
                type : 'post',
                success : function(dataJson) {

                    var catJsonArray = catClickRate.util.getAjaxResult(dataJson);
                    var userCatIdArray = [];
                    if (catJsonArray === undefined || catJsonArray == null || catJsonArray.length <= 0) {
                        catJsonArray = [];
                    }

                    var catNames = '';
                    if(catJsonArray.length > 0) {

                        $(catJsonArray).each(function(index, catJson) {
                            userCatIdArray[catJsonArray.length - index - 1] = catJson.cid;
                        });
                    }



                    for (var i = 0; i < 3; i++) {
                        if (userCatIdArray.length < 3) {
                            userCatIdArray[userCatIdArray.length] = 0;
                        } else {
                            break;
                        }
                    }

                    catClickRate.category.initFirstCatSelect(userCatIdArray);

                }
            });
        },
        getContainer: function() {
            return catClickRate.container;
        }
    }, catClickRate.Init)


    catClickRate.category = catClickRate.category || {};
    catClickRate.category = $.extend({
        initFirstCatSelect: function(userCatIdArray) {

            var container = catClickRate.Init.getContainer();

            var firstCatSelectObj = container.find(".first-cat-select");


            $.ajax({
                url : '/cattopword/findLevel1',
                data : {},
                type : 'post',
                success : function(dataJson) {

                    firstCatSelectObj.html("");

                    if (catClickRate.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    var catJsonArray = catClickRate.util.getAjaxResult(dataJson);
                    if (catJsonArray === undefined || catJsonArray == null || catJsonArray.length <= 0) {
                        catJsonArray = [];
                        firstCatSelectObj.html('<option value="0" selected="selected">暂无类目，请联系我们</option>');

                    } else {
                        var allHtml = '';
                        $(catJsonArray).each(function(index, catJson) {
                            allHtml += '<option value="' + catJson.cid + '" >' + catJson.name + '</option>';
                        });

                        firstCatSelectObj.html(allHtml);

                        //默认选中类目
                        var targetCid = catClickRate.category.getSelectCid(userCatIdArray, 0);
                        catClickRate.category.doSelectOption(firstCatSelectObj, targetCid);
                    }

                    //触发二级类目
                    catClickRate.category.initSecondCatSelect(userCatIdArray);

                }
            });

        },
        initSecondCatSelect: function(userCatIdArray) {
            var container = catClickRate.Init.getContainer();

            var firstCatSelectObj = container.find(".first-cat-select");
            var parentCid = firstCatSelectObj.val();

            var secondCatSelectObj = container.find(".second-cat-select");


            $.ajax({
                url : '/cattopword/findLevel2or3',
                data : {parentCid: parentCid},
                type : 'post',
                success : function(dataJson) {
                    secondCatSelectObj.html("");
                    if (catClickRate.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    var catJsonArray = catClickRate.util.getAjaxResult(dataJson);
                    if (catJsonArray === undefined || catJsonArray == null || catJsonArray.length <= 0) {
                        catJsonArray = [];
                        secondCatSelectObj.html('<option value="0">暂无类目</option>');

                    } else {
                        var allHtml = '';
                        $(catJsonArray).each(function(index, catJson) {
                            allHtml += '<option value="' + catJson.cid + '" >' + catJson.name + '</option>';
                        });

                        secondCatSelectObj.html(allHtml);

                        //默认选中类目
                        var targetCid = catClickRate.category.getSelectCid(userCatIdArray, 1);
                        catClickRate.category.doSelectOption(secondCatSelectObj, targetCid);
                    }

                    //触发三级类目
                    catClickRate.category.initThirdCatSelect(userCatIdArray);

                }
            });
        },
        initThirdCatSelect: function(userCatIdArray) {
            var container = catClickRate.Init.getContainer();

            var secondCatSelectObj = container.find(".second-cat-select");
            var parentCid = secondCatSelectObj.val();

            var thirdCatSelectObj = container.find(".third-cat-select");


            $.ajax({
                url : '/cattopword/findLevel2or3',
                data : {parentCid: parentCid},
                type : 'post',
                success : function(dataJson) {
                    thirdCatSelectObj.html("");
                    if (catClickRate.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    var catJsonArray = catClickRate.util.getAjaxResult(dataJson);
                    if (catJsonArray === undefined || catJsonArray == null || catJsonArray.length <= 0) {
                        catJsonArray = [];
                        thirdCatSelectObj.html('<option value="0">暂无类目</option>');

                    } else {
                        var allHtml = '';
                        $(catJsonArray).each(function(index, catJson) {
                            allHtml += '<option value="' + catJson.cid + '" >' + catJson.name + '</option>';
                        });

                        thirdCatSelectObj.html(allHtml);

                        //默认选中类目
                        var targetCid = catClickRate.category.getSelectCid(userCatIdArray, 2);
                        catClickRate.category.doSelectOption(thirdCatSelectObj, targetCid);
                    }

                    catClickRate.show.doShow();

                }
            });
        },
        getSelectCid: function(userCatIdArray, selectIndex) {
            if (userCatIdArray === undefined || userCatIdArray == null || userCatIdArray.length <= 0) {
                return 0;
            }
            if (userCatIdArray.length < selectIndex + 1) {
                return 0;
            }
            return userCatIdArray[selectIndex];
        },
        doSelectOption: function(selectObj, targetCid) {
            if (targetCid === undefined || targetCid == null || targetCid <= 0) {
                selectObj.find('option:eq(0)').attr("selected", true);
            } else {
                var selectOptionObj = selectObj.find('option[value="' + targetCid + '"]');
                if (selectOptionObj.length > 0) {
                    selectOptionObj.attr("selected", true);
                } else {
                    selectObj.find('option:eq(0)').attr("selected", true);
                }
            }
        }
    }, catClickRate.category);

    catClickRate.show = catClickRate.show || {};
    catClickRate.show = $.extend({
        doShow : function(orderBy, sort){
            if(orderBy === undefined || orderBy == null) {
                orderBy = "clickRate";
            }
            if(sort === undefined || sort == null) {
                sort = "desc";
            }
            var container = catClickRate.Init.getContainer();
            var cid = catClickRate.show.getParamData();
            if (cid <= 0) {
                container.find('.catClickRateContent no-data').show();
                return;
            }
            container.find('.catClickRateContentPagging').tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount:1,
                ajax: {
                    param : {cid : cid, orderBy : orderBy, sort : sort},
                    on: true,
                    dataType: 'json',
                    url: "/CatAnalysis/catClickRate",
                    callback:function(data){
                        container.find('.catClickRateTable tbody').empty();
                        if(data === undefined || data == null) {
                            container.find('.catClickRateTable tbody').append($('<tr><td colspan="7">该类目暂时无数据</td></tr>'));
                            return;
                        }
                        if(data.res.length <= 0) {
                            container.find('.catClickRateTable tbody').append($('<tr><td colspan="7">该类目暂时无数据</td></tr>'));
                            return;
                        }

                        $(data.res).each(function(i, item){
                            var href = "http://item.taobao.com/item.htm?id=" + item.numIid;
                            container.find('.catClickRateTable tbody').append('<tr>' +
                                '<td style="width: 200px;"><a target="_blank" href="'+href+'"><img style="width: 180px; height: 180px;margin: 10px 0;" src="'+item.picUrl+'" alt="宝贝主图"></a></td>' +
                                '<td>'+item.impression+'</td>' +
                                '<td>'+item.aclick+'</td>' +
                                '<td>'+new Number(item.clickRate).toPercent(2)+'</td></tr>');
                        });
                    }
                }

            });
        },
        getParamData: function() {

            var container = catClickRate.Init.getContainer();
            var firstCatSelectObj = container.find(".first-cat-select");
            var secondCatSelectObj = container.find(".second-cat-select");
            var thirdCatSelectObj = container.find(".third-cat-select");

            if(parseInt(thirdCatSelectObj.val()) > 0) {
                return thirdCatSelectObj.val();
            } else if(parseInt(secondCatSelectObj.val()) > 0) {
                return secondCatSelectObj.val();
            } else if(parseInt(firstCatSelectObj.val()) > 0){
                return firstCatSelectObj.val();
            } else {
                return 0;
            }

        }
    }, catClickRate.show);

    catClickRate.util = catClickRate.util || {};
    catClickRate.util = $.extend({
        judgeAjaxResult: function(resultJson) {
            var msg = resultJson.message;
            if (msg === undefined || msg == null || msg == "") {
                msg = resultJson.msg;
            }
            if (msg === undefined || msg == null || msg == "") {

            } else {
                alert(msg);
            }
            var isSuccess = resultJson.success;
            if (isSuccess === undefined || isSuccess == null) {
                isSuccess = resultJson.isOk;
            }

            return isSuccess;
        },
        getAjaxResult: function(resultJson) {
            var json = resultJson.results;
            if (json === undefined || json == null) {
                json = resultJson.res;
            }

            return json;
        }
    }, catClickRate.util);

})(jQuery,window));

