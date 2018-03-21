package models.carrierTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Entity;

import org.apache.commons.lang.StringUtils;
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
import actions.carriertask.ItemCarrierForXXXAction.CarryItem;

@JsonIgnoreProperties(value = { "dataSrc", "persistent", "entityId", "entityId", "ts", "tableHashKey", "persistent", "tableName", "idName", "idColumn" })
@Entity(name = SubCarrierTaskForXXX.TABLE_NAME)
public class SubCarrierTaskForXXX extends Model implements PolicySQLGenerator<Long> {

	private static final Logger log = LoggerFactory.getLogger(SubCarrierTaskForXXX.class);

	public static final String TABLE_NAME = "sub_carrier_task_for_xxx";

	public static final SubCarrierTaskForXXX EMPTY = new SubCarrierTaskForXXX();

	public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

	/**
	 * 任务Id（CarrierTaskForXXX表主键）
	 */
	@Index(name = "taskId")
	private Long taskId;
	
	/**
	 * 商品Id
	 */
	@Index(name = "numIid")
	private Long numIid;
	
	/**
	 * 商品来源（复制来源）
	 */
	@Index(name = "source")
	private int source;
	
	/**
	 * 商品去向（复制去向）
	 */
	@Index(name = "target")
	private int target;
	
	/**
	 * 商品标题
	 */
	@Index(name = "title")
	private String title;
	
	/**
	 * 商品主图链接
	 */
	@Index(name = "picUrl")
	private String picUrl;
	
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
	
	private Long userId;
	
	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public Long getNumIid() {
		return numIid;
	}

	public void setNumIid(Long numIid) {
		this.numIid = numIid;
	}

	public int getSource() {
		return source;
	}

	public void setSource(int source) {
		this.source = source;
	}

	public int getTarget() {
		return target;
	}

