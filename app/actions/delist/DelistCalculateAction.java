package actions.delist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import models.autolist.AutoListTime;
import models.autolist.plan.UserDelistPlan;
import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.DateUtil;
import actions.delist.DelistScheduleAction.DelistScheduleLog;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.NumberUtil;

import dao.autolist.AutoListTimeDao;

/**
 * 计算上下架分布
 * @author ying
 *
 */
public class DelistCalculateAction {
    
    private static final Logger log = LoggerFactory.getLogger(DelistCalculateAction.class);
    
    public static DelistScheduleLog doCalcuItemsDelist(User user, UserDelistPlan delistPlan, List<ItemPlay> itemList) {
        try {
            
            //当前的分布
            List<AutoListTime> delistList = AutoListTimeDao.findListTimeByPlanId(delistPlan.getPlanId(), user.getId());
            if (CommonUtils.isEmpty(delistList)) {
                delistList = new ArrayList<AutoListTime>();
            }
            
            //
            if (delistPlan.isRuleItemType()) {
                //从计划中删除不存在的宝贝
                List<ItemPlay> allItemList = itemList;
                delistList = DelistFilterAction.deleteSomeHistoryDelist(user, delistPlan, allItemList, delistList);
                
                
            } else if (delistPlan.isUserSelectItemType()) {
                
                if (CommonUtils.isEmpty(delistList) == false) {
                    //从计划中删除不存在的宝贝
                    //List<ItemPlay> allItemList = ItemDao.findOnSaleByUserId(user.getId());
                    //delistList = DelistFilterAction.deleteSomeHistoryDelist(user, delistPlan, allItemList, delistList);
                }
                
            }
            
            //过滤已分布的宝贝，注意过滤顺序，这个不能在删除之前
            List<ItemPlay> needAddItemList = DelistFilterAction.filterExistItems(user, itemList);
            
            //获取当前的上下架分布情况
            int[] curExistArray = calcuDistriNumArray(delistList);
            String distriNums = arrToString(curExistArray);
            delistPlan.setDistriNums(distriNums);
            
            boolean isSuccess = delistPlan.jdbcSave();
            if (isSuccess == false) {
                return new DelistScheduleLog(false, "上下架计划更新分布情况出错，请联系我们！");
            }
            
            if (CommonUtils.isEmpty(needAddItemList)) {
                return new DelistScheduleLog(false, "找不到要设置上下架分布的宝贝，可能已经添加在其他计划中了！");
            }
            
            //分布比例
            int[] hourRateArray = parseToHourArray(delistPlan.getHourRates());
            
            int totalNum = needAddItemList.size() + delistList.size();
            int[] goodDistriArray = GoodDistriAction.genGoodDistriArray(hourRateArray, totalNum);
            
            int[] resultDistriArray = GoodDistriAction.calcuResultAddDistriArray(goodDistriArray, curExistArray);
            
            
            return buildNewDelistList(user, delistPlan, delistList, needAddItemList, resultDistriArray);
            
            
        } catch (Exception ex) {
            
            log.error(ex.getMessage(), ex);
            
            return new DelistScheduleLog(false, "上下架分布时间出现异常，请联系我们！");
        }
        
    } 
    
    
    private static Map<Integer, List<AutoListTime>> toHourDelistMap(List<AutoListTime> oldDelistList) {
        Map<Integer, List<AutoListTime>> delistMap = new HashMap<Integer, List<AutoListTime>>();
        
        if (CommonUtils.isEmpty(oldDelistList)) {
            return delistMap;
        }
        
        for (AutoListTime delist : oldDelistList) {
            if (delist == null) {
                continue;
            }
            
            int relativeHour = getRelativeHour(delist.getRelativeListTime());
            
            List<AutoListTime> tempList = delistMap.get(relativeHour);
            if (CommonUtils.isEmpty(tempList)) {
                tempList = new ArrayList<AutoListTime>();
            }
            tempList.add(delist);
            
            delistMap.put(relativeHour, tempList);
        }
        
        return delistMap;
    }
    
