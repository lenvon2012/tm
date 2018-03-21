TM = TM || {};
((function($,window){
    TM.showResult = TM.showResult || {};
    var showResult = TM.showResult || {};
    showResult.result = $.extend({
        toPutResultDialog:function(data,actStr,url1,url2){
            if(data != null){
               var num = 0;
               var tableObj=$('<table class="errorTable busSearch" style="text-align: center;"></table>');
               var firstRow = showResult.result.createFirstRow();
               tableObj.append(firstRow);
               var success = 0;
               var fail = 0;
               $.each(data,function(i,value){
                   if(value.ok){
                       success ++;
                   }else{
                       fail ++;
                       num ++;
                       tableObj.append(showResult.result.createErrorRow(value,num));
                   }
               });
               if(fail == 0){
                    TM.Alert.load('<div>' + actStr + '成功：<span class="successNum">'+ success +'</span>个</div>',300,250,function(){window.location.href=url1},false,"投放结果");
               }else{
                   var tableDiv=$("<div class='tableDiv'><div style='width: 100%;height: 40px;line-height: 40px;'>" + actStr + "成功个数：<span class='successNum'>"+ success +"</span>" +
                                  "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + actStr + "失败个数：<span class='failNum'>" + fail + "</span></div></div>");
                   tableDiv.append(tableObj);
                   TM.Alert.load(tableDiv,780,500,function(){window.location.href=url1},false,"投放结果");
               }
            }
        },
        createErrorRow:function(errMsg,i){
            if(i%2==0)
            var trObj=$('<tr class="errorContent evenRow"></tr>');
            else var trObj=$('<tr class="errorContent oddRow"></tr>');
            var td1=showResult.result.createTd1(errMsg.picPath);
            var td2=showResult.result.createTd2(errMsg.title);
            var td3=showResult.result.createTd3(errMsg.msg);
            trObj.append(td1);
            trObj.append(td2);
            trObj.append(td3);
            return trObj;
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
    });
})(jQuery,window));