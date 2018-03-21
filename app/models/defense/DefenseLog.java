package models.defense;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = DefenseLog.TABLE_NAME)
public class DefenseLog extends Model implements PolicySQLGenerator {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(DefenseLog.class);

    @Transient
    public static final String TABLE_NAME = "defense_log";

    @Transient
    public static final DefenseLog EMPTY = new DefenseLog();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, EMPTY);

    public static class DefenseLogStatus {
        public static final int OpSuccess = 1;
        public static final int OpError = 2;
        public static final int ItemPass = 4;
        public static final int WhiteListAllow = 8;
        public static final int BlackListBlock = 16;
        public static final int RuleBlock = 32;
        public static final int BuyLimitBlock = 64;
    }

    @Index(name = "userId")
    private Long userId;

    @Index(name = "tradeId")
    private Long tradeId;

    @Index(name = "numIid")
    private Long numIid;

    private String buyerName;

    private String opMsg;
    
    private String closeFailReason = StringUtils.EMPTY;

    private int status;

    private Long ts;

    public DefenseLog() {

    }

    public DefenseLog(Long userId, Long tradeId, Long numIid, String buyerName, 
    		String opMsg, String closeFailReason, int status) {
        super();
        this.userId = userId;
        this.tradeId = tradeId;
        this.numIid = numIid;
        this.buyerName = buyerName;
        this.opMsg = opMsg;
        this.closeFailReason = closeFailReason;
        this.status = status;
        this.ts = System.currentTimeMillis();
    }

    public DefenseLog(Long userId, Long tradeId, Long numIid, String buyerName, String opMsg,
    		String closeFailReason, int status, Long ts) {
        super();
        this.userId = userId;
        this.tradeId = tradeId;
        this.numIid = numIid;
        this.buyerName = buyerName;
        this.opMsg = opMsg;
        this.closeFailReason = closeFailReason;
        this.status = status;
        this.ts = ts;
    }
    
    public DefenseLog(Long userId, Long tradeId, Long numIid, String buyerName, 
    		String opMsg, int status) {
        super();
        this.userId = userId;
        this.tradeId = tradeId;
        this.numIid = numIid;
        this.buyerName = buyerName;
        this.opMsg = opMsg;
        this.closeFailReason = StringUtils.EMPTY;
        this.status = status;
        this.ts = System.currentTimeMillis();
    }

    public DefenseLog(Long userId, Long tradeId, Long numIid, String buyerName, String opMsg,
    		int status, Long ts) {
        super();
        this.userId = userId;
        this.tradeId = tradeId;
        this.numIid = numIid;
        this.buyerName = buyerName;
        this.opMsg = opMsg;
        this.closeFailReason = StringUtils.EMPTY;
        this.status = status;
        this.ts = ts;
    }

    public DefenseLog(ResultSet rs) throws SQLException {
        this.userId = rs.getLong(1);
        this.tradeId = rs.getLong(2);
        this.numIid = rs.getLong(3);
        this.buyerName = rs.getString(4);
        this.opMsg = rs.getString(5);
        this.status = rs.getInt(6);
        this.ts = rs.getLong(7);
        this.closeFailReason = rs.getString(8);
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCloseFailReason() {
		return closeFailReason;
	}

	public void setCloseFailReason(String closeFailReason) {
		this.closeFailReason = closeFailReason;
	}

	public boolean isOperateSuccess() {
        return (this.status & DefenseLogStatus.OpSuccess) > 0;
    }

    public void setOperateSuccess(boolean isSuccess) {
        if (isSuccess) {
            this.status |= DefenseLogStatus.OpSuccess;
        } else {
            this.status &= (~DefenseLogStatus.OpSuccess);
        }
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Long getNumIid() {
        return numIid;
    }

    public void setNumIid(Long numIid) {
        this.numIid = numIid;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public String getOpMsg() {
        return opMsg;
    }

    public void setOpMsg(String opMsg) {
        this.opMsg = opMsg;
    }

    public Long getTradeId() {
        return tradeId;
    }

    public void setTradeId(Long tradeId) {
        this.tradeId = tradeId;
    }

    @Override
    public String toString() {
        return "DefenseLog [userId=" + userId + ", tradeId=" + tradeId + ", numIid=" + numIid + ", buyerName="
                + buyerName + ", opMsg=" + opMsg + ", status=" + status + ", closeFailReason=" 
                + closeFailReason + " ts=" + ts + "]";
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

    // static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where userId = ? ";

    private static long findExistId(Long userId) {
        // return dp.singleLongQuery(EXIST_ID_QUERY, userId);
        return 0L;
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#jdbcSave()
     */
    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.userId);
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
        long id = dp
                .insert("insert into `defense_log`(`userId`,`tradeId`,`numIid`,`buyerName`,`opMsg`,`closeFailReason`,`status`,`ts`) values(?,?,?,?,?,?,?,?)",
                        this.userId, this.tradeId, this.numIid, this.buyerName, this.opMsg, this.closeFailReason, this.status, this.ts);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }

    }

    public boolean rawUpdate() {
        long updateNum = dp
                .insert("update `defense_log` set  `userId` = ?, `tradeId` = ?, `numIid` = ?, `buyerName` = ?, `opMsg` = ?, `closeFailReason` = ?, `status` = ?, `ts` = ? where `id` = ? ",
                        this.userId, this.tradeId, this.numIid, this.buyerName, this.opMsg, this.closeFailReason, this.status, this.ts,
                        this.getId());

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
