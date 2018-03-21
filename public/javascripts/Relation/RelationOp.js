$(document).ready(function() {
    TM.gcs('/js/jquery-ui-1.9.1.custom.min.js');
    TM.gcs('/js/tm.js',function(){
            TM.gcs('/home/firstSync',function(){
                RelationOp.init.doInit();
            })
        })

//    });


/**
 * 关联
 * @type {*}
 */
var RelationOp = RelationOp || {};

RelationOp.init = RelationOp.init || {};

RelationOp.init = $.extend({
    doInit: function() {
        RelationOp.container =$(".relationOpContainer");
        RelationOp.query.doQuery();
        RelationOp.event.setEvent();
    }
}, RelationOp.init);

RelationOp.event = RelationOp.event || {};

RelationOp.event = $.extend({
    setEvent: function() {
        $(".titleSearchBtn").click(function() {
            var title = $(".titleSearchText").val();
            RelationOp.query.ruleJson.title = title;
            var state = $(".relationState").val();
            RelationOp.query.ruleJson.relatedState = state;

            RelationOp.query.doQuery();
        });

        $(".titleSearchText").keydown(function(event) {
            if (event.keyCode == 13) {//按回车
                $(".titleSearchBtn").click();
            }
        });

        RelationOp.event.checkBoxEvent();
        $('#relateAllItemsBtn').click(function(){
            RelationOp.submit.relateAllItems();
        });
        
        $('#removeAllItemsRelationBtn').click(function() {
        	RelationOp.submit.removeAllItemsRelation();
        });


        $("#closeImg").click(function(){
            if($("#warmNotice ol").css("display")=="block")
            {
//            $("#warmNotice ol").css("display","none");
                $("#warmNotice ol").slideUp('normal');
                $("#closeImg").attr("title", "打开功能介绍");
                $("#closeImg").attr("src","http://img02.taobaocdn.com/imgextra/i2/1039626382/T2S5O1XoJXXXXXXXXX_!!1039626382.gif");
            }
            else if($("#warmNotice ol").css("display")=="none")
            {
//            $("#warmNotice ol").css("display","block");
                $("#warmNotice ol").slideDown('normal');
                $("#closeImg").attr("title", "关闭功能介绍");
                $("#closeImg").attr("src","http://img03.taobaocdn.com/imgextra/i3/1039626382/T2No11XjNXXXXXXXXX_!!1039626382.gif");
            }
        });

        $(".relateSelectItemBtn").click(function() {
            var itemIdArr = [];
            var selectObjs = $(".oneItemCheck:checked");
            if (selectObjs.length == 0) {
                alert("请先选择要添加关联的宝贝");
                return;
            }
            selectObjs.each(function() {
                itemIdArr[itemIdArr.length] = $(this).attr("numIid");
            });
            RelationOp.submit.addRelation(itemIdArr);
        });
        $(".deRelateSelectItemBtn").click(function() {
            if (confirm("确定取消选中宝贝的关联") == false)
                return;
            var itemIdArr = [];
            var selectObjs = $(".oneItemCheck:checked");
            if (selectObjs.length == 0) {
                alert("请先选择要取消关联的宝贝");
                return;
            }
            selectObjs.each(function() {
                itemIdArr[itemIdArr.length] = $(this).attr("numIid");
            });
            RelationOp.submit.removeRelation(itemIdArr);
        });
        $.get("/items/sellerCatCount",function(data){
            var sellerCat = $('#sellerCat');
            sellerCat.empty();

            var exist = false;
            var cat = $('<option>所有分类</option>');
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
        });
        $.get("/items/itemCatCount",function(data){
            var sellerCat = $('#itemCat');
            sellerCat.empty();
            var exist = false;
            var cat = $('<option>所有分类</option>');
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
        });
        //
    },
    //勾选框
    checkBoxEvent: function() {
        $(".selectAll").click(function() {
            RelationOp.event.checkAllSeclect();

        });
        $(".selectAllSpan").click(function() {
        	var checkObj = $(".selectAll");
        	var isChecked = checkObj.is(':checked');
        	if (isChecked == true) {
        		checkObj.attr("checked", false);
        	} else {
        		checkObj.attr("checked", true);
        	}
        	RelationOp.event.checkAllSeclect();
        });
    },
    checkAllSeclect: function() {
    	var checkObj = $(".selectAll");
    	var isChecked = checkObj.is(':checked');
        if (isChecked == true) {
            $(".itemTable").find(".oneItemCheck").attr("checked", true);
            $(".itemTable").find(".unSelectedTr").addClass("selectedTr");
            $(".itemTable").find(".unSelectedTr").removeClass("unSelectedTr");
        } else {
            $(".itemTable").find(".oneItemCheck").attr("checked", false);
            $(".itemTable").find(".selectedTr").addClass("unSelectedTr");
            $(".itemTable").find(".selectedTr").removeClass("selectedTr");
        }
    }
}, RelationOp.event);

/**
 * 分页查询
 * @type {*}
 */
RelationOp.show = $.extend({
    currentPage: 1,
    doShow: function(currentPage) {
        var ruleData = RelationOp.show.getQueryRule();
        var itemTbodyObj = RelationOp.container.find(".items-container");
        itemTbodyObj.html("");
        //alert(currentPage);
        if (currentPage === undefined || currentPage == null || currentPage <= 0)
            currentPage = 1;
        RelationOp.container.find(".paging-div").tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount: 1,
            ajax: {
                on: true,
                param: ruleData,
                dataType: 'json',
                url: '/PaiPaiOrderForm/queryOrders',
                callback: function(dataJson){
                    RelationOp.show.currentPage = dataJson.pn;//记录当前页
                    itemTbodyObj.html("");
                    var itemArray = dataJson.res;
                    //SkinBatch.container.find(".select-all-item").attr("checked", true);
                    $(itemArray).each(function(index, itemJson) {
                        var trObj = RelationOp.row.createRow(index, itemJson);
                        itemTbodyObj.append(trObj);
                    });
                }
            }

        });
    },
    refresh: function() {
        RelationOp.show.doShow(RelationOp.show.currentPage);
        RelationOp.container.find(".error-item-div").hide();
    }
}, RelationOp.show);

    /**
     * 显示一行宝贝
     * @type {*}
     */
    RelationOp.row = RelationOp.row || {};
    RelationOp.row = $.extend({
        createRow: function(index, itemJson) {
            var html1 = '' +
                '<table class="item-table list-table busSearch">' +
                '   <tbody>' +
                '   <tr>' +
                '       <td style="background-color: #ebf3ff;"><input type="checkbox" class="item-checkbox width17" /></td>' +
                '       <td class="commodity_number" colspan="9" >' +
                '           <span class="body-dealCode"></span>' +
                '           <span class="body-createTime"></span>' +
                '       </td>' +
                '   </tr>' +
                '   </tbody>' +
                '</table>' +
                '';
            var html2 =''+
                '   <tr>' +
                '       <td  style="width: 3%;">' +
                '       <td  style="width: 7%;">' +
                '           <span class="pic"><a class="item-link" target="_blank"><img style="width: 60px;height: 60px;" class="body-itemImg" /></a></span>' +
                '       </td>' +
                '       <td  style="width: 20%;">' +
                '           <span class="name"><a class="item-link" target="_blank"><span class="body-itemName" style="color:#014CCC;"></span></a> </span>' +
                '       </td>' +
                '       <td style="width: 10%;">' +
                '           <div class="body-itemDealPrice"></div>' +
                '           <div class="body-itemDealCount"></div>' +
                '       </td>' +
                '       <td style="width: 5%;"><a class="position-link" target="_blank"><img class="body-itemposition" /></td>' +
                '       <td style="width: 15%;border: 1px solid #ccc" class="commodity_price">' +
                '            <div class="body-totalCash"></div> ' +
                '            <div class="body-freight"></div>' +
                '       </td>' +
                '       <td style="width: 15%;border: 1px solid #ccc" class="commodity_buyer">' +
                '            <div class="body-buyerName"></div> ' +
                '            <div class="body-buyerUin"></div>' +
                '          <div class="hover-tips"><span class="hover_tips_ztb"></span></div>' +
                '            <div><a class="body-information" target="_blank"><img class="body-informationImg" /></div>' +
                '       </td>' +
                '       <td style="width: 15%;border: 1px solid #ccc" class="commodity_states">' +
                '            <div><span><a class="order-link" target="_blank" style="color: #014CCC;">订单详情</a></span></div>' +
                '            <div class="body-dealState"></div>' +
                '            <div class="body-dealRateState"></div>' +
                '       </td>' +
                '       <td style="width: 10%;border: 1px solid #ccc" class="commodity_action"><span style="margin: 5px 0;" class="tmbtn yellow-btn item-modify-price-btn">打印订单</span>' +
                '            <span style="margin: 5px 0;" class="tmbtn short-green-btn evaluate-btn">评价买家</span></td>' +
                '   </tr>' +
                '';
            var html3 =''+
                '   <tr>' +
                '       <td  style="width: 3%;">' +
                '       <td  style="width: 7%;">' +
                '           <span class="pic"><a class="item-link" target="_blank"><img style="width: 60px;height: 60px;" class="body-itemImg" /></a></span>' +
                '       </td>' +
                '       <td  style="width: 20%;">' +
                '           <span class="name"><a class="item-link" target="_blank"><span class="body-itemName" style="color:#014CCC;"></span></a> </span>' +
                '       </td>' +
                '       <td style="width: 10%;">' +
                '           <div class="body-itemDealPrice"></div>' +
                '           <div class="body-itemDealCount"></div>' +
                '       </td>' +
                '       <td style="width: 5%;"><a class="position-link" target="_blank"><img class="body-itemposition" /></td>' +
                '   </tr>' +
                '' ;


            var trObj = $(html1);
            var tradeItemList=itemJson.itemList;
            var itemLength= tradeItemList.length;
            var freight=0;

            $(tradeItemList).each(function(index, tradeItem) {
                freight+=tradeItem.itemDealCount*tradeItem.itemDealPrice;
            });
            freight=(itemJson.totalCash-freight)/100;

            $(tradeItemList).each(function(index, tradeItem) {
                var obj2 = $(html2) ;
                var obj3 = $(html3);
                if(index==0){
                    obj2.find(".body-itemImg").attr("src", tradeItem.picLink);
                    obj2.find(".body-itemName").html(tradeItem.itemName);
                    obj2.find(".body-itemDealPrice").html("￥"+tradeItem.itemDealPrice/100+"元");
                    obj2.find(".body-itemDealCount").html("("+tradeItem.itemDealCount+"件)");
                    obj2.find(".body-itemposition").attr("src", "/public/images/dazhe/position.gif");
                    obj2.find(".position-link").attr("href","http://my.paipai.com/cgi-bin/mypaipai/selling?itemid="+tradeItem.itemCode);
                    obj2.find(".body-totalCash").html("￥"+itemJson.totalCash/100+"元");
                    obj2.find(".body-freight").html("(邮费:"+freight+"元)");
                    obj2.find(".body-buyerName").html(itemJson.buyerName);
                    obj2.find(".body-buyerUin").html("(Q:"+itemJson.buyerUin+")");
                    obj2.find(".body-informationImg").attr("src", "/public/images/dazhe/icon_trade.gif");
                    obj2.find(".order-link").attr("href","http://pay.paipai.com/cgi-bin/deal_detail/view?deal_id="+itemJson.dealCode);
                    var  dealState=   RelationOp.row.checkDealState(itemJson.dealState);
                    var  dealRateState=   RelationOp.row.checkDealRateState(itemJson.dealRateState);
                    obj2.find(".body-dealState").html(dealState);
                    obj2.find(".body-dealRateState").html(dealRateState);
                    obj2.find(".commodity_price").attr("rowspan",itemLength );
                    obj2.find(".commodity_buyer").attr("rowspan",itemLength );
                    obj2.find(".commodity_states").attr("rowspan",itemLength );
                    obj2.find(".commodity_action").attr("rowspan",itemLength );
                    obj2.find(".item-link").attr("href", "http://auction1.paipai.com/"+tradeItem.itemCode);
                    trObj.find("tbody").append(obj2);
                }
                else{
                    obj3.find(".body-itemImg").attr("src", tradeItem.picLink);
                    obj3.find(".body-itemName").html(tradeItem.itemName);
                    obj3.find(".body-itemDealPrice").html("￥"+tradeItem.itemDealPrice/100+"元");
                    obj3.find(".body-itemDealCount").html("("+tradeItem.itemDealCount+"件)");
                    obj3.find(".body-itemposition").attr("src", "/public/images/dazhe/position.gif");
                    obj3.find(".position-link").attr("href","http://my.paipai.com/cgi-bin/mypaipai/selling?itemid="+tradeItem.itemCode);
                    obj3.find(".item-link").attr("href", "http://auction1.paipai.com/"+tradeItem.itemCode);
                    trObj.find("tbody").append(obj3);
                }
            });

            trObj.find(".body-dealCode").html(" 订单编号: "+itemJson.dealCode);
            trObj.find(".body-createTime").html(" 下单时间: "+RelationOp.row.parseLongToDate(itemJson.createTime));

            trObj.find(".item-checkbox").attr("dealCode", itemJson.dealCode);
            trObj.find(".item-checkbox").each(function() {
                this.itemJson = itemJson;
            });

            //添加事件
            trObj.find(".body-informationImg").mouseover(function(){
                var html4=''+
                    '<div class="hover_tips" style="top: 477px; left: 735px; width: 250px; " id="hoverTips">' +
                    '<span class="hover_tips_ztb"></span>' +
                    '<div class="hover_tips_cont" id="hoverTipContent">' +
                    '   <p>买家姓名：'+itemJson.receiverName+'</p>' +
                    '   <p>手机：'+itemJson.receiverMobile+'</p>' +
                    '   <p>电话：'+itemJson.receiverPhone+'</p>' +
                    '   <p style="padding-left:5em;">' +
                    '       <span style="margin-left:-5em;">收货信息：</span>' +itemJson.receiverName+","+itemJson.receiverMobile+","+itemJson.receiverAddress+
                    '   </p>' +
                    '</div>' +
                    '</div>' +
                    '';
                trObj.find('.hover-tips').html(html4);
                trObj.find('.hover-tips').show();
            });
            trObj.find(".body-informationImg").mouseout(function(){
                trObj.find('.hover-tips').hide();
            });

            trObj.find(".evaluate-btn").click(function() {
                var itemCheckObj = $(this).parents("tbody").find(".item-checkbox");
                var dealCode = itemCheckObj.attr("dealCode");
                var numIidArray = [];
                numIidArray[numIidArray.length] = dealCode;
                var itemJson = null;
                itemCheckObj.each(function() {
                    itemJson = this.itemJson;
                });

                RelationOp.submit.modifyPriceParam.numIidList = numIidArray;
                RelationOp.submit.modifyPriceParam.itemJson = itemJson;
                RelationOp.submit.doEvaluate();
            });

            var checkCallback = function(checkObj) {
                var isChecked = checkObj.is(":checked");
                if (isChecked == false) {
                    RelationOp.container.find(".select-all-item").attr("checked", false);
                } else {
                    var checkObjs = RelationOp.container.find(".item-checkbox");
                    var flag = true;
                    checkObjs.each(function() {
                        if ($(this).is(":checked") == false)
                            flag = false;
                    });
                    RelationOp.container.find(".select-all-item").attr("checked", flag);
                }
            }
            trObj.find(".item-checkbox").click(function() {
                checkCallback($(this));
            });

            return trObj;

        } ,
        parseLongToDate:function(ts) {
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
            var timeStr =year+"-"+ month+"-"+date+" "+hour+":"+minutes+ ":" + second;
            return timeStr;
        }
    }, RelationOp.row);

