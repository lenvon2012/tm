
package actions;

import java.util.HashSet;
import java.util.Set;

import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Controller;
import play.mvc.Scope.Session;
import secure.SimulateRequestUtil;
import utils.TaobaoUtil;
import bustbapi.LoginApi;
import bustbapi.TMTradeApi;
import bustbapi.UserAPIs;

import com.ciaosir.client.CommonUtils;

import configs.Subscribe.Version;
import configs.TMConfigs;
import configs.TMConfigs.App;
import configs.TMConfigs.WebParams;
import dao.UserDao;

/**
 * 用户Action
 *
 */
public class UserAction extends Controller {

    public static final Logger log = LoggerFactory.getLogger(UserAction.class);

    public static int updateUser(models.user.User userPlay) {

        com.taobao.api.domain.User userTB = new UserAPIs.UserGetApi(userPlay.sessionKey, userPlay.getUserNick()).call();
        if (userTB == null) {
            return Version.FREE;
        }

        int ver = SubcribeAction.getSubscribeInfo(userPlay);
        userPlay.updateVersion(ver);
        userPlay.jdbcSave();

        if (TMConfigs.SMS_ONLINE) {
            SubcribeAction.updateSmsCount(userPlay);
        }

        return ver;
    }

    /**
     * 登录 有错误的时候返回错误提示，否则返回null
     */
    public static boolean login(String top_appkey, String top_parameters, String top_session, String top_sign) {

        log.error("Secret :" + App.APP_SECRET);

        // 登录并保存SESSION_KEY
        try {
            // Sign验证
            boolean flag1 = TaobaoUtil
                    .validateSign(top_sign, top_appkey + top_parameters + top_session, App.APP_SECRET);
            // 时间戳验证
            // boolean flag2 = TaobaoUtil.validateTimestamp(top_parameters, 5,
            // 30);
            boolean flag2 = true;
            // 验证返回
            boolean flag3 = TaobaoUtil.verifyTopResponse(top_parameters, top_session, top_sign, top_appkey,
                    App.APP_SECRET);

            log.warn("Login verify :sign:" + flag1 + ",ts:" + flag2 + ",top response:" + flag3);

            return flag1 && flag2 && flag3;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 登出
     */
    public static void logout(Session session) {
        // 登出并清除SESSION_KEY
        if (session.get(WebParams.SESSION_USER_KEY) != null)
            session.clear();// 清除所有Session信息
    }

    /**
     * 检查登录状态
     */
    public static boolean checkLogin(Session session) {
        // 检查Session中是否有SESSION_KEY
        if (session.get(WebParams.SESSION_USER_KEY) != null)
            return true;
        else
            return false;
    }

    /**
     * 检查登录状态
     */
    public static boolean checkLogin() {
        // 检查Session中是否有SESSION_KEY
        if (session.get(WebParams.SESSION_USER_KEY) != null)
            return true;
        else
            return false;
    }

    /**
     * 取得当前在Session中的用户
     * 
     * @return
     */
    public static User getSessionUser() {
        Long userId = CommonUtils.String2Long(session.get(WebParams.SESSION_USER_ID));
        if (userId == null)
            return null;
        else
            return UserDao.findById(userId);
    }

    public static Long getSessionUserId() {
        String sessionId = session.get(WebParams.SESSION_USER_ID);
        return sessionId == null ? null : CommonUtils.String2Long(sessionId);
    }

    public static boolean checkUser(User user) {
        return user == null || user.sessionKey == null || user.sessionKey == "" ? false : true;
    }

    public static String getSellerMobile(String sid) {
        Long tid = new TMTradeApi.GetSellerOneTid(sid).call();
        if (tid == null) {
            return null;
        }

        // 御城河日志接入
        SimulateRequestUtil.sendTopLog(SimulateRequestUtil.TRADE_FULLINFO_GET);
        return new TMTradeApi.GetSellerMobile(sid, tid).call();
    }

    public static Set<Long> debugUserId = new HashSet<Long>();

    public static boolean isUserDebug(User user) {
        if (user == null) {
            return false;
        }
        return debugUserId.contains(user.getId());
    }
    
    
    
    
    /**
     * 直通车
     * @author ying
     *
     */
    public static class SubwayTokenErrorType {
        public static int VAILD = 0;
        public static int NOT_MAIN_MEMBER = 1;
        public static int MEMBERID_IS_NULL = 2;
        public static int RETURN_NULL = 4;
    }

    public static int checkUserBusEnable(String sid, String userNick) {

        String subwayToken = new LoginApi.AuthsignGetApi(sid, userNick).call();
        if (StringUtils.isEmpty(subwayToken)) {
            log.warn(" No Sub Way Token Returned....");
            return SubwayTokenErrorType.RETURN_NULL;
        } else if (subwayToken.contains("账户memeberid不能为空")) {
            return SubwayTokenErrorType.MEMBERID_IS_NULL;
        } else if (subwayToken.contains("直通车主账号不存在")) {
            return SubwayTokenErrorType.NOT_MAIN_MEMBER;
        } else if (subwayToken.contains("无法根据nick获取直通车帐号信息")) {
            return SubwayTokenErrorType.NOT_MAIN_MEMBER;
        }
        return SubwayTokenErrorType.VAILD;
    }
}
