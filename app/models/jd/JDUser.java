package models.jd;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.PolicySQLGenerator;

@Entity(name = JDUser.TABLE_NAME)
public class JDUser extends GenericModel implements PolicySQLGenerator {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(JDUser.class);

    @Transient
    public static final String TAG = "JDUser";

    @Transient
    public static final String TABLE_NAME = "jd_user";

    @Id
    Long id;

    String nick;

    String accessToken; 

    String refreshToken;

    public Long firstLoginTime;

    private int type;

    private int version;

    public static class Type {
        public static final int IS_VALID = 1;

        // 流量推广状态
        public static final int IS_POPULAR_OFF = 2;

        // 流量奖励推广位
        public static final int IS_POPULAR_AWARD = 4;

        // 自动评价
        public static final int IS_AUTOCOMMENT_ON = 16;
    }

    public JDUser() {

    }

    public JDUser(Long id, String accessToken, String refreshToken) {
        super();
        this.id = id;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getFirstLoginTime() {
        return firstLoginTime;
    }

    public void setFirstLoginTime(Long firstLoginTime) {
        this.firstLoginTime = firstLoginTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public JDUser(ResultSet rs) throws SQLException {
        this.id = rs.getLong(1);
        this.nick = rs.getString(2);
        this.accessToken = rs.getString(3);
        this.refreshToken = rs.getString(4);
        this.firstLoginTime = rs.getLong(5);
        this.type = rs.getInt(6);
        this.version = rs.getInt(7);
    }

    public boolean isValid() {
        return (this.type & Type.IS_VALID) > 0;
    }

    public void setValid(boolean toBeOn) {
        if (toBeOn) {
            this.type |= Type.IS_VALID;
        } else {
            this.type &= (~Type.IS_VALID);
        }
    }

    public boolean isPopularOff() {
        return (this.type & Type.IS_POPULAR_OFF) > 0;

    }

    public void setPopularOff(boolean toBeOn) {
        if (toBeOn) {
            this.type |= Type.IS_POPULAR_OFF;
        } else {
            this.type &= (~Type.IS_POPULAR_OFF);
        }
    }

    public boolean isPopularAward() {
        return (this.type & Type.IS_POPULAR_AWARD) > 0;
    }

    public void setPopularAward(boolean toBeOn) {
        if (toBeOn) {
            this.type |= Type.IS_POPULAR_AWARD;
        } else {
            this.type &= (~Type.IS_POPULAR_AWARD);
        }
    }

    public boolean isAutoCommentOn() {
        return (this.type & Type.IS_AUTOCOMMENT_ON) > 0;
    }

    public void setAutoCommentOn(boolean toBeOn) {
        if (toBeOn) {
            this.type |= Type.IS_AUTOCOMMENT_ON;
        } else {
            this.type &= (~Type.IS_AUTOCOMMENT_ON);
        }
    }

    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return TABLE_NAME;
    }

    @Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getIdColumn() {
        // TODO Auto-generated method stub
        return "id";
    }

    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "id";
    }

    @Transient
    static String SELECT_SQL = "select id,nick,accessToken,refreshToken,firstLoginTime,type,version from " + TABLE_NAME;

    @Transient
    static String FIND_BY_ID_QUERY = SELECT_SQL + " where id = ? ";

    public static JDUser findByUserId(Long userId) {
        return new JDBCExecutor<JDUser>(FIND_BY_ID_QUERY, userId) {
            @Override
            public JDUser doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new JDUser(rs);
                }
                return null;
            }
        }.call();
    }

    @Transient
    static String FIND_BY_ACCESS_TOKEN_QUERY = SELECT_SQL + " where `accessToken` = ? ";

    public static JDUser findByAccessToken(String accessToken) {
        return new JDBCExecutor<JDUser>(FIND_BY_ACCESS_TOKEN_QUERY, accessToken) {

            @Override
            public JDUser doWithResultSet(ResultSet rs) throws SQLException {
                while (rs.next()) {
                    return new JDUser(rs);
                }
                return null;
            }

        }.call();

    }

    @Override
    public String toString() {
        return "JDUser [id=" + id + ", nick=" + nick + ", accessToken=" + accessToken + ", refreshToken="
                + refreshToken + ", firstLoginTime=" + firstLoginTime + "]";
    }

    @Transient
    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where id = ? ";

    private static long findExistId(Long userId) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, userId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see codegen.CodeGenerator.PolicySQLGenerator#jdbcSave()
     */

    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.id);
            // if (existdId != 0)
            // log.info("find existed Id: " + existdId);

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

    public boolean rawInsert() {
        long id = JDBCBuilder
                .insert("insert into `jd_user`(`id`,`nick`,`accessToken`,`refreshToken`,`firstLoginTime`,`type`,`version`) values(?,?,?,?,?,?,?)",
                        this.id, this.nick, this.accessToken, this.refreshToken, this.firstLoginTime, this.type,
                        this.version);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.id);
            return false;
        }

    }

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder
                .insert("update `jd_user` set  `nick` = ?, `accessToken` = ?, `refreshToken` = ?, `firstLoginTime` = ?, `type` = ?, `version` = ? where `id` = ? ",
                        this.nick, this.accessToken, this.refreshToken, this.firstLoginTime, this.type, this.version,
                        this.getId());

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for :" + this.id + "[userId : ]" + this.id);

            return false;
        }
    }

}
