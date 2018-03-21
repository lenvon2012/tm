/**
 *
 */

package dao.trade;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import jdbcexecutorwrapper.JDBCMapLongExecutor;
import jdbcexecutorwrapper.JDBCSetLongExecutor;
import jdp.JdpModel.JdpTradeModel;
import job.ItemCatOrderPayTimeDisTributeJob;
import models.Status;
import models.order.OrderDisplay;
import models.trade.TradeDisplay;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import transaction.DBBuilder;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import transaction.JPATransactionManager;
import actions.WangwangOnlineAction;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.DateUtil;
import com.taobao.api.domain.Trade;

/**
 * @author navins
 * @date 2013-6-16 下午12:55:59
 */
public class OrderDisplayDao {

    private static final Logger log = LoggerFactory.getLogger(OrderDisplayDao.class);

    public static final String ALL_FIELDS = "userId,ts,tid,oid,status,numIid,cid,buyerNick,title,num,picPath,payment,price,totalFee,created,payTime,consignTime,endTime,modified,createdDay,payTimeDay,consignTimeDay,endTimeDay,modifiedDay,buyerRate,sellerRate,phone,receiverName ";

    private static final String USER_TRADE_QUERY = " select " + ALL_FIELDS + " from order_display_%s ";

    private static final String COUNT_USER_TRADE_QUERY = " select count(*) from order_display_%s ";

    public static List<OrderDisplay> findByUserId(Long userId, Long ts) {
        String sql = OrderDisplay.genShardQuery(USER_TRADE_QUERY, userId) + " where userId = ? and ts >= ?";
        return new JDBCExecutor<List<OrderDisplay>>(OrderDisplay.dp, sql, userId, ts) {
            @Override
            public List<OrderDisplay> doWithResultSet(ResultSet rs) throws SQLException {
                List<OrderDisplay> list = new ArrayList<OrderDisplay>();

                while (rs.next()) {
                    OrderDisplay order = new OrderDisplay(rs);
                    if (order != null) {
                        list.add(order);
                    }
                }
                return list;
            }
        }.call();
    }

    public static List<OrderDisplay> findByOffsetLimit(int offset, int limit) {
        String sql = " select " + ALL_FIELDS + " from order_display_"
                + ItemCatOrderPayTimeDisTributeJob.OrderDisplayIndex + " limit ?,?";
        return new JDBCExecutor<List<OrderDisplay>>(OrderDisplay.dp, sql, offset, limit) {
            @Override
            public List<OrderDisplay> doWithResultSet(ResultSet rs) throws SQLException {
                List<OrderDisplay> list = new ArrayList<OrderDisplay>();

                while (rs.next()) {
                    OrderDisplay order = new OrderDisplay(rs);
                    if (order != null) {
                        list.add(order);
                    }
                }
                return list;
            }
        }.call();
    }

    public static final String COUNT_USER_NUMIID_BUY_NUM = "select numIid,count(*) from order_display_%s";

    public static Map<Long, Long> countUserNumIidBuyNum(Long userId, String buyerNick, Long ts) {
        String sql = OrderDisplay.genShardQuery(COUNT_USER_NUMIID_BUY_NUM, userId)
                + " where userId = ? and buyerNick = ? and ts >= ? and status != ? ";
        return new JDBCMapLongExecutor(OrderDisplay.dp, sql, userId, buyerNick, ts,
                Status.TRADE_STATUS.TRADE_CLOSED_BY_TAOBAO.ordinal()).call();
    }

    public static final String COUNT_BUY_NUMIID_NUM = "select count(distinct tid) from order_display_%s";

    public static long countBuyNumIidNum(Long userId, String buyerNick, Long numIid, Long ts) {
        String sql = OrderDisplay.genShardQuery(COUNT_BUY_NUMIID_NUM, userId)
                + " where userId = ? and numIid = ? and buyerNick = ? and ts >= ? and status <> ? ";
        int status = Status.TRADE_STATUS.TRADE_CLOSED_BY_TAOBAO.ordinal();
        return OrderDisplay.dp.singleLongQuery(sql, userId, numIid, buyerNick, ts, status);
    }

