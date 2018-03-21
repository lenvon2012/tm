package job.defense;

import static java.lang.String.format;

import java.io.Serializable;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import jdp.JdpModel.JdpTradeModel;
import job.writter.TradeRateWritter;
import models.comment.CommentConf;
import models.defense.BlackListBuyer;
import models.defense.TMCMsgLog;
import models.order.OrderDisplay;
import models.sms.SmsSendLog;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.common.util.concurrent.jsr166y.ConcurrentLinkedDeque;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.jobs.Every;
import play.jobs.Job;
import titleDiag.DiagResult;
import utils.TaobaoUtil;
import bustbapi.TradeRateApi.TraderatesGet;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.google.gson.Gson;
import com.taobao.api.ApiException;
import com.taobao.api.SecretException;
import com.taobao.api.domain.Order;
import com.taobao.api.domain.Trade;
import com.taobao.api.domain.TradeRate;

import configs.TMConfigs;
import controllers.APIConfig;
import controllers.TmSecurity;
import controllers.SkinComment.TradeApiUpdateCaller;
import controllers.TmSecurity.SecurityType;
import dao.UserDao;
import dao.trade.OrderDisplayDao;

@Every("15s")
public class TradeRateMsgDealer extends Job {

    private static final Logger log = LoggerFactory.getLogger(TradeRateMsgDealer.class);

    public static final String TAG = "TradeRateMsgDealer";

    public static boolean IS_ALLOW_TMC_RATED = true;

    public static class TradeRateMsg implements Serializable {

        private static long serialVersionUID = 1L;

        Long userId;

        Long tid;

        Long oid;

        JSONObject content;

        @Override
        public String toString() {
            return "TradeRateMsg [userId=" + userId + ", tid=" + tid + ", oid=" + oid + ", content=" + content + "]";
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getTid() {
            return tid;
        }

        public void setTid(Long tid) {
            this.tid = tid;
        }

        public Long getOid() {
            return oid;
        }

        public void setOid(Long oid) {
            this.oid = oid;
        }

        public TradeRateMsg(Long userId, Long tid, Long oid, JSONObject content) {
            super();
            this.userId = userId;
            this.tid = tid;
            this.oid = oid;
            this.content = content;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((oid == null) ? 0 : oid.hashCode());
            result = prime * result + ((tid == null) ? 0 : tid.hashCode());
            result = prime * result + ((userId == null) ? 0 : userId.hashCode());
            return result;
        }

        public JSONObject getContent() {
            return content;
        }

        public void setContent(JSONObject content) {
            this.content = content;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TradeRateMsg other = (TradeRateMsg) obj;
            if (oid == null) {
                if (other.oid != null)
                    return false;
            } else if (!oid.equals(other.oid))
                return false;
            if (tid == null) {
                if (other.tid != null)
                    return false;
            } else if (!tid.equals(other.tid))
                return false;
            if (userId == null) {
                if (other.userId != null)
                    return false;
            } else if (!userId.equals(other.userId))
                return false;
            return true;
        }

    }

    static Queue<TradeRateMsg> queue = new ConcurrentLinkedDeque<TradeRateMsg>();

    public static void addMsg(Long userId, Long tid, Long oid, JSONObject notifyTrade) {
        if (TMConfigs.App.IS_TRADE_ALLOW && IS_ALLOW_TMC_RATED) {
            // log.info(format("addMsg:userId, tid, oid".replaceAll(", ", "=%s, ") + "=%s", userId, tid, oid));
            TradeRateMsg tradeRateMsg = new TradeRateMsg(userId, tid, oid, notifyTrade);
//            new TMCMsgLog(tradeRateMsg).jdbcSave();
            queue.add(tradeRateMsg);
        }
    }

    public void doJob() {
        TradeRateMsg msg = null;
        while ((msg = queue.poll()) != null) {
            if (!APIConfig.get().enableSyncTradeRate()) {
                continue;
            }
            TMConfigs.getDiagResultPool().submit(new TradeRateMsgCaller(msg));
        }
    }
    

    private static class TradeRateMsgCaller implements Callable<DiagResult> {
        private TradeRateMsg msg;
        
        public TradeRateMsgCaller(TradeRateMsg msg) {
            super();
            this.msg = msg;
        }

        @Override
        public DiagResult call() throws Exception {
            try {
                doForMsg();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }

            return null;
        }
        
