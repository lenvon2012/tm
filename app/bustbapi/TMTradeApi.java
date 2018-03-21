
package bustbapi;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.pojo.ItemThumb;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.MapIterator;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.client.utils.NumberUtil.Addable;
import com.google.gson.Gson;
import com.taobao.api.ApiException;
import com.taobao.api.TaobaoRequest;
import com.taobao.api.TaobaoResponse;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.Order;
import com.taobao.api.domain.Task;
import com.taobao.api.domain.Trade;
import com.taobao.api.domain.TradeRate;
import com.taobao.api.request.TopatsTradesSoldGetRequest;
import com.taobao.api.request.TradeCloseRequest;
import com.taobao.api.request.TradeFullinfoGetRequest;
import com.taobao.api.request.TraderatesGetRequest;
import com.taobao.api.request.TradesSoldGetRequest;
import com.taobao.api.request.TradesSoldIncrementGetRequest;
import com.taobao.api.response.TopatsTradesSoldGetResponse;
import com.taobao.api.response.TradeCloseResponse;
import com.taobao.api.response.TradeFullinfoGetResponse;
import com.taobao.api.response.TraderatesGetResponse;
import com.taobao.api.response.TradesSoldGetResponse;
import com.taobao.api.response.TradesSoldIncrementGetResponse;

import configs.TMConfigs;
import dao.item.ItemDao;

public abstract class TMTradeApi<K extends TaobaoRequest<V>, V extends TaobaoResponse, W> extends TBApi<K, V, W> {

    public TMTradeApi(String sid) {
        super(sid);
    }

    public final static Logger log = LoggerFactory.getLogger(TMTradeApi.class);

    public static final String TRADE_FIELDS = "buyer_area,tid,status,seller_nick,buyer_nick,num,"
            + "trade_from,payment,post_fee,price,received_payment,total_fee,num_iid,buyer_alipay_no,"
            + "created,pay_time,consign_time,end_time,modified,receiver_state,"
            + "receiver_city,receiver_district,receiver_address,receiver_zip,"
            + "receiver_mobile,receiver_phone,receiver_name,orders.oid,"
            + "orders.status, orders.buyer_nick, orders.seller_nick, orders.num_iid,"
            + "orders.cid, orders.title,orders.num, orders.pic_path,orders.payment," +
            "orders.price,orders.total_fee,orders.buyer_rate,orders.seller_rate";

    public final static String TRADE_ACOOKIE_FIELDS = "acookie_id,status,buyer_nick,num,payment,"
            + "created,pay_time,consign_time,end_time,modified,"
            + "orders.num_iid,orders.status,orders.cid,orders.num,orders.payment";

    public final static String RECENT_TRADE_FIELD = "tid,status,pay_time,orders.oid,orders.num_iid,orders.num,orders.pay_time,orders.status";

    static String TRADE_OID_FIELD = "tid,orders.oid,orders.num_iid,orders.num";

    static String TRADE_COMMENT_FIELD = "tid,buyer_nick,seller_can_rate,seller_rate,buyer_rate,end_time,orders.oid,orders.seller_rate,orders.buyer_rate,orders.seller_type,orders.end_time";

    public final static String TRADE_GET_FIELDS = "consign_time";

    public static final Long TRADE_PAGE_SIZE = 100L;

    static PYFutureTaskPool<ShopBaseTradeInfo> pool = new PYFutureTaskPool<ShopBaseTradeInfo>(16);

    static long concurrentMillis = 8 * DateUtil.DAY_MILLIS;

    @Override
    protected V execProcess() throws ApiException {
        if (Play.mode.isDev() && !"zrb".equals(Play.id) && !"lzl".equals(Play.id) && !"ww".equals(Play.id)
                && !"autodev".equals(Play.id) && !"defender".equals(Play.id) && !"tbtdev".equals(Play.id)) {
            log.error("No Trade api local  for this." + this.getClass());
            return null;
        }

        return super.execProcess();
    }

    public static ShopBaseTradeInfo buildNumIidSaleMap(User user, int days) throws Exception {
        if (!TMConfigs.App.IS_TRADE_ALLOW) {
            return null;
        }
        long end = DateUtil.formCurrDate();
        long start = end - DateUtil.DAY_MILLIS * days;
        end -= 1000L;

        ShopBaseTradeInfo finalRes = new ShopBaseTradeInfo(user);
        
        ShopBaseTradeInfo temp = new NumIidSaleNum(user, start, end).call();
        finalRes.add(temp);
        
        /*List<FutureTask<ShopBaseTradeInfo>> tasks = new ArrayList<FutureTask<ShopBaseTradeInfo>>();

        for (long tempStart = start; tempStart < end; tempStart += (concurrentMillis)) {
            long tempEnd = tempStart + concurrentMillis;
            if (tempEnd > end) {
                tempEnd = end;
            }
            log.info("[start : ]" + new Date(tempStart));
            tasks.add(pool.submit(new NumIidSaleNum(user, tempStart, tempEnd)));
        }
        
        for (FutureTask<ShopBaseTradeInfo> futureTask : tasks) {
            ShopBaseTradeInfo temp = futureTask.get();
            if (temp == null) {
                log.error("fails....");
                return null;
            }
            finalRes.add(temp);
        }*/
        
        return finalRes;
    }

