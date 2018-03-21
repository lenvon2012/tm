package job.writter;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import message.tradeupdate.TradeDBDone;
import models.paipai.PaiPaiUser;
import models.updatetimestamp.updates.TradeUpdateTs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import ppapi.models.PaiPaiTradeDisplay;
import ppapi.models.PaiPaiTradeItem;

import com.ciaosir.client.CommonUtils;

import controllers.APIConfig;
import controllers.APIConfig.Platform;

@Every("10s")
public class PaiPaiTradeWritter extends Job {

    private static final Logger log = LoggerFactory.getLogger(PaiPaiTradeWritter.class);

    public static final String TAG = "PaiPaiTradeWritter";

    public static final Queue<TradeList> tradeListToWritten = new ConcurrentLinkedQueue<TradeList>();

    public static String statusMessage;

    public static void addTradeList(Long userId, Long ts, List<PaiPaiTradeDisplay> tradeList) {

        TradeList tradeListUpdate = new TradeList(userId, ts, tradeList);

        while (tradeListToWritten.size() > 512) {
            CommonUtils.sleepQuietly(1000L);
        }

        tradeListToWritten.add(tradeListUpdate);

    }

    public static void addFinishedMarkMsg(Long userId, Long ts) {

        tradeListToWritten.add(new TradeList(userId, ts, true));
    }

    @Override
    public void doJob() {

        Thread.currentThread().setName(TAG);
        if (APIConfig.get().getPlatform() != Platform.paipai) {
            return;
        }
        
        log.error("do for paipaitrade writter---->>>>>>>");

        TradeList tradeListUpdate = null;

        while ((tradeListUpdate = tradeListToWritten.poll()) != null) {
            doInsert(tradeListUpdate);
        }
    }

    private static void doInsert(TradeList tradeList) {

        final Long userId = tradeList.userId;    	
        PaiPaiUser user = PaiPaiUser.findByUserId(userId);
        if (user == null) {
            return;
        }
        
        if (tradeList.isFinished) {
            statusMessage = "Finishing userId:" + tradeList.userId + " ts:" + tradeList.ts;

            log.warn(statusMessage);

            afterFinished(tradeList);
            return;
        }

        List<PaiPaiTradeDisplay> trades = tradeList.tradeList; 
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
        List<PaiPaiTradeDisplay> trades = new ArrayList<PaiPaiTradeDisplay>();
        trades.addAll(tradeList.tradeList);

        for (PaiPaiTradeDisplay trade : trades) {
            trade.jdbcSave();

            List<PaiPaiTradeItem> orders = trade.getItemList();

            if (CommonUtils.isEmpty(orders)) {
                continue;
            }

            for (PaiPaiTradeItem order : orders) {
                order.jdbcSave();
            }
        }
    }

    public static class TradeList {

        Long userId;

        Long ts;

        boolean isFinished;

        List<PaiPaiTradeDisplay> tradeList;

        public TradeList(Long userId, Long ts, List<PaiPaiTradeDisplay> tradeList) {

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
