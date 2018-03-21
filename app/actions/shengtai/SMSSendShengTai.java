package actions.shengtai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smsprovider.SendInfo;
import smsprovider.SmsStatus;

/**
 * 盛泰嘉和  发送短信类
 */
public class SMSSendShengTai {
    
    private static final Logger log = LoggerFactory.getLogger(SMSSendShengTai.class);
    
    private static final String INDUSTRY_USER_NAME = "STJH68_STKJ";
    
    private static final String INDUSTRY_PASSWORD = "stkj168.";
    
    /** 行业短信发送链接 */
    private static final String INDUSTRY_URL = "http://222.73.117.158/msg/HttpBatchSendSM";
    
    /** 行业短信余额查询链接 */
    private static final String INDUSTRY_URL_BALANCE = "http://222.73.117.158/msg/QueryBalance";
    
    private static final String NEED_STATUS = "false";
    
    /**
     * 行业/通知	短信发送接口  
     * @param phoneNumber  手机号码
     * @param smsContent  短信内容
     */
    public static SendInfo sendIndustrySms(String phoneNumber, String smsContent, String userShopName) {
        if(StringUtils.isEmpty(phoneNumber) || StringUtils.isEmpty(smsContent)){
            log.error("-99 : phoneNumber or smsContent is null");
            return new SendInfo(0L, System.currentTimeMillis(), phoneNumber, smsContent, SmsStatus.EMayErr);
        }
        if(userShopName.length() > 20){
            userShopName = userShopName.substring(0, 20);
        }
        if(smsContent.startsWith("【") == false) {
        	smsContent = "【" + userShopName + "】" + smsContent;
        }
        // 该通道不需要加退订回T
        if(smsContent.endsWith("退订回T")){
            smsContent = smsContent.substring(0, smsContent.length() - 4);
        }
        // 参数拼接
        StringBuffer param = new StringBuffer();
        param.append("account=").append(INDUSTRY_USER_NAME);
        param.append("&pswd=").append(INDUSTRY_PASSWORD);
        param.append("&mobile=").append(phoneNumber);
        param.append("&msg=").append(smsContent);
        param.append("&needstatus=").append(NEED_STATUS);
        // 发送短信
        String result = sendPost(INDUSTRY_URL, param.toString());
        String[] split = result.split("\n");
        String[] split2 = split[0].split(",");
        if("0".equals(split2[1])){
            return new SendInfo(0L, System.currentTimeMillis(), phoneNumber, smsContent, SmsStatus.SUCCESS);
        }
        log.error("send sms error " + split2[1]);
        return new SendInfo(0L, System.currentTimeMillis(), phoneNumber, smsContent, SmsStatus.EMayErr);
    }
    
    public static String sendPost(String url, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = StringUtils.EMPTY;
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
				result += "\n";
			}
			return result;
		} catch (MalformedURLException e) {
			log.error(e.toString());;
		} catch (IOException e) {
			log.error(e.toString());
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(in);
		}
		return result;
	}
        
    public static void main(String[] args) {
    	sendIndustrySms("13656676326", "您当前的手机验证码为:236750，验证码十分钟内有效。", "天天魔盒");
    }
    
}
