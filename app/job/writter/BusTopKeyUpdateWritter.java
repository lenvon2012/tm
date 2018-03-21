
package job.writter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import models.word.top.BusTopKey;
import models.word.top.TmpBusTopKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;

import com.ciaosir.client.CommonUtils;

import controllers.APIConfig;


//@Every("3s")
public class BusTopKeyUpdateWritter extends Job {

    private static final Logger log = LoggerFactory.getLogger(BusTopKeyUpdateWritter.class);

    public static final String TAG = "BusTopKeyUpdateWritter";

    static Queue<TmpBusTopKey> msgs = new ConcurrentLinkedQueue<TmpBusTopKey>();

    public void doJob() {
    	if(APIConfig.get().getApp() != 21255586){
    		return;
    	}
    	TmpBusTopKey msg = null;
    	log.info("BusTopKeyUpdateWritter queue size = " + msgs.size());
        while ((msg = msgs.poll()) != null) {
            msg.rawInsert();
        }
    }
    
    public static void addMsg(BusTopKey msg) {
        msgs.add(new TmpBusTopKey(msg));
        if(msgs.size() > 512) {
        	CommonUtils.sleepQuietly(10000);
        }
    }
}
