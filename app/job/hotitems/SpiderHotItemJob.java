package job.hotitems;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import models.hotitem.CatHotItemPlay;
import models.hotitem.CatHotItemUpdateLog;
import models.hotitem.CatHotItemUpdateLog.CatHotUpdateStatus;
import models.hotitem.CatHotItemUpdateTs;
import models.hotitem.CatTopWordPlay;
import models.hotitem.CatTopWordSearchLog;
import models.item.ItemCatPlay;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bustbapi.TMApi;

import com.ciaosir.client.CommonUtils;

public class SpiderHotItemJob implements Callable<Void> {

    private static final Logger log = LoggerFactory.getLogger(SpiderHotItemJob.class);
    
    private static final int MaxSpiderWordNum = 10;
    private static final int MinSpiderWordNum = 5;
    private static final int ProperItemNum = 400;
    
    
    private int cidIndex;
    private int totalCount;
    
    private Long cid;
    private String catName;
    
    private CatHotItemUpdateLog spiderLog;
    
    public SpiderHotItemJob(int cidIndex, int totalCount, Long cid, String catName) {
        super();
        this.cidIndex = cidIndex;
        this.totalCount = totalCount;
        this.cid = cid;
        this.catName = catName;
        
        this.spiderLog = new CatHotItemUpdateLog(cid, catName);
    }
    
    private boolean hasSpiderBefore() {
        
        CatHotItemUpdateTs updateTs = CatHotItemUpdateTs.findByCid(cid);
        if (updateTs != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timeStr = sdf.format(new Date(updateTs.getUpdateTs()));
            log.warn("cid: " + cid + " has been spider in " + timeStr + "------------------------");
            return true;
        } else {
            return false;
        }
    }


    @Override
    public Void call() throws Exception {
        
        try {
            if (cid == null || cid <= 0L) {
                return null;
            }
            //之前已经爬取过了
            if (hasSpiderBefore() == true) {
                return null;
            }
            List<String> spiderWordList = SpiderWordsGetAction.findSpiderWordList(cid);
            if (CommonUtils.isEmpty(spiderWordList)) {
                spiderLog.setStatus(CatHotUpdateStatus.NoSpiderWord);
                spiderLog.setMessage("该类目下找不到需要搜索的关键词！");
                log.error("cannot find spider word for cid: " + cid + ", log: " + spiderLog.toString()
                        + ", cidIndex: " + cidIndex + ", totalCount: " + totalCount + "----------------------");

                spiderLog.jdbcSave();
                return null;
            }
            
            doSpiderCatHotItems(spiderWordList);
            
            CatHotItemUpdateTs updateTs = new CatHotItemUpdateTs(cid);
            updateTs.jdbcSave();
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            spiderLog.setMessage("系统出现异常：" + ex.getMessage());

            spiderLog.jdbcSave();
        }
        return null;
    }

    
    private void doSpiderCatHotItems(List<String> spiderWordList) {
        
        long startTime = System.currentTimeMillis();
        
        log.warn("do spider word: " + StringUtils.join(spiderWordList, ",") 
                + ", for cid: " + cid + "-------------------");
        
        int totalWordNum = 0;
        int cachedWordNum = 0;//
        int failWordNum = 0;
        
        int totalItemNum = 0;
        int cachedItemNum = 0;
        int failItemNum = 0;

        int catItemNum = (int) CatHotItemPlay.countCatItemByCid(cid);
        int newItemNum = 0;
        
        
        for (String word : spiderWordList) {
            if (StringUtils.isEmpty(word)) {
                continue;
            }
            CatTopWordSearchJob searchJob = new CatTopWordSearchJob(cid, catName, word);
            CatTopWordSearchLog searchLog = searchJob.searchHotItems();
            totalWordNum++;
            if (searchLog.isItemFromTaobao() == false) {
                cachedWordNum++;
            }
            if (searchLog.getTotalItemNum() <= 0) {
                failWordNum++;
            }
            
            totalItemNum += searchLog.getTotalItemNum();
            cachedItemNum += searchLog.getTotalItemNum() - searchLog.getApiGetItemNum();
            failItemNum += searchLog.getFailGetItemNum();
            
            newItemNum += searchLog.getNewItemNum();
            
            catItemNum += searchLog.getNewItemNum();
            
            
            if (catItemNum >= ProperItemNum && totalWordNum >= MinSpiderWordNum) {
                break;
            }
            if (totalWordNum >= MaxSpiderWordNum) {
                break;
            }
        }
        
        if (totalItemNum <= 0) {
            spiderLog.setStatus(CatHotUpdateStatus.NoItemReturn);
        } else {
            spiderLog.setStatus(CatHotUpdateStatus.Success);
        }

        
        spiderLog.setTotalWordNum(totalWordNum);
        spiderLog.setCachedWordNum(cachedWordNum);
        spiderLog.setFailWordNum(failWordNum);
        
        spiderLog.setTotalItemNum(totalItemNum);
        spiderLog.setCachedItemNum(cachedItemNum);
        spiderLog.setFailItemNum(failItemNum);
        
        int deleteItemNum = 0;
        
        spiderLog.setCatItemNum(catItemNum);
        spiderLog.setNewItemNum(newItemNum);
        spiderLog.setDeleteItemNum(deleteItemNum);
        
        
        
        long endTime = System.currentTimeMillis();
        
        long usedTime = endTime - startTime;
        
        spiderLog.setUsedTime(usedTime);

        spiderLog.jdbcSave();
        
        log.info("end spider hot items for cid: " + cid 
                + ", spiderLog: " + spiderLog + ", used " + usedTime + " ms"
                + ", cidIndex: " + cidIndex + ", totalCount: " + totalCount + "----------------------");
        
        return;
    }
    
    
    public static class SpiderWordsGetAction {
        
        public static List<String> findSpiderWordList(Long cid) {
            if (cid == null || cid <= 0L) {
                return new ArrayList<String>();
            }
            ItemCatPlay itemCat = ItemCatPlay.findByCid(cid);
            if (itemCat == null) {
                return new ArrayList<String>();
            }
            CatTopWordPlay topWord = new CatTopWordPlay(cid, itemCat, "");
            
            try {
                long firstCid = topWord.getCidLevel1();
                long secondCid = topWord.getCidLevel2();
                long thirdCid = topWord.getCidLevel3();
                int pn = 1;
                int ps = 100;
                String orderBy = "pv";
                boolean isDesc = true;
                List<CatTopWordPlay> topWordPlayList = new TMApi.TMCatTopWordGetApi(firstCid, secondCid, thirdCid, 
                        pn, ps, orderBy, isDesc).execute();
                if (CommonUtils.isEmpty(topWordPlayList)) {
                    return new ArrayList<String>();
                }
                List<String> wordList = new ArrayList<String>();
                for (CatTopWordPlay topWordPlay : topWordPlayList) {
                    
                    if (topWordPlay == null) {
                        continue;
                    }
                    if (topWordPlay.getItemCount() <= 0) {
                        continue;
                    }
                    String word = topWordPlay.getWord();
                    if (StringUtils.isEmpty(word)) {
                        continue;
                    }
                    if (wordList.contains(word)) {
                        continue;
                    }
                    wordList.add(word);
                    if (wordList.size() >= MaxSpiderWordNum) {
                        break;
                    }
                }
                
                return wordList;
                
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                return new ArrayList<String>();
            }
        }
        
    }
    
    
}
