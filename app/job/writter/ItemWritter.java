
package job.writter;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import message.itemupdate.ItemDBDone;
import models.item.ItemPlay;
import models.updatetimestamp.updates.ItemUpdateTs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.Item;

//@Every("5s")
public class ItemWritter extends Job {

    private static final Logger log = LoggerFactory.getLogger(ItemWritter.class);

    public static final String TAG = "ItemWritter";

    public static final Queue<ItemList> itemListToWritten = new ConcurrentLinkedQueue<ItemList>();

    public static String statusMessage;

//    public static void addItemList(Long userId, List<Item> itemsGet) {
//        addItemList(userId, System.currentTimeMillis(), itemsGet);
//    }
//
//    public static void addItemList(Long userId, Long ts, List<Item> itemList) {
//        itemListToWritten.add(new ItemList(userId, ts, itemList));
//    }
//
//    public static void addFinishedMarkMsg(Long userId, Long ts) {
//
//        itemListToWritten.add(new ItemList(userId, ts, true));
//    }

    @Override
    public void doJob() {

        Thread.currentThread().setName(TAG);

        ItemList itemList = null;

        while ((itemList = itemListToWritten.poll()) != null) {
            doInsert(itemList);
        }
    }

    public static void doInsert(ItemList itemList) {

        statusMessage = "Writting for userId:" + itemList.userId + " ts:" + itemList.ts + " with size:"
                + (itemList.itemList == null ? null : itemList.itemList.size());

        log.info(statusMessage);

        if (itemList.isFinished) {

            statusMessage = "Finishing userId:" + itemList.userId + " ts:" + itemList.ts;

            log.warn(statusMessage);

            afterFinished(itemList);
            return;
        }

        if (!CommonUtils.isEmpty(itemList.itemList)) {

            for (Item item : itemList.itemList) {
                new ItemPlay(itemList.userId, itemList.ts, item).jdbcSave();

            }
            itemList.itemList.clear();
        }

    }

    private static void afterFinished(ItemList itemList) {

        new ItemDBDone(itemList.userId, itemList.ts).publish();

        ItemUpdateTs.updateLastItemModifedTime(itemList.userId, itemList.ts);

    }

    public static class ItemList {

        Long userId;

        Long ts;

        boolean isFinished;

        List<Item> itemList;

        public ItemList(Long userId, Long ts, List<Item> itemList) {

            super();
            this.userId = userId;
            this.ts = ts;
            this.isFinished = false;
            this.itemList = itemList;
        }

        public ItemList(Long userId, Long ts, boolean isFinished) {

            super();
            this.userId = userId;
            this.ts = ts;
            this.isFinished = true;
            this.itemList = null;
        }

    }
}
