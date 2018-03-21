
/**
 * 修改标题那个页面的js
 * @type {*}
 */
var ModifyTitle = ModifyTitle || {};

ModifyTitle.main = ModifyTitle.main || {};

ModifyTitle.event = ModifyTitle.event || {};

ModifyTitle.util = ModifyTitle.util || {};

ModifyTitle.main = $.extend({
    init: function(params) {
        // TODO ???? why??????????
//        $(document).unbind();
        Diagnose.init.doInit(params);
        //推荐词
        RecWord.main.doInit("", params.itemId);
       // HotTitle.init.doInit(params.itemId);
        //HotTitle.init.initCompetition($('.hotTitleBlock'),params.itemId);
        LongTail.init.doInit(params);
        MyWords.init.doInit(params);
        BusSearch.init.doInit(params);
        CWords.init.doInit(params);
        PromoteWord.init.doInit();
        ModifyTitle.event.setEvent(params);
        RenameHistory.init.doInit(params);
        RecentlySearchWords.init.doInit(params);
        PvUvDiag.init.doInit(params, $('.pvuvDiagResultBlock'));
        //TM.BusTopKey.Init.init($('#bustopkey'), params.itemId);
        TM.CatTopWord.init.doInit($('.cat-top-word-container'), params.itemId);
        if(params.enableHotProps){
            ItemProp.init.hotProps($(".hotPropsBlock"),params.itemId);
            $(".hotPropsBlock").hide();
        }else{
            $(".hotPropsBlock").hide();
        }
        // 推荐标题
        $.post("/Titles/shortTitleRecommend",{numIid:params.itemId}, function(data){
            if(data === undefined || data ==  null){
                return;
            }
            if(data == ""){
                return;
            }
            $('.shortTitleRecommend').text(data);

            $('.shortTitleRecommendDiv').show();
            $('.shortTitleRecommendDiv .use-recommend').unbind('click').click(function(){
                $('.newTitle').val(data);
                $('.submitNewTitle').trigger('click');
            });
        });

        // 获取标题中有点击的关键词
        $.ajax({
            //async : false,
            url : '/Titles/TitleComeInWordsClick',
            data : {numIid: params.itemId},
            type : 'post',
            global: false,
            success : function(data) {
                if(data === undefined || data == null) {
                    return;
                }
                if(data.success == false) {
                    return;
                }
                var comeInWords = $('<div class="comeInWords"></div>');
                $.each(data,function(key,value){
                    comeInWords.append($('<span class="key clickword">'+key+'(<span class="value">'+value+'</span>)</span>'));
                });
                // 如果找不到有点击的关键词，那么隐藏这个快
                if(comeInWords.find('.key').length <= 0) {
                    return;
                }
                $('.comeInWordsWrapper').append(comeInWords);
                $('.clickWordsTitle').show();
                $('.comeInWordsWrapper').show();
            }
        });

        // 获取标题中有点击的无线端的关键词
        $.ajax({
            url : '/Titles/titleComeInWirelessWordsClick',
            data : {numIid: params.itemId},
            type : 'get',
            global: false,
            success : function(data) {
                if(data === undefined || data == null) {
                    return;
                }
                if(data.success == false) {
                    return;
                }
                var comeInWords = $('<div class="comeInWirelessWords"></div>');
                $.each(data,function(key,value){
                    comeInWords.append($('<span class="wirelessKey clickword">'+key+'(<span class="value">'+value+'</span>)</span>'));
                });
                // 如果找不到有点击的关键词，那么隐藏这个快
                if(comeInWords.find('.wirelessKey').length <= 0) {
                    return;
                }
                $('.comeInWirelessWordsWrapper').append(comeInWords);
                $('.clickWirelessWordsTitle').show();
                $('.comeInWirelessWordsWrapper').show();
            }
        });

        // 获取标题中无点击的关键词
        $.ajax({
            //async : false,
            url : '/Titles/TitleNoClickWords',
            data : {numIid: params.itemId},
            type : 'post',
            global: false,
            success : function(data) {
                if(data === undefined || data == null) {
                    return;
                }
                if(data.success == false) {
                    return;
                }
                if(data.length <= 0) {
                    return;
                }
                var noClickWords = $('<div class="noClickWords"></div>');

                $(data).each(function(i, key){
                    noClickWords.append($('<span class="key noclickword">'+key+'</span>'));
                });
                // 如果找不到有点击的关键词，那么隐藏这个快
                if(noClickWords.find('.key').length <= 0) {
                    return;
                }
                $('.noClickWordsWrapper').append(noClickWords);
                $('.noClickWordsWrapper').show();
                $('.no-click-title').show();
            }
        });

        // 获取标题中无线端 无点击的关键词
        $.ajax({
            url : '/Titles/titleNoClickWirelessWords',
            data : {numIid: params.itemId},
            type : 'get',
            global: false,
            success : function(data) {
                if(data === undefined || data == null) {
                    return;
                }
                if(data.success == false) {
                    return;
                }
                if(data.length <= 0) {
                    return;
                }
                var noClickWords = $('<div class="noClickWords"></div>');
                $(data).each(function(i, key){
                    noClickWords.append($('<span class="wirelessKey noclickword">'+key+'</span>'));
                });
                // 如果找不到有点击的关键词，那么隐藏这个快
                if(noClickWords.find('.wirelessKey').length <= 0) {
                    return;
                }
                $('.noClickWirelessWordsWrapper').append(noClickWords);
                $('.noClickWirelessWordsWrapper').show();
                $('.clickWirelessWordsTitle').show();
            }
        });

    }
}, ModifyTitle.main);

var PvUvDiag = PvUvDiag || {};
PvUvDiag.Event = PvUvDiag.Event || {};

PvUvDiag.Event = $.extend({
    setEvent : function(){
        PvUvDiag.Event.setPvUvIntervalEvent();
        PvUvDiag.Event.setOpTabClickEvent();
    },
    setPvUvIntervalEvent : function(){
        var curr = new Date().getTime();
        var dayMillis = 24 * 3600 * 1000;
        var startTimeInput = $("#pvuvDiag-startTimeInput");
        var endTimeInput = $("#pvuvDiag-endTimeInput");
        startTimeInput.datepicker();
        startTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
        startTimeInput.val(new Date(curr - 7 * dayMillis).formatYMS());

        endTimeInput.datepicker();
        endTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
        endTimeInput.val(new Date(curr).formatYMS());

        startTimeInput.unbind('change').change(function(){
            PvUvDiag.init.Container.find('.pvuvDiag-interval-tr .pvuvDiag-interval[value="0"]').trigger("click");
        });
        endTimeInput.unbind('change').change(function(){
            PvUvDiag.init.Container.find('.pvuvDiag-interval-tr .pvuvDiag-interval[value="0"]').trigger("click");
        });
        PvUvDiag.init.Container.find('.pvuvDiag-interval-tr .pvuvDiag-interval').unbind('click').click(function(){
            var endTime, interval;
            var val = parseInt($(this).val());
            switch (val) {
                case 1 :
                    endTime = curr - dayMillis;
                    interval = 1;
                    break;
                case 3 :
                    endTime = curr - dayMillis;
                    interval = 3;
                    break;
                case 7 :
                    endTime = curr - dayMillis;
                    interval = 7;
                    break;
                case 14 :
                    endTime = curr - dayMillis;
                    interval = 14;
                    break;
                case 0 :
                    if(endTimeInput.val() == new Date().formatYMS()) {
                        endTime = curr - dayMillis;
                    } else {
                        endTime = parseInt(Date.parse(endTimeInput.val().toString().replace("-","/")));
                    }


                    if(endTime > new Date().getTime()) {
                        TM.Alert.load("截止时间请勿超过当前时间");
                        endTimeInput.val(new Date(curr).formatYMS());
                        return;
                    }
                    var startTime = parseInt(Date.parse(startTimeInput.val().toString().replace("-","/")));
                    if(endTime < startTime) {
                        TM.Alert.load("截止时间请勿小于开始时间");
                        return;
                    }
                    interval = Math.floor((endTime - startTime) / dayMillis) + 1;
                    break;
                default :
                    endTime = new Date().getTime();
                    interval = 7;
                    break;
            }
            var platform = PvUvDiag.init.getPlatform();
            PvUvDiag.init.shopViewTrade(platform, interval, endTime);
        });
        PvUvDiag.init.Container.find('.pvuvDiag-interval-tr .pvuvDiag-interval:checked').trigger('click');
    },
    setOpTabClickEvent : function(){
        PvUvDiag.init.Container.find('.opTabWrapper .opTab').unbind('click').click(function(){
            if($(this).hasClass("selected")) {
                return;
            }
            PvUvDiag.init.Container.find('.opTabWrapper .selected').removeClass("selected");
            $(this).addClass("selected");
            var target = $(this).attr("target");
            var interval = PvUvDiag.init.getInterval();
            var endTime = PvUvDiag.init.getEndTime();
            switch (target){
                case "shop" :
                    PvUvDiag.init.shopViewTrade(0, interval, endTime);break;
                case "pc" :
                    PvUvDiag.init.shopViewTrade(1, interval, endTime);break;
                case "wireless" :
                    PvUvDiag.init.shopViewTrade(2, interval, endTime);break;
                default :
                    PvUvDiag.init.shopViewTrade(0, interval, endTime);break;
            }
        });
        PvUvDiag.init.Container.find('.opTabWrapper .opTab').eq(0).trigger("click");
    }
}, PvUvDiag.Event);

PvUvDiag.init = PvUvDiag.init || {};

