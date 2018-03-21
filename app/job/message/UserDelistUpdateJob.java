
package job.message;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.NoTransaction;
import play.jobs.Every;
import play.jobs.Job;
import actions.delist.DelistUpdateAction;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;

import dao.UserDao;

@Every("5s")
@NoTransaction
public class UserDelistUpdateJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(UserDelistUpdateJob.class);

    private static PYFutureTaskPool<Void> pool = new PYFutureTaskPool<Void>(3);

    private static Queue<Long> userIdQueue = new ConcurrentLinkedQueue<Long>();

    public static void addUser(User user) {
        if (user == null) {
            return;
        }
        Long userId = user.getId();
        if (userId == null || userId <= 0L) {
            return;
        }
        if (userIdQueue.contains(userId)) {
//            log.warn("UserDelistUpdateJob userIdQueue is already contain userId: " + userId 
//                    + ", userNick: " + user.getUserNick() + "---------------");
            return;
        }

        userIdQueue.add(userId);
    }

    @Override
    public void doJob() {

        if (CommonUtils.isEmpty(userIdQueue)) {
            return;
        }

        long startTime = System.currentTimeMillis();

        List<FutureTask<Void>> promises = new ArrayList<FutureTask<Void>>();

        int userCount = 0;
        Long userId = null;
        while ((userId = userIdQueue.poll()) != null) {
            userCount++;
            User user = UserDao.findById(userId);
            if (user == null) {
                log.error("can not find user for userId: " + userId + " in UserDelistUpdateJob------------");
                continue;
            }

            UserDelistUpdateSubmit call = new UserDelistUpdateSubmit(user);

            promises.add(pool.submit(call));
        }

        for (FutureTask<Void> promise : promises) {
            try {
                promise.get();

            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);

            }
        }

        long endTime = System.currentTimeMillis();

        long usedTime = endTime - startTime;

        log.error("end do UserDelistUpdateJob started with queue size = " + userCount
                + ", used " + usedTime + " ms------------------");

    }

    public static class UserDelistUpdateSubmit implements Callable<Void> {

        private User user;

        public UserDelistUpdateSubmit(User user) {
            super();
            this.user = user;
        }

        @Override
        public Void call() throws Exception {
            if (user == null) {
                return null;
            }
            DelistUpdateAction.doUpdateUserDelist(user);
            return null;
        }

    }

}
