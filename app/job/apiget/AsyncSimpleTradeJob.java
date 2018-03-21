
package job.apiget;

import static java.lang.String.format;

import java.util.Date;

import models.updatetimestamp.updates.TradeUpdateTs;
import models.visit.ATSLocalTask;
import models.visit.ATSLocalTask.TaskType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ats.ATSTaskUpdate;
import bustbapi.TMTradeApi;
import bustbapi.TMTradeApi.AsyncTradeApi;

import com.ciaosir.client.utils.DateUtil;
import com.taobao.api.domain.Task;

import controllers.APIConfig;

public class AsyncSimpleTradeJob extends TBUpdateJob {

    private static final Logger log = LoggerFactory.getLogger(AsyncSimpleTradeJob.class);

    private static final String TAG = "TradeUpdateJob";

    private static TaskType taskType = TaskType.ATSSimpleTrade;

    boolean recent3Day = true;

    public AsyncSimpleTradeJob(Long userId, boolean recent3Day) {
        super(userId);
        this.now = DateUtil.formCurrDate() - DateUtil.DAY_MILLIS;
        this.recent3Day = recent3Day;
    }

//
//    public AsyncSimpleTradeJob(Long userId, Long ts) {
//        super(userId);
//        // this.now = DateUtil.formDailyTimestamp(ts);
//        this.now = ts;
//        log.warn("[new trade update job]" + userId);
//    }

    @Override
    protected boolean prepare() {

        Thread.currentThread().setName(TAG);

        if (now < 0L) {
            // now = DateUtil.formCurrDate();
            now = DateUtil.formCurrDate() - DateUtil.DAY_MILLIS;
        }

        log.info("[user async]" + APIConfig.get().useAsyncTrade());
        if (!APIConfig.get().useAsyncTrade()) {
            return false;
        }

        TradeUpdateTs tradeTs = TradeUpdateTs.findByUserId(userId);
        log.info("[Found Current Version]" + tradeTs);

        if (System.currentTimeMillis() - maxUpdateTs < DateUtil.DAY_MILLIS) {
            // 一天内更新过，那今天就不更新了
            return false;
        }

        // maxUpdateTs = getMaxUserUpdateVersion();
        // return tradeTs == null ? 0L : tradeTs.getLastUpdateTime();
        if (tradeTs != null) {
            maxUpdateTs = tradeTs.getLastUpdateTime();
        }
        if (maxUpdateTs == 0L) {
            maxUpdateTs = now - DateUtil.DAY_MILLIS * 30;
        }

        start = maxUpdateTs;
        end = now;

        log.info("[Set new Start and End]" + DateUtil.formDateForLog(start) + " -- " + DateUtil.formDateForLog(end));
        return true;
    }

    public void requestUpdate(long start, long end) {

        log.warn(format("Trade Update Job:start, end".replaceAll(", ", "=%s, ") + "=%s", start, end));
//
//        if (isFirstUpdate) {
//            UserDao.refreshTokenNow(user);
//        }
        log.warn("[do for async start trades...]");
        ATSLocalTask localTask = ATSLocalTask.findTradeRecentSold(user.getId());
//        if (localTask != null && (localTask.isDownToLocal() || localTask.isOver())) {

        if (localTask != null && (localTask.isNotReady() || localTask.isDownToLocal() || localTask.isOver())) {
            // 快要完成了。。。
            return;
        }
        Task task = new AsyncTradeApi(user.getSessionKey(), new Date(start), new Date(end),
                TMTradeApi.RECENT_TRADE_FIELD).call();

        if (task == null) {
            log.error("no taobao task for::" + user);
            return;
        }

        localTask = ATSLocalTask.findOrNew(task, user.getId(), end, taskType);
        ATSTaskUpdate.addObject(localTask);
    }

    @Override
    protected long getInterval() {
        return DateUtil.DAY_MILLIS;
    }

    @Override
    public long getMaxUserUpdateVersion() {
        //TradeUpdateTs tradeTs = TradeUpdateTs.findById(userId);
        TradeUpdateTs tradeTs = TradeUpdateTs.findByUser(userId);
        log.info("[Found Current Version]" + tradeTs);
        return tradeTs == null ? 0L : tradeTs.getLastUpdateTime();
    }
}
