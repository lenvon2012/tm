
package dao.item.sub;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jdp.ApiJdpAdapter;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.NoTransaction;
import play.jobs.Job;
import bustbapi.ItemApi.ItemsInventory;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.Item;

import dao.item.ItemDao;

@NoTransaction
public class ItemDeletedCheckJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(ItemDeletedCheckJob.class);

    Set<Long> exists = null;

    List<Item> onSaleItems = null;

    User user = null;

    public ItemDeletedCheckJob(User user, Set<Long> exists, List<Item> rawItems) {
        super();
        this.exists = exists;
        this.onSaleItems = rawItems;
        this.user = user;
    }

    private void checkInventory() {
        List<Item> inventries = new ItemsInventory(user, null, null).call();
        if (CommonUtils.isEmpty(inventries)) {
            log.info("no inventory :" + user);
            return;
        }

        List<Long> ids = new ArrayList<Long>();
        for (Item item : inventries) {
            ids.add(item.getNumIid());
        }

        ItemDao.updateInventory(user.getId(), ids);
    }

    private void checkDelete() {
        List<Long> res = new ArrayList<Long>();

        Set<Long> currentOnSale = new HashSet<Long>();
        if (CommonUtils.isEmpty(onSaleItems)) {
            return;
        }
        for (Item item : onSaleItems) {
            currentOnSale.add(item.getNumIid());
        }

        Iterator<Long> iterator = exists.iterator();
        while (iterator.hasNext()) {
            Long next = iterator.next();
            if (currentOnSale.contains(next)) {
                continue;
            }
            res.add(next);
        }

        for (Long id : res) {
            tryFixDeleteItem(user, id);
        }

    }

    public static void tryFixItem(User user, List<Long> ids) {
        for (Long long1 : ids) {
            tryFixDeleteItem(user, long1);
        }
    }

    public static void tryFixDeleteItem(User user, Long id) {
        // TODO, try find delete....
        ApiJdpAdapter.fixDeletedItem(user, id);
    }

    @Override
    public void doJob() {
        checkDelete();
    }
}
