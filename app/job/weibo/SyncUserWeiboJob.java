package job.weibo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import job.weibo.SyncWeiboMsgThread.SyncUserWeiboLog;
import models.user.User;
import models.weibo.SocialAccountPlay;
import models.weibo.WeiboMsgPlay;
import models.weibo.WeiboMsgPlay.WeiboMsgSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.PlayUtil;
import weibo4j.Timeline;
import weibo4j.model.Paging;
import weibo4j.model.Status;
import weibo4j.model.StatusWapper;

import com.ciaosir.client.CommonUtils;

public class SyncUserWeiboJob implements Callable<SyncUserWeiboLog> {

    private static final Logger log = LoggerFactory.getLogger(SyncUserWeiboJob.class);
    
    //为了防止接口调用次数过多，每3秒调一次，如果没到3秒，就睡眠
    private static final long AccountIntervalTime = 3000;
    
    public static final int SyncWeiboNum = 20;
    
    private User user;
    
    private int userIndex;
    
    private int source;
    
    private SyncUserWeiboLog userLog = new SyncUserWeiboLog();
    
    public static class SyncUserWeiboSource {
        public static final int DaySync = 1;//每天的更新
        public static final int ChangeAccount = 2;//换帐号
    }
    
    public SyncUserWeiboJob(User user, int userIndex, int source) {
        this.user = user;
        this.userIndex = userIndex;
        this.source = source;
    }
    
    public SyncUserWeiboLog call() {
        doSyncUserWeibo();
        
        return userLog;
    }
    
    
    private void doSyncUserWeibo() {
        
        if (user == null || user.isVaild() == false) {
            return;
        }
        
        String userNick = user.getUserNick();
        
        List<SocialAccountPlay> accountList = SocialAccountPlay.findByUserId(user.getId());
        if (CommonUtils.isEmpty(accountList)) {
            log.error("user: " + userNick + ", userIndex: " + userIndex + " has not weibo account, so return-----------------");
            return;
        }
        
        for (SocialAccountPlay account : accountList) {
            doForOneAccount(account);
        }
        
    }
    
    
    private void doForOneAccount(SocialAccountPlay account) {
        
        long startTime = System.currentTimeMillis();
        
        if (account == null) {
            return;
        }
        String userNick = user.getUserNick();
        
        if (account.isMainAccount() == false) {
            //log.error("user: " + userNick + ", userIndex: " + userIndex + ", account: " + account.getAccountName() 
            //        + " is not main account now, so return-----------------");
            return;
        }
        
        if (account.isBinding() == false) {
            userLog.addUnBindNum();
            log.error("user: " + userNick + ", userIndex: " + userIndex + ", account: " + account.getAccountName() 
                    + " is not binding now, so return-----------------");
            return;
        }
        
        if (account.isOutOfDate() == true) {
            userLog.addOutDateNum();
            log.error("user: " + userNick + ", userIndex: " + userIndex + ", account: " + account.getAccountName() 
                    + " is out of date, so return-----------------");
            return;
        }
        
        userLog.addValidAccountNum();
        
        List<WeiboMsgPlay> weiboList = null;
        
        if (account.isSinaAccount()) {
            weiboList = fetchSinaWeiboList(account);
        } else {
            log.error("user: " + userNick + "'s accountType is error: " + account.toString() + "----------");
            return;
        }
        
        
        if (CommonUtils.isEmpty(weiboList)) {
            userLog.addNoResultCount();
            //log.warn("user: " + userNick + ", account: " + account.getAccountName() 
            //        + " has no new weibo now-----------------");
            weiboList = new ArrayList<WeiboMsgPlay>();
        }
        
        int newAddWeiboNum = saveWeiboList(account, weiboList);
        userLog.addNewWeiboNum(newAddWeiboNum);
        
        long endTime = System.currentTimeMillis();
        
        long usedTime = endTime - startTime;
        
        long sleepTime = AccountIntervalTime - usedTime;
        sleepTime = sleepTime / 100 * 100;
        
        if (source == SyncUserWeiboSource.ChangeAccount) {
            sleepTime = 0;
        }
        
        if (sleepTime >= 200) {
            PlayUtil.sleepQuietly(sleepTime);
        } else {
            sleepTime = 0;
        }
        
        log.info("end fetch weibo for user: " + userNick + ", userIndex: " + userIndex + ", account: " + account.getAccountName() 
                + ", get " + weiboList.size() + " weibos, new add " + newAddWeiboNum + " weibos, " 
                + userLog.toString() + ", "
                + "used " + usedTime + " ms, and then sleep " + sleepTime 
                + " ms, source: " + source + "-----------------");
    }
    
    
    private int saveWeiboList(SocialAccountPlay account, List<WeiboMsgPlay> weiboList) {
        if (CommonUtils.isEmpty(weiboList)) {
            return 0;
        }
        int newAddWeiboNum = 0;
        int deleteNum = 0;
        
        List<WeiboMsgPlay> existWeiboList = WeiboMsgPlay.findByAccount(user.getId(), 
                account.getAccountId(), account.getAccountType());
        
        Map<String, WeiboMsgPlay> weiboMap = new HashMap<String, WeiboMsgPlay>();
        for (WeiboMsgPlay weibo : existWeiboList) {
            weiboMap.put(weibo.getWeiboId(), weibo);
        }
        
        for (WeiboMsgPlay weibo : weiboList) {
            boolean isExist = weiboMap.containsKey(weibo.getWeiboId());
            if (isExist == true) {
                weiboMap.remove(weibo.getWeiboId());
                continue;
            }
            weibo.jdbcSave();
            newAddWeiboNum++;
        }
        
        for (WeiboMsgPlay weibo : weiboMap.values()) {
            if (weibo == null) {
                continue;
            }
            weibo.rawDelete();
            deleteNum++;
        }
        
        userLog.addDeleteWeiboNum(deleteNum);
        
        return newAddWeiboNum;
    }
    
    
    private List<WeiboMsgPlay> fetchSinaWeiboList(SocialAccountPlay account) {
        
        List<WeiboMsgPlay> weiboList = new ArrayList<WeiboMsgPlay>();
        
        try {
            Timeline timeline = new Timeline();
            timeline.setToken(account.getToken());
            StatusWapper statusWapper = timeline.getUserTimelineByUid(account.getAccountId(), 
                    new Paging(1, SyncWeiboNum), 0, 0);
            
            List<Status> statusList = statusWapper.getStatuses();
            if (CommonUtils.isEmpty(statusList)) {
                return weiboList;
            }
            
            for (Status status : statusList) {
                WeiboMsgPlay weibo = new WeiboMsgPlay(user.getId(), 
                        account.getAccountType(), account.getAccountId(), account.getAccountName(), 
                        status, WeiboMsgSource.FromMainAccount);
                weiboList.add(weibo);
            }
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            userLog.addFailCount();
        }
        
        return weiboList;
        
    }
}
