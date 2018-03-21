package controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import job.weibo.ClearWeiboMsgThread;
import job.weibo.SyncForNewAccountJob;
import job.weibo.SyncWeiboMsgThread;
import models.user.User;
import models.weibo.SocialAccountPlay;
import models.weibo.SocialAccountPlay.SocialAccountFunction;
import models.weibo.SocialAccountPlay.SocialAccountStatus;
import models.weibo.SocialAccountPlay.SocialAccountType;
import models.weibo.TodayAttentionPlay;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Http.Cookie;
import weibo4j.Oauth;
import weibo4j.Users;
import weibo4j.http.AccessToken;
import actions.weibo.SocialAccountAction;
import actions.weibo.WeiboMsgGetAction;
import actions.weibo.WeiboMsgGetAction.WeiboGetResult;
import actions.weibo.WeiboOperationAction;
import actions.weibo.WeiboOperationAction.WeiboOperationResult;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;

public class AccountAdmin extends TMController {

    private static final Logger log = LoggerFactory.getLogger(AccountAdmin.class);
    
    private static final String AccontFunctionCookie = "_AccontFunctionCookie_";
    
    private static final int ForwardLimitEveryDay = 10;
    
    private static final int FriendLimitEveryDay = 200;
    
    public static void bindError() {
        render("dianputuiguang/binderror.html");
    }
    
    public static void bindSuccess() {
        render("dianputuiguang/bindsuccess.html");
    }
    
    public static void queryAccountsByType(int accountType) {
        User user = getUser();
        
        if (accountType <= 0) {
            renderError("系统出现异常，微博类型出错，请联系我们！");
        }
        
        List<SocialAccountPlay> accountList = SocialAccountPlay.findByAccountType(user.getId(), accountType);
        
        SocialAccountAction.ensureSyncAccountUser(user, accountType, accountList);
        
        renderSuccess("", accountList);
    }
    
    public static void unBindAccount(Long accountId, int accountType) {
        
        if (accountId <= 0) {
            renderError("系统出现异常，帐号ID为空，请联系我们！");
        }
        if (accountType <= 0) {
            renderError("系统出现异常，帐号类型出错，请联系我们！");
        }
        
        User user = getUser();
        
        SocialAccountPlay account = SocialAccountPlay.findByAccountId(user.getId(), accountId);
        
        if (account == null) {
            renderError("系统出现异常，找不到该帐号，请刷新页面重试！");
        }
        
        account.setStatus(SocialAccountStatus.UnBinding);
        
        account.jdbcSave();
        
        /*
        try {
            if (accountType == SocialAccountType.XinLangWeibo) {
                Oauth oauth = new Oauth();
                oauth.setToken(account.getToken());
                JSONObject resultJson = oauth.revokeoauth(account.getToken());
                log.info("revokeoauth: " + resultJson + "--------------");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            renderSuccess("帐号解除绑定成功，但微博取消授权失败", null);
        }
        */
        renderSuccess("帐号解除绑定成功！", null);
    }
    
    private static String getCookieKey(int accountType) {
        return AccontFunctionCookie + accountType + "_";
    }
    
    private static void setAccountFunctionCookie(int accountType, int function) {
        response.setCookie(getCookieKey(accountType), function + "");
    }
    
    private static void clearAccountFunctionCookie(int accountType) {
        response.removeCookie(getCookieKey(accountType));
    }
    
    private static int getAccountFunctionCookie(int accountType) {
        Cookie cookie = request.cookies.get(getCookieKey(accountType));
        if (cookie == null) {
            return 0;
        }
        String functionStr = cookie.value;
        if (StringUtils.isEmpty(functionStr)) {
            return 0;
        }
        
        return NumberUtil.parserInt(functionStr, 0);
    }
    
