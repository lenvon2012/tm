/**
 * 
 */

package job.defense;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import job.sms.SmsSendJob;
import models.defense.BlackListBuyer;
import models.defense.BlackListExplain;
import models.defense.DefenderOption;
import models.defense.DefenseLog;
import models.defense.DefenseLog.DefenseLogStatus;
import models.defense.DefenseWarn;
import models.defense.ItemBuyLimit;
import models.defense.ItemPass;
import models.defense.ItemPass.ItemPassStatus;
import models.defense.WhiteListBuyer;
import models.item.ItemCatPlay;
import models.item.ItemPlay;
import models.sms.SmsSendLog;
import models.user.User;
import models.user.UserIdNick;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.DateUtil;
import actions.DefenderAction;
import actions.DefenderAction.DefenderRet;
import actions.catunion.UserIdNickAction;
import actions.catunion.UserRateSpiderAction;
import actions.catunion.UserRateSpiderAction.UserRateInfo;
import bustbapi.ItemApi.ItemGet;
import bustbapi.IdNickApi;
import bustbapi.TMTradeApi;
import bustbapi.TradeMemoApi;
import cache.TradeDefenseCache.BlackListBuyerCache;
import cache.TradeDefenseCache.ItemPassCache;
import cache.TradeDefenseCache.WhiteListBuyerCache;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.ApiException;
import com.taobao.api.SecretException;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.Order;
import com.taobao.api.domain.Trade;

import configs.Subscribe.Version;
import configs.TMConfigs.TMWarning;
import controllers.TmSecurity;
import controllers.TmSecurity.SecurityType;
import dao.defense.BlackListBuyerDao;
import dao.defense.BlackListExplainDao;
import dao.defense.DefenderOptionDao;
import dao.defense.DefenseWarnDao;
import dao.defense.ItemBuyLimitDao;
import dao.defense.ItemPassDao;
import dao.defense.WhiteListBuyerDao;
import dao.item.ItemDao;
import dao.trade.OrderDisplayDao;

/**
 * @author navins
 * @date 2013-6-16 下午12:16:36
 */
public class TradeDefenseCaller implements Callable<Boolean> {

    private static final Logger log = LoggerFactory.getLogger(TradeDefenseCaller.class);

    // 关闭订单重试次数（10s内生成的交易是不能关闭的）
    private static final int DOCLOSE_RETRY_TIME = 4;

    private User user;

    private Trade trade;

    private String buyerNick;

    public TradeDefenseCaller(User user, Trade trade, String buyerNick) {
        super();
        this.user = user;
        this.trade = trade;
        this.buyerNick = buyerNick;
    }

