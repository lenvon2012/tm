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

@Entity(name = SyncWeiboMsgLog.TABLE_NAME)
public class SyncWeiboMsgLog extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(SyncWeiboMsgLog.class);
    
    @Transient
    public static final String TABLE_NAME = "sync_weibo_msg_log";

    @Transient
    public static SyncWeiboMsgLog EMPTY = new SyncWeiboMsgLog();

    @Transient
    private static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    @Column(columnDefinition = "int default 0 ")
    private int newWeiboNum;
    
    @Column(columnDefinition = "int default 0 ")
    private int deleteWeiboNum;
    
    @Column(columnDefinition = "int default 0 ")
    private int validAccountNum;
    
    @Column(columnDefinition = "int default 0 ")
    private int unBindNum;
    
    @Column(columnDefinition = "int default 0 ")
    private int outDateNum;
    
    @Column(columnDefinition = "int default 0 ")
    private int userNum;
    
    @Column(columnDefinition = "int default 0 ")
    private int noResultCount;
    
    @Column(columnDefinition = "int default 0 ")
    private int failCount;
    
    @Column(columnDefinition = "int default 0 ")
    private int restartCount;
    
    private long offset;
    
    private long usedTime;
    
    private long finishTs;

    public int getNewWeiboNum() {
        return newWeiboNum;
    }

    public void setNewWeiboNum(int newWeiboNum) {
        this.newWeiboNum = newWeiboNum;
    }

    public int getDeleteWeiboNum() {
        return deleteWeiboNum;
    }

    public void setDeleteWeiboNum(int deleteWeiboNum) {
        this.deleteWeiboNum = deleteWeiboNum;
    }

    public int getValidAccountNum() {
        return validAccountNum;
    }

    public void setValidAccountNum(int validAccountNum) {
        this.validAccountNum = validAccountNum;
    }

    public int getUnBindNum() {
        return unBindNum;
    }

    public void setUnBindNum(int unBindNum) {
        this.unBindNum = unBindNum;
    }

    public int getOutDateNum() {
        return outDateNum;
    }

    public void setOutDateNum(int outDateNum) {
        this.outDateNum = outDateNum;
    }

    public int getUserNum() {
        return userNum;
    }

    public void setUserNum(int userNum) {
        this.userNum = userNum;
    }

    public long getUsedTime() {
        return usedTime;
    }

    public void setUsedTime(long usedTime) {
        this.usedTime = usedTime;
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

    public int getRestartCount() {
        return restartCount;
    }

    public void setRestartCount(int restartCount) {
        this.restartCount = restartCount;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getFinishTs() {
        return finishTs;
    }

    public void setFinishTs(long finishTs) {
        this.finishTs = finishTs;
    }

    public SyncWeiboMsgLog() {
        super();
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
                "(`newWeiboNum`,`deleteWeiboNum`,`validAccountNum`,`unBindNum`,`outDateNum`," +
                "`userNum`,`noResultCount`,`failCount`,`restartCount`,`offset`," +
                "`usedTime`,`finishTs`) " +
                " values(?,?,?,?,?,?,?,?,?,?,?,?)";
        
        
        long id = dp.insert(true, insertSQL, 
                this.newWeiboNum, this.deleteWeiboNum, this.validAccountNum, this.unBindNum, this.outDateNum,
                this.userNum, this.noResultCount, this.failCount, this.restartCount, this.offset,
                this.usedTime, this.finishTs);

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
                " `newWeiboNum` = ?, `deleteWeiboNum` = ?, `validAccountNum` = ?, `unBindNum` = ?, `outDateNum` = ?, " +
                " `userNum` = ?, `noResultCount` = ?, `failCount` = ?, `restartCount` = ?, `offset` = ?, " +
                " `usedTime` = ?, `finishTs` = ? " +
                " where id = ? ";
        
        
        long updateNum = dp.update(false, updateSQL, 
                this.newWeiboNum, this.deleteWeiboNum, this.validAccountNum, this.unBindNum, this.outDateNum,
                this.userNum, this.noResultCount, this.failCount, this.restartCount, this.offset,
                this.usedTime, this.finishTs,
                this.id);

        if (updateNum >= 1) {
            //log.info("update ok for :" + this.getId());
            return true;
        } else {
            log.error("update failed...for :" + this.getId());
            return false;
        }
    }
    
    
    public static SyncWeiboMsgLog findLatestSyncLog() {
        
        String query = " select " + SelectAllProperty + " from " + TABLE_NAME + " order by id desc limit 1 ";
        
        return new JDBCBuilder.JDBCExecutor<SyncWeiboMsgLog>(dp, query) {

            @Override
            public SyncWeiboMsgLog doWithResultSet(ResultSet rs)
                    throws SQLException {
                if (rs.next()) {
                    return parseSyncWeiboMsgLog(rs);
                } else {
                    return null;
                }
            }
            
            
        }.call();
        
    }
    
    
    private static final String SelectAllProperty = " id,newWeiboNum,deleteWeiboNum,validAccountNum," +
    		    "unBindNum,outDateNum," +
                "userNum,noResultCount,failCount,restartCount,offset," +
                "usedTime,finishTs ";
    
    
    private static SyncWeiboMsgLog parseSyncWeiboMsgLog(ResultSet rs) {
        try {
            SyncWeiboMsgLog syncLog = new SyncWeiboMsgLog();
            
            syncLog.setId(rs.getLong(1));
            syncLog.setNewWeiboNum(rs.getInt(2));
            syncLog.setDeleteWeiboNum(rs.getInt(3));
            syncLog.setValidAccountNum(rs.getInt(4));
            syncLog.setUnBindNum(rs.getInt(5));
            syncLog.setOutDateNum(rs.getInt(6));
            syncLog.setUserNum(rs.getInt(7));
            syncLog.setNoResultCount(rs.getInt(8));
            syncLog.setFailCount(rs.getInt(9));
            syncLog.setRestartCount(rs.getInt(10));
            syncLog.setOffset(rs.getLong(11));
            syncLog.setUsedTime(rs.getLong(12));
            syncLog.setFinishTs(rs.getLong(13));
            
            return syncLog;
            
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
    
}