PvUvDiag.init = $.extend({
    doInit : function(params, container) {
        PvUvDiag.init.Container = container;
        PvUvDiag.init.numIid = params.itemId;
        PvUvDiag.Event.setEvent();
    },
    shopViewTrade : function(platform, interval, endTime){
        // 默认全店
        if(platform === undefined || platform == null) {
            platform = 0;
        }
        if(interval == null) {
            interval = 7;
        }
        if(endTime == null) {
            endTime = new Date().getTime();
        }
        $.get("/Diag/ItemPCWirelessViewTrade", {platform : platform, interval: interval, endTime: endTime, numIid: PvUvDiag.init.numIid}, function(data){
            if(data === undefined || data == null) {
                PvUvDiag.init.refreshShopViewTradeInfo();
            }
            if(data.success == false) {
                PvUvDiag.init.refreshShopViewTradeInfo();
            }
            PvUvDiag.init.setShopViewTradeInfo(data);
        });
    },
    refreshShopViewTradeInfo : function(){
        PvUvDiag.init.Container.find('.shop-pc-online-info .shop-pc-online-info-table .key-value-tr .value').text("0");
    },
    setShopViewTradeInfo : function(data){
        if(data === undefined || data == null) {
            return;
        }
        if(data.success == false) {
            return;
        }
        var tranrate = parseInt(data.uv) == 0 ? "0.00%" : new Number(parseInt(data.alipay_winner_num) / parseInt(data.uv)).toPercent(2);
        PvUvDiag.init.Container.find('.shop-pc-online-info .shop-pc-online-info-table div.pv').text(data.pv);
        PvUvDiag.init.Container.find('.shop-pc-online-info .shop-pc-online-info-table div.uv').text(data.uv);
        PvUvDiag.init.Container.find('.shop-pc-online-info .shop-pc-online-info-table div.tranrate').text(tranrate);
        PvUvDiag.init.Container.find('.shop-pc-online-info .shop-pc-online-info-table div.alipayTradeNum').text(data.alipay_trade_num);
        PvUvDiag.init.Container.find('.shop-pc-online-info .shop-pc-online-info-table div.alipayItemNum').text(data.alipay_auction_num);
        PvUvDiag.init.Container.find('.shop-pc-online-info .shop-pc-online-info-table div.alipayTradeAmount').text(new Number(data.alipay_trade_amt).toFixed(2));
        PvUvDiag.init.Container.find('.shop-pc-online-info .shop-pc-online-info-table div.alipayUserNum').text(data.alipay_winner_num);

    },
    getInterval : function(){
        var value = parseInt(PvUvDiag.init.Container.find('.pvuvDiag-interval-tr .pvuvDiag-interval:checked').val());
        var dayMillis = 24 * 3600 * 1000;
        switch (value) {
            case 1 :
                return 1;
            case 3 :
                return 3;
            case 7 :
                return 7;
            case 14 :
                return 14;
            case 0 :
                var endTimeInput = $("#pvuvDiag-endTimeInput");
                var startTimeInput = $("#pvuvDiag-startTimeInput");
                var curr = new Date().getTime();
                var endTime = parseInt(new Date(Date.parse(endTimeInput.val())).getTime());
                if(endTime > new Date().getTime()) {
                    TM.Alert.load("截止时间请勿超过当前时间");
                    endTimeInput.val(new Date(curr).formatYMS());
                    return;
                }
                var startTime = parseInt(new Date(Date.parse(startTimeInput.val())).getTime());
                if(endTime < startTime) {
                    TM.Alert.load("截止时间请勿小于开始时间");
                    return;
                }
                return Math.floor((endTime - startTime) / dayMillis) + 1;
            default :
                return 7;
        }
    },
    getEndTime : function(){
        var value = parseInt(PvUvDiag.init.Container.find('.pvuvDiag-interval-tr .pvuvDiag-interval:checked').val());
        var curr = new Date().getTime();
        var dayMills = 24 * 3600 * 1000;
        switch (value) {
            case 1 :
                return curr - dayMills;
            case 3 :
                return curr - dayMills;
            case 7 :
                return curr - dayMills;
            case 14 :
                return curr - dayMills;
            case 0 :
                var endTimeInput = $("#endTimeInput");
                var endTime = parseInt(new Date(Date.parse(endTimeInput.val())).getTime());
                if(endTime > new Date().getTime()) {
                    return curr;
                } else {
                    return endTime;
                }
            default :
                return curr - dayMills;
        }
    },
    getPlatform : function(){
        var target = PvUvDiag.init.Container.find('.shop-pc-online-info .opTabWrapper .selected').attr("target");
        switch (target){
            case "shop" :
                return 0;
            case "pc" :
                return 1;
            case "wireless" :
                return 2;
            default :
                return 0;
        }
    }
},PvUvDiag.init);

ModifyTitle.event = $.extend({
    setEvent: function(params) {
        // bus search sort click
        $('.bus-search-sort-th').click(function(){
            var order = $(this).find('.sort').attr('sort');
            var sort = "";
            if($(this).find('.sort').hasClass('Desc')){
                $(this).find('.sort').removeClass('Desc');
                $(this).find('.sort').addClass('Asc');
                sort = "asc";
            } else {
                $(this).find('.sort').removeClass('Asc');
                $(this).find('.sort').addClass('Desc');
                sort = 'desc';
            }
            var s = $('#busSearchText').val();
            BusSearch.init.doInit(params,order,sort,s);
        });
        // #closeImg
        $('#closeImg').click(function(){
            if($(this).hasClass('closed')){
                $(this).removeClass('closed');
                $(this).addClass('open');
                $(this).attr('src','/img/tab/rotate_up-s.png');
                $('.tipContent').fadeIn(1000);
            } else {
                $(this).removeClass('open');
                $(this).addClass('closed');
                $(this).attr('src','/img/tab/rotate_left-s.png');
                $('.tipContent').fadeOut(1000);
            }

        });
        setTimeout(function(){
            $('#closeImg').click();
        },5000)
        //tab
        $(".tabDiv li span").click(function() {
            window.location.hash="recommendDiv";
            var selectObj = $(this);
            if (selectObj.hasClass("select"))
                return;
            $(".tabDiv li span").removeClass("select");
            selectObj.addClass("select");
            var targetDiv = selectObj.attr("targetDiv");
            $(".liTargetDiv").hide();
            $("." + targetDiv).show();
        });
        //返回按钮
        if(params.isKitty) {
            $(".backToItemList").find("a").attr("href", "/KittyTitle/kittyTitle?pn=" + params.currentPage+"&start="+params.start+"&end="+params.end+"&sort="+params.sort+"&status="+params.status);
        } else {
            $(".backToItemList").find("a").attr("href", "/home/commodityDiag?pn=" + params.currentPage+"&start="+params.start+"&end="+params.end+"&sort="+params.sort+"&status="+params.status);
        }
    }
}, ModifyTitle.event);

ModifyTitle.util = $.extend({
    loadingNum: 0,
    showLoading: function() {
        if (ModifyTitle.util.loadingNum <= 0) {
            Loading.init.show({});
            ModifyTitle.util.loadingNum = 0;
        }
        ModifyTitle.util.loadingNum++;

    },
    hideLoading: function() {
        ModifyTitle.util.loadingNum--;
        if (ModifyTitle.util.loadingNum <= 0) {
            Loading.init.hidden();
            ModifyTitle.util.loadingNum = 0;
        }


    },
    putIntoTitle:function(text,spanObj){
        var newTitle = $(".modifyTitleDiv").find(".newTitle");
        if(ModifyTitle.util.countCharacters(newTitle.val()) + ModifyTitle.util.countCharacters(text) > 60) {
            spanObj.qtip({
                content: {
                    text: "标题长度将超过字数限制，请先删减标题后再添加~"
                },
                position: {
                    at: "center left "
                },
                show: {
                    when: false,
                    ready:true
                },
                hide: {
                    delay:1000
                },
                style: {
                    name:'cream'
                }
            });
        }
        else {
            var start = {}, end = {};
            start.left = spanObj.offset().left+"px";
            start.top = spanObj.offset().top+"px";
            end.left = $(".modifyTitleDiv").find(".newTitle").offset().left+"px";
            end.top = $(".modifyTitleDiv").find(".newTitle").offset().top+"px";
            ModifyTitle.util.flyFromTo(start,end,spanObj,function(){
                var dthis = newTitle[0];
                if(document.selection){
                    dthis.focus();
                    var fus = document.selection.createRange();
                    fus.text = text;
                    dthis.focus();
                }
                else if(dthis.selectionStart || dthis.selectionStart == '0'){
                    var start = dthis.selectionStart;
                    var end =dthis.selectionEnd;
                    dthis.value = dthis.value.substring(0, start) + text + dthis.value.substring(end, dthis.value.length);
                }
                else{this.value += text; this.focus();}
                newTitle.trigger('keyup');
            })




          //  newTitle.val(newTitle.val()+text);
            //$(".modifyTitleDiv").find(".newRemainLength").html((60-ModifyTitle.util.countCharacters(newTitle.val()))/2);
            spanObj.qtip({
                content: {
                    text: "已添加至标题尾部哟"
                },
                position: {
                    at: "center left "
                },
                show: {
                    when: false,
                    ready:true
                },
                hide: {
                    delay:1000
                },
                style: {
                    name:'cream'
                }
            });

        }
    },
    flyToTitle : function($this,$targrt){
        var start = {}, end = {};
        start.left = $this.offset().left+"px";
        start.top = $this.offset().top+"px";
        end.left = $targrt.offset().left+"px";
        end.top = $targrt.offset().top+"px";
        ModifyTitle.util.flyFromTo(start,end,$this,null)
    },
    flyFromTo : function(start,end,flyObj,callback){
        //var img = $('<span id="fly-from-to-img" class="inlineblock" style="z-index:200001;position: absolute;top:'+start.top+';left: '+start.left+'"></span>');
        var obj = flyObj.clone();
        obj.css("position","absolute");
        obj.css('left',start.left);
        obj.css('top',start.top);
        obj.appendTo($('body'));

        obj.animate({top:end.top,left:end.left},1500, function(){
            obj.fadeOut(1000);
            callback && callback();
        });
    },
    countCharacters : function(str){
        var totalCount = 0;
        for (var i=0; i<str.length; i++) {
            var c = str.charCodeAt(i);
            if ((c >= 0x0001 && c <= 0x007e) || (0xff60<=c && c<=0xff9f)) {
                totalCount++;
            }else {
                totalCount+=2;
            }
        }
        return totalCount;
    }
}, ModifyTitle.util);


