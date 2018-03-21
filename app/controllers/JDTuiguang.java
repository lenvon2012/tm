
package controllers;

import models.jd.JDUser;

public class JDTuiguang extends JDController {

    public static void index() {
        render("jdtuiguang/index.html");
    }

    public static void test() {
        JDUser user = getUser();
        renderJSON(user);
    }

    public static void queryUserNick() {
        JDUser user = getUser();
        if (user == null) {
            renderText("");
        }
        renderText(user.getNick());
    }

    //推广宝贝
    public static void promoteItem() {
        render("jdtuiguang/items.html");
    }

    //热销
    public static void hotRecommend() {
        render("jdtuiguang/hotrecommend.html");
    }

    //帮助
    public static void help() {
        render("jdtuiguang/help.html");
    }

    //帮助
    public static void tryhelp() {
        render("jdtuiguang/tryhelp.html");
    }

    //奖励推广位
    public static void award() {
        render("jdtuiguang/award.html");
    }

    //百度收藏
    public static void shoucang() {
        render("jdtuiguang/shoucang.html");
    }

    //升级
    public static void upgrade() {
        render("jdtuiguang/upgrade.html");
    }

    public static void undefined() {

    }

}
