var MultiModify = MultiModify || {};

MultiModify.createErrTable=MultiModify.createErrTable||{};

MultiModify.createErrTable=$.extend({
	createTableDiv:function(data){
        var tableDiv=$('<div></div>');

        var tableObj=MultiModify.createErrTable.createTableObj(data);
        tableDiv.append(tableObj);
        return tableDiv;
    },
    multiModifyArea:function(data){
        var multiModifyArea=$("#multiModifyArea");
        $(".tableDiv").remove();
        var tableDiv=$('<div class="tableDiv"><div style="width: 100%;height: 40px;line-height: 40px;">标题修改成功个数：<span class="successNum">'+data.successNum+'</span>'+
            '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;标题修改失败个数：<span class="failNum">'+data.failNum+'</span></div></div>');

        var tableObj=MultiModify.createErrTable.createTableObj(data);
        tableDiv.append(tableObj);
        var modifyAreaTableDiv = tableObj.clone();
        multiModifyArea.find('.errorTable').remove();
        multiModifyArea.append(modifyAreaTableDiv);
        modifyAreaTableDiv.hide();
        TM.Alert.loadDetail(tableDiv,780, 600, null,"错误列表")

    },

    createTableObj:function(data){

        var tableObj=$('<table class="errorTable busSearch" style="text-align: center;"></table>');
        var firstRow=MultiModify.createErrTable.createFirstRow();
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
                    errRow=MultiModify.createErrTable.createErrorRow(data.errorList[i],i);
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
		var td1=MultiModify.createErrTable.createTd1(errMsg.picPath);
		var td2=MultiModify.createErrTable.createTd2(errMsg.title);
		var td3=MultiModify.createErrTable.createTd3(errMsg.msg);
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
								 
},MultiModify.createErrTable);


MultiModify.multiModify=MultiModify.multiModify||{};
MultiModify.multiModify=$.extend({

    modify:function(numIidList){

        var modifyRules=$("input[name='modifyRules']:checked").val();
        if(modifyRules==1)
        {

            var oldKeyword=$("#oldKeyword").val();
            var newKeyword=$("#newKeyword").val();
            $.ajax({
                //async : false,
                url : '/BatchOp/replaceAll',
                type : "post",
                data : {"src":oldKeyword,"target":newKeyword,"numIidList":numIidList},
                success:function(data){

                    MultiModify.createErrTable.multiModifyArea(data);
                    Loading.init.hidden();
                },
                error:function(data){}
            });
        }

        else if(modifyRules==2)
        {

            var deleteKeyword=$("#deleteKeyword").val();
            $.ajax({
                //async : false,
                url : '/BatchOp/removeAll',
                type : "post",
                data : {"src":deleteKeyword,"numIidList":numIidList},
                success:function(data){
                    MultiModify.createErrTable.multiModifyArea(data);
                    Loading.init.hidden();
                },
                error:function(data){}
            });
        }

        else if(modifyRules==3)
        {

            var prefixKeyword=$("#prefixKeyword").val();

            $.ajax({
                //async : false,
                url : '/BatchOp/appendHead',
                type : "post",
                data : {"target":prefixKeyword,"numIidList":numIidList},
                success:function(data){
                    MultiModify.createErrTable.multiModifyArea(data);
                    Loading.init.hidden();
                },
                error:function(data){}
            });
        }

        else if(modifyRules==4)
        {

            var suffixKeyword=$("#suffixKeyword").val();
            $.ajax({
                //async : false,
                url : '/BatchOp/appendTail',
                type : "post",
                data : {"target":suffixKeyword,"numIidList":numIidList},
                success:function(data){
                    MultiModify.createErrTable.multiModifyArea(data);
                    Loading.init.hidden();
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

                    MultiModify.createErrTable.multiModifyArea(data);
                    Loading.init.hidden();
                },
                error:function(data){}
            });
        }
    }
},MultiModify.multiModify)

$(document).ready(function(){
	
	$("#modifyButton").mouseover(function(){
		$(this).attr("src","/public/images/button/multiModify.png");
	});
	$("#modifyButton").mouseout(function(){
		$(this).attr("src","/public/images/button/multiModify2.png");
	});
	$("#lookupButton").mouseover(function(){
		$(this).attr("src","/public/images/button/chakan2.png");
	});
	$("#lookupButton").mouseout(function(){
		$(this).attr("src","/public/images/button/chakan1.png");
	});
	$("#closeImg").click(function(){
		if($("#warmNotice ol").css("display")=="block")
		{
			//$("#warmNotice ol").css("display","none");
            $("#warmNotice ol").fadeOut(1000);
			$("#closeImg").attr("src","/public/images/tips/arrow_down.gif");
		}
		else if($("#warmNotice ol").css("display")=="none")
		{
			//$("#warmNotice ol").css("display","block");
            $("#warmNotice ol").fadeIn(1000);
			$("#closeImg").attr("src","/public/images/tips/arrow_up.gif");
		}
	});
	
	$("#modifyButton").click(function(){
        Loading.init.show();

        var isAll=$("input[name='modifyArea']:checked").val();

        if(isAll==1)
            MultiModify.multiModify.modify("");
        else {

            MultiModify.multiModify.modify(CommChoose.rule.numIidList);
        }
	});

    $("#showResultButton").click(function(){
        $("#multiModifyArea").find('.errorTable').show();
    });

    $("#itemsChooseSpan").click(function(){
        CommChoose.createChoose.createOrRefleshCommsDiv();
    });

	


});
		
