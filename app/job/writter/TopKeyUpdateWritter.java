
package job.writter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import models.word.top.TmpTopKey;
import models.word.top.TopKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;

import com.ciaosir.client.CommonUtils;


//@Every("3s")
public class TopKeyUpdateWritter extends Job {

    private static final Logger log = LoggerFactory.getLogger(TopKeyUpdateWritter.class);

    public static final String TAG = "TitleLogWritter";

    static Queue<TmpTopKey> msgs = new ConcurrentLinkedQueue<TmpTopKey>();

    public void doJob() {
//    	if(APIConfig.get().getApp() != 21255586){
//    		return;
//    	}

    	TmpTopKey msg = null;
    	log.info("TopKeyUpdateWritter queue size = " + msgs.size());
        while ((msg = msgs.poll()) != null) {
        	msg.id = null;
            msg.save();
        }
    }
    
    public static void addMsg(TopKey msg) {
        msgs.add(new TmpTopKey(msg));
        if(msgs.size() > 512) {
        	CommonUtils.sleepQuietly(10000);
        }
    }
}