/**
 * 诊断宝贝
 * @type {*}
 */
var Diagnose = Diagnose || {};

Diagnose.init = Diagnose.init || {};

/**
 * 新标题的诊断
 * @type {*}
 */
Diagnose.newTitle = Diagnose.newTitle || {};

Diagnose.common = Diagnose.common || {};

Diagnose.init = $.extend({
    doInit: function(params) {
        var data = {};
        data.numIid = params.itemId;
        var callback = function(diagnoseJson) {
            Diagnose.init.initItemInfo(diagnoseJson);
            Diagnose.init.setEvent(params, diagnoseJson);
        };
        Diagnose.common.doAjax(data, callback);
        //上一个宝贝，下一个宝贝
        ItemJump.init.doInit(params);
        //宝贝属性列表Tab
        ItemProp.init.doInit(params.itemId);

    },
    initItemInfo: function(diagnoseJson) {
        //图片
        $(".itemInfoBlock .itemImg").attr("src", diagnoseJson.picPath);
        //查看宝贝链接
        var url = "http://item.taobao.com/item.htm?id=" + diagnoseJson.numIid;
        $(".itemInfoBlock .goto-item-page").attr("href", url);
        //标题
        $(".modifyBlock .originTitle").html(diagnoseJson.title);
        $(".modifyBlock .newTitle").val(diagnoseJson.title);
        //原来标题的得分
        Diagnose.common.setTitleScore(diagnoseJson.score, false);
    },
    setEvent: function(params, diagnoseJson) {
        //提交标题
        $(".modifyBlock .submitNewTitle").click(function() {
            var newTitle = $(this).parent().find('.newTitle').val();
            if(ModifyTitle.util.countCharacters(newTitle) > 60) {
                alert("您添加的标题超过淘宝字数限制，请删减后再提交");
            } else {
                Diagnose.newTitle.submitNewTitle(params, diagnoseJson.score);
            }
        });
        //诊断标题
        $(".modifyBlock .diagNewTitle").click(function() {
            var newTitle = $(this).parent().find('.newTitle').val();
            if(ModifyTitle.util.countCharacters(newTitle) > 60) {
                alert("您添加的标题超过淘宝字数限制，请删减后再提交");
            } else {
                Diagnose.newTitle.doDiagnose(params, diagnoseJson.score);
            }
        });
        //标题剩余长度
        $(".modifyBlock .newTitle").inputlimitor({
            limit: 60,
            boxId: "remainWordId",
            remText: '剩余字数<span class="newRemainLength">%n</span>',
            limitText: '/ %n'
        });

        $(".modifyBlock .newTitle").keyup();//这样初始化标题剩余长度
    }
}, Diagnose.init);

Diagnose.newTitle = $.extend({
    //提交新标题
    submitNewTitle: function(params, originScore) {
        var title = $(".modifyBlock .newTitle").val();
        if (title == "") {
            alert("请先输入新标题");
            return;
        }
        var data = {};
        data.numIid = params.itemId;
        data.title = title;
        //弹出loading动画
        ModifyTitle.util.showLoading();
        $.ajax({
            url: '/Titles/rename',
            dataType: 'json',
            type: 'post',
            data: data,
            error: function() {
                alert("标题修改失败！");
            },
            success: function (res) {
                if(!res){
                    alert("标题修改失败！请您稍后重试哟!");
                }else if(res.ok){
                    //诊断新标题
                    Diagnose.newTitle.doDiagnose(params, originScore);
                    alert("标题修改成功！");
                }else{
                   alert(res.msg);
                }

                ModifyTitle.util.hideLoading();
            }
        });
    },
    //诊断新标题
    doDiagnose: function(params, originScore) {
        var data = {};
        data.numIid = params.itemId;
        var title = $(".modifyBlock .newTitle").val();
        if (title == "") {
            alert("请先输入新标题");
            return;
        }
        data.title = title;
        var callback = function(diagnoseJson) {
            Diagnose.newTitle.compareTitle(originScore, diagnoseJson.score);

        };
        Diagnose.common.doAjax(data, callback);
    },
    //修改标题后，比较效果
    compareTitle: function(originScore, newScore) {
        var evaluateObj = $(".diagResultDiv .modifyEvaluate");
        if (originScore < newScore) {
            evaluateObj.html("亲，标题得分提高了，效果不错哦。o(∩_∩)o");
        } else if (originScore > newScore) {
            evaluateObj.html("亲，标题得分降低了，再重新改改吧。T__T");
        } else {
            evaluateObj.html("亲，标题修改好像没有起到效果，再重新改改吧。T__T");
        }
    }
}, Diagnose.newTitle);

Diagnose.common = $.extend({
    doAjax: function(data, callback) {
        //弹出loading动画
        ModifyTitle.util.showLoading();
        $.ajax({
            url: '/Titles/singleDiag',
            dataType: 'json',
            type: 'post',
            data: data,
            error: function() {
            },
            success: function (diagnoseJson) {
                //预计搜索词
                //EstimateSearch.init.doInit(diagnoseJson.title);
                var title = diagnoseJson.title;
                var cid = diagnoseJson.cid;
                $('.itemCatBlock .itemCatNameSpan').text(diagnoseJson.catName);
                $.post("/titles/getCWords",{title:title, cid:cid},function(estimateWords){
                    var parseWords = $('.parseWords');
                    parseWords.empty();
                    for(var i = 0; i < estimateWords.length; i++) {
                        parseWords.append(genKeywordSpan.gen({
                            "text":estimateWords[i],
                            "callback":ModifyTitle.util.putIntoTitle,
                            spanClass:'baseblock',
                            enableStyleChange:true,
                            addBtn : false,
                            enableShadow:false
                        }));
                    }
                    parseWords.append("<span class='clearfix'></span>")

                    $('.longTailBlock .baseblock').click(function(){
                        $('#longTailSearchText').val($(this).text());
                        $('#longTailSearchBtn').trigger("click");
                    });
                    $('.recommendWordBlock .baseblock').click(function(){
                        $('#recommendSearchText').val($(this).text());
                        $('#recommendSearchBtn').trigger("click");
                    });
                    $('.busSearchBlock .baseblock').click(function(){
                        $('#busSearchText').val($(this).text());
                        $('#busSearchBtn').trigger("click");
                    });
                    $('.busSearchBlock .baseblock:eq(0)').click();
                    $('.longTailBlock .baseblock:eq(0)').click();
                    $('.recommendWordBlock .baseblock:eq(0)').click();
                });
/*

                $.ajax({
                    on:true,
                    dataType: 'jsonp',
                    url: 'http://chedao.taovgo.com/Commons/getCWord?title='+title + '&cid=' + cid,
                    success:function(estimateWords){
                        var parseCWords = $('.parseCWords');
                        parseCWords.empty();
                        for(var i = 0; i < estimateWords.length; i++) {
                            parseCWords.append(genKeywordSpan.gen({
                                "text":estimateWords[i],
                                "callback":ModifyTitle.util.putIntoTitle,
                                spanClass:'baseblock',
                                enableStyleChange:true,
                                addBtn : false,
                                enableShadow:false
                            }));
                        }
                        parseCWords.append("<span class='clearfix'></span>")

                        $('.CWordsBlock .baseblock').click(function(){
                            $('#CWordsText').val($(this).text());
                            $('#CWordsBtn').trigger("click");
                        });

                        $('.CWordsBlock .baseblock:eq(0)').click();
                    }
                });
*/

                callback(diagnoseJson);
                //新标题得分
                Diagnose.common.setTitleScore(diagnoseJson.score, true);
                //设置标题得分细节Tab
                DiagResult.init.doInit(diagnoseJson);

                ModifyTitle.util.hideLoading();
            }
        });

    },
    setTitleScore: function(score, isNewTitle) {
        var fatherObj = "";
        if (isNewTitle == false)
            fatherObj = $(".originScore");
        else
            fatherObj = $(".newScore");
        //分数
        fatherObj.find(".diagScore").html(score);
        if (score < 60) {
            fatherObj.find(".scoreLevel").html("不及格");
            fatherObj.find(".advice").html("急需改进");
        } else if (score < 70) {
            fatherObj.find(".scoreLevel").html("及格");
            fatherObj.find(".advice").html("仍需改进");
        }else if (score < 85) {
            fatherObj.find(".scoreLevel").html("良好");
            fatherObj.find(".advice").html("表现不错");
        }else {
            fatherObj.find(".scoreLevel").html("优秀");
            fatherObj.find(".advice").html("表现很好");
        }
        // 如果标题已经不错了，则默认显示该宝贝入店关键词
        if(score >= 85) {
            $('.recommendDiv .tabDiv .RecentlySearchWords span').trigger("click");
        }
    }
}, Diagnose.common);



var ItemJump = ItemJump || {};

ItemJump.init = ItemJump.init || {};