    public static class NumIidSaleNum extends
            TMTradeApi<TradesSoldGetRequest, TradesSoldGetResponse, ShopBaseTradeInfo> {

        public NumIidSaleNum(User user, long start, long end) {
            super(user.getSessionKey());
            this.start = start;
            this.end = end;
            this.finalRes = new ShopBaseTradeInfo(user);
            this.user = user;
        }

        public User user;

        long start;

        long end;

//        Map<Long, Integer> finalRes = new HashMap<Long, Integer>();
        ShopBaseTradeInfo finalRes = null;

        Long pageNum = 1L;

        @Override
        public TradesSoldGetRequest prepareRequest() {
            TradesSoldGetRequest req = new TradesSoldGetRequest();
            req.setPageSize(TRADE_PAGE_SIZE);
            req.setPageNo(pageNum);
            req.setStartCreated(new Date(start));
            req.setEndCreated(new Date(end));
            req.setUseHasNext(true);
            req.setFields(RECENT_TRADE_FIELD);

            return req;
        }

        @Override
        public ShopBaseTradeInfo validResponse(TradesSoldGetResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            if (!resp.isSuccess()) {
                log.error("resp " + new Gson().toJson(resp));
                return null;
            }

            if (resp.getHasNext()) {
                this.iteratorTime = 1;
                this.pageNum++;
            }
//            resp.getTrades() == null ? ListUtils.EMPTY_LIST : resp.getTrades();
            ShopBaseTradeInfo tradeInfo = new ShopBaseTradeInfo(user);
            List<Trade> trades = resp.getTrades();
            if (CommonUtils.isEmpty(trades)) {
                return tradeInfo;
            }
//            tradeInfo.addTrades(trades);

//            log.info("[trade num:]" + trades.size());

            for (Trade trade : trades) {
//                CommonUtils.infoObj(trade);
                List<Order> orders = trade.getOrders();
                if (CommonUtils.isEmpty(orders)) {
                    continue;
                }
                if (trade.getPayTime() == null || trade.getPayTime().getTime() <= 0L) {
                    continue;
                }
                tradeInfo.addTrade(trade);
//                tradeInfo.tradeNum++;
                Map<Long, Integer> currRes = tradeInfo.numIidSales;
                for (Order order : orders) {
                    Integer existCount = currRes.get(order.getNumIid());
                    if (existCount == null) {
                        currRes.put(order.getNumIid(), order.getNum().intValue());
                    } else {
                        currRes.put(order.getNumIid(), existCount + order.getNum().intValue());
                    }
                }
            }

            // TODO write this later...
//            if (APIConfig.get().isSimpleTradeToLocal()) {
//                log.info(" write the user [" + user + "] with trade num" + trades.size());
//                TradeWritter.writeIncrementalTrades(user.getId(), System.currentTimeMillis(), trades);
//            }

            return tradeInfo;
        }

        public ShopBaseTradeInfo applyResult(ShopBaseTradeInfo res) {
            this.finalRes.add(res);
//            log.error("after append :" + this.finalRes);
            return this.finalRes;
        }
    }

    public static Map<Long, Integer> sumCount(Map<Long, Integer> srcMap, final Map<Long, Integer> finalRes) {
        if (CommonUtils.isEmpty(srcMap)) {
            return finalRes;
        }
        new MapIterator<Long, Integer>(srcMap) {
            @Override
            public void execute(Entry<Long, Integer> entry) {
                Integer existCount = finalRes.get(entry.getKey());
                if (existCount == null) {
                    finalRes.put(entry.getKey(), entry.getValue());
                } else {
                    finalRes.put(entry.getKey(), entry.getValue() + existCount);
                }
            }
        }.call();
        return finalRes;
    }

    public static class TradesSold extends TMTradeApi<TradesSoldGetRequest, TradesSoldGetResponse, List<Trade>> {

        public User user;

        public long ts;

        public Date startCreated;

        public Date endCreated;

        public boolean isAcookie;

        public long pageNo = 1;

        public List<Trade> resList;

        public TradesSold(User user, long ts, Date startCreated, Date endCreated) {
            this(user, ts, startCreated, endCreated, false);
        }

        public TradesSold(User user, long ts, Date startCreated, Date endCreated, boolean isAcookie) {

            super(user.getSessionKey());
            this.user = user;
            this.ts = ts;
            this.startCreated = startCreated;
            this.endCreated = endCreated;
            this.isAcookie = isAcookie;
            this.pageNo = 1;

            this.resList = new ArrayList<Trade>();
        }

        boolean isSimpleField = false;

        public TradesSold(User user, long ts, Date endCreated) {

            super(user.getSessionKey());
            this.user = user;
            this.ts = ts;
            this.startCreated = new Date(endCreated.getTime() - DateUtil.DAY_MILLIS);
            this.endCreated = endCreated;
            this.isSimpleField = true;

            this.resList = new ArrayList<Trade>();
        }

