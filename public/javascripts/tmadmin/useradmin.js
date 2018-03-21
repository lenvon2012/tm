
TM.UserAdmin = TM.UserAdmin || {};
((function ($, window) {

    /**
     * namespace
     * @type {*}
     */
    var UserAdmin = TM.UserAdmin;


    UserAdmin.init = UserAdmin.init || {};
    UserAdmin.init = $.extend({
        doInit: function(container) {

            UserAdmin.container = container;


            container.find(".search-btn").click(function() {
                var userNick = container.find(".search-input").val();

                if (userNick == "") {
                    alert("请先输入要查询的旺旺！");
                    return;
                }

                location.href = "/bkuseradmin/index?userNick=" + encodeURI(userNick);
            });
            container.find(".search-input").keydown(function(event) {
                if (event.keyCode == 13) {//按回车
                    container.find(".search-btn").click();
                }
            });

            container.find(".do-save-user-btn").click(function() {
                UserAdmin.submit.doSaveUser();
            });


            var hrefParams = $.url(window.location.href);
            var userNick = hrefParams.param('userNick') || "";
            if (userNick === undefined || userNick == null || userNick == "") {

            } else {

                userNick = decodeURI(userNick);
                container.find(".search-input").val(userNick);
                UserAdmin.show.doShow();
            }






        }
    }, UserAdmin.init);


    UserAdmin.show = UserAdmin.show || {};
    UserAdmin.show = $.extend({
        doShow: function() {
            var container = UserAdmin.container;
            var userNick = container.find(".search-input").val();

            container.find(".user-info-div").hide();

            if (userNick == "") {
                alert("请先输入要查询的旺旺！");
                return;
            }

            $.ajax({
                type: "post",
                url: "/bkuseradmin/searchUser",
                data: {userNick: userNick},
                success: function(dataJson){
                    if (TM.AdminUtil.common.judgeAjaxResult(dataJson) == false) {
                        return;
                    }

                    var userJson = dataJson.results;

                    container.find(".user-info-div").show();

                    var tbodyObj = container.find(".user-info-div .user-info-table tbody");

                    tbodyObj.find(".user-id-span").html(userJson.id);
                    tbodyObj.find(".user-nick-span").html(userJson.userNick);
                    tbodyObj.find(".session-key-span").html(userJson.sessionKey);
                    tbodyObj.find(".refresh-token").html(userJson.refreshToken);
                    tbodyObj.find(".first-login-time").html(userJson.firstLoginTime);
                    tbodyObj.find(".last-update-time").html(userJson.lastUpdateTime);
                    tbodyObj.find(".version-span").html(userJson.version);
                    tbodyObj.find(".cid-span").html(userJson.cid);
                    tbodyObj.find(".level-span").html(userJson.level);
                    tbodyObj.find(".type-span").html(userJson.type);
                }
            });

        }
    }, UserAdmin.show);


    UserAdmin.submit = UserAdmin.submit || {};
    UserAdmin.submit = $.extend({
        doSaveUser: function() {
            var tbodyObj = UserAdmin.container.find(".user-info-div .user-info-table tbody");

            var userId = tbodyObj.find(".user-id-span").html();
            var userNick = tbodyObj.find(".user-nick-span").html();

            if (confirm("确定要修改用户信息？") == false) {
                return;
            }

            var paramJson = {};
            paramJson.userId = userId;

            $.ajax({
                type: "post",
                url: "/bkuseradmin/updateUser",
                data: paramJson,
                success: function(dataJson){
                    if (TM.AdminUtil.common.judgeAjaxResult(dataJson) == false) {
                        return;
                    }

                    alert("保存成功！");

                    UserAdmin.container.find(".search-input").val(userNick);

                    UserAdmin.show.doShow();

                }
            });


        }
    }, UserAdmin.submit);

})(jQuery, window));
