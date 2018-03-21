
package job.showwindow;

import java.util.List;
import java.util.concurrent.Callable;

import jdp.ApiJdpAdapter;
import job.showwindow.ShowWindowInitJob.WindowItemInfo;
import job.writter.OpLogWritter;
import models.item.ItemPlay;
import models.oplog.OpLog.LogType;
import models.showwindow.ShowWindowConfig;
import models.showwindow.ShowwindowMustDoItem;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import result.TMResult;
import cache.CacheVisitor;
import cache.UserHasTradeItemCache;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.Item;

import controllers.APIConfig;

/**
 * Long numIid;....
 * @author zrb
 *
 */
public class CheckNoDownShelfJob implements Callable<Boolean>, CacheVisitor<Long> {

    private static final Logger log = LoggerFactory.getLogger(CheckNoDownShelfJob.class);

    public static final String TAG = "CheckNoDownShelfJob";

    static long SIX_DAY_MILLIS = 6 * DateUtil.DAY_MILLIS;

    public static CheckNoDownShelfJob _instance = new CheckNoDownShelfJob();

    boolean debug = false;

    private CheckNoDownShelfJob() {
    }

    User user;

    public CheckNoDownShelfJob(User user) {
        super();
        this.user = user;
    }

    Item item;

    public CheckNoDownShelfJob(User user, Item item) {
        super();
        this.user = user;
        this.item = item;
    }

    public CheckNoDownShelfJob(User user, Long numIid) {
        super();
        this.user = user;
        this.item = ApiJdpAdapter.get(user).findItem(user, numIid);
    }

    boolean delistInRecen6Days(Item item) {
        if (item.getDelistTime() == null) {
//            log.info("[no delist time for user:]");
            return true;
        }

        long curr = System.currentTimeMillis();
        if (item.getDelistTime().getTime() - curr <= (5 * DateUtil.DAY_MILLIS)) {
            return true;
        }
        return false;
    }

    public static boolean cancel(User user, Long numIid) {
        TMResult<Item> res = ApiJdpAdapter.doCancel(user, numIid);
//        boolean res = api == null || api.call() != null;
//        log.info("deshow :" + item.getNumIid() + " with res:" + res);
        if (res != null && res.isOk()) {
//            onWindowNumIids.remove(item.getNumIid());
            String msg = "取消橱窗推荐..";
            OpLogWritter.addMsg(user.getId(), msg, numIid, LogType.ShowWindow, false);
            return true;
        } else {
            if (res == null) {
                log.error("no res for user;" + user.toIdNick() + " with numIid: " + numIid);
            } else if ("isv.item-recommend-service-error:ITEM_NOT_FOUND".equals(res.getCode())) {
            }
            return false;
        }
    }

    private boolean isInMustId(Item item) {
//        Set<Long> mustIds = ShowwindowMustDoItem.findIdsByUser(user.getId());
//        if (mustIds.contains(item.getNumIid())) {
//            log.info("[this is the thing that must be done...] :" + item.getNumIid());
//            return true;
//        }
//
//        return false;
        return ShowwindowMustDoItem.exist(user.getId(), item.getNumIid());
    }

