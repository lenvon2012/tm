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

import com.ciaosir.client.pojo.PageOffset;

@Entity(name = RateSmsReceiveLog.TABLE_NAME)
public class RateSmsReceiveLog extends GenericModel implements PolicySQLGenerator {

	private static final Logger log = LoggerFactory.getLogger(RateSmsReceiveLog.class);
	
	public static final String TABLE_NAME = "rate_sms_receive_log_";
	
	public static final RateSmsReceiveLog EMPTY = new RateSmsReceiveLog();
	
	public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

	/**
	 * 回复短信Id
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long Id;
	
	/**
	 * 子订单编号
	 */
	public Long oid;
	
	/**
	 * 电话号码
	 */
	public String number;
	
	/**
	 * 短信内容
	 */
	public String content;
	
	/**
	 * 回复时间
	 */
	public Long receiveTs;
	
	/**
	 * 扩展码（关联rate_sms_send_log表的Id）
	 */
	public Long kzm;
	
	/**
	 * 用户提交的批次号
	 */
	public Long batchId;
	
	/**
	 * 是否已查看 false：未查看  true：已查看
	 */
	public Boolean status;
	
	/**
	 * 创建时间
	 */
	public Long createTs;
	
	/**
	 * 更新时间
	 */
	public Long updateTs;
	
	/**
	 * 用户Id
	 */
	public Long userId;

	public Long getOid() {
		return oid;
	}

	public void setOid(Long oid) {
		this.oid = oid;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Long getReceiveTs() {
		return receiveTs;
	}

	public void setReceiveTs(Long receiveTs) {
		this.receiveTs = receiveTs;
	}

	public Long getKzm() {
		return kzm;
	}

	public void setKzm(Long kzm) {
		this.kzm = kzm;
	}

	public Long getBatchId() {
		return batchId;
	}

	public void setBatchId(Long batchId) {
		this.batchId = batchId;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public Long getCreateTs() {
		return createTs;
	}

	public void setCreateTs(Long createTs) {
		this.createTs = createTs;
	}

	public Long getUpdateTs() {
		return updateTs;
	}

	public void setUpdateTs(Long updateTs) {
		this.updateTs = updateTs;
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

	public static RateSmsReceiveLog getEmpty() {
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
	
	public RateSmsReceiveLog() {
	
	}
	
	public RateSmsReceiveLog(Long oid, String number, String content, Long receiveTs, Long batchId, Boolean status, Long userId) {
		super();
		this.oid = oid;
		this.number = number;
		this.content = content;
		this.receiveTs = receiveTs;
		this.batchId = batchId;
		this.status = status;
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
				this.setId(existdId);
				return this.rawUpdate();
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
			return false;
		}
		
	}
	
	public boolean rawInsert() {
		this.createTs = System.currentTimeMillis();
		this.updateTs = System.currentTimeMillis();
		
		String insertSQL = "insert into `" + TABLE_NAME + "`(`oid`," +
				"`number`,`content`,`receiveTs`," +
				"`batchId`,`status`,`createTs`,`updateTs`,`userId`)" +
				" values(?,?,?,?,?,?,?,?,?,?)";

		long id = dp.insert(true, insertSQL, this.oid,
				this.number, this.content, this.receiveTs,
				this.batchId, this.status, this.createTs, this.updateTs, this.userId);

		if (id > 0L) {
			this.setId(id);
			return true;
		} else {
			log.error("Insert Fails....." + "[Id : ]" + this.Id + "userId : ]" + this.userId);
			return false;
		}
	}
	
	public boolean rawUpdate() {
		this.updateTs = System.currentTimeMillis();

		String updateSQL = "update `" + TABLE_NAME + "` set " +
				"`updateTs` = ?, " +
				"`status` = ? " +
				" where `Id` = ? ";
		
		long updateNum = dp.update(updateSQL, 
				this.updateTs, this.status, 
				this.Id);

		if (updateNum >= 1) {
			return true;
		} else {
			log.error("update failed...for :" + this.getId() + "[Id : ]" + this.Id + "[userId : ]" + this.userId);
			return false;
		}
	}
	
	public boolean rawDelete() {
		String sql = " delete from " + TABLE_NAME + " where Id = ? and userId = ? ";
		
		dp.update(sql, this.Id, this.userId);
		
		return true;
	}
	
	public static List<RateSmsReceiveLog> findByOid(Long oid, Long userId, PageOffset po) {
		String query = " SELECT " + SelectAllProperty + " from " + TABLE_NAME + " where oid = ? and userId = ? "
			+ " order by receiveTs desc limit ?,? ";
		return findListByJDBC(query, oid, userId, po.getOffset(), po.getPs());
	}
	
	public static int countReceivedMsgByOid(Long oid, Long userId) {
		String query = " select count(*) from " + TABLE_NAME + " where oid = ? and userId = ?";
		return (int) dp.singleLongQuery(query, oid, userId);
	}
	
	public static int countByOidAndStatus(Long oid, Boolean status, Long userId) {
		String query = " select count(*) from " + TABLE_NAME + " where oid = ? and status = ? and userId = ? ";
		return (int) dp.singleLongQuery(query, oid, status, userId);
	}
	
	private static RateSmsReceiveLog findByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<RateSmsReceiveLog>(dp, query, params) {

			@Override
			public RateSmsReceiveLog doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
	}
	
	private static List<RateSmsReceiveLog> findListByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<List<RateSmsReceiveLog>>(dp, query, params) {

			@Override
			public List<RateSmsReceiveLog> doWithResultSet(ResultSet rs) throws SQLException {

				List<RateSmsReceiveLog> resultList = new ArrayList<RateSmsReceiveLog>();

				while (rs.next()) {
					RateSmsReceiveLog result = parseResult(rs);
					if (result != null) {
						resultList.add(result);
					}
				}

				return resultList;
			}

		}.call();
	}
	
	private static final String SelectAllProperty = " `Id`, `oid`, `number`, `content`, `receiveTs`, " +
			"`kzm`, `batchId`, `status`, `createTs`, `updateTs`, `userId` ";

	private static RateSmsReceiveLog parseResult(ResultSet rs) {
		try {
			
			RateSmsReceiveLog rptObj = new RateSmsReceiveLog();
			
			int colIndex = 1;
			
			rptObj.Id = rs.getLong(colIndex++);
			rptObj.oid = rs.getLong(colIndex++);
			rptObj.number = rs.getString(colIndex++);
			rptObj.content = rs.getString(colIndex++);
			rptObj.receiveTs = rs.getLong(colIndex++);
			rptObj.kzm = rs.getLong(colIndex++);
			rptObj.batchId = rs.getLong(colIndex++);
			rptObj.status = rs.getBoolean(colIndex++);
			rptObj.createTs = rs.getLong(colIndex++);
			rptObj.updateTs = rs.getLong(colIndex++);
			rptObj.userId = rs.getLong(colIndex++);
			return rptObj;
			
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
	}

}
