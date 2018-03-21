package job.checker.dailyupdate;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import message.tradeupdate.TradeDailyUpdateMsg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import transaction.TransactionSecurity;


@Every("20s")
public class TradeDailyUpdateChecker extends Job {

    private static final Logger log = LoggerFactory.getLogger(TradeDailyUpdateChecker.class);

    public static final String TAG = "TradeDailyUpdateChecker";

    public static Queue<TradeDailyUpdateMsg> queue = new ConcurrentLinkedQueue<TradeDailyUpdateMsg>();

    public void writeAllMessgae() {
    	
    	new TransactionSecurity<Void>() {
            @Override
            public Void operateOnDB() {
            	TradeDailyUpdateMsg msg = null;
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

    public static void addMessage(TradeDailyUpdateMsg msg) {
//        log.error("Add Trade Daily Update Msg:" + msg);

        queue.add(msg);
    }

    @Override
    public void before() {
        Thread.currentThread().setName(TAG);
    }

    public Queue<TradeDailyUpdateMsg> getQueue() {
        return queue;
    }
    
    @Override
    public void doJob() {
    	Thread.currentThread().setName(TAG);
        writeAllMessgae();
    }
}