        private void doForMsg() throws JSONException {
            User user = UserDao.findById(msg.getUserId());
            if (user == null || !user.isVaild()) {
                return;
            }

            // Long numIid = new GetNumIidOfTradeOid(user.getSessionKey(), msg.getTid(), msg.getOid()).call();
            // log.info("[get numiid :]" + numIid);

            /**
             * rater    评价者：枚举buyer,seller,unknown  基本类型    String  是 
             */
            Trade trade = JdpTradeModel.fetchTrade(msg.getTid());
            if (trade == null) {
                log.warn(" no tid in jdp :" + msg);
                return;
            }
            List<Order> orders = trade.getOrders();
            if (CommonUtils.isEmpty(orders)) {
                log.warn(" no tid orders  in jdp :" + msg);
                return;
            }

            Order order = null;

            for (Order o : orders) {
                if (o.getOid().longValue() == msg.oid.longValue()) {
                    order = o;
                }
            }
            boolean isWorkToDo = false;
            if (order == null) {
                log.warn("no order for msg:" + msg + " with trade :" + trade + " for ");
                return;
            } else if (order.getBuyerRate() == null) {
                log.warn("no buyer rate for msg:" + msg + " with trade :" + order);
            } else if (order.getSellerRate() == null) {
                log.warn("no seller rate for msg:" + msg + " with trade :" + order);
            }

            String rater = msg.getContent().getString("rater");
            boolean isBuyerRate = "buyer".equals(rater);
            boolean isSellerRate = "seller".equals(rater);
            if (order != null && (order.getBuyerRate() || isBuyerRate) && (order.getSellerRate() || isSellerRate)) {
                isWorkToDo = true;
//            } else if (trade.getBuyerRate() && trade.getSellerRate()) {
//                isWorkToDo = true;
            } else {
                //
            }

            if (!isWorkToDo) {
                log.warn(" no work to do for :\n" + msg + "\n" + trade);
                return;
            }

//            if (targetOrder == null) {
//                log.warn(" no target oid  in jdp :" + msg);
//                return;
//            }
//            if (targetOrder.getBuyerRate() == null) {
//                log.warn(" no target buyer rate  in jdp :" + msg);
//                return;
//            }
//            if (targetOrder.getSellerRate()== null) {
//                log.warn(" no target seller rate  in jdp :" + msg);
//                return;
//            }
//
//            if (!targetOrder.getBuyerRate()) {
//                return;
//            }
//            if (!targetOrder.getSellerRate()) {
//                return;
//            }
            
            String key = user.getId() + "_" + msg.getTid() + "_TMCTradeRate";
            List<TradeRate> rates = (List<TradeRate>) Cache.get(key);
            if (!CommonUtils.isEmpty(rates)) {
            	return;
            }
            
            rates = new TraderatesGet(user, msg.getTid()).call();


            if (CommonUtils.isEmpty(rates)) {
                log.warn("[TradeRateMsgInt]no traderates for tid = " + msg.getTid());
                return;
            }
            
            Cache.set(key, rates, "1mn");
            log.warn("[TradeRateMsgInt]trade rate size [" + rates.size() + "]for tid = " + msg.getTid() + " and userId = " + msg.getUserId());

//            log.info("[TradeRateMsgInt] write traderates for tid = " + msg.getTid());
//            if(!targetOrder.getSellerRate()){
//            }
            long ts = System.currentTimeMillis();
            TradeRateWritter.addTradeRateList(user.getId(), ts, rates);

//            for (TradeRate rate : rates) {
//                if (rate == null) {
//                    continue;
//                }
//
//                // 检查差评处理
//                TradeRateMsgDealer.dealWithBadComment(user, rate);
//
//                // 买家好评后立即评价
//                TradeRateMsgDealer.checkGoodCommentNow(user, rate);
//            }

//            rates.clear();
        }

    }

