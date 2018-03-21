package controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import models.carrierTask.CarrierTaskForXXX;
import models.carrierTask.CarrierTaskForXXX.CarrierTaskForXXXStatus;
import models.carrierTask.ItemImgForXXX;
import models.carrierTask.ItemImgForXXX.batchOpType;
import models.carrierTask.SubCarrierTaskForXXX;
import models.carrierTask.SubCarrierTaskForXXX.SubCarrierTaskForXXXStatus;
import models.user.User;

import org.apache.commons.lang.StringUtils;

import play.db.jpa.Transactional;
import play.mvc.Controller;
import actions.carriertask.ItemCarrierForXXXAction;
import actions.carriertask.ItemCarrierForDQAction.itemCarryResult;
import actions.carriertask.ItemCarrierForXXXAction.CarryItem;
import actions.carriertask.ItemCarrierForXXXAction.CarryParam;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.JsonUtil;

import configs.TMConfigs;
import configs.TMConfigs.App;
import controllers.TMController.TMResultMsg;
import dao.UserDao;

public class ItemCarrierForXXX extends Controller {
	
	/**
	 * 商品复制-店铺授权
	 */
	public static void authDQ() {
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
	 * 商品复制-创建复制任务
	 */
	@Transactional
	public static void itemCarryPrepare(String paramJson) {
		if (StringUtils.isEmpty(paramJson)) {
			renderError("入参信息为空！！！");
		}
		CarryParam param = ItemCarrierForXXXAction.parseCarryJson(paramJson);
		if (param == null) {
			renderError("入参解析异常！！！json:" + paramJson);
		}
		
		String sid = param.getSid();
		User user = UserDao.findBySessionKey(sid);
		if(user == null) {
			renderError("用户不存在！！！");
		}
		boolean isValid = UserDao.doValid(user);
		if(!isValid) {
			renderError("用户已过期，请重新授权或续订软件！");
		}
		
		List<CarryItem> items = param.getItems();
		
		CarrierTaskForXXX task = new CarrierTaskForXXX(items.size(), param.getType(), CarrierTaskForXXXStatus.CREATED, param.getWw(), user.getUserNick(), user.getId());
		boolean success = task.jdbcSave();
		if(!success) {
			renderError("主任务添加失败！！！");
		}
		
		List<SubCarrierTaskForXXX> subTasks = new ArrayList<SubCarrierTaskForXXX>();
		Long now = System.currentTimeMillis();
		for (CarryItem item : items) {
			SubCarrierTaskForXXX subTask = new SubCarrierTaskForXXX(task.getId(), item, SubCarrierTaskForXXXStatus.PREPARING, now, task.getUserId());
			subTasks.add(subTask);
		}
		boolean batchInsert = SubCarrierTaskForXXX.batchInsert(subTasks);
		if(!batchInsert) {
			renderError("子任务添加失败！！！");
		}
		
		ItemCarrierForXXXAction.doItemPrePare(task.getId());
		
		renderSuccess("", task.getId());
	}
	
	/**
	 * 商品复制-图片回传 开始复制
	 */
	@Transactional
	public static void itemCarryStart(String imgsJson) {
		if (StringUtils.isEmpty(imgsJson)) {
			renderError("入参信息为空！！！");
		}
		List<ItemImgForXXX> imgs = ItemCarrierForXXXAction.parseImgsJson(imgsJson);
		if (CommonUtils.isEmpty(imgs)) {
			renderError("入参解析异常！！！json:" + imgsJson);
		}
		
		Long subTaskId = imgs.get(0).getSubTaskId();
		final SubCarrierTaskForXXX subTask = SubCarrierTaskForXXX.findById(subTaskId);
		if(subTask == null) {
			renderError("子任务【:" + subTaskId + "】不存在！！！json:" + imgsJson);
		}
		
		List<ItemImgForXXX> existImgs = ItemImgForXXX.findBySubTaskId(subTaskId);
		if(imgs.size() != existImgs.size()) {
			renderError("子任务【" + subTaskId + "】共有图片" + existImgs.size() + "张，回传总计" + imgs.size() + "张，数量不匹配！！！");
		}
		
		boolean batchOp = ItemImgForXXX.batchOp(imgs, batchOpType.UPDATE);
		if(!batchOp) {
			renderError("数据库异常，图片批量更新失败!!!");
		}
		
		TMConfigs.getCarrierTaskForXXXPool().submit(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				Boolean success = ItemCarrierForXXXAction.ItemCarry(subTask.getId());
				CarrierTaskForXXX.addOneFinishCount(subTask.getTaskId(), success);
				return null;
			}
		});
		
		renderSuccess("任务【" + subTask.getId() + "】已开始复制", "");
	}
	
	/**
	 * 商品复制-获取用户所有主任务
	 */
	@Transactional
	public static void getAllTask(String sids) {
		if(StringUtils.isEmpty(sids)) {
			renderError("入参异常，sid为空！！！");
		}
		
		List<CarrierTaskForXXX> result = new ArrayList<CarrierTaskForXXX>();
		
		String[] sidArr = sids.split(",");
		for (int i = 0, l = sidArr.length; i < l; i++) {
			String sid = sidArr[i];
			User user = UserDao.findBySessionKey(sid);
			if(user == null) {
				continue;
			}
			
			List<CarrierTaskForXXX> tasks = CarrierTaskForXXX.findByUserId(user.getId());
			if(CommonUtils.isEmpty(tasks)) {
				continue;
			}
			result.addAll(tasks);
		}
		
		renderSuccess("", result);
	}
	
	/**
	 * 商品复制-获取主任务下的所有子任务
	 */
	@Transactional
	public static void getAllSubTask(Long taskId) {
		if(taskId == null || taskId <= 0) {
			renderError("入参信息为空！！！");
		}
		
		List<SubCarrierTaskForXXX> subTasks = SubCarrierTaskForXXX.findByTaskId(taskId);
		if(CommonUtils.isEmpty(subTasks)) {
			renderError("任务【" + taskId + "】下暂无相关子任务！！！");
		}
		
		renderSuccess("", subTasks);
	}
	
	/**
	 * 商品复制-获取任务商品图片信息
	 */
	@Transactional
	public static void getItemImg(Long subTaskId) {
		if (subTaskId == null || subTaskId <= 0) {
			renderError("subTaskId为空！！！");
		}
		
		SubCarrierTaskForXXX exist = SubCarrierTaskForXXX.findById(subTaskId);
		if(exist == null) {
			renderError("子任务【" + subTaskId + "】不存在，请检查subTaskId是否正确！！！");
		}
		if(SubCarrierTaskForXXXStatus.PREPARING == exist.getStatus()) {
			renderError("任务【" + subTaskId + "】图片尚未准备就绪！！！");
		}
		
		List<ItemImgForXXX> imgs = ItemImgForXXX.findBySubTaskId(subTaskId);
		if (CommonUtils.isEmpty(imgs)) {
			renderError("图片信息异常！！！");
		}
		
		renderSuccess("", imgs);
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
