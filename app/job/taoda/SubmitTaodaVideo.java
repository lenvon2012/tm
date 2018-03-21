package job.taoda;

import java.util.ArrayList;
import java.util.List;

import models.taoda.SpiderTaodaVideoLog;
import models.taoda.SpiderTaodaVideoLog.TaodaVideoSpiderStatus;
import models.taoda.SpiderTaodaVideoLog.TaodaVideoSpiderType;
import models.taoda.TaodaOrderPlay;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.JsoupUtil;
import com.ciaosir.client.utils.NumberUtil;

public class SubmitTaodaVideo {
    
    private static final Logger log = LoggerFactory.getLogger(SubmitTaodaVideo.class);
    
    private Long videoId;
    
    private int videoIndex;
    
    private String refer;

    
    public SubmitTaodaVideo(Long videoId, int videoIndex, String refer) {
        super();
        this.videoId = videoId;
        this.videoIndex = videoIndex;
        this.refer = refer;
    }

    public Void call() {
        try {
            long startTime = System.currentTimeMillis();
            
            OrderSpiderResult spiderRes = doSpider();
            
            long endTime = System.currentTimeMillis();
            
            
            if (spiderRes.isSuccess()) {
                int orderNum = 0;
                if (!CommonUtils.isEmpty(spiderRes.getOrderList())) {
                    orderNum = spiderRes.getOrderList().size();
                }
                
                log.info("success spider videoIndex: " + videoIndex 
                        + ", use time " + (endTime - startTime) + " ms, orderPages: " + spiderRes.getPageNum() 
                        + ", get " + orderNum + " orders, url: " + refer + "--------------------------");
            } else {
                log.warn("fail to spider videoIndex: " + videoIndex 
                        + ", use time " + (endTime - startTime) + " ms, msg: " + spiderRes.getMessage() 
                        + "url: " + refer + "--------------------------");
                
            }
            
            return null;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        
        return null;
    }
    
    private static int getOrderPageNum(Document orderDoc) {
        //找到分页
        String cssQuery = "div.deal-list div.b-pager ul li.b-pager-num a";
        
        Elements pageListEles = orderDoc.select(cssQuery);
        
        //如果pageListNum为0，表示没有分页，但当前页还是有的
        if (CommonUtils.isEmpty(pageListEles)) {
            return 1;
        }
        
        Element lastPageEle = pageListEles.get(pageListEles.size() - 1);
        
        int pageListNum = NumberUtil.parserInt(lastPageEle.html(), 0);
        
        return pageListNum;
    }
    
    //"http://www.alibado.com/learning/study/tradeHistory.htm?type=VIDEO&cId=13290&pageNo=1"
    private OrderSpiderResult doSpider() {
        
        if (videoId == null || videoId <= 0) {
            return new OrderSpiderResult(false, "videoId为空！！！！！");
        }
        
        String startOrderUrl = getOrderLink(1);
        
        Document orderDoc = JsoupUtil.loadHtmlDoc(startOrderUrl, refer, true);
        
        if (orderDoc == null) {
            SpiderTaodaVideoLog spiderLog = new SpiderTaodaVideoLog(videoId, 0, 0, 
                    TaodaVideoSpiderStatus.Fail, TaodaVideoSpiderType.VideoPage, startOrderUrl, "爬虫失败，document为空！");
            
            spiderLog.jdbcSave();
            
            return new OrderSpiderResult(false, "jsoup document为空！！！！！");
        }
        int orderPageNum = getOrderPageNum(orderDoc);
        
        if (orderPageNum <= 0) {
            SpiderTaodaVideoLog spiderLog = new SpiderTaodaVideoLog(videoId, 0, orderPageNum, 
                    TaodaVideoSpiderStatus.Fail, TaodaVideoSpiderType.VideoPage, startOrderUrl, "解析失败，没有订购记录");
            
            spiderLog.jdbcSave();
            
            
            return new OrderSpiderResult(false, "orderPageNum为0！！！！！");
        }
        if (orderPageNum <= 1) {
            /*String message = "分页只有" + orderPageNum + "页？？";
            
            SpiderTaodaVideoLog spiderLog = new SpiderTaodaVideoLog(0L, 0, orderPageNum, 
                    TaodaVideoSpiderStatus.Success, TaodaVideoSpiderType.FirstPage, "", message);
            
            spiderLog.jdbcSave();*/
        }
        
        List<TaodaOrderPlay> allOrderList = new ArrayList<TaodaOrderPlay>();
        
        String pageRefer = refer;
        for (int pageIndex = 1; pageIndex <= orderPageNum; pageIndex++) {

            String orderLink = getOrderLink(pageIndex);
            SpiderTaodaOrderJob spiderJob = null;
            if (pageIndex == 1) {
                spiderJob = new SpiderTaodaOrderJob(videoId, pageIndex, orderPageNum, pageRefer, orderLink, orderDoc);
            } else {
                spiderJob = new SpiderTaodaOrderJob(videoId, pageIndex, orderPageNum, pageRefer, orderLink, null);
            }
            
            List<TaodaOrderPlay> orderList = spiderJob.call();
            
            if (!CommonUtils.isEmpty(orderList)) {
                allOrderList.addAll(orderList);
            }
            
            pageRefer = orderLink;
            
            if (spiderJob.isHasSpiderBefore()) {
                log.info("we have spidered videoIndex: " + videoIndex 
                        + ", videoId: " + videoId + " before, pageIndex: " + pageIndex 
                        + ", totalPage: " + orderPageNum + "--------------------------");
                break;
            }
        }
        
        return new OrderSpiderResult(orderPageNum, allOrderList);
    }
    
    private String getOrderLink(int pageIndex) {
        String startOrderUrl = "http://www.alibado.com/learning/study/tradeHistory.htm?type=VIDEO&cId=" +
                videoId + "&pageNo=" + pageIndex;
        
        return startOrderUrl;
    }
    
    public static class OrderSpiderResult {
        private boolean isSuccess;
        private String message;
        private int pageNum;
        private List<TaodaOrderPlay> orderList;
        public OrderSpiderResult(boolean isSuccess, String message) {
            super();
            this.isSuccess = isSuccess;
            this.message = message;
        }
        public OrderSpiderResult(int pageNum, List<TaodaOrderPlay> orderList) {
            super();
            this.isSuccess = true;
            this.pageNum = pageNum;
            this.orderList = orderList;
        }
        public boolean isSuccess() {
            return isSuccess;
        }
        public void setSuccess(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }
        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
        public List<TaodaOrderPlay> getOrderList() {
            return orderList;
        }
        public void setOrderList(List<TaodaOrderPlay> orderList) {
            this.orderList = orderList;
        }
        public int getPageNum() {
            return pageNum;
        }
        public void setPageNum(int pageNum) {
            this.pageNum = pageNum;
        }
        
        
        
        
        
    }
}
