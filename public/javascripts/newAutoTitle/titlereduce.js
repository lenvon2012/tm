var TM = TM || {};

((function($, window){
	TM.TitleReduceAdmin = TM.TitleReduceAdmin || {};
	var TitleReduceAdmin = TM.TitleReduceAdmin;
	
	TitleReduceAdmin.init = TitleReduceAdmin.init || {};
	TitleReduceAdmin.init = $.extend({
		
		doInit: function(container){
			TitleReduceAdmin.container = container;
			
			TM.TitleSingleReduce.init.doInit(container.find('.single-title-div'));
			TM.TitleAllReduce.init.doInit(container.find('.all-title-div'));
			TM.TitleTimeReduce.init.doInit(container.find('.time-title-div'));
			
			var tabBtnObjs = container.find('.title-reduce-tab');
			
			tabBtnObjs.unbind().click(function(){
				tabBtnObjs.removeClass('select-tab');
				
				var thisBtnObj = $(this);
				thisBtnObj.addClass("select-tab");
				
				container.find(".title-tab-target-div").hide();
				
				var targetDivCss = thisBtnObj.attr("targetDiv");
				var targetDivObj = container.find("." + targetDivCss);
				targetDivObj.show();
				
				if(targetDivCss == "single-title-div"){
					TM.TitleSingleReduce.show.doShow();
				}else if(targetDivCss == "all-title-div"){
					TM.TitleAllReduce.show.doShow();
				}else if(targetDivCss == "time-title-div"){
					TM.TitleTimeReduce.init.getContainer().find('.timeTitleBtn').click();
				}
				
			});
			container.find(".title-reduce-tab.select-tab").click();
		}
	
	}, TitleReduceAdmin.init);
})(jQuery, window));


