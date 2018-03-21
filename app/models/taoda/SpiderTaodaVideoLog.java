package models.taoda;

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

@Entity(name = SpiderTaodaVideoLog.TABLE_NAME)
public class SpiderTaodaVideoLog extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(SpiderTaodaVideoLog.class);
    
    @Transient
    public static final String TABLE_NAME = "spider_taoda_video_log";

    @Transient
    public static SpiderTaodaVideoLog EMPTY = new SpiderTaodaVideoLog();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    
    @Index(name = "videoId")
    private Long videoId;
    
    @Column(columnDefinition = "int default 0 ")
    private int pageIndex;
    
    @Column(columnDefinition = "int default 0 ")
    private int childNum;//可能是分页数，可以能每页视频数
    
    
    @Column(columnDefinition = "int default 0 ")
    private int status;
    
    public static class TaodaVideoSpiderStatus {
        public static final int NotKnown = 0;
        public static final int OnProcess = 1;
        public static final int Success = 2;
        public static final int Fail = 4;
    }
    
    @Column(columnDefinition = "int default 0 ")
    private int type;
    
    public static class TaodaVideoSpiderType {
        public static final int NotKnown = 0;
        public static final int FirstPage = 1;
        public static final int VideoList = 2;
        public static final int VideoPage = 4;
        public static final int OrderPage = 8;
    }
    
    private String pageLink;
    
    private String message;
    
    private long createTs;
    
    private long updateTs;

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getChildNum() {
        return childNum;
    }

    public void setChildNum(int childNum) {
        this.childNum = childNum;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPageLink() {
        return pageLink;
    }

    public void setPageLink(String pageLink) {
        this.pageLink = pageLink;
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

    public SpiderTaodaVideoLog() {
        super();
    }

    public SpiderTaodaVideoLog(Long videoId, int pageIndex, int childNum,
            int status, int type, String pageLink, String message) {
        super();
        this.videoId = videoId;
        this.pageIndex = pageIndex;
        this.childNum = childNum;
        this.status = status;
        this.type = type;
        this.pageLink = pageLink;
        this.message = message;
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
    public void setId(Long id) {
        this.id = id;
    }
    

    @Override
    public String getIdName() {
        return "id";
    }

    public static long findExistId(Long id) {
        
        String query = "select id from " + TABLE_NAME + " where id = ?";
        
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
                "(`videoId`,`pageIndex`,`childNum`,`status`,`type`,`pageLink`,`message`," +
                "`createTs`,`updateTs`) " +
                " values(?,?,?,?,?,?,?,?,?)";
        
        createTs = System.currentTimeMillis();
        updateTs = System.currentTimeMillis();
        
        long id = dp.insert(true, insertSQL, 
                this.videoId, this.pageIndex, this.childNum, this.status, this.type, this.pageLink, this.message,
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
                " `videoId` = ?, `pageIndex` = ?, `childNum` = ?, `status` = ?, `type` = ?, " +
                " `pageLink` = ?, `message` = ?, `updateTs` = ? " +
                " where `id` = ? ";
        
        updateTs = System.currentTimeMillis();
        
        long updateNum = dp.update(false, updateSQL, 
                this.videoId, this.pageIndex, this.childNum, this.status, this.type, 
                this.pageLink, this.message, this.updateTs, 
                this.id);

        if (updateNum == 1) {
            //log.info("update ok for :" + this.getId());
            return true;
        } else {
            log.error("update failed...for :" + this.getId());
            return false;
        }
    }

    
    
    
    
}