    public static List<OrderDisplay> findByUserIdNoCloseOrder(Long userId, Long ts) {
        String sql = OrderDisplay.genShardQuery(USER_TRADE_QUERY, userId)
                + " where userId = ? and ts >= ? and status != ?";
        return new JDBCExecutor<List<OrderDisplay>>(OrderDisplay.dp, sql, userId, ts,
                Status.TRADE_STATUS.TRADE_CLOSED_BY_TAOBAO.ordinal()) {
            @Override
            public List<OrderDisplay> doWithResultSet(ResultSet rs) throws SQLException {
                List<OrderDisplay> list = new ArrayList<OrderDisplay>();

                while (rs.next()) {
                    OrderDisplay order = new OrderDisplay(rs);
                    if (order != null) {
                        list.add(order);
                    }
                }
                return list;
            }
        }.call();
    }


    public static abstract class OrderDisplayBatchOper implements Callable<Boolean> {
        public int offset = 0;

        public int limit = 32;

        protected long sleepTime = 500L;

        public OrderDisplayBatchOper(int offset, int limit) {
            this.offset = offset;
            this.limit = limit;
        }

        public OrderDisplayBatchOper(int limit) {
            this.limit = limit;
        }

        public List<OrderDisplay> findNext() {
            return OrderDisplayDao.findByOffsetLimit(offset, limit);
        }

        public abstract void doForEachOrder(OrderDisplay order);

        @Override
        public Boolean call() {

            while (true) {

                List<OrderDisplay> findList = findNext();
                if (CommonUtils.isEmpty(findList)) {
                    return Boolean.TRUE;
                }

                for (OrderDisplay order : findList) {
                    offset++;
                    doForEachOrder(order);
                }

                JPATransactionManager.clearEntities();
                CommonUtils.sleepQuietly(sleepTime);
            }

        }
    }

    public static List<OrderDisplay> findByUserIdTid(Long userId, Long tid) {
        String sql = OrderDisplay.genShardQuery(USER_TRADE_QUERY, userId) + " where tid = ? ";
        return new JDBCExecutor<List<OrderDisplay>>(OrderDisplay.dp, sql, tid) {
            @Override
            public List<OrderDisplay> doWithResultSet(ResultSet rs) throws SQLException {
                List<OrderDisplay> list = new ArrayList<OrderDisplay>();

                while (rs.next()) {
                    OrderDisplay itemLimit = new OrderDisplay(rs);
                    if (itemLimit != null) {
                        list.add(itemLimit);
                    }
                }
                return list;
            }
        }.call();
    }

    public static List<OrderDisplay> findByUserIdTid(Long userId, Long tid, Long ts) {
        String sql = OrderDisplay.genShardQuery(USER_TRADE_QUERY, userId) + " where userId = ? and tid = ? and ts >= ?";
        return new JDBCExecutor<List<OrderDisplay>>(OrderDisplay.dp, sql, userId, tid, ts) {
            @Override
            public List<OrderDisplay> doWithResultSet(ResultSet rs) throws SQLException {
                List<OrderDisplay> list = new ArrayList<OrderDisplay>();

                while (rs.next()) {
                    OrderDisplay itemLimit = new OrderDisplay(rs);
                    if (itemLimit != null) {
                        list.add(itemLimit);
                    }
                }
                return list;
            }
        }.call();
    }

    public static List<OrderDisplay> findByUserIdPhone(Long userId, String phone) {
        String sql = OrderDisplay.genShardQuery(USER_TRADE_QUERY, userId) + " where userId = ? and phone like '%"
                + phone + "%'";
        return new JDBCExecutor<List<OrderDisplay>>(OrderDisplay.dp, sql, userId) {
            @Override
            public List<OrderDisplay> doWithResultSet(ResultSet rs) throws SQLException {
                List<OrderDisplay> list = new ArrayList<OrderDisplay>();

                while (rs.next()) {
                    OrderDisplay order = new OrderDisplay(rs);
                    if (order != null) {
                        list.add(order);
                    }
                }
                return list;
            }
        }.call();
    }

    public static List<OrderDisplay> findByUserIdPhone(Long userId, String phone, String encryptPhone) {
        String sql = OrderDisplay.genShardQuery(USER_TRADE_QUERY, userId) + " where userId = ? and phone in( ?, ? )";
        return new JDBCExecutor<List<OrderDisplay>>(OrderDisplay.dp, sql, userId, phone, encryptPhone) {
            @Override
            public List<OrderDisplay> doWithResultSet(ResultSet rs) throws SQLException {
                List<OrderDisplay> list = new ArrayList<OrderDisplay>();

                while (rs.next()) {
                    OrderDisplay order = new OrderDisplay(rs);
                    if (order != null) {
                        list.add(order);
                    }
                }
                return list;
            }
        }.call();
    }

