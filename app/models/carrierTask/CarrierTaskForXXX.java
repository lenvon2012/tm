package models.carrierTask;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.CodeGenerator.DBDispatcher;
import transaction.CodeGenerator.PolicySQLGenerator;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;

@JsonIgnoreProperties(value = { "dataSrc", "persistent", "entityId", "entityId", "ts", "tableHashKey", "persistent", "tableName", "idName", "idColumn" })
@Entity(name = CarrierTaskForXXX.TABLE_NAME)
public class CarrierTaskForXXX extends Model implements PolicySQLGenerator<Long> {

	private static final Logger log = LoggerFactory.getLogger(CarrierTaskForXXX.class);

	public static final String TABLE_NAME = "carrier_task_for_xxx";

	public static final CarrierTaskForXXX EMPTY = new CarrierTaskForXXX();

	public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
	
	private static int DEFAULT_VALUE = 0;

	/**
	 * 商品数量
	 */
	@Index(name = "itemCount")
	private int itemCount;
	
	/**
	 * 成功数量
	 */
	@Index(name = "successCount")
	private int successCount;
	
	/**
	 * 完成数量
	 */
	@Index(name = "finishCount")
	private int finishCount;
	
	/**
	 * 任务类型
	 */
	@Index(name = "type")
	private int type;
	
	/**
	 * 任务进程
	 */
	@Index(name = "status")
	private int status;
	
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
	 * 目标旺旺
	 */
	@Index(name = "ww")
	private String ww;
	
	/**
	 * 用户昵称
	 */
	@Index(name = "userNick")
	private String userNick;
	
	/**
	 * 用户Id
	 */
	@Index(name = "userId")
	private Long userId;
	
	public int getItemCount() {
		return itemCount;
	}

	public void setItemCount(int itemCount) {
		this.itemCount = itemCount;
	}

	public int getSuccessCount() {
		return successCount;
	}

	public void setSuccessCount(int successCount) {
		this.successCount = successCount;
	}

	public int getFinishCount() {
		return finishCount;
	}

