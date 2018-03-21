package controllers;

import java.util.List;

import job.UpdateCatOrderpayTimeDistributeByDelistTime;
import models.CatClickRatePic;
import models.CatPayHourDistribute;
import models.newCatPayHourDistribute;
import models.item.ItemCatPlay;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;

import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;

public class CatAnalysis extends TMController {

	private static final Logger log = LoggerFactory.getLogger(CatAnalysis.class);

    public static final String TAG = "CatAnalysis";
    
    public static void catPayHour() {
    	render("CatAnalysis/catPayHour.html");
    }
    
    public static void getPayHourDistributeByCid(Long cid) {
    	if(cid == null || cid <= 0) {
    		renderFailedJson("传入的Cid参数不正确");
    	}
    	CatPayHourDistribute distribute = CatPayHourDistribute.findByCid(cid);
    	if(distribute == null) {
    		renderFailedJson("找不到该类目对应的数据");
    	}
    	renderJSON(JsonUtil.getJson(distribute.orderByHour()));
    }
    
    public static void getNewPayHourDistributeByCid(Long cid) {
    	if(cid == null || cid <= 0) {
    		renderFailedJson("传入的Cid参数不正确");
    	}
    	newCatPayHourDistribute distribute = newCatPayHourDistribute.findByCid(cid);
    	if(distribute == null) {
    		renderFailedJson("找不到该类目对应的数据");
    	}
    	renderJSON(JsonUtil.getJson(distribute.orderByHour()));
    }
    
    public static void addCatPayHourByEight() {
    	new CatPayHourDistribute.CatPayHourDistributeBatchOper(16) {
			
			@Override
			public void doForEachDistribute(CatPayHourDistribute distribute) {
				CatPayHourDistribute newDistribute = new CatPayHourDistribute(distribute.getCid(),
						distribute.getClock16(), distribute.getClock17(), distribute.getClock18(),
						distribute.getClock19(), distribute.getClock20(), distribute.getClock21(),
						distribute.getClock22(), distribute.getClock23(), distribute.getClock0(),
						distribute.getClock1(), distribute.getClock2(), distribute.getClock3(),
						distribute.getClock4(), distribute.getClock5(), distribute.getClock6(),
						distribute.getClock7(), distribute.getClock8(), distribute.getClock9(),
						distribute.getClock10(), distribute.getClock11(), distribute.getClock12(),
						distribute.getClock13(), distribute.getClock14(), distribute.getClock15());
				newDistribute.jdbcSave();
				
			}
		}.call();
    }
    
    public static void catClickRate(int pn, int ps, Long cid, String orderBy, String sort) {
    	if(StringUtils.isEmpty(orderBy)) {
    		orderBy = "clickRate";
    	}
    	if(StringUtils.isEmpty(sort)) {
    		sort = "desc";
    	}
    	PageOffset po = new PageOffset(pn, ps, 10);
    	if(cid == null || cid <= 0) {
    		renderFailedJson("请输入正确的cid");
    	}
    	List<CatClickRatePic> res = CatClickRatePic.findByCidWithPo(cid, po, orderBy, sort);
    	int count = (int) CatClickRatePic.countByCid(cid);
    	log.info("count =="  + count);
    	TMResult tmRes = new TMResult(res, count, po);
    	renderJSON(JsonUtil.getJson(tmRes));
    }
    
    public static void updateCatPayHourDistributeByCid(Long cid) {
    	if(cid == null || cid <= 0) {
    		renderFailedJson("传入的cid不合法");
    	}
    	ItemCatPlay itemCatPlay = ItemCatPlay.findByCid(cid);
    	if(itemCatPlay == null) {
    		renderFailedJson("对应的类目不存在");
    	}
    	String catName = itemCatPlay.getName();
    	if(StringUtils.isEmpty(catName)) {
    		renderFailedJson("对应的类目不存在");
    	} 
    	String[] searchWords = catName.split("/");
    	if(searchWords.length <= 0) {
    		renderFailedJson("对应的类目不存在");
    	}
    	String searchWord = searchWords[0];
    	int[] hourArray = UpdateCatOrderpayTimeDistributeByDelistTime.analyseTaobaoDelists(searchWord, "renqi-desc", 30);

    	newCatPayHourDistribute catPayHourDistribute = new newCatPayHourDistribute(hourArray, 
    			cid);
    	boolean isSuccess = catPayHourDistribute.jdbcSave();
    	if(isSuccess) {
    		renderSuccessJson("更新成功");
    	} else {
    		renderFailedJson("更新失败");
    	}
    }
}
