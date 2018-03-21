
var TM = TM || {};
((function ($, window) {
    TM.TBLMUser = TM.TBLMUser || {};
    var TBLMUser = TM.TBLMUser;
    TBLMUser.Event = TBLMUser.Event || {};
    var t;
    TBLMUser.Init = TBLMUser.Init || {};
    TBLMUser.Init = $.extend({
        doInit : function(){

            TBLMUser.Init.initUserInfo();
            TBLMUser.Event.setEvent();
        },
        initUserInfo : function(){
            if($.cookie("tblmusername") != null){
                $('#s_username_top').text($.cookie("tblmusername"));
                $('#u_login').hide();
                $('#u_logout').show();
            }else{
                //alert("no user")
            }
        }
    },TBLMUser.Init);

    TBLMUser.util = $.extend({
        setUserCookie : function(data){
            $.cookie("tblmusername",data.name,{expires: 7});
            $.cookie("tblmuserscore",data.score,{expires: 7});
            $.cookie("tblmuseremail",data.email,{expires: 7});
            $.cookie("tblmuserphone",data.phone,{expires: 7});
        },
        clearUserCookie : function(){
            $.cookie("tblmusername",null);
            $.cookie("tblmuserscore",null);
            $.cookie("tblmuseremail",null);
            $.cookie("tblmuserphone",null);
        },
        initCopy : function(id,text){
            ZeroClipboard.setMoviePath("/js/utils/copy.swf");
            var clip = new ZeroClipboard.Client();
            clip.setHandCursor(true);
            clip.setText('');
            clip.addEventListener('mouseDown', function (client) {
                clip.setText(text);
            });
            clip.glue(id);
        }
    },TBLMUser.util);

    TBLMUser.Event = $.extend({
        setEvent : function(){
            TBLMUser.Event.setUserHoverEvent();
            TBLMUser.Event.setUserMenuHoverEvent();
            TBLMUser.Event.setUserRegisterClickEvent();
            TBLMUser.Event.setUserLoginClickEvent();
            TBLMUser.Event.setUserLogoutClickEvent();
            TBLMUser.Event.setUserMenuClickEvent();
            TBLMUser.Event.setInviteClickEvent();
        },
        setUserHoverEvent : function(){
            $('#s_username_top').hover(
                function(){
                    if($('#s_username_top').text() != "游客") {
                        clearTimeout(t);
                        $('#s_username_menu').fadeIn(500);
                    }
                },
                function(){
                    t=setTimeout(function(){
                        $('#s_username_menu').fadeOut(500);
                    },200)
                }
            );
        },
        setUserMenuHoverEvent : function(){
            $('#s_username_menu').hover(
                function(){
                    clearTimeout(t);
                },
                function(){
                    t=setTimeout(function(){
                        $('#s_username_menu').fadeOut(500);
                    },200)
                }
            );
        },
        setUserRegisterClickEvent : function() {
            $('#u_register').click(function(){
                var registerWindow = $('#registerWindow');
                registerWindow.empty();
                var htmls = [];
                htmls.push("<form id='registerWindow' style='text-align: center;' ><table style='margin: 0 auto;'>");
                htmls.push("<tr><td style='text-align: right;'>用户名</td><td><input type='text' class='userName'></td><td class='inputtip'>请输入用户名</td></tr>");
                htmls.push("<tr><td style='text-align: right;'>密码</td><td><input type='password' class='password'></td><td class='inputtip'>请输入密码</td></tr>");
                htmls.push("<tr><td style='text-align: right;'>确认密码</td><td><input type='password' class='passwordcheck'></td><td class='inputtip'>确认密码</td></tr>");
                htmls.push("<tr><td style='text-align: right;'>邮箱</td><td><input type='text' class='email'></td><td class='inputtip'>可不填</td></tr>");
                htmls.push("<tr><td style='text-align: right;'>电话</td><td><input type='text' class='phone'></td><td class='inputtip'>可不填</td></tr>");
                var register = $(htmls.join(''));

                TM.Alert.loadDetail(register,400,270,function(){
                    if(register.find('.password').val() != register.find('.passwordcheck').val()) {
                        TM.Alert.load("密码不一致，请检查");
                        return false;
                    }
                    var username = register.find('.userName').val();
                    var password = register.find('.password').val();
                    var email = register.find('.email').val();
                    var phone = register.find('.phone').val();
                    $.post('/TBLMUserUI/register',{username:username, password:password, email:email, phone:phone, fromname:$.cookie("_in")}, function(data){
                        if (data == "用户名已存在") {
                            TM.Alert.load("用户名已存在，请更换用户名");
                        } else {
                            TBLMUser.util.setUserCookie(data);
                            location.reload();
                        }
                    })
                },"新用户注册");
            });
        },
        setUserLoginClickEvent : function(){
            $('#u_login').click(function(){
                var loginWindow = $('#loginWindow');
                loginWindow.empty();
                var htmls = [];
                htmls.push("<form id='loginWindow' style='text-align: center;' ><table style='margin: 0 auto;'>");
                htmls.push("<tr><td style='text-align: right;'>用户名</td><td><input type='text' class='userName'></td><td class='inputtip'>请输入用户名</td></tr>");
                htmls.push("<tr><td style='text-align: right;'>密码</td><td><input type='password' class='password'></td><td class='inputtip'>请输入密码</td></tr>");
                var login = $(htmls.join(''));

                TM.Alert.loadDetail(login,400,270,function(){
                    var username = login.find('.userName').val();
                    var password = login.find('.password').val();
                    $.post('/TBLMUserUI/login',{username:username, password:password}, function(data){
                        if(data == "用户不存在"){
                            TM.Alert.load("不存在该用户名");
                        } else {
                            TBLMUser.util.setUserCookie(data);
                            location.reload();
                        }
                    })
                },"用户登陆");
            });
        },
        setUserLogoutClickEvent : function(){
            $('#u_logout').click(function(){
               TBLMUser.util.clearUserCookie();
                location.reload();
            });
        },
        setUserMenuClickEvent : function(){
            $('#s_username_menu .sep').click(function(){
                TBLMUser.util.clearUserCookie();
                location.reload();
            });
            $('#s_username_menu .userInf').click(function(){
                var userInfWindow = $('#userInfWindow');
                userInfWindow.empty();
                var htmls = [];
                htmls.push("<form id='userInfWindow' style='text-align: center;' ><table style='margin: 0 auto;'>");
                htmls.push("<tr><td>用户名:</td><td>"+$.cookie('tblmusername')+"</td></tr>");
                htmls.push("<tr><td>用户邮箱:</td><td>"+$.cookie('tblmuseremail')+"</td></tr>");
                htmls.push("<tr><td>用户电话:</td><td>"+$.cookie('tblmuserephone')+"</td></tr>");
                htmls.push("<tr><td>已获取积分:</td><td>"+$.cookie('tblmuserscore')+"</td></tr>");
                var userInf = $(htmls.join(''));

                TM.Alert.loadDetail(userInf,400,270,null,"用户信息");
            });
        },
        setInviteClickEvent : function(){
            $('#s_username_menu .invite').click(function(){
                $.post('/TBLMUserUI/genTBLMInviteUrl',{name:$.cookie("tblmusername")},function(data){
                    var inviteWindow = $('#inviteWindow');
                    inviteWindow.empty();
                    var htmls = [];
                    htmls.push("<form id='inviteWindow' style='text-align: center;' ><table style='margin: 0 auto;'>");
                    htmls.push("<tr><td style='text-align: left;'>这是您的专属邀请链接，每邀请一个用户都可以获取一定积分，积分可以在淘宝联盟内部换取高级功能或者现金</td></tr>");
                    htmls.push("<tr><td style='text-align: right;'><textarea id='inviteContent' style='width: 100%;height: 60px;overflow: hidden;'>这是我见过最好的淘宝查询神器,隐形降权、买家信誉、对手销量轻松获取!"+data.url+"</textarea></td></tr>");
                    htmls.push("<tr><td style='text-align: left;'><span id='paste' class='baseblock bigblock' style='width: 60px;height: 40px;line-height:40px;font-size: 22px;text-align: center;'>复制</span></td></tr>");
                    var invite = $(htmls.join(''));

                    TM.Alert.loadDetail(invite,500,370,function(){

                    },"用户邀请");
                    TBLMUser.util.initCopy('paste',invite.find('#inviteContent').text());
                    /*invite.find('#paste').click(function(){
                        ZeroClipboard.setMoviePath("/js/utils/copy.swf");
                        var clip = new ZeroClipboard.Client();
                        clip.setHandCursor(true);
                        clip.setText(invite.find('#inviteContent').text());
                        alert(clip.clipText)
                    });*/
                });

            });
        }
    },TBLMUser.Event);
})(jQuery,window));