	public void setTarget(int target) {
		this.target = target;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
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

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public static Logger getLog() {
		return log;
	}

	public static SubCarrierTaskForXXX getEmpty() {
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
	
	public SubCarrierTaskForXXX() {
	
	}
	
	public SubCarrierTaskForXXX(Long taskId, CarryItem item, int status, Long createTs, Long userId) {
		super();
		this.taskId = taskId;
		this.numIid = item.getNumIid();
		this.source = item.getSource();
		this.target = item.getTarget();
		this.status = status;
		this.createTs = createTs;
		this.updateTs = 0L;
		this.finishTs = 0L;
		this.userId = userId;
	}
	
	public static class SubCarrierTaskForXXXStatus {
		public static final int PREPARING = 0; // 子任务创建成功，等待获取商品数据
		public static final int PREPARED = 1; // 数据准备就绪，等待图片回传
		public static final int RUNNING = 2; // 复制进行中
		public static final int SUCCESS = 4; // 复制成功
		public static final int FAIL = 8; // 复制失败
	}
	
	public static long findExistId(Long id) {
	
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
		
		String insertSQL = "insert into `" + TABLE_NAME + "` (`taskId`," +
				"`numIid`,`source`,`target`,`title`,`picUrl`,`status`,`msg`,) " +
				"`createTs`,`updateTs`,`finishTs`,`userId`) " +
				" values(?,?,?,?,?,?,?,?,?,?)";

		long id = dp.insert(insertSQL, this.taskId, 
				this.numIid, this.source, this.target, this.title, this.picUrl, this.status, this.msg,
				this.createTs, this.updateTs, this.finishTs, this.userId);

		if (id > 0L) {
			this.setId(id);
			return true;
		} else {
			log.error("sub carry task insert failed.....[userId] : " + this.userId + "[taskId] : " + this.taskId);
			return false;
		}
	}
	
	public boolean rawUpdate() {
		this.updateTs = System.currentTimeMillis();
		if(this.status >= SubCarrierTaskForXXXStatus.SUCCESS) {
			this.finishTs = this.updateTs;
		}

		String updateSQL = "update `" + TABLE_NAME + "` set " +
				"`title` = ?, `picUrl` = ?, `status` = ?, `msg` = ?, " +
				"`updateTs` = ?, `finishTs` = ? " +
				"where `id` = ? ";
		
		long updateNum = dp.update(updateSQL, 
				this.title, this.picUrl, this.status, this.msg,
				this.updateTs, this.finishTs,
				this.id);

		if (updateNum >= 1) {
			return true;
		} else {
			log.error("sub carry task update failed.....[userId] : " + this.userId + "[taskId] : " + this.taskId);
			return false;
		}
	}
	
	public boolean rawDelete() {

		String sql = " delete from " + TABLE_NAME + " where id = ? ";

		dp.update(sql, this.id);

		return true;
	}
	
	public static SubCarrierTaskForXXX findById(Long id) {
		String query = " SELECT " + SelectAllProperty + " from " + TABLE_NAME + " where id = ? ";
		
		return findByJDBC(query, id);
	}
	
	public static List<SubCarrierTaskForXXX> findByTaskId(Long taskId) {
		String query = " SELECT " + SelectAllProperty + " from " + TABLE_NAME + " where taskId = ? ";
		
		return findListByJDBC(query, taskId);
	}
	
	public static void updateSubTask(String title, String picUrl, int status, String msg, SubCarrierTaskForXXX subTask) {
		if(!StringUtils.isEmpty(title)) {
			subTask.setTitle(title);
		}
		if(!StringUtils.isEmpty(picUrl)) {
			subTask.setPicUrl(picUrl);
		}
		
		subTask.setStatus(status);
		subTask.setMsg(msg);
		
		subTask.jdbcSave();
		
		if(status == SubCarrierTaskForXXXStatus.SUCCESS) {
			new CarrierItemPlay(subTask).jdbcSave();
		}
	}
	
	private static SubCarrierTaskForXXX findByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<SubCarrierTaskForXXX>(dp, query, params) {

			@Override
			public SubCarrierTaskForXXX doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
	}
	
	private static List<SubCarrierTaskForXXX> findListByJDBC(String query,Object... params) {
		
		return new JDBCBuilder.JDBCExecutor<List<SubCarrierTaskForXXX>>(dp, query, params) {

			@Override
			public List<SubCarrierTaskForXXX> doWithResultSet(ResultSet rs)
					throws SQLException {

				List<SubCarrierTaskForXXX> resultList = new ArrayList<SubCarrierTaskForXXX>();

				while (rs.next()) {
					SubCarrierTaskForXXX result = parseResult(rs);
					if (result != null) {
						resultList.add(result);
					}
				}

				return resultList;
			}

		}.call();
	}
	
	public static String sqlSave = "INSERT INTO " + TABLE_NAME + "(taskId, numIid, source, target, title, picUrl, status, msg, createTs, userId) "
			+ "VALUES(?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE title = ?, picUrl = ?, status = ?, msg = ?, updateTs = ?, finishTs = ? ";
	
	public static boolean batchInsert(List<SubCarrierTaskForXXX> subTasks) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DBBuilder.getConn(DataSrc.BASIC);
			conn.setAutoCommit(false);
			ps = conn.prepareStatement(sqlSave, 1);
			Iterator<SubCarrierTaskForXXX> iterator = subTasks.iterator();
			while(iterator.hasNext()){
				SubCarrierTaskForXXX subTask = iterator.next();
				setArgs(subTask, ps);
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
	
	private static void setArgs(SubCarrierTaskForXXX subTask, PreparedStatement ps) throws SQLException {
		int num = 0;
		ps.setLong(++num, subTask.getTaskId());
		ps.setLong(++num, subTask.getNumIid());
		ps.setInt(++num, subTask.getSource());
		ps.setInt(++num, subTask.getTarget());
		ps.setString(++num, subTask.getTitle());
		ps.setString(++num, subTask.getPicUrl());
		ps.setInt(++num, subTask.getStatus());
		ps.setString(++num, subTask.getMsg());
		ps.setLong(++num, subTask.getCreateTs());
		ps.setLong(++num, subTask.getUserId());
		// 更新数据
		ps.setString(++num, subTask.getTitle());
		ps.setString(++num, subTask.getPicUrl());
		ps.setInt(++num, subTask.getStatus());
		ps.setString(++num, subTask.getMsg());
		ps.setLong(++num, subTask.getUpdateTs());
		ps.setLong(++num, subTask.getFinishTs());
	}
	
	private static final String SelectAllProperty = " id,taskId,numIid,source,target,title,picUrl,status,msg,createTs,updateTs,finishTs,userId ";
	
	private static SubCarrierTaskForXXX parseResult(ResultSet rs) {
		try {
			
			SubCarrierTaskForXXX rptObj = new SubCarrierTaskForXXX();
			
			int colIndex = 1;
			
			rptObj.id = rs.getLong(colIndex++);
			rptObj.taskId = rs.getLong(colIndex++);
			rptObj.numIid = rs.getLong(colIndex++);
			rptObj.source = rs.getInt(colIndex++);
			rptObj.target = rs.getInt(colIndex++);
			rptObj.title = rs.getString(colIndex++);
			rptObj.picUrl = rs.getString(colIndex++);
			rptObj.status = rs.getInt(colIndex++);
			rptObj.msg = rs.getString(colIndex++);
			rptObj.createTs = rs.getLong(colIndex++);
			rptObj.updateTs = rs.getLong(colIndex++);
			rptObj.finishTs = rs.getLong(colIndex++);
			rptObj.userId = rs.getLong(colIndex++);
			
			return rptObj;
			
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
	}

}
