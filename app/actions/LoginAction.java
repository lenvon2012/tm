
package actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.TaobaoUtil;
import configs.TMConfigs.App;

public class LoginAction {

    private static final Logger log = LoggerFactory.getLogger(LoginAction.class);

    public static final String TAG = "LoginAction";

    public static boolean login(String top_appkey, String top_parameters, String top_session,
            String top_sign) {

        // 登录并保存SESSION_KEY
        try {
            // Sign验证
            boolean flag1 = TaobaoUtil.validateSign(top_sign, top_appkey + top_parameters
                    + top_session, App.APP_SECRET);
            // 时间戳验证
//      boolean flag2 = TaobaoUtil.validateTimestamp(top_parameters, 5, 30);
            boolean flag2 = true;
            // 验证返回
            boolean flag3 = TaobaoUtil.verifyTopResponse(top_parameters, top_session, top_sign,
                    top_appkey, App.APP_SECRET);

            log.warn("Login verify :sign:" + flag1 + ",ts:" + flag2 + ",top response:" + flag3);

            return flag1 && flag2 && flag3;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}