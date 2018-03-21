

var TM = TM || {};

((function ($, window) {

    TM.CommentAdmin = TM.CommentAdmin || {};
    var CommentAdmin = TM.CommentAdmin;

    CommentAdmin.init = CommentAdmin.init || {};
    CommentAdmin.init = $.extend({

        doInit: function(container) {

            CommentAdmin.container = container;

            CommentAdmin.init.initComment();
            CommentAdmin.Event.setStaticEvent();

        },
        getContainer: function() {
            return CommentAdmin.container;
        },
        initComment: function(){
            CommentAdmin.init.initIsCommentOpen();
            CommentAdmin.init.initCommentConf();
            CommentAdmin.init.initCommentLog();
        },
        initIsCommentOpen: function(){
            $.get("/AutoComments/isOn", function(data){
                if(data === undefined || data == null) {
                    CommentAdmin.Util.setAutoCommentOff();
                    return;
                }
                if(data.res == true) {
                    CommentAdmin.Util.setAutoCommentOn();
                    return;
                }
                CommentAdmin.Util.setAutoCommentOff();
            });
        },
        initCommentConf: function(){
            $.get("/skincomment/commentConf", function(data){
                if(data === undefined || data == null) {
                    CommentAdmin.Util.setDefaultCommentConf();
                    return;
                }
                if(parseInt(data.commentType) == 2) {
                    CommentAdmin.container.find('input[name="comment-time-config"][value=2]').trigger('click');
                }
                CommentAdmin.container.find('.comment-before-deadline-days').val(data.commentDays);
                CommentAdmin.container.find('.comment-content').val(data.randomComment);
            })
        },
        initCommentLog: function(){
            CommentAdmin.container.find('.comment-admin-log-paging').tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount:1,
                ajax: {
                    param : {},
                    on: true,
                    dataType: 'json',
                    url: "/AutoComments/getNewAutoTitleCommentLog",
                    callback:function(data){
                        if(data == undefined || data == null) {
                            return;
                        }
                        if(data.res.length > 0) {
                            //CommentAdmin.container.find('.comment-admin-log-table tbody .no-log-tr').remove();
                            $(data.res).each(function(i, comment){
                                if(comment.result == "good") {
                                    comment.resultPic =  "goodResult";
                                } else if(comment.result == "neutral") {
                                    comment.resultPic =  "neutralResult";
                                } else {
                                    comment.resultPic =  "badResult";
                                }
                                comment.commentTs = new Date(comment.ts).format("yy-MM-dd hh:mm:ss");
                            });
                            CommentAdmin.container.find('.comment-admin-log-table tbody').empty();
                            CommentAdmin.container.find('.comment-admin-log-table tbody').append($('#comment-admin-log-table-tr').tmpl(data.res));
                        }
                    }
                }
            });
        }
    }, CommentAdmin.init);


    CommentAdmin.Util = CommentAdmin.Util || {};
    CommentAdmin.Util = $.extend({
        setAutoCommentOff: function(){
            var $this =  CommentAdmin.container.find('.comment-switch');
            $this.removeClass("comment-open");
            $this.addClass("comment-close");
            $this.removeClass("wide-yellow-btn");
            $this.addClass("long-green-btn");
            $this.text("开启自动评价");
            CommentAdmin.container.find('.comment-status').removeClass("green-bold");
            CommentAdmin.container.find('.comment-status').addClass("red-bold");
            CommentAdmin.container.find('.comment-status-text').text("尚未开启");
        },
        setAutoCommentOn: function(){
            var $this =  CommentAdmin.container.find('.comment-switch');
            $this.removeClass("comment-close");
            $this.addClass("comment-open");

            $this.removeClass("long-green-btn");
            $this.addClass("wide-yellow-btn");
            $this.text("关闭自动评价");
            CommentAdmin.container.find('.comment-status').addClass("green-bold");
            CommentAdmin.container.find('.comment-status').removeClass("red-bold");
            CommentAdmin.container.find('.comment-status-text').text("已开启");
        },
        setDefaultCommentConf: function(){
            return;
        },
        countCharacters : function(str){
            var totalCount = 0;
            for (var i=0; i<str.length; i++) {
                var c = str.charCodeAt(i);
                if ((c >= 0x0001 && c <= 0x007e) || (0xff60<=c && c<=0xff9f)) {
                    totalCount++;
                }else {
                    totalCount+=2;
                }
            }
            return totalCount;
        }
    }, CommentAdmin.Util);

    CommentAdmin.Event = CommentAdmin.Event || {};
    CommentAdmin.Event = $.extend({
        setStaticEvent: function(){
            CommentAdmin.Event.setCommentSwitchEvent();
            CommentAdmin.Event.setCommentTimeConfigTextEvent();
            CommentAdmin.Event.setSaveCommentConfigEvent();
        },
        // 自动评价开启关闭事件
        setCommentSwitchEvent: function(){
            CommentAdmin.container.find('.comment-switch').unbind("click").click(function(){
                // 如果是开启状态，则关闭
                var $this = $(this);
                if($this.hasClass("comment-open")) {
                    if(confirm("确认要关闭自动评价功能吗?")) {
                        $.get("/AutoComments/setOff", function(data){
                            if(data === undefined || data == null) {
                                TM.Alert.load("关闭自动评价失败，请重试或联系客服");
                            }
                            if(data.res == false) {
                                TM.Alert.load("关闭自动评价失败，请重试或联系客服");
                            }
                            TM.Alert.load("关闭自动评价成功", 400, 300, function(){
                                CommentAdmin.Util.setAutoCommentOff();
                            });
                        });
                    }
                } else {
                    if(confirm("确认要开启自动评价功能吗?")) {
                        $.get("/AutoComments/setOn", function(data){
                            if(data === undefined || data == null) {
                                TM.Alert.load("开启自动评价失败，请重试或联系客服");
                            }
                            if(data.res == false) {
                                TM.Alert.load("开启自动评价失败，请重试或联系客服");
                            }
                            TM.Alert.load("开启自动评价成功", 400, 300, function(){
                                CommentAdmin.Util.setAutoCommentOn();
                            });
                        });
                    }
                }
            });
        },
        // 相当于点击radio
        setCommentTimeConfigTextEvent: function(){
            CommentAdmin.container.find('.comment-time-config-text').unbind('click').click(function(){
                var tag = $(this).attr("tag");
                $(this).parent().find('input[tag="'+tag+'"]').trigger("click");
            });
        },
        // 保存自动评价配置
        setSaveCommentConfigEvent: function(){
            CommentAdmin.container.find('.save-comment-config').unbind('click').click(function(){
                var commentType, commentTime, content;
                commentType = CommentAdmin.container.find('input[name="comment-time-config"]:checked').val();
                commentTime = CommentAdmin.container.find('.comment-before-deadline-days').val();
                content = CommentAdmin.container.find('.comment-content').val();
                if(!(/^(\+|-)?\d+$/.test(commentTime)) || commentTime < 0) {
                    TM.Alert.load("抢评到期时间必须为正整数");
                    return;
                }
                if(content.indexOf("日") >= 0 || content.indexOf("草") >= 0 || content.indexOf("操") >=0 || content.indexOf("http://") >= 0) {
                    TM.Alert.load("评语不能包含网址或者'日''草''操'这些词");
                    return;
                }
                if(content.length > 252) {
                    TM.Alert.load("评语字数不能超过252，请缩短评语再提交");
                    return;
                }
                if(confirm("确认修改自动评价配置?")) {

                    $.post("/AutoComments/setNewAutoTitleCommentConf", {commentType: commentType, commentTime: commentTime, content: content}, function(data){
                        if(data === undefined || data == null) {
                            TM.Alert.load("保存配置失败，请重试或联系客服");
                            return;
                        }
                        if(data.success == false) {
                            TM.Alert.load(data.message);
                            return;
                        }
                        TM.Alert.load("保存成功", 400, 300, function(){
                            location.reload();
                        });
                    });
                }
            });
        }
    }, CommentAdmin.Event);

})(jQuery, window));
