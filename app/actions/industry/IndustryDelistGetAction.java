package actions.industry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spider.mainsearch.MainSearchKeywordsUpdater.MainSearchItemRank;
import utils.DateUtil;
import actions.industry.IndustryDelistResultAction.DelistItemInfo;
import actions.industry.RemoteIndustryGetAction.IndustryItemInfo;
import actions.industry.SearchIndustryAction.SearchIndustryRule;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;

public class IndustryDelistGetAction {

    private static final Logger log = LoggerFactory.getLogger(IndustryDelistGetAction.class);
    
    public static List<DelistItemInfo> doSearchTaobaoRankItems(String searchKey, 
            String itemOrderType, int searchPages) {
        
        Map<Long, MainSearchItemRank> itemRankInfoMap = spiderTaobaoRankItemMap(searchKey, 
                itemOrderType, searchPages);
        
        if (CommonUtils.isEmpty(itemRankInfoMap)) {
            return new ArrayList<DelistItemInfo>();
        }
        
        //根据rank排序
        List<MainSearchItemRank> itemRankInfoList = sortItemRankInfos(itemRankInfoMap);
        
        Set<Long> rankNumIidSet = toNumIidSet(itemRankInfoList);
        if (CommonUtils.isEmpty(rankNumIidSet)) {
            return new ArrayList<DelistItemInfo>();
        }
        
        long startTime = System.currentTimeMillis();
        
        Map<Long, IndustryItemInfo> itemMap = RemoteIndustryGetAction.fetchTaobaoItemMap(rankNumIidSet);
        if (CommonUtils.isEmpty(itemMap)) {
            log.error("fail to fetch taobao rank items" 
                    + ", searchKey: " + searchKey + ", itemOrderType: " + itemOrderType
                    + ", searchPages: " + searchPages + "----------------------------------");
            return new ArrayList<DelistItemInfo>();
        }
        
        int orderIndex = 1;
        List<DelistItemInfo> resultItemList = new ArrayList<DelistItemInfo>();
        for (MainSearchItemRank itemRank : itemRankInfoList) {
            Long numIid = itemRank.getNumIid();
            if (numIid == null || numIid <= 0L) {
                continue;
            }
            IndustryItemInfo industryItem = itemMap.get(numIid);
            if (industryItem == null) {
                continue;
            }
            
            DelistItemInfo delistItem = toDelistItemInfo(orderIndex, itemRank.getSalesCount(), industryItem);
            resultItemList.add(delistItem);
            
            orderIndex++;
        }
        
        long endTime = System.currentTimeMillis();
        
        int totalItemSize = itemRankInfoList.size();
        int successItemSize = resultItemList.size();
        log.info("end fetch taobao rand item"
                + ", searchKey: " + searchKey + ", itemOrderType: " + itemOrderType
                + ", success get " + successItemSize + " items of total " + totalItemSize + " items, for " 
                + searchPages + " searchPages, "
                + "used time " + (endTime - startTime) + " ms--------------------");
        
        return resultItemList;
        
    }
    
