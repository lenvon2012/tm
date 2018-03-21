package secure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.google.gson.Gson;
import com.taobao.api.domain.Item;

import controllers.APIConfig;

/** 
 * 御城河日志接入
 * 
 * 比格希勃网络技术有限公司   68756527  SjtlI3HXclfQh0xpJRAB
 * 骑着绵羊飞  68756511  w4jRdbsPUCL6zV5ji22n
 */ 
public class SimulateRrequest implements Callable<Boolean>{ 
	
	private static final Logger log = LoggerFactory.getLogger(SimulateRrequest.class);
	
	private static Map<String, String> logUrl = new HashMap<String, String>(5);
	
	private static final String UTF_8 = "UTF-8";
	
	public static final String SUCCESS = "success";
	
	private static final String FAIL = "fail";
	
	public static final String TID = "速推科技44";
	
	static {
		// 登录日志
		logUrl.put(LogType.LOGIN, "http://gw.ose.aliyun.com/event/login");
		// 订单访问日志
		logUrl.put(LogType.ORDER, "http://gw.ose.aliyun.com/event/order");
		// 订单发送到第三方的日志
		logUrl.put(LogType.SEND_ORDER, "http://gw.ose.aliyun.com/event/sendOrder");
		// TOP调用日志
		logUrl.put(LogType.TOP, "http://gw.ose.aliyun.com/event/top");
		// 数据库访问日志
		logUrl.put(LogType.SQL, "http://gw.ose.aliyun.com/event/sql");
		// 账号系统风险控制  登录日志
		logUrl.put(LogType.ACCOUNT_LOGIN, "http://account.ose.aliyun.com/login");
		// 风险计算(computeRisk)接口
		logUrl.put(LogType.COMPUTE_RISK, "http://account.ose.aliyun.com/computeRisk");
		// 获取二次验证地址(getVerifyUrl)接口（B/S端调用）
		logUrl.put(LogType.GET_VERIFY_URL, "http://account.ose.aliyun.com/getVerifyUrl");
		// 验证通过（isVerifyPassed）接口
		logUrl.put(LogType.IS_VERIFY_PASSED, "http://account.ose.aliyun.com/isVerifyPassed");
		// 风险重置（resetRisk）接口
		logUrl.put(LogType.RESET_RISK, "http://account.ose.aliyun.com/resetRisk");
	}

	private Param param;
	
	private String type;
	
	
	public SimulateRrequest(){  }
	
	public SimulateRrequest(Param param, String type){ 
		this.param = param;
		this.type = type;
	}
	
	@Override
	public Boolean call() throws Exception {
		String result_post = sendLog(this.param, this.type);
		LogResultInfo sendLog = new Gson().fromJson(result_post, LogResultInfo.class);
		boolean isSuccess = SUCCESS.equalsIgnoreCase(sendLog.result);
		if (!isSuccess) {
			log.error(sendLog.errMsg);
		}
		return isSuccess;
	}
	
