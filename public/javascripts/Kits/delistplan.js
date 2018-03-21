


var TM = TM || {};

TM.DelistPlan = TM.DelistPlan || {};



((function ($, window) {
    var me = TM.DelistPlan;

    TM.DelistPlan.init = function(isTaozhanggui){
        me.wrapper = $('.delistPlansWrapper');
        me.planBody = $("#plansTable tbody");

        TM.DelistPlan.initDelistStatus();

        TM.DelistPlan.loadPlans(isTaozhanggui);
        // TODO bind something....

        me.wrapper.find(".simple-delist-btn").click(function() {

            if (confirm("确定要创建均匀上架计划，创建后，在售宝贝将会被均匀分配到高峰时间段？") == false) {
                return;
            }

            $.ajax({
                //async : false,
                url : '/delistplan/createSimpleDelistPlan',
                data : {},
                type : 'post',
                error: function() {
                },
                success: function (dataJson) {
                    if (TM.DelistBase.util.judgeAjaxResult(dataJson) == false) {

                    } else {
                        alert("上下架计划创建成功！");
                    }


                    TM.DelistPlan.loadPlans(isTaozhanggui);

                    TM.DelistTime.init.doInit();
                }
            });

        });


        TM.DelistTime.init.doInit();


    }
    TM.DelistPlan.Status = {ON:1,OFF:2};

    TM.DelistPlan.initDelistStatus = function() {
        $.get('/autodelist/isOn',function(dataJson){

            var isOn = dataJson.res;

            var switchStatus = TM.Switch.createSwitch.createSwitchForm("上下架计划开启状态");

            switchStatus.find('input[name="auto_valuation"]').tzCheckbox({
                labels:['已开启','已关闭'],
                doChange: function(isCurrentOn){
                    if(!TM.isVip()){
                        TM.Alert.showVIPNeeded();
                        return;
                    }

                    if (isCurrentOn == false) {//要开启

                        $.ajax({
                            //async : false,
                            url : '/autodelist/simpleTurnOn',
                            data : {},
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

                            }
                        });
                    } else if (isCurrentOn == true) {//要关闭


                        $.ajax({
                            //async : false,
                            url : '/autodelist/simpleTurnOff',
                            data : {},
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

                            }
                        });


                    }
                    return true;
                },
                isOn : isOn
            });

            switchStatus.appendTo($('.auto-delist-status-div'));

        });
    }


    TM.DelistPlan.loadPlans = function(isTaozhanggui){
        if(isTaozhanggui == undefined || isTaozhanggui == null){
            isTaozhanggui = true;
        }
        var createHref = "";
        if(isTaozhanggui){
            createHref = "/kits/delistCreate";
        } else {
            createHref = "/autotitle/delistCreate";
        }
        $.get('/DelistPlan/queryDelistPlanList',function(dataJson){
            var list = dataJson.results;
            me.planBody.empty();
            if(!list || list.length == 0){
                // no result???
                me.planBody.append('<tr><td colspan="20"><a href="'+createHref+'" class="tmbtn too-long-blue-btn" style="font-size:17px;margin-left:10px;">创建自定义高级计划</a></td></tr>');
            }

            $.each(list,function(i, elem){
                if(elem.status == 1){
                }else{
                }
            });

            var rows = $('#planRowTmpl').tmpl(list);
            $.each(rows, function(i, elem){
                var oThis = $(this);
                var status=  oThis.attr('status');
                me.changeStatus($(elem),status);
            });
            rows.find('.startBtn').click(function(){
                var btn =    $(this);
                var planId = btn.attr("planId");
                $.post('/delistplan/turnOnPlan',{planId:planId},function(data){
                    if(data && data.success){
                        var row = btn.parent().parent();
                        me.changeStatus(row, me.Status.ON);
                    }else{
                        alert('系统出现了异常,您可以联系客服帮您解决');
                    }

                });
            });
            rows.find('.pauseBtn').click(function(){
                var btn =    $(this);
                var planId = btn.attr("planId");
                $.post('/delistplan/turnOffPlan',{planId:planId},function(data){
                    if(data&&data.success){
                        var row = btn.parent().parent();
                        me.changeStatus(row, me.Status.OFF);
                    }else{
                        alert('系统出现了异常,您可以联系客服帮您解决');
                    }

                });
            });

            rows.find('.deleteBtn').click(function(){
                if (confirm("确定要删除该上下架计划？") == false) {
                    return;
                }
                var btn =    $(this);
                var planId = btn.attr("planId");
                $.post('/delistplan/deletePlan',{planId:planId},function(data){
                    if(data && data.success){
                        var row = btn.parent().parent();
                        row.remove();
                        if(me.planBody.find('tr').length == 0){
                            me.planBody.append($('<tr><td colspan="20"><a href="/kits/delistCreate" class="tmbtn too-long-blue-btn" style="font-size:17px;margin-left:10px;">创建自定义高级计划</a></td></tr>'));
                        }
                        TM.DelistTime.init.doInit();
                    }else{
                        alert('系统出现了异常,您可以联系客服帮您解决');
                    }
                });
            });

            me.planBody.append(rows);
//            rows.find('.hello');
        });
    }
    TM.DelistPlan.changeStatus = function(row, status){
        var targetTxt = row.find('.statusTxt');
        if((status & me.Status.ON) > 0){
            // Now on....
            targetTxt.html('<span class="green">已开启</span>');
            row.find('.startBtn').addClass("hidden");
            row.find('.pauseBtn').removeClass("hidden");
        }else{
            targetTxt.html('<span class="red">未开启</span>');
            row.find('.pauseBtn').addClass("hidden");
            row.find('.startBtn').removeClass("hidden");
        }
        row.attr('status',status);
    }

})(jQuery,window));


