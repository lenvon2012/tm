
package job.showwindow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import service.WindowsService;
import configs.TMConfigs.Server;

@Every("5s")
public class RecentDownCacheJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(RecentDownCacheJob.class);

    public static final String TAG = "RecentDownCacheJob";

    public static boolean recentDownCheck = true;

    public void doJob() {

        if (Play.mode.isDev()) {
            return;
        }

        Thread.currentThread().setName(TAG);
        log.error("do for recent down check");
        if (!Server.jobTimerEnable) {
            log.warn("no job timer ");
            return;
        }

//        if (recentDownCheck) {
//            new CheckForRecentToDropCacheJob().doJob();
        WindowsService.addDropWindowCheck();
//        }
    }
}
