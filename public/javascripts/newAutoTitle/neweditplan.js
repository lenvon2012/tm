/**
 * Created by uttp on 7/28/14.
 */
TM = TM || {};

((function($, window){
    TM.newEditPlan = TM.newEditPlan || {};
    var newEditPlan = TM.newEditPlan;

    newEditPlan.init= $.extend({
        doInit:function(planId, container){
            newEditPlan.planId = planId;
            newEditPlan.container = container;
            newEditPlan.init.initHead();
            newEditPlan.initStatus(container.find('.planstatus'));
            newEditPlan.manualConfig.doInit(container.find('.manualconfig'));
        },
        initHead:function(){
            $('.nav_bar').find('.header-nav').removeClass('current');
            $('.nav_bar').find('.auto-delist').addClass("current");
        }
    }, newEditPlan.init);
    //plan的标题和状态进
    newEditPlan.initStatus = function(container){
        $.post("/delistplan/queryDelistPlan",{planId:newEditPlan.planId},function(data){
            if(data == null || data.success == false){
                TM.Alert.load("获取计划信息失败！");
                return;
            }
            var planTitle = container.find('.plantitle'),
                planStatus = container.find('.planstatus'),
                planConfig = container.find('.config');
            planTitle.append("<span>" + data.results.title + "</span>");
            var status = "<span style='position:relative;opacity:initial;'>";
            if(data.results.status == 1){
                status += "已开启</span>";
                planStatus.append(status);
                planStatus.find('span').addClass("on");
            }else if(data.results.status == 2){
                status +="已关闭</span>";
                planStatus.append(status);
                planStatus.find('span').addClass("shut");
            }
            if(data.results.delistConfig & 1){
                planConfig.find("input[name='addnewitem'][status='true']").prop("checked", true);
            }else{
                planConfig.find("input[name='addnewitem'][status='false']").prop("checked", true);
            }
            if(data.results.delistConfig & (1<<3)){
                planConfig.find("input[name='first10sale'][status='true']").prop("checked", true);
            }else{
                planConfig.find("input[name='first10sale'][status='false']").prop("checked", true);
            }
            if(data.results.templateType == 2){
                container.find('.configtitle, .config').hide();
            }
            planStatus.find('span').click(function(){
                var on="on", shut="shut", current=$(this),currClass=current.hasClass("on")?on:shut, url="/delistplan/" + (currClass=="on"?"turnOffPlan":"turnOnPlan"),
                    messSuccess=(currClass=="on"?"关闭计划成功！":"打开计划成功！"),messFail=(currClass=="on"?"关闭计划失败":"打开计划失败"),text=(currClass=="on"?"已关闭":"已开启");
                $.post(url,{planId:newEditPlan.planId},function(data){
                    if(data == null || data.success == false){
                        TM.Alert.load(messFail);
                        return;
                    }
                    current.removeClass(currClass).addClass(currClass=="on"?"shut":"on");
                    current.html(text);
                    TM.Alert.load(messSuccess);
                })
            })
            planConfig.find('.modifyconfig').click(function(){
                var params = {};
                params.planId = newEditPlan.planId;
                params.isAutoAddNewItem =planConfig.find('input[name="addnewitem"]:checked').attr('status');
                params.isFilterGoodSalesItem = planConfig.find('input[name="first10sale"]:checked').attr('status');
                if(confirm("是否修改计划配置？")) {
                    $.post("/DelistPlan/modifyPlanConfig", params, function (data) {
                        if(data == null || data.success == false){
                            TM.Alert.load("修改计划配置失败！");
                            return;
                        }else{
                            confirm("修改计划成功");
                        }
                    })
                }
            })
        })
    }
    //宝贝上下架时间设置
    newEditPlan.manualConfig = $.extend({
        doInit:function(container){
            newEditPlan.manualConfig.initTab(container.find('.base-tab-out'))
            newEditPlan.manualConfig.initSingelItemEdit(container.find('.configdelisttime'));
            newEditPlan.manualConfig.initTodayDelist(container.find('.todaydelistplan'));
            newEditPlan.manualConfig.initDelistLog(container.find('.delistlog'));
        },
        initTab:function(container){
            var tarDiv = container.find('.base-tab-div .select-tab').attr('tarDiv');
            container.find("." +tarDiv).show();
            container.find('.base-tab-div .base-tab-title').click(function(){
                var tarDiv=$(this).attr('tarDiv');
                container.find('.base-tab-title').removeClass('select-tab');
                $(this).addClass('select-tab');
                container.find('.target-div').hide();
                container.find("." + tarDiv).show();
            })
        },
        initSingelItemEdit:function(container){
            container.find('.singleitemsetting .itemsearchbtn').click(function(){
                var params = {};
                params.nowTime = new Date().getTime();
                params.title = container.find('.singleitemsetting .searchtext').val();
                params.planId = newEditPlan.planId;
                container.find('.singleitemsetting tbody').empty();
                container.find('.singleitemsetting').find('.manualpaging').tmpage({
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
                            container.find('.singleitemsetting tbody').empty();
                            $.each(data.res, function(i, item){
                                item.price = "￥"+item.price;
                            })
                            newEditPlan.util.insertHtml('.configdelisttime .singleitemsetting tbody', data.res);
                        }
                    }
                })
            })

            container.find('.singleitemsetting .itemsearchbtn').trigger('click');

            container.find('.singleitemsetting .searchtext').keydown(function(event){
                if(event.keyCode == 13){
                    container.find('.singleitemsetting .itemsearchbtn').trigger('click');
                }
            })
        },
        initTodayDelist:function(container){
            var params = {};
            params.nowTime = new Date().getTime();
            params.planId = newEditPlan.planId;
            container.find('.todaypaging').tmpage({
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
                            item.listTime = newEditPlan.util.parseTime(item.listTime);
                            item.realDelistTime = newEditPlan.util.parseTime(item.realDelistTime);
                        })
                        var rows = $("#todaydelisttmpl").tmpl(data.res);
                        container.find('tbody').append(rows);
                    }
                }
            })
        },
        initDelistLog:function(container){
            container.find('.itemsearchbtn').click(function(){
                var params = {};
                params.nowtime = new Date().getTime();
                params.planId = newEditPlan.planId;
                params.title = container.find(".searchtext").val();
                container.find('.logpaging').tmpage({
                    currPage:1,
                    pageSize:10,
                    pageCount:1,
                    ajax:{
                        on:true,
                        param:params,
                        dataType:'json',
                        url:'/DelistPlan/getDelistLog',
                        callback:function(data){
                            container.find('tbody').empty();
                            if(data == null || data.isOk == false){
                                TM.Alert.load('获取数据失败')
                            }
//                            $.each(data.res, function(i, item){
//                                item.price = "￥"+item.price;
//                                item.listTime = newEditPlan.util.parseTime(item.listTime);
//                                item.ok = false;
//                                if (item.status == 0) {
//                                    item.ok = true;
//                                    item.status = "上架成功";
//                                    item.opMsg = "-";
//                                } else if (itemJson.status == 8) {
//                                    item.status = "宝贝属性有错";
//                                } else {
//                                    item.status = "上架失败"
//                                }
//                            });
                            for(var i in data.res){
                                data.res[i].price = "￥" + data.res[i].price;
                                data.res[i].listTime = newEditPlan.util.parseTime(data.res[i].listTime);
                                data.res[i].ok = false;
                                if(data.res[i].status == 0){
                                    data.res[i].ok = true;
                                    data.res[i].status = "上架成功";
                                    data.res[i].opMsg = "-";
                                }else if(data.res[i].status == 8){
                                    data.res[i].status = "宝贝属性有误";
                                }else{
                                    data.res[i].status = "上架失败";
                                }
                            }
                            var rows = $('#delistplantmpl').tmpl(data.res);
                            container.find('tbody').append(rows);
                        }
                    }
                })
            })
            container.find('.searchtext').keydown(function(event){
                if(event.keyCode == 13){
                    container.find('.itemsearchbtn').trigger('click');
                }
            })
            container.find('.itemsearchbtn').trigger('click');
        }
    }, newEditPlan.manualConfig);
    newEditPlan.util = $.extend({
        editDate:function(arr){
            $.each(arr, function(i, item){
                var listTime = item.listTime;
                item.parseListTime = newEditPlan.util.parseTime(listTime);
                item.realDelistTime = newEditPlan.util.parseTime(item.realDelistTime);
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
                var listTimeObject = newEditPlan.util.parseTime(listTime);
                var realDelistTime = newEditPlan.util.parseTime(item.realDelistTime);
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
                    data.planId = newEditPlan.planId;
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
    }, newEditPlan.util);
})(jQuery, window));