ItemJump.init = $.extend({
    doInit: function(params) {
        ModifyTitle.util.showLoading();
        $(".diagnosePrevItem").unbind();
        $(".diagnoseNextItem").unbind();
        var data = {};
        var search = params.search;
        if (search === undefined || search == null || search == "")
            ;
        else
            data.s = params.search;
        data.offset = params.offset;
        data.sort=params.sort;
        data.lowBegin = params.start;
        data.topEnd = params.end;
        data.status = params.status;
        data.catId = params.catId;
        data.optimised = params.optimised;
        if(data.catId==0){
            $.ajax({
                url: '/titles/nearby',
                dataType: 'json',
                type: 'post',
                data: data,
                error: function() {
                },
                success: function (result) {
                    ItemJump.init.setPrevBtn(params, result.after);
                    ItemJump.init.setNextBtn(params, result.before);
                    ModifyTitle.util.hideLoading();
                }
            });
        }   else {
            $.ajax({
                url: '/titles/nearbyCatSearch',
                dataType: 'json',
                type: 'post',
                data: data,
                error: function() {
                },
                success: function (result) {
                    ItemJump.init.setPrevBtn(params, result.after);
                    ItemJump.init.setNextBtn(params, result.before);
                    ModifyTitle.util.hideLoading();
                }
            });
        }

    },
    setPrevBtn: function(params, beforeId) {
        $(".diagnosePrevItem").unbind();
        if (beforeId === undefined || beforeId == null || beforeId == 0 || params.offset < 1) {
            //$(".diagnosePrevItem").removeClass("enableBtn");
            //$(".diagnosePrevItem").addClass("disableBtn");
        } else {
            //$(".diagnosePrevItem").removeClass("disableBtn");
            //$(".diagnosePrevItem").addClass("enableBtn");
            //var newParams = $.extend({}, params);
            //newParams.offset = newParams.offset - 1;
           // newParams.itemId = beforeId;
            /*$(".diagnosePrevItem").click(function() {
                Diagnose.init.doInit(newParams);
            });*/
            //还是用链接好，不然在Diagnose.init.doInit(newParams);中很多按钮事件又被重新加载一次了
            if(params.isKitty){
                ItemJump.init.setKittyHref(beforeId, params.offset - 1, params, $(".diagnosePrevItem"));
            } else {
                ItemJump.init.setHref(beforeId, params.offset - 1, params, $(".diagnosePrevItem"));
            }
        }

    },
    setNextBtn: function(params, nextId) {
        $(".diagnoseNextItem").unbind();
        if (nextId === undefined || nextId == null || nextId == 0) {
            //$(".diagnoseNextItem").removeClass("enableBtn");
            //$(".diagnoseNextItem").addClass("disableBtn");
        } else {
            //$(".diagnoseNextItem").removeClass("disableBtn");
            //$(".diagnoseNextItem").addClass("enableBtn");
            /*var newParams = $.extend({}, params);
            newParams.offset = newParams.offset + 1;
            newParams.itemId = nextId;
            $(".diagnoseNextItem").click(function() {
                Diagnose.init.doInit(newParams);
            });*/
            if(params.isKitty){
                ItemJump.init.setKittyHref(nextId, params.offset + 1, params, $(".diagnoseNextItem"));
            } else {
                ItemJump.init.setHref(nextId, params.offset + 1, params, $(".diagnoseNextItem"));
            }
        }
    },
    setHref: function(itemId, offset, params, btnObj) {
        var href = "";
        href = "/titles/titleop?numIid=" + itemId + "&pn=" + params.currentPage + "&offset=" + offset+
           "&start="+params.start+"&end="+params.end+"&sort="+params.sort+"&status="+params.status + 
            "&catId=" + params.catId + "&optimised=" + params.optimised +"&_tms=" + TM.tms ;
        var search = params.search;
        if (search === undefined || search == null || search == "")
            ;
        else
            href = href  + "&s=" + search;
        btnObj.find("a").attr("href", href);
    },
    setKittyHref : function(itemId, offset, params, btnObj){
        var href = "";
        href = "/KittyTitle/KittyDo?numIid=" + itemId + "&pn=" + params.currentPage + "&offset=" + offset+
            "&start="+params.start+"&end="+params.end+"&sort="+params.sort+"&status="+params.status +
            "&catId=" + params.catId + "&optimised=" + params.optimised +"&_tms=" + TM.tms ;
        var search = params.search;
        if (search === undefined || search == null || search == "")
            ;
        else
            href = href  + "&s=" + search;
        btnObj.find("a").attr("href", href);
    }
}, ItemJump.init);


/**
 * 宝贝的属性
 * @type {*}
 */
var ItemProp = ItemProp || {};

ItemProp.init = ItemProp.init || {};

ItemProp.init = $.extend({
    doInit: function(itemId) {
       /* ModifyTitle.util.showLoading();
        $(".propBlock tbody").html("");
        var data = {};
        data.numIid = itemId;
        $.ajax({
            url: '/items/prop',
            dataType: 'json',
            type: 'post',
            data: data,
            error: function() {
            },
            success: function (propArray) {
                //先把相同的属性放在一起
                var resultArray = ItemProp.init.parseSameKey(propArray);
                $(resultArray).each(function(index, propJson) {
                    var trObj = ItemProp.init.createPropTr(propJson);
                    if (index % 2 == 0)
                        trObj.addClass("evenTr");
                    else
                        trObj.addClass("oddTr");
                    $(".propBlock tbody").append(trObj);
                });
                $(".propBlock tbody").find('.addTextWrapperSmall').click(function(){
                    ModifyTitle.util.putIntoTitle($(this).text(),$(this));
                });
                ModifyTitle.util.hideLoading();
            }
        });*/
        ModifyTitle.util.showLoading();
        $.get("/Titles/props",{numIid:itemId}, function(data){
            var table = $('.propTable');
            if(table.length == 0){
                table = $('<table class="propsTable" style="text-align: center;vertical-align: middle;font-size: 12px;"></table>');
            }
            table.empty();
            /*table.append($('<thead ><th style="width: 200px;">属性列表</th><th style="width: 580px;">属性内容</th></thead>'));
             $(data).each(function(i,word){
             table.append($('<tr style="height: 30px;"><td class="word">'+word.key+'</td><td class="searchMore" style="cursor: pointer;">'+genKeywordSpan.gen({"text":word.value,"callback":autoTitle.util.putIntoTitle,"enableStyleChange":true})+'</td></tr>'));
             });*/
            var trsize = 3;
            var tmpTr;
            $(data).each(function(i,word){
                if(i%trsize == 0){
                    var trObj = $('<tr style="height: 30px;width: 100%;"></tr>');
                    tmpTr = trObj;
                    table.append(tmpTr);
                }
                tmpTr.append($('<td class="word" style="width: 258px;"><span class="inlineblock" style="width: 98px;">'+word.key+':</span>'+genKeywordSpan.gen({"text":word.value,"callback":"","enableStyleChange":true})+'</td>'));
            });
            table.find('span').css("margin","5px 0 5px 0");
            table.find('span').click(function(){
                ModifyTitle.util.putIntoTitle($(this).text(),$(this));
            });

            ModifyTitle.util.hideLoading();
        });
    },
    parseSameKey: function(propArray) {
        var resultArray = [];
        $(propArray).each(function(i, propJson) {
            var key = propJson.key;
            var value = propJson.value;
            for (var j = 0; j < resultArray.length; j++) {
                var tempKey = resultArray[j].key;
                if (key != null && key != "" && key == tempKey) {
                    var tempValue = resultArray[j].value;
                    tempValue += ", " + value;
                    resultArray[j].value = tempValue;
                    return true;
                }
            }
            var newPropJson = $.extend({}, propJson);
            resultArray[resultArray.length] = newPropJson;
        });
        return resultArray;
    },
    createPropTr: function(propJson) {
        var trObj = $("<tr class='propTr'></tr>");
        var keyTd = $("<td class='propKeyTd'></td>");
        keyTd.html(propJson.key);
        var valueTd = $("<td class='propValueTd'></td>");
        valueTd.html(genKeywordSpan.gen({"text":propJson.value,"callback":"","enableStyleChange":true,"spanClass":'addTextWrapperSmall'}));
        trObj.append(keyTd);
        trObj.append(valueTd);
        return trObj;
    },
    hotProps : function(container, numIid){
        container.empty();
        var htmls = [];
        $.get('/items/catProps',{numIid : numIid},function(res){
//            htmls.push('<table><thead><th>属性名</th><th>热销属性词</th></thead><tbody>');
            htmls.push('<table class="oplogs"><thead><th style="width:120px;">属性名</th><th>热销属性词/搜索热度</th></thead><tbody>');
            if(!res || res.length == 0){
                // TODO no res temp...
                htmls.push('<tr><td colspan="2">您这个类目暂无热销属性哟,您也可以联系客服,如果是您发现了升流量的存在的问题,我们会给您奖励一个月的使用时间</td></tr>');
                return;
            }else{

                var pPvMax = 0;
                $.each(res, function(i, prop){
                    if(i == 0){
                        pPvMax = prop.pv;
                    }

                    htmls.push('<tr><td class="greybottom"><div><b>'+prop.pname+'</b></div><div>热度:<b class="red"> '+prop.pv+'</b></div></td>');
                    // values...
                    var maxWidth = 300;
                    htmls.push('<td class="greybottom" > <table width="100%">');
                    var vPvMax = 0;
                    $.each(prop.list, function(j, value){
                        if(j == 0 && vPvMax < value.pv){
                            vPvMax = value.pv;
                        }
                        if(vPvMax < 1 ){
                            vPvMax = 1;
                        }

                        var currWidth = (j==0)?maxWidth:(value.pv*maxWidth/vPvMax);
                        currWidth = Math.round(currWidth);
                        if(currWidth < 5){
                            currWidth = 5;
                        }

                        //                    genKeywordSpan.gen({"text":data[i].word,"callback":autoTitle.util.putIntoTitle,"enableStyleChange":true})
                        htmls.push('<tr><td width="140px"><span class="addTextWrapper shadowbase" style="padding-top:5px;"><img src="/img/btns/addblue.png" >'+value.name+'</span></td><td style="text-align: left;width:300px;"><div style="width:'+
                            currWidth+'px;height:6px;border-top: 6px solid #2D8ABE"></div></td><td>'+value.pv+'</td></tr>');
                    });

                    htmls.push('</table></td></tr>');
                })
            }
            htmls.push('</tbody></table>');
            var res = $(htmls.join(''));
            res.find('.addTextWrapper').click(function(){
                ModifyTitle.util.putIntoTitle($(this).text(),$(this),container);
            });
            container.append(res);
        });

    }
}, ItemProp.init);

