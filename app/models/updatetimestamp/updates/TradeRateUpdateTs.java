package models.updatetimestamp.updates;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.persistence.Entity;

import jdbcexecutorwrapper.JDBCMapLongExecutor;
import models.updatetimestamp.UserUpdateTimestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.JDBCBuilder;
import utils.DateUtil;

@Entity(name = TradeRateUpdateTs.TABLE_NAME)
public class TradeRateUpdateTs extends UserUpdateTimestamp {

    private static final Logger log = LoggerFactory.getLogger(TradeRateUpdateTs.class);

    public static final String TABLE_NAME = "trade_rate_update_ts";

    public static final String TAG = "TradeRateUpdateTs";

//    public static final long MAX_AVAILABLE_SPAN = DateUtil.THIRTY_DAYS;
    public static final long MAX_AVAILABLE_SPAN = 60 * DateUtil.DAY_MILLIS;
    
    public TradeRateUpdateTs(Long userId) {
        super(userId);
    }

    public TradeRateUpdateTs(Long userId, long ts) {
        super(userId, ts);
    }

    public TradeRateUpdateTs(Long userId, long firstUpdateTime, long lastUpdateTime) {
        super(userId, firstUpdateTime, lastUpdateTime);
    }

    public static void updateLastTradeRateModifedTime(Long userId, long ts) {

        // TradeRateUpdateTs rateTs = TradeRateUpdateTs.findById(userId);
        TradeRateUpdateTs rateTs = TradeRateUpdateTs.findByUserId(userId);
        if (rateTs == null) {
            log.warn("No User Found...Create it now for id:" + userId);
            new TradeRateUpdateTs(userId, ts).jdbcSave();
            return;
        }

        if (ts < rateTs.lastUpdateTime) {
            log.warn("ts[" + ts + "] is less than [" + rateTs.lastUpdateTime + "], No Update");
            return;
        }

        rateTs.setLastUpdateTime(ts);
        rateTs.jdbcSave();

        log.info("save new update time successfully");
    }

    static String FIND_USERID_WITH_LASTUPDATETS = "select userId,last_ts from " + TradeRateUpdateTs.TABLE_NAME;

    public static Map<Long, Long> findUsreIdWithLastUpdateTs() {
        return new JDBCMapLongExecutor(FIND_USERID_WITH_LASTUPDATETS).call();
    }

    static String deleteSQL = "delete from " + TABLE_NAME + " where `userId` = ? ";

    public static boolean jdbcDelete(Long userId) {

        long id = JDBCBuilder.insert(false, deleteSQL, userId);

        // log.info("[delete from RefundTidProcessingIds userId:]" + userId);

        if (id > 0L) {
            return true;
        } else {
            log.error("delete Fails....." + "[userId : ]" + userId);
            return false;
        }
    }

    public static long findExistId(Long userId) {

        String query = "select userId from " + TABLE_NAME + " where userId = ? ";

        return JDBCBuilder.singleLongQuery(query, userId);
    }

    public boolean jdbcSave() {
        try {

            long existId = findExistId(this.userId);

            if (existId <= 0) {
                return this.rawInsert();
            } else {
                return this.rawUpdate();
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }

    public String getIdName() {
        return "userId";
    }

    public boolean rawInsert() {
        try {
            String insertSQL = "insert into `" + TABLE_NAME + "`(`userId`,`first_ts`,`last_ts`) values(?,?,?)";

            long id = JDBCBuilder.insert(insertSQL, this.userId, this.firstUpdateTime, this.lastUpdateTime);

            if (id > 0L) {
                return true;
            } else {
                return false;
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);

            return false;
        }
    }

    public boolean rawUpdate() {

        String updateSQL = "update `" + TABLE_NAME + "` set `first_ts` = ?, `last_ts` = ? where `userId` = ?  ";

        this.lastUpdateTime = System.currentTimeMillis();

        long updateNum = JDBCBuilder.insert(updateSQL, this.firstUpdateTime, this.lastUpdateTime, this.userId);

        if (updateNum == 1) {

            return true;
        } else {

            return false;
        }
    }

    public static TradeRateUpdateTs findByUserId(Long userId) {

        String query = "select userId, first_ts, last_ts from " + TABLE_NAME + " where userId = ? ";

        return new JDBCBuilder.JDBCExecutor<TradeRateUpdateTs>(query, userId) {

            @Override
            public TradeRateUpdateTs doWithResultSet(ResultSet rs) throws SQLException {

                if (rs.next()) {
                    return new TradeRateUpdateTs(rs.getLong(1), rs.getLong(2), rs.getLong(3));
                } else {
                    return null;
                }

            }

        }.call();
    }
}
