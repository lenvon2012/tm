package actions.ump;

import models.promotion.TMProActivity;
import models.promotion.TMProActivity.ActivityStatus;
import models.promotion.TMProActivity.ShopDiscountParam;
import models.ump.ShopMinDiscountPlay;
import models.ump.PromotionPlay.ItemPromoteType;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tbapi.ump.UMPApi.ShopDiscountActivityAdd;
import tbapi.ump.UMPApi.ShopDiscountActivityUpdate;
import tbapi.ump.UMPApi.UmpSingleItemActivityDelete;

import actions.ump.PromotionResult.PromotionErrorMsg;
import actions.ump.PromotionResult.PromotionErrorType;

import com.dbt.cred.utils.JsonUtil;

public class ShopActivityAction {

    private static final Logger log = LoggerFactory.getLogger(ShopActivityAction.class);
    
    
    public static PromotionResult createShopDiscountActivity(User user, 
            TMProActivity activity, long discountRate) {
        
        ShopDiscountParam shopParam = new ShopDiscountParam(ItemPromoteType.discount, discountRate, 0);
        
        activity.setMjsParams(JsonUtil.getJson(shopParam));
        
        ShopDiscountActivityAdd addApi = new ShopDiscountActivityAdd(user, activity, discountRate);
        
        Long promotionId = addApi.call();
        if (promotionId == null || promotionId <= 0L || addApi.isApiSuccess() == false) {
            
            PromotionResult promotionRes = new PromotionResult(addApi, "创建全店打折活动失败");
            
            return promotionRes;
        } 
        
        activity.setMjsActivityId(promotionId);
        
        
        activity.setStatus(ActivityStatus.ACTIVE);
        
        
        boolean isSuccess = activity.jdbcSave();
        if (isSuccess == false) {
            return new PromotionResult(false, "数据库存储失败，请联系我们！");
        }
        
        String shopDiscountMsg = getShopMinDiscountMessage(user, discountRate);
        
        return new PromotionResult(true, "全店打折活动创建成功" + shopDiscountMsg + "！");
    }
    
    private static String getShopMinDiscountMessage(User user, long discountRate) {
        ShopMinDiscountPlay shopDiscount = ShopMinDiscountPlay.findByUserId(user.getId());
        
        if (shopDiscount == null) {
            return "";
        }
        int minDiscountRate = shopDiscount.getMinDiscountRate();
        
        if (minDiscountRate <= 0) {
            return "";
        }
        
        if (discountRate >= minDiscountRate) {
            return "";
        }
        
        String message = "，但是活动折扣低于店铺最低折扣" 
                + minDiscountRate * 1.0 / 100 + "折，折扣将无法显示，请修改店铺最低折扣";
        
        return message;
    }
    
    
    public static PromotionResult updateShopDiscountActivity(User user, TMProActivity tmActivity,
            long discountRate) {
        
        ShopDiscountActivityUpdate updateApi = new ShopDiscountActivityUpdate(user, tmActivity, 
                tmActivity.getMjsActivityId(), discountRate);
        
        Boolean updateRes = updateApi.call();
        if (updateRes == null || updateRes.booleanValue() == false 
                || updateApi.isApiSuccess() == false) {
            
            PromotionResult promotionRes = new PromotionResult(updateApi, "全店打折活动修改失败");
            
            return promotionRes;
        } 
        
        ShopDiscountParam shopParam = new ShopDiscountParam(ItemPromoteType.discount, discountRate, 0);
        
        tmActivity.setMjsParams(JsonUtil.getJson(shopParam));
        
        
        boolean isSuccess = tmActivity.jdbcSave();
        if (isSuccess == false) {
            return new PromotionResult(false, "数据库存储失败，请联系我们！");
        }
        
        String shopDiscountMsg = getShopMinDiscountMessage(user, discountRate);
        
        return new PromotionResult(true, "全店打折活动修改成功" + shopDiscountMsg + "！");
    }
    
    
    
    public static PromotionResult cancelShopDiscountActivity(User user, TMProActivity tmActivity) {
        
        UmpSingleItemActivityDelete deleteApi = new UmpSingleItemActivityDelete(user, 
                tmActivity.getMjsActivityId());
        
        Boolean deleteRes = deleteApi.call();
        if (deleteRes == null || deleteRes.booleanValue() == false || deleteApi.isApiSuccess() == false) {
            
            PromotionErrorMsg promotionError = PromotionResult.createPromotionError(deleteApi, 
                    0L);
            
            if (PromotionErrorType.PromotionDeletedInTaobao == promotionError.getErrorType()) {
                
            } else {
                PromotionResult promotionRes = new PromotionResult(deleteApi, "全店打折活动结束失败");
                
                return promotionRes;
            }
            
            
        } 
         
        //tmActivity.setStatus(ActivityStatus.UNACTIVE);
        tmActivity.setUnActiveStatusAndEndTime();

        boolean isSuccess = tmActivity.jdbcSave();
        if (isSuccess == false) {
            return new PromotionResult(false, "数据库存储失败，请联系我们！");
        }
        
        return new PromotionResult(true, "全店打折活动结束成功！");
    }
    
    
}
