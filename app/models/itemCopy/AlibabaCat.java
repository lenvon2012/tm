package models.itemCopy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Query;

import models.CategoryProps;
import models.carrierTask.CarrierTaskForDQ;
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
import utils.ApiUtilFor1688;
import utils.CommonUtil;

/**
 * 阿里类目到淘宝类目映射
 * 
 * @author oyster
 * 
 */
public class AlibabaCat extends Model implements PolicySQLGenerator {

	public static final String TABLE_NAME = "alibaba_cat";

	public static final String ALI_CAT_TABLE_NAME = "";

	public static AlibabaCat EMPTY = new AlibabaCat();

	public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

	public static final Logger log = LoggerFactory.getLogger(ItemCatPlay.class);

	private long catId; // 1688cid

	private String catName; // 淘宝cid

	private boolean isLeaf;

	private long parentId;

	public AlibabaCat() {
		super();
	}

	private static AlibabaCat parseResult(ResultSet rs) {
		try {

			AlibabaCat icp = new AlibabaCat();
			icp.catId = rs.getLong("catId");
			icp.catName = rs.getString("catName");
			icp.isLeaf = rs.getBoolean("isLeaf");
			icp.parentId = rs.getLong("parentId");
			icp.id = rs.getLong("id");
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

		return this.rawInsert();

	}

	public boolean rawInsert() {
		try {
			long now = System.currentTimeMillis();
			String insertSQL = "insert into `"
					+ TABLE_NAME
					+ "`(`catId`,`catName`,`isLeaf`,`parentId`) values(?,?,?,?)";
			long id = dp.insert(insertSQL, this.catId, this.catName,
					this.isLeaf, this.parentId);
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
		AlibabaCat icp = findById(cid);
		if (icp != null) {
			icp.delete();
		}

	}

	public static AlibabaCat getMappingByAliCid(long alicid) {
		String sql = "select * from " + TABLE_NAME + " where alicid = ?";
		return new JDBCBuilder.JDBCExecutor<AlibabaCat>(dp, sql, alicid) {

			@Override
			public AlibabaCat doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
	}

	// public static AlibabaCat formatObj(long alicid, long tbcid, long
	// aliItemId, long tbItemId) {
	// AlibabaCat cat=new AlibabaCat();
	// cat.alicid = alicid;
	// mapping.tbcid = tbcid;
	// mapping.aliItemId = aliItemId;
	// mapping.tbItemId = tbItemId;
	// return mapping;
	// }

	// 查询数据库中1688 cid对应的类目名称（含父类目）
	public static String getWholeCatName(long catId){
		StringBuffer catStr=new StringBuffer();
		AlibabaCat sonCat=findByCatId(catId);
		//如果数据库中未找到该类目信息，则调用1688接口保存
		if (sonCat==null) {
			AlibabaCat cat=ApiUtilFor1688.getAliCatByCid(catId);
			cat.rawInsert();
			sonCat=cat;
		}
		while (sonCat.getParentId()!=0) {
			catStr.append(sonCat.getCatName()+",");
			sonCat=findByCatId(sonCat.getParentId());
		}
		
		return catStr.toString();
	}
	
	public static AlibabaCat findByCatId(long catId){
		
		String sql="select * from "+TABLE_NAME+" where catId = ? ";
		return findByJDBC(sql, catId);
		
	}
	
	private static AlibabaCat findByJDBC(String query, Object... params) {
		return new JDBCBuilder.JDBCExecutor<AlibabaCat>(dp, query, params) {

			@Override
			public AlibabaCat doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
	}
	
	private static List<AlibabaCat> findListByJDBC(String query,Object... params) {
		
		return new JDBCBuilder.JDBCExecutor<List<AlibabaCat>>(dp, query, params) {

			@Override
			public List<AlibabaCat> doWithResultSet(ResultSet rs)
					throws SQLException {

				List<AlibabaCat> resultList = new ArrayList<AlibabaCat>();

				while (rs.next()) {
					AlibabaCat result = parseResult(rs);
					if (result != null) {
						resultList.add(result);
					}
				}

				return resultList;
			}

		}.call();
	}

	public long getCatId() {
		return catId;
	}

	public String getCatName() {
		return catName;
	}

	public boolean isLeaf() {
		return isLeaf;
	}

	public long getParentId() {
		return parentId;
	}

	public void setCatId(long catId) {
		this.catId = catId;
	}

	public void setCatName(String catName) {
		this.catName = catName;
	}

	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	public void setParentId(long parentId) {
		this.parentId = parentId;
	}
	
	
	

}
