package models.sms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.pojo.PageOffset;

@Entity(name = SmsSendLog.TABLE_NAME)
public class SmsSendLog extends Model implements PolicySQLGenerator {

    private static final long serialVersionUID = -6783885164952528230L;

    private static final Logger log = LoggerFactory.getLogger(SmsSendLog.class);

    public static final String TABLE_NAME = "SmsSendLog_";

    public static SmsSendLog EMPTY = new SmsSendLog();

    public SmsSendLog() {
    }

    @Column(name = "userId")
    public Long userId;

    public String nick;

    public String phone;

    public long addAt = 0L;

    public String content;

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.BASIC, EMPTY);

    public static class TYPE {
        public static int TEMP = 0;
        public static int FREE_USER_REMIND = 1;
        public static int JOIN_QUN_REMIND = 2;
        public static int USER_EXPIRED_REMIND = 4;
        public static int USER_LOGIN_CHECK = 8;
        public static int DEFESE_SUCCESS_WARN = 16;
        public static int DEFESE_FAIL_WARN = 32;
        public static int DEFESE_WARN = 64;
        public static int BADCOMMENT_NOTICE = 128;
        public static int BADCOMMENT_BUYER_SMS = 256;
        public static int NOTICE_SELLER_SMS_OUT = 512;
        public static int SMS_SEND_BY_WAIBAO = 1024;
    }

    public int type;

    public long tid;

    public boolean success = true;

    public SmsSendLog(Long userId, String nick, String phone, String content, int type, long tid) {
        this.userId = userId;
        this.nick = nick;
        this.phone = phone;
        this.content = content;
        this.type = type;
        this.tid = tid;
    }

    public SmsSendLog(Long userId, String nick, String phone, String content, int type, long tid, boolean success) {
        this.userId = userId;
        this.nick = nick;
        this.phone = phone;
        this.content = content;
        this.type = type;
        this.tid = tid;
        this.success = success;
    }

    public SmsSendLog(ResultSet rs) throws SQLException {
        this.userId = rs.getLong(1);
        this.nick = rs.getString(2);
        this.phone = rs.getString(3);
        this.addAt = rs.getLong(4);
        this.content = rs.getString(5);
        this.type = rs.getInt(6);
        this.tid = rs.getLong(7);
        this.success = rs.getBoolean(8);
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

    public long getAddAt() {
        return addAt;
    }

    public void setAddAt(long addAt) {
        this.addAt = addAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "SmsSendLog [userId=" + userId + ", nick=" + nick + ", phone=" + phone + ", addAt=" + addAt
                + ", content=" + content + ", type=" + type + ", tid=" + tid + ", success=" + success + "]";
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

    @Override
    public void _save() {
        this.jdbcSave();
    }

    @Override
    public boolean jdbcSave() {

        try {
            addAt = System.currentTimeMillis();
            return this.rawInsert();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

    }

    static String insertSQL = "insert into `SmsSendLog_`(`userId`,`nick`,`phone`,`content`,`addAt`,`type`,`tid`,`success`) values(?,?,?,?,?,?,?,?)";

    public boolean rawInsert() {

        long id = dp.insert(false, insertSQL, this.userId, this.nick, this.phone, this.content, this.addAt, this.type,
                this.tid, this.success);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);

            return false;
        }

    }

    public static String SMS_SEND_LOG_QUERY = "select userId,nick,phone,addAt,content,type,tid,success from SmsSendLog_ ";

    public static List<SmsSendLog> findSmsSendLogByRules(Long userId, String buyerNick, Long startTs, Long endTs,
            PageOffset po) {
        String query = formatQuery(userId, buyerNick, startTs, endTs);

        String sql = SMS_SEND_LOG_QUERY + " where " + query + " order by addAt desc limit ? offset ? ";
        log.info(sql);
        List<SmsSendLog> logList = new JDBCExecutor<List<SmsSendLog>>(dp, sql, userId, po.getPs(), po.getOffset()) {
            @Override
            public List<SmsSendLog> doWithResultSet(ResultSet rs) throws SQLException {
                List<SmsSendLog> list = new ArrayList<SmsSendLog>();
                while (rs.next()) {
                    SmsSendLog smsLog = new SmsSendLog(rs);
                    if (smsLog != null)
                        list.add(smsLog);
                }
                return list;
            }
        }.call();

        return logList;
    }

    public static String COUNT_SMS_SEND_LOG_QUERY = "select count(*) from SmsSendLog_ ";

    public static long countSmsSendLogByRules(Long userId, String buyerNick, Long startTs, Long endTs) {
        String query = formatQuery(userId, buyerNick, startTs, endTs);

        String sql = COUNT_SMS_SEND_LOG_QUERY + " where " + query;
        log.info(sql);

        return dp.singleLongQuery(sql, userId);
    }

    private static String formatQuery(Long userId, String buyerNick, Long startTs, Long endTs) {
        String query = " userId=? ";
        if (!StringUtils.isEmpty(buyerNick)) {
            query += " and " + formatBuyerNickLike(buyerNick);
        }
        if (startTs != null && startTs.longValue() > 0L) {
            query += " and addAt >= " + startTs + " ";
        }
        if (endTs != null && endTs.longValue() > 0L) {
            query += " and addAt < " + endTs + " ";
        }
        return query;
    }

    private static String formatBuyerNickLike(String buyerNick) {
        String like = " nick like '%" + buyerNick + "%' ";
        return like;
    }

    public static List<SmsSendLog> findByUserIdAndType(final Long userId, final int type) {

        String sql = SMS_SEND_LOG_QUERY + " where userId =? and type = ? order by addAt desc ";
        log.info(sql);
        List<SmsSendLog> logList = new JDBCExecutor<List<SmsSendLog>>(dp, sql, userId, type) {
            @Override
            public List<SmsSendLog> doWithResultSet(ResultSet rs) throws SQLException {
                List<SmsSendLog> list = new ArrayList<SmsSendLog>();
                while (rs.next()) {
                    SmsSendLog smsLog = new SmsSendLog(rs);
                    if (smsLog != null) {
                        list.add(smsLog);
                    }
                }
                return list;
            }
        }.call();

        return logList;
    }
    
    public static List<SmsSendLog> findSameMsgSendLogs(Long userId, String phone, int type, long tid, Long startTs, Long endTs) {

        String sql = SMS_SEND_LOG_QUERY + " where userId =? and type = ? and tid = ? and phone = ? ";
        if (startTs != null && startTs.longValue() > 0L) {
            sql += " and addAt >= " + startTs;
        }
        if (endTs != null && endTs.longValue() > 0L) {
            sql += " and addAt < " + endTs;
        }
        sql += " order by addAt desc ";
        
//        log.info(sql + "   userId=" + userId + ", tid=" + tid + ", startTs=" + startTs + ", endTs=" + endTs);
        List<SmsSendLog> logList = new JDBCExecutor<List<SmsSendLog>>(dp, sql, userId, type, tid, phone) {
            @Override
            public List<SmsSendLog> doWithResultSet(ResultSet rs) throws SQLException {
                List<SmsSendLog> list = new ArrayList<SmsSendLog>();
                while (rs.next()) {
                    SmsSendLog smsLog = new SmsSendLog(rs);
                    if (smsLog != null) {
                        list.add(smsLog);
                    }
                }
                return list;
            }
        }.call();

        return logList;
    }

}