    public static List<OrderDisplay> searchWithArgs(Long userId, Long tid, String buyerNick, int status,
                                                    Boolean buyerRate, Boolean sellerRate, Long startTs, Long endTs, PageOffset po) {
        String sql = OrderDisplay.genShardQuery(USER_TRADE_QUERY, userId)
                + formatQuery(userId, tid, buyerNick, buyerRate, sellerRate, startTs, endTs)
                + " and status = ? order by created desc limit ? offset ?";
        return new JDBCExecutor<List<OrderDisplay>>(OrderDisplay.dp, sql, userId, status, po.getPs(), po.getOffset()) {
            @Override
            public List<OrderDisplay> doWithResultSet(ResultSet rs) throws SQLException {
                List<OrderDisplay> list = new ArrayList<OrderDisplay>();
                while (rs.next()) {
                    OrderDisplay order = new OrderDisplay(rs);
                    if (order != null) {
                        list.add(order);
                    }
                }
                return list;
            }
        }.call();
    }

    public static String ORDER_DISPLAY_ONLINE_OFFSET = "OrderDisplay_online_";

    public static List<OrderDisplay> searchWithArgsOnline(Long userId, Long tid, String buyerNick, int status,
                                                          Boolean buyerRate, Boolean sellerRate, Long startTs, Long endTs, PageOffset po) {
        int pn = po.getPn();
        int ps = po.getPs();
        Cache.get(ORDER_DISPLAY_ONLINE_OFFSET + userId + "_" + tid);
        int offset = po.getOffset();
        String sql = OrderDisplay.genShardQuery(USER_TRADE_QUERY, userId)
                + formatQuery(userId, tid, buyerNick, buyerRate, sellerRate, startTs, endTs)
                + " and status = ? limit ? offset ?";

        List<OrderDisplay> orderList = new ArrayList<OrderDisplay>();
        while (true) {
            List<OrderDisplay> list = new JDBCExecutor<List<OrderDisplay>>(OrderDisplay.dp, sql, userId, status,
                    ps * 5, (pn - 1) * ps) {
                @Override
                public List<OrderDisplay> doWithResultSet(ResultSet rs) throws SQLException {
                    List<OrderDisplay> list = new ArrayList<OrderDisplay>();

                    while (rs.next()) {
                        OrderDisplay order = new OrderDisplay(rs);
                        if (order != null) {
                            list.add(order);
                        }
                    }
                    return list;
                }
            }.call();

            if (CommonUtils.isEmpty(list)) {
                break;
            }

            for (int i = 0; i < list.size() && orderList.size() < ps; i++) {
                OrderDisplay order = list.get(i);
                boolean isOnline = WangwangOnlineAction.isOnline(order.buyerNick);
                if (isOnline == true) {
                    orderList.add(order);
                }
            }
            if (orderList.size() == ps) {
                break;
            }
        }

        return orderList;
    }

    public static long countWithArgs(Long userId, Long tid, String buyerNick, int status, Boolean buyerRate,
                                     Boolean sellerRate, Long startTs, Long endTs) {
        String sql = OrderDisplay.genShardQuery(COUNT_USER_TRADE_QUERY, userId)
                + formatQuery(userId, tid, buyerNick, buyerRate, sellerRate, startTs, endTs) + " and status = ?";

        return OrderDisplay.dp.singleLongQuery(sql, userId, status);
    }

    private static String formatQuery(Long userId, Long tid, String buyerNick, Boolean buyerRate, Boolean sellerRate,
                                      Long startTs, Long endTs) {
        String query = " where userId = ? ";
        if (tid != null && tid > 0) {
            query += " and tid = " + tid;
        }
        if (!StringUtils.isEmpty(buyerNick)) {
            query += " and buyerNick " + formatBuyerNickLike(buyerNick);
        }
        if (buyerRate != null) {
            query += " and buyerRate = " + buyerRate;
        }
        if (sellerRate != null) {
            query += " and sellerRate = " + sellerRate;
        }
        if (startTs != null && startTs.longValue() > 0) {
            query += " and created >= " + startTs;
        }
        if (endTs != null && endTs.longValue() > 0L) {
            query += " and created < " + endTs;
        }
        return query;
    }