((function($, window){
	TM.TitleSingleReduce = TM.TitleSingleReduce || {};
	var TitleSingleReduce = TM.TitleSingleReduce;
	
	TitleSingleReduce.init = TitleSingleReduce.init || {};
	TitleSingleReduce.init = $.extend({
		
		doInit: function(container){
			TitleSingleReduce.container = container;
			TitleSingleReduce.init.initSearchText();
			TitleSingleReduce.init.initSearchClick();
			TitleSingleReduce.init.initDefaultKeyDown();
			TitleSingleReduce.init.initSearchArea();
			TitleSingleReduce.init.initSort();
            TitleSingleReduce.init.initCategory();
		},
		getContainer:function(){
			return TitleSingleReduce.container;
		},
		initSearchClick:function(){
			$('.titleSingleBtn').click(function(){
				TitleSingleReduce.show.doShow();
			});
		},
		initDefaultKeyDown:function(){
			$('#searchText').keyup(function(event){
				if(event.which == 13){
					$(".titleSingleBtn").trigger("click");
				}
			})
		},
		initSearchArea:function(){
			$.get("/items/itemCatCount",function(data){
                var taobaoCat = $('#taobaoCat');
                taobaoCat.empty();


                var cat = $('<option>所有淘宝类目</option>');
                taobaoCat.append(cat);
                if(!data || data.length == 0){
                    return;
                }
                for(var i=0;i<data.length;i++) {
                    if(data[i].count <= 0){
                        continue;
                    }
                    var option = $('<option></option>');
                    option.attr("catId",data[i].id);
                    option.html(data[i].name+"("+data[i].count+")");
                    taobaoCat.append(option);
                }
                TitleSingleReduce.init.initTaoBaoCatClick();
            });

            $.get("/items/sellerCatCount", function(data){
                var sellerCat = $('#itemsCat');
                sellerCat.empty();

                var cat = $('<option>所有店铺分类</option>');
                sellerCat.append(cat);

                if(!data || data.length == 0){
                    return;
                }
                for(var i=0;i<data.length;i++) {
                    if(data[i].count <= 0){
                        continue;
                    }

                    var option = $('<option></option>');
                    option.attr("catId",data[i].id);
                    option.html(data[i].name+"("+data[i].count+")");
                    sellerCat.append(option);
                }
                TitleSingleReduce.init.initItemCatClick();
            });
		},
        initTaoBaoCatClick:function(){
            $('#taobaoCat').change(function(){
                $('.titleSingleBtn').trigger('click');
            })
        },
        initItemCatClick:function(){
            $('#itemsCat').change(function(){
                $('.titleSingleBtn').trigger('click');
            })
        },
		initSearchText:function(){
			var searchText = $('#searchText');
			$('#searchText').click(function(){
				if($.trim($(this).val()) == "输入宝贝标题"){
					searchText.val("");
				}
			}).blur(function(){
				if($.trim($(this).val()) == ""){
					searchText.val("输入宝贝标题");
				}
			});
		},
        initCategory:function(){
            $('#itemStatus').change(function(){
                $('.titleSingleBtn').click();
            });

        },
		initSort:function(){
			var container = TitleSingleReduce.init.getContainer();
			var sort = container.find('.sort-td');
			sort.toggle(function(){
				sort.removeClass('sort-up');
				sort.addClass('sort-down');
				sort.attr('value', 2);
//				sort.attr("value").val("2");
				container.find(".titleSingleBtn").trigger("click");
			},function(){
				sort.removeClass('sort-down');
				sort.addClass('sort-up');
//				sort.attr("value").val("1")
				sort.attr('value', 1);
				container.find(".titleSingleBtn").trigger("click");
			})
		}
		
	}, TitleSingleReduce.init);
	
	TitleSingleReduce.show = TitleSingleReduce.show || {};
	TitleSingleReduce.show = $.extend({
		doShow:function(){
			var container = TitleSingleReduce.init.getContainer();
			var searchTextVal = $.trim($('#searchText').val());
			if(searchTextVal == "输入宝贝标题"){
				$('#searchText').val("");
			}
			var params = TitleSingleReduce.show.getParams(container);
			container.find(".single-paging-div").tmpage({
				currPage: 1,
				pageSize: 10,
				pageCount: 1,
				ajax: {
					param : {
						s: params.s,
						status:params.status,
		                catId:params.catId,
		                cid: params.cid,
		                sort:params.sort,
		                lowBegin:params.lowBegin,
		                topEnd: params.topEnd
		                },
					on:true,
					dataType:'json',
					url: '/Titles/getItemsWithDiagResult',
					callback: function(data){
						var tbodyObj = container.find('.title-single-table tbody');
						tbodyObj.html('');
						var singleJsonArray = data.res;
						if(singleJsonArray == null  || singleJsonArray.length <= 0){
							tbodyObj.html('<tr><td colspan="5" style="padding: 10px 0px;">暂无标题还原记录！</td></tr>');
						}
						var trObjs = $('#titlesingletmpl').tmpl(singleJsonArray);
						tbodyObj.empty();
						var searchText = $('#searchText');
						if($.trim(searchText.val()) == ""){
							searchText.val("输入宝贝标题");
						}
						tbodyObj.append(trObjs);
						TitleSingleReduce.historyTitles.initHistoryTitles($('.title-single-table'));
					}
				}
			});
		},
		getParams:function(container){
			var params = {};
			params.s = container.find("#searchText").val();
			params.status = container.find("#itemStatus option:selected").attr('value');
			params.cid = container.find('#taobaoCat option:selected').attr('catId');
            params.catId = container.find('#itemsCat option:selected').attr("catId");
//			params.sort = TitleSingleReduce.show.getSort(container.find('#itemsSortBy option:selected').attr("tag"));
			params.sort = container.find(".sort-td").val();
			params.lowBegin = container.find('#lowScore').val();
			params.topEnd = container.find('#highScore').val();
			return params;
		}
//		getStatus:function(strStatus){
//			var stat;
//			if(strStatus == "all"){
//				stat = 2;
//			}else if(strStatus == "onsale"){
//				stat = 0;
//			}else if(strStatus == "instock"){
//				stat = 1;
//			}
//			return stat;
//		},
//		getSort:function(strSort){
//			var sort;
//			switch(strSort){
//            	case "sortByScoreUp" : sort=1;break;
//            	case "sortByScoreDown" : sort=2;break;
//            	default : sort=1;break;
//			}
//			return sort;
//		}
	}, TitleSingleReduce.show);
	
	TitleSingleReduce.historyTitles = TitleSingleReduce.historyTitles || {};
	TitleSingleReduce.historyTitles = $.extend({
		initHistoryTitles:function(container){
			var historySpans = container.find('.historyTitle');
			historySpans.unbind().click(function(){
				var numIid = $(this).attr('targetId');
				var diagObj = TitleSingleReduce.historyTitles.initDiagHistory(numIid);
				var dialogTitle = "历史标题";
				diagObj.dialog({
					modal: true,
	                bgiframe: true,
	                height:600,
	                width:1000,
	                title: dialogTitle,
	                autoOpen: false,
	                resizable: false,
                    draggable:true,
                    hide:{effect:"explode",duration:100},
	                buttons:{
	                	'取消':function(){
	                		diagObj.dialog('close');
	                	}
	                }
				});
				diagObj.dialog('open');
				TitleSingleReduce.historyTitles.initReduceSingleTitle($('.historyDiv'));
			});
		},
		initDiagHistory:function( numIid){
			var diagObj;
			$.ajax({
				type:'get',
				url:'/Titles/renameHistoryAll',
				data:{numIid:numIid},
				dataType:'json',
				async: false,
				success:function(data){
					if(data == null){
						return;
					}
					for(var i = 0; i < data.res.length; ++i){
						data.res[i].updated = new Date(data.res[i].updated).formatYMDHMS();
					}
					diagObj = $('#historytitlestmpl').tmpl(data);					
				}
			})
			return diagObj;
		},
		initReduceSingleTitle:function(container){
			var reduceSingleTitleBtns = $('.reduceSingleTitle');
			reduceSingleTitleBtns.unbind().click(function(){
                if(!confirm("确定要还原该宝贝标题？")){
                    return;
                }
				var title = $(this).attr('oldTitle');
				var numIid = $(this).attr('targetId');
				$.ajax({
					type:'get',
					url:'/Titles/rename',
					dataType:'json',
					data:{
						title:title,
						numIid:numIid
					},
					success:function(data){

						if (data.ok) {
							alert("提示：亲，还原成功！");
                        } else {
                        	alert("提示：还原失败！" + data.msg);
                        }
					}
				})
			});
		}
	}, TitleSingleReduce.historyTitles);
	
	
})(jQuery, window));


