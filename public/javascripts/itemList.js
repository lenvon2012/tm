var ItemsList = ItemsList || {};

ItemsList.ItemsToList=ItemsList.ItemsToList||{};
ItemsList.ItemsToList = $.extend({
    getItemsList:function(){
        var data = {};
        var ruleJson =ItemsList.rule.getRuleJson();
        data.s=ruleJson.ItemSearchText;
        data.pn=ruleJson.pn;
        data.ps=ruleJson.ps;
        $.ajax({
            url : '/Items/list',
            data : data,
            type : 'get',
            success : function(data) {
                var totalCount = data.totalPnCount*ruleJson.ps;
                var per_page = ruleJson.ps;
                ItemsList.ItemsToList.initPagination(totalCount, per_page, 1);

            }
        });
    },
    initPagination:function(totalCount, per_page, currentPage){
        currentPage--;
        $("#PaginationItem").pagination(totalCount, {
            num_display_entries : 3, // 主体页数
            num_edge_entries : 2, // 边缘页数
            current_page: currentPage,
            callback : ItemsList.ItemsToList.findItemsList,
            items_per_page : per_page,// 每页显示多少项
            prev_text : "&lt上一页",
            next_text : "下一页&gt"
        });
    },
    findItemsList:function(currentPage, jq){
        var ruleJson = ItemsList.rule.getRuleJson();
        ruleJson.pn = currentPage+1;
        var data = {};
        data.s=ruleJson.ItemSearchText;
        data.pn=ruleJson.pn;
        data.ps=ruleJson.ps;
        var pageData = data;
        $.ajax({
            //async : false,
            url : '/Items/list',
            data : data,
            type : 'post',
            success : function(data) {
                ItemsList.ItemsToList.createItems(data.res);
                ItemsList.SetEvent.setItemCheckboxEvent();
                Loading.init.hidden();
            }
        });
    },
    createItems:function(items){
        if(items.length>0)
        {
            var itemTable=$("#allItemTable");
            $(".itemEachRow").each(function(){$(this).remove()});
            var itemRow;
            for(var i=0;i<items.length;i++)
            {
                itemEachRow=ItemsList.ItemsToList.createItemRow(items[i]);
                itemTable.append(itemEachRow);
            }

        }
    },
    createItemRow:function(item,i){
        if(i%2==0)
            var trObj=$('<tr class="itemEachRow evenRow"></tr>');
        else   var trObj=$('<tr class="itemEachRow"></tr>');
        trObj.attr("numIid",item.id);


        var td1=ItemsList.ItemsToList.createItemTd1(item);
        var td2=ItemsList.ItemsToList.createItemTd2(item);
        var td3=ItemsList.ItemsToList.createItemTd3(item);
        var td4=ItemsList.ItemsToList.createItemTd4(item);
        var td5=ItemsList.ItemsToList.createItemTd5(item);
        trObj.append(td1);
        trObj.append(td2);
        trObj.append(td3);
        trObj.append(td4);
        trObj.append(td5);
        return trObj;
    },
    createItemTd1:function(item){
        var tdObj=$('<td class="td1"></td>');
        var input=$('<input  type="checkbox" value="1" name="subCheck" class="CheckOrNot"  style="vertical-align: middle;">');
        input.attr("id",item.id);
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
        var aObj=$('<a></a>');
        var href="/Items/downloadWords?numIid="+item.id;
        aObj.attr("href",href);
        var imgObj=$('<img src="/public/images/button/out.png" class="outBtn"/>');
        aObj.append(imgObj);
        tdObj.append(aObj);
        return tdObj;
    }
}, ItemsList.ItemsToList);

ItemsList.rule = ItemsList.rule || {};

ItemsList.rule = $.extend({
    getRuleJson: function() {
        var ruleJson = {
            pn: 1,
            ps: 7//每页条数
        };


        var ItemSearchText = $("#ItemSearchText").val();
        if (ItemSearchText != null && ItemSearchText != "")
            ruleJson.ItemSearchText = ItemSearchText;


        return ruleJson;
    }
}, ItemsList.rule);

ItemsList.SetEvent = ItemsList.SetEvent || {};
ItemsList.SetEvent= $.extend({
    setEvent:function(){
        ItemsList.SetEvent.setItemSearchBtnEvent();
        ItemsList.SetEvent.setCheckAllEvent();
        ItemsList.SetEvent.setOutAllEvent();
    },

    setItemSearchBtnEvent:function(){
        $(".ItemSearchBtn").click(function(){
            ItemsList.ItemsToList.getItemsList();
        });
    },
    setCheckAllEvent:function(){
        $("#CheckAll").click(function(){
            $('input[name="subCheck"]').attr("checked",this.checked);

        });
    },
    setItemCheckboxEvent:function(){
        $(".CheckOrNot").click(function(){
            var $subBox = $("input[name='subCheck']");
            $("#CheckAll").attr("checked",$subBox.length == $("input[name='subCheck']:checked").length ? true : false);
        });
    },
    setOutAllEvent:function(){
        $("#selectItemsOK").click(function(){
            alert(1);
            var numIidList="";
            var href="/Items/downloadWords?numIid=";
            $(".CheckOrNot").each(function(){
                if($(this).attr("checked")=="checked")
                    numIidList=numIidList+$(this).attr("id")+",";
            });
            $("#outAll").attr("href",href+numIidList);
        });

    }


},ItemsList.SetEvent );

$(document).ready(function(){
    ItemsList.SetEvent.setEvent();
});