    public static List<DelistItemInfo> doSearchTaobaoRankItems(String searchKey, String itemOrderType, 
            int searchPages, String searchPlace, PageOffset po, User user) {
        
        
        Map<Long, MainSearchItemRank> itemRankInfoMap = spiderTaobaoRankItemMap(searchKey, 
                itemOrderType, searchPages, searchPlace, po, user);
        
        if (CommonUtils.isEmpty(itemRankInfoMap)) {
            return new ArrayList<DelistItemInfo>();
        }
        
        //根据rank排序
        List<MainSearchItemRank> itemRankInfoList = sortItemRankInfos(itemRankInfoMap);
        
        Set<Long> rankNumIidSet = toNumIidSet(itemRankInfoList);
        if (CommonUtils.isEmpty(rankNumIidSet)) {
            return new ArrayList<DelistItemInfo>();
        }

        long startTime = System.currentTimeMillis();

        List<DelistItemInfo> resultItemList = new ArrayList<DelistItemInfo>();

        for (int i = 0; i < itemRankInfoList.size(); i++) {
            long numIid = itemRankInfoList.get(i).getNumIid();
            String title = itemRankInfoList.get(i).getTitle();
            String dt = itemRankInfoList.get(i).getDt();
            String picUrl = itemRankInfoList.get(i).getPicPath();
            String delistTimestamp = itemRankInfoList.get(i).getdelistTimestamp();

            DelistItemInfo delistItem = changetoDelistItemInfo(numIid, title,
                    dt, picUrl, delistTimestamp);
            resultItemList.add(delistItem);
        }

        long endTime = System.currentTimeMillis();

        int totalItemSize = itemRankInfoList.size();
        // int successItemSize = resultItemList.size();
        log.info("end fetch taobao rand item" + ", searchKey: " + searchKey
                + ", itemOrderType: " + itemOrderType + ", success get " 
                + totalItemSize + " items, for " + searchPages
                + " searchPages, " + "used time " + (endTime - startTime)
                + " ms--------------------");
        
        return resultItemList;
        
    }
    
    public static DelistItemInfo changetoDelistItemInfo(long numIid,
            String title, String dt, String picUrl, String delistTimestamp) {
        DelistItemInfo delistItem = new DelistItemInfo(numIid, title, dt,
                picUrl, delistTimestamp);

        return delistItem;
    }
    
    public static DelistItemInfo searchOneItem(Long numIid) {
        if (numIid == null || numIid <= 0L) {
            return null;
        }
        
        Set<Long> numIidSet = new HashSet<Long>();
        numIidSet.add(numIid);
        
        Map<Long, IndustryItemInfo> itemMap = RemoteIndustryGetAction.fetchTaobaoItemMap(numIidSet);
        
        IndustryItemInfo industryItem = itemMap.get(numIid);
        
        if (industryItem == null) {
            return null;
        }
        
        DelistItemInfo itemInfo = toDelistItemInfo(1, 0, industryItem);
        
        return itemInfo;
    }
    
    private static DelistItemInfo toDelistItemInfo(int orderIndex, int salesCount, 
            IndustryItemInfo industryItem) {
        long delistTime = industryItem.getDelistTime();
        
        long relativeTime = getRelativeDelistTime(delistTime);
        //周日
        if (relativeTime >= 0 && relativeTime < DateUtil.DAY_MILLIS) {
            relativeTime += 6 * DateUtil.DAY_MILLIS;
        } else {
            relativeTime -= DateUtil.DAY_MILLIS;
        }
        
        //下架剩余时间
        long leftTime = 0;
        if (delistTime > 0) {
            leftTime = delistTime - System.currentTimeMillis();
            while (leftTime < 0) {
                leftTime += 7 * DateUtil.DAY_MILLIS;
            }
        } else {
            leftTime = 7 * DateUtil.DAY_MILLIS;
        }
        
        //final long createTime = industryItem.getCreateTime() == null ? 0 : industryItem.getCreateTime();
        final long createTime = 0;
        
        DelistItemInfo delistItem = new DelistItemInfo(industryItem.getNumIid(), 
                industryItem.getTitle(), industryItem.getPicUrl(),
                orderIndex, relativeTime, leftTime, createTime, salesCount);
        
        return delistItem;
    }
    
    //relativeTime是可能为0的。。。
    private static long getRelativeDelistTime(long delistTime) {
        if (delistTime <= 0) {
            return -1;
        }
        
        long weekStart = DateUtil.findThisWeekStart(delistTime);
        
        if (weekStart <= 0) {
            return -1;
        }
        
        long relativeTime = delistTime - weekStart;
        
        return relativeTime;
    }
    
    private static final String[] WeekArray = new String[] {"周一", "周二", "周三", 
            "周四", "周五", "周六", "周日", };
    
