
package onlinefix;

import java.util.concurrent.Callable;

import job.showwindow.WindowRemoteJob;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import dao.UserDao;
import dao.UserDao.UserBatchOper;

public class FixAllUserValid extends Job {

    private static final Logger log = LoggerFactory.getLogger(FixAllUserValid.class);

    public static final String TAG = "FixAllUserValid";

    @Override
    public void doJob() {

        new UserBatchOper(256) {

            @Override
            public void doForEachUser(final User user) {
                this.sleepTime = 10L;

                WindowRemoteJob.getPool().submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        log.info("[offset : ]" + offset);
                        return UserDao.doValid(user);
                    }
                });
            }
        }.call();
    }

}
