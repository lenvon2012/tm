package models.carrierTask;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.Entity;

import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.CodeGenerator.DBDispatcher;
import transaction.CodeGenerator.PolicySQLGenerator;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import transaction.JPATransactionManager;
import bustbapi.CarrierItemApi.itemSkuGet;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.Sku;

import dao.UserDao;

@JsonIgnoreProperties(value = { "dataSrc", "persistent", "entityId", "entityId", "ts", "tableHashKey", "persistent", "tableName", "idName", "idColumn" })
@Entity(name = CarrierItemPlay.TABLE_NAME)
public class CarrierItemPlay extends Model implements PolicySQLGenerator<Long> {

	private static final Logger log = LoggerFactory.getLogger(CarrierItemPlay.class);

	public static final String TABLE_NAME = "carrier_item_play";

	public static final CarrierItemPlay EMPTY = new CarrierItemPlay();

	public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
	
	/**
	 * 现商品id
	 */
	@Index(name = "numIid")
	private Long numIid;
	
	/**
	 * 现商品所属平台
	 * 1-淘宝,2-京东,4-1688, 8-拼多多
	 */
	@Index(name = "platform")
	private int platform;
	
	/**
	 * 现商品sku库存字符串
	 * 格式为skuId:库存值:outerId;skuId:库存值:outerId
	 * outerId为原商品skuId
	 */
	private String skuQuantities;
	
	/**
	 * 原商品id
	 */
	@Index(name = "originNumIid")
	private Long originNumIid;
	
	/**
	 * 原商品所属平台
	 * 1-淘宝,2-京东,4-1688, 8-拼多多
	 */
	@Index(name = "originPlatform")
	private int originPlatform;
	
	/**
	 * 原商品sku库存字符串
	 * 格式为skuId:库存值;skuId:库存值
	 */
	private String originSkuQuantities;
	
	/**
	 * 复制子任务id
	 */
	@Index(name = "subTaskId")
	private Long subTaskId;
	
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
	 * 用户Id
	 */
	@Index(name = "userId")
	private Long userId;
	
	public Long getNumIid() {
		return numIid;
	}

	public void setNumIid(Long numIid) {
		this.numIid = numIid;
	}

	public int getPlatform() {
		return platform;
	}

	public void setPlatform(int platform) {
		this.platform = platform;
	}

	public String getSkuQuantities() {
		return skuQuantities;
	}

	public void setSkuQuantities(String skuQuantities) {
		this.skuQuantities = skuQuantities;
	}

	public Long getOriginNumIid() {
		return originNumIid;
	}

	public void setOriginNumIid(Long originNumIid) {
		this.originNumIid = originNumIid;
	}

	public int getOriginPlatform() {
		return originPlatform;
	}

	public void setOriginPlatform(int originPlatform) {
		this.originPlatform = originPlatform;
	}

	public String getOriginSkuQuantities() {
		return originSkuQuantities;
	}

	public void setOriginSkuQuantities(String originSkuQuantities) {
		this.originSkuQuantities = originSkuQuantities;
	}

	public Long getSubTaskId() {
		return subTaskId;
	}