    private static Map<Integer, List<ItemPlay>> toHourItemMap(List<ItemPlay> needAddItemList) {
        Map<Integer, List<ItemPlay>> itemMap = new HashMap<Integer, List<ItemPlay>>();
        
        if (CommonUtils.isEmpty(needAddItemList)) {
            return itemMap;
        }
        
        for (ItemPlay item : needAddItemList) {
            if (item == null) {
                continue;
            }
            
            long delistTime = item.getDeListTime();
            long relativeTime = 0;
            if (delistTime > 0) {
                relativeTime = delistTime - DateUtil.findThisWeekStart(delistTime);
            }
            
            int relativeHour = getRelativeHour(relativeTime);
            
            List<ItemPlay> tempList = itemMap.get(relativeHour);
            if (CommonUtils.isEmpty(tempList)) {
                tempList = new ArrayList<ItemPlay>();
            }
            tempList.add(item);
            
            itemMap.put(relativeHour, tempList);
        }
        
        return itemMap;
    }
    
    private static Map<Integer, List<ItemPlay>> distributeItemsToHour(List<ItemPlay> needAddItemList, 
            int[] resultDistriArray) {
        
        Map<Integer, List<ItemPlay>> itemMap = toHourItemMap(needAddItemList);
        
        //将item对应到每个小时上
        List<ItemPlay> changeItemList = new ArrayList<ItemPlay>();
        for (int hourIndex = 0; hourIndex < resultDistriArray.length; hourIndex++) {
            List<ItemPlay> tempList = itemMap.get(hourIndex);
            if (CommonUtils.isEmpty(tempList)) {
                tempList = new ArrayList<ItemPlay>();
            }
            int distriNum = resultDistriArray[hourIndex];
            
            if (tempList.size() > distriNum) {
                List<ItemPlay> frontList = tempList.subList(0, distriNum);
                List<ItemPlay> tailList = tempList.subList(distriNum, tempList.size());
                
                itemMap.put(hourIndex, frontList);
                changeItemList.addAll(tailList);
            }
        }
        
        for (int hourIndex = 0; hourIndex < resultDistriArray.length; hourIndex++) {
            
            if (CommonUtils.isEmpty(changeItemList)) {
                break;
            }
            
            List<ItemPlay> tempList = itemMap.get(hourIndex);
            if (CommonUtils.isEmpty(tempList)) {
                tempList = new ArrayList<ItemPlay>();
            }
            int distriNum = resultDistriArray[hourIndex];
            
            int needNum = distriNum - tempList.size();
            if (needNum > 0) {
                if (needNum >= changeItemList.size()) {
                    tempList.addAll(changeItemList);
                    changeItemList = new ArrayList<ItemPlay>();
                } else {
                    tempList.addAll(changeItemList.subList(0, needNum));
                    changeItemList = changeItemList.subList(needNum, changeItemList.size());
                }
            }
            
            itemMap.put(hourIndex, tempList);
        }
        
        return itemMap;
        
    }
    
    
    
