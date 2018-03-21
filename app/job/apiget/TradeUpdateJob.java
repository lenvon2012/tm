
package job.apiget;

import static java.lang.String.format;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import jdp.JdpModel.JdpTradeModel;
import job.writter.TradeWritter;
import message.tradeupdate.TradeApiDoing;
import message.tradeupdate.TradeApiDoneDBDoing;
import message.tradeupdate.TradeApiException;
import models.updatetimestamp.updates.TradeUpdateTs;
import models.updatetimestamp.updatestatus.TradeDailyUpdateTask;
import models.visit.ATSLocalTask;
import models.visit.ATSLocalTask.TaskType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import secure.SimulateRequestUtil;
import ats.ATSTaskUpdate;
import bustbapi.TMTradeApi;

import com.ciaosir.client.utils.DateUtil;
import com.taobao.api.domain.Task;
import com.taobao.api.domain.Trade;

import configs.TMConfigs;
import configs.TMConfigs.ExpiredTime;
import configs.TMConfigs.TradeDay;
import dao.UserDao;

public class TradeUpdateJob extends TBUpdateJob {

    private static final Logger log = LoggerFactory.getLogger(TradeUpdateJob.class);

    private static final String TAG = "TradeUpdateJob";

    public TradeUpdateJob(Long userId, Long ts) {
        super(userId);
        // this.now = DateUtil.formDailyTimestamp(ts);
        this.now = ts;
        log.warn("[new trade update job]" + userId);
    }

    @Override
    protected boolean prepare() {

        Thread.currentThread().setName(TAG);

        if (now <= 0L) {
            // now = DateUtil.formCurrDate();
            now = System.currentTimeMillis();
        }

        TradeUpdateTs tradeTs = TradeUpdateTs.findByUserId(userId);
        log.info("[Found Current Version]" + tradeTs);
//
//        if (tradeTs == null && TMConfigs.ENABLE_ASYNC_TRADEUPDATE == true) {
//            // No update ts??? let's call the async trade update job to ensure the job...
//            log.warn("[TradeUpdateJob] <submit async trade update job> for userId: " + userId);
//            ensureAsyncTrade();
//            return false;
//        }

        // maxUpdateTs = getMaxUserUpdateVersion();
        // return tradeTs == null ? 0L : tradeTs.getLastUpdateTime();
        if (tradeTs != null) {
            maxUpdateTs = tradeTs.getLastUpdateTime();
        }
        if (maxUpdateTs == 0) {
            maxUpdateTs = now - DateUtil.DAY_MILLIS * TradeDay.MAX_TRADE_GET;
        }

        if (maxUpdateTs < DateUtil.formDailyTimestamp(user.getFirstLoginTime())) {
            this.isFirstUpdate = true;
        }

        if (now - maxUpdateTs < DateUtil.TEN_MINUTE_MILLIS) {
            log.info("[No Need To Update for]" + user.getId());
            return false;
        }

        start = maxUpdateTs;
        end = now;

        log.info("[Set new Start and End]" + new Date(start) + new Date(end));

        return true;
    }

    private void ensureAsyncTrade() {
        log.warn("[do for async start trades...]");
        ATSLocalTask localTask = ATSLocalTask.findTradeRecentSold(user.getId());
//        if (localTask != null && (localTask.isDownToLocal() || localTask.isOver())) {

        if (localTask != null && (localTask.isNotReady() || localTask.isDownToLocal() || localTask.isOver())) {
            // 快要完成了。。。
            return;
        }

        long end = DateUtil.formDailyTimestamp(user.getFirstLoginTime()) - DateUtil.DAY_MILLIS;

        if (System.currentTimeMillis() - end > TradeUpdateTs.MAX_VALID_INTERVAL) {
            end = DateUtil.formCurrDate();
        }

        long maxTradeGetMills = TradeDay.MAX_TRADE_GET * DateUtil.DAY_MILLIS;
        long start = end - maxTradeGetMills;
        if (DateUtil.formCurrDate() - start > maxTradeGetMills) {
            start = DateUtil.formCurrDate() - maxTradeGetMills + DateUtil.DAY_MILLIS;
        }

        Task task = new TMTradeApi.AsyncTradeApi(user.getSessionKey(), new Date(start), new Date(end)).call();

        if (task == null) {
            log.error("no taobao task for::" + user);
            return;
        }

        localTask = ATSLocalTask.findOrNew(task, user.getId(), end, TaskType.ATSTradeSold);
        ATSTaskUpdate.addObject(localTask);
//        }

    }

