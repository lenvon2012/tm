var TM = TM || {};
TM.Article = TM.Article || {};
TM.Loading = TM.Loading || {};

TM.Loading.init = $.extend({
    //显示加载，opts是选项，暂时没用到
    show: function() {
        var scrollLeft = $(document).scrollLeft();
        var scrollTop = $(document).scrollTop();
        var left = ($(window).width() - $(".WaitingForLoadingDiv").width())/2 + scrollLeft;
        var top = ($(window).height() - $(".WaitingForLoadingDiv").height())/2 + scrollTop;
        $(".WaitingForLoadingDiv").css("left", left+"px");
        $(".WaitingForLoadingDiv").css("top", top+"px");
        $(".WaitingForLoadingDiv").show();
    },
    hidden: function() {
        $(".WaitingForLoadingDiv").hide();
    }
}, TM.Loading.init);

$(document).ajaxStart(function(){
    TM.Loading.init.show();
});
$(document).ajaxStop(function(){
    TM.Loading.init.hidden();
});

$(document).ready(function(){
    TM.KefuSweety.Init.doInit($('#user'));
});
TM.Article.renderList = function(list){
    $.each(list, function(i, elem){
        elem.wangwang = "" + encodeURI(elem.nick) + "";
        elem.wwHref =  "http://www.taobao.com/webww/ww.php?ver=3&touid="+elem.wangwang+"&siteid=cntaobao&status=1&charset=utf-8";
        elem.wwImg = "http://amos.alicdn.com/online.aw?v=2&uid="+elem.wangwang+"&site=cntaobao&s=2&charset=UTF-8";
        elem.goodRate = elem.willGoodRate?"<b class='green'>是</b>":"<b class='red'>否</b>";
        elem.enterAdminUrl = "http://chedao.taovgo.com/tm/name?name="+elem.wangwang;
    });
}
Date.prototype.format = function(format) //author: meizz
{
    var o = {
        "M+" : this.getMonth()+1, //month
        "d+" : this.getDate(),    //day
        "h+" : this.getHours(),   //hour
        "m+" : this.getMinutes(), //minute
        "s+" : this.getSeconds(), //second
        "q+" : Math.floor((this.getMonth()+3)/3),  //quarter
        "S" : this.getMilliseconds() //millisecond
    }
    if(/(y+)/.test(format)) format=format.replace(RegExp.$1,
        (this.getFullYear()+"").substr(4 - RegExp.$1.length));
    for(var k in o)if(new RegExp("("+ k +")").test(format))
        format = format.replace(RegExp.$1,
            RegExp.$1.length==1 ? o[k] :
                ("00"+ o[k]).substr((""+ o[k]).length));
    return format;
}
Date.prototype.formatYMSH = function(){
    return this.format('yyyyMMddhh');
}

Date.prototype.formatYMS = function(){
    return this.format('yyyy-MM-dd');
}

