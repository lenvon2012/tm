
package job.click;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;

import job.ApplicationStopJob;
import models.paipai.PaiPaiUser;
import models.user.User;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.common.util.concurrent.jsr166y.ConcurrentLinkedDeque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import titleDiag.DiagResult;
import utils.DateUtil;
import bustbapi.ClickApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;

import configs.Subscribe.Version;
import configs.TMConfigs;
import controllers.APIConfig;
import controllers.APIConfig.Platform;
import dao.UserDao;
import dao.popularized.PopularizedDao;
import fakeClick.FakeClick;
import fakeClick.PaiPaiFakeClick;

/*@Every("60s")
@OnApplicationStart(async = true)*/
public class HourlyCheckerJob extends Job {

    public static long intervalMillis = 1600000L;

    private static final Logger log = LoggerFactory.getLogger(HourlyCheckerJob.class);

    private static PYFutureTaskPool<Boolean> pool = null;

    public static Map<Long, Integer> userIdNummIdCount = new HashMap<Long, Integer>();

    public static PYFutureTaskPool<Boolean> getPool() {
        if (pool != null) {
            return pool;
        }

        pool = new PYFutureTaskPool<Boolean>(512);
        ApplicationStopJob.addShutdownPool(pool);
        return pool;
    }

    public static boolean HOUR_JOB_ENABLE = false;

    public static int clickRound = 0;

    public void doJob() {
        log.error("do hourly :" + HOUR_JOB_ENABLE);
        if (!HOUR_JOB_ENABLE) {
            return;
        }
        
        if (APIConfig.get().getPlatform() == Platform.jingdong) {
            return;
        }

        if (!isTimeAllowed()) {
            return;
        }

        if (APIConfig.get().getPlatform() == Platform.paipai) {
            clickRound++;
            if (clickRound > 1) {
//                if (clickRound > 10) {
                    clickRound = 0;
//                }
                return;
            }
        }

//        TMConfigs.getDiagResultPool().submit(new Callable<DiagResult>() {
//            @Override
//            public DiagResult call() throws Exception {
//                doCommonlyJob();
//                return null;
//            }
//        });
        doCommonlyJob();

        if (APIConfig.get().getPlatform() == Platform.paipai || APIConfig.get().getPlatform() == Platform.jingdong) {
            CommonUtils.sleepQuietly(30000L);
            return;
        }

        TMConfigs.getDiagResultPool().submit(new Callable<DiagResult>() {
            @Override
            public DiagResult call() throws Exception {

                int count = 50;
                while (count-- > 0) {
                    doForUserNeedMoreClick();
                }
                return null;
            }
        });

//        CommonUtils.sleepQuietly(500L);
    }

    private boolean isTimeAllowed() {

        int hour = DateUtil.getCurrHour();

        if (hour >= 8) {
            return true;
        }
        /**
         * 三点到六点，不点击
         */
        if (hour > 2 && hour < 6) {
            return false;
        }

        /*if (Math.random() >= 0.33d) {
            return true;
        }*/

        return true;
    }

    private void doForUserNeedMoreClick() {

        Queue<User> moreclickiusers = (Queue<User>) UserDao.findidbynick();
        log.info("[fetch moreclickids size :]" + CollectionUtils.size(moreclickiusers));

        if (CommonUtils.isEmpty(moreclickiusers)) {
            return;
        }

        log.info("[moreclickiusers queue size = ]" + moreclickiusers.size()
                + ",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");
        //for (User user : moreclickiusers) {
        //    if (!user.isVaild() || user.isPopularOff()) {
        //        moreclickiusers.remove(user);
        //    }
        //}
        Iterator<User> it = moreclickiusers.iterator();
        while (it.hasNext()) {
            User user = it.next();
            if (user == null || !user.isVaild() || user.isPopularOff()) {
                it.remove();
                continue;
            }
        }

        // log.error(" init pool");
        log.info("job begin:::::::::::::::::::::::::::::::::::::::::::::::::::::::::: + for more click users::+"
                + moreclickiusers.size());
        getPool().submit(new NeedToMoreClickUser(moreclickiusers));
        CommonUtils.sleepQuietly(2500L);

    }

