
package controllers;

import java.util.List;

import models.paipai.PaiPaiUser;
import ppapi.PaiPaiSubscribeApi;
import ppapi.models.PaiPaiSubscribe;

public class PaiPaiLeTuiguang extends PaiPaiController {

    public static void index() {
        render("paipailetuiguang/index.html");
    }
    
    public static void test() {
        PaiPaiUser user = getUser();
        List<PaiPaiSubscribe> sub = new PaiPaiSubscribeApi.PaiPaiSubscribeListApi(user).call();
        renderJSON(sub);
    }

    public static void queryUserNick() {
        PaiPaiUser user = getUser();
        if (user == null) {
            renderText("");
        }
        renderText(user.getNick());
    }

    //推广宝贝
    public static void promoteItem() {
        render("paipailetuiguang/items.html");
    }

    //热销
    public static void hotRecommend() {
        render("paipailetuiguang/hotrecommend.html");
    }

    //帮助
    public static void help() {
        render("paipailetuiguang/help.html");
    }

    //帮助
    public static void tryhelp() {
        render("paipailetuiguang/tryhelp.html");
    }

    //奖励推广位
    public static void award() {
        render("paipailetuiguang/award.html");
    }

    //百度收藏
    public static void shoucang() {
        render("paipailetuiguang/shoucang.html");
    }

    //升级
    public static void upgrade() {
        render("/paipailetuiguang/upgrade.html");
    }

    public static void undefined() {
        
    }
    
    
    
}
