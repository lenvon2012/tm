package models.fenxiao;

import java.sql.ResultSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = RemoveDescLinkLog.TABLE_NAME)
@JsonIgnoreProperties(value = { "entityId", "tableHashKey", "persistent", "tableName", "idName", "idColumn",
        "propsName" })
public class RemoveDescLinkLog extends Model implements PolicySQLGenerator {

    private static final Logger log = LoggerFactory.getLogger(RemoveDescLinkLog.class);

    public static final String TABLE_NAME = "remove_desc_link_log";
    
    @Transient
    public static RemoveDescLinkLog _instance = new RemoveDescLinkLog();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, _instance);

    
    @Index(name = "userId")
    private Long userId;
    
    @Index(name = "numIid")
    private Long numIid;
    
    @Lob
    private String originDesc;
    
    @Lob
    private String removedLinks;

    @Column(columnDefinition = "int default 0")
    private int status;
    
    public static class RemoveDescLinkLogStatus {
        public static final int NewCreate = 1;
        public static final int Recovered = 2;
    }
    
    private long createTs;
    
    private long recoverTs;//恢复时间

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getNumIid() {
        return numIid;
    }

    public void setNumIid(Long numIid) {
        this.numIid = numIid;
    }

    public String getOriginDesc() {
        return originDesc;
    }

    public void setOriginDesc(String originDesc) {
        this.originDesc = originDesc;
    }

    public String getRemovedLinks() {
        return removedLinks;
    }

    public void setRemovedLinks(String removedLinks) {
        this.removedLinks = removedLinks;
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

    public long getRecoverTs() {
        return recoverTs;
    }

    public void setRecoverTs(long recoverTs) {
        this.recoverTs = recoverTs;
    }

    public RemoveDescLinkLog() {
        super();
    }

    public RemoveDescLinkLog(Long userId, Long numIid, String originDesc,
            String removedLinks, int status) {
        super();
        this.userId = userId;
        this.numIid = numIid;
        this.originDesc = originDesc;
        this.removedLinks = removedLinks;
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
    public String getIdName() {
        return "id";
    }

    @Override
    public String getIdColumn() {
        return "id";
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public static long findExistId(Long id) {
        String query = " select id from " + TABLE_NAME + " where id = ? ";
        return dp.singleLongQuery(query, id);
    }

    @Override
    public boolean jdbcSave() {

        try {
            long existId = findExistId(this.id);

            if (existId <= 0L) {
                return rawInsert();
            } else {
                this.setId(existId);
                return rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

    }

    public boolean rawInsert() {
        
        String insertSQL = "insert into `" + TABLE_NAME + "`(`userId`," +
                "`numIid`,`originDesc`,`removedLinks`,`status`," +
                "`createTs`,`recoverTs`) " +
                " values(?,?,?,?,?,?,?)";
        
        this.createTs = System.currentTimeMillis();
        
        
        long id = dp.insert(insertSQL, this.userId,
                this.numIid, this.originDesc, this.removedLinks, this.status,
                this.createTs, this.recoverTs);

        if (id >= 0L) {
            this.setId(id);
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }

    }

    public boolean rawUpdate() {

        String updateSQL = "update `" + TABLE_NAME + "` set  `userId` = ?, " +
                "`numIid` = ?, `originDesc` = ?, `removedLinks` = ?, `status` = ?, `recoverTs` = ? " +
                " where `id` = ? ";

        
        long updateNum = dp.update(updateSQL, this.userId,
                this.numIid, this.originDesc, this.removedLinks, this.status, this.recoverTs,
                this.getId());

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.getId() + "[userId : ]" + this.userId);
            return false;
        }
    }
    
    private static final String SelectAllProperty = " id,userId," +
                "numIid,originDesc,removedLinks,status," +
                "createTs,recoverTs ";

    private static RemoveDescLinkLog parseRemoveDescLinkLog(ResultSet rs) {
        
        try {
            
            RemoveDescLinkLog removeLog = new RemoveDescLinkLog();
            
            removeLog.setId(rs.getLong(1));
            removeLog.setUserId(rs.getLong(2));
            removeLog.setNumIid(rs.getLong(3));
            removeLog.setOriginDesc(rs.getString(4));
            removeLog.setRemovedLinks(rs.getString(5));
            removeLog.setStatus(rs.getInt(6));
            removeLog.setCreateTs(rs.getLong(7));
            removeLog.setRecoverTs(rs.getLong(8));
            
            return removeLog;
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
        
    }
    
}