    @Override
    public Boolean call() throws Exception {
    	try {
    		buyerNick = TmSecurity.decrypt(buyerNick, SecurityType.SIMPLE, user);
		} catch (SecretException e) {
			e.printStackTrace();
		}
        try {
        	if(user.getIdlong() == 816963326L) {
        		//log.info("do not defense for special user: 想长大的小小");
        		return Boolean.TRUE;
        	}
            long tradeId = trade.getTid();
            log.info("[TradeDefenseCaller]tid=" + tradeId + " TradeDefenseCaller buyer=" + buyerNick + " userId="
                    + user.getIdlong());

            // 排除邮费链接
            boolean isSpecial = checkSpecialItem(user, trade.getNumIid());
            if (isSpecial == true) {
                log.info("[Postage] escape post fee item: " + trade.getNumIid() + "  tid=" + tradeId + " , userId="
                        + user.getId());
                return Boolean.TRUE;
            }

            //log.info("log just before white list check for tid : " + tradeId);
            // 白名单直接通过
            boolean isWhitelist = checkWhiteList(user, buyerNick);
            if (isWhitelist == true) {
                log.info("[TradeDefenseCaller]tid=" + tradeId + " WhiteList buyer=" + buyerNick + " , userId="
                        + user.getId());
                return Boolean.TRUE;
            }

            //log.info("log just before trade status check for tid : " + tradeId);
            if (!"WAIT_BUYER_PAY".equals(trade.getStatus()) && !"TRADE_NO_CREATE_PAY".equals(trade.getStatus())) {
                log.warn("[TradeDefenseCaller]Trade Status ERROR: " + trade.getStatus() + " , userId=" + user.getId());
                return Boolean.TRUE;
            }

            int status = 0;
            long numIid = 0L;
            String closeReason = "";
            String opMsg = "";

            //log.info("log just before item pass check for tid : " + tradeId);
            int itemPass = checkItemPass(user, trade);
            if (itemPass == ItemPassStatus.PASS_ALL) {
                // 不拦截宝贝
                log.info("[TradeDefenseCaller]ItemPass Items, tid=" + tradeId + " , userId=" + user.getId());
                return Boolean.TRUE;
            }

            // 不拦截宝贝，黑名单拦截
            //log.info("log just before black list check for tid : " + tradeId);
            BlackListBuyer buyer = checkBlackList(user, buyerNick);
            if (buyer != null) {
                // 黑名单用户
                BlackListExplain explain = BlackListExplainDao.findByUserId(user.getId());
                if (explain != null) {
                    closeReason = explain.getTradeExplain();
                }
                if (StringUtils.isEmpty(closeReason)) {
                    closeReason = BlackListExplain.Default_Explain;
                }
                status = DefenseLogStatus.BlackListBlock;
                opMsg = "[黑名单]" + buyer.getRemark();
            } else {
                // 非黑名单，且设置不拦截宝贝
                if (itemPass == ItemPassStatus.BLOCK_BLACKLIST) {
                    log.info("[TradeDefenseCaller]ItemPass Items, BLOCK_BLACKLIST, tid=" + tradeId + " , userId="
                            + user.getId());
                    return Boolean.TRUE;
                }

                // 宝贝限购
                ItemBuyLimit itemLimit = checkItemBuyLimit(user.getIdlong(), buyerNick, trade);
                if (itemLimit != null) {
                    numIid = itemLimit.getNumIid();
                    status = DefenseLogStatus.BuyLimitBlock;
                    closeReason = itemLimit.getCloseReason();
                    if (StringUtils.isEmpty(closeReason)) {
                        closeReason = ItemBuyLimit.DEFAULT_CLOSEREASON;
                    }
                    opMsg = "[宝贝限购]宝贝ID：" + itemLimit.getNumIid();
                } else {
                    if (user.isAutoDefenseOn() == false) {
                        log.info("[TradeDefenseCaller][AutoDefense] off, userId=" + user.getIdlong() + ", tid="
                                + tradeId);
                        return Boolean.TRUE;
                    }
                    // 是否被规则排除
                    DefenderOption option = DefenderOptionDao.findByUserId(user.getIdlong());
                    DefenderRet ret = checkDefenseByRule(option, buyerNick, trade);
                    if (ret == null || ret.isChaping() == false) {
                        log.info("[TradeDefenseCaller][DefenderRet] defense by rule = false, userId="
                                + user.getIdlong() + ", tid=" + tradeId + " buyerNick=" + buyerNick);
                        return Boolean.TRUE;
                    }
                    closeReason = option.getCloseReason();
                    if (StringUtils.isEmpty(closeReason)) {
                        closeReason = DefenderOption.DEFAULT_CLOSEREASON;
                    }
                    status = DefenseLogStatus.RuleBlock;
                    opMsg = ret.getMsg();
                }
            }

            // 天猫店铺
            //log.info("log just before tmall check for tid : " + tradeId);
            if (user.isTmall() == true) {
                log.warn("[TradeDefenseCaller]Tmall & NotClose notice, tid=" + tradeId + ", user=" + user.getIdlong()
                        + ", buyerNick=" + buyerNick);
                saveDefenseLog(user.getIdlong(), tradeId, numIid, buyerNick, opMsg, StringUtils.EMPTY, status, true);
                if (status != DefenseLogStatus.BuyLimitBlock) {
                    String smsContent = user.getUserNick() + ": 亲，发现疑似中评师" + buyerNick + "的订单，请及时登录淘宝查看。";
                    if (user.isSendDefenseMsgOn() && !user.isDefenseNoticeSmsOff()) {
                        sendSmsMsg(user, smsContent, SmsSendLog.TYPE.DEFESE_WARN, tradeId);
                    } else {
                        log.warn("[TradeDefenseCaller]sendSmsMsg isSendDefenseMsgOn=false, userId=" + user.getIdlong());
                    }
                    log.warn("[TradeDefenseCaller]sms no close: " + smsContent);
                }
                return Boolean.FALSE;
            }

            log.warn("Do Close TRADE, tid=" + tradeId + ", user=" + user.getIdlong() + ", buyerNick=" + buyerNick);
            // 关闭订单 10s内不能关闭订单
            long sleep = 11000L - (System.currentTimeMillis() - trade.getCreated().getTime());
//            if (checkChongzhiItem(user, numIid) == true) {
//                // 充值平台 60s之后才能关
//                sleep += 50000L;
//            }
            if (sleep > 0) {
            	if(sleep > 10000) {
            		sleep = 10000;
            	}
                CommonUtils.sleepQuietly(sleep);
            }

            // 关闭订单
            closeReason = checkCloseReason(closeReason);
            OperateStatus opStatus = closeTradeById(user, tradeId, closeReason);
            int retry = 1;

            while (opStatus.isSuccess == false && retry++ < DOCLOSE_RETRY_TIME) {
                CommonUtils.sleepQuietly(3000);
                opStatus = closeTradeById(user, tradeId, closeReason);
            }
            // OperateStatus opStatus = new OperateStatus(false, "");

            // 关闭成功
            String appName = "我们";
            if (opStatus.isSuccess == true) {
                saveDefenseLog(user.getIdlong(), tradeId, numIid, buyerNick, opMsg, StringUtils.EMPTY, status, true);
                if (status != DefenseLogStatus.BuyLimitBlock) {
                    String smsContent = user.getUserNick() + ": 亲，" + appName + "成功为您关闭了疑似中评师" + buyerNick
                            + "的订单，如有问题请及时登录淘宝查看。";
                    if (user.isSendDefenseMsgOn() && !user.isDefenseNoticeSmsOff()) {
                        sendSmsMsg(user, smsContent, SmsSendLog.TYPE.DEFESE_SUCCESS_WARN, tradeId);
                    } else {
                        log.warn("[TradeDefenseCaller]sendSmsMsg isSendDefenseMsgOn=false, userId=" + user.getIdlong());
                    }
                    //log.warn("[Defender]" + smsContent);
                }
                
                // 这里对关闭的订单进行备注
                //if(user.getId() == 712621070L) {
                	Boolean addMemoSuccess = new TradeMemoApi.TradeMemoUpdate(user, tradeId, opMsg).call();
                	log.info("TradeMemoApi.TradeMemoUpdate for user: " + user.getUserNick() + " and tid = " + tradeId +
                			" and memo is " + opMsg + " is " + addMemoSuccess);
                //}
            } else {
                // 关闭失败
                log.warn("close trade failed: " + tradeId);
                saveDefenseLog(user.getIdlong(), tradeId, numIid, buyerNick, opMsg,opStatus.getOpMsg(), status, false);
                if (status != DefenseLogStatus.BuyLimitBlock) {
                    String smsContent = user.getUserNick() + ": 亲，" + appName + "发现疑似差评用户" + buyerNick
                            + "的订单，未关闭成功，请及时登录淘宝查看。";
                    if (user.isSendDefenseMsgOn() && !user.isDefenseNoticeSmsOff()) {
                        sendSmsMsg(user, smsContent, SmsSendLog.TYPE.DEFESE_FAIL_WARN, tradeId);
                    } else {
                        log.warn("[TradeDefenseCaller]sendSmsMsg isSendDefenseMsgOn=false, userId=" + user.getIdlong());
                    }
                    //log.warn("[Defender]fail: " + smsContent);
                }
            }

        } catch (Exception e) {
            log.error("[TradeDefenseCaller] error! >>>>>>>>>>>>>>>>>>>>>>>");
            log.error(e.getMessage(), e);
        }
        return Boolean.FALSE;
    }

