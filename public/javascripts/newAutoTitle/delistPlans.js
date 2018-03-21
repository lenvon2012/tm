/**
 * Created by uttp on 7/15/14.
 */
var TM = TM || {};

((function($, window){
    TM.delistPlans = TM.delistPlans || {};

    var delistPlans = TM.delistPlans;

    delistPlans.init = delistPlans.init || {} ;
    delistPlans.init = $.extend({
        doInit:function(){
            delistPlans.init.doShowDiff();
            delistPlans.init.initSimpleCreatePlan();
            delistPlans.init.doShowDelistPlans();
            delistPlans.init.initStartBtn();
        },
        doShowDiff:function(){
            $.ajax({
                type:'post',
                url:'/AutoDelist/queryDelistDistribute',
                success:function(data){
                    if(data == null || data.length != 2){
                        TM.Alert.load("获取数据失败，请等会再试！");
                        return;
                    }
                    var id = $("#delistDiff");
                    chart = new Highcharts.Chart({
                        chart : {
                            renderTo : 'delistDiff',
                            defaultSeriesType: 'spline' //图表类型line(折线图)
                        },
                        credits: {
                            enabled: false //右下角不显示LOGO
                        },
                        title: {
                            text: '计划上架宝贝与当前上架宝贝比较',
                            x: -20
                        },
                        xAxis: {
                            categories: ['周一', '周二', '周三', '周四', '周五', '周六','周日']
                        },
                        yAxis: {
                            title: {
                                text: '宝贝个数'
                            },
                            plotLines: [{
                                value: 0,
                                width: 1,
                                color: '#808080'
                            }]
                        },
                        tooltip: {
                            valueSuffix: '个宝贝'
                        },
                        legend: {
                            layout: "horizontal",
                            align: 'center',
                            verticalAlign: 'bottom',
                            y: 10
                        },
                        series: [{
                            'color':'#3dab31',
                            name: '预期上架时间',
                            data: data[0]
                        }, {
                            'color':'#ab0c88',
                            name: '当前上架时间',
                            data: data[1]
                        }]
                    });
                }
            });
        },
        initSimpleCreatePlan:function(){
            $('.simple-delist-btn').click(function(){
                if (confirm("确定要创建均匀上架计划，创建后，在售宝贝将会被均匀分配到高峰时间段？") == false) {
                    return;
                }

                $.ajax({
                    type:'post',
                    url:'/delistplan/createSimpleDelistPlan',
                    success:function(data){
                        if (TM.DelistBase.util.judgeAjaxResult(data) == false) {

                        } else {
                            alert("上下架计划创建成功！");
                            window.location.reload(true);
                        }

                    }
                })
            })
        },
        doShowDelistPlans:function(){
            $.ajax({
                type:'post',
                url:'/DelistPlan/queryDelistPlanList',
                success:function(data){
                    if (TM.DelistBase.util.judgeAjaxResult(data) == false) {
                        return;
                    }
                    var i, j;
                    for(i = 0; i < data.results.length; ++i){
                        if(data.results[i].distriNums == null){
                            data.results[i].itemNums = 0;
                            continue;
                        }
                        var arr = data.results[i].distriNums.split(",");
                        var itemNums = 0;
                        for(j = 0; j < arr.length; ++j){
                            itemNums +=parseInt(arr[j]);
                        }
                        data.results[i].itemNums = itemNums;
                    }
                    var tbody = $('.delistPlans tbody');
                    tbody.empty();
                    var rows = $('#planRowTmpl').tmpl(data.results);
                    tbody.append(rows);
                    delistPlans.init.reverseRows(rows);
                }
            })
        },
        reverseRows:function(rows){
            for(var i = 0; i < rows.length; ++i){
                if($(rows[i]).attr("status") == 1){
                    $(rows[i]).find(".statusTxt").empty().append("<span style='color:green'>已开启</span>");
                    $(rows[i]).find(".startBtn").hide();
                }else if($(rows[i]).attr("status") == 2){
                    $(rows[i]).find(".statusTxt").empty().append("<span style='color:red'>未开启</span>");
                    $(rows[i]).find(".pauseBtn").hide();
                }
            }
        },
        initStartBtn:function(){
            $(".delistPlans tbody .startBtn, .delistPlans tbody .pauseBtn, .delistPlans tbody .deleteBtn").live("click", function(){
                var url;
                if($(this).hasClass("startBtn")){
                    url = "/delistplan/turnOnPlan";
                }else if($(this).hasClass("pauseBtn")){
                    url = "/delistplan/turnOffPlan";
                }else if($(this).hasClass("deleteBtn")){
                    if(confirm("确定要删除该上下架计划？") == false){
                        return;
                    }
                    url = "/delistplan/deletePlan"
                }
                var planId = $(this).attr("planId");
                var current = $(this);
                $.ajax({
                    type:'post',
                    url:url,
                    data:{
                        planId:planId
                    },
                    success:function(data){
                        if (TM.DelistBase.util.judgeAjaxResult(data) == false) {
                            return;
                        }
                        if(current.hasClass("deleteBtn"))
                            delistPlans.init.doShowDelistPlans();
                        else if(current.hasClass("startBtn")){
                            current.parent().parent().attr("status", 1);
                        }else if(current.hasClass("pauseBtn")){
                            current.parent().parent().attr("status", 2);
                        }
                        delistPlans.init.initRow(current.parent().parent());
                    }
                })
            })
        },
        initRow:function(row){
            if(row.attr("status") == 1){
                row.find(".statusTxt").empty();
                row.find(".statusTxt").append("<span style='color:green'>已开启</span>");
                $(row).find(".startBtn").hide();
                $(row).find(".pauseBtn").show();
            }else if(row.attr("status") == 2){
                $(row).find(".statusTxt").empty().append("<span style='color:red'>未开启</span>");
                $(row).find(".pauseBtn").hide();
                $(row).find(".startBtn").show();
            }
        }
    }, delistPlans.init);

})(jQuery, window));
