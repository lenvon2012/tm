var TM = TM || {};
((function ($, window) {
    TM.DaZhe = TM.DaZhe || {};
    var DaZhe = TM.DaZhe;
    var me = TM.DaZhe;
    me.members = me.members || {};
    me.ProMode = {
        Simple : 1,
        Mjs : 2,
        ShopMjs : 3
    };

    me.currProMode = me.ProMode.Simple;

    me.ProAction = me.ProAction || {};
    function ProAction(params){
        this.doStep2 = params.doStep2;
    }
    TM.DXPromote = new ProAction({
        doStep2 : function(enString){
            window.location.href="/TaoDiscount/itemSelect?activityString="+enString;
        }
    });

    TM.UMPSinglePromote = new ProAction({
        doStep2 : function(enString){
            window.location.href="/TaoDiscount/itemSelect?activityString="+enString;
        }
    });

    TM.MjsPromote = new ProAction({
        doStep2 : function(enString){
            window.location.href="/TaoDiscount/itemSelect?activityString="+enString;
        }
    });
    TM.MjsParams = {

        activityName : "",

        start: 0,

        end: 0,

        participageRange: 1,

        isAmountOver : true,

        isAmountMultiple : false,// 上不封顶,400 减20, 800减 40

        totalPrice : 0, // 满多少钱...

        isItemCountOver : false, // 满多少件

        isItemMultiple : false, // 上不封顶

        itemCount : 0, // 多少件

        isShopMember: false,

        shopMemberLevel: 0,

        isUserTag : false,

        userTagStr : '',

        isDecrease : false,

        decreaseValue : 0,

        isDiscount: false,

        discountValue: 0,

        isSentGift: false,

        giftName: "",

        giftId: 0,

        giftUrl: '',

        isFreePost: false,

        excludedAreas : '',

        excludedCodes : '',

        toDisplay : '',

        toServerParams : function(){
            return $.extend({}, this, {toDisplay:null,toServerParams:null});
        },

        buildDisplay : function(){
            var htmls = '';

            if(this.isAmountOver){
                htmls += '单笔订单满 <strong>'+this.totalPrice/100 +'</strong>元, ';
            }else if(this.isItemCountOver){
                htmls += '单笔订单满 <strong>'+this.itemCount +'</strong>件, ';
            }
            if(this.isDiscount){
                htmls +='&nbsp;打'+(this.discountValue > 0 ? this.discountValue/100:'&nbsp;')+'折';
            }else if(this.isDecrease){
                htmls='&nbsp;减'+(this.decreaseValue > 0 ? this.decreaseValue/100:'&nbsp;')+'元';
            }

            if(this.isFreePost){

                var checkedAreas = $('.baoyouAreaSelector .areaName input').find('input:checked');
                var noCheckedAreas = $('.baoyouAreaSelector .areaName input').not('input:checked');

                var checkedNum = checkedAreas.length;

                var noCheckedNum =noCheckedAreas.length;

                var areaNum = checkedNum + noCheckedNum;
                if(areaNum == checkedNum){
                    // all included nothing need to do..
                    htmls += '&nbsp; 包邮';
                }else if(areaNum == 0){
                    // No areas are included...
                    htmls += '&nbsp; 包邮(仅限地球外)';
                }else if(checkedNum > (areaNum/2)){
                    // More than half areas are included.., show the not included areas...
                    var arr = [];
                    noCheckedAreas.each(function(i,elem){arr.push($(elem).val())});
                    htmls += '&nbsp; 包邮(除 '+arr.join(',')+'外) ';
                }else {
                    // direct show the little included baoyou areas..
                    // More than half areas are included.., show the not included areas...
                    var arr = [];
                    checkedAreas.each(function(i,elem){arr.push($(elem).val())});
                    htmls += '&nbsp; 包邮(仅限 '+arr.join(',')+')';
                }
            }
            var commentInput = $('#comment');
            var commentValue = commentInput.val().trim();
            if(commentInput.attr('default')==commentValue){
                // So, no new comment is typed...
            }else{
                htmls += '<br/> '+commentValue;
            }

            return $.extend({}, this, {toDisplay:htmls,toServerParams:null,
                startDateStr:new Date(this.start).formatYMS(),endDateStr:new Date(this.end).formatYMS()});

        },
        createOne : function(){
            return $.extend({}, this, { startDateStr:'2014.04.05',endDateStr:'2014.04.10'});
        }
    };


    DaZhe.Init = DaZhe.Init || {};
    DaZhe.Init = $.extend({
        init : function(mode, isOnlyUpdateMjsActivity, activityId){
            mode = DaZhe.util.setCurrentmode(mode);
            if(isOnlyUpdateMjsActivity ===undefined || isOnlyUpdateMjsActivity == null) {
                isOnlyUpdateMjsActivity = false;
            }
            me.currProMode = mode;
            var nowTime=new Date();
            var month=nowTime.getMonth()+1;
            var def_name=month+"月";
            if(mode == me.ProMode.Mjs) {
                def_name += "满就送";
            } else if(mode == me.ProMode.ShopMjs) {
                def_name += "全店满就送";
            } else {
                def_name += "促销";
            }
            DaZhe.Init.doMembers();

            $('#name').attr("value",def_name) ;
            $('#start_time').attr("value",  DaZhe.Init.parseLongToDate(nowTime.getTime()));
            $('#end_time').attr("value",  DaZhe.Init.parseLongToDate(nowTime.getTime()+30*1000*60*60*24));
            $('#end_time').parent().find('.append').html("促销活动结束时间，当前活动将持续 30 天");


            DaZhe.Init.DoTimePicker();
            DaZhe.Init.docheck();
            DaZhe.Init.GoStep2(mode, isOnlyUpdateMjsActivity, activityId);
            if(mode == me.ProMode.Mjs || mode == me.ProMode.ShopMjs) {
                DaZhe.Event.setMjsEvent(mode);
                // 活动选择的满就送模板
                me.mainForm.find('.promoteTemplate .uCheckbox[index="1"]').trigger("click")
            }
        } ,
        restartByMjsActivityId: function(isShop, activityId){
            if(isShop ===undefined || isShop == null) {
                isShop = false;
            }
            if(activityId === undefined || activityId == null || activityId <= 0) {
                TM.Alert.load("activityId参数错误");
                return;
            }
            DaZhe.Init.doMembers();

            DaZhe.Event.setMjsEvent();

            $.get("/UmpMjs/getTMProActivityById", {activityId: activityId}, function(data){
                if(data === undefined || data == null) {
                    TM.Alert.load("获取活动信息出错");
                }
                if(data.success == false) {
                    TM.Alert.load("获取活动信息出错");
                }

                DaZhe.Init.initMjsActivity(data, true);


            });

            DaZhe.Init.DoTimePicker();
            DaZhe.Init.docheck();
            DaZhe.Init.restartMjsActivityGoStep2(isShop, activityId);

        },
        initByMjsActivityId: function(mode, isOnlyUpdateMjsActivity, activityId){
            mode = DaZhe.util.setCurrentmode(mode);
            if(isOnlyUpdateMjsActivity ===undefined || isOnlyUpdateMjsActivity == null) {
                isOnlyUpdateMjsActivity = false;
            }
            me.currProMode = mode;
            if(activityId === undefined || activityId == null || activityId <= 0) {
                TM.Alert.load("activityId参数错误");
                return;
            }
            DaZhe.Init.doMembers();
            if(mode == me.ProMode.Mjs || mode == me.ProMode.ShopMjs) {
                DaZhe.Event.setMjsEvent(mode);
            }

            $.get("/UmpMjs/getTMProActivityById", {activityId: activityId}, function(data){
                if(data === undefined || data == null) {
                    TM.Alert.load("获取活动信息出错");
                }
                if(data.success == false) {
                    TM.Alert.load("获取活动信息出错");
                }

                DaZhe.Init.initMjsActivity(data);


            });

            DaZhe.Init.DoTimePicker();
            DaZhe.Init.docheck();
            DaZhe.Init.GoStep2(mode, isOnlyUpdateMjsActivity, activityId);

        },
        initMjsActivity: function(activity, isRestart){
            if(isRestart === undefined || isRestart == null) {
                isRestart = false;
            }
            var dayMills = 3600 * 1000 * 24;

            // 初始化活动信息
            $('#name').attr("value",activity.activityDescription) ;
            if(isRestart) {
                var nowTime = new Date();
                $('#start_time').attr("value",  DaZhe.Init.parseLongToDate(nowTime.getTime()));
                $('#end_time').attr("value",  DaZhe.Init.parseLongToDate(nowTime.getTime() + 30 * dayMills));
            } else {
                $('#start_time').attr("value",  DaZhe.Init.parseLongToDate(activity.activityStartTime));
                $('#end_time').attr("value",  DaZhe.Init.parseLongToDate(activity.activityEndTime));
            }

            $('#end_time').parent().find('.append').html("促销活动结束时间，当前活动将持续 "+Math.floor((activity.activityEndTime - activity.activityStartTime) / dayMills) + " 天");
            $('#comment').val(activity.remark);
            // 设置活动标签
            if(activity.activityTitle == "满就送") {
                $('#promotion_title option[tag="manjiusong"]').attr("selected", true);
            } else if(activity.activityTitle == "满就包邮") {
                $('#promotion_title option[tag="manjiubaoyou"]').attr("selected", true);
            } else {
                $('.title-select').hide();
                $('.self-put').hide();
                $('.title-input').show();
                $('.select-put').show();
                $('.title-input').addClass("promotion_title") ;
                $('.title-select').removeClass("promotion_title") ;
                $('.promotion_title').val(activity.activityTitle);
            }

            // 初始化满就送参数信息
            // 满件还是满元
            var mjsParams = eval("(" + activity.mjsParams + ")");
            me.mainForm.find('span[name="mjsType"]').removeClass("selected");
            me.mainForm.find('span[name="mjsType"]').addClass("unselected");
            if(mjsParams.isAmountOver == true) {
                me.mainForm.find('#manMoney').parent().parent().removeClass("unselected");
                me.mainForm.find('#manMoney').parent().parent().addClass("selected");
                me.mainForm.find('#manMoney').val(mjsParams.totalPrice / 100);
            } else {
                me.mainForm.find('#itemcountinput').parent().parent().removeClass("unselected");
                me.mainForm.find('#itemcountinput').parent().parent().addClass("selected");
                me.mainForm.find('#itemcountinput').val(mjsParams.itemCount);
            }

            // 设置是减价还是打折
            if(mjsParams.isDecrease == true) {
                $('#decreaseNum').trigger('click');
                $('#decreaseNum').val(mjsParams.decreaseValue / 100);
                // 是否上不封顶
                if(mjsParams.isAmountMultiple == true) {
                    me.mainForm.find('.decreaseMultipleOrNot .uCheckbox').removeClass("unselected");
                    me.mainForm.find('.decreaseMultipleOrNot .uCheckbox').addClass("selected");
                } else {
                    me.mainForm.find('.decreaseMultipleOrNot .uCheckbox').removeClass("selected");
                    me.mainForm.find('.decreaseMultipleOrNot .uCheckbox').addClass("unselected");
                }
            } else if(mjsParams.isDiscount == true){
                $('#disCountNum').trigger("click");
                $('#disCountNum').val(mjsParams.discountValue / 100);
                $('.decreaseMultipleOrNot').hide();
            } else {
                $('.decreaseMultipleOrNot').hide();
            }

            // 是否包邮
            if(mjsParams.isFreePost == true) {
                $('span[name="baoyou"]').trigger("click");
                $('.setArea').show();
                var excludedCodes = mjsParams.excludedCodes;
                if(excludedCodes === undefined || excludedCodes == null || excludedCodes == "") {
                    $('.baoyouAreaSelector .selectAllAreas').trigger("click");
                } else {
                    var excludedIds = excludedCodes.split("*");
                    if(excludedIds.length == 0) {
                        $('.baoyouAreaSelector .selectAllAreas').trigger("click");
                    } else {
                        // 先全选
                        $('.baoyouAreaSelector .selectAllAreas').trigger("click");
                        // 再去除排除地区
                        $(excludedIds).each(function(i, excludeId){
                            $('.baoyouAreaSelector .areatext[areaId="'+excludeId+'"]').trigger("click");
                        });
                    }
                }
            }

            // 是否送小礼物
            if(mjsParams.isSentGift == true) {
                $('span[name="gift"] span.clicktarget').trigger('click');
                $('.little-gift-name').val(mjsParams.giftName);
            }

            // 活动选择的满就送模板
            me.mainForm.find('.promoteTemplate .uCheckbox[index="'+activity.mjsTmplIndex+'"]').trigger("click")

        },
        doMembers : function(){
            me.mainForm = $('.mainForm');
            var mem = me.members;
            mem.mjsTmplHolder = me.mainForm.find('div[tag="HNMjsTmplHolder"]');
            mem.startTimeInput = $('#start_time');
            mem.endTimeInput = $('#end_time');
            mem.formMjsParams  =function(){
                var param = DaZhe.util.getMjsParams();
                param.startDateStr = mem.startTimeInput.val();
                param.endDateStr = mem.endTimeInput.val();
                param.start = new Date(param.startDateStr).getTime();
                param.end = new Date(param.endDateStr).getTime();
                param.activityName = me.mainForm.find('#name').val();

                /*param.isDecrease = me.mainForm.find('#manMoney').parent().parent().hasClass("selected");
                 param.isDiscount = !param.isDecrease;
                 param.*/
                return param;
            }
        },
        DoTimePicker : function(){
            $("#start_time").datetimepicker({


                onClose: function(input, inst) {
                    var mindate = $('#start_time').datetimepicker('getDate');
                    if(!mindate) mindate=new Date();
                    var enddate = new Date($("#end_time").val());
                    if(!enddate) enddate=new Date();
                    $('#end_time').datetimepicker('destroy');
                    $('#end_time').datetimepicker({

                        minDate: mindate,
                        onClose: function(input, inst) {
                            DaZhe.Init.updateTimeDiff();
                            DaZhe.Init.checkTime() ;
                        }
                    });
                    var day = mindate.getTime()-mindate.getTime()%(1000*60*60*24)+30*1000*60*60*24-(1000*60*60*6) + Math.floor(Math.random()*60*60*4*1000);
                    mindate.setTime(day);
                    if( mindate.getTime() > enddate.getTime() ) {
                        $('#end_time').datetimepicker('setDate', mindate);
                    }
                    DaZhe.Init.updateTimeDiff();
                    DaZhe.Init.checkTime();
                }
            }) ;

            $('#end_time').datetimepicker({

                onClose: function(input, inst) {
                    DaZhe.Init.updateTimeDiff();
                    DaZhe.Init.checkTime();
                }
            });
        }   ,
        restartMjsActivityGoStep2 : function(isShop, activityId){
            $('.StepBtn1').click(function(){
                /* if($('.mjsContentMustChooseOne.selected').length == 0) {
                 TM.Alert.load("请至少选择减价,打折,包邮中的一种");
                 return;
                 }*/
                $('.promoteTemplate span.selected').trigger("click");
                DaZhe.Init.checkName();
                DaZhe.Init.checkTime();
                DaZhe.Init.checkTitle();

                var StartTime=$('#start_time').val();
                var EndTime=$('#end_time').val();
                var name=$('#name').val();
                var title=$('.promotion_title').val();
                var decrease_num=$('#decrease_num').val();
                var promotionType="1";
                var limitTime= StartTime+10*24*60*60*1000;
                //var NowTime =new Date().getTime();  alert(NowTime)

                if($(".error").length > 0) {
                    alert("请先修正错误再提交");
                    setTimeout(function() {
                        $("input.error").effect('bounce', {times: 3, distance: 30}, 1500);
                    }, 300);
                    return false;
                }

                var activityString="";

                activityString +=name+","+StartTime+","+EndTime+","+title+","+promotionType+","+decrease_num;

                var enString=encodeURI(activityString) ;

                if(isShop == false) {
                    var mjsParams = DaZhe.util.getMjsParamsWithTime(StartTime, EndTime, name);
                    var tmplHtml = $('#mjsTmplHolderWrapper').html();
                    var remark = me.mainForm.find('#comment').val();
                    if(me.mainForm.find('.title-input:visible').length == 0) {
                        mjsTitle = $('#promotion_title option:selected').text();
                    } else {
                        mjsTitle = $('.promotion_title').val();
                    }
                    if(mjsTitle === undefined || mjsTitle == null || mjsTitle == "") {
                        TM.Alert.load("请输入活动标签");
                        return;
                    }
                    $.post("/TaoDiscount/restartMjsActivity", {startTimeStr: StartTime, endTimeStr: EndTime, title: mjsTitle, description: name, mjsParamStr: DaZhe.util.O2String(mjsParams),
                        tmplHtml: tmplHtml, remark: remark, activityId: activityId}, function(data){
                        if(data === undefined || data == null) {
                            TM.Alert.load("创建活动失败，请重试或联系客服");
                            return;
                        }
                        if(data.success == false) {
                            if(data.taobaoAuthorizeUrl === undefined || data.taobaoAuthorizeUrl == null || data.taobaoAuthorizeUrl == "") {
                                TM.Alert.load(data.message);
                                return;
                            }
                            // 需要重新授权

                            TM.UmpUtil.util.showAuthDialog("/umppromotion/newReShouquan", null);
                        } else {
                            var activityId = data.message;
                            window.location.href="/TaoDiscount/mjsItemSelect?activityId=" + activityId;
                        }

                    });
                } else {
                    var mjsParams = DaZhe.util.getMjsParamsWithTime(StartTime, EndTime, name);
                    var tmplHtml = $('#mjsTmplHolderWrapper').html();
                    var remark = me.mainForm.find('#comment').val();
                    if(me.mainForm.find('.title-input:visible').length == 0) {
                        mjsTitle = $('#promotion_title option:selected').text();
                    } else {
                        mjsTitle = $('.promotion_title').val();
                    }
                    if(mjsTitle === undefined || mjsTitle == null || mjsTitle == "") {
                        TM.Alert.load("请输入活动标签");
                        return;
                    }
                    $.post("/UmpMjs/restartShopMjsActivity", {startTimeStr: StartTime, endTimeStr: EndTime, title: mjsTitle, description: name, mjsParamStr: DaZhe.util.O2String(mjsParams),
                        tmplHtml: tmplHtml, remark:remark, activityId: activityId}, function(data){
                        if(data === undefined || data == null) {
                            TM.Alert.load("创建活动失败，请重试或联系客服");
                            return;
                        }
                        if(data.success == false) {
                            if(data.taobaoAuthorizeUrl === undefined || data.taobaoAuthorizeUrl == null || data.taobaoAuthorizeUrl == "") {
                                TM.Alert.load(data.message);
                                return;
                            }
                            // 需要重新授权

                            TM.UmpUtil.util.showAuthDialog("/umppromotion/newReShouquan", null);
                        } else {
                            TM.Alert.load("活动创建成功", 400, 300, function(){
                                location.href = "/Sales/index";
                            });
                        }

                    });
                }


            })
        },
        GoStep2 : function(mode, isOnlyUpdateMjsActivity, activityId){
            if(mode === undefined || mode == null) {
                mode = me.ProMode.Simple;
            }
            if(mode != me.ProMode.Simple && mode != me.ProMode.Mjs && mode != me.ProMode.ShopMjs) {
                mode = me.ProMode.Simple;
            }
            $('.StepBtn1').click(function(){
                $('.promoteTemplate span.selected').trigger("click");
                DaZhe.Init.checkName();
                DaZhe.Init.checkTime();
                DaZhe.Init.checkTitle();

                var StartTime=$('#start_time').val();
                var EndTime=$('#end_time').val();
                var name=$('#name').val();
                var title=$('.promotion_title').val();
                var decrease_num=$('#decrease_num').val();
                var promotionType="1";
                var limitTime= StartTime+10*24*60*60*1000;
                //var NowTime =new Date().getTime();  alert(NowTime)

                if($(".error").length > 0) {
                    alert("请先修正错误再提交");
                    setTimeout(function() {
                        $("input.error").effect('bounce', {times: 3, distance: 30}, 1500);
                    }, 300);
                    return false;
                }

                var activityString="";

                activityString +=name+","+StartTime+","+EndTime+","+title+","+promotionType+","+decrease_num;

                var enString=encodeURI(activityString) ;

                if(mode == me.ProMode.Simple) {
                    window.location.href="/TaoDiscount/itemSelect?activityString="+enString;
                } else if(mode == me.ProMode.Mjs) {
                    /*if($('.mjsContentMustChooseOne.selected').length == 0) {
                     TM.Alert.load("请至少选择减价,打折,包邮中的一种");
                     return;
                     }*/
                    // 如果是创建活动
                    if(isOnlyUpdateMjsActivity == false) {
                        var mjsParams = DaZhe.util.getMjsParamsWithTime(StartTime, EndTime, name);
                        var tmplHtml = $('#mjsTmplHolderWrapper').html();
                        var remark = me.mainForm.find('#comment').val();
                        if(me.mainForm.find('.title-input:visible').length == 0) {
                            mjsTitle = $('#promotion_title option:selected').text();
                        } else {
                            mjsTitle = $('.promotion_title').val();
                        }
                        if(mjsTitle === undefined || mjsTitle == null || mjsTitle == "") {
                            TM.Alert.load("请输入活动标签");
                            return;
                        }
                        if(mjsParams.isSentGift == true) {
                            if(mjsParams.giftName === undefined || mjsParams.giftName == "" || mjsParams.giftName == "请输入小礼物名称") {
                                TM.Alert.load("请输入小礼物名称");
                                return;
                            }
                        }
                        $.post("/TaoDiscount/addMjsActivity", {startTimeStr: StartTime, endTimeStr: EndTime, title: mjsTitle, description: name, mjsParamStr: DaZhe.util.O2String(mjsParams),
                            tmplHtml: tmplHtml, remark: remark}, function(data){
                            if(data === undefined || data == null) {
                                TM.Alert.load("创建活动失败，请重试或联系客服");
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
                                var activityId = data.message;
                                window.location.href="/TaoDiscount/mjsItemSelect?activityId=" + activityId;
                            }

                        });
                    }
                    // 如果是修改满就送活动
                    else {
                        var mjsParams = DaZhe.util.getMjsParamsWithTime(StartTime, EndTime, name);
                        var tmplHtml = $('#mjsTmplHolderWrapper').html();
                        var remark = me.mainForm.find('#comment').val();
                        var mjsTitle = "";
                        if(me.mainForm.find('.title-input:visible').length == 0) {
                            mjsTitle = $('#promotion_title option:selected').text();
                        } else {
                            mjsTitle = $('.promotion_title').val();
                        }
                        if(mjsTitle === undefined || mjsTitle == null || mjsTitle == "") {
                            TM.Alert.load("请输入活动标签");
                            return;
                        }
                        if(mjsParams.isSentGift == true) {
                            if(mjsParams.giftName === undefined || mjsParams.giftName == "" || mjsParams.giftName == "请输入小礼物名称") {
                                TM.Alert.load("请输入小礼物名称");
                                return;
                            }
                        }
                        $.post("/TaoDiscount/updateMjsActivity", {startTimeStr: StartTime, endTimeStr: EndTime, title: mjsTitle, description: name,
                            mjsParamStr: DaZhe.util.O2String(mjsParams), activityId: activityId, isShop: false, tmplHtml: tmplHtml, remark:remark}, function(data){
                            if(data === undefined || data == null) {
                                TM.Alert.load("活动修改失败，请重试或联系客服");
                                return;
                            }
                            if(data.success == false) {
                                if(data.taobaoAuthorizeUrl === undefined || data.taobaoAuthorizeUrl == null || data.taobaoAuthorizeUrl == "") {
                                    TM.Alert.load(data.message);
                                    return;
                                }
                                // 需要重新授权

                                TM.UmpUtil.util.showAuthDialog("/umppromotion/newReShouquan", null);
                            } else {
                                TM.Alert.load("活动修改成功", 400, 300, function(){
                                    location.href = "/Sales/index";
                                });

                            }

                        });
                    }


                } else if(mode == me.ProMode.ShopMjs) {
                    /*if($('.mjsContentMustChooseOne.selected').length == 0) {
                     TM.Alert.load("请至少选择减价,打折,包邮中的一种");
                     return;
                     }*/
                    if(isOnlyUpdateMjsActivity == false) {
                        var mjsParams = DaZhe.util.getMjsParamsWithTime(StartTime, EndTime, name);
                        var tmplHtml = $('#mjsTmplHolderWrapper').html();
                        var remark = me.mainForm.find('#comment').val();
                        if(me.mainForm.find('.title-input:visible').length == 0) {
                            mjsTitle = $('#promotion_title option:selected').text();
                        } else {
                            mjsTitle = $('.promotion_title').val();
                        }
                        if(mjsTitle === undefined || mjsTitle == null || mjsTitle == "") {
                            TM.Alert.load("请输入活动标签");
                            return;
                        }
                        if(mjsParams.isSentGift == true) {
                            if(mjsParams.giftName === undefined || mjsParams.giftName == "" || mjsParams.giftName == "请输入小礼物名称") {
                                TM.Alert.load("请输入小礼物名称");
                                return;
                            }
                        }
                        $.post("/UmpMjs/addShopMjsActivity", {startTimeStr: StartTime, endTimeStr: EndTime, title: mjsTitle, description: name, mjsParamStr: DaZhe.util.O2String(mjsParams),
                            tmplHtml: tmplHtml, remark:remark}, function(data){
                            if(data === undefined || data == null) {
                                TM.Alert.load("创建活动失败，请重试或联系客服");
                                return;
                            }
                            if(data.success == false) {
                                if(data.taobaoAuthorizeUrl === undefined || data.taobaoAuthorizeUrl == null || data.taobaoAuthorizeUrl == "") {
                                    TM.Alert.load(data.message);
                                    return;
                                }
                                // 需要重新授权

                                TM.UmpUtil.util.showAuthDialog("/umppromotion/newReShouquan", null);
                            } else {
                                TM.Alert.load("活动创建成功", 400, 300, function(){
                                    location.href = "/Sales/index";
                                });
                            }

                        });
                    } else {
                        var mjsParams = DaZhe.util.getMjsParamsWithTime(StartTime, EndTime, name);
                        var tmplHtml = $('#mjsTmplHolderWrapper').html();
                        var remark = me.mainForm.find('#comment').val();
                        var mjsTitle = "";
                        if(me.mainForm.find('.title-input:visible').length == 0) {
                            mjsTitle = $('#promotion_title option:selected').text();
                        } else {
                            mjsTitle = $('.promotion_title').val();
                        }
                        if(mjsTitle === undefined || mjsTitle == null || mjsTitle == "") {
                            TM.Alert.load("请输入活动标签");
                            return;
                        }
                        if(mjsParams.isSentGift == true) {
                            if(mjsParams.giftName === undefined || mjsParams.giftName == "" || mjsParams.giftName == "请输入小礼物名称") {
                                TM.Alert.load("请输入小礼物名称");
                                return;
                            }
                        }
                        $.post("/TaoDiscount/updateMjsActivity", {startTimeStr: StartTime, endTimeStr: EndTime, title: mjsTitle, description: name,
                            mjsParamStr: DaZhe.util.O2String(mjsParams), activityId: activityId, isShop: true, tmplHtml: tmplHtml, remark:remark}, function(data){
                            if(data === undefined || data == null) {
                                TM.Alert.load("活动修改失败，请重试或联系客服");
                                return;
                            }
                            if(data.success == false) {
                                if(data.taobaoAuthorizeUrl === undefined || data.taobaoAuthorizeUrl == null || data.taobaoAuthorizeUrl == "") {
                                    TM.Alert.load("活动修改失败，请重试或联系客服");
                                    return;
                                }
                                // 需要重新授权

                                TM.UmpUtil.util.showAuthDialog("/umppromotion/newReShouquan", null);
                            } else {
                                TM.Alert.load("活动修改成功", 400, 300, function(){
                                    location.href = "/Sales/index";
                                });

                            }

                        });
                    }

                }


            })

        }  ,

        updateTimeDiff: function(){
            var start  = $('#start_time').datetimepicker('getDate');
            var end  = $('#end_time').datetimepicker('getDate');
            if(!start) start=new Date();
            if(!end) end=new Date();
            var diff = end.getTime()-start.getTime();
            var parents = $("#end_time").parentsUntil(".tmLine");
            var $input = $(parents[parents.length-1]).parent().find(".hint");

            diff = (diff-diff%60000)/60000;
            var t = diff%60;
            $("em:eq(2)", $input).text(t);
            diff = (diff-t)/60;
            t = diff%24;
            $("em:eq(1)", $input).text(t);
            diff = (diff-t)/24;
            $("em:eq(0)", $input).text(diff);
            $("#end_time").parentsUntil(".tmLine").parent().find(".okMsg").show();
            $("#end_time").parentsUntil(".tmLine").parent().find(".errorMsg").hide();
        },

        addAppendError : function(line, text) {
            var append = $(".append", line);
            append.html(text)
                .addClass("error");
        },
        removeAppendError : function(line) {
            var append = $(".append", line);
            append.html(append.data("normal"))
                .removeClass("error");
        },

        checkName: function(){
            var input = $("#name");
            var val = $.trim(input.val());
            if(val.length < 2) {
                var text = '请至少输入2个字';
                input.addClass("error");
                DaZhe.Init.addAppendError(input.parent(), text);
            } else if(val.length > 30) {
                var text = '最多只能输入30个字';
                input.addClass("error");
                DaZhe.Init.addAppendError(input.parent(), text);
            } else {
                input.removeClass("error");
                DaZhe.Init.removeAppendError(input.parent());
            }
        },
        checkTitle: function(){
            var input = $(".promotion_title");
//            var val = $.trim(input.val());
            var val = input.val();
            var reg=/^(\w|[\u4E00-\u9FA5]|_)*$/;

            var title = val;
            if(title.length <= 0) {
                var text = '请先输入宝贝促销标签';
                input.addClass("error");
                DaZhe.Init.addAppendError(input.parent(), text);
            } else if (title.length > 10) {
                var text = '促销标签最多5个字，您已超出';
                input.addClass("error");
                DaZhe.Init.addAppendError(input.parent(), text);
            } else if(val == "满就减") {
                var text = '淘宝规定价格标签不能为满就减';
                input.addClass("error");
                DaZhe.Init.addAppendError(input.parent(), text);
            } else if(!reg.test(val)){
                var text = '标签只允许出现中英文、数字、下划线!';
                input.addClass("error");
                DaZhe.Init.addAppendError(input.parent(), text);
            } else {
                input.removeClass("error");
                DaZhe.Init.removeAppendError(input.parent());
            }

        },

        checkTime :function(){

            var sdate= $('#start_time').datetimepicker('getDate');
            var edate= $('#end_time').datetimepicker('getDate');
            if(!sdate||!edate)     {
                return false;
            }
            var activityTime =edate.getTime()-sdate.getTime();
            var activityDay = Math.round(activityTime/86400000);
            $('#end_time').parent().find('.append').html("促销活动结束时间，当前活动将持续 "+activityDay+" 天");

            var input = $("#start_time");
            if(!sdate || isNaN(sdate.getTime())) {
                var text = '开始时间格式不正确';
                input.addClass("error");
                DaZhe.Init.addAppendError(input.parent(), text);
            }
            else {
                input.removeClass("error");
                DaZhe.Init.removeAppendError(input.parent());
            }

            var input = $("#end_time");
            if(!edate || isNaN(edate.getTime())) {
                var text = '结束时间格式不正确';
                input.addClass("error");
                DaZhe.Init.addAppendError(input.parent(), text);
            }
            else if(edate.getTime() <= sdate.getTime()) {
                var text = '结束时间要大于开始时间';
                input.addClass("error");
                DaZhe.Init.addAppendError(input.parent(), text);
            }
            else {
                input.removeClass("error");
                $('#end_time').parent().find('.append').removeClass("error");
//                DaZhe.Init.removeAppendError(input.parent());
            }

        } ,


        docheck :function(){
            $(".append").each(function() {
                $(this).data("normal", $(this).html());
            });

            $("#name").keyup(function() {
                DaZhe.Init.checkName();
            });
            $("#name").blur(function() {
                DaZhe.Init.checkName();
            });

            $("#start_time").keyup(function() {
                DaZhe.Init.checkTime();
            });
            $("#end_time").keyup(function() {
                DaZhe.Init.checkTime();
            });
            $(".title-input").keyup(function() {
                DaZhe.Init.checkTitle();
            });
            $('.title-input').hide();
            $('.select-put').hide();
            $('.self-put').click(function(){
                $('.title-select').hide();
                $('.self-put').hide();
                $('.title-input').show();
                $('.select-put').show();
                $('.title-input').addClass("promotion_title") ;
                $('.title-select').removeClass("promotion_title") ;

                DaZhe.Init.checkTitle();
            });
            $('.select-put').click(function(){
                $('.promotion_title').attr("value","");
                DaZhe.Init.checkTitle();
                $('.title-input').hide();
                $('.select-put').hide();
                $('.title-select').show();
                $('.self-put').show();
                $('.title-select').addClass("promotion_title") ;
                $('.title-input').removeClass("promotion_title") ;


                $('.title-input').removeClass("error");
            });
//            $('.xiangou').hide();
//            $('.promotionType').change(function(){
//                var promotionType=$('.promotionType').val();
//                if(promotionType==0){
//                   $('.xiangou').hide();
//                }
//                else{
//                   $('.xiangou').show();
//                }
//            });

        },
        parseLongToDate:function(ts) {
            var theDate = new Date();
            theDate.setTime(ts);
            var year = theDate.getFullYear();
            var month = theDate.getMonth() + 1;//js从0开始取
            var date = theDate.getDate();
            var hour = theDate.getHours();
            var minutes = theDate.getMinutes();
            var second = theDate.getSeconds();

            if (month < 10) {
                month = "0" + month;
            }
            if (date < 10) {
                date = "0" + date;
            }
            if (hour < 10) {
                hour = "0" + hour;
            }
            if (minutes < 10) {
                minutes = "0" + minutes;
            }
            if (second < 10) {
                second = "0" + second;
            }
            var timeStr =year+"-"+ month+"-"+date+" "+hour+":"+minutes+ ":" + second;
            return timeStr;
        }

    }, DaZhe.Init);

    DaZhe.util = DaZhe.util || {};
    DaZhe.util = $.extend({
        setCurrentmode : function(mode) {
            if(mode === undefined || mode == null) {
                return me.ProMode.Simple;
            }
            if(mode != me.ProMode.Simple && mode != me.ProMode.Mjs && mode != me.ProMode.ShopMjs) {
                return me.ProMode.Simple;
            }
            return mode;
        },
        getMjsParamsWithTime: function(StartTime, EndTime, name){
            var mjsParams = DaZhe.util.getMjsParams();
            mjsParams.start = StartTime;
            mjsParams.end = EndTime;
            mjsParams.activityName = name;
            return mjsParams;
        },
        getMjsParams : function(){
            var mjsParams = $.extend({}, TM.MjsParams);

            // 如果是满多少元就送
            if(me.mainForm.find('span[name="mjsType"].selected').attr("tag") == "amount") {
                mjsParams.totalPrice = Math.round(parseFloat($('#manMoney').val()) * 100);
                mjsParams.isAmountOver = true;
                mjsParams.isItemCountOver = false;
            } else {
                mjsParams.itemCount = $('#itemcountinput').val();
                mjsParams.isItemCountOver = true;
                mjsParams.isAmountOver = false;
            }

            // 如果是满就减
            if(me.mainForm.find('span[name="mjsDecreasOrDisCount"].selected').length == 0) {
                // do nothing
            } else if(me.mainForm.find('span[name="mjsDecreasOrDisCount"].selected').attr("tag") == "decrease") {
                mjsParams.decreaseValue = Math.round(parseFloat($('#decreaseNum').val()) * 100);
                mjsParams.isDecrease = true;
                mjsParams.isDiscount = false;
                // 是否上不封顶
                if(me.mainForm.find('.decreaseMultipleOrNot span.selected').length == 1) {
                    mjsParams.isAmountMultiple = true;
                    mjsParams.isItemMultiple = true;
                }
            } else {
                mjsParams.discountValue = Math.round(parseFloat($('#disCountNum').val()) * 100);
                mjsParams.isDiscount = true;
                mjsParams.isDecrease = false;
            }

            if(me.mainForm.find('span[name="baoyou"]').parent().hasClass("selected")) {
                mjsParams.excludedAreas = $.cookie("excludedAreas") == null ? "" : $.cookie("excludedAreas");
                mjsParams.excludedCodes = $.cookie("excludedCodes") == null ? "" : $.cookie("excludedCodes");
                mjsParams.isFreePost = true;
            }

            if(me.mainForm.find('span[name="gift"].selected').length == 1) {
                mjsParams.isSentGift = true;
                mjsParams.giftName = $('.little-gift-name').val();
                mjsParams.giftId = 0;
                mjsParams.giftUrl = "";

            }

            mjsParams.toServerParams = "";
            mjsParams.buildDisplay = "";
            mjsParams.createOne = "";
            return mjsParams;
        },
        buildDisplayByMjsParam : function(mjsParam){
            var htmls = '';
            if(mjsParam.isAmountOver){
                htmls += '单笔订单满 <strong style="color: red;font-size: 16px;font-weight: bold;">'+mjsParam.totalPrice / 100 +'</strong>元, ';
            }else if(mjsParam.isItemCountOver){
                htmls += '单笔订单满 <strong style="color: red;font-size: 16px;font-weight: bold;">'+mjsParam.itemCount +'</strong>件, ';
            }
            if(mjsParam.isDiscount){
                htmls +='&nbsp;打 <strong style="color: red;font-size: 16px;font-weight: bold;">'+(mjsParam.discountValue > 0 ? mjsParam.discountValue / 100:'&nbsp;')+'</strong>折';
            }else if(mjsParam.isDecrease){
                htmls +='&nbsp;减 <strong style="color: red;font-size: 16px;font-weight: bold;">'+(mjsParam.decreaseValue > 0 ? mjsParam.decreaseValue / 100 : 0)+'</strong>元';
                if(mjsParam.isAmountMultiple && mjsParam.isItemMultiple) {
                    htmls += "&nbsp;(上不封顶)";
                }
            }

            if(mjsParam.isFreePost){

                var checkedAreas = $('.baoyouAreaSelector .areaName').find('input:checked');
                var noCheckedAreas = $('.baoyouAreaSelector .areaName input').not('input:checked');

                var checkedNum = checkedAreas.length;

                var noCheckedNum =noCheckedAreas.length;

                var areaNum = checkedNum + noCheckedNum;
                if(areaNum == checkedNum){
                    // all included nothing need to do..
                    htmls += '&nbsp; 全国包邮';
                }else if(checkedNum == 0){
                    // No areas are included...
                    htmls += '&nbsp; 包邮(仅限地球外)';
                }else if(checkedNum > (areaNum/2)){
                    // More than half areas are included.., show the not included areas...
                    var arr = [];
                    noCheckedAreas.each(function(i,elem){arr.push($(elem).parent().find('.areatext').text())});
                    htmls += '&nbsp; 包邮(除 '+arr.join(',')+'外) ';
                }else {
                    // direct show the little included baoyou areas..
                    // More than half areas are included.., show the not included areas...
                    var arr = [];
                    checkedAreas.each(function(i,elem){arr.push($(elem).parent().find('.areatext').text())});
                    htmls += '&nbsp; 包邮(仅限 '+arr.join(',')+')';
                }
            }

            if(mjsParam.giftName === undefined || mjsParam.giftName == null || mjsParam.giftName == "") {

            } else {
                htmls += '  &nbsp; &nbsp; <span style="color: red;font-size: 16px;font-weight: bold;">送礼物</span>&nbsp; &nbsp;' + mjsParam.giftName;
            }


            var commentInput = $('#comment');
            var commentValue = commentInput.val().trim();
            if(commentValue != ""){
                htmls += '<br/> '+commentValue;
            }


            return htmls;


        },
        updateMjsTmplHtml : function(){
            me.mainForm.find('.promoteTemplate span.selected').trigger("click");
        },
        O2String : function (O) {
            //return JSON.stringify(jsonobj);

            var S = [];
            var J = "";
            if (Object.prototype.toString.apply(O) === '[object Array]') {
                for (var i = 0; i < O.length; i++)
                    S.push(DaZhe.util.O2String(O[i]));
                J = '[' + S.join(',') + ']';
            }
            else if (Object.prototype.toString.apply(O) === '[object Date]') {
                J = "new Date(" + O.getTime() + ")";

            }
            else if (Object.prototype.toString.apply(O) === '[object RegExp]' || Object.prototype.toString.apply(O) === '[object Function]') {
                J = O.toString();
            }
            else if (Object.prototype.toString.apply(O) === '[object Object]') {
                for (var i in O) {
                    O[i] = typeof (O[i]) == 'string' ? '"' + O[i] + '"' : (typeof (O[i]) === 'object' ? DaZhe.util.O2String(O[i]) : O[i]);
                    S.push('"' + i + '"' + ':' + O[i]);
                }
                J = '{' + S.join(',') + '}';
            }
            return J;
        }
    },DaZhe.util);

    DaZhe.Event = DaZhe.Event || {};
    DaZhe.Event = $.extend({
        setMjsEvent : function(){
            DaZhe.Event.setInputKeyUpEvent();
            DaZhe.Event.setMjsTypeRadioEvent();
            DaZhe.Event.setMjsContentEvent();
            DaZhe.Event.setMjsAreaSelectorEvent();
            DaZhe.Event.setMjsTemplate();
        },
        setMjsTemplate : function(){
            var template = me.mainForm.find('.promoteTemplate');
            var changeTmpl = function(index){
                if(index === undefined || index == null) {
                    index = 1;
                }
                var box = template.find('.selected');
                var tmplDiv=  $(box.find('.tmpl'));
                var mjsParam = me.members.formMjsParams();
                var display = DaZhe.util.buildDisplayByMjsParam(mjsParam);
                mjsParam.toDisplay = display;
                var htmls = tmplDiv.tmpl(mjsParam);
                me.members.mjsTmplHolder.html(htmls);
                me.members.mjsTmplHolder.attr("index", index);
            }

            template.find('.uCheckbox').click(function(){
                var box = $(this);

                /*if(box.hasClass('selected')){
                 // current is already selected...
                 return;
                 }*/
                template.selectBox = template.find('.selected');
                template.selectBox.removeClass('selected');
                template.selectBox.addClass('unselected');
                box.removeClass('unselected');
                box.addClass('selected');
                var index = box.attr("index");
                changeTmpl(index);
            });
            // TODO, when the option changed, we have to reform the mjs params....

        },
        setInputKeyUpEvent : function(){
            $('.numberInput').keyup(function(){
                var oThis = $(this);
                if(oThis.val().replace(/[^0-9]/g,'') == oThis.val()) {
                    return;
                }
                oThis.val(oThis.val().replace(/[^0-9]/g,''));
            })
            $('.doubleInput').keyup(function(){
                var oThis =$(this);
                if(oThis.val().replace(/[^0-9\\.]/g,'') == oThis.val()) {
                    return;
                }
                oThis.val(oThis.val().replace("。",'.'));
                oThis.val(oThis.val().replace(/[^0-9\\.]/g,''));
                if(oThis.val().substr(oThis.val().indexOf(".") + 1, 10).length > 2) {
                    oThis.val(oThis.val().substr(0, oThis.val().length - 1));
                }
                DaZhe.util.updateMjsTmplHtml();
            });
        },
        setMjsTypeRadioEvent : function() {

            me.mainForm.find('span[name="mjsType"]').unbind('click').click(function(){

                var oThis = $(this);
                if($(this).hasClass('selected')) {
                    return;
                }

                var currSelected = oThis.parent().parent().find('.selected');
                currSelected.removeClass('selected');
                currSelected.addClass('unselected');
                currSelected.find('.bigMoneyInput').addClass("mjsTypeUnCheck");
                currSelected.find('.bigMoneyInput').removeClass("mjsTypeUnCheck");
                oThis.removeClass('unselected');
                oThis.addClass('selected');

                DaZhe.util.updateMjsTmplHtml();
            });

            $('.mjsTypeInput').click(function(){
                var oThis =$(this);
                if(oThis.parent().prev().attr("checked") == true) {
                    return;
                }
                oThis.parent().prev().trigger('click');
                /*oThis.parent().parent().find('.bigMoneyInput').addClass("mjsTypeUnCheck");
                 oThis.find('input').removeClass("mjsTypeUnCheck");*/
            });
        },
        setMjsContentEvent : function(){
            $('.clicktoprev').click(function(){
                var oThis =$(this);
                /* var prev = oThis.parent().parent().parent().find('span[name="mjsDecreasOrDisCount"]');
                 prev.addClass('unselected');
                 prev.removeClass('selected');*/
                oThis.prev().trigger('click');
                $('.decreaseOrDiscount .bigMoneyInput').addClass("mjsTypeUnCheck");
                oThis.find('input').removeClass("mjsTypeUnCheck");
                /*if(oThis.parent().hasClass("manjian")) {
                 $('.decreaseMultipleOrNot').show();
                 } else {
                 $('.decreaseMultipleOrNot').hide();
                 }
                 */
                DaZhe.util.updateMjsTmplHtml();
            });
            $('.decreaseMultipleOrNot').unbind("click").click(function(){
                var uCheckBox = $(this).find('.uCheckbox');
                if(uCheckBox.hasClass("unselected")) {
                    uCheckBox.removeClass("unselected");
                    uCheckBox.addClass("selected");
                } else {
                    uCheckBox.removeClass("selected");
                    uCheckBox.addClass("unselected");
                }

                DaZhe.util.updateMjsTmplHtml();
            });
            me.mainForm.find('span[name="gift"] .clicktarget').unbind('click').click(function(){
                var oThis = $(this).parent();
                if(oThis.hasClass('selected')) {
                    $(this).parent().find('.little-gift-name').hide();
                    oThis.removeClass('selected');
                    oThis.addClass('unselected');
                } else {
                    oThis.parent().find('.little-gift-name').show();
                    oThis.addClass('selected');
                    oThis.removeClass('unselected');
                }
                DaZhe.util.updateMjsTmplHtml();
            });
            var giftInput = me.mainForm.find('.little-gift-name');
            giftInput.focus(function(){
                var oThis = $(this);
                if(oThis.val()==oThis.attr('default')){
                    oThis.val('');
                }
            });
            giftInput.blur(function(){
                var oThis = $(this);
                if(oThis.val().trim().length<=0){
                    oThis.val(oThis.attr('default'));
                }
                DaZhe.util.updateMjsTmplHtml();
            });
            me.mainForm.find('.baoyou em').unbind('click').click(function(){
                var oThis = $(this).parent();
                if(oThis.hasClass('selected')) {
                    oThis.parent().find('.setArea').hide();
                    oThis.removeClass('selected');
                    oThis.addClass('unselected');
                } else {
                    oThis.parent().find('.setArea').show();
                    oThis.addClass('selected');
                    oThis.removeClass('unselected');
                }
                DaZhe.util.updateMjsTmplHtml();
            });
            me.mainForm.find('span[name="baoyou"]').unbind('click').click(function(){
                var oThis = $(this).parent();
                if(oThis.hasClass('selected')) {
                    oThis.parent().find('.setArea').hide();
                    oThis.removeClass('selected');
                    oThis.addClass('unselected');
                } else {
                    oThis.parent().find('.setArea').show();
                    oThis.addClass('selected');
                    oThis.removeClass('unselected');
                }
                DaZhe.util.updateMjsTmplHtml();
            });
            $('.setArea').unbind('click')
                .click(function(){
                    var left = ($(window).width() - 800) / 2;
                    $('.baoyouAreaSelector').css("left", left + "px");
                    $('.baoyouAreaSelector').show();
                });
            /*me.mainForm.find('span[name="mjsDecreasOrDisCount"]').unbind('click').click(function(){
             var oThis = $(this);
             if(oThis.hasClass('selected')) {
             oThis.find('input').addClass("mjsTypeUnCheck");
             if(oThis.attr("tag") == "decrease") {
             $('.decreaseMultipleOrNot').show();
             }
             } else {
             $('.decreaseOrDiscount .bigMoneyInput').addClass("mjsTypeUnCheck");
             oThis.parent().find('span[name="mjsDecreasOrDisCount"]').removeClass("mjsTypeUnCheck");
             if(oThis.attr("tag") == "decrease") {
             $('.decreaseMultipleOrNot').show();
             $('.manzhe .uCheckbox').removeClass('selected');
             $('.manzhe .uCheckbox').addClass('unselected');
             $('.manjian .uCheckbox').addClass('selected');
             $('.manjian .uCheckbox').removeClass('unselected');
             } else {
             $('.decreaseMultipleOrNot').hide();
             $('.manjian .uCheckbox').removeClass('selected');
             $('.manjian .uCheckbox').addClass('unselected');
             $('.manzhe .uCheckbox').addClass('selected');
             $('.manzhe .uCheckbox').removeClass('unselected');
             }
             }

             DaZhe.util.updateMjsTmplHtml();
             });*/
            me.mainForm.find('span[name="mjsDecreasOrDisCount"] .prmoteDetailLineEM').unbind('click').click(function(){
                var oThis = $(this).parent();
                if(oThis.hasClass('selected')) {
                    /* oThis.find('input').addClass("mjsTypeUnCheck");
                     if(oThis.attr("tag") == "decrease") {
                     $('.decreaseMultipleOrNot').show();
                     }*/
                    oThis.find('input').removeClass("mjsTypeUnCheck");
                    oThis.removeClass("selected");
                    oThis.addClass("unselected");
                    if(oThis.attr("tag") == "decrease") {
                        $('.decreaseMultipleOrNot').hide();
                    }
                } else {
                    $('.decreaseOrDiscount .bigMoneyInput').addClass("mjsTypeUnCheck");
                    oThis.parent().find('span[name="mjsDecreasOrDisCount"]').removeClass("mjsTypeUnCheck");
                    if(oThis.attr("tag") == "decrease") {
                        $('.decreaseMultipleOrNot').show();
                        $('.manzhe .uCheckbox').removeClass('selected');
                        $('.manzhe .uCheckbox').addClass('unselected');
                        $('.manjian .uCheckbox').addClass('selected');
                        $('.manjian .uCheckbox').removeClass('unselected');
                    } else {
                        $('.decreaseMultipleOrNot').hide();
                        $('.manjian .uCheckbox').removeClass('selected');
                        $('.manjian .uCheckbox').addClass('unselected');
                        $('.manzhe .uCheckbox').addClass('selected');
                        $('.manzhe .uCheckbox').removeClass('unselected');
                    }
                }

                DaZhe.util.updateMjsTmplHtml();
            });

        },
        setMjsAreaSelectorEvent : function(){
            $('.baoyouAreaSelector .selectAllAreas').unbind('click')
                .click(function(){
                    $('.baoyouAreaSelector input').attr("checked", true);
                });

            $('.baoyouAreaSelector .selectNoArea').unbind('click')
                .click(function(){
                    $('.baoyouAreaSelector input').attr("checked", false);
                });

            $('.baoyouAreaSelector .jzh').unbind('click')
                .click(function(){
                    $('.baoyouAreaSelector .jiangzhehu').attr("checked", true);
                });

            $('.baoyouAreaSelector .noRemoteAreas').unbind('click')
                .click(function(){
                    $('.baoyouAreaSelector .remoteArea').attr("checked", false);
                });

            $('.baoyouAreaSelector .selectChild').unbind('click')
                .click(function(){
                    if($(this).find('input').attr("checked") == "checked") {
                        $(this).parent().find('.areaName input').attr("checked", true);
                    } else {
                        $(this).parent().find('.areaName input').attr("checked", false);
                    }

                });

            $('.baoyouAreaSelector .areaSelectOK').unbind('click').click(function(){
                var excludeArea = "", excludeCode = "";
                if($('.baoyouAreaSelector .areaName input:checked').length == 0) {
                    me.mainForm.find('span[name="baoyou"]').trigger("click");
                    $('.baoyouAreaSelector').hide();

                    DaZhe.util.updateMjsTmplHtml();
                    return;
                }
                $('.baoyouAreaSelector .areaName input').not('input:checked').each(function(i ,exclude){
                    excludeArea += $(exclude).parent().find('.areatext').text();
                    excludeCode += $(exclude).parent().find('.areatext').attr("areaId");
                    if(i < $('.baoyouAreaSelector .areaName input').not('input:checked').length - 1) {
                        excludeArea += "*";
                        excludeCode += "*";
                    }
                });;
                $.cookie("excludedAreas", excludeArea);
                $.cookie("excludedCodes", excludeCode);
                $('.baoyouAreaSelector').hide();

                DaZhe.util.updateMjsTmplHtml();
            });

            $('.baoyouAreaSelector .areatext').unbind('click')
                .click(function(){
                    if($(this).parent().find('input').attr("checked") == "checked") {
                        $(this).parent().find('input').attr("checked", false);
                        $(this).parent().parent().find('.selectChild input').attr("checked", false);
                    } else {
                        $(this).parent().find('input').attr("checked", true);
                        if($(this).parent().parent().find('.areaName input:checked').length == $(this).parent().parent().find('.areaName').length) {
                            $(this).parent().parent().find('.selectChild input').attr("checked", true);
                        }
                    }

                });
            $('.baoyouAreaSelector .areaName input').unbind('click')
                .click(function(){
                    if($(this).attr("checked") == "checked") {
                        if($(this).parent().parent().find('.areaName input:checked').length == $(this).parent().parent().find('.areaName').length) {
                            $(this).parent().parent().find('.selectChild input').attr("checked", true);
                        }
                    } else {
                        $(this).parent().parent().find('.selectChild input').attr("checked", false);
                    }

                });
            $('.baoyouAreaSelector .exitAreaSelector').unbind('click')
                .click(function(){
                    $('.baoyouAreaSelector').hide();
                });
        }
    },DaZhe.Event);


    TM.DisItem = TM.DisItem || {};

    var DisItem = TM.DisItem;

    DisItem.init = DisItem.init || {};
    DisItem.init = $.extend({
        doInit: function(container) {
            DisItem.container = container;
            DisItem.search.doSearch();
            DisItem.search.doquanxuan();
            DisItem.submit.doSub();
        }

    },DisItem.init);

    var selectItems =[];

    DisItem.search =DisItem.search || {};
    DisItem.search= $.extend({
        doSearch: function() {
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
                DisItem.search.doShow(1);
            });
            $('.fRl-ico-pd').click(function(){
                $('.fR-text').html("↓ 价格从高到低");
                $('.fR-text').attr("order","pd");
                DisItem.search.doShow(1);
            });
            $('.fRl-ico-su').click(function(){
                $('.fR-text').html("↑ 下架时间");
                $('.fR-text').attr("order","su");
                DisItem.search.doShow(1);
            });
            $('.fRl-ico-sd').click(function(){
                $('.fR-text').html("↓ 下架时间");
                $('.fR-text').attr("order","sd");
                DisItem.search.doShow(1);
            });
            $('.fRl-ico-df').click(function(){
                $('.fR-text').html("默认排序");
                $('.fR-text').attr("order","df");
                DisItem.search.doShow(1);
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
                DisItem.search.doShow(1);
            });
            $('.fD-ico-undis').click(function(){
                $('.fD-text').html("未参加活动");
                $('.fD-text').attr("isDis","undis");
                DisItem.search.doShow(1);
            });
            $('.fD-ico-all').click(function(){
                $('.fD-text').html("所有宝贝");
                $('.fD-text').attr("isDis","all");
                DisItem.search.doShow(1);
            });

            $('.doDazhe').mousemove(function(){
                $('.tip-content').html("现在是减价模式，点击变为折扣模式！（折扣模式会出现小数点）");
                $('.tspy').show();
            });
            $('.doDazhe').mouseout(function(){
                $('.tspy').hide();
            });
            $('.doJianjia').mousemove(function(){
                $('.tip-content').html("现在是折扣模式，点击变为减价模式！（对于多个价格的宝贝建议使用折扣模式）");
                $('.tspy').show();
            });
            $('.doJianjia').mouseout(function(){
                $('.tspy').hide();
            });

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
                    DisItem.search.doShow(1);
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
                        DisItem.search.doShow(1);
                    });
                    li_option.append(option);
                    sellerCat.append(li_option);

                }
            });
            $.get("/items/itemCatCount",function(data){
                var sellerCat = $('#itemCat');
                sellerCat.empty();
                if(!data || data.length == 0){
                    sellerCat.hide();
                }

                var exist = false;
                var cat = $('<li><a href="javascript:void(0);">所有类目</a></li>');
                cat.click(function(){
                    $('.fI-text').html('<a href="javascript:void(0);">所有类目</a>');
                    DisItem.search.doShow(1);
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
                        DisItem.search.doShow(1);
                    });
                    li_option.append(option);
                    sellerCat.append(li_option);
                }
            });
            $('.guanjianci-select').click(function(){
                DisItem.search.doShow(1);
            })  ;

            DisItem.search.doShow(1);
            //批量修改
            $('.piliang').keyup(function(){
                var piliang= $('.piliang').val();
                $(".item").each(function(i,val){
                    var status=$(val).find(".item-status").val();
                    if(status!=4){
                        $(val).find('.item-dis').attr("value",piliang);
                        var price = $(val).find('.old-price').val();
                        var discount =Math.round((price*piliang/10)*100)/100;
                        var jianjia= Math.round((price-discount)*100)/100;
                        $(val).find('.item-disprice').attr("value",discount);
                        $(val).find('.item-jianjia').attr("value",jianjia);
                    }
                });
            });
            $('.piliang-jianjia').keyup(function(){
                var piliang= $('.piliang-jianjia').val();
                $(".item").each(function(i,val){
                    var status=$(val).find(".item-status").val();
                    if(status!=4){
                        $(val).find('.item-jianjia').attr("value",piliang);
                        var price = $(val).find('.old-price').val();
                        var disprice= Math.round((price-piliang)*100)/100;
                        var discount =Math.round((disprice/price)*1000)/100;
                        $(val).find('.item-disprice').attr("value",disprice);
                        $(val).find('.item-dis').attr("value",discount);
                    }
                });
            });
            //抹零操作
            $('.dofen').click(function(){
                $(".item").each(function(i,val){
                    var disprice = $(val).find('.item-disprice').val();
                    var status=$(val).find(".item-status").val();
                    if(status!=4){
                        if(disprice){
                            disprice= Math.round(disprice*10)/10;
                            $(val).find('.item-disprice').attr("value",disprice);
                            var price = $(val).find('.old-price').val();
                            var discount =Math.round((disprice/price)*1000)/100;
                            var jianjia =Math.round((price-disprice)*100)/100;

                            $(val).find('.item-jianjia').attr("value",jianjia);
                            $(val).find('.item-dis').attr("value",discount);
                        }
                    }
                });
            });
            $('.dofenjiao').click(function(){
                $(".item").each(function(i,val){
                    var disprice = $(val).find('.item-disprice').val();
                    var status=$(val).find(".item-status").val();
                    if(status!=4){
                        if(disprice){
                            disprice= Math.round(disprice);
                            $(val).find('.item-disprice').attr("value",disprice);
                            var price = $(val).find('.old-price').val();
                            var discount =Math.round((disprice/price)*1000)/100;
                            var jianjia =Math.round((price-disprice)*100)/100;

                            $(val).find('.item-jianjia').attr("value",jianjia);
                            $(val).find('.item-dis').attr("value",discount);
                        }
                    }
                });
            });

            //错误提示
            $('.orangeBtn').click(function(){
                $('#hraBox').hide();
            });
            $('.closeModal').click(function(){
                $('#settingActModal').hide();
            });

            //打折模式
            $('.doJianjia').hide();
            $('.doDazhe').click(function(){
                if (confirm("确定本页所有宝贝全部变为打折模式？" ) == false) {
                    return;
                }
                $('.doDazhe').hide();
                $('.doJianjia').show();
                $(".item").each(function(i,val){
                    var status=$(val).find(".item-status").val();
                    if(status!=4) {
                        $(val).find(".Model-dazhe").show();
                        $(val).find(".Model-jianjia").hide();
                        $(val).find(".item-discountType").attr("value",0);
                    }
                })
            });
            $('.doJianjia').click(function(){
                if (confirm("确定本页所有宝贝全部变为减价模式？") == false)
                    return;
                $('.doJianjia').hide();
                $('.doDazhe').show();
                $(".item").each(function(i,val){
                    var status=$(val).find(".item-status").val();
                    if(status!=4) {
                        $(val).find(".Model-jianjia").show();
                        $(val).find(".Model-dazhe").hide();
                        $(val).find(".item-discountType").attr("value",1);
                    }
                })
            });
        },
        doShow: function(currentPage) {
            var data={};
            data.title=$('.guanjianci').val();
            data.cid=$(".fI-text a").attr("catid");
            data.sellerCid= $(".fS-text a").attr("catid");
            data.order = $(".fR-text").attr("order");
            data.isDis=  $(".fD-text").attr("isDis");
            if (currentPage < 1)
                currentPage = 1;
            var tbodyObj = DisItem.container.find(".item-table");
            DisItem.container.find(".paging-div").tmpage({
                currPage: currentPage,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: data,
                    dataType: 'json',
                    url: '/TaoDiscount/searchItems',
                    callback: function(dataJson){
                        DisItem.row.setArrayString();
                        if($(".error").length > 0) {
                            alert("折扣范围（0.01--9.9）折，请修正错误再提交");
                            setTimeout(function() {
                                $("input.error").effect('bounce', {times: 3, distance: 30}, 1500);
                            }, 300);
                            return false;
                        }
                        tbodyObj.html("");
                        var itemArray = dataJson.res;
                        $(itemArray).each(function(index, itemJson) {
                            var trObj = DisItem.row.createRow(index, itemJson);
                            tbodyObj.append(trObj);
                        });
                    }
                }
            });
        } ,
        doquanxuan: function(){
            $(".unchoose").hide();
            $('.choose').click(function(){
                $(".choose").hide();
                $(".unchoose").show();
                var piliang= $('.piliang').val();
                $(".item").each(function(i,val){
                    var status=$(val).find(".item-status").val();
                    if(status==0) {
                        $(val).find('.lightBlueBtn').hide();
                        $(val).find(".lightGrayBtn").show();
                        if(piliang){
                            $(val).find('.item-dis').attr("value",piliang);
                            var price = $(val).find('.old-price').val();
                            var discount =Math.round((price*piliang/10)*100)/100;
                            var jianjia= Math.round((price-discount)*100)/100;
                            $(val).find('.item-disprice').attr("value",discount);
                            $(val).find('.item-jianjia').attr("value",jianjia);
                        }
                        $(val).find(".item-status").attr("value", 8);
                    }
                })
            });
            $('.unchoose').click(function(){
                $(".unchoose").hide();
                $(".choose").show();
                $(".item").each(function(i,val){
                    var status=$(val).find(".item-status").val();
                    if(status==8) {
                        $(val).find('.lightGrayBtn').hide();
                        $(val).find(".lightBlueBtn").show();
                        $(val).find('.item-dis').attr("value","");
                        $(val).find('.item-disprice').attr("value","");
                        $(val).find('.item-jianjia').attr("value","");
                        $(val).find(".item-status").attr("value", 0);
                    }
                })
            });
        }
    },DisItem.search);

    DisItem.row = DisItem.row || {};
    DisItem.row = $.extend({
        createRow: function(index, itemJson) {
            var html = DisItem.row.createHtml();
            var trObj = $(html);

            trObj.find(".item-code").attr("value", itemJson.numiid);
            var href = "http://item.taobao.com/item.htm?id=" + itemJson.numiid;
            trObj.find(".item-href").attr("href", href);
            trObj.find(".item-href").attr("target", "_blank");
            trObj.find(".item-img").attr("src", itemJson.picURL);
            trObj.find(".item-name").html(itemJson.title);
            trObj.find(".item-price").html(itemJson.price);
            trObj.find(".old-price").attr("value",itemJson.price) ;

//            trObj.find(".item-status").attr("value", 0);//一开始都是未参加活动的


            var refreshCallback = function() {
                DisItem.search.doSearch();
            };


            var html1 = '' +
                '<a href="javascript:;" class="lightBlueBtn addToActBtn productBtn" >加入活动</a>' +
                '<a href="javascript:;"  class="lightGrayBtn addedToActBtn productBtn" >已添加</a>' +
                '';
            var html2=''+
                '<span class="item_disable">已参加其他活动</span>' +
                '';
            if(itemJson.discountValue!=0) {
                trObj.find(".item-status").attr("value", 4);//已经参加其他活动的
                trObj.addClass("disabled");
                trObj.find(".op-td").html(html2);
                trObj.find(".item-discountType").attr("value",itemJson.discountType) ;
                if(itemJson.discountType==0){
                    var dis=itemJson.discountValue;
                    var price = itemJson.price;
                    var discount =Math.round((price*dis/10)*100)/100;
                    var jianjia= Math.round((price-discount)*100)/100;
                    trObj.find(".item-dis").attr("value",dis);
                    trObj.find('.item-disprice').attr("value",discount);
                    trObj.find('.item-jianjia').attr("value",jianjia);

                    trObj.find(".Model-tip").html("");
                    trObj.find(".Model-dazhe").show();
                    trObj.find(".Model-jianjia").hide();

                }
                else{
                    var jianjia= itemJson.discountValue;
                    var price =itemJson.price;
                    var dis=Math.round(((price-jianjia)/price)*1000)/100;
                    var disprice= Math.round((price-jianjia)*100)/100;
                    trObj.find(".item-disprice").attr("value",disprice);
                    trObj.find(".item-dis").attr("value",dis) ;
                    trObj.find('.item-jianjia').attr("value",jianjia);

                    trObj.find(".Model-tip").html("");
                    trObj.find(".Model-jianjia").show();
                    trObj.find(".Model-dazhe").hide();

                }
            }
            else{
                trObj.find(".item-status").attr("value", 0);//一开始都是未参加活动的
                trObj.find(".item-discountType").attr("value",1) ;

                trObj.find(".op-td").html(html1);
                trObj.find('.lightGrayBtn').hide();

                trObj.find(".Model-jianjia").show();
                trObj.find(".Model-dazhe").hide();
                trObj.find(".Model-dazhe").click(function(){
                    trObj.find(".Model-jianjia").show();
                    trObj.find(".Model-dazhe").hide();
                    trObj.find(".item-discountType").attr("value",1);
                }) ;
                trObj.find(".Model-jianjia").click(function(){
                    trObj.find(".Model-dazhe").show();
                    trObj.find(".Model-jianjia").hide();
                    trObj.find(".item-discountType").attr("value",0);
                }) ;

                for (var i=0; i<selectItems.length;i++){
                    if (selectItems[i].id == itemJson.numiid){
                        trObj.find(".item-status").attr("value", 8);
                        trObj.find(".item-disprice").attr("value",selectItems[i].disprice);
                        trObj.find(".item-dis").attr("value",selectItems[i].discount) ;
                        trObj.find('.item-jianjia').attr("value",selectItems[i].jianjia);
                        trObj.find(".item-discountType").attr("value",selectItems[i].discountType);
                        trObj.find(".lightGrayBtn").css("display","block");
                        trObj.find('.lightBlueBtn').css("display","none");
                        if(selectItems[i].discountType==0){
                            trObj.find(".Model-dazhe").show();
                            trObj.find(".Model-jianjia").hide();
                        }
                        else{
                            trObj.find(".Model-jianjia").show();
                            trObj.find(".Model-dazhe").hide();
                        }
                    }
                }

                trObj.find(".lightBlueBtn").click(function() {
                    trObj.find('.lightBlueBtn').hide();
                    trObj.find(".lightGrayBtn").show();
                    var piliang= $('.piliang').val();
                    if(piliang){
                        trObj.find('.item-dis').attr("value",piliang);
                        var price = itemJson.price;
                        var discount =Math.round((price*piliang/10)*100)/100;
                        var jianjia= Math.round((price-discount)*100)/100;
                        trObj.find('.item-disprice').attr("value",discount);
                        trObj.find('.item-jianjia').attr("value",jianjia);
                    }
                    trObj.find(".item-status").attr("value", 8);
                });
                trObj.find(".lightGrayBtn").click(function(){
                    trObj.find('.lightBlueBtn').show();
                    trObj.find(".lightGrayBtn").hide();
                    trObj.find('.item-dis').attr("value","");
                    trObj.find('.item-disprice').attr("value","");
                    trObj.find('.item-jianjia').attr("value","");
                    trObj.find(".item-status").attr("value", 0);
                }) ;

                trObj.find(".item-dis").keyup(function(){
                    var dis= trObj.find(".item-dis").val();
                    var price =itemJson.price;
                    var discount =Math.round((price*dis/10)*100)/100;
                    var jianjia=Math.round((price-discount)*100)/100 ;
                    trObj.find(".item-disprice").attr("value",discount);
                    trObj.find(".item-jianjia").attr("value",jianjia);
                });

                trObj.find(".item-disprice").keyup(function(){
                    var dis= trObj.find(".item-disprice").val();
                    var price =itemJson.price;
                    var discount=Math.round((dis/price)*1000)/100;
                    var jianjia= Math.round((price-dis)*100)/100;
                    trObj.find(".item-dis").attr("value",discount) ;
                    trObj.find(".item-jianjia").attr("value",jianjia);
                });
                trObj.find(".item-jianjia").keyup(function(){
                    var jianjia= trObj.find(".item-jianjia").val();
                    var price =itemJson.price;
                    var dis=Math.round(((price-jianjia)/price)*1000)/100;
                    var disprice= Math.round((price-jianjia)*100)/100;
                    trObj.find(".item-disprice").attr("value",disprice);
                    trObj.find(".item-dis").attr("value",dis) ;
                });

            }
            return trObj;
        },
        createHtml: function(itemJson) {
            var html='' +
                '<div class="item">' +
                '   <input type="hidden" class="item-code" /> ' +
                '   <input type="hidden" class="item-discountType" />'+
                '   <input type="hidden" class="item-status" /> ' +
                '   <input type="hidden" class="old-price" >'+
                '    <a class="productImg item-href">' +
                '        <img class="item-img"  />' +
                '    </a>' +
                '    <div class="productInfo">' +
                '        <div class="productTitle">' +
                '          <a style="height: 40px;width: 100%;display: block;overflow: hidden;" class="item-href item-link item-name"></a>' +
                '        </div>' +
                '        <p>' +
                '           <span style="font-size: 20px;color: #C49173">原价：</span>' +
                '           <em class="proSell-price">¥</em>' +
                '           <em class="proSell-price item-price"></em>' +
                '        </p>' +
                '        <p>' +
                '           <span style="font-size: 12px;color: #C49173">促销模式：</span>' +
                '           <a class="disModel" style="color:#FF9A36" href="javascript:void(0)"><b class="Model-dazhe">打折<span class="Model-tip"  style="color: #C49173">（点击修改）</span></b><b class="Model-jianjia">减价<span class="Model-tip"  style="color: #C49173">（点击修改）</span></b>' +
                '           </a>' +
                '        </p>' +
                '    </div>' +
                '    <div class="productDis">' +
                '        <div class="dazheValue">' +
                //'           打折：' +
                //'            <br>' +
                '            打<input class="item-dis" type="text" style="border: 1px solid #B0A59F"  />' +
                '           折' +
                '        </div>' +
                '        <div class="jianjiaValue">' +
                //'           减价：' +
                //'            <br>' +
                '            减<input class="item-jianjia" type="text" style="color:#B0A59F;border: 1px solid #B0A59F" />' +
                '           元' +
                '        </div>' +
                '        <div class="zhehoujiaValue">' +
                //'           折后价：' +
                //'            <br>' +
                '            结果<input class="item-disprice" type="text" style="color:#C00;border: 1px solid #B0A59F" />' +
                '           元' +
                '        </div>' +
                '        ' +
                '    </div>' +
                '    <div class="op-td">' +
                '    </div>' +
                '</div>' +
                '';

            return html;
        },
        addToSelectedItems :function(item){
            var existed = DisItem.row.removeFromSelectedItems(item);
            selectItems.push(item);
            return !existed;
        },
        removeFromSelectedItems : function(item){
            for (var i=0; i<selectItems.length;i++){
                if (selectItems[i].id == item.id){
                    selectItems.splice(i,1);
                    return true;
                }
            }
            return false;
        },
        setArrayString : function(){
            $(".item").each(function(i,val){
                var item={};
                var itemCode=$(val).find('.item-code').val();
                var status=$(val).find(".item-status").val();
                var discount=$(val).find(".item-dis").val();
                var input =$(val).find(".item-dis");
                var jianjia=$(val).find(".item-jianjia").val();
                var disprice=$(val).find(".item-disprice").val();
                var discountType=$(val).find('.item-discountType').val();
                item.id=itemCode;

                if(status==8) {
                    DisItem.submit.checkDiscount(input);
                    item.discount=discount;
                    item.jianjia=jianjia;
                    item.disprice=disprice;
                    item.discountType=discountType;
                    DisItem.row.addToSelectedItems(item);
                }
                else{
                    input.removeClass('error');
                    DisItem.row.removeFromSelectedItems(item);
                }
            })
        }
    }, DisItem.row);

    DisItem.submit = DisItem.submit || {};
    DisItem.submit = $.extend({
        doSub:function(){
            $('.StepBtn2').click(function(){
                var subString=DisItem.submit.subString();
                var activityString=$('.activityString').val();
                if($(".error").length > 0) {
                    alert("折扣范围（0.01--9.9）折，请修正错误再提交");
                    setTimeout(function() {
                        $("input.error").effect('bounce', {times: 3, distance: 30}, 1500);
                    }, 300);
                    return false;
                }
                if(selectItems.length==0){
                    alert("亲，请选择至少一个宝贝加入活动！！");
                    return false;
                }
                $.ajax({
                    url : '/TaoDiscount/addActivity',
                    data : {itemString:subString},
                    type : 'post',
                    success : function(data) {
                        if(data.res == null || data.res.length == 0){
                            var scrollTop = $(document).scrollTop();
                            var scrollLeft = $(document).scrollLeft();
                            if(data.msg=="0"){
                                var Div = $('#hraBox');
                                var Divtop = ($(window).height() - Div.height())/2;
                                var Divleft = ($(window).width() - Div.width())/2;
                                Div.css( { 'top' : Divtop + scrollTop, left : Divleft + scrollLeft } ).show();
                            }
                            else if(data.msg=="-1"){
                                alert("淘宝服务器忙，请过2分钟后重试！") ;
                            }
                            else if(!data.msg){
                                alert("活动创建成功！");
                                window.location.href ="/Sales/index";
                            }
                            else{
                                var limitDis=data.msg;
                                limitDis=limitDis/10;
                                var settingActModal = $('#settingActModal');
                                var settingActModaltop = ($(window).height() - settingActModal.height())/2;
                                var settingActModalleft = ($(window).width() - settingActModal.width())/2;
                                settingActModal.find('.orange').html(limitDis) ;
                                settingActModal.css( { 'top' : settingActModaltop + scrollTop, left : settingActModalleft + scrollLeft } ).show();
                            }
                        } else {
                            DisAct.error.showErrors(data.res);
                        }
                    }
                });
            })

        },
        subString : function(){
            var subString="";
            DisItem.row.setArrayString();
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
    }, DisItem.submit) ;

    TM.DisAct = TM.DisAct || {};

    var DisAct = TM.DisAct;

    DisAct.init = DisAct.init || {};
    DisAct.init = $.extend({
        doInit: function(container) {
            DisAct.container = container;
            //DisAct.search.doSearch();
            TM.ActivityAdmin.init.doInit(container);


            DisAct.init.doApidelete();
//            $.get('/OPUserInterFace/show5yuanXufei',function(data){
//                if(data == "show"){
//                    DisAct.init.showFirstXufei();
//                }
//            });
            DisAct.init.isNotTaobaoCeshi();
//            DisAct.init.doApideleteLimit();
//            DisAct.init.doApiDeleteByActivityId;
        } ,
        isNotTaobaoCeshi : function(){
            $.get("/status/discountName",function(){
                var nick=TM.name;
                if(nick === undefined || nick == null){
                    nick = "";
                }
                if(nick.indexOf("测试") >= 0){
                    return false;
                }
                DisAct.init.showFirstXufei();
                DisAct.init.showHaoPingOrNor();
            });
        } ,
        showHaoPingOrNor: function(){
            $.get('/OPUserInterFace/show5XingHaoPing',function(res){
                if(res == 'show'){
                    $.get('/OPUserInterFace/haopingshowed',function(data){
                        if(data == "unshowed") {
                            $.get('/status/user',function(data){
                                var firstlogintime = TM.firstLoginTime;
                                var now = new Date().getTime();
                                var interval = now - firstlogintime;
                                if(interval > 7*24*3600*1000){
                                    var html = ''+
                                        '<table style="z-index: 1001;width: 750px;height: 350px;background: url(http://img02.taobaocdn.com/imgextra/i2/1039626382/T2ViPDXHpXXXXXXXXX-1039626382.png);position: fixed;_position:absolute;">' +
                                        '<tbody>' +
                                        '<tr style="height: 270px;width: 750px;">' +
                                        '<td style="width: 550px;">' +
                                        '<a target="_blank" href="http://fuwu.taobao.com/serv/manage_service.htm?spm=a1z13.1113649.0.0.wRvp1i&service_code=ts-11477">' +
                                        '<div style="cursor: pointer;width: 550px;height: 270px;"></div>'+
                                        '</a>'+
                                        '</td>'+
                                        '<td style="width: 200px;">' +
                                        '<a target="_blank" href="http://fuwu.taobao.com/serv/manage_service.htm?spm=a1z13.1113649.0.0.wRvp1i&service_code=ts-11477">' +
                                        '<div style="cursor: pointer;width: 200px;height: 270px;"></div>'+
                                        '</a>'+
                                        '</td>'+
                                        '</tr>' +
                                        '<tr style="height: 80px;width: 750px;">' +
                                        '<td style="width: 550px">' +
                                        '<a target="_blank" href="http://fuwu.taobao.com/serv/manage_service.htm?spm=a1z13.1113649.0.0.wRvp1i&service_code=ts-11477">' +
                                        '<div style="cursor: pointer;width: 200px;height: 80px;"></div>'+
                                        '</a>'+
                                        '</td>'+
                                        '<td class="" style="width: 200px;cursor: pointer;">' +
                                        '<a style="margin-left: 110px;" target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid=%E4%B8%8A%E5%AE%98_%E5%B0%8F%E9%9B%AA&siteid=cntaobao&status=1&charset=utf-8"><img border="0" src="http://img04.taobaocdn.com/tps/i4/T1uUG.XjtkXXcb2gzo-77-19.gif" alt="联系售后"></a>' +
                                        '</td>'+
                                        '</tr>' +
                                        '</tbody>'+
                                        '</table>'+
                                        '';
                                    var left = ($(document).width() - 750)/2;
                                    var top = 130;
                                    var content = $(html);
                                    content.css('top',top+"px");
                                    content.css('left',left+"px");
                                    content.unbind('click').click(function(){
                                        content.remove();
                                        $('body').unmask();
                                    });
                                    $('body').mask();
                                    $('body').append(content);
                                    $.post('/OPUserInterFace/setHaoPingShowed',function(data){

                                    });
                                }
                            });
                        }
                    });

                }
            });
        },
        showFirstXufei : function(){
            $.get('/OPUserInterFace/show5yuanXufei',function(res){
                if(res == "show") {
                    $.get('/OPUserInterFace/fiveyuanshowed',function(data){
                        if(data == 'unshowed'){
                            var link = "http://tb.cn/A0c5eTy";
                            //var link = "http://to.taobao.com/ZEIb3gy";

                            var left = ($(document).width() - 500)/2;
                            var top = 130;
                            var html = '<div style="z-index: 19000;position: absolute;" class="five-yuan-xufei-img-dialog">' +
                                '<a target="_blank" href="'+link+'">' +
                                //'<img src="/img/dazhe/xx.jpg" style="width: 500px;height: 330px;">' +
                                '<img src="http://img04.taobaocdn.com/imgextra/i4/1132351118/T2k3ASXkXXXXXXXXXX_!!1132351118.jpg" style="width: 500px;height: 330px;">' +
                                '</a>' +
                                '<span class="inlineblock close-five-yuan-dialog" style="width: 40px;height: 40px;position: absolute;top:0px;right: 0px;z-index: 19100;">' +
                                '</span>' +
                                '</div>';
                            $('.five-yuan-xufei-img-dialog').remove();
                            var five_yuan = $(html);
                            if($.browser.msie) {
                                five_yuan.find('.close-five-yuan-dialog').css('filter',"alpha(opacity=10)");
                                five_yuan.find('.close-five-yuan-dialog').css('background-color',"#D53A04");
                            }
                            five_yuan.css('top',top+"px");
                            five_yuan.css('left',left+"px");
                            five_yuan.find('.close-five-yuan-dialog').click(function(){
                                five_yuan.remove();
                                $('body').unmask();
                            });five_yuan.appendTo($('body'));
                            $('body').mask();

                            // no show any more
                            $.get('/OPUserInterFace/set5YuanXufeiShowed',function(data){
                                return true;
                            });
                        }
                    });
                }

            });
//            $.get("/OPUserInterFace/fiveyuanshowed",function(data){
//                if(data == "unshowed"){
//                    var hour = new Date().getHours();
//                    var remain;
//                    if(hour <= 8){
//                        remain = 12;
//                    } else if(hour <= 12){
//                        remain = 9;
//                    } else if(hour <= 16){
//                        remain = 6;
//                    } else if(hour <= 20){
//                        remain = 3;
//                    } else if(hour <= 22){
//                        remain = 2;
//                    } else {
//                        remain = 1;
//                    }
//                        var html = '' +
//                            '<p style="font-size: 60px;color: red;font-weight: bold;margin: 20px 0px;text-align: center">年中大促</p>' +
//                            '<p style="font-size:50px;font-weight: bold;margin: 20px 0px;text-align: center">恭喜您获得5元续费一个月 奖励</p>' +
//                            '<p style="font-size:50px;font-weight: bold;margin: 20px 0px;text-align: center">点击以下链接获取哦亲</p>' +
//                            '<p style="margin: 20px 0px;text-align: center" class="free-link"><a target="_blank" href="http://to.taobao.com/FHZMkgy"><span class="free-link" style="font-size: 35px;font-weight: bold;">http://to.taobao.com/FHZMkgy</span></a></p>' +
//                            '<p style="text-align: center">备注：此活动每天仅限15人，剩余<span class="remain" style="font-size: 60px;font-weight: bold;">' + remain + '</span>人</p>' +
//                            '';
//                        var content = $(html);
//                        /*content.find('.free-link').click(function(){
//                         $('.ui-dialog ').hide();
//                         $('.ui-widget-overlay').hide();
//                         });*/
//                        var redshow = function () {
//                            content.find('.remain').toggleClass('red');
//                            content.find('.free-link').toggleClass('red');
//                        }
//                        setInterval(redshow, 300);
//                        TM.Alert.loadDetail(content, 800, 550, function () {
//                            return true;
//                        }, "年中大促")
//                    // no show any more
//                    $.get('/OPUserInterFace/set5YuanXufeiShowed',function(data){
//                        return true;
//                    });
//                }
//            });
        },
        showthreeyuandinggou : function(){
            $.get('/OPUserInterFace/show3yuanXufei',function(data){
                if(data == "show") {
                    $.get('/OPUserInterFace/threeyuanshowed',function(data){
                        if(data == 'unshowed'){
                            var link = "http://fuwu.taobao.com/ser/plan/planSubDetail.htm?planId=12651&isHidden=true&cycle=1&itemIds=2971";
                            var left = ($(document).width() - 500)/2;
                            var top = 130;
                            var html = '<table class="three-yuan-xufei-img-dialog" style="position: absolute;z-index: 19000;width: 500px;height: 330px;background: url(http://img04.taobaocdn.com/imgextra/i4/1132351118/T2cB4hXrlaXXXXXXXX_!!1132351118.gif)"><tbody>' +
                                '<tr style="height: 40px;"><td style="width: 460px;"></td><td style="width: 40px;"><span class="inlineblock close-three-yuan-dialog" style="width: 40px;height: 40px;position: absolute;top:0px;right: 0px;z-index: 19100;"></td></tr>' +
                                '<tr style="height: 290px;"><td style="width: 460px;vertical-align: top;"><a class="inlineblock" style="position: absolute;width: 460px;height: 290px;" target="_blank" href="'+link+'"></a></td><td style="width: 40px;"></td></tr>'+
                                '</tbody></table>';
                            $('.three-yuan-xufei-img-dialog').remove();
                            var three_yuan = $(html);
                            if($.browser.msie) {
                                three_yuan.find('.close-five-yuan-dialog').css('filter',"alpha(opacity=10)");
                                three_yuan.find('.close-five-yuan-dialog').css('background-color',"#D53A04");
                            }
                            three_yuan.css('top',top+"px");
                            three_yuan.css('left',left+"px");
                            three_yuan.find('.close-three-yuan-dialog').click(function(){
                                three_yuan.remove();
                                $('body').unmask();
                            });
                            three_yuan.appendTo($('body'));
                            $('body').mask();

                            $.post('/OPUserInterFace/set3YuanXufeiShowed',function(data){

                            });
                        }
                    })
                }
            })
        },
        doApidelete:function(){
            $('.deleteApiBtn').click(function(){
                if(confirm('确实要删除该内容吗?')){
                    $.ajax({
                        url : '/TaoDiscount/debugApiDelete',
                        data : {},
                        type : 'post',
                        success : function(data) {
                            alert(data.msg);
                            location.reload();
                        }
                    });
                }
            })
        },
        doApideleteLimit:function(){
            var promotionId= 879105756;
            $.ajax({
                url : '/TaoDiscount/debugApiDeleteLimit',
                data : {promotionId:promotionId},
                type : 'post',
                success : function(data) {
                    alert(data.msg);
                }
            });
        },
        doApiDeleteByActivityId:function(){
            var activityId=26619;
            $.ajax({
                url : '/TaoDiscount/debugApiDeleteByActivityId',
                data : {activityId:activityId},
                type : 'post',
                success : function(data) {
                    alert(data.msg);
                }
            });
        }

    },DisAct.init);

    DisAct.search =DisAct.search || {};
    DisAct.search= $.extend({
        doSearch: function() {
            DisAct.search.doShow(1);
            DisAct.search.doactive();
        },
        doShow: function(isactive) {
            var tbodyObj = DisAct.container.find(".item-table").find("tbody");
            DisAct.container.find(".paging-div").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: {isactive:isactive},
                    dataType: 'json',
                    url: '/taodiscount/listActivity',
                    callback: function(dataJson){
                        DisAct.container.html();
                        if(dataJson.res==null||dataJson.res.length==0){

                            DisAct.container.find(".f-chuangjian .new-activity").show();
                            DisAct.container.find(".f-chuangjian .f-huodong").show();
                            DisAct.container.find(".f-chuangjian .item-table").hide();
                            DisAct.container.find(".f-chuangjian .paging-div").hide();
                        }
                        else{
                            DisAct.container.find(".f-chuangjian .new-activity").hide();
                            DisAct.container.find(".f-chuangjian .f-huodong").hide();
                            DisAct.container.find(".f-chuangjian .item-table").show();
                            DisAct.container.find(".f-chuangjian .paging-div").show();
                            tbodyObj.html("");
                            var itemArray = dataJson.res;
                            $(itemArray).each(function(index, itemJson) {
                                var trObj = DisAct.row.createRow(index, itemJson);
                                tbodyObj.append(trObj);
                            });
                        }
                    }
                }
            });

        } ,
        doactive : function(){
            $('.isactive').attr("value",1) ;
            var going=$('.going');
            going.addClass("active");
            var ended=$('.ended');
            going.click(function(){
                going.addClass("active");
                ended.removeClass("active");
                $('.isactive').attr("value",1);
                DisAct.search.doShow(1);
            })
            ended.click(function(){
                ended.addClass("active");
                going.removeClass("active");
                $('.isactive').attr("value",0);
                DisAct.search.doShow(0);
            })

        }


    },DisAct.search);

    DisAct.row = DisAct.row || {};
    DisAct.row = $.extend({
        createRow: function(index, itemJson) {
            var html = DisAct.row.createHtml(itemJson);
            var trObj = $(html);

            trObj.find(".item-name").html(itemJson.activityDescription);
            var beginTime = DisAct.row.parseLongToDate(itemJson.activityStartTime) ;
            var endTime =   DisAct.row.parseLongToDate(itemJson.activityEndTime) ;
            trObj.find(".item-beginTime").html(beginTime);
            trObj.find(".item-endTime").html(endTime);

            var refreshCallback = function() {
                DisAct.search.doSearch();
            };

            var html1 = '' +
                '<a  href="javascript:;" class="haoniu-base-btn reviseItem">修改商品</a>' +
                '<a  href="javascript:;" class="haoniu-base-btn addItem">添加商品</a>' +
                //'<a  href="javascript:;" class="haoniu-base-btn reviseAct">修改活动信息</a>' +
                //'<a  href="javascript:;" class="haoniu-base-btn deleteAct">结束活动</a>' +
                '<a href="javascript:void(0);" class="haoniu-base-btn activity-more-op-btn">更多操作>></a>' +
                '' +
                '';

            var activeHtml = '';
            if (TM.ActivityList.util.isOldActivity(itemJson) == true || TM.ActivityList.util.isDiscountActivity(itemJson) == true
                || TM.ActivityList.util.isMjsActivity(itemJson) == true) {

                activeHtml = '' +
                    '<a  href="javascript:;" class="haoniu-base-btn reviseItem">修改商品</a>' +
                    '<a  href="javascript:;" class="haoniu-base-btn addItem">添加商品</a>' +
                    '<a href="javascript:void(0);" class="haoniu-base-btn activity-more-op-btn">更多操作>></a>' +
                    '' +
                    '';
            } else if (TM.ActivityList.util.isShopMjsActivity(itemJson) == true
                || TM.ActivityList.util.isShopDiscountActivity(itemJson) == true) {

                activeHtml = '' +
                    '<a  href="javascript:;" class="haoniu-base-btn reviseAct">修改活动</a>' +
                    '<a  href="javascript:;" class="haoniu-base-btn deleteAct">结束活动</a>';

                if (TM.ActivityList.util.isShopMjsActivity(itemJson) == true) {
                    activeHtml += '<a href="javascript:void(0);" class="haoniu-base-btn activity-more-op-btn">更多操作>></a>' +
                        '' +
                        '';
                }

            }


            var unActiveHtml ='<a  href="javascript:;" class="haoniu-base-btn deleteUnAct">删除记录</a>';

            if (TM.ActivityList.util.isDiscountActivity(itemJson) == true || TM.ActivityList.util.isMjsActivity(itemJson) == true
                || TM.ActivityList.util.isShopMjsActivity(itemJson) == true
                || TM.ActivityList.util.isShopDiscountActivity(itemJson) == true) {
                unActiveHtml = '' +
                    '<a  href="javascript:;" class="haoniu-base-btn restart-activity-btn" style="margin-right: 10px;">重启活动</a>' +
                    unActiveHtml +
                    '';
            }

            var isactive = $('.isactive').val();
            if(isactive == 1) {
                trObj.find(".op-td").html(activeHtml);
            }
            else{
                trObj.find(".op-td").html(unActiveHtml);
            }


            TM.ActivityList.row.addMoreOpBtnsDiv(itemJson, trObj);


            if (TM.ActivityList.util.isOldActivity(itemJson) == true) {
                trObj.find('.activity-type-span').html('[打折]');
                trObj.find(".reviseItem").click(function() {
                    var href="/taodiscount/revise_item?activityId="+itemJson.id;
                    trObj.find(".reviseItem").attr("href", href);
//                trObj.find(".reviseItem").attr("target","_blank");
                });
                trObj.find(".addItem").unbind().click(function(){
                    var href="/taodiscount/add_Item?activityId="+itemJson.id;
                    trObj.find(".addItem").attr("href", href);
//                trObj.find(".addItem").attr("target","_blank");
                }) ;
                trObj.find(".reviseAct").unbind().click(function(){
                    var href="/taodiscount/revise_Act?activityId="+itemJson.id;
                    trObj.find(".reviseAct").attr("href", href);
//                trObj.find(".reviseAct").attr("target","_blank");
                }) ;
                trObj.find(".deleteAct").unbind().click(function(){
                    if(confirm('确实要删除该内容吗?')){
                        $.ajax({
                            url : '/taodiscount/deleteActivity',
                            data : {id:itemJson.id},
                            type : 'post',
                            success : function(data) {
                                if(data == null || data.length == 0){
                                    alert("删除活动成功");
                                    location.reload();
                                }
                                else {
                                    alert(data.msg);

                                }

                            }
                        });
                    }
                }) ;
                trObj.find(".deleteUnAct").unbind().click(function(){
                    $.ajax({
                        url : '/taodiscount/deleteUnactive',
                        data : {id:itemJson.id},
                        type : 'post',
                        success : function(data) {
                            if(data == null || data.length == 0){
                                alert("删除活动成功");
                                location.reload();
                            }
                            else {
                                alert(data.msg);
                            }
                        }
                    });
                }) ;
            } else {
                TM.ActivityList.event.initNewActivityEvent(itemJson, trObj);
            }


            return trObj;
        },
        createHtml: function(itemJson) {

            var html = '' +

                '<tr item-id="'+itemJson.id+'">' +
                '   <td class="result-td"><span style="color: #a10000;" class="activity-type-span"></span>&nbsp;' + itemJson.activityTitle + ' </td> ' +
                '   <td class="result-td"><span class="item-href item-link item-name"></span></td>' +

                '   <td class="result-td"><span class="item-beginTime" style=""></span></td>'+
                '   <td class="result-td"><span class="item-endTime" style=""></span></td>'+
                //'   </td class="result-td">' +
                '   <td class="result-td op-td">' +
                '       ' +
                '   </td> ' +
                '</tr>' +
                '';
            return html;
        }  ,

        parseLongToDate:function(ts) {
            var theDate = new Date();
            theDate.setTime(ts);
            var year = theDate.getFullYear();
            var month = theDate.getMonth() + 1;//js从0开始取
            var date = theDate.getDate();
            var hour = theDate.getHours();
            var minutes = theDate.getMinutes();
            var second = theDate.getSeconds();

            if (month < 10) {
                month = "0" + month;
            }
            if (date < 10) {
                date = "0" + date;
            }
            if (hour < 10) {
                hour = "0" + hour;
            }
            if (minutes < 10) {
                minutes = "0" + minutes;
            }
            if (second < 10) {
                second = "0" + second;
            }
            var timeStr = month+"月"+date+"日 "+hour+":"+minutes;//+ ":" + second;
            return timeStr;
        }

    }, DisAct.row);

    /**
     * 操作失败的日志
     * @type {*}
     */
    DisAct.error = DisAct.error || {};
    DisAct.error = $.extend({
        showErrors: function(errorJsonArray) {

            var html='' +
                '<div class="error-item-div busSearch" style="margin-top: 10px;">' +
                '   <span class="error-tip-span">宝贝操作失败列表：</span> ' +
                '   <table class="error-item-table list-table">' +
                '       <thead>' +
                '       <tr>' +
                '           <td style="width: 15%;">宝贝图片</td>' +
                '           <td style="width: 35%;">标题</td>' +
                '           <td style="width: 30%;">失败说明</td>' +
                '           <td style="width: 15%;">操作时间</td>' +
                '       </tr>' +
                '       </thead>' +
                '       <tbody></tbody>' +
                '   </table> ' +
                '</div>' +
                '';
            var dialogObj = $(html);

            $(errorJsonArray).each(function(index, errorJson) {
                var trObj = DisAct.error.createRow(index, errorJson);
                dialogObj.find(".error-item-table").find("tbody").append(trObj);
            });

            $("body").append(dialogObj);
            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:500,
                width:780,
                title:'宝贝错误列表',
                autoOpen: false,
                resizable: false,
                buttons:{'返回活动列表':function() {
                    window.location.href="/Sales/index";
                },'取消':function(){
                    location.reload();
                    $(this).dialog('close');
                }}
            });

            dialogObj.dialog("open");

        },
        createRow: function(index, errorJson) {
            var itemJson = errorJson.item;
            var opstatus = errorJson.opstatus;
            var html = '' +
                '<tr>' +
                '   <td><a class="item-link" target="_blank"><img class="item-img" style="width: 60px;height: 60px;" /> </a> </td>' +
                '   <td><a class="item-link" target="_blank"><span class="item-title"></span> </a> </td>' +
                '   <td><span class="error-intro"></span> </td>' +
                '   <td><span class="op-time"></span> </td>' +
                '</tr>' +
                '' +
                '' +
                '';
            var trObj = $(html);
            var url = "http://auction2.paipai.com/" + itemJson.numIid;
            trObj.find(".item-link").attr("href", url);
            trObj.find(".item-img").attr("src", itemJson.picURL);
            trObj.find(".item-title").html(itemJson.title);
            trObj.find(".error-intro").html(opstatus.opMsg);

            var theDate = new Date();
            var year = theDate.getFullYear();
            var month = theDate.getMonth() + 1;//js从0开始取
            var date = theDate.getDate();
            var hour = theDate.getHours();
            var minutes = theDate.getMinutes();
            var second = theDate.getSeconds();

            if (month < 10) {
                month = "0" + month;
            }
            if (date < 10) {
                date = "0" + date;
            }
            if (hour < 10) {
                hour = "0" + hour;
            }
            if (minutes < 10) {
                minutes = "0" + minutes;
            }
            if (second < 10) {
                second = "0" + second;
            }
            var timeStr = year + "-" + month+"-"+date+" "+hour+":"+minutes+ ":" + second;
            trObj.find(".op-time").html(timeStr);

            return trObj;
        }
    }, DisAct.error);



})(jQuery,window));