	public void setSubTaskId(Long subTaskId) {
		this.subTaskId = subTaskId;
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

	public static CarrierItemPlay getEmpty() {
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
	
	public CarrierItemPlay() {
	
	}
	
	public CarrierItemPlay(Long numIid, int platform, Long originNumIid,
			int originPlatform, Long subTaskId, Long userId) {
		super();
		this.numIid = numIid;
		this.platform = platform;
		this.originNumIid = originNumIid;
		this.originPlatform = originPlatform;
		this.subTaskId = subTaskId;
		this.userId = userId;
	}
	
	public CarrierItemPlay(Long numIid, Long originNumIid, User user) {
		super();
		this.numIid = numIid;
		this.platform = CarrierItemPlatform.TB;
		this.skuQuantities = getSkuQuantitiesStr(this.numIid, user, true);
		this.originNumIid = originNumIid;
		this.originPlatform = CarrierItemPlatform.TB;
		this.originSkuQuantities = getSkuQuantitiesStr(this.originNumIid, user, false);
		this.subTaskId = 0L;
		this.userId = user.getId();
	}

	public CarrierItemPlay(SubCarrierTaskForXXX subTask) {
		super();
		User user = UserDao.findById(subTask.getUserId());
		if(user == null) {
			log.error("why user is null??? userId:" + subTask.getUserId());
			return;
		}
		this.numIid = Long.valueOf(subTask.getMsg());
		this.platform = subTask.getTarget();
		this.skuQuantities = getSkuQuantitiesStr(this.numIid, user, true);
		this.originNumIid = subTask.getNumIid();
		this.originPlatform = subTask.getSource();
		this.originSkuQuantities = getSkuQuantitiesStr(this.originNumIid, user, false);
		this.subTaskId = subTask.getId();
		this.userId = subTask.getUserId();
	}

	private String getSkuQuantitiesStr(Long numIid, User user, Boolean isNewItem) {
		String skuQuantities = StringUtils.EMPTY;
		
		itemSkuGet itemSkuGet = new itemSkuGet(String.valueOf(numIid), user.getSessionKey());
		List<Sku> skus = itemSkuGet.call();
		if(skus == null) {
			return skuQuantities;
		}
		for (Sku sku : skus) {
			skuQuantities += sku.getSkuId() + ":" + sku.getQuantity();
			if(isNewItem) {
				skuQuantities += ":" + sku.getOuterId();
			}
			skuQuantities += ";";
		}
		if(!StringUtils.isEmpty(skuQuantities)) {
			skuQuantities = skuQuantities.substring(0, skuQuantities.length() - 1);
		}
		
		return skuQuantities;
	}

	public static class CarrierItemPlatform {
		public static final int TB = 1; // 淘宝
		public static final int JD = 2; // 京东
		public static final int ALIBABA = 4; // 1688
		public static final int PDD = 8; // 拼多多
	}

	public static long findExistId(Long numIid, Long userId) {
	
		String query = "select id from " + TABLE_NAME + " where numIid = ? and userId = ? ";
		
		return dp.singleLongQuery(query, numIid, userId);
	
	}
	
	@Override
	public boolean jdbcSave() {
		
		try {
			long existdId = findExistId(this.numIid, this.userId);
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
		
		String insertSQL = "insert into `" + TABLE_NAME + "` (`numIid`,`platform`,`skuQuantities`,`originNumIid`,`originPlatform`,`originSkuQuantities`," +
				"`subTaskId`,`createTs`,`updateTs`,`userId`) " +
				" values(?,?,?,?,?,?,?,?,?,?)";

		long id = dp.insert(insertSQL, this.numIid, this.platform, this.skuQuantities, this.originNumIid, this.originPlatform, this.originSkuQuantities,
				this.subTaskId, this.createTs, this.updateTs, this.userId);

		if (id > 0L) {
			this.setId(id);
			return true;
		} else {
			log.error("Carrier item insert failed.....[numIid] : " + this.numIid + "[userId] : " + this.userId);
			return false;
		}
	}
	
	public boolean rawUpdate() {
		this.updateTs = System.currentTimeMillis();

		String updateSQL = "update `" + TABLE_NAME + "` set " +
				"`skuQuantities` = ?, `originSkuQuantities` = ?, " +
				"`updateTs` = ? " +
				"where `id` = ? ";
		
		long updateNum = dp.update(updateSQL, 
				this.skuQuantities, this.originSkuQuantities,
				this.updateTs,
				this.id);

		if (updateNum >= 1) {
			return true;
		} else {
			log.error("Carry task update failed.....[numIid] : " + this.numIid + "[userId] : " + this.userId);
			return false;
		}
	}
	
	public boolean rawDelete() {

		String sql = " delete from " + TABLE_NAME + " where id = ? ";

		dp.update(sql, this.id);

		return true;
	}
	
	public static CarrierItemPlay findById(Long id) {
		String query = " SELECT " + SelectAllProperty + " from " + TABLE_NAME + " where id = ? ";
		
		return findByJDBC(query, id);
	}
	
	public static List<CarrierItemPlay> findByUserId(Long userId) {
		String query = " SELECT " + SelectAllProperty + " from " + TABLE_NAME + " where userId = ? ";
		
		return findListByJDBC(query, userId);
	}
	
	public static final List<CarrierItemPlay> findItemList(int offset, int limit) {
		String query = " SELECT " + SelectAllProperty + " from " + TABLE_NAME + " order by id desc limit ? offset ? ";
		
		return findListByJDBC(query, limit, offset);
	}
	
	public static abstract class CarrierItemBatchOper implements Callable<Boolean> {
		
		public int offset = 0;

		public int limit = 32;

		protected long sleepTime = 500L;
		
		public CarrierItemBatchOper(int limit) {
			super();
			this.limit = limit;
		}

		public CarrierItemBatchOper(int offset, int limit) {
			super();
			this.offset = offset;
			this.limit = limit;
		}

		public CarrierItemBatchOper(int offset, int limit, long sleepTime) {
			super();
			this.offset = offset;
			this.limit = limit;
			this.sleepTime = sleepTime;
		}

		public List<CarrierItemPlay> findNext() {
			return findItemList(offset, limit);
		}

		public abstract void doForEachCarrierItem(CarrierItemPlay item);

		@Override
		public Boolean call() {
			while (true) {
				List<CarrierItemPlay> findList = findNext();
				if (CommonUtils.isEmpty(findList)) {
					return Boolean.TRUE;
				}

				for (CarrierItemPlay item : findList) {
					offset++;
					doForEachCarrierItem(item);
				}

				findList.clear();
				JPATransactionManager.clearEntities();
				CommonUtils.sleepQuietly(sleepTime);
			}
		}
	}
	
	private static CarrierItemPlay findByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<CarrierItemPlay>(dp, query, params) {

			@Override
			public CarrierItemPlay doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
	}
	
	private static List<CarrierItemPlay> findListByJDBC(String query,Object... params) {
		
		return new JDBCBuilder.JDBCExecutor<List<CarrierItemPlay>>(dp, query, params) {

			@Override
			public List<CarrierItemPlay> doWithResultSet(ResultSet rs)
					throws SQLException {

				List<CarrierItemPlay> resultList = new ArrayList<CarrierItemPlay>();

				while (rs.next()) {
					CarrierItemPlay result = parseResult(rs);
					if (result != null) {
						resultList.add(result);
					}
				}

				return resultList;
			}

		}.call();
	}

	private static final String SelectAllProperty = " id,numIid,platform,skuQuantities,originNumIid,originPlatform,originSkuQuantities,subTaskId,createTs,updateTs,userId ";
	
	private static CarrierItemPlay parseResult(ResultSet rs) {
		try {
			
			CarrierItemPlay rptObj = new CarrierItemPlay();
			
			int colIndex = 1;
			
			rptObj.id = rs.getLong(colIndex++);
			rptObj.numIid = rs.getLong(colIndex++);
			rptObj.platform = rs.getInt(colIndex++);
			rptObj.skuQuantities = rs.getString(colIndex++);
			rptObj.originNumIid = rs.getLong(colIndex++);
			rptObj.originPlatform = rs.getInt(colIndex++);
			rptObj.originSkuQuantities = rs.getString(colIndex++);
			rptObj.subTaskId = rs.getLong(colIndex++);
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
