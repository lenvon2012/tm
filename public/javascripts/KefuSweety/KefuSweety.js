var TM = TM || {};
((function ($, window) {
    TM.KefuSweety = TM.KefuSweety || {};
    var KefuSweety = TM.KefuSweety;
    KefuSweety.Init = KefuSweety.Init || {};
    KefuSweety.Init = $.extend({
        doInit : function(container){
            if(KefuSweety.Util.alredyLogin()){
                container.find('.un .sweety-name').text($.cookie('login-user'));
                container.find('.un').show();
                container.find('.logout').show();
                container.find('.relogin').show();
                container.find('.login').hide();
            }
            KefuSweety.Event.setEvent(container);
        }
    },KefuSweety.Init)

    KefuSweety.Util = KefuSweety.Util || {};
    KefuSweety.Util = $.extend({
        alredyLogin : function(){
            var loginUser = $.cookie('login-user');
            if(loginUser === undefined || loginUser == null){
                return false;
            }
            return true;
        }
    },KefuSweety.Util)

    KefuSweety.Event = KefuSweety.Event || {};
    KefuSweety.Event = $.extend({
        setEvent : function(container){
            container.find('.login').click(function(){
                var html = '' +
                    '<table style="text-align: center;width: 90%;">' +
                    '   <tbody>' +
                    '       <tr>' +
                    '           <td style="width: 30%;"><span>用户名:</span></td>' +
                    '           <td style="width: 70%;"><input type="text" class="user-name" style="width: 250px;"/></td>' +
                    '       </tr>' +
                    '       <tr>' +
                    '           <td><span>密码:</span></td>' +
                    '           <td><input type="password" class="password"  style="width: 250px;"/></td>' +
                    '       </tr>' +
                    '   </tbody>' +
                    '</table>';
                var obj = $(html);
                TM.Alert.loadDetail(obj, 500, 300, function(){
                    var name = obj.find('.user-name').val();
                    if(name === undefined || name == ''){
                        alert("亲，用户名不能为空哦");
                        return false;
                    }
                    var password = obj.find('.password').val();
                    if(password === undefined || password == ''){
                        alert("亲，密码不能为空哦");
                        return false;
                    }
                    $.post('/KefuSweetyUI/login',{username:name,password:password},function(data){
                        if(data === undefined || data == null){
                            TM.Alert.load("用户登陆失败，请重试或联系客服~");
                        } else if(data == '用户不存在'){
                            TM.Alert.load("用户不存在或密码错误~");
                        } else {
                            $.cookie("login-user",data.name,{expires: 7});
                            TM.Alert.load("用户登陆成功", 400, 300, function(){
                                location.reload();
                            });
                        }
                    });
                },"登陆");
            });
            container.find('.relogin').click(function(){
                var html = '' +
                    '<table style="text-align: center;width: 90%;">' +
                    '   <tbody>' +
                    '       <tr>' +
                    '           <td style="width: 30%;"><span>用户名:</span></td>' +
                    '           <td style="width: 70%;"><input type="text" class="user-name" style="width: 250px;"/></td>' +
                    '       </tr>' +
                    '       <tr>' +
                    '           <td><span>密码:</span></td>' +
                    '           <td><input type="password" class="password"  style="width: 250px;"/></td>' +
                    '       </tr>' +
                    '   </tbody>' +
                    '</table>';
                var obj = $(html);
                TM.Alert.loadDetail(obj, 500, 300, function(){
                    var name = obj.find('.user-name').val();
                    if(name === undefined || name == ''){
                        alert("亲，用户名不能为空哦");
                        return false;
                    }
                    var password = obj.find('.password').val();
                    if(password === undefined || password == ''){
                        alert("亲，密码不能为空哦");
                        return false;
                    }
                    $.post('/KefuSweetyUI/login',{username:name,password:password},function(data){
                        if(data === undefined || data == null){
                            TM.Alert.load("用户登陆失败，请重试或联系客服~");
                        } else if(data == '用户不存在'){
                            TM.Alert.load("用户不存在或密码错误~");
                        } else {
                            $.cookie("login-user",data.name,{expires: 7});
                            TM.Alert.load("用户登陆成功", 400, 300, function(){
                                location.reload();
                            });
                        }
                    });
                },"登陆");
            });
            container.find('.sweety-name').click(function(){
                var html = '' +
                    '<table style="text-align: left;width: 95%;">' +
                    '   <tbody>' +
                    '       <tr>' +
                    '           <td style="width: 30%;"><span>您的用户名:</span></td>' +
                    '           <td style="width: 70%;"><input type="text" class="user-name" style="width: 250px;" disabled="disabled" value="'+ $.cookie('login-user')+'"/></td>' +
                    '       </tr>' +
                    '       <tr>' +
                    '           <td><span>原始密码:</span></td>' +
                    '           <td><input type="password" class="password"  style="width: 250px;"/><span class="modify-password" style="margin-left: 20px;color: blueviolet;font-size: 16px;cursor: pointer">(修改密码)</span></td>' +
                    '       </tr>' +
                    '       <tr class="hidden modify-password-row">' +
                    '           <td><span style="color: blueviolet;">新密码:</span></td>' +
                    '           <td><input type="password" class="newpassword"  style="width: 250px;"/></td>' +
                    '       </tr>' +
                    '       <tr class="hidden modify-password-row">' +
                    '           <td><span style="color: blueviolet;">确认新密码:</span></td>' +
                    '           <td><input type="password" class="newpassword-confirm"  style="width: 250px;"/></td>' +
                    '       </tr>' +
                    '       <tr class="">' +
                    '           <td><span>邮件地址:</span></td>' +
                    '           <td><input type="text" class="email"  style="width: 250px;"/></td>' +
                    '       </tr>' +
                    '       <tr class="">' +
                    '           <td><span>电话号码:</span></td>' +
                    '           <td><input type="text" class="phone"  style="width: 250px;"/></td>' +
                    '       </tr>' +
                    '   </tbody>' +
                    '</table>' +
                    '';
                var obj = $(html);
                obj.find('.modify-password').click(function(){
                    if($(this).text() == '(修改密码)'){
                        obj.find('.modify-password-row').show();
                        $(this).text("(放弃改密)");
                    } else if($(this).text() == '(放弃改密)'){
                        obj.find('.modify-password-row').hide();
                        $(this).text("(修改密码)");
                    }
                });
                TM.Alert.loadDetail(obj, 600, 400, function(){
                    var name = obj.find('.user-name').val();
                    if(name === undefined || name == ''){
                        alert("亲，用户名不能为空哦");
                        return false;
                    }
                    var password = obj.find('.password').val();
                    if(password === undefined || password == ''){
                        alert("亲，密码不能为空哦");
                        return false;
                    }
                    if(obj.find('.modify-password-row:visible').length > 0){
                        var newpassword = obj.find('.newpassword').val();
                        if(newpassword === undefined || newpassword == ''){
                            alert("亲，新密码不能为空哦");
                            return false;
                        }
                        var newpasswordconfirm = obj.find('.newpassword-confirm').val();
                        if(newpasswordconfirm === undefined || newpasswordconfirm == ''){
                            alert("亲，请确认密码");
                            return false;
                        }
                        if(newpassword != newpasswordconfirm){
                            alert("亲，新输入密码不一致，请确认后提交~");
                            return false;
                        }
                    }
                    var email = obj.find('.email').val();
                    var phone = obj.find('.phone').val();
                    $.post('/KefuSweetyUI/editUser',{username:name,password:password, newPassword:newpassword, email:email, phone:phone},function(data){
                        if(data === undefined || data == null){
                            TM.Alert.load("用户登陆失败，请重试或联系客服~");
                        } else if(data == '用户不存在'){
                            TM.Alert.load("用户不存在或密码错误~");
                        } else {
                            $.cookie("login-user",data.name,{expires: 7});
                            TM.Alert.load("用户登陆成功", 400, 300, function(){
                                location.reload();
                            });
                        }
                    });
                },"登陆");
            });
            container.find('.logout').click(function(){
                if(confirm("确定退出当前登陆？")){
                    $.cookie('login-user',null);
                    location.reload();
                }
            });
            container.find('.register').click(function(){
                var html = '' +
                    '<table style="text-align: center;width: 90%;">' +
                    '   <tbody>' +
                    '       <tr>' +
                    '           <td style="width: 30%;"><span>用户名:</span></td>' +
                    '           <td style="width: 70%;"><input type="text" class="user-name" style="width: 250px;"/></td>' +
                    '       </tr>' +
                    '       <tr>' +
                    '           <td><span>密码:</span></td>' +
                    '           <td><input type="password" class="password"  style="width: 250px;"/></td>' +
                    '       </tr>' +
                    '       <tr>' +
                    '           <td><span>确认密码:</span></td>' +
                    '           <td><input type="password" class="password-confirm"  style="width: 250px;"/></td>' +
                    '       </tr>' +
                    '   </tbody>' +
                    '</table>';
                var obj = $(html);
                TM.Alert.loadDetail(obj, 500, 300, function(){
                    var name = obj.find('.user-name').val();
                    if(name === undefined || name == ''){
                        alert("亲，用户名不能为空哦");
                        return false;
                    }
                    var password = obj.find('.password').val();
                    if(password === undefined || password == ''){
                        alert("亲，密码不能为空哦");
                        return false;
                    }
                    var passwordConfirm = obj.find('.password-confirm').val();
                    if(passwordConfirm === undefined || passwordConfirm == ''){
                        alert("亲，请确认密码~");
                        return false;
                    }
                    if(password != passwordConfirm){
                        alert("亲，两次输入的密码不一致，请确认后提交~");
                        return false;
                    }
                    $.post('/KefuSweetyUI/register',{username:name,password:password},function(data){
                        if(data === undefined || data == null){
                            TM.Alert.load("用户注册失败，请重试或联系客服~");
                            return false;
                        } else if(data == '用户名已存在'){
                            TM.Alert.load(data);
                            return false;
                        } else {
                            $.cookie("login-user",data.name,{expires: 7});
                            TM.Alert.load("用户注册成功", 400, 300, function(){
                                location.reload();
                            });
                            return true;
                        }
                    });
                },"注册");
            });
        }
    },KefuSweety.Event)
})(jQuery,window));