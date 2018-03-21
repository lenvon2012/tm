package job.sync;

import job.sync.rpt.SyncAllRptJob;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.DateUtil;
import controllers.APIConfig;

public class SyncSimbaJob {

    private final static Logger log = LoggerFactory.getLogger(SyncSimbaJob.class);

    private User user;

    public SyncSimbaJob(User user) {
        this.user = user;
    }
    
    
    public void doJob() {
        try {
            if (user == null) {
                return;
            }
            
            String userNick = user.getUserNick();
            
            if (APIConfig.get().isNeedSyncUserSimba(user) == false) {
                return;
                
            }
            
            if (user.isVaild() == false) {
                log.warn("user is not valid to sync simba!!!! for nick: " + userNick + "---------------");
                return;
            }
            
            log.warn("start sync user simba for user: " + userNick + "--------------------");
            
            new SyncAllCampaignsJob(user, userNick, true, false).doJob();
            
            new SyncAllRptJob(user, userNick, DateUtil.formCurrDate(), true, true).doJob();
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }
    
}
