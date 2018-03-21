package job.weibo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.FutureTask;

import job.weibo.SyncUserWeiboJob.SyncUserWeiboSource;
import models.user.User;
import models.weibo.SyncWeiboMsgLog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.jobs.Every;
import play.jobs.Job;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.utils.DateUtil;

import configs.TMConfigs;
import dao.UserDao;

@Every("30min")
public class SyncWeiboMsgThread extends Job {

    private static final Logger log = LoggerFactory.getLogger(SyncWeiboMsgThread.class);
    
    private static PYFutureTaskPool<SyncUserWeiboLog> pool = new PYFutureTaskPool<SyncUserWeiboLog>(1);
   
    private static boolean checkIsNeedScan(SyncWeiboMsgLog syncLog) {
        if (syncLog == null) {
            return false;
        }
        
        long finishTs = syncLog.getFinishTs();
        if (finishTs <= 0) {
            return true;
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        log.error("sync weibo message lastFinishTs is " + sdf.format(new Date(finishTs)) 
                + ", the interval time is too short, so not sync now----------");
        return false;
    }
    
    private static SyncWeiboMsgLog ensureSyncWeiboMsgLog() {
        SyncWeiboMsgLog syncLog = SyncWeiboMsgLog.findLatestSyncLog();
        
        if (syncLog == null) {
            syncLog = new SyncWeiboMsgLog();
        } else {
            long finishTs = syncLog.getFinishTs();
            if (finishTs > 0 && System.currentTimeMillis() - finishTs >= DateUtil.ONE_HOUR) {
                syncLog = new SyncWeiboMsgLog();
            }
        }
        
        return syncLog;
    }
    
    private static boolean isNowNeedSync() {
        return true;
    }
    
    private static int getCurrentHour() {
        Calendar now = Calendar.getInstance();
        
        int hour = now.get(Calendar.HOUR_OF_DAY);
        
        return hour;
        
    }
    
    @Override
    public void doJob() {
        
        if (isNowNeedSync() == false) {
            return;
        }
        
        if (!TMConfigs.Server.jobTimerEnable) {
            if ("ywj".equals(Play.id) == false) {
                return;
            }
            
        }
        
        if (TMConfigs.Is_Sync_Weibo == false) {
            return;
        }
        
        
        SyncWeiboMsgLog syncLog = ensureSyncWeiboMsgLog();
        
        boolean isNeedSync = checkIsNeedScan(syncLog);
        if (isNeedSync == false) {
            return;
        }
        
        long offset = syncLog.getOffset();
        if (offset <= 0) {
            offset = 0;
        }
        
        syncLog.setRestartCount(syncLog.getRestartCount() + 1);
        syncLog.jdbcSave();
        
        
        while (true) {
            
            if (isNowNeedSync() == false) {
                return;
            }
            
            long startTime = System.currentTimeMillis();
            int limit = 8;
            
            List<User> userList = UserDao.findValidList((int) offset, limit);
            if (CommonUtils.isEmpty(userList)) {
                break;
            }
            
            doForSomeUsers(userList, syncLog, (int) offset);

            offset += limit;
            
            //保存ts
            syncLog.setOffset(offset);
            
            //保存log
            long endTime = System.currentTimeMillis();
            long usedTime = endTime - startTime;
            syncLog.setUsedTime(syncLog.getUsedTime() + usedTime);
            
            
            syncLog.jdbcSave();
            
        }
        
        if (isNowNeedSync() == true) {
            //syncLog.setOffset(0);
            syncLog.setFinishTs(System.currentTimeMillis());
            syncLog.jdbcSave();
        }
    }
    
    
    private static void doForSomeUsers(List<User> userList, SyncWeiboMsgLog syncLog, int userIndex) {
        if (CommonUtils.isEmpty(userList)) {
            return;
        }
        
        List<FutureTask<SyncUserWeiboLog>> promises = new ArrayList<FutureTask<SyncUserWeiboLog>>();
        
        int index = 0;
        for (User user : userList) {
            index++;
            if (user == null || user.isVaild() == false) {
                continue;
            }
            promises.add(pool.submit(new SyncUserWeiboJob(user, userIndex + index, 
                    SyncUserWeiboSource.DaySync)));
        }
        
        for (FutureTask<SyncUserWeiboLog> promise : promises) {
            try {
                SyncUserWeiboLog userLog = promise.get();
                
                syncLog.setNewWeiboNum(syncLog.getNewWeiboNum() + userLog.getNewWeiboNum());
                syncLog.setValidAccountNum(syncLog.getValidAccountNum() + userLog.getValidAccountNum());
                syncLog.setUnBindNum(syncLog.getUnBindNum() + userLog.getUnBindNum());
                syncLog.setOutDateNum(syncLog.getOutDateNum() + userLog.getOutDateNum());
                syncLog.setNoResultCount(syncLog.getNoResultCount() + userLog.getNoResultCount());
                syncLog.setFailCount(syncLog.getFailCount() + userLog.getFailCount());
                
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                
            }
        }
    }
     
    
    public static class SyncUserWeiboLog {

        private int newWeiboNum;
        
        private int deleteWeiboNum;
        
        private int validAccountNum;
        
        private int unBindNum;
        
        private int outDateNum;
        
        private int noResultCount;
        
        private int failCount;

        public SyncUserWeiboLog() {
            super();
        }
        
        public void addValidAccountNum() {
            this.validAccountNum = validAccountNum + 1;
        }

        public void addNewWeiboNum(int weiboNum) {
            this.newWeiboNum = newWeiboNum + weiboNum;
        }
        
        public void addDeleteWeiboNum(int weiboNum) {
            this.deleteWeiboNum = deleteWeiboNum + weiboNum;
        }
        
        public void addUnBindNum() {
            this.unBindNum = unBindNum + 1;
        }
        
        public void addOutDateNum() {
            this.outDateNum = outDateNum + 1;
        }
        
        public void addNoResultCount() {
            this.noResultCount = noResultCount + 1;
        }

        public void addFailCount() {
            this.failCount = failCount + 1;
        }
        
        

        public int getNewWeiboNum() {
            return newWeiboNum;
        }

        public int getDeleteWeiboNum() {
            return deleteWeiboNum;
        }

        public int getValidAccountNum() {
            return validAccountNum;
        }

        public int getUnBindNum() {
            return unBindNum;
        }

        public int getOutDateNum() {
            return outDateNum;
        }

        public int getNoResultCount() {
            return noResultCount;
        }

        public int getFailCount() {
            return failCount;
        }

        @Override
        public String toString() {
            return "SyncUserWeiboLog [newWeiboNum=" + newWeiboNum
                    + ", deleteWeiboNum=" + deleteWeiboNum
                    + ", validAccountNum=" + validAccountNum + ", unBindNum="
                    + unBindNum + ", outDateNum=" + outDateNum
                    + ", noResultCount=" + noResultCount + ", failCount="
                    + failCount + "]";
        }
        
        
    }
    
    
}
