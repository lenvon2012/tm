
var TM = TM || {};
((function ($, window) {
    TM.DefenseBlacklist = TM.DefenseBlacklist || {};

    var DefenseBlacklist = TM.DefenseBlacklist;

    /**
     * 初始化
     * @type {*}
     */
    DefenseBlacklist.init = DefenseBlacklist.init || {};
    DefenseBlacklist.init = $.extend({
        doInit: function(container) {
//            var html = DefenseBlacklist.init.createHtml();
//            container.html(html);
            DefenseBlacklist.container = container;

            container.find(".search-btn").click(function() {
                DefenseBlacklist.show.doShow();
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
            DefenseBlacklist.show.doShow();
        }
    }, DefenseBlacklist.init);

    DefenseBlacklist.show = DefenseBlacklist.show || {};
    DefenseBlacklist.show = $.extend({
        doShow: function() {
            var ruleData = DefenseBlacklist.show.getQueryRule();
            var tbodyObj = DefenseBlacklist.container.find(".skincomment-table").find("tbody");
            //tbodyObj.html("");
            DefenseBlacklist.container.find(".paging-div").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: ruleData,
                    dataType: 'json',
                    url: '/SkinDefender/queryDefenseBlacklists',
                    callback: function(dataJson){
                        tbodyObj.html("");
                        var logItemJsonArray = dataJson.res;
                        if (logItemJsonArray === undefined || logItemJsonArray == null || logItemJsonArray.length == 0) {
                        	tbodyObj.html('<tr><td colspan="7" style="height:40px;">亲，暂无订单记录哦</td></tr>');
                        	return;
                        }
                        $(logItemJsonArray).each(function(index, logItemJson) {
                            var trObj = DefenseBlacklist.row.createRow(index, logItemJson);
                            tbodyObj.append(trObj);
                        });
                    }
                }

            });
        },
        refresh: function() {
            DefenseBlacklist.show.doShow();
        },
        getQueryRule: function() {
            var ruleData = {};
            var tradeId = DefenseBlacklist.container.find(".trade-id-text").val();
            var title = DefenseBlacklist.container.find(".item-title-text").val();
            var buyerName = DefenseBlacklist.container.find(".buyer-name-text").val();
            var startTime = DefenseBlacklist.container.find(".start-time-text").val();
            var endTime = DefenseBlacklist.container.find(".end-time-text").val();
            ruleData.tradeId = tradeId;
            ruleData.title = title;
            ruleData.buyerName = buyerName;
            ruleData.startTime = startTime;
            ruleData.endTime = endTime;
            return ruleData;
        }
    }, DefenseBlacklist.show);


    DefenseBlacklist.row = DefenseBlacklist.row || {};
    DefenseBlacklist.row = $.extend({
        createRow: function(index, logItemJson) {
            var itemJson = logItemJson;
            if (itemJson === undefined || itemJson == null) {
            	itemJson = {};
            }
            
            var html = '' +
                '<tr>' +
//                '	<td><input type="checkbox" name=""></td>' +
                '   <td><span class="trade-id"></span> </td>' +
                //'   <td><a target="_blank" class="item-link"><img class="item-img" /> </a> </td>' +
                //'   <td><a target="_blank" class="item-link"><span class="item-title"></span> </a> </td>' +
                //'   <td><span class="item-price"></span> </td>' +
                '	<td><span class="created"></span></td>' +
                '	<td><span class="pay-time"></span></td>' +
                //'	<td><span class="comment-end-time"></span></td>' +
                '   <td><span class="buyer-nick"></span> </td>' +
                '   <td><span class="buyer-mobile"></span> </td>' +
                '   <td><span class="total-fee"></span> </td>' +
                '	<td><span class="status"></span></td>' +
                '</tr>' +
                '';
            var trObj = $(html);
//            trObj.find(".trade-id").html(itemJson.tid);
            trObj.find(".trade-id").html('<a href="http://trade.taobao.com/trade/detail/trade_item_detail.htm?bizOrderId='+itemJson.tid +'" target="_blank">'+itemJson.tid+'</a>');
            trObj.find(".total-fee").html(itemJson.totalFee);
            trObj.find(".buyer-nick").html(itemJson.buyerNick + '<a target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid='+
            		encodeURI(itemJson.buyerNick)+'&siteid=cntaobao&status=2&charset=utf-8" ><img class="wwimg" border="0" src="http://amos.alicdn.com/online.aw?v=2&uid='+
            		encodeURI(itemJson.buyerNick)+'&site=cntaobao&s=2&charset=utf-8" alt="点击这里给他发消息" /></a>');
            trObj.find(".buyer-mobile").html(itemJson.receiverMobile);
            
            var status = itemJson.status;
            var statusMsg = "";
            if (status == 0) {
            	statusMsg = "没有创建支付宝交易";
// trObj.find(".status").css("color", "green");
            } else if(status == 1) {
            	statusMsg = "等待买家付款";
// trObj.find(".status").css("color", "#a10000");
            } else if(status == 2) {
            	statusMsg = "买家已付款";
            } else if(status == 3) {
            	statusMsg = "等待买家确认收货";
            } else if(status == 4) {
            	statusMsg = "买家已签收";
            } else if(status == 5) {
            	statusMsg = "交易成功";
            } else if(status == 6) {
            	statusMsg = "用户退款成功";
            } else if(status == 7) {
            	statusMsg = "关闭交易";
            } else if(status == 1) {
            	statusMsg = "";
            }
            
            trObj.find(".status").html(statusMsg);
            
            var created = DefenseBlacklist.row.getTimeStr(itemJson.created);
            trObj.find(".created").html(created);
            if (status >= 2) {
            	var payTime = DefenseBlacklist.row.getTimeStr(itemJson.payTime);
            	trObj.find(".pay-time").html(payTime);
            } else {
            	trObj.find(".pay-time").html("未支付");
            }

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
    }, DefenseBlacklist.row);



})(jQuery,window));