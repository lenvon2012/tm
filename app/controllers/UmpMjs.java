package controllers;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

import job.ump.UmpMjsTmplUpdateJob;
import models.promotion.TMProActivity;
import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;

import play.Play;
import tbapi.ump.UMPApi;
import tbapi.ump.UMPApi.MjsParams;
import tbapi.ump.UMPApi.UmpMjsActivityAdd;
import actions.RelationAction;
import actions.ump.PromotionResult;
import actions.ump.UmpMjsAction;
import bustbapi.ItemApi;
import bustbapi.ItemApi.ItemUpdate;
import bustbapi.PictureApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.JsonUtil;
import com.taobao.api.domain.Picture;

import dao.UserDao;
import dao.item.ItemDao;

public class UmpMjs extends TMController {

	private static final Logger log = LoggerFactory
			.getLogger(UmpPromotion.class);
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static void addMjsItems(Long tmActivityId, String numIids) {
		
		User user = getUser();
		log.info("add mjs Activity " + tmActivityId +" for user = " + user.getUserNick()
				+" with numIids = " + numIids);
		// 先检查w2权限
		UmpPromotion.checkW2Expires(user);

		if (StringUtils.isEmpty(numIids)) {
			renderError("请先选择要加入满就送活动的宝贝！");
		}
		Set<Long> numIidSet = UmpPromotion.parseIdsToSet(numIids);

		if (CommonUtils.isEmpty(numIidSet)) {
			renderError("请先选择要加入满就送活动的宝贝！");
		}
		TMProActivity tmActivity = checkMjsActivity(tmActivityId);

		PromotionResult promotionRes = UmpMjsAction.doAddMsjItems(user,
				tmActivity, numIidSet);

		if (promotionRes.isSuccess() == false) {
			renderError(promotionRes.getMessage());
		}

		renderResultJson(promotionRes);
	}

	public static void deleteMjsItems(Long tmActivityId, String numIids) {
		
		User user = getUser();
		log.info("delete mjs Activity " + tmActivityId +" for user = " + user.getUserNick()
				+" with numIids = " + numIids);
		// 先检查w2权限
		UmpPromotion.checkW2Expires(user);

		if (StringUtils.isEmpty(numIids)) {
			renderError("请先选择要取消满就送活动的宝贝！");
		}
		Set<Long> numIidSet = UmpPromotion.parseIdsToSet(numIids);

		if (CommonUtils.isEmpty(numIidSet)) {
			renderError("请先选择要取消满就送活动的宝贝！");
		}
		TMProActivity tmActivity = checkMjsActivity(tmActivityId);

		PromotionResult promotionRes = UmpMjsAction.deleteSomeMsjItems(user,
				tmActivity, numIidSet);

		if (promotionRes.isSuccess() == false) {
			renderError(promotionRes.getMessage());
		}

		renderResultJson(promotionRes);
	}

