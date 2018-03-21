package models.promotion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Entity;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.CodeGenerator.DBDispatcher;
import transaction.CodeGenerator.PolicySQLGenerator;
import transaction.DBBuilder;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;

@Entity(name = SalesTitlePlay.TABLE_NAME)
public class SalesTitlePlay extends Model implements PolicySQLGenerator<Long> {

	private static final Logger log = LoggerFactory.getLogger(SalesTitlePlay.class);

	public static final String TABLE_NAME = "sales_title_play";

	public static final SalesTitlePlay EMPTY = new SalesTitlePlay();

	public static final DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, EMPTY);

	/**
	 * 类型
	 */
	@Index(name = "clazz")
	private String clazz;
	
	/**
	 * 促销标签
	 */
	@Index(name = "words")
	private String words;
	
	/**
	 * 创建时间
	 */
	@Index(name = "createTs")
	private Long createTs;
	
	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public String getWords() {
		return words;
	}

	public void setWords(String words) {
		this.words = words;
	}

	public Long getCreateTs() {
		return createTs;
	}

	public void setCreateTs(Long createTs) {
		this.createTs = createTs;
	}

	public static Logger getLog() {
		return log;
	}

	public static SalesTitlePlay getEmpty() {
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
	
	@Override
	public boolean jdbcSave() {
		return false;
	}
	
	public SalesTitlePlay() {
	
	}
	
	public SalesTitlePlay(String clazz, String words) {
		super();
		this.clazz = clazz;
		this.words = words;
	}
	
	public static boolean deleteAllWords() {

		String sql = " TRUNCATE TABLE " + TABLE_NAME;

		dp.update(sql);

		return true;
	}
	
	public static List<SalesTitlePlay> getAll() {
		String query = " select " + SelectAllProperty + " from " + TABLE_NAME;

		return findListByJDBC(query);
	}
	
	private static SalesTitlePlay findByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<SalesTitlePlay>(dp, query, params) {

			@Override
			public SalesTitlePlay doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
	}
	
	private static List<SalesTitlePlay> findListByJDBC(String query,Object... params) {
		
		return new JDBCBuilder.JDBCExecutor<List<SalesTitlePlay>>(dp, query, params) {

			@Override
			public List<SalesTitlePlay> doWithResultSet(ResultSet rs)
					throws SQLException {

				List<SalesTitlePlay> resultList = new ArrayList<SalesTitlePlay>();

				while (rs.next()) {
					SalesTitlePlay result = parseResult(rs);
					if (result != null) {
						resultList.add(result);
					}
				}

				return resultList;
			}

		}.call();
	}
	
	public static boolean batchInsert(List<SalesTitlePlay> salesTitleList){
		String sql = "insert into " + TABLE_NAME + " (clazz, words, createTs) values(?,?,?)";
		long nowTime = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		int count = 1;
		try {
			conn = DBBuilder.getConn(DataSrc.QUOTA);
			conn.setAutoCommit(false);
			ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			Iterator<SalesTitlePlay> iterator = salesTitleList.iterator();
			while(iterator.hasNext()){
				int num = 0;
				SalesTitlePlay s = iterator.next();
				ps.setString(++num, s.getClazz());
				ps.setString(++num, s.getWords());
				ps.setLong(++num, nowTime);
				ps.addBatch();
				if (count % 1000 == 0) {
					ps.executeBatch();
					conn.commit();
				}
				count++;
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

	private static final String SelectAllProperty = " id,clazz,words,createTs";
	
	private static SalesTitlePlay parseResult(ResultSet rs) {
		try {
			
			SalesTitlePlay rptObj = new SalesTitlePlay();
			
			int colIndex = 1;
			
			rptObj.id = rs.getLong(colIndex++);
			rptObj.clazz = rs.getString(colIndex++);
			rptObj.words = rs.getString(colIndex++);
			rptObj.createTs = rs.getLong(colIndex++);
			
			return rptObj;
			
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
	}

}
