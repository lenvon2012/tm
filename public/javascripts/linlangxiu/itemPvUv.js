var QueryCommodity = QueryCommodity || {};

var numIid = "";

QueryCommodity.query = QueryCommodity.query || {};
QueryCommodity.query = $.extend({
	getComms:function(){
		var container = $("#commodityDiag");
		container.html("");
		$.ajax({
			url: '/Diag/singleItemInfo',
			data: {numIid : numIid},
			type: 'GET',
			global: false,
			beforeSend: function() {
				TM.Loading.init.show();
			},
			success: function(data){
				if(!data.isOk) {
					TM.Alert.load(data.msg);
				}

				var itemJson = data.res;
				
				var diagContainer =$("#commodityDiag");
				diagContainer.empty();
				var commodityDiag= $("<div></div>");

				var itemInfo = QueryCommodity.commodityDiv.createDiv(itemJson);
				var itemDiag = QueryCommodity.commodityDiv.createDetail(itemJson);
				commodityDiag.append(itemInfo);
				commodityDiag.append(itemDiag);

				var elems = commodityDiag.children();

				elems.find('.explore-diag').click(function(){
					var oThis = $(this);
					var numiid = oThis.attr('numiid');
					var tag = oThis.attr("tag");
					var diagRow = diagContainer.find('.diagtr[numiid="'+numiid+'"]');
					diagRow .show(400);
					diagRow.find('.tabDiv ul li a[targetcls="'+tag+'"]').trigger("click");
				});
				diagContainer.append(elems);
				// 显示第一个宝贝的效果分析
				var numIid = diagContainer.find('.singlecomm').first().attr('numiid');
				var diagRow = diagContainer.find('.diagtr[numiid="'+numIid+'"]');
				diagRow.show(400);
				diagRow.find('.tabDiv .select').removeClass("select");
				diagRow.find('.tabDiv a[targetcls=itemView]').addClass("select");
				QueryCommodity.commodityDiv.createItemView($('.liTargetDiv').first(), numIid);
				
				TM.Loading.init.hidden();
			}
		});
	}
}, QueryCommodity.query);


