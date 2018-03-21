
package cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.item.ItemCatPlay;
import models.user.User;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.NumberUtil;

import dao.item.ItemDao;

public class CountItemCatCache implements CacheVisitor<Long>, CacheUserClearer {

    private static final Logger log = LoggerFactory.getLogger(CountItemCatCache.class);

    public static final String TAG = "CountItemCatCache";

    public static final String ONSALE_TAG = "CountOnSaleItemCatCache";

    public static CountItemCatCache _instance = new CountItemCatCache();

    public CountItemCatCache() {
    }

    @Override
    public String prefixKey() {
        return TAG;
    }

    @Override
    public String expired() {
        return "4h";
    }

    @Override
    public String genKey(Long t) {
        return TAG + t;
    }

    public String genOnSaleKey(Long t) {
        return ONSALE_TAG + t;
    }

    public static class EasyHashMap extends HashMap<ItemCatPlay, Integer> {
        @SuppressWarnings("unused")
        private static long serialVersionUID = -4883998389774015477L;
    }

    public Map<ItemCatPlay, Integer> getByUser(User user) {
        Map<ItemCatPlay, Integer> scs;
        String key = genKey(user.getId());
        scs = (EasyHashMap) Cache.get(key);
        if (scs != null) {
            return scs;
        }
        Map<Long, Integer> cidCount = ItemDao.cidCount(user);
        Set<Long> cids = cidCount.keySet();
        if (CommonUtils.isEmpty(cids)) {
            return MapUtils.EMPTY_MAP;
        }

        scs = new EasyHashMap();
        List<ItemCatPlay> list = ItemCatPlay.findCats(cids);
        for (ItemCatPlay cat : list) {
            if (cat == null) {
                continue;
            }
            Long cid = cat.getCid();
            if (NumberUtil.isNullOrZero(cid)) {
                continue;
            }

            Integer count = cidCount.get(cat.getCid());
            if (count != null) {
                scs.put(cat, count);
            }

        }

        Cache.add(key, scs, expired());
        return scs;
    }

    public Map<ItemCatPlay, Integer> getOnSaleByUser(User user) {
        Map<ItemCatPlay, Integer> scs;
        String key = genOnSaleKey(user.getId());
        scs = (Map<ItemCatPlay, Integer>) Cache.get(key);
        if (scs != null) {
            return scs;
        }
        Map<Long, Integer> cidCount = ItemDao.cidOnSaleCount(user);
        Set<Long> cids = cidCount.keySet();
        if (CommonUtils.isEmpty(cids)) {
            return MapUtils.EMPTY_MAP;
        }

        scs = new HashMap<ItemCatPlay, Integer>();
        List<ItemCatPlay> list = ItemCatPlay.findCats(cids);
        for (ItemCatPlay cat : list) {
            if (cat == null) {
                continue;
            }
            Long cid = cat.getCid();
            if (NumberUtil.isNullOrZero(cid)) {
                continue;
            }

            Integer count = cidCount.get(cat.getCid());
            if (count != null) {
                scs.put(cat, count);
            }

        }

        Cache.add(key, scs, expired());
        return scs;
    }

    public static CountItemCatCache get() {
        return _instance;
    }

    public void clear(User user) {
        String key = genKey(user.getId());
        Cache.delete(key);
    }
}
