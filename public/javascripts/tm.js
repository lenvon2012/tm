var TM = TM ||{};

TM.util = TM.util || {};

TM.widget = TM.widget || {};

TM.price = TM.price || {};

((function ($, window) {
    TM.price.youhua = 19;
    TM.price.zhizun = 39;
    TM.price.zhizunMonth = 3;

    TM.widget.qtipStyle = {
        'name':'cream',
        'color':'black',
        'border':{width:1,color:'#fda145'},
        'width':'50px',
        'background-color':'rgb(254, 254, 248)'
    };
    TM.widget.simpleQtips = function(target, content){
        var oThis = target;
        oThis.qtip({
            content: {
                text: content
            },
            position: {
                at: "top left ",
                corner: {
                    target: 'bottomRight'
                }
            },
            show: {
                ready:false
            },
//                        hide:false,
            style:TM.widget.qtipStyle
        });
    }

    TM.widget.bindQtips = function(){
        $('.question').each(function(i,elem){
            var oThis = $(this);
            oThis.qtip({
                content: {
                    text: oThis.attr('content')
                },
                position: {
                    at: "top left ",
                    corner: {
                        target: 'bottomRight'
                    }
                },
                show: {
                    ready:false
                },
//                        hide:false,
                style:TM.widget.qtipStyle
            });

        });
    }
    TM.widget.setLotteryInterval = function(){
        if(TM.ver >= 20){
            return;
        }
        setInterval(function(){
            var goodRateTxt = $('#lotteryanchor');
            if(goodRateTxt.hasClass('toRed')){
                goodRateTxt.css('color','red');
                goodRateTxt.removeClass('toRed');
            }else{
//                    goodRateTxt.css('color','#0088cc');
                goodRateTxt.css('color','blue');
                goodRateTxt.addClass('toRed');
            }

        },500);
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
                        '<span class="paging-size10 paging-size-span paging-nav paging-size-link" style="font-weight: bold;cursor: pointer;">10</span>|&nbsp;' +
                        '<span class="paging-size20 paging-size-span paging-nav paging-size-link" style="font-weight: bold;cursor: pointer;">20</span>|&nbsp;' +
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
                            '到&nbsp;{currText}&nbsp;页&nbsp;<input  class="gopage-submit hand" title="跳转页面" type="button" value="确定"/></span>'
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
                        if(!data){
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
                            global: typeof ajax.global == 'undefined',
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
        TM.Alert.load(html.join(''),600,340);
    }

    TM.Alert.showVIPNeeded = function(){
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

        TM.Alert.load(html.join(''),700,550);
    }

    TM.Alert.getDom = function(){
        TM.Alert.alertDom = TM.Alert.alertDom || $('.tmAlert');
        if(TM.Alert.alertDom.length == 0){
            TM.Alert.alertDom = $('<div class="tmAlert"></div>')
            TM.Alert.alertDom.appendTo($('body'));
        }
        return TM.Alert.alertDom;
    }
    TM.Alert.getDetail = function(){
        TM.Alert.alertDetail = TM.Alert.alertDetail || $('.tmAlertDetail');
        if(TM.Alert.alertDetail.length == 0){
            TM.Alert.alertDetail  = $('<div class="tmAlertDetail"></div>')
            TM.Alert.alertDetail.appendTo($('body'));
        }
        return TM.Alert.alertDetail;
    }
    TM.Alert.loadDetail = function(html, width, height, callback,title, hideafter, before, after){

        $("body").mask();
        TM.Loading.beforeShow && TM.Loading.beforeShow();

        var alertDiv = TM.Alert.getDetail();
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
                    }
                    $("body").unmask();
                },
                '取消':function(){
                    $(this).dialog('close');
                    $("body").unmask();
                }
            }
            ,beforeClose:function(event, ui){
                $("body").unmask();
                after && after();
                TM.Loading.afterShow && TM.Loading.afterShow();
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
    TM.Alert.noNavLoad = function(html, width, height, callback, isCancel, title){
        var alertDiv = TM.Alert.noNavLoadDiv || $('.tmNoNavLoadDiv');
        if(alertDiv.length == 0){
            alertDiv  = $('<div class="tmNoNavLoadDiv"></div>')
            alertDiv.appendTo($('body'));
            TM.Alert.noNavLoadDiv = alertDiv;
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
                TM.Loading.afterShow && TM.Loading.afterShow();
            }
        })
        alertDiv.dialog('open');

//        setTimeout(function(){
//
//        },1000);
        var dg = TM.Alert.noNavLoadDiv.parent();
        var left = (new Number(dg.css('left').replace('px',''))+140)+'px'
        dg.css('left',left);
    }

    TM.Alert.load=function(html, width, height, callback, isCancel, title, hideafter){
        var alertDiv = TM.Alert.getDom();
        $("body").mask();
        TM.Loading.beforeShow && TM.Loading.beforeShow();
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
                TM.Loading.afterShow && TM.Loading.afterShow();
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

    TM.Alert.showDialog=function(html, width, height, okCallback, cancelCallback, title){
        var alertDiv = TM.Alert.getDom();
        $("body").mask();
        TM.Loading.beforeShow && TM.Loading.beforeShow();
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
                TM.Loading.afterShow && TM.Loading.afterShow();
            }
        })
        alertDiv.dialog('open');
    };

    TM.Alert.chooseWangwang = function(html, callback, hideafter){
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
                    TM.Alert.load('请输入旺旺');
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
                        //var params = TM.AudotDelist.getParams(distriTypeDiv);
                        //if (params === undefined || params == null)
                        //    return;

                        var params = {};
                        params.planId = TM.AudotDelist.getPlanId();

                        $.ajax({
                            //async : false,
                            url : '/delistplan/turnOnPlan',
                            data : params,
                            type : 'post',
                            error: function() {
                            },
                            success: function (dataJson) {
                                if (TM.DelistBase.util.judgeAjaxResult(dataJson) == false) {
                                    return;
                                }
                                var isSuccess = dataJson.results;
                                if (isSuccess == false) {
                                    TM.Alert.load('亲,上下架计划开启失败！');
                                } else {
                                    TM.Alert.load('亲,上下架计划开启成功！');
                                }
                                openCallback && openCallback();
                            }
                        });
                    } else if (isCurrentOn == true) {//要关闭

                        var params = {};
                        params.planId = TM.AudotDelist.getPlanId();

                        $.ajax({
                            //async : false,
                            url : '/delistplan/turnOffPlan',
                            data : params,
                            type : 'post',
                            error: function() {
                            },
                            success: function (dataJson) {
                                if (TM.DelistBase.util.judgeAjaxResult(dataJson) == false) {
                                    return;
                                }
                                var isSuccess = dataJson.results;
                                if (isSuccess == false) {
                                    TM.Alert.load('亲,上下架计划关闭失败！');
                                } else {
                                    TM.Alert.load('亲,上下架计划关闭成功！');
                                }
                                closeCallback && closeCallback();
                            }
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
//        html.push('，您还在犹豫什么？</p>');
        html.push('</p>');
        html.push('<p>现在升级，即能享受<b class="red">至尊版69元包年特价</b></p>');
        html.push('<table style="margin: 10px auto;width:350px;padding-left:20px;">');
//        html.push('<tr><td><a style="margin-left: 100px;*margin-left: 0px;" target="_blank" class="orange-button" href="http://to.taobao.com/1Abfzgy" >马上升级</a></td>');
        html.push('<tr><td><a style="margin-left: 100px;*margin-left: 0px;" target="_blank" class="orange-button" href="/home/freeup" >马上升级</a></td>');
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
            if(TM.isAutoShow === true){
                TM.ShowWindow.buildAutoStatus(windowSwith, true);
            }else if(TM.isAutoShow === false){
                TM.ShowWindow.buildAutoStatus(windowSwith, false);
            }else{
                $.get('/windows/isOn',function(data){
                    TM.ShowWindow.buildAutoStatus(windowSwith, data.res);
                });
            }

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
            if(TM.isAutoComment === true){
                TM.Comment.buildAutoStatus(windowSwith, true);
            }else if(TM.isAutoComment === false){
                TM.Comment.buildAutoStatus(windowSwith, false);
            }else{
                $.get('/autocomments/isOn',function(data){
                    TM.Comment.buildAutoStatus(windowSwith, data.res);
                });
            }
        }
        windowSwith.appendTo(container);
    }

    TM.widget.buildLeft150Nav = function(){
        var nav = $(".nav-table nav");
        if(nav.length <= 0){
            return;
        }

        nav.find('a.menu').click(function(){
            var me = $(this);
            var icon = me.find('i');
            var line = me.parent();
            if(line.hasClass("active")){
                // TODO hide...
                line.removeClass('active');
                line.find('.sub-lists ').hide(300);
                icon.removeClass('icon-show');
                icon.addClass('icon-fold');
            }else{
                // TODO show
                line.addClass('active');
                line.find('.sub-lists ').show(300);
                icon.addClass('icon-show');
                icon.removeClass('icon-fold');
            }
        })
        var currHref = window.location.href.toLowerCase();
        nav.find('.sub-lists a').each(function(i, elem){
            var thisSubNav = $(elem);
            var target = thisSubNav.attr('href').toLowerCase();
            if(target.length > 3 && currHref.indexOf(target)>=0){
                thisSubNav.parent().parent().parent().find('a.menu').trigger('click');
                thisSubNav.find('em').addClass('rigth-small-icon');
            }
        });
    }
})(jQuery, window));

