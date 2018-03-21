package actions.delist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.autolist.plan.UserDelistPlan;
import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.NumberUtil;

import dao.item.ItemDao;

/**
 * 上下架分布
 * @author ying
 *
 */
public class DelistScheduleAction {
    
    private static final Logger log = LoggerFactory.getLogger(DelistScheduleAction.class);

    public static DelistScheduleLog doSchedule(User user, UserDelistPlan delistPlan) {
        try {
            if (user == null) {
                return new DelistScheduleLog(false, "找不到用户，请联系我们！");
            }
            if (delistPlan == null) {
                return new DelistScheduleLog(false, "上下架计划为空，请联系我们！");
            }

            List<ItemPlay> itemList = findResultDelistItems(user, delistPlan);

            return DelistCalculateAction.doCalcuItemsDelist(user, delistPlan, itemList);

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new DelistScheduleLog(false, "上下架分布设置异常，请联系我们！");
        }

    }
    
    
    public static List<ItemPlay> findResultDelistItems(User user, UserDelistPlan delistPlan) {
        
        List<ItemPlay> itemList = null;
        
        if (delistPlan.isUserSelectItemType()) {
            String numIids = delistPlan.getSelectNumIids();
            Set<Long> numIidSet = toLongSet(numIids);
            if (CommonUtils.isEmpty(numIidSet)) {
                return new ArrayList<ItemPlay>();
            }
            itemList = ItemDao.findByIds(user.getId(), StringUtils.join(numIidSet, ","));

            
        } else if (delistPlan.isRuleItemType()) {
            
            itemList = findItemsByPlanRule(user, delistPlan);
            
        } else {
            return new ArrayList<ItemPlay>();
        }
        
        if (CommonUtils.isEmpty(itemList)) {
            return new ArrayList<ItemPlay>();
        }
        
        //过滤排除宝贝
        itemList = DelistFilterAction.filterNotDelistItems(user, itemList, delistPlan);
        
        if (delistPlan.isRuleItemType()) {
            //排除销量靠前的宝贝
            itemList = DelistFilterAction.filterGoodSalesItems(user, itemList, delistPlan);
        }
        
        return itemList;
    }
    
    
    private static Set<Long> toLongSet(String idStr) {
        Set<Long> idSet = new HashSet<Long>();
        if (StringUtils.isEmpty(idStr)) {
            return idSet;
        }
        
        String[] idArray = idStr.split(",");
        if (idArray == null || idArray.length <= 0) {
            return idSet;
        }
        
        for (String str : idArray) {
            long id = NumberUtil.parserLong(str, 0L);
            if (id <= 0) {
                continue;
            }
            idSet.add(id);
        }
        return idSet;
    }
    
    private static List<ItemPlay> findItemsByPlanRule(User user, UserDelistPlan delistPlan) {
        String sellerCids = delistPlan.getSelfCateIds();
        String itemCids = delistPlan.getDelistCateIds();
        
        Set<Long> sellerCidSet = new HashSet<Long>();
        Set<Long> itemCidSet = new HashSet<Long>();
        
        if (!StringUtils.isEmpty(sellerCids) && !sellerCids.equals(UserDelistPlan.AllCateIds)) {
            sellerCidSet = toLongSet(sellerCids);
        }
        if (!StringUtils.isEmpty(itemCids) && !itemCids.equals(UserDelistPlan.AllCateIds)) {
            itemCidSet = toLongSet(itemCids);
        }
        
        int status = UserDelistPlan.transItemStatus(delistPlan.getItemStatusRule());
        
        List<ItemPlay> itemList = new ArrayList<ItemPlay>();
        
        int offset = 0;
        final int limit = 10000;
        
        while (true) {
            
            
            List<ItemPlay> tempList = ItemDao.findForBatchDelist(user, sellerCidSet, 
                    itemCidSet, status, delistPlan.getSalesNumRule(), offset, limit);
            
            if (CommonUtils.isEmpty(tempList)) {
                break;
            }
            itemList.addAll(tempList);
            
            if (tempList.size() < limit) {
                break;
            }
            offset += limit;
            
            log.error("item size is big than " + limit + " for user: " + user.getUserNick() + "!!!!!!!!!!!!!!!!!!!");
        }
        
        
        return itemList;
    }
    
    
    
    public static class DelistScheduleLog {
        private boolean isSuccess;
        private String message;
        
        
        public DelistScheduleLog(boolean isSuccess, String message) {
            super();
            this.isSuccess = isSuccess;
            this.message = message;
        }
        
        public DelistScheduleLog(boolean isSuccess) {
            super();
            this.isSuccess = isSuccess;
        }
        
        
        public boolean isSuccess() {
            return isSuccess;
        }

        public void setSuccess(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
        
        
        
    }
    
}
