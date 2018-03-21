
var TM = TM || {};
((function ($, window) {

    TM.SkinBatch = TM.SkinBatch || {};

    var SkinBatch = TM.SkinBatch;

    /**
     * 初始化
     * @type {*}
     */
    SkinBatch.init = SkinBatch.init || {};
    SkinBatch.init = $.extend({
        doInit: function(container) {
            var html = SkinBatch.init.createHtml();
            container.html(html);
            SkinBatch.container = container;

            SkinBatch.init.initSearchDiv();
            SkinBatch.init.initBatchOpDiv();
            SkinBatch.init.initItemContainer();
            //第一次执行
            SkinBatch.show.doShow();
        },
        createHtml: function() {
            var html = '' +
                '<div class="search-container"></div> ' +
                '<div class="batchop-div"></div>' +
                '<div class="paging-div"></div>' +
                '<div class="thead-container"></div> ' +
                '<div class="items-container"></div> ' +
                '<div class="paging-div"></div>' +
                '<div class="batchop-div"></div>' +
                '<div class="error-item-div" style="margin-top: 10px;">' +
                '   <span class="error-tip-span">宝贝操作失败列表：</span> ' +
                '   <table class="error-item-table list-table">' +
                '       <thead>' +
                '       <tr>' +
                '           <td style="width: 40%;">订单号</td>' +
                '           <td style="width: 40%;">失败说明</td>' +
                '           <td style="width: 20%;">操作时间</td>' +
                '       </tr>' +
                '       </thead>' +
                '       <tbody></tbody>' +
                '   </table> ' +
                '</div>' +
                '';

            return html;
        },
        //搜索的div
        initSearchDiv: function() {
            var html = '' +
                '   <table>' +
                '       <tbody>' +
                '       <tr>' +
                '           <td ><span> 收 货 人 ：</span><input type="text" class="buyerName" /> </td>' +
                '           <td style="padding-left: 10px;">' +
                '               <span>订单状态：</span><select class="dealState" style="width: 110px">' +
                '                   <option value="" selected="selected">全部</option>' +
                '                   <optgroup label="财付通交易"></optgroup>' +
                '                   <option value="DS_WAIT_BUYER_PAY">等待买家付款</option>' +
                '                   <option value="DS_WAIT_SELLER_DELIVERY">买家已付款</option>' +
                '                   <option value="DS_WAIT_BUYER_RECEIVE">卖家已发货</option>' +
                '                   <option value="DS_DEAL_END_NORMAL">交易成功</option>' +
                '                   <option value="DS_DEAL_CANCELLED">订单取消</option>' +
                '                   <optgroup label="货到付款交易"></optgroup>' +
                '                   <option value="STATE_COD_WAIT_SHIP">货到付款待发货</option>' +
                '                   <option value="STATE_COD_SHIP_OK" >货到付款已发货</option>' +
                '                   <option value="STATE_COD_SIGN">货到付款已签收</option>' +
                '                   <option value="STATE_COD_REFUSE">货到付款已拒收</option>' +
                '                   <option value="STATE_COD_SUCESS">货到付款已完成</option>' +
                '                   <option value="STATE_COD_CANCEL">货到付款已关闭</option>' +
                '               </select>' +
                '           </td>' +
                '           <td style="padding-left: 10px;"><span>下单时间：</span><input type="text" class="create-time" /><span>到</span> </td>' +
                '           <td ><input type="text" class="now-time" /></td>' +
                '           <td style="padding-left: 20px;"><span class="tmbtn sky-blue-btn trade-sync">同步订单</span></td>' +
                '       </tr>' +
                '       <tr>' +
                '           <td><span>商品名称：</span><input type="text" class="itemName" /> </td>' +
                '           <td style="padding-left: 10px;"><span>评价状态：</span><select class="dealRateState" style="width: 110px">' +
                '                   <option value="" selected="selected">全部</option>' +
                '                   <option value="DEAL_RATE_BUYER_NO_SELLER_NO">双方未评</option>' +
                '                   <option value="DEAL_RATE_BUYER_DONE_SELLER_NO">对方已评，我未评</option>' +
                '                   <option value="DEAL_RATE_BUYER_NO_SELLER_DONE">我已评，对方未评</option>' +
                '                   <option value="DEAL_RATE_BUYER_DONE_SELLER_DONE">双方已评</option>' +
                '           </td>' +
                '           <td style="padding-left: 10px;"><span>订单编号：</span><input type="text" class="dealCode" /> </td>' +
                '           <td style="padding-left: 10px;"><span>QQ号：</span><input type="text" class="buyerUin" /> </td>' +
                '           <td style="padding-left: 20px;">' +
                '               <span class="tmbtn sky-blue-btn search-btn">搜索订单</span> ' +
                '           </td>' +
                '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '';
            SkinBatch.container.find(".search-container").html(html);
            var now=new Date().getTime();
            var weektime=now-7*24*60*60*1000;

            SkinBatch.container.find('.now-time').attr("value",SkinBatch.row.parseLongToDate(now));
            SkinBatch.container.find('.create-time').attr("value",SkinBatch.row.parseLongToDate(weektime));

            //添加事件
            SkinBatch.container.find(".search-btn").click(function() {
                SkinBatch.show.doShow();
            });
            SkinBatch.container.find(".trade-sync").click(function(){
                $.get('/paipaiitems/tradeSync',function(){
                    TM.Alert.load('<p style="font-size:14px">亲，同步成功,点击确定刷新页面</p>',300,230,function(){
                        window.location.reload();
                    });
                });
            });

            $(".create-time").datetimepicker({
            }) ;


        },
        //功能按钮
        initBatchOpDiv: function() {
            var html = '' +
                '   <table>' +
                '       <tbody>' +
                '           <tr>' +
                '               <td><span class="tmbtn long-flat-green-btn batch-evaluate-btn">批量买家好评</span></td>' +
                '               <td><span class="tmbtn long-yellow-btn batch-modify-price-btn">批量打印订单</span></td>' +
                '           </tr>' +
                '       </tbody>' +
                '   </table>' +
                '';
            SkinBatch.container.find(".batchop-div").html(html);

            var getCheckedItems = function() {
                var checkObjs = SkinBatch.container.find(".item-checkbox:checked");
                var numIidArray = [];
                checkObjs.each(function() {
                    numIidArray[numIidArray.length] = $(this).attr("dealCode");
                });
                return numIidArray;
            };
            //添加事件
            SkinBatch.container.find(".batch-modify-price-btn").click(function() {
                alert("对不起!亲，您的版本权限不足！") ;
//                var numIidArray = getCheckedItems();
//                if (numIidArray.length == 0) {
//                    alert("请先选择要修改价格的宝贝");
//                    return;
//                }
//                var checkObjs = SkinBatch.container.find(".item-checkbox:checked");
//                var itemJson = checkObjs[0].itemJson;
//                SkinBatch.submit.modifyPriceParam.numIidList = numIidArray;
//                SkinBatch.submit.modifyPriceParam.itemJson = itemJson;
//                SkinBatch.submit.doModifyPrice();
            });
            SkinBatch.container.find(".batch-evaluate-btn").click(function() {
                var numIidArray = getCheckedItems();
                if (numIidArray.length == 0) {
                    alert("请先选择要评价的订单!");
                    return;
                }
                var checkObjs = SkinBatch.container.find(".item-checkbox:checked");
                var itemJson = checkObjs[0].itemJson;
                SkinBatch.submit.modifyPriceParam.numIidList = numIidArray;
                SkinBatch.submit.modifyPriceParam.itemJson = itemJson;
                SkinBatch.submit.doEvaluate();
            });
        },

        //宝贝表格
        initItemContainer: function() {
            var html = '' +
                '<table class="thead-table list-table">' +
                '   <thead>' +
                '   <tr>' +
                '       <td style="width: 3%;"><input type="checkbox" class="select-all-item width17" /></td>' +
                '       <td style="width: 10%;">商品图片</td>' +
                '       <td style="width: 17%;">商品名称</td>' +
                '       <td style="width: 10%;">价格</td>' +
                '       <td style="width: 5%;">定位</td>' +
                '       <td style="width: 15%;">实付金额</td>' +
                '       <td style="width: 15%;">买家</td>' +
                '       <td style="width: 15%;">订单状态</td>' +
                '       <td style="width: 10%;">操作</td>' +
                '   </tr>' +
                '   </thead>' +
                '   <tbody style="display: none;"></tbody>' +
                '</table>' +
                '';

            SkinBatch.container.find(".thead-container").html(html);

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
     * @type {*}
     */
    SkinBatch.show = SkinBatch.show || {};
    SkinBatch.show.orderData = {
        asc: "asc",
        desc: "desc"
    };
    SkinBatch.show = $.extend({
        currentPage: 1,
        ruleData: {
            orderProp: '',      //排序的属性
            orderType: SkinBatch.show.orderData.asc    //排序的类型，升序还是降序
        },
        doShow: function(currentPage) {
            var ruleData = SkinBatch.show.getQueryRule();
            var itemTbodyObj = SkinBatch.container.find(".items-container");
            itemTbodyObj.html("");
            //alert(currentPage);
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
                    url: '/PaiPaiOrderForm/queryOrders',
                    callback: function(dataJson){
                        SkinBatch.show.currentPage = dataJson.pn;//记录当前页
                        itemTbodyObj.html("");
                        var itemArray = dataJson.res;
                        //SkinBatch.container.find(".select-all-item").attr("checked", true);
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
            SkinBatch.container.find(".error-item-div").hide();
        },
        getQueryRule: function() {
            var ruleData = {};
            var buyerName = SkinBatch.container.find(".buyerName").val();
            var dealState = SkinBatch.container.find(".dealState").val();
            var createTime = SkinBatch.container.find(".create-time").val();
            var itemName = SkinBatch.container.find(".itemName").val();
            var dealRateState= SkinBatch.container.find(".dealRateState").val();
            var dealCode= SkinBatch.container.find(".dealCode").val();
            var buyerUin= SkinBatch.container.find(".buyerUin").val();

            ruleData.buyerName = buyerName;
            ruleData.dealState = dealState;
            ruleData.createTime = createTime;
            ruleData.itemName = itemName;
            ruleData.dealRateState = dealRateState;
            ruleData.dealCode = dealCode;
            ruleData.buyerUin = buyerUin;

            return ruleData;
        }
    }, SkinBatch.show);


    /**
     * 显示一行宝贝
     * @type {*}
     */
    SkinBatch.row = SkinBatch.row || {};
    SkinBatch.row = $.extend({
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
                    var  dealState=   SkinBatch.row.checkDealState(itemJson.dealState);
                    var  dealRateState=   SkinBatch.row.checkDealRateState(itemJson.dealRateState);
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
            trObj.find(".body-createTime").html(" 下单时间: "+SkinBatch.row.parseLongToDate(itemJson.createTime));

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


            trObj.find(".item-modify-price-btn").click(function() {
                alert("对不起!亲，您的版本权限不足！") ;
//                var itemCheckObj = $(this).parents("tr").find(".item-checkbox");
//                var numIid = itemCheckObj.attr("numIid");
//                var numIidArray = [];
//                numIidArray[numIidArray.length] = numIid;
//                var itemJson = null;
//                itemCheckObj.each(function() {
//                    itemJson = this.itemJson;
//                });
//
//                SkinBatch.submit.modifyPriceParam.numIidList = numIidArray;
//                SkinBatch.submit.modifyPriceParam.itemJson = itemJson;
//                SkinBatch.submit.doModifyPrice();
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

                SkinBatch.submit.modifyPriceParam.numIidList = numIidArray;
                SkinBatch.submit.modifyPriceParam.itemJson = itemJson;
                SkinBatch.submit.doEvaluate();
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

        } ,
        checkDealState :function(DealState){
            var dealString="";
            if(DealState=="DS_WAIT_BUYER_PAY") {
                dealString="等待买家付款";
            }
            else if(DealState=="DS_WAIT_SELLER_DELIVERY"){
                dealString="买家已付款";
            }
            else if(DealState=="DS_WAIT_BUYER_RECEIVE"){
                dealString="卖家已发货";
            }
            else if(DealState=="DS_DEAL_END_NORMAL"){
                dealString="交易成功";
            }
            else if(DealState=="DS_DEAL_CANCELLED"){
                dealString="订单取消";
            }
            else if(DealState=="STATE_COD_WAIT_SHIP"){
                dealString="货到付款待发货";
            }
            else if(DealState=="STATE_COD_SHIP_OK"){
                dealString="货到付款已发货";
            }
            else if(DealState=="STATE_COD_SIGN"){
                dealString="货到付款已签收";
            }
            else if(DealState=="STATE_COD_REFUSE"){
                dealString="货到付款已拒收";
            }
            else if(DealState=="STATE_COD_SUCESS"){
                dealString="货到付款已完成";
            }
            else if(DealState=="STATE_COD_CANCEL"){
                dealString="货到付款已关闭";
            }
            else{
                dealString="其他";
            }
            return  dealString;
        },
        checkDealRateState:function(DealRateState){
            var dealString="";
            if(DealRateState=="DEAL_RATE_BUYER_NO_SELLER_NO") {
                dealString="双方未评";
            }
            else if(DealRateState=="DEAL_RATE_BUYER_DONE_SELLER_NO") {
                dealString="对方已评，我未评";
            }
            else if(DealRateState=="DEAL_RATE_BUYER_NO_SELLER_DONE") {
                dealString="我已评，对方未评";
            }
            else if(DealRateState=="DEAL_RATE_BUYER_DONE_SELLER_DONE") {
                dealString="双方已评";
            }
            else if(DealRateState=="DEAL_RATE_NO_EVAL") {
                dealString="评价未到期";
            }
            else if(DealRateState=="DEAL_RATE_DISABLE") {
                dealString="不可评价";
            }
            else{
                dealString="其他";
            }
            return  dealString;
        },
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
    }, SkinBatch.row);

    /**
     * 提交批量操作的功能
     * @type {*}
     */
    SkinBatch.submit = SkinBatch.submit || {};
    SkinBatch.submit = $.extend({
        modifyPriceParam: {
            numIidList: [],
            itemJson: {}
        },
        //评价
        doEvaluate: function() {
            var dialogObj = $(".modify-price-dialog");
            if (dialogObj.length == 0) {
                var html = '' +
                    '<div class="modify-price-dialog">' +
                    '   <div class="direct-modify-price-div modify-div">' +
                    '       <span>请输入评语：</span>' +
                    '       <p></p>' +
                    '       <input style="width: 280px;height: 100px;display: block;" type="text" class="new-evaluate-text" />' +
                    '   </div> ' +
                    '</div>';

                dialogObj = $(html);

                var ajaxSuccessCallback = function(dataJson) {
//                    if (SkinBatch.util.checkAjaxSuccess(dataJson) == false) {
//                        return;
//                    }
                    dialogObj.dialog('close');
                    SkinBatch.error.showErrors(dataJson.res);
                };

                var direcEvaluate = function(numIidArray, itemJson) {
                    var newEvaluate =dialogObj.find(".new-evaluate-text").val();
                    $.cookie('evaluate',newEvaluate);
                    if (newEvaluate === undefined || newEvaluate == null || newEvaluate == "") {
                        alert("请先填写要评价的内容");
                        return;
                    }
                    var confirmStr = "";
                    if (numIidArray.length == 1) {
                        confirmStr = "确定评价订单：" + itemJson.dealCode + "？";
                    } else {
                        confirmStr = "确定要评价" + numIidArray.length + "个订单？";
                    }
                    if (confirm(confirmStr) == false) {
                        return;
                    }
                    var data = {};
                    data.evaluateList = numIidArray;
                    data.evaluatecontent = newEvaluate;

                    $.ajax({
                        url : '/PaiPaiOrderForm/doEvaluate',
                        data : data,
                        type : 'post',
                        success : function(dataJson) {
                            ajaxSuccessCallback(dataJson);
                        }
                    });
                };

                $("body").append(dialogObj);
                dialogObj.dialog({
                    modal: true,
                    bgiframe: true,
                    height:270,
                    width:320,
                    title:'评价买家',
                    autoOpen: false,
                    resizable: false,
                    buttons:{'确定':function() {
                        var numIidArray = SkinBatch.submit.modifyPriceParam.numIidList;
                        var itemJson = SkinBatch.submit.modifyPriceParam.itemJson;
                        var evaluate= dialogObj.find(".new-evaluate-text").val();

                        direcEvaluate(numIidArray, itemJson);
                    },'取消':function(){
                        $(this).dialog('close');
                    }}
                });
            }
            dialogObj.find(".new-evaluate-text").val($.cookie('evaluate'));
            dialogObj.dialog("open");
        }
    }, SkinBatch.submit);

    /**
     * 操作失败的日志
     * @type {*}
     */
    SkinBatch.error = SkinBatch.error || {};
    SkinBatch.error = $.extend({
        showErrors: function(errorJsonArray) {
            if (errorJsonArray === undefined || errorJsonArray == null || errorJsonArray.length == 0){
                alert("操作成功！");
                SkinBatch.show.refresh();
                return ;
            }
            alert("操作有误，请查看最下方的错误列表！");
            SkinBatch.container.find(".error-item-div").show();
            $(errorJsonArray).each(function(index, errorJson) {
                var trObj = SkinBatch.error.createRow(index, errorJson);
                SkinBatch.container.find(".error-item-table").find("tbody").append(trObj);
            });
        },
        createRow: function(index, errorJson) {
            var html = '' +
                '<tr>' +
                '   <td><a class="deal-link" target="_blank"><span class="error-dealCode"></span> </a> </td>' +
                '   <td><span class="error-intro"></span> </td>' +
                '   <td><span class="op-time"></span> </td>' +
                '</tr>' +
                '' +
                '' +
                '';
            var trObj = $(html);
            var url = "http://pay.paipai.com/cgi-bin/deal_detail/view?deal_id=" + errorJson.dealCode;
            trObj.find(".deal-link").attr("href", url);
            trObj.find(".error-dealCode").html(errorJson.dealCode);
            trObj.find(".error-intro").html(errorJson.errorMessage);

            var theDate = new Date();
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
            var timeStr = year + "-" + month+"-"+date+" "+hour+":"+minutes+ ":" + second;
            trObj.find(".op-time").html(timeStr);

            return trObj;
        }
    }, SkinBatch.error);

    /**
     * 工具类
     * @type {*}
     */
    SkinBatch.util = SkinBatch.util || {};
    SkinBatch.util = $.extend({
        //判断ajax是否返回成功的状态
        checkAjaxSuccess: function(dataJson) {
            if (dataJson.message === undefined || dataJson.message == null || dataJson.message == "") {

            } else {
                alert(dataJson.message);
            }
            return dataJson.success;
        }
    }, SkinBatch.util);

})(jQuery,window));
