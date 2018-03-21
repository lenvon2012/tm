
package controllers;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Yueji extends TMController {
    private static final Logger log = LoggerFactory.getLogger(Yueji.class);

    public static void index(String sid) {
        render("yueji/firstpage.html");
    }

    public static void queryUserNick() {
        User user = getUser();
        if (user == null) {
            renderText("");
        }
        renderText(user.getUserNick());
    }

    public static void tuiguang() {
        render("yueji/items.html");
    }

    public static void hotRecommend() {
        render("yueji/hotrecommend.html");
    }

    public static void help() {
        render("yueji/help.html");
    }

    public static void tryhelp() {
        render("yueji/tryhelp.html");
    }

    public static void award() {
        render("yueji/award.html");
    }

    public static void shoucang() {
        render("yueji/shoucang.html");
    }

    public static void usites() {
        render("yueji/usites.html");
    }

    public static void upgrade() {
        render("yueji/upgrade.html");
    }
}
