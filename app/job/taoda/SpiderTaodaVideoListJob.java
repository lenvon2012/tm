package job.taoda;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import models.taoda.SpiderTaodaVideoLog;
import models.taoda.SpiderTaodaVideoLog.TaodaVideoSpiderStatus;
import models.taoda.SpiderTaodaVideoLog.TaodaVideoSpiderType;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.JsoupUtil;

public class SpiderTaodaVideoListJob implements Callable<List<SpiderTaodaVideoCaller>> {

    private final static Logger log = LoggerFactory.getLogger(SpiderTaodaVideoListJob.class);
    
    private static final int PerPageVideoNum = 20;
    
    private int listIndex;
    private int totalPageNum;
    
    private String refer;
    private String videoListUrl;
    
    private Document videoListDoc;
    
    

    public SpiderTaodaVideoListJob(int listIndex, int totalPageNum, String refer,
            String videoListUrl, Document videoListDoc) {
        super();
        this.listIndex = listIndex;
        this.totalPageNum = totalPageNum;
        this.refer = refer;
        this.videoListUrl = videoListUrl;
        this.videoListDoc = videoListDoc;
    }

    @Override
    public List<SpiderTaodaVideoCaller> call() throws Exception {
        try {
            
            List<SpiderTaodaVideoCaller> callerList = doSpider();
            
            if (CommonUtils.isEmpty(callerList)) {
                callerList = new ArrayList<SpiderTaodaVideoCaller>(); 
                log.warn("spider " + callerList.size() + " videos, while PerPageVideoNum is " 
                        + PerPageVideoNum + " for pageIndex " + listIndex + "--------------------------");
            } else {
                if (callerList.size() < PerPageVideoNum && listIndex != totalPageNum) {
                    log.warn("spider " + callerList.size() + " videos, while PerPageVideoNum is " 
                            + PerPageVideoNum + " for pageIndex " + listIndex + ", not the last page--------------------------");
                }
            }
            
            
            
            int status = TaodaVideoSpiderStatus.Success;
            String pageLink = "";
            String message = "";
            if (CommonUtils.isEmpty(callerList)) {
                status = TaodaVideoSpiderStatus.Fail;
                pageLink = videoListUrl;
                message = "爬虫失败，没有video!!";
            }
            
            //log
            SpiderTaodaVideoLog spiderLog = new SpiderTaodaVideoLog(0L, listIndex, callerList.size(), 
                    status, TaodaVideoSpiderType.VideoList, pageLink, message);
            
            spiderLog.jdbcSave();
            
            
            return callerList;
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            
            return new ArrayList<SpiderTaodaVideoCaller>();
        }
    }
    
    private List<SpiderTaodaVideoCaller> doSpider() {
        if (videoListDoc == null) {
            videoListDoc = JsoupUtil.loadHtmlDoc(videoListUrl, refer, true);
        }
        
        if (videoListDoc == null) {
            return new ArrayList<SpiderTaodaVideoCaller>(); 
        }
        
        String cssQuery = "div.x-layout-main div.result div.result-body ul#J_ResultList li";
        
        Elements videoEles = videoListDoc.select(cssQuery);
        if (CommonUtils.isEmpty(videoEles)) {
            return new ArrayList<SpiderTaodaVideoCaller>(); 
        }
        
        List<SpiderTaodaVideoCaller> callerList = new ArrayList<SpiderTaodaVideoCaller>();
        
        for (int i = 0; i < videoEles.size(); i++) {
            int videoIndex = (listIndex - 1) * PerPageVideoNum + i + 1;
            //String videoRefer = videoListUrl;
            SpiderTaodaVideoCaller caller = new SpiderTaodaVideoCaller(videoIndex, videoEles.get(i));
            callerList.add(caller);
        }
        
        return callerList;
        
    }

}
