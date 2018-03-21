
package job.showwindow;

import java.util.List;

import models.showwindow.DropWindowTodayCache;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;

import com.ciaosir.client.utils.DateUtil;

import configs.TMConfigs.Server;
import controllers.APIConfig;
import dao.UserDao;
import dao.UserDao.UserBatchOper;

//@Every("6h")
public class DropCacheInitJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(DropCacheInitJob.class);

    public static final String TAG = "DropCacheInitJob";

    static int offsetOutput = 0;

    static long lastExec = 0L;

    static boolean isDoing = false;

    int startOffset = 0;

    public DropCacheInitJob() {
        super();
    }

    public DropCacheInitJob(int startOffset) {
        super();
        this.startOffset = startOffset;
    }

    public static String getStatus() {
        return "[DropCacheInitJob] current offset :" + offsetOutput + " for last exec :"
                + DateUtil.formDateForLog(lastExec) + "  and is working :" + isDoing;
    }

    public void doJob() {
        Thread.currentThread().setName(TAG);

        if (!Server.jobTimerEnable) {
            log.warn("not job timer:");
            return;
        }

        if (APIConfig.get().getApp() == APIConfig.defender.getApp()) {
            // defender not do this ..
            return;
        }
        lastExec = System.currentTimeMillis();
        isDoing = true;

        try {
            new UserBatchOper(startOffset, 128) {
                public List<User> findNext() {
                    log.error("curr offset :" + offset + " with ");
                    return UserDao.findWindowShowOn(offset, limit);
                }

                @Override
                public void doForEachUser(final User user) {
                    offsetOutput = this.offset;
                    try {
                        if (user.isVaild() && user.isShowWindowOn()) {
                            int addNum = DropWindowTodayCache.addCacheForUser(user, 599);
                            log.info("drop window init for user:" + user.getUserNick() + "  for :[" + addNum + "]");
                        }
                    } catch (Exception e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            }.call();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

        isDoing = false;
        log.info("  drop window init over");

    }
}