	public static void updateMjsItems(Long tmActivityId, String numIids) {
		User user = getUser();
		log.info("update mjs Activity " + tmActivityId +" for user = " + user.getUserNick()
				+" with numIids = " + numIids);
		// 先检查w2权限
		UmpPromotion.checkW2Expires(user,
				"/taodiscount/mjsItemSelect?activityId=" + tmActivityId);

		if (StringUtils.isEmpty(numIids)) {
			renderError("请先选择要取消满就送活动的宝贝！");
		}
		Set<Long> numIidSet = UmpPromotion.parseIdsToSet(numIids);

		if (CommonUtils.isEmpty(numIidSet)) {
			renderError("请先选择要取消满就送活动的宝贝！");
		}
		TMProActivity tmActivity = checkMjsActivity(tmActivityId);

		String itemsInPromotion = tmActivity.getItems();

		// 纯是加宝贝
		if (StringUtils.isEmpty(itemsInPromotion)
				|| CommonUtils.isEmpty(UmpPromotion
						.parseIdsToSet(itemsInPromotion))) {
			PromotionResult promotionRes = UmpMjsAction.doAddMsjItems(user,
					tmActivity, numIidSet);

			if (promotionRes.isSuccess() == false) {
				renderError(promotionRes.getMessage());
			}

			tmActivity.setStatus(TMProActivity.ActivityStatus.ACTIVE);
			for (Long numIid : numIidSet) {
				tmActivity.addMjsItemNumIid(numIid);
			}

			tmActivity.jdbcSave();
			renderResultJson(promotionRes);
		}

		// 否则需要先删一些宝贝
		Set<Long> inProNumIidSet = UmpPromotion.parseIdsToSet(itemsInPromotion);

		if (!CommonUtils.isEmpty(inProNumIidSet)) {
			PromotionResult promotionRes = UmpMjsAction.deleteSomeMsjItems(
					user, tmActivity, inProNumIidSet);
			if (promotionRes.isSuccess() == false) {
				renderError(promotionRes.getMessage());
			}

			tmActivity.setStatus(TMProActivity.ActivityStatus.ACTIVE);
			tmActivity.setItems(StringUtils.EMPTY);

		}

		// 然后再添加宝贝
		PromotionResult promotionRes = UmpMjsAction.doAddMsjItems(user,
				tmActivity, numIidSet);

		if (promotionRes.isSuccess() == false) {
			renderError(promotionRes.getMessage());
		}

		renderResultJson(promotionRes);
	}

	public static void getMjsItemNumByActivityId(Long activityId) {
		if(activityId == null || activityId <= 0) {
			renderSuccessJson("0");
		}
		User user = getUser();
		if(user == null) {
			renderSuccessJson("0");
		}
		TMProActivity tmActivity = TMProActivity.findByActivityId(user.getId(),
				activityId);
		if(tmActivity == null) {
			renderSuccessJson("0");
		}
		String items = tmActivity.getItems();
		if(StringUtils.isEmpty(items)) {
			renderSuccessJson("0");
		}
		int count = items.split(",").length;
		renderSuccessJson(count + "");
	}
	
	public static void removeMsjActivityTmpl(Long tmActivityId) {
	    
	    User user = getUser();

        if (tmActivityId == null || tmActivityId <= 0) {
            renderError("系统出现异常，活动ID为空，请联系我们！");
        }
        TMProActivity tmActivity = TMProActivity.findByActivityId(user.getId(),
                tmActivityId);
        if (tmActivity == null) {
            renderError("系统出现异常，找不到要添加的活动，请检查是否已删除该活动，或联系我们！");
        }
        
        
        if (tmActivity.isMjsActivity()) {
            UmpMjsAction.removeItemsMjsTmpl(user, tmActivity.getItems(), 
                    tmActivity.getId());
        } else if (tmActivity.isShopMjsActivity()) {
            UmpMjsAction.removeItemsMjsTmpl(user, ItemDao.findNumIidWithUser(user.getId()), 
                    tmActivity.getId());
            
        } else {
            renderError("系统出现异常，这不是一个满就送活动，请联系我们！");
        }
        
        renderTMSuccess("");
	}
	
	public static void reloadMsjActivityTmpl(Long tmActivityId) {
        
        User user = getUser();

        if (tmActivityId == null || tmActivityId <= 0) {
            renderError("系统出现异常，活动ID为空，请联系我们！");
        }
        TMProActivity tmActivity = TMProActivity.findByActivityId(user.getId(),
                tmActivityId);
        if (tmActivity == null) {
            renderError("系统出现异常，找不到要添加的活动，请检查是否已删除该活动，或联系我们！");
        }
        
        Set<Long> targetNumIidSet = new HashSet<Long>();
        
        if (tmActivity.isMjsActivity()) {
            targetNumIidSet = UmpPromotion.parseIdsToSet(tmActivity.getItems());
            
        } else if (tmActivity.isShopMjsActivity()) {
            targetNumIidSet = ItemDao.findNumIidWithUser(user.getId());
            
        } else {
            renderError("系统出现异常，这不是一个满就送活动，请联系我们！");
        }
        if (CommonUtils.isEmpty(targetNumIidSet)) {
            targetNumIidSet = new HashSet<Long>();
        }
        
        for (Long numIid : targetNumIidSet) {
            if (numIid == null || numIid <= 0L) {
                continue;
            }
            UmpMjsTmplUpdateJob.addTmpl(user, numIid, tmActivity.getTmplHtml(), tmActivity.getId(), false);
        }
        
        renderTMSuccess("");
    }
	