    public void requestUpdate(long start, long end) {

        log.warn(format("Trade Update Job:start, end".replaceAll(", ", "=%s, ") + "=%s", start, end));

        if (isFirstUpdate) {
            UserDao.refreshTokenNow(user);
        }

        for (long tempStart = start; tempStart < end; tempStart += getInterval()) {

            long tempEndDay = tempStart + getInterval();
            if (!checkTask(user.getId(), tempEndDay)) {
                log.warn(String.format("TradeUpdate for %s, startTs %s, endTs %s is already doing!!!", user.getId(),
                        tempStart, tempEndDay));
                continue;
            }

            log.warn(String.format("TradeUpdate for %s, startTs %s, endTs %s ", user.getId(), tempStart, tempEndDay));

            try {

                new TradeApiDoing(userId, tempEndDay).publish();

                if (!getTrades(tempStart, tempEndDay)) {
                    new TradeApiException(userId, tempEndDay).publish();
                    log.error("Get Api Fails for the trade update job:[userId:" + userId + "]");
                    return;
                }

                new TradeApiDoneDBDoing(userId, tempEndDay).publish();
                TradeWritter.addFinishedMarkMsg(user.getId(), tempEndDay);

            } catch (Exception e) {
                log.error("[TradeUpdateJob] error ! userId=" + userId);
                log.error(e.getMessage(), e);
            }

        }
    }

    public boolean checkTask(Long userId, Long taskTs) {

        TradeDailyUpdateTask task = TradeDailyUpdateTask.findByUserIdAndTs(userId, taskTs);

        if (task != null && (System.currentTimeMillis() - task.getUpdateAt() < ExpiredTime.TASK_EXPIRE_TIME)) {
            return false;
        }

        return true;
    }

    private boolean getTrades(long currStart, long currEnd) {
        List<Trade> tradeList = null;
        if (isFirstUpdate) {
            tradeList = new TMTradeApi.TradesSold(user, currStart, new Date(currStart), new Date(currEnd), false)
                    .call();
            // 御城河日志接入
            SimulateRequestUtil.sendTopLog(SimulateRequestUtil.TRADES_SOLD_GET);
            TradeWritter.addTradeList(userId, currStart, tradeList, false);
        } else {
            tradeList = new TMTradeApi.TradesSoldIncrementextends(user, currStart, new Date(currStart), new Date(
                    currEnd), false).call();
            // 御城河日志接入
            SimulateRequestUtil.sendTopLog(SimulateRequestUtil.TRADES_SOLD_INCREMENT_GET);
            TradeWritter.addTradeList(userId, currStart, tradeList, true);
        }

        return true;

        // return false === 报错时~
    }

    @Override
    protected long getInterval() {
        return DateUtil.DAY_MILLIS;
    }

    @Override
    public long getMaxUserUpdateVersion() {
        TradeUpdateTs tradeTs = TradeUpdateTs.findByUser(userId);
        log.info("[Found Current Version]" + tradeTs);
        return tradeTs == null ? 0L : tradeTs.getLastUpdateTime();
    }

    public class TradeJdpSyncer implements Callable<Boolean> {
        private long currStart;

        private long currEnd;

        public TradeJdpSyncer(long currStart, long currEnd) {
            super();
            this.currStart = currStart;
            this.currEnd = currEnd;
        }

        @Override
        public Boolean call() throws Exception {
            Map<Long, Trade> tradeMap = JdpTradeModel.queryModifiedTrades(user.getUserNick(), currStart, currEnd);
            TradeWritter.batchWriteTrades(userId, end, tradeMap.values());
            return null;
        }
    }
}
