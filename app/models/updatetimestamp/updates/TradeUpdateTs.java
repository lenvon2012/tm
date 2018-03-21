
package models.updatetimestamp.updates;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.persistence.Entity;

import jdbcexecutorwrapper.JDBCMapLongExecutor;
import models.updatetimestamp.UserUpdateTimestamp;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.JDBCBuilder;

import com.ciaosir.client.utils.DateUtil;

/**
 * TODO No transaction later...
 * 
 * @author zrb
 * 
 */
@Entity(name = TradeUpdateTs.TABLE_NAME)
public class TradeUpdateTs extends UserUpdateTimestamp {

    private static final Logger log = LoggerFactory.getLogger(TradeUpdateTs.class);

    public static long MAX_VALID_INTERVAL = DateUtil.WEEK_MILLIS;

    public static final String TAG = "TradeUpdateTs";

    public static final String TABLE_NAME = "trade_update_ts";

    public TradeUpdateTs(Long userId) {
        super(userId);
    }

    public TradeUpdateTs(Long userId, long ts) {
        super(userId, ts);
    }

    public TradeUpdateTs(ResultSet rs) throws SQLException {
        super();
        this.userId = rs.getLong(1);
        this.firstUpdateTime = rs.getLong(2);
        this.lastUpdateTime = rs.getLong(3);
    }

    public TradeUpdateTs(Long userId, long firstUpdateTime, long lastUpdateTime) {
        super(userId, firstUpdateTime, lastUpdateTime);
    }

    public static void updateLastModifedTime(Long userId, long ts) {

        // TradeUpdateTs memberTs = TradeUpdateTs.findById(userId);
//        TradeUpdateTs memberTs = TradeUpdateTs.findByUserId(userId);
//        if (memberTs == null) {
//            log.warn("No User Found...Create it now for id:" + userId);
//            new TradeUpdateTs(userId, ts).jdbcSave();
//            return;
//        }
//
//        if (ts < memberTs.lastUpdateTime) {
//            log.warn("ts[" + ts + "] is less than [" + memberTs.lastUpdateTime + "], No Update");
//            return;
//        }
//        memberTs.setLastUpdateTime(ts);
//        memberTs.jdbcSave();
        
        new TradeUpdateTs(userId, ts).rawInsertOnDupUpdate();

        log.info("save new update time successfully");
    }

    static String FIND_USERID_WITH_LASTUPDATETS = "select userId,last_ts from " + TradeUpdateTs.TABLE_NAME;

    public static Map<Long, Long> findUsreIdWithLastUpdateTs() {
        return new JDBCMapLongExecutor(FIND_USERID_WITH_LASTUPDATETS).call();
    }

    public static TradeUpdateTs findByUser(User user) {
        return fetch("userId = ?", user.getId());
    }

    public static TradeUpdateTs findByUser(Long userId) {
        return fetch("userId = ?", userId);
    }

    public static TradeUpdateTs fetch(String whereQuery, Object... parms) {
        return new JDBCBuilder.JDBCExecutor<TradeUpdateTs>("select userId, first_ts, last_ts from " + TABLE_NAME
                + " where " + whereQuery, parms) {

            @Override
            public TradeUpdateTs doWithResultSet(java.sql.ResultSet rs) throws SQLException {
                while (rs.next()) {
                    return new TradeUpdateTs(rs);
                }
                return null;
            }
        }.call();
    }

    public static boolean exist(Long userId) {
        return JDBCBuilder.singleLongQuery("select userId from " + TABLE_NAME + " where userId=  ?", userId) > 0L;
    }

    static String deleteSQL = "delete from " + TABLE_NAME + " where `userId` = ? ";

    public void _save() {
        this.jdbcSave();
    }

    public static boolean jdbcDelete(Long userId) {

        long id = JDBCBuilder.insert(false, deleteSQL, userId);

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
    
    public boolean rawInsertOnDupUpdate() {
        try {
            String sql = "insert into `" + TABLE_NAME + "`(`userId`,`first_ts`,`last_ts`) values(?,?,?) on duplicate key update `last_ts` = ?";

            long id = JDBCBuilder.insert(sql, this.userId, this.firstUpdateTime, this.lastUpdateTime, this.lastUpdateTime);

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

    public static TradeUpdateTs findByUserId(Long userId) {

        String query = "select userId, first_ts, last_ts from " + TABLE_NAME + " where userId = ? ";

        return new JDBCBuilder.JDBCExecutor<TradeUpdateTs>(query, userId) {

            @Override
            public TradeUpdateTs doWithResultSet(ResultSet rs) throws SQLException {

                if (rs.next()) {
                    return new TradeUpdateTs(rs);
                } else {
                    return null;
                }
            }

        }.call();
    }
}