TM.CreateDelist = TM.CreateDelist || {};

((function ($, window) {

    var me = TM.CreateDelist;

    TM.CreateDelist.init = function(canedit, isTaozhanggui){
        TM.CreateDelist.planId = 0;
        me.wrapper = $('.delistPlansWrapper');
        me.titleInput = $('#titleInput');
        me.titleInput.val('掌柜上架计划 '+ new Date().formatYMDHMS());
        me.itemCatContainer = $('#itemCatContainer');
        me.sellerCatContainer = $('#sellerCatConainer');

        me.step1 = me.wrapper.find('#step1');
        me.step2 = me.wrapper.find('#step2');
        me.step3Cat = me.wrapper.find('#step3-category');
        me.step3Manual = me.wrapper.find('#step3-manual');
        me.step4 = me.wrapper.find('#step4');
        me.step5 = me.wrapper.find('#step5');
        me.step6 = me.wrapper.find('#step6');

        me.initSteps(canedit, isTaozhanggui);
        me.initCats();
    }

    TM.CreateDelist.initSteps = function(canedit, isTaozhanggui){
        me.step1.show();

        me.initStep1(isTaozhanggui);
        me.initStep2();
        me.initStep3Category();
        me.initStep3Manual();
        me.initStep4(canedit);
        me.initStep5();
//        me.initStep6();

    }

    TM.CreateDelist.initStep1 = function(isTaozhanggui){
        if(isTaozhanggui == undefined || isTaozhanggui == null){
            isTaozhanggui = true;
        }
        me.step1.find('.next').click(function(){
            if(true){
            }
            var title = me.step1.find('#titleInput').val();
            if(title == ""){
                TM.Alert.load("亲，计划标题不能为空的哦~");
            } else {
                $.post('/DelistPlan/createDelistPlan', {title:title, planId: TM.CreateDelist.planId}, function(data){
                    if (TM.DelistBase.util.judgeAjaxResult(data) == false) {
                        return;
                    }
                    TM.CreateDelist.planId = data.results;
                    me.step1.hide(500, function(){
                        me.step2.show(500);
                    });
                    var detailHref = "";
                    if(isTaozhanggui) {
                        detailHref = "/kits/delistPlanDetail?planId=" + TM.CreateDelist.planId;
                    } else {
                        detailHref = "/autotitle/delistPlanDetail?planId=" + TM.CreateDelist.planId;
                    }
                    me.step6.find(".plan-detail-link").attr("href", detailHref);
                });
            }

        });
    }


    TM.CreateDelist.initStep2 = function(){
        me.step2.find('.back').click(function(){
            if(true){
            }
            me.step2.hide(500,function(){
                me.step1.show(500);
            })
        });
        me.step2.find('p .input-tip').click(function(){
            $(this).parent().find('input').click();
        });
        var step2Next = me.step2.find('.next');
        step2Next.click(function(){
            if(true){

            }
            var checked = me.step2.find('input[name="itemtype"]:checked');
            if(checked.length == 0){
                TM.widget.simpleQtips(step2Next, '亲,请您先选择您要要调整的宝贝范围哟');
                return;
            }
            me.step2.hide(500,function(){
                if(checked.hasClass('category')){
                    // For categories....
                    me.step3Cat.show(500);
                }else if(checked.hasClass('manual')){
                    me.step3Manual.show(500);
                }
            });
        });
    }
    TM.CreateDelist.initStep3 = function(){

    }

    TM.CreateDelist.initStep3Category = function(){
        me.step3Cat.find('.back').click(function(){
            me.step3Cat.hide(500,function(){me.step2.show(500)})
        });

        var step3CatNext = me.step3Cat.find('.next');
        step3CatNext.click(function(){
            me.step4.backTarget = me.step3Cat;

            var itemCids = [];
            var sellerCids = [];
            var noDelistNumIids = [];
            var itemStatus = me.step3Cat.find('input[name="itemStatus"]:checked').attr('status');
            me.itemCatContainer.find('input:checked').each(function(i,elem){
                itemCids.push($(elem).attr("cid"));
            });
            me.sellerCatContainer.find('input:checked').each(function(i, elem){
                sellerCids.push($(elem).attr("cid"));
            });
            me.step3Cat.find('.stepOptline .busSearch tbody .itemrow').each(function(){
                noDelistNumIids.push($(this).attr('numIid'));
            });
            var params = {};
            params.planId = TM.CreateDelist.planId;
            params.itemStatusRule = itemStatus;
            params.selfCateIds = sellerCids.join(',');
            params.delistCateIds = itemCids.join(',');
            params.notDelistNumIids = noDelistNumIids.join(",");
            params.isFilterGoodSalesItem = me.step3Cat.find('input[name="first10sale"]:checked').attr("status");
            $.ajax({
                //async : false,
                url : '/DelistPlan/setDelistPlanConfig',
                data : params,
                type : 'post',
                error: function() {
                },
                success: function (data) {
                    if (TM.DelistBase.util.judgeAjaxResult(data) == false) {
                        return;
                    }
                    if(data.success){
                        me.step3Cat.hide(500,function(){
                            me.step4.show(500);
                        })
                    }else{
                        // There are somehing wrong . do tips...
                        TM.widget.simpleQtips(step3CatNext, '亲,请您先选择您要要调整的宝贝范围哟');
                    }
                }
            });
        });
        me.step3Cat.find('.category-add-exclude-items').unbind().click(function(){
            var excludeNumIids = [];
            me.step3Cat.find('.itemrow').each(function(){
                excludeNumIids.push($(this).attr('numIid'));
            });
            CommChoose.createChoose.createOrRefleshCommsDiv(function(){
                TM.AudotDelist.itemChoosedTable(me.step3Cat.find('.busSearch'), CommChoose.rule.numIidList);
            },"/DelistPlan/chooseItemsTMPaginger", excludeNumIids.join(","));
        });
    }

    TM.CreateDelist.initStep3Manual = function(){
        me.step3Manual.find('.back').click(function(){
            me.step3Manual.hide(500,function(){me.step2.show(500)})
        });

        // TODO 手动添加宝贝部分
        var step3ManualNext = me.step3Manual.find('.next');
        step3ManualNext.click(function(){
            me.step4.backTarget = me.step3Manual;
            if(me.step3Manual.find('.itemrow').length == 0){
                TM.Alert.load("请至少选择一款宝贝哦亲");
            } else {
                var numIids = [];
                me.step3Manual.find('.itemrow').each(function(){
                    numIids.push($(this).attr('numIid'));
                });
                $.post('/DelistPlan/setUserSelectNumIids',{planId:TM.CreateDelist.planId, numIids:numIids.join(",")}, function(data){
                    if (TM.DelistBase.util.judgeAjaxResult(data) == false) {
                        return;
                    }

                    if(data.success){
                        me.step3Manual.hide(500,function(){
                            me.step4.show(500);
                        });
                    }else{

                    }
                });

            }
        })
        me.step3Manual.find('.manual-add-delist-items').click(function(){
            var excludeNumIids = [];
            me.step3Manual.find('.itemrow').each(function(){
                excludeNumIids.push($(this).attr('numIid'));
            });
            CommChoose.createChoose.createOrRefleshCommsDiv(function(){
                TM.AudotDelist.itemChoosedTable(me.step3Manual.find('.busSearch'), CommChoose.rule.numIidList);
            }, "/DelistPlan/findRemainItems", excludeNumIids.join(","));
        });
    }
    TM.CreateDelist.initStep4 = function(canedit){
        me.step4.find('.back').click(function(){
           if(!me.step4.backTarget){
               me.step4.backTarget = me.step3Cat;
           }
           me.step4.hide(500,function(){me.step4.backTarget.show(500)});
        });

        me.step4.find('.next').click(function(){
           // submit the week time
            var hours = [], weekdays = [];
            me.step4.find('.stepbody .layout-table .advance-hour-check:checked').each(function(){
                hours.push($(this).val());
            });
            me.step4.find('.stepbody .advance-weekday-check:checked').each(function(){
                weekdays.push($(this).val());
            });
            $.post('/DelistPlan/calcuDistribute',{planId:TM.CreateDelist.planId, weeks:weekdays.join(","), hours:hours.join(",")}, function(data){
                TM.AudotDelist.renderRuleAreaWithData(me.step5.find('.auto-delist-distribute-table'), data, canedit);
                me.step4.hide(500,function(){me.step5.show(500)})
            });

        });
        me.step4.find('.skip').click(function(){
            // submit the week time
            var hours = [], weekdays = [];
            me.step4.find('.stepbody .layout-table .advance-hour-check:checked').each(function(){
                hours.push($(this).val());
            });
            me.step4.find('.stepbody .advance-weekday-check:checked').each(function(){
                weekdays.push($(this).val());
            });
            $.post('/DelistPlan/calcuDistribute',{planId:TM.CreateDelist.planId, weeks:weekdays.join(","), hours:hours.join(",")}, function(data){
                TM.AudotDelist.renderRuleAreaWithData(me.step5.find('.auto-delist-distribute-table'), data);
                me.step4.hide(500,function(){me.step5.show(500)})
            });
        });

        me.step4.find(".advance-hour-check").unbind().click(function() {
            var isChecked = $(this).is(":checked");
            if (isChecked == true) {
                $(this).parent().find(".advance-hour-span").addClass("checked-hour");
            } else {
                $(this).parent().find(".advance-hour-span").removeClass("checked-hour");
            }
        });

        me.step4.find(".advance-hour-span").unbind().click(function() {
            var checkObj = $(this).parent().find(".advance-hour-check");
            var isChecked = checkObj.is(":checked");
            if (isChecked == true) {
                checkObj.attr("checked", false);
                $(this).removeClass("checked-hour");
            } else {
                checkObj.attr("checked", true);
                $(this).addClass("checked-hour");
            }
        });

        me.step4.find(".select-all-hour-btn").click(function() {
            me.step4.find(".advance-hour-check").attr("checked", true);
            me.step4.find(".advance-hour-span").addClass("checked-hour");
        });

        me.step4.find(".cancel-all-hour-btn").click(function() {
            me.step4.find(".advance-hour-check").attr("checked", false);
            me.step4.find(".advance-hour-span").removeClass("checked-hour");
        });

    }
    TM.CreateDelist.initStep5 = function(){
        me.step5.find('.back').click(function(){
            me.step5.hide(500,function(){me.step4.show(500)});
        });
        // actually, this is the finish btn
        me.step5.find('.next').click(function(){
            if(parseInt(me.step5.find('.already-alocated').text()) == 0){
                TM.Alert.load("该计划没有宝贝哦亲");
            } else if(parseInt(me.step5.find('.already-alocated').text()) != parseInt(me.step5.find('.total').text())){
                TM.Alert.load("该计划宝贝总数与目前分配数不一致，请修改后再提交~");
            } else {
                if (confirm("确定要提交上架计划分布？") == false) {
                    return;
                }
                var hourRates = [];
                for(var i = 0; i < 7; i++){
                    me.step5.find('.ruleTd[index="'+(i+6)%7+'"]').each(function(){
                        var num = 0;
                        if($(this).find('span:visible').length == 1){
                            num = $(this).find('span:visible').text();
                        } else if($(this).find('input:visible').length == 1){
                            num = $(this).find('input:visible').val();
                        }
                        hourRates.push(num);
                    });
                }
                $.post('/DelistPlan/reDistribute',{planId:TM.CreateDelist.planId, hourRates:hourRates.join(","), isTurnOn: true}, function(data){
                    if (TM.DelistBase.util.judgeAjaxResult(data) == false) {
                        return;
                    }
                    if(data.success){
                        me.step5.hide(500,function(){me.step6.show(500)});
                    }else{

                    }
                })
            }

        })
    }


    TM.CreateDelist.initCats = function(){
        $.ajax({
            url : '/items/itemCatStatusCount',
            success : function(res){
                // 淘宝类目
                if(!res || res.length == 0){
                    // Hide this part...
                    return;
                }

                var rows = $('#catTmpl').tmpl(res);
                me.itemCatContainer.empty();
                me.itemCatContainer.append(rows);
            },
            global : false
        });

        $.ajax({
            url : '/items/sellerCatStatusCount',
            success : function(res){
                if(!res || res.length == 0){
                    me.sellerCatContainer.hide();
                    $(".no-delist-index-span").html("4");
                } else {
                    me.sellerCatContainer.show();
                    $(".no-delist-index-span").html("5");
                }
                // 店铺自定义类目
                var rows = $('#catTmpl').tmpl(res);
                me.sellerCatContainer.empty();
                me.sellerCatContainer.append(rows);
            },
            global : false
        });
    }


    TM.CreateDelist.submitTitle = function(success, fail){

        if(true){
            success && success();
            return;
        }

        $.post('',function(){
        });
    }


    TM.CreateDelist.submitCampProp = function(success, error){

//        $.post('',function(){
//
//        })
    }


})(jQuery,window));


