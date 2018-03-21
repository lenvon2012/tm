package actions.sms;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actions.sms.SmsSendLaiqt.SmsResult;

import com.ciaosir.client.utils.DateUtil;

/**
 * LinagY  发送短信类
 */
public class SmsSendLiangY {
	
	private static final Logger log = LoggerFactory.getLogger(SmsSendLiangY.class);
	
	private static final String APIKEY = "7b6e10993af8ad4776ba313043a25abc";
	
	/** 
	 * 加群短信发送链接 
	 * */
	private static final String JIAQUN_URL_SIGN = "http://223.4.49.248:30003/v1/sms/sendyx";
	
	/** 
	 * 加群短信余额查询链接 
	 * */
	private static final String JIAQUN_URL_BALANCE_SIGN = "http://223.4.49.248:30003/v1/sms/userinfo";
	
	/** 
	 * 加群短信状态报告获取链接 
	 * */
	private static final String JIAQUN_URL_REPORT_SIGN = "http://223.4.49.248:30003/v1/sms/reports";
	
	/** 
	 * 加群短信审核获取链接 
	 * */
	private static final String JIAQUN_URL_VERIFY_SIGN = "http://223.4.49.248:30003/v1/verify/get";
	
	private static final String ENCODE = "UTF-8";
	
	public enum resultType {
		user,	// 用户信息
		send,	// 发送短信
		report,	// 状态报告, 审核报告
	}

