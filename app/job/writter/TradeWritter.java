
package job.writter;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import message.tradeupdate.TradeDBDone;
import models.order.OrderDisplay;
import models.trade.TradeDisplay;
import models.updatetimestamp.updates.TradeUpdateTs;
import models.user.User;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import transaction.DBBuilder;
import transaction.JDBCBuilder;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.taobao.api.domain.Order;
import com.taobao.api.domain.Trade;

import configs.TMConfigs;
import dao.trade.OrderDisplayDao;
import dao.trade.TradeDisplayDao;

@Every("5s")
public class TradeWritter extends Job {

    private static final Logger log = LoggerFactory.getLogger(TradeWritter.class);

    public static final String TAG = "TradeWritter";

    public static final Queue<TradeList> tradeListToWritten = new ConcurrentLinkedQueue<TradeList>();

    public static String statusMessage;

    public static void addTradeList(Long userId, Long ts, List<Trade> tradeList) {
        if (!TMConfigs.App.IS_TRADE_ALLOW) {
            return;
        }

        // true适用于增量添加，false为批量插入
        addTradeList(userId, ts, tradeList, true);
    }

    public static void addTradeList(Long userId, Long ts, List<Trade> tradeList, boolean isIncremental) {
        if (!TMConfigs.App.IS_TRADE_ALLOW) {
            return;
        }

        if (CommonUtils.isEmpty(tradeList)) {
            log.info("[TradeWritter] ignore addTradeList empty!!!");
            return;
        }

        while (tradeListToWritten.size() > 10000) {
            log.info("[TradeWritter] wait to add! tradeListToWritten size : " + tradeListToWritten.size());
            CommonUtils.sleepQuietly(1000L);
        }

        TradeList tradeListUpdate = new TradeList(userId, ts, tradeList, isIncremental);

        tradeListToWritten.add(tradeListUpdate);
    }

    public static void addFinishedMarkMsg(Long userId, Long ts) {
        if (!TMConfigs.App.IS_TRADE_ALLOW) {
            return;
        }
        tradeListToWritten.add(new TradeList(userId, ts, true));
    }

    public static int getQueueSize() {
        return tradeListToWritten.size();
    }

