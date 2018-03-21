

var QueryCommodity = QueryCommodity || {};

QueryCommodity.query = QueryCommodity.query || {};

QueryCommodity.query = $.extend({
    getComms:function(currentPage, interval, endTime){
        TM.Loading.init.show();
        var container = $("#commodityDiag");
        container.html("");
        var pageData = {};
        var ruleJson = QueryCommodity.rule.getRuleJson();
        pageData.s=ruleJson.searchText;
        pageData.numIid=ruleJson.numIid;
        // data.pn=ruleJson.pn;
        pageData.pn=currentPage;
        pageData.ps=ruleJson.ps;
        pageData.status = QueryCommodity.rule.itemStatus;
        pageData.sort = QueryCommodity.rule.sort;
        pageData.order = QueryCommodity.rule.order;
        pageData.isDesc = QueryCommodity.rule.isDesc;
        
        // 这个是卖家类目
        var catId = $("#sellerCat option:selected").attr("catid");
        if(catId === undefined || catId == null){
            catId = "";
        }
        pageData.catId = catId;
        // 这个是淘宝类目
        var taobaoCatId = $("#taobaoCat option:selected").attr("catid");
        if(taobaoCatId === undefined || taobaoCatId == null){
            taobaoCatId = "";
        }
        pageData.taobaoCatId = taobaoCatId;
        
        pageData.interval = interval;
        pageData.endTime = endTime;

        $('.tmpage').tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount:1,
            ajax: {
                param : pageData,
                on: true,
                dataType: 'json',
                url: "/diag/newListDiagTMpage",
                callback:function(data){
                    TM.Loading.init.hidden();
                    var res;
                    var diagContainer =$("#commodityDiag");
                    diagContainer.empty();
                    var commodityDiag= $("<div></div>");

                    var size = data.res.length;

                    for ( var i = 0; i < size; i++) {
                        var itemInfo = QueryCommodity.commodityDiv.createDiv(data.res[i], pageData, data,i);
                        var itemDiag = QueryCommodity.commodityDiv.createDetail(i, data.res[i]);
                        commodityDiag.append(itemInfo);
                        commodityDiag.append(itemDiag);
                    }

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
                    QueryCommodity.commodityDiv.createItemView(0, $('.liTargetDiv').first(), numIid);
                    //QueryCommodity.commodityDiv.createUvPv(0, $('.liTargetDiv').first(), numIid);
                }
            }
        });
    }
}, QueryCommodity.query);


