
package onlinefix;

import models.user.SellerMobile;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import dao.UserDao;

public class FixAllUserMobile extends Job {

    @Override
    public void doJob() {
        final Logger log = LoggerFactory.getLogger(FixAllUserMobile.class);
        new UserDao.UserBatchOper(32) {
            @Override
            public void doForEachUser(User user) {
                log.info("[do for user ]" + user);
                SellerMobile.ensure(user);
            }
        }.call();
    }
}
