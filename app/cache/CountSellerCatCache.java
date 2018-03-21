
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
import utils.TaobaoUtil;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.SellerCat;

import dao.item.ItemDao;

public class CountSellerCatCache implements CacheVisitor<Long> , CacheUserClearer{

    private static final Logger log = LoggerFactory.getLogger(CountSellerCatCache.class);

    public static final String TAG = "CountSellerCatCache";

    public static CountSellerCatCache _instance = new CountSellerCatCache();

    public CountSellerCatCache() {
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

    public Map<SellerCat, Integer> getByUser(User user) {
        Map<SellerCat, Integer> scs;
        String key = genKey(user.getId());
        scs = (Map<SellerCat, Integer>) Cache.get(key);
        if (scs != null) {
            return scs;
        }
        List<SellerCat> list = TaobaoUtil.getSellerCatByUserId(user);
        if (list == null) {
            return MapUtils.EMPTY_MAP;
        }

        scs = new HashMap<SellerCat, Integer>();
        for (SellerCat cat : list) {
            if (cat == null) {
                continue;
            }
            Long cid = cat.getCid();
            if (NumberUtil.isNullOrZero(cid)) {
                continue;
            }
            long count = ItemDao.countAllBySellerCat(user.getId(), cid);
            scs.put(cat, (int) count);
        }

        Cache.add(key, scs, expired());
        return scs;
    }
    
    public Map<SellerCat, Integer> getOnSaleByUser(User user) {
        Map<SellerCat, Integer> scs;
        String key = genKey(user.getId());
        scs = (Map<SellerCat, Integer>) Cache.get(key);
        if (scs != null) {
            return scs;
        }
        List<SellerCat> list = TaobaoUtil.getSellerCatByUserId(user);
        if (list == null) {
            return MapUtils.EMPTY_MAP;
        }

        scs = new HashMap<SellerCat, Integer>();
        for (SellerCat cat : list) {
            if (cat == null) {
                continue;
            }
            Long cid = cat.getCid();
            if (NumberUtil.isNullOrZero(cid)) {
                continue;
            }
            long count = ItemDao.countBySellerCat(user.getId(), cid, 1);
            scs.put(cat, (int) count);
        }

        Cache.add(key, scs, expired());
        return scs;
    }

    public String getRawSellerNameDisplay(User user, String itemSellerIds) {
        return null;
    }

    public static CountSellerCatCache get() {
        return _instance;
    }

    public void clear(User user) {
        String key = genKey(user.getId());
        Cache.delete(key);
    }
}
