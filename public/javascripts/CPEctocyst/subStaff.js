var TM = TM || {};
((function ($, window) {
    TM.SubStaff = TM.SubStaff || {};

    var SubStaff = TM.SubStaff;

    SubStaff.init = SubStaff.init || {};
    SubStaff.init = $.extend({
        doInit: function(container){
            SubStaff.container = container;

            SubStaff.Event.setStaticEvent();
        },

        addSubStaff: function(){
            var addJoinTable = $('#addSubStaffTable').tmpl();
            TM.Alert.loadDetail(addJoinTable, 500, 350, function(){
                var name = addJoinTable.find('.name').val();
                if(name === undefined || name == null || name == "") {
                    alert("亲，请输入用户名称");
                    return false;
                }
                
                var role = addJoinTable.find('.role').val();
                if(role === undefined || role == null || role == "") {
                    alert("亲，请选择权限");
                    return false;
                }

                var password = addJoinTable.find('.password').val();
                if(password === undefined || password == null || password == "") {
                    alert("亲，请输入密码");
                    return false;
                }

                var passwordConfirm = addJoinTable.find('.password-confirm').val();
                if(passwordConfirm === undefined || passwordConfirm == null || passwordConfirm == "") {
                    alert("亲，请再次输入密码");
                    return false;
                }

                if(password != passwordConfirm) {
                    alert("两次输入密码不一致，请重试");
                    return false;
                }

                var phone = addJoinTable.find('.phone').val();
                if(phone === undefined || phone == null || phone == "") {
                    alert("亲，请输入手机号");
                    return false;
                }

                $.get("/CPEctocyst/addSubStaff", {name: name, role:role, password: password, phone: phone}, function(data){
                    if(data === undefined || data == null) {
                        alert("新增用户失败, 请重试或联系客服")
                        return false;
                    }
                    if(data.success == false) {
                        alert(data.message);
                        return;
                    }
                    TM.Alert.load("新增用户成功", 400, 300, function(){
                        location.reload();
                    });
                });
            }, "新增加盟商");
        },
        search: function(searchText) {
            if(searchText === undefined || searchText == null || searchText == "请输入用户名进行搜索") {
                searchText = "";
            }
            SubStaff.container.find('.subStaffPaging').tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount:1,
                useSmallPageSize: true,
                ajax: {
                    param : {searchText: searchText},
                    on: true,
                    dataType: 'json',
                    url: "/CPEctocyst/getSubStaffs",
                    callback:function(data){
                        if(data === undefined || data == null) {
                            SubStaff.container.find('.subStaffListTable tbody .subStaffTableTr').remove();
                            SubStaff.container.find('.subStaffListTable tbody .no-staff-tr').show();
                            return;
                        }
                        if(data.res === undefined || data.res == null || data.res.length == 0) {
                            SubStaff.container.find('.subStaffListTable tbody .subStaffTableTr').remove();
                            SubStaff.container.find('.subStaffListTable tbody .no-staff-tr').show();
                            return;
                        }
                        $(data.res).each(function(i, subStaff){
                            subStaff.createStr = new Date(subStaff.created).formatYMDHMS();
                        });
                        SubStaff.container.find('.subStaffListTable tbody .subStaffTableTr').remove();
                        SubStaff.container.find('.subStaffListTable tbody .no-staff-tr').hide();
                        SubStaff.container.find('.subStaffListTable tbody').append($('#subStaffTableTr').tmpl(data.res));
                        
                        if(!TM.isCPEctocystSuperAdmin()) {
                        	SubStaff.container.find('.showOnlySuperAdmin').remove();
                        }
                        
                        SubStaff.Event.setDynamicEvent();
                    }
                }
            });
        }
    }, SubStaff.init);

    SubStaff.Event = SubStaff.Event || {};
    SubStaff.Event = $.extend({
        setStaticEvent: function(){
            SubStaff.Event.setAddSubStaffEvent();
            SubStaff.Event.setSearchSubStaffEvent();
        },
        setAddSubStaffEvent: function(){
            SubStaff.container.find('.addSubStaff').unbind('click').click(function(){
            	if(!TM.isCPEctocystSuperAdmin()) {
                	alert("亲，您尚无该操作权限！");
                	return;
                }
                SubStaff.init.addSubStaff();
            });
        },
        setSearchSubStaffEvent: function(){
            SubStaff.container.find('.subStaffSearchBtn').unbind("click").click(function(){
                var searchText =  SubStaff.container.find('#subStaffSearchText').val();
                if(searchText === undefined || searchText == null || searchText == "请输入用户名进行搜索") {
                    searchText = "";
                }
                SubStaff.init.search(searchText);
            });
            SubStaff.container.find('#subStaffSearchText').keydown(function(event){
                event.keyCode==13 && SubStaff.container.find('.subStaffSearchBtn').trigger('click');
            })
            SubStaff.container.find('.subStaffSearchBtn').trigger("click");
        },
        setDynamicEvent: function(){
            SubStaff.Event.setChangePswByparentEvent();
            SubStaff.Event.setDeleteByparentEvent();
            SubStaff.Event.setCheckByparentEvent();
        },
        setDeleteByparentEvent: function(){
            SubStaff.container.find('.deleteByParent').unbind("click").click(function(){
            	if(!TM.isCPEctocystSuperAdmin()) {
                	alert("亲，您尚无该操作权限！");
                	return;
                }
                var subStaffId = $(this).parent().parent().attr("substaffid");
                var subStaffName = $(this).parent().parent().attr("substaffname");
                if(confirm("确定要删除用户" + subStaffName)) {
                    $.get("/CPEctocyst/deleteByParent", {subStaffId: subStaffId}, function(data){
                        if(data === undefined || data == null) {
                            TM.Alert.load("删除用户失败, 请重试或联系客服");
                            return;
                        }
                        if(data.success == false) {
                            TM.Alert.load(data.message);
                            return;
                        }
                        TM.Alert.load("删除用户成功", 400, 300, function(){
                            location.reload();
                        });
                    });
                }
            });
        },
        setCheckByparentEvent: function(){
            SubStaff.container.find('.checkByParent').unbind("click").click(function(){
                var subStaffId = $(this).parent().parent().attr("substaffid");
                var subStaffName = $(this).parent().parent().attr("substaffname");
                window.location.href = "/CPEctocyst/rateAdmin?name=" + subStaffName;
            });
        },
        setChangePswByparentEvent: function(){
            SubStaff.container.find('.changePswByParent').unbind('click').click(function(){
            	if(!TM.isCPEctocystSuperAdmin()) {
                	alert("亲，您尚无该操作权限！");
                	return;
                }
                var subStaffId = $(this).parent().parent().attr("substaffid");
                var subStaffName = $(this).parent().parent().attr("substaffname");
                var html = '' +
                    '<table style="text-align: left;width: 95%;">' +
                    '   <tbody style="text-align: center;">' +
                    '       <tr style="height: 50px;line-height: 50px;">' +
                    '           <td style="width: 30%;"><span>用户名:</span></td>' +
                    '           <td style="width: 70%;"><input type="text" class="user-name" style="width: 250px;" disabled="disabled" value="'+ subStaffName +'"/></td>' +
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

                        $.post('/CPEctocyst/changePswByParent',{subStaffId:subStaffId, newPassword:newpassword},function(data){
                            if(data === undefined || data == null){
                                alert.load("修改密码失败，请重试或联系客服");
                                return;
                            }
                            if(data.success == false){
                                alert(fata.message);
                                return;
                            }

                            TM.Alert.load("修改密码成功");

                        });
                    }
                });
                TM.Alert.loadDetail(obj, 600, 400, function(){
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

                    $.post('/CPEctocyst/changePswByParent',{subStaffId:subStaffId, newPassword:newpassword},function(data){
                        if(data === undefined || data == null){
                            alert("修改密码失败，请重试或联系客服");
                            return;
                        }
                        if(data.success == false){
                            alert(fata.message);
                            return;
                        }

                        TM.Alert.load("修改密码成功");

                    });
                }, "登陆");
            });
        }
    }, SubStaff.Event);

    SubStaff.util = SubStaff.util || {};
    SubStaff.util = $.extend({
        checkAjaxData: function(data) {
            if(data === undefined || data == null) {
                return false;
            }
            if(data.success == false) {
                return false;
            }
            return true;
        }
    }, SubStaff.util);
})(jQuery,window));