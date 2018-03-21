
package controllers;

import java.util.List;

import models.op.TMRefundName;
import models.vas.ArticleBizOrderPlay;
import play.mvc.With;
import result.TMResult;

import com.ciaosir.client.pojo.PageOffset;
import com.taobao.api.internal.util.StringUtils;

import dao.refund.TMRefundDao;
import dao.vas.ArticleBizOrderDao;

@With(Secure.class)
public class TMRefundAdmin extends CRUD {

    public static void index() {
        render("Application/refundcrud.html");
    }

    public static void edit(long id) {
        TMRefundName RefundTrade = TMRefundDao.findRefundTradeListId(id);
        System.out.println(RefundTrade);
        render("Application/refundedit.html", RefundTrade);
    }

    public static void update(String wangwang, long created, long updated, String upname, String reason,
            String assessor, String app, String status, double amount, String advice) {
        long up = System.currentTimeMillis();
        TMRefundName RefundTrade = new TMRefundName(wangwang, created, up, upname, reason
                , assessor, app, status, amount, advice);

        RefundTrade.jdbcSave();
        renderText("success");
    }

    public static void findRefundTrade(String wangwang, String app, String status, int pn, int ps) {
        wangwang = org.apache.commons.lang.StringUtils.trim(wangwang);
        PageOffset po = new PageOffset(pn, ps, 10);

        if (StringUtils.isEmpty(wangwang) && StringUtils.isEmpty(app) && StringUtils.isEmpty(status)) {
            TMResult tmResult = TMRefundDao.findRefundTradeListAll(po);
            renderJSON(tmResult);
        }
        if (StringUtils.isEmpty(wangwang) && !StringUtils.isEmpty(app) && StringUtils.isEmpty(status)) {
            TMResult tmResult = TMRefundDao.findRefundTradeListA(app, po);
            renderJSON(tmResult);
        }
        if (!StringUtils.isEmpty(wangwang) && StringUtils.isEmpty(app) && StringUtils.isEmpty(status)) {
            TMResult tmResult = TMRefundDao.findRefundTradeListW(wangwang, po);
            renderJSON(tmResult);
        }
        if (StringUtils.isEmpty(wangwang) && StringUtils.isEmpty(app) && !StringUtils.isEmpty(status)) {
            TMResult tmResult = TMRefundDao.findRefundTradeListS(status, po);
            renderJSON(tmResult);
        }
        if (StringUtils.isEmpty(wangwang) && !StringUtils.isEmpty(app) && !StringUtils.isEmpty(status)) {
            TMResult tmResult = TMRefundDao.findRefundTradeListAS(app, status, po);
            renderJSON(tmResult);
        }
        if (!StringUtils.isEmpty(wangwang) && StringUtils.isEmpty(app) && !StringUtils.isEmpty(status)) {
            TMResult tmResult = TMRefundDao.findRefundTradeListWS(wangwang, status, po);
            renderJSON(tmResult);
        }
        if (!StringUtils.isEmpty(wangwang) && !StringUtils.isEmpty(app) && StringUtils.isEmpty(status)) {
            TMResult tmResult = TMRefundDao.findRefundTradeListWA(wangwang, app, po);
            renderJSON(tmResult);
        }

        else {
            TMResult tmResult = TMRefundDao.findRefundTradeListWAS(wangwang, app, status, po);
            renderJSON(tmResult);

        }

    }

    public static void findTradeList(String wangwang) {
        List<ArticleBizOrderPlay> list = ArticleBizOrderDao.findVasOrderByNick(wangwang);
        renderJSON(list);
    }

//    public static void updatebyid(long id,String assessor){
//    	TMRefundDao.updateRefundTrade(id, assessor);
//    	renderText("success");
//    }

    public static void submitRefund(String wangwang, String upname, String reason, String app, double amount) {

        if (StringUtils.isEmpty(wangwang)) {
            renderText("wangwang empty");
            return;
        }
        if (StringUtils.isEmpty(upname)) {
            renderText("请输入申请人");
            return;
        }
        if (StringUtils.isEmpty(reason)) {
            renderText("请输入申请原因");
            return;
        }
        if (StringUtils.isEmpty(app)) {
            renderText("请输入产品应用");
            return;
        }

        boolean success = TMRefundDao.submitRefundTrade(wangwang, upname, reason, app, amount);
        renderText("success");
    }

    public static void isRefundByNick(String wangwang) {
        if (StringUtils.isEmpty(wangwang)) {
            renderText("wangwang empty");
            return;
        }

        TMRefundName Wrefund = TMRefundDao.findIsRefundByNick(wangwang);

        if (Wrefund == null) {
            renderJSON("no refund!!!!");
        }

        else {
            renderJSON(new TMResult(Wrefund));
        }
    }
}
