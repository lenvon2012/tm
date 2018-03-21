package job.click;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.Callable;

import jdp.ApiJdpAdapter;
import job.ApplicationStopJob;
import models.user.User;

import org.apache.commons.collections.CollectionUtils;
import org.elasticsearch.common.util.concurrent.jsr166y.ConcurrentLinkedDeque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.taobao.api.domain.Item;

import configs.Subscribe.Version;
import controllers.APIConfig;
import controllers.APIConfig.Platform;
import dao.UserDao;
import dao.popularized.PopularizedDao;

@Every("24h")
//@OnApplicationStart(async = true)
public class ItemStatusDailyCheck extends Job {

    public static long intervalMillis = 320000L;

    private static final Logger log = LoggerFactory.getLogger(ItemStatusDailyCheck.class);

    static PYFutureTaskPool<Boolean> pool = null;

    static void initPool() {
        if (pool != null) {
            return;
        }

        pool = new PYFutureTaskPool<Boolean>(256);
        ApplicationStopJob.addShutdownPool(pool);
    }
    public void doJob() {
        dodailyJob();
    }

    static int limit = 32;

    protected static void dailyItemClick(Queue<ItemNum> items) {
        log.info("[ItemNum queue size = ]" + items.size() + ",,,,,,,,,,,");
        Iterator<ItemNum> it = items.iterator();
        Queue<ItemNum> needtodelete = new ConcurrentLinkedDeque<ItemNum>();
        while (it.hasNext()) {
            ItemNum item = it.next();
            Long userId = item.getUserId();
            User user = UserDao.findById(userId);
            if (user == null) {
                needtodelete.add(item);
                continue;
            }

            if (!user.isVaild()) {
                needtodelete.add(item);
                continue;
            }
            //if (isUserVersionReduction(user)) {
            //    needtodelete.add(item);
            //    continue;
            //}
            Item call = ApiJdpAdapter.get(user).findItem(user, item.getNumIid());
            if(call == null){
                needtodelete.add(item);
                continue;
            }
            if(call.getApproveStatus().equals("instock")){
                needtodelete.add(item);
                continue;
            }
        }

//    log.error(" init pool");
        initPool();
        log.info("job begin::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        if(needtodelete.size() > 0) {
            pool.submit(new NeedToDeleteItemChecker(needtodelete));
        }

    }

    /**
     * 如果用户买的是体验版，那么，我们就给之给他三分之一的点击。。。
     * @param user
     * @return
     */
    private static boolean isUserVersionReduction(User user) {
        if (user.getVersion() > Version.BLACK) {
            return false;
        }
        return Math.random() > 0.3d;
    }

    public void dodailyJob() {
        try {
            if (APIConfig.get().getPlatform() == Platform.paipai || APIConfig.get().getPlatform() == Platform.jingdong) {
                return;
            }
            
            long sleepInterval = 100l;

            int offset = 0;
//            int limit = 128;
            while (true) {
                Queue<ItemNum> items = toClickItems.getItemNums(offset, limit, 0);
                log.info("[fetch item size :]" + CollectionUtils.size(items));

                if (CommonUtils.isEmpty(items)) {
                    break;
                }

                offset += limit;

                dailyItemClick(items);
                CommonUtils.sleepQuietly(sleepInterval);
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    public static class NeedToDeleteItemChecker implements Callable<Boolean> {

        public Queue<ItemNum> queue = new ConcurrentLinkedDeque<ItemNum>();

        public NeedToDeleteItemChecker(Queue<ItemNum> queue) {
            this.queue = queue;
        }

        @Override
        public Boolean call() throws Exception {
            long size = queue.size();

            ItemNum item = null;
            while ((item = queue.poll()) != null) {
                pool.submit(new DeleteItemCaller(item));
                CommonUtils.sleepQuietly(500L);
//                CommonUtils.sleepQuietly(1L);
            }

            return null;
        }
    }

    public static class DeleteItemCaller implements Callable<Boolean> {
        ItemNum item;

        public DeleteItemCaller(ItemNum item) {
            super();
            this.item = item;
        }

        @Override
        public Boolean call() throws Exception {
            // TODO come on...
            PopularizedDao.deletePopularizeById(item.userId, item.numIid);
            return true;
        }

    }
}
