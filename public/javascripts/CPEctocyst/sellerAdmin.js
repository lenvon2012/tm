var TM = TM || {};
((function ($, window) {
    TM.SellerAdmin = TM.SellerAdmin || {};

    var SellerAdmin = TM.SellerAdmin;

    SellerAdmin.init = SellerAdmin.init || {};
    SellerAdmin.init = $.extend({
        doInit: function(container){
            SellerAdmin.container = container;
            SellerAdmin.init.initStaffSelectorSeach();
            SellerAdmin.Event.setStaticEvent();
        },
        initStaffSelectorSeach: function(){
            // 如果是外包Chief账号登陆，则显示客服搜索下拉框
            if(TM.isCPEctocystAdmin()) {

                    // 这里用ajax取客服列表，并生成 subStaffSelector
                    $.get("/CPEctocyst/getAllSubStaffs", function(data){
                        if(data === undefined || data == null) {
                            return;
                        }
                        if(data.success == false) {
                            return;
                        }
                        if(data.length == 0) {
                            return;
                        }
                        var html = '<select class="kefuSearchSelector"><option value="">全部</option>';
                        $(data).each(function(i, kefu){
                            html += '<option value="'+kefu.name+'">'+kefu.name+'</option>';
                        });
                        html += '</select>';
                        SellerAdmin.container.find('.staffSelectorSearchWrapper').append($(html));
                        SellerAdmin.container.find('.staffSelectorSearchWrapper').show();
                    });

            }
        },
        search: function(searchText, staffName){
            if(searchText === undefined || searchText == null || searchText == "请输入卖家旺旺进行搜索") {
                searchText = "";
            }
            if(staffName === undefined || staffName == null) {
                staffName = "";
            }
            SellerAdmin.container.find('.sellerAdminPaging').tmpage({
                currPage: 1,
                pageSize: 10,
                pageCount:1,
                useSmallPageSize: true,
                ajax: {
                    param : {searchText: searchText, staffName: staffName},
                    on: true,
                    dataType: 'json',
                    url: "/CPEctocyst/getSellers",
                    callback:function(data){
                        if(data === undefined || data == null) {
                            SellerAdmin.container.find('.sellerAdminTable tbody .sellerAdminTableTr').remove();
                            SellerAdmin.container.find('.sellerAdminTable tbody .no-seller-tr').show();
                            return;
                        }
                        if(data.res === undefined || data.res == null || data.res.length == 0) {
                            SellerAdmin.container.find('.sellerAdminTable tbody .sellerAdminTableTr').remove();
                            SellerAdmin.container.find('.sellerAdminTable tbody .no-seller-tr').show();
                            return;
                        }
                        SellerAdmin.container.find('.sellerAdminTable tbody .sellerAdminTableTr').remove();
                        SellerAdmin.container.find('.sellerAdminTable tbody .no-seller-tr').hide();
                        SellerAdmin.container.find('.sellerAdminTable tbody').append($('#sellerAdminTableTr').tmpl(data.res));
                        
                        if(!TM.isCPEctocystSuperAdmin()) {
                        	SellerAdmin.container.find('.showOnlySuperAdmin').remove();
                        }
                        if(!TM.isCPEctocystAdmin()) {
                            SellerAdmin.container.find('.showOnlyAdmin').remove();
                        }
                        
                        SellerAdmin.Event.setDynamicEvent();
                    }
                }
            });
        },
        subStaffSelector: ''
    }, SellerAdmin.init);

    SellerAdmin.Event = SellerAdmin.Event || {};
    SellerAdmin.Event = $.extend({
        setStaticEvent: function(){
            SellerAdmin.Event.setSellerAdminSearchEvent();
        },
        setSellerAdminSearchEvent: function(){
            SellerAdmin.container.find('.sellerAdminSearchBtn').unbind("click").click(function(){
                var searchText =  SellerAdmin.container.find('#sellerAdminSearchText').val();
                if(searchText === undefined || searchText == null || searchText == "请输入卖家旺旺进行搜索") {
                    searchText = "";
                }
                var staffName = SellerAdmin.container.find('.kefuSearchSelector').val();
                SellerAdmin.init.search(searchText, staffName);
            });
            SellerAdmin.container.find('#sellerAdminSearchText').keydown(function(event){
                event.keyCode==13 && SellerAdmin.container.find('.sellerAdminSearchBtn').trigger('click');
            })
            SellerAdmin.container.find('.sellerAdminSearchBtn').trigger("click");
        },
        setDynamicEvent: function(){
            SellerAdmin.container.find('.allocateToSubStaff').unbind("click").click(function(){
            	if(!TM.isCPEctocystSuperAdmin()) {
                	alert("亲，您尚无该操作权限！");
                	return;
                }
                var userId = $(this).parent().parent().attr("userid");
                var subStaffId = $(this).attr("substaffid");
                if(SellerAdmin.init.subStaffSelector === undefined || SellerAdmin.init.subStaffSelector == null || SellerAdmin.init.subStaffSelector == "") {
                    // 这里用ajax取客服列表，并生成 subStaffSelector
                    $.get("/CPEctocyst/getAllSubStaffs", function(data){
                        if(data === undefined || data == null) {
                            TM.Alert.load("获取客服列表出错，请重试或联系客服");
                            return;
                        }
                        if(data.success == false) {
                            TM.Alert.load(data.message);
                            return;
                        }
                        if(data.length == 0) {
                            TM.Alert.load("亲，您还没有添加客服哦，请去客服管理页面添加哦", 400, 300, function(){
                                window.open("/CPEctocyst/staffAdmin",'_self');
                        });
                            return;
                        }
                        var html = '<select class="allocateKefuSelector">';
                        $(data).each(function(i, kefu){
                            html += '<option subStaffName="'+kefu.name+'" value="'+kefu.id+'">'+kefu.name+'</option>';
                        });
                        html += '</select>';
                        SellerAdmin.init.subStaffSelector = html;
                        var obj = $(html);
                        if(subStaffId === undefined || subStaffId == null || subStaffId > 0) {
                            obj.val(subStaffId);
                        }
                        TM.Alert.loadDetail(obj, 400, 300, function(){
                            var subStaffId = obj.val();
                            $.get("/CPEctocyst/allocateSellerToSubStaff", {subStaffId: subStaffId, userId: userId}, function(data){
                                if(data === undefined || data == null) {
                                    alert("设置失败，请重试或联系客服");
                                    location.reload();
                                }
                                if(data.success == false) {
                                    alert(data.message);
                                    location.reload();
                                }
                                alert("设置成功");
                                location.reload();
                            });
                        }, "请选择客服");
                    });
                } else {
                    var html = SellerAdmin.init.subStaffSelector;
                    var obj = $(html);
                    if(subStaffId === undefined || subStaffId == null || subStaffId > 0) {
                        obj.val(subStaffId);
                    }
                    TM.Alert.loadDetail(obj, 400, 300, function(){
                        var subStaffId = obj.val();
                        $.get("/CPEctocyst/allocateSellerToSubStaff", {subStaffId: subStaffId, userId: userId}, function(data){
                            if(data === undefined || data == null) {
                                alert("设置失败，请重试或联系客服");
                                location.reload();
                            }
                            if(data.success == false) {
                                alert(data.message);
                                location.reload();
                            }
                            alert("设置成功");
                            location.reload();
                        });
                    }, "请选择客服");
                }

            });
        }

    }, SellerAdmin.Event);

    SellerAdmin.util = SellerAdmin.util || {};
    SellerAdmin.util = $.extend({

    }, SellerAdmin.util);
})(jQuery,window));