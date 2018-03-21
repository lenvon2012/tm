/**
 * 
 */

package dao.trade;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jdbcexecutorwrapper.JDBCLongSetExecutor;
import models.Status;
import models.Status.TRADE_FROM;
import models.defense.BlackListBuyer;
import models.trade.TradeDisplay;
import models.user.User;
import models.visit.LinezingRecord;
import models.visit.TidLineZingBind;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import secure.SimulateRequestUtil;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import utils.PlayUtil;
import utils.TBIpApi.IpDataBean;
import codegen.CodeGenerator.DBDispatcher;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.NumberUtil;

import controllers.APIConfig;
import dao.defense.BlackListBuyerDao;
import dao.item.ItemDao;

/**
 * @author navins
 * @date 2013-6-16 下午12:55:59
 */
public class TradeDisplayDao {

    private static final Logger log = LoggerFactory.getLogger(TradeDisplayDao.class);

    public static final String ALL_FIELDS = " tid,userId,ts,status,buyerNick,num,tradeFrom,payment,"
            + "postFee,price,receivedPayment,totalFee,created,payTime,consignTime,endTime,modified,"
            + "createdDay,payTimeDay,consignTimeDay,endTimeDay,modifiedDay,receiverAddress,receiverMobile,"
            + "buyerAlipayNo,buyerArea,receiverPhone ";

    private static final String USER_TRADE_QUERY = " select  " + ALL_FIELDS + " from trade_display_%s ";

    private static final String TRADE_COUNT_SQL = " select count(*) from trade_display_%s ";

    public static int countUserRecentTradeNum(Long userId) {
        String sql = TradeDisplay.genShardQuery("select count(tid) from " + TradeDisplay.TABLE_NAME
                + "%s where userId = ?", userId);
        return (int) JDBCBuilder.singleLongQuery(TradeDisplay.dp.getSrc(), sql, userId);
    }

    public static Map<Long, String> findTidsBuyerAlipayNo(Long userId, Collection<Long> ids) {
        if (CommonUtils.isEmpty(ids)) {
            return MapUtils.EMPTY_MAP;
        }
        String sql = TradeDisplay.genShardQuery(" select tid, buyerAlipayNo from trade_display_%s ", userId)
                + " where tid in (" + StringUtils.join(ids, ',') + ")";

        // 御城河日志
        SimulateRequestUtil.sendSqlLog("订单编号", APIConfig.get().getRdsHostAddress(), sql);
        return new JDBCExecutor<Map<Long, String>>(sql) {
            Map<Long, String> map = new HashMap<Long, String>();

            @Override
            public Map<Long, String> doWithResultSet(ResultSet rs) throws SQLException {
                while (rs.next()) {
                    Long tid = rs.getLong(1);
                    String buyerAlipayNo = rs.getString(2);
                    if (buyerAlipayNo != null) {
                        map.put(tid, buyerAlipayNo);
                    }
                }
                return map;
            }
        }.call();
    }

    public static List<TradeDisplay> findByUserId(Long userId, Long ts) {
        String sql = TradeDisplay.genShardQuery(USER_TRADE_QUERY, userId) + " where userId = ? and ts >= ?";
        // 御城河日志
        SimulateRequestUtil.sendSqlLog("订单详情", APIConfig.get().getRdsHostAddress(), sql);
        return new JDBCTradeListQuerier(TradeDisplay.dp, sql, userId, ts).call();
    }

    public static class JDBCTradeListQuerier extends JDBCExecutor<List<TradeDisplay>> {

        public JDBCTradeListQuerier(boolean debug, DataSrc src, String query, Object... params) {
            super(debug, src, query, params);
        }

        public JDBCTradeListQuerier(DBDispatcher dp, String sql, Object... params) {
            super(dp, sql, params);
        }

