package models.defense;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = TradeRateSyncLog.TABLE_NAME)
public class TradeRateSyncLog extends Model implements PolicySQLGenerator {

	private static final Logger log = LoggerFactory.getLogger(TradeRateSyncLog.class);
	
	public static final String TABLE_NAME = "trade_rate_sync_log";
	
	public static final TradeRateSyncLog EMPTY = new TradeRateSyncLog();
	
	public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

	@Index(name = "apiCount")
	public int apiCount;
	
	@Index(name = "startTs")
	public Long startTs;
	
	@Index(name = "endTs")
	public Long endTs;
	
	@Index(name = "userNick")
	public String userNcik;
	
	@Index(name = "userId")
	public Long userId;

	public int getApiCount() {
		return apiCount;
	}

	public void setApiCount(int apiCount) {
		this.apiCount = apiCount;
	}

	public Long getStartTs() {
		return startTs;
	}

	public void setStartTs(Long startTs) {
		this.startTs = startTs;
	}

	public Long getEndTs() {
		return endTs;
	}

	public void setEndTs(Long endTs) {
		this.endTs = endTs;
	}

	public String getUserNcik() {
		return userNcik;
	}

	public void setUserNcik(String userNcik) {
		this.userNcik = userNcik;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public static Logger getLog() {
		return log;
	}

	public static TradeRateSyncLog getEmpty() {
		return EMPTY;
	}

	public static DBDispatcher getDp() {
		return dp;
	}

	public static String getSelectallproperty() {
		return SelectAllProperty;
	}

	@Override
	public String getTableName() {
		return TABLE_NAME;
	}

	@Override
	public String getTableHashKey() {
		return null;
	}

	@Override
	public String getIdColumn() {
		return "id";
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String getIdName() {
		return "id";
	}
	
	public TradeRateSyncLog() {
	
	}
	
	public TradeRateSyncLog(int apiCount, Long startTs, Long endTs,
			String userNcik, Long userId) {
		super();
		this.apiCount = apiCount;
		this.startTs = startTs;
		this.endTs = endTs;
		this.userNcik = userNcik;
		this.userId = userId;
	}

	public static Long findExistId(Long id) {
		String query = "select id from " + TABLE_NAME + " where id = ? ";
		
		return dp.singleLongQuery(query, id);
	}
	
	@Override
	public boolean jdbcSave() {
		
		try {
			long existdId = findExistId(this.id);
			if (existdId <= 0L) {
				return this.rawInsert();
			} else {
				return false;
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
			return false;
		}
		
	}
	
	public boolean rawInsert() {
		String insertSQL = "insert into `" + TABLE_NAME + "`(`apiCount`," +
				"`startTs`,`endTs`,`userNcik`,`userId`)" +
				" values(?,?,?,?,?)";

		long id = dp.insert(true, insertSQL, this.apiCount,
				this.startTs, this.endTs, this.userNcik, this.userId);

		if (id > 0L) {
			this.setId(id);
			return true;
		} else {
			log.error("Insert Fails....." + "[userNcik]" + this.userNcik);
			return false;
		}
	}
	
	public boolean rawDelete() {
		String sql = " delete from " + TABLE_NAME + " where id = ? ";
		
		dp.update(sql, this.id);
		
		return true;
	}
	
	private static TradeRateSyncLog findByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<TradeRateSyncLog>(dp, query, params) {

			@Override
			public TradeRateSyncLog doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
	}
	
	private static List<TradeRateSyncLog> findListByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<List<TradeRateSyncLog>>(dp, query, params) {

			@Override
			public List<TradeRateSyncLog> doWithResultSet(ResultSet rs) throws SQLException {

				List<TradeRateSyncLog> resultList = new ArrayList<TradeRateSyncLog>();

				while (rs.next()) {
					TradeRateSyncLog result = parseResult(rs);
					if (result != null) {
						resultList.add(result);
					}
				}

				return resultList;
			}

		}.call();
	}
	
	private static final String SelectAllProperty = " `id`, `apiCount`, `startTs`, `endTs`, `userNcik`, `userId` ";

	private static TradeRateSyncLog parseResult(ResultSet rs) {
		try {
			
			TradeRateSyncLog rptObj = new TradeRateSyncLog();
			
			int colIndex = 1;
			
			rptObj.id = rs.getLong(colIndex++);
			rptObj.apiCount = rs.getInt(colIndex++);
			rptObj.startTs = rs.getLong(colIndex++);
			rptObj.endTs = rs.getLong(colIndex++);
			rptObj.userNcik = rs.getString(colIndex++);
			rptObj.userId = rs.getLong(colIndex++);
			return rptObj;
			
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
	}

}
