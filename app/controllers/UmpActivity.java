package controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import models.promotion.SalesTitlePlay;
import models.promotion.TMProActivity;
import models.promotion.TMProActivity.ActivityStatus;
import models.promotion.TMProActivity.ActivityType;
import models.ump.ShopMinDiscountPlay;
import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tbapi.ump.NewUMPApi.CommonItemActivityAdd;
import tbapi.ump.NewUMPApi.CommonItemActivityUpdate;
import tbapi.ump.UMPApi;
import actions.ump.PromotionResult;
import actions.ump.ShopActivityAction;
import actions.ump.ShopMinDiscountGetAction;
import actions.ump.UmpMjsAction;
import actions.ump.UmpPromotionAction;
import bustbapi.result.CommonItemActivity;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.MjsPromotion;

import dao.ump.PromotionDao;

public class UmpActivity extends TMController {

    private static final Logger log = LoggerFactory.getLogger(UmpActivity.class);
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat sdfWithSep = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    
    public static void addDiscount() {
        render("ump/discountadd.html");
    }
    
    public static void modifyDiscount(Long activityId) {
        render("ump/discountmodify.html", activityId);
    }
    
    public static void addShopDiscount() {
        render("ump/shopdiscountadd.html");
    }
    
    public static void modifyShopDiscount(Long activityId) {
        render("ump/shopdiscountmodify.html", activityId);
    }
    
    public static void restartDiscount(Long activityId) {
        render("ump/discountrestart.html", activityId);
    }

    public static void restartShopDiscount(Long activityId) {
        render("ump/shopdiscountrestart.html", activityId);
    }
    
    public static void closePage() {
        render("ump/closepage.html");
    }
    

    public static void getServerNowTime() {
        long nowTime = System.currentTimeMillis();
        
        String timeStr = sdfWithSep.format(new Date(nowTime));
        
        renderResultJson(timeStr);
    }
    
    
    public static void findShopMinDiscount() {
        
        User user = getUser();
        ShopMinDiscountPlay shopDiscount = ShopMinDiscountPlay.findByUserId(user.getId());
        
        if (shopDiscount == null) {
            renderResultJson(0);
        } else {
            renderResultJson(shopDiscount.getMinDiscountRate());
        }
    }
    
    public static void syncShopMinDiscount() {
        User user = getUser();
        //先检查w2权限
        UmpPromotion.checkW2Expires(user);
        
        boolean isSuccess = ShopMinDiscountGetAction.fetchShopMinDiscount(user);
        
        if (isSuccess == false) {
            renderError("同步店铺最低折扣出错，请联系我们！");
        } else {
            renderTMSuccess("同步店铺最低折扣成功！");
        }
        
    }
    
    public static void queryActivity(Long tmActivityId) {
        if (tmActivityId == null || tmActivityId <= 0) {
            renderError("系统出现异常，活动ID为空，请联系我们！");
        }
        
        User user = getUser();
        
        TMProActivity tmActivity = TMProActivity.findByActivityId(user.getId(), tmActivityId);
        if (tmActivity == null) {
            renderError("系统出现异常，找不到要指定的活动，请检查是否已删除该活动，或联系我们！");
        }
        
        renderResultJson(tmActivity);
    }
    
