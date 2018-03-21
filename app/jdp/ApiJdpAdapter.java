
package jdp;

import static java.lang.String.format;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import jdp.JdpModel.JdpItemModel;
import job.diagjob.PropDiagJob;
import job.message.DeleteItemJob;
import job.showwindow.ShowWindowExecutor;
import job.showwindow.ShowWindowInitJob.ShowCaseInfo;
import job.showwindow.ShowWindowInitJob.WindowItemInfo;
import models.item.ItemPlay;
import models.op.RawId;
import models.oplog.TMUserWorkRecord;
import models.oplog.TMWorkLog;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.jobs.Job;
import result.TMResult;
import service.WindowsService;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import bustbapi.ItemApi;
import bustbapi.JDPApi;
import bustbapi.JDPApi.JdpItemStatus;
import bustbapi.JDPApi.JuDataType;
import bustbapi.JDPApi.JuShiTaDataDeleteApi;
import bustbapi.OperateItemApi;
import bustbapi.OperateItemApi.ItemRecentDownGet;
import bustbapi.OperateItemApi.OnWindowItemsDelistDesc;
import bustbapi.OperateItemApi.OnWindowNumIids;
import bustbapi.ShowWindowApi;
import bustbapi.ShowWindowApi.AddRecommend;
import bustbapi.ShowWindowApi.DeleteRecommend;
import bustbapi.TBApi;

import com.ciaosir.client.CommonUtils;
import com.google.gson.Gson;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.Task;

import configs.TMConfigs;
import configs.TMConfigs.Rds;
import controllers.APIConfig;
import dao.UserDao;
import dao.item.ItemDao;

public abstract class ApiJdpAdapter {

    static final Logger log = LoggerFactory.getLogger(ApiJdpAdapter.class);

    public static final String TAG = "ApiAdapter";

    public abstract ShowCaseInfo buildShowCase(User user);

    /**
     * This method might return something that is not synced...
     * @param user
     * @param maxFetchNum
     * @return
     */
    public abstract List<Item> findRecentDownItems(User user, int maxFetchNum);

    public abstract List<Item> findCurrOnWindowItems(User user);

    public abstract Set<Long> findCurrOnWindowNumIids(User user);

    public abstract DeleteRecommend cancelItemRecommend(User user, Long numIid);

    public abstract TBApi addItemRecommend(User user, Long numIid);

    public abstract Long onSaleItemNum(User user);

    public abstract Long inventoryItemNum(User user);

    public abstract Item findItem(User user, Long numIid);

//    public abstract boolean fixDeletedItem(User user, Long id);

    public abstract List<Item> OnWindowItemsDelistDesc(User user, int maxFetchNum);

//    public static boolean useJdp(User user) {
//        return Rds.enableJdpPush && RawId.hasId(user.getId());
//    }

    public static boolean enableJdp(User user) {
//        if (Play.mode.isDev() && user != null && user.getId().longValue() == 528614075L) {
//            return true;
//        }
        if (true) {
            return false;
        }

        boolean res = Rds.enableJdpPush && Rds.enableJdpApi
                && APIConfig.get().getApp() == APIConfig.taoxuanci.getApp();
//        ;

        if (!res) {
            return false;
        }

        if (!Rds.checkUserWithInRawId) {
            return true;
        }

        if (user == null) {
            return res;
        }
        if (RawId.hasId(user.getId())) {
            /*
             * 白名单过滤机制,白名单内启用api调用
             */
            return false;
        } else {
            return true;
        }

    }

    public static ApiJdpAdapter get(User user) {

        if (enableJdp(user)) {
            return JdpApiImpl.get();
        } else {
            return OriginApiImpl.get();
        }

//        return OriginApiImpl.get();
    }

    public void doItemRecommend() {
    }

    public void doItemCancelRecommend() {
    }

    /**
     *
     * @return
     */
    public static Item singleItem(User user, Long numIid) {
        if (TMConfigs.Rds.enableJdpPush) {
            Item item = JdpItemModel.findByNumIid(user == null ? null : user.getId(), numIid);
            if (item != null) {
                return item;
            }
        }

        Item item = new ItemApi.ItemGet(user, numIid, true).call();
        return item;
    }

    public static class OriginApiImpl extends ApiJdpAdapter {
        static OriginApiImpl _instance = new OriginApiImpl();

