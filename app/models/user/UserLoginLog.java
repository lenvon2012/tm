
package models.user;

import javax.persistence.Entity;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.JDBCBuilder;

@Entity(name = UserLoginLog.TABLE_NAME)
public class UserLoginLog extends Model {

    public final static Logger log = LoggerFactory.getLogger(UserLoginLog.class);

    public final static String TABLE_NAME = "user_login_log";

    @Index(name = "userId")
    public Long userId;

    public String userNick;

    @Index(name = "ts")
    public Long loginTs;

    String ip;

    public UserLoginLog(User user) {
        this.userId = user.getId();
        this.userNick = user.getUserNick();
    }

    public UserLoginLog(Long userId, String userNick) {
        this.userId = userId;
        this.userNick = userNick;
    }

    public UserLoginLog(models.user.User user, String ip2) {
        this(user);
        this.ip = ip2;
    }

    @Override
    public void _save() {
        loginTs = System.currentTimeMillis();
        jdbcSave();
    }

    public boolean jdbcSave() {

        try {
            return this.rawInsert();
        } catch (Exception e) {
            return false;
        }
    }

    static String insertSQL = "insert into `user_login_log`(`userId`,`userNick`,`loginTs`,`ip`) values(?,?,?,?)";

    public boolean rawInsert() {

        long id = JDBCBuilder.insert(false, insertSQL, this.userId, this.userNick, this.loginTs, this.ip);

        log.info("[Insert UserLoginLog Id:]" + id);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert UserLoginLog Fails.....");
            return false;
        }
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserNick() {
        return userNick;
    }

    public void setUserNick(String userNick) {
        this.userNick = userNick;
    }

    public Long getLoginTs() {
        return loginTs;
    }

    public void setLoginTs(Long loginTs) {
        this.loginTs = loginTs;
    }

}
