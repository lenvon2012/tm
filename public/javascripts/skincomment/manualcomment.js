
var TM = TM || {};
((function ($, window) {
    TM.ManualComment = TM.ManualComment || {};

    var ManualComment = TM.ManualComment;

    ManualComment.Init = ManualComment.Init || {};
    ManualComment.Init = $.extend({
        init : function(container){
            $.get("/items/tradeUpdated",function(data){
                if(!data){
                    TM.Alert.load("亲，数据加载出错了，请重试或联系客服");
                } else if(data == "亲，好评助手正在为您进行订单同步，这大约需要1小时左右，请耐心等待"){
                    TM.Alert.load(data);
                }
            });
            TM.ManualComment.Event.setStaticEvent();
            TM.ManualComment.Util.setModelContent();

//            container.find(".date-picker").datepicker();

            container.find(".start-time-text").datepicker({
                maxDate : "d",
                onClose : function(selectedDate) {
                    if(selectedDate != null && selectedDate.length > 0){
                        $(".end-time-text").datepicker("option", "minDate", selectedDate);
                    }
                }}
            );
            container.find(".end-time-text").datepicker({
                maxDate : "d",
                onClose : function(selectedDate) {
                    if(selectedDate != null && selectedDate.length > 0){
                        $(".start-time-text").datepicker("option", "maxDate", selectedDate);
                    }
                }}
            );
        }
    },ManualComment.Init);

    ManualComment.TradeSearch = ManualComment.TradeSearch || {};
    ManualComment.TradeSearch  = $.extend({
        search : function(){
            var ruleData = ManualComment.TradeSearch.getQueryRule();
            $(".trade-search-pagging").tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount: 1,
                ajax: {
                    on: true,
                    param: ruleData,
                    dataType: 'json',
                    url: '/SkinComment/queryTradeRecord',
                    callback: function(dataJson){
                        if(!dataJson || !dataJson.isOk){
                            TM.Alert.load("数据获取发生错误，请重试或联系客服");
                        } else {
                            // empty the old trade-search-table-tbody content
                            $('.skincomment-table-tbody').remove();
                            if(dataJson.res.length > 0){
                                $('.skincomment-trade-search-table').append(TM.ManualComment.TradeSearch.createTbody(dataJson.res));
                            } else {
                            	$('.skincomment-trade-search-table').append('<tbody class="skincomment-table-tbody" ><tr><td colspan="6" style="height:40px;">亲，暂无数据哦</td></tr></tbody>');
                            }
                        }
                    }
                }

            });
        },
        getQueryRule : function(){
            var ruleData = {};
            var type = $('.trade-type span.selected').find('label').attr('type');
            ruleData.type = type;
            var tradeId = $('.trade-id-text').val();
            ruleData.tradeId = tradeId;
            var buyerNick = $('.buyer-name-text').val();
            ruleData.buyerNick = buyerNick;
            var startTime = $('.start-time-text').val();
            ruleData.startTime = startTime;
            var endTime = $('.end-time-text').val();
            ruleData.endTime = endTime;
            return ruleData;
        },
        createTbody : function(results) {
            var tbody = $('<tbody class="skincomment-table-tbody" ></tbody>');
            $(results).each(function(i,result){
                var rate,rate_content;
                var buyer_rate_time;
                var expire = '';
                var created = '[创建]'+ new Date(result.order.created).format("yy-MM-dd hh:mm:ss")+'<br>';
                if(!result.tradeRate || result.tradeRate == null){
                    rate = "/";
                    rate_content = "暂无评价";
                    buyer_rate_time = "";
                    if (result.order != null && result.order.sellerRate == false) {
                    	expire = '[到期]' + TM.ManualComment.TradeSearch.genRateDeadlineTime(result.order.endTime);
                    }
                } else {
                    rate = TM.ManualComment.Util.createRateImg(result.tradeRate.rate);
                    rate_content = result.tradeRate.content;
                    if(rate_content == '' || rate_content == null) {
                    	rate_content = result.tradeRate.reply;
                    }
                    buyer_rate_time = '[评价]'+new Date(result.tradeRate.created).format("yy-MM-dd hh:mm:ss") + '<br>';
                }
                var phone = result.order.phone ? (result.order.phone + "<br>") : "";
                tbody.append($('<tr buyerNick="'+result.order.buyerNick+'" tid="'+result.order.tid+'" oid="'+result.order.oid+'">' +
                    '<td style=""><input style="width:13px;" type="checkbox" tag="subCheck" class="subCheck"></td>'+
                    '<td>'+TM.ManualComment.TradeSearch.createImg(result)+'</td>'+
                    '<td classs="detail"><div title="'+result.order.title+'">'+TM.ManualComment.TradeSearch.createTitle(result)+'</div></td>'+
                    '<td class="detail"><span style="color:blue">[订单号]'+result.order.tid + '</span><br>' + created+buyer_rate_time+expire+'</td>'+
                    '<td class="detail">'+rate+'</td>'+
                    //'<td style=";"></td>'+
                    '<td class="detail">'+phone+result.order.buyerNick+TM.ManualComment.Util.createBuyerWangWang(result.order.buyerNick)+'</td>'+
                    '</tr>'
                    ));
//                    + '<tr style="height:60px;"><td colspan="1" style="color:#005580">评价内容:</td><td colspan="3"><div style="" title="'
//                        +rate_content+'"><span style="overflow:hidden;height:60px;text-overflow:ellipsis;-o-text-overflow:ellipsis;-moz-binding: url(ellipsis.xml#ellipsis);">'+rate_content+'</span></div></td></tr>'
                tbody.find('.subCheck').click(function(){
                    var $subBox = $("input[tag='subCheck']");
                    $(".checkAll").attr("checked",$subBox.length == $("input[tag='subCheck']:checked").length ? true : false);
                });
            });

            return tbody;
        },
        createTitle : function(result){
            var href = "http://item.taobao.com/item.htm?id=" + result.order.numIid;
            return '<a target="_blank" href="'+href+'">'+result.order.title+'</a>';
        },
        createImg : function(result) {
            var href = "http://item.taobao.com/item.htm?id=" + result.order.numIid;
            return '<div ><a target="_blank" href="'+href+'"><img class="imgborder"  style="width: 60px;height: 60px;" src="'+result.order.picPath+'_80x80.jpg" title="'+result.order.title+'" /></a></div>';
        },
        genRateDeadlineTime : function(time){
            return new Date(time + 15*24*60*60*1000).format("yy-MM-dd hh:mm:ss")
        }
    },ManualComment.TradeSearch);

    ManualComment.Event = ManualComment.Event || {};
    ManualComment.Event = $.extend({
        setStaticEvent : function(){
            TM.ManualComment.Event.setModelStatusClickEvent();
            TM.ManualComment.Event.setTradeTypeClickEvent();
            TM.ManualComment.Event.setTradeSearchBtnClickEvent();
            TM.ManualComment.Event.setCheckAllClickEvent();
            TM.ManualComment.Event.setBatchSelectedRateBtnClickEvent();
            TM.ManualComment.Event.setBatchPageRateBtnClickEvent();
            TM.ManualComment.Event.setBatchAllRateBtnClickEvent();
        },
        setModelStatusClickEvent : function(){
            $('.modelStatus').click(function(){
                if($(this).text() == "隐藏模板>>") {
                    $(this).text("展开模板<<");
                    $('.commentModelsArea').fadeOut(1000);
                } else {
                    $(this).text("隐藏模板>>");
                    $('.commentModelsArea').fadeIn(1000);
                }
            });
        },
        setTradeTypeClickEvent : function(){
            $('.trade-type').find('span').click(function(){
                if(!$(this).hasClass("selected")){
                    // update tmnav selected type
                    $('.trade-type').find('.selected').removeClass('selected');
                    $(this).addClass("selected");
                    // init the search param to original
                    TM.ManualComment.Util.initSearchParams();
                    // update the trade-search-table-tbody content
                    TM.ManualComment.TradeSearch.search();
                }
            });
            $('.trade-type').find('span').eq(0).trigger("click");
        },
        setTradeSearchBtnClickEvent : function() {
            $('.search-trade-btn').click(function(){
                $('.checkAll').attr("checked",false);
                TM.ManualComment.TradeSearch.search();
            });
        },
        setCheckAllClickEvent : function(){
            $(".checkAll").click(function(){
                $('input[tag="subCheck"]').attr("checked",this.checked);
            });
        },
        setBatchSelectedRateBtnClickEvent : function(){
            $('.batch-selected-rate-btn').click(function(){
                var oidstidsbuyers = "";
                var content = $('.comment-content').val();
                if($('.subCheck:checked').length == 0) {
                    TM.Alert.load("亲，请先选择要评价的订单");
                } else if(content == ""){
                    TM.Alert.load("评价内容不能为空，请选择模板或手动添加");
                } else {
                    $('.subCheck:checked').each(function(){
                        var oidtidbuyer = $(this).parent().parent().attr('tid') + "#@!" + $(this).parent().parent().attr('oid') + "#@!" + $(this).parent().parent().attr('buyerNick');
                        oidstidsbuyers += oidtidbuyer + "!@#";
                    });
                    var result = $('input[name="ratetype"]:checked').attr("value");
                    $.post("/SkinComment/manualBatchComment",{oidstidsbuyers:oidstidsbuyers,content:content,result:result},function(data){
                        if(!data){
                            TM.Alert.load("评价出错啦，请重试或联系客服");
                        } else {
                            TM.ManualComment.Util.showRateResult(data);
                        }
                    });
                }
            });
        },
        setBatchPageRateBtnClickEvent : function(){
            $('.batch-page-rate-btn').click(function(){
                var oidstidsbuyers = "";
                var content = $('.comment-content').val();
                if(content == ""){
                    TM.Alert.load("评价内容不能为空，请选择模板或手动添加");
                } else if($('.subCheck').length == 0) {
                    TM.Alert.load("亲，当前页面没有可选订单哦");
                } else {
                    $('.subCheck').each(function(){
                        var oidtidbuyer = $(this).parent().parent().attr('tid') + "#@!" + $(this).parent().parent().attr('oid') + "#@!" + $(this).parent().parent().attr('buyerNick');
                        oidstidsbuyers += oidtidbuyer + "!@#";
                    });
                    var result = $('input[name="ratetype"]:checked').attr("value");
                    $.post("/SkinComment/manualBatchComment",{oidstidsbuyers:oidstidsbuyers,content:content,result:result},function(data){
                        if(!data){
                            TM.Alert.load("评价出错啦，请重试或联系客服");
                        } else {
                            TM.ManualComment.Util.showRateResult(data);
                        }
                    });
                }
            });
        },
        setBatchAllRateBtnClickEvent : function(){
            $('.batch-all-rate-btn').click(function(){
                var content = $('.comment-content').val();
                if(content == ""){
                    TM.Alert.load("评价内容不能为空，请选择模板或手动添加");
                } else {
                    var result = $('input[name="ratetype"]:checked').attr("value");
                    $.post("/SkinComment/manualCommentAll",{interval:30,content:content,result:result},function(data){
                        if(!data){
                            TM.Alert.load("评价出错啦，请重试或联系客服");
                        } else {
                            TM.ManualComment.Util.showRateResult(data);
                        }
                    });
                }
            });
        }
    },ManualComment.Event);

    ManualComment.Util = ManualComment.Util || {};
    ManualComment.Util = $.extend({
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
        initSearchParams : function(){
            $('.trade-id-text').val("");
            $('.buyer-name-text').val("");
            $('.start-time-text').val("");
            $('.end-time-text').val("");
            $('.checkAll').attr("checked",false);

            var now = new Date();
            var lastMonth = new Date();
            lastMonth.setDate(now.getDate() - 30);
            $(".start-time-text").val(lastMonth.format("yyyy-MM-dd"));
            $(".end-time-text").val(now.format("yyyy-MM-dd"));
        },
        createBuyerWangWang : function(buyerNick){
            return '<a class="buyer-wangwang" target="_blank" href="http://amos.im.alisoft.com/msg.aw?v=2&uid='+encodeURI(buyerNick)+'&site=cntaobao&s=1&charset=utf-8">'+
                '<img class="wwimg" border="0" src="http://amos.alicdn.com/online.aw?v=2&uid='+encodeURI(buyerNick)+'&site=cntaobao&s=2&charset=utf-8" alt="联系买家" /> '+
                '</a>';
        },
        createRateImg : function(rate){
            var real_rate = rate & 3;
            var rate_value;
            if(real_rate == 2) {
                rate_value = "buyer-netural-rate";
            } else if(real_rate == 3) {
                rate_value = "buyer-bad-rate";
            } else {
                rate_value = "buyer-good-rate";
            }
            return '<div style="margin:auto" class="rate-flower-img '+rate_value+'"></div>';
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
                     var rate = TM.ManualComment.Util.getRateResult(failOrder.result);
                     html += '<tr>' +
                         '<td>'+failOrder.tid+'</td>'+
                         '<td>'+failOrder.oid+'</td>'+
                         '<td>'+failOrder.buyerNick+'</td>'+
                         '<td>'+TM.ManualComment.Util.createRateImg(rate)+'</td>'+
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
    }
    },ManualComment.Util);
})(jQuery,window));