var Ali = Ali || {};


((function ($, window) {

    Ali.Alert=Ali.Alert|| {};

    Ali.Alert.showOrder = function(){
        var html = [];
//        html.push('<p>亲，您需要订购<b class="red">优化版本或至尊版</b>,才能使用该功能哟</p>');
        html.push('<p style="margin: 20px 0px 20px 0px;font-size: 20px;">亲，立即订购<b class="red" style="font-size: 24px;">至尊版</b>,就能使用该功能哟!</p>');
        /*html.push('<p style="margin: 20px 0px 20px 0px;">');
         html.push('<a target="_blank" class="bluebutton" style="color:white;margin-right:30px;" href="http://to.taobao.com/1Abfzgy">立即升级</a>');
         //html.push('<a class="bluebutton" style="color:white;" href="/home/buyVersion">查看详情</a>');
         html.push('</p>');*/
        html.push('<a target="_blank" href="http://to.taobao.com/FV9tzgy" style="display: inline-block;*display: inline;zoom:1;padding: 3px 10px;"><span class="btn xufei-btn" style="margin-top: 0px;width: 66px;">8元一月</span></a>');
        html.push('<a target="_blank" href="http://to.taobao.com/nIbfzgy" style="display: inline-block;*display: inline;zoom:1;padding: 3px 10px;"><span class="btn xufei-btn" style="margin-top: 0px;width: 66px;">24一季</span></a>');
        html.push('<a target="_blank" href="http://to.taobao.com/uRbfzgy" style="display: inline-block;*display: inline;zoom:1;padding: 3px 10px;"><span class="btn xufei-btn" style="margin-top: 0px;width: 66px;">39半年</span></a>');
        html.push('<a target="_blank" href="http://to.taobao.com/1Abfzgy" style="display: inline-block;*display: inline;zoom:1;padding: 3px 10px;"><span class="btn xufei-btn" style="margin-top: 0px;width: 66px;">69一年</span></a>');
        html.push('<p  style="margin: 20px 0px 20px 0px;font-size: 20px;">仅需 <b class="red">'+69+'</b> 元 即可享受<b class="red">一年</b>的超值服务</p>')
        Ali.Alert.load(html.join(''),600,340);
    }

    Ali.Alert.showVIPNeeded = function(){
        var html = [];
        html.push('<p style="margin: 20px 0px 20px 0px;font-size: 20px;">亲，立即升级<b class="red" style="font-size	: 24px;">至尊版</b>,就能使用该功能哟!</p>');
        /* html.push('<p style="margin: 20px 0px 20px 0px;">');
         html.push('<a target="_blank" class="bluebutton" style="color:white;margin-right:30px; " href="http://to.taobao.com/1Abfzgy">立即升级</a>');
         //html.push('<a class="bluebutton" style="color:white;" href="/home/buyzhizun">查看详情</a>');
         html.push('</p>');*/
        html.push('<a target="_blank" href="http://to.taobao.com/FV9tzgy" style="display: inline-block;*display: inline;zoom:1;padding: 3px 10px;"><span class="btn xufei-btn" style="margin-top: 0px;width: 66px;">8元一月</span></a>');
        html.push('<a target="_blank" href="http://to.taobao.com/nIbfzgy" style="display: inline-block;*display: inline;zoom:1;padding: 3px 10px;"><span class="btn xufei-btn" style="margin-top: 0px;width: 66px;">24一季</span></a>');
        html.push('<a target="_blank" href="http://to.taobao.com/uRbfzgy" style="display: inline-block;*display: inline;zoom:1;padding: 3px 10px;"><span class="btn xufei-btn" style="margin-top: 0px;width: 66px;">39半年</span></a>');
        html.push('<a target="_blank" href="http://to.taobao.com/1Abfzgy" style="display: inline-block;*display: inline;zoom:1;padding: 3px 10px;"><span class="btn xufei-btn" style="margin-top: 0px;width: 66px;">69一年</span></a>');
        html.push('<p style="margin: 20px 0px 20px 0px;font-size: 20px;">仅需 <b class="red">'+69+'</b> 元包年 即可享受<b class="red">至尊</b>超值服务</p>');
        html.push('<p style="margin: 20px 0px 20px 0px;font-size: 20px;">包含自动上下架、自动橱窗、自动评价等12大功能</p>');
        html.push('<p style="margin: 20px 0px 20px 0px;font-size: 20px;">' +
//            '<a href="http://to.taobao.com/TguGTiy"><img style="width: 190px;height: 190px;" src="http://img03.taobaocdn.com/imgextra/i3/62192401/T2wQ0rXehOXXXXXXXX_!!62192401.gif">' +
            '<div style="text-align: center;"><a target="_blank" href="/home/freeup" style="color:red;font-size:40px;font-weight: bold;">立即升级</a></div>' +
            '</a></p>');

        Ali.Alert.load(html.join(''),700,550);
    }

    Ali.Alert.getDom = function(){
        Ali.Alert.alertDom = Ali.Alert.alertDom || $('.tmAlert');
        if(Ali.Alert.alertDom.length == 0){
            Ali.Alert.alertDom = $('<div class="tmAlert"></div>')
            Ali.Alert.alertDom.appendTo($('body'));
        }
        return Ali.Alert.alertDom;
    }
    Ali.Alert.getDetail = function(){
        Ali.Alert.alertDetail = Ali.Alert.alertDetail || $('.tmAlertDetail');
        if(Ali.Alert.alertDetail.length == 0){
            Ali.Alert.alertDetail  = $('<div class="tmAlertDetail"></div>')
            Ali.Alert.alertDetail.appendTo($('body'));
        }
        return Ali.Alert.alertDetail;
    }
    Ali.Alert.loadDetail = function(html, width, height, callback,title, hideafter, before, after){

        $("body").mask();


        var alertDiv = Ali.Alert.getDetail();
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
            buttons:{
                '确定':function(){
                    var flag = callback && callback();
                    if(flag != true && flag != false){
                        flag = true;
                    }
                    if(flag == true){
                        $(this).dialog('close');
                        $("body").unmask();
                    }

                },
                '取消':function(){
                    $(this).dialog('close');
                    $("body").unmask();
                }
            }
            ,beforeClose:function(event, ui){
                $("body").unmask();
                after && after();
            }
        })
        alertDiv.dialog('open');
        if(hideafter !== undefined && hideafter != null) {
            if(!isNaN(hideafter)){
                setTimeout(function(){
                    if(alertDiv.parent().css('display') == "none"){
                        // 已关闭弹窗，什么事都不做
                    } else {
                        // 没关闭
                        alertDiv.parent().find('.ui-button-text').eq(0).trigger('click');
                    }
                },hideafter);
            }
        }
    }
    Ali.Alert.noNavLoad = function(html, width, height, callback, isCancel, title){
        var alertDiv = Ali.Alert.noNavLoadDiv || $('.tmNoNavLoadDiv');
        if(alertDiv.length == 0){
            alertDiv  = $('<div class="tmNoNavLoadDiv"></div>')
            alertDiv.appendTo($('body'));
            Ali.Alert.noNavLoadDiv = alertDiv;
        }

        $("body").mask();
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
            buttons:{
                '确定':function(){
                    callback && callback();
                    $(this).dialog('close');
                    $("body").unmask();
                }
            },beforeClose: function(){
                $("body").unmask();
            }
        })
        alertDiv.dialog('open');

