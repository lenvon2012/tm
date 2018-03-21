package actions.ump;

import java.util.Date;
import java.util.List;

import models.item.ItemPlay;
import models.item.ItemPlay.Status;
import models.ump.ShopMinDiscountGetLog;
import models.ump.ShopMinDiscountGetLog.ShopMinDiscountApiStatus;
import models.ump.ShopMinDiscountGetLog.ShopMinDiscountTMStatus;
import models.ump.ShopMinDiscountPlay;
import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actions.promotion.PromotionAction;
import actions.promotion.PromotionAction.PromotionAddApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.taobao.api.domain.Promotion;

import dao.item.ItemDao;

public class ShopMinDiscountGetAction {

    private static final Logger log = LoggerFactory.getLogger(ShopMinDiscountGetAction.class);
    
    
    public static boolean fetchShopMinDiscount(User user) {
        
        
        try {
            long startTime = System.currentTimeMillis();
            
            boolean isSuccess = doFetchShopMinDiscount(user);
            
            long endTime = System.currentTimeMillis();
            
            log.info("end fetchShopMinDiscount for user: " + user.getUserNick() 
                    + ", used " + (endTime - startTime) + " ms, isSuccess: " + isSuccess + "--------------");
            
            return isSuccess;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
        
        
        
    }
    
    private static boolean doFetchShopMinDiscount(User user) {
        ItemPlay item = findProperPromotionItem(user);
        if (item == null) {
            log.error("cannot find proper item to test shop min discount for user: " 
                    + user.getUserNick() + "--------------------------");
            return false;
        }
        
        ShopMinDiscountGetLog shopLog = new ShopMinDiscountGetLog(user.getId(), 
                item.getNumIid(), ShopMinDiscountTMStatus.Started);
        
        boolean isSuccess = shopLog.jdbcSave();
        if (isSuccess == false) {
            return false;
        }
        
        long nowTime = System.currentTimeMillis();
        if (nowTime < 1398221360942L) {
            nowTime = 1398221360942L;
        }
        
        final long startDateTime = nowTime + DateUtil.DAY_MILLIS * 1000 * 400;
        final long endDateTime = startDateTime + 1000;
        
        final String numIids = item.getNumIid() + "";
        
        final Date startDate = new Date(startDateTime);
        final Date endDate = new Date(endDateTime);
        
        final Long tmActivityId = 0L;
        
        final String title = "店铺折扣";
        final String description = "";
        final Long decreaseNumber = 1L;
        final Long tagId = 1L;
        final String discountType = "DISCOUNT";
        final String discountValue = "0.01";
        
        
        PromotionAddApi api = new PromotionAddApi(user, tmActivityId, numIids, 
                discountType, discountValue, 
                startDate,
                endDate, title, tagId, description, decreaseNumber);
        
        List<Promotion> promotionList = api.call();
        
        //失败
        if (api.isApiSuccess() == false || CommonUtils.isEmpty(promotionList)) {
            String subMsg = api.getErrorMsg();
            String subCode = api.getSubErrorCode();
            if (StringUtils.isEmpty(subMsg)) {
                subMsg = "";
            }
            if (StringUtils.isEmpty(subCode)) {
                subCode = "";
            }
            
            final String errorPrev = "商品折扣幅度不能低于设置的店铺最低折扣";
            boolean isResultGet = false;
            
            if (subCode.contains("isv.w2-security-authorize-invalid")) {
                shopLog.setApiStatus(ShopMinDiscountApiStatus.PromotionAuthOutDate);
                shopLog.setTmStatus(ShopMinDiscountTMStatus.GetShopDiscountFail);
            } else if (subMsg.contains(errorPrev)) {
                shopLog.setApiStatus(ShopMinDiscountApiStatus.PromotionMinDiscountFail);
                calcuShopMinDiscount(user, 
                        subMsg.substring(errorPrev.length(), errorPrev.length() + 4));
                shopLog.setTmStatus(ShopMinDiscountTMStatus.Finished);
                isResultGet = true;
            } else {
                log.error("promotion error: " + subMsg + ", " + subCode + " for user: " + user.getUserNick());
                shopLog.setApiStatus(ShopMinDiscountApiStatus.PromotionOtherFail);
                shopLog.setTmStatus(ShopMinDiscountTMStatus.GetShopDiscountFail);
            }
            
            shopLog.jdbcSave();
            return isResultGet;
            
        } else {
            shopLog.setApiStatus(ShopMinDiscountApiStatus.PromotionSuccess);
            shopLog.setPromotionId(promotionList.get(0).getPromotionId());
            shopLog.setTmStatus(ShopMinDiscountTMStatus.Promotioned);
            calcuShopMinDiscount(user, "0.1");
            
            shopLog.jdbcSave();
        }
        
        //然后删除活动
        deletePromotion(user, shopLog);
        
        return true;
    }
    

    private static void deletePromotion(User user, ShopMinDiscountGetLog shopLog) {
        
        try {
            shopLog.setDeleteTimes(shopLog.getDeleteTimes() + 1);
            
            boolean isSuccess = PromotionAction.deletePromotion(user.getSessionKey(), 
                    shopLog.getPromotionId());
            
            if (isSuccess == true) {
                shopLog.setApiStatus(ShopMinDiscountApiStatus.DeleteSuccess);
                shopLog.setTmStatus(ShopMinDiscountTMStatus.Finished);
            } else {
                shopLog.setApiStatus(ShopMinDiscountApiStatus.DeleteFail);
                shopLog.setTmStatus(ShopMinDiscountTMStatus.DeletePromotionError);
            }
            
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            shopLog.setApiStatus(ShopMinDiscountApiStatus.DeleteFail);
            shopLog.setTmStatus(ShopMinDiscountTMStatus.DeletePromotionError);
        }
        
        shopLog.jdbcSave();
        
    }
    
    //discountStr: 49.9
    private static void calcuShopMinDiscount(User user, String discountStr) {
        
        try {
            discountStr = discountStr.replace("%", "");
            discountStr = discountStr.replace(",", "");
            
            double discountRate = Double.parseDouble(discountStr);
            
            int discountRateInt = (int) Math.round(discountRate * 10);
            
            log.info("get shop min discount: " + discountRateInt + " for user: " 
                    + user.getUserNick() + "-----------");
                    
            ShopMinDiscountPlay shopDiscount = ShopMinDiscountPlay.findByUserId(user.getId());
            
            if (shopDiscount == null) {
                shopDiscount = new ShopMinDiscountPlay(user.getId(), user.getUserNick(), discountRateInt);
            } else {
                shopDiscount.setMinDiscountRate(discountRateInt);
            }
            
            shopDiscount.jdbcSave();
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        
    }
    
    
    
    //先找下架的宝贝，再找销量低的
    private static ItemPlay findProperPromotionItem(User user) {
        

        final int minPrice = 10;
        
        ItemPlay item = ItemDao.findMinSalesItemWithStatus(user.getId(), Status.INSTOCK, minPrice);
        if (item != null) {
            return item;
        }
        
        item = ItemDao.findMinSalesItemWithStatus(user.getId(), Status.ONSALE, minPrice);
        
        return item;
    }
    
    
}
