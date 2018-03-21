package controllers;

import actions.DefenderAction;
import actions.DefenderAction.DefenderRet;
import actions.SubcribeAction;
import actions.catunion.UserIdNickAction.BuyerIdApi;
import actions.catunion.UserRateSpiderAction;
import actions.catunion.UserRateSpiderAction.CommentType;
import actions.catunion.UserRateSpiderAction.UserRateInfo;
import actions.clouddata.PCSrcUvPv;
import actions.juxin.JUXinSmsSend;
import actions.juxin.JUXinSmsSend.ResultInfo;
import actions.juxin.JUXinSmsSend.Signatures;
import actions.meilian.SMSSendMeiLian;
import actions.shengtai.SMSSendShengTai;
import actions.sms.SmsAction;
import bustbapi.ShopApi;
import bustbapi.TradeRateApi;
import bustbapi.JMSApi.JushitaJmsUserAdd;
import bustbapi.JMSApi.JushitaJmsUserGet;
import bustbapi.TradeRateApi.TraderatesGet;
import bustbapi.UsersGetApi;
import cache.TradeDefenseCache.ItemBuyLimitCache;
import cache.TradeDefenseCache.ItemPassCache;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;
import com.taobao.api.domain.Shop;
import com.taobao.api.domain.TmcUser;
import com.taobao.api.domain.TradeRate;

import configs.TMConfigs;
import controllers.Op.InviteInfo;
import dao.UserDao;
import dao.comments.CommentsDao;
import dao.defense.*;
import dao.item.ItemDao;
import dao.trade.TradeDisplayDao;
import job.defense.TradeDefenseCaller;
import job.defense.TradeMsgDealerJob;
import job.defense.TradeRateMsgDealer;
import job.word.BusRefreshWordJob;
import models.CPEctocyst.ChiefStaffDetail;
import models.CPEctocyst.SellerToStaff;
import models.SendMsgLog;
import models.defense.DefenderOption;
import models.defense.DefenseLog;
import models.defense.ItemBuyLimit;
import models.defense.ItemPass;
import models.item.ItemPlay;
import models.op.CPStaff;
import models.sms.SmsSendCount;
import models.sms.SmsSendLog;
import models.trade.TradeDisplay;
import models.traderatesms.RateSmsReceiveLog;
import models.traderatesms.RateSmsRechargeLog;
import models.traderatesms.RateSmsSendLog;
import models.user.User;
import models.user.UserIdNick;
import models.word.top.BusCatPlay;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import play.cache.Cache;
import play.libs.Codec;
import proxy.ProxyDaili999API;
import result.TMPaginger;
import result.TMResult;
import secure.SimulateRequestUtil;
import smsprovider.SZYaoJiaProvider;
import smsprovider.SendInfo;
import smsprovider.QTL.QLTProvider;
import utils.DateUtil;
import utils.PlayUtil;
import utils.TaobaoUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.String.format;

/**
 * 淘掌柜-好评王-差评防御-防御管理
 * 
 */
public class SkinDefender extends TMController {

    public static void reinitRateSize(long size){
        TraderatesGet.PAGE_SIZE = size;
        renderText(TraderatesGet.PAGE_SIZE);
    }

    public static void reinitSync(boolean enable) {
//        APIConfig.get().setEnableSyncTrade(enable);
        TradeRateMsgDealer.IS_ALLOW_TMC_RATED = enable;
        renderText(enable);
    }


    public static void checkTradeRate(Long tid) {
        User user = getUser();
        List<TradeRate> rateList = new TradeRateApi.TraderatesGet(user, tid).call();

        renderJSON(rateList);
    }

    public static void word() {
        new BusRefreshWordJob().now();
    }

    public static void cat() {
        renderJSON(BusCatPlay.findByParentId(1));
    }

    
    public static void index() {
    	User user = getUser();
    	SellerToStaff sellerToStaff = SellerToStaff.findByUserId(user.getId());
    	boolean isAllcoted = false;
    	ChiefStaffDetail chiefStaffDetail = null;
    	if(sellerToStaff != null) {
    		isAllcoted = true;
    		chiefStaffDetail = ChiefStaffDetail.findByChiedName(sellerToStaff.getChiefName());
    		if(chiefStaffDetail != null) {
    			if("领航工作室".equals(chiefStaffDetail.getCompanyName()) || "腾飞售后客服团队".equals(chiefStaffDetail.getCompanyName())
    				|| "美淘工作室".equals(chiefStaffDetail.getCompanyName())
    				|| "优速淘删评工作室".equals(chiefStaffDetail.getCompanyName())) {
	    			chiefStaffDetail.setShowPrice(false);
	    		}
    		}
    		
    	}
		// 查询某个用户是否同步消息
		JushitaJmsUserGet jmsGet = new JushitaJmsUserGet(user);
		TmcUser onsUser = jmsGet.call();
		if (onsUser == null) {
			// 添加ONS消息同步用户
			JushitaJmsUserAdd jmsAdd = new JushitaJmsUserAdd(user);
			Boolean success = jmsAdd.call();
			if (!success) {
				log.error("添加ONS消息同步用户失败！" + user.toString() + "~~~错误： "
						+ jmsAdd.getSubErrorMsg());
			} else {
				log.info("添加ONS消息同步用户成功！" + user.toString());
			}
		}
        render("skincomment/defenderindex.html", isAllcoted, chiefStaffDetail);
    }
    
    
    public static void CPEctocystIndex() {
    	render("skincomment/CPEctocystIndex.html");
    }
    
    public static void CPEctocystStatus() {
    	render("skincomment/CPEctocystStatus.html");
    }

    public static void CPEctocystCommentUrge() {
    	render("skincomment/CPEctocystCommentUrge.html");
    }

    public static void lottery() {
        render("lottery/lotterydefender.html");
    }

