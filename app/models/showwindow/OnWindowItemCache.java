
package models.showwindow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jdp.ApiJdpAdapter.OriginApiImpl;
import models.user.User;

import org.apache.commons.collections.SetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import bustbapi.OperateItemApi;
import cache.CacheVisitor;
import dao.UserDao;

/**
 * We should care more for the item chang status
 * @author zrb
 *
 */
public class OnWindowItemCache implements CacheVisitor<Long> {

    private static final Logger log = LoggerFactory.getLogger(OnWindowItemCache.class);

    public static final String TAG = "_OnWindowItemCache";

    static OnWindowItemCache _instance = new OnWindowItemCache();

    public static OnWindowItemCache get() {
        return _instance;
    }

    @Override
    public String prefixKey() {
        return TAG;
    }

    @Override
    public String expired() {
        return "1d";
    }

    Map<Long, String> idKeyMap = new HashMap<Long, String>();

    @Override
    public String genKey(Long userId) {
//        String key = idKeyMap.get(userId);
//        if (key != null) {
//            return key;
//        }
//        key = TAG + userId;
//        idKeyMap.put(userId, key);
        String key = TAG + userId;
        return key;
    }

    public Set<Long> refresh(User user) {
        return getIds(user, true);
    }

    public Set<Long> getIds(User user, boolean forceRefresh) {
        if (user == null) {
            return SetUtils.EMPTY_SET;
        }
        String key = genKey(user.getId());
        Set<Long> ids = null;
        if (!forceRefresh) {
            ids = (Set<Long>) Cache.get(key);
            if (ids != null) {
                return new HashSet<Long>(ids);
            }
        }

        ids = OriginApiImpl.get().findCurrOnWindowNumIids(user);
//        log.info("[set cache ids:]" + ids);
        Cache.safeSet(key, ids, getExpired());
        return ids;
    }

    public String getExpired() {
        return "1d";
    }

    public Set<Long> removeItem(User user, Long numIid) {

        if (user == null) {
            return SetUtils.EMPTY_SET;
        }
        if (numIid == null) {
            return SetUtils.EMPTY_SET;
        }
//        log.info(format("removeItem:user, numIid".replaceAll(", ", "=%s, ") + "=%s", user.toIdNick(), numIid));
        if (!user.isShowWindowOn()) {
            return SetUtils.EMPTY_SET;
        }

        String key = genKey(user.getId());
//        log.info("[key :]" + key);
        Set<Long> ids = (Set<Long>) Cache.get(key);
        if (ids == null) {
            ids = OriginApiImpl.get().findCurrOnWindowNumIids(user);
            ids.remove(numIid);
//            log.info("[remove with safe set cache ids:]" + ids);
            Cache.safeSet(key, ids, getExpired());
        } else {
            ids.remove(numIid);
//            log.info("[remove with safe repalce cache ids:]" + ids);
            Cache.safeReplace(key, ids, getExpired());
        }

        return ids;
    }

    public Set<Long> addItem(User user, Long numIid) {

        if (user == null) {
            return SetUtils.EMPTY_SET;
        }
        if (numIid == null) {
            return SetUtils.EMPTY_SET;
        }
//        log.info(format("addItem:user, numIid".replaceAll(", ", "=%s, ") + "=%s", user.toIdNick(), numIid));
        String key = genKey(user.getId());
        Set<Long> ids = (Set<Long>) Cache.get(key);
        if (ids == null) {
            ids = OriginApiImpl.get().findCurrOnWindowNumIids(user);
            ids.add(numIid);
            Cache.safeSet(key, ids, getExpired());
        } else {
            ids = new HashSet<Long>(ids);
            ids.add(numIid);
            Cache.safeReplace(key, ids, getExpired());
        }

        int totalWindowLimit = OperateItemApi.getUserTotalWindowNum(user);
        if (ids.size() > totalWindowLimit) {
            if (user.isTmall()) {

            } else {

            }

//            boolean isOn = OriginApiImpl.get().findCurrOnWindowItems(user).contains(numIid);
//            log.error("why there are more items is recommends???? : + is on ???: [" + isOn + "]");
//            Set<Long> cacheWindowId = getIds(user, false);
//            Set<Long> realWindowId = OriginApiImpl.get().findCurrOnWindowNumIids(user);
//
//            Set<Long> baseCached = new HashSet<Long>(cacheWindowId);
//            baseCached.removeAll(new ArrayList<Long>(realWindowId));
//
//            Set<Long> realIds = new HashSet<Long>(realWindowId);
//            realIds.removeAll(new ArrayList<Long>(cacheWindowId));
//            log.warn(" cached extra ids:" + baseCached + "  ----- real window id:" + realIds);
//
//            try {
//                throw new NullPointerException("why there are more items is recommends + for user:" + user.toIdNick()
//                        + "  for max :" + totalWindowLimit);
//
//            } catch (Exception e) {
//                log.warn(e.getMessage(), e);
//            }
            return refresh(user);
        }
//        log.info("[set cache ids:]" + ids);

        return ids;
    }

    public void printStatus() {

        new UserDao.UserBatchOper(0, 16, 10L) {
            public List<User> findNext() {
                List<User> list = UserDao.findWindowShowOn(offset, limit);
                return list;
            }

            @Override
            public void doForEachUser(User user) {
                OnWindowItemCache.get().getIds(user, true);
            }
        }.call();
    }

    public Set<Long> checkRefresh(User user, Long numIid) {
        if (user == null) {
            return SetUtils.EMPTY_SET;
        }
        if (numIid == null) {
            return SetUtils.EMPTY_SET;
        }
        if (!user.isShowWindowOn()) {
            return SetUtils.EMPTY_SET;
        }

        Set<Long> exist = getIds(user, false);
        if (exist.contains(numIid)) {
            return getIds(user, true);
        } else {
            return exist;
        }

    }

}
