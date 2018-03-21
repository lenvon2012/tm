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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.helper.DataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import smsprovider.SendInfo;
import utils.DateUtil;

/**
 * 讯信通  发送短信类
 */
public class SMSSendXunXinTong {
	
	private static final Logger log = LoggerFactory.getLogger(SMSSendXunXinTong.class);
	
	private static final String INDUSTRY_USER_NAME = "320125";
	
	private static final String INDUSTRY_PASSWORD = "aa7wb2zg";
	
	/** 行业短信发送链接 */
	private static final String INDUSTRY_URL = "http://218.204.70.58:28083/CmppWebServiceJax/sendsms.jsp";
	
	/** 行业上行短信获取链接 */
	private static final String RECEIVE_MSG_URL = "http://218.204.70.58:28083/CmppWebServiceJax/getsms.jsp";
	
	/** 发送短信状态获取链接 */
	private static final String SEND_MSG_STATUS_URL = "http://218.204.70.58:28083/CmppWebServiceJax/getreport.jsp";
	
	/** 服务代码 */
	private static final String SERVERS_CODE = "10693848";
	
	private static final String ENCODE = "GBK";
	
	/**
	 * 行业/通知	短信发送接口  
	 * @param phoneNumber  手机号码
	 * @param smsContent  短信内容
	 */
	public static Boolean sendIndustrySms(String phoneNumber, String smsContent, String userShopName, String kzm) {
		if(StringUtils.isEmpty(phoneNumber) || StringUtils.isEmpty(smsContent)){
			return false;
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
		param.append("spid=").append(INDUSTRY_USER_NAME);
		param.append("&password=").append(INDUSTRY_PASSWORD);
		param.append("&nr=").append(contentUrlEncode);
		param.append("&mobs=").append(phoneNumber);
		param.append("&kzm=").append(kzm);
		// 发送短信
		String result = sendPost(INDUSTRY_URL, param.toString());
		if(!StringUtils.isEmpty(result)) {
			result = result.trim();
		}
		if("0".equals(result)){
			return true;
		}
		log.error("send sms error " + result);
		return false;
	}
	
	/**
	 * 行业/通知	上行短信获取
	 */
	public static String receiveMsgGet() {
		// 参数拼接
		StringBuffer param = new StringBuffer();
		param.append("spid=").append(INDUSTRY_USER_NAME);
		param.append("&password=").append(INDUSTRY_PASSWORD);
		String result = sendPost(RECEIVE_MSG_URL, param.toString());
		if(!StringUtils.isEmpty(result)) {
			result = result.trim();
		}
		return result;
	}
	
	/**
	 * 行业/通知	短信状态报告获取
	 */
	public static String sendMsgStatusGet() {
		// 参数拼接
		StringBuffer param = new StringBuffer();
		param.append("spid=").append(INDUSTRY_USER_NAME);
		param.append("&password=").append(INDUSTRY_PASSWORD);
		String result = sendPost(SEND_MSG_STATUS_URL, param.toString());
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
	
	public static void main(String[] args) {
		sendIndustrySms("18814887685", "创建支付密码的验证码为： 951866，不要告诉任何人。", "速推科技", "999999");
//		sendIndustrySms("13656676326", "亲在小店买的孕妇装 没让您满意很抱歉，求您删除评价返二十元钱的，知道不是钱的事，真心表达歉意，旺旺等您有删除步骤求您啦，精品孕妇装", "评价", "1");
//		receiveMsgGet();
	}
	
}
