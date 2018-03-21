
package job;

import job.word.HotWordUpdateJob;
import play.Play;
import play.jobs.Job;
import play.jobs.On;
import configs.TMConfigs.Server;
import controllers.APIConfig;
import controllers.APIConfig.Platform;

//@On("0 0 0 * * ?")
/**
 * topkey words update
 * @author lzl
 *
 */
public class MorningFiveClockJob extends Job {

    public void doJob() {
        if (!Server.jobTimerEnable || Play.mode.isDev()) {
            return;
        }

        if (APIConfig.get().getPlatform() != Platform.taobao) {
            return;
        }

        if (APIConfig.get().getApp() != APIConfig.taobiaoti.getApp()) {
            return;
        }

        new HotWordUpdateJob().doJob();
    }

}
