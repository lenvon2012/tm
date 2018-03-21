
package job;

import java.util.concurrent.Callable;

import job.CommentMessages.CommenMsg;
import job.defense.TradeMsgDealerJob;
import job.defense.TradeRateMsgDealer;
import job.message.AddItemJob;
import job.message.DeleteItemJob;
import models.comment.CommentConf;
import models.defense.BlackListBuyer;
import models.item.ItemPlay;
import models.user.User;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ats.TMMessageDealer;
import cache.ItemDownShelfCache;

import com.taobao.api.domain.TmcMessage;
import com.taobao.api.internal.tmc.Message;

import configs.TMConfigs;
import configs.TMConfigs.ShowWindowParams;
import controllers.APIConfig;
import dao.UserDao;

public class MessagesDealer {
    private static final Logger log = LoggerFactory.getLogger(MessagesDealer.class);

    private static final String TAG = "MessagesDealer";

    public static final void onReceiveMessage(String msg) {
//        log.info("[receive msg:]" + msg);
//        if (true) {
//            return ;
//        }

        try {
            JSONObject obj = new JSONObject(msg);
            JSONObject target = null;
//          log.info(obj.toString());
            if (obj.has("notify_trade")) {
                target = obj.getJSONObject("notify_trade");
            } else if (obj.has("notify_item")) {
                target = obj.getJSONObject("notify_item");
            } else if (obj.has("notify_refund")) {
                target = obj.getJSONObject("notify_refund");
            }
            if (target == null) {
                return;
            }

            String status = target.getString("status");
//          log.info(status);
            TMMessageDealer dealer = TMMessageDealer.matchAtsDealer(status);
//            log.info(" find dealer for msg [" + msg + "] with dealer :" + dealer);
            if (dealer == null) {
                return;
            }
            dealer.doForTmc(target.getLong("user_id"), target);

        } catch (JSONException e) {
            log.error("msg :" + msg);
            log.warn(e.getMessage(), e);
        }

    }

    /**
     * This is the old way deal with the msg..
     * @param msg
     * @param obj
     * @throws JSONException
     */
    private static void dealWithEachMsgType(String msg, JSONObject obj) throws JSONException {
        if (obj.has("notify_trade")) {
            JSONObject notifyTrade = obj.getJSONObject("notify_trade");
            dealWithTradeMsg(msg, notifyTrade);
        } else if (obj.has("notify_item")) {
            JSONObject notify_item = obj.getJSONObject("notify_item");
            dealWithItemMsg(msg, notify_item);
        } else if (obj.has("notify_refund")) {
            JSONObject notify_refund = obj.getJSONObject("notify_refund");
            dealWithRefundMsg(notify_refund);
        }
    }

    private static void dealWithItemMsg(String msg, JSONObject notify_item) throws JSONException {
        //log.info("[msg ]:" + msg);
        String statusString = notify_item.getString("status");

        if (statusString.equals("ItemRecommendDelete")) {

            long userId = notify_item.getLong("user_id");
            long numIid = notify_item.getLong("num_iid");

//                    log.info("[do for nofity item;]" + notify_item);

            ItemDownShelfCache.setCache(userId, numIid);
//            WindowsService.addLightWeightInstant(userId);
//                    LightWeightQueueJob.add(userId);

        } else if (statusString.equals("ItemDownshelf") || statusString.equals("ItemPunishDownshelf")) {

//                    log.info("[do for nofity item;]" + notify_item);

            if (ShowWindowParams.enableItemShelfDownMesssage) {
                long userId = notify_item.getLong("user_id");
                long numIid = notify_item.getLong("num_iid");
//                WindowsService.addLightWeightInstant(userId);

            }

        } else if (statusString.equals("ItemDelete")) {
            DeleteItemJob.addAtsMsg(msg);
        } else if (statusString.equals("ItemAdd")) {
            /*
             * {"notify_item":{"topic":"item","status":"ItemAdd","user_id":412536637,"nick":"张顺罗","modified":"2013-11-16 17:26:19","num":100,"title":"思密达韩国代购正品2013冬季新款绅士兔可爱蝴蝶结针织衫","price":"340.00","num_iid":36091123181}} 
             */
            AddItemJob.addMsg(msg);
        }
    }

