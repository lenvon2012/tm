var TM = TM || {};
((function ($, window) {
    
    TM.DefenseConfig = TM.DefenseConfig || {};

    var DefenseConfig = TM.DefenseConfig;

    /**
     * 初始化
     * @type {*}
     */
    DefenseConfig.init = DefenseConfig.init || {};
    DefenseConfig.init = $.extend({
        doInit: function(container) {
        	DefenseConfig.container = container;
        	DefenseConfig.init.initSwitchOp();
            DefenseConfig.Event.setEvent();
//        	container.find('input:radio').unbind('click').click(function(){
//        		var me = $(this);
//        		var status = me.attr('value');
//        		$.get('/SkinDefender/autoClose', {status:status}, function(data){
//        			if(data == "on"){
//        				alert("【已打开】自动关闭订单");
//        			}else if(data == "off"){
//        				alert("【已关闭】自动关闭订单");
//        			}
//        		});
//        	});
        },
	    initSwitchOp: function() {
	        $.ajax({
	            url : '/skindefender/isOn',
	            data : {},
	            type : 'post',
	            success : function(dataJson) {
	                if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
	                    return;
	                var isOn = dataJson.res;
	                var title = "自动关闭订单";
	                if (TM.ver < 20) {
	                	title = "差评师订单提醒";
	                	$("#titleinfo1").html("若要使用自动提醒疑似差评师订单功能，请确保为【打开】状态。 &lt;<a href='http://fuwu.taobao.com/ser/detail.htm?spm=a1z13.1113643.1113643.27.lKFX41&service_code=FW_GOODS-1850391&tracelog=defense&scm=&ppath=&labels=' target='_blank'>升级高级版</a>可自动关闭订单&gt;");
	                	$("#titleinfo2").text("本功能主要是自动提醒疑似差评师的订单，符合您下面所设置条件的订单，将短信通知卖家。");
	                }
	                var switchStatus = TM.Switch.createSwitch.createSwitchForm(title);
	                switchStatus.appendTo(DefenseConfig.container.find(".switch-defense-div"));
	                switchStatus.find('input[name="auto_valuation"]').tzCheckbox({
	                    labels:['已开启','已关闭'],
	                    doChange:function(isCurrentOn){
	                        if (isCurrentOn == false) {//要开启
	                            $.ajax({
	                                url : '/skindefender/turnOn',
	                                data : {},
	                                type : 'post',
	                                success : function(dataJson) {
	                                	if(dataJson == null || dataJson == undefined) {
	                                		alert('操作失败，请稍后再试');
	                                		return;
	                                	}
//	                                    if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//	                                        return;
	                                }
	                            });
	                        } else if (isCurrentOn == true) {//要关闭
	                            $.ajax({
	                                url : '/skindefender/turnOff',
	                                data : {},
	                                type : 'post',
	                                success : function(dataJson) {
	                                	if(dataJson == null || dataJson == undefined) {
	                                		alert('操作失败，请稍后再试');
	                                		return;
	                                	}
//	                                    if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//	                                        return;
	                                }
	                            });
	                        }
	                        return true;
	                    },
	                    isOn : isOn
	                });
	            }
	        });
	    },
        doInitExcludeArea: function(){
            var excludeAreas = DefenseConfig.container.find('.excludeAreas').val();
            if(excludeAreas === undefined || excludeAreas == null) {
                // 如果没有排除的地域，则清空
                DefenseConfig.container.find('.excludeAreaSelector input').attr("checked", false);
                return;
            }
            var excludeArr = excludeAreas.split(",");
            if(excludeArr === undefined || excludeArr == null || excludeArr.length == 0) {
                // 如果没有排除的地域，则清空
                DefenseConfig.container.find('.excludeAreaSelector input').attr("checked", false);
                return;
            }
            $(excludeArr).each(function(i, area){
                DefenseConfig.container.find('.excludeAreaSelector input[word="'+area+'"]').attr("checked", true);
            });
        }
    }, DefenseConfig.init);

    DefenseConfig.Event = DefenseConfig.Event || {};
    DefenseConfig.Event = $.extend({
        setEvent : function(){
            DefenseConfig.Event.setNoEditExcludeInputEvent();
            DefenseConfig.Event.setSelectExcludeAreaEvent();
            DefenseConfig.Event.setExcludeAreaEvent();
        },
        setNoEditExcludeInputEvent: function(){
            DefenseConfig.container.find('.toSelectExcludeAreas').unbind('click').click(function(){
                DefenseConfig.init.doInitExcludeArea();
                DefenseConfig.container.find('.excludeAreaSelector').show();
            });
        },
        setSelectExcludeAreaEvent: function(){
            DefenseConfig.container.find('.excludeAreas').unbind('keyup').keyup(function(){
                DefenseConfig.container.find('.excludeAreas').val(DefenseConfig.NowToExcludeArea);
            });
            DefenseConfig.container.find('.excludeAreas').unbind('keydown').keydown(function(){
                DefenseConfig.NowToExcludeArea = DefenseConfig.container.find('.excludeAreas').val();
            });
        },
        setExcludeAreaEvent: function(){
            DefenseConfig.container.find('.excludeAreaSelector .selectAllAreas').unbind('click')
                .click(function(){
                    DefenseConfig.container.find('.excludeAreaSelector input').attr("checked", true);
                });

            DefenseConfig.container.find('.excludeAreaSelector .selectNoArea').unbind('click')
                .click(function(){
                    DefenseConfig.container.find('.excludeAreaSelector input').attr("checked", false);
                });

            DefenseConfig.container.find('.excludeAreaSelector .noRemoteAreas').unbind('click')
                .click(function(){
                    DefenseConfig.container.find('.excludeAreaSelector .remoteArea').attr("checked", true);
                });

            DefenseConfig.container.find('.excludeAreaSelector .selectChild').unbind('click')
                .click(function(){
                    if($(this).find('input').attr("checked") == "checked") {
                        $(this).parent().find('.areaName input').attr("checked", true);
                    } else {
                        $(this).parent().find('.areaName input').attr("checked", false);
                    }

                });

            DefenseConfig.container.find('.excludeAreaSelector .areaSelectOK').unbind('click').click(function(){
                var excludeArea = [];
                DefenseConfig.container.find('.excludeAreaSelector .areaName input:checked').each(function(i ,exclude){
                    excludeArea.push($(exclude).attr("word"));
                });;
                DefenseConfig.container.find('.excludeAreas').val(excludeArea.join(","));
                DefenseConfig.container.find('.excludeAreaSelector').hide();

            });

            DefenseConfig.container.find('.excludeAreaSelector .areatext').unbind('click')
                .click(function(){
                    if($(this).parent().find('input').attr("checked") == "checked") {
                        $(this).parent().find('input').attr("checked", false);
                        $(this).parent().parent().fin-
                        d('.selectChild input').attr("checked", false);
                    } else {
                        $(this).parent().find('input').attr("checked", true);
                        if($(this).parent().parent().find('.areaName input:checked').length == $(this).parent().parent().find('.areaName').length) {
                            $(this).parent().parent().find('.selectChild input').attr("checked", true);
                        }
                    }

                });
            DefenseConfig.container.find('.excludeAreaSelector .areaName input').unbind('click')
                .click(function(){
                    if($(this).attr("checked") == "checked") {
                        if($(this).parent().parent().find('.areaName input:checked').length == $(this).parent().parent().find('.areaName').length) {
                            $(this).parent().parent().find('.selectChild input').attr("checked", true);
                        }
                    } else {
                        $(this).parent().parent().find('.selectChild input').attr("checked", false);
                    }

                });
            DefenseConfig.container.find('.excludeAreaSelector .exitAreaSelector').unbind('click')
                .click(function(){
                    DefenseConfig.container.find('.excludeAreaSelector').hide();
                });
        }
    }, DefenseConfig.Event);
})(jQuery,window));