package models.traderatesms;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import actions.sms.SmsSendLaiqt;

import com.ciaosir.client.CommonUtils;

import configs.TMConfigs.Server;
import controllers.APIConfig;

/**
 * 获取回复短信
 */
@Every("60s")
public class ReceiveMsgGetJob extends Job {
	
	private static final Logger log = LoggerFactory.getLogger(RateSmsReceiveLog.class);
	
	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private static final int APP = 21404171;
	
	private static final int NUM = 10;
	
	@Override
	public void doJob() throws Exception {
		if (!Server.jobTimerEnable) {
			return;
		}
		if (APIConfig.get().getApp() != APP) {
			return;
		}
		doReceiveMsgGetJob();
	}

	private Boolean doReceiveMsgGetJob() {
		String resultStr = SmsSendLaiqt.receiveMsgGet(NUM);
		
		if(StringUtils.isEmpty(resultStr)) {
			log.error("短信上行获取失败！");
			return false;
		}
		if(resultStr.indexOf("isSubmited") >= 0) {
//			if(resultStr.indexOf("-3020") >= 0) {
//				log.error("一次性获取上行数量超限！");
//			} else if (resultStr.indexOf("-3021") >= 0) {
//				log.error("当前暂无可以获取的上行！");
//			}
			return false;
		}
		
		List<RateSmsReceive> list = parseResult(resultStr);
		if(CommonUtils.isEmpty(list)) {
			log.error("短信上行解析失败！返回字符串：" + resultStr);
			return false;
		}
		for (RateSmsReceive result : list) {
			RateSmsSendLog exist = RateSmsSendLog.findByBatchIdAndNumber(result.getBatchId(), result.getPhone());
			if(exist == null) {
				log.error("评价短信发送日志中未匹配到相关记录！BatchId:" + result.getBatchId() + ",Phone:" + result.getPhone());
				continue;
			}
			Long receiveTs = 0L;
			try {
				receiveTs = sdf.parse(result.getReceivetime()).getTime();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			RateSmsReceiveLog rateSmsReceiveLog = new RateSmsReceiveLog(exist.getOid(), result.getPhone(), result.getMessage(), receiveTs, result.batchId, false, exist.getUserId());
			boolean isSuccess = rateSmsReceiveLog.jdbcSave();
			if(!isSuccess) {
				log.error("上行短信日志新增失败！Oid:" + exist.getOid() + ",BatchId:" + result.getBatchId() + ",Phone:" + result.getPhone());
				continue;
			}
		}
		return true;
	}
	
	public static List<RateSmsReceive> parseResult(String jsonString) {
		List<RateSmsReceive> result = new ArrayList<RateSmsReceive>();
		try {
			RateSmsReceive rateSms = null;
			JSONArray jsonArray = new JSONArray(jsonString);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				Long batchId = jsonObject.getLong("batchId");
				String phone = jsonObject.getString("phone");
				String message = jsonObject.getString("message");
				String receivetime = jsonObject.getString("receivetime");
				rateSms = new RateSmsReceive(batchId, phone, message, receivetime);
				result.add(rateSms);
			}
			return result;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return result;
	}
	
	public static class RateSmsReceive {

		/**
		 * 批次ID
		 */
		private Long batchId;
		
		/**
		 * 手机号码
		 */
		private String phone;
		
		/**
		 * 回复的短信内容
		 */
		private String message;
		
		/**
		 * 短信真实接收时间
		 */
		private String receivetime;

		public Long getBatchId() {
			return batchId;
		}

		public void setBatchId(Long batchId) {
			this.batchId = batchId;
		}

		public String getPhone() {
			return phone;
		}

		public void setPhone(String phone) {
			this.phone = phone;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getReceivetime() {
			return receivetime;
		}

		public void setReceivetime(String receivetime) {
			this.receivetime = receivetime;
		}
		
		public RateSmsReceive() {
		}
		
		public RateSmsReceive(Long batchId, String phone, String message, String receivetime) {
			this.batchId = batchId;
			this.phone = phone;
			this.message = message;
			this.receivetime = receivetime;
		}
		
	}
	
}