    private static String formatBuyerNickLike(String buyerNick) {
        String like = " like '%" + buyerNick + "%' ";
        return like;
    }

    public static String UDPATE_SELLER_RATE = " update order_display_%s set `sellerRate` = ? where `oid` = ? ";

    public static void updateSellerRate(Long userId, Long oid, boolean sellerRate) {
        String sql = OrderDisplay.genShardQuery(UDPATE_SELLER_RATE, userId);
        OrderDisplay.dp.update(sql, sellerRate, oid);
    }

    public static String UDPATE_BUYER_RATE = " update order_display_%s set `buyerRate` = ? where `oid` = ? ";

    public static void updateBuyerRate(Long userId, Long oid, boolean buyerRate) {
        String sql = OrderDisplay.genShardQuery(UDPATE_BUYER_RATE, userId);
        OrderDisplay.dp.update(sql, buyerRate, oid);
    }

    public static List<OrderDisplay> findByUserIdOids(Long userId, HashSet<Long> oids) {
        if (CommonUtils.isEmpty(oids)) {
            return ListUtils.EMPTY_LIST;
        }
        String oidString = StringUtils.join(oids, ",");
        String sql = OrderDisplay.genShardQuery(USER_TRADE_QUERY, userId) + " where userId = ? and oid in ("
                + oidString + ")";
        return new JDBCExecutor<List<OrderDisplay>>(OrderDisplay.dp, sql, userId) {
            @Override
            public List<OrderDisplay> doWithResultSet(ResultSet rs) throws SQLException {
                List<OrderDisplay> list = new ArrayList<OrderDisplay>();

                while (rs.next()) {
                    OrderDisplay order = new OrderDisplay(rs);
                    if (order != null) {
                        list.add(order);
                    }
                }
                return list;
            }
        }.call();
    }

    public static Map<Long, Integer> findUserRecentTrade(Long userId) {

        long startDate = System.currentTimeMillis() - DateUtil.THIRTY_DAYS;

        String sql = " select numIid,sum(num) from " + OrderDisplay.TABLE_NAME + DBBuilder.genUserIdHashKey(userId)
                + " where  userId=  ? and payTimeDay > ? group by numIid";

        return new JDBCBuilder.JDBCExecutor<Map<Long, Integer>>(true, OrderDisplay.dp, sql, userId, startDate) {

            @Override
            public Map<Long, Integer> doWithResultSet(ResultSet rs) throws SQLException {
                Map<Long, Integer> res = new HashMap<Long, Integer>();
                while (rs.next()) {
                    res.put(rs.getLong(1), rs.getInt(2));
                }
                return res;
            }
        }.call();

    }

    public static long removeUser(Long userId) {
        String sql = OrderDisplay.genShardQuery("delete from "
                + OrderDisplay.TABLE_NAME + "%s where userId = ?", userId);
        return JDBCBuilder.update(false, OrderDisplay.dp.getSrc(), sql, userId);
    }

    public static long removeUserOids(Long userId, Collection<Long> oids) {
        if (CommonUtils.isEmpty(oids)) {
            return 0L;
        }

        String sql = OrderDisplay.genShardQuery("delete from "
                + OrderDisplay.TABLE_NAME + "%s where oid in (" + StringUtils.join(oids, ',') + ")", userId);

//                + OrderDisplay.TABLE_NAME + "%s where userId = ? and oid in (" + StringUtils.join(oids, ',') + ")",
//                userId);
//        log.info("[sql :]" + sql);

        long update = JDBCBuilder.update(false, OrderDisplay.dp.getSrc(), sql);
        return update;
    }

    public static String LOAD_DATA_IN_FILE_ORDER_SQL = "LOAD DATA LOCAL INFILE ? INTO TABLE " + OrderDisplay.TABLE_NAME
            + "%s CHARACTER SET 'utf8' FIELDS TERMINATED BY ',' " + " (" + ALL_FIELDS + "); ";

    public static long executeOrderLoadDataInFile(Long userId, File file) {
        String sql = OrderDisplay.genShardQuery(LOAD_DATA_IN_FILE_ORDER_SQL, userId);
        return OrderDisplay.dp.update(sql, file.getAbsolutePath());
    }