        public static OriginApiImpl get() {
            return _instance;
        }

        @Override
        public ShowCaseInfo buildShowCase(User user) {
            return new ShowCaseInfo(user);
        }

        @Override
        public DeleteRecommend cancelItemRecommend(User user, Long numIid) {
//            log.info("[old cancel]" + user.getId() + " for numIid;" + numIid);
            DeleteRecommend api = new ShowWindowApi.DeleteRecommend(user, numIid);
//            String msg = api.call();
//            api.call();
            return api;
        }

        @Override
        public TBApi addItemRecommend(User user, Long numIid) {
            AddRecommend api = new ShowWindowApi.AddRecommend(user, numIid);
//            CommonUtils.sleepQuietly(LightWeightRecommend.SLEEP_TIME);
            return api;
        }

        @Override
        public List<Item> findRecentDownItems(User user, int maxFetchNum) {
            return new ItemRecentDownGet(user, maxFetchNum).call();
        }

        @Override
        public List<Item> findCurrOnWindowItems(User user) {
            return new OperateItemApi.ItemsOnWindowInit(user).call();
        }

//        @Override
        public boolean doDeletedItem(User user, Long id) {
            try {
                Item call = new ItemApi.ItemGet(user, id, false).call();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);

            }
            return true;
        }

        @Override
        public Long onSaleItemNum(User user) {
            return new ItemApi.ItemsOnsaleCount(user.getSessionKey(), null, null).call();
        }

        @Override
        public Long inventoryItemNum(User user) {
            return new ItemApi.ItemsInventoryCount(user, null, null).call();
        }

        @Override
        public Item findItem(User user, Long numIid) {
            //return new ItemGet(user, numIid).call();
            return new ItemApi.ItemGet(user, numIid, true).call();
        }

        @Override
        public List<Item> OnWindowItemsDelistDesc(User user, int maxFetchNum) {
            return new OnWindowItemsDelistDesc(user, maxFetchNum).call();
        }

        @Override
        public List<Item> tryItemList(User user, Collection<Long> idsList) {
            return ItemApi.tryItemList(user, idsList, true);
        }

