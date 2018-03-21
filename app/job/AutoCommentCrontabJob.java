
package job;

import static java.lang.String.format;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import titleDiag.DiagResult;
import utils.DateUtil;
import utils.TaobaoUtil;

import com.ciaosir.client.CommonUtils;

import configs.TMConfigs;
import controllers.APIConfig;
import dao.UserDao;
import dao.UserDao.UserBatchOper;

//@Every("12h")
public class AutoCommentCrontabJob extends Job {

    static final Logger log = LoggerFactory.getLogger(AutoCommentCrontabJob.class);

    public void doJob() {
        String TAG = "CrontabJob";
        Thread.currentThread().setName(TAG);
        log.error("[enable :]" + TMConfigs.Server.jobTimerEnable);
        if (!TMConfigs.Server.jobTimerEnable) {
            return;
        }

        new UserBatchOper(16) {
            public List<User> findNext() {
                return UserDao.findAutoCommentOn(offset, limit);
            }

            @Override
            public void doForEachUser(final User user) {
                log.info("[submit auto comment user:]" + user);
                TMConfigs.getDiagResultPool().submit(new UserCaller(user));
            }
        }.call();
    }

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static class UserCaller implements Callable<DiagResult> {
        User user;

        public UserCaller(User user) {
            super();
            this.user = user;
        }

        @Override
        public DiagResult call() {
            try {
                doWithUser(user);
                log.warn(" update over for usesr:" + user);
                CommonUtils.sleepQuietly(200L);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
            return null;
        }

        long intervalMillis = DateUtil.FOUR_DAYS;

        public void doWithUser(final User user) {

            log.info(format("doWithUser:user".replaceAll(", ", "=%s, ") + "=%s", user));
            // TODO  
            Date end = new Date();
            Date start = new Date(end.getTime() - intervalMillis);

            if (user.isAutoCommentOn() == false) {
                log.error("[AutoCommentCrontabJob]user auto comment = false ! userId: " + user.getId());
                return;
            }

            if (APIConfig.get().getApp() == APIConfig.defender.getApp()) {
                TaobaoUtil.commentOrdersByConf(user, start, end, sdf.format(new Date()));
            } else {
                TaobaoUtil.getOrdersByUser(user, start, end);
            }

        }
    }

}
