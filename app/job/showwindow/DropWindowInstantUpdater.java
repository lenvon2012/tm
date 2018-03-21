
package job.showwindow;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import job.SPWorker;
import models.showwindow.DropWindowTodayCache;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dao.UserDao;

public class DropWindowInstantUpdater extends SPWorker {

    private static final Logger log = LoggerFactory.getLogger(DropWindowInstantUpdater.class);

    Queue<DropWindowTodayCache> cacheQueue = new ConcurrentLinkedQueue<DropWindowTodayCache>();

    public DropWindowInstantUpdater(int hashId) {
        TAG = String.format("DropWindowInstantUpdater[%d]", hashId);
    }

    public void addMsg(DropWindowTodayCache bean) {
        this.cacheQueue.add(bean);
    }

    public String toStatus() {
        return TAG + ":with size:[" + this.cacheQueue.size() + "] is working :" + isWorking();
    }

    @Override
    protected Boolean doWork() {
//        int beanQueueSize = cacheQueue.size();
//        log.error("do for size :" + beanQueueSize);
        DropWindowTodayCache bean = null;
        while ((bean = cacheQueue.poll()) != null) {
            User user = UserDao.findById(bean.getUserId());
            if (user == null || !user.isVaild() || !user.isShowWindowOn()) {
                continue;
            }
            log.info("[do for replace : ]" + bean);
            new WindowReplaceJob(user, bean).call();
        }

        return Boolean.TRUE;
    }

    public int size() {
        return this.cacheQueue.size();
    }

}
