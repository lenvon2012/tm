
package onlinefix;

import java.util.concurrent.Callable;

import job.showwindow.WindowRemoteJob;
import models.user.User;
import models.user.UserTracer;
import play.jobs.Job;
import dao.UserDao.UserBatchOper;

public class FixAllUserTracerJob extends Job {

    @Override
    public void doJob() {

        new UserBatchOper(32) {

            @Override
            public void doForEachUser(final User user) {
                this.sleepTime = 50L;

                WindowRemoteJob.getPool().submit(new Callable<Boolean>() {

                    @Override
                    public Boolean call() throws Exception {

                        if (!user.isVaild()) {
                            return Boolean.FALSE;
                        }
                        UserTracer.ensure(user.getId());
                        return Boolean.TRUE;
                    }

                });

            }
        }.call();
    }

}