    static int limit = 32;

    protected static long getSleepInterval(long interval) {
        log.info("[check user id  with numiid count:]");

        long total = PopularizedDao.countAll();
        userIdNummIdCount = PopularizedDao.countUser();

        long sleepInterval = 100L;

//        int total = userIdNummIdCount.size() * 5;
        if (total <= 1L) {
            total = 2L;
        }

        sleepInterval = interval * limit / total;
        if (sleepInterval <= 5L) {
            sleepInterval = 5L;
        } else {
//            sleepInterval = sleepInterval * 9 / 10;
        }

        log.error(">>>>>>>>>>>sleep interval :" + sleepInterval + " for userid num id count :" + total);

        return sleepInterval;
    }

    protected static void doItemClick(Queue<ItemNum> items) {
        log.info("[ItemNum queue size = ]" + items.size() + ",,,,,,,,,,,");
        Iterator<ItemNum> it = items.iterator();

        if (APIConfig.get().getPlatform() == Platform.taobao) {
            while (it.hasNext()) {
                ItemNum item = it.next();
                Long userId = item.getUserId();
                User user = UserDao.findById(userId);
                if (user == null) {
                    it.remove();
                    continue;
                }

                if (!user.isVaild() || user.isPopularOff()) {
                    it.remove();
                    continue;
                }
                if (isUserVersionReduction(user)) {
                    it.remove();
                    continue;
                }
            }
        } else if (APIConfig.get().getPlatform() == Platform.paipai) {
            while (it.hasNext()) {
                ItemNum item = it.next();
                Long userId = item.getUserId();
                PaiPaiUser user = PaiPaiUser.findByUserId(userId);
                if (user == null) {
                    it.remove();
                    continue;
                }
                if (!user.isValid() || user.isPopularOff()) {
                    it.remove();
                    continue;
                }

                if (isUserVersionReduction(user)) {
                    it.remove();
                    continue;
                }
            }
        }

//    log.error(" init pool");

        log.info("job begin:::::::::::::::::::::::::::::::::::::::::::::::::::::::::: with size:::" + items.size());
        getPool().submit(new NeedToClickItemChecker(items));

    }

    /**
     * 如果用户买的是体验版，那么，我们就给之给他十分之一的点击。。。
     * @param user
     * @return
     */
    private static boolean isUserVersionReduction(User user) {
        if (user.getVersion() > Version.BLACK) {
            return false;
        }
        return Math.random() > 0.15d;
    }

    private static boolean isUserVersionReduction(PaiPaiUser user) {
        if (user.getVersion() > Version.BLACK) {
            return false;
        }
        return Math.random() > 0.15d;
    }