QueryCommodity.commodityDiv = QueryCommodity.commodityDiv || {};
QueryCommodity.commodityDiv = $.extend({
	createDiv: function(DiagResult) {
		DiagResult.new_alipay_trade_amt = new Number(DiagResult.alipay_trade_amt).toFixed(2);
		DiagResult.tranrate = parseInt(DiagResult.uv) == 0 ? "0.00%" : new Number(DiagResult.alipay_winner_num / DiagResult.uv).toPercent(2);
		DiagResult.clickRate = parseInt(DiagResult.impression) == 0 ? "0.00%" : new Number(DiagResult.click / DiagResult.impression).toPercent(2);
		var tmpl = $('#diagitem').tmpl(
			[DiagResult]
		);
		return tmpl;
	},
    createTitleDiv: function(DiagResult, pageData,data, offset) {
        
    },
    createImgDiv: function(DiagResult) {
        
    },
    createSpanObj: function(DiagResult) {
        
    },
    createBtn:function(DiagResult, pageData,data, offset){
        
    },
	createDetail: function(DiagResult) {
		var diagtr = $('<tr class="diagtr hidden"  numiid="' + DiagResult.numIid + '">' +
			'<td colspan="20" style="border-top: 0; border-bottom: 0;">' +
			'<div class="tabDiv" style="width: 100%; margin: 20px auto 10px auto;">' +
			'	<ul class="clearfix" iid="'+DiagResult.numIid+'">' +
			'		<li>' +
			'			<a style="width: 100px; margin-left: 15px;" targetcls="itemView" href="javascript:void(0);">宝贝效果分析</a>' +
			'		</li>' +
			'		<li>' +
			'			<a style="width: 100px;" targetcls="uvpv" href="javascript:void(0);" class="">流量来源</a>' +
			'		</li>' +
			'		<li>' +
			'			<a style="width: 100px;" targetcls="appComeInWords" href="javascript:void(0);" class="select">无线入店关键词</a>' +
			'		</li>' +
			'		<li>' +
			'			<a style="width: 100px;" targetcls="comeInWords" href="javascript:void(0);" class="">PC入店关键词</a>' +
			'		</li>' +
			'		<li>' +
			'			<a style="width: 140px;" targetcls="searchAnalysis" href="javascript:void(0);" class="">7天单品搜索数据分析</a>' +
			'		</li>' +
			'	</ul>' +
			'</div>' +
			'<div class="liTargetDiv">' +
			'' +
			'</div>' +
			'</td>' +
			'</tr>');
		diagtr.find('.tabDiv a').click(function(){
			var selectTargetCls = diagtr.find('.tabDiv .select').attr('targetcls');
			var targetcls = $(this).attr("targetcls");
			if(selectTargetCls == targetcls){
				return;
			}
			diagtr.find('.tabDiv .select').removeClass("select");
			$(this).addClass("select");
			switch (targetcls) {
				case "itemView" :
					QueryCommodity.commodityDiv.createItemView(diagtr.find('.liTargetDiv'), DiagResult.numIid);break;
				case "uvpv" :
					QueryCommodity.commodityDiv.createUvPv(diagtr.find('.liTargetDiv'), DiagResult.numIid);break;
				case "appComeInWords" :
					QueryCommodity.commodityDiv.createAppComeInWords(diagtr.find('.liTargetDiv'), DiagResult.numIid);break;
				case "comeInWords" :
					QueryCommodity.commodityDiv.createComeInWords(diagtr.find('.liTargetDiv'), DiagResult.numIid);break;
				case "searchAnalysis" :
					QueryCommodity.commodityDiv.createSearchAnalysis(diagtr.find('.liTargetDiv'), DiagResult.numIid);break;
				default :
					QueryCommodity.commodityDiv.createItemView(diagtr.find('.liTargetDiv'), DiagResult.numIid);break;
			}
		});
		return diagtr;
	},
    createItemTitleAnalysis : function(index, targetDiv, id){
    	
    },
    createItemViewTrade : function(targetDiv, id){
        
    },
	createItemView: function(targetDiv, id){
		targetDiv.empty();
		var itemView = $('.itemViewTmp').clone();
		itemView.removeClass('itemViewTmp');

		var interval = (new Date().getTime() - parseInt(TM.firstLoginTime)) / (24 * 60 * 60 * 1000);
		if(interval < 1) {
			$('.searchDiagTip').show();
			return;
		} else if (interval < 3 ) {
			interval = 1;
		} else if (interval < 7) {
			interval = 3;
		} else {
			interval = 7;
		}
		
		itemView.find(".interval[value='" + interval + "']").attr('checked', true);
		itemView.show();

		itemView.find('.interval').attr("name", "interval");
		itemView.find('.startTimeInput').attr("id", "startTimeInput");

		itemView.find('.data_show').attr('id', 'data_show_' + id);
		targetDiv.append(itemView);
		$('#data_show_'+id).find('#view_item_show').empty();
		QueryCommodity.Event.setItemViewEvent(itemView, id);
	},
    createRelativeAccess : function(index,targetDiv, id){
        
    },
    createComeInWords : function(targetDiv, id){
		targetDiv.empty();
		var comeInWords = $('.comeInWordsTmp').clone();
		comeInWords.removeClass('comeInWordsTmp');

		var interval = (new Date().getTime() - parseInt(TM.firstLoginTime)) / (24 * 60 * 60 * 1000);
		if(interval < 1) {
			$('.searchDiagTip').show();
			return;
		} else if (interval < 3 ) {
			interval = 1;
		} else if (interval < 7) {
			interval = 3;
		} else {
			interval = 7;
		}

		comeInWords.find(".interval[value='" + interval + "']").attr('checked', true);
		comeInWords.show();

		comeInWords.find('.interval').attr("name", "interval");
		comeInWords.find('.startTimeInput').attr("id", "startTimeInput");

		targetDiv.append(comeInWords);
		QueryCommodity.Event.setComeInWordsDivEvent(comeInWords, id, false);
    },
    createAppComeInWords : function(targetDiv, id){
		targetDiv.empty();
		var comeInWords = $('.appComeInWordsTmp').clone();
		comeInWords.removeClass('appComeInWordsTmp');

		var interval = (new Date().getTime() - parseInt(TM.firstLoginTime)) / (24 * 60 * 60 * 1000);
		if(interval < 1) {
			$('.searchDiagTip').show();
			return;
		} else if (interval < 3 ) {
			interval = 1;
		} else if (interval < 7) {
			interval = 3;
		} else {
			interval = 7;
		}

		comeInWords.find(".interval[value='" + interval + "']").attr('checked', true);
		comeInWords.show();

		comeInWords.find('.interval').attr("name", "interval");
		comeInWords.find('.startTimeInput').attr("id", "startTimeInput");

		targetDiv.append(comeInWords);
		QueryCommodity.Event.setComeInWordsDivEvent(comeInWords, id, true);
    },
    createWirelessuvpv : function(index, targetDiv, id){
        
    },
    createUvPv : function(targetDiv, id){
		targetDiv.empty();
		var srcUvPv = $('.SrcUvPvTmp').clone();
		srcUvPv.removeClass("SrcUvPvTmp");

		var interval = (new Date().getTime() - parseInt(TM.firstLoginTime)) / (24 * 60 * 60 * 1000);
		if(interval < 1) {
			$('.searchDiagTip').show();
			return;
		} else if (interval < 3 ) {
			interval = 1;
		} else if (interval < 7) {
			interval = 3;
		} else {
			interval = 7;
		}

		srcUvPv.find(".interval[value='" + interval + "']").attr('checked', true);
		srcUvPv.show();

		srcUvPv.find('.interval').attr("name", "srcinterval");
		srcUvPv.find('.startTimeInput').attr("id", "srcstartTimeInput");
		targetDiv.append(srcUvPv);
		QueryCommodity.Event.setSrcUvPvDivEvent(srcUvPv, id);
    },
	createSearchAnalysis : function(targetDiv, id){
		targetDiv.empty();
		var searchAnalysis = $('.searchAnalysisTmp').clone();
		searchAnalysis.removeClass('searchAnalysisTmp');

		searchAnalysis.show();

		searchAnalysis.find('.data_show').attr('id', 'data_show_' + id);
		targetDiv.append(searchAnalysis);
		$('#data_show_'+id).find('#view_item_show').empty();
		QueryCommodity.commodityDiv.searchAnalysisDiag(searchAnalysis.find('.diag-result-div'), numIid);
	},
    itemWirelessPCViewTrade : function(targetDiv, numIid, interval, endTime, platform){
        
    },
    refreshShopHighCharts : function(map, numIid){
        
    },
	searchAnalysisDiag: function(targetDiv, numIid){
		var html = '';
		html += '<tr class="search-analysis-diag-result-table-th" style="height: 60px;">'+
		'<td class="impression"> ~ </td>'+
		'<td class="click"> ~ </td>'+
		'<td class="pv"> ~ </td>'+
		'<td class="uv"> ~ </td>'+
		'<td class="alipay_trade_num"> ~ </td>'+
		'<td class="alipay_winner_num"> ~ </td>'+
		'<td class="new_alipay_trade_amt"> ~ </td>'+
		'<td class="tranrate"> ~ </td>'+
		'<td class="clickRate"> ~ </td></tr>';

		$('#data_show_'+numIid).find('#view_item_show').append(html);

		$.ajax({
			url: '/Diag/singleItemInfo',
			data: {numIid : numIid},
			type: 'GET',
			global: false,
			success: function(data){
				if(!data.isOk) {
					TM.Alert.load(data.msg);
				}

				var itemJson = data.res;
				var targetTr = $('#data_show_'+numIid).find('#view_item_show');

				itemJson.new_alipay_trade_amt = new Number(itemJson.alipay_trade_amt).toFixed(2);
				itemJson.tranrate = parseInt(itemJson.uv) == 0 ? "0.00%" : new Number(itemJson.alipay_winner_num / itemJson.uv).toPercent(2);
				itemJson.clickRate = parseInt(itemJson.impression) == 0 ? "0.00%" : new Number(itemJson.click / itemJson.impression).toPercent(2);

				targetTr.find('.impression').html(itemJson.impression);
				targetTr.find('.click').html(itemJson.click);
				targetTr.find('.pv').html(itemJson.pv);
				targetTr.find('.uv').html(itemJson.uv);
				targetTr.find('.alipay_trade_num').html(itemJson.alipay_trade_num);
				targetTr.find('.alipay_winner_num').html(itemJson.alipay_winner_num);
				targetTr.find('.new_alipay_trade_amt').html(itemJson.new_alipay_trade_amt);
				targetTr.find('.tranrate').html(itemJson.tranrate);
				targetTr.find('.clickRate').html(itemJson.clickRate);
			}
		});
	},
    itemViewDiag: function(targetDiv, numIid, interval, endTime){
		if(interval == null) {
			interval = 7;
		}
		if(endTime == null) {
			endTime = new Date().getTime();
		}
		$('#data_show_'+numIid).find('#view_item_show').empty();
		var time = endTime;
		var html = '';
		for(var i = interval - 1; i>=0; i--){
			var temp = new Date(time - (24*60*60*1000) * i);
			var year = temp.getFullYear().toString();
			var month = (temp.getMonth()+1).toString();
			var day = temp.getDate().toString();
			if(month.length == 1){
				month = '0' + month;
			}
			if(day.length == 1){
				day = '0' + day;
			}
			var week = temp.getDay();
			var weekHtml = '';
			if(week == 6){
				weekHtml = '<span style="color:red;">（周六）</span>';
			} else if (week == 0){
				weekHtml = '<span style="color:red;">（周日）</span>';
			} else {
				weekHtml = "（周" + "p一二三四五".split("")[temp.getDay()] + "）";
			}
			html += '<tr class="app-word-diag-result-table-th '+ year+month+day +'">'+
			'<td class="dataTime">'+ month+day+ weekHtml +'</td>'+
			'<td class="pv"> ~ </td>'+
			'<td class="uv"> ~ </td>'+
			'<td class="alipayTradeAmt"> ~ </td>'+
			'<td class="alipayAuctionNum"> ~ </td>'+
			'<td class="alipayTradeNum"> ~ </td>'+
			'<td class="tradeRate"> ~ </td>'+
			'<td class="entranceNum"> ~ </td>'+
			'<td class="itemCollectNum"> ~ </td>'+
			'<td class="itemCollectNumPer"> ~ </td>'+
			'<td class="itemCartNum"> ~ </td>'+
			'<td class="itemCartNumPer"> ~ </td>'+
			'<td class="searchUv"> ~ </td>'+
			'<td class="searchClickRate"> ~ </td></tr>';
		}
		$('#data_show_'+numIid).find('#view_item_show').append(html);
		// 宝贝效果分析
		$.ajax({
			url: '/Diag/getItemPlayViewTrade',
			data: {numIid:numIid, interval:interval, endTime:endTime},
			type: 'POST',
			global: false,
			success: function(data){
				if(data === undefined || data == null) {
					return;
				}
				if(data.success != undefined && !data.success){
					return;
				}
				QueryCommodity.commodityDiv.getItemInfo(numIid, interval, endTime);
				var target = $('#data_show_'+numIid).find('#view_item_show');
				$.each(data, function(key, values){
					var targetTr = target.find('.' + key);
					targetTr.find('.pv').html(values.pv);
					targetTr.find('.uv').html(values.uv);
					targetTr.find('.alipayTradeAmt').html(Math.round(values.alipayTradeAmt * 100)/100);
					targetTr.find('.alipayAuctionNum').html(values.alipayAuctionNum);
					targetTr.find('.alipayTradeNum').html(values.alipayTradeNum);
					targetTr.find('.tradeRate').html(values.tradeRate);
				});
			}
		});
		$.ajax({
			url: '/Diag/getSearchUV',
			data: {numIid:numIid, interval:interval, endTime:endTime},
			type: 'POST',
			global: false,
			success: function(data){
				if(data === undefined || data == null) {
					return;
				}
				if(data.success != undefined && !data.success){
					return;
				}
				QueryCommodity.commodityDiv.getImpression(numIid, interval, endTime);
				var target = $('#data_show_'+numIid).find('#view_item_show');
				$.each(data, function(key, values){
					var targetTr = target.find('.' + key);
					targetTr.find('.searchUv').html(values.searchUv);
				});
			}
		});
		$.ajax({
			url: '/Diag/getEntranceNum',
			data: {numIid:numIid, interval:interval, endTime:endTime},
			type: 'POST',
			global: false,
			success: function(data){
				if(data === undefined || data == null) {
					return;
				}
				if(data.success != undefined && !data.success){
					return;
				}
				var target = $('#data_show_'+numIid).find('#view_item_show');
				$.each(data, function(key, values){
					var targetTr = target.find('.' + key);
					targetTr.find('.entranceNum').html(values.entranceNum);
				});
			}
		});
	},
	// 宝贝效果分析里的收藏数和加购数
	getItemInfo: function(numIid, interval, endTime){
		$.ajax({
			url: '/Diag/getItemCollectNum',
			data: {numIid:numIid, interval:interval, endTime:endTime},
			type: 'POST',
			global: false,
			success: function(data){
				if(data === undefined || data == null) {
					return;
				}
				if(data.success != undefined && !data.success){
					return;
				}
				var target = $('#data_show_'+numIid).find('#view_item_show');
				$.each(data, function(key, values){
					var targetTr = target.find('.' + key);
					var itemCollectNum = values.itemCollectNum;
					targetTr.find('.itemCollectNum').html(itemCollectNum);
					var uv = targetTr.find('.uv').html();
					var itemCollectNumPer = parseInt(uv) == 0 ? '0.00%' : new Number(itemCollectNum / uv).toPercent(2);
					targetTr.find('.itemCollectNumPer').html(itemCollectNumPer);
				});
			}
		});
		$.ajax({
			url: '/Diag/getItemCartNum',
			data: {numIid:numIid, interval:interval, endTime:endTime},
			type: 'POST',
			global: false,
			success: function(data){
				if(data === undefined || data == null) {
					return;
				}
				if(data.success != undefined && !data.success){
					return;
				}
				var target = $('#data_show_'+numIid).find('#view_item_show');
				$.each(data, function(key, values){
					var targetTr = target.find('.' + key);
					var itemCartNum = values.itemCartNum;
					targetTr.find('.itemCartNum').html(itemCartNum);
					var uv = targetTr.find('.uv').html();
					var itemCartNumPer = parseInt(uv) == 0 ? '0.00%' : new Number(itemCartNum / uv).toPercent(2);
					targetTr.find('.itemCartNumPer').html(itemCartNumPer);
				});
			}
		});
	},
    getImpression: function(numIid, interval, endTime){
		// 获得宝贝的展现量
		$.ajax({
			url: '/Diag/getImpression',
			data: {numIid:numIid, interval:interval, endTime:endTime},
			type: 'POST',
			global: false,
			success: function(data){
				if(data === undefined || data == null) {
					return;
				}
				if(data.success != undefined && !data.success){
					return;
				}
				var target = $('#data_show_'+numIid).find('#view_item_show');
				$.each(data, function(key, values){
					var targetTr = target.find('.' + key);
					var impression = parseInt(values.impression);
					var searchUv = parseInt(targetTr.find('.searchUv').html());
					if(impression == 0){
						targetTr.find('.searchClickRate').html('0.00%');
						return;
					}
					var searchRate = parseInt(searchUv) == 0 ? '0.00%' : new Number(searchUv / impression).toPercent(2);
					targetTr.find('.searchClickRate').html(searchRate);
				});
			}
		});
    },
    relativeAccessDiag : function(targetDiv, numIid, interval, endTime){
        
    },
    itemTitleAnalysisDiag : function(targetDiv, numIid, sortBy, isDesc){
        
    },
    itemWordDiag : function(targetDiv, numIid, interval, endTime, orderBy) {
		if(interval == null) {
			interval = 7;
		}
		if(endTime == null) {
			endTime = new Date().getTime();
		}
		if(orderBy === undefined || orderBy == null) {
			orderBy = "impression";
		}
		var table = targetDiv.find('.word-diag-result-table');
		table.find('tbody .word-tr').remove();
		table.find('tbody').append('<tr class="loading"><td colspan="9"><img src="/img/promoteimages/loading.gif?_v=01" width="28px" height="28"/></td></tr>');
		targetDiv.parent().find('.word-diag-paging').tmpage({
			currPage: 1,
			pageSize: 10,
			pageCount: 1,
			ajax: {
				on: true,
				param: {numIid:numIid, interval:interval, endTime:endTime, orderBy : orderBy},
				dataType: 'json',
				url: '/Diag/diagItem',
				global: false,
				callback: function(data){
					table.find('tbody .loading').remove();
					if(data === undefined || data == null) {
						return;
					}
					if(data.success == false) {
						return;
					}
					if(data.res == null){
						return;
					}
					table.find('tbody .word-tr').remove();
					var totalUv = 0, totalTrade = 0;
					if(data.res.length > 0) {
						$(data.res).each(function(i, word){
							table.find('tr.no-data').remove();
							var tranrate = parseInt(word.uv) == 0 ? "0.00%" : new Number(word.alipay_winner_num / word.uv).toPercent(2);
//							var clickRate = parseInt(word.pv) == 0 ? "0.00%" : new Number(word.click / word.pv).toPercent(2);
							table.find('tbody').append($('<tr class="word-tr"><td class="word-td">'+word.word+'</td><td>'+word.pv+'</td><td>'+word.uv+'</td>' +
							'<td>'+word.alipay_winner_num+'</td><td>'+word.alipay_auction_num+'</td><td>'+new Number(word.alipay_trade_amt).toFixed(2)+'</td><td>'+tranrate+'</td></tr>'))
						});
					} else {
						table.find('tr.no-data').remove();
						table.find('tbody').append($('<tr class="word-tr no-data" style="height: 45px;"><td colspan="20">此时段暂无数据</td></tr>'));
					}

					if(data.msg != null) {
						var totalAtt = data.msg.split(",");
						var totalUv = parseInt(totalAtt[0]);
						var totalTrade = parseInt(totalAtt[1]);
						$('.search-trade-tranrate').text(totalUv == 0 ? "0%" : new Number(totalTrade / totalUv).toPercent(2));
					} else {
						$('.search-trade-tranrate').text("0.00%");
					}

				}
			}
		});
		targetDiv.append(table);
    },
    appItemWordDiag : function(targetDiv, numIid, interval, endTime, orderBy) {
		if(interval == null) {
			interval = 7;
		}
		if(endTime == null) {
			endTime = new Date().getTime();
		}
		if(orderBy === undefined || orderBy == null) {
			orderBy = "impression";
		}
		var table = targetDiv.find('.app-word-diag-result-table');
		table.find('tbody .word-tr').remove();
		table.find('tbody').append('<tr class="loading"><td colspan="9"><img src="/img/promoteimages/loading.gif?_v=01" width="28px" height="28"/></td></tr>');
		targetDiv.parent().find('.app-word-diag-paging').tmpage({
			currPage: 1,
			pageSize: 10,
			pageCount: 1,
			ajax: {
				on: true,
				param: {numIid:numIid, interval:interval, endTime:endTime, orderBy : orderBy},
				dataType: 'json',
				global: false,
				url: '/Diag/diagAppItem',
				callback: function(data){
					table.find('tbody .loading').remove();
					if(data === undefined || data == null) {
						return;
					}
					if(data.success == false) {
						return;
					}
					if(data.res == null){
						return;
					}
					table.find('tbody .word-tr').remove();
					var totalUv = 0, totalTrade = 0;
					if(data.res.length > 0) {
						$(data.res).each(function(i, word){
							var tranrate = parseInt(word.uv) == 0 ? "0.00%" : new Number(word.alipay_winner_num / word.uv).toPercent(2);
							table.find('tbody').append($('<tr class="word-tr"><td class="word-td">'+word.word+'</td><td>'+word.pv+'</td><td>'+word.uv+'</td>' +
							'<td>'+word.alipay_winner_num+'</td><td>'+word.alipay_auction_num+'</td><td>'+new Number(word.alipay_trade_amt).toFixed(2)+'</td><td>'+tranrate+'</td></tr>'))
						});
					} else {
						table.find('tr.no-data').remove();
						table.find('tbody').append($('<tr class="word-tr no-data" style="height: 45px;"><td colspan="20">此时段暂无数据</td></tr>'));
					}

					if(data.msg != null) {
						var totalAtt = data.msg.split(",");
						var totalUv = parseInt(totalAtt[0]);
						var totalTrade = parseInt(totalAtt[1]);
						$('.search-trade-tranrate').text(totalUv == 0 ? "0%" : new Number(totalTrade / totalUv).toPercent(2));
					} else {
						$('.search-trade-tranrate').text("0.00%");
					}

				}
			}
		});
		targetDiv.append(table);
		// 给导出数据按钮添加点击事件
		$("[name='export_excel']").unbind('click').bind("click", function(){
			var numIid = $(this).parent().parent().parent().parent().parent().parent().attr('numiid');
			var interval = $(this).parent().find("input:radio:checked").val();
			QueryCommodity.commodityDiv.exportExcelClick(numIid, interval, $(this));
		});
    },
    wirelessSrcUvPvDiag: function(targetDiv, numIid, interval, endTime){
		var table = targetDiv.find('.src-uvpv-result-table');
		var srcTbody = table.find('.wireless-src-uvpv');
		// 判断是否展开
		var srcIds = new Array();
		srcTbody.find('.isFirdtSrcLevel').find('.showChildren').each(function(){
			if($(this).html() == '收起明细'){
				var srcId = $(this).parent().parent().attr('srcid');
				srcIds.push(srcId);
			}
		});
		srcTbody.empty();
		srcTbody.append('<tr><td colspan="9"><img src="/img/promoteimages/loading.gif?_v=01" width="28px" height="28px;"/></td></tr>');
		$.ajax({
			url: '/Diag/getWireelessItemSource',
			data: {numIid:numIid, interval:interval, endTime:endTime},
			type: 'POST',
			global: false,
			success: function(data){
				srcTbody.empty();
				if(data === undefined || data == null)
					return;
				if(!data.success)
					return;
				// 计算无线端流量的总和
				var uv = 0;
				var pv = 0;
				var alipay_trade_num = 0;
				var alipay_winner_num = 0;
				var alipay_trade_amt = 0;
				$.each(data.res, function(index, item){
					if(parseInt(item.srcLevel) != 2){
						return true;
					}
					uv += parseInt(item.uv);
					pv += parseInt(item.pv);
					alipay_trade_num += parseInt(item.alipayTradeNum);
					alipay_winner_num += parseInt(item.alipayWinnerNum);
					alipay_trade_amt += parseInt(item.alipayTradeAmt);
				});
				var sumTranrate = uv == 0 ? "0.00%" : new Number(alipay_winner_num / uv).toPercent(2);
				var tempHtml = '<tr style="font-weight: bold;background-color: rgb(246, 250, 255);font-size: 16px;">' +
					'<td align="left" style="width: 25%;"><span style="padding-left: 6px;">无线端流量来源</span></td>'+
					'<td style="width: 15%;"> ~ </td><td>'+ uv +'</td>'+
					'<td style="width: 10%;">'+ pv +'</td><td style="width: 10%;">'+ alipay_trade_num +'</td>'+
					'<td style="width: 10%;">'+ alipay_winner_num +'</td><td style="width: 10%;">'+ sumTranrate +'</td>'+
					'<td style="width: 10%;">￥'+ new Number(alipay_trade_amt).toFixed(2) +'</td>'+
					'<td style="width: 10%;"><span class="tmbtn sky-blue-btn showWireless open">收起明细</span></td></tr>'
				srcTbody.append(tempHtml);

				$.each(data.res, function(index, item){
					// 计算转化率
					var srcLevel = parseInt(item.srcLevel);
					var hasParend = srcLevel == 2;
					var img = hasParend ? '<span class="fold-class-wrapper"><img class="fold-class" src="/img/promoteimages/iconfont-jiahao.png"></span>' : '';
					var isFirdtSrcLevel = hasParend ? "isFirdtSrcLevel" : 'isNotFirdtSrcLevel hidden';
					var srcName = hasParend ? '<span style="padding-left: 6px; CUrsor: pointer" class="fold-wrapper">'+ item.srcName +'</span>' : '<span style="padding-left: 40px;" class="src_name">' + item.srcName +'</span>';
					var operate = hasParend ? "<span class='tmbtn sky-blue-btn showChildren open'>展开明细</span>": "<span class='tmbtn short-green-btn showWirelessDetail'>查看趋势</span>";
					var per = parseInt(item.uv) == 0 ? "0.00%" : new Number(item.alipayWinnerNum / item.uv).toPercent(2);
					var tempHtml = '<tr class="wireless '+ isFirdtSrcLevel +'" parentSrcId="'+ item.parentSrcId +'" srcId="'+ item.srcId +'">'+
						'<td align="left" style="width: 25%;"><span style="padding-left: 12%;">'+ img + '&nbsp;&nbsp;' + srcName +'</span></td>'+
						'<td>'+ item.parentSrcName +'</td>'+
						'<td>'+ item.uv +'</td>'+
						'<td>'+ item.pv +'</td>'+
						'<td>'+ item.alipayTradeNum +'</td>'+
						'<td>'+ item.alipayWinnerNum +'</td>'+
						'<td>'+ per +'</td>'+
						'<td>￥'+ new Number(item.alipayTradeAmt).toFixed(2) +'</td>'+
						'<td>'+ operate +'</td></tr>'
					srcTbody.append(tempHtml);
				});
				// 查看趋势按钮点击事件
				$(".showWirelessDetail").unbind().click(function(){
					var srcId = $(this).parent().parent().attr("srcid");
					var srcName = $(this).parent().parent().find(".src_name").text();
					var days = 30;
					$.post("/Diag/showWirelessDetail", {numIid:numIid, srcId:srcId, days:days}, function(data){
						if(!data.success) {
							TM.Alert.load(data.message);
							return false;
						}
						var dataJsonArray = data.res;
						var interval = dataJsonArray.length;
						var titleStr = '无线端' + srcName + '来源的最近' + interval + '天效果数据'
						QueryCommodity.rule.drawChart(dataJsonArray, interval, titleStr);
					});
				});
				// 上次是展开的这次依然展开
				$(srcIds).each(function(i, item){
					var level = srcTbody.find('.isFirdtSrcLevel[srcid="'+ item +'"]');
					level.find('img').attr('src', '/img/promoteimages/iconfont-jianhao.png');
					level.find('.showChildren ').html('收起明细');
					srcTbody.find('.isNotFirdtSrcLevel[parentSrcId="'+ item +'"]').show();
				});
				// 二级目录是展开 收起按钮
				srcTbody.find('.showChildren').unbind('click').click(function(){
					var SrcId = $(this).parent().parent().attr("srcId");
					if($(this).text() == "展开明细") {
						$(this).html("收起明细");
						$(this).parent().parent().find('.fold-class').attr('src', '/img/promoteimages/iconfont-jianhao.png');
						table.find('.isNotFirdtSrcLevel[parentSrcId="'+SrcId+'"]').show();
						table.find('.levelThree').show();
					} else {
						$(this).html("展开明细");
						$(this).parent().parent().find('.fold-class').attr('src', '/img/promoteimages/iconfont-jiahao.png');
						table.find('.isNotFirdtSrcLevel[parentSrcId="'+SrcId+'"]').hide();
						table.find('.levelThree').hide();
					}
				});
				// 无线流量来源  展开  收起按钮点击事件
				srcTbody.find('.showWireless').unbind('click').click(function(){
					if($(this).text() == "展开明细") {
						$(this).html("收起明细");
						srcTbody.find('.fold-class').attr('src', '/img/promoteimages/iconfont-jiahao.png')
						srcTbody.find('.isFirdtSrcLevel').show();
					} else {
						$(this).html("展开明细");
						srcTbody.find('.fold-class').attr('src', '/img/promoteimages/iconfont-jianhao.png')
						srcTbody.find('.wireless').find('.showChildren').html('展开明细');
						srcTbody.find('.wireless').hide();
					}
				});
				// 加号减号点击事件
				srcTbody.find('.fold-class-wrapper').unbind("click").click(function(){
					var img = $(this).find('img');
					if(img.attr("src") == "/img/promoteimages/iconfont-jianhao.png") {
						img.attr("src", "/img/promoteimages/iconfont-jiahao.png");
						var srcId = $(this).parent().parent().parent().attr("srcid");
						table.find('tr[parentsrcid="'+srcId+'"]').hide();
						table.find('tr[parentsrcid="'+srcId+'"]').each(function(){
							var srcId2Level = $(this).attr('srcid');
							table.find('tr[parentsrcid="'+srcId2Level+'"]').hide();
						});
						$(this).parent().parent().parent().find('.showChildren').html('展开明细');
					} else {
						img.attr("src", "/img/promoteimages/iconfont-jianhao.png");
						var srcId = $(this).parent().parent().parent().attr("srcid");
						table.find('tr[parentsrcid="'+srcId+'"]').show();
						table.find('tr[parentsrcid="'+srcId+'"]').each(function(){
							var srcId2Level = $(this).attr('srcid');
							table.find('tr[parentsrcid="'+srcId2Level+'"]').show();
						});
						$(this).parent().parent().parent().find('.showChildren').html('收起明细');
					}
				});
				// 点击文字 展开  收起
				srcTbody.find('.fold-wrapper').unbind('click').click(function(){
					var img = $(this).prev().find('img');
					if(img.attr("src") == "/img/promoteimages/iconfont-jianhao.png") {
						img.attr("src", "/img/promoteimages/iconfont-jiahao.png");
						var srcId = $(this).parent().parent().parent().attr("srcid");
						table.find('tr[parentsrcid="'+srcId+'"]').hide();
						table.find('tr[parentsrcid="'+srcId+'"]').each(function(){
							var srcId2Level = $(this).attr('srcid');
							table.find('tr[parentsrcid="'+srcId2Level+'"]').hide();
						});
						$(this).parent().parent().parent().find('.showChildren').html('展开明细');
					} else {
						img.attr("src", "/img/promoteimages/iconfont-jianhao.png");
						var srcId = $(this).parent().parent().parent().attr("srcid");
						table.find('tr[parentsrcid="'+srcId+'"]').show();
						table.find('tr[parentsrcid="'+srcId+'"]').each(function(){
							var srcId2Level = $(this).attr('srcid');
							table.find('tr[parentsrcid="'+srcId2Level+'"]').show();
						});
						$(this).parent().parent().parent().find('.showChildren').html('收起明细');
					}
				});
			}
		});
    },
    itemSrcUvPvDiag : function(targetDiv, numIid, interval, endTime) {
		if(interval == null) {
			interval = 7;
		}
		if(endTime == null) {
			endTime = new Date().getTime();
		}
		var table = targetDiv.find('.src-uvpv-result-table');
		var srcTbody = table.find('.src-uvpv');
		// 判断是否展开
		var srcIds = new Array();
		srcTbody.find('.isFirdtSrcLevel').find('.showChildren').each(function(){
			if($(this).html() == '收起明细'){
				var srcId = $(this).parent().parent().attr('srcid');
				srcIds.push(srcId);
			}
		});
		srcTbody.empty();
		srcTbody.append('<tr><td colspan="9"><img src="/img/promoteimages/loading.gif?_v=01" width="28px" height="28"/></td></tr>');
		$.ajax({
			url: '/Diag/getPCItemSource',
			data: {numIid:numIid, interval:interval, endTime:endTime},
			type: 'POST',
			global: false,
			success: function(data){
				srcTbody.empty();
				if(data === undefined || data == null)
					return;
				if(data.success == false)
					return;
				if(data == null)
					return;
				if(data.length > 0) {
					// 就算pc端所有流量的总和
					var uv = 0;
					var pv = 0;
					var alipay_trade_num = 0;
					var alipay_winner_num = 0;
					var alipay_trade_amt = 0;
					$(data).each(function(i, uvpv){
						if(parseInt(uvpv.parentSrcId) != 0){
							return true;
						}
						uv += parseInt(uvpv.uv);
						pv += parseInt(uvpv.pv);
						alipay_trade_num += parseInt(uvpv.alipay_trade_num);
						alipay_winner_num += parseInt(uvpv.alipay_winner_num);
						alipay_trade_amt += parseInt(uvpv.alipay_trade_amt);
					});
					var sumTranrate = uv == 0 ? "0.00%" : new Number(alipay_winner_num / uv).toPercent(2);
					table.find('.src-uvpv').append($('<tr class="src-uvpv-tr" style="font-size: 16px;font-weight: bold;background-color: rgb(246, 250, 255);">' +
					'<td style="width: 25%;" align="left"><span style="padding-left: 6px;">PC端流量来源</span></td>' +
					'<td style="width: 15%;"> ~ </td>' +
					'<td style="width: 10%;">'+ uv +'</td>' +
					'<td style="width: 10%;">'+ pv +'</td>' +
					'<td style="width: 10%;">'+ alipay_trade_num +'</td>' +
					'<td style="width: 10%;">'+ alipay_winner_num +'</td>' +
					'<td style="width: 10%;">'+ sumTranrate +'</td>' +
					'<td style="width: 10%;">￥'+new Number(alipay_trade_amt).toFixed(2)+'</td>' +
					'<td style="width: 10%;" class="operate"><span class="tmbtn sky-blue-btn showPC open">收起明细</span></td></tr>'));

					$(data).each(function(i, uvpv){
						table.find('tr.no-data').remove();
						var isFirdtSrcLevel = (parseInt(uvpv.parentSrcId) == 0) ? "isFirdtSrcLevel" : "isNotFirdtSrcLevel hidden";
						var img = (parseInt(uvpv.parentSrcId) == 0) ? '<sapn class="fold-class-wrapper"><img class="fold-class" src="/img/promoteimages/iconfont-jiahao.png"></span>' : '';
						var srcName = (parseInt(uvpv.parentSrcId) == 0) ? "<span style='CUrsor: pointer' class='fold-wrapper'>"+uvpv.srcName+"</span>" : '<span style="padding-left: 40px;" class="src_name">' + uvpv.srcName +'</span>';
						var operate = (parseInt(uvpv.parentSrcId) == 0) ? "<span class='tmbtn sky-blue-btn showChildren open'>展开明细</span>": "<span class='tmbtn short-green-btn showPCDetail'>查看趋势</span>";
						var tranrate = parseInt(uvpv.uv) == 0 ? "0.00%" : new Number(uvpv.alipay_winner_num / uvpv.uv).toPercent(2);
						var parentSrcName = (parseInt(uvpv.parentSrcId) == 0) ? "~" : uvpv.parentSrcName;
						table.find('.src-uvpv').append($('<tr srcId="'+uvpv.srcId+'" parentSrcId="'+uvpv.parentSrcId+'" class="pc src-uvpv-tr '+isFirdtSrcLevel+'">' +
						'<td style="width: 25%;" align="left"><span style="padding-left: 12%;">'+ img + '&nbsp;&nbsp;' + srcName+'</span></td>' +
						'<td style="width: 15%;">'+ parentSrcName +'</td>' +
						'<td style="width: 10%;">'+uvpv.uv+'</td>' +
						'<td style="width: 10%;">'+uvpv.pv+'</td>' +
						'<td style="width: 10%;">'+uvpv.alipay_trade_num+'</td>' +
						'<td style="width: 10%;">'+uvpv.alipay_winner_num+'</td>' +
						'<td style="width: 10%;">'+ tranrate +'</td>' +
						'<td style="width: 10%;">￥'+new Number(uvpv.alipay_trade_amt).toFixed(2)+'</td>' +
						'<td style="width: 10%;" class="operate">'+operate+'</td>' +
						'</tr>'))
					});
					// 查看趋势按钮点击事件
					$(".showPCDetail").unbind().click(function(){
						var srcId = $(this).parent().parent().attr("srcid");
						var srcName = $(this).parent().parent().find(".src_name").text();
						var days = 30;
						$.post("/Diag/showPCDetail", {numIid:numIid, srcId:srcId, days:days}, function(data){
							if(!data.success) {
								TM.Alert.load(data.message);
								return false;
							}
							var dataJsonArray = data.res;
							var interval = dataJsonArray.length;
							var titleStr = 'PC端' + srcName + '来源的最近' + interval + '天效果数据'
							QueryCommodity.rule.drawChart(dataJsonArray, interval, titleStr);
						});
					});
					// 上次是展开的这次依然展开
					$(srcIds).each(function(i, item){
						var level = srcTbody.find('.isFirdtSrcLevel[srcid="'+ item +'"]');
						level.find('img').attr('src', '/img/promoteimages/iconfont-jianhao.png');
						level.find('.showChildren ').html('收起明细');
						srcTbody.find('.isNotFirdtSrcLevel[parentSrcId="'+ item +'"]').show();
					});
					srcTbody.find('.showChildren').unbind('click').click(function(){
						var SrcId = $(this).parent().parent().attr("srcId");
						if($(this).text() == "展开明细") {
							$(this).html("收起明细");
							$(this).parent().parent().find('.fold-class').attr('src', "/img/promoteimages/iconfont-jianhao.png");
							table.find('.isNotFirdtSrcLevel[parentSrcId="'+SrcId+'"]').show();
						} else {
							$(this).html("展开明细");
							$(this).parent().parent().find('.fold-class').attr('src', "/img/promoteimages/iconfont-jiahao.png");
							table.find('.isNotFirdtSrcLevel[parentSrcId="'+SrcId+'"]').hide();
						}
					});
					// 点击PC端流量来源展开  收起按钮
					srcTbody.find('.showPC').unbind('click').click(function(){
						if($(this).text() == "展开明细") {
							$(this).html("收起明细");
							srcTbody.find('.pc[parentSrcId=0]').show();
							srcTbody.find('.fold-class').attr('src', "/img/promoteimages/iconfont-jiahao.png");
						} else {
							$(this).html("展开明细");
							srcTbody.find('.pc').find('.showChildren').html('展开明细');
							srcTbody.find('.pc').hide();
							srcTbody.find('.fold-class').attr('src', "/img/promoteimages/iconfont-jianhao.png");
						}
					});
					// 点击二级目录前面的加号
					srcTbody.find('.fold-class-wrapper').unbind("click").click(function(){
						var img = $(this).find('img');
						var trHtml = $(this).parent().parent().parent();
						if(img.attr("src") == "/img/promoteimages/iconfont-jianhao.png") {
							img.attr("src", "/img/promoteimages/iconfont-jiahao.png");
							var srcId = trHtml.attr("srcid");
							table.find('tr[parentsrcid="'+srcId+'"]').hide();
							trHtml.find('.showChildren').html('展开明细');
						} else {
							img.attr("src", "/img/promoteimages/iconfont-jianhao.png");
							var srcId = trHtml.attr("srcid");
							table.find('tr[parentsrcid="'+srcId+'"]').show();
							trHtml.find('.showChildren').html('收起明细');
						}
					});
					// 点击文字 展开  收起
					srcTbody.find('.fold-wrapper').unbind('click').click(function(){
						var img = $(this).parent().find('img');
						var trHtml = $(this).parent().parent();
						if(img.attr("src") == "/img/promoteimages/iconfont-jianhao.png") {
							img.attr("src", "/img/promoteimages/iconfont-jiahao.png");
							var srcId = trHtml.attr("srcid");
							table.find('tr[parentsrcid="'+srcId+'"]').hide();
							table.find('tr[parentsrcid="'+srcId+'"]').each(function(){
								var srcId2Level = $(this).attr('srcid');
								table.find('tr[parentsrcid="'+srcId2Level+'"]').hide();
							});
							trHtml.find('.showChildren').html('展开明细');
						} else {
							img.attr("src", "/img/promoteimages/iconfont-jianhao.png");
							var srcId = trHtml.attr("srcid");
							table.find('tr[parentsrcid="'+srcId+'"]').show();
							table.find('tr[parentsrcid="'+srcId+'"]').each(function(){
								var srcId2Level = $(this).attr('srcid');
								table.find('tr[parentsrcid="'+srcId2Level+'"]').show();
							});
							trHtml.find('.showChildren').html('收起明细');
						}
					});
				} else {
					table.find('tr.no-data').remove();
					table.find('.srcTbody').append($('<tr class="no-data" style="height: 45px;"><td colspan="20">此时段暂无数据</td></tr>'));
				}
			}
		});
		// 无线端流量来源
		QueryCommodity.commodityDiv.wirelessSrcUvPvDiag(targetDiv, numIid, interval, endTime);
		targetDiv.append(table);
    },
    exportExcelClick: function(numIid, interval, obj){
		var curr = new Date().getTime();
		var dayMillis = 24 * 3600 * 1000;
		var endTime = curr - dayMillis;
		if(interval == 0){
			var endTimeInput = obj.parent().find(".endTimeInput");
			endTime = parseInt(Date.parse(endTimeInput.val().toString().replace("-","/")));
			if(endTime > new Date().getTime()) {
				TM.Alert.load("截止时间请勿超过当前时间");
				endTimeInput.val(new Date(curr).formatYMS());
				return;
			}
			var startTimeInput = obj.parent().find(".startTimeInput");
			var startTime = parseInt(Date.parse(startTimeInput.val().toString().replace("-","/")));
			if(endTime < startTime) {
				TM.Alert.load("截止时间请勿小于开始时间");
				return;
			}
			interval = Math.floor((endTime - startTime) / dayMillis) + 1;
		}
		var form = $("<form>");   //定义一个form表单
		form.attr('style', 'display:none');   //在form表单中添加查询参数
		form.attr('method', 'get');
		form.attr('action', "/Diag/exportExcel");

		var numIidInput = $('<input>');
		numIidInput.attr('type', 'hidden');
		numIidInput.attr('name', 'numIid');
		numIidInput.attr('value', numIid);

		var intervalInput = $('<input>');
		intervalInput.attr('type', 'hidden');
		intervalInput.attr('name', 'interval');
		intervalInput.attr('value', interval);

		var endTimeInput = $('<input>');
		endTimeInput.attr('type', 'hidden');
		endTimeInput.attr('name', 'endTime');
		endTimeInput.attr('value', endTime);

		$('body').append(form);  //将表单放置在web中
		form.append(intervalInput);   //将查询参数控件提交到表单上
		form.append(numIidInput);
		form.append(endTimeInput);
		form.submit();
    }
}, QueryCommodity.commodityDiv);


