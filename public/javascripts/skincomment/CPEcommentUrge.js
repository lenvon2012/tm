var TM = TM || {};
((function ($, window) {
    TM.CPECommentUrge = TM.CPECommentUrge || {};

    var CPECommentUrge = TM.CPECommentUrge;

    CPECommentUrge.Init = CPECommentUrge.Init || {};
    CPECommentUrge.Init = $.extend({
        init : function(container){


            container.find(".start-time-text").datetimepicker({
                maxDate : "d",
                onClose : function(selectedDate) {
                    if(selectedDate != null && selectedDate.length > 0){
                        $(".end-time-text").datetimepicker("option", "minDate", selectedDate);
                    }
                }});
            container.find(".end-time-text").datetimepicker({
                maxDate : "d",
                onClose : function(selectedDate) {
                    if(selectedDate != null && selectedDate.length > 0){
                        $(".start-time-text").datetimepicker("option", "maxDate", selectedDate);
                    }
                }});

            TM.CPECommentUrge.Util.initSearchParams();

            TM.CPECommentUrge.Util.initRemarkSet();

            // update the trade-search-table-tbody content
            TM.CPECommentUrge.TradeSearch.search();

            TM.CPECommentUrge.Event.setStaticEvent();

            TM.CPECommentUrge.Init.initShortMsg(container);

            TM.CPECommentUrge.Init.initMsgLogs();

            TM.CPECommentUrge.Init.initRateMsg();
            TM.CPECommentUrge.Init.initZfbMsg();



        },
        initShortMsg:function(container){
            //得到发送短信的按钮
            var shortMsgBtn=$("#short-msg-btn");
            shortMsgBtn.click(function(){
                var subCheck=container
                    .find(".subCheck:checked")
                    .parent()
                    .parent();
                if(subCheck.length<=0){
                    alert("亲，请至少选择一个用户，然后再发送短信!  ");
                    return;
                }

                TM.CPECommentUrge.Show.showSendMsgWindow(subCheck);


            });
        },
        initMsgLogs:function(){
            $("#msg-log-btn").click(function(){
                CPECommentUrge.Show.showSendMsgLogs();

                //阻止事件冒泡
//                  event.stopPropagation();
            });
        },
        initRateMsg:function(container){
            //发送短信按钮
            var rateMsgBtn=$(".rate-sms-send-btn");
            rateMsgBtn.click(function(){
                alert("亲，应短信运营商要求，中差评修改相关短信暂时停止发送，请见谅！");
                return;

                var subCheck=container
                    .find(".subCheck:checked")
                    .parent()
                    .parent();
                if(subCheck.length <= 0){
                    alert("亲，请至少选择一个用户，然后再发送短信!");
                    return;
                }
                // 获取剩余短信条数
                $.ajax({
                    url : '/SkinDefender/getRateSmsRemainCount',
                    type : 'post',
                    dataType:"text",
                    success : function(data) {
                        if(data == null || data == undefined) {
                            alert('出错啦，请刷新重试一下，有问题联系客服哦~');
                            return;
                        } else {
                            TM.CommentNormal.Show.showRateMsgSendDiv(subCheck, data);
                        }
                    }
                });
            });
        },
        initZfbMsg:function(container){
            //发送短信按钮
            var rateMsgBtn=$(".zfb-sms-send-btn");
            rateMsgBtn.click(function(){
                alert("亲，应短信运营商要求，中差评修改相关短信暂时停止发送，请见谅！");
                return;

                var subCheck=container
                    .find(".subCheck:checked")
                    .parent()
                    .parent();
                if(subCheck.length <= 0){
                    alert("亲，请至少选择一个用户，然后再发送短信!");
                    return;
                }
                // 获取剩余短信条数
                $.ajax({
                    url : '/SkinDefender/getRateSmsRemainCount',
                    type : 'post',
                    dataType:"text",
                    success : function(data) {
                        if(data == null || data == undefined) {
                            alert('出错啦，请刷新重试一下，有问题联系客服哦~');
                            return;
                        } else {
                            TM.CommentNormal.Show.showZfbMsgSendDiv(subCheck, data);
                        }
                    }
                });
            });
        },
        initposhytip:function(){
            var showTd=$(".msg-info");

            showTd.poshytip({
                content:function(){
                    return  $(this).find(".msg-content").html();
                },
                className: 'tip-yellow',
                showTimeout: 1,
                alignTo: 'target',
                alignX: 'left',
                offsetY: -40,
                allowTipHover: true
            });

        }
    },CPECommentUrge.Init);

    CPECommentUrge.Show = CPECommentUrge.Show || {};
    CPECommentUrge.Show = $.extend({
        showSendMsgWindow:function(checkedArray){
            var msgWindow = $(".dialog-div");
            msgWindow.empty();

            var sendHtml="";

            //思路，这里先得到号码
            var phones=new Array();
            var msgInfos="";
            checkedArray.each(function(index,element){
                //得到电话号码
                var curSplit=$(element).find(".nick-info").html().split("<br>");
                var curPhone= $.trim(curSplit[1]);

                //组合成一个tr,方便放如table 中
                sendHtml+=
                    '       <tr>' +
                    '           <td style="height:30px; width: 250px;"><span style="width: 250px; text-align: left;">'+curSplit[0]+'</span></td>' +
                    '           <td>'+curSplit[1]+'</td>' +
                    '           <td>'+curSplit[2]+'</td>' +
                    '       </tr>'

                //通过算法过滤掉重复的电话号码
                if(phones.indexOf(curPhone)==-1){
                    phones[phones.length]=curPhone;
//                    msgInfos.msgInfos[msgInfos.msgInfos.length]=curSplit;
                    msgInfos+=curSplit[0]+","+curSplit[1]+","+ $.trim($("<div>"+curSplit[2]+"</div>").text())+"#"
                }

            });



            var msg=
                '       <div class="msg">' +
                '           <div style="height:40px; "><span style="width: 250px; text-align: left; color: #999; margin-top: 20px;">提示：短信发送系统会自动多虑掉重复的电话号码（多个相同号码，只发送一条）</span></div>' +
                '           <div class="black-line"></div>' +
                '           <div class="msg-left" style="height:40px; width: 250px;"><span style="width: 250px; text-align: left;">请输入短信内容：</span></div>' +
                '           <div class="msg-right"><textarea class="content-text" style="width: 250px; height: 120px; "></textarea> </div>' +
                '       </div>' ;

            var html = '' +
                '<div class="dialog-div" style="background:#fff; width: 500px; height: 350px">' +
                '   <table class="dialog-tab" style="margin: 0 auto; margin-top: 20px;">' +
                '   <thead>'+
                '       <tr>' +
                '           <td style="height:40px; width: 30%;"><span style=" text-align: left;">姓名</span></td>' +
                '           <td style="width:30%;">电话</td>' +
                '           <td style="width:40%;">旺旺</td>' +
                '       </tr>'+
                '   </thead>'+
                '       <tbody>' +
                sendHtml+

                '       </tbody>' +
                '   </table>' +
                msg+
                '</div> ';

            msgWindow = $(html);
            $("body").append(msgWindow);

            var title = "批量发送短信：你正在给"+checkedArray.length+"个用户发送短信";
            msgWindow.dialog({
                modal: true,
                bgiframe: true,
                height:450+checkedArray.length*40,
                width:650,
                title: title,
                autoOpen: false,
                resizable: false,
                buttons:{'确定发送':function() {
                    var content = msgWindow.find(".content-text").val();
                    //校验一下短信内容
                    if(content==null||$.trim(content)==""){
                        alert("请输入短信内容");
                        msgWindow.find(".content-text").focus();
                        return;
                    };
                    $.ajax({
                        //测试方法
//                          url : '/SkinDefender/testSendMsg',
                        url : '/SkinDefender/sendMsg',
                        data : {
                            content:content,
                            msgInfoStrings:msgInfos
                        },
                        type : 'post',
                        dataType:"text",
                        success : function(dataJson) {
//                                if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//                                    return;
                            if(dataJson == null || dataJson == undefined) {
                                alert('出错啦，请刷新重试一下，有问题联系客服哦~');
                                return;
                            } else {
                                TM.Alert.load('<br><p style="font-size:14px">'+dataJson+'</p>',400,230,function(){

                                });
                            }
                            //刷新
                            msgWindow.dialog('close');
                        }
                    });
                },'取消':function(){
                    $(this).dialog('close');
                }}
            });

            msgWindow.dialog('open');
        },
        showSendMsgLogs:function(){


            //    listLog.addrName = addrName;
//    listLog.addrPhone = addrPhone;
//    listLog.buyernick = buyernick;
//    listLog.sendTime = sendTime;
//    listLog.isSuccess = isSuccess;
//    listLog.msgInfo = msgInfo;
//    listLog.Remarks = Remarks;

            //生成tab
            var msgLogTable ='<table class="msg-log-tab">' +
                '<thead class="msg-log-thead">' +
                '<tr>' +
                '<td style="width:15%">收件人</td>' +
                '<td style="width:15%">电话号码</td>' +
                '<td style="width:15%">旺旺名称</td>' +
                '<td style="width:15%">发送时间</td>' +
                '<td style="width:15%">短信内容</td>' +
                '<td style="width:10%">是否成功</td>' +
                '<td style="width:15%">备注</td>' +
                '</tr>' +
                '</thead>' +
                '<tbody class="msg-log-tbody">' +
                '</tbody>' +
                '</table>';


            //弹出一个窗口
            TM.Alert.load('<div class="msg-log-box">' +
                '<div class="msg-log-title">短信发送记录：</div>' +
                '<div class="msg-log-header"></div>' +
                '<div class="msg-log-body">' +
                msgLogTable +
                '</div>' +
                '</div>',800,650);


            $(".msg-log-header").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    dataType: 'json',
                    url: '/SkinDefender/findPage',
                    callback: function(dataJson){



                        if(!dataJson){
                            alert("数据获取发生错误，请稍后重试或联系客服");
                        } else {
                            // empty the old trade-search-table-tbody content
                            if(!dataJson.isOk){
                                $('.msg-log-tbody').html('<tr><td colspan="7" style="height:40px;">'+dataJson.msg+'</td></tr>');

                            }
                            else if(dataJson.res != null && dataJson.res != undefined && dataJson.res.length > 0){
                                var res=CPECommentUrge.Show.formatMsgLog(dataJson.res);
                                var html=$("#msg-log-tmpl").tmpl(res);
                                $(".msg-log-tbody").html(html);
                                CPECommentUrge.Init.initposhytip();

                            } else {
                                $('.msg-log-tbody').html('<tr><td colspan="7" style="height:40px;">亲，暂无数据哦</td></tr>');
                            }
                        }
                    }
                }

            });

        },
        formatMsgLog:function(res){

            $(res).each(function(i,e){
                e.sendTime=(new Date(e.sendTime)).formatYMDHMS();
                e.subMsg=$.trim(e.msgInfo).substring(0,5)+"...";

            });

            return res;
        }
    },CPECommentUrge.Show);

    CPECommentUrge.TradeSearch = CPECommentUrge.TradeSearch || {};
    CPECommentUrge.TradeSearch = $.extend({
        search : function(){
            var ruleData = CPECommentUrge.TradeSearch.getQueryRule();
            ruleData.isShowAll = true;
            $('.checkAll').attr("checked",false);
            $(".trade-search-pagging").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: ruleData,
                    dataType: 'json',
                    url: '/SkinComment/searchTradeRecord',
                    callback: function(dataJson){
                        if(!dataJson || !dataJson.isOk){
                            TM.Alert.load("数据获取发生错误，请稍后重试或联系客服");
                        } else {
                            // empty the old trade-search-table-tbody content
                            $('.skincomment-table-tbody').remove();
                            if(dataJson.res != null && dataJson.res != undefined && dataJson.res.length > 0){
                                $('#totalCount').html('搜索到总记录：'+dataJson.count+' 条');
                                $('#totalCount').attr("totalCount", dataJson.count);

                                $('.commentUrge-table').append(TM.CPECommentUrge.TradeSearch.createTbody(dataJson.res));

                                TM.CPECommentUrge.Event.setRemarkSaveClickEvent();
                            } else {
                                $('#totalCount').html('搜索到总记录：0 条');
                                $('#totalCount').attr("totalCount", 0);
                                $('.commentUrge-table').append('<tbody class="skincomment-table-tbody" ><tr><td colspan="9" style="height:40px;">亲，暂无数据哦</td></tr></tbody>');
                            }
                        }
                    }
                }

            });
        },
        getQueryRule : function(){
            var ruleData = {};
            var rate = $('.trade-type span.selected').find('label').attr('type');
            ruleData.type = rate;
            var online = $('#online option:selected').attr("tag");
            ruleData.online = online;
            var buyerNick = $('.buyer-name-text').val();
            ruleData.buyerNick = buyerNick;
            var phone = $('.buyer-phone-text').val();
            ruleData.phone = phone;
            var numIid = $('.numIid-text').val();
            ruleData.numIid = numIid;
            var startTime = $('.start-time-text').val();
            ruleData.startTime = startTime;
            var endTime = $('.end-time-text').val();
            ruleData.endTime = endTime;
            //var sendCount = $('#send-count option:selected').attr("tag");
            //ruleData.sendCount = sendCount;
            var dispatchId = $(".servicegroup option:selected").val();
            ruleData.dispatchId = dispatchId;
            return ruleData;
        },
        createTbody : function(results) {
            var tbody = $('<tbody class="skincomment-table-tbody" ></tbody>');
            $(results).each(function(i,result){
                //console.log(result);

                //已发短信数和已回短信数
                var sendNoteNum = result.rateSmsSendCount ? result.rateSmsSendCount : "0";
                var respondNoteNum = result.rateSmsReceiveCount ? result.rateSmsReceiveCount : "0";
                //宝贝标题
                var goodTitle = TM.CPECommentUrge.TradeSearch.createTitle(result);
                var goodId = result.order.numIid;
                //订单号/收货时间
                var orderIdLink = '<a href="http://trade.taobao.com/trade/detail/trade_item_detail.htm?bizOrderId='+result.order.tid + '" target="_blank" style="font-size: 13px;*font-size: 12px;text-decoration: underline;">[订单号]'+result.order.tid + '</a>';
                var creatTime = '[创建]' + new Date(parseInt(result.order.created)).format("yyyy-MM-dd hh:mm:ss");
                var receiveTime = '[结束]' + new Date(parseInt(result.order.endTime)).format("yyyy-MM-dd hh:mm:ss");
                //成交价/原价
                var closingCost = (result && result.order && result.order.totalFee) ? (result.order.totalFee) : '~';
                var originalCost = (result && result.order && result.order.price) ? (result.order.price) : '~';
                //联系方式
                var receiverName, phone, buyerNick, buyerAlipayNo;
                if(result.order == null || result.order == undefined) {
                    receiverName = "~";
                    phone = "等待获取电话";
                    buyerNick = result.order.nick;
                    buyerAlipayNo = "~";
                } else {
                    receiverName = result.order.receiverName != null && result.order.receiverName != '' ? result.order.receiverName : "~";
                    phone = result.order.phone ?　result.order.phone　: "~";
                    buyerNick = result.order.buyerNick;
                    buyerAlipayNo = (result && result.order && result.order.buyerAlipayNo) ? result.order.buyerAlipayNo :　"~";
                }
                //备注
                var remark = '';
                if(result.order.remark != null) {
                    remark = result.order.remark;
                }
                //分配专员
                var groupName = (result.order.groupName == null || result.order.groupName == undefined) ? '' : result.order.groupName;

                //添加html语句
                //'<td class="detail left-groupName" ><span>' + sendNoteNum + '</span></td>' +
                //'<td class="detail left-groupName" ><span>' + respondNoteNum + '</span></td>' +
                tbody.append($('<tr buyerNick="' + buyerNick + '" receiverName="'+receiverName+'" phone="'+phone+'" tid="' + result.order.tid + '" oid="' + result.order.oid + '">' +
                    '<td><input style="width:13px;" type="checkbox" tag="subCheck" class="subCheck"></td>' +
                    '<td class="detail left-groupName" ><span>' + groupName + '</span></td>' +
                    '<td class="detail left-groupName" ><span>' + sendNoteNum + '</span></td>' +
                    '<td class="detail left-groupName" ><span>' + respondNoteNum + '</span></td>' +
                    '<td class="detail"><div title="' + result.order.title   + '">' + goodTitle + '</div><div>宝贝Id: ' + goodId + '</div></td>' +
                    '<td class="detail">' + orderIdLink + '<br>'+creatTime+ '<br>'+ receiveTime + '</td>' +
                    '<td class="detail" >' + closingCost + '¥/' + originalCost + '¥</td>' +
                    '<td class="detail nick-info">' + receiverName + '<br>' + phone + '<br>' + buyerNick + TM.CPECommentUrge.Util.createBuyerWangWang(buyerNick) + '<br>[支付宝]' + buyerAlipayNo + '</td>' +
                    '<td><textarea rows="2" style="width:100%;height:100%;">' + remark + '</textarea></td>' +
                    '<td><button class="btn btn-primary save-remark" style="margin-left:3px;" tid="' + result.order.tid + '" oid="'+result.order.oid+'">保存</button></td></tr>'
                ));
                //checkbox的点击事件
                tbody.find('.subCheck').click(function(){
                    var $subBox = $("input[tag='subCheck']");
                    $(".checkAll").attr("checked",$subBox.length == $("input[tag='subCheck']:checked").length ? true : false);
                });

            });
            tbody.find('tr:odd').addClass('odd');
            //console.log(tbody);
            return tbody;
        },
        createTitle : function(result){
            var href = "http://item.taobao.com/item.htm?id=" + result.order.numIid;
            return '<a target="_blank" style="text-decoration: none;" href="'+href+'">'+result.order.title+'</a>';
        },
        createImg : function(result) {
            var href = "http://item.taobao.com/item.htm?id=" + result.order.numIid;
            var picPath = "http://img01.taobaocdn.com/imgextra/i2/T1jGS6Xl8aXXXXXXXX-70-70.gif";
            if(result.order != null && result.order != undefined) {
                picPath = result.order.picPath + "_80x80.jpg";
            }
            return '<div ><a target="_blank" href="'+href+'"><img class="imgborder" style="width: 80px;height: 80px;" src="'+picPath+'" title="'+result.order.itemTitle+'" /></a></div>';
        },
        exportExcel: function (btn) {
            var ruleData = CPECommentUrge.TradeSearch.getQueryRule();
            ruleData.isShowAll = true;
            var param = "";
            for (var p in ruleData) {
                param += p + "=" + encodeURIComponent(ruleData[p]) + "&";
            }
            param = param.substring(0, param.length - 1);
            btn.attr('href', '/SkinComment/exportCommentUrgeExcel2?' + param);
        }
    },CPECommentUrge.TradeSearch);

    CPECommentUrge.Event = CPECommentUrge.Event || {};
    CPECommentUrge.Event = $.extend({
        setStaticEvent : function(){
            TM.CPECommentUrge.Event.setTradeTypeClickEvent();
            TM.CPECommentUrge.Event.setTradeSearchBtnClickEvent();

            TM.CPECommentUrge.Event.setCheckAllClickEvent();

            TM.CPECommentUrge.Event.setNoteCareButtonEvent();
            TM.CPECommentUrge.Event.setBatchSelectedDispatchBtnClickEvent();
            TM.CPECommentUrge.Event.setRemarkSaveClickEvent();
            TM.CPECommentUrge.Event.setAddGroupClickEvent();
            TM.CPECommentUrge.Event.setDeleteGroupClickEvent();
            TM.CPECommentUrge.Event.bindServiceGroupEvent();

            TM.CPECommentUrge.ServiceGroup.queryServiceGroups();
        },
        setTradeTypeClickEvent : function(){
            $('.trade-type').find('span').click(function(){
                if(!$(this).hasClass("selected")){
                    // update tmnav selected type
                    $('.trade-type').find('.selected').removeClass('selected');
                    $(this).addClass("selected");
                    // init the search param to original
                    TM.CPECommentUrge.Util.initSearchParams();
                    // update the trade-search-table-tbody content
                    TM.CPECommentUrge.TradeSearch.search();
                }
            });

//            $('.trade-type').find('span').eq(0).trigger("click");
        },
        setTradeSearchBtnClickEvent : function() {
            $('.search-btn').click(function(){
                $('.checkAll').attr("checked",false);
                TM.CPECommentUrge.TradeSearch.search();
            });
            $('.buyer-name-text').keydown(function() {
                if (event.keyCode == "13") {//keyCode=13是回车键
                    $('.search-btn').click();
                }
            });
            $('.buyer-phone-text').keydown(function() {
                if (event.keyCode == "13") {//keyCode=13是回车键
                    $('.search-btn').click();
                }
            });
            $('.numIid-text').keydown(function() {
                if (event.keyCode == "13") {//keyCode=13是回车键
                    $('.search-btn').click();
                }
            });

            $('.update-btn').click(function(){
                $.get("/items/syncTradeRates", function(data){
                    if(!data.success) {
                        TM.Alert.load('<br><p style="font-size:14px">同步太频繁啦！请5分钟后再来试试吧~</p>',400,230,function(){

                        });
                        return;
                    }
//            		TM.CommentUrge.TradeSearch.search();
                    TM.Alert.load('<br><p style="font-size:14px">亲，更新评价成功，点击确定刷新页面查看数据</p>',400,230,function(){
                        window.location.reload();
                    });
                });
            });
            $('.export-btn').click(function(){
                TM.CPECommentUrge.TradeSearch.exportExcel($(this));
            });
        },
        setCheckAllClickEvent : function(){
            $(".checkAll").click(function(){
                $('input[tag="subCheck"]').attr("checked",this.checked);
            });
        },
        setRemarkSaveClickEvent : function() {
            $('.save-remark').unbind('click');
            $('.save-remark').click(function(){
                var oid = $(this).attr('oid');
                var tid = $(this).attr('tid');
                var remark = $(this).parent().parent().find("textarea").val();
                $.post("/skincomment/saveRemarkCommentUrge", {tid:tid, remark:remark, oid:oid}, function(data){
                    TM.Alert.load('<br><p style="font-size:14px">'+data.msg+'</p>',400,230, function () {
                        TM.CPECommentUrge.TradeSearch.search();
                    });
                });
            });
        },
        setNoteCareButtonEvent : function () {
            var note_template_select_div =  $("#note-template-select");
            var query_template = false;//是否正在请求查询模板

            //关怀选中按钮
            $(".send-checked").click(function () {
                var subCheckChecked = $(".subCheck:checked");

                if (subCheckChecked.length == 0) {
                    TM.Alert.load('<br><p style="font-size:14px">亲，请先勾选要发送短信的订单</p>',400,230);
                } else {
                    var checked_number = $(".subCheck:checked").length;
                    var number_message = "发送人数:~人&nbsp;&nbsp;".replace("~", checked_number)
                    note_template_select_div.find(".note-number-message").html(number_message);
                    note_template_select_div.find(".save-div").attr("t", 0);
                    showNoteTemplateSelectDiv();
                }
            });
            //关怀所有按钮
            $(".send-all").click(function () {

                var checked_number = $("#totalCount").attr("totalCount");
                var number_message = "发送人数：~人&nbsp;&nbsp;".replace("~", checked_number);
                note_template_select_div.find(".note-number-message").html(number_message);

                note_template_select_div.find(".save-div").attr("t", 1);
                showNoteTemplateSelectDiv();
            });


            //短信模板数据请求并展示
            function showNoteTemplateSelectDiv() {
                var url = "/SkinComment/queryPassCheckNoteCareTemplate";
                var data = {};
                if (!query_template) {
                    query_template = true;
                    $.post(url, data, function (result) {
                        var template_list = result;
                        var html = "";
                        if (template_list.length == 0) {
                            html += '<div class="note_template" style="height: 50px; line-height: 50px;display: flex;border-bottom: 1px solid #a0a0a0 ;"><span style="flex:1;font-size: 17px">暂无模板</span></div>';
                        } else {
                            $(template_list).each(function (i, template) {
                                var radioInput = '<input type="radio" name="note_select" value="' + template.id + '" ' + (i == 0 ? 'checked="checked"' : '') + '>';
                                var content = template.noteContent + '【' + template.noteDigest + '】'
                                var source = "用户添加";
                                var tr = '<div class="note_template" style="min-height: 35px; line-height: 35px;display: flex;border-bottom: 1px solid #a0a0a0"> ' +
                                    '<span style="flex: 1;width: 0;word-break: break-all;">' + radioInput + '</span> ' +
                                    '<span style="flex: 1;width: 0;word-break: break-all;">' + (i + 1) + '</span> ' +
                                    '<span style="flex: 6;width: 0;word-break: break-all;">' + content + '</span> ' +
                                    '<span style="flex: 3;width: 0;word-break: break-all;">' + source + '</span> ' +
                                    '</div>';
                                html += tr;
                            });
                        }
                        note_template_select_div.find(".note_template").remove();
                        $("#cut-line").before(html);

                        $(".mask").show();
                        $("#note-template-select").show();
                        query_template = false;
                    });
                }


            }



            //短信模板窗口-关闭窗口事件
            note_template_select_div.find(".close-div").click(function () {
                $("#note-template-select").hide();
                $(".mask").hide();
                note_template_select_div.find(".note_template").remove();
                note_template_select_div.find(".note-number-message").html("发送人数:~人&nbsp;&nbsp;预估短信数:~条");
            });

            var sending = false;
            //短信模板窗口-发送短信按钮事件
            note_template_select_div.find(".save-div").click(function () {
                //获取用户选择了哪个模板
                var templateId = $("input[name=note_select]:checked").val();
                if (!templateId) {
                    TM.Alert.load('<br><p style="font-size:14px">没有模板被选中</p>',400,230);
                    return;
                }


                if (sending) return;
                sending = true;
                var flag = note_template_select_div.find(".save-div").attr("t");
                if (flag == 1) {//关怀所有
                    sendNoteSearched(templateId);
                } else { //关怀选中
                    sendNoteChecked(templateId);
                }

            });


            //发送搜索到的所有记录
            function sendNoteSearched(templateId) {
                //获取请求数据
                var data = CPECommentUrge.TradeSearch.getQueryRule();
                //获取用户选择了哪个模板
                data.mid = templateId;
                //提示预估消耗短信数量
                var message = "";
                $.ajax({
                    url:'/SkinComment/forecastNoteAmountSearched',
                    type:'post',
                    data:data,
                    async : true, //默认为true 异步
                    error:function(){
                        alert('error');
                        sending = false;
                    },
                    success:function (result) {
                        var checked_number = $("#totalCount").attr("totalCount");
                        message = "发送人数：" + checked_number + "人    预估短信数：" + (result ? result : "~") + "条";
                        if (confirm(message)) {
                            //请求后台发送短信
                            var url = "/SkinComment/sendUrgeCommentNoteBySearchCondition";
                            $.post(url, data, sendNoteCallBack);
                        } else {
                            sending = false;
                        }
                    }
                });

            }
            //发送用户勾选的记录
            function sendNoteChecked(templateId) {
                var data = {};
                //获取用户选择了哪个模板
                data.mid = templateId;
                //获取用户所选的催评订单记录

                data.oidAndPhoneList = [];
                var subCheckChecked = $(".subCheck:checked");
                subCheckChecked.each(function (index, order) {
                    var tr = $(order).parent().parent("tr");
                    var item = {};
                    item.oid = tr.attr("oid");
                    item.phone = tr.attr("phone");
                    item.receiverName = tr.attr("receiverName");
                    item.buyerNick = tr.attr("buyerNick");

                    data.oidAndPhoneList.push(item);
                })

                //提示预估消耗短信数量
                var message = "";
                $.ajax({
                    url:'/SkinComment/forecastNoteAmountChecked',
                    type:'post',
                    data:data,
                    async : true, //默认为true 异步
                    error:function(){
                        alert('error');
                    },
                    success:function (result) {
                        var checked_number = $(".subCheck:checked").length;
                        message = "发送人数：" + checked_number + "人    预估短信数：" + (result ? result : "~") + "条";
                        //请求后台发送短信
                        if (confirm(message)) {
                            var url = "/SkinComment/sendUrgeCommentNote";
                            $.post(url, data, sendNoteCallBack);
                        } else {
                            sending = false;
                        }
                    }
                });


            }


            //发送短信请求后的回调函数
            function sendNoteCallBack(result) {
                TM.Alert.load('<br><p style="font-size:14px">' + result + '</p>',400,230, function () {
                    note_template_select_div.find(".close-div").click();
                    TM.CPECommentUrge.TradeSearch.search();
                    sending = false;
                });
            }

        },
        setBatchSelectedDispatchBtnClickEvent : function(){
            $('.dispatch-servicegroup-btn').click(function(){
                var oids = "";
                if($('.subCheck:checked').length == 0) {
                    TM.Alert.load('<br><p style="font-size:14px">亲，请先勾选要分配的订单</p>',400,230);
                } else if($(".servicegroup option").length <= 2){
                    TM.Alert.load("请先在右侧添加客服分组，谢谢",400,230);
                } else {
                    CPECommentUrge.ServiceGroup.dispatchGroupDialog();
                }
            });
        },
        setAddGroupClickEvent : function() {
            $(".add-servicegroup-btn").unbind("click");
            $(".add-servicegroup-btn").click(function(){
                CPECommentUrge.ServiceGroup.addGroupDialog();
            });
        },
        setDeleteGroupClickEvent : function() {
            $(".delete-servicegroup-btn").unbind("click");
            $(".delete-servicegroup-btn").click(function(){
                CPECommentUrge.ServiceGroup.deleteGroupDialog();
            });
        },
        bindServiceGroupEvent : function() {
            $(".servicegroup").change(function(){
                TM.CPECommentUrge.TradeSearch.search();
            });
        }

    },CPECommentUrge.Event);

    CPECommentUrge.ServiceGroup = CPECommentUrge.ServiceGroup || {};
    CPECommentUrge.ServiceGroup = $.extend({
        setModelContent : function(){
            $.get('/autocomments/currContent',function(data){
                if(!data){
                    return;
                }
            });
        },
        queryServiceGroups: function() {
            $.get("/skincomment/queryServiceGroups", function(data){
                var servicegroup = $(".servicegroup");
                servicegroup.empty();

                servicegroup.append('<option value="-1">显示所有</option>');
                servicegroup.append('<option value="0">未分配</option>');

                if(data && data.res.length > 0) {
                    $(data.res).each(function(i,one){
                        servicegroup.append('<option value="'+one.id+'">'+one.groupName+'</option>');
                    });
                }
            });
        },
        addGroupDialog: function(){
            var dialogObj = $(".dialog-div");
            dialogObj.empty();

            var html = '' +
                '<div class="dialog-div" style="background:#fff;">' +
                '   <table style="margin: 0 auto; margin-top: 20px;">' +
                '       <tbody>' +
                '       <tr>' +
                '           <td style="height:40px;">新增分组名称：</td>' +
                '           <td><input type="text" class="servicegroup-name" style="width: 200px;"/> </td>' +
                '       </tr>' +
//                    '       <tr>' +
//                    '           <td>备注(可不填)：</td>' +
//                    '           <td><input type="text" class="warn-remark-text"  style="width: 200px;"/> </td>' +
//                    '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '</div> ' +
                '';

            dialogObj = $(html);
            $("body").append(dialogObj);

            var title = "添加客服分组";
            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:250,
                width:450,
                title: title,
                autoOpen: false,
                resizable: false,
                buttons:{'确定':function() {
                    var groupName = dialogObj.find(".servicegroup-name").val();
                    $.ajax({
                        url : '/SkinComment/addServiceGroup',
                        data : {groupName: groupName},
                        type : 'post',
                        success : function(dataJson) {
//                                if (TM.skincomment.util.checkAjaxSuccess(dataJson) == false)
//                                    return;
                            if(dataJson == null || dataJson == undefined) {
                                alert('出错啦，请刷新重试一下，有问题联系客服哦~');
                                return;
                            } else {
                                TM.Alert.load('<br><p style="font-size:14px">'+dataJson.message+'</p>',400,230,function(){
//                                    TM.CommentUrge.ServiceGroup.queryServiceGroups();
//                                    TM.CommentUrge.TradeSearch.search();
                                    window.location.reload();
                                });
                            }
                            //刷新
                            dialogObj.dialog('close');
                        }
                    });
                },'取消':function(){
                    $(this).dialog('close');
                }}
            });

            dialogObj.dialog('open');
        },

        dispatchGroupDialog: function(){
            var dialogObj = $(".dialog-div");
            dialogObj.empty();
            var html = '' +
                '<div class="dialog-div" style="background:#fff;">' +
                '   <table style="margin: 0 auto; margin-top: 20px;">' +
                '       <tbody>' +
                '       <tr>' +
                '           <td style="height:40px;">选择分配到：</td>' +
                '           <td><select name="op-servicegroup" class="op-servicegroup">' +
                '           </select></td>' +
                '       </tr>' +
//                    '       <tr>' +
//                    '           <td>备注(可不填)：</td>' +
//                    '           <td><input type="text" class="warn-remark-text"  style="width: 200px;"/> </td>' +
//                    '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '</div> ' +
                '';

            dialogObj = $(html);

            $("body").append(dialogObj);

            var title = "分配订单到对应客服";
            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:250,
                width:450,
                title: title,
                autoOpen: false,
                resizable: false,
                buttons:{'确定':function() {
                    var oids = "";
                    $('.subCheck:checked').each(function(){
                        var oid = $(this).parent().parent().attr('oid');
                        oids += oid + ",";
                    });
                    var dispatchId = dialogObj.find(".op-servicegroup option:selected").attr("tag");
                    $(this).dialog('close');

                    $.post("/SkinComment/dispatchCommentUrgeServiceGroup",{oids:oids, dispatchId:dispatchId},function(data){
                        if(!data){
                            TM.Alert.load("出错啦，请刷新重试一下，有问题联系客服哦~");
                        } else {
                            TM.Alert.load('<br><p style="font-size:14px">'+data.message+'</p>',400,230,function(){
                                TM.CPECommentUrge.TradeSearch.search();
                            });
//                                TM.CommentUrge.Util.showRateResult(data);
                        }
                    });

                },'取消':function(){
                    $(this).dialog('close');
                }}
            });

            $.get("/skincomment/queryServiceGroups", function(data){
                var servicegroup = dialogObj.find(".op-servicegroup");
                servicegroup.empty();

                if(data && data.res.length > 0) {
                    servicegroup.append('<option tag="0">不分配</option>');
                    $(data.res).each(function(i,one){
                        servicegroup.append('<option tag="'+one.id+'">'+one.groupName+'</option>');
                    });
                }
                dialogObj.dialog('open');
            });

        },

        deleteGroupDialog: function(){
            var dialogObj = $(".dialog-div");
            dialogObj.empty();
            var html = '' +
                '<div class="dialog-div" style="background:#fff;">' +
                '   <table style="margin: 0 auto; margin-top: 20px;">' +
                '       <tbody>' +
                '       <tr>' +
                '           <td style="height:40px;">选择要删除的分组：</td>' +
                '           <td><select name="op-servicegroup" class="op-servicegroup">' +
                '           </select></td>' +
                '       </tr>' +
//                    '       <tr>' +
//                    '           <td>备注(可不填)：</td>' +
//                    '           <td><input type="text" class="warn-remark-text"  style="width: 200px;"/> </td>' +
//                    '       </tr>' +
                '       </tbody>' +
                '   </table>' +
                '</div> ' +
                '';

            dialogObj = $(html);


            $("body").append(dialogObj);

            var title = "选择删除客服";
            dialogObj.dialog({
                modal: true,
                bgiframe: true,
                height:250,
                width:450,
                title: title,
                autoOpen: false,
                resizable: false,
                buttons:{'确定':function() {
                    var groupId = dialogObj.find(".op-servicegroup option:selected").attr("tag");
                    $(this).dialog('close');
                    $.post("/SkinComment/deleteServiceGroup",{groupId:groupId},function(data){
                        if(!data){
                            TM.Alert.load("出错啦，请刷新重试一下，有问题联系客服哦~");
                        } else {
                            TM.Alert.load('<br><p style="font-size:14px">'+data.message+'</p>',400,230,function(){
                                window.location.reload();
                            });
//                                TM.CommentUrge.Util.showRateResult(data);
                        }
                    });

                },'取消':function(){
                    $(this).dialog('close');
                }}
            });


            $.get("/skincomment/queryServiceGroups", function(data){
                var servicegroup = dialogObj.find(".op-servicegroup");
                servicegroup.empty();

                if(data && data.res.length > 0) {
                    $(data.res).each(function(i,one){
                        servicegroup.append($('<option tag="'+one.id+'">'+one.groupName+'</option>'));
                    });
                }

                dialogObj.dialog('open');
            });
        }

    },CPECommentUrge.ServiceGroup);

    CPECommentUrge.Util = CPECommentUrge.Util || {};
    CPECommentUrge.Util = $.extend({
        setModelContent : function(){
            $.get('/autocomments/currContent',function(data){
                if(!data){
                    return;
                }

                var commentModels = data.res.split("!@#");
                var commentModelsArea = $('<div class="commentModelsArea"></div>');
                var random = Math.floor(Math.random()*(commentModels.length-1));
                for(var i = 0; i<commentModels.length-1; i++) {
                    var selected_img = "model-unselected";
                    var selected_text = "comment-model-option-unselected";
                    if(i == random){
                        selected_img = "model-selected";
                        selected_text = "comment-model-option-selected";
                        $('.comment-content').text(commentModels[i]);
                    }
                    commentModelsArea.append($('' +
                        '<div class="singleModel '+selected_img+'">'+
                        '<p>'+commentModels[i]+'</p>'+
                        '<div class="comment-model-option '+selected_text+'">已选择</div>'+
                        '</div>'+
                        ''));
                }
                commentModelsArea.find('.singleModel').click(function(){
                    if($(this).hasClass("model-unselected")) {
                        var selected = $(this).parent().find('.model-selected');
                        selected.removeClass('model-selected');
                        selected.addClass("model-unselected");
                        selected.find('.comment-model-option').text("未选择");
                        selected.find('.comment-model-option').removeClass('comment-model-option-selected');
                        $(this).removeClass('model-unselected');
                        $(this).addClass('model-selected');
                        $(this).find('.comment-model-option').text("已选择");
                        $(this).find('.comment-model-option').addClass('comment-model-option-selected');
                        $('.comment-content').val($(this).find('p').text());
                    }
                });
                $('.modelTip').parent().append(commentModelsArea);
            });
        },
        initRemarkSet : function() {
            $.get("/skincomment/getRemarkSet", function(data){
                var updateRemarkToTB = data.res;
                if(updateRemarkToTB) {
                    $(".remark_set").attr("updateRemarkToTB", true);
                    $(".remark_set").attr("setOp", false);
                    $(".remark_set").html("（同步至淘宝）");
                } else {
                    $(".remark_set").attr("updateRemarkToTB", false);
                    $(".remark_set").attr("setOp", true);
                    $(".remark_set").html("（不同步至淘宝）");
                }

                $(".remark_set").unbind().click(function() {
                    var updateRemarkToTB = $(".remark_set").attr("setOp");
                    $.get("/skincomment/updateRemarkSet", {updateRemarkToTB : updateRemarkToTB}, function(data){
                        TM.Alert.load('<br><p style="font-size:14px">' + data.msg + '</p>',400,230,function(){
                            window.location.reload();
                        });
                    });
                })
            });
        },
        initSearchParams : function(){
            $('.trade-id-text').val("");
            $('.buyer-name-text').val("");
            $('.buyer-phone-text').val("");
            $('.numIid-text').val("");

            var dayMillis = 24 * 3600 * 1000;
            var timeZoneDiff = 8 * 3600 * 1000;
            var endDate = new Date();
            var endTime = parseInt((endDate.getTime() + timeZoneDiff) / dayMillis) * dayMillis - timeZoneDiff + dayMillis - 1;
            endDate.setTime(endTime);
            var startDate = new Date();
            var startTime = endTime - 31 * dayMillis + 1;
            startDate.setTime(startTime);
            $(".start-time-text").val(startDate.format("yyyy-MM-dd hh:mm:ss"));
            $(".end-time-text").val(endDate.format("yyyy-MM-dd hh:mm:ss"));

            $('.checkAll').attr("checked",false);

        },
        createBuyerWangWang : function(buyerNick){
            //return '<a class="buyer-wangwang" target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid='+encodeURI(buyerNick)+'&siteid=cntaobao&status=1&charset=utf-8">'+
            return '<a class="buyer-wangwang" target="_blank" href="http://www.taobao.com/webww/ww.php?ver=3&touid=' + encodeURI(buyerNick) + '&siteid=cntaobao&status=2&charset=utf-8">'+
                '<img class="wwimg" border="0" src="http://amos.alicdn.com/online.aw?v=2&uid='+encodeURI(buyerNick)+'&site=cntaobao&s=2&charset=utf-8" alt="联系买家" /> '+
                '</a>';
        },
        createRateImg : function(rate){
            var real_rate = rate & 3;
            var rate_value;
            if(real_rate == 1) {
                rate_value = "buyer-good-rate";
            } else if(real_rate == 2) {
                rate_value = "buyer-netural-rate";
            } else if(real_rate == 3) {
                rate_value = "buyer-bad-rate";
            } else if(real_rate == 0) {
                rate_value = "buyer-delete-rate";
            }
            return '<span class="rate-flower-img '+rate_value+'" style="margin:auto;display:inline-block;*display:inline;*zoom: 1;"></span>';
            //return '<a class="rate-flower-img '+rate_value+'" style="margin:auto;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</a>';
        },
        showRateResult : function(data){
            if(data.message == "content is empty"){
                TM.Alert.load("评价内容为空，请修改后重试");
            } else if(data.message == "no trade to rate"){
                TM.Alert.load("没有可评价的订单");
            } else if(data.res.length == 0) {
                TM.Alert.load("没有可评价的订单");
            }else {
                var html = '';
                // successNum and failNum
                html += '<div style="text-align: center;width: 100%;height: 30px;line-height: 3px;font-family:微软雅黑;font-size: 24px;margin-top: 15px;">评价成功订单数:<span style="font-size: 30px;color: red;margin-right: 50px;">'+data.successNum+'</span>评价失败订单数<span style="font-size: 30px;color: red;">'+data.failNum+'</span></div>';
                // fail trade table
                html += '<table class="rateResultTable" style="width: 100%;word-wrap: break-word;word-break: break-all;">' +
                    '<thead>' +
                    '<tr>'+
                    '<th style="width: 95px;">主订单号</th>'+
                    '<th style="width: 95px;">子订单号</th>'+
                    '<th style="width: 90px;">买家昵称</th>'+
                    '<th style="width: 40px;">评价</th>'+
                    '<th style="width: 445px;">评价内容</th>'+
                    '<th style="width: 160px;">失败原因</th>'+
                    '</tr>'+
                    '</thead>'+
                    '<tbody>';

                $(data.res).each(function(i,failOrder){
                    var rate = TM.CPECommentUrge.Util.getRateResult(failOrder.result);
                    html += '<tr>' +
                        '<td>'+failOrder.tid+'</td>'+
                        '<td>'+failOrder.oid+'</td>'+
                        '<td>'+failOrder.buyerNick+'</td>'+
                        '<td>'+TM.CPECommentUrge.Util.createRateImg(rate)+'</td>'+
                        '<td>'+failOrder.content+'</td>'+
                        '<td>'+failOrder.reason+'</td>'+
                        '</tr>';
                });
                html += '</tbody>'+
                    '</table>';
                TM.Alert.loadDetail(html, 1000, 600, null,"评价失败列表");
            }
        },
        getRateResult : function(result){
            if(result == 'good'){
                rate = 1;
            } else if (result == "netural") {
                rate = 2;
            } else {
                rate = 3;
            }
            return rate;
        },
        sendMsg:function(){
            //发送短信，待实现
        }
    },CPECommentUrge.Util);
})(jQuery,window));