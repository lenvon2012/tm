package controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actions.industry.SearchIndustryAction;
import actions.industry.SearchIndustryAction.IndustryPriceIntervalInfo;
import actions.industry.SearchIndustryAction.IndustrySummaryInfo;
import actions.industry.SearchIndustryAction.SearchIndustryRule;

import com.ciaosir.client.CommonUtils;

public class SearchIndustry extends TMController {
    
    private static final Logger log = LoggerFactory.getLogger(SearchIndustry.class);
    
    public static void index() {
        render("industry/searchindustry.html");
    }
    
    public static void summaryIndustryInfo(SearchIndustryRule searchRule) {
        if (searchRule == null) {
            renderFailedJson("请先设置搜索条件！");
        }
        
        IndustrySummaryInfo summaryInfo = SearchIndustryAction.summarySearchIndustry(searchRule);
        
        renderBusJson(summaryInfo);
    }
    
    
    public static void searchPriceIntervalInfos(SearchIndustryRule searchRule, int splitNum) {
        
        if (searchRule == null) {
            renderFailedJson("请先设置搜索条件！");
        }
        
        if (splitNum <= 0) {
            renderFailedJson("请先设置分段个数！");
        }
        
        List<IndustryPriceIntervalInfo> priceInfoList = SearchIndustryAction.searchPriceIntervalInfos(searchRule, 
                splitNum);
        
        if (CommonUtils.isEmpty(priceInfoList)) {
            renderFailedJson("系统出现异常，无法获取到宝贝，请稍后重试一次！");
        }
        
        renderBusJson(priceInfoList);
    }
    
}