((function($, window){
	TM.TitleAllReduce = TM.TitleAllReduce || {};
	var TitleAllReduce = TM.TitleAllReduce;
	
	TitleAllReduce.init = TitleAllReduce.init || {};
	TitleAllReduce.init = $.extend({
		doInit:function(container){
			TitleAllReduce.container = container;
			
		},
		getContainer:function(){
			return TitleAllReduce.container;
		}
	}, TitleAllReduce.init);
	
	TitleAllReduce.show = TitleAllReduce.show || {};
	TitleAllReduce.show = $.extend({
		doShow:function(){
			var container = TitleAllReduce.init.getContainer();
			
			container.find('.all-paging-div').tmpage({
				currPage:1,
				pageSize:10,
				pageCount:1,
				ajax:{
					on:true,
					dataType:'json',
					url:'/Titles/batchOpLogs',
					callback:function(data){
						if(data == null || data.res == null || data.res.length <= 0){
							return;
						}
						for(var i = 0; i < data.res.length; ++i){
							data.res[i].ts = new Date(data.res[i].ts).formatYMDHMS();
						}
						var bodyObj = $('#titlealltmpl').tmpl(data.res);
						var tbody = container.find('.title-all-table tbody');
						tbody.empty();
						tbody.append(bodyObj);			
						TitleAllReduce.show.initBatchDetail(tbody);
						TitleAllReduce.show.initBatchReduce(tbody);
					}
				}
			});
		},
		initBatchDetail:function(container){
			var detailBtns = container.find('.detailBtn');
			detailBtns.unbind().click(function(){
				var id = $(this).attr('targetId');
				var diagObj = TitleAllReduce.show.getBatchLogData(id);
				var dialogTitle = "标题还原中心";
				diagObj.dialog({
					modal: true,
	                bgiframe: true,
	                height:600,
	                width:1000,
	                title: dialogTitle,
	                autoOpen: false,
	                resizable: false,
	                buttons:{
	                	'确定':function(){
	                		diagObj.dialog('close');
	                	}
	                }
				});
				diagObj.dialog('open');
			});
		},
		getBatchLogData:function(id){
			var diagObj;
			$.ajax({
				type:'post',
				url:'/Titles/batchOpLogDetail',
				data:{id:id},
				async:false,
				success:function(data){
					var dataObj = {'dataArr':data};
					diagObj = $("#batchAllDetail").tmpl(dataObj);
				}
				
			});
			return diagObj;
		},
		initBatchReduce:function(container){
			var oneKeyBatchReduceBtns = container.find('.oneKeyReduce');
			oneKeyBatchReduceBtns.unbind().click(function(){
                if(!confirm("是否一键还原所有宝贝？")){
                    return;
                }
				var id = $(this).attr('targetId');
				$.ajax({
					type:'post',
					url:'/Titles/recoverBatch',
					data:{id:id},
					success:function(data){
						alert('还原成功宝贝数:' + data.successNum+"  还原失败宝贝数:"+data.failNum);
					}
				})
			})
			
		}
	}, TitleAllReduce);
	
})(jQuery, window));


