var TM = TM || {};
((function ($, window) {
    TM.mjsItem = TM.mjsItem || {};

    var mjsItem = TM.mjsItem;

    var mjsSelectedNumIids = "mjsSelectedNumIids";
    var mjsAlreadyNum = "mjsAlreadyNum";

    mjsItem.init = mjsItem.init || {};
    mjsItem.init = $.extend({
        doInit: function(container, activityId, isOnlyAdd) {
            mjsItem.container = container;
            mjsItem.activityId = activityId;
            // 默认修改宝贝
            if(isOnlyAdd === undefined || isOnlyAdd == null) {
                isOnlyAdd = false;
            }
            mjsItem.init.getAlreadyNum(activityId);
            mjsItem.isOnlyAdd = isOnlyAdd;
            mjsItem.Event.setStaticEvent();
            mjsItem.search.initUserCatInfo();
            mjsItem.search.doquanxuan();
            mjsItem.submit.doSub();
        },
        getAlreadyNum : function(activityId){
            $.get("/UmpMjs/getMjsItemNumByActivityId", {activityId: activityId}, function(data){
                $.cookie(mjsAlreadyNum, data.message);
            });
        }
    },mjsItem.init);

    var selectItems =[];

    mjsItem.search =mjsItem.search || {};
    mjsItem.search= $.extend({
        initUserCatInfo : function(){
            $.get("/items/sellerCatCount",function(data){
                var sellerCat = $('#sellerCat');
                sellerCat.empty();
                if(!data || data.length == 0){
                    sellerCat.hide();
                }
                var exist = false;
                var cat = $('<li><a href="javascript:void(0);">所有类目</a></li>');
                cat.click(function(){
                    $('.fS-text').html('<a href="javascript:void(0);">所有类目</a>');
                    mjsItem.search.doShow(1);
                });
                sellerCat.append(cat);
                for(var i=0;i<data.length;i++) {
                    if(data[i].count <= 0){
                        continue;
                    }
                    exist = true;
                    var li_option = $('<li></li>');
                    var option = $('<a href="javascript:void(0);"></a>');
                    option.attr("catId",data[i].id);
                    option.html(data[i].name+"("+data[i].count+")");
                    option.click(function(){
                        $('.fS-text').html($(this).parent().html());
                        mjsItem.search.doShow(1);
                    });
                    li_option.append(option);
                    sellerCat.append(li_option);

                }
            });
            $.get("/items/onSaleItemCatCount",function(data){
                var sellerCat = $('#itemCat');
                sellerCat.empty();
                if(!data || data.length == 0){
                    sellerCat.hide();
                }

                var exist = false;
                var cat = $('<li><a href="javascript:void(0);">所有类目</a></li>');
                cat.click(function(){
                    $('.fI-text').html('<a href="javascript:void(0);">所有类目</a>');
                    mjsItem.search.doShow(1);
                });
                sellerCat.append(cat);
                for(var i=0;i<data.length;i++) {
                    if(data[i].count <= 0){
                        continue;
                    }

                    exist = true;
                    var li_option = $('<li></li>');
                    var option = $('<a href="javascript:void(0);"></a>');
                    option.attr("catId",data[i].id);
                    option.html(data[i].name+"("+data[i].count+")");
                    option.click(function(){
                        $('.fI-text').html($(this).parent().html());
                        mjsItem.search.doShow(1);
                    });
                    li_option.append(option);
                    sellerCat.append(li_option);
                }
            });
        },
        doShow: function(currentPage) {
            var data={};
            data.title=$('.guanjianci').val();
            data.cid=$(".fI-text a").attr("catid");
            data.sellerCid= $(".fS-text a").attr("catid");
            data.order = $(".fR-text").attr("order");
            data.isDis=  $(".fD-text").attr("isDis");
            data.isOnsale=  $(".fO-text").attr("isOnsale");
            data.tmProActivityId = mjsItem.activityId;
            mjsItem.util.clearCookie();
            if (currentPage < 1)
                currentPage = 1;
            var tbodyObj = mjsItem.container.find(".item-table");
            mjsItem.container.find(".paging-div").tmpage({
                currPage: currentPage,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: data,
                    dataType: 'json',
                    url: '/TaoDiscount/searchMjsItemsWithParams',
                    callback: function(dataJson){
                        var tbody = $('.item-table table tbody');
                        tbody.find('tr.mjsitemTr').remove();
                        if(dataJson === undefined || dataJson == null) {
                            tbody.find('tr.no-data').show();
                            return;
                        }
                        if(dataJson.res == null || dataJson.res.length <= 0) {
                            tbody.find('tr.no-data').show();
                            return;
                        }
                        tbody.find('tr.no-data').hide();
                        $(dataJson.res).each(function(i, item){
                            var isAdded = mjsItem.util.isAddedToActBtn(item);
                            item.isAddedToActBtn = isAdded;

                            if(isAdded == true && !mjsItem.isOnlyAdd) {
                                mjsItem.util.addSelectedNumIidToCookie(item.numiid);
                            }
/*
                            var trObj;
                            if(i % 2 == 0) {
                                trObj = $('<tr class="itemTr"></tr>');
                                trObj.append($('#mjsItemTd').tmpl(item));
                                tmpTr = trObj;
                                if(i == dataJson.res.length - 1) {
                                    tbody.append(trObj);
                                }
                            } else {
                                trObj = tmpTr;
                                trObj.append($('#mjsItemTd').tmpl(item));
                                tbody.append(trObj);
                            }*/

                            tbody.append($('#mjsItemTrTmpl').tmpl(item));
                        });
                        mjsItem.Event.setItemTableEvent(tbody);
                    }
                }
            });
        } ,
        doquanxuan: function(){
            $(".unchoose").hide();
            $('.choose').click(function(){
                $(".choose").hide();
                $(".unchoose").show();
                $(".mjsitemTr").each(function(i,val){
                    $(this).find('.addToActBtn').hide();
                    $(this).find('.addedToActBtn').show();
                    mjsItem.util.addSelectedNumIidToCookie($(this).attr('numIid'));
                })
            });
            $('.unchoose').click(function(){
                $(".unchoose").hide();
                $(".choose").show();
                $(".mjsitemTr").each(function(i,val){
                    $(this).find('.addedToActBtn').hide();
                    $(this).find('.addToActBtn').show();
                    mjsItem.util.deleteSelectedNumIidToCookie($(this).attr('numIid'));
                })
            });
        }
    },mjsItem.search);

    mjsItem.Event = mjsItem.Event || {};
    mjsItem.Event = $.extend({
        setStaticEvent : function(){
            $('.guanjianci').keyup(function() {
                var lable_key=$('.guanjianci').val();
                if(!lable_key){
                    $('.combobox-label-item').show();
                }
                else{
                    $('.combobox-label-item').hide();
                }
            });
            $('.fRange').mousemove(function(){
                $('.fR-list').show();
            });
            $('.fRange').mouseout(function(){
                $('.fR-list').hide();
            });
            $('.fRl-ico-pu').click(function(){
                $('.fR-text').html("↑ 价格从低到高");
                $('.fR-text').attr("order","pu");
                mjsItem.search.doShow(1);
            });
            $('.fRl-ico-pd').click(function(){
                $('.fR-text').html("↓ 价格从高到低");
                $('.fR-text').attr("order","pd");
                mjsItem.search.doShow(1);
            });
            $('.fRl-ico-su').click(function(){
                $('.fR-text').html("↑ 下架时间");
                $('.fR-text').attr("order","su");
                mjsItem.search.doShow(1);
            });
            $('.fRl-ico-sd').click(function(){
                $('.fR-text').html("↓ 下架时间");
                $('.fR-text').attr("order","sd");
                mjsItem.search.doShow(1);
            });
            $('.fRl-ico-df').click(function(){
                $('.fR-text').html("默认排序");
                $('.fR-text').attr("order","df");
                mjsItem.search.doShow(1);
            });

            $('.fSellercat').mousemove(function(){
                $('.fS-list').show();
            });
            $('.fSellercat').mouseout(function(){
                $('.fS-list').hide();
            });

            $('.fItemcat').mousemove(function(){
                $('.fI-list').show();
            });
            $('.fItemcat').mouseout(function(){
                $('.fI-list').hide();
            });

            $('.fDiscount').mousemove(function(){
                $('.fD-list').show();
            });
            $('.fDiscount').mouseout(function(){
                $('.fD-list').hide();
            });
            $('.fD-ico-dis').click(function(){
                $('.fD-text').html("已参加活动");
                $('.fD-text').attr("isDis","dis");
                mjsItem.search.doShow(1);
            });
            $('.fD-ico-undis').click(function(){
                $('.fD-text').html("未参加活动");
                $('.fD-text').attr("isDis","undis");
                mjsItem.search.doShow(1);
            });
            $('.fD-ico-all').click(function(){
                $('.fD-text').html("所有宝贝");
                $('.fD-text').attr("isDis","all");
                mjsItem.search.doShow(1);
            });

            $('.fOnsale').mousemove(function(){
                $('.fO-list').show();
            });
            $('.fOnsale').mouseout(function(){
                $('.fO-list').hide();
            });
            $('.fO-ico-dis').click(function(){
                $('.fO-text').html("出售中");
                $('.fO-text').attr("isOnsale","onsale");
                mjsItem.search.doShow(1);
            });
            $('.fO-ico-undis').click(function(){
                $('.fO-text').html("仓库中");
                $('.fO-text').attr("isOnsale","instock");
                mjsItem.search.doShow(1);
            });
            $('.fO-ico-all').click(function(){
                $('.fO-text').html("所有宝贝");
                $('.fO-text').attr("isOnsale","all");
                mjsItem.search.doShow(1);
            });

            $('.guanjianci-select').click(function(){
                mjsItem.search.doShow(1);
            })  ;

            //错误提示
            $('.orangeBtn').click(function(){
                $('#hraBox').hide();
            });
            $('.closeModal').click(function(){
                $('#settingActModal').hide();
            });

            mjsItem.search.doShow(1);
        },
        setItemTableEvent : function(tbody){
            tbody.find('.addToActBtn').unbind('click')
                .click(function(){
                    $(this).hide();
                    $(this).parent().find('.addedToActBtn').show();
                    var numIid = $(this).parent().parent().attr('numIid');
                    var numIidStr = $.cookie(mjsSelectedNumIids);
                    if(numIidStr === undefined || numIidStr == null) {
                        numIidStr = numIid + ",";
                        $.cookie("mjsSelectedNumIids", numIidStr);
                    } else {
                        if(numIidStr.indexOf(numIid) < 0) {
                            numIidStr += numIid + ",";
                            $.cookie(mjsSelectedNumIids, numIidStr);
                        }
                    }
                });
            tbody.find('.addedToActBtn').unbind('click')
                .click(function(){
                    $(this).hide();
                    $(this).parent().find('.addToActBtn').show();
                    var numIid = $(this).parent().parent().attr('numIid');
                    var numIidStr = $.cookie(mjsSelectedNumIids);
                    if(numIidStr === undefined || numIidStr == null) {
                        numIidStr = "";
                        $.cookie(mjsSelectedNumIids, numIidStr);
                    } else {
                        if(numIidStr.indexOf(numIid) >= 0) {
                            numIidStr = numIidStr.replace(numIid + ",", "");
                            $.cookie(mjsSelectedNumIids, numIidStr);
                        }
                    }
                });
        }
    }, mjsItem.Event) ;

    mjsItem.util = mjsItem.util || {};
    mjsItem.util = $.extend({
        clearCookie : function(){
            $.cookie(mjsSelectedNumIids, "");
        },
        isAddedToActBtn : function(item) {
            if(item === undefined || item == null) {
                return false;
            }
            if(item.isthisActivity == true) {
                return true;
            }
            var numIid = item.numiid;
            if(numIid <= 0) {
                return false;
            }
            var selectedNumIidStr = $.cookie(mjsSelectedNumIids);
            if(selectedNumIidStr === undefined || selectedNumIidStr == null || selectedNumIidStr == "") {
                return false;
            }
            if(selectedNumIidStr.indexOf(numIid + ",") >= 0) {
                return true;
            }
            return false;
        },
        addSelectedNumIidToCookie : function(numIid){
            if(numIid === undefined || numIid == null) {
                return;
            }
            if(numIid <= 0) {
                return;
            }
            var selectedNumIidStr = $.cookie(mjsSelectedNumIids);
            if(selectedNumIidStr === undefined || selectedNumIidStr == null || selectedNumIidStr == "") {
                $.cookie(mjsSelectedNumIids, numIid + ",");
                return;
            }
            if(selectedNumIidStr.indexOf(numIid + ",") >= 0) {
                return;
            }
            selectedNumIidStr += numIid + ",";
            $.cookie(mjsSelectedNumIids, selectedNumIidStr);
        },
        deleteSelectedNumIidToCookie : function(numIid){
            if(numIid === undefined || numIid == null) {
                return;
            }
            if(numIid <= 0) {
                return;
            }
            var selectedNumIidStr = $.cookie(mjsSelectedNumIids);
            if(selectedNumIidStr === undefined || selectedNumIidStr == null || selectedNumIidStr == "") {
                return;
            }
            if(selectedNumIidStr.indexOf(numIid + ",") < 0) {
                return;
            }
            selectedNumIidStr = selectedNumIidStr.replace(numIid + ",", "");
            $.cookie(mjsSelectedNumIids, selectedNumIidStr);
        }
    }, mjsItem.util) ;

    mjsItem.submit = mjsItem.submit || {};
    mjsItem.submit = $.extend({
        doSub:function(){
            $('.StepBtn2').click(function(){

                var selectedNumIidStr = $.cookie(mjsSelectedNumIids);
                if(selectedNumIidStr === undefined || selectedNumIidStr == null || selectedNumIidStr == ""){
                    TM.Alert.load("亲，请选择至少一个宝贝加入活动！！");
                    return false;
                }
                if(mjsItem.isOnlyAdd == true) {
                    if(selectedNumIidStr.split(",").length - 1 + parseInt($.cookie(mjsAlreadyNum)) > 150) {
                        TM.Alert.load("一个活动最多只能添加150个宝贝，建议亲创建全店满就送活动~");
                        return;
                    }
                    $.ajax({
                        url : '/UmpMjs/addMjsItems',
                        data : {tmActivityId: mjsItem.activityId, numIids: selectedNumIidStr},
                        type : 'post',
                        success : function(data) {
                            if(data === undefined || data == null) {
                                TM.Alert.load("宝贝添加出错，请重试或联系客服");
                                return;
                            }
                            if(data.success == false) {
                                if(data.taobaoAuthorizeUrl === undefined || data.taobaoAuthorizeUrl == null || data.taobaoAuthorizeUrl == "") {
                                    TM.Alert.load("创建活动失败，请重试或联系客服");
                                    return;
                                }
                                // 需要重新授权

                                TM.UmpUtil.util.showAuthDialog("/umppromotion/newReShouquan", null);
                            } else {
                                TM.Alert.load("成功添加宝贝" + data.res.successNum + "个，失败" + data.res.errorList.length + "个", 400, 300, function(){
                                    window.location.href = "/taodiscount/index";
                                });

                            }


                        }
                    });
                } else {
                    if(selectedNumIidStr.split(",").length - 1 > 150) {
                        TM.Alert.load("一个活动最多只能添加150个宝贝，建议亲创建全店满就送活动~");
                        return;
                    }
                    $.ajax({
                        url : '/UmpMjs/updateMjsItems',
                        data : {tmActivityId: mjsItem.activityId, numIids: selectedNumIidStr},
                        type : 'post',
                        success : function(data) {
                            if(data === undefined || data == null) {
                                TM.Alert.load("宝贝添加出错，请重试或联系客服");
                                return;
                            }
                            if(data.success == false) {
                                if(data.taobaoAuthorizeUrl === undefined || data.taobaoAuthorizeUrl == null || data.taobaoAuthorizeUrl == "") {
                                    TM.Alert.load("创建活动失败，请重试或联系客服");
                                    return;
                                }
                                // 需要重新授权

                                TM.UmpUtil.util.showAuthDialog("/umppromotion/newReShouquan", null);
                            } else {
                                TM.Alert.load("成功添加宝贝" + data.res.successNum + "个，失败" + data.res.errorList.length + "个", 400, 300, function(){
                                    window.location.href = "/taodiscount/index";
                                });

                            }


                        }
                    });
                }

            })

        },
        subString : function(){
            var subString="";
            mjsItem.row.setArrayString();
            var activityString=$('.activityString').val();
//            var arrayact= activityString.split(",");
//            if(arrayact[5]==1){
//                distype=1;
//            }
            for (var i=0; i<selectItems.length;i++){
                if(selectItems[i].discountType==0){
                    subString += activityString+","+selectItems[i].id+","+selectItems[i].discount+","+selectItems[i].discountType+"!";
                }
                else{
                    subString += activityString+","+selectItems[i].id+","+selectItems[i].jianjia+","+selectItems[i].discountType+"!";
                }
            }
            return subString;
        } ,
        checkDiscount : function(input) {
            var val = $.trim(input.val());
            if(isNaN(val) || val <= 0 || val >= 10) {
                var text = '折扣应是大于0.01小于10的数字';
                input.addClass("error");
            }
            else {
                input.removeClass("error");
            }
        }
    }, mjsItem.submit) ;
})(jQuery,window));
