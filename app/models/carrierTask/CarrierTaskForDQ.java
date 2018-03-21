package models.carrierTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Entity;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.CodeGenerator.DBDispatcher;
import transaction.CodeGenerator.PolicySQLGenerator;
import transaction.DBBuilder;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import utils.DateUtil;
import utils.PlayUtil;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;

/**
 * 淘掌柜复制合作（店群）
 */
@JsonIgnoreProperties(value = { "dataSrc", "persistent", "entityId", "entityId", "ts", "tableHashKey", "persistent", "tableName", "idName", "idColumn" })
@Entity(name = CarrierTaskForDQ.TABLE_NAME)
public class CarrierTaskForDQ extends Model implements PolicySQLGenerator<Long> {

	private static final Logger log = LoggerFactory.getLogger(CarrierTaskForDQ.class);

	public static final String TABLE_NAME = "carrier_task_for_dq";

	public static final CarrierTaskForDQ EMPTY = new CarrierTaskForDQ();

	public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

	/**
	 * 商品Id
	 */
	@Index(name = "numIid")
	private Long numIid;
	
	/**
	 * 商品类目id(1688复制的时候需带上此参数)
	 */
	@Index(name = "cid")
	private Long cid;
	
	/**
	 * 复制类型
	 */
	@Index(name = "type")
	private int type;
	
	/**
	 * 复制进程
	 */
	@Index(name = "status")
	private int status;
	
	/**
	 * 复制相关信息
	 */
	@Index(name = "msg")
	private String msg;
	
	/**
	 * 创建时间
	 */
	@Index(name = "createTs")
	private Long createTs;
	
	/**
	 * 更新时间
	 */
	@Index(name = "updateTs")
	private Long updateTs;
	
	/**
	 * 完成时间
	 */
	@Index(name = "finishTs")
	private Long finishTs;
	
	/**
	 * 重试次数
	 */
	private int retry;
	
	private Long userId;
	
	public Long getNumIid() {
		return numIid;
	}

	public void setNumIid(Long numIid) {
		this.numIid = numIid;
	}

	public Long getCid() {
		return cid;
	}

