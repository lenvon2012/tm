
package cache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import jdp.ApiJdpAdapter;
import models.item.ItemPlay;
import models.showwindow.ShowWindowConfig;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import bustbapi.SellerAPI;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.API.PYSpiderOption;
import com.ciaosir.client.pojo.ItemThumb;
import com.taobao.api.domain.Item;

import configs.TMConfigs;
import configs.TMConfigs.Server;
import dao.item.ItemDao;

public class UserHasTradeItemCache implements CacheVisitor<User> {

    private static final Logger log = LoggerFactory.getLogger(UserHasTradeItemCache.class);

    public static final String TAG = "UserHashTradeItemCache";

    public static UserHasTradeItemCache _instance = new UserHasTradeItemCache();

    static HttpHost[] hosts = new HttpHost[0];

    static int count = 0;

    static {
//        Server.useHost = Play.mode.isProd() || "zrb".equals(Play.id);
        if (Server.enableProxy) {
            List<HttpHost> list = new ArrayList<HttpHost>();
            list.add(new HttpHost("py01", 30002));
            list.add(new HttpHost("py02", 30002));
            list.add(new HttpHost("py03", 30002));
            list.add(new HttpHost("py04", 30002));
            list.add(new HttpHost("bus01", 30002));
            list.add(new HttpHost("bus02", 30002));
            list.add(new HttpHost("bus03", 30002));
            list.add(new HttpHost("bus04", 30002));
            list.add(new HttpHost("comm01", 30002));
            list.add(new HttpHost("subway01", 30002));
            list.add(new HttpHost("subway02", 30002));
            list.add(new HttpHost("subway03", 30002));
            list.add(new HttpHost("subway04", 30002));
            list.add(new HttpHost("ss01", 30002));
            list.add(new HttpHost("ss02", 30002));
            list.add(new HttpHost("op01", 30002));
            list.add(new HttpHost("op02", 30002));
            hosts = list.toArray(hosts);
        }

    }

    public UserHasTradeItemCache() {
    }

    public static List<ItemPlay> getByUserForShowWindow(User user) {
        //List<ItemPlay> items = getByUser(user, ShowWindowExecutor.MUST_RECOMMEND_BY_TRADE_ORDER_NUM, false);
        int prior_num = ShowWindowConfig.findOrCreate(user.getId()).checkPrioSaleNum();
        //log.warn("UserHasTradeItemCache/getByUser for user ["+ user.getUserNick() +"] with limit = [" + prior_num + "]");
        List<ItemPlay> items = getByUser(user, prior_num, false);
        Iterator<ItemPlay> it = items.iterator();
        while (it.hasNext()) {
            ItemPlay item = it.next();
            if (item.getSalesCount() <= 0) {
                it.remove();
            }
        }
        return items;
    }

    public static List<ItemPlay> getByUser(User user, int limit) {
        //log.warn("UserHasTradeItemCache/getByUser for user ["+ user.getUserNick() +"] with limit = [" + limit + "]");
        if (limit == 0) {
            return new ArrayList<ItemPlay>();
        }
        List<ItemPlay> items = getByUser(user, limit, false);
        Iterator<ItemPlay> it = items.iterator();
        while (it.hasNext()) {
            ItemPlay item = it.next();
            if (item.getSalesCount() <= 0) {
                it.remove();
            }
        }
        return items;
    }

    public static List<Long> getNumIidsByUser(User user, int limit) {
        //log.warn("UserHasTradeItemCache/getByUser for user ["+ user.getUserNick() +"] with limit = [" + limit + "]");
        if (limit == 0) {
            return new ArrayList<Long>();
        }
        List<Long> numIids = new ArrayList<Long>();
        List<ItemPlay> items = getByUser(user, limit, false);
        Iterator<ItemPlay> it = items.iterator();
        while (it.hasNext()) {
            ItemPlay item = it.next();
            if (item.getSalesCount() > 0) {
                numIids.add(item.getNumIid());
            }
        }
        return numIids;
    }

    public static List<ItemPlay> getByUser(User user, int limit, boolean mustRefresh) {
//        log.warn("UserHasTradeItemCache/getByUser for user [" + user.getUserNick() + "] with limit = [" + limit + "]");

        if (limit == 0) {
            return new ArrayList<ItemPlay>();
        }
        String cacheKey = _instance.genKey(user);
        List<ItemPlay> list = (List<ItemPlay>) Cache.get(cacheKey);
//        log.info("[cache hint:]" + (list != null));
        if (list != null && list.size() > 0 && !mustRefresh) {
            if (list.size() > limit) {
                list = list.subList(0, limit);
            }
            return list;
        }

//        int retry = 3;
//        while (retry-- > 0) {
//            list = tryGetItems(user, true, false);
//            if (list != null) {
//                break;
//            }
//        }
//        if (list == null) {
//            list = tryGetItems(user, true, true);
//        }
        list = ItemDao.findOnlineByUserWithTradeNUm(user.getId(), 10000);
//        log.info("[list:]" + list);
//        log.info("[sale list:]" + list);

        if (list != null) {
//            log.info("[set cache for ]" + user);
            Cache.set(cacheKey, list, _instance.expired());
        } else {
            log.error(" no item list for user:" + user);
        }

        if (list.size() > limit) {
            list = list.subList(0, limit);
        }

        return list;
    }

