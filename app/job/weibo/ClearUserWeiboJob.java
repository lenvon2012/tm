package job.weibo;

import job.weibo.ClearWeiboMsgThread.ClearUserWeiboLog;
import models.user.User;
import models.weibo.SocialAccountPlay;
import models.weibo.SocialAccountPlay.SocialAccountFunction;
import models.weibo.WeiboMsgPlay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.PlayUtil;

public class ClearUserWeiboJob {

    /**
     * 1. 如果用户是not valid，那么删除所有微博
     * 2. 删除不是当前主帐号的微博
     * 3. 删除太久之前的微博
     */
    
    private static final Logger log = LoggerFactory.getLogger(ClearUserWeiboJob.class);
    
    //不能小于SyncUserWeiboJob.SyncWeiboNum，不然删除的微博又要被同步下来了
    private static final int MaxRemainWeiboNum = SyncUserWeiboJob.SyncWeiboNum;
    
    private User user;
    
    private ClearUserWeiboLog userLog = new ClearUserWeiboLog();
    
    private int accountType;
    
    private int userIndex;

    public ClearUserWeiboJob(User user, int accountType, int userIndex) {
        super();
        this.user = user;
        this.accountType = accountType;
        this.userIndex = userIndex;
    }
    
    
    public ClearUserWeiboLog clearUserWeibo() {
        
        long startTime = System.currentTimeMillis();
        
        try {
            doClearUserWeibo();
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            userLog.addFailCount();
        }
        
        long endTime = System.currentTimeMillis();
        
        long usedTime = endTime - startTime;
        
        log.info("end clear user weibo, " + userLog.toString() 
                + ", user: " + user.getUserNick() + ", userIndex: " + userIndex
                + ", used time " + usedTime + " ms-----------");
        
        PlayUtil.sleepQuietly(3000);
        
        return userLog;
    }
    
    
    public void doClearUserWeibo() {
        if (user.isVaild() == false) {
            deleteUnValidUserWeibo();
            return;
        }
        
        deleteNotMainAccountWeibo();
        
        deleteOldWeibo();
        
        long remainWeiboNum = WeiboMsgPlay.countByUserIdAndAccountType(user.getId(), accountType);
        
        userLog.addRemainWeiboNum((int) remainWeiboNum);
    }
    
    //删除过期用户的微博
    private void deleteUnValidUserWeibo() {
        if (user.isVaild() == true) {
            return;
        }
        
        long deleteNum = WeiboMsgPlay.deleteByUserIdAndAccountType(user.getId(), accountType);
        log.warn("delete " + deleteNum + " weibos because of user: " + user.getUserNick() 
                + " is not valid now, userIndex: " + userIndex + "--------");
        
        userLog.addDeleteWeiboNum((int) deleteNum);
        userLog.addUnValidUserNum(1);
        return;
    }
    
    
    
    //删除不是当前主帐号的微博
    private void deleteNotMainAccountWeibo() {
        SocialAccountPlay mainAccount = SocialAccountPlay.findByFunction(user.getId(), accountType, 
                SocialAccountFunction.MainAccount);
        long notMainNum = 0;
        if (mainAccount == null || mainAccount.isBinding() == false) {
            notMainNum = WeiboMsgPlay.deleteByUserIdAndAccountType(user.getId(), accountType);
        } else {
            notMainNum = WeiboMsgPlay.deleteNotAccountWeibo(user.getId(), mainAccount.getAccountId(), accountType);
        }
        
        userLog.addDeleteWeiboNum((int) notMainNum);
        userLog.addNotMainNum((int) notMainNum);
    }
    
    //删除很久之前发的微博
    private void deleteOldWeibo() {
        
        int remainWeiboNum = MaxRemainWeiboNum;
        long oldWeiboNum = WeiboMsgPlay.deleteUserOldWeibo(user.getId(), accountType, remainWeiboNum);
        
        userLog.addDeleteWeiboNum((int) oldWeiboNum);
        userLog.addOldWeiboNum((int) oldWeiboNum);
        
        
    }
}
