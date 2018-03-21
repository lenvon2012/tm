var xyywapdesc = cookie('xyywapdesc');
var xyydata = cookie('xyywapdesc_item');
var itemId = 0;
var sellerId = 0;
var installDelay = 2;
var pageDelay = 1;

var sum = "dshfwewfsfsfwjlj131jdaow~~**^%&";
if (xyywapdesc && xyydata) {
    xyywapdesc = JSON.parse(xyywapdesc);
    xyydata = JSON.parse(xyydata);
    itemId = xyydata.itemId;
    sellerId = xyydata.sellerId;
    installDelay = xyywapdesc.installDelay;
    pageDelay = xyywapdesc.pageDelay;
    setTimeout(edit, parseInt(installDelay) * 1000);
} else {
}
function cookie(name) {
    var cookieArray = document.cookie.split("; ");
    var cookie = new Object();
    for (var i = 0; i < cookieArray.length; i++) {
        var arr = cookieArray[i].split("=");
        if (arr[0] == name)return unescape(arr[1])
    }
    return""
}
function setcookie(name, value) {
    var Days = 30;
    var exp = new Date();
    exp.setTime(exp.getTime() + Days * 24 * 60 * 60 * 1000);
    document.cookie = name + "=" + escape(value) + ";path=/;domain=.taobao.com;expires=" + exp.toGMTString()
}
function getAndSaveWapDesc() {
    //修改手机详情页参数
    $(".import-detail").trigger("click");

    //点一个按钮等待一秒
    setTimeout(function(){
        $("#J_MobileEditor").trigger("click");
    },300);

    //点一个按钮等待一秒
    setTimeout(function(){
        $(".btn-build-mdetail").trigger("click");
    },700);
    var _delay = (parseInt(installDelay) + 1) * 1000;
    setTimeout(function(){
        $(".J_Submit").trigger("click");
    }, _delay);
}
function edit() {
    var error = document.getElementById("J_PageFeedback");
    var match = window.location.href.match(/upload\.taobao\.com.*?item_num_id=(\d+)/);
    if (error && match != null) {
        location.href = "http://item.taobao.com/item.htm?id=" + itemId
    }
    if (document.getElementById('J_MobileDetail') == null) {
        location.href = "http://item.taobao.com/item.htm?id=" + itemId
    }
    var result = document.getElementById('J_MobileDetail').value ? JSON.parse(document.getElementById('J_MobileDetail').value) : {data: []};
    //if (coverType == 1) {
    //    if (result.data.length > 0) {
    //        location.href = "http://item.taobao.com/item.htm?id=" + itemId
    //    }
    //}
    getAndSaveWapDesc();
    setcookie("xyywapdesc_item", "")
}