String.prototype.cnLength = function () {
    return this.replace(/[^u4E00-u9FA5]/g, 'nn').length;
}

String.prototype.trim = function () {
    return this.replace(/^\s*|\s*$/g, '');
}
String.prototype.isBlank = function () {
    return this.replace(/^\s*|\s*$/g, '').length <= 0;
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

Date.prototype.formatYMDHMS = function(){
    return this.format("yyyy-MM-dd hh:mm:ss");
}




var genKeywordSpan = genKeywordSpan || {};

((function ($, window) {
    genKeywordSpan.gen = function(options){
        options = $.extend({
            text:'',
            callback:'',
            enableStyleChange:true,
            spanClass:'addTextWrapper',
            enableShadow : true,
            addBtn : true
        }, options);

//        var spanObj = $("<span style='border:solid 2px #5CADAD;margin: 0px 5px 15px 5px; padding: 5px 15px;font-size: 14px;display:-moz-inline-box;  display: inline-block;cursor: pointer'></span>");
//        var plusObj = $("<img src='/public/images/plus2.png' style='margin-left:-6px; margin-right: 8px;'>");
//        spanObj.append(plusObj);
//        spanObj.append(options.text);
//        spanObj.click(function(){
//            options.callback(options.text,spanObj);
//        });
        return "<span class='"+ (options.enableShadow?'shadowbase ':'')+options.spanClass+"'>"
//                        + (options.addBtn ? "<img src='/img/btns/addblue.png' style='margin-left:-6px; margin-right: 4px;'>":"")
            + (options.addBtn ? "<img src='/img/btns/addblue.png' >":"")
            + options.text + "</span>";

//        return spanObj;
    }
})(jQuery, window));

TM.tmJsLoaded = true;