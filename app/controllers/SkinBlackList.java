
package controllers;

import java.util.ArrayList;
import java.util.List;

import models.defense.BlackListBuyer;
import models.defense.BlackListExplain;
import models.user.User;

import org.apache.commons.lang3.StringUtils;

import result.TMResult;
import utils.TaobaoUtil;
import cache.TradeDefenseCache.BlackListBuyerCache;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;

import dao.defense.BlackListBuyerDao;
import dao.defense.BlackListExplainDao;

/**
 * 淘掌柜-好评王-差评防御-老的黑名单
 * @author zrb
 *
 */
public class SkinBlackList extends TMController {
    public static void index() {
        render("skincomment/blacklist.html");
    }

    public static void isOn() {
        User user = getUser();
        boolean isOn = user.isBlackListAutoDefenseOn();
        renderSuccess("", isOn);
    }

    public static void turnOn() {
        User user = getUser();
        user.setBlackListAutoDefenseOn(true);
        //这里加入交易create的消息  TradeCreate
//        TaobaoUtil.permitByUser(user);
        TaobaoUtil.permitTMCUser(user);
        user.jdbcSave();

        BlackListExplain blackListExplain = BlackListExplainDao.findByUserId(user.getId());
        if (blackListExplain == null) {
            blackListExplain = new BlackListExplain(user.getId(), BlackListExplain.Default_Explain,
                    System.currentTimeMillis());
        }
        blackListExplain.jdbcSave();

        renderSuccess("黑名单开启成功", "");
    }

    public static void turnOff() {
        User user = getUser();
        user.setBlackListAutoDefenseOn(false);
        user.jdbcSave();
        renderSuccess("黑名单关闭成功", "");
    }

    public static void querySkinBlackExplain() {
        User user = getUser();
        BlackListExplain blackListExplain = BlackListExplainDao.findByUserId(user.getId());
        String reason = "";
        if (blackListExplain == null) {
            reason = BlackListExplain.Default_Explain;
        } else {
            reason = blackListExplain.getTradeExplain();
        }
        boolean autoChapingBlackList = user.isAutoChapingBlackListOn();
        boolean autoRefundBlackList = user.isAutoRefundBlackListOn();
        String json = "{'success':true, 'autoChapingBlackList':" + autoChapingBlackList + ",'autoRefundBlackList':"
                + autoRefundBlackList + ",'res':'" + reason + "'}";
        json = json.replace('\'', '"');
        renderJSON(json);
//        renderSuccess("", reason);
    }

    /**
     * 提交黑名单加入设置
     * @param explain
     */
    public static void submitSettings(String explain, boolean autoChapingBlackList, boolean autoRefundBlackList,
            int otherAddBlacklist) {
        if (StringUtils.isEmpty(explain))
            renderError("请先输入交易关闭的解释");

        User user = getUser();
        user.setAutoChapingBlackListOn(autoChapingBlackList);
        user.setAutoRefundBlackListOn(autoRefundBlackList);

        // 这里加入交易create的消息 TradeCreate
//        TaobaoUtil.permitByUser(user);
        TaobaoUtil.permitTMCUser(user);
        user.jdbcSave();

        BlackListExplain blackListExplain = BlackListExplainDao.findByUserId(user.getId());
        long ts = System.currentTimeMillis();
        if (blackListExplain == null) {
            blackListExplain = new BlackListExplain(user.getId(), explain, ts);
        } else {
            blackListExplain.setTradeExplain(explain);
            blackListExplain.setTs(ts);
        }
        boolean isSuccess = blackListExplain.jdbcSave();
        if (isSuccess == false) {
            index();
        } else {
            index();
        }
    }

    /**
     * 提交交易关闭的解释
     * @param explain
     */
    public static void submitExplain(String explain) {
        if (StringUtils.isEmpty(explain))
            renderError("请先输入交易关闭的解释");

        User user = getUser();
        BlackListExplain blackListExplain = BlackListExplainDao.findByUserId(user.getId());
        long ts = System.currentTimeMillis();
        if (blackListExplain == null) {
            blackListExplain = new BlackListExplain(user.getId(), explain, ts);
        } else {
            blackListExplain.setTradeExplain(explain);
            blackListExplain.setTs(ts);
        }
        boolean isSuccess = blackListExplain.jdbcSave();
        if (isSuccess == false) {
            renderError("亲，提交失败，请联系我们");
        } else {
            renderSuccess("解释提交成功!", "");
        }
    }

