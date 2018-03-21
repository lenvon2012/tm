
package models.comment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

@JsonIgnoreProperties(value = {
        "tableHashKey", "persistent", "tableName", "idName", "idColumn", "entityId"
})
@Entity(name = CommentConf.TABLE_NAME)
public class CommentConf extends Model implements PolicySQLGenerator {
    @Transient
    private static final Logger log = LoggerFactory.getLogger(CommentConf.class);

    @Transient
    public static final String TABLE_NAME = "comment_conf";

    @Transient
    static CommentConf EMPTY = new CommentConf();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    @Transient
    public static String DEAFULT_BADCOMMENT_BUYER_SMS = "亲爱的#买家#，很抱歉打扰到您，亲对我们店里购买的宝贝有任何不满意，请联系我们旺旺[#卖家#]，我们会热情为您服务；满意请给5分好评哦~谢谢。";

    @Index(name = "userId")
    private Long userId;

    private String nick;

    // 评价内容
    @Column(columnDefinition = "varchar(1023)")
    private String commentContent;

    // 0：确认收货立即评价  1：买家好评后立即评价；到期前x天未评价，进行抢评  2：买家评价后不立即评价；到期前x天未评价，进行抢评
    private Long commentType;

    // 评价结束前多少毫秒，抢评（单位：毫秒）
    private Long commentTime;

    // 0、全好评  1、好评后好评，差评立即差评  2、全中评  3、全差评
    private Long commentRate;

    // 差评短信通知内容
    @Column(columnDefinition = "varchar(255)")
    private String badCommentMsg;

    @Transient
    private boolean badCommentNotice;

    @Transient
    private boolean badCommentBuyerSms;
    
    @Transient
    private boolean defenseNotice;

    public CommentConf() {
        super();
    }

    public CommentConf(Long userId, String nick, String commentContent) {
        this.userId = userId;
        this.nick = nick;
        this.commentContent = commentContent;
        this.commentType = 0L;
        this.commentTime = 0L;
    }

    public CommentConf(Long userId, String nick, String commentContent, Long commentType, Long commentTime,
            Long commentRate, String badCommentMsg) {
        super();
        this.userId = userId;
        this.nick = nick;
        this.commentContent = commentContent;
        this.commentType = commentType;
        this.commentTime = commentTime;
        this.commentRate = commentRate;
        this.badCommentMsg = badCommentMsg;
    }

