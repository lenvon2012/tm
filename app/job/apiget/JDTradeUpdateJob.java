package job.apiget;

import static java.lang.String.format;

import java.util.Date;
import java.util.List;

import job.writter.JDTradeWritter;
import message.tradeupdate.jd.JDTradeApiDoing;
import message.tradeupdate.jd.JDTradeApiDoneDBDoing;
import models.updatetimestamp.updates.JDTradeUpdateTs;
import models.updatetimestamp.updates.TradeUpdateTs;
import models.updatetimestamp.updatestatus.JDTradeDailyUpdateTask;
import models.visit.ATSLocalTask;
import models.visit.ATSLocalTask.TaskType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ats.ATSTaskUpdate;
import bustbapi.JDTradeApi;
import bustbapi.TMTradeApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.jd.open.api.sdk.domain.order.OrderSearchInfo;
import com.taobao.api.domain.Task;
import com.taobao.api.domain.Trade;

import configs.TMConfigs.ExpiredTime;
import configs.TMConfigs.TradeDay;

public class JDTradeUpdateJob extends JDUpdateJob {

    private static final Logger log = LoggerFactory.getLogger(TradeUpdateJob.class);

    private static final String TAG = "TradeUpdateJob";

    public JDTradeUpdateJob(Long userId, Long ts) {
        super(userId);
        this.now = DateUtil.formDailyTimestamp(ts);
        log.warn("[new trade update job]" + userId);
    }

    @Override
    protected boolean prepare() {

        Thread.currentThread().setName(TAG);
        if (now <= 0L) {
            now = DateUtil.formCurrDate();
        }

        //JDTradeUpdateTs tradeTs = JDTradeUpdateTs.findById(userId);
        JDTradeUpdateTs tradeTs = JDTradeUpdateTs.findByUserId(userId);
        log.info("[Found Current Version]" + tradeTs);
        if (tradeTs == null) {
            // No update ts??? let's call the async trade update job to ensure the job...
            // ensureAsyncTrade();
            // return false;
            ensureTrade();
        }

        // maxUpdateTs = getMaxUserUpdateVersion();
        // return tradeTs == null ? 0L : tradeTs.getLastUpdateTime();
        maxUpdateTs = tradeTs.getLastUpdateTime();
        if (maxUpdateTs == 0) {
            maxUpdateTs = now - DateUtil.DAY_MILLIS * TradeDay.MAX_TRADE_GET;
        }

        if (maxUpdateTs < DateUtil.formDailyTimestamp(user.getFirstLoginTime())) {
            this.isFirstUpdate = true;
        }

        if (now - maxUpdateTs < getInterval()) {
            log.info("[No Need To Update for]" + user.getId());
            return false;
        }

        start = maxUpdateTs;
        end = now;

        log.info("[Set new Start and End]" + new Date(start) + new Date(end));
        return true;
    }

    private void ensureTrade() {
        log.warn("[do for async start trades...]");

        end = DateUtil.formDailyTimestamp(user.getFirstLoginTime()) - DateUtil.DAY_MILLIS;

        if (System.currentTimeMillis() - end > JDTradeUpdateTs.MAX_VALID_INTERVAL) {
            end = DateUtil.formCurrDate();
        }
        start = end - DateUtil.THIRTY_DAYS;

        // Task task = new TradeApi.AsyncTradeApi(user.getSessionKey(), new Date(start), new Date(end)).call();

        // if (task == null) {
        // log.error("no taobao task for::" + user);
        // return;
        // }

    }

    public void requestUpdate(long start, long end) {

        log.warn(format("Trade Update Job:start, end".replaceAll(", ", "=%s, ") + "=%s", start, end));

        for (long tempStart = start; tempStart < end; tempStart += getInterval()) {

            long tempEndDay = tempStart + getInterval();

            if (!checkTask(user.getId(), tempEndDay)) {
                log.warn(String.format("TradeUpdate for %s, startTs %s, endTs %s is already doing!!!", user.getId(),
                        tempStart, tempEndDay));
                continue;
            }

            log.warn(String.format("JDTradeUpdate for %s, startTs %s, endTs %s ", user.getId(), tempStart, tempEndDay));

            new JDTradeApiDoing(userId, tempEndDay).publish();

            // if (!getTrades(tempStart, tempEndDay)) {
            //
            // new TradeApiException(userId, tempEndDay).publish();
            //
            // log.error("Get Api Fails for the trade update job:[userId:" + userId + "]");
            // return;
            // }

            List<OrderSearchInfo> tradeList = getJDTrades(tempStart, tempEndDay);
            if (!CommonUtils.isEmpty(tradeList)) {
                JDTradeWritter.addTradeList(userId, tempEndDay, tradeList);
            }

            new JDTradeApiDoneDBDoing(userId, tempEndDay).publish();

            JDTradeWritter.addFinishedMarkMsg(user.getId(), tempEndDay);
        }
    }

    public boolean checkTask(Long userId, Long taskTs) {

        JDTradeDailyUpdateTask task = JDTradeDailyUpdateTask.findByUserIdAndTs(userId, taskTs);

        if (task != null && (System.currentTimeMillis() - task.getUpdateAt() < ExpiredTime.TASK_EXPIRE_TIME)) {
            return false;
        }

        return true;
    }

    private boolean getTrades(long currStart, long currEnd) {

        List<Trade> tradeList = null;
        // if (isFirstUpdate) {
        // tradeList = new TradeApi.TradesSold(user, currStart, new Date(currStart), new Date(currEnd), false).call();
        //
        // } else {
        // tradeList = new TradeApi.TradesSoldIncrementextends(user, currStart, new Date(currStart),
        // new Date(currEnd), false).call();
        // }

        return tradeList != null;
    }

    private List<OrderSearchInfo> getJDTrades(long currStart, long currEnd) {

        List<OrderSearchInfo> list = new JDTradeApi.JDTradesSold(user, currStart, new Date(currStart),
                new Date(currEnd)).call();

        return list;

    }

    @Override
    protected long getInterval() {
        return DateUtil.DAY_MILLIS;
    }

    @Override
    public long getMaxUserUpdateVersion() {

        //JDTradeUpdateTs tradeTs = JDTradeUpdateTs.findById(userId);
    	JDTradeUpdateTs tradeTs = JDTradeUpdateTs.findByUserId(userId);
        log.info("[Found Current Version]" + tradeTs);
        return tradeTs == null ? 0L : tradeTs.getLastUpdateTime();
    }

    private void ensureAsyncTrade() {
        log.warn("[do for async start trades...]");
        ATSLocalTask localTask = ATSLocalTask.findTradeRecentSold(user.getId());
        // if (localTask != null && (localTask.isDownToLocal() || localTask.isOver())) {

        if (localTask != null && (localTask.isNotReady() || localTask.isDownToLocal() || localTask.isOver())) {
            // 快要完成了。。。
            return;
        }

        long end = DateUtil.formDailyTimestamp(user.getFirstLoginTime()) - DateUtil.DAY_MILLIS;

        if (System.currentTimeMillis() - end > TradeUpdateTs.MAX_VALID_INTERVAL) {
            end = DateUtil.formCurrDate();
        }
        long start = end - DateUtil.THIRTY_DAYS;

        Task task = new TMTradeApi.AsyncTradeApi(user.getAccessToken(), new Date(start), new Date(end)).call();

        if (task == null) {
            log.error("no taobao task for::" + user);
            return;
        }

        localTask = ATSLocalTask.findOrNew(task, user.getId(), end, TaskType.ATSTradeSold);
        ATSTaskUpdate.addObject(localTask);
        // }

    }
}
