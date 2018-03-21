package job.dazhe;
import java.util.ArrayList;
import java.util.List;

import models.promotion.SalesTitlePlay;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import play.jobs.On;
import actions.sms.SmsSendLaiqt;

import com.ciaosir.client.CommonUtils;

import configs.TMConfigs.Server;
import controllers.APIConfig;

@On("0 0 10 * * ?")
public class GetSalesTitleJob extends Job {
	
	private static final int APP = 21255586;

	private static final Logger log = LoggerFactory.getLogger(GetSalesTitleJob.class);
	
	/** 
	 * 自定义签名营销短信
	 * */
	private static final String SALES_TITLE_URL = "https://huodong.m.taobao.com/api/data/v2/5fe5e737d3314fa2973297f86f7bff3a.js";

	public void doJob() {
		if (!Server.jobTimerEnable) {
			return;
		}
		// 淘掌柜工具
		if (APIConfig.get().getApp() != APP) {
			return;
		}
		log.info("GetShopScoreJob launch!!");
		
		doGetSalesTitleJob();
	}
	
	private void doGetSalesTitleJob() {
		String result = SmsSendLaiqt.sendPost(SALES_TITLE_URL, StringUtils.EMPTY);
		if(StringUtils.isEmpty(result)) {
			log.error("【" + SALES_TITLE_URL + "】返回为空！！！");
			return;
		}
		if(result.startsWith("KW(")) {
			result = result.substring(3);
		}
		if(result.endsWith(")")) {
			result = result.substring(0, result.length() - 1);
		}
		List<SalesTitlePlay> resultList = parseResult(result);
		if(CommonUtils.isEmpty(resultList)) {
			log.error("【解析错误】 result: " + result);
			return;
		}
		boolean isSuccess = SalesTitlePlay.deleteAllWords();
		if(!isSuccess) {
			log.error("【删除数据失败】 Table: " + SalesTitlePlay.TABLE_NAME);
			return;
		}
		boolean success = SalesTitlePlay.batchInsert(resultList);
		if(!success) {
			log.error("【数据库批量插入异常】 result: " + result);
			return;
		}
		log.info("营销签名更新成功！！！");
	}
	
	private static List<SalesTitlePlay> parseResult(String jsonString) {
		List<SalesTitlePlay> result = new ArrayList<SalesTitlePlay>();
		try {
			JSONObject jsonObject = new JSONObject(jsonString);
			JSONArray jsonArray = jsonObject.getJSONArray("keywords");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				String clazz = object.getString("clazz");
				String words = object.getString("words");
				if(StringUtils.isEmpty(clazz) || StringUtils.isEmpty(words)) {
					continue;
				}
				SalesTitlePlay title = new SalesTitlePlay(clazz, words);
				result.add(title);
			}
			return result;
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return result;
	}
	
}
