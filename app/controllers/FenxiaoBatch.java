package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import models.item.ItemPlay;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;
import actions.batch.BatchEditResult;
import actions.fenxiao.FenxiaoPriceConfig;
import actions.fenxiao.FenxiaoPriceConfig.FenxiaoSubmitItemType;
import actions.fenxiao.FenxiaoPriceEditAction;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;

import dao.item.ItemDao;

public class FenxiaoBatch extends TMController {

    private static final Logger log = LoggerFactory.getLogger(FenxiaoBatch.class);
    
    public static void index() {
        render("newAutoTitle/fenxiaobatch.html");
    }
    
    
    public static void searchFenxiaoItems(String title, Long catId, Long sellerCatId, int status,
            String orderBy, boolean isDesc, int pn, int ps) {
        
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps);
        
        String catIdStr = "";
        if (catId != null && catId > 0) {
            catIdStr = catId + "";
        }
        String sellerCatIdStr = "";
        if (sellerCatId != null && sellerCatId > 0) {
            sellerCatIdStr = sellerCatId + "";
        }
        
        final boolean isFenxiao = true;
        TMResult tmRes = ItemDao.findItemsBySearchRules(user, title, status, catIdStr, sellerCatIdStr, 
                isFenxiao, orderBy, isDesc, po);
                
        renderJSON(JsonUtil.getJson(tmRes));
    }
    
    
    public static void submitFenxiaoItemPrice(FenxiaoSubmitItemType itemType, 
            String selectNumIids, String title, Long catId, Long sellerCatId, int status,
            FenxiaoPriceConfig config) {
        
        User user = getUser();
        
        //log.info(JsonUtil.getJson(config));
        
        checkW2Expires(user);
        
        List<ItemPlay> targetItemList = new ArrayList<ItemPlay>();
        
        if (FenxiaoSubmitItemType.selectedItems.equals(itemType)) {
            Set<Long> numIidSet = UmpPromotion.parseIdsToSet(selectNumIids);
            if (CommonUtils.isEmpty(numIidSet)) {
                renderFailedJson("请先选择要改价的宝贝！");
            }
            targetItemList = ItemDao.findByNumIids(user.getId(), numIidSet);
        } else if (FenxiaoSubmitItemType.allSearchItem.equals(itemType)) {
            String catIdStr = "";
            if (catId != null && catId > 0) {
                catIdStr = catId + "";
            }
            String sellerCatIdStr = "";
            if (sellerCatId != null && sellerCatId > 0) {
                sellerCatIdStr = sellerCatId + "";
            }
            targetItemList = ItemDao.findItemsWithoutPaging(user.getId(), 
                    title, status, catIdStr, sellerCatIdStr, true);
            
        } else {
            renderError("系统出现异常，改价的宝贝类型出错，请联系我们！");
        }
        
        if (CommonUtils.isEmpty(targetItemList)) {
            renderError("找不到需要改价的宝贝！");
        }
        
        
        BatchEditResult fenxiaoRes = FenxiaoPriceEditAction.doEditFenxiaoPrice(user, 
                targetItemList, config);
        
        if (fenxiaoRes.isSuccess() == false) {
            renderError(fenxiaoRes.getMessage());
        }
        
        renderResultJson(fenxiaoRes);
    }
    
    
    private static void checkW2Expires(User user) {
        
        UmpPromotion.checkW2Expires(user);
        
    }
    
}
