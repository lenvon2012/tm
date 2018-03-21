
package dao;

import static java.lang.String.format;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;

import jdbcexecutorwrapper.JDBCStringListExecutor;
import job.showwindow.WindowRemoteJob;
import models.user.User;
import models.user.User.Type;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.elasticsearch.common.util.concurrent.jsr166y.ConcurrentLinkedDeque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.jobs.Job;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import transaction.JPATransactionManager;
import utils.TaobaoUtil;
import actions.SubcribeAction;
import bustbapi.ErrorHandler;
import bustbapi.FenxiaoApi;
import bustbapi.ShopApi.ShopGet;
import bustbapi.UserAPIs;
import bustbapi.UserAPIs.UserGetApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.taobao.api.domain.Shop;

import configs.TMConfigs;
import controllers.APIConfig;
import controllers.APIConfig.Platform;

public class UserDao {

    final static Logger log = LoggerFactory.getLogger(UserDao.class);

    public static boolean isVaild(final String sid) {

        /*return new TransactionSecurity<Boolean>() {
            @Override
            public Boolean operateOnDB() {
                User user = User.find("sessionKey = ? ", sid).first();

                if (user == null) {
                    return true;
                }

                return user.isVaild();
            }
        }.execute();*/

        User user = UserDao.findBySessionKey(sid);
        if (user == null) {
            return true;
        }

        return user.isVaild();
    }

    public static boolean updateVaild(Long userId, boolean isVaild) {

        log.info(format("updateVaild:userId, isVaild".replaceAll(", ", "=%s, ") + "=%s", userId, isVaild));

        User user = UserDao.findById(userId);
        if (user == null) {
            return false;
        }

        return user.updateIsVaild(isVaild);
    }

    public static final User findById(Long userId) {

//        log.info(format("findById:userId".replaceAll(", ", "=%s, ") + "=%s", userId));

        if (userId == null) {
            return null;
        }
        if (userId <= 0L) {
            return null;
        }

        User user = getUserCache(userId);
//        log.info("[cache :]" + user);
        if (user != null) {
            return user;
        }

        user = User.findByUserId(userId);
//        log.info("[ jpa]" + user);
        if (user != null) {
            setUserCache(user);
        }
        return user;
    }

    public static final User findByUserNick(String userNick) {
        User user = getUserCache(userNick);
        if (user != null) {
            return user;
        }

        user = User.findByUserNick(userNick);
        if (user != null) {
            setUserCache(user);
        }

        return user;
    }

    public static User getUserCache(Long userId) {
        String key = "user-" + userId;
        User user = (User) (Cache.get(key));
        return user;
    }

    public static User getUserCache(String nick) {
        String key = "usernick-" + nick;
        User user = (User) (Cache.get(key));
        return user;
    }

    public static void clearUserCache(User user) {
        for (String key : new String[] {
                "user-" + user.getId(), "usernick-" + user.getUserNick()
        }) {
            Cache.delete(key);
        }

    }

    public static void setUserCache(User user) {
        for (String key : new String[] {
                "user-" + user.getId(), "usernick-" + user.getUserNick()
        }) {
            Cache.safeDelete(key);
            Cache.safeSet(key, user, "24h");
        }

//        log.error(" reset user cahce : user:" + user);
    }

    public static User findBySessionKey(String sessionKey) {

//        log.info(format("findBySessionKey:sessionKey".replaceAll(", ", "=%s, ") + "=%s", sessionKey));

        if (StringUtils.isEmpty(sessionKey)) {
            return null;
        }
        //return User.find("bySessionKey", sessionKey).first();

        return User.findBySessionKey(sessionKey);
    }

    public static void updateSession(User user, String session) {
        if (StringUtils.isEmpty(session)) {
            return;
        }
        user.setSessionKey(session);
        user.jdbcSave();
    }

