
package job.word;

import java.util.concurrent.Callable;

import job.apiget.ItemUpdateJob;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import titleDiag.DiagResult;

import com.ciaosir.client.CommonUtils;

import configs.TMConfigs;
import dao.UserDao;

public class MainWordJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(MainWordJob.class);

    public static final String TAG = "MainWordJob";

    public void doJob() {

        new UserDao.UserBatchOper(1000, 1024) {
            @Override
            public void doForEachUser(final User user) {
                if (user.getLevel() <= 6) {
                    return;
                }
                TMConfigs.getDiagResultPool().submit(new Callable<DiagResult>() {
                    @Override
                    public DiagResult call() throws Exception {
                        log.info("[fetch user:]" + user);
                        new ItemUpdateJob(user.getId()).call();
                        return null;
                    }
                });
                CommonUtils.sleepQuietly(1000L);
            }
        }.call();
    }
}
