var TM = TM || {};
((function ($, window) {
    TM.CommentNoticeLog = TM.CommentNoticeLog || {};

    var CommentNoticeLog = TM.CommentNoticeLog;

    /**
     * 初始化
     * @type {*}
     */
    CommentNoticeLog.init = CommentNoticeLog.init || {};
    CommentNoticeLog.init = $.extend({
        doInit: function(container) {
//            var html = CommentNoticeLog.init.createHtml();
//            container.html(html);
            CommentNoticeLog.container = container;

            container.find(".search-btn").click(function() {
                CommentNoticeLog.show.doShow();
            });
            container.find(".trade-id-text").keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    container.find(".search-btn").click();
                }
            });
            
            container.find(".buyer-name-text").keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    container.find(".search-btn").click();
                }
            });
//            container.find(".item-title-text").keydown(function(event) {
//                if (event.keyCode == 13) {//按回车
//                    container.find(".search-btn").click();
//                }
//            });
            container.find(".date-picker").datepicker();
            CommentNoticeLog.show.doShow();
        }
    }, CommentNoticeLog.init);

    CommentNoticeLog.show = CommentNoticeLog.show || {};
    CommentNoticeLog.show = $.extend({
        doShow: function() {
            var ruleData = CommentNoticeLog.show.getQueryRule();
            var tbodyObj = CommentNoticeLog.container.find(".skincomment-table").find("tbody");
            //tbodyObj.html("");
            CommentNoticeLog.container.find(".paging-div").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: ruleData,
                    dataType: 'json',
                    url: '/SkinComment/findSmsLog',
                    callback: function(dataJson){
                        tbodyObj.html("");
                        var logItemJsonArray = dataJson.res;
                        if (logItemJsonArray === undefined || logItemJsonArray == null || logItemJsonArray.length == 0) {
                        	tbodyObj.html('<tr><td colspan="6" style="height:40px;">亲，暂无日志记录哦</td></tr>');
                        	return;
                        }
                        $(logItemJsonArray).each(function(index, logItemJson) {
                            var trObj = CommentNoticeLog.row.createRow(index, logItemJson);
                            tbodyObj.append(trObj);
                        });
                    }
                }

            });
        },
        refresh: function() {
            CommentNoticeLog.show.doShow();
        },
        getQueryRule: function() {
            var ruleData = {};
//            var tradeId = CommentNoticeLog.container.find(".trade-id-text").val();
//            var title = CommentNoticeLog.container.find(".item-title-text").val();
            var buyerNick = CommentNoticeLog.container.find(".buyer-name-text").val();
            var startTime = CommentNoticeLog.container.find(".start-time-text").val();
            var endTime = CommentNoticeLog.container.find(".end-time-text").val();
//            ruleData.tradeId = tradeId;
//            ruleData.title = title;
            ruleData.buyerNick = buyerNick;
            ruleData.startTime = startTime;
            ruleData.endTime = endTime;
            return ruleData;
        }
    }, CommentNoticeLog.show);


    CommentNoticeLog.row = CommentNoticeLog.row || {};
    CommentNoticeLog.row = $.extend({
        createRow: function(index, logItemJson) {
            var itemJson = logItemJson;
            if (itemJson === undefined || itemJson == null) {
            	itemJson = {};
            }
            
            var html = '' +
                '<tr>' +
//                '	<td><input type="checkbox" name=""></td>' +
//                '   <td><span class="id"></span> </td>' +
                //'   <td><a target="_blank" class="item-link"><img class="item-img" /> </a> </td>' +
                //'   <td><a target="_blank" class="item-link"><span class="item-title"></span> </a> </td>' +
                //'   <td><span class="item-price"></span> </td>' +
                //'	<td><span class="comment-end-time"></span></td>' +
                '   <td><span class="buyer-nick"></span> </td>' +
                '   <td><span class="buyer-mobile"></span> </td>' +
                '   <td><span class="content"></span> </td>' +
                '	<td><span class="created"></span></td>' +
                '	<td><span class="type"></span></td>' +
                '	<td><span class="status"></span></td>' +
                '</tr>' +
                '';
            var trObj = $(html);
            trObj.find(".id").html(itemJson.id);
            trObj.find(".total-fee").html(itemJson.totalFee);
            trObj.find(".buyer-nick").html(itemJson.nick + '<a target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid='+itemJson.nick+'&siteid=cntaobao&status=2&charset=utf-8" ><img border="0" style="width:16px;height:16px;" src="http://amos.alicdn.com/online.aw?v=2&uid='+encodeURIComponent(itemJson.nick)+'&site=cntaobao&s=2&charset=utf-8" alt="点击这里给他发消息" /></a>');
            trObj.find(".buyer-mobile").html(itemJson.phone);
            trObj.find(".content").html(itemJson.content);
            
            var type = itemJson.type;
            if (type == 16) {
            	trObj.find(".type").html("拦截成功");
            } else if (type == 32) {
            	trObj.find(".type").html("拦截失败");
            } else if (type == 64) {
            	trObj.find(".type").html("拦截通知");
            } else if (type == 128) {
            	trObj.find(".type").html("差评通知");
            } else if (type == 256) {
            	trObj.find(".type").html("通知买家");
            } else if (type == 512) {
            	trObj.find(".type").html("免费提醒");
            }
            
            var status = itemJson.success;
            var statusMsg = "";
            if (status == 1) {
            	statusMsg = "发送成功";
            	trObj.find(".status").css("color", "green");
            } else if(status == 0) {
            	statusMsg = "发送失败";
            	trObj.find(".status").css("color", "#a10000");
            }
            trObj.find(".status").html(statusMsg);
            
            var created = CommentNoticeLog.row.getTimeStr(itemJson.addAt);
            trObj.find(".created").html(created);

            return trObj;
        },
        getTimeStr: function(ts) {
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
    }, CommentNoticeLog.row);



})(jQuery,window));