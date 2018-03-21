package job;

import controllers.APIConfig;
import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import proxy.NewProxyTools;

/**
 * Created by hao on 16-2-29.
 */
@Every("5s")
public class InitProxyJob extends Job {
    @Override
    public void doJob() throws Exception {
        boolean prod = Play.mode.isProd();
        boolean isTZG = "21255586".equals(APIConfig.get().getApiKey());
        if (prod && isTZG) {
            NewProxyTools.initPools();
        }
    }
}
