package job;

import java.util.List;

import models.item.ItemCatPlay;
import models.user.User;
import models.word.top.NoMatchTopURLBaseCid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.ItemCat;

import bustbapi.ItemCatApi;

import configs.TMConfigs;
import controllers.newAutoTitle;

import play.jobs.Job;

public class FixNoMatchTopURLBaseCidJob extends Job{

	static final Logger log = LoggerFactory.getLogger(FixNoMatchTopURLBaseCidJob.class);

	public static String TAG = "FixNoMatchTopURLBaseCidJob";
	
	public static int count = 0;
	
	@Override
	public void doJob(){
		
		Thread.currentThread().setName(TAG);
 
        /*if (!TMConfigs.Server.jobTimerEnable) {
            return;
        }*/
		count = 0;
        new NoMatchTopURLBaseCid.NoMatchTopURLBaseCidBatchOper(32){
        	@Override
            public void doForEachUser(final NoMatchTopURLBaseCid cat) {
                this.sleepTime = 100L;
                updateCat(cat);
            }
        }.call();
	}
	
	public static boolean updateCat(NoMatchTopURLBaseCid cat) {
		log.info("FixNoMatchTopURLBaseCidJob count = [" + count++ + "]");
		if(cat == null) {
			return false;
		}
		
		Long cid = cat.getCid();
		
		if(cid== null || cid <= 0) {
			return false;
		}
		ItemCatPlay itemCatPlay = ItemCatPlay.findByCid(cid);
		
		// 如果数据库中没有该类目，则更新
		if(itemCatPlay == null){
			ItemCatPlay.updateNewCatByTaobaoCat(cid);
		}
		
		return false;
	}
	
}
