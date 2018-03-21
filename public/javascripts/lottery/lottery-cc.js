/*淘掌柜 - 抽奖转盘 - 版权所有，盗版必究*/
function loadData() {
	$.get("/lottery/latest",function(data) {
			var record = $("#reward-record");
			record.html("");
            $(data).each(function(index, one) {
                var li = $("<li>" + one.nick + "   <b>" + one.prize + "</b>   " + createTimeStr(one.ts) + "</li>");
                record.append(li);
            });
		}
	);
	
	$.get("/lottery/my", function(data) {
			var record = $("#myreward-record");
			record.html("");
            $(data.res).each(function(index, one) {
                var li = $("<li>" + createTimeStr(one.ts)  + "   <b>" + one.prize + "</b>   <a href='" + one.rewarUrl + "' target='_blank'>立即领奖</a></li>");
                record.append(li);
            });
		}
	);
}

function createTimeStr(ts) {
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
    var timeStr = year + "-" + month+"-"+date+" "+hour+":"+minutes+ ":" + second;
    return timeStr;
}

function lottery() {
	$("#startbtn").unbind('click').css("cursor","default");
    $("#startbtn").click(function() {
        lottery();
    });
	
	$.ajax({
		type : 'POST',
		url : '/lottery/go',
		dataType : 'json',
		cache : false,
		error : function() {
			alert('出错了！');
			return false;
		},
		success : function(data) {
			lotteryPlay(data);
		}
	});
	
}

function lotteryPlay(data) {

    var error = data.error;
    if (error == 1) {
        //alert("亲，您今天的抽奖机会已经用完，请明天再来哦~");
        createDialogCommon("明天再来哦~", "亲，您今天的抽奖机会已经用完，请明天再来哦~");
        return;
    }
    var a = data.angle;
    var p = data.prize;
    var url = data.url;
    var id = data.id;
    $("#startbtn").rotate({
        duration : 3000,
        angle : 0,
        animateTo : 1800 + a,
        easing : $.easing.easeOutSine,
        callback : function() {
        	if (p=="话费1元" || p=="彩票1注") {
        		createDialogWangwang(p, id);
        		return;
        	}
        	if (p=="再来一次" || p=="赠送短信1条" || p=="赠送短信5条") {
        		createDialogCommon(p, "恭喜您，获得了【<font color='red'><b>" + p + "</b></font>】奖励，确认直接获取。");
        		return;
        	}
        	if (p=="未中奖" || p=="未开始") {
        		if (p=="未中奖") {
            		info = "亲，抱歉未中奖，明天再来哦~";
            	} else if(p=="未开始"){
            		info = "亲，抽奖中心8月8日正式开通，记得来哦~";
            	}
            	createDialogCommon(p, info);
            	return;
        	}
        	
            //loadData();
//            alert('create dialog');
            createDialog(p, url);
//            var con = confirm('恭喜你，获得【' + p + '】奖励！按确认领取奖品。');
//            if (con) {
//                window.location.href = url;
//            } else {
//                return false;
//            }
        }
    });
}

function createDialog(p, url) {
	var dialogObj = $(".dialog-div");
	var info = "恭喜您，获得【<font color='red'><b>" + p + "</b></font>】奖励！";
	
    if (dialogObj.length == 0) {
        var html = '' +
            '<div class="dialog-div" style="background:#fff;">' +
            '   <table style="margin: 0 auto;">' +
            '       <tbody>' +
            '       <tr>' +
            '           <td>'+info+'</td>' +
            '       </tr>' +
            '       </tbody>' +
            '   </table>' +
            '</div> ' +
            '';

        dialogObj = $(html);
        $("body").append(dialogObj);
        
        if(p == "未中奖") {
        	dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:180,
                width:350,
                title: "未中奖，下次再来哦~",
                autoOpen: false,
                resizable: false,
                buttons:{'关闭':function(){
                    $(this).dialog('close');
                    window.location.reload(); 
                }}
            });
        } else {
            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:180,
                width:350,
                title: "恭喜您，中奖啦！",
                autoOpen: false,
                resizable: false,
                buttons:{'领取奖励':function() {
                    $(this).dialog('close');
                    //window.location.href = url;
                    var win=window.open(url, '_blank');
                    win.focus();
                    window.location.reload(); 
                },'放弃奖品':function(){
                    $(this).dialog('close');
                    window.location.reload(); 
                }}
            });
        }
    }
    dialogObj.dialog('open');
    return false;
}