    private static DelistScheduleLog buildNewDelistList(User user, UserDelistPlan delistPlan, 
            List<AutoListTime> oldDelistList, List<ItemPlay> needAddItemList, int[] resultDistriArray) {
        
        Map<Integer, List<AutoListTime>> delistMap = toHourDelistMap(oldDelistList);
        
        List<AutoListTime> totalDelistList = new ArrayList<AutoListTime>();
        totalDelistList.addAll(oldDelistList);
        
        Map<Integer, List<ItemPlay>> itemMap = distributeItemsToHour(needAddItemList, resultDistriArray);
        
        
        int hourCount = 7 * 24;
        
        for (int hourIndex = 0; hourIndex < hourCount; hourIndex++) {
            List<ItemPlay> hourItemList = itemMap.get(hourIndex);
            
            if (CommonUtils.isEmpty(hourItemList)) {
                hourItemList = new ArrayList<ItemPlay>();
                continue;
            }
            
            List<AutoListTime> hourDelistList = delistMap.get(hourIndex);
            if (CommonUtils.isEmpty(hourDelistList)) {
                hourDelistList = new ArrayList<AutoListTime>();
            }
                    
            long baseRelativeTime = hourIndex * DateUtil.ONE_HOUR_MILLIS;
            
            int[] secondNumArray = parseToDistriSecondArray(hourDelistList);
            
            int hourItemCount = hourItemList.size();
            for (int i = 0; i < hourItemCount; i++) {
                
                DelistSecondInterval interval = findNextSecondInterval(secondNumArray, 0, 3599);
                
                int startSecond = interval.getStartSecond();
                int endSecond = interval.getEndSecond();
                
                ItemPlay targetItem = null;
                int targetSecondIndex = 0;
                for (ItemPlay tempItem : hourItemList) {
                    
                    if (tempItem == null) {
                        continue;
                    }
                    long delistTime = tempItem.getDeListTime();
                    long relativeTime = 0;
                    if (delistTime > 0) {
                        relativeTime = delistTime - DateUtil.findThisWeekStart(delistTime);
                    } else {
                        continue;
                    }
                    
                    int relativeHour = getRelativeHour(relativeTime);
                    if (relativeHour == hourIndex) {
                        int secondIndex = (int) ((relativeTime - relativeHour * DateUtil.ONE_HOUR_MILLIS) / 1000);
                        if (secondIndex >= startSecond && secondIndex <= endSecond) {
                            targetItem = tempItem;
                            targetSecondIndex = secondIndex;
                            break;
                        }
                    }
                }
                
                
                if (targetItem == null) {
                    targetItem = hourItemList.get(0);
                    Random rand = new Random();
                    targetSecondIndex = rand.nextInt(endSecond - startSecond + 1) + startSecond;
                } 

                hourItemList.remove(targetItem);
                
                secondNumArray[targetSecondIndex]++;
                
                long resultRelativeTime = baseRelativeTime + targetSecondIndex * 1000;
                AutoListTime result = AutoListTime.createAutoListTime(user.getId(), 
                        targetItem.getNumIid(), resultRelativeTime, delistPlan.getPlanId());
                boolean isSuccess = AutoListTimeDao.saveOrUpdateAutoListTime(result);
                
                if (isSuccess == true) {
                    totalDelistList.add(result);
                    
                }
                
            }
            
        }
        
        int[] hourDistriArray = calcuDistriNumArray(totalDelistList);
        String distriNums = arrToString(hourDistriArray);
        delistPlan.setDistriNums(distriNums);
        
        boolean isSuccess = delistPlan.jdbcSave();
        if (isSuccess == false) {
            return new DelistScheduleLog(false, "上下架计划更新分布情况出错，请联系我们！");
        }
        
        
        return new DelistScheduleLog(true);
    }
    
    
    
    // 0到3599，包含endSecond的
    private static DelistSecondInterval findNextSecondInterval(int[] secondNumArray, int startSecond, int endSecond) {
        
        if (endSecond - startSecond <= 0) {
            return new DelistSecondInterval(startSecond, startSecond);
        }
        
        boolean isAllZero = true;
        for (int index = startSecond; index <= endSecond; index++) {
            if (secondNumArray[index] > 0) {
                isAllZero = false;
            }
        }
        
        if (isAllZero == true) {
            return new DelistSecondInterval(startSecond, endSecond);
        }
        
        int middle = (startSecond + endSecond) / 2;
        
        int frontSize = 0;
        int tailSize = 0;
        for (int index = startSecond; index <= endSecond; index++) {
            if (index <= middle) {
                frontSize += secondNumArray[index];
            } else {
                tailSize += secondNumArray[index];
            }
        }
        
        if (frontSize > tailSize) {
            return findNextSecondInterval(secondNumArray, middle + 1, endSecond);
        } else {
            return findNextSecondInterval(secondNumArray, startSecond, middle);
        }
        
    }
    
    
    
