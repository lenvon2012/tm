
package job.apiget;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import job.writter.TradeRateWritter;
import models.updatetimestamp.updates.TradeRateUpdateTs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.DateUtil;
import bustbapi.TradeRateApi;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.TradeRate;

import controllers.APIConfig;

public class TradeRateUpdateJob extends TBUpdateJob {

    private static final Logger log = LoggerFactory.getLogger(TradeRateUpdateJob.class);

    private static final String TAG = "TradeRateUpdateJob";

    private static boolean forceUpdateAll = false;

    public TradeRateUpdateJob(Long userId, boolean forceUpdateAll) {
        super(userId);
        this.forceUpdateAll = forceUpdateAll;
        this.now = System.currentTimeMillis();
    }

    public TradeRateUpdateJob(Long userId, Long ts) {
        super(userId);
        this.now = ts;
    }

    private static HashMap<Long, Long> updateHistory = new HashMap<Long, Long>();

    protected boolean prepare() {
        Thread.currentThread().setName(TAG);
        if (!APIConfig.get().enableSyncTradeRate()) {
            return false;
        }

        maxUpdateTs = getMaxUserUpdateVersion();
        if (maxUpdateTs == 0) {
            this.isFirstUpdate = true;
            maxUpdateTs = now - DateUtil.DAY_MILLIS * 30;
        }
        
        log.info("Current Max Info:" + new Date(maxUpdateTs));

        if (now - maxUpdateTs < DateUtil.ONE_MINUTE_MILLIS) {
            log.info("[No Need To Update for]just one min:" + userId);
            return false;
        }

        // 将最近更新日期格式化成天，当天更新过重新更新
//        if (maxUpdateTs > 0) {
//            maxUpdateTs = DateUtil.formDailyTimestamp(maxUpdateTs);
//        }

        // long maxReachable = today - PYConfigs.TradeRateDay.MAX_RATE_GET * DateUtil.DAY_MILLIS;
//        long maxReachable = now - TradeRateUpdateTs.MAX_AVAILABLE_SPAN;
//        start = Math.max(maxReachable, maxUpdateTs);
        long minReachable = now - DateUtil.DAY_MILLIS;
        start = Math.min(minReachable, maxUpdateTs);
        end = now;

//        if (forceUpdateAll == true) {
//            // 时间故意每次都拖30天的评价
//            Long lastTs = updateHistory.get(user.getId());
//            if (lastTs == null || now - lastTs.longValue() > DateUtil.WEEK_MILLIS) {
//                start = end - DateUtil.THIRTY_DAYS;
//                updateHistory.put(userId, now);
//            }
//        }

        log.info("Set new Start and End[" + new Date(start) + "--" + new Date(end) + "] userId=" + userId);
        return true;
    }

    public void requestUpdate(long start, long end) {

        List<TradeRate> tradeRateList = TradeRateApi.fetchBadTradeRate(user, start, end);
        log.warn("requestUpdate tradeRateList size: " + (tradeRateList == null ? 0 : tradeRateList.size())
                + "  userId=" + userId);
        if (!CommonUtils.isEmpty(tradeRateList)) {
            TradeRateWritter.addTradeRateList(userId, end, tradeRateList);
        }

        TradeRateUpdateTs.updateLastTradeRateModifedTime(userId, end);

//        tradeRateList.clear();
    }

    public long getMaxUserUpdateVersion() {
        TradeRateUpdateTs tradeRateTs = TradeRateUpdateTs.findByUserId(userId);
        return tradeRateTs == null ? 0L : tradeRateTs.getLastUpdateTime();
    }

//    public static void doWithDeleteTradeRate(User user) {
//        PageOffset po = new PageOffset(1, 3000);
//        List<TradeRatePlay> list = TradeRatePlayDao.searchWithArgs(user.getId(), null, null, 128,
//                System.currentTimeMillis() - DateUtil.THIRTY_DAYS, null, null, null, po);
//        if (CommonUtils.isEmpty(list)) {
//            return;
//        }
//        for (TradeRatePlay tradeRatePlay : list) {
//            List<TradeRate> rateList = new TradeRateApi.TraderatesGet(user, tradeRatePlay.getTid()).call();
//            boolean found = false;
//            if (!CommonUtils.isEmpty(rateList)) {
//                for (TradeRate tradeRate : rateList) {
//                    if (tradeRate.getOid() == tradeRatePlay.getOid()) {
//                        int newRate = TradeRatePlay.parseRate(tradeRate.getResult());
//                        if (tradeRatePlay.getLatestRate() != newRate) {
//                            tradeRatePlay.putTradeRate(tradeRate);
//                            tradeRatePlay.rawUpdate();
//                        }
//                        found = true;
//                        break;
//                    }
//                }
//            }
//
//            if (CommonUtils.isEmpty(rateList) || found == false) {
//                if (tradeRatePlay.getLatestRate() > 0) {
//                    log.warn(String.format("[delete comment] userId=%d, tid=%d, oid=%d", user.getId(),
//                            tradeRatePlay.getTid(), tradeRatePlay.getOid()));
//                    tradeRatePlay.setNewRate(0);
//                    tradeRatePlay.setUpdated(System.currentTimeMillis());
//                    tradeRatePlay.rawUpdate();
//                } else if (tradeRatePlay.getLatestRate() == 0) {
//                    int rate = tradeRatePlay.getRate();
//                    int lastRate = 0;
//                    while (rate > 0) {
//                        lastRate = rate;
//                        rate = (rate >> 2);
//                    }
//
//                    int newRate = (lastRate << 2);
//                    if (newRate != tradeRatePlay.getRate()) {
//                        tradeRatePlay.setRate(newRate);
//                        tradeRatePlay.rawUpdate();
//                    }
//                }
//            }
//        }
//    }
}
