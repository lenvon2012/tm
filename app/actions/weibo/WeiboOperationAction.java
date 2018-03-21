package actions.weibo;

import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

import models.user.User;
import models.weibo.AccountFriendRecord;
import models.weibo.ForwardMsgRecord;
import models.weibo.SocialAccountPlay;
import models.weibo.SocialAccountPlay.SocialAccountFunction;
import models.weibo.TodayAttentionPlay;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weibo4j.Friendships;
import weibo4j.Timeline;

import com.ciaosir.client.utils.NumberUtil;

public class WeiboOperationAction {

    private static final Logger log = LoggerFactory.getLogger(WeiboOperationAction.class);
    
    
    //error:already followed error_code:20506/2/friendships/create.json
    private static String getErrorMsg(String errorMsg) {
        if (StringUtils.isEmpty(errorMsg)) {
            return "";
        }
        
        int startIndex = errorMsg.indexOf("error_code:");
        if (startIndex < 0) {
            return "";
        }
        startIndex += "error_code:".length();
        
        int endIndex = errorMsg.indexOf("/", startIndex);
        
        String errorCode = "";
        if (endIndex < 0) {
            errorCode = errorMsg.substring(startIndex);
        } else {
            errorCode = errorMsg.substring(startIndex, endIndex);
        }
        
        int codeInt = NumberUtil.parserInt(errorCode, 0);
        
        String message = WeiboErrorCodeAction.getErrorMsg(codeInt);
        
        return message;
        
    }
    
    private static double roundContribute(double contribute) {
        contribute = Math.round(contribute * 10) / 10;
        
        return contribute;
    }
    