    private String checkCloseReason(String closeReason) {
    	DefenderOption option = DefenderOptionDao.findByUserId(user.getIdlong());
    	if(option == null) {
    		return "未及时付款";
    	}
    	closeReason = option.getCloseReason();
    	if(StringUtils.isEmpty(closeReason)) {
    		return "未及时付款";
    	}
    	if(closeReason.equals("未及时付款") || closeReason.equals("买家信息填写错误，重新拍") || closeReason.equals("恶意买家/同行捣乱") || 
    			closeReason.equals("缺货") || closeReason.equals("买家拍错了") || closeReason.equals("同城见面交易") ||
    			closeReason.equals("买家不想买了")) {
    		return closeReason;
    	}
    	return "未及时付款";
    }
    
    private boolean checkSpecialItem(User user, Long numIid) {
        if (numIid == null || numIid <= 0) {
            return false;
        }
        ItemPlay item = ItemDao.findByNumIid(user.getId(), numIid);
        if (item == null) {
            Item itembase = new ItemGet(user, numIid).call();
            if (itembase != null) {
                item = new ItemPlay(user.getId(), itembase);
                item.jdbcSave();
            }
        }

        if (item != null && (item.getCid() == 50023725L || item.getCid() == 50023728L)) {
            return true;
        }
        return false;
    }

