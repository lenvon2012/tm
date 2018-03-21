package models.sms;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = SmsSendRecord.TABLE_NAME)
public class SmsSendRecord extends Model implements PolicySQLGenerator {

    private static final long serialVersionUID = -6783885164952528230L;

    private static final Logger log = LoggerFactory.getLogger(SmsSendRecord.class);

    public static final String TABLE_NAME = "SmsSendRecord_";

    public static SmsSendRecord EMPTY = new SmsSendRecord();

    public SmsSendRecord() {
    }

    @Column(name = "userId")
    public Long userId;

    public String nick;

    public String phone;

    public long addAt = 0L;

    public String content;

    public SmsSendRecord(SmsNotifyQueue notify, String content) {
        this.userId = notify.getUserId();
        this.nick = notify.getNick();
        this.phone = notify.getPhone();
        this.content = content;
    }

    public SmsSendRecord(Long userId, String nick, String phone, String content) {
        this.userId = userId;
        this.nick = nick;
        this.phone = phone;
        this.content = content;
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

    static String insertSQL = "insert into `SmsSendRecord_`(`userId`,`nick`,`phone`,`content`,`addAt`) values(?,?,?,?,?)";

    public boolean rawInsert() {

        long id = JDBCBuilder.insert(false, insertSQL, this.userId, this.nick, this.phone, this.content, this.addAt);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);

            return false;
        }

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

    @Override
    public String toString() {
        return "SmsSendRecord [userId=" + userId + ", nick=" + nick + ", addAt=" + addAt + ", content=" + content + "]";
    }

}
