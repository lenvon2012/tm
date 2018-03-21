package models.shop;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.api.domain.Shop;
import com.taobao.api.domain.ShopScore;

import play.db.jpa.Model;
import transaction.CodeGenerator.DBDispatcher;
import transaction.CodeGenerator.PolicySQLGenerator;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import utils.DateUtil;

@Entity(name = ShopScorePlay.TABLE_NAME)
public class ShopScorePlay extends Model implements PolicySQLGenerator<Long> {

	private static final Logger log = LoggerFactory.getLogger(ShopScorePlay.class);

	public static final String TABLE_NAME = "shop_score_play";

	public static final ShopScorePlay EMPTY = new ShopScorePlay();

	public static final DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, EMPTY);

	/**
	 * 时间
	 */
	@Index(name = "time")
	private Long time;
	
	/**
	 * 时间 Str
	 */
	@Index(name = "timeStr")
	private String timeStr;
	
	/**
	 * 商品描述评分
	 */
	private String itemScore;
	
	/**
	 * 服务态度评分
	 */
	private String serviceScore;
	
	/**
	 * 发货速度评分
	 */
	private String deliveryScore;
	
	@Index(name = "userId")
	private Long userId;

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public String getTimeStr() {
		return timeStr;
	}

	public void setTimeStr(String timeStr) {
		this.timeStr = timeStr;
	}

	public String getItemScore() {
		return itemScore;
	}

	public void setItemScore(String itemScore) {
		this.itemScore = itemScore;
	}

	public String getServiceScore() {
		return serviceScore;
	}

	public void setServiceScore(String serviceScore) {
		this.serviceScore = serviceScore;
	}

	public String getDeliveryScore() {
		return deliveryScore;
	}

	public void setDeliveryScore(String deliveryScore) {
		this.deliveryScore = deliveryScore;
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

	public static ShopScorePlay getEmpty() {
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
	
	public ShopScorePlay() {
	
	}
	
	public ShopScorePlay(Shop shop, Long userId) {
		ShopScore shopScore = shop.getShopScore();
		if(shopScore == null) {
			return;
		}
		this.itemScore = shopScore.getItemScore();
		this.serviceScore = shopScore.getServiceScore();
		this.deliveryScore = shopScore.getDeliveryScore();
		
		this.time = DateUtil.formDailyTimestamp(System.currentTimeMillis());
		this.timeStr = DateUtil.ymdsdf.format(new Date(time));
		this.userId = userId;
	}
	
	public ShopScorePlay(Long time, String timeStr, String itemScore,
			String serviceScore, String deliveryScore, Long userId) {
		super();
		this.time = time;
		this.timeStr = timeStr;
		this.itemScore = itemScore;
		this.serviceScore = serviceScore;
		this.deliveryScore = deliveryScore;
		this.userId = userId;
	}

	public static long findExistId(Long time, Long userId) {
	
		String query = "select id from " + TABLE_NAME + " where time = ? and userId = ? ";
		
		return dp.singleLongQuery(query, time, userId);
	
	}
	
	@Override
	public boolean jdbcSave() {
		
		try {
			long existdId = findExistId(this.time, this.userId);
			
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
		
		String insertSQL = "insert into `" + TABLE_NAME + "`(`time`,`timeStr`," +
				"`itemScore`,`serviceScore`,`deliveryScore`," +
				"`userId`) " +
				" values(?,?,?,?,?,?)";

		long id = dp.insert(insertSQL, this.time, this.timeStr,
				this.itemScore, this.serviceScore, this.deliveryScore, this.userId);

		if (id > 0L) {
			this.setId(id);
			return true;
		} else {
			log.error("Insert Fails....." + "[id : ]" + this.id);
			return false;
		}

	}
	
	public boolean rawDelete() {

		String sql = " delete from " + TABLE_NAME + " where id = ? ";

		dp.update(sql, this.id);

		return true;
	}
	
	public static List<ShopScorePlay> findBySearchRules(Long startTime,
			Long endTime, Long userId) {
		
		List<Object> paramList = new ArrayList<Object>();
		String whereSql = genWhereSqlByRules(startTime, endTime, paramList, userId);

		String query = "select " + SelectAllProperty + " from "
				+ TABLE_NAME + " where " + whereSql;

		query += " order by time asc ";

		Object[] paramArray = paramList.toArray();
		return findListByJDBC(query, paramArray);
		
	}
	
	private static String genWhereSqlByRules(Long startTime, Long endTime, List<Object> paramList, Long userId) {

		String whereSql = " 1=1 "; // 规范sql语句
		
		if(startTime != null && startTime > 0) {
			whereSql += " and time >= ? ";
			paramList.add(startTime);
		}
		
		if(endTime != null && endTime > 0) {
			whereSql += " and time <= ? ";
			paramList.add(endTime);
		}
		
		if(userId != null && userId > 0) {
			whereSql += " and userId = ? ";
			paramList.add(userId);
		}
		
		return whereSql;

	}
	
	public static int countBySearchRules(Long startTime, Long endTime, Long userId) {

		List<Object> paramList = new ArrayList<Object>();

		String whereSql = genWhereSqlByRules(startTime, endTime, paramList, userId);

		String query = " select count(*) from " + TABLE_NAME + " where "
				+ whereSql;

		Object[] paramArray = paramList.toArray();

		return (int) dp.singleLongQuery(query, paramArray);
	}
	
	private static ShopScorePlay findByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<ShopScorePlay>(dp, query, params) {

			@Override
			public ShopScorePlay doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
	}
	
	private static List<ShopScorePlay> findListByJDBC(String query,Object... params) {
		
		return new JDBCBuilder.JDBCExecutor<List<ShopScorePlay>>(dp, query, params) {

			@Override
			public List<ShopScorePlay> doWithResultSet(ResultSet rs)
					throws SQLException {

				List<ShopScorePlay> resultList = new ArrayList<ShopScorePlay>();

				while (rs.next()) {
					ShopScorePlay result = parseResult(rs);
					if (result != null) {
						resultList.add(result);
					}
				}

				return resultList;
			}

		}.call();
	}

	private static final String SelectAllProperty = " id,time,timeStr,"
			+ "itemScore,serviceScore,deliveryScore,userId ";
	
	private static ShopScorePlay parseResult(ResultSet rs) {
		try {
			
			ShopScorePlay rptObj = new ShopScorePlay();
			
			int colIndex = 1;
			
			rptObj.id = rs.getLong(colIndex++);
			rptObj.time = rs.getLong(colIndex++);
			rptObj.timeStr = rs.getString(colIndex++);
			rptObj.itemScore = rs.getString(colIndex++);
			rptObj.serviceScore = rs.getString(colIndex++);
			rptObj.deliveryScore = rs.getString(colIndex++);
			rptObj.userId = rs.getLong(colIndex++);
			
			return rptObj;
			
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
	}

}