    private boolean checkChongzhiItem(User user, Long numIid) {
        if (numIid == null || numIid <= 0) {
            return false;
        }
        ItemPlay item = ItemDao.findByNumIid(user.getId(), numIid);
        if (item == null) {
            Item itembase = new ItemGet(user, numIid).call();
            if (itembase != null) {
                item = new ItemPlay(user.getId(), itembase);
                item.jdbcSave();
            }
        }

        if (item == null) {
            return false;
        }

        List<ItemCatPlay> cidPaths = ItemCatPlay.findCidPath(item.getCid());
        if (cidPaths != null && cidPaths.size() > 0) {
            long pid = cidPaths.get(0).getCid();
            if (pid == 50004958L || pid == 50019286L) {
                return true;
            }
        }
        return false;
    }

    private OperateStatus closeTradeById(User user, Long tradeId, String closeReason) throws Exception {
        try {
            TMTradeApi.CloseTrade tradeApi = new TMTradeApi.CloseTrade(user, tradeId, closeReason);
            tradeApi.call();
            boolean isSuccess = tradeApi.isApiSuccess();
            if (isSuccess == true) {
                return new OperateStatus(true, "");
            } else {
                String errorMsg = tradeApi.getSubMsg();
                log.error("[close trade error]: " + errorMsg);
                return new OperateStatus(false, errorMsg);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new OperateStatus(false, "系统出现异常，" + ex.toString());
        }
    }

    /**
     * 检查白名单
     * 
     * @param user
     * @param buyerNick
     * @return 该用户是否在白名单，true表示在白名单
     */
    private boolean checkWhiteList(User user, String buyerNick) {
        // if (StringUtils.isEmpty(buyerNick) || user.isWhiteListAutoDefenseOn() == false) {
        if (StringUtils.isEmpty(buyerNick)) {
            return false;
        }

        HashMap<String, WhiteListBuyer> map = WhiteListBuyerCache.getWhiteListBuyerFromCache(user.getIdlong());
        if (map == null) {
            List<WhiteListBuyer> buyers = WhiteListBuyerDao.findWhiteListBuyers(user.getIdlong());
            map = new HashMap<String, WhiteListBuyer>();
            if (buyers == null) {
                return false;
            }
            for (WhiteListBuyer buyer : buyers) {
                map.put(buyer.getBuyerName(), buyer);
            }
            WhiteListBuyerCache.putIntoCache(user.getIdlong(), map);
        }
        if (map == null || map.size() == 0) {
            return false;
        }

        // WhiteListBuyer buyer = WhiteListBuyerDao.findByBuyerName(user.getIdlong(), buyerNick);
        WhiteListBuyer buyer = map.get(buyerNick);
        if (buyer == null) {
            return false;
        }
        return true;
    }

    /**
     * 检查黑名单
     * 
     * @param user
     * @param buyerNick
     * @return 该用户在黑名单返回BlackListBuyer，返回null表示不在黑名单
     */
    private BlackListBuyer checkBlackList(User user, String buyerNick) {
        // if (StringUtils.isEmpty(buyerNick) || user.isBlackListAutoDefenseOn() == false) {
        if (StringUtils.isEmpty(buyerNick)) {
            return null;
        }

        HashMap<String, BlackListBuyer> map = BlackListBuyerCache.getBlackListBuyerFromCache(user.getIdlong());
        if (map == null) {
            List<BlackListBuyer> itemList = BlackListBuyerDao.findBlackListBuyers(user.getIdlong());
            map = new HashMap<String, BlackListBuyer>();
            if (itemList == null) {
                return null;
            }
            for (BlackListBuyer buyer : itemList) {
                map.put(buyer.getBuyerName(), buyer);
            }
            BlackListBuyerCache.putIntoCache(user.getIdlong(), map);
        }
        if (map == null || map.size() == 0) {
            return null;
        }

        BlackListBuyer buyer = map.get(buyerNick);
        return buyer;
    }

    /**
     * 检查不拦截宝贝
     * 
     * @param user
     * @param list
     * @return ItemPassStatus 状态
     */
    private int checkItemPass(User user, Trade trade) {
        int itemPass = ItemPassStatus.NOT_PASS;
        List<Order> list = trade.getOrders();
        if (CommonUtils.isEmpty(list)) {
            return itemPass;
        }

        HashMap<Long, ItemPass> map = ItemPassCache.getItemPassFromCache(user.getIdlong());
        if (map == null) {
            List<ItemPass> itemList = ItemPassDao.findByUserId(user.getIdlong());
            map = new HashMap<Long, ItemPass>();
            if (itemList == null) {
                return itemPass;
            }
            for (ItemPass item : itemList) {
                map.put(item.getNumIid(), item);
            }
            ItemPassCache.putIntoCache(user.getIdlong(), map);
        }
        if (map == null || map.size() == 0) {
            return itemPass;
        }

        for (Order order : list) {
            // ItemPass item = ItemPassDao.findByUserIdNumIid(user.getIdlong(), order.getNumIid());
            ItemPass item = map.get(order.getNumIid());
            if (item == null || item.getStatus() == ItemPassStatus.NOT_PASS) {
                itemPass = ItemPassStatus.NOT_PASS;
                break;
            }
            if (item.getStatus() > itemPass) {
                itemPass = item.getStatus();
            }
        }
        return itemPass;
    }

    /**
     * 检查限购
     * 
     * @param userId
     * @param buyerNick
     * @param trade
     * @return 限购宝贝的宝贝的限购内容ItemBuyLimit，返回null表示不限购
     */
    private ItemBuyLimit checkItemBuyLimit(Long userId, String buyerNick, Trade trade) {
        List<Order> list = trade.getOrders();

        // HashMap<Long, ItemBuyLimit> limitMap = ItemBuyLimitCache.getItemBuyLimitFromCache(userId);
        // if (limitMap == null) {
        List<ItemBuyLimit> itemLimits = ItemBuyLimitDao.findByUserId(userId);
//        log.error("[TradeDefenseCaller]ItemLimit: " + itemLimits.toString());
        HashMap<Long, ItemBuyLimit> limitMap = new HashMap<Long, ItemBuyLimit>();
        for (ItemBuyLimit item : itemLimits) {
            limitMap.put(item.getNumIid(), item);
        }
        // ItemBuyLimitCache.putIntoCache(userId, limitMap);
        // }
        if (CommonUtils.isEmpty(limitMap) || CommonUtils.isEmpty(list)) {
            return null;
        }

        // 先写数据库，做限购判断
//        List<Trade> trades = new ArrayList<Trade>();
//        trades.add(trade);
//        TradeWritter.writeTradeImmediately(user.getId(), trades, trade.getCreated().getTime());

        // Map<Long, Long> numIidBuyNum = OrderDisplayDao.countUserNumIidBuyNum(userId, buyerNick, ts);

        // 合并order
        HashMap<Long, Order> orders = new HashMap<Long, Order>();
        for (Order order : list) {
            Order exist = orders.get(order.getNumIid());
            if (exist == null) {
                orders.put(order.getNumIid(), order);
                continue;
            }
            exist.setNum(exist.getNum() + order.getNum());
        }

        Iterator iter = orders.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Long, Order> entry = (Entry<Long, Order>) iter.next();
            Order order = entry.getValue();

            ItemBuyLimit itemLimit = limitMap.get(order.getNumIid());
            if (itemLimit == null) {
                log.error("[TradeDefenseCaller]itemLimit NULL numIid:" + order.getNumIid());
                continue;
            }

            long ts = 0;
            if (itemLimit.getDaysLimit() > 0) {
                ts = System.currentTimeMillis() - DateUtil.DAY_MILLIS * itemLimit.getDaysLimit();
            }

            // 查订购记录 该用户最近itemLimit.getDaysLimit()，购买该item单数
            long totalTradeNum = OrderDisplayDao.countBuyNumIidNum(userId, buyerNick, order.getNumIid(), ts);
            log.error("[TradeDefenseCaller]userId=" + userId + ", numIid=" + order.getNumIid() + ", buyerNick="
                    + buyerNick + ", totalTradeNum=" + totalTradeNum + ", tradeNum=" + itemLimit.getTradeNum());
            if (itemLimit.getTradeNum() > 0 && totalTradeNum > itemLimit.getTradeNum()) {
                return itemLimit;
            }

            if (itemLimit.getItemMinNum() > 0 && order.getNum() < itemLimit.getItemMinNum()) {
                return itemLimit;
            }
            if (itemLimit.getItemMaxNum() > 0 && order.getNum() > itemLimit.getItemMaxNum()) {
                return itemLimit;
            }
        }

        return null;
    }

