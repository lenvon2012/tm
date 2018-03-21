var TM = TM || {};
$(function() {
	TM.AutoTitleUtil = TM.AutoTitleUtil || {};
	$
			.extend({
				//一键自动橱窗
				isOn : function(me) {

					var isOn = false;
					$.ajax({
						type : "post",
						async : false,
						url : "/windows/isOn",
						success : function(data) {
							isOn = data.res;

							if (isOn) {
								me.children().eq(1).html("[已开启]");
							} else {
								me.children().eq(1).html("[已关闭]");

							}

						},
						error : function() {
							alert("出错了")

						}

					});
					return isOn;

				},
				autoWindow : function() {
					var me = $(".onekey-disabled");

					//            $.ajax({
					//                type:"post",
					//                async:false,
					//                url:"/windows/isOn",
					//                success:function(data){
					//                    console.log("默认值：")
					//                    console.log(data)
					//                },
					//                error:function(){
					//                    console.log("出错了")
					//
					//                }
					//
					//            });
					var isOn = $.isOn(me);

					me.toggle(function() {

						isOn = !isOn;
						$.ajax({
							data : {
								isOn : isOn
							},
							type : "post",
							async : false,
							url : "/windows/turn",
							success : function() {

								if (isOn) {
									me.children().eq(1).html("[已开启]");
								} else {
									me.children().eq(1).html("[已关闭]");

								}

							},
							error : function() {
								alert("出错了")

							}

						})
					}, function() {

						isOn = !isOn;

						$.ajax({
							data : {
								isOn : isOn
							},
							type : "post",
							async : false,
							url : "/windows/turn",
							success : function(data) {
								if (isOn) {
									me.children().eq(1).html("[已开启]");
								} else {
									me.children().eq(1).html("[已关闭]");

								}
							},
							error : function() {
								alert("出错了")

							}

						})
					});
				},
				//一键自动标题
				titleIsOn : function(me) {
					var isOn = false;
					$.ajax({
						type : "post",
						async : false,
						url : "/autocomments/isOn",
						success : function(data) {
							isOn = data.res;

							if (isOn) {
								me.children().eq(1).html("[已开启]");
							} else {
								me.children().eq(1).html("[已关闭]");

							}
						},
						error : function() {
							alert("获取自动标题默认值失败，亲请尝试刷新页面")

						}

					})
					return isOn;
				},

				autoTitle : function() {
					var me = $(".onekey-inverse");
					var isOn = $.titleIsOn(me);
					var setOnOrOff;
					me.click(function() {

						if (isOn) {
							setOnOrOff = "setOff";
						} else {
							setOnOrOff = "setOn";

						}
						isOn = !isOn;
						console.log(isOn)
						$.ajax({
							type : "post",
							async : false,
							url : "/autocomments/" + setOnOrOff,
							success : function() {

								if (isOn) {
									me.children().eq(1).html("[已开启]");
								} else {
									me.children().eq(1).html("[已关闭]");

								}
							},
							error : function() {
								alert("获取自动标题默认值失败，亲请尝试刷新页面")

							}

						})
					});

				},
				//一键去外链

				removeLinks : function() {
					var me = $(".remove-all-item-btn");
					me.click(function() {
						if (confirm("你确定要去除全店所有外链吗？")) {
							$.ajax({
								data : {
									status : 2
								},
								type : "post",
								async : true,
								url : "/removelinks/doRemoveAllItemLinks",
								success : function(data) {

									if (data.success) {
										alert("成功：  " + data.res.length
												+ "  个宝贝受影响");
									} else {
										alert("失败：  " + data.message);

									}

								},
								error : function() {
									alert("获取自动标题默认值失败，亲请尝试刷新页面")

								}

							})

						}
					});
				},
				//一键批量改价
				changeAllPrice : function() {
					var me = $(".onekey-default");
					var body = $("body");
					me
							.click(function() {

								var html = body.find("#maskTable").tmpl({});

								body.mask("<div class='mymask'></div>");
								var mymask = $(".mymask");

								mymask.prepend(html);
								var box = mymask.find('.box');
								box.Tabs();//调用 tabs插件

								//给tabs下的DIV添加选中状态
								box.find(".tab_menu").children("li").mouseover(
										function() {
											var thisIndex = $(this).index();
											box.find(".tab_box").children()
													.removeClass("current");

											box.find(".tab_box").children().eq(
													thisIndex).addClass(
													"current");
										}

								);

								$.initRadioEvent(box);//调用 方法实现百分比和数字之间的切换

								//确认提交
								$(".queding")
										.click(
												function() {
													var submitParam = $
															.getCommonSubmitParam($(".tab_box .current"));
													if (submitParam == null) {
														return;
													}

													$
															.ajax({
																url : "/fenxiaobatch/submitFenxiaoItemPrice",
																data : submitParam,
																type : 'post',
																success : function(
																		dataJson) {
																	console
																			.log("看一下参数")
																	console
																			.log(dataJson)
																	if (dataJson.success
																			&& dataJson.res != null
																			&& dataJson.res.length >= 0) {
																		alert("改价成功"
																				+ dataJson.res.length
																				+ "  条数据受影响!")
																	} else {
																		alert("改价失败  "
																				+ dataJson.message)

																	}

																}
															});

												});

								//关闭遮罩层
								$(".loadmask,.quxiao").click(function() {
									body.unmask();
								});
							});
				},
				getCommonSubmitParam : function(dialogObj) {

					var container = dialogObj;

					var submitParam = {};

					var itemType = "allSearchItem";

					submitParam.itemType = itemType;

					var config = $.getPriceConfig(dialogObj);
					if (config === undefined || config == null) {
						return null;
					}

					submitParam.config = config;

					return submitParam;

				},
				getPriceConfig : function(dialogObj) {

					var modifyPriceTypeObj = dialogObj
							.find('input.modify-price-type-radio:checked');
					if (modifyPriceTypeObj.length <= 0) {
						alert('请先选择改价类型！');
						return null;
					}

					var modifyParameterDivCss = modifyPriceTypeObj
							.attr("target");
					var modifyParameter = dialogObj.find(
							'.' + modifyParameterDivCss).find(
							'.modify-parameter-input').val();

					if (modifyParameter === undefined
							|| modifyParameter == null || modifyParameter == ''
							|| isNaN(modifyParameter) || modifyParameter < 0) {

						alert("请先输入正确的改价幅度，不能为空，且必须为数字，且不能小于0！");
						return null;
					}

					var priceConfig = {};
					priceConfig.modifyType = modifyPriceTypeObj.val();
					priceConfig.modifyParameter = modifyParameter;
					priceConfig.skuEditType = dialogObj.find(
							'input.sku-config-radio:checked').val();

					var minProfit = dialogObj.find('.min-profit-input').val();
					if (minProfit === undefined || minProfit == null
							|| minProfit == '' || isNaN(minProfit)
							|| minProfit < 0) {

						alert("请先输入正确的最低利润，不能为空，且必须为数字，且不能小于0！");
						return null;
					}

					priceConfig.minProfit = minProfit;

					priceConfig.overType = dialogObj.find(
							'input.overflow-config-radio:checked').val();
					priceConfig.decimalType = dialogObj.find(
							'input.dot-config-radio:checked').val();

					return priceConfig;
				},
				initRadioEvent : function(dialogObj) {

					var clickCallback = function(radioObj) {
						var targetDivCss = radioObj.attr("target");
						var targetGroupCss = radioObj.attr("targetGroup");

						if (targetDivCss === undefined || targetDivCss == null
								|| targetDivCss == "") {
							return;
						}
						if (targetGroupCss === undefined
								|| targetGroupCss == null
								|| targetGroupCss == "") {
							return;
						}

						dialogObj.find('.' + targetGroupCss).hide();
						dialogObj.find('.' + targetDivCss).show();
					}

					dialogObj.find(".radio-span").click(
							function() {
								var radioObj = $(this).parent().find(
										'input[type="radio"]');
								radioObj.attr("checked", true);
								clickCallback(radioObj);
							});
					dialogObj.find('input[type="radio"]').click(function() {
						var radioObj = $(this);
						clickCallback(radioObj);
					});
				},
				//一键标题还原
				titleRestore : function() {
					var me = $(".onekey-danger");
					var body = $("body");
					me
							.click(function() {
								body
										.mask("<div class='mymask'> <img class='oneKeyLoadingGif' src='/public/images/newAutoTitle/oneKeyImg/loading.gif' />  </div>");
								var mymask = $(".mymask");
								var htmls = $.loadTmpl(
										"/public/tmpl/allTitleRecord.html",
										"/Titles/batchOpLogs", {
											pn : 1,
											ps : 10000
										});

								mymask.html(htmls);
								//包裹表格以实现滚动条效果
								mymask.find("#noteTable").wrap(
										"<div class='noteDiv'></div>");

								//还原数据
								$.reData();
								//查看详情
								$.seeDetails();

								//关闭弹窗
								$(".loadmask").click(function() {
									body.unmask();
								});
							});
				},
				//获取模板
				loadTmpl : function(htmlUrl, JsonUrl, jsonRequestData) {
					var htmls = null;

					$.ajax({
						type : 'get',
						dataType : "html",
						url : htmlUrl,
						async : false,
						success : function(htmlData) {
							$.ajax({
								data : jsonRequestData,
								type : 'post',
								dataType : "json",
								url : JsonUrl,
								async : false,
								success : function(jsonData) {
									var resleng = jsonData.res.length;
									for (var i = 0; i < resleng; i++) {
										//格式日期时间
										//                               jsonData.res[i].ts=new Date(jsonData.res[i].ts).formatYMSHMS();
										jsonData.res[i].ts = new Date(
												jsonData.res[i].ts)
												.formatYMDHMS();//格式化日期时间

									}

									htmls = $(htmlData).tmpl(jsonData);

								},
								error : function() {
									alert("加载Json出错:" + JsonUrl)
								}
							});

						},
						error : function() {
							alert("加载HTML出错:" + htmlUrl)
						}
					});

					return htmls;
				},
				//获取详情模板
				loadTmplInfo : function(htmlUrl, JsonUrl) {
					var htmls = null;

					$.ajax({
						type : 'get',
						dataType : "html",
						url : htmlUrl,
						async : false,
						success : function(htmlData) {
							$.ajax({
								type : 'post',
								dataType : "json",
								url : JsonUrl,
								async : false,
								success : function(jsonData) {
									console.log(jsonData)

									//格式化数据
									var dataArr = {
										"dataArr" : jsonData
									};

									htmls = $(htmlData).tmpl(dataArr);

								},
								error : function() {
									alert("加载Json出错:" + JsonUrl)
								}
							});

						},
						error : function() {
							alert("加载HTML出错:" + htmlUrl)
						}
					});

					return htmls;
				},
				//还原数据功能
				reData : function() {
					var me = $('.recoverBtn');
					me.click(function() {

						var thisBtn = $(this);//得到当前对象
						var targetId = thisBtn.attr("targetId");
						var succn = thisBtn.attr("succn");
						console.log(succn)
						if (succn < 1) {
							alert("亲，成功优化的宝贝数量为0，无法还原哦！ ");
							return;
						}
						if (confirm("点击确定还原标题")) {

							$.ajax({
								url : "/Titles/recoverBatch",
								data : {
									"id" : targetId
								},
								type : "post",
								datatype : "json",
								async : true,//异步
								success : function(data) {

									alert("提示：亲，" + data.successNum + "个还原成功"
											+ data.failNum + "个还原失败。");

								},
								error : function() {
									alert("提示：哎呀出错了，请尝试刷新页面，如果问题依然存在，请联系我们");

								}
							})
						}
					});

				},
				//查看详情功能
				seeDetails : function() {

					$('.detailBtn').click(
							function() {
								//所有参数都用默认值,并且是遮住整个页面的

								var oThis = $(this);//得到当前对象
								var targetId = oThis.attr("targetId");
								var succn = oThis.attr("succn");

								//如果还原的宝贝数量为0
								if (succn < 1) {
									alert("提示：亲，优化成功的宝贝数量为0，无法查看详情哦");
									return;
								}

								var body = $("body");
								body.mask("<div class='mymask'></div>");

								var htmls = $.loadTmplInfo(
										"/public/tmpl/allTitleRecordInfo.html",
										"/Titles/batchOpLogDetail?id="
												+ targetId);
								var mymask = $(".mymask");

								mymask.append(htmls);

								//关闭遮罩层
								$(".loadmask,.titleClose").click(function() {
									body.unmask();
								});
							});

				}

			});
	//调用一键自动橱窗
	$.autoWindow();
	//调用一键自动标题
	$.autoTitle();
	//调用一键去外链  这个方法会删除全店所有外链
	$.removeLinks();
	//调用一键批量改价
	$.changeAllPrice();
	//调用一键标题还原
//	$.titleRestore();

	//    $([{"name":"柯常青","sex":"男"},{"name":"李双琪","sex":"女"}]).each(function(index,element){
	//           console.log(element.name+"  "+element.sex)
	//    });
})