    public static models.user.User updateIsVaildAndSessionKey(com.taobao.api.domain.User tbUser, models.user.User user,
            boolean b, String top_session, String refreshToken) {
        if (tbUser != null) {
            user.setLevel((int) tbUser.getSellerCredit().getLevel().longValue());
        }
        int version = SubcribeAction.getMax(user).getVersion();

        user.setVersion(version);
        if (tbUser != null) {
        	user.setUserNick(tbUser.getNick());
        }
        user.setVaild(b);
        user.setSessionKey(top_session);
        user.setRefreshToken(refreshToken);
        user.jdbcSave();
        log.warn("get version [" + version + "] for user : " + user);
        return user;
    }

    public static models.user.User addUser(com.taobao.api.domain.User user, String userNick, Long userId,
            String top_session, boolean isValid, int shopCid, String refreshToken) {
        /**
         * get seller mobile
         */
//        String mobile = UserAction.getSellerMobile(top_session);
//        if (mobile != null && mobile.length() == 11) {
//            new SellerMobile(userId, userNick, mobile).jdbcSave();
//        }

        models.user.User res = null;
        if (user == null) {
            res = new User(userNick, userId, top_session, isValid, shopCid);
        } else {
            res = new User(user, top_session, shopCid);
        }

        int version = SubcribeAction.getMax(res).getVersion();
        res.setVersion(version);
        res.setRefreshToken(refreshToken);

        FenxiaoApi.setUserFenxiao(res);
        res.jdbcSave();
        res.setNew(true);

        permitFirstInUser(res);

        return res;
    }