    public static String getDelistWeekDay(long delistTime) {
        if (delistTime <= 0) {
            return "-";
        }
        
        long relativeTime = getRelativeDelistTime(delistTime);
        if (relativeTime < 0) {
            return "-";
        }
        
        //周日
        if (relativeTime >= 0 && relativeTime < DateUtil.DAY_MILLIS) {
            relativeTime += 6 * DateUtil.DAY_MILLIS;
        } else {
            relativeTime -= DateUtil.DAY_MILLIS;
        }
        
        long delistDay = relativeTime / DateUtil.DAY_MILLIS;
        relativeTime = relativeTime - delistDay * DateUtil.DAY_MILLIS;
        
        long delistHour = relativeTime / DateUtil.HOUR_MILLS;
        relativeTime = relativeTime - delistHour * DateUtil.HOUR_MILLS;
        
        
        
        String timeStr = "";
        
        if (delistDay >= 0 && delistDay < WeekArray.length) {
            timeStr += WeekArray[(int) delistDay] + "";
        }
        if (delistHour < 6) {
            timeStr += "凌晨";
        } else if (delistHour < 11) {
            timeStr += "上午";
        } else if (delistHour < 14) {
            timeStr += "中午";
        } else if (delistHour < 18) {
            timeStr += "下午";
        } else {
            timeStr += "晚上";
        }
        
        
        return timeStr;
    }
    
    public static String getDelistHHmmss(long delistTime) {
        if (delistTime <= 0) {
            return "-";
        }
        
        long relativeTime = getRelativeDelistTime(delistTime);
        if (relativeTime < 0) {
            return "-";
        }
        
        //周日
        if (relativeTime >= 0 && relativeTime < DateUtil.DAY_MILLIS) {
            relativeTime += 6 * DateUtil.DAY_MILLIS;
        } else {
            relativeTime -= DateUtil.DAY_MILLIS;
        }
        
        long delistDay = relativeTime / DateUtil.DAY_MILLIS;
        relativeTime = relativeTime - delistDay * DateUtil.DAY_MILLIS;
        
        long delistHour = relativeTime / DateUtil.HOUR_MILLS;
        relativeTime = relativeTime - delistHour * DateUtil.HOUR_MILLS;
        
        long delistMinute = relativeTime / DateUtil.ONE_MINUTE_MILLIS;
        relativeTime = relativeTime - delistMinute * DateUtil.ONE_MINUTE_MILLIS;
        
        long delistSecond = relativeTime / 1000;
        
        String timeStr = "";
        
        if (delistHour < 10) {
            timeStr += "0";
        }
        timeStr += delistHour + ":";
        
        if (delistMinute < 10) {
            timeStr += "0";
        }
        timeStr += delistMinute + ":";
        
        if (delistSecond < 10) {
            timeStr += "0";
        }
        timeStr += delistSecond + "";
        
        return timeStr;
    }
    
    
    private static Set<Long> toNumIidSet(List<MainSearchItemRank> itemRankInfoList) {
        Set<Long> rankNumIidSet = new HashSet<Long>();
        
        if (CommonUtils.isEmpty(itemRankInfoList)) {
            return rankNumIidSet;
        }
        
        for (MainSearchItemRank itemRank : itemRankInfoList) {
            Long numIid = itemRank.getNumIid();
            if (numIid == null || numIid <= 0L) {
                continue;
            }
            rankNumIidSet.add(numIid);
        }
        
        return rankNumIidSet;
    }
    
    /**
     * 获取淘宝搜索排名前几页的宝贝
     * @param searchKey
     * @param searchPages
     * @return
     */
    private static Map<Long, MainSearchItemRank> spiderTaobaoRankItemMap(String searchKey, String itemOrderType, int searchPages) {
        
        SearchIndustryRule searchRule = new SearchIndustryRule();
        
        searchRule.setSearchKey(searchKey);
        searchRule.setItemOrderType(itemOrderType);
        searchRule.setSearchPages(searchPages);

        return SearchIndustryAction.spiderTaobaoRankItemMap(searchRule);
    }
    
