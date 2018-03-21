package models.item;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.api.domain.ItemCat;

import bustbapi.ItemCatApi.ItemcatsGet;
import play.db.jpa.GenericModel;
import transaction.DBBuilder;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = NewItemCatPlay.TABLE_NAME)
public class NewItemCatPlay extends GenericModel implements PolicySQLGenerator {
    
    private static final Logger log = LoggerFactory.getLogger(NewItemCatPlay.class);

    public static final String TABLE_NAME = "new_item_cat";

    public static final NewItemCatPlay EMPTY = new NewItemCatPlay();

    public static final DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public long id;
    
    // 类目名称
    public String name;

    // 一级类目Id
    public Long level_1;
    
    // 二级类目Id
    public Long level_2;
    
    // 三级类目Id
    public Long level_3;
    
    // 四级类目Id
    public Long level_4;
    
    // 五级类目Id
    public Long level_5;
    
    // 展现量
    public Long impressions;
    
    // 展现量实际对应的词
    public String word;
    
    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getLevel_1() {
		return level_1;
	}

	public void setLevel_1(Long level_1) {
		this.level_1 = level_1;
	}

	public Long getLevel_2() {
		return level_2;
	}

	public void setLevel_2(Long level_2) {
		this.level_2 = level_2;
	}

	public Long getLevel_3() {
		return level_3;
	}

	public void setLevel_3(Long level_3) {
		this.level_3 = level_3;
	}

	public Long getLevel_4() {
		return level_4;
	}

	public void setLevel_4(Long level_4) {
		this.level_4 = level_4;
	}

	public Long getLevel_5() {
		return level_5;
	}

	public void setLevel_5(Long level_5) {
		this.level_5 = level_5;
	}

	public Long getImpressions() {
		return impressions;
	}

	public void setImpressions(Long impressions) {
		this.impressions = impressions;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public void setId(long id) {
		this.id = id;
	}

	public static Logger getLog() {
        return log;
    }

    public static NewItemCatPlay getEmpty() {
        return EMPTY;
    }

    public static DBDispatcher getDp() {
        return dp;
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
    
    public NewItemCatPlay() {
	
    }
    
    public NewItemCatPlay(String name, long level_1, long level_2, long level_3, long level_4, long level_5) {
		super();
		this.name = name;
        this.level_1 = level_1;
        this.level_2 = level_2;
        this.level_3 = level_3;
        this.level_4 = level_4;
        this.level_5 = level_5;
    }
    
    public NewItemCatPlay(String name, long level_1, long level_2, long level_3, long level_4, long level_5, long impressions, String word) {
		super();
		this.name = name;
        this.level_1 = level_1;
        this.level_2 = level_2;
        this.level_3 = level_3;
        this.level_4 = level_4;
        this.level_5 = level_5;
        this.impressions = impressions;
        this.word = word;
    }
    
    public static long findExistId(Long id) {
	
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
        	this.setId(existdId);
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
        
    }
    
    public boolean rawInsert() {
        
        String insertSQL = "insert into `" + TABLE_NAME + "`(`name`," +
                "`level_1`,`level_2`,`level_3`,`level_4`,`level_5`,`impressions`,`word`) " +
                " values(?,?,?,?,?,?,?,?)";

        long id = dp.insert(true, insertSQL,
                this.name, this.level_1, this.level_2, this.level_3, this.level_4, this.level_5, this.impressions, this.word);

        if (id > 0L) {
            this.setId(id);
            return true;
        } else {
            log.error("Insert Fails....." + "[id : ]" + this.id + "[name : ]" + this.name);
            return false;
        }

    }
    
    public boolean rawUpdate() {
        
        String updateSQL = "update `" + TABLE_NAME + "` set " +
                "`name` = ?, `level_1` = ?, `level_2` = ?, `level_3` = ?, `level_4` = ?, `level_5` = ?, `impressions` = ?, `word` = ?" +
                " where `id` = ? ";

        long updateNum = dp.update(updateSQL, 
        		this.name, this.level_1, this.level_2, this.level_3, this.level_4, this.level_5, this.impressions, this.word,
                this.id);

        if (updateNum >= 1) {
            return true;
        } else {
            log.error("update failed...for id : ]" + this.id + "[name : ]" + this.name);
            return false;
        }
    }
    
    public boolean rawDelete() {
	
		String sql = " delete from " + TABLE_NAME + " where id = ? ";
		
		dp.update(sql, this.id);
		
		return true;
    }
    
    public static NewItemCatPlay findById(Long id) {
    	
    	String query = " SELECT " + SelectAllProperty + " FROM " + TABLE_NAME + " WHERE id = ? ";
        
        return findByJDBC(query, id);
	
    }
    
    public static List<NewItemCatPlay> findNeedSearchList() {
    	
    	String query = " SELECT " + SelectAllProperty + " FROM " + TABLE_NAME
    		+ " WHERE impressions IS NULL OR impressions = '' OR word IS NULL OR word = '' ORDER BY id ASC LIMIT 100 ";
    	return findListByJDBC(query);
    	
    }
    
    private static NewItemCatPlay findByJDBC(String query, Object... params) {
        return new JDBCBuilder.JDBCExecutor<NewItemCatPlay>(dp, query, params) {

            @Override
            public NewItemCatPlay doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return parseCatPlay(rs);
                } else {
                    return null;
                }
            }

        }.call();
    }
    
