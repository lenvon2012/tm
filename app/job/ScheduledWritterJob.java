
package job;

import java.util.Queue;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.JPA;
import play.db.jpa.JPABase;
import play.jobs.Job;

public abstract class ScheduledWritterJob<T extends JPABase> extends Job<T> {

    private static final Logger log = LoggerFactory.getLogger(ScheduledWritterJob.class);

    public static final String TAG = "ScheduledWritterJob";

    abstract public Queue<T> getQueue();

    protected boolean tryMerge = false;

    protected void initEnvironment() {
    }

    @Override
    public void doJob() {
//        if (PolicyUtil.doNotRunHeavyJob) {
//            return;
//        }

        T t;
        EntityManager manager = JPA.em();
        initEnvironment();

        while ((t = getQueue().poll()) != null) {
            try {
                if (!needToWrite(t)) {
                    continue;
                }
                // log.info("Try to Persistent Entity: " + t);

                if (tryMerge && t._key() != null) {
                    t = doMerge(manager, t);
                }

                doUpdate(t);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    protected T doMerge(EntityManager manager, T t) {
        if (!manager.contains(t)) {
            t = manager.merge(t);
        }
        return t;
    }

    public void doUpdate(T t) {
        t._save();
    }

    public boolean needToWrite(T t) {
        return true;
    }

    public static <T> void addObject(T t) {
        throw new UnsupportedOperationException("Impelemt for each class...");
    }
}
