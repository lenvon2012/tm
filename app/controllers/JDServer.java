
package controllers;

import java.io.IOException;
import java.util.List;

import jdapi.JDItemApi.WareListSearch;
import models.jd.JDItemPlay;
import models.jd.JDUser;
import result.TMResult;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;

import dao.jd.JDItemDao;
import dao.jd.crmmember.CrmMemberSqlBuilder;

/**
 * 测试期间，用tmcontroller
 */
public class JDServer extends JDController {

    /**
     * 会员等级设置
     */
    public static void memberlevelset() {
        render("jd/jdsubmemberlevelset.html");
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
        TMController.renderMockFileInJsonIfDev("crmmember.json");

    }

    public static void smsmanuallogData(String txtLeftTime, String txtRightTime) throws IOException {
        TMController.renderMockFileInJsonIfDev("smsmanuallog.json");
    }
    
    public static void test(){
        JDUser user = getUser();
        System.out.println(user);
        renderJSON(user);
    }
    
    public static void queryItems(String word, int pn, int ps) {
        JDUser user = getUser();
        user = JDUser.findByUserId(26883L);
        System.out.println(user);
        
        PageOffset po = new PageOffset(pn, ps);
        TMResult res = JDItemDao.queryUserItems(user.getId(), word, -1, 5, po);
        renderJSON(JsonUtil.getJson(res));
    }

    public static void syncItems() {
        JDUser user = getUser();
        System.out.println(user);
        user = JDUser.findByUserId(26883L);

        List<JDItemPlay> list = new WareListSearch(user).call();
        if (CommonUtils.isEmpty(list)) {
            renderJSON("no result");
        }
        for (JDItemPlay jdItemPlay : list) {
            jdItemPlay.jdbcSave();
        }
        renderJSON(list);
    }
}
