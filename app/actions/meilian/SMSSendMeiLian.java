package actions.meilian;

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

/*
	* username  用户名
	* password_md5   密码
	* mobile  手机号
	* apikey  apikey秘钥
	* content  短信内容
	* startTime  UNIX时间戳，不写为立刻发送，http://tool.chinaz.com/Tools/unixtime.aspx （UNIX时间戳网站）
	* 
	* success:msgid  提交成功。
	* error:msgid  提交失败  
	* error:Missing username  用户名为空
	* error:Missing password  密码为空
	* error:Missing apikey  APIKEY为空
	* error:Missing recipient  手机号码为空
	* error:Missing message content  短信内容为空
	* error:Account is blocked  帐号被禁用
	* error:Unrecognized encoding  编码未能识别
	* error:APIKEY or password_md5 error  APIKEY或密码错误
	* error:Unauthorized IP address  未授权 IP 地址
	* error:Account balance is insufficient  余额不足
* */

/**
 * 美联软通  发送短信类
 */
public class SMSSendMeiLian {
    
    private static final Logger log = LoggerFactory.getLogger(SMSSendMeiLian.class);
    
    private static final String INDUSTRY_USER_NAME = "10898652";
    
    private static final String INDUSTRY_PASSWORD = "abc123456";
    
    private static final String MARKETING_USER_NAME = "610427806";
    
    private static final String MARKETING_PASSWORD = "123456";
    
    private static final String APIKEY = "4b4e2acfa9ec71cc7132f89f80138edc";
    
    /** 行业短信发送链接 */
    private static final String INDUSTRY_URL = "http://m.5c.com.cn/api/send/index.php";
    
    /** 营销短信发送链接 */
    private static final String MARKETING_URL = "http://115.28.10.221:8877/jk.aspx";
    
    /** 用户的通道ID */
    private static final String SMS_TYPE = "41";
    
    private static final String ENCODE = "UTF-8";
    
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
        smsContent = "【" + userShopName + "】" + smsContent;
        // 该通道不需要加退订回T
        if(smsContent.endsWith("退订回T")){
            smsContent = smsContent.substring(0, smsContent.length() - 4);
        }
        //对短信内容做Urlencode编码操作。
        String contentUrlEncode = StringUtils.EMPTY;
        try {
			contentUrlEncode = URLEncoder.encode(smsContent, ENCODE);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        // 参数拼接
        StringBuffer param = new StringBuffer();
        param.append("username=").append(INDUSTRY_USER_NAME);
        param.append("&password_md5=").append(string2MD5(INDUSTRY_PASSWORD));
        param.append("&apikey=").append(APIKEY);
        param.append("&mobile=").append(phoneNumber);
        param.append("&content=").append(contentUrlEncode);
        param.append("&encode=").append(ENCODE);
        // 发送短信
        String result = SMSSendMeiLian.sendPost(INDUSTRY_URL, param.toString());
        String[] split = result.trim().split(":");
        if("success".equals(split[0])){
            return new SendInfo(0L, System.currentTimeMillis(), phoneNumber, smsContent, SmsStatus.SUCCESS);
        }
        log.error("send sms error " + split[1]);
        return new SendInfo(0L, System.currentTimeMillis(), phoneNumber, smsContent, SmsStatus.EMayErr);
    }
    
    /**
     * 营销	短信发送接口  
     * @param phoneNumber  手机号码
     * @param smsContent  短信内容
     */
    public static SendInfo sendMarketingSms(String phoneNumber, String smsContent, String userShopName) {
        if(StringUtils.isEmpty(phoneNumber) || StringUtils.isEmpty(smsContent)){
            log.error("-99 : phoneNumber or smsContent is null");
            return new SendInfo(0L, System.currentTimeMillis(), phoneNumber, smsContent, SmsStatus.EMayErr);
        }
        if(userShopName.length() > 20){
            userShopName = userShopName.substring(0, 20);
        }
        
        userShopName = userShopName.replaceAll("【", "(");
        userShopName = userShopName.replaceAll("】", ")");
        smsContent = smsContent.replaceAll("【", "(");
        smsContent = smsContent.replaceAll("】", ")");
        
        smsContent = "【" + userShopName + "】" + smsContent;
        if(!smsContent.endsWith("退订回T")){
            smsContent = smsContent + " 退订回T";
        }
        // 参数拼接
        StringBuffer param = new StringBuffer();
        param.append("zh=").append(MARKETING_USER_NAME);
        param.append("&mm=").append(MARKETING_PASSWORD);
        param.append("&hm=").append(phoneNumber);
        param.append("&nr=").append(smsContent);
        param.append("&sms_type=").append(SMS_TYPE);
        // 发送短信
        String result = SMSSendMeiLian.sendPost(MARKETING_URL, param.toString());
        String[] split = result.trim().split(":");
        if(split.length == 2 && "0".equalsIgnoreCase(split[0])){
            return new SendInfo(0L, System.currentTimeMillis(), phoneNumber, smsContent, SmsStatus.SUCCESS);
        }
        log.error("send sms error " + result);
        return new SendInfo(0L, System.currentTimeMillis(), phoneNumber, smsContent, SmsStatus.EMayErr);
    }
    
    /**
	 * 向指定 URL 发送POST方法的请求
	 * 
	 * @param url 发送请求的 URL
	 * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @return 所代表远程资源的响应结果
	 */
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
    
    private static String string2MD5(String inStr){  
        MessageDigest md5 = null;  
        try{  
            md5 = MessageDigest.getInstance("MD5");  
        }catch (Exception e){  
        	log.error("MD5 error:", e);
            return "";  
        }  
        char[] charArray = inStr.toCharArray();  
        byte[] byteArray = new byte[charArray.length];  
  
        for (int i = 0; i < charArray.length; i++){
        	byteArray[i] = (byte) charArray[i];  
        }
        byte[] md5Bytes = md5.digest(byteArray);  
        StringBuffer hexValue = new StringBuffer();  
        for (int i = 0; i < md5Bytes.length; i++){  
            int val = ((int) md5Bytes[i]) & 0xff;  
            if (val < 16){
            	hexValue.append("0");  
            }
            hexValue.append(Integer.toHexString(val));  
        }  
        return hexValue.toString();  
    }  
    
    public static void main(String[] args) {
        sendIndustrySms("18814887685", "欧尼，您的宝贝已经韩国订购，1.收货时间9天左右（爆款延迟7-20个工作日）2.不支持7天退换。3.条形码包装袋保留，有问题好处理。心情和您一样，尽快尽快哦：）微信：jinxiaojiedaigou", "LUNA 韩国代购女装");
//    	sendMarketingSms("18814887685", "亲，我们的交易已经成功，请关注微信NBfuli001 领会员福利。祝您生活愉快！ 退订回T", "速推科技");
    }
    
}
