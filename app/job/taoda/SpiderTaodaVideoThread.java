package job.taoda;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;

import models.taoda.SpiderTaodaVideoLog;
import models.taoda.SpiderTaodaVideoLog.TaodaVideoSpiderStatus;
import models.taoda.SpiderTaodaVideoLog.TaodaVideoSpiderType;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.utils.JsoupUtil;
import com.ciaosir.client.utils.NumberUtil;

public class SpiderTaodaVideoThread extends Job {

    private final static Logger log = LoggerFactory.getLogger(SpiderTaodaVideoThread.class);
    
    private static final String StrartUrl = "http://xue.alibado.com/search/list-c-p-k-s-o-t-f.htm";
    
    private static final String Refer = "http://xue.alibado.com/";
    
    private static PYFutureTaskPool<List<SpiderTaodaVideoCaller>> videoListPool = 
            new PYFutureTaskPool<List<SpiderTaodaVideoCaller>>(10);
    
    private static PYFutureTaskPool<Void> videoPagePool = new PYFutureTaskPool<Void>(20);
    
    private static final boolean isDebug = true;
    
    @Override
    public void doJob() {
        doSpiderVideoList();
    }
    
    private static void doSpiderVideoList() {
        try {
            
            long startTime = System.currentTimeMillis();
            
            List<SpiderTaodaVideoCaller> callerList = getAllSpiderCaller();
            
            log.warn("total get " + callerList.size() + " videos----------------------------");
            
            List<FutureTask<Void>> promises = new ArrayList<FutureTask<Void>>();
            
            for (SpiderTaodaVideoCaller caller : callerList) {
                promises.add(videoPagePool.submit(caller));
            }
            
            for (FutureTask<Void> promise : promises) {
                try {
                    promise.get();
                } catch (InterruptedException e) {
                    log.warn(e.getMessage(), e);
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
            }
            
            long endTime = System.currentTimeMillis();
            
            log.warn("end spider of taoda, used time " + (endTime - startTime) / 1000 + " s---------------");
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }
    
    private static int getTotalPageListNum(Document videoListDoc) {
        //找到分页
        String cssQuery = "div.result-footer div.x-pager.x-right ul.x-pager-list li.x-pager-num a";
        
        Elements pageListEles = videoListDoc.select(cssQuery);
        //如果pageListNum为0，表示没有分页，但当前页还是有的
        if (CommonUtils.isEmpty(pageListEles)) {
            return 1;
        }
        
        Element lastPageEle = pageListEles.get(pageListEles.size() - 1);
        
        int pageListNum = NumberUtil.parserInt(lastPageEle.html(), 0);
        
        log.warn("find " + pageListNum + " pages of taodao video-------------------");
        
        return pageListNum;
    }
    
    
    
    private static List<SpiderTaodaVideoCaller> getAllSpiderCaller() {
        Document videoListDoc = JsoupUtil.loadHtmlDoc(StrartUrl, Refer, true);
        
        if (videoListDoc == null) {
          //log
            SpiderTaodaVideoLog spiderLog = new SpiderTaodaVideoLog(0L, 0, 0, 
                    TaodaVideoSpiderStatus.Fail, TaodaVideoSpiderType.FirstPage, StrartUrl, "首页爬虫失败！！！！");
            
            spiderLog.jdbcSave();
            return new ArrayList<SpiderTaodaVideoCaller>();
        }
        
        
        int pageListNum = getTotalPageListNum(videoListDoc);
        if (isDebug) {
            //pageListNum = 2;
        }
        
        //log
        String message = "";
        if (pageListNum <= 1) {
            message = "分页只有" + pageListNum + "页？？";
        }
        SpiderTaodaVideoLog spiderLog = new SpiderTaodaVideoLog(0L, 0, pageListNum, 
                TaodaVideoSpiderStatus.Success, TaodaVideoSpiderType.FirstPage, "", message);
        
        spiderLog.jdbcSave();
        
        
        List<FutureTask<List<SpiderTaodaVideoCaller>>> listPromises = 
                new ArrayList<FutureTask<List<SpiderTaodaVideoCaller>>>();
        
        String listRefer = Refer;
        for (int listIndex = 1; listIndex <= pageListNum; listIndex++) {

            String listUrl = "";
            SpiderTaodaVideoListJob spiderJob = null;
            if (listIndex == 1) {
                listUrl = StrartUrl;
                spiderJob = new SpiderTaodaVideoListJob(listIndex, pageListNum, listRefer, listUrl, videoListDoc);
            } else {
                listUrl = "http://xue.alibado.com/search/list-c-p" + listIndex + "-k-s-o-t-f.htm";
                spiderJob = new SpiderTaodaVideoListJob(listIndex, pageListNum, listRefer, listUrl, null);
            }
            
            if (isDebug) {
                //log.info("listUrl: " + listUrl + ", listRefer: " + listRefer + "-----------------");
            }
            
            FutureTask<List<SpiderTaodaVideoCaller>> promise = videoListPool.submit(spiderJob);
            listPromises.add(promise);
            
            listRefer = listUrl;
        }
        
        
        List<SpiderTaodaVideoCaller> resultList = new ArrayList<SpiderTaodaVideoCaller>();
        
        for (FutureTask<List<SpiderTaodaVideoCaller>> promise : listPromises) {
            try {
                List<SpiderTaodaVideoCaller> tempList = promise.get();
                if (CommonUtils.isEmpty(tempList) == false) {
                    resultList.addAll(tempList);
                }
            } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
        
        return resultList;
    }
    
    
    
}
