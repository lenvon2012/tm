
((function ($, window) {

//    TM.autoTitle = TM.autoTitle || {};
//
//    var autoTitle = TM.autoTitle;
//
//    autoTitle.CatAnalysis = autoTitle.CatAnalysis || {};
//
//    autoTitle.CatAnalysis = $.extend({
//        init:function (container) {
//            autoTitle.CatAnalysis.initCatAnalysis(container);
//        },
//        initCatAnalysis:function (container) {
//            container.empty();
//            autoTitle.CatAnalysis.initLevelSelect(container);
//        },
//        initLevelSelect:function (container) {
//            $.ajax({
//                //async : false,
//                url:'/Items/findLevel1',
//                data:{},
//                type:'post',
//                success:function (data) {
//                    //初始化第一个
//                    $(".selectCat1").empty();
//
//                    if (data == null || data.length == 0) {
//                        TM.Alert.load("没有找到一级类目数据");
//                    } else {
//                        for (var i = 0; i < data.length; i++) {
//                            var option = $('<option catId="' + data[i].cid + '">' + data[i].name + '</option>');
//                            $(".selectCat1").append(option);
//                        }
//                    }
//                    // $(".selectCat1").append($('<option></option>'));
//                    //设置事件$('.XXX).change(function(){});
//                    autoTitle.CatAnalysis.Level2Select(container);
//                }
//            });
//        },
//        Level2Select:function (container) {
//            $(".selectCat1").unbind();
//            $(".selectCat1").change(function () {
//                var catId = $(".selectCat1 option:selected").attr('catId');
//                var name = $(".selectCat1 option:selected").html();
//
//                $.ajax({
//                    url:'/Items/findLevel2',
//                    data:{catId:catId},
//                    type:'post',
//                    success:function (data) {
//                        $(".selectCat2").empty();
//                        var Choselevel = 0;
//                        if (data == null) {
//                            TM.Alert.load("获取类目数据出错");
//                        } else if (data.length == 0) {
//                            $(".selectCat2").append($('<option catId="' + catId + '">' + name + '</option>'));
//                            Choselevel = 1;
//                            $.cookie('Choselevel', 1);
//                        } else {
//                            for (var i = 0; i < data.length; i++) {
//                                var option = $('<option catId="' + data[i].cid + '">' + data[i].name + '</option>');
//                                $(".selectCat2").append(option);
//                            }
//                            autoTitle.CatAnalysis.initPropSelect(container);
//                            $('.selectCat2').trigger("change");
//                        }
//
//                    }
//
//                })
//            });
//            $(".selectCat1").trigger('change');
//        },
//        initPropSelect : function(container) {
//            $('.selectCat2').unbind('change').change(function () {
//                $(".propselect").empty();
//                var cid = $(".selectCat2 option:selected").attr('catId');
//                $.get('/items/catHotProps', {cid:cid}, function (res) {
//                    if(res.length == 0) {
//                        $(".propselect").append($('<option>该类目暂无热销属性</option>'));
//                    } else {
//                        $(res).each(function(i, name){
//                            $('.propselect').append($('<option pid="'+name.pid+'" name="'+name.pname+'">'+name.pname+'</option>'));
//                        });
//                        $('.propselect').unbind('change').change(function(){
//                            var pid = $('.propselect option:selected').attr("pid");
//                            autoTitle.CatAnalysis.createHighChart(res, pid, container);
//                        });
//                        $('.propselect').trigger('change');
//                    }
//
//                });
//            });
//        },
//        createHighChart : function(res, pid, container) {
//            container.find('#catanalysisoplogs').empty();
//            $(res).each(function(i, prop){
//                if(parseInt(prop.pid) == parseInt(pid)) {
//                    autoTitle.CatAnalysis.genHighChart(pid, prop.list);
//                }
//            });
//        },
//        CreateNewTable:function (container) {
//            var cid = $(".selectCat2 option:selected").attr('catId');
//            var htmls = [];
//            $.get('/items/catHotProps', {cid:cid}, function (res) {
////            htmls.push('<table><thead><th>属性名</th><th>热销属性词</th></thead><tbody>');
//                container.find('table.catanalysisoplogs').remove();
//                htmls.push('<table class="catanalysisoplogs"><thead><th style="width:120px;">属性名</th><th>热销属性词/搜索热度</th></thead><tbody>');
//                if (!res || res.length == 0) {
//                    // TODO no res temp...
//                    htmls.push('<tr><td colspan="2">您选择的类目暂无热销属性</td></tr></tbody></table>');
//                    container.append(htmls.join(""));
//                    return;
//                } else {
//                    $.each(res, function (i, prop) {
//                        htmls.push('<tr class="propTr"><td class="greybottom"><div><b>' + prop.pname + '</b></div><div>热度:<b class="red"> ' + prop.pv + '</b></div></td>');
//                        htmls.push('<td class="greybottom" ><div style="width: 670px;height: 400px;" id="chart'+prop.pid+'"></div> </td></tr>');
//                    })
//                }
//                htmls.push('</tbody></table>');
//                container.append($(htmls.join("")));
//                autoTitle.CatAnalysis.genPropCharts(res);
//            });
//        },
//        genPropCharts : function(res){
//            $.each(res, function (i, prop) {
//                autoTitle.CatAnalysis.genHighChart(prop.pid, prop.list);
//                /*var tmphighchart = $('#tmphighchart').clone();
//                tmphighchart.attr("id", prop.pid);
//                tmphighchart.show();
//                $('#'+prop.pid).append(tmphighchart);*/
//            })
//        },
//        genHighChart : function(id, list){
//            if(list === undefined || list == null){
//                $('#catanalysisoplogs').empty();
//                return;
//            }
//            if(list.length == 0) {
//                $('#catanalysisoplogs').empty();
//                return;
//            }
//            var xAxis = autoTitle.CatAnalysis.createXAxis(list);
//            var pvArr = autoTitle.CatAnalysis.createPvArr(list);
//            //var clickArr = autoTitle.CatAnalysis.createClickArr(list);
//            chart = new Highcharts.Chart({
//                chart : {
//                    renderTo : "catanalysisoplogs",
//                    defaultSeriesType: 'column', //图表类型line(折线图)
//                    inverted: true
//                },
//                credits : {
//                    enabled: false   //右下角不显示LOGO
//                },
//                title: {
//                    text: '行业分析'
//                }, //图表标题
//                xAxis: {  //x轴
//                    categories: xAxis, //x轴标签名称
//                    gridLineWidth: 1, //设置网格宽度为1
//                    lineWidth: 2,  //基线宽度
//                    labels:{
//                         //x轴标签位置：距X轴下方26像素
//                        //倾斜度
//                        align: 'right'
//                    }
//                },
//                yAxis: [{  //y轴
//                    title: {text: ''}, //标题
//                    lineWidth: 2 //基线宽度
//                }],
//                plotOptions:{ //设置数据点
//                    line:{
//                        dataLabels:{
//                            enabled:true  //在数据点上显示对应的数据值
//                        },
//                        enableMouseTracking: true //取消鼠标滑向触发提示框
//                    }
//                },
//                series: [
//                    {  //数据列
//                        name: '关键词热度',
//                        data: pvArr,
//                        yAxis:0
//                    }
//                ],
//                legend:{
//                    layout:"horizontal"
//                }
//            });
//        },
//        createXAxis : function(list){
//            var xAxis = [];
//            $(list).each(function(i, name){
//                xAxis.push(name.name)
//            });
//            return xAxis;
//        },
//        createPvArr : function(list){
//            var pvArr = [];
//            $(list).each(function(i, name){
//                if(parseInt(name.pv) > 0){
//                    pvArr.push(parseInt(name.pv));
//                } else {
//                    pvArr.push(-parseInt(name.pv));
//                }
//
//            });
//            return pvArr;
//        },
//        createClickArr : function(list){
//            var clickArr = [];
//            $(list).each(function(i, name){
//                clickArr.push(name.click)
//            });
//            return clickArr;
//        },
//        CreateTable:function (container) {
//            var cid = $(".selectCat2 option:selected").attr('catId');
//            var htmls = [];
//            $.get('/items/catHotProps', {cid:cid}, function (res) {
////            htmls.push('<table><thead><th>属性名</th><th>热销属性词</th></thead><tbody>');
//                container.find('table.catanalysisoplogs').remove();
//                htmls.push('<table class="catanalysisoplogs"><thead><th style="width:120px;">属性名</th><th>热销属性词/搜索热度</th></thead><tbody>');
//                if (!res || res.length == 0) {
//                    // TODO no res temp...
//                    htmls.push('<tr><td colspan="2">您选择的类目暂无热销属性</td></tr></tbody></table>');
//                    container.append(htmls.join(""));
//                    return;
//                } else {
//
//                    var pPvMax = 0;
//                    $.each(res, function (i, prop) {
//                        if (i == 0) {
//                            pPvMax = prop.pv;
//                        }
//
//                        htmls.push('<tr class="propTr"><td class="greybottom"><div><b>' + prop.pname + '</b></div><div>热度:<b class="red"> ' + prop.pv + '</b></div></td>');
//                        // values...
//                        var maxWidth = 300;
//                        htmls.push('<td class="greybottom" > <table width="100%">');
//                        var vPvMax = 0;
//                        $.each(prop.list, function (j, value) {
//                            if (j == 0 && vPvMax < value.pv) {
//                                vPvMax = value.pv;
//                            }
//                            if (vPvMax < 1) {
//                                vPvMax = 1;
//                            }
//
//                            var currWidth = (j == 0) ? maxWidth : (value.pv * maxWidth / vPvMax);
//                            currWidth = Math.round(currWidth);
//                            if (currWidth < 5) {
//                                currWidth = 5;
//                            }
//
//                            //                    genKeywordSpan.gen({"text":data[i].word,"callback":autoTitle.util.putIntoTitle,"enableStyleChange":true})
//                            htmls.push('<tr><td width="140px"><span class="addTextWrapper shadowbase" style="padding-top:5px;"><img src="/img/btns/addblue.png" >' + value.name + '</span></td><td style="text-align: left;width:300px;"><div style="width:' +
//                                currWidth + 'px;height:6px;border-top: 6px solid #2D8ABE"></div></td><td>' + value.pv + '</td></tr>');
//                        });
//
//                        htmls.push('</table></td></tr>');
//                    })
//                }
//                htmls.push('</tbody></table>');
//                var res = $(htmls.join(''));
//                res.find('.addTextWrapper').click(function () {
//                    autoTitle.util.putIntoTitle($(this).text(), $(this), container);
//                });
//                container.append(res);
//            });
//        }
//    }, autoTitle.CatAnalysis)

})(jQuery, window));