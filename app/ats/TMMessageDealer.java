
package ats;

import static models.defense.BlackListBuyer.RemarkMsg.AUTO_ADD_BLACKLIST_REFUND;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import job.CommentMessages;
import job.CommentMessages.CommenMsg;
import job.ItemUpdateMessages;
import job.ItemUpdateMessages.UpdateMsg;
import job.defense.TradeMsgDealerJob;
import job.defense.TradeRateMsgDealer;
import job.message.AddItemJob;
import job.showwindow.ShowWindowExecutor;
import models.comment.CommentConf;
import models.defense.BlackListBuyer;
import models.item.ItemPlay;
import models.showwindow.OnWindowItemCache;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import cache.UserHasTradeItemCache;
import configs.TMConfigs;
import controllers.APIConfig;
import dao.UserDao;
import dao.item.ItemDao;

/**
 * changed_fields    title,price 商品此次操作所变更的字段，以“,”分割，对应于商品Item的字段名称。
 * 目前支持title，price，num，item_img，prop_img，location，cid，approve_status， list_time几个字段的更改标记返回，其中“item_img，prop_img”会同时出现表示商品相关图片列表发生了修改
 * @author zrb
 *
 */
public abstract class TMMessageDealer {

    private static final Logger log = LoggerFactory.getLogger(TMMessageDealer.class);

    public static final String TAG = "TMMsgDealer";

    String tmcField = StringUtils.EMPTY;

    String atsStatus = StringUtils.EMPTY;

    public String getTmcField() {
        return tmcField;
    }

    public void setTmcField(String tmcField) {
        this.tmcField = tmcField;
    }

    public String getAtsStatus() {
        return atsStatus;
    }

    public void setAtsStatus(String atsStatus) {
        this.atsStatus = atsStatus;
    }

    @Override
    public String toString() {
        return "TMMsgDealer [tmcField=" + tmcField + ", atsStatus=" + atsStatus + "]";
    }

    public TMMessageDealer(String tmcField, String atsStatus) {
        super();
        this.tmcField = tmcField;
        this.atsStatus = atsStatus;
    }

    public void doForTmc(Long userId, JSONObject content) throws JSONException {

    }

    public long getUserId(JSONObject content) throws JSONException {
        return content.getLong("user_id");
    }

    public long getNumIid(JSONObject content) throws JSONException {
        return content.getLong("num_iid");
    }

    public String getChangeFields(JSONObject content) throws JSONException {
        return content.getString("changed_fields");
    }

    public static TMMessageDealer itemAddDealer = new TMMessageDealer("taobao_item_ItemAdd", "ItemAdd") {
        @Override
        public void doForTmc(Long userId, JSONObject content) throws JSONException {

//            long numIid = getNumIid(content);
            content.put("user_id", userId);
            AddItemJob.addMsg(content.toString());
        }
    };

    static class TMMsgItemRemover extends TMMessageDealer {

        public TMMsgItemRemover(String tmcField, String atsStatus) {
            super(tmcField, atsStatus);
        }

        @Override
        public void doForTmc(Long userId, JSONObject content) throws JSONException {
            long numIid = getNumIid(content);
            boolean isSuccess = false;
            ItemPlay itemPlay = ItemDao.findByNumIid(userId, numIid);
            if (itemPlay != null) {
                isSuccess = itemPlay.rawDelete();
            }
        }
    }

    public static TMMessageDealer itemCommonRemover = new TMMsgItemRemover("taobao_item_ItemDelete", "ItemDelete") {
    };

    public static TMMessageDealer itemPunishRemover = new TMMsgItemRemover("taobao_item_ItemPunishDelete",
            "ItemPunishDelete") {
    };

    public static TMMessageDealer itemUpshelfDealer = new TMMessageDealer("taobao_item_ItemUpshelf", "ItemUpshelf") {

        @Override
        public void doForTmc(Long userId, JSONObject content) throws JSONException {
            // TODO add to item update...
            long numIid = getNumIid(content);
            ItemPlay item = ItemDao.findByNumIid(userId, numIid);
            if (item == null) {
                // TODO try fetch item...
            } else {
                item.setStatus(ItemPlay.Status.ONSALE);
            }

        }
    };

    public static TMMessageDealer itemDownshelfDealer = new TMMessageDealer("taobao_item_ItemDownshelf",
            "ItemDownshelf") {
        @Override
        public void doForTmc(final Long userId, final JSONObject content) throws JSONException {
            TMConfigs.getShowwindowPool().submit(new Callable<ItemPlay>() {

                @Override
                public ItemPlay call() throws Exception {
                    // TODO add to item update...
                    long numIid = getNumIid(content);
                    ItemPlay item = ItemDao.findByNumIid(userId, numIid);
                    if (item == null) {
                        // TODO try fetch item...
                    } else {
                        item.setStatus(ItemPlay.Status.INSTOCK);
                    }
                    User user = UserDao.findById(userId);
                    if (user == null) {
                        return null;
                    }
                    UserHasTradeItemCache.removeForChange(user, numIid);
                    if (user.isShowWindowOn()) {
                        OnWindowItemCache.get().refresh(user);
                        new ShowWindowExecutor(user).doJob();
                    }

                    // TODO perhaps there's more problem..

                    // 自动下架没有时间安排
//                    WindowsService.addLightWeightInstant(userId);
                    return null;
                }
            });

        }
    };