    public CommentConf(ResultSet rs) throws SQLException {
        this.userId = rs.getLong(1);
        this.nick = rs.getString(2);
        this.commentContent = rs.getString(3);
        this.commentType = rs.getLong(4);
        this.commentTime = rs.getLong(5);
        this.commentRate = rs.getLong(6);
        this.badCommentMsg = rs.getString(7);
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void editCommentContent(String oldContent, String newContent) {
        int index = this.commentContent.indexOf(oldContent + "!@#");
        if (index == -1) {
            return;
        }
        if (index == 0) {
            this.commentContent = this.commentContent.replace(oldContent + "!@#", newContent + "!@#");
        } else {
            this.commentContent = this.commentContent.replace("!@#" + oldContent + "!@#", "!@#" + newContent + "!@#");
        }
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getNick() {
        return this.nick;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public String getCommentContent() {
        return this.commentContent;
    }

    public Long getCommentType() {
        return commentType;
    }

    public void setCommentType(Long commentType) {
        this.commentType = commentType;
    }

    public Long getCommentTime() {
        return commentTime;
    }

    public void setCommentTime(Long commentTime) {
        this.commentTime = commentTime;
    }

    public long getCommentDays() {
        long commentDays = commentTime / (3600000 * 24);
        return commentDays;
    }

    public void setCommentDays(Long commentDays) {
        if (commentDays > 0) {
            this.commentTime = commentDays * 3600000 * 24;
        }
    }

    public Long getCommentRate() {
        return commentRate;
    }

    public void setCommentRate(Long commentRate) {
        this.commentRate = commentRate;
    }

    public void addCommentContent(String content) {
        this.commentContent += content + "!@#";
    }

    public void deleteCommentContent(String content) {
        int index = this.commentContent.indexOf(content + "!@#");
        if (index == -1) {
            return;
        }
        if (index == 0) {
            this.commentContent = this.commentContent.replace(content + "!@#", "");
        } else {
            this.commentContent = this.commentContent.replace("!@#" + content + "!@#", "!@#");
        }

    }

    public String getRandomComment() {
        int length = this.commentContent.split("!@#").length;
        if(length <= 0){
        	this.commentContent = "欢迎再次光临!@#";
        	length = 1;
        }
        int offset = new Random().nextInt(length);
        return this.commentContent.split("!@#")[offset];
    }

    public String getBadCommentMsg() {
        if (StringUtils.isEmpty(badCommentMsg)) {
            return DEAFULT_BADCOMMENT_BUYER_SMS;
        }
        return badCommentMsg;
    }

    public void setBadCommentMsg(String badCommentMsg) {
        this.badCommentMsg = badCommentMsg;
    }

    public boolean isBadCommentNotice() {
        return badCommentNotice;
    }

    public void setBadCommentNotice(boolean badCommentNotice) {
        this.badCommentNotice = badCommentNotice;
    }

    public boolean isBadCommentBuyerSms() {
        return badCommentBuyerSms;
    }

    public void setBadCommentBuyerSms(boolean badCommentBuyerSms) {
        this.badCommentBuyerSms = badCommentBuyerSms;
    }

    public boolean isDefenseNotice() {
        return defenseNotice;
    }

    public void setDefenseNotice(boolean defenseNotice) {
        this.defenseNotice = defenseNotice;
    }

    public String replaceTemplate(String buyer, String seller, String rateStr) {
        String msg = this.badCommentMsg;
        if (StringUtils.isEmpty(msg)) {
            msg = DEAFULT_BADCOMMENT_BUYER_SMS;
        }
        msg = msg.replaceAll("#买家#", buyer);
        msg = msg.replaceAll("\\[#卖家#\\]", seller);
        msg = msg.replaceAll("#评价#", rateStr);
        return msg;
    }

    @Override
    public String toString() {
        return "CommentConf [userId=" + userId + ", nick=" + nick + ", commentContent=" + commentContent
                + ", commentType=" + commentType + ", commentTime=" + commentTime + ", commentRate=" + commentRate
                + ", badCommentMsg=" + badCommentMsg + ", badCommentNotice=" + badCommentNotice
                + ", badCommentBuyerSms=" + badCommentBuyerSms + "]";
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

    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where userId = ? ";

    public static long findExistId(Long userId) {
        return dp.singleLongQuery(EXIST_ID_QUERY, userId);
    }

    static String CONF_QUERY = "select commentContent from " + TABLE_NAME + " where userId = ? ";

    public static String findConf(Long userId) {
        return dp.singleStringQuery(CONF_QUERY, userId);
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
                .insert("insert into `comment_conf`(`userId`,`nick`,`commentContent`, `commentType`, `commentTime`, `commentRate`, `badCommentMsg`) values(?,?,?,?,?,?,?)",
                        this.userId, this.nick, this.commentContent, this.commentType, this.commentTime,
                        this.commentRate, this.badCommentMsg);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);
            return false;
        }
    }

    public boolean rawUpdate() {
        long updateNum = dp
                .insert("update `comment_conf` set  `userId` = ?, `nick` = ?, `commentContent` = ?, `commentType` = ?, `commentTime` = ?, `commentRate` = ?, `badCommentMsg` = ? where `id` = ? ",
                        this.userId, this.nick, this.commentContent, this.commentType, this.commentTime,
                        this.commentRate, this.badCommentMsg, this.id);

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

    static String COMMENT_CONF_QUERY = "select userId,nick,commentContent,commentType,commentTime,commentRate,badCommentMsg from "
            + TABLE_NAME + " where userId = ? ";

    public static CommentConf findByUserId(Long userId) {
        return new JDBCBuilder.JDBCExecutor<CommentConf>(dp, COMMENT_CONF_QUERY, userId) {
            @Override
            public CommentConf doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new CommentConf(rs);
                } else {
                    return null;
                }
            }
        }.call();
    }

}
