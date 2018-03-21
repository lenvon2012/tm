/**
 *TS is top shop
 *@type object
 */

var TS = TS || {};

/**
 * @name shop cat 
 * @type object
 */

TS.shopcat = TS.shopcat || {};
/**
 * shops
 */
TS.shops = TS.shops || {};

/**
 * Utilities.
 */
(function($, window) {
	    TS.shopcat.setLevel3Select=function(level2ID){
	        $.ajax({
	            //async : false,
	            url : '/KeyWords/findLevel3',
	            data : {level2:level2ID},
	            type : 'post',
	            success : function(data) {
	            	alert(data);
	            	TS.shopcat.setLevel3Content(data);
	            }
	        });
	    }
	    TS.shopcat.setLevel3Content=function(data){
	        $(".selectCat3").html("");
	        for(var i=0;i<data.length;i++){
	            var option=$('<option></option>');
	            option.html(data[i].tag);
	            option.attr("urlID",data[i].id);
	            $(".selectCat3").append(option);
	        }
	        $(".selectCat3").append($('<option></option>'));
	    }
	    TS.shops.findshops = function(shopcat){
	    	$.ajax({
	    		url:"/topshops/searchShopCat",
	    		data:{shopcat:shopcat.replace('&nbsp;','')},
	    		type : 'post',
	    		success:function(data){
//	    			$.each(data,function(i,t){
//	    				alert(t.nick);
//	    			});
	    			 TS.shops.showshops(data);
	    		}
	    	});
	    }
	   
	    TS.shops.showshops = function(data){
	    	$(".shopresult").html("");
	    	var code =[];
	    	$.each(data,function(i,t){
	    		code.push("<tr>");
	    		code.push("<td><img class = 'shopswitch' onclick = 'TS.shops.searchItems("+t.id+")' src='/public/images/topshop/close.png'></td> " );
	    		code.push("<td class = 'shopnumber'>NO "+i+".</td>");
	    		code.push("<td><img alt='店铺logo' class = 'shoplogo' src='"+t.picPath+"'></td>");
	    		code.push("<td><div>");
	    		code.push("掌柜名："+t.nick+"");
	    		code.push("件宝贝数量："+t.itemCount+"件");
	    		code.push("30天销售量："+t.recentTradeCount+"");
	    		code.push("人气指数："+t.popularity);
	    		code.push("店铺等级："+t.level);
//	    		code.append("*****："+t.quality);
//	    		code.append("*****："+t.isBType);
	    		code.push("</div></td>");
	    		code.push("</tr>");
	    		code.push("<tr class = 'showItems'><td></td><td></td><td><table class = 'itemResult' >");
	    		code.push("	</table></td></tr>");
//	    		$(".shopresult").append(code);
	    	});
//	    	alert(code);
//	    	$(".shopresult").html(code);
	    	$(code.join('')).appendTo(".shopresult");
	    }
	    TS.shops.open = function(){
	    	$(".showItems").removeAttr();
	    }
	    TS.shops.searchItems = function(data){
	    	$.ajax({
	    		url:"/TopShops/searchShopItem",
	    		data:{userId:data},
	    		success:function(data){
	    			TS.shops.open();
	    			TS.shops.showItems(data);	
	    		}
	    	});
	    };
	    TS.shops.showItems = function(data){
	    	var code = [];
	    	$.each(data,function(i,t){
	    		code.push("<tr><td><img alt='宝贝图片' src="+t.picPath+">");
	    		code.push("</td><td><span>宝贝名称：</span>");
	    		code.push(t.title);
	    		code.push("<span>宝贝销量：</span>");
	    		code.push(t.sale);
	    		code.push("</td></tr>");
	    	});
//	    	alert(code);
	    	$(".itemResult").html(code);
	    }
	  
	    TS.shops.select2changeFunction = function(){
	    	var level2ID=$('.selectCat2 option:selected').val();
	        TS.shopcat.setLevel3Select(level2ID);
	    }
	    
	    
	    TS.shops.select3changeFunction=function(){
	    	var shopcat =  $('.selectCat3 option:selected').html();
	    	TS.shops.findshops(shopcat);
	    }
})(jQuery, window);
$('.selectCat2').change(function(){
	TS.shops.select2changeFunction();
//    TS.shops.select3changeFunction();
});
$('.selectCat3').change(function(){
	 TS.shops.select3changeFunction();
});