        @Override
        public List<TradeDisplay> doWithResultSet(ResultSet rs) throws SQLException {
            List<TradeDisplay> list = new ArrayList<TradeDisplay>();

            while (rs.next()) {
                TradeDisplay itemLimit = new TradeDisplay(rs);
                if (itemLimit != null) {
                    list.add(itemLimit);
                }
            }

            return list;
        }

    }

    public static TradeDisplay findByUserIdTid(Long userId, Long tid) {
        String sql = TradeDisplay.genShardQuery(USER_TRADE_QUERY, userId) + " where userId = ? and tid = ?";
        // 御城河日志
        SimulateRequestUtil.sendSqlLog("订单详情", APIConfig.get().getRdsHostAddress(), sql);
        return new JDBCExecutor<TradeDisplay>(TradeDisplay.dp, sql, userId, tid) {
            @Override
            public TradeDisplay doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    TradeDisplay trade = new TradeDisplay(rs);
                    if (trade != null) {
                        return trade;
                    }
                }
                return null;
            }
        }.call();
    }

    public static List<TradeDisplay> findByUserIdBuyerNick(Long userId, String buyerNick, Long ts, List<Integer> status) {

        String sql = TradeDisplay.genShardQuery(USER_TRADE_QUERY, userId)
                + " where userId = ? and buyerNick = ? and ts >= ? and status in (%s)";
        String statusString = StringUtils.join(status, ',');
        sql = String.format(sql, statusString);
        // 御城河日志
        SimulateRequestUtil.sendSqlLog("订单详情", APIConfig.get().getRdsHostAddress(), sql);
        return new JDBCTradeListQuerier(TradeDisplay.dp, sql, userId, buyerNick, ts).call();
    }

    public static List<TradeDisplay> findByUserIdBuyerNick(Long userId, String buyerNick, Long ts) {
        List<Integer> status = new ArrayList<Integer>();
        status.add(Status.TRADE_STATUS.WAIT_BUYER_PAY.ordinal());
        status.add(Status.TRADE_STATUS.WAIT_SELLER_SEND_GOODS.ordinal());
        status.add(Status.TRADE_STATUS.WAIT_BUYER_CONFIRM_GOODS.ordinal());
        status.add(Status.TRADE_STATUS.TRADE_BUYER_SIGNED.ordinal());
        status.add(Status.TRADE_STATUS.TRADE_FINISHED.ordinal());

        return findByUserIdBuyerNick(userId, buyerNick, ts, status);
    }

    public static List<TradeDisplay> findByBlacklist(Long userId, Long tid, String buyerNick, Long startTs, Long endTs,
            PageOffset po) {
        List<String> buyerNicks = new ArrayList<String>();
        List<BlackListBuyer> buyers = BlackListBuyerDao.findBlackListBuyers(userId);
        for (BlackListBuyer buyer : buyers) {
            String name = buyer.getBuyerName();
            if (!StringUtils.isEmpty(buyerNick) && name.indexOf(buyerNick) < 0) {
                continue;
            }
            buyerNicks.add(buyer.getBuyerName());
        }
        if (CommonUtils.isEmpty(buyerNicks)) {
            return null;
        }
        String sql = TradeDisplay.genShardQuery(USER_TRADE_QUERY, userId)
                + formatQuery(userId, tid, buyerNicks, startTs, endTs, po.getPs(), po.getOffset());
        // 御城河日志
        SimulateRequestUtil.sendSqlLog("订单详情", APIConfig.get().getRdsHostAddress(), sql);
        return new JDBCExecutor<List<TradeDisplay>>(TradeDisplay.dp, sql, userId) {
            @Override
            public List<TradeDisplay> doWithResultSet(ResultSet rs) throws SQLException {
                List<TradeDisplay> list = new ArrayList<TradeDisplay>();
                while (rs.next()) {
                    TradeDisplay trade = new TradeDisplay(rs);
                    if (trade != null) {
                        list.add(trade);
                    }
                }
                return list;
            }
        }.call();
    }

    public static long countDefenseBlacklistByRules(Long userId, Long tid, String buyerNick, Long startTs, Long endTs) {
        List<String> buyerNicks = new ArrayList<String>();
        List<BlackListBuyer> buyers = BlackListBuyerDao.findBlackListBuyers(userId);
        for (BlackListBuyer buyer : buyers) {
            String name = buyer.getBuyerName();
            if (!StringUtils.isEmpty(buyerNick) && name.indexOf(buyerNick) < 0) {
                continue;
            }
            buyerNicks.add(buyer.getBuyerName());
        }
        if (CommonUtils.isEmpty(buyerNicks)) {
            return 0;
        }
        String query = formatQuery(userId, tid, buyerNicks, startTs, endTs, 0, 0);
        String countSql = TradeDisplay.genShardQuery(TRADE_COUNT_SQL, userId) + query;
        long count = TradeDisplay.dp.singleLongQuery(countSql, userId);
        return count;
    }

    private static String formatQuery(Long userId, Long tid, List<String> buyerNicks, Long startTs, Long endTs, int ps,
            int offset) {
        String query = " where userId=? ";
        if (!CommonUtils.isEmpty(buyerNicks)) {
            query += " and buyerNick " + formatInSet(buyerNicks);
        }
        if (startTs != null) {
            query += " and ts>=" + startTs;
        }
        if (endTs != null && endTs.longValue() > 0L) {
            query += " and ts < " + endTs;
        }
        if (tid != null && tid > 0) {
            query += " and tid=" + tid;
        }
        query += " order by created desc";
        if (ps > 0 && offset >= 0) {
            query += " limit " + ps + " offset " + offset;
        }
        return query;
    }

    private static String formatTitleLike(Long userId, String title) {
        if (StringUtils.isEmpty(title))
            return "";
        String itemTable = ItemDao.genShardQuery("item%s", userId);
        String query = " numIid in (select numIid from " + itemTable + " where title like '%" + title + "%') ";

        return query;
    }

    private static String formatInSet(List<String> buyerNicks) {
        if (CommonUtils.isEmpty(buyerNicks)) {
            return null;
        }
        String nickString = StringUtils.join(buyerNicks, "','");
        String in = " in ('" + nickString + "') ";
        return in;
    }

    private static String formatLike(String val) {
        String like = " like '%" + val + "%' ";
        return like;
    }

    public static long removeUser(Long userId) {

        String sql = TradeDisplay.genShardQuery("delete from "
                + TradeDisplay.TABLE_NAME + "%s where userId = ?", userId);
        return JDBCBuilder.update(false, TradeDisplay.dp.getSrc(), sql, userId);
    }

    public static long removeUserTids(Long userId, Collection<Long> tids) {
        if (CommonUtils.isEmpty(tids)) {
            return 0L;
        }

        String sql = TradeDisplay.genShardQuery("delete from " + TradeDisplay.TABLE_NAME + "%s where tid in ("
                + StringUtils.join(tids, ',') + ")", userId);
        log.info("[sql :]" + sql);

        long update = JDBCBuilder.update(false, TradeDisplay.dp.getSrc(), sql);
        return update;
    }

    public static String LOAD_DATA_IN_FILE_TRADE_SQL = "LOAD DATA LOCAL INFILE ? INTO TABLE " + TradeDisplay.TABLE_NAME
            + "%s CHARACTER SET 'utf8' FIELDS TERMINATED BY ',' " + " (" + ALL_FIELDS + "); ";

    public static long executeTradeLoadDataInFile(Long userId, File file) {
        String sql = TradeDisplay.genShardQuery(LOAD_DATA_IN_FILE_TRADE_SQL, userId);
        return TradeDisplay.dp.update(sql, file.getAbsolutePath());
    }

    public static void clearOld() {
        long oldTime = System.currentTimeMillis() - DateUtil.THIRTY_DAYS - (DateUtil.DAY_MILLIS * 2);
        for (int i = 0; i < 16; i++) {
            JDBCBuilder.update(false, TradeDisplay.dp.getSrc(), " delete from " + TradeDisplay.TABLE_NAME + i
                    + " where payTimeDay < ?", oldTime);
        }
    }

    /**
     * 应该只有两个地方要调整buyerArea这个字段
     * @param trade
     * @param records
     */
    public static void bindPayedUser(TradeDisplay trade, List<LinezingRecord> records) {
        TRADE_FROM platform = trade.genFromPlatfrom();
        if (platform != TRADE_FROM.TAOBAO) {
            // Not to bind ...
            return;
        }

        Long created = trade.getCreated();
        String buyerArea = trade.getBuyerArea();
        String city = trade.getReceiverCity();

        if (!StringUtils.isEmpty(buyerArea)) {
        }
    }

    public static MatchInfo findBestMatch(TradeDisplay trade, Map<String, MatchInfo> map) {
        String buyerArea = trade.getBuyerArea();

        List<MatchInfo> candidates = new ArrayList<MatchInfo>();

        boolean isNoBuyerAddr = StringUtils.isEmpty(buyerArea);
        IpDataBean bean = trade.genIpDataBean();
        if (bean == null || !bean.mightHasData()) {
            // No cadidates...
            Collection<MatchInfo> values = map.values();

            long tradeCreated = trade.getCreated();
            long currMin = Long.MAX_VALUE;
            MatchInfo bestMatch = null;
            for (MatchInfo matchInfo : values) {
                long diff = tradeCreated - matchInfo.middle;
                if (diff < currMin) {
                    bestMatch = matchInfo;
                    currMin = diff;
                }
            }
            log.info("[best match with middle:]" + bestMatch + " for trade create time:"
                    + DateUtil.formDateForLog(tradeCreated));
            return bestMatch;

        } else {

            Collection<MatchInfo> values = map.values();
            for (MatchInfo matchInfo : values) {
                matchInfo.computeMatchScore(trade, bean);
                candidates.add(matchInfo);
            }

            Collections.sort(candidates, new Comparator<MatchInfo>() {
                @Override
                public int compare(MatchInfo o1, MatchInfo o2) {
                    return o1.getScore() - o2.getScore();
                }
            });
            MatchInfo best = NumberUtil.first(candidates);
            log.info("[fiter for best:]" + best);
            return best;

        }
//        /**
//         * 假设地址的匹配没有找到合适的对象,那怎么办
//         */
//        if (CommonUtils.isEmpty(candidates)) {
//
//        }
    }

