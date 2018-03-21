
package job.showwindow;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import jdp.ApiJdpAdapter;
import job.showwindow.ShowWindowInitJob.ShowCaseInfo;
import job.showwindow.ShowWindowInitJob.WindowItemInfo;
import job.writter.OpLogWritter;
import job.writter.UserTracerWritter;
import models.item.ItemPlay;
import models.oplog.OpLog.LogType;
import models.showwindow.OnWindowItemCache;
import models.showwindow.ShowWindowConfig;
import models.showwindow.ShowwindowExcludeItem;
import models.showwindow.ShowwindowMustDoItem;
import models.showwindow.ShowwindowTmallTotalNumFixedNum;
import models.user.User;

import org.apache.commons.collections.SetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.cache.Cache;
import play.jobs.Job;
import result.TMResult;
import service.WindowsService;
import bustbapi.ErrorHandler;
import bustbapi.OperateItemApi;
import cache.UserHasTradeItemCache;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.Item;

import configs.TMConfigs;
import configs.TMConfigs.ShowWindowParams;
import dao.UserDao;
import dao.item.sub.ItemDeletedCheckJob;

public class ShowWindowExecutor extends Job {

    private static final Logger logger = LoggerFactory.getLogger(ShowWindowExecutor.class);

    public static final String TAG = "ShowWindowExecutor";

//    private static long minGap = 10000L;

    protected User user;

    protected int available = 0;

    protected boolean debug = Play.mode.isDev();

    public static class WindowCondition {

        private Set<Long> mustIds;

        private Set<Long> excludeIds;

        private ShowWindowConfig config;

        private Set<Long> hasSaleIds;

        private int minStock = Integer.MAX_VALUE;

        public WindowCondition(User user) {
            Long userId = user.getId();
            this.mustIds = ShowwindowMustDoItem.findIdsByUser(userId);
            this.excludeIds = ShowwindowExcludeItem.findIdsByUser(userId);
            this.config = ShowWindowConfig.findOrCreate(userId);
            this.hasSaleIds = new HashSet<Long>();
            List<ItemPlay> list = UserHasTradeItemCache.getByUser(user, config.getPriorSaleNum());
            for (ItemPlay itemPlay : list) {
                hasSaleIds.add(itemPlay.getNumIid());
            }
            this.minStock = config.checkMinStockNum();
        }

        public static WindowCondition getByUser(User user) {
            return new WindowCondition(user);
        }

        public void fiterForItem(List<Item> recentDownItems) {
            Iterator<Item> it = recentDownItems.iterator();
            while (it.hasNext()) {
                Item item = it.next();
                Long numIid = item.getNumIid();
                if (mustIds.contains(numIid)) {
                    it.remove();
                    continue;
                }
                if (excludeIds.contains(numIid)) {
                    it.remove();
                    continue;
                }
                if (hasSaleIds.contains(numIid)) {
                    it.remove();
                    continue;
                }
                if (item.getNum() < minStock) {
                    it.remove();
                    continue;
                }
            }
        }

        public boolean shouldBeAlwaysThere(Long numIid) {
            if (mustIds.contains(numIid)) {
                return true;
            }
            if (hasSaleIds.contains(numIid)) {
                return true;
            }

            return false;
        }

        public boolean isCandidate(Item item, Set<Long> onWindowNumIids) {
            Long numIid = item.getNumIid();
            if (onWindowNumIids.contains(numIid)) {
                return false;
            }
            if (excludeIds.contains(numIid)) {
                return false;
            }
            if (item.getNum() < minStock) {
//                logger.warn(" not availabe stock with numIid :[" + numIid + "] stock :" + item.getNum() + " with min:"
//                        + minStock);
                return false;
            }

            return true;
        }

        public Set<Long> getMustIds() {
            return mustIds;
        }

        public void setMustIds(Set<Long> mustIds) {
            this.mustIds = mustIds;
        }

        public Set<Long> getExcludeIds() {
            return excludeIds;
        }

        public void setExcludeIds(Set<Long> excludeIds) {
            this.excludeIds = excludeIds;
        }

        public ShowWindowConfig getConfig() {
            return config;
        }

        public void setConfig(ShowWindowConfig config) {
            this.config = config;
        }