        @Override
        public TradesSoldGetRequest prepareRequest() {

            TradesSoldGetRequest req = new TradesSoldGetRequest();
            req.setPageSize(TRADE_PAGE_SIZE);
            req.setStartCreated(startCreated);
            req.setEndCreated(endCreated);
            req.setUseHasNext(true);
            req.setPageNo(pageNo);

            if (isAcookie) {
                req.setIsAcookie(isAcookie);
            }

            if (isSimpleField) {
                req.setFields(RECENT_TRADE_FIELD);
            } else if (isAcookie == true) {
                req.setFields(TRADE_ACOOKIE_FIELDS);
            } else {
                req.setFields(TRADE_FIELDS);
            }

            return req;
        }

        @Override
        public List<Trade> validResponse(TradesSoldGetResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            if (!resp.isSuccess()) {
                log.error("resp submsg" + resp.getSubMsg());
                log.error("resp error code " + resp.getErrorCode());
                log.error("resp Mesg " + resp.getMsg());
                return null;
            }

            if (resp.getHasNext()) {
                this.iteratorTime = 1;
                this.pageNo++;
            }
            return resp.getTrades() == null ? ListUtils.EMPTY_LIST : resp.getTrades();
        }

        @Override
        public List<Trade> applyResult(List<Trade> res) {
//            log.error("apply ... pn:" + pageNo);
            if (res == null) {
                return resList;
            }
            resList.addAll(res);
            return resList;
        }
    }

    public static class TradesSoldUnCommented extends
            TMTradeApi<TradesSoldGetRequest, TradesSoldGetResponse, List<Trade>> {

        public User user;

        public long ts;

        public Date startCreated;

        public Date endCreated;

        public boolean isAcookie;

        public long pageNo = 1;

        public List<Trade> resList;

        public TradesSoldUnCommented(User user, long ts, Date startCreated, Date endCreated) {
            this(user, ts, startCreated, endCreated, false);
        }

        public TradesSoldUnCommented(User user, long ts, Date startCreated, Date endCreated, boolean isAcookie) {

            super(user.getSessionKey());
            this.user = user;
            this.ts = ts;
            this.startCreated = startCreated;
            this.endCreated = endCreated;
            this.isAcookie = isAcookie;
            this.pageNo = 1;

            this.resList = new ArrayList<Trade>();
        }

        boolean isSimpleField = false;

        public TradesSoldUnCommented(User user, long ts, Date endCreated) {

            super(user.getSessionKey());
            this.user = user;
            this.ts = ts;
            this.startCreated = new Date(endCreated.getTime() - DateUtil.DAY_MILLIS);
            this.endCreated = endCreated;
            this.isSimpleField = true;

            this.resList = new ArrayList<Trade>();
        }

        @Override
        public TradesSoldGetRequest prepareRequest() {

            TradesSoldGetRequest req = new TradesSoldGetRequest();
            req.setPageSize(TRADE_PAGE_SIZE);
            req.setStartCreated(startCreated);
            req.setEndCreated(endCreated);
            req.setUseHasNext(true);
            req.setPageNo(pageNo);
            req.setStatus("TRADE_FINISHED");
            req.setRateStatus("RATE_UNSELLER");
            if (isAcookie) {
                req.setIsAcookie(isAcookie);
            }

            if (isSimpleField) {
                req.setFields(RECENT_TRADE_FIELD);
            } else if (isAcookie == true) {
                req.setFields(TRADE_ACOOKIE_FIELDS);
            } else {
                req.setFields(TRADE_FIELDS);
            }

            return req;
        }

        @Override
        public List<Trade> validResponse(TradesSoldGetResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            if (!resp.isSuccess()) {
                log.error("resp submsg" + resp.getSubMsg());
                log.error("resp error code " + resp.getErrorCode());
                log.error("resp Mesg " + resp.getMsg());
                return null;
            }

            if (resp.getHasNext()) {
                this.iteratorTime = 1;
                this.pageNo++;
            }
            return resp.getTrades() == null ? ListUtils.EMPTY_LIST : resp.getTrades();
        }

        @Override
        public List<Trade> applyResult(List<Trade> res) {
//            log.error("apply ... pn:" + pageNo);
            if (res == null) {
                return resList;
            }
            resList.addAll(res);
            return resList;
        }
    }

