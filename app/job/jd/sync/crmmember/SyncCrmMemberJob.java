package job.jd.sync.crmmember;

import models.jd.JDCrmMemberUpdateTs;
import models.jd.JDUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import utils.DateUtil;

public class SyncCrmMemberJob extends Job {
    
    private static final Logger log = LoggerFactory.getLogger(SyncCrmMemberJob.class);

    private static final int MaxReachableSyncDay = 3;
    
    private long syncStartTime;
    private long syncEndTime;
    
    private JDUser seller;


    public SyncCrmMemberJob(JDUser seller) {
        super();
        this.seller = seller;
    }
    
    private boolean prepare() {
        JDCrmMemberUpdateTs updateTs = JDCrmMemberUpdateTs.findBySellerId(seller.getId());
        
        syncEndTime = DateUtil.formYestadyMillis();
        
        if (updateTs == null) {
            syncStartTime = syncEndTime - MaxReachableSyncDay * DateUtil.DAY_MILLIS;
            return true;
        } else {
            long lastUpdateTime = updateTs.getLastUpdateTime();
            if (lastUpdateTime >= syncEndTime) {
                return false;
            }
            syncStartTime = lastUpdateTime;
            return true;
        }
        
    }
    
    @Override
    public void doJob() {
        try {
            
            boolean isNeedSync = prepare();
            if (isNeedSync == false) {
                return;
            }
            
            boolean isSuccess = doSyncCrmMember();
            if (isSuccess == false) {
                return;
            }
            
            
            updateLastUpdateTs();
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return;
        }
        
        
    }
    
    private boolean doSyncCrmMember() {
        
        return true;
    }
    
    private boolean updateLastUpdateTs() {
        JDCrmMemberUpdateTs updateTs = JDCrmMemberUpdateTs.findBySellerId(seller.getId());
        
        if (updateTs == null) {
            updateTs = new JDCrmMemberUpdateTs();
            updateTs.setSellerId(seller.getId());
            updateTs.setLastUpdateTime(syncEndTime);
        } else {
            updateTs.setLastUpdateTime(syncEndTime);
        }
        
        boolean isSuccess = updateTs.jdbcSave();
        
        return isSuccess;
    }
    
    
}
