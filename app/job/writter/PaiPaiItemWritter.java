
package job.writter;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import message.itemupdate.ItemDBDone;
import models.updatetimestamp.updates.ItemUpdateTs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import ppapi.models.PaiPaiItem;

import com.ciaosir.client.CommonUtils;

@Every("5s")
public class PaiPaiItemWritter extends Job {

    private static final Logger log = LoggerFactory.getLogger(PaiPaiItemWritter.class);

    public static final String TAG = "PaiPaiItemWritter";

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
    
    public static void addTradeList(Long userId, Long ts, List<PaiPaiItem> tradeList) {

        ItemList tradeListUpdate = new ItemList(userId, ts, tradeList);

        while (itemListToWritten.size() > 512) {
            CommonUtils.sleepQuietly(1000L);
        }

        itemListToWritten.add(tradeListUpdate);

    }

    public static void addFinishedMarkMsg(Long userId, Long ts) {

        itemListToWritten.add(new ItemList(userId, ts, true));
    }

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

            for (PaiPaiItem item : itemList.itemList) {
//                List<PaiPaiItem> list = new PaiPaiItem.ListFetcher(item.getSellerUin(), "itemCode = ?",
//                        item.getItemCode()).call();
//                if (!CommonUtils.isEmpty(list)) {
//                    PaiPaiItem paipaiItem = list.get(0);
//                    item.setType(paipaiItem.getType());
//                    item.rawUpdate();
//                } else {
//                    item.rawInsert();
//                }
                item.jdbcSave();
            }
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

        List<PaiPaiItem> itemList;

        public ItemList(Long userId, Long ts, List<PaiPaiItem> itemList) {

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

        @Override
        public String toString() {
            return "ItemList [userId=" + userId + ", ts=" + ts + ", isFinished=" + isFinished + ", itemList="
                    + itemList + "]";
        }

    }
}
