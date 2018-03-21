package models.defense;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import job.defense.TradeRateMsgDealer.TradeRateMsg;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = TMCMsgLog.TABLE_NAME)
public class TMCMsgLog extends Model implements PolicySQLGenerator {

	private static final Logger log = LoggerFactory.getLogger(TMCMsgLog.class);
	
	public static final String TABLE_NAME = "tmc_msg_log";
	
	public static final TMCMsgLog EMPTY = new TMCMsgLog();
	
	public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

	@Index(name = "tid")
	public Long tid;
	
	@Index(name = "oid")
	public Long oid;
	
	public String content;
	
	@Index(name = "createTs")
	public Long createTs;
	
	@Index(name = "userId")
	public Long userId;

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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Long getCreateTs() {
		return createTs;
	}

	public void setCreateTs(Long createTs) {
		this.createTs = createTs;
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

	public static TMCMsgLog getEmpty() {
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
	
	public TMCMsgLog() {
	
	}
	
	public TMCMsgLog(Long tid, Long oid, String content, Long userId) {
		super();
		this.tid = tid;
		this.oid = oid;
		this.content = content;
		this.userId = userId;
	}
	
	public TMCMsgLog(TradeRateMsg msg) {
		super();
		this.tid = msg.getTid();
		this.oid = msg.getOid();
		this.content = String.valueOf(msg.getContent());
		this.userId = msg.getUserId();
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
		this.createTs = System.currentTimeMillis();
		
		String insertSQL = "insert into `" + TABLE_NAME + "`(`tid`," +
				"`oid`,`content`,`createTs`,`userId`)" +
				" values(?,?,?,?,?)";

		long id = dp.insert(true, insertSQL, this.tid,
				this.oid, this.content, this.createTs, this.userId);

		if (id > 0L) {
			this.setId(id);
			return true;
		} else {
			log.error("Insert Fails....." + "[tid]" + this.tid + "~~~[oid]" + this.oid);
			return false;
		}
	}
	
	public boolean rawDelete() {
		String sql = " delete from " + TABLE_NAME + " where id = ? ";
		
		dp.update(sql, this.id);
		
		return true;
	}
	
	private static TMCMsgLog findByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<TMCMsgLog>(dp, query, params) {

			@Override
			public TMCMsgLog doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
	}
	
	private static List<TMCMsgLog> findListByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<List<TMCMsgLog>>(dp, query, params) {

			@Override
			public List<TMCMsgLog> doWithResultSet(ResultSet rs) throws SQLException {

				List<TMCMsgLog> resultList = new ArrayList<TMCMsgLog>();

				while (rs.next()) {
					TMCMsgLog result = parseResult(rs);
					if (result != null) {
						resultList.add(result);
					}
				}

				return resultList;
			}

		}.call();
	}
	
	private static final String SelectAllProperty = " `id`, `tid`, `oid`, `content`, `createTs`, `userId` ";

	private static TMCMsgLog parseResult(ResultSet rs) {
		try {
			
			TMCMsgLog rptObj = new TMCMsgLog();
			
			int colIndex = 1;
			
			rptObj.id = rs.getLong(colIndex++);
			rptObj.tid = rs.getLong(colIndex++);
			rptObj.oid = rs.getLong(colIndex++);
			rptObj.content = rs.getString(colIndex++);
			rptObj.createTs = rs.getLong(colIndex++);
			rptObj.userId = rs.getLong(colIndex++);
			return rptObj;
			
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
	}

}