    private static List<NewItemCatPlay> findListByJDBC(String query, Object... params) {
        return new JDBCBuilder.JDBCExecutor<List<NewItemCatPlay>>(dp, query, params) {

            @Override
            public List<NewItemCatPlay> doWithResultSet(ResultSet rs) throws SQLException {

                List<NewItemCatPlay> resultList = new ArrayList<NewItemCatPlay>();

                while (rs.next()) {
                    NewItemCatPlay result = parseCatPlay(rs);
                    if (result != null) {
                        resultList.add(result);
                    }
                }

                return resultList;
            }

        }.call();
    }
    
    public static String sqlSave = "INSERT INTO "+TABLE_NAME +"(name,level_1,level_2,level_3"
            + ",level_4,level_5,impressions,word)"
            + " values(?,?,?,?,?,?,?,?)";
    
    public static boolean batchSave(List<NewItemCatPlay> itemCatList) {
        return batchSql(sqlSave, true, DataSrc.BASIC, itemCatList);
    }
    
    public static boolean batchSql(String sql, boolean isKeyGenerated, DataSrc src, List<NewItemCatPlay> itemCatList){
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DBBuilder.getConn(src);
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(sql, isKeyGenerated ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
            for (int i = 0; i< itemCatList.size(); i++) {
            	NewItemCatPlay itemCatPlay = itemCatList.get(i);
                setTradeArgs(itemCatPlay, ps);
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
            return true;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        } finally {
            JDBCBuilder.closeQuitely(conn);
            JDBCBuilder.closeQuitely(ps);
        }
        return false;
    }
    
    private static void setTradeArgs(NewItemCatPlay itemCatPlay, PreparedStatement ps) throws SQLException{
        int num = 0;
        ps.setString(++num, itemCatPlay.getName());
        ps.setLong(++num, itemCatPlay.getLevel_1());
        ps.setLong(++num, itemCatPlay.getLevel_2());
        ps.setLong(++num, itemCatPlay.getLevel_3());
        ps.setLong(++num, itemCatPlay.getLevel_4());
        ps.setLong(++num, itemCatPlay.getLevel_5());
        ps.setLong(++num, itemCatPlay.getImpressions());
        ps.setString(++num, itemCatPlay.getWord());
    }
    
    private static final String SelectAllProperty = " `id`," +
                "`name`, `level_1`, `level_2`, `level_3`, `level_4`, `level_5`, `impressions`, `word` ";

    private static NewItemCatPlay parseCatPlay(ResultSet rs) {
        try {
    
            NewItemCatPlay rptObj = new NewItemCatPlay();
            
            int colIndex = 1;
    
            rptObj.id = rs.getLong(colIndex++);
            rptObj.name = rs.getString(colIndex++);
            rptObj.level_1 = rs.getLong(colIndex++);
            rptObj.level_2 = rs.getLong(colIndex++);
            rptObj.level_3 = rs.getLong(colIndex++);
            rptObj.level_4 = rs.getLong(colIndex++);
            rptObj.level_5 = rs.getLong(colIndex++);
            rptObj.impressions = rs.getLong(colIndex++);
            rptObj.word = rs.getString(colIndex++);
            
            return rptObj;
    
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
    
}
