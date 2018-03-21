package job.taoda;

import java.util.concurrent.Callable;

import models.taoda.SpiderTaodaVideoLog;
import models.taoda.SpiderTaodaVideoLog.TaodaVideoSpiderStatus;
import models.taoda.SpiderTaodaVideoLog.TaodaVideoSpiderType;
import models.taoda.TaodaVideoPlay;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.NumberUtil;

public class SpiderTaodaVideoCaller implements Callable<Void> {

    private static final Logger log = LoggerFactory.getLogger(SpiderTaodaVideoCaller.class);
    
    private int videoIndex;
    
    private Element videoEle;
    
    public SpiderTaodaVideoCaller(int videoIndex, Element videoEle) {
        super();
        this.videoIndex = videoIndex;
        this.videoEle = videoEle;
    }

    @Override
    public Void call() throws Exception {
        try {
            //log.info("start do for videoIndex: " + videoIndex + "-----------------------");
            String videoLink = getVideoLink();
            Long videoId = getVideoId(videoLink);
            if (videoId == null || videoId <= 0L) {
                log.warn("fail to get videoId while videoIndex: " + videoIndex + ", videoLink: " + videoLink + "---------------");
                //log
                SpiderTaodaVideoLog spiderLog = new SpiderTaodaVideoLog(0L, videoIndex, 0, 
                        TaodaVideoSpiderStatus.Fail, TaodaVideoSpiderType.VideoPage, videoLink, "无法解析出videoLink");
                
                spiderLog.jdbcSave();
                return null;
            }
            
            String teacher = getTeacher();
            String title = getTitle();
            int orderNum = getOrderNum();
            
            TaodaVideoPlay videoPlay = new TaodaVideoPlay(videoId, title, teacher, orderNum, TaodaVideoSpiderStatus.Success);
            videoPlay.jdbcSave();
            
            SubmitTaodaVideo submitVideo = new SubmitTaodaVideo(videoId, videoIndex, videoLink);
            submitVideo.call();
            
            return null;
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            
            return null;
        }
    }
    
    private Long getVideoId(String videoLink) {
        if (StringUtils.isEmpty(videoLink)) {
            return 0L;
        }
        int startIndex = videoLink.lastIndexOf("detail-");
        if (startIndex < 0) {
            return 0L;
        }
        startIndex += "detail-".length();
        int endIndex = videoLink.indexOf(".htm", startIndex);
        if (endIndex < 0) {
            return 0L;
        }
        
        String idStr = videoLink.substring(startIndex, endIndex);
        
        Long videoId = NumberUtil.parserLong(idStr, 0L);
        return videoId;
    }
    
    private String getTeacher() {
        String cssQuery = "div.result-desc div.result-desc-main p.result-desc-teacher a";
        
        Elements elements = videoEle.select(cssQuery);
        if (CommonUtils.isEmpty(elements)) {
            return "";
        }
        String teacher = elements.get(0).html();
        
        int index = teacher.indexOf("<img");
        if (index > 0) {
            teacher = teacher.substring(0, index);
        }
        
        return teacher;
    }
    
    private String getVideoLink() {
        String cssQuery = "div.result-desc div.result-desc-main h4 a";
        
        Elements elements = videoEle.select(cssQuery);
        if (CommonUtils.isEmpty(elements)) {
            return "";
        }
        String link = elements.get(0).attr("href");
        
        return link;
    }
    
    private String getTitle() {
        String cssQuery = "div.result-desc div.result-desc-main h4 a";
        
        Elements elements = videoEle.select(cssQuery);
        if (CommonUtils.isEmpty(elements)) {
            return "";
        }
        String title = elements.get(0).html();
        
        return title;
    }
    
    private int getOrderNum() {
        String cssQuery = "div.result-desc div.result-desc-other";
        
        Elements elements = videoEle.select(cssQuery);
        if (CommonUtils.isEmpty(elements)) {
            return 0;
        }
        String str = elements.get(0).html();
        
        int index = str.indexOf("人已学习");
        if (index < 0) {
            return 0;
        }
        
        str = str.substring(0, index);
        
        int orderNum = NumberUtil.parserInt(str, 0);
        
        return orderNum;
    }
    

}
