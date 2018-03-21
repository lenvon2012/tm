package actions.industry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import spider.mainsearch.MainSearchApi.MainSearchOrderType;
import spider.mainsearch.MainSearchApi.MainSearchParams;
import spider.mainsearch.MainSearchKeywordsUpdater;
import spider.mainsearch.MainSearchKeywordsUpdater.MainSearchItemRank;
import utils.CollectInfoByWebpage;
import utils.UserCache;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;

import controllers.TMController;

public class SearchIndustryAction {
    
    private static final Logger log = LoggerFactory.getLogger(SearchIndustryAction.class);
    
    private static final int DefaultSearchPages = 10;
    private static final int MaxSearchPages = 50;
    
    private static final int DefaultSplitNum = 5;
    private static final int MaxSplitNum = 30;
    
    public static IndustrySummaryInfo summarySearchIndustry(SearchIndustryRule searchRule) {
        
        Map<Long, MainSearchItemRank> itemRankInfoMap = spiderTaobaoRankItemMap(searchRule);
        
        
        if (CommonUtils.isEmpty(itemRankInfoMap)) {
            return new IndustrySummaryInfo(); 
        }
        double totalPrice = 0;
        double maxPrice = 0;
        double minPrice = 0;
        double totalPayamount = 0;
        double sellerNum = 0;
        double itemNum = 0;
        
        Set<Long> sellerIdSet = new HashSet<Long>();
        
        for (MainSearchItemRank itemRank : itemRankInfoMap.values()) {
            if (itemRank == null) {
                continue;
            }
            itemNum++;
            Long sellerId = itemRank.getSellerId();
            if (sellerId != null && sellerIdSet.contains(itemRank.getSellerId()) == false) {
                sellerIdSet.add(sellerId);
                sellerNum++;
            }
            int price = itemRank.getPrice();
            totalPrice += price;
            if (maxPrice < price) {
                maxPrice = price;
            }
            if (minPrice > price || minPrice <= 0) {
                minPrice = price;
            }
            
            totalPayamount += itemRank.getSalesCount();
        }
        
        double avgPrice = 0;
        double avgPayamount = 0;
        if (itemNum > 0) {
            avgPrice = totalPrice / itemNum;
            avgPayamount = totalPayamount / itemNum;
        }
        IndustrySummaryInfo summaryInfo = new IndustrySummaryInfo(avgPrice, maxPrice, minPrice, 
                avgPayamount, itemNum, sellerNum);
        
        
        return summaryInfo;
    }
    
    
    public static List<IndustryPriceIntervalInfo> searchPriceIntervalInfos(SearchIndustryRule searchRule, 
            int splitNum) {
        
        
        if (searchRule == null) {
            return new ArrayList<IndustryPriceIntervalInfo>();
        }
        
        Map<Long, MainSearchItemRank> itemRankInfoMap = spiderTaobaoRankItemMap(searchRule);
        
        if (CommonUtils.isEmpty(itemRankInfoMap)) {
            return new ArrayList<IndustryPriceIntervalInfo>();
        }
        
        double maxPrice = 0;
        double minPrice = 0;
        for (MainSearchItemRank itemRank : itemRankInfoMap.values()) {
            if (itemRank == null) {
                continue;
            }
            int price = itemRank.getPrice();
            if (maxPrice < price) {
                maxPrice = price;
            }
            if (minPrice > price || minPrice <= 0) {
                minPrice = price;
            }
        }
        
        //转成元格式
        int startPrice = (int) Math.floor((double) minPrice * 1.0 / 100);
        int endPrice = (int) Math.ceil((double) maxPrice * 1.0 / 100);
        
        List<IndustryPriceIntervalInfo> priceInfoList = splitItemPrices(itemRankInfoMap, 
                startPrice, endPrice, splitNum);
        
        
        return priceInfoList;
    }
    