RelationOp.query = RelationOp.query || {};

RelationOp.query = $.extend({
    ruleJson: {
        title: '',
        relatedState: 'all'
    },
    doQuery: function() {
        $(".itemListBlock .itemListDiv").html("");

        var ruleJson = RelationOp.query.ruleJson;
        var opt = {
            countUrl: "/relation/getItemCount",
            dataUrl: "/relation/getItemList",
            pageSize: 5,
            getRuleData: function(isCurrentPage, currentPage) {//isCurrentPage判断是否需要当前页的条件
                var data = {};
                var queryRule = {};
                queryRule.currentPage = currentPage;
                queryRule.title = ruleJson.title;
                queryRule.pageSize = 5;
                queryRule.relationState = ruleJson.relatedState;
                data.queryRule = queryRule;
                return data;
            },
            parseTotalCount: function(resultJson) {
                return resultJson;
            },
            queryCallback: function(dataJson) {
                RelationOp.showItems.show(dataJson);
                RelationOp.showItems.buildRelated();
            }
        };
        $("#itemPaging").setPaging(opt);
    }
}, RelationOp.query);


RelationOp.showItems = RelationOp.showItems || {};

RelationOp.showItems = $.extend({
    buildRelated: function() {
        $('.relatedLoadWaiting').each(function(i, elem){
            var obj = $(elem);
            var numIid = obj.attr('numiid');
            $.get('/Relation/getRelatedRecommends',{numIid: numIid},function(data){

                obj.empty();
                var imgs = [];
                imgs.push('<div class="relatedimgsnap">')
                $.each(data, function(i, item){
                    imgs.push('<span><img style="" src="'+item.picURL+'" ></span>');
                });
                imgs.push('</div>');
                $(imgs.join('')).appendTo(obj);
            });
        });
    },
    isRelated: function(itemJson) {
        //alert(itemJson.type);
        if ((itemJson.type & 2) == 0) {
            return false;
        } else {
            return true;
        }
    },
    show: function(itemArray) {
        $(".itemListBlock .itemListDiv").html("");
        $(".selectAll").attr("checked", false);
        $(itemArray).each(function(index, itemJson) {
            var tableObj = $("#tableTemplate").eq(0).clone(true);
            tableObj.removeAttr("id");//去掉id属性
            tableObj.addClass("itemTable");
            RelationOp.showItems.setCheckTd(tableObj, itemJson);
            RelationOp.showItems.setImgTd(tableObj, itemJson);
            RelationOp.showItems.setTitleTd(tableObj, itemJson);
            RelationOp.showItems.setStateTd(tableObj, itemJson);
            RelationOp.showItems.setRelationOpTd(tableObj, itemJson);
            tableObj.find(".itemTitle").click(function() {
            	var checkObj = $(this).parents("tr").find(".oneItemCheck");
                checkObj.click();
                RelationOp.showItems.checkSelected(checkObj);
            });
            tableObj.show();
            $(".itemListDiv").append(tableObj);
        });
    },
    checkSelected: function(checkObj) {
    	/*var isChecked = checkObj.is(':checked');
    	if (isChecked == true) {
    		checkObj.attr("checked", false);
    	} else {
    		checkObj.attr("checked", true);
    	}*/
    	var isChecked = checkObj.is(':checked');

        if (isChecked == true) {
            checkObj.parent().parent().addClass("selectedTr");
            checkObj.parent().parent().removeClass("unSelectedTr");
        } else {
            $(".selectAll").attr("checked", false);
            checkObj.parent().parent().addClass("unSelectedTr");
            checkObj.parent().parent().removeClass("selectedTr");
        }
    },
    setCheckTd: function(tableObj, itemJson) {
        var checkObj = tableObj.find(".oneItemCheck");
        checkObj.attr("numIid", itemJson.id);
        checkObj.click(function() {
        	RelationOp.showItems.checkSelected($(this));
        });

    },
    setImgTd: function(tableObj, itemJson) {

        var url = "http://item.taobao.com/item.htm?id=" + itemJson.id;
        var aObj = tableObj.find(".imgTd").find(".goto-item-page");
        aObj.attr("href", url);
        aObj.find(".itemImg").attr("src", itemJson.picURL);
    },
    setTitleTd: function(tableObj, itemJson) {
        var titleTd = tableObj.find(".titleTd");
        titleTd.find(".itemTitle").html(itemJson.name);
        var url = "http://item.taobao.com/item.htm?id=" + itemJson.id;
        titleTd.find(".goto-item-page").attr("href", url);
        if(itemJson.related){
            titleTd.find(".goto-item-page").html("查看关联结果");
        }
        var preview = titleTd.find('.previewbtn');
        if (RelationOp.showItems.isRelated(itemJson) == false) {//未关联
            preview.attr('numiid',itemJson.id);
            preview.click(function(){
                var numIid = $(this).attr('numiid');
                $.get('/Relation/previewRelatedRecommends',{numIid: numIid},function(html){
                    var diag = $('.relationpreview');

                    if(diag.length == 0){
                        diag = $("<div class='relationpreview'></div>");
                        diag.appendTo($('body'));
                    }
                    diag.dialog({
                        modal: true,
                        bgiframe: true,
                        title:"宝贝关联效果预览",
                        height:800,
                        width:760,
                        autoOpen: false,
                        resizable: false,
                        buttons:{'确定':function() {
                            $(this).dialog('close');
                        }}
                    });

                    if(html){
                        diag.html(html);
                    }
                    diag.dialog('open');

                },"html");
            });
        } else {
            preview.hide();
        }

    },
    setStateTd: function(tableObj, itemJson) {
        var stateTd = tableObj.find(".stateTd");
        if (RelationOp.showItems.isRelated(itemJson)) {//已关联
            stateTd.find(".relationNumDiv").html("已关联<span class='relationNum'>6</span>件宝贝");
        } else {
            var numDiv = stateTd.find(".relationNumDiv");
            numDiv.html("尚未关联任何宝贝");
            numDiv.attr('numiid',itemJson.id);
            numDiv.addClass('relatedLoadWaiting');
        }
    },
    setRelationOpTd: function(tableObj, itemJson) {
        var relationOpTd = tableObj.find(".relationOpTd");
        var spanObj = relationOpTd.find('.relationOpBtn');
        spanObj.unbind();
        var itemIdArr = [];
        itemIdArr[0] = itemJson.id;
        if (RelationOp.showItems.isRelated(itemJson)) {//已关联
            spanObj.html("取消关联");
            spanObj.click(function() {
                if (confirm("确定取消该宝贝的关联") == false)
                    return;
                RelationOp.submit.removeRelation(itemIdArr);
            });
        } else {
            spanObj.html("添加关联");
            spanObj.click(function() {
                RelationOp.submit.addRelation(itemIdArr);
            });
        }
    }
}, RelationOp.showItems);

