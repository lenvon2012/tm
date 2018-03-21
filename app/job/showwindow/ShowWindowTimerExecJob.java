
package job.showwindow;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

import job.CommentMessages;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import titleDiag.DiagResult;
import configs.TMConfigs;
import dao.UserDao;
import dao.UserDao.UserBatchOper;

@Every("5h")
//@OnApplicationStart(async = true)
public class ShowWindowTimerExecJob extends Job {
    private static final Logger log = LoggerFactory.getLogger(CommentMessages.class);

    public static final String TAG = "WindowsRecommendMessages";

    @Override
    public void doJob() {
//        if (Play.mode.isDev()) {
//            return;
//        }

        log.error("[do for job timer:]" + TMConfigs.Server.jobTimerEnable);
        if (!TMConfigs.Server.jobTimerEnable) {
            return;
        }

        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if (hour > 0 && hour < 7) {
            return;
        }

        TMConfigs.getDiagResultPool().submit(new Callable<DiagResult>() {
            @Override
            public DiagResult call() throws Exception {
                new ShowWindowCrontabJob(true).doJob();
                return null;
            }
        });

    }

    public static abstract class ShowWindowUserBatcher extends UserBatchOper {

        public ShowWindowUserBatcher() {
            super(32);
            this.sleepTime = 10L;
        }

        public List<User> findNext() {
            List<User> findWindowShowOn = UserDao.findWindowShowOn(offset, limit);
            //            log.info("[find windo]" + findWindowShowOn);
            return findWindowShowOn;
        }
    }

}
