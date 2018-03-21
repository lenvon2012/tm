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

/**
 * 店群复制个数限制
 */
@JsonIgnoreProperties(value = { "dataSrc", "persistent", "entityId", "entityId", "ts", "tableHashKey", "persistent", "tableName", "idName", "idColumn" })
@Entity(name = CarrierLimitForDQ.TABLE_NAME)
public class CarrierLimitForDQ extends Model implements PolicySQLGenerator<Long> {

	private static final Logger log = LoggerFactory.getLogger(CarrierLimitForDQ.class);

	public static final String TABLE_NAME = "carrier_limit_for_dq";

	public static final CarrierLimitForDQ EMPTY = new CarrierLimitForDQ();

	public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
	
	private static final int LIMIT_COUNT = 500;

	/**
	 * 当月已复制商品个数
	 */
	@Index(name = "useCount")
	private int useCount;
	
	/**
	 * 当月可复制商品总数
	 */
	@Index(name = "limitCount")
	private int limitCount;
	
	/**
	 * 该用户总计复制的商品总数
	 */
	@Index(name = "totalCount")
	private int totalCount;
	
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
	 * 用户id
	 */
	@Index(name = "userId")
	private Long userId;
	
	public int getUseCount() {
		return useCount;
	}

	public void setUseCount(int useCount) {
		this.useCount = useCount;
	}

	public int getLimitCount() {
		return limitCount;
	}

	public void setLimitCount(int limitCount) {
		this.limitCount = limitCount;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
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

	public static CarrierLimitForDQ getEmpty() {
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
	
	public CarrierLimitForDQ() {
	
	}
	
	public CarrierLimitForDQ(int useCount, int limitCount, int totalCount,
			Long userId) {
		super();
		this.useCount = useCount;
		this.limitCount = limitCount;
		this.totalCount = totalCount;
		this.userId = userId;
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
		this.updateTs = this.createTs;
		
		String insertSQL = "insert into `" + TABLE_NAME + "` (`useCount`,`limitCount`,`totalCount`," +
				"`createTs`,`updateTs`,`userId`) " +
				" values(?,?,?,?,?,?)";

		long id = dp.insert(insertSQL, this.useCount, this.limitCount, this.totalCount,
				this.createTs, this.updateTs, this.userId);

		if (id > 0L) {
			this.setId(id);
			return true;
		} else {
			log.error("Insert failed.....[userId] : " + this.userId);
			return false;
		}
	}
	
	public boolean rawUpdate() {
		this.updateTs = System.currentTimeMillis();

		String updateSQL = "update `" + TABLE_NAME + "` set " +
				"`useCount` = ?, `limitCount` = ?, " +
				"`totalCount` = ?, `updateTs` = ? " +
				"where `id` = ? ";
		
		long updateNum = dp.update(updateSQL, 
				this.useCount, this.limitCount,
				this.totalCount, this.updateTs,
				this.id);

		if (updateNum >= 1) {
			return true;
		} else {
			log.error("Update failed.....[userId] : " + this.userId);
			return false;
		}
	}
	
	public boolean rawDelete() {

		String sql = " delete from " + TABLE_NAME + " where id = ? ";

		dp.update(sql, this.id);

		return true;
	}
	
	public static CarrierLimitForDQ findByUserId(Long userId) {
		String query = " SELECT " + SelectAllProperty + " from " + TABLE_NAME + " where userId = ? ";
		
		return findByJDBC(query, userId);
	}
	
	public static boolean updateLimitCountByUserId(Long newLimit, Long userId) {
		Long updateTs = System.currentTimeMillis();
		
		String sql = "update `" + TABLE_NAME + "` set `limitCount` = ?, `updateTs` = ? where userId = ?";

		return dp.update(sql, newLimit, updateTs, userId) > 0;
	}
	
	public static boolean updateUseCountByUserId(Long userId) {
		Long updateTs = System.currentTimeMillis();
		
		String sql = "update `" + TABLE_NAME + "` set `useCount` = useCount + 1, `totalCount` = totalCount + 1, `updateTs` = ? where userId = ?";

		return dp.update(sql, updateTs, userId) > 0;
	}
	
	public static Boolean checkUserLimit(Long userId) {
		CarrierLimitForDQ exist = findByUserId(userId);
		if(exist == null) {
			synchronized(CarrierLimitForDQ.class){
				CarrierLimitForDQ limit = findByUserId(userId);
				if(limit == null) {
					new CarrierLimitForDQ(0, LIMIT_COUNT, 0, userId).rawInsert();
				}
			}
			return true;
		}
		
		if(exist.getUseCount() >= exist.getLimitCount()) {
			return false;
		}
		
		return true;
	}
	
	private static CarrierLimitForDQ findByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<CarrierLimitForDQ>(dp, query, params) {

			@Override
			public CarrierLimitForDQ doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
	}
	
	private static List<CarrierLimitForDQ> findListByJDBC(String query,Object... params) {
		
		return new JDBCBuilder.JDBCExecutor<List<CarrierLimitForDQ>>(dp, query, params) {

			@Override
			public List<CarrierLimitForDQ> doWithResultSet(ResultSet rs)
					throws SQLException {

				List<CarrierLimitForDQ> resultList = new ArrayList<CarrierLimitForDQ>();

				while (rs.next()) {
					CarrierLimitForDQ result = parseResult(rs);
					if (result != null) {
						resultList.add(result);
					}
				}

				return resultList;
			}

		}.call();
	}

	private static final String SelectAllProperty = " id,useCount,limitCount,totalCount,createTs,updateTs,userId";
	
	private static CarrierLimitForDQ parseResult(ResultSet rs) {
		try {
			
			CarrierLimitForDQ rptObj = new CarrierLimitForDQ();
			
			int colIndex = 1;
			
			rptObj.id = rs.getLong(colIndex++);
			rptObj.useCount = rs.getInt(colIndex++);
			rptObj.limitCount = rs.getInt(colIndex++);
			rptObj.totalCount = rs.getInt(colIndex++);
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