    public static TMMessageDealer itemRecommendDeleteDealer = new TMMessageDealer("taobao_item_ItemRecommendDelete",
            "ItemRecommendDelete") {
        @Override
        public void doForTmc(Long userId, JSONObject content) throws JSONException {
            // TODO add to item update...
            long numIid = getNumIid(content);
//
////            ItemDownShelfCache.setCache(userId, numIid);
//            if (CheckNoDownShelfJob.isRecentCanceled(numIid)) {
//                return;
//            }
//            WindowsService.addLightWeightInstant(userId);
        }
    };

    /**
     * Perhaps not ItemChanged
     * {
    "user_id": 883264761,
    "user_nick": "emp海棠",
    "content": "{"nick":"emp海棠","num":660,"sku_num":30,"changed_fields":"desc,
    num,
    sku","num_iid":37157280239,"sku_id":40185694152}",
    "id": 1191400053221183737,
    "pub_time": "2014-01-26 19:58:45",
    "pub_app_key": "12497914",
    "topic": "taobao_item_ItemUpdate"
    }
     */
    public static TMMessageDealer itemUpdateDealer = new TMMessageDealer("taobao_item_ItemUpdate", "ItemUpdate") {

        @Override
        public void doForTmc(Long userId, JSONObject content) throws JSONException {
            ItemUpdateMessages.addMsg(new UpdateMsg(userId, content.toString()));
        }
    };

    //public static int count = 0;
    //public static String tmpTradeSuccessContent = "{\"buyer_nick\":\"df0124\",\"topic\":\"trade\",\"payment\":\"148.00\",\"nick\":\"gxhc258\",\"status\":\"TradeSuccess\",\"oid\":534012597072256,\"is_3D\":true,\"user_id\":153953964,\"tid\":534012597072256,\"type\":\"guarantee_trade\",\"seller_nick\":\"gxhc258\",\"modified\":\"2014-02-22 11:28:14\"}";
    public static String autoCommentTidCachePre = "autoCommentTidCachePre_";

    public static TMMessageDealer tradeSuccesDealer = new TMMessageDealer("taobao_trade_TradeSuccess", "TradeSuccess") {
        @Override
        public void doForTmc(Long userId, JSONObject content) throws JSONException {
//        	log.info(content.toString());
            String tid = content.getString("tid");
            // 说明是子订单
            if (Cache.get(autoCommentTidCachePre + tid) != null) {
//                log.info("这是子订单，已经评价过了，跳出");
                return;
            }
            // 说明是主订单
            Cache.set(autoCommentTidCachePre + tid, 1, "4h");
            //count++;log.info(count + "");
            content.put("user_id", userId);
            
            // 淘掌柜也增加了抢评功能
            CommentConf conf = CommentConf.findByUserId(userId);
            if (conf != null && conf.getCommentType() > 0) {
            	//log.warn("[defender] not comment now~! userId=" + userId + "  msg: " + content);
                return;
            }
            CommentMessages.addMsg(new CommenMsg(1, content.toString()));
        }
    };

    /**
     * 差评师专用
     */
    public static TMMessageDealer tradeCreateDealer = new TMMessageDealer("taobao_trade_TradeCreate", "TradeCreate") {
        @Override
        public void doForTmc(Long userId, JSONObject content) throws JSONException {
            content.put("user_id", userId);
            TradeMsgDealerJob.addTradeMsg(content.toString());
        }
    };

    /**
     * 差评师专用
     */
    public static TMMessageDealer tradeRatedDealer = new TMMessageDealer("taobao_trade_TradeRated", "TradeRated") {

        @Override
        public void doForTmc(Long userId, JSONObject content) throws JSONException {
            long oid = Long.parseLong(content.getString("oid"));
            long tid = Long.parseLong(content.getString("tid"));
            TradeRateMsgDealer.addMsg(userId, tid, oid, content);
        }
    };

    /**
     * 差评师专用
     */
    public static TMMessageDealer refundSuccessDealer = new TMMessageDealer("tmall_refund_RefundSucceed",
            "RefundSuccess") {

        @Override
        public void doForTmc(Long userId, JSONObject content) throws JSONException {
            User user = UserDao.findById(userId);
            if (user == null) {
                return;
            }

            // 黑名单设置：退款用户加入黑名单
            if (user.isAutoRefundBlackListOn() == true) {
                String buyerNick = content.getString("buyer_nick");
                long ts = System.currentTimeMillis();
                new BlackListBuyer(userId, buyerNick, ts, AUTO_ADD_BLACKLIST_REFUND).jdbcSave();
            }
        }
    };

    static Map<String, TMMessageDealer> tmcTopicEntry = new HashMap<String, TMMessageDealer>();

    static Map<String, TMMessageDealer> atsStatusEntry = new HashMap<String, TMMessageDealer>();

    static TMMessageDealer[] dealers = new TMMessageDealer[] {
            itemAddDealer, itemCommonRemover, itemPunishRemover, itemUpshelfDealer, itemDownshelfDealer,
            itemRecommendDeleteDealer, itemUpdateDealer, tradeSuccesDealer, tradeCreateDealer, tradeRatedDealer,
            refundSuccessDealer
    };
    static {
        for (TMMessageDealer dealer : dealers) {
            tmcTopicEntry.put(dealer.getTmcField(), dealer);
            atsStatusEntry.put(dealer.getAtsStatus(), dealer);
        }
    }

    public static TMMessageDealer matchTmcDealer(String topic) {
        return tmcTopicEntry.get(topic);
    }

    public static TMMessageDealer matchAtsDealer(String status) {
        return atsStatusEntry.get(status);
    }

}
