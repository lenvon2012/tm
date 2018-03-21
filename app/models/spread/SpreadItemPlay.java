package models.spread;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = SpreadItemPlay.TABLE_NAME)
public class SpreadItemPlay extends GenericModel implements PolicySQLGenerator {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(SpreadItemPlay.class);

    @Transient
    public static final String TABLE_NAME = "spread_item_play1";

    @Index(name = "userId")
    private Long userId;

    @Id
    @PolicySQLGenerator.CodeNoUpdate
    private Long numIid;

    private String spreadUrl;
    
    public static class SpreadStatus {
        public static final int ON = 1;
        public static final int OFF = 2;//暂停，取消则是直接删除记录的
    }
    
    private int spreadStatus = 0;
    
    
    public static class SpreadLevelType {
        public static final int Level1 = 1;
        public static final int Level2 = 2;
        public static final int Level3 = 4;
        public static final int Level4 = 8;
        public static final int Level5 = 16;
        public static final int Level6 = 32;
    }
    
    private int spreadLevel = 0;
    
    private long createTs = 0L;

    private long updateTs = 0L;
    
    
    
    
    @Transient
    private String picURL;
    
    @Transient
    private String title;
    
    @Transient
    private double price;
    
    @Transient
    private int salesCount;
    
    

    public long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public long getNumIid() {
        return numIid;
    }

    public void setNumIid(Long numIid) {
        this.numIid = numIid;
    }

    

    

    public String getSpreadUrl() {
        return spreadUrl;
    }

    public void setSpreadUrl(String spreadUrl) {
        this.spreadUrl = spreadUrl;
    }

    public int getSpreadStatus() {
        return spreadStatus;
    }

    public void setSpreadStatus(int spreadStatus) {
        this.spreadStatus = spreadStatus;
    }

    public int getSpreadLevel() {
        return spreadLevel;
    }

    public void setSpreadLevel(int spreadLevel) {
        this.spreadLevel = spreadLevel;
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

    public String getPicURL() {
        return picURL;
    }

    public void setPicURL(String picURL) {
        this.picURL = picURL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(int salesCount) {
        this.salesCount = salesCount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getIdColumn()
     */
    @Override
    public String getIdColumn() {
        // TODO Auto-generated method stub
        return "numIid";
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getIdName()
     */
    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "numIid";
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getTableHashKey()
     */
    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getTableName()
     */
    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
    }

    static String EXIST_ID_QUERY = "select numIid from " + TABLE_NAME + " where  numIid = ? ";

    public static long findExistId(Long numIid) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, numIid);
    }

    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.numIid);

            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                setId(existdId);
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }

    public boolean rawInsert() {
        createTs = System.currentTimeMillis();
        updateTs = createTs;
        // TODO Auto-generated method stub
        long id = JDBCBuilder.insert(
                "insert into `" + TABLE_NAME + "`(`numIid`,`userId`,`spreadUrl`,`spreadStatus`,`spreadLevel`,`createTs`,`updateTs`) values(?,?,?,?,?,?,?)", this.numIid,
                this.userId, this.spreadUrl, this.spreadStatus, this.spreadLevel, this.createTs, this.updateTs);

        if (id > 0L) {
            log.info("Insert success....." + "[Id : ]" + this.numIid);
            return true;
        } else {
            log.error("Insert Fails....." + "[Id : ]" + this.numIid);

            return false;
        }
    }

    public boolean rawUpdate() {
        updateTs = System.currentTimeMillis();
        long updateNum = JDBCBuilder.insert(
                "update `" + TABLE_NAME + "` set  `userId` = ?, `spreadUrl` = ?, `spreadStatus` = ?, `spreadLevel` = ?, `createTs` = ?, `updateTs` = ? where `numIid` = ? ",
                this.userId, this.spreadUrl, this.spreadStatus, this.spreadLevel, this.createTs, this.updateTs, this.numIid);

        if (updateNum > 0L) {
            log.info("update success....." + "[Id : ]" + this.numIid);
            return true;
        } else {
            log.error("update Fails....." + "[Id : ]" + this.numIid);

            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#setId(java.lang.Long)
     */
    @Override
    public void setId(Long id) {
        // TODO Auto-generated method stub
        this.numIid = id;
    }

    @Override
    public Long getId() {
        return numIid;
    }

}
