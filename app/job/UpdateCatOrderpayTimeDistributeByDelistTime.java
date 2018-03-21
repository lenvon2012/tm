package job;

import java.util.concurrent.Callable;

import models.newCatPayHourDistribute;
import models.item.ItemCatPlay;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import titleDiag.DiagResult;
import actions.industry.IndustryDelistResultAction;

import com.ciaosir.client.CommonUtils;

import configs.TMConfigs;

public class UpdateCatOrderpayTimeDistributeByDelistTime extends Job{

	static final Logger log = LoggerFactory.getLogger(UpdateCatOrderpayTimeDistributeByDelistTime.class);

	public static String TAG = "UpdateCatOrderpayTimeDistributeByDelistTime";
	
	public static int totalCount = 0;
	public static int lessThanFifty = 0;
	public static int catSearchLessThanFifty = 0;
	public static int successCount = 0;
	public static int failCount = 0;
	public static Long jobTs = 0L;

	@Override
	public void doJob() {
		
		Thread.currentThread().setName(TAG);
 
		totalCount = 0;
		lessThanFifty = 0;
		successCount = 0;
		failCount = 0;
		jobTs = System.currentTimeMillis();
        /*if (!TMConfigs.Server.jobTimerEnable) {
            return;
        }*/
        
        new ItemCatPlay.ItemCatPlayBatchOper(16) {
            @Override
            public void doForEachItemCat(final ItemCatPlay itemCatPlay) {
            	totalCount++;
            	newCatPayHourDistribute catPayHourDistribute = newCatPayHourDistribute
            			.findByCid(itemCatPlay.getCid());
            	Integer count = 0;
            	if(catPayHourDistribute != null) {
            		count = catPayHourDistribute.getTotalTradeCount();
            	}
            	
            	if(count >= 50) {
            		return;
            	}
            	lessThanFifty++;
                TMConfigs.getDiagResultPool().submit(new CatCaller(itemCatPlay));
                CommonUtils.sleepQuietly(10000);
            }
        }.call();
	}
	
	public static class CatCaller implements Callable<DiagResult> {
		ItemCatPlay cat;

        public CatCaller(ItemCatPlay cat) {
            super();
            this.cat = cat;
        }

        @Override
        public DiagResult call() {
            try {
                doWithItemCatPlay(cat);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
            return null;
        }
        
        public void doWithItemCatPlay(final ItemCatPlay itemCatPlay) {

        	// 使用行业上下架分析数据
        	Long cid = itemCatPlay.getCid();
        	log.info("UpdateCatOrderpayTimeDistributeByDelistTime : Now we need to rebuild" +
        			" the newCatPayHourDistribute using analyseTaobaoDelists for cid = " + cid);
        	
        	if(cid == null || cid <= 0) {
        		return;
        	}
        	
        	String catName = itemCatPlay.getName();
        	if(StringUtils.isEmpty(catName)) {
        		return;
        	}
        	String[] searchWords = catName.split("/");
        	if(searchWords.length <= 0) {
        		return;
        	}
        	String searchWord = searchWords[0];
        	int[] hourArray = analyseTaobaoDelists(searchWord, "renqi-desc", 30);
        	int catSearchCount = 0;
        	for(int i = 0; i < 24; i++) {
        		catSearchCount += hourArray[i];
        	}
        	if(catSearchCount < 50) {
        		catSearchLessThanFifty++;
        	} 
        	newCatPayHourDistribute catPayHourDistribute = new newCatPayHourDistribute(hourArray, 
        			itemCatPlay.getCid());
        	boolean isSuccess = catPayHourDistribute.jdbcSave();
        	if(isSuccess) {
        		successCount++;
        	} else {
        		failCount++;
        	}
        }
    }
	
	public static int[] analyseTaobaoDelists(String searchKey, String itemOrderType, int searchPages) {
		int[] hourDelistArray = new int [7 * 24];
        if (StringUtils.isBlank(searchKey)) {
            return hourDelistArray;
        }
        searchKey = searchKey.trim();
        if (StringUtils.isBlank(searchKey)) {
        	return hourDelistArray;
        }
        if (StringUtils.isEmpty(itemOrderType)) {
        	return hourDelistArray;
        }
        if (searchPages <= 0) {
        	return hourDelistArray;
        }
        
        hourDelistArray = IndustryDelistResultAction.countTaobaoItemHourlyDelist(searchKey, 
                itemOrderType, searchPages);

        return formatHour(hourDelistArray);
    }

	public static int[] formatHour(int[] hourDelistArray){
		int[] houeArray = new int [24];
		for(int i = 0; i < 7 * 24; i++) {
			int index = i % 24;
			houeArray[index] = houeArray[index] + hourDelistArray[i];
		}
		return houeArray;
	}
}
