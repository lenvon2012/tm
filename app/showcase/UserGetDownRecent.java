
package showcase;

import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

import job.showwindow.LightWeightQueueJob;
import job.showwindow.WindowShelfDropForDelistTimerJob;
import models.item.ItemPlay;
import models.user.User;
import bustbapi.OperateItemApi.ItemsOnWindowInit;

import com.taobao.api.domain.Item;

public class UserGetDownRecent {

    User user;

    PriorityQueue<ItemPlay> recentDownNumber = new PriorityQueue<ItemPlay>(12);

    static int MAX_RECENT_DELIST_ITEM = 12;

    static int MIN_RECENT_DELIST_ITEM = 3;

    static Map<Long, PriorityQueue<ItemPlay>> toShowItemMap = new ConcurrentHashMap<Long, PriorityQueue<ItemPlay>>();

    public void doForUser(User user) {
        PriorityQueue<ItemPlay> queue = ensureUserQueue();

        while (true) {
            ItemPlay peek = queue.peek();
            if (peek == null) {
            }
            long now = System.currentTimeMillis();
            if (now - peek.getDeListTime() <= 0) {
                break;
            }

            peek = queue.poll();

            WindowShelfDropForDelistTimerJob.submitUserCheckShelfJob(user);
            // TODO perhaps we need a little delay...
            LightWeightQueueJob.submitUser(user);

        }

    }

    private PriorityQueue<ItemPlay> ensureUserQueue() {
        PriorityQueue<ItemPlay> queue = toShowItemMap.get(user.getId());
        if (queue != null) {
            if (queue.size() < MIN_RECENT_DELIST_ITEM) {
                reinitQueue(queue);
            }
            return queue;
        }
        queue = new PriorityQueue<ItemPlay>(MAX_RECENT_DELIST_ITEM);
        reinitQueue(queue);
        return null;
    }

    private void reinitQueue(PriorityQueue<ItemPlay> queue) {
        List<Item> onWindowItems = new ItemsOnWindowInit(user, MAX_RECENT_DELIST_ITEM, true).call();
        queue.clear();
        for (Item wItem : onWindowItems) {
            new ItemPlay(user.getId(), wItem);
            ItemPlay item = new ItemPlay();
            queue.add(item);
        }
    }
}
