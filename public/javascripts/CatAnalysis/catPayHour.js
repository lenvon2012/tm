
var TM = TM || {};
((function ($, window) {
    TM.CatPayHour = TM.CatPayHour || {};
    var CatPayHour = TM.CatPayHour;

    CatPayHour.Init = CatPayHour.Init || {};
    CatPayHour.Init = $.extend({
        doInit : function(container){
            CatPayHour.container = container;

            container.find(".first-cat-select").change(function() {
                CatPayHour.category.initSecondCatSelect([0, 0, 0]);
                $("body").focus();//在ie6下使其失去焦点
            });
            container.find(".second-cat-select").change(function() {
                CatPayHour.category.initThirdCatSelect([0, 0, 0]);
                $("body").focus();//在ie6下使其失去焦点
            });
            container.find(".third-cat-select").change(function() {
                CatPayHour.show.doShow();
                $("body").focus();//在ie6下使其失去焦点
            });

            CatPayHour.Init.initUserCatInfo();


        },
        initUserCatInfo: function(numIid) {

            var container = CatPayHour.Init.getContainer();

            $.ajax({
                url : '/CatTopWord/findUserMostCat',
                data : {numIid: 0},
                type : 'post',
                success : function(dataJson) {

                    var catJsonArray = CatPayHour.util.getAjaxResult(dataJson);
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

                    CatPayHour.category.initFirstCatSelect(userCatIdArray);

                }
            });
        },
        getContainer: function() {
            return CatPayHour.container;
        }
    }, CatPayHour.Init)


    CatPayHour.category = CatPayHour.category || {};
    CatPayHour.category = $.extend({
        initFirstCatSelect: function(userCatIdArray) {

            var container = CatPayHour.Init.getContainer();

            var firstCatSelectObj = container.find(".first-cat-select");


            $.ajax({
                url : '/cattopword/findLevel1',
                data : {},
                type : 'post',
                success : function(dataJson) {

                    firstCatSelectObj.html("");

                    if (CatPayHour.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    var catJsonArray = CatPayHour.util.getAjaxResult(dataJson);
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
                        var targetCid = CatPayHour.category.getSelectCid(userCatIdArray, 0);
                        CatPayHour.category.doSelectOption(firstCatSelectObj, targetCid);
                    }

                    //触发二级类目
                    CatPayHour.category.initSecondCatSelect(userCatIdArray);

                }
            });

        },
        initSecondCatSelect: function(userCatIdArray) {
            var container = CatPayHour.Init.getContainer();

            var firstCatSelectObj = container.find(".first-cat-select");
            var parentCid = firstCatSelectObj.val();

            var secondCatSelectObj = container.find(".second-cat-select");


            $.ajax({
                url : '/cattopword/findLevel2or3',
                data : {parentCid: parentCid},
                type : 'post',
                success : function(dataJson) {
                    secondCatSelectObj.html("");
                    if (CatPayHour.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    var catJsonArray = CatPayHour.util.getAjaxResult(dataJson);
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
                        var targetCid = CatPayHour.category.getSelectCid(userCatIdArray, 1);
                        CatPayHour.category.doSelectOption(secondCatSelectObj, targetCid);
                    }

                    //触发三级类目
                    CatPayHour.category.initThirdCatSelect(userCatIdArray);

                }
            });
        },
        initThirdCatSelect: function(userCatIdArray) {
            var container = CatPayHour.Init.getContainer();

            var secondCatSelectObj = container.find(".second-cat-select");
            var parentCid = secondCatSelectObj.val();

            var thirdCatSelectObj = container.find(".third-cat-select");


            $.ajax({
                url : '/cattopword/findLevel2or3',
                data : {parentCid: parentCid},
                type : 'post',
                success : function(dataJson) {
                    thirdCatSelectObj.html("");
                    if (CatPayHour.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    var catJsonArray = CatPayHour.util.getAjaxResult(dataJson);
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
                        var targetCid = CatPayHour.category.getSelectCid(userCatIdArray, 2);
                        CatPayHour.category.doSelectOption(thirdCatSelectObj, targetCid);
                    }

                    CatPayHour.show.doShow();

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
    }, CatPayHour.category);

    CatPayHour.show = CatPayHour.show || {};
    CatPayHour.show = $.extend({
        doShow : function(){
            var container = CatPayHour.Init.getContainer();
            var cid = CatPayHour.show.getParamData();
            if (cid <= 0) {
                container.find('.catPayHourContent no-data').show();
                return;
            }
            $.get("/CatAnalysis/getNewPayHourDistributeByCid", {cid : cid}, function(data){
                if(data === undefined || data == null) {
                    container.find('.catPayHourContent no-data').show();
                    return;
                }
                CatPayHour.show.drawHighCharts(data);
            })
        },
        drawHighCharts : function(data) {
            var xAxis = CatPayHour.show.getXAxis();
            chart = new Highcharts.Chart({
                chart : {
                    renderTo : "catPayHourCharts",
                    defaultSeriesType: 'column', //图表类型line(折线图)
                    inverted: false
                },
                credits : {
                    enabled: false   //右下角不显示LOGO
                },
                title: {
                    text: '行业分析'
                }, //图表标题
                xAxis: {  //x轴
                    categories: xAxis, //x轴标签名称
                    gridLineWidth: 1, //设置网格宽度为1
                    lineWidth: 4,  //基线宽度
                    labels:{
                        //x轴标签位置：距X轴下方26像素
                        //倾斜度
                        align: 'right'
                    }
                },
                yAxis: [{  //y轴
                    title: {text: ''}, //标题
                    lineWidth: 2 //基线宽度
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
                        name: '购买热度',
                        data: data,
                        yAxis:0
                    }
                ],
                legend:{
                    layout:"horizontal"
                }
            });
        },
        getXAxis : function(){
            /*var xAxis = ["00:00-01:00", "01:00-02:00","02:00-03:00","03:00-04:00","04:00-05:00","05:00-06:00","06:00-07:00","07:00-08:00","08:00-09:00","09:00-10:00",
                "10:00-11:00","11:00-12:00","12:00-13:00","13:00-14:00","14:00-15:00","15:00-16:00","16:00-17:00","17:00-18:00","18:00-19:00","19:00-20:00","20:00-21:00",
                "21:00-22:00","22:00-23:00","23:00-00:00"];*/

            var xAxis = ["00:00", "01:00","02:00","03:00","04:00","05:00","06:00","07:00","08:00","09:00",
                "10:00","11:00","12:00","13:00","14:00","15:00","16:00","17:00","18:00","19:00","20:00",
                "21:00","22:00","23:00"];
            return xAxis;
        },
        getParamData: function() {

            var container = CatPayHour.Init.getContainer();
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
    }, CatPayHour.show);

    CatPayHour.util = CatPayHour.util || {};
    CatPayHour.util = $.extend({
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
    }, CatPayHour.util);

})(jQuery,window));

