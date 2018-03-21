var TM = TM || {};

((function($, window) {
	$(function() {
		$.fn.extend({
			tmpage : function(param) {
				init(param, $(this));
				return $(this);
			}
		});

		function init(param, obj) {

			// 每页多少条
			function getPageSizeOption() {
				var pageSizeArray = param.selfPageSizeArray;
				if (pageSizeArray === undefined || pageSizeArray == null
						|| pageSizeArray.length <= 0) {
					var html = '每页显示&nbsp;'
							+ '<span class="small-pagesize-span"><span class="paging-size5 paging-size-span" style="font-weight: bold;color: blue;cursor: pointer;">5</span>&nbsp;|&nbsp;</span>'
							+ '<span class="paging-size10 paging-size-span" style="font-weight: bold;color: blue;cursor: pointer;">10</span>&nbsp;|&nbsp;'
							+ '<span class="paging-size20 paging-size-span" style="font-weight: bold;color: blue;cursor: pointer;">20</span>&nbsp;|&nbsp;'
							+
							// '<span class="paging-size50 paging-size-span"
							// style="font-weight: bold;color: blue;cursor:
							// pointer;">50</span>&nbsp;|&nbsp;' +
							'<span class="paging-size50 paging-size-span" style="font-weight: bold;color: blue;cursor: pointer;">50</span>'
							+ '&nbsp;条&nbsp;';
					return html;
				} else {
					var html = '每页显示&nbsp;';
					$(pageSizeArray)
							.each(
									function(index, selfPageSize) {
										html += '<span class="paging-size'
												+ selfPageSize
												+ ' paging-size-span" style="font-weight: bold;color: blue;cursor: pointer;">'
												+ selfPageSize + '</span>';
										if (index < pageSizeArray.length - 1) {
											html += '&nbsp;|&nbsp;';
										}
									});
					html += '&nbsp;条&nbsp;';
					return html;
				}

			}

			function isSelfDefinePageSize() {
				var pageSizeArray = param.selfPageSizeArray;
				if (pageSizeArray === undefined || pageSizeArray == null
						|| pageSizeArray.length <= 0)
					return false;
				return true;
			}

			// 设置选择每页多少条的事件
			function setPageSizeOptionEvent(obj) {
				/*
				 * obj.children(".paging-size-select").change(function () { var
				 * newPageSize = $(this).val(); pageSize = newPageSize;
				 * //保存到cookie $.cookie(tmPagingSizeCookie, newPageSize,
				 * {expires: 365, path:'/'}); createView(1); });
				 */
				obj.find(".paging-size-span").click(function() {
					var newPageSize = $(this).html();
					pageSize = newPageSize;
					// 保存到cookie
					$.cookie(tmPagingSizeCookie, newPageSize, {
						expires : 365,
						path : '/'
					});
					createView(1);
				});

			}

			// 设置当前的pageSize
			function setCurrentPageSizeOption(obj, curretPageSize) {
				// obj.children(".paging-size-select").val(pageSize);

				var className = "paging-size" + curretPageSize;
				obj.find("." + className).css("color", "#a10000");
				if (getIsUseSmallPageSize() == false) {
					obj.find(".small-pagesize-span").remove();
				}
			}

			var tmPagingSizeCookie = "tmPagingSizeCookie";
			if (param && param instanceof Object) {
				var options;
				var currPage;
				var pageCount;
				var pageSize;
				var tempPage;
				var linkNum = 10;
				var defaults = new Object(
						{
							currPage : 1,
							pageCount : 10,
							pageSize : 5,
							useSmallPageSize : false,
							ajax : {
								on : false,
								pageCountId : 'pageCount',
								param : {
									pn : 1,
									ps : 8
								},
								ajaxStart : function() {
									return false;
								}
							},
							info : {
								next : '',
								prev : '',
								next_on : true,
								prev_on : true,
								msg_on : true,
								link : 'javascript:void(0);',
								msg : '<span>&nbsp;&nbsp;共&nbsp;{sumPage}&nbsp;页&nbsp;'
										+ getPageSizeOption()
										+ '到&nbsp;{currText}&nbsp;页&nbsp;<input  class="wBox_close gopage-submit hand" title="跳转页面" type="button" value="确定"/></span>'
							}
						});

				function getCurrPage() {
					if (typeof options.currPage != 'undefined') {
						return options.currPage;
					} else {
						return defaults.currPage;
					}
				}

				function getPageCount() {
					if (typeof options.pageCount != 'undefined') {
						return options.pageCount;
					} else {
						return defaults.pageCount;
					}
				}

				function getPageSize() {
					if (typeof options.pageSize != 'undefined') {
						return options.pageSize;
					} else {
						return defaults.pageSize;
					}
				}

				function getIsUseSmallPageSize() {
					if (typeof options.useSmallPageSize != 'undefined') {
						return options.useSmallPageSize;
					} else {
						return defaults.useSmallPageSize;
					}
				}

				function getPrev() {
					if (options.info && options.info.prev_on == false) {
						return "";
					}
					if (options.info && options.info.prev) {
						return options.info.prev;
					} else {
						return defaults.info.prev;
					}
				}

				function getNext() {
					if (options.info && options.info.next_on == false) {
						return "";
					}
					if (options.info && options.info.next) {
						return options.info.next;
					} else {
						return defaults.info.next;
					}
				}

				function getLink() {
					if (options.info && options.info.link) {
						return options.info.link;
					} else {
						return defaults.info.link;
					}
				}

				function getAjax() {
					if (options.ajax && options.ajax.on) {
						return options.ajax;
					} else {
						return defaults.ajax;
					}
				}

				function getParam() {
					if (options.ajax.param) {
						options.ajax.param.pn = currPage;
						options.ajax.param.ps = pageSize;
						return options.ajax.param;
					} else {
						defaults.ajax.param.pn = currPage;
						defaults.ajax.param.ps = pageSize;
						return defaults.ajax.param;
					}
				}

				function getPageCountId() {
					if (options.ajax && options.ajax.pageCountId) {
						return options.ajax.pageCountId;
					} else {
						return defaults.ajax.pageCountId;
					}
				}

				function getAjaxStart() {
					if (options.ajax && options.ajax.ajaxStart) {
						options.ajax.ajaxStart();
					} else {
						defaults.ajax.ajaxStart;
					}
				}

				function getMsg() {
					var input = "<input type='text' class='jumpTo' value='"
							+ currPage + "' >";
					var str;
					if (options.info && options.info.msg_on == false) {
						return false;
					}
					str = (options.info && options.info.msg) ? options.info.msg
							: defaults.info.msg;
					str = str.replace("{currText}", input);
					str = str.replace("{currPage}", currPage);
					str = str.replace("{sumPage}", pageCount);

					return str;
				}
			}

			function getText() {
				var msg = getMsg();
				if (msg) {
					msg = $(msg);
				} else {
					return "";
				}
				return msg.html();
			}

			function isCode(val) {
				if (val < 1) {
					TM.Alert.load('输入值不能小于1');

					// alert("输入值不能小于1");
					return false;
				}
				var patrn = /^[0-9]{1,8}$/;
				if (!patrn.exec(val)) {
					TM.Alert.load('请输入正确的数字');

					// alert("请输入正确的数字");
					return false;
				}
				if (val > pageCount) {
					TM.Alert.load('输入值不能大于总页数');

					// alert("输入值不能大于总页数");
					return false;
				}
				return true;
			}

			function updateView() {
				currPage = parseInt(currPage);
				pageCount = parseInt(pageCount);
				var link = getLink();
				var lastPage;
				var firstPage = lastPage = 1;
				if (currPage - tempPage > 0) {
					firstPage = currPage - tempPage;
				} else {
					firstPage = 1;
				}
				if (firstPage + linkNum > pageCount) {
					lastPage = pageCount + 1;
					firstPage = lastPage - linkNum;
				} else {
					lastPage = firstPage + linkNum;
				}
				var content = "";

				if (currPage == 1) {
					content += "<span class=\"page-prev page-prev-disabled\" title=\""
							+ getPrev() + "\"></span>&nbsp;";
				} else {
					content += "<a class='page-prev page-prev-abled' href='"
							+ link + "' title='" + (currPage - 1) + "'>"
							+ getPrev() + "</a>&nbsp;";
				}
				if (firstPage <= 0) {
					firstPage = 1;
				}
				for (firstPage; firstPage < lastPage; firstPage++) {
					if (firstPage == currPage) {
						content += "<span class=\"current\" title=\""
								+ firstPage + "\">" + firstPage
								+ "</span>&nbsp;";
					} else {
						content += "<a href='" + link + "' title='" + firstPage
								+ "'>" + firstPage + "</a>&nbsp;";
					}
				}

				if (currPage == pageCount) {
					content += "<span class=\"page-next page-next-disabled\" title=\""
							+ getNext() + "\"></span>&nbsp;";
				} else {
					content += "<a class='page-next page-next-abled' href='"
							+ link + "' title='" + (currPage + 1) + "'>"
							+ getNext() + " </a>&nbsp;";
				}

				content += getText();
				obj.html(content);
				setCurrentPageSizeOption(obj, pageSize);

				obj.children(":text").keypress(function(event) {
					var keycode = event.which;
					if (keycode == 13) {
						var page = $(this).val();
						if (isCode(page)) {
							// obj.children("a").unbind("click");
							// obj.children("a").each(function() {
							// $(this).click(function() {
							// return false;
							// })
							// });
							createView(page);
						}
					}
				});

				obj.children(":button").click(function() {
					var page = obj.children(":text").val();
					if (isCode(page)) {
						createView(page);
					}
					//console.log(page);
					var items = "";
					$(".boyvonselected").each(function(index,value){
						items += $(this).attr("numiid");
						if(index!=$(".boyvonselected").length-1){
							items += ",";
						}
					});
					$("#select_items").val(items);
					
				});
				setPageSizeOptionEvent(obj);
				obj.children("a").click(function(i) {
					var page = this.title;
					createView(page);
				});

			}

			function createView(page) {
				currPage = page;
				var ajax = getAjax();
				if (ajax.on) {
					getAjaxStart();
					var varUrl = ajax.url;
					var param = getParam();
					// console.info(param);
					$
							.ajax({
								url : varUrl,
								type : 'post',
								data : param,
								contentType : "application/x-www-form-urlencoded;utf-8",
								// async:false,
								dataType : 'json',
								error : function(jqXHR, textStatus) {
									updateView();
									// alert(textStatus);
								},
								success : function(data) {
									if (!data) {
										return;
									}

									if (data.isOk) {
										loadPageCount({
											dataType : ajax.dataType,
											callback : ajax.callback,
											data : data
										});
										updateView();
										return true;
										// } else {
										// // alert(data.message);
										// return false;
										// }
									} else {
										$('#loadingImg').parents('tr').remove();

										if (data.message) {
											TM.Alert.load(data.message);
										}
										// alert(data.message);
									}

								}
							})
				} else {
					updateView();
				}
			}

			function checkParam() {
				if (currPage < 1) {
					TM.Alert.load('配置参数错误\n错误代码:-1');

					// alert("配置参数错误\n错误代码:-1");
					return false;
				}
				/*
				 * if (currPage > pageCount) { TM.Alert.load('配置参数错误\n错误代码:-2');
				 * 
				 * //alert("配置参数错误\n错误代码:-2"); return false; }
				 */
				if (pageSize < 2) {
					TM.Alert.load('配置参数错误\n错误代码:-3');

					// alert("配置参数错误\n错误代码:-3");
					return false;
				}
				return true;
			}

			function loadPageCount(options) {
				if (options.dataType) {
					var formData;
					var data = options.data;
					var resultPageCount = false;
					var isB = true;
					var pageCountId = getPageCountId();
					var callback;
					switch (options.dataType) {
					case "json":
						// data =$.parseJSON(data);
						// resultPageCount = eval("data." + pageCountId);
						formData = options.data;
						resultPageCount = formData.pnCount;
						break;
					case "xml":
						resultPageCount = $(data).find(pageCountId).text();
						break;
					default:
						isB = false;
						options.callback && options.callback(data);
						resultPageCount = $("#" + pageCountId).val();
						break;
					}
					if (resultPageCount) {
						pageCount = resultPageCount;
					}
					if (isB) {
						options.callback && options.callback(data);
					}
				}
			}
			options = param;
			currPage = getCurrPage();
			pageCount = getPageCount();

			if (getIsUseSmallPageSize() == true) {
				tmPagingSizeCookie = "SmallPageSize_" + tmPagingSizeCookie;
			}
			var cookiePageSize = $.cookie(tmPagingSizeCookie);
			if (cookiePageSize == null || cookiePageSize <= 0) {
				pageSize = getPageSize();
			} else {
				pageSize = cookiePageSize;
			}
			if (pageSize <= 8 && getIsUseSmallPageSize() == true)
				pageSize = 5;
			else if (pageSize <= 15)
				pageSize = 10;
			else if (pageSize <= 30)
				pageSize = 20;
			else
				pageSize = 20;

			// 天猫联盟中，降权分页
			if (isSelfDefinePageSize() == true) {
				tmPagingSizeCookie = "SelfDefinePageSize_" + tmPagingSizeCookie;
				cookiePageSize = $.cookie(tmPagingSizeCookie);
				if (cookiePageSize == null || cookiePageSize <= 0) {
					pageSize = getPageSize();
				} else {
					pageSize = cookiePageSize;
				}
			}

			tempPage = parseInt(linkNum / 2);
			if (checkParam() && createView(currPage)) {
				updateView();
			}
		}
	});

})(jQuery, window));

