package models.words;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import models.item.ItemCatPlay;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@JsonIgnoreProperties(value = { "dataSrc", "dp", "persistent", "entityId", "tableHashKey", "tableName", "idName", "idColumn", })
@Entity(name = TradeFollower.TABLE_NAME)
public class TradeFollower extends Model implements PolicySQLGenerator {
	
	@Transient
	public static final String TABLE_NAME = "trade_follower";

	@Transient
	public static final Logger log = LoggerFactory.getLogger(TradeFollower.class);

	@Transient
	public static TradeFollower EMPTY = new TradeFollower();

	@Transient
	public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

	@Index(name = "userId")
	private long userId;
	
	@Index(name = "cid")
	private long cid;

	@Column(columnDefinition = "varchar(60) default '其它/Others' ")
	private String displayName;

	private long createTs;
	
	private long updateTs;

	@Enumerated(EnumType.ORDINAL)
	@Column(columnDefinition = "tinyint NOT NULL default '1'")
	private Status status; 

	public enum Status{
		UNFOLLOW(0),//用户取消关注
		FOLLOW(1),//用户关注
		BAN(2),//管理员禁止其查看，UNFOLLOW + BAN
		FOLLOWBAN(3),//FOLLOW + BAN
		INVALIDATION(4),//该行业失效,UNFOLLOW + INVALIDATION
		FOLLOWINVALIDATION(5),//FOLLOW + INVALIDATION
		INVALIDATIONBAN(6),//行业失效且被BAN，UNFOLLOW + INVALIDATION + BAN
		FOLLOWINVALIDATIONBAN(7);//关注中，失效，且被BAN

		private int code;
		private Status(int code){
			this.code = code;
		}
		public int getCode() {
			return code;
		}
		public static Status valueOf(int ordinal) {
			if (ordinal < 0 || ordinal >= values().length) {
				throw new IndexOutOfBoundsException("Invalid ordinal");
			}
			return values()[ordinal];
		}
	}

	public TradeFollower() {
	
	}
	
	/**
	 * @Title:TradeFollower
	 * @Description:仅用于新建关注
	 * @param userId
	 * @param cid
	 */
	public TradeFollower(long userId, long cid) {
		super();
		this.userId = userId;
		this.cid = cid;
		ItemCatPlay icp = ItemCatPlay.findByCid(cid);
		this.displayName = icp.getName();
		while (icp.parentCid > 0) {
			icp = ItemCatPlay.findByCid(icp.getParentCid());
			if (icp == null) {
				break;
			} else {
				this.displayName = icp.getName() + ">" + this.displayName;
			}
		}
		this.createTs = System.currentTimeMillis();
		this.updateTs = System.currentTimeMillis();
		this.status = Status.FOLLOW;
	}

	private static TradeFollower findByJDBC(String query, Object... params) {

		return new JDBCBuilder.JDBCExecutor<TradeFollower>(dp, query, params) {

			@Override
			public TradeFollower doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseTradeFollower(rs);
				}
				return null;
			}
		}.call();

	}
	
//	public static boolean followTrade(long userId, long cid) {
//		if (userId < 0 || cid < 0) {
//			return false;
//		}
//		TradeFollower tf = findByUserIdAndCid(userId, cid);
//		if (tf == null) {
//			tf = new TradeFollower(userId, cid);
//			return tf.jdbcSave();
//		} else if ((tf.status.ordinal() & Status.FOLLOW.ordinal()) == 0) {//若未关注
//			tf.status = Status.valueOf(tf.status.ordinal() | Status.FOLLOW.ordinal());
//			return tf.jdbcSave();
//		} else {
//			return true;
//		}
//	}

//	public static boolean unfollowTrade(long userId, long cid) {
//		TradeFollower tf = findByUserIdAndCid(userId, cid);
//		if (tf == null) {
//			return false;
//		}
//		if ((tf.status.ordinal() & Status.FOLLOW.ordinal()) == 0) {// 未关注
//			return true;
//		} else {
//			tf.status = Status.valueOf(tf.status.ordinal() ^ Status.FOLLOW.ordinal());
//			return tf.jdbcSave();
//		}
//
//	}

	public boolean unfollowTrade() {
		if ((this.status.ordinal() & Status.FOLLOW.ordinal()) == 0) {
			return true;
		}
		this.status = Status.valueOf(this.status.ordinal() ^ Status.FOLLOW.ordinal());
		return jdbcSave();
	}
	
//	public static boolean banUserFollow(long userId, long cid) {
//		TradeFollower tf = findByUserIdAndCid(userId, cid);
//		if (tf == null) {
//			log.error("禁用用户UserId" + userId + "查看行业Cid" + cid + "数据时出错，原因是该用户未关注该行业");
//			return false;
//		}
//		if ((tf.status.ordinal() & Status.BAN.ordinal()) == 0) {//尚未BAN掉
//			tf.status = Status.valueOf(tf.status.ordinal() | Status.BAN.ordinal());//或操作保证BAN位一定为1
//			return tf.jdbcSave();
//		} else {
//			return true;
//		}
//	}
	
