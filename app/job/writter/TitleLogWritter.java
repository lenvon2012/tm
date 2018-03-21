
package job.writter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import models.oplog.TitleOptimiseLog;
import models.oplog.TitleOptimiseLog.TitleOptimiseMsg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;

@Every("3s")
public class TitleLogWritter extends Job {

    private static final Logger log = LoggerFactory.getLogger(TitleLogWritter.class);

    public static final String TAG = "TitleLogWritter";

    static Queue<TitleOptimiseMsg> msgs = new ConcurrentLinkedQueue<TitleOptimiseMsg>();

    public void doJob() {
    	try {
			TitleOptimiseMsg msg = null;
	        while ((msg = msgs.poll()) != null) {
	            TitleOptimiseLog opLogModel = new TitleOptimiseLog(msg).save();
	            // 同步更新宝贝标题是否优化过
	            TitleOptimisedWritter.addMsg(msg.getUserId(), msg.getNumIid(), true);
	            log.info("[saved op log:]" + opLogModel);
	        }
		} catch (Exception e) {
			// TODO: handle exception
		}
        
    }

    public static void addMsg(Long userId, Long numIid, String originTitle, String newTitle) {
        addMsg(new TitleOptimiseMsg(numIid, userId, originTitle, newTitle));
    }

    public static void addMsg(TitleOptimiseMsg msg) {
        msgs.add(msg);
    }
}
