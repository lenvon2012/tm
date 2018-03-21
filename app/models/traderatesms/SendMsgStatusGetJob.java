package models.traderatesms;

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
 * 获取发送短信状态报告
 */
@Every("60s")
public class SendMsgStatusGetJob extends Job {
	
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
		doSendMsgStatusGetJob();
	}

	private Boolean doSendMsgStatusGetJob() {
		String resultStr = SmsSendLaiqt.sendMsgStatusGet(NUM);
		
		if(StringUtils.isEmpty(resultStr)) {
			log.error("状态报告获取失败！");
			return false;
		}
		if(resultStr.indexOf("isSubmited") >= 0) {
//			if(resultStr.indexOf("-3018") >= 0) {
//				log.error("一次性获取状态报表数量超限！");
//			} else if (resultStr.indexOf("-3019") >= 0) {
//				log.error("当前暂无可以获取的状态报告！");
//			}
			return false;
		}
		
		List<RateSmsSend> list = parseResult(resultStr);
		if(CommonUtils.isEmpty(list)) {
			log.error("状态报告解析失败！返回字符串：" + resultStr);
			return false;
		}
		for (RateSmsSend result : list) {
			RateSmsSendLog exist = RateSmsSendLog.findByBatchIdAndNumber(result.getBatchId(), result.getPhone());
			if(exist == null) {
				log.error("评价短信发送日志中未匹配到相关记录！BatchId:" + result.getBatchId() + ",Phone:" + result.getPhone());
				continue;
			}
			exist.setStatus(result.getStatus());
			boolean isSuccess = exist.jdbcSave();
			if(!isSuccess) {
				log.error("数据库更新失败！ID：" + exist.getId());
				continue;
			}
		}
		return true;
	}
	
	public static List<RateSmsSend> parseResult(String jsonString) {
		List<RateSmsSend> result = new ArrayList<RateSmsSend>();
		try {
			RateSmsSend rateSms = null;
			JSONArray jsonArray = new JSONArray(jsonString);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				Long batchId = jsonObject.getLong("batchId");
				String phone = jsonObject.getString("phone");
				String status = jsonObject.getString("errorcode");
				rateSms = new RateSmsSend(batchId, phone, status);
				result.add(rateSms);
			}
			return result;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return result;
	}
	
	public static class RateSmsSend {

		/**
		 * 批次ID
		 */
		private Long batchId;
		
		/**
		 * 手机号码
		 */
		private String phone;
		
		/**
		 * 状态报告
		 */
		private String status;
		
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

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public RateSmsSend() {
			
		}
		
		public RateSmsSend(Long batchId, String phone, String status) {
			this.batchId = batchId;
			this.phone = phone;
			this.status = status;
		}

	}
	
}