((function($, window){
	TM.TitleTimeReduce = TM.TitleTimeReduce || {};
	var TitleTimeReduce = TM.TitleTimeReduce;
	
	TitleTimeReduce.init = TitleTimeReduce.init || {};
	TitleTimeReduce.init = $.extend({
		doInit: function(container){
//			var dayMillsSeconds = 24*60*60*1000;
			TitleTimeReduce.container = container;
			container.find('.timeText').datetimepicker({
				lang:'ch',
				format:'Y-m-d H:i:s',
				value: new Date(new Date().getTime()).format('yyyy-MM-dd h:m:s')
			});
//            var timeText = container.find('.timeText').val();
//            var backToTs = Date.parse(timeText);
			TitleTimeReduce.init.initSearchTime();
            TitleTimeReduce.init.initOneKeyReduce(container);
		},
		getContainer:function(){
			return TitleTimeReduce.container;
		},
		initSearchTime:function(){
			var container = TitleTimeReduce.init.getContainer();
			var timeTitleBtn = container.find('.timeTitleBtn');
			
			timeTitleBtn.unbind().click(function(){
				container.find(".timetitleholder").show();
				var timeText = container.find('.timeText').val();
				var backToTs = Date.parse(timeText);
				container.find(".time-paging-div").tmpage({
					currPage:1,
					pageSize:10,
					pageCount:1,
					ajax:{
						on:true,
						param:{
							backToTs:backToTs
						},
						dataType:'json',
						url:'/Titles/searchBackByTime',
						callback: function(data){
							container.find(".timetitleholder tbody").empty();
							if(data == null || data.res == null || data.res.length == 0){
								return;
							}
							for(var i =0; i < data.res.length; ++i){
								data.res[i].lastOptimiseTs = new Date(data.res[i].lastOptimiseTs).formatYMDHMS();
								data.res[i].num = i + 1;
							}
							var dataObjs = $('#titletimetmpl').tmpl(data.res);
							
							container.find(".timetitleholder tbody").append(dataObjs);
						}
					}
				})
			});
		},
		initOneKeyReduce:function(container){
			var oneKeyReduce = container.find(".onekeyreducebtn");
			oneKeyReduce.click(function(){
                if(!confirm("是否确定还原到"+ $('.timeText').val()+"时间点")){
                    return;
                }
                var timeText = container.find('.timeText').val();
                var backToTs = Date.parse(timeText);
				$.ajax({
					type:'post',
					url:'/Titles/titleBackByTime',
					data:{backToTs:backToTs},
					success:function(data){
                        if(data.success != null && data.success == false){
                            alert(data.message);
                            return;
                        }
						var succnum = 0;
						var failnum = 0;

						$.each(data,function(index, element){
							element.ok == true?++succnum:++failnum;
						});
						alert("批量还原成功的个数是:" + succnum + " 失败的个数是:" + failnum);
					}
				});
			})
		}
	}, TitleTimeReduce.init);
	
})(jQuery, window));


