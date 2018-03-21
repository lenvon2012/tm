package models.weibo;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = ClearWeiboMsgLog.TABLE_NAME)
public class ClearWeiboMsgLog extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(ClearWeiboMsgLog.class);
    
    @Transient
    public static final String TABLE_NAME = "clear_weibo_msg_log";

    @Transient
    public static ClearWeiboMsgLog EMPTY = new ClearWeiboMsgLog();

    @Transient
    private static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    @Column(columnDefinition = "int default 0 ")
    private int accountType;
    
    @Column(columnDefinition = "int default 0 ")
    private int deleteWeiboNum;
    
    @Column(columnDefinition = "int default 0 ")
    private int remainWeiboNum;
    
    @Column(columnDefinition = "int default 0 ")
    private int oldWeiboNum;
    
    @Column(columnDefinition = "int default 0 ")
    private int notMainNum;//不再是主帐号的微博数
    
    @Column(columnDefinition = "int default 0 ")
    private int oldFakeWeiboNum;//之前僵尸粉的微博
    
    @Column(columnDefinition = "int default 0 ")
    private int unValidUserNum;//用户过期了，not valid user
    
    @Column(columnDefinition = "int default 0 ")
    private int restartCount;
    
    @Column(columnDefinition = "int default 0 ")
    private int noResultCount;
    
    @Column(columnDefinition = "int default 0 ")
    private int failCount;
    
    private long offset;
    
    private long usedTime;
    
    private long finishTs;
    
    public int getAccountType() {
        return accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public int getDeleteWeiboNum() {
        return deleteWeiboNum;
    }

    public void setDeleteWeiboNum(int deleteWeiboNum) {
        this.deleteWeiboNum = deleteWeiboNum;
    }

    public int getRemainWeiboNum() {
        return remainWeiboNum;
    }

    public void setRemainWeiboNum(int remainWeiboNum) {
        this.remainWeiboNum = remainWeiboNum;
    }

    public int getOldWeiboNum() {
        return oldWeiboNum;
    }

    public void setOldWeiboNum(int oldWeiboNum) {
        this.oldWeiboNum = oldWeiboNum;
    }

    public int getNotMainNum() {
        return notMainNum;
    }

    public void setNotMainNum(int notMainNum) {
        this.notMainNum = notMainNum;
    }

    public int getOldFakeWeiboNum() {
        return oldFakeWeiboNum;
    }

    public void setOldFakeWeiboNum(int oldFakeWeiboNum) {
        this.oldFakeWeiboNum = oldFakeWeiboNum;
    }

    public int getUnValidUserNum() {
        return unValidUserNum;
    }

    public void setUnValidUserNum(int unValidUserNum) {
        this.unValidUserNum = unValidUserNum;
    }

    public int getRestartCount() {
        return restartCount;
    }

    public void setRestartCount(int restartCount) {
        this.restartCount = restartCount;
    }

    public int getNoResultCount() {
        return noResultCount;
    }

    public void setNoResultCount(int noResultCount) {
        this.noResultCount = noResultCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getUsedTime() {
        return usedTime;
    }

    public void setUsedTime(long usedTime) {
        this.usedTime = usedTime;
    }

    public long getFinishTs() {
        return finishTs;
    }

    public void setFinishTs(long finishTs) {
        this.finishTs = finishTs;
    }

    public ClearWeiboMsgLog() {
        super();
    }
    
    public ClearWeiboMsgLog(int accountType) {
        super();
        this.accountType = accountType;
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
                "(`accountType`,`deleteWeiboNum`,`remainWeiboNum`,`oldWeiboNum`,`notMainNum`," +
                "`oldFakeWeiboNum`,`unValidUserNum`,`restartCount`,`noResultCount`,`failCount`," +
                "`offset`,`usedTime`,`finishTs`) " +
                " values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        
        
        long id = dp.insert(true, insertSQL, 
                this.accountType, this.deleteWeiboNum, this.remainWeiboNum, this.oldWeiboNum, this.notMainNum,
                this.oldFakeWeiboNum, this.unValidUserNum, this.restartCount, this.noResultCount, this.failCount,
                this.offset, this.usedTime, this.finishTs);

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
                " `accountType` = ?, `deleteWeiboNum` = ?, `remainWeiboNum` = ?, `oldWeiboNum` = ?, `notMainNum` = ?, " +
                " `oldFakeWeiboNum` = ?, `unValidUserNum` = ?, `restartCount` = ?, `noResultCount` = ?, `failCount` = ?, " +
                " `offset` = ?, `usedTime` = ?, `finishTs` = ? " +
                " where id = ? ";
        
        
        long updateNum = dp.update(false, updateSQL, 
                this.accountType, this.deleteWeiboNum, this.remainWeiboNum, this.oldWeiboNum, this.notMainNum,
                this.oldFakeWeiboNum, this.unValidUserNum, this.restartCount, this.noResultCount, this.failCount,
                this.offset, this.usedTime, this.finishTs,
                this.id);

        if (updateNum >= 1) {
            //log.info("update ok for :" + this.getId());
            return true;
        } else {
            log.error("update failed...for :" + this.getId());
            return false;
        }
    }
    
    public static ClearWeiboMsgLog findLatestClearLog(int accountType) {
        
        String query = " select " + SelectAllProperty + " from " + TABLE_NAME + " " +
        		" where accountType = ? order by id desc limit 1 ";
        
        return new JDBCBuilder.JDBCExecutor<ClearWeiboMsgLog>(dp, query, accountType) {

            @Override
            public ClearWeiboMsgLog doWithResultSet(ResultSet rs)
                    throws SQLException {
                if (rs.next()) {
                    return parseClearWeiboMsgLog(rs);
                } else {
                    return null;
                }
            }
            
            
        }.call();
        
    }
    
    
    
    private static final String SelectAllProperty = " id,accountType,deleteWeiboNum,remainWeiboNum," +
    		    "oldWeiboNum,notMainNum," +
                "oldFakeWeiboNum,unValidUserNum,restartCount,noResultCount,failCount," +
                "offset,usedTime,finishTs ";
    
    
    private static ClearWeiboMsgLog parseClearWeiboMsgLog(ResultSet rs) {
        try {
            
            ClearWeiboMsgLog clearLog = new ClearWeiboMsgLog();
            
            clearLog.setId(rs.getLong(1));
            clearLog.setAccountType(rs.getInt(2));
            clearLog.setDeleteWeiboNum(rs.getInt(3));
            clearLog.setRemainWeiboNum(rs.getInt(4));
            clearLog.setOldWeiboNum(rs.getInt(5));
            clearLog.setNotMainNum(rs.getInt(6));
            clearLog.setOldFakeWeiboNum(rs.getInt(7));
            clearLog.setUnValidUserNum(rs.getInt(8));
            clearLog.setRestartCount(rs.getInt(9));
            clearLog.setNoResultCount(rs.getInt(10));
            clearLog.setFailCount(rs.getInt(11));
            clearLog.setOffset(rs.getLong(12));
            clearLog.setUsedTime(rs.getLong(13));
            clearLog.setFinishTs(rs.getLong(14));
            
            return clearLog;
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
    
    
}
