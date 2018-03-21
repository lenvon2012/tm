
    
var Monitor = Monitor || {};
//缓存的变量
Monitor.vars = Monitor.vars || {};

Monitor.init = Monitor.init || {};

Monitor.init = $.extend({

    doInit: function() {
        Monitor.init.getMonitorResult(true);
        $(".refreshBtn").click(function() {
            Monitor.init.getMonitorResult(true);
        });
        MonitorRefresh.init.doInit();
    },
    getMonitorResult: function(isShowLoading) {
        Monitor.util.showLoading(isShowLoading);//等待动画
        var data = {};
        $.ajax({
            url: '/monitor/getMonitorResult',
            dataType: 'json',
            type: 'post',
            data: data,
            error: function() {
                //alert("系统发生异常，请刷新重试");
                return;
            },
            success: function (resultJson) {
                var visitorArray = resultJson.visitorArray;
                if (visitorArray === undefined || visitorArray == null)
                    visitorArray = [];
                Monitor.vars.queryTime = resultJson.queryTime;
                var dateStr = Monitor.util.parseToStrTime(resultJson.queryTime);
                $(".monitorTime").html(dateStr);
                $(".onlineNum").html(visitorArray.length + "");
                $(".visitorListDiv ul").html("");
                //先根据时间排序
                visitorArray = Monitor.util.orderWithDesc(visitorArray, "lastTime");
                Visitors.init.doInit(visitorArray, isShowLoading);
                Monitor.util.hideLoading(isShowLoading);
            }
        });
    }
}, Monitor.init);


Monitor.util = Monitor.util || {};

Monitor.util = $.extend({
    //最后活动时间从大到小
    orderWithDesc: function(jsonArray, orderField) {
        var tempArray = [];
        //要先复制一份，不然在后面将数组元素置成null，会损坏jsonArray的结构的
        for (var i = 0; i < jsonArray.length; i++) {
            tempArray[i] = jsonArray[i];
        }
        var newArray = [];
        for (var i = 0; i < tempArray.length; i++) {
            var maxValue = -1;
            var maxIndex = -1;
            for (var j = tempArray.length - 1; j >= 0; j--) {
                if (tempArray[j] == null)
                    continue;
                if (tempArray[j][orderField] >= maxValue) {
                    maxValue = tempArray[j][orderField];
                    maxIndex = j;
                }
            }
            newArray[newArray.length] = tempArray[maxIndex];
            tempArray[maxIndex] = null;
        }
        return newArray;
    },
    //long时间，转换成2012-10-10 10:10:10类型
    parseToStrTime: function(longTime) {
        var theDate = new Date(longTime);
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

        var strTime = year+"-"+month+"-"+date+" "+hour+":"+minutes+":"+second;
        // alert(strTime);
        return strTime;
    },
    loadingNum: 0,
    showLoading: function(isShowLoading) {
        if (isShowLoading == false)
            return;
        if (Monitor.util.loadingNum <= 0) {
            Loading.init.show({});
            Monitor.util.loadingNum = 0;
        }
        Monitor.util.loadingNum++;

    },
    hideLoading: function(isShowLoading) {
        if (isShowLoading == false)
            return;
        Monitor.util.loadingNum--;
        if (Monitor.util.loadingNum <= 0) {
            Loading.init.hidden();
            Monitor.util.loadingNum = 0;
        }
    }
}, Monitor.util);

/**
 * 左边的访客列表
 * @type {*}
 */
var Visitors = Visitors || {};

Visitors.init = Visitors.init || {};

