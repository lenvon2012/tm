
package jdp;

import models.oplog.TMWorkLog.TMWorkWritter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.cache.Cache;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import configs.TMConfigs;
import configs.TMConfigs.Rds;

//@Every("3h")
@OnApplicationStart(async = true)
public class JdpListenEnsuer extends Job {

    public static final String TAG = "JdpListenEnsuer";

    public void doJob() {

        if (Play.mode.isDev()) {
            return;
        }
        if (!Rds.enableJdpPush) {
            return;
        }
        if (!TMConfigs.Server.jobTimerEnable) {
            return;
        }

        if (Cache.get(TAG) != null) {
            return;
        }
        Cache.set(TAG, Boolean.TRUE, "23h");
        Logger log = LoggerFactory.getLogger(JdpListenEnsuer.class);
        Thread.currentThread().setName(TAG);
        try {
            JdpRegisterAllUserJob job = new JdpRegisterAllUserJob();
            job.doJob();
            TMWorkWritter.addToWritter(null, TAG, job.toString());
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }
}
