var TM = TM || {};

((function ($, window) {
    TM.ItemPass = TM.ItemPass || {};

    var ItemPass = TM.ItemPass;
    /**
	 * 初始化
	 * 
	 * @type {*}
	 */
    ItemPass.init = ItemPass.init || {};
    ItemPass.init = $.extend({
        doInit: function(container) {
            ItemPass.container = container;
            ItemPass.init.initSearchArea();
            container.find(".search-btn").click(function() {
            	ItemPass.show.doShow();
            });
            ItemPass.show.doShow();
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
    }, ItemPass.init);
    
    ItemPass.show = ItemPass.show || {};
    ItemPass.show = $.extend({
        doShow: function() {
            var params = $.extend({
                "s":"",
                "status":2,
                "catId":null,
                "sort":1,
                "lowBegin":0,
                "ps":5,
                "topEnd":100
            },ItemPass.show.getParams());
            
            ItemPass.container.find(".paging-div").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: params,
                    dataType: 'json',
                    url: '/SkinDefender/getItemsWithItemPass',
                    callback: function(data){
                    	var list = data.res;
                    	if (list == undefined || list == null || list.length == 0) {
                    		$('#rows').empty();
                    		$('#rows').html('<tr><td colspan="4" height="40px">亲，没找到相关内容哦</td></tr>');
                    		return;
                    	}
                    	
                    	for(var i in list){
                    		list[i].limitStatus1 = 1;
            				list[i].limitStatus2 = 2;
                    		if (list[i].itemPass != null) {
                    			if(list[i].itemPass.status == 1){
                    				list[i].limitStatus1 = 0;
                    				list[i].statusLabel = "已加入永不拦截";
                    			}
                    			if(list[i].itemPass.status == 2){
                    				list[i].limitStatus2 = 0;
                    				list[i].statusLabel = "已加入仅黑名单拦截";
                    			}
                    		}
                    	}
                    	
                    	var rows = $('#rows');
                    	rows.empty();
                        $('#tplItem').tmpl(list).appendTo('#rows');
                        
                        rows.find('.addItemPass').each(function(){
                        	var row = $(this);
                        	var status = row.attr('status');
                        	if(status == 0) {
                        		row.text("取消不拦截");
                        		row.css('background', "green");
                        	}
                        });
                        rows.find('.addItemPassNoBlacklist').each(function(){
                        	var row = $(this);
                        	var status = row.attr('status');
                        	if(status == 0) {
                        		row.text("取消不拦截");
                        		row.css('background', "green");
                        	}
                        });
                        
                        rows.find('.addItemPass').unbind('click').click(function(){
                        	var me = $(this);
                        	var numIid = me.attr('numiid');
                        	var status = me.attr('status');
                        	$.post('/SkinDefender/addItemPass',{numIid:numIid, status:status},function(data){
                        		var statusLabel = me.parent().parent().parent().find('.status-label');
                        		if(data == "on") {
                        			me.text("取消不拦截");
                        			me.attr('status', 0);
                        			me.css('background', "green");
                        			var noblack = me.parent().parent().parent().find('.addItemPassNoBlacklist');
                        			if(noblack.attr('status') == 0) {
                        				noblack.text("设置仅黑名单拦截");
                        				noblack.attr('status', 2);
                        				noblack.css("background", "");
                        			}
                        			statusLabel.text("已加入永不拦截");
                        		} else if(data == "off") {
                        			me.text("设置永不拦截");
                        			me.attr('status', 1);
                        			me.css("background", "");
                        			statusLabel.text("");
                        		}
                        	});
                        });
                        
                        rows.find('.addItemPassNoBlacklist').unbind('click').click(function(){
                        	var me = $(this);
                        	var numIid= me.attr('numiid');
                        	var status= me.attr('status');
                        	$.post('/SkinDefender/addItemPass',{numIid:numIid, status:status},function(data){
                        		var statusLabel = me.parent().parent().parent().find('.status-label');
                        		if(data == "on") {
                        			me.text("取消不拦截");
                        			me.attr('status', 0);
                        			me.css('background', "green");
                        			var itempass = me.parent().parent().parent().find('.addItemPass');
                        			if(itempass.attr('status') == 0) {
                        				itempass.text("设置永不拦截");
                        				itempass.attr('status', 2);
                        				itempass.css("background", "");
                        			}
                        			statusLabel.text("已加入仅黑名单拦截");
                        		} else if(data == "off") {
                        			me.text("设置仅黑名单拦截");
                        			me.attr('status', 2);
                        			me.css("background", "");
                        			statusLabel.text("");
                        		}
                        	});
                        });
                    }
                }
            });
        },
        refresh: function() {
            ItemPass.show.doShow();
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
    }, ItemPass.show);


})(jQuery,window));