//        setTimeout(function(){
//
//        },1000);
        var dg = Ali.Alert.noNavLoadDiv.parent();
        var left = (new Number(dg.css('left').replace('px',''))+140)+'px'
        dg.css('left',left);
    }

    Ali.Alert.load=function(html, width, height, callback, isCancel, title, hideafter){
        var alertDiv = Ali.Alert.getDom();
        $("body").mask();
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
            buttons:{
                '确定':function(){
                    callback && callback();
                    $(this).dialog('close');
                    $("body").unmask();
                }
            },beforeClose: function(){
                $("body").unmask();
            }
        })
        alertDiv.dialog('open');
        if(hideafter !== undefined && hideafter != null) {
            if(!isNaN(hideafter)){
                setTimeout(function(){
                    if(alertDiv.parent().css('display') == "none"){
                        // 已关闭弹窗，什么事都不做
                    } else {
                        // 没关闭
                        alertDiv.parent().find('.ui-button-text').eq(0).trigger('click');
                    }
                },hideafter);
            }
        }

        var dg = alertDiv.parent().parent();
        var left = (new Number(dg.css('left').replace('px',''))+200)+'px'
        dg.css('left',left);
    };

    Ali.Alert.showDialog=function(html, width, height, okCallback, cancelCallback, title){
        var alertDiv = Ali.Alert.getDom();
        $("body").mask();
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
            buttons:{
                '确定':function(){
                    okCallback && okCallback();
                    $(this).dialog('close');
                    $("body").unmask();
                },
                '取消':function(){
                    cancelCallback && cancelCallback();
                    $(this).dialog('close');
                    $("body").unmask();
                }
            },beforeClose:function(){
            }
        })
        alertDiv.dialog('open');

    };

    Ali.Alert.chooseWangwang = function(html, callback, hideafter){
        var diag = $('.chooseWW');
        $("body").mask();
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
                    Ali.Alert.load('请输入旺旺');
                    return;
                }
                if(callback(text)){
                    $(this).dialog('close');
                }else{
                    // nothing to do....
                }
                $("body").unmask();
            },'取消':function(){
                $(this).dialog('close');
                $("body").unmask();
            }}
        });
