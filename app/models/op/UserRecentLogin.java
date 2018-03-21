
package models.op;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import play.cache.Cache;
import play.db.jpa.GenericModel;

@Entity(name = UserRecentLogin.TABLE_NAME)
public class UserRecentLogin extends GenericModel {
    @Transient
    public static final String TABLE_NAME = "user_recent_login";

    @Transient
    private static final long serialVersionUID = 117768797195283246L;

    @Id
    Long id;

    String ip;

    long ts;

    public static String userRecent(Long userId) {
        UserRecentLogin model = UserRecentLogin.findById(userId);
        if (model == null) {
            return null;
        } else {
            return model.ip;
        }
    }

    public static void ensure(Long userId, String ip) {
        String key = TABLE_NAME + userId;
        UserRecentLogin record = (UserRecentLogin) Cache.get(key);
        if (record != null) {
            return;
        }

        record = UserRecentLogin.findById(userId);
        if (record == null) {
            record = new UserRecentLogin(userId, ip);
            record.save();
        } else {
            if (!StringUtils.equals(ip, record.ip)) {
                record.ip = ip;
                record.save();
            }
        }

        Cache.set(key, record, "4h");
    }

    public UserRecentLogin(Long id, String ip) {
        super();
        this.id = id;
        this.ip = ip;
        this.ts = System.currentTimeMillis();
    }

    public static class UserIp implements Serializable {
        private static final long serialVersionUID = 1L;

        Long userId;

        String ip;

        public UserIp(Long userId, String ip) {
            super();
            this.userId = userId;
            this.ip = ip;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        @Override
        public String toString() {
            return "UserIp [userId=" + userId + ", ip=" + ip + "]";
        }

    }

    public static void ensure(UserIp model) {
        ensure(model.getUserId(), model.getIp());
    }
}
