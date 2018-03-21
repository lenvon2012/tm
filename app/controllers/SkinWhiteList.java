
package controllers;

import java.util.ArrayList;
import java.util.List;

import models.defense.WhiteListBuyer;
import models.user.User;

import org.apache.commons.lang3.StringUtils;

import result.TMResult;
import utils.TaobaoUtil;
import cache.TradeDefenseCache.WhiteListBuyerCache;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;

import dao.defense.WhiteListBuyerDao;

/**
 * 淘掌柜-好评王-差评防御-老的白名单
 * @author zrb
 *
 */
public class SkinWhiteList extends TMController {
    public static void index() {
        render("skincomment/whitelist.html");
    }

    public static void isOn() {
        User user = getUser();
        boolean isOn = user.isWhiteListAutoDefenseOn();
        renderSuccess("", isOn);
    }

    public static void turnOn() {
        User user = getUser();
        user.setWhiteListAutoDefenseOn(true);
        //这里加入交易create的消息  TradeCreate
//        TaobaoUtil.permitByUser(user);
        TaobaoUtil.permitTMCUser(user);
        user.jdbcSave();

        renderSuccess("白名单开启成功", "");
    }

    public static void turnOff() {
        User user = getUser();
        user.setWhiteListAutoDefenseOn(false);
        user.jdbcSave();
        renderSuccess("白名单关闭成功", "");
    }

    public static void querySkinWhiteExplain() {
        User user = getUser();
        String reason = "";
        renderSuccess("", reason);
    }

    /**
     * 提交交易关闭的解释
     * @param explain
     */
    public static void submitExplain(String explain) {
        if (StringUtils.isEmpty(explain))
            renderError("请先输入交易关闭的解释");
        
        renderSuccess("解释提交成功!", "");
    }

    /**
     * 保存白名单
     * @param buyerNames
     * @param remark
     */
    public static void addWhiteList(String buyerNames, String remark) {
        User user = getUser();

        if (StringUtils.isEmpty(buyerNames)) {
            renderError("请先输入白名单买家的账号");
        }
        buyerNames = buyerNames.trim();
        buyerNames = buyerNames.replace("\r", "");
        if (StringUtils.isEmpty(buyerNames)) {
            renderError("请先输入白名单买家的账号");
        }
        String[] buyerNameArray = buyerNames.split("\n");//可能linux下没有\r
        if (buyerNameArray == null || buyerNameArray.length == 0) {
            renderError("请先输入白名单买家的账号");
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
            renderError("请先输入白名单买家的账号");
        }
        Long ts = System.currentTimeMillis();
        if (!StringUtils.isEmpty(remark))
            remark = remark.trim();
        List<String> errorList = new ArrayList<String>();
        for (String buyerName : resultNameList) {
            WhiteListBuyer whiteListBuyer = new WhiteListBuyer(user.getId(), buyerName, ts, remark);
            boolean isSuccess = whiteListBuyer.jdbcSave();
            if (isSuccess == false) {
                errorList.add(buyerName);
            }
        }
        
        WhiteListBuyerCache.deleteWhiteListBuyerFromCache(user.getId());
        if (!errorList.isEmpty()) {
            renderError("有" + errorList.size() + "个白名单保存失败，请重新尝试一次！");
        } else {
            renderSuccess("白名单保存成功", "");
        }

    }

    /**
     * 查询白名单
     * @param buyerName
     * @param pn
     * @param ps
     */
    public static void queryWhiteListBuyers(String buyerName, int pn, int ps) {
        if (!StringUtils.isEmpty(buyerName)) {
            buyerName = buyerName.trim();
        }
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps, 10);
        TMResult tmResult = WhiteListBuyerDao.findWhiteListBuyersByName(user.getId(), buyerName, po);

        renderJSON(tmResult);
    }

    public static void modifyRemark(Long whiteId, String remark) {
        if (whiteId == null || whiteId.longValue() <= 0) {
            renderError("请先选择要更改的白名单");
        }
        if (StringUtils.isEmpty(remark)) {
            renderError("请先输入备注");
        }

        User user = getUser();
        WhiteListBuyer whiteBuyer = WhiteListBuyerDao.findWhiteListBuyersById(user.getId(), whiteId);
        if (whiteBuyer == null) {
            renderError("找不到要修改的白名单，请联系我们");
        }
        whiteBuyer.setRemark(remark);
        whiteBuyer.jdbcSave();

    }

    public static void deleteWhiteList(List<Long> whiteIdList) {
        if (CommonUtils.isEmpty(whiteIdList)) {
            renderError("请先选择要删除的白名单");
        }
        User user = getUser();
        List<WhiteListBuyer> buyerList = WhiteListBuyerDao.findWhiteListBuyersByIds(user.getId(), whiteIdList);

        if (CommonUtils.isEmpty(buyerList)) {
            renderError("找不到要删除的白名单，请联系我们");
        }

        boolean isSuccess = WhiteListBuyerDao.deleteByIds(user.getId(), whiteIdList);
        if (isSuccess == false) {
            renderError("系统出现一些意外，白名单删除失败");
        } else {
            renderSuccess("删除成功！", "");
        }
    }
}
