package job.hotitems;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.FutureTask;

import models.hotitem.CatHotItemPlay;
import models.hotitem.CatHotItemPlay.CatHotItemStatus;
import models.hotitem.CatTopWordSearchLog;
import models.hotitem.CatTopWordSearchLog.CatTopWordSearchStatus;
import models.hotitem.ItemSearchCache;
import models.hotitem.ItemSearchCache.ItemSearchType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spider.mainsearch.MainSearchApi.MainSearchOrderType;
import spider.mainsearch.MainSearchApi.MainSearchParams;
import spider.mainsearch.MainSearchKeywordsUpdater;
import spider.mainsearch.MainSearchKeywordsUpdater.MainSearchItemRank;
import bustbapi.ItemApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.client.utils.SplitUtils;
import com.taobao.api.domain.Item;

public class CatTopWordSearchJob {

    private static final Logger log = LoggerFactory.getLogger(CatTopWordSearchJob.class);
    
    private static final int ItemGetThreadSize = 8;
    
    private static PYFutureTaskPool<List<Item>> itemGetPool = new PYFutureTaskPool<List<Item>>(ItemGetThreadSize);
    
    
    private static final int SearchType = ItemSearchType.Renqi;
    private static final String SearchTypeStr = MainSearchOrderType.Renqi;
    private static final int SearchPages = 10;
    
    private Long cid;
    private String word;
    
    private CatTopWordSearchLog searchLog;

    public CatTopWordSearchJob(Long cid, String catName, String word) {
        super();
        this.cid = cid;
        this.word = word;

        this.searchLog = new CatTopWordSearchLog(cid, catName, word);
    }
    
    private static boolean isValidSearchCache(ItemSearchCache searchCache) {
        if (searchCache == null) {
            return false;
        }
        
        return true;
    }
    
    //以后可以根据updateTs 判断
    private static boolean isValidCatHotItem(CatHotItemPlay catItem) {
        if (catItem == null) {
            return false;
        }
        
        return true;
    }
    
    public CatTopWordSearchLog searchHotItems() {
        try {
            doSearchWord();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            searchLog.setMessage("系统运行出现异常：" + ex.getMessage());
            searchLog.jdbcSave();
        }
        
        return searchLog;
    }
    
    private void doSearchWord() {
        if (StringUtils.isEmpty(word) || cid == null || cid <= 0L) {
            return;
        }
        
        long startTime = System.currentTimeMillis();
        
        long beforeCatItemNum = CatHotItemPlay.countCatItemByCid(cid);
        
        ItemSearchCache searchCache = ItemSearchCache.findBywordAndSearchType(word, SearchType);
        
        List<CatHotItemPlay> hotItemList = new ArrayList<CatHotItemPlay>();
        
        if (searchCache != null && isValidSearchCache(searchCache)) {
            searchLog.setStatus(CatTopWordSearchStatus.Cached);
            hotItemList = doForItemSearchCache(searchCache.getNumIids());
        } else {
            searchLog.setStatus(CatTopWordSearchStatus.FromTaobao);
            hotItemList = doSearchFromTaobao();
        }
        if (CommonUtils.isEmpty(hotItemList)) {
            hotItemList = new ArrayList<CatHotItemPlay>();
        }
        
        searchLog.setCatItemNum(hotItemList.size());
        
        long afterCatItemNum = CatHotItemPlay.countCatItemByCid(cid);
        
        searchLog.setNewItemNum((int) (afterCatItemNum - beforeCatItemNum));
        
        long endTime = System.currentTimeMillis();
        long usedTime = endTime - startTime;
        
        
        searchLog.setUsedTime(usedTime);
        
        searchLog.jdbcSave();
        
        log.info("end search for cid: " + cid + ", word: " + word 
                + ", searchLog: " + searchLog + ", used " + usedTime + " ms---------------------------");
        
        return;
    }
    
    private List<CatHotItemPlay> doForItemSearchCache(String numIids) {
        if (StringUtils.isEmpty(numIids)) {
            return new ArrayList<CatHotItemPlay>();
        }
        String[] numIidArr = numIids.split(",");
        if (numIidArr == null || numIidArr.length <= 0) {
            return new ArrayList<CatHotItemPlay>();
        }
        Set<Long> numIidSet = new HashSet<Long>();
        for (String idStr : numIidArr) {
            Long numIid = NumberUtil.parserLong(idStr, 0L);
            if (numIid == null || numIid <= 0L) {
                continue;
            }
            numIidSet.add(numIid);
        }
        
        searchLog.setTotalItemNum(numIidSet.size());
        if (CommonUtils.isEmpty(numIidSet)) {
            return new ArrayList<CatHotItemPlay>();
        }
        
        List<CatHotItemPlay> catItemList = CatHotItemPlay.findByCidAndNumIids(cid, numIidSet);
        if (CommonUtils.isEmpty(catItemList)) {
            catItemList = new ArrayList<CatHotItemPlay>();
        }
        
        setCatItemStatus(catItemList);
        
        return catItemList;
        
    }
    
