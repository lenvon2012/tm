
package job.writter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import models.mysql.word.TmpWordBase;
import models.mysql.word.WordBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;

import com.ciaosir.client.CommonUtils;

import controllers.APIConfig;


//@Every("3s")
public class RawWordBaseUpdateWritter extends Job {

    private static final Logger log = LoggerFactory.getLogger(RawWordBaseUpdateWritter.class);

    public static final String TAG = "RawWordBaseUpdateWritter";

    static Queue<TmpWordBase> msgs = new ConcurrentLinkedQueue<TmpWordBase>();

    public void doJob() {
    	if(APIConfig.get().getApp() != 21348761){
    		return;
    	}
    	TmpWordBase msg = null;
    	log.info("RawWordBaseUpdateWritter queue size = " + msgs.size());
        while ((msg = msgs.poll()) != null) {
            msg.rawInsert();
        }
    }
    
    public static void addMsg(WordBase msg) {
        msgs.add(new TmpWordBase(msg));
        if(msgs.size() > 512) {
        	CommonUtils.sleepQuietly(10000);
        }
    }
}
