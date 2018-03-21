package models.updatetimestamp.updatets.process;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.persistence.Entity;

import jdbcexecutorwrapper.JDBCMapLongExecutor;
import models.updatetimestamp.UserUpdateTimestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.JDBCBuilder;

@Entity(name = TradeProcessUpdateTs.TABLE_NAME)
public class TradeProcessUpdateTs extends UserUpdateTimestamp {

    private static final Logger log = LoggerFactory.getLogger(TradeProcessUpdateTs.class);

	public static final String TAG = "TradeProcessUpdateTs";

	public static final String TABLE_NAME = "trade_process_update_ts";

	public TradeProcessUpdateTs(Long userId) {
		super(userId);
	}

	public TradeProcessUpdateTs(Long userId, long ts) {
		super(userId, ts);
	}

	public TradeProcessUpdateTs(ResultSet rs) throws SQLException {
        super();
        this.userId = rs.getLong(1);
        this.firstUpdateTime = rs.getLong(2);
        this.lastUpdateTime = rs.getLong(3);
    }
	
	public static void updateLastModifedTime(Long userId, long ts) {

		//TradeProcessUpdateTs memberTs = TradeProcessUpdateTs.findById(userId);
		TradeProcessUpdateTs memberTs = TradeProcessUpdateTs.findByUserId(userId);
		if (memberTs == null) {
			log.warn("No User Found...Create it now for id:" + userId);
			new TradeProcessUpdateTs(userId, ts).jdbcSave();

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
			+ TradeProcessUpdateTs.TABLE_NAME;

	public static Map<Long, Long> findUsreIdWithLastUpdateTs() {
		return new JDBCMapLongExecutor(
				FIND_USERID_WITH_LASTUPDATETS).call();
	}

	public static TradeProcessUpdateTs findByUserId(Long userId) {

        String query = "select userId, first_ts, last_ts from " + TABLE_NAME + " where userId = ? ";

        return new JDBCBuilder.JDBCExecutor<TradeProcessUpdateTs>(query, userId) {

            @Override
            public TradeProcessUpdateTs doWithResultSet(ResultSet rs) throws SQLException {

                if (rs.next()) {
                    return new TradeProcessUpdateTs(rs);
                } else {
                    return null;
                }
            }

        }.call();
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
}