
package cache;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import cache.UserRequestCache.UserReqestUriBean;

public class UserRequestCache implements CacheVisitor<UserReqestUriBean> {

    private static final Logger log = LoggerFactory.getLogger(UserRequestCache.class);

    public static final String TAG = "UserRequestCache";

    public static UserRequestCache _instance = new UserRequestCache();

    public UserRequestCache() {
    }

    public static class UserReqestUriBean implements Serializable {
        private static final long serialVersionUID = -3130529837821333427L;

        String uri;

        Long userId;

        public UserReqestUriBean(String uri, Long userId) {
            super();
            this.uri = uri;
            this.userId = userId;
        }

        public UserReqestUriBean() {
            super();
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

    }

    @Override
    public String prefixKey() {
        return TAG;
    }

    @Override
    public String expired() {
        return "12h";
    }

    @Override
    public String genKey(UserReqestUriBean t) {
        return TAG + t.getUserId() + t.getUri();
    }

    public String genKey(Long userId, String uri) {
        return TAG + userId + uri;
    }

    public static void addCount(Long userId, String uri) {
        String key = _instance.genKey(userId, uri);
        Integer currCount = (Integer) Cache.get(key);
        if (currCount == null) {
            currCount = 1;
        } else {
            currCount++;
        }

        Cache.safeSet(key, currCount, _instance.expired());
    }

    public static int getCount(Long userId, String uri) {
        String key = _instance.genKey(userId, uri);
        Integer currCount = (Integer) Cache.get(key);
        return currCount == null ? 0 : currCount;
    }

}
