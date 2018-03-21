package controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.carrierTask.CarrierTaskForDQ;
import models.carrierTask.ItemCarryCustom;
import models.user.User;

import org.apache.commons.lang.StringUtils;

import play.db.jpa.NoTransaction;
import play.mvc.Controller;
import result.TMResult;
import utils.DateUtil;
import utils.ToolBy1688;
import actions.carriertask.ItemCarrierForDQAction;
import actions.carriertask.ItemCarrierForDQAction.CarryParam;
import actions.carriertask.ItemCarrierForDQAction.RecommendItemCat;
import actions.carriertask.ItemCarrierForDQAction.itemCarryResult;
import actions.sms.SmsSendAction;
import bustbapi.ShopApi;
import bustbapi.ShopApi.ShopGet;

import com.alibaba.fastjson.JSON;
import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;
import com.taobao.api.domain.Shop;

import configs.TMConfigs.App;
import controllers.TMController.TMResultMsg;
import dao.UserDao;

public class ItemCarrierForDQ extends Controller {
	
	private static final String ITEM_CAT_RECOMMEND_URL = "http://115.29.162.138:9090/api/ItemCat/search";
	
	private static final String ENCODE = "UTF-8";

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * 商品复制-淘宝复制页面
	 */
	public static void TZGItemCarrier() {
		render("carrierForDQ/TZGItemCarrier.html");
	}

	/**
	 * 商品复制-1688复制页面
	 */
	public static void alibabaCarrier() {
		render("carrierForDQ/alibabaCarrier.html");
	}

	/**
	 * 商品复制-拼多多复制页面
	 */
	public static void pddCarrier() {
		render("carrierForDQ/pddCarrier.html");
	}

	/**
	 * 商品复制-任务管理页面
	 */
	public static void tasklist() {
		render("carrierForDQ/tasklist.html");
	}

	/**
	 * 商品复制-任务管理后台页面
	 */
	public static void itemFailLog() {
		render("carrierForDQ/ItemFailLog.html");
	}

	/**
	 * 商品复制-店群授权页面
	 */
	@NoTransaction
	public static void dqAuth() {
		String authUrl = App.TAOBAO_AUTH_URL;
		String redirectUrl = APIConfig.get().getRedirURL() + "/in/authDQ";

		try {
			redirectUrl = URLEncoder.encode(redirectUrl, "utf-8");
		} catch (UnsupportedEncodingException e) {
			renderError("系统出现一些异常，请联系我们！");
		}

		authUrl += "&redirect_uri=" + redirectUrl;

		redirect(authUrl);
	}
	
	/**
	 * 商品复制-查询用户sid是否有效
	 */
	@NoTransaction
	public static void checkUser(String sid) {
		if(StringUtils.isEmpty(sid)) {
			renderError("请先传入用户秘钥！！！");
		}
		
		User user = ItemCarrierForDQAction.getUserForDQ(sid);
		
		if(user == null || !user.isVaild) {
			renderError("无效的用户秘钥！！！");
		}
		
		renderSuccess("用户秘钥有效！！！", "");
	}
		
	/**
	 * 商品复制-创建复制任务
	 */
	@NoTransaction
	public static void itemCarry(String paramJson) {
		if (StringUtils.isEmpty(paramJson)) {
			renderError("入参信息为空！！！");
		}
		List<CarryParam> paramList = ItemCarrierForDQAction.parseCarrierJson(paramJson);
		if (CommonUtils.isEmpty(paramList)) {
			renderError("入参解析异常！！！json:" + paramJson);
		}
		
		List<itemCarryResult> result = new ArrayList<itemCarryResult>();
		
		for (CarryParam param : paramList) {
			String sid = param.getSid();
			Long numIid = param.getNumIid();
			int type = param.getType();
			Long cid = param.getCid();

			itemCarryResult carryResult = ItemCarrierForDQAction.doItemCarry(sid, numIid, type, cid);
			result.add(carryResult);
		}
		
		renderSuccess("", result);
	}
	
	/**
	 * 商品复制-查询复制进程
	 */
	@NoTransaction
	public static void checkItemCarry(Long taskId) {
		if (taskId == null || taskId <= 0) {
			renderError("入参信息为空！！！");
		}
		
		CarrierTaskForDQ exist = CarrierTaskForDQ.findById(taskId);
		if(exist == null) {
			renderError("任务【" + taskId + "】不存在，请检查taskId是否正确！！！");
		}
		
		renderSuccess("", exist);
	}
	
