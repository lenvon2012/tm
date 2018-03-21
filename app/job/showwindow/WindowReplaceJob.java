
package job.showwindow;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import jdp.ApiJdpAdapter;
import job.showwindow.ShowWindowExecutor.WindowCondition;
import job.showwindow.ShowWindowInitJob.ShowCaseInfo;
import job.writter.OpLogWritter;
import job.writter.UserTracerWritter;
import models.oplog.OpLog.LogType;
import models.showwindow.DropWindowTodayCache;
import models.showwindow.OnWindowItemCache;
import models.showwindow.WindowMoreRecommend;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;
import bustbapi.ErrorHandler;
import bustbapi.OperateItemApi;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.Item;

import dao.item.ItemDao;

public class WindowReplaceJob implements Callable<Boolean> {

    private static final Logger log = LoggerFactory.getLogger(WindowReplaceJob.class);

    public static final String TAG = "WindowReplaceJob";

    long toCancelNumIid;

    long delistTime;

    User user;

    WindowCondition condition;

    public WindowReplaceJob(User user, DropWindowTodayCache cache) {
        this.user = user;
        this.delistTime = cache.getDelistTime();
        this.toCancelNumIid = cache.getNumIid();

        this.tempRawItemds = null;
//        log.info(format("WindowReplaceJob:user, cache".replaceAll(", ", "=%s, ") + "=%s", user, cache));
    }

    @Override
    public Boolean call() {
        if (!user.isShowWindowOn()) {
            return Boolean.FALSE;
        }
        boolean noMoreChoose = OperateItemApi.isNoMoreCandidatesCached(user);
        if (noMoreChoose) {
            log.info("no more choose for the :" + user.toIdNick());
            return Boolean.FALSE;
        }

        condition = WindowCondition.getByUser(user);
        if (condition.shouldBeAlwaysThere(toCancelNumIid)) {
//            log.warn(" item should be there :" + this);
            return Boolean.FALSE;
        }

        fetchRawItems();
        // TODO now, we need to cancel..
        doReplace(toCancelNumIid);

        return Boolean.FALSE;
    }

    public void doReplace(Long toCancelNumIid) {
        boolean recommendAvailable = false;
        TMResult<Item> cancelResp = ApiJdpAdapter.doCancel(user, toCancelNumIid);
//        log.warn(" cancel  res:" + cancelResp);
        if (cancelResp != null && cancelResp.isOk()) {
//            CheckNoDownShelfJob.tagItemRecentCaneled(toCancelNumIid);
            OpLogWritter.addMsg(user.getId(), "取消橱窗", toCancelNumIid, LogType.ShowWindow, false);
            recommendAvailable = true;
        } else {
            if (cancelResp == null) {
                log.error("no res --:" + toCancelNumIid + " with user:" + user.toIdNick());
            } else if ("isv.item-recommend-service-error:ITEM_NOT_FOUND".equals(cancelResp.getCode())) {
                recommendAvailable = true;
            } else {
                recommendAvailable = true;
            }
        }

        if (!recommendAvailable) {
            return;
        }

        boolean recommendRes = false;
        Set<Long> currOn = OnWindowItemCache.get().removeItem(user, toCancelNumIid);

//        log.warn("[cancel for user: :]" + user.toIdNick() + " with numiid :" + toCancelNumIid);
//        log.warn("[removed window size:]" + currOn.size());

        // 已经考虑了大量因为 同时上架的宝贝的情况，确保不会出问题
        singleRecommend(currOn);

//        log.warn(" recommend res:" + cancelResp);
//        Set<Long> cacheWindowId = OnWindowItemCache.get().getIds(user, false);
//        Set<Long> realWindowId = OriginApiImpl.get().findCurrOnWindowNumIids(user);
//
//        Set<Long> baseCached = new HashSet<Long>(cacheWindowId);
//        baseCached.removeAll(new ArrayList<Long>(realWindowId));
//
//        Set<Long> realIds = new HashSet<Long>(realWindowId);
//        realIds.removeAll(new ArrayList<Long>(cacheWindowId));
//        if (!CommonUtils.isEmpty(realIds) || !CommonUtils.isEmpty(baseCached)) {
//            log.warn(" cached extra ids:" + baseCached + "  ----- real window id:" + realIds);
//        }
    }

    List<Item> tempRawItemds = null;

    private List<Item> fetchRawItems() {

        if (tempRawItemds != null) {
            return tempRawItemds;
        }

        int maxWindowNum = OperateItemApi.getUserTotalWindowNum(user);

        int candidate = maxWindowNum * 2;
        if (candidate > 100 || candidate < 0) {
            candidate = 100;
        }
        tempRawItemds = ItemDao.recentDownRawItems(user, candidate);
        return tempRawItemds;
    }

    public void singleRecommend(Set<Long> currOn) {
        int nextAvailable = 1;
        boolean recommendRes;
        boolean remainChecked = false;
        Set<Long> recommendedIds = new HashSet<Long>();

        List<Item> items = fetchRawItems();
        int size = items.size();
        if (WindowMoreRecommend.hasId(user.getId())) {
            nextAvailable = 2;
        }

        for (int i = 0; i < size; i++) {
            Item item = items.get(i);
            boolean isCandidate = condition.isCandidate(item, currOn);
            if (!isCandidate) {
                continue;
            }
            recommendedIds.add(item.getNumIid());
            Long toRecommendId = item.getNumIid();

            TMResult<Item> api = ApiJdpAdapter.doRecommend(user, toRecommendId);
            if (api != null && api.isOk()) {
                String msg = "橱窗商品推荐成功";
                OpLogWritter.addMsg(user.getId(), msg, toRecommendId, LogType.ShowWindow, false);
                UserTracerWritter.addShowWindowMsg(user.getId());
                OnWindowItemCache.get().addItem(user, toRecommendId);
                CommonUtils.sleepQuietly(ShowWindowExecutor.SLEEP_TIME);
                recommendRes = true;
            } else {
                if (api == null) {
                    log.error(" no rees????? :" + user.toIdNick());
                    recommendRes = false;
                } else if (ErrorHandler.isRecommendMaxReached(user, api)) {
                    log.warn("max reached ..let's go:" + user + "\n");
                    return;
                }

                ShowWindowExecutor.checkForTheRecommendFail(api, user, toRecommendId, condition.getMustIds());
                log.error("failed for numiid :" + toRecommendId);
                OpLogWritter.addMsg(user.getId(), "橱窗商品推荐失败", toRecommendId, LogType.ShowWindow, true);
                CommonUtils.sleepQuietly(ShowWindowExecutor.SLEEP_TIME);
                recommendRes = false;
            }

            if (!recommendRes) {
                continue;
            }
            nextAvailable--;
            if (nextAvailable > 0) {
                continue;
            }
            if (nextAvailable == 0 && remainChecked) {
                break;
            }
            remainChecked = true;
//            if (user.isTmall()) {
//                continue;
//            }
            nextAvailable = ShowCaseInfo.build(user).getRemainWindowCount();
            if (nextAvailable > 0) {
                nextAvailable += 5;
            }
            log.info(" start to check remain : " + nextAvailable + " for user:" + user.toIdNick());
            if (nextAvailable == 0) {
                break;
            }
        }

    }

    @Override
    public String toString() {
        return "WindowReplaceJob [numIid=" + toCancelNumIid + ", delistTime=" + delistTime + ", user="
                + user.toIdNick() + ", condition=" + condition + "]";
    }

}
