
package controllers;

import models.user.User;
import configs.TMConfigs.App;

public class Share extends TMController {
    public static void toShare() {
        User user = getUser();
        String userId = String.valueOf(user.getId());
        response.setCookie("userId", userId);
        render("/share/share.html");
    }

    public static void creatCode(String all) {

        String word = "这是我目前用过的最好的淘宝卖家服务软件，及标题优化、店铺诊断、热词查询、店铺实时监控等等功能的于一体的优秀的服务软件！";
        String inviter = "http://t.taovgo.com/Share/receiveInviter?inviterId=";
        String[] ac = all.split(";");

        for (String a : ac) {

            if (a.indexOf("userId") == 1) {
                String[] ids = a.split("=");
                String usId = ids[1];

                renderText(word + inviter + usId);

            }
        }
    }

    public static void receiveInviter(String inviterId) {
        //将邀请者的id放入cookie中
        response.setCookie("inviterId", inviterId);
        redirect(App.CONTAINER_TAOBAO_URL);
    }
}
