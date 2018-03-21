package actions.juxin;

import org.apache.log4j.Logger;

import actions.juxin.JUXinSmsSend.ResultInfo;
import smsprovider.SendInfo;
import smsprovider.SmsStatus;

public class JUXinIdentifyingCode {

    private static final Logger log = Logger.getLogger(JUXinIdentifyingCode.class);
    
    private static final String USER_NAME = "bgxb2";
    
    private static final String PWD = "uLylORSm";
    
    public static SendInfo sendIdentifyingCode(String message, String mobile){
        // 参数拼接
        StringBuffer param = new StringBuffer();
        param.append("username=").append(USER_NAME);
        param.append("&pwd=").append(JUXinSmsSend.string2MD5(PWD));
        param.append("&p=").append(mobile);
        param.append("&isUrlEncode=").append("no");
        param.append("&msg=").append(message);
        param.append("&charSetStr=").append(JUXinSmsSend.CHARSET);
        // 发送短信
        String sendPost = JUXinSmsSend.sendPost(JUXinSmsSend.URL,param.toString());
        log.info(sendPost);
        ResultInfo resultInfo = JUXinSmsSend.parseJson(sendPost);
        return new SendInfo(0L, System.currentTimeMillis(), mobile, message, resultInfo.isSuccess() ? SmsStatus.SUCCESS : SmsStatus.EMayErr);
    }

//    public static void main(String[] args) {
//        JUXinIdentifyingCode.sendIdentifyingCode("【天小猫】您的校验码为： 589624", "18814887685");
//    }
}
