package actions.sms;

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
 * 吉因科技  发送短信类
 */
public class SmsSendJiYin {
	
	private static final Logger log = LoggerFactory.getLogger(SmsSendJiYin.class);
	
	private static final String INDUSTRY_USER_NAME = "100015";
	
	private static final String INDUSTRY_PASSWORD = "ycyh8888";
	
	/** 行业短信发送链接 */
	private static final String INDUSTRY_URL = "http://59.110.9.210:9002/md_httpserver/smsSend.do";
	
	/** 行业短信余额查询链接 */
	private static final String INDUSTRY_URL_BALANCE = "http://59.110.9.210:9002/md_httpserver/balance.do";
	
	private static final String ENCODE = "GBK";
	
	/**
	 * 行业/通知	短信发送接口  
	 * @param phoneNumber  手机号码
	 * @param smsContent  短信内容
	 */
	public static SendInfo sendIndustrySms(String phoneNumber, String smsContent, String userShopName, String kzm) {
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
		
		smsContent = smsContent + "【" + userShopName + "】";
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
		param.append("userid=").append(INDUSTRY_USER_NAME);
		param.append("&pwd=").append(string2MD5(INDUSTRY_PASSWORD));
		param.append("&mobile=").append(phoneNumber);
		param.append("&content=").append(contentUrlEncode);
		if(!StringUtils.isEmpty(kzm)) {
			param.append("&ext=").append(kzm);
		}
		// 发送短信
		String result = sendPost(INDUSTRY_URL, param.toString());
		if(!StringUtils.isEmpty(result)) {
			result = result.trim();
		}
		if(Long.valueOf(result) > 0){
			return new SendInfo(0L, System.currentTimeMillis(), phoneNumber, smsContent, SmsStatus.SUCCESS);
		}
		log.error("send sms error " + result);
		return new SendInfo(0L, System.currentTimeMillis(), phoneNumber, smsContent, SmsStatus.EMayErr);
	}
	
	/**
	 * 行业/通知	短信余额查询
	 */
	public static String industrySmsBalances(){
		// 参数拼接
		StringBuffer param = new StringBuffer();
		param.append("userid=").append(INDUSTRY_USER_NAME);
		param.append("&pwd=").append(string2MD5(INDUSTRY_PASSWORD));
		String result = sendPost(INDUSTRY_URL_BALANCE, param.toString());
		
		if(!StringUtils.isEmpty(result)) {
			result = result.trim();
		}
		
		return result;
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
			in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "GBK"));
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
	
	static String string2MD5(String inStr){  
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
		return hexValue.toString().toUpperCase();  
	}
	
	public static void main(String[] args) {
//		sendIndustrySms("13656676326", "验证码：555555。请在1分钟之内输入。", "吉因科技", null);
//		sendIndustrySms("18814887685", "为了感谢回馈老客户，即日起添加本店官方微信可获得3元现金红包，微信账号：15259730447（加好友备注旺旺ID）", "天天魔盒", null);
		industrySmsBalances();
	}
	
}