QueryCommodity.commodityDiv = QueryCommodity.commodityDiv || {};
QueryCommodity.commodityDiv = $.extend({

    createDiv: function(DiagResult, pageData,data, offset) {
        DiagResult.new_alipay_trade_amt = new Number(DiagResult.alipay_trade_amt).toFixed(2);
        DiagResult.tranrate = parseInt(DiagResult.uv) == 0 ? "0.00%" : new Number(DiagResult.alipay_winner_num / DiagResult.uv).toPercent(2);
        DiagResult.itemCollectNumPer = parseInt(DiagResult.uv) == 0 ? "0.00%" : new Number(DiagResult.itemCollectNum / DiagResult.uv).toPercent(2);
        DiagResult.itemCartNumPer = parseInt(DiagResult.uv) == 0 ? "0.00%" : new Number(DiagResult.itemCartNum / DiagResult.uv).toPercent(2);
        data.pn = data.pn ? data.pn : 1;
        offset = (data.pn - 1) * data.ps + offset;
        DiagResult.pn = data.pn;
        DiagResult.offset = offset;
        var status = $('#itemsStatus option:selected').attr("tag");
        if(status == "onsale") {
            DiagResult.status = 0;
        } else if(status == "instock") {
            DiagResult.status = 1;
        } else {
            DiagResult.status = 2;
        }
        var tmpl = $('#diagitem').tmpl(
            [DiagResult]
        );
        return tmpl;

    },
    createTitleDiv: function(DiagResult, pageData,data, offset) {
        var titleDiv=$('<td class="title"></td>');
        var imgObj=QueryCommodity.commodityDiv.createImgDiv(DiagResult);
        titleDiv.append(imgObj);
        var titleContent=$('<div class="titleContent">&nbsp;</div>');

        var upper=$('<div></div>')

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
    createDetail: function(index, DiagResult) {
        var diagtr = $('<tr class="diagtr hidden"  numiid="'+DiagResult.numIid+'">' +
            '<td colspan="20">' +
            '<div class="tabDiv" style="width:100%;margin:10px auto 10px auto;">' +
            '   <ul class="clearfix" iid="'+DiagResult.numIid+'">' +
            '       <li>' +
            '           <a style="width: 100px;" targetcls="appComeInWords" href="javascript:void(0);" class="select">无线入店关键词</a>' +
            '       </li>' +
            '       <li>' +
            '           <a style="width: 100px;" targetcls="uvpv" href="javascript:void(0);" class="">流量来源</a>' +
            '       </li>' +
            '       <li>' +
            '           <a style="width: 100px;" targetcls="comeInWords" href="javascript:void(0);" class="">PC入店关键词</a>' +
            '       </li>' +
            '       <li>' +
            '           <a style="width: 100px;" targetcls="relativeAccess" href="javascript:void(0);">宝贝关联分析</a>' +
            '       </li>' +
            '       <li>' +
            '           <a style="width: 100px;" targetcls="itemViewTrade" href="javascript:void(0);">宝贝经营数据分析</a>' +
            '       </li>' +
            '       <li>' +
            '           <a style="width: 100px;" targetcls="itemView" href="javascript:void(0);">宝贝效果分析</a>' +
            '       </li>' +
            '       <li>' +
            '           <a style="width: 100px;" targetcls="itemTitleAnalysis" href="javascript:void(0);">预估进店词</a>' +
            '       </li>' +
            '       <li>' +
            '           <a style="width: 100px;" targetcls="skuDetail" href="javascript:void(0);">SKU销售详情</a>' +
            '       </li>' +
            '       <span class="tmbtn short-green-btn" style="float: right;" numiid="'+DiagResult.numIid+'">收起</span>' +
            '   </ul>' +
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
                case "comeInWords" :
                    QueryCommodity.commodityDiv.createComeInWords(index,diagtr.find('.liTargetDiv'), DiagResult.numIid);break;
                case "appComeInWords" :
                    QueryCommodity.commodityDiv.createAppComeInWords(index,diagtr.find('.liTargetDiv'), DiagResult.numIid);break;
                case "wirelessuvpv" :
                    QueryCommodity.commodityDiv.createWirelessuvpv(index,diagtr.find('.liTargetDiv'), DiagResult.numIid);break;
                case "uvpv" :
                    QueryCommodity.commodityDiv.createUvPv(index, diagtr.find('.liTargetDiv'), DiagResult.numIid);break;
                case "relativeAccess" :
                    QueryCommodity.commodityDiv.createRelativeAccess(index, diagtr.find('.liTargetDiv'), DiagResult.numIid);break;
                case "itemViewTrade" :
                    QueryCommodity.commodityDiv.createItemViewTrade(index, diagtr.find('.liTargetDiv'), DiagResult.numIid);break;
                case "itemView" :
                    QueryCommodity.commodityDiv.createItemView(index, diagtr.find('.liTargetDiv'), DiagResult.numIid);break;
                case "itemTitleAnalysis" :
                	QueryCommodity.commodityDiv.createItemTitleAnalysis(index, diagtr.find('.liTargetDiv'), DiagResult.numIid);break;
                case "skuDetail" :
                	QueryCommodity.commodityDiv.createSkuDetail(index, diagtr.find('.liTargetDiv'), DiagResult.numIid);break;
                default :
                    QueryCommodity.commodityDiv.createComeInWords(index, diagtr.find('.liTargetDiv'), DiagResult.numIid);break;
            }
        });
        diagtr.find('.tabDiv a').eq(0).trigger("click");
        // 收起按钮添加点击事件
        diagtr.find('.tmbtn').click(function(){
            var numiid = $(this).attr('numiid');
            $('.diagtr[numiid="'+numiid+'"]').hide(500);
        });
        return diagtr;
    },

    createItemTitleAnalysis : function(index, targetDiv, id){
    	targetDiv.empty();
        var hotSearchWords = $('.recommendDivTmp').clone();
        hotSearchWords.removeClass('recommendDivTmp');
        hotSearchWords.show();
        
        targetDiv.append(hotSearchWords);
        
        // 更改为默认排序  sortBy score
        var sortBy = "";
//        var sortBy = "impressions";
        var isDesc = true;
        QueryCommodity.commodityDiv.itemTitleAnalysisDiag(hotSearchWords.find(".hot-word-result-div"), id, sortBy, isDesc);
        
        $(".orderTd").click(function() {
    		if ($(this).find(".sort").hasClass("Desc")) {
    			$(".sort").removeClass("current-sort");
    			$(this).find(".sort").removeClass("Desc");
    			$(this).find(".sort").addClass("Asc");
    			$(this).find(".sort").addClass("current-sort");
    		} else {
    			$(".sort").removeClass("current-sort");
    			$(this).find(".sort").removeClass("Asc");
    			$(this).find(".sort").addClass("Desc");
    			$(this).find(".sort").addClass("current-sort");
    		}
    		var sortObj = $(".hot-word-result-table").find(".current-sort");
			if (sortObj.length == 0) {
				sortBy = "impressions";
			} else {
				sortBy = sortObj.attr("sort");
				if (sortObj.hasClass("Desc"))
					isDesc = true;
				else {
					isDesc = false;
				}
			}
			targetDiv.find('.hot-word-result-table').find('tbody .word-tr').remove();
			QueryCommodity.commodityDiv.itemTitleAnalysisDiag(hotSearchWords.find(".hot-word-result-div"), id, sortBy, isDesc);
    	});
    },
    createItemViewTrade : function(index,targetDiv, id){
        targetDiv.empty();
        var viewTrade = $('.itemDayViewTradeTmp').clone();
        viewTrade.removeClass('itemDayViewTradeTmp');

        var interval = QueryCommodity.rule.getDaysBetween();
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
    	
    	// viewTrade.find(".interval[value='" + interval + "']").attr('checked', true);
        viewTrade.show();

        // viewTrade.find('.interval').attr("name", "interval" + index);
        // viewTrade.find('.startTimeInput').attr("id", "startTimeInput" + index);

        viewTrade.find('.item-pv-uv-trade-trend-chart').attr("id", "item-pv-uv-trade-trend-chart" + id);
        targetDiv.append(viewTrade);
        QueryCommodity.Event.setItemViewTradeEvent2(viewTrade, id, index, interval);
    },
    createItemView: function(index,targetDiv, id){
        targetDiv.empty();
        var itemView = $('.itemViewTmp').clone();
        itemView.removeClass('itemViewTmp');

        var interval = QueryCommodity.rule.getDaysBetween();
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
    	
    	// itemView.find(".interval[value='" + interval + "']").attr('checked', true);
        itemView.show();

        // itemView.find('.interval').attr("name", "interval" + index);
        // itemView.find('.startTimeInput').attr("id", "startTimeInput" + index);

        itemView.find('.data_show').attr('id', 'data_show_' + id);
        targetDiv.append(itemView);
        $('#data_show_'+id).find('#view_item_show').empty();
        QueryCommodity.Event.setItemViewEvent2(itemView, id, index, interval);
    },
    createSkuDetail : function(index, targetDiv, id){
    	targetDiv.empty();
        var skuDetail = $('.skuDetailTmp').clone();
        skuDetail.removeClass('skuDetailTmp');
        
        var interval = QueryCommodity.rule.getDaysBetween();
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
    	
    	// skuDetail.find(".interval[value='" + interval + "']").attr('checked', true);
    	skuDetail.show();

    	// skuDetail.find('.interval').attr("name", "interval" + index);
    	// skuDetail.find('.startTimeInput').attr("id", "startTimeInput" + index);

    	skuDetail.find('.data_show').attr('id', 'data_show_' + id);
        targetDiv.append(skuDetail);
        $('#data_show_'+id).find('#sku_detail_show').empty();
        QueryCommodity.Event.setSkuDetailEvent2(skuDetail, id, index, interval);
    },
    createRelativeAccess : function(index,targetDiv, id){
        targetDiv.empty();
        var relativeWords = $('.relativeAccessTmp').clone();
        relativeWords.removeClass('relativeAccessTmp');
        
        var interval = QueryCommodity.rule.getDaysBetween();
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
    	
    	// relativeWords.find(".interval[value='" + interval + "']").attr('checked', true);
        relativeWords.show();

        // relativeWords.find('.interval').attr("name", "interval" + index);
        // relativeWords.find('.startTimeInput').attr("id", "startTimeInput" + index);

        targetDiv.append(relativeWords);
        QueryCommodity.Event.setRelitiveAccessEvent2(relativeWords, id, index, interval);
    },
    createComeInWords : function(index,targetDiv, id){
        targetDiv.empty();
        var comeInWords = $('.comeInWordsTmp').clone();
        comeInWords.removeClass('comeInWordsTmp');

        var interval = QueryCommodity.rule.getDaysBetween();
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
    	
    	// comeInWords.find(".interval[value='" + interval + "']").attr('checked', true);
        comeInWords.show();

        // comeInWords.find('.interval').attr("name", "interval" + index);
        // comeInWords.find('.startTimeInput').attr("id", "startTimeInput" + index);

        targetDiv.append(comeInWords);
        QueryCommodity.Event.setComeInWordsDivEvent2(comeInWords, id, index, false, interval);

    },
    createAppComeInWords : function(index,targetDiv, id){
        targetDiv.empty();
        var comeInWords = $('.appComeInWordsTmp').clone();
        comeInWords.removeClass('appComeInWordsTmp');
        
        var interval = QueryCommodity.rule.getDaysBetween();
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
    	
    	// comeInWords.find(".interval[value='" + interval + "']").attr('checked', true);
        comeInWords.show();

        // comeInWords.find('.interval').attr("name", "interval" + index);
        // comeInWords.find('.startTimeInput').attr("id", "startTimeInput" + index);

        targetDiv.append(comeInWords);
        QueryCommodity.Event.setComeInWordsDivEvent2(comeInWords, id, index, true, interval);
    },
    createWirelessuvpv : function(index, targetDiv, id){
        targetDiv.empty();
        var wirelessSrcUvPv = $('.wirelessSrcUvPv').clone();
        wirelessSrcUvPv.removeClass("wirelessSrcUvPv");
        
        var interval = QueryCommodity.rule.getDaysBetween();
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
    	
    	// wirelessSrcUvPv.find(".interval[value='" + interval + "']").attr('checked', true);
        wirelessSrcUvPv.show();

        // wirelessSrcUvPv.find('.interval').attr("name", "srcinterval" + index);
        // wirelessSrcUvPv.find('.startTimeInput').attr("id", "srcstartTimeInput" + index);

        wirelessSrcUvPv.find('.wireless-src-uvpv-result-table').attr("id", "wireless_src_" + id);
        targetDiv.append(wirelessSrcUvPv);
        QueryCommodity.Event.setWirelessSrcUvPvDivEvent2(wirelessSrcUvPv, id, index, interval);
    },
    createUvPv : function(index, targetDiv, id){
        targetDiv.empty();
        var srcUvPv = $('.SrcUvPvTmp').clone();
        srcUvPv.removeClass("SrcUvPvTmp");

        var interval = QueryCommodity.rule.getDaysBetween();
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
    	
    	// srcUvPv.find(".interval[value='" + interval + "']").attr('checked', true);
        srcUvPv.show();

        // srcUvPv.find('.interval').attr("name", "srcinterval" + index);
        // srcUvPv.find('.startTimeInput').attr("id", "srcstartTimeInput" + index);
        targetDiv.append(srcUvPv);
        QueryCommodity.Event.setSrcUvPvDivEvent2(srcUvPv, id, index, interval);
    },
    itemWirelessPCViewTrade : function(targetDiv, numIid, interval, endTime, platform){
        // 默认全店
        if(platform === undefined || platform == null) {
            platform = 0;
        }
        if(interval === undefined || interval == null) {
            interval = 7;
        }
        if(endTime == null) {
            endTime = new Date().getTime();
        }
        if(numIid == undefined || numIid == null) {
            alert("numIid为空");
        }
        //$.get("/Diag/ItemPlayPCWirelessViewTrade", {platform : platform, interval: interval, endTime: endTime, numIid : numIid}, function(data){
        //    if(data === undefined || data == null) {
        //        return;
        //    }
        //    if(data.success == false) {
        //        TM.Alert.load(data.message);
        //        return;
        //    }
        //    QueryCommodity.commodityDiv.refreshShopHighCharts(data, numIid);
        //});
        $('#item-pv-uv-trade-trend-chart' + numIid).empty();
        $('#item-pv-uv-trade-trend-chart' + numIid).append('<div style="line-height: 300px;"><img src="/img/promoteimages/loading.gif?_v=01" width="28px" height="28"/></div>');
        $.ajax({
            url: '/Diag/ItemPlayPCWirelessViewTrade',
            data: {platform : platform, interval: interval, endTime: endTime, numIid : numIid},
            type: 'GET',
            global: false,
            success: function(data){
                $('#item-pv-uv-trade-trend-chart' + numIid).empty();
                if(data === undefined || data == null) {
                    return;
                }
                if(data.success == false) {
                    TM.Alert.load(data.message);
                    return;
                }
                QueryCommodity.commodityDiv.refreshShopHighCharts(data, numIid);
            }
        });
    },
    refreshShopHighCharts : function(map, numIid){
        if(map === undefined || map == null) {
            return;
        }
        var pvArr = [], uvArr = [], tradeNumArr = [], tradeAmountArr = [], dayArr = [], tranRateArr = [], searchTranRateArr = [];
        $.each(map,function(key,values){
            dayArr.push(key)
            pvArr.push(parseInt(values.pv));
            uvArr.push(parseInt(values.uv));
            tradeNumArr.push(parseInt(values.alipay_trade_num));
            tradeAmountArr.push(parseFloat(new Number(values.alipay_trade_amt).toFixed(2)));
            var tranRate = parseInt(values.uv) == 0 ? 0.00 : parseFloat(new Number(values.alipay_winner_num / values.uv).toFixed(4));
            tranRateArr.push(tranRate);
            var searchTranRate = parseInt(values.searchUv) == 0 ? 0.00 : parseFloat(new Number(values.search_alipay_winner_num / values.searchUv).toFixed(4));
            searchTranRateArr.push(searchTranRate);
        });
        chart = new Highcharts.Chart({
            chart : {
                renderTo : 'item-pv-uv-trade-trend-chart' + numIid,
                defaultSeriesType: 'line' //图表类型line(折线图)
            },
            credits : {
                enabled: false   //右下角不显示LOGO
            },
            title: {
                text: "店铺流量销量趋势"
            }, //图表标题
            xAxis: {  //x轴
                categories: dayArr, //x轴标签名称
                gridLineWidth: 0, //设置网格宽度为1
                lineWidth: 2,  //基线宽度
                labels:{
                    y:20   //x轴标签位置：距X轴下方26像素
                    /*rotation: -45   //倾斜度*/
                }
            },
            yAxis: [{  //y轴
                    title: {text: '流量销量数据'}, //标题
                    lineWidth: 0 //基线宽度
                },
                {
                    title: {text: '成交转化率数据'}, //标题
                    lineWidth: 0, //基线宽度
                    opposite:true
                }
            ],
            plotOptions:{ //设置数据点
                line:{
                    dataLabels:{
                        enabled:true  //在数据点上显示对应的数据值
                    },
                    enableMouseTracking: true //取消鼠标滑向触发提示框
                }
            },
            series: [
                {  //数据列
                    name: '浏览量趋势',
//                    color:'#67CEFF',
                    color:'#96CBF1',
                    data: pvArr,
                    yAxis:0
                },
                {  //数据列
                    name: '访客数趋势',
                    data: uvArr,
//                    color:'#376FFF',
                    color:'#F5AD46',
                    yAxis:0
                },
                {  //数据列
                    name: '支付宝成交订单数趋势',
                    data: tradeNumArr,
//                    color:'#FFB23F',
                    color:'#6CBF3D',
                    yAxis:0
                },
                {  //数据列
                    name: '支付宝成交金额趋势',
                    data: tradeAmountArr,
                    color:'#2E91DA',
                    yAxis:0
                },
                {  //数据列
                    name: '成交转化率',
                    data: tranRateArr,
                    color:'#FF595F',
                    yAxis:1
                },
                {  //数据列
                    name: '站内搜索转化率',
                    data: searchTranRateArr,
                    color:'#EEDB03',
                    yAxis:1
                }
            ]
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
            html += '<tr class="app-word-diag-result-table-th '+ year+month+day +'" date="'+ year+month+day + '">'+
                '<td class="dataTime">'+ month+day+ weekHtml +'</td>'+
                '<td class="pv"> ~ </td>'+
                '<td class="uv"> ~ </td>'+
                '<td class="alipayTradeAmt"> ~ </td>'+
                '<td class="alipayAuctionNum"> ~ </td>'+
                '<td class="alipayTradeNum"> ~ </td>'+
                '<td class="tradeRate"> ~ </td>'+
                '<td class="entranceNum"> ~ </td>'+
                '<td class="entranceNumChange" style="cursor: pointer;"> ~ </td>'+
                '<td class="itemCollectNum"> ~ </td>'+
                '<td class="itemCollectNumPer"> ~ </td>'+
                '<td class="itemCartNum"> ~ </td>'+
                '<td class="itemCartNumPer"> ~ </td>'+
                '<td class="searchUv"> ~ </td>'+
                '<td class="CPCUv"> ~ </td>'+
                '<td class="pcuv"> ~ </td>' +
                '<td class="pcClick"> ~ </td>' +
                '<td class="bounceRate"> ~ </td></tr>';
        }
        $('#data_show_'+numIid).find('#view_item_show').append(html);
        
        // 跳失率
        $.ajax({
            url: '/Diag/getBounceRate',
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
                var target = $('#data_show_' + numIid).find('#view_item_show');
                $.each(data, function(key, values){
                    var targetTr = target.find('.' + key);
                    var bounceRate = values.bounceRate == '0.00%' ? '0.00%' : new Number(values.bounceRate).toPercent(2);
                    targetTr.find('.bounceRate').html(bounceRate);
                });
            }
        });
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
                    targetTr.find('.alipayTradeNum').html(values.alipayWinnerNum);
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
                var target = $('#data_show_'+numIid).find('#view_item_show');
                $.each(data, function(key, values){
                    var targetTr = target.find('.' + key);
                    targetTr.find('.searchUv').html(values.searchUv);
                });
            }
        });
        $.ajax({
            url: '/Diag/getCPCUV',
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
                    targetTr.find('.CPCUv').html(values.uv);
                });
            }
        });
        $.ajax({
            url: '/Diag/getPCUV',
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
                    targetTr.find('.pcuv').html(values.pcUv);
                });
                QueryCommodity.commodityDiv.getImpression(numIid, interval, endTime);
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
                    var change = '<span class="reduce_count" style="color: green;">-' + values.reduceNum + '</span>/<span class="increase_count" style="color: red;">+' + values.increaseNum + '</span>'; 
                        targetTr.find('.entranceNumChange').html(change);
                });
                // 查看入口数详情按钮点击事件
                $(".entranceNumChange").unbind().click(function(){
                    var date = $(this).parent().attr("date");
                    var numIidStr = $(this).parent().parent().parent().attr("id");
                    var numIid = numIidStr.substring(10)
                    var reduceCount = $(this).find(".reduce_count").html()
                    var increaseCount = $(this).find(".increase_count").html()
                    $.post("/Diag/showEntranceDetail", {date:date, numIid:numIid}, function(data){
                    	if(!data.success) {
                    		TM.Alert.load(data.message);
                			return false;
                    	}
                    	
                    	var dataJsonArray = data.res;
                    	var targetDiv = $('.entranceDetailTmp').clone();
                    	targetDiv.removeClass("entranceDetailTmp");
                    	targetDiv.find(".reduceCount").html(reduceCount);
                    	targetDiv.find(".increaseCount").html(increaseCount);
                    	targetDiv.show();
                    	var html = '';
                    	if(dataJsonArray == null || dataJsonArray == "") {
                    		targetDiv.find(".entrance_detail_show").append('<tr class="word-tr no-data" style="height: 32px; font-size: 16px; color: red;"><td colspan="2">暂无符合要求的数据！</td></tr>');
                    		TM.Alert.loadDetail(targetDiv, 1000, 530);
                    		return false;
                    	}
                    	$(dataJsonArray).each(function(index, dataJson) {
                    		var imgHtml = '<img src="http://txgyun.b0.upaiyun.com/paiming/img/rank/personalcenter/sj.png">';
                    		var reduceWordImg = "";
                    		var increaseWordImg = "";
                    		if(dataJson.reduceWordPC == false) {
                    			reduceWordImg = imgHtml;
                    		}
                    		if(dataJson.increaseWordPC == false) {
                    			increaseWordImg = imgHtml;
                    		}
                    		html += '<tr style="height: 24px;">' +
                            '<td class="entrance_reduceWord">' + dataJson.reduceWord + ' ' + reduceWordImg + '</td>'+
                            '<td class="entrance_increaseWord">' + dataJson.increaseWord + ' ' + increaseWordImg + '</td></tr>';
                        });
                    	targetDiv.find(".entrance_detail_show").append(html)
                    	TM.Alert.loadDetail(targetDiv, 1000, 530);
                    });
                });
            }
        });
    },
	skuDetailDiag: function(targetDiv, numIid, interval, endTime){
		if(interval == null) {
			interval = 7;
		}
		if(endTime == null) {
			endTime = new Date().getTime();
		}
		
		var tBody = $('#data_show_'+numIid).find('#sku_detail_show');
		tBody.empty();
		tBody.append('<tr class="loading"><td colspan="20"><img src="/img/promoteimages/loading.gif?_v=01" width="28px" height="28"/></td></tr>');
		// 宝贝效果分析
		$.ajax({
			url: '/Diag/getSkuDetail',
			data: {numIid:numIid, interval:interval, endTime:endTime},
			type: 'POST',
			global: false,
			success: function(data){
				if(data.success != undefined && !data.success){
					TM.Alert.load(data.message);
					return;
				}
				
				var dataJsonArray = data.res;
				tBody.empty();
				var html = '';
				
				if(dataJsonArray == null || dataJsonArray == "") {
					tBody.append('<tr class="word-tr no-data" style="height: 32px; font-size: 16px; color: red;"><td colspan="10">暂无符合要求的数据！</td></tr>');
					return false;
				}
				$(dataJsonArray).each(function(index, dataJson) {
					html += '<tr style="height: 36px;">' +
					'<td class="properties_name">' + dataJson.propertiesName + '</td>' +
					'<td class="price">' + dataJson.price + '元</td>' +
					'<td class="sku_stock">' + dataJson.skuStock + '</td>' +
					'<td class="add_cart_user_num">' + dataJson.addCartUserNum + '</td>' +
					'<td class="gmv_auction_num">' + dataJson.gmvAuctionNum + '</td>' +
					'<td class="gmv_winner_num">' + dataJson.gmvWinnerNum + '</td>' +
					'<td class="alipay_auction_num">' + dataJson.alipayAuctionNum + '</td>' +
					'<td class="alipay_winner_num">' + dataJson.alipayWinnerNum + '</td></tr>';
				});
				tBody.append(html)
			}
		});
		// 给导出数据按钮添加点击事件
		$("[name='export_sku_excel']").unbind('click').bind("click", function(){
			var form = $("<form>");   //定义一个form表单
			form.attr('style', 'display:none');   //在form表单中添加查询参数
			form.attr('method', 'get');
			form.attr('action', "/Diag/exportSkuExcel");

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
                    var pcuv = parseInt(targetTr.find('.pcuv').html());
                    if(impression == 0){
                        targetTr.find('.pcClick').html('0.00%');
                        return;
                    }
                    var pcClick = parseInt(pcuv) == 0 ? '0.00%' : new Number(pcuv / impression).toPercent(2);
                    targetTr.find('.pcClick').html(pcClick);
                });
            }
        });
    },
    relativeAccessDiag : function(targetDiv, numIid, interval, endTime){
        if(interval == null) {
            interval = 7;
        }
        if(endTime == null) {
            endTime = new Date().getTime();
        }
        //$.post("/Diag/relativeAccessByNumIid", {numIid:numIid, interval:interval, endTime:endTime}, function(data){
        //    if(data === undefined || data == null) {
        //        return;
        //    }
        //    if(data.success == false) {
        //        TM.Alert.load(data.message);
        //        return;
        //    }
        //    targetDiv.find('.relativeAccess-table .asso_access_num').html(data.asso_access_num);
        //    targetDiv.find('.relativeAccess-table .asso_access_user_num').html(data.asso_access_user_num);
        //    targetDiv.find('.relativeAccess-table .asso_alipay_num').html(data.asso_alipay_num);
        //    targetDiv.find('.relativeAccess-table .asso_alipay_user_num').html(data.asso_alipay_user_num);
        //    targetDiv.find('.relativeAccess-table .asso_alipay_auction_num').html(data.asso_alipay_auction_num);
        //    targetDiv.find('.relativeAccess-table .asso_alipay_amt').html(new Number(data.asso_alipay_amt).toFixed(2));
        //});
        var html = '<img src="/img/promoteimages/loading.gif?_v=01" width="28px" height="28"/>';
        targetDiv.find('.relativeAccess-table .asso_access_num').html(html);
        targetDiv.find('.relativeAccess-table .asso_access_user_num').html(html);
        targetDiv.find('.relativeAccess-table .asso_alipay_num').html(html);
        targetDiv.find('.relativeAccess-table .asso_alipay_user_num').html(html);
        targetDiv.find('.relativeAccess-table .asso_alipay_auction_num').html(html);
        targetDiv.find('.relativeAccess-table .asso_alipay_amt').html(html);
        $.ajax({
            url: '/Diag/relativeAccessByNumIid',
            data: {numIid:numIid, interval:interval, endTime:endTime},
            type: 'POST',
            global: false,
            success: function(data){
                if(data === undefined || data == null) {
                    return;
                }
                if(data.success == false) {
                    TM.Alert.load(data.message);
                    return;
                }
                targetDiv.find('.relativeAccess-table .asso_access_num').html(data.asso_access_num);
                targetDiv.find('.relativeAccess-table .asso_access_user_num').html(data.asso_access_user_num);
                targetDiv.find('.relativeAccess-table .asso_alipay_num').html(data.asso_alipay_num);
                targetDiv.find('.relativeAccess-table .asso_alipay_user_num').html(data.asso_alipay_user_num);
                targetDiv.find('.relativeAccess-table .asso_alipay_auction_num').html(data.asso_alipay_auction_num);
                targetDiv.find('.relativeAccess-table .asso_alipay_amt').html(new Number(data.asso_alipay_amt).toFixed(2));
            }
        });
    },
    itemTitleAnalysisDiag : function(targetDiv, numIid, sortBy, isDesc){
        
        var table = targetDiv.find('.hot-word-result-table');
        table.find('tbody').append('<tr class="loading"><td colspan="20"><img src="/img/promoteimages/loading.gif?_v=01" width="28px" height="28"/></td></tr>');
        
        targetDiv.parent().find('.hot-word-paging').tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount: 1,
            ajax: {
                on: true,
                param: {numIid : numIid, sortBy : sortBy, isDesc : isDesc},
                dataType: 'json',
                url: '/Diag/getItemTitleAnalysis',
                global: false,
                callback: function(data){
                    table.find('tbody .loading').remove();
                    if(data === undefined || data == null) {
                        return;
                    }
                    if(data.success == false) {
                    	TM.Alert.load(data.message);
                        return;
                    }
                    if(data.res == null){
                        return;
                    }
                    table.find('tbody .word-tr').remove();
                    
                    var maxHot = 0;
                    if(data.res.length > 0) {
                    	$(data.res).each(function(i, word){
                    		hot = word.hotSearchDegree;
                    		if(parseInt(hot) > parseInt(maxHot)) {
                    			maxHot = hot;
                    		}
                    	});
                        $(data.res).each(function(i, word){
                        	var hotSearchStr = parseInt(word.hotSearchDegree / 10000);
                        	var hotSearchPercent = new Number(word.hotSearchDegree / maxHot).toPercent(2);
                            table.find('tr.no-data').remove();
                            table.find('tbody').append($('<tr class="word-tr"><td colspan="6" class="word-td">'+word.name+'</td><td colspan="6" class="word-td">'+word.word+'</td>' +
                            		'<td colspan="5" class="word-td"><div class="progress" title="'+hotSearchStr+'万"><div class="progress-bar" style="width: '+hotSearchPercent+';"><span class="sr-only"></span></div></td>' +
                            		'<td colspan="3" class="word-td">'+new Number(word.relevancy).toPercent(2)+'&nbsp;</td></tr>'))
                        });
                    } else {
                        table.find('tr.no-data').remove();
                        table.find('tbody').append($('<tr class="word-tr no-data" style="height: 45px;"><td colspan="20">未找到相关热词，请稍后重试或者联系我们</td></tr>'));
                    }
                }
            }
        });

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
        //targetDiv.empty();
        //var table = searchDiag.Diag.createTableHtml();
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
                            var clickRate = parseInt(word.pv) == 0 ? "0.00%" : new Number(word.click / word.pv).toPercent(2);
                            table.find('tbody').append($('<tr class="word-tr"><td class="word-td">'+word.word+'</td><td>'+word.pv+'</td><td>'+word.click+'</td><td>'+clickRate+'</td><td>'+word.uv+'</td>' +
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
        //targetDiv.empty();
        //var table = searchDiag.Diag.createTableHtml();
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
                                '<td>'+word.alipay_winner_num+'</td><td>'+word.alipay_auction_num+'</td><td>'+new Number(word.alipay_trade_amt).toFixed(2)+'</td><td>'+tranrate+'</td><td><span title="查看浏览量趋势" class="inlineblock pvTrend"></span></td></tr>'))
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
        	var btnObj = $(this);
            var numIid = btnObj.parent().parent().parent().parent().parent().parent().attr('numiid');
            var interval = btnObj.parent().find("input:radio:checked").val();
            var html = "<div style='margin-left: 35px; font-size: 16px; font-family: 微软雅黑;'><span style='color: red;'>请填写需要过滤的关键词和次数,以 ',' 分开。例：女装,3</span><textarea class='key_word_area' style='width: 400px; height: 130px; margin-top: 15px; padding: 5px 0 0 10px; font-size: 14px; font-family: 微软雅黑;' placeholder='每行一条数据'></textarea><div>";
            TM.Alert.loadDetail(html, 500, 300, function(){
            	var content = $(".key_word_area").val();
            	QueryCommodity.commodityDiv.exportExcelClick2(numIid, interval, content, btnObj);
            }, "关键词过滤");
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
        //$.post('/Diag/getWireelessItemSource', {numIid:numIid, interval:interval, endTime:endTime}, function(data){       });
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
        //$.post("/Diag/getPCItemSource", {numIid:numIid, interval:interval, endTime:endTime}, function(data){        });
        // 无线端流量来源
        QueryCommodity.commodityDiv.wirelessSrcUvPvDiag(targetDiv, numIid, interval, endTime);
        targetDiv.append(table);
    },
    exportExcelClick: function(numIid, interval, content, obj){
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
        
        var contentInput = $('<input>');
        contentInput.attr('type', 'hidden');
        contentInput.attr('name', 'content');
        contentInput.attr('value', content);

        $('body').append(form);  //将表单放置在web中
        form.append(intervalInput);   //将查询参数控件提交到表单上
        form.append(numIidInput);
        form.append(endTimeInput);
        form.append(contentInput);
        form.submit();
    },
    exportExcelClick2: function(numIid, interval, content, obj){
        var interval = QueryCommodity.rule.getInterval2(obj.parent());
        var endTime = QueryCommodity.rule.getEndTime2(obj.parent());
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

        var contentInput = $('<input>');
        contentInput.attr('type', 'hidden');
        contentInput.attr('name', 'content');
        contentInput.attr('value', content);

        $('body').append(form);  //将表单放置在web中
        form.append(intervalInput);   //将查询参数控件提交到表单上
        form.append(numIidInput);
        form.append(endTimeInput);
        form.append(contentInput);
        form.submit();
    }
}, QueryCommodity.commodityDiv);