	private static TMProActivity checkMjsActivity(Long tmActivityId) {

		User user = getUser();

		if (tmActivityId == null || tmActivityId <= 0) {
			renderError("系统出现异常，要添加的活动ID为空，请联系我们！");
		}

		TMProActivity tmActivity = TMProActivity.findByActivityId(user.getId(),
				tmActivityId);
		if (tmActivity == null) {
			renderError("系统出现异常，找不到要添加的活动，请检查是否已删除该活动，或联系我们！");
		}

		if (tmActivity.isMjsActivity() == false) {
			renderError("系统出现异常，这不是一个满就送活动，请联系我们！");
		}
		Long msgActivityId = tmActivity.getMjsActivityId();
		if (msgActivityId == null || msgActivityId <= 0L) {
			renderError("系统出现异常，满就送活动ID为空，请联系我们！");
		}

		return tmActivity;
	}

	public static void getTMProActivityById(Long activityId) {
		if (activityId == null || activityId <= 0) {
			renderFailedJson("传入的活动ID为空");
		}
		User user = getUser();
		if (user == null) {
			renderFailedJson("用户不存在");
		}
		TMProActivity activity = TMProActivity.findByActivityId(user.getId(),
				activityId);
		if (activity == null) {
			renderFailedJson("活动不存在");
		}
		renderJSON(JsonUtil.getJson(activity));
	}

