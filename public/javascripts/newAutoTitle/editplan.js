/**
 * Created by uttp on 7/16/14.
 */
var TM = TM || {};

((function($, window){
    TM.editplan = TM.editplan || {};

    var editplan = TM.editplan;

    editplan.init = $.extend({
        doInit:function(planId){
            editplan.init.initHead();
            editplan.init.planId = planId;
            editplan.init.initStatus();
            editplan.init.initConfAction();
            editplan.singleItemEdit.doInit();
            editplan.todayDelist.doInit();
            editplan.delistLog.doInit();
            editplan.init.initConfig($(".configure .configtitle .conftxt"));
            editplan.init.initConfig($(".configure .configtitle .confbtn"));
        },
        initHead:function(){
            $('.nav_bar').find('.header-nav').removeClass('current');
            $('.nav_bar').find('.auto-delist').addClass("current");
        },
        initStatus:function(){
            $.post("/delistplan/queryDelistPlan",{planId:editplan.init.planId},function(data){
                if(data == null || data.success == false){
                    TM.Alert.load("获取计划信息失败！");
                    return;
                }
                $('.plantitle').append("<span>" + data.results.title + "</span>");
                var status = "<span class='tooltip' style='position:relative;opacity:initial;'>";
                if(data.results.status == 1){
                    status += "已开启</span>";
                    $('.planstatus').append(status);
                    $(".planstatus span").addClass("on");
                }else if(data.results.status == 2){
                    status +="已关闭</span>";
                    $('.planstatus').append(status);
                    $(".planstatus span").addClass("shut");
                }
                if(data.results.delistConfig & 1){
                    $(".configure .config input[name='addnewitem'][status='true']").prop("checked", true);
                }else{
                    $(".configure .config input[name='addnewitem'][status='false']").prop("checked", true);
                }
                if(data.results.delistConfig & (1<<3)){
                    $(".configure .config input[name='first10sale'][status='true']").prop("checked", true);
                }else{
                    $(".configure .config input[name='first10sale'][status='false']").prop("checked", true);
                }
                $('.planstatus span').click(function(){
                    var on="on", shut="shut", current=$(this),currClass=current.hasClass("on")?on:shut, url="/delistplan/" + (currClass=="on"?"turnOffPlan":"turnOnPlan"),
                        messSuccess=(currClass=="on"?"关闭计划成功！":"打开计划成功！"),messFail=(currClass=="on"?"关闭计划失败":"打开计划失败"),text=(currClass=="on"?"已关闭":"已开启");
                    $.post(url,{planId:editplan.init.planId},function(data){
                        if(data == null || data.success == false){
                            TM.Alert.load(messFail);
                            return;
                        }
                        current.removeClass(currClass).addClass(currClass=="on"?"shut":"on");
                        current.html(text);
                        TM.Alert.load(messSuccess);
                    })
                })
                $('.planstatus span').tooltipster({
                    content:'点击改变计划的状态',
                    delay:0,
                    trigger:'hover'
                });
            })
        },
        initConfig:function(tarCSS){
            tarCSS.click(function(){
                var currStatus="hidebtn";
                var antiStatus="showbtn";
                if(tarCSS.hasClass('showbtn') || tarCSS.siblings().hasClass('showbtn')){
                    currStatus = "showbtn";
                    antiStatus = "hidebtn";
                }
                if(tarCSS.siblings().hasClass(currStatus)){
                   tarCSS = tarCSS.siblings();
                }
                tarCSS.removeClass(currStatus).addClass(antiStatus);
                if(currStatus == "hidebtn"){
                    tarCSS.parent().siblings().show(100);
                }else{
                    tarCSS.parent().siblings().hide(100);
                }
            })
        },
        initConfAction:function(){
            $('.configure .config .modifyconfig').click(function(){
                var params = {};
                params.planId = editplan.init.planId;
                params.isAutoAddNewItem =$('.configure .config input[name="addnewitem"]:checked').attr('status');
                params.isFilterGoodSalesItem = $('.configure .config input[name="first10sale"]:checked').attr('status');
                if(confirm("是否修改计划配置？")) {
                    $.post("/DelistPlan/modifyPlanConfig", params, function (data) {
                        if(data == null && data.success == false){
                            TM.Alert.load("修改计划配置失败！");
                            return;
                        }else{
                            confirm("修改计划成功");
                        }
                    })
                }
            })
        }
    }, editplan);

    editplan.singleItemEdit = $.extend({
        doInit:function(){
            editplan.singleItemEdit.initSearchBtn();
            editplan.init.initConfig($('.singleitemsetting .singletitle .singletxt'));
            editplan.init.initConfig($('.singleitemsetting .singletitle .singlebtn'));
        },
        initSearchBtn:function(){
            $('.singleitemsetting .itemsearchbtn').click(function(){
                var params = {};
                params.nowTime = new Date().getTime();
                params.title = $('.singleitemsetting .searchtext').val();
                params.planId = editplan.init.planId;
                $('.singleitemsetting tbody').empty();
                $('.singleitemsetting').find('.manualpaging').tmpage({
                    currPage: 1,
                    pageSize: 10,
                    pageCount: 1,
                    ajax:{
                        on:true,
                        param:params,
                        dataType:'json',
                        url:'/DelistPlan/queryDelistTimes',
                        callback:function(data){
                            if(data == null || data.isOk == false){
                                TM.Alert.load("获取数据失败！");
                            }
                            $('.singleitemsetting tbody').empty();
                            $.each(data.res, function(i, item){
                                item.price = "￥"+item.price;
                            })
                            editplan.util.insertHtml('.singleitemsetting tbody', data.res);
                        }
                    }
                })
            })

            $('.singleitemsetting .itemsearchbtn').trigger('click');

            $('.singleitemsetting .searchtext').keydown(function(event){
                if(event.keyCode == 13){
                    $('.singleitemsetting .itemsearchbtn').trigger('click');
                }
            })
        }
    }, editplan.singleItemEdit);

    editplan.todayDelist = $.extend({
        doInit:function(){
            editplan.init.initConfig($(".todaydelistplan .todaytitle .todaytxt"));
            editplan.init.initConfig($(".todaydelistplan .todaytitle .todaybtn"));
            editplan.todayDelist.showTodayDelistItems();
        },
        showTodayDelistItems:function(){
            var params = {};
            params.nowTime = new Date().getTime();
            params.planId = editplan.init.planId;
            $('.todaydelistplan').find('.todaypaging').tmpage({
                currPage:1,
                pageSize:10,
                pageCount:1,
                ajax:{
                    on:true,
                    param:params,
                    dataType:'json',
                    url:'/DelistPlan/getTodayDelist',
                    callback:function(data){
                        $('.todaydelistplan tbody').empty();
                        if(data == null || data.isOk == false){
                            TM.Alert.load("获取数据失败");
                        }
                        $.each(data.res, function(i, item){
                            item.price = "￥" + item.price;
                            item.listTime = editplan.util.parseTime(item.listTime);
                            item.realDelistTime = editplan.util.parseTime(item.realDelistTime);
                        })

                        var rows = $("#todaydelisttmpl").tmpl(data.res);
                        $('.todaydelistplan tbody').append(rows);
                    }
                }
            })
        }
    }, editplan.todayDelist)
    editplan.delistLog = $.extend({
        doInit:function(){
            editplan.init.initConfig($(".delistplanlog .logtitle .logtxt"));
            editplan.init.initConfig($(".delistplanlog .logtitle .logbtn"));
            editplan.delistLog.showDelistLog();
        },
        showDelistLog:function(){
            $('.delistplanlog .itemsearchbtn').click(function(){
                var params = {};
                params.nowtime = new Date().getTime();
                params.planId = editplan.init.planId;
                params.title = $(".delistplanlog .searchtext").val();
                $('.delistplanlog').find('.logpaging').tmpage({
                    currPage:1,
                    pageSize:10,
                    pageCount:1,
                    ajax:{
                        on:true,
                        param:params,
                        dataType:'json',
                        url:'/DelistPlan/getDelistLog',
                        callback:function(data){
                            $('.delistplanlog tbody').empty();
                            if(data == null || data.isOk == false){
                                TM.Alert.load('获取数据失败')
                            }
                            $.each(data.res, function(i, item){
                                item.price = "￥"+item.price;
                                item.listTime = editplan.util.parseTime(item.listTime);
                                item.ok = false;
                                if (item.status == 0) {
                                    item.ok = true;
                                    item.status = "上架成功";
                                    item.opMsg = "-";
                                } else if (itemJson.status == 8) {
                                    item.status = "宝贝属性有错";
                                } else {
                                    item.status = "上架失败"
                                }
                            });

                        }
                    }
                })
            })
            $('.delistplanlog .searchtext').keydown(function(event){
                if(event.keyCode == 13){
                    $('.delistplanlog .itemsearchbtn').trigger('click');
                }
            })
            $('.delistplanlog .itemsearchbtn').trigger('click');
        }
    }, editplan.delistLog)
    editplan.util = $.extend({
        editDate:function(arr){
            $.each(arr, function(i, item){
                var listTime = item.listTime;
                item.parseListTime = editplan.util.parseTime(listTime);
                item.realDelistTime = editplan.util.parseTime(item.realDelistTime);
            })
        },
        parseTime:function(time){
            var array = [
                "周日", "周一", "周二", "周三", "周四", "周五", "周六"
            ];
            var theDate = new Date(time);
            var day = array[theDate.getDay()];
            var hour = theDate.getHours();
            var minutes = theDate.getMinutes();
            var second = theDate.getSeconds();
            if (hour < 10) {
                hour = "0" + hour;
            }
            if (minutes < 10) {
                minutes = "0" + minutes;
            }
            if (second < 10) {
                second = "0" + second ;
            }
            return day + " "+hour +":" + minutes + ":" + second;
        },
        insertHtml:function(container, arr){
            $.each(arr, function(i, item){
                var trObject = $("<tr></tr>");
                var listTime = item.listTime;
                var listTimeObject = editplan.util.parseTime(listTime);
                var realDelistTime = editplan.util.parseTime(item.realDelistTime);
                var imgTd = $("<td><a target='_blank'><img /></a></td>");
                imgTd.find("img").attr("src", item.picPath);
                imgTd.find("a").attr("href", url);
                trObject.append(imgTd);
                trObject.append('<td>'+listTimeObject +'</td>');
                trObject.append('<td>' + realDelistTime + '</td>')
                var url = "http://item.taobao.com/item.htm?id=" + item.numIid;
                trObject.append('<td>' + item.price +'</td>');
                trObject.append('<td><a target="_blank" href="http://item.taobao.com/item.htm?id='+item.numIid+'">'+item.title + '</a></td>');
                var theDate = new Date(listTime);
                var hour = theDate.getHours();
                var minutes = theDate.getMinutes();
                var second = theDate.getSeconds();
                if (hour < 10) {
                    hour = "0" + hour;
                }
                if (minutes < 10) {
                    minutes = "0" + minutes;
                }
                if (second < 10) {
                    second = "0" + second ;
                }
                var html = '<td>' +
                    '<select class="week-select">' +
                    '   <option value="0">周日</option> ' +
                    '   <option value="1">周一</option> ' +
                    '   <option value="2">周二</option> ' +
                    '   <option value="3">周三</option> ' +
                    '   <option value="4">周四</option> ' +
                    '   <option value="5">周五</option> ' +
                    '   <option value="6">周六</option> ' +
                    '</select> ' +
                    '<input type="text" class="delist-time" style="font-size: 14px;width:60px;" /> ' +
                    '<span class="tmbtn blue-short-btn save-btn" style="">保存</span> ' +
                    '</td>';

                var modifyTd = $(html);
                modifyTd.find(".week-select").val(theDate.getDay());
                modifyTd.find(".delist-time").val(hour+":"+minutes+":"+second);
                modifyTd.find(".delist-time").timepicker({
                    timeOnlyTitle: "选择时间：",
                    currentText: "当前时间",
                    closeText: "关闭",
                    timeFormat: "HH:mm:ss",
                    timeText: "时间",
                    hourText: "小时",
                    minuteText: '分',
                    secondText: '秒',
                    showSecond: true,
                    timeOnly: true,
                    controlType: 'select'
                });
                modifyTd.find(".save-btn").attr("numIid", item.numIid);
                var saveBtn = modifyTd.find(".save-btn");
                modifyTd.find(".save-btn").click(function(){
                    if (confirm("确定修改该宝贝的上架时间？") == false)
                        return;
                    var selectObj = saveBtn.parent().find(".week-select");
                    var timeObj = saveBtn.parent().find(".delist-time");
                    var data = {};
                    data.numIid = saveBtn.attr("numIid");
                    var weekIndex = selectObj.val();
                    if (weekIndex === undefined || weekIndex == null || weekIndex == "") {
                        alert("请先选择要修改的时间");
                        return;
                    }
                    data.weekIndex = weekIndex;
                    var timeStr = timeObj.val();
                    if (timeStr === undefined || timeStr == null || timeStr == "") {
                        alert("请先选择要修改的时间");
                        return;
                    }
                    data.timeStr = timeStr;
                    data.planId = editplan.init.planId;
                    //data.numIid = 0;
                    $.ajax({
                        url : "/delistplan/modifyDelistTime",
                        data : data,
                        type : 'post',
                        error: function() {
                        },
                        success: function (data) {
                            if(data == null || data.isOk == false){
                                TM.Alert.load("修改宝贝上架时间失败");
                            }

                            $('.singleitemsetting .itemsearchbtn').trigger('click');
                        }
                    });
                })
                modifyTd.find('.delist-time').keydown(function(event){
                    if(event.keyCode == 13){
                        modifyTd.find('save-btn').trigger('click');
                    }
                })
                trObject.append(modifyTd);
                $(container).append(trObject);
            })
        }
    }, editplan.util);
})(jQuery, window));