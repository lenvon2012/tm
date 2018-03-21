
package controllers;

import java.io.IOException;
import java.util.List;

import job.paipai.PaiPaiItemUpdateJob;
import models.paipai.PaiPaiUser;
import ppapi.PaiPaiItemApi;
import ppapi.models.PaiPaiTradeDisplay;
import dao.jd.crmmember.CrmMemberSqlBuilder;

/**
 * 测试期间，用tmcontroller
 */
public class PaiPaiServer extends PaiPaiController {

    public static void test() {
        PaiPaiUser user = getUser();
        System.out.println(user);
//        renderText(new PaiPaiItemApi.PaiPaiItemListApi(user).call());
//        renderText(new PaiPaiItemApi.PaiPaiItemCatListApi(user,220520L).call());
//        renderText(PaiPaiItemCatPlay.findTopParentCatPlay(220520L));
    }
    
    public static void test2() {
        PaiPaiUser user = getUser();
        List<PaiPaiTradeDisplay> list = new PaiPaiItemApi.PaiPaiTradeListApi(user).call();
        for(PaiPaiTradeDisplay trade : list) {
            trade.jdbcSave();
        }
        renderText(list);
    }
    
    public static void test3() {
        new PaiPaiItemUpdateJob(301074800L).now();
    }

    /**
     * 会员等级设置
     */
    public static void memberlevelset() {
        renderText("login paipai");
//        render("jd/jdsubmemberlevelset.html");
    }

    public static void memberall() {

    }

    public static void memberanalysis() {
        render("jd/client_analysis.html");
    }

    public static void setmember() {
        render("jd/grade_set.html");
    }

    public static void showmember() {
        render("jd/client_list.html");
    }

    public static void member_privilege() {
        render("jd/member_privilege.html");
    }

    public static void membertradesuccess() {

    }

    public static void memberoldcustomer() {

    }

    public static void smsmanual() {
        render("jd/jdmanualSms.html");
    }

    public static void smsmanualLog() {
        render("jd/jdmanualSmsLog.html");
    }

    public static void groupSmsLog() {
        render("jd/groupSmsLog.html");
    }

    public static void queryCrmMembers(CrmMemberSqlBuilder sqlBuilder) throws IOException {
        renderMockFileInJsonIfDev("crmmember.json");

    }

    public static void smsmanuallogData(String txtLeftTime, String txtRightTime) throws IOException {
        renderMockFileInJsonIfDev("smsmanuallog.json");
    }

    public static void list(String s, int pn, int ps) {

    }
}