    public static Map<String, String> queryOrderDay(Long userId) {
        String sql = OrderDisplay.genShardQuery(" select payTimeDay, count(oid) from " + OrderDisplay.TABLE_NAME
                + "%s where userId = ? group by payTimeDay ", userId);
        return new JDBCBuilder.JDBCExecutor<Map<String, String>>(sql, userId) {
            @Override
            public Map<String, String> doWithResultSet(ResultSet rs) throws SQLException {
                final Map<String, String> mapRes = new HashMap<String, String>();
                while (rs.next()) {
                    mapRes.put(DateUtil.formDateForLog(rs.getLong(1)), rs.getString(2));
                }
                return mapRes;
            }
        }.call();
    }

    public static void clearOld() {
        long oldTime = System.currentTimeMillis() - DateUtil.THIRTY_DAYS - (DateUtil.DAY_MILLIS * 2);
        for (int i = 0; i < 16; i++) {
            JDBCBuilder.update(false, OrderDisplay.dp.getSrc(), " delete from " + OrderDisplay.TABLE_NAME + i
                    + " where payTimeDay < ?", oldTime);
        }
    }

    public static void ensureBuyerAlipayNo(List<OrderDisplay> orders) {
        if (CommonUtils.isEmpty(orders)) {
            return;
        }
        Map<Long, OrderDisplay> noPayNo = new HashMap<Long, OrderDisplay>();

        for (OrderDisplay orderDisplay : orders) {
            String buyerAlipayNo = orderDisplay.getBuyerAlipayNo();
            if (buyerAlipayNo == null) {
                noPayNo.put(orderDisplay.getOid(), orderDisplay);
            }
        }

        if (CommonUtils.isEmpty(noPayNo)) {
            return;
        }

        Set<Long> tids = new HashSet<Long>();
        Collection<OrderDisplay> values = noPayNo.values();
        for (OrderDisplay orderDisplay : values) {
            tids.add(orderDisplay.getTid());
        }

        Map<Long, Trade> tidTrades = new HashMap<Long, Trade>();
        List<Trade> fetchTrades = JdpTradeModel.fetchTrades(tids);
        for (Trade trade : fetchTrades) {
            tidTrades.put(trade.getTid(), trade);
        }

        for (OrderDisplay orderDisplay : orders) {
            Trade trade = tidTrades.get(orderDisplay.getTid());
            if (trade == null) {
                continue;
            }
            orderDisplay.setBuyerAlipayNo(trade.getBuyerAlipayNo());
        }

    }

    public static Set<Long> findNumIidsByTid(TradeDisplay trade) {
        String sql = OrderDisplay.genShardQuery(" select numIid from " + OrderDisplay.TABLE_NAME
                + "%s where tid = ? ", trade.getUserId());

        Set<Long> numIids = new JDBCSetLongExecutor(sql, trade.getTid()).call();
        return numIids;
    }

    public static List<OrderDisplay> searchWithArgs(long userId, String buyerNick, String phone, String numIid, int status, Boolean buyerRate, Boolean sellerRate, Long startTs, Long endTs, PageOffset po) {
        List params = new ArrayList();
        String sql = OrderDisplay.genShardQuery(USER_TRADE_QUERY, userId)
                + formatQuery(userId, phone, numIid, buyerNick, buyerRate, sellerRate, startTs, endTs, params)
                + " and status = ? order by created desc limit ? offset ?";
        params.add(status);
        params.add(po.getPs());
        params.add(po.getOffset());
        return new JDBCExecutor<List<OrderDisplay>>(OrderDisplay.dp, sql, params.toArray()) {
            @Override
            public List<OrderDisplay> doWithResultSet(ResultSet rs) throws SQLException {
                List<OrderDisplay> list = new ArrayList<OrderDisplay>();
                while (rs.next()) {
                    OrderDisplay order = new OrderDisplay(rs);
                    if (order != null) {
                        list.add(order);
                    }
                }
                return list;
            }
        }.call();

    }

    public static long countWithArgs(long userId, String buyerNick, String phone, String numIid, int status, Boolean buyerRate, Boolean sellerRate, Long startTs, Long endTs) {
        List params = new ArrayList();
        String sql = OrderDisplay.genShardQuery(COUNT_USER_TRADE_QUERY, userId)
                + formatQuery(userId, phone, numIid, buyerNick, buyerRate, sellerRate, startTs, endTs, params) + " and status = ?";
        params.add(status);
        return OrderDisplay.dp.singleLongQuery(sql, params.toArray());

    }

