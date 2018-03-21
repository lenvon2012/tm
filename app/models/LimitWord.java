
package models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.persistence.Entity;

import models.item.ItemCatPlay;
import models.itemCopy.AliCatMapping;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;
import play.Play;
import play.data.validation.Unique;
import play.db.jpa.GenericModel;
import play.db.jpa.Model;
import transaction.JDBCBuilder;
import transaction.DBBuilder.DataSrc;


@Entity(name = LimitWord.TABLE_NAME)
public class LimitWord extends Model implements PolicySQLGenerator  {
    public static final String TABLE_NAME = "limit_words";

    public static final Logger log = LoggerFactory.getLogger(ItemCatPlay.class);

    public static LimitWord EMPTY = new LimitWord();
    
	public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    @Unique
    private String keyWord;


    public LimitWord() {

    }

    public LimitWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public String toString() {
        return "HotTitle [keyWord=" + keyWord + "]";
    }

    
	public static LimitWord parseResult(ResultSet result) {
		try {
			LimitWord limitWord=new LimitWord();
			limitWord.keyWord = result.getString("keyWord");
			limitWord.id = result.getLong("id");
			return limitWord;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
		
	}

	
	public static LimitWord getByKeyWord(long keyWord){
		String sql="select * from "+TABLE_NAME+" where keyWord = ?";
		return new JDBCBuilder.JDBCExecutor<LimitWord>(dp, sql, keyWord) {

			@Override
			public LimitWord doWithResultSet(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return parseResult(rs);
				} else {
					return null;
				}
			}

		}.call();
	}
	
	public static String getWholeLimitStr(){
		String sql="select keyWord from "+TABLE_NAME;
		return new JDBCBuilder.JDBCExecutor<String>(dp, sql) {

			@Override
			public String doWithResultSet(ResultSet rs) throws SQLException {
				StringBuffer result=new StringBuffer();
				while (rs.next()) {
					result.append(rs.getString(1)+"|");
				}
				return result.toString();
				
			}

		}.call();
	}

	@Override
	public boolean jdbcSave() {
//		AliCatMapping mapping=getMappingByAliCid(alicid);
//		log.info("查询得之："+mapping);
//		if (mapping==null) {
//			log.info("rawInsert start");
			return this.rawInsert();
//		}
//		log.info("rawUpdate start");
//		return this.rawUpdate();

	}

	public boolean rawInsert() {
		try {
			String insertSQL = "insert into `" + TABLE_NAME
					+ "`(`keyWord`) values(?)";
			long id = dp.insert(insertSQL, this.keyWord);
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
					+ "` where `keyWord` = ? ";
			long id = dp.update(sql,this.keyWord);
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
					+ "` set `keyWord` = ? ";
			long id = dp.update(insertSQL, this.keyWord);
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
	public void setId(Long id) {
		this.id=id;
	}


	@Override
	public String getIdName() {
		return "id";
	}
    
    

    
}
