package actions.sms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 吉因科技  发送短信类
 */
public class SmsSendLaiqt {
	
	private static final Logger log = LoggerFactory.getLogger(SmsSendLaiqt.class);
	
	/**
	 * 用户帐号
	 */
	private static final String PARTNER_NAME = "defender";
	
	/**
	 * 帐号密码
	 */
	private static final String PARTNER_PASSWORD = "975ecb719692fa2bc7255b0c2dd2f3a4";
	
	public static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd hh:mm:ss");
	
	/** 
	 * 自定义签名营销短信
	 * */
	private static final String MARKETING_URL_ANAY_SIGN = "http://sms.laiqt.com/SmsController/sendMarketingSms?";
	
	/** 
	 * 短信状态获取链接
	 * */
	private static final String SEND_MSG_STATUS_URL = "http://sms.laiqt.com/SmsController/smsStatus?";
	
	/** 
	 * 短信上行获取链接
	 * */
	private static final String RECEIVE_MSG_URL = "http://sms.laiqt.com/SmsController/smsReply?";
	
	/**
	 * Laiqt短信发送接口  
	 */
	public static SmsResult sendLaiqtSms(String phoneNumber, String smsContent, String userShopName) {
		SmsResult result = new SmsResult();
		if(StringUtils.isEmpty(phoneNumber) || StringUtils.isEmpty(smsContent)){
			return new SmsResult(false, 0, 1, 0, 0L, StringUtils.EMPTY);
		}
		if(userShopName.length() > 10){
			userShopName = userShopName.substring(0, 10);
		}
		
		userShopName = userShopName.replaceAll("【", "(");
		userShopName = userShopName.replaceAll("】", ")");
		smsContent = smsContent.replaceAll("【", "(");
		smsContent = smsContent.replaceAll("】", ")");
		
		smsContent = "【" + userShopName + "】" + smsContent;
		// 该通道需要加退订回T
		if(!smsContent.endsWith("退订回T")){
//			smsContent = smsContent.substring(0, smsContent.length() - 4);
			smsContent += " 退订回T";
		}
		
		// 参数拼接
		StringBuffer param = new StringBuffer();
		param.append("phones=").append(phoneNumber);
		param.append("&message=").append(smsContent);
		param.append("&pname=").append(PARTNER_NAME);
		param.append("&ppsw=").append(PARTNER_PASSWORD);
		param.append("&_t=").append(System.currentTimeMillis());

		// 发送短信
		String resultStr = sendPost(MARKETING_URL_ANAY_SIGN, param.toString());
		if(!StringUtils.isEmpty(resultStr)) {
			resultStr = resultStr.trim();
		}
		
		result = parseResult(resultStr);
		
		return result;
	}
	
	public static SmsResult parseResult(String jsonString) {
		SmsResult result = null;
		try {
			JSONObject jsonObject = new JSONObject(jsonString);
			boolean isSubmited = jsonObject.getBoolean("isSubmited");
			int successCount = jsonObject.getInt("successCount");
			int failCount = jsonObject.getInt("failCount");
			int errorCode = jsonObject.getInt("errorCode");
			Long batchId = jsonObject.getLong("batchId");
			String nick = jsonObject.getString("nick");
			
			result = new SmsResult(isSubmited, successCount, failCount, errorCode, batchId, nick);
			return result;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return result;
	}
	
	/**
	 * 短信返回结果
	 */
	public static class SmsResult {

		/**
		 * 提交状态
		 */
		private Boolean isSubmited;
		
		/**
		 * 成功条数
		 */
		private int successCount;
		
		/**
		 * 失败条数
		 */
		private int failCount;

		/**
		 * 错误码
		 */
		private int errorCode;
		
		/**
		 * 批次号
		 */
		private Long batchId;

		/**
		 * 用户nick
		 */
		private String nick;
		
		public Boolean getIsSubmited() {
			return isSubmited;
		}

		public void setIsSubmited(Boolean isSubmited) {
			this.isSubmited = isSubmited;
		}

		public int getSuccessCount() {
			return successCount;
		}

		public void setSuccessCount(int successCount) {
			this.successCount = successCount;
		}

		public int getFailCount() {
			return failCount;
		}

		public void setFailCount(int failCount) {
			this.failCount = failCount;
		}

		public int getErrorCode() {
			return errorCode;
		}

		public void setErrorCode(int errorCode) {
			this.errorCode = errorCode;
		}

		public Long getBatchId() {
			return batchId;
		}

		public void setBatchId(Long batchId) {
			this.batchId = batchId;
		}

		public String getNick() {
			return nick;
		}

		public void setNick(String nick) {
			this.nick = nick;
		}

		public SmsResult() {
			
		}
		
		public SmsResult(Boolean isSubmited, int successCount, int failCount, int errorCode, Long batchId, String nick) {
			this.isSubmited = isSubmited;
			this.successCount = successCount;
			this.failCount = failCount;
			this.errorCode = errorCode;
			this.batchId = batchId;
			this.nick = nick;
		}

	}
	
	/**
	 * Laiqt短信状态报告获取
	 */
	public static String sendMsgStatusGet(int num) {
		// 参数拼接
		StringBuffer param = new StringBuffer();
		param.append("num=").append(num);
		param.append("&pname=").append(PARTNER_NAME);
		param.append("&ppsw=").append(PARTNER_PASSWORD);
		param.append("&_t=").append(System.currentTimeMillis());
		String result = sendPost(SEND_MSG_STATUS_URL, param.toString());
		if(!StringUtils.isEmpty(result)) {
			result = result.trim();
		}
		return result;
	}
	
	/**
	 * Laiqt短信上行获取
	 */
	public static String receiveMsgGet(int num) {
		// 参数拼接
		StringBuffer param = new StringBuffer();
		param.append("num=").append(num);
		param.append("&pname=").append(PARTNER_NAME);
		param.append("&ppsw=").append(PARTNER_PASSWORD);
		param.append("&_t=").append(System.currentTimeMillis());
		String result = sendPost(RECEIVE_MSG_URL, param.toString());
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
//		sendLaiqtSms("18814887685", "您的验证码：000001,请在一分钟内输入。","天天魔盒");
		sendMsgStatusGet(2);
	}
}
