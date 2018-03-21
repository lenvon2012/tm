package models.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = UserContact.TABLE_NAME)
public class UserContact extends Model implements PolicySQLGenerator {

	private static final Logger log = LoggerFactory.getLogger(UserContact.class);
	
	public static final String TABLE_NAME = "user_contact";
	
	public static final UserContact EMPTY = new UserContact();
	
	public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

	@Index(name = "mobile")
	public String mobile;
	
	@Index(name = "createTs")
	public Long createTs;
	
	@Index(name = "updateTs")
	public Long updateTs;
	
	@Index(name = "userNick")
	public String userNcik;
	
	@Index(name = "userId")
	public Long userId;

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
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

	public String getUserNcik() {
		return userNcik;
	}

	public void setUserNcik(String userNcik) {
		this.userNcik = userNcik;
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

	public static UserContact getEmpty() {
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
	
	public UserContact() {
	
	}
	
	public UserContact(String mobile, String userNcik, Long userId) {
		super();
		this.mobile = mobile;
		this.userNcik = userNcik;
		this.userId = userId;
	}

	public static Long findExistId(Long id) {
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
				return false;
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
			return false;
		}
		
	}
	
	public boolean rawInsert() {
		this.createTs = System.currentTimeMillis();
		this.updateTs = this.createTs;
		
		String insertSQL = "insert into `" + TABLE_NAME + "`(`mobile`," +
				"`createTs`,`updateTs`,`userNcik`,`userId`)" +
				" values(?,?,?,?,?)";

		long id = dp.insert(true, insertSQL, this.mobile,
				this.createTs, this.updateTs, this.userNcik, this.userId);

		if (id > 0L) {
			this.setId(id);
			return true;
		} else {
			log.error("Insert Fails....." + "[userNcik]" + this.userNcik);
			return false;
		}
	}
	
	public boolean rawDelete() {
		String sql = " delete from " + TABLE_NAME + " where id = ? ";
		
		dp.update(sql, this.id);
		
		return true;
	}
	
	public static UserContact findByUserId(Long userId) {
		String query = "select " + SelectAllProperty + "from " + TABLE_NAME + " where userId = ? ";
		
		return findByJDBC(query, userId);
	}
	
	private static UserContact findByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<UserContact>(dp, query, params) {

			@Override
			public UserContact doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
	}
	
	private static List<UserContact> findListByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<List<UserContact>>(dp, query, params) {

			@Override
			public List<UserContact> doWithResultSet(ResultSet rs) throws SQLException {

				List<UserContact> resultList = new ArrayList<UserContact>();

				while (rs.next()) {
					UserContact result = parseResult(rs);
					if (result != null) {
						resultList.add(result);
					}
				}

				return resultList;
			}

		}.call();
	}
	
	private static final String SelectAllProperty = " `id`, `mobile`, `createTs`, `updateTs`, `userNcik`, `userId` ";

	private static UserContact parseResult(ResultSet rs) {
		try {
			
			UserContact rptObj = new UserContact();
			
			int colIndex = 1;
			
			rptObj.id = rs.getLong(colIndex++);
			rptObj.mobile = rs.getString(colIndex++);
			rptObj.createTs = rs.getLong(colIndex++);
			rptObj.updateTs = rs.getLong(colIndex++);
			rptObj.userNcik = rs.getString(colIndex++);
			rptObj.userId = rs.getLong(colIndex++);
			return rptObj;
			
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
	}

}