((function ($, window) {

    /**
     * 每周上下架分布数
     * @type {*}
     */
    TM.DelistTime = TM.DelistTime || {};

    var DelistTime = TM.DelistTime;

    DelistTime.init = DelistTime.init || {};
    DelistTime.init = $.extend({
        doInit: function() {
            var container = $(".delist-distribute-div");
            DelistTime.container = container;

            TM.gcs('/js/jquery.tmpl.js',function(){
                $.post('/AutoDelist/queryDelistDistribute',function(arr){
                    DelistTime.init.doAjaxRes(arr);
                });

            })

        },
        getContainer: function() {
            return DelistTime.container;
        },
        doAjaxRes: function(arr) {
            var container = DelistTime.init.getContainer();
            container.find('.planRow').html("");
            container.find('.realRow').html("");

            container.find('.planRow').append(DelistTime.init.wrap(arr[0]));
            container.find('.realRow').append(DelistTime.init.wrap(arr[1]));
            container.show();
        },
        wrap: function(week) {
            var data = [];
            data.push({'weekName':'星期一','weekValue':week[0]});
            data.push({'weekName':'星期二','weekValue':week[1]});
            data.push({'weekName':'星期三','weekValue':week[2]});
            data.push({'weekName':'星期四','weekValue':week[3]});
            data.push({'weekName':'星期五','weekValue':week[4]});
            data.push({'weekName':'星期六','weekValue':week[5]});
            data.push({'weekName':'星期日','weekValue':week[6]});
            return $('#delistWeekDistribution').tmpl(data);
        }
    }, DelistTime.init);


})(jQuery,window));