((function ($, window) {

    TM.RmLinks  = TM.RmLinks || {}

    var me = TM.RmLinks;

    var pn = 1;

    TM.RmLinks.init = function(container){
        me.container = container;
        me.appendCheckLinks();

        me.listRmLinks();
    }

    TM.RmLinks.appendCheckLinks = function(){
        me.container.empty();

        var diagBtn = $('<div class="diagbtnpos"><div class=""><span class="checkLinkBtn btn btn-success btn-large"><b>立即查找外链</b></span><span class="doRemoveBtn btn btn-primary btn-large"><b>修改选中到淘宝</b></span><span class="doRemoveAllBtn btn btn-warning btn-large"><b>修改全部到淘宝</b></span></div></div>');
        diagBtn.find(".checkLinkBtn").click(function(){
            me.RmConfirmDialog();
        });
        diagBtn.find(".doRemoveBtn").click(function(){
            me.doRemoveLinks();
        });
        diagBtn.find(".doRemoveAllBtn").click(function(){
            me.doRemoveAllLinks();
        });
        me.container.append(diagBtn);

        var resTable = $('<div class="paging-div"></div><table class="rmlinks busSearch"><thead><th style="width:5%;"><input type="checkbox" tag="checkAll" class="checkAll" style="width:13px;" checked="checked"/></th><th style="width:60px;">宝贝</th><th>标题</th><th style="width:40px;">状态</th><th style="width:55%;">存在的外链</th></thead><tbody class="resBody"></tbody></table><div class="paging-div"></div>')
        var resBody = resTable.find('tbody');
        me.resBody = resBody;

        me.container.append(resTable);
    }

    TM.RmLinks.listRmLinks = function() {
        var tbodyObj = me.resBody;
        me.container.find(".paging-div").tmpage({
            currPage: 1,
            pageSize: 10,
            pageCount: 1,
            ajax: {
                on: true,
                dataType: 'json',
                url: '/fenxiao/listRemoveLinks',
                callback: function(dataJson){
                    tbodyObj.empty();
                    var itemArr = dataJson.res;

                    if (!itemArr || itemArr == null || itemArr.length == 0) {
                        tbodyObj.html('<tr><td colspan="5" style="height:40px;padding: 30px;">亲，没有发现外链记录，请点击立即检测吧~</td></tr>');
                        return;
                    }

                    $(itemArr).each(function(index, item) {
                        var trObj = me.createTrObj(item);
                        tbodyObj.append(trObj);
                    });

                    me.container.find('.subCheck').click(function(){
                        var $subBox = me.container.find("input[tag='subCheck']");
                        me.container.find(".checkAll").attr("checked",$subBox.length == me.container.find("input[tag='subCheck']:checked").length ? true : false);
                    });
                }
            }

        });

        me.container.find(".checkAll").click(function(){
            $('input[tag="subCheck"]').attr("checked",this.checked);
        });

    }

    TM.RmLinks.createTrObj = function(item) {
        var linkAction = item.linkActionMap;
        var linkStr = "";
        for(var link in linkAction){
            if(linkAction[link] == 1){
                linkStr += '<p><span style="color:red;font-weight:bold;">删除：</span><a href="'+link+'" target="_blank" style="word-break: break-all;">'+link+'</a></p>';
            } else {
                linkStr += '<p><span style="font-weight:bold;">不处理：</span><a href="'+link+'" target="_blank" style="word-break: break-all;">'+link+'</a></p>';
            }
        }
        var chk = 'checked="checked"';
        var status = "未处理";
        if(item.status == 1) {
            chk += ' disabled="disabled"';
            status = "已修改";
        }
        var trObj = $('<tr tag="'+item.numIid+'"><td><input style="width:13px;" type="checkbox" tag="subCheck" class="subCheck" '+chk+'></td><td><a href="http://item.taobao.com/item.htm?id='+item.numIid+'" target="_blank"><img src="'+item.picUrl+'_80x80.jpg" style="width:60px;height:60px;" /></a></td><td><a href="http://item.taobao.com/item.htm?id='+item.numIid+'" target="_blank">'+item.title+'</a></td><td>'+status+'</td><td>'+linkStr+'</td></tr>');
        return trObj;
    }

    TM.RmLinks.appendPropList = function(){
//        me.container.empty();
        $.get('/Props/dodiag',function(data){
            if(!data || data.length == 0){
                me.resBody.empty();
                me.resBody.append("<tr><td colspan='4'><div class='oknoproblem '><p style='padding-top:20px;'>恭喜您，您的宝贝属性<b class='red'>没有</b>发现问题哟</p></div></td></tr>");
                return;
            }

            var htmls = [];
//            htmls.push('<table class="">');
//            htmls.push('<thead><th>宝贝</th><th>标题</th><th>操作</th></thead>')
            //http://upload.taobao.com/auction/publish/edit.htm?spm=686.1000925.1000774.8.AthpLL&item_num_id=21587756782&auto=false
            $.each(data,function(i, elem){
                htmls.push('<tr>');
                htmls.push('<td><a target="_blank" href="http://item.taobao.com/item.html?id='+elem.numIid+'"><img class="itemsnapwithborder" src="'+elem.picPath+'"></a></td>');
                htmls.push('<td>'+elem.title+'</td>');
                htmls.push('<td class="errormsg">'+elem.msg+'</td>');
                htmls.push('<td>');
                htmls.push('<a target="_blank" class="tmbtn sky-blue-btn" href="http://upload.taobao.com/auction/publish/edit.htm?item_num_id='+elem.numIid+'&auto=false">修改属性</a>');
                htmls.push('<a target="_blank" class="tmbtn yellow-btn" style="margin-top: 5px;" href="http://item.taobao.com/item.html?id='+elem.numIid+'&auto=false">查看宝贝</a>');
                htmls.push('</td>');
                htmls.push('</tr>');
            });
//            htmls.push('</table>');
            var elems = $(htmls.join(''));
            me.resBody.empty();
            elems.appendTo(me.resBody);
        });
    }

    TM.RmLinks.RmConfirmDialog = function(){
        var dialogObj = $(".dialog-div");
        dialogObj.empty();
        var html = '' +
            '<div class="dialog-div" style="background:#fff;">' +
            '   <table class="busSearch" style="margin-top: 10px;">' +
            '       <thead><tr>' +
//            '           <td>宝贝主图</td>' +
//            '           <td>宝贝标题</td>' +
            '           <th style="width:630px;font-size:14px;">发现外链</th>' +
            '           <th><div><input type="checkbox" tag="rmCheckAll" class="rmCheckAll" id="rmCheckAll" style="width:13px;" /><label class="remove-all-label" for="rmCheckAll">去除</label></div></div></th>' +
            '       </tr></thead>' +
            '       <tbody class="dialog-tbody">' +
            '       </tbody>' +
            '   </table>' +
            '   <div class="loading-div" style="text-align: center;padding: 20px;font-weight: bold;color: red;"><img src="/public/images/fenxiao/loading.gif" /><span class="loading-text">正在载入中</span></div>' +
            '</div> ' +
            '';

        dialogObj = $(html);

        $("body").append(dialogObj);

        var title = "选择外链处理方式";
        dialogObj.dialog({
            modal: true,
            bgiframe: true,
            height:450,
            width:750,
            title: title,
            autoOpen: false,
            resizable: false,
            buttons:{'确定':function() {
                var ids = "";
                $('.rmSubCheck:checked').each(function(){
                    var id = $(this).parent().attr('tag');
                    ids += id + ",";
                });
                $.post("/fenxiao/updateLinkAction", {ids:ids, actionType: 1}, function(data){
                    if(data.success){
                        window.location.reload();
                    }
                });
            },'取消':function(){
                $(this).dialog('close');
            }}
        });

        dialogObj.find(".rmCheckAll").click(function(){
            $('input[tag="rmSubCheck"]').attr("checked",this.checked);
        });

        me.checkLinksReq(dialogObj);
    }

    TM.RmLinks.checkLinksReq = function(dialogObj) {
        $.get("/fenxiao/checkLinks", {pn: pn}, function(data){
            var tbody = dialogObj.find(".dialog-tbody");

            if(data.res && data.res.length > 0) {
                $(data.res).each(function(i,one){
                    var chk = one.action == 1 ? 'checked="checked" ' : '';
                    tbody.append('<tr><td><a href="'+one.link+'" title="'+one.link+'" target="_blank"><div style="word-break: break-all;">'+one.link+'</div></a></td>' +
                        '<td tag="'+one.id+'" style="text-align:center;"><input style="width:13px;" type="checkbox" tag="rmSubCheck" class="rmSubCheck" '+chk+'/></td></tr>');
//                        '<td tag="'+one.id+'"><button class="remove-btn btn btn-danger">去除</button><button class="ignore-btn btn btn-info" style="margin-left:5px;">忽略</button></td></tr>');
                });
            }

            dialogObj.find('.rmSubCheck').click(function(){
                var $subBox = dialogObj.find("input[tag='rmSubCheck']");
                dialogObj.find(".rmCheckAll").attr("checked",$subBox.length == dialogObj.find("input[tag='rmSubCheck']:checked").length ? true : false);
            });

//            tbody.find(".remove-btn").click(function(){
//                var id = $(this).parent().attr("tag");
//                var cur = $(this).parent().parent();
//                $.post("/fenxiao/updateLinkAction", {id:id, actionType: 1}, function(data){
//                    if(data.success){
//                        if(cur.parent().find("tr").length == 1){
//                            tbody.append('<tr><td colspan="2" style="text-align: center;font-size: 14px;line-height: 40px;">设置完成，请点击确定开始</td></tr>');
//                        }
//                        cur.remove();
//                    }
//                });
//            });
//            tbody.find(".ignore-btn").click(function(){
//                var id = $(this).parent().attr("tag");
//                var cur = $(this).parent().parent();
//                $.post("/fenxiao/updateLinkAction", {id:id, actionType: 0}, function(data){
//                    if(data.success){
//                        if(cur.parent().find("tr").length == 1){
//                            tbody.append('<tr><td colspan="2" style="text-align: center;font-size: 14px;line-height: 40px;">设置完成，请点击确定开始</td></tr>');
//                        }
//                        cur.remove();
//                    }
//                });
//            });
            dialogObj.dialog('open');
            TM.Loading.init.hidden();

            if(data.count == 10){
                pn++;
                TM.RmLinks.checkLinksReq(dialogObj);
                dialogObj.find(".loading-text").html("正在处理第" + data.pn * 10 + "到" + (data.pn+1)*10 +"个宝贝");
            } else {
                dialogObj.find(".loading-div").hide();

                var l = dialogObj.find(".dialog-tbody tr").length;
                if(l == 0){
                    dialogObj.find(".dialog-tbody").append('<tr><td colspan="3" style="text-align: center;padding: 20px;font-size: 14px;color: blue;">恭喜您，没有发现外链哦</td></tr>');
                }
            }
        });
    }

    TM.RmLinks.doRemoveLinks = function(){
        var ids = "";
        $('.subCheck:checked').each(function(){
            var id = $(this).parent().parent().attr('tag');
            ids += id + ",";
        });
        $.post("/fenxiao/doRemoveLinks", {numIids:ids}, function(data){
            TM.Alert.load('<br><p style="font-size:14px">'+data.message+'</p>',400,230,function(){
                window.location.reload();
            });
        });
    }

    TM.RmLinks.doRemoveAllLinks = function(){
//        me.container.empty();
        $.post('/fenxiao/doRemoveAllLinks',function(data){
            TM.Alert.load('<br><p style="font-size:14px">'+data.message+'</p>',400,230,function(){
                window.location.reload();
            });
        });
    }

})(jQuery, window))