    public static class TradesSoldIncrementextends extends
            TMTradeApi<TradesSoldIncrementGetRequest, TradesSoldIncrementGetResponse, List<Trade>> {

        public User user;

        public long ts;

        public Date startModified;

        public Date endModified;

        public boolean isAcookie;

        public long pageNo = 1;

        public List<Trade> resList;

        public TradesSoldIncrementextends(User user, long ts, Date startModified, Date endModified) {
            this(user, ts, startModified, endModified, false);
        }

        public TradesSoldIncrementextends(User user, long ts, Date startModified, Date endModified, boolean isAcookie) {

            super(user.getSessionKey());
            this.user = user;
            this.ts = ts;
            this.startModified = startModified;
            this.endModified = endModified;
            this.isAcookie = isAcookie;

            this.resList = new ArrayList<Trade>();
        }

        boolean isRecenField = false;

        public TradesSoldIncrementextends(User user, long ts, Date endModified) {

            super(user.getSessionKey());
            this.user = user;
            this.ts = ts;
            this.startModified = new Date(endModified.getTime() - DateUtil.DAY_MILLIS);
            this.endModified = endModified;
            this.isRecenField = true;
            this.resList = new ArrayList<Trade>();
        }

        public TradesSoldIncrementextends(User user, long start, long end) {
            super(user.getSessionKey());
            this.user = user;
            this.ts = start;
            this.startModified = new Date(start - 6 * DateUtil.TEN_MINUTE_MILLIS);
            this.endModified = new Date(end);
            this.isRecenField = true;
            this.resList = new ArrayList<Trade>();
        }

        @Override
        public TradesSoldIncrementGetRequest prepareRequest() {

            TradesSoldIncrementGetRequest req = new TradesSoldIncrementGetRequest();
            req.setPageSize(TRADE_PAGE_SIZE);
            req.setStartModified(startModified);
            req.setEndModified(endModified);
            req.setUseHasNext(true);
            req.setPageNo(pageNo);

            if (isAcookie) {
                req.setIsAcookie(isAcookie);
            }
            if (isRecenField) {
                req.setFields(RECENT_TRADE_FIELD);
            } else if (isAcookie == true) {
                req.setFields(TRADE_ACOOKIE_FIELDS);
            } else {
                req.setFields(TRADE_FIELDS);
            }

            return req;
        }

        @Override
        public List<Trade> validResponse(TradesSoldIncrementGetResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            if (!resp.isSuccess()) {
                log.error("resp submsg" + resp.getSubMsg());
                log.error("resp error code " + resp.getErrorCode());
                log.error("resp Mesg " + resp.getMsg());
                return null;
            }

            if (resp.getHasNext()) {
                this.iteratorTime = 1;
                this.pageNo++;
            }
            return resp.getTrades() == null ? ListUtils.EMPTY_LIST : resp.getTrades();
        }

        @Override
        public List<Trade> applyResult(List<Trade> res) {

            if (res == null) {
                return null;
            }
            resList.addAll(res);
            return resList;

        }
    }

    public static class TradesSoldUnComment extends
            TMTradeApi<TradesSoldGetRequest, TradesSoldGetResponse, List<Trade>> {

        public User user;

        public Date startCreated;

        public Date endCreated;

        public boolean isAcookie;

        public long pageNo = 1;

        public List<Trade> resList;

        boolean isSimpleField = false;

        boolean onlyBuyerRate = false;

        public TradesSoldUnComment(User user, Date startCreated, Date endCreated, boolean onlyBuyerRate) {
            super(user.getSessionKey());
            this.user = user;
            this.startCreated = startCreated;
            this.endCreated = endCreated;
            this.onlyBuyerRate = onlyBuyerRate;
            this.isSimpleField = true;
            this.pageNo = 1;

            this.resList = new ArrayList<Trade>();
        }

        public TradesSoldUnComment(User user, Date startCreated, Date endCreated, boolean isAcookie,
                boolean isSimpleField) {
            super(user.getSessionKey());
            this.user = user;
            this.startCreated = startCreated;
            this.endCreated = endCreated;
            this.isAcookie = isAcookie;
            this.isSimpleField = isSimpleField;
            this.pageNo = 1;

            this.resList = new ArrayList<Trade>();
        }

        public TradesSoldUnComment(User user, Date endCreated) {
            super(user.getSessionKey());
            this.user = user;
            this.startCreated = new Date(endCreated.getTime() - DateUtil.DAY_MILLIS);
            this.endCreated = endCreated;
            this.isSimpleField = true;
            this.pageNo = 1;

            this.resList = new ArrayList<Trade>();
        }

        @Override
        public TradesSoldGetRequest prepareRequest() {

            TradesSoldGetRequest req = new TradesSoldGetRequest();
            req.setPageSize(TRADE_PAGE_SIZE);
            req.setStartCreated(startCreated);
            req.setEndCreated(endCreated);
            req.setUseHasNext(true);
            req.setPageNo(pageNo);

            req.setStatus("TRADE_FINISHED");
            if (onlyBuyerRate) {
                req.setRateStatus("RATE_BUYER_UNSELLER");
            } else {
                req.setRateStatus("RATE_UNSELLER");
            }

            if (isAcookie) {
                req.setIsAcookie(isAcookie);
            }

            if (isSimpleField) {
                req.setFields(TRADE_COMMENT_FIELD);
            } else if (isAcookie == true) {
                req.setFields(TRADE_ACOOKIE_FIELDS);
            } else {
                req.setFields(TRADE_FIELDS);
            }

            return req;
        }

        @Override
        public List<Trade> validResponse(TradesSoldGetResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            if (!resp.isSuccess()) {
                log.error("resp submsg" + resp.getSubMsg());
                log.error("resp error code " + resp.getErrorCode());
                log.error("resp Mesg " + resp.getMsg());
                return null;
            }

            if (resp.getHasNext()) {
                this.iteratorTime = 1;
                this.pageNo++;
            }
            return resp.getTrades() == null ? ListUtils.EMPTY_LIST : resp.getTrades();
        }

        @Override
        public List<Trade> applyResult(List<Trade> res) {
//            log.error("apply ... pn:" + pageNo);
            if (res == null) {
                return resList;
            }
            resList.addAll(res);
            return resList;
        }
    }

