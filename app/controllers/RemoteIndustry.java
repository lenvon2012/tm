package controllers;

import java.net.URLDecoder;
import java.util.List;

import models.item.ItemCatPlay;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.CacheFor;
import play.mvc.Controller;
import result.TMResult;
import actions.industry.CatPropsIndustryAction;
import actions.industry.IndustryDelistResultAction;
import actions.industry.RemoteIndustryGetAction.CatPNameResult;
import actions.industry.RemoteIndustryGetAction.CatVNameBaseBean;

import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;

import controllers.TMController.BusUIResult;

public class RemoteIndustry extends Controller {
    
    private static final Logger log = LoggerFactory.getLogger(RemoteIndustry.class);
    
    
    private static void renderJsonp(String json, String callback) {
        
        /*
        StringBuilder sb = new StringBuilder();
        sb.append(callback);
        sb.append('(');
        sb.append(json);
        sb.append(')');

        json = sb.toString();
        */
        renderJSON(json);
    }
    
    protected static void renderBusJsonp(Object json, String callback) {
        renderJsonp(JsonUtil.getJson(new BusUIResult(json)), callback);
    }

    protected static void renderFailedJsonp(String message, String callback) {
        renderJsonp(JsonUtil.getJson(new BusUIResult(false, message)), callback);
    }
    
    
    private static String decodeSearchKey(String searchKey) {
        if (StringUtils.isEmpty(searchKey)) {
            return "";
        }
        
        try {
            searchKey = URLDecoder.decode(searchKey, "utf-8");
            return searchKey;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return "";
        }
    }
    
    
    public static void analyseTaobaoDelists(String searchKey, String itemOrderType, 
            int searchPages, String callback) {
        searchKey = decodeSearchKey(searchKey);
        
        if (StringUtils.isBlank(searchKey)) {
            renderFailedJsonp("搜索关键词不能为空！", callback);
        }
        searchKey = searchKey.trim();
        if (StringUtils.isBlank(searchKey)) {
            renderFailedJsonp("搜索关键词不能为空！", callback);
        }
        if (StringUtils.isEmpty(itemOrderType)) {
            renderFailedJsonp("请先选择宝贝要排序的方式！", callback);
        }
        if (searchPages <= 0) {
            renderFailedJsonp("请先选择要搜索的宝贝页数！", callback);
        }
        
        int[] hourDelistArray = IndustryDelistResultAction.countTaobaoItemHourlyDelist(searchKey, 
                itemOrderType, searchPages);
        
        renderBusJsonp(hourDelistArray, callback);
        
    }
    
    public static void searchTaobaoItems(String searchKey, String itemOrderType, int searchPages, 
            String orderBy, boolean isDesc, int pn, int ps, String callback) {
        
        searchKey = decodeSearchKey(searchKey);
        
        if (StringUtils.isBlank(searchKey)) {
            renderFailedJsonp("搜索关键词不能为空！", callback);
        }
        searchKey = searchKey.trim();
        if (StringUtils.isBlank(searchKey)) {
            renderFailedJsonp("搜索关键词不能为空！", callback);
        }
        if (StringUtils.isEmpty(itemOrderType)) {
            renderFailedJsonp("请先选择宝贝要排序的方式！", callback);
        }
        if (searchPages <= 0) {
            renderFailedJsonp("请先选择要搜索的宝贝页数！", callback);
        }
        
        PageOffset po = new PageOffset(pn, ps);
        
        TMResult tmResult = IndustryDelistResultAction.findTaobaoItemsWithPaging(searchKey, 
                itemOrderType, searchPages, 
                orderBy, isDesc, po);
        
        renderJsonp(JsonUtil.getJson(tmResult), callback);
        
    }
    
    //行业类目属性
    
    @CacheFor("3h")
    public static void findFirstLevelCat(String callback) {
        List<ItemCatPlay> catList = ItemCatPlay.findAllFirstLevelCats();
        renderBusJsonp(catList, callback);
    }

    @CacheFor("3h")
    public static void findChildCats(Long parentCid, String callback) {
        
        if (parentCid == null || parentCid <= 0L) {
            renderFailedJsonp("请先选择一个父类目！", callback);
        }
        
        List<ItemCatPlay> catList = ItemCatPlay.findByParentCid(parentCid);
        renderBusJsonp(catList, callback);
    }
    
    
    
    
    public static void queryCategoryProps(Long cid, String callback) {
        if (cid == null || cid <= 0L) {
            renderFailedJsonp("请先选择一个类目！", callback);
        }
        
        List<CatPNameResult> pNameResList = CatPropsIndustryAction.findCatPNameList(cid);
        
        
        renderBusJsonp(pNameResList, callback);
        
    }
    
    public static void queryPropWordBase(Long cid, Long pid, String orderBy, boolean isDesc, String callback) {
        if (cid == null || cid <= 0L) {
            renderFailedJsonp("请先选择一个类目！", callback);
        }
        
        if (pid == null || pid <= 0L) {
            renderFailedJsonp("请先选择一个属性！", callback);
        }
        if (StringUtils.isEmpty(orderBy)) {
            orderBy = "pv";
        }
        
        List<CatVNameBaseBean> vNameBaseList = CatPropsIndustryAction.findCatVNameBaseList(cid, pid, 
                orderBy, isDesc);
        
        renderBusJsonp(vNameBaseList, callback);
    }
    
     
    //行业情报
    
    /*
    private static SearchIndustryRule toSearchRule(String searchKey, String itemOrderType, int searchPages,
            double startPrice, double endPrice, double startSales, double endSales) {
        searchKey = decodeSearchKey(searchKey);
        
        if (StringUtils.isEmpty(searchKey)) {
            return null;
        }
        
        SearchIndustryRule searchRule = new SearchIndustryRule(searchKey, itemOrderType, searchPages, 
                startPrice, endPrice, startSales, endSales);
        
        return searchRule;
    }
    
    public static void summaryIndustryInfo(String searchKey, String itemOrderType, int searchPages,
            double startPrice, double endPrice, double startSales, double endSales, String callback) {
        
        SearchIndustryRule searchRule = toSearchRule(searchKey, itemOrderType, searchPages, 
                startPrice, endPrice, startSales, endSales);
        
        if (searchRule == null) {
            renderFailedJsonp("请先设置搜索条件！", callback);
        }
        
        IndustrySummaryInfo summaryInfo = SearchIndustryAction.summarySearchIndustry(searchRule);
        
        renderBusJsonp(summaryInfo, callback);
    }
    
    
    public static void searchPriceIntervalInfos(String searchKey, String itemOrderType, int searchPages,
            double startPrice, double endPrice, double startSales, double endSales,
            int splitNum, String callback) {
        
        SearchIndustryRule searchRule = toSearchRule(searchKey, itemOrderType, searchPages, 
                startPrice, endPrice, startSales, endSales);
        
        if (searchRule == null) {
            renderFailedJsonp("请先设置搜索条件！", callback);
        }
        
        if (splitNum <= 0) {
            renderFailedJsonp("请先设置分段个数！", callback);
        }
        
        List<IndustryPriceIntervalInfo> priceInfoList = SearchIndustryAction.searchPriceIntervalInfos(searchRule, 
                splitNum);
        
        if (CommonUtils.isEmpty(priceInfoList)) {
            renderFailedJsonp("系统出现异常，无法获取到宝贝，请稍后重试一次！", callback);
        }
        
        renderBusJsonp(priceInfoList, callback);
    }
    */
    
}
