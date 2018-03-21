
package cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.item.ItemCatPlay;
import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import pojo.webpage.top.ItemStatusCount;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.NumberUtil;

import dao.item.ItemDao;

public class CountItemCatStatusCache implements CacheVisitor<Long>, CacheUserClearer {

    private static final Logger log = LoggerFactory.getLogger(CountItemCatStatusCache.class);

    public static final String TAG = "CountItemCatCache_";

    public static CountItemCatStatusCache _instance = new CountItemCatStatusCache();

    public CountItemCatStatusCache() {
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

    public Map<ItemCatPlay, ItemStatusCount> getByUser(User user) {
        Map<ItemCatPlay, ItemStatusCount> scs;
        String key = genKey(user.getId());
        scs = (Map<ItemCatPlay, ItemStatusCount>) Cache.get(key);
        if (scs != null) {
            return scs;
        }
        Map<Long, Integer> cidTotalCount = ItemDao.cidCount(user);
        Map<Long, Integer> cidOnSaleCount = ItemDao.cidStatusCount(user, ItemPlay.Status.ONSALE);
        Map<Long, Integer> cidInStockCount = ItemDao.cidStatusCount(user, ItemPlay.Status.INSTOCK);

        Set<Long> cids = cidTotalCount.keySet();
        if (CommonUtils.isEmpty(cids)) {
            return MapUtils.EMPTY_MAP;
        }
        scs = new HashMap<ItemCatPlay, ItemStatusCount>();
        List<ItemCatPlay> list = ItemCatPlay.findCats(cids);
        for (ItemCatPlay cat : list) {
            if (cat == null) {
                continue;
            }
            Long cid = cat.getCid();
            if (NumberUtil.isNullOrZero(cid)) {
                continue;
            }

            ItemStatusCount statusCount = new ItemStatusCount();
            Integer count = null;
            
            statusCount.setId(cid);
            statusCount.setName(cat.getName());

            count = cidTotalCount.get(cid);
            if (count != null) {
                statusCount.setTotalCount(count);
            }
            count = cidOnSaleCount.get(cid);
            if (count != null) {
                statusCount.setOnsaleCount(count);
            }

            count = cidInStockCount.get(cid);
            if (count != null) {
                statusCount.setInstockCount(count);
            }


            scs.put(cat, statusCount);
        }

        Cache.add(key, scs, expired());
        return scs;
    }

    public static CountItemCatStatusCache get() {
        return _instance;
    }

    public void clear(User user) {
        String key = genKey(user.getId());
        Cache.delete(key);
    }
}
