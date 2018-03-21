

var TM = TM ||{};

TM.util = TM.util || {};

TM.widget = TM.widget || {};

TM.price = TM.price || {};

((function ($, window) {
    TM.price.youhua = 19;
    TM.price.zhizun = 39;
    TM.price.zhizunMonth = 4.9;

})(jQuery, window));

((function ($, window) {
    $(function () {
        $.fn.extend({
            tmpage:function (param) {
                init(param, $(this));
                return $(this);
            }
        });

        function init(param, obj) {
            if (param && param instanceof Object) {
                var options;
                var currPage;
                var pageCount;
                var pageSize;
                var tempPage;
                var defaults = new Object({
                    currPage:1,
                    pageCount:10,
                    pageSize:5,
                    ajax:{
                        on:false,
                        pageCountId:'pageCount',
                        param:{
                            pn:1,
                            ps:8
                        },
                        ajaxStart:function () {
                            return false;
                        }},
                    info:{
                        next:'下一页',
                        prev:'上一页',
                        next_on:true,
                        prev_on:true,
                        msg_on:true,
                        link:'javascript:void(0);',
                        msg:'<span>&nbsp;&nbsp;共&nbsp;{sumPage}&nbsp;页&nbsp;到&nbsp;{currText}&nbsp;页&nbsp;<input  class="gopage-submit hand" title="跳转页面" type="button" value="确定"/></span>'
                    }
                });

                function getCurrPage() {
                    if (typeof options.currPage != 'undefined') {
                        return options.currPage;
                    } else {
                        return defaults.currPage;
                    }
                }

                function getPageCount() {
                    if (typeof options.pageCount != 'undefined') {
                        return options.pageCount;
                    } else {
                        return defaults.pageCount;
                    }
                }

                function getPageSize() {
                    if (typeof options.pageSize != 'undefined') {
                        return options.pageSize;
                    } else {
                        return defaults.pageSize;
                    }
                }

                function getPrev() {
                    if (options.info && options.info.prev_on == false) {
                        return "";
                    }
                    if (options.info && options.info.prev) {
                        return options.info.prev;
                    } else {
                        return defaults.info.prev;
                    }
                }

                function getNext() {
                    if (options.info && options.info.next_on == false) {
                        return "";
                    }
                    if (options.info && options.info.next) {
                        return options.info.next;
                    } else {
                        return defaults.info.next;
                    }
                }

                function getLink() {
                    if (options.info && options.info.link) {
                        return options.info.link;
                    } else {
                        return defaults.info.link;
                    }
                }

                function getAjax() {
                    if (options.ajax && options.ajax.on) {
                        return options.ajax;
                    } else {
                        return defaults.ajax;
                    }
                }

                function getParam() {
                    if (options.ajax.param) {
                        options.ajax.param.pn = currPage;
                        options.ajax.param.ps = pageSize;
                        return options.ajax.param;
                    } else {
                        defaults.ajax.param.pn = currPage;
                        defaults.ajax.param.ps = pageSize;
                        return defaults.ajax.param;
                    }
                }

                function getPageCountId() {
                    if (options.ajax && options.ajax.pageCountId) {
                        return options.ajax.pageCountId;
                    } else {
                        return defaults.ajax.pageCountId;
                    }
                }

                function getAjaxStart() {
                    if (options.ajax && options.ajax.ajaxStart) {
                        options.ajax.ajaxStart();
                    } else {
                        defaults.ajax.ajaxStart;
                    }
                }

                function getMsg() {
                    var input = "<input type='text' class='jumpTo' value='" + currPage + "' >";
                    var str;
                    if (options.info && options.info.msg_on == false) {
                        return false;
                    }
                    str = (options.info && options.info.msg) ? options.info.msg : defaults.info.msg;
                    str = str.replace("{currText}", input);
                    str = str.replace("{currPage}", currPage);
                    str = str.replace("{sumPage}", pageCount);

                    return str;
                }
            }

            function getText() {
                var msg = getMsg();
                if (msg) {
                    msg = $(msg);
                } else {
                    return "";
                }
                return msg.html();
            }

            function isCode(val) {
                if (val < 1) {
                    TM.Alert.load('输入值不能小于1');

                    //alert("输入值不能小于1");
                    return false;
                }
                var patrn = /^[0-9]{1,8}$/;
                if (!patrn.exec(val)) {
                    TM.Alert.load('请输入正确的数字');

                    //alert("请输入正确的数字");
                    return false;
                }
                if (val > pageCount) {
                    TM.Alert.load('输入值不能大于总页数');

                    //alert("输入值不能大于总页数");
                    return false;
                }
                return true;
            }

            function updateView() {
                currPage = parseInt(currPage);
                pageCount = parseInt(pageCount);
                var link = getLink();
                var lastPage;
                var firstPage = lastPage = 1;
                if (currPage - tempPage > 0) {
                    firstPage = currPage - tempPage;
                } else {
                    firstPage = 1;
                }
                if (firstPage + pageSize > pageCount) {
                    lastPage = pageCount + 1;
                    firstPage = lastPage - pageSize;
                } else {
                    lastPage = firstPage + pageSize;
                }
                var content = "";

                if (currPage == 1) {
                    content += "<span class=\"page-prev page-prev-disabled\" title=\"" + getPrev() + "\"></span>&nbsp;";
                } else {
                    content += "<a class='page-prev page-prev-abled' href='" + link + "' title='" + (currPage - 1) + "'>"+ getPrev() +"</a>&nbsp;";
                }
                if (firstPage <= 0) {
                    firstPage = 1;
                }
                for (firstPage; firstPage < lastPage; firstPage++) {
                    if (firstPage == currPage) {
                        content += "<span class=\"current\" title=\"" + firstPage + "\">" + firstPage + "</span>&nbsp;";
                    } else {
                        content += "<a href='" + link + "' title='" + firstPage + "'>" + firstPage + "</a>&nbsp;";
                    }
                }
                if (currPage == pageCount) {
                    content += "<span class=\"page-next page-next-disabled\" title=\"" + getNext() + "\"></span>&nbsp;";
                } else {
                    content += "<a class='page-next page-next-abled' href='" + link + "' title='" + (currPage + 1) + "'>"+getNext()+" </a>&nbsp;";
                }

                content += getText();
                obj.html(content);
                obj.children(":text").keypress(function (event) {
                    var keycode = event.which;
                    if (keycode == 13) {
                        var page = $(this).val();
                        if (isCode(page)) {
//                            obj.children("a").unbind("click");
//                            obj.children("a").each(function() {
//                                $(this).click(function() {
//                                    return false;
//                                })
//                            });
                            createView(page);
                        }
                    }
                });

                obj.children(":button").click(function () {
                    var page = obj.children(":text").val();
                    if (isCode(page)) {
                        createView(page);
                    }
                });

                obj.children("a").click(function (i) {
                    var page = this.title;
                    createView(page);
                });

            }

            function createView(page) {
                currPage = page;
                var ajax = getAjax();
                if (ajax.on) {
                    getAjaxStart();
                    var varUrl = ajax.url;
                    var param = getParam();
//                    console.info(param);
                    $.ajax({
                        url:varUrl ,
                        type:'post',
                        data:param,
                        contentType:"application/x-www-form-urlencoded;utf-8",
                        //async:false,
                        dataType:'json',
                        error:function (jqXHR, textStatus) {
                            updateView();
                            //alert(textStatus);
                        },
                        success:function (data) {
                            if(!data){
                                return;
                            }

                            if (data.isOk) {
                                    loadPageCount({
                                        dataType:ajax.dataType,
                                        callback:ajax.callback,
                                        data:data
                                    });
                                    updateView();
                                    return true;
//                                } else {
////                                alert(data.message);
//                                    return false;
//                                }
                            }
                            else {
                                $('#loadingImg').parents('tr').remove();

                                if(data.message){
                                    TM.Alert.load(data.message);
                                }
                                //alert(data.message);
                            }

                        }})
                } else {
                    updateView();
                }
            }


            function checkParam() {
                if (currPage < 1) {
                    TM.Alert.load('配置参数错误\n错误代码:-1');

                    //alert("配置参数错误\n错误代码:-1");
                    return false;
                }
                if (currPage > pageCount) {
                    TM.Alert.load('配置参数错误\n错误代码:-2');

                    //alert("配置参数错误\n错误代码:-2");
                    return false;
                }
                if (pageSize < 2) {
                    TM.Alert.load('配置参数错误\n错误代码:-3');

                    //alert("配置参数错误\n错误代码:-3");
                    return false;
                }
                return true;
            }

            function loadPageCount(options) {
                if (options.dataType) {
                    var formData;
                    var data = options.data;
                    var resultPageCount = false;
                    var isB = true;
                    var pageCountId = getPageCountId();
                    var callback;
                    switch (options.dataType) {
                        case"json":
//                        data =$.parseJSON(data);
//                         resultPageCount = eval("data." + pageCountId);
                            formData = options.data;
                            resultPageCount = formData.pnCount;
                            break;
                        case"xml":
                            resultPageCount = $(data).find(pageCountId).text();
                            break;
                        default:
                            isB = false;
                            options.callback && options.callback(data);
                            resultPageCount = $("#" + pageCountId).val();
                            break;
                    }
                    if (resultPageCount) {
                        pageCount = resultPageCount;
                    }
                    if (isB) {
                        options.callback && options.callback(data);
                    }
                }
            }

            options = param;
            currPage = getCurrPage();
            pageCount = getPageCount();
            pageSize = getPageSize();
            tempPage = parseInt(pageSize / 2);
            if (checkParam() && createView(currPage)) {
                updateView();
            }
        }
    });

})(jQuery, window));