    /**
     * 差评师设置判断
     * 
     * @param option
     * @param buyerNick
     * @param trade
     * @return DefenderRet
     */
    private DefenderRet checkDefenseByRule(DefenderOption option, String buyerNick, Trade trade) {
        if (option == null) {
            return null;
        }
        
        // 这里优先检查仅与订单相关的条件，以防UserRateInfo查找不到直接跳过了
        // 如果已经判断拦截了，就返回，不需要再判断买家个人信誉等信息
        DefenderRet ret = DefenderAction.checkSellerConfig(option, trade, buyerNick);
        if(ret.isChaping()) {
        	return ret;
        }
        
        /*UserIdNick idnick = UserIdNick.findOrCreate(buyerNick);
        UserRateInfo info = UserRateSpiderAction.spiderUserRateById(idnick);
        int retry = 1;
        while ((info == null || StringUtils.isEmpty(info.getUserNick())) && retry++ < 3) {
            log.error("[checkDefenseByRule] info null! nick: " + buyerNick + " userId=" + option.getUserId());
            info = UserRateSpiderAction.spiderUserRateById(idnick);
        }
        if (info == null || StringUtils.isEmpty(info.getUserNick())) {
            log.error("[checkDefenseByRule] FINAL info null! nick: " + buyerNick + " userId=" + option.getUserId()
                    + "  trade: " + trade.getTid());
            return ret;
        }
        log.info("[checkDefenseByRule]userId: " + option.getUserId() + " buyerInfo: " + info);
        
        ret = DefenderAction.checkDefender(option, info, trade);*/
        com.taobao.api.domain.User user = UserRateSpiderAction.getUserByApi(buyerNick);
        if(user == null) {
        	user = UserRateSpiderAction.getUserByApi(buyerNick);
        }
        if(user == null) {
        	return ret;
        }
        ret = DefenderAction.checkNewDefender(option, user, trade.getTid());
        return ret;
    }

