
package job.apiget;

import java.util.concurrent.Callable;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import titleDiag.DiagResult;
import actions.UserAction;

import com.ciaosir.client.CommonUtils;

import configs.TMConfigs;
import dao.UserDao;

public class UpdateAllUserVersionJob extends Job {

    @Override
    public void doJob() {
        final Logger log = LoggerFactory.getLogger(UpdateAllUserVersionJob.class);
        new UserDao.UserBatchOper(32) {
            @Override
            public void doForEachUser(final User user) {
                TMConfigs.getDiagResultPool().submit(new Callable<DiagResult>() {
                    @Override
                    public DiagResult call() throws Exception {
                        log.info("do for user:" + user);
                        UserAction.updateUser(user);

                        return null;
                    }
                });
                CommonUtils.sleepQuietly(50L);
            }
        }.call();

    }
}
