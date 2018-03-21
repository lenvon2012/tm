
package controllers;

import java.util.List;

import models.spread.SpreadItemPlay;
import models.spread.SpreadItemPlay.SpreadLevelType;
import models.spread.SpreadItemPlay.SpreadStatus;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;
import actions.spread.SpreadItemsAction;
import actions.spread.UserSpreadAction;
import actions.spread.UserSpreadAction.LevelSpreadInfo;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;

import dao.spread.SpreadItemDao;

public class SpreadItem extends TMController {
    private static final Logger log = LoggerFactory.getLogger(SpreadItem.class);

    public static void index() {
        render("spreaditem/spreadindex.html");
    }

    public static void admin() {
        render("spreaditem/spreaditems.html");
    }

    public static void querySpreadInfo() {
        User user = getUser();
        LevelSpreadInfo spreadInfo = UserSpreadAction.queryLevelSpreadInfo(user, SpreadLevelType.Level1);

        renderSuccess("", spreadInfo);

    }

    public static void queryItems(String title, Long cid, int status, int sort, int pn, int ps) {
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps, 10);

        String catId = "";
        if (cid != null && cid > 0) {
            catId = cid + "";
        }
        int level = SpreadLevelType.Level1;
        TMResult res = SpreadItemDao.queryWithItemInfo(user.getId(), catId, status, title, level, sort, po);

        renderJSON(JsonUtil.getJson(res));
    }

    public static void doSpreadItems(List<Long> numIidList) {
        if (CommonUtils.isEmpty(numIidList)) {
            renderError("请先选择要推广的宝贝!");
        }
        int level = SpreadLevelType.Level1;
        User user = getUser();
        List<SpreadItemPlay> itemList = SpreadItemDao.querySpreadItemsByIds(user.getId(), numIidList, level);

        LevelSpreadInfo spreadInfo = UserSpreadAction.queryLevelSpreadInfo(user, level);

        int newSpreadNum = 0;
        int unSpreadNum = 0;

        for (Long numIid : numIidList) {
            if (numIid == null)
                continue;
            SpreadItemPlay spreadItem = null;
            for (SpreadItemPlay temp : itemList) {
                if (numIid.equals(temp.getNumIid())) {
                    spreadItem = temp;
                    break;
                }
            }
            if (spreadItem == null) {
                if (spreadInfo.getUsedNum() + newSpreadNum < spreadInfo.getTotalNum()) {
                    newSpreadNum++;
                } else {
                    unSpreadNum++;
                    continue;
                }
                String spreadUrl = SpreadItemsAction.doSpreadItem(numIid);
                spreadItem = new SpreadItemPlay();
                spreadItem.setUserId(user.getId());
                spreadItem.setNumIid(numIid);
                spreadItem.setSpreadLevel(level);
                spreadItem.setSpreadStatus(SpreadStatus.ON);
                spreadItem.setSpreadUrl(spreadUrl);

                spreadItem.jdbcSave();
            } else {
                spreadItem.setSpreadStatus(SpreadStatus.ON);
                spreadItem.jdbcSave();
            }
        }

        if (numIidList.size() == unSpreadNum) {
            renderError("您的推广位已经全部被使用！如想继续推广，请先升级或取消其他宝贝的推广。");
        } else if (numIidList.size() > 1 && unSpreadNum > 0) {
            renderSuccess("由于推广位不足，有" + unSpreadNum + "个宝贝没有开启推广", "");
        } else {
            renderSuccess("推广成功", "");
        }
    }

    public static void suspendSpread(List<Long> numIidList) {
        if (CommonUtils.isEmpty(numIidList)) {
            renderError("请先选择要暂停推广的宝贝!");
        }
        int level = SpreadLevelType.Level1;
        User user = getUser();
        List<SpreadItemPlay> itemList = SpreadItemDao.querySpreadItemsByIds(user.getId(), numIidList, level);

        for (Long numIid : numIidList) {
            if (numIid == null)
                continue;
            SpreadItemPlay spreadItem = null;
            for (SpreadItemPlay temp : itemList) {
                if (numIid.equals(temp.getNumIid())) {
                    spreadItem = temp;
                    break;
                }
            }
            if (spreadItem == null) {
                continue;
            } else {
                spreadItem.setSpreadStatus(SpreadStatus.OFF);
                spreadItem.jdbcSave();
            }
        }

        renderSuccess("宝贝暂停推广成功", "");
    }

    public static void deleteSpread(List<Long> numIidList) {
        if (CommonUtils.isEmpty(numIidList)) {
            renderError("请先选择要取消推广的宝贝!");
        }
        int level = SpreadLevelType.Level1;
        User user = getUser();

        boolean flag = SpreadItemDao.deleteSpreadItemByNumIids(user.getId(), level, numIidList);

        renderSuccess("宝贝取消推广成功", "");
    }

    public static void shopSuspend() {
        User user = getUser();

        int level = SpreadLevelType.Level1;
        SpreadItemDao.updateSpreadItemStatus(user.getId(), level, SpreadStatus.OFF);

        renderSuccess("全店暂停推广成功", "");
    }

    public static void shopDelete() {
        User user = getUser();

        int level = SpreadLevelType.Level1;
        SpreadItemDao.deleteAllByLevel(user.getId(), level);

        renderSuccess("全店取消推广成功", "");
    }
}
