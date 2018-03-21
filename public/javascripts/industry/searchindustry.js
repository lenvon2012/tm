

((function ($, window) {
    TM.SearchIndustry = TM.SearchIndustry || {};

    var SearchIndustry = TM.SearchIndustry;

    SearchIndustry.init = SearchIndustry.init || {};
    SearchIndustry.init = $.extend({
        doInit: function(container) {
            SearchIndustry.container = container;

            container.find(".search-industry-btn").click(function() {
                SearchIndustry.search.doSearch();
            });

            container.find(".search-input").keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    SearchIndustry.search.doSearch();
                }
            });

            var tabBtns = container.find(".search-tab-btn");
            tabBtns.click(function() {
                tabBtns.removeClass("select");
                $(this).addClass("select");
                SearchIndustry.search.doSearch();
            });

            container.find(".price-interval-select").change(function() {
                SearchIndustry.search.doSearch();
            });
        },
        getContainer: function() {
            return SearchIndustry.container;
        }
    }, SearchIndustry.init);


    SearchIndustry.search = SearchIndustry.search || {};
    SearchIndustry.search = $.extend({
        doSearch: function() {
            var container = SearchIndustry.init.getContainer();

            var paramData = SearchIndustry.search.getParamData();
            if (paramData === undefined || paramData == null) {
                return;
            }

            var tabDivObj = SearchIndustry.search.getTargetTabDiv();
            if (tabDivObj.length <= 0) {
                alert("系统出现异常，找不到对应的页面！");
                return;
            }

            container.find(".search-result-div").hide();
            tabDivObj.show();

            if (tabDivObj.hasClass("search-summary-div")) {
                SearchIndustry.summary.doShow(tabDivObj, paramData);
            } else if (tabDivObj.hasClass("search-item-price-div")) {
                SearchIndustry.analysePrice.doShow(tabDivObj, paramData);
            }

        },
        getTargetTabDiv: function() {
            var container = SearchIndustry.init.getContainer();
            var selectBtnObj = container.find(".search-tab-btn.select");

            var targetDivCss = selectBtnObj.attr("targetDiv");
            var tabDivObj = container.find("." + targetDivCss);

            return tabDivObj;
        },
        getParamData: function() {
            var searchRule = {};

            var container = SearchIndustry.init.getContainer();

            var searchKey = container.find(".keyword-input").val();
            if (searchKey === undefined || searchKey == null || searchKey == "") {
                alert("请先输入要搜索的关键词！");
                return null;
            }
            var itemOrderType = container.find(".order-style-select").val();
            if (itemOrderType === undefined || itemOrderType == null || itemOrderType == "") {
                alert("请先选择宝贝要排序的方式！");
                return null;
            }
            var searchPages = container.find(".search-page-select").val();
            if (searchPages === undefined || searchPages == null || searchPages == "") {
                alert("请先选择要搜索的页数！");
                return null;
            }

            searchRule.searchKey = searchKey;
            searchRule.itemOrderType = itemOrderType;
            searchRule.searchPages = searchPages;

            searchRule.startPrice = container.find(".start-price-input").val();
            searchRule.endPrice = container.find(".end-price-input").val();
            searchRule.startSales = container.find(".start-sales-input").val();
            searchRule.endSales = container.find(".end-sales-input").val();


            var paramData = {};
            paramData.searchRule = searchRule;

            return paramData;
        }
    }, SearchIndustry.search);



    SearchIndustry.summary = SearchIndustry.summary || {};
    SearchIndustry.summary = $.extend({
        doShow: function(summaryDivObj, paramData) {
            $.ajax({
                url : '/SearchIndustry/summaryIndustryInfo',
                data : paramData,
                type : 'post',
                success : function(dataJson) {

                    if (TM.IndustryBase.judgeAjaxResult(dataJson) == false) {
                        return;
                    }

                    var summaryJson = TM.IndustryBase.getAjaxResult(dataJson);
                    summaryDivObj.find(".avg-price-span").html((summaryJson.avgPrice / 100).toFixed(2));
                    summaryDivObj.find(".max-price-span").html((summaryJson.maxPrice / 100).toFixed(2));
                    summaryDivObj.find(".min-price-span").html((summaryJson.minPrice / 100).toFixed(2));

                    summaryDivObj.find(".avg-payamount-span").html(summaryJson.avgPayamount.toFixed(2));
                    summaryDivObj.find(".item-num-span").html(summaryJson.itemNum);
                    summaryDivObj.find(".seller-num-span").html(summaryJson.sellerNum);

                }
            });
        }
    }, SearchIndustry.summary);


    SearchIndustry.analysePrice = SearchIndustry.analysePrice || {};
    SearchIndustry.analysePrice = $.extend({
        doShow: function(priceDivObj, paramData) {

            var splitNum = priceDivObj.find(".price-interval-select").val();
            if (splitNum == "") {
                alert("请先选择价格分段个数！");
                return;
            }

            paramData.splitNum = splitNum;

            var chartDivObj = priceDivObj.find(".item-price-result-div");
            chartDivObj.html("");
            var chartObj = $('<div id="item-price-interval-chart" style="width: 100%;height: 450px;"></div> ');
            chartDivObj.append(chartObj);

            $.ajax({
                url : '/SearchIndustry/searchPriceIntervalInfos',
                data : paramData,
                type : 'post',
                success : function(dataJson) {

                    if (TM.IndustryBase.judgeAjaxResult(dataJson) == false) {
                        return;
                    }

                    var priceJsonList = TM.IndustryBase.getAjaxResult(dataJson);

                    SearchIndustry.analysePrice.showChart(chartObj, priceJsonList);
                }
            });


        },
        showChart: function(chartObj, priceJsonList) {

            var xArray = [];
            var payamountArray = [];
            var itemNumArray = [];

            if (priceJsonList === undefined || priceJsonList == null || priceJsonList.length <= 0) {
                return;
            }
            $(priceJsonList).each(function(index, priceJson) {
                xArray[xArray.length] = priceJson.priceInterval;
                payamountArray[payamountArray.length] = priceJson.payamount;
                itemNumArray[itemNumArray.length] = priceJson.itemNum;
            });

            var chart = new Highcharts.Chart({
                chart : {
                    renderTo : 'item-price-interval-chart',
                    defaultSeriesType: 'line' //图表类型line(折线图)
                },
                credits : {
                    enabled: false   //右下角不显示LOGO
                },
                title: {
                    text: '行业宝贝价格分布'
                }, //图表标题
                xAxis: {  //x轴
                    categories: xArray, //x轴标签名称
                    //gridLineWidth: 1, //设置网格宽度为1
                    lineWidth: 1,  //基线宽度
                    labels:{y:20}  //x轴标签位置：距X轴下方26像素
                },
                yAxis: [{  //y轴
                    title: {text: '宝贝个数'}
                }, {
                    title: {text: '成交量'}, //标题
                    opposite: true
                }],
                plotOptions:{ //设置数据点
                    line:{
                        dataLabels:{
                            enabled:true  //在数据点上显示对应的数据值
                        },
                        enableMouseTracking: true //取消鼠标滑向触发提示框
                    }
                },
                series: [
                    {  //数据列
                        name: '宝贝个数',
                        data: itemNumArray,
                        yAxis:0
                    },
                    {  //数据列
                        name: '成交量',
                        data: payamountArray,
                        yAxis:1
                    }
                ]
            });
        }
    }, SearchIndustry.analysePrice);




})(jQuery,window));