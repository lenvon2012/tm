
package models.comment;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;



@Entity(name = UserTradeCommentLog.TABLE_NAME)
public class UserTradeCommentLog extends Model implements PolicySQLGenerator {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(UserTradeCommentLog.class);

    public static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    @Transient
    static UserTradeCommentLog EMPTY = new UserTradeCommentLog();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    @Transient
    public static final String TABLE_NAME = "user_trade_comment_log";

    @Index(name = "userId")
    private Long userId;

    private String nick;
    
    private String ts;
    
    private String jobTs;
    
    private int orderCount = 0;
    
    private int unCommentedOrderCount = 0;
    
    private int cannotrateCount = 0;

    private int successCount = 0;
    
    private int failCount = 0;
    
    private String failOrderIds;

    public UserTradeCommentLog() {
        super();
    }

    public UserTradeCommentLog(Long userId, String nick, String jobTs, int orderCount, int unCommentedOrderCount, int cannotrateCount, int successCount, int failCount, String failOrderIds) {
        this.userId = userId;
        this.nick = nick;
        this.jobTs = jobTs;
        this.orderCount = orderCount;
        this.unCommentedOrderCount = unCommentedOrderCount;
        this.cannotrateCount = cannotrateCount;
        this.successCount = successCount;
        this.failCount = failCount;
        this.failOrderIds = failOrderIds;
        this.ts = df.format(new Date());
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public String getJobTs() {
        return this.jobTs;
    }

    public void setJobTs(String jobTs) {
        this.jobTs = jobTs;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }
    
    public int getUnCommentedOrderCount() {
        return unCommentedOrderCount;
    }

    public void setUnCommentedOrderCount(int unCommentedOrderCount) {
        this.unCommentedOrderCount = unCommentedOrderCount;
    }

    public int getCannotrateCount() {
        return cannotrateCount;
    }

    public void setCannotrateCount(int cannotrateCount) {
        this.cannotrateCount = cannotrateCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public String getFailOrderIds() {
        return failOrderIds;
    }

    public void setFailOrderIds(String failOrderIds) {
        this.failOrderIds = failOrderIds;
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

    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where userId = ? and jobTs = ?";

    private static long findExistId(Long userId, String jobTs) {
        return dp.singleLongQuery(EXIST_ID_QUERY, userId, jobTs);
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#jdbcSave()
     */
    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.userId, this.jobTs);
//            if (existdId != 0)
//                log.info("find existed Id: " + existdId);

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
                .insert(
                        "insert into `user_trade_comment_log`(`userId`,`nick`,`jobTs`,`orderCount`,`unCommentedOrderCount`,`cannotrateCount`,`successCount`,`failCount`,`ts`,`failOrderIds`) values(?,?,?,?,?,?,?,?,?,?)",
                        this.userId, this.nick, this.jobTs, this.orderCount, this.unCommentedOrderCount, this.cannotrateCount, this.successCount, this.failCount, this.ts, this.failOrderIds);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert into user_trade_comment_log Fails....." + "[userId : ]" + this.userId + " and [jobTs : ] " + this.jobTs);
            return false;
        }
    }

    public boolean rawUpdate() {
        long updateNum = dp
                .insert(
                        "update `user_trade_comment_log` set  `userId` = ?, `nick` = ?, `jobTs` = ?, `orderCount` = ?, `unCommentedOrderCount` = ?, `cannotrateCount` = ?, `successCount` = ?, `failCount` = ?, `ts` = ?, `failOrderIds` = ? where `id` = ? ",
                        this.userId, this.nick, this.jobTs, this.orderCount, this.unCommentedOrderCount, this.cannotrateCount, this.successCount, this.failCount, this.ts, this.failOrderIds, this.id);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update user_trade_comment_log failed...for :" + this.id + "[userId : ]" + this.userId+ " and [jobTs : ] " + this.jobTs);

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
