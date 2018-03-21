package models.traderatesms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = RateSmsRechargeLog.TABLE_NAME)
public class RateSmsRechargeLog extends GenericModel implements PolicySQLGenerator {

	private static final Logger log = LoggerFactory.getLogger(RateSmsRechargeLog.class);
	
	public static final String TABLE_NAME = "rate_sms_recharge_log_";
	
	public static final RateSmsRechargeLog EMPTY = new RateSmsRechargeLog();
	
	public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

	/**
	 * 充值记录Id
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long Id;
	
	/**
	 * 充值短信数
	 */
	public Long count;
	
	/**
	 * 充值时间
	 */
	public Long rechargeTs;
	
	/**
	 * 用户昵称
	 */
	public String userNick;
	
	/**
	 * 用户Id
	 */
	public Long userId;

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public Long getRechargeTs() {
		return rechargeTs;
	}

	public void setRechargeTs(Long rechargeTs) {
		this.rechargeTs = rechargeTs;
	}

	public String getUserNick() {
		return userNick;
	}

	public void setUserNick(String userNick) {
		this.userNick = userNick;
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

	public static RateSmsRechargeLog getEmpty() {
		return EMPTY;
	}

	public static DBDispatcher getDp() {
		return dp;
	}

	public static String getSelectallproperty() {
		return SelectAllProperty;
	}

	public void setId(long id) {
		Id = id;
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
		return "Id";
	}

	@Override
	public Long getId() {
		return Id;
	}

	@Override
	public void setId(Long id) {
		this.Id = id;
	}

	@Override
	public String getIdName() {
		return "Id";
	}
	
	public RateSmsRechargeLog() {
	
	}
	
	public RateSmsRechargeLog(long count, String userNick, long userId) {
		super();
		this.count = count;
		this.userNick = userNick;
		this.userId = userId;
	}
	
	public static Long findExistId(Long userId, Long Id) {
		String query = "select Id from " + TABLE_NAME + " where userId = ? and Id = ? ";
		
		return dp.singleLongQuery(query, userId, Id);
	}
	
	@Override
	public boolean jdbcSave() {
		
		try {
			long existdId = findExistId(this.userId, this.Id);
			if (existdId <= 0L) {
				return this.rawInsert();
			} else {
//				this.setId(existdId);
//				return this.rawUpdate();
				return false;
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
			return false;
		}
		
	}
	
	public boolean rawInsert() {
		this.rechargeTs = System.currentTimeMillis();
		
		String insertSQL = "insert into `" + TABLE_NAME + "`(`count`," +
				"`rechargeTs`,`userNick`,`userId`)" +
				" values(?,?,?,?)";

		long id = dp.insert(true, insertSQL, this.count,
				this.rechargeTs, this.userNick, this.userId);

		if (id > 0L) {
			this.setId(id);
			return true;
		} else {
			log.error("Insert Fails....." + "[Id : ]" + this.Id + "userId : ]" + this.userId);
			return false;
		}
	}
	
//	public boolean rawUpdate() {
//		this.updateTs = System.currentTimeMillis();
//
//		String updateSQL = "update `" + TABLE_NAME + "` set " +
//				"`updateTs` = ?, " +
//				"`success` = ?, `status` = ? " +
//				" where `Id` = ? ";
//		
//		long updateNum = dp.update(updateSQL, 
//				this.updateTs, this.success, this.status, 
//				this.Id);
//
//		if (updateNum >= 1) {
//			return true;
//		} else {
//			log.error("update failed...for :" + this.getId() + "[Id : ]" + this.Id + "[userId : ]" + this.userId);
//			return false;
//		}
//	}
	
	public boolean rawDelete() {
		String sql = " delete from " + TABLE_NAME + " where Id = ? and userId = ? ";
		
		dp.update(sql, this.Id, this.userId);
		
		return true;
	}
	
	private static RateSmsRechargeLog findByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<RateSmsRechargeLog>(dp, query, params) {

			@Override
			public RateSmsRechargeLog doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
	}
	
	private static List<RateSmsRechargeLog> findListByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<List<RateSmsRechargeLog>>(dp, query, params) {

			@Override
			public List<RateSmsRechargeLog> doWithResultSet(ResultSet rs) throws SQLException {

				List<RateSmsRechargeLog> resultList = new ArrayList<RateSmsRechargeLog>();

				while (rs.next()) {
					RateSmsRechargeLog result = parseResult(rs);
					if (result != null) {
						resultList.add(result);
					}
				}

				return resultList;
			}

		}.call();
	}

	private static final String SelectAllProperty = " `Id`, `count`, `rechargeTs`, `userNick`, " +
			"`userId` ";

	private static RateSmsRechargeLog parseResult(ResultSet rs) {
		try {
			
			RateSmsRechargeLog rptObj = new RateSmsRechargeLog();
			
			int colIndex = 1;
			
			rptObj.Id = rs.getLong(colIndex++);
			rptObj.count = rs.getLong(colIndex++);
			rptObj.rechargeTs = rs.getLong(colIndex++);
			rptObj.userNick = rs.getString(colIndex++);
			rptObj.userId = rs.getLong(colIndex++);
			return rptObj;
			
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
	}

}