    public static void autoClose(boolean status) {
        User user = getUser();
        user.setAutoDefenseOn(status);
        // 这里加入交易create的消息 TradeCreate
        // TaobaoUtil.permitByUser(user);
        TaobaoUtil.permitTMCUser(user);
        user.jdbcSave();

        if (status == true) {
            renderText("on");
        } else {
            renderText("off");
        }
    }

    /**
     * 保存黑名单
     * @param buyerNames
     * @param remark
     */
    public static void addBlackList(String buyerNames, String remark) {
        User user = getUser();

        if (StringUtils.isEmpty(buyerNames)) {
            renderError("请先输入黑名单买家的账号");
        }
        buyerNames = buyerNames.trim();
        buyerNames = buyerNames.replace("\r", "");
        if (StringUtils.isEmpty(buyerNames)) {
            renderError("请先输入黑名单买家的账号");
        }
        String[] buyerNameArray = buyerNames.split("\n");//可能linux下没有\r
        if (buyerNameArray == null || buyerNameArray.length == 0) {
            renderError("请先输入黑名单买家的账号");
        }
        List<String> resultNameList = new ArrayList<String>();
        for (int i = 0; i < buyerNameArray.length; i++) {
            String buyerName = buyerNameArray[i];
            buyerName = buyerName.trim();
            if (StringUtils.isEmpty(buyerName)) {

            } else {
                resultNameList.add(buyerName);
            }
        }
        if (resultNameList.isEmpty()) {
            renderError("请先输入黑名单买家的账号");
        }
        Long ts = System.currentTimeMillis();
        if (!StringUtils.isEmpty(remark))
            remark = remark.trim();
        List<String> errorList = new ArrayList<String>();
        for (String buyerName : resultNameList) {
            BlackListBuyer blackListBuyer = new BlackListBuyer(user.getId(), buyerName, ts, remark);
            boolean isSuccess = blackListBuyer.jdbcSave();
            if (isSuccess == false) {
                errorList.add(buyerName);
            }
        }

        BlackListBuyerCache.deleteBlackListBuyerFromCache(user.getIdlong());

        if (!errorList.isEmpty()) {
            renderError("有" + errorList.size() + "个黑名单保存失败，请重新尝试一次！");
        } else {
            renderSuccess("黑名单保存成功", "");
        }

    }

    /**
     * 查询黑名单
     * @param buyerName
     * @param pn
     * @param ps
     */
    public static void queryBlackListBuyers(String buyerName, int pn, int ps) {
        if (!StringUtils.isEmpty(buyerName)) {
            buyerName = buyerName.trim();
        }
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps, 10);
        TMResult tmResult = BlackListBuyerDao.findBlackListBuyersByName(user.getId(), buyerName, po);

        renderJSON(tmResult);
    }

    public static void modifyRemark(Long blackId, String remark) {
        if (blackId == null || blackId.longValue() <= 0) {
            renderError("请先选择要更改的黑名单");
        }
        if (StringUtils.isEmpty(remark)) {
            renderError("请先输入备注");
        }

        User user = getUser();
        BlackListBuyer blackBuyer = BlackListBuyerDao.findBlackListBuyersById(user.getId(), blackId);
        if (blackBuyer == null) {
            renderError("找不到要修改的黑名单，请联系我们");
        }
        blackBuyer.setRemark(remark);
        blackBuyer.jdbcSave();

    }

    public static void deleteBlackList(List<Long> blackIdList) {
        if (CommonUtils.isEmpty(blackIdList)) {
            renderError("请先选择要删除的黑名单");
        }
        User user = getUser();
        List<BlackListBuyer> buyerList = BlackListBuyerDao.findBlackListBuyersByIds(user.getId(), blackIdList);

        if (CommonUtils.isEmpty(buyerList)) {
            renderError("找不到要删除的黑名单，请联系我们");
        }

        boolean isSuccess = BlackListBuyerDao.deleteByIds(user.getId(), blackIdList);
        if (isSuccess == false) {
            renderError("系统出现一些意外，黑名单删除失败");
        } else {
            BlackListBuyerCache.deleteBlackListBuyerFromCache(user.getIdlong());
            renderSuccess("删除成功！", "");
        }
    }
}
