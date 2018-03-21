package job.writter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import models.item.ItemCatHotProps;
import models.item.ItemCatPlay;
import models.item.NoPropsItemCat;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import controllers.APIConfig;

@Every("10s")
public class NoPropsItemCatWritter extends Job {

	private static final Logger log = LoggerFactory.getLogger(NoPropsItemCatWritter.class);

    public static final String TAG = "NoPropsItemCatWritter";

    static Queue<ItemCatPlay> msgs = new ConcurrentLinkedQueue<ItemCatPlay>();

    public void doJob() {
    	if(APIConfig.get().getApp() != 21348761){
    		return;
    	}
    	
    	ItemCatPlay cat2 = null;
    	
    	log.info("NoPropsItemCatWritter queue size = " + msgs.size());
    	
        while ((cat2 = msgs.poll()) != null) {
        	String res = ItemCatHotProps.getCachedRecent(cat2.getCid());
    		if (StringUtils.isEmpty(res) || res.equals("[]")) {
    			new NoPropsItemCat(cat2.getCid(), cat2.getName()).jdbcSave();
    		}
        }
    }
    
    public static void addMsg(ItemCatPlay msg) {
        msgs.add(msg);
    }
}