/**
 * 最近如电搜索词
 * @type {*}
 */
var RecentlySearchWords = RecentlySearchWords || {};
RecentlySearchWords.init = RecentlySearchWords.init || {};

RecentlySearchWords.init = $.extend({
    doInit : function(params) {
        if((new Date().getTime() - parseInt(TM.firstLoginTime)) < 24 * 3600 * 1000) {
            $('.RecentlySearchWordsTip').show();
        }
        RecentlySearchWords.init.setIntervalEvent(params);
        RecentlySearchWords.init.setWirelessIntervalEvent(params);
    },
    setIntervalEvent : function(params){
        var curr = new Date().getTime();
        var dayMillis = 24 * 3600 * 1000;
        var startTimeInput = $("#startTimeInput");
        var endTimeInput = $("#endTimeInput");
        startTimeInput.datepicker();
        startTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
        startTimeInput.val(new Date(curr - 7 * dayMillis).formatYMS());

        endTimeInput.datepicker();
        endTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
        endTimeInput.val(new Date(curr).formatYMS());

        startTimeInput.unbind('change').change(function(){
            $('.interval-tr .interval[value="0"]').trigger("click");
        });
        endTimeInput.unbind('change').change(function(){
            $('.interval-tr .interval[value="0"]').trigger("click");
        });
        $('.interval-tr .interval').unbind('click').click(function(){
            var endTime, interval;
            var val = parseInt($(this).val());
            switch (val) {
                case 1 :
                    endTime = curr - dayMillis;
                    interval = 1;
                    break;
                case 3 :
                    endTime = curr - dayMillis;
                    interval = 3;
                    break;
                case 7 :
                    endTime = curr - dayMillis;
                    interval = 7;
                    break;
                case 14 :
                    endTime = curr - dayMillis;
                    interval = 14;
                    break;
                case 0 :
                    endTime = parseInt(Date.parse(endTimeInput.val().toString().replace("-","/")));
                    if(endTime > new Date().getTime()) {
                        TM.Alert.load("截止时间请勿超过当前时间");
                        endTimeInput.val(new Date(curr).formatYMS());
                        return;
                    }
                    var startTime = parseInt(Date.parse(startTimeInput.val().toString().replace("-","/")));
                    if(endTime < startTime) {
                        TM.Alert.load("截止时间请勿小于开始时间");
                        return;
                    }
                    interval = Math.floor((endTime - startTime) / dayMillis) + 1;
                    break;
                default :
                    endTime = curr - dayMillis;
                    interval = 7;
                    break;
            }
            RecentlySearchWords.init.itemDiag($('.diag-result-div'), params.itemId, interval, endTime);
        });
        $('.interval-tr .interval:checked').trigger('click');
    },
    itemDiag : function(targetDiv, numIid, interval, endTime) {
        if(interval == null) {
            interval = 7;
        }
        if(endTime == null) {
            endTime = new Date().getTime();
        }
        targetDiv.empty();
        var table = RecentlySearchWords.init.createTableHtml();
        $('.word-diag-paging').tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount: 1,
            ajax: {
                on: true,
                param: {numIid:numIid, interval:interval, endTime:endTime},
                dataType: 'json',
                url: '/Diag/diagItem',
                callback: function(data){
                    if(data === undefined || data == null) {
                        return;
                    }
                    if(data.success == false) {
                        TM.Alert.load(data.message);
                        return;
                    }
                    if(data.res == null){
                        return;
                    }
                    table.find('tbody .word-tr').remove();
                    table.find('tbody .no-data').remove();
                    var totalUv = 0, totalTrade = 0;
                    if(data.res.length > 0) {
                        $(data.res).each(function(i, word){
                            var tranrate = parseInt(word.uv) == 0 ? "0.00%" : new Number(word.alipay_winner_num / word.uv).toPercent(2);
                            var clickrate = parseInt(word.pv) == 0 ? "0.00%" : new Number(word.click / word.pv).toPercent(2);
                            table.find('tbody').append($('<tr class="word-tr"><td>'+word.word+'</td><td>'+word.pv+'</td><td>'+word.click+'</td><td>'+clickrate+'</td><td>'+word.uv+'</td>' +
                                '<td>'+word.alipay_winner_num+'</td><td>'+word.alipay_auction_num+'</td><td>'+new Number(word.alipay_trade_amt).toFixed(2)+'</td><td>'+tranrate+'</td></tr>'))
                        });
                    } else {
                        table.find('tbody').append($('<tr class="no-data" style="height: 45px;"><td colspan="20">此时段暂无数据</td></tr>'));
                    }

                    if(data.msg != null) {
                        var totalAtt = data.msg.split(",");
                        var totalUv = parseInt(totalAtt[0]);
                        var totalTrade = parseInt(totalAtt[1]);
                        $('.search-trade-tranrate').text(totalUv == 0 ? "0%" : new Number(totalTrade / totalUv).toPercent(2));
                    } else {
                        $('.search-trade-tranrate').text("0.00%");
                    }

                }
            }
        });
        targetDiv.append(table);
    },
    setWirelessIntervalEvent: function(params){
        var curr = new Date().getTime();
        var dayMillis = 24 * 3600 * 1000;
        var startTimeInput = $("#startTimeInputWireless");
        var endTimeInput = $("#endTimeInputWireless");
        startTimeInput.datepicker();
        startTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
        startTimeInput.val(new Date(curr - 7 * dayMillis).formatYMS());

        endTimeInput.datepicker();
        endTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
        endTimeInput.val(new Date(curr).formatYMS());

        startTimeInput.unbind('change').change(function(){
            $('.interval-tr-wireless .interval-wireless[value="0"]').trigger("click");
        });
        endTimeInput.unbind('change').change(function(){
            $('.interval-tr-wireless .interval-wireless[value="0"]').trigger("click");
        });
        $('.interval-tr-wireless .interval-wireless').unbind('click').click(function(){
            var endTime, interval;
            var val = parseInt($(this).val());
            switch (val) {
                case 1 :
                    endTime = curr - dayMillis;
                    interval = 1;
                    break;
                case 3 :
                    endTime = curr - dayMillis;
                    interval = 3;
                    break;
                case 7 :
                    endTime = curr - dayMillis;
                    interval = 7;
                    break;
                case 14 :
                    endTime = curr - dayMillis;
                    interval = 14;
                    break;
                case 0 :
                    endTime = parseInt(Date.parse(endTimeInput.val().toString().replace("-","/")));
                    if(endTime > new Date().getTime()) {
                        TM.Alert.load("截止时间请勿超过当前时间");
                        endTimeInput.val(new Date(curr).formatYMS());
                        return;
                    }
                    var startTime = parseInt(Date.parse(startTimeInput.val().toString().replace("-","/")));
                    if(endTime < startTime) {
                        TM.Alert.load("截止时间请勿小于开始时间");
                        return;
                    }
                    interval = Math.floor((endTime - startTime) / dayMillis) + 1;
                    break;
                default :
                    endTime = curr - dayMillis;
                    interval = 7;
                    break;
            }
            RecentlySearchWords.init.itemDiagWireless($('.diag-result-div-wireless'), params.itemId, interval, endTime);
        });
        $('.interval-tr-wireless .interval-wireless:checked').trigger('click');
    },
    itemDiagWireless: function(targetDiv, numIid, interval, endTime){
        if(interval == null) {
            interval = 7;
        }
        if(endTime == null) {
            endTime = new Date().getTime();
        }
        targetDiv.empty();
        var table = RecentlySearchWords.init.createWirelessTableHtml();
        $('.word-diag-paging').tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount: 1,
            ajax: {
                on: true,
                param: {numIid:numIid, interval:interval, endTime:endTime},
                dataType: 'json',
                url: '/Diag/diagAppItem',
                callback: function(data){
                    if(data === undefined || data == null) {
                        return;
                    }
                    if(data.success == false) {
                        TM.Alert.load(data.message);
                        return;
                    }
                    if(data.res == null){
                        return;
                    }
                    table.find('tbody .word-tr').remove();
                    table.find('tbody .no-data').remove();
                    var totalUv = 0, totalTrade = 0;
                    if(data.res.length > 0) {
                        $(data.res).each(function(i, word){
                            var tranrate = parseInt(word.uv) == 0 ? "0.00%" : new Number(word.alipay_winner_num / word.uv).toPercent(2);
                            table.find('tbody').append($('<tr class="word-tr"><td>'+word.word+'</td><td>'+word.pv+'</td><td>'+word.uv+'</td>' +
                            '<td>'+word.alipay_winner_num+'</td><td>'+word.alipay_auction_num+'</td><td>'+new Number(word.alipay_trade_amt).toFixed(2)+'</td><td>'+tranrate+'</td></tr>'))
                        });
                    } else {
                        table.find('tbody').append($('<tr class="no-data" style="height: 45px;"><td colspan="20">此时段暂无数据</td></tr>'));
                    }

                    if(data.msg != null) {
                        var totalAtt = data.msg.split(",");
                        var totalUv = parseInt(totalAtt[0]);
                        var totalTrade = parseInt(totalAtt[1]);
                        $('.search-trade-tranrate').text(totalUv == 0 ? "0%" : new Number(totalTrade / totalUv).toPercent(2));
                    } else {
                        $('.search-trade-tranrate').text("0.00%");
                    }

                }
            }
        });
        targetDiv.append(table);

    },
    createTableHtml : function(){
        var html = '<table class="word-diag-result-table busSearch"><tbody>' +
            '<tr class="word-diag-result-table-th"><td rowspan="2">关键词</td><td colspan="3">展现数据</td><td colspan="4">引流数据</td><td colspan="4">转化数据(直接成交)</td></tr>' +
            '<tr class="word-diag-result-table-th"><td>展现量</td><td>点击量</td><td>点击率</td><td>访客数</td>' +
            '<td>成交用户数</td><td>成交件数</td><td>成交金额</td><td>成交转化率</td></tr>' +
            '</tbody></table>';
        return $(html);
    },
    createWirelessTableHtml : function(){
        var html = '<table class="word-diag-result-table busSearch"><tbody><tr class="word-diag-result-table-th">' +
        '<td rowspan="2">关键词</td>' +
        '<td colspan="2">流量数据</td>' +
        '<td colspan="3">成交数据</td>' +
        '<td colspan="1">转化数据(直接成交)</td></tr>' +
        '<tr class="word-diag-result-table-th">' +
        '<td class="orderTd">浏览量<span class="inlineblock sort Desc" sort="pv"></span></td>' +
        '<td class="orderTd">访客数<span class="inlineblock sort Desc" sort="uv"></span></td>' +
        '<td class="orderTd">成交人数<span class="inlineblock sort Desc" sort="direct_alipay_winner_num"></span></td>' +
        '<td class="orderTd">成交件数<span class="inlineblock sort Desc" sort="direct_alipay_trade_num"></span></td>' +
        '<td class="orderTd">成交金额<span class="inlineblock sort Desc" sort="direct_alipay_trade_amt"></span></td>' +
        '<td>成交转化率</td></tr></tbody></table>';
        return $(html);
    }
},RecentlySearchWords.init);

