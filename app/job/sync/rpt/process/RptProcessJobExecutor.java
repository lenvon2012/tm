package job.sync.rpt.process;

import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;

public abstract class RptProcessJobExecutor<T, W> extends Job {

    private static final Logger log = LoggerFactory.getLogger(RptProcessJobExecutor.class);

    public static final String TAG = "RptProcessJobExecutor";

    abstract public Queue<T> getQueue();

    abstract public Queue<W> getIdQueue();

    @Override
    public void doJob() {
        T t;
        try {
            while ((t = getQueue().poll()) != null) {
//                log.info("Current Queue Size: " + getQueue().size());
                doProcess(t);
                getIdQueue().poll();
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    public abstract void doProcess(T t);

    public static <V> void addObject(V v) {
        throw new UnsupportedOperationException("Impelemt for each class...");
    }

}