((function ($, window) {
    TM.permission =TM.permission || {};

    TM.permission.isVip = function(){
        return TM.ver >= 20;
    }

    TM.isVip = TM.permission.isVip;

})(jQuery, window));

((function ($, window) {

    TM.Alert=TM.Alert|| {};

    TM.Alert.showOrder = function(){
        var html = [];
        html.push('<p>亲，您需要订购<b class="red">优化版本或至尊版</b>,才能使用该功能哟</p>');
        html.push('<p>');
        html.push('<a class="bluebutton" style="color:white;margin-right:30px;" href="/home/buyVersion">立即升级</a>');
        //html.push('<a class="bluebutton" style="color:white;" href="/home/buyVersion">查看详情</a>');
        html.push('</p>');
        html.push('<p >仅需 <b class="red">'+TM.price.youhua+'</b> 元 即可享受<b class="red">一年</b>的超值服务</p>')
        TM.Alert.load(html.join(''),500,330);
    }

    TM.Alert.showVIPNeeded = function(){
        var html = [];
        html.push('<p>亲，您需要升级到<b class="red">至尊版</b>,才能使用该功能哟</p>');
        html.push('<p>');
        html.push('<a class="bluebutton" style="color:white;margin-right:30px; " href="/home/buyzhizun">立即升级</a>');
        //html.push('<a class="bluebutton" style="color:white;" href="/home/buyzhizun">查看详情</a>');
        html.push('</p>');
        html.push('<p >仅需 <b class="red">'+TM.price.zhizunMonth+'</b> 元每月 即可享受<b class="red">至尊</b>超值服务</p>');
        html.push('<p>自动上下架、自动橱窗、自动评价最新上线,并将陆续推出更多全新功能</p>');
        html.push('<p><a href="http://fuwu.taobao.com/item/subsc.htm?items=ts-1820059-3:1"><img src="http://img03.taobaocdn.com/imgextra/i3/62192401/T2wQ0rXehOXXXXXXXX_!!62192401.gif"></a></p>');

        TM.Alert.load(html.join(''),700,600);
    }

    TM.Alert.getDom = function(){
        TM.Alert.alertDom = TM.Alert.alertDom || $('.tmAlert');
        if(TM.Alert.alertDom.length == 0){
            TM.Alert.alertDom = $('<div class="tmAlert"></div>')
            TM.Alert.alertDom.appendTo($('body'));
        }
        return TM.Alert.alertDom;
    }

    TM.Alert.load=function(html, width, height, callback, isCancel, title){
        var alertDiv = TM.Alert.getDom();
        var buttons = {};
        buttons['确定']=function(){
            callback && callback();
            $(this).dialog('close');
        }
        if(isCancel){
            buttons['取消']=function(){
                $(this).dialog('close');
            }
        }

        alertDiv.html(html);
        alertDiv.dialog({
            modal: true,
            bgiframe: true,
            height: height || 300,
            width: width || 400,
            title : title || '提示',
            autoOpen: false,
            resizable: false,
            zIndex: 6003,
            buttons:buttons
        });
        alertDiv.dialog('open');

    };

    TM.Alert.chooseWangwang = function(html, callback){
        var diag = $('.chooseWW');
        if(diag.length == 0){
            diag = $("<div class='chooseWW'></div>");
            diag.appendTo($('body'));
        }

        diag.dialog({
            modal: true,
            bgiframe: true,
            height:200,
            width:300,
//            title:'添加监控项',
            autoOpen: false,
            resizable: false,
            buttons:{'确定':function() {
                var text = diag.find('input').val();
                if(!text || text.length == 0){
                    TM.Alert.load('请输入旺旺');
                    return;
                }
                if(callback(text)){
                    $(this).dialog('close');
                }else{
                    // nothing to do....
                }
            },'取消':function(){
                $(this).dialog('close');
            }}
        });
//        diag.html('<p>请输入您要监控的店铺掌柜名:</p><p><input type="text" value=""></p>');
        if(html){
            diag.html(html);
        }
        diag.dialog('open');
    }

})(jQuery, window));


