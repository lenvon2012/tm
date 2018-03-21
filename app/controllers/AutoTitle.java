
package controllers;

import models.user.User;

import com.ciaosir.client.utils.JsonUtil;

import controllers.Op.InviteInfo;

public class AutoTitle extends TMController {

    public static void comment() {
        render("autoTitle/helicomment.html");
    }

    public static void delist() {
        render("autoTitle/helidelist.html");
    }

    public static void window() {
        render("autoTitle/heliwindow.html");
    }

    public static void propdiag() {
        render("autoTitle/autodiag.html");
    }

    public static void invite() {
        render("op/autotitleinvites.html");
    }

    public static void recover() {
        render("autoTitle/recover.html");
    }

    public static void batchmulti() {
        render("autoTitle/multiModify.html");
    }

    public static void award() {
        render("autoTitle/award.html");
    }

    public static void batchDelist() {
        render("autoTitle/batch.html");
    }

    public static void lottery() {
        render("lottery/lotteryautotitle.html");
    }

    public static void mywords() {
        render("autoTitle/mywords.html");
    }

    public static void seawords() {
        render("autoTitle/seawords.html");
    }

    public static void delistPlans() {
        render("autoTitle/delistPlans.html");
    }

    public static void delistCreate() {
        render("autoTitle/delistCreate.html");
    }

    public static void delistPlanDetail(long planId) {
        render("autoTitle/delistPlanDetail.html");
    }
    
    public static void rmlinks() {
        render("autoTitle/rmlinks.html");
    }
    
    public static void autocat() {
        render("autoTitle/autocat.html");
    }
    
    public static void genInviteUrl() {
        User user = getUser();
        InviteInfo info = new InviteInfo();
        log.info("[user : ]" + user);
        String url = "http://" + request.host + "/autotitleinvite/" + user.getId();
        info.setUrl(url);
        renderJSON(JsonUtil.getJson(info));
    }

    public static void tobeFenxiao(boolean toBeOn) {
        User user = getUser();
        user.setFenxiaoOn(toBeOn);
        boolean isSuccess = user.jdbcSave();
        if (isSuccess) {
            renderText("设置成功");
        } else {
            renderText("设置失败");
        }
    }

}