        @Override
        public Set<Long> findCurrOnWindowNumIids(User user) {
            return new OnWindowNumIids(user).call();
//            List<Item> items = findCurrOnWindowItems(user);
//            return ShowWindowApi.toNumIids(items);
        }

    }

    public static class JdpApiImpl extends ApiJdpAdapter {
        DataSrc src = DataSrc.JDP;

        static JdpApiImpl _instance = new JdpApiImpl();

        public static JdpApiImpl get() {
            return _instance;
        }

        @Override
        public ShowCaseInfo buildShowCase(User user) {
//            int totalNum = OperateItemApi.getUserTotalWindowNum(user);
//            int onWindow = JdpItemModel.countOnWindow(user);
//            if (onWindow > totalNum) {
//                onWindow = totalNum;
//            }
//
//            return new ShowCaseInfo(onWindow, totalNum, totalNum - onWindow);
            return new ShowCaseInfo(user);
        }

        @Override
        public DeleteRecommend cancelItemRecommend(User user, Long numIid) {
            boolean isOn = isOnRecommend(user, numIid);
            log.info("is on [" + isOn + "] for user:" + user.getUserNick() + " with numIid: " + numIid);
            if (isOn) {
                return OriginApiImpl.get().cancelItemRecommend(user, numIid);
            } else {
                return null;
            }
        }

        @Override
        public TBApi addItemRecommend(User user, Long numIid) {
            boolean isOn = isOnRecommend(user, numIid);
            if (isOn) {
                return null;
            } else {
                return OriginApiImpl.get().addItemRecommend(user, numIid);
            }
        }

        static String hasShowCaseSql = "select has_showcase from jdp_tb_item where num_iid = ? limit 1 ";

        /**
         * @param user
         * @param numIid
         * @return
         */
        public boolean isOnRecommend(User user, long numIid) {

            String showCaseStr = JDBCBuilder.singleStringQuery(src, hasShowCaseSql, numIid);
            if (showCaseStr == null) {
                log.warn(format("No item info ???isOnRecommend:user, numIid".replaceAll(", ", "=%s, ") + "=%s", user,
                        numIid));

                // Not in the modified db... Get the item in the database..
                List<Item> items = new OperateItemApi.ItemsOnWindowInit(user, true).call();
                if (CommonUtils.isEmpty(items)) {
                    return false;
                }
                for (Item item : items) {
                    if (item.getNumIid().longValue() == numIid) {
                        return true;
                    }
                }
                return false;
            }

            if ("true".equals(showCaseStr)) {
                // TODO it's on....
                return true;
            } else {
                return false;
            }
        }

        @Override
        public List<Item> findRecentDownItems(User user, int maxFetchNum) {
            return ItemDao.recentDownRawItems(user, maxFetchNum);
        }

        @Override
        public List<Item> findCurrOnWindowItems(User user) {
            return JdpItemModel.jdpItemFetcher(" nick = ? and  has_showcase = 'true' and jdp_delete = 0 ",
                    user.getUserNick());

        }

//        @Override
        public boolean doDeletedItem(User user, Long numIid) {
            boolean isDeleted = JdpItemModel.isItemRecentDeleted(numIid);
            if (isDeleted) {
                DeleteItemJob.tryDeleteItem(user.getId(), numIid);
            }
            return isDeleted;
        }

        @Override
        public Long onSaleItemNum(User user) {
            return new Long(JdpItemModel.countOnSaleItem(user));
        }

        @Override
        public Long inventoryItemNum(User user) {
            return new Long(JdpItemModel.countInventoryItems(user));
        }

        @Override
        public Item findItem(User user, Long numIid) {
            return JdpItemModel.findByNumIid(user == null ? null : user.getId(), numIid);
        }

        @Override
        public List<Item> OnWindowItemsDelistDesc(User user, int maxFetchNum) {
            List<Item> items = this.findCurrOnWindowItems(user);
            Collections.sort(items, ItemApi.ItemDelistDescComparator);
            if (maxFetchNum > items.size()) {
                items = items.subList(0, maxFetchNum);
            }
            return items;
        }

        @Override
        public List<Item> tryItemList(User user, Collection<Long> idsList) {
            if (CommonUtils.isEmpty(idsList)) {
                return ListUtils.EMPTY_LIST;
            }

            return JdpItemModel.jdpItemFetcher(" num_iid in (" + StringUtils.join(idsList, ',') + ")");
        }

        @Override
        public Set<Long> findCurrOnWindowNumIids(User user) {
            return JdpItemModel.onWindowNumIids(user);
        }
    }

    static String USER_JDP_AVAILABLE_KEY = "_is_user_jdp_";

    public static boolean isUserJdpAvailable(User user) {
        if (user == null) {
            return false;
        }
        if (!Rds.enableJdpPush) {
            return false;
        }

        // TODO fetch from cache..
        String key = USER_JDP_AVAILABLE_KEY + user.getId();
        Boolean cachedRes = (Boolean) Cache.get(key);
        if (cachedRes == null) {
            cachedRes = JDPApi.get().isItemNumMatch(user);
//            cachedRes = RawId.hasId(user.getId());
            String expired = null;
            if (user.getId().longValue() % 2L == 0) {
                expired = "2d";
            } else {
                expired = "3d";
            }
            Cache.set(key, cachedRes, expired);
        }
        return cachedRes;
    }

    public static class PrintUserWinwdowStatusJob extends UserDao.UserBatchJob {
        public void doForUser(User user) {
            WindowItemInfo build = new WindowItemInfo(user);
            log.info("[build :]" + build + " for user id :" + user.getId());
        }
    }

    public static class PrintUserJdpAvailableJob extends UserDao.UserBatchJob {
        @Override
        public void doForUser(User user) {
            log.info(" [ print user jdp api] :" + user.getId() + "-->" + isUserJdpAvailable(user));
        }
    }

    public static class UserCurrJdpStatusPrintJob extends UserDao.UserBatchJob {

        @Override
        public void doForUser(User user) {
            JdpItemStatus status = new JdpItemStatus(user);
            StringBuilder sb = status.toStrBuilder();
            log.warn(" jdp status :" + sb.toString());
        }
    }

    public static class WindowUserJdpStatusPrint extends UserDao.UserBatchJob {

        @Override
        public void doForUser(User user) {

            JdpItemStatus status = new JdpItemStatus(user);

            boolean isMatch = false;
            if (user.isShowWindowOn()) {
                isMatch = status.isOnSaleSynced() && status.isRecentDownMatch();
            } else {
                isMatch = status.isOnSaleSynced();
            }

            if (isMatch) {
                log.warn("___ Match :" + user);
            } else {
                log.info(" !!! Not Match user:" + status.toStrBuilder().toString());
            }
        }
    }

    public static class WindowStatusMatchCheckerJob extends Job {
        public void doJob() {
            new WindowStatusMatchChecker().call();
        }
    }

    public static class WindowStatusMatchChecker extends UserDao.UserBatchOper {
        public WindowStatusMatchChecker() {
            super(128);
            this.sleepTime = 50L;
        }

        @Override
        public List<User> findNext() {
            return UserDao.findWindowShowOn(offset, limit);
        }

        @Override
        public void doForEachUser(final User user) {
            ShowWindowExecutor exec = new ShowWindowExecutor(user);
            String msg = exec.doMatch();
            log.warn(" msg :" + msg);
        }
    }

    public static class JdpUserOnSaleFixer extends UserDao.UserBatchJob {
        public JdpUserOnSaleFixer() {
            super();
            this.limit = 512;
        }

        @Override
        public void doForUser(final User user) {
            final Long apiOnSaleItemNum = OriginApiImpl.get().onSaleItemNum(user);
            if (apiOnSaleItemNum == null || apiOnSaleItemNum < 0L) {
                return;
            }

            final Long orginJdpOnSaleNum = JdpApiImpl.get().onSaleItemNum(user);
            if (orginJdpOnSaleNum == null) {
                return;
            }

            if (apiOnSaleItemNum.intValue() == orginJdpOnSaleNum.intValue()) {
                log.warn("___ Match :" + user);
                return;
            }

            log.warn(" !!! Not Match [" + apiOnSaleItemNum + "][" + orginJdpOnSaleNum + "] user:" + user);

            TMConfigs.getShowwindowPool().submit(new Callable<ItemPlay>() {
                @Override
                public ItemPlay call() throws Exception {
                    if (TMUserWorkRecord.recentFails(user)) {
                        log.error("recent jdp fails for user:" + user);
                        return null;
                    }

                    new PropDiagJob(user, true).doJob();
                    final Long newJdpOnSaleNum = JdpApiImpl.get().onSaleItemNum(user);
                    log.error(" old jdp onsale num:[" + orginJdpOnSaleNum + "] -- new jdp onsale num:["
                            + newJdpOnSaleNum + "] with api onsale num:[" + apiOnSaleItemNum + "] for :" + user);
                    return null;
                }

            });

        }
    }

    public static class JdpStatusClearFixJob extends UserDao.UserBatchJob {

        public JdpStatusClearFixJob() {
            super();
        }

        public JdpStatusClearFixJob(boolean doValid) {
            super(doValid);
        }

        @Override
        public void doForUser(User user) {
            if (!UserDao.doValid(user)) {
                return;
            }
            TMConfigs.getShowwindowPool().submit(new JdpUserStatusFixer(user, true));
            CommonUtils.sleepQuietly(300L);
        }
    }

    public static class JdpDelistTimeFixer extends UserDao.UserBatchJob {
        public JdpDelistTimeFixer() {
            super();
        }

        public JdpDelistTimeFixer(boolean doValid) {
            super(doValid);
        }

        @Override
        public void doForUser(User user) {
            long updateNum = ItemDao.addDelistWeekMillis(user);
            log.info(" change delist time num[" + updateNum + "] for user:" + user);
        }

    }

    public static class JdpWindowStatusJob extends Job {
        public void doJob() {
            new JdpWindowStatusCaller().call();
        }
    }

    public static class JdpWindowStatusCaller extends UserDao.UserBatchOper {
        public JdpWindowStatusCaller() {
            super(128);
            this.sleepTime = 50L;
        }

        @Override
        public List<User> findNext() {
            return UserDao.findWindowShowOn(offset, limit);
        }

        @Override
        public void doForEachUser(final User user) {
            WindowItemInfo info = new WindowItemInfo(user);
            log.warn(" update over for usesr:\n" + info);
            if (info.getRemainWindowCount() > 0 && info.getOnSaleCount() > info.getOnShowItemCount()) {
                log.warn(" fuck !!!!!!!!!!!!!!!!!!!!!!!!!!!1");
                WindowsService.addLightWeightInstant(user.getId());
            }

        }
    }

    public static class JdpStatusFastFixerJob extends UserDao.UserBatchJob {
        User user;

        @Override
        public void doForUser(User user) {
            if (!UserDao.doValid(user)) {
                return;
            }
            TMConfigs.getShowwindowPool().submit(new JdpUserStatusFixer(user, false));
            CommonUtils.sleepQuietly(300L);
        }
    }

    public static class JdpUserStatusFixer implements Callable<ItemPlay> {
        User user;

        boolean clearOldData = true;

        public JdpUserStatusFixer(User user, boolean clearOldData) {
            super();
            this.user = user;
            this.clearOldData = clearOldData;
        }

        @Override
        public ItemPlay call() {

            JdpItemStatus status = new JdpItemStatus(user);
            StringBuilder sb = status.toStrBuilder();
            String oldMsg = sb.toString();
            log.warn(" jdp status :" + oldMsg);

            if (!status.isJdpListening()) {
                new JDPApi.JuShiTaAddUserApi(user).call();
            }

            if (TMUserWorkRecord.recentFails(user)) {
                log.error("recent jdp fails for user:" + user);
                return null;
            }

            if (status.isJdpStatusMatch()) {
                return null;
            }

            Set<Long> jdpItemIds = JdpItemModel.allNumIids(user);
            Set<Long> apiNumIids = ItemApi.allNumIids(user);

            Set<Long> jdpExtraItems = new HashSet<Long>();
            Set<Long> apiExtraItems = new HashSet<Long>();

            for (Long jdpId : jdpItemIds) {
                if (apiNumIids.contains(jdpId)) {
                    apiNumIids.remove(jdpId);
                } else {
                    jdpExtraItems.add(jdpId);
                }
            }

            apiExtraItems.addAll(apiNumIids);
            log.info(" jdpExtra items:" + StringUtils.join(jdpExtraItems, ','));
            log.info(" api Extra items:" + StringUtils.join(apiExtraItems, ','));

            if (!CommonUtils.isEmpty(jdpExtraItems) && clearOldData) {
                // There is some local data not matched...
                log.info(" since jdp status not match :" + user);
                Task deleteTask = new JuShiTaDataDeleteApi(user, JuDataType.tb_item).call();
                log.error(" task :" + new Gson().toJson(deleteTask));
                CommonUtils.sleepQuietly(80000L);

                log.warn(" try fix : user: " + user);
                new PropDiagJob(user, true).doJob();

            } else {
                new PropDiagJob(user, true, apiExtraItems).doJob();
            }

            CommonUtils.sleepQuietly(50000L);
            status = new JdpItemStatus(user);
            sb = status.toStrBuilder();
            String newMsg = sb.toString();
            log.error(" after new jdp status :" + sb.toString());
            TMWorkLog.TMWorkWritter.addToWritter(user.getId(), this.getClass().getName(), oldMsg + "\n------\n"
                    + newMsg);

            return null;
        }
    }

    public abstract List<Item> tryItemList(User user, Collection<Long> idsList);

    public static List<Item> multiItemList(User user, Collection<Long> idsList) {
        Set<Long> ids = new HashSet<Long>(idsList);
        List<Item> list = JdpApiImpl.get().tryItemList(user, ids);
        for (Item item : list) {
            ids.remove(item.getNumIid());
        }
        if (!CommonUtils.isEmpty(ids)) {
            List<Item> left = ItemApi.tryItemList(user, ids, true);
            list.addAll(left);
        }
        return list;
    }

    /**
     * Try jdp first, if not, use api instead...
     * @param user
     * @param numIid
     * @return
     */
    public static Item tryFetchSingleItem(User user, Long numIid) {
        /**
         * This acutally, is a static method...
         */
        return singleItem(user, numIid);
    }

    public static TMResult<Item> doCancel(User user, Long numIid) {
        DeleteRecommend api = new ShowWindowApi.DeleteRecommend(user, numIid);
        return api.call();
    }

    public static TMResult<Item> doRecommend(User user, Long numIid) {
        AddRecommend api = new AddRecommend(user, numIid);
        return api.call();
    }

    public static void fixDeletedItem(User user, Long id) {
        if (Rds.enableJdpPush) {
            boolean isDeleted = JdpApiImpl.get().doDeletedItem(user, id);
            if (isDeleted) {
                return;
            }
        }
        OriginApiImpl.get().doDeletedItem(user, id);
    }

}
