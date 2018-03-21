((function ($, window) {
    TM.skinTitle = TM.skinTitle || {};
    TM.skinTitle = $.extend({
        genSkinShopDiagDiv : function(container){
            $.ajax({
                url: '/Titles/scoreSread',
                dataType: 'json',
                type: 'post',
                data: {},
                error: function() {
                },
                success: function (scoreMap) {
                    TM.skinTitle.showChart(scoreMap);
                }
            });
            TM.skinTitle.genSkinTitleInfo(container);
           // TM.ShopDiag.genShopDiagDiv(container);
        },
        genSkinTitleInfo : function(container){
            var info = $('<div class="skinTitleInfo"></div>');
            $.get("/Diag/shop",function(data){
                info.append($('<div style="text-align: center;font-size: 16px;font-weight: bold;">您的店铺标题平均得分为:<span style="font-size: 24px;color: red;font-weight: bolder;">'+data.titleScore+'</span></div>'));
                var conversionRate = (data.conversionRate/100).toFixed(2);
                var conversationRateExp;
                if(data.conversionRate < 50){
                    conversationRateExp = "， 相比其他卖家有点低哦，内功是关键啊亲";
                }else if(data.conversionRate > 100){
                    conversationRateExp = "， 有点虚高，注意控制哦";
                } else{
                    conversationRateExp = "， 非常不错，继续努力哦";
                }
                info.append($('<div style="text-align: center;font-size: 16px;font-weight: bold;">转化率为：<span style="font-size: 24px;color: red;font-weight: bolder;">'+conversionRate+'%</span>'+conversationRateExp+'</div>'));
                info.append($('<a href="/SkinTitles/autoTitle"><div style="width: 260px;height: 75px;" class="skinTitleYouhuaBtn"></div></a>'));
            });
           // info.append(TM.ShopDiag.createDiagBottom());
            container.append(info);
        },
        showChart: function(scoreMap, shopScore) {
            var totalCount = 0;
            var totalScore = 0;
            var map = {//防止scoreMap中有其他key
                "5": true,
                "4": true,
                "3": true,
                "12": true
            }
            for (var key in scoreMap) {
                if (map[key] == true) {
                    totalCount++;
                    totalScore += scoreMap[key];
                }
            }

            var index = 0;
            var scoreSread = {};
            var totalSread = 0;
            for (var key in scoreMap) {
                if (map[key] != true)
                    continue;
                if (index == totalCount - 1) {
                    var sread = Math.round(10000 - totalSread);
                    scoreSread[key] = sread / 100;
                } else {
                    var score = scoreMap[key];
                    var sread = Math.round(score * 10000 / totalScore);
                    totalSread += sread;
                    scoreSread[key] = sread / 100;
                }
                index++;
            }


            var dataJson = {};
            var seriesData = [];
            var value = scoreSread["5"];
            if (!(value === undefined || value == null)) {
                var str = '优秀 ' + value + "%";
                var item = {
                    name: str,
                    y: value,
                    color: "#7876ff"
                };
                seriesData[seriesData.length] = item;
            }
            value = scoreSread["4"];
            if (!(value === undefined || value == null)) {
                var str = '良好 ' + value + "%";
                var item = {
                    name: str,
                    y: value,
                    color: "#ff78aa"
                };
                seriesData[seriesData.length] = item;
            }
            value = scoreSread["3"];
            if (!(value === undefined || value == null)) {
                var str = '及格  ' + value + "%";
                var item = {
                    name: str,
                    y: value,
                    color: "#70ff58"
                };
                seriesData[seriesData.length] = item;
            }
            value = scoreSread["12"];
            if (!(value === undefined || value == null)) {
                var str = '不及格 ' + value + "%";
                var item = {
                    name: str,
                    y: value,
                    color: "#ffef52"
                };
                seriesData[seriesData.length] = item;
            }
            dataJson.data = seriesData;
            var titleX = 100;
            var titleY = 20;
            var chart = new Highcharts.Chart({
                chart: {
                    //width : 780,
                    type: 'pie',
                    renderTo: 'chartContainer',
                    shadow:true
                },
                title: {
                    text: '店铺标题得分',
                    align: "center",
                    verticalAlign: 'top',
                    x: titleX,
                    y: titleY,
                    style: {
                        fontWeight: 'bold',
                        color: '#333',
                        fontSize: '16px',
                        fontFamily: '微软雅黑',
                        borderWidth:2
                    }
                },
                tooltip: {
                    pointFormat: '',
                    percentageDecimals: 1,
                    style: {
                        fontWeight: 'bold'
                    }
                },
                credits: {
                    enabled: false
                },
                legend: {
                    layout: 'vertical',
                    align: 'left',
                    verticalAlign: 'middle',
                    borderWidth: 2,
                    itemMarginTop: 5,
                    itemMarginBottom: 5,
                    x: 50,
                    itemStyle: {
                        fontWeight: 'bold'
                    },
                    symbolPadding: 10,
                    symbolWidth: 30
                },
                plotOptions: {
                    pie: {
                        //size : 100,
                        //center: [-50, 80],
                        allowPointSelect: true,
                        cursor: 'pointer',
                        borderWidth:2,
                        dataLabels: {
                            enabled: false
                        },
                        showInLegend: true
                    }
                },
                series: [{
                    data: dataJson.data,
                    color: 'red'

                }]
            });
        }
    },TM.skinTitle);
})(jQuery, window));

