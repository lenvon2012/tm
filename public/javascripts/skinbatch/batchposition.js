
var TM = TM || {};
((function ($, window) {

	TM.SkinBatch = TM.SkinBatch || {};

	var SkinBatch = TM.SkinBatch;

	SkinBatch.init = SkinBatch.init || {};
	SkinBatch.init = $.extend({
		doInit: function(container) {
			var html = SkinBatch.init.createHtml();
			container.html(html);
			SkinBatch.container = container;

			SkinBatch.init.initSearchDiv();
			SkinBatch.init.initBatchOpDiv();
			SkinBatch.init.initItemContainer();

			SkinBatch.show.doShow();
		},
		createHtml: function() {
			var html = '' +
			'<div class="search-container"></div> ' +
			'<div class="batchop-div"></div>' +
			'<div class="paging-div"></div>' +
			'<div class="items-container"></div> ' +
			'<div class="paging-div"></div>' +
			'<div class="batchop-div"></div>' +
			'<div class="error-item-div" style="margin-top: 10px;">' +
			'	<span class="error-tip-span">宝贝操作失败列表：</span> ' +
			'	<table class="error-item-table list-table">' +
			'		<thead>' +
			'		<tr>' +
			'			<td style="width: 15%;">宝贝图片</td>' +
			'			<td style="width: 45%;">标题</td>' +
			'			<td style="width: 20%;">失败说明</td>' +
			'			<td style="width: 15%;">操作时间</td>' +
			'		</tr>' +
			'		</thead>' +
			'		<tbody></tbody>' +
			'	</table> ' +
			'</div>' +
			'';

			return html;
		},
		//搜索的div
		initSearchDiv: function() {
			var html = '' +
			'	<table>' +
			'		<tbody>' +
			'		<tr>' +
			'			<td style="padding-left: 20px;"><span></span><select class="category-select"><option value="0" selected="selected">所有分类</option> </select> </td>' +
			'			<td style="padding-left: 20px;">' +
			'				<span></span>' +
			'				<select class="state-select">' +
			'					<option value="OnSale" selected="selected">在售宝贝</option> ' +
			'					<option value="InStock">仓库中宝贝</option> ' +
			'					<option value="All">所有宝贝</option> ' +
			'				</select> ' +
			'			</td>' +
			'			<td style="padding-left: 10px;"><span>宝贝标题：</span><input type="text" class="search-text" /> </td>' +
			'			<td style="padding-left: 20px;">' +
			'				<span class="tmbtn blue-btn search-btn">搜索宝贝</span> ' +
			'			</td>' +
			'		</tr>' +
			'		</tbody>' +
			'	</table>' +
			'';
			SkinBatch.container.find(".search-container").html(html);

			//类目
			$.get("/home/getSellerCat",function(data){
				var categorySelectObj = SkinBatch.container.find(".category-select");
				for (var i = 0; i < data.length; i++) {
					var optionObj = $('<option></option>');
					optionObj.attr("value", data[i].cid);
					optionObj.html(data[i].name);
					categorySelectObj.append(optionObj);
				}
			});

			//添加事件
			SkinBatch.container.find(".category-select").change(function() {
				SkinBatch.show.doShow();
			});
			SkinBatch.container.find(".state-select").change(function() {
				SkinBatch.show.doShow();
			});
			SkinBatch.container.find(".search-text").keydown(function(event) {
				if (event.keyCode == 13) {//按回车
					SkinBatch.container.find(".search-btn").click();
				}
			});
			SkinBatch.container.find(".search-btn").click(function() {
				SkinBatch.show.doShow();
			});

		},
		//功能按钮
		initBatchOpDiv: function() {
			var html = '' +
			'	<table>' +
			'		<tbody>' +
			'			<tr>' +
			'				<td><span class="tmbtn yellow-btn batch-change-position-btn">批量换图</span> </td>' +
			'			</tr>' +
			'		</tbody>' +
			'	</table>' +
			'';
			SkinBatch.container.find(".batchop-div").html(html);

			var getCheckedItems = function() {
				var checkObjs = SkinBatch.container.find(".item-checkbox:checked");
				var numIidArray = [];
				checkObjs.each(function() {
					numIidArray[numIidArray.length] = $(this).attr("numIid");
				});
				return numIidArray;
			};
			//添加事件
			SkinBatch.container.find(".batch-change-position-btn").click(function() {
				var numIidArray = getCheckedItems();
				if (numIidArray.length == 0) {
					TM.Alert.load("请先选择要交换图片位置的宝贝");
					return;
				}
				var html = "<div style='margin-top: 60px; margin-left: 70px; font-size: 16px; font-family: 微软雅黑'><span style='color: red; margin-left: 80px;'>请填写1到5之间的整数</span><br><br>将宝贝的第 <input style='width: 50px; height: 21px; text-align: center;' type='text' class='img_i_posotion'> 张图片和第 <input style='width: 50px; height: 21px; text-align: center;' type='text' class='img_j_posotion'> 张交换<div>";
				TM.Alert.loadDetail(html, 500, 300, function(){
					var i = $(".img_i_posotion").val();
					var j = $(".img_j_posotion").val();
					SkinBatch.submit.dochangeposition(numIidArray, i, j);
				}, "提示");
			});
		},
		//宝贝表格
		initItemContainer: function() {
			var html = '' +
			'<table class="item-table list-table busSearch">' +
			'	<thead>' +
			'	<tr>' +
			'		<td style="width: 4%;"><input type="checkbox" class="select-all-item width17" /> </td>' +
			'		<td style="width: 10%;">宝贝主图</td>' +
			'		<td style="width: 40%;">标题</td>' +
			'		<td style="width: 6%;">销量</td>' +
			'		<td style="width: 15%;">价格</td>' +
			'		<td style="width: 15%;">状态</td>' +
			'		<td style="width: 10%;">操作</td>' +
			'	</tr>' +
			'	</thead>' +
			'	<tbody></tbody>' +
			'</table>' +
			'';

			SkinBatch.container.find(".items-container").html(html);

			//设置事件
			SkinBatch.container.find(".select-all-item").click(function() {
				var isChecked = $(this).is(":checked");
				var checkObjs = SkinBatch.container.find(".item-checkbox");
				checkObjs.attr("checked", isChecked);
			});
		}
	}, SkinBatch.init);

	/**
	 * 查询
	 */
	SkinBatch.show = SkinBatch.show || {};
	SkinBatch.show.orderData = {
		asc: "asc",
		desc: "desc"
	};
	SkinBatch.show = $.extend({
		currentPage: 1,
		ruleData: {
			orderProp: '',		//排序的属性
			orderType: SkinBatch.show.orderData.asc		//排序的类型，升序还是降序
		},
		doShow: function(currentPage) {
			var ruleData = SkinBatch.show.getQueryRule();
			var itemTbodyObj = SkinBatch.container.find(".item-table").find("tbody");
			itemTbodyObj.html("");
			if (currentPage === undefined || currentPage == null || currentPage <= 0)
				currentPage = 1;
			SkinBatch.container.find(".paging-div").tmpage({
				currPage: 1,
				pageSize: 10,
				pageCount: 1,
				ajax: {
					on: true,
					param: ruleData,
					dataType: 'json',
					url: '/skinbatch/queryItems',
					callback: function(dataJson){
						SkinBatch.show.currentPage = dataJson.pn;		//记录当前页
						itemTbodyObj.html("");
						var itemArray = dataJson.res;
						$(itemArray).each(function(index, itemJson) {
							var trObj = SkinBatch.row.createRow(index, itemJson);
							itemTbodyObj.append(trObj);
						});
					}
				}

			});
		},
		refresh: function() {
			SkinBatch.show.doShow(SkinBatch.show.currentPage);
		},
		getQueryRule: function() {
			var ruleData = {};
			var title = SkinBatch.container.find(".search-text").val();
			var state = SkinBatch.container.find(".state-select").val();
			var catId = SkinBatch.container.find(".category-select").val();

			ruleData.title = title;
			ruleData.state = state;
			ruleData.catId = catId;

			return ruleData;
		}
	}, SkinBatch.show);

	/**
	 * 显示一行宝贝
	 */
	SkinBatch.row = SkinBatch.row || {};
	SkinBatch.row = $.extend({
		createRow: function(index, itemJson) {
			var html = '' +
			'<tr>' +
			'	<td><input type="checkbox" class="item-checkbox width17" /></td>' +
			'	<td><a class="item-link" target="_blank"><img class="item-img" /></a> </td>' +
			'	<td><a class="item-link" target="_blank"><span class="item-title" style="color: #333;"></span></a> </td>' +
			'	<td><span class="sale-count"></span> </td>' +
			'	<td>' +
			'		<div class="item-price"></div> ' +
			'	</td>' +
			'	<td>' +
			'		<div class="item-state"></div> ' +
			'	</td>' +
			'	<td>' +
			'		<div class="tmbtn sky-blue-btn change-position-btn">图片换位</div>' +
			'	</td>' +
			'</tr>' +
			'' +
			'';
			var trObj = $(html);
			var numIid = itemJson.numIid;
			if (numIid === undefined || numIid == null || numIid <= 0) {
				itemJson.numIid = itemJson.id;
				numIid = itemJson.id;
			}

			var url = "http://item.taobao.com/item.htm?id=" + itemJson.numIid;
			trObj.find(".item-checkbox").attr("numIid", itemJson.numIid);
			trObj.find(".item-checkbox").each(function() {
				this.itemJson = itemJson;
			});
			trObj.find(".item-link").attr("href", url);
			trObj.find(".item-img").attr("src", itemJson.picURL + "_80x80.jpg");
			trObj.find(".item-title").html(itemJson.title);
			trObj.find(".sale-count").html(itemJson.salesCount);
			trObj.find(".inventory").html(itemJson.quantity);
			trObj.find(".item-price").html("￥" + itemJson.price);
			var state = "";
			if (itemJson.status == 0) {
				state = "待上架";
			} else if (itemJson.status == 1) {
				state = "在架上";
			}
			trObj.find(".item-state").html(state);
			trObj.find(".change-position-btn").click(function() {
				var itemCheckObj = $(this).parents("tr").find(".item-checkbox");
				var numIid = itemCheckObj.attr("numIid");
				var numIidArray = [];
				numIidArray[numIidArray.length] = numIid;
				var html = "<div style='margin-top: 60px; margin-left: 70px; font-size: 16px; font-family: 微软雅黑'><span style='color: red; margin-left: 80px;'>请填写1到5之间的整数</span><br><br>将宝贝的第 <input style='width: 50px; height: 21px; text-align: center;' type='text' class='img_i_posotion'> 张图片和第 <input style='width: 50px; height: 21px; text-align: center;' type='text' class='img_j_posotion'> 张交换<div>";
				TM.Alert.loadDetail(html, 500, 300, function(){
					var i = $(".img_i_posotion").val();
					var j = $(".img_j_posotion").val();
					SkinBatch.submit.dochangeposition(numIidArray, i, j);
				}, "提示");
			});

			var checkCallback = function(checkObj) {
				var isChecked = checkObj.is(":checked");
				if (isChecked == false) {
					SkinBatch.container.find(".select-all-item").attr("checked", false);
				} else {
					var checkObjs = SkinBatch.container.find(".item-checkbox");
					var flag = true;
					checkObjs.each(function() {
						if ($(this).is(":checked") == false)
							flag = false;
					});
					SkinBatch.container.find(".select-all-item").attr("checked", flag);
				}
			}
			trObj.find(".item-checkbox").click(function() {
				checkCallback($(this));
			});

			return trObj;

		}
	}, SkinBatch.row);

	/**
	 * 批量交换图片位置
	 */
	SkinBatch.submit = SkinBatch.submit || {};
	SkinBatch.submit = $.extend({
		dochangeposition: function(numIidArray, i, j) {
			var data = {};
			data.numIidList = numIidArray;
			data.i = i;
			data.j = j;
			$.ajax({
				url : '/skinbatch/batchChangeImgPosition',
				data : data,
				type : 'post',
				success : function(data) {
					TM.Alert.load(data, 500, 300);
				}
			});
		}
	}, SkinBatch.submit);

})(jQuery,window));