
package controllers;

import models.paipai.PaiPaiUser;
import ppapi.PaiPaiItemApi.PaiPaiItemDetailApi;
import ppapi.PaiPaiItemApi.PaiPaiItemUpdateTitleApi;
import ppapi.models.PaiPaiItem;

public class PaiPaiWeigou extends PaiPaiController {

    public static void index() {
        render("paipaiweigou/index.html");
    }
    
    public static void test(String itemCode) {
        PaiPaiUser user = getUser();
//        List<PaiPaiSubscribe> sub = new PaiPaiSubscribeApi.PaiPaiSubscribeListApi(user).call();
        PaiPaiItem item = new PaiPaiItemDetailApi(user, itemCode).call();
        
        Boolean call = new PaiPaiItemUpdateTitleApi(user, itemCode, "ttttt中文测试卡强大测试团体加入测试").call();
        System.out.println(call);
        renderJSON(item);
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
        render("paipaiweigou/items.html");
    }

    //热销
    public static void hotRecommend() {
        render("paipaiweigou/hotrecommend.html");
    }

    //帮助
    public static void help() {
        render("paipaiweigou/help.html");
    }

    //帮助
    public static void tryhelp() {
        render("paipaiweigou/tryhelp.html");
    }

    //奖励推广位
    public static void award() {
        render("paipaiweigou/award.html");
    }

    //百度收藏
    public static void shoucang() {
        render("paipaiweigou/shoucang.html");
    }

    //升级
    public static void upgrade() {
        render("/paipaiweigou/upgrade.html");
    }

    public static void undefined() {
        
    }
    
    
    
}
