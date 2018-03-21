package models.hotitem;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = CatHotItemUpdateLog.TABLE_NAME)
public class CatHotItemUpdateLog extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(CatHotItemUpdateLog.class);
    
    @Transient
    public static final String TABLE_NAME = "cat_hot_item_update_log";

    @Transient
    public static CatHotItemUpdateLog EMPTY = new CatHotItemUpdateLog();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    
    @Index(name = "cid")
    private Long cid;
    
    private String catName;
    
    @Column(columnDefinition = "int default 0 ")
    private int status;
    
    public static class CatHotUpdateStatus {
        public static final int Success = 1;
        public static final int NoSpiderWord = 2;
        public static final int NoItemReturn = 4;
    }

    private int totalWordNum;
    
    private int cachedWordNum;//
    
    private int failWordNum;
    
    private int totalItemNum;//包括重复的，且不是本类目的
    
    private int cachedItemNum;//包括重复的，且不是本类目的
    
    private int failItemNum;
    
    private int catItemNum;//不包括重复的，且是本类目的
    
    private int newItemNum;
    
    private int deleteItemNum;
    
    private long usedTime;
    
    private String message;
    
    private long createTs;
    
    private long updateTs;

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public String getCatName() {
        return catName;
    }

    public void setCatName(String catName) {
        this.catName = catName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTotalWordNum() {
        return totalWordNum;
    }

    public void setTotalWordNum(int totalWordNum) {
        this.totalWordNum = totalWordNum;
    }

    public int getCachedWordNum() {
        return cachedWordNum;
    }

    public void setCachedWordNum(int cachedWordNum) {
        this.cachedWordNum = cachedWordNum;
    }

    public int getFailWordNum() {
        return failWordNum;
    }

    public void setFailWordNum(int failWordNum) {
        this.failWordNum = failWordNum;
    }

    public int getTotalItemNum() {
        return totalItemNum;
    }

    public void setTotalItemNum(int totalItemNum) {
        this.totalItemNum = totalItemNum;
    }

    public int getCachedItemNum() {
        return cachedItemNum;
    }

    public void setCachedItemNum(int cachedItemNum) {
        this.cachedItemNum = cachedItemNum;
    }

    public int getFailItemNum() {
        return failItemNum;
    }

    public void setFailItemNum(int failItemNum) {
        this.failItemNum = failItemNum;
    }

    public int getCatItemNum() {
        return catItemNum;
    }

    public void setCatItemNum(int catItemNum) {
        this.catItemNum = catItemNum;
    }

    public int getNewItemNum() {
        return newItemNum;
    }

    public void setNewItemNum(int newItemNum) {
        this.newItemNum = newItemNum;
    }

    public int getDeleteItemNum() {
        return deleteItemNum;
    }

    public void setDeleteItemNum(int deleteItemNum) {
        this.deleteItemNum = deleteItemNum;
    }

    public long getUsedTime() {
        return usedTime;
    }

    public void setUsedTime(long usedTime) {
        this.usedTime = usedTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public CatHotItemUpdateLog() {
        super();
    }

    
    
    public CatHotItemUpdateLog(Long cid, String catName) {
        super();
        this.cid = cid;
        this.catName = catName;
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
                "(`cid`,`catName`,`status`,`totalWordNum`,`cachedWordNum`,`failWordNum`," +
                "`totalItemNum`,`cachedItemNum`,`failItemNum`," +
                "`catItemNum`,`newItemNum`,`deleteItemNum`," +
                "`usedTime`,`message`,`createTs`,`updateTs`) " +
                " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        
        createTs = System.currentTimeMillis();
        updateTs = System.currentTimeMillis();
        
        long id = dp.insert(true, insertSQL, 
                this.cid, this.catName, this.status, this.totalWordNum, this.cachedWordNum, this.failWordNum,
                this.totalItemNum, this.cachedItemNum, this.failItemNum, 
                this.catItemNum, this.newItemNum, this.deleteItemNum,
                this.usedTime, this.message, this.createTs, this.updateTs);

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
                " `cid` = ?, `catName` = ?, `status` = ?, `totalWordNum` = ?, `cachedWordNum` = ?, `failWordNum` = ?, " +
                " `totalItemNum` = ?, `cachedItemNum` = ?, `failItemNum` = ?, " +
                " `catItemNum` = ?, `newItemNum` = ?, `deleteItemNum` = ?, " +
                " `usedTime` = ?, `message` = ?, `createTs` = ?, `updateTs` = ? " +
                " where `id` = ? ";
        
        updateTs = System.currentTimeMillis();
        
        long updateNum = dp.update(false, updateSQL, 
                this.cid, this.catName, this.status, this.totalWordNum, this.cachedWordNum, this.failWordNum,
                this.totalItemNum, this.cachedItemNum, this.failItemNum, 
                this.catItemNum, this.newItemNum, this.deleteItemNum,
                this.usedTime, this.message, this.createTs, this.updateTs,
                this.id);

        if (updateNum == 1) {
            log.info("update ok for :" + this.getId());
            return true;
        } else {
            log.error("update failed...for :" + this.getId());
            return false;
        }
    }

    @Override
    public String toString() {
        return "CatHotItemUpdateLog [cid=" + cid + ", catName=" + catName
                + ", status=" + status + ", totalWordNum=" + totalWordNum
                + ", cachedWordNum=" + cachedWordNum + ", failWordNum="
                + failWordNum + ", totalItemNum=" + totalItemNum
                + ", cachedItemNum=" + cachedItemNum + ", failItemNum="
                + failItemNum + ", catItemNum=" + catItemNum + ", newItemNum="
                + newItemNum + ", deleteItemNum=" + deleteItemNum
                + ", usedTime=" + usedTime + ", message=" + message
                + ", createTs=" + createTs + ", updateTs=" + updateTs + "]";
    }

    

    
    
    
    
}
