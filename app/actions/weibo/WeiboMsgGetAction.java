package actions.weibo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.user.User;
import models.weibo.SocialAccountPlay;
import models.weibo.SocialAccountPlay.SocialAccountFunction;
import models.weibo.WeiboMsgPlay;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;

public class WeiboMsgGetAction {

    private static final Logger log = LoggerFactory.getLogger(WeiboMsgGetAction.class);
    
    private static long PrevUserOffset = 0L;
    
    private static final int AccountPageSize = WeiboMsgAction.PageSize;
    private static final int ResultWeiboSize = WeiboMsgAction.RandNum;
    private static final int LoopCount = WeiboMsgAction.LoopCount;
    
    public static WeiboGetResult queryMostContributeWeiboList(User user, int accountType, long offset) {
        
        SocialAccountPlay slaveAccount = SocialAccountPlay.findByFunction(user.getId(), accountType, 
                SocialAccountFunction.SlaveAccount);
        
        String slaveAccountId = "";
        if (WeiboMsgValidAction.isValidSlaveAccount(slaveAccount)) {
            slaveAccountId = slaveAccount.getAccountId();
        }
        
        List<WeiboMsgPlay> resultWeiboList = new ArrayList<WeiboMsgPlay>();
        
        boolean isUseContribution = true;//是否根据积分排序
        
        long tempPrevUserOffset = PrevUserOffset;
        boolean hasBackToFront = false;//数据库查询返回到了开头
        
        for (int i = 0; i < LoopCount; i++) {
            
            List<SocialAccountPlay> accountList = null;
            
            if (isUseContribution == true) {
                accountList = SocialAccountPlay.findMostContribute(user.getId(), accountType, 
                        slaveAccountId, offset, AccountPageSize); 
            } else {
                accountList = SocialAccountPlay.findBindMainWithOffset(user.getId(), accountType, 
                        slaveAccountId, PrevUserOffset, AccountPageSize);
            }
            
            log.info("do for most contribute weibos index: " + i + ", isUseContribution: " 
                    + isUseContribution + "--------------------");
            
            
            //获取帐号的微博
            long usedAccountNum = addWeibosByAccountIds(accountList, accountType, slaveAccountId, resultWeiboList);
            
            if (isUseContribution == true) {
                offset += usedAccountNum;
            } else {
                PrevUserOffset += usedAccountNum;
            }
            
            if (CommonUtils.isEmpty(accountList) || accountList.size() < AccountPageSize) {
                if (isUseContribution == true) {
                    isUseContribution = false;
                } else {
                    PrevUserOffset = 0L;
                    hasBackToFront = true;
                }
            }
            
            //所有的记录都查询过了
            if (hasBackToFront == true && PrevUserOffset >= tempPrevUserOffset) {
                break;
            }
            
            if (resultWeiboList.size() >= ResultWeiboSize) {
                break;
            }
            
            
        }
        
        
        WeiboGetResult weiboRes = new WeiboGetResult(resultWeiboList, offset);
        
        return weiboRes;
    }
    
    
    private static long addWeibosByAccountIds(List<SocialAccountPlay> accountList, 
            int accountType, String slaveAccountId,
            List<WeiboMsgPlay> resultWeiboList) {
     
        if (CommonUtils.isEmpty(accountList)) {
            return 0;
        }
        Set<Long> userIdSet = new HashSet<Long>();
        Set<String> accountIdSet = new HashSet<String>();
        
        for (SocialAccountPlay account : accountList) {
            userIdSet.add(account.getUserId());
            accountIdSet.add(account.getAccountId());
        }
        
        List<WeiboMsgPlay> weiboList = WeiboMsgPlay.findUnForwardByAccountIds(userIdSet, accountIdSet, 
                accountType, slaveAccountId);
        
        if (CommonUtils.isEmpty(weiboList)) {
            return accountList.size();
        }
        
        Map<Long, List<WeiboMsgPlay>> weiboMap = toWeiboMap(weiboList);
        
        if (CommonUtils.isEmpty(weiboMap)) {
            return accountList.size();
        }
        
        Set<String> existWeiboIdSet = new HashSet<String>();
        for (WeiboMsgPlay weibo : resultWeiboList) {
            existWeiboIdSet.add(weibo.getWeiboId());
        }
        
        
        long accountNum = 0;
        Set<Long> noWeiboUserIdSet = new HashSet<Long>();
        for (SocialAccountPlay account : accountList) {
            accountNum++;
            List<WeiboMsgPlay> tempList = weiboMap.get(account.getUserId());
            if (CommonUtils.isEmpty(tempList)) {
                noWeiboUserIdSet.add(account.getUserId());
                continue;
            }
            for (WeiboMsgPlay weibo : tempList) {
                if (existWeiboIdSet.contains(weibo.getWeiboId())) {
                    continue;
                }
                weibo.setHeadImgUrl(account.getHeadImgUrl());
                resultWeiboList.add(weibo);
                existWeiboIdSet.add(weibo.getWeiboId());
                break;
            }
            
            if (resultWeiboList.size() >= ResultWeiboSize) {
                break;
            }
        }
        
        if (noWeiboUserIdSet.size() > 0) {
            log.warn(noWeiboUserIdSet.size() + " users has no weibo: " 
                    + StringUtils.join(noWeiboUserIdSet, ",") + "--------------------");
        }
        return accountNum;
    }
    
    
    private static Map<Long, List<WeiboMsgPlay>> toWeiboMap(List<WeiboMsgPlay> weiboList) {
        
        Map<Long, List<WeiboMsgPlay>> weiboMap = new HashMap<Long, List<WeiboMsgPlay>>();
        
        if (CommonUtils.isEmpty(weiboList)) {
            return weiboMap;
        }
        for (WeiboMsgPlay weibo : weiboList) {
            Long userId = weibo.getUserId();
            List<WeiboMsgPlay> tempList = weiboMap.get(userId);
            if (CommonUtils.isEmpty(tempList)) {
                tempList = new ArrayList<WeiboMsgPlay>();
            }
            tempList.add(weibo);
            
            weiboMap.put(userId, tempList);
        }
        
        return weiboMap;
        
    }
    
    
    public static class WeiboGetResult {
        private List<WeiboMsgPlay> weiboList;
        
        private long offset;

        public List<WeiboMsgPlay> getWeiboList() {
            return weiboList;
        }

        public void setWeiboList(List<WeiboMsgPlay> weiboList) {
            this.weiboList = weiboList;
        }

        public long getOffset() {
            return offset;
        }

        public void setOffset(long offset) {
            this.offset = offset;
        }

        public WeiboGetResult(List<WeiboMsgPlay> weiboList, long offset) {
            super();
            this.weiboList = weiboList;
            this.offset = offset;
        }
        
        
    }
    
}
