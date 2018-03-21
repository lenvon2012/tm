package actions.delist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.autolist.AutoListTime;
import models.autolist.NoAutoListItem;
import models.autolist.plan.UserDelistPlan;
import models.item.ItemPlay;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;

import dao.autolist.AutoListTimeDao;
import dao.item.ItemDao;

public class DelistFilterAction {

    private static final Logger log = LoggerFactory.getLogger(DelistCalculateAction.class);
    
    /**
     * 排除销量前10的宝贝
     * @param itemList
     * @return
     */
    public static List<ItemPlay> filterGoodSalesItems(User user, List<ItemPlay> itemList, UserDelistPlan delistPlan) {
        
        if (CommonUtils.isEmpty(itemList)) {
            itemList = new ArrayList<ItemPlay>();
        }
        
        if (delistPlan.isFilterGoodSalesItem() == false) {
            return itemList;
        }
        
        int limit = 10;
        int minSales = 1;
        
        List<ItemPlay> salesItemList = ItemDao.findOnlineWithSalesDesc(user.getId(), 0, limit);
        
        Set<Long> filterNumIidSet = new HashSet<Long>();
        for (ItemPlay item : salesItemList) {
            if (item == null) {
                continue;
            }
            if (item.getSalesCount() >= minSales) {
                filterNumIidSet.add(item.getNumIid());
            }
        }
        
        return filterByNumIids(itemList, filterNumIidSet);
        
    }
    
    
    /**
     * 排除已经被分布的宝贝
     * @param user
     * @param itemList
     * @return
     */
    public static List<ItemPlay> filterExistItems(User user, List<ItemPlay> itemList) {
        
        Set<Long> numIidSet = AutoListTimeDao.findNumIidsByUserId(user.getId());
        
        return filterByNumIids(itemList, numIidSet);
        
    }
    
    
    
    /**
     * 过滤不自动上下架的宝贝
     * @param user
     * @param itemList
     * @return
     */
    public static List<ItemPlay> filterNotDelistItems(User user, List<ItemPlay> itemList, UserDelistPlan delistPlan) {
        Set<Long> noDelistNumIidSet = NoAutoListItem.findNumIidsByUser(user.getId(), delistPlan.getPlanId());
        
        return filterByNumIids(itemList, noDelistNumIidSet);
        
    }
    
    
    private static List<ItemPlay> filterByNumIids(List<ItemPlay> itemList, Set<Long> numIidSet) {
        if (CommonUtils.isEmpty(numIidSet)) {
            numIidSet = new HashSet<Long>();
        }
        
        if (CommonUtils.isEmpty(itemList)) {
            itemList = new ArrayList<ItemPlay>();
        }
        
        Iterator<ItemPlay> iterator = itemList.iterator();
        while (iterator.hasNext()) {
            ItemPlay item = iterator.next();
            if (item == null) {
                iterator.remove();
                continue;
            }
            if (numIidSet.contains(item.getNumIid())) {
                iterator.remove();
                continue;
            }
        }
        
        return itemList;
    }
    
    

    
    private static Map<Long, ItemPlay> toItemMap(List<ItemPlay> itemList) {
        Map<Long, ItemPlay> itemMap = new HashMap<Long, ItemPlay>();
        if (CommonUtils.isEmpty(itemList)) {
            return itemMap;
        }
        
        for (ItemPlay item : itemList) {
            if (item == null) {
                continue;
            }
            itemMap.put(item.getNumIid(), item);
        }
        return itemMap;
    }
    
    
    
    /**
     * 删除一些已经不存在了的宝贝的上下架
     * @param user
     * @param delistPlan
     * @return
     */
    public static List<AutoListTime> deleteSomeHistoryDelist(User user, UserDelistPlan delistPlan, List<ItemPlay> allItemList, 
            List<AutoListTime> delistList) {
        
        if (CommonUtils.isEmpty(allItemList)) {
            allItemList = new ArrayList<ItemPlay>();
        }
        if (CommonUtils.isEmpty(delistList)) {
            delistList = new ArrayList<AutoListTime>();
        }
        
        Map<Long, ItemPlay> itemMap = toItemMap(allItemList);
        
        Iterator<AutoListTime> iterator = delistList.iterator();
        
        while(iterator.hasNext()) {
            AutoListTime delist = iterator.next();
            if (delist == null) {
                iterator.remove();
                continue;
            }
            ItemPlay item = itemMap.get(delist.getNumIid());
            if (item == null) {
                AutoListTimeDao.deleteAutoListTime(delist);
                iterator.remove();
                continue;
            }
        }
        
        
        
        return delistList;
        
    }
    
    
    
    
    
}
