package job.clouddate;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import actions.clouddata.UserHourViewTrade;

@Every("15s")
public class UserHourViewTradeWritter extends Job {
	private static final Logger log = LoggerFactory.getLogger(UserHourViewTradeWritter.class);

    static Queue<UserHourViewTrade> queue = new ConcurrentLinkedQueue<UserHourViewTrade>();

    @Override
    public void doJob() {
    	UserHourViewTrade pLog = null;
        while ((pLog = queue.poll()) != null) {
            pLog.rawInsert();
        }
    }

    public static void addMsg(UserHourViewTrade viewTrade) {
        queue.add(viewTrade);
    }
}
