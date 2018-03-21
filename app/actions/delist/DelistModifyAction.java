package actions.delist;

import java.util.List;
import java.util.Set;

import models.autolist.AutoListTime;
import models.autolist.NoAutoListItem;
import models.autolist.plan.UserDelistPlan;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actions.delist.DelistScheduleAction.DelistScheduleLog;

import com.ciaosir.client.CommonUtils;

import dao.autolist.AutoListTimeDao;

public class DelistModifyAction {

    private static final Logger log = LoggerFactory.getLogger(DelistModifyAction.class);
    
    public static DelistScheduleLog addNoDelistItems(User user, UserDelistPlan delistPlan, Set<Long> numIidSet) {
        
        if (CommonUtils.isEmpty(numIidSet)) {
            return new DelistScheduleLog(false, "请先选择要排除的宝贝！");
        }
        
        for (Long numIid : numIidSet) {
            if (numIid == null || numIid <= 0L) {
                continue;
            }
            NoAutoListItem.add(user, numIid, delistPlan.getPlanId());
            
        }
        
        return deleteDelists(user, delistPlan, numIidSet);
        
    }
    
    public static DelistScheduleLog deleteDelists(User user, UserDelistPlan delistPlan, Set<Long> numIidSet) {
        
        List<AutoListTime> needDeleteList = AutoListTimeDao.queryByNumIidsAndPlanId(user.getId(), numIidSet, delistPlan.getPlanId());

        if (CommonUtils.isEmpty(needDeleteList)) {
            return new DelistScheduleLog(true);
        }
        
        AutoListTimeDao.deleteAutoListTimeByNumIids(user.getId(), delistPlan.getPlanId(), numIidSet);
        
        int[] hourNumArray = DelistCalculateAction.parseToHourArray(delistPlan.getDistriNums());
        
        
        for (AutoListTime delist : needDeleteList) {
            if (delist == null) {
                continue;
            }
            long relativeTime = delist.getRelativeListTime();
            int hourIndex = DelistCalculateAction.getRelativeHour(relativeTime);
            if (hourNumArray[hourIndex] <= 0) {
                continue;
            } else {
                hourNumArray[hourIndex]--;
            }
        }
        
        String distriNums = DelistCalculateAction.arrToString(hourNumArray);
        delistPlan.setDistriNums(distriNums);
        
        delistPlan.jdbcSave();
        
        return new DelistScheduleLog(true);
    }
    
    public static DelistScheduleLog removeAllNoDelistItems(User user, UserDelistPlan delistPlan) {
        
        boolean isSuccess = NoAutoListItem.removeAll(user, delistPlan.getPlanId());
        if (isSuccess == false) {
            return new DelistScheduleLog(false, "清空排除宝贝失败，请联系我们！");
        }
        
        
        DelistUpdateAction.doUpdateOneDelistPlan(user, delistPlan);
        
        return new DelistScheduleLog(true);
    }
    
    public static DelistScheduleLog removeNoDelistItems(User user, UserDelistPlan delistPlan, Set<Long> numIidSet) {

        boolean isSuccess = NoAutoListItem.removeNumIids(user, numIidSet, delistPlan.getPlanId());
        if (isSuccess == false) {
            return new DelistScheduleLog(false, "删除排除宝贝失败，请联系我们！");
        }
        
        DelistUpdateAction.updateDelistPlan(user, delistPlan);
        
        return new DelistScheduleLog(true);
        
    }
    
    
    public static DelistScheduleLog modifyDelistTime(User user, UserDelistPlan delistPlan, long numIid, long newTime) {
        AutoListTime delist = AutoListTimeDao.queryByNumIid(user.getId(), delistPlan.getPlanId(), numIid);
        if (delist == null) {
            return new DelistScheduleLog(false, "修改失败，找不到对应的上下架计划");
        }
        
        long oldTime = delist.getRelativeListTime();
        
        delist.setRelativeListTime(newTime);
        delist.setListTime(0L);
        AutoListTimeDao.saveOrUpdateAutoListTime(delist);
        
        int[] hourNumArray = DelistCalculateAction.parseToHourArray(delistPlan.getDistriNums());
        
        int oldHour = DelistCalculateAction.getRelativeHour(oldTime);
        int newHour = DelistCalculateAction.getRelativeHour(newTime);
        
        hourNumArray[oldHour]--;
        
        hourNumArray[newHour]++;
        
        String distriNums = DelistCalculateAction.arrToString(hourNumArray);
        delistPlan.setDistriNums(distriNums);
        
        delistPlan.jdbcSave();
        
        return new DelistScheduleLog(true);
        
        
    }
    
    
}
