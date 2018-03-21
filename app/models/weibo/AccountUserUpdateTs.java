package models.weibo;

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

@Entity(name = AccountUserUpdateTs.TABLE_NAME)
public class AccountUserUpdateTs extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(AccountUserUpdateTs.class);
    
    @Transient
    public static final String TABLE_NAME = "account_user_update_ts";

    @Transient
    public static AccountUserUpdateTs EMPTY = new AccountUserUpdateTs();

    @Transient
    private static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    
    @Index(name = "userId")
    private Long userId;
    
    @Column(columnDefinition = "int default 0 ")
    private int accountType;
    
    private long dayTs;
    
    private long createTs;
    
    private long updateTs;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getAccountType() {
        return accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public long getDayTs() {
        return dayTs;
    }

    public void setDayTs(long dayTs) {
        this.dayTs = dayTs;
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

    public AccountUserUpdateTs() {
        super();
    }

    public AccountUserUpdateTs(Long userId, int accountType, long dayTs) {
        super();
        this.userId = userId;
        this.accountType = accountType;
        this.dayTs = dayTs;
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

    public static long findExistId(Long userId, int accountType) {
        
        String query = "select id from " + TABLE_NAME + " where userId = ? and accountType = ? ";
        
        return dp.singleLongQuery(query, userId, accountType);
    }

    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.userId, this.accountType);

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
                "(`userId`,`accountType`,`dayTs`," +
                "`createTs`,`updateTs`) " +
                " values(?,?,?,?,?)";
        
        createTs = System.currentTimeMillis();
        updateTs = System.currentTimeMillis();
        
        long id = dp.insert(true, insertSQL, 
                this.userId, this.accountType, this.dayTs, 
                this.createTs, this.updateTs);

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
                " `dayTs` = ?, " +
                " `updateTs` = ? " +
                " where userId = ? and accountType = ? ";
        
        updateTs = System.currentTimeMillis();
        
        long updateNum = dp.update(false, updateSQL, 
                this.dayTs, 
                this.updateTs,
                this.userId, this.accountType);

        if (updateNum >= 1) {
            //log.info("update ok for :" + this.getId());
            return true;
        } else {
            log.error("update failed...for :" + this.getId());
            return false;
        }
    }
    
    
    public static AccountUserUpdateTs findByAccountType(Long userId, int accountType) {
        
        String query = " select " + SelectAllProperty + " from " + TABLE_NAME + " where userId = ? " +
                " and accountType = ? ";
        
        return new JDBCBuilder.JDBCExecutor<AccountUserUpdateTs>(dp, query, userId, accountType) {

            @Override
            public AccountUserUpdateTs doWithResultSet(ResultSet rs)
                    throws SQLException {
                
                if (rs.next()) {
                    AccountUserUpdateTs updateTs = parseAccountUserUpdateTs(rs);
                    return updateTs;
                } else {
                    return null;
                }
            }
            
        }.call();
        
    }
    
    private static final String SelectAllProperty = " id,userId,accountType,dayTs," +
            "createTs,updateTs ";

    private static AccountUserUpdateTs parseAccountUserUpdateTs(ResultSet rs) {
    try {
        
        AccountUserUpdateTs updateTs = new AccountUserUpdateTs();
        
        updateTs.setId(rs.getLong(1));
        updateTs.setUserId(rs.getLong(2));
        updateTs.setAccountType(rs.getInt(3));
        updateTs.setDayTs(rs.getLong(4));
        updateTs.setCreateTs(rs.getLong(5));
        updateTs.setUpdateTs(rs.getLong(6));
        
        return updateTs;
        
    } catch (Exception ex) {
        log.error(ex.getMessage(), ex);
        return null;
    }
}
}