	public static void addShopMjsActivity(String startTimeStr, String endTimeStr,
			String title, String description, String mjsParamStr,
			String tmplHtml, String remark) {
		long startTime = 0;
        long endTime = 0;
        
        try {
            startTime = sdf.parse(startTimeStr).getTime();
            endTime = sdf.parse(endTimeStr).getTime();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
		if (StringUtils.isEmpty(description)) {
			renderFailedJson("请输入活动名称");
		}
		if (StringUtils.isEmpty(title)) {
			renderFailedJson("请输入活动标签");
		}
		User user = getUser();
		if (user == null) {
			renderFailedJson("用户不存在");
		}

		UmpPromotion.checkW2Expires(user, "/taodiscount/mjsRestartShopActivity");

		MjsParams mjsParams = new MjsParams();
		if (!StringUtils.isEmpty(mjsParamStr)) {
			log.info("addShopMjsActivity for user = " + user.getUserNick() + " with mjsParams = " +
					mjsParamStr);
			mjsParams = mjsParams.createByJson(mjsParamStr);
		}
		if (mjsParams == null) {
			renderFailedJson("满就送json解析出错");
		}
		mjsParams.setActivityName(title);

		UmpMjsActivityAdd umpMjsActivityAdd = new UMPApi.UmpMjsActivityAdd(user, mjsParams, true);
		Long mjsActivityId = umpMjsActivityAdd.call();

		if (mjsActivityId == null || mjsActivityId <= 0) {
			renderFailedJson(umpMjsActivityAdd.getSubErrorMsg());
		}

		TMProActivity activity = new TMProActivity(user.getId(), startTime,
				endTime, title, description,
				TMProActivity.ActivityStatus.ACTIVE,
				TMProActivity.ActivityType.ShopMjs);
		
		activity.setRemark(remark);
		activity.setMjsActivityId(mjsActivityId);
		activity.setMjsParams(mjsParamStr);
		activity.setTmplHtml(tmplHtml);
		activity.jdbcSave();
		//if (!StringUtils.isEmpty(tmplHtml)) {
			
			// 全店活动的话，需要为每个宝贝的详情页都添加模板
			UmpMjsAction.updateItemsMjsTmpl(user,
					ItemDao.findNumIidWithUser(user.getId()), tmplHtml, activity.getId());
		//}
		renderSuccessJson(activity.getId().toString());
	}
	
	public static void restartShopMjsActivity(String startTimeStr, String endTimeStr,
			String title, String description, String mjsParamStr,
			String tmplHtml, String remark, Long activityId) {
		if(activityId == null || activityId <= 0) {
			renderFailedJson("活动ID不合法");
		}
		long startTime = 0;
        long endTime = 0;
        
        try {
            startTime = sdf.parse(startTimeStr).getTime();
            endTime = sdf.parse(endTimeStr).getTime();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
		if (StringUtils.isEmpty(description)) {
			renderFailedJson("请输入活动名称");
		}
		if (StringUtils.isEmpty(title)) {
			renderFailedJson("请输入活动标签");
		}
		User user = getUser();
		if (user == null) {
			renderFailedJson("用户不存在");
		}
		TMProActivity activity = TMProActivity.findByActivityId(user.getId(), activityId);
		if(activity == null) {
			renderFailedJson("活动不存在");
		}
		UmpPromotion.checkW2Expires(user, "/taodiscount/mjsAddActivity");

		MjsParams mjsParams = new MjsParams();
		if (!StringUtils.isEmpty(mjsParamStr)) {
			log.info("restartShopMjsActivity for user = " + user.getUserNick() + " with mjsParams = " +
					mjsParamStr);
			mjsParams = mjsParams.createByJson(mjsParamStr);
		}
		if (mjsParams == null) {
			renderFailedJson("满就送json解析出错");
		}
		mjsParams.setActivityName(title);

		UmpMjsActivityAdd umpMjsActivityAdd = new UMPApi.UmpMjsActivityAdd(user, mjsParams, true);
		Long mjsActivityId = umpMjsActivityAdd.call();

		if (mjsActivityId == null || mjsActivityId <= 0) {
			renderFailedJson(umpMjsActivityAdd.getSubErrorMsg());
		}

		activity.setUserId(user.getId());
    	activity.setActivityStartTime(startTime);
    	activity.setActivityEndTime(endTime);
    	activity.setActivityTitle(title);
    	activity.setActivityDescription(description);
    	activity.setStatus(TMProActivity.ActivityStatus.ACTIVE);
    	activity.setActivityType(TMProActivity.ActivityType.ShopMjs);
		activity.setRemark(remark);
		activity.setMjsActivityId(mjsActivityId);
		activity.setMjsParams(mjsParamStr);
		
		activity.jdbcSave();
		//if (!StringUtils.isEmpty(tmplHtml)) {
			activity.setTmplHtml(tmplHtml);
			// 全店活动的话，需要为每个宝贝的详情页都添加模板
			UmpMjsAction.updateItemsMjsTmpl(user,
					ItemDao.findNumIidWithUser(user.getId()), tmplHtml, activity.getId());
		//}
		renderSuccessJson(activity.getId().toString());
	}

	public static void removeMjsTmplForItem(String userNick, Long numIid) {
		if (numIid == null || numIid <= 0) {
			renderFailedJson("请输入正确的宝贝Id");
		}
		if (StringUtils.isEmpty(userNick)) {
			renderFailedJson("用户名为空");
		}
		User user = UserDao.findByUserNick(userNick);
		if (user == null) {
			renderFailedJson("用户不存在");
		}
		// 更新宝贝详情页
		String desc = RelationAction.getItemDesc(user, numIid);

		String newDesc = UmpMjsAction.deleteMjsTmpl(desc, null);

		ItemUpdate api = new ItemApi.ItemUpdate(user.getSessionKey(), numIid,
				newDesc);
		api.call();
		String errorMsg = api.getErrorMsg();
		// 更新详情页出错
		if (!StringUtils.isEmpty(errorMsg)) {
			renderFailedJson(errorMsg);
		}
		renderSuccessJson("删除成功");
	}
	
	public static void removeMjsTmplForItemWithActivityId(String userNick, Long numIid, Long activityId) {
		if (numIid == null || numIid <= 0) {
			renderFailedJson("请输入正确的宝贝Id");
		}
		if (activityId == null || activityId <= 0) {
			renderFailedJson("请输入正确的活动ID");
		}
		if (StringUtils.isEmpty(userNick)) {
			renderFailedJson("用户名为空");
		}
		User user = UserDao.findByUserNick(userNick);
		if (user == null) {
			renderFailedJson("用户不存在");
		}
		// 更新宝贝详情页
		String desc = RelationAction.getItemDesc(user, numIid);

		String newDesc = UmpMjsAction.deleteMjsTmpl(desc, activityId);

		ItemUpdate api = new ItemApi.ItemUpdate(user.getSessionKey(), numIid,
				newDesc);
		api.call();
		String errorMsg = api.getErrorMsg();
		// 更新详情页出错
		if (!StringUtils.isEmpty(errorMsg)) {
			renderFailedJson(errorMsg);
		}
		renderSuccessJson("删除成功");
	}

	public static void removeMjsTmplForUser(String userNick) {

		if (StringUtils.isEmpty(userNick)) {
			renderFailedJson("用户名为空");
		}
		User user = UserDao.findByUserNick(userNick);
		if (user == null) {
			renderFailedJson("用户不存在");
		}
		Set<Long> numIidSet = ItemDao.findNumIidWithUser(user.getId());
		if (CommonUtils.isEmpty(numIidSet)) {
			renderFailedJson("该用户当前还未上传宝贝");
		}
		int successCount = 0, failCount = 0;
		for (Long numIid : numIidSet) {
			// 更新宝贝详情页
			String desc = RelationAction.getItemDesc(user, numIid);

			String newDesc = UmpMjsAction.deleteMjsTmpl(desc, null);

			ItemUpdate api = new ItemApi.ItemUpdate(user.getSessionKey(),
					numIid, newDesc);
			api.call();
			String errorMsg = api.getErrorMsg();
			// 更新详情页出错
			if (!StringUtils.isEmpty(errorMsg)) {
				failCount++;
			} else {
				successCount++;
			}
		}

		renderSuccessJson("该卖家总共" + numIidSet.size() + "个宝贝，删除成功"
				+ successCount + "个，失败" + failCount + "个");
	}

	public static void mjsItemAdd(Long activityId) {
		render("ump/mjsItemAdd.html", activityId);
	}

	public static void mjsUpdateActivity(Long activityId) {
		render("ump/mjsUpdateActivity.html", activityId);
	}

	public static void mjsShopActivity() {
		render("ump/mjsShopActivity.html");
	}

	public static void mjsShopUpdateActivity(Long activityId) {
		render("ump/shopMjsUpdateActivity.html", activityId);
	}
	
	public static void getUmpMjsTmplUpdateJobQueueSize() {
		int size = UmpMjsTmplUpdateJob.getQueueSize();
		renderText("UmpMjsTmplUpdateJob queue size = " + size);
	}
	
	public static void uploadPicForUser() {
		User user = getUser();
		if(user == null) {
			renderFailedJson("用户不存在");
		}
		Picture  p = new PictureApi.PictureCarrier(user.getSessionKey(),
				Play.applicationPath + "/public/images/dazhe/manjiusong1.jpg", "manjiusong1.jpg", "满就送1")
		.call();
		log.info("dd");
	}
	
	public static void checkItemDesc(String userNick, Long numIid) {
		if(numIid == null || numIid <= 0) {
			renderFailedJson("宝贝ID为空");
		}
		if(StringUtils.isEmpty(userNick)) {
			renderFailedJson("用户名为空");
		}
		User user = UserDao.findByUserNick(userNick);
		if(user == null) {
			renderFailedJson("用户不存在");
		}
		String desc = RelationAction.getItemDesc(user, numIid);
		ItemUpdate api = new ItemApi.ItemUpdate(user.getSessionKey(), numIid, desc);
        api.call();
        String errorMsg = api.getErrorMsg();
        if(!StringUtils.isEmpty(errorMsg)) {
        	renderFailedJson(errorMsg);
        }
        renderSuccessJson("该宝贝详情页不存在问题");
	}
}
