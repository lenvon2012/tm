var ItemsChoose = ItemsChoose || {};

ItemsChoose.ItemsToChoose=ItemsChoose.ItemsToChoose||{};
ItemsChoose.ItemsToChoose = $.extend({
    getItemsList:function(){
        var data = {};
        var ruleJson =ItemsChoose.rule.getRuleJson();
        data.s=ruleJson.ItemSearch;
        data.pn=ruleJson.pn;
        data.ps=ruleJson.ps;
        $.ajax({
                url : '/Items/list',
                data : data,
                type : 'get',
                success : function(data) {
                    var totalCount = data.totalPnCount*ruleJson.ps;
                    var per_page = ruleJson.ps;
                    ItemsChoose.ItemsToChoose.initPagination(totalCount, per_page, 1);

                }
        });
    },
    initPagination:function(totalCount, per_page, currentPage){
        currentPage--;
        $("#PaginationItem").pagination(totalCount, {
            num_display_entries : 3, // 主体页数
            num_edge_entries : 2, // 边缘页数
            current_page: currentPage,
            callback : ItemsChoose.ItemsToChoose.findItemsList,
            items_per_page : per_page,// 每页显示多少项
            prev_text : "&lt上一页",
            next_text : "下一页&gt"
        });
    },
    findItemsList:function(currentPage, jq){
        var ruleJson = ItemsChoose.rule.getRuleJson();
        ruleJson.pn = currentPage+1;
        var data = {};
        data.s=ruleJson.ItemSearch;
        data.pn=ruleJson.pn;
        data.ps=ruleJson.ps;
        var pageData = data;
        $.ajax({
            //async : false,
            url : '/Items/list',
            data : data,
            type : 'post',
            success : function(data) {
                ItemsChoose.ItemsToChoose.createItems(data.res);
                ItemsChoose.SetEvent.setItemCheckboxEvent();
                Loading.init.hidden();
            }
        });
    },
    createItems:function(items){
      if(items.length>0)
       {
           var itemTable=$("#itemTable");
           $(".itemRow").each(function(){$(this).remove()});
           var itemRow;
           for(var i=0;i<items.length;i++)
           {
               itemRow=ItemsChoose.ItemsToChoose.createItemRow(items[i]);
               itemTable.append(itemRow);
           }

       }
   },
   createItemRow:function(item,i){
       if(i%2==0)
       var trObj=$('<tr class="itemRow evenRow"></tr>');
       else   var trObj=$('<tr class="itemRow"></tr>');
       trObj.attr("numIid",item.id);
       trObj.attr("name",item.name);

       var td1=ItemsChoose.ItemsToChoose.createItemTd1(item);
       var td2=ItemsChoose.ItemsToChoose.createItemTd2(item);
       var td3=ItemsChoose.ItemsToChoose.createItemTd3(item);
       var td4=ItemsChoose.ItemsToChoose.createItemTd4(item);
       var td5=ItemsChoose.ItemsToChoose.createItemTd5(item);
       trObj.append(td1);
       trObj.append(td2);
       trObj.append(td3);
       trObj.append(td4);
       trObj.append(td5);
       return trObj;
   },
   createItemTd1:function(item){
       var tdObj=$('<td class="td1"></td>');
       var input=$('<input  type="checkbox" value="1" name="checkOne" class="CheckOrNot"  style="vertical-align: middle;">');
       if(ItemsChoose.rule.numIidList.search(item.id)>=0)
            input.attr("checked","checked");
       tdObj.append(input);
       return tdObj;
   },
    createItemTd2:function(item){
        var tdObj=$('<td class="td2"></td>');
        var imgObj=$('<img class="itemImg" />');
        imgObj.attr("src",item.pic);
        tdObj.append(imgObj);
        return tdObj;
    },
    createItemTd3:function(item){
        var tdObj=$('<td class="td3"></td>');
        tdObj.append(item.price);
        return tdObj;
    },
    createItemTd4:function(item){
        var tdObj=$('<td class="td4"></td>');
        tdObj.append(item.name);
        return tdObj;
    },
    createItemTd5:function(item){
        var tdObj=$('<td class="td5"></td>');
        if(item.status==1)
            tdObj.append("出售中");
        else if (item.status==0)
            tdObj.append("下架中");
        return tdObj;
    }
}, ItemsChoose.ItemsToChoose);

ItemsChoose.rule = ItemsChoose.rule || {};

