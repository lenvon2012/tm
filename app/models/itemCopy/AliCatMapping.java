package models.itemCopy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Query;

import models.CategoryProps;
import models.item.ItemCatPlay;
import models.jms.JMSMsgLog;
import models.task.AutoTitleTask;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;
import play.db.jpa.GenericModel;
import play.db.jpa.JPA;
import play.db.jpa.Model;
import transaction.JDBCBuilder;
import transaction.DBBuilder.DataSrc;
import utils.CommonUtil;

/**
 * 阿里类目到淘宝类目映射
 * 
 * @author oyster
 * 
 */
@Entity(name = AliCatMapping.TABLE_NAME)
public class AliCatMapping extends GenericModel implements PolicySQLGenerator {

	public static final String TABLE_NAME = "alicat_to_tbcat";
	
	public static AliCatMapping EMPTY = new AliCatMapping();

	public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

	public static final Logger log = LoggerFactory.getLogger(AliCatMapping.class);

	@Index(name = "alicid")
	@Id
	private long alicid; // 1688cid

	private long tbcid; // 淘宝cid
	
	private long aliItemId;
	
	private long tbItemId;
	
	private long createTs;

	public AliCatMapping() {
		super();
	}

	private static AliCatMapping parseResult(ResultSet rs) {
		try {

			AliCatMapping icp = new AliCatMapping();
			icp.alicid = rs.getLong("alicid");
			icp.tbcid = rs.getLong("tbcid");
			icp.aliItemId = rs.getLong("aliItemId");
			icp.tbItemId = rs.getLong("tbItemId");
			icp.createTs = rs.getLong("createTs");
			return icp;

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
		return "cid";
	}

	@Override
	public Long getId() {
		return alicid;
	}

	@Override
	public void setId(Long id) {
		this.alicid = id;

	}

	@Override
	public boolean jdbcSave() {
		AliCatMapping mapping=getMappingByAliCid(alicid);
		log.info("查询得之："+mapping);
		if (mapping==null) {
			log.info("rawInsert start");
			return this.rawInsert();
		}
		log.info("rawUpdate start");
		return this.rawUpdate();

	}

	public boolean rawInsert() {
		try {
			long now=System.currentTimeMillis();
			String insertSQL = "insert into `" + TABLE_NAME
					+ "`(`alicid`,`tbcid`,`aliItemId`,`tbItemId`,`createTs`) values(?,?,?,?,?)";
			long id = dp.insert(insertSQL, this.alicid, this.tbcid,this.aliItemId,this.tbItemId,now);
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
	
	public boolean rawDelete() {
		try {
			String sql = "delete  `" + TABLE_NAME
					+ "` where `alicid` = ? and `tbcid` = ?";
			long id = dp.update(sql,  this.alicid, this.tbcid);
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
//			long now=System.currentTimeMillis();
			String insertSQL = "update  `" + TABLE_NAME
					+ "` set `tbcid` = ? ,`aliItemId` = ? ,`tbItemId` = ? where `alicid` = ? ";
			long id = dp.update(insertSQL, this.tbcid,this.aliItemId,this.tbItemId,this.alicid);
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
		return "cid";
	}

	/**
	 * 删除数据库中的记录
	 * 
	 * @param cid
	 */
	public static void deleteByCid(Long cid) {
		AliCatMapping icp = findById(cid);
		if (icp != null) {
			icp.delete();
		}

	}
	
	public static AliCatMapping getMappingByAliCid(long alicid){
		String sql="select * from "+TABLE_NAME+" where alicid = ?";
		return new JDBCBuilder.JDBCExecutor<AliCatMapping>(dp, sql, alicid) {

			@Override
			public AliCatMapping doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
	}

	public long getAlicid() {
		return alicid;
	}

	public long getTbcid() {
		return tbcid;
	}

	public void setAlicid(long alicid) {
		this.alicid = alicid;
	}

	public void setTbcid(long tbcid) {
		this.tbcid = tbcid;
	}

	public long getAliItemId() {
		return aliItemId;
	}

	public long getTbItemId() {
		return tbItemId;
	}

	public long getCreateTs() {
		return createTs;
	}

	public void setAliItemId(long aliItemId) {
		this.aliItemId = aliItemId;
	}

	public void setTbItemId(long tbItemId) {
		this.tbItemId = tbItemId;
	}

	public void setCreateTs(long createTs) {
		this.createTs = createTs;
	}


	public static AliCatMapping formatObj(long alicid, long tbcid, long aliItemId, long tbItemId) {
		AliCatMapping mapping=new AliCatMapping();
		mapping.alicid = alicid;
		mapping.tbcid = tbcid;
		mapping.aliItemId = aliItemId;
		mapping.tbItemId = tbItemId;
		return mapping;
	}

	@Override
	public String toString() {
		return "AliCatMapping [alicid=" + alicid + ", tbcid=" + tbcid
				+ ", aliItemId=" + aliItemId + ", tbItemId=" + tbItemId
				+ ", createTs=" + createTs + "]";
	}
	
	
	
	
	

}
