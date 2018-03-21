((function ($, window) {
var Popularize = Popularize || {};
var autoTitle = TM.autoTitle;
TM.Popularize = Popularize;

Popularize.Init = Popularize.Init || {};
Popularize.Init = $.extend({
    init : function(){

        $.ajax({
            url : '/popularize/getUserInfo',
            data : {},
            type : 'post',
            success : function(dataJson) {
                var userJson = dataJson;
                if (userJson.level <= -1) {
                    Popularize.isTryVersion = true;//体验版
                    var remainNum = userJson.remainNum;
                    if (userJson.award) {
                        remainNum++;
                    }
                    Popularize.remainPopularizeNum = remainNum;
                    Popularize.isPopularizedOn = userJson.isPopularOn;
                } else {
                    Popularize.isTryVersion = false;
                }

                Popularize.Init.initUserInfo();
                autoTitle.Init.initSearchArea();
                Popularize.popularize.getItems();
                //Popularize.Init.xufei();
            }
        });


    },
    initPopularizedOrNot : function(){

    },
    xufei : function(level){
        if(level != null) {
            $.getScript("/Status/js",function(data){
                if(parseInt(TM.timeLeft) < 15) {
                    $('body').append(Popularize.util.createXufei(level,TM.timeLeft));
                }
            });
        }

    },
    initUserInfo : function(){
        $.get("/Popularize/getUserInfo",function(data){
            $.cookie("awarded",data.award);
            if(data.award){
                $.cookie("totalNum",data.totalNum+1);
            } else {
                $.cookie("totalNum",data.totalNum);
            }
            $.cookie("level",data.level);
            Popularize.Init.xufei($.cookie("level"));
            $.cookie("popularizedNum",data.popularizedNum);

            $('.userInfo').find('span[tag="version"]').html(data.version);
            $('.userInfo').find('span[tag="totalNum"]').html($.cookie("totalNum"));
            if(data.award){
                $('.userInfo').find('span[tag="totalNum"]').parent().append($('<span class="userData" tag="awardNum">(+1)</span>'));
            }
            $('.userInfo').find('span[tag="popularizedNum"]').html($.cookie("popularizedNum"));
            $('.userInfo').find('span[tag="remainNum"]').html($.cookie("totalNum") - $.cookie("popularizedNum"));
        })
    }
},Popularize.Init);

Popularize.util = Popularize.util || {};
Popularize.util = $.extend({
    createXufei : function(level, timeLeft){
        if(level <= 1) {
            return $('<div class="xufei inlineblock ie6fixedBR" style="display: block;">亲,您的服务将在<span style="color: red;font-size: 32px;">'+timeLeft+'</span>天后到期，请尽快续订哦!!' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/m8pBNiy" target="_blank">12个月/<span style="font-size: 30px;color: white;">40</span>元</a></span>' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/GBpBNiy" target="_blank">6个月/<span style="font-size: 30px;color: white;">20</span>元</a></span>' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/C1pBNiy" target="_blank">3个月/<span style="font-size: 30px;color: white;">10</span>元</a></span>' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/ngpBNiy" target="_blank">1个月/<span style="font-size: 30px;color: white;">5</span>元</a></span>' +
                '</div>');
        } else if(level <= 10) {
            return $('<div class="xufei inlineblock ie6fixedBR" style="display: block;">亲,您的服务将在<span style="color: red;font-size: 32px;">'+timeLeft+'</span>天后到期，请尽快续订哦!!' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/kCnBNiy" target="_blank">12个月/<span style="font-size: 30px;color: white;">160</span>元</a></span>' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/LGnBNiy" target="_blank">6个月/<span style="font-size: 30px;color: white;">80</span>元</a></span>' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/1ylBNiy" target="_blank">3个月/<span style="font-size: 30px;color: white;">40</span>元</a></span>' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/TznBNiy" target="_blank">1个月/<span style="font-size: 30px;color: white;">20</span>元</a></span>' +
                '</div>');
        } else if(level <= 20) {
            return $('<div class="xufei inlineblock ie6fixedBR" style="display: block;">亲,您的服务将在<span style="color: red;font-size: 32px;">'+timeLeft+'</span>天后到期，请尽快续订哦!!' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/OZYBNiy" target="_blank">12个月/<span style="font-size: 30px;color: white;">280</span>元</a></span>' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/NhmBNiy" target="_blank">6个月/<span style="font-size: 30px;color: white;">140</span>元</a></span>' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/z3aBNiy" target="_blank">3个月/<span style="font-size: 30px;color: white;">70</span>元</a></span>' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/l6bBNiy" target="_blank">1个月/<span style="font-size: 30px;color: white;">35</span>元</a></span>' +
                '</div>');
        } else if(level <= 30) {
            return $('<div class="xufei inlineblock ie6fixedBR" style="display: block;">亲,您的服务将在<span style="color: red;font-size: 32px;">'+timeLeft+'</span>天后到期，请尽快续订哦!!' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/p4TBNiy" target="_blank">12个月/<span style="font-size: 30px;color: white;">480</span>元</a></span>' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/4eTBNiy" target="_blank">6个月/<span style="font-size: 30px;color: white;">240</span>元</a></span>' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/ZsVBNiy" target="_blank">3个月/<span style="font-size: 30px;color: white;">120</span>元</a></span>' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/euWBNiy" target="_blank">1个月/<span style="font-size: 30px;color: white;">60</span>元</a></span>' +
                '</div>');
        } else if(level <= 40) {
            return $('<div class="xufei inlineblock ie6fixedBR" style="display: block;">亲,您的服务将在<span style="color: red;font-size: 32px;">'+timeLeft+'</span>天后到期，请尽快续订哦!!' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/3TQBNiy" target="_blank">12个月/<span style="font-size: 30px;color: white;">800</span>元</a></span>' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/QwQBNiy" target="_blank">6个月/<span style="font-size: 30px;color: white;">400</span>元</a></span>' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/G6QBNiy" target="_blank">3个月/<span style="font-size: 30px;color: white;">240</span>元</a></span>' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/xnRBNiy" target="_blank">1个月/<span style="font-size: 30px;color: white;">120</span>元</a></span>' +
                '</div>');
        } else if(level <= 50) {
            return $('<div class="xufei inlineblock ie6fixedBR" style="display: block;">亲,您的服务将在<span style="color: red;font-size: 32px;">'+timeLeft+'</span>天后到期，请尽快续订哦!!' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/OhMBNiy" target="_blank">12个月/<span style="font-size: 30px;color: white;">1000</span>元</a></span>' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/GANBNiy" target="_blank">6个月/<span style="font-size: 30px;color: white;">500</span>元</a></span>' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/jsNBNiy" target="_blank">3个月/<span style="font-size: 30px;color: white;">300</span>元</a></span>' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/oxNBNiy" target="_blank">1个月/<span style="font-size: 30px;color: white;">150</span>元</a></span>' +
                '</div>');
        } else {
            return $('<div class="xufei inlineblock ie6fixedBR" style="display: block;">亲,您的服务将在<span style="color: red;font-size: 32px;">'+timeLeft+'</span>天后到期，请尽快续订哦!!' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/wFKBNiy" target="_blank">12个月/<span style="font-size: 30px;color: white;">2000</span>元</a></span>' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/PdJBNiy" target="_blank">6个月/<span style="font-size: 30px;color: white;">1000</span>元</a></span>' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/DTLBNiy" target="_blank">3个月/<span style="font-size: 30px;color: white;">500</span>元</a></span>' +
                '<span class="inlineblock xudingBtn"><a href="http://to.taobao.com/ZYLBNiy" target="_blank">1个月/<span style="font-size: 30px;color: white;">300</span>元</a></span>' +
                '</div>');
        }
    },
    getParams : function(){
        var params = {};
        var status = $("#itemsStatus option:selected").attr("tag");
        switch(status){
            case "polularized":params.polularized=0;break;
            case "topopularize" : params.polularized=1;break;
            default : params.polularized=2;break;
        }

        var catId = $('#itemsCat option:selected').attr("catId");
        params.catId = catId;

        var sort = $('#itemsSortBy option:selected').attr("tag");
        switch(sort){
            case "sortBySaleCountUp" : params.sort=3;break;
            case "sortBySaleCountDown" : params.sort=4;break;
            default : params.sort=3;break;
        }

        params.lowBegin = $('#lowScore').val();
        params.topEnd = $('#highScore').val();
        params.s = $('#searchWord').val();
        return params;
    }
},Popularize.util);


Popularize.popularize = Popularize.popularize || {};
Popularize.popularize = $.extend({
    getItems : function(){
        $('.itemsList').empty();
        var params = $.extend({
            "s":"",
            "polularized":2,
            "catId":null,
            "sort":3,
            "lowBegin":0,
            "topEnd":100
        },Popularize.util.getParams());

        params.popularizeStatus = 5;

        var bottom = $('<div class="popularizeBottom" style="text-align: center;"></div>');
        bottom.tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount:1,
            ajax: {
                param : params,
                on: true,
                dataType: 'json',
                url: "/Popularize/searchItems",
                callback:function(data){
                    $('.itemsList').empty();
                    $('.itemsList').find(".popularizeBottom").remove();
                    $('.itemsList').append(Popularize.popularize.createItemsHead());
                    $('.itemsList').append(Popularize.popularize.createItemsList(data.res));
                    bottom.appendTo($('.itemsList'));
                    Popularize.Event.setEvent();
                }
            }

        });

    },
    createItemsHead : function(){
        var head = $('<div class="diagHead"></div>');
        var ulObj = $('<ul class="diagHeadUL"></ul>');

        var liObjSelect = $('<li class=" fl" style="width:40px;s" ></li>');
        liObjSelect.append($('<input type="checkbox" id="checkAllItems" style="margin-top:15px"  >'));
        ulObj.append(liObjSelect);
        ulObj.append($('<li class="fl" style="width:120px;text-align: center;"><span class="batchSelectUsePopularize commbutton btntext6">选中使用自动推广</span></li>'));
        ulObj.append($('<li class="fl" style="width:20px"></li>'));
        ulObj.append($('<li class="fl" style="width:120px;text-align: center;"><span class="batchSelectRemovePopularize commbutton btntext6">选中取消自动推广</span></li>'));
        ulObj.append($('<li class="fl" style="width:200px"></li>'));
        ulObj.append($('<li class="fl" style="width:120px"><span class="batchAllAddPopularize commbutton btntext6">全店一键自动推广</span></li>'));
        ulObj.append($('<li class="fl" style="width:20px"></li>'));
        ulObj.append($('<li class="fl" style="width:120px"><span class="batchAllRemovePopularize commbutton btntext6">全店一键取消推广</span></li>'));

        //体验版，不能删除，不能修改
        if (Popularize.isTryVersion == true) {
            if (Popularize.remainPopularizeNum <= 0) {
                ulObj.find(".batchSelectUsePopularize").remove();
                ulObj.find(".batchAllAddPopularize").remove();
            }
            ulObj.find(".batchSelectRemovePopularize").remove();
            ulObj.find(".batchAllRemovePopularize").parents("li").remove();
            ulObj.append($('<li class="fl" style="width:120px"><span class="batchAllPausePopularize commbutton btntext6">全店一键暂停推广</span></li>'));

            if (Popularize.isPopularizedOn == true) {
                ulObj.find(".batchAllPausePopularize").html("全店一键暂停推广");
            } else {
                ulObj.find(".batchAllPausePopularize").html("全店一键开启推广");
            }

            ulObj.find(".batchAllPausePopularize").click(function() {
                var btnObj = $(this);
                var data = {};
                if (Popularize.isPopularizedOn == true) {
                    $.ajax({
                        url : '/popularize/setPopularOff',
                        data : data,
                        type : 'post',
                        success : function(dataJson) {
                            alert("全店一键开启推广");
                        }
                    });
                    Popularize.isPopularizedOn = false;
                    btnObj.html("全店开启推广");
                } else {
                    $.ajax({
                        url : '/popularize/setPopularOn',
                        data : data,
                        type : 'post',
                        success : function(dataJson) {
                            alert("全店开启推广成功");
                        }
                    });
                    Popularize.isPopularizedOn = true;
                    btnObj.html("全店一键暂停推广");
                }
            });



        }

        head.append(ulObj);



        return head;
    },
    createItemsList : function(res){
        var items = $('<div class="itemsDiv" ></div>');
        for (var i = 0; i < res.length; i++) {
            items.append(Popularize.popularize.createSingleItem(res[i],i));
        }
        return items;
    },
    createSingleItem : function(result,i){
        var singleItem = $('<div class="singleItem"></div>');
        var ulObj = $('<ul class="singleDiagULObj"></ul>');
        ulObj.attr("numIid",result.numIid);
        ulObj.append(autoTitle.ItemsDiag.createCheckboxLiObj(result));
        ulObj.append(autoTitle.ItemsDiag.createItemImgLiObj(result));
        ulObj.append(Popularize.popularize.createItemTitleLiObj(result,i));
        ulObj.append(Popularize.popularize.createOPLiObj(result));
//        ulObj.append(autoTitle.ItemsDiag.createStatusLiObj(result));
//        ulObj.append(autoTitle.ItemsDiag.createSalesCountLiObj(result));

        singleItem.append(ulObj);
        singleItem.append($('<div class="clear"></div>'));
        return singleItem;
    },
    createItemTitleLiObj : function(result, i){
        var liObj = $('<li class=" fl" style="width:460px;margin: 37px 0 0 0;" ></li>');
        liObj.append($('<span class="itemTitle twelve">'+result.title+'</span>'));
        return liObj;
    },
    createOPLiObj : function(result){
        var isOn = result.popularized;

        var liObj = $('<li class=" fl" style="width:150px;text-align: center;" ></li>');
        var line = $("<div class='switchStatusLine' ></div>");
        var switchLine = TM.Switch.createSwitch.createSwitchForm("");
        switchLine.find('input[name="auto_valuation"]').tzCheckbox(
            {
                labels:['已推广','未推广'],
                doChange:function(isCurrentOn){


                    if(isCurrentOn == false) {
                        if(($.cookie("totalNum") - $.cookie("popularizedNum")) > 0){

                            var postData = {numIids : result.id};

                            if (Popularize.isTryVersion == true) {//体验版
                                postData.status = 4;
                                if (confirm("亲，体验版的推广位开启后不能更换宝贝，您确定要推广该宝贝吗？") == false)
                                    return;
                            }
                            $.post("/Popularize/addPopularized", postData, function(){
                                $.cookie("popularizedNum",parseInt($.cookie("popularizedNum")) + 1);
                                $('.userData[tag="popularizedNum"]').html($.cookie("popularizedNum"));
                                $('.userData[tag="remainNum"]').html($.cookie("totalNum")-$.cookie("popularizedNum"));
                                TM.Alert.load("宝贝已添加到推广计划中~",400,200);
                            });
                            liObj.find('.gotoSeePopularited').show();
                        } else {
                            TM.Alert.load("您需要<a target='_blank' href='http://fuwu.taobao.com/ser/detail.htm?service_code=FW_GOODS-1845420&tracelog=upgrade' style='color: blue;font-size: 16px;font-weight: bold;'>升级版本</a>才能继续添加推广宝贝~",400,200);
                            return false;
                        }

                    }
                    else {
                        var postData = {numIids : result.id};
                        postData.status = 5;

                        $.post("/Popularize/removePopularized", postData, function(){
                            $.cookie("popularizedNum",parseInt($.cookie("popularizedNum")) - 1);
                            $('.userData[tag="popularizedNum"]').html($.cookie("popularizedNum"));
                            $('.userData[tag="remainNum"]').html($.cookie("totalNum")-$.cookie("popularizedNum"));
                            TM.Alert.load("宝贝已从推广计划中取消~",400,200);
                        });
                        liObj.find('.gotoSeePopularited').hide();
                    }
                    return true;
                },
                isOn : isOn
            });
        switchLine.appendTo(line);
        line.appendTo(liObj);
        liObj.append($('<div class="gotoSeePopularited"><span style="margin:0 0 0 50px;cursor: pointer;"><a href="/share?numIid='+result.id+'" target="_blank" class="lookupPopularized">查看推广</a></span></div>'));
        if(!isOn) {
            liObj.find('.gotoSeePopularited').hide();
        }
        return liObj;
    }
},Popularize.popularize);

Popularize.Event = Popularize.Event || {};
Popularize.Event = $.extend({
    setEvent : function(){
        Popularize.Event.setSelectChangeEvent();
        Popularize.Event.setCheckAllEvent();
        Popularize.Event.setSubcheckEvent();
        Popularize.Event.setGOSearchEvent();
        Popularize.Event.setSelectAddBatch();
        Popularize.Event.setAllAddBatch();
        Popularize.Event.setSelectRemoveBatch();
        Popularize.Event.setAllRemoveBatch();
        Popularize.Event.setGotoSeePopularitedEvent();
    },
    setSelectChangeEvent : function(){
        $('.searchArea').find('select').unbind("change");
        $('.searchArea').find('select').change(function(){
           $('#goSearchItems').trigger("click");
        });
    },
    setCheckAllEvent : function(){
        $("#checkAllItems").unbind("click");
        $("#checkAllItems").click(function(){
            $('input[name="subCheck"]').attr("checked",this.checked);
        });
    },
    setSubcheckEvent : function(){
        $(".subCheckBox").click(function(){
            var $subBox = $("input[name='subCheck']");
            $("#checkAllItems").attr("checked",$subBox.length == $("input[name='subCheck']:checked").length ? true : false);
        });
    },
    setGOSearchEvent : function(){
        $('#goSearchItems').unbind("click");
        $('#goSearchItems').click(function(){
            Popularize.popularize.getItems();
        });
    },
    setSelectAddBatch : function(){
        $('.batchSelectUsePopularize').unbind("click");
        $('.batchSelectUsePopularize').click(function(){
            var numIids = "";
            if($(".itemsList").find(".subCheckBox:checked").length == 0) {
                TM.Alert.load("请选择宝贝~",400,230);
            }
            else if($(".itemsList").find(".subCheckBox:checked").length > ($.cookie("totalNum")- $.cookie("popularizedNum"))){
                TM.Alert.load("您选择的宝贝数大于可推荐宝贝数~",400,230);
            } else {
                $.each($(".itemsList").find(".subCheckBox:checked"),function(i, input){
                    var oThis = $(input);
                    numIids += (oThis.attr('numIid')) + ",";
                });

                var postData = {numIids : numIids};
                if (Popularize.isTryVersion == true) {//体验版
                    postData.status = 4;
                    if (confirm("亲，体验版的推广位开启后不能更换宝贝，您确定要推广该宝贝吗？") == false)
                        return;
                }

                $.post("/Popularize/addPopularized", postData, function(){
                    TM.Alert.load("宝贝已添加到推广计划中~",400,200,function(){
                        location.reload();
                    });
                });
            }
        });
    },
    setSelectRemoveBatch : function(){
        $('.batchSelectRemovePopularize').unbind("click");
        $('.batchSelectRemovePopularize').click(function(){
            var numIids = "";
            if($(".itemsList").find(".subCheckBox:checked").length == 0) {
                TM.Alert.load("请选择宝贝~",400,230);
            } else {
                $.each($(".itemsList").find(".subCheckBox:checked"),function(i, input){
                    var oThis = $(input);
                    numIids += (oThis.attr('numIid')) + ",";
                });

                var postData = {numIids : numIids};
                postData.status = 5;
                $.post("/Popularize/removePopularized", postData, function(){
                    TM.Alert.load("宝贝已从推广计划中取消~",400,200,function(){
                        location.reload();
                    });
                });
            }
        });
    },
    setAllAddBatch : function(){
        $('.batchAllAddPopularize').unbind("click");
        $('.batchAllAddPopularize').click(function(){

            var postData = {};
            if (Popularize.isTryVersion == true) {//体验版
                postData.status = 4;
                if (confirm("亲，体验版的推广位开启后不能更换宝贝，您确定要全店推广吗？") == false)
                    return;
            }

            $.get("/Popularize/addPopularizedAll", postData, function(data){
                if(data.res){
                    TM.Alert.load("宝贝已添加到推广计划中~",400,200,function(){
                        location.reload();
                    });
                }else{
                    TM.Alert.load("您的全店宝贝数大于可推荐宝贝数，请先升级哦~",400,230);
                }
            });
        });
    },
    setAllRemoveBatch : function(){
        $('.batchAllRemovePopularize').unbind("click");
        $('.batchAllRemovePopularize').click(function(){
            var postData = {};
            postData.status = 5;
            $.get("/Popularize/removePopularizedAll", postData, function(data){
                TM.Alert.load("宝贝已从推广计划中取消~",400,200,function(){
                    location.reload();
                });
            });
        });
    },
    setGotoSeePopularitedEvent : function(){
        $('.gotoSeePopularited').click(function(){
            // todo
        });
    }
},Popularize.Event);
})(jQuery, window));