package job.writter;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import message.tradeupdate.TradeDBDone;
import models.order.JDOrderDisplay;
import models.trade.JDTradeDisplay;
import models.updatetimestamp.updates.TradeUpdateTs;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;

import com.ciaosir.client.CommonUtils;
import com.jd.open.api.sdk.domain.order.ItemInfo;
import com.jd.open.api.sdk.domain.order.OrderSearchInfo;

import dao.UserDao;

@Every("10s")
public class JDTradeWritter extends Job {

    private static final Logger log = LoggerFactory.getLogger(JDTradeWritter.class);

    public static final String TAG = "TradeWritter";

    public static final Queue<TradeList> tradeListToWritten = new ConcurrentLinkedQueue<TradeList>();

    public static String statusMessage;

    public static void addTradeList(Long userId, Long ts, List<OrderSearchInfo> tradeList) {

        TradeList tradeListUpdate = new TradeList(userId, ts, tradeList);

        while (tradeListToWritten.size() > 512) {
            CommonUtils.sleepQuietly(60000L);
        }

        tradeListToWritten.add(tradeListUpdate);

    }

    public static void addFinishedMarkMsg(Long userId, Long ts) {

        tradeListToWritten.add(new TradeList(userId, ts, true));
    }

    @Override
    public void doJob() {

        Thread.currentThread().setName(TAG);
        //log.error("do for jdtrade writter---->>>>>>>");

        TradeList tradeListUpdate = null;

        while ((tradeListUpdate = tradeListToWritten.poll()) != null) {
            doInsert(tradeListUpdate);
        }
    }

    private static void doInsert(TradeList tradeList) {

        final Long userId = tradeList.userId;
        //User user = User.findById(userId);
        User user = UserDao.findById(userId);
        if (user == null) {
            return;
        }

        if (tradeList.isFinished) {
            statusMessage = "Finishing userId:" + tradeList.userId + " ts:" + tradeList.ts;

            log.warn(statusMessage);

            afterFinished(tradeList);
            return;
        }

        List<OrderSearchInfo> trades = tradeList.tradeList;
        if (CommonUtils.isEmpty(trades)) {
            return;
        }

        statusMessage = "Do For Trade userId:" + tradeList.userId + " ts:" + tradeList.ts + " with size:"
                + tradeList.tradeList.size();

        log.warn(statusMessage);

        doForTrade(tradeList);

        tradeList.tradeList.clear();
    }

    private static void afterFinished(TradeList tradeList) {

        new TradeDBDone(tradeList.userId, tradeList.ts).publish();

        TradeUpdateTs.updateLastModifedTime(tradeList.userId, tradeList.ts);
    }

    private static void doForTrade(TradeList tradeList) {
        Long userId = tradeList.userId;
        Long ts = tradeList.ts;
        List<OrderSearchInfo> trades = new ArrayList<OrderSearchInfo>();
        trades.addAll(tradeList.tradeList);

        for (OrderSearchInfo trade : trades) {
            new JDTradeDisplay(userId, ts, trade).jdbcSave();

            List<ItemInfo> orders = trade.getItemInfoList();

            if (CommonUtils.isEmpty(orders)) {
                continue;
            }

            for (ItemInfo order : orders) {
                new JDOrderDisplay(userId, ts, trade, order).jdbcSave();
            }
        }
    }

    public static class TradeList {

        Long userId;

        Long ts;

        boolean isFinished;

        List<OrderSearchInfo> tradeList;

        public TradeList(Long userId, Long ts, List<OrderSearchInfo> tradeList) {

            super();
            this.userId = userId;
            this.ts = ts;
            this.isFinished = false;
            this.tradeList = tradeList;
        }

        public TradeList(Long userId, Long ts, boolean isFinished) {
            super();
            this.userId = userId;
            this.ts = ts;
            this.isFinished = isFinished;
            this.tradeList = null;
        }
    }

}
