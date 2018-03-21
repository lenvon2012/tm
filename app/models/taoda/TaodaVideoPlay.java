package models.taoda;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = TaodaVideoPlay.TABLE_NAME)
public class TaodaVideoPlay extends GenericModel implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(TaodaVideoPlay.class);
    
    @Transient
    public static final String TABLE_NAME = "taoda_video_play";

    @Transient
    public static TaodaVideoPlay EMPTY = new TaodaVideoPlay();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    
    @PolicySQLGenerator.CodeNoUpdate
    @Id
    private Long videoId;
    
    private String title;
    
    @Index(name = "teacher")
    private String teacher;
    
    private int orderNums;//学习人数
    
    @Column(columnDefinition = "int default 0 ")
    private int status;
    
    
    private long createTs;
    
    private long updateTs;

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public int getOrderNums() {
        return orderNums;
    }

    public void setOrderNums(int orderNums) {
        this.orderNums = orderNums;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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
    
    public TaodaVideoPlay() {
        super();
    }
    
    public TaodaVideoPlay(Long videoId, String title, String teacher,
            int orderNums, int status) {
        super();
        this.videoId = videoId;
        this.title = title;
        this.teacher = teacher;
        this.orderNums = orderNums;
        this.status = status;
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
        return "videoId";
    }

    @Override
    public Long getId() {
        return videoId;
    }

    @Override
    public void setId(Long id) {
        this.videoId = id;
    }

    @Override
    public String getIdName() {
        return "videoId";
    }

    public static long findExistId(Long videoId) {
        
        String query = "select videoId from " + TABLE_NAME + " where videoId = ? ";
        
        return dp.singleLongQuery(query, videoId);
    }

    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.videoId);

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
                "(`videoId`,`title`,`teacher`,`orderNums`,`status`," +
                "`createTs`,`updateTs`) " +
                " values(?,?,?,?,?,?,?)";
        
        createTs = System.currentTimeMillis();
        updateTs = System.currentTimeMillis();
        
        long id = dp.insert(true, insertSQL, 
                this.videoId, this.title, this.teacher, this.orderNums, this.status,
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
                " `title` = ?, `teacher` = ?, `orderNums` = ?, `status` = ?, `updateTs` = ? " +
                " where `videoId` = ? ";
        
        updateTs = System.currentTimeMillis();
        
        long updateNum = dp.update(false, updateSQL, 
                this.title, this.teacher, this.orderNums, this.status, this.updateTs, 
                this.videoId);

        if (updateNum == 1) {
            //log.info("update ok for :" + this.getId());
            return true;
        } else {
            log.error("update failed...for :" + this.getId());
            return false;
        }
    }
    
    
}