    static String STATUS_FORMAT = "{\"success\":true, \"defenseCount\":%d, \"commentCount\":%d, \"smswarnsCount\":%d, \"smsCount\":%d, \"blacklistCount\":%d, \"whitelistCount\":%d, \"smsLeft\":%d}";

    
    public static void status() {
        User user = getUser();
        long defenseCount = DefenseLogDao.countDefenseLogByRules(user.getId(), null, null, null, null, null, 0);
        long commentCount = CommentsDao.countOnlineByUser(user.getId());
        long smswarnsCount = DefenseWarnDao.countByUserId(user.getId());
        long smsCount = SmsSendLog.countSmsSendLogByRules(user.getId(), null, null, null);
        long blacklistCount = BlackListBuyerDao.countBlackListBuyers(user.getId());
        long whitelistCount = WhiteListBuyerDao.countWhiteListBuyers(user.getId());
        SmsSendCount sms = SmsSendCount.findByUserId(user.getId());
        if (sms == null) {
            sms = SubcribeAction.updateSmsCount(user);
        }
        long smsLeft = sms.getTotal() - sms.getUsed();

        renderJSON(String.format(STATUS_FORMAT, defenseCount, commentCount, smswarnsCount, smsCount, blacklistCount,
                whitelistCount, smsLeft));
    }

    
    public static void warnsCount() {
        User user = getUser();
        long smswarnsCount = DefenseWarnDao.countByUserId(user.getId());
        renderJSON("{\"smswarnsCount\": " + smswarnsCount + "}");
    }

    
    public static void addSmsCount(String userNick, int addNum) {
        User user = UserDao.findByUserNick(userNick);
        String info = "";
        if (user == null) {
            info = "该用户不存在！";
            renderJSON("{\"info\": \"" + info + "\"}");
        }
        if (addNum <= 0) {
            info = "当前 " + SmsSendCount.findByUserId(user.getId()).toString();
            renderJSON("{\"info\": \"" + info + "\"}");
        }
        boolean success = SmsSendCount.addTotalSmsCount(user.getId(), addNum);
        if(!success) {
        	info = "数据库更新错误，充值失败！";
            renderJSON("{\"info\": \"" + info + "\"}");
        }
        // 充值日志
        RateSmsRechargeLog log = new RateSmsRechargeLog(addNum, userNick, user.getId());
        log.jdbcSave();
        
        info = "充值成功：当前 " + SmsSendCount.findByUserId(user.getId()).toString();
        renderJSON("{\"info\": \"" + info + "\"}");
    }

    
    public static void limit() throws IOException {
        User user = getUser();
        UserDao.refreshToken(user);

        String msgBody = FileUtils.readFileToString(new File(TMConfigs.mockDir, "trade.json"));
        log.error("sss: " + msgBody);
        TradeMsgDealerJob.addTradeMsg(msgBody);
    }


    public static void rate(long tid) {
        List<TradeRate> list = null;
        if (tid > 0) {
            list = new TradeRateApi.TraderatesGet(getUser(), tid).call();
            log.warn("[rate]trade rates[" + list);
            renderJSON(list);
        }

        list = TradeRateApi.fetchBadTradeRate(getUser(), System.currentTimeMillis() - DateUtil.WEEK_MILLIS,
                System.currentTimeMillis());
        renderJSON(JsonUtil.getJson(list));
    }

    
    public static void badcomment(Long userId) {
        if (userId == null || userId <= 0) {
            userId = 153921866L;
        }
        String json = UserRateSpiderAction.doSpiderCommentList(userId, StringUtils.EMPTY, 1, CommentType.Neutral, true);
        renderJSON(json);
    }

    
    public static void tt() throws Exception {

//        new RefreshTokenJob().doJob();
//
        User user = getUser();
//        TradeRateMsgDealer.addMsg(user.getIdlong(), 365086289922284L, 365086289932284L);
//
//        String msgBody = FileUtils.readFileToString(new File(TMConfigs.mockDir, "trade.json"));
//        log.error("sss: " + msgBody);
//        TradeMsgDealerJob.addTradeMsg(msgBody);
//
//        // Trade trade = new TradeApi.GetFullTrade(user.getSessionKey(), 365086289922284L).call();
//        // new TradeDisplay(user.getIdlong(), System.currentTimeMillis(), trade).insertOnDupKeyUpdate();
//
//        TradeRatePlay tt = new TradeRatePlay();
//        tt.setNick("ssss");
//        tt.setUserId(user.getIdlong());
//        tt.setId(12131L);
//        tt.setTid(0L);
//        tt.setOid(10);
//        tt.setNumIid(2312321L);
//        tt.setRatedNick("");
//        tt.jdbcSave();
//
//        TradeDisplay td = new TradeDisplay();
//        td.buyerNick = "xxx";
//        td.userId = 0L;
//        td.tid = 0L;
//        td.insertOnDupKeyUpdate();

        // OrderDisplay orderDisplay = new OrderDisplay();
        // orderDisplay.setNumIid(0L);
        // orderDisplay.setId(0L);
        // orderDisplay.title = "sssss";
        // orderDisplay.jdbcSave();

//        new SmsSendLog(user.getIdlong(), "test2", "15088682225", "内容内容", 16, 1L, true).jdbcSave();
//        SmsSendJob.addQueue(user.getId(), "ddd", "15088682225", "我的内容", 1, 22L);
//        new PaiPaiMidNightJob().doJob();

//        String content = user.getUserNick() + ": 淘宝买家给您，请及时登录淘宝查看，购买宝贝。【好评助手】";
//        TradeDefenseCaller.sendSmsMsg(user, content, SmsSendLog.TYPE.BADCOMMENT_NOTICE, 111111L);
        String json = UserRateSpiderAction.doSpiderCommentList(1651420064L, StringUtils.EMPTY, 1, CommentType.Negative, true);
//        renderText(DefenderAction.countComments(1651420064L, CommentType.Neutral));
//        new ProxiesUpdate().doJob();
        renderText(json);
    }

    
    public static void autoClose(boolean status) {
        User user = getUser();
        user.setAutoDefenseOn(status);
        // 这里加入交易create的消息 TradeCreate
//        TaobaoUtil.permitByUser(user);
        TaobaoUtil.permitTMCUser(user);
        user.jdbcSave();

        if (status == true) {
            renderText("on");
        } else {
            renderText("off");
        }
    }

    
    public static void isOn() {
        User user = getUser();
        boolean isOn = user.isAutoDefenseOn();
        renderSuccess("", isOn);
    }

    public static void turnOn() {
        User user = getUser();
        user.setAutoDefenseOn(true);
        // 这里加入交易create的消息 TradeCreate
//        TaobaoUtil.permitByUser(user);
        TaobaoUtil.permitTMCUser(user);
        user.jdbcSave();

        renderSuccess("【已打开】自动关闭订单", "");
    }

    
    public static void turnOff() {
        User user = getUser();
        user.setAutoDefenseOn(false);
        user.jdbcSave();
        renderSuccess("【已关闭】自动关闭订单", "");
    }

    
    public static void config() {
        User user = getUser();
        DefenderOption option = DefenderOptionDao.findOrUseDefaultOption(user.getIdlong());
        render("skincomment/defenseconfig.html", option);
    }

    public static void update(DefenderOption option, int isVerifyLimit, int allowNoCreditBuyer,
    		int allowSeller) {
        // MixHelpers.infoAll(request, response);
        if (isVerifyLimit == 1) {
            option.setVerifyLimit(true);
        } else {
            option.setVerifyLimit(false);
        }
        if (allowNoCreditBuyer == 1) {
            option.setAllowNoCreditBuyer(true);
        } else {
            option.setAllowNoCreditBuyer(false);
        }
        if(allowSeller == 1) {
        	option.setAllowSeller(true);
        } else {
        	option.setAllowSeller(false);
        }
        User user = getUser();
        option.setUserId(user.getIdlong());
        DefenderOptionDao.updateDefenderOption(option);
        // render("skincomment/defenseconfig.html", option);
        SkinDefender.config();
    }

    
    public static void log(String sid) {
        render("skincomment/defenselog.html");
    }

    
    public static void blackOrder() {
        render("skincomment/defenseblackorder.html");
    }

    
    public static void invite() {
        render("skincomment/defenseOpInvite.html");
    }

    
    public static void haoping() {
        render("skincomment/defenseOpHaoPing.html");
    }

    
    public static void itemProtection() {
        render("skincomment/itemprotection.html");
    }

    
    public static void itemProtectDetail(Long numIid) {
        User user = getUser();
        ItemPlay item = ItemDao.findByNumIid(user.getIdlong(), numIid);
        ItemBuyLimit option = ItemBuyLimitDao.findOrUseDefaultOption(user.getIdlong(), numIid);
        render("skincomment/itemprotectiondetail.html", item, option);
    }

