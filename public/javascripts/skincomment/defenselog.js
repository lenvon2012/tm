var TM = TM || {};
((function ($, window) {
    TM.DefenseLog = TM.DefenseLog || {};

    var DefenseLog = TM.DefenseLog;

    /**
     * 初始化
     * @type {*}
     */
    DefenseLog.init = DefenseLog.init || {};
    DefenseLog.init = $.extend({
        doInit: function(container) {
            var html = DefenseLog.init.createHtml();
            container.html(html);
            DefenseLog.container = container;

            container.find(".search-btn").click(function() {
                DefenseLog.show.doShow();
            });
            container.find(".buyer-name-text").keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    container.find(".search-btn").click();
                }
            });
            container.find(".item-title-text").keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    container.find(".search-btn").click();
                }
            });
            
            container.find(".date-picker").datepicker();
            DefenseLog.show.doShow();
        },
        createHtml: function() {
            var html = '' +
                '<div class="log-search-div">' +
                '   <table style="width: 100%;">' +
                '       <tbody>' +
                '       <tr>' +
                '           <td style="">' +
                '               <span>交易编号：</span>' +
                '               <input type="text" class="trade-id-text" style="width: 60px;"/>' +
                '           </td>' +
                '           <td style=""><span>买家昵称：</span><input type="text" class="buyer-name-text" style="width: 60px;"/></td>' +
                '           <td style=""><span>拦截状态：</span><select class="defense-status"><option value="0">全部</option><option value="1">拦截成功</option><option value="2">拦截失败</option></select></td>' +
                //'           <td style="padding-left: 20px;"><span>宝贝标题：</span><input type="text" class="item-title-text" style="width: 200px;"/></td>' +
                '           <td style="">' +
                '               <span>发生日期：</span>' +
                '               <input type="text" class="start-time-text date-picker" style="width: 75px;"/>' +
                '               <span>-</span>' +
                '               <input type="text" class="end-time-text date-picker" style="width: 75px;"/>' +
                '           </td>' +
                '           <td style=""><span class="search-btn commbutton btntext4">搜索日志</span> </td>' +
                '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '</div>' +
                '<div class="log-table-div">' +
                '   <div class="paging-div"></div> ' +
                '   <table class="defenselog-table skincomment-table">' +
                '       <thead>' +
                '       <tr>' +
                '           <td style="width: 20%;">交易编号</td> ' +
                //'           <td style="width: 13%;">宝贝图片</td> ' +
                //'           <td style="width: 31%;">宝贝标题</td> ' +
                //'           <td style="width: 14%;">宝贝价格 (元)</td> ' +
                '           <td style="width: 20%;">买家昵称</td>' +
                '           <td style="width: 20%;">阻挡状态</td>' +
                '           <td style="width: 20%;">阻挡原因</td>' +
                '           <td style="width: 20%;">失败原因</td>' +
                '       </tr>' +
                '       </thead>' +
                '       <tbody></tbody>' +
                '   </table> ' +
                '   <div class="paging-div"></div> ' +
                '</div> ' +
                '';

            return html;

        }
    }, DefenseLog.init);

    DefenseLog.show = DefenseLog.show || {};
    DefenseLog.show = $.extend({
        doShow: function() {
            var ruleData = DefenseLog.show.getQueryRule();
            var tbodyObj = DefenseLog.container.find(".defenselog-table").find("tbody");
            //tbodyObj.html("");
            DefenseLog.container.find(".paging-div").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: ruleData,
                    dataType: 'json',
                    url: '/SkinDefender/queryDefenseLogs',
                    callback: function(dataJson){
                        tbodyObj.html("");
                        var logItemJsonArray = dataJson.res;
                        if (logItemJsonArray === undefined || logItemJsonArray == null || logItemJsonArray.length == 0) {
                        	tbodyObj.html('<tr><td colspan="5" style="height:40px;">亲，未查到相关拦截日志哦</td></tr>');
                        	return;
                        }
                        $(logItemJsonArray).each(function(index, logItemJson) {
                            var trObj = DefenseLog.row.createRow(index, logItemJson);
                            tbodyObj.append(trObj);
                        });
                    }
                }

            });
        },
        refresh: function() {
            DefenseLog.show.doShow();
        },
        getQueryRule: function() {
            var ruleData = {};
            var tradeId = DefenseLog.container.find(".trade-id-text").val();
//            var title = DefenseLog.container.find(".item-title-text").val();
            var buyerName = DefenseLog.container.find(".buyer-name-text").val();
            var startTime = DefenseLog.container.find(".start-time-text").val();
            var endTime = DefenseLog.container.find(".end-time-text").val();
            var defenseStatus = DefenseLog.container.find('.defense-status').val();
            ruleData.tradeId = tradeId;
//            ruleData.title = title;
            ruleData.buyerName = buyerName;
            ruleData.startTime = startTime;
            ruleData.endTime = endTime;
            ruleData.defenseStatus = defenseStatus;
            return ruleData;
        }
    }, DefenseLog.show);


    DefenseLog.row = DefenseLog.row || {};
    DefenseLog.row = $.extend({
        createRow: function(index, logItemJson) {
            var logJson = logItemJson.log;
            var itemJson = logItemJson.item;
            if (itemJson === undefined || itemJson == null)
                itemJson = {};
            var html = '' +
                '<tr>' +
                '   <td><span class="trade-id"></span> </br> <span class="log-ts"></span> </td>' +
                //'   <td><a target="_blank" class="item-link"><img class="item-img" /> </a> </td>' +
                //'   <td><a target="_blank" class="item-link"><span class="item-title"></span> </a> </td>' +
                //'   <td><span class="item-price"></span> </td>' +
                '   <td><span class="buyer-name"></span> </td>' +

                '   <td><span class="op-status"></span> </td>' +
                '   <td><span class="op-time"></span> </td>' +            //  拦截原因
                '   <td><span class="op-msg"></span> </td>' +             // 失败原因
                '</tr>' +
                '';
            var trObj = $(html);
            trObj.find(".trade-id").html('<a href="http://trade.taobao.com/trade/detail/trade_item_detail.htm?bizOrderId='+logJson.tradeId +'" target="_blank">'+logJson.tradeId+'</a>');
            trObj.find(".log-ts").html(DefenseLog.util.parseLongToDate(logJson.ts, false));
            //var numIid = itemJson.numIid;
            //var url = "http://item.taobao.com/item.htm?id=" + numIid;
            //trObj.find(".item-link").attr("href", url);
            //trObj.find(".item-img").attr("src", itemJson.picURL);
            //trObj.find(".item-title").html(itemJson.title);
            //trObj.find(".item-price").html(itemJson.price);
            trObj.find(".buyer-name").html(logJson.buyerName + '<a target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid='+
            		encodeURI(logJson.buyerName)+'&siteid=cntaobao&status=2&charset=utf-8" ><img  class="wwimg" border="0" src="http://amos.alicdn.com/online.aw?v=2&uid='+
            		encodeURI(logJson.buyerName)+'&site=cntaobao&s=2&charset=utf-8" alt="点击这里给他发消息" /></a>');
            
//            trObj.find(".op-msg").html(logJson.opMsg);
            var status = logJson.status;
            if ((status & 1) > 0) {
                trObj.find(".op-status").html("交易关闭成功");
                trObj.find(".op-status").css("color", "green");
                trObj.find('.op-msg').text("~");
            } else {
                trObj.find(".op-status").css("color", "#a10000");
//                var opMsg = logJson.opMsg;
                var statusHtml = "交易关闭失败";
//                if (opMsg === undefined || opMsg == null || opMsg == "") {
//                    statusHtml = "交易关闭失败";
//                } else {
//                    statusHtml = "交易关闭失败";
//                }
                trObj.find(".op-status").html(statusHtml);
                trObj.find('.op-msg').text(logJson.closeFailReason);
            }

            trObj.find(".op-time").html(logJson.opMsg);

            return trObj;
        }
    }, DefenseLog.row);
    
    
	DefenseLog.util = DefenseLog.util || {};
	DefenseLog.util = $.extend({
		parseLongToDate : function(ts, isShort) {
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
			
			var timeStr = year+"-"+month+"-"+date+" "+hour+":"+minutes+":"+second;
			
			if(isShort) {
				timeStr = year+"-"+month+"-"+date;
			}
			return timeStr;
		}
	}, DefenseLog.util);



})(jQuery,window));