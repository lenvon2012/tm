
package job;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import job.showwindow.DropWindowInstantUpdater;
import models.showwindow.DropWindowTodayCache;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;

import com.ciaosir.client.CommonUtils;

import configs.TMConfigs;

public abstract class SPWorker implements Callable<Boolean> {

    private static final Logger log = LoggerFactory.getLogger(SPWorker.class);

    boolean isWorking = false;

    protected String TAG = StringUtils.EMPTY;

    public static int DEFAULT_WORK_THREAD_NUM = 64;

    @Override
    public Boolean call() throws Exception {
        Thread.currentThread().setName(this.TAG);
        isWorking = true;
        try {
            return doWork();
        } catch (Exception e) {

        } finally {
            isWorking = false;
        }
        return Boolean.FALSE;
    }

    abstract protected Boolean doWork();

    public boolean isWorking() {
        return isWorking;
    }

    public void setWorking(boolean isWorking) {
        this.isWorking = isWorking;
    }

    abstract public String toStatus();

    public static List<DropWindowInstantUpdater> dropWindowInstantUpdater = new ArrayList<DropWindowInstantUpdater>(
            DEFAULT_WORK_THREAD_NUM);

    public static <T extends SPWorker> void ensureWorking(List<T> list) {

        for (T t : list) {
            if (t.isWorking()) {
                continue;
            }
            TMConfigs.getBooleanPool().submit(t);
        }
    }

    @Every("1s")
    public static class WorkerCallerJob extends Job {
        public void doJob() {
            ensureWorking(dropWindowInstantUpdater);
        }
    }

    public static synchronized void addDropWindow(DropWindowTodayCache cache) {
        if (CommonUtils.isEmpty(dropWindowInstantUpdater)) {
            for (int i = 0; i < DEFAULT_WORK_THREAD_NUM; i++) {
                dropWindowInstantUpdater.add(new DropWindowInstantUpdater(i));
            }
        }

        Long userId = cache.getUserId();
        if (userId == null) {
            return;
        }
        int hashKey = userId.intValue() % DEFAULT_WORK_THREAD_NUM;
        dropWindowInstantUpdater.get(hashKey).addMsg(cache);
    }

}