    public static void updateItemProtectionDetail(ItemBuyLimit option) {
        User user = getUser();
        option.setUserId(user.getIdlong());
        ItemBuyLimitDao.updateDefenderOption(option);
        ItemBuyLimitCache.deleteItemBuyLimitFromCache(user.getId());
        itemProtection();
    }

    
    public static void deleteItemProtectionDetail(Long numIid) {
        User user = getUser();
        ItemBuyLimitDao.deleteDefenderOption(user.getIdlong(), numIid);
        ItemBuyLimitCache.deleteItemBuyLimitFromCache(user.getId());
        render("skincomment/itemprotection.html");
    }

    
    public static void itemPass() {
        render("skincomment/itempass.html");
    }

    
    public static void addItemPass(Long numIid, int status) {
        User user = getUser();
        boolean res = ItemPassDao.updateItemPass(user.getIdlong(), numIid, status);
        if (res == false) {
            renderText("error");
        }
        ItemPassCache.deleteItemPassFromCache(user.getId());
        if (status > 0) {
            renderText("on");
        }
        renderText("off");
    }

    /**
     * defener log...
     * 
     * @param tradeId
     * @param title
     * @param buyerName
     * @param startTime
     * @param endTime
     * @param pn
     * @param ps
     * @throws IOException
     */
    public static void queryDefenseLogs(Long tradeId, String title, String buyerName, String startTime, String endTime, int defenseStatus, 
            int pn, int ps) throws IOException {

        //renderMockFileInJsonIfDev("defenselogs.json");

        if (!StringUtils.isEmpty(title)) {
            title = title.trim();
        }
        if (!StringUtils.isEmpty(buyerName)) {
            buyerName = buyerName.trim();
        }
        // 0: 全部  1: 拦截成功  2: 拦截失败
        if (defenseStatus != 0 && defenseStatus  != 1 && defenseStatus != 2) {
        	defenseStatus = 0;
        }
        User user = getUser();
        Long startTs = null;
        Long endTs = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (!StringUtils.isEmpty(startTime)) {
            startTime = startTime.trim();
            try {
                startTs = sdf.parse(startTime).getTime();
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        if (!StringUtils.isEmpty(endTime)) {
            endTime = endTime.trim();
            try {
                endTs = sdf.parse(endTime).getTime();
                // 要加上一天的时间
                endTs += 24L * 3600L * 1000L;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        PageOffset po = new PageOffset(pn, ps);

        List<DefenseLog> logList = DefenseLogDao.findDefenseLogByRules(user.getId(), tradeId, title, buyerName,
                startTs, endTs, defenseStatus, po);
        long count = DefenseLogDao.countDefenseLogByRules(user.getId(), tradeId, title, buyerName, startTs, endTs, defenseStatus);
        List<DefenseLogItem> logItemList = new ArrayList<DefenseLogItem>();

        Set<Long> numIidSet = new HashSet<Long>();
        for (DefenseLog log : logList) {
            if (log.getNumIid() != null)
                numIidSet.add(log.getNumIid());
        }
        List<ItemPlay> itemList = ItemDao.findByNumIids(user.getId(), numIidSet);

        for (DefenseLog log : logList) {
            ItemPlay targetItem = null;
            for (ItemPlay item : itemList) {
                if (log.getNumIid() != null && log.getNumIid().equals(item.getNumIid())) {
                    targetItem = item;
                    break;
                }
            }
            if (targetItem == null)
                targetItem = new ItemPlay();

            DefenseLogItem logItem = new DefenseLogItem(log, targetItem);
            logItemList.add(logItem);
        }

        TMResult tmResult = new TMResult(logItemList, (int) count, po);
        renderJSON(tmResult);
    }

    public static void queryDefenseBlacklists(Long tradeId, String title, String buyerName, String startTime,
            String endTime, int pn, int ps) throws IOException {

        renderMockFileInJsonIfDev("defenseBlacklists.json");

        if (!StringUtils.isEmpty(title)) {
            title = title.trim();
        }
        if (!StringUtils.isEmpty(buyerName)) {
            buyerName = buyerName.trim();
        }

        User user = getUser();
        Long startTs = null;
        Long endTs = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (!StringUtils.isEmpty(startTime)) {
            startTime = startTime.trim();
            try {
                startTs = sdf.parse(startTime).getTime();
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        if (!StringUtils.isEmpty(endTime)) {
            endTime = endTime.trim();
            try {
                endTs = sdf.parse(endTime).getTime();
                // 要加上一天的时间
                endTs += 24L * 3600L * 1000L;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        PageOffset po = new PageOffset(pn, ps, 10);

        List<TradeDisplay> tradeList = TradeDisplayDao.findByBlacklist(user.getId(), tradeId, buyerName, startTs,
                endTs, po);
        // 御城河日志接入
        sendOrderLog(user.getId(), "差评统计", SimulateRequestUtil.getTradeTid(tradeList));
        long count = TradeDisplayDao.countDefenseBlacklistByRules(user.getId(), tradeId, buyerName, startTs, endTs);

        TMResult tmResult = new TMResult(tradeList, (int) count, po);
        renderJSON(tmResult);
    }

    
    public static void test(Long numIid) {
        User user = getUser();
        DefenseLog log = new DefenseLog(user.getId(), 1L, numIid, "test", "123", 0, System.currentTimeMillis());
        log.jdbcSave();
    }

    
    public static class DefenseLogItem {
        private DefenseLog log;

        private ItemPlay item;

        public DefenseLog getLog() {
            return log;
        }

        public void setLog(DefenseLog log) {
            this.log = log;
        }

        public ItemPlay getItem() {
            return item;
        }

        public void setItem(ItemPlay item) {
            this.item = item;
        }

        public DefenseLogItem(DefenseLog log, ItemPlay item) {
            super();
            this.log = log;
            this.item = item;
        }

    }

    /**
     * Actually, no diag needed....
     * 
     * @param s
     * @param pn
     * @param ps
     * @param lowBegin
     * @param topEnd
     * @param sort
     * @param status
     * @param catId
     */

    public static void getItems(String s, int pn, int ps, final int lowBegin, final int topEnd, int sort, int status,
            Long catId, Long cid, int only) {
        final User user = getUser();
        PageOffset po = new PageOffset(pn, ps, 5);

        log.info(format(
                "getItemsWithDiagResult:userId, s, pn, ps, lowBegin, topEnd, sort, status, catId".replaceAll(", ",
                        "=%s, ") + "=%s", user.getId(), s, pn, ps, lowBegin, topEnd, sort, status, catId));

        int count = 0;
        List<ItemPlay> list = null;
        List<ItemBuyLimit> itemLimits = ItemBuyLimitDao.findByUserId(user.getIdlong());
        if (only == 1) {
            HashSet<Long> ids = new HashSet<Long>();
            if (!CommonUtils.isEmpty(itemLimits)) {
                for (ItemBuyLimit itemLimit : itemLimits) {
                    ids.add(itemLimit.getNumIid());
                }
                list = ItemDao.findOnlineByUserWithArgsIds(user.getId(), po.getOffset(), po.getPs(), s, lowBegin,
                        topEnd, sort, status, catId == null ? "" : catId.toString(), cid, ids);
            }
            count = ids.size();
        } else {
            list = ItemDao.findOnlineByUserWithArgs(user.getId(), po.getOffset(), po.getPs(), s, lowBegin, topEnd,
                    sort, status, catId == null ? "" : catId.toString(), cid, true);

            if (CommonUtils.isEmpty(list)) {
                TMPaginger.makeEmptyFail("亲， 您还没有上架宝贝哟！！！！！");
            }

            count = (int) ItemDao.countOnlineByUserWithArgs(user.getId(), lowBegin, topEnd, s, 0L, status,
                    catId == null ? "" : catId.toString(), cid);
        }

        if (!CommonUtils.isEmpty(itemLimits) && !CommonUtils.isEmpty(list)) {
            for (ItemBuyLimit itemLimit : itemLimits) {
                for (ItemPlay item : list) {
                    if (item.getId().longValue() == itemLimit.getNumIid().longValue()) {
                        item.setItemLimit(itemLimit);
                        break;
                    }
                }
            }
        }
        // ComparatorItemBuyLimit comparator = new ComparatorItemBuyLimit();
        // Collections.sort(list, comparator);

        TMResult tmRes = new TMResult(list, count, po);
        renderJSON(JsonUtil.getJson(tmRes));
    }

    
    public static class ComparatorItemBuyLimit implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            ItemPlay item1 = (ItemPlay) o1;
            ItemPlay item2 = (ItemPlay) o2;
            if (item1.getItemLimit() != null) {
                return -1;
            }
            if (item2.getItemLimit() != null) {
                return 0;
            }
            return 0;
        }

    }

    public static void getItemsWithItemPass(String s, int pn, int ps, final int lowBegin, final int topEnd, int sort,
            int status, Long catId, Long cid, int only) {
        final User user = getUser();
        PageOffset po = new PageOffset(pn, ps, 5);

        log.info(format(
                "getItemsWithDiagResult:userId, s, pn, ps, lowBegin, topEnd, sort, status, catId".replaceAll(", ",
                        "=%s, ") + "=%s", user.getId(), s, pn, ps, lowBegin, topEnd, sort, status, catId));

        int count = 0;
        List<ItemPlay> list = null;
        List<ItemPass> itemPasses = ItemPassDao.findByUserId(user.getIdlong());
        if (only == 1) {
            HashSet<Long> ids = new HashSet<Long>();
            if (!CommonUtils.isEmpty(itemPasses)) {
                for (ItemPass itemPass : itemPasses) {
                    ids.add(itemPass.getNumIid());
                }
                list = ItemDao.findOnlineByUserWithArgsIds(user.getId(), po.getOffset(), po.getPs(), s, lowBegin,
                        topEnd, sort, status, catId == null ? "" : catId.toString(), cid, ids);
            }
            count = ids.size();
        } else {
            list = ItemDao.findOnlineByUserWithArgs(user.getId(), po.getOffset(), po.getPs(), s, lowBegin, topEnd,
                    sort, status, catId == null ? "" : catId.toString(), cid, true);

            if (CommonUtils.isEmpty(list)) {
                TMPaginger.makeEmptyFail("亲， 您还没有上架宝贝哟！！！！！");
            }

            count = (int) ItemDao.countOnlineByUserWithArgs(user.getId(), lowBegin, topEnd, s, 0L, status,
                    catId == null ? "" : catId.toString(), cid);
        }

        if (!CommonUtils.isEmpty(itemPasses) && !CommonUtils.isEmpty(list)) {
            for (ItemPass itemPass : itemPasses) {
                for (ItemPlay item : list) {
                    if (item.getId().longValue() == itemPass.getNumIid().longValue()) {
                        item.setItemPass(itemPass);
                        break;
                    }
                }
            }
        }

        TMResult tmRes = new TMResult(list, count, po);
        renderJSON(JsonUtil.getJson(tmRes));
    }

    
    public static void genInviteUrl() {
        User user = getUser();
        InviteInfo info = new InviteInfo();
        log.info("[user : ]" + user);
        String url = "http://chaping.taovgo.com/f/" + user.getId();
        info.setUrl(url);
        renderJSON(JsonUtil.getJson(info));
    }

    
    public static void getChapingBannerLink(String interval) {
        if (!interval.equals("quarter") && !interval.equals("halfyear")) {
            renderText("链接时间段出错");
        }
        User user = getUser();
        if (user == null) {
            renderText("找不到对应用户");
        }
        String key = TMConfigs.YINGXIAO.CHAPINGBANNERPREFIX + (user.getVersion() == -1 ? 20 : user.getVersion())
                + interval;
        String link = TMConfigs.YINGXIAO.chapingBannerMap.get(key);

        if (link == null || link.isEmpty()) {
            renderText("获取营销链接出错");
        }
        renderText(link);
    }
    
    
    public static void testNewRule() {
    	User user = getUser();
    	DefenderOption option = DefenderOptionDao.findByUserId(user.getId());
    	UserRateInfo info = UserRateSpiderAction.spiderUserRateById(778649664L);
    	DefenderRet ret = null;
    }
    
    
    public static void testSpiderUserRateById(String nick) {
    	if(StringUtils.isEmpty(nick)) {
    		renderFailedJson("买家号为空");
    	}
    	UserIdNick idnick = UserIdNick.findOrCreate(nick);
        UserRateInfo info = UserRateSpiderAction.spiderUserRateById(idnick);
        renderJSON(JsonUtil.getJson(info));
    }
    
    
    public static void getTMProxyPoolSize() {
    	
    }
    
    
    public static void getBuyerId(String nick) {
    	Long buyerId = BuyerIdApi.findBuyerId(nick);
    	
    	renderText(buyerId + "dds");
    }

    
    public static void setDailiModeAndOrderId(int mode, Long oid) {
    	if(mode != 1 && mode != 2 && mode != 3) {
    		renderText("模式不正确");
    	}
    	if(oid == null || oid <= 0L) {
    		renderText("订单ID为空");
    	}
    	TMConfigs.DAILIMODE = mode;
    	if(mode == 1) {
    		// 如果是999
    		ProxyDaili999API.order999Id = oid.toString();
    		renderText("已经更新mode为999，以及订单ID");
    	} else if(mode == 2) {
    		// 如果是panzhigao
    		// panzhigao家的订单号固定为czn123，不用更改，只需要续费
    		renderText("已经更新为panzhigao, 订单号固定为czn123，不用更改，只需要续费");
    	} else {
    		// 如果是ProxyXuehaiAPI
    		// 这个已经不用啦
    		renderText("ProxyXuehaiAPI已经被抛弃啦");
    	}
    	
    }
    
    
    public static void getCurrentModeAndOid() {
    	int mode = TMConfigs.DAILIMODE;
    	String oid = StringUtils.EMPTY;
    	if(mode == 1) {
    		oid = ProxyDaili999API.order999Id;
    	} else if(mode == 2) {
    		oid = "czn123";
    	}
    	renderText("current mode = " + mode + " and oid = " + oid);
    }
    
    
    public static void getDaili999Oid() {
    	
    	renderText("daili999 oid = " + ProxyDaili999API.order999Id);
    }
    
    
    public static void CommentLists() {
    	/*int badCount = DefenderAction.countComments(0L, CommentType.Negative)
                + DefenderAction.countComments(0L, CommentType.Neutral);
    	renderText(badCount);*/
    }


    public static void getBadTradeRateWithTid(Long tid) {
    	User user = getUser();
    	Long end = System.currentTimeMillis();
    	Long start = end - DateUtil.DAY_MILLIS * 60;
    	
    	List<TradeRate> tmpRes = new TradeRateApi.TraderatesGet(user, tid).call();

        log.warn("[GetBadTradeRate]trade rate size [" + tmpRes + "]");
    	renderJSON(JsonUtil.getJson(tmpRes));
    }
    
    
    public static void testCommentList(String userNick) {
    	int netural = DefenderAction.countComments(0L, userNick, 3);
    	int bad = DefenderAction.countComments(0L, userNick, 4);
    	renderText("netural = " + netural + " and bad = " + bad);
    }
    
    
    public static void testCommentListSpider() {
    	
    	List<String> nicks = new ArrayList<String>();
    	List<String> result = new ArrayList<String>();
    	nicks.add("楚之小南");
    	nicks.add("clorest510");
    	nicks.add("开放个平台");
    	nicks.add("qnxvkihfucr");
    	nicks.add("862328858qq");
    	nicks.add("jirounan6");
    	nicks.add("一二三开心6");
    	nicks.add("ll718521");
    	nicks.add("东营熊恋");
    	nicks.add("lu135533");
    	nicks.add("无尢静坐");
    	nicks.add("小曹霞儿");
    	nicks.add("gao1102605653");
    	nicks.add("baobao12716");
    	nicks.add("吴龟延");
    	nicks.add("youmairu");
    	nicks.add("微笑50329");
    	nicks.add("520阿瑟柯南道尔");
    	nicks.add("淘来淘去耶耶");
    	nicks.add("蒲公英的约定yd");
    	nicks.add("我们约会吧8800");
    	nicks.add("wdlc10001");
    	nicks.add("沉溺情海jing");

    	for(String userNick : nicks) {
    		int netural = DefenderAction.countComments(0L, userNick, 3);
    		int bad = DefenderAction.countComments(0L, userNick, 4);
    		result.add(netural + "" + bad);
    	}
    	
    	
    	renderJSON(JsonUtil.getJson(result));
    }
    
    
    public static void checkRegExp() {
    	String content = "dsjaklj(djkdjas)(djkdjas)djasklj[dsak][dsak]【差评防御师】";
    	content = content.replaceAll("\\(", "");
    	log.info(content);
        content = content.replaceAll("\\)", "");log.info(content);
        content = content.replaceAll("\\[", "");log.info(content);
        content = content.replaceAll("\\]", "");log.info(content);
        content = content.replaceAll("【差评防御师】", "");
        log.info(content);
    }
    
    
    public static void getIsJudgedByTid(Long tid) {
    	if(tid == null || tid <= 0L) {
    		renderFailedJson("订单号为空");
    	}
    	String key = "_defenderClose" +tid;
        Boolean object = (Boolean) Cache.get(key);
        renderJSON(JsonUtil.getJson(object));
    }
    
    
    public static void setCPStaffByCompany(String companyName) {
    	if(StringUtils.isEmpty(companyName)) {
    		renderFailedJson("请输入公司名");
    	}
    	User user = getUser();
    	if(user == null) {
    		renderFailedJson("用户不存在或已过期");
    	}
    	CPStaff chiefStaff = CPStaff.findByName(companyName);
    	if(chiefStaff == null || !chiefStaff.isSuperAdmin()) {
    		renderFailedJson("您选择的外包商不合法");
    	}
    	String sellerNick = user.getUserNick();
    	render("/CPEctocyst/confirm.html", sellerNick, companyName);
    }
    
    
    public static void setCPStaff(Long chiefId) {
    	if(chiefId == null || chiefId < 0L) {
    		renderFailedJson("请输入正确的外包商");
    	}
    	User user = getUser();
    	if(user == null) {
    		renderFailedJson("用户不存在或已过期");
    	}
    	CPStaff chiefStaff = CPStaff.findById(chiefId);
    	if(chiefStaff == null || !chiefStaff.isSuperAdmin()) {
    		renderFailedJson("您选择的外包商不合法");
    	}
    	String sellerNick = user.getUserNick();
    	String companyName = chiefStaff.getName();
    	render("/CPEctocyst/confirm.html", sellerNick, companyName);
    }
    
    
    public static void reConfirm(String companyName) {
    	if(StringUtils.isEmpty(companyName)) {
    		renderFailedJson("亲请选择要授权的公司名");
    	}
    	CPStaff cpStaff = CPStaff.findByName(companyName);
    	if(cpStaff == null || !cpStaff.isSuperAdmin()) {
    		renderFailedJson("亲选择的公司不合法");
    	}
    	User user = getUser();
    	if(user == null) {
    		renderFailedJson("亲请先登陆好评助手");
    	}
    	SellerToStaff sellerToStaff = new SellerToStaff(user.getId(), user.getUserNick(), -1L, StringUtils.EMPTY, cpStaff.getId()
    			, companyName);
    	
    	Boolean isSuccess = sellerToStaff.jdbcSave();
    	if(!isSuccess) {
    		renderFailedJson("设置失败，请重试或联系客服");
    	}
    	renderSuccessJson("设置成功");
    }
    
    
    public static void getChiefStaffDetailCache() {
    	List<ChiefStaffDetail> details = (List<ChiefStaffDetail>) Cache.get(ChiefStaffDetailPre);
    	renderJSON(JsonUtil.getJson(details));
    }

    
    public static final int chiefStaffLimit = 7;
    public static String ChiefStaffDetailPre = "ChiefStaffDetailPre";
    public static void getChiefStaffDetailList() {
    	List<ChiefStaffDetail> details = new ArrayList<ChiefStaffDetail>();
    	details = (List<ChiefStaffDetail>) Cache.get(ChiefStaffDetailPre);
    	if(!CommonUtils.isEmpty(details)) {
    		renderJSON(JsonUtil.getJson(details));
    	}
    	details = ChiefStaffDetail.findAllDetails();
    	List<ChiefStaffDetail> toShow = new ArrayList<ChiefStaffDetail>();
    	if(!CommonUtils.isEmpty(details)) {
    		for(ChiefStaffDetail detail : details) {
        		if(detail == null) {
        			continue;
        		}
        		detail.setAcceptNum((int) SellerToStaff.countByChiefName(detail.getCompanyName(), StringUtils.EMPTY, StringUtils.EMPTY));
        		if("鑫盛网络服务公司".equals(detail.getCompanyName())) {
        			detail.setAcceptNum(detail.getAcceptNum() + 260);
        		}
        		if("沐煦修改中差评团队".equals(detail.getCompanyName())) {
        			detail.setAcceptNum(detail.getAcceptNum() + 150);
        		}
        		if("腾飞售后客服团队".equals(detail.getCompanyName())) {
        			detail.setAcceptNum(detail.getAcceptNum() + 200);
        		}
        		if("锋杀淘中差评".equals(detail.getCompanyName())) {
        			detail.setAcceptNum(detail.getAcceptNum() + 400);
        		}
        		if("歪歪改评电子商务工作室".equals(detail.getCompanyName())) {
        			detail.setAcceptNum(detail.getAcceptNum() + 200);
        		}
        		if("淘易优网络科技有限公司".equals(detail.getCompanyName())) {
        			detail.setAcceptNum(detail.getAcceptNum() + 200);
        		}
        		if("淘专家删评工作室".equals(detail.getCompanyName())) {
        			detail.setAcceptNum(detail.getAcceptNum() + 300);
        		}
        		if("腾飞售后客服团队".equals(detail.getCompanyName())) {
        			detail.setAcceptNum(detail.getAcceptNum() + 100);
        		}
        		if(detail.getAcceptNum() >= 10) {
        			toShow.add(detail);
        		}
    		}
    		Collections.sort(toShow, acceptNumComparator);
    		Cache.set(ChiefStaffDetailPre, toShow, "1h");
    		
    		renderJSON(JsonUtil.getJson(toShow));
    	}
    	
    	renderJSON(JsonUtil.getJson(details));
    }

    public static Comparator<ChiefStaffDetail> acceptNumComparator = new Comparator<ChiefStaffDetail>() {

        @Override
        public int compare(ChiefStaffDetail o1, ChiefStaffDetail o2) {
            int score1 = o1.getAcceptNum();
            int score2 = o2.getAcceptNum();

            if (score2 <= score1) {
                return -1;
            } else {
                return 1;
            }

        }
    };
    
    
    public static void initCPChiefStaff() {
    	new CPStaff("xinsheng", "123456", "", "",
                CPStaff.TYPE.DX, CPStaff.STATUS.NORMAL, CPStaff.Role.SUPERADMIN).save();
    	new CPStaff("longtai", "123456", "", "",
                CPStaff.TYPE.DX, CPStaff.STATUS.NORMAL, CPStaff.Role.SUPERADMIN).save();
    	new CPStaff("xinhong", "123456", "", "",
                CPStaff.TYPE.DX, CPStaff.STATUS.NORMAL, CPStaff.Role.SUPERADMIN).save();
    	new CPStaff("yixin", "123456", "", "",
                CPStaff.TYPE.DX, CPStaff.STATUS.NORMAL, CPStaff.Role.SUPERADMIN).save();
    	new CPStaff("baihe", "123456", "", "",
                CPStaff.TYPE.DX, CPStaff.STATUS.NORMAL, CPStaff.Role.SUPERADMIN).save();
    	new CPStaff("wukong", "123456", "", "",
                CPStaff.TYPE.DX, CPStaff.STATUS.NORMAL, CPStaff.Role.SUPERADMIN).save();
    	new CPStaff("taozhuanjia	", "123456", "", "",
                CPStaff.TYPE.DX, CPStaff.STATUS.NORMAL, CPStaff.Role.SUPERADMIN).save();
    	new CPStaff("bangbangtao", "123456", "", "",
                CPStaff.TYPE.DX, CPStaff.STATUS.NORMAL, CPStaff.Role.SUPERADMIN).save();
    	new CPStaff("zhongchaping", "123456", "", "",
                CPStaff.TYPE.DX, CPStaff.STATUS.NORMAL, CPStaff.Role.SUPERADMIN).save();
    }
    
    
    public static void getHashByCompanyName(String companyName) {
    	if(StringUtils.isEmpty(companyName)) {
    		renderFailedJson("要加密的公司名为空");
    	}
    	String passwordSalt = Codec.encodeBASE64(Codec.UUID()).substring(0, 16);
        String passwordHash = Codec.hexSHA1(String.format("{%s} salt={%s}", companyName, passwordSalt));
        renderSuccessJson(passwordSalt + passwordHash);
    }
    
    /**
     * SHA: 每家外包公司，都有一个根据公司名生成的SHA（SHA加密）
     * 这里相当于授权链接，根据SHA找到对名的外包公司名
     * 然后跳转到授权页面
     */
    
    public static void deSHA(String SHA) {
    	if(StringUtils.isEmpty(SHA)) {
    		renderFailedJson("链接不正确，请重试或联系客服");
    	}
    	String userName = PlayUtil.getCookieString(request, "login-user");
        if (!StringUtils.isEmpty(userName)) {
            renderFailedJson("亲，您是外包商，不能授权啊！！");
        }
    	User user = getUser();
    	if(user == null) {
    		renderFailedJson("用户不存在或已过期");
    	}
    	SellerToStaff sellerToStaff = SellerToStaff.findByUserId(user.getId());
    	if(sellerToStaff != null) {
    		renderFailedJson("亲已经授权过外包商了，如需取消，请联系客服");
    	}
    	List<ChiefStaffDetail> details = (List<ChiefStaffDetail>) Cache.get(ChiefStaffDetailPre);
    	if(CommonUtils.isEmpty(details)) {
    		details = ChiefStaffDetail.findAllDetails();
    		if(CommonUtils.isEmpty(details)) {
        		renderFailedJson("发生地震，外包公司都不见啦");
        	}
        	Cache.set(ChiefStaffDetailPre, details, "1h");
    	}
    	log.info("companyName size is " + details.size());
    	
    	
    	// 开始解密
    	String salt = SHA.substring(0, 16);
        String hash = SHA.substring(16);
    	String companyName = StringUtils.EMPTY;
    	for(ChiefStaffDetail detail : details) {
    		log.info("companyName = " + detail.getCompanyName());
    		String tmpHash = Codec.hexSHA1(String.format("{%s} salt={%s}", detail.getCompanyName(), salt));
    		if(StringUtils.equals(hash, tmpHash)) {
    			log.info("companyName equal");
    			companyName = detail.getCompanyName();
    			break;
    		}
    	}
    	log.info("real companyName is " + companyName);
    	CPStaff chiefStaff = CPStaff.findByName(companyName);
    	if(chiefStaff == null) {
    		renderFailedJson("您选择的外包商不合法");
    	}
    	String sellerNick = user.getUserNick();
    	render("/CPEctocyst/confirm.html", sellerNick, companyName);
    }
    
    
    public static void updateChiefStaffDetailCache() {
    	List<ChiefStaffDetail> details = ChiefStaffDetail.findAllDetails();
		if(CommonUtils.isEmpty(details)) {
    		renderFailedJson("发生地震，外包公司都不见啦");
    	}
		for(ChiefStaffDetail detail : details) {
    		if(detail == null) {
    			continue;
    		}
    		detail.setAcceptNum((int) SellerToStaff.countByChiefName(detail.getCompanyName(), StringUtils.EMPTY, StringUtils.EMPTY));
    	}
		Collections.sort(details, acceptNumComparator);
		Cache.set(ChiefStaffDetailPre, details, "1h");
    }
    
    
    public static void deleteCPSTaffCache(String name) {
    	if(StringUtils.isEmpty(name)) {
    		renderFailedJson("用户名为空");
    	}
    	Cache.delete(CPStaff.CPSTaffCache + name.trim());
    }
    
    public static void testSmsAction() {
    	User user = getUser();
    	if(user == null) {
    		renderFailedJson("用户未登陆");
    	}
    	List<String> phones = new ArrayList<String>();
    	phones.add("13656676326");
    	phones.add("13656676700");
    	renderJSON(JsonUtil.getJson(SmsAction.batchSendSms(user, phones, "测试代码")));
    }







    public static void findPage(int pn,int ps){

        if(SendMsgLog.isLoginSend(request)){
            TMResult res = new TMResult("哎呀，亲，你还没有登陆，请先登陆！");
            renderJSON(JsonUtil.getJson(res));
        }

        PageOffset po = new PageOffset(pn, ps);
        List<SendMsgLog> msgLogList=SendMsgLog.findPageByUserId(SendMsgLog.getSendId(request), pn, ps);

        int count = SendMsgLog.countByParams(SendMsgLog.getSendId(request));

        TMResult res = new TMResult(msgLogList, count, po);

        renderJSON(JsonUtil.getJson(res));
    }
	
	public static void showSendMsgLog(int pn, int ps, Long oid){

		if(SendMsgLog.isLoginSend(request)){
			TMResult res = new TMResult("哎呀，亲，你还没有登陆，请先登陆！");
			renderJSON(JsonUtil.getJson(res));
		}
		
		User user = getUser();
		
		PageOffset po = new PageOffset(pn, ps);
		List<RateSmsSendLog> sendedMsgLogList = RateSmsSendLog.findSendedMsgByOid(oid, user.getId(), po);
		int count = RateSmsSendLog.countSendedMsgByOid(oid, user.getId());
		
		TMResult res = new TMResult(sendedMsgLogList, count, po);

		renderJSON(JsonUtil.getJson(res));
	}

	public static void showReceiveMsgLog(int pn, int ps, Long oid){

		if(SendMsgLog.isLoginSend(request)){
			TMResult res = new TMResult("哎呀，亲，你还没有登陆，请先登陆！");
			renderJSON(JsonUtil.getJson(res));
		}
		
		User user = getUser();
		
		PageOffset po = new PageOffset(pn, ps);
		List<RateSmsReceiveLog> receivedMsgLogList = RateSmsReceiveLog.findByOid(oid, user.getId(), po);
		// 更新状态为已查看
		if(!CommonUtils.isEmpty(receivedMsgLogList)) {
			for (RateSmsReceiveLog receivedMsgLog : receivedMsgLogList) {
				if(receivedMsgLog.status) {
					continue;
				}
				receivedMsgLog.setStatus(true);
				boolean isSuccess = receivedMsgLog.jdbcSave();
				if(!isSuccess) {
					log.error("更新回复短信状态失败，Id" + receivedMsgLog.getId());
					continue;
				}
			}
		}
		
		int count = RateSmsReceiveLog.countReceivedMsgByOid(oid, user.getId());
		
		TMResult res = new TMResult(receivedMsgLogList, count, po);

		renderJSON(JsonUtil.getJson(res));
	}

    public static void sendMsg(String content,String msgInfoStrings){

        //确认短信格式是否合法
        validationMsg(content,msgInfoStrings);
        
        content = content.replaceAll("\\(", "");
        content = content.replaceAll("\\)", "");
        content = content.replaceAll("\\[", "");
        content = content.replaceAll("\\]", "");
        content = content.replaceAll("【差评防御师】", "");

        List<String> phones = new ArrayList();
        String[] msgInfoarr = msgInfoStrings.split("#");
        if(msgInfoarr == null || msgInfoarr.length <= 0) {
        	renderText("格式错误");
        }
        for (String msgInfos : msgInfoarr) {
            String[] msgInfo = msgInfos.split(",");
            if(msgInfo == null || msgInfo.length != 3) {
            	continue;
            }
            phones.add(msgInfo[1]);
        }

        List<SmsAction.SmsSendRet> smsSendRets = SmsAction.batchSendSms(getUser(), phones, content);

        int successNum = 0, errorNum = 0;
        for (int i=0;i<smsSendRets.size();i++) {
            SmsAction.SmsSendRet s=smsSendRets.get(i);
            String[] msgInfo=msgInfoarr[i].split(",");
            if (s.equals(SmsAction.SmsSendRet.SUCCESS)) {
                successNum += 1;
            } else {
                errorNum += 1;
            }

            //插入短信发送 Log
            boolean logIsSuccess= SendMsgLog.insertLog(msgInfo[0],msgInfo[1],msgInfo[2],System.currentTimeMillis(),s.isSuccess(),content,SendMsgLog.getSendId(request),s.getMsg());
            if(!logIsSuccess){
                log.info("insert send msg log error, class name is SkinDefender.java");
            }

        }
        renderText("本次共发送 "+smsSendRets.size()+"条短信,其中"+successNum+"条短信发送成功，"+errorNum+"条短信发送失败");
    }
	
	public static void getRateSmsRemainCount() {
		User user = getUser();
		long remainCount = SmsSendCount.countSmsSendUsed(user.getId());
		renderText(remainCount);
	}
	
	public static void sendRateMsg(String content, String msgInfoStrings){
		
		//确认短信格式是否合法
		validationMsg(content,msgInfoStrings);
		
		User user = getUser();
		
		content = content.replaceAll("\\(", "");
		content = content.replaceAll("\\)", "");
		content = content.replaceAll("\\[", "");
		content = content.replaceAll("\\]", "");
		content = content.replaceAll("【差评防御师】", "");

		Map<Long, String> map = new HashMap<Long, String>();
		String[] msgInfoarr = msgInfoStrings.split("#");
		if(msgInfoarr == null || msgInfoarr.length <= 0) {
			renderText("入参错误");
		}
		for (String msgInfos : msgInfoarr) {
			String[] msgInfo = msgInfos.split(",");
			if(msgInfo == null || msgInfo.length != 2) {
				continue;
			}
			map.put(Long.parseLong(msgInfo[0]), msgInfo[1]);
		}
		
		// 获取短信签名用的店铺名称
		String cacheKey = user.getId() + "_shopName";
		String shopName = (String) Cache.get(cacheKey);
		if(StringUtils.isEmpty(shopName)) {
			Shop shop = new ShopApi.ShopGet(user.getUserNick()).call();
			if(shop == null){
				shopName = "评价";
			}
			shopName = shop.getTitle();
			Cache.set(cacheKey, shopName, "24h");
		}
		
		List<SmsAction.SmsSendRet> smsSendRets = SmsAction.batchSendRateSms(user, map, content, shopName);
		
		int successNum = 0, errorNum = 0;
		String errorMsg = StringUtils.EMPTY;
		for (int i = 0; i < smsSendRets.size(); i ++) {
			SmsAction.SmsSendRet s = smsSendRets.get(i);
			if (s.equals(SmsAction.SmsSendRet.SUCCESS)) {
				successNum += 1;
			} else {
				errorNum += 1;
				errorMsg = s.getMsg();
			}
		}
		renderText("本次共发送 " + smsSendRets.size() + " 条短信</br>其中" + successNum + "条短信发送成功，" + errorNum + "条短信发送失败!</br>" + errorMsg);
	}

    //检查短信格式是否合法
    private static void validationMsg(String content,String msgInfoStrings){
        if(SendMsgLog.isLoginSend(request)){
            renderText("亲，你还没有登陆，不可以发送短信哦！");
        }else if(content.isEmpty()){
            renderText("亲，发送短信失败，短信内容不能为空哦！");
        }else if(msgInfoStrings.isEmpty()){
            renderText("亲，发送短信失败，收件人为空哦！");
        }
    }



    //该方法为测试方法，实际并不会发送短信，但是你可以认为他发送了短信，因为除了发送短信的这个操作，其他的结果，和算法全部和发送短信是相同的
    public static void testSendMsg(String content,String msgInfoStrings){

        //确认短信格式是否合法
        validationMsg(content,msgInfoStrings);

        //测试
        msgInfoStrings="夏阳,18682541751,tb518669_44#骆扬帆,13556226540,骆扬帆#董灿东,18031605004,dongcandong_521#";

        List<String> phones = new ArrayList();
        String[] msgInfoarr = msgInfoStrings.split("#");
        for (String msgInfos : msgInfoarr) {
            String[] msgInfo = msgInfos.split(",");
            phones.add(msgInfo[1]);
        }

//        List<String> testPhones = new ArrayList();
//        String testcontent = "我是柯常青，这是一条测试短信";
//        testPhones.add("13819497938");
//        testPhones.add("15258822303");
//        List<SmsAction.SmsSendRet> smsSendRets = SmsAction.batchSendSms(getUser(), phones, content);
        List<SmsAction.SmsSendRet> smsSendRets = new ArrayList<SmsAction.SmsSendRet>();
        smsSendRets.add(SmsAction.SmsSendRet.SUCCESS);
        smsSendRets.add(SmsAction.SmsSendRet.CONTENT_EMPTY);
        smsSendRets.add(SmsAction.SmsSendRet.CONTENT_EMPTY);


//    listLog.addrName = addrName;
//    listLog.addrPhone = addrPhone;
//    listLog.buyernick = buyernick;
//    listLog.sendTime = sendTime;
//    listLog.isSuccess = isSuccess;
//    listLog.msgInfo = msgInfo;
//    listLog.senderId = senderId;

        int successNum = 0, errorNum = 0;
        for (int i=0;i<smsSendRets.size();i++) {
            SmsAction.SmsSendRet s=smsSendRets.get(i);
            String[] msgInfo=msgInfoarr[i].split(",");
            if (s.equals(SmsAction.SmsSendRet.SUCCESS)) {
                successNum += 1;
            } else {
                errorNum += 1;
            }

            //插入短信发送 Log
            boolean logIsSuccess= SendMsgLog.insertLog(msgInfo[0],msgInfo[1],msgInfo[2],System.currentTimeMillis(),s.isSuccess(),content,SendMsgLog.getSendId(request),s.getMsg());
            if(!logIsSuccess){
                log.info("insert send msg log error, class name is SkinDefender.java");
            }

        }
        renderText("本次共发送 "+smsSendRets.size()+"条短信,其中"+successNum+"条短信发送成功，"+errorNum+"条短信发送失败");
    }

    //模拟向数据库中插入20条测试数据
//    public static void testInsertLog(){
//        //插入短信发送 Log
//        for(int i=0;i<20;i++){
//            boolean logIsSuccess= SendMsgLog.insertLog("测试"+i,"18682541751","tb518669_44",System.currentTimeMillis(),true,"随便写测试短信",getSendId(),"这是测试");
//        }
//    }
    
    public static void testJianghu(String nick) {
    	if(StringUtils.isEmpty(nick)) {
    		renderFailedJson("传入的用户名为空");
    	}
    	Long buyerId = BuyerIdApi.findBuyerId(nick);
    }
    
    public static void testNewDefense(String nick) {
    	if(StringUtils.isEmpty(nick)) {
    		renderFailedJson("传入的用户名为空");
    	}
    	UserIdNick idnick = UserIdNick.findOrCreate(nick);
        UserRateInfo info = UserRateSpiderAction.spiderUserRateById(idnick);
        renderJSON(JsonUtil.getJson(info));
    }
    
    public static void testJuxinSms() {
    	String phone = "13656676326", content = "小云朵19262: 淘宝买家呆呆小花痴给您中评，请及时登录淘宝查看，购买宝贝正品男包 单肩包男 牛皮男士包 商务男";
    	SMSSendShengTai.sendIndustrySms(phone, content, "速推科技44");
    	renderText("chenggong");
    }
    
    public static void testSZYaoJia() {
    	User user = getUser();
    	SendInfo sendInfo = SZYaoJiaProvider.SZYaojiaSmsSend(user.getId(), System.currentTimeMillis(), "13656676326", "固体鞋油: 亲，我们在关闭疑似差评用户(tb5705302_2011)的订单时出现错误，请及时登录淘宝查看。退订回t");
    }
    
    public static void getSZYaoJiaBalance() {
    	renderText(SZYaoJiaProvider.getSZYaoJiaBanlance());
    }
    
    public static void getSmsReply() {
    	renderText(SZYaoJiaProvider.getSZYaoJiaReply());
    }
    
    public static void testGuotong() {
    	SendInfo sendInfo = SMSSendMeiLian.sendMarketingSms("13656676326", 
    			"亲爱的施文宁静，很抱歉打扰到您，亲对我们店里购买的宝贝有任何不满意，请联系我们旺旺[clorest510]，我们会热情为您服务；满意请给5分好评哦~谢谢。",
    			"小猪芭啦啦");
    	renderJSON(JsonUtil.getJson(sendInfo));
    }
    
    public static void testUsersGetApi(String nick) {
    	if(StringUtils.isEmpty(nick)) {
    		renderFailedJson("传入的用户名为空");
    	}
    	com.taobao.api.domain.User user = UserRateSpiderAction.getUserByApi(nick);
    	renderJSON(JsonUtil.getJson(user));
    }
    
    public static void testDefense(String nick) {
    	if(StringUtils.isEmpty(nick)) {
    		renderFailedJson("传入的用户名为空");
    	}
    	User user = getUser();
    	DefenderOption option = DefenderOptionDao.findByUserId(user.getIdlong());
    	com.taobao.api.domain.User tb_user = UserRateSpiderAction.getUserByApi(nick);
        DefenderRet ret = DefenderAction.checkNewDefender(option, tb_user, (Long) 37123789L);
    }
}