((function ($, window) {

    TM.Comment = TM.Comment || {};

    TM.Comment.buildAutoStatus  = function(switchStatus, isOn){
        switchStatus.find('input[name="auto_valuation"]').tzCheckbox(
            {
                labels:['已开启','已关闭'],
                doChange:function(isCurrentOn){
                    if(!TM.isVip()){
                        TM.Alert.showVIPNeeded();
                        return;
                    }

                    // TODO， we curerntly have nothing to do...
                    if(isCurrentOn == false) {
                        $.get('/autocomments/setOn',function(data){
                            if(!data)  TM.Alert.load("自动评价开启失败，请联系淘标题客服人员~");
                        });
                    }
                    else {
                        $.get('/autocomments/setOff',function(data){
                            if(!data)  TM.Alert.load("自动评价关闭失败，请联系淘标题客服人员~");
                        });
                    }
                    return true;
                },
                isOn : isOn
            });
    }

})(jQuery, window));


((function ($, window) {

    TM.AudotDelist = TM.AudotDelist || {};

    TM.AudotDelist.buildAutoStatus = function(switchStatus, isOn, openCallback, closeCallback, distriTypeDiv){
        switchStatus.find('input[name="auto_valuation"]').tzCheckbox(
            {
                labels:['已开启','已关闭'],
                doChange:function(isCurrentOn){
                    if(!TM.isVip()){
                        TM.Alert.showVIPNeeded();
                        return;
                    }

                    if (isCurrentOn == false) {//要开启
                        var params = TM.AudotDelist.getParams(distriTypeDiv);
                        if (params === undefined || params == null)
                            return;
                        $.ajax({
                            //async : false,
                            url : '/autodelist/turnOn',
                            data : params,
                            type : 'post',
                            error: function() {
                            },
                            success: function (data) {
                                if (!data || data.res == false) {
                                    TM.Alert.load('亲,自动上下架开启失败！');
                                } else {
                                    TM.Alert.load('亲,自动上下架开启成功！');
                                }
                                openCallback && openCallback();
                            }
                        });
                    } else if (isCurrentOn == true) {//要关闭
                        $.get('/autodelist/turnOff',function(data){
                            if (!data || data.res == false) {
                                TM.Alert.load('亲,自动上下架关闭失败！');
                            } else {
                                TM.Alert.load('亲,自动上下架关闭成功！');
                            }

                            closeCallback && closeCallback();
                        });
                    }
                    return true;
                },
                isOn : isOn
            });

    }
})(jQuery, window));

