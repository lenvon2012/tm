
package job.paipai;

import models.paipai.PaiPaiUser;
import models.popularized.Popularized.PopularizedStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.jobs.Job;
import play.jobs.On;
import actions.paipai.PaiPaiAction;
import actions.popularized.PopularizedAction;
import configs.TMConfigs.Server;
import controllers.APIConfig;
import controllers.APIConfig.Platform;
import dao.paipai.PaiPaiUserDao.PaiPaiUserBatchOper;

@On("0 0 0 * * ?")
public class PaiPaiMidNightJob extends Job {
    
    public final static Logger log = LoggerFactory.getLogger(PaiPaiTradeUpdateJob.class);

    public void doJob() {
        if (!Server.jobTimerEnable || Play.mode.isDev()) {
            return;
        }

        if (APIConfig.get().getPlatform() != Platform.paipai) {
            return;
        }
        
        new PaiPaiUserBatchOper(256){

            @Override
            public void doForEachUser(PaiPaiUser user) {
                PaiPaiAction.get().getVersion(user);
                if (user.isValid() == true) {
                    try {
                        PaiPaiAction.get().getVersion(user);
                        if (user.isValid() == false) {
                            PopularizedAction.removeAllPopularized(user.getId(), PopularizedStatus.Try);
                            PopularizedAction.removeAllPopularized(user.getId(), PopularizedStatus.Normal);
                            PopularizedAction.removeAllPopularized(user.getId(), PopularizedStatus.HotSale);
                        }
                        
                        new PaiPaiItemUpdateJob(user.getId()).doJob();
                        
                        if (APIConfig.get().enableSyncTrade(user.getId())) {
                            new PaiPaiTradeUpdateJob(user.getId()).doJob();
                        }
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
                }
            }
            
        }.call();
        
    }

}
