package actions.weibo;

import java.util.List;

import models.user.User;
import models.weibo.AccountUserUpdateTs;
import models.weibo.SocialAccountPlay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weibo4j.Users;

import com.ciaosir.client.utils.DateUtil;

public class SocialAccountAction {

    private static final Logger log = LoggerFactory.getLogger(SocialAccountAction.class);
    
    //同步微博用户信息，包括粉丝数，关注数
    public static void ensureSyncAccountUser(User user, int accountType, List<SocialAccountPlay> accountList) {
        
        AccountUserUpdateTs updateTs = AccountUserUpdateTs.findByAccountType(user.getId(), accountType);
        if (updateTs == null) {
            updateTs = new AccountUserUpdateTs(user.getId(), accountType, 0);
        }
        
        long currDate = DateUtil.formCurrDate();
        
        if (updateTs.getDayTs() == currDate) {
            return;
        }
        
        //每天都要同步一次account user
        log.info("do first sync user weibo account today: " + currDate + "------------");
        
        for (SocialAccountPlay account : accountList) {
            if (account == null) {
                continue;
            }
            if (account.isBinding() == false) {
                continue;
            }
            if (account.isOutOfDate() == true) {
                continue;
            }
            //同步
            try {
                //获取user
                Users um = new Users();
                um.client.setToken(account.getToken());
                weibo4j.model.User weiboUser = um.showUserById(account.getAccountId());
                account.updateSinaAccountBasic(weiboUser);
                
                account.jdbcSave();
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            
        }
        
        updateTs.setDayTs(currDate);
        updateTs.jdbcSave();
    }
    
}
