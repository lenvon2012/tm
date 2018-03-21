

((function ($, window) {

    var getOptimizeAdmin = function() {

        var OptimizeAdmin = {};

        OptimizeAdmin.init = OptimizeAdmin.init || {};
        OptimizeAdmin.init = $.extend({
            doInit: function(container, optConfig) {
                OptimizeAdmin.container = container;
                OptimizeAdmin.optConfig = optConfig;

                var optimizeObj = $('#optimizeTitleTmpl').tmpl({});

                container.html(optimizeObj);

                var newTitleObj = container.find('.new-item-title-input');
                var boxId ="title-remain-length-" + optConfig.numIid;
                newTitleObj.inputlimitor({
                    limit: 60,
                    boxId: boxId,
                    remText: '&nbsp;(剩余%n字)',
                    limitText: ''
                });

                newTitleObj.val(optConfig.title);
                newTitleObj.trigger("keyup");
                container.find('#'+boxId).find('br').remove();
                container.find('#'+boxId).css('display', 'inline');

                OptimizeAdmin.init.initItemCatName();


                //初始化tab div
                DiagnoseTitle.init.doInit(container.find('.diag-new-title-div'));
                PromoteWords.init.doInit(container.find('.promote-word-div'));
                SearchMoreWord.init.doInit(container.find('.search-more-word-div'));
                CatHotWord.init.doInit(container.find('.cat-hot-word-div'));
                ItemPropWords.init.doInit(container.find('.item-prop-word-div'));
                HistoryTitles.init.doInit(container.find('.item-history-title-div'));

                //绑定事件
                container.find('.save-new-title-btn').unbind().click(function() {
                    OptimizeAdmin.submit.doSaveNewTitle();
                });
                container.find('.diagnose-new-title-btn').unbind().click(function() {
                    OptimizeAdmin.submit.diagnoseNewTitle();
                });
                container.find('.show-recommend-title-btn').unbind().click(function() {
                    OptimizeAdmin.submit.showRecommendTitle();
                });




                var tabBtnObjs = container.find('.optimize-tab');

                tabBtnObjs.unbind().click(function() {
                    tabBtnObjs.removeClass("select");

                    var thisBtnObj = $(this);
                    thisBtnObj.addClass("select");

                    container.find(".tab-target-div").hide();

                    var targetDivCss = thisBtnObj.attr('targetDiv');
                    var targetDivObj = container.find('.' + targetDivCss);
                    targetDivObj.show();

                    if (thisBtnObj.attr('hasLoaded') == 'hasLoaded' && targetDivCss != 'item-history-title-div') {
                        return;
                    }


                    thisBtnObj.attr('hasLoaded', 'hasLoaded');

                    if (targetDivCss == 'diag-new-title-div') {
                        DiagnoseTitle.show.doShow();
                    } else if (targetDivCss == 'promote-word-div') {
                        PromoteWords.show.doShow();
                    } else if (targetDivCss == 'search-more-word-div') {
                        SearchMoreWord.show.doShow();
                    } else if (targetDivCss == 'cat-hot-word-div') {
                        CatHotWord.show.doShow();
                    } else if (targetDivCss == 'cat-heima-word-div') {

                    } else if (targetDivCss == 'item-prop-word-div') {
                        ItemPropWords.show.doShow();
                    } else if (targetDivCss == 'item-history-title-div') {
                        HistoryTitles.show.doShow();
                    }

                });


                container.find('.optimize-tab.select').click();

                container.show();
            },
            initItemCatName: function() {
                var container = OptimizeAdmin.init.getContainer();
                var numIid = OptimizeAdmin.init.getNumIid();

                $.ajax({
                    url : "/CatTopWord/findUserMostCat",
                    data : {numIid: numIid},
                    type : 'post',
                    success : function(dataJson) {

/*
                        if (TM.AutoTitleUtil.util.judgeAjaxResult(dataJson) == false) {
                            return;
                        }*/

                        var catJsonArray = TM.AutoTitleUtil.util.getAjaxResultJson(dataJson);

                        if (catJsonArray === undefined || catJsonArray == null || catJsonArray.length <= 0) {
                            catJsonArray = [];
                        }

                        var catName = '';

                        $(catJsonArray).each(function(index, catJson) {
                            if (catName != '') {
                                catName = ' > ' + catName;
                            }
                            catName = catJson.name + catName;
                        });

                        container.find('.item-cat-name-span').html(catName);
                    }
                });



            },
            getContainer: function() {
                return OptimizeAdmin.container;
            },
            getNumIid: function() {
                var optConfig = OptimizeAdmin.optConfig;
                return optConfig.numIid;
            }
        }, OptimizeAdmin.init);


        OptimizeAdmin.submit = OptimizeAdmin.submit || {};
        OptimizeAdmin.submit = $.extend({
            doSaveNewTitle: function() {
                var container = OptimizeAdmin.init.getContainer();
                var numIid = OptimizeAdmin.init.getNumIid();


                var newTitle = container.find('.new-item-title-input').val();

                if (newTitle == "") {
                    alert('请先输入新标题！');
                    return;
                }
                if (confirm('确定要提交新标题？') == false) {
                    return;
                }
                var submitParams = {numIid: numIid, title: newTitle};

                $.ajax({
                    url : "/titles/rename",
                    data : submitParams,
                    type : 'post',
                    success : function(dataJson) {


                        if (TM.AutoTitleUtil.util.judgeAjaxResult(dataJson) == false) {
                            return;
                        }

                        alert('新标题提交成功！');
                    }
                });

            },
            diagnoseNewTitle: function() {
                var container = OptimizeAdmin.init.getContainer();

                OptimizeAdmin.util.gotoDiagnoseTitleTab();

                DiagnoseTitle.show.doShow();

            },
            showRecommendTitle: function() {

                var container = OptimizeAdmin.init.getContainer();
                var numIid = OptimizeAdmin.init.getNumIid();

                $.ajax({
                    url : "/titles/getRecommendByNumIid",
                    data : {numIid: numIid},
                    type : 'post',
                    success : function(dataJson) {

                        var newTitle = dataJson;

                        container.find('.new-item-title-input').val(newTitle);
                        container.find('.new-item-title-input').trigger('keyup');
                    }
                });


            }
        }, OptimizeAdmin.submit);


        OptimizeAdmin.util = OptimizeAdmin.util || {};
        OptimizeAdmin.util = $.extend({
            gotoDiagnoseTitleTab: function() {
                var container = OptimizeAdmin.init.getContainer();

                container.find('.optimize-tab[targetDiv="diag-new-title-div"]').click();

            },
            getNewTitle: function() {
                var container = OptimizeAdmin.init.getContainer();

                return container.find('.new-item-title-input').val();
            },
            countTitleLength: function(str){
                var totalCount = 0;
                for (var i = 0; i < str.length; i++) {
                    var c = str.charCodeAt(i);
                    if ((c >= 0x0001 && c <= 0x007e) || (0xff60 <= c && c <= 0xff9f)) {
                        totalCount++;
                    }else {
                        totalCount += 2;
                    }
                }
                return totalCount;
            },
            addWordToTitle: function(wordObj) {
                var container = OptimizeAdmin.init.getContainer();
                var newTitleObj = container.find('.new-item-title-input');
                var newTitle = newTitleObj.val();

                var word = wordObj.attr('word');

                if(OptimizeAdmin.util.countTitleLength(newTitle + word) > 60) {
                    alert('标题长度最多30个字，您已经无法加入，请先删除！');
                    return;
                }
                else {
                    var start = {}
                    var end = {};
                    start.left = wordObj.offset().left + "px";
                    start.top = wordObj.offset().top + "px";
                    end.left = newTitleObj.offset().left + "px";
                    end.top = newTitleObj.offset().top + "px";

                    TM.AutoTitleUtil.Fly.flyFromTo(start, end, wordObj, function(){
                        newTitleObj.val(newTitle + word);
                        newTitleObj.trigger("keyup");
                    });


                }
            }
        }, OptimizeAdmin.util);


        /**
         * 诊断标题
         * @type {Object}
         */
        var DiagnoseTitle = {};

        DiagnoseTitle.init = DiagnoseTitle.init || {};
        DiagnoseTitle.init = $.extend({
            doInit: function(container) {
                DiagnoseTitle.container = container;
            },
            getContainer: function() {
                return DiagnoseTitle.container;
            }
        }, DiagnoseTitle.init);

        DiagnoseTitle.show = DiagnoseTitle.show || {};
        DiagnoseTitle.show = $.extend({
            doShow: function() {


                var title = OptimizeAdmin.util.getNewTitle();

                if (title === undefined || title == null || title == '') {
                    alert('请先输入新标题！');
                    return;
                }

                var container = DiagnoseTitle.init.getContainer();


                var paramData = {};
                paramData.numIid = OptimizeAdmin.init.getNumIid();
                paramData.title = title;

                $.ajax({
                    url : "/titles/singleDiag",
                    data : paramData,
                    type : 'post',
                    success : function(dataJson) {


                        container.html($('#diagnoseTitleTmpl').tmpl({title: title}));

                        if (dataJson == '服务器不正常') {
                            alert('标题诊断异常，请稍后重试！');
                            return;
                        }

                        var diagnoseJson = dataJson;

                        container.find('.title-score-span').html(diagnoseJson.score + '分');

                        DiagnoseTitle.show.showDiagResTable(diagnoseJson);


                    }
                });


            },
            showDiagResTable: function(diagnoseJson) {

                var container = DiagnoseTitle.init.getContainer();

                var tbodyObj = container.find('.diag-res-table tbody.diag-res-tbody');

                var setGoodTip = function(tdObj) {
                    tdObj.addClass('green-bold');
                    tdObj.html('表现不错');
                }
                var setBadTip = function(tdObj) {
                    tdObj.addClass('red-bold');
                    tdObj.html('建议优化');
                }
                var setNormalTip = function(tdObj) {
                    tdObj.addClass('yellow-bold');
                    tdObj.html('诊断分析');
                }


                //标题长度
                if (diagnoseJson.wordLength >= 57) {
                    setGoodTip(tbodyObj.find('.title-length-tip'));
                    tbodyObj.find('.title-length-res').html('您已经充分利用了标题字数！');
                } else {
                    var engNum = 60 - diagnoseJson.wordLength;
                    var chsNum = Math.floor(engNum / 2);

                    setBadTip(tbodyObj.find('.title-length-tip'));
                    var diagRes = '标题还可以添加<span class="red-bold">' + engNum + '</span>个英文，或者<span class="red-bold">' + chsNum + '</span>个中文！';
                    tbodyObj.find('.title-length-res').html(diagRes);
                }

                //标点符号
                var engPunctuationNum = diagnoseJson.engPunctuationNum - diagnoseJson.spaceNum;
                var chsPunctuationNum = diagnoseJson.chsPunctuationNum;

                if (engPunctuationNum <= 0 && chsPunctuationNum <= 0) {
                    setGoodTip(tbodyObj.find('.title-punctuation-tip'));
                    tbodyObj.find('.title-punctuation-res').html('当前标题不存在标点符号！');
                } else if (engPunctuationNum > 0 && chsPunctuationNum <= 0) {

                    setBadTip(tbodyObj.find('.title-punctuation-tip'));
                    var diagRes = '当前标题存在<span class="red-bold">' + engPunctuationNum + '</span>个英文标点，建议您去除！';
                    tbodyObj.find('.title-punctuation-res').html(diagRes);

                } else if (engPunctuationNum <= 0 && chsPunctuationNum > 0) {

                    setBadTip(tbodyObj.find('.title-punctuation-tip'));
                    var diagRes = '当前标题存在<span class="red-bold">' + chsPunctuationNum + '</span>个中文标点，建议您去除！';
                    tbodyObj.find('.title-punctuation-res').html(diagRes);

                } else {
                    setBadTip(tbodyObj.find('.title-punctuation-tip'));
                    var diagRes = '当前标题存在<span class="red-bold">' + engPunctuationNum + '</span>个英文标点，<span class="red-bold">' + chsPunctuationNum + '</span>个中文标点，建议您去除！';
                    tbodyObj.find('.title-punctuation-res').html(diagRes);

                }

                //类目属性
                if (diagnoseJson.cidProps === undefined || diagnoseJson.cidProps == null) {
                    diagnoseJson.cidProps = '';
                }
                if (diagnoseJson.itemProps === undefined || diagnoseJson.itemProps == null) {
                    diagnoseJson.itemProps = '';
                }
                var catPropArray = diagnoseJson.cidProps.split(',');
                var itemPropArray = diagnoseJson.itemProps.split(',');

                var remainPropArray = [];
                $(catPropArray).each(function(index, catProp) {
                    for (var i = 0; i < itemPropArray.length; i++) {
                        var itemProp = itemPropArray[i];
                        if (itemProp == catProp) {
                            return;
                        }
                    }
                    remainPropArray.push(catProp);
                });

                if (remainPropArray.length <= 0) {
                    setGoodTip(tbodyObj.find('.cat-prop-tip'));
                    tbodyObj.find('.cat-prop-res').html('您已经填写了宝贝的所有属性！');
                } else {
                    setBadTip(tbodyObj.find('.cat-prop-tip'));
                    var diagRes = '宝贝有<span class="red-bold">' + remainPropArray.length + '</span>个属性未填：' +
                        '<span class="bold">' + remainPropArray.join(',') + '</span> ';
                    tbodyObj.find('.cat-prop-res').html(diagRes);
                }


                //标题包含属性
                var containPropArray = diagnoseJson.props;
                if (containPropArray === undefined || containPropArray == null) {
                    containPropArray = [];
                }
                setNormalTip(tbodyObj.find('.item-prop-tip'));

                if (containPropArray.length <= 0) {
                    tbodyObj.find('.item-prop-res').html('标题中未包含任何宝贝属性！');
                } else {
                    var diagRes = '标题包含<span class="red-bold">' + containPropArray.length + '</span>个宝贝属性：' +
                        '<span class="bold">' + containPropArray.join(',') + '</span> ';
                    tbodyObj.find('.item-prop-res').html(diagRes);
                }

                //重复关键词
                var dumpWordJson = diagnoseJson.dumpCount;
                var dumpWordArray = [];
                for (var dumpWord in dumpWordJson) {
                    var dumpCount = dumpWordJson[dumpWord];
                    dumpWordArray.push(dumpWord);
                }

                if (dumpWordArray.length <= 0) {
                    setNormalTip(tbodyObj.find('.dump-word-tip'));
                    tbodyObj.find('.dump-word-res').html('标题中未包含重复关键词！');
                } else {
                    setNormalTip(tbodyObj.find('.dump-word-tip'));
                    var diagRes = '标题包含<span class="red-bold">' + dumpWordArray.length + '</span>个重复词：' +
                        '<span class="bold">' + dumpWordArray.join(',') + '</span> ';
                    tbodyObj.find('.dump-word-res').html(diagRes);
                }

            }
        }, DiagnoseTitle.show);



        /**
         * 搜更多词
         * @type {Object}
         */
        var SearchMoreWord = {};

        SearchMoreWord.init = SearchMoreWord.init || {};
        SearchMoreWord.init = $.extend({
            doInit: function(container) {
                SearchMoreWord.container = container;
            },
            getContainer: function() {
                return SearchMoreWord.container;
            }
        }, SearchMoreWord.init);

        SearchMoreWord.show = SearchMoreWord.show || {};
        SearchMoreWord.show = $.extend({
            doShow: function() {

                var container = SearchMoreWord.init.getContainer();

                container.html($('#searchMoreWordTmpl').tmpl({}));

                container.find('.search-more-word-text').unbind().keydown(function(event) {
                    if (event.keyCode == 13) {//按回车
                        SearchMoreWord.show.doSearch();
                    }
                });

                container.find('.search-more-word-btn').unbind().click(function() {
                    SearchMoreWord.show.doSearch();
                });
                TM.AutoTitleUtil.util.setSortTdEvent(container, function() {
                    SearchMoreWord.show.doSearch();
                });

                SearchMoreWord.show.doSearch();

            },
            doSearch: function() {
                var container = SearchMoreWord.init.getContainer();

                var paramData = SearchMoreWord.show.getParamData();

                if (paramData === undefined || paramData == null) {
                    return;
                }

                container.find(".paging-div").tmpage({
                    currPage: 1,
                    pageSize: 10,
                    pageCount: 1,
                    ajax: {
                        on: true,
                        param: paramData,
                        dataType: 'json',
                        url: '/words/bussearch',
                        callback: function(dataJson){

                            var tbodyObj = container.find('.more-word-res-table tbody.more-word-res-tbody');
                            tbodyObj.html('');


                            var wordJsonArray = dataJson.res;

                            if (wordJsonArray === undefined || wordJsonArray == null || wordJsonArray.length <= 0) {
                                tbodyObj.html('<tr><td colspan="7" style="padding: 10px 0px;">当前暂无满足条件的关键词！</td> </tr>');
                                return;
                            }

                            $(wordJsonArray).each(function(i, wordJson){
                                wordJson.newScore = (wordJson.scount > 0) ? Math.floor(parseInt(wordJson.pv) / parseInt(wordJson.scount)) : 0;
                            });

                            var trObjs = $('#moreWordRowTmpl').tmpl(wordJsonArray);

                            tbodyObj.html(trObjs);


                        }
                    }

                });

            },
            getParamData: function() {
                var paramData = {};
                var container = SearchMoreWord.init.getContainer();
                var searchWord = container.find('.search-more-word-text').val();

                paramData.numIid = OptimizeAdmin.init.getNumIid();
                TM.AutoTitleUtil.util.addSortParams(container, paramData);
                paramData.order = paramData.orderBy;
                if (paramData.isDesc == true) {
                    paramData.sort = 'desc';
                } else {
                    paramData.sort = 'asc';
                }

                paramData.word = searchWord;

                return paramData;
            }
        }, SearchMoreWord.show);


        /**
         * 类目热词
         * @type {Object}
         */
        var CatHotWord = {};

        CatHotWord.init = CatHotWord.init || {};
        CatHotWord.init = $.extend({
            doInit: function(container) {
                CatHotWord.container = container;
            },
            getContainer: function() {
                return CatHotWord.container;
            }
        }, CatHotWord.init);


        CatHotWord.category = CatHotWord.category || {};
        CatHotWord.category = $.extend({
            initFirstCatSelect: function(userCatIdArray) {

                var container = CatHotWord.init.getContainer();

                var firstCatSelectObj = container.find(".first-cat-select");


                $.ajax({
                    url : '/cattopword/findLevel1',
                    data : {},
                    type : 'post',
                    success : function(dataJson) {

                        firstCatSelectObj.html("");

                        if (TM.AutoTitleUtil.util.judgeAjaxResult(dataJson) == false) {
                            return;
                        }
                        var catJsonArray = TM.AutoTitleUtil.util.getAjaxResultJson(dataJson);
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
                            var targetCid = CatHotWord.category.getSelectCid(userCatIdArray, 0);
                            CatHotWord.category.doSelectOption(firstCatSelectObj, targetCid);
                        }

                        //触发二级类目
                        CatHotWord.category.initSecondCatSelect(userCatIdArray);

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
                var container = CatHotWord.init.getContainer();

                var firstCatSelectObj = container.find(".first-cat-select");
                var parentCid = firstCatSelectObj.val();

                var secondCatSelectObj = container.find(".second-cat-select");


                $.ajax({
                    url : '/cattopword/findLevel2or3',
                    data : {parentCid: parentCid},
                    type : 'post',
                    success : function(dataJson) {
                        secondCatSelectObj.html("");
                        if (TM.AutoTitleUtil.util.judgeAjaxResult(dataJson) == false) {
                            return;
                        }
                        var catJsonArray = TM.AutoTitleUtil.util.getAjaxResultJson(dataJson);
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
                            var targetCid = CatHotWord.category.getSelectCid(userCatIdArray, 1);
                            CatHotWord.category.doSelectOption(secondCatSelectObj, targetCid);
                        }

                        //触发三级类目
                        CatHotWord.category.initThirdCatSelect(userCatIdArray);

                    }
                });
            },
            initThirdCatSelect: function(userCatIdArray) {
                var container = CatHotWord.init.getContainer();

                var secondCatSelectObj = container.find(".second-cat-select");
                var parentCid = secondCatSelectObj.val();

                var thirdCatSelectObj = container.find(".third-cat-select");


                $.ajax({
                    url : '/cattopword/findLevel2or3',
                    data : {parentCid: parentCid},
                    type : 'post',
                    success : function(dataJson) {
                        thirdCatSelectObj.html("");
                        if (TM.AutoTitleUtil.util.judgeAjaxResult(dataJson) == false) {
                            return;
                        }
                        var catJsonArray = TM.AutoTitleUtil.util.getAjaxResultJson(dataJson);
                        if (catJsonArray === undefined || catJsonArray == null || catJsonArray.length <= 0) {
                            catJsonArray = [];
                            thirdCatSelectObj.html('<option value="0">暂无</option>');

                        } else {
                            var allHtml = '';
                            $(catJsonArray).each(function(index, catJson) {
                                allHtml += '<option value="' + catJson.cid + '" >' + catJson.name + '</option>';
                            });

                            thirdCatSelectObj.html(allHtml);

                            //默认选中类目
                            var targetCid = CatHotWord.category.getSelectCid(userCatIdArray, 2);
                            CatHotWord.category.doSelectOption(thirdCatSelectObj, targetCid);
                        }

                        CatHotWord.show.doSearch();

                    }
                });
            }
        }, CatHotWord.category);



        CatHotWord.show = CatHotWord.show || {};
        CatHotWord.show = $.extend({
            doShow: function() {

                var container = CatHotWord.init.getContainer();

                container.html($('#catHotWordTmpl').tmpl({}));

                container.find('select').unbind().change(function(event) {
                    CatHotWord.show.doSearch();
                });

                TM.AutoTitleUtil.util.setSortTdEvent(container, function() {
                    CatHotWord.show.doSearch();
                });


                var numIid = OptimizeAdmin.init.getNumIid();
                $.ajax({
                    url : '/CatTopWord/findUserMostCat',
                    data : {numIid: numIid},
                    type : 'post',
                    success : function(dataJson) {

                        /*if (CatTopWord.util.judgeAjaxResult(dataJson) == false) {

                         }*/

                        var catJsonArray = TM.AutoTitleUtil.util.getAjaxResultJson(dataJson);
                        var userCatIdArray = [];
                        if (catJsonArray === undefined || catJsonArray == null || catJsonArray.length <= 0) {
                            catJsonArray = [];
                        }

                        var catNames = '';
                        if(catJsonArray.length <= 0) {

                        } else {
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

                        CatHotWord.category.initFirstCatSelect(userCatIdArray);

                    }
                });

            },
            doSearch: function() {
                var container = CatHotWord.init.getContainer();

                var paramData = CatHotWord.show.getParamData();

                if (paramData === undefined || paramData == null) {
                    return;
                }

                container.find(".paging-div").tmpage({
                    currPage: 1,
                    pageSize: 10,
                    pageCount: 1,
                    ajax: {
                        on: true,
                        param: paramData,
                        dataType: 'json',
                        url: '/cattopword/findcattopwords',
                        callback: function(dataJson){

                            var tbodyObj = container.find('.cat-hot-word-table tbody');
                            tbodyObj.html('');


                            var wordJsonArray = dataJson.res;

                            if (wordJsonArray === undefined || wordJsonArray == null || wordJsonArray.length <= 0) {
                                tbodyObj.html('<tr><td colspan="7" style="padding: 10px 0px;">当前暂无满足条件的关键词！</td> </tr>');
                                return;
                            }

                            $(wordJsonArray).each(function(i, wordJson) {
                                wordJson.clickRate = wordJson.ctrInt / 100 + '%';
                                wordJson.newScore = (wordJson.itemCount > 0) ? Math.floor(parseInt(wordJson.pv) / parseInt(wordJson.itemCount)) : 0;
                            });

                            var trObjs = $('#catHotRowTmpl').tmpl(wordJsonArray);

                            tbodyObj.html(trObjs);


                        }
                    }

                });

            },
            getParamData: function() {
                var paramData = {};
                var container = CatHotWord.init.getContainer();

                TM.AutoTitleUtil.util.addSortParams(container, paramData);

                paramData.firstCid = container.find('.first-cat-select').val();
                paramData.secondCid = container.find('.second-cat-select').val();
                paramData.thirdCid = container.find('.third-cat-select').val();


                return paramData;
            }
        }, CatHotWord.show);


        /**
         * 宝贝属性
         * @type {Object}
         */
        var ItemPropWords = {};

        ItemPropWords.init = ItemPropWords.init || {};
        ItemPropWords.init = $.extend({
            doInit: function(container) {
                ItemPropWords.container = container;
            },
            getContainer: function() {
                return ItemPropWords.container;
            }
        }, ItemPropWords.init);

        ItemPropWords.show = ItemPropWords.show || {};
        ItemPropWords.show = $.extend({
            doShow: function() {

                var container = ItemPropWords.init.getContainer();

                container.html($('#itemPropWordTmpl').tmpl({}));

                var paramData = {};
                paramData.numIid = OptimizeAdmin.init.getNumIid();

                $.ajax({
                    url : "/titles/props",
                    data : paramData,
                    type : 'post',
                    success : function(dataJson) {

                        var propJsonArray = dataJson;

                        var tbodyObj = container.find('.item-prop-word-table tbody');
                        tbodyObj.html('');

                        if (propJsonArray === undefined || propJsonArray == null || propJsonArray.length <= 0) {
                            tbodyObj.html('<tr><td style="padding: 10px;">宝贝没有设置属性词！</td></tr>')
                            return;
                        }


                        var rowTdNum = 3;
                        var tdIndex = 0;
                        var allHtml = '';

                        $(propJsonArray).each(function(index, propJson) {

                            if (tdIndex % rowTdNum == 0) {
                                allHtml += '<tr>';
                            }

                            allHtml += '<td style="font-weight: bold;">' + propJson.key + '：</td>'

                            var word = propJson.value;
                            allHtml += '<td>' +
                                '<div class="click-add-word-div item-prop-word-div" word="' + word + '" title="点击加入标题尾部">+&nbsp;' + word + '</div>' +
                                '</td>'

                            /*if (propWordArray.length > 2) {
                                tdIndex += rowTdNum;
                            } else {
                                tdIndex += 1;
                            }*/

                            tdIndex += 1;

                            if (tdIndex % rowTdNum == 0) {
                                allHtml += '</tr>';
                            }
                        });


                        if (tdIndex % rowTdNum > 0) {
                            while (tdIndex % rowTdNum > 0) {

                                allHtml += '<td></td><td></td>'

                                tdIndex += 1;
                            }
                            allHtml += '</tr>';
                        }


                        tbodyObj.html(allHtml);

                        tbodyObj.find('.item-prop-word-div').unbind().click(function() {
                            OptimizeAdmin.util.addWordToTitle($(this));
                        });
                    }
                });



            }
        }, ItemPropWords.show);




        /**
         * 促销词
         * @type {Object}
         */
        var PromoteWords = {};

        PromoteWords.init = PromoteWords.init || {};
        PromoteWords.init = $.extend({
            doInit: function(container) {
                PromoteWords.container = container;
            },
            getContainer: function() {
                return PromoteWords.container;
            }
        }, PromoteWords.init);

        PromoteWords.show = PromoteWords.show || {};
        PromoteWords.show = $.extend({
            doShow: function() {

                var container = PromoteWords.init.getContainer();

                container.html('');

                $.ajax({
                    url : "/titles/getPromoteWords",
                    data : {},
                    type : 'post',
                    success : function(dataJson) {

                        var wordArray = dataJson;

                        var allHtml = '';

                        $(wordArray).each(function(index, word) {
                            allHtml += '<div class="click-add-word-div promote-word-div" word="' + word + '" title="点击加入标题尾部">+&nbsp;' + word + '</div>';
                        });

                        container.html(allHtml);

                        container.find('.promote-word-div').unbind().click(function() {
                            OptimizeAdmin.util.addWordToTitle($(this));
                        });
                    }
                });



            }
        }, PromoteWords.show);



        /**
         * 历史标题
         * @type {Object}
         */
        var HistoryTitles = {};

        HistoryTitles.init = HistoryTitles.init || {};
        HistoryTitles.init = $.extend({
            doInit: function(container) {
                HistoryTitles.container = container;
            },
            getContainer: function() {
                return HistoryTitles.container;
            }
        }, HistoryTitles.init);

        HistoryTitles.show = HistoryTitles.show || {};
        HistoryTitles.show = $.extend({
            doShow: function() {

                var container = HistoryTitles.init.getContainer();

                container.html($('#itemHistoryTitleTmpl').tmpl({}));

                HistoryTitles.show.doSearch();
            },
            doRefresh: function() {
                HistoryTitles.show.doSearch();
            },
            doSearch: function() {

                var container = HistoryTitles.init.getContainer();

                var paramData = {};
                paramData.numIid = OptimizeAdmin.init.getNumIid();

                container.find(".paging-div").tmpage({
                    currPage: 1,
                    pageSize: 10,
                    pageCount: 1,
                    ajax: {
                        on: true,
                        param: paramData,
                        dataType: 'json',
                        url: '/titles/renamehistory',
                        callback: function(dataJson){

                            var tbodyObj = container.find('.history-title-table tbody');
                            tbodyObj.html('');


                            var historyJsonArray = dataJson.res;

                            if (historyJsonArray === undefined || historyJsonArray == null || historyJsonArray.length <= 0) {
                                tbodyObj.html('<tr><td colspan="7" style="padding: 10px 0px;">当前暂无标题优化记录！</td> </tr>');
                                return;
                            }

                            $(historyJsonArray).each(function(i, historyJson){
                                historyJson.updateTimeStr = new Date(historyJson.updated).formatYMDMS();
                            });

                            var trObjs = $('#historyTitleRowTmpl').tmpl(historyJsonArray);

                            tbodyObj.html(trObjs);


                            trObjs.find('.recover-title-btn').unbind().click(function() {

                                if (confirm('确定要还原宝贝标题？') == false) {
                                    return;
                                }

                                var numIid = $(this).attr('numIid');
                                var originTitle = $(this).attr('originTitle');
                                var submitParams = {numIid: numIid, title: originTitle};

                                $.ajax({
                                    url : "/titles/rename",
                                    data : submitParams,
                                    type : 'post',
                                    success : function(dataJson) {


                                        if (TM.AutoTitleUtil.util.judgeAjaxResult(dataJson) == false) {
                                            return;
                                        }

                                        alert('标题还原成功！');

                                        HistoryTitles.show.doRefresh();
                                    }
                                });

                            });

                        }
                    }

                });

            }
        }, HistoryTitles.show);



        return OptimizeAdmin;

    }





    $.fn.extend({
        showOptimizeDiv: function(optConfig) {


            var OptimizeAdmin = getOptimizeAdmin();


            var container = $(this);

            OptimizeAdmin.init.doInit(container, optConfig);

        },
        closeOptimizeDiv: function () {

            var container = $(this);
            container.html('');

            container.hide();

        }
    });


})(jQuery,window));
