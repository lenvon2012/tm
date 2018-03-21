var TM = TM || {};

((function ($, window) {
    TM.ItemProtection = TM.ItemProtection || {};

    var ItemProtection = TM.ItemProtection;
    /**
     * 初始化
     * @type {*}
     */
    ItemProtection.init = ItemProtection.init || {};
    ItemProtection.init = $.extend({
        doInit: function(container) {
            ItemProtection.container = container;
            ItemProtection.init.initSearchArea();
//            ItemProtection.show.loadItems();
            container.find(".search-btn").click(function() {
            	ItemProtection.show.doShow();
            });
            ItemProtection.show.doShow();
            
            container.find(".delete-btn").click(function() {
            	var numIid = container.find(".delete-btn").attr('numiid');
            	window.location.href = '/skindefender/deleteItemProtectionDetail?numIid=' + numIid; 
            });
            container.find(".cancel-btn").click(function() {
            	window.location.href = '/skindefender/itemProtection';
            });
            
        },
        initSearchArea : function(){
            $.get("/items/sellerCatCount",function(data){
                var sellerCat = $('#itemsCat');
                sellerCat.empty();
                if(!data || data.length == 0){
                    sellerCat.hide();
                }

                var exist = false;
                var cat = $('<option>自定义类目</option>');
                sellerCat.append(cat);
                for(var i=0;i<data.length;i++) {
                    if(data[i].count <= 0){
                        continue;
                    }

                    exist = true;
                        var option = $('<option></option>');
                        option.attr("catId",data[i].id);
                        option.html(data[i].name+"("+data[i].count+")");
                        sellerCat.append(option);
                }
                if(!exist){
                    sellerCat.hide();
                }
            });
            $.get("/items/itemCatCount",function(data){
                var taobaoCat = $('#taobaoCat');
                taobaoCat.empty();
                if(!data || data.length == 0){
                    taobaoCat.hide();
                }

                var exist = false;
                var cat = $('<option>淘宝类目</option>');
                taobaoCat.append(cat);
                for(var i=0;i<data.length;i++) {
                    if(data[i].count <= 0){
                        continue;
                    }

                    exist = true;
                    var option = $('<option></option>');
                    option.attr("catId",data[i].id);
                    option.html(data[i].name+"("+data[i].count+")");
                    taobaoCat.append(option);
                }
                if(!exist){
                    taobaoCat.hide();
                }
            });
        }
    }, ItemProtection.init);
    
    ItemProtection.show = ItemProtection.show || {};
    ItemProtection.show = $.extend({
        doShow: function() {
            var params = $.extend({
                "s":"",
                "status":2,
                "catId":null,
                "sort":1,
                "lowBegin":0,
                "ps":5,
                "topEnd":100
            },ItemProtection.show.getParams());
            
            ItemProtection.container.find(".paging-div").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: params,
                    dataType: 'json',
                    url: '/SkinDefender/getItems',
                    callback: function(data) {
                    	var list = data.res;
                    	if (list == undefined || list == null) {
                    		$('#rows').empty();
                    		$('#rows').html('<tr><td colspan="4" height="40px">亲，没找到相关内容哦</td></tr>');
                    		return;
                    	}
                    	
                    	for(var i in list) {
                    		var one = list[i].itemLimit;
                    		if (one != null) {
                    			if (one.daysLimit != undefined && one.daysLimit > 0) {
                    				list[i].limit = one.daysLimit + "天内";
                    			}
                    			if (one.tradeNum > 0) {
                    				if(list[i].limit == undefined) {
                    					list[i].limit = "最多订单"+one.tradeNum + "单";
                    				} else {
                    					list[i].limit += "，最多订单"+one.tradeNum + "单";
                    				}
                    			}
                    			if (one.itemMinNum > 0) {
                    				if (list[i].limit == undefined) {
                    					list[i].limit = "最少"+ one.itemMinNum + "件";
                    				} else {
                    					list[i].limit += "，最少"+ one.itemMinNum + "件";
                    				}
                    			}
                    			if (one.itemMaxNum > 0) {
                    				if (list[i].limit == undefined) {
                    					list[i].limit = "最多"+one.itemMaxNum + "件";
                    				}else{
                    					list[i].limit += "，最多"+one.itemMaxNum + "件";
                    				}
                    			}
                    			if (one.vipLevel > 0) {
                    				if (list[i].limit == undefined) {
                    					list[i].limit = "只允许VIP" + one.vipLevel + "以上购买";
                    				}else{
                    					list[i].limit += "，只允许VIP" + one.vipLevel + "以上购买";
                    				}
                    			}
                    		}
                    	}
                    	$('#rows').empty();
                        $('#tplItem').tmpl(list).appendTo('#rows');
                    }
                }

            });
        },
        refresh: function() {
            ItemProtection.show.doShow();
        },
        getParams : function(){
            var params = {};
            var status = $("#itemsStatus option:selected").attr("tag");
            switch(status){
                case "onsale":params.status=0;break;
                case "instock" : params.status=1;break;
                default : params.status=2;break;
            }

            var catId = $('#itemsCat option:selected').attr("catId");
            params.catId = catId;

            var cid = $('#taobaoCat option:selected').attr("catId");
            params.cid = cid;

            var sort = $('#itemsSortBy option:selected').attr("tag");
            switch(sort){
                case "sortByScoreUp" : params.sort=1;break;
                case "sortByScoreDown" : params.sort=2;break;
                case "sortBySaleCountUp" : params.sort=3;break;
                case "sortBySaleCountDown" : params.sort=4;break;
                default : params.sort=1;break;
            }
            
            var only = $('#onlyItemLimit option:selected').attr("tag");
            params.only = only;

            params.lowBegin = $('#lowScore').val();
            params.topEnd = $('#highScore').val();
            params.s = $('#searchText').val();
            params.numIid = $('#numIid').val();
            return params;
        }
    }, ItemProtection.show);


})(jQuery,window));