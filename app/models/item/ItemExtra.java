package models.item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;

@Entity(name = ItemExtra.TABLE_NAME)
public class ItemExtra extends GenericModel implements PolicySQLGenerator {
	
	private static final Logger log = LoggerFactory.getLogger(ItemExtra.class);

	public static final String TABLE_NAME = "item_extra_";

	public static final ItemExtra EMPTY = new ItemExtra();

	public static final DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, EMPTY);

	@Id
	@JsonProperty(value = "numIid")
	@PolicySQLGenerator.CodeNoUpdate
	public Long numIid;
	
	@Index(name = "insertTs")
	public Long insertTs;

	/*
	 * Item的发布时间
	 */
	@Index(name = "created")
	public Long created;
	
	@Index(name = "user_id")
	@PolicySQLGenerator.CodeNoUpdate
	public Long userId;

	public Long getNumIid() {
		return numIid;
	}

	public void setNumIid(Long numIid) {
		this.numIid = numIid;
	}

	public Long getInsertTs() {
		return insertTs;
	}

	public void setInsertTs(Long insertTs) {
		this.insertTs = insertTs;
	}

	public Long getCreated() {
		return created;
	}

	public void setCreated(Long created) {
		this.created = created;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public void setId(long id) {
		this.numIid = id;
	}

	public static Logger getLog() {
		return log;
	}

	public static ItemExtra getEmpty() {
		return EMPTY;
	}

	public static DBDispatcher getDp() {
		return dp;
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
		return "numIid";
	}

	@Override
	public Long getId() {
		return this.numIid;
	}

	@Override
	public void setId(Long id) {
		this.numIid = id;
	}

	@Override
	public String getIdName() {
		return "numIid";
	}
	
	public ItemExtra() {
	
	}
	
	public ItemExtra(Long numIid, Long insertTs, Long created, Long userId) {
		super();
		this.numIid = numIid;
		this.insertTs = insertTs;
		this.created = created;
		this.userId = userId;
	}
	
	public static long findExistId(Long numIid, Long userId) {
	
		String query = "select numIid from " + TABLE_NAME + "%s where numIid = ? and userId = ? ";
		
		query = genShardQuery(query, userId);
		
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
		
		this.insertTs = System.currentTimeMillis();
		
		String insertSQL = "insert into `" + TABLE_NAME + "%s` (`numIid`," +
				"`insertTs`,`created`,`userId`) " +
				" values(?,?,?,?)";
		
		insertSQL = genShardQuery(insertSQL, userId);
		
		long id = dp.insert(true, insertSQL,
				this.numIid, this.insertTs, this.created, this.userId);

		if (id > 0L) {
			return true;
		} else {
			log.error("Insert Fails...for" + "[userId : ]" + this.userId + "-----[numIid : ]" + this.numIid);
			return false;
		}

	}
	
	public boolean rawUpdate() {
		
		String updateSQL = "update `" + TABLE_NAME + "%s` set " +
				"`insertTs` = ?, `created` = ?" +
				" where `numIid` = ? ";
		
		updateSQL = genShardQuery(updateSQL, userId);
		
		long updateNum = dp.update(updateSQL, 
				this.insertTs, this.created,
				this.numIid);

		if (updateNum >= 1) {
			return true;
		} else {
			log.error("update failed...for" + "[userId : ]" + this.userId + "-----[numIid : ]" + this.numIid);
			return false;
		}
	}
	
	public boolean rawDelete() {
	
		String sql = " delete from " + TABLE_NAME + "%s where numIid = ? and userId = ? ";
		
		sql = genShardQuery(sql, userId);
		
		dp.update(sql, this.numIid, this.userId);
		
		return true;
	}
	
	public static String genShardQuery(String query, Long userId) {
		return genShardQuery(query, String.valueOf(DBBuilder.genUserIdHashKey(userId)));
	}

	private static String genShardQuery(String query, String key) {
		query = query.replaceAll("%s", "~~");
		query = query.replaceAll("%", "##");
		query = query.replaceAll("~~", "%s");

		String formQuery = String.format(query, key, key);
		return formQuery.replaceAll("##", "%");
	}
	
	public static List<ItemExtra> findByUserId(Long userId) {
		
		String query = " select " + SelectAllProperty + " from " + TABLE_NAME
			+ "%s where userId = ? ";
		
		query = genShardQuery(query, userId);
		
		return findListByJDBC(query, userId);
		
	}
	
	public static void deleteAll(Long userId, Collection<Long> existIds) {
		if (CommonUtils.isEmpty(existIds)) {
			return;
		}

		StringBuilder sb = new StringBuilder("delete from item_extra_%s where numIid in (");
		sb.append(StringUtils.join(existIds, ','));
		sb.append(")");

		long deleteNum = dp.update(genShardQuery(sb.toString(), userId));
		log.info("[delete num:]" + deleteNum);
	}
	
	private static ItemExtra findByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<ItemExtra>(dp, query, params) {

			@Override
			public ItemExtra doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
	}
	
	private static List<ItemExtra> findListByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<List<ItemExtra>>(dp, query, params) {

			@Override
			public List<ItemExtra> doWithResultSet(ResultSet rs) throws SQLException {

				List<ItemExtra> resultList = new ArrayList<ItemExtra>();

				while (rs.next()) {
					ItemExtra result = parseResult(rs);
					if (result != null) {
						resultList.add(result);
					}
				}

				return resultList;
			}

		}.call();
	}
	
	public static String sqlSave = "INSERT INTO "+TABLE_NAME +"%s (numIid"
			+ ",insertTs,created,userId)"
			+ " values(?,?,?,?)";
	
	public static boolean batchSave(List<ItemExtra> itemExtraList, Long userId) {
		String sql = genShardQuery(sqlSave, userId);
		return batchSql(sql, true, DataSrc.QUOTA, itemExtraList);
	}
	
	public static boolean batchSql(String sql, boolean isKeyGenerated, DataSrc src, List<ItemExtra> itemExtraList){
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = DBBuilder.getConn(src);
			conn.setAutoCommit(false);
			ps = conn.prepareStatement(sql, isKeyGenerated ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
			for (int i = 0; i< itemExtraList.size(); i++) {
				ItemExtra itemExtraPlay = itemExtraList.get(i);
				setTradeArgs(itemExtraPlay, ps);
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
	
	private static void setTradeArgs(ItemExtra itemExtraPlay, PreparedStatement ps) throws SQLException{
		int num = 0;
		ps.setLong(++num, itemExtraPlay.getNumIid());
		ps.setLong(++num, itemExtraPlay.getInsertTs());
		ps.setLong(++num, itemExtraPlay.getCreated());
		ps.setLong(++num, itemExtraPlay.getUserId());
	}
	
	private static final String SelectAllProperty = " `numIid`," +
			"`insertTs`, `created`, `userId` ";

	private static ItemExtra parseResult(ResultSet rs) {
		try {
			
			ItemExtra rptObj = new ItemExtra();
			
			int colIndex = 1;
			
			rptObj.numIid = rs.getLong(colIndex++);
			rptObj.insertTs = rs.getLong(colIndex++);
			rptObj.created = rs.getLong(colIndex++);
			rptObj.userId = rs.getLong(colIndex++);
			
			return rptObj;
			
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
	}
	
}
