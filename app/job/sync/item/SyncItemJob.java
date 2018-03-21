package job.sync.item;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import bustbapi.ItemApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.taobao.api.domain.Item;

import configs.TMConfigs.PageSize;

public class SyncItemJob extends Job {

    public final static Logger log = LoggerFactory.getLogger(SyncItemJob.class);

    User user;
    String userNick;
    Long startModified;
    Long endModified;

    static PYFutureTaskPool<List<Item>> pool = new PYFutureTaskPool<List<Item>>(4);

    public SyncItemJob(User user, String userNick) {
        this(user, userNick, null, null);
    }

    public SyncItemJob(User user, String userNick, Long startModified, Long endModified) {
        this.user = user;
        this.userNick = userNick;
        this.startModified = startModified;
        this.endModified = endModified;
    }

    @Override
    public void doJob() throws Exception {

        Long itemTotalNum = new ItemApi.ItemsOnsaleCount(user.getSessionKey(), startModified, endModified).call();

        if (itemTotalNum == null) {
            return;
        }

        log.info(String.format("Item Get userId, totalNum".replaceAll(", ", "=%s, ") + "=%s ", user.getId(),
                itemTotalNum));

        long totalPageCount = CommonUtils.calculatePageCount(itemTotalNum, PageSize.API_ITEM_PAGE_SIZE);

        List<FutureTask<List<Item>>> promises = new ArrayList<FutureTask<List<Item>>>();

        List<Item> resList = new ArrayList<Item>();
        for (Long pageNo = 1L; pageNo < totalPageCount + 1; pageNo++) {
            FutureTask<List<Item>> promise = pool.submit(new ItemApi.ItemsOnsalePage(user, startModified, endModified,
                    pageNo));
            promises.add(promise);

        }

        for (FutureTask<List<Item>> promise : promises) {

            List<Item> itemGet = promise.get();
            if (!CommonUtils.isEmpty(itemGet)) {
                resList.addAll(itemGet);
            }
        }
    }
}
