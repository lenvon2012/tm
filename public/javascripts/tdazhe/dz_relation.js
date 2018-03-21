
var TM = TM || {};
((function ($, window) {

    TM.RelationOp = TM.RelationOp || {};

    var RelationOp = TM.RelationOp;

    /**
     * 初始化
     * @type {*}
     */
    RelationOp.init = RelationOp.init || {};
    RelationOp.init = $.extend({
        doInit: function(container) {
            RelationOp.container = container;
            TM.Loading.init.show();
            RelationOp.init.initSearchDiv();
            RelationOp.init.initBatchOpDiv();
            RelationOp.init.initItemContainer();
            //第一次执行
            RelationOp.show.doShow();
        },
        //搜索的div
        initSearchDiv: function() {

            //添加事件
            RelationOp.container.find(".search-btn").click(function() {
                RelationOp.show.doShow();
            });
            RelationOp.container.find(".item-sync").click(function(){
                $.get('/items/sync',function(){
                    TM.Alert.load('<p style="font-size:14px">亲，同步成功,点击确定刷新页面</p>',300,230,function(){
                        window.location.reload();
                    });
                });
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


        },
        //功能按钮
        initBatchOpDiv: function() {

            var getCheckedItems = function() {
                var checkObjs = RelationOp.container.find(".item-checkbox:checked");
                var numIidArray = [];
                checkObjs.each(function() {
                    numIidArray[numIidArray.length] = $(this).attr("numIid");
                });
                return numIidArray;
            };
            //添加事件
            RelationOp.container.find(".relateSelectItemBtn").click(function() {
                var numIidArray = getCheckedItems();
                if (numIidArray.length == 0) {
                    alert("请先选择要添加关联的宝贝");
                    return;
                }
                if (confirm("确定要添加关联" + numIidArray.length + "个宝贝?") == false) {
                    return;
                }
                RelationOp.submit.addRelation(numIidArray,ModelIndex);
            });
            RelationOp.container.find(".deRelateSelectItemBtn").click(function() {
                var numIidArray = getCheckedItems();
                if (numIidArray.length == 0) {
                    alert("请先选择要取消关联的宝贝");
                    return;
                }
                if (confirm("确定要取消关联" + numIidArray.length + "个宝贝?") == false) {
                    return;
                }
                RelationOp.submit.removeRelation(numIidArray);
            });

            RelationOp.container.find(".relateAllItemsBtn").click(function() {
                RelationOp.submit.relateAllItems(ModelIndex);
            });
            RelationOp.container.find(".removeAllItemsRelationBtn").click(function() {
                RelationOp.submit.removeAllItemsRelation();
            });
            RelationOp.container.find(".relateSelectItemALLBtn").click(function() {
                var ruleData=RelationOp.show.getQueryRule();
                ruleData.relateState=0;
                ruleData.index = ModelIndex;
                RelationOp.submit.relateSelectAllItems(ruleData);
            });
            RelationOp.container.find(".deRelateSelectItemALLBtn").click(function() {
                var ruleData=RelationOp.show.getQueryRule();
                ruleData.relateState=1;
                RelationOp.submit.removeSelectAllItemsRelation(ruleData);
            });
            RelationOp.container.find(".ModelSelect").click(function(){
                RelationOp.submit.DoSelectModel();
            });
        },

        //宝贝表格
        initItemContainer: function() {

            //设置事件
            RelationOp.container.find(".select-all-item").click(function() {
                var isChecked = $(this).is(":checked");
                var checkObjs = RelationOp.container.find(".item-checkbox");
                checkObjs.attr("checked", isChecked);
                if(isChecked==false){
                    RelationOp.container.find(".top-1").css("background-color","#ebf3ff");
                    RelationOp.container.find(".top-2").css("background-color","#ebf3ff");
                }
                else{
                    RelationOp.container.find(".top-1").css("background-color","#ffffab");
                    RelationOp.container.find(".top-2").css("background-color","#ffffab");
                }
            });
        }
    }, RelationOp.init);

    /**
     * 查询
     * @type {*}
     */
    RelationOp.show = RelationOp.show || {};
    RelationOp.show.orderData = {
        asc: "asc",
        desc: "desc"
    };
    RelationOp.show = $.extend({
        currentPage: 1,
        ruleData: {
            orderProp: '',      //排序的属性
            orderType: RelationOp.show.orderData.asc    //排序的类型，升序还是降序
        },
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
                    url: '/Relation/queryitems',
                    callback: function(dataJson){
                        RelationOp.show.currentPage = dataJson.pn;//记录当前页
                        itemTbodyObj.html("");
                        var itemArray = dataJson.res;
                        //RelationOp.container.find(".select-all-item").attr("checked", true);
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
        },
        getQueryRule: function() {
            var ruleData = {};
            var title = RelationOp.container.find(".title").val();
            var cid = $("#itemCat option:selected").attr("catid");
            var sellerCid = $("#sellerCat option:selected").attr("catid");
            var itemPriceMin = RelationOp.container.find(".itemPriceMin").val();
            var itemPriceMax = RelationOp.container.find(".itemPriceMax").val();
            var itemState= RelationOp.container.find(".itemState").val();
            var relateState= RelationOp.container.find(".relateState").val();

            ruleData.title = title;
            ruleData.cid = cid;
            ruleData.sellerCid = sellerCid;
            ruleData.itemPriceMin = itemPriceMin;
            ruleData.itemPriceMax = itemPriceMax;
            ruleData.itemState = itemState;
            ruleData.relateState = relateState;

            return ruleData;
        }
    }, RelationOp.show);

    var ModelIndex=0;
    /**
     * 显示一行宝贝
     * @type {*}
     */
    RelationOp.row = RelationOp.row || {};
    RelationOp.row = $.extend({
        createRow: function(index, itemJson) {
            var html1 = '' +
                '<span class="pic"><a class="relation-link" target="_blank"><img style="width: 60px;height: 60px;" class="relation-itemImg" /></a></span>' +
                '';
            var html2 =''+
                '<table class="item-table list-table busSearch">' +
                '   <tbody>' +
                '   <tr>' +
                '       <td  class="top-1" style="background-color: #ebf3ff;" ><input type="checkbox" class="item-checkbox" /></td>' +
                '       <td  class="top-2" style="background-color: #ebf3ff;" colspan="7"></td>' +
                '   </tr>' +
                '   <tr>' +
                '       <td  style="width: 5%;">' +
                '       <td  style="width: 15%;">' +
                '           <span class="main-pic"><a class="item-link" target="_blank"><img style="width:80px;height: 80px;" class="body-itemImg" /></a></span>' +
                '       </td>' +
                '       <td  style="width: 25%;">' +
                '           <span class="name"><a class="item-link" target="_blank"><span class="body-itemName" style="color:#014CCC;"></span></a> </span>' +
                '       </td>' +
                '       <td style="width: 10%;">' +
                '           <div class="body-itemPrice"></div>' +
                '       </td>' +
                '       <td style="width: 10%;border: 1px solid #ccc" >' +
                '            <div class="body-itemState"></div> ' +
                '            <div class="body-position"><a class="position-link" target="_blank"><img class="body-itemposition" /></div>' +
                '       </td>' +
                '       <td style="width: 15%;border: 1px solid #ccc; text-align: center;" rowspan="1">' +
                '            <div class="body-liulan"><img class="body-liulanImg" style="width: 40px;height: 40px;" /></div> ' +
                '          <div class="hover-tips"></div>' +
                '       </td>' +
                '       <td style="width: 10%;border: 1px solid #ccc; text-align: center;font-size: 18px;" rowspan="2">' +
                '            <div class="body-relateState"></div> ' +
                '       </td>' +
                '       <td style="width: 10%;border: 1px solid #ccc" rowspan="2">' +
                '            <span class="relateOneItemBtn tmbtn">添加关联</span>' +
                '       </td>' +
                '   </tr>' +
                '   <tr>' +
                '       <td  style="width: 5%;"><span style="color:#014CCC;">自动宝贝推荐</span></td>' +
                '       <td style="width: 60%" colspan="5" class="relation-img">' +
                '       </td>' +
                '   </tbody>' +
                '</table>' +
                '' ;



            var trObj = $(html2);
            trObj.find(".body-itemImg").attr("src",itemJson.picURL);
            trObj.find(".item-link").attr("href", "http://item.taobao.com/item.htm?id="+itemJson.numIid);
            trObj.find(".body-itemName").html(itemJson.title);
            trObj.find(".body-itemPrice").html("￥"+itemJson.price+"元");
            trObj.find(".body-liulanImg").attr("src","http://img01.taobaocdn.com/top/i1/T1cja7FgtbXXaCwpjX.png");
            if(itemJson.status==1){
                trObj.find(".body-itemState").html("在售中");
                trObj.find(".position-link").attr("href","http://sell.taobao.com/auction/goods/goods_on_sale.htm");
            }
            else{
                trObj.find(".body-itemState").html("下架中");
                trObj.find(".position-link").attr("href","http://sell.taobao.com/auction/goods/goods_in_stock.htm");
            }

            trObj.find(".body-itemposition").attr("src", "/public/images/dazhe/position.gif");


            trObj.find(".item-checkbox").attr("numIid", itemJson.numIid);
            trObj.find(".item-checkbox").each(function() {
                this.itemJson = itemJson;
            });

            //添加事件
            $.get('/Relation/getRelatedRecommends',{numIid: itemJson.numIid},function(data){
                $.each(data, function(i, item){
                    var obj=$(html1);
                    obj.find(".relation-itemImg").attr("src",item.picURL);
                    obj.find(".relation-link").attr("href", "http://item.taobao.com/item.htm?id="+item.numIid);
                    trObj.find(".relation-img").append(obj);
                })
            });

            if((itemJson.type & 2) > 0){
                trObj.find(".relateOneItemBtn").html("取消关联");
                trObj.find(".relateOneItemBtn").addClass("sky-blue-btn");
                trObj.find(".top-1").css("background-color","#FEEDF0");
                trObj.find(".top-2").css("background-color","#FEEDF0");
                trObj.find(".body-relateState").html("已关联8个宝贝");
                trObj.find(".body-relateState").css("color","red");
                trObj.find(".relateOneItemBtn").click(function(){
                    if (confirm("确定取消该宝贝的关联") == false)
                        return;
                    var itemCheckObj = $(this).parents("tbody").find(".item-checkbox");
                    var numIid = itemCheckObj.attr("numIid");
                    var numIidArray = [];
                    numIidArray[numIidArray.length] = numIid;
                    var itemJson = null;
                    itemCheckObj.each(function() {
                        itemJson = this.itemJson;
                    });
                    RelationOp.submit.removeRelation(numIidArray);
                });
            }
            else{
                trObj.find(".relateOneItemBtn").html("添加关联");
                trObj.find(".relateOneItemBtn").addClass("yellow-btn");
                trObj.find(".body-relateState").html("未关联");
                trObj.find(".body-relateState").css("color","green");
                trObj.find(".relateOneItemBtn").click(function(){
                    var itemCheckObj = $(this).parents("tbody").find(".item-checkbox");
                    var numIid = itemCheckObj.attr("numIid");
                    var numIidArray = [];
                    numIidArray[numIidArray.length] = numIid;
                    var itemJson = null;
                    itemCheckObj.each(function() {
                        itemJson = this.itemJson;
                    });

                    RelationOp.submit.addRelation(numIidArray,ModelIndex);
                });
            }

            var model="";

            $.ajax({
                url : '/Relation/getModel',
                data : {numIid:itemJson.numIid,picURL:itemJson.picURL,title:itemJson.title,price:itemJson.price,salesCount:itemJson.salesCount,px:1,index:ModelIndex},
                type : 'post',
                success : function(data) {
                    if(data == null || data.length == 0){
                        alert("没有选择模板！");
                    } else {
//                        trObj.find(".body-liulan").html(data);
                        model=data;
                    }
                }
            });


            trObj.find(".body-liulanImg").mouseover(function(){
                var html4=''+
//                    '<span class="hover_tips_ztb"></span>' +
                    '<div class="hover_tips_cont" id="hoverTipContent">' +
                    model+
                    '</div>' +
                    '';
                trObj.find('.hover-tips').html(html4);
                trObj.find('.hover-tips').show();
            });
            trObj.find(".body-liulanImg").mouseout(function(){
                trObj.find('.hover-tips').hide();
            });

            var checkCallback = function(checkObj) {
                var isChecked = checkObj.is(":checked");
                if (isChecked == false) {
                    trObj.find(".top-1").css("background-color","#ebf3ff");
                    trObj.find(".top-2").css("background-color","#ebf3ff");
                    RelationOp.container.find(".select-all-item").attr("checked", false);
                } else {
                    trObj.find(".top-1").css("background-color","#ffffab");
                    trObj.find(".top-2").css("background-color","#ffffab");
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

    /**
     * 提交批量操作的功能
     * @type {*}
     */
    RelationOp.submit = RelationOp.submit || {};
    RelationOp.submit = $.extend({
        modifyPriceParam: {
            numIidList: [],
            itemJson: {}
        },
        addRelation: function(itemIdArr,index) {
            if (itemIdArr === undefined || itemIdArr == null || itemIdArr.length == 0)
                return;
            var data = {};
            data.numIidArr = itemIdArr;
            data.index=index;
            $.ajax({
                url: '/relation/addRelation',
                dataType: 'json',
                type: 'post',
                data: data,
                error: function() {

                },
                success: function (json) {  alert("宝贝关联成功");
                    //刷新一下
                    RelationOp.show.refresh();
                }
            });
        },
        removeRelation: function(itemIdArr) {
            if (itemIdArr === undefined || itemIdArr == null || itemIdArr.length == 0)
                return;
            var data = {};
            data.numIidArr = itemIdArr;
            $.ajax({
                url: '/relation/removeRelation',
                dataType: 'json',
                type: 'post',
                data: data,
                error: function() {

                },
                success: function (json) {
                    alert("宝贝取消关联成功");
                    RelationOp.show.refresh();
                    //alert("宝贝取消关联成功");
                }
            });
        },
        relateAllItems : function(index){
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
//                        timeout:8000,
                        data:{index:index},
                        type : 'POST',
                        error:function(){
                        },
                        success : function(){
                            //TM.Alert.load('关联成功');
                            alert("全店关联成功");
                            RelationOp.show.refresh();
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
//                        timeout:8000,
                        type : 'POST',
                        error:function(){
                        },
                        success : function(){
                            //TM.Alert.load('关联成功');
                            alert("关联取消成功");
                            RelationOp.show.refresh();
//                            location.reload();
                        }
                    });
                },'取消':function(){
                    $(this).dialog('close');
                }}
            });

            alertDiv.html("<div>确认取消店铺所有宝贝的关联?</div>");
            alertDiv.dialog('open');
        },
        relateSelectAllItems : function(ruleData){
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
                        url : '/Relation/relateLiebiaoItems',
                        data:ruleData,
                        type : 'POST',
                        success : function(){
                            //TM.Alert.load('关联成功');
                            alert("列表页关联成功");
                            RelationOp.show.refresh();
                        }
                    });
                },'取消':function(){
                    $(this).dialog('close');
                }}
            });
            var html="<div>确认关联以下搜索页面的所有宝贝?</div>";
            alertDiv.html();
            alertDiv.dialog('open');
        },
        removeSelectAllItemsRelation : function(ruleData){
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
                        url : '/Relation/removeLiebiaoItems',
                        data:ruleData,
                        type : 'POST',
                        success : function(){
                            //TM.Alert.load('关联成功');
                            alert("取消列表关联成功");
                            RelationOp.show.refresh();
                        }
                    });
                },'取消':function(){
                    $(this).dialog('close');
                }}
            });

            alertDiv.html("<div>确认取消搜索列表页面的所有宝贝的关联?</div>");
            alertDiv.dialog('open');
        },
        //模板选择
        DoSelectModel: function() {
            var dialogObj = $(".model-dialog");
            if (dialogObj.length == 0) {
                var html = '' +
                    '<div class="model-dialog" style="width: 750px;height: 500px;">' +
                    '   <ul class="ul-modelList">' +
                    '   </ul>' +
                    '</div>';

                dialogObj = $(html);

                RelationOp.submit.doModelShow(dialogObj);

                $("body").append(dialogObj);
                dialogObj.dialog({
                    modal: true,
                    bgiframe: true,
                    height:620,
                    width:825,
                    title:'模板选择',
                    autoOpen: false,
                    resizable: false,
                    buttons:{'确定':function() {
                        var selectModel=dialogObj.find(".selected");
                        if(selectModel.length==0){
                            alert("请至少选择一个模板！");
                            return;
                        }
                        if(selectModel.length>1){
                            alert("只能选择一个模板！请正确选择");
                            return;
                        }
                        $(this).dialog('close');
                        ModelIndex =selectModel.val();
                        RelationOp.show.doShow();
                    },'取消':function(){
                        $(this).dialog('close');
                    }}
                });
            }
            dialogObj.dialog("open");
        },
        doModelShow: function(dialogObj) {
            var ulObj= dialogObj.find(".ul-modelList");
            $.ajax({
                url : '/Relation/getModelList',
                data : {},
                type : 'post',
                success : function(dataJson) {
                    if(dataJson == null || dataJson.length == 0){
                        alert("没有选择模板！");
                    } else {
                        var itemArray = dataJson.res;
                        $(itemArray).each(function(index, itemJson) {
                            var trObj = RelationOp.submit.createRow(index, itemJson);
                            ulObj.append(trObj);
                        });
                    }
                }
            });
        },
        createRow: function(index, itemJson) {

            var html='' +
                '<li class="li-modelList" style="position: relative;width: 182px;height: 230px;">' +
                '<div class="td-bigDiv" style="text-align: center;">' +
                '   <div class="td-modelDiv"></div>' +
                '   <div class="td-base" "></div>' +
                '   <div class="td-mouseover" >' +
                '       <div class="border">' +
                '          <span class="td-text-ext" style="background-color: #fb9857;margin-top: 120px;">点击选择</span>' +
                '       </div>' +
                '   </div>' +
                '   <div class="td-click" >' +
                '       <div class="td-img-click">' +
                '       </div>' +
                '      <span class="td-text-ext" style="background-color: #333;margin-top: 50px;">点击取消</span>' +
                '   </div> ' +
                '   <input type="hidden" class="M-index" />' +
                '</div>' +
                '</li>';

            var mobj=$(html);
            mobj.find(".M-index").attr("value",index);
            mobj.find(".td-modelDiv").append(itemJson);

            //设置事件
            mobj.find(".td-bigDiv").mouseover(function(){
                if(!mobj.find(".td-base").hasClass("base")) {
                    mobj.find(".td-mouseover").addClass("mouseover");
                }
            });
            mobj.find(".td-bigDiv").mouseout(function(){
                mobj.find(".td-mouseover").removeClass("mouseover");
            });
            mobj.find(".td-bigDiv").click(function(){
                if(mobj.find(".td-base").hasClass("base")) {
                    mobj.find(".td-base").removeClass("base");
                    mobj.find(".td-click").removeClass("checked");
                    mobj.find(".M-index").removeClass("selected");
                }
                else{
                    mobj.find(".td-base").addClass("base");
                    mobj.find(".td-click").addClass("checked");
                    mobj.find(".td-mouseover").removeClass("mouseover");
                    mobj.find(".M-index").addClass("selected");
                }
            })

            return mobj;
        }
    }, RelationOp.submit);

    /**
     * 操作失败的日志
     * @type {*}
     */
    RelationOp.error = RelationOp.error || {};
    RelationOp.error = $.extend({
        showErrors: function(errorJsonArray) {
            if (errorJsonArray === undefined || errorJsonArray == null || errorJsonArray.length == 0){
                alert("操作成功！");
                RelationOp.show.refresh();
                return ;
            }
            alert("操作有误，请查看最下方的错误列表！");
            RelationOp.container.find(".error-item-div").show();
            $(errorJsonArray).each(function(index, errorJson) {
                var trObj = RelationOp.error.createRow(index, errorJson);
                RelationOp.container.find(".error-item-table").find("tbody").append(trObj);
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
    }, RelationOp.error);

    /**
     * 工具类
     * @type {*}
     */
    RelationOp.util = RelationOp.util || {};
    RelationOp.util = $.extend({
        //判断ajax是否返回成功的状态
        checkAjaxSuccess: function(dataJson) {
            if (dataJson.message === undefined || dataJson.message == null || dataJson.message == "") {

            } else {
                alert(dataJson.message);
            }
            return dataJson.success;
        }
    }, RelationOp.util);

})(jQuery,window));

