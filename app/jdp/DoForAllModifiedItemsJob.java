
package jdp;

import job.diagjob.PropDiagJob;
import models.user.User;
import play.jobs.Job;

import com.ciaosir.client.CommonUtils;

import dao.UserDao;

public class DoForAllModifiedItemsJob extends Job {

    public void doJob() {

        new UserDao.UserBatchOper(64) {
            @Override
            public void doForEachUser(User user) {
                new PropDiagJob(user).doJob();
                CommonUtils.sleepQuietly(1000L);
            }
        }.call();
    }
}
