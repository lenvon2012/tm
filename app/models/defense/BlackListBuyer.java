package models.defense;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = BlackListBuyer.TABLE_NAME)
public class BlackListBuyer extends Model implements PolicySQLGenerator {

    public BlackListBuyer() {
        super();
    }

    @Transient
    public static BlackListBuyer EMPTY = new BlackListBuyer();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, EMPTY);

    @Transient
    private static final Logger log = LoggerFactory.getLogger(BlackListBuyer.class);

    @Transient
    public static final String TABLE_NAME = "blacklist_buyer";

    public static class RemarkMsg {
        public static final String AUTO_ADD_BLACKLIST_CHAPING = "[黑名单设置]差评用户直接加入黑名单";

        public static final String AUTO_ADD_BLACKLIST_REFUND = "[黑名单设置]退款用户直接加入黑名单";
    }

    @Index(name = "userId")
    private Long userId;

    @Index(name = "buyerName")
    private String buyerName;

    private Long ts;

    private String remark;// 备注

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public BlackListBuyer(Long userId, String buyerName, Long ts, String remark) {
        super();
        this.userId = userId;
        this.buyerName = buyerName;
        this.ts = ts;
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "BlackListBuyer [userId=" + userId + ", buyerName=" + buyerName + ", ts=" + ts + ", remark=" + remark
                + "]";
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

    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where userId = ? and buyerName=? ";

    private static long findExistId(Long userId, String buyerName) {
        return dp.singleLongQuery(EXIST_ID_QUERY, userId, buyerName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#jdbcSave()
     */
    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.userId, this.buyerName);
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
        long id = dp.insert("insert into `blacklist_buyer`(`userId`,`buyerName`,`ts`,`remark`) values(?,?,?,?)",
                this.userId, this.buyerName, this.ts, this.remark);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }

    }

    public boolean rawUpdate() {
        long updateNum = dp.insert(
                "update `blacklist_buyer` set  `userId` = ?, `buyerName` = ?, `ts` = ?, `remark` = ? where `id` = ? ",
                this.userId, this.buyerName, this.ts, this.remark, this.id);

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
