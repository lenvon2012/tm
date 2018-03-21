


var TM = TM || {};

TM.Comment = TM.Comment || {};

((function ($, window) {
    var me = TM.Comment;

    me.ErrorHandler = me.ErrorHandler || {};

    me.Rule = me.Rule || {};
    me.Rule.pn=1;
    me.Rule.ps=8;


    TM.Comment.init = function(container, isDenfender){
        if(isDenfender === undefined || isDenfender == null){
            isDenfender = false;
        }

        $("body").find(".qtip").hide();

        me.container = container;
        me.container.empty();

//        me.itemBase = $("<div id='swItemBase' class='clearfix'></div>");


        me.Tips = $('<div id="swItemBase" style="font-family: 微软雅黑;padding:0 20px 0 20px;"><p style="margin-top: 10px;"><em style="color: rgb(157, 22, 221);">温馨提示1：</em>亲，拍拍店长是<em style="color:rgb(157, 22, 221)">帮亲们给买家评价</em>，而不是帮买家给亲们评价哦~</p>' +
            '<p><em style="color: rgb(157, 22, 221);">温馨提示2：</em>拍拍店长会在买家<span style="color: rgb(157, 22, 221);margin-right: 10px;">确认收货(即订单成功)</span>的时候帮亲们给买家评价，目的是吸引买家好评哦，很有效的哦</p>' +
            '<p><em style="color: rgb(157, 22, 221);">温馨提示3：</em>买家确认收货成功之后，如发现未立即评价，亲无需担心，耐心等待~<span style="color: rgb(157, 22, 221);margin-right: 10px;">4小时内</span>拍拍店长会自动帮亲们补全评价滴哦，希望亲能谅解哦~</p><p>若有疑问，请联系<span style="color: rgb(157, 22, 221);">自动评价专员:</span>'+
            '<span style="font-size:14px;padding-top:15px;color:white;">' +
            '<a target="_blank" href="http://wpa.qq.com/msgrd?v=3&uin=2950407153&site=qq&menu=yes">'+
            '<img border="0" src="http://wpa.qq.com/pa?p=2:2950407153:41" alt="点击这里给我发消息" title="点击这里给我发消息"/></a>'+
            '</span>'+
            '</div>');
        me.Tips.appendTo(container);
        me.opArea = $("<div class='clearfix swOpArea'></div>");
        me.switchLine =$("<div class='switchStatusLine '></div>");
        me.switchBottom = $('<div class="clearfix" style="height:10px;border-bottom: 1px solid #9c9c9c;"></div>');
        me.nav = $("<div class='clearfix kitNav'><div class='tmNav'><span class='selected'><a tag='oplogs'>评价日志</a></span></div></div>");
        me.listArea = $("<div class='kitListArea clearfix'></div>");
        me.commentsLog = $("<div class='commentsLog' style='height: 450px;'></div>");
        me.Pagination = $('<p  id="PaginationComments" class="pagination"></p>');

//        me.Config = $('<div style="margin:10px 10px;line-height: 30px;"><table><tr><td style="vertical-align: top;text-align: right;"><div style="display: inline-block;vertical-align: top;font-weight:bold;">自动评价方式：</div></td><td>'
//            +'<div style="display: inline-block;"><div class="radio"><label><input type="radio" name="commentType" id="commentType" value="0" style="width:20px;">买家确认收货后立即评价（推荐）</label></div>'
//            +'<div class="radio"><label><input type="radio" name="commentType" id="commentType" value="1" style="width:20px;">买家好评后立即评价；到期前<input type="text" name="commentTime" class="commentTime-1" style="width:40px;margin:0 5px;text-align: center;" />天未评价，进行抢评</label></div>'
//            +'<div class="radio"><label><input type="radio" name="commentType" id="commentType" value="2" style="width:20px;">买家评价后不立即评价；到期前<input type="text" name="commentTime" class="commentTime-2" style="width:40px;margin:0 5px;text-align: center;" />天未评价，进行抢评</label></div></td></tr>'
//            +'<tr><td style="vertical-align: top;font-weight:bold;text-align:right;">中差评通知：</td><td><div class="checkbox" style="display:inline-block;*display:inline;position:relative;width:220px;"><label><input type="checkbox" class="badcomment-notice" style="float: center;margin-left: 0px;width:20px;">买家给中差评，立即短信通知我</label></div>'
//            +'<div style="display:inline-block;*display:inline;position: relative;margin-left:20px;width:100px;"><a href="/SkinComment/warn" target="_blank" style="color:#3300FF;">设置通知号码</a></div></td></tr>'
//            +'<tr><td style="vertical-align: top;font-weight:bold;text-align:right;">中差评修改：</td><td><div class="checkbox" style="display:inline-block;"><label><input type="checkbox" class="badcomment-check" style="float: center;margin-left: 0px;width:20px;">买家给中差评，立即给买家发送短信，短信模板如下</label></div></td></tr>'
//            +'<tr><td style="vertical-align: top;font-weight:bold;text-align:right;">短信模板：<br><span style="font-weight:normal;color:#669933;">(给中差评买家发送)</span></td><td><div style="display:inline-block;vertical-align: top;"><textarea class="badcomment-msg" style="width:350px;margin-left:3px;" rows="4"></textarea></div>'
//            +'<div style="display:inline-block;margin-left:10px;line-height: 22px;margin-top: 10px;"><span style="color:#CC0000;">#买家#</span> 自动替换成买家昵称<br><span style="color:#CC0000;">#卖家#</span> 自动替换成卖家昵称<br><span style="color:#CC0000;">#评价#</span> 自动替换成买家给出评价</div></td></tr>'
//            +'<tr><td></td><td><a class="btn btn-primary save-comment-btn" style="margin:10px 0 0 10px;" href="javascript:void(0);" >保存设置</a></td></tr></div>');

//        me.itemBase.appendTo(me.container);
        me.commentsLog.appendTo(me.listArea);
        me.Pagination.appendTo(me.listArea);
        me.switchLine.appendTo(me.opArea);

//        if(isDenfender) {
//            me.Config.appendTo(me.opArea);
//        }
        me.switchBottom.appendTo(me.opArea);
        me.opArea.appendTo(me.container);
        me.nav.appendTo(me.container);
        me.listArea.appendTo(me.container);

//        if(!TM.isVip()){
//            TM.widget.buildKitGuidePay('comment').insertBefore(me.Tips);
//        }

        me.regCommentBtn();

        me.container.show();
//
//      me.renderItemInfo();
        me.renderOpArea();
//      me.bindTabListeners();
        me.loadCommentLog();
    }

    TM.Comment.regCommentBtn = function(){
        // 预读配置
        $.get('/PaiPaiSkinComment/commentConf', function(data){
            if(data != null) {
                var commentType = data.commentType;
                $("input[name='commentType'][value="+commentType+"]").attr("checked", true);
                if(commentType > 0) {
                    $(".commentTime-"+commentType).val(data.commentDays);
                }

                if(data.badCommentNotice == true) {
                    $(".badcomment-notice").attr("checked", "checked");
                }
                if(data.badCommentBuyerSms == true) {
                    $(".badcomment-check").attr("checked", "checked");
                } else {
                    $(".badcomment-msg").attr("disabled", "disabled");
                }
                if(data.badCommentMsg != null) {
                    $(".badcomment-msg").val(data.badCommentMsg);
                }
            }
        });

        $(".badcomment-check").click(function(){
            if($(".badcomment-check").attr("checked")=="checked"){
                $(".badcomment-msg").removeAttr("disabled");
            }else{
                $(".badcomment-msg").attr("disabled", "disabled");
            }
        });

        $(".save-comment-btn").click(function(){
            var commentType = $("input[name='commentType']:checked").val();
            var commentTime = 0;
            if(commentType > 0) {
                commentTime = $(".commentTime-"+commentType).val();
            }

            var commentnotice = $(".badcomment-notice").attr("checked");
            var commentNotice = false;
            if(commentnotice == "checked") {
                commentNotice = true;
            }

            var commentcheck = $(".badcomment-check").attr("checked");
            var commentCheck = false;
            if(commentcheck == "checked") {
                commentCheck = true;
            }

            var badcommentMsg = $(".badcomment-msg").val();

            $.post('/PaiPaiSkinComment/updateConf', {commentType:commentType, commentTime:commentTime, badCommentNotice:commentNotice, badCommentBuyerSms:commentCheck, badCommentMsg:badcommentMsg}, function(data){
                TM.Alert.load(data.msg);
            });
        });
    }

//    TM.Comment.genWangWangUrl = function(userName){
//        return "http://www.taobao.com/webww/ww.php?spm=0.0.0.0.FoNdqD&ver=3&touid="+userName+"&siteid=cntaobao&status=1&charset=utf-8"
//    }

    TM.Comment.genCommentModels = function() {
        var commentModels =   '<div class="commentModelsArea">' +
            '   <div class="singleModel">' +
            '       <p style="margin-top: 5px;">特别棒的买家，感谢您一直的陪伴，分享我们的美丽！我们用心经营自己，让品味与价值与日俱增！超多震撼折扣，期待您的再次光临咚咚咚！</p>'+
            '       <div class="comment-model-option">使用</div>'+
            '   </div>'+
            '   <div class="singleModel">' +
            '       <p style="margin-top: 5px;">非常棒的买家，收到宝贝有任何问题请立即与小店售后联系，我们尽力给您处理，也别忘给小店一个好评，互相鼓励下哦。常来看看，让我们有幸为您提供更多的惊喜！</p>'+
            '       <div class="comment-model-option">使用</div>'+
            '    </div>'+
            '   <div class="singleModel">' +
            '       <p style="margin-top: 5px;">很好的买家，欢迎下次光临</p>'+
            '       <div class="comment-model-option">使用</div>'+
            '    </div>'+
            '</div>';
        return commentModels;
    }

    TM.Comment.renderOpArea = function(){
        $.get('/paipaiskincomment/isOn',function(data){
            if(!data){
                return;
            }

            var isOn = data.res;
            var html1 = '' +
                '<span style="font-size: 18px ;display: inline-block;font-weight: bold;color: rgb(157, 22, 221);">自动评价开启状态:</span>' +
                '<a href="javascript:;" class="lightBlueBtn addToActBtn productBtn">已开启</a>' +
                '<a href="javascript:;"  class="lightGrayBtn addedToActBtn productBtn">已关闭</a>' +
                '';
            $(".switchStatusLine").html(html1);
            if(isOn){
                $(".lightBlueBtn").css("display","inline-block");
                $('.lightGrayBtn').css("display","none");
            }
            else{
                $(".lightGrayBtn").css("display","inline-block");
                $('.lightBlueBtn').css("display","none");
            }

            $(".lightBlueBtn").click(function(){
                $(".lightGrayBtn").css("display","inline-block");
                $('.lightBlueBtn').css("display","none");
                $.get('/PaiPaiSkinComment/setOff',function(data){
                    if(data.res==true){
                        TM.Alert.load("自动评价关闭成功！");
                    }
                    else{
                        TM.Alert.load("自动评价关闭失败，请联系在线QQ客服！");
                    }

                });
            }) ;
            $(".lightGrayBtn").click(function(){
                $(".lightBlueBtn").css("display","inline-block");
                $('.lightGrayBtn').css("display","none");
                $.get('/PaiPaiSkinComment/setOn',function(data){
                    if(data.res==true){
                        TM.Alert.load("自动评价开启成功！");
                    }
                    else{
                        TM.Alert.load("自动评价开启失败，请联系在线QQ客服！");
                    }
                });
            }) ;
//            var line = $("<div class='switchStatusLine'></div>");
//            var switchStatus = TM.Switch.createSwitch.createSwitchForm("自动评价开启状态");
//            TM.Comment.buildAutoStatus(switchStatus,isOn);
//            switchStatus.appendTo(me.switchLine);
        });

        $.get('/paipaiskincomment/currContent',function(data){
            if(!data){
                return;
            }

            var commentModels = data.res.split("!@#");
            var content = "";
            for(var i = 0; i<commentModels.length-1; i++) {
                content += commentModels[i] ;
            }

            var html = [];
            html.push('<div style="padding-top:5px;" class="clearfix">');
            /* html.push('<span class="contentarea left"><div>默认评价:</div><div style="background-color:white;border: 1px solid #9c9c9c;width: 730px;word-break: break-all;"><ul class="commentUL" >');
             for(var i = 0; i<commentModels.length-1; i++) {
             html.push('<li class="commentLI"><span style="width:600px; ">'+commentModels[i] + '</span><img src="/public/images/tab/del.gif" class="deleteCommentLI"/>'+'</li>');
             }
             html.push('</ul></div></span>');*/
            html.push('<div style="font-weight:bold;font-size:16px;display:inline-block;">评价内容:</div><span style="font-size:14px;color:#CC0000;">（自动评价默认好评）</span><div style="background-color:white;border: 1px solid #cccccc;border-radius: 5px;width: 730px;padding:10px 0 10px 0" class="commentUL">');
            for(var i = 0; i<commentModels.length-1; i++) {
                var j = i + 1;
                html.push('<div class="commentLI" style="margin-left: 20px;width: 680px;">'+j+'.<div style="width:600px; margin-left: 10px;overflow-x: hidden;word-wrap: break-word;word-break: break-all;" class="inlineblock commentLiContent " title="'+commentModels[i]+'">'+commentModels[i] + '</div><div style="width:600px; margin-left: 10px;overflow-x: hidden;display: none;" class="commentLiInputContentDiv inlineblock"><textarea  class="commentLiInputContent" style="width: 590px;">'+commentModels[i]+'</textarea></div><img src="/public/images/tab/edt.gif" title="编辑" class="editCommentLI"/><img src="/public/images/tab/tb.gif" title="保存" class="saveCommentLI"/><img src="/public/images/tab/del.gif" title="删除" class="deleteCommentLI"/>'+'</div>');
            }
            //html.push('<span style="margin-left: 30px;" class="left contentarea "><div>推荐评价</div><div class="sampleinput" ><span style="padding-top: 2px;padding-left: 2px;">买家人很好，欢迎下次再来</span><br /><span style="padding-left: 2px;">期待您的下次光临</span></div></span>');
            html.push('</div>');
            html.push("<div class='clearfix' style='margin-top: 10px;'><span class='tmbtn yellow-btn' id='setCommentContent' >添加评价</span><input type='text' id='addCommentContent' style='width: 600px;height:25px;'></div>");
            html.push("<div class='clearfix' style='height:10px;border-bottom: 1px solid #9c9c9c;'></div>");
            html.push('<div class="modelTip"><span style="margin-right: 30px;">您可以使用以下<span style="color: red;font-size: 24px;">评价模板</span>作为评价内容</span><span class="toOpenModel modelStatus" >隐藏模板>></span></div>');
            html.push(TM.Comment.genCommentModels());
            var textLine = $('</div>');
            var commitBtn = $(html.join(''));
            commitBtn.find('.comment-model-option').click(function(){
                var modelContetn = $(this).parent().find('p').text();
                var existed = false;
                $('.commentLiContent ').each(function(){
                    var usedContent = $(this).text();
                    if(usedContent == modelContetn) {
                        TM.Alert.load("亲，该模板内容已使用，您不需要重复添加");
                        existed = true ;
                        return ;
                    }
                })
                if(!existed) {
                    $.post('/paipaiskincomment/addContent',{"content":modelContetn},function(data){
                        if(!data.res)  TM.Alert.load("亲，评语添加失败，请重试或联系在线QQ客服~");
                        else  {
                            TM.Alert.load("亲，评语添加成功~请刷新",400,300,function(){location.reload()});
                        }
                    });
                }

            });
            commitBtn.find(".modelStatus").click(function(){
                if($(this).text() == "隐藏模板>>") {
                    $(this).text("展开模板<<");
                    $('.commentModelsArea').fadeOut(1000);
                } else {
                    $(this).text("隐藏模板>>");
                    $('.commentModelsArea').fadeIn(1000);
                }
            });
            commitBtn.find(".commentUL").click(function(){
                $(this).qtip({
                    content: {
                        text: "提示：系统会随机挑选亲设置的评语进行评价（只对自动评价开启之后的订单有效哦~）。亲总共可输入500个字, 请不要包含URL网址，否则自动评价会失败！商城订单与新农业不支持自动评价~"
                    },
                    position: {
                        at: "top left ",
                        corner: {
                            target: 'topRight'
                        }
                    },
                    show: {
                        when: false,
                        ready:true
                    },
                    hide:false,
                    style: {
                        name:'cream'
                    }
                });
            });
            commitBtn.find(".editCommentLI").click(function(){
                var commentLiContent = $(this).parent().find('.commentLiContent');
                var commentLiContentText = commentLiContent.text();
                if(commentLiContent.css("display") == "none"){
                    $(this).parent().find('.commentLiContent').show();
                    $(this).parent().find('.commentLiInputContentDiv').hide();
                } else {
                    $(this).parent().find('.commentLiContent').hide();
                    $(this).parent().find('.commentLiInputContentDiv').show();
                }

            });
            commitBtn.find(".saveCommentLI").click(function(){
                var oldContent = $(this).parent().find('.commentLiContent').text();
                var newContent = $(this).parent().find('.commentLiInputContent').val();
                if(oldContent == newContent){
                    TM.Alert.load("亲，请先修改评语再保存");
                } else {
                    $.post('/paipaiskincomment/editContent',{oldContent:oldContent,newContent:newContent},function(data){
                        if(!data.res)  {
                            TM.Alert.load("亲，修改评语失败，请重试或联系淘标题客服~");
                        }
                        else {
                            TM.Alert.load("亲，修改评语成功~点击刷新",400,300,function(){location.reload()});
                        };
                    });
                }

            });
            commitBtn.find(".deleteCommentLI").click(function(){
                if(confirm("确定要删除本条评语么，亲？")) {
                    var liContent = $(this).parent().find(".commentLiContent").text();
                    $.post('/paipaiskincomment/deleteContent',{"content":liContent},function(data){
                        if(!data.res)  {
                            TM.Alert.load("亲，删除评语失败，请重试或联系淘标题客服~");
                        }
                        else {
                            TM.Alert.load("亲，删除评语成功~请刷新",400,300,function(){location.reload()});
                        };
                    });
                }
            });
            textLine.appendTo(me.opArea);
            commitBtn.appendTo(me.opArea);

            commitBtn.find("#setCommentContent").click(function(){
                var commentWord = commitBtn.find("#addCommentContent").val();
                if(commentWord==""){
                    TM.Alert.load("评语不能为空，请重新输入~");
                } else if(commentWord.indexOf("http") >= 0) {
                    TM.Alert.load("亲，评语不能包含URL网址，否则评价会失败，请修改后提交");
                }

                else{
                    var existed = false;
                    $('.commentLiContent ').each(function(){
                        var usedContent = $(this).text();
                        if(usedContent == commentWord) {
                            TM.Alert.load("亲，您添加的评语已存在，不需要重复添加");
                            existed = true ;
                            return ;
                        }
                    })
                    if(!existed) {
                        $.post('/paipaiskincomment/addContent',{"content":commitBtn.find("#addCommentContent").val()},function(data){
                            if(!data.res)  TM.Alert.load("亲，评语添加失败，请重试或联系淘标题客服~");
                            else  {
                                TM.Alert.load("亲，评语添加成功~请刷新",400,300,function(){location.reload()});
                            }
                        });
                    }
                }

            });

        });
    }


    me.ErrorHandler.validRemoveRes = function(res){
        if(!res || !res.isOk){
            TM.Alert.load('亲,删除失败！');
        }else{
            TM.Alert.load('亲,删除成功！');
        }
    }


    TM.Comment.renderOpLogs = function(){
        me.listArea.empty();
//        var table = $('<div></div>');
//        var bottom = $('<div style="text-align: center;"></div>');
//
//        bottom.tmpage({
//            currPage: 1,
//            pageSize: 10,
//            pageCount:1,
//            ajax: {
//                on: true,
//                dataType: 'json',
//                url: "/windows/listOpLogs",
//                callback:function(data){
//                    if(!data){
//                        return;
//                    }
//
//                    var html = [];
//                    html.push('<table class="oplogs">');
//                    html.push('<thead><tr><th style="width:168px">操作宝贝</th><th>推荐结果</th><th>更新时间</th></tr></thead>');
//                    html.push('<tbody>');
//                    $.each(data.res ,function(i, olog){
//                        html.push('<tr>');
//                        html.push('<td><a href="item.taobao.com/item.htm?id='+olog.numIid+'">'+olog.title+'</a></td>');
//                        html.push('<td>'+olog.content+'</td>');
//                        html.push('<td>'+new Date(olog.ts).formatYMDMS()+'</td>');
//                        html.push('</tr>');
//                    });
//                    html.push('</tbody>');
//                    html.push('</table>');
//
//                    $(html.join('')).appendTo(table);
//                    table.appendTo(me.listArea);
//                    bottom.appendTo(me.listArea);
//                }
//            }
//        });

    }




    TM.Comment.bindTabListeners = function(){
        me.nav.find('a').click(function(){
            var anchor = $(this);
            me.nav.find('.selected').removeClass('selected');
            anchor.parent().addClass('selected');
            me.listArea.empty();
            switch(anchor.attr('tag')){
//                case 'must':
//                    me.renderMustShowItem();
//                    break;
//                case 'exclude':
//                    me.renderExcludeShowItem();
//                    break;
//                case 'onitems':
//                    me.renderOnShowItem();
//                    break;
//                case 'oplogs':
//                    me.renderOpLogs();
//                    break;
            }
        });
    }

    TM.Comment.loadCommentLog = function(){
        $.ajax({
            url:'/paipaiskincomment/getCommentLog',
            type : "post",
            data : {"pn":me.Rule.pn, "ps":me.Rule.ps},
            success : function(data) {
                TM.Comment.initPagination(data.totalPnCount*data.ps, data.ps, data.pn);
                //TM.Comment.createCommentArea(data.res);
            }
        });
    }

    TM.Comment.initPagination = function(totalCount, per_page, currentPage) {
        currentPage--;
        $(".pagination").pagination(totalCount, {
            num_display_entries : 6, // 主体页数
            num_edge_entries : 2, // 边缘页数
            current_page: currentPage,
            callback : TM.Comment.findCommentsList,
            items_per_page : per_page,// 每页显示多少项
            prev_text : "&lt上一页",
            next_text : "下一页&gt"
        });
    }

    TM.Comment.findCommentsList = function(currentPage, jq) {
        $.ajax({
            url:'/paipaiskincomment/getCommentLog',
            type : "post",
            data : {"pn":currentPage+1, "ps":me.Rule.ps},
            success : function(data) {
                TM.Comment.createCommentArea(data.res);
            }
        });
    }

    TM.Comment.createCommentArea = function(comments) {
        me.commentsLog.empty();
        var commentsLog = me.commentsLog;
        commentsLog.append(TM.Comment.createCommentTable(comments));
        //return commentsLog;
    }

    TM.Comment.createCommentTable = function(comments){
        var tableObj = $('<table class="commentTable"></table>');
        tableObj.append(TM.Comment.createTableHead());
        tableObj.append(TM.Comment.createTableContent(comments));
        return tableObj;
    }

    TM.Comment.createTableHead = function(){
        var tableHead = $('<thead class="commentsTableHead"></thead>');
        var trObj = $('<tr></tr>');
        trObj.append($('<td class="result">好/中/差</td>'));
        trObj.append($('<td class="buyer_nick">被评买家</td>'));
        trObj.append($('<td class="content">评价内容</td>'));
        trObj.append($('<td class="oid">订单号</td>'));
        trObj.append($('<td class="ts">评价时间</td>'));
        tableHead.append(trObj);
        return tableHead;
    }

    TM.Comment.createTableContent = function(comments){
        var tableBody = $('<tbody class="commentsTableBody"></tbody>');
        for(var i =0; i<comments.length; i++)
            tableBody.append(TM.Comment.createCommentRow(comments[i]));
        return tableBody;
    }

    TM.Comment.createCommentRow = function(comment) {
        var commentRow = $('<tr class="commentRow"></tr>');
//        if(comment.result == "good")
            var resultTD = $('<td class="result goodResult"><div></div></td>');
//        else if(comment.result == "neutral")
//            var resultTD = $('<td class="result neutralResult"></td>');
//        else
//            var resultTD = $('<td class="result badResult"></td>');

        var buyerNickTD = $('<td class="buyer_nick"></td>');
        buyerNickTD.html(comment.buyerNick);
        var ContentTD = $('<td class="content"></td>');
        ContentTD.html(comment.content);
        var oidTD = $('<td class="oid"></td>');
        oidTD.html(comment.result);
        var tsTD = $('<td class="ts"></td>');
        tsTD.html(new Date(comment.ts).format("yy-MM-dd hh:mm:ss"));

        commentRow.append(resultTD);
        commentRow.append(buyerNickTD);
        commentRow.append(ContentTD);
        commentRow.append(oidTD);
        commentRow.append(tsTD);
        return commentRow;
    }
})(jQuery,window))
