package models.itemCopy;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Table(name =SkuInfo.TABLE_NAME)
public class SkuInfo extends GenericModel implements PolicySQLGenerator{
	public static final String TABLE_NAME = "sku_info";

	public static SkuInfo EMPTY = new SkuInfo();

	public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

	public static final Logger log = LoggerFactory.getLogger(SkuInfo.class);
	

	@Index(name = "pandv")	
	@Id
	private String pandv;

	private String name;


	
	public String getPandv() {
		return pandv;
	}

	public void setPandv(String pandv) {
		this.pandv = pandv;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 根据值名称获取对应符合的SkuInfo
	 * @return 成功返回结果，失败或者没找到返回null
	 */
	public static SkuInfo getSkuInfo(String name){
		String equalsSql="select * from "+TABLE_NAME+" where name = ? limit 1";
		
		String likeSql="select * from "+TABLE_NAME+" where name like ? limit 1";
		
		SkuInfo equalSkuInfo= new JDBCBuilder.JDBCExecutor<SkuInfo>(dp, equalsSql, name) {

			@Override
			public SkuInfo doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
		
		if (equalSkuInfo!=null) {
			return equalSkuInfo;
		}
		
		SkuInfo likeSkuInfo= new JDBCBuilder.JDBCExecutor<SkuInfo>(dp, likeSql, name) {
			@Override
			public SkuInfo doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
		
		
		return likeSkuInfo;

        
	}
	
	private static SkuInfo parseResult(ResultSet rs) {
		try {
			
			SkuInfo skuInfo = new SkuInfo();
			skuInfo.pandv = rs.getString("pandv");
			skuInfo.name = rs.getString("name");
			return skuInfo;
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
		return "name";
	}

	@Override
	public Long getId() {
		return null;
	}

	@Override
	public void setId(Long id) {
		
	}

	@Override
	public boolean jdbcSave() {
		return false;
	}

	@Override
	public String getIdName() {
		return "name";
	}

}