    private static List<IndustryPriceIntervalInfo> splitItemPrices(Map<Long, MainSearchItemRank> itemRankInfoMap,
            int startPrice, int endPrice, int splitNum) {
        
        if (CommonUtils.isEmpty(itemRankInfoMap)) {
            return new ArrayList<IndustryPriceIntervalInfo>();
        }
        if (startPrice > endPrice) {
            return new ArrayList<IndustryPriceIntervalInfo>();
        }
        if (splitNum <= 0) {
            splitNum = DefaultSplitNum;
        }
        if (splitNum > MaxSplitNum) {
            splitNum = MaxSplitNum;
        }
        
        List<IndustryPriceIntervalInfo> priceInfoList = new ArrayList<IndustryPriceIntervalInfo>();
        
        for (; splitNum > 0; splitNum--) {
            int intervalEnd = getFirstIntervalEnd(startPrice, endPrice, splitNum);
            IndustryPriceIntervalInfo priceInfo = new IndustryPriceIntervalInfo(startPrice, intervalEnd);
            priceInfoList.add(priceInfo);
            
            if (intervalEnd >= endPrice) {
                break;
            }
            startPrice = intervalEnd;
        }
        
        int index = 0;
        for (IndustryPriceIntervalInfo priceInfo : priceInfoList) {
            
            int itemNum = 0;
            int payamount = 0;
            
            for (MainSearchItemRank itemRank : itemRankInfoMap.values()) {
                if (itemRank == null) {
                    continue;
                }
                
                //比较的时候是这样： =< price < 的，但最后的区间只要  =< price  就好了
                int price = itemRank.getPrice();
                if (price < priceInfo.getStartPrice() * 100) {
                    continue;
                }
                if (index < priceInfoList.size() - 1 && price >= priceInfo.getEndPrice() * 100) {
                    continue;
                }
                
                itemNum++;
                payamount += itemRank.getSalesCount();
                
                if (price >= priceInfo.getStartPrice() * 100) {
                    
                    
                }
            }
            
            priceInfo.setItemNum(itemNum);
            priceInfo.setPayamount(payamount);
            
            index++;
        }
        
        
        return priceInfoList;
        
    }
    
    private static int getFirstIntervalEnd(int startPrice, int endPrice, int splitNum) {
        
        if (splitNum <= 1) {
            return endPrice;
        }
        
        int interval = (int) Math.round(((double) (endPrice - startPrice)) / splitNum);
        
        //最少也要是1元
        if (interval <= 0) {
            interval = 1;
        }
        
        return startPrice + interval;
    }
    
    
    
    protected static Map<Long, MainSearchItemRank> spiderTaobaoRankItemMap(SearchIndustryRule searchRule) {
        
        long startTime = System.currentTimeMillis();
        
        if (searchRule == null) {
            return new HashMap<Long, MainSearchItemRank>();
        }
        
        String searchKey = searchRule.getSearchKey();
        if (StringUtils.isEmpty(searchKey)) {
            return new HashMap<Long, MainSearchItemRank>();
        }
        
        searchKey = trimSearchKey(searchKey);
        
        if (StringUtils.isEmpty(searchKey)) {
            return new HashMap<Long, MainSearchItemRank>();
        }
        
        String itemOrderType = searchRule.getItemOrderType();
        if (StringUtils.isEmpty(itemOrderType)) {
            itemOrderType = MainSearchOrderType.Renqi;
        }
        
        int searchPages = searchRule.getSearchPages();
        if (searchPages <= 0) {
            searchPages = DefaultSearchPages;
        }
        if (searchPages > MaxSearchPages) {
            searchPages = MaxSearchPages;
        }
        
        Map<Long, MainSearchItemRank> itemRankInfoMap = MainSearchKeywordsUpdater.doSearch(new MainSearchParams(
                searchKey, searchPages, itemOrderType));
        
        if (CommonUtils.isEmpty(itemRankInfoMap)) {
            log.error("fail to spider taobao rank numIids from taobao "
                    + ", searchKey: " + searchKey + ", itemOrderType: " + itemOrderType
                    + ", searchPages: " + searchPages + "----------------------------------");
            return new HashMap<Long, MainSearchItemRank>();
        }
        
        long endTime = System.currentTimeMillis();
        int rankNumIidSize = itemRankInfoMap.size();
        log.info("end search taobao item rand " 
                + ", searchKey: " + searchKey + ", itemOrderType: " + itemOrderType
                + ", total get " + rankNumIidSize + " items for " + searchPages + " searchPages, "
                + "used time " + (endTime - startTime) + " ms--------------------");
        
        itemRankInfoMap = filterSpiderResult(searchRule, itemRankInfoMap);
        
        return itemRankInfoMap;
        
        
    } 
    
