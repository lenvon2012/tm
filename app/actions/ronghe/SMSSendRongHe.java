package actions.ronghe;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smsprovider.SendInfo;
import smsprovider.SmsStatus;
import actions.juxin.JUXinSmsSend;

public class SMSSendRongHe {
    
    private static final Logger log = LoggerFactory.getLogger(SMSSendRongHe.class);
    
    private static final String USER_NAME = "lh160513";
    
    private static final String PASSWORD = "123456aA";
    
    private static final String URL = "http://118.126.4.12/msg/HttpSendSM";
    
    private static final String URL_BALANCE = "http://118.126.4.12/msg/QueryBalance?account="+ USER_NAME +"&pswd="+ PASSWORD;
    
    /**
     * 短信发送接口  
     * @param phoneNumber  手机号码
     * @param smsContent  短信内容
     */
    public static SendInfo sendSms(String phoneNumber, String smsContent, String userShopName) {
        if(StringUtils.isEmpty(phoneNumber) || StringUtils.isEmpty(smsContent)){
            log.error("-99 : phoneNumber or smsContent is null");
            return new SendInfo(0L, System.currentTimeMillis(), phoneNumber, smsContent, SmsStatus.EMayErr);
        }
        if(userShopName.length() > 8){
            userShopName = userShopName.substring(0, 8);
        }
        smsContent = "【" + userShopName + "】" + smsContent;
//        if(!smsContent.endsWith("退订回T")){
//            smsContent = smsContent + " 退订回T";
//        }
        // 参数拼接
        StringBuffer param = new StringBuffer();
        param.append("account=").append(USER_NAME);
        param.append("&pswd=").append(PASSWORD);
        param.append("&mobile=").append(phoneNumber);
        param.append("&msg=").append(smsContent);
        param.append("&needstatus=").append(true);
        // 发送短信
        String result = JUXinSmsSend.sendPost(URL, param.toString());
        String[] split = result.split("\n");
        String[] split2 = split[0].split(",");
        if("0".equals(split2[1])){
            return new SendInfo(0L, System.currentTimeMillis(), phoneNumber, smsContent, SmsStatus.SUCCESS);
        }
        log.error("send sms error " + split2[1]);
        return new SendInfo(0L, System.currentTimeMillis(), phoneNumber, smsContent, SmsStatus.EMayErr);
    }
    
    /**
     * 短信余额查询
     */
    public static int querySmsBalances(){
        String result = JUXinSmsSend.sendPost(URL_BALANCE, StringUtils.EMPTY);
        System.out.println(result);
        String[] split = result.split("\n");
        String[] split2 = split[1].split(",");
        return Integer.valueOf(split2[1]);
    }
    
    public static void main(String[] args) {
        sendSms("18814887685", "测试短信。tb7736512_2012: 淘宝买家9999勇给您中评，请及时登录淘宝查看，购买宝贝现代简约风格装修效果图制作室内客厅吊顶三居室设计图二居室房子。", "MAX出品");
//        int querySmsBalances = querySmsBalances();
//        System.out.println(querySmsBalances);
    }
    
}