	/**
	 * 商品复制-保存自定义配置模板
	 */
	@NoTransaction
	public static void saveItemCarryCustom(String paramJson) {
		if (StringUtils.isEmpty(paramJson)) {
			renderError("入参信息为空！！！");
		}
		
		ItemCarryCustom itemCarryCustom = ItemCarrierForDQAction.parseItemCarryCustom(paramJson);
		if (itemCarryCustom == null) {
			renderError("入参解析异常！！！json:" + paramJson);
		}

		// 是否存在相同的模板
		Long id = itemCarryCustom.findId();
		Boolean success = false;
		if (id > 0) {
			itemCarryCustom.setId(id);
			itemCarryCustom.setCreateTs(System.currentTimeMillis());
			success = itemCarryCustom.rawUpdate();
		} else {
			success = itemCarryCustom.rawInsert();
		}
		
		if(!success) {
			renderError("保存失败");
		}
		
		renderSuccess("保存成功", null);
	}
	
	/**
	 * 商品复制-类目推荐
	 */
	@NoTransaction
	public static void itemCatRecommend(Long numIid, String sid) {
		if (numIid == null || numIid <= 0) {
			renderError("请先传入宝贝id！！！");
		}
		
		User user = ItemCarrierForDQAction.getUserForDQ(sid);
		
		if(user == null || !user.isVaild) {
			renderError("无效的用户秘钥！！！");
		}
		
		String itemUrl = "https://m.1688.com/offer/" + numIid + ".html";
		ToolBy1688 tool = new ToolBy1688(itemUrl);
		if (tool.getItemInfo() == null) {
			renderError("1688商品数据获取异常！！！");
		}
		
		String title = tool.getPageTitle();
		if(StringUtils.isEmpty(title)) {
			renderError("1688商品keywords获取异常！！！");
		}

		// 参数拼接
		StringBuffer param = new StringBuffer();
		param.append("itemTitle=").append(title);
		
		String resultStr = SmsSendAction.sendPost(ITEM_CAT_RECOMMEND_URL, param.toString(), ENCODE);
		if(!StringUtils.isEmpty(resultStr)) {
			resultStr = resultStr.trim();
		}
		
		List<RecommendItemCat> result = ItemCarrierForDQAction.parseItemCatJson(resultStr);
		if (CommonUtils.isEmpty(result)) {
			renderError("解析异常！！！json:" + resultStr);
		}
		
		renderSuccess("", result);
	}
	
	/**
	 * 商品复制-查询任务列表
	 */
	public static void failTaskList(String startTime, String endTime, int pn, String msg, Long taskId, Long userId, int taskStatus, Integer subTaskType, Long numiid, Long cid) {
		PageOffset po = new PageOffset(pn, 10);
		Long startTs;
		Long endTs;
		try {
			startTs = sdf.parse(startTime).getTime();
		} catch (ParseException e) {
			startTs = System.currentTimeMillis() - DateUtil.DAY_MILLIS * 3;
		}
		try {
			endTs = sdf.parse(endTime).getTime() + DateUtil.DAY_MILLIS - 1;
		} catch (ParseException e) {
			endTs = new Date().getTime();
		}

		List<CarrierTaskForDQ> bySearchRules = CarrierTaskForDQ.findBySearchRules(taskId, numiid, subTaskType, cid, taskStatus, msg, startTs, endTs, userId, po);
		int count = CarrierTaskForDQ.countBySearchRules(taskId, numiid, subTaskType, cid, taskStatus, msg, startTs, endTs, userId);

		renderJSON(JSON.toJSONString(new TMResult(bySearchRules, count, po)));
	}

	/**
	 * 商品复制-任务重启
	 */
	public static void rebootById(String... ids) {
		for (String id: ids) {
			CarrierTaskForDQ.resetTask(Long.valueOf(id), "");
		}
		renderSuccess("重启成功", null);
	}
	
	/**
	 * 商品复制-日志删除
	 */
	public static void delById(String... ids) {
		for (String id: ids) {
			CarrierTaskForDQ.delTask(Long.valueOf(id), "");
		}
		renderSuccess("删除成功", null);
	}
	
	/**
	 * 商品复制-获取店铺信息
	 */
	public static void getShopInfo(String ww) {
		if(StringUtils.isEmpty(ww)) {
			renderError("请先输入旺旺");
		}
		
		ShopGet shopGet = new ShopApi.ShopGet(ww);
		Shop shop = shopGet.call();
		if(shop == null) {
			renderError(shopGet.getSubErrorMsg());
		}
		
		renderSuccess("", shop);
	}

	private static void renderError(String msg) {
		TMResultMsg wmMsg = new TMResultMsg();
		wmMsg.setSuccess(false);
		wmMsg.setMessage(msg);
		renderJSON(JsonUtil.getJson(wmMsg));
	}
	
	private static void renderSuccess(String msg, Object res) {
		TMResultMsg wmMsg = new TMResultMsg();
		wmMsg.setSuccess(true);
		wmMsg.setMessage(msg);
		wmMsg.setRes(res);
		renderJSON(JsonUtil.getJson(wmMsg));
	}

}
