
package onlinefix;

import java.util.List;
import java.util.concurrent.Callable;

import job.apiget.ItemUpdateJob;
import job.apiget.TradeRateUpdateJob;
import job.apiget.TradeUpdateJob;
import models.updatetimestamp.updates.ItemUpdateTs;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.jobs.Job;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.utils.NumberUtil;

import configs.Subscribe.Version;
import configs.TMConfigs.Rds;
import controllers.APIConfig;
import dao.UserDao;

public class UpdateAllItemsJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(UpdateAllItemsJob.class);

    public static final String TAG = "UpdateAllItemsJob";

    static int totalCount = 0;

    static int currentNum = 0;

    static int finishNum = 0;

    boolean clearOld = true;

    public UpdateAllItemsJob(boolean clearOld) {
        super();
        this.clearOld = clearOld;
    }

    static PYFutureTaskPool<Boolean> pool = null;

    public static synchronized PYFutureTaskPool<Boolean> getPool() {
        if (pool != null) {
            return pool;
        }
        pool = new PYFutureTaskPool<Boolean>(NumberUtil.parserInt(Play.configuration.get("thread.itemupdate.num"), 32));
        return pool;
    }

    int offset = 0;

    public UpdateAllItemsJob() {
        super();
    }

    public UpdateAllItemsJob(int offset) {
        super();
        this.offset = offset;
    }

    @Override
    public void doJob() {
        totalCount = 0;
        currentNum = 0;

        new UserDao.UserBatchOper(0, 32) {

            public List<User> findNext() {
                return UserDao.findValidListOrderBydFirstLoginTime(offset, limit);
            }

            @Override
            public void doForEachUser(final User user) {
                log.info("[do for user;]" + user);
//                if (user.isAutoCommentOn() || user.isAutoCommentOn() || user.isShowwindowOn()) {

                updateUser(user);
                if (this.offset > 100) {
                    CommonUtils.sleepQuietly(200L);
                }
            }
        }.call();
    }

    public static String getStatus() {
        return String.format("[total--%d--curr%d--finish %d]executing!", totalCount, currentNum, finishNum);
    }

    public static void submitUserId(final Long userId) {
        User user = UserDao.findById(userId);
        if (user == null || !user.isVaild()) {
            return;
        }
//        updateUser(user);
        new ItemUpdateJob(userId).now();
    }

    static int threadCount = 0;

    public void updateUser(final User user) {
        if (user == null) {
            return;
        }
        if (APIConfig.get().getApp() == APIConfig.taobiaoti.getApp() && user.getVersion() < Version.BASE) {
            return;
        }
        if(Version.LL == user.getVersion()) {
        	return;
        }

        totalCount++;
        getPool().submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Thread.currentThread().setName("ItemUpdateNum[" + threadCount + "]");
                currentNum++;

                doForUser(user);

                log.warn(" update over for usesr:   " + user);
                finishNum++;
                return null;
            }

        });
    }

    public static void doForUser(final User user) {
        if (user == null) {
            return;
        }
        try {
            final Long userId = user.getId();
            ItemUpdateTs ts = ItemUpdateTs.fetchByUser(user.getId());
            log.info("[find ts : ]" + ts);
            if (!Rds.enableJdpPush) {
                if (ts != null) {
                    ts.jdbcDelete(user.getId());
                }
            }

            new ItemUpdateJob(userId, false).doJob();

            if (APIConfig.get().enableSyncTrade(user.getId())) {
                // TODO try sync trades and comments...
                new TradeUpdateJob(user.getId(), System.currentTimeMillis()).doJob();
            }
            if (APIConfig.get().enableSyncTradeRate()) {
                // TODO: sync trade rate..
                new TradeRateUpdateJob(user.getId(), System.currentTimeMillis()).doJob();
            }
        } catch (Exception e) {
            // TODO: handle exception
            log.error("error TRACE ItemUpdateJob . userId = " + user.getId());
            log.error(e.getMessage(), e);
        }
    }

}