((function($, window) {
	TM.jinnang = TM.jinnang || {};
	var jinnang = TM.jinnang;
	jinnang.util = jinnang.util || {};
	jinnang.util = $.extend({
        trim: function (str) {
            return str.replace(/(^\s*)|(\s*$)/g, "");
        },
        ltrim: function (str) {
            return str.replace(/(^\s*)/g,""); //删除左边空格
        },
        rtrim: function (str) {
            return str.replace(/(\s*$)/g,""); //删除右边空格
        }
	},jinnang.util);

	// 导航基础功能
	jinnang.Main = jinnang.Main || {};
	jinnang.Main = $.extend({
		init:function(){
			$.ajax({
				url : '/JinNangZheKou/getUserInfo',
				data : {},
				type : 'get',
				success : function(dataJson) {
					var userJson = dataJson;
					var level = userJson.level;
					if (level <= -1) {
						jinnang.isTryLevel = true;
					} else {
						jinnang.isTryLevel = false;
				    }	
					jinnang.Main.initEvents();
				}
			});
		},
		initEvents:function(){
			jinnang.Event.setSyncDataEvent();
		}
	},jinnang.Main);
	// 生成标签
	jinnang.UserTagCnt =jinnang.UserTagCnt || {};
	jinnang.UserTagCnt = $.extend({
		init:function(){
			$.ajax({
				url : '/JinNangZheKou/getUserInfo',
				data : {},
				type : 'get',
				success : function(dataJson) {
					var userJson = dataJson;
					var level = userJson.level;
					if (level <= -1) {
						jinnang.isTryLevel = true;
					} else {
						jinnang.isTryLevel = false;
				    }	
					jinnang.UserTagCnt.initEvents();
					
				}
			});
		},
		initEvents:function(){
			jinnang.Event.setCreateUserTagEvent();
		}
	},jinnang.UserTagCnt);
	
	jinnang.UserTagList = jinnang.UserTagList || {};
	jinnang.UserTagList = $.extend({
		init:function(){
			$.ajax({
				url : "/JinNangZheKou/getUserInfo",
				data : {},
				type : 'get',
				success : function(dataJson) {
					var userJson = dataJson;
					var level = userJson.level;
					if (level <= -1) {
						jinnang.isTryLevel = true;
					} else {
						jinnang.isTryLevel = false;
				    }	
					jinnang.UserTagList.initEvents();
				}
			});
		},
		initEvents:function(){
			jinnang.Event.setEditUserTagEvent();
			jinnang.Event.setDeleteUserTagEvent();
		}
	},jinnang.UserTagList);
	// 生成活动
	jinnang.ActivityCnt = jinnang.ActivityCnt || {};
	jinnang.ActivityCnt = $.extend({
		init : function() {
			$.ajax({
				url : '/JinNangZheKou/getUserInfo',
				data : {},
				type : 'get',
				success : function(dataJson) {
					var userJson = dataJson;
					var level = userJson.level;
					if (level <= -1) {
						jinnang.isTryLevel = true;
					} else {
						jinnang.isTryLevel = false;
				    }	
					jinnang.ActivityCnt.initDateTimePicker();
					jinnang.ActivityCnt.initEvents();
				}
			})
		},
		initDateTimePicker : function() {
			
			$("#start_date").datetimepicker({
				showTimePicker: true
			});
			$("#end_date").datetimepicker({
				showTimePicker: true
			});
			var current = new Date();
			current.setHours(0);
			current.setMinutes(0);
			current.setSeconds(0);
			
			if($("#start_date").val().length==0){
				$("#start_date").datepicker("setDate", current)
			}
			if($("#end_date").val().length==0){
				$("#end_date").datepicker("setDate",current);
			}
			
		},
		initEvents : function(){
			jinnang.Event.setCreateActivityEvent();
			jinnang.Event.setActivityTypeChangeEvent();
			jinnang.Event.setManualDiscountChangeEvent();
			jinnang.Event.setSelectItemsEvent();
        },
		initUserInfo : function() {
				$.get("/JinNangZheKou/getUserInfo",
								function(data) {
									$.cookie("version",
											data.version);
									$.cookie("award", data.award);
									if ($.cookie("award") == "true") {
										$.cookie("totalNum",
												data.totalNum + 1);
									} else {
										$.cookie("totalNum",
												data.totalNum);
									}
									$.cookie("level", data.level);
									$.cookie("popularizedNum",
											data.popularizedNum);
				});
		}
	},jinnang.ActivityCnt);
	jinnang.VIP = jinnang.VIP || {};
	jinnang.VIP = $.extend({
		init:function(){
			$.ajax({
				url : '/JinNangZheKou/getUserInfo',
				data : {},
				type : 'get',
				success : function(dataJson) {
					var userJson = dataJson;
					var level = userJson.level;
					if (level <= -1) {
						jinnang.isTryLevel = true;
					} else {
						jinnang.isTryLevel = false;
				    }	
					jinnang.VIP.initEvents();
				}
			});
		},
		initEvents:function(){
			jinnang.Event.setSaveVIPConfigEvent();
			jinnang.Event.setViewVIPEvent();
			jinnang.Event.setDownloadVipEvent();
			jinnang.Event.setAddVipEvent();
		}
	},jinnang.VIP);
	
	jinnang.ActivityList = jinnang.ActivityList || {};
	jinnang.ActivityList = $.extend({
		init:function(){
			$.ajax({
				url : '/JinNangZheKou/getUserInfo',
				data : {},
				type : 'get',
				success : function(dataJson) {
					var userJson = dataJson;
					var level = userJson.level;
					if (level <= -1) {
						jinnang.isTryLevel = true;
					} else {
						jinnang.isTryLevel = false;
				    }	
					jinnang.ActivityList.initEvents();
				}
			});
		},
		initEvents:function(){
			jinnang.Event.setFilterActivityEvent();
			jinnang.Event.setDeleteActivityEvent();
			jinnang.Event.setEditActivityEvent();
		}
	});
	
	jinnang.Event = jinnang.Event || {};
	jinnang.Event = $.extend({
		setCreateActivityEvent:function(){
			
			$("#create_or_update").click(function(){
				
				var data = {};
				var title;
				var description;
				var promotionPolicy;
				var startDate;
				var endDate;
				var items = [];
				var userTag;
				var activityType;
				var discountValue;
				var discountRate;
				var reduceCount;
				// check page parameters
				data.title = title = $("#act_title").val();
				if(title==null || title.length==0 || jinnang.util.trim(title)==""){
					alert("活动标题不能为空！");
					return;
				}
				
				data.description = description = $("#act_desp").val();
				if(description==null || description.length==0 || jinnang.util.trim(description)==""){
					alert("活动描述不能为空！");
					return;
				}
				
				data.promotionPolicy = promotionPolicy = $("input:radio[name=promotion_policy]:checked").val();
				/*
				if(promotionPolicy!="UNIQUE" && promotionPolicy!="CUSTOMIZE"){
					alert("优惠策略设置错误！");
					return;
				}
				*/
				data.startDate = startDate = $("#start_date").val();
				data.endDate = endDate = $("#end_date").val();
				if(startDate==null || endDate==null){
					if(startDate==null){
						alert("活动开始时间没有设置!");
						return;
					}
					if(endDate==null){
						alert("活动结束时间没有设置!");
						return;
					}
				}
                
				data.items = items = $("#select_items").val();
                // TODO,remove all non number items
                
				if(items == null || items.length == 0){
					alert("没有选择商品!");
					return;
				}
				
				data.userTag = userTag = $("#user_tag").val();
				if(userTag == null || userTag==""){
					alert("用户标签设置错误!");
					return;
				}
				
				data.activityType = activityType = $('input:radio[name=activity_type]:checked').val();
				if(activityType!="PRICE" && activityType!="DISCOUNT"){
					alert("活动类型设置错误！");
					return;
				}
				
				data.decreaseNum = reduceCount = $("#reduce_count").val();
				data.discountValue = discountValue = $("#discount_value").val();
				data.discountValue = discountRate = $('input:radio[name=discount_rate]:checked').val();
				if(activityType == "PRICE"){
					if(discountValue==null || isNaN(parseFloat(discountValue)) || !isFinite(discountValue)){
						alert("减价额度设置错误!");
						return;
					}
					// 设置减价件数
					if(reduceCount!="1" && reduceCount!="0"){
						alert("减价件数设置错误!");
						return;
					}
				}else if(activityType == "DISCOUNT"){
					// 折扣设置检查
					if(discountRate==null){
						discountRate = $("#manual_set_discount_value").val();
						if(discountRate==null || isNaN(parseFloat(discountRate)) || !isFinite(discountRate)){
							alert("折扣设置错误！");
							return;
						}
					}else{
						if(isNaN(parseFloat(discountRate)) || !isFinite(discountRate)){
							alert("折扣设置错误！");
							return;
						}
					}
				}else{
					alert("活动类型设置错误！");
					return;
				}
                
                
				// ajax添加活动
                
				$.ajax({
					url:"/JinNangZheKou/addActivity",
					data:{
						title : data.title,
						description : data.description,
						startTimeStr : data.startDate,
						endTimeStr : data.endDate,
						discountValue : data.discountValue,
						activityType : data.activityType,
						userTagId : data.userTag,
						decreaseNum : data.decreaseNum,
						items:data.items
					},
					type: 'post',
					success: function(dataJson){
						alert(dataJson);
					},
					error : function(request, textStatus){
						alert('error!');
					}
			    });
			});
		},
		setActivityTypeChangeEvent:function(){
			$("input:radio[name=activity_type]").change(function(){
				var type = $(this).val();
				if(type=="DISCOUNT"){
					$("#set_discount_container").show();
					$("#set_reduce_container").hide();
				}else if(type=="PRICE"){
					$("#set_discount_container").hide();
					$("#set_reduce_container").show();
				}else{
					// should not be here
				}
			});
		},
		setManualDiscountChangeEvent:function(){
			
			$("#manual_set_discount_value").change(function(){
				var value = $(this).val();
				if(value==null || value.length==0){
					$('input:radio[name=discount_rate]').attr('disabled', false);
				}else{
					$('input:radio[name=discount_rate]').attr('disabled', true);
				}
			});
		},
		setSelectItemsEvent:function(){
			$("#select_items_btn").click(function() {
				var box = $('.wbox').wBox({
					title : '商品选择',
					target : "#select_items_tb"
				});
				box.showBox();
			});
		},
		setCreateUserTagEvent:function(){
			
			$("#create_or_update").click(function(){
				var data = {};
				var tagId;
				var tagName;
				var tagDesp;
				var userList;
				data.tagName = tagName = $("#tag_name").val();
				data.tagDesp = tagDesp = $("#tag_desp").val();
				data.userList = userList = $("#user_list").val();
				if(tagName==null || tagName.length==0){
					alert("标签名称不能为空!");
					return;
				}
				if(tagDesp==null || tagDesp.length==0){
					alert("标签描述不能为空!");
					return;
				}
				data.tagId = tagId = $("#tag_id").val();
				if(tagId==null || jinnang.util.trim(tagId)==""){
					$.ajax({
						url:"/JinNangZheKou/addUserTag",
						data:{
							name:data.tagName,
							description:data.tagDesp
						},
						type: 'post',
						success: function(dataJson){
							$("#tag_id").val(dataJson.res);
							$("#create_or_update").val("更新");
							$("#user_list").attr("readonly",false);
							//set update env
							alert(JSON.stringify(dataJson));
						},
						error : function(request, textStatus){
							alert('error!');
						}
					});
				}else{
					$.ajax({
						url:"/JinNangZheKou/editUserTag",
						data:{
							name:data.tagName,
							description:data.tagDesp,
							userTagId:data.tagId
						},
						type: 'post',
						success: function(dataJson){
							alert(dataJson);
						},
						error : function(request, textStatus){
							alert('error!');
						}
					});
				}
			});
			
			//add user action
			$("#add_user_btn").click(function(){
				var userList = $("#user_list").val();
				if(userList==null || jinnang.util.trim(userList)==""){
					//do nothing
				}else{
					var userTagId = $("#tag_id").val();
					$.ajax({
						url:"/JinNangZheKou/userTagAddUser",
						data:{
							userTagId:userTagId,
							userList:userList
						},
						type:'post',
						success:function(dataJson){
							alert(JSON.stringify(dataJson));
							var successUserList = dataJson.res;
							for (var i = 0; i < successUserList.length; i++) { 
								var tableLine = '<tr><td>'+successUserList[i]+'</td><td>删</td></tr>'
								$("#side_user_list").append(tableLine);
							}
						},
						error:function(request,textStatus){
							alert(JSON.stringify(textStatus));
						}
					});
				}
			});
			
		},
		setEditUserTagEvent:function(){
			
			$.each($(".row-actions > div"),function(i,l){
				var anchor = $(l).siblings("span.edit").children("a");
				$(anchor).bind("click",function(){
					var tagId = $(l).text();
					var result = false;
					$.ajax({
						url:"/JinNangZheKou/checkUserTagEditable",
						data:{
							userTagId : tagId
						},
						type : 'post',
						success:function(dataJson){
							//alert(JSON.stringify(dataJson));
							if(dataJson.isOk){
								location.href=$(anchor).attr("href");
							}else{
								alert(JSON.stringify(dataJson));
							}
							result = true;
						},
						error:function(request,textStatus){
							alert("error!");
							result = false;
						}
					});
					return result;
				});
			});
			
		},
		setDeleteUserTagEvent:function(){
			$.each($(".row-actions > div"),function(i,l){
				var anchor = $(l).siblings("span.trash").children("a");
				$(anchor).bind("click",function(){
					var ifDelete = confirm("真的要删除用户标签?");
					var resutl = false;
					var tagId = $(l).text();
					var result = false;
					if(ifDelete){
						$.ajax({
							url:"/JinNangZheKou/checkUserTagDeletable",
							data:{
								userTagId : tagId
							},
							type : 'post',
							success:function(dataJson){
								//alert(JSON.stringify(dataJson));
								if(dataJson.isOk){
									result = true;
									$.ajax({
										url:"/JinNangZheKou/removeUserTag",
										data:{
											userTagId : tagId
										},
										type: 'post',
										success:function(dataJson){
											if(dataJson.isOk){
												location.href=$(anchor).attr("href");
											}else{
												alert(JSON.stringify(dataJson));
											}
										},
										error:function(){
											alert("error!");
										}
									});
									return result;
								}else{
									alert(JSON.stringify(dataJson));
								}
								result = true;
							},
							error:function(request,textStatus){
								alert("error!");
								result = false;
							}
						});
					}else{
						//do nothing
					}
					return result;
				});
			});
		},
		setSaveVIPConfigEvent:function(){
			$("#save_vip_config").click(function(){
				//save vip configuration
				//TODO should set more approprivate data validation
				
				var type = $("input:radio[name=policy]:checked").val();
				
				if(type!="COUNT" && type!="AMOUNT"){
					alert("分级策略设置不正确!");
					return;
				}
				
				var quantity = new Array();
				var discount = new Array();
				quantity[0] = 0;
				quantity[1] = $("#vip2_amount").val();
				quantity[2] = $("#vip3_amount").val();
				quantity[3] = $("#vip4_amount").val();
				if(
						quantity[1]==null || parseInt(quantity[1],10)!=quantity[1] ||
						quantity[2]==null || parseInt(quantity[2],10)!=quantity[2] ||
						quantity[3]==null || parseInt(quantity[3],10)!=quantity[3]){
					alert('交易量(额)设置不正确!');
					return;
				}
				discount[0] = $("#vip1_discount").val();
				discount[1] = $("#vip2_discount").val();
				discount[2] = $("#vip3_discount").val();
				discount[3] = $("#vip4_discount").val();
				if(discount[0]==null || parseInt(discount[0],10)!=discount[0] ||
						discount[1]==null || parseInt(discount[1],10)!=discount[1] ||
						discount[2]==null || parseInt(discount[2],10)!=discount[2] ||
						discount[3]==null || parseInt(discount[3],10)!=discount[3]){
					alert('会员优惠设置不正确!');
					return;
				}
				$.ajax({
					url:"/JinNangZheKou/saveVipConfig",
					data:{
						type:type,
						quantity:quantity,
						discount:discount
					},
					type:'post',
					success:function(dataJson){
						alert(JSON.stringify(dataJson));
					},
					error:function(request,textStatus){
						alert("error!");
					}
				});
				
			});
		},
		setViewVIPEvent:function(){
			$("#view_vip_btn").click(function(){
			var vipLevel = $("#vip_select").val();
			console.log(vipLevel);
				if(vipLevel==null || vipLevel.length==0){
					alert("错误的会员等级!");
				}else{
					$.ajax({
						url:"/JinNangZheKou/viewVipList",
						data:{
							vipLevel:vipLevel
						},
						success:function(dataJson){
							if(dataJson.isOk){
								$("#side_user_list").empty();
								var successUserList = dataJson.res;
								for (var i = 0; i < successUserList.length; i++) { 
									var tableLine = '<tr><td>'+successUserList[i]+'</td></tr>'
									$("#side_user_list").append(tableLine);
								}
							}else{
								alert(dataJson.msg);
							}
						},
						error:function(request,textStatus){
							
						}
					});
				}
			});
		},
		setFilterActivityEvent:function(){
			$("#activity-query-submit").click(function(){
				var cat = $("#cat").val();
				windows.href = "/JinNangZheKou/activity_mnt?page=1&cat="+cat;
			});
		},
		setDeleteActivityEvent:function(){
			$.each($(".row-actions > div"),function(i,l){
				var anchor = $(l).siblings("span.trash").children("a");
				$(anchor).bind("click",function(){
					var tagId = $(l).text();
					var result = false;
					$.ajax({
						url:"/JinNangZheKou/checkUserTagEditable",
						data:{
							userTagId : tagId
						},
						type : 'post',
						success:function(dataJson){
							//alert(JSON.stringify(dataJson));
							if(dataJson.isOk){
								location.href=$(anchor).attr("href");
							}else{
								alert(JSON.stringify(dataJson));
							}
							result = true;
						},
						error:function(request,textStatus){
							alert("error!");
							result = false;
						}
					});
					return result;
				});
			});
		},
		setEditActivityEvent:function(){
			
		},
		setDownloadVipEvent : function(){
			
		},
		setAddVipEvent : function(){
			$("#add_user").click(function(){
				var userNick = $("#user_nick").val();
				var vipLevel = $("#vip_add_select").val();
				if(userNick==null || userNick.length==0){
					alert("新，用户昵称不能为空！");
					return;
				}
				if(vipLevel==null || vipLevel.length==0){
					alert("用户等级选择错误！");
					return;
				}
				$.ajax({
					url:"/JinNangZheKou/addVipMember",
					type:"post",
					data:{
						userNick:userNick,
						vipLevel:vipLevel
					},
					success:function(dataJson){
						if(dataJson.isOk){
							alert("成功！");
						}else{
							alert("失败");
						}
					},
					error:function(request,textStatus){
						alert("error!");
					}
				})
			});
		},
		setSyncDataEvent:function(){
			$("#sync_data").bind("click",function(){
			    $.get("/items/sync");
			});
		}
	},jinnang.Event);
})(jQuery, window));

