var mallId,passId;
$(function() {
		$(document).ajaxStart(function(){
            TM.Loading.init.show();
        });
        $(document).ajaxStop(function(){
            TM.Loading.init.hidden();
        });

	 	
	 	$("#itemUrl").keyup(function(e){
            if(e.keyCode == 13)
             $("#doCopy").click();
        });
		
	})
	
	
	//进行拷贝操作
	function copyItem(){
		//获取宝贝的url
		var linkUrl=$("#itemUrl").val().trim();
		if(!checkUrl(linkUrl)) return;
		//运费模板
		var dtId=$("#cmbDt").val();
		//要修改的标题关键字
		var oldTitle=$("#oldTitle").val().trim();
		//替换后的标题关键字
		var newTitle=$("#newTitle").val().trim();
		//价格变更方式
		var priceWay=$("#priceWay").val();
		//价格变更值
		var priceVal=$("#priceVal").val();
		//拼多多分类
		var itemCat=$("#cmbThirdCat").val();
		//七天无理由
		var backGood=$("#backGood").val();
		//商品类型
		var goodType=$("#goodType").val();
		//发货承诺
		var promisePost=$("#promisePost").val();
		
		//直接发布
		var isPublish=$("#isPublish").val();
		
		var payment=$("#payment").val();
		var data=null;
		//执行复制
		if(itemCat==0||$("#cmbThirdCat").children().length<1){
			$("#searchMsg").html("<font color='red'>请先选择分类信息！</font>");
			return;
		}
		
		if(dtId==0){
			$("#searchMsg").html("<font color='red'>请先选择模板信息！</font>");
			return;
		}
		
		$.ajax({
			type : "get",
			url : "/ItemCarrierPdd/copyItem",
			data:{
				'url':linkUrl,
				'dtId':dtId,
				'oldTitle':oldTitle,
				'newTitle':newTitle,
				'priceWay':priceWay,
				'priceVal':priceVal,
				'itemCat':itemCat,
				'passId':passId,
				'payment':payment,
				'backGood':backGood,
				'goodType':goodType,
				'promisePost':promisePost,
				'isPublish':isPublish
			},
			dataType : "json",
			success : function(data) {
		        var content="宝贝复制错误，请重试，多次重试仍失败请联系客服反馈。";
                if(!isUfOrNullOrEmpty(data)){
                    content=data.msg;
                }
                TM.Alert.load('<br><p style="font-size:15px">'+content+'</p>',470,230,function(){
                });
			},
			error : function(XMLHttpRequest, textStatus, errorThrown) {
				  TM.Alert.load('<br><p style="font-size:15px">宝贝复制错误，请重试，多次重试仍失败请联系客服反馈。</p>',400,230);
			}
		});
	}
	
	
	//判断值是否为undefined或者null或者为空
	function isUfOrNullOrEmpty(val){
		return val==null||val=='undefined'||val=="";
	}
	//检验宝贝地址是否正确
	function checkUrl(url){
		if(isUfOrNullOrEmpty(url)){
			$("#urlMsg").html("<font color='red'>请填写要复制的宝贝地址。</font>");
			return false;
		}
		return true;
//		//tm
//		var tmPattern=/^https?:\/\/detail.tmall.com\/item.htm\?(\w+)id=(\d+).*$/;
//		//淘宝https://item.taobao.com/item.htm?id=555917614274
//		var tbPattern=/^https?:\/\/item.taobao.com\/item.htm\?(\w+)id=(\d+).*$/;
//		if(tmPattern.test(url)||tbPattern.test(url)){
//			$("#urlMsg").html("<font color='blue'>填写正确。</font>");
//			return true;
//		}else{
//			$("#urlMsg").html("<font color='red'>宝贝地址填写错误！</font>");
//			return false;
//		}
	
	}
	
	function loginGetInfo(){
		var paramData={};
		paramData.userName=$("#pdduserName").val();
		paramData.password=$("#pddpassword").val();
		
		if(isUfOrNullOrEmpty(paramData.userName)||isUfOrNullOrEmpty(paramData.password)){
			  TM.Alert.load('<br><p style="font-size:15px">用户名和密码不可为空！</p>',400,230,function(){
                });
                return false;
		}
		
		$.ajax({
			type: "post",
			url : "/ItemCarrierPdd/loginGetInfo",
			data: paramData,
			dataType : "json",
			success : function(data) {
				if(data.isOk){
					mallId=data.res.mallId;
					passId=data.res.passId;
					//登录成功加载数据
					initShipTemplate();
					initMainCat();
				}else{
					 TM.Alert.load('<br><p style="font-size:15px">'+data.msg+'</p>',400,230,function(){
                });
					//失败处理：短信验证码
				}
				console.log(JSON.stringify(data));
			},
			error : function(XMLHttpRequest, textStatus, errorThrown) {
				console.info('系统出错，请重试！');
			}
		});
	}
	
	//加载运费模板信息
	function initShipTemplate(){
		var paramData={};
		paramData.mallId=mallId;
		paramData.passId=passId;
		$.ajax({
			type: "post",
			url : "/ItemCarrierPdd/initShipTemplate",
			data: paramData,
			dataType : "json",
			success : function(data) {
				var templateArray=data.result.list;
				
				if(templateArray==null){
					 TM.Alert.load('<br><p style="font-size:15px">'+'亲，您的店铺还没有一个合适的运费模版呢。'+'</p><br/><p><a style="text-decription:underline;color:blue;" target="_blank" href="http://mms.pinduoduo.com/Pdd.html#/orders/carriage/list">前往创建<a/></p>',400,230,function(){
		                });
					 return;
				}
				var html="";
				$.each(templateArray,function(i,t){
					html=html+'<option value="'+t.costTemplateId+'">'+t.costTemplateName+'</option>';
				})
				$("#cmbDt").empty();
				$("#cmbDt").append(html);
				
			},
			error : function(XMLHttpRequest, textStatus, errorThrown) {
				console.info('系统出错，请重试！');
			}
		});
	}
	
	//加载主类目信息
	function initMainCat(){
		var paramData={};
		paramData.mallId=mallId;
		paramData.passId=passId;
		$.ajax({
			type: "post",
			url : "/ItemCarrierPdd/initMainCat",
			data: paramData,
			dataType : "json",
			success : function(data) {
				var html="";
				$.each(data.result,function(i,t){
					html=html+'<option value="'+t.id+'">'+t.name+'</option>';
				})
//				$("#cmbMainCat").empty();
				$("#cmbMainCat").append(html);
			},
			error : function(XMLHttpRequest, textStatus, errorThrown) {
				console.info('系统出错，请重试！');
			}
		});
	}
	
	$("#cmbMainCat").change(function(){
		//级联二级类目
		var paramData={};
		paramData.mallId=mallId;
		paramData.passId=passId;
		paramData.level=2;
		paramData.parentId=$(this).val();
		if(paramData.parentId==0){
			return;
		}
		$.ajax({
			type: "post",
			url : "/ItemCarrierPdd/initSonCat",
			data: paramData,
			dataType : "json",
			success : function(data) {
				var html='<option value="0" selected="selected">&nbsp;---请选择分类---&nbsp;</option>';
				$.each(data,function(i,t){
					html=html+'<option value="'+t.id+'">'+t.cat_name+'</option>';
				})
				$("#cmbSecondLevelCat").empty();
				$("#cmbSecondLevelCat").append(html);
			},
			error : function(XMLHttpRequest, textStatus, errorThrown) {
				console.info('系统出错，请重试！');
			}
		});
	})
	
	$("#cmbSecondLevelCat").change(function(){
		//级联二级类目
		var paramData={};
		paramData.mallId=mallId;
		paramData.passId=passId;
		paramData.level=3;
		paramData.parentId=$(this).val();
		$.ajax({
			type: "post",
			url : "/ItemCarrierPdd/initSonCat",
			data: paramData,
			dataType : "json",
			success : function(data) {
				var html="";
				$.each(data,function(i,t){
					html=html+'<option value="'+t.id+'">'+t.cat_name+'</option>';
				})
				$("#cmbThirdCat").empty();
				$("#cmbThirdCat").append(html);
			},
			error : function(XMLHttpRequest, textStatus, errorThrown) {
				console.info('系统出错，请重试！');
			}
		});
	})
	
	