//配置dateRangePicker插件
var locale = {
    "format": 'YYYY-MM-DD',
    "separator": " - ",
    "applyLabel": "确定",
    "cancelLabel": "取消",
    "fromLabel": "起始时间",
    "toLabel": "结束时间'",
    "customRangeLabel": "自定义",
    "weekLabel": "W",
    "daysOfWeek": ["日", "一", "二", "三", "四", "五", "六"],
    "monthNames": ["一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"],
    "firstDay": 1,
};

QueryCommodity.Event = QueryCommodity.Event || {};
QueryCommodity.Event = $.extend({
    setCommsEvent: function(){
        var commsDiv = $('.commsDiv');
        var curr = new Date().getTime();
        var dayMillis = 24 * 3600 * 1000;
        var timeZoneDiff = 8 * 3600 * 1000;
        var startTimeInput = commsDiv.find(".startTimeInput");
        var endTimeInput = commsDiv.find(".endTimeInput");
        startTimeInput.datepicker();
        startTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
        startTimeInput.val(new Date(curr - 7 * dayMillis).formatYMS());

        endTimeInput.datepicker();
        endTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
        endTimeInput.val(new Date(curr).formatYMS());

        startTimeInput.unbind('change').change(function(){
        	commsDiv.find('.interval-tr .interval[value="0"]').trigger("click");
        });
        endTimeInput.unbind('change').change(function(){
        	commsDiv.find('.interval-tr .interval[value="0"]').trigger("click");
        });

        commsDiv.find('.interval-tr .interval').unbind('click').click(function(){
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
                    var currDate = new Date();
                    var currTime = parseInt((currDate.getTime() + timeZoneDiff) / dayMillis) * dayMillis - timeZoneDiff;
                    if(endTime == currTime) {
                    	endTime = endTime - dayMillis;
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
            var currentPage = 1;
            QueryCommodity.query.getComms(currentPage, interval, endTime);
        });
        // 点击input文字相当于点击input
        QueryCommodity.Event.setIntervalSpanClickEvent(commsDiv);
        commsDiv.find('.interval-tr .interval:checked').trigger("click");
    },
    setCommsEvent2 : function () {
        var dayMillis = 24 * 3600 * 1000;
        var date_range_picker = $(".commsDiv input[drpid='date-range-picker']");
        date_range_picker.unbind('change').change(function () {
            var endTime, interval;
            var startTimeInput, endTimeInput;
            startTimeInput = date_range_picker.val().split(locale.separator)[0];
            endTimeInput = date_range_picker.val().split(locale.separator)[1];
            endTime = parseInt(Date.parse(endTimeInput.toString().replace("-","/")));
            if(endTime > new Date().getTime()) {
                TM.Alert.load("截止时间请勿超过当前时间");
                return;
            }
            var startTime = parseInt(Date.parse(startTimeInput.toString().replace("-","/")));
            if(endTime < startTime) {
                TM.Alert.load("截止时间请勿小于开始时间");
                return;
            }
            interval = Math.floor((endTime - startTime) / dayMillis) + 1;

            var currentPage = 1;
            QueryCommodity.query.getComms(currentPage, interval, endTime);
        });
        date_range_picker.daterangepicker({
            "locale": locale,
            "ranges" : {
                '最近1天': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
                '最近3天': [moment().subtract(3, 'days'), moment().subtract(1, 'days')],
                '最近7天': [moment().subtract(7, 'days'), moment().subtract(1, 'days')],
                '最近14天': [moment().subtract(14, 'days'), moment().subtract(1, 'days')]
            },
            "opens":"right",
            "startDate": moment().subtract(1, 'days'),//默认最近1天
            "endDate": moment().subtract(1, 'days'),
            "maxDate": moment().subtract(1, 'days')
        })
    },
    setItemViewTradeEvent : function(viewTradeDiv, numIid, index){
        var curr = new Date().getTime();
        var dayMillis = 24 * 3600 * 1000;
        var startTimeInput = viewTradeDiv.find(".startTimeInput");
        var endTimeInput = viewTradeDiv.find(".endTimeInput");
        startTimeInput.datepicker();
        startTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
        startTimeInput.val(new Date(curr - 7 * dayMillis).formatYMS());

        endTimeInput.datepicker();
        endTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
        endTimeInput.val(new Date(curr).formatYMS());

        startTimeInput.unbind('change').change(function(){
            viewTradeDiv.find('.interval-tr .interval[value="0"]').trigger("click");
        });
        endTimeInput.unbind('change').change(function(){
            viewTradeDiv.find('.interval-tr .interval[value="0"]').trigger("click");
        });



        // 设置时间单选框事件
        viewTradeDiv.find('.interval-tr .interval').unbind('click').click(function(){
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
            };
            var platform = QueryCommodity.rule.getPlatform(viewTradeDiv);
            QueryCommodity.commodityDiv.itemWirelessPCViewTrade(viewTradeDiv.find('.diag-result-div'), numIid,
                interval, endTime, platform);
        });

        // 点击input文字相当于点击input
        QueryCommodity.Event.setIntervalSpanClickEvent(viewTradeDiv);

        // 设施无线/PC的tab事件
        viewTradeDiv.find('.opTabWrapper .opTab').unbind('click').click(function(){
            if($(this).hasClass("selected")) {
                return;
            }
            viewTradeDiv.find('.opTabWrapper .selected').removeClass("selected");
            $(this).addClass("selected");
            var target = $(this).attr("target");
            var interval = QueryCommodity.rule.getInterval(viewTradeDiv);
            var endTime = QueryCommodity.rule.getEndTime(viewTradeDiv);
            switch (target){
                case "shop" :
                    QueryCommodity.commodityDiv.itemWirelessPCViewTrade(viewTradeDiv.find('.diag-result-div'), numIid,
                        interval, endTime, 0);
                    break;
                case "pc" :
                    QueryCommodity.commodityDiv.itemWirelessPCViewTrade(viewTradeDiv.find('.diag-result-div'), numIid,
                        interval, endTime, 1);
                    break;
                case "wireless" :
                    QueryCommodity.commodityDiv.itemWirelessPCViewTrade(viewTradeDiv.find('.diag-result-div'), numIid,
                        interval, endTime, 2);
                    break;
                default :
                    QueryCommodity.commodityDiv.itemWirelessPCViewTrade(viewTradeDiv.find('.diag-result-div'), numIid,
                        interval, endTime, 0);
                    break;
            }
        });
        viewTradeDiv.find('.opTabWrapper .opTab').eq(0).trigger("click");
    },
    setItemViewTradeEvent2 : function (viewTradeDiv, numIid, index, initInterval) {
        var dayMillis = 24 * 3600 * 1000;
        var date_range_picker = viewTradeDiv.find("input[drpid='date-range-picker']");
        date_range_picker.unbind('change').change(function () {
            var endTime, interval;
            var startTimeInput, endTimeInput;
            startTimeInput = date_range_picker.val().split(locale.separator)[0];
            endTimeInput = date_range_picker.val().split(locale.separator)[1];

            endTime = parseInt(Date.parse(endTimeInput.toString().replace("-","/")));
            if(endTime > new Date().getTime()) {
                TM.Alert.load("截止时间请勿超过当前时间");
                return;
            }
            var startTime = parseInt(Date.parse(startTimeInput.toString().replace("-","/")));
            if(endTime < startTime) {
                TM.Alert.load("截止时间请勿小于开始时间");
                return;
            }
            interval = Math.floor((endTime - startTime) / dayMillis) + 1;

            var platform = QueryCommodity.rule.getPlatform(viewTradeDiv);
            QueryCommodity.commodityDiv.itemWirelessPCViewTrade(viewTradeDiv.find('.diag-result-div'), numIid,
                interval, endTime, platform);
        });
        date_range_picker.daterangepicker({
            "locale": locale,
            "ranges" : {
                '最近1天': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
                '最近3天': [moment().subtract(3, 'days'), moment().subtract(1, 'days')],
                '最近7天': [moment().subtract(7, 'days'), moment().subtract(1, 'days')],
                '最近14天': [moment().subtract(14, 'days'), moment().subtract(1, 'days')]
            },
            "opens":"right",
            "startDate": moment().subtract(initInterval, 'days'),//默认查询最近(initInterval)天
            "endDate": moment().subtract(1, 'days'),
            "maxDate": moment().subtract(1, 'days')
        })

        // 设施无线/PC的tab事件
        viewTradeDiv.find('.opTabWrapper .opTab').unbind('click').click(function(){
            if($(this).hasClass("selected")) {
                return;
            }
            viewTradeDiv.find('.opTabWrapper .selected').removeClass("selected");
            $(this).addClass("selected");
            var target = $(this).attr("target");
            var interval = QueryCommodity.rule.getInterval2(viewTradeDiv);
            var endTime = QueryCommodity.rule.getEndTime2(viewTradeDiv);
            switch (target){
                case "shop" :
                    QueryCommodity.commodityDiv.itemWirelessPCViewTrade(viewTradeDiv.find('.diag-result-div'), numIid,
                        interval, endTime, 0);
                    break;
                case "pc" :
                    QueryCommodity.commodityDiv.itemWirelessPCViewTrade(viewTradeDiv.find('.diag-result-div'), numIid,
                        interval, endTime, 1);
                    break;
                case "wireless" :
                    QueryCommodity.commodityDiv.itemWirelessPCViewTrade(viewTradeDiv.find('.diag-result-div'), numIid,
                        interval, endTime, 2);
                    break;
                default :
                    QueryCommodity.commodityDiv.itemWirelessPCViewTrade(viewTradeDiv.find('.diag-result-div'), numIid,
                        interval, endTime, 0);
                    break;
            }
        });
    },
    setItemViewEvent: function(itemView, numIid, index){
        var curr = new Date().getTime();
        var dayMillis = 24 * 3600 * 1000;
        var timeZoneDiff = 8 * 3600 * 1000;
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
                    var currDate = new Date();
                    var currTime = parseInt((currDate.getTime() + timeZoneDiff) / dayMillis) * dayMillis - timeZoneDiff;
                    if(endTime == currTime) {
                    	endTime = endTime - dayMillis;
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
    setItemViewEvent2 : function (itemView, numIid, index, initInterval) {
        var dayMillis = 24 * 3600 * 1000;
        var date_range_picker = itemView.find("input[drpid='date-range-picker']");
        date_range_picker.unbind('change').change(function () {
            var endTime, interval;
            var startTimeInput, endTimeInput;
            var curr = new Date().getTime();
            startTimeInput = date_range_picker.val().split(locale.separator)[0];
            endTimeInput = date_range_picker.val().split(locale.separator)[1];

            endTime = parseInt(Date.parse(endTimeInput.toString().replace("-","/")));
            if(endTime > new Date().getTime()) {
                TM.Alert.load("截止时间请勿超过当前时间");
                return;
            }
            var startTime = parseInt(Date.parse(startTimeInput.toString().replace("-","/")));
            if(endTime < startTime) {
                TM.Alert.load("截止时间请勿小于开始时间");
                return;
            }
            interval = Math.floor((endTime - startTime) / dayMillis) + 1;

            QueryCommodity.commodityDiv.itemViewDiag(itemView.find('.diag-result-div'), numIid, interval, endTime);
        });
        date_range_picker.daterangepicker({
            "locale": locale,
            "ranges" : {
                '最近1天': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
                '最近3天': [moment().subtract(3, 'days'), moment().subtract(1, 'days')],
                '最近7天': [moment().subtract(7, 'days'), moment().subtract(1, 'days')],
                '最近14天': [moment().subtract(14, 'days'), moment().subtract(1, 'days')]
            },
            "opens":"right",
            "startDate": moment().subtract(initInterval, 'days'),//默认查询前(initInterval)天
            "endDate": moment().subtract(1, 'days'),
            "maxDate": moment().subtract(1, 'days')
        })
    },
    setSkuDetailEvent: function(skuDetail, numIid, index){
        var curr = new Date().getTime();
        var dayMillis = 24 * 3600 * 1000;
        var timeZoneDiff = 8 * 3600 * 1000;
        var startTimeInput = skuDetail.find(".startTimeInput");
        var endTimeInput = skuDetail.find(".endTimeInput");
        startTimeInput.datepicker();
        startTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
        startTimeInput.val(new Date(curr - 7 * dayMillis).formatYMS());

        endTimeInput.datepicker();
        endTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
        endTimeInput.val(new Date(curr).formatYMS());

        startTimeInput.unbind('change').change(function(){
        	skuDetail.find('.interval-tr .interval[value="0"]').trigger("click");
        });
        endTimeInput.unbind('change').change(function(){
        	skuDetail.find('.interval-tr .interval[value="0"]').trigger("click");
        });

        skuDetail.find('.interval-tr .interval').unbind('click').click(function(){
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
                    var currDate = new Date();
                    var currTime = parseInt((currDate.getTime() + timeZoneDiff) / dayMillis) * dayMillis - timeZoneDiff;
                    if(endTime == currTime) {
                    	endTime = endTime - dayMillis;
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
            QueryCommodity.commodityDiv.skuDetailDiag(skuDetail.find('.diag-result-div'), numIid, interval, endTime);
        });
        // 点击input文字相当于点击input
        QueryCommodity.Event.setIntervalSpanClickEvent(skuDetail);
        skuDetail.find('.interval-tr .interval:checked').trigger("click");
    },
    setSkuDetailEvent2 : function (skuDetail, numIid, index, initInterval) {
        var dayMillis = 24 * 3600 * 1000;
        var date_range_picker = skuDetail.find("input[drpid='date-range-picker']");
        date_range_picker.unbind('change').change(function () {
            var endTime, interval;
            var startTimeInput, endTimeInput;
            startTimeInput = date_range_picker.val().split(locale.separator)[0];
            endTimeInput = date_range_picker.val().split(locale.separator)[1];

            endTime = parseInt(Date.parse(endTimeInput.toString().replace("-","/")));
            if(endTime > new Date().getTime()) {
                TM.Alert.load("截止时间请勿超过当前时间");
                return;
            }
            var startTime = parseInt(Date.parse(startTimeInput.toString().replace("-","/")));
            if(endTime < startTime) {
                TM.Alert.load("截止时间请勿小于开始时间");
                return;
            }
            interval = Math.floor((endTime - startTime) / dayMillis) + 1;

            QueryCommodity.commodityDiv.skuDetailDiag(skuDetail.find('.diag-result-div'), numIid, interval, endTime);
        });
        date_range_picker.daterangepicker({
            "locale": locale,
            "ranges" : {
                '最近1天': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
                '最近3天': [moment().subtract(3, 'days'), moment().subtract(1, 'days')],
                '最近7天': [moment().subtract(7, 'days'), moment().subtract(1, 'days')],
                '最近14天': [moment().subtract(14, 'days'), moment().subtract(1, 'days')]
            },
            "opens":"right",
            "startDate": moment().subtract(initInterval, 'days'),//默认查询最近(initInterval)天
            "endDate": moment().subtract(1, 'days'),
            "maxDate": moment().subtract(1, 'days')
        })
    },
    setRelitiveAccessEvent : function(relativeAccessDiv, numIid, index){
        var curr = new Date().getTime();
        var dayMillis = 24 * 3600 * 1000;
        var startTimeInput = relativeAccessDiv.find(".startTimeInput");
        var endTimeInput = relativeAccessDiv.find(".endTimeInput");
        startTimeInput.datepicker();
        startTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
        startTimeInput.val(new Date(curr - 7 * dayMillis).formatYMS());

        endTimeInput.datepicker();
        endTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
        endTimeInput.val(new Date(curr).formatYMS());

        startTimeInput.unbind('change').change(function(){
            relativeAccessDiv.find('.interval-tr .interval[value="0"]').trigger("click");
        });
        endTimeInput.unbind('change').change(function(){
            relativeAccessDiv.find('.interval-tr .interval[value="0"]').trigger("click");
        });

        relativeAccessDiv.find('.interval-tr .interval').unbind('click').click(function(){
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
            QueryCommodity.commodityDiv.relativeAccessDiag(relativeAccessDiv.find('.diag-result-div'), numIid,
                interval, endTime);
        });

        // 点击input文字相当于点击input
        QueryCommodity.Event.setIntervalSpanClickEvent(relativeAccessDiv);

        relativeAccessDiv.find('.interval-tr .interval:checked').trigger("click");
    },
    setRelitiveAccessEvent2 : function (relativeAccessDiv, numIid, index, initInterval) {
        var dayMillis = 24 * 3600 * 1000;
        var date_range_picker = relativeAccessDiv.find("input[drpid='date-range-picker']");
        date_range_picker.unbind('change').change(function () {
            var endTime, interval;
            var startTimeInput, endTimeInput;
            var curr = new Date().getTime();
            startTimeInput = date_range_picker.val().split(locale.separator)[0];
            endTimeInput = date_range_picker.val().split(locale.separator)[1];

            endTime = parseInt(Date.parse(endTimeInput.toString().replace("-","/")));
            if(endTime > new Date().getTime()) {
                TM.Alert.load("截止时间请勿超过当前时间");
                return;
            }
            var startTime = parseInt(Date.parse(startTimeInput.toString().replace("-","/")));
            if(endTime < startTime) {
                TM.Alert.load("截止时间请勿小于开始时间");
                return;
            }
            interval = Math.floor((endTime - startTime) / dayMillis) + 1;

            QueryCommodity.commodityDiv.relativeAccessDiag(relativeAccessDiv.find('.diag-result-div'), numIid,
                interval, endTime);
        });
        date_range_picker.daterangepicker({
            "locale": locale,
            "ranges" : {
                '最近1天': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
                '最近3天': [moment().subtract(3, 'days'), moment().subtract(1, 'days')],
                '最近7天': [moment().subtract(7, 'days'), moment().subtract(1, 'days')],
                '最近14天': [moment().subtract(14, 'days'), moment().subtract(1, 'days')]
            },
            "opens":"right",
            "startDate": moment().subtract(initInterval, 'days'),//默认查询最近(initInterval)天
            "endDate": moment().subtract(1, 'days'),
            "maxDate": moment().subtract(1, 'days')
        })
    },
    setComeInWordsDivEvent : function(comeInWords, numIid, index, isApp) {
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
    setComeInWordsDivEvent2 : function (comeInWords, numIid, index, isApp, initInterval) {
        var dayMillis = 24 * 3600 * 1000;
        var date_range_picker = comeInWords.find("input[drpid='date-range-picker']");
        date_range_picker.unbind('change').change(function () {
            var endTime, interval;
            var startTimeInput, endTimeInput;
            startTimeInput = date_range_picker.val().split(locale.separator)[0];
            endTimeInput = date_range_picker.val().split(locale.separator)[1];

            endTime = parseInt(Date.parse(endTimeInput.toString().replace("-","/")));
            if(endTime > new Date().getTime()) {
                TM.Alert.load("截止时间请勿超过当前时间");
                return;
            }
            var startTime = parseInt(Date.parse(startTimeInput.toString().replace("-","/")));
            if(endTime < startTime) {
                TM.Alert.load("截止时间请勿小于开始时间");
                return;
            }
            interval = Math.floor((endTime - startTime) / dayMillis) + 1;

            if(isApp == false) {
                QueryCommodity.commodityDiv.itemWordDiag(comeInWords.find('.diag-result-div'), numIid,
                    interval, endTime);
            } else {
                QueryCommodity.commodityDiv.appItemWordDiag(comeInWords.find('.app-diag-result-div'), numIid,
                    interval, endTime);
            }
        });
        date_range_picker.daterangepicker({
            "locale": locale,
            "ranges" : {
                '最近1天': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
                '最近3天': [moment().subtract(3, 'days'), moment().subtract(1, 'days')],
                '最近7天': [moment().subtract(7, 'days'), moment().subtract(1, 'days')],
                '最近14天': [moment().subtract(14, 'days'), moment().subtract(1, 'days')]
            },
            "opens":"right",
            "startDate": moment().subtract(initInterval, 'days'),//默认查询最近(initInterval)天
            "endDate": moment().subtract(1, 'days'),
            "maxDate": moment().subtract(1, 'days')
        })
    },
    setWirelessSrcUvPvDivEvent: function(wirelessSrcUvPv, numIid, index){
        var curr = new Date().getTime();
        var dayMillis = 24 * 3600 * 1000;
        var startTimeInput = wirelessSrcUvPv.find(".startTimeInput");
        var endTimeInput = wirelessSrcUvPv.find(".endTimeInput");
        startTimeInput.datepicker();
        startTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
        startTimeInput.val(new Date(curr - 7 * dayMillis).formatYMS());

        endTimeInput.datepicker();
        endTimeInput.datepicker('option', 'dateFormat','yy-mm-dd');
        endTimeInput.val(new Date(curr).formatYMS());

        startTimeInput.unbind('change').change(function(){
            wirelessSrcUvPv.find('.interval-tr .interval[value="0"]').trigger("click");
        });
        endTimeInput.unbind('change').change(function(){
            wirelessSrcUvPv.find('.interval-tr .interval[value="0"]').trigger("click");
        });

        wirelessSrcUvPv.find('.interval-tr .interval').unbind('click').click(function(){
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
            QueryCommodity.commodityDiv.wirelessSrcUvPvDiag(wirelessSrcUvPv.find('.diag-result-div'), numIid, interval, endTime);
        });
        // 点击input文字相当于点击input
        QueryCommodity.Event.setIntervalSpanClickEvent(wirelessSrcUvPv);
        wirelessSrcUvPv.find('.interval-tr .interval:checked').trigger("click");
    },
    setWirelessSrcUvPvDivEvent2: function(wirelessSrcUvPv, numIid, index, initInterval){
        var dayMillis = 24 * 3600 * 1000;
        var date_range_picker = wirelessSrcUvPv.find("input[drpid='date-range-picker']");
        date_range_picker.unbind('change').change(function () {
            var endTime, interval;
            var startTimeInput, endTimeInput;
            var curr = new Date().getTime();
            startTimeInput = date_range_picker.val().split(locale.separator)[0];
            endTimeInput = date_range_picker.val().split(locale.separator)[1];

            endTime = parseInt(Date.parse(endTimeInput.toString().replace("-","/")));
            if(endTime > new Date().getTime()) {
                TM.Alert.load("截止时间请勿超过当前时间");
                return;
            }
            var startTime = parseInt(Date.parse(startTimeInput.toString().replace("-","/")));
            if(endTime < startTime) {
                TM.Alert.load("截止时间请勿小于开始时间");
                return;
            }
            interval = Math.floor((endTime - startTime) / dayMillis) + 1;

            QueryCommodity.commodityDiv.wirelessSrcUvPvDiag(wirelessSrcUvPv.find('.diag-result-div'), numIid, interval, endTime);
        });
        date_range_picker.daterangepicker({
            "locale": locale,
            "ranges" : {
                '最近1天': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
                '最近3天': [moment().subtract(3, 'days'), moment().subtract(1, 'days')],
                '最近7天': [moment().subtract(7, 'days'), moment().subtract(1, 'days')],
                '最近14天': [moment().subtract(14, 'days'), moment().subtract(1, 'days')]
            },
            "opens":"right",
            "startDate": moment().subtract(initInterval, 'days'),//默认查询最近(initInterval)天
            "endDate": moment().subtract(1, 'days'),
            "maxDate": moment().subtract(1, 'days')
        })
    },
    setSrcUvPvDivEvent : function(srcUvPv, numIid, index) {
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
    setSrcUvPvDivEvent2 : function(srcUvPv, numIid, index, initInterval) {
        var dayMillis = 24 * 3600 * 1000;
        var date_range_picker = srcUvPv.find("input[drpid='date-range-picker']");
        date_range_picker.unbind('change').change(function () {
            var endTime, interval;
            var startTimeInput, endTimeInput;
            var curr = new Date().getTime();
            startTimeInput = date_range_picker.val().split(locale.separator)[0];
            endTimeInput = date_range_picker.val().split(locale.separator)[1];

            endTime = parseInt(Date.parse(endTimeInput.toString().replace("-","/")));
            if(endTime > new Date().getTime()) {
                TM.Alert.load("截止时间请勿超过当前时间");
                return;
            }
            var startTime = parseInt(Date.parse(startTimeInput.toString().replace("-","/")));
            if(endTime < startTime) {
                TM.Alert.load("截止时间请勿小于开始时间");
                return;
            }
            interval = Math.floor((endTime - startTime) / dayMillis) + 1;

            QueryCommodity.commodityDiv.itemSrcUvPvDiag(srcUvPv.find('.diag-result-div'), numIid,
                interval, endTime);
        });
        date_range_picker.daterangepicker({
            "locale": locale,
            "ranges" : {
                '最近1天': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
                '最近3天': [moment().subtract(3, 'days'), moment().subtract(1, 'days')],
                '最近7天': [moment().subtract(7, 'days'), moment().subtract(1, 'days')],
                '最近14天': [moment().subtract(14, 'days'), moment().subtract(1, 'days')]
            },
            "opens":"right",
            "startDate": moment().subtract(initInterval, 'days'),//默认查询最近(initInterval)天
            "endDate": moment().subtract(1, 'days'),
            "maxDate": moment().subtract(1, 'days')
        })
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
    sort:0,
    order:0,
    isDesc:false,
    itemStatus:0,     //默认全部
    isOptimised:1,    //默认全部
    getRuleJson: function() {
        var ruleJson = {
            pn: 1,
            ps: 10
        };

        var searchText = $("#searchText").val();
        if (searchText != null && searchText != "") {
            ruleJson.searchText = searchText;
        } else {
            ruleJson.searchText == "";
        }
        
        var numIid = $("#numIid").val();
        if (numIid != null && numIid != "") {
            ruleJson.numIid = numIid;
        } else {
            ruleJson.numIid == "";
        }
        
        return ruleJson;
    },
    clearCookie : function(){
        $.cookie("isCatSearch",null);
    },
    getPlatform : function(tarfetDiv){
        var target = tarfetDiv.find('.shop-pc-online-info .opTabWrapper .selected').attr("target");
        switch (target){
            case "shop" :
                return 0;
            case "pc" :
                return 1;
            case "wireless" :
                return 2;
            default :
                return 0;
        }
    },
    getInterval : function(tarfetDiv){
        var value = parseInt(tarfetDiv.find('.interval-tr .interval:checked').val());
        var dayMillis = 24 * 3600 * 1000;
        switch (value) {
            case 1 :
                return 1;
            case 3 :
                return 3;
            case 7 :
                return 7;
            case 14 :
                return 14;
            case 0 :
                var endTimeInput = tarfetDiv.find(".endTimeInput");
                var startTimeInput = tarfetDiv.find(".startTimeInput");
                var curr = new Date().getTime();
                var endTime = parseInt(new Date(Date.parse(endTimeInput.val())).getTime());
                if(endTime > new Date().getTime()) {
                    TM.Alert.load("截止时间请勿超过当前时间");
                    endTimeInput.val(new Date(curr).formatYMS());
                    return;
                }
                var startTime = parseInt(new Date(Date.parse(startTimeInput.val())).getTime());
                if(endTime < startTime) {
                    TM.Alert.load("截止时间请勿小于开始时间");
                    return;
                }
                return Math.floor((endTime - startTime) / dayMillis) + 1;
            default :
                return 7;
        }
    },
    getInterval2 : function (container) {
        var dayMillis = 24 * 3600 * 1000;
        var date_range_picker = container.find("input[drpid='date-range-picker']");
        var endTime, interval;
        var startTimeInput, endTimeInput;
        startTimeInput = date_range_picker.val().split(locale.separator)[0];
        endTimeInput = date_range_picker.val().split(locale.separator)[1];
        endTime = parseInt(Date.parse(endTimeInput.toString().replace("-","/")));

        if(endTime > new Date().getTime()) {
            TM.Alert.load("截止时间请勿超过当前时间");
            return;
        }
        var startTime = parseInt(Date.parse(startTimeInput.toString().replace("-","/")));
        if(endTime < startTime) {
            TM.Alert.load("截止时间请勿小于开始时间");
            return;
        }
        interval = Math.floor((endTime - startTime) / dayMillis) + 1;

        return interval;
    },
    getEndTime : function(tarfetDiv){
        var value = parseInt(tarfetDiv.find('.interval-tr .interval:checked').val());
        var curr = new Date().getTime();
        var dayMills = 24 * 3600 * 1000;
        switch (value) {
            case 1 :
                return curr - dayMills;
            case 3 :
                return curr - dayMills;
            case 7 :
                return curr - dayMills;
            case 14 :
                return curr - dayMills;
            case 0 :
                var endTimeInput = tarfetDiv.find(".endTimeInput");
                var endTime = parseInt(new Date(Date.parse(endTimeInput.val())).getTime());
                if(endTime > new Date().getTime()) {
                    return curr;
                } else {
                    return endTime;
                }
            default :
                return curr - dayMills;
        }
    },
    getEndTime2 : function (container) {
        var dayMillis = 24 * 3600 * 1000;
        var date_range_picker = container.find("input[drpid='date-range-picker']");
        var endTime;
        var startTimeInput, endTimeInput;
        startTimeInput = date_range_picker.val().split(locale.separator)[0];
        endTimeInput = date_range_picker.val().split(locale.separator)[1];

        endTime = parseInt(Date.parse(endTimeInput.toString().replace("-","/")));
        if(endTime > new Date().getTime()) {
            TM.Alert.load("截止时间请勿超过当前时间");
            return;
        }
        var startTime = parseInt(Date.parse(startTimeInput.toString().replace("-","/")));
        if(endTime < startTime) {
            TM.Alert.load("截止时间请勿小于开始时间");
            return;
        }

        return endTime;
    },
    getDaysBetween : function(){
    	var hourMills = 3600 * 1000;
    	var dayMills = 24 * 3600 * 1000;
    	var timeZoneDiff = 8 * hourMills;
        var nowTime = new Date().getTime();
        var firstLoginTime = parseInt(TM.firstLoginTime);
        var limitTime = parseInt(((firstLoginTime + timeZoneDiff) / dayMills)) * dayMills  - timeZoneDiff + dayMills + timeZoneDiff;
        
        var interval = parseInt((nowTime - limitTime) / dayMills) + 1;

        return interval;
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

QueryCommodity.init = function(currentPage,params) {
    var interval = QueryCommodity.rule.getDaysBetween();
    
    if(interval < 1) {
       $('.searchDiagTip').show();
    }

    QueryCommodity.rule.clearCookie();
    var searchBtn = $('#searchBtn');
    QueryCommodity.rule.itemStatus=params.status;

    $('#searchText').keydown(function() {
        if (event.keyCode == "13") {//keyCode=13是回车键
            searchBtn.click();
        }
    });
    QueryCommodity.Event.setCommsEvent2();

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
        
        if($('#sort option:selected').attr("tag")=="recentSalesCount"){
            QueryCommodity.rule.sort= 0;
        } else if($('#sort option:selected').attr("tag")=="created") {
            QueryCommodity.rule.sort= 1;
        } else {
            QueryCommodity.rule.sort= 0;
        }

        var date_range_picker = $(".commsDiv input[drpid='date-range-picker']");
        date_range_picker.trigger("change");
    });
    
	$(".sortTh").click(function() {
		if ($(this).find(".sort").hasClass("Desc")) {
			$(".sort").removeClass("current-sort");
			$(this).find(".sort").removeClass("Desc");
			$(this).find(".sort").addClass("Asc");
			$(this).find(".sort").addClass("current-sort");
		} else {
			$(".sort").removeClass("current-sort");
			$(this).find(".sort").removeClass("Asc");
			$(this).find(".sort").addClass("Desc");
			$(this).find(".sort").addClass("current-sort");
		}
		
		var sortObj = $(".item-table").find(".current-sort");
		if (sortObj.length != 0) {
			QueryCommodity.rule.order = sortObj.attr("order");
			if (sortObj.hasClass("Desc"))
				QueryCommodity.rule.isDesc = true;
			else {
				QueryCommodity.rule.isDesc = false;
			}
		}
		
		searchBtn.trigger('click');
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
	
	searchBtn.trigger('click');
	
};