((function ($, window) {

    TM.ShowWindow = TM.ShowWindow || {};

    TM.ShowWindow.buildAutoStatus = function(switchLine, isOn){
        switchLine.find('input[name="auto_valuation"]').tzCheckbox({
                isOn : isOn,
                labels:['已开启','已关闭'],
                doChange:function(isCurrentOn){
                    if(isCurrentOn == false) {
                        $.post('/Windows/turn',{"isOn":true},function(data){
                            if(!data)  TM.Alert.load("自动橱窗开启失败，请联系淘标题客服人员~");
                        });
                    }
                    else {
                        $.post('/Windows/turn',{"isOn":false},function(data){
                            if(!data)  TM.Alert.load("自动橱窗关闭失败，请联系淘标题客服人员~");
                        });
                    }
                    return true;
                }

        });
    }
})(jQuery, window));

((function ($, window) {
    TM.widget.showWillOpen = function(text){
        TM.Alert.load('<p style="font-size: 16px;">亲，<b style="color:red">['+text+']</b>将在 <b>2012-12-03 23:59:59</b> 准时开放，敬请期待！</p><p><img src="http://img03.taobaocdn.com/imgextra/i3/62192401/T2wQ0rXehOXXXXXXXX_!!62192401.gif"></p>',600,400);
    }

    TM.widget.buildKitGuidePay = function(tag){

        var html = [];
        html.push('<div style="margin: 10px 10px 10px 10px;padding: 10px 0 10px 0;padding-left:10px;text-align:center;">');
//        html.push('');
        switch(tag){
            case 'show':
                html.push('<p>亲，自动橱窗仅开放给<b class="red">至尊版</b>用户。全自动实时监控即将下架的宝贝,榨干淘宝的每一滴流量');
                break;
            case 'comment':
                html.push('<p>亲，自动评价仅开放给<b class="red">至尊版</b>用户。全自动秒级智能评价，提升客户回头满意率的利器');
                break;
            case 'delist':
                html.push('<p>亲，自动上下架仅开放给<b class="red">至尊版</b>用户。智能上下架,为您量身定制最佳的上架计划,引爆您的店铺流量');
                break;
        }
        html.push('，您还在犹豫什么？</p>');
        html.push('<p>现在升级，即能享受<b class="red">至尊版'+TM.price.zhizun+'元包年特价</b></p>');
        html.push('<table style="margin: 10px auto;width:350px;padding-left:120px;">');
        html.push('<tr><td><a class="orange-button" href="/home/buyzhizun" >马上升级</a></td>');
        //html.push('<td><a href="/home/buyzhizun" class="white-button">查看版本详情</a></td></tr></table>');
        html.push('<td></td></tr></table>');
        html.push('</div>')
        return $(html.join(''));
    }

    TM.widget.guideWwithToPay = function(switchLine,text){
        switchLine.find('input[name="auto_valuation"]').tzCheckbox({
            isOn : false,
            labels:['已开启','尚未开启'],
            doChange:function(isCurrentOn){
//                TM.Alert.load('优化版和尊享版才能开启哦！立即抢购尊享版');
//                TM.widget.showWillOpen(text);
                TM.Alert.showVIPNeeded();
                return false;
            }
        });
    }

    TM.widget.createShowSwitch = function(container){
        container.empty();
        var windowSwith = TM.Switch.createSwitch.createSwitchForm("",true);
        if(TM.ver < 20){
            TM.widget.guideWwithToPay(windowSwith,'自动橱窗');
        }else{
            TM.ShowWindow.buildAutoStatus(windowSwith, TM.isAutoShow);
        }
        windowSwith.appendTo(container);
    }

    TM.widget.createDelistSwitch = function(container){
        container.empty();
        var windowSwith = TM.Switch.createSwitch.createSwitchForm("",true);
        if(TM.ver < 20){
            TM.widget.guideWwithToPay(windowSwith,'自动上下架');
        }else{
            TM.AudotDelist.buildAutoStatus(windowSwith, TM.isAutoDelist);
        }
        windowSwith.appendTo(container);
    }


    TM.widget.createCommentSwitch = function(container){
        container.empty();
        var windowSwith = TM.Switch.createSwitch.createSwitchForm("",true);
        if(TM.ver < 20){
            TM.widget.guideWwithToPay(windowSwith,'自动评价');
        }else{
            $.get('/autocomments/isOn',function(data){
                TM.Comment.buildAutoStatus(windowSwith, TM.isAutoComment);
            });
        }
        windowSwith.appendTo(container);
    }

})(jQuery, window));

