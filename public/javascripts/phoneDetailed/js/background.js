var TM = TM || {};

TM.serverPath="http://t.taovgo.com";
//TM.serverPath="http://x.tobti.com:9000";
//TM._tms="?_tms=61016189297fadfd1d33c36be11abc00af4928b89732a1579742176";
TM._tms="";

//待处理的商品ID数组
var itemIds = [];
//当前处理等的商品ID
var itemId = 0;
//处理商品的tab ID
var tabid = -1;
//用来通知content页面是否进行更新操作的状态
var status = 0;
//定时器对象
var checkTimer;
//待生成的商品总数
var total = 0;
//已完成的商品数量
var progress = 0;
//已跳过的商品数量
var skiped = 0;
//获取的数据集合大小
var pageSize = 10;

//获取的page number
var pageNumber = 1;

//是否需要跳过
var needSkip = false;
var sum = "dshfwewfsfsfwjlj131jdaow~~**^%&";

var hidestopcancel = false;

Background = (function () {
    var sellerId = 0;
    var igroneError = true;

    function start() {
        hidestopcancel = true;
        tabid = -1;
        status = 1;
        progress = 0;
        pageHandler(false);
    }

    function stop() {
        initVariable();
    }

    function skip() {
        if (tabid != -1) {
            chrome.tabs.get(tabid, function (t) {
                var mat = t.url.match(/upload\.taobao\.com.*?item_num_id=(\d+)/);
                if (mat) {
                    needSkip = true
                }
            })
        }
    }

    function pageHandler(getSize) {
        console.log("pageHandler", "getsize" , getSize);
        //这里每次向数据库中发送请求时，都将page number 加1
        $.post(TM.serverPath+'/PhoneDetaileds/getDeliveryTemplates?sellerId=' + sellerId + '&pn='+1+'&ps=' + pageSize , function (data) {
            console.log(data);
            //这里要将返回的数据放入到一个待处理的数组列表中
            if (data.isOk) {

                page = data.pn;
                total = data.count;
                if (getSize) {
                    report();
                    return
                }
                if (total == 0) {
                    stop();
                    return
                }
                itemIds = data.res;

                if (itemIds.length) {
                    console.log("before publish");
                    Publisher.publish()
                } else {
                    stop()
                }
            }
        });

    }

    return{start: function () {
        start()
    }, stop: function () {
        stop()
    }, skip: function () {
        skip()
    }, cancel: function () {
        cancel();
    }, pageHandler: function (getSize) {
        pageHandler(getSize)
    }, setSellerId: function (_sellerId) {
        sellerId = _sellerId
    }, getSellerId: function () {
        return sellerId
    }, setIgroneError: function (_igroneError) {
        igroneError = _igroneError
    }, getIgroneError: function () {
        return igroneError
    }}
})();
Publisher = (function () {
    var checkTime = 0;

    function check() {
        checkTime++;
        if (needSkip || (Background.getIgroneError() && checkTime >= 25)) {
            //更新状态为失败
            $.post(TM.serverPath+'/PhoneDetaileds/chengeStatus?sellerId=' + Background.getSellerId() + '&itemId=' + itemId + "&status=Failure", function (data) {
            });
            needSkip = false;
            skiped++;
            total--;
            report();
            publish();
            return
        }
        if (status == 0) {
            report();
            return
        }
        chrome.tabs.get(tabid, function (t) {
            var mat = t.url.match(/item\.taobao\.com.*?id=(\d+)&?/);
            if (mat == null) {
                mat = t.url.match(/2\.taobao\.com.*?id=(\d+)&?/)
            }
            if (mat != null) {
                //提交成功了，把这个记录标记为已经成功
                $.post(TM.serverPath+'/PhoneDetaileds/chengeStatus?sellerId=' + Background.getSellerId() + '&itemId=' + itemId + "&status=Success" , function (data) {
                    progress++;
                    total--;
                    report();
                    publish()
                })
            } else {
                checkTimer = setTimeout(check, 1000)
            }
        })
    }

    function publish() {
        //如果用户没有点击开始，就不再往后运行 了
        if (status == 0) {
            return
        }
        //如果待处理列表为空，就直接去获取
        if (itemIds.length == 0) {
            Background.pageHandler(false);
            return
        }
        console.log("publish", status, itemIds);
        itemId = itemIds.pop();
        setItemCookie(itemId);
        //是开启一个新窗口处理还是覆盖当前窗口
        if (tabid == -1) {
            chrome.tabs.create({
                url: "http://upload.taobao.com/auction/publish/edit.htm?auto=false&item_num_id=" + itemId,
                "selected": false,
                "pinned": true,
                "index": 0
            }, function (tab) {
                tabid = tab.id;
                checkTime = 0;
                check()
            })
        } else {
            chrome.tabs.update(tabid, {url: "http://upload.taobao.com/auction/publish/edit.htm?auto=false&item_num_id=" + itemId}, function (tab) {
                tabid = tab.id;
                checkTime = 0;
                check()
            })
        }
    }

    return{publish: function () {
        publish()
    }}
})();
function report() {
    chrome.extension.sendMessage({action: 'report', progress: progress, skiped: skiped, total: total, status: status});
    if(total == 0) {
        chrome.browserAction.setBadgeBackgroundColor({color: '#33CC33'});
        chrome.browserAction.setBadgeText({text: ' √ '});
        initVariable();
        if("Notification" in window) {
            var notification = new Notification("", {
                body: "手机详情页生成成功",
                icon: "../img/1.png"
            });
        }
    }
}
function setItemCookie(itemId) {
    var conf = {};
    conf.itemId = itemId;
    conf.sellerId = Background.getSellerId();
    chrome.cookies.set({url: 'http://*.taobao.com/', name: 'xyywapdesc_item', value: JSON.stringify(conf), domain: '.taobao.com', expirationDate: 30 * 24 * 60 * 60 * 1000})
}

/**
 * 初始化全局参数
 */
function initVariable() {
    if(tabid !== -1) {
        chrome.tabs.remove(tabid);
    }
    progress = 0;
    skiped = 0;
    status = 0;
    hidestopcancel = false;
    itemIds = [];
    itemId = 0;
    tabid = -1;
    total = 0;
    pageSize = 10;
    pageNumber = 1;
    needSkip = false;
}