        public Set<Long> getHasSaleIds() {
            return hasSaleIds;
        }

        public void setHasSaleIds(Set<Long> hasSaleIds) {
            this.hasSaleIds = hasSaleIds;
        }

        public int getMinStock() {
            return minStock;
        }

        public void setMinStock(int minStock) {
            this.minStock = minStock;
        }

        @Override
        public String toString() {
            return "WindowCondition [mustIds=" + mustIds + ", excludeIds=" + excludeIds + ", config=" + config
                    + ", minStock=" + minStock + "]";
        }

    }

//    List<Item> onWindowItems;

    protected Set<Long> onWindowNumIids = new HashSet<Long>();

    protected Set<Long> mustIds = SetUtils.EMPTY_SET;

    protected Set<Long> excludeIds = SetUtils.EMPTY_SET;

    public static int MUST_RECOMMEND_BY_TRADE_ORDER_NUM = 15;

//    static int MORE_ITEMS_IN_REVERSE = 300;

    public static int TmallWindowSize = 300;

    protected ShowWindowConfig config = null;

    boolean lightweighRecommend = false;

    List<Long> candidateItemId = new ArrayList<Long>();

    Map<Long, Long> candidatesToDelistTime = new HashMap<Long, Long>();

    public static long SLEEP_TIME = 800L;

    public static final int MORE_ITEMS_IN_REVERSE = 40;

    public ShowWindowExecutor(User user) {
        this(user, ShowwindowMustDoItem.findIdsByUser(user.getId()), ShowwindowExcludeItem.findIdsByUser(user.getId()));
    }

    public ShowWindowExecutor(User user, boolean lightweighRecommend) {
        this(user, ShowwindowMustDoItem.findIdsByUser(user.getId()), ShowwindowExcludeItem.findIdsByUser(user.getId()));
        this.user = user;
        this.lightweighRecommend = lightweighRecommend;
//        this.config = ShowWindowConfig.findOrCreate(user.getId());
    }

    private ShowWindowExecutor(User user, Set<Long> mustIds, Set<Long> excludedIds) {
        super();
        this.user = user;
        this.mustIds = mustIds;
        this.excludeIds = excludedIds;
        this.config = ShowWindowConfig.findOrCreate(user.getId());

        if (CommonUtils.isEmpty(mustIds)) {
            return;
        }
    }

    public static class DeleteNumIidCacheControl {

        static String tag = "DeleteNumIidCache";

        public static void addDeleteItem(Long numIid) {
            if (NumberUtil.isNullOrZero(numIid)) {
                return;
            }

            Cache.set(tag + numIid, numIid);
        }

        public static boolean isDeleted(Long numIid) {
            if (NumberUtil.isNullOrZero(numIid)) {
                return true;
            }

            return Cache.get(tag + numIid) != null;
        }
    }

    public static class CacheControl {

        static String EXPIRED_TIME = "2min";

        static long EXPIRED_MILLIS = 2 * 60000L;

        private static String getCacheKey(Long userId) {
            return TAG + userId;
        }

        public static boolean isDoing(Long userId) {
            return Cache.get(getCacheKey(userId)) != null;
        }

        public static void setUserUpdate(Long userId) {
            Cache.safeSet(getCacheKey(userId), System.currentTimeMillis(), EXPIRED_TIME);
        }

        public static void free(Long id) {
            String cacheKey = getCacheKey(id);
            Cache.safeDelete(cacheKey);
        }
    }

    private boolean isAvaialbeToDo() {
        if (user == null || !user.isVaild()) {
            return false;
        }

        int retry = 8;
        if (lightweighRecommend) {

        } else {

            while (CacheControl.isDoing(user.getId())) {
                CommonUtils.sleepQuietly(3000L);
                retry--;
                if (retry <= 0) {
                    WindowsService.addQueue(user.getId());
                    logger.warn("user is doing :" + user);
                    return false;
                }
            }

        }
        CacheControl.setUserUpdate(user.getId());

        return true;
    }