    public static boolean isTradesRankContains(User user, Long numIid) {
        int priorSaleNum = ShowWindowConfig.findOrCreate(user.getId()).checkPrioSaleNum();
        List<ItemPlay> items = UserHasTradeItemCache.getByUser(user, priorSaleNum);
        //int prior_num = SalesCountSetting.findNum(user.getId());
        //List<ItemPlay> items = UserHasTradeItemCache.getByUser(user, prior_num);
        if (!CommonUtils.isEmpty(items)) {
            for (ItemPlay itemPlay : items) {
                if (itemPlay.getNumIid().longValue() == numIid) {
                    log.info("[this is the trade that must be done...] :" + numIid);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String prefixKey() {
        return TAG;
    }

    @Override
    public String expired() {
        return "24h";
    }

    public static boolean isItemShouldBeThere(Item item) {
        return CheckStatus.IT_SHOULD_BE_THERE.equals(Cache.get(_instance.genKey(item.getNumIid())));
    }

    public static void tagItemShouldBeOnShelf(Item item) {
        Cache.safeSet(_instance.genKey(item.getNumIid()), CheckStatus.IT_SHOULD_BE_THERE, _instance.expired());
    }

    public static boolean isRecentCanceled(Item item) {
        return CheckStatus.ITEM_RECENT_CANCELED_SHELF.equals(Cache.get(_instance.genKey(item.getNumIid())));
    }

    public static boolean isRecentCanceled(Long numIid) {
        return CheckStatus.ITEM_RECENT_CANCELED_SHELF.equals(Cache.get(_instance.genKey(numIid)));
    }

    public static void tagItemRecentCaneled(Item item) {
        tagItemRecentCaneled(item.getNumIid());
    }

    public static void tagItemRecentCaneled(Long numIid) {
        Cache.safeSet(_instance.genKey(numIid), CheckStatus.ITEM_RECENT_CANCELED_SHELF, _instance.expired());
    }

    @Override
    public String genKey(Long numIid) {
        return TAG + numIid;
    }

    public static class CheckStatus {

        static final String IT_SHOULD_BE_THERE = "_be_there";

        static final String ITEM_RECENT_CANCELED_SHELF = "_is_canceled";
    }

    @Override
    public Boolean call() {

        if (user == null || !user.isShowWindowOn()) {
            log.info("[user closed...]" + user);
            return Boolean.FALSE;
        }

        if (item == null) {
            item = NumberUtil.first(ApiJdpAdapter.get(user).OnWindowItemsDelistDesc(user, 300));
        }

        if (debug) {
            log.info("[check not down shelf ]" + user + " for item numiid :" + (item == null ? null : item.getNumIid()));
        }

        if (debug) {
//            log.info(new Gson().toJson(item));
        }

        if (item == null) {
            if (debug) {
                log.info("[no last id for user:]" + user);
            }
            return Boolean.FALSE;
        }

        if (debug) {
            log.info("[last delist :" + item.getNumIid() + " with the date :"
                    + DateUtil.formDateForLog(item.getDelistTime().getTime()) + " for :" + user);
        }

//        if (delistInRecen6Days(item)) {
//            return Boolean.FALSE;
//        }

//        log.info("[more than 6 days for the item :]" + item.getNumIid() + " with time :" + item.getDelistTime());

//        if (isItemShouldBeThere(item) || isRecentCanceled(item)) {
        if (isItemShouldBeThere(item)) {
            if (debug) {
                log.warn("num iids :" + item.getNumIid() + " uid[" + user.getId()
                        + "]has been recently canceled... or it should be there....");
            }
            return Boolean.FALSE;
        }

        /**
         * The last item might be in some better way...
         */
        if (isInMustId(item) || isTradesRankContains(user, item.getNumIid())) {
            if (debug) {
                log.warn("item :" + item.getNumIid() + " for it's in the must or the trade last...");
            }
            tagItemShouldBeOnShelf(item);
            return Boolean.FALSE;
        }

        WindowItemInfo info = new WindowItemInfo(user);
        if (info.getOnSaleCount() <= info.getOnShowItemCount()) {
            tagItemShouldBeOnShelf(item);
            if (debug) {
                log.warn("item :" + item.getNumIid() + " uid[" + user.getId() + "]should be there for no candidate");
            }
            return Boolean.FALSE;
        }

        // 检查该宝贝不在橱窗上，则不用cancel
        if (!item.getHasShowcase() && APIConfig.get().getApp() == APIConfig.taobiaoti.getApp()) {
            if (debug) {
                log.warn("no need to cancel item :" + item.getNumIid() + " not on window... for userId :"
                        + user.getId());
            }
            return Boolean.FALSE;
        }

//        log.warn("start to cancel: :" + item.getNumIid() + " ");
        // Now, try cancel it...
        boolean res = cancel(user, item.getNumIid());

        // after cancel.. than, tag it and immediate recommend one...
        tagItemRecentCaneled(item);
//        WindowsService.addLightWeightCommon(user.getId());

        return res;
    }
}
