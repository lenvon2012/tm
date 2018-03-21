var url = "//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js";
    // 创建script标签，设置其属性
    var script = document.createElement('script');
    script.setAttribute('src', url);
    // 把script标签加入head，此时调用开始
    document.getElementsByTagName('head')[0].appendChild(script);

var timeStr = '2013-07-21';
var price = '2';
$.ajax({
	"url":"http://zuanshi.taobao.com/whiteBidCpmTrans/createWhiteBidCpmTrans.json",
	"type":"post",
	"data":{
		"adboards[0].adboardid":'191729940001',
		"cpmtrans.adzoneId":'13276784',
		"cpmtrans.begintime":timeStr,
		"cpmtrans.daybudget":'200',
		"cpmtrans.endtime":timeStr,
		"cpmtrans.hourspan":'0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23',
		"cpmtrans.transname":'推广标题',
		"cpmtrans.unitprice":price,
		"csrfID":UserInfo.csrfID
	},
	"success":function(response){

	},
	"error":function(req,errorText){

	}
});
