var TM = TM || {};
((function ($, window) {
    TM.ShopBase = TM.ShopBase || {};

    var ShopBase = TM.ShopBase;

    ShopBase.init = ShopBase.init || {};
    ShopBase.init = $.extend({
        showColumnNum: 0,
        doInit: function() {
            var windowWidth = $(window).width();
            if (windowWidth >= 1200) {
                $(".nav-container").css("width", "1100px");
                $(".content-wrapper").css("width", "1200px");
                ShopBase.init.showColumnNum = 5;
            } else if (windowWidth >= 950) {
                $(".nav-container").css("width", "900px");
                $(".content-wrapper").css("width", windowWidth + "px");
                ShopBase.init.showColumnNum = 4;
            } else {
                $(".nav-container").css("width", "900px");
                $(".content-wrapper").css("width", "950px");
                ShopBase.init.showColumnNum = 4;
            }

            ShopBase.init.createNav();
            ShopBase.init.setSelectedNav();

            //搜索框
            TM.gcs('/js/jquery.url.js', function () {
                var searchTitle = ShopBase.util.getItemSearchParam();
                if (searchTitle === undefined || searchTitle == null || searchTitle == "") {

                } else {
                    $(".item-search-input").removeClass("search-tip");
                    $(".item-search-input").val(searchTitle);
                }
            });
        },
        createNav: function() {
            var html = '' +
                '<tr>' +
                '    <td><a class="nav-link first-page-link" href="/shopping/index">首页</a></td>' +
                '    <td><a class="nav-link" href="/shopping/index?topcat=1">女装</a></td>' +
                '    <td><a class="nav-link" href="/shopping/index?topcat=2">男装</a></td>' +
                '    <td><a class="nav-link" href="/shopping/index?topcat=3">鞋子</a></td>' +
                '    <td><a class="nav-link" href="/shopping/index?topcat=4">包包</a></td>' +
                '    <td><a class="nav-link" href="/shopping/index?topcat=5">美容</a></td>' +
                '    <td><a class="nav-link" href="/shopping/index?topcat=6">家居</a></td>' +
                '    <td><a class="nav-link" href="/shopping/index?topcat=7">母婴</a></td>' +
                '    <td><a class="nav-link" href="/shopping/index?topcat=8">数码</a></td>' +
                '    <td><a class="nav-link" href="/shopping/index?topcat=9">美食</a></td>' +
                '    <td><a class="nav-link" href="/shopping/index?topcat=10">随意淘</a></td>' +
                '</tr> ' +
                '';

            $('.nav-wrapper').find("table").find("tbody").html(html);
        },
        setSelectedNav: function() {
            TM.gcs('/js/jquery.url.js', function () {
                var topCatId = ShopBase.util.getSearchCat();
                var navObj = $('.nav-wrapper');
                if (topCatId === undefined || topCatId == null) {
                    navObj.find(".first-page-link").addClass('selected-nav');
                } else {
                    navObj.find('a').each(function(){
                        var anchor = $(this);
                        var tempParams = $.url(anchor.attr('href'));
                        if(tempParams.param('topcat') == topCatId){
                            navObj.find('a').removeClass('selected-nav');
                            anchor.addClass('selected-nav');
                        }
                    });
                }
            });
        }
    }, ShopBase.init);

    ShopBase.util = ShopBase.util || {};
    ShopBase.util = $.extend({
        judgeAjaxResult: function(dataJson) {
            var message = dataJson.msg;
            if (message === undefined || message == null || message == "") {

            } else {
                alert(message);
            }
            return dataJson.isOk;
        },
        getSearchCat: function() {
            var href = window.location.href;
            var params = $.url(href);
            var topCatId = params.param('topcat');

            return topCatId;
        },
        getItemSearchParam: function() {
            var href = window.location.href;
            var params = $.url(href);
            var searchTitle = params.param('search');
            if (searchTitle === undefined || searchTitle == null)
                searchTitle = "";
            else {
                searchTitle =decodeURI(searchTitle);//解码
            }
            return searchTitle;
        }
    }, ShopBase.util);


})(jQuery,window));