    public static void getBindAccountUrl(int accountType, int function) {
        if (accountType <= 0) {
            renderError("系统出现异常，帐号类型出错，请联系我们！");
        }
        if (function <= 0) {
            renderError("系统出现异常，帐号状态出错，请联系我们！");
        }
        
        try {
            //设置cookie缓存
            setAccountFunctionCookie(accountType, function);
            
            if (accountType == SocialAccountType.XinLangWeibo) {
                Oauth oauth = new Oauth();
                
                //BareBonesBrowserLaunch.openURL(oauth.authorize("code","",""));
                String bindUrl = oauth.authorize("code","","");
                
                renderSuccess("", bindUrl);
            } else {
                renderError("系统出现异常，帐号类型出错，请联系我们！");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            renderError("帐号授权出错，请联系我们！");
        }
    }
    
    
    public static void bindSinaAccountCallback(String code) {
        
        User user = getUser();
        
        try {
            int accountType = SocialAccountType.XinLangWeibo;
            
            int function = getAccountFunctionCookie(accountType);
            clearAccountFunctionCookie(accountType);
            
            //获取token,uid,expireSecond
            Oauth oauth = new Oauth();
            AccessToken accessToken = oauth.getAccessTokenByCode(code);
            String token = accessToken.getAccessToken();
            String uid = accessToken.getUid();
            long checkAuthTs = System.currentTimeMillis();
            long expireSecond = NumberUtil.parserLong(accessToken.getExpireIn(), 0L);
            
            //获取user
            Users um = new Users();
            um.client.setToken(token);
            weibo4j.model.User weiboUser = um.showUserById(uid);
            
            //log.info("get sina weibo user: " + weiboUser.toString() + "--------------");
            
            int[] functionArray = null;
            
            //主帐号或小号，只绑定一个帐号
            if (function == SocialAccountFunction.MainAccount 
                    || function == SocialAccountFunction.SlaveAccount) {
                functionArray = new int[] {function};
                
            } else {
                //绑定两个帐号
                functionArray = new int[] {SocialAccountFunction.MainAccount, SocialAccountFunction.SlaveAccount};
            }
            
            boolean isSyncWeibo = false;
            for (int tempFunc : functionArray) {
                SocialAccountPlay account = SocialAccountPlay.findByFunction(user.getId(), accountType, tempFunc);
                if (account == null) {
                    account = new SocialAccountPlay(user.getId(), weiboUser.getScreenName(), weiboUser.getScreenName(), 
                            token, uid, accountType, SocialAccountStatus.Binding, tempFunc,
                            checkAuthTs, expireSecond);
                } else {
                    account.updateAccount(weiboUser.getScreenName(), weiboUser.getScreenName(), 
                            token, uid, SocialAccountStatus.Binding,
                            checkAuthTs, expireSecond);
                }
                account.updateSinaAccountBasic(weiboUser);
                
                account.jdbcSave();
                
                log.info("weibo account: " + account.toString());
                
                if (account.isMainAccount()) {
                    isSyncWeibo = true;
                }
                
            }
            
            if (isSyncWeibo == true) {
                SyncForNewAccountJob.addQueue(user);
            }
            
            
            bindSuccess();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            //renderError("绑定帐号出错，请联系我们！");
            bindError();
            
        }
        
    }
    
    
    public static void queryTodayAttention(int accountType) {
        if (accountType <= 0) {
            renderError("系统出现异常，帐号类型出错，请联系我们！");
        }
        User user = getUser();
        
        TodayAttentionPlay attention = TodayAttentionPlay.ensureTodayAttention(user.getId(), accountType);
        
        renderSuccess("", attention);
    }
    
    public static void queryWeiboList(int accountType, long offset) {
        if (accountType <= 0) {
            renderError("系统出现异常，帐号类型出错，请联系我们！");
        }
        User user = getUser();
        
        //List<WeiboMsgPlay> weiboList = WeiboMsgAction.queryRandomWeiboList(user, accountType);
        //List<WeiboMsgPlay> weiboList = WeiboMsgPlay.findByAccountType(accountType, 0, 20);
        
        WeiboGetResult weiboRes = WeiboMsgGetAction.queryMostContributeWeiboList(user, accountType, offset);
        
        renderSuccess("", weiboRes);
    }
    
    private static SocialAccountPlay checkUserAccount(int accountType) {
        if (accountType <= 0) {
            renderError("系统出现异常，帐号类型出错，请联系我们！");
        }
        User user = getUser();
        
        SocialAccountPlay slaveAccount = SocialAccountPlay.findByFunction(user.getId(), accountType, 
                SocialAccountFunction.SlaveAccount);
        if (slaveAccount == null || slaveAccount.isBinding() == false) {
            renderError("请先绑定一个微博小号，然后再转发或关注一条微博！");
        }
        if (slaveAccount.isOutOfDate() == true) {
            renderError("您的微博小号授权已过期，请重新授权，然后再转发或关注一条微博！");
        }
        
        SocialAccountPlay mainAccount = SocialAccountPlay.findByFunction(user.getId(), accountType, 
                SocialAccountFunction.MainAccount);
        
        if (mainAccount == null || mainAccount.isBinding() == false) {
            renderError("请先绑定一个微博大号，不然将无法获取积分！");
        }
        
        return slaveAccount;
    }
    
    private static void checkForwardWeiboPrivate(int accountType) {
        if (accountType <= 0) {
            renderError("系统出现异常，帐号类型出错，请联系我们！");
        }
        User user = getUser();
        TodayAttentionPlay attention = TodayAttentionPlay.ensureTodayAttention(user.getId(), accountType);
        if (attention.getForwardNum() >= ForwardLimitEveryDay) {
            renderError("今天您已经转发了" + attention.getForwardNum() + "条微博，超出了限制，请明天再转发！");
        }
        checkUserAccount(accountType);
        
    }
    
    private static void checkFriendWeiboPrivate(int accountType) {
        if (accountType <= 0) {
            renderError("系统出现异常，帐号类型出错，请联系我们！");
        }
        User user = getUser();
        TodayAttentionPlay attention = TodayAttentionPlay.ensureTodayAttention(user.getId(), accountType);
        if (attention.getAttentionNum() >= FriendLimitEveryDay) {
            renderError("今天您已经关注了" + attention.getAttentionNum() + "个帐号，超出了限制，请明天再关注！");
        }
        checkUserAccount(accountType);
        
    }
    
    public static void checkForwardWeibo(int accountType) {
        checkForwardWeiboPrivate(accountType);
        renderSuccess("", null);
    }
    
    public static void checkFriendWeibo(int accountType) {
        checkFriendWeiboPrivate(accountType);
        renderSuccess("", null);
    }
    
    public static void forwardWeibo(int accountType, String weiboId, 
            Long friendUserId, String friendAccountId, String forwardMsg) {
        if (accountType <= 0) {
            renderError("系统出现异常，帐号类型出错，请联系我们！");
        }
        if (StringUtils.isEmpty(friendAccountId)) {
            renderError("系统出现异常，微博帐号ID为空，请联系我们！");
        }
        if (StringUtils.isEmpty(weiboId)) {
            renderError("系统出现异常，微博ID为空，请联系我们！");
        }
        if (friendUserId == null || friendUserId <= 0L) {
            renderError("系统出现异常，帐号用户ID为空，请联系我们！");
        }
        
        if (StringUtils.isEmpty(forwardMsg) == false) {
            if (forwardMsg.length() > 140) {
                renderError("微博长度不能超过140个字！");
            }
        }
        
        User user = getUser();
        
        checkForwardWeiboPrivate(accountType);
        
        WeiboOperationResult weiboRes = WeiboOperationAction.doForwardWeibo(user, accountType, 
                friendUserId, friendAccountId, weiboId, forwardMsg);
        
        if (weiboRes.isSuccess() == false) {
            renderError(weiboRes.getMessage());
        } else {
            renderSuccess(weiboRes.getMessage(), null);
        }
    }
    
    
    public static void friendWeibos(int accountType, Long friendUserId, String friendAccountIds) {
        if (accountType <= 0) {
            renderError("系统出现异常，帐号类型出错，请联系我们！");
        }
        if (StringUtils.isEmpty(friendAccountIds)) {
            renderError("请先选择要关注的微博帐号！");
        }
        if (friendUserId == null || friendUserId <= 0L) {
            renderError("系统出现异常，帐号用户ID为空，请联系我们！");
        }
        
        String[] friendAccountIdArray = friendAccountIds.split(",");
        if (friendAccountIdArray == null || friendAccountIdArray.length <= 0) {
            renderError("请先选择要关注的微博帐号！");
        }
        
        Set<String> friendAccountIdSet = new HashSet<String>();
        for (String friendAccountId : friendAccountIdArray) {
            if (StringUtils.isEmpty(friendAccountId) == false) {
                friendAccountIdSet.add(friendAccountId);
            }
        }
        if (CommonUtils.isEmpty(friendAccountIdSet)) {
            renderError("请先选择要关注的微博帐号！");
        }
        
        User user = getUser();
        
        checkFriendWeiboPrivate(accountType);
        
        
        WeiboOperationResult weiboRes = WeiboOperationAction.doFriendAccounts(user, accountType, 
                friendUserId, friendAccountIdSet);
        
        if (weiboRes.isSuccess() == false) {
            renderError(weiboRes.getMessage());
        } else {
            renderSuccess(weiboRes.getMessage(), null);
        }
        
                
    }
    
    public static void testSyncWeibo() {
        SyncWeiboMsgThread job = new SyncWeiboMsgThread();
        job.now();
    }


    
    public static void testClearWeibo() {
        ClearWeiboMsgThread job = new ClearWeiboMsgThread();
        job.now();
    }
    
    public static void addSyncUserWeibo() {
        User user = getUser();
        SyncForNewAccountJob.addQueue(user);
    }

    public static void currentuser() {
    	User user = getUser();
    	renderJSON(JsonUtil.getJson(user));
    }
}
