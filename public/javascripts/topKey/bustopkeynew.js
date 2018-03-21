
var TM = TM || {};
((function ($, window) {
    TM.CatTopWord = TM.CatTopWord || {};
    var CatTopWord = TM.CatTopWord;


    CatTopWord.init = CatTopWord.init || {};
    CatTopWord.init = $.extend({
        doInit: function(container, numIid) {
            CatTopWord.container = container;

            container.find(".first-cat-select").change(function() {
                CatTopWord.category.initSecondCatSelect([0, 0, 0]);
                $("body").focus();//在ie6下使其失去焦点
            });
            container.find(".second-cat-select").change(function() {
                CatTopWord.category.initThirdCatSelect([0, 0, 0]);
                $("body").focus();//在ie6下使其失去焦点
            });
            container.find(".third-cat-select").change(function() {
                CatTopWord.show.doShow();
                $("body").focus();//在ie6下使其失去焦点
            });

            CatTopWord.init.initUserCatInfo(numIid);


            var sortTdObjs = container.find(".sort-td");
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
                CatTopWord.show.doShow();
            });

        },
        initUserCatInfo: function(numIid) {

            var container = CatTopWord.init.getContainer();
            var catNameObj = container.find(".catName");

            if (numIid === undefined || numIid == null) {
                numIid = 0;
            }

            $.ajax({
                url : '/CatTopWord/findUserMostCat',
                data : {numIid: numIid},
                type : 'post',
                success : function(dataJson) {

                    /*if (CatTopWord.util.judgeAjaxResult(dataJson) == false) {

                    }*/

                    var catJsonArray = CatTopWord.util.getAjaxResult(dataJson);
                    var userCatIdArray = [];
                    if (catJsonArray === undefined || catJsonArray == null || catJsonArray.length <= 0) {
                        catJsonArray = [];
                    }

                    var catNames = '';
                    if(catJsonArray.length <= 0) {
                        catNameObj.hide();
                    } else {
                        $(catJsonArray).each(function(index, catJson) {
                            if (index > 0) {
                                catNames = " > " + catNames;
                            }
                            catNames = catJson.name + catNames;
                            userCatIdArray[catJsonArray.length - index - 1] = catJson.cid;
                        });

                        catNameObj.find(".cat-name-span").html(catNames);
                        catNameObj.show();
                    }



                    for (var i = 0; i < 3; i++) {
                        if (userCatIdArray.length < 3) {
                            userCatIdArray[userCatIdArray.length] = 0;
                        } else {
                            break;
                        }
                    }

                    CatTopWord.category.initFirstCatSelect(userCatIdArray);

                    catNameObj.find(".cat-name-span").unbind().click(function() {
                        CatTopWord.category.initFirstCatSelect(userCatIdArray);
                    });
                }
            });
        },
        getContainer: function() {
            return CatTopWord.container;
        }
    }, CatTopWord.init);


    CatTopWord.category = CatTopWord.category || {};
    CatTopWord.category = $.extend({
        initFirstCatSelect: function(userCatIdArray) {

            var container = CatTopWord.init.getContainer();

            var firstCatSelectObj = container.find(".first-cat-select");


            $.ajax({
                url : '/cattopword/findLevel1',
                data : {},
                type : 'post',
                success : function(dataJson) {

                    firstCatSelectObj.html("");

                    if (CatTopWord.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    var catJsonArray = CatTopWord.util.getAjaxResult(dataJson);
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
                        var targetCid = CatTopWord.category.getSelectCid(userCatIdArray, 0);
                        CatTopWord.category.doSelectOption(firstCatSelectObj, targetCid);
                    }

                    //触发二级类目
                    CatTopWord.category.initSecondCatSelect(userCatIdArray);

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
        },
        initSecondCatSelect: function(userCatIdArray) {
            var container = CatTopWord.init.getContainer();

            var firstCatSelectObj = container.find(".first-cat-select");
            var parentCid = firstCatSelectObj.val();

            var secondCatSelectObj = container.find(".second-cat-select");


            $.ajax({
                url : '/cattopword/findLevel2or3',
                data : {parentCid: parentCid},
                type : 'post',
                success : function(dataJson) {
                    secondCatSelectObj.html("");
                    if (CatTopWord.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    var catJsonArray = CatTopWord.util.getAjaxResult(dataJson);
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
                        var targetCid = CatTopWord.category.getSelectCid(userCatIdArray, 1);
                        CatTopWord.category.doSelectOption(secondCatSelectObj, targetCid);
                    }

                    //触发三级类目
                    CatTopWord.category.initThirdCatSelect(userCatIdArray);

                }
            });
        },
        initThirdCatSelect: function(userCatIdArray) {
            var container = CatTopWord.init.getContainer();

            var secondCatSelectObj = container.find(".second-cat-select");
            var parentCid = secondCatSelectObj.val();

            var thirdCatSelectObj = container.find(".third-cat-select");


            $.ajax({
                url : '/cattopword/findLevel2or3',
                data : {parentCid: parentCid},
                type : 'post',
                success : function(dataJson) {
                    thirdCatSelectObj.html("");
                    if (CatTopWord.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    var catJsonArray = CatTopWord.util.getAjaxResult(dataJson);
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
                        var targetCid = CatTopWord.category.getSelectCid(userCatIdArray, 2);
                        CatTopWord.category.doSelectOption(thirdCatSelectObj, targetCid);
                    }

                    CatTopWord.show.doShow();

                }
            });
        }
    }, CatTopWord.category);


    CatTopWord.show = CatTopWord.show || {};
    CatTopWord.show = $.extend({
        doShow: function() {

            var paramData = CatTopWord.show.getParamData();
            if (paramData == null) {
                return;
            }

            var container = CatTopWord.init.getContainer();

            var tbodyObj = container.find(".catWordTable tbody.cat-top-word-tbody");
            tbodyObj.html("");

            container.find(".paging-div").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount:1,
                //isJsonp: true,
                ajax: {
                    param : paramData,
                    on: true,
                    dataType: 'json',
                    url: "/cattopword/findCatTopWords",
                    //url: "http://chedao.tobti.com/commons/findCatTopWords",
                    //url: "http://localhost:9000/commons/findCatTopWords",
                    callback:function(dataJson){

                        var wordJsonArray = dataJson.res;

                        tbodyObj.html("");

                        if (wordJsonArray === undefined || wordJsonArray == null || wordJsonArray.length <= 0) {

                            var html = '<tr><td colspan="8" style="height:40px;">亲，当前类目下暂无数据！</td></tr>';

                            tbodyObj.html(html);

                            return;

                        } else {
                            var trHtmlArray = [];
                            var words = new Array();
                            $(wordJsonArray).each(function(index, wordJson) {
                                var trHtml = CatTopWord.show.createRowHtml(index, wordJson);
                                trHtmlArray[trHtmlArray.length] = trHtml;
                                // 宝贝数为0
                                if(wordJson.itemCount <= 0){
                                    var dataWord = dataWord || {};
                                    dataWord.index = index;
                                    dataWord.word = wordJson.word;
                                    dataWord.pv = wordJson.pv;
                                    words.push(dataWord);
                                }
                            });

                            tbodyObj.html(trHtmlArray.join(""));

                            tbodyObj.find(".add-word-btn").unbind().click(function() {
                                var btnObj = $(this);
                                var trObj = btnObj.parent().parent();
                                if (trObj.hasClass("top-word-tr") == false) {
                                    alert("系统出现异常，找不到关键词，请联系我们！");
                                    return;
                                }
                                var targetWord = trObj.find(".word-td").html();
                                var targetPv = -1;
                                var targetPrice = -1;
                                $(wordJsonArray).each(function(index, wordJson){
                                    if(wordJson.word == targetWord){
                                        targetPv = wordJson.pv;
                                        targetPrice = wordJson.price;
                                    }
                                });
                                $.post('/KeyWords/addMyWord',{word:targetWord, pv:targetPv, price:targetPrice},function(data){
                                    TM.Alert.load(data,400, 300, function(){}, "", "已成功添加到词库", 3000);
                                });
                            });

                            if(words.length > 0) {
                                CatTopWord.getItemCount.searchWordItemCount(words);
                            }
                        }
                    }
                }
            });
        },
        createRowHtml: function(index, wordJson) {

            var pv = "-";
            var click = "-";
            var ctr = "-";
            var price = '-';
            if (wordJson.pv > 0) {
                pv = parseInt(wordJson.pv) > 10000 ? new Number(wordJson.pv).toTenThousand(1) : wordJson.pv;
                click = parseInt(wordJson.click) > 10000 ? new Number(wordJson.click).toTenThousand(1) : wordJson.click;
                ctr = (wordJson.ctrInt / 10000).toPercent(2);
                price = '￥' + (wordJson.price / 100).toFixed(2);
            }

            var itemCount = "-";
            if (wordJson.itemCount > 0) {
                itemCount = parseInt(wordJson.itemCount) > 10000 ? new Number(wordJson.itemCount).toTenThousand(1) : wordJson.itemCount;
            }

            var score = "-";
            if (wordJson.pv > 0 && wordJson.itemCount > 0) {
                score = Math.round(wordJson.pv / wordJson.itemCount).toFixed(0);
            }


            var trCss = '';
            if (index % 2 == 1) {
                trCss = 'even';
            }
            
            var word = wordJson.word
            word = word.replace("2013", "2016");
            word = word.replace("2014", "2016");
            word = word.replace("2015", "2016");
            word = word.replace("2016", "2017");

            var trHtml = '' +
                '<tr class="top-word-tr ' + trCss + '">' +
                '   <td class="word-td">' + word + '</td>' +
                '   <td style="">' + pv + '</td>' +
                '   <td style="">' + click + '</td>' +
                '   <td style="">' + ctr + '</td>' +
                '   <td style="">' + itemCount + '</td>' +
                '   <td style="">' + score + '</td>' +
                '   <td style="">' + price + '</td>' +
                '   <td style=""><span class="tmbtn sky-blue-btn add-word-btn">添加到词库</span>' +
                '</tr>' +
                '';

            return trHtml;

        },
        getParamData: function() {
            var paramData = {};

            var container = CatTopWord.init.getContainer();
            var firstCatSelectObj = container.find(".first-cat-select");
            var secondCatSelectObj = container.find(".second-cat-select");
            var thirdCatSelectObj = container.find(".third-cat-select");


            paramData.firstCid = firstCatSelectObj.val();
            paramData.secondCid = secondCatSelectObj.val();
            paramData.thirdCid = thirdCatSelectObj.val();

            var orderObj = container.find(".current-sort");
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

            return paramData;

        }
    }, CatTopWord.show);


    CatTopWord.util = CatTopWord.util || {};
    CatTopWord.util = $.extend({
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
    }, CatTopWord.util);

    CatTopWord.getItemCount = CatTopWord.getItemCount || {};
    CatTopWord.getItemCount = $.extend({
        searchWordItemCount: function(words){
            if(words == undefined || words == null || words.length <= 0) {
                return;
            }
            // 宝贝数为0时,获取宝贝数;
            $.ajax({
                url: "http://chedao.taovgo.com/commons/findWordItemCount",
                // url: "http://127.0.0.1:9996/commons/findWordItemCount",
                type: "get",
                async: true,
                dataType: 'jsonp',
                jsonpCallback: "respData",
                jsonp:'callback',
                // beforeSend: function(){  },
                data: {words: words},
                success: function(respData){
                    $.each(respData, function(i, item){
                        var wordIndex = $(".cat-top-word-tbody tr ").eq(item.index);
                        var itemCountTd = wordIndex.children().eq(4);
                        var scoreTd = wordIndex.children().eq(5);
                        var itemCount = item.itemCount;
                        if(itemCount <= 0){
                            return false;
                        }
                        // 性价比
                        var score = Math.round(parseInt(item.pv) / parseInt(itemCount)).toFixed(0);
                        itemCount = parseInt(itemCount) > 10000 ? new Number(itemCount).toTenThousand(1) : itemCount;
                        itemCountTd.html(itemCount);
                        scoreTd.html(score);
                    });
                },error: function(e){
                    // alert(JSON.stringify(e));
                }
            });
        }
    });

})(jQuery,window));

