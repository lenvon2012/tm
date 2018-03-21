package controllers;

import java.util.List;

import models.item.ItemCatPlay;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.CacheFor;
import actions.industry.CatPropsIndustryAction;
import actions.industry.RemoteIndustryGetAction.CatPNameResult;
import actions.industry.RemoteIndustryGetAction.CatVNameBaseBean;

public class CategoryIndustry extends TMController {

    private static final Logger log = LoggerFactory.getLogger(CategoryIndustry.class);
    
    public static void index() {
        render("industry/categoryindustry.html");
    }
    
    
    @CacheFor("3h")
    public static void findFirstLevelCat() {
        List<ItemCatPlay> catList = ItemCatPlay.findAllFirstLevelCats();
        renderBusJson(catList);
    }

    @CacheFor("3h")
    public static void findChildCats(Long parentCid) {
        
        if (parentCid == null || parentCid <= 0L) {
            renderFailedJson("请先选择一个父类目！");
        }
        
        List<ItemCatPlay> catList = ItemCatPlay.findByParentCid(parentCid);
        renderBusJson(catList);
    }
    
    
    /**
     * 找到用户宝贝中最多的类目，再找到该类目的一级类目，二级类目
     * 结果是一个list，依次是一级类目，二级类目。。。。
     */
    @CacheFor("3h")
    public static void findUserMostCid() {
        
        Long[] testCidArray = new Long[] {16L, 50013194L};
        
        renderBusJson(testCidArray);
    }
    
    
    public static void queryCategoryProps(Long cid) {
        if (cid == null || cid <= 0L) {
            renderFailedJson("请先选择一个类目！");
        }
        
        List<CatPNameResult> pNameResList = CatPropsIndustryAction.findCatPNameList(cid);
        
        
        renderBusJson(pNameResList);
        
    }
    
    public static void queryPropWordBase(Long cid, Long pid, String orderBy, boolean isDesc) {
        if (cid == null || cid <= 0L) {
            renderFailedJson("请先选择一个类目！");
        }
        
        if (pid == null || pid <= 0L) {
            renderFailedJson("请先选择一个属性！");
        }
        if (StringUtils.isEmpty(orderBy)) {
            orderBy = "pv";
        }
        
        List<CatVNameBaseBean> vNameBaseList = CatPropsIndustryAction.findCatVNameBaseList(cid, pid, 
                orderBy, isDesc);
        
        renderBusJson(vNameBaseList);
    }
    
    
}
