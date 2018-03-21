
package cache;

import java.util.ArrayList;
import java.util.List;

import models.user.User;

public class UserLoginInfoCache {

    static UserLoginInfoCache _instance = new UserLoginInfoCache();

    public static abstract class UserCacheCaller<T> {
        public abstract T ensureForUser(User user);

        public abstract void clearForUser(User user);
    }

    public static UserLoginInfoCache get() {
        return _instance;
    }

    List<UserCacheCaller> callers = new ArrayList<UserCacheCaller>();

    public void addUserCaller(UserCacheCaller caller) {
        callers.add(caller);
    }

    public void doClearUser(User user) {
        for (UserCacheCaller caller : callers) {
            caller.clearForUser(user);
        }

    }
}
