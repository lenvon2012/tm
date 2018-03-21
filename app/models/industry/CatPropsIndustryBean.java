package models.industry;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;


/**
 * 类目属性的行业数据
 * @author ying
 *
 */
@Entity(name = CatPropsIndustryBean.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "dataSrc", "persistent", "entityId",
        "entityId", "ts", "numIid", "detailURL", "sellerCids", "tableHashKey", "persistent", "tableName",
        "idName", "idColumn",
})
public class CatPropsIndustryBean extends GenericModel implements PolicySQLGenerator {
    
    private static final Logger log = LoggerFactory.getLogger(CatPropsIndustryBean.class);
    
    @Transient
    public static final String TABLE_NAME = "cat_props_industry_bean_";

    @Transient
    public static CatPropsIndustryBean EMPTY = new CatPropsIndustryBean();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    //类目ID
    @PolicySQLGenerator.CodeNoUpdate
    @Id
    private Long cid;
    
    @Column(columnDefinition = "longtext")
    private String propsJson;
    
    @Column(columnDefinition = "int default 0 ")
    private int status;
    
    public static class CatPropsIndustryStatus {
        public static final int NotKnown = 0;
        public static final int LoadedWordBase = 1; // propsJson中已经把属性的click,pv等值获取到了
    }
    
    private long createTs;
    
    private long updateTs;

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public String getPropsJson() {
        return propsJson;
    }

    public void setPropsJson(String propsJson) {
        this.propsJson = propsJson;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    
    public boolean isLoadedWordBase() {
        return (status & CatPropsIndustryStatus.LoadedWordBase) > 0;
    }
    
    public void setLoadedWordBase() {
        status = status | CatPropsIndustryStatus.LoadedWordBase;
    }
    
    public void removeLoadedWordBase() {
        status = status & (~CatPropsIndustryStatus.LoadedWordBase);
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

    public CatPropsIndustryBean() {
        super();
    }

    public CatPropsIndustryBean(Long cid) {
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
        		"(`cid`,`propsJson`,`status`,`createTs`,`updateTs`) " +
                " values(?,?,?,?,?)";
        
        createTs = System.currentTimeMillis();
        updateTs = System.currentTimeMillis();
        
        long id = dp.insert(false, insertSQL, 
                this.cid, this.propsJson, this.status, this.createTs, this.updateTs);

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
        		" `propsJson` = ?, `status` = ?, `updateTs` = ? " +
        		" where `cid` = ? ";
        
        updateTs = System.currentTimeMillis();
        
        long updateNum = dp.insert(false, updateSQL, 
                this.propsJson, this.status, this.updateTs, 
                this.cid);

        if (updateNum == 1) {
            log.info("update ok for :" + this.getId());
            return true;
        } else {
            log.error("update failed...for :" + this.getId());
            return false;
        }
    }
    
    public static CatPropsIndustryBean findByCatId(Long cid) {
        if (cid == null || cid <= 0) {
            return null;
        }
        String query = " select " + SelectAllProperties + " from " + TABLE_NAME + " where cid = ? ";
        
        return new JDBCBuilder.JDBCExecutor<CatPropsIndustryBean>(dp, query, cid) {

            @Override
            public CatPropsIndustryBean doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {

                    return parseCatPropsIndustryBean(rs);

                } else {
                    return null;
                }

            }

        }.call();
        
    }
    
    private static final String SelectAllProperties = " cid,propsJson,status,createTs,updateTs ";
    
    public static CatPropsIndustryBean parseCatPropsIndustryBean(ResultSet rs) {
        try {
            CatPropsIndustryBean propsBean = new CatPropsIndustryBean();
            
            propsBean.setCid(rs.getLong(1));
            propsBean.setPropsJson(rs.getString(2));
            propsBean.setStatus(rs.getInt(3));
            propsBean.setCreateTs(rs.getLong(4));
            propsBean.setUpdateTs(rs.getLong(5));
            
            return propsBean;
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
    
}