    private static void dealWithTradeMsg(String msg, JSONObject notifyTrade) throws JSONException {
        String status = notifyTrade.getString("status");
        long userId = notifyTrade.getLong("user_id");

        if (APIConfig.get().getApp() == APIConfig.defender.getApp()) {
            if ("TradeSuccess".equals(status)) {
                CommentConf conf = CommentConf.findByUserId(userId);
                if (conf != null && conf.getCommentType() > 0) {
                    log.warn("[defender] not comment now~! userId=" + userId + "  msg: " + msg);
                    return;
                }
                CommentMessages.addMsg(new CommenMsg(1, msg));

            } else if ("TradeCreate".equals(status)) { // || "TradeAlipayCreate".equals(status)
                // 交易创建，trade处理，差评师拦截等
                TradeMsgDealerJob.addTradeMsg(msg);

            } else if ("TradeRated".equals(status) || "TradeChanged".equals(status)) {
                /**
                 * 
                12、交易评价变更TradeRated：交易的商品或店铺被评价时产生此消息。通过页面完成评价商品和评价店铺动态评分后，会产生此消息。
                当只评价商品或只评价店铺动态评分不会产生此消息，只会产生交易变更消息（TradeChanged），此可以进入页面中继续评价另一项。
                当追加评价时，会产生此交易评价消息。
                当评价完成后，修改评价为匿名评价，会产生交易变更(TradeChanged)消息。
                当评价完成后，修改差、中评及进行评价解释，不会产生任何消息。
                当评价完成后，删除评价不会产生此消息，只会产生交易变更(TradeChanged)消息。

                13、交易备注修改TradeMemoModified：在交易创建后，买家或者卖家修改交易备注

                14、修改交易收货地址TradeLogisticsAddressChanged：发货前卖家修改交易的收货地址（目前没有此消息发出）。
                当通过API修改物流地址和页面修改物流地址时，只会产生交易变更(TradeChanged)消息，而不会产生交易地址变更消息。

                15、修改订单信息（SKU等）TradeChanged：买家未付款之前，卖家修改sku等信息
                当买家付完款后，卖家通过页面修改收货地址时，会产生交易变更消息。如下图：
                 */
                /**
                 *  {"buyer_nick":"小奥_1120","topic":"trade","payment":"0.79",
                 *  "nick":"80165411liang",
                 *  "status":"TradeRated",
                 *  "oid":228644414317267,"is_3D":true,
                 *  "user_id":445827855,
                 *  "tid":228644414317267,
                 *  "type":"guarantee_trade",
                 *  "seller_nick":"80165411liang",
                 *  "modified":"2013-06-18 01:16:14"
                 *  }
                 */
                long oid = Long.parseLong(notifyTrade.getString("oid"));
                long tid = Long.parseLong(notifyTrade.getString("tid"));
//                long userId = notifyTrade.getLong("user_id");

                if ("TradeRated".equals(status)) {
                    // 评价变更TradeRated，更新评价信息
                    TradeRateMsgDealer.addMsg(userId, tid, oid, notifyTrade);
                }
//                else if ("TradeChanged".equals(status)) {
//                    if (userId == 1039626382L) { // 楚之小南 测试
//                        User user = UserDao.findById(userId);
//                        List<TradeRate> rates = new TraderatesGet(user, tid).call();
//                        if (CommonUtils.isEmpty(rates)) {
//                            log.error(">>>>>>no rate find!>>>> " + notifyTrade);
//                        }
//                        for (TradeRate tradeRate : rates) {
//                            log.error(">>>>>Rate Detail >>> " + tradeRate.getRole() + " >> " + tradeRate);
//                        }
//                    }
//                }
            }
        } else {
            if ("TradeSuccess".equals(status)) {
                CommentMessages.addMsg(new CommenMsg(1, msg));
            }
        }

    }

