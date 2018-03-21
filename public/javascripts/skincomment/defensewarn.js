var TM = TM || {};
((function ($, window) {
    TM.DefenseWarn = TM.DefenseWarn || {};

    var DefenseWarn = TM.DefenseWarn;

    /**
     * 初始化
     * @type {*}
     */
    DefenseWarn.init = DefenseWarn.init || {};
    DefenseWarn.init = $.extend({
        doInit: function(container) {
            var html = DefenseWarn.init.createHtml();
            container.html(html);
            DefenseWarn.container = container;

            DefenseWarn.init.bindSmsConfig();

            container.find(".add-warn-btn").click(function() {
                DefenseWarn.submit.doAddWarn();
            });
            DefenseWarn.init.initSwitchOp();

            DefenseWarn.show.doShow();
        },
        bindSmsConfig: function() {
            var config = '<div style="margin:10px 10px;line-height: 30px;"><table><tr style="display: none;"><td style="vertical-align: top;text-align: right;"><div style="display: inline-block;vertical-align: top;font-weight:bold;">自动评价方式：</div></td><td>'
                +'<div style="display: inline-block;"><div><input type="radio" name="commentType" id="commentType-0" value="0" style="width:20px;"><label for="commentType-0" style="display:inline-block;*display:inline;position: relative;*zoom:1;">买家确认收货后立即评价（推荐）</label></div>'
                +'<div><input type="radio" name="commentType" id="commentType-1" value="1" style="width:20px;"><label for="commentType-1" style="display:inline-block;*display:inline;position: relative;*zoom:1;">买家好评后立即评价；到期前</label><input type="text" name="commentTime" class="commentTime-1" style="width:40px;margin:0 5px;text-align: center;" /><label for="commentType-1" style="display:inline-block;*display:inline;position: relative;*zoom:1;">天未评价，进行抢评</label></div>'
                +'<div><input type="radio" name="commentType" id="commentType-2" value="2" style="width:20px;"><label for="commentType-2" style="display:inline-block;*display:inline;position: relative;*zoom:1;">买家评价后不立即评价；到期前</label><input type="text" name="commentTime" class="commentTime-2" style="width:40px;margin:0 5px;text-align: center;" /><label for="commentType-2" style="display:inline-block;*display:inline;position: relative;*zoom:1;">天未评价，进行抢评</label></div></td></tr>'
                +'<tr><td style="font-weight:bold;text-align:right;">差评师拦截通知：</td><td><div style="display:inline-block;*display:inline;position: relative;*zoom:1;"><input type="checkbox" class="defense-notice" id="defense-notice" style="margin-left: 0px;width:20px;"><label for="defense-notice" style="display:inline-block;*display:inline;position:relative;*zoom:1;">发现疑似差评师拍下店铺宝贝时，拦截提醒</label></div></td></tr>'
                +'<tr><td style="font-weight:bold;text-align:right;">中差评通知：</td><td><div style="display:inline-block;*display:inline;position: relative;*zoom:1;"><input type="checkbox" class="badcomment-notice" id="badcomment-notice" style="margin-left: 0px;width:20px;"><label for="badcomment-notice" style="display:inline-block;*display:inline;position:relative;*zoom:1;">买家给中差评，立即短信通知我</label></div>'
                +'<div style="display:inline-block;*display:inline;position: relative;*zoom:1;margin-left:20px;"><span style="color:#3300FF;">（在下面设置通知号码）</span></div></td></tr>'
                +'<tr><td style="font-weight:bold;text-align:right;">中差评反击：</td><td><div style="display:inline-block;*display:inline;position: relative;*zoom:1;"><input type="checkbox" class="badcomment-reply" id="badcomment-reply" style="margin-left: 0px;width:20px;"><label for="badcomment-reply" style="display:inline-block;*display:inline;position:relative;*zoom:1;">买家给中差评，立即回评短信模板内容</label></div></td></tr>'
//                +'<tr><td style="font-weight:bold;text-align:right;">中差评修改：</td><td><div class="checkbox"><input type="checkbox" class="badcomment-check" id="badcomment-check" style="float: center;margin-left: 0px;width:20px;"><label for="badcomment-check"  style="display:inline-block;*display:inline;position:relative;*zoom:1;">买家给中差评，立即给买家发送短信，短信模板如下</label></div></td></tr>'
                +'<tr><td style="vertical-align: top;font-weight:bold;text-align:right;">短信模板：<br><span style="font-weight:normal;color:#669933;">(给中差评买家发送)</span></td><td><div style="display:inline-block;*display:inline;position:relative;*zoom:1;vertical-align: top;"><textarea class="badcomment-msg" style="width:350px;margin-left:3px;" rows="4"></textarea></div>'
                +'<div style="display:inline-block;*display:inline;position:relative;*zoom:1;margin-left:10px;line-height: 22px;"><span style="color:#CC0000;">#买家#</span> 自动替换成买家昵称<br><span style="color:#CC0000;">#卖家#</span> 自动替换成卖家昵称<br><span style="color:#CC0000;">#评价#</span> 自动替换成买家给出评价</div></td></tr>'
                +'<tr><td></td><td><a class="btn btn-primary save-comment-btn" style="margin:10px 0 0 10px;" href="javascript:void(0);" >保存设置</a></td></tr></div>';

            DefenseWarn.container.find(".sms-config").html(config);
            DefenseWarn.init.bindCommentConfBtn();
        },
        bindCommentConfBtn: function(){
            var badcommentCheck = DefenseWarn.container.find(".badcomment-check");
            var badcommentReply = DefenseWarn.container.find(".badcomment-reply");
            var badcommentMsg = DefenseWarn.container.find(".badcomment-msg");
            var badcommentNotice = DefenseWarn.container.find(".badcomment-notice");
            var defenseNotice = DefenseWarn.container.find(".defense-notice");

            
            // 预读配置
            $.get('/skincomment/commentConf', function(data){
                if(data && data != undefined) {
                    var commentType = data.commentType;
                    DefenseWarn.container.find("input[name='commentType'][value="+commentType+"]").attr("checked", true);

                    if(commentType > 0) {
                        DefenseWarn.container.find(".commentTime-"+commentType).val(data.commentDays);
                    }
                    if(data.badCommentNotice == true) {
                        badcommentNotice.attr("checked", "checked");
                    }
                    if(data.badCommentBuyerSms == true) {
                        badcommentCheck.attr("checked", "checked");
                    }
                    if(data.badCommentMsg != null) {
                        badcommentMsg.val(data.badCommentMsg);
                    }
                    if(data.commentRate == 1) {
                        badcommentReply.attr("checked", "checked");
                    }
                    if(data.defenseNotice == true) {
                        defenseNotice.attr("checked", "checked");
                    }

                    if(data.badCommentBuyerSms == false && data.commentRate != 1) {
                        badcommentMsg.attr("disabled", "disabled");
                    }
                } else {
                    DefenseWarn.container.find("#commentType-0").attr("checked", true);
                }

            });

            
            badcommentCheck.click(function(){
                if(badcommentCheck.attr("checked")=="checked" || badcommentReply.attr("checked")=="checked"){
                    badcommentMsg.removeAttr("disabled");
                }else{
                    badcommentMsg.attr("disabled", "disabled");
                }
            });
            DefenseWarn.container.find(".badcomment-reply").click(function(){
                if(badcommentCheck.attr("checked")=="checked" || badcommentReply.attr("checked")=="checked"){
                    badcommentMsg.removeAttr("disabled");
                }else{
                    badcommentMsg.attr("disabled", "disabled");
                }
            });

            DefenseWarn.container.find(".commentTime-1").focus(function(){
                DefenseWarn.container.find("#commentType-1").attr("checked", true)
            });
            DefenseWarn.container.find(".commentTime-2").focus(function(){
                DefenseWarn.container.find("#commentType-2").attr("checked", true)
            });

            DefenseWarn.container.find(".save-comment-btn").click(function(){
                var commentType = DefenseWarn.container.find("input[name='commentType']:checked").val();
                var commentTime = 0;
                if(commentType > 0) {
                    commentTime = DefenseWarn.container.find(".commentTime-"+commentType).val();
                }

                var commentNotice = badcommentNotice.attr("checked") == "checked" ? true : false;
                var commentReply = badcommentReply.attr("checked") == "checked" ? true : false;
                var commentCheck = badcommentCheck.attr("checked") == "checked" ? true : false;
                var defenseNoticeVal = defenseNotice.attr("checked") == "checked" ? true : false;

                var badCommentMsg = badcommentMsg.val();

                $.post('/skincomment/updateConf',
                    {commentType:commentType, commentTime:commentTime, badCommentNotice:commentNotice, badCommentReply:commentReply, badCommentBuyerSms:commentCheck, badCommentMsg:badCommentMsg, defenseNotice:defenseNoticeVal},
                    function(data){
    //    			TM.Alert.load(data.msg);
    //                window.location.reload();
                    TM.Alert.load('<p style="font-size:14px">'+data.msg+'</p>',400,230,function(){
                        if(data.isOk){
                            window.location.reload();
                        }
                    });
                });
            });
        },
        createHtml: function() {
            var html = '' +
                '<div class="warn-switch-div"><div class="sms-config"></div></div> ' +
                '' +
                '<div class="warn-table-div">' +
                '   <table>' +
                '       <tbody>' +
                '       <tr>' +
                '           <td><span class="add-warn-btn btn btn-info" style="margin-left:15px;">添加手机</span></td>' +
                '           <td style="padding-left: 15px;"><span style="color: #a10000;">注：当短信通知开启后，我们就会给以下的手机号码发送短信通知。<br>通知内容包括：中差评提醒，差评师拍下宝贝提醒，黑名单上的买家拍下宝贝提醒等</span></td>' +
                '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '   <table class="skincomment-table defense-warn-table" style="margin-top: 0px;">' +
                '       <thead>' +
                '       <tr>' +
                '           <td style="width: 50%;">手机号码</td>' +
                '           <td style="width: 20%;">备注</td>' +
                '           <td style="width: 15%;">修改</td>' +
                '           <td style="width: 15%;">删除</td>' +
                '       </tr>' +
                '       </thead>' +
                '       <tbody></tbody>' +
                '   </table>' +
                '</div> ' +
                '';

            return html;

        },
        initSwitchOp: function() {
            $.ajax({
                url : '/skincomment/isOn',
                data : {},
                type : 'post',
                success : function(dataJson) {
//                    if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//                        return;
                	if(dataJson == null || dataJson == undefined) {
                		alert('操作失败，请稍后再试');
                		return;
                	}
                    var isOn = dataJson.res;
                    var switchStatus = TM.Switch.createSwitch.createSwitchForm("短信通知开启状态");
//                    switchStatus.appendTo(DefenseWarn.container.find(".warn-switch-div"));
                    switchStatus.insertBefore(DefenseWarn.container.find(".sms-config"));
                    switchStatus.find('input[name="auto_valuation"]').tzCheckbox({
                        labels:['已开启','已关闭'],
                        doChange:function(isCurrentOn){
                            if (isCurrentOn == false) {//要开启
                            	var warnsJson = $.get("/skindefender/warnsCount", function(dataJson){
	                        		var smswarnsCount = dataJson.smswarnsCount;
		                        	if (smswarnsCount == 0) {
		                        		DefenseWarn.submit.doAddWarn();
		                        	}
	                        	});
                            	
                                $.ajax({
                                    url : '/skincomment/turnOn',
                                    data : {},
                                    type : 'post',
                                    success : function(dataJson) {
//                                        if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//                                            return;
                                    	if(dataJson == null || dataJson == undefined) {
	                                		alert('操作失败，请稍后再试');
	                                		return;
	                                	}
                                    }
                                });
                            } else if (isCurrentOn == true) {//要关闭
                                $.ajax({
                                    url : '/skincomment/turnOff',
                                    data : {},
                                    type : 'post',
                                    success : function(dataJson) {
//                                        if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//                                            return;
                                    	if(dataJson == null || dataJson == undefined) {
	                                		alert('操作失败，请稍后再试');
	                                		return;
	                                	}
                                    }
                                });
                            }
                            return true;
                        },
                        isOn : isOn
                    });


                }
            });
        }
    }, DefenseWarn.init);

    DefenseWarn.show = DefenseWarn.show || {};
    DefenseWarn.show = $.extend({
        doShow: function() {
            var tbodyObj = DefenseWarn.container.find(".defense-warn-table").find("tbody");
            tbodyObj.html("");
            $.ajax({
                url : '/skincomment/queryDefenseWarns',
                data : {},
                type : 'post',
                success : function(dataJson) {
//                    if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//                        return;
                	if(dataJson == null || dataJson == undefined) {
                		alert('操作失败，请稍后再试');
                		return;
                	}
                    tbodyObj.html("");
                    var warnJsonArray = dataJson.res;
                    $(warnJsonArray).each(function(index, warnJson) {
                        var trObj = DefenseWarn.row.createRow(index, warnJson);
                        tbodyObj.append(trObj);
                    });
                }
            });
        }
    }, DefenseWarn.show);


    DefenseWarn.row = DefenseWarn.row || {};
    DefenseWarn.row = $.extend({
        createRow: function(index, warnJson) {
            var html = '' +
                '<tr>' +
                '   <td><span class="telephone"></span> </td>' +
                '   <td><span class="remark"></span> </td>' +
                '   <td><span class="modify-warn-btn commbutton btntext4">修改</span> </td>' +
                '   <td><span class="delete-warn-btn commbutton btntext4">删除</span> </td>' +
                '</tr>' +
                '';

            var trObj = $(html);
            trObj.find(".telephone").html(warnJson.telephone);
            trObj.find(".remark").html(warnJson.remark);
            trObj.find(".modify-warn-btn").click(function() {
                DefenseWarn.submit.doModifyWarn(warnJson.id, warnJson);
            });
            trObj.find(".delete-warn-btn").click(function() {
                DefenseWarn.submit.doDeleteWarn(warnJson.id, warnJson);
            });
            return trObj;
        }
    }, DefenseWarn.row);


    DefenseWarn.submit = DefenseWarn.submit || {};
    DefenseWarn.submit = $.extend({
        doDeleteWarn: function(warnId, warnJson) {
            if (confirm("确定删除手机号码：" + warnJson.telephone + "？") == false)
                return;
            var data = {};
            data.warnId = warnId;
            $.ajax({
                url : '/SkinComment/deleteWarn',
                data : data,
                type : 'post',
                success : function(dataJson) {
//                    if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//                        return;
                	if(dataJson == null || dataJson == undefined) {
                		alert('操作失败，请稍后再试');
                		return;
                	}
                    //刷新
                    DefenseWarn.show.doShow();
                }
            });

        },
        doAddWarn: function() {
            DefenseWarn.submit.modifyWarnParams.warnId = 0;
            DefenseWarn.submit.modifyWarnParams.warnJson = {};
            DefenseWarn.submit.addOrModifyWarn(true);
        },
        doModifyWarn: function(warnId, warnJson) {
            DefenseWarn.submit.modifyWarnParams.warnId = warnId;
            DefenseWarn.submit.modifyWarnParams.warnJson = warnJson;
            DefenseWarn.submit.addOrModifyWarn(false);
        },
        modifyWarnParams: {
            warnId: 0,
            warnJson: {}
        },
        addOrModifyWarn: function(isAdd) {
            var dialogObj = $(".modify-warn-div");
            if (dialogObj.length == 0) {
                var html = '' +
                    '<div class="modify-warn-div">' +
                    '   <table style="margin: 0 auto;">' +
                    '       <tbody>' +
                    '       <tr>' +
                    '           <td style="height: 30px;">手机号码：</td>' +
                    '           <td><input type="text" class="warn-telephone-text" style="width: 200px;"/> </td>' +
                    '       </tr>' +
                    '       <tr>' +
                    '           <td>备注(可不填)：</td>' +
                    '           <td><input type="text" class="warn-remark-text"  style="width: 200px;"/> </td>' +
                    '       </tr>' +
                    '       </tbody>' +
                    '   </table>' +
                    '</div> ' +
                    '';

                dialogObj = $(html);
                $("body").append(dialogObj);

                var title = "";
                if (isAdd == true)
                    title = "设置手机号码";
                else
                    title = "设置手机号码";
                dialogObj.dialog({
                    modal: true,
                    bgiframe: true,
                    height:250,
                    width:450,
                    title: title,
                    autoOpen: false,
                    resizable: false,
                    buttons:{'确定':function() {
                        var warnId = DefenseWarn.submit.modifyWarnParams.warnId;
                        var warnJson = DefenseWarn.submit.modifyWarnParams.warnJson;
                        var telephone = dialogObj.find(".warn-telephone-text").val();
                        var remark = dialogObj.find(".warn-remark-text").val();
                        if (telephone == "") {
                            alert("请先输入手机号码");
                            return;
                        }
                        var confrimStr = "";
                        if (warnId > 0) {
                            if (confirm("确定修改手机号码：" + warnJson.telephone + "？") == false) {
                                return;
                            }
                        } else {

                        }

                        var data = {};
                        data.warnId = warnId;
                        data.telephone = telephone;
                        data.remark = remark;
                        $.ajax({
                            url : '/SkinComment/addOrModifyWarn',
                            data : data,
                            type : 'post',
                            success : function(dataJson) {
//                                if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//                                    return;
                            	if(dataJson == null || dataJson == undefined) {
                            		alert('操作失败，请稍后再试');
                            		return;
                            	}
                                //刷新
                                dialogObj.dialog('close');
                                DefenseWarn.show.doShow();
                            }
                        });


                    },'取消':function(){
                        $(this).dialog('close');
                    }}
                });
            }
            dialogObj.find(".warn-telephone-text").val(DefenseWarn.submit.modifyWarnParams.warnJson.telephone);
            dialogObj.find(".warn-remark-text").val(DefenseWarn.submit.modifyWarnParams.warnJson.remark);
            dialogObj.dialog('open');
        }
    }, DefenseWarn.submit);

})(jQuery,window));