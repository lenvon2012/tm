package models.jd;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = JDCrmMemberUpdateTs.TABLE_NAME)
public class JDCrmMemberUpdateTs extends GenericModel implements PolicySQLGenerator {
    private static final Logger log = LoggerFactory.getLogger(JDCrmMemberUpdateTs.class);
    
    public static final String TABLE_NAME = "JDCrmMemberUpdateTs_";
    
    private static final JDCrmMemberUpdateTs EMPTY = new JDCrmMemberUpdateTs();
    
    private static final DBDispatcher CrmMemberUpdateTsDp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    
    @Id
    private long sellerId;
    
    
    private long firstUpdateTime;
    
    private long lastUpdateTime;

    

    public long getSellerId() {
        return sellerId;
    }

    public void setSellerId(long sellerId) {
        this.sellerId = sellerId;
    }

    public long getFirstUpdateTime() {
        return firstUpdateTime;
    }

    public void setFirstUpdateTime(long firstUpdateTime) {
        this.firstUpdateTime = firstUpdateTime;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
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

        return "sellerId";
    }

    @Override
    public Long getId() {
        return sellerId;
    }

    @Override
    public void setId(Long id) {
        this.sellerId = id;
    }

    private static String EXIST_ID_QUERY = "select sellerId from " + TABLE_NAME + " where sellerId = ? ";
    
    private static long findExistId(long sellerId) {
        
        return CrmMemberUpdateTsDp.singleLongQuery(EXIST_ID_QUERY, sellerId);
        
    }
    
    public boolean rawInsert() {
        
        this.firstUpdateTime = lastUpdateTime;
        
        String insertSql = "insert into `" + TABLE_NAME + "`(`sellerId`,`firstUpdateTime`,`lastUpdateTime`) values(?,?,?)";
        
        long id = CrmMemberUpdateTsDp.insert(insertSql, this.sellerId, this.firstUpdateTime, this.lastUpdateTime);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[sellerId : ]" + this.sellerId);

            return false;
        }
    }
    
    public boolean rawUpdate() {
        String updateSql = "update `" + TABLE_NAME + "` set `lastUpdateTime` = ? where `sellerId` = ?";
        
        long updateNum = CrmMemberUpdateTsDp.insert(updateSql, this.lastUpdateTime, this.sellerId);

        if (updateNum == 1L) {
            return true;
        } else {
            log.error("update Fails....." + "[sellerId : ]" + this.sellerId);

            return false;
        }
    }
    
    
    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.sellerId);
            
            boolean status = false;
            if (existdId <= 0L) {
                status = rawInsert();
            } else {
                status = rawUpdate();
            }
            
            return status;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public String getIdName() {
        return "sellerId";
    }
    
    
    private static final String UpdateTsProperties = " sellerId, firstUpdateTime, lastUpdateTime ";
    
    public static JDCrmMemberUpdateTs findBySellerId(long sellerId) {
        String sql = " select " + UpdateTsProperties + " from " + TABLE_NAME + " where sellerId = ? ";
        
        return new JDBCExecutor<JDCrmMemberUpdateTs>(CrmMemberUpdateTsDp, sql, sellerId) {
            @Override
            public JDCrmMemberUpdateTs doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return parseJDCrmUpdateTs(rs);
                } else {
                    return null;
                }
            }
        }.call();
        
    }
    
    
    
    private static JDCrmMemberUpdateTs parseJDCrmUpdateTs(ResultSet rs) {
        try {
            
            JDCrmMemberUpdateTs updateTs = new JDCrmMemberUpdateTs();
            
            updateTs.setSellerId(rs.getLong(1));
            updateTs.setFirstUpdateTime(rs.getLong(2));
            updateTs.setLastUpdateTime(rs.getLong(3));
            
            return updateTs;
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
    
    
    
    
    
}
