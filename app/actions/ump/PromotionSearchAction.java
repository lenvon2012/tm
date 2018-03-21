package actions.ump;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.item.ItemPlay;
import models.ump.PromotionPlay;
import models.ump.PromotionPlay.PromotionParams;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actions.promotion.PromotionAction;

import com.ciaosir.client.CommonUtils;

import dao.ump.PromotionDao;

public class PromotionSearchAction {
    
    private static final Logger log = LoggerFactory.getLogger(PromotionSearchAction.class);

    protected static Map<Long, PromotionPlay> findAllOnActivePromotionNumIidMap(User user, 
            List<PromotionParams> paramsList) {
        
        if (CommonUtils.isEmpty(paramsList)) {
            return new HashMap<Long, PromotionPlay>();
        }
        
        Set<Long> numIidSet = new HashSet<Long>();
        for (PromotionParams params : paramsList) {
            if (params == null) {
                continue;
            }
            numIidSet.add(params.getNumIid());
        }
        
        Map<Long, PromotionPlay> promotionMap = new HashMap<Long, PromotionPlay>();
        
        List<PromotionPlay> promotionList = PromotionDao.findAllOnActiveByNumIids(user.getId(), numIidSet);
        
        for (PromotionPlay promotion : promotionList) {
            if (promotion == null) {
                continue;
            }
            if (promotionMap.containsKey(promotion.getNumIid())) {
                log.error("the activity has more than one promotions for the same item numIid for user: " 
                        + user.getUserNick() + "--------------------");
                
                if (promotion.isActive() == false) {
                    continue;
                }
                
            }
            promotionMap.put(promotion.getNumIid(), promotion);
        }
        
        return promotionMap;
        
    }
    
    
    protected static Map<Long, PromotionPlay> findPromotionNumIidMapByParams(
            User user, Long tmActivityId,
            List<PromotionParams> paramsList) {
        
        if (CommonUtils.isEmpty(paramsList)) {
            return new HashMap<Long, PromotionPlay>();
        }
        
        Set<Long> numIidSet = new HashSet<Long>();
        for (PromotionParams params : paramsList) {
            if (params == null) {
                continue;
            }
            numIidSet.add(params.getNumIid());
        }
        
        Map<Long, PromotionPlay> promotionNumIidMap = findActivityPromotionNumIidMap(user, tmActivityId,
                numIidSet);
        
        
        return promotionNumIidMap;
    }
    
    public static Map<Long, PromotionPlay> findActivityPromotionNumIidMap(
            User user, Long tmActivityId, Set<Long> numIidSet) {
        if (CommonUtils.isEmpty(numIidSet)) {
            return new HashMap<Long, PromotionPlay>();
        }
        
        List<PromotionPlay> promotionList = PromotionDao.findByNumIidsInOneActivity(user.getId(), 
                tmActivityId, numIidSet);
        

        Map<Long, PromotionPlay> promotionMap = new HashMap<Long, PromotionPlay>();

        for (PromotionPlay promotion : promotionList) {
            if (promotion == null) {
                continue;
            }
            if (promotionMap.containsKey(promotion.getNumIid())) {
                log.error("the activity has more than one promotions for the same item numIid for user: " 
                        + user.getUserNick() + "--------------------");
                
                if (promotion.isActive() == false) {
                    continue;
                }
                
            }
            promotionMap.put(promotion.getNumIid(), promotion);
        }

        return promotionMap;
    }
    
    public static Map<Long, PromotionPlay> findActivityPromotionNumIidMap(
            User user, Long tmActivityId, List<ItemPlay> itemList) {

        if (CommonUtils.isEmpty(itemList)) {
            return new HashMap<Long, PromotionPlay>();
        }

        Set<Long> numIidSet = new HashSet<Long>();
        for (ItemPlay item : itemList) {
            numIidSet.add(item.getNumIid());
        }

        return findActivityPromotionNumIidMap(user, tmActivityId, numIidSet);
        
    }
    
    
    public static Set<Long> findOldPromotionedNumIidSet(User user, List<ItemPlay> itemList) {
        
        if (CommonUtils.isEmpty(itemList)) {
            return new HashSet<Long>();
        }
        
        Set<Long> numIidSet = new HashSet<Long>();
        for (ItemPlay item : itemList) {
            if (item == null) {
                continue;
            }
            numIidSet.add(item.getNumIid());
        }
        
        Set<Long> promotionNumIidSet = PromotionAction.findPromotionedNumIids(user.getId(), numIidSet);
        
        return promotionNumIidSet;
    }
    
}