    private static String formatQuery(long userId, String phone, String numIid, String buyerNick, Boolean buyerRate, Boolean sellerRate, Long startTs, Long endTs, List params) {
        String whereSql = " where userId = ? ";
        params.add(userId);
        if (!StringUtils.isEmpty(buyerNick)) {
            whereSql += " and buyerNick " + formatBuyerNickLike(buyerNick);
        }
        if (StringUtils.isNotEmpty(phone)) {
            whereSql += " and phone = ? ";
            params.add(phone);
        }
        if (StringUtils.isNotEmpty(numIid)) {
            whereSql += " and numIid = ? ";
            params.add(numIid);
        }
        if (buyerRate != null) {
            whereSql += " and buyerRate = ? ";
            params.add(buyerRate);
        }
        if (sellerRate != null) {
            whereSql += " and sellerRate = ? ";
            params.add(sellerRate);
        }
        if (startTs != null && startTs.longValue() > 0) {
            whereSql += " and created >= ? ";
            params.add(startTs);
        }
        if (endTs != null && endTs.longValue() > 0L) {
            whereSql += " and created < ? ";
            params.add(endTs);
        }

        return whereSql;
    }

    public static List<OrderDisplay> searchWithArgs(long userId, String buyerNick, String phone, String numIid, int status, Boolean buyerRate, Boolean sellerRate, Long startTs, Long endTs) {
        List params = new ArrayList();
        String sql = OrderDisplay.genShardQuery(USER_TRADE_QUERY, userId)
                + formatQuery(userId, phone, numIid, buyerNick, buyerRate, sellerRate, startTs, endTs, params)
                + " and status = ? order by created desc";
        params.add(status);
        return new JDBCExecutor<List<OrderDisplay>>(OrderDisplay.dp, sql, params.toArray()) {
            @Override
            public List<OrderDisplay> doWithResultSet(ResultSet rs) throws SQLException {
                List<OrderDisplay> list = new ArrayList<OrderDisplay>();
                while (rs.next()) {
                    OrderDisplay order = new OrderDisplay(rs);
                    if (order != null) {
                        list.add(order);
                    }
                }
                return list;
            }
        }.call();

    }

