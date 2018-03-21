package job.taoda;

import java.text.SimpleDateFormat;
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

public class SpiderTaodaOrderJob {

    private final static Logger log = LoggerFactory.getLogger(SpiderTaodaOrderJob.class);
    
    private static final int PerPageOrderNum = 15;
    
    private Long videoId;
    
    
    //分页第几页
    private int orderPageIndex;
    
    private int totalPageNum;
    
    private String refer;
    private String orderListUrl;
    
    private Document orderListDoc;
    
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private boolean hasSpiderBefore;

    public SpiderTaodaOrderJob(Long videoId, int orderPageIndex, int totalPageNum,
            String refer, String orderListUrl, Document orderListDoc) {
        super();
        this.videoId = videoId;
        this.orderPageIndex = orderPageIndex;
        this.totalPageNum = totalPageNum;
        this.refer = refer;
        this.orderListUrl = orderListUrl;
        this.orderListDoc = orderListDoc;
    }
    
    
    public boolean isHasSpiderBefore() {
        return hasSpiderBefore;
    }


    public List<TaodaOrderPlay> call() {
        try {
            
            List<TaodaOrderPlay> orderList = doSpider();
            
            //第一页可能也是空的，没有订购记录
            if (CommonUtils.isEmpty(orderList) && orderPageIndex > 1) {
                orderList = new ArrayList<TaodaOrderPlay>(); 
                log.warn("spider " + orderList.size() + " orders, while PerPageOrderNum is " 
                        + PerPageOrderNum + " for pageIndex " + orderPageIndex + "--------------------------");
            } else {
                if (orderList.size() < PerPageOrderNum 
                        && orderPageIndex != totalPageNum) {
                    
                    log.warn("spider " + orderList.size() + " videos, while PerPageOrderNum is " 
                            + PerPageOrderNum + " for pageIndex " + orderPageIndex + ", not the last page--------------------------");
                }
            }
            
            
            
            return orderList;
            
        } catch (Exception ex) {
            log.warn(ex.getMessage(), ex);
            
            return new ArrayList<TaodaOrderPlay>();
        }
    }
    
    
    private List<TaodaOrderPlay> doSpider() {
        if (orderListDoc == null) {
            orderListDoc = JsoupUtil.loadHtmlDoc(orderListUrl, refer, true);
        }
        
        if (orderListDoc == null) {
            String message = "爬虫失败，没有order!!";
            
            //log
            SpiderTaodaVideoLog spiderLog = new SpiderTaodaVideoLog(videoId, orderPageIndex, 0, 
                    TaodaVideoSpiderStatus.Fail, TaodaVideoSpiderType.OrderPage, orderListUrl, message);
            
            spiderLog.jdbcSave();
            return new ArrayList<TaodaOrderPlay>(); 
        }
        
        String cssQuery = "div.deal-list div.deal-content table.deal-list-table tbody tr";
        
        Elements orderEleList = orderListDoc.select(cssQuery);
        if (CommonUtils.isEmpty(orderEleList)) {
            return new ArrayList<TaodaOrderPlay>(); 
        }
        
        List<TaodaOrderPlay> orderList = new ArrayList<TaodaOrderPlay>();
        for (Element orderEle : orderEleList) {
            TaodaOrderPlay orderPlay = toTaodaOrderPlay(orderEle);
            if (orderPlay == null) {
                continue;
            }
            orderPlay.jdbcSave();
            orderList.add(orderPlay);
        }
        
        return orderList;
        
    }
    
    private TaodaOrderPlay toTaodaOrderPlay(Element orderEle) {
        if (orderEle == null) {
            return null;
        }
        
        String userNick = getUserNick(orderEle);
        String priceStr = getPriceStr(orderEle);
        int orderNum = getOrderNum(orderEle);
        String orderTsStr = getOrderTsStr(orderEle);
        long orderTs = 0;
        try {
            orderTs = sdf.parse(orderTsStr).getTime();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        //在没存之前，判断之前有没有爬取过
        if (TaodaOrderPlay.hasExisted(videoId, userNick, orderTs)) {
            hasSpiderBefore = true;
        }
        
        TaodaOrderPlay orderPlay = new TaodaOrderPlay(videoId, userNick, priceStr, orderNum, orderTsStr, orderTs);
        
        return orderPlay;
        
    }
    
    private static String getUserNick(Element orderEle) {
        String cssQuery = "td:eq(0) a.deal-list-buyer";
        Elements eles = orderEle.select(cssQuery);
        
        if (CommonUtils.isEmpty(eles)) {
            return "";
        }
        
        String userNick = eles.get(0).html();
        
        return userNick;
    }
    
    private static String getPriceStr(Element orderEle) {
        String cssQuery = "td:eq(1) span.x-highlight strong";
        Elements eles = orderEle.select(cssQuery);
        
        if (CommonUtils.isEmpty(eles)) {
            return "";
        }
        
        String priceStr = eles.get(0).html();
        
        return priceStr;
    }
    
    private static int getOrderNum(Element orderEle) {
        String cssQuery = "td:eq(2)";
        Elements eles = orderEle.select(cssQuery);
        
        if (CommonUtils.isEmpty(eles)) {
            return 0;
        }
        
        String orderNumStr = eles.get(0).html();
        
        int orderNum = NumberUtil.parserInt(orderNumStr, 0);
        
        return orderNum;
    }
    
    private static String getOrderTsStr(Element orderEle) {
        String cssQuery = "td:eq(3) span.x-hint";
        Elements eles = orderEle.select(cssQuery);
        
        if (CommonUtils.isEmpty(eles)) {
            return "";
        }
        
        String orderTsStr = eles.get(0).html();
        
        return orderTsStr;
    }
    
}
