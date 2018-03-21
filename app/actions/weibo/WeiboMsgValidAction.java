package actions.weibo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.user.User;
import models.weibo.AccountFriendRecord;
import models.weibo.ForwardMsgRecord;
import models.weibo.SocialAccountPlay;
import models.weibo.WeiboMsgPlay;
import models.weibo.WeiboMsgPlay.WeiboMsgSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;

import dao.UserDao;

public class WeiboMsgValidAction {

    private static final Logger log = LoggerFactory.getLogger(WeiboMsgValidAction.class);
    
    public static List<WeiboMsgPlay> getValidWeiboList(User user, int accountType, 
            SocialAccountPlay slaveAccount, List<WeiboMsgPlay> weiboList) {
        
        if (CommonUtils.isEmpty(weiboList)) {
            return new ArrayList<WeiboMsgPlay>();
        }
        
        
        //自己发的微博不要，不再是主帐号发的微博不要，没绑定的帐号发的微博不要，已关注的帐号不要，已转发的微博不要
        List<WeiboMsgPlay> resultList = new ArrayList<WeiboMsgPlay>();
        
        resultList = filterMineWeibo(user, slaveAccount, weiboList);
        resultList = filterNotBindMainAccounts(accountType, resultList);
        resultList = filterFriendAccounts(accountType, slaveAccount, resultList);
        resultList = filterForwardWeibos(accountType, slaveAccount, resultList);
        resultList = filterNotValidUser(resultList);
        
        return resultList;
    }
    
    //过滤自己发的微博，比如userId相同，accountId与slaveAccountId相同
    private static List<WeiboMsgPlay> filterMineWeibo(User user, SocialAccountPlay slaveAccount, 
            List<WeiboMsgPlay> weiboList) {
        
        if (CommonUtils.isEmpty(weiboList)) {
            return new ArrayList<WeiboMsgPlay>();
        }
        
        List<WeiboMsgPlay> resultList = new ArrayList<WeiboMsgPlay>();
        
        for (WeiboMsgPlay weibo : weiboList) {
            if (weibo == null) {
                continue;
            }
            Long userId = weibo.getUserId();
            if (userId != null && userId.equals(user.getId()) == true) {
                continue;
            }
            
            if (isValidSlaveAccount(slaveAccount)) {
                String accountId = weibo.getAccountId();
                if (StringUtils.isEmpty(accountId) == false && accountId.equals(slaveAccount.getAccountId())) {
                    continue;
                }
            }
            
            resultList.add(weibo);
            
        }
        
        return resultList;
    }
    
    
    //过滤那些不是当前主帐号发的微博，或者是僵尸粉发的微博
    private static List<WeiboMsgPlay> filterNotBindMainAccounts(int accountType, List<WeiboMsgPlay> weiboList) {
        
        if (CommonUtils.isEmpty(weiboList)) {
            return new ArrayList<WeiboMsgPlay>();
        }
        
        Map<String, SocialAccountPlay> mainAccountMap = new HashMap<String, SocialAccountPlay>();
        Set<String> accountIdSet = new HashSet<String>();
        for (WeiboMsgPlay weibo : weiboList) {
            accountIdSet.add(weibo.getAccountId());
        }
        //加上僵尸粉的帐号
        
        
        //再加上当前主帐号
        List<SocialAccountPlay> accountList = SocialAccountPlay.findBindMainAccountIds(accountIdSet, accountType);
        if (CommonUtils.isEmpty(accountList) == false) {
            for (SocialAccountPlay account : accountList) {
                mainAccountMap.put(account.getAccountId(), account);
            }
        }
        
        
        
        List<WeiboMsgPlay> resultList = new ArrayList<WeiboMsgPlay>();
        
        for (WeiboMsgPlay weibo : weiboList) {
            if (weibo == null) {
                continue;
            }
            SocialAccountPlay account = mainAccountMap.get(weibo.getAccountId());
            if (account == null) {
                continue;
            }
            
            weibo.setHeadImgUrl(account.getHeadImgUrl());
            resultList.add(weibo);
        }
        
        return resultList;
    } 
    
    
    //过滤那些已经关注过的帐号
    private static List<WeiboMsgPlay> filterFriendAccounts(int accountType, SocialAccountPlay slaveAccount,
            List<WeiboMsgPlay> weiboList) {
        
        if (CommonUtils.isEmpty(weiboList)) {
            return new ArrayList<WeiboMsgPlay>();
        }
        
        
        Set<String> accountIdSet = new HashSet<String>();
        for (WeiboMsgPlay weibo : weiboList) {
            accountIdSet.add(weibo.getAccountId());
        }
        
        Set<String> friendIdSet = new HashSet<String>();
        if (isValidSlaveAccount(slaveAccount) == true) {
            friendIdSet = AccountFriendRecord.findFriendIds(accountType, 
                    slaveAccount.getAccountId(), accountIdSet);
        }
        
        List<WeiboMsgPlay> resultList = new ArrayList<WeiboMsgPlay>();
        
        for (WeiboMsgPlay weibo : weiboList) {
            if (weibo == null) {
                continue;
            }
            if (friendIdSet.contains(weibo.getAccountId()) == true) {
                continue;
            }
            
            resultList.add(weibo);
        }
        
        return resultList;
    } 
    
    //过滤那些已经转发过的微博
    private static List<WeiboMsgPlay> filterForwardWeibos(int accountType, SocialAccountPlay slaveAccount,
            List<WeiboMsgPlay> weiboList) {
        
        if (CommonUtils.isEmpty(weiboList)) {
            return new ArrayList<WeiboMsgPlay>();
        }
        
        
        Set<String> weiboIdSet = new HashSet<String>();
        for (WeiboMsgPlay weibo : weiboList) {
            weiboIdSet.add(weibo.getWeiboId());
        }
        
        Set<String> forwardWeiboIdSet = new HashSet<String>();
        if (isValidSlaveAccount(slaveAccount) == true) {
            forwardWeiboIdSet = ForwardMsgRecord.findForwardWeiboIds(accountType, 
                    slaveAccount.getAccountId(), weiboIdSet);
        }
        
        List<WeiboMsgPlay> resultList = new ArrayList<WeiboMsgPlay>();
        
        for (WeiboMsgPlay weibo : weiboList) {
            if (weibo == null) {
                continue;
            }
            if (forwardWeiboIdSet.contains(weibo.getWeiboId()) == true) {
                continue;
            }
            
            resultList.add(weibo);
        }
        
        return resultList;
    } 
    
    //过滤not valid的用户
    private static List<WeiboMsgPlay> filterNotValidUser(List<WeiboMsgPlay> weiboList) {
        
        if (CommonUtils.isEmpty(weiboList)) {
            return new ArrayList<WeiboMsgPlay>();
        }
        
        List<WeiboMsgPlay> resultList = new ArrayList<WeiboMsgPlay>();
        
        for (WeiboMsgPlay weibo : weiboList) {
            if (weibo == null) {
                continue;
            }
            if (weibo.getSource() == WeiboMsgSource.FromMainAccount) {
                Long userId = weibo.getUserId();
                if (userId == null || userId <= 0) {
                    
                } else {
                    User user = UserDao.findById(userId);
                    if (user == null || user.isVaild() == false) {
                        continue;
                    }
                }
                
            }
            
            
            resultList.add(weibo);
        }
        
        return resultList;
    }
    
    protected static boolean isValidSlaveAccount(SocialAccountPlay slaveAccount) {
        if (slaveAccount == null) {
            return false;
        }
        if (slaveAccount.isBinding() == false) {
            return false;
        }
        return true;
    }
}