	public void setFinishCount(int finishCount) {
		this.finishCount = finishCount;
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

	public String getWw() {
		return ww;
	}

	public void setWw(String ww) {
		this.ww = ww;
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

	public static CarrierTaskForXXX getEmpty() {
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
	
	public CarrierTaskForXXX() {
	
	}
	
	public CarrierTaskForXXX(int itemCount, int type, int status, String ww,
			String userNick, Long userId) {
		super();
		this.itemCount = itemCount;
		this.successCount = DEFAULT_VALUE;
		this.finishCount = DEFAULT_VALUE;
		this.type = type;
		this.status = status;
		this.ww = ww;
		this.userNick = userNick;
		this.userId = userId;
	}

	public static class CarrierTaskForXXXStatus {
		public static final int CREATED = 0; // 任务创建成功，等待复制中
		public static final int RUNNING = 1; // 复制进行中
		public static final int FINISHED = 2; // 复制完成
	}
	
	public static class CarrierTaskForXXXType {
		public static final int SINGLE = 1; // 单独复制任务
		public static final int SHOP = 2; // 店铺复制任务
		public static final int BATCH = 4; // 批量复制任务
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
		this.updateTs = this.createTs;
		this.finishTs = 0L;
		
		String insertSQL = "insert into `" + TABLE_NAME + "` (`itemCount`,`successCount`,`finishCount`,`status`,`type`," +
				"`createTs`,`updateTs`,`finishTs`,`ww`,`userNick`,`userId`) " +
				" values(?,?,?,?,?,?,?,?,?,?,?)";

		long id = dp.insert(insertSQL, this.itemCount, this.successCount, this.finishCount, this.status, this.type,
				this.createTs, this.updateTs, this.finishTs, this.ww, this.userNick, this.userId);

		if (id > 0L) {
			this.setId(id);
			return true;
		} else {
			log.error("Carry task insert failed.....[userId] : " + this.userId);
			return false;
		}
	}
	
	public boolean rawUpdate() {
		this.updateTs = System.currentTimeMillis();
		if(this.status == CarrierTaskForXXXStatus.FINISHED) {
			this.finishTs = this.updateTs;
		}

		String updateSQL = "update `" + TABLE_NAME + "` set " +
				"`successCount` = ?, `finishCount` = ?, `status` = ?, " +
				"`updateTs` = ?, `finishTs` = ? " +
				"where `id` = ? ";
		
		long updateNum = dp.update(updateSQL, 
				this.successCount, this.finishCount, this.status,
				this.updateTs, this.finishTs,
				this.id);

		if (updateNum >= 1) {
			return true;
		} else {
			log.error("Carry task update failed.....[userId] : " + this.userId);
			return false;
		}
	}
	
	public boolean rawDelete() {

		String sql = " delete from " + TABLE_NAME + " where id = ? ";

		dp.update(sql, this.id);

		return true;
	}
	
	public static CarrierTaskForXXX findById(Long id) {
		String query = " SELECT " + SelectAllProperty + " from " + TABLE_NAME + " where id = ? ";
		
		return findByJDBC(query, id);
	}
	
	public static List<CarrierTaskForXXX> findByUserId(Long userId) {
		String query = " SELECT " + SelectAllProperty + " from " + TABLE_NAME + " where userId = ? ";
		
		return findListByJDBC(query, userId);
	}
	
	public static void addOneFinishCount(Long taskId, Boolean success) {
		CarrierTaskForXXX task = CarrierTaskForXXX.findById(taskId);
		
		if(success) {
			int successCount = task.getSuccessCount() + 1;
			task.setSuccessCount(successCount);
		}
		
		int finishCount = task.getFinishCount() + 1;
		task.setFinishCount(finishCount);
		if (finishCount >= task.getItemCount()) {
			task.setStatus(CarrierTaskForXXXStatus.FINISHED);
		} else {
			task.setStatus(CarrierTaskForXXXStatus.RUNNING);
		}
		
		log.error("addOneFinishCount taskId: " + task + "~~~ itemCount: " + task.getItemCount() + "~~~ finishCount: " + finishCount);
		task.jdbcSave();
	}
	
	private static CarrierTaskForXXX findByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<CarrierTaskForXXX>(dp, query, params) {

			@Override
			public CarrierTaskForXXX doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
	}
	
	private static List<CarrierTaskForXXX> findListByJDBC(String query,Object... params) {
		
		return new JDBCBuilder.JDBCExecutor<List<CarrierTaskForXXX>>(dp, query, params) {

			@Override
			public List<CarrierTaskForXXX> doWithResultSet(ResultSet rs)
					throws SQLException {

				List<CarrierTaskForXXX> resultList = new ArrayList<CarrierTaskForXXX>();

				while (rs.next()) {
					CarrierTaskForXXX result = parseResult(rs);
					if (result != null) {
						resultList.add(result);
					}
				}

				return resultList;
			}

		}.call();
	}

	private static final String SelectAllProperty = " id,itemCount,successCount,finishCount,status,type,createTs,updateTs,finishTs,ww,userNick,userId ";
	
	private static CarrierTaskForXXX parseResult(ResultSet rs) {
		try {
			
			CarrierTaskForXXX rptObj = new CarrierTaskForXXX();
			
			int colIndex = 1;
			
			rptObj.id = rs.getLong(colIndex++);
			rptObj.itemCount = rs.getInt(colIndex++);
			rptObj.successCount = rs.getInt(colIndex++);
			rptObj.finishCount = rs.getInt(colIndex++);
			rptObj.status = rs.getInt(colIndex++);
			rptObj.type = rs.getInt(colIndex++);
			rptObj.createTs = rs.getLong(colIndex++);
			rptObj.updateTs = rs.getLong(colIndex++);
			rptObj.finishTs = rs.getLong(colIndex++);
			rptObj.ww = rs.getString(colIndex++);
			rptObj.userNick = rs.getString(colIndex++);
			rptObj.userId = rs.getLong(colIndex++);
			
			return rptObj;
			
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
	}

}
