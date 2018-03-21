package job.weibo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import models.user.User;
import models.weibo.ClearWeiboMsgLog;
import models.weibo.SocialAccountPlay.SocialAccountType;
import models.weibo.WeiboMsgPlay;
import models.weibo.WeiboMsgPlay.WeiboMsgSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.jobs.Job;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;

import configs.TMConfigs;
import dao.UserDao;

public class ClearWeiboMsgThread extends Job {

    
    private static final Logger log = LoggerFactory.getLogger(ClearWeiboMsgThread.class);
    
    private static boolean checkIsNeedClear(ClearWeiboMsgLog clearLog) {
        if (clearLog == null) {
            return false;
        }
        
        long finishTs = clearLog.getFinishTs();
        if (finishTs <= 0) {
            return true;
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        log.error("clear weibo message lastFinishTs is " + sdf.format(new Date(finishTs)) 
                + ", accountType: " + clearLog.getAccountType() 
                + ", the interval time is too short, so not clear now----------");
        return false;
    }
    
    private static ClearWeiboMsgLog ensureClearWeiboMsgLog(int accountType) {
        ClearWeiboMsgLog clearLog = ClearWeiboMsgLog.findLatestClearLog(accountType);
        
        if (clearLog == null) {
            clearLog = new ClearWeiboMsgLog(accountType);
        } else {
            long finishTs = clearLog.getFinishTs();
            if (finishTs > 0 && System.currentTimeMillis() - finishTs >= 6 * DateUtil.ONE_HOUR) {
                clearLog = new ClearWeiboMsgLog(accountType);
            }
        }
        
        return clearLog;
    }
    
    private static boolean isNowNeedClear() {
        return true;
    }
    
    
    @Override
    public void doJob() {
        
        if (isNowNeedClear() == false) {
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
        
        
        doClearOneAccountType(SocialAccountType.XinLangWeibo);
        
    }
    
    
    
    private static void doClearOneAccountType(int accountType) {
        
        if (isNowNeedClear() == false) {
            return;
        }
        
        ClearWeiboMsgLog clearLog = ensureClearWeiboMsgLog(accountType);
        
        boolean isNeedClear= checkIsNeedClear(clearLog);
        if (isNeedClear == false) {
            return;
        }
        
        long offset = clearLog.getOffset();
        if (offset <= 0) {
            offset = 0;
        }
        
        clearLog.setRestartCount(clearLog.getRestartCount() + 1);
        clearLog.jdbcSave();
        
        while (true) {
            
            if (isNowNeedClear() == false) {
                return;
            }
            
            long startTime = System.currentTimeMillis();
            int limit = 8;
            
            List<User> userList = UserDao.findAllUserList((int) offset, limit); 
            if (CommonUtils.isEmpty(userList)) {
                break;
            }
            
            doForSomeUsers(userList, clearLog, (int) offset);

            offset += limit;
            
            //保存ts
            clearLog.setOffset(offset);
            
            //保存log
            long endTime = System.currentTimeMillis();
            long usedTime = endTime - startTime;
            clearLog.setUsedTime(clearLog.getUsedTime() + usedTime);
            
            
            clearLog.jdbcSave();
            
        }
        
        if (isNowNeedClear() == true) {
            
            long startTime = System.currentTimeMillis();
            
            clearNoUserWeibo(clearLog);
            clearOldFakeWeibo(clearLog);
            
            long endTime = System.currentTimeMillis();
            long usedTime = endTime - startTime;
            clearLog.setUsedTime(clearLog.getUsedTime() + usedTime);
            
            clearLog.setFinishTs(System.currentTimeMillis());
            clearLog.jdbcSave();
        }
    }
    
    
    private static void doForSomeUsers(List<User> userList, ClearWeiboMsgLog clearLog, int userIndex) {
        if (CommonUtils.isEmpty(userList)) {
            return;
        }
        
        int index = 0;
        for (User user : userList) {
            index++;
            ClearUserWeiboJob clearJob = new ClearUserWeiboJob(user, clearLog.getAccountType(), userIndex + index);
        
            ClearUserWeiboLog userLog = clearJob.clearUserWeibo();
            
            clearLog.setDeleteWeiboNum(clearLog.getDeleteWeiboNum() + userLog.getDeleteWeiboNum());
            clearLog.setFailCount(clearLog.getFailCount() + userLog.getFailCount());
            clearLog.setNoResultCount(clearLog.getNoResultCount() + userLog.getNoResultCount());
            clearLog.setNotMainNum(clearLog.getNotMainNum() + userLog.getNotMainNum());
            clearLog.setOldWeiboNum(clearLog.getOldWeiboNum() + userLog.getOldWeiboNum());
            clearLog.setRemainWeiboNum(clearLog.getRemainWeiboNum() + userLog.getRemainWeiboNum());
            clearLog.setUnValidUserNum(clearLog.getUnValidUserNum() + userLog.getUnValidUserNum());
        }
        
    }
    
    //删除没有用户的weibo
    private static void clearNoUserWeibo(ClearWeiboMsgLog clearLog) {
        
        int limit = 1024;
        long startTime = System.currentTimeMillis();
        long totalDeleteNum = 0;
        
        while (true) {
            long deleteNum = WeiboMsgPlay.deleteNoUserWeibos(clearLog.getAccountType(), limit);
            
            if (deleteNum <= 0) {
                break;
            }
            totalDeleteNum += deleteNum;
            clearLog.setDeleteWeiboNum(clearLog.getDeleteWeiboNum() + (int) deleteNum);
        }
        long endTime = System.currentTimeMillis();
        log.info("end clear no user weibo, total delete " + totalDeleteNum 
                + " weibos, used " + (endTime - startTime) + " ms-----------------");
    }
    
    //删除僵尸粉发的微博
    private static void clearOldFakeWeibo(ClearWeiboMsgLog clearLog) {
        
        int limit = 1024;
        long startTime = System.currentTimeMillis();
        long totalDeleteNum = 0;
        
        long offset = 0;
        
        long accountWeiboNum = WeiboMsgPlay.countBySource(clearLog.getAccountType(), WeiboMsgSource.FromMainAccount);
        if (accountWeiboNum <= 5000) {
            offset = 4000;
        } else if (accountWeiboNum <= 10000) {
            offset = 2000;
        } else {
            offset = 0;
        }
        
        while (true) {
            long deleteNum = WeiboMsgPlay.deleteFakeOldWeibos(clearLog.getAccountType(), offset, limit);
            
            if (deleteNum <= 0) {
                break;
            }
            totalDeleteNum += deleteNum;
            clearLog.setDeleteWeiboNum(clearLog.getDeleteWeiboNum() + (int) deleteNum);
            clearLog.setOldFakeWeiboNum(clearLog.getOldFakeWeiboNum() + (int) deleteNum);
        }
        long endTime = System.currentTimeMillis();
        log.info("end clear old fake weibo, total delete " + totalDeleteNum 
                + " weibos, used " + (endTime - startTime) + " ms-----------------");
    }
    
    
    public static class ClearUserWeiboLog {

        private int deleteWeiboNum;
        
        private int remainWeiboNum;
        
        private int oldWeiboNum;
        
        private int notMainNum;//不再是主帐号的微博数
        
        private int unValidUserNum;//用户过期了，not valid user
        
        private int noResultCount;
        
        private int failCount;

        public ClearUserWeiboLog() {
            super();
        }
        
        
        public void addDeleteWeiboNum(int deleteWeiboNum) {
            this.deleteWeiboNum += deleteWeiboNum;
        }

        public void addRemainWeiboNum(int remainWeiboNum) {
            this.remainWeiboNum += remainWeiboNum;
        }

        public void addOldWeiboNum(int oldWeiboNum) {
            this.oldWeiboNum += oldWeiboNum;
        }

        public void addNotMainNum(int notMainNum) {
            this.notMainNum += notMainNum;
        }

        public void addUnValidUserNum(int unValidUserNum) {
            this.unValidUserNum += unValidUserNum;
        }
        
        public void addNoResultCount() {
            this.noResultCount = noResultCount + 1;
        }

        public void addFailCount() {
            this.failCount = failCount + 1;
        }
        
        
        
        
        public int getDeleteWeiboNum() {
            return deleteWeiboNum;
        }

        public int getRemainWeiboNum() {
            return remainWeiboNum;
        }

        public int getOldWeiboNum() {
            return oldWeiboNum;
        }

        public int getNotMainNum() {
            return notMainNum;
        }

        public int getUnValidUserNum() {
            return unValidUserNum;
        }

        public int getNoResultCount() {
            return noResultCount;
        }

        public int getFailCount() {
            return failCount;
        }


        @Override
        public String toString() {
            return "ClearUserWeiboLog [deleteWeiboNum=" + deleteWeiboNum
                    + ", remainWeiboNum=" + remainWeiboNum + ", oldWeiboNum="
                    + oldWeiboNum + ", notMainNum=" + notMainNum
                    + ", unValidUserNum=" + unValidUserNum + ", noResultCount="
                    + noResultCount + ", failCount=" + failCount + "]";
        }
        
        
        
        
    }
    
    
}