    public static void createShopActivity(String startTimeStr, String endTimeStr, String title, 
            String description, long discountRate) {
        User user = getUser();
        
        long startTime = 0;
        long endTime = 0;
        
        try {
            startTime = sdf.parse(startTimeStr).getTime();
            endTime = sdf.parse(endTimeStr).getTime();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        
        //先检查w2权限
        UmpPromotion.checkW2Expires(user);
        
        checkActivityParams(startTime, endTime, title, description);
        checkShopDiscountRate(discountRate);
        
        TMProActivity activity = new TMProActivity(user.getId(), startTime, endTime, 
                title, description, ActivityStatus.ACTIVE, ActivityType.ShopDiscount);
        
        
        PromotionResult promotionRes = ShopActivityAction.createShopDiscountActivity(user, 
                activity, discountRate);
        
        if (promotionRes.isSuccess() == false) {
            renderError(promotionRes.getMessage());
        }
        
        renderResultJson(promotionRes);
        
    }
    
    public static void updateShopActivity(Long tmActivityId, String startTimeStr, String endTimeStr, 
            String title, String description, long discountRate) {
        User user = getUser();
        
        long startTime = 0;
        long endTime = 0;
        
        try {
            startTime = sdf.parse(startTimeStr).getTime();
            endTime = sdf.parse(endTimeStr).getTime();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        
        
        TMProActivity tmActivity = TMProActivity.findByActivityId(user.getId(), tmActivityId);
        if (tmActivity == null) {
            renderError("系统出现异常，找不到要指定的活动，请检查是否已删除该活动，或联系我们！");
        }
        if (tmActivity.isShopDiscountActivity() == false) {
            renderError("系统出现异常，这不是全店打折活动，请联系我们！");
        }
        
        checkActivityParams(startTime, endTime, title, description);
        checkShopDiscountRate(discountRate);
        
        
        long originStartTime = tmActivity.getActivityStartTime() == null ? 0 
                : tmActivity.getActivityStartTime();
        long originEndTime = tmActivity.getActivityEndTime() == null ? 0 
                : tmActivity.getActivityEndTime();
        long originDiscountRate = tmActivity.getShopDiscountParam() == null ? 0 
                : tmActivity.getShopDiscountParam().getDiscountRate();
        String originTitle = tmActivity.getActivityTitle();
        
        boolean isNeedApiUpdate = true;
        //不需要使用api更新
        if (originStartTime == startTime && originEndTime == endTime
                && title.equals(originTitle)
                && originDiscountRate == discountRate) {
            isNeedApiUpdate = false;
        }
        
        tmActivity.updateActivityParams(startTime, endTime, title, description);
        
        if (isNeedApiUpdate == true) {
            //先检查w2权限
            UmpPromotion.checkW2Expires(user);
            
            PromotionResult promotionRes = ShopActivityAction.updateShopDiscountActivity(user, tmActivity, 
                    discountRate);
            
            if (promotionRes.isSuccess() == false) {
                renderError(promotionRes.getMessage());
            }
            renderResultJson(promotionRes);
            
        } else {
            boolean isSuccess = tmActivity.jdbcSave();
            if (isSuccess == false) {
                renderError("系统出现异常，活动修改失败，请联系我们！");
            } else {
                renderTMSuccess("全店打折活动修改成功！");
            }
        }
    }
    
    
    public static void restartShopActivity(Long tmActivityId, String startTimeStr, String endTimeStr, 
            String title, String description, long discountRate) {
        User user = getUser();
        
        long startTime = 0;
        long endTime = 0;
        
        try {
            startTime = sdf.parse(startTimeStr).getTime();
            endTime = sdf.parse(endTimeStr).getTime();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        
        
        TMProActivity tmActivity = TMProActivity.findByActivityId(user.getId(), tmActivityId);
        if (tmActivity == null) {
            renderError("系统出现异常，找不到要指定的活动，请检查是否已删除该活动，或联系我们！");
        }
        if (tmActivity.isShopDiscountActivity() == false) {
            renderError("系统出现异常，这不是全店打折活动，请联系我们！");
        }
        
        if (tmActivity.isNowActive() == true) {
            renderError("系统异常，该活动尚未结束或之前重启过！");
        }
        
        UmpPromotion.checkW2Expires(user);
        
        checkActivityParams(startTime, endTime, title, description);
        checkShopDiscountRate(discountRate);
        
        tmActivity.updateActivityParams(startTime, endTime, title, description);
        
        //这里的状态还是unactive的，因为还没有提交宝贝
        tmActivity.setStatus(ActivityStatus.UNACTIVE);
        
        PromotionResult promotionRes = ShopActivityAction.createShopDiscountActivity(user, 
                tmActivity, discountRate);
        
        if (promotionRes.isSuccess() == false) {
            renderError(promotionRes.getMessage());
        }
        
        
        renderResultJson(promotionRes);
    }
    
    
    public static void createDazheActivity(String startTimeStr, String endTimeStr, String title, 
            String description) {
        
        long startTime = 0;
        long endTime = 0;
        
        try {
            startTime = sdf.parse(startTimeStr).getTime();
            endTime = sdf.parse(endTimeStr).getTime();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        
        User user = getUser();
        
        checkActivityParams(startTime, endTime, title, description);
        
        UmpPromotion.checkW2Expires(user);
        
        // UMP接口改造
        CommonItemActivityAdd activityAddApi = new CommonItemActivityAdd(user, title, description, startTime, endTime);
        Long disActivityId = activityAddApi.call();
        if(disActivityId == null) {
        	if("优惠活动数量超过限制".equalsIgnoreCase(activityAddApi.getSubErrorMsg())) {
        		renderError("优惠活动数量超过限制，淘宝规定所有第三方软件活动总数（包含已结束的活动）最多为30个，请先去官方<a href='https://smf.taobao.com/index.htm?menu=activity&module=rmgj' style='color: red' target='_blank'>营销中心</a>删除不需要的活动后再来吧！");
        	}
        	renderError(activityAddApi.getSubErrorMsg());
        }
        TMProActivity activity = new TMProActivity(user.getId(), startTime, endTime, 
                title, description, ActivityStatus.ACTIVE, ActivityType.NewDiscount, disActivityId);
        
//        TMProActivity activity = new TMProActivity(user.getId(), startTime, endTime, 
//                title, description, ActivityStatus.ACTIVE, ActivityType.Discount);
        
        boolean isSuccess = activity.jdbcSave();
        if (isSuccess == false) {
            renderError("系统出现异常，活动创建失败，请联系我们！");
        }
        
        renderResultJson(activity.getTMActivityId());
        
    }
    
    
    public static void updateDazheActivity(Long tmActivityId, String startTimeStr, String endTimeStr, 
            String title, String description) {
        
        long startTime = 0;
        long endTime = 0;
        if(startTimeStr.indexOf(":") < 0) {
        	startTimeStr += " 00:00:00";
        }
        if(endTimeStr.indexOf(":") < 0) {
        	endTimeStr += " 00:00:00";
        }
        try {
            startTime = sdf.parse(startTimeStr).getTime();
            endTime = sdf.parse(endTimeStr).getTime();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        
    
        User user = getUser();
        
        TMProActivity tmActivity = UmpPromotion.checkDazheActivity(tmActivityId);
        
        checkActivityParams(startTime, endTime, title, description);
        
        // UMP接口改造
        if(tmActivity.getActivityType() == ActivityType.NewDiscount) {
        	//先检查w2权限
            UmpPromotion.checkW2Expires(user);
            // 改造后的活动Id存在mjsActivityId上
        	CommonItemActivityUpdate activityUpdateApi = new CommonItemActivityUpdate(user, tmActivity.getMjsActivityId(), title, description, startTime, endTime);
        	Boolean success = activityUpdateApi.call();
        	if(!success) {
        		renderError(activityUpdateApi.getSubErrorMsg());
        	}
        	// 接口调用成功 修改本地数据库
        	tmActivity.updateActivityParams(startTime, endTime, title, description);
            boolean isSuccess = tmActivity.jdbcSave();
            if (isSuccess == false) {
                renderError("系统出现异常，数据库更新活动失败，请联系我们！");
            }
        	renderTMSuccess("活动修改成功！");
        }
        
        long originStartTime = tmActivity.getActivityStartTime() == null ? 0 
                : tmActivity.getActivityStartTime();
        long originEndTime = tmActivity.getActivityEndTime() == null ? 0 
                : tmActivity.getActivityEndTime();
        String originTitle = tmActivity.getActivityTitle();
        
        boolean isNeedApiUpdate = true;
        //不需要使用api更新
        if (originStartTime == startTime && originEndTime == endTime
                && title.equals(originTitle)
                && tmActivity.isUpdatePromotonNotAllSuccess() == false) {
            isNeedApiUpdate = false;
        } else {
            //long promotionCount = PromotionDao.countByTMActivityId(user.getId(), tmActivityId);
            long promotionCount = PromotionDao.countActivePromotionsByTMActivityId(user.getId(), 
                    tmActivityId);
            
            
            if (promotionCount <= 0) {
                isNeedApiUpdate = false;
            } else {
                isNeedApiUpdate = true;
            }
            
            
            
        }
        
        if (isNeedApiUpdate == true) {
            //先检查w2权限
            UmpPromotion.checkW2Expires(user);
            
            //先设置成尚未全部成功的状态
            tmActivity.setUpdatePromotonNotAllSuccess();
        } else {
            tmActivity.removeUpdatePromotonNotAllSuccess();
        }
        
        //title不要更新的
        tmActivity.updateActivityParams(startTime, endTime, title, description);
        
        boolean isSuccess = tmActivity.jdbcSave();
        if (isSuccess == false) {
            renderError("系统出现异常，活动修改失败，请联系我们！");
        }
        
        if (isNeedApiUpdate == true) {
            PromotionResult promotionRes = UmpPromotionAction.updateAllPromotions(user, tmActivity);
            
            if (promotionRes.isSuccess() == false) {
                renderError(promotionRes.getMessage());
            }
            if (CommonUtils.isEmpty(promotionRes.getErrorList())
                    && promotionRes.getNotExecuteNum() <= 0) {
                //如果全部宝贝都更新成功了
                tmActivity.removeUpdatePromotonNotAllSuccess();
                tmActivity.jdbcSave();
                
                renderTMSuccess("活动修改成功，" + promotionRes.getMessage());
            } else {
                renderResultJson(promotionRes);
            }
            
        } else {
            renderTMSuccess("活动修改成功！");
        }
        
        
    }
    
    //结束活动，但不删除数据库，可以重启
    public static void cancelActivity(Long tmActivityId) {
        if (tmActivityId == null || tmActivityId <= 0) {
            renderError("系统出现异常，活动ID为空，请联系我们！");
        }
        
        User user = getUser();
        //先检查w2权限
        UmpPromotion.checkW2Expires(user);
        
        TMProActivity tmActivity = TMProActivity.findByActivityId(user.getId(), tmActivityId);
        if (tmActivity == null) {
            renderError("系统出现异常，找不到要指定的活动，请检查是否已删除该活动，或联系我们！");
        }
        
        PromotionResult promotionRes = null;
        
        if (tmActivity.isOldActivity()) {
            renderError("系统出现异常，这不是新的打折活动，请联系我们！");
        } else if (tmActivity.isDiscountActivity()) {
            promotionRes = UmpPromotionAction.cancelDazheActivity(user, tmActivity);
        } else if (tmActivity.isMjsActivity()) {
            promotionRes = UmpMjsAction.cancelMjsActivity(user, tmActivity);
        } else if (tmActivity.isShopMjsActivity()) {
            promotionRes = UmpMjsAction.cancelShopMjsActivity(user, tmActivity);
        } else if (tmActivity.isShopDiscountActivity()) {
            promotionRes = ShopActivityAction.cancelShopDiscountActivity(user, tmActivity);
        } else if (tmActivity.isNewDiscountActivity()) {
        	promotionRes = UmpPromotionAction.cancelNewDazheActivity(user, tmActivity);
        } else {
            renderError("系统出现异常，活动类型出错，请联系我们！");
        }
        
        if (promotionRes.isSuccess() == false) {
            renderError(promotionRes.getMessage());
        }
        
        renderResultJson(promotionRes);
    }
    
    
    //删除活动，只需要删除数据库，淘宝activity在结束活动的时候已经删除
    public static void deleteActivity(Long tmActivityId) {
        if (tmActivityId == null || tmActivityId <= 0) {
            renderError("系统出现异常，活动ID为空，请联系我们！");
        }
        
        User user = getUser();
        //先检查w2权限
        //UmpPromotion.checkW2Expires(user);
        
        TMProActivity tmActivity = TMProActivity.findByActivityId(user.getId(), tmActivityId);
        if (tmActivity == null) {
            renderError("系统出现异常，找不到要指定的活动，请检查是否已删除该活动，或联系我们！");
        }
        
        if (tmActivity.isOldActivity()) {
            renderError("系统出现异常，这不是新的打折活动，请联系我们！");
        } else if (tmActivity.isDiscountActivity()) {
            //只需要删除数据库
            PromotionDao.deleteByTMActivityId(user.getId(), tmActivityId);
            tmActivity.rawDelete();
            
        } else if (tmActivity.isNewDiscountActivity()) {
            //只需要删除数据库
            PromotionDao.deleteByTMActivityId(user.getId(), tmActivityId);
            tmActivity.rawDelete();
            
        } else if (tmActivity.isMjsActivity()) {
            //只需要删除数据库
            tmActivity.rawDelete();
            
        } else if (tmActivity.isShopMjsActivity()) {
            //只需要删除数据库
            tmActivity.rawDelete();
            
        } else if (tmActivity.isShopDiscountActivity()) {
            //只需要删除数据库
            tmActivity.rawDelete();
            
        } else {
            renderError("系统出现异常，活动类型出错，请联系我们！");
        }
        
        renderResultJson("活动删除成功！");
    }
    
    public static void restartDazheActivity(Long tmActivityId, String startTimeStr, String endTimeStr, 
            String title, 
            String description) {
        
        long startTime = 0;
        long endTime = 0;
        
        try {
            startTime = sdf.parse(startTimeStr).getTime();
            endTime = sdf.parse(endTimeStr).getTime();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        
        User user = getUser();
        
        TMProActivity tmActivity = UmpPromotion.checkDazheActivity(tmActivityId);
        
        if (tmActivity.isNowActive() == true) {
            renderError("系统异常，该活动尚未结束或之前重启过！");
        }
        
        checkActivityParams(startTime, endTime, title, description);
        
        UmpPromotion.checkW2Expires(user);
        
        // UMP接口改造
        CommonItemActivityAdd activityAddApi = new CommonItemActivityAdd(user, title, description, startTime, endTime);
        Long disActivityId = activityAddApi.call();
        if(disActivityId == null) {
        	if("优惠活动数量超过限制".equalsIgnoreCase(activityAddApi.getSubErrorMsg())) {
        		renderError("优惠活动数量超过限制，淘宝规定所有第三方软件活动总数（包含已结束的活动）最多为30个，请先去官方<a href='https://smf.taobao.com/index.htm?menu=activity&module=rmgj' style='color: red' target='_blank'>营销中心</a>删除不需要的活动后再来吧！");
        	}
        	renderError(activityAddApi.getSubErrorMsg());
        }
        
        tmActivity.updateActivityParams(startTime, endTime, title, description);
        tmActivity.setActivityType(ActivityType.NewDiscount);
        tmActivity.setMjsActivityId(disActivityId);
        
        //这里的状态还是unactive的，因为还没有提交宝贝
        tmActivity.setStatus(ActivityStatus.UNACTIVE);
        
        boolean isSuccess = tmActivity.jdbcSave();
        if (isSuccess == false) {
            renderError("数据库出现异常，请联系我们！");
        } else {
            renderTMSuccess("");
        }
    }
    
    
    private static void checkActivityParams(long startTime, long endTime, String title, 
            String description) {
        if (startTime <= 0) {
            renderError("请先设置活动的起始时间！");
        }
        if (endTime <= 0) {
            renderError("请先设置活动的结束时间！");
        }
        if (endTime <= startTime) {
            renderError("活动结束时间必须大于起始时间！");
        }
        
        if (StringUtils.isEmpty(title) || StringUtils.isEmpty(title.trim())) {
            renderError("请先输入一个活动标签！");
        }
        
        if (StringUtils.isEmpty(description) || StringUtils.isEmpty(description.trim())) {
            renderError("请先输入一个活动描述！");
        }
    }
    
    private static void checkShopDiscountRate(long discountRate) {
        if (discountRate <= 0) {
            renderError("全店宝贝的折扣必须大于0折！");
        }
        if (discountRate >= 1000) {
            renderError("全店宝贝的折扣必须小于10折！");
        }
    }
    
    public static void removeUserMjsActivity(Long type) {
    	if(type == null || type <= 0) {
    		type = 1L;
    	}
    	User user = getUser();
    	if(user == null) {
    		renderFailedJson("用户不存在");
    	}
    	
    	UmpPromotion.checkW2Expires(user);
    	
    	List<MjsPromotion> promotions = new UMPApi.UmpMjsActivityListGet(user, type).call();
    	if(CommonUtils.isEmpty(promotions)) {
    		renderSuccessJson("亲您目前没有生效中的UMP Msj 活动哦");
    	}
    	boolean isSuccess = true;
    	for(MjsPromotion promotion : promotions) {
    		if(new UMPApi.UmpMjsActivityDelete(user, promotion.getActivityId()).call() == false) {
    			isSuccess = false;
    		}	
    	}
    	if(isSuccess) {
    		renderSuccessJson("删除成功");
    	}
    	renderFailedJson("删除失败");
    }
    
	public static void getSalesTitle() {
		User user = getUser();
    	if(user == null) {
    		renderFailedJson("用户不存在");
    	}
    	List<SalesTitlePlay> list = SalesTitlePlay.getAll();
    	if(CommonUtils.isEmpty(list)) {
    		renderFailedJson("找不到可以使用的营销标签！请联系我们");
    	}
    	renderResultJson(list);
	}
    
}
