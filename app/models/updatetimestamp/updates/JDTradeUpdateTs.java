
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

import com.ciaosir.client.utils.DateUtil;

/**
 * TODO No transaction later...
 * @author zrb
 *
 */
@Entity(name = JDTradeUpdateTs.TABLE_NAME)
public class JDTradeUpdateTs extends UserUpdateTimestamp {

    private static final Logger log = LoggerFactory.getLogger(JDTradeUpdateTs.class);

    public static long MAX_VALID_INTERVAL = DateUtil.WEEK_MILLIS;

    public static final String TAG = "JDTradeUpdateTs";

    public static final String TABLE_NAME = "jd_trade_update_ts";

    public JDTradeUpdateTs(Long userId) {
        super(userId);
    }

    public JDTradeUpdateTs(Long userId, long ts) {
        super(userId, ts);
    }
    
    public JDTradeUpdateTs(Long userId, long firstUpdateTime, long lastUpdateTime) {
        super(userId, firstUpdateTime, lastUpdateTime);
    } 

    public static void updateLastModifedTime(Long userId, long ts) {

        //JDTradeUpdateTs memberTs = JDTradeUpdateTs.findById(userId);
    	JDTradeUpdateTs memberTs = JDTradeUpdateTs.findByUserId(userId);
        if (memberTs == null) {
            log.warn("No User Found...Create it now for id:" + userId);
            new JDTradeUpdateTs(userId, ts).jdbcSave();
            return;
        }

        if (ts < memberTs.lastUpdateTime) {
            log.warn("ts[" + ts + "] is less than [" + memberTs.lastUpdateTime
                    + "], No Update");
            return;
        }

        memberTs.setLastUpdateTime(ts);
        memberTs.jdbcSave();

        log.info("save new update time successfully");
    }

    static String FIND_USERID_WITH_LASTUPDATETS = "select userId,last_ts from "
            + JDTradeUpdateTs.TABLE_NAME;

    public static Map<Long, Long> findUsreIdWithLastUpdateTs() {
        return new JDBCMapLongExecutor(
                FIND_USERID_WITH_LASTUPDATETS).call();
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
            String insertSQL = "insert into `"
                    + TABLE_NAME
                    + "`(`userId`,`first_ts`,`last_ts`) values(?,?,?)";
            
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

        String updateSQL = "update `"
                + TABLE_NAME
                + "` set `first_ts` = ?, `last_ts` = ? where `userId` = ?  ";

        this.lastUpdateTime = System.currentTimeMillis();
        
        long updateNum = JDBCBuilder.insert(updateSQL, this.firstUpdateTime, this.lastUpdateTime,
                this.userId);

        if (updateNum == 1) {

            return true;
        } else {

            return false;
        }
    }
    
    public static JDTradeUpdateTs findByUserId(Long userId) {

        String query = "select userId, first_ts, last_ts from " + TABLE_NAME
                + " where userId = ? ";

        return new JDBCBuilder.JDBCExecutor<JDTradeUpdateTs>(query, userId) {

            @Override
            public JDTradeUpdateTs doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {
                	return new JDTradeUpdateTs(rs.getLong(1),rs.getLong(2),rs.getLong(3));
                } else {
                    return null;
                }

            }

        }.call();
    }
}
