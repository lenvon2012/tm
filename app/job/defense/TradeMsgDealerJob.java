
package job.defense;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import job.writter.TradeWritter;
import models.defense.TidReceiveTime;
import models.trade.TradeDisplay;
import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.jobs.Every;
import play.jobs.Job;
import secure.SimulateRequestUtil;
import bustbapi.TMTradeApi;

import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.utils.DateUtil;
import com.taobao.api.domain.Trade;

import configs.TMConfigs;
import controllers.APIConfig;
import dao.UserDao;

@Every("2s")
public class TradeMsgDealerJob extends Job {

    public static final String TAG = "TradeMsgDealerJob";

    private static final Logger log = LoggerFactory.getLogger(TradeMsgDealerJob.class);

    private static Queue<TradeMsg> queue = new ConcurrentLinkedQueue<TradeMsg>();

    private static Queue<Trade> tradeQueue = new ConcurrentLinkedQueue<Trade>();

    private static Queue<TradeMsg> errorQueue = new ConcurrentLinkedQueue<TradeMsg>();

    PYFutureTaskPool<Boolean> tasks = new PYFutureTaskPool<Boolean>(128);

    public static void addTradeMsg(String msgBody) {
        if (TMConfigs.App.IS_TRADE_ALLOW) {
//            log.info("[msg body :]" + msgBody);
            TradeMsg tradeMsg = new TradeMsg(1, msgBody);
            queue.add(tradeMsg);
        }
    }

    public static void addTrade(Trade trade) {
        tradeQueue.add(trade);
    }

    @Override
    public void doJob() {
        if (!TMConfigs.App.IS_TRADE_ALLOW) {
            return;
        }

        Thread.currentThread().setName(TAG);

//        log.info("[current queue size =]" + queue.size());

        Trade tMsg = null;
        while ((tMsg = tradeQueue.poll()) != null) {
            if (APIConfig.get().enableSyncTradeRate()) {
                doCloseTrade(tMsg);
            }
        }
        errorQueue = new ConcurrentLinkedQueue<TradeMsg>();
        TradeMsg tradeMsg = null;

        while ((tradeMsg = queue.poll()) != null) {
            if (APIConfig.get().enableSyncTradeRate()) {
                doCloseTrade(tradeMsg);
            }
        }

        TradeMsg errorMsg = null;
        while ((errorMsg = errorQueue.poll()) != null) {
            queue.add(errorMsg);
        }

    }

    private void doCloseTrade(Trade tMsg) {
        if (tMsg == null) {
            return;
        }

        String nick = tMsg.getSellerNick();
        if (StringUtils.isEmpty(nick)) {
            log.error(" no  nick :" + tMsg);
            return;
        }

        User user = UserDao.findByUserNick(nick);
        if (user == null) {
            log.error(" no  user :" + tMsg);
        }

        judgeTradeClose(user, tMsg);
    }

    private void doCloseTrade(TradeMsg tradeMsg) {
        if (!APIConfig.get().doCloseTrade()) {
            return;
        }

        try {
            JSONObject tradeJson = new JSONObject(tradeMsg.getMsgBody());
            if (tradeJson.has("notify_trade")) {
                tradeJson = tradeJson.getJSONObject("notify_trade");
            }
            long userId = tradeJson.getLong("user_id");
            User user = UserDao.findById(userId);
            if (user == null) {
                /*log.error("[TradeMsgDealerJob] userId not found||inValid!!, userId=" + userId);*/
                return;
            }
            if (!user.isVaild() || !user.isAutoDefenseOn()) {
                /*log.error("[TradeMsgDealerJob] userId not found||inValid!!, userId=" + userId + " ; valid: "
                        + user.isVaild());*/
                return;
            }

            String buyerNick = tradeJson.getString("buyer_nick");
//            log.info("[TradeMsgDealerJob]trade buyer: " + buyerNick);
            if (StringUtils.isEmpty(buyerNick)) {
                log.error("buyerNick null! tradeMsg: " + tradeMsg);
                return;
            }

            long tradeId = Long.parseLong(tradeJson.getString("tid"));
            Trade trade = new TMTradeApi.GetFullTrade(user.getSessionKey(), tradeId).call();
            // 御城河日志接入
            SimulateRequestUtil.sendTopLog("taobao.trade.fullinfo.getfield=" + TMTradeApi.TRADE_FIELDS);
            if (trade == null) {
                log.error("[TradeMsgDealerJob]GetFullTrade null! tid=" + tradeId + ", userId=" + userId);
                return;
            }
            if (buyerNick != null) {
                trade.setBuyerNick(buyerNick);
            }

            judgeTradeClose(user, trade);

        } catch (Exception ex) {
            log.error("[TradeMsgDealer] submit error! >>>>>>>>>>>>>>>>>>>>>>>");
            log.error(ex.getMessage(), ex);
        }
    }

