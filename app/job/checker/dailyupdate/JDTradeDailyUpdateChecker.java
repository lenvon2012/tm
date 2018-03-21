package job.checker.dailyupdate;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import message.tradeupdate.jd.JDTradeDailyUpdateMsg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import transaction.TransactionSecurity;


@Every("20s")
public class JDTradeDailyUpdateChecker extends Job {

    private static final Logger log = LoggerFactory.getLogger(JDTradeDailyUpdateChecker.class);

    public static final String TAG = "JDTradeDailyUpdateChecker";

    public static Queue<JDTradeDailyUpdateMsg> queue = new ConcurrentLinkedQueue<JDTradeDailyUpdateMsg>();

    public void writeAllMessgae() {
    	
    	new TransactionSecurity<Void>() {
            @Override
            public Void operateOnDB() {
            	JDTradeDailyUpdateMsg msg = null;
            	while ((msg = queue.poll()) != null) {
                    msg.applyFor(msg.findEntity());
                }
				return null;
            }
        }.execute();
        
//        while ((msg = queue.poll()) != null) {
//            msg.applyFor(msg.findEntity());
//        }
    }

    public static void addMessage(JDTradeDailyUpdateMsg msg) {
//        log.error("Add Trade Daily Update Msg:" + msg);

        queue.add(msg);
    }

    @Override
    public void before() {
        Thread.currentThread().setName(TAG);
    }

    public Queue<JDTradeDailyUpdateMsg> getQueue() {
        return queue;
    }
    
    @Override
    public void doJob() {
    	Thread.currentThread().setName(TAG);
        writeAllMessgae();
    }
}