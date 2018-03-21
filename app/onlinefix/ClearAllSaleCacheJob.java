
package onlinefix;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import cache.UserHasTradeItemCache;
import dao.UserDao;

public class ClearAllSaleCacheJob extends Job {

    public void doJob() {

        final Logger log = LoggerFactory.getLogger(ClearAllSaleCacheJob.class);

        new UserDao.UserBatchOper(256) {
            @Override
            public void doForEachUser(User user) {
                log.info("[clear user:]" + user);
                UserHasTradeItemCache.clear(user);
            }
        }.call();
    }
}