    @Override
    public void doJob() {

        if (!isAvaialbeToDo()) {
            return;
        }
        if (!user.isShowWindowOn()) {
            return;
        }

//        ShowCaseInfo info = ShowCaseInfo.build(user);

//        if (info == null) {
//            logger.warn("no build for the user:" + user);
//            CacheControl.free(user.getId());
//        }

//        if (user.isTmall() && info == null) {
//            info = new ShowCaseInfo(onWindowNumIids.size(), TmallWindowSize, TmallWindowSize - onWindowNumIids.size());
//        }

        //生成候选列表

        //取候选列表排名前toShowNumber个
        int windowNum = user.isTmall() ? ShowwindowTmallTotalNumFixedNum.findOrCreate(user) : OperateItemApi
                .getUserTotalWindowNum(user);
        onWindowNumIids = OnWindowItemCache.get().refresh(user);
        int canceledCount = 0;
        available = windowNum - onWindowNumIids.size();

        if (debug) {
            logger.error("total window num [" + windowNum + "] To show num:[" + candidateItemId.size() + "]"
                    + " onwindow  num: [" + onWindowNumIids.size() + "] + available :" + available);
        }

        buildCandidateItemIdList();
        /**
         * isv.item-recommend-service-error:ITEM_NOT_FOUND
         */
        if (!lightweighRecommend) {
            int maxToShowNumber = (windowNum > candidateItemId.size()) ? candidateItemId.size() : windowNum;
            canceledCount = cancelToDeshowItems(candidateItemId.subList(0, maxToShowNumber));
//            CommonUtils.sleepQuietly(8000L);
            if (canceledCount > 0) {
                onWindowNumIids = OnWindowItemCache.get().refresh(user);
            }
        }

        if (debug) {
            logger.info("[max to show num: ]" + windowNum + " wi th on window num :" + onWindowNumIids.size()
                    + " and canciddate: size: :" + candidateItemId.size());
        }

        doRecommend(windowNum - onWindowNumIids.size(), candidateItemId.size());
        refix();

        CacheControl.free(user.getId());
        CommonUtils.sleepQuietly(500L);
//        if (debug) {
//            refix();
//        }
//        new ComputeUserWindowInfo(user).now();

//        info = new ShowCaseInfo(user);
//        info = ShowCaseInfo.build(user);
//
//        if (info.getRemainWindowCount() == 0) {
////            logger.info("remian windows number is 0, windows is full!!!!!!!!" + " for user::" + user.getUserNick());
//        } else if (info.getRemainWindowCount() > 0) {
////            logger.info("remian windows number is: " + info.getRemainWindowCount() + "refix noooooooot full!!!"
////                    + " for user::" + user.getUserNick());
//        }

    }

    public void doRecommend(int recommendAvailable, int toShowNum) {

        if (debug) {
            logger.info(format("doRecommend:recommendAvailable, toShowNum".replaceAll(", ", "=%s, ") + "=%s",
                    recommendAvailable, toShowNum));
        }
//        recommendAvailable = ShowCaseInfo.build(user).getRemainWindowCount();
        int count = 0;
        String msg = null;

        while (recommendAvailable > 0 && count < toShowNum) {
            Long numIid = candidateItemId.get(count++);

            // try to add to this to show case..
            if (onWindowNumIids.contains(numIid) || excludeIds.contains(numIid)) {
                continue;
            }

            TMResult<Item> api = ApiJdpAdapter.doRecommend(user, numIid);
            if (api != null && api.isOk()) {
                recommendAvailable--;
                msg = "橱窗商品推荐成功";
                recommendedItems.add(numIid);
                OnWindowItemCache.get().addItem(user, numIid);
                OpLogWritter.addMsg(user.getId(), msg, numIid, LogType.ShowWindow, false);
                UserTracerWritter.addShowWindowMsg(user.getId());
                CommonUtils.sleepQuietly(ShowWindowExecutor.SLEEP_TIME);
            } else {
                if (api == null) {
                    logger.error(" no rees????? :" + user.toIdNick());
                    continue;
                } else if (ErrorHandler.isRecommendMaxReached(user, api)) {
                    logger.warn("max reached ..let's go:" + user + "\n");
                    break;
                }

                checkForTheRecommendFail(api, user, numIid, mustIds);
                logger.error("failed for numiid :" + numIid);
                OpLogWritter.addMsg(user.getId(), "橱窗商品推荐失败", numIid, LogType.ShowWindow, true);
                CommonUtils.sleepQuietly(ShowWindowExecutor.SLEEP_TIME);
            }

        }

        if (recommendAvailable > 0) {
//            logger.info("FUNCKING!!!!!!!!Need to refix the windows for user: " + user.getUserNick());
        }
    }