    //OrderDisplay左连OrderPlay查询 分页
    public static List<OrderDisplay> findListByCondition(long userId, String buyerNick, String phone, String numIid, int status,
                                                         Boolean buyerRate, Boolean sellerRate, Long startTs, Long endTs, Integer dispatchId, PageOffset po) {
        String tableName = OrderDisplay.genShardQuery("order_display_%s", userId);
        String selectFrom = "select a.userId,ts,tid,a.oid,status,numIid,cid,buyerNick,title,num,picPath,payment,price,totalFee," +
                "created,payTime,consignTime,endTime,modified,createdDay,payTimeDay,consignTimeDay,endTimeDay," +
                "modifiedDay,buyerRate,sellerRate,phone,receiverName,b.dispatchId,b.remark from " + tableName + " as a " +
                "LEFT JOIN order_play as b on a.userId = b.userId and a.oid = b.oid ";
        List params = new ArrayList();
        String whereSql = " where a.userId = ? ";
        params.add(userId);
        if (!StringUtils.isEmpty(buyerNick)) {
            whereSql += " and buyerNick = ? ";
            params.add(buyerNick);
        }
        if (StringUtils.isNotEmpty(phone)) {
            whereSql += " and phone = ? ";
            params.add(phone);
        }
        if (StringUtils.isNotEmpty(numIid)) {
            whereSql += " and numIid = ? ";
            params.add(numIid);
        }
        if (buyerRate != null) {
            whereSql += " and buyerRate = ? ";
            params.add(buyerRate);
        }
        if (sellerRate != null) {
            whereSql += " and sellerRate = ? ";
            params.add(sellerRate);
        }
        if (startTs != null && startTs.longValue() > 0) {
            whereSql += " and created >= ? ";
            params.add(startTs);
        }
        if (endTs != null && endTs.longValue() > 0L) {
            whereSql += " and created < ? ";
            params.add(endTs);
        }
        if (dispatchId == -1 || dispatchId == null) {//查看所有
            //不做操作
        } else if (dispatchId == 0) {//查看未分配
            whereSql += " and (dispatchId = 0 or dispatchId is null) ";
        } else {//查看分配人的记录
            whereSql += " and dispatchId = ? ";
            params.add(dispatchId);
        }
        whereSql += " and status = ? order by created desc limit ? offset ? ";
        params.add(status);
        params.add(po.getPs());
        params.add(po.getOffset());

        return new JDBCExecutor<List<OrderDisplay>>(OrderDisplay.dp, selectFrom + whereSql, params.toArray()) {
            @Override
            public List<OrderDisplay> doWithResultSet(ResultSet rs) throws SQLException {
                List<OrderDisplay> list = new ArrayList<OrderDisplay>();
                while (rs.next()) {
                    OrderDisplay order = new OrderDisplay();
                    int i = 1;
                    order.userId = rs.getLong(i++);
                    order.ts = rs.getLong(i++);
                    order.tid = rs.getLong(i++);
                    order.oid = rs.getLong(i++);
                    order.status = rs.getInt(i++);
                    order.numIid = rs.getLong(i++);
                    order.cid = rs.getLong(i++);
                    order.buyerNick = rs.getString(i++);
                    order.title = rs.getString(i++);
                    order.num = rs.getInt(i++);
                    order.picPath = rs.getString(i++);
                    order.payment = rs.getDouble(i++);
                    order.price = rs.getDouble(i++);
                    order.totalFee = rs.getDouble(i++);
                    order.created = rs.getLong(i++);
                    order.payTime = rs.getLong(i++);
                    order.consignTime = rs.getLong(i++);
                    order.endTime = rs.getLong(i++);
                    order.modified = rs.getLong(i++);
                    order.createdDay = rs.getLong(i++);
                    order.payTimeDay = rs.getLong(i++);
                    order.consignTimeDay = rs.getLong(i++);
                    order.endTimeDay = rs.getLong(i++);
                    order.modifiedDay = rs.getLong(i++);
                    order.buyerRate = rs.getBoolean(i++);
                    order.sellerRate = rs.getBoolean(i++);
                    order.phone = rs.getString(i++);
                    order.receiverName = rs.getString(i++);
                    order.dispatchId = rs.getLong(i++);
                    order.remark = rs.getString(i++);

                    if (order != null) {
                        list.add(order);
                    }
                }
                return list;
            }
        }.call();
    }

    //OrderDisplay左连OrderPlay统计数量
    public static Long countByCondition(long userId, String buyerNick, String phone, String numIid, int status,
                                        Boolean buyerRate, Boolean sellerRate, Long startTs, Long endTs, Integer dispatchId) {
        String tableName = OrderDisplay.genShardQuery("order_display_%s", userId);
        String selectFrom = "select count(*) from " + tableName + " a " +
                "LEFT JOIN order_play b on a.userId = b.userId and a.oid = b.oid ";
        List params = new ArrayList();
        String whereSql = " where a.userId = ? ";
        params.add(userId);
        if (!StringUtils.isEmpty(buyerNick)) {
            whereSql += " and buyerNick = ? ";
            params.add(buyerNick);
        }
        if (StringUtils.isNotEmpty(phone)) {
            whereSql += " and phone = ? ";
            params.add(phone);
        }
        if (StringUtils.isNotEmpty(numIid)) {
            whereSql += " and numIid = ? ";
            params.add(numIid);
        }
        if (buyerRate != null) {
            whereSql += " and buyerRate = ? ";
            params.add(buyerRate);
        }
        if (sellerRate != null) {
            whereSql += " and sellerRate = ? ";
            params.add(sellerRate);
        }
        if (startTs != null && startTs.longValue() > 0) {
            whereSql += " and created >= ? ";
            params.add(startTs);
        }
        if (endTs != null && endTs.longValue() > 0L) {
            whereSql += " and created < ? ";
            params.add(endTs);
        }
        if (dispatchId == -1 || dispatchId == null) {//查看所有
            //不做操作
        } else if (dispatchId == 0) {//查看未分配
            whereSql += " and (dispatchId = 0 or dispatchId is null) ";
        } else {//查看分配人的记录
            whereSql += " and dispatchId = ? ";
            params.add(dispatchId);
        }
        whereSql += " and status = ? order by created desc ";
        params.add(status);

        return OrderDisplay.dp.singleLongQuery(selectFrom + whereSql, params.toArray());

    }

