
package dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import models.jd.JDUser;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import transaction.JDBCBuilder.JDBCExecutor;

public class JDUserDao {

    public final static Logger log = LoggerFactory.getLogger(JDUserDao.class);

    public static final JDUser findById(Long userId) {

//        log.info(format("findById:userId".replaceAll(", ", "=%s, ") + "=%s", userId));

        if (userId == null) {
            return null;
        }

        JDUser user = getUserCache(userId);
//        log.info("[cache :]" + user);
        if (user != null) {
            return user;
        }

        user = JDUser.findByUserId(userId);
//        log.info("[ jpa]" + user);
        if (user != null) {
            setUserCache(user);
        }
        return user;
    }

    static String SELECT_SQL = "select `id`,`accessToken`,`refreshToken`,`nick`,`firstLoginTime`,`type`,`version` from " + JDUser.TABLE_NAME;
    public static final JDUser findByUserNick(String userNick) {
        //return JDUser.find("userNick = ? ", userNick).first();
    	return new JDBCExecutor<JDUser>(SELECT_SQL + " where nick = ? ", userNick) {
            @Override
            public JDUser doWithResultSet(ResultSet rs) throws SQLException {
                while (rs.next()) {
                    return new JDUser(rs);
                }
                return null;
            }
        }.call();
    }

    public static JDUser getUserCache(Long userId) {
        String key = "user-" + userId;
        JDUser user = (JDUser) (Cache.get(key));
        return user;
    }

    public static void clearUserCache(Long userId) {
        String key = "user-" + userId;
        Cache.delete(key);
    }

    public static void setUserCache(JDUser user) {
        String key = "user-" + user.getId();
        Cache.safeDelete(key);
        Cache.safeSet(key, user, "24h");
//        log.error(" reset user cahce : user:" + user);
    }

    public static JDUser findBySessionKey(String sessionKey) {
        if (StringUtils.isEmpty(sessionKey)) {
            return null;
        }
        //return JDUser.find("bySessionKey", sessionKey).first();
        return new JDBCExecutor<JDUser>(SELECT_SQL + " where sessionKey = ? ", sessionKey) {
            @Override
            public JDUser doWithResultSet(ResultSet rs) throws SQLException {
                while (rs.next()) {
                    return new JDUser(rs);
                }
                return null;
            }
        }.call();
    }

    public static void updateSession(JDUser user, String session) {
        if (StringUtils.isEmpty(session)) {
            return;
        }
//        user.setSessionKey(session);
//        user.jdbcSave();
    }

}