	/**
	 * 加群	短信发送接口  
	 */
	public static SmsResult jiaQunSmsSend(String phoneNumber, String smsContent, String userShopName) {
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
		// 该通道必须加退订回T
		if(!smsContent.endsWith("退订回T")){
			smsContent += " 退订回T";
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
		param.append("apikey=").append(APIKEY);
		param.append("&mobile=").append(phoneNumber);
		param.append("&content=").append(contentUrlEncode);
		
		// 发送短信
		String resultStr = SmsSendAction.sendPost(JIAQUN_URL_SIGN, param.toString(), ENCODE);
		if(!StringUtils.isEmpty(resultStr)) {
			resultStr = resultStr.trim();
		}
		JiaQunResult result = parseResult(resultStr, resultType.send);
		if(result.getCode() == 1) {
			return new SmsResult(true, 1, 0, 0, 0L, StringUtils.EMPTY);
		}
		return new SmsResult(false, 0, 1, 0, 0L, StringUtils.EMPTY);
	}
	
	/**
	 * 加群	用户信息查询
	 */
	public static JiaQunResult jiaQunUserInfo(){
		// 参数拼接
		StringBuffer param = new StringBuffer();
		param.append("apikey=").append(APIKEY);
		String resultStr = SmsSendAction.sendPost(JIAQUN_URL_BALANCE_SIGN, param.toString(), ENCODE);
		
		if(!StringUtils.isEmpty(resultStr)) {
			resultStr = resultStr.trim();
		}
		
		JiaQunResult result = parseResult(resultStr, resultType.user);
		
		return result;
	}
	
	/**
	 * 加群	短信审核状态获取
	 */
	public static JiaQunResult jiaQunSmsVerify(Long startTs, Long endTs){
		if(endTs == null || endTs <= 0L) {
			endTs = System.currentTimeMillis();
		}
		if(startTs == null || startTs <= 0L || startTs > endTs) {
			startTs = endTs - DateUtil.ONE_MINUTE_MILLIS;
		}
		// 参数拼接
		StringBuffer param = new StringBuffer();
		param.append("apikey=").append(APIKEY);
		param.append("&start_ts=").append(startTs);
		param.append("&end_ts=").append(endTs);
		//param.append("ser_no=").append(ser_no);
		String resultStr = SmsSendAction.sendPost(JIAQUN_URL_VERIFY_SIGN, param.toString(), ENCODE);
		
		if(!StringUtils.isEmpty(resultStr)) {
			resultStr = resultStr.trim();
		}
		
		JiaQunResult result = parseResult(resultStr, resultType.report);
		
		return result;
	}
	
	/**
	 * 加群	短信状态报告获取
	 */
	public static JiaQunResult jiaQunSmsReport(){
		// 参数拼接
		StringBuffer param = new StringBuffer();
		param.append("apikey=").append(APIKEY);
		String resultStr = SmsSendAction.sendPost(JIAQUN_URL_REPORT_SIGN, param.toString(), ENCODE);
		
		if(!StringUtils.isEmpty(resultStr)) {
			resultStr = resultStr.trim();
		}
		
		JiaQunResult result = parseResult(resultStr, resultType.report);
		
		return result;
	}
	
	public static JiaQunResult parseResult(String resultStr, resultType type){
		
		if(StringUtils.isEmpty(resultStr)) {
			return null;
		}
		
		try {
			JSONObject jsonObject = new JSONObject(resultStr);
			int code = jsonObject.getInt("code");
			String msg = jsonObject.getString("msg");
			
			switch (type) {
				case user:
					JiaQunResult<JiaQunUserInfo> userResult = new JiaQunResult<JiaQunUserInfo>();
					
					userResult.setCode(code);
					userResult.setMsg(msg);
					
					if(code != 1) {
						return userResult;
					}
					
					JSONObject infoObject = jsonObject.getJSONObject("result");
					String account = infoObject.getString("account");
					int warnSmsCount = infoObject.getInt("warnSmsCount");
					String whiteIps = infoObject.getString("whiteIps");
					String balance = infoObject.getString("balance");
					
					JiaQunUserInfo info = new JiaQunUserInfo(account, warnSmsCount, whiteIps, balance);
					
					userResult.setResult(info);
					
					return userResult;
				case send:
					JiaQunResult<String> sendResult = new JiaQunResult<String>();
					
					sendResult.setCode(code);
					sendResult.setMsg(msg);
					
					if(code != 1) {
						return sendResult;
					}
					
					String result = jsonObject.getString("result");
					
					sendResult.setResult(result);
					
					return sendResult;
				case report:
					JiaQunResult<List<JiaQunReport>> reportResult = new JiaQunResult<List<JiaQunReport>>();
					
					reportResult.setCode(code);
					reportResult.setMsg(msg);
					
					if(code != 1) {
						return reportResult;
					}
					
					JSONArray reportArray = jsonObject.getJSONArray("result");
					List<JiaQunReport> reportList = new ArrayList();
					
					for (int i = 0; i < reportArray.length(); i++) {
						JSONObject reportObject = reportArray.getJSONObject(i);
						String mobile = reportObject.has("mobile") ? reportObject.getString("mobile") : StringUtils.EMPTY;
						String serNo = reportObject.has("serNo") ? reportObject.getString("serNo") : StringUtils.EMPTY;
						String status = reportObject.has("status") ? reportObject.getString("status") : StringUtils.EMPTY;
						Long sendTs = reportObject.has("sendTs") ? reportObject.getLong("sendTs") : -1L;
						Long reportTs = reportObject.has("reportTs") ? reportObject.getLong("reportTs") : -1L;
						String desc = reportObject.has("descr") ? reportObject.getString("descr") : StringUtils.EMPTY;
						
						JiaQunReport report = new JiaQunReport(mobile, serNo, status, sendTs, reportTs, desc);
						reportList.add(report);
					}
					
					reportResult.setResult(reportList);
					
					return reportResult;
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	/**
	 * 加群	返回结果
	 */
	public static class JiaQunResult<T> {
		
		// 返回结果，code为1，表示成功，其他请参考返回值说明
		int code;
		
		// 返回结果说明
		String msg;
		
		T result;

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

		public T getResult() {
			return result;
		}

		public void setResult(T result) {
			this.result = result;
		}
		
	}
	
	/**
	 * 加群	用户信息
	 */
	public static class JiaQunUserInfo {
		
		// 账户名
		String account;
		
		// 预警短信号码数
		int warnSmsCount;

		// 账号白名单号码
		String whiteIps;
		
		// 账户短信余额
		String balance;

		public String getAccount() {
			return account;
		}

		public void setAccount(String account) {
			this.account = account;
		}

		public int getWarnSmsCount() {
			return warnSmsCount;
		}

		public void setWarnSmsCount(int warnSmsCount) {
			this.warnSmsCount = warnSmsCount;
		}

		public String getWhiteIps() {
			return whiteIps;
		}

		public void setWhiteIps(String whiteIps) {
			this.whiteIps = whiteIps;
		}

		public String getBalance() {
			return balance;
		}

		public void setBalance(String balance) {
			this.balance = balance;
		}

		public JiaQunUserInfo(String account, int warnSmsCount,
				String whiteIps, String balance) {
			super();
			this.account = account;
			this.warnSmsCount = warnSmsCount;
			this.whiteIps = whiteIps;
			this.balance = balance;
		}
		
	}
	
	/**
	 * 加群	状态报告
	 */
	public static class JiaQunReport {
		
		// 接收短信的手机号码
		String mobile;
		
		// 短信批次号
		String serNo;

		// 短信状态
		String status;
		
		// 短信发送时间
		Long sendTs;
		
		// 短信反馈时间
		Long reportTs;

		// 审核报告： 审核未通过，则返回描述
		String desc;
		
		public String getMobile() {
			return mobile;
		}

		public void setMobile(String mobile) {
			this.mobile = mobile;
		}

		public String getSerNo() {
			return serNo;
		}

		public void setSerNo(String serNo) {
			this.serNo = serNo;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public Long getSendTs() {
			return sendTs;
		}

		public void setSendTs(Long sendTs) {
			this.sendTs = sendTs;
		}

		public Long getReportTs() {
			return reportTs;
		}

		public void setReportTs(Long reportTs) {
			this.reportTs = reportTs;
		}

		public String getDesc() {
			return desc;
		}

		public void setDesc(String desc) {
			this.desc = desc;
		}

		public JiaQunReport(String mobile, String serNo, String status,
				Long sendTs, Long reportTs, String desc) {
			super();
			this.mobile = mobile;
			this.serNo = serNo;
			this.status = status;
			this.sendTs = sendTs;
			this.reportTs = reportTs;
			this.desc = desc;
		}
		
	}
	
	public static void main(String[] args) {
//		jiaQunSmsSend("13656676326", "【爱上惠】尊敬的淘达人，您的购物信誉良好，特邀您加入微信秒杀群，加微信号：805287298抢福利，各大品牌一折秒杀。 退订回T");
//		jiaQunSmsSend("13656676326", "【爱淘】亲爱的，您购物信誉良好，加达人微信：xk5842，进内部群，名额有限，内部优惠券，免单商品，打牌秒杀天天有！ 退订回T");
//		jiaQunSmsSend("13656676326", "【咕咕韩式炸鸡】美团现在有17元代金券,限期7天40-30,限美团紫金港下单用户,微信:kefu939。 退订回T");
//		jiaQunSmsSend("13656676326", "【天天魔盒】微信客户群4月20号晚上9点1100元现金奖励，限美团4月14号到4月20号下单用户，微信kefu939。 退订回T");
//		jiaQunSmsSend("13656676326", "【clorest510】店铺又上新啦，加微信群：kefu939，立享优惠价格。 退订回T");
//		jiaQunSmsSend("18814887685", "【量聚花蜜试用】亲爱的商家，现平台推出优化关键词排名功能，商家可登陆花蜜试用后台进行报名了解活动详情，拒绝淡季，轻松获取免费流量！ 退订回T");
//		jiaQunSmsSend("18814887685", "佰利山马汀: 亲，我们发现疑似差评用户yg2047的订单，未关闭成功，请及时登录淘宝查看。 退订回T", "佰赫山工艺品");
		jiaQunSmsSend("18814887685", "clorest510: 亲，我们成功为您关闭了疑似茶评师wangwei的订单，如有问题请及时登录淘宝查看。 退订回T", "小猪芭啦啦");
		jiaQunSmsSend("18814887685", "clorest510: 亲，我们发现疑似茶评用户wangwei的订单，未关闭成功，请及时登录淘宝查看。 退订回T", "小猪芭啦啦");
		jiaQunSmsSend("18814887685", "clorest510: 亲，我们成功为您关闭了wangwei的订单，如有问题请及时登录淘宝查看。 退订回T", "小猪芭啦啦");
		jiaQunSmsSend("18814887685", "clorest510: 亲，我们发现用户wangwei的订单，未关闭成功，请及时登录淘宝查看。 退订回T", "小猪芭啦啦");
//		jiaQunUserInfo();
		//jiaQunSmsVerify();
	}
}
