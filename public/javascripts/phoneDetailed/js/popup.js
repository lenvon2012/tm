var sum = "dshfwewfsfsfwjlj131jdaow~~**^%&";
Popup = (function () {
    var bg = chrome.extension.getBackgroundPage();

    function start() {
        bg.Background.start()
    }

    function stop() {
        bg.Background.stop()
    }

    function skip() {
        bg.Background.skip()
    }

    function cancel() {
        var url = TM.serverPath+'/PhoneDetaileds/cancel';
        var data = {
            "sellerId" : ckvalue
        };
        $.post(url, data);
        back();
    }

    function back() {
        bg.Background.stop();
        $("#workspace").hide();
        $("#submit-data").show();
    }


    return {init: function () {
        chrome.browserAction.setBadgeText({text: ''});
        $("#start").click(function () {
            start();
            hidebackstop();
            $("#start").val("安装中...");
            $("#start").attr("disabled", "disabled")
        });
        $("#stop").click(function () {
            stop();
            showbackstop();
        });
        $("#skip").click(function () {
            skip()
        });
        $("#cancel").click(function () {
            cancel()
        });
        $("#back").click(function () {
            back()
        });
        $("#pageDelay").change(function () {
            saveConfig();
        });
        $("#installDelay").change(function () {
            saveConfig();
        });
        $("#igroneError").click(function () {
            bg.Background.setIgroneError($("#igroneError").is(':checked'));
            saveConfig()
        });
        $("#reload").click(function () {
            window.reload()
        });

        $(".goto-set-discount-btn,.item-list").click(function() {
           var data= TM.AddPromotionCommon.event.createPhones();
            //开始安装
            if(data==undefined||data==null){
                alert("生成手机详情页模板失败！请重试，如果问题一直存在，请联系我们");
            }else{
                if(data.successNum>0){
                    alert("本次成功生成"+data.successNum+" 个宝贝的手机详情页");
                }else{
                    alert("本次成功生成"+data.successNum+" 个宝贝的手机详情页，原因可能是系统过滤掉了3天内重复生成的宝贝，\n请去掉勾选的过滤3天内的手机详情页");
                }
                loadDeliveryTemplate(bg, ckvalue);
            }

        });

        if(bg.hidestopcancel) {
            hidebackstop();
        } else {
            showbackstop();
        }

        getCookie("_nk_", function (ck) {
            if (ck) {
                getCookie("unb", function (ck) {
                    if (ck) {
                        getCookie("_nk_", function (ck) {
                            if (ck) {
                                $("#sellerNick").html("当前账号：" + ascii2native(decodeURI(ck.value)));
                            }
                        });
                        ckvalue=ck.value;
                        loadDeliveryTemplate(bg, ck.value);
                    } else {
                        //没有登陆，显示登陆DIV
                        $("#login").show();
                        return;
                    }
                })
            }
        });
    }}
})();

//去加载待处理的模板,通过当前登陆的用户，如果登陆了，就显示处理DIV
function loadDeliveryTemplate(bg, sellerId) {
    //发送用户的ID 到后台，后台去判断这个用户有没有待处理的模板，如果没有就让用户去生成，有的话就显示处理页面
    $.post( TM.serverPath+'/PhoneDetaileds/getDeliveryTemplates?sellerId=' + sellerId + '&pn=1&ps=' + 10 , function (data) {
        if(data.isOk) {
            if (data.count > 0) {
                bg.Background.setSellerId(sellerId);
                bg.Background.pageHandler(true);
                loadCookie(bg);

                $("#login").hide();
                $("#submit-data").hide();
                $("#workspace").show();
            }else{
                $("#login").hide();
                $("#submit-data").show();
            }
        } else {
            $("#errorMsg").html(data.msg);
        }
    });
}
//设置提交手机详情页
function setCommitPhones(){
        $("#info-text").html('加载数据成功，请点击“开始安装”按钮开始安装...');
}

function loadCookie(bg) {
    getCookie("xyywapdesc", function (str) {
        if (str) {
            var conf = JSON.parse(str.value);
            if (conf) {
                $("#globalType").val(conf.globalType);
                if (conf.coverType)$("#coverType").val(conf.coverType);
                if (conf.deliveryTemplate)$("#deliveryTemplate").val(conf.deliveryTemplate);
                if (conf.installDelay)$("#installDelay").val(conf.installDelay);
                $("#igroneError").attr("checked", conf.igroneError);
                bg.Background.setIgroneError($("#igroneError").is(':checked'))
            }
        }
        saveConfig()
    })
}

function ascii2native(ascii) {
    var character = ascii.split("\\u");
    var native1 = character[0];
    for (var i = 1; i < character.length; i++) {
        var code = character[i];
        native1 += String.fromCharCode(parseInt("0x" + code.substring(0, 4)));
        if (code.length > 4) {
            native1 += code.substring(4, code.length)
        }
    }
    return native1
}
function getCookie(name, callback) {
    chrome.cookies.get({url: 'http://*.taobao.com/', name: name}, callback)
}
function saveConfig() {
    var conf = {};
    conf.installDelay = $("#installDelay").val();
    conf.pageDelay = $("#pageDelay").val();
    conf.igroneError = $("#igroneError").is(':checked');
    chrome.cookies.set({url: 'http://*.taobao.com/', name: 'xyywapdesc', value: JSON.stringify(conf), domain: '.taobao.com', expirationDate: 30 * 24 * 60 * 60 * 1000})
}
chrome.extension.onMessage.addListener(function (message, sender, sendResponse) {
    if (message.action == 'report') {
        $("#progress").html(message.progress);
        $("#total").html(message.total);
        $("#skiped").html(message.skiped);
        $(".state-text-progress").css("width", (100 - message.total * 100 / (message.total + message.progress + message.skiped)) + "%");

        //如果用户登录了，就去选择商品的页面

        if (message.total > 0) {
            $("#noitem").hide();
            $("#workspace").show();
            setCommitPhones();
        } else {
            $("#noitem").show();
            $("#workspace").hide()
        }
        if (message.status == 1) {
            $("#start").val("安装中...");
            $("#start").attr("disabled", "disabled")
        } else {
            $("#start").val("开始安装");
            $("#start").removeAttr("disabled")
        }
    }
});

function hidebackstop() {
    $("#back").hide();
    $("#cancel").hide();
}

function showbackstop() {
    $("#back").show();
    $("#cancel").show();
}

$(document).ready(function () {
    Popup.init();
});