//
//    public boolean hasIspInfo(TradeDisplay trade){
//        return trade.getBuyerArea() != null
//    }

    public static Map<String, MatchInfo> buildMatch(List<LinezingRecord> records) {
        Map<String, MatchInfo> map = new HashMap<String, MatchInfo>();
        for (LinezingRecord linezingRecord : records) {
            String uv = linezingRecord.getUv();
            MatchInfo info = map.get(uv);
            if (info == null) {
                info = new MatchInfo(linezingRecord);
                map.put(uv, info);
            }
            info.addRecordCheck(linezingRecord);
            long numIid = linezingRecord.getCurNumIid();
            info.addPassNumIid(numIid);
        }
        return map;
    }

    static class MatchInfo {

        public MatchInfo(LinezingRecord record) {
            this.uv = record.getUv();
            this.country = record.getCountry();
            this.city = record.getCity();
            this.isp = record.getIsp();
            this.province = record.getProvince();
            this.locationName = record.getLocationName();
        }

        String locationName;

        /**
         * 目前好像只有这5个维度
         * @param trade
         * @param tradeBean
         * @return
         */
        public int computeMatchScore(TradeDisplay trade, IpDataBean tradeBean) {
            // TODO Auto-generated method stub
            int score = 0;

            if (this.locationName != null) {
                if (!StringUtils.isEmpty(tradeBean.getCity()) && this.locationName.contains(tradeBean.getCity())) {
                    score += 1500;
                }
                if (!StringUtils.isEmpty(tradeBean.getProvince())
                        && this.locationName.contains(tradeBean.getProvince())) {
                    score += 1200;
                }
                if (!StringUtils.isEmpty(tradeBean.getCountry()) && this.locationName.contains(tradeBean.getCountry())) {
                    score += 600;
                }
            }
            if (tradeBean.getCity().equals(city)) {
                score += 1000;
            }
            if (tradeBean.getProvince().equals(province)) {
                score += 800;
            }
            if (tradeBean.getCountry().equals(country)) {
                score += 100;
            }

            if (tradeBean.getIsp().equals(isp)) {
                score = 300;
            }

            boolean hasVisitPayed = false;
            if (CommonUtils.isEmpty(numIidsOnVisitPath)) {
                hasVisitPayed = false;
            } else {
                Set<Long> payedNumIids = trade.ensureNumIids();
                for (Long numIidOnPath : numIidsOnVisitPath) {
                    if (payedNumIids.contains(numIidOnPath)) {
                        hasVisitPayed = true;
                    }
                }
            }

            if (hasVisitPayed) {
                score += 1500;
            }

            long timeDiff = trade.getCreated() - outTime;
            if (timeDiff < 0L) {
                timeDiff = -timeDiff;
            }

            if (timeDiff < DateUtil.TEN_MINUTE_MILLIS) {
                score += 200;
            } else if (timeDiff < (2 * DateUtil.TEN_MINUTE_MILLIS)) {
                score += 150;
            } else if (timeDiff < (4 * DateUtil.TEN_MINUTE_MILLIS)) {
                score += 100;
            } else if (timeDiff < (2 * DateUtil.ONE_HOUR)) {
                score += 60;
            } else {
                score += 30;
            }

            this.score = score;
            log.info("[set score --- " + score + "]" + this);
            return score;
        }

        int score;

        public void addPassNumIid(long numIid) {
            numIidsOnVisitPath.add(numIid);
        }

        public void addRecordCheck(LinezingRecord record) {

            long curNumIid = record.getCurNumIid();
            if (curNumIid > 0L) {
                numIidsOnVisitPath.add(curNumIid);
            }

            long created = record.getCreated();
            if (this.inTime > created) {
                this.inTime = created;
            }
            if (this.outTime < created) {
                this.outTime = created;
            }
            this.middle = (inTime + outTime) / 2;
        }

        String uv;

        String country;

        String city;

        String isp;

        String province;

        long inTime = Long.MAX_VALUE;

        long outTime = Long.MIN_VALUE;

        long middle;

        Set<Long> numIidsOnVisitPath = new HashSet<Long>();

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public String getUv() {
            return uv;
        }

        public void setUv(String uv) {
            this.uv = uv;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getIsp() {
            return isp;
        }

        public void setIsp(String isp) {
            this.isp = isp;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public long getInTime() {
            return inTime;
        }

        public void setInTime(long inTime) {
            this.inTime = inTime;
        }

        public long getOutTime() {
            return outTime;
        }

        public void setOutTime(long outTime) {
            this.outTime = outTime;
        }

        public long getMiddle() {
            return middle;
        }

        public void setMiddle(long middle) {
            this.middle = middle;
        }

        public Set<Long> getNumIidsOnVisitPath() {
            return numIidsOnVisitPath;
        }

        public void setNumIidsOnVisitPath(Set<Long> numIidsOnVisitPath) {
            this.numIidsOnVisitPath = numIidsOnVisitPath;
        }

        public String getLocationName() {
            return locationName;
        }

        public void setLocationName(String locationName) {
            this.locationName = locationName;
        }

        @Override
        public String toString() {
            return "MatchInfo [score=" + score + ", uv=" + uv + ", country=" + country + ", city=" + city + ", isp="
                    + isp + ", province=" + province + ", inTime=" + DateUtil.formDateForLog(inTime) + ", outTime="
                    + DateUtil.formDateForLog(outTime) + ", middle=" + DateUtil.formDateForLog(middle)
                    + ", numIidsOnVisitPath=" + numIidsOnVisitPath + "]";
        }

    }

    public static void uploadVisitLogs(String data) {

    }

    public static List<TradeDisplay> findTodayCreated(User user) {
        Long userId = user.getId();
        long start = DateUtil.formCurrDate();
        long end = System.currentTimeMillis();
        String sql = TradeDisplay.genShardQuery(USER_TRADE_QUERY, userId)
                + " where userId = ? and created between ? and ?";
        // 御城河日志
        SimulateRequestUtil.sendSqlLog("订单详情", APIConfig.get().getRdsHostAddress(), sql);
        return new JDBCExecutor<List<TradeDisplay>>(TradeDisplay.dp, sql, userId, start, end) {
            @Override
            public List<TradeDisplay> doWithResultSet(ResultSet rs) throws SQLException {
                List<TradeDisplay> list = new ArrayList<TradeDisplay>();
                while (rs.next()) {
                    TradeDisplay trade = new TradeDisplay(rs);
                    if (trade != null) {
                        list.add(trade);
                    }
                }
                return list;
            }
        }.call();
    }

    public static Set<Long> todayCreatedValidPCTids(User user) {
        long start = DateUtil.formCurrDate();
        String sql = TradeDisplay.genShardQuery(" select tid from " + TradeDisplay.TABLE_NAME
                + "%s where userId = ? and created between ? and ? and tradeFrom = ? and " +
                "(status <> 'TRADE_NO_CREATE_PAY' and status <> 'WAIT_BUYER_PAY' and " +
                " status <> 'TRADE_CLOSED' and status <> 'TRADE_CLOSED_BY_TAOBAO' )", user.getId());

        // 御城河日志
        SimulateRequestUtil.sendSqlLog("订单编号", APIConfig.get().getRdsHostAddress(), sql);
        Set<Long> toDayTids = new JDBCLongSetExecutor(true, TradeDisplay.dp, sql, user.getId(), start,
                System.currentTimeMillis(), TRADE_FROM.TOP.ordinal()).call();

        return toDayTids;
    }

    public static List<TradeDisplay> findAllTrades(Long userId, Set<Long> tids) {
        String raw = USER_TRADE_QUERY + " where tid in (" + StringUtils.join(tids, ',') + ")";
        // 御城河日志
        SimulateRequestUtil.sendSqlLog("订单详情", APIConfig.get().getRdsHostAddress(), raw);
        String sql = TradeDisplay.genShardQuery(raw, userId);
        return new JDBCTradeListQuerier(TradeDisplay.dp, sql).call();
    }

    public static List<TradeDisplay> findTodayLinezingNotBindedPCTrades(User user) {
        Set<Long> allTids = todayCreatedValidPCTids(user);
        Set<Long> binded = TidLineZingBind.findTodayBinded(user);
        allTids.removeAll(binded);
        if (CommonUtils.isEmpty(allTids)) {
            log.warn(" all binded....");
        }

        return findAllTrades(user.getId(), allTids);
    }

    public static List<TradeDisplay> doBind(User user) {
        List<TradeDisplay> trades = findTodayLinezingNotBindedPCTrades(user);
        log.info("not binded trades : " + trades.size());
        log.info("[no bind trades:]" + PlayUtil.genPrettyGson().toJson(trades));
        return trades;
    }
    
    public static Set<Long> findTradeByUserId(User user){
        Long userId = user.getId();
        String sql = "select tid from " + TradeDisplay.TABLE_NAME + "%s where userId = ? limit 0,10";
        sql = TradeDisplay.genShardQuery(sql, userId);
        Set<Long> toDayTids = new JDBCLongSetExecutor(true, TradeDisplay.dp, sql, userId).call(); 
        return toDayTids;
    }
    
}