    /**
     * 存放日志
     * 
     * @param userId
     * @param tradeId
     * @param numIid
     * @param buyerName
     * @param opMsg
     * @param status
     * @param isSuccess
     */
    private void saveDefenseLog(Long userId, Long tradeId, Long numIid, String buyerName, String opMsg,
    		String closeFailReason, int status, boolean isSuccess) {
        DefenseLog defenseLog = new DefenseLog(userId, tradeId, numIid, buyerName, opMsg, closeFailReason, status);
        log.info(defenseLog.toString());
        defenseLog.setOperateSuccess(isSuccess);
        defenseLog.jdbcSave();
    }

    public static void sendSmsMsg(User user, String content, int type, long tid) {
        try {

            if (user == null) {
                log.warn("sendSmsMsg isSendDefenseMsgOn=false && user NULL!");
                return;
            }
            
            List<DefenseWarn> warnList = DefenseWarnDao.findByUserId(user.getId());
            for (int i = 0; i < warnList.size() && i < TMWarning.Max_Phone_Number; i++) {
                DefenseWarn warn = warnList.get(i);
                String phoneNumber = warn.getTelephone();
                if (StringUtils.isEmpty(phoneNumber)) {
                    continue;
                }

                SmsSendJob.addQueue(user.getId(), user.getUserNick(), phoneNumber, content, type, tid);
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    public static void sendSmsMsg(User user, String content, String phone, int type, long tid) {
        try {

            if (user == null) {
                log.warn("sendSmsMsg isSendDefenseMsgOn=false && user NULL!");
                return;
            }
            if (StringUtils.isEmpty(phone)) {
                log.warn("sendSmsMsg null! userId=" + user.getId());
            }
            SmsSendJob.addQueue(user.getId(), user.getUserNick(), phone, content, type, tid);

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    public static class OperateStatus {
        private boolean isSuccess;

        private String opMsg;

        public boolean isSuccess() {
            return isSuccess;
        }

        public void setSuccess(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }

        public String getOpMsg() {
            return opMsg;
        }

        public void setOpMsg(String opMsg) {
            this.opMsg = opMsg;
        }

        public OperateStatus(boolean isSuccess, String opMsg) {
            super();
            this.isSuccess = isSuccess;
            this.opMsg = opMsg;
        }

    }

}
