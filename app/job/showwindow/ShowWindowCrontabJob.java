
package job.showwindow;

import java.util.List;
import java.util.concurrent.Callable;

import job.ApplicationStopJob;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import utils.DateUtil;

import com.ciaosir.client.PYFutureTaskPool;

import configs.Subscribe.Version;
import controllers.APIConfig;
import dao.UserDao;
import dao.UserDao.UserBatchOper;

//@Every("24h")
//@On("0 30 0 * * ?")
public class ShowWindowCrontabJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(ShowWindowCrontabJob.class);

    public static final String TAG = "ShowWindowCrontabJob";

    static int totalCount = 0;

    static int currentNum = 0;

    boolean smooth = false;

    public ShowWindowCrontabJob() {
        super();
    }

    public ShowWindowCrontabJob(boolean smooth) {
        super();
        this.smooth = smooth;
    }

    public static String getStatus() {
        return TAG + String.format("[total --- %d  ---- curr%d] task executing!", totalCount, currentNum);
    }

    private static PYFutureTaskPool<Boolean> pool = null;

    static PYFutureTaskPool<Boolean> getWindowPool() {
        if (pool != null) {
            return pool;
        }
        pool = new PYFutureTaskPool<Boolean>(72);
        ApplicationStopJob.addShutdownPool(pool);
        return pool;
    }

    @Override
    public void doJob() {
        totalCount = 0;
        currentNum = 0;

//        if (!Server.jobTimerEnable) {
//            return;
//        }

        if (APIConfig.get().getApp() == APIConfig.defender.getApp()) {
            // defender not do this ..
            return;
        }

        long countWindowShowOn = UserDao.countWindowShowOn();
        long sleepTime = 50L;
        if (smooth) {
            sleepTime = DateUtil.ONE_HOUR_MILLIS * 2 / countWindowShowOn;
            sleepTime = sleepTime * 9 / 10L;
        }

        new UserBatchOper(0, 16, sleepTime) {

            public List<User> findNext() {
                return UserDao.findWindowShowOn(offset, limit);
            }

            @Override
            public void doForEachUser(final User user) {
                this.sleepTime = 1000L;
                log.info("[show window crontab :]" + user);
                totalCount++;
                getWindowPool().submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        if (user.isVaild() && user.isShowWindowOn() && (Version.LL != user.getVersion())) {

                            new ShowWindowExecutor(user).doJob();
                            log.warn(" update over for usesr:" + user);
                            currentNum++;
                        }
                        return null;
                    }
                });

            }
        }.call();

        // 等待线程全部执行结束
    }

}