    protected static Map<Long, MainSearchItemRank> spiderTaobaoRankItemMap(SearchIndustryRule searchRule, User user) {
        
        long startTime = System.currentTimeMillis();
        
        if (searchRule == null) {
            return new HashMap<Long, MainSearchItemRank>();
        }
        
        String searchKey = searchRule.getSearchKey();
        if (StringUtils.isEmpty(searchKey)) {
            return new HashMap<Long, MainSearchItemRank>();
        }
        
        searchKey = trimSearchKey(searchKey);
        
        if (StringUtils.isEmpty(searchKey)) {
            return new HashMap<Long, MainSearchItemRank>();
        }
        
        String itemOrderType = searchRule.getItemOrderType();
        if (StringUtils.isEmpty(itemOrderType)) {
            itemOrderType = MainSearchOrderType.Renqi;
        }
        
        int searchPages = searchRule.getSearchPages();
        if (searchPages <= 0) {
            searchPages = DefaultSearchPages;
        }
        if (searchPages > MaxSearchPages) {
            searchPages = MaxSearchPages;
        }
        
        String searchPlace = searchRule.getSearchPlace();
        if (StringUtils.isEmpty(searchPlace)) {
            return new HashMap<Long, MainSearchItemRank>();
        }

        int pn = searchRule.getPo().getPn();
        if (pn <= 0) {
            pn = 1;
        }
        
        UserCache usercache = TMController.checkEndTimeCache(user.getId());

        Map<Long, MainSearchItemRank> itemRankInfoMap = new HashMap<Long, MainSearchItemRank>();
        try {
            itemRankInfoMap = CollectInfoByWebpage.getDelistInfoMap(searchKey,
                    itemOrderType, searchPlace, searchPages, pn, user, usercache);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

        if (CommonUtils.isEmpty(itemRankInfoMap)) {
            log.error("fail to spider taobao rank numIids from taobao "
                    + ", searchKey: " + searchKey + ", itemOrderType: "
                    + itemOrderType + ", searchPages: " + searchPages
                    + "----------------------------------");
            Map<Long, MainSearchItemRank> newMap = new HashMap<Long, MainSearchItemRank>();
            usercache.setSearch(false);
            Cache.set(Long.toString(user.getId()), usercache);
            return newMap;
        }

        long endTime = System.currentTimeMillis();
        int rankNumIidSize = itemRankInfoMap.size();
        log.info("end search taobao item rand " + ", searchKey: " + searchKey
                + ", itemOrderType: " + itemOrderType + ", total get "
                + rankNumIidSize + " items for " + searchPages
                + " searchPages, " + "used time " + (endTime - startTime)
                + " ms--------------------");

        itemRankInfoMap = filterSpiderResult(searchRule, itemRankInfoMap);

        return itemRankInfoMap;
    } 
    
    private static Map<Long, MainSearchItemRank> filterSpiderResult(SearchIndustryRule searchRule, 
            Map<Long, MainSearchItemRank> itemRankInfoMap) {
        
        if (searchRule == null) {
            return itemRankInfoMap;
        }
        double startPrice = searchRule.getStartPrice() * 100;
        double endPrice = searchRule.getEndPrice() * 100;
        double startSales = searchRule.getStartSales();
        double endSales = searchRule.getEndSales();
        
        if (startPrice <= 0 && endPrice <= 0 && startSales <= 0 && endSales <= 0) {
            return itemRankInfoMap;
        }
        
        Map<Long, MainSearchItemRank> resultMap = new HashMap<Long, MainSearchItemRank>();
        
        for (Long numIid : itemRankInfoMap.keySet()) {
            MainSearchItemRank itemRank = itemRankInfoMap.get(numIid);
            
            if (itemRank == null) {
                continue;
            }
            
            if (startPrice > 0) {
                if (itemRank.getPrice() < startPrice) {
                    continue;
                }
            }
            if (endPrice > 0) {
                if (itemRank.getPrice() > endPrice) {
                    continue;
                }
            }
            
            if (startSales > 0) {
                if (itemRank.getSalesCount() < startSales) {
                    continue;
                }
            }
            
            if (endSales > 0) {
                if (itemRank.getSalesCount() > endSales) {
                    continue;
                }
            }
            
            resultMap.put(numIid, itemRank);
            
        }
        
        log.info("after filter rule, origin size: " + itemRankInfoMap.size() 
                + ", result size: " + resultMap.size() + ", rule: " + searchRule.toString() + "-------------");
        
        return resultMap;
    }
    
    private static String trimSearchKey(String searchKey) {
        if (StringUtils.isEmpty(searchKey)) {
            return "";
        }
        searchKey = searchKey.trim();
        
        return searchKey;
    }
    
    public static class SearchIndustryRule {
        private String searchKey;
        private String itemOrderType;
        private int searchPages;
        private String searchPlace;
        private PageOffset po;
        
        private double startPrice;
        private double endPrice;
        private double startSales;
        private double endSales;//销量条件
        
        public String getSearchPlace() {
            return searchPlace;
        }
        public void setSearchPlace(String searchPlace) {
            this.searchPlace = searchPlace;
        }
        public PageOffset getPo() {
            return po;
        }
        public void setPo(PageOffset po) {
            this.po = po;
        }
        public String getSearchKey() {
            return searchKey;
        }
        public void setSearchKey(String searchKey) {
            this.searchKey = searchKey;
        }
        public String getItemOrderType() {
            return itemOrderType;
        }
        public void setItemOrderType(String itemOrderType) {
            this.itemOrderType = itemOrderType;
        }
        public int getSearchPages() {
            return searchPages;
        }
        public void setSearchPages(int searchPages) {
            this.searchPages = searchPages;
        }
        public double getStartPrice() {
            return startPrice;
        }
        public void setStartPrice(double startPrice) {
            this.startPrice = startPrice;
        }
        public double getEndPrice() {
            return endPrice;
        }
        public void setEndPrice(double endPrice) {
            this.endPrice = endPrice;
        }
        public double getStartSales() {
            return startSales;
        }
        public void setStartSales(double startSales) {
            this.startSales = startSales;
        }
        public double getEndSales() {
            return endSales;
        }
        public void setEndSales(double endSales) {
            this.endSales = endSales;
        }
        @Override
        public String toString() {
            return "SearchIndustryRule [searchKey=" + searchKey
                    + ", itemOrderType=" + itemOrderType + ", searchPages="
                    + searchPages + ", startPrice=" + startPrice
                    + ", endPrice=" + endPrice + ", startSales=" + startSales
                    + ", endSales=" + endSales + "]";
        }
        public SearchIndustryRule(String searchKey, String itemOrderType,
                int searchPages, double startPrice, double endPrice,
                double startSales, double endSales) {
            super();
            this.searchKey = searchKey;
            this.itemOrderType = itemOrderType;
            this.searchPages = searchPages;
            this.startPrice = startPrice;
            this.endPrice = endPrice;
            this.startSales = startSales;
            this.endSales = endSales;
        }
        public SearchIndustryRule() {
            super();
        }
        
        
    }
    
    public static class IndustrySummaryInfo {
        private double avgPrice;
        private double maxPrice;
        private double minPrice;
        private double avgPayamount;
        private double itemNum;
        private double sellerNum;
        
        public IndustrySummaryInfo() {
            super();
        }
        
        
        public IndustrySummaryInfo(double avgPrice, double maxPrice,
                double minPrice, double avgPayamount, double itemNum,
                double sellerNum) {
            super();
            this.avgPrice = avgPrice;
            this.maxPrice = maxPrice;
            this.minPrice = minPrice;
            this.avgPayamount = avgPayamount;
            this.itemNum = itemNum;
            this.sellerNum = sellerNum;
        }


        public double getAvgPrice() {
            return avgPrice;
        }
        public void setAvgPrice(double avgPrice) {
            this.avgPrice = avgPrice;
        }
        public double getMaxPrice() {
            return maxPrice;
        }
        public void setMaxPrice(double maxPrice) {
            this.maxPrice = maxPrice;
        }
        public double getMinPrice() {
            return minPrice;
        }
        public void setMinPrice(double minPrice) {
            this.minPrice = minPrice;
        }
        public double getAvgPayamount() {
            return avgPayamount;
        }
        public void setAvgPayamount(double avgPayamount) {
            this.avgPayamount = avgPayamount;
        }
        public double getItemNum() {
            return itemNum;
        }
        public void setItemNum(double itemNum) {
            this.itemNum = itemNum;
        }
        public double getSellerNum() {
            return sellerNum;
        }
        public void setSellerNum(double sellerNum) {
            this.sellerNum = sellerNum;
        }
        
    }
    
    /**
     * 宝贝价格区间
     * @author ying
     *
     */
    public static class IndustryPriceIntervalInfo {
        private int startPrice;
        private int endPrice;
        private int itemNum;
        private int payamount;
        
        public String getPriceInterval() {
            return startPrice + "-" + endPrice + "元";
        }
        
        public int getStartPrice() {
            return startPrice;
        }
        public void setStartPrice(int startPrice) {
            this.startPrice = startPrice;
        }
        public int getEndPrice() {
            return endPrice;
        }
        public void setEndPrice(int endPrice) {
            this.endPrice = endPrice;
        }
        public int getItemNum() {
            return itemNum;
        }
        public void setItemNum(int itemNum) {
            this.itemNum = itemNum;
        }
        public int getPayamount() {
            return payamount;
        }
        public void setPayamount(int payamount) {
            this.payamount = payamount;
        }

        public IndustryPriceIntervalInfo(int startPrice, int endPrice) {
            super();
            this.startPrice = startPrice;
            this.endPrice = endPrice;
        }
        
        
        
    }
    
    
}
