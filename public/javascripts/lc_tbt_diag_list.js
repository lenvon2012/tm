

var QueryCommodity = QueryCommodity || {};

QueryCommodity.query = QueryCommodity.query || {};

QueryCommodity.query = $.extend({
	getComms:function(currentPage){
		TM.Loading.init.show();
		var container = $("#commodityDiag");
		container.html("");
		var pageData = {};
		var ruleJson = QueryCommodity.rule.getRuleJson();
		pageData.s=ruleJson.searchText;
		pageData.numIid=ruleJson.numIid;
		pageData.pn=currentPage;
		pageData.ps=ruleJson.ps;
		pageData.lowBegin=ruleJson.lowBegin;
		pageData.topEnd=ruleJson.topEnd;
		pageData.sort=QueryCommodity.rule.sort;
		pageData.status = QueryCommodity.rule.itemStatus;
		if(pageData.status == 1) {
			$(".changeTh").hide();
		} 
		pageData.optimised = QueryCommodity.rule.isOptimised;
		// 这个是卖家类目
		var catId = $("#sellerCat option:selected").attr("catid");
		if(catId === undefined || catId == null){
			catId = "";
		}
		pageData.catId = catId;

		// 这个是淘宝类目
		var taobaoCatId = $("#taobaoCat option:selected").attr("catid");
		if(taobaoCatId === undefined || taobaoCatId == null){
			taobaoCatId = -1;
		}
		pageData.taobaoCatId = parseInt(taobaoCatId);
		
		// 先展示列表
		$('.tmpage').tmpage({
			currPage: 1,
			pageSize: 10,
			pageCount:1,
			ajax: {
				param : pageData,
				on: true,
				dataType: 'json',
				url: "/Titles/listDiagTMpageWithOptimisedBefore",
				callback:function(data){
					Loading.init.hidden();
					
					var diagContainer =$("#commodityDiag");
					diagContainer.empty();
					var commodityDiag= $("<div></div>");

					var size = data.res.length;
					for ( var i = 0; i < size; i++) {
						var itemInfo = QueryCommodity.commodityDiv.createDiv(data.res[i], pageData, data,i);
						commodityDiag.append(itemInfo);
					}

					var elems = commodityDiag.children();
					elems.find('.scoretd').each(function(i, elem){
						var oThis = $(this);
						var opBtn = oThis.parent().find('.toOptimise');
						oThis.addClass('orange-light');
						opBtn.addClass('wide-yellow-btn')
						opBtn.text('立即优化');
					});
					elems.find('.explore-diag').click(function(){
						var oThis = $(this);
						var numiid = oThis.attr('numiid');
						var diagRow = diagContainer.find('.diagtr[numiid="'+numiid+'"]');
						if(oThis.hasClass('toHide')){
							diagRow .hide(400);
							oThis.removeClass('toHide');
							oThis.text('查看诊断');
						}else{
							diagRow .show(400);
							oThis.addClass('toHide');
							oThis.text('收起诊断');
						}
					});
					
					elems.find('.changeImgPosition').click(function(){
						var numIid = $(this).attr('numiid');
						var html = "<div style='margin-top: 60px; margin-left: 70px; font-size: 16px; font-family: 微软雅黑'><span style='color: red; margin-left: 80px;'>请填写1到5之间的整数</span><br><br>将宝贝的第 <input style='width: 50px; height: 21px; text-align: center;' type='text' class='img_i_posotion'> 张图片和第 <input style='width: 50px; height: 21px; text-align: center;' type='text' class='img_j_posotion'> 张交换<div>";
						TM.Alert.loadDetail(html, 500, 300, function(){
							var i = $(".img_i_posotion").val();
							var j = $(".img_j_posotion").val();
							$.ajax({
								url : '/Titles/changeImgPosition',
								type : "post",
								data : {numIid : numIid, i : i, j : j},
								timeout: 200000,
								success : function(data) {
									TM.Alert.load(data);
									return;
								}
							});
						}, "提示");
					});

					diagContainer.append(elems);
					
					// 列表展示后 增加详情
					$.ajax({
						url : '/Titles/listDiagTMpageWithOptimised',
						data : pageData,
						type : 'json',
						global : false,
						success : function(data) {
							var diagContainer =$("#commodityDiag");
							
							var size = data.res.length;
							for ( var i = 0; i < size; i++) {
//								var itemInfo = QueryCommodity.commodityDiv.createDiv(data.res[i], pageData, data,i);
								var itemDiag = QueryCommodity.commodityDiv.createDetail(data.res[i]);
//								commodityDiag.append(itemInfo);
								var target = diagContainer.find(".singlecomm[numIid='" + data.res[i].numIid + "']")
								// 更新得分字段
								target.find(".titlescore").html(data.res[i].score)
								target.after(itemDiag);
							}
						}
					});
				}
		
			}

		});

		setTimeout(function(){
			$('#closeImg').trigger('click');
		},6000);

	},
    initPagination: function(totalCount, per_page, currentPage) {
        currentPage--;
        $(".Pagination").pagination(totalCount, {
            num_display_entries : 3,
            num_edge_entries : 2,
            current_page: currentPage,
            callback : QueryCommodity.query.findCommodityList,
            items_per_page : per_page,
            prev_text : "&lt上一页",
            next_text : "下一页&gt"
        });
    },
    initPaginationWithCat: function(totalCount, per_page, currentPage) {
        currentPage--;
        $(".Pagination").pagination(totalCount, {
            num_display_entries : 3,
            num_edge_entries : 2,
            current_page: currentPage,
            callback : QueryCommodity.query.findCommodityListWithCat,
            items_per_page : per_page,
            prev_text : "&lt上一页",
            next_text : "下一页&gt"
        });
    },
    findCommodityList: function(currentPage, jq) {
        var container = $("#commodityDiag");
        container.html("");
        var ruleJson = QueryCommodity.rule.getRuleJson();
        ruleJson.pn = currentPage + 1;
        var data = {};
        data.s=ruleJson.searchText;
        data.pn=ruleJson.pn;
        data.ps=ruleJson.ps;
        data.lowBegin=ruleJson.lowBegin;
        data.topEnd=ruleJson.topEnd;
        data.sort=QueryCommodity.rule.sort;
        data.status = QueryCommodity.rule.itemStatus;
        var pageData = data;
        $.ajax({
            //async : false,
            url : '/Titles/listDiag',
            data : data,
            type : 'get',
            success : function(data) {
				
				Loading.init.hidden(); 
                var res;
                var  diagContainer =$("#commodityDiag");
                var commodityDiag= $("<div></div>");


                var size = data.res.length;
				//var tableHead=$('<table width="883px" style="border:1px solid #B0B0B0;background-color: #259;"></table>');
				 //var thead=$('<thead></thead>');
				// var th=$('<th style="color: white;">宝贝列表</th>');
				// thead.append(th);
                var thead=$('<div class="itemsListHead" ></div>');
                thead.append("宝贝列表");

				 //tableHead.append(thead);
				 //commodityDiag.append(tableHead);
                commodityDiag.append(thead);
                for ( var i = 0; i < size; i++) {
                  //  if(data.res[i].score>=lowBegin&&data.res[i].score<=topEnd){
                        res=QueryCommodity.commodityDiv.createDiv(data.res[i], pageData, data,i);
                       if(i<2){
                        	res.find(".diagDetail").css("display","block");
                       }

                        commodityDiag.append(res);
                        var youhuaLink=res.find(".jumpTo");
                        var jumpHref=youhuaLink.attr("href");
                        var jumpToRename=res.find(".jumpToRename");
                        jumpToRename.attr("href",jumpHref);

                        var titleArea=res.find(".title");
                        titleArea.click(function(){
                            if($(this).parent().find(".diagDetail").css("display")=="none")
                                $(this).parent().find(".diagDetail").css("display","block");
                            else if($(this).parent().find(".diagDetail").css("display")=="block")
                                $(this).parent().find(".diagDetail").css("display","none");
                    });
				}
                diagContainer.append(commodityDiag.children());

                var youhua=$(".youhua");
				youhua.mouseover(function(){$(this).attr("src","http://img04.taobaocdn.com/imgextra/i4/1039626382/T2qo51XaVXXXXXXXXX_!!1039626382.png")});
				youhua.mouseout(function(){$(this).attr("src","http://img03.taobaocdn.com/imgextra/i3/1039626382/T2OF12XfdXXXXXXXXX_!!1039626382.png")});


            }
        });
    },
    findCommodityListWithCat: function(currentPage, jq) {
        var catId=$('#sellerCat option:selected').attr("catId");
        var container = $("#commodityDiag");
        container.html("");
        var ruleJson = QueryCommodity.rule.getRuleJson();
        ruleJson.pn = currentPage + 1;
        var data = {};
        data.pn=ruleJson.pn;
        data.ps=ruleJson.ps;
        data.catId=catId;
        var pageData = data;
        $.ajax({
            //async : false,
            url : '/Titles/getItemsBySellerCatId',
            type : "post",
            data : data,
            success : function(data) {
                Loading.init.hidden();
                var res;
                var commodityDiag=$("#commodityDiag");
                var size = data.res.length;
                //var tableHead=$('<table width="883px" style="border:1px solid #B0B0B0;background-color: #259;"></table>');
                //var thead=$('<thead></thead>');
                // var th=$('<th style="color: white;">宝贝列表</th>');
                // thead.append(th);
                var thead=$('<div class="itemsListHead" ></div>');
                thead.append("宝贝列表");

                //tableHead.append(thead);
                //commodityDiag.append(tableHead);
                commodityDiag.append(thead);
                for ( var i = 0; i < size; i++) {
                    //  if(data.res[i].score>=lowBegin&&data.res[i].score<=topEnd){
                    res=QueryCommodity.commodityDiv.createDiv(data.res[i], pageData, data,i);
                    if(i<2)
                        res.find(".diagDetail").css("display","block");
                    commodityDiag.append(res);
                    var youhuaLink=res.find(".jumpTo");
                    var jumpHref=youhuaLink.attr("href");
                    var jumpToRename=res.find(".jumpToRename");
                    jumpToRename.attr("href",jumpHref);

                    var titleArea=res.find(".title");
                    titleArea.click(function(){
                        if($(this).parent().find(".diagDetail").css("display")=="none")
                            $(this).parent().find(".diagDetail").css("display","block");
                        else if($(this).parent().find(".diagDetail").css("display")=="block")
                            $(this).parent().find(".diagDetail").css("display","none");
                    });
                }
                var youhua=$(".youhua");
                youhua.mouseover(function(){$(this).attr("src","http://img04.taobaocdn.com/imgextra/i4/1039626382/T2qo51XaVXXXXXXXXX_!!1039626382.png")});
                youhua.mouseout(function(){$(this).attr("src","http://img03.taobaocdn.com/imgextra/i3/1039626382/T2OF12XfdXXXXXXXXX_!!1039626382.png")});

            }
        });
    }
}, QueryCommodity.query);


