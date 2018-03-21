package job.autolist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.autolist.AutoListTime;
import models.autolist.AutoListTime.DelistState;
import models.autolist.NoAutoListItem;
import models.item.ItemPlay;
import models.user.User;
import utils.DateUtil;

import com.ciaosir.client.CommonUtils;

import dao.autolist.AutoListTimeDao;
import dao.item.ItemDao;

public class BackToOldDelistAction {
    
    public static boolean doBackToOldDelist(User user) {
        //先得到所有上线宝贝
        List<ItemPlay> itemList = ItemDao.findOnSaleByUserId(user.getId());
        
        if (CommonUtils.isEmpty(itemList)) {
            itemList = new ArrayList<ItemPlay>();
        }
        
        List<AutoListTime> listTimeList = AutoListTimeDao.queryAllAutoListTime(user.getId());        
        if (CommonUtils.isEmpty(listTimeList)) {
            listTimeList = new ArrayList<AutoListTime>();
        }
        
        Map<Long, AutoListTime> listTimeMap = parseToMap(listTimeList);
        
        Set<Long> noAutoListItems = NoAutoListItem.findIdsByUser(user.getId(), models.autolist.AutoListTime.DefaultPlanId);
        if (CommonUtils.isEmpty(noAutoListItems)) {
            noAutoListItems = new HashSet<Long>();
        }
        
        for (ItemPlay item : itemList) {
            if (item == null) {
                continue;
            }
            
            
            Long numIid = item.getNumIid();
            
            if (noAutoListItems.contains(numIid)) {
                continue;
            }
            
            AutoListTime listTime = listTimeMap.get(numIid);
            
            if (listTime == null) {
                createNewAutoListTime(user, item);
                continue;
            } else {
                setOldDelist(listTime, item);
                listTimeMap.remove(numIid);
            }
            
        }
        
        for (AutoListTime listTime : listTimeMap.values()) {
            AutoListTimeDao.deleteAutoListTime(listTime);
        }
        
        return true;
    }
    
    public static AutoListTime createNewAutoListTime(User user, ItemPlay item) {
        
        AutoListTime listTime = AutoListTime.createAutoListTime(user.getId(), 
                item.getNumIid(), 0L, AutoListTime.DefaultPlanId);
        
        setOldDelist(listTime, item);
        
        listTime.jdbcSave();
        
        
        return listTime;
    }
    
    private static void setOldDelist(AutoListTime listTime, ItemPlay item) {
        
        if (listTime == null) {
            return;
        }
        
        listTime.setStatus(DelistState.Success);
        
        long time = item.getDeListTime() - DateUtil.DAY_MILLIS * 7;
        
        listTime.setListTime(time);
        
        
        long weekStart = DateUtil.findThisWeekStart(time);
        
        listTime.setRelativeListTime(time - weekStart);
        
        listTime.jdbcSave();
    }
    
    private static Map<Long, AutoListTime> parseToMap(List<AutoListTime> listTimeList) {
        
        Map<Long, AutoListTime> listTimeMap = new HashMap<Long, AutoListTime>();
        
        for (AutoListTime listTime : listTimeList) {
            if (listTime == null) {
                continue;
            }
            listTimeMap.put(listTime.getNumIid(), listTime);
        }
        
        return listTimeMap;
        
    }
    
    
    
}
