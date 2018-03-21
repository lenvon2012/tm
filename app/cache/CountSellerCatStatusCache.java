
package cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import pojo.webpage.top.ItemStatusCount;
import utils.TaobaoUtil;

import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.SellerCat;

import dao.item.ItemDao;

public class CountSellerCatStatusCache implements CacheVisitor<Long>, CacheUserClearer {

    private static final Logger log = LoggerFactory.getLogger(CountSellerCatStatusCache.class);

    public static final String TAG = "CountSellerCatStatusCache_";

    public static CountSellerCatStatusCache _instance = new CountSellerCatStatusCache();

    public CountSellerCatStatusCache() {
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

    public Map<SellerCat, ItemStatusCount> getByUser(User user) {
        Map<SellerCat, ItemStatusCount> scs;
        String key = genKey(user.getId());
        scs = (Map<SellerCat, ItemStatusCount>) Cache.get(key);
        if (scs != null) {
            return scs;
        }
        List<SellerCat> list = TaobaoUtil.getSellerCatByUserId(user);
        if (list == null) {
            return MapUtils.EMPTY_MAP;
        }

        scs = new HashMap<SellerCat, ItemStatusCount>();
        for (SellerCat cat : list) {
            if (cat == null) {
                continue;
            }
            Long cid = cat.getCid();
            if (NumberUtil.isNullOrZero(cid)) {
                continue;
            }
            long totalCount = ItemDao.countAllBySellerCat(user.getId(), cid);
            int onSaleCount = ItemDao.countBySellerCat(user.getId(), cid, ItemPlay.Status.ONSALE);
            int inStockCount = ItemDao.countBySellerCat(user.getId(), cid, ItemPlay.Status.INSTOCK);
            ItemStatusCount statusCount = new ItemStatusCount(cid, (int) totalCount, onSaleCount, inStockCount);
            statusCount.setName(cat.getName());

            scs.put(cat, statusCount);
        }

        Cache.add(key, scs, expired());
        return scs;
    }

    public String getRawSellerNameDisplay(User user, String itemSellerIds) {
        return null;
    }

    public static CountSellerCatStatusCache get() {
        return _instance;
    }

    public void clear(User user) {
        String key = genKey(user.getId());
        Cache.delete(key);
    }

}