/**
 * 历史标题
 * @type {*}
 */
var RenameHistory = RenameHistory || {};
RenameHistory.init = RenameHistory.init || {};

RenameHistory.init = $.extend({
    doInit : function(params) {
        $('.rename-history-pagging').tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount:1,
            ajax: {
                on: true,
                dataType: 'json',
                url: "/titles/renameHistory",
                param:{numIid : params.itemId},
                callback:function(data){
                    if(!data) {
                        TM.Alert.load("亲，获取数据出错啦，请重试或联系客服");
                    } else {
                        if(data.res.length > 0) {
                            $('.renameHistoryTable').find('tbody').remove();
                            $('.renameHistoryTable').append(RenameHistory.init.createRenameHistoryTbody(data.res));
                        }

                    }
                }
            }
        });
    },
    createRenameHistoryTbody : function (results) {
        var tbody = $('<tbody></tbody>');
        $(results).each(function(i,result){
            var even = "";
            if(i%2 == 0){
                even = "even";
            }
            tbody.append('<tr class="'+even+'" numIid="'+result.numIid+'">' +
                '<td class="rename-history-oldtitle">'+result.oldTitle+'</td>'+
                '<td class="rename-history-newtitle">'+result.newTitle+'</td>'+
                '<td><span class="set-old-title-back tmbtn yellow-btn">还原</span></td>'+
                '</tr>');
        });
        tbody.find('.set-old-title-back').click(function(){
            var oldTitle = $(this).parent().parent().find('.rename-history-oldtitle').text();
            var numIid = $(this).parent().parent().attr("numIid");
            var data = {};
            data.numIid = numIid;
            data.title = oldTitle;
            //弹出loading动画
            ModifyTitle.util.showLoading();
            $.ajax({
                url: '/Titles/rename',
                dataType: 'json',
                type: 'post',
                data: data,
                error: function() {
                    alert("标题修改失败！");
                },
                success: function (res) {
                    if(!res){
                        alert("标题修改失败！请您稍后重试哟!");
                    }else if(res.ok){
                        //诊断新标题
                        //Diagnose.newTitle.doDiagnose(params, originScore);
                        TM.Alert.loadDetail("亲，标题修改成功，点击确定刷新",400,300,function(){
                            location.reload();
                        })
                    }else{
                        alert(res.msg);
                    }

                    ModifyTitle.util.hideLoading();
                }
            });
        });
        return tbody;
    }
},RenameHistory.init);
/**
 * 促销词
 * @type {*}
 */
var PromoteWord = PromoteWord || {};

PromoteWord.init = PromoteWord.init || {};

PromoteWord.init = $.extend({
    doInit: function() {
        ModifyTitle.util.showLoading();
        $(".promoteBlock").html("");
        var data = {};
        $.ajax({
            url: '/titles/getPromoteWords',
            dataType: 'json',
            type: 'post',
            data: data,
            error: function() {
            },
            success: function (promoteArray) {
                var container = $('.promoteBlock');
                var rowCount = 5;
                var count = 0;
                var html = [];

                html.push('<table class="promoteWordsTable">');
                html.push('<tbody>');
                html.push('<tr>')
                $(promoteArray).each(function(index, promoteWord) {
                    //var spanObj = PromoteWord.init.createWordSpan(promoteWord);
                    var spanObj = genKeywordSpan.gen({"text":promoteWord,"callback":ModifyTitle.util.putIntoTitle,"enableStyleChange":true});
//                    $(".promoteBlock").append(spanObj);
                    if(count++ == rowCount){
                        count = 1;
                        html.push('</tr></tr>');
                    }
                    html.push('<td>'+spanObj+'</td>');
                });
                html.push('</tbody>');
                html.push('</table>');
                var table = $(html.join(''));
                table.find('span').click(function(){
                    ModifyTitle.util.putIntoTitle($(this).text(),$(this));
                });

                $(table).appendTo(container);
                ModifyTitle.util.hideLoading();
            }
        });
    },
    createWordSpan: function(promoteWord) {
        var spanObj = $("<span></span>");
        spanObj.html(promoteWord);
        return spanObj;
    }
}, PromoteWord.init);


/**
 * 当前诊断结果的tab
 * @type {*}
 */
var DiagResult = DiagResult || {};


DiagResult.init = DiagResult.init || {};


//这是从lzl生成的结果来获得的
DiagResult.init = $.extend({
    doInit: function(diagnoseJson) {
        //$(".diagResultBlock .detailResult").addClass("lzlDetailResult");
        $(".diagResultBlock .lzlDetailResult").removeClass("detailResult");
        $(".diagResultBlock .lzlDetailResult").html("");

        var detail = QueryCommodity.commodityDiv.createDetail(diagnoseJson);
        var tableObj = detail.find("table");
        tableObj.css("width", "100%");
        tableObj.find(".td1").css("width", "15%");
        tableObj.find(".td2").css("width", "15%");
        tableObj.find(".td3").css("width", "70%");
        $(".diagResultBlock .lzlDetailResult").append(tableObj);
        $(".diagResultBlock .lzlDetailResult").append("<div class='blank0' style='height: 0px;'></div>");
    }
}, DiagResult.init);

var CWords = CWords || {};
CWords.init = CWords.init || {};
CWords.init = $.extend({
    doInit : function(params,order, sort,s){
        $(".CWordsBlock #CWordsText").keydown(function(event) {
            if (event.keyCode == 13) {//按回车
                $(".CWordsBlock #CWordsBtn").click();
            }
        });
        $(".CWordsBlock #CWordsBtn").click(function() {
            var searchText = $(".CWordsBlock #CWordsText").val();
            if (searchText == "") {
                //alert("请先输入查询条件");
                //return;
            }
            CWords.init.search(params,order, sort,searchText);
        });
        CWords.init.search(params,order, sort,s);
    },
    search : function(params,order, sort, s){
        if(order === undefined || order == ''){
            order = 'pv';
        }
        if(sort === undefined || sort == ''){
            sort = 'desc';
        }
        if(sort === undefined ){
            s = '';
        }
        $('.cwords-pagging').tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount:1,
            ajax: {
                on: true,
                dataType: 'json',
                url: "/words/busSearch",
                param:{numIid:params.itemId,order:order,sort:sort,word:s},
                callback:function(data){
                    $('#CWords-tbody').empty();
                    if(data != null){
                        if(data.res.length > 0) {
                            $(data.res).each(function(i,myword){
                                if(myword.word != "") {
                                    var pv = myword.pv > 0 ? new Number(myword.pv).toTenThousand(2) : "~";
                                    var scount = myword.scount > 0 ? new Number(myword.scount).toTenThousand(2) : "~";
                                    var score = myword.score > 0 ? myword.score : "~";
                                    $('#CWords-tbody').append($('<tr><td class="word-content">'+genKeywordSpan.gen({"text":myword.word,"callback":"","enableStyleChange":true,"spanClass":'addTextWrapperSmall'})+'</td><td>'+pv+'</td><td>'+scount+'</td><td>'+myword.transRate+'</td><td>'+score+'</td><td>'+myword.bidPrice+'</td><td class="add-to-mywords" style="cursor: pointer;"><span class="addTextWrapper tmbtn long-yellow-btn" style="width: 112px;margin: 0;border: 0;padding:0;">添加到词库</span></td></tr>'))
                                }
                            })
                            $('#CWords-tbody').find('tr:even').addClass('even');
                            $(".bussearch tbody").find('.add-to-mywords span').click(function(){
                                $.post('/KeyWords/addMyWord',{word:$(this).parent().parent().find('.word-content').text()},function(data){
                                    TM.Alert.load(data,400, 300, function(){}, "", "添加到词库", 3000);
                                });
                            });
                            $(".bussearch tbody").find('.addTextWrapperSmall').click(function(){
                                ModifyTitle.util.putIntoTitle($(this).text(),$(this));
                            });
                        } else{
                            $('#CWords-tbody').append($('<td colspan="7"><p style="font-size: 16px;text-align: center;font-family: 微软雅黑;">亲，没有搜索到相关的直通车热词呢</p></td>'));
                        }

                    }
                }
            }
        });
    }
},CWords.init);

