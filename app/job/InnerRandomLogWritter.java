package job;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import models.oplog.InnerRandomLog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
@Every("15s")
public class InnerRandomLogWritter extends Job {
	private static final Logger log = LoggerFactory.getLogger(InnerRandomLogWritter.class);

    static Queue<InnerRandomLog> queue = new ConcurrentLinkedQueue<InnerRandomLog>();

    @Override
    public void doJob() {
    	InnerRandomLog pLog = null;
        while ((pLog = queue.poll()) != null) {
            pLog.rawInsert();
        }
    }

    public static void addMsg(InnerRandomLog viewTrade) {
        queue.add(viewTrade);
    }
}