    private static void dealWithRefundMsg(JSONObject notify_refund) {
        if (APIConfig.get().getApp() != APIConfig.defender.getApp()) {
            return;
        }
        try {
            if ("RefundSuccess".equals(notify_refund.getString("status"))) {
                long userId = notify_refund.getLong("user_id");
                User user = UserDao.findById(userId);

                // 黑名单设置：退款用户加入黑名单
                if (user.isAutoRefundBlackListOn() == true) {
                    new BlackListBuyer(userId, notify_refund.getString("buyer_nick"), System.currentTimeMillis(),
                            BlackListBuyer.RemarkMsg.AUTO_ADD_BLACKLIST_REFUND).jdbcSave();
                }
            }
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * {"tmc_messages_consume_response":{"messages":{"tmc_message":[{"user_id":1736568971,"user_nick":"许嵩后院会","content":"{"buyer_nick":"但愿我天真","payment":"15.70","oid":519847211351083,"tid":519847211351083,"type":"guarantee_trade","seller_nick":"许嵩后院会"}","id":1172200053212681005,"pub_time":"2014-01-26 17:39:41","pub_app_key":"12497914","topic":"taobao_trade_TradeRated"},{"user_id":1831206202,"user_nick":"yc61073635","content":"{"nick":"yc61073635","num_iid":36007760723}","id":1172200053212681006,"pub_time":"2014-01-26 17:39:40","pub_app_key":"12497914","topic":"taobao_item_ItemRecommendAdd"},{"user_id":1826606929,"user_nick":"huakaimang","content":"{"nick":"huakaimang","changed_fields":"","num_iid":36614380272}","id":1172200053212681007,"pub_time":"2014-01-26 17:39:40","pub_app_key":"12497914","topic":"taobao_item_ItemUpdate"},{"user_id":1826606929,"user_nick":"huakaimang","content":"{"nick":"huakaimang","changed_fields":"","num_iid":36614092702}","id":1172200053212681008,"pub_time":"2014-01-26 17:39:40","pub_app_key":"12497914","topic":"taobao_item_ItemUpdate"},{"user_id":398656990,"user_nick":"feilongxiong","content":"{"buyer_nick":"yoyobbq","payment":"57.82","oid":525920279704970,"tid":525920279704970,"type":"guarantee_trade","seller_nick":"feilongxiong"}","id":1172200053212681009,"pub_time":"2014-01-26 17:39:41","pub_app_key":"12497914","topic":"taobao_trade_TradeAlipayCreate"},{"user_id":191708679,"user_nick":"zzp夜明珠","content":"{"nick":"zzp夜明珠","num_iid":19730334579}","id":1172200053212681010,"pub_time":"2014-01-26 17:39:42","pub_app_key":"12497914","topic":"taobao_item_ItemRecommendAdd"},{"user_id":191708679,"user_nick":"zzp夜明珠","content":"{"nick":"zzp夜明珠","num_iid":26591320155}","id":1172200053212681011,"pub_time":"2014-01-26 17:39:43","pub_app_key":"12497914","topic":"taobao_item_ItemRecommendAdd"},{"user_id":191708679,"user_nick":"zzp夜明珠","content":"{"nick":"zzp夜明珠","num_iid":26621840166}","id":1172200053212681012,"pub_time":"2014-01-26 17:39:43","pub_app_key":"12497914","topic":"taobao_item_ItemRecommendAdd"},{"user_id":1685281213,"user_nick":"胖胖龙旗舰店","content":"{"buyer_nick":"杨晨尉","payment":"15.50","oid":526135691251898,"tid":526135691251898,"type":"guarantee_trade","seller_nick":"胖胖龙旗舰店"}","id":1172200053212681013,"pub_time":"2014-01-26 17:39:44","pub_app_key":"12497914","topic":"taobao_trade_TradeAlipayCreate"},{"user_id":1068244295,"user_nick":"乐途票务酒店网","content":"{"nick":"乐途票务酒店网","num":138,"changed_fields":"desc,num","num_iid":36552905512}","id":1172200053212681014,"pub_time":"2014-01-26 17:39:43","pub_app_key":"12497914","topic":"taobao_item_ItemUpdate"}]}}}
     * essages": {
      "tmc_message": [
        {
          "user_id": 1112757170,
          "user_nick": "伟利鞋店",
          "content": "{"nick":"伟利鞋店","num_iid":18189406618}",
          "id": 1191300053224478050,
          "pub_time": "2014-01-26 19:28:50",
          "pub_app_key": "12497914",
          "topic": "taobao_item_ItemRecommendAdd"
        },
        {
          "user_id": 833230312,
          "user_nick": "轩美舞台灯光",
          "content": "{"nick":"轩美舞台灯光","num_iid":19085499724}",
          "id": 1191300053224478051,
          "pub_time": "2014-01-26 19:28:51",
          "pub_app_key": "12497914",
          "topic": "taobao_item_ItemRecommendAdd"
        },
        {
          "user_id": 363569567,
          "user_nick": "hailiang1003",
          "content": "{"buyer_nick":"李润成金娜娜","payment":"8.00","oid":526181528918320,"tid":526181528918320,"type":"guarantee_trade","seller_nick":"hailiang1003"}",
          "id": 1191300053224478052,
          "pub_time": "2014-01-26 19:28:51",
          "pub_app_key": "12497914",
          "topic": "taobao_trade_TradeChanged"
        },
     */
    public static void tmcReceiveMsg() {
    }

    public static void onReceiveTmcMessage(Message msg) {
        onReceiveTmcMessage(msg.getUserId(), msg.getTopic(), msg.getContent());
    }

    public static void onReceiveTmcMessage(TmcMessage msg) {
        onReceiveTmcMessage(msg.getUserId(), msg.getTopic(), msg.getContent());
    }

    /**
            "taobao_trade_TradeSuccess",
            "taobao_trade_TradeTimeoutRemind",
            "taobao_trade_TradeRated",
            "taobao_trade_TradeMemoModified",
            "taobao_trade_TradeLogisticsAddressChanged",
            "taobao_trade_TradeChanged",
            "taobao_trade_TradeAlipayCreate",
            "taobao_item_ItemAdd",
            "taobao_item_ItemUpshelf",
            "taobao_item_ItemDownshelf",
            "taobao_item_ItemDelete",
            "taobao_item_ItemUpdate",
            "taobao_item_ItemRecommendAdd",
            "taobao_item_ItemZeroStock",
            "taobao_item_ItemPunishDelete"
     * @param msg
     */
    public static void onReceiveTmcMessage(final Long userId, final String topic, final String content) {
        // TODO Auto-generated method stub
//        String topic = msg.getTopic();
//        final String content = msg.getContent();
//        final long userId = msg.getUserId();

        final TMMessageDealer dealer = TMMessageDealer.matchTmcDealer(topic);
//        if (APIConfig.get().getApp() != APIConfig.taobiaoti.getApp()) {
//            log.info("[match dealer :]" + dealer + "  for msg :" + topic);
//        }

        if (dealer == null) {
            return;
        }
//        if(dealer == TMMessageDealer.tradeRatedDealer){
//            log.info(" on receive " + topic + ":" + content);
//        }

        TMConfigs.getShowwindowPool().submit(new Callable<ItemPlay>() {
            @Override
            public ItemPlay call() throws Exception {
                try {
                    dealer.doForTmc(userId, new JSONObject(content));
                } catch (JSONException e) {
                    log.warn(e.getMessage(), e);
                }
                return null;
            }
        });

    }
}
