
package job.showwindow;

import java.util.concurrent.Callable;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.NoTransaction;
import play.jobs.Every;
import play.jobs.Job;
import service.WindowsService;

import com.ciaosir.client.PYFutureTaskPool;

import configs.TMConfigs.ShowWindowParams;
import dao.UserDao;

@NoTransaction
@Every("15s")
public class WindowRemoteJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(WindowRemoteJob.class);

    public static final String TAG = "WindowRemoteJob";

    boolean debug = false;

    @Override
    public void doJob() {
//        log.info("[do for remote :]");
//        log.info("[enable remotes:]" + ShowWindowConfig.enableRemoteWindow);

//        log.info("[current active count:]" + activeCount);
        if (!ShowWindowParams.enableRemoteWindow) {
            return;
        }

//        log.info("[enabled....]");

        Long[] userIds = WindowsService.getCurrentReadyIds();
//        log.info("[foudn remote ids :]" + ArrayUtils.toString(userIds));
        for (final Long long1 : userIds) {
            getPool().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    User user = UserDao.findById(long1);
                    if (user == null) {
                        return Boolean.FALSE;
                    }
                    new ShowWindowExecutor(user, true).doJob();

                    return null;
                }
            });
        }
    }

    static PYFutureTaskPool<Boolean> pool = null;

    public static PYFutureTaskPool<Boolean> getPool() {
        if (pool != null) {
            return pool;
        }
        pool = new PYFutureTaskPool<Boolean>(386);
        return pool;
    }

}