Date.prototype.formatYMSHMS = function(){
    return this.format('yyyy-MM-dd hh:mm:ss');
}


















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
                if(TM.isCPEctocystAdmin()) {
                    container.find('.tmstaff-allocate').show();
                }
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
                            $.cookie("CPEctocyst-Staff-Role",data.role,{expires: 7, path:'/'});
                            $.cookie("login-user",data.name,{expires: 7, path:'/'});
                            $.cookie("wangwang1",data.wangwang1,{expires: 7,path:'/'});
                            $.cookie("wangwang2",data.wangwang2,{expires: 7,path:'/'});
                            $.cookie("wangwang3",data.wangwang3,{expires: 7,path:'/'});
                            TM.Alert.loadDetail("用户登陆成功", 400, 300, function(){
                                location.reload();
                            });
                        }
                    });
                }, "登陆");
            });
            container.find('.relogin').click(function(){
                var html = '' +
                    '<table style="text-align: center;width: 90%;">' +
                    '   <tbody>' +
                    '       <tr style="height: 40px;">' +
                    '           <td style="width: 30%;"><span>用户名:</span></td>' +
                    '           <td style="width: 70%;"><input type="text" class="user-name" style="width: 250px;"/></td>' +
                    '       </tr>' +
                    '       <tr style="height: 40px;">' +
                    '           <td><span>密码:</span></td>' +
                    '           <td><input type="password" class="password"  style="width: 250px;"/></td>' +
                    '       </tr>' +
                    '   </tbody>' +
                    '</table>';
                var obj = $(html);
                obj.find('.password').keydown(function(e){
                    var key = (e.keyCode) || (e.which) || (e.charCode);
                    if(key == "13") {
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
                        $.post('/CPEctocystLogin/login',{username:name,password:password},function(data){
                            if(data === undefined || data == null){
                                TM.Alert.load("用户登陆失败，请重试或联系客服~");
                            } else if(data == '用户不存在'){
                                TM.Alert.load("用户不存在或密码错误~");
                            } else {
                                $.cookie("CPEctocyst-Staff-Role",data.role,{expires: 7, path:'/'});
                                $.cookie("login-user",data.name,{expires: 7,path:'/'});
                                TM.Alert.loadDetail("用户登陆成功", 400, 300, function(){
                                    if(TM.isCPEctocystAdmin()) {
                                        location.reload();
                                    } else {
                                        window.location = '/CPEctocyst/sellerAdmin';
                                    }
                                });
                            }
                        });
                    }
                });
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
                    $.post('/CPEctocystLogin/login',{username:name,password:password},function(data){
                        if(data === undefined || data == null){
                            TM.Alert.load("用户登陆失败，请重试或联系客服~");
                        } else if(data == '用户不存在'){
                            TM.Alert.load("用户不存在或密码错误~");
                        } else {
                            $.cookie("CPEctocyst-Staff-Role",data.role,{expires: 7, path:'/'});
                            $.cookie("login-user",data.name,{expires: 7,path:'/'});
                            TM.Alert.loadDetail("用户登陆成功", 400, 300, function(){
                                if(TM.isCPEctocystAdmin()) {
                                    location.reload();
                                } else {
                                    window.location = '/CPEctocyst/sellerAdmin';
                                }
                            });
                        }
                    });
                }, "登陆");
            });

            container.find('.edit-password').click(function(){
                var html = '' +
                    '<table style="text-align: left;width: 95%;">' +
                    '   <tbody style="text-align: center;">' +
                    '       <tr style="height: 50px;line-height: 50px;">' +
                    '           <td style="width: 30%;"><span>您的用户名:</span></td>' +
                    '           <td style="width: 70%;"><input type="text" class="user-name" style="width: 250px;" disabled="disabled" value="'+ $.cookie('login-user')+'"/></td>' +
                    '       </tr>' +
                    '       <tr style="height: 50px;line-height: 50px;">' +
                    '           <td><span>原始密码:</span></td>' +
                    '           <td><input type="password" class="password"  style="width: 250px;"/></td>' +
                    '       </tr>' +
                    '       <tr class="modify-password-row" style="height: 50px;line-height: 50px;">' +
                    '           <td><span style="color: blueviolet;">新密码:</span></td>' +
                    '           <td><input type="password" class="newpassword"  style="width: 250px;"/></td>' +
                    '       </tr>' +
                    '       <tr class="modify-password-row" style="height: 50px;line-height: 50px;">' +
                    '           <td><span style="color: blueviolet;">确认新密码:</span></td>' +
                    '           <td><input type="password" class="newpassword-confirm"  style="width: 250px;"/></td>' +
                    '       </tr>' +
                    '   </tbody>' +
                    '</table>' +
                    '';
                var obj = $(html);
                obj.find('.newpassword-confirm').keydown(function(e){
                    var key = (e.keyCode) || (e.which) || (e.charCode);
                    if(key == "13") {
                        var name = obj.find('.user-name').val();
                        if(name === undefined || name == ''){
                            alert("亲，用户名不能为空哦");
                            return false;
                        }
                        var password = obj.find('.password').val();
                        if(password === undefined || password == ''){
                            alert("亲，请输入密码");
                            return false;
                        }

                        var newpassword = obj.find('.newpassword').val();
                        if (newpassword === undefined || newpassword == '') {
                            alert("亲，新密码不能为空哦");
                            return false;
                        }
                        var newpasswordconfirm = obj.find('.newpassword-confirm').val();
                        if (newpasswordconfirm === undefined || newpasswordconfirm == '') {
                            alert("亲，请确认密码");
                            return false;
                        }
                        if (newpassword != newpasswordconfirm) {
                            alert("亲，新输入密码不一致，请确认后提交~");
                            return false;
                        }

                        var email = obj.find('.email').val();
                        var phone = obj.find('.phone').val();
                        $.post('/CPEctocystLogin/editUser',{username:name,password:password, newPassword:newpassword},function(data){
                            if(data === undefined || data == null){
                                TM.Alert.load("用户登陆失败，请重试或联系客服~");
                            } else if(data == '用户不存在'){
                                TM.Alert.load("用户不存在或密码错误~");
                            } else {
                                $.cookie("CPEctocyst-Staff-Role",data.role,{expires: 7, path:'/'});
                                $.cookie("login-user",data.name,{expires: 7,path:'/'});
                                $.cookie("wangwang1",data.wangwang1,{expires: 7,path:'/'});
                                $.cookie("wangwang2",data.wangwang2,{expires: 7,path:'/'});
                                $.cookie("wangwang3",data.wangwang3,{expires: 7,path:'/'});
                                TM.Alert.loadDetail("修改密码成功", 400, 300, function(){
                                    location.reload();
                                });
                            }
                        });
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
                        alert("亲，请输入密码");
                        return false;
                    }

                    var newpassword = obj.find('.newpassword').val();
                    if (newpassword === undefined || newpassword == '') {
                        alert("亲，新密码不能为空哦");
                        return false;
                    }
                    var newpasswordconfirm = obj.find('.newpassword-confirm').val();
                    if (newpasswordconfirm === undefined || newpasswordconfirm == '') {
                        alert("亲，请确认密码");
                        return false;
                    }
                    if (newpassword != newpasswordconfirm) {
                        alert("亲，新输入密码不一致，请确认后提交~");
                        return false;
                    }

                    var email = obj.find('.email').val();
                    var phone = obj.find('.phone').val();
                    $.post('/CPEctocystLogin/editUser',{username:name,password:password, newPassword:newpassword},function(data){
                        if(data === undefined || data == null){
                            TM.Alert.load("用户登陆失败，请重试或联系客服~");
                        } else if(data == '用户不存在'){
                            TM.Alert.load("用户不存在或密码错误~");
                        } else {
                            $.cookie("CPEctocyst-Staff-Role",data.role,{expires: 7, path:'/'});
                            $.cookie("login-user",data.name,{expires: 7,path:'/'});
                            $.cookie("wangwang1",data.wangwang1,{expires: 7,path:'/'});
                            $.cookie("wangwang2",data.wangwang2,{expires: 7,path:'/'});
                            $.cookie("wangwang3",data.wangwang3,{expires: 7,path:'/'});
                            TM.Alert.loadDetail("修改密码成功", 400, 300, function(){
                                location.reload();
                            }, "成功");
                        }
                    });
                }, "登陆");
            });

            container.find('.logout').click(function(){
                if(confirm("确定退出当前登陆？")){
                    $.cookie("CPEctocyst-Staff-Role",null,{expires: 7, path:'/'});
                    $.cookie('login-user',null,{expires: 7,path:'/'});
                    $.cookie("wangwang1",null,{expires: 7, path:'/'});
                    $.cookie('wangwang2',null,{expires: 7,path:'/'});
                    $.cookie('wangwang3',null,{expires: 7,path:'/'});
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
                    $.post('/CPEctocystLogin/register',{username:name,password:password},function(data){
                        if(data === undefined || data == null){
                            TM.Alert.load("用户注册失败，请重试或联系客服~");
                            return false;
                        } else if(data == '用户名已存在'){
                            TM.Alert.load(data);
                            return false;
                        } else {
                            $.cookie("CPEctocyst-Staff-Role",data.role,{expires: 7, path:'/'});
                            $.cookie("login-user",data.name,{expires: 7, path:"/"});
                            $.cookie('wangwang1',data.wangwang1,{expires: 7,path:'/'});
                            $.cookie('wangwang2',data.wangwang2,{expires: 7,path:'/'});
                            $.cookie('wangwang3',data.wangwang3,{expires: 7,path:'/'});
                            TM.Alert.loadDetail("用户注册成功", 400, 300, function(){
                                location.reload();
                            });
                            return true;
                        }
                    });
                }, "注册");
            });
        },
        setWangWangNickKeyDown : function(obj,target){
            obj.keydown(function(e) {
                var key = (e.keyCode) || (e.which) || (e.charCode);
                if (key == "13") {//keyCode=13是回车键
                    var index = target.find('tr').length + 1;
                    var newTr = $('<tr style="height: 50px;line-height: 50px;"><td style="width: 30%;"><span>卖家旺旺'+index+':</span></td><td style="width: 70%;"><input type="text" class="wangwang-nick" style="width: 250px;"/></td></tr>');
                    target.append(newTr);
                    target.find('.wangwang-nick:last-child').focus();
                    KefuSweety.Event.setWangWangNickKeyDown(newTr.find('.wangwang-nick'), target);
                }
            });
        }
    },KefuSweety.Event)
})(jQuery,window));