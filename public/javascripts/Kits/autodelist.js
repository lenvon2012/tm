


var TM = TM || {};

TM.AudotDelist = TM.AudotDelist || {};


((function ($, window) {
    var me = TM.AudotDelist;


    me.ErrorHandler = me.ErrorHandler || {};

    TM.AudotDelist.getPlanId = function() {

        var planId = TM.AudotDelist.planId;

        if (planId === undefined) {
            var hrefParams = $.url(window.location.href);
            planId = hrefParams.param('planId') || 0;
            TM.AudotDelist.planId = planId;
        }


        return planId;

    }

    TM.AudotDelist.init = function(container, version){
        // version : 1 means old delist, and 2 means new delist
        if(version === undefined || version == null){
            version = 1;
        }
        TM.AudotDelist.version = version;

        $("body").find(".qtip").hide();
        me.container = container;
        me.container.empty();

        me.itemBase = $("<div id='swItemBase' class='clearfix' style='display:none'></div>");
        var html = "<div class='autoDelistIntroductionDiv clearfix'>" +
            "<span class='autoDelistTip'>1.为您提供专业的自动上下架服务，在黄金时段均匀下架，从此提高流量。</span>" +
            "<span class='autoDelistTip'>2.为您提供不同分布时间的选择，根据您的实际情况，您可以选择最适合的分布类型。</span>" +
            "</div>";
        me.itemIntroduction = $(html);
        me.itemDistri = $("<div class='autoDelistDistriDiv'></div>");

        var configContainer = $('<div class="plan_config" style="margin-top: 20px;"></div> ');

        if(version != 2){
            me.itemDistri.appendTo(me.container);
        } else {

            var planId = TM.AudotDelist.getPlanId();
            if (planId <= 0) {
                location.href = "/kits/delistPlans";
                return;
            }

            $.ajax({
                url : '/delistplan/queryDelistPlan',
                type: "post",
                data: {planId: planId},
                success : function(resultJson){
                    if (TM.DelistBase.util.judgeAjaxResult(resultJson) == false) {
                        location.href = "/kits/delistPlans";
                        return;
                    }

                    var planJson = resultJson.results;
                    $(".cur-plan-title").html(planJson.title);


                    PlanConfig.init.doInit(configContainer, planJson);
                },
                error : function(){

                }
            });





        }
        me.distributeWrapper = $("<div class='out-wrapper'></div>");
        me.opArea = $("<div class='swOpArea'></div>");
        me.switchLine =$("<div class='switchStatusLine '></div>");
        if(version == 2){
            me.nav = $("<div class='clearfix kitNav'><div class='tmNav'>" +
                "<span class='selected'><a tag='todayList'>今日上架计划</a></span>" +
                "<span><a tag='weekList'>一周上架计划</a></span>" +
                "<span><a tag='newDistributeRule'>上下架时间分布</a></span>" +
                "<span><a tag='oneKeySimplePlan'>一键均匀分布</a></span>" +
                "<span><a tag='noautolistitems'>设置排除宝贝</a></span>" +
                "<span><a tag='autolistsetting'>手动设置上架时间</a></span>" +
                //"<span><a tag='delisttime'>上架时间分布</a></span>" +
                "<span><a tag='oplogs'>上下架日志</a></span>" +
                "</div></div>");
        } else {
            me.nav = $("<div class='clearfix kitNav'><div class='tmNav'>" +
                "<span class='selected'><a tag='todayList'>今日上架计划</a></span>" +
                "<span><a tag='weekList'>一周上架计划</a></span>" +
                "<span><a tag='noautolistitems'>设置排除宝贝</a></span>" +
                "<span><a tag='autolistsetting'>手动设置上架时间</a></span>" +
                "<span><a tag='delisttime'>上架时间分布</a></span>" +
                "<span><a tag='oplogs'>上下架日志</a></span>" +
                "</div></div>");
        }

        me.listArea = $("<div class='kitListArea autoDelistArea clearfix'></div>");


        me.itemBase.appendTo(me.container);
        //me.itemIntroduction.appendTo(me.container);

        me.switchLine.appendTo(me.opArea);
        me.opArea.appendTo(me.container);

        configContainer.appendTo(me.container);

        me.distributeWrapper.appendTo(me.container);
        me.nav.appendTo(me.container);
        me.listArea.appendTo(me.container);

        if(!TM.isVip()){
            TM.widget.buildKitGuidePay('delist').insertBefore(me.itemBase);
        }

        me.container.show();


        me.bindTabListeners();

        TM.AudotDelist.refresh();

        me.renderOpArea();

        //me.renderRuleArea();

    }


    TM.AudotDelist.renderRuleArea = function(target, canedit){
        var planId = TM.AudotDelist.getPlanId();
        $.post('/delistplan/queryDetailDistribute?planId=' + planId,function(data){

            if(!data){
                return;
            }
            target.find('.distribution-area').remove();
            var mainWrappe = $('.distribution-area').clone();
            mainWrappe.find('tbody').remove();
            var tbody = $('<tbody></tbody>');
            var totalItem = 0;
            var className = canedit?" ":" hidden";
            tbody.append($('<tr class="horizon-operTr '+className+'"><td>按天批量</td><td class="operTd"><a href="javascript:;" class="verticle-add" index=1></a><a href="javascript:;" class="verticle-minus" index=1></a></td>' +
                '<td class="operTd"><a href="javascript:;" class="verticle-add" index=2></a><a href="javascript:;" class="verticle-minus" index=2></a></td>' +
                '<td class="operTd"><a href="javascript:;" class="verticle-add" index=3></a><a href="javascript:;" class="verticle-minus" index=3></a></td>' +
                '<td class="operTd"><a href="javascript:;" class="verticle-add" index=4></a><a href="javascript:;" class="verticle-minus" index=4></a></td>' +
                '<td class="operTd"><a href="javascript:;" class="verticle-add" index=5></a><a href="javascript:;" class="verticle-minus" index=5></a></td>' +
                '<td class="operTd"><a href="javascript:;" class="verticle-add" index=6></a><a href="javascript:;" class="verticle-minus" index=6></a></td>' +
                '<td class="operTd"><a href="javascript:;" class="verticle-add" index=7></a><a href="javascript:;" class="verticle-minus" index=7></a></td>' +
                '<td></td></tr>'));
            $(data).each(function(i,hour){
                if(i < 24){
                    var trObj = $('<tr class="hour-tr"><td>'+TM.AudotDelist.genHour(i)+'</td></tr>');
                    $(hour).each(function(j,day){
                        var className = (day > 0) ? "overOne" : "";
                        className += " " + "weekday-index" + j;
                        if(i == 0){
                            className += " first-row";
                        }
                        trObj.append($('<td index='+j+' class="ruleTd '+className+'"><span class="oldnum inlineblock">'+day+'</span><input type="text" class="num" style="display:none;"><p class="oldnum-tip inlineblock" style="display: none;color: red;margin: 0 0 0 5px;">'+day+'</p>' +
                            '<div class="hidden single-add-minus"><p class="addSingle inlineblock" style="margin: 0 8px 0 0;"></p><p class="minusSingle inlineblock" style="margin: 0;"></p></div></td>'));
                    });
                    var className = canedit?" ":" hidden";
                    trObj.append('<td class="operTd '+className+'"><a href="javascript:;" class="add"></a><a href="javascript:;" class="minus"></a></td>');
                } else if(i == 24) {
                    $(hour).each(function(j,day){
                        mainWrappe.find('#distributionTable thead tr span').eq(j).html(day);
                        totalItem += day;
                    });
                }
                mainWrappe.find('.total').html(totalItem);
                mainWrappe.find('.total-bottom').html(totalItem);
                mainWrappe.find('.already-alocated').html(totalItem);
                mainWrappe.find('.already-alocated-bottom').html(totalItem);
                mainWrappe.find('.un-alocated').html(0);
                mainWrappe.find('.un-alocated-bottom').html(0);
                tbody.append(trObj);
            });
            /*tbody.find('.num').blur(function(){
                var newTotal = TM.AudotDelist.genNewTotal(tbody);
                if(newTotal - parseInt($(this).parent().find('span').text()) + parseInt($(this).val()) > parseInt(mainWrappe.find('.total').text())){
                    TM.Alert.load("超过宝贝数目限制");
                } else {
                    if(parseInt($(this).val()) < 0){
                        TM.Alert.load("宝贝数目不能低于0哦亲");
                        $(this).parent().find('span').text(0);
                    } else {
                        $(this).parent().find('span').text($(this).val());
                    }
                }
                $(this).hide();
                $(this).parent().find('.single-add-minus').hide();
                $(this).parent().find('span').show();
                if(parseInt($(this).parent().find('span').text()) == 0){
                    $(this).parent().removeClass('overOne');
                } else {
                    $(this).parent().addClass('overOne');
                }
                TM.AudotDelist.refreshTdTotal(tbody, $(this).parent().attr("index"));
                newTotal = TM.AudotDelist.genNewTotal(tbody);
                mainWrappe.find('.already-alocated').text(newTotal)
            });*/
            tbody.find('.num').focus(function(){
                $.cookie("originNum", parseInt($(this).val()));
            });
            tbody.find('.num').keyup(function () {
                if(isNaN($(this).val())){
                    $(this).val($.cookie("originNum"));
                } else if (parseInt($(this).val()) < 0) {
                    $(this).val($.cookie("originNum"));
                } else if($(this).val() == ''){
                    var index = parseInt($(this).parent().attr('index')) + 1;
                    TM.AudotDelist.VerticleRefreshTable(tbody, index, 0 - $.cookie('originNum'));
                    $.cookie("originNum",0);
                } else {
                    var index = parseInt($(this).parent().attr('index')) + 1;
                    TM.AudotDelist.VerticleRefreshTable(tbody, index, parseInt($(this).val()) - $.cookie('originNum'));
                    $.cookie("originNum",parseInt($(this).val()));
                }
            })
            if(canedit){
                tbody.find('.oldnum').unbind('click').click(function(){
                    TM.AudotDelist.checkInput(tbody);
                    //TM.AudotDelist.refreshTable(tbody);
                    $(this).hide();
                    $(this).parent().find('input').val($(this).text());
                    $(this).parent().find('input').show();
                    //$(this).parent().find('.oldnum-tip').html($(this).text());
                    $(this).parent().find('.oldnum-tip').show();
                    $(this).parent().find('input').focus();
                    $(this).parent().find('.single-add-minus').show();
                });
            } else {
                tbody.find('.oldnum').unbind('click');
            }

            tbody.find('.verticle-add').click(function(){
                var index = $(this).attr('index');
                tbody.find('.hour-tr').each(function(i,trObj){
                    var $this = $(trObj).find('td').eq(index);
                    if ($this.find('span:visible').length == 1) {
                        $this.find('span:visible').text(parseInt($this.find('span').text()) + 1);
                    } else {
                        $this.find('input:visible').val(parseInt($this.find('input:visible').val()) + 1);
                    }
                    $this.addClass('overOne');
                });
                TM.AudotDelist.VerticleRefreshTable(tbody, index, 24);
            });
            tbody.find('.add').click(function(){
                $(this).parent().parent().find('.ruleTd').each(function () {
                    if ($(this).find('span:visible').length == 1) {
                        $(this).find('span:visible').text(parseInt($(this).find('span').text()) + 1);
                    } else {
                        $(this).find('input:visible').val(parseInt($(this).find('input:visible').val()) + 1);
                    }
                    $(this).addClass('overOne');
                });
                TM.AudotDelist.HorizonRefreshTable(tbody,[1,1,1,1,1,1,1]);
            });
            tbody.find('.minus').click(function(){
                var arr = [];
                $(this).parent().parent().find('.ruleTd').each(function(i,ruleTd){
                    var newNum = 0;
                    if($(this).find('span:visible').length == 1) {
                        newNum = (parseInt($(this).find('span').text()) < 1) ? 0 : (parseInt($(this).find('span').text()) - 1);
                        arr[i] = (parseInt($(this).find('span').text()) < 1) ? 0 : -1;
                        $(this).find('span:visible').text(parseInt(newNum));
                    } else {
                        newNum = (parseInt($(this).find('input').val()) < 1) ? 0 : (parseInt($(this).find('input').val()) - 1);
                        arr[i] = (parseInt($(this).find('input').val()) < 1) ? 0 : -1;
                        $(this).find('input:visible').val(parseInt(newNum));
                    }
                    if(newNum == 0){
                        $(this).removeClass('overOne');
                    }
                });
                TM.AudotDelist.HorizonRefreshTable(tbody, arr);
            });
            tbody.find('.verticle-minus').click(function(){
                var index = $(this).attr('index');
                var count = 0;
                tbody.find('.hour-tr').each(function(i,trObj){
                    var newNum = 0;
                    var $this = $(trObj).find('td').eq(index);
                    if ($this.find('span:visible').length == 1) {
                        newNum = (parseInt($this.find('span').text()) < 1) ? 0 : (parseInt($this.find('span').text()) - 1);
                        count =  (parseInt($this.find('span').text()) < 1) ? count : (count - 1);
                        $this.find('span:visible').text(parseInt(newNum));
                    } else {
                        newNum = (parseInt($this.find('input').val()) < 1) ? 0 : (parseInt($this.find('input').val()) - 1);
                        count =  (parseInt($this.find('input').val()) < 1) ? count : (count - 1);
                        $this.find('input:visible').val(parseInt(newNum));
                    }
                    if(newNum == 0){
                        $this.removeClass('overOne');
                    }
                });
                TM.AudotDelist.VerticleRefreshTable(tbody, index, count);
            });
            tbody.find('.addSingle').click(function(){
                var index = parseInt($(this).parent().parent().attr('index')) + 1;
                $(this).parent().parent().find('input').val(parseInt($(this).parent().parent().find('input').val()) + 1);
                if(parseInt($(this).parent().parent().find('input').val()) > 0){
                    $(this).parent().parent().addClass('overOne');
                }
                TM.AudotDelist.VerticleRefreshTable(tbody, index, 1);
            });
            tbody.find('.minusSingle').click(function(){
                var index = parseInt($(this).parent().parent().attr('index')) + 1;
                var isMinus = (parseInt($(this).parent().parent().find('input').val()) < 1) ? 0 : -1;
                var newValue  = (parseInt($(this).parent().parent().find('input').val()) < 1) ? 0 : (parseInt($(this).parent().parent().find('input').val()) - 1);
                $(this).parent().parent().find('input').val(newValue);

                if(newValue == 0){
                    $(this).parent().parent().removeClass('overOne');
                }
                TM.AudotDelist.VerticleRefreshTable(tbody, index, isMinus);
            });
            mainWrappe.find('.submit').click(function(){
                var remain = parseInt(mainWrappe.find('.un-alocated').text());
                if(remain < 0){
                    TM.Alert.load("分配的宝贝数超过宝贝总数，请重新调整后再提交哦亲");
                } else if(remain > 0) {
                    TM.Alert.load("还有宝贝未分配，请重新调整后再提交哦亲");
                } else {
                    var alocated = parseInt(mainWrappe.find('.already-alocated').text());
                    if (alocated <= 0) {
                        TM.Alert.load("该计划宝贝数为0，不能提交！");
                        return;
                    }
                    if (confirm("确定要重新修改计划分布？") == false) {
                        return;
                    }

                    var hourRates = [];
                    for(var i = 0; i < 7; i++){
                        mainWrappe.find('.ruleTd[index="'+(i+6)%7+'"]').each(function(){
                            var num = 0;
                            if($(this).find('span:visible').length == 1){
                                num = $(this).find('span:visible').text();
                            } else if($(this).find('input:visible').length == 1){
                                num = $(this).find('input:visible').val();
                            }
                            hourRates.push(num);
                        });
                    }
                    $.post('/DelistPlan/reDistribute',{planId:TM.AudotDelist.getPlanId(), hourRates:hourRates.join(","), isTurnOn: false}, function(data){
                        if (TM.DelistBase.util.judgeAjaxResult(data) == false) {
                            return;
                        }
                        if(data.success){
                            alert("上架计划重新分布成功！");
                        }else{

                        }
                    })
                }
            });
            mainWrappe.find('.edit-plan').click(function(){
                mainWrappe.find('.edit-plan').hide();
                mainWrappe.find('.roll-back').show();
                mainWrappe.find('.clear-all').show();
                mainWrappe.find('.submit').show();
                tbody.find('.horizon-operTr').show();
                tbody.find('.operTd').show();
                tbody.parent().find('thead .operTh').show();

                // set oldnum click event
                tbody.find('.oldnum').unbind('click').click(function () {
                    TM.AudotDelist.checkInput(tbody);
                    //TM.AudotDelist.refreshTable(tbody);
                    $(this).hide();
                    $(this).parent().find('input').val($(this).text());
                    $(this).parent().find('input').show();
                    //$(this).parent().find('.oldnum-tip').html($(this).text());
                    $(this).parent().find('.oldnum-tip').show();
                    $(this).parent().find('input').focus();
                    $(this).parent().find('.single-add-minus').show();
                });
            });
            mainWrappe.find('.roll-back').click(function(){
                if(confirm("确认要还原到默认分布吗?")){
                    TM.AudotDelist.renderRuleArea(target, false);
                }
            });
            mainWrappe.find('.clear-all').click(function(){
                if(confirm("确认要清零当前分布吗?")){
                    tbody.find('span').text(0);
                    tbody.find('input').val(0);
                    tbody.find('.overOne').removeClass('overOne');
                    tbody.parent().parent().parent().find('.already-alocated').text(0);
                    tbody.parent().parent().parent().find('.already-alocated-bottom').text(0);
                    tbody.parent().parent().parent().find('.un-alocated').text(tbody.parent().parent().parent().find('.total').text());
                    tbody.parent().parent().parent().find('.un-alocated-bottom').text(tbody.parent().parent().parent().find('.total').text());
                    tbody.parent().find('th span').text(0);
                    //TM.AudotDelist.refreshTable(tbody);
                }
            });
            mainWrappe.find('.chooseitem').click(function(){
                CommChoose.createChoose.createOrRefleshCommsDiv(function(){
                    TM.AudotDelist.itemChoosedTable(mainWrappe, CommChoose.rule.numIidList);
                });
            });
            mainWrappe.find('#distributionTable').append(tbody);
            target.empty();
            target.append(mainWrappe);
            mainWrappe.show();
        });

    }

    TM.AudotDelist.renderRuleAreaWithData = function (target, data, canedit) {

        if (!data) {
            return;
        }
        target.find('.distribution-area').remove();
        var mainWrappe = $('.distribution-area').clone();
        mainWrappe.find('tbody').remove();
        var tbody = $('<tbody></tbody>');
        var totalItem = 0;
        var className = canedit?" ":" hidden";
        tbody.append($('<tr class="horizon-operTr'+className+'"><td>按天批量</td><td class="operTd"><a href="javascript:;" class="verticle-add" index=1></a><a href="javascript:;" class="verticle-minus" index=1></a></td>' +
            '<td class="operTd"><a href="javascript:;" class="verticle-add" index=2></a><a href="javascript:;" class="verticle-minus" index=2></a></td>' +
            '<td class="operTd"><a href="javascript:;" class="verticle-add" index=3></a><a href="javascript:;" class="verticle-minus" index=3></a></td>' +
            '<td class="operTd"><a href="javascript:;" class="verticle-add" index=4></a><a href="javascript:;" class="verticle-minus" index=4></a></td>' +
            '<td class="operTd"><a href="javascript:;" class="verticle-add" index=5></a><a href="javascript:;" class="verticle-minus" index=5></a></td>' +
            '<td class="operTd"><a href="javascript:;" class="verticle-add" index=6></a><a href="javascript:;" class="verticle-minus" index=6></a></td>' +
            '<td class="operTd"><a href="javascript:;" class="verticle-add" index=7></a><a href="javascript:;" class="verticle-minus" index=7></a></td>' +
            '<td></td></tr>'));
        $(data).each(function (i, hour) {
            if (i < 24) {
                var trObj = $('<tr class="hour-tr"><td>' + TM.AudotDelist.genHour(i) + '</td></tr>');
                $(hour).each(function (j, day) {
                    var className = (day > 0) ? "overOne" : "";
                    className += " " + "weekday-index" + j;
                    if(i == 0){
                        className += " first-row";
                    }
                    trObj.append($('<td index=' + j + ' class="ruleTd ' + className + '"><span class="oldnum inlineblock">' + day + '</span><input type="text" class="num" style="display:none;"><p class="oldnum-tip inlineblock" style="display: none;color: red;margin: 0 0 0 5px;">' + day + '</p>' +
                        '<div class="hidden single-add-minus"><p class="addSingle inlineblock" style="margin: 0 8px 0 0;"></p><p class="minusSingle inlineblock" style="margin: 0;"></p></div></td>'));
                });
                var className = canedit?" ":" hidden";
                trObj.append('<td class="operTd '+className+'"><a href="javascript:;" class="add"></a><a href="javascript:;" class="minus"></a></td>');
            } else if (i == 24) {
                $(hour).each(function (j, day) {
                    mainWrappe.find('#distributionTable thead tr span').eq(j).html(day);
                    totalItem += day;
                });
            }
            mainWrappe.find('.total').html(totalItem);
            mainWrappe.find('.total-bottom').html(totalItem);
            mainWrappe.find('.already-alocated').html(totalItem);
            mainWrappe.find('.already-alocated-bottom').html(totalItem);
            mainWrappe.find('.un-alocated').html(0);
            mainWrappe.find('.un-alocated-bottom').html(0);
            tbody.append(trObj);
        });
        /*tbody.find('.num').blur(function(){
         var newTotal = TM.AudotDelist.genNewTotal(tbody);
         if(newTotal - parseInt($(this).parent().find('span').text()) + parseInt($(this).val()) > parseInt(mainWrappe.find('.total').text())){
         TM.Alert.load("超过宝贝数目限制");
         } else {
         if(parseInt($(this).val()) < 0){
         TM.Alert.load("宝贝数目不能低于0哦亲");
         $(this).parent().find('span').text(0);
         } else {
         $(this).parent().find('span').text($(this).val());
         }
         }
         $(this).hide();
         $(this).parent().find('.single-add-minus').hide();
         $(this).parent().find('span').show();
         if(parseInt($(this).parent().find('span').text()) == 0){
         $(this).parent().removeClass('overOne');
         } else {
         $(this).parent().addClass('overOne');
         }
         TM.AudotDelist.refreshTdTotal(tbody, $(this).parent().attr("index"));
         newTotal = TM.AudotDelist.genNewTotal(tbody);
         mainWrappe.find('.already-alocated').text(newTotal)
         });*/
        tbody.find('.num').focus(function () {
            $.cookie("originNum", parseInt($(this).val()));
        });
        tbody.find('.num').keyup(function () {
            if (isNaN($(this).val())) {
                $(this).val($.cookie("originNum"));
            } else if (parseInt($(this).val()) < 0) {
                $(this).val($.cookie("originNum"));
            } else if ($(this).val() == '') {
                var index = parseInt($(this).parent().attr('index')) + 1;
                TM.AudotDelist.VerticleRefreshTable(tbody, index, 0 - $.cookie('originNum'));
                $.cookie("originNum", 0);
            } else {
                var index = parseInt($(this).parent().attr('index')) + 1;
                TM.AudotDelist.VerticleRefreshTable(tbody, index, parseInt($(this).val()) - $.cookie('originNum'));
                $.cookie("originNum", parseInt($(this).val()));
            }
        })
        if(canedit){
            tbody.find('.oldnum').unbind('click').click(function () {
                TM.AudotDelist.checkInput(tbody);
                //TM.AudotDelist.refreshTable(tbody);
                $(this).hide();
                $(this).parent().find('input').val($(this).text());
                $(this).parent().find('input').show();
                //$(this).parent().find('.oldnum-tip').html($(this).text());
                $(this).parent().find('.oldnum-tip').show();
                $(this).parent().find('input').focus();
                $(this).parent().find('.single-add-minus').show();
            });
        } else {
            tbody.find('.oldnum').unbind('click');
        }

        tbody.find('.verticle-add').click(function () {
            var index = $(this).attr('index');
            tbody.find('.hour-tr').each(function (i, trObj) {
                var $this = $(trObj).find('td').eq(index);
                if ($this.find('span:visible').length == 1) {
                    $this.find('span:visible').text(parseInt($this.find('span').text()) + 1);
                } else {
                    $this.find('input:visible').val(parseInt($this.find('input:visible').val()) + 1);
                }
                $this.addClass('overOne');
            });
            TM.AudotDelist.VerticleRefreshTable(tbody, index, 24);
        });
        tbody.find('.add').click(function () {
            $(this).parent().parent().find('.ruleTd').each(function () {
                if ($(this).find('span:visible').length == 1) {
                    $(this).find('span:visible').text(parseInt($(this).find('span').text()) + 1);
                } else {
                    $(this).find('input:visible').val(parseInt($(this).find('input:visible').val()) + 1);
                }
                $(this).addClass('overOne');
            });
            TM.AudotDelist.HorizonRefreshTable(tbody, [1, 1, 1, 1, 1, 1, 1]);
        });
        tbody.find('.minus').click(function () {
            var arr = [];
            $(this).parent().parent().find('.ruleTd').each(function (i, ruleTd) {
                var newNum = 0;
                if ($(this).find('span:visible').length == 1) {
                    newNum = (parseInt($(this).find('span').text()) < 1) ? 0 : (parseInt($(this).find('span').text()) - 1);
                    arr[i] = (parseInt($(this).find('span').text()) < 1) ? 0 : -1;
                    $(this).find('span:visible').text(parseInt(newNum));
                } else {
                    newNum = (parseInt($(this).find('input').val()) < 1) ? 0 : (parseInt($(this).find('input').val()) - 1);
                    arr[i] = (parseInt($(this).find('input').val()) < 1) ? 0 : -1;
                    $(this).find('input:visible').val(parseInt(newNum));
                }
                if (newNum == 0) {
                    $(this).removeClass('overOne');
                }
            });
            TM.AudotDelist.HorizonRefreshTable(tbody, arr);
        });
        tbody.find('.verticle-minus').click(function () {
            var index = $(this).attr('index');
            var count = 0;
            tbody.find('.hour-tr').each(function (i, trObj) {
                var newNum = 0;
                var $this = $(trObj).find('td').eq(index);
                if ($this.find('span:visible').length == 1) {
                    newNum = (parseInt($this.find('span').text()) < 1) ? 0 : (parseInt($this.find('span').text()) - 1);
                    count = (parseInt($this.find('span').text()) < 1) ? count : (count - 1);
                    $this.find('span:visible').text(parseInt(newNum));
                } else {
                    newNum = (parseInt($this.find('input').val()) < 1) ? 0 : (parseInt($this.find('input').val()) - 1);
                    count = (parseInt($this.find('input').val()) < 1) ? count : (count - 1);
                    $this.find('input:visible').val(parseInt(newNum));
                }
                if (newNum == 0) {
                    $this.removeClass('overOne');
                }
            });
            TM.AudotDelist.VerticleRefreshTable(tbody, index, count);
        });
        tbody.find('.addSingle').click(function () {
            var index = parseInt($(this).parent().parent().attr('index')) + 1;
            $(this).parent().parent().find('input').val(parseInt($(this).parent().parent().find('input').val()) + 1);
            if (parseInt($(this).parent().parent().find('input').val()) > 0) {
                $(this).parent().parent().addClass('overOne');
            }
            TM.AudotDelist.VerticleRefreshTable(tbody, index, 1);
        });
        tbody.find('.minusSingle').click(function () {
            var index = parseInt($(this).parent().parent().attr('index')) + 1;
            var isMinus = (parseInt($(this).parent().parent().find('input').val()) < 1) ? 0 : -1;
            var newValue = (parseInt($(this).parent().parent().find('input').val()) < 1) ? 0 : (parseInt($(this).parent().parent().find('input').val()) - 1);
            $(this).parent().parent().find('input').val(newValue);

            if (newValue == 0) {
                $(this).parent().parent().removeClass('overOne');
            }
            TM.AudotDelist.VerticleRefreshTable(tbody, index, isMinus);
        });
        mainWrappe.find('.edit-plan').click(function(){
            mainWrappe.find('.edit-plan').hide();
            mainWrappe.find('.roll-back').show();
            mainWrappe.find('.clear-all').show();
            mainWrappe.find('.submit').show();
            tbody.find('.horizon-operTr').show();
            tbody.find('.operTd').show();
            tbody.parent().find('thead .operTh').show();

            // set oldnum click event
            tbody.find('.oldnum').unbind('click').click(function () {
                TM.AudotDelist.checkInput(tbody);
                //TM.AudotDelist.refreshTable(tbody);
                $(this).hide();
                $(this).parent().find('input').val($(this).text());
                $(this).parent().find('input').show();
                //$(this).parent().find('.oldnum-tip').html($(this).text());
                $(this).parent().find('.oldnum-tip').show();
                $(this).parent().find('input').focus();
                $(this).parent().find('.single-add-minus').show();
            });
        });
        mainWrappe.find('.submit').click(function () {
            var remain = parseInt(mainWrappe.find('.un-alocated').text());
            if (remain < 0) {
                TM.Alert.load("分配的宝贝数超过宝贝总数，请重新调整后再提交哦亲");
            } else if (remain > 0) {
                TM.Alert.load("还有宝贝未分配，请重新调整后再提交哦亲");
            } else {

            }
        });
        mainWrappe.find('.roll-back').click(function () {
            if (confirm("确认要还原到默认分布吗?")) {
                TM.AudotDelist.renderRuleAreaWithData(target, data, canedit);
            }
        });
        mainWrappe.find('.clear-all').click(function () {
            if (confirm("确认要清零当前分布吗?")) {
                tbody.find('span').text(0);
                tbody.find('input').val(0);
                tbody.find('.overOne').removeClass('overOne');
                tbody.parent().parent().parent().find('.already-alocated').text(0);
                tbody.parent().parent().parent().find('.already-alocated-bottom').text(0);
                tbody.parent().parent().parent().find('.un-alocated').text(tbody.parent().parent().parent().find('.total').text());
                tbody.parent().parent().parent().find('.un-alocated-bottom').text(tbody.parent().parent().parent().find('.total').text());
                tbody.parent().find('th span').text(0);
                //TM.AudotDelist.refreshTable(tbody);
            }
        });
        mainWrappe.find('#distributionTable').append(tbody);
        target.empty();
        target.append(mainWrappe);
        mainWrappe.show();

    }

    TM.AudotDelist.itemChoosedTable = function(target,numIids){
        target.find('.empty-row').hide();
        $.post('/AutoDelist/showChooseItem',{numIids:numIids}, function(data){
            if(!data){
                return;
            }
            var html = [];
            if(target.find('tbody tr:visible').length == 0 && data.length == 0){
                target.find('.empty-row').show();
            } else {
                var existIds = [];
                target.find('.itemrow').each(function(){
                    existIds.push($(this).attr('numIid'));
                });
                var existedNumIidsd = existIds.join(",");
                $.each(data,function(i, item){
                    if(existedNumIidsd.indexOf(item.id) < 0){
                        var even = "";
                        if(i%2 == 0){
                            even = "even";
                        }
                        var deListTime = DelistArea.util.correctTime(item.deListTime);
                        var href = "http://item.taobao.com/item.htm?id=" + item.id;
                        html.push('<tr numIid="'+item.id+'" class="itemrow '+even+'">');
                        html.push('<td><a target="_blank" href="'+href+'"><img style="width: 80px;height: 80px;" class="itemsnap" src="'+item.picURL+'"/></td>');
                        html.push('<td><a target="_blank" href="'+href+'">'+item.name+'</a></td>');
                        html.push('<td><span class="tmbtn sky-blue-btn delete-this-one">删除</span></td>');
                        html.push('</tr>');
                    }
                });
            }

            var dom = $(html.join(''));
            dom.appendTo(target);
            target.find('.delete-this-one').click(function(){
                $(this). parent().parent().remove();
                if(target.find('tbody tr:visible').length == 0){
                    target.find('.empty-row').show();
                }
            });
        });
    }

    TM.AudotDelist.checkInput = function(tbody){
        tbody.find('input:visible').each(function(){
            if($(this).val() == ""){
                $(this).val(0);
            } else if(parseInt($(this).val()) < 0){
                $(this).val(0);
            }
        });
    }

    TM.AudotDelist.refreshTdTotal = function(tbody, index){
        var tdCount = 0;
        tbody.find('td[index='+index+']').each(function(){
            if($(this).find('span:visible').length == 1){
                tdCount += parseInt($(this).find('span:visible').text());
            } else {
                tdCount += parseInt($(this).find('input:visible').val());
            }
        });
        if(tdCount < 0){
            tdCount = 0;
        }
        tbody.parent().find('th[index='+index+']').find('span').text(tdCount);
    }

    TM.AudotDelist.refreshTable = function(tbody){
        for(var i = 0; i < 7; i++) {
            TM.AudotDelist.refreshTdTotal(tbody,i)
        }
        var nowTotal = TM.AudotDelist.genNewTotal(tbody);
        var nowRemain = parseInt(tbody.parent().parent().parent().find('.total').text()) - nowTotal;
        tbody.parent().parent().parent().find('.already-alocated').text(nowTotal);
        tbody.parent().parent().parent().find('.already-alocated-bottom').text(nowTotal);
        tbody.parent().parent().parent().find('.un-alocated').text(nowRemain);
        tbody.parent().parent().parent().find('.un-alocated-bottom').text(nowRemain);
    }

    TM.AudotDelist.VerticleRefreshTable = function(tbody, index, count){
        tbody.parent().find('th span').eq(index-1).text(parseInt(tbody.parent().find('th span').eq(index-1).text()) + count);
        tbody.parent().parent().find('.already-alocated').text(parseInt(tbody.parent().parent().find('.already-alocated').text()) + count);
        tbody.parent().parent().find('.already-alocated-bottom').text(parseInt(tbody.parent().parent().find('.already-alocated-bottom').text()) + count);
        tbody.parent().parent().find('.un-alocated').text(parseInt(tbody.parent().parent().find('.un-alocated').text()) - count);
        tbody.parent().parent().find('.un-alocated-bottom').text(parseInt(tbody.parent().parent().find('.un-alocated-bottom').text()) - count);
    }

    TM.AudotDelist.HorizonRefreshTable = function(tbody, arr){
        var count = 0;
        for(var i = 0; i < 7; i++){
            tbody.parent().find('th span').eq(i).text(parseInt(tbody.parent().find('th span').eq(i).text()) + arr[i]);
            count += arr[i];
        }
        tbody.parent().parent().find('.already-alocated').text(parseInt(tbody.parent().parent().find('.already-alocated').text()) + count);
        tbody.parent().parent().find('.already-alocated-bottom').text(parseInt(tbody.parent().parent().find('.already-alocated-bottom').text()) + count);
        tbody.parent().parent().find('.un-alocated').text(parseInt(tbody.parent().parent().find('.un-alocated').text()) - count);
        tbody.parent().parent().find('.un-alocated-bottom').text(parseInt(tbody.parent().parent().find('.un-alocated-bottom').text()) - count);
    }

    TM.AudotDelist.SingleRefreshTable = function(tbody, index, count){
        tbody.parent().find('th span').eq(index-1).text(parseInt(tbody.parent().find('th span').eq(index-1).text()) + count);
        tbody.parent().parent().find('.already-alocated').text(parseInt(tbody.parent().parent().find('.already-alocated').text()) + count);
        tbody.parent().parent().find('.already-alocated-bottom').text(parseInt(tbody.parent().parent().find('.already-alocated-bottom').text()) + count);
        tbody.parent().parent().find('.un-alocated').text(parseInt(tbody.parent().parent().find('.un-alocated').text()) - count);
        tbody.parent().parent().find('.un-alocated-bottom').text(parseInt(tbody.parent().parent().find('.un-alocated-bottom').text()) - count);
    }

    TM.AudotDelist.genNewTotal = function(container) {
        var newTotal = 0;
        $(container).find('span:visible').each(function(){
            newTotal += parseInt($(this).text());
        });
        $(container).find('input:visible').each(function(){
            newTotal += parseInt($(this).val());
        });
        return newTotal;
    }

    TM.AudotDelist.genHour = function(hour){
        switch(hour){
            case 0 : return "0-1点";
            case 1 : return "1-2点";
            case 2 : return "2-3点";
            case 3 : return "3-4点";
            case 4 : return "4-5点";
            case 5 : return "5-6点";
            case 6 : return "6-7点";
            case 7 : return "7-8点";
            case 8 : return "8-9点";
            case 9 : return "9-10点";
            case 10 : return "10-11点";
            case 11 : return "11-12点";
            case 12 : return "12-13点";
            case 13 : return "13-14点";
            case 14 : return "14-15点";
            case 15 : return "15-16点";
            case 16 : return "16-17点";
            case 17 : return "17-18点";
            case 18 : return "18-19点";
            case 19 : return "19-20点";
            case 20 : return "20-21点";
            case 21 : return "21-22点";
            case 22 : return "22-23点";
            case 23 : return "23-24点";
            case 24 : return "总计";
        }
    }

    TM.AudotDelist.renderOpArea = function(){
        var planId = TM.AudotDelist.getPlanId();
        $.get('/delistplan/isOn?planId=' + planId,function(dataJson){
            if (TM.DelistBase.util.judgeAjaxResult(dataJson) == false) {
                return;
            }

            var isOn = dataJson.results;

            var switchStatus = TM.Switch.createSwitch.createSwitchForm("上下架计划开启状态");

            TM.AudotDelist.buildAutoStatus(switchStatus, isOn,
                function(){
                    TM.AudotDelist.refresh();
                },function(){
                    TM.AudotDelist.refresh();
                },
            me.itemDistri);
            switchStatus.appendTo(me.switchLine);
            me.opArea.find('.tzCheckBox').qtip({
                content: {
                    text: "虚拟充值平台类宝贝  暂不支持哟~"
                },
                position: {
                    at: "top left ",
                    corner: {
                        target: 'bottomRight'
                    }
                },
                show: {
                    when: false,
                    ready:true
                },
                hide:false,
                style:TM.widget.qtipStyle
            });
        });

    }

    TM.AudotDelist.itemDistriType = function() {
        $.get('/autodelist/getDistriType', function(data){
            var html = '<div class="autodelistInfo">' +

                '<div>' +
                '   <span class="distriTypeSpan">宝贝分布时间设置:</span>' +
                '</div>' +
                '<div>' +
                '   <span class="distriTypeName" style="">星期：</span>' +
                '   <div class="singleType">' +
                '       <input type="checkbox" name="itemDistriTime" class="distriCheck distriCheck0" checked="checked" value="0"><span class="oneTimeSpan">周一</span> ' +
                '   </div>' +
                '   <div class="singleType">' +
                '       <input type="checkbox" name="itemDistriTime" class="distriCheck distriCheck1" checked="checked" value="1"><span class="oneTimeSpan">周二</span> ' +
                '   </div>' +
                '   <div class="singleType">' +
                '       <input type="checkbox" name="itemDistriTime" class="distriCheck distriCheck2" checked="checked" value="2"><span class="oneTimeSpan">周三</span> ' +
                '   </div>' +
                '   <div class="singleType">' +
                '       <input type="checkbox" name="itemDistriTime" class="distriCheck distriCheck3" checked="checked" value="3"><span class="oneTimeSpan">周四</span> ' +
                '   </div>' +
                '   <div class="singleType">' +
                '       <input type="checkbox" name="itemDistriTime" class="distriCheck distriCheck4" checked="checked" value="4"><span class="oneTimeSpan">周五</span> ' +
                '   </div>' +
                '   <div class="singleType">' +
                '       <input type="checkbox" name="itemDistriTime" class="distriCheck distriCheck5" checked="checked" value="5"><span class="oneTimeSpan">周六</span> ' +
                '   </div>' +
                '   <div class="singleType">' +
                '       <input type="checkbox" name="itemDistriTime" class="distriCheck distriCheck6" checked="checked" value=6"><span class="oneTimeSpan">周日</span> ' +
                '   </div>' +
                '</div>' +
                '<div>' +
                '   <table style="margin: 5px 0px 10px 0px; border-collapse: collapse;" class="layout-table">' +
                '       <tbody>' +
                '       <tr>' +
                '           <td style="vertical-align: top;">' +
                '               <span class="distriTypeNameNoHeight">时间段：</span>' +
                '               <div style="margin-top: 40px; text-align: center;">' +
                '                   <span class="select-all-hour-btn commontextcursor" style="margin: 0 auto;">选中全部时间</span>' +
                '                   <div style="height: 10px;"></div> ' +
                '                   <span class="commontextcursor cancel-all-hour-btn" style="margin: 0 auto;">取消选中时间</span>' +
                '               </div> ' +
                '           </td>' +
                '           <td style="vertical-align: top;">' +
                /*'               <div class="simple-hour-setting-div">' +
                '                   <div class="singleType">' +
                '                       <input type="checkbox" name="itemDistriType" class="distriRadio morningRadio" value="3"><span class="oneTypeSpan">上午(9:00-12:00)</span>' +
                '                   </div>' +
                '                   <div class="singleType">' +
                '                       <input type="checkbox" name="itemDistriType" class="distriRadio afternoonRadio" value="4"><span class="oneTypeSpan">下午(14:00-17:00)</span>' +
                '                   </div>' +
                '                   <div class="singleType">' +
                '                       <input type="checkbox" name="itemDistriType" class="distriRadio nightRadio" value="2"><span class="oneTypeSpan">晚上(19:00-23:00)</span>' +
                '                   </div>' +
                '                   <div class="singleType" style="margin-left: 20px;">' +
                '                       <span class="advance-hour-setting-btn">高级设置>></span> ' +
                '                   </div>' +
                '               </div> ' +*/
                '               <div class="advance-hour-setting-div" style="">' +
                '                   <table class="advance-hour-setting-table layout-table">' +
                '                       <tbody>' +
                '                       ' +
                '                       </tbody>' +
                '                   </table> ' +
                '               </div>' +
                '           </td>' +
                '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '   ' +
                '   <div class="clearfix">' +
                '       <span class="  tmbtn sky-blue-btn reDistriBtn" style="margin-left: 30px; float: left;">提交设置</span>' +
                '       <span class=" clear-plan-btn tmbtn yellow-btn" style="display:none;margin-left: 30px; float: left;">清空计划</span>' +
                '       <div class="blank0"></div> ' +
                '   </div>' +
                '</div>' +
                '</div>';
            me.itemDistri.html(html);



            TM.AudotDelist.initCleanPlan(me.itemDistri);

            var distriType = data.state;
            var isShowBtn = data.isShowBtn;

            var distriHours = "";


            if (distriType == 0) {//均匀分布，所有时间
                me.itemDistri.find(".distriRadio").attr("checked", true);
                distriHours = "9,10,11,14,15,16,19,20,21,22";
            } else if (distriType == 1) {//白天
                me.itemDistri.find(".morningRadio").attr("checked", true);
                me.itemDistri.find(".afternoonRadio").attr("checked", true);
                distriHours = "9,10,11,14,15,16";
            } else if (distriType == 2) {//晚上
                me.itemDistri.find(".nightRadio").attr("checked", true);
                distriHours = "19,20,21,22";
            } else if (distriType == 3) {//上午
                me.itemDistri.find(".morningRadio").attr("checked", true);
                distriHours = "9,10,11";
            } else if (distriType == 4) {//下午
                me.itemDistri.find(".afternoonRadio").attr("checked", true);
                distriHours = "14,15,16";
            } else if (distriType == 5) {//上午和晚上
                me.itemDistri.find(".morningRadio").attr("checked", true);
                me.itemDistri.find(".nightRadio").attr("checked", true);
                distriHours = "9,10,11,19,20,21,22";
            } else if (distriType == 6) {//下午和晚上
                me.itemDistri.find(".afternoonRadio").attr("checked", true);
                me.itemDistri.find(".nightRadio").attr("checked", true);
                distriHours = "14,15,16,19,20,21,22";
            } else {
                me.itemDistri.find(".distriRadio").attr("checked", true);

            }

            if (data.distriHours === undefined || data.distriHours == null || data.distriHours == "") {

            } else {
                distriHours = data.distriHours;
            }

            var isDistriTime = data.isDistriTime;
            for (var i = 0; i < 7; i++) {
                if (isDistriTime[i] == false) {
                    me.itemDistri.find(".distriCheck" + i).attr("checked", false);
                }
            }

            if (isShowBtn == false) {
                //me.itemDistri.find(".reDistriBtn").remove();
            }

            me.itemDistri.find(".oneTypeSpan").click(function() {
                $(this).parent().find("input[name='itemDistriType']").click();
            });

            me.itemDistri.find(".oneTimeSpan").click(function() {
                $(this).parent().find("input[name='itemDistriTime']").click();
            });

            me.itemDistri.find(".reDistriBtn").click(function() {
                var returnVal = window.confirm("亲，你确定要重新分布宝贝上下架吗？");
                if(!returnVal) {
                    return;
                }

                var params = TM.AudotDelist.getParams(me.itemDistri);
                if (params === undefined || params == null)
                    return;

                $.ajax({
                    //async : false,
                    url : '/autodelist/reDistribute',
                    data : params,
                    type : 'post',
                    error: function() {
                    },
                    success: function (data) {
                        if (!data || data.res == false) {
                            TM.Alert.load('上下架重新分布成功');
                            TM.AudotDelist.refresh();
                        } else {
                            TM.Alert.load('上下架重新分布失败');
                        }
                    }
                });

            });


            TM.AudotDelist.initAdvanceHourSetting(me.itemDistri, distriHours);



        });
    }


    TM.AudotDelist.initAdvanceHourSetting = function(container, distriHours) {

        var distriHourArr = distriHours.split(",");
        var allHourArr = [];
        for (var i = 0; i < 24; i++) {
            allHourArr[i] = 0;
        }
        for (var i = 0; i < distriHourArr.length; i++) {
            allHourArr[distriHourArr[i]] = 1;
        }

        var htmlArr = [];
        //初始化table
        for (var i = 0; i < 6; i++) {
            htmlArr.push('<tr>');
            for (var j = 0; j < 4; j++) {
                var startHour = i * 4 + j;

                if (startHour <= 23) {
                    var endHour = startHour + 1;
                    var startStr = startHour + ":00";
                    var endStr = endHour + ":00";
                    if (startHour < 10) {
                        startStr = '0' + startStr;
                    }
                    if (endHour < 10) {
                        endStr = '0' + endStr;
                    }
                    var colSpanHtml = '';
                    if (startHour == 23) {
                        colSpanHtml = ' colspan="1" ';
                    }

                    var checkedHtml = "";
                    var checkSpanClass = "";
                    if (allHourArr[startHour] == 1) {
                        checkedHtml = ' checked="checked" ';
                        checkSpanClass = " checked-hour ";
                    }


                    htmlArr.push('<td style="" ' + colSpanHtml + ' ><div class="singleType"><input type="checkbox" class="advance-hour-check" value="' + startHour + '" ' + checkedHtml + '/> ');
                    htmlArr.push('<span class="advance-hour-span ' + checkSpanClass + '">' + startStr + '-' + endStr + '</span> ');
                    htmlArr.push('</div> </td>');
                } else {


                }


            }
            htmlArr.push('</tr>');
        }

        var hourSettingHtml = htmlArr.join('');
        var tbodyObj = container.find(".advance-hour-setting-table tbody");
        tbodyObj.html(hourSettingHtml);


        tbodyObj.find(".advance-hour-span").click(function() {
            var checkObj = $(this).parent().find(".advance-hour-check");
            checkObj.click();
            if (checkObj.is(":checked")) {
                $(this).addClass("checked-hour");
            } else {
                $(this).removeClass("checked-hour");
            }
        });

        container.find(".select-all-hour-btn").click(function() {
            tbodyObj.find(".advance-hour-check").attr("checked", true);
            tbodyObj.find(".advance-hour-span").addClass("checked-hour");
        });
        container.find(".cancel-all-hour-btn").click(function() {
            tbodyObj.find(".advance-hour-check").attr("checked", false);
            tbodyObj.find(".advance-hour-span").removeClass("checked-hour");
        });


    }


    TM.AudotDelist.initCleanPlan = function(container) {
        $.ajax({
            url : '/autodelist/hasSetDelist',
            data : {},
            type : 'post',
            error: function() {
            },
            success: function (dataJson) {
                if (TM.AudotDelist.judgeAjaxResult(dataJson) == false) {
                    return;
                }
                if (dataJson.res == true) {
                    container.find(".clear-plan-btn").show();

                    container.find(".clear-plan-btn").click(function() {
                        if (confirm("确定要清空原来安排的上下架计划？") == false) {
                            return;
                        }
                        $.ajax({
                            url : '/autodelist/backToOldDelist',
                            data : {},
                            type : 'post',
                            error: function() {
                            },
                            success: function (dataJson) {
                                if (TM.AudotDelist.judgeAjaxResult(dataJson) == false) {
                                    return;
                                }
                                location.reload();

                            }
                        });
                    });
                }

            }
        });
    }

    TM.AudotDelist.judgeAjaxResult = function(dataJson) {
        if (dataJson === undefined || dataJson == null) {
            return false;
        }

        var message = dataJson.message;
        if (message === undefined || message == null || message == "") {

        } else {
            alert(message);
        }


        var isSuccess = dataJson.success;

        return isSuccess;
    }


    TM.AudotDelist.getParams = function(distriTypeDiv) {
        var params = {};
        if (distriTypeDiv === undefined || distriTypeDiv == null || distriTypeDiv.length == 0) {
            params.distriType = 0;
            params.distriTimeArray = ["1","1","1","1","1","1","1"];
            return params;
        }
        var distriTypeObjs = distriTypeDiv.find("input[name='itemDistriType']:checked");
        if (distriTypeObjs.length == 0) {
            //TM.Alert.load("请先选择具体的分布类型");
            //return null;
        }
        var distriType = 0;
        var distriSum = 0;
        distriTypeObjs.each(function() {
            var value = parseInt($(this).val());
            distriSum += value;
        });

        if (distriSum == 9) {
            distriType = 0;//全部
        } else if (distriSum == 7){
            distriType = 1;//上午和下午
        } else if (distriSum == 5) {
            distriType = 5;//上午和晚上
        } else if (distriSum == 6) {
            distriType = 6;//下午和晚上
        } else
            distriType = distriSum;

        distriType = 7;//自定义

        var distriTimeObjs = me.itemDistri.find("input[name='itemDistriTime']:checked");
        var distriTimeArray = ["0","0","0","0","0","0","0"];
        if (distriTimeObjs.length == 0) {
            TM.Alert.load("请先选择具体的分布时间");
            return null;
        }
        distriTimeObjs.each(function() {
            var value = $(this).val();
            distriTimeArray[parseInt(value)] = "1";
        });

        params.distriType = distriType;
        params.distriTimeArray = distriTimeArray;


        var hourCheckObjs = me.itemDistri.find(".advance-hour-check:checked");
        if (hourCheckObjs.length <= 0) {
            TM.Alert.load("请先选择具体的分布时间段");
            return null;
        }
        var distriHourArray = [];
        hourCheckObjs.each(function() {
            var checkObj = $(this);
            distriHourArray[distriHourArray.length] = checkObj.val();
        });

        params.distriHours = distriHourArray.join(",");


        return params;
    };



    TM.AudotDelist.refresh = function() {
        var date = new Date();
        var nowTime = date.getTime();

        if (TM.AudotDelist.version != 2) {
            TM.AudotDelist.itemDistriType();
        }

        //DelistArea.list.doInit(nowTime);
        me.nav.find('.selected').find("a").click();
    }

    TM.AudotDelist.renderNoAutoListItems = function(){
        me.listArea.empty();
        var paramData = {};
        paramData.planId = TM.AudotDelist.getPlanId();
        var table = $('<div></div>');
        var bottom = $('<div style="text-align: center;"></div>');
        var addNoAutoList = $("<p style='padding-top: 20px;'><div class='tmbtn sky-blue-btn addNoAutoListItem' tag='show' style='margin-left: 10px;'>添加宝贝 </div><div class='tmbtn yellow-btn deleteAllNoAutoListItems' tag='show' style='margin-left: 10px;'>全部取消 </div></p>");
        addNoAutoList.unbind();
        bottom.tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount:1,
            ajax: {
                on: true,
                dataType: 'json',
                url: "/delistplan/listNoAutoListItems",
                param: paramData,
                callback:function(data){
                    table.empty();
                    bottom.empty();
                    if(!data){
                        return;
                    }
                    var html = [];
                    html.push('<table class="oplogs">');
                    html.push('<thead><tr><th>宝贝</th><th style="width:250px;">标题</th><th>下架时间</th><th>操作</th></tr></thead>');
                    html.push('<tbody>');

                    $.each(data.res,function(i, item){
                        var even = "";
                        if(i%2 == 0){
                            even = "even";
                        }
                        var deListTime = DelistArea.util.correctTime(item.deListTime);
                        html.push('<tr class="'+even+'">');
                        html.push('<td><img class="itemsnap" src="'+item.picURL+'"</td>');
                        html.push('<td>'+item.name+'</td>');
                        html.push('<td>'+new Date(deListTime).formatYMDMS()+'</td>');
                        html.push('<td><span class="addAutoList bluebutton" href="/delistplan/removeNoDelist?numIids='+item.id+'" >添加自动</span></td>');
                        html.push('</tr>');
                    });
                    html.push('</tbody>');
                    html.push('</table>');
                    var dom = $(html.join(''));
                    dom.find('.addAutoList').click(function(){
                        var btn = $(this);
                        var addParam = {};
                        addParam.planId = TM.AudotDelist.getPlanId();
                        if(confirm('亲,确认将宝贝添加到自动上下架序列中?')){
                            $.ajax({
                                url : btn.attr('href'),
                                type: "post",
                                data: addParam,
                                success : function(resultJson){
                                    if (TM.DelistBase.util.judgeAjaxResult(resultJson) == false) {
                                        return;
                                    }
                                    me.renderNoAutoListItems();
                                    TM.Alert.load('添加自动上下架宝贝成功');
                                },
                                error : function(){
                                    TM.Alert.load('添加自动上下架宝贝失败');
                                }
                            });
                        }
                    });

                    dom.appendTo(table);
                    table.appendTo(me.listArea);
                    bottom.appendTo(me.listArea);
                    addNoAutoList.appendTo(me.listArea);
                    $(".addNoAutoListItem").click(function(){

                        var actionCallback = function(resultJson) {
                            if (TM.DelistBase.util.judgeAjaxResult(resultJson) == false) {
                                return;
                            }
                            alert("排除宝贝添加成功！");
                            me.renderNoAutoListItems();
                        }

                        var planId = TM.AudotDelist.getPlanId();

                        multiOpByURL.createChoose.createOrRefleshCommsDiv({"itemsURL":"/delistplan/chooseItems?planId=" + planId ,"actionURL":"/delistplan/addNoAutoListItems?planId=" + planId,"pn":1,"px":8,"enableSearch":true, actionCallback: actionCallback});
                    });

                    $(".deleteAllNoAutoListItems").click(function(){
                        if(confirm("确定要删除不自动上架列表中的所有宝贝？")) {

                            $.ajax({
                                url : '/delistplan/removeAllNoDelist',
                                type: "post",
                                data: {planId: TM.AudotDelist.getPlanId()},
                                success : function(resultJson){
                                    if (TM.DelistBase.util.judgeAjaxResult(resultJson) == false) {
                                        return;
                                    }
                                    me.renderNoAutoListItems();
                                    TM.Alert.load('亲，删除成功~');
                                },
                                error : function(){
                                    TM.Alert.load("亲，删除失败，请重试或联系我们~");
                                }
                            });

                        }
                    });

                    var itemArray = data.res;
                    if (itemArray === undefined || itemArray == null || itemArray.length <= 0) {
                        bottom.hide();
                    } else {
                        bottom.show();
                    }
                }
            }

        });

    }

    TM.AudotDelist.bindTabListeners = function(){
        DelistArea.init.doInit();

        me.nav.find('a').click(function(){
            DelistArea.util.currentPage = 1;
            var anchor = $(this);
            me.nav.find('.selected').removeClass('selected');
            anchor.parent().addClass('selected');
            $(".autoDelistArea .listDiv").hide();
            switch(anchor.attr('tag')){
                case 'todayList':
                    me.listArea.empty();
                    DelistArea.init.createTodayDiv();
                    $(".autoDelistArea .autodelistTodayDiv").show();
                    DelistArea.list.todayTable(new Date().getTime());
                    break;
                case 'weekList':
                    me.listArea.empty();
                    DelistArea.init.createWeekDiv();
                    $(".autoDelistArea .autodelistWeekDiv").show();
                    DelistArea.list.weekTable(new Date().getTime());
                    break;
                case 'oplogs':
                    me.listArea.empty();
                    DelistArea.init.createLogDiv();
                    $(".autoDelistArea .autodelistLogDiv").show();
                    DelistArea.list.logTable(new Date().getTime());
                    break;
                case 'noautolistitems':
                    me.renderNoAutoListItems();
                    break;
                case 'autolistsetting':
                    me.listArea.empty();
                    DelistArea.manual.doInit(new Date().getTime());
                    break;
                case 'delisttime':
                    me.listArea.empty();
                    DelistArea.rpt.init();
                    break;
                case 'newDistributeRule':
                    me.listArea.empty();
                    me.renderRuleArea(me.listArea);
                    break;
                case 'oneKeySimplePlan':
                    me.listArea.empty();
                    SimplePlan.init.doInit(me.listArea);
            }
        });

        //me.nav.find('a:eq(0)').trigger('click');
    }

    TM.DelistArea = TM.DelistArea || {};
    var DelistArea = TM.DelistArea;

    DelistArea.init = DelistArea.init || {};
    DelistArea.init = $.extend({
        doInit: function() {
            //DelistArea.init.createTodayDiv();
            // DelistArea.init.createWeekDiv();
            //DelistArea.init.createLogDiv();
        },
        createTodayDiv: function() {
            var todayDiv = $("<div class='autodelistTodayDiv listDiv'  style='display: none;'></div>");
            todayDiv.append("<div class='todayPaging' style='text-align: center;'></div>");
            var todayTable = $("<table class='autodelistTodayTable listTable oplogs'>" +
                "<thead>" +
                "<tr class='tableHead'>" +
                "<td style='width: 13%;'>计划上架时间</td>" +
                "<td style='width: 13%;'>原上架时间</td>" +
                "<td style='width: 12%;'>宝贝图片</td>" +
                "<td style='width: 8%;'>价格</td>" +
                "<td style='width: 54%;'>宝贝标题</td>" +
                "</tr>" +
                "</thead>" +
                "<tbody>" +
                "</tbody>" +
                "</table>");
            todayDiv.append(todayTable);
            todayDiv.append("<div class='todayPaging' style='text-align: center;'></div>");
            $(".autoDelistArea").append(todayDiv);
        },
        createWeekDiv: function() {
            var weekDiv = $("<div class='autodelistWeekDiv listDiv' style='display: none;'></div>");
            weekDiv.append("<div class='weekPaging' style='text-align: center;'></div>");
            var weekTable = $("<table class='autodelistWeekTable listTable oplogs'>" +
                "<thead>" +
                "<tr class='tableHead'>" +
                "<td style='width: 15%;'>上架计划</td>" +
                "<td style='width: 15%;'>原上架时间</td>" +
                "<td style='width: 12%;'>宝贝图片</td>" +
                "<td style='width: 8%;'>价格</td>" +
                "<td style='width: 50%;'>宝贝标题</td>" +
                "</tr>" +
                "</thead>" +
                "<tbody>" +
                "</tbody>" +
                "</table>");
            weekDiv.append(weekTable);
            weekDiv.append("<div class='weekPaging' style='text-align: center;'></div>");
            $(".autoDelistArea").append(weekDiv);
        },
        createLogDiv: function() {
            var logDiv = $("<div class='autodelistLogDiv listDiv' style='display: none;'></div>");

            var searchHtml = '<div class="">' +
                '<table class="log-search-table"  style="border-collapse: collapse; margin: 10px 0px 10px 10px;">' +
                '   <tbody>' +
                '   <tr>' +
                '       <td><span>宝贝标题：</span></td>' +
                '       <td><input type="text" class="log-title-text" style="font-size: 14px;width: 300px;"/></td>' +
                '       <td style="padding-left: 10px;"><span class="tmbtn short-green-btn log-search-btn">搜索日志</span></td>' +
                '   </tr>' +
                '   </tbody>' +
                '</table> ' +
                '</div> ';
            logDiv.append(searchHtml);

            logDiv.append("<div class='logPaging' style='text-align: center;'></div>");

            var logTable = $("<table class='autodelistLogTable listTable oplogs'>" +
                "<thead>" +
                "<tr class='tableHead'>" +
                "<td style='width: 13%;'>上架时间</td>" +
                "<td style='width: 12%;'>宝贝图片</td>" +
                "<td style='width: 8%;'>价格</td>" +
                "<td style='width: 30%;'>宝贝标题</td>" +
                "<td style='width: 12%;'>状态</td>" +
                "<td style='width: 25%;'>上架消息</td>" +
                "</tr>" +
                "</thead>" +
                "<tbody>" +
                "</tbody>" +
                "</table>");
            logDiv.append(logTable);
            logDiv.append("<div class='logPaging' style='text-align: center;'></div>");
            $(".autoDelistArea").append(logDiv);

            if (DelistArea.init.logSearchTitle === undefined) {

            } else {
                logDiv.find(".log-title-text").val(DelistArea.init.logSearchTitle);
            }


            logDiv.find(".log-title-text").keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    logDiv.find(".log-search-btn").click();
                }
            });
            logDiv.find(".log-search-btn").click(function() {
                var nowTime = new Date().getTime();
                DelistArea.init.logSearchTitle = logDiv.find(".log-title-text").val();
                DelistArea.list.logTable(new Date().getTime());
            });
        }
    }, DelistArea.init);

    DelistArea.list = DelistArea.list || {};
    DelistArea.list = $.extend({
        doInit: function(nowTime) {
            //DelistArea.list.todayTable(nowTime);
            //DelistArea.list.weekTable(nowTime);
            //DelistArea.list.logTable(nowTime);
        },
        todayTable: function(nowTime) {
            var callback = function(trObj, itemJson) {
                var listTime = itemJson.listTime;
                var timeStr = DelistArea.util.parseTime(listTime);
                var timeTd = $("<td></td>");
                timeTd.html(timeStr);
                trObj.append(timeTd);

                DelistArea.util.addRealDelistTimeTd(trObj, itemJson);

                DelistArea.util.createTds(trObj, itemJson);
            };
            var targetObj = $(".autoDelistArea .autodelistTodayTable tbody");
            var url = "/delistplan/getTodayDelist";
            var pagingObj = $(".autoDelistArea .todayPaging");
            DelistArea.util.showList(targetObj, url, nowTime, callback, pagingObj, null);
        },
        weekTable: function(nowTime) {
            var callback = function(trObj, itemJson) {
                var listTime = itemJson.listTime;
                var array = [
                    "周日", "周一", "周二", "周三", "周四", "周五", "周六"
                ];
                listTime = DelistArea.util.correctTime(listTime);
                var theDate = new Date(listTime);
                var day = array[theDate.getDay()];
                var hour = theDate.getHours();
                var minutes = theDate.getMinutes();
                var second = theDate.getSeconds();

                if (hour < 10) {
                    hour = "0" + hour;
                }
                if (minutes < 10) {
                    minutes = "0" + minutes;
                }
                if (second < 10) {
                    second = "0" + second ;
                }

                var timeStr = day+" "+hour+":"+minutes+":"+second;
                var timeTd = $("<td></td>");
                timeTd.html(timeStr);
                trObj.append(timeTd);

                DelistArea.util.addRealDelistTimeTdWithWeek(trObj, itemJson);

                DelistArea.util.createTds(trObj, itemJson);
            };
            var targetObj = $(".autoDelistArea .autodelistWeekTable tbody");
            var url = "/delistplan/getWeekDelist";
            var pagingObj = $(".autoDelistArea .weekPaging");
            DelistArea.util.showList(targetObj, url, nowTime, callback, pagingObj, null);
        },
        logTable: function(nowTime) {
            var callback = function(trObj, itemJson) {
                var listTime = itemJson.listTime;
                var timeStr = DelistArea.util.parseTime(listTime);
                var timeTd = $("<td></td>");
                timeTd.html(timeStr);
                trObj.append(timeTd);
                DelistArea.util.createTds(trObj, itemJson);
                //状态
                var stateTd = $("<td><span></span></td>");
                if (itemJson.status == 0) {
                    stateTd.find("span").html("上架成功");
                    //stateTd.find("span").css("color", "#a10000");
                } else if (itemJson.status == 8) {
                    stateTd.find("span").html("宝贝属性有错");
                    stateTd.find("span").css("color", "#a10000");
                } else {
                    stateTd.find("span").html("上架失败");
                    stateTd.find("span").css("color", "#a10000");
                }
                trObj.append(stateTd);
                var msgTd = $("<td><span style='word-break:break-word;'></span></td>");
                if (itemJson.status == 0) {
                    msgTd.find("span").html("-");
                    //msgTd.find("span").css("color", "#a10000");
                } else if (itemJson.status == 8) {
                    msgTd.find("span").html(itemJson.opMsg);
                    //msgTd.find("span").css("color", "#a10000");
                } else {
                    msgTd.find("span").html(itemJson.opMsg);
                    //msgTd.find("span").css("color", "#a10000");
                }
                trObj.append(msgTd);
            };
            var targetObj = $(".autoDelistArea .autodelistLogTable tbody");
            var url = "/delistplan/getDelistLog";
            var pagingObj = $(".autoDelistArea .logPaging");

            var title = $('.log-search-table .log-title-text').val();

            DelistArea.util.showList(targetObj, url, nowTime, callback, pagingObj, title);
        }
    }, DelistArea.list);


    DelistArea.util = DelistArea.util || {};
    DelistArea.util = $.extend({
        currentPage: 1,
        showList: function(targetObj, url, nowTime, callback, pagingObj, title, currentPage) {
            targetObj.html("");
            var param = {
                nowTime: nowTime,
                pn:1,
                ps:10
            };
            if (title === undefined || title == null)
                ;
            else
                param.title = title;

            if (currentPage === undefined) {
                currentPage = 1;
            }

            param.planId = TM.AudotDelist.getPlanId();

            pagingObj.tmpage({
                currPage: currentPage,
                pageSize: 10,
                pageCount:1,
                ajax: {
                    on: true,
                    param: param,
                    dataType: 'json',
                    url: url,
                    callback:function(resultJson){
                        targetObj.html("");
                        var itemArray = resultJson.res;
                        DelistArea.util.currentPage = resultJson.pn;
                        var rowIndex = 0;
                        $(itemArray).each(function(index, itemJson) {
                            if (itemJson.price > 0) {

                                var trObj = $("<tr></tr>");
                                callback(trObj, itemJson);

                                if (rowIndex % 2 == 0)
                                    trObj.addClass("evenRow");
                                targetObj.append(trObj);
                                rowIndex++;
                            }


                        });

                        if (itemArray === undefined || itemArray == null || itemArray.length <= 0) {
                            pagingObj.hide();
                        } else {
                            pagingObj.show();
                        }

                    }
                }

            });



















            /*
             var data = {};
             data.currentPage = 1;
             data.pageSize = 0;
             data.nowTime = nowTime;
             $.ajax({
             url: url,
             dataType: 'json',
             type: 'post',
             data: data,
             error: function() {
             },
             success: function (resultJson) {
             var totalCount = resultJson.totalCount;

             var pagingCallback = function(currentPage, jq) {
             DelistArea.util.doQuery(targetObj, url, nowTime, callback, currentPage + 1);
             };
             DelistArea.util.initPagination(pagingObj, pagingCallback, totalCount);
             }
             });*/
        },
        initPagination: function(pagingObj, callback, totalCount) {
            pagingObj.pagination(totalCount, {
                num_display_entries : 3, // 主体页数
                num_edge_entries : 2, // 边缘页数
                callback : callback,
                current_page: 0,
                link_to: 'javascript:void(0);',
                items_per_page : 10,// 每页显示多少项
                prev_text : "&lt上一页",
                next_text : "下一页&gt"
            });

        },
        doQuery: function(targetObj, url, nowTime, callback, currentPage) {
            targetObj.html("");
            var data = {};
            data.currentPage = currentPage;
            data.pageSize = 10;
            data.nowTime = nowTime;
            $.ajax({
                url : url,
                data : data,
                type : 'post',
                error: function() {
                },
                success: function (dataJson) {
                    var itemArray = dataJson.timeList;
                    $(itemArray).each(function(index, itemJson) {
                        var trObj = $("<tr></tr>");
                        callback(trObj, itemJson);

                        if (index % 2 == 0)
                            trObj.addClass("evenRow");
                        targetObj.append(trObj);
                    });
                }
            });
        },
        //图片，价格，标题单元格的生成
        createTds: function(trObj, itemJson) {
            var url = "http://item.taobao.com/item.htm?id=" + itemJson.numIid;
            var imgTd = $("<td><a target='_blank'><img /></a></td>");
            imgTd.find("img").attr("src", itemJson.picPath);
            imgTd.find("a").attr("href", url);
            var priceTd = $("<td></td>");
            priceTd.html("￥" + itemJson.price);
            var titleTd = $("<td style='padding: 0 10px;'><a target='_blank'></a></td>");
            titleTd.find("a").attr("href", url);
            titleTd.find("a").html(itemJson.title);

            trObj.append(imgTd);
            trObj.append(priceTd);
            trObj.append(titleTd);
        },
        parseTime: function(listTime) {
            listTime = DelistArea.util.correctTime(listTime);
            var theDate = new Date(listTime);
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
                second = "0" + second ;
            }

            var timeStr = year+"-"+month+"-"+date+" "+hour+":"+minutes+":"+second;
            return timeStr;
        },
        correctTime: function(time) {
            var theDate = new Date(time);
            var offset = theDate.getTimezoneOffset() * 1000 * 60;


            var baseOffset = -480;//东8区

            var realTime = time - baseOffset * 1000 * 60 + offset;

            return realTime;
        },
        addRealDelistTimeTd: function(trObj, itemJson) {

            var realDelistTime = itemJson.realDelistTime - 7 * 24 * 3600 * 1000;

            var realDelistTimeStr = DelistArea.util.parseTime(realDelistTime);
            var realTimeTd = $('<td>' + realDelistTimeStr + '</td>')
            trObj.append(realTimeTd);

        },
        addRealDelistTimeTdWithWeek: function(trObj, itemJson) {
            var realDelistTime = itemJson.realDelistTime;
            var realDelistTimeStr = DelistArea.util.parseWeekTime(realDelistTime);
            var realTimeTd = $('<td>' + realDelistTimeStr + '</td>')
            trObj.append(realTimeTd);
        },
        parseWeekTime: function(listTime) {
            var array = [
                "周日", "周一", "周二", "周三", "周四", "周五", "周六"
            ];
            listTime = DelistArea.util.correctTime(listTime);
            var theDate = new Date(listTime);
            var day = array[theDate.getDay()];
            var hour = theDate.getHours();
            var minutes = theDate.getMinutes();
            var second = theDate.getSeconds();

            if (hour < 10) {
                hour = "0" + hour;
            }
            if (minutes < 10) {
                minutes = "0" + minutes;
            }
            if (second < 10) {
                second = "0" + second ;
            }

            var timeStr = day+" "+hour+":"+minutes+":"+second;
            return timeStr;
        }
    }, DelistArea.util);

    /**
     * 手动设置宝贝上下架
     * @type {*}
     */
    DelistArea.manual = DelistArea.manual || {};
    DelistArea.manual = $.extend({
        searchTitle: '',
        doInit: function(nowTime) {
            DelistArea.manual.createHtml();
            DelistArea.manual.showItems(nowTime);
        },
        createHtml: function() {
            var weekDiv = $("<div class=''></div>");
            var searchHtml = '<div class="itemsearchdiv">' +
                '<table class="searchtable"  style="margin: 10px 0px 0px 10px;">' +
                '   <tbody>' +
                '   <tr>' +
                '       <td><span class="searchlabel">宝贝标题：</span></td>' +
                '       <td><input type="text" class="searchtext" style="font-size: 14px;width: 300px;"/></td>' +
                '       <td style="padding-left: 10px;"><span class="tmbtn short-green-btn delist-searchbtn">搜索宝贝</span></td>' +
                '   </tr>' +
                '   </tbody>' +
                '</table> ' +
                '</div> ';
            weekDiv.append(searchHtml);
            weekDiv.append("<div class='manualPaging' style='text-align: center;'></div>");
            var weekTable = $("<table class='manualTable listTable oplogs'>" +
                "<thead>" +
                "<tr class='tableHead'>" +
                "<td style='width: 13%;'>计划上架时间</td>" +
                "<td style='width: 13%;'>原上架时间</td>" +
                "<td style='width: 12%;'>宝贝图片</td>" +
                "<td style='width: 8%;'>价格</td>" +
                "<td style='width: 34%;'>宝贝标题</td>" +
                "<td style='width: 20%;'>修改上架时间</td>" +
                "</tr>" +
                "</thead>" +
                "<tbody>" +
                "</tbody>" +
                "</table>");
            weekDiv.append(weekTable);
            weekDiv.append("<div class='manualPaging' style='text-align: center;'></div>");
            $(".autoDelistArea").append(weekDiv);

            weekDiv.find(".searchtext").val(DelistArea.manual.searchTitle);

            weekDiv.find(".searchtext").keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    weekDiv.find(".delist-searchbtn").click();
                }
            });
            weekDiv.find(".delist-searchbtn").click(function() {
                var nowTime = new Date().getTime();
                DelistArea.manual.searchTitle = weekDiv.find(".searchtext").val();
                DelistArea.manual.showItems(nowTime);
            });
        },
        showItems: function(nowTime) {
            /*$.ajax({
                url : "/AutoDelist/hasInitDelistTimes",
                data : {'_ts':new Date().getTime()},
                type : 'post',
                error: function() {
                },
                success: function (dataJson) {
                    if (dataJson == false) {
                        alert("请先开启自动上下架");
                        return;
                    }
                    DelistArea.manual.doShowItems(nowTime);
                }
            });*/

            DelistArea.manual.doShowItems(nowTime);
        },
        doShowItems: function(nowTime) {
            var callback = function(trObj, itemJson) {
                var listTime = itemJson.listTime;

                var array = [
                    "周日", "周一", "周二", "周三", "周四", "周五", "周六"
                ];
                listTime = DelistArea.util.correctTime(listTime);
                var theDate = new Date(listTime);
                var day = array[theDate.getDay()];
                var hour = theDate.getHours();
                var minutes = theDate.getMinutes();
                var second = theDate.getSeconds();

                if (hour < 10) {
                    hour = "0" + hour;
                }
                if (minutes < 10) {
                    minutes = "0" + minutes;
                }
                if (second < 10) {
                    second = "0" + second ;
                }

                var timeStr = day+" "+hour+":"+minutes+":"+second;

                var timeTd = $("<td></td>");
                timeTd.html(timeStr);
                trObj.append(timeTd);

                DelistArea.util.addRealDelistTimeTdWithWeek(trObj, itemJson);
                //itemJson.numIid = itemJson.id;
                //itemJson.picPath = itemJson.picURL;
                DelistArea.util.createTds(trObj, itemJson);

                //修改的单元格
                var html = '<td>' +
                    '<select class="week-select">' +
                    '   <option value="0" selected="selected">周日</option> ' +
                    '   <option value="1">周一</option> ' +
                    '   <option value="2">周二</option> ' +
                    '   <option value="3">周三</option> ' +
                    '   <option value="4">周四</option> ' +
                    '   <option value="5">周五</option> ' +
                    '   <option value="6">周六</option> ' +
                    '</select> ' +
                    '<input type="text" class="delist-time" style="font-size: 14px;" /> ' +
                    '<span class="tmbtn blue-short-btn save-btn" style="">保存</span> ' +
                    '</td>';

                var modifyTd = $(html);
                modifyTd.find(".week-select").val(theDate.getDay());
                modifyTd.find(".delist-time").val(hour+":"+minutes+":"+second);
                modifyTd.find(".save-btn").attr("numIid", itemJson.numIid);
                modifyTd.find(".save-btn").click(function() {
                    DelistArea.manual.modifyDelist($(this));
                });
                modifyTd.find(".delist-time").keydown(function(event) {
                    if (event.keyCode == 13) {//按回车
                        $(this).parent().find(".save-btn").click();
                    }
                });
                //modifyTd.find(".delist-time").attr("readonly", "readonly");
                modifyTd.find(".delist-time").timepicker({
                    timeOnlyTitle: "选择时间：",
                    currentText: "当前时间",
                    closeText: "关闭",
                    timeFormat: "HH:mm:ss",
                    timeText: "时间",
                    hourText: "小时",
                    minuteText: '分',
                    secondText: '秒',
                    showSecond: true,
                    timeOnly: true,
                    controlType: 'select'
                });
                trObj.append(modifyTd);
            };
            var targetObj = $(".autoDelistArea .manualTable tbody");
            var url = "/delistplan/queryDelistTimes";
            var pagingObj = $(".autoDelistArea .manualPaging");
            DelistArea.util.showList(targetObj, url, nowTime, callback, pagingObj, DelistArea.manual.searchTitle, DelistArea.util.currentPage);
        },
        modifyDelist: function(btnObj) {
            if (confirm("确定修改该宝贝的上架时间？") == false)
                return;
            var selectObj = btnObj.parent().find(".week-select");
            var timeObj = btnObj.parent().find(".delist-time");
            var data = {};
            data.numIid = btnObj.attr("numIid");
            var weekIndex = selectObj.val();
            if (weekIndex === undefined || weekIndex == null || weekIndex == "") {
                alert("请先选择要修改的时间");
                return;
            }
            data.weekIndex = weekIndex;
            var timeStr = timeObj.val();
            if (timeStr === undefined || timeStr == null || timeStr == "") {
                alert("请先选择要修改的时间");
                return;
            }
            data.timeStr = timeStr;
            data.planId = TM.AudotDelist.getPlanId();
            //data.numIid = 0;
            $.ajax({
                url : "/delistplan/modifyDelistTime",
                data : data,
                type : 'post',
                error: function() {
                },
                success: function (dataJson) {
                    if (TM.DelistBase.util.judgeAjaxResult(dataJson) == false) {
                        return;
                    }
                    //刷新
                    DelistArea.manual.doShowItems(new Date().getTime());
                }
            });
        }
    }, DelistArea.manual);

    DelistArea.rpt  =  DelistArea.rpt || {};
    DelistArea.rpt.init = function(){
        TM.gcs('/js/jquery.tmpl.js',function(){
            $.get('/AutoDelist/queryDelistDistribute',function(arr){
                DelistArea.rpt.doAjaxRes(arr);
            });

        })
    }
    DelistArea.rpt.doAjaxRes = function(arr){
        var container = $('.weekTimes').clone();
        container.find('.planRow').append(DelistArea.rpt.wrap(arr[0]));
        container.find('.realRow').append(DelistArea.rpt.wrap(arr[1]));
        container.show();
        container.appendTo(me.listArea);
    }

    DelistArea.rpt.wrap = function(week){
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





    var PlanConfig = PlanConfig || {};

    PlanConfig.init = PlanConfig.init || {};
    PlanConfig.init = $.extend({
        doInit: function(container, planJson) {

            //手动选宝贝的，没有配置
            if (planJson.userSelectItemType == true) {
                var html = PlanConfig.init.createSelectItemTip();
                container.html(html);
                return;
            }

            var html = PlanConfig.init.createHtml();
            container.html(html);




            if (planJson.filterGoodSalesItem == true) {
                container.find('.first10sale[status="true"]').attr("checked", true);
            } else {
                container.find('.first10sale[status="false"]').attr("checked", true);
            }


            if (planJson.autoAddNewItem == true) {
                container.find('.add-new-item[status="true"]').attr("checked", true);
            } else {
                container.find('.add-new-item[status="false"]').attr("checked", true);
            }


            container.find(".modify-plan-btn").click(function() {

                if (confirm("确定要修改上下架计划的配置？") == false) {
                    return;
                }

                var paramData = {};
                paramData.planId = TM.AudotDelist.getPlanId();
                paramData.isAutoAddNewItem = container.find(".add-new-item:checked").attr("status");
                paramData.isFilterGoodSalesItem = container.find(".first10sale:checked").attr("status");

                $.ajax({
                    url : "/delistplan/modifyPlanConfig",
                    data : paramData,
                    type : 'post',
                    error: function() {
                    },
                    success: function (dataJson) {
                        if (TM.DelistBase.util.judgeAjaxResult(dataJson) == false) {
                            return;
                        }
                        alert("上下架计划修改成功！");
                        location.reload();
                    }
                });

            });

        },
        createHtml: function() {
            /*var html = '' +
                '<div class="plan-config-area">' +
                '   <table>' +
                '       <tbody>' +
                '       <tr>' +
                '           <td><span class="tip-label">上下架计划配置</span> </td>' +
                '           <td style="padding-left: 20px"><input type="checkbox" class="filter-sales-check" /> </td> ' +
                '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '</div> ' +
                '';*/

            var html = '' +
                '<div class="plan-config-div">' +
                '   <div class="headspan" style="width: 97%;">上下架计划配置</div> ' +
                '   <table class="plan-config-table" style="margin: 10px 0px 10px 50px;">' +
                '       <tbody>' +
                '       <tr>' +
                '           <td>' +
                '               <b>1.</b>&nbsp;&nbsp;排除销量前10的宝贝？ &nbsp;&nbsp;' +
                '               <input type="radio" name="first10sale" class="first10sale" style="width: 20px;" status="true" />是 ' +
                '               &nbsp;&nbsp;<input type="radio" name="first10sale" class="first10sale" style="width: 20px;" status="false" />否 ' +
                '           </td>' +
                '           <td rowspan="2" style="padding-left: 70px;">' +
                '               <span class="tmbtn too-wide-yellow-btn modify-plan-btn">修改计划配置</span> ' +
                '           </td> ' +
                '       </tr>' +
                '       <tr>' +
                '           <td>' +
                '               <b>2.</b>&nbsp;&nbsp;自动添加新增宝贝？ &nbsp;&nbsp;' +
                '               <input type="radio" name="add-new-item" class="add-new-item" style="width: 20px;" status="true" />是 ' +
                '               &nbsp;&nbsp;<input type="radio" name="add-new-item" class="add-new-item" style="width: 20px;" status="false" />否 ' +
                '           </td>' +
                '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '   ' +
                '</div> ' +
                '';

            return html;
        },
        createSelectItemTip: function() {
            var html = '' +
                '<div class="plan-config-div">' +
                '   <div class="headspan" style="width: 97%;">当前计划为手动选择宝贝计划，不支持自动添加新增宝贝。</div> ' +
                '   ' +
                '</div> ' +
                '';

            return html;
        }
    }, PlanConfig.init);


    var SimplePlan = SimplePlan || {};

    SimplePlan.init = SimplePlan.init || {};
    SimplePlan.init = $.extend({
        doInit: function(container) {
            var html = SimplePlan.init.createHtml();

            container.html(html);

            var planId = TM.AudotDelist.getPlanId();
            $.ajax({
                url : '/delistplan/queryDelistPlan',
                type: "post",
                data: {planId: planId},
                success : function(resultJson){
                    if (TM.DelistBase.util.judgeAjaxResult(resultJson) == false) {
                        location.href = "/kits/delistPlans";
                        return;
                    }

                    var planJson = resultJson.results;

                    var distriNums = planJson.hourRates;

                    SimplePlan.init.setDistriChecked(container, distriNums);

                },
                error : function(){

                }
            });


            container.find(".week-distri-check, .day-distri-check").click(function() {
                var spanObj = $(this).parent().find('span');

                if ($(this).is(":checked")) {
                    spanObj.addClass("checked-hour");
                } else {
                    spanObj.removeClass("checked-hour");
                }
            });
            container.find(".week-distri-span, .day-distri-span").click(function() {
                var checkObj = $(this).parent().find('input[type="checkbox"]');

                if (checkObj.is(":checked") == false) {
                    checkObj.attr("checked", true);
                    $(this).addClass("checked-hour");
                } else {
                    checkObj.attr("checked", false);
                    $(this).removeClass("checked-hour");
                }
            });

            container.find(".select-all-hour-btn").click(function() {
                container.find(".day-distri-check").attr("checked", true);
                container.find(".day-distri-span").addClass("checked-hour");
            });
            container.find(".cancel-all-hour-btn").click(function() {
                container.find(".day-distri-check").attr("checked", false);
                container.find(".day-distri-span").removeClass("checked-hour");
            });



            container.find(".simple-plan-btn").click(function() {

                var weekArray = [];
                var weekCheckObj = container.find(".week-distri-check:checked");
                weekCheckObj.each(function() {
                    var checkObj = $(this);
                    var weekIndex = checkObj.val();
                    if (weekIndex == "0") {
                        weekIndex = "7";
                    }
                    weekArray[weekArray.length] = weekIndex;
                });
                var hourArray = [];
                var hourCheckObj = container.find(".day-distri-check:checked");
                hourCheckObj.each(function() {
                    var checkObj = $(this);
                    hourArray[hourArray.length] = checkObj.val();

                });

                if (weekArray.length <= 0) {
                    alert("请先选择一周宝贝分布！");
                    return;
                }
                if (hourArray.length <= 0) {
                    alert("请先选择每天宝贝分布！");
                    return;
                }


                if (confirm("确定要一键均匀分布宝贝，如确定，则原来的计划将被取代？") == false) {
                    return;
                }

                var paramData = {};
                paramData.planId = TM.AudotDelist.getPlanId();
                paramData.weeks = weekArray.join(',');
                paramData.hours = hourArray.join(',');

                $.ajax({
                    url : '/delistplan/simpleReDistribute',
                    type: "post",
                    data: paramData,
                    success : function(resultJson){
                        if (TM.DelistBase.util.judgeAjaxResult(resultJson) == false) {

                            return;
                        }

                        alert("一键均匀分布成功！");
                        location.reload();

                    },
                    error : function(){

                    }
                });

            });


        },
        setDistriChecked: function(container, distriNums) {
            var weekArray = [0, 0, 0, 0, 0, 0, 0];
            var hourArray = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];

            if (distriNums === undefined || distriNums == null || distriNums == "") {
                weekArray = [1, 1, 1, 1, 1, 1, 1];
                hourArray = [0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0];
            } else {
                var distriNumArray = distriNums.split(",");
                if (distriNumArray.length != 7 * 24) {
                    weekArray = [1, 1, 1, 1, 1, 1, 1];
                    hourArray = [0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0];
                } else {
                    for (var weekIndex = 0; weekIndex < 7; weekIndex++) {

                        for (var hourIndex = 0; hourIndex < 24; hourIndex++) {
                            if (distriNumArray[weekIndex * 24 + hourIndex] > 0) {
                                weekArray[weekIndex] = 1;
                                hourArray[hourIndex] = 1;
                            }
                        }
                    }
                }
            }


            for (var i = 0; i < weekArray.length; i++) {
                var checkObj = container.find('.week-distri-check[value="' + i + '"]');
                var spanObj = checkObj.parent().find("span");
                if (weekArray[i] > 0) {
                    checkObj.attr("checked", true);
                    spanObj.addClass("checked-hour");
                } else {
                    checkObj.attr("checked", false);
                    spanObj.removeClass("checked-hour");
                }
            }

            for (var i = 0; i < hourArray.length; i++) {
                var checkObj = container.find('.day-distri-check[value="' + i + '"]');
                var spanObj = checkObj.parent().find("span");
                if (hourArray[i] > 0) {
                    checkObj.attr("checked", true);
                    spanObj.addClass("checked-hour");
                } else {
                    checkObj.attr("checked", false);
                    spanObj.removeClass("checked-hour");
                }
            }

        },
        createHtml: function() {
            var html = '' +
                '<div class="simple-div-plan" style="padding: 10px 0px 70px 50px;">' +
                '   <div style="padding: 20px 0px; font-size: 16px;">一键均匀上下架优化计划，将宝贝<span style="color: #a10000;font-weight: bold;">均匀分布</span>到您所勾选的时间段。</div>' +
                '   <table class="simple-plan-table week-distri-table">' +
                '       <tbody>' +
                '       <tr>' +
                '           <td style="font-weight: bold;">一周宝贝分布：</td>' +
                '           <td style="padding-left: 20px;">' +
                '               <input type="checkbox" class="week-distri-check" value="1"><span class="week-distri-span">周一</span>' +
                '           </td> ' +
                '           <td style="padding-left: 15px;">' +
                '               <input type="checkbox" class="week-distri-check" value="2"><span class="week-distri-span">周二</span>' +
                '           </td> ' +
                '           <td style="padding-left: 15px;">' +
                '               <input type="checkbox" class="week-distri-check" value="3"><span class="week-distri-span">周三</span>' +
                '           </td> ' +
                '           <td style="padding-left: 15px;">' +
                '               <input type="checkbox" class="week-distri-check" value="4"><span class="week-distri-span">周四</span>' +
                '           </td> ' +
                '           <td style="padding-left: 15px;">' +
                '               <input type="checkbox" class="week-distri-check" value="5"><span class="week-distri-span">周五</span>' +
                '           </td> ' +
                '           <td style="padding-left: 15px;">' +
                '               <input type="checkbox" class="week-distri-check" value="6"><span class="week-distri-span">周六</span>' +
                '           </td> ' +
                '           <td style="padding-left: 15px;">' +
                '               <input type="checkbox" class="week-distri-check" value="0"><span class="week-distri-span">周日</span>' +
                '           </td> ' +
                '       </tr>' +
                '       </tbody>' +
                '   </table> ' +
                '   <table class="simple-plan-table day-distri-table" style="margin-top: 10px;">' +
                '       <tbody>' +
                '       <tr>' +
                '           <td style="vertical-align: top;" rowspan="6">' +
                '               <span style="font-weight: bold; ">一日宝贝分布：</span>' +
                '               <div style="margin-top: 30px; text-align: center;">' +
                '                   <span class="select-all-hour-btn commontextcursor" style="margin: 0 auto;">选中全部时间</span>' +
                '                   <div style="height: 20px;"></div> ' +
                '                   <span class="commontextcursor cancel-all-hour-btn" style="margin: 0 auto;">取消选中时间</span>' +
                '               </div> ' +
                '           </td>' +
                '           <td style="padding-left: 20px;">' +
                '               <input type="checkbox" class="day-distri-check" value="0"><span class="day-distri-span">00:00-01:00</span>' +
                '           </td> ' +
                '           <td style="padding-left: 15px;">' +
                '               <input type="checkbox" class="day-distri-check" value="1"><span class="day-distri-span">01:00-02:00</span>' +
                '           </td> ' +
                '           <td style="padding-left: 15px;">' +
                '               <input type="checkbox" class="day-distri-check" value="2"><span class="day-distri-span">02:00-03:00</span>' +
                '           </td> ' +
                '           <td style="padding-left: 15px;">' +
                '               <input type="checkbox" class="day-distri-check" value="3"><span class="day-distri-span">03:00-04:00</span>' +
                '           </td> ' +
                '       </tr>' +
                '       <tr>' +
                '           <td style="padding-left: 20px;">' +
                '               <input type="checkbox" class="day-distri-check" value="4"><span class="day-distri-span">04:00-05:00</span>' +
                '           </td> ' +
                '           <td style="padding-left: 15px;">' +
                '               <input type="checkbox" class="day-distri-check" value="5"><span class="day-distri-span">05:00-06:00</span>' +
                '           </td> ' +
                '           <td style="padding-left: 15px;">' +
                '               <input type="checkbox" class="day-distri-check" value="6"><span class="day-distri-span">06:00-07:00</span>' +
                '           </td> ' +
                '           <td style="padding-left: 15px;">' +
                '               <input type="checkbox" class="day-distri-check" value="7"><span class="day-distri-span">07:00-08:00</span>' +
                '           </td> ' +
                '       </tr>' +
                '       <tr>' +
                '           <td style="padding-left: 20px;">' +
                '               <input type="checkbox" class="day-distri-check" value="8"><span class="day-distri-span">08:00-09:00</span>' +
                '           </td> ' +
                '           <td style="padding-left: 15px;">' +
                '               <input type="checkbox" class="day-distri-check" value="9"><span class="day-distri-span">09:00-10:00</span>' +
                '           </td> ' +
                '           <td style="padding-left: 15px;">' +
                '               <input type="checkbox" class="day-distri-check" value="10"><span class="day-distri-span">10:00-11:00</span>' +
                '           </td> ' +
                '           <td style="padding-left: 15px;">' +
                '               <input type="checkbox" class="day-distri-check" value="11"><span class="day-distri-span">11:00-12:00</span>' +
                '           </td> ' +
                '       </tr>' +
                '       <tr>' +
                '           <td style="padding-left: 20px;">' +
                '               <input type="checkbox" class="day-distri-check" value="12"><span class="day-distri-span">12:00-13:00</span>' +
                '           </td> ' +
                '           <td style="padding-left: 15px;">' +
                '               <input type="checkbox" class="day-distri-check" value="13"><span class="day-distri-span">13:00-14:00</span>' +
                '           </td> ' +
                '           <td style="padding-left: 15px;">' +
                '               <input type="checkbox" class="day-distri-check" value="14"><span class="day-distri-span">14:00-15:00</span>' +
                '           </td> ' +
                '           <td style="padding-left: 15px;">' +
                '               <input type="checkbox" class="day-distri-check" value="15"><span class="day-distri-span">15:00-16:00</span>' +
                '           </td> ' +
                '       </tr>' +
                '       <tr>' +
                '           <td style="padding-left: 20px;">' +
                '               <input type="checkbox" class="day-distri-check" value="16"><span class="day-distri-span">16:00-17:00</span>' +
                '           </td> ' +
                '           <td style="padding-left: 15px;">' +
                '               <input type="checkbox" class="day-distri-check" value="17"><span class="day-distri-span">17:00-18:00</span>' +
                '           </td> ' +
                '           <td style="padding-left: 15px;">' +
                '               <input type="checkbox" class="day-distri-check" value="18"><span class="day-distri-span">18:00-19:00</span>' +
                '           </td> ' +
                '           <td style="padding-left: 15px;">' +
                '               <input type="checkbox" class="day-distri-check" value="19"><span class="day-distri-span">19:00-20:00</span>' +
                '           </td> ' +
                '       </tr>' +
                '       <tr>' +
                '           <td style="padding-left: 20px;">' +
                '               <input type="checkbox" class="day-distri-check" value="20"><span class="day-distri-span">20:00-21:00</span>' +
                '           </td> ' +
                '           <td style="padding-left: 15px;">' +
                '               <input type="checkbox" class="day-distri-check" value="21"><span class="day-distri-span">21:00-22:00</span>' +
                '           </td> ' +
                '           <td style="padding-left: 15px;">' +
                '               <input type="checkbox" class="day-distri-check" value="22"><span class="day-distri-span">22:00-23:00</span>' +
                '           </td> ' +
                '           <td style="padding-left: 15px;">' +
                '               <input type="checkbox" class="day-distri-check" value="23"><span class="day-distri-span">23:00-24:00</span>' +
                '           </td> ' +
                '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '   <div style="padding-top: 20px;">' +
                '       <span class="tmbtn too-wide-yellow-btn simple-plan-btn ">一键均匀分布宝贝</span> ' +
                '   </div> ' +
                '</div> ' +
                '';

            return html;
        }

    }, SimplePlan.init);


})(jQuery,window));



