


var TM = TM || {};

TM.Comment = TM.Comment || {};

((function ($, window) {
    var me = TM.Comment;

    me.ErrorHandler = me.ErrorHandler || {};

    me.Rule = me.Rule || {};
    me.Rule.pn=1;
    me.Rule.ps=8;

    me.Rule.isDefender=false;


    TM.Comment.init = function(container, isDefender){
        if(isDefender === undefined || isDefender == null){
            isDefender = false;
        }
        me.Rule.isDefender = isDefender;

        $("body").find(".qtip").hide();

        me.container = container;
        me.container.empty();

//        me.itemBase = $("<div id='swItemBase' class='clearfix'></div>");
        
        
        me.Tips = $('#swItemBase');
        me.Tips.appendTo(container);
        me.opArea = $("<div class='clearfix swOpArea'></div>");
        me.switchLine =$("<div class='switchStatusLine '></div>");
        me.switchBottom = $('<div class="clearfix" style="height:10px;border-bottom: 1px solid #efefef;"></div>');
        me.nav = $("<div class='clearfix kitNav'><div class='tmNav'><span class='selected'><a tag='oplogs'>评价日志</a></span></div></div>");
        me.listArea = $("<div class='kitListArea clearfix'></div>");
        me.commentsLog = $("<div class='commentsLog'></div>");
        me.Pagination = $('<p  id="PaginationComments" class="pagination"></p>');

        me.Config = $('<div style="margin:10px 10px;line-height: 30px;"><table><tr><td style="vertical-align: top;text-align: right;"><div style="display: inline-block;vertical-align: top;font-weight:bold;">自动评价方式：</div></td><td>'
        		+'<div style="display: inline-block;"><div><input type="radio" name="commentType" id="commentType-0" value="0" style="width:20px;"><label for="commentType-0" style="display:inline-block;*display:inline;position: relative;*zoom:1;">买家确认收货后立即评价（推荐）</label></div>'
        		+'<div style="display: none;"><input type="radio" name="commentType" id="commentType-1" value="1" style="width:20px;"><label for="commentType-1" style="display:inline-block;*display:inline;position: relative;*zoom:1;">买家好评后立即评价</label></div>'
        		+'<div><input type="radio" name="commentType" id="commentType-2" value="2" style="width:20px;"><label for="commentType-2" style="display:inline-block;*display:inline;position: relative;*zoom:1;">买家评价后不立即给其评价，到期前</label><input type="text" name="commentTime" class="commentTime" style="width:40px;margin:0 5px;text-align: center;" />天，进行抢评</div></td></tr>'
                +'<tr style="display:none"><td style="font-weight:bold;text-align:right;">抢评时间设置：</td><td>买家未评价，到期前<input type="text" name="commentTime" class="commentTime" style="width:40px;margin:0 5px;text-align: center;" />天，进行抢评</td></tr>'
                +'<tr style="display: none;"><td style="font-weight:bold;text-align:right;">差评师拦截通知：</td><td><div style="display:inline-block;*display:inline;position: relative;*zoom:1;"><input type="checkbox" class="defense-notice" id="defense-notice" style="margin-left: 0px;width:20px;"><label for="defense-notice" style="display:inline-block;*display:inline;position:relative;*zoom:1;">发现疑似差评师拍下店铺宝贝时，拦截提醒</label></div></td></tr>'
                +'<tr style="display: none;"><td style="font-weight:bold;text-align:right;">中差评通知：</td><td><div style="display:inline-block;*display:inline;position: relative;*zoom:1;"><input type="checkbox" class="badcomment-notice" id="badcomment-notice" style="margin-left: 0px;width:20px;"><label for="badcomment-notice" style="display:inline-block;*display:inline;position:relative;*zoom:1;">买家给中差评，立即短信通知我</label></div>'
        		+'<div style="display:inline-block;*display:inline;position: relative;*zoom:1;margin-left:20px;"><a href="/SkinComment/warn" target="_blank" style="color:#3300FF;">设置通知号码</a></div></td></tr>'
                +'<tr style="display: none;"><td style="font-weight:bold;text-align:right;">中差评反击：</td><td><div style="display:inline-block;*display:inline;position: relative;*zoom:1;"><input type="checkbox" class="badcomment-reply" id="badcomment-reply" style="margin-left: 0px;width:20px;"><label for="badcomment-reply" style="display:inline-block;*display:inline;position:relative;*zoom:1;">买家给中差评，立即回评短信模板内容</label></div></td></tr>'
        		+'<tr style="display: none;"><td style="font-weight:bold;text-align:right;">中差评修改：</td><td><div class="checkbox"><input type="checkbox" class="badcomment-check" id="badcomment-check" style="float: center;margin-left: 0px;width:20px;"><label for="badcomment-check"  style="display:inline-block;*display:inline;position:relative;*zoom:1;">买家给中差评，立即给买家发送短信，短信模板如下</label></div></td></tr>'
        		+'<tr style="display: none;"><td style="vertical-align: top;font-weight:bold;text-align:right;">短信模板：<br><span style="font-weight:normal;color:#669933;">(给中差评买家发送)</span></td><td><div style="display:inline-block;*display:inline;position:relative;*zoom:1;vertical-align: top;"><textarea class="badcomment-msg" style="width:350px;margin-left:3px;" rows="4"></textarea></div>'
        		+'<div style="display:inline-block;*display:inline;position:relative;*zoom:1;margin-left:10px;line-height: 22px;"><span style="color:#CC0000;">#买家#</span> 自动替换成买家昵称<br><span style="color:#CC0000;">#卖家#</span> 自动替换成卖家昵称<br><span style="color:#CC0000;">#评价#</span> 自动替换成买家给出评价</div></td></tr>'
        		+'<tr><td></td><td><a class="btn btn-primary save-comment-btn" style="margin:10px 0 0 10px;" href="javascript:void(0);" >保存设置</a></td></tr></div>');
        
//        me.itemBase.appendTo(me.container);
        me.commentsLog.appendTo(me.listArea);
        me.Pagination.appendTo(me.listArea);
        me.switchLine.appendTo(me.opArea);
        
        if(isDefender) {
        	me.Config.appendTo(me.opArea);
        }
        me.switchBottom.appendTo(me.opArea);
        me.opArea.appendTo(me.container);
        me.nav.appendTo(me.container);
        me.listArea.appendTo(me.container);

        if(!TM.isVip()){
            TM.widget.buildKitGuidePay('comment').insertBefore(me.Tips);
        }
        me.container.show();
        
        if(isDefender) {
        	me.bindCommentConfBtn();
        }
//
//      me.renderItemInfo();
        me.renderOpArea();
//      me.bindTabListeners();
        me.loadCommentLog();
    }
    
    TM.Comment.bindCommentConfBtn = function(){
    	// 预读配置
    	$.get('/skincomment/commentConf', function(data){
			if(data && data != undefined) {
				var commentType = data.commentType;
				$("input[name='commentType'][value="+commentType+"]").attr("checked", true);
				
//				if(commentType > 0) {
					$(".commentTime").val(data.commentDays);
//				}
				if(data.badCommentNotice == true) {
					$(".badcomment-notice").attr("checked", "checked");
				}
				if(data.badCommentBuyerSms == true) {
					$(".badcomment-check").attr("checked", "checked");
				}
				if(data.badCommentMsg != null) {
					$(".badcomment-msg").val(data.badCommentMsg);
				}
                if(data.commentRate == 1) {
                    $(".badcomment-reply").attr("checked", "checked");
                }
                if(data.defenseNotice == true) {
                    $(".defense-notice").attr("checked", "checked");
                }

                if(data.badCommentBuyerSms == false && data.commentRate != 1) {
                    $(".badcomment-msg").attr("disabled", "disabled");
                }
			} else {
                $("#commentType-0").attr("checked", true);
            }

		});
    	
    	$(".badcomment-check").click(function(){
	        if($(".badcomment-check").attr("checked")=="checked" || $(".badcomment-reply").attr("checked")=="checked"){
	        	$(".badcomment-msg").removeAttr("disabled");
	        }else{
	            $(".badcomment-msg").attr("disabled", "disabled");
	        }
	    });
        $(".badcomment-reply").click(function(){
            if($(".badcomment-check").attr("checked")=="checked" || $(".badcomment-reply").attr("checked")=="checked"){
                $(".badcomment-msg").removeAttr("disabled");
            }else{
                $(".badcomment-msg").attr("disabled", "disabled");
            }
        });

        $(".commentTime-1").focus(function(){
            $("#commentType-1").attr("checked", true)
        });
        $(".commentTime").focus(function(){
            $("#commentType-2").attr("checked", true)
        });
    	
    	$(".save-comment-btn").click(function(){
    		var commentType = $("input[name='commentType']:checked").val();
    		var commentTime = 0;
//    		if(commentType > 0) {
    			commentTime = $(".commentTime").val();
//    		}

            var commentNotice = $(".badcomment-notice").attr("checked") == "checked" ? true : false;
            var commentReply = $(".badcomment-reply").attr("checked") == "checked" ? true : false;
            var commentCheck = $(".badcomment-check").attr("checked") == "checked" ? true : false;
            var defenseNotice = $(".defense-notice").attr("checked") == "checked" ? true : false;

    		var badCommentMsg = $(".badcomment-msg").val();
    		
    		$.post('/skincomment/updateConf', {commentType:commentType, commentTime:commentTime, badCommentNotice:commentNotice, badCommentReply:commentReply, badCommentBuyerSms:commentCheck, badCommentMsg:badCommentMsg, defenseNotice:defenseNotice}, function(data){
//    			TM.Alert.load(data.msg);
//                window.location.reload();
    			TM.Alert.load('<p style="font-size:14px">'+data.msg+'</p>',400,230,function(){
                    if(data.isOk){
                    	window.location.reload();
                    }
                });
    		});
    	});
    }

    TM.Comment.genWangWangUrl = function(userName){
        return "http://www.taobao.com/webww/ww.php?spm=0.0.0.0.FoNdqD&ver=3&touid="+userName+"&siteid=cntaobao&status=1&charset=utf-8"
    }

    TM.Comment.genCommentModels = function() {
        var commentModels =   '<div class="commentModelsArea">' +
                '   <div class="singleModel">' +
                '       <p style="margin-top: 5px;">特别棒的买家，感谢您一直的陪伴，分享我们的美丽！超多震撼折扣，期待您的再次光临咚咚咚！</p>'+
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
        $.get('/autocomments/isOn',function(data){
                if(!data){
                    return;
                }

            var isOn = data.res;
//            var line = $("<div class='switchStatusLine'></div>");
            var switchStatus = TM.Switch.createSwitch.createSwitchForm("自动评价开启状态");
            TM.Comment.buildAutoStatus(switchStatus,isOn);
            switchStatus.appendTo(me.switchLine);
        });

        $.get('/autocomments/currContent',function(data){
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
            html.push("<div class='clearfix' style='margin-top: 10px;'><span class='tmbtn yellow-btn' id='setCommentContent' >添加评价</span><input type='text' id='addCommentContent' style='width: 600px;height:50px;'></div>");
            html.push("<div class='clearfix' style='height:10px;border-bottom: 1px solid #efefef;'></div>");
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
                    $.post('/autocomments/addContent',{"content":modelContetn},function(data){
                        if(!data.res)  TM.Alert.load("亲，评语添加失败，请重试或联系淘标题客服~");
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
                    $.post('/AutoComments/editContent',{oldContent:oldContent,newContent:newContent},function(data){
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
                    $.post('/AutoComments/deleteContent',{"content":liContent},function(data){
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
                } else if(commentWord.indexOf("http") >= 0 || commentWord.indexOf(".com") >= 0) {
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
                        $.post('/autocomments/addContent',{"content":commitBtn.find("#addCommentContent").val()},function(data){
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
            url:'/AutoComments/getCommentLog',
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
            url:'/AutoComments/getCommentLog',
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
        if(me.Rule.isDefender) {
            trObj.append($('<td class="result">买家评价</td>'));
            trObj.append($('<td class="result">卖家评价</td>'));
        } else {
            trObj.append($('<td class="result">好/中/差</td>'));
        }
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
        if(me.Rule.isDefender){
            var buyerRateTd = $('<td class="result"></td>');
            if(comment.buyerRate) {
                if((comment.buyerRate & 3) == 1){
                    buyerRateTd = $('<td class="result goodResult"><div></div></td>');
                } else if((comment.buyerRate & 3) == 2) {
                    buyerRateTd = $('<td class="result neutralResult"></td>');
                } else if((comment.buyerRate & 3) == 3) {
                    buyerRateTd = $('<td class="result badResult"></td>');
                }
            }
            commentRow.append(buyerRateTd);
        }

        if(comment.result == "good")
            var resultTD = $('<td class="result goodResult"><div></div></td>');
        else if(comment.result == "neutral")
            var resultTD = $('<td class="result neutralResult"></td>');
        else
            var resultTD = $('<td class="result badResult"></td>');

        var buyerNickTD = $('<td class="buyer_nick"></td>');
        buyerNickTD.html(comment.buyerNick);
        var ContentTD = $('<td class="content"></td>');
        ContentTD.html(comment.content);
        var oidTD = $('<td class="oid"></td>');
        oidTD.html(comment.oid);
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