Visitors.init = $.extend({
    selectIp: '',
    doInit: function(visitorArray, isShowLoading) {
        $(".visitorListDiv").removeClass("scrollDiv");
        $(".visitorListDiv").addClass("noScrollDiv");
        if (visitorArray.length < 5) {
            $(".visitorListDiv").removeClass("scrollDiv");
            $(".visitorListDiv").addClass("noScrollDiv");
        } else {
            $(".visitorListDiv").removeClass("noScrollDiv");
            $(".visitorListDiv").addClass("scrollDiv");
        }
        $(visitorArray).each(function(index, visitorJson) {
            var visitorLi = Visitors.item.create(visitorJson);
            visitorLi.each(function() {
                this.visitorJson = visitorJson;
                this.index = index;
            });
            $(".visitorListDiv ul").append(visitorLi);
        });

        $(".visitorListDiv ul li").mouseover(function() {
            $(this).addClass("mouseOver");
        });

        $(".visitorListDiv ul li").mouseout(function() {
            $(this).removeClass("mouseOver");
        });

        $(".visitorListDiv ul li").each(function(index, liEle) {
            var liObj = $(liEle);
            var visitorJson = null;
            liObj.each(function() {
                visitorJson = this.visitorJson;
                this.index = index;
            });
            //点击事件
            liObj.click(function() {
                Visitors.init.selectUesr(liObj, true);
            });
        });
        if ($(".visitorListDiv ul li").length > 0) {
            var targetLiObj = null;
            if (Visitors.init.selectIp != "") {
                $(".visitorListDiv ul li").each(function() {
                    if (this.visitorJson.ip == Visitors.init.selectIp) {
                        targetLiObj = $(this);
                    }
                });
            }
            if (targetLiObj == null)
                targetLiObj = $($(".visitorListDiv ul li").get(0));
            Visitors.init.selectUesr(targetLiObj, isShowLoading);
            $(".noVisitor").hide();
            $(".hasVisitor").show();
        } else {
            $(".noVisitor").show();
            $(".hasVisitor").hide();
        }
    },
    selectUesr: function(liObj, isShowLoading) {
        var index = 0;
        var visitorJson = null;
        liObj.each(function() {
            visitorJson = this.visitorJson;
            index = this.index;
        });
        Visitors.init.selectIp = visitorJson.ip;
        if (liObj.hasClass("select"))
            return;
        $(".visitorListDiv ul li").removeClass("select");
        liObj.addClass("select");
        VisitorDetail.init.doInit(visitorJson, index, $(".visitorListDiv ul li"), isShowLoading);
    }
}, Visitors.init);

Visitors.item = Visitors.item || {};

Visitors.item = $.extend({
    create: function(visitorJson) {
        visitorJson = $.extend({
            location: '',
            startTime: 0
        }, visitorJson);
        var liHtml = "<li><div class='visitorItemDiv'><div class='visitorInfo  clearfix'>";
        liHtml += "<span><img /></span><span class='visitorLocation'></span></div><div class='visitorTime'>";
        liHtml += "</div></div></li>"
        var liObj = $(liHtml);
        liObj.find("img").attr("src", "/public/images/default-user.png");
        liObj.find(".visitorLocation").html(visitorJson.location);
        var strTime = Monitor.util.parseToStrTime(visitorJson.lastTime);
        liObj.find(".visitorTime").html("最近访问：" + strTime);

        return liObj;

    }
}, Visitors.item);


/**
 * 右边的访客详情
 * @type {*}
 */
var VisitorDetail = VisitorDetail || {};

VisitorDetail.init = VisitorDetail.init || {};

VisitorDetail.init = $.extend({
    //在这里，可以ajax得到访问的宝贝信息
    doInit: function(visitorJson, index, liObjs, isShowLoading) {
        Monitor.util.showLoading(isShowLoading);
        //取消按钮事件
        $(".visitorDetail .prevVisitor").unbind();
        $(".visitorDetail .nextVisitor").unbind();

        $(".visitorName").html("访客" + (index + 1));
        $(".visitorDetail .ipValue").html(visitorJson.ip);
        $(".visitorDetail .locationValue").html(visitorJson.location);
        var startTime = Monitor.util.parseToStrTime(visitorJson.startTime);
        var endTime = Monitor.util.parseToStrTime(Monitor.vars.queryTime);
        $(".visitorDetail .timeValue").html(startTime + " 至 " + endTime);

        var lastTime = Monitor.util.parseToStrTime(visitorJson.lastTime);
        $(".visitorDetail .lastTimeValue").html(lastTime);
        var queryTime = Monitor.vars.queryTime;
        var stayTime = (queryTime - visitorJson.startTime) / 1000;
        stayTime = Math.round(stayTime);
        $(".visitorDetail .stayTime").html(stayTime + "秒");

        VisitorDetail.init.setEvent(index, liObjs);

        VisitorDetail.list.showVisitItems(visitorJson, isShowLoading);
    },
    setEvent: function(index, liObjs) {
        //按钮事件
        if (index <= 0) {
            $(".visitorDetail .prevVisitor").removeClass("enableJumpBtn");
            $(".visitorDetail .prevVisitor").addClass("disableJumpBtn");
        } else {
            $(".visitorDetail .prevVisitor").removeClass("disableJumpBtn");
            $(".visitorDetail .prevVisitor").addClass("enableJumpBtn");
            $(".visitorDetail .prevVisitor").click(function() {
                $(liObjs.get(index - 1)).click();
            });
        }
        if (index >= liObjs.length - 1) {
            $(".visitorDetail .nextVisitor").removeClass("enableJumpBtn");
            $(".visitorDetail .nextVisitor").addClass("disableJumpBtn");
        } else {
            $(".visitorDetail .nextVisitor").removeClass("disableJumpBtn");
            $(".visitorDetail .nextVisitor").addClass("enableJumpBtn");
            $(".visitorDetail .nextVisitor").click(function() {
                $(liObjs.get(index + 1)).click();
            });
        }
    }
}, VisitorDetail.init);

VisitorDetail.list = VisitorDetail.list || {};

