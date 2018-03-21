
package job.showwindow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;

import job.CommentMessages;
import models.item.ItemPlay;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.NoTransaction;
import play.jobs.Job;
import configs.TMConfigs;
import dao.UserDao;

//@Every("30min")
@NoTransaction
public class WindowsQueueJob extends Job {
    private static final Logger log = LoggerFactory.getLogger(CommentMessages.class);

    public static final String TAG = "WindowsRecommendMessages";

    public static Queue<Long> queueDeleteMsg = new ConcurrentLinkedQueue<Long>();

    public static synchronized Set<Long> pollOutSet() {
        Long userId = null;
        Set<Long> ids = new HashSet<Long>();
        while ((userId = queueDeleteMsg.poll()) != null) {
            ids.add(userId);
        }
        log.info("poll out size:" + ids.size());
        return ids;
    }

    private boolean mustDo = false;

    public WindowsQueueJob() {
        super();
    }

    public WindowsQueueJob(boolean mustDo) {
        super();
        this.mustDo = mustDo;
    }

    public void WindowsRecommend(Long userId) {
    }

    @Override
    public void doJob() {
        /*
         * Let's stop the exec for the remote server for a while...
         */
//        if (!mustDo && !ShowWindowConfig.enableExecPool) {
//            return;
//        }

        Long userId = null;

        Set<Long> ids = pollOutSet();
        List<FutureTask<ItemPlay>> tasks = new ArrayList<FutureTask<ItemPlay>>();
        Iterator<Long> it = ids.iterator();
        while (it.hasNext()) {
            userId = it.next();
            log.warn("[recommend :]" + userId);
            final User user = UserDao.findById(userId);
            if (user == null || !user.isShowWindowOn()) {
                continue;
            }

            FutureTask<ItemPlay> submit = TMConfigs.getShowwindowPool().submit(new Callable<ItemPlay>() {
                @Override
                public ItemPlay call() throws Exception {
                    if (!user.isVaild()) {
                        return null;
                    }
                    if(!user.isShowWindowOn()){
                        return null;
                    }

                    long start = System.currentTimeMillis();
                    // new ShowWindowExecutor(user).doJob();
//                    new LightWeightRecommend(user).doJob();
                    new ShowWindowExecutor(user).doJob();
                    long end = System.currentTimeMillis();
                    log.info("[user Id :" + user.getId() + "] took" + (end - start) + " ms");
                    return null;
                }

            });

            tasks.add(submit);
        }

        log.error("submit recommend queue :" + tasks.size());
        for (FutureTask<ItemPlay> futureTask : tasks) {
            try {
                futureTask.get();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }

    }

    public static void addUserIdOfDeleteMsg(Long userId) {
        User user = UserDao.findById(userId);
        if (user.isShowWindowOn()) {
            queueDeleteMsg.add(userId);
        }

    }
}
