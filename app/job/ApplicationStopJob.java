
package job;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import play.jobs.Job;
import play.jobs.OnApplicationStop;

import com.ciaosir.client.PYExecPool;
import com.ciaosir.client.PYFutureTaskPool;

@OnApplicationStop
public class ApplicationStopJob extends Job {

    static List<PYExecPool> toShutdownPool = new ArrayList<PYExecPool>();

    static List<PYFutureTaskPool> toShutDownTaskPool = new ArrayList<PYFutureTaskPool>();
    
    static List<ThreadPoolExecutor> toShutdownThreadPool = new ArrayList<ThreadPoolExecutor>();

    public static void addShutdownPool(PYExecPool pool) {
        toShutdownPool.add(pool);
    }

    public static void addShutdownPool(PYFutureTaskPool pool) {
        toShutDownTaskPool.add(pool);
    }
    
    public static void addShutdownPool(ThreadPoolExecutor pool) {
    	toShutdownThreadPool.add(pool);
    }

    @Override
    public void doJob() {
        for (PYExecPool pool : toShutdownPool) {
            pool.shutdown();
        }

        for (PYFutureTaskPool taskPool : toShutDownTaskPool) {
            taskPool.shutdown();
        }
        
        for (ThreadPoolExecutor threadPool : toShutdownThreadPool) {
        	threadPool.shutdown();
        }
    }
}
