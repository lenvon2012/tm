package actions.clouddata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actions.UvPvDiagAction;

import com.taobao.api.domain.QueryRow;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = SrcPlay.TABLE_NAME)
public class SrcPlay extends GenericModel implements PolicySQLGenerator {

	private static final Logger log = LoggerFactory.getLogger(SrcPlay.class);
	public static final String TABLE_NAME = "src_play";
	public static final SrcPlay EMPTY = new SrcPlay();
	public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

	@Id
	@Index(name="Id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public long Id;
	
	/**
	 * 终端类型
	 */
	public String device_type;
	
	/**
	 * 来源id
	 */
	public String src_id;
	
	/**
	 * 来源名称
	 */
	public String src_name;
	
	/**
	 * 父来源id
	 */
	public String src_parent_id;
	
	/**
	 * 父来源id
	 */
	public String src_parent_name;
	
	/**
	 * 来源层级
	 */
	public String src_level;
	
	/**
	 * 是否叶子
	 */
	public String is_leaf;
	
	/**
	 * 是否PC端: true:PC false:无线
	 */
	public Boolean is_pc;
	
	public String getDevice_type() {
		return device_type;
	}

	public void setDevice_type(String device_type) {
		this.device_type = device_type;
	}

	public String getSrc_id() {
		return src_id;
	}

	public void setSrc_id(String src_id) {
		this.src_id = src_id;
	}

	public String getSrc_name() {
		return src_name;
	}

	public void setSrc_name(String src_name) {
		this.src_name = src_name;
	}

	public String getSrc_parent_id() {
		return src_parent_id;
	}

	public void setSrc_parent_id(String src_parent_id) {
		this.src_parent_id = src_parent_id;
	}

	public String getSrc_parent_name() {
		return src_parent_name;
	}

	public void setSrc_parent_name(String src_parent_name) {
		this.src_parent_name = src_parent_name;
	}

	public String getSrc_level() {
		return src_level;
	}

	public void setSrc_level(String src_level) {
		this.src_level = src_level;
	}

	public String getIs_leaf() {
		return is_leaf;
	}

	public void setIs_leaf(String is_leaf) {
		this.is_leaf = is_leaf;
	}

	public Boolean getIs_pc() {
		return is_pc;
	}

	public void setIs_pc(Boolean is_pc) {
		this.is_pc = is_pc;
	}

	public static Logger getLog() {
		return log;
	}

	public static SrcPlay getEmpty() {
		return EMPTY;
	}

	public static DBDispatcher getDp() {
		return dp;
	}

	public static String getSelectallproperty() {
		return SelectAllProperty;
	}

	public void setId(long id) {
		Id = id;
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
		return "Id";
	}

	@Override
	public Long getId() {
		return Id;
	}

	@Override
	public void setId(Long id) {
		this.Id = id;
	}

	@Override
	public String getIdName() {
		return "Id";
	}
	
	public SrcPlay() {
	
	}
	
	public SrcPlay(String device_type, String src_id, String src_name,
			String src_parent_id, String src_parent_name, String src_level,
			String is_leaf, Boolean is_pc) {
		super();
		this.device_type = device_type;
		this.src_id = src_id;
		this.src_name = src_name;
		this.src_parent_id = src_parent_id;
		this.src_parent_name = src_parent_name;
		this.src_level = src_level;
		this.is_leaf = is_leaf;
		this.is_pc = is_pc;
	}
	
	public SrcPlay(QueryRow row, Boolean is_pc){
		List<String> values = row.getValues();
		if(is_pc) {
			this.device_type = values.get(0);
			this.src_id = values.get(1);
			this.src_name = values.get(2);
			this.src_parent_id = values.get(3);
			this.src_parent_name = values.get(4);
			this.src_level = values.get(5);
			this.is_leaf = values.get(6);
			this.is_pc = is_pc;
		} else {
			this.device_type = values.get(1);
			this.src_id = values.get(2);
			this.src_name = values.get(3);
			this.src_parent_id = values.get(4);
			this.src_parent_name = values.get(5);
			this.src_level = values.get(6);
			this.is_leaf = values.get(7);
			this.is_pc = is_pc;
		}
    }
	
	public static long findExistId(Long Id) {
		String query = "select Id from " + TABLE_NAME + " where Id = ? ";
		
		return dp.singleLongQuery(query, Id);
	}
	
	@Override
	public boolean jdbcSave() {
		
		try {
			long existdId = findExistId(this.Id);
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
		String insertSQL = "insert into `" + TABLE_NAME + "`(`device_type`," +
				"`src_id`,`src_name`," +
				"`src_parent_id`,`src_parent_name`,`src_level`,`is_leaf`,`is_pc`)" +
				" values(?,?,?,?,?,?,?,?)";

		long id = dp.insert(true, insertSQL, this.device_type,
				this.src_id, this.src_name,
				this.src_parent_id, this.src_parent_name, this.src_level, this.is_leaf, this.is_pc);

		if (id > 0L) {
			this.setId(id);
			return true;
		} else {
			log.error("Insert Fails....." + "[Id : ]" + this.Id);
			return false;
		}
	}
	
	public boolean rawUpdate() {
		String updateSQL = "update `" + TABLE_NAME + "` set " +
				"`device_type` = ?,`src_id` = ?,`src_name` = ?, " +
				"`src_parent_id` = ?,`src_parent_name` = ?,`src_level` = ?,`is_leaf` = ?,`is_pc` = ? " +
				" where `Id` = ? ";
		
		long updateNum = dp.update(updateSQL, 
				this.device_type, this.src_id, this.src_name,
				this.src_parent_id, this.src_parent_name, this.src_level, this.is_leaf, this.is_pc,
				this.Id);

		if (updateNum >= 1) {
			return true;
		} else {
			log.error("update failed...for :" + this.getId() + "[Id : ]" + this.Id);
			return false;
		}
	}
	
	public boolean rawDelete() {
		String sql = " delete from " + TABLE_NAME + " where Id = ? ";
		
		dp.update(sql, this.Id);
		
		return true;
	}
	
	public static SrcPlay findBySrcId(String srcId) {
		String query = "select " + SelectAllProperty + " from " + TABLE_NAME + " where src_id = ?";
		return findByJDBC(query, srcId);
	}
	
	private static SrcPlay findByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<SrcPlay>(dp, query, params) {

			@Override
			public SrcPlay doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseSrcPlay(rs);
				} else {
					return null;
				}
			}

		}.call();
	}
	
	private static List<SrcPlay> findListByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<List<SrcPlay>>(dp, query, params) {

			@Override
			public List<SrcPlay> doWithResultSet(ResultSet rs) throws SQLException {

				List<SrcPlay> resultList = new ArrayList<SrcPlay>();

				while (rs.next()) {
					SrcPlay result = parseSrcPlay(rs);
					if (result != null) {
						resultList.add(result);
					}
				}

				return resultList;
			}

		}.call();
	}

	private static final String SelectAllProperty = " `Id`, `device_type`, `src_id`, `src_name`, " +
			"`src_parent_id`, `src_parent_name`, `src_level`, `is_leaf`, `is_pc` ";

	private static SrcPlay parseSrcPlay(ResultSet rs) {
		try {
			
			SrcPlay rptObj = new SrcPlay();
			
			int colIndex = 1;
			
			rptObj.Id = rs.getLong(colIndex++);
			rptObj.device_type = rs.getString(colIndex++);
			rptObj.src_id = rs.getString(colIndex++);
			rptObj.src_name = rs.getString(colIndex++);
			rptObj.src_parent_id = rs.getString(colIndex++);
			rptObj.src_parent_name = rs.getString(colIndex++);
			rptObj.src_level = rs.getString(colIndex++);
			rptObj.is_leaf = rs.getString(colIndex++);
			rptObj.is_pc = rs.getBoolean(colIndex++);
			
			return rptObj;
			
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
	}

}