    /**
     * 上下架时间允许的区间
     * @author ying
     *
     */
    private static class DelistSecondInterval {
        private int startSecond;
        private int endSecond;
        public int getStartSecond() {
            return startSecond;
        }
        public void setStartSecond(int startSecond) {
            this.startSecond = startSecond;
        }
        public int getEndSecond() {
            return endSecond;
        }
        public void setEndSecond(int endSecond) {
            this.endSecond = endSecond;
        }
        public DelistSecondInterval(int startSecond, int endSecond) {
            super();
            this.startSecond = startSecond;
            this.endSecond = endSecond;
        }
        
        
        
    }
    
    
    //将已有的上下架计划，分布到秒的数组
    private static int[] parseToDistriSecondArray(List<AutoListTime> hourDelistList) {
        int arrLength = 60 * 60;
        
        int[] secondNumArray = new int[arrLength];
        
        for (int i = 0; i< secondNumArray.length; i++) {
            secondNumArray[i] = 0;
        }
        
        if (CommonUtils.isEmpty(hourDelistList)) {
            hourDelistList = new ArrayList<AutoListTime>();
        }
        
        for (AutoListTime delist : hourDelistList) {
            if (delist == null) {
                continue;
            }
            
            long relativeTime = delist.getRelativeListTime();
            int relativeHour = getRelativeHour(relativeTime);
            
            int secondIndex = (int) ((relativeTime - relativeHour * DateUtil.ONE_HOUR_MILLIS) / 1000);
            secondNumArray[secondIndex]++;
            
        }
        
        return secondNumArray;
        
    }
    
    
    public static int getRelativeHour(long relativeTime) {
        
        int relativeHour = (int) (relativeTime / DateUtil.ONE_HOUR_MILLIS);
        return relativeHour;
    }
    
    private static int[] calcuDistriNumArray(List<AutoListTime> delistList) {
        int arrayLength = 7 * 24;
        int[] distriNumArray = new int[arrayLength];
        
        for (int i = 0; i < distriNumArray.length; i++) {
            distriNumArray[i] = 0;
        }
        
        if (CommonUtils.isEmpty(delistList)) {
            return distriNumArray;
        }
        
        for (AutoListTime delist : delistList) {
            if (delist == null) {
                continue;
            }
            
            int relativeHour = getRelativeHour(delist.getRelativeListTime());
            if (relativeHour >= distriNumArray.length) {
                log.error("上下架分布计算中出现错误，时间超过数组");
            }
            
            distriNumArray[relativeHour]++;
        }
        
        return distriNumArray;
    }
    
    public static String calcuDistriNums(List<AutoListTime> delistList) {
        int[] curExistArray = calcuDistriNumArray(delistList);
        String distriNums = arrToString(curExistArray);
        
        return distriNums;
    }
    
    public static int[] parseToHourArray(String hourStr) {
        int arrayLength = 7 * 24;
        int[] hourArray = new int[arrayLength];
        for (int i = 0; i < hourArray.length; i++) {
            hourArray[i] = 0;
        }
        
        if (StringUtils.isEmpty(hourStr)) {
            return hourArray;
        }
        String[] strArray = hourStr.split(",");
        if (strArray == null || strArray.length <= 0) {
            return hourArray;
        }
        
        for (int i = 0; i < hourArray.length; i++) {
            if (i > strArray.length) {
                break;
            }
            String str = strArray[i];
            hourArray[i] = NumberUtil.parserInt(str, 0);
        }
        
        return hourArray;
    }
    
    public static String arrToString(int[] arr) {
        if (arr == null || arr.length <= 0) {
            return "";
        }
        
        List<Integer> list = new ArrayList<Integer>();
        for (int value : arr) {
            list.add(value);
        }
        
        return StringUtils.join(list, ",");
        
    }
    
}
