TM.MyUtil = TM.MyUtil || {};
((function ($, window) {

    /**
     * namespace
     * @type {*}
     */
    var MyUtil = TM.MyUtil;
    /**
     * 添加改价任务时的飞入效果
     * @type {*}
     */
    MyUtil.Fly = MyUtil.Fly || {};
    MyUtil.Fly = $.extend({
        // user like this
        // TM.MyUtil.Fly.fly($(this));
        fly : function($this){
            var start = {}, end = {};
            start.left = $this.offset().left+"px";
            start.top = $this.offset().top+"px";
            end.left = $('#task-going-count').offset().left+"px";
            end.top = $('#task-going-count').offset().top+"px";
            TM.MyUtil.Fly.flyFromTo(start,end,function(){$('#task-going-count').html(parseInt($('#task-going-count').html())+1)})
        },
        flyFromTo : function(start,end,callback){
            $('#fly-from-to-img').remove();
            var img = $('<img id="fly-from-to-img" src="/img/favicon.ico" style="z-index:200001;width: 16px;height: 16px;position: absolute;top:'+start.top+';left: '+start.left+'"/>');
            img.appendTo($('body'));

            img.animate({top:end.top,left:end.left},1500, function(){
                img.fadeOut(1000);
                callback && callback();
            });
        },
        centerFly: function() {
            var start = {};
            var end = {};

            var windowWidth = $(window).width();
            var windowHeight = $(window).height();
            var scrollLeft = $(document).scrollLeft();
            var scrollTop = $(document).scrollTop();

            var left = (windowWidth) / 2 + scrollLeft;
            var top = (windowHeight) / 2 + scrollTop;
            start.left = left + "px";
            start.top = top + "px";

            end.left = $('#task-going-count').offset().left+"px";
            end.top = $('#task-going-count').offset().top+"px";
            TM.MyUtil.Fly.flyFromTo(start,end,function(){$('#task-going-count').html(parseInt($('#task-going-count').html())+1)})
        }
    },MyUtil.Fly);

    MyUtil.common = MyUtil.common || {};

    /**
     * 初始化
     * @type {*}
     */
    MyUtil.common = $.extend({
        //设置标题
        setTitle: function(title) {
            /*$("#title-wrapper").html("");
             var html = '<div class="busTitleDiv">' +
             '<span class="busTitleSpan"></span>' +
             '</div>';
             var titleObj = $(html);
             titleObj.find(".busTitleSpan").html(title);
             $("#title-wrapper").append(titleObj);*/
        },
        //判断ajax返回结果
        judgeAjaxResult: function(dataJson) {

            var msg = dataJson.message;
            if (msg === undefined || msg == null || msg == "")
                ;
            else
                alert(msg);
            return dataJson.success;
        },
        ajaxShowLoading: function() {
            //TM.Loading.init.show();
        },
        ajaxHideLoading: function() {
            //TM.Loading.init.hidden();
        },
        //显示有多少推广计划
        showPlanNum: function(planJsonArray) {
            var spreadPlanNum = 0;
            $(planJsonArray).each(function(index, planJson) {
                if (MyUtil.common.isSpread(planJson) == true) {
                    spreadPlanNum++;
                }
            });
            var length = planJsonArray.length;
            $(".totalPlanNum").html(length);
            $(".spreadPlanNum").html(spreadPlanNum);
        },
        //判断一个计划是否推广了
        isSpread: function(planJson) {
            if (planJson.onlineStatus == "ON")
                return true;
            else
                return false;
        },
        //将数字转成百分数
        formatToPercent: function(num){
            return Math.round(num * 10000)/100 + '%';
            //return num.toPercent(2);
        }
    }, MyUtil.common);

    MyUtil.maxPrice = MyUtil.maxPrice || {};
    MyUtil.maxPrice = $.extend({
        setRebirthCidMaxPrice: function(targetObj) {
            $.ajax({
                type: "post",
                url: "/EXMaxPrice/getRebirthCidMaxPrice",
                data: {},
                success: function(dataJson){
                    if (MyUtil.common.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    targetObj.val(dataJson.results / 100);
                }
            });
        },
        //低价引流的最高价
        setLowCidMaxPrice: function(targetObj) {
            $.ajax({
                type: "post",
                url: "/EXMaxPrice/getLowPriceCidMaxPrice",
                data: {},
                success: function(dataJson){
                    if (MyUtil.common.judgeAjaxResult(dataJson) == false) {
                        return;
                    }

                    targetObj.val(dataJson.results / 100);
                }
            });
        },
        setCidDefaultPrice: function(targetObj) {
            $.ajax({
                type: "post",
                url: "/EXMaxPrice/getLowPriceCidDefaultPrice",
                data: {},
                success: function(dataJson){
                    if (MyUtil.common.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    targetObj.val(dataJson.results / 100);
                }
            });
        },
        isUserSetDefaultPrice: function() {
            return false;
        },
        getResultDefaultPrice: function(maxPrice) {
            if (maxPrice === undefined || maxPrice == null || isNaN(maxPrice) || maxPrice < 0.05) {
                alert("系统出现异常，默认出价出错！");
                return 0.05;
            }
            var defaultPrice = maxPrice * 0.9;
            if (defaultPrice < 0.05) {
                defaultPrice = 0.05;
            }
            if (defaultPrice > maxPrice) {
                defaultPrice = maxPrice;
            }
            return defaultPrice;
        },
        getMaxPriceConfirm: function(maxPrice) {
            if (maxPrice === undefined || maxPrice == null || isNaN(maxPrice) || maxPrice < 0.05) {
                alert("系统出现异常，最高出价出错！");
                return "";
            }
            if (maxPrice < 2) {
                return "";
            } else if (maxPrice < 3) {
                return "您设置的关键词最高价超过了2元，"
            } else if (maxPrice < 4) {
                return "您设置的关键词最高价超过了3元，"
            } else if (maxPrice < 5) {
                return "您设置的关键词最高价超过了4元，"
            } else if (maxPrice < 6) {
                return "您设置的关键词最高价超过了5元，"
            } else {
                return "您设置的关键词最高价超过了6元，"
            }
        }
    }, MyUtil.maxPrice);


    MyUtil.adgroupNum = MyUtil.adgroupNum || {};
    MyUtil.adgroupNum = $.extend({
        setLowPriceMaxAdgroupNum: function(targetObj) {
            $.ajax({
                type: "post",
                url: "/EXAdgroupAdd/getLowPriceMaxAdgroupNum",
                data: {},
                success: function(dataJson){
                    if (MyUtil.common.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    targetObj.html(dataJson.results);
                }
            });
        },
        setRebirthMaxAdgroupNum: function(targetObj) {
            $.ajax({
                type: "post",
                url: "/EXAdgroupAdd/getRebirthMaxAdgroupNum",
                data: {},
                success: function(dataJson){
                    if (MyUtil.common.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    targetObj.html(dataJson.results);
                }
            });
        }
    }, MyUtil.adgroupNum);


    MyUtil.chedao = MyUtil.chedao || {};
    MyUtil.chedao = $.extend({
        doSetCampaignType: function(campaignType, minVersion, planJson, priceJson, refeshCallback) {
            if (TM.UserVersion.init.judgeTargetVersion(minVersion) == false) {

                return;
            }

            var ajaxSetCallback = function(maxPrice, defaultPrice, isResetWordPrice, callback) {
                var data = {};
                data.campaignId = planJson.campaignId;
                data.campaignType = campaignType;
                data.maxPrice = maxPrice;
                data.defaultPrice = defaultPrice;
                data.isResetKeyword = isResetWordPrice;
                $.ajax({
                    type: "post",
                    url: "/EXCampaignAdmin/setCampaignType",
                    data: data,
                    success: function(dataJson){
                        if (!TM.MyUtil.common.judgeAjaxResult(dataJson))
                            return;

                        var finishCallback = function() {
                            if (callback === undefined || callback == null) {

                            } else {
                                callback();
                            }
                            if (refeshCallback === undefined || refeshCallback == null) {

                            } else {
                                refeshCallback();
                            }

                        }

                        if (campaignType <= 0) {
                            finishCallback();
                        } else {

                            if (isResetWordPrice == true) {
                                /*$.ajax({
                                 type: "post",
                                 url: "/EXCampaignAdd/doUpdateCampaignKeyword",
                                 data: {campaignId: planJson.campaignId},
                                 success: function(dataJson){
                                 if (!TM.MyUtil.common.judgeAjaxResult(dataJson))
                                 return;
                                 var logJson = dataJson.results;
                                 if (logJson.useTask == true) {

                                 var taskConfirm = '系统已为您提交了修改关键词出价的<span style="color:#a10000;">后台任务</span>，是否立即进入<span style="color:#a10000;">任务中心</span>查看？';
                                 TM.Alert.confirmDialog(taskConfirm, "提示", 400, 250, function(){
                                 location.href = "/extask/index";
                                 }, function() {
                                 finishCallback();
                                 return;
                                 });

                                 TM.MyUtil.Fly.centerFly();


                                 } else {
                                 if (logJson.totalNum == logJson.successNum) {
                                 alert("宝贝关键词修改成功！");

                                 } else {
                                 alert("宝贝关键词修改成功" + logJson.successNum + "个，失败" + (logJson.totalNum - logJson.successNum) + "个。");

                                 }

                                 finishCallback();
                                 return;
                                 }


                                 }

                                 });*/


                                var taskConfirm = '系统已为您提交了修改关键词出价的<span style="color:#a10000;">后台任务</span>，是否立即进入<span style="color:#a10000;">任务中心</span>查看？';
                                TM.Alert.confirmDialog(taskConfirm, "提示", 400, 250, function(){
                                    location.href = "/extask/index";
                                }, function() {
                                    finishCallback();
                                    return;
                                });

                                TM.MyUtil.Fly.centerFly();


                            } else {
                                finishCallback();
                                return;
                            }


                            /*
                             var confirmHtml = '是否立即修改计划中所有宝贝的关键词？如确定，则计划中<span style="color: #a10000;">所有关键词的出价都会被改变，请谨慎确定！</span> ';

                             TM.Alert.confirmDialog(confirmHtml, "提示", 400, 300, function(dialogObj){

                             if (confirm("为防止误操作，请您再确认是否重置所有关键词的出价和匹配模式？") == false) {
                             finishCallback();
                             return;
                             }
                             dialogObj.dialog("close");



                             }, function() {
                             finishCallback();
                             return;
                             });
                             */
                        }


                    }
                });
            }

            if (campaignType <= 0) {
                if (confirm("确定要取消车道专属计划？") == false) {
                    return;
                }
                ajaxSetCallback(0, 0, false, function() {});
            } else {
                $(".set-campaign-type-div").remove();

                var html = '' +
                    '<div class="set-campaign-type-div" style="text-align: center;">' +
                    '   <div style="height: 40px;"></div> ' +
                    '   <div style="color: #a10000;text-align: left;">如果您选择了重置关键词出价，所有关键词的出价都将会&nbsp;<span style="font-weight: bold;">根据最高价重新调整</span>；</div>' +
                    '   <div style="color: #a10000;padding-top: 10px;padding-bottom: 30px;text-align: left;">如果不选择重置，则原关键词出价&nbsp;<span style="font-weight: bold;">将保持不变</span>。</div>' +
                    '   <table style="margin: 0 auto;" class="set-campaign-type-table">' +
                    '       <tbody>' +
                    '       <tr>' +
                    '           <td><span style="font-weight: bold;">请先输入关键词最高出价：</span></td>' +
                    '           <td colspan="2" style="padding-left: 10px;"><input class="max-price-text" style="width: 100px" value="" /> 元 </td>' +
                    '       </tr>' +
                    '       <tr class="default-price-tr">' +
                    '           <td><span style="font-weight: bold;">请先输入关键词默认出价：</span></td>' +
                    '           <td><input class="default-price-text" style="width: 100px" value="" /> 元 </td>' +
                    '       </tr>' +
                    '       <tr>' +
                    '           <td><span style="font-weight: bold;">是否立即重置关键词出价：</span></td>' +
                    '           <td style="text-align: left;"><input type="radio" name="reset-price-radio" class="reset-price-radio do-reset" value="" /><span class="radio-span">是，我要立即重置关键词出价</span> </td>' +
                    '       </tr>' +
                    '       <tr>' +
                    '           <td>&nbsp;</td>' +
                    '           <td style="text-align: left;"><input type="radio" name="reset-price-radio" class="reset-price-radio" /><span class="radio-span">否，我要保持关键词原价</span> </td>' +
                    '       </tr>' +
                    '       </tbody>' +
                    '   </table>' +
                    '</div> ' +
                    '';

                var dialogObj = $(html);

                dialogObj.find(".radio-span").click(function() {
                    $(this).parent().find("input").click();
                });

                if (!(priceJson === undefined || priceJson == null) && priceJson.maxPrice > 0) {
                    dialogObj.find(".max-price-text").val(priceJson.maxPrice/ 100);
                } else {
                    if (campaignType == 2 || campaignType == 4) {
                        TM.MyUtil.maxPrice.setRebirthCidMaxPrice(dialogObj.find(".max-price-text"));
                    } else {
                        TM.MyUtil.maxPrice.setLowCidMaxPrice(dialogObj.find(".max-price-text"));
                    }
                }
                if (!(priceJson === undefined || priceJson == null) && priceJson.defaultPrice > 0) {
                    dialogObj.find(".default-price-text").val(priceJson.defaultPrice / 100);
                } else {
                    TM.MyUtil.maxPrice.setCidDefaultPrice(dialogObj.find(".default-price-text"));
                }

                if (campaignType == 2 || campaignType == 4) {
                    dialogObj.find(".default-price-tr").remove();
                }

                if (TM.MyUtil.maxPrice.isUserSetDefaultPrice() == false) {
                    dialogObj.find(".default-price-tr").remove();
                }

                dialogObj.dialog({
                    modal: true,
                    bgiframe: true,
                    height:400,
                    width:600,
                    title:'车道计划托管',
                    autoOpen: true,
                    resizable: false,
                    buttons:{'确定':function() {
                        var maxPrice = $(this).find(".max-price-text").val();
                        var defaultPrice = $(this).find(".default-price-text").val();

                        if (maxPrice === undefined || maxPrice == null || maxPrice == "" || isNaN(maxPrice) || maxPrice < 0.05) {
                            alert("关键词最高价不能低于5分！");
                            return;
                        }

                        if (campaignType == 2 || campaignType == 4) {
                            defaultPrice = 0;
                        } else {
                            if (TM.MyUtil.maxPrice.isUserSetDefaultPrice() == false) {
                                defaultPrice = TM.MyUtil.maxPrice.getResultDefaultPrice(maxPrice);
                            }
                        }

                        var radioObj = $(this).find(".reset-price-radio:checked");

                        if (radioObj.length <= 0) {
                            alert("请先选择是否要重置关键词出价！");
                            return;
                        }

                        var isResetPrice = radioObj.hasClass("do-reset");

                        var priceConfirm = TM.MyUtil.maxPrice.getMaxPriceConfirm(maxPrice);

                        var obj = $(this);

                        if (isResetPrice == false) {
                            if (confirm(priceConfirm + "确定要设置托管计划？") == false) {
                                return;
                            }
                            ajaxSetCallback(maxPrice, defaultPrice, false, function() {
                                obj.dialog('close');
                            });
                        } else {
                            /*
                             var confirmHtml = priceConfirm + '确定要设置托管计划？您当前选择了<span style="color: #a10000;font-weight: bold;font-size: 16px;">重置</span>关键词出价，' +
                             '<span style="color: #a10000;">所有关键词的出价都将会被改变，请谨慎操作！！！</span>' +
                             '<br /><br /><br />' +
                             '点击确定，系统将提交托管；点击取消，操作将返回！';

                             TM.Alert.confirmDialog(confirmHtml, "提示", 400, 300, function(){
                             if (confirm("为防止误操作，请您再确认是否重置所有关键词的出价？") == false) {

                             return;
                             }

                             ajaxSetCallback(maxPrice, defaultPrice, true, function() {
                             obj.dialog('close');
                             });


                             }, function() {

                             return;
                             });
                             */


                            if (confirm(priceConfirm + "您当前选择了重置关键词出价，这将可能修改某些关键词出价，确定要设置托管计划？该操作不可恢复，请谨慎操作！！！") == false) {
                                return;
                            }
                            ajaxSetCallback(maxPrice, defaultPrice, true, function() {
                                obj.dialog('close');
                            });

                        }




                    },'取消':function(){
                        $(this).dialog('close');
                    }}
                });
                dialogObj.dialog("open");
            }

        }
    }, MyUtil.chedao);



    MyUtil.cleanCampaign = MyUtil.cleanCampaign || {};
    MyUtil.cleanCampaign = $.extend({
        doClean: function(campaignTitle, callback) {

            $(".verify-clean-campaign-dialog").remove();

            var html = '' +
                '<div style="padding: 20px 10px;">' +
                '确认清空推广计划：' + campaignTitle +
                '？<span style="color: #a10000;">(该计划下的推广组将会全被删除)</span> ' +
                '</div>' +
                '<div style="text-align: center;padding-top: 10px;">' +
                '   <div>为防止误操作，请先在下面的文本框中输入&nbsp;<span style="font-weight: bold; color: #a10000;">清空计划</span>&nbsp;这四个字</div>' +
                '   <div style="padding-top: 10px;"><input type="text" class="verify-clean-input" style="border: 1px solid #999; width: 120px;" /> </div> ' +
                '</div> ' +
                '' +
                '';

            var dialogObj = $('<div class="verify-clean-campaign-dialog"></div> ');
            dialogObj.html(html);


            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:300,
                width:420,
                title:'清空计划',
                autoOpen: true,
                resizable: false,
                buttons:{'确定':function() {

                    var confirmStr = dialogObj.find(".verify-clean-input").val();

                    if (confirmStr == "清空计划") {
                        if (confirm("确定要清空计划？") == false) {
                            return;
                        }
                        callback();
                        dialogObj.dialog('close');
                    } else {
                        alert("为防止误操作，请先在文本框中输入清空计划这四个字！")
                        return;
                    }


                },'取消':function(){
                    $(this).dialog('close');
                }}
            });

        }
    }, MyUtil.cleanCampaign);


    MyUtil.timeInterval = MyUtil.timeInterval || {};
    MyUtil.timeInterval = $.extend({
        setRptTimeText: function(startTimeObj, endTimeObj, interval, callback) {


            $.ajax({
                type: "post",
                url: "/EXCampaignAdmin/queryRptTimeText",
                data: {timeLength: interval},
                success: function(dataJson){
                    if (!TM.MyUtil.common.judgeAjaxResult(dataJson))
                        return;

                    var timeArray = dataJson.results;

                    startTimeObj.val(timeArray[0]);
                    endTimeObj.val(timeArray[1]);

                    callback();
                }

            });
        }
    }, MyUtil.timeInterval);


})(jQuery, window));