    public static void checkForTheRecommendFail(TMResult<Item> api, User user, Long numId, Set<Long> mustIds) {
        String code = api.getCode();
        if ("isv.item-recommend-service-error:ITEM_NOT_FOUND".equals(code)
                || "isv.item-recommend-service-error:ITEM_NOT_FOUND-tmall".equals(code)
                || "isv.item-is-delete:invalid-numIid-or-iid".equals(code)) {
            DeleteNumIidCacheControl.addDeleteItem(numId);
            if (mustIds.contains(numId)) {
                logger.warn("fuck..... mustids contains deleted ids:" + numId);
                DeleteNumIidCacheControl.addDeleteItem(numId);
                ShowwindowMustDoItem.remove(user.getId(), numId);
            } else if (ShowWindowParams.enableItemTradeCache) {
                logger.info("try fix the cache");
                UserHasTradeItemCache.fixCacheForItemRemoved(user, numId);
            }

            ItemDeletedCheckJob.tryFixDeleteItem(user, numId);
        } else if ("isp.item-recommend-service-unavailable".equals(code)) {
            // "橱窗推荐商品异常！"
        }
    }

    private Set<Long> recommendedItems = new HashSet<Long>();

    private int cancelToDeshowItems(List<Long> toShow) {
        int canceledCount = 0;

        if (debug) {
            logger.info(" refix the on window numiid :" + onWindowNumIids);
        }
//        onWindowNumIids = ApiJdpAdapter.get(user).findCurrOnWindowNumIids(user);
        //设置要取消推荐的宝（不在toShow中的宝贝）
        List<Long> toCancelIds = new ArrayList<Long>();
        for (Long numIid : onWindowNumIids) {
            if (!toShow.contains(numIid)) {
                toCancelIds.add(numIid);
            } else if (excludeIds.contains(numIid)) {
                toCancelIds.add(numIid);
            }
        }

        if (CommonUtils.isEmpty(toCancelIds)) {
            return canceledCount;
        }
        if (debug) {
            logger.warn("to cancel num:" + toCancelIds);
        }

        for (Long numIid : toCancelIds) {
//        DeleteRecommend api = ApiJdpAdapter.get(user).cancelItemRecommend(user, numIid);
//        if (api == null) {
//            logger.warn(" No need to call api : for " + numIid + " as item is already not on...");
//            canceledCount++;
//            continue;
//        }

            TMResult<Item> res = ApiJdpAdapter.doCancel(user, numIid);
            if (res != null && res.isOk()) {
                canceledCount++;
                OnWindowItemCache.get().removeItem(user, numIid);
                CheckNoDownShelfJob.tagItemRecentCaneled(numIid);
                onWindowNumIids.remove(numIid);
                OpLogWritter.addMsg(user.getId(), "由于下架时间非常遥远_取消橱窗", numIid, LogType.ShowWindow, false);
                CommonUtils.sleepQuietly(ShowWindowExecutor.SLEEP_TIME);
            } else {
                if (res == null) {
                    logger.error("no res --:" + numIid + " with user:" + user.toIdNick());
                } else if ("isv.item-recommend-service-error:ITEM_NOT_FOUND".equals(res.getCode())) {
                    canceledCount++;
                }
            }

        }

        return canceledCount;
    }

    public static boolean isCandidateItem(Item item, ShowWindowConfig config, Set<Long> onWindowNunIids,
            Set<Long> mustIds, Set<Long> exclude) {

        Long currentId = item.getNumIid();
        if (item.getDelistTime() == null) {
            return false;
        }
        if (onWindowNunIids.contains(currentId)) {
            return false;
        }
        if (mustIds.contains(currentId)) {
            return true;
        }

        if (exclude.contains(currentId)) {
            return false;
        }
        if (item.getDelistTime().getTime() - System.currentTimeMillis() <= 0L) {
            return false;
        }
        int stockNum = item.getNum() == null ? Integer.MAX_VALUE : item.getNum().intValue();
//      log.error("[config:]" + config + " \n : stock num:" + stockNum);
        if (stockNum < config.checkMinStockNum()) {
            return false;
        }

        return true;
    }

