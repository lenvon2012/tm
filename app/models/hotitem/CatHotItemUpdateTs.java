package models.hotitem;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = CatHotItemUpdateTs.TABLE_NAME)
public class CatHotItemUpdateTs extends GenericModel implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(CatHotItemUpdateTs.class);
    
    @Transient
    public static final String TABLE_NAME = "cat_hot_item_update_ts";

    @Transient
    public static CatHotItemUpdateTs EMPTY = new CatHotItemUpdateTs();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    
    @PolicySQLGenerator.CodeNoUpdate
    @Id
    private Long cid;

    private long createTs;
    
    private long updateTs;

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public long getCreateTs() {
        return createTs;
    }

    public void setCreateTs(long createTs) {
        this.createTs = createTs;
    }

    public long getUpdateTs() {
        return updateTs;
    }

    public void setUpdateTs(long updateTs) {
        this.updateTs = updateTs;
    }

    public CatHotItemUpdateTs() {
        super();
    }

    public CatHotItemUpdateTs(Long cid) {
        super();
        this.cid = cid;
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
    public String getIdName() {
        return "cid";
    }

    @Override
    public Long getId() {
        return cid;
    }

    @Override
    public void setId(Long id) {
        this.cid = id;
    }

    public static long findExistId(Long cid) {
        
        String query = "select cid from " + TABLE_NAME + " where cid = ? ";
        
        return dp.singleLongQuery(query, cid);
    }
    
    @Override
    public boolean jdbcSave() {
        try {
            
            long existdId = findExistId(this.cid);

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
                "(`cid`,`createTs`,`updateTs`) " +
                " values(?,?,?)";
        
        createTs = System.currentTimeMillis();
        updateTs = System.currentTimeMillis();
        
        long id = dp.insert(true, insertSQL, 
                this.cid, this.createTs, this.updateTs);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails.....");
            return false;
        }

    }
    
    public boolean rawUpdate() {
        
        String updateSQL = "update `" + TABLE_NAME + "` set  " +
                " `updateTs` = ? where `cid` = ? ";
        
        updateTs = System.currentTimeMillis();
        
        long updateNum = dp.update(false, updateSQL, 
                this.updateTs, this.cid);

        if (updateNum == 1) {
            //log.info("update ok for :" + this.getId());
            return true;
        } else {
            log.error("update failed...for :" + this.getId());
            return false;
        }
    }
    
    public static CatHotItemUpdateTs findByCid(Long cid) {
        String query = "select " + SelectAllProperty + " from " + TABLE_NAME + " where cid = ?";
        
        return new JDBCBuilder.JDBCExecutor<CatHotItemUpdateTs>(dp, query, cid) {

            @Override
            public CatHotItemUpdateTs doWithResultSet(ResultSet rs)
                    throws SQLException {
                if (rs.next()) {
                    return parseCatHotItemUpdateTs(rs);
                } else {
                    return null;
                }
            }
            
            
            
        }.call();
    }
    
    
    private static final String SelectAllProperty = " cid,createTs,updateTs ";
    
    private static CatHotItemUpdateTs parseCatHotItemUpdateTs(ResultSet rs) {
        try {
            CatHotItemUpdateTs updateTs = new CatHotItemUpdateTs();
            
            updateTs.setCid(rs.getLong(1));
            updateTs.setCreateTs(rs.getLong(2));
            updateTs.setUpdateTs(rs.getLong(3));
            
            return updateTs;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
    
}