QueryCommodity.commodityDiv = QueryCommodity.commodityDiv || {};
QueryCommodity.commodityDiv = $.extend({

	 createDiv: function(DiagResult, pageData,data, offset) {
		 DiagResult.createdStr = QueryCommodity.rule.parseLongToDate(DiagResult.created);

         var tmpl = $('#diagitem').tmpl(
             [DiagResult]
         );

         var btnObj = tmpl.find('.toOptimise');
         //在这里
         data.pn = data.pn ? data.pn : 1;
         offset = (data.pn - 1) * data.ps + offset;
         var start=$("#lowBegin").val();
         var end=$("#topEnd").val();
         var sort= QueryCommodity.rule.sort;
         var status = QueryCommodity.rule.itemStatus;
         if(status == 1) {
        	 tmpl.find(".delist-time-td").hide();
         } else {
        	 tmpl.find(".delist-time-td").show();
         }
         var isOptimised = $('#isOptimised').val();

         var version = parseInt(TM.ver);
         if(version<10){
             //      TM.Alert.showOrder();
//                 href="javascript:void(0)";
             href="javascript:void(0)";
             btnObj.attr("target","_self");
             btnObj.click(function(){
                 TM.Alert.showOrder();
             });
         }else{


             var href = null;
             if($.cookie("isCatSearch")==1) {
                 href = "/titles/titleop?numIid=" + DiagResult.numIid + "&pn=" + data.pn + "&offset=" + offset + "&start="+start+
                     "&end="+end+"&sort="+sort+"&status="+status+"&catId=" + $.cookie("catId") + "&optimised=" + isOptimised
                    +((TM.encodeUid && TM.encodeUid.length > 2)?'':("&_tms="+TM.tms+''));
                     //+"&_tms="+TM.tms;
             } else {
                 href = "/titles/titleop?numIid=" + DiagResult.numIid + "&pn=" + data.pn + "&offset=" + offset + "&start="+start+
                     "&end="+end+"&sort="+sort+"&status="+status+"&catId=" + $.cookie("catId") + "&optimised=" + isOptimised
                     +((TM.encodeUid && TM.encodeUid.length > 2)?'':("&_tms="+TM.tms));
                     //"&_tms="+TM.tms;

                 var s = data.s;
                 if (s === undefined || s == null || s == "")
                     ;
                 else{
                     href = href  + "&s=" + pageData.s;
                 }
             }

             btnObj.attr('target','_blank');
             btnObj.attr('href',href);
         }

         return tmpl;

//         var  res= $('<tr class="singleComm"></tr>');
//         var titleDiv=QueryCommodity.commodityDiv.createTitleDiv(DiagResult,pageData, data, offset);
//         res.append(titleDiv.children());
//         res.append(detail);
//
//         var clear=$('<div class="clear"></div>');
//         res.append(clear);
//        return res;
	},
	/*createTopDiv: function(DiagResult) {
		var top=$('<div style="width:400px;margin-top:12px;float:left;"></div>');
		var span=$('<span class="searchTitle">请输入宝贝核心关键词:</span>');
		top.append(span);
		var input=$('<input type="text" id="searchText">');
		top.append(input);
		var a=$('<a href="#"><img src="/public/images/searchBtn1.png" id="searchBtn"/></a>');
		top.append(a);
		return top;	
		
	
     },
     */
	createTitleDiv: function(DiagResult, pageData,data, offset) {
		var titleDiv=$('<td class="title"></td>');
		var imgObj=QueryCommodity.commodityDiv.createImgDiv(DiagResult);
		titleDiv.append(imgObj);
		var titleContent=$('<div class="titleContent">&nbsp;</div>');

        var upper=$('<div></div>')
//		var imgObj2=$('<img src="http://img01.taobaocdn.com/imgextra/i1/1039626382/T2wsC1XmFaXXXXXXXX_!!1039626382.png" style="float:left;"/>');
//		var imgObj2=$('<img src="http://img01.taobaocdn.com/imgextra/i1/1039626382/T26IB7XXhdXXXXXXXX_!!1039626382.png" style="float:left;"/>');
//		var imgObj2=$('<img src="" style="float:left;"/>');
//        upper.append(imgObj2);
		var baseInf=$('<td id="baseInf"></td>')
		var priceSpan=$('<span class="priceSpan">价格：￥</span>');
		var price=$('<span class="red"></span>');
		price.append(DiagResult.price);
		priceSpan.append(price);
		baseInf.append(priceSpan);

        var scoreSpan=$('<span class="priceSpan">标题得分：</span>');
        var score=  $('<span class="red"></span>');
        score.append(DiagResult.score);
        scoreSpan.append(score);
        scoreSpan.append($('<span class="question"></span>'));
        baseInf.append(scoreSpan);

        baseInf.append($('<br />'));
        var chakan=$('<a><span class="lookup green" style="font-weight: normal"></span></a>');
        var url = "http://item.taobao.com/item.htm?id=" + DiagResult.numIid;
        chakan.attr("href",url);
        baseInf.append(chakan);
		upper.append(baseInf);


        var lower=$('<td style="height:24px;font-size:16px;"></td>');
        var spanObj=QueryCommodity.commodityDiv.createSpanObj(DiagResult);
        lower.append(spanObj);

		titleContent.append(lower);
        titleContent.append(upper);
		titleDiv.append(titleContent);
		var btnObj=QueryCommodity.commodityDiv.createBtn(DiagResult,pageData, data, offset);
		titleDiv.append(btnObj);
//		var clear=$('<div class="clear"></div>');
//		titleDiv.append(clear);
		
		return titleDiv.children();
     },
	createImgDiv: function(DiagResult) {
	/*	var commImg=$('<div class="commodityImg"></div>');
        var img=$('<img  width="80px" />');
        img.attr("src",DiagResult.picPath);
        var a=$("<a></a>");
        a.attr("href","#");
        a.append(img);
        commImg.append(a);
        return commImg;
        */
        return $('<td><a target="_blank" href="http://item.taobao.com/item.htm?id="'
            + DiagResult.numIid+'"><img class="commImg"  style="width:80px" src="'+DiagResult.picPath+'" /></a></td>');
     },
     createSpanObj: function(DiagResult) {
	
        var spanObj=$('<span class="commodityTitle">&nbsp;</span>');
		var searchText=QueryCommodity.rule.getRuleJson().searchText;
		
		if(typeof(searchText)=="undefined") 
			spanObj.append(DiagResult.title);
		else 
        {
			var title=DiagResult.title;
			var titleArr=title.split(searchText);
			
			
			var length=titleArr.length;
			for (var i=0;i<length;i++)
			{
				spanObj.append(titleArr[i]);
				if(i<(length-1)){
					var keySpan=$('<span class="red"></span>');
					keySpan.append(searchText);
					spanObj.append(keySpan);
				}
				
				}
		}
		
		//spanObj.append(DiagResult.title);
        return spanObj;
     },
     createBtn:function(DiagResult, pageData,data, offset){
	
		var btnObj=$('<td class="youhuaBtn"></td>');
        var aa=$('<a class="jumpTo" target="_blank"><img class="youhua"  src="http://img03.taobaocdn.com/imgextra/i3/1039626382/T2OF12XfdXXXXXXXXX_!!1039626382.png"/></a>');
        //aa.attr("href","#");
         data.pn = data.pn ? data.pn : 1;
         offset = (data.pn - 1) * data.ps + offset;
         var start=$("#lowBegin").val();
         var end=$("#topEnd").val();
         var sort= QueryCommodity.rule.sort;
         var status = QueryCommodity.rule.itemStatus;
         if($.cookie("iskitty")){
             if($.cookie("isCatSearch")==1) {
                 var href = "/KittyTitle/KittyDo?numIid=" + DiagResult.numIid + "&pn=" + data.pn + "&offset=" + offset + "&start="+start+"&end="+end+"&sort="+sort+"&status="+status+"&catId=" + $.cookie("catId") +"&_tms="+TM.tms;
             } else {
                 var href = "/KittyTitle/KittyDo?numIid=" + DiagResult.numIid + "&pn=" + data.pn + "&offset=" + offset + "&start="+start+"&end="+end+"&sort="+sort+"&status="+status+"&catId=" + $.cookie("catId") +"&_tms="+TM.tms;
                 var s = data.s;
                 if (s === undefined || s == null || s == "")
                     ;
                 else{
                     href = href  + "&s=" + pageData.s;
                 }
             }
             //在这里
             var version = parseInt(TM.ver);
             if(version<10){
                 //      TM.Alert.showOrder();
//                 href="javascript:void(0)";
                 href="javascript:void(0)";
                 aa.attr("id","version");
                 aa.attr("target","_self");
                 btnObj.click(function(){
                     TM.Alert.showOrder();
                 });

             }
         } else {
             if($.cookie("isCatSearch")==1) {
                 var href = "/titles/titleop?numIid=" + DiagResult.numIid + "&pn=" + data.pn + "&offset=" + offset + "&start="+start+"&end="+end+"&sort="+sort+"&status="+status+"&catId=" + $.cookie("catId") +"&_tms="+TM.tms;
             } else {
                 var href = "/titles/titleop?numIid=" + DiagResult.numIid + "&pn=" + data.pn + "&offset=" + offset + "&start="+start+"&end="+end+"&sort="+sort+"&status="+status+"&catId=" + $.cookie("catId") +"&_tms="+TM.tms;
                 var s = data.s;
                 if (s === undefined || s == null || s == "")
                     ;
                 else{
                     href = href  + "&s=" + pageData.s;
                 }
             }
//             TM.encodeUid = '';
             href += ((TM.encodeUid && TM.encodeUid.length > 2)?'':("&_tms="+TM.tms+''));
             //在这里
             var version = parseInt(TM.ver);
             if(version<10){
                 //      TM.Alert.showOrder();
//                 href="javascript:void(0)";
                 href="javascript:void(0)";
                 aa.attr("id","version");
                 aa.attr("target","_self");
                 btnObj.click(function(){
                     TM.Alert.showOrder();
                 });
             }
         }
         aa.attr("href", href);
         //alert(href);
        btnObj.append(aa);
        return btnObj;
	},
	createDetail: function(DiagResult) {
//		 var diagDetail=$('<td colspan="10" class="diagDetail"></td>');
        var diagDetail=$('<td colspan="10" class=""></td>');
		 
		 var tableObj=$('<table class="diagTable"></table>');
		 var tr=QueryCommodity.commodityDiv.createRow1(DiagResult);
         tableObj.append(tr);
         tr=QueryCommodity.commodityDiv.createRow10(DiagResult);
         tableObj.append(tr);
         tr=QueryCommodity.commodityDiv.createRow2(DiagResult);
         tableObj.append(tr);
         tr=QueryCommodity.commodityDiv.createRow3(DiagResult);
         tableObj.append(tr);
         tr=QueryCommodity.commodityDiv.createRow4(DiagResult);
         tableObj.append(tr);
         tr=QueryCommodity.commodityDiv.createRow5(DiagResult);
         tableObj.append(tr);
         tr=QueryCommodity.commodityDiv.createRow6(DiagResult);
         // 拿掉促销词
         /*tableObj.append(tr);
         tr=QueryCommodity.commodityDiv.createRow7(DiagResult);*/
         tableObj.append(tr);


        tr=QueryCommodity.commodityDiv.createRow11(DiagResult);
        tableObj.append(tr);
        tr=QueryCommodity.commodityDiv.createRow12(DiagResult);
        tableObj.append(tr);
        // tr=QueryCommodity.commodityDiv.createRow8(DiagResult);
        // tableObj.append(tr);
		 tr=QueryCommodity.commodityDiv.createRow9(DiagResult);
         tableObj.append(tr);

         tableObj.find('tr:odd').addClass('diag-odd');
         tableObj.find('tr:last').addClass("diag-last");
         diagDetail.append(tableObj);
//         diagtr = $('<tr class="diagtr hidden" numiid="'+DiagResult.numIid+'"></tr>');
         var diagtr = $('<tr class="diagtr hidden"  numiid="'+DiagResult.numIid+'"></tr>');
         diagtr.append(diagDetail);

         return diagtr;
	},
	createRow1:function(DiagResult){
	
		 var tr=$('<tr class="intro1"></tr>');
         var td1=$('<td class="td1"></td>');
         var td2=$('<td class="td2"></td>');
         var td3=$('<td class="td3"></td>');
        
         td2.append("标题字数");
         
		 var span=$('<span class="titleNum"></span>');
		 
		
		 if(DiagResult.wordLength>60)
		 {
			 var imgObj=$('<img width="20px" />');
        	 imgObj.attr("src","http://img01.taobaocdn.com/imgextra/i1/1039626382/T2ZOxZXc8dXXXXXXXX_!!1039626382.png");
     	     td1.append(imgObj);
			 span.addClass("red");
			 span.append("亲您的标题字数已超过30限制(╯^╰)");
			 
			 td3.append(span);        	 
			 }
		 else if(DiagResult.wordLength>56)
		 {
			 var imgObj=$('<img width="20px" />');
        	 imgObj.attr("src","http://img02.taobaocdn.com/imgextra/i2/1039626382/T2FXG2XfFXXXXXXXXX_!!1039626382.png");
     	     td1.append(imgObj);
			 span.addClass("green");
			 span.append("亲您已经充分利用了字数限制，继续加油哦亲(^0^)！");
			
			 td3.append(span);        	 
			 }
		else{
			 var imgObj=$('<img width="20px" />');
        	 imgObj.attr("src","http://img01.taobaocdn.com/imgextra/i1/1039626382/T2ZOxZXc8dXXXXXXXX_!!1039626382.png");
     	     td1.append(imgObj);
			 span.append("亲您还可以");
			 var Aobj=$('<a href="#" style="color:black;" class="jumpToRename bold">添加</a>');
			
			 span.append(Aobj);
			 var remainChs=$('<span class="bold"></span>');
			 remainChs.html(60-DiagResult.wordLength);
			 span.append(remainChs);
			 span.append("个英文字符或者");
			 var remainEng=$('<span class="bold"></span>');
			 remainEng.html(Math.floor((60-DiagResult.wordLength)/2));
			 span.append(remainEng);
			 span.append("个中文字符(^0^)");
			 
			 td3.append(span);        	 
			}
        
		 tr.append(td1);
         tr.append(td2);
         tr.append(td3);
         
         return tr;
	},
	createRow2:function(DiagResult){
	
		var tr=$('<tr class="intro2"></tr>');
         var td1=$('<td class="td1"></td>');
         var td2=$('<td class="td2"></td>');
         var td3=$('<td class="td3"></td>');
         var imgObj=$('<img width="20px" />');
         
         var span=$('<span>包含宝贝属性</span>');
         td2.append(span);      
        
		 var span=$('<span class="titleNum"></span>');
		 if(DiagResult.props.length==0)
		 {
			 imgObj.attr("src","http://img01.taobaocdn.com/imgextra/i1/1039626382/T2ZOxZXc8dXXXXXXXX_!!1039626382.png");
      	     td1.append(imgObj);
			 
			 span.append("亲您还没有");
			 
			 var Aobj=$('<a href="#" style="color:black;" class="jumpToRename bold">添加</a>');
			 
			 span.append(Aobj);
			 span.append("任何属性词(╯^╰)");
			 td3.append(span);  
			 }
		 else if(DiagResult.props.length>1)
		 {
			 imgObj.attr("src","http://img02.taobaocdn.com/imgextra/i2/1039626382/T2FXG2XfFXXXXXXXXX_!!1039626382.png");
      	     td1.append(imgObj);
			 var propList=$('<span class="bold"></span>');
			 for(var i=0;i<DiagResult.props.length;i++)
			 	propList.append(DiagResult.props[i]+" ");
			 
			 td3.append(propList);
		}
		else{
			 imgObj.attr("src","http://img01.taobaocdn.com/imgextra/i1/1039626382/T2ZOxZXc8dXXXXXXXX_!!1039626382.png");
      	     td1.append(imgObj);
             td3.append("亲，您只包含");
			 var propList=$('<span class="bold red"></span>');
			 for(var i=0;i<DiagResult.props.length;i++)
			 	propList.append(DiagResult.props[i]+" ");
			 td3.append(propList);
             td3.append("这些属性词，")
			 var suggest=$('<span class="bold"></span>');
			 var remainPropLength=$('<span class="bold"></span>');
			 suggest.append("建议再");
			 var Aobj=$('<a href="#" style="color:black;"  class="jumpToRename bold">添加</a>');
			 suggest.append(Aobj);
			 remainPropLength.append(2-parseInt(DiagResult.props.length));
			 suggest.append(remainPropLength);

			 
			 
			 td3.append(suggest);
             td3.append("个属性词(^0^)！");
			}
		// td3.append(span);
		 tr.append(td1);
         tr.append(td2);
         tr.append(td3);
         return tr;
	},
	createRow3:function(DiagResult){
		var tr=$('<tr class="intro1"></tr>');
         var td1=$('<td class="td1"></td>');
         var td2=$('<td class="td2"></td>');
         var td3=$('<td class="td3"></td>');
         var imgObj=$('<img width="20px" />');
         
         td2.append("低频搜索词");
         
		 if(DiagResult.lowPvWordCount==0)
		 {
			 imgObj.attr("src","http://img02.taobaocdn.com/imgextra/i2/1039626382/T2FXG2XfFXXXXXXXXX_!!1039626382.png");
         	 td1.append(imgObj);
			 var span=$('<span ></span>');
			 span.html("亲，您的标题里一个低频搜索词都没有，继续加油哦亲O(∩_∩)O");
			 
			 td3.append(span);
			 }
		 else {
			 imgObj.attr("src","http://img01.taobaocdn.com/imgextra/i1/1039626382/T2ZOxZXc8dXXXXXXXX_!!1039626382.png");
         	 td1.append(imgObj);

			 
			 var lowPvWordsList=$('<span class="bold"></span>');
			 td3.append("亲，您的标题里目前还是有这些低频搜索词哟  ╮(╯_╰)╭");
			 lowPvWordsList.append($('<br />'));
			 lowPvWordsList.append(DiagResult.lowPvWords);
			 td3.append(lowPvWordsList);
			 }
         tr.append(td1);
         tr.append(td2);
         tr.append(td3);
         return tr;
	},
	createRow4:function(DiagResult){
		var tr=$('<tr class="intro2"></tr>');
         var td1=$('<td class="td1"></td>');
         var td2=$('<td class="td2"></td>');
         var td3=$('<td class="td3"></td>');
         var imgObj=$('<img width="20px" />');
         td1.append(imgObj);
         td2.append("重复关键词");

      //   if(JSON.stringify(DiagResult.dumpCount)=="{}")
		// td3.append("亲您的标题中没有发现重复关键字，请再接再厉(^0^)");
		//  else
		//  {

			 var dumpWords=$('<span ></span>');

			 var dump=$('<span class="bold"></span>');
             var countNum = 0;
			 for(var x in DiagResult.dumpCount){
				dump.append(x+"("+DiagResult.dumpCount[x]+") ");
                 countNum++;
				}
            if(countNum == 0)
            {
            	imgObj.attr("src","http://img02.taobaocdn.com/imgextra/i2/1039626382/T2FXG2XfFXXXXXXXXX_!!1039626382.png");
                dumpWords.append("亲您的标题中没有发现重复关键字，请再接再厉(^0^)");
               // dumpWords.append(dump);
            } else if(countNum == 1) {
                imgObj.attr("src","http://img02.taobaocdn.com/imgextra/i2/1039626382/T2FXG2XfFXXXXXXXXX_!!1039626382.png");
                dumpWords.append("亲您的标题中仅包含一个重复关键词，请再接再厉(^0^)");
                dumpWords.append($('<br />'))
                dumpWords.append(dump);
            }
			else {
				imgObj.attr("src","http://img01.taobaocdn.com/imgextra/i1/1039626382/T2ZOxZXc8dXXXXXXXX_!!1039626382.png");
				dumpWords.append("亲，您的标题中含有如下重复关键词，如非必要，建议您去掉重复的关键词，重复的关键词会降低您的搜索权重哦！");
                dumpWords.append($('<br />'))
                dumpWords.append(dump);
            }
            td3.append(dumpWords);


         tr.append(td1);
         tr.append(td2);
         tr.append(td3);
         return tr;
	},
	createRow5:function(DiagResult){
		var tr=$('<tr class="intro1"></tr>');
         var td1=$('<td class="td1"></td>');
         var td2=$('<td class="td2"></td>');
         var td3=$('<td class="td3"></td>');
         var imgObj=$('<img width="20px" />');
         imgObj.attr("src","http://img01.taobaocdn.com/imgextra/i1/1039626382/T2ZOxZXc8dXXXXXXXX_!!1039626382.png");
         td1.append(imgObj);
         td2.append("热搜词");
         
		 if(DiagResult.hotPvWordCount == 0)
		 {
			imgObj.attr("src","http://img01.taobaocdn.com/imgextra/i1/1039626382/T2ZOxZXc8dXXXXXXXX_!!1039626382.png");
            td1.append(imgObj);
			var hotSpan=$('<span class="red"></span>');
		 	hotSpan.html("亲，您的标题里怎么一个热搜词都没有呢T_T");
			var Aobj=$('<a href="#" style="color:black;"  class="jumpToRename bold">热搜词</a>');
			 hotSpan.append(Aobj);
			hotSpan.append("(╯^╰)");

			td3.append(hotSpan);
			}
         else if(DiagResult.hotPvWordCount < 8) {
             imgObj.attr("src","http://img02.taobaocdn.com/imgextra/i2/1039626382/T2FXG2XfFXXXXXXXXX_!!1039626382.png");
             td1.append(imgObj);


             var hotPvWords=$('<span class="bold"></span>');
             td3.append("亲，您的标题里只包含这些热搜词哦，还可以添加<span class='bold'>"+(8-DiagResult.hotPvWordCount)+"</span>个热搜词：");
             hotPvWords.append($('<br />'));
             hotPvWords.append("<span class='red'>"+DiagResult.hotPvWords+"</span>");

             td3.append(hotPvWords);
         }
		 else
		 {
			 imgObj.attr("src","http://img02.taobaocdn.com/imgextra/i2/1039626382/T2FXG2XfFXXXXXXXXX_!!1039626382.png");
             td1.append(imgObj);


			 var hotPvWords=$('<span class="bold"></span>');
			 td3.append("亲，您的标题里已经包含了这些热搜词哦，继续努力：");
			 hotPvWords.append($('<br />'));
			 hotPvWords.append("<span class='red'>"+DiagResult.hotPvWords+"</span>");
			 
			 td3.append(hotPvWords);
			 }
         tr.append(td1);
		 tr.append(td2);
         tr.append(td3);
         return tr;
	},
	createRow6:function(DiagResult){
		 var tr=$('<tr class="intro2"></tr>');
         var td1=$('<td class="td1"></td>');
         var td2=$('<td class="td2"></td>');
         var td3=$('<td class="td3"></td>'); 
		 var imgObj=$('<img width="20px" />');
         
         td2.append("标点与空格");
        if(DiagResult.engPunctuationNum==0&&DiagResult.chsPunctuationNum==0)
        {

            imgObj.attr("src","http://img02.taobaocdn.com/imgextra/i2/1039626382/T2FXG2XfFXXXXXXXXX_!!1039626382.png");
            td1.append(imgObj);
            var PunctuationNum=$('<span ></span>');
            PunctuationNum.append("亲您的标题中没有发现任何中英文标点，继续加油哦(^0^)");
            td3.append(PunctuationNum);
        }
        /*else if(DiagResult.engPunctuationNum<3&&DiagResult.chsPunctuationNum==0)
		 {
			
             imgObj.attr("src","http://img01.taobaocdn.com/imgextra/i1/1039626382/T2ZOxZXc8dXXXXXXXX_!!1039626382.png");
			 td1.append(imgObj);
			 var PunctuationNum=$('<span ></span>');
			 PunctuationNum.append("亲您的标题中只出现");
             PunctuationNum.append(DiagResult.engPunctuationNum);
             PunctuationNum.append("个英文标点，继续加油哦(^0^)");
		     td3.append(PunctuationNum);
			}
         else if(DiagResult.engPunctuationNum==0&&DiagResult.chsPunctuationNum<2)
         {
                imgObj.attr("src","http://img01.taobaocdn.com/imgextra/i1/1039626382/T2ZOxZXc8dXXXXXXXX_!!1039626382.png");
                td1.append(imgObj);
                var PunctuationNum=$('<span ></span>');
                PunctuationNum.append("亲您的标题中只出现");
                PunctuationNum.append(DiagResult.chsPunctuationNum);
                PunctuationNum.append("个中文标点，继续加油哦(^0^)");
                td3.append(PunctuationNum);
            }*/
        else
		 {
             imgObj.attr("src","http://img01.taobaocdn.com/imgextra/i1/1039626382/T2ZOxZXc8dXXXXXXXX_!!1039626382.png");
			 td1.append(imgObj);
			 var span=$('<span ></span>');
			 span.append("亲您的标题中发现了");
			 var engPunctuationNum=$('<span class="bold"></span>');
			 engPunctuationNum.html(DiagResult.engPunctuationNum);
			 span.append(engPunctuationNum);
			 span.append("个英文标点和");
			 var chsPunctuationNum=$('<span class="bold"></span>');
			 chsPunctuationNum.html(DiagResult.chsPunctuationNum);
			 span.append(chsPunctuationNum);
			 span.append("个中文标点(╯^╰),");
			
			 var Aobj=$('<a href="#" style="color:black;"  class="jumpToRename bold">建议去除</a>');
			 span.append(Aobj);
			
			 
			 span.append("(^0^)");
		     td3.append(span);
			}
		 tr.append(td1);
		 tr.append(td2);
         tr.append(td3);
         return tr;
	},
	createRow7:function(DiagResult){
		var tr=$('<tr class="intro1"></tr>');
         var td1=$('<td class="td1"></td>');
         var td2=$('<td class="td2"></td>');
         var td3=$('<td class="td3"></td>');
		 var imgObj=$('<img width="20px" />');
              
         td2.append("促销词");
         
		 if(DiagResult.promoteWords.length==0)
		 {
			 
             imgObj.attr("src","http://img01.taobaocdn.com/imgextra/i1/1039626382/T2ZOxZXc8dXXXXXXXX_!!1039626382.png");
			 td1.append(imgObj); 
			 var promoteWords=$('<span ></span>');
			 promoteWords.html("亲，您的标题中没有发现任何");
			 var Aobj=$('<a href="#" style="color:black;"  class="jumpToRename bold">促销词</a>');
			 promoteWords.append(Aobj);
			 promoteWords.append("哦╮(╯_╰)╭");
			 
		     td3.append(promoteWords);
     		 }
		 else
		 {
			 
             imgObj.attr("src","http://img02.taobaocdn.com/imgextra/i2/1039626382/T2FXG2XfFXXXXXXXXX_!!1039626382.png");
			 td1.append(imgObj);
             var promoteWords=$('<span class="bold"></span>');
             td3.append("亲，您的标题里已经包含了这些促销词哦: ");
             promoteWords.append($('<br />'));
			 promoteWords.append(DiagResult.promoteWords);
             td3.append(promoteWords);
			 }
		 tr.append(td1);
         tr.append(td2);
         tr.append(td3);
         return tr;
	},
	createRow8:function(DiagResult){
		var tr=$('<tr class="intro2"></tr>');
         var td1=$('<td class="td1"></td>');
         var td2=$('<td class="td2"></td>');
         var td3=$('<td class="td3"></td>');
         var imgObj=$('<img width="20px" />');
         
         td2.append("总搜索热度");
         
		 var span=$('<span ></span>');
		 
		 if(DiagResult.pv>10000000)
		 {
			 imgObj.attr("src","http://img02.taobaocdn.com/imgextra/i2/1039626382/T2FXG2XfFXXXXXXXXX_!!1039626382.png");
             td1.append(imgObj);
			// span.addClass("green");
			 span.append("亲您的标题的搜索热度为:");
			 var pvSpan=$('<span class="score red"></span>');
			 pvSpan.append(DiagResult.pv);
			 
			 td3.append(span);
			 td3.append(pvSpan);
			 td3.append(",表现良好(^0^)");
			 }
		 else 
		 {
			 imgObj.attr("src","http://img01.taobaocdn.com/imgextra/i1/1039626382/T2ZOxZXc8dXXXXXXXX_!!1039626382.png");
             td1.append(imgObj);
			 //span.addClass("red");
			 span.append("亲您的标题的搜索热度只有");
			 var pvSpan=$('<span class="score red"></span>');
			 pvSpan.append(DiagResult.pv);
			
			 td3.append(span);     
			 td3.append(pvSpan);
			 td3.append(",要加油哦(╯^╰)");
			 }
		 
		 tr.append(td1);
		 tr.append(td2);
       	 tr.append(td3);
         return tr;
	},
	createRow9:function(DiagResult){
		var tr=$('<tr class="intro1"></tr>');
         var td1=$('<td class="td1"></td>');
         var td2=$('<td class="td2"></td>');
         var td3=$('<td class="td3"></td>');
         var imgObj=$('<img width="20px" />');
         
         td2.append("标题质量得分");
         
		 var span=$('<span class="score red"></span>');
		 span.append(DiagResult.score);
		 td3.append(span); 
		 if(DiagResult.score>80)
		 {
			 imgObj.attr("src","http://img02.taobaocdn.com/imgextra/i2/1039626382/T2FXG2XfFXXXXXXXXX_!!1039626382.png");
             td1.append(imgObj);
			 var suggestSpan=$('<span ></span>');
			 suggestSpan.append("亲您做的非常好哦(^0^)");
			 td3.append(suggestSpan);
			 }
		 else 
		 {
			 imgObj.attr("src","http://img01.taobaocdn.com/imgextra/i1/1039626382/T2ZOxZXc8dXXXXXXXX_!!1039626382.png");
             td1.append(imgObj);
			 var suggestSpan=$('<span ></span>');
			 suggestSpan.append("亲您的标题还有提升空间");

			 
			 td3.append(suggestSpan);
			 }
		 
		 tr.append(td1);
		 tr.append(td2);
       	 tr.append(td3);
         return tr;
	},
    createRow10 : function(DiagResult){
        var tr=$('<tr class="intro2"></tr>');
        var td1=$('<td class="td1"></td>');
        var td2=$('<td class="td2"></td>');
        var td3=$('<td class="td3"></td>');
        var imgObj=$('<img width="20px" />');

        td2.append("宝贝详情页属性数");
        var itemPropsNum = 0;
        if(DiagResult.itemProps === undefined || DiagResult.itemProps == null || DiagResult.itemProps == "") {
            itemPropsNum = 0;
        } else {
            itemPropsNum = DiagResult.itemProps.split(",").length;
        }
        var catPropsNum = DiagResult.cidProps.split(",").length;
        var lackPropNames = catPropsNum - itemPropsNum > 0 ? QueryCommodity.rule.getLackPropNames(DiagResult.itemProps, DiagResult.cidProps.split(",")) : "";
        if(itemPropsNum > 0)
        {
            imgObj.attr("src","http://img02.taobaocdn.com/imgextra/i2/1039626382/T2FXG2XfFXXXXXXXXX_!!1039626382.png");
            td1.append(imgObj);
            var suggestSpan=$('<span ></span>');
            suggestSpan.append("您的宝贝详情页属性词有<span class='bold'>"+itemPropsNum+"</span>个,");
            if(catPropsNum > itemPropsNum) {
                suggestSpan.append("但还有下述属性未填写：<br> <span class='bold'>" + lackPropNames + "</span>");
            } else {
                suggestSpan.append("已全部填写，做的非常不错哦~");
            }
            td3.append(suggestSpan);
        }
        else
        {
            imgObj.attr("src","http://img01.taobaocdn.com/imgextra/i1/1039626382/T2ZOxZXc8dXXXXXXXX_!!1039626382.png");
            td1.append(imgObj);
            var suggestSpan=$('<span ></span>');
            suggestSpan.append("您的宝贝详情页未填写任何属性词，严重影响了亲的类目流量，请添加完整");


            td3.append(suggestSpan);
        }

        tr.append(td1);
        tr.append(td2);
        tr.append(td3);
        return tr;
    },
    createRow11:function(DiagResult){
        var tr=$('<tr class="intro1"></tr>');
        var td1=$('<td class="td1"></td>');
        var td2=$('<td class="td2"></td>');
        var td3=$('<td class="td3"></td>');
        var imgObj=$('<img width="20px" />');

        td2.append("宝贝销量");
        var tradeCount = parseInt(DiagResult.tradeCount);
        if(tradeCount <= 0)
        {

            imgObj.attr("src","http://img01.taobaocdn.com/imgextra/i1/1039626382/T2ZOxZXc8dXXXXXXXX_!!1039626382.png");
            td1.append(imgObj);
            td3.append($('<span>亲，您的宝贝销量为<span style="color: red;font-size: 16px;">0</span>，要加油了哦</span>'));
        }
        else if(tradeCount <= 10)
        {
            imgObj.attr("src","http://img01.taobaocdn.com/imgextra/i1/1039626382/T2ZOxZXc8dXXXXXXXX_!!1039626382.png");
            td1.append(imgObj);
            td3.append($('<span>亲，您的宝贝销量低于<span style="color: red;font-size: 16px;">10</span>，要加油了哦</span>'));
        }
        else if(tradeCount <= 50)
        {
            imgObj.attr("src","http://img01.taobaocdn.com/imgextra/i1/1039626382/T2ZOxZXc8dXXXXXXXX_!!1039626382.png");
            td1.append(imgObj);
            td3.append($('<span>亲，您的宝贝销量低于<span style="color: red;font-size: 16px;">50</span>，要加油了哦</span>'));
        }
        else if(tradeCount <= 100)
        {
            imgObj.attr("src","http://img01.taobaocdn.com/imgextra/i1/1039626382/T2ZOxZXc8dXXXXXXXX_!!1039626382.png");
            td1.append(imgObj);
            td3.append($('<span>亲，您的宝贝销量低于<span style="color: red;font-size: 16px;">100</span>，要加油了哦</span>'));
        }
        else
        {

            imgObj.attr("src","http://img02.taobaocdn.com/imgextra/i2/1039626382/T2FXG2XfFXXXXXXXXX_!!1039626382.png");
            td1.append(imgObj);
            td3.append($('<span>亲，您的宝贝销量高于<span style="color: red;font-size: 16px;">100</span>，非常给力哦</span>'));
        }
        tr.append(td1);
        tr.append(td2);
        tr.append(td3);
        return tr;
    },
    createRow12:function(DiagResult){
        var tr=$('<tr class="intro1"></tr>');
        var td1=$('<td class="td1"></td>');
        var td2=$('<td class="td2"></td>');
        var td3=$('<td class="td3"></td>');
        var imgObj=$('<img width="20px" />');

        td2.append("标题行业类目热度");
        var tradeCount = parseInt(DiagResult.tradeCount);
        imgObj.attr("src","http://img02.taobaocdn.com/imgextra/i2/1039626382/T2FXG2XfFXXXXXXXXX_!!1039626382.png");
        td1.append(imgObj);

        td3.append($('<span>亲，您的宝贝标题行业类目搜索热度总数为<span style="color: red;font-size: 16px;" class="titleOpDiagTitleCatSearchHot">'+DiagResult.titleCatPv+'</span>，行业类目预期展现指数为<span class="titleOpDiagTitleCatSearchpre" style="color: red;font-size: 16px;">'+DiagResult.titleCatPrePv+'</span></span>'));

        tr.append(td1);
        tr.append(td2);
        tr.append(td3);
        return tr;
    }
}, QueryCommodity.commodityDiv);