    /**
     * In some case, the window can't be fully used, but this is why????
     */
    private void refix() {
        ShowCaseInfo build = ShowCaseInfo.build(user);
        int total = build.getTotalWindowCount();
        int remain = build.getRemainWindowCount();
        int onshow = build.getOnShowItemCount();
        if (debug) {
            logger.error(" current remain :" + remain + " and current build: " + build);
        }
//        remain = total;
        if (remain <= 0) {
            return;
        }
        if (onshow >= total) {
        	return;
        }

        remain += 10;
//        List<Item> refixOnWindowItems = ApiJdpAdapter.get(user).findCurrOnWindowItems(user);
//        Set<Long> refixOnWindowNumIids = ShowWindowApi.toNumIids(refixOnWindowItems);
//        Set<Long> refixOnWindowNumIids = ApiJdpAdapter.get(user).findCurrOnWindowNumIids(user);
        Set<Long> refixOnWindowNumIids = OnWindowItemCache.get().getIds(user, false);

        int fetchDelistItemNum = total + excludeIds.size() + MORE_ITEMS_IN_REVERSE + refixOnWindowNumIids.size();
        List<Item> delistItems = ApiJdpAdapter.get(user).findRecentDownItems(user, fetchDelistItemNum);
        if (delistItems != null && delistItems.size() > 0) {
            for (Item item : delistItems) {
                if (remain <= 0) {
                    break;
                }

                Long numIid = item.getNumIid();
                if (recommendedItems.contains(numIid)) {
                    continue;
                }
                if (!isCandidateItem(item, config, refixOnWindowNumIids, mustIds, excludeIds)) {
                    continue;
                }

                TMResult<Item> tmResult = ApiJdpAdapter.doRecommend(user, numIid);

                if (tmResult != null && tmResult.isOk()) {
                    // null的情况已经在橱窗上了，跟成功效果一样
                    remain--;
                    String msg = "橱窗商品推荐成功";
                    OnWindowItemCache.get().addItem(user, numIid);
                    OpLogWritter.addMsg(user.getId(), msg, numIid, LogType.ShowWindow, false);
                    UserTracerWritter.addShowWindowMsg(user.getId());

//                    if (!mustIds.contains(numIid)) {
//                        ShowWindowRecommendWritter.addRecommend(user.getId(), numIid);
//                    }
                } else {
                    if (ErrorHandler.isRecommendMaxReached(user, tmResult)) {
//                        logger.error("max recommend reached, let's go....");
                        break;
                    }
                    logger.error("failed for :" + item.getNumIid());
                    OpLogWritter.addMsg(user.getId(), "橱窗商品推荐失败", item.getNumIid(), LogType.ShowWindow, true);
                }
            }
        }

    }

