package job.writter;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import models.CatClickRatePic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;

@Every("15s")
public class CatClickRatePicWritter extends Job {
	private static final Logger log = LoggerFactory.getLogger(CatClickRatePicWritter.class);

    static Queue<CatClickRatePic> queue = new ConcurrentLinkedQueue<CatClickRatePic>();

    @Override
    public void doJob() {
    	CatClickRatePic pLog = null;
        while ((pLog = queue.poll()) != null) {
            pLog.jdbcSave();
        }
    }

    public static void addMsg(CatClickRatePic catClickRatePic) {
        queue.add(catClickRatePic);
    }
}
