/**
 * 
 */
package cache;

import java.util.HashMap;

import models.defense.BlackListBuyer;
import models.defense.ItemBuyLimit;
import models.defense.ItemPass;
import models.defense.WhiteListBuyer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;

/**
 * @author navins
 * @date 2013-6-24 下午5:06:03
 */
public class TradeDefenseCache {
    
    private static final Logger log = LoggerFactory.getLogger(TradeDefenseCache.class);

    public static class ItemBuyLimitCache {
        public static final String BUY_LIMIT_KEY = "ItemBuyLimit_";
        
        public static void putIntoCache(Long userId, HashMap<Long, ItemBuyLimit> map) {
            if (userId == null || map == null) {
                return;
            }
            String key = BUY_LIMIT_KEY + userId;
            try {
                Cache.set(key, map, "10min");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        
        public static HashMap<Long, ItemBuyLimit> getItemBuyLimitFromCache(Long userId) {
            if (userId == null) {
                return null;
            }
            String key = BUY_LIMIT_KEY + userId;
            Object cached = Cache.get(key);
            if (cached == null) {
                return null;
            }
            try {
                HashMap<Long, ItemBuyLimit> map = (HashMap<Long, ItemBuyLimit>) cached;
                return map;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            return null;
        }
        
        public static void deleteItemBuyLimitFromCache(Long userId) {
            if (userId == null) {
                return;
            }
            String key = BUY_LIMIT_KEY + userId;
            try {
                Cache.delete(key);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    public static class ItemPassCache {
        public static final String ITEM_PASS_KEY = "ItemPasss_";
        
        public static void putIntoCache(Long userId, HashMap<Long, ItemPass> map) {
            if (userId == null || map == null) {
                return;
            }
            String key = ITEM_PASS_KEY + userId;
            try {
                Cache.set(key, map, "6h");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        
        public static HashMap<Long, ItemPass> getItemPassFromCache(Long userId) {
            if (userId == null) {
                return null;
            }
            String key = ITEM_PASS_KEY + userId;
            Object cached = Cache.get(key);
            if (cached == null) {
                return null;
            }
            try {
                HashMap<Long, ItemPass> map = (HashMap<Long, ItemPass>) cached;
                return map;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            return null;
        }
        
        public static void deleteItemPassFromCache(Long userId) {
            if (userId == null) {
                return;
            }
            String key = ITEM_PASS_KEY + userId;
            try {
                Cache.delete(key);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    public static class BlackListBuyerCache {
        public static final String BLACKLIST_BUYER_KEY = "BlackListBuyers_";
        
        public static void putIntoCache(Long userId, HashMap<String, BlackListBuyer> map) {
            if (userId == null || map == null) {
                return;
            }
            String key = BLACKLIST_BUYER_KEY + userId;
            try {
                Cache.set(key, map, "12h");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        
        public static HashMap<String, BlackListBuyer> getBlackListBuyerFromCache(Long userId) {
            if (userId == null) {
                return null;
            }
            String key = BLACKLIST_BUYER_KEY + userId;
            Object cached = Cache.get(key);
            if (cached == null) {
                return null;
            }
            try {
                HashMap<String, BlackListBuyer> map = (HashMap<String, BlackListBuyer>) cached;
                return map;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            return null;
        }
        
        public static void deleteBlackListBuyerFromCache(Long userId) {
            if (userId == null) {
                return;
            }
            String key = BLACKLIST_BUYER_KEY + userId;
            try {
                Cache.delete(key);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    public static class WhiteListBuyerCache {
        public static final String WHITELIST_BUYER_KEY = "WhiteListBuyers_";
        
        public static void putIntoCache(Long userId, HashMap<String, WhiteListBuyer> map) {
            if (userId == null || map == null) {
                return;
            }
            String key = WHITELIST_BUYER_KEY + userId;
            try {
                Cache.set(key, map, "12h");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        
        public static HashMap<String, WhiteListBuyer> getWhiteListBuyerFromCache(Long userId) {
            if (userId == null) {
                return null;
            }
            String key = WHITELIST_BUYER_KEY + userId;
            Object cached = Cache.get(key);
            if (cached == null) {
                return null;
            }
            try {
                HashMap<String, WhiteListBuyer> map = (HashMap<String, WhiteListBuyer>) cached;
                return map;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            return null;
        }
        
        public static void deleteWhiteListBuyerFromCache(Long userId) {
            if (userId == null) {
                return;
            }
            String key = WHITELIST_BUYER_KEY + userId;
            try {
                Cache.delete(key);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
    
}