    public static WeiboOperationResult doForwardWeibo(User user, int accountType, 
            Long friendUserId, String friendAccountId, String weiboId, String forwardMsg) {
        
        try {
            
            SocialAccountPlay mainAccount = SocialAccountPlay.findByFunction(user.getId(), accountType, 
                    SocialAccountFunction.MainAccount);
            
            
            SocialAccountPlay slaveAccount = SocialAccountPlay.findByFunction(user.getId(), accountType, 
                    SocialAccountFunction.SlaveAccount);
            
            TodayAttentionPlay attention = TodayAttentionPlay.ensureTodayAttention(user.getId(), accountType);
            
            if (slaveAccount.isSinaAccount()) {

                forwardMsg = URLEncoder.encode(forwardMsg, "utf-8");
                
                Timeline timeline = new Timeline();
                timeline.setToken(slaveAccount.getToken());
                timeline.Repost(weiboId, forwardMsg, 0);
            } else {
                return new WeiboOperationResult(false, "系统出现异常，微博类型出错，请联系我们！");
            }
            
            SocialAccountPlay friendAccount = SocialAccountPlay.findByFunction(friendUserId, accountType, 
                    SocialAccountFunction.MainAccount);
            if (friendAccount != null) {
                
                double contribute = friendAccount.getContribution();
                if (contribute >= 1) {
                    friendAccount.setNewForwardNum(friendAccount.getNewForwardNum() + 1);
                    friendAccount.setContribution(roundContribute(contribute - 1));
                    friendAccount.jdbcSave();
                }

            }
            
            //存储记录
            mainAccount.setContribution(roundContribute(mainAccount.getContribution() + 0.8));
            mainAccount.setContributeTs(System.currentTimeMillis());
            mainAccount.jdbcSave();
            
            //最近转发次数
            slaveAccount.setNewForwardNum(slaveAccount.getNewForwardNum() + 1);
            slaveAccount.jdbcSave();
            
            ForwardMsgRecord forwardRecord = new ForwardMsgRecord(slaveAccount.getAccountId(), 
                    friendAccountId, weiboId, accountType);
            forwardRecord.jdbcSave();
            attention.setForwardNum(attention.getForwardNum() + 1);
            attention.jdbcSave();
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            
            String errorMsg = getErrorMsg(ex.getMessage());
            if (StringUtils.isEmpty(errorMsg)) {
                errorMsg = "您可以稍后重试或联系我们";
            }
            
            return new WeiboOperationResult(false, "转发微博失败，" + errorMsg + "！");
        }
        
        return new WeiboOperationResult(true, "");
        
    }
    
    
    public static WeiboOperationResult doFriendAccounts(User user, int accountType, 
            Long friendUserId, Set<String> friendAccountIdSet) {
        
        SocialAccountPlay mainAccount = SocialAccountPlay.findByFunction(user.getId(), accountType, 
                SocialAccountFunction.MainAccount);
        
        SocialAccountPlay slaveAccount = SocialAccountPlay.findByFunction(user.getId(), accountType, 
                SocialAccountFunction.SlaveAccount);
        
        TodayAttentionPlay attention = TodayAttentionPlay.ensureTodayAttention(user.getId(), accountType);
        
        int successNum = 0;
        int failNum = 0;
        Set<String> errorMsgSet = new HashSet<String>();
        for (String friendAccountId : friendAccountIdSet) {
            WeiboOperationResult opRes = doOneFriendAccount(user, accountType, 
                    friendUserId, friendAccountId, mainAccount, slaveAccount, attention);
            if (opRes.isSuccess() == false) {
                failNum++;
                errorMsgSet.add(opRes.getMessage());
            } else {
                successNum++;
            }
        }
        
        String message = "成功关注" + successNum + "个微博";
        boolean isSuccess = true;
        if (failNum > 0) {
            message += ", 失败" + failNum + "个！";
            message += StringUtils.join(errorMsgSet, ";");
        } else {
            message += "！";
        }
        
        if (successNum > 0) {
            isSuccess = true;
        } else {
            isSuccess = false;
        }
        
        return new WeiboOperationResult(isSuccess, message);
    }
    
    
    private static WeiboOperationResult doOneFriendAccount(User user, int accountType, 
            Long friendUserId, String friendAccountId, 
            SocialAccountPlay mainAccount, SocialAccountPlay slaveAccount, TodayAttentionPlay attention) {

        try {
            
            if (StringUtils.isEmpty(friendAccountId)) {
                return new WeiboOperationResult(false, "帐号ID为空");
            }
            
            
            if (slaveAccount.isSinaAccount()) {

                Friendships friendships = new Friendships();
                friendships.setToken(slaveAccount.getToken());
                
                friendships.createFriendshipsById(friendAccountId);
                
            } else {
                return new WeiboOperationResult(false, "微博类型出错");
            }
            
            SocialAccountPlay friendAccount = SocialAccountPlay.findByFunction(friendUserId, accountType, 
                    SocialAccountFunction.MainAccount);
            if (friendAccount != null) {
                
                double contribute = friendAccount.getContribution();
                if (contribute >= 1) {
                    friendAccount.setFansNum(friendAccount.getFansNum() + 1);
                    friendAccount.setAddFansNum(friendAccount.getAddFansNum() + 1);
                    friendAccount.setContribution(roundContribute(contribute - 1));
                    friendAccount.jdbcSave();
                }

            }
            
            //存储记录
            mainAccount.setContribution(roundContribute(mainAccount.getContribution() + 0.8));
            mainAccount.setContributeTs(System.currentTimeMillis());
            mainAccount.jdbcSave();
            
            slaveAccount.setAttentionNum(slaveAccount.getAttentionNum() + 1);
            slaveAccount.setNewAttentionNum(slaveAccount.getNewAttentionNum() + 1);
            slaveAccount.jdbcSave();
            
            AccountFriendRecord friendRecord = new AccountFriendRecord(slaveAccount.getAccountId(), 
                    friendAccountId, accountType);
            friendRecord.jdbcSave();
            attention.setAttentionNum(attention.getAttentionNum() + 1);
            attention.jdbcSave();
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            String errorMsg = getErrorMsg(ex.getMessage());
            if (StringUtils.isEmpty(errorMsg)) {
                errorMsg = "";
            }
            
            return new WeiboOperationResult(false, errorMsg);
        }
        
        return new WeiboOperationResult(true, "");
        
        
    }
    
    
    
    public static class WeiboOperationResult {
        
        private boolean isSuccess;
        
        private String message;

        public boolean isSuccess() {
            return isSuccess;
        }

        public void setSuccess(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public WeiboOperationResult(boolean isSuccess, String message) {
            super();
            this.isSuccess = isSuccess;
            this.message = message;
        }
        
        
        
    }
    
    /*
    public static void main(String[] args) {
        
        try {
            String errorFile = "/home/ying/tmp/errorCode";
            
            List<String> errorCodeList = FileUtils.readLines(new File(errorFile));
            
            for (String errorCode : errorCodeList) {
                if (StringUtils.isEmpty(errorCode)) {
                    continue;
                }
                String[] arr = errorCode.split("：");
                if (arr == null || arr.length != 2) {
                    continue;
                }
                System.out.println("WeiboErrorCodeMap.put(" + arr[0].trim() + ", \"" + arr[1].trim() + "\");");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        
    }
    
    */
}