    @Override
    public void doJob() {
        if (!TMConfigs.App.IS_TRADE_ALLOW) {
            return;
        }

        Thread.currentThread().setName(TAG);
        log.error("do for trade writter---->>>>>>> size: " + tradeListToWritten.size());

        TradeList tradeListUpdate = null;
        while ((tradeListUpdate = tradeListToWritten.poll()) != null) {
            final TradeList target = tradeListUpdate;
            TMConfigs.getTradeWriterPool().submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doInsert(target);
                    return null;
                }
            });
        }
    }

    private void doInsert(final TradeList tradeList) {

        final Long userId = tradeList.userId;
        User user = User.findByUserId(userId);
        if (user == null) {
            return;
        }

        if (tradeList.isFinished) {
            statusMessage = "Finishing userId:" + tradeList.userId + " ts:" + tradeList.ts;

            log.warn(statusMessage);

            afterFinished(tradeList);
            return;
        }

        doForTrade(tradeList);

    }

    private void afterFinished(TradeList tradeList) {

        new TradeDBDone(tradeList.userId, tradeList.ts).publish();

        TradeUpdateTs.updateLastModifedTime(tradeList.userId, tradeList.ts);
    }

    private void doForTrade(TradeList tradeList) {
        if (CommonUtils.isEmpty(tradeList.tradeList)) {
            return;
        }

        statusMessage = "Do For Trade userId:" + tradeList.userId + " ts:" + tradeList.ts + " with size:"
                + tradeList.tradeList.size();

        log.warn(statusMessage);

        Long userId = tradeList.userId;
        Long ts = tradeList.ts;
        List<Trade> trades = tradeList.tradeList;

        log.info("[write for user:]" + userId + " with is incremental :" + tradeList.useIncremental);
//        if (tradeList.useIncremental) {
//            writeIncrementalTrades(userId, ts, trades);
//        } else {
//            batchWriteTrades(userId, ts, trades);
//        }
        batchWriteTrades(userId, ts, trades);

        tradeList.tradeList.clear();
    }

    public static void writeIncrementalTrades(Long userId, Long ts, Collection<Trade> trades) {
        for (Trade trade : trades) {
            new TradeDisplay(userId, ts, trade).jdbcSave();

            List<Order> orders = trade.getOrders();

            if (CommonUtils.isEmpty(orders)) {
                continue;
            }

            for (Order order : orders) {
                OrderDisplay orderModel = new OrderDisplay(userId, ts, trade, order);
                boolean jdbcSave = orderModel.jdbcSave();
//                log.info("[save order model]" + orderModel + " with res :" + jdbcSave);
            }
        }
        trades.clear();
    }

    public static void writeTradeImmediately(Long userId, List<Trade> trades, Long ts) {
        if (CommonUtils.isEmpty(trades)) {
            return;
        }

        statusMessage = "Do For Trade Immediately! userId:" + userId + " ts:" + ts + " with size:" + trades.size();

        log.warn(statusMessage);

        writeIncrementalTrades(userId, ts, trades);

        trades.clear();
    }

    public static class TradeList {

        Long userId;

        Long ts;

        boolean isFinished;

        List<Trade> tradeList;

        boolean useIncremental = true;

        public TradeList(Long userId, Long ts, List<Trade> tradeList, boolean useIncremental) {
            super();
            this.userId = userId;
            this.ts = ts;
            this.isFinished = false;
            this.tradeList = new ArrayList<Trade>(tradeList);
            this.useIncremental = useIncremental;
        }

        public TradeList(Long userId, Long ts, List<Trade> tradeList) {

            super();
            this.userId = userId;
            this.ts = ts;
            this.isFinished = false;
            this.tradeList = new ArrayList<Trade>(tradeList);
        }

        public TradeList(Long userId, Long ts, boolean isFinished) {
            super();
            this.userId = userId;
            this.ts = ts;
            this.isFinished = isFinished;
            this.tradeList = null;
        }
    }

    static Collection<Trade> mergeDumpTrades(Collection<Trade> trades) {
        if (CommonUtils.isEmpty(trades)) {
            return trades;
        }
        Map<Long, Trade> map = new HashMap<Long, Trade>();
        for (Trade trade : trades) {
            map.put(trade.getTid(), trade);
        }
        return map.values();
    }

    static List<OrderDisplay> mergeDumpOrders(List<OrderDisplay> orders) {
        if (CommonUtils.isEmpty(orders)) {
            return orders;
        }
        Map<Long, OrderDisplay> map = new HashMap<Long, OrderDisplay>();
        for (OrderDisplay order : orders) {
            map.put(order.getOid(), order);
        }
        return new ArrayList(map.values());
    }

    public static void batchWriteTrades(Long userId, Long ts, Collection<Trade> trades) {
        if (CommonUtils.isEmpty(trades)) {
            log.info("now trades for userId :" + userId);
            return;
        }

        log.info(format("batchWriteTrades:userId, ts, trades".replaceAll(", ", "=%s, ") + "=%s", userId,
                DateUtil.formDateForLog(ts), trades.size()));
        trades = mergeDumpTrades(trades);

        Set<Long> tids = new HashSet<Long>();
        List<OrderDisplay> orders = new ArrayList<OrderDisplay>();
        Set<Long> oids = new HashSet<Long>();
        StringBuilder sb = new StringBuilder();
        long curr = System.currentTimeMillis();
        long hashKey = DBBuilder.genUserIdHashKey(userId);
        int length = 0;

        sb.append(" insert into ");
        sb.append(TradeDisplay.TABLE_NAME);
        sb.append(hashKey);
//        log.info("[all fields:]" + TradeDisplayDao.ALL_FIELDS);
        sb.append(" ( " + TradeDisplayDao.ALL_FIELDS + " ) ");
        sb.append(" values ");
        Iterator<Trade> it = trades.iterator();
        /*
         * 写入order
         */
        while (it.hasNext()) {
            Trade trade = it.next();
            tids.add(trade.getTid());
            if (!CommonUtils.isEmpty(trade.getOrders())) {
                for (Order order : trade.getOrders()) {
                    orders.add(new OrderDisplay(userId, ts, trade, order));
                }
            }
            new TradeDisplay(userId, ts, trade).appendInsertLine(sb);
            if (it.hasNext()) {
                sb.append(',');
            }
        }

//        log.info("[trade sql]"+sb.toString());
        long deleteNum = TradeDisplayDao.removeUserTids(userId, tids);
        log.info(" delete tid num :" + deleteNum);
        long loadTradeNum = JDBCBuilder.insert(false, false, TradeDisplay.dp.getSrc(), sb.toString());

        orders = mergeDumpOrders(orders);

        /*
         * 写入order
         */
        sb.delete(0, sb.length());
        sb = new StringBuilder();
        sb.append(" insert into ");
        sb.append(OrderDisplay.TABLE_NAME);
        sb.append(hashKey);
        sb.append(" ( " + OrderDisplayDao.ALL_FIELDS + " ) ");
        sb.append(" values ");
        Iterator<OrderDisplay> oit = orders.iterator();
        while (oit.hasNext()) {
            OrderDisplay order = oit.next();
            oids.add(order.getOid());
            order.appendInsertLine(sb);
            if (oit.hasNext()) {
                sb.append(',');
            }
        }

        long removeUserOids = OrderDisplayDao.removeUserOids(userId, oids);
        log.info("[remove user oid num:]" + removeUserOids);
        long loadOrderNum = JDBCBuilder.insert(false, false, OrderDisplay.dp.getSrc(), sb.toString());

//        log.info("[load trade for user:]" + userId + "  with trade num[" + loadTradeNum + "] and order num:]"
//                + loadOrderNum + "]");
    }

    /**
     * @deprecated  No load data in file for the taobao rds....
     * @param userId
     * @param ts
     * @param trades
     */
    public static void oldloadToDB(Long userId, Long ts, List<Trade> trades) {
        Set<Long> tids = new HashSet<Long>();
        List<OrderDisplay> orders = new ArrayList<OrderDisplay>();
        Set<Long> oids = new HashSet<Long>();

        long curr = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        File tradeFile = new File(Play.tmpDir, "trade_" + userId + curr);
        File orderFile = new File(Play.tmpDir, "order_" + userId + curr);

        /*
         * 写入order
         */
        for (Trade trade : trades) {
            tids.add(trade.getTid());
            if (!CommonUtils.isEmpty(trade.getOrders())) {
                for (Order order : trade.getOrders()) {
                    orders.add(new OrderDisplay(userId, ts, trade, order));
                }
            }

            new TradeDisplay(userId, ts, trade).appendTradeLine(sb);
        }

        try {
            FileUtils.writeStringToFile(tradeFile, sb.toString());
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
        sb.delete(0, sb.length());
        TradeDisplayDao.removeUserTids(userId, tids);
        long loadTradeNum = TradeDisplayDao.executeTradeLoadDataInFile(userId, tradeFile);

        /*
         * 写入order
         */
        for (OrderDisplay order : orders) {
            oids.add(order.getOid());
            order.appendTradeLine(sb);
        }
        try {
            FileUtils.writeStringToFile(orderFile, sb.toString());
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        }
        OrderDisplayDao.removeUserOids(userId, oids);
        long loadOrderNum = OrderDisplayDao.executeOrderLoadDataInFile(userId, orderFile);

        tradeFile.delete();
        orderFile.delete();
        log.info("[load trade for user:]" + userId + "  with trade num[" + loadTradeNum + "] and order num:]"
                + loadOrderNum + "]");
    }
}
