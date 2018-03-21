package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.pojo.PageOffset;

@Entity(name = CatClickRatePic.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "entityId", "tableHashKey", "persistent", "tableName", "idName", "idColumn", "id"
})
public class CatClickRatePic extends GenericModel implements
		PolicySQLGenerator {

	@Transient
	private static final Logger log = LoggerFactory
			.getLogger(CatClickRatePic.class);

	@Transient
	public static final String TABLE_NAME = "cat_click_rate";

	@Transient
	public static CatClickRatePic EMPTY = new CatClickRatePic();

	@Transient
	public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

	@Index(name = "cid")
	public Long cid;

	public String picUrl;
	
	public Long userId;
	
	@Id
	public Long numIid;
	
	// 7天点击率
	public Double clickRate;
	
	// 7天点击数
	public int aclick;
	
	// 7天展现量
	public int impression;
	
	public CatClickRatePic() {
		super();
	}

	public CatClickRatePic(Long cid, String picUrl, Long userId, Long numIid,
			Double clickRate, int aclick, int impression) {
		super();
		this.cid = cid;
		this.picUrl = picUrl;
		this.userId = userId;
		this.numIid = numIid;
		this.clickRate = clickRate;
		this.aclick = aclick;
		this.impression = impression;
	}

	public CatClickRatePic(Long cid) {
		super();
		this.cid = cid;
	}

	public Long getCid() {
		return cid;
	}

	public void setCid(Long cid) {
		this.cid = cid;
	}	

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getNumIid() {
		return numIid;
	}

	public void setNumIid(Long numIid) {
		this.numIid = numIid;
	}

	public Double getClickRate() {
		return clickRate;
	}

	public void setClickRate(Double clickRate) {
		this.clickRate = clickRate;
	}

	public int getAclick() {
		return aclick;
	}

	public void setAclick(int aclick) {
		this.aclick = aclick;
	}

	public int getImpression() {
		return impression;
	}

	public void setImpression(int impression) {
		this.impression = impression;
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
		return numIid;
	}

	@Override
	public void setId(Long numIid) {
		this.numIid = numIid;
	}

	public static long findExistId(Long cid, Long numIid) {

		String query = "select cid from " + TABLE_NAME + " where cid = ? and numIid = ? ";

		return dp.singleLongQuery(query, cid, numIid);
	}

	@Override
	public boolean jdbcSave() {
		try {

			long existId = findExistId(this.cid, this.numIid);

			if (existId <= 0) {
				return this.rawInsert();
			} else {
				return this.rawUpdate();
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

	public boolean rawInsert() {
		try {
			String insertSQL = "insert into `"
					+ TABLE_NAME
					+ "` (`cid`,`picUrl`,`userId`,`numIid`,`clickRate`,`aclick`,`impression`)" 
					+ " values(?,?,?,?,?,?,?)";

			long id = dp.insert(insertSQL, this.cid, this.picUrl, this.userId, this.numIid,
					this.clickRate, this.aclick, this.impression);

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

		String updateSQL = "update `"
				+ TABLE_NAME
				+ "` set `picUrl` = ?, `userId` = ?, `clickRate` = ?," +
				" `aclick` = ?, `impression` = ?  where `cid` = ? and numIid = ? ";

		long updateNum = dp.insert(updateSQL, this.picUrl, this.userId,
				this.clickRate, this.aclick, this.impression, this.cid, this.numIid);

		if (updateNum == 1) {

			return true;
		} else {

			return false;
		}
	}
	
	public static Integer countByCid(Long cid) {
		if (cid == null) {
			return 0;
		}
		String query = "select count(*) from " + TABLE_NAME
				+ " where cid = ? ";

		return new JDBCBuilder.JDBCExecutor<Integer>(dp, query,
				cid) {
			@Override
			public Integer doWithResultSet(ResultSet rs)
					throws SQLException {
				if (rs.next()) {
					return rs.getInt(1);
				}
				return 0;
			}
		}.call();
	}
	
	public static List<CatClickRatePic> findByCid(Long cid) {
		if (cid == null) {
			return null;
		}
		String query = "select " + SelectAllProperties + " from " + TABLE_NAME
				+ " where cid = ? ";

		return new JDBCBuilder.JDBCExecutor<List<CatClickRatePic>>(dp, query,
				cid) {
			@Override
			public List<CatClickRatePic> doWithResultSet(ResultSet rs)
					throws SQLException {
				List<CatClickRatePic> result = new ArrayList<CatClickRatePic>();
				while (rs.next()) {
					result.add(parseCatClickRatePic(rs));
				}
				return result;
			}
		}.call();
	}

	public static List<CatClickRatePic> findByCidWithPo(Long cid, PageOffset po,
			String orderBy, String sort) {
		if (cid == null) {
			return null;
		}
		if(StringUtils.isEmpty(orderBy)) {
			orderBy = "clickRate";
		}
		if(StringUtils.isEmpty(sort)) {
			sort = "desc";
		}
		if(po == null) {
			po = new PageOffset(1, 10);
		}
		String query = "select " + SelectAllProperties + " from " + TABLE_NAME
				+ " where cid = ? order by " + orderBy + " " + sort + " limit ?,?";

		return new JDBCBuilder.JDBCExecutor<List<CatClickRatePic>>(dp, query,
				cid, po.getOffset(), po.getPs()) {
			@Override
			public List<CatClickRatePic> doWithResultSet(ResultSet rs)
					throws SQLException {
				List<CatClickRatePic> result = new ArrayList<CatClickRatePic>();
				while (rs.next()) {
					result.add(parseCatClickRatePic(rs));
				}
				return result;
			}
		}.call();
	}
	
	private static final String SelectAllProperties = " cid, picUrl, userId, numIid, clickrate," +
			" aclick, impression ";

	private static CatClickRatePic parseCatClickRatePic(ResultSet rs) {
		try {
			return new CatClickRatePic(rs.getLong(1), rs.getString(2), rs.getLong(3), rs.getLong(4),
					rs.getDouble(5), rs.getInt(6), rs.getInt(7));

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
	}
	
}
