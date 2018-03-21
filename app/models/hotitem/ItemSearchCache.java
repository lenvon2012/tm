package models.hotitem;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = ItemSearchCache.TABLE_NAME)
public class ItemSearchCache extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(ItemSearchCache.class);
    
    @Transient
    public static final String TABLE_NAME = "item_search_cache";

    @Transient
    public static ItemSearchCache EMPTY = new ItemSearchCache();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    

    @Index(name = "word")
    private String word;
    
    @Column(columnDefinition = "text")
    private String numIids;
    
    @Column(columnDefinition = "int default 0 ")
    private int searchType;
    
    public static class ItemSearchType {
        public static final int Default = 1;
        public static final int Renqi = 2;
    }
    
    private long updateTs;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getNumIids() {
        return numIids;
    }

    public void setNumIids(String numIids) {
        this.numIids = numIids;
    }

    public int getSearchType() {
        return searchType;
    }

    public void setSearchType(int searchType) {
        this.searchType = searchType;
    }

    public long getUpdateTs() {
        return updateTs;
    }

    public void setUpdateTs(long updateTs) {
        this.updateTs = updateTs;
    }

    public ItemSearchCache() {
        super();
    }
    
    public ItemSearchCache(String word, String numIids, int searchType) {
        super();
        this.word = word;
        this.numIids = numIids;
        this.searchType = searchType;
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
    public String getIdName() {
        return "id";
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    
    public static long findExistId(String word, int searchType) {
        
        String query = "select id from " + TABLE_NAME + " where word = ? and searchType = ? ";
        
        return dp.singleLongQuery(query, word, searchType);
    }
    
    @Override
    public boolean jdbcSave() {
        try {
            
            long existdId = findExistId(this.word, this.searchType);

            if (existdId <= 0L) {
                return this.rawInsert();
            } else {
                setId(existdId);
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean rawInsert() {

        String insertSQL = "insert into `" + TABLE_NAME + "`" +
                "(`word`,`numIids`,`searchType`,`updateTs`) " +
                " values(?,?,?,?)";
        
        updateTs = System.currentTimeMillis();
        
        long id = dp.insert(true, insertSQL, 
                this.word, this.numIids, this.searchType, this.updateTs);

        if (id > 0L) {
            setId(id);
            return true;
        } else {
            log.error("Insert Fails.....");
            return false;
        }

    }
    
    public boolean rawUpdate() {
        
        String updateSQL = "update `" + TABLE_NAME + "` set  " +
                " `numIids` = ?, `updateTs` = ? " +
                " where `word` = ? and `searchType` = ? ";
        
        updateTs = System.currentTimeMillis();
        
        long updateNum = dp.update(false, updateSQL, 
                this.numIids, this.updateTs,
                this.word, this.searchType);

        if (updateNum >= 1) {
            //log.info("update ok for :" + this.getId());
            return true;
        } else {
            log.error("update failed...for :" + this.getId());
            return false;
        }
    }
    
    
    public static ItemSearchCache findBywordAndSearchType(String word, int searchType) {
        String query = "select " + SelectAllProperty + " from " + TABLE_NAME 
                + " where word = ? and searchType = ? ";
        
        return new JDBCBuilder.JDBCExecutor<ItemSearchCache>(dp, query, word, searchType) {

            @Override
            public ItemSearchCache doWithResultSet(ResultSet rs)
                    throws SQLException {
                if (rs.next()) {
                    return parseItemSearchCache(rs);
                } else {
                    return null;
                }
            }
            
        }.call();
    }
    
    
    private static final String SelectAllProperty = " id,word,numIids,searchType,updateTs ";
    
    private static ItemSearchCache parseItemSearchCache(ResultSet rs) {
        try {
            ItemSearchCache searchRes = new ItemSearchCache();
            
            searchRes.setId(rs.getLong(1));
            searchRes.setWord(rs.getString(2));
            searchRes.setNumIids(rs.getString(3));
            searchRes.setSearchType(rs.getInt(4));
            searchRes.setUpdateTs(rs.getLong(5));
            
            return searchRes;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
    
    
}
