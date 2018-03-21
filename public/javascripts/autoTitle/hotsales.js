var TM = TM || {};

((function ($, window) {
	'use strict';
	TM.HotSales = TM.HotSales || {};
	var HotSales = TM.HotSales;

	var cdnPath = 'http://txgyun.b0.upaiyun.com/paiming/img/rank/trade/';
	var catMap = new Map(), keywordSearch = '', inputTimeOutID = 0;
	HotSales.constant = HotSales.constant || {};
	HotSales.constant = $.extend({
		MILLISECONDS_OF_MINIMUM_FOLLOW_LIFETIME: 43200000,
		DEFAULT_ITEM_RANK_RADIX: 240,
		MAX_ITEM_RANK: 160
	});

	HotSales.init = HotSales.init || {};
	HotSales.init = $.extend({
		doInit: function () {
			HotSales.init.initCalendar();
			HotSales.init.initFollow();
			HotSales.event.sortMethodListener();
		},

		initCalendar: function () {
			$("#datezone.date").datepicker('remove');
			var endDate = TM.HotSales.util.getEndDate('-');
			$("#datezone.date").datepicker({
				format: "yyyy-mm-dd",
				startDate: TM.HotSales.util.formateDate(new Date(new Date().getTime() - 28*24*60*60*1000), '-'),
				endDate: endDate,
				todayHighlight: false
			});
			$("#datezone.date").datepicker('update', endDate);
			$("#datezone.date").datepicker().on('changeDate', function () {
				HotSales.salesRank.renderSalesRank();
			});
		},

		initFollow: function () {
			HotSales.followManager.getFollow(function (data) {
				if (data.message === 'NEEDSIGNINORSIGNUP') {// 未登录时允许其点击我要关注行业
					//匿名用户显示随机一个行业
					data.res = data.res || {"follows":[{"cid":1623,"displayName":"女装/女士精品>半身裙(随机一个)","status":"ANONYMOUS"}],"followMore":true};
					data.success = true;
					swal({ title: "无法确定您感兴趣的行业！", text: "您目前尚未登陆，因而无法追踪您感兴趣的行业，我们将随机为您展示一个行业的热销数据哦！！！", timer: 2000, showConfirmButton: false });
				}
				if (data.res.followMore) {
					$('#followBtn').attr("style", "background-color:#3085D5;");
					HotSales.event.showTradePannelListener();
				} else {
					$('#followBtn').attr("style", "background-color:#b8b8b8;");
					$('#followBtn').unbind('click').bind('click', function () {
						swal({ title: "想关注更多行业？", text: "您目前只能且已关注" + data.res.follows.length + "个行业！", timer: 4000, showConfirmButton: true });
					});
				}
				if (data.success && data.res.follows && data.res.follows.length > 0) {
					$(".enable_cat").html("您目前还可以关注" + (5 - data.res.follows.length) + "个类目")
					var tempHtml = $("#trade_select").html();
					var resultHtml = "";
					$.each(data.res.follows, function (index, ele) {// 渲染已关注行业
						var html = tempHtml;
						html = html.replace("{key_id}", index)
							.replace("{key_id}", ele.cid)
							.replace("{key_isParent}", ele.isParent)
							.replace("{key_updateTs}", ele.updateTs)
							.replace("{status}", ele.status)
							.replace("{index}", index)
							.replace("{key_keyword}", ele.displayName)
							.replace("{key_keyword}", ele.displayName);
						resultHtml += html;
					});
					$("#hy_select .select-ul-div .select-ul").html(resultHtml);
					var parent = $("#hy_select");
					parent.hover(null, function () {
						parent.find(".select-ul-div").addClass("hidden");
					});
					parent.find(".hy_select_top").unbind("click").bind("click", function (e) {
						var $select_div = parent.find(".select-ul-div");
						if ($select_div.attr("class").indexOf("hidden") > 0) {
							$select_div.removeClass("hidden");
						} else {
							$select_div.addClass("hidden");
						}
					});
					parent.find(".select-ul>li").unbind("click").bind("click", function () {
						var ele = $(this);
						var select_span = parent.find(".select-span");
						var ret = {};
						ret.lang = ele.attr('lang');
						ret.updateTs = ele.attr('updateTs');
						ret.status = ele.attr('status');
						ret.title = ele.find('span').attr('title');
						ret.isParent = ele.attr("isParent");

						select_span.attr('lang', ret.lang);
						select_span.attr('updateTs', ret.updateTs);
						select_span.attr('status', ret.status);
						select_span.attr('title', ret.title);
						select_span.attr('isParent', ret.isParent);
						select_span.html(ele.find('span').html());
						parent.find(".select-ul-div").addClass("hidden");
						// render page
						$("#datezone.date").datepicker('update', HotSales.util.getEndDate('-'));// 重新初始化日期
						HotSales.salesRank.renderSalesRank();
					});
					$("#key_li_" + (data.res.follows.length - 1)).click();
				} else {
					$("#hy_select .hy_select_top .select-span").text("您当前无已关注的行业");
				}

			});
		},
	});

	HotSales.event = HotSales.event || {};
	HotSales.event = $.extend({
		sortMethodListener: function () {
			$('#soldColumn').unbind('click').bind('click', function () {
				if ($('#soldColumn').hasClass('soldDesc')) {
					$('#soldColumn').removeClass().addClass('soldAsc');
					$('#soldColumn img').attr('src', cdnPath + 'sort_asc.png');
				} else {
					$('#soldColumn').removeClass().addClass('soldDesc');
					$('#soldColumn img').attr('src', cdnPath + 'sort_desc.png');
				}
				HotSales.salesRank.renderSalesRank();
			});
		},

		showTradePannelListener : function() {
			$('#followBtn').unbind('click').bind('click', function () {
				$(document.body).append('<div class="bg_div_grey_4_trade"></div>').append($('#dialog_addCat').html());
				$(".trade_search").unbind("click").bind("click", function () {// 已显示则隐藏，否则显示
					if ($('.trade_selector_div .trade_selector_class.selector_1').attr('style') === 'display: block;') {
						$(".trade_selector .trade_selector_div .trade_selector_class").hide();
						$(".trade_selector .trade_selector_div").hide();
					} else {
						HotSales.followManager.getCatInfo(0, 1);
					}
				});
				HotSales.event.tradeSearchInputListener();
				$('.trade_selector .trade_selector_close').unbind('click').bind('click', function () {// 关闭面板
					$('body .bg_div_grey_4_trade').remove();
					$('body .trade_selector').remove();
				});
				$('.bg_div_grey_4_trade').unbind('click').bind('click', function () {// 点击栏目以外位置隐藏栏目
					$(".trade_selector .trade_selector_div .trade_selector_class").hide();
					$(".trade_selector .trade_selector_div").hide();
				});
			});
		},

		tradeSearchInputListener: function () {
			//手动输入框
			$(".trade_search_input").keyup(function () {
				keywordSearch = $.trim($(".trade_search_input").val());
				window.clearTimeout(inputTimeOutID);
				inputTimeOutID = window.setTimeout(function () {
					if (keywordSearch === $.trim($(".trade_search_input").val()) && keywordSearch !== '') {
						TM.HotSales.followManager.searchCat();
					}
				}, 500);
			});
		},
	});

	HotSales.followManager = HotSales.followManager || {};
	HotSales.followManager = $.extend({

		getCatInfo: function (parentId, levelId) {
			for (var i = levelId + 1; i <= 4; i++) {
				$(".trade_selector .trade_selector_div .selector_" + i + " .selector_content").html("");
				$(".trade_selector .trade_selector_div .selector_" + i).hide();
			}
			var catData = catMap.get(parentId);
			if (catData && catData.length > 0) {
				HotSales.followManager.initCatSelector(catData, levelId);
			} else {
				$.get("/SupportUtil/ItemCat?parentCid=" + parentId, function (data) {
					if (data.success && data.res.length > 0) {
						catMap.set(parentId, data.res);
						HotSales.followManager.initCatSelector(data.res, levelId);
					} else {
						$("div[t_id='" + parentId + "']").removeClass("have_child").addClass("end_child");
						for (var i = levelId; i <= 4; i++) {
							$(".trade_selector .trade_selector_div .selector_" + i + " .selector_content").html("");
							$(".trade_selector .trade_selector_div .selector_" + i).hide();
						}
						HotSales.followManager.bindEndChildClick();
					}
				}, "JSON");
			}
		},

		searchCat: function () {
			if (keywordSearch !== "") {
				for (var i = 2; i <= 4; i++) {
					$(".trade_selector .trade_selector_div .selector_" + i + " .selector_content").html("");
					$(".trade_selector .trade_selector_div .selector_" + i).hide();
				}
				var catData = catMap.get(keywordSearch);
				if (catData) {
					HotSales.followManager.initCatSelector(catData, 1);
				} else {
					$.get("/SupportUtil/ItemCat?search=" + keywordSearch, function (data) {
						catMap.set(keywordSearch, data.res);
						HotSales.followManager.initCatSelector(data.res, 1);
					}, "JSON");
				}
			} else {
				HotSales.followManager.getCatInfo(0, 1);
			}
		},

		initCatSelector: function (dataList, levelId) {
			var html = "";
			$.each(dataList, function (i, ele) {
				if (ele.isParent) {
					html += "<div t_id='" + ele.id + "' c_id='" + ele.cid + "' l_id='" + levelId + "' p_id='" + ele.parentCId + "' class='have_child'>" + ele.name + "</div>";
				} else {
					html += "<div t_id='" + ele.id + "' c_id='" + ele.cid + "' l_id='" + levelId + "' p_id='" + ele.parentCId + "' class='end_child'>" + ele.name + "</div>";
				}
			});
			$(".trade_selector .trade_selector_div").show();
			$(".trade_selector .trade_selector_div .selector_" + levelId + " .selector_content").html(html);
			$(".trade_selector .trade_selector_div .selector_" + levelId).show();
			$(".trade_selector .trade_selector_div .selector_" + levelId + " .selector_content div").unbind("mouseover").bind("mouseover", function () {
				$(this).parent().parent().prev().find(".cat_selected").removeClass("cat_selected");
				$(this).parent().parent().prev().find("div[t_id='" + $(this).attr("p_id") + "']").addClass("cat_selected");

				if ($(this).hasClass("have_child")) {
					HotSales.followManager.getCatInfo($(this).attr("t_id"), parseInt($(this).attr("l_id")) + 1);
				} else {
					for (var i = levelId + 1; i <= 4; i++) {
						$(".trade_selector .trade_selector_div .selector_" + i + " .selector_content").html("");
						$(".trade_selector .trade_selector_div .selector_" + i).hide();
					}
					HotSales.followManager.bindEndChildClick();
				}
			});
		},

		bindEndChildClick: function () {
			$(".end_child").unbind("click").bind("click", function () {
				var tradeId = $(this).attr("c_id");
				var tradeName = [];
				var ele = this;
				for (var i = 0; i < 4; i++) {
					tradeName.push($(ele).text());
					ele = $(ele).parent().parent().prev().find(".cat_selected");
					if (ele.length === 0) {
						break;
					}
				}
				$(".trade_selected_input").attr("trade_id", tradeId).val(tradeName.reverse().join(">"));
				$(".trade_search_input").val("");
				$(".trade_selector .trade_selector_div .trade_selector_class").hide();
				$(".trade_selector .trade_selector_div").hide();
				$('#addTrade').unbind("click").bind("click", function () {
					HotSales.followManager.addFollow();
				});
			});
		},

		addFollow: function () {
			$.ajax({
				url: '/TradeQuery/follow',
				type: 'POST',
				dataType: 'json',
				data: { cid: Number($(".trade_selected_input").attr("trade_id")) },
				beforeSend: function () {
					$(".loading_div").css("display", "block");
					$("#loadingmask").css("display", "block");
				},
				success: function (data) {
					$('.trade_selector .trade_selector_close').click();
					if (data.success) {
						//刷新当前关注
						swal({ title: "关注成功！", text: "关注新行业成功，热腾腾的数据正在赶来，客官请稍等......", timer: 2000, showConfirmButton: false });
						HotSales.init.initFollow();
					} else {
						HotSales.util.swalAjaxError(data, function () {
							swal('哎呀！！！', '抱歉您新提交的行业类目关注失败，失败原因为：' + data.message, 'error');
						});
						}
				},
				complete: function () {
					$(".loading_div").css("display", "none");
					$("#loadingmask").css("display", "none");
				},
			});
		},

		getFollow: function (callback) {
			$.ajax({
				url: '/TradeQuery/follow',
				type: 'GET',
				success: function (data) {
					callback(data);
				}
			});
		},

		unFollow: function (callback) {
			$.ajax({
				url: '/TradeQuery/follow',
				type: 'DELETE',
				dataType: 'json',
				data: { cid: Number($("#hy_select .hy_select_top .select-span").attr('lang')) },
				beforeSend: function () {
					$(".loading_div").css("display", "block");
					$("#loadingmask").css("display", "block");
				},
				success: function (data) {
					if (callback) {
						callback(data);
					} else {
						$('#unfollowBtn').unbind('click');// 解绑取关按钮
						$('#unfollowBtn').attr('style', '');// 变更取关按钮底色为灰
						if ($('#treasureRankList .pagination').html()) {// 页码选择器归零
							$('#treasureRankList .pagination').pagination('updatePages', 0);
						}
						$('#treasureRankList .Successful_task_middle2_1_top_span.count').html(0);// 结果计数归零
						$('#treasureRankList .Successful_task_middle2_1_top_span.pageSize').html(0);// 分页情况归零
						if (data.success) {
							//刷新当前关注
							swal({ title: "取消成功！", text: "取消关注行业成功，页面刷新中，客官请稍等......", timer: 2000, showConfirmButton: false });
							HotSales.init.initFollow();
						} else {
							HotSales.util.swalAjaxError(data, function () {
								swal('天哪！！！', '抱歉您取消关注行业类目失败，失败原因为：' + data.message, 'error');
							});
						}
					}
				},
				complete: function () {
					$(".loading_div").css("display", "none");
					$("#loadingmask").css("display", "none");
				},
			});
		}
	});

	HotSales.salesRank = HotSales.salesRank || {};
	HotSales.salesRank = $.extend({
		renderSalesRank: function () {
			$('#treasureRankList .treasureList').html('');// 清空之前搜索结果
			if ($("#hy_select .hy_select_top .select-span").attr('lang') === '-1') {// 无已关注行业时直接退出
				return;
			}
			if ($("#hy_select .hy_select_top .select-span").attr('status').indexOf("BAN") > 0) {
				swal('抱歉，无法访问！', '很抱歉,由于某些原因,您被管理员禁止访问当前行业-->' + $("#hy_select .hy_select_top .select-span").attr('title') + '<--的数据！', 'error');
			}
			if ($("#hy_select .hy_select_top .select-span").attr('status').indexOf("INVALIDATION") > 0) {
				swal('抱歉，无法访问！', '很抱歉当前行业' + $("#hy_select .hy_select_top .select-span").attr('title') + '已失效，如有疑问请联系客服', 'warning');
			}
			if ($("#hy_select .hy_select_top .select-span").attr('status') === 'FOLLOW') {
				if ($('#treasureRankList .pagination').html()) {
					$('#treasureRankList .pagination').pagination('updatePages', 1);// 将页码重置到1
				}
				HotSales.salesRank.pullData(1);
			}
			$('#unfollowBtn').unbind('click');
			if ($("#hy_select .hy_select_top .select-span").attr('status').indexOf("ANONYMOUS") > -1) {// 匿名状态下拉取行业信息
				if ($('#treasureRankList .pagination').html()) {
					$('#treasureRankList .pagination').pagination('updatePages', 1);// 将页码重置到1
				}
				HotSales.salesRank.pullData(1);
				$('#unfollowBtn').attr('style', '');
				$('#unfollowBtn').bind('click', function () {
					swal({ title: "无法取消！", text: "注册用户方可添加或取消关注行业数据哦！！！", timer: 3000, showConfirmButton: false });
				});
			} else if ($("#hy_select .hy_select_top .select-span").attr('updateTs') > new Date().getTime() - HotSales.constant.MILLISECONDS_OF_MINIMUM_FOLLOW_LIFETIME) {// 未超过12小时
				$('#unfollowBtn').attr('style', '');
				$('#unfollowBtn').bind('click', function () {
					swal({ title: "暂不能取消", text: "关注成功的新行业在12小时后才开放取消关注功能哦...", timer: 3000, showConfirmButton: false });
				});
			} else {
				$('#unfollowBtn').attr('style', 'background-color: #3085D5;');
				$('#unfollowBtn').bind('click', function () {
					swal({
						title: "确定要取消关注吗？",
						text: "确定要取消对当前行业 " + $("#hy_select .hy_select_top .select-span").attr('title') + "  的关注吗？",
						type: "info",
						showCancelButton: true,
						cancelButtonText: "且慢，容朕再看看",
						confirmButtonText: "还不快去",
						closeOnConfirm: false,
						showLoaderOnConfirm: true,
					},
					function () {
						TM.HotSales.followManager.unFollow();
					});
				});
			}
		},

		pullData: function (pn) {
			$.ajax({
				url: '/TradeQuery/hotSalesRank?cid=' + $("#hy_select .hy_select_top .select-span").attr('lang') + '&day=' + HotSales.util.formateDate($("#datezone.date").datepicker('getDate')) + '&pn=' + pn + '&sort=' + $('#soldColumn').attr('class'),
				type: 'GET',
				beforeSend: function () {
					$(".loading_div").css("display", "block");
					$("#loadingmask").css("display", "block");
				},
				success: function (data) {
					if (data.isOk) {
						HotSales.salesRank.listResult(data);
					} else {
						if ($('#treasureRankList .pagination').html()) {// 页码选择器归零
							$('#treasureRankList .pagination').pagination('updatePages', 0);
						}
						$('#treasureRankList .Successful_task_middle2_1_top_span.count').html(0);// 结果计数归零
						$('#treasureRankList .Successful_task_middle2_1_top_span.pageSize').html(0);// 分页情况归零
						HotSales.util.swalAjaxError(data, function () {
							swal('糟糕', data.msg, 'warning');
						});
					}
				},
				complete: function () {
					$(".loading_div").css("display", "none");
					$("#loadingmask").css("display", "none");
				},
			});
		},

		listResult: function (data) {
			var html, tmp;
			for (var i = 0; i < data.res.length; i++) {
				tmp = "";
				try {
					tmp = '<tr class="hotsales_item"><td style="width: 50px;"><span class="rank_index' + ((data.pn === 1 && i < 3) ? ' top3' : '') + '">' + (i + 1) + '</span></td><td style="width: 70px;"><a href="https://item.taobao.com/item.htm?id=' + data.res[i].numIid + '" target="_blank"><img class="Salesranking_content1_rxbbphb_content1li2div1 itemPic" src="' + data.res[i].picPath + '" alt="图像加载失败"></a></td><td style="width: 200px;padding-left: 15px;padding-right: 15px;"><a href="https://item.taobao.com/item.htm?id=' + data.res[i].numIid + '" target="_blank">' + data.res[i].title + '</a></td><td style="width:120px;"><a href="http://store.taobao.com/shop/view_shop.htm?user_number_id=' + data.res[i].userId + '" target="_blank" style="color:#6eb2dc;font-weight: 700;font-size: 14px;">' + data.res[i].nick + '</a></td><td style="width: 85px;padding-left: 5px;padding-right: 5px;">' + data.res[i].location + '</td><td style="width: 80px;"> ' + data.res[i].price + '</td><td><span class="' + (data.res[i].soldRoc >= 0 ? 'positive' : 'negative') + '" style="width: 100%;float: left;">' + data.res[i].sold + '</span><span class="' + (data.res[i].soldRoc >= 0 ? 'positive' : 'negative') + '" style="width: 100%;float: left;">' + Number(data.res[i].soldRoc).toFixed(2) + '%<img src="' + cdnPath + (data.res[i].soldRoc >= 0 ? 'up' : 'down') + '.png"></span></td><td><span class="' + (data.res[i].salesRoc >= 0 ? 'positive' : 'negative') + '" style="width: 100%;float: left;">' + Number(data.res[i].sales).toFixed(2) + '</span><span class="' + (data.res[i].salesRoc >= 0 ? 'positive' : 'negative') + '" style="width: 100%;float: left;">' + Number(data.res[i].salesRoc).toFixed(2) + '%<img src="' + cdnPath + (data.res[i].salesRoc >= 0 ? 'up' : 'down') + '.png"></span></td><td style="width: 120px;padding-left: 5px;padding-right: 5px;"><div class="query_item_rank" numiid="' + data.res[i].numIid + '" date="' + data.res[i].day + '" data="false" topn="1"><i class="rankBtn" style="color: #1299ec; cursor:pointer;">查看排名情况</i></div><div class="search_heat" numiid="' + data.res[i].numIid + '" date="' + data.res[i].day + '"data="false"><i class="chartBtn" style="color: #1299ec; cursor:pointer;">查看搜索热度</i></div><div class="item_sold" numiid="' + data.res[i].numIid + '" date="' + data.res[i].day + '"data="false"><i class="soldBtn" style="color: #1299ec; cursor:pointer;">查看宝贝销量</i></div></td></tr><tr class="hotsales_data"><td colspan="9" id="datazone' + data.res[i].numIid + '" class="hide"><img src="' + cdnPath + 'loading.gif"></td></tr><tr class="search_heat_data"><td colspan="9" id="chartzone' + data.res[i].numIid + '" class="hide"><img src="' + cdnPath + 'loading.gif"></td></tr><tr class="item_sold_data"><td colspan="9" id="soldzone' + data.res[i].numIid + '" class="hide"><img src="' + cdnPath + 'loading.gif"></td></tr>';
				} catch (error) {
					
				} finally {
					html += tmp;
				}
			}
			$('#treasureRankList .treasureList').html(html);
			// 绑定查询排名事件
			$('.query_item_rank').unbind('click').bind('click', function (e) {
				var x = e.currentTarget;
				var dataZone = $('#datazone' + $(x).attr('numiid'));
				var chartZone = $('#chartzone' + $(x).attr('numiid'));
				var soldZone = $('#soldzone' + $(x).attr('numiid'));
				if (dataZone.hasClass('hide')) {
					x.innerHTML = '<i class="rankBtn" style="color: #1299ec; cursor:pointer;">收起排名</i>';
					dataZone.removeClass('hide');
					if(!chartZone.hasClass('hide')) {
						$(x).siblings().find(".chartBtn").html("查看搜索热度");
						chartZone.addClass('hide');
					}
					if(!soldZone.hasClass('hide')) {
						$(x).siblings().find(".soldBtn").html("查看搜索热度");
						soldZone.addClass('hide');
					}
				} else {
					x.innerHTML = '<i class="rankBtn" style="color: #1299ec; cursor:pointer;">查看排名情况</i>';
					dataZone.addClass('hide');
				}
				// 绘制table
				if ($(x).attr('data') === 'false') {
					HotSales.salesRank.queryItemRank($(x).attr('numiid'), $(x).attr('date'), 1, function (numIid, day, data) {
						if (data.isOk) {
							var tableHTML = '<table class="itemrank_table" style="width: 100%; border-collapse: collapse;"><thead><tr><th style="width: 188px;">自然搜索关键词</th><th style="width: 280px;">' + data.res.categories[2] + '/名次</th><th style="width: 280px;">' + data.res.categories[1] + '/名次</th><th style="width: 280px;">' + data.res.categories[0] + '/名次</th><th style="width: 110px;">更多排名</th></tr></thead><tbody id="itemrank' + numIid + '">';
							var tfootHTML = '</tbody><tfoot><tr><td colspan="4"><div class="pagination"></div></td></tr></tfoot></table>';
							dataZone.html('<div style="width: 95%;border: 1px solid #e7e7e7;">' + tableHTML + tfootHTML + '</div>');
							HotSales.salesRank.renderItemRank(numIid, day, data);
							if (data.pnCount > 1) {
								dataZone.find('.pagination').pagination({
									pages: data.pnCount,
									styleClass: ['pagination-large'],
									showCtrl: true,
									displayPage: 4,
									onSelect: function (num) {
										HotSales.salesRank.queryItemRank(numIid, day, num);
									}
								});
							}
						} else {
							$('#datazone' + $(x).attr('numiid')).html('Oops...' + data.message);
						}
						$(x).attr('data', 'true');
					});
				}
			});

			$('.search_heat').unbind('click').bind('click', function (e) {
				var dataZone = $('#datazone' + $(this).attr('numiid'));
				var chartZone = $('#chartzone' + $(this).attr('numiid'));
				var soldZone = $('#soldzone' + $(this).attr('numiid'));
				if (chartZone.hasClass('hide')) {
					$(this).find(".chartBtn").html("收起热度");
					chartZone.removeClass('hide');
					if(!dataZone.hasClass('hide')) {
						$(this).siblings().find(".rankBtn").html("查看排名情况");
						dataZone.addClass('hide');
					}
					if(!soldZone.hasClass('hide')) {
						$(this).siblings().find(".soldBtn").html("查看搜索热度");
						soldZone.addClass('hide');
					}
				} else {
					$(this).find(".chartBtn").html("查看搜索热度");
					chartZone.addClass('hide');
				}
				//绘制chart
				if ($(this).attr('data') === 'false') {
					var numIid = $(this).attr("numIid");
					var day = $(this).attr("date");
					$.ajax({
						url: '/TradeQuery/getSearchHeatData',
						data: { numIid: numIid, day: day },
						type: 'GET',
						global: false,
						success: function (data) {
							if (!data.isOk) {
								$(this).attr('data', 'true');
								$('#chartzone' + numIid).html('Oops...' + data.msg);
								return;
							}
							var dataJsonArray = data.res;
							var dateArr = [];
							var scoreArr = [];

							$(dataJsonArray).each(function (index, dataJson) {
								dateArr.push(dataJson.date);
								scoreArr.push(parseInt(dataJson.score));
							});

							var html = $('#search-heat-tmpl').clone();
							html.attr("id", "search-heat-chart-" + numIid);
							chartZone.html(html);

							// 搜索热度(Highchart)
							chart = new Highcharts.Chart({
								chart: {
									renderTo: 'search-heat-chart-' + numIid,
									defaultSeriesType: 'line', //图表类型line(折线图)
									width: 1140
								},
								credits: {
									enabled: false //右下角不显示LOGO
								},
								title: {
									text: '宝贝近7天搜索热度趋势' //图表标题
								},
								xAxis: {  //x轴
									categories: dateArr, //x轴标签名称
									gridLineWidth: 0, //设置网格宽度为1
									lineWidth: 2,  //基线宽度
									labels: {
										y: 20   //x轴标签位置：距X轴下方20像素
									}
								},
								yAxis: [
									{
										title: {
											text: '热度趋势'
										},
										labels: {
											formatter: function () {
												return this.value / 10000 + '万';
											}
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
										name: '搜索热度',
										data: scoreArr,
										yAxis: 0
									}
								]
							});
						}
					});
					$(this).attr('data', 'true');
				}
			});

			$('.item_sold').unbind('click').bind('click', function (e) {
				var dataZone = $('#datazone' + $(this).attr('numiid'));
				var chartZone = $('#chartzone' + $(this).attr('numiid'));
				var soldZone = $('#soldzone' + $(this).attr('numiid'));
				if (soldZone.hasClass('hide')) {
					$(this).find(".soldBtn").html("收起销量");
					soldZone.removeClass('hide');
					if(!dataZone.hasClass('hide')) {
						$(this).siblings().find(".rankBtn").html("查看排名情况");
						dataZone.addClass('hide');
					}
					if(!chartZone.hasClass('hide')) {
						$(this).siblings().find(".chartBtn").html("查看搜索热度");
						chartZone.addClass('hide');
					}
				} else {
					$(this).find(".soldBtn").html("查看宝贝销量");
					soldZone.addClass('hide');
				}
				//绘制chart
				if ($(this).attr('data') === 'false') {
					var numIid = $(this).attr("numIid");
					var day = $(this).attr("date");
					$.ajax({
						url: '/TradeQuery/getSearchHeatData',
						data: { numIid: numIid, day: day },
						type: 'GET',
						global: false,
						success: function (data) {
							if (!data.isOk) {
								$(this).attr('data', 'true');
								$('#soldzone' + numIid).html('Oops...' + data.msg);
								return;
							}
							var dataJsonArray = data.res;
							var dateArr = [];
							var soldArr = [];

							$(dataJsonArray).each(function (index, dataJson) {
								dateArr.push(dataJson.date);
								soldArr.push(parseInt(dataJson.sold));
							});

							var html = $('#item-sold-tmpl').clone();
							html.attr("id", "item-sold-chart-" + numIid);
							soldZone.html(html);

							// 宝贝销量(Highchart)
							chart = new Highcharts.Chart({
								chart: {
									renderTo: 'item-sold-chart-' + numIid,
									defaultSeriesType: 'line', //图表类型line(折线图)
									width: 1140
								},
								credits: {
									enabled: false //右下角不显示LOGO
								},
								title: {
									text: '宝贝近7天30天销量趋势' //图表标题
								},
								xAxis: {  //x轴
									categories: dateArr, //x轴标签名称
									gridLineWidth: 0, //设置网格宽度为1
									lineWidth: 2,  //基线宽度
									labels: {
										y: 20   //x轴标签位置：距X轴下方20像素
									}
								},
								yAxis: [
									{
										title: {
											text: '销量趋势'
										},
										labels: {
											formatter: function () {
												return this.value + '件';
											}
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
										name: '30天销量',
										data: soldArr,
										yAxis: 0
									}
								]
							});
						}
					});
					$(this).attr('data', 'true');
				}
			});

			$('#treasureRankList .Successful_task_middle2_1_top_span.count').html(data.count);
			$('#treasureRankList .Successful_task_middle2_1_top_span.pageSize').html(data.ps);
			if ($('#treasureRankList .pagination').html()) {
				$('#treasureRankList .pagination').pagination('updatePages', data.pnCount);
			} else {
				$('#treasureRankList .pagination').pagination({
					pages: data.pnCount,
					styleClass: ['pagination-large'],
					showCtrl: false,
					displayPage: 2,
					onSelect: function (num) {
						HotSales.salesRank.pullData(num);
					}
				});
			}
		},

		queryItemRank: function (numIid, day, pn, callback) {
			$.ajax({
				url: '/TradeQuery/hotSalesItemRank?numIid=' + numIid + '&day=' + day + '&pn=' + pn + '&track=3&rankType=1&wordId=0',
				type: 'GET',
				beforeSend: function () {
					$(".loading_div").css("display", "block");
					$("#loadingmask").css("display", "block");
				},
				success: function (data) {
					if (callback) {
						callback(numIid, day, data);
					} else {
						if (data.isOk) {
							HotSales.salesRank.renderItemRank(numIid, day, data);
						} else {
							HotSales.util.swalAjaxError(data, function () {
								swal('排名信息', data.msg, 'info');
							});
						}
					}
				},
				complete: function () {
					$(".loading_div").css("display", "none");
					$("#loadingmask").css("display", "none");
				},
			});
		},

		renderItemRank: function (numIid, day, data) {
			var object = data.res, maxValue = HotSales.constant.MAX_ITEM_RANK;
			var tbodyHTML = '';
			for (var key in object) {
				if (object.hasOwnProperty(key) && key !== 'categories') {
					var element = object[key];
					tbodyHTML += '<tr class="itemrank" wordid="' + key.split('`~@')[1] + '"><td>' + key.split('`~@')[0] + '</td><td>' + (element[2] % 10000 > maxValue ? (maxValue + '+') : HotSales.util.convertItemRank(element[2])) + (element[2] % 10000 === element[1] % 10000 ? '' : '<img src="' + cdnPath + (element[2] % 10000 < element[1] % 1000 ? 'up' : 'down') + '.png">') + '</td><td>' + (element[1] % 10000 > maxValue ? (maxValue + '+') : HotSales.util.convertItemRank(element[1])) + (element[1] % 10000 === element[0] % 10000 ? '' : '<img src="' + cdnPath + (element[1] % 10000 < element[0] % 10000 ? 'up' : 'down') + '.png">') + '</td><td>' + (element[0] % 10000 > maxValue ? (maxValue + '+') : HotSales.util.convertItemRank(element[0])) + '</td><td class="more_item_rank" style="color: #528600;">七日排名走势</td></tr><tr class="more_item_rank_chartzone hide" ><td colspan="5" style="height: auto;"><img src=" ' + cdnPath + 'bounceloading.gif"></td></tr>';
				}
			}
			$('#itemrank' + numIid).html(tbodyHTML);
			$('.more_item_rank').unbind('click').bind('click', function (e) {
				var chartZone = $(e.currentTarget).parent().next();
				if (chartZone.hasClass('hide')) {
					$('.more_item_rank').html('<i style="color: #528600; cursor:pointer;">七日排名走势</i>');
					$('.more_item_rank_chartzone').addClass('hide');
					$(e.currentTarget).html('<i style="color: #528600; cursor:pointer;">收起</i>');
					chartZone.removeClass('hide');
				} else {
					$(e.currentTarget).html('<i style="color: #528600; cursor:pointer;">七日排名走势</i>');
					chartZone.addClass('hide');
				}
				if (!!chartZone.find('img')[0] && Number($(e.currentTarget).parent().attr('wordid')) > 0) {
					$.ajax({
						url: '/TradeQuery/hotSalesItemRank?numIid=' + numIid + '&day=' + day + '&wordId=' + $(e.currentTarget).parent().attr('wordid') + '&track=7&rankType=1',
						type: 'GET',
						success: function (data) {
							if (data.isOk) {
								//绘制排名折线图
								var series = [], object = data.res, maxValue_Y_Axis = HotSales.constant.MAX_ITEM_RANK + 20, maxValue = 1;// maxValue_Y_Axis:图表展示时允许出现的最大值，对应y轴最大值，不宜过大；maxValue:返回数据中的最大值
								for (var key in object) {
									if (object.hasOwnProperty(key) && key !== 'categories') {
										var element = object[key], tmp;
										for (var index = 0; index < element.length; index++) {
											tmp = element[index] % 10000;
											if (tmp > maxValue_Y_Axis) {
												element[index] = maxValue_Y_Axis;
											}
											if (tmp > maxValue) {
												maxValue = tmp;
											}
											if (element[index] > 10000) {// FYI, add 0.01 , mark for util.convertItemRank()
												element[index] = tmp + 0.01;
											}
										}
										series.push({ name: key, data: element });
									}
								}
								chartZone.find('td').highcharts({
									title: {
										text: '宝贝7日关键词排名趋势',
										x: -20 // center
									},
									subtitle: {
										text: '依据淘宝网公开数据整理',
										x: -20
									},
									xAxis: {
										categories: data.res.categories,
										labels: {
											formatter: function () {
												return HotSales.util.formateDate(new Date(this.value.toString().substring(0, 4), (this.value - 100).toString().substring(4, 6), this.value.toString().substring(6, 8)), '-');
											},
										},
									},
									yAxis: {
										min: 1,
										max: maxValue >= maxValue_Y_Axis ? maxValue_Y_Axis - 5 : Math.ceil(maxValue / 20) * 20,
											tickInterval: 20,
											title: {
												text: '名次'
											},
											labels: {
												formatter: function () {
													if (this.value < maxValue_Y_Axis) {
														return this.value;
													} else {
														return "未知";
													}
												},
											},
											plotLines: [{
												value: 0,
												width: 1,
												color: '#808080'
											}],
											reversed: true,
									},
									tooltip: {
										backgroundColor: {
											linearGradient: [0, 0, 0, 60],
											stops: [
												[0, '#FFFFFF'],
												[1, '#E0E0E0']
											]
										},// 背景颜色
										borderRadius: 10,// 边框圆角
										borderWidth: 1,// 边框宽度
										shadow: true,// 是否显示阴影
										animation: true,// 是否启用动画效果
										style: {// 文字内容相关样式
											color: "#ff0000",
											fontSize: "12px",
											fontWeight: "blod",
											fontFamily: "Courir new"
										},
										useHTML: true,
										formatter: function () {
											if (this.y < maxValue_Y_Axis) {
												return '<b style="color:' + this.series.color + '">' + this.series.name + ' </b><span> @' + HotSales.util.formateDate(new Date(this.x.toString().substring(0, 4), (this.x - 100).toString().substring(4, 6), this.x.toString().substring(6, 8)), '.') + ' </span><br /><span style="color:' + this.series.color + '">●</span><span>   ' + HotSales.util.convertItemRank(this.y) + '</span> ';
											} else {
												return '<b style="color:' + this.series.color + '">' + this.series.name + ' </b><span> @' + HotSales.util.formateDate(new Date(this.x.toString().substring(0, 4), (this.x - 100).toString().substring(4, 6), this.x.toString().substring(6, 8)), '.') + ' </span><br /><span style="color:' + this.series.color + '">●</span><span>   暂无该日排名数据</span> ';
											}
										},
									},
									legend: {
										layout: 'vertical',
										align: 'right',
										verticalAlign: 'middle',
										borderWidth: 0
									},
									series: series,
									credits: {
										enabled: false
									},
								});
							} else {
								chartZone.find('td').html('<i style="color: #528600; cursor:pointer;">Oops...' + data.msg + '</i>');
							}
						},
					});
				}
			});
		}
	});

	HotSales.util = HotSales.util || {};
	HotSales.util = $.extend({
		getEndDate: function (separator) {
			if (typeof (separator) !== 'string') {
				separator = '';
			}
			var date = new Date(new Date().getTime() - 86400000);

			return date.getFullYear() + separator + (date.getMonth() + 1 < 10 ? "0" + (date.getMonth() + 1) : date.getMonth() + 1) + separator + (date.getDate() < 10 ? "0" + date.getDate() : date.getDate());
		},

		formateDate: function (date, separator) {
			if (typeof (separator) !== 'string') {
				separator = '';
			}
			return date.getFullYear() + separator + (date.getMonth() + 1 < 10 ? "0" + (date.getMonth() + 1) : date.getMonth() + 1) + separator + (date.getDate() < 10 ? "0" + date.getDate() : date.getDate());
		},

		swalAjaxError: function (data, defaultCallback) {
			if (data.message === 'NEEDSIGNINORSIGNUP') {
				swal({
					title: "请登录或注册•••",
					text: "匿名状态下无法查看行业热销信息哦！<br/>手机验证码/微信扫码，一分钟极速注册！<br/>注册即得关注一行业，推荐好友送关注，多推多送，超爽de！",
					type: "info",
					html: "true",
					showCancelButton: true,
					cancelButtonText: "不用了，谢谢",
					confirmButtonText: "前去注册/登录",
					closeOnConfirm: false,
					showLoaderOnConfirm: true,
				},
				function () {
					window.location.href = "/rank/login";
				});
			} else if (data.message === 'USERACCOUNTERROR') {
				swal({
					title: "抱歉，无法进行查询",
					text: "您的帐号存在异常：您的帐号已注销或被管理员禁用，如有意问请联系客服",
					type: "info",
					showCancelButton: false,
					confirmButtonText: "知道了",
					closeOnConfirm: false,
					showLoaderOnConfirm: true,
				});
			} else {
				defaultCallback(data);
			}
		},

		convertItemRank: function (ranks) {
			var rankStr = '';
			if (ranks % 1 > 0) {//highcharts图中转化为此形态
				rankStr = '.';
				ranks = Math.floor(ranks);
			} else if (ranks > 10000) {//后台直传、未处理的形态
				rankStr = '.';
				ranks = ranks % 10000;
			}
			var i = ranks % 20;
			if (i === 0) {
				rankStr = '总第' + ranks + '名，第' + Math.ceil(ranks / 20) + '页第20名' + rankStr;
			} else {
				rankStr = '总第' + ranks + '名，第' + Math.ceil(ranks / 20) + '页第' + i + '名' + rankStr;
			}
			return rankStr;
		}
	});

})(jQuery, window));