


var TM = TM || {};

TM.ShowWindow = TM.ShowWindow || {};

((function ($, window) {
    var me = TM.ShowWindow;

    me.ErrorHandler = me.ErrorHandler || {};

//    me.tag = ;

    TM.ShowWindow.init = function(container){
        $("body").find(".qtip").hide();
        me.container = container;
//        me.container.empty();
        me.itemBase = $("#swItemBase");
        me.opArea = $("#windowSwitch");

        me.nav = $(".kitNav");
        me.listArea = $(".kitListArea");
        me.buttonArea=  $('#buttonArea');
        me.DoubleTwelveTip = $('#double-twelve-tip');
        me.advanceSettingArea = $('#window-advance-setting');
        me.buttonArea.find('.allReRecommend').click(function(){
            if(confirm('亲,您确定要重新执行橱窗推荐吗?')){
                $.post('/windows/immediateRecommend',function(data){
                    if(data === undefined || data == null){

                    } else if(data == "双十二活动商品暂时不能取消橱窗推荐！") {
                        TM.Alert.load("亲，您有宝贝因为参加双十二活动导致暂时不能取消该宝贝的橱窗推荐，亲检查一下哦~");
                    }
                    TM.ShowWindow.renderOnShowItem();
                    TM.ShowWindow.renderItemInfo();
                });
            }
        });

        me.buttonArea.find('.allCancelRecommend').click(function(){
            if(confirm('亲,您确定要取消所有在橱窗中的宝贝吗?')){
                $.post('/windows/allCancel',function(){
                    TM.ShowWindow.renderOnShowItem();
                    TM.ShowWindow.renderItemInfo();
                });
            }
        });

        me.DoubleTwelveTip.find('.double-twelve-manual-open').unbind('click').click(function(){
            var tip = "";
            if($('.is-double-twelve-window-open').text() == "关闭中"){
                tip = "开启";
            } else {
                tip = "关闭";
            }
            if(confirm("亲，您确定已经手动" + tip + "了双12自动橱窗功能？")){
                $.get('/Windows/setDoubleTwelveWindowOpen', function(data){
                    TM.Alert.load(data.message, 400, 300, function(){
                        location.reload();
                    });
                });
            }
        });
//        me.opArea.appendTo(me.container);
//        me.itemBase.appendTo(me.container);
//        me.buttonArea.appendTo(me.container);
//        me.nav.appendTo(me.container);
//        me.listArea.appendTo(me.container);
        me.container.show();

        if(!TM.isVip()){
//        if(true){
            TM.widget.buildKitGuidePay('show').insertBefore(me.opArea);
        }

        me.saleConfig = me.advanceSettingArea.find('.saleConfig');
        me.stockConfig = me.advanceSettingArea.find('.stockNumConfig');
        me.manualWindowNumConfig = me.advanceSettingArea.find('.manualWindowSizeConfig');
        me.bindConfigLine(me.saleConfig);
        me.bindConfigLine(me.stockConfig);
        me.bindConfigLine(me.manualWindowNumConfig);

        me.renderPriorNum();
        me.renderItemInfo();
        me.renderOpArea();
        //me.renderIsDoubleTwelveOpen();
        me.bindTabListeners();
    }


    TM.ShowWindow.bindConfigLine = function(config){
        config.find('.modify-new-input').click(function(){
//            me.saleConfig.find('#how-many-salesCount').hide()
            config.find('.newNumWrapper').show();
            config.find('.modify-new-input').hide();
            config.find('.submit-how-many-salesCount').fadeIn(1000);
            config.find('.cancel-how-many-salesCount').fadeIn(1000);
//            me.advanceSettingArea.find('.newPriorSale').show();

        });

        config.find('.cancel-how-many-salesCount').click(function(){
//            me.saleConfig.find('#how-many-salesCount').show();
            config.find('.newNumWrapper').hide();
            config.find('.modify-new-input').fadeIn(1000);
            config.find('.submit-how-many-salesCount').hide();
            config.find('.cancel-how-many-salesCount').hide();
        });

        config.find('.submit-how-many-salesCount').click(function(){
            var newValue = config.find('.new-num').val();
            if(newValue < 0){
                TM.Alert.load("销量数得是大于0的整数哦");
                return;
            }

            TM.ShowWindow.reSubmitConfig(false);
            config.find('.old-num').val(newValue);
            config.find('.cancel-how-many-salesCount').trigger('click');

        });
        config.find('input[type="checkbox"]').click(function(){
            var checked = !($(this).attr('checked'));
            me.reSubmitConfig();
        });
//        config.find('.statustext').click(function(){
//            config.find('input[type="checkbox"]').trigger('click');
//        })

    }
    TM.ShowWindow.reSubmitConfig = function(slient){
        if(!TM.isAutoShow){
            alert('亲,请先开启自动橱窗的开关后再设置选项哟');
            return;
        }
        var enableSaleNum =  me.saleConfig.find('input.enable-advance-window-setting:checked').length > 0 ? true : false;
        var enableStockNum = me.stockConfig.find('input.enable-advance-window-setting:checked').length > 0 ? true : false;
        var newSaleConfigNum = me.saleConfig.find('.new-num').val();
        var newStockConfigNum = me.stockConfig.find('.new-num').val();

        if(newSaleConfigNum >= 0 && newStockConfigNum >=0 ){
            // Nothing, it's ok...
        }else{
            TM.Alert.load("亲,请输入整数哟");
            return;
        }

        var params = {
            'config.priorSaleNum':newSaleConfigNum,
            'config.enableSaleNum':enableSaleNum,
            'config.enableInstockNum':enableStockNum,
            'config.minInstockNum':newStockConfigNum
        };
        $.post('/windows/submitNewConfig',params,function(data){
            if(!data || !data.ok){
                TM.Alert.load('亲,设置失败,请稍后重试,或联系客服');
            }
            me.renderPriorNum();
        });

        var enableManualWindowNum =  me.manualWindowNumConfig.find('input.enable-advance-window-setting:checked').length > 0 ? true : false;
        var newManualWindowNum = me.manualWindowNumConfig.find('.new-num').val();
        $.post('/windows/submitManualWindowNum',{newManualWindowNum: newManualWindowNum, enableManualWindowNum: enableManualWindowNum},function(data){
            if(data === undefined || data == null){
                TM.Alert.load('亲,设置失败,请稍后重试,或联系客服');
                return;
            }
            if(data.success == false) {
                TM.Alert.load('亲,设置失败,请稍后重试,或联系客服');
                return;
            }
            me.renderPriorNum();
        });
    }

    TM.ShowWindow.renderIsDoubleTwelveOpen = function(){
        $.get('/Windows/isDoubleTwelveOpen', function(data){
            if(data.success){
                $('.is-double-twelve-window-open').text("已开启");
                $('.to-open').text("我要关闭")
            } else {
                $('.is-double-twelve-window-open').text("关闭中");
                $('.to-open').text("我要开启");
            }
        });
    }

    TM.ShowWindow.renderPriorNum = function(){

//        $('.enable-advance-window-setting').click(function(){
//            TM.ShowWindow.windowConfigAjax();
//        });

//
//        me.saleConfig.find('.modify-how-many-salesCount').click(function(){
////            me.saleConfig.find('#how-many-salesCount').hide()
//            me.saleConfig.find('.new-num').show();
//            me.saleConfig.find('.modify-how-many-salesCount').hide();
//            me.saleConfig.find('.submit-how-many-salesCount').fadeIn(1000);
//            me.saleConfig.find('.cancel-how-many-salesCount').fadeIn(1000);
////            me.advanceSettingArea.find('.newPriorSale').show();
//
//        });
//
//        me.saleConfig.find('.cancel-how-many-salesCount').click(function(){
////            me.saleConfig.find('#how-many-salesCount').show();
//            me.saleConfig.find('.new-num').hide();
//            me.saleConfig.find('.modify-how-many-salesCount').fadeIn(1000);
//            me.saleConfig.find('.submit-how-many-salesCount').hide();
//            me.saleConfig.find('.cancel-how-many-salesCount').hide();
//        });
//
//        me.saleConfig.find('.submit-how-many-salesCount').click(function(){
//            var newValue = me.saleConfig.find('#new-how-many-salesCount').val();
//            if(newValue < 0){
//                TM.Alert.load("销量数得是大于0的整数哦");
//            } else {
//                me.saleConfig.find('#how-many-salesCount').val(newValue);
//                TM.ShowWindow.windowConfigAjax();
//            }
//        });



        $.get('/windows/getConfig',function(data){
            if(!data || data == "获取优先推荐销量数出错，请联系客服") {
                alert(data);
                return;
            }

            if(data.enableSaleNum && TM.isAutoShow){
                me.saleConfig.find('input.enable-advance-window-setting').attr('checked',true);
                var text = me.saleConfig.find('.statustext');
                text.addClass('statusopentext');
                text.removeClass('statusclosetext');
                text.html('[已开启]');
            }else{
                me.saleConfig.find('input.enable-advance-window-setting').attr('checked',false);
                var text = me.saleConfig.find('.statustext');
                text.addClass('statusclosetext');
                text.removeClass('statusopentext');
                text.html('[已关闭]');
            }
            me.saleConfig.find('input[type="text"]').val(data.priorSaleNum);

            if(data.enableInstockNum && TM.isAutoShow){
                me.stockConfig.find('input.enable-advance-window-setting').attr('checked',true);
                me.stockConfig.find('.statustext').addClass('statusopentext').removeClass('statusclosetext').html('[已开启]');
            }else{
                me.stockConfig.find('input.enable-advance-window-setting').attr('checked',false);
                me.stockConfig.find('.statustext').addClass('statusclosetext').removeClass('statusopentext').html('[已关闭]');
            }

            me.stockConfig.find('input[type="text"]').val(data.minInstockNum);
        });

        $.get("/windows/getManualWindowNum", function(data){
            if(data === undefined || data == null) {
                me.manualWindowNumConfig.find('input.enable-advance-window-setting').attr("checked", false);
                me.manualWindowNumConfig.find('.statustext').addClass('statusopentext').removeClass('statusclosetext').html('[已关闭]');
                me.manualWindowNumConfig.find('input[type="text"]').val(-1);
                return;
            }
            if(data.success == false) {
                me.manualWindowNumConfig.find('input.enable-advance-window-setting').attr("checked", false);
                me.manualWindowNumConfig.find('.statustext').addClass('statusopentext').removeClass('statusclosetext').html('[已关闭]');
                me.manualWindowNumConfig.find('input[type="text"]').val(-1);
                return;
            }
            me.manualWindowNumConfig.find('input[type="text"]').val(data.fixedNum);
            if(data.enableManualWindowNum && TM.isAutoShow){
                me.manualWindowNumConfig.find('input.enable-advance-window-setting').attr('checked',true);
                me.manualWindowNumConfig.find('.statustext').addClass('statusopentext').removeClass('statusclosetext').html('[已开启]');
            }else{
                me.manualWindowNumConfig.find('input.enable-advance-window-setting').attr('checked',false);
                me.manualWindowNumConfig.find('.statustext').addClass('statusclosetext').removeClass('statusopentext').html('[已关闭]');
            }
        });

    }

    TM.ShowWindow.windowConfigAjax = function(){
        var enableSaleNum =  ($('input.enable-advance-window-setting:checked').length == 1) ? true : false;
        var priorNum = me.advanceSettingArea.find('.new-num').val();
        if(priorNum < 0){
            TM.Alert.load("数目不能小于0哦亲");
        } else {
            $.post('/Windows/setPriorNum',{priorNum:priorNum,enableSaleNum:enableSaleNum},function(data){
                if(data === undefined || data == null){
                } else {
                    //TM.Alert.load(data);
                }
            });
        }
    }

    TM.ShowWindow.renderOpArea = function(){
        $.get('/windows/isOn',function(data){
            var isOn = data.res;
            var line = $("<div class='switchStatusLine' ></div>");
            var switchLine = TM.Switch.createSwitch.createSwitchForm("自动橱窗开启状态");
            //switchLine.find("ul").append($("<input type='checkbox' name='salesFirst' class='salesFirst'>优先推荐销量前15的宝贝</input>"));
            switchLine.find('input[name="auto_valuation"]').tzCheckbox(
                {
                    labels:['已开启','已关闭'],
                    doChange:function(isCurrentOn){
                        if(!TM.isVip()){
                            TM.Alert.showVIPNeeded();
                            return;
                        }

                        if(isCurrentOn == false) {
                            var isSalesCountFirst = switchLine.find(".salesFirst").attr("checked")=="checked";
                            $.post('/Windows/turn',{"isOn":true,"isSalesCountFirst":isSalesCountFirst},function(data){
                                if(!data)  {TM.Alert.load("自动橱窗开启失败，请联系淘标题客服人员~");return;}
                                TM.isAutoShow=true;
                                TM.ShowWindow.renderPriorNum();
                            });
                        }
                        else {
                            var isSalesCountFirst = switchLine.find(".salesFirst").attr("checked")=="checked";
                            $.post('/Windows/turn',{"isOn":false,"isSalesCountFirst":isSalesCountFirst},function(data){
                                if(!data)  {TM.Alert.load("自动橱窗开启失败，请联系淘标题客服人员~");return;}
                                TM.isAutoShow=false;
                                TM.ShowWindow.renderPriorNum();
                            });
                        }
                        return true;
                    },
                    isOn : isOn
                });
            var btn = $('<span class="tmbtn long-green-btn refresh-window-status" style="margin-left:180px;font-size: 17px;">刷新橱窗状态</span>');
            btn.click(function(){
                TM.ShowWindow.renderItemInfo();
            })
            switchLine.find('li').append(btn);
            var btn2 = $('<span class="tmbtn long-green-btn refresh-window-status" style="margin-left:20px;font-size: 17px;">取消仓库宝贝推荐</span>');
            btn2.click(function(){
                TM.ShowWindow.batchCancelItemOnShow();
            })
            switchLine.find('li').append(btn2);

            me.opArea.empty();
            switchLine.appendTo(line);//warmTips.appendTo(line);
            line.appendTo(me.opArea);
        })

    }

    TM.ShowWindow.renderItemInfo = function(){
        $.get('/windows/base',function(data){
            me.itemBase.empty();

//            var html = [];
//            html.push('<div class="showWindowStatus"><span>橱窗总数:');
//            html.push('<b>'+data.totalWindowCount+'</b>')
//            html.push('</span><span>剩余橱窗:');
//            html.push('<b>'+data.remainWindowCount+'</b>')
//            html.push('</span><span>已利用橱窗数:');
//            html.push('<b>'+data.onShowItemCount+'</b>')
//            html.push('</span></div>');
//            html.push('<div>');
//            html.push('<span>在售商品总数:');
//            html.push('<b>'+data.onSaleCount+'</b>');
//            html.push('</span>');
//            html.push('<span>库存商品总数:');
//            html.push('<b>'+data.inventoryCount+'</b>');
//            html.push('</span>');
//            html.push('</div>');
//            html.push('<div id="showwindowbuttons"><label class="commbutton btntext6 refreshWindowStatus">刷新橱窗状态</label></div>')
//            var info = $(html.join(''));

            var info = $('#windowStatusTmpl').tmpl([data]);
//            info.find('.refreshWindowStatus').click(function(){
////                TM.ShowWindow.reqWatiToReRecommend();
//                TM.ShowWindow.renderItemInfo();
//            });
            info.appendTo(me.itemBase);
        });
    }

    TM.ShowWindow.batchCancelItemOnShow = function () {
        $.get('/windows/batchDropInstockOnShow',function(data){
            if (data) {
                TM.Alert.load(data.msg);
            }
        });
    }


    me.ErrorHandler.validRemoveRes = function(res){
        if(!res || !res.isOk){
            TM.Alert.load('亲,删除失败！');
        }else{
            TM.Alert.load('亲,删除成功！');
        }
    }

    TM.ShowWindow.renderMustShowItem = function(){
//        commsTab.rule.init('/windows/listMustItems','/windows/removeMustItem');
//        commsTab.SetEvent.setRemoveCommEvent(me.ErrorHandler.validRemoveRes);
//        commsTab.createItemsByURL();
		me.listArea.empty();
        var table = $('<div></div>');
        var bottom = $('<div style="text-align: center;"></div>');
        var addMust = $("<div class='tmbtn sky-blue-btn addMustButton' tag='show' style='margin:6px 0 6px 10px;'>添加必推 </div><div class='tmbtn yellow-btn deleteAllMusts' tag='deleteAll' style='margin:6px 0 6px 10px;'>全部取消 </div>");
        addMust.unbind();
        bottom.tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount:1,
            ajax: {
                on: true,
                dataType: 'json',
                url: "/windows/listMustItems",
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
                        html.push('<tr class="'+even+'">');
                        html.push('<td><a target="_blank" href="http://item.taobao.com/item.html?id='+item.id+'"><img class="itemsnap" src="'+item.picURL+'"</a></td>');
                        html.push('<td>'+item.name+'</td>');
                        html.push('<td>'+new Date(item.deListTime).formatYMDMS()+'</td>');
                        html.push('<td><span class="dropOnShow tmbtn sky-blue-btn" href="/windows/removeMustItem?numIid='+item.id+'" >取消必推</span></td>');
                        html.push('</tr>');
                    });

                    html.push('</tbody>');
                    html.push('</table>');
                    var dom = $(html.join(''));
                    dom.find('.dropOnShow').click(function(){
                        var btn = $(this);
                        if(confirm('亲,确认取消该宝贝的橱窗推荐?')){
                            $.ajax({
                                url : btn.attr('href'),
                                success : function(){
                                    me.renderMustShowItem();
                                    me.renderItemInfo();
                                    TM.Alert.load('取消必推成功');
                                },
                                error : function(){
                                    TM.Alert.load('取消必推失败');
                                }
                            });
                        }
                    });

                    dom.appendTo(table);
                    addMust.appendTo(me.listArea);
                    table.appendTo(me.listArea);
                    bottom.appendTo(me.listArea);

                    $('.addMustButton').click(function(){
                        multiOpByURL.createChoose.createOrRefleshCommsDiv({
                            itemsURL:"/Windows/chooseItems",
                            actionURL:"/Windows/addMustItems",
                            pn:1,
                            ps:8,
                            enableSearch:true,
                            callbackFun:function(){
                                if($('.kitListArea').find('tbody tr').size() == 0){
                                    alert('新添加的必推宝贝会在5分钟内被橱窗推荐哟');
                                }
                                TM.ShowWindow.reqWatiToReRecommend();
                            }
                        });
                    });
                    $('.addMustButton').qtip({
                        content: {
                            text: "点击'添加必推'按钮可选择必推宝贝哦亲~"
                        },
                        position: {
                            at: "bottom right ",
                            corner: {
                                target: 'bottomRight'
                            }
                        },
                        show: {
                            when: false,
                            ready:true
                        },
                        hide:false,
                        style: {
                            name:'cream',
                            width:'30px'
                        }
                    });
                    $(window).trigger('scroll');
                    $('.deleteAllMusts').click(function(){
                        if(confirm("确定要删除必推列表中的所有宝贝？")) {
                            $.get('/Windows/removeAllMustItem',function(data){
                                if(!data.res)  {
                                    TM.Alert.load("亲，删除失败，请重试或联系淘标题客服~");
                                }
                                else {
                                    me.renderMustShowItem();
                                    TM.Alert.load("亲，删除成功~");
                                };
                            });
                        }

                    });
                }
            }

        });

    }


    TM.ShowWindow.renderExcludeShowItem = function(){
//        commsTab.rule.init('/windows/listMustItems','/windows/removeMustItem');
//        commsTab.SetEvent.setRemoveCommEvent(me.ErrorHandler.validRemoveRes);
//        commsTab.createItemsByURL();
        me.listArea.empty();
        var table = $('<div></div>');
        var bottom = $('<div style="text-align: center;"></div>');
        var addExclude = $("<p style='margin-top: 5px;'><span class='tmbtn sky-blue-btn addExclude' tag='show' style='margin-left: 10px;'>添加不推 </span>" +
            "<span class='tmbtn yellow-btn deleteAllExcludes' tag='show' style='margin-left: 10px;'>全部取消 </span>" +
            "<span class=' addBaoyou tmbtn long-flat-green-btn' tag='show' style='margin-left:10px;font-size:12px;font-weight: bold;'>一键不推包邮和赠品</span>" +
            "</p>");
        addExclude.unbind();
        bottom.tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount:1,
            ajax: {
                on: true,
                dataType: 'json',
                url: "/windows/listExcludeItems",
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
                        html.push('<tr class="'+even+'">');
                        html.push('<td><a target="_blank" href="http://item.taobao.com/item.html?id='+item.id+'"><img class="itemsnap" src="'+item.picURL+'" /></a></td>');
                        html.push('<td>'+item.name+'</td>');
                        html.push('<td>'+new Date(item.deListTime).formatYMDMS()+'</td>');
                        html.push('<td><span class="dropOnShow tmbtn sky-blue-btn" href="/windows/removeExcludeItem?numIid='+item.id+'" >取消不推</span></td>');
                        html.push('</tr>');
                    });
                    html.push('</tbody>');
                    html.push('</table>');
                    var dom = $(html.join(''));
                    dom.find('.dropOnShow').click(function(){
                        var btn = $(this);
                        if(confirm('亲,确认取消该宝贝的橱窗推荐?')){
                            $.ajax({
                                url : btn.attr('href'),
                                success : function(){
                                    me.renderExcludeShowItem();
                                    TM.Alert.load('取消必不推成功');
                                },
                                error : function(){
                                    TM.Alert.load('取消必不推失败');
                                }
                            });
                        }
                    });

                    dom.appendTo(table);
                    addExclude.appendTo(me.listArea);
                    table.appendTo(me.listArea);
                    bottom.appendTo(me.listArea);

                    $(".addExclude").click(function(){
                        multiOpByURL.createChoose.createOrRefleshCommsDiv({
                            "itemsURL":"/Windows/chooseItems",
                            "actionURL":"/Windows/addExcludeItems",
                            "pn":1,"ps":8,"enableSearch":true,
                            callbackFun:function(){
                                if($('.kitListArea').find('tbody tr').size() == 0){
                                    alert('最新排除橱窗的宝贝会在5分钟内生效哟');
                                }
                                TM.ShowWindow.reqWatiToReRecommend();
                            }
                        });
                    });
                    $('.addExclude').qtip({
                        content: {
                            text: "点击'添加不推'按钮可选择必不推宝贝哦亲~"
                        },
                        position: {
                            at: "bottom right ",
                            corner: {
                                target: 'bottomRight'
                            }
                        },
                        show: {
                            when: false,
                            ready:true
                        },
                        hide:false,
                        style: {
                            name:'cream',
                            width:'30px'
                        }
                    });
                    $(window).trigger('scroll');
                    $(".deleteAllExcludes").click(function(){
                        if(confirm("确定要删除必不推列表中的所有宝贝？")) {
                            $.get('/Windows/removeAllExcludeItem',function(data){
                                if(!data.res)  {
                                    TM.Alert.load("亲，删除失败，请重试或联系淘标题客服~");
                                }
                                else {
                                    me.renderExcludeShowItem();
                                    TM.Alert.load("亲，删除成功~")
                                };
                            });
                        }
                    });
                    $(".addBaoyou").click(function(){
                       // multiOpByURL.createChoose.createOrRefleshCommsDiv({"itemsURL":"/Windows/chooseBaoyouItems","actionURL":"/Windows/addExcludeItems","pn":1,"px":8,"enableSearch":true});
                        $.get('/Windows/addBaoyou',function(data){
                            TM.Alert.load("亲，包邮和赠品类目商品已添加不推",400,300,function(){
//                                location.reload()
                                TM.ShowWindow.renderExcludeShowItem();
                            });
                        })
                    });
                }
            }

        });

    }

    TM.ShowWindow.reqWatiToReRecommend = function(){
        $.post('/windows/toRecommend');
    }

    TM.ShowWindow.renderOnShowItem = function(){

        $.get('/windows/listOnItems',function(data){
            me.listArea.empty();
            if(!data){
                return;
            }

            var html = [];
            html.push('<table class="oplogs">');
            html.push('<thead><tr><th style="width:100px;">宝贝</th><th style="width:25%;">标题</th><th>销量</th><th style="width:214px;">下架时间</th><th>推荐理由</th><th style="width:100px;">操作</th></tr></thead>');
            html.push('<tbody>');

            $.each(data,function(i, item){
                var even = "";
                if(i%2 == 0){
                    even = "even";
                }
                html.push('<tr class="'+even+'">');
                html.push('<td><a target="_blank" href="http://item.taobao.com/item.html?id='+item.numIid+'"><img class="itemsnap" src="'+item.picURL+'"</a></td>');
                html.push('<td>'+item.title+'</td>');
                if(item.salesCount == 0){
                    html.push('<td>'+item.salesCount+'</td>');
                    html.push('<td><span style="color: red;">'+new Date(item.deListTime).formatYMDMS()+'</span></td>');
                } else {
                    html.push('<td><span style="color: red;">'+item.salesCount+'</span></td>');
                    html.push('<td>'+new Date(item.deListTime).formatYMDMS()+'</td>');
                }
                html.push('<td>'+item.onShowWindowReason+'</td>');
                html.push('<td><span class="dropOnShow tmbtn sky-blue-btn" href="/windows/dropOnShow?numIid='+item.numIid+'" >取消推荐</span>' +
                    '<span style="margin-top: 5px;" class="neverRecommend tmbtn yellow-btn" href="/windows/addExcludeItemFromShow?numIid='+item.numIid+'">永不推荐</span></td>');
                html.push('</tr>');
            });
            html.push('</tbody>');
            html.push('</table>');
            var dom = $(html.join(''));
            dom.find('.dropOnShow').click(function(){
                var btn = $(this);
                if(confirm('亲,确认取消该宝贝的橱窗推荐?')){
                    $.ajax({
                        url : btn.attr('href'),
                        success : function(){
                            me.renderOnShowItem();
                            TM.Alert.load('取消推荐成功');
                        },
                        error : function(){
                            TM.Alert.load('取消推荐失败');
                        }
                    });
                }
            });
            dom.find('.neverRecommend').click(function(){
                var btn = $(this);
                if(confirm('亲,确认永不推荐该宝贝到橱窗')){
                    $.ajax({
                        url : btn.attr('href'),
                        success : function(){
                            me.renderOnShowItem();
                            TM.Alert.load('取消推荐成功');
                        },
                        error : function(){
                            TM.Alert.load('取消推荐失败');
                        }
                    });
                }
            });
            dom.appendTo(me.listArea);
        });
    }
    TM.ShowWindow.renderOpLogs = function(){
        me.listArea.empty();
        var table = $('<div></div>');
        var bottom = $('<div style="text-align: center;"></div>');

        bottom.tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount:1,
            ajax: {
                on: true,
                dataType: 'json',
                url: "/windows/listOpLogs",
                callback:function(data){
                    table.empty();
                    bottom.empty();
                    if(!data){
                        return;
                    }
                    var html = [];
                    html.push('<table class="oplogs">');
                    html.push('<thead><tr><th style="width:268px">操作宝贝</th><th>推荐结果</th><th>更新时间</th></tr></thead>');
                    html.push('<tbody>');
                    $.each(data.res ,function(i, olog){
                        var even = "";
                        if(i%2 == 0){
                            even = "even";
                        }
                        html.push('<tr style="" class="'+even+'">');
                        html.push('<td><a target="_blank" href="http://item.taobao.com/item.htm?id='+olog.numIid+'">'+olog.title+'</a></td>');
                        html.push('<td>'+olog.content+'</td>');
                        html.push('<td>'+new Date(olog.ts).formatYMDMS()+'</td>');
                        html.push('</tr>');
                    });
                    html.push('</tbody>');
                    html.push('</table>');

                    $(html.join('')).appendTo(table);
                    table.appendTo(me.listArea);
                    bottom.appendTo(me.listArea);
                }
            }
        });
    }




TM.ShowWindow.bindTabListeners = function(){
        me.nav.find('a').click(function(){
            var anchor = $(this);
            me.nav.find('.selected').removeClass('selected');
            anchor.parent().addClass('selected');
            me.listArea.empty();
            switch(anchor.attr('tag')){
                case 'must':
                    $('.qtip-cream').remove();
                    me.renderMustShowItem();
                    break;
                case 'exclude':
                    $('.qtip-cream').remove();
                    me.renderExcludeShowItem();
                    break;
                case 'onitems':
                    $('.qtip-cream').remove();
                    me.renderOnShowItem();
                    break;
                case 'oplogs':
                    $('.qtip-cream').remove();
                    me.renderOpLogs();
                    break;
            }
        });

        me.nav.find('a:eq(0)').trigger('click');
    }

})(jQuery,window))