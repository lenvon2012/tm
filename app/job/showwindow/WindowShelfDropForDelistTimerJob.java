
package job.showwindow;

import java.util.concurrent.Callable;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import configs.TMConfigs;
import dao.UserDao;

@Every("360min")
public class WindowShelfDropForDelistTimerJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(WindowShelfDropForDelistTimerJob.class);

    public static final String TAG = "DropShelfForDelistItemJob";

    @Override
    public void doJob() {
        if (!TMConfigs.Server.jobTimerEnable) {
            return;
        }

//        new ShowWindowUserBatcher() {
//
//            @Override
//            public void doForEachUser(final User user) {
//                if (!user.isVaild()) {
//                    return;
//                }
//                submitUserCheckShelfJob(user);
//                CommonUtils.sleepQuietly(10L);
//            }
//
//        }.call();

    }

    public static void submitUserCheckShelfJob(final User user) {
        WindowRemoteJob.getPool().submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                User currUser = UserDao.findById(user.getId());
                if (currUser.isShowWindowOn()) {
                    new CheckNoDownShelfJob(currUser).call();
                }
                return Boolean.TRUE;
            }

        });

    }
}
