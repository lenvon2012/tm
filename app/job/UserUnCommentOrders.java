
package job;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import titleDiag.DiagResult;
import utils.DateUtil;
import bustbapi.TMTradeApi;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.Trade;

import dao.UserDao;
import dao.UserDao.UserBatchOper;

public class UserUnCommentOrders extends Job {
    static final Logger log = LoggerFactory.getLogger(UserUnCommentOrders.class);

    public static String TAG = "UserUnCommentOrders";

    public void doJob() {
        Thread.currentThread().setName(TAG);
        new UserBatchOper(16) {
            public List<User> findNext() {
                return UserDao.findAutoCommentOn(offset, limit);
            }

            @Override
            public void doForEachUser(final User user) {
                UnCommentedTradeJob.userCount++;
//                TMConfigs.getDiagResultPool().submit(new UserUnCommentCaller(user));
                new UserUnCommentCaller(user).call();
            }
        }.call();

    }

    public static class UserUnCommentCaller implements Callable<DiagResult> {
        User user;

        public UserUnCommentCaller(User user) {
            super();
            this.user = user;
        }

        @Override
        public DiagResult call() {
            try {
                doWithUser(user);
                CommonUtils.sleepQuietly(2000L);
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

            List<Trade> trades = new TMTradeApi.TradesSoldUnCommented(user, end.getTime(), start, end).call();
            if (trades == null || trades.isEmpty()) {
                log.info("no uncommented trade founded for user " + user.getUserNick());
                return;
            }

            int size = trades.size();
            log.error(" uncommented trade exist for user:" + user + " with size: [" + size + "]");

//            for (Trade trade : trades) {
//                log.info("oop, uncommented trade existed!!!");
//                // todo
//            }

        }
    }
}
