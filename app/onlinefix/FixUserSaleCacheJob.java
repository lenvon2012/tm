
package onlinefix;

import models.user.User;
import play.jobs.Job;
import cache.UserHasTradeItemCache;
import dao.UserDao;

public class FixUserSaleCacheJob extends Job {

    public void doJob() {
        new UserDao.UserBatchOper(32) {
            @Override
            public void doForEachUser(final User user) {
                UserHasTradeItemCache.getByUser(user, 1000);
            }
        }.call();
    }
}
