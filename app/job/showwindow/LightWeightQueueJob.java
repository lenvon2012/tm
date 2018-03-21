
package job.showwindow;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import job.CommentMessages;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;

import com.ciaosir.client.utils.NumberUtil;

import dao.UserDao;

/**
 * @deprecated Now, use showwindow exec instead...
 * @author zrb
 *
 */
@Every("8s")
//@NoTransaction
public class LightWeightQueueJob extends Job {
    private static final Logger log = LoggerFactory.getLogger(CommentMessages.class);

    public static final String TAG = "WindowsRecommendMessages";

    public static Queue<Long> userIdMsg = new ConcurrentLinkedQueue<Long>();

    public static Set<Long> pollOutSet() {
        Long userId = null;
        Set<Long> ids = new HashSet<Long>();
        while ((userId = userIdMsg.poll()) != null) {
            ids.add(userId);
        }
        return ids;
    }

    public LightWeightQueueJob() {
        super();
    }

    public void WindowsRecommend(Long userId) {
    }

    @Override
    public void doJob() {
//
//        if (!ShowWindowParams.enableLightWeightQueue) {
//            return;
//        }

        Long userId = null;
        Set<Long> ids = pollOutSet();
//        log.info("[do for light weight with ids :]" + ids.size());
        Iterator<Long> it = ids.iterator();
        while (it.hasNext()) {
            userId = it.next();
//            log.warn("[recommend :]" + userId);
            submitUserId(userId);
        }
    }

    public static void submitUserId(Long userId) {
        if (NumberUtil.isNullOrZero(userId)) {
            return;
        }
        final User user = UserDao.findById(userId);
        if (user == null || !user.isShowWindowOn()) {
            return;
        }

        submitUser(user);
    }

    public static void add(Long userid) {
        userIdMsg.add(userid);
    }

    public static void submitUser(final User user) {
        WindowRemoteJob.getPool().submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                long start = System.currentTimeMillis();
//                LightWeightRecommend lightJob = new LightWeightRecommend(user, ShowwindowMustDoItem.findIdsByUser(user
//                        .getId()), ShowwindowExcludeItem.findByUser(user.getId()));
//                lightJob.doJob();
//                CommonUtils.sleepQuietly(1000L);
                new ShowWindowExecutor(user, true).doJob();
                long end = System.currentTimeMillis();
                log.info("[user Id :" + user.getId() + "] took" + (end - start) + " ms");
//                log.info("with  starut job:" + lightJob.getStatusRecord().toString());
                return Boolean.TRUE;
            }

        });
    }
}