    public static void permitFirstInUser(final models.user.User user) {
        if (!TMConfigs.App.ENABLE_TMHttpServlet) {
            return;
        }

        if (APIConfig.get().getPlatform() != Platform.taobao) {
            return;
        }
        WindowRemoteJob.getPool().submit(new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                // 现在每个用户都默认开启主动通知
                /*if (!user.isMsgOn()) {
                    return Boolean.FALSE;
                }*/
                return TaobaoUtil.permitTMCUser(user);
            }
        });
    }

    public static models.user.User getOneVaildUser() {
        //return User.find("type>=16").first();
        List<User> res = fetch(" type >= 16 limit 1");
        if (res.size() == 0) {
            return null;
        } else {
            return res.get(0);
        }
    }

    public static List<User> fetchAllUser() {
        return fetch("1 = 1");
    }

    public static void update(Long userId) {
        User localUser = UserDao.findById(userId);
        com.taobao.api.domain.User tbUser = new UserAPIs.UserGetApi(localUser.getSessionKey(), null).call();
        localUser.update(tbUser);
        localUser.jdbcSave();

    }

    public static final List<User> findValidListOrderBydFirstLoginTime(int offset, int limit) {
        List<User> res = fetch(" type & " + Type.IS_VALID
                + " > 0 order by firstLoginTime desc limit ? offset ?",
                limit, offset);
        return res;
    }

    public static final List<User> findAllUserList(int offset, int limit) {
        List<User> res = fetch(" 1=1 order by id desc limit ? offset ?", limit, offset);
        return res;
    }

    public static final List<User> findValidList(int offset, int limit) {
        List<User> res = fetch(" type & " + Type.IS_VALID + " > 0 order by id desc  limit ? offset ?", limit, offset);
        return res;
    }

    public static final List<User> findLevel5Users(int offset, int limit) {
        List<User> res = fetch(" type & " + Type.IS_VALID + " > 0 and level > 5 order by id desc  limit ? offset ?",
                limit, offset);
        return res;
    }

    public static final List<User> findWindowShowOn(int offset, int limit) {
        List<User> res = fetch(true, " type & " + Type.IS_SHOWWINDOW_ON + " > 0 and type & " + Type.IS_VALID
                + " > 0 order by firstLoginTime desc limit ? offset ?", limit, offset);
        return res;
    }

    public static final long countWindowShowOn() {

        return User.dp.singleLongQuery(" select count(*) from " + User.TABLE_NAME + " where  type & "
                + Type.IS_SHOWWINDOW_ON + " > 0 and type & " + Type.IS_VALID + " > 0 ");

    }

    public static final Set<Long> findUserSetIdWindowOn(int offset, int limit) {
        return new JDBCBuilder.JDBCLongSetExecutor(User.dp, " select id from user where  type & "
                + Type.IS_SHOWWINDOW_ON
                + " > 0 and type & " + Type.IS_VALID
                + " > 0 order by id desc limit ? offset ?", limit, offset).call();
    }

    public static final List<User> findAutoCommentOn(int offset, int limit) {
        List<User> res = fetch(" type & " + Type.IS_AUTOCOMMENT_ON + " > 0 and type & " + Type.IS_VALID
                + " > 0 order by id desc limit ? offset ?", limit, offset);
        return res;
    }

    public static final List<Long> findUserIdWindowShowOn() {
        List<Long> res = fetchIds(" type & " + Type.IS_SHOWWINDOW_ON + " > 0 and type & " + Type.IS_VALID
                + " > 0 order by id desc ");
        return res;
    }

    public static final List<User> findValidPayedList(int offset, int limit) {
        List<User> res = fetch("type >= 16 limit ? offset ?", limit, offset);
        return res;
    }

    public static final List<User> findAutoDefenseOn(int offset, int limit) {
        List<User> res = fetch(" type & " + Type.IS_AutoDefense_On + " > 0 and type & " + Type.IS_VALID
                + " > 0 order by id desc limit ? offset ?", limit, offset);
        return res;
    }

    public static abstract class UserBatchOper implements Callable<Boolean> {
        public int offset = 0;

        public int limit = 32;

        protected long sleepTime = 500L;

        public UserBatchOper(int offset, int limit) {
            this.offset = offset;
            this.limit = limit;
        }

        public UserBatchOper(int limit) {
            this.limit = limit;
        }

        public UserBatchOper(int offset, int limit, long sleepTime) {
            super();
            this.offset = offset;
            this.limit = limit;
            this.sleepTime = sleepTime;
        }

        public List<User> findNext() {
            return UserDao.findValidList(offset, limit);
        }

        public abstract void doForEachUser(User user);

        @Override
        public Boolean call() {

            while (true) {

                List<User> findList = findNext();
                if (CommonUtils.isEmpty(findList)) {
                    return Boolean.TRUE;
                }

                for (User user : findList) {
                    offset++;
                    doForEachUser(user);
                }

                findList.clear();
                JPATransactionManager.clearEntities();
                CommonUtils.sleepQuietly(sleepTime);
            }

        }
    }

    public static Queue<User> findidbynick() {
        return new JDBCExecutor<Queue<User>>(
                User.dp,
                " select id,userNick,sessionKey,firstLoginTime,version,cid,level,type,lastUpdateTime from user where userNick in (select nick from more_click_nick)") {

            @Override
            public Queue<User> doWithResultSet(ResultSet rs) throws SQLException {
                Queue<User> list = new ConcurrentLinkedDeque<User>();
                while (rs.next()) {
                    list.add(new User(rs));
                }
                return list;
            }

        }.call();
    }

    public static List<User> fetch(String whereQuery, Object... args) {
        return fetch(false, whereQuery, args);
    }

    public static List<User> fetch(boolean debug, String whereQuery, Object... args) {
        return new JDBCExecutor<List<User>>(debug, User.dp,
                " select id,userNick,sessionKey,firstLoginTime,version,cid,level,type,lastUpdateTime from user where "
                        + whereQuery, args) {

            @Override
            public List<User> doWithResultSet(ResultSet rs) throws SQLException {
                List<User> list = new ArrayList<User>();
                while (rs.next()) {
                    list.add(new User(rs));
                }
                return list;
            }

        }.call();
    }

    public static List<Long> fetchIds(String whereQuery) {
        return new JDBCExecutor<List<Long>>(User.dp, " select id from user where " + whereQuery) {
            @Override
            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {
                List<Long> list = new ArrayList<Long>();
                while (rs.next()) {
                    list.add(rs.getLong(1));
                }
                return list;
            }

        }.call();
    }

    public static void refreshToken(User user) {
        if (StringUtils.isEmpty(user.getRefreshToken())) {
            return;
        }
        String refreshToken = TaobaoUtil.refreshToken(user);
        if (StringUtils.isEmpty(refreshToken)) {
            return;
        }
        UserDao.updateIsVaildAndSessionKey(user, true, user.getSessionKey(), refreshToken);
    }

    public static void refreshTokenNow(User user) {
        if (user == null) {
            return;
        }
        String refreshToken = TaobaoUtil.refreshToken(user);
        if (StringUtils.isEmpty(refreshToken)) {
            return;
        }
        UserDao.updateIsVaildAndSessionKey(user, true, user.getSessionKey(), refreshToken);
    }

    public static User updateIsVaildAndSessionKey(User user, boolean isVaild, String sessionKey, String refreshToken) {

        user.setVaild(isVaild);
        user.sessionKey = sessionKey;
        user.setSessionKey(sessionKey);
        user.setRefreshToken(refreshToken);
        if (isVaild) {
            int version = SubcribeAction.getMax(user).getVersion();
            user.setVersion(version);
        }
        user.jdbcSave();
        return user;

    }

    public static class UserShopCidUpdateJob extends Job {
        public void doJob() {
            new UserBatchOper(32) {
                @Override
                public void doForEachUser(User user) {
                    doForUser(user);
                }
            }.call();
        }

        private void doForUser(User user) {
            Shop shop = new ShopGet(user.getUserNick()).call();
            if (shop == null) {
                return;
            }
            if (user.getCid() == shop.getCid().intValue()) {
                return;
            }
            user.setCid(shop.getCid().intValue());
            user.jdbcSave();
        }
    }

    public static abstract class UserBatchJob extends Job {
        public abstract void doForUser(User user);

        protected boolean doValidUser = true;

        public UserBatchJob() {
            super();
        }

        public UserBatchJob(boolean doValidUser) {
            super();
            this.doValidUser = doValidUser;
        }

        protected int offset = 0;

        protected int limit = 128;

        public void doJob() {
            new UserBatchOper(offset, limit) {
                @Override
                public void doForEachUser(User user) {
                    if (doValidUser) {
                        boolean res = UserDao.doValid(user);
                        if (!res) {
                            return;
                        }
                    }
                    doForUser(user);
                }
            }.call();
        }

    }

    public static JsonNode refreshTokenAndGetJson(User user) {
        JsonNode result = null;
        if (StringUtils.isEmpty(user.getRefreshToken())) {
            return result;
        }
        result = TaobaoUtil.refreshTokenAndGetJsonObject(user);
        String refreshToken = result.get("refresh_token").getTextValue();
        if (StringUtils.isEmpty(refreshToken)) {
            return result;
        }
        UserDao.updateIsVaildAndSessionKey(user, true, user.getSessionKey(),
                refreshToken);
        return result;
        /*
         * String w2_expires_in_str =
         * result.get("w2_expires_in").getTextValue(); Long w2_expires_in =
         * Long.parseLong(w2_expires_in_str); if(w2_expires_in==null ||
         * w2_expires_in < 300){
         * log.warn("refresh token & w2_expires_in is "+w2_expires_in); result =
         * ; }else{ result = refreshObj; } return result;
         */
    }

    public static List<String> fetchNicks(String whereQuery, Object... args) {
        return new JDBCStringListExecutor("select `userNick` from user where " + whereQuery, args).call();
    }

    public static boolean doValid(User user) {
        if (user == null) {
            return false;
        }
        UserGetApi api = new UserAPIs.UserGetApi(user.getSessionKey(), null);
        com.taobao.api.domain.User tbUser = api.call();
        if (tbUser == null) {
            ErrorHandler.fuckWithTheErrorCode(user.getId(), user.getSessionKey(), api.getSubErrorCode());
            return false;
        }
        return true;
    }
    
    public static void clearOld() {
        long now = System.currentTimeMillis();
        long lastUpdate = now - 3 * DateUtil.THIRTY_DAYS;
        User.dp.update(" delete from " + User.TABLE_NAME + " where type & " + Type.IS_VALID
                + " = 0 and lastUpdateTime < ? ", lastUpdate);

    }
}