var BusSearch = BusSearch || {};
BusSearch.init = BusSearch.init || {};
BusSearch.init = $.extend({
    doInit : function(params,order, sort,s){
        $(".busSearchBlock #busSearchText").keydown(function(event) {
            if (event.keyCode == 13) {//按回车
                $(".busSearchBlock #busSearchBtn").click();
            }
        });
        $(".busSearchBlock #busSearchBtn").click(function() {
            var searchText = $(".busSearchBlock #busSearchText").val();
            if (searchText == "") {
                //alert("请先输入查询条件");
                //return;
            }
            BusSearch.init.search(params,order, sort,searchText);
        });
        BusSearch.init.search(params,order, sort,s);
    },
    search : function(params,order, sort, s){
        if(order === undefined || order == ''){
            order = 'pv';
        }
        if(sort === undefined || sort == ''){
            sort = 'desc';
        }
        if(sort === undefined ){
            s = '';
        }
        var tmPagingSizeCookie = "tmPagingSizeCookie";
        $.cookie(tmPagingSizeCookie, 50, {expires: 365, path:'/'});
        $('.bus-search-pagging').tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount:1,
            ajax: {
                on: true,
                dataType: 'json',
                url: "/words/busSearch",
                param:{numIid:params.itemId,order:order,sort:sort,word:s},
                callback:function(data){
                    $('#bus-search-tbody').empty();
                    if(data != null){
                        if(data.res.length > 0) {
                            $(data.res).each(function(i,myword){
                                if(myword.word != "") {
                                    if(parseInt(myword.pv) <= 0) {
                                        return;
                                    }
                                    var pv = parseInt(myword.pv) > 10000 ? new Number(myword.pv).toTenThousand(2) : myword.pv;
                                    var click = myword.click > 10000 ? new Number(myword.click).toTenThousand(2) : (myword.click > 0 ? myword.click : 0);
                                    var scount = myword.scount > 10000 ? new Number(myword.scount).toTenThousand(2) : (myword.scount > 0 ? myword.scount : "~");
                                    var score = parseInt(myword.scount) <= 0 ? 0 : (parseInt(myword.pv) / parseInt(myword.scount)).toFixed(2);
                                    var clickRate = myword.pv > 0 ? new Number(myword.click * 1.0 / myword.pv).toPercent(2) : "~";
                                    $('#bus-search-tbody').append($('<tr><td class="word-content">'+genKeywordSpan.gen({"text":myword.word,"callback":"","enableStyleChange":true,"spanClass":'addTextWrapperSmall'})+'</td><td>'+pv+'</td><td>'+click+'</td><td>'+scount+'</td><td>'+clickRate+'</td><td>'+myword.transRate+'</td><td>'+score+'</td><td>'+myword.bidPrice+'</td><td class="add-to-mywords" style="cursor: pointer;"><span class="addTextWrapper tmbtn long-yellow-btn" style="width: 112px;margin: 0;border: 0;padding:0;">添加到词库</span></td></tr>'))
                                }
                            })
                            $('#bus-search-tbody').find('tr:even').addClass('even');
                            $(".bussearch tbody").find('.add-to-mywords span').click(function(){
                                $.post('/KeyWords/addMyWord',{word:$(this).parent().parent().find('.word-content').text()},function(data){
                                    TM.Alert.load(data,400, 300, function(){}, "", "添加到词库", 3000);
                                });
                            });
                            $(".bussearch tbody").find('.addTextWrapperSmall').click(function(){
                                ModifyTitle.util.putIntoTitle($(this).text(),$(this));
                            });
                        } else{
                            $('#bus-search-tbody').append($('<td colspan="7"><p style="font-size: 16px;text-align: center;font-family: 微软雅黑;">亲，没有搜索到相关的直通车热词呢</p></td>'));
                        }

                    }
                }
            }
        });
    }
},BusSearch.init);

var MyWords = MyWords || {};
MyWords.init = MyWords.init || {};
MyWords.init = $.extend({
    doInit : function(){
        $('.mywords-pagging').tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount:1,
            ajax: {
                on: true,
                dataType: 'json',
                url: "/KeyWords/searchmywords",
                param:{},
                callback:function(data){
                    $('#ML_result').empty();
                    var allwords = "";
                    if(data != null){
                        if(data.res.length > 0) {
                            $(data.res).each(function(i,myword){
                                if(myword.word != "") {
                                    allwords += myword.word + ",";

                                    //$('#ML_result').append($('<tr class="'+even+'"><td class="word-content">'+myword.word+'</td><td>0</td><td>0</td><td>0</td><td>0</td><td class="" style=""><span class="delete-myword btn btn-danger">删除</span> </td></tr>'))
                                }
                            })
                            var isEven = false;
                            $.post('/Words/tmEqual',{words:allwords},function(wordBeans){
                                if(wordBeans != null && wordBeans.length > 0){
                                    var even = "";
                                    if(isEven) {
                                        even = "even";
                                        isEven = false;
                                    } else {
                                        isEven = true;
                                    }
                                    $(wordBeans).each(function(i,wordBean){
                                        var isPvOverTenThousand = parseInt(wordBean.pv) > 10000 ? new Number(wordBean.pv).toTenThousand(2) : wordBean.pv;
                                        var isClickOverTenThousand = parseInt(wordBean.click) > 10000 ? new Number(wordBean.click).toTenThousand(2) : wordBean.click;
                                        var isCompetitionOverTenThousand = parseInt(wordBean.competition) > 10000 ? new Number(wordBean.competition).toTenThousand(2) : wordBean.competition;
                                        $('#ML_result').append($('<tr class="'+even+'"><td class="word-content">'+genKeywordSpan.gen({"text":wordBean.word,"callback":"","enableStyleChange":true,"spanClass":'addTextWrapperSmall'})+'</td><td>'+isPvOverTenThousand+'</td><td>'+isClickOverTenThousand+'</td><td>'+isCompetitionOverTenThousand+'</td><td>'+wordBean.price/100+'</td><td class="" style=""><span class="delete-myword tmbtn red-short-btn">删除</span> </td></tr>'))
                                    });
                                    $("#ML_result").find('.addTextWrapperSmall').click(function(){
                                        ModifyTitle.util.putIntoTitle($(this).text(),$(this));
                                    });
                                    $('#ML_result').find('.delete-myword').click(function(data){
                                        if(confirm("确定要删除该关键词？")){
                                            var $this = $(this);
                                            $.post('/KeyWords/deleteMyWord',{word:$(this).parent().parent().find('.word-content').text()},function(data){
                                                if(data == "删除成功"){
                                                    TM.Alert.load("<p style='text-align: center'>删除成功</p>",400,300,function(){
                                                        $this.parent().parent().fadeOut(1000);
                                                    },false,"删除成功",3000);
                                                } else {
                                                    TM.Alert.load(data);
                                                }
                                            })
                                        }
                                    });
                                }

                            });

                        } else{
                            $('#ML_result').append($('<td colspan="6"><p style="font-size: 16px;text-align: center;font-family: 微软雅黑;">亲，您的词库还没有任何关键词，请去<a style="color: red;" href="/home/topkey">top搜词</a>或<a style="color: red;" href="/home/bustopkey">类目搜词</a>添加</p></td>'));
                        }

                    }
                }
            }
        });
    }
},MyWords.init);
//长尾词
var LongTail = LongTail || {};

LongTail.init = LongTail.init || {};