function createDialogWangwang(p, id) {

	var dialogObj = $(".dialog-div");
	var info = "恭喜您，获得【<font color='red'><b>" + p + "</b></font>】奖励！";
	var url = "http://amos.im.alisoft.com/msg.aw?v=2&amp;uid=%E9%AA%91%E7%9D%80%E7%BB%B5%E7%BE%8A%E9%A3%9E:%E9%A3%98%E9%9B%AA&amp;site=cntaobao&amp;s=1&amp;charset=utf-8";
	if (p=="未中奖") {
		info = "亲，抱歉未中奖，明天再来哦~";
	} else if(p=="未开始"){
		info = "亲，抽奖中心8月8日正式开通，记得来哦~";
	} else if (p=="彩票1注" || p=="话费1元") {
		info += '<br>请填写以下信息并提交，我们会在24小时内给亲奖励。<br><br>旺旺：<input type="text" name="wangwang" id="wangwang" /><br>手机：<input type="text" name="mobile" id="mobile" />';
		info += '<br><br>有问题请联系客服：<a target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid=%E9%AA%91%E7%9D%80%E7%BB%B5%E7%BE%8A%E9%A3%9E%3A%E9%A3%98%E9%9B%AA&siteid=cntaobao&status=1&charset=utf-8">' +
		'<img border="0" src="http://amos.alicdn.com/realonline.aw?v=2&uid=%E9%AA%91%E7%9D%80%E7%BB%B5%E7%BE%8A%E9%A3%9E%3A%E9%A3%98%E9%9B%AA&site=cntaobao&s=1&charset=utf-8" alt="联系售后"/></a>' +
        '&nbsp;&nbsp;<a target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid=%E9%AA%91%E7%9D%80%E7%BB%B5%E7%BE%8A%E9%A3%9E%3A%E5%B0%8F%E8%A8%80&siteid=cntaobao&status=1&charset=utf-8">' +
        '<img border="0" src="http://amos.alicdn.com/realonline.aw?v=2&uid=%E9%AA%91%E7%9D%80%E7%BB%B5%E7%BE%8A%E9%A3%9E%3A%E5%B0%8F%E8%A8%80&site=cntaobao&s=1&charset=utf-8" alt="联系售后"/></a>';
	}

    if (dialogObj.length == 0) {
        var html = '' +
            '<div class="dialog-div" style="background:#fff;">' +
            '   <table style="margin: 0 auto;">' +
            '       <tbody>' +
            '       <tr>' +
            '           <td>'+info+'</td>' +
            '       </tr>' +
            '       </tbody>' +
            '   </table>' +
            '</div> ' +
            '';

        dialogObj = $(html);
        $("body").append(dialogObj);

        dialogObj.dialog({
            modal: true,
            bgiframe: true,
            height:280,
            width:450,
            title: "恭喜您，中奖啦！",
            autoOpen: false,
            resizable: false,
            buttons:{'确认提交':function() {
                $(this).dialog('close');
                //window.location.href = url;
                //var win=window.open(url, '_blank');
                //win.focus();
                var wangwang = $("#wangwang").val();
                var mobile = $("#mobile").val();
                $.post("/lottery/post", {id:id, wangwang:wangwang, mobile:mobile}, function(data){window.location.reload();});
            },'放弃奖励':function(){
                $(this).dialog('close');
                window.location.reload();
            }}
        });
    }

    dialogObj.dialog('open');

    return false;
}

function createDialogCommon(title, info) {

	var dialogObj = $(".dialog-div");
	
    if (dialogObj.length == 0) {
        var html = '' +
            '<div class="dialog-div" style="background:#fff;">' +
            '   <table style="margin: 0 auto;">' +
            '       <tbody>' +
            '       <tr>' +
            '           <td>'+info+'</td>' +
            '       </tr>' +
            '       </tbody>' +
            '   </table>' +
            '</div> ' +
            '';

        dialogObj = $(html);
        $("body").append(dialogObj);

        dialogObj.dialog({
            modal: true,
            bgiframe: true,
            height:180,
            width:450,
            title: title,
            autoOpen: false,
            resizable: false,
            buttons:{'确定':function() {
                $(this).dialog('close');
                window.location.reload();
            }}
        });
    }

    dialogObj.dialog('open');

    return false;
}



$(document).ready(function(){
	// 先转一下，解决IE兼容问题
    $("#startbtn").rotate({
        duration : 3000,
        angle : 0,
        animateTo : 1,
        easing : $.easing.easeOutSine,
        callback : function() {
            
        }
    });
    $("#startbtn").click(function() {
        lottery();
    });
})