    public static void dealWithBadComment(User user, TradeRate rate) {
        long ts = System.currentTimeMillis();
        try {
        	// 解密rate中nick字段
			rate.setNick(TmSecurity.decrypt(rate.getNick(), SecurityType.SIMPLE, user));
		} catch (SecretException e1) {
			e1.printStackTrace();
		}
        if ("buyer".equals(rate.getRole()) && !StringUtils.isEmpty(rate.getResult())
                && !"good".equals(rate.getResult())) {

            String rateStr = "bad".equals(rate.getResult()) ? "差评" : "中评";
            CommentConf conf = CommentConf.findByUserId(user.getId());
            if (conf == null) {
                conf = new CommentConf(user.getId(), user.getUserNick(), "欢迎再次光临!@#");
                conf.jdbcSave();
            }
            String msg = conf.replaceTemplate(rate.getNick(), user.getUserNick(), rateStr);

            if (rate.getCreated().getTime() > user.getFirstLoginTime() - DateUtil.DAY_MILLIS * 5L
                    && rate.getCreated().getTime() >= ts - DateUtil.DAY_MILLIS * 16L) {

                if (user.isBadCommentNoticeOff() == false && user.isSendDefenseMsgOn() == true) {
                    String content = user.getUserNick() + ": 淘宝买家" + rate.getNick() + "给您" + rateStr
                            + "，请及时登录淘宝查看，购买宝贝" + rate.getItemTitle() + "。";
                    TradeDefenseCaller.sendSmsMsg(user, content, SmsSendLog.TYPE.BADCOMMENT_NOTICE, rate.getTid());
                } else {
                    log.info("[TradeRateMsgDealer]isBadCommentNoticeOn=false, userId=" + user.getIdlong() + "  , nick="
                            + rate.getNick());
                }

                // 买家给中差评，立即短信给买家
                // 干掉 停止给买家发送任何改中差评相关短信
                if (user.isBadCommentBuyerSmsOn() == true && user.isBadCommentBuyerSmsOn() == false && user.isSendDefenseMsgOn() == true) {
                    List<OrderDisplay> orderList = OrderDisplayDao.findByUserIdTid(user.getId(), rate.getTid(), 0L);
                    String phone = StringUtils.EMPTY;
                    if (!CommonUtils.isEmpty(orderList)) {
                        OrderDisplay order = orderList.get(0);
                        try {
							phone = TmSecurity.decrypt(order.phone, SecurityType.PHONE, user);
						} catch (SecretException e) {
							e.printStackTrace();
						}
                    }
                    if (StringUtils.isEmpty(phone)) {
//                        phone = new GetSellerMobile(user.getSessionKey(), rate.getTid()).call();
                        Trade trade;
                        try {
                            trade = new TradeApiUpdateCaller(user, rate.getTid()).call();
                            if (trade != null) {
                            	try {
        							phone = TmSecurity.decrypt(trade.getReceiverMobile(), SecurityType.PHONE, user);
        						} catch (SecretException e) {
        							e.printStackTrace();
        						}
                                if (StringUtils.isEmpty(phone)) {
                                    phone = trade.getReceiverPhone();
                                }
                            }
                        } catch (Exception e) {
                            log.warn(e.getMessage(), e);

                        }

                    }
                    if (!StringUtils.isEmpty(phone)) {
                        TradeDefenseCaller.sendSmsMsg(user, msg, phone, SmsSendLog.TYPE.BADCOMMENT_BUYER_SMS,
                                rate.getTid());
                    } else {
                        log.error("[TradeRateMsgDealer]No phone found for :userId=" + user.getIdlong() + "  , nick="
                                + rate.getNick() + " , tid=" + rate.getTid());
                    }
                } else {
                    log.info("[TradeRateMsgDealer]isBadCommentBuyerSmsOn=false, userId=" + user.getIdlong()
                            + "  , nick=" + rate.getNick() + " , tid=" + rate.getTid());
                }

            } else {
                log.info("[TradeRateMsgDealer]rate create time 4days ago, userId=" + user.getIdlong() + " , nick="
                        + rate.getNick() + " , tid=" + rate.getTid());
            }

            // 差评立即回评反击
            if (conf.getCommentRate() != null && conf.getCommentRate() == 1) {
                log.warn(format("badRate COMMENT! user=%s, userId=%d, buyerNick=%s, tid=%d, oid=%d, content=[%s]",
                        user.userNick, user.getId(), rate.getNick(), rate.getTid(), rate.getOid(), msg));
                TaobaoUtil.commentNowWithReason(user.userNick, user.getId(), rate.getNick(), rate.getResult(),
                        rate.getTid(), rate.getOid(), msg);
            }

            // 黑名单设置： 差评用户直接加入黑名单
            if (user.isAutoChapingBlackListOn()) {
                new BlackListBuyer(user.getIdlong(), rate.getNick(), System.currentTimeMillis(),
                        BlackListBuyer.RemarkMsg.AUTO_ADD_BLACKLIST_CHAPING).jdbcSave();
            }
        }
        rate = null;
    }

    public static void checkGoodCommentNow(User user, TradeRate rate) {
        // 买家好评后立即评价
        if (user.isAutoCommentOn() && "buyer".equals(rate.getRole())) {
            if (StringUtils.equals(rate.getResult(), "good")) {
                CommentConf conf = CommentConf.findByUserId(user.getId());
                if (conf != null && conf.getCommentType() == 1) {
                    String content = StringUtils.EMPTY;
                    if (conf == null || StringUtils.isEmpty(conf.getCommentContent())) {
                        content = "很好的买家，欢迎下次再来！";
                    } else {
                        String[] commentArr = conf.getCommentContent().split("!@#");
                        int length = commentArr.length;
                        if (length <= 0) {
                            content = "很好的买家，欢迎下次再来！";
                        } else {
                            int offset = new Random().nextInt(length);
                            content = commentArr[offset];
                        }
                    }
                    log.warn(format(
                            "[TradeRateMsgDealer]goodRate COMMENT! user=%s, userId=%d, buyerNick=%s, tid=%d, oid=%d, content=[%s]",
                            user.userNick, user.getId(), rate.getNick(), rate.getTid(), rate.getOid(), content));
                    TaobaoUtil.commentNowWithReason(user.userNick, user.getId(), rate.getNick(), "good", rate.getTid(),
                            rate.getOid(), content);
                }
            }
        }
    }

}
