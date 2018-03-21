package models.comment;

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

@Entity(name = CommentsFailed.TABLE_NAME)
public class CommentsFailed extends Model implements PolicySQLGenerator {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(CommentsFailed.class);

    @Transient
    public static final String TABLE_NAME = "comments_failed";

    @Transient
    static CommentsFailed EMPTY = new CommentsFailed();
    
    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);
    
    private Long tid;

    private Long oid;

    @Index(name = "userId")
    private Long userId;

    private String result;

    private String content;

    private String nick;

    private String buyer_nick;
    
    private String reason;

    @Column(columnDefinition = "bigint default 0")
    private long ts;

    public CommentsFailed() {
        super();
    }

    public CommentsFailed(Long userId, Long tid, Long oid, String result, String content,
            String nick, String buyer_nick, String reason) {
        this.userId = userId;
        this.oid = oid;
        this.tid = tid;
        this.result = result;
        this.content = content;
        this.nick = nick;
        this.buyer_nick = buyer_nick;
        this.reason = reason;
        this.ts = System.currentTimeMillis();
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setTId(Long tid) {
        this.tid = tid;
    }

    public Long getTId() {
        return this.tid;
    }

    public void setOId(Long oid) {
        this.oid = oid;
    }

    public Long getOId() {
        return this.oid;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResult() {
        return this.result;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getNick() {
        return this.nick;
    }

    public void setBuyerNick(String buyer_nick) {
        this.buyer_nick = buyer_nick;
    }

    public String getBuyerNick() {
        return this.buyer_nick;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return this.reason;
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

    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where userId = ? and tid = ? and oid =?";

    private static long findExistId(Long userId, Long tid, Long oid) {
        return dp.singleLongQuery(EXIST_ID_QUERY, userId, tid, oid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#jdbcSave()
     */
    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.userId, this.tid, this.oid);
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
                .insert("insert into `comments_failed`(`tid`,`oid`,`userId`,`result`,`content`,`nick`,`buyer_nick`,`ts`,`reason`) values(?,?,?,?,?,?,?,?,?)",
                        this.tid, this.oid, this.userId, this.result, this.content, this.nick, this.buyer_nick, this.ts, this.reason);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }

    }

    public boolean rawUpdate() {
        long updateNum = dp
                .insert("update `comments_failed` set  `tid` = ?, `oid` = ?,`userId` = ?,`result` = ?,`content` = ?, `nick` = ?, `buyer_nick` = ?, `reason` = ? where `id` = ? ",
                        this.tid, this.oid, this.userId, this.result, this.content, this.nick, this.buyer_nick, this.reason, this.id);

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

    @Override
    public void _save() {
        this.jdbcSave();
    }
}