//        diag.html('<p>请输入您要监控的店铺掌柜名:</p><p><input type="text" value=""></p>');
        if(html){
            diag.html(html);
        }
        diag.dialog('open');
        if(hideafter !== undefined && hideafter != null) {
            if(!isNaN(hideafter)){
                setTimeout(function(){
                    if(diag.parent().css('display') == "none"){
                        // 已关闭弹窗，什么事都不做
                    } else {
                        // 没关闭
                        diag.parent().find('.ui-button-text').eq(0).trigger('click');
                    }
                },hideafter);
            }
        }
    }

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

            //是否使用jsonp
            function isUseJsonp() {
                var isJsonp = param.isJsonp;
                if (isJsonp == true) {
                    return true;
                } else {
                    return false;
                }
            }


            //每页多少条

            function getPageSizeOption() {
                /*var html = '每页&nbsp;<select style="width: 60px;" class="paging-size-select">' +
                 '   <option value="10" selected="selected">10</option> ' +
                 '   <option value="20">20</option> ' +
                 '   <option value="50">50</option> ' +
                 '   <option value="100">100</option> ' +
                 '</select>&nbsp;条&nbsp;';
                 return html;*/

                var pageSizeArray = param.selfPageSizeArray;
                if (pageSizeArray === undefined || pageSizeArray == null || pageSizeArray.length <= 0) {

                    var html = '每页显示&nbsp;' +
                        '<span class="small-pagesize-span"><span class="paging-size5 paging-size-span paging-nav paging-size-link" style="font-weight: bold;cursor: pointer;">5</span>|&nbsp;</span>' +
                        '<span class="paging-size10 paging-size-span paging-nav paging-size-link" style="font-weight: bold;cursor: pointer;">10</span>' +
                        '<span class="paging-size20 paging-size-span paging-nav paging-size-link" style="font-weight: bold;cursor: pointer;">20</span>' +
                        //'<span class="paging-size50 paging-size-span" style="font-weight: bold;color: blue;cursor: pointer;">50</span>&nbsp;|&nbsp;' +
                        '<span class="paging-size50 paging-size-span paging-nav paging-size-link" style="font-weight: bold;cursor: pointer;">50</span>' +
                        '条&nbsp;';
                    return html;
                } else {
                    var html = '每页显示&nbsp;';
                    $(pageSizeArray).each(function(index, selfPageSize) {
                        html += '<span class="paging-size' + selfPageSize + ' paging-size-span paging-nav paging-size-link" style="font-weight: bold;cursor: pointer;">' + selfPageSize + '</span>';
                        if (index < pageSizeArray.length - 1) {
                            html += '&nbsp;|&nbsp;';
                        }
                    });
                    html += '&nbsp;条&nbsp;';
                    return html;
                }

            }

            function isSelfDefinePageSize() {
                var pageSizeArray = param.selfPageSizeArray;
                if (pageSizeArray === undefined || pageSizeArray == null || pageSizeArray.length <= 0)
                    return false;
                return true;
            }

            //设置选择每页多少条的事件
            function setPageSizeOptionEvent(obj) {
                /*obj.find(".paging-size-select").change(function () {
                 var newPageSize = $(this).val();
                 pageSize = newPageSize;
                 //保存到cookie
                 $.cookie(tmPagingSizeCookie, newPageSize, {expires: 365, path:'/'});
                 createView(1);
                 });*/
                obj.find(".paging-size-span").click(function () {
                    var newPageSize = $(this).html();
                    pageSize = newPageSize;
                    //保存到cookie
                    $.cookie(tmPagingSizeCookie, newPageSize, {expires: 365, path:'/'});
                    createView(1);
                });

            }

            //设置当前的pageSize
            function setCurrentPageSizeOption(obj, curretPageSize) {
                //obj.find(".paging-size-select").val(pageSize);

                var className = "paging-size" + curretPageSize;
                //obj.find("." + className).css("color", "#a10000");
                if (getIsUseSmallPageSize() == false) {
                    obj.find(".small-pagesize-span").remove();
                }

                obj.find("." + className).removeClass("paging-size-link");
                obj.find("." + className).addClass("paging-size-select");
            }

            var tmPagingSizeCookie = "tmPagingSizeCookie";
            if (param && param instanceof Object) {
                var options;
                var currPage;
                var pageCount;
                var pageSize;
                var tempPage;
                var resultCount;
                var linkNum = 10;
                var defaults = new Object({
                    currPage:1,
                    pageCount:10,
                    pageSize:5,
                    useSmallPageSize: false,
                    isJsonp: false,
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
                        msg:'<span>&nbsp;&nbsp;共&nbsp;{sumPage}&nbsp;页&nbsp;' +
                            getPageSizeOption() +
                            '<span class="mf25"></span>到&nbsp;{currText}&nbsp;页&nbsp;<input  class="gopage-submit hand" title="跳转页面" type="button" value="确定"/></span>'
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
                function getResultCount() {
                    if (options.resultCount ) {
                        return options.resultCount;
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

                function getIsUseSmallPageSize() {
                    if (typeof options.useSmallPageSize != 'undefined') {
                        return options.useSmallPageSize;
                    } else {
                        return defaults.useSmallPageSize;
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

            function updateView(resultCount) {
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
                if (firstPage + linkNum > pageCount) {
                    lastPage = pageCount + 1;
                    firstPage = lastPage - linkNum;
                } else {
                    lastPage = firstPage + linkNum;
                }
                var content = '<div class="tm-paging-container">';

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
                        content += "<span class=\"paging-nav paging-select current\" title=\"" + firstPage + "\">" + firstPage + "</span>&nbsp;";
                    } else {
                        content += "<a class='paging-nav paging-link' href='" + link + "' title='" + firstPage + "'>" + firstPage + "</a>&nbsp;";
                    }
                }

                if (currPage == pageCount) {
                    content += "<span class=\"page-next page-next-disabled\" title=\"" + getNext() + "\"></span>&nbsp;";
                } else {
                    content += "<a class='page-next page-next-abled' href='" + link + "' title='" + (currPage + 1) + "'>"+getNext()+" </a>&nbsp;";
                }

                content += getText();
                content += '</div>';

                if( (resultCount && resultCount < 10)){
                    obj.html('');
                }else {
                    obj.html(content);
                }

                setCurrentPageSizeOption(obj, pageSize);

                obj.find(":text").keypress(function (event) {
                    var keycode = event.which;
                    if (keycode == 13) {
                        var page = $(this).val();
                        if (isCode(page)) {
//                            obj.find("a").unbind("click");
//                            obj.find("a").each(function() {
//                                $(this).click(function() {
//                                    return false;
//                                })
//                            });
                            createView(page);
                        }
                    }
                });

                obj.find(":button").click(function () {
                    var page = obj.find(":text").val();
                    if (isCode(page)) {
                        createView(page);
                    }
                });
                setPageSizeOptionEvent(obj);
                obj.find("a").click(function (i) {
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

                    var ajaxEndCallback = function(data) {
                        if(!data) {
                            return;
                        }

                        if (Ali.AliUtil.util.judgeAjaxResult(data) == false) {
                            return;
                        }

                        if (data.isOk) {
                            loadPageCount({
                                dataType:ajax.dataType,
                                callback:ajax.callback,
                                data:data
                            });
                            updateView(data.count);
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

                    }

                    if (isUseJsonp() == true) {

                        TM.Loading.init.show();
                        /*if (varUrl.indexOf("?") >= 0) {
                         varUrl += "&pn=" + param.pn + "&ps=" + param.ps;
                         } else {
                         varUrl += "?pn=" + param.pn + "&ps=" + param.ps;
                         }*/
                        $.ajax({
                            type: "get",
                            url: varUrl,
                            data:param,
                            dataType: "jsonp",
                            success: function(data){

                                ajaxEndCallback(data);

                                TM.Loading.init.hidden();
                            },
                            error: function(){
                                updateView();
                                TM.Loading.init.hidden();
                            }
                        });
                    } else {
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
                                ajaxEndCallback(data);
                            }
                        })
                    }


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
                /*if (currPage > pageCount) {
                 TM.Alert.load('配置参数错误\n错误代码:-2');

                 //alert("配置参数错误\n错误代码:-2");
                 return false;
                 }*/
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

            if (getIsUseSmallPageSize() == true) {
                tmPagingSizeCookie = "SmallPageSize_" + tmPagingSizeCookie;
            }
            var cookiePageSize = $.cookie(tmPagingSizeCookie);
            if (cookiePageSize == null || cookiePageSize <= 0) {
                pageSize = getPageSize();
            } else {
                pageSize = cookiePageSize;
            }
            if (pageSize <= 8 && getIsUseSmallPageSize() == true)
                pageSize = 5;
            else if (pageSize <= 15)
                pageSize = 10;
            else if (pageSize <= 30)
                pageSize = 20;
            else
                pageSize = 50;

            //天猫联盟中，降权分页
            if (isSelfDefinePageSize() == true) {
                tmPagingSizeCookie = "SelfDefinePageSize_" + tmPagingSizeCookie;
                cookiePageSize = $.cookie(tmPagingSizeCookie);
                if (cookiePageSize == null || cookiePageSize <= 0) {
                    pageSize = getPageSize();
                } else {
                    pageSize = cookiePageSize;
                }
            }


            tempPage = parseInt(linkNum / 2);
            if (checkParam() && createView(currPage)) {
                updateView();
            }
        }
    });
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

    Date.prototype.formatYMDHMS = function(){
        return this.format("yyyy-MM-dd hh:mm:ss");
    }
})(jQuery, window));
