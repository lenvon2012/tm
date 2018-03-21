package controllers;

import org.apache.commons.lang.StringUtils;

import secure.SimulateRrequest;
import secure.SimulateRrequest.ResultRisk;
import models.CRUDUser;
import controllers.Secure.Security;

public class CRUDDevSecurity extends Security {

    static boolean authentify(String username, String password) {
        //        return ("###tmadmin".equals(username) && "###tmadmin".equals(password))
        //                || ("tmadmin###".equals(username) && "tmadmin###".equals(password));
        //return ("###mianyangerdai".equals(username)) && ("mianyangerdai###".equals(password));
        boolean validUser = CRUDUser.isValidUser(username, password);
        // 御城河日志接入
        if (APIConfig.get().isRisk()) {
            if (!StringUtils.isEmpty(username)) {
                TMController.sendLoginLog(username, SimulateRrequest.TID, validUser, "unknownusernameorpassword");
            }
        }
        if (APIConfig.get().isRisk() && validUser) {
            // 验证成功，调用computeRisk计算风险
            ResultRisk sendComputeRisk = TMController.computeRisk(username);
            // 判定为有风险，进入二次身份验证流程
            if (sendComputeRisk.getRisk() > 0.5) {
                // 调用getVerifyUrl获取二次验证URL  给定redirectURL
                ResultRisk resultRisk = TMController.getVerifyUrl(username, session.getId(), "15703975326", APIConfig
                        .get().getRedirURL() + "/CRUDDevSecurity/verifyPassed");
                // 获取token
                String verifyUrl = resultRisk.getVerifyUrl();
                // 重定向到淘宝二次验证页面
                redirect(verifyUrl);
            }
        }
        return validUser;
    }

    public static void verifyPassed(String token) {
        // 判断验证是否通过
        ResultRisk verifyPassed = TMController.isVerifyPassed(token);
        if (SimulateRrequest.SUCCESS.equals(verifyPassed.getResult())
                && SimulateRrequest.SUCCESS.equals(verifyPassed.getVerifyResult())) {
            // 验证通过  风险重置
            TMController.resetRisk("!@#bigesandai");
            // 重定向到  /local
            redirect(APIConfig.get().getRedirURL() + "/local");
        }
    }

    static void onDisconnected() {
        try {
            Secure.login();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