VisitorDetail.list = $.extend({
    showVisitItems: function(visitorJson, isShowLoading) {
        var trObjArray = visitorJson.trObjArray;
        var callback = function(trObjArray) {
            if (visitorJson.ip != Visitors.init.selectIp) {
            	Monitor.util.hideLoading(isShowLoading);
                return;
            }
            $(".visitorDiv table tbody").html("");
            $(".hasVisitor").addClass("detailHeight");
            visitorJson.trObjArray = trObjArray;//缓存下表格的行，这样重新选择用户时，就不用再ajax
            if (trObjArray.length < 10) {
                $(".hasVisitor").addClass("detailHeight");
            } else {
                $(".hasVisitor").removeClass("detailHeight");
            }
            $(trObjArray).each(function(index, trObj) {
                $(".visitorDiv table tbody").append(trObj);
            });
            Monitor.util.hideLoading(isShowLoading);
        };
        if (trObjArray === undefined || trObjArray == null) {
            VisitorDetail.list.doAjax(visitorJson.visitItemList, callback);
        } else {
            callback(trObjArray);
        }

    },
    //显示查看了哪些宝贝
    doAjax: function(visitItemList, callback) {

        //先截断
        visitItemList = VisitorDetail.util.checkVisitItemList(visitItemList);
        //先排序
        visitItemList = Monitor.util.orderWithDesc(visitItemList, "visitTime");
        var data = {};
        data.numIidArr = VisitorDetail.util.parseItemIdArr(visitItemList);
        $.ajax({
            url: '/items/getMonitorItems',
            dataType: 'json',
            type: 'post',
            data: data,
            error: function() {

            },
            success: function (itemArray) {
                var trObjArray = [];
                $(visitItemList).each(function(index, visitItem) {
                    var itemMap = VisitorDetail.util.getItemJson(visitItem.numIid, itemArray);
                    if (itemMap == null)
                        return;
                    var trObj = VisitorDetail.list.createRow(visitItem, itemMap);
                    //if (visitItemList.length > 5) {
                        if ((index + 1) % 2 == 1)
                            trObj.addClass("oddTr");
                        else
                            trObj.addClass("evenTr");
                    //}
                    trObjArray[trObjArray.length] = trObj;
                });
                callback(trObjArray);
            }
        });
    },
    createRow: function(visitItem, itemMap) {
        var trObj = $("<tr></tr>");
        var timeTd = $("<td></td>");
        var visitTime = Monitor.util.parseToStrTime(visitItem.visitTime);
        timeTd.html(visitTime);
        var itemTd = $("<td></td>");
        var aObj = $("<a target='_blank'></a>");
        aObj.attr("href", "http://item.taobao.com/item.htm?id=" + itemMap.id);
        aObj.html(itemMap.name);
        itemTd.append(aObj);
        trObj.append(timeTd);
        trObj.append(itemTd);
        return trObj;
    }
}, VisitorDetail.list);

VisitorDetail.util = VisitorDetail.util || {};

VisitorDetail.util = $.extend({
    //这个超过30个，后面的就不列了
    checkVisitItemList: function(visitItemList) {
        /*var newArray = [];
        if (visitItemList.length > 30) {
            for (var i = 0; i < 30; i++) {
                newArray[i] = visitItemList[i];
            }
            return newArray;
        } else
            return visitItemList;*/
        return visitItemList;

    },
    //获取宝贝id的集合，去掉重复的
    parseItemIdArr: function(visitItemList) {
        var itemIdArr = [];
        if (visitItemList === undefined || visitItemList == null || visitItemList.length == 0)
            return itemIdArr;
        for (var i = 0; i < visitItemList.length; i++) {
            var itemId = visitItemList[i].numIid;
            var j = 0;
            for (j = 0; j < itemIdArr.length; j++) {
                if (itemId == itemIdArr[j])
                    break;
            }
            if (j >= itemIdArr.length) {
                itemIdArr[itemIdArr.length] = itemId;
            }
        }
        return itemIdArr;
    },
    getItemJson: function(itemId, itemArray) {
        for (var i = 0; i < itemArray.length; i++) {
            var itemMap = itemArray[i];
            if (itemMap.id == itemId)
                return itemMap;
        }
        return null;
    }
}, VisitorDetail.util);

/**
 * 定时刷新
 * @type {*}
 */
var MonitorRefresh = MonitorRefresh || {};

MonitorRefresh.init = MonitorRefresh.init || {};

MonitorRefresh.init = $.extend({
    doInit: function() {
        var timer = setInterval(function() {
            Monitor.init.getMonitorResult(false);
        }, 10000);
    }
}, MonitorRefresh.init);

$(document).ready(function() {
    $(document).unbind();//去掉main.html中的ajaxStart
    Monitor.init.doInit();
});