QueryCommodity.rule = QueryCommodity.rule || {};

QueryCommodity.rule = $.extend({
    sort:1,
    itemStatus:0,     //默认全部
    isOptimised:1,    //默认全部
    getRuleJson: function() {
        var ruleJson = {
            pn: 1,
            ps: 10
        };

        var searchText = $("#searchText").val();
        if (searchText != null && searchText != ""){
            ruleJson.searchText = searchText;
        }else{ruleJson.searchText == ""}
        
        var numIid = $("#numIid").val();
        if (numIid != null && numIid != "") {
            ruleJson.numIid = numIid;
        } else {ruleJson.numIid == ""}


        var lowBegin=parseInt($('#lowBegin').val()) ;
        if(lowBegin>=0&&lowBegin<100)
            ruleJson.lowBegin=lowBegin;
        else ruleJson.lowBegin=0;

        var topEnd=parseInt($('#topEnd').val()) ;
        if(topEnd<=100&&topEnd>0)
            ruleJson.topEnd=topEnd;
        else ruleJson.topEnd=100;


        return ruleJson;
    },
    clearCookie : function(){
        $.cookie("isCatSearch",null);
    },
    getLackPropNames : function(itemProps, cidProps){
        if(cidProps.length <= 0) {
            return "";
        }
        if(itemProps === undefined || itemProps == null) {
            return cidProps.join(",");
        }
        var lackPropNames = [];
        $(cidProps).each(function(i, propName){
            if(itemProps.indexOf(propName) < 0) {
                lackPropNames.push(propName);
            }
        });
        return lackPropNames.join(",");
    },
    parseLongToDate:function(ts) {
    	if(ts == 0) {
    		return "-"; 
    	}
		var theDate = new Date();
		theDate.setTime(ts);
		var year = theDate.getFullYear();
		var month = theDate.getMonth() + 1;//js从0开始取
		var date = theDate.getDate();
		var hour = theDate.getHours();
		var minutes = theDate.getMinutes();
		var second = theDate.getSeconds();

		if (month < 10) {
			month = "0" + month;
		}
		if (date < 10) {
			date = "0" + date;
		}
		if (hour < 10) {
			hour = "0" + hour;
		}
		if (minutes < 10) {
			minutes = "0" + minutes;
		}
		if (second < 10) {
			second = "0" + second;
		}
		var timeStr = year+"-"+month+"-"+date+" "+hour+":"+minutes+":"+second;
		return timeStr;
	}
}, QueryCommodity.rule);

