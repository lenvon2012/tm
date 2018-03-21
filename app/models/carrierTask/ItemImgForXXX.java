package models.carrierTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Entity;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@JsonIgnoreProperties(value = { "dataSrc", "persistent", "entityId", "entityId", "ts", "tableHashKey", "persistent", "tableName", "idName", "idColumn" })
@Entity(name = ItemImgForXXX.TABLE_NAME)
public class ItemImgForXXX extends Model implements PolicySQLGenerator {

	private static final Logger log = LoggerFactory.getLogger(ItemImgForXXX.class);
	
	public static final String TABLE_NAME = "item_img_for_xxx";
	
	public static final ItemImgForXXX EMPTY = new ItemImgForXXX();
	
	public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

	/**
	 * 子任务id（SubCarrierTaskForXXX表主键）
	 */
	@Index(name="subTaskId")
	public Long subTaskId;
	
	/**
	 * 图片原始链接
	 */
	public String oldUrl;
	
	/**
	 * 图片转换后链接
	 */
	public String newUrl;
	
	/**
	 * 创建时间
	 */
	@Index(name="createTs")
	public Long createTs;
	
	/**
	 * 更新时间
	 */
	@Index(name="updateTs")
	public Long updateTs;
	
	public Long getSubTaskId() {
		return subTaskId;
	}

	public void setSubTaskId(Long subTaskId) {
		this.subTaskId = subTaskId;
	}

	public String getOldUrl() {
		return oldUrl;
	}

	public void setOldUrl(String oldUrl) {
		this.oldUrl = oldUrl;
	}

	public String getNewUrl() {
		return newUrl;
	}

	public void setNewUrl(String newUrl) {
		this.newUrl = newUrl;
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

	public static Logger getLog() {
		return log;
	}

	public static ItemImgForXXX getEmpty() {
		return EMPTY;
	}

	public static DBDispatcher getDp() {
		return dp;
	}

	public static String getSelectallproperty() {
		return SelectAllProperty;
	}

	public void setId(long id) {
		this.id = id;
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
	
	@Override
	public boolean jdbcSave() {
		return false;
	}
	
	public ItemImgForXXX() {
	
	}
	
	public ItemImgForXXX(Long subTaskId, String oldUrl, String newUrl,
			Long createTs, Long updateTs) {
		super();
		this.subTaskId = subTaskId;
		this.oldUrl = oldUrl;
		this.newUrl = newUrl;
		this.createTs = createTs;
		this.updateTs = updateTs;
	}

	public static List<ItemImgForXXX> findBySubTaskId(Long subTaskId) {
		String query = " SELECT " + SelectAllProperty + " from " + TABLE_NAME + " where subTaskId = ? ";
		
		return findListByJDBC(query, subTaskId);
	}
	
	private static ItemImgForXXX findByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<ItemImgForXXX>(dp, query, params) {

			@Override
			public ItemImgForXXX doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResule(rs);
				} else {
					return null;
				}
			}

		}.call();
	}
	
	private static List<ItemImgForXXX> findListByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<List<ItemImgForXXX>>(dp, query, params) {

			@Override
			public List<ItemImgForXXX> doWithResultSet(ResultSet rs) throws SQLException {

				List<ItemImgForXXX> resultList = new ArrayList<ItemImgForXXX>();

				while (rs.next()) {
					ItemImgForXXX result = parseResule(rs);
					if (result != null) {
						resultList.add(result);
					}
				}

				return resultList;
			}

		}.call();
	}
	
	public static class batchOpType {
		public static final int INSERT = 1; // 批量插入
		public static final int UPDATE = 2; // 批量更新
	}
	
	public static String insertSql = "INSERT INTO " + TABLE_NAME + " (subTaskId, oldUrl, newUrl, createTs) VALUES (?,?,?,?)";
	
	public static String updateSql = "UPDATE " + TABLE_NAME + " SET newUrl = ?, updateTs = ? WHERE id = ? ";
	
	public static boolean batchOp(List<ItemImgForXXX> imgs, int type) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DBBuilder.getConn(DataSrc.BASIC);
			conn.setAutoCommit(false);
			String sql = insertSql;
			if(type == batchOpType.UPDATE) {
				sql = updateSql;
			}
			ps = conn.prepareStatement(sql, 1);
			Iterator<ItemImgForXXX> iterator = imgs.iterator();
			while(iterator.hasNext()){
				ItemImgForXXX img = iterator.next();
				setArgs(img, ps, type);
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
	
	private static void setArgs(ItemImgForXXX img, PreparedStatement ps, int type) throws SQLException {
		int num = 0;
		
		if(type == batchOpType.INSERT) {
			ps.setLong(++num, img.getSubTaskId());
			ps.setString(++num, img.getOldUrl());
			ps.setString(++num, img.getNewUrl());
			ps.setLong(++num, img.getCreateTs());
		} else if(type == batchOpType.UPDATE) {
			ps.setString(++num, img.getNewUrl());
			ps.setLong(++num, img.getUpdateTs());
			ps.setLong(++num, img.getId());
		}
	}

	private static final String SelectAllProperty = " `id`, `subTaskId`, `oldUrl`, `newUrl`, `createTs`, `updateTs` ";

	private static ItemImgForXXX parseResule(ResultSet rs) {
		try {
			
			ItemImgForXXX rptObj = new ItemImgForXXX();
			
			int colIndex = 1;
			
			rptObj.id = rs.getLong(colIndex++);
			rptObj.subTaskId = rs.getLong(colIndex++);
			rptObj.oldUrl = rs.getString(colIndex++);
			rptObj.newUrl = rs.getString(colIndex++);
			rptObj.createTs = rs.getLong(colIndex++);
			rptObj.updateTs = rs.getLong(colIndex++);
			
			return rptObj;
			
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
	}

}