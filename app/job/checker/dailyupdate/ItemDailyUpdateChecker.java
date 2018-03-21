package job.checker.dailyupdate;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import message.itemupdate.ItemDailyUpdateMsg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import transaction.TransactionSecurity;

@Every("20s")
public class ItemDailyUpdateChecker extends Job {

    private static final Logger log = LoggerFactory.getLogger(ItemDailyUpdateChecker.class);

    public static final String TAG = "ItemDailyUpdateChecker";

    private static Queue<ItemDailyUpdateMsg> queue = new ConcurrentLinkedQueue<ItemDailyUpdateMsg>();

    public void writeAllMessgae() {

        new TransactionSecurity<Void>() {
            @Override
            public Void operateOnDB() {
                ItemDailyUpdateMsg msg = null;
                while ((msg = queue.poll()) != null) {
                    msg.applyFor(msg.findEntity());
                }
                return null;
            }
        }.execute();

        // while ((msg = queue.poll()) != null) {
        // msg.applyFor(msg.findEntity());
        // }
    }

    public static void addMessage(ItemDailyUpdateMsg msg) {
         log.error("Add Item Daily Update Msg:" + msg);

//        queue.add(msg);
    }

    @Override
    public void before() {
        Thread.currentThread().setName(TAG);
    }

    public Queue<ItemDailyUpdateMsg> getQueue() {
        return queue;
    }

    @Override
    public void doJob() {
        Thread.currentThread().setName(TAG);
        writeAllMessgae();
    }
}