QueryCommodity.Event = QueryCommodity.Event || {};
QueryCommodity.Event = $.extend({
    setItemViewTradeEvent : function(viewTradeDiv, numIid){
        
    },
    setItemViewEvent: function(itemView, numIid){
		var curr = new Date().getTime();
		var dayMillis = 24 * 3600 * 1000;
		var startTimeInput = itemView.find(".startTimeInput");
		var endTimeInput = itemView.find(".endTimeInput");
		startTimeInput.datepicker();
		startTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
		startTimeInput.val(new Date(curr - 7 * dayMillis).formatYMS());

		endTimeInput.datepicker();
		endTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
		endTimeInput.val(new Date(curr).formatYMS());

		startTimeInput.unbind('change').change(function(){
			itemView.find('.interval-tr .interval[value="0"]').trigger("click");
		});
		endTimeInput.unbind('change').change(function(){
			itemView.find('.interval-tr .interval[value="0"]').trigger("click");
		});

		itemView.find('.interval-tr .interval').unbind('click').click(function(){
			var endTime, interval;
			var val = parseInt($(this).val());
			switch (val) {
				case 1 :
					endTime = curr - dayMillis;
					interval = 1;
					break;
				case 3 :
					endTime = curr - dayMillis;
					interval = 3;
					break;
				case 7 :
					endTime = curr - dayMillis;
					interval = 7;
					break;
				case 14 :
					endTime = curr - dayMillis;
					interval = 14;
					break;
				case 0 :
					endTime = parseInt(Date.parse(endTimeInput.val().toString().replace("-","/")));
					if(endTime > new Date().getTime()) {
						TM.Alert.load("截止时间请勿超过当前时间");
						endTimeInput.val(new Date(curr).formatYMS());
						return;
					}
					var startTime = parseInt(Date.parse(startTimeInput.val().toString().replace("-","/")));
					if(endTime < startTime) {
						TM.Alert.load("截止时间请勿小于开始时间");
						return;
					}
					interval = Math.floor((endTime - startTime) / dayMillis) + 1;
					break;
				default :
					endTime = curr - dayMillis;
					interval = 7;
					break;
			}
			QueryCommodity.commodityDiv.itemViewDiag(itemView.find('.diag-result-div'), numIid, interval, endTime);
		});
		// 点击input文字相当于点击input
		QueryCommodity.Event.setIntervalSpanClickEvent(itemView);
		itemView.find('.interval-tr .interval:checked').trigger("click");
    },
    setRelitiveAccessEvent : function(relativeAccessDiv, numIid, index){
        
    },
    setComeInWordsDivEvent : function(comeInWords, numIid, isApp) {
		if(isApp === undefined || isApp == null) {
			isApp == false;
		}
		var curr = new Date().getTime();
		var dayMillis = 24 * 3600 * 1000;
		var startTimeInput = comeInWords.find(".startTimeInput");
		var endTimeInput = comeInWords.find(".endTimeInput");
		startTimeInput.datepicker();
		startTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
		startTimeInput.val(new Date(curr - 7 * dayMillis).formatYMS());

		endTimeInput.datepicker();
		endTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
		endTimeInput.val(new Date(curr).formatYMS());

		startTimeInput.unbind('change').change(function(){
			comeInWords.find('.interval-tr .interval[value="0"]').trigger("click");
		});
		endTimeInput.unbind('change').change(function(){
			comeInWords.find('.interval-tr .interval[value="0"]').trigger("click");
		});

		comeInWords.find('.interval-tr .interval').unbind('click').click(function(){
			var endTime, interval;
			var val = parseInt($(this).val());
			switch (val) {
				case 1 :
					endTime = curr - dayMillis;
					interval = 1;
					break;
				case 3 :
					endTime = curr - dayMillis;
					interval = 3;
					break;
				case 7 :
					endTime = curr - dayMillis;
					interval = 7;
					break;
				case 14 :
					endTime = curr - dayMillis;
					interval = 14;
					break;
				case 0 :
					endTime = parseInt(Date.parse(endTimeInput.val().toString().replace("-","/")));
					if(endTime > new Date().getTime()) {
						TM.Alert.load("截止时间请勿超过当前时间");
						endTimeInput.val(new Date(curr).formatYMS());
						return;
					}
					var startTime = parseInt(Date.parse(startTimeInput.val().toString().replace("-","/")));
					if(endTime < startTime) {
						TM.Alert.load("截止时间请勿小于开始时间");
						return;
					}
					interval = Math.floor((endTime - startTime) / dayMillis) + 1;
					break;
				default :
					endTime = curr - dayMillis;
					interval = 7;
					break;
			}
			if(isApp == false) {
				QueryCommodity.commodityDiv.itemWordDiag(comeInWords.find('.diag-result-div'), numIid,
					interval, endTime);
			} else {
				QueryCommodity.commodityDiv.appItemWordDiag(comeInWords.find('.app-diag-result-div'), numIid,
					interval, endTime);
			}

		});

		// 点击input文字相当于点击input
		QueryCommodity.Event.setIntervalSpanClickEvent(comeInWords);

		comeInWords.find('.interval-tr .interval:checked').trigger("click");
    },
    setWirelessSrcUvPvDivEvent: function(wirelessSrcUvPv, numIid, index){
        
    },
    setSrcUvPvDivEvent : function(srcUvPv, numIid) {
		var curr = new Date().getTime();
		var dayMillis = 24 * 3600 * 1000;
		var startTimeInput = srcUvPv.find(".startTimeInput");
		var endTimeInput = srcUvPv.find(".endTimeInput");
		startTimeInput.datepicker();
		startTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
		startTimeInput.val(new Date(curr - 7 * dayMillis).formatYMS());

		endTimeInput.datepicker();
		endTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
		endTimeInput.val(new Date(curr).formatYMS());

		startTimeInput.unbind('change').change(function(){
			srcUvPv.find('.interval-tr .interval[value="0"]').trigger("click");
		});
		endTimeInput.unbind('change').change(function(){
			srcUvPv.find('.interval-tr .interval[value="0"]').trigger("click");
		});

		srcUvPv.find('.interval-tr .interval').unbind('click').click(function(){
			var endTime, interval;
			var val = parseInt($(this).val());
			switch (val) {
				case 1 :
					endTime = curr - dayMillis;
					interval = 1;
					break;
				case 3 :
					endTime = curr - dayMillis;
					interval = 3;
					break;
				case 7 :
					endTime = curr - dayMillis;
					interval = 7;
					break;
				case 14 :
					endTime = curr - dayMillis;
					interval = 14;
					break;
				case 0 :
					endTime = parseInt(Date.parse(endTimeInput.val().toString().replace("-","/")));
					if(endTime > new Date().getTime()) {
						TM.Alert.load("截止时间请勿超过当前时间");
						endTimeInput.val(new Date(curr).formatYMS());
						return;
					}
					var startTime = parseInt(Date.parse(startTimeInput.val().toString().replace("-","/")));
					if(endTime < startTime) {
						TM.Alert.load("截止时间请勿小于开始时间");
						return;
					}
					interval = Math.floor((endTime - startTime) / dayMillis) + 1;
					break;
				default :
					endTime = curr - dayMillis;
					interval = 7;
					break;
			}
			QueryCommodity.commodityDiv.itemSrcUvPvDiag(srcUvPv.find('.diag-result-div'), numIid,
				interval, endTime);
		});

		// 点击input文字相当于点击input
		QueryCommodity.Event.setIntervalSpanClickEvent(srcUvPv);

		srcUvPv.find('.interval-tr .interval:checked').trigger("click");
    },
    setIntervalSpanClickEvent: function(target){
		target.find('.interval-tr .interval-span').unbind("click").click(function(){
			var value = $(this).attr("value");
			target.find('..interval-tr .interval[value="'+value+'"]').trigger("click");
		});
    }
}, QueryCommodity.Event);


