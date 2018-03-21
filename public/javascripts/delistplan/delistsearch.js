((function($, window) {

    TM.DelistSearch = TM.DelistSearch || {};

    var DelistSearch = TM.DelistSearch;

    DelistSearch.init = DelistSearch.init || {};
    DelistSearch.init.params = {
        url : '/delistsearch/analyseTaobaoDelists'
    };
    DelistSearch.init = $.extend({
        doInit : function(container, params) {
            DelistSearch.container = container;
            $.extend(DelistSearch.init.params, params);

            container.find(".delist-analyse-btn").click(function() {
                DelistSearch.show.doAnalyse();
            });

            container.find(".delist-search-btn").click(function() { // 行业下架查询
                DelistSearch.show.doSearch();
            });

            container.find(".search-input").keydown(function() {
                if (event.keyCode == 13) {// 按回车
                    DelistSearch.show.doShow();
                }
            });

            // 排序事件
            container.find(".sort-td").attr("title", "点击进行排序");

            var searchSortTds = container.find("table.delist-search-table .sort-td");
            
            searchSortTds.click(function() {

                if ($(this).hasClass("sort-up")) {
                    $(this).removeClass("sort-up");
                    $(this).addClass("sort-down");
                } else {
                    $(this).removeClass("sort-down");
                    $(this).addClass("sort-up");
                }
                searchSortTds.removeClass("current-sort");
                $(this).addClass("current-sort");

                DelistSearch.show.doSearch();
            });

        },
        getContainer : function() {
            return DelistSearch.container;
        }
    }, DelistSearch.init);

    $(document).ajaxStart(function() {
        TM.Loading.init.hidden();
        DelistSearch.loadingImg.showImg();
    });
    $(document).ajaxStop(function() {
        TM.Loading.init.hidden();
        DelistSearch.loadingImg.hideImg();
    });

    DelistSearch.show = DelistSearch.show || {};
    DelistSearch.show = $.extend({
        analyseShowType : 1,
        searchShowType : 2,
        currentShowType : 1,
        doShow : function() {
            var currentShowType = DelistSearch.show.currentShowType;

            if (currentShowType == DelistSearch.show.analyseShowType) {
                DelistSearch.show.doAnalyse();
            } else if (currentShowType == DelistSearch.show.searchShowType) {
                DelistSearch.show.doSearch();
            } else {
                alert("系统出现异常，查询出错，请联系我们！");
            }
        },
        doAnalyse : function(newParam) {

            var container2 = DelistSearch.init.getContainer();
            
            container2.find(".delist-search-div").find("table.delist-search-table  tbody.delist-search-tbody").html("");

            DelistSearch.show.currentShowType = DelistSearch.show.analyseShowType;

            var paramData = newParam|| DelistSearch.show.getParamData();

            if (paramData == null) {
                return;
            }

            var container = DelistSearch.init.getContainer();

            var tbodyObj = container.find("table.delist-chart-table tbody.delist-chart-tbody");

            tbodyObj.html("");
            
//            showloading();

            $.ajax({
//                global : false,
                url : DelistSearch.init.params.url, // /delistsearch/analyseTaobaoDelists
                data : paramData,
                type : 'post',
                success : function(dataJson) {

                    if (TM.IndustryBase.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    container.find(".industry-tip-div").hide();
                    container.find(".delist-search-div").hide();
                    container.find(".delist-chart-div").show();
//                    hidden();

                    var hourArray = TM.IndustryBase.getAjaxResult(dataJson);

                    DelistSearch.hourChart.showChart(hourArray);
                }
            });

        },
        doSearch : function() {

            DelistSearch.show.currentShowType = DelistSearch.show.searchShowType;

            var paramData = DelistSearch.show.getParamData();

            if (paramData == null) {
                return;
            }

            var container = DelistSearch.init.getContainer();

            var searchObj = container.find(".delist-search-div");

            var orderObj = searchObj.find(".current-sort"); // 搜索排名
            var orderBy = "";
            var isDesc = false;

            if (orderObj.length <= 0) {
                orderBy = "orderIndex";
            } else {
                orderBy = orderObj.attr("orderBy");
                if (orderObj.hasClass("sort-down"))
                    isDesc = true;
                else
                    isDesc = false;
            }

            paramData.orderBy = orderBy;
            paramData.isDesc = isDesc;

            var tbodyObj = searchObj.find("table.delist-search-table tbody.delist-search-tbody");

            tbodyObj.html("");

//            showloading();

            searchObj.find(".paging-div").tmpage({
                currPage : 1,
                pageSize : 10,
                pageCount : 1,
                ajax : {
//                    global : false,
                    param : paramData,
                    on : true,
                    dataType : 'json',
                    url : "/delistsearch/searchTaobaoItems",
                    callback : function(dataJson) {

                        var itemJsonArray = dataJson.res;

                        tbodyObj.html("");

                        container.find(".industry-tip-div").hide();
                        container.find(".delist-chart-div").hide();
                        searchObj.show();

                        var allTrHtml = '';

//                        hidden();
                        $(itemJsonArray).each(
                            function(index,itemJson) {

                                var numIid = itemJson.numIid;
                                if (numIid === undefined|| numIid == null|| numIid <= 0) {
                                    itemJson.numIid = itemJson.id;
                                    numIid = itemJson.id;
                                }

                                var trHtml = DelistSearch.item.createItemRow(index,itemJson);
                                allTrHtml += trHtml;
                            });
                        tbodyObj.html(allTrHtml);
                    }
                }
            });
        },
        getParamData : function() {
            
            var paramData = {};
            var container = DelistSearch.init.getContainer();
            var searchKey = container.find(".search-input").val();
            
            if (searchKey === undefined || searchKey == null|| searchKey == "") {
                alert("请先输入要搜索的关键词！");
                return null;
            }
            
            var itemOrderType = container.find(".order-style-select").val();
            
            if (itemOrderType === undefined|| itemOrderType == null|| itemOrderType == "") {
                alert("请先选择宝贝要排序的方式！");
                return null;
            }
            
            var searchPages = container.find(".search-page-select").val();
            
            if (searchPages === undefined|| searchPages == null || searchPages == "") {
                alert("请先选择要搜索的页数！");
                return null;
            }
            var searchPlace = container.find(".search-place-select").val();

            paramData.searchKey = searchKey;
            paramData.itemOrderType = itemOrderType;
            paramData.searchPages = searchPages;
            paramData.searchPlace = searchPlace;

            return paramData;
        }
    }, DelistSearch.show);

    DelistSearch.item = DelistSearch.item || {};
    DelistSearch.item = $.extend({
        createItemRow : function(index, itemJson) {

            var href = 'http://item.taobao.com/item.htm?id='+ itemJson.numIid;
            var imgSrc = itemJson.picUrl/* + "_190x190.jpg" */;
            var trHtml = null;

            if (!(itemJson.dt == null)) {
                trHtml = ''
                        + '<tr>'
                        + '   <td><a href="'
                        + href
                        + '" target="_blank"><img class="item-img" style="width: 70px; height: 70px;" src="'
                        + imgSrc
                        + '" /></a> </td>'
                        + '   <td><a href="'
                        + href
                        + '" target="_blank"><span style="color: #666;">'
                        + itemJson.title + '</span> </td>'
                        + '   <td>' + itemJson.dt + '</td>'
                        + '</tr>';
            }
            return trHtml;
        }

    }, DelistSearch.item);

    DelistSearch.hourChart = DelistSearch.hourChart || {};
    DelistSearch.hourChart = $.extend({
        showChart : function(hourArray) {
            var container = DelistSearch.init.getContainer();
            var tbodyObj = container.find("table.delist-chart-table tbody.delist-chart-tbody");

            tbodyObj.html("");

            var allTrHtml = "";

            var totalNum = DelistSearch.hourChart.getTotalDelistNum(hourArray);

            for (var hourIndex = 0; hourIndex < 24; hourIndex++) {
                var trHtml = DelistSearch.hourChart.createHourRow(hourIndex, hourArray,totalNum);
                allTrHtml += trHtml;
            }

            var sumRow = DelistSearch.hourChart.createSumRow(hourArray, totalNum);
            allTrHtml += sumRow;

            tbodyObj.html(allTrHtml);

        },
        createSumRow : function(hourArray, totalNum) {

            var trHtml = '<tr>';

            trHtml += '<td style="font-weight: bold">24小时总和</td> ';

            for (var dayIndex = 0; dayIndex < 7; dayIndex++) {
                var sumDelistNum = 0;
                for (var hourIndex = 0; hourIndex < 24; hourIndex++) {
                    var hourDelistNum = DelistSearch.hourChart.getHourDelistNum(hourIndex,dayIndex, hourArray);
                    
                    sumDelistNum += hourDelistNum;
                }
                var sumTdHtml = DelistSearch.hourChart.createDaySumTd(dayIndex, sumDelistNum,totalNum);
                
                trHtml += sumTdHtml;
            }

            trHtml += DelistSearch.hourChart.createDaySumTd(7,totalNum, totalNum);

            trHtml += '</tr>';

            return trHtml;
        },
        createHourRow : function(hourIndex, hourArray, totalNum) {

            var hourStr = DelistSearch.hourChart.getHourStr(hourIndex);

            var trHtml = '<tr>';
            // 时间段
            trHtml += '<td style="font-weight: bold;">'
                    + hourStr + '</td>';

            // 下架宝贝数
            var sumDelistNum = 0;
            for (var dayIndex = 0; dayIndex < 7; dayIndex++) {
                var hourDelistNum = DelistSearch.hourChart.getHourDelistNum(hourIndex, dayIndex,hourArray);
                sumDelistNum += hourDelistNum;
                var tdHtml = DelistSearch.hourChart.createHourTd(hourDelistNum, totalNum);
                trHtml += tdHtml;
            }

            // 总和
            var sumTdHtml = DelistSearch.hourChart.createHourSumTd(7, sumDelistNum, totalNum);
            
            trHtml += sumTdHtml;

            trHtml += '</tr>';

            return trHtml;

        },
        // dayIndex：从周一到周日，hourIndex从0点到23点
        getHourDelistNum : function(hourIndex, dayIndex,hourArray) {

            var realIndex = dayIndex * 24 + hourIndex;

            var hourDelistNum = hourArray[realIndex];

            return hourDelistNum;
        },
        // totalNum是总的下架宝贝数
        createHourTd : function(hourDelistNum, totalNum) {

            var hourRate = hourDelistNum / totalNum;

            var hourRateStr = hourRate.toPercent(2);

            var tdHtml = '<td>' +
            // hourDelistNum + '&nbsp;/&nbsp' + hourRateStr +
            hourDelistNum + '</td>';

            return tdHtml;

        },
        createHourSumTd : function(dayIndex, sumDelistNum,
                totalNum) {
            var hourRate = sumDelistNum / totalNum;

            var hourRateStr = hourRate.toPercent(2);

            var tdHtml = ''
                    + '<td class="sum-delist-td" style="font-weight: bold; vertical-align: middle;">'
                    +
                    // ' <div style="z-index: 0;position:
                    // absolute; background: #a10000;width:
                    // 50px; height: 16px;"></div> ' +
                    // ' <div style="z-index: 1;position:
                    // absolute;width: ' + columnWidth + 'px;
                    // text-align: center;">' + sumDelistNum +
                    // '&nbsp;/&nbsp' + hourRateStr + '</div>' +
                    // ' <div class="sum-bar-div"
                    // style="z-index: 0;background:
                    // #a10000;"></div>' +
                    '   <div class="sum-delist-div" style="">'
                    + sumDelistNum + '&nbsp;/&nbsp'
                    + hourRateStr + '</div>' + '</td>';

            return tdHtml;
        },
        createDaySumTd : function(dayIndex, sumDelistNum,
                totalNum) {
            var hourRate = sumDelistNum / totalNum;

            var hourRateStr = hourRate.toPercent(2);

            var tdHtml = ''
                    + '<td class="sum-delist-td" style="font-weight: bold; vertical-align: middle;">'
                    + '   <div style="padding-bottom: 5px;">'
                    + sumDelistNum + '</div>' + '   <div>'
                    + hourRateStr + '</div>' + '</td>';

            return tdHtml;
        },
        getTotalDelistNum : function(hourArray) {
            var totalNum = 0;
            $(hourArray).each(function(index, hourNum) {
                totalNum += hourNum;
            });

            return totalNum;
        },
        getHourStr : function(hourIndex) {
            var hourStr = '';
            if (hourIndex <= 5) {
                hourStr = "凌晨";
            } else if (hourIndex <= 10) {
                hourStr = "上午";
            } else if (hourIndex <= 13) {
                hourStr = "中午";
            } else if (hourIndex <= 17) {
                hourStr = "下午";
            } else {
                hourStr = "晚上";
            }
            hourStr += hourIndex + ":00 - " + (hourIndex + 1)
                    + ":00";
            return hourStr;
        },
        // 建立条形图css
        createBarCss : function(tbodyObj) {
            var sumTdObjArray = tbodyObj.find("td.sum-delist-td");

            sumTdObjArray.each(function(index, sumTdEle) {
                
                var sumTdObj = $(sumTdEle);
                var sumDivObj = sumTdObj.find(".sum-delist-div");
                var barDivObj = sumTdObj.find(".sum-bar-div");

                var divWidth = sumTdObj.width();
                var divHeight = sumTdObj.height() + 15;

                alert(divWidth + ", " + divHeight);

                if (divWidth > 3) {
                    divWidth = divWidth - 3;
                }
                if (divHeight > 3) {
                    divHeight = divHeight - 3;
                }

                var barWidth = divWidth / 2;
                var barHeight = 0;

                if (divHeight > 16) {
                    barHeight = 16;
                } else {
                    barHeight = divHeight;
                }
                barDivObj.css("position", "absolute");
                sumDivObj.css("position", "absolute");

                barDivObj.css("width", barWidth + "px");
                barDivObj.css("height", barHeight + "px");

                sumDivObj.css("width", divWidth + "px");
                sumDivObj.css("height", divHeight + "px");

            });

        }
    }, DelistSearch.hourChart);

    DelistSearch.util = DelistSearch.util || {};
    DelistSearch.util = $.extend({
        parseSecondTime : function(millis) {

            var theDate = new Date();
            var offset = theDate.getTimezoneOffset() * 1000 * 60;

            var baseOffset = -480;// 东8区

            var realTime = millis - baseOffset * 1000 * 60 + offset;

            theDate = new Date(realTime);

            var year = theDate.getFullYear();
            var month = theDate.getMonth() + 1;// js从0开始取
            var date = theDate.getDate();
            var hour = theDate.getHours();
            var minutes = theDate.getMinutes();
            var second = theDate.getSeconds();

            if (month < 10) {
                month = "0" + month;
            }
            if (date < 10) {
                date = "0" + date;
            }
            if (hour < 10) {
                hour = "0" + hour;
            }
            if (minutes < 10) {
                minutes = "0" + minutes;R
            }
            if (second < 10) {
                second = "0" + second;
            }

            var timeStr = year + "-" + month + "-" + date + " " + hour + ":"
                    + minutes + ":" + second;
            return timeStr;
        }
    }, DelistSearch.util);

    DelistSearch.loadingImg = DelistSearch.loadingImg || {};
    DelistSearch.loadingImg = $.extend({
        showImg : function() {
            var div = document.getElementById("loading-img");
            if(div) {
                div.style.display = "inline";
            }
        },
        hideImg : function() {
            var div = document.getElementById("loading-img");
            if(div) {
                div.style.display = "none";
            }
        }
    }, DelistSearch.loadingImg);
})(jQuery, window));