    private static Map<Long, MainSearchItemRank> spiderTaobaoRankItemMap(
            String searchKey, String itemOrderType, int searchPages,
            String searchPlace, PageOffset po, User user) {

        SearchIndustryRule searchRule = new SearchIndustryRule();

        searchRule.setSearchKey(searchKey);
        searchRule.setItemOrderType(itemOrderType);
        searchRule.setSearchPages(searchPages);
        searchRule.setSearchPlace(searchPlace);
        searchRule.setPo(po);
        return SearchIndustryAction.spiderTaobaoRankItemMap(searchRule, user);
    }
    
    private static List<MainSearchItemRank> sortItemRankInfos(Map<Long, MainSearchItemRank> itemRankInfoMap) {
        
        List<MainSearchItemRank> itemRankInfoList = new ArrayList<MainSearchItemRank>();
        
        if (CommonUtils.isEmpty(itemRankInfoMap)) {
            return itemRankInfoList;
        } 
        Collection<MainSearchItemRank> itemRankInfoColl = itemRankInfoMap.values();
        
        if (CommonUtils.isEmpty(itemRankInfoColl)) {
            return itemRankInfoList;
        }
        
        for (MainSearchItemRank itemRankInfo : itemRankInfoColl) {
            if (itemRankInfo == null) {
                continue;
            }
            itemRankInfoList.add(itemRankInfo);
        }
        
        //rank 升序
        Collections.sort(itemRankInfoList, new Comparator<MainSearchItemRank>() {

            @Override
            public int compare(MainSearchItemRank o1, MainSearchItemRank o2) {
                return o1.getRank() - o2.getRank();
            }
            
        });
        
        return itemRankInfoList;
    }
    
    
    
    /**
     * 根据api获取的淘宝item的cache
     * @author ying
     *
     */
    /*
    public static class TaobaoItemCache {
        
        private static final String Prefix = "De-TaobaoItemCahce-";
        
        private static String genKey(Long numIid) {
            return Prefix + numIid;
        }
        
        public static void putItemToCache(Long numIid, Item item) {
            if (numIid == null || numIid <= 0L) {
                return;
            }
            
            String cacheKey = genKey(numIid);
            
            try {
                Cache.set(cacheKey, item, "90min");
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        
        public static Item getItemFromCache(Long numIid) {
            if (numIid == null || numIid <= 0L) {
                return null;
            }
            
            String cacheKey = genKey(numIid);
            
            try {
                
                Item item = (Item) Cache.get(cacheKey);
                
                return item;
                
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                return null;
            }
        }
        
    }
    */
    
    /**
     * 宝贝的排序的numIidList
     * @author ying
     *
     */
    /*
    public static class TaobaoRankNumIidsCache {
        
        private static final String Prefix = "TaobaoRankNumIidsCache-";
        
        private static String genKey(String searchKey, String itemOrderType, int searchPages) {
            if (StringUtils.isBlank(searchKey)) {
                searchKey = "";
            }
            searchKey = trimSearchKey(searchKey);
            
            return Prefix + searchKey.replaceAll("\\s", "_") + "-" + itemOrderType + "-" + searchPages;
        }
        
        public static void putItemListToCache(String searchKey, String itemOrderType, int searchPages, 
                List<Long> rankNumIidList) {
            
            String cacheKey = genKey(searchKey, itemOrderType, searchPages);
            
            //这里就不存了
            if (CommonUtils.isEmpty(rankNumIidList)) {
                return;
            }
            
            try {
                String cacheTime = "";
                //因为人气的排名变化不会很大，所以可以缓存久一点，而默认排序受下架时间影响，变化比较大
                if (MainSearchOrderType.Renqi.equals(itemOrderType)) {
                    cacheTime = "30min";
                } else {
                    return;
                }
                
                Cache.set(cacheKey, rankNumIidList, cacheTime);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            
        }
        
        public static List<Long> getItemListFromCache(String searchKey, String itemOrderType, int searchPages) {
            String cacheKey = genKey(searchKey, itemOrderType, searchPages);
            
            try {
                
                List<Long> rankNumIidList = (List<Long>) Cache.get(cacheKey);
                
                return rankNumIidList;
                
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                return null;
            }
        }
        
    }
    */
    
}