    public static synchronized int getNextCount() {
        count++;
        count = count % hosts.length;
        return count;
    }

    private static void buildItemArray(List<ItemThumb> itemArray, List<ItemPlay> items) {
        for (ItemThumb itemThumb : itemArray) {

            ItemPlay item = new ItemPlay();
            item.setNumIid(itemThumb.getId());
            item.setSalesCount(itemThumb.getTradeNum());
            item.setPrice((double) itemThumb.getPrice() / 100d);
            item.setTitle(itemThumb.getFullTitle());
            item.setPicURL(itemThumb.getPicPath());

            items.add(item);
        }
    }

    private static void buildItemInfoConcurrent(final User user, List<ItemThumb> itemArray, List<ItemPlay> items)
            throws InterruptedException, ExecutionException {
        List<FutureTask<ItemPlay>> tasks = new ArrayList<FutureTask<ItemPlay>>();

        for (final ItemThumb itemThumb : itemArray) {
            TMConfigs.getShowwindowPool().submit(new Callable<ItemPlay>() {
                @Override
                public ItemPlay call() throws Exception {
                    Item tbItem = ApiJdpAdapter.get(user).findItem(user, itemThumb.getId());
                    ItemPlay item = new ItemPlay(user.getId(), tbItem);
                    item.setSalesCount(itemThumb.getTradeNum());
                    return item;
                }
            });
        }

        for (FutureTask<ItemPlay> futureTask : tasks) {
            ItemPlay itemPlay = futureTask.get();
            if (itemPlay == null) {
                continue;
            }

            items.add(itemPlay);
        }
    }

    @Override
    public String prefixKey() {
        return TAG;
    }

    @Override
    public String expired() {
        return "48h";
    }

    @Override
    public String genKey(User t) {
        return TAG + t.getId();
    }

    public static void fixCacheForItemRemoved(User user, Long currentId) {
        List<ItemPlay> cacheList = getByUser(user, Integer.MAX_VALUE);
        if (CommonUtils.isEmpty(cacheList)) {
            log.warn(" no curerntids here");
            return;
        }

        boolean contains = false;
        for (ItemPlay itemPlay : cacheList) {
            if (itemPlay.getNumIid().equals(currentId)) {
                contains = true;
            }
        }
        log.info("[contains :]??" + contains + " with numiids :" + ItemDao.toIdsList(cacheList) + " with current id :"
                + currentId);

        if (!contains) {
            log.warn(" no containes...");
            return;
        }

        List<ItemPlay> newRes = new ArrayList<ItemPlay>();
        for (ItemPlay itemPlay : cacheList) {
            if (itemPlay.getNumIid().equals(currentId)) {
                continue;
            }
            newRes.add(itemPlay);
        }

        String cacheKey = _instance.genKey(user);
        Cache.delete(cacheKey);
        Cache.set(cacheKey, newRes, _instance.expired());
    }

    public static void clear(User user) {
        Cache.safeDelete(_instance.genKey(user));
    }

    @Deprecated
    private static List<ItemPlay> tryGetItems(final User user, boolean ensureItemInfo, boolean directConn) {
        List<ItemThumb> rawArray;
        try {
            HttpHost host = null;
            if (Server.enableProxy && !directConn) {
                host = hosts[getNextCount()];
                log.info("[host : ]" + host);
            }

//            rawArray = SellerAPI.getItemArray(user.getUserNick(), null, 30, null, false);
            rawArray = SellerAPI.getItemArray(user.getUserNick(), null, new PYSpiderOption(null, true, 2, false, 50));

//            Map<Long, ItemThumb> map = ItemThumb.toMap(itemArray);

            if (CommonUtils.isEmpty(rawArray)) {
                return ListUtils.EMPTY_LIST;
            }

            List<ItemThumb> hasSaleThums = new ArrayList<ItemThumb>();

            /*
             * filter for the items having sales greater than 0 
             */
            for (ItemThumb itemThumb : rawArray) {
                if (itemThumb.getTradeNum() > 0) {
                    hasSaleThums.add(itemThumb);
                }
            }

            List<ItemPlay> items = new ArrayList<ItemPlay>();
            if (ensureItemInfo) {
                buildItemInfoConcurrent(user, hasSaleThums, items);
            } else {
                buildItemArray(hasSaleThums, items);
            }

            return items;
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }

    public static void removeForChange(User user, Long numIid) {
        List<ItemPlay> list = UserHasTradeItemCache.getByUser(user, 100);
        if (CommonUtils.isEmpty(list)) {
            return;
        }
        boolean reset = false;
        Iterator<ItemPlay> it = list.iterator();
        while (it.hasNext()) {
            ItemPlay item = it.next();
            if (item == null) {
                it.remove();
                reset = true;
                continue;
            }
            if (item.getNumIid().longValue() == numIid.longValue()) {
                it.remove();
                reset = true;
                break;
            }
        }

        if (reset) {
            String cacheKey = _instance.genKey(user);
            Cache.set(cacheKey, list, _instance.expired());
        }
    }
}