	/**
	 * api调用
	 * 
	 * @param param 需要接入的参数	
	 * @param type	接入类型   可选值1、login 2、order 3、sendOrder 4、top 5、sql
	 */
	public static String sendLog(Param param, String type){
		StringBuffer query = new StringBuffer();
		// 可以按照自己的习惯构造参数
		Map<String,String> paramMap = constructParam(String.valueOf(System.currentTimeMillis()), type, param);
		// 生成签名
		String sign = getSignature(APIConfig.get().getLogAppSecret(), paramMap);
		
		for(Entry<String,String> en : paramMap.entrySet()){
			query.append(en.getKey());
			query.append("=");
			query.append(en.getValue());
			query.append("&");
		}
		query.append("sign=");
		query.append(sign);
		try{
            String result_post = doPost(logUrl.get(type), query.toString());
            return result_post;
		} catch (IOException e){
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	/**
	 * post mothod
	 */
	private static String doPost(String path, String query) throws IOException {

		URLConnection connection = null;
		OutputStreamWriter out = null;
		try {
			URL url = new URL(path);
			connection = url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestProperty("ContentType", "application/x-www-form-urlencoded");
			out = new OutputStreamWriter(connection.getOutputStream(), UTF_8);
			out.write(query); // 向页面传递数据。post的关键所在！
			out.flush();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
//			if (out != null)
//				out.close();
			IOUtils.closeQuietly(out);
		}

		// 一旦发送成功，用以下方法就可以得到服务器的回应：
		StringBuffer respStr = new StringBuffer();
		try {
			String sCurrentLine = null;
			InputStream l_urlStream = connection.getInputStream();
			BufferedReader l_reader = new BufferedReader(new InputStreamReader(l_urlStream));
			while ((sCurrentLine = l_reader.readLine()) != null) {
				respStr.append(sCurrentLine + "\r\n");
			}
			l_urlStream.close();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return respStr.toString();
	}

	// generate signature
	private static String getSignature(String appSecret, Map<String, String> paramMap) {
		try {
			if (paramMap == null) {
				return "";
			}
			// 按照key进行排序（升序）然后拼接字符串
			StringBuilder combineString = new StringBuilder();
			combineString.append(appSecret);
			Set<Entry<String, String>> entrySet = paramMap.entrySet();
			for (Entry<String, String> entry : entrySet) {
				combineString.append(entry.getKey() + entry.getValue());
			}
			combineString.append(appSecret);
			
			// 获取拼接字符串的utf-8编码字节序列
			byte[] bytesOfMessage = combineString.toString().getBytes(UTF_8);
			// md5加密
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] thedigest = md.digest(bytesOfMessage);
			// 加密后的字节转化为16进制
			String signature = bytesToHexString(thedigest);
			return signature;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return "";
		}
	}
	
	private static String bytesToHexString(byte[] src) {
		try {
			StringBuilder stringBuilder = new StringBuilder("");
			if (src == null || src.length <= 0) {
				return null;
			}
			for (int i = 0; i < src.length; i++) {
				int v = src[i] & 0xFF;
				String hv = Integer.toHexString(v);
				if (hv.length() < 2) {
					stringBuilder.append(0);
				}
				stringBuilder.append(hv);
			}
			return stringBuilder.toString();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}
        
	private static Map<String, String> constructParam(String timeStamp, String path, Param param) {
		Map<String, String> paramMap = new TreeMap<String, String>();
		paramMap.put("time", timeStamp);
		paramMap.put("appKey", APIConfig.get().getLogAppkey());
		if(LogType.IS_VERIFY_PASSED.equals(path)){
			// 验证通过（isVerifyPassed）接口
			paramMap.put("token", param.getToken());
			return paramMap;
		}
		// 自有帐号的，登录日志接入帐号风控的；使用淘系帐号的，不需要接入风控
		paramMap.put("ati", param.getAti());
		paramMap.put("userId", param.getUserId());
		paramMap.put("userIp", param.getUserIp());
		paramMap.put("topAppKey", param.getTopAppKey());
		paramMap.put("appName", param.getAppName());
		// paramMap.put("serverIp", "serverIp");
		if (LogType.ORDER.equals(path)) {
			// 订单访问日志
			paramMap.put("url", param.getUrl());
			paramMap.put("tradeIds", param.getTradeIds());
			paramMap.put("operation", param.getOperation());
		} else if (LogType.SEND_ORDER.equals(path)) {
			// TODO 订单发送到第三方的日志
			paramMap.put("url", param.getUrl());
			paramMap.put("tradeIds", "tradeIds");
			paramMap.put("sendTo", "sendTo");
		} else if (LogType.SQL.equals(path)) {
			// 数据库访问日志
			paramMap.put("url", param.getUrl());
			paramMap.put("db", param.getDb());
			paramMap.put("sql", param.getSql());
		} else if (LogType.TOP.equals(path)){
			// TOP调用日志 
			paramMap.put("url", param.getUrl());
		} else if (LogType.ACCOUNT_LOGIN.equals(path)){
			// 风控登陆日志
			paramMap.put("tid", param.getTid());
			paramMap.put("loginResult", param.getLoginResult());
			paramMap.put("loginMessage", param.getLoginMessage());
		} else if (LogType.GET_VERIFY_URL.equals(path)){
			// 获取二次验证地址
			paramMap.put("sessionId", param.getSessionId());
			paramMap.put("mobile", param.getMobile());
			paramMap.put("redirectURL", param.getRedirectURL());
		}
		return paramMap;
	}
	
	public static class Param {
		
		private String ati;
		
		private String userId;
		
		private String userIp;
		
		private String topAppKey;
		
		private String appName;
		
		private String url;
		
		// 订单号列表，用英文逗号分隔，每次最多100条记录。如果超过100条，拆分成多条请求
		private String tradeIds;
		
		// 对订单的操作，比如打印订单
		private String operation;
		
		private String db;
		
		private String sql;
		
		// 和用户关联的淘宝帐号（如果没有帐号，设置可以关联到淘宝帐号的信息，如店铺名。如果关联多个帐号，用英文逗号分隔）
		private String tid;
		
		private String loginResult;
		
		// 额外信息，比如失败原因
		private String loginMessage;
		
		// 二次验证页面在跳转到redirectURL时会追加token参数
		private String token;
		
		private String sessionId;
		
		private String mobile;
		
		private String redirectURL;
		
		/**
		 * TOP调用日志
		 */
		public Param(String ati, Long userId, String userIp, String topAppKey, String appName, String url){
			this.ati = ati;
			this.userId = userId == 0L ? StringUtils.EMPTY : String.valueOf(userId);
			this.userIp = userIp;
			this.topAppKey = topAppKey;
			this.appName = appName;
			this.url = url;
		}
		
		/**
		 * 订单访问日志
		 */
		public Param(String ati, Long userId, String userIp, String topAppKey, String appName, String url, List<Long> tradeIds, String operation){
			this(ati, userId, userIp, topAppKey, appName, url);
			this.tradeIds = CommonUtils.isEmpty(tradeIds) ? "" : StringUtils.join(tradeIds, ",");
			this.operation = operation;
		}
		
		/**
		 * 数据库访问日志
		 */
		public Param(String ati, Long userId, String userIp, String topAppKey, String appName, String url, String db, String sql){
			this(ati, userId, userIp, topAppKey, appName, url);
			this.sql = sql;
			this.db = db;
		}
		
		/**
		 * 账号风控登录日志
		 */
		public Param(String ati, String userId, String userIp, String topAppKey, String appName, String tid, boolean loginResult, String loginMessage){
			this.ati = ati;
			this.userId = userId;
			this.userIp = userIp;
			this.topAppKey = topAppKey;
			this.appName = appName;
			this.tid = tid;
			this.loginResult = loginResult ? SUCCESS : FAIL;
			this.loginMessage= loginMessage;
		}
		
		/**
		 * 风险计算(computeRisk)接口
		 */
		public Param(String ati, String userId, String userIp, String topAppKey, String appName) {
			this.ati = ati;
			this.userId = userId;
			this.userIp = userIp;
			this.topAppKey = topAppKey;
			this.appName = appName;
		}
		
		/**
		 * 获取二次验证地址(getVerifyUrl)接口
		 */
		public Param(String ati, String userId, String userIp, String topAppKey, String appName, String sessionId, String mobile, String redirectURL){
			this(ati, userId, userIp, topAppKey, appName);
			this.sessionId = sessionId;
			this.mobile = mobile;
			this.redirectURL = redirectURL;
		}

		public Param(String token){
			this.token = token;
		}
		
		public String getAti() {
			return ati;
		}

		public void setAti(String ati) {
			this.ati = ati;
		}

		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public String getUserIp() {
			return userIp;
		}

		public void setUserIp(String userIp) {
			this.userIp = userIp;
		}

		public String getTopAppKey() {
			return topAppKey;
		}

		public void setTopAppKey(String topAppKey) {
			this.topAppKey = topAppKey;
		}

		public String getAppName() {
			return appName;
		}

		public void setAppName(String appName) {
			this.appName = appName;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getTradeIds() {
			return tradeIds;
		}

		public void setTradeIds(String tradeIds) {
			this.tradeIds = tradeIds;
		}

		public String getOperation() {
			return operation;
		}

		public void setOperation(String operation) {
			this.operation = operation;
		}

		public String getDb() {
			return db;
		}

		public void setDb(String db) {
			this.db = db;
		}

		public String getSql() {
			return sql;
		}

		public void setSql(String sql) {
			this.sql = sql;
		}

		public String getTid() {
			return tid;
		}

		public void setTid(String tid) {
			this.tid = tid;
		}

		public String getLoginResult() {
			return loginResult;
		}

		public void setLoginResult(String loginResult) {
			this.loginResult = loginResult;
		}

		public String getLoginMessage() {
			return loginMessage;
		}

		public void setLoginMessage(String loginMessage) {
			this.loginMessage = loginMessage;
		}

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public String getSessionId() {
			return sessionId;
		}

		public void setSessionId(String sessionId) {
			this.sessionId = sessionId;
		}

		public String getMobile() {
			return mobile;
		}

		public void setMobile(String mobile) {
			this.mobile = mobile;
		}

		public String getRedirectURL() {
			return redirectURL;
		}

		public void setRedirectURL(String redirectURL) {
			this.redirectURL = redirectURL;
		}
		
	}
	
	public static class LogType{
		
		public static final String TOP = "top";
		
		public static final String LOGIN = "login";
		
		public static final String ORDER = "order";
		
		public static final String SEND_ORDER = "sendOrder";
		
		public static final String SQL = "sql";
		
		public static final String ACCOUNT_LOGIN = "accountLogin";
		
		public static final String COMPUTE_RISK = "computeRisk";
		
		public static final String GET_VERIFY_URL = "getVerifyUrl";
		
		public static final String IS_VERIFY_PASSED = "isVerifyPassed";
		
		public static final String RESET_RISK = "resetRisk";
	}
	
	public static class LogResultInfo{
		
		private String result;
		
		private String errMsg;

		public String getResult() {
			return result;
		}
		public void setResult(String result) {
			this.result = result;
		}
		public String getErrMsg() {
			return errMsg;
		}
		public void setErrMsg(String errMsg) {
			this.errMsg = errMsg;
		}
	}
	
	public static class ResultRisk{
		
		private String result;
		
		private String errMsg;

		private double risk;
		
		private String riskType;
		
		private String riskDescription;
		
		private String verifyUrl;
		
		private String verifyResult;

		public String getResult() {
			return result;
		}

		public void setResult(String result) {
			this.result = result;
		}

		public String getErrMsg() {
			return errMsg;
		}

		public void setErrMsg(String errMsg) {
			this.errMsg = errMsg;
		}

		public double getRisk() {
			return risk;
		}

		public void setRisk(double risk) {
			this.risk = risk;
		}

		public String getRiskType() {
			return riskType;
		}

		public void setRiskType(String riskType) {
			this.riskType = riskType;
		}

		public String getRiskDescription() {
			return riskDescription;
		}

		public void setRiskDescription(String riskDescription) {
			this.riskDescription = riskDescription;
		}

		public String getVerifyUrl() {
			return verifyUrl;
		}

		public void setVerifyUrl(String verifyUrl) {
			this.verifyUrl = verifyUrl;
		}

		public String getVerifyResult() {
			return verifyResult;
		}

		public void setVerifyResult(String verifyResult) {
			this.verifyResult = verifyResult;
		}
	}
	
}