((function($, window) {
	TM.boyvon = TM.boyvon || {};
	var boyvon = TM.boyvon;
	boyvon.allItems = boyvon.allItems || {};
	boyvon.allItems = $
			.extend(
					{
						init : function() {
							$.ajax({
								url : '/JinNangZheKou/getUserInfo',
								data : {},
								type : 'get',
								success : function(dataJson) {
									var userJson = dataJson;
									var level = userJson.level;
									if (level <= -1) {
										boyvon.isTryLevel = true;
									} else {
										boyvon.isTryLevel = false;
									}
									// boyvon.allItems.initUserInfo();
									boyvon.allItems.initSearchArea();
									boyvon.allItems.get();
								}
							});

						},
						initUserInfo : function() {
							$
									.get(
											"/JinNangZheKou/getUserInfo",
											function(data) {
												$.cookie("version",
														data.version);
												$.cookie("award", data.award);
												if ($.cookie("award") == "true") {
													$.cookie("totalNum",
															data.totalNum + 1);
												} else {
													$.cookie("totalNum",
															data.totalNum);
												}
												$.cookie("level", data.level);
												$.cookie("popularizedNum",
														data.popularizedNum);
											});
						},
						get : function(params) {
							var container = $('.allItemsDiv');
							container.empty();
							var params = $.extend({
								"s" : "",
								"polularized" : 2,
								"catId" : null,
								"sort" : 3
							}, boyvon.util.getParams());
							var bottom = $('<div class="popularizeBottom pagenavi" style="text-align: center;"></div>');

							params.popularizeStatus = 5;// 包含体验版

							bottom
									.tmpage({
										currPage : 1,
										pageSize : 12,
										pageCount : 1,
										selfPageSizeArray : [ 12, 16, 20 ],
										ajax : {
											param : params,
											on : true,
											dataType : 'json',
											url : "/JinNangZheKou/searchItems",
											callback : function(data) {
												container.empty();
												container.find(
														".popularizeBottom")
														.remove();
												container
														.append(boyvon.allItems
																.createItemsHead());
												container
														.append(boyvon.allItems
																.createUserInfo());
												container
														.append(boyvon.allItems
																.createItemsBody(data));
												var height = boyvon.util
														.updateUlHeight(data.res.length);
												// container.find('.pic-list2').css("height",height+"px");
												bottom.appendTo(container);
												boyvon.Event.setDynamicEvent();
												bottom.find(".page-next")
														.empty();
												
											}
										}

									});
						},
						initSearchArea : function() {
							/*
							 * $.get("/items/sellerCatCount",function(data){ var
							 * sellerCat = $('#itemsCat'); sellerCat.empty();
							 * if(!data || data.length == 0){ sellerCat.hide(); }
							 * 
							 * var exist = false; var cat = $('<option>自定义类目</option>');
							 * sellerCat.append(cat); for(var i=0;i<data.length;i++) {
							 * if(data[i].count <= 0){ continue; }
							 * 
							 * exist = true; var option = $('<option></option>');
							 * option.attr("catId",data[i].id);
							 * option.html(data[i].name+"("+data[i].count+")");
							 * sellerCat.append(option); } if(!exist){
							 * sellerCat.hide(); } });
							 * $.get("/items/itemCatCount",function(data){ var
							 * taobaoCat = $('#taobaoCat'); taobaoCat.empty();
							 * if(!data || data.length == 0){ taobaoCat.hide(); }
							 * 
							 * var exist = false; var cat = $('<option>淘宝类目</option>');
							 * taobaoCat.append(cat); for(var i=0;i<data.length;i++) {
							 * if(data[i].count <= 0){ continue; }
							 * 
							 * exist = true; var option = $('<option></option>');
							 * option.attr("catId",data[i].id);
							 * option.html(data[i].name+"("+data[i].count+")");
							 * taobaoCat.append(option); } if(!exist){
							 * taobaoCat.hide(); } });
							 */
							$
									.get(
											"/items/sellerCatCount",
											function(data) {
												// here is the clicksearch
												// seller cat btn
												var clicksearchcat = $('.skinsellercat');
												clicksearchcat.empty();
												if (!data || data.length == 0) {
													clicksearchcat.hide();
												}
												var flag = false;
												clicksearchcat
														.append($('<span class="inlineblock clicksearchcatbtn selectcolor" tag="catall"><input type="radio" name="sellercatgory" checked="true">自定义类目</span>'));
												var catname = "";
												for ( var i = 0; i < data.length; i++) {
													if (data[i].count <= 0) {
														continue;
													}

													flag = true;
													catname = data[i].name
															+ "("
															+ data[i].count
															+ ")";
													clicksearchcat
															.append($('<span class="inlineblock clicksearchcatbtn" tag="'
																	+ data[i].id
																	+ '"><input type="radio" name="sellercatgory">'
																	+ catname
																	+ '</span>'));
												}
												if (!flag) {
													clicksearchcat.hide();
												}
												boyvon.Event
														.setSellerCatClickEvent();
											});
							$
									.get(
											"/items/itemCatCount",
											function(data) {
												// here is the clicksearch
												// taobao cat btn
												var clicksearchcat = $('.skintaobaocat');
												clicksearchcat.empty();
												if (!data || data.length == 0) {
													clicksearchcat.hide();
												}
												var flag = false;
												clicksearchcat
														.append($('<span class="inlineblock clicksearchcatbtn selectcolor" tag="catall"><input type="radio" name="taobaocatgory" checked="true">淘宝类目</span>'));
												var catname = "";
												for ( var i = 0; i < data.length; i++) {
													if (data[i].count <= 0) {
														continue;
													}

													flag = true;
													catname = data[i].name
															+ "("
															+ data[i].count
															+ ")";
													clicksearchcat
															.append($('<span class="inlineblock clicksearchcatbtn" tag="'
																	+ data[i].id
																	+ '"><input type="radio" name="taobaocatgory">'
																	+ catname
																	+ '</span>'));
												}
												if (!flag) {
													clicksearchcat.hide();
												}
												boyvon.Event
														.setTaobaoCatClickEvent();
											});
						},
						createItemsHead : function() {
							var head = $('<div class="diagHead"></div>');
							var ulObj = $('<ul class="diagHeadUL"></ul>');
							ulObj
									.append($('<li class="fl" style="width:120px;text-align: center;"><span class="batchSelectUsePopularize ">添加选中</span></li>'));
							ulObj
									.append($('<li class="fl" style="width:20px;height: 45px;"></li>'));
							ulObj
									.append($('<li class="fl" style="width:120px;text-align: center;"><span class="batchSelectRemovePopularize ">取消选中</span></li>'));
							ulObj
									.append($('<li class="fl" style="width:20px;height: 45px;"></li>'));
							ulObj
									.append($('<li class="fl" style="width:120px;text-align: center;"><span class="batchAllAddPopularize">全部选中</span></li>'));
							ulObj
									.append($('<li class="fl" style="width:20px;height: 45px;"></li>'));
							/*
							ulObj
									.append($('<li class="fl" style="width:120px;text-align: center;"><span class="batchAllRemovePopularize">全店</span></li>'));
							*/
							head.append(ulObj);
							return head;
						},
						createUserInfo : function() {
							var userInf = $('<div class="userInf"></div>');
							$
									.get(
											"/Popularize/getUserInfo",
											function(data) {
												$.cookie("version",
														data.version);
												$.cookie("award", data.award);
												if ($.cookie("award") == "true") {
													$.cookie("totalNum",
															data.totalNum + 1);
												} else {
													$.cookie("totalNum",
															data.totalNum);
												}
												$.cookie("level", data.level);
												$.cookie("popularizedNum",
														data.popularizedNum);

												var remain = $
														.cookie("totalNum")
														- $
																.cookie("popularizedNum");
												var totalNum;
												if ($.cookie("award") == "true") {
													totalNum = $
															.cookie("totalNum")
															+ "(+1)";
												} else {
													totalNum = $
															.cookie("totalNum");
												}
												/*
												userInf
														.append($('<div class="userInfDiv"><span class="inlineblock">用户版本：</span><span class="inlineblock version infoNum">'
																+ $
																		.cookie("version")
																+ '</span><span class="inlineblock">可推广宝贝总数：</span><span class="inlineblock totalNum infoNum">'
																+ totalNum
																+ '</span></div>'));
												userInf
														.append($('<div class="userInfDiv"><span class="inlineblock">已推广数：</span><span class="inlineblock popularized infoNum">'
																+ $
																		.cookie("popularizedNum")
																+ '</span><span class="inlineblock">还可推广宝贝数：</span><span class="inlineblock remain infoNum">'
																+ remain
																+ '</span></div>'));
											   */

											});
							return userInf;
						},
						createItemsBody : function(data) {
							var itembody = $('<ul class="pic-list2 clearfix"></ul>');
							if (data.res != null && data.res.length > 0) {
								$(data.res).each(
										function(i, oneitem) {
											itembody.append(boyvon.allItems
													.createItem(oneitem));
										})
							}
							itembody.find(".selectPic").click(function() {
								if ($(this).hasClass("boyvonunselected")) {
									$(this).removeClass("boyvonunselected");
									var pic = $(this).parent().find('.pic');
									pic.addClass("borderon");
									pic.removeClass("borderoff");
									$(this).addClass("boyvonselected");
								} else {
									$(this).removeClass("boyvonselected");
									// $(this).parent().find('.pic').removeClass("borderon");
									var pic = $(this).parent().find('.pic');
									pic.addClass("borderoff");
									pic.removeClass("borderon");
									$(this).addClass("boyvonunselected");
								}
							});
							itembody
									.find(".pic")
									.click(
											function() {
												if ($(this).parent().find(
														'.selectPic').hasClass(
														"boyvonunselected")) {
													$(this)
															.parent()
															.find('.selectPic')
															.removeClass(
																	"boyvonunselected");
													$(this)
															.addClass(
																	"borderon");
													$(this).removeClass(
															"borderoff");
													$(this)
															.parent()
															.find('.selectPic')
															.addClass(
																	"boyvonselected");
												} else {
													$(this)
															.parent()
															.find('.selectPic')
															.removeClass(
																	"boyvonselected");
													$(this).removeClass(
															"borderon");
													$(this).addClass(
															"borderoff");
													$(this)
															.parent()
															.find('.selectPic')
															.addClass(
																	"boyvonunselected");
												}
											});
							itembody
									.find('.switch')
									.click(
											function() {
												if ($(this)
														.hasClass("switchOn")) {
													// if(boyvon.util.isFree($.cookie("level")))
													// {
													// TM.Alert.load("您现在是体验版，不能取消已推广宝贝~",400,230);
													// } else {
													$(this).removeClass(
															"switchOn");
													$(this).addClass(
															"switchOff");
													var numIid = $(this)
															.parent()
															.find('.selectPic')
															.attr("numIid");
													var dataParams = {
														numIids : numIid
													};

													if (boyvon.isTryLevel == true) {// 体验版
														TM.Alert
																.load(
																		"体验版不能修改推广位，请先升级~",
																		400,
																		200);
														return;
													}

													dataParams.status = 5;
													$
															.post(
																	"/Popularize/removePopularized",
																	dataParams,
																	function() {
																		$
																				.cookie(
																						"popularizedNum",
																						parseInt($
																								.cookie("popularizedNum")) - 1);
																		$(
																				'.popularized')
																				.text(
																						$
																								.cookie("popularizedNum"));
																		$(
																				'.remain ')
																				.text(
																						$
																								.cookie("totalNum")
																								- $
																										.cookie("popularizedNum"));
																		TM.Alert
																				.load(
																						"宝贝已从推广计划中取消~",
																						400,
																						200);
																	});
													// }
												} else {
													if (parseInt($
															.cookie("totalNum")) > parseInt($
															.cookie("popularizedNum"))) {

														var numIid = $(this)
																.parent()
																.find(
																		'.selectPic')
																.attr("numIid");
														var dataParams = {
															numIids : numIid
														};
														if (boyvon.isTryLevel == true) {// 体验版
															dataParams.status = 4;
															if (confirm("体验版推广位开启后不能再更换宝贝，确定要推广该宝贝？") == false)
																return;
														}
														$(this).removeClass(
																"switchOff");
														$(this).addClass(
																"switchOn");
														$
																.post(
																		"/Popularize/addPopularized",
																		dataParams,
																		function() {
																			$
																					.cookie(
																							"popularizedNum",
																							parseInt($
																									.cookie("popularizedNum")) + 1);
																			$(
																					'.popularized')
																					.text(
																							$
																									.cookie("popularizedNum"));
																			$(
																					'.remain ')
																					.text(
																							$
																									.cookie("totalNum")
																									- $
																											.cookie("popularizedNum"));
																			TM.Alert
																					.load(
																							"宝贝已添加到推广计划中~",
																							400,
																							200,
																							function() {
																								if (boyvon.isTryLevel == true) {// 体验版
																									location
																											.reload();
																								}
																							});

																		});
													} else {
														TM.Alert
																.load(
																		"您的推广位已经用完，请<a target='_blank' style='color: blue;' href='http://fuwu.taobao.com/ser/detail.htm?spm=a1z13.1113643.51050017.101.AmcWkq&service_code=FW_GOODS-1848326&tracelog=category&scm=1215.1.1.51050017&ppath='>升级</a>后再试~",
																		400,
																		200);
													}
												}
											});
							return itembody;
						},
						createItem : function(oneitem) {
							var item = $('<li class="photo-list-padding"></li>');
							var url = "http://item.taobao.com/item.htm?id="
									+ oneitem.id;
							item
									.append($('<a href="javascript:void(0)" class="boyvonunselected selectPic" numIid="'
											+ oneitem.id + '"></a>'));
							item
									.append($('<a class="pic borderoff" href="javascript:void(0)"><img width="208px" height="208px" title="'
											+ oneitem.title
											+ '" src="'
											+ oneitem.picURL + '"></a>'));
							item
									.append($('<a target="_blank" class="title inlineblock" href="'
											+ url
											+ '"><span title="'
											+ oneitem.title
											+ '"><em>'
											+ oneitem.title
											+ '</em></span></a>'));
							item.append($('<div class="shadow"></div>'));
							if (boyvon.isTryLevel == true
									&& oneitem.popularized) {
								var html = ''
										+ '<div class="status" style="margin: 0 auto;">'
										+ '   <a target="_blank" class="look-popularized-btn">查看推广</a> '
										+ '</div> ' + '';
								var btnObj = $(html);
								btnObj.find(".look-popularized-btn").attr(
										"href",
										"/tryshare?numIid=" + oneitem.id);
								item.append(btnObj);
							} else {
								/*
								item
										.append($('<span class="inlineblock status" >推广状态：</span><span class="inlineblock switch"></span>'));
								*/
								/*
								if (oneitem.popularized) {
									item.find('.switch').addClass("switchOn");
								} else {
									item.find('.switch').addClass("switchOff");
								}
								*/
							}

							return item;
						}

					}, boyvon.allItems);

	boyvon.showedItems = boyvon.showedItems || {};
	boyvon.showedItems = $
			.extend(
					{
						init : function() {
							$.ajax({
								url : '/popularize/getUserInfo',
								data : {},
								type : 'get',
								success : function(dataJson) {
									var userJson = dataJson;
									var level = userJson.level;
									if (level <= -1) {
										boyvon.isTryLevel = true;
									} else {
										boyvon.isTryLevel = false;
									}

									boyvon.showedItems.doShow();
								}
							});
						},
						doShow : function() {

							// boyvon.allItems.initUserInfo();

							var container = $('.showedItemsDiv');
							container.empty();
							var params = $.extend({
								"s" : "",
								"polularized" : 0,
								"catId" : null,
								"sort" : 3
							}, boyvon.util.getParams());

							params.popularizeStatus = 5;// 包含体验版

							var bottom = $('<div class="popularizeBottom pagenavi" style="text-align: center;"></div>');
							bottom
									.tmpage({
										currPage : 1,
										pageSize : 12,
										pageCount : 1,
										selfPageSizeArray : [ 12, 16, 20 ],
										ajax : {
											param : params,
											on : true,
											dataType : 'json',
											url : "/JinNangZheKou/searchItems",
											callback : function(data) {
												container.empty();
												container.find(
														".popularizeBottom")
														.remove();
												container
														.append(boyvon.allItems
																.createItemsHead());
												container
														.append(boyvon.allItems
																.createUserInfo());
												container
														.append(boyvon.allItems
																.createItemsBody(data));
												var height = boyvon.util
														.updateUlHeight(data.res.length);
												// container.find('.pic-list2').css("height",height+"px");
												bottom.appendTo(container);
												boyvon.Event.setDynamicEvent();

											}
										}

									});
						}
					}, boyvon.showedItems);

	boyvon.util = boyvon.util || {};
	boyvon.util = $.extend(
			{
				isFree : function(userVersion) {
					if (userVersion <= 1) {
						return true;
					} else {
						return false;
					}
				},
				getParams : function() {
					/*
					 * var params = {}; var status = $("#itemsStatus
					 * option:selected").attr("tag"); switch(status){ case
					 * "polularized":params.polularized=0;break; case
					 * "topopularize" : params.polularized=1;break; default :
					 * params.polularized=2;break; }
					 * 
					 * var catId = $('#itemsCat option:selected').attr("catId");
					 * params.catId = catId;
					 * 
					 * var sort = $('#itemsSortBy option:selected').attr("tag");
					 * switch(sort){ case "sortBySaleCountUp" :
					 * params.sort=3;break; case "sortBySaleCountDown" :
					 * params.sort=4;break; default : params.sort=3;break; }
					 * params.s = $('#searchWord').val(); return params;
					 */
					var params = {};
					// get search status value
					var statustag = $('.skinstatus').find(
							'input[name="status"]:checked').attr("tag");
					switch (statustag) {
					case "statusall":
						params.status = 2;
						break;
					case "statusonsale":
						params.status = 0;
						break;
					case "statusinstock":
						params.status = 1;
						break;
					default:
						params.status = 2;
						break;
					}

					// get search sort value
					var sorttag = $('.skinsort').find(
							'input[name="sort"]:checked').attr("tag");
					switch (sorttag) {
					case "sortsaledown":
						params.sort = 4;
						break;
					case "sortsaleup":
						params.sort = 3;
						break;
					default:
						params.sort = 4;
						break;
					}

					// get search seller catgory value
					var sellertag = $('.skinsellercat').find(
							'input[name="sellercatgory"]:checked').parent()
							.attr("tag");
					switch (sellertag) {
					case "catall":
						params.catId = null;
						break;
					default:
						params.catId = sellertag;
						break;
					}

					// get search taobao catgory value
					var taobaotag = $('.skintaobaocat').find(
							'input[name="taobaocatgory"]:checked').parent()
							.attr("tag");
					switch (taobaotag) {
					case "catall":
						params.cid = null;
						break;
					default:
						params.cid = taobaotag;
						break;
					}

					params.lowBegin = 0;
					params.topEnd = 100;
					params.s = $('#searchWord').val();
					return params;
				},
				updateUlHeight : function(size) {
					if (size == 0) {
						return 0;
					} else if (size < 5) {
						return 310;
					} else if (size < 9) {
						return 620;
					} else if (size < 13) {
						return 930;
					} else if (size < 17) {
						return 1240;
					} else if (size < 21) {
						return 1550;
					}
				}
			}, boyvon.util);

	boyvon.Event = boyvon.Event || {};
	boyvon.Event = $
			.extend(
					{
						setDynamicEvent : function() {
							boyvon.Event.setSelectChangeEvent();
							boyvon.Event.setGOSearchEvent();
							boyvon.Event.setSelectAddBatch();
							boyvon.Event.setAllAddBatch();
							boyvon.Event.setSelectRemoveBatch();
							boyvon.Event.setAllRemoveBatch();
						},
						setSelectChangeEvent : function() {
							$('.searchArea').find('select').unbind("change");
							$('.searchArea').find('select').change(function() {
								$('#goSearchItems').trigger("click");
							});
						},
						setGOSearchEvent : function() {
							$('#goSearchItems').unbind("click");
							$('#goSearchItems').click(function() {
								boyvon.allItems.get();
							});
						},
						setSelectAddBatch : function() {
							$('.batchSelectUsePopularize').unbind("click");
							$('.batchSelectUsePopularize')
									.click(
											function() {
												var numIids = "";
												if ($('.boyvonselected').length == 0) {
													TM.Alert.load("请选择宝贝~",
															400, 230);
												} else if ($('.boyvonselected').length > ($
														.cookie("totalNum") - $
														.cookie("popularizedNum"))) {
													TM.Alert.load(
															"您选择的宝贝数大于可推荐宝贝数~",
															400, 230);
												} else {
													$
															.each(
																	$('.boyvonselected'),
																	function(i,
																			input) {
																		var oThis = $(input);
																		numIids += (oThis
																				.attr('numIid'))
																				+ ",";
																	});
													var dataParams = {
														numIids : numIids
													};
													if (boyvon.isTryLevel == true) {// 体验版
														dataParams.status = 4;
														if (confirm("体验版推广位开启后不能再更换宝贝，确定要推广该宝贝？") == false)
															return;
													}
													$
															.post(
																	"/Popularize/addPopularized",
																	dataParams,
																	function() {
																		TM.Alert
																				.load(
																						"宝贝已添加到推广计划中~",
																						400,
																						200,
																						function() {
																							location
																									.reload();
																						});
																	});
												}
											});
						},
						setSelectRemoveBatch : function() {
							$('.batchSelectRemovePopularize').unbind("click");
							$('.batchSelectRemovePopularize')
									.click(
											function() {
												// if(boyvon.util.isFree($.cookie("level")))
												// {
												// TM.Alert.load("您现在是体验版，不能取消已推广宝贝~",400,230);
												// } else {
												var numIids = "";
												if ($('.boyvonselected').length == 0) {
													TM.Alert.load("请选择宝贝~",
															400, 230);
												} else {
													$
															.each(
																	$('.boyvonselected'),
																	function(i,
																			input) {
																		var oThis = $(input);
																		numIids += (oThis
																				.attr('numIid'))
																				+ ",";
																	});
													var dataParams = {
														numIids : numIids
													};
													if (boyvon.isTryLevel == true) {// 体验版
														TM.Alert
																.load(
																		"体验版不能修改推广位，请先升级~",
																		400,
																		200);
														return;
													}
													dataParams.status = 5;
													$
															.post(
																	"/Popularize/removePopularized",
																	dataParams,
																	function() {
																		TM.Alert
																				.load(
																						"宝贝已从推广计划中取消~",
																						400,
																						200,
																						function() {
																							location
																									.reload();
																						});
																	});
												}
												// }
											});
						},
						setAllAddBatch : function() {
							$('.batchAllAddPopularize').unbind("click");
							$('.batchAllAddPopularize')
									.click(
											function() {
												var dataParams = {};
												if (boyvon.isTryLevel == true) {// 体验版
													dataParams.status = 4;
													if (confirm("体验版推广位开启后不能再更换宝贝，确定要全店推广？") == false)
														return;
												}
												$
														.get(
																"/Popularize/addPopularizedAll",
																dataParams,
																function(data) {
																	if (data.res) {
																		TM.Alert
																				.load(
																						"宝贝已添加到推广计划中~",
																						400,
																						200,
																						function() {
																							location
																									.reload();
																						});
																	} else {
																		TM.Alert
																				.load(
																						"您的全店宝贝数大于可推荐宝贝数，请先升级哦~",
																						400,
																						230);
																	}
																});
											});
						},
						setAllRemoveBatch : function() {
							$('.batchAllRemovePopularize').unbind("click");
							$('.batchAllRemovePopularize')
									.click(
											function() {
												// if(boyvon.util.isFree($.cookie("level")))
												// {
												// TM.Alert.load("您现在是体验版，不能取消已推广宝贝~",400,230);
												// } else {
												var dataParams = {};
												if (boyvon.isTryLevel == true) {// 体验版
													TM.Alert.load(
															"体验版不能修改推广位，请先升级~",
															400, 200);
													return;
												}
												dataParams.status = 5;
												$
														.get(
																"/Popularize/removePopularizedAll",
																dataParams,
																function(data) {
																	TM.Alert
																			.load(
																					"宝贝已从推广计划中取消~",
																					400,
																					200,
																					function() {
																						location
																								.reload();
																					});
																});
												// }
											});
						},
						setSellerCatClickEvent : function() {
							$(".skinsellercat .clicksearchcatbtn").unbind(
									"click");
							$('.skinsellercat .clicksearchcatbtn').click(
									function() {
										$(this).find('input').attr("checked",
												true);
										$(this).parent().find(
												'span.selectcolor')
												.removeClass('selectcolor');
										$(this).addClass("selectcolor");
										var params = $.extend({
											"s" : "",
											"status" : 2,
											"catId" : null,
											"cid" : null,
											"ps" : 12
										}, boyvon.util.getParams());
										if ($(this).attr('tag') != "catall") {
											params.catId = $(this).attr('tag');
										}
										boyvon.allItems.get();
									});

						},
						setTaobaoCatClickEvent : function() {
							$(".skintaobaocat .clicksearchcatbtn").unbind(
									"click");
							$('.skintaobaocat .clicksearchcatbtn').click(
									function() {
										$(this).find('input').attr("checked",
												true);
										$(this).parent().find(
												'span.selectcolor')
												.removeClass('selectcolor');
										$(this).addClass("selectcolor");
										var params = $.extend({
											"s" : "",
											"status" : 2,
											"catId" : null,
											"cid" : null,
											"ps" : 5
										}, boyvon.util.getParams());
										if ($(this).attr('tag') != "catall") {
											params.cid = $(this).attr('tag');
										}
										boyvon.allItems.get();
									});
						}
					}, boyvon.Event);
})(jQuery, window));