    public static class GetSellerOneTid extends TMTradeApi<TradesSoldGetRequest, TradesSoldGetResponse, Long> {

        public Long startCreated;

        public Long endCreated;

        public String field = "tid";

        public GetSellerOneTid(String sid) {
            this(sid, null, null);
        }

        public GetSellerOneTid(String sid, Long startCreated, Long endCreated) {
            super(sid);
            this.startCreated = startCreated;
            this.endCreated = endCreated;
        }

        @Override
        public TradesSoldGetRequest prepareRequest() {

            TradesSoldGetRequest req = new TradesSoldGetRequest();
            req.setPageSize(1L);
            if (startCreated != null && startCreated > 0) {
                req.setStartCreated(new Date(startCreated));
            }
            if (endCreated != null && endCreated > 0) {

                req.setEndCreated(new Date(endCreated));
            }
            req.setFields(field);
            return req;
        }

        @Override
        public Long validResponse(TradesSoldGetResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            if (!resp.isSuccess()) {
                log.error("resp submsg" + resp.getSubMsg());
                log.error("resp error code " + resp.getErrorCode());
                log.error("resp Mesg " + resp.getMsg());
                return null;
            }

            if (CommonUtils.isEmpty(resp.getTrades())) {

                return null;
            }

            return resp.getTrades().get(0).getTid();
        }

        @Override
        public Long applyResult(Long res) {
            return res;
        }
    }

    public static class GetSellerMobile extends TMTradeApi<TradeFullinfoGetRequest, TradeFullinfoGetResponse, String> {

        public Long tid;

        public String field = "seller_mobile";

        public GetSellerMobile(String sid, Long tid) {
            super(sid);
            this.tid = tid;
        }

        @Override
        public TradeFullinfoGetRequest prepareRequest() {

            TradeFullinfoGetRequest req = new TradeFullinfoGetRequest();
            req.setTid(tid);
            req.setFields(field);
            return req;
        }

        @Override
        public String validResponse(TradeFullinfoGetResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            if (!resp.isSuccess()) {
                log.error("resp submsg" + resp.getSubMsg());
                log.error("resp error code " + resp.getErrorCode());
                log.error("resp Mesg " + resp.getMsg());
                return null;
            }

            if (resp.getTrade() == null) {
                return null;
            }

            return resp.getTrade().getSellerMobile();
        }

        @Override
        public String applyResult(String res) {
            return res;
        }
    }

    public static class GetNumIidOfTradeOid extends TMTradeApi<TradeFullinfoGetRequest, TradeFullinfoGetResponse, Long> {

        Long tid;

        Long oid;

        public GetNumIidOfTradeOid(String sid, Long tid, Long oid) {
            super(sid);
            this.tid = tid;
            this.oid = oid;
        }

        @Override
        public TradeFullinfoGetRequest prepareRequest() {
            TradeFullinfoGetRequest req = new TradeFullinfoGetRequest();
            req.setTid(tid);
            req.setFields(TRADE_OID_FIELD);
            return req;
        }

        @Override
        public Long validResponse(TradeFullinfoGetResponse resp) {
            if (this.oid == null) {
                return null;
            }
            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            ErrorHandler.validTaoBaoResp(resp);
            if (!resp.isSuccess()) {
                return null;
            }

            if (resp.getTrade() == null) {
                return null;
            }

            for (Order order : resp.getTrade().getOrders()) {
                if (order.getOid() != null && order.getOid().longValue() == this.oid.longValue()) {
                    return order.getNumIid();
                }
            }

            return null;
        }

        @Override
        public Long applyResult(Long res) {
            return null;
        }

    }

    public static class GetFullTrade extends TMTradeApi<TradeFullinfoGetRequest, TradeFullinfoGetResponse, Trade> {

        public Long tid;

        // tid,seller_nick,buyer_nick,num_iid,num,status,orders
        public String field = TRADE_FIELDS;

        public GetFullTrade(String sid, Long tid) {
            super(sid);
            this.tid = tid;
        }

        @Override
        public TradeFullinfoGetRequest prepareRequest() {

            TradeFullinfoGetRequest req = new TradeFullinfoGetRequest();
            req.setTid(tid);
            req.setFields(field);
            return req;
        }

        @Override
        public Trade validResponse(TradeFullinfoGetResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            if (!resp.isSuccess()) {
                log.error("resp submsg" + resp.getSubMsg());
                log.error("resp error code " + resp.getErrorCode());
                log.error("resp Mesg " + resp.getMsg());
                return null;
            }

            if (resp.getTrade() == null) {
                return null;
            }

            return resp.getTrade();
        }

        @Override
        public Trade applyResult(Trade res) {
            return res;
        }
    }

    public static class GetTradeRate extends TMTradeApi<TraderatesGetRequest, TraderatesGetResponse, List<TradeRate>> {

        public Long tid;

        public String field = "seller_mobile";

        public GetTradeRate(String sid, Long tid) {
            super(sid);
            this.tid = tid;
        }

