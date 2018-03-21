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

@Entity(name = CatTopWordSearchLog.TABLE_NAME)
public class CatTopWordSearchLog extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(CatTopWordSearchLog.class);
    
    @Transient
    public static final String TABLE_NAME = "cat_top_word_search_log";

    @Transient
    public static CatTopWordSearchLog EMPTY = new CatTopWordSearchLog();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    
    @Index(name = "cid")
    private Long cid;
    
    private String catName;
    
    @Index(name = "word")
    private String word;
    
    @Column(columnDefinition = "int default 0 ")
    private int status;
    
    public static class CatTopWordSearchStatus {
        public static final int FromTaobao = 1;
        public static final int Cached = 2;
        //public static final int NoItemReturnFromTaobao = 4;
    }
    
    private int totalItemNum;
    private int catItemNum;
    
    private int newItemNum;
    
    private int apiGetItemNum;
    
    //如果是FromTaobao，那么可能是api调用失败；如果是
    private int failGetItemNum;
    
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

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    
    public boolean isItemFromTaobao() {
        return (status & CatTopWordSearchStatus.FromTaobao) > 0;
    }

    public int getTotalItemNum() {
        return totalItemNum;
    }

    public void setTotalItemNum(int totalItemNum) {
        this.totalItemNum = totalItemNum;
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

    public int getApiGetItemNum() {
        return apiGetItemNum;
    }

    public void setApiGetItemNum(int apiGetItemNum) {
        this.apiGetItemNum = apiGetItemNum;
    }

    public int getFailGetItemNum() {
        return failGetItemNum;
    }

    public void setFailGetItemNum(int failGetItemNum) {
        this.failGetItemNum = failGetItemNum;
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

    public CatTopWordSearchLog() {
        super();
    }

    public CatTopWordSearchLog(Long cid, String catName, String word) {
        super();
        this.cid = cid;
        this.catName = catName;
        this.word = word;
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
                "(`cid`,`catName`,`word`,`status`,`totalItemNum`,`catItemNum`," +
                "`newItemNum`,`apiGetItemNum`,`failGetItemNum`," +
                "`usedTime`,`message`,`createTs`,`updateTs`) " +
                " values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        
        createTs = System.currentTimeMillis();
        updateTs = System.currentTimeMillis();
        
        long id = dp.insert(true, insertSQL, 
                this.cid, this.catName, this.word, this.status, this.totalItemNum, this.catItemNum, 
                this.newItemNum, this.apiGetItemNum, this.failGetItemNum,
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
                " `cid` = ?, `catName` = ?, `word` = ?, `status` = ?, `totalItemNum` = ?, `catItemNum` = ?, " +
                " `newItemNum` = ?, `apiGetItemNum` = ?, `failGetItemNum` = ?, " +
                " `usedTime` = ?, `message` = ?, `createTs` = ?, `updateTs` = ? " +
                " where `id` = ? ";
        
        updateTs = System.currentTimeMillis();
        
        long updateNum = dp.update(false, updateSQL, 
                this.cid, this.catName, this.word, this.status, this.totalItemNum, this.catItemNum, 
                this.newItemNum, this.apiGetItemNum, this.failGetItemNum,
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
        return "CatTopWordSearchLog [cid=" + cid + ", catName=" + catName
                + ", word=" + word + ", status=" + status + ", totalItemNum="
                + totalItemNum + ", catItemNum=" + catItemNum + ", newItemNum="
                + newItemNum + ", apiGetItemNum=" + apiGetItemNum
                + ", failGetItemNum=" + failGetItemNum + ", usedTime="
                + usedTime + ", message=" + message + ", createTs=" + createTs
                + ", updateTs=" + updateTs + "]";
    }

    

    
    
    
    
}
