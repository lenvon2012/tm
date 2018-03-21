
package controllers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import com.alibaba.fastjson.serializer.*;
import com.google.common.collect.Lists;
import com.taobao.api.ApiException;
import com.taobao.api.SecretException;
import com.taobao.api.TaobaoClient;

import jdp.JdpModel.JdpTradeModel;
import job.shop.CrawlSellerDSRJob;
import job.writter.TradeWritter;
import models.Status;
import models.comment.CommentConf;
import models.comment.CommentsFailed;
import models.defense.DefenseWarn;
import models.defense.ServiceGroup;
import models.order.OrderDisplay;
import models.shop.ShopScorePlay;
import models.traderate.OrderPlay;
import models.urgeComment.NoteCareLog;
import models.urgeComment.NoteCareTemplate;
import models.sms.SmsSendLog;
import models.trade.TradeDisplay;
import models.traderate.TradeRatePlay;
import models.traderatesms.RateSmsReceiveLog;
import models.traderatesms.RateSmsSendLog;
import models.user.SellerDSR;
import models.user.User;
import models.user.UserOP;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Play;
import result.TMResult;
import secure.SimulateRequestUtil;
import spider.DSRSpider;
import transaction.MapIterator;
import utils.CommonUtil;
import utils.DateUtil;
import utils.ExcelUtil;
import utils.TaobaoUtil;
import actions.WangwangOnlineAction;
import bustbapi.JMSApi.JushitaJmsUserAdd;
import bustbapi.JMSApi.JushitaJmsUserGet;
import bustbapi.TBApi;
import bustbapi.TMTradeApi;
import bustbapi.TradeMemoApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;
import com.taobao.api.domain.Order;
import com.taobao.api.domain.TmcUser;
import com.taobao.api.domain.Trade;
import com.taobao.api.request.TradesSoldGetRequest;
import com.taobao.api.response.TradesSoldGetResponse;

import configs.TMConfigs;
import configs.TMConfigs.TMWarning;
import controllers.TmSecurity.SecurityType;
import dao.UserDao;
import dao.defense.DefenseWarnDao;
import dao.trade.OrderDisplayDao;
import dao.trade.TradeDisplayDao;
import dao.trade.TradeRatePlayDao;

/**
 * 淘掌柜-好评王-差评防御-评价管理
 *
 * @author zrb rate-ok, rate-normal, rate-bad
 */
public class SkinComment extends TMController {

    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final SimpleDateFormat dateSDF = new SimpleDateFormat("yyyy-MM-dd");

    public static HashSet<String> smsBanWords = new HashSet<String>();

    static {
        initSmsBanWords();
    }

    public static void testJmsUserAdd() {
        User user = getUser();
        JushitaJmsUserAdd api = new JushitaJmsUserAdd(user);
        Boolean success = api.call();
        if(!success) {
            renderText(api.getSubErrorMsg());
        }
        renderText("添加成功");
    }

    public static void testJmsUserGet() {
        User user = getUser();
        JushitaJmsUserGet api = new JushitaJmsUserGet(user);
        TmcUser tmcUser = api.call();
        if(tmcUser == null) {
            renderText(api.getSubErrorMsg());
        }
        renderText(String.valueOf(tmcUser));
    }

    static void initSmsBanWords() {
        try {
            List<String> readLines = FileUtils
                    .readLines(new File(TMConfigs.configDir, "init/smsBanWords.txt"), "utf-8");
            if (CommonUtils.isEmpty(readLines)) {
                log.error("[initSmsBanWords] read file no lines!!  init/smsBanWords.txt");
                return;
            }
            smsBanWords = new HashSet<String>();
            for (String line : readLines) {
                if (!StringUtils.isEmpty(line)) {
                    smsBanWords.add(line);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 自动评价
     */
    public static void index() {
        render("skincomment/comment.html");
    }

    public static void commentConf() {
        User user = getUser();
        CommentConf conf = CommentConf.findByUserId(user.getId());
        if (conf == null) {
            conf = new CommentConf(user.getId(), user.getUserNick(), "欢迎再次光临!@#");
            conf.jdbcSave();
        }
        conf.setBadCommentNotice(!user.isBadCommentNoticeOff());
        conf.setBadCommentBuyerSms(user.isBadCommentBuyerSmsOn());
        conf.setDefenseNotice(!user.isDefenseNoticeSmsOff());
        renderJSON(JsonUtil.getJson(conf));
    }

    public static void testCommentConf() {
        String msg = CommentConf.DEAFULT_BADCOMMENT_BUYER_SMS;
        msg = msg.replaceAll("#买家#", "的就算卡车");
        msg = msg.replaceAll("\\[#卖家#\\]", "觉得克拉司机");
        msg = msg.replaceAll("#评价#", "大赛的djsk");
        renderText(msg);
    }

    /**
     * 短信提醒通知
     * @param commentType
     * @param commentTime
     * @param badCommentNotice
     * @param badCommentBuyerSms
     * @param badCommentMsg
     * @param badCommentReply
     * @param defenseNotice
     */
    public static void updateConf(Long commentType, Long commentTime, boolean badCommentNotice,
                                  boolean badCommentBuyerSms, String badCommentMsg, boolean badCommentReply, boolean defenseNotice) {

        if (commentTime == null || (commentType > 0 && (commentTime <= 0 || commentTime >= 15))) {
            renderJSON(TMResult.renderMsg("请修改错误后提交<br><br>填写的天数必须为(大于0且小于15)的整数！谢谢"));
        }

        if (badCommentBuyerSms == true && !StringUtils.isEmpty(badCommentMsg)) {
            String word = filterBanWords(badCommentMsg);
            if (!StringUtils.isEmpty(word)) {
                renderJSON(TMResult.renderMsg("<br><br>亲填写的短信内容包含违禁词【" + word + "】<br><br>请修改后提交！谢谢"));
            }
        }

        User user = getUser();
        CommentConf conf = CommentConf.findByUserId(user.getId());
        if (conf == null) {
            conf = new CommentConf(user.getId(), user.getUserNick(), "欢迎再次光临!@#");
        }
        System.out.println(commentTime);
        conf.setCommentType(commentType);
        conf.setCommentDays(commentTime);
        conf.setBadCommentMsg(badCommentMsg);
        if (badCommentReply == true) {
            conf.setCommentRate(1L);
        } else {
            conf.setCommentRate(0L);
        }
        conf.jdbcSave();

        user.setBadCommentNoticeOff(!badCommentNotice);
        user.setBadCommentBuyerSmsOn(badCommentBuyerSms);
        user.setDefenseNoticeSmsOff(!defenseNotice);
        user.jdbcSave();

        if (badCommentNotice == true || badCommentBuyerSms == true) {
//            TaobaoUtil.permitByUser(user);
            TaobaoUtil.permitTMCUser(user);
        }
        TMResult renderMsg = TMResult.renderMsg("设置修改成功");
        renderMsg.isOk = true;
        renderJSON(renderMsg);
    }

    static String filterBanWords(String msg) {
        if (CommonUtils.isEmpty(smsBanWords)) {
            initSmsBanWords();
        }
        for (String word : smsBanWords) {
            if (msg.contains(word) && !StringUtils.isEmpty(word)) {
                return word;
            }
        }
        return null;
    }

    /**
     * 好评助手备注查询
     */
    public static void getRemarkSet() {
        User user = getUser();

        UserOP userOP = UserOP.findByUserId(user.getId());
        if(userOP == null) {
            userOP = new UserOP(user.getId(), false, 0, true);
            userOP.jdbcSave();
        }

        renderSuccess("", userOP.isUpdateRemarkToTB());
    }

    /**
     * 好评助手备注设置
     */
    public static void updateRemarkSet(Boolean updateRemarkToTB) {
        User user = getUser();

        UserOP userOP = UserOP.findByUserId(user.getId());
        if(userOP == null) {
            renderJSON(TMResult.renderMsg("未找到相关配置！请刷新后重试！"));
        }

        userOP.setUpdateRemarkToTB(updateRemarkToTB);
        boolean success = userOP.jdbcSave();
        if(!success) {
            if(updateRemarkToTB) {
                renderJSON(TMResult.renderMsg("开启备注同步失败！"));
            } else {
                renderJSON(TMResult.renderMsg("关闭备注同步失败！"));
            }
        }

        if(updateRemarkToTB) {
            renderJSON(TMResult.renderMsg("开启备注同步成功！"));
        } else {
            renderJSON(TMResult.renderMsg("关闭备注同步成功！"));
        }
    }

    /**
     * 好评助手备注保存
     */
    public static void saveRemark(Long tid, String remark) {
        User user = getUser();
        boolean updated = TradeRatePlayDao.updateRemark(user.getId(), tid, remark.trim());
        if (updated == false) {
            renderJSON(TMResult.renderMsg("备注保存失败！"));
        }

        // 判断是否需要将备注上传至淘宝
        UserOP userOP = UserOP.findByUserId(user.getId());
        if(userOP == null) {
            userOP = new UserOP(user.getId(), false, 0, true);
            userOP.jdbcSave();
        }
        if(!userOP.isUpdateRemarkToTB()) {
            renderJSON(TMResult.renderMsg("备注保存成功！"));
        }

        Boolean res = new TradeMemoApi.TradeMemoUpdate(user, tid, remark).call();
        if (res == Boolean.FALSE) {
            renderJSON(TMResult.renderMsg("备注更新失败！"));
        }
        renderJSON(TMResult.renderMsg("备注更新成功！"));
    }

    /**
     * 历史评价目前可能不需要
     */
    public static void commendHistory() {
    }

    public static void commentNormal() {
        render("skincomment/commentNormal.html");
    }

    public static void commentBad() {
        render("skincomment/commentBad.html");
    }

    public static void remindHistory() {
        render("skincomment/commentremindHistory.html");
    }

    public static void noticeConfig() {
        render("skincomment/commentNoticeConfig.html");
    }

    public static void noticeLog() {
        render("skincomment/commentNoticeLog.html");
    }

    public static void commentStatus() {
        render("skincomment/commentStatus.html");
    }

    public static void manual() {
        render("skincomment/commentManual.html");
    }

    public static void commentUrge() {
        render("skincomment/commentUrge.html");
    }

    public static void warn() {
        render("skincomment/defensewarn.html");
    }

    public static void shopScore() {
        render("skincomment/shopScore.html");
    }

    public static void isOn() {
        User user = getUser();
        boolean isOn = user.isSendDefenseMsgOn();
        renderSuccess("", isOn);
    }

    public static void turnOn() {
        User user = getUser();
        user.setSendDefenseMsgOn(true);
        user.jdbcSave();
//        TaobaoUtil.permitByUser(user);
        TaobaoUtil.permitTMCUser(user);
        renderSuccess("短信通知开启成功", "");
    }

    public static void turnOff() {
        User user = getUser();
        user.setSendDefenseMsgOn(false);
        user.jdbcSave();
        renderSuccess("短信通知关闭成功", "");
    }

    public static void queryDefenseWarns() {
        User user = getUser();
        List<DefenseWarn> warnList = DefenseWarnDao.findByUserId(user.getId());

        renderSuccess("", warnList);
    }

    public static void addOrModifyWarn(Long warnId, String telephone, String remark) {
        User user = getUser();
        DefenseWarn defenseWarn = null;
        boolean isSave = true;
        // 保存的
        if (warnId == null || warnId.longValue() <= 0) {
            isSave = true;
            long count = DefenseWarnDao.countByUserId(user.getId());
            if (count >= TMWarning.Max_Phone_Number) {
                renderError("亲，手机号码最多可以设置" + TMWarning.Max_Phone_Number + "个!");
            }
            defenseWarn = new DefenseWarn(user.getId(), telephone, remark, System.currentTimeMillis());
        } else {
            isSave = false;
            defenseWarn = DefenseWarnDao.findById(user.getId(), warnId);
            if (defenseWarn == null) {
                renderError("系统出现意外，找不到对应的手机号码！");
            }
            defenseWarn.setRemark(remark);
            defenseWarn.setTelephone(telephone);
        }
        boolean isSuccess = defenseWarn.jdbcSave();
        if (isSuccess == true) {
            if (isSave == true) {
                renderSuccess("手机号码保存成功", "");
            } else {
                renderSuccess("手机号码修改成功", "");
            }
        } else {
            if (isSave == true) {
                renderSuccess("手机号码保存失败！", "");
            } else {
                renderSuccess("手机号码修改失败！", "");
            }
        }
    }

    public static void deleteWarn(Long warnId) {
        User user = getUser();
        boolean isSuccess = DefenseWarnDao.deleteById(user.getId(), warnId);
        if (isSuccess == false) {
            renderError("删除失败！");
        } else {
            renderSuccess("删除成功", "");
        }
    }

    public static void findSmsLog(String buyerNick, String startTime, String endTime, int pn, int ps) {
        User user = getUser();
        long userId = user.getIdlong();
        PageOffset po = new PageOffset(pn, ps);

        Long startTs = null;
        Long endTs = null;
        if (!StringUtils.isEmpty(startTime)) {
            startTime = startTime.trim();
            try {
                startTs = dateSDF.parse(startTime).getTime();
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        if (!StringUtils.isEmpty(endTime)) {
            endTime = endTime.trim();
            try {
                endTs = dateSDF.parse(endTime).getTime();
                // 要加上一天的时间
                endTs += 24L * 3600L * 1000L;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        List<SmsSendLog> list = SmsSendLog.findSmsSendLogByRules(userId, buyerNick, startTs, endTs, po);
        int count = (int) SmsSendLog.countSmsSendLogByRules(userId, buyerNick, startTs, endTs);

        TMResult tmRes = new TMResult(list, count, po);
        renderJSON(JsonUtil.getJson(tmRes));
    }

    // 未评价订单
    /**
     *
     * @param type
     *            0:买家未评，卖家未评； 1：买家已评，卖家未评； 2：买家未评，卖家已评； 3：买家卖家都评
     * @param tradeId
     * @param buyerNick
     * @param startTime
     * @param endTime
     * @param pn
     * @param ps
     * @throws IOException
     */
    public static void queryTradeRecord(int type, Long tradeId, String buyerNick, String startTime, String endTime,
                                        int pn, int ps) throws IOException {
        renderMockFileInJsonIfDev("tradecomment.json");

        User user = getUser();
        long userId = user.getIdlong();
        PageOffset po = new PageOffset(pn, ps);

        Long startTs = null;
        Long endTs = null;
        if (!StringUtils.isEmpty(startTime)) {
            startTime = startTime.trim();
            try {
                startTs = dateSDF.parse(startTime).getTime();
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        if (!StringUtils.isEmpty(endTime)) {
            endTime = endTime.trim();
            try {
                endTs = dateSDF.parse(endTime).getTime();
                // 要加上一天的时间
                endTs += 24L * 3600L * 1000L;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        Boolean buyerRate = null;
        if ((type & 1) > 0) {
            // 评价
            buyerRate = false;
        } else if ((type & 2) > 0) {
            buyerRate = true;
        }
        Boolean sellerRate = null;
        if (((type >> 2) & 1) > 0) {
            sellerRate = false;
        } else if (((type >> 2) & 2) > 0) {
            sellerRate = true;
        }

        int status = Status.TRADE_STATUS.TRADE_FINISHED.ordinal();
        List<OrderDisplay> orderList = OrderDisplayDao.searchWithArgs(userId, tradeId, buyerNick, status, buyerRate,
                sellerRate, startTs, endTs, po);
        int count = (int) OrderDisplayDao.countWithArgs(userId, tradeId, buyerNick, status, buyerRate, sellerRate,
                startTs, endTs);

        HashSet<Long> oids = new HashSet<Long>();
        for (OrderDisplay order : orderList) {
            oids.add(order.oid);
        }

        List<TradeRatePlay> tradeRateList = TradeRatePlayDao.findByUserIdOidSet(userId, oids);

        List<OrderRate> list = new ArrayList<OrderRate>();
        for (OrderDisplay order : orderList) {
            if (order == null) {
                continue;
            }
            OrderRate orderRate = new OrderRate(order);
            if (!CommonUtils.isEmpty(tradeRateList)) {
                for (TradeRatePlay rate : tradeRateList) {
                    if (order.oid.longValue() == rate.getOid()) {
                        orderRate.setTradeRate(rate);
                        break;
                    }
                }
            }
            list.add(orderRate);
        }

        TMResult tmRes = new TMResult(list, count, po);
        renderJSON(JsonUtil.getJson(tmRes));
    }





    /**
     * 催评管理 搜索订单评价信息
     * @param type 0:买家未评，卖家未评； 1：买家已评，卖家未评； 2：买家未评，卖家已评； 3：买家卖家都评 低一位表示买家,低二位表示卖家
     * @param online 旺旺在线状态
     * @param buyerNick 买家昵称
     * @param phone 联系电话
     * @param numIid 宝贝ID
     * @param startTime 日期区间
     * @param endTime 日期区间
     * @param isShowAll 是否获取完整的数据
     * @param pn pageNumber
     * @param ps pageSize
     */
    public static void searchTradeRecord(int type ,Integer online , String buyerNick, String phone, String numIid,
                                         String startTime, String endTime, Boolean isShowAll,Integer dispatchId, int pn, int ps) {
        //获取买家卖家是否已经评价参数
        Boolean buyerRate, sellerRate;
        buyerRate = (type & 1) > 0 ? true : false;
        sellerRate = (type & 1 << 1) > 0 ? true : false;
        //获取开始时间和结束时间参数
        Long startTs, endTs;
        startTs = parseDateToTime(startTime);
        endTs = parseDateToTime(endTime);
        //获取用户ID参数
        User user = getUser();
        long userId = user.getIdlong();
        //获取分页信息参数
        PageOffset po = new PageOffset(pn, ps);
        //获取订单状态参数
        int status = Status.TRADE_STATUS.TRADE_FINISHED.ordinal();
        //查询订单评价信息分页 查询订单总数
        Page<OrderRate> page = doSearchTradeRecord(online, buyerNick, phone, numIid, isShowAll, buyerRate, sellerRate, startTs, endTs, user, userId, po, status, dispatchId);
        //返回响应数据
        TMResult tmRes = new TMResult(page.getList(), page.getTotalRow(), po);

        renderJSON(utils.JsonUtil.toJson(tmRes));
    }


    //催评管理 搜索订单评价信息
    private static Page<OrderRate> doSearchTradeRecord(Integer online, String buyerNick, String phone, String numIid,
                                                       Boolean isShowAll, Boolean buyerRate, Boolean sellerRate, Long startTs,
                                                       Long endTs, User user, long userId, PageOffset po, int status, Integer dispatchId) {

        //加密phone字段 Nick字段
        String encryptPhone = StringUtils.EMPTY;
        String encryptNick = StringUtils.EMPTY;
        try {
            if (!StringUtils.isEmpty(phone))
                encryptPhone = TmSecurity.encrypt(phone, SecurityType.PHONE, user);
            if (!StringUtils.isEmpty(buyerNick))
                encryptNick = TmSecurity.encrypt(buyerNick, SecurityType.SIMPLE, user);
        } catch (SecretException e) {
            e.printStackTrace();
        }
        phone = encryptPhone;
        buyerNick = encryptNick;

        Integer count = OrderDisplayDao.countByCondition(userId, buyerNick, phone, numIid,
                status, buyerRate, sellerRate, startTs, endTs, dispatchId).intValue();
        //查询订单
        List<OrderDisplay> orderList = null;
        if (po != null) {
            orderList = OrderDisplayDao.findListByCondition(userId, buyerNick, phone, numIid,
                    status, buyerRate, sellerRate, startTs, endTs, dispatchId, po);
        } else {
            orderList = OrderDisplayDao.findListByCondition(userId, buyerNick, phone, numIid,
                    status, buyerRate, sellerRate, startTs, endTs, dispatchId);
        }


        //根据online参数过滤旺旺不在线的订单
        if (online == 1) {
            //因为订单表没有是否旺旺在线的标识 所以不能在数据库中分页 这里获取所有订单数据过滤后做分页操作
            orderList = OrderDisplayDao.findListByCondition(userId, buyerNick, phone, numIid,
                    status, buyerRate, sellerRate, startTs, endTs, dispatchId);
            filterOfflineOrderList(orderList);
            count = orderList.size();
            if (po != null) //为空表示不分页
                orderList = paginate(orderList, po);
        }

        //根据订单信息获取其对应的评价信息等等  合并成OrderRate
        List<OrderRate> orderRateList = getOrderRates(isShowAll, user, orderList);

        //解密订单表里联系方式等字段
        for (OrderRate orderRate : orderRateList) {
            OrderDisplay order = orderRate.getOrder();
            try {
                // 解密order中buyerNick,phone,receiverName,buyerAlipayNo字段
                if(order != null) {
                    order.setTidStr(String.valueOf(order.getTid()));
                    order.setOidStr(String.valueOf(order.getOid()));
                    order.setBuyerNick(TmSecurity.decrypt(order.getBuyerNick(), SecurityType.SIMPLE, user));
                    order.setPhone(TmSecurity.decrypt(order.getPhone(), SecurityType.PHONE, user));
                    order.setReceiverName(TmSecurity.decrypt(order.getReceiverName(), SecurityType.SIMPLE, user));
                    order.setBuyerAlipayNo(TmSecurity.decrypt(order.getBuyerAlipayNo(), SecurityType.SIMPLE, user));
                }
            } catch (SecretException e) {
                e.printStackTrace();
            }
        }


        if (po != null)
            return new Page<OrderRate>(orderRateList, po.getPn(), po.getPs(), count);
        else
            return new Page<OrderRate>(orderRateList, 0, count, count);
    }

    /**
     * 获取订单信息集合对应的评价信息等等 合并成OrderRate
     * @param isShowAll 是否获取短信发送情况
     * @param user 用户对象
     * @param orderList 订单集合
     * @return OrderRate集合
     */
    private static List<OrderRate> getOrderRates(Boolean isShowAll, User user, List<OrderDisplay> orderList) {
        long userId = user.getIdlong();
        //根据订单号查询用户评价记录
        HashSet<Long> oids = new HashSet<Long>();
        if (orderList != null)
        for (OrderDisplay order : orderList) oids.add(order.oid);
        //如果搜索的是用户未评价的记录，则tradeRateList大小应该是0
        List<TradeRatePlay> tradeRateList = TradeRatePlayDao.findByUserIdOidSet(userId, oids);
        //合并用户评价记录(TradeRatePlay)和订单信息(OrderDisplay)
        List<OrderRate> orderRateList = new ArrayList<OrderRate>();
        if (orderList != null)
            for (OrderDisplay order : orderList) {
                if (order != null) {
                    TradeRatePlay tradeRatePlay = findTradeRatePlayByOid(order.getOid(), tradeRateList);
                    OrderRate orderRate = new OrderRate(order, tradeRatePlay);
                    orderRateList.add(orderRate);
                }
            }
        //根据订单号查询对应短信发送情况信息
        if(isShowAll)
            for (OrderRate orderRate : orderRateList) {
                Long oid = orderRate.getOrder().getOid();
                // 获取短信发送条数&短信回复条数&是否有未读回复短信
                int rateSmsSendCount = RateSmsSendLog.countSendedMsgByOid(oid, userId);
                int rateSmsReceiveCount = RateSmsReceiveLog.countReceivedMsgByOid(oid, userId);
                int unReadCount = RateSmsReceiveLog.countByOidAndStatus(oid, false, userId);
                orderRate.setRateSmsSendCount(rateSmsSendCount);
                orderRate.setRateSmsReceiveCount(rateSmsReceiveCount);
                orderRate.setUnReadCount(unReadCount);
            }
        //获取买家支付宝账号
        new BuyerAlipayNoEnsuer(orderRateList, user).call();
        //分组信息获取
        Map<Long, String> dispatchMap = ServiceGroup.queryUserGroupsMap(userId);
        for (OrderRate orderRate : orderRateList) {
            TradeRatePlay tradeRate = orderRate.getTradeRate();
            OrderDisplay order = orderRate.getOrder();
            Long dispatchId;
            if (!CommonUtils.isEmpty(dispatchMap) && (dispatchId = tradeRate.getDispatchId()) != null) {
                String groupName = dispatchMap.get(dispatchId);
                if (!StringUtils.isEmpty(groupName))
                    tradeRate.setGroupName(groupName);
            }
            if (!CommonUtils.isEmpty(dispatchMap) && (order.dispatchId) != null) {
                String groupName = dispatchMap.get(order.dispatchId);
                if (!StringUtils.isEmpty(groupName))
                    order.groupName = groupName;
            }

        }

        return orderRateList;
    }


    static class Page<T> {
        private List<T> list;
        private int pageNumber;
        private int pageSize;
        private int totalPage;
        private int totalRow;

        public Page(List<T> list, int pageNumber, int pageSize, int totalRow) {
            this.list = list;
            this.pageNumber = pageNumber;
            this.pageSize = pageSize;
            this.totalPage = totalRow > 0 ? (totalRow - 1) / pageSize + 1 : 0;
            this.totalRow = totalRow;
        }

        public List<T> getList() {
            return list;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public int getPageSize() {
            return pageSize;
        }

        public int getTotalPage() {
            return totalPage;
        }

        public int getTotalRow() {
            return totalRow;
        }

        public boolean isFirstPage() {
            return pageNumber == 1;
        }

        public boolean isLastPage() {
            return pageNumber == totalPage;
        }

    }



    /**
     * 集合分页
     * @param orderList 集合
     * @param po 分页信息
     * @return 集合
     */
    private static List<OrderDisplay> paginate(List<OrderDisplay> orderList, PageOffset po) {
        int fromIndex = (po.getPn() - 1) * po.getPs();
        int toIndex = po.getPn() * po.getPs();
        int listSize = orderList.size();

        if (listSize > fromIndex && listSize <= toIndex) {

            return orderList.subList(fromIndex, listSize);
        } else if (listSize >= toIndex) {

            return orderList.subList(fromIndex, toIndex);
        } else {

            return Lists.newArrayList();
        }

    }




    /**
     * 返回tradeRateList中oid对应的TradeRatePlay
     * @param oid 订单ID
     * @param tradeRateList 评价记录信息集合
     * @return 没找到的话,返回空的TradeRatePlay对象
     */
    private static TradeRatePlay findTradeRatePlayByOid(long oid, List<TradeRatePlay> tradeRateList) {
        if (tradeRateList != null)
        for (TradeRatePlay rate : tradeRateList) {
            if (oid == rate.getOid())
                return rate;
        }
        return new TradeRatePlay();
    }

    /**
     * 过滤掉orderList中旺旺状态为不在线的OrderDisplay
     * @param orderList 订单信息集合
     */
    private static void filterOfflineOrderList(List<OrderDisplay> orderList) {
        Iterator<OrderDisplay> iterator = orderList.iterator();
        while (iterator.hasNext()) {
            OrderDisplay next = iterator.next();
            String buyerNick = next.getBuyerNick();
            try {
                buyerNick = TmSecurity.decrypt(buyerNick, SecurityType.SIMPLE, getUser());
            } catch (SecretException e) {
                e.printStackTrace();
            }
            //根据买家昵称判断旺旺在线状态
            if (!WangwangOnlineAction.isOnline(buyerNick)) {
                iterator.remove(); //不在线就从集合中移除
            }
        }
    }

    /**
     * 将字符串的时间(yyyy-MM-dd HH:mm:ss)转换成Long类型时间戳
     * @param time 时间字符串
     * @return 失败返回null
     */
    private static Long parseDateToTime (String time){
        if (!StringUtils.isEmpty(time)) {
            time = time.trim();
            try {
                return sdf.parse(time).getTime();
            } catch (Exception ex) {
                try {
                    return dateSDF.parse(time).getTime();
                } catch (ParseException e) {
                    log.error(ex.getMessage(), ex);
                }
            }
        }
        return 0L;
    }

    /**
     * 催评管理-订单分组
     * @param oids 订单号 13,2131,213...
     * @param dispatchId 分组号
     */
    public static void dispatchCommentUrgeServiceGroup(String oids, Long dispatchId) {
        User user = getUser();
        if (StringUtils.isEmpty(oids) || dispatchId == null || dispatchId < 0) {
            renderFailedJson("请选择正确的分组和订单后提交");
        }
        String[] oidArr = oids.split(",");
        if (oidArr.length == 0) {
            renderFailedJson("请选择正确的订单后提交");
        }
        HashSet<Long> oidSet = new HashSet<Long>();
        for (int i = 0; i < oidArr.length; i++) {
            if (StringUtils.isEmpty(oidArr[i])) {
                continue;
            }
            oidSet.add(Long.valueOf(oidArr[i]));
        }

        Integer updated = OrderPlay.updateDispatchIdByOidSet(user.getIdlong(), oidSet, dispatchId);

        if (updated != oidSet.size()) {
            renderFailedJson("更新数据失败，请刷新再提交下，谢谢");
        }
        renderSuccess("分配客服成功！", null);

    }

    private static SimpleDateFormat sdfMMdd = new SimpleDateFormat("MMdd");

    /**
     * 催评管理-备注保存
     * @param oid 订单号
     * @param remark 备注
     * @param tid 备注上传淘宝用到
     */
    public static void saveRemarkCommentUrge(Long oid, String remark, Long tid) {
        User user = getUser();
        if (org.apache.commons.lang.StringUtils.isEmpty(remark)) {
            remark = "";
        }
        if(remark.length() > 255) {
            remark = remark.substring(0, 250);
        }
        remark += "[" + sdfMMdd.format(System.currentTimeMillis()) + "] ";

        Boolean updated = OrderPlay.updateRemarkByOid(user.getIdlong(), oid, remark);
        if (updated == false) {
            renderJSON(TMResult.renderMsg("备注保存失败！"));
        }

        // 判断是否需要将备注上传至淘宝
        UserOP userOP = UserOP.findByUserId(user.getId());
        if(userOP == null) {
            userOP = new UserOP(user.getId(), false, 0, true);
            userOP.jdbcSave();
        }
        if(!userOP.isUpdateRemarkToTB()) {
            renderJSON(TMResult.renderMsg("备注保存成功！"));
        }

        Boolean res = new TradeMemoApi.TradeMemoUpdate(user, tid, remark).call();
        if (res == Boolean.FALSE) {
            renderJSON(TMResult.renderMsg("备注更新失败！"));
        }

        renderJSON(TMResult.renderMsg("备注更新成功！"));
    }

    /**
     * 催评管理-导出excel文件
     * @param type 0:买家未评，卖家未评； 1：买家已评，卖家未评； 2：买家未评，卖家已评； 3：买家卖家都评 低一位表示买家,低二位表示卖家
     * @param online 旺旺在线状态
     * @param buyerNick 买家昵称
     * @param phone 联系电话
     * @param numIid 宝贝ID
     * @param startTime 日期区间
     * @param endTime 日期区间
     * @param isShowAll 是否获取完整的数据
     * @param pn pageNumber
     * @param ps pageSize
     */
    public static void exportCommentUrgeExcel(int type ,Integer online , String buyerNick, String phone, String numIid,
                                              String startTime, String endTime, Boolean isShowAll,Integer dispatchId, int pn, int ps) {
        //获取买家卖家是否已经评价参数
        Boolean buyerRate, sellerRate;
        buyerRate = (type & 1) > 0 ? true : false;
        sellerRate = (type & 1 << 1) > 0 ? true : false;
        //获取开始时间和结束时间参数
        Long startTs, endTs;
        startTs = parseDateToTime(startTime);
        endTs = parseDateToTime(endTime);
        //获取用户ID参数
        User user = getUser();
        long userId = user.getIdlong();
        //获取分页信息参数
        PageOffset po = new PageOffset(pn, ps);
        //获取订单状态参数
        int status = Status.TRADE_STATUS.TRADE_FINISHED.ordinal();
        List<String[]> records = new ArrayList<String[]>();
        String fields = "分配专员,已发短信数,已回短信数,宝贝标题,宝贝ID,订单号,订单创建时间,订单结束时间,成交价,原价,买家姓名,联系电话,卖家旺旺昵称,支付宝账号,备注";
        fields = "分配专员,宝贝标题,宝贝ID,订单号,订单创建时间,订单结束时间,成交价,原价,买家姓名,联系电话,卖家旺旺昵称,支付宝账号,备注";
        List<OrderRate> orderRateList = doSearchTradeRecord(online, buyerNick, phone, numIid, isShowAll,
                buyerRate, sellerRate, startTs, endTs, user, userId, null, status, dispatchId).getList();

        for (OrderRate orderRate : orderRateList) {
            String[] record = new String[15];
            record = new String[13];
            OrderDisplay order = orderRate.getOrder();
            TradeRatePlay rate = orderRate.getTradeRate();
            int i = 0;
            record[i++] = order.groupName;
            //record[i++] = String.valueOf(orderRate.getRateSmsSendCount());
            //record[i++] = String.valueOf(orderRate.getRateSmsReceiveCount());
            record[i++] = order.getTitle();
            record[i++] = order.getNumIid().toString();
            record[i++] = order.getTid().toString();
            record[i++] = order.getCreated().toString();
            record[i++] = order.getEndTime().toString();
            record[i++] = String.valueOf(order.getTotalFee());
            record[i++] = String.valueOf(order.getPrice());
            record[i++] = order.getReceiverName();
            record[i++] = order.getPhone();
            record[i++] = order.getBuyerNick();
            record[i++] = order.getBuyerAlipayNo();
            record[i++] = rate.getRemark();

            records.add(record);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        String fileName = Play.tmpDir.getPath() + "/[导出]" + user.getUserNick() + "_" + sdf.format(new Date(startTs)) + "-" + sdf.format(new Date(endTs))
                + ".xls";
        String sheetName = "列表";

        ExcelUtil.writeToExcel(records, fields, sheetName, fileName);

        File file = new File(fileName);
        renderBinary(file);
    }

    public static void exportCommentUrgeExcel2(int type ,Integer online , String buyerNick, String phone, String numIid,
                                              String startTime, String endTime, Boolean isShowAll,Integer dispatchId, int pn, int ps) {
        //获取买家卖家是否已经评价参数
        Boolean buyerRate, sellerRate;
        buyerRate = (type & 1) > 0 ? true : false;
        sellerRate = (type & 1 << 1) > 0 ? true : false;
        //获取开始时间和结束时间参数
        Long startTs, endTs;
        startTs = parseDateToTime(startTime);
        endTs = parseDateToTime(endTime);
        //获取用户ID参数
        User user = getUser();
        long userId = user.getIdlong();
        //获取分页信息参数
        PageOffset po = new PageOffset(pn, ps);
        //获取订单状态参数
        int status = Status.TRADE_STATUS.TRADE_FINISHED.ordinal();
        List<String[]> records = new ArrayList<String[]>();
        String fields = "分配专员,已发短信数,已回短信数,宝贝标题,宝贝ID,订单号,订单创建时间,订单结束时间,成交价,原价,买家姓名,联系电话,卖家旺旺昵称,支付宝账号,备注";
        List<OrderRate> orderRateList = doSearchTradeRecord(online, buyerNick, phone, numIid, isShowAll,
                buyerRate, sellerRate, startTs, endTs, user, userId, null, status, dispatchId).getList();

        for (OrderRate orderRate : orderRateList) {
            String[] record = new String[15];
            OrderDisplay order = orderRate.getOrder();
            TradeRatePlay rate = orderRate.getTradeRate();
            int i = 0;
            record[i++] = order.groupName;
            record[i++] = String.valueOf(orderRate.getRateSmsSendCount());
            record[i++] = String.valueOf(orderRate.getRateSmsReceiveCount());
            record[i++] = order.getTitle();
            record[i++] = order.getNumIid().toString();
            record[i++] = order.getTid().toString();
            record[i++] = order.getCreated().toString();
            record[i++] = order.getEndTime().toString();
            record[i++] = String.valueOf(order.getTotalFee());
            record[i++] = String.valueOf(order.getPrice());
            record[i++] = order.getReceiverName();
            record[i++] = order.getPhone();
            record[i++] = order.getBuyerNick();
            record[i++] = order.getBuyerAlipayNo();
            record[i++] = rate.getRemark();

            records.add(record);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        String fileName = Play.tmpDir.getPath() + "/[导出]" + user.getUserNick() + "_" + sdf.format(new Date(startTs)) + "-" + sdf.format(new Date(endTs))
                + ".xls";
        String sheetName = "列表";

        ExcelUtil.writeToExcel(records, fields, sheetName, fileName);

        File file = new File(fileName);
        renderBinary(file);
    }



    // 外包系统需要，计算每个好评助手卖家现在的中差评数
    public static void countSellerBadRate() {
        User user = getUser();
        long userId = user.getIdlong();

        Long endTs = System.currentTimeMillis();
        Long startTs = endTs - com.ciaosir.client.utils.DateUtil.DAY_MILLIS;

        int count = (int) TradeRatePlayDao.countWithArgs(userId, null, StringUtils.EMPTY, 4, startTs, endTs, -1L,
                null);
        renderText(count + "");
    }

    public static void queryTradeRate(Long tradeId, String buyerNick, Long numIid, String startTime, String endTime, int pn, int ps,
                                      int rate, int online, Long dispatchId, String phone, Boolean isShowAll) throws IOException, InterruptedException,
            ExecutionException {

//        renderMockFileInJsonIfDev("tradecomment.json");

        User user = getUser();
        long userId = user.getIdlong();

        if(isShowAll == null) {
            isShowAll = false;
        }

        Long startTs = null;
        Long endTs = null;
        if (!StringUtils.isEmpty(startTime)) {
            startTime = startTime.trim();
            if(startTime.indexOf(":") < 0) {
                startTime += " 00:00:00";
            }
            try {
                startTs = sdf.parse(startTime).getTime();
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        if (!StringUtils.isEmpty(endTime)) {
            endTime = endTime.trim();
            if(endTime.indexOf(":") < 0) {
                endTime += " 00:00:00";
            }
            try {
                endTs = sdf.parse(endTime).getTime();
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        PageOffset po = null;
        if (online == 1) {
            po = new PageOffset(pn, ps * 10);
        } else {
            po = new PageOffset(pn, ps);
        }

        if (!StringUtils.isEmpty(buyerNick)) {
            buyerNick = buyerNick.trim();
        }

//        long t1 = System.currentTimeMillis();
//        log.warn("test starts... userId=" + userId);

        HashSet<Long> targetOids = new HashSet<Long>();
        if (!StringUtils.isEmpty(phone)) {
            // 加密phone字段
            String encryptPhone = StringUtils.EMPTY;
            try {
                encryptPhone = TmSecurity.encrypt(phone, SecurityType.PHONE, user);
            } catch (SecretException e) {
                e.printStackTrace();
            }
            List<OrderDisplay> targetOrders = OrderDisplayDao.findByUserIdPhone(userId, phone.trim(), StringUtils.isEmpty(encryptPhone) ? phone : encryptPhone);
            // 御城河日志接入
            sendOrderLog(userId, "订单评价", SimulateRequestUtil.getOrderTid(targetOrders));
            if (CommonUtils.isEmpty(targetOrders)) {
                renderJSON(new TMResult(null, 0, po));
            }
            for (OrderDisplay orderDisplay : targetOrders) {
                targetOids.add(orderDisplay.getId());
            }
        }

//        long t2 = System.currentTimeMillis();
//        log.warn("test starts... userId=" + userId + "   takes: " + (t2 - t1) * 1.0 / 10);

        String encryptNick = StringUtils.EMPTY;
        if(!StringUtils.isEmpty(buyerNick)) {
            // 加密buyerNick字段
            try {
                encryptNick = TmSecurity.encrypt(buyerNick, SecurityType.SIMPLE, user);
            } catch (SecretException e) {
                e.printStackTrace();
            }
        }

        int count = (int) TradeRatePlayDao.countWithArgs(userId, tradeId, buyerNick, encryptNick, numIid, rate, startTs, endTs, dispatchId,
                targetOids);

//        long t3 = System.currentTimeMillis();
//        log.warn("test starts... userId=" + userId + "   takes: " + (t3 - t2) * 1.0 / 10);

        List<TradeRatePlay> tradeRateList = TradeRatePlayDao.searchWithArgs(userId, tradeId, buyerNick, encryptNick, numIid, rate, startTs,
                endTs, dispatchId, targetOids, po);

//        long t4 = System.currentTimeMillis();
//        log.warn("test starts... userId=" + userId + "   takes: " + (t4 - t3) * 1.0 / 10);

        // log.error("[queryTradeRate] " + tradeRateList);
        if (CommonUtils.isEmpty(tradeRateList)) {
            TMResult tmRes = new TMResult(null, count, po);
            renderJSON(JsonUtil.getJson(tmRes));
        }

        List<TradeRatePlay> tradeRateRes = new ArrayList<TradeRatePlay>();
        HashSet<Long> oids = new HashSet<Long>();
        for (TradeRatePlay tradeRate : tradeRateList) {
            if (online == 1) {
                boolean isOnline = WangwangOnlineAction.isOnline(tradeRate.getNick());
                if (isOnline == true) {
                    oids.add(tradeRate.getOid());
                    tradeRateRes.add(tradeRate);
                }
            } else {
                oids.add(tradeRate.getOid());
                tradeRateRes.add(tradeRate);
            }
        }
        // log.error("[queryTradeRate] oids: " + oids);
        List<OrderDisplay> orderList = OrderDisplayDao.findByUserIdOids(userId, oids);
        // 御城河日志接入
        sendOrderLog(userId, "订单评价", SimulateRequestUtil.getOrderTid(orderList));

        // log.error("[queryTradeRate] order: " + orderList);
        // if (CommonUtils.isEmpty(orderList)) {
        // TMResult tmRes = new TMResult(null, count, po);
        // renderJSON(JsonUtil.getJson(tmRes));
        // }

//        long t5 = System.currentTimeMillis();
//        log.warn("test starts... userId=" + userId + "   takes: " + (t5 - t4) * 1.0 / 10);

        Map<Long, String> dispatchMap = ServiceGroup.queryUserGroupsMap(userId);
            List<OrderRate> list = new ArrayList<OrderRate>();
            for (TradeRatePlay tradeRate : tradeRateRes) {
                if (tradeRate == null) {
                    continue;
                }

                if (!CommonUtils.isEmpty(dispatchMap)) {
                    String groupName = dispatchMap.get(tradeRate.getDispatchId());
                    if (!StringUtils.isEmpty(groupName)) {
                        tradeRate.setGroupName(groupName);
                    }
                }

            OrderRate orderRate = new OrderRate(tradeRate);

            if(isShowAll) {
                // 获取短信发送条数&短信回复条数&是否有未读回复短信
                long oid = tradeRate.getOid();
                int rateSmsSendCount = RateSmsSendLog.countSendedMsgByOid(oid, userId);
                int rateSmsReceiveCount = RateSmsReceiveLog.countReceivedMsgByOid(oid, userId);
                int unReadCount = RateSmsReceiveLog.countByOidAndStatus(oid, false, userId);
                orderRate.setRateSmsSendCount(rateSmsSendCount);
                orderRate.setRateSmsReceiveCount(rateSmsReceiveCount);
                orderRate.setUnReadCount(unReadCount);
            }

            if (CommonUtils.isEmpty(orderList)) {
                list.add(orderRate);
                continue;
            }
            for (OrderDisplay order : orderList) {
                if (order.oid.longValue() == tradeRate.getOid()) {
                    orderRate.setOrder(order);
                    break;
                }
            }
            list.add(orderRate);
        }

//        long t6 = System.currentTimeMillis();
//        log.warn("test starts... userId=" + userId + "   takes: " + (t6 - t5) * 1.0 / 10);

        List<FutureTask<Trade>> futureTasks = new ArrayList<FutureTask<Trade>>();
        for (OrderRate orderRate : list) {
            if (orderRate.getOrder() == null) {
                FutureTask<Trade> future = TMConfigs.getTradeApiUpdatePool().submit(
                        new TradeApiUpdateCaller(user, orderRate.getTradeRate().getTid()));
                futureTasks.add(future);
            }
        }

        if (!CommonUtils.isEmpty(futureTasks)) {
            List<Trade> toWriteTradeList = new ArrayList<Trade>();
            for (FutureTask<Trade> future : futureTasks) {
                Trade trade = future.get();
                if(trade != null) {
                    toWriteTradeList.add(trade);
                }
            }

            if (!CommonUtils.isEmpty(toWriteTradeList)) {
                TradeWritter.addTradeList(userId, System.currentTimeMillis(), toWriteTradeList);

                for (OrderRate orderRate : list) {
                    if (orderRate.getOrder() != null) {
                        continue;
                    }

                    for (Trade trade : toWriteTradeList) {
                        if (orderRate.getTradeRate().getTid() == trade.getTid()) {
                            List<Order> orders = trade.getOrders();
                            if (!CommonUtils.isEmpty(orders)) {
                                for (Order order : orders) {
                                    if (orderRate.getTradeRate().getOid() == order.getOid()) {
                                        OrderDisplay orderDisplay = new OrderDisplay(userId, trade, order);
                                        orderRate.setOrder(orderDisplay);
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }

        new BuyerAlipayNoEnsuer(list, user).call();

        for (OrderRate orderRate : list) {
            TradeRatePlay tradeRate = orderRate.getTradeRate();
            OrderDisplay order = orderRate.getOrder();

            try {
                // 解密tradeRate中nick字段
                if(tradeRate != null) {
                    tradeRate.setNick(TmSecurity.decrypt(tradeRate.getNick(), SecurityType.SIMPLE, user));
                    // tid,oid转String
                    tradeRate.setTidStr(String.valueOf(tradeRate.getTid()));
                    tradeRate.setOidStr(String.valueOf(tradeRate.getOid()));
                }
                // 解密order中buyerNick,phone,receiverName,buyerAlipayNo字段
                if(order != null) {
                    order.setBuyerNick(TmSecurity.decrypt(order.getBuyerNick(), SecurityType.SIMPLE, user));
                    order.setPhone(TmSecurity.decrypt(order.getPhone(), SecurityType.PHONE, user));
                    order.setReceiverName(TmSecurity.decrypt(order.getReceiverName(), SecurityType.SIMPLE, user));
                    order.setBuyerAlipayNo(TmSecurity.decrypt(order.getBuyerAlipayNo(), SecurityType.SIMPLE, user));
                }
            } catch (SecretException e) {
                e.printStackTrace();
            }
        }

        TMResult tmRes = new TMResult<List<OrderRate>>(list, count, po);
        renderJSON(JsonUtil.getJson(tmRes));
    }

    public static class TradeApiUpdateCaller implements Callable<Trade> {

        User user;

        Long tid;

        public TradeApiUpdateCaller(User user, Long tid) {
            this.user = user;
            this.tid = tid;
        }

        @Override
        public Trade call() throws Exception {
            // TODO Auto-generated method stub
            Trade trade = JdpTradeModel.fetchUserTrade(user.getUserNick(), tid);
            if (trade != null) {
                log.warn(" fix trade by rds success :" + user.toIdNick() + " For tid:" + tid);
                return trade;
            }
            trade = new TMTradeApi.GetFullTrade(user.getSessionKey(), tid).call();
            // 御城河日志接入
            sendTopLog(user.getId(), SimulateRequestUtil.TRADE_FULLINFO_GET, "tid=" + tid + "field=" + TMTradeApi.TRADE_FIELDS);
            if (trade != null) {
                log.warn(" fix trade by api success :" + user.toIdNick() + " For tid:" + tid);
            } else {
                log.warn(" fix trade fail ....:" + user.toIdNick() + " For tid:" + tid);
            }

            return trade;
        }

    }

    public static void exportTradeRate(Long tradeId, String buyerNick, String startTime, String endTime, int pn,
                                       int ps, int rate, int online, Long dispatchId, String phone) throws IOException, InterruptedException,
            ExecutionException {

        User user = getUser();
        long userId = user.getIdlong();

        Long startTs = null;
        Long endTs = null;
        if (!StringUtils.isEmpty(startTime)) {
            startTime = startTime.trim();
            try {
                startTs = dateSDF.parse(startTime).getTime();
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        if (!StringUtils.isEmpty(endTime)) {
            endTime = endTime.trim();
            try {
                endTs = dateSDF.parse(endTime).getTime();
                // 要加上一天的时间
                endTs += 24L * 3600L * 1000L;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        PageOffset po = new PageOffset(1, 5000);

        if (!StringUtils.isEmpty(buyerNick)) {
            buyerNick = buyerNick.trim();
        }

        HashSet<Long> targetOids = new HashSet<Long>();
        if (!StringUtils.isEmpty(phone)) {
            // 加密phone字段
            String encryptPhone = StringUtils.EMPTY;
            try {
                encryptPhone = TmSecurity.encrypt(phone, SecurityType.PHONE, user);
            } catch (SecretException e) {
                e.printStackTrace();
            }
            List<OrderDisplay> targetOrders = OrderDisplayDao.findByUserIdPhone(userId, phone.trim(), StringUtils.isEmpty(encryptPhone) ? phone : encryptPhone);

            if (CommonUtils.isEmpty(targetOrders)) {
                renderJSON(new TMResult(null, 0, po));
            }
            for (OrderDisplay orderDisplay : targetOrders) {
                targetOids.add(orderDisplay.getId());
            }
        }

        String encryptNick = StringUtils.EMPTY;
        if(!StringUtils.isEmpty(buyerNick)) {
            // 加密buyerNick字段
            try {
                encryptNick = TmSecurity.encrypt(buyerNick, SecurityType.SIMPLE, user);
            } catch (SecretException e) {
                e.printStackTrace();
            }
        }

        int count = (int) TradeRatePlayDao.countWithArgs(userId, tradeId, buyerNick, encryptNick, null, rate, startTs, endTs, dispatchId,
                targetOids);

        List<TradeRatePlay> tradeRateList = TradeRatePlayDao.searchWithArgs(userId, tradeId, buyerNick, encryptNick, null, rate, startTs,
                endTs, dispatchId, targetOids, po);

        // log.error("[queryTradeRate] " + tradeRateList);
        if (CommonUtils.isEmpty(tradeRateList)) {
            TMResult tmRes = new TMResult(null, count, po);
            renderJSON(JsonUtil.getJson(tmRes));
        }

        List<TradeRatePlay> tradeRateRes = new ArrayList<TradeRatePlay>();
        HashSet<Long> oids = new HashSet<Long>();
        for (TradeRatePlay tradeRate : tradeRateList) {
            if (online == 1) {
                boolean isOnline = WangwangOnlineAction.isOnline(tradeRate.getNick());
                if (isOnline == true) {
                    oids.add(tradeRate.getOid());
                    tradeRateRes.add(tradeRate);
                }
            } else {
                oids.add(tradeRate.getOid());
                tradeRateRes.add(tradeRate);
            }
        }
        List<OrderDisplay> orderList = OrderDisplayDao.findByUserIdOids(userId, oids);

        Map<Long, String> dispatchMap = ServiceGroup.queryUserGroupsMap(userId);

        List<OrderRate> list = new ArrayList<OrderRate>();
        for (TradeRatePlay tradeRate : tradeRateRes) {
            if (tradeRate == null) {
                continue;
            }

            if (!CommonUtils.isEmpty(dispatchMap)) {
                String groupName = dispatchMap.get(tradeRate.getDispatchId());
                if (!StringUtils.isEmpty(groupName)) {
                    tradeRate.setGroupName(groupName);
                }
            }

            OrderRate orderRate = new OrderRate(tradeRate);

            if (CommonUtils.isEmpty(orderList)) {
                list.add(orderRate);
                continue;
            }
            for (OrderDisplay order : orderList) {
                if (order.oid.longValue() == tradeRate.getOid()) {
                    orderRate.setOrder(order);
                    break;
                }
            }
            list.add(orderRate);
        }

        List<FutureTask<Trade>> futureTasks = new ArrayList<FutureTask<Trade>>();
        for (OrderRate orderRate : list) {
            if (orderRate.getOrder() == null) {
                FutureTask<Trade> future = TMConfigs.getTradeApiUpdatePool().submit(
                        new TradeApiUpdateCaller(user, orderRate.getTradeRate().getTid()));
                futureTasks.add(future);
            }
        }

        if (!CommonUtils.isEmpty(futureTasks)) {
            List<Trade> toWriteTradeList = new ArrayList<Trade>();
            for (FutureTask<Trade> future : futureTasks) {
                Trade trade = future.get();
                toWriteTradeList.add(trade);
            }

            if (!CommonUtils.isEmpty(toWriteTradeList)) {
                TradeWritter.addTradeList(userId, System.currentTimeMillis(), toWriteTradeList);

                for (OrderRate orderRate : list) {
                    if (orderRate.getOrder() != null) {
                        continue;
                    }

                    for (Trade trade : toWriteTradeList) {
                        if (orderRate.getTradeRate().getTid() == trade.getTid()) {
                            List<Order> orders = trade.getOrders();
                            if (!CommonUtils.isEmpty(orders)) {
                                for (Order order : orders) {
                                    if (orderRate.getTradeRate().getOid() == order.getOid()) {
                                        OrderDisplay orderDisplay = new OrderDisplay(userId, trade, order);
                                        orderRate.setOrder(orderDisplay);
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }

        new BuyerAlipayNoEnsuer(list, user).call();

        for (OrderRate orderRate : list) {
            TradeRatePlay tradeRate = orderRate.getTradeRate();
            OrderDisplay order = orderRate.getOrder();

            try {
                // 解密tradeRate中nick字段
                if(tradeRate != null) {
                    tradeRate.setNick(TmSecurity.decrypt(tradeRate.getNick(), SecurityType.SIMPLE, user));
                }
                // 解密order中buyerNick,phone,receiverName,buyerAlipayNo字段
                if(order != null) {
                    order.setBuyerNick(TmSecurity.decrypt(order.getBuyerNick(), SecurityType.SIMPLE, user));
                    order.setPhone(TmSecurity.decrypt(order.getPhone(), SecurityType.PHONE, user));
                    order.setReceiverName(TmSecurity.decrypt(order.getReceiverName(), SecurityType.SIMPLE, user));
                    order.setBuyerAlipayNo(TmSecurity.decrypt(order.getBuyerAlipayNo(), SecurityType.SIMPLE, user));
                }
            } catch (SecretException e) {
                e.printStackTrace();
            }
        }

        List<String[]> records = new ArrayList<String[]>();
        String fields = "分配专员,商品名称,买家昵称,原始评价,买家姓名,买家手机,买家支付宝,评价时间,修改时间,评价内容,备注";
        if (rate < 8) {
            fields = "分配专员,商品名称,买家昵称,评价,买家姓名,买家手机,买家支付宝,评价时间,到期时间,评价内容,备注";
        }

        for (OrderRate orderRate : list) {
            String[] record = new String[11];
            record[0] = orderRate.tradeRate.getGroupName();
            record[1] = orderRate.tradeRate.getItemTitle();
            record[2] = orderRate.tradeRate.getNick();

            int tRate = orderRate.tradeRate.getRate();
            if (tRate > 3) {
                tRate = (tRate >> 2);
            }
            if ((tRate & 3) == 2) {
                record[3] = "中评";
            } else if ((tRate & 3) == 3) {
                record[3] = "差评";
            }

            // －－－－－原来的record[6]是评价时间，　现在改成买家支付宝
            if (orderRate.getOrder() != null) {
                OrderDisplay order = orderRate.getOrder();
                record[4] = order.getReceiverName();
                record[5] = order.getPhone();
                record[6] = order.getBuyerAlipayNo() == null ? StringUtils.EMPTY : order.getBuyerAlipayNo();
            }

            record[7] = sdf.format(new Date(orderRate.tradeRate.getCreated()));
            if (rate < 8) {
                record[8] = calcTimeSpan(orderRate.tradeRate.getCreated());
            } else if (rate >= 8 && orderRate.tradeRate.getCreated() < orderRate.tradeRate.getUpdated()) {
                record[8] = sdf.format(new Date(orderRate.tradeRate.getUpdated()));
            }

            record[9] = orderRate.tradeRate.getContent();
            record[10] = orderRate.tradeRate.getRemark();

            records.add(record);
        }

        String fileName = Play.tmpDir.getPath() + "/[导出评价]" + user.getUserNick() + "_" + startTime + "-" + endTime
                + ".xls";

        String sheetName = "评价列表";

        ExcelUtil.writeToExcel(records, fields, sheetName, fileName);

        File file = new File(fileName);
        renderBinary(file);

    }

    static String spanFormat = "%d天，%d小时%d分钟%d秒";

    static String calcTimeSpan(Long created) {

        long timeSpan = (created + DateUtil.THIRTY_DAYS - System.currentTimeMillis()) / 1000;
        long secondSpan = timeSpan % 60;
        long minSpan = (timeSpan / 60) % 60;
        long hourSpan = (timeSpan / 3600) % 24;
        long daySpan = (timeSpan / (3600 * 24));

        return String.format(spanFormat, daySpan, hourSpan, minSpan, secondSpan);
    }

    public static class OrderRate {
        private User user;

        private OrderDisplay order;

        private TradeRatePlay tradeRate;
        //获取短信发送条数
        private int rateSmsSendCount;
        //短信回复条数
        private int rateSmsReceiveCount;
        //是否有未读回复短信
        private int unReadCount;

        @Override
        public String toString() {
            return "OrderRate [user=" + user.toIdNick() + ", order=" + order + ", tradeRate=" + tradeRate + "]";
        }

        public OrderRate() {

        }

        public OrderRate(OrderDisplay order) {
            this.order = order;
            this.user = UserDao.findById(order.getUserId());
        }

        public OrderRate(TradeRatePlay tradeRate) {
            this.tradeRate = tradeRate;
            this.user = UserDao.findById(tradeRate.getUserId());
        }

        public OrderRate(OrderDisplay order, TradeRatePlay tradeRate) {
            this.order = order;
            this.tradeRate = tradeRate;
            if (order.getUserId() != null) {
                this.user = UserDao.findById(order.getUserId());
            } else if (tradeRate.getUserId() != null) {
                this.user = UserDao.findById(tradeRate.getUserId());
            }
        }

        public OrderDisplay getOrder() {
            return order;
        }

        public void setOrder(OrderDisplay order) {
            this.order = order;
        }

        public TradeRatePlay getTradeRate() {
            return tradeRate;
        }

        public void setTradeRate(TradeRatePlay tradeRate) {
            this.tradeRate = tradeRate;
        }

        public int getRateSmsSendCount() {
            return rateSmsSendCount;
        }

        public void setRateSmsSendCount(int rateSmsSendCount) {
            this.rateSmsSendCount = rateSmsSendCount;
        }

        public int getRateSmsReceiveCount() {
            return rateSmsReceiveCount;
        }

        public void setRateSmsReceiveCount(int rateSmsReceiveCount) {
            this.rateSmsReceiveCount = rateSmsReceiveCount;
        }

        public int getUnReadCount() {
            return unReadCount;
        }

        public void setUnReadCount(int unReadCount) {
            this.unReadCount = unReadCount;
        }

    }

    public static class BuyerAlipayNoEnsuer implements Callable<Boolean> {
        List<OrderRate> list;

        public BuyerAlipayNoEnsuer(List<OrderRate> list) {
            super();
            this.list = list;
        }

        public BuyerAlipayNoEnsuer(List<OrderRate> list, User user) {
            super();
            this.list = list;
            this.user = user;
        }

//        Map<Long, OrderDisplay> noAlipayNoOrders = new HashMap<Long, OrderDisplay>();

        Map<Long, String> tidAlipayNo = new HashMap<Long, String>();

        List<OrderDisplay> noAlipayOrders = new ArrayList<OrderDisplay>();

        User user;

        Set<Long> notDoneTids = new HashSet<Long>();

        private void prepare() {

            for (OrderRate orderRate : list) {
                if (orderRate.user != null) {
                    user = orderRate.user;
                }

                OrderDisplay order2 = orderRate.getOrder();
                if (order2 == null) {
                    continue;
                }

                if (StringUtils.isEmpty(order2.getBuyerAlipayNo())) {
//                    noAlipayNoOrders.put(order2.getOid(), order2);
                    noAlipayOrders.add(order2);
                    notDoneTids.add(order2.getTid());
                }
            }

            log.info("[not done tids:]" + notDoneTids);
            log.warn(" no alipay orders size:" + noAlipayOrders.size());

        }

        private void fixAllAlipayNo() {
            log.warn(" tid alipay no :" + tidAlipayNo);
            for (OrderDisplay order2 : noAlipayOrders) {
                Long tid = order2.getTid();
                String alipayNo = tidAlipayNo.get(tid);
                order2.setBuyerAlipayNo(alipayNo);
            }
        }

        @Override
        public Boolean call() {
            log.info("[found curr list]" + list.size());
            if (CommonUtils.isEmpty(list)) {
                return Boolean.FALSE;
            }

            prepare();

            fetchByDb();

            if (CommonUtils.isEmpty(notDoneTids)) {
                fixAllAlipayNo();
                return Boolean.TRUE;
            }

            fetchByRds();

            if (CommonUtils.isEmpty(notDoneTids)) {
                fixAllAlipayNo();
                return Boolean.TRUE;
            }

            fetchByApi();

            fixAllAlipayNo();

            return Boolean.TRUE;
        }

        private void fetchByDb() {
            Map<Long, String> findTidsBuyerAlipayNo = TradeDisplayDao.findTidsBuyerAlipayNo(user.getId(), notDoneTids);
            findTidsBuyerAlipayNo = findTidsBuyerAlipayNo == null ? new HashMap<Long, String>() : findTidsBuyerAlipayNo;
            // 御城河日志接入
            sendOrderLog(getUser().getId(), "获取买家支付宝账号", new ArrayList<Long>(findTidsBuyerAlipayNo.keySet()));
            log.warn("[fix alipay no in db :]" + findTidsBuyerAlipayNo.size() + " for user:" + user.toIdNick());
            tidAlipayNo.putAll(findTidsBuyerAlipayNo);
            notDoneTids.removeAll(findTidsBuyerAlipayNo.keySet());
        }

        private void fetchByApi() {
            List<Long> doneTids = new ArrayList<Long>();
            for (Long tid : notDoneTids) {
                Trade trade = new TMTradeApi.GetFullTrade(user.getSessionKey(), tid).call();
                // 御城河日志接入
                sendTopLog(getUser().getId(), SimulateRequestUtil.TRADE_FULLINFO_GET, "field=" + TMTradeApi.TRADE_FIELDS + "tid=" + tid);
                if (trade == null || trade.getBuyerAlipayNo() == null) {
                    continue;
                }

                log.info("fix alipay no by api :" + tid + " for user:" + user.toIdNick());
                tidAlipayNo.put(trade.getTid(), trade.getBuyerAlipayNo());
                doneTids.add(tid);

                new TradeDisplay(user.getId(), System.currentTimeMillis(), trade).jdbcSave();
            }
            log.warn("skin comment done by api :" + doneTids);
            notDoneTids.removeAll(doneTids);
        }

        private void fetchByRds() {
            int fixByRdsNum = 0;
            List<Trade> fetchTrades = JdpTradeModel.fetchTrades(notDoneTids);
            for (Trade trade : fetchTrades) {
                if (trade.getBuyerAlipayNo() == null) {
                    continue;
                }

                fixByRdsNum++;

                notDoneTids.remove(trade.getTid());
                tidAlipayNo.put(trade.getTid(), trade.getBuyerAlipayNo());
            }
            log.warn("fix alipay number by rds : [" + fixByRdsNum + "] for user:" + user.toIdNick());
        }
    }

    public static void queryStatus(String startTime, String endTime) {
        // renderMockFileInJsonIfDev("tradecomment.json");
        final List<CommentStatus> res = new ArrayList<CommentStatus>();
        User user = getUser();
        long userId = user.getIdlong();

        Long startTs = null;
        Long endTs = null;

        Date startDate = null;
        Date endDate = null;
        try {
            startDate = dateSDF.parse(startTime.trim());
            endDate = dateSDF.parse(endTime.trim());

            if (!StringUtils.isEmpty(startTime)) {
                startTs = startDate.getTime();
            }
            if (!StringUtils.isEmpty(endTime)) {
                endTs = endDate.getTime();
                // 要加上一天的时间
                endTs += 24L * 3600L * 1000L;
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        HashMap<String, CommentStatus> dailyComments = new HashMap<String, CommentStatus>();
        for (Date tmp = startDate; !tmp.after(endDate); tmp.setDate(tmp.getDate() + 1)) {
            String tmpDate = dateSDF.format(tmp);
            dailyComments.put(tmpDate, new CommentStatus(tmpDate));
        }

        List<TradeRatePlay> tradeRateList = TradeRatePlayDao.findByUserBadComment(userId, startTs, endTs);
        // log.error("[queryTradeRate] " + tradeRateList);

        HashSet<String> totalModifyUser = new HashSet<String>();
        HashSet<String> totalModifyNeutralUser = new HashSet<String>();
        HashSet<String> totalModifyBadUser = new HashSet<String>();
        if (!CommonUtils.isEmpty(tradeRateList)) {

            for (TradeRatePlay tradeRate : tradeRateList) {
                String created = dateSDF.format(new Date(tradeRate.getCreated()));
                String updated = dateSDF.format(new Date(tradeRate.getUpdated()));
                if (tradeRate.getRate() <= 3) {
                    CommentStatus comment = dailyComments.get(created);
                    if (comment != null) {
                        int rate = tradeRate.getRate() & 3;
                        if (rate == 2) {
                            // 中评
                            comment.count += 1;
                            comment.neutralCount += 1;
                        } else if (rate == 3) {
                            // 差评
                            comment.count += 1;
                            comment.badCount += 1;
                        }
                    }
                } else if (tradeRate.getRate() > 3) {
                    int rate = tradeRate.getRate() & 3;
                    int pastRate = ((tradeRate.getRate() >> 2) & 3);
                    CommentStatus comment = dailyComments.get(created);
                    if (comment != null) {
                        if (pastRate == 2) {
                            // 中评
                            comment.count += 1;
                            comment.neutralCount += 1;
                        } else if (pastRate == 3) {
                            // 差评
                                comment.count += 1;
                            comment.badCount += 1;
                        }
                    }

                    comment = dailyComments.get(updated);
                    if (comment != null) {
                        if (rate <= 1 && pastRate == 2) {
                            // 中评改好评
                            comment.modifyCount += 1;
                            comment.modifyNeutralCount += 1;

                            comment.modifyUser.add(tradeRate.getNick());
                            comment.modifyNeutralUser.add(tradeRate.getNick());

                            totalModifyUser.add(tradeRate.getNick());
                            totalModifyNeutralUser.add(tradeRate.getNick());
                        } else if (rate <= 1 && pastRate == 3) {
                            // 差评改好评
                            comment.modifyCount += 1;
                            comment.modifyBadCount += 1;

                            comment.modifyUser.add(tradeRate.getNick());
                            comment.modifyBadUser.add(tradeRate.getNick());

                            totalModifyUser.add(tradeRate.getNick());
                            totalModifyBadUser.add(tradeRate.getNick());
                        }
                    }
                }
            }
        }

        //统计当天未评价订单数和评价数
        List<OrderDisplay> orderDisplays = OrderDisplayDao.searchWithArgs(userId, null, null, null, 5, false, null, startTs, endTs);
        for (OrderDisplay orderDisplay : orderDisplays) {
            String created = dateSDF.format(new Date(orderDisplay.getCreated()));//未评价订单创建时间
            CommentStatus comment = dailyComments.get(created);
            if (comment != null) {
                comment.noCommentCount++;
            }
        }
        List<TradeRatePlay> tradeRatePlayList = TradeRatePlayDao.findByUserIdDate(userId, startTs, endTs);
        for (TradeRatePlay tradeRatePlay : tradeRatePlayList) {
            String created = dateSDF.format(new Date(tradeRatePlay.getCreated()));//订单评价时间
            CommentStatus comment = dailyComments.get(created);
            if (comment != null) {
                comment.haveCommentCount++;
            }
        }

        //统计当天好评数
        List<TradeRatePlay> byUserGoodComment = TradeRatePlayDao.findByUserGoodComment(userId, startTs, endTs);
        for (TradeRatePlay tradeRatePlay : byUserGoodComment) {
            long created = tradeRatePlay.getCreated();
            long updated = tradeRatePlay.getUpdated();
            long max = Math.max(created, updated);
            String date = dateSDF.format(new Date(max));//订单评价时间
            CommentStatus comment = dailyComments.get(date);
            if (comment != null) {
                comment.goodCommentCount++;
            }
        }
        //统计催评得到的好评
        List<TradeRatePlay> originalGoodComment = TradeRatePlayDao.findByUserOriginalGoodComment(userId, startTs, endTs);
        for (TradeRatePlay tradeRatePlay : originalGoodComment) {
            long created = tradeRatePlay.getCreated();
            String date = dateSDF.format(new Date(created));//评价创建时间
            CommentStatus comment = dailyComments.get(date);
            if (comment != null) {
                OrderPlay orderPlay = OrderPlay.findByOid(userId, tradeRatePlay.getOid());
                if (orderPlay != null && StringUtils.isNotEmpty(orderPlay.getRemark()))
                    comment.urgeGoodCommentCount ++;
            }
        }





        String totalKey = "总 计";
        final CommentStatus totalComment = new CommentStatus(totalKey);
        new MapIterator<String, CommentStatus>(dailyComments) {

            @Override
            public void execute(Entry<String, CommentStatus> entry) {
                // TODO Auto-generated method stub
                CommentStatus comment = entry.getValue();
                comment.modifyUserCount = comment.modifyUser.size();
                comment.modifyNeutralUserCount = comment.modifyNeutralUser.size();
                comment.modifyBadUserCount = comment.modifyBadUser.size();
                res.add(comment);

                totalComment.count += comment.count;
                totalComment.neutralCount += comment.neutralCount;
                totalComment.badCount += comment.badCount;

                totalComment.modifyCount += comment.modifyCount;
                totalComment.modifyNeutralCount += comment.modifyNeutralCount;
                totalComment.modifyBadCount += comment.modifyBadCount;

                totalComment.noCommentCount += comment.noCommentCount;
                totalComment.haveCommentCount += comment.haveCommentCount;
                totalComment.goodCommentCount += comment.goodCommentCount;
                totalComment.urgeGoodCommentCount += comment.urgeGoodCommentCount;
            }

        }.call();

        totalComment.modifyUserCount = totalModifyUser.size();
        totalComment.modifyNeutralUserCount = totalModifyNeutralUser.size();
        totalComment.modifyBadUserCount = totalModifyBadUser.size();



        if (dailyComments.size() > 1) {
            res.add(totalComment);
        }

        Collections.sort(res, new Comparator<CommentStatus>() {
            @Override
            public int compare(CommentStatus o1, CommentStatus o2) {
                // return o1.date.compareTo(o2.date);
                return o2.date.compareTo(o1.date);
            }
        });

        renderJSON(JsonUtil.getJson(res));
    }

    public static class CommentStatus {
        /**
         * 统计时间 中差评总数 中评总数 差评总数 修改中差评用户数 修改中评用户数 修改差评用户数 修改中差评数 修改中评数 修改差评数
         */
        public String date;

        public int count = 0;

        public int neutralCount = 0;

        public int badCount = 0;

        public int modifyUserCount = 0;

        public int modifyNeutralUserCount = 0;

        public int modifyBadUserCount = 0;

        public int noCommentCount; //当天未评价数

        public int haveCommentCount; //当天评价数

        public int goodCommentCount; //当天好评数

        public int urgeGoodCommentCount;//当天催评所得好评数

        @JsonIgnore
        HashSet<String> modifyUser = new HashSet<String>();

        @JsonIgnore
        HashSet<String> modifyNeutralUser = new HashSet<String>();

        @JsonIgnore
        HashSet<String> modifyBadUser = new HashSet<String>();

        public int modifyCount = 0;

        public int modifyNeutralCount = 0;

        public int modifyBadCount = 0;

        public CommentStatus(String date) {
            this.date = date;
        }
    }

    public static void queryGoodRate(String startTime, String endTime) {
        User user = getUser();
        Long startTs = 0L;
        Long endTs = System.currentTimeMillis();

        try {
            if (StringUtils.isEmpty(startTime)) {
                startTs = dateSDF.parse(startTime).getTime();
            }
            if (StringUtils.isEmpty(endTime)) {
                endTs = dateSDF.parse(endTime).getTime() + DateUtil.DAY_MILLIS;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        List<SellerDSR> list = SellerDSR.findSellerDSR(user.getId(), startTs, endTs);
        renderJSON(JsonUtil.getJson(list));
    }

    public static void dsrSpider() throws Exception {
        User user = getUser();
        DSRSpider.spiderSellerGoodRate(user.getId());
        renderText("finished!");
    }

    public static void dsrSpiderAll() throws Exception {
        new CrawlSellerDSRJob().doJob();
        renderText("finished!");
    }

    public static void addServiceGroup(String groupName) {
        User user = getUser();
        boolean res = new ServiceGroup(user.getId(), groupName).jdbcSave();
        if (res == true) {
            renderSuccess("创建分组成功", "");
        } else {
            renderSuccess("创建分组失败", "");
        }
    }

    public static void queryServiceGroups() {
        User user = getUser();
        List<ServiceGroup> list = ServiceGroup.queryUserGroups(user.getId());
        renderSuccess("", list);
    }

    public static void dispatchServiceGroup(String oids, Long dispatchId) {
        User user = getUser();
        if (StringUtils.isEmpty(oids) || dispatchId == null || dispatchId < 0) {
            renderFailedJson("请选择正确的分组和订单后提交");
        }
        String[] oidArr = oids.split(",");
        if (oidArr.length == 0) {
            renderFailedJson("请选择正确的订单后提交");
        }
        HashSet<Long> oidSet = new HashSet<Long>();
        for (int i = 0; i < oidArr.length; i++) {
            if (StringUtils.isEmpty(oidArr[i])) {
                continue;
            }
            oidSet.add(Long.valueOf(oidArr[i]));
        }
        boolean updated = TradeRatePlayDao.updateDispatchId(user.getId(), oidSet, dispatchId);
        if (updated == false) {
            renderFailedJson("更新数据失败，请刷新再提交下，谢谢");
        }
        renderSuccess("分配客服成功！", null);
    }

    public static void deleteServiceGroup(Long groupId) {
        User user = getUser();
        if (groupId == null || groupId <= 0) {
            renderFailedJson("请选择正确的分组和订单后提交");
        }

        boolean deleted = ServiceGroup.deleteServiceGroup(user.getId(), groupId);
        if (deleted == false) {
            renderFailedJson("删除客服分组失败，请刷新后再试！");
        }
        TradeRatePlayDao.updateNewDispatchId(user.getId(), groupId, 0L);
        renderSuccess("删除客服成功！", null);
    }

    public static void manualBatchComment(String oidstidsbuyers, String result, String content) {
        log.info("batch manual comment starts!");
        User user = getUser();
        if (oidstidsbuyers == null || oidstidsbuyers.isEmpty()) {
            renderSuccess("no trade to rate", null);
        }
        if (content.isEmpty()) {
            renderSuccess("content is empty", null);
        }
        String[] oidtidbuyerArr = oidstidsbuyers.split("!@#");
        if (oidtidbuyerArr.length == 0) {
            renderSuccess("no trade to rate", null);
        }
        int successNum = 0;
        int failNum = 0;
        List<CommentsFailed> faillist = new ArrayList<CommentsFailed>();
        // sub string when content is too long to save
        String realContent = StringUtils.EMPTY;
        if (content.length() > 255) {
            realContent = content.substring(0, 250).concat("...");
        } else {
            realContent = content;
        }
        for (int i = 0; i < oidtidbuyerArr.length; i++) {
            String[] oidtidbuyer = oidtidbuyerArr[i].split("#@!");
            Long tid = Long.valueOf(oidtidbuyer[0]);
            Long oid = Long.valueOf(oidtidbuyer[1]);
            String buyerNick = oidtidbuyer[2];
            String reason = TaobaoUtil.commentNowWithReason(user.getUserNick(), user.getId(), buyerNick, result, tid,
                    oid, content);
            if (reason.isEmpty()) {
                successNum++;
            } else {
                failNum++;
                CommentsFailed fail = new CommentsFailed(user.getId(), tid, oid, result, realContent,
                        user.getUserNick(), buyerNick, reason);
                faillist.add(fail);
                fail.jdbcSave();
            }
        }
        renderJSON(JsonUtil.getJson(new MunualResult(successNum, failNum, faillist)));
    }

    public static void manualCommentAll(int interval, String result, String content) {
        if (content.isEmpty()) {
            renderSuccess("content is empty", null);
        }
        // log.info(format("getOrdersByUser:user, start, end".replaceAll(", ", "=%s, ") + "=%s", user, start, end));
        User user = getUser();
        int successNum = 0;
        int failNum = 0;
        // sub string when content is too long to save
        String realContent = StringUtils.EMPTY;
        if (content.length() > 255) {
            realContent = content.substring(0, 250).concat("...");
        } else {
            realContent = content;
        }
        List<CommentsFailed> faillist = new ArrayList<CommentsFailed>();
        log.info("[for user;]" + user);
        Date end = new Date();
        Date start = new Date(end.getTime() - DateUtil.DAY_MILLIS * interval);
        TaobaoClient client = TBApi.genClient();
        TradesSoldGetRequest req = new TradesSoldGetRequest();
        req.setFields("tid,orders,buyer_nick,seller_can_rate");
        req.setStartCreated(start);
        req.setEndCreated(end);
        req.setStatus("TRADE_FINISHED");
        // req.setType("game_equipment");
        // req.setExtType("service");
        req.setRateStatus("RATE_UNSELLER");
        // req.setTag("time_card");
        // req.setPageNo(1L);
        // req.setPageSize(40L);
        // req.setUseHasNext(true);
        // req.setIsAcookie(false);
        try {
            TradesSoldGetResponse response = client.execute(req, user.sessionKey);
            // 御城河日志接入
            sendTopLog(user.getId(), SimulateRequestUtil.TRADES_SOLD_GET, "fields=tid,orders,buyer_nick,seller_can_ratestart_created=" + start + "end_created=" + end + "status=TRADE_FINISHEDrate_status=RATE_UNSELLER");
            if (response.isSuccess()) {
                JSONObject trades_sold_get_response = new JSONObject(response.getBody())
                        .getJSONObject("trades_sold_get_response");
                JSONObject obj = null;
                if (trades_sold_get_response.has("trades")) {
                    obj = trades_sold_get_response.getJSONObject("trades");
                    JSONArray trades = obj.getJSONArray("trade");
                    if (trades != null && trades.length() > 0) {
                        int i = 0;
                        while (i++ < trades.length()) {
                            JSONObject trade = (JSONObject) trades.get(i - 1);
                            // 检测卖家是否可以评价
                            Long tid = Long.parseLong(trade.getString("tid"));
                            String buyerNick = trade.getString("buyer_nick");
                            if (!trade.getBoolean("seller_can_rate")) {
                                log.error("seller can not rate this trade!!!");
                                failNum++;
                                CommentsFailed fail = new CommentsFailed(user.getId(), tid, 0l, result, realContent,
                                        user.getUserNick(), buyerNick, "seller can not rate this trade!!!");
                                faillist.add(fail);
                                fail.jdbcSave();
                                continue;
                            }

                            JSONObject orderObj = trade.getJSONObject("orders");
                            JSONArray orders = orderObj.getJSONArray("order");
                            if (orders.length() > 0) {
                                int j = 0;
                                while (j++ < orders.length()) {
                                    JSONObject order = (JSONObject) orders.get(j - 1);
                                    // 如果卖家已评价
                                    if (order.getBoolean("seller_rate")) {
                                        log.info("seller already rated!!!");
                                        continue;
                                    }
                                    // 如果卖家是商城卖家
                                    if (order.getString("seller_type").equals("B")) {
                                        log.info("tmall seller!!!");
                                        // todo...
                                        continue;
                                    }
                                    if (order.getString("end_time") == null || order.getString("end_time").isEmpty()) {
                                        log.info("order is 15 days before , can not rate any more!!!");
                                        continue;
                                    }
                                    // 检测子订单结束时间是否超过15天
                                    Date date = sdf.parse(order.getString(("end_time")));
                                    if (System.currentTimeMillis() - date.getTime() > DateUtil.FIFTEEN_DAYS) {
                                        log.info("order is 15 days before , can not rate any more!!!");
                                        // todo...
                                        continue;
                                    }
                                    Long oid = Long.parseLong(order.getString("oid"));
                                    String conf = CommentConf.findConf(user.getId());
                                    String reason = TaobaoUtil.commentNowWithReason(user.userNick, user.getId(),
                                            buyerNick, result, tid, oid, content);
                                    if (reason.isEmpty()) {
                                        successNum++;
                                    } else {
                                        failNum++;
                                        CommentsFailed fail = new CommentsFailed(user.getId(), tid, oid, result,
                                                realContent, user.getUserNick(), buyerNick, reason);
                                        faillist.add(fail);
                                        fail.jdbcSave();
                                    }
                                }
                            }
                        }
                    }
                } else {
                    renderSuccess("no trade to rate", null);
                }
                renderJSON(JsonUtil.getJson(new MunualResult(successNum, failNum, faillist)));
            }
        } catch (ApiException e) {
            log.warn(e.getMessage(), e);
        } catch (JSONException e) {
            log.warn(e.getMessage(), e);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static class RateFail {
        private Long tid;

        private Long oid;

        // private String title;

        private String buyerNick;

        private String reason;

        public RateFail() {

        }

        public RateFail(Long tid, Long oid, String buyerNick, String reason) {
            this.oid = oid;
            this.tid = tid;
            // this.title = title;
            this.buyerNick = buyerNick;
            this.reason = reason;
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

        /*
         * public String getTitle() { return title; }
         * 
         * public void setTitle(String title) { this.title = title; }
         */

        public String getBuyerNick() {
            return buyerNick;
        }

        public void setBuyerNick(String buyerNick) {
            this.buyerNick = buyerNick;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    @JsonAutoDetect
    public static class MunualResult {

        @JsonProperty
        public int successNum;

        @JsonProperty
        public int failNum;

        @JsonProperty
        protected Object res;

        public MunualResult(int successNum, int failNum, Object res) {
            this.successNum = successNum;
            this.failNum = failNum;
            this.res = res;
        }
    }

    public static void testManualComment(Long tid, Long oid, String result, String content,
                                         String buyerNick) {
        User user = getUser();
        TaobaoUtil.commentNowWithReason(user.getUserNick(), user.getId(), buyerNick, result, tid,
                oid, content);
    }

    public static void tids() {
        User user = getUser();
        Date end = new Date(System.currentTimeMillis());
        Date start = new Date(end.getTime() - DateUtil.WEEK_MILLIS);
        List<Trade> call = new TMTradeApi.TradesSold(user, 0L, start, end).call();
        renderJSON(call);
    }

    public static void tidDetail(long tid) {
        User user = getUser();
        Trade trade = new TMTradeApi.GetFullTrade(user.getSessionKey(), tid).call();
        // 御城河日志接入
        sendTopLog(user.getId(), SimulateRequestUtil.TRADE_FULLINFO_GET, "tid=" + tid + "field=" + TMTradeApi.TRADE_FIELDS);
        renderJSON(trade);
    }

    /**
     * 获取店铺动态评分
     */
    public static void getShopScore(Long startTime, Long endTime) {
        User user = getUser();
        List<ShopScorePlay> scoreList = ShopScorePlay.findBySearchRules(startTime, endTime, user.getId());
        renderJSON(scoreList);
    }
    
}