        @Override
        public TraderatesGetRequest prepareRequest() {

            TraderatesGetRequest req = new TraderatesGetRequest();
            req.setTid(tid);
            req.setFields(field);
            return req;
        }

        @Override
        public List<TradeRate> validResponse(TraderatesGetResponse resp) {

            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            if (!resp.isSuccess()) {
                log.error("resp submsg" + resp.getSubMsg());
                log.error("resp error code " + resp.getErrorCode());
                log.error("resp Mesg " + resp.getMsg());
                return null;
            }

            if (resp.getTradeRates() == null) {
                return null;
            }

            return resp.getTradeRates();
        }

        @Override
        public List<TradeRate> applyResult(List<TradeRate> res) {
            return res;
        }
    }

    public static class TradeNumUpdate extends TMTradeApi<TradesSoldGetRequest, TradesSoldGetResponse, Long> {

        private Date start;

        private Date end;

        /**
         * One week default...
         * @param user
         */
        public TradeNumUpdate(User user) {
            super(user.getSessionKey());
            end = new Date();
            start = new Date(end.getTime() - com.ciaosir.client.utils.DateUtil.THIRTY_DAYS);
        }

        public TradeNumUpdate(String sid, Date start, Date end) {
            super(sid);
        }

        @Override
        public TradesSoldGetRequest prepareRequest() {
            if (!TMConfigs.App.IS_TRADE_ALLOW) {
                return null;
            }
            TradesSoldGetRequest tradeNumReq = new TradesSoldGetRequest();

            tradeNumReq.setPageSize(1L);
            tradeNumReq.setPageNo(1L);
            tradeNumReq.setFields("tid");
            tradeNumReq.setStartCreated(start);
            tradeNumReq.setEndCreated(end);

            return tradeNumReq;

        }

        @Override
        public Long validResponse(TradesSoldGetResponse resp) {
            if (resp == null) {
                log.warn(" Get Num NULL");
                return null;
            }
            if (!resp.isSuccess()) {
                log.warn("Error Body: " + resp.getBody());
            }
            return resp.getTotalResults();
        }

        @Override
        public Long applyResult(Long res) {
            return res;
        }
    }

//
//    public static class TradesRecentWeek extends
//            TBApi<TradesSoldIncrementGetRequest, TradesSoldIncrementGetResponse, Map<Long, Integer>> {
//
//        public User user;
//
//        public long ts;
//
//        static String RECENT_TRADE_FIELD = "tid,pay_time,orders.num_iid,orders.num";
//
////        public List<Trade> resList;
//        Map<Long, Integer> map = new HashMap<Long, Integer>();
//
////        long end = System.currentTimeMillis();
////
////        long start = end - DateUtil.WEEK_MILLIS;
//
//        Date start;
//
//        Date end;
//
//        public TradesRecentWeek(User user) {
//            super(user.getSessionKey());
//            this.user = user;
//        }
//
//        public TradesRecentWeek(User user, Date start, Date end) {
//            super(user.getSessionKey());
//            this.user = user;
//            this.end = end;
//            this.start = start;
//        }
//
//        @Override
//        public TradesSoldIncrementGetRequest prepareRequest() {
//
//            TradesSoldIncrementGetRequest req = new TradesSoldIncrementGetRequest();
//            req.setPageSize(TRADE_PAGE_SIZE);
//
//            public TradesRecentWeek(User user) {
//                super(user.getSessionKey());
//                this.user = user;
//            }
//            req.setUseHasNext(true);
//            // req.setIsAcookie(isAcookie);
//            req.setFields(RECENT_TRADE_FIELD);
//            return req;
//        }
//
//        @Override
//        public Map<Long, Integer> validResponse(TradesSoldIncrementGetResponse resp) {
//
//            if (resp == null) {
//                log.error("Null Resp Returned");
//                return null;
//            }
//
//            if (!resp.isSuccess()) {
//                log.error("resp submsg" + resp.getSubMsg());
//                log.error("resp error code " + resp.getErrorCode());
//                log.error("resp Mesg " + resp.getMsg());
//                return null;
//            }
//
//            if (resp.getHasNext()) {
//                this.iteratorTime = 1;
//            }
//            List<Trade> trades = resp.getTrades();
//            if (CommonUtils.isEmpty(trades)) {
//                return this.map;
//            }
//            for (Trade trade : trades) {
//                // TODO check the trade...
//
//                log.info("[trade num]" + trade.getNum());
//                List<Order> orders = trade.getOrders();
//                if (CommonUtils.isEmpty(orders)) {
//                    continue;
//                }
//                for (Order order : orders) {
//                    Long numIid = order.getNumIid();
//                    Long num = order.getNum();
//                    if (numIid == null || num == null) {
//                        continue;
//                    }
//
//                    log.info("[order num]" + order.getNum());
//                    Integer exist = this.map.get(numIid);
//                    if (exist == null) {
//                        map.put(numIid, num.intValue());
//                    } else {
//                        map.put(numIid, exist + num.intValue());
//                    }
//                }
//            }
//            return this.map;
//        }
//
//        @Override
//        public Map<Long, Integer> applyResult(Map<Long, Integer> res) {
//            return res == null ? null : res;
//        }
//    }