	public void setCid(Long cid) {
		this.cid = cid;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
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

	public Long getFinishTs() {
		return finishTs;
	}

	public void setFinishTs(Long finishTs) {
		this.finishTs = finishTs;
	}

	public int getRetry() {
		return retry;
	}

	public void setRetry(int retry) {
		this.retry = retry;
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

	public static CarrierTaskForDQ getEmpty() {
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
	public String getIdColumn() {
		return "id";
	}
	
	@Override
	public String getTableHashKey(Long t) {
		return null;
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
	
	public CarrierTaskForDQ() {
	
	}
	
	public CarrierTaskForDQ(Long numIid, int status, Long userId, int type, Long cid) {
		super();
		this.numIid = numIid;
		this.status = status;
		this.userId = userId;
		this.type = type;
		this.cid = cid;
	}
	
	public static class CarrierTaskForDQType {
		public static final int TB = 1; // 淘宝 -> 淘宝
		public static final int ALIBABA = 2; // 1688 -> 淘宝
	}
	
	public static class CarrierTaskForDQStatus {
		public static final int WAITING = 0; // 等待复制
		public static final int RUNNING = 1; // 复制进行中
		public static final int SUCCESS = 2; // 复制成功
		public static final int FAIL = 4; // 复制失败
	}

	public static long findExistId(Long id, Long userId) {
	
		String query = "select id from " + TABLE_NAME + " where id = ? and userId = ? ";
		
		return dp.singleLongQuery(query, id, userId);
	
	}
	
	@Override
	public boolean jdbcSave() {
		
		try {
			long existdId = findExistId(this.id, this.userId);
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
		
		String insertSQL = "insert into `" + TABLE_NAME + "` (`numIid`,`type`,`cid`,`status`,`msg`," +
				"`createTs`,`updateTs`,`finishTs`,`retry`,`userId`) " +
				" values(?,?,?,?,?,?,?,?,?,?)";

		long id = dp.insert(insertSQL, this.numIid, this.type, this.cid, this.status, this.msg,
				this.createTs, this.updateTs, this.finishTs, this.retry, this.userId);

		if (id > 0L) {
			this.setId(id);
			return true;
		} else {
			log.error("Insert failed.....[userId] : " + this.userId + "[numIid] : " + this.numIid);
			return false;
		}
	}
	
	public boolean rawUpdate() {
		this.updateTs = System.currentTimeMillis();

		String updateSQL = "update `" + TABLE_NAME + "` set " +
				"`status` = ?, `msg` = ?, " +
				"`updateTs` = ?, `finishTs` = ?, `retry` = ? " +
				"where `id` = ? ";
		
		long updateNum = dp.update(updateSQL, 
				this.status, this.msg,
				this.updateTs, this.finishTs, this.retry,
				this.id);

		if (updateNum >= 1) {
			return true;
		} else {
			log.error("Update failed.....[userId] : " + this.userId + "[numIid] : " + this.numIid);
			return false;
		}
	}
	
	public boolean rawDelete() {

		String sql = " delete from " + TABLE_NAME + " where id = ? ";

		dp.update(sql, this.id);

		return true;
	}
	
	public static CarrierTaskForDQ findById(Long id) {
		String query = " SELECT " + SelectAllProperty + " from " + TABLE_NAME + " where id = ? ";
		
		return findByJDBC(query, id);
	}
	
	public static List<CarrierTaskForDQ> getNeedSubmitTask(int limit) {
		String query = " SELECT " + SelectAllProperty + " from " + TABLE_NAME + " where status = ?" +
				" order by id asc limit ? ";
		
		return findListByJDBC(query, CarrierTaskForDQStatus.WAITING, limit);
	}
	
	public static List<CarrierTaskForDQ> getNeedResetTask() {
		long limitTime = System.currentTimeMillis() - DateUtil.ONE_MINUTE_MILLIS * 25;
		
		String query = " SELECT " + SelectAllProperty + " from " + TABLE_NAME + " where status = ?" +
				" and updateTs < ? and retry < ? order by id asc limit ? ";
		
		return findListByJDBC(query, CarrierTaskForDQStatus.RUNNING, limitTime, 3, 16);
	}
	
	public static boolean resetTask(Long id, String msg) {
		Long updateTs = System.currentTimeMillis();
		
		String sql = "update `" + TABLE_NAME + "` set `status` = ?, `msg` = ?, `updateTs` = ?, `retry` = retry + 1 where id = ?";

		return dp.update(sql, CarrierTaskForDQStatus.WAITING, msg, updateTs, id) > 0;
	}
	
	public static boolean delTask(Long id, String msg) {
		
		String sql = "delete from `" + TABLE_NAME + "`  where id = ?";

		return dp.update(sql,  id) > 0;
	}
	
	public static boolean startTask(Long id) {
		Long updateTs = System.currentTimeMillis();
		
		String sql = "update `" + TABLE_NAME + "` set `status` = ?, `updateTs` = ? where id = ?";

		return dp.update(sql, CarrierTaskForDQStatus.RUNNING, updateTs, id) > 0;
	}
	
	public static boolean finishTask(Long id, boolean success, String msg) {
		Long updateTs = System.currentTimeMillis();
		Long finishTs = updateTs;
		
		int status = CarrierTaskForDQStatus.FAIL;
		if(success) {
			status = CarrierTaskForDQStatus.SUCCESS;
		}
		
		String sql = "update `" + TABLE_NAME + "` set `status` = ?, `updateTs` = ?, `finishTs` = ?, `msg` = ? where id = ?";

		return dp.update(sql, status, updateTs, finishTs, msg, id) > 0;
	}
	
	public static List<CarrierTaskForDQ> findBySearchRules(Long taskId,
			Long numIid, int type, Long cid, int status, String msg,
			Long startTime, Long endTime, Long userId, PageOffset po) {
		
		List<Object> paramList = new ArrayList<Object>();
		String whereSql = genWhereSqlByRules(taskId, numIid, type, cid, status, msg, startTime, endTime, userId, paramList);

		String query = "select " + SelectAllProperty + " from "
				+ TABLE_NAME + " where " + whereSql;

		query += " order by createTs desc";

		if(po != null) {
			query += " limit ?, ? ";
			paramList.add(po.getOffset());
			paramList.add(po.getPs());
		}
		Object[] paramArray = paramList.toArray();
		return findListByJDBC(query, paramArray);
	}
	
	private static String genWhereSqlByRules(Long taskId,
			Long numIid, int type, Long cid, int status, String msg,
			Long startTime, Long endTime, Long userId, List<Object> paramList) {

		String whereSql = " 1=1 "; // 规范sql语句
		
		if(taskId != null && taskId > 0) {
			whereSql += " and id = ?";
			paramList.add(taskId);
		}
		
		if(numIid != null && numIid > 0) {
			whereSql += " and numIid = ?";
			paramList.add(numIid);
		}
		
		if(userId != null && userId > 0) {
			whereSql += " and userId = ?";
			paramList.add(userId);
		}
		
		if(startTime != null && startTime > 0) {
			whereSql += " and createTs >= ?";
			paramList.add(startTime);
		}
		
		if(endTime != null && endTime > 0) {
			whereSql += " and createTs <= ?";
			paramList.add(endTime);
		}
		
		if(type > 0) {
			whereSql += " and type = ?";
			paramList.add(type);
		}
		
		if(cid != null && cid > 0) {
			whereSql += " and cid = ?";
			paramList.add(cid);
		}
		
		if(status >= 0) {
			whereSql += " and status = ?";
			paramList.add(status);
		}
		
		msg = PlayUtil.trimValue(msg);
		if (!StringUtils.isEmpty(msg)) {
			msg = CommonUtils.escapeSQL(msg);
			whereSql += " and msg like '%" + msg + "%' ";
		}
		
		return whereSql;

	}
	
	public static int countBySearchRules(Long taskId,
			Long numIid, int type, Long cid, int status, String msg,
			Long startTime, Long endTime, Long userId) {

		List<Object> paramList = new ArrayList<Object>();

		String whereSql = genWhereSqlByRules(taskId, numIid, type, cid, status, msg, startTime, endTime, userId, paramList);

		String query = " select count(*) from " + TABLE_NAME + " where " + whereSql;

		Object[] paramArray = paramList.toArray();

		return (int) dp.singleLongQuery(query, paramArray);
	}
	
	public static String startSql = "update " + TABLE_NAME + " set status = ?, updateTs = ? where id = ? ";
	
	public static boolean batchStartTask(List<CarrierTaskForDQ> tasks) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DBBuilder.getConn(DataSrc.BASIC);
			conn.setAutoCommit(false);
			String sql = startSql;
			ps = conn.prepareStatement(sql, 1);
			Iterator<CarrierTaskForDQ> iterator = tasks.iterator();
			while(iterator.hasNext()){
				CarrierTaskForDQ task = iterator.next();
				setArgs(task, ps);
				ps.addBatch();
			}
			ps.executeBatch();
			conn.commit();
			return true;
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		} finally {
			JDBCBuilder.closeQuitely(conn);
			JDBCBuilder.closeQuitely(ps);
		}
		return false;
	}
	
	private static void setArgs(CarrierTaskForDQ task, PreparedStatement ps) throws SQLException {
		int num = 0;
		
		Long updateTs = System.currentTimeMillis();
		
		ps.setInt(++num, CarrierTaskForDQStatus.RUNNING);
		ps.setLong(++num, updateTs);
		ps.setLong(++num, task.getId());
	}
	
	private static CarrierTaskForDQ findByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<CarrierTaskForDQ>(dp, query, params) {

			@Override
			public CarrierTaskForDQ doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
	}
	
	private static List<CarrierTaskForDQ> findListByJDBC(String query,Object... params) {
		
		return new JDBCBuilder.JDBCExecutor<List<CarrierTaskForDQ>>(dp, query, params) {

			@Override
			public List<CarrierTaskForDQ> doWithResultSet(ResultSet rs)
					throws SQLException {

				List<CarrierTaskForDQ> resultList = new ArrayList<CarrierTaskForDQ>();

				while (rs.next()) {
					CarrierTaskForDQ result = parseResult(rs);
					if (result != null) {
						resultList.add(result);
					}
				}

				return resultList;
			}

		}.call();
	}

	private static final String SelectAllProperty = " id,numIid,type,cid,status,msg,createTs,updateTs,finishTs,retry,userId ";
	
	private static CarrierTaskForDQ parseResult(ResultSet rs) {
		try {
			
			CarrierTaskForDQ rptObj = new CarrierTaskForDQ();
			
			int colIndex = 1;
			
			rptObj.id = rs.getLong(colIndex++);
			rptObj.numIid = rs.getLong(colIndex++);
			rptObj.type = rs.getInt(colIndex++);
			rptObj.cid = rs.getLong(colIndex++);
			rptObj.status = rs.getInt(colIndex++);
			rptObj.msg = rs.getString(colIndex++);
			rptObj.createTs = rs.getLong(colIndex++);
			rptObj.updateTs = rs.getLong(colIndex++);
			rptObj.finishTs = rs.getLong(colIndex++);
			rptObj.retry = rs.getInt(colIndex++);
			rptObj.userId = rs.getLong(colIndex++);
			
			return rptObj;
			
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
	}

}