QueryCommodity.rule = QueryCommodity.rule || {};
QueryCommodity.rule = $.extend({
    getRuleJson: function() {
        
    },
    clearCookie : function(){
        
    },
    getPlatform : function(tarfetDiv){
        
    },
    getInterval : function(tarfetDiv){
        
    },
    getEndTime : function(tarfetDiv){
        
    },
	drawChart : function(dataJsonArray, interval, titleStr){
		// x轴显示间隔控制
		if(interval <= 10) {
			x = 1;
		} else if (interval <= 20) {
			x =2;
		} else {
			x = 3;
		}

		var dayArr = new Array();
		var uvArr = new Array();	// 访客数
		var pvArr = new Array();	// 浏览次数
		var gmvWinnerNumArr = new Array();	// 下单买家数
		var gmvRateArr = new Array();	// 下单转化率
		var alipayWinnerNumArr = new Array();	// 支付买家数
		var alipayAuctionNumArr = new Array();	// 支付件数
		var alipayRateArr = new Array();	// 支付转化率


		$(dataJsonArray).each(function(index, dataJson) {
			dayArr.push(dataJson.thedate);
			uvArr.push(parseInt(dataJson.uv));
			pvArr.push(parseInt(dataJson.pv));
			gmvWinnerNumArr.push(parseInt(dataJson.gmvWinnerNum));
			gmvRateArr.push(parseInt(dataJson.uv) == 0 ? parseFloat(0) : parseFloat((dataJson.gmvWinnerNum / dataJson.uv).toFixed(2)));
			alipayWinnerNumArr.push(parseInt(dataJson.alipayWinnerNum));
			alipayAuctionNumArr.push(parseInt(dataJson.alipayAuctionNum));
			alipayRateArr.push(parseInt(dataJson.uv) == 0 ? parseFloat(0) : parseFloat((dataJson.alipayWinnerNum / dataJson.uv).toFixed(2)));
		});

		var html = $('#detail-chart-tmpl').clone();
		$('#detail-chart').remove();
		html.attr("id", "detail-chart");
		html.show();
		$('body').append(html);

		// 数据统计(Highchart)
		chart = new Highcharts.Chart({
			chart : {
				renderTo : 'detail-chart',
				defaultSeriesType: 'line', //图表类型line(折线图)
				width: 969
			},
			credits : {
				enabled: false //右下角不显示LOGO
			},
			title: {
				text: titleStr //图表标题
			},
			xAxis: {  //x轴
				tickInterval: x,
				categories: dayArr, //x轴标签名称
				gridLineWidth: 0, //设置网格宽度为1
				lineWidth: 2,  //基线宽度
				labels:{
					y:20   //x轴标签位置：距X轴下方20像素
				}
			},
			yAxis: [
				{
					title: {
						text: '流量趋势'
					}
				},
				{
					opposite: true,
					title: {
						text: '转化效果'
					}
				}
			],
			legend: {
				layout: 'horizontal',
				align: 'center',
				verticalAlign: 'bottom',
				borderWidth: 0
			},
			series: [
				{  //数据列
					name: '访客数',
					data: uvArr,
					yAxis: 0
				},
				{  //数据列
					name: '浏览次数',
					data: pvArr,
					yAxis: 0
				},
				{  //数据列
					name: '下单买家数',
					data: gmvWinnerNumArr,
					yAxis: 0
				},
				{  //数据列
					name: '下单转化率',
					data: gmvRateArr,
					yAxis: 1
				},
				{  //数据列
					name: '支付买家数',
					data: alipayWinnerNumArr,
					yAxis: 0
				},
				{  //数据列
					name: '支付件数',
					data: alipayAuctionNumArr,
					yAxis: 0
				},
				{  //数据列
					name: '支付转化率',
					data: alipayRateArr,
					yAxis: 1
				}

			]
		});
		TM.Alert.loadDetail(html, 1000, 530);
	}
}, QueryCommodity.rule);


QueryCommodity.init = function() {
	var datMills = 24* 3600 * 1000;

	if((new Date().getTime() - parseInt(TM.firstLoginTime)) < datMills) {
		$('.searchDiagTip').show();
	}
	
	var urlArray = location.href;
	if(urlArray.indexOf("numIid") > 0) {
		numIid = urlArray.split("numIid=")[1];
	}
	
	QueryCommodity.query.getComms();
};
