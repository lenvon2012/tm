
package job.writter;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import job.defense.TradeRateMsgDealer;
import models.traderate.TradeRatePlay;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.taobao.api.domain.TradeRate;

import configs.TMConfigs;
import controllers.Items;
import dao.UserDao;
import dao.trade.OrderDisplayDao;
import dao.trade.TradeRatePlayDao;

/**
 * 准备截杀交易评价变更的主动通知
 * 
 * 12、交易评价变更TradeRated：交易的商品或店铺被评价时产生此消息。通过页面完成评价商品和评价店铺动态评分后，会产生此消息。
 * 当只评价商品或只评价店铺动态评分不会产生此消息，只会产生交易变更消息（TradeChanged），此可以进入页面中继续评价另一项。 当追加评价时，会产生此交易评价消息。
 * 当评价完成后，修改评价为匿名评价，会产生交易变更(TradeChanged)消息。 当评价完成后，修改差、中评及进行评价解释，不会产生任何消息。 当评价完成后，删除评价不会产生此消息，只会产生交易变更(TradeChanged)消息。
 * 
 * 13、交易备注修改TradeMemoModified：在交易创建后，买家或者卖家修改交易备注
 * 
 * 14、修改交易收货地址TradeLogisticsAddressChanged：发货前卖家修改交易的收货地址（目前没有此消息发出）。
 * 当通过API修改物流地址和页面修改物流地址时，只会产生交易变更(TradeChanged)消息，而不会产生交易地址变更消息。
 * 
 * 15、修改订单信息（SKU等）TradeChanged：买家未付款之前，卖家修改sku等信息 当买家付完款后，卖家通过页面修改收货地址时，会产生交易变更消息。如下图：
 * 
 * http://open.taobao.com/doc/detail.htm?spm=0.0.0.0.sn0QdH&id=101200
 * 
 * @author zrb
 * 
 */
@Every("5s")
public class TradeRateWritter extends Job {
    private static final Logger log = LoggerFactory.getLogger(TradeRateWritter.class);

    public static final String TAG = "TradeRateWritter";

    public static final int BATCH_SIZE = 1024;

    public static final Queue<TradeRateListUpdate> tradeRateListToWritten = new ConcurrentLinkedQueue<TradeRateListUpdate>();

    public static String statusMessage;

    public static PYFutureTaskPool<Void> pool = new PYFutureTaskPool<Void>(16);

    public static void addTradeRateList(Long userId, Long ts, List<TradeRate> tradeRateList) {
        if (!TMConfigs.App.IS_TRADE_ALLOW) {
            return;
        }

        if (CommonUtils.isEmpty(tradeRateList)) {
            log.info("[TradeRateWritter] tradeRateList empty!!!  userId=" + userId);
            return;
        }

        while (tradeRateListToWritten.size() > 2048) {
            log.info("[TradeRateWritter] wait to add! tradeListToWritten size : " + tradeRateListToWritten.size()
                    + " userId=" + userId);
            CommonUtils.sleepQuietly(500L);
        }

        TradeRateListUpdate tradeRateListUpdate = new TradeRateListUpdate(userId, ts, tradeRateList);

        tradeRateListToWritten.add(tradeRateListUpdate);
    }

    @Override
    public void doJob() {
        if (!TMConfigs.App.IS_TRADE_ALLOW) {
            return;
        }

        Thread.currentThread().setName(TAG);
        log.info("Do Job for Trade Rate Writter, queue size: " + tradeRateListToWritten.size());

        TradeRateListUpdate tradeRateListUpdate = null;

        while ((tradeRateListUpdate = tradeRateListToWritten.poll()) != null) {
            doInsert(tradeRateListUpdate);
        }
    }

    public void doInsert(TradeRateListUpdate tradeRateListUpdate) {

        final Long userId = tradeRateListUpdate.userId;

        List<TradeRate> tradeRateList = tradeRateListUpdate.tradeRateList;
        if (CommonUtils.isEmpty(tradeRateList)) {
            log.info("[TradeRateWritter] empty list! userId:" + userId + " ts:" + tradeRateListUpdate.ts);
            return;
        }
        statusMessage = "Do for TradeRate userId:" + userId + " ts:" + tradeRateListUpdate.ts + " with size:"
                + tradeRateList.size();

//        log.info(statusMessage);

        for (TradeRate tradeRate : tradeRateList) {
            try {
                if ("good".equals(tradeRate.getResult())) {
                    // 略过好评数据，不存储
                    continue;
                }
//                if(Items.VIP_USER_ID.contains(userId)) {
//                	continue;
//                }
                log.info("~~~~~MessageDealer更新已有的评价~~~~~tradeRatePlay with tid = " + tradeRate.getTid());
                TradeRatePlay tradeRatePlay = TradeRatePlayDao.findByUserIOid(userId, tradeRate.getOid());
                if (tradeRatePlay == null) {
                    new TradeRatePlay(tradeRate, userId).rawInsert();

                    if ("buyer".equals(tradeRate.getRole())) {
                        // 用于检查买家中差评处理
                        pool.submit(new TradeRateMsgCommentDealer(userId, tradeRate));
//                        User user = UserDao.findById(userId);
//                        TradeRateMsgDealer.dealWithBadComment(user, tradeRate);
//                        TradeRateMsgDealer.checkGoodCommentNow(user, tradeRate);
                    }
                } else {
                    Boolean toWriteDB = tradeRatePlay.putTradeRate(tradeRate);
                    log.warn("[TradeRateWritter] putTradeRate is true for tid = " + tradeRate.getTid() + " and sellerNick = " + tradeRate.getRatedNick() + " and result = " + tradeRate.getResult());
                    if (toWriteDB == Boolean.TRUE) {
                        tradeRatePlay.rawUpdate();
                    }
                }

                if ("seller".equals(tradeRate.getRole())) {
                    OrderDisplayDao.updateSellerRate(userId, tradeRate.getOid(), true);
                } else {
                    OrderDisplayDao.updateBuyerRate(userId, tradeRate.getOid(), true);
                }
                tradeRate = null;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        // TradeRateStatusSetting.getInstance().setDbDone(tradeRateListUpdate.userId, tradeRateListUpdate.ts);

        // TradeRateUpdateTs.updateLastTradeRateModifedTime(tradeRateListUpdate.userId, tradeRateListUpdate.ts);

        statusMessage = "Finishing userId:" + userId + " ts:" + tradeRateListUpdate.ts + " with size:"
                + tradeRateList.size();
//        log.info(statusMessage);
        tradeRateListUpdate.tradeRateList.clear();
    }

    public static class TradeRateListUpdate {

        public Long userId;

        public Long ts;

        public List<TradeRate> tradeRateList;

        public TradeRateListUpdate(Long userId, Long ts, List<TradeRate> tradeRateList) {
            super();
            this.userId = userId;
            this.ts = ts;
            this.tradeRateList = tradeRateList;
            this.tradeRateList = new ArrayList<TradeRate>(tradeRateList);
        }
    }

    public static class TradeRateMsgCommentDealer implements Callable<Void> {

        Long userId;

        TradeRate tradeRate;

        public TradeRateMsgCommentDealer(Long userId, TradeRate tradeRate) {
            super();
            this.userId = userId;
            this.tradeRate = tradeRate;
        }

        @Override
        public Void call() throws Exception {
            try {
                User user = UserDao.findById(userId);
                TradeRateMsgDealer.dealWithBadComment(user, tradeRate);

                // 未评价前，看不到别人是好评还是差评
//                TradeRateMsgDealer.checkGoodCommentNow(user, tradeRate);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return null;
        }

    }
}
