/**
 * 
 */
package job.fenxiao;

import java.util.List;
import java.util.concurrent.Callable;

import models.fenxiao.CooperationPlay;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import bustbapi.FenxiaoApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.taobao.api.domain.Cooperation;

import dao.UserDao;

/**
 * @author navins
 * @date: Dec 23, 2013 10:06:38 PM
 */
public class CooperationSyncJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(CooperationSyncJob.class);

    @Override
    public void doJob() throws Exception {

        new UserDao.UserBatchOper(0, 32) {

            public List<User> findNext() {
                return UserDao.findValidListOrderBydFirstLoginTime(offset, limit);
            }

            @Override
            public void doForEachUser(final User user) {
                log.info("[do for user;]" + user);
                List<Cooperation> cooperations = new FenxiaoApi.CooperationGetApi(user).call();
                if (CommonUtils.isEmpty(cooperations)) {
                    return;
                }
                for (Cooperation cooperation : cooperations) {
                    new CooperationPlay(user.getId(), cooperation).jdbcSave();
                }
                if (this.offset > 100) {
                    CommonUtils.sleepQuietly(250L);
                }
            }
        }.call();

    }

    static PYFutureTaskPool<Void> pool = new PYFutureTaskPool<Void>(8);

    public static class UserCooperationCaller implements Callable<Void> {
        User user;

        public UserCooperationCaller(User user) {
            this.user = user;
        }

        @Override
        public Void call() throws Exception {
            List<Cooperation> cooperations = new FenxiaoApi.CooperationGetApi(user).call();
            if (CommonUtils.isEmpty(cooperations)) {
                return null;
            }
            for (Cooperation cooperation : cooperations) {
                new CooperationPlay(user.getId(), cooperation).jdbcSave();
            }
            return null;
        }

    }

    public static void syncUserCooperation(User user) {
        log.info("submit UserCooperationCaller for userId=" + user.getId());
        pool.submit(new UserCooperationCaller(user));
    }

}