    private static void setCatItemStatus(List<CatHotItemPlay> catItemList) {
        if (CommonUtils.isEmpty(catItemList)) {
            catItemList = new ArrayList<CatHotItemPlay>();
        }
        
        for (CatHotItemPlay catItem : catItemList) {
            if (catItem == null) {
                continue;
            }
            if (catItem.isHitCatItem() == true) {
                continue;
            }
            catItem.setStatus(CatHotItemStatus.CatItem);
            catItem.jdbcSave();
        }
    }
    
    
    private List<CatHotItemPlay> doSearchFromTaobao() {
        //从淘宝查
        Map<Long, MainSearchItemRank> itemRankInfoMap = MainSearchKeywordsUpdater.doSearch(new MainSearchParams(word,
                SearchPages, SearchTypeStr));
        
        //爬虫失败
        if (CommonUtils.isEmpty(itemRankInfoMap)) {
            log.error("fail to search items from taobao for word: " + word + "-----------------");
            return new ArrayList<CatHotItemPlay>();
        }
        
        Set<Long> numIidSet = new HashSet<Long>();
        List<CatHotItemPlay> catItemList = new ArrayList<CatHotItemPlay>();
        Set<Long> notCachedNumIidSet = new HashSet<Long>();
        for (MainSearchItemRank itemRank : itemRankInfoMap.values()) {
            if (itemRank == null) {
                continue;
            }
            Long numIid = itemRank.getNumIid();
            if (numIid == null || numIid <= 0) {
                continue;
            }
            numIidSet.add(numIid);
            
            CatHotItemPlay catItem = CatHotItemPlay.findByNumIid(numIid);
            if (catItem != null && isValidCatHotItem(catItem)) {
                if (cid != null && cid.equals(catItem.getCid())) {
                    catItemList.add(catItem);
                }
            } else {
                notCachedNumIidSet.add(numIid);
            }
            
        }
        
        searchLog.setTotalItemNum(numIidSet.size());
        searchLog.setApiGetItemNum(notCachedNumIidSet.size());
        
        setCatItemStatus(catItemList);
        
        //使用api获取宝贝
        addCatItemsFromTaobao(notCachedNumIidSet, itemRankInfoMap, catItemList);
        
        //存储ItemSearchCache
        ItemSearchCache searchCache = new ItemSearchCache(word, StringUtils.join(numIidSet, ","), SearchType);
        searchCache.jdbcSave();
        
        return catItemList;
    }
    
    
    private void addCatItemsFromTaobao(Set<Long> notCachedNumIidSet, 
            Map<Long, MainSearchItemRank> itemRankInfoMap, List<CatHotItemPlay> catItemList) {
        
        if (CommonUtils.isEmpty(notCachedNumIidSet)) {
            return;
        }
        if (CommonUtils.isEmpty(itemRankInfoMap)) {
            return;
        }
        
        //每个线程要获取的宝贝数
        int repeateApiTimes = 3;//一个线程中，调用3次api，获取多个宝贝
        int eachThreadItemSize = ItemApi.ItemsListGet.MAX_NUMIID_LENGTH * repeateApiTimes;
        
        List<List<Long>> splitNumIidsList = SplitUtils.splitToSubLongList(notCachedNumIidSet, eachThreadItemSize);
        
        if (CommonUtils.isEmpty(splitNumIidsList)) {
            return;
        }
        
        List<FutureTask<List<Item>>> promises = new ArrayList<FutureTask<List<Item>>>();
        
        for (List<Long> splitNumIids : splitNumIidsList) {
            if (CommonUtils.isEmpty(splitNumIids)) {
                continue;
            }
            ItemApi.ItemsListGet itemGetApi = new ItemApi.ItemsListGet(splitNumIids, true);
            promises.add(itemGetPool.submit(itemGetApi));
        }
        
        List<Item> tbItemList = new ArrayList<Item>();
        
        for (FutureTask<List<Item>> promise : promises) {
            
            try {
                List<Item> tempList = promise.get();
                if (CommonUtils.isEmpty(tempList)) {
                    continue;
                } else {
                    tbItemList.addAll(tempList);
                }
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        
        searchLog.setFailGetItemNum(notCachedNumIidSet.size() - tbItemList.size());
        
        for (Item tbItem : tbItemList) {
            if (tbItem == null) {
                continue;
            }
            MainSearchItemRank itemRank = itemRankInfoMap.get(tbItem.getNumIid());
            if (itemRank == null) {
                continue;
            }
            CatHotItemPlay catItem = new CatHotItemPlay(tbItem, itemRank);
            if (cid != null && cid.equals(catItem.getCid())) {
                catItem.setStatus(CatHotItemStatus.CatItem);
                catItemList.add(catItem);
            } else {
                catItem.setStatus(CatHotItemStatus.ForCache);
            }
            catItem.jdbcSave();
        }
        
    }
    
}
