((function ($, window) {
    TM.TitleRecover = TM.TitleRecover || {};
    var me = TM.TitleRecover;
    me.init = function(id){
        me.container = $('#batchAllRecords');
        me.batchBody = $('#batchAllBody');
        me.container.find('.pagination').tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount:1,
            ajax: {
                on: true,
                dataType: 'json',
                url: "/titles/batchOpLogs",
                callback:function(data){
                    me.buildBatchOpTable(data);
                    if(id && id >0){
                        me.batchBody.find('.detailBtn[targetid="'+id+'"]').trigger('click');
                    }
                }
            }
        });
    }
    me.buildBatchOpTable = function(data){
        me.batchBody.empty();
        if(!TM.util.hasListRes(data)){
            me.batchBody.append("<tr style='height:200px;'><td colspan='4'>亲,暂无您的全店标题优化的记录哟！</td></tr>");
            return;
        }

        var htmls = [];
        $.each(data.res, function(i, elem){
            htmls.push('<tr>');
            htmls.push('<td>'+(i+1)+'</td>')
            htmls.push('<td>'+new Date(elem.ts).formatYMDMS()+'</td>')
            htmls.push('<td>'+elem.successNum+'</td>')
            htmls.push('<td><span class="detailBtn tmbtn sky-blue-btn" targetId='+elem.id+'> 查看详情</span></td>')
            htmls.push('<td><span class="recoverBtn tmbtn yellow-btn" itemNum="'+elem.successNum+'" targetId='+elem.id+'> 一键还原</span></td>')
            htmls.push('</tr>');
        });
        var body = $(htmls.join(''));
        body.find('.detailBtn').click(function(){
            var oThis = $(this);
            var targetid = oThis.attr('targetId');
            me.showDetailOp(targetid);
        });

        body.find('.recoverBtn').click(function(){
            var oThis = $(this);
            if(confirm('点击确定将还原该记录的'+oThis.attr('itemNum')+"个宝贝的标题")){
                var targetid = oThis.attr('targetId');
                $.post("/titles/recoverBatch",{id:targetid},function(data){
                    if(data.failNum == 0) {
                        TM.Alert.load("批量应用推荐标题成功"+data.successNum+"个，失败0个~,点击确定刷新数据",400,300,TM.sync);
                        return;
                    }

                    var multiModifyArea = $('<div></div>');

//                    var multiModifyArea = $("<div></div>");
                    var tableDiv=$('<div id="tableDiv"></div>');
                    // }
                    // tableDiv.empty();
                    var tableObj=MultiModify.createErrTable.createTableObj(data);
                    tableObj.addClass("oplogs");
                    tableDiv.append(tableObj);


                    //var exitBatchOPMsg = multiModifyArea.find('.exitBatchOPMsg');
                    //if(exitBatchOPMsg.length>0) {
                    //   exitBatchOPMsg.remove();
                    // }

                    var successNum = $('<div class="blank0" style="height:10px"></div><div >'+'标题还原成功数:  '+'<span class="successNum" style="color:red">'+data.successNum+'</span>'+'</div>') ;
                    var failNum = $('<div class="blank0" style="height:10px"></div><div >'+'标题还原失败数:  '+'<span class="failNum" style="color:red">'+data.failNum+'</span>'+'</div>') ;
                    multiModifyArea.append(successNum);
                    multiModifyArea.append(failNum);
                    multiModifyArea.append(tableDiv);
//                    multiModifyArea.append($('<div style="width:600px;display: inline-block;"></div>'));
//                    multiModifyArea.append($('<div class="exitBatchOPMsg"><span ></span></div>'));

                    TM.Alert.load(multiModifyArea, 1000, 600, TM.sync);

                });
            }
        });

        me.batchBody.append(body);
    }
    me.showDetailOp = function(targetid){
        $.get('/titles/batchOpLogDetail',{id:targetid},function(list){
            var length = list.length;
            var htmls = [];
            htmls.push("<table class='oplogs' style='width:100%;'><thead><th style='width:80px'>宝贝图片</th><th style='width:270px;'>原标题</th><th style='width:80px;'>是否成功</th><th style='width:270px;'>执行结果</th><th>操作</th></thead>");
            $.each(list, function(i,elem){
                if(!elem ){
                    return;
                }
                if(!elem.msg  || (elem.msg == 'null')){
                    elem.msg = '';
                }
                

                htmls.push("<tr>");
                if(i > 200){
                	htmls.push("<td></td>");
                }else{
                    htmls.push("<td><a target='_blank' href='http://item.taobao.com/item.htm?id="+elem.numIid+"'><img class='itemsnap' src='"+elem.picPath+"' /></a></td>")                	
                }

                htmls.push("<td>"+elem.originTitle+"</td>")

                if( elem.ok && !(elem.newTitle == 'null')){
                    htmls.push("<td style='color:green'>成功</td>");
                    htmls.push("<td>"+elem.newTitle+"</td>");
                    htmls.push("<td>~</td>");
                }else{
                    htmls.push("<td style='color:red'>失败</td>");
                    htmls.push("<td>"+elem.msg+"</td>");
                    if(elem.msg.indexOf("属性出错") >= 0){
                        htmls.push('<td><a target="_blank" class="tmbtn sky-blue-btn" href="http://upload.taobao.com/auction/publish/edit.htm?item_num_id='+elem.numIid+'&auto=false">修改属性</a></td>');
                    } else {
                        htmls.push("<td>~</td>");
                    }
                }

                htmls.push("</tr>");
            });
            htmls.push('</table>');
            TM.Alert.noNavLoad(htmls.join(''), 940, 600);
        });
    }
})(jQuery, window));