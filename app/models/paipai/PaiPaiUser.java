
package models.paipai;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.utils.NumberUtil;

@Entity(name = PaiPaiUser.TABLE_NAME)
public class PaiPaiUser extends GenericModel implements PolicySQLGenerator {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(PaiPaiUser.class);

    @Transient
    public static final String TAG = "PaipaiUser";

    @Transient
    public static final String TABLE_NAME = "paipai_user";

    /**
     * qq 号码，对应uin参数
     */
    @Id
    private Long id;

    private String nick;

    private String accessToken;

    private String refreshToken;

    private Long firstLoginTime;

    private int type;

    private int version;

    public static class Type {
        public static final int IS_VALID = 1;
        
        //流量推广状态
        public static final int IS_POPULAR_OFF = 2;
        
        //流量奖励推广位
        public static final int IS_POPULAR_AWARD = 4;
        
        //自动评价
        public static final int IS_AUTOCOMMENT_ON=16;
    }

    public PaiPaiUser() {

    }

    public PaiPaiUser(ResultSet rs) throws SQLException {
        int count = 1;
        this.id = rs.getLong(count++);
        this.nick = rs.getString(count++);
        this.accessToken = rs.getString(count++);
        this.refreshToken = rs.getString(count++);
        this.firstLoginTime = rs.getLong(count++);
        this.type = rs.getInt(count++);
        this.version = rs.getInt(count++);
    }

    public PaiPaiUser(Long id, String accessToken) {
        super();
        this.id = id;
        this.accessToken = accessToken;
    }

    public PaiPaiUser(Long id, String accessToken, String refreshToken) {
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
    

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "PaiPaiUser [id=" + id + ", nick=" + nick + ", accessToken=" + accessToken + ", refreshToken="
                + refreshToken + ", firstLoginTime=" + firstLoginTime + ", type=" + type + ", version=" + version + "]";
    }

    @Transient
    static String SELECT_SQL = "select `id`,`nick`,`accessToken`,`refreshToken`,`firstLoginTime`,`type`,`version` from ";

    @Transient
    static String FIND_BY_ID_QUERY = SELECT_SQL + TABLE_NAME + " where id = ? ";

    public static PaiPaiUser findByUserId(Long userId) {
        if (NumberUtil.isNullOrZero(userId)) {
            return null;
        }

        return new JDBCExecutor<PaiPaiUser>(FIND_BY_ID_QUERY, userId) {
            @Override
            public PaiPaiUser doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new PaiPaiUser(rs);
                }
                return null;
            }

        }.call();
    }

    @Transient
    static String FIND_BY_ACCESS_TOKEN = SELECT_SQL + TABLE_NAME + " where accessToken = ? ";

    public static PaiPaiUser findBySessionKey(String accessToken) {
        if (StringUtils.isEmpty(accessToken)) {
            return null;
        }
        return new JDBCExecutor<PaiPaiUser>(FIND_BY_ACCESS_TOKEN, accessToken) {
            @Override
            public PaiPaiUser doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new PaiPaiUser(rs);
                }
                return null;
            }

        }.call();
    }

    @Override
    @Transient
    public String getTableName() {
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
    public void _save() {
        if (this.firstLoginTime == null || this.firstLoginTime <= 0L) {
            this.firstLoginTime = System.currentTimeMillis();
        }

        jdbcSave();
        // super._save();
    }

    @Override
    public boolean jdbcSave() {
        try {
            long existdId = findExistId(this.id);

            if (existdId <= 0L) {
                this.rawInsert();
            } else {
                this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

        // UserDao.setUserCache(this);
        return true;
    }

    @Override
    public String getIdName() {
        // TODO Auto-generated method stub
        return "id";
    }

    @Transient
    static String EXIST_ID_QUERY = "select id from " + PaiPaiUser.TABLE_NAME + " where id = ? ";

    public static long findExistId(Long uin) {
        return JDBCBuilder.singleLongQuery(EXIST_ID_QUERY, uin);
    }

    public boolean rawInsert() {
        long id = JDBCBuilder
                .insert("insert into `paipai_user`(`id`,`nick`,`accessToken`,`refreshToken`,`firstLoginTime`,`type`,`version`) values(?,?,?,?,?,?,?)",
                        this.id, this.nick, this.accessToken, this.refreshToken, this.firstLoginTime, this.type,
                        this.version);

        log.info("[Insert user Id:]" + id + "[userId : ]" + this.nick);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userNick : ]" + this.nick);
            return false;
        }

    }

    public boolean rawUpdate() {

        long updateNum = JDBCBuilder
                .insert("update `paipai_user` set  `nick` = ?, `accessToken` = ?, `refreshToken` = ?, `firstLoginTime` = ?, `type` = ?, `version` = ?  where `id` = ? ",
                        this.nick, this.accessToken, this.refreshToken, this.firstLoginTime, this.type, this.version,
                        this.getId());

        if (updateNum == 1) {
            log.info("update ok for :" + this.getId() + "[userNick : ]" + this.nick);
            return true;
        } else {
            log.error("update failed...for :" + this.getId() + "[userNick : ]" + this.nick);
            return false;
        }
    }

}
