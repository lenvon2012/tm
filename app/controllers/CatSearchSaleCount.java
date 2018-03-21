package controllers;

import java.util.List;

import models.tmsearch.TmallSearchLog.TmallSearchType;
import models.tmsearch.UserShopPlay;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actions.catunion.UserRateSpiderAction;
import actions.catunion.UserRateSpiderAction.UserRateInfo;
import actions.catunion.UserShopSalesAction;
import actions.catunion.UserShopSalesAction.ShopSalesResult;

import com.ciaosir.client.utils.JsonUtil;

public class CatSearchSaleCount extends CatUnionBase {
    private static final Logger log = LoggerFactory.getLogger(CatSearchComment.class);

    public static void index() {
        String title = "卖家销量查询";
        String keywords = MetaKeywords;
        String description = MetaDescription;
        List<SearchNickInfo> nickInfoList = queryLatestNick(TmallSearchType.ShopSales);
        render("tmSearch/searchsalecount.html", title, keywords, description, nickInfoList);

    }
    
    public static void userSales() {
        String userNick = queryNickByUserId(TmallSearchType.ShopSales);
        if (StringUtils.isEmpty(userNick))
            renderText("");
        //log.error(userNick);
        String title = "" + userNick + " 卖家销量查询 - 天猫联盟";
        String keywords = userNick + " " + MetaKeywords;
        String description = "淘宝账号：" + userNick + " 卖家销量查询。 " + MetaDescription;
        
        List<SearchNickInfo> nickInfoList = queryLatestNick(TmallSearchType.ShopSales);
        render("tmSearch/searchsalecount.html", title, keywords, description, userNick, nickInfoList);
    }
    
    
    public static void doQuerySellerCredit(Long userId, String userNick) {
        
        checkUserId(userId);
        userNick = trimUserNick(userNick);
        
        UserRateInfo userRateInfo = UserRateSpiderAction.doSpiderUserRate(userId, userNick);

        if (userRateInfo == null) {
            
            renderFailJson("");
        }

        //log.error(JsonUtil.getJson(userRateInfo));
        
        renderResultJson(userRateInfo);
    }
    
    public static void doQuerySaleCount(String userNick) {
        
  
        
        //log.error("userNick: " + userNick + "---------------");
        userNick = trimUserNick(userNick);
        //log.error("userNick: " + userNick + "---------------");
        
        ShopSalesResult shopResult = UserShopSalesAction.doQueryUserShopSales(userNick);
        if (shopResult == null || shopResult.isOk() == false) {
            saveSearchLog(TmallSearchType.ShopSales, 0, userNick, false);
        } else {
            UserShopPlay userShop = (UserShopPlay)(shopResult.getRes());
            String realNick = "";
            boolean isSuccess = false;
            Long userId = 0L;
            if (userShop == null) {
                isSuccess = false;
                realNick = userNick;
            } else {
                isSuccess = true;
                userId = userShop.getUserId();
                realNick = userShop.getNick();
            }
            if (userId == null)
                userId = 0L;
            
            saveSearchLog(TmallSearchType.ShopSales, userId, realNick, isSuccess);
        }
        if (shopResult == null)
            renderFailJson("系统出现一些异常，请稍后重试");
        else
            renderJSON(JsonUtil.getJson(shopResult));
    }
    
    /*
    public static void batchUpdateSales() {
        UpdateShopSalesJob job = new UpdateShopSalesJob();
        job.doJob();
    }
    
    public static void updateShop(String nick) {
        nick = trimUserNick(nick);
        UserShopPlay userShop = UserShopPlay.findByNick(nick);
        
        if (userShop == null)
            renderText("找不到nick为" + nick + "的店铺");
        
        boolean isOk = UpdateShopSalesJob.updateOneShop(userShop);
        
        if (isOk == false)
            renderText("更新nick为" + nick + "的店铺失败");
        else
            renderText("更新nick为" + nick + "的店铺成功");
    }*/
}