String.prototype.cnLength = function () {
    return this.replace(/[^u4E00-u9FA5]/g, 'nn').length;
}

String.prototype.trim = function () {
    return this.replace(/^\s*|\s*$/g, '');
}
Number.prototype.toPercent = function (n) {
    n = n || 0;
    return ( Math.round(this * Math.pow(10, n + 2)) / Math.pow(10, n) ).toFixed(n) + '%';
}
Date.prototype.format = function(format) //author: meizz
{
    var o = {
        "M+" : this.getMonth()+1, //month
        "d+" : this.getDate(),    //day
        "h+" : this.getHours(),   //hour
        "m+" : this.getMinutes(), //minute
        "s+" : this.getSeconds(), //second
        "q+" : Math.floor((this.getMonth()+3)/3),  //quarter
        "S" : this.getMilliseconds() //millisecond
    }
    if(/(y+)/.test(format)) format=format.replace(RegExp.$1,
        (this.getFullYear()+"").substr(4 - RegExp.$1.length));
    for(var k in o)if(new RegExp("("+ k +")").test(format))
        format = format.replace(RegExp.$1,
            RegExp.$1.length==1 ? o[k] :
                ("00"+ o[k]).substr((""+ o[k]).length));
    return format;
}

Date.prototype.formatYMS = function(){
    return this.format('yyyy-MM-dd');

}
Date.prototype.formatMS = function(){
    return this.format('MM月dd');
}


//***********************************************
//
//$.getScript('/js/jquery.ui.js',function(){
//	$.getScript("/js/prize.js",function(){
//	$("#dialog").dialog({
//		closeText: "关闭",
//  		autoOpen: false,
//       width: 630,
//       resizable: false,
//       title:"温情提示栏",
//       modal: true,
//       buttons: [
//           {
//               text: "关闭",
//               click: function() {
//                   $(this).dialog( "close" );
//               }
//           }
//       ]
//});
//	 // Link to open the dialog
//	// alert(TM.ver);
//	$( "#dialog-link" ).click(function( event ) {
//   	$( "#dialog" ).dialog( "open" );
//   	event.preventDefault();
//	});
//});
//});
//***********************************************	

