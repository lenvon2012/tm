
package cache;

import message.UserItemTimestampMsg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;

import com.ciaosir.client.utils.DateUtil;

public class ItemDownShelfCache implements CacheVisitor<UserItemTimestampMsg> {

    private static final Logger log = LoggerFactory.getLogger(ItemDownShelfCache.class);

    public static final String TAG = "ItemDownShelfCache";

    public static ItemDownShelfCache _instance = new ItemDownShelfCache();

    public ItemDownShelfCache() {
    }

    @Override
    public String prefixKey() {
        return TAG;
    }

    @Override
    public String expired() {
        return "15min";
    }

    @Override
    public String genKey(UserItemTimestampMsg msg) {
        return TAG + String.valueOf(msg.getNumIid());
    }

    public String genKey(Long numIid) {
        return TAG + String.valueOf(numIid);
    }

    public static void setCache(long userId, long numIid) {
        setCache(new UserItemTimestampMsg(userId, System.currentTimeMillis(), numIid));
    }

    public static void setCache(UserItemTimestampMsg msg) {
        Cache.safeSet(_instance.genKey(msg), msg, _instance.expired());
    }

    public static boolean isRecentDownShelf(Long numIid) {
        UserItemTimestampMsg msg = (UserItemTimestampMsg) Cache.get(_instance.genKey(numIid));
        if (msg == null) {
            return false;
        }

        if (msg != null && (System.currentTimeMillis() - msg.getTs() < DateUtil.TEN_MINUTE_MILLIS)) {
            return true;
        }
        return false;
    }
}
