
package onlinefix;

import java.util.Set;

import jdbcexecutorwrapper.JDBCLongSetExecutor;
import models.user.User;
import play.jobs.Job;
import cache.UserHasTradeItemCache;
import dao.UserDao;

public class ClearAllUserCache extends Job {

    public void doJob() {
        Set<Long> ids = new JDBCLongSetExecutor("select id from " + User.TABLE_NAME).call();
        for (Long long1 : ids) {
//            UserDao.clearUserCache(long1);
            User user = UserDao.findById(long1);
            if (user == null) {
                continue;
            }
            UserHasTradeItemCache.clear(user);
        }
    }
}