    public void doCommonlyJob() {
        try {

            //long sleepInterval = getSleepInterval(intervalMillis);
        	long sleepInterval = 16000L;
            int offset = 0;
//            int limit = 128;
            while (true) {
                Queue<ItemNum> items = toClickItems.getItemNums(offset, limit, 0);
                log.info("[fetch item size :]" + CollectionUtils.size(items) + " with sleep time :" + sleepInterval
                        + " with offset :" + offset);

                if (CommonUtils.isEmpty(items)) {
                    break;
                }

                offset += limit;

                doItemClick(items);
                CommonUtils.sleepQuietly(sleepInterval);
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    public static class NeedToClickItemChecker implements Callable<Boolean> {

        public Queue<ItemNum> queue = new ConcurrentLinkedDeque<ItemNum>();

        public NeedToClickItemChecker(Queue<ItemNum> queue) {
            this.queue = queue;
        }

        @Override
        public Boolean call() throws Exception {
            // TODO find all need to do items... for currently
            //Queue<ItemPlay> queue = new ConcurrentLinkedQueue<ItemPlay>();
            long size = queue.size();
//
//            long totalMillis = intervalMillis;
//
//            // TODO here need to change 1800 to size... 
//            long interval = totalMillis / size;
//            if (interval > 1L) {
//                interval = interval * 3 / 4L;
//            } else {
//                interval = 5L;
//            }
//            long interval = 5L;

            ItemNum item = null;
            while ((item = queue.poll()) != null) {
//                log.info("[submit item num: ]" + item);

                if (APIConfig.get().getPlatform() == Platform.taobao) {
                    getPool().submit(new ClickItemCaller(item));
                } else if (APIConfig.get().getPlatform() == Platform.paipai) {
                    getPool().submit(new ClickPaiPaiItemCaller(item));
                }

                CommonUtils.sleepQuietly(500L);

//                CommonUtils.sleepQuietly(1L);
            }

            return null;
        }
    }

    public static class ClickItemCaller implements Callable<Boolean> {
        ItemNum item;

        public ClickItemCaller(ItemNum item) {
            super();
            this.item = item;
        }

        @Override
        public Boolean call() throws Exception {
            // TODO come on...
            FakeClick fakeClick = new FakeClick(item);
            fakeClick.setRefer();
            fakeClick.setJumpUrls(item.getUserId());
            fakeClick.setWaitTime();
            ClickApi.doClick(fakeClick.url1, fakeClick.url2, fakeClick.url3, fakeClick.waitTime, fakeClick.referer);
            return true;
        }
    }

    public static class manualClickItem implements Callable<Boolean> {
    	
        String itemUrl;
        
        String referer;
         
        int waitTime;

        public manualClickItem(String itemUrl, String referer, int waitTime) {
            super();
            this.itemUrl = itemUrl;
            this.referer = referer;
            this.waitTime = waitTime;
        }

        @Override
        public Boolean call() throws Exception {
            // TODO come on...
            ClickApi.doClick(itemUrl, StringUtils.EMPTY, StringUtils.EMPTY, waitTime, referer);
            return true;
        }

    }
    
    public static class ClickPaiPaiItemCaller implements Callable<Boolean> {
        ItemNum item;

        public ClickPaiPaiItemCaller(ItemNum item) {
            super();
            this.item = item;
        }

        @Override
        public Boolean call() throws Exception {
            // TODO come on...
            PaiPaiFakeClick fakeClick = new PaiPaiFakeClick(item);
            fakeClick.setRefer();
            fakeClick.setJumpUrls(item.getUserId());
            fakeClick.setWaitTime();
            ClickApi.doClick(fakeClick.url1, fakeClick.url2, fakeClick.url3, fakeClick.waitTime, fakeClick.referer);
            return true;
        }

    }

    public static class MoreClickUserCaller implements Callable<Boolean> {
        User user;

        public MoreClickUserCaller(User user) {
            super();
            this.user = user;
        }

        @Override
        public Boolean call() throws Exception {
            Queue<ItemNum> items = PopularizedDao.getItemNumsByUserId(user.getId());
            if (CommonUtils.isEmpty(items)) {
                log.warn("this user " + user.getUserNick() + " has no items in popularized");
                return false;
            }
            for (ItemNum item : items) {
//                log.info("click for item " + item.numIid);
                FakeClick fakeClick = new FakeClick(item);
                fakeClick.setRefer();
                fakeClick.setJumpUrls(item.getUserId());
                fakeClick.setWaitTime();
                ClickApi.doClick(fakeClick.url1, fakeClick.url2, fakeClick.url3, fakeClick.waitTime, fakeClick.referer);
                CommonUtils.sleepQuietly(5000L);
            }
            return true;
        }

    }

    public class NeedToMoreClickUser implements Callable<Boolean> {

        public Queue<User> queue = new ConcurrentLinkedDeque<User>();

        public NeedToMoreClickUser(Queue<User> queue) {
            this.queue = queue;
        }

        @Override
        public Boolean call() throws Exception {
            User user = null;
            while ((user = queue.poll()) != null) {
                if (!user.isVaild() || user.isPopularOff()) {
                    continue;
                }
                getPool().submit(new MoreClickUserCaller(user));
                CommonUtils.sleepQuietly(50L);
            }

            return null;
        }
    }
}
