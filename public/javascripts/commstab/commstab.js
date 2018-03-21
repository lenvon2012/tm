//$(document).ready(function(){
//    commsTab.rule.init("/Items/list","");
//    commsTab.ItemsToChoose.createItemsByURL().appendTo($("#commsArea"));
//});

var commsTab = commsTab||{};
((function ($, window) {


commsTab.rule = $.extend({
    commsListURL:"",
    commRemoveURL:"",
    getRuleJson: function() {
        var ruleJson = {
            pn: 1,
            ps: 11//每页条数
        };
        return ruleJson;
    },
    init:function(commsListURL,commRemoveURL){
        commsTab.rule.commsListURL = commsListURL;
        commsTab.rule.commRemoveURL = commRemoveURL;
    }
}, commsTab.rule);

commsTab.ItemsToChoose=commsTab.ItemsToChoose||{};
commsTab.ItemsToChoose = $.extend({
    createItemsByURL:function(ops){
        var itemsDiv=$('<div class="itemsByURL"></div>');
        var options = $.extend({
            listUrl :commsTab.rule.commsListURL
        },ops);

        itemsDiv.append(commsTab.ItemsToChoose.createItemTableDiv(options));
        itemsDiv.append(commsTab.ItemsToChoose.createPagination(options));
        return itemsDiv;
    },
    createItemTableDiv:function(){
        var itemTableDiv=$('<div class="itemTableDiv"></div>');
        itemTableDiv.append(commsTab.ItemsToChoose.createTable());
        return itemTableDiv;
    },
    createTable:function(options){
        var tableObj=$('<table id="itemTable"></table>');
        tableObj.append(commsTab.ItemsToChoose.createTableHead());
        commsTab.ItemsToChoose.getItemsList(options);
        return tableObj;
    },
    createTableHead:function(){
        var theadObj=$('<tr class="tableHead"></tr>');
        theadObj.append($('<td class="td1"><input  type="checkbox" value="1" name="checkAll" id="allCheck"  style="vertical-align: middle;"></td>'));
        theadObj.append($('<td class="td2"><span>宝贝图片</span></td>'));
        theadObj.append($('<td class="td3">价格</td>'));
        theadObj.append($('<td class="td4">宝贝标题</td>'));
        theadObj.append($('<td class="td5">操作</td>'));
        return theadObj;
    },
    getItemsList:function(options){
        commsListURL=options.listUrl;
        var data = {};
        var ruleJson =commsTab.rule.getRuleJson();
        data.pn=ruleJson.pn;
        data.ps=ruleJson.ps;
        $.ajax({
            url : commsListURL,
            data : data,
            type : 'get',
            success : function(data) {
                var totalCount = data.totalPnCount*ruleJson.ps;
                var per_page = ruleJson.ps;
                commsTab.ItemsToChoose.initPagination(totalCount, per_page, 1);
            }
        });
    },
    initPagination:function(totalCount, per_page, currentPage){
        currentPage--;
        $("#PaginationItem").pagination(totalCount, {
            num_display_entries : 3, // 主体页数
            num_edge_entries : 2, // 边缘页数
            current_page: currentPage,
            callback : commsTab.ItemsToChoose.findItemsList,
            items_per_page : per_page,// 每页显示多少项
            prev_text : "&lt上一页",
            next_text : "下一页&gt"
        });
    },
    findItemsList:function(currentPage, jq){
        var ruleJson = commsTab.rule.getRuleJson();
        commsListURL=commsTab.rule.commsListURL;
        ruleJson.pn = currentPage+1;
        var data = {};
        data.pn=ruleJson.pn;
        data.ps=ruleJson.ps;
        $.ajax({
            //async : false,
            url : commsListURL,
            data : data,
            type : 'post',
            success : function(data) {
                commsTab.ItemsToChoose.createItems(data.res);
                commsTab.SetEvent.setEvent();
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
                itemRow=commsTab.ItemsToChoose.createItemRow(items[i]);
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

        var td1=commsTab.ItemsToChoose.createItemTd1(item);
        var td2=commsTab.ItemsToChoose.createItemTd2(item);
        var td3=commsTab.ItemsToChoose.createItemTd3(item);
        var td4=commsTab.ItemsToChoose.createItemTd4(item);
        var td5=commsTab.ItemsToChoose.createItemTd5(item);
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
      //  if(commsTab.rule.numIidList.search(item.id)>=0)
      //      input.attr("checked","checked");
        tdObj.append(input);
        return tdObj;
    },
    createItemTd2:function(item){
        var tdObj=$('<td class="td2"></td>');
        var imgObj=$('<img class="itemImg" />');
        imgObj.attr("src",item.pic);
        tdObj.append(imgObj);
        return tdObj;                                                         d
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
            tdObj.append($('<a href="#" class="removeComm">移除</a>'));
        else if (item.status==0)
            tdObj.append($('<a href="#" class="addComm">添加</a>'));
        return tdObj;
    },
    createPagination:function(){
        var paginationDiv=$('<div class="paginationDiv"></div>')
        var pagination=$('<p  id="PaginationItem" class="paginationItem"></p>');
        paginationDiv.append(pagination);
        return paginationDiv;
    }
}, commsTab.ItemsToChoose);


commsTab.SetEvent = commsTab.SetEvent || {};
commsTab.SetEvent= $.extend({
    setEvent:function(){
        commsTab.SetEvent.setAllCheckEvent();
        commsTab.SetEvent.setItemCheckboxEvent();
        commsTab.SetEvent.setRemoveCommEvent();
        commsTab.SetEvent.setAddCommEvent();
    },
    setAllCheckEvent:function(){
        $("#allCheck").attr("checked",false);//先取消全选checkbox
        $("#allCheck").click(function(){
            $('input[name="checkOne"]').attr("checked",this.checked);

        });
    },
    setItemCheckboxEvent:function(){
        $(".CheckOrNot").click(function(){
            var $subBox = $("input[name='checkOne']");
            $("#allCheck").attr("checked",$subBox.length == $("input[name='checkOne']:checked").length ? true : false);
        });
    },
    setRemoveCommEvent:function(callback){
        $(".removeComm").click(function(){
            var commRemoveURL=commsTab.rule.commRemoveURL;
            var data={};
            data.numIid= $(this).parent().attr("numIid");
            $.ajax({
                //async : false,
                url : commRemoveURL,
                data : data,
                type : 'post',
                success : function(data) {
                    callback && callback(data);
                    commsTab.ItemsToChoose.getItemsList();
                }
            });
        });
    },
    setAddCommEvent:function(){
        $(".addComm").click(function(){

        });
    }
},commsTab.SetEvent );


})(jQuery, window));