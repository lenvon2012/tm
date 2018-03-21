package models.CPEctocyst;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import models.item.ItemCatPlay;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.pojo.PageOffset;

import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;
import play.db.jpa.Model;
import transaction.JDBCBuilder;
import transaction.DBBuilder.DataSrc;

@Entity(name = ChiefStaffDetail.TABLE_NAME)
@JsonIgnoreProperties(value = {})
public class ChiefStaffDetail extends Model implements PolicySQLGenerator {

	@Transient
	public static final String TABLE_NAME = "chief_staff_detail";

	@Transient
	public static final Logger log = LoggerFactory
			.getLogger(ChiefStaffDetail.class);

	@Transient
	public static ChiefStaffDetail EMPTY = new ChiefStaffDetail();

	@Transient
	public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

	// 公司名
	public String companyName;

	// 对应cp_staff的chiefId
	public Long chiefId;	
	
	// 几个起接
	public int startNum;

	// 公司logo
	public String logo;

	// 单条价格
	public String price;

	// 备注
	public String remark;

	// 旺旺
	public String wangwang;

	// QQ
	public String QQNum;

	// 统计该公司接的卖家总数
	@Transient
	public int acceptNum;
	
	public Long created;
	
	/**
	 * 这个代表选择该外包商处理差评的卖家总数
	 * 数值为SellerToStaff.countByChiefId(Long chiefId, "", "")
	 */
	@Transient
	public long dealingUserCount;

	@Transient
	public boolean showPrice = true;
	
	public ChiefStaffDetail() {
		super();
	}

	public ChiefStaffDetail(String companyName, Long chiefId, int startNum,
			String logo, String price, String remark, String wangwang,
			String qQNum, Long created, long dealingUserCount) {
		super();
		this.companyName = companyName;
		this.chiefId = chiefId;
		this.startNum = startNum;
		this.logo = logo;
		this.price = price;
		this.remark = remark;
		this.wangwang = wangwang;
		QQNum = qQNum;
		this.created = created;
		this.dealingUserCount = dealingUserCount;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public int getAcceptNum() {
		return acceptNum;
	}

	public void setAcceptNum(int acceptNum) {
		this.acceptNum = acceptNum;
	}

	public boolean isShowPrice() {
		return showPrice;
	}

	public void setShowPrice(boolean showPrice) {
		this.showPrice = showPrice;
	}

	public int getStartNum() {
		return startNum;
	}
	
	public int getRealPrice() {
		return Integer.valueOf(price) / 100;
	}

	public void setStartNum(int startNum) {
		this.startNum = startNum;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getWangwang() {
		return wangwang;
	}

	public void setWangwang(String wangwang) {
		this.wangwang = wangwang;
	}

	public String getQQNum() {
		return QQNum;
	}

	public void setQQNum(String qQNum) {
		QQNum = qQNum;
	}

	public Long getCreated() {
		return created;
	}

	public void setCreated(Long created) {
		this.created = created;
	}

	public Long getChiefId() {
		return chiefId;
	}

	public void setChiefId(Long chiefId) {
		this.chiefId = chiefId;
	}

	public long getDealingUserCount() {
		return dealingUserCount;
	}

	public void setDealingUserCount(long dealingUserCount) {
		this.dealingUserCount = dealingUserCount;
	}

	@Override
	@JsonIgnore
	public String getTableHashKey() {
		return null;
	}

	@Override
	@JsonIgnore
	public String getTableName() {
		return this.TABLE_NAME;
	}

	@Override
	@JsonIgnore
	public String getIdColumn() {
		return "id";
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	@JsonIgnore
	public String getIdName() {
		return "id";
	}

	@Override
	public Long getId() {
		return id;
	}

	@Transient
	static String insertSQL = "insert into `chief_staff_detail` (`companyName`,`chiefId`,`startNum`,`logo`,`price`,"
			+ "`remark`,`wangwang`,`QQNum`,`created`) values(?,?,?,?,?,?,?,?,?)";

	@Override
	public boolean jdbcSave() {
		long now = System.currentTimeMillis();
		long id = JDBCBuilder.insert(false, insertSQL, this.companyName, this.chiefId,
				this.startNum, this.logo, this.price, this.remark,
				this.wangwang, this.QQNum, now);

		if (id > 0L) {
			return true;
		} else {
			log.error("Insert chief_staff_detail Fails.....");
			return false;
		}

	}

	private static final String SelectAllProperties = " companyName,startNum,logo,price,remark,wangwang,QQNum,created,chiefId,id ";

	public static List<ChiefStaffDetail> findAllDetails() {

		String query = "select " + SelectAllProperties + " from " + TABLE_NAME;

		return new JDBCBuilder.JDBCExecutor<List<ChiefStaffDetail>>(dp, query) {

			@Override
			public List<ChiefStaffDetail> doWithResultSet(ResultSet rs)
					throws SQLException {
				List<ChiefStaffDetail> catList = new ArrayList<ChiefStaffDetail>();
				while (rs.next()) {
					ChiefStaffDetail catPlay = parseChiefStaffDetail(rs);
					if (catPlay != null) {
						catList.add(catPlay);
					}
				}

				return catList;
			}

		}.call();
	}
	
	public static ChiefStaffDetail findByChiedName(String companyName) {
		if(StringUtils.isEmpty(companyName)) {
			return null;
		}
		String query = "select " + SelectAllProperties + " from " + TABLE_NAME + " where companyName = ?";

		return new JDBCBuilder.JDBCExecutor<ChiefStaffDetail>(dp, query, companyName) {

			@Override
			public ChiefStaffDetail doWithResultSet(ResultSet rs)
					throws SQLException {
				if (rs.next()) {
					ChiefStaffDetail catPlay = parseChiefStaffDetail(rs);
					if (catPlay != null) {
						return catPlay;
					}
				}

				return null;
			}

		}.call();
	}

	private static ChiefStaffDetail parseChiefStaffDetail(ResultSet rs) {
		try {
			ChiefStaffDetail detail = new ChiefStaffDetail();
			detail.setCompanyName(rs.getString(1));
			detail.setStartNum(rs.getInt(2));
			detail.setLogo(rs.getString(3));
			detail.setPrice(rs.getString(4));
			detail.setRemark(rs.getString(5));
			detail.setWangwang(rs.getString(6));
			detail.setQQNum(rs.getString(7));
			detail.setCreated(rs.getLong(8));
			detail.setChiefId(rs.getLong(9));
			detail.setId(rs.getLong(10));
			return detail;

		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return null;
		}
	}
}
