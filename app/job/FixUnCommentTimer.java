
package job;

import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import configs.TMConfigs.Server;

@Every("4h")
//@OnApplicationStart(async = true)
public class FixUnCommentTimer extends Job {

    @Override
    public void doJob() {
        if (!Server.jobTimerEnable || Play.mode.isDev()) {
            return;
        }

        new UnCommentedTradeJob().now();
    }
}