QueryCommodity.init = function(currentPage,params) {
    $.cookie("iskitty",params.iskitty);
    QueryCommodity.rule.clearCookie();
    var searchBtn = $('#searchBtn');
    $("#lowBegin").val(params.start);
    $("#topEnd").val(params.end);
    QueryCommodity.rule.sort=params.sort;
    QueryCommodity.rule.itemStatus=params.status;

    $('#searchText').keydown(function() {
        if (event.keyCode == "13") {//keyCode=13是回车键
            searchBtn.click();
        }
    });
    
	$.get("/items/sellerCatStatusCount", function(data) {
		var sellerCat = $('#sellerCat');
		sellerCat.empty();
		if(!data || data.length == 0) {
			sellerCat.hide();
			return;
		}

		var cat = $('<option>卖家类目（在售|仓库）</option>');
		sellerCat.append(cat);
		for(var i = 0; i < data.length; i++) {
			if(data[i].totalCount <= 0){
				continue;
			}

			var option = $('<option></option>');
			option.attr("catId", data[i].id);
			option.html(data[i].name + "（" + data[i].onSaleCount + " | " + data[i].inStockCount + "）");
			sellerCat.append(option);
		}
	});

	$.get("/items/itemCatStatusCount", function(data) {
		var taobaoCat = $('#taobaoCat');
		taobaoCat.empty();
		if(!data || data.length == 0) {
			taobaoCat.hide();
			return;
		}

		var cat = $('<option>淘宝类目（在售|仓库）</option>');
		taobaoCat.append(cat);
		for(var i = 0; i < data.length; i++) {
			if(data[i].totalCount <= 0){
				continue;
			}
			
			var option = $('<option></option>');
			option.attr("catId", data[i].id);
			option.html(data[i].name + "（" + data[i].onSaleCount + " | " + data[i].inStockCount + "）");
			taobaoCat.append(option);
		}
	});

    searchBtn.click(function(){
        //if($('#NumIid').val() == "") {
            $.cookie("isCatSearch",0);
            $.cookie("catId",0);

            if($('#itemsStatus option:selected').attr("tag")=="onsale"){
                QueryCommodity.rule.itemStatus = 0;
            } else if($('#itemsStatus option:selected').attr("tag")=="instock") {
                QueryCommodity.rule.itemStatus = 1;
            } else {
                QueryCommodity.rule.itemStatus = 2;
            }
            
            if($('#sort option:selected').attr("tag")=="score"){
                QueryCommodity.rule.sort= 1;
            } else if($('#sort option:selected').attr("tag")=="created") {
                QueryCommodity.rule.sort= 2;
            } else {
                QueryCommodity.rule.sort= 1;
            }

            if($('#isOptimised option:selected').val()==2){
                QueryCommodity.rule.isOptimised = 2;
            } else if($('#isOptimised option:selected').val()==4) {
                QueryCommodity.rule.isOptimised = 4;
            } else {
                QueryCommodity.rule.isOptimised = 1;
            }
            QueryCommodity.query.getComms(currentPage);
        /*} else {
            var numIid = $('#NumIid').val();
            var ruleJson = QueryCommodity.rule.getRuleJson();
            ruleJson.pn = currentPage + 1;
            var pageData = {};
            pageData.s="";
            pageData.pn=1;
            pageData.ps=10;
            pageData.lowBegin=0;
            pageData.topEnd=100;
            pageData.sort=QueryCommodity.rule.sort;
            pageData.status = QueryCommodity.rule.itemStatus;

            $.post("/titles/diagNumIid",{numIid : numIid}, function(data){
                $('.Pagination').empty();
                Loading.init.hidden();
                var res;
                var  diagContainer =$("#commodityDiag");
                diagContainer.empty();
                var commodityDiag= $("<div></div>");


                var size = data.res.length;
                //var tableHead=$('<table width="883px" style="border:1px solid #B0B0B0;background-color: #259;"></table>');
                //var thead=$('<thead></thead>');
                // var th=$('<th style="color: white;">宝贝列表</th>');
                // thead.append(th);
//                var thead=$('<div class="itemsListHead" ></div>');
//                thead.append("宝贝列表");

                //tableHead.append(thead);
                //commodityDiag.append(tableHead);
//                commodityDiag.append(thead);

                for ( var i = 0; i < size; i++) {
                    //  if(data.res[i].score>=lowBegin&&data.res[i].score<=topEnd){
                    res=QueryCommodity.commodityDiv.createDiv(data.res[i], pageData, data,i);
                    if(i<2){
                        res.find(".diagDetail").css("display","block");
                    }

                    commodityDiag.append(res);
                    var youhuaLink=res.find(".jumpTo");
                    var jumpHref=youhuaLink.attr("href");
                    var jumpToRename=res.find(".jumpToRename");
                    jumpToRename.attr("href",jumpHref);

                    var titleArea=res.find(".title");
                    titleArea.click(function(){
                        if($(this).parent().find(".diagDetail").css("display")=="none")
                            $(this).parent().find(".diagDetail").css("display","block");
                        else if($(this).parent().find(".diagDetail").css("display")=="block")
                            $(this).parent().find(".diagDetail").css("display","none");
                    });
                }
                diagContainer.append(commodityDiag.children());

                var youhua=$(".youhua");
                youhua.mouseover(function(){$(this).attr("src","http://img04.taobaocdn.com/imgextra/i4/1039626382/T2qo51XaVXXXXXXXXX_!!1039626382.png")});
                youhua.mouseout(function(){$(this).attr("src","http://img03.taobaocdn.com/imgextra/i3/1039626382/T2OF12XfdXXXXXXXXX_!!1039626382.png")});


            });
        }*/

        /*Loading.init.show();
        var container = $("#commodityDiag");
        container.html("");
        var data = {};
        var ruleJson = QueryCommodity.rule.getRuleJson();
        data.s=ruleJson.searchText;
        data.pn=ruleJson.pn;
        data.ps=ruleJson.ps;
        data.lowBegin=ruleJson.lowBegin;
        data.topEnd=ruleJson.topEnd;
        data.sort=QueryCommodity.rule.sort;
        $.ajax({
            //async : false,
            url : '/Titles/listDiag',
            type : "post",
            data : data,
            success : function(data) {
                Loading.init.hidden();
                var totalCount = data.totalPnCount*ruleJson.ps;
                var per_page = ruleJson.ps;
                QueryCommodity.query.initPagination(totalCount, per_page, currentPage);

            }
        });*/

    });
//    searchBtn.mouseout(function(){$(this).attr("src","/public/images/button/searchComm1.png");});
//    searchBtn.mouseover(function(){$(this).attr("src","/public/images/button/searchComm2.png");});

    $("#closeImg").click(function(){
        if($("#warmNotice ol").css("display")=="block")
        {
//            $("#warmNotice ol").css("display","none");
            $("#warmNotice ol").slideUp('normal');
            $("#closeImg").attr("src","http://img02.taobaocdn.com/imgextra/i2/1039626382/T2S5O1XoJXXXXXXXXX_!!1039626382.gif");
        }
        else if($("#warmNotice ol").css("display")=="none")
        {
//            $("#warmNotice ol").css("display","block");
            $("#warmNotice ol").slideDown('normal');
            $("#closeImg").attr("src","http://img03.taobaocdn.com/imgextra/i3/1039626382/T2No11XjNXXXXXXXXX_!!1039626382.gif");
        }
    });
    $("#scoreDown").click(function(){
        QueryCommodity.rule.sort= -1;
        $.cookie("isCatSearch",0);
        $.cookie("catId",0);
        QueryCommodity.query.getComms(1);
    });
    $("#scoreUp").click(function(){
        $.cookie("isCatSearch",0);
        $.cookie("catId",0);
        QueryCommodity.rule.sort= 1;
        QueryCommodity.query.getComms(1);
    });
    $("#onsaleItems").click(function(){
        $.cookie("isCatSearch",0);
        $.cookie("catId",0);
        QueryCommodity.rule.itemStatus= 0;
        QueryCommodity.rule.sort= 1;
        QueryCommodity.query.getComms(1);
    });
    $("#allItems").click(function(){
        $.cookie("isCatSearch",0);
        $.cookie("catId",0);
        QueryCommodity.rule.itemStatus= 2;
        QueryCommodity.rule.sort= 1;
        QueryCommodity.query.getComms(1);
    });
    $("#instockItems").click(function(){
        $.cookie("isCatSearch",0);
        $.cookie("catId",0);
        QueryCommodity.rule.itemStatus= 1;
        QueryCommodity.rule.sort= 2;
        QueryCommodity.query.getComms(1);
    });

    $('#NumIid').qtip({
        content: {
            text: "此为可选项，若填选宝贝id，则其余搜索条件将被忽略~"
        },
        position: {
            at: "top",
            corner: {
                target: 'leftBottom'
            }
        },
        show: {
            ready:false
        },
        style: {
            name:'cream'
        }
    });
//    $('#sellerCat').change(function(){
//        var catId=$('#sellerCat option:selected').attr("catId");
//        $.cookie("isCatSearch",1);
//        $.cookie("catId",catId);
//        var container = $("#commodityDiag");
//        container.html("");
//        var ruleJson = QueryCommodity.rule.getRuleJson();
//        ruleJson.pn = currentPage + 1;
//        var data = {};
//        data.pn=ruleJson.pn;
//        data.ps=ruleJson.ps;
//        data.catId=catId;
//        $.ajax({
//            //async : false,
//            url : '/Titles/getItemsBySellerCatId',
//            type : "post",
//            data : data,
//            success : function(data) {
//                Loading.init.hidden();
//                if(data.totalPnCount==0){
//                    TM.Alert.load("没有查询到对应的宝贝~")
//                }else{
//                    var totalCount = data.totalPnCount*ruleJson.ps;
//                    var per_page = ruleJson.ps;
//                    QueryCommodity.query.initPaginationWithCat(totalCount, per_page, currentPage);
//                }
//            }
//        });
//    });


    searchBtn.trigger('click');
};