    //OrderDisplay左连OrderPlay查询 不分页
    public static List<OrderDisplay> findListByCondition(long userId, String buyerNick, String phone, String numIid, int status,
                                                         Boolean buyerRate, Boolean sellerRate, Long startTs, Long endTs, Integer dispatchId) {
        String tableName = OrderDisplay.genShardQuery("order_display_%s", userId);
        String selectFrom = "select a.userId,ts,tid,a.oid,status,numIid,cid,buyerNick,title,num,picPath,payment,price,totalFee," +
                "created,payTime,consignTime,endTime,modified,createdDay,payTimeDay,consignTimeDay,endTimeDay," +
                "modifiedDay,buyerRate,sellerRate,phone,receiverName,b.dispatchId,b.remark from " + tableName + " as a " +
                "LEFT JOIN order_play as b on a.userId = b.userId and a.oid = b.oid ";
        List params = new ArrayList();
        String whereSql = " where a.userId = ? ";
        params.add(userId);
        if (!StringUtils.isEmpty(buyerNick)) {
            whereSql += " and buyerNick = ? ";
            params.add(buyerNick);
        }
        if (StringUtils.isNotEmpty(phone)) {
            whereSql += " and phone = ? ";
            params.add(phone);
        }
        if (StringUtils.isNotEmpty(numIid)) {
            whereSql += " and numIid = ? ";
            params.add(numIid);
        }
        if (buyerRate != null) {
            whereSql += " and buyerRate = ? ";
            params.add(buyerRate);
        }
        if (sellerRate != null) {
            whereSql += " and sellerRate = ? ";
            params.add(sellerRate);
        }
        if (startTs != null && startTs.longValue() > 0) {
            whereSql += " and created >= ? ";
            params.add(startTs);
        }
        if (endTs != null && endTs.longValue() > 0L) {
            whereSql += " and created < ? ";
            params.add(endTs);
        }

        if (dispatchId == -1 || dispatchId == null) {//查看所有
            //不做操作
        } else if (dispatchId == 0) {//查看未分配
            whereSql += " and (dispatchId = 0 or dispatchId is null) ";
        } else {//查看分配人的记录
            whereSql += " and dispatchId = ? ";
            params.add(dispatchId);
        }
        whereSql += " and status = ? order by created desc ";
        params.add(status);

        return new JDBCExecutor<List<OrderDisplay>>(OrderDisplay.dp, selectFrom + whereSql, params.toArray()) {
            @Override
            public List<OrderDisplay> doWithResultSet(ResultSet rs) throws SQLException {
                List<OrderDisplay> list = new ArrayList<OrderDisplay>();
                while (rs.next()) {
                    OrderDisplay order = new OrderDisplay();
                    int i = 1;
                    order.userId = rs.getLong(i++);
                    order.ts = rs.getLong(i++);
                    order.tid = rs.getLong(i++);
                    order.oid = rs.getLong(i++);
                    order.status = rs.getInt(i++);
                    order.numIid = rs.getLong(i++);
                    order.cid = rs.getLong(i++);
                    order.buyerNick = rs.getString(i++);
                    order.title = rs.getString(i++);
                    order.num = rs.getInt(i++);
                    order.picPath = rs.getString(i++);
                    order.payment = rs.getDouble(i++);
                    order.price = rs.getDouble(i++);
                    order.totalFee = rs.getDouble(i++);
                    order.created = rs.getLong(i++);
                    order.payTime = rs.getLong(i++);
                    order.consignTime = rs.getLong(i++);
                    order.endTime = rs.getLong(i++);
                    order.modified = rs.getLong(i++);
                    order.createdDay = rs.getLong(i++);
                    order.payTimeDay = rs.getLong(i++);
                    order.consignTimeDay = rs.getLong(i++);
                    order.endTimeDay = rs.getLong(i++);
                    order.modifiedDay = rs.getLong(i++);
                    order.buyerRate = rs.getBoolean(i++);
                    order.sellerRate = rs.getBoolean(i++);
                    order.phone = rs.getString(i++);
                    order.receiverName = rs.getString(i++);
                    order.dispatchId = rs.getLong(i++);
                    order.remark = rs.getString(i++);

                    if (order != null) {
                        list.add(order);
                    }
                }
                return list;
            }
        }.call();
    }

}
