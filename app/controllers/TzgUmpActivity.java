
package controllers;

import models.promotion.TMProActivity;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actions.ump.PromotionResult;
import actions.ump.ShopActivityAction;
import actions.ump.UmpMjsAction;
import actions.ump.UmpPromotionAction;

public class TzgUmpActivity extends TMController {

    private static final Logger log = LoggerFactory.getLogger(UmpActivity.class);

    public static void editDetail() {
        render("tzgump/editDetail.html");
    }

    public static void index() {
        render("tzgump/activitylist.html");

    }

    public static void addDiscount() {
        render("tzgump/discountadd.html");
    }

    public static void modifyDiscount(long activityId) {
        render("tzgump/discountmodify.html", activityId);
    }

    public static void cancelActivity(Long tmActivityId) {
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
        } else {
            renderError("系统出现异常，活动类型出错，请联系我们！");
        }

        if (promotionRes.isSuccess() == false) {
            renderError(promotionRes.getMessage());
        }

        renderResultJson(promotionRes);
    }

    /*public static void queryActivityList(boolean isActive) {
        
        User user = getUser();
        
        List<TMProActivity> activity = TMProActivity.findOnActiveActivitys(user.getId());

        long nowTime = CommonUtils.Date2long(new Date());

        for (TMProActivity act : activity) {

            long endTime = act.getActivityEndTime();
            if (endTime <= nowTime) {
                
                if (act.isDiscountActivity()) {
                    PromotionDao.unActiveActivityPromotions(user.getId(), act.getTMActivityId());
                }
                act.setStatus(ActivityStatus.UNACTIVE);
                act.jdbcSave();
                
            }

        }
        if (isActive == true) {
            List<TMProActivity> activityList = TMProActivity.findOnActiveActivitys(user.getId());
            renderResultJson(activityList);
        } else {
            List<TMProActivity> activityList = TMProActivity.findUnActiveActivitys(user.getId());
            renderResultJson(activityList);
        }

        
        
    }*/

}
