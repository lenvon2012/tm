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
 * 不可以自定义的类目销售属性
 * 
 * @author oyster
 * 
 */
@Entity(name = NotCustomizableProp.TABLE_NAME)
public class NotCustomizableProp extends Model implements PolicySQLGenerator {

	public static final String TABLE_NAME = "not_customeizable_prop";
	
	public static NotCustomizableProp EMPTY = new NotCustomizableProp();

	public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

	public static final Logger log = LoggerFactory.getLogger(NotCustomizableProp.class);

	private long cid; // 淘宝cid
	
	private long pid;
	
	private long createTs;

	public NotCustomizableProp() {
		super();
	}

	private static NotCustomizableProp parseResult(ResultSet rs) {
		try {

			NotCustomizableProp icp = new NotCustomizableProp();
			icp.cid = rs.getLong("cid");
			icp.pid = rs.getLong("pid");
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
		return id ;
	}

	@Override
	public void setId(Long id) {
		this.id = id;

	}

	@Override
	public boolean jdbcSave() {
//		NotCustomizableProp mapping=getMappingByAliCid(alicid);
//		if (mapping==null) {
			return this.rawInsert();
//		}
//		
//		return mapping.rawUpdate();
		

	}

	public boolean rawInsert() {
		try {
			long now=System.currentTimeMillis();
			String insertSQL = "insert into `" + TABLE_NAME
					+ "`(`cid`,`pid`,`createTs`) values(?,?,?)";
			long id = dp.insert(insertSQL, this.cid, this.pid,now);
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
//		try {
////			long now=System.currentTimeMillis();
//			String updateSQL = "update  `" + TABLE_NAME
//					+ "` set `pid` = ? ,`cid` = ? ,`tbItemId` = ? where `alicid` = ? ";
//			long id = dp.update(insertSQL, this.tbcid,this.aliItemId,this.tbItemId,this.alicid);
//			if (id > 0L) {
//				return true;
//			} else {
//				return false;
//			}
//
//		} catch (Exception ex) {
//			log.error(ex.getMessage(), ex);
			return false;
//		}
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
		NotCustomizableProp icp = findById(cid);
		if (icp != null) {
			icp.delete();
		}

	}
	
	public static NotCustomizableProp getPropCidAndPid(long cid,long pid){
		String sql="select * from "+TABLE_NAME+" where cid = ? and pid = ? ";
		return new JDBCBuilder.JDBCExecutor<NotCustomizableProp>(dp, sql, cid,pid) {

			@Override
			public NotCustomizableProp doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
	}


	public void setCreateTs(long createTs) {
		this.createTs = createTs;
	}
	

	public long getCid() {
		return cid;
	}

	public void setCid(long cid) {
		this.cid = cid;
	}

	public long getPid() {
		return pid;
	}

	public void setPid(long pid) {
		this.pid = pid;
	}

	public long getCreateTs() {
		return createTs;
	}

//	public static NotCustomizableProp formatObj(long alicid, long tbcid, long aliItemId, long tbItemId) {
//		NotCustomizableProp mapping=new NotCustomizableProp();
//		mapping.alicid = alicid;
//		mapping.tbcid = tbcid;
//		mapping.aliItemId = aliItemId;
//		mapping.tbItemId = tbItemId;
//		return mapping;
//	}
//	
	
	
	
	

}