//	public static boolean unbanUserFollow(long userId, long cid) {
//		TradeFollower tf = findByUserIdAndCid(userId, cid);
//		if (tf == null) {
//			log.error("解禁用户UserId" + userId + "查看行业Cid" + cid + "数据时出错，原因是该用户未关注该行业");
//			return false;
//		}
//		if ((tf.status.ordinal() & Status.BAN.ordinal()) == 0) {//尚未BAN掉
//			return true;
//		} else {
//			tf.status = Status.valueOf(tf.status.ordinal() ^ Status.BAN.ordinal());//异或操作对BAN位取反
//			return tf.jdbcSave();
//		}
//	}
	
	public static boolean isTradeAccessableForUser(long userId, long cid) {
		if (userId < 1 || cid < 1) {
			return false;
		}
		StringBuilder query = new StringBuilder("SELECT status FROM ").append(TABLE_NAME).append(" WHERE userId = ? AND cid = ? LIMIT 1");
		return dp.singleLongQuery(query.toString(), userId, cid) == (long) Status.FOLLOW.ordinal();
	}

	public static List<TradeFollower> findListByJDBC(String whereQuery, Object... params) {
		StringBuilder query = new StringBuilder("SELECT ").append(SELECT_ALL_PROPERTIES).append(" FROM ").append(TABLE_NAME) ;
		if(!StringUtils.isEmpty(whereQuery)) {
			query.append(" WHERE ").append(whereQuery);
		}
		return new JDBCBuilder.JDBCExecutor<List<TradeFollower>>(dp, query.toString(), params) {

			@Override
			public List<TradeFollower> doWithResultSet(ResultSet rs) throws SQLException {

				List<TradeFollower> resultList = new ArrayList<TradeFollower>();
				while (rs.next()) {
					TradeFollower result = parseTradeFollower(rs);
					if (result != null) {
						resultList.add(result);
					}
				}
				return resultList;
			}
		}.call();

	}

	public static int countFollowByUserId(long userId) {
		if (userId <= 0) {
			return 0;
		}
		StringBuilder query  = new StringBuilder("SELECT COUNT(id) FROM ").append(TABLE_NAME).append(" WHERE userId = ? AND status & 1 = 1");
		return (int) dp.singleLongQuery(query.toString(), userId);
	}
	
//    public static int countFollowByCid(long cid) {
//    	if (cid < 1) {
//    		return 0;
//    	}
//    	StringBuilder query  = new StringBuilder("SELECT COUNT(id) FROM ").append(TABLE_NAME).append(" WHERE cid = ? AND status & 1 = 1");
//    	return (int) dp.singleLongQuery(query.toString(), cid);
//    }

	public static TradeFollower findByUserIdAndCid(long userId, long cid) {
		if (userId < 1 || cid < 1) {
			return null;
		}
		StringBuilder query = new StringBuilder("SELECT ").append(SELECT_ALL_PROPERTIES).append(" FROM ").append(TABLE_NAME).append(" WHERE userId = ? AND cid = ? LIMIT 1");
		return findByJDBC(query.toString(), userId, cid);
	}
	/**
	 * @Description: 获取用户关注行业列表，不包含取消关注的
	 * @param userId
	 * @return
	 * @return: List<TradeFollower>
	 */
	public static List<TradeFollower> findFollowsByUserId(long userId) {
		if (userId < 1) {
			return null;
		}
		return findListByJDBC(" userId = ? AND status & 1 = 1", userId);// 若status为奇数则仍在关注
	}
	
	public static long findExistId(long userId, long cid) {
		String sql = "SELECT id FROM " + TABLE_NAME + " WHERE userId = ? AND cid = ? LIMIT 1";
		return dp.singleLongQuery(sql, userId, cid);
	}

	private static final String SELECT_ALL_PROPERTIES ="id,userId,cid,displayName,createTs,updateTs,status";
	private static TradeFollower parseTradeFollower(ResultSet rs) {
		try {
			TradeFollower bean = new TradeFollower();
			bean.setId(rs.getLong(1));
			bean.setUserId(rs.getLong(2));
			bean.setCid(rs.getLong(3));
			bean.setDisplayName(rs.getString(4));
			bean.setCreateTs(rs.getLong(5));
			bean.setUpdateTs(rs.getLong(6));
			bean.setStatus(Status.valueOf(rs.getInt(7)));
			return bean;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	@Override
	public boolean jdbcSave() {
		try {
			long existdId = findExistId(this.userId, this.cid);

			if (existdId <= 0L) {
				return this.rawInsert();
			} else {
				this.id = existdId;
				return this.rawUpdate();
			}

		} catch (Exception e) {
			log.warn(e.getMessage(), e);
			return false;
		}
	}
	
	public boolean rawInsert() {
		String insertSql = "INSERT into " + TABLE_NAME + " (userId, cid, displayName, createTs, updateTs, status) VALUES(?, ?, ?, ?, ?, ?)";

		long id = dp.insert(insertSql, this.userId, this.cid, this.displayName, this.createTs, this.updateTs, this.status.ordinal());

		if (id > 0L) {
			return true;
		} else {
			log.error("Insert TradeFoller Fails....." + "[id : ]" + this.id);
			return false;
		}
	}

	public boolean rawUpdate() {

		String updateSql = "UPDATE " + TABLE_NAME + " SET userId = ?, cid = ?, displayName = ?, createTs = ?, updateTs = ?, status = ? WHERE id = ?";

		this.updateTs = System.currentTimeMillis();
		long updateNum = dp.update(updateSql, this.userId, this.cid, this.displayName, this.createTs, this.updateTs, this.status.ordinal(), this.id);

		if (updateNum == 1) {
			return true;
		} else {
			log.error("update TradeFoller failed...for [id : ]" + this.id);
			return false;
		}
	}
	
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public long getCid() {
		return cid;
	}
	public void setCid(long cid) {
		this.cid = cid;
	}
	public long getCreateTs() {
		return createTs;
	}
	public void setCreateTs(long createTs) {
		this.createTs = createTs;
	}
	public long getUpdateTs() {
		return updateTs;
	}
	public void setUpdateTs(long updateTs) {
		this.updateTs = updateTs;
	}

	@Override
	public String getTableName() {
		return this.TABLE_NAME;
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
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String getIdName() {
		return null;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

}
