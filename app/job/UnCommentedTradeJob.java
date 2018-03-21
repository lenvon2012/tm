
package job;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import models.oplog.UnCommentedTradeJobLog;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import titleDiag.DiagResult;
import utils.DateUtil;
import utils.TaobaoUtil;
import configs.TMConfigs;
import dao.UserDao;
import dao.UserDao.UserBatchOper;

//@Every("12h")
public class UnCommentedTradeJob extends Job {

    static final Logger log = LoggerFactory.getLogger(UnCommentedTradeJob.class);

//    static PYFutureTaskPool<DiagResult> pool = new PYFutureTaskPool<DiagResult>(1024);
//
//    static {
//        ApplicationStopJob.addShutdownPool(pool);
//    }

    public static String TAG = "UnCommentedTradeJob";

    public static Long userCount = 0L;

    public void doJob() {
        Thread.currentThread().setName(TAG);
        UnCommentedTradeJobLog unLog = new UnCommentedTradeJobLog();
        final String ts = unLog.getTs();
        UnCommentedTradeJob.userCount = 0L;

        new UserBatchOper(16) {
            public List<User> findNext() {
                return UserDao.findAutoCommentOn(offset, limit);
            }

            @Override
            public void doForEachUser(final User user) {
                UnCommentedTradeJob.userCount++;
                TMConfigs.getDiagResultPool().submit(new UserCaller(user, ts));
            }
        }.call();

        unLog.setUserCount(UnCommentedTradeJob.userCount);
        unLog.saveLog();

    }

    public static class UserCaller implements Callable<DiagResult> {
        User user;

        String ts;

        public UserCaller(User user, String ts) {
            super();
            this.user = user;
            this.ts = ts;
        }

        @Override
        public DiagResult call() {
            try {
                doWithUser(user);
//                CommonUtils.sleepQuietly(300L);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
            return null;
        }

        long intervalMillis = DateUtil.THIRTY_DAYS;

        public void doWithUser(final User user) {
            Date end = new Date();
            Date start = new Date(end.getTime() - intervalMillis);

            if (user.isAutoCommentOn() == false) {
                log.error("[AutoComment]user auto comment = false ! userId: " + user.getId());
                return;
            }

            TaobaoUtil.commentOrdersByConf(user, start, end, ts);
            
            /*if (APIConfig.get().getApp() == APIConfig.defender.getApp()) {
                TaobaoUtil.commentOrdersByConf(user, start, end, ts);
            } else {
                TaobaoUtil.checkOrdersByUser(user, start, end, ts);
            }*/
        }
    }

}
