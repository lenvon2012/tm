package models.sms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = SmsNotifyQueue.TABLE_NAME)
public class SmsNotifyQueue extends GenericModel implements PolicySQLGenerator {

    private static final long serialVersionUID = -5764295838694912086L;

    private static final Logger log = LoggerFactory.getLogger(SmsNotifyQueue.class);

    public static final String TAG = "SmsNotifyQueue";

    public final static String TABLE_NAME = "SmsNotifyQueue_";

    public static SmsNotifyQueue EMPTY = new SmsNotifyQueue();

    public SmsNotifyQueue() {
    }

    @Id
    @Column(name = "user_id")
    @PolicySQLGenerator.CodeNoUpdate
    public Long userId;

    @PolicySQLGenerator.CodeNoUpdate
    public String nick;

    @PolicySQLGenerator.CodeNoUpdate
    public String phone;

    @PolicySQLGenerator.CodeNoUpdate
    public Long addAt = 0L;

    public Long updateAt;

    public int repeatTimes;

    public String content = StringUtils.EMPTY;

    public SmsNotifyQueue(Long userId, String userNick, String phone, String content) {
        this.userId = userId;
        this.nick = userNick;
        this.phone = phone;
        this.repeatTimes = 0;
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
        return "userId";
    }

    @Override
    public void setId(Long id) {
        this.userId = id;
    }

    @Override
    public String getIdName() {
        return "userId";
    }

    @Override
    public Long getId() {
        return userId;
    }

    @Override
    public void _save() {
        this.jdbcSave();
    }

    static String EXIST_ID_QUERY = "select user_id from SmsNotifyQueue_ where user_id = ? ";

    public static long findExistId(Long userId) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, userId);
    }

    @Override
    public boolean jdbcSave() {

        if (addAt <= 0L) {
            this.addAt = System.currentTimeMillis();
        }
        this.updateAt = System.currentTimeMillis();

        try {
            long existdId = findExistId(this.userId);

            if (existdId == 0L) {
                return this.rawInsert();
            } else {
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }
    }

    static String insertSQL = "insert into `SmsNotifyQueue_`(`user_id`,`nick`,`phone`,`addAt`,`updateAt`,`repeatTimes`,`content`) values(?,?,?,?,?,?,?)";

    public boolean rawInsert() {

        long id = JDBCBuilder.insert(false, insertSQL, this.userId, this.nick, this.phone, this.addAt, this.updateAt,
                this.repeatTimes, this.content);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId);

            return false;
        }
    }

    public boolean rawDelete() {
        return JDBCBuilder.insert(false, "delete from `SmsNotifyQueue_` where user_id = ?", this.getUserId()) > 0L;
    }

    static String updateSQL = "update `SmsNotifyQueue_` set  `updateAt` = ?, `repeatTimes` = ?, `content` = ? where `user_id` = ? ";

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder.insert(false, updateSQL, this.updateAt, this.repeatTimes, this.content,
                this.getId());

        if (updateNum == 1) {

            return true;
        } else {
            log.error("update failed...for :" + this.getId() + "[userId : ]" + this.userId);

            return false;
        }
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public Long getAddAt() {
        return addAt;
    }

    public void setAddAt(Long addAt) {
        this.addAt = addAt;
    }

    public Long getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Long updateAt) {
        this.updateAt = updateAt;
    }

    public int getRepeatTimes() {
        return repeatTimes;
    }

    public void setRepeatTimes(int repeatTimes) {
        this.repeatTimes = repeatTimes;
    }

    public static List<SmsNotifyQueue> findNotDoneRecords(int size) {
        //return SmsNotifyQueue.find("repeatTimes < 3  order by addAt asc").fetch(size);
    	return SmsNotifyQueue.findListWithLimit(size);
    }

    public boolean addSendTime() {
        this.repeatTimes = this.repeatTimes + 1;
        this.updateAt = System.currentTimeMillis();
        return this.rawUpdate();
    }
    
    private static final String SelectAllProperties = " userId,nick,phone,addAt,updateAt,repeatTimes,content ";
    
    public static List<SmsNotifyQueue> findListWithLimit(int size) {

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where repeatTimes < 3  order by addAt asc limit ?";

        return new JDBCBuilder.JDBCExecutor<List<SmsNotifyQueue>>(query, size) {

            @Override
            public List<SmsNotifyQueue> doWithResultSet(ResultSet rs)
                    throws SQLException {
            	List<SmsNotifyQueue> list = new ArrayList<SmsNotifyQueue>();
                while (rs.next()) {
                	SmsNotifyQueue sms = new SmsNotifyQueue(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getString(7));
                	sms.setAddAt(rs.getLong(4));
                	sms.setUpdateAt(rs.getLong(5));
                	sms.setRepeatTimes(rs.getInt(6));
                	list.add(sms);
                } 
                return list;
            }

        }.call();
    }
}