    public List<Long> buildCandidateItemIdList() {
        if (debug) {
            logger.warn(" build candidates item list " + user);
        }
        //add must
        candidateItemId.addAll(mustIds);

        //add the first 10 salesCounts if necessary
        //if (user.isSalesCountOn()) {
        List<ItemPlay> saleCountItemPlays = UserHasTradeItemCache.getByUser(user, config.checkPrioSaleNum());

//        logger.info("[sale list size:]" + saleCountItemPlays.size());

        int saleListSize = saleCountItemPlays.size();
        int containSale = 0;
        for (int i = 0; i < saleListSize; i++) {
            ItemPlay itemPlay = saleCountItemPlays.get(i);
            Long id = itemPlay.getNumIid();
            if (mustIds.contains(id)) {
                int j = candidateItemId.indexOf(id);
                Collections.swap(candidateItemId, containSale++, j);
                continue;
            }

            if (excludeIds.contains(id) || candidateItemId.contains(id) || itemPlay.getSalesCount() <= 0) {
                continue;
            }

//            logger.warn("item :" + id + "time:[" + DateUtil.formDateForLog(itemPlay.getDeListTime()) + "]"
//                    + " with a stocknum ::" + itemPlay.getQuantity() + "  white config is :" + config);

            if (itemPlay.getQuantity() < config.checkMinStockNum()) {
                continue;
            }
            candidateItemId.add(itemPlay.getId());
//            logger.info("[add for sale count :]" + itemPlay);

        }

//        logger.info("[candidates:]" + candidateItemId);
//        logger.error(" has sale itemplays : " + hasSaleItemPlays);

        // TODO get the delist items on sale by page...
        int totalWindowNum = OperateItemApi.getUserTotalWindowNum(user);

        int maxFetchNum = totalWindowNum + excludeIds.size() + MORE_ITEMS_IN_REVERSE;
        if (maxFetchNum > 500) {
            maxFetchNum = 500;
        }

        List<Item> toDelistItems = ApiJdpAdapter.get(user).findRecentDownItems(user, maxFetchNum);
//        logger.warn("[found recent down item size: :]" + toDelistItems.size());

        if (toDelistItems != null && toDelistItems.size() > 0) {
            long now = System.currentTimeMillis();
            for (Item item : toDelistItems) {
                Long id = item.getNumIid();
                if (excludeIds.contains(id) || candidateItemId.contains(id)) {
                    continue;
                }
                if (item.getDelistTime() == null) {
//                    logger.warn("item :" + id + " with no time:");
                    continue;
                }
                if (item.getDelistTime().getTime() - now < 0L) {
//                    logger.warn("item :" + id + " with a delist time:" + item.getDelistTime());
                    continue;
                }

//                log.info("[stock:" + item.getNum());
                int stockNum = item.getNum() == null ? Integer.MAX_VALUE : item.getNum().intValue();
                if (stockNum < config.checkMinStockNum()) {
//                    logger.warn("item :" + id + " with a stocknum ::" + stockNum + "  white config is :" + config);
                    continue;
                }

                candidateItemId.add(item.getNumIid());
                candidatesToDelistTime.put(item.getNumIid(), item.getDelistTime().getTime());
//                logger.info("[candidates:]" + candidateItemId);
            }
        }

//        logger.info("[buidl cancidates:]" + candidateItemId.size());
//        logger.info("[candidates:]" + candidateItemId);
        return candidateItemId;
    }

//    private static final Logger log = LoggerFactory.getLogger(ShowWindowExecutor.class);

    public static class PrintUserWinwdowStatus extends Job {
        public void doJob() {
            new UserDao.UserBatchOper(128) {
                @Override
                public void doForEachUser(User user) {
                    if (!user.isVaild()) {
                        return;
                    }
                    printUserWindowStatus(user);
                }
            }.call();
        }

        private void printUserWindowStatus(User user) {
            WindowItemInfo build = new WindowItemInfo(user);
            logger.info("[build :]" + build + " for user id :" + user.getId());
        }
    }

    public String doMatch() {
        List<Long> candidates = buildCandidateItemIdList();
        Set<Long> onWindowItems = ApiJdpAdapter.get(user).findCurrOnWindowNumIids(user);
        ShowCaseInfo info = ShowCaseInfo.build(user);
        int maxToShowNumber = info.getTotalWindowCount();
        maxToShowNumber = (maxToShowNumber > candidates.size()) ? candidates.size() : maxToShowNumber;
        candidates = candidates.subList(0, maxToShowNumber);
//        Set<Long> candiateSet = new HashSet<Long>(candidates);
        Iterator<Long> it = candidates.iterator();
        while (it.hasNext()) {
            Long next = it.next();
            if (onWindowItems.contains(next)) {
                onWindowItems.remove(next);
                it.remove();
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(" user final num : candidate [" + candidates.size() + "] and on window more :["
                + onWindowItems.size() + "] for user:" + user);
//        logger.info("[not on candidates]" + candidates);

        return sb.toString();
    }

    public static void delayExec(final User user) {
        TMConfigs.getShowwindowPool().submit(new Callable<ItemPlay>() {
            @Override
            public ItemPlay call() throws Exception {
                CommonUtils.sleepQuietly(10000L);
                new ShowWindowExecutor(user).doJob();
                return null;
            }
        });
    }

}
