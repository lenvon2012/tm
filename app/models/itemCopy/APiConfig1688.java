package models.itemCopy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

/**
 * 阿里应用配置
 * 
 * @author oyster
 * 
 */
@Entity(name = APiConfig1688.TABLE_NAME)
public class APiConfig1688 extends Model implements PolicySQLGenerator {

	public static final String TABLE_NAME = "api_config_1688";

	public static APiConfig1688 EMPTY = new APiConfig1688();

	public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

	public static final Logger log = LoggerFactory
			.getLogger(APiConfig1688.class);

	private String appkey;

	private String appSecret;

	private String refreshToken;

	private String accessToken;

	private int useCount; // 在第一次调用24小时内的调用次数

	private long useTs; // 第一次调用时间 重置调用次数的时候重置改时间

	private long updateTs; // 第一次调用时间 重置调用次数的时候重置改时间

	private int status; // 1--normal -1-disable

	public APiConfig1688() {
		super();
	}

	private static APiConfig1688 parseResult(ResultSet rs) {
		try {

			APiConfig1688 config = new APiConfig1688();
			config.id = rs.getLong("id");
			config.appkey = rs.getString("appkey");
			config.appSecret = rs.getString("appSecret");
			config.accessToken = rs.getString("accessToken");
			config.refreshToken = rs.getString("refreshToken");
			config.useCount = rs.getInt("useCount");
			config.useTs = rs.getLong("useTs");
			config.updateTs = rs.getLong("updateTs");
			config.status = rs.getInt("status");
			return config;

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
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
	public boolean jdbcSave() {
		if (findByAppkey(appkey) == null) {
			return this.rawInsert();
		}

		return this.updateAccessToken(accessToken, appkey);

	}

	public boolean rawInsert() {
		try {
			long now = System.currentTimeMillis();
			String insertSQL = "insert into `"
					+ TABLE_NAME
					+ "`(`appkey`,`appSecret`,`accessToken`,`refreshToken`,`useCount`,`useTs`,`updateTs`,`status`) values(?,?,?,?,?,?,?,?)";
			long id = dp.insert(insertSQL, this.appkey, this.appSecret,
					this.accessToken, this.refreshToken, this.useCount, now,
					now,status);
			if (id > 0L) {
				return true;
			} else {
				return false;
			}

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return false;
		}
	}

	public boolean rawUpdate() {
		try {
			long now = System.currentTimeMillis();
			String updateSql = "update `"
					+ TABLE_NAME
					+ "` set `accessToken` = ?,`refreshToken` = ?,`useCount` = ? ,`useTs` = ? , `updateTs` = ? where `appkey` = ? ";
			long id = dp.update(updateSql, this.accessToken, this.refreshToken,
					this.useCount, this.useTs, now, this.appkey);
			if (id > 0L) {
				return true;
			} else {
				return false;
			}

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return false;
		}
	}

	public static boolean updateAccessToken(String accessToken, String appkey) {
		try {
			String updateSql = "update `"
					+ TABLE_NAME
					+ "` set `accessToken` = ?, `updateTs` = ? where `appkey` = ? ";
			long id = dp.update(updateSql, accessToken,
					System.currentTimeMillis(), appkey);
			if (id > 0L) {
				return true;
			} else {
				return false;
			}

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return false;
		}
	}

	@Override
	public String getIdName() {
		return "id";
	}

	/**
	 * 删除数据库中的记录
	 * 
	 * @param cid
	 */
	public static void deleteByCid(Long cid) {
		APiConfig1688 icp = findById(cid);
		if (icp != null) {
			icp.delete();
		}

	}

	// 获取有效的应用
	public static APiConfig1688 getValidApp() {
		long now = System.currentTimeMillis();
		// 使用次数 限制
		String sql = "select * from " + TABLE_NAME
				+ " where useCount <= ? and status = ? limit 1";
		return new JDBCBuilder.JDBCExecutor<APiConfig1688>(dp, sql, 4500,
				ApiConfig1688Status.NORMAL) {

			@Override
			public APiConfig1688 doWithResultSet(ResultSet rs)
					throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();

	}


	public APiConfig1688(String appkey, String appSecret, String refreshToken,
			String accessToken, int useCount,int status) {
		super();
		this.appkey = appkey;
		this.appSecret = appSecret;
		this.refreshToken = refreshToken;
		this.accessToken = accessToken;
		this.useCount = useCount;
		this.status=status;
	}

	public static APiConfig1688 findByAppkey(String appkey) {
		String sql = "select * from " + TABLE_NAME + " where appkey= ? limit 1";
		return new JDBCBuilder.JDBCExecutor<APiConfig1688>(dp, sql, appkey) {

			@Override
			public APiConfig1688 doWithResultSet(ResultSet rs)
					throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();

	}

	// 获取所有配置APP
	public static List<APiConfig1688> getAppList() {
		// 使用次数 限制
		String sql = "select * from " + TABLE_NAME;
		return new JDBCBuilder.JDBCExecutor<List<APiConfig1688>>(dp, sql) {

			@Override
			public List<APiConfig1688> doWithResultSet(ResultSet rs)
					throws SQLException {
				List<APiConfig1688> config1688s = new ArrayList<APiConfig1688>();
				while (rs.next()) {
					APiConfig1688 config = parseResult(rs);
					config1688s.add(config);
				}
				return config1688s;
			}

		}.call();

	}

	public String getAppkey() {
		return appkey;
	}

	public String getAppSecret() {
		return appSecret;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public int getUseCount() {
		return useCount;
	}

	public long getUseTs() {
		return useTs;
	}

	public void setAppkey(String appkey) {
		this.appkey = appkey;
	}

	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public void setUseCount(int useCount) {
		this.useCount = useCount;
	}

	public void setUseTs(long useTs) {
		this.useTs = useTs;
	}

	// 抉择是否重置使用次数
	public static void doRefresh() {
		// TODO Auto-generated method stub

	}

	public long getUpdateTs() {
		return updateTs;
	}

	public void setUpdateTs(long updateTs) {
		this.updateTs = updateTs;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	

	@Override
	public String toString() {
		return "APiConfig1688 [appkey=" + appkey + ", appSecret=" + appSecret
				+ ", refreshToken=" + refreshToken + ", accessToken="
				+ accessToken + ", useCount=" + useCount + ", useTs=" + useTs
				+ ", updateTs=" + updateTs + ", status=" + status + "]";
	}



	public static class ApiConfig1688Status {
		public static final int NORMAL = 1;
		public static final int DISABLED = -1;
	}

}
