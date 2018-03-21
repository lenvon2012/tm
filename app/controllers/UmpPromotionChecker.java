package controllers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.With;
import tbapi.ump.UMPApi.UmpSingleItemActivityDelete;
import utils.TBItemUtil;
import actions.ump.PromotionResult;
import actions.ump.UmpMjsAction;
import actions.ump.MjsModuleTemplateAction.ItemDescModule;
import actions.ump.PromotionResult.PromotionErrorMsg;
import bustbapi.ItemApi;
import bustbapi.FenxiaoApi.FXScItemApi;

import com.ciaosir.client.utils.JsonUtil;
import com.taobao.api.domain.FenxiaoProduct;
import com.taobao.api.domain.Item;

import dao.item.ItemDao;

@With(Secure.class)
public class UmpPromotionChecker extends TMController {

    private static final Logger log = LoggerFactory
            .getLogger(UmpPromotion.class);
    
    
    public static void deleteTaobaoPromotion(Long promotionId) {
        if (promotionId == null || promotionId <= 0L) {
            renderError("id出错");
        }
        
        User user = getUser();
        
        UmpSingleItemActivityDelete deleteApi = new UmpSingleItemActivityDelete(user, 
                promotionId);
        
        Boolean isSuccess = deleteApi.call();
        if (isSuccess == null || isSuccess.booleanValue() == false || deleteApi.isApiSuccess() == false) {
            PromotionErrorMsg promotionError = PromotionResult.createPromotionError(deleteApi, 
                    0L);
            
            renderResultJson(promotionError);
        } else {
            renderTMSuccess("成功");
        }
        
        
    }
    
    public static void moduleTest(Long numIid) {
        
        User user = getUser();
        
        Item tbItem = new ItemApi.ItemDescModulesGet(user, numIid).call();
        
        String descModules = tbItem.getDescModules();
        
        log.warn(descModules);
        
        ItemDescModule[] moduleArray = JsonUtil.toObject(descModules, ItemDescModule[].class);
        if (moduleArray == null) {
            log.error("fail to parse ItemDescModule json: " + descModules);
            return;
        }
        
        final String tmplHtml = "<div>1111</div>";
        final Long activityId = 1001L;
        boolean isDeleteTemplate = false;
        boolean hasInsertTmpl = false;
        for (int i = 0; i < moduleArray.length; i++) {
            
            ItemDescModule module = moduleArray[i];
            String content = module.getContent();
            if (StringUtils.isEmpty(content)) {
                content = "";
            }
            
            if (isDeleteTemplate == false && hasInsertTmpl == false && module.isRequired() == true) {
                //更新模板
                String newContent = UmpMjsAction.addMjsTmpl(user, tmplHtml.trim(), activityId) 
                        + UmpMjsAction.deleteMjsTmpl(content, activityId);
                module.setContent(newContent);
                
                hasInsertTmpl = true;
                
            } else {
                //删除模板
                String newContent = UmpMjsAction.deleteMjsTmpl(content, activityId);
                module.setContent(newContent);
            }
        }
        
        descModules = JsonUtil.getJson(moduleArray);
        
        log.info(descModules);
    }
    
    
    /*
    public static void getItemFenxiaoInfo(String numIids) {
        User user = getUser();
        
        if (StringUtils.isEmpty(numIids)) {
            renderError("请先输入宝贝ID");
        }
        
        Set<Long> numIidSet = UmpPromotion.parseIdsToSet(numIids);
        
        List<ItemPlay> itemList = ItemDao.findByNumIids(user.getId(), numIidSet);
        
        Map<Long, FenxiaoProduct> productMap = TBItemUtil.findFenxiaoProductMap(user, itemList);
        
        
        renderJSON(productMap);
    }
    
    public static void getOneItemFenxiao(Long numIid) {
        
        User user = getUser();
        
        if (numIid == null || numIid <= 0L) {
            renderError("请先输入宝贝ID");
        }
        
        FenxiaoProduct product = new FXScItemApi(user, numIid).call();
        
        renderJSON(product);
    }
    */
    
    /*
    
    public static void showItem(Long numIid) {
        
        User user = getUser();
        
        if (numIid == null || numIid <= 0L) {
            renderError("请先输入宝贝ID");
        }
        
        ItemGet api = new ItemGet(user, numIid, false);
        
        Item item = api.call();
        
        
        renderJSON(JsonUtil.getJson(item));
        
    }
    
    
    public static void showItemList(String numIids) {
        
        User user = getUser();
        
        if (StringUtils.isEmpty(numIids)) {
            renderError("请先输入宝贝ID");
        }
        
        Set<Long> numIidSet = UmpPromotion.parseIdsToSet(numIids);
        List<Long> numIidList = new ArrayList<Long>();
        numIidList.addAll(numIidSet);
        
        ItemApi.MultiItemsListGet itemGetApi = new ItemApi.MultiItemsListGet(user.getSessionKey(),
                numIidList, false);
        
        List<Item> itemList = itemGetApi.call();
        
        
        renderJSON(JsonUtil.getJson(itemList));
        
    }
    
    
    public static void showOnsaleItem() {
        
        User user = getUser();
        
        ItemsOnsalePage api = new ItemApi.ItemsOnsalePage(user, null, null, 1L, 20L);
        
        List<Item> itemList = api.call();
        
        
        renderJSON(JsonUtil.getJson(itemList));
        
    }
    
    */
    /*
    public static void setMinDiscountByOldWay(Long numIid) {
        User user = getUser();
        
        final long nowTime = System.currentTimeMillis();
        
        final long startDateTime = nowTime + DateUtil.DAY_MILLIS * 1000 * 400;
        final long endDateTime = startDateTime + 1000;
        
        final Date startDate = new Date(startDateTime);
        final Date endDate = new Date(endDateTime);
        
        final Long tmActivityId = 0L;
        
        final String title = "店铺最低";
        final String description = "";
        final Long decreaseNumber = 1L;
        final Long tagId = 1L;
        final String discountType = "DISCOUNT";
        final String discountValue = "0.01";
        
        
        PromotionAddApi api = new PromotionAddApi(user, tmActivityId, numIid + "", 
                discountType, discountValue, 
                startDate,
                endDate, title, tagId, description, decreaseNumber);
        
        List<Promotion> promotionList = api.call();
        
        log.error(JsonUtil.getJson(promotionList));
        
//        try {
//            promotionList = PromotionAction.getPromotions(user.getSessionKey(), new Long[] {numIid});
//            log.error(JsonUtil.getJson(promotionList));
//        } catch (Exception ex) {
//            log.error(ex.getMessage(), ex);
//        }
    }
    */
}
