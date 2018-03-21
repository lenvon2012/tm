
var TM = TM || {};

((function ($, window) {

    TM.newAutoTitleChange = TM.newAutoTitleChange || {};

    var newAutoTitleChange = TM.newAutoTitleChange;

    newAutoTitleChange.init = newAutoTitleChange.init || {};
    newAutoTitleChange.init = $.extend({
        doInit: function(container){
            newAutoTitleChange.container = container;

            newAutoTitleChange.Event.setEvent();
        }
    }, newAutoTitleChange.init);

    newAutoTitleChange.Event = newAutoTitleChange.Event || {};
    newAutoTitleChange.Event = $.extend({
        setEvent: function(){
            newAutoTitleChange.Event.setModifyAreaEvent();
            newAutoTitleChange.Event.setItemChooseEvent();
            newAutoTitleChange.Event.setBatchChangeTypeEvent();
            newAutoTitleChange.Event.setModifyButtonEvent();
            newAutoTitleChange.Event.setShowResultButtonEvent();
        },
        setModifyAreaEvent: function(){
            newAutoTitleChange.container.find('input[name="modifyArea"]').unbind('click').click(function(){
                if($(this).val() == 2) {
                    newAutoTitleChange.container.find('#itemsChooseSpan').show();
                } else if($(this).val() == 1) {
                    newAutoTitleChange.container.find('#itemsChooseSpan').hide();
                }
            });
        },
        setItemChooseEvent: function(){
            newAutoTitleChange.container.find('#itemsChooseSpan').unbind('click').click(function(){
                CommChoose.createChoose.createOrRefleshCommsDiv();
            });
        },
        setBatchChangeTypeEvent: function(){
            newAutoTitleChange.container.find('.batchChangeType').unbind('click').click(function(){
                var tag = $(this).attr("tag");
                newAutoTitleChange.container.find('.batchChangeTypeTarget').hide();
                newAutoTitleChange.container.find('.batchChangeTypeTarget[tag="'+tag+'"]').show();
            });
        },
        setModifyButtonEvent: function(){
            newAutoTitleChange.container.find('#modifyButton').unbind('click').click(function(){
                if(confirm("亲，确认要批量修改标题吗?")) {
                    var isAll=$("input[name='modifyArea']:checked").val();

                    if(isAll==1)
                        newAutoTitleChange.Modify.modify("");
                    else {

                        newAutoTitleChange.Modify.modify(CommChoose.rule.numIidList);
                    }
                }
            });
        },
        setShowResultButtonEvent: function(){
            newAutoTitleChange.container.find('#showResultButton').unbind('click').click(function(){
                newAutoTitleChange.container.find(".errTableWrapper").show();
                newAutoTitleChange.container.find(".errTableWrapper .errorTable").show();
            });
        }
    }, newAutoTitleChange.Event);

    newAutoTitleChange.Modify = newAutoTitleChange.Modify || {};
    newAutoTitleChange.Modify = $.extend({
        modify: function(numIidList){
            var modifyRules=newAutoTitleChange.container.find("input.batchChangeType:checked").val();
            if(modifyRules==1)
            {

                var oldKeyword=newAutoTitleChange.container.find("#oldKeyword").val();
                var newKeyword=newAutoTitleChange.container.find("#newKeyword").val();
                $.ajax({
                    //async : false,
                    url : '/BatchOp/replaceAll',
                    type : "post",
                    data : {"src":oldKeyword,"target":newKeyword,"numIidList":numIidList},
                    success:function(data){

                        newAutoTitleChange.Modify.multiModifyArea(data);

                    },
                    error:function(data){}
                });
            }

            else if(modifyRules==2)
            {

                var deleteKeyword=newAutoTitleChange.container.find("#deleteKeyword").val();
                $.ajax({
                    //async : false,
                    url : '/BatchOp/removeAll',
                    type : "post",
                    data : {"src":deleteKeyword,"numIidList":numIidList},
                    success:function(data){
                        newAutoTitleChange.Modify.multiModifyArea(data);

                    },
                    error:function(data){}
                });
            }

            else if(modifyRules==3)
            {

                var prefixKeyword=newAutoTitleChange.container.find("#prefixKeyword").val();

                $.ajax({
                    //async : false,
                    url : '/BatchOp/appendHead',
                    type : "post",
                    data : {"target":prefixKeyword,"numIidList":numIidList},
                    success:function(data){
                        newAutoTitleChange.Modify.multiModifyArea(data);

                    },
                    error:function(data){}
                });
            }

            else if(modifyRules==4)
            {

                var suffixKeyword=newAutoTitleChange.container.find("#suffixKeyword").val();
                $.ajax({
                    //async : false,
                    url : '/BatchOp/appendTail',
                    type : "post",
                    data : {"target":suffixKeyword,"numIidList":numIidList},
                    success:function(data){
                        newAutoTitleChange.Modify.multiModifyArea(data);

                    },
                    error:function(data){}
                });
            }

            else if(modifyRules==5)
            {

                $.ajax({
                    //async : false,
                    url : '/BatchOp/removeAll',
                    type : "post",
                    data : {"src":" ","numIidList":numIidList},
                    success:function(data){

                        newAutoTitleChange.Modify.multiModifyArea(data);

                    },
                    error:function(data){}
                });
            }
        },
        multiModifyArea:function(data){
            var multiModifyArea = newAutoTitleChange.container.find(".errTableWrapper");
            newAutoTitleChange.container.find(".tableDiv").remove();
            var tableDiv=$('<div class="tableDiv"><div style="width: 100%;height: 40px;line-height: 40px;">标题修改成功个数：<span class="successNum">'+data.successNum+'</span>'+
                '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;标题修改失败个数：<span class="failNum">'+data.failNum+'</span></div></div>');

            var tableObj = newAutoTitleChange.Modify.createTableObj(data);
            tableDiv.append(tableObj);
            var modifyAreaTableDiv = tableObj.clone();
            multiModifyArea.find('.errorTable').remove();
            multiModifyArea.append(modifyAreaTableDiv);
            modifyAreaTableDiv.hide();
            TM.Alert.loadDetail(tableDiv,780, 600, null,"错误列表")

        },
        createTableObj:function(data){

            var tableObj=$('<table class="errorTable busSearch" style="text-align: center;"></table>');
            var firstRow=newAutoTitleChange.Modify.createFirstRow();
            tableObj.append(firstRow);

            var failNum=$(".failNum");
            failNum.html(data.failNum);
            var successNum=$(".successNum");
            successNum.html(data.successNum);

            var length=data.errorList.length;
            if(length==0)       return  tableObj;
            else
            {
                var errRow;
                for(var i=0;i<length;i++)
                {
                    errRow=newAutoTitleChange.Modify.createErrorRow(data.errorList[i],i);
                    tableObj.append(errRow);
                }
            }
            return tableObj;
        },
        createFirstRow:function(){
            var firstRow=$('<tr class="firstRow "></tr>');
            var td1=$('<th class="errImg" width="120px">宝贝图片</th>');
            var td2=$('<th class="errTitle">宝贝标题</th>');
            var td3=$('<th class="errCause">错误原因</th>');
            firstRow.append(td1);
            firstRow.append(td2);
            firstRow.append(td3);
            return firstRow;
        },
        createErrorRow:function(errMsg,i){


            if(i%2==0)
                var trObj=$('<tr class="errorContent evenRow"></tr>');
            else var trObj=$('<tr class="errorContent oddRow"></tr>');
            var td1=newAutoTitleChange.Modify.createTd1(errMsg.picPath);
            var td2=newAutoTitleChange.Modify.createTd2(errMsg.title);
            var td3=newAutoTitleChange.Modify.createTd3(errMsg.msg);
            trObj.append(td1);
            trObj.append(td2);
            trObj.append(td3);
            return trObj;
        },
        createTd1:function(imgPath){
            var tdObj=$('<td class="errImg"></td>');
            var imgObj=$('<img  style="width:50px;height:50px;" />');
            imgObj.attr("src",imgPath);
            tdObj.append(imgObj);
            return tdObj;
        },
        createTd2:function(title){
            var tdObj=$('<td class="errTitle"></td>');

            tdObj.append(title);
            return tdObj;
        },
        createTd3:function(errMsg){
            var tdObj=$('<td class="errCause"></td>');

            tdObj.append(errMsg);
            return tdObj;
        }
    }, newAutoTitleChange.Modify);
})(jQuery,window));
