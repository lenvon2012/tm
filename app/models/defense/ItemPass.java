package models.defense;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = ItemPass.TABLE_NAME)
public class ItemPass extends Model implements PolicySQLGenerator {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(ItemPass.class);

    @Transient
    public static final String TABLE_NAME = "item_pass";

    @Transient
    public static final ItemPass EMPTY = new ItemPass();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, EMPTY);

    public static class ItemPassStatus {
        public static int NOT_PASS = 0;
        public static int PASS_ALL = 1;
        public static int BLOCK_BLACKLIST = 2;
    }

    @Index(name = "userId")
    private Long userId;

    @Index(name = "numIid")
    private Long numIid;

    // 1 表示黑名单也不拦截；2 表示黑名单也要拦截
    private int status;

    private Long ts;

    public ItemPass() {

    }

    public ItemPass(Long userId, Long numIid, int status) {
        super();
        this.userId = userId;
        this.numIid = numIid;
        this.status = status;
        this.ts = System.currentTimeMillis();
    }

    public ItemPass(ResultSet rs) throws SQLException {
        this.userId = rs.getLong(1);
        this.numIid = rs.getLong(2);
        this.status = rs.getInt(3);
        this.ts = rs.getLong(4);
    }

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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    @Override
    public String toString() {
        return "ItemPass [userId=" + userId + ", numIid=" + numIid + ", status=" + status + ", ts=" + ts + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getId()
     */
    @Override
    public Long getId() {
        // TODO Auto-generated method stub
        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getIdColumn()
     */
    @Override
    public String getIdColumn() {
        // TODO Auto-generated method stub
        return "id";
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#getIdName()
     */
    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "id";
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

    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where userId = ? and numIid = ? ";

    private static long findExistId(Long userId, Long numIid) {
        return dp.singleLongQuery(EXIST_ID_QUERY, userId, numIid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#jdbcSave()
     */
    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.userId, this.numIid);
            // if (existdId != 0)
            // log.info("find existed Id: " + existdId);

            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                id = existdId;
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

    }

    public boolean rawInsert() {
        long id = dp.insert("insert into `item_pass`(`userId`,`numIid`,`status`,`ts`) values(?,?,?,?)", this.userId,
                this.numIid, this.status, this.ts);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }
    }

    public boolean rawUpdate() {
        long updateNum = dp.insert(
                "update `item_pass` set  `userId` = ?, `numIid` = ?, `status` = ?, `ts` = ? where `id` = ? ",
                this.userId, this.numIid, this.status, this.ts, this.getId());

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.id + "[userId : ]" + this.userId);

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
        this.id = id;
    }

}