ItemsChoose.rule = $.extend({
    numIidList:"",
    getRuleJson: function() {
        var ruleJson = {
            pn: 1,
            ps: 7//每页条数
        };


        var ItemSearch = $("#ItemSearch").val();
        if (ItemSearch != null && ItemSearch != "")
            ruleJson.ItemSearch = ItemSearch;


        return ruleJson;
    }
}, ItemsChoose.rule);

ItemsChoose.SetEvent = ItemsChoose.SetEvent || {};
ItemsChoose.SetEvent= $.extend({
   setEvent:function(){
       ItemsChoose.SetEvent.setItemChooseDivCloseEvent();
       ItemsChoose.SetEvent.setItemsChooseSpanEvent();
       ItemsChoose.SetEvent.setItemSearchBtnEvent();
       ItemsChoose.SetEvent.setAddToBtnEvent();
       ItemsChoose.SetEvent.setDeleteBtnEvent();
       ItemsChoose.SetEvent.setAllCheckEvent();
       ItemsChoose.SetEvent.setSelectOKEvent();
       ItemsChoose.SetEvent.setClearBtnEvent();
   },
    setItemChooseDivCloseEvent:function(){
        $('#itemChooseDivClose').click(function(){
            ItemsChoose.rule.numIidList="";
            $("#itemChooseDiv").css("display","none");

        });
    },
    setItemsChooseSpanEvent:function(){
        $("#itemsChooseSpan").click(function(){
            $("#itemChooseDiv").css("left",($(window).width()-1000)/2+"px");
            // $("#itemChooseDiv").css("left",($(window).width()-800)/2+"px");
            $("#itemChooseDiv").css("display","block");
            ItemsChoose.ItemsToChoose.getItemsList();
        });
    },
    setItemSearchBtnEvent:function(){
        $(".ItemSearchBtn").click(function(){
            ItemsChoose.ItemsToChoose.getItemsList();
        });
    },
    setAddToBtnEvent:function(){
        $('#addToBtn').click(function(){

            $('.itemRow').each(function(){
                if($(this).find(".CheckOrNot").attr("checked")=="checked")
                {
                    if(ItemsChoose.rule.numIidList.search($(this).attr("numIid"))<0)
                        ItemsChoose.rule.numIidList=ItemsChoose.rule.numIidList+$(this).attr("numIid")+",";
                 //   else alert("亲您重复添加了该宝贝:\n"+$(this).attr("name"));
                }
            });
            var selectNum=ItemsChoose.rule.numIidList.split(",").length-1;
            alert("已选择"+selectNum+"个宝贝(^0^)");
            $("#selectNum").html(ItemsChoose.rule.numIidList.split(",").length-1);

        });
    },
    setDeleteBtnEvent:function(){
        $('#deleteBtn').click(function(){
            var deleteCount=0;
            $('.itemRow').each(function(){
                if($(this).find(".CheckOrNot").attr("checked")=="checked")
                {
                    if(ItemsChoose.rule.numIidList.search($(this).attr("numIid"))>=0)
                    {
                        ItemsChoose.rule.numIidList=ItemsChoose.rule.numIidList.replace(($(this).attr("numIid")+","),"");
                        deleteCount++;
                    }
                //    else alert("亲您试图取消不在选中列表中的宝贝:\n"+$(this).attr("name"));
                }
            });
            var selectNum=ItemsChoose.rule.numIidList.split(",").length-1;
            alert("已删除"+deleteCount+"个宝贝(^0^)");
            $("#selectNum").html(selectNum);


        });
    },
    setAllCheckEvent:function(){
        $("#allCheck").click(function(){
            $('input[name="checkOne"]').attr("checked",this.checked);

        });
    },
    setSelectOKEvent:function(){
        $("#selectOK").click(function(){

            if($('#selectNum').html()=="0")
                alert("亲，请单击左上角‘添加宝贝’按钮将勾选的宝贝添加到宝贝列表(^0^)");
            else
                $("#itemChooseDiv").css("display","none");
        });
    },
    setClearBtnEvent:function(){
        $("#clearBtn").click(function(){
            ItemsChoose.rule.numIidList="";
            $('.itemRow').each(function(){
                $(this).find(".CheckOrNot").attr("checked",false);
            });
            $('#allCheck').attr("checked",false);
            $("#selectNum").html(0);
        });
    },
    setItemCheckboxEvent:function(){
        $(".CheckOrNot").click(function(){
            var $subBox = $("input[name='checkOne']");
            $("#allCheck").attr("checked",$subBox.length == $("input[name='checkOne']:checked").length ? true : false);
        });
    }
},ItemsChoose.SetEvent );

$(document).ready(function(){
    ItemsChoose.SetEvent.setEvent();


});