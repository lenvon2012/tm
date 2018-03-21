var TM = TM || {};

((function ($, window) {

    var path = document.location.pathname;

    TM.path = path;

    TM.JSLoad = TM.JSLoad || {};
    var getCacheScript = function(url, callback){

        callback = callback || function(){}
        if(TM.JSLoad[url]){
            callback();
        }else{
            $.ajax({url:url,dataType:"script",cache:true, success : function(){
                TM.JSLoad[url] = true;
                callback();
            }});
        }
    }
    TM.gcs = getCacheScript;
    var ensureJs = function(url, callback){
        if(TM.JSLoad[url]){
            callback();
        }else{
            TM.gcs(url,callback);
        }
    }
    TM.ensureJs = ensureJs;

    jQuery.getScript =TM.gcs;
    $.getScript = TM.gcs;

})(jQuery, window));


((function ($, window) {


    TM.Loading = TM.Loading || {};

    TM.Loading.init = $.extend({
        //显示加载，opts是选项，暂时没用到
        show: function(opts) {
            $(".LoadingMask").css("position", "absolute");
            $(".WaitingForLoadingDiv").css("position", "absolute");
            TM.BackMask.show($(".LoadingMask"));
            var divObj = $(".WaitingForLoadingDiv");
            TM.CenterAlert.show(divObj);
        },
        hidden: function() {
            var divObj = $(".WaitingForLoadingDiv");
            TM.CenterAlert.hidden(divObj);
            TM.BackMask.hidden($(".LoadingMask"));
        }
    }, TM.Loading.init);

    var CenterAlert = CenterAlert || {};
    CenterAlert = $.extend({
        show: function(divObj) {
            var windowWidth = $(window).width();
            var windowHeight = $(window).height();
            var scrollLeft = $(document).scrollLeft();
            var scrollTop = $(document).scrollTop();

            var divWidth = divObj.width();
            var divHeight = divObj.height();
            var left = (windowWidth - divWidth) / 2 + scrollLeft;
            var top = (windowHeight - divHeight) / 2 + scrollTop;
            if (left < 0)
                left = scrollLeft;
            if (top < 0)
                top = scrollTop;
            divObj.css("position", "absolute");
            divObj.css("left", left + "px");
            divObj.css("top", top + "px");
            divObj.css("display", "block");

            divObj.find(".loading-mask-iframe").css("width", divWidth + "px");
            divObj.find(".loading-mask-iframe").css("height", divHeight + "px");
        },
        hidden: function(divObj) {
            divObj.css("display", "none");
        }
    }, CenterAlert);

    var BackMask = BackMask || {};


    BackMask = $.extend({
        show: function(maskObj) {
            var maskWidth = $(document).width();
            var maskHeight = $(document).height();
            maskObj.css("width", maskWidth + "px");
            maskObj.css("height", maskHeight + "px");
            maskObj.css("left", "0px");
            maskObj.css("top", "0px");
            maskObj.show();
        },
        hidden: function(maskObj) {
            maskObj.hide();
        }
    }, BackMask);

    TM.CenterAlert = CenterAlert;
    TM.BackMask = BackMask;


    $(document).ajaxStart(function(){
        TM.Loading.init.show();
    });
    $(document).ajaxStop(function(){
        TM.Loading.init.hidden();
    });

})(jQuery, window));



((function ($, window) {

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
    Number.prototype.toTenThousand = function (n) {
        n = n || 0;
        return ( Math.round(this * Math.pow(10, n)) / Math.pow(10, n + 4) ).toFixed(n) + '万';
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
    Date.prototype.formatYMDMS = function(){
        return this.format('yyyy-MM-dd hh:mm:ss');
    }

    jQuery.fn.replaceClass = function(oldClass, newClass){
        this.removeClass(oldClass);
        this.addClass(newClass);
    }


})(jQuery, window));