    static String RECENT_CANCEL_TAG = "_defenderClose";

    private String genCancelKey(Long tid) {
        return RECENT_CANCEL_TAG + tid;
    }

    public void judgeTradeClose(final User user, final Trade trade) {
        if (user == null || !user.isAutoDefenseOn()) {
//            log.warn(" user not valid for user:" + user + "  with trade :" + trade);
            return;
        }
        final List<Trade> trades = new ArrayList<Trade>();
        trades.add(trade);
        final Long ts = trade.getCreated().getTime();

        // 进来的都是defender的
        if (TradeDisplay.isCreateStatus(trade)) {
            String key = genCancelKey(trade.getTid());
            Boolean object = (Boolean) Cache.get(key);
            if (object != null) {
                log.warn(" trade has been recent judged :" + trade.getTid() + "and object = " + object);
                return;
            }
            Cache.set(key, Boolean.TRUE, "2h");
            TidReceiveTime.addMsg(user.getId(), trade);

            tasks.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    log.info("[TradeMsgDealerJob] submit TradeDefenseCaller; uid=" + user.getId() + ", buyerNick="
                            + trade.getBuyerNick());
                    // 先写数据库，做限购判断
                    long startMillis = System.currentTimeMillis();
                    TradeWritter.writeTradeImmediately(user.getId(), trades, ts);
                    long endWriteMillis = System.currentTimeMillis();
                    new TradeDefenseCaller(user, trade, trade.getBuyerNick()).call();
                    long endDefense = System.currentTimeMillis();
                    log.info(" trade defense for trade :" + trade + " with start :"
                            + DateUtil.formDateForLog(startMillis) + " middle :"
                            + DateUtil.formDateForLog(endWriteMillis) + "  end defense :" + endDefense
                            + "  write took :" + (endWriteMillis - startMillis) + "ms --> defense:"
                            + (endDefense - endWriteMillis
                            ) + " ms");
                    return null;
                }
            });

        } else {
            TradeWritter.addTradeList(user.getId(), ts, trades, true);
            log.info("[TradeMsgDealerJob] trade not submit, satus=" + trade.getStatus() + ", trade=" + trade);
        }
    }

    public static class TradeMsg {
        int inQueueTime = 0;

        String msgBody;

        public void updateCountPlus() {
            inQueueTime++;
        }

        public TradeMsg() {
            super();
        }

        public TradeMsg(int inQueueTime, String msgBody) {
            super();
            this.inQueueTime = inQueueTime;
            this.msgBody = msgBody;
        }

        public int getInQueueTime() {
            return inQueueTime;
        }

        public void setInQueueTime(int inQueueTime) {
            this.inQueueTime = inQueueTime;
        }

        public String getMsgBody() {
            return msgBody;
        }

        public void setMsgBody(String msgBody) {
            this.msgBody = msgBody;
        }

        @Override
        public String toString() {
            return "TradeMsg [inQueueTime=" + inQueueTime + ", msgBody=" + msgBody + "]";
        }
    }

}