LongTail.init = $.extend({
    doInit: function(params) {

        var numIid = params.itemId;
        //LongTail.init.search("", numIid);
        $(".longTailBlock #longTailSearchText").keydown(function(event) {
            if (event.keyCode == 13) {//按回车
                $(".longTailBlock #longTailSearchBtn").click();
            }
        });
        $(".longTailBlock #longTailSearchBtn").click(function() {
            var searchText = $(".longTailBlock #longTailSearchText").val();
            if (searchText == "") {
                //alert("请先输入查询条件");
                //return;
            }
            LongTail.init.search(searchText, numIid);
        });

//        $('#longTailWarning').text('亲，请输入您想查询的关键词然后点击搜索哟');
    },
    search: function(searchText, numIid) {

        ModifyTitle.util.showLoading();
        if (searchText === undefined || searchText == null)
            searchText = "";
        $(".longTailTable tbody").html("");
        var data = {};
        data.s = searchText;
        data.numIid = numIid;
        $('.longtail-paging').tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount:1,
            ajax: {
                on: true,
                dataType: 'json',
                url: "/titles/newLongTail",
                param:{s:searchText, numIid:numIid},
                global:false,
                callback:function(longTailArr) {
                    if(longTailArr === undefined || longTailArr == null){
                        var str = (!searchText || searchText.length == 0)?"亲，请输入关键词来查询哟":"亲，找不到相关长尾词，换一个关键词试试。T___T";
                        $(".longTailBlock #longTailWarning").html(str);
                        $(".longTailBlock #longTailWarning").show();
                        $(".longTailTable").hide();
                        return;
                    }
                    LongTail.init.setLongTailTable(longTailArr.res, searchText);
                    ModifyTitle.util.hideLoading();
                }
            }
        })
        /*$.ajax({
            //async : false,
            url : "/titles/newLongTail",
            data : data,
            type : 'post',
            error: function() {
            },
            success: function (longTailArr) {
                LongTail.init.setLongTailTable(longTailArr, searchText);
                ModifyTitle.util.hideLoading();
            }
        });*/
    },
    setLongTailTable: function(longTailArr, searchText) {
        $(".longTailTable tbody").html("");
        if (longTailArr === undefined || longTailArr == null || longTailArr.length == 0) {
            var str = (!searchText || searchText.length == 0)?"亲，请输入关键词来查询哟":"亲，找不到相关长尾词，换一个关键词试试。T___T";
            $(".longTailBlock #longTailWarning").html(str);
            $(".longTailBlock #longTailWarning").show();
            $(".longTailTable").hide();
            return;
        } else {
            $(".longTailBlock #longTailWarning").hide();
            $(".longTailTable").show();
        }
        var index = 0;
        for (var i = 0; i < longTailArr.length; i++) {
            var trObj = $("<tr></tr>");
            var wordTd = $("<td class='wordTd'></td>");
            var searchMoreTd = $("<td style=''></td>");
            var addToMyWordsTd = $('<td class="add-to-mywords" style="cursor: pointer;"><span class="addTextWrapper titleop-add-to-word-library" style="margin: 0;border: 0;padding:0;width: 112px;"></span></td>');
            trObj.append(wordTd);
            var isPvOverTenThousand = Math.abs(longTailArr[i].pv) > 10000 ? new Number(Math.abs(longTailArr[i].pv)).toTenThousand(2) : Math.abs(longTailArr[i].pv);
            var isClickOverTenThousand = Math.abs(longTailArr[i].click) > 10000 ? new Number(Math.abs(longTailArr[i].click)).toTenThousand(2) : Math.abs(longTailArr[i].click);
            var competitionStr = Math.abs(longTailArr[i].competition) > 10000 ? new Number(Math.abs(longTailArr[i].competition)).toTenThousand(2) : Math.abs(longTailArr[i].competition);
            trObj.append($('<td>'+isPvOverTenThousand+'</td>'));
            trObj.append($('<td>'+isClickOverTenThousand+'</td>'))
            trObj.append($('<td>'+competitionStr+'</td>'))
            trObj.append(searchMoreTd);
            trObj.append(addToMyWordsTd);
            wordTd.html(genKeywordSpan.gen({"text":longTailArr[i].word,"callback":"","enableStyleChange":true,"spanClass":'addTextWrapperSmall'}));
            var aObj = $('<span class="addTextWrapper titleop-search-more-btn" style="margin: 0;border: 0;padding: 0;"></span>');
            aObj.attr("longTailValue", longTailArr[i].word);
            aObj.click(function() {
                var longTailValue = $(this).attr("longTailValue");
                $(".longTailBlock #longTailSearchText").val(longTailValue);
                $(".longTailBlock #longTailSearchBtn").click();
            });
            addToMyWordsTd.find('span').click(function(){   alert($(this).parent().parent().find('.wordTd').text())
                $.post('/KeyWords/addMyWord',{word:$(this).parent().parent().find('.wordTd').text()},function(data){
                    TM.Alert.load(data,400, 300, function(){}, "", "添加到词库", 3000);
                });
            });
            searchMoreTd.append(aObj);
            if (index % 2 == 0)
                trObj.addClass("evenTr");
            else
                trObj.addClass("oddTr");
            $(".longTailTable tbody").append(trObj);
            index++;
        }
        $(".longTailTable tbody").find('.addTextWrapperSmall').click(function(){
            ModifyTitle.util.putIntoTitle($(this).text(),$(this));
        });
    }
}, LongTail.init);


//预计搜索词
var EstimateSearch = EstimateSearch || {};

EstimateSearch.init = EstimateSearch.init || {};

EstimateSearch.init = $.extend({
    doInit: function(title) {
        ModifyTitle.util.showLoading();
        if (title === undefined || title == null)
            title = "";
        $(".estimateTable tbody").html("");
        var data = {};
        data.title = title;
        $.ajax({
            //async : false,
            url : "/titles/estimateSearchWord",
            data : data,
            type : 'post',
            error: function() {
            },
            success: function (estimateArr) {
                EstimateSearch.init.setEstimateTable(estimateArr);
                ModifyTitle.util.hideLoading();
            }
        });
    },
    setEstimateTable: function(estimateArr) {
        $(".estimateTable tbody").html("");
        if (estimateArr === undefined || estimateArr == null || estimateArr.length == 0) {
            return;
        }
        var index = 0;
        for (var i = 0; i < estimateArr.length; i++) {
            var trObj = $("<tr></tr>");
            var td1 = $("<td></td>");
            trObj.append(td1);
            td1.html(genKeywordSpan.gen({"text":estimateArr[i],"callback":"","enableStyleChange":true,"spanClass":'addTextWrapperSmall'}));
            if (index % 2 == 0)
                trObj.addClass("evenTr");
            else
                trObj.addClass("oddTr");
            $(".estimateTable tbody").append(trObj);
            index++;
        }
        $(".estimateTable tbody").find('.addTextWrapperSmall').click(function(){
            ModifyTitle.util.putIntoTitle($(this).text(),$(this));
        });
    }
}, EstimateSearch.init);


//火爆标题
var HotTitle = HotTitle || {};

HotTitle.init = HotTitle.init || {};

HotTitle.init = $.extend({
    doInit: function(numIid) {
        ModifyTitle.util.showLoading();
        $(".hotTitleTable tbody").html("");
        var data = {};
        $.ajax({
            //async : false,
            url : "/titles/hotTitles",
            data : data,
            type : 'post',
            error: function() {
            },
            success: function (titleArr) {
                HotTitle.init.setHotTitleTable(titleArr);
                ModifyTitle.util.hideLoading();
            }
        });
    },
    setHotTitleTable: function(titleArr) {
        $(".hotTitleTable tbody").html("");
        if (titleArr === undefined || titleArr == null || titleArr.length == 0) {
            $(".hotTitleBlock #hotTitleWarning").html("亲，找不到相关火爆标题。T___T");
            $(".hotTitleBlock #hotTitleWarning").show();
            $(".hotTitleTable").hide();
            return;
        } else {
            $(".hotTitleBlock #hotTitleWarning").hide();
            $(".hotTitleTable").show();
        }
        var index = 0;
        for (var i = 0; i < titleArr.length; i++) {
            var trObj = $("<tr></tr>");
            var td1 = $("<td></td>");
            trObj.append(td1);
            td1.html(titleArr[i].title);
            if (index % 2 == 0)
                trObj.addClass("evenTr");
            else
                trObj.addClass("oddTr");
            $(".hotTitleTable tbody").append(trObj);
            index++;
        }
    },
    initCompetition : function(container, numIid){
        var pageList = $('<div style="height:30px;text-align: center;" class="pagingArea"></div>');
        container.append(pageList);
        pageList.tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount:1,
            ajax: {
                on: true,
                dataType: 'json',
                url: "/titles/topTitle",
                param:{numIid : numIid},
                callback:function(data){
                    if(!data.isOk){
                        // TODO, no res...
                    }
                    if(container.find('.itemShowTable').length == 0) {
                        var table = $('<table class="itemShowTable"></table>');
                    } else {
                        var table =container.find('.itemShowTable');
                        table.empty();
                    }

                    var htmls = [];
                    htmls.push('<thead><th>宝贝图片</th><th>宝贝标题</th><th style="width:50px;">月销量</th></thead>');
                    $.each(data.res,function(i, item){
                        htmls.push('<tr numIid="'+item.numIid+'">');
                        htmls.push('<td><a target="_blank" href="http://item.taobao.com/item.html?id='+item.numIid+'"><img class="itemsnap" src="'+item.picPath+'" /></a></td>');
                        //htmls.push('<td><div>'+item.title+'</div><div>'+item.splits.join(',')+'</div></td>');
                        htmls.push('<td><div class="topItemTitle">'+item.title+'</div><div class="topItemSplitWords">');
                        for(var i = 0; i < item.splits.length; i++) {
                            htmls.push(genKeywordSpan.gen({"text":item.splits[i],"callback":"","enableStyleChange":true,"spanClass":'addTextWrapperSmall'}));
                        }
                        htmls.push('</div></td>');
                        htmls.push('<td>'+item.sale+'</td>');
                        htmls.push('</tr>');
                    })
                    table.append($(htmls.join('')));
                    table.find('span').click(function(){
                        HotTitle.init.addIntoTitle($(this).text(),$(this),container);
                    });
                    table.find('tr:even').addClass('even');
                    container.append(table);
                }
            }
        });
        container.show();
    },
    addIntoTitle : function(text,spanObj,container){
        var newTitle = $(".modifyTitleDiv").find(".newTitle");
        if(HotTitle.init.countCharacters(newTitle.val()+text) > 60) {
            spanObj.qtip({
                content: {
                    text: "标题长度将超过字数限制，请先删减标题后再添加~"
                },
                position: {
                    at: "center left "
                },
                show: {
                    when: false,
                    ready:true
                },
                hide: {
                    delay:1000
                },
                style: {
                    name:'cream'
                }
            });
        }
        else {
            newTitle.val(newTitle.val()+text);
            newTitle.trigger("keyup");
            $(".modifyTitleDiv").find(".newRemainLength").html(30-newTitle.val().length);
            spanObj.qtip({
                content: {
                    text: "已添加至标题尾部哟"
                },
                position: {
                    at: "center left "
                },
                show: {
                    when: false,
                    ready:true
                },
                hide: {
                    delay:1000
                },
                style: {
                    name:'cream'
                }
            });
        }
    },
    countCharacters : function(str){
        var totalCount = 0;
        for (var i=0; i<str.length; i++) {
            var c = str.charCodeAt(i);
            if ((c >= 0x0001 && c <= 0x007e) || (0xff60<=c && c<=0xff9f)) {
                totalCount++;
            }else {
                totalCount+=2;
            }
        }
        return totalCount;
    }
}, HotTitle.init);

