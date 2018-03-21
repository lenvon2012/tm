var TM = TM || {};
((function ($, window) {
    TM.CommentNormal = TM.CommentNormal || {};

    var CommentNormal = TM.CommentNormal;

    CommentNormal.Init = CommentNormal.Init || {};
    CommentNormal.Init = $.extend({
        init : function(container){

//            $.get("/items/tradeUpdated",function(data){
//                if(!data){setStaticEvent
//                    TM.Alert.load("亲，数据加载出错了，请重试或联系客服");
//                } else if(data == "亲，差评防御师正在为您进行订单同步，这大约需要1小时左右，请耐心等待"){
//                    TM.Alert.load(data);
//                }
//            });


//            TM.CommentNormal.Util.setModelContent();
//            container.find(".date-picker").datepicker();
            container.find(".start-time-text").datetimepicker({
            	maxDate : "d",
            	onClose : function(selectedDate) {
    			if(selectedDate != null && selectedDate.length > 0){
    				$(".end-time-text").datetimepicker("option", "minDate", selectedDate);
    			}
    		}});
            container.find(".end-time-text").datetimepicker({
            	maxDate : "d",
            	onClose : function(selectedDate) {
    			if(selectedDate != null && selectedDate.length > 0){
    				$(".start-time-text").datetimepicker("option", "maxDate", selectedDate);
    			}
    		}});

            TM.CommentNormal.Util.initSearchParams();
            // update the trade-search-table-tbody content
            TM.CommentNormal.TradeSearch.search();

            TM.CommentNormal.Event.setStaticEvent();

            TM.CommentNormal.Init.initShortMsg(container);

            TM.CommentNormal.Init.initMsgLogs();

            TM.CommentNormal.Init.initRateMsg(container);

            TM.CommentNormal.Init.initZfbMsg(container);

        },
        initShortMsg:function(container){
            //得到发送短信的按钮
            var shortMsgBtn=$("#short-msg-btn");
            shortMsgBtn.click(function(){
                var subCheck=container
                    .find(".subCheck:checked")
                    .parent()
                    .parent();
                if(subCheck.length<=0){
                    alert("亲，请至少选择一个用户，然后再发送短信!  ");
                    return;
                }

                TM.CommentNormal.Show.showSendMsgWindow(subCheck);


            });
        },
        initMsgLogs:function(){
              $("#msg-log-btn").click(function(){
                  CommentNormal.Show.showSendMsgLogs();

                  //阻止事件冒泡
//                  event.stopPropagation();
              });
        },
        initRateMsg:function(container){
            //发送短信按钮
            var rateMsgBtn=$(".rate-sms-send-btn");
            rateMsgBtn.click(function(){
            	alert("亲，应短信运营商要求，中差评修改相关短信暂时停止发送，请见谅！");
            	return;
            	
                var subCheck=container
                    .find(".subCheck:checked")
                    .parent()
                    .parent();
                if(subCheck.length <= 0){
                    alert("亲，请至少选择一个用户，然后再发送短信!");
                    return;
                }
                // 获取剩余短信条数
                $.ajax({
                    url : '/SkinDefender/getRateSmsRemainCount',
                    type : 'post',
                    dataType:"text",
                    success : function(data) {
                        if(data == null || data == undefined) {
                            alert('出错啦，请刷新重试一下，有问题联系客服哦~');
                            return;
                        } else {
                        	TM.CommentNormal.Show.showRateMsgSendDiv(subCheck, data);
                        }
                    }
                });
            });
        },
        initZfbMsg:function(container){
            //发送短信按钮
            var rateMsgBtn=$(".zfb-sms-send-btn");
            rateMsgBtn.click(function(){
            	alert("亲，应短信运营商要求，中差评修改相关短信暂时停止发送，请见谅！");
            	return;
            	
                var subCheck=container
                    .find(".subCheck:checked")
                    .parent()
                    .parent();
                if(subCheck.length <= 0){
                    alert("亲，请至少选择一个用户，然后再发送短信!");
                    return;
                }
                // 获取剩余短信条数
                $.ajax({
                    url : '/SkinDefender/getRateSmsRemainCount',
                    type : 'post',
                    dataType:"text",
                    success : function(data) {
                        if(data == null || data == undefined) {
                            alert('出错啦，请刷新重试一下，有问题联系客服哦~');
                            return;
                        } else {
                        	TM.CommentNormal.Show.showZfbMsgSendDiv(subCheck, data);
                        }
                    }
                });
            });
        },
        initposhytip:function(){
            var showTd=$(".msg-info");

            showTd.poshytip({
                content:function(){
                    return  $(this).find(".msg-content").html();
                },
                className: 'tip-yellow',
                showTimeout: 1,
                alignTo: 'target',
                alignX: 'left',
                offsetY: -40,
                allowTipHover: true
            });

        }
    },CommentNormal.Init);

    CommentNormal.Show = CommentNormal.Show || {};
    CommentNormal.Show = $.extend({
        showSendMsgWindow:function(checkedArray){
            var msgWindow = $(".dialog-div");
            msgWindow.empty();

            var sendHtml="";

            //思路，这里先得到号码
            var phones=new Array();
            var msgInfos="";
            checkedArray.each(function(index,element){
                //得到电话号码
                var curSplit=$(element).find(".nick-info").html().split("<br>");
                var curPhone= $.trim(curSplit[1]);

                //组合成一个tr,方便放如table 中
                sendHtml+=
                    '       <tr>' +
                    '           <td style="height:30px; width: 250px;"><span style="width: 250px; text-align: left;">'+curSplit[0]+'</span></td>' +
                    '           <td>'+curSplit[1]+'</td>' +
                    '           <td>'+curSplit[2]+'</td>' +
                    '       </tr>'

                //通过算法过滤掉重复的电话号码
                if(phones.indexOf(curPhone)==-1){
                    phones[phones.length]=curPhone;
//                    msgInfos.msgInfos[msgInfos.msgInfos.length]=curSplit;
                    msgInfos+=curSplit[0]+","+curSplit[1]+","+ $.trim($("<div>"+curSplit[2]+"</div>").text())+"#"
                }

            });



            var msg=
                '       <div class="msg">' +
                '           <div style="height:40px; "><span style="width: 250px; text-align: left; color: #999; margin-top: 20px;">提示：短信发送系统会自动多虑掉重复的电话号码（多个相同号码，只发送一条）</span></div>' +
                '           <div class="black-line"></div>' +
                '           <div class="msg-left" style="height:40px; width: 250px;"><span style="width: 250px; text-align: left;">请输入短信内容：</span></div>' +
                '           <div class="msg-right"><textarea class="content-text" style="width: 250px; height: 120px; "></textarea> </div>' +
                '       </div>' ;

            var html = '' +
                '<div class="dialog-div" style="background:#fff; width: 500px; height: 350px">' +
                '   <table class="dialog-tab" style="margin: 0 auto; margin-top: 20px;">' +
                '   <thead>'+
                '       <tr>' +
                '           <td style="height:40px; width: 30%;"><span style=" text-align: left;">姓名</span></td>' +
                '           <td style="width:30%;">电话</td>' +
                '           <td style="width:40%;">旺旺</td>' +
                '       </tr>'+
                '   </thead>'+
                '       <tbody>' +
                sendHtml+

                '       </tbody>' +
                '   </table>' +
                msg+
                '</div> ';

            msgWindow = $(html);
            $("body").append(msgWindow);

            var title = "批量发送短信：你正在给"+checkedArray.length+"个用户发送短信";
            msgWindow.dialog({
                modal: true,
                bgiframe: true,
                height:450+checkedArray.length*40,
                width:650,
                title: title,
                autoOpen: false,
                resizable: false,
                buttons:{'确定发送':function() {
                    var content = msgWindow.find(".content-text").val();
                    //校验一下短信内容
                    if(content==null||$.trim(content)==""){
                        alert("请输入短信内容");
                        msgWindow.find(".content-text").focus();
                        return;
                    };
                    $.ajax({
                        //测试方法
//                          url : '/SkinDefender/testSendMsg',
                        url : '/SkinDefender/sendMsg',
                        data : {
                            content:content,
                            msgInfoStrings:msgInfos
                        },
                        type : 'post',
                        dataType:"text",
                        success : function(dataJson) {
//                                if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//                                    return;
                            if(dataJson == null || dataJson == undefined) {
                                alert('出错啦，请刷新重试一下，有问题联系客服哦~');
                                return;
                            } else {
                                TM.Alert.load('<br><p style="font-size:14px">'+dataJson+'</p>',400,230,function(){

                                });
                            }
                            //刷新
                            msgWindow.dialog('close');
                        }
                    });
                },'取消':function(){
                    $(this).dialog('close');
                }}
            });

            msgWindow.dialog('open');
        },
        showSendMsgLogs:function(){


            //    listLog.addrName = addrName;
//    listLog.addrPhone = addrPhone;
//    listLog.buyernick = buyernick;
//    listLog.sendTime = sendTime;
//    listLog.isSuccess = isSuccess;
//    listLog.msgInfo = msgInfo;
//    listLog.Remarks = Remarks;

            //生成tab
            var msgLogTable ='<table class="msg-log-tab">' +
                '<thead class="msg-log-thead">' +
                '<tr>' +
                '<td style="width:15%">收件人</td>' +
                '<td style="width:15%">电话号码</td>' +
                '<td style="width:15%">旺旺名称</td>' +
                '<td style="width:15%">发送时间</td>' +
                '<td style="width:15%">短信内容</td>' +
                '<td style="width:10%">是否成功</td>' +
                '<td style="width:15%">备注</td>' +
                '</tr>' +
                '</thead>' +
                '<tbody class="msg-log-tbody">' +
                '</tbody>' +
                '</table>';


            //弹出一个窗口
            TM.Alert.load('<div class="msg-log-box">' +
                '<div class="msg-log-title">短信发送记录：</div>' +
                '<div class="msg-log-header"></div>' +
                '<div class="msg-log-body">' +
                msgLogTable +
                '</div>' +
                '</div>',800,650);


            $(".msg-log-header").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    dataType: 'json',
                    url: '/SkinDefender/findPage',
                    callback: function(dataJson){



                        if(!dataJson){
                            alert("数据获取发生错误，请稍后重试或联系客服");
                        } else {
                            // empty the old trade-search-table-tbody content
                            if(!dataJson.isOk){
                                $('.msg-log-tbody').html('<tr><td colspan="7" style="height:40px;">'+dataJson.msg+'</td></tr>');

                            }
                            else if(dataJson.res != null && dataJson.res != undefined && dataJson.res.length > 0){
                                var res=CommentNormal.Show.formatMsgLog(dataJson.res);
                                var html=$("#msg-log-tmpl").tmpl(res);
                                $(".msg-log-tbody").html(html);
                                CommentNormal.Init.initposhytip();

                            } else {
                                $('.msg-log-tbody').html('<tr><td colspan="7" style="height:40px;">亲，暂无数据哦</td></tr>');
                            }
                        }
                    }
                }

            });

        },
        formatMsgLog:function(res){

            $(res).each(function(i,e){
                e.sendTime=(new Date(e.sendTime)).formatYMDHMS();
                e.subMsg=$.trim(e.msgInfo).substring(0,5)+"...";

            });

            return res;
        },
        formatRateMsgLog:function(res){

            $(res).each(function(i,e){
            	if(e.createTs) {
            		e.createTsStr=(new Date(e.createTs)).formatYMDHMS();
            	}
            	if(e.receiveTs) {
            		e.receiveTsStr=(new Date(e.receiveTs)).formatYMDHMS();
            	}
            });

            return res;
        },
        // 短信发送--手机号码
        showRateMsgSendDiv:function(checkedArray, remainCount){
            var msgDiv = $(".dialog-div");
            msgDiv.empty();

            var sendHtml="";

            var phones=new Array();
            var msgInfos="";
            checkedArray.each(function(index,element){
                // 基础信息
                var curSplit=$(element).find(".nick-info").html().split("<br>");
                var curName= $.trim(curSplit[0]);
                var curPhone= $.trim(curSplit[1]);
                var currOid = $(element).attr("oid");
                
                msgInfos += currOid + "," + curPhone + "#";

                // 组合成一个tr,方便放如table中
                sendHtml+=
                    '       <tr>' +
                    '           <td style="height: 30px; width: 250px;"><span style="width: 250px; text-align: left;">'+currOid+'</span></td>' +
                    '           <td>'+curName+'</td>' +
                    '           <td>'+curPhone+'</td>' +
                    '       </tr>'
            });

            var msg=
                '       <div class="msg">' +
                '           <div style="height: 40px;"><span style="width: 250px; text-align: left; color: #999"></span></div>' +
                '           <div class="black-line"></div>' +
                '           <div style="height: 40px; margin-top: 40px; text-align: center;"><span style="width: 250px; color: red;">剩余短信条数：'+remainCount+'&nbsp;&nbsp;&nbsp;&nbsp;请输入短信内容：</span></div>' +
                '           <div><textarea class="content-text" style="width: 400px; height: 120px; margin-left: 100px;"></textarea> </div>' +
                '       </div>' ;

            var html = '' +
                '<div class="dialog-div" style="background: #fff; width: 500px; height: 350px">' +
                '   <table class="dialog-tab" style="margin: 0 auto; margin-top: 20px;">' +
                '   <thead>'+
                '       <tr>' +
                '           <td style="height: 40px; width: 30%;"><span style="text-align: left;">子订单编号</span></td>' +
                '           <td style="width: 30%;">姓名</td>' +
                '           <td style="width: 40%;">号码</td>' +
                '       </tr>'+
                '   </thead>'+
                '       <tbody>' +
                sendHtml+
                '       </tbody>' +
                '   </table>' +
                msg+
                '</div> ';

            msgDiv = $(html);
            $("body").append(msgDiv);

            var title = "批量发送短信：你正在给"+checkedArray.length+"个用户发送短信";
            msgDiv.dialog({
                modal: true,
                bgiframe: true,
                height:450+checkedArray.length*40,
                width:650,
                title: title,
                autoOpen: false,
                resizable: false,
                buttons:{'确定发送':function() {
                    var content = msgDiv.find(".content-text").val();
                    //校验一下短信内容
                    if(content==null||$.trim(content)==""){
                        alert("请输入短信内容");
                        msgDiv.find(".content-text").focus();
                        return;
                    };
                    $.ajax({
                        url : '/SkinDefender/sendRateMsg',
                        data : {
                            content:content,
                            msgInfoStrings:msgInfos
                        },
                        type : 'post',
                        dataType:"text",
                        success : function(dataJson) {
                            if(dataJson == null || dataJson == undefined) {
                                alert('出错啦，请刷新重试一下，有问题联系客服哦~');
                                return;
                            } else {
                                TM.Alert.load('<br><p style="font-size: 14px; margin-top: 20px;">'+dataJson+'</p>',400,230,function(){});
                            }
                            //刷新
                            msgDiv.dialog('close');
                        }
                    });
                },'取消':function(){
                    $(this).dialog('close');
                }}
            });

            msgDiv.dialog('open');
        },
        // 短信发送--支付宝
        showZfbMsgSendDiv:function(checkedArray, remainCount){
            var msgDiv = $(".dialog-div");
            msgDiv.empty();

            var sendHtml="";

            var phones=new Array();
            var msgInfos="";
            checkedArray.each(function(index,element){
                // 基础信息
                var curSplit=$(element).find(".nick-info").html().split("<br>");
                if(curSplit.length <= 3) {
                    return;
                }
                var curName= $.trim(curSplit[0]);
                var curPhone= $.trim(curSplit[3]).substring(6);
                // 手机号码验证
                var dg = /^1[3|4|5|7|8]\d{9}$/;
                if (!(curPhone.length == 11 && curPhone.match(dg))) {
                    return;
                }
                var currOid = $(element).attr("oid");
                
                msgInfos += currOid + "," + curPhone + "#";

                // 组合成一个tr,方便放如table中
                sendHtml+=
                    '       <tr>' +
                    '           <td style="height: 30px; width: 250px;"><span style="width: 250px; text-align: left;">'+currOid+'</span></td>' +
                    '           <td>'+curName+'</td>' +
                    '           <td>'+curPhone+'</td>' +
                    '       </tr>'
            });
            
            if(sendHtml == null || sendHtml == '') {
                alert("亲，没有可以发送的号码，请确认支付宝是否为手机号!");
                return false;
            }

            var msg=
                '       <div class="msg">' +
                '           <div style="height: 40px;"><span style="width: 250px; text-align: left; color: #999"></span></div>' +
                '           <div class="black-line"></div>' +
                '           <div style="height: 40px; margin-top: 40px; text-align: center;"><span style="width: 250px; color: red;">剩余短信条数：'+remainCount+'&nbsp;&nbsp;&nbsp;&nbsp;请输入短信内容：</span></div>' +
                '           <div><textarea class="content-text" style="width: 400px; height: 120px; margin-left: 100px;"></textarea> </div>' +
                '       </div>' ;

            var html = '' +
                '<div class="dialog-div" style="background: #fff; width: 500px; height: 350px">' +
                '   <table class="dialog-tab" style="margin: 0 auto; margin-top: 20px;">' +
                '   <thead>'+
                '       <tr>' +
                '           <td style="height: 40px; width: 30%;"><span style="text-align: left;">子订单编号</span></td>' +
                '           <td style="width: 30%;">姓名</td>' +
                '           <td style="width: 40%;">支付宝</td>' +
                '       </tr>'+
                '   </thead>'+
                '       <tbody>' +
                sendHtml+
                '       </tbody>' +
                '   </table>' +
                msg+
                '</div> ';

            msgDiv = $(html);
            $("body").append(msgDiv);

            var title = "批量发送短信：你正在给"+checkedArray.length+"个用户发送短信";
            msgDiv.dialog({
                modal: true,
                bgiframe: true,
                height:450+checkedArray.length*40,
                width:650,
                title: title,
                autoOpen: false,
                resizable: false,
                buttons:{'确定发送':function() {
                    var content = msgDiv.find(".content-text").val();
                    //校验一下短信内容
                    if(content==null||$.trim(content)==""){
                        alert("请输入短信内容");
                        msgDiv.find(".content-text").focus();
                        return;
                    };
                    $.ajax({
                        url : '/SkinDefender/sendRateMsg',
                        data : {
                            content:content,
                            msgInfoStrings:msgInfos
                        },
                        type : 'post',
                        dataType:"text",
                        success : function(dataJson) {
                            if(dataJson == null || dataJson == undefined) {
                                alert('出错啦，请刷新重试一下，有问题联系客服哦~');
                                return;
                            } else {
                                TM.Alert.load('<br><p style="font-size: 14px; margin-top: 20px;">'+dataJson+'</p>',400,230,function(){});
                            }
                            //刷新
                            msgDiv.dialog('close');
                        }
                    });
                },'取消':function(){
                    $(this).dialog('close');
                }}
            });

            msgDiv.dialog('open');
        },
        // 查看发送短信记录
        showRateSmsSendLogs:function(oid) {
        	//生成tab
        	var sendMsgLogTable ='<table class="send-msg-log-tab">' +
        		'<thead class="send-msg-log-thead">' +
        		'<tr>' +
        		'<td style="width:20%">子订单编号</td>' +
        		'<td style="width:15%">电话号码</td>' +
        		'<td style="width:15%">发送时间</td>' +
        		'<td style="width:50%">短信内容</td>' +
        		'</tr>' +
        		'</thead>' +
        		'<tbody class="send-msg-log-tbody">' +
        		'</tbody>' +
        		'</table>';

        	//弹出一个窗口
        	TM.Alert.load('<div class="send-msg-log-box">' +
        		'<div class="send-msg-log-title">短信发送记录：</div>' +
        		'<div class="send-msg-log-header"></div>' +
        		'<div class="send-msg-log-body">' +
        		sendMsgLogTable +
        		'</div>' +
        		'</div>',800,650);

        	$(".send-msg-log-header").tmpage({
        		currPage: 1,
        		pageSize: 10,
        		pageCount: 1,
        		ajax: {
        			on: true,
        			dataType: 'json',
        			param: {oid:oid},
        			url: '/SkinDefender/showSendMsgLog',
        			callback: function(dataJson){
        				if(!dataJson){
        					alert("数据获取发生错误，请稍后重试或联系客服");
        				} else {
        					if(!dataJson.isOk){
        						$('.send-msg-log-tbody').html('<tr><td colspan="4" style="height:40px;">'+dataJson.msg+'</td></tr>');
        					}
        					else if(dataJson.res != null && dataJson.res != undefined && dataJson.res.length > 0){
        						var res=CommentNormal.Show.formatRateMsgLog(dataJson.res);
        						res = dataJson.res;
        						var html=$("#send-msg-log-tmpl").tmpl(res);
        						$(".send-msg-log-tbody").html(html);
//        						CommentNormal.Init.initposhytip();
        					} else {
        						$('.send-msg-log-tbody').html('<tr><td colspan="4" style="height:40px;">亲，暂无数据哦</td></tr>');
        					}
        				}
        			}
        		}
        	});
        },
        // 查看回复短信记录
        showRateSmsReceiveLogs:function(oid) {
        	//生成tab
        	var sendMsgLogTable ='<table class="receive-msg-log-tab">' +
        		'<thead class="receive-msg-log-thead">' +
        		'<tr>' +
        		'<td style="width:20%">子订单编号</td>' +
        		'<td style="width:15%">电话号码</td>' +
        		'<td style="width:15%">发送时间</td>' +
        		'<td style="width:50%">短信内容</td>' +
        		'</tr>' +
        		'</thead>' +
        		'<tbody class="receive-msg-log-tbody">' +
        		'</tbody>' +
        		'</table>';

        	//弹出一个窗口
        	TM.Alert.load('<div class="receive-msg-log-box">' +
        		'<div class="receive-msg-log-title">短信回复记录：</div>' +
        		'<div class="receive-msg-log-header"></div>' +
        		'<div class="receive-msg-log-body">' +
        		sendMsgLogTable +
        		'</div>' +
        		'</div>',800,650);

        	$(".receive-msg-log-header").tmpage({
        		currPage: 1,
        		pageSize: 10,
        		pageCount: 1,
        		ajax: {
        			on: true,
        			dataType: 'json',
        			param: {oid:oid},
        			url: '/SkinDefender/showReceiveMsgLog',
        			callback: function(dataJson){
        				if(!dataJson){
        					alert("数据获取发生错误，请稍后重试或联系客服");
        				} else {
        					if(!dataJson.isOk){
        						$('.receive-msg-log-tbody').html('<tr><td colspan="4" style="height:40px;">'+dataJson.msg+'</td></tr>');
        					}
        					else if(dataJson.res != null && dataJson.res != undefined && dataJson.res.length > 0){
        						var res=CommentNormal.Show.formatRateMsgLog(dataJson.res);
        						res = dataJson.res;
        						var html=$("#receive-msg-log-tmpl").tmpl(res);
        						$(".receive-msg-log-tbody").html(html);
//        						CommentNormal.Init.initposhytip();
        					} else {
        						$('.receive-msg-log-tbody').html('<tr><td colspan="4" style="height:40px;">亲，暂无数据哦</td></tr>');
        					}
        				}
        			}
        		}
        	});
        },
    },CommentNormal.Show);

    CommentNormal.TradeSearch = CommentNormal.TradeSearch || {};
    CommentNormal.TradeSearch = $.extend({
        search : function(){
            var ruleData = CommentNormal.TradeSearch.getQueryRule();
            ruleData.isShowAll = true;
            $('.checkAll').attr("checked",false);
            $(".trade-search-pagging").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: ruleData,
                    dataType: 'json',
                    url: '/SkinComment/queryTradeRate',
                    callback: function(dataJson){
                        if(!dataJson || !dataJson.isOk){
                            TM.Alert.load("数据获取发生错误，请稍后重试或联系客服");
                        } else {
                            // empty the old trade-search-table-tbody content
                            $('.skincomment-table-tbody').remove();
                            if(dataJson.res != null && dataJson.res != undefined && dataJson.res.length > 0){
                            	$('#totalCount').html('搜索到总记录：'+dataJson.count+' 条');
                                $('.commentNormal-table').append(TM.CommentNormal.TradeSearch.createTbody(dataJson.res));

                                TM.CommentNormal.Event.setCopyLinkEvent();
                                TM.CommentNormal.Event.setShowMsgLogEvent();
                                TM.CommentNormal.Event.setRemarkSaveClickEvent();
                            } else {
                                $('#totalCount').html('搜索到总记录：0 条');
                            	$('.commentNormal-table').append('<tbody class="skincomment-table-tbody" ><tr><td colspan="11" style="height:40px;">亲，暂无数据哦</td></tr></tbody>');
                            }
                        }
                    }
                }

            });
        },
        exportExcel : function(btn){
            var ruleData = CommentNormal.TradeSearch.getQueryRule();
            var param = "";
            for(var p in ruleData) {
                param += p + "=" + encodeURIComponent(ruleData[p]) + "&";
            }
            param = param.substring(0, param.length - 1);
            btn.attr('href', '/SkinComment/exportTradeRate?'+param);
        },
        getQueryRule : function(){
            var ruleData = {};
            var rate = $('.trade-type span.selected').find('label').attr('type');
            ruleData.rate = rate;
//            var rate = $('#rate option:selected').attr("tag");
//            ruleData.rate = rate;
            var online = $('#online option:selected').attr("tag");
            ruleData.online = online;
            var tradeId = $('.trade-id-text').val();
            ruleData.tradeId = tradeId;
            var buyerNick = $('.buyer-name-text').val();
            ruleData.buyerNick = buyerNick;
            var phone = $('.buyer-phone-text').val();
            ruleData.phone = phone;
            var numIid = $('.numIid-text').val();
            ruleData.numIid = numIid;
            var startTime = $('.start-time-text').val();
            ruleData.startTime = startTime;
            var endTime = $('.end-time-text').val();
            ruleData.endTime = endTime;

            var dispatchId = $(".servicegroup option:selected").val();
            ruleData.dispatchId = dispatchId;
            return ruleData;
        },
        createTbody : function(results) {
            var tbody = $('<tbody class="skincomment-table-tbody" ></tbody>');
            $(results).each(function(i,result){
                if(result.tradeRate.tid == 784706833508892) {
                    return;
                }
                var rate,rate_content;
                var buyer_rate_time;
                var expire = '';
                var buyerNick = '';
                var created = '';
                var phone = '';
                var remark = '';
                var receiverName = '';
                var rate_change_deadline = '';
                var modify_link;

                buyer_rate_time = '[评价]'+new Date(result.tradeRate.created).format("yyyy-MM-dd hh:mm:ss") + '<br>';
//                if(result.tradeRate.updated > result.tradeRate.created){
//                    buyer_rate_time += '[修改]'+new Date(result.tradeRate.updated).format("yyyy-MM-dd hh:mm:ss") + '<br>';
//                }
                rate = TM.CommentNormal.Util.createRateImg(result.tradeRate.rate);
                rate_content = result.tradeRate.content;
                if(result.tradeRate.rate > 3) {
                    if(!result.tradeRate.reverse && result.tradeRate.reverse != null && rate_content != result.tradeRate.reverse){
                        rate_content = "[改评]" + result.tradeRate.reverse;
                        rate_content += "<br>[评价]" + result.tradeRate.content;
                    }

                    buyer_rate_time += '[修改]'+new Date(result.tradeRate.updated).format("yyyy-MM-dd hh:mm:ss") + '<br>';
                	if((result.tradeRate.rate&3) == 0) {
                    	rate_content = "[删除]"+ result.tradeRate.content;
                    }

                    rate = TM.CommentNormal.Util.createRateImg(result.tradeRate.rate>>2);
                    if(result.tradeRate.rate&3>0){
                        rate += "改" + TM.CommentNormal.Util.createRateImg(result.tradeRate.rate);
                    } else {
                        rate += "删除";
                    }

                } else {
                    expire = '[到期] <b>'+TM.CommentNormal.TradeSearch.genRateChangeDeadline(result.tradeRate.created) + '</b>';
                }

                if(result.tradeRate.remark != null) {
                	remark = result.tradeRate.remark;
                }

                if(result.order == null || result.order == undefined) {
                	phone = "等待获取电话";
                	buyerNick = result.tradeRate.nick;
                } else {
                	buyerNick = result.order.buyerNick;
                	created = '[下单]'+ new Date(result.order.created).format("yyyy-MM-dd hh:mm:ss")+'<br>';
                	if(result.order.receiverName != null && result.order.receiverName != '') {
                    	receiverName = result.order.receiverName + "<br>";
                    }
                	phone = result.order.phone;
                }
                modify_link = "http://rate.taobao.com/RateDetailBuyer.htm?parentTradeId=" + result.tradeRate.tidStr;
                
                // 显示未读条数
                var unReadHtml = '';
                if(result.unReadCount > 0) {
                	unReadHtml = '<p style="color: red;" title="未读短信">[未读：' + result.unReadCount + ']</p>';
                }

//                var chk = result.tradeRate.dispatchId>0?'checked':'';
                var chk = '';  // 都不选中
                var groupName = (result.tradeRate.groupName == null || result.tradeRate.groupName == undefined) ? '' : result.tradeRate.groupName;
                tbody.append($('<tr buyerNick="'+buyerNick+'" tid="'+result.tradeRate.tidStr+'" oid="'+result.tradeRate.oidStr+'">' +
                        '<td><input style="width:13px;" type="checkbox" tag="subCheck" class="subCheck" '+chk+'></td>'+
//                        '<td rowspan="1">'+TM.CommentNormal.TradeSearch.createImg(result)+'</td>'+
                        '<td class="detail left-groupName" ><span>'+groupName+'</span></td>'+
                        '<td class="detail sendCount"><div style="cursor: pointer;">'+result.rateSmsSendCount+'</div></td>'+
                        '<td class="detail receiveCount"><div style="cursor: pointer;">'+result.rateSmsReceiveCount + unReadHtml+'</div></td>'+
                        '<td class="detail"><div title="'+result.tradeRate.itemTitle+'">'+TM.CommentNormal.TradeSearch.createTitle(result)+'</div><div>宝贝Id: '+result.tradeRate.numIid+'</div></td>'+
                        '<td class="detail"><a href="http://trade.taobao.com/trade/detail/trade_item_detail.htm?bizOrderId='+result.tradeRate.tidStr + '" target="_blank" style="font-size: 13px;*font-size: 12px;text-decoration: underline;">[订单号]'+result.tradeRate.tidStr + '</a><br>' +buyer_rate_time+expire+'</td>'+
                        '<td class="detail" >'+((result && result.order && result.order.totalFee)?(result.order.totalFee) :'~')+'¥/'+((result && result.order && result.order.price)?(result.order.price) :'~')+'¥</td>'+
                        '<td class="detail" ><div>'+rate+'</div><div style="margin-top: 5px;"><a class="copy_btn" data-clipboard-text="' + modify_link + '" style="cursor: pointer;">复制修改链接</a></div></td>'+
                        //'<td style=";"></td>'+
                        '<td class="detail nick-info">'
                            +receiverName+phone+'<br>'+buyerNick+TM.CommentNormal.Util.createBuyerWangWang(buyerNick)+
                            ((result && result.order && result.order.buyerAlipayNo)?('<br />[支付宝] ' + result.order.buyerAlipayNo) :'')+
                        '</td>'+
                        '<td class="detail" style="max-width:300px;_width:300px;">'+rate_content+'</td>' +
                        '<td><textarea rows="2" style="width:160px;height:46px;">'+remark+'</textarea></td>' +
                        '<td><button class="btn btn-primary save-remark" style="margin-left:3px;" tid="'+result.tradeRate.tidStr+'">保存</button></td></tr>'
                        ));

                tbody.find('.subCheck').click(function(){
                    var $subBox = $("input[tag='subCheck']");
                    $(".checkAll").attr("checked",$subBox.length == $("input[tag='subCheck']:checked").length ? true : false);
                });
                
            });
            tbody.find('tr:odd').addClass('odd');

            return tbody;
        },
        createTitle : function(result){
            //var href = 'http://trade.taobao.com/trade/detail/trade_item_detail.htm?bizOrderId='+result.tradeRate.tid;
            var href = "http://item.taobao.com/item.htm?id=" + result.tradeRate.numIid;
            return '<a target="_blank" style="text-decoration: none;" href="'+href+'">'+result.tradeRate.itemTitle+'</a>';
        },
        createImg : function(result) {
            var href = "http://item.taobao.com/item.htm?id=" + result.tradeRate.numIid;
            var picPath = "http://img01.taobaocdn.com/imgextra/i2/T1jGS6Xl8aXXXXXXXX-70-70.gif";
            if(result.order != null && result.order != undefined) {
            	picPath = result.order.picPath + "_80x80.jpg";
            }
            return '<div ><a target="_blank" href="'+href+'"><img class="imgborder" style="width: 80px;height: 80px;" src="'+picPath+'" title="'+result.tradeRate.itemTitle+'" /></a></div>';
        },
        genRateDeadlineTime : function(time){
            return new Date(time + 15*24*60*60*1000).format("yy-MM-dd hh:mm:ss")
        },
        genRateChangeDeadline : function(time){
            var deadline = new Date(time + 30*24*3600000);
            var now = new Date();

            var minLast = deadline.getMinutes() - now.getMinutes();
            var tmpLast = 0;
            if(minLast < 0) {
                tmpLast = 1;
                minLast += 60;
            }
            var hourLast = deadline.getHours() - now.getHours() - tmpLast;
            tmpLast = 0;
            if(hourLast < 0) {
                tmpLast = 1;
                hourLast += 24;
            }
            var dayLast = Math.floor((deadline.getTime() - now.getTime())/(24*3600000));

            if(dayLast < 0) {
                return "已到期，无法修改";
            }

            return dayLast + "天，" + hourLast + "小时" + minLast + "分";
        }
    },CommentNormal.TradeSearch);

    CommentNormal.Event = CommentNormal.Event || {};
    CommentNormal.Event = $.extend({
        setStaticEvent : function(){
            TM.CommentNormal.Event.setTradeTypeClickEvent();
            TM.CommentNormal.Event.setTradeSearchBtnClickEvent();

            TM.CommentNormal.Event.setCheckAllClickEvent();
            TM.CommentNormal.Event.setBatchSelectedDispatchBtnClickEvent();
            TM.CommentNormal.Event.setAddGroupClickEvent();
            TM.CommentNormal.Event.setDeleteGroupClickEvent();
            TM.CommentNormal.Event.bindServiceGroupEvent();
            TM.CommentNormal.ServiceGroup.queryServiceGroups();

        },
        setTradeTypeClickEvent : function(){
            $('.trade-type').find('span').click(function(){
                if(!$(this).hasClass("selected")){
                    // update tmnav selected type
                    $('.trade-type').find('.selected').removeClass('selected');
                    $(this).addClass("selected");
                    // init the search param to original
                    TM.CommentNormal.Util.initSearchParams();
                    // update the trade-search-table-tbody content
                    TM.CommentNormal.TradeSearch.search();
                }
            });

//            $('.trade-type').find('span').eq(0).trigger("click");
        },
        setTradeSearchBtnClickEvent : function() {
            $('.search-btn').click(function(){
                $('.checkAll').attr("checked",false);
                TM.CommentNormal.TradeSearch.search();
            });
            $('.buyer-name-text').keydown(function() {
                if (event.keyCode == "13") {//keyCode=13是回车键
                    $('.search-btn').click();
                }
            });
            $('.buyer-phone-text').keydown(function() {
                if (event.keyCode == "13") {//keyCode=13是回车键
                    $('.search-btn').click();
                }
            });
            $('.numIid-text').keydown(function() {
                if (event.keyCode == "13") {//keyCode=13是回车键
                    $('.search-btn').click();
                }
            });

            $('.update-btn').click(function(){
            	$.get("/items/syncTradeRates", function(data){
            		if(!data.success) {
            			TM.Alert.load('<br><p style="font-size:14px">同步太频繁啦！请5分钟后再来试试吧~</p>',400,230,function(){
            				
            			});
            			return;
            		}
//            		TM.CommentNormal.TradeSearch.search();
            		TM.Alert.load('<br><p style="font-size:14px">亲，更新评价成功，点击确定刷新页面查看数据</p>',400,230,function(){
                        window.location.reload();
                    });
            	});
            });
            $('.export-btn').click(function(){
                TM.CommentNormal.TradeSearch.exportExcel($(this));
            });
        },
        setCheckAllClickEvent : function(){
            $(".checkAll").click(function(){
                $('input[tag="subCheck"]').attr("checked",this.checked);
            });
        },
        setBatchSelectedDispatchBtnClickEvent : function(){
            $('.dispatch-servicegroup-btn').click(function(){
                var oids = "";
                if($('.subCheck:checked').length == 0) {
//                    TM.Alert.load("亲，请先勾选要分配的中差评订单");
                    TM.Alert.load('<br><p style="font-size:14px">亲，请先勾选要分配的中差评订单</p>',400,230);
                } else if($(".servicegroup option").length <= 2){
                    TM.Alert.load("请先在右侧添加客服分组，谢谢",400,230);
                } else {
                    CommentNormal.ServiceGroup.dispatchGroupDialog();
                }
            });
        },
        setCopyLinkEvent : function() {
        	// 初始化复制插件
        	var clipboard = new Clipboard('.copy_btn');
        	clipboard.on('success', function(e) {
        		TM.Alert.load('链接复制成功');
        	});
        },
        setShowMsgLogEvent : function() {
            $('.sendCount div').click(function() {
            	var oid = $(this).parent().parent().attr("oid");
            	CommentNormal.Show.showRateSmsSendLogs(oid);
            });
            $('.receiveCount div').click(function() {
            	var oid = $(this).parent().parent().attr("oid");
            	CommentNormal.Show.showRateSmsReceiveLogs(oid);
            	// 移除未读提醒
            	$(this).children("p").empty();
            });
        },
        setRemarkSaveClickEvent : function() {
        	$('.save-remark').unbind('click');
            $('.save-remark').click(function(){
            	var tid = $(this).attr('tid');
            	var remark = $(this).parent().parent().find("textarea").val();
            	$.post("/skincomment/saveRemark", {tid:tid, remark:remark}, function(data){
            		/*
                    TM.Alert.load('<br><p style="font-size:14px">'+data.msg+'</p>',400,230,function(){
                        window.location.reload();
                    });
                    */
            	});
            });
        },
        setAddGroupClickEvent : function() {
            $(".add-servicegroup-btn").unbind("click");
            $(".add-servicegroup-btn").click(function(){
                CommentNormal.ServiceGroup.addGroupDialog();
            });
        },
        setDeleteGroupClickEvent : function() {
            $(".delete-servicegroup-btn").unbind("click");
            $(".delete-servicegroup-btn").click(function(){
                CommentNormal.ServiceGroup.deleteGroupDialog();
            });
        },
        bindServiceGroupEvent : function() {
            $(".servicegroup").change(function(){
                TM.CommentNormal.TradeSearch.search();
            });
        }

    },CommentNormal.Event);

    CommentNormal.ServiceGroup = CommentNormal.ServiceGroup || {};
    CommentNormal.ServiceGroup = $.extend({
        setModelContent : function(){
            $.get('/autocomments/currContent',function(data){
                if(!data){
                    return;
                }
            });
        },
        queryServiceGroups: function() {
            $.get("/skincomment/queryServiceGroups", function(data){
                var servicegroup = $(".servicegroup");
                servicegroup.empty();

                servicegroup.append('<option value="-1">显示所有</option>');
                servicegroup.append('<option value="0">未分配</option>');

                if(data && data.res.length > 0) {
                    $(data.res).each(function(i,one){
                        servicegroup.append('<option value="'+one.id+'">'+one.groupName+'</option>');
                    });
                }
            });
        },
        addGroupDialog: function(){
            var dialogObj = $(".dialog-div");
            dialogObj.empty();

            var html = '' +
                '<div class="dialog-div" style="background:#fff;">' +
                '   <table style="margin: 0 auto; margin-top: 20px;">' +
                '       <tbody>' +
                '       <tr>' +
                '           <td style="height:40px;">新增分组名称：</td>' +
                '           <td><input type="text" class="servicegroup-name" style="width: 200px;"/> </td>' +
                '       </tr>' +
//                    '       <tr>' +
//                    '           <td>备注(可不填)：</td>' +
//                    '           <td><input type="text" class="warn-remark-text"  style="width: 200px;"/> </td>' +
//                    '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '</div> ' +
                '';

            dialogObj = $(html);
            $("body").append(dialogObj);

            var title = "添加客服分组";
            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:250,
                width:450,
                title: title,
                autoOpen: false,
                resizable: false,
                buttons:{'确定':function() {
                    var groupName = dialogObj.find(".servicegroup-name").val();
                    $.ajax({
                        url : '/SkinComment/addServiceGroup',
                        data : {groupName: groupName},
                        type : 'post',
                        success : function(dataJson) {
//                                if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//                                    return;
                            if(dataJson == null || dataJson == undefined) {
                                alert('出错啦，请刷新重试一下，有问题联系客服哦~');
                                return;
                            } else {
                                TM.Alert.load('<br><p style="font-size:14px">'+dataJson.message+'</p>',400,230,function(){
//                                    TM.CommentNormal.ServiceGroup.queryServiceGroups();
//                                    TM.CommentNormal.TradeSearch.search();
                                    window.location.reload();
                                });
                            }
                            //刷新
                            dialogObj.dialog('close');
                        }
                    });
                },'取消':function(){
                    $(this).dialog('close');
                }}
            });

            dialogObj.dialog('open');
        },

        dispatchGroupDialog: function(){
            var dialogObj = $(".dialog-div");
            dialogObj.empty();
            var html = '' +
                '<div class="dialog-div" style="background:#fff;">' +
                '   <table style="margin: 0 auto; margin-top: 20px;">' +
                '       <tbody>' +
                '       <tr>' +
                '           <td style="height:40px;">选择分配到：</td>' +
                '           <td><select name="op-servicegroup" class="op-servicegroup">' +
                '           </select></td>' +
                '       </tr>' +
//                    '       <tr>' +
//                    '           <td>备注(可不填)：</td>' +
//                    '           <td><input type="text" class="warn-remark-text"  style="width: 200px;"/> </td>' +
//                    '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '</div> ' +
                '';

            dialogObj = $(html);

            $("body").append(dialogObj);

            var title = "分配中差评订单到对应客服";
            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:250,
                width:450,
                title: title,
                autoOpen: false,
                resizable: false,
                buttons:{'确定':function() {
                    var oids = "";
                    $('.subCheck:checked').each(function(){
                        var oid = $(this).parent().parent().attr('oid');
                        oids += oid + ",";
                    });
                    var dispatchId = dialogObj.find(".op-servicegroup option:selected").attr("tag");
                    $(this).dialog('close');

                    $.post("/SkinComment/dispatchServiceGroup",{oids:oids, dispatchId:dispatchId},function(data){
                        if(!data){
                            TM.Alert.load("出错啦，请刷新重试一下，有问题联系客服哦~");
                        } else {
                            TM.Alert.load('<br><p style="font-size:14px">'+data.message+'</p>',400,230,function(){
                                TM.CommentNormal.TradeSearch.search();
                            });
//                                TM.CommentNormal.Util.showRateResult(data);
                        }
                    });

                },'取消':function(){
                    $(this).dialog('close');
                }}
            });

            $.get("/skincomment/queryServiceGroups", function(data){
                var servicegroup = dialogObj.find(".op-servicegroup");
                servicegroup.empty();

                if(data && data.res.length > 0) {
                    servicegroup.append('<option tag="0">不分配</option>');
                    $(data.res).each(function(i,one){
                        servicegroup.append('<option tag="'+one.id+'">'+one.groupName+'</option>');
                    });
                }
                dialogObj.dialog('open');
            });

        },

        deleteGroupDialog: function(){
            var dialogObj = $(".dialog-div");
            dialogObj.empty();
            var html = '' +
                '<div class="dialog-div" style="background:#fff;">' +
                '   <table style="margin: 0 auto; margin-top: 20px;">' +
                '       <tbody>' +
                '       <tr>' +
                '           <td style="height:40px;">选择要删除的分组：</td>' +
                '           <td><select name="op-servicegroup" class="op-servicegroup">' +
                '           </select></td>' +
                '       </tr>' +
//                    '       <tr>' +
//                    '           <td>备注(可不填)：</td>' +
//                    '           <td><input type="text" class="warn-remark-text"  style="width: 200px;"/> </td>' +
//                    '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '</div> ' +
                '';

            dialogObj = $(html);


            $("body").append(dialogObj);

            var title = "选择删除客服";
            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:250,
                width:450,
                title: title,
                autoOpen: false,
                resizable: false,
                buttons:{'确定':function() {
                    var groupId = dialogObj.find(".op-servicegroup option:selected").attr("tag");
                    $(this).dialog('close');
                    $.post("/SkinComment/deleteServiceGroup",{groupId:groupId},function(data){
                        if(!data){
                            TM.Alert.load("出错啦，请刷新重试一下，有问题联系客服哦~");
                        } else {
                            TM.Alert.load('<br><p style="font-size:14px">'+data.message+'</p>',400,230,function(){
                                window.location.reload();
                            });
//                                TM.CommentNormal.Util.showRateResult(data);
                        }
                    });

                },'取消':function(){
                    $(this).dialog('close');
                }}
            });


            $.get("/skincomment/queryServiceGroups", function(data){
                var servicegroup = dialogObj.find(".op-servicegroup");
                servicegroup.empty();

                if(data && data.res.length > 0) {
                    $(data.res).each(function(i,one){
                        servicegroup.append($('<option tag="'+one.id+'">'+one.groupName+'</option>'));
                    });
                }

                dialogObj.dialog('open');
            });
        }

    },CommentNormal.ServiceGroup);

    CommentNormal.Util = CommentNormal.Util || {};
    CommentNormal.Util = $.extend({
        setModelContent : function(){
            $.get('/autocomments/currContent',function(data){
                if(!data){
                    return;
                }

                var commentModels = data.res.split("!@#");
                var commentModelsArea = $('<div class="commentModelsArea"></div>');
                var random = Math.floor(Math.random()*(commentModels.length-1));
                for(var i = 0; i<commentModels.length-1; i++) {
                    var selected_img = "model-unselected";
                    var selected_text = "comment-model-option-unselected";
                    if(i == random){
                        selected_img = "model-selected";
                        selected_text = "comment-model-option-selected";
                        $('.comment-content').text(commentModels[i]);
                    }
                    commentModelsArea.append($('' +
                        '<div class="singleModel '+selected_img+'">'+
                        '<p>'+commentModels[i]+'</p>'+
                        '<div class="comment-model-option '+selected_text+'">已选择</div>'+
                        '</div>'+
                        ''));
                }
                commentModelsArea.find('.singleModel').click(function(){
                    if($(this).hasClass("model-unselected")) {
                        var selected = $(this).parent().find('.model-selected');
                        selected.removeClass('model-selected');
                        selected.addClass("model-unselected");
                        selected.find('.comment-model-option').text("未选择");
                        selected.find('.comment-model-option').removeClass('comment-model-option-selected');
                        $(this).removeClass('model-unselected');
                        $(this).addClass('model-selected');
                        $(this).find('.comment-model-option').text("已选择");
                        $(this).find('.comment-model-option').addClass('comment-model-option-selected');
                        $('.comment-content').val($(this).find('p').text());
                    }
                });
                $('.modelTip').parent().append(commentModelsArea);
            });
        },
        initSearchParams : function(){
            $('.trade-id-text').val("");
            $('.buyer-name-text').val("");
            $('.buyer-phone-text').val("");
            $('.numIid-text').val("");

            var dayMillis = 24 * 3600 * 1000;
            var timeZoneDiff = 8 * 3600 * 1000;
            var endDate = new Date();
            var endTime = parseInt((endDate.getTime() + timeZoneDiff) / dayMillis) * dayMillis - timeZoneDiff + dayMillis - 1;
            endDate.setTime(endTime);
            var startDate = new Date();
			var startTime = endTime - 31 * dayMillis + 1;
			startDate.setTime(startTime);
			$(".start-time-text").val(startDate.format("yyyy-MM-dd hh:mm:ss"));
			$(".end-time-text").val(endDate.format("yyyy-MM-dd hh:mm:ss"));

            $('.checkAll').attr("checked",false);

        },
        createBuyerWangWang : function(buyerNick){
            //return '<a class="buyer-wangwang" target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid='+encodeURI(buyerNick)+'&siteid=cntaobao&status=1&charset=utf-8">'+
            return '<a class="buyer-wangwang" target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid=' + encodeURI(buyerNick) + '&siteid=cntaobao&status=2&charset=utf-8">'+
                '<img class="wwimg" border="0" src="http://amos.alicdn.com/online.aw?v=2&uid='+encodeURI(buyerNick)+'&site=cntaobao&s=2&charset=utf-8" alt="联系买家" /> '+
                '</a>';
        },
        createRateImg : function(rate){
            var real_rate = rate & 3;
            var rate_value;
            if(real_rate == 1) {
                rate_value = "buyer-good-rate";
            } else if(real_rate == 2) {
                rate_value = "buyer-netural-rate";
            } else if(real_rate == 3) {
                rate_value = "buyer-bad-rate";
            } else if(real_rate == 0) {
                rate_value = "buyer-delete-rate";
            }
            return '<span class="rate-flower-img '+rate_value+'" style="margin:auto;display:inline-block;*display:inline;*zoom: 1;"></span>';
            //return '<a class="rate-flower-img '+rate_value+'" style="margin:auto;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</a>';
        },
        showRateResult : function(data){
            if(data.message == "content is empty"){
                TM.Alert.load("评价内容为空，请修改后重试");
            } else if(data.message == "no trade to rate"){
                 TM.Alert.load("没有可评价的订单");
             } else if(data.res.length == 0) {
                 TM.Alert.load("没有可评价的订单");
             }else {
                 var html = '';
                 // successNum and failNum
                 html += '<div style="text-align: center;width: 100%;height: 30px;line-height: 3px;font-family:微软雅黑;font-size: 24px;margin-top: 15px;">评价成功订单数:<span style="font-size: 30px;color: red;margin-right: 50px;">'+data.successNum+'</span>评价失败订单数<span style="font-size: 30px;color: red;">'+data.failNum+'</span></div>';
                 // fail trade table
                 html += '<table class="rateResultTable" style="width: 100%;word-wrap: break-word;word-break: break-all;">' +
                     '<thead>' +
                        '<tr>'+
                            '<th style="width: 95px;">主订单号</th>'+
                            '<th style="width: 95px;">子订单号</th>'+
                            '<th style="width: 90px;">买家昵称</th>'+
                            '<th style="width: 40px;">评价</th>'+
                            '<th style="width: 445px;">评价内容</th>'+
                            '<th style="width: 160px;">失败原因</th>'+
                        '</tr>'+
                     '</thead>'+
                     '<tbody>';

                 $(data.res).each(function(i,failOrder){
                     var rate = TM.CommentNormal.Util.getRateResult(failOrder.result);
                     html += '<tr>' +
                         '<td>'+failOrder.tid+'</td>'+
                         '<td>'+failOrder.oid+'</td>'+
                         '<td>'+failOrder.buyerNick+'</td>'+
                         '<td>'+TM.CommentNormal.Util.createRateImg(rate)+'</td>'+
                         '<td>'+failOrder.content+'</td>'+
                         '<td>'+failOrder.reason+'</td>'+
                         '</tr>';
                 });
                 html += '</tbody>'+
                     '</table>';
                 TM.Alert.loadDetail(html, 1000, 600, null,"评价失败列表");
             }
        },
        getRateResult : function(result){
            if(result == 'good'){
                rate = 1;
            } else if (result == "netural") {
                rate = 2;
            } else {
                rate = 3;
            }
            return rate;
        },
        sendMsg:function(){
            //发送短信，待实现
        }
    },CommentNormal.Util);
})(jQuery,window));