/**
 * 添加关联，或者取消关联
 * @type {*}
 */
RelationOp.submit = RelationOp.submit || {};

RelationOp.submit = $.extend({
    addRelation: function(itemIdArr) {
        if (itemIdArr === undefined || itemIdArr == null || itemIdArr.length == 0)
            return;
        var data = {};
        data.numIidArr = itemIdArr;

        $.ajax({
            url: '/relation/addRelation',
            dataType: 'json',
            type: 'post',
            data: data,
            error: function() {

            },
            success: function (json) {
                //刷新一下
                RelationOp.query.doQuery();
                alert("宝贝关联成功");
            }
        });
    },
    removeRelation: function(itemIdArr) {
        if (itemIdArr === undefined || itemIdArr == null || itemIdArr.length == 0)
            return;
        var data = {};
        Loading.init.show({});
        data.numIidArr = itemIdArr;
        $.ajax({
            url: '/relation/removeRelation',
            dataType: 'json',
            type: 'post',
            data: data,
            error: function() {

            },
            success: function (json) {
                //刷新一下
                RelationOp.query.doQuery();
                Loading.init.hidden();
                //alert("宝贝取消关联成功");
            }
        });
    },
    relateAllItems : function(){
        var alertDiv = TM.Alert.getDom();
        alertDiv.dialog({
            modal: true,
            bgiframe: true,
            height: 200,
            width: 300,
            title : '提示',
            autoOpen: false,
            resizable: false,
            zIndex: 6003,
            buttons:{'确定':function() {
                $(this).dialog('close');
                $.ajax({
                    url : '/Relation/relateAllItems',
                    timeout:8000,
                    type : 'POST',
                    success : function(){
                        //TM.Alert.load('关联成功');
                        alert("全店关联成功");
                        location.reload();
                    }
                });
            },'取消':function(){
                $(this).dialog('close');
            }}
        });

        alertDiv.html("<div>确认关联店铺所有宝贝?</div>");
        alertDiv.dialog('open');
    },
    removeAllItemsRelation: function() {
    	var alertDiv = TM.Alert.getDom();
        alertDiv.dialog({
            modal: true,
            bgiframe: true,
            height: 200,
            width: 300,
            title : '提示',
            autoOpen: false,
            resizable: false,
            zIndex: 6003,
            buttons:{'确定':function() {
                $(this).dialog('close');
                $.ajax({
                    url : '/Relation/removeAllItemRelation',
                    timeout:8000,
                    type : 'POST',
                    success : function(){
                        //TM.Alert.load('关联成功');
                        alert("关联取消成功");
                        location.reload();
                    }
                });
            },'取消':function(){
                $(this).dialog('close');
            }}
        });

        alertDiv.html("<div>确认取消店铺所有宝贝的关联?</div>");
        alertDiv.dialog('open');
    }
}, RelationOp.submit);

});