    static PYFutureTaskPool<List<Trade>> tradesPool = new PYFutureTaskPool<List<Trade>>(14);

    /**
     * @deprecated so slow....
     * @param user
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static Map<Long, Integer> go(User user) throws InterruptedException, ExecutionException {
        if (!TMConfigs.App.IS_TRADE_ALLOW) {
            return MapUtils.EMPTY_MAP;
        }
        int failedCount = 0;

        long end = DateUtil.formCurrDate();
        long start = end - DateUtil.WEEK_MILLIS;
        List<Trade> trades = new ArrayList<Trade>();
//        List<FutureTask<List<Trade>>> futures = new ArrayList<FutureTask<List<Trade>>>();
//        for (long time = end; time >= start; time -= DateUtil.DAY_MILLIS) {
//            Date date = new Date(time);
//            futures.add(tradesPool.submit(new TradesSoldIncrementextends(user, 0L, date)));
//            log.info("[submit for ]" + date);
//        }
//
//        for (FutureTask<List<Trade>> futureTask : futures) {
//            List<Trade> list = futureTask.get();
//            if (list == null) {
//                failedCount++;
//            }
//
//            trades.addAll(list);
//        }
        for (long time = end; time >= start; time -= DateUtil.DAY_MILLIS) {
            Date date = new Date(time);
            List<Trade> list = new TradesSoldIncrementextends(user, 0L, date).call();
            log.info("[submit for ]" + date);
            if (list == null) {
                failedCount++;
            }

            trades.addAll(list);
        }

        if (failedCount >= 2) {
            log.error("More than 2 fails for :" + user);
            return null;
        }
        if (CommonUtils.isEmpty(trades)) {
            return MapUtils.EMPTY_MAP;
        }
        Map<Long, Integer> map = new HashMap<Long, Integer>();
        for (Trade trade : trades) {
            // TODO check the trade...

            log.info("[trade num]" + trade.getNum());
            List<Order> orders = trade.getOrders();
            if (CommonUtils.isEmpty(orders)) {
                continue;
            }
            for (Order order : orders) {
                Long numIid = order.getNumIid();
                Long num = order.getNum();
                if (numIid == null || num == null) {
                    continue;
                }

                log.info("[order num]" + order.getNum());
                Integer exist = map.get(numIid);
                if (exist == null) {
                    map.put(numIid, num.intValue());
                } else {
                    map.put(numIid, exist + num.intValue());
                }
            }
        }
        return map;

    }

    public static String getSellerMobile(String sid) {

        Long tid = new GetSellerOneTid(sid).call();
        if (tid == null) {
            return "";
        }

        String mobile = new GetSellerMobile(sid, tid).call();
        return mobile == null ? "" : mobile;
    }

    public static class ShopBaseTradeInfo implements Addable<ShopBaseTradeInfo> {

        User user;

        int tradeNum = 0;

        Map<Long, Integer> numIidSales = new HashMap<Long, Integer>();

        List<Trade> trades = new ArrayList<Trade>();

//
//        public ShopBaseTradeInfo(int tradeNum, Map<Long, Integer> numIidSales) {
//            super();
//            this.tradeNum = tradeNum;
//            this.numIidSales = numIidSales;
//        }

        public void addTrades(List<Trade> trades2) {
            if (CommonUtils.isEmpty(trades2)) {
                this.trades.addAll(trades2);
            }
        }

        public void addTrade(Trade trade) {
            if (trade == null) {
                return;
            }
            this.trades.add(trade);
            this.tradeNum++;
        }

        public ShopBaseTradeInfo(User user) {
            super();
            this.user = user;
        }

        public List<Trade> getTrades() {
            return trades;
        }

        public void setTrades(List<Trade> trades) {
            this.trades = trades;
        }

        @Override
        public void add(ShopBaseTradeInfo t) {
            this.sumNumPlus();
            this.tradeNum += t.tradeNum;
            sumCount(t.numIidSales, this.numIidSales);
            this.trades.addAll(t.trades);
//            log.info("[add for t:]" + t);
        }

        @JsonIgnore
        public int sum = 1;

        @JsonIgnore
        public int getSumNum() {
            return sum;
        }

        public int sumNumPlus() {
            return ++sum;
        }

        @Override
        public ShopBaseTradeInfo clone() {
            ShopBaseTradeInfo o = null;
            try {
                o = (ShopBaseTradeInfo) super.clone();
            } catch (CloneNotSupportedException e) {
                log.warn(e.getMessage(), e);
            }
            return o;
        }

        @Override
        public String toString() {
            return "ShopBaseTradeInfo [user=" + user + ", tradeNum=" + tradeNum + ", numIidSales=" + numIidSales
                    + ", sum=" + sum + "]";
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public int getTradeNum() {
            return tradeNum;
        }

        public void setTradeNum(int tradeNum) {
            this.tradeNum = tradeNum;
        }

        public Map<Long, Integer> getNumIidSales() {
            return numIidSales;
        }

        public void setNumIidSales(Map<Long, Integer> numIidSales) {
            this.numIidSales = numIidSales;
        }

        public List<ItemThumb> buildItemThumbs() {
            final List<ItemThumb> list = new ArrayList<ItemThumb>();
            new MapIterator<Long, Integer>(this.numIidSales) {
                @Override
                public void execute(Entry<Long, Integer> entry) {
                    ItemThumb thumb = new ItemThumb();
                    thumb.setId(entry.getKey());
                    thumb.setTradeNum(entry.getValue());
                    ItemDao.ensure(user, thumb);
                    list.add(thumb);
                }
            }.call();
            return list;
        }

        public List<ItemThumb> buildItemThumbs(List<Item> onSaleItemList) {
            final List<ItemThumb> list = new ArrayList<ItemThumb>();
            for (Item item : onSaleItemList) {
                ItemThumb thumb = new ItemThumb();
                thumb.setId(item.getNumIid());
                thumb.setFullTitle(item.getTitle());
                thumb.setPrice(NumberUtil.getIntFromPrice(item.getPrice()));
                thumb.setPicPath(item.getPicUrl());
                thumb.setSellerId(user.getId());
                Integer tradeNum = this.numIidSales.get(item.getNumIid());
                if (tradeNum != null) {
                    thumb.setTradeNum(tradeNum);
                }
            }
            return list;
        }
    }

    public static class CloseTrade extends TMTradeApi<TradeCloseRequest, TradeCloseResponse, Boolean> {

        private User user;

        private Long tid;

        private String closeReason;

        private String subMsg;

        public CloseTrade(User user, Long tid, String closeReason) {
            super(user.getSessionKey());
            this.user = user;
            this.tid = tid;
            this.closeReason = closeReason;
        }

        @Override
        public TradeCloseRequest prepareRequest() {
            TradeCloseRequest req = new TradeCloseRequest();

            req.setTid(tid);
            req.setCloseReason(closeReason);

            return req;

        }

        @Override
        public Boolean validResponse(TradeCloseResponse resp) {
            if (resp == null) {
                log.warn(" Get Num NULL");
                subMsg = "返回的response为空";
                return null;
            }
            if (!resp.isSuccess()) {
                subMsg = resp.getSubMsg();
                log.warn("Error Body: " + resp.getBody());
                return null;
            }
            subMsg = "";
            return Boolean.TRUE;
        }

        public String getSubMsg() {
            return subMsg;
        }

        @Override
        public Boolean applyResult(Boolean res) {
            return res;
        }
    }

    public Long asyncSoldTrades(String start, String end, String sid) throws ApiException {
        TopatsTradesSoldGetRequest req = new TopatsTradesSoldGetRequest();
        req.setFields("tid,seller_nick,buyer_nick,buyer_message,orders");
        req.setStartTime(start);
        req.setEndTime(end);
        TopatsTradesSoldGetResponse rsp = TBApi.genClient().execute(req, sid);
        if (rsp.isSuccess()) {
            return rsp.getTask().getTaskId();
        }
        return null;
    }

    public static class AsyncTradeApi extends TMTradeApi<TopatsTradesSoldGetRequest, TopatsTradesSoldGetResponse, Task> {

        String start = StringUtils.EMPTY;

        String end = StringUtils.EMPTY;

        static String dateFormat = "yyyyMMdd";

        public AsyncTradeApi(User user) {
            super(user.getSessionKey());
            Long firstLoginTime = user.getFirstLoginTime();
            if (System.currentTimeMillis() - firstLoginTime > DateUtil.WEEK_MILLIS) {
                firstLoginTime = DateUtil.formCurrDate();
            }

            Date end = new Date(DateUtil.formDailyTimestamp(firstLoginTime));
            Date start = new Date(end.getTime() - DateUtil.THIRTY_DAYS);
            formDate(start, end);
        }

        private void formDate(Date start, Date end) {
            DateFormat df = new SimpleDateFormat(dateFormat);
            this.start = df.format(start);
            this.end = df.format(end);
        }

        public AsyncTradeApi(String sid) {
            super(sid);
            Date end = new Date(DateUtil.formCurrDate());
            Date start = new Date(end.getTime() - DateUtil.THIRTY_DAYS);
            formDate(start, end);
        }

        public AsyncTradeApi(String sid, Date start, Date end) {
            super(sid);
            formDate(start, end);
        }

        public AsyncTradeApi(String sid, Date start, Date end, String field) {
            super(sid);
            formDate(start, end);
            this.fields = field;
        }

        String fields = TRADE_FIELDS;

        @Override
        public TopatsTradesSoldGetRequest prepareRequest() {
            TopatsTradesSoldGetRequest req = new TopatsTradesSoldGetRequest();
//            req.setFields("tid,seller_nick,buyer_nick,buyer_message,orders,status");
            req.setFields(fields);
            req.setStartTime(start);
            req.setEndTime(end);
            return req;
        }

        @Override
        public Task validResponse(TopatsTradesSoldGetResponse resp) {
            ErrorHandler.validTaoBaoResp(resp);

            if (resp.isSuccess()) {
                log.info("[is success : ]");
                return resp.getTask();
            }

            return null;
        }

        @Override
        public Task applyResult(Task res) {
            return res;
        }

    }
}
