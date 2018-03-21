/**
 * 显示店铺诊断的结果
 * @type {*}
 */
var ShopDiag = ShopDiag || {};

ShopDiag.init = ShopDiag.init || {};

ShopDiag.init = $.extend({
    diagnose: function() {
    	if(true){
    		return;
    	}

        $.ajax({
                url: '/Titles/scoreSread',
            dataType: 'json',
            type: 'post',
            data: {},
            error: function() {
            },
            success: function (scoreMap) {
				ShopDiag.init.showChart(scoreMap);
            }
        });
    },
    //显示图表
    showChart: function(scoreMap) {
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
        var titleX = 50;
        var titleY = 20;
        var chart = new Highcharts.Chart({
            chart: {
//                width : 200,
                type: 'pie',
                renderTo: 'chartContainer'
            },
            title: {
                text: '店铺标题得分',
                align: "left",
                verticalAlign: 'top',
                x: titleX,
                y: titleY,
                style: {
                    fontWeight: 'bold',
                    color: '#333',
                    fontSize: '16px',
                    fontFamily: '微软雅黑'
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
                borderWidth: 0,
                itemMarginTop: 5,
                itemMarginBottom: 5,
                itemStyle: {
                    fontWeight: 'bold'
                },
                symbolPadding: 10,
                symbolWidth: 30
            },
            plotOptions: {
                pie: {
                    size : 100,
                    center: [-50, 80],
                    allowPointSelect: true,
                    cursor: 'pointer',
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
}, ShopDiag.init);

$(document).ready(function() {
    ShopDiag.init.diagnose();
});