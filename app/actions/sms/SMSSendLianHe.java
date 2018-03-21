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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smsprovider.SendInfo;
import smsprovider.SmsStatus;

/**
 * 联合维拓  发送短信类
 */
public class SMSSendLianHe {
	
	private static final Logger log = LoggerFactory.getLogger(SMSSendLianHe.class);
	
	private static final String INDUSTRY_USER_NAME = "7SDK-LHW-0588-QFYSK";
	
	private static final String INDUSTRY_PASSWORD = "667055";
	
	/** 行业短信发送链接 */
	private static final String INDUSTRY_URL = "http://sdk.univetro.com.cn:6200/sdkproxy/sendsms.action";
	
	/** 行业短信余额查询链接 */
	private static final String INDUSTRY_URL_BALANCE = "http://sdk.univetro.com.cn:6200/sdkproxy/querybalance.action";
	
	/** 行业上行短信获取链接 */
	private static final String RECEIVE_MSG_URL = "http://sdk.univetro.com.cn:6200/sdkproxy/getmo.action";
	
	/** 发送短信状态获取链接 */
	private static final String SEND_MSG_STATUS_URL = "http://sdk.univetro.com.cn:6200/sdkproxy/getreport.action";
	
	/** 服务代码 */
	private static final String SERVERS_CODE = "480006";
	
	private static final String ENCODE = "UTF-8";
	
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
		param.append("cdkey=").append(INDUSTRY_USER_NAME);
		param.append("&password=").append(INDUSTRY_PASSWORD);
		param.append("&phone=").append(phoneNumber);
		param.append("&message=").append(contentUrlEncode);
		param.append("&seqid=").append(SERVERS_CODE);
		param.append("&addserial=").append(kzm);
		// 发送短信
		String result = sendPost(INDUSTRY_URL, param.toString());
		if(!StringUtils.isEmpty(result)) {
			result = result.trim();
		}
		result = result.substring(result.indexOf("<error>") + 7, result.indexOf("</error>"));
		if("0".equalsIgnoreCase(result)){
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
		param.append("cdkey=").append(INDUSTRY_USER_NAME);
		param.append("&password=").append(INDUSTRY_PASSWORD);
		String result = sendPost(INDUSTRY_URL_BALANCE, param.toString());
		
		if(!StringUtils.isEmpty(result)) {
			result = result.trim();
		}
		result = result.substring(result.indexOf("<message>") + 9, result.indexOf("</message>"));
		
		return result;
	}
	
	/**
	 * 行业/通知	上行短信获取
	 */
//	public static String receiveMsgGet() {
//		// 参数拼接
//		StringBuffer param = new StringBuffer();
//		param.append("cdkey=").append(INDUSTRY_USER_NAME);
//		param.append("&password=").append(INDUSTRY_PASSWORD);
//		String result = sendPost(RECEIVE_MSG_URL, param.toString());
//		if(!StringUtils.isEmpty(result)) {
//			result = result.trim();
//		}
//		return result;
//	}
	
	/**
	 * 行业/通知	短信状态报告获取
	 */
//	public static String sendMsgStatusGet() {
//		// 参数拼接
//		StringBuffer param = new StringBuffer();
//		param.append("cdkey=").append(INDUSTRY_USER_NAME);
//		param.append("&password=").append(INDUSTRY_PASSWORD);
//		String result = sendPost(SEND_MSG_STATUS_URL, param.toString());
//		if(!StringUtils.isEmpty(result)) {
//			result = result.trim();
//		}
//		return result;
//	}
	
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
			in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
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
		sendIndustrySms("17721482133", "淘宝买家黄馨语给您中评，请及时登录淘宝查看，购买宝贝美式风格现代美式乡村田园实景房屋室内家装三居室装修设计效果图。", "测试", "");
//		industrySmsBalances();
//		receiveMsgGet();
//		sendMsgStatusGet();
	}
	
}
