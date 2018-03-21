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
import actions.onekey.SellerCatItemAction;
import actions.onekey.SellerCatItemAction.ShopSellerCatsCache;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.dbt.cred.utils.JsonUtil;
import com.taobao.api.domain.SellerCat;

import dao.item.ItemDao;

public class SellerCatItems extends TMController {

    private static final Logger log = LoggerFactory.getLogger(SellerCatItems.class);
    
    public  static  void index(){
        render("/newAutoTitle/sellercatitems.html");
    }
    
    public static void searchItemsByRules(String title, int itemStatus, long tbCid, long sellerCid,
            String orderBy, boolean isDesc, int pn, int ps) {
        
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps);
        
        String catIdStr = "";
        if (tbCid > 0) {
            catIdStr = tbCid + "";
        }
        String sellerCatIdStr = "";
        if (sellerCid != 0L) {
            sellerCatIdStr = sellerCid + "";
        }
        
        final boolean isFenxiao = false;
        TMResult tmRes = ItemDao.findItemsBySearchRules(user, title, itemStatus, catIdStr, sellerCatIdStr, 
                isFenxiao, orderBy, isDesc, po);
                
        renderJSON(JsonUtil.getJson(tmRes));
    }
    
    public static void clearSellerCatsCache() {
        User user = getUser();
        ShopSellerCatsCache.clearCache(user.getId());
    }
    
    public static void getShopSellerCats() {
        
        User user = getUser();
        
        List<SellerCat> sellerCatList = SellerCatItemAction.getShopChildSellerCats(user);
        
        if (CommonUtils.isEmpty(sellerCatList)) {
            sellerCatList = new ArrayList<SellerCat>();
        }
        
        renderJSON(JsonUtil.getJson(sellerCatList));
    }
    
    public static void countSubmitItems(String title, int itemStatus, long tbCid, long sellerCid) {
        User user = getUser();
        
        String catIdStr = "";
        if (tbCid > 0) {
            catIdStr = tbCid + "";
        }
        String sellerCatIdStr = "";
        if (sellerCid < 0) {
            sellerCid = -1;
        }
        if (sellerCid != 0) {
            sellerCatIdStr = sellerCid + "";
        }
        
        long count = ItemDao.countBySearchRules(user.getId(), 
                title, itemStatus, catIdStr, sellerCatIdStr, false);
        
        renderResultJson(count);
    }
    
    public static void setSellerCatItems(boolean isSelectItems, String selectNumIids, 
            String title, int itemStatus, long tbCid, long sellerCid,
            long targetSellerCid, boolean isRemoveOriginSellerCat) {
        
        if (targetSellerCid <= 0) {
            renderError("请先选择目标店铺分类！");
        }
        if (sellerCid == targetSellerCid) {
            renderError("目标店铺分类不能与原来店铺分类相同！");
        }
        
        User user = getUser();
        List<ItemPlay> targetItemList = null;
        
        if (isSelectItems == true) {
            Set<Long> numIidSet = UmpPromotion.parseIdsToSet(selectNumIids);
            if (CommonUtils.isEmpty(numIidSet)) {
                renderError("请先选择要分类的宝贝！");
            }
            targetItemList = ItemDao.findByNumIids(user.getId(), numIidSet);
        } else {
            String catIdStr = "";
            if (tbCid > 0) {
                catIdStr = tbCid + "";
            }
            String sellerCatIdStr = "";
            if (sellerCid < 0) {
                sellerCid = -1;
            }
            if (sellerCid != 0) {
                sellerCatIdStr = sellerCid + "";
            }
            targetItemList = ItemDao.findItemsWithoutPaging(user.getId(), 
                    title, itemStatus, catIdStr, sellerCatIdStr, false);
        }
        
        if (CommonUtils.isEmpty(targetItemList)) {
            renderError("找不到要分类的宝贝！");
        }
        
        BatchEditResult updateRes = SellerCatItemAction.doSubmitSellerCatItems(user, targetItemList, 
                targetSellerCid, isRemoveOriginSellerCat);
        
        if (updateRes.isSuccess() == false) {
            renderError(updateRes.getMessage());
        }
        
        renderResultJson(updateRes);
    }
    
    
}
