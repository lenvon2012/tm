
package models.updatetimestamp.updates;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.persistence.Entity;

import jdbcexecutorwrapper.JDBCMapLongExecutor;
import models.updatetimestamp.UserUpdateTimestamp;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.JDBCBuilder;

import com.ciaosir.client.utils.DateUtil;

@Entity(name = ItemUpdateTs.TABLE_NAME)
public class ItemUpdateTs extends UserUpdateTimestamp {

    private static final Logger log = LoggerFactory.getLogger(ItemUpdateTs.class);

    public static final String TAG = "ItemUpdateTs";

    public static final String TABLE_NAME = "item_update_ts";

    public ItemUpdateTs(ResultSet rs) throws SQLException {
        super();
        this.userId = rs.getLong(1);
        this.firstUpdateTime = rs.getLong(2);
        this.lastUpdateTime = rs.getLong(3);
    }

    public ItemUpdateTs(Long userId) {
        super(userId);
    }

    public ItemUpdateTs(Long userId, long ts) {
        super(userId, ts);
    }

    public static void updateLastItemModifedTime(Long userId, long ts) {
        updateLastItemModifedTime(userId, ts, false);
    }

    public static void updateLastItemModifedTime(Long userId, long ts, boolean forseUpdate) {

        ItemUpdateTs itemTs = ItemUpdateTs.fetchByUser(userId);
        if (itemTs == null) {
            log.warn("No User Found...Create it now for id:" + userId);
            itemTs = new ItemUpdateTs(userId, ts);
            itemTs.jdbcSave();
            log.warn("save new update time successfully with new item update ts:" + itemTs);
//            log.info("[now find :]" + findByUser(userId));
            return;
        }

        if (!forseUpdate && ts < itemTs.lastUpdateTime) {
            log.warn("ts[" + ts + "] is less than [" + itemTs.lastUpdateTime + "], No Update");
            return;
        }

        itemTs.setLastUpdateTime(ts);
        itemTs.jdbcSave();

        log.warn("save new update time successfully with new item update ts:" + itemTs);
    }

    static String FIND_USERID_WITH_LASTUPDATETS = "select userId,last_ts from " + ItemUpdateTs.TABLE_NAME;

    public static Map<Long, Long> findUsreIdWithLastUpdateTs() {
        return new JDBCMapLongExecutor(FIND_USERID_WITH_LASTUPDATETS).call();
    }

    public static ItemUpdateTs fetchByUser(User user) {
        return fetchByUser(user.getId());
    }

    public static ItemUpdateTs fetchByUser(Long userId) {
        return fetch("userId = ?", userId);
    }

    public static ItemUpdateTs fetch(String whereQuery, Object... parms) {
        return new JDBCBuilder.JDBCExecutor<ItemUpdateTs>("select userId, first_ts, last_ts from " + TABLE_NAME
                + " where " + whereQuery, parms) {

            @Override
            public ItemUpdateTs doWithResultSet(java.sql.ResultSet rs) throws SQLException {
                while (rs.next()) {
                    return new ItemUpdateTs(rs);
                }
                return null;
            }
        }.call();
    }

    public static boolean exist(Long userId) {
        return JDBCBuilder.singleLongQuery("select userId from " + TABLE_NAME + " where userId=  ?", userId) > 0L;
    }

    static String deleteSQL = "delete from " + TABLE_NAME + " where `userId` = ? ";

    public void rawInsert() {
        long insert = JDBCBuilder.insert("insert into " + TABLE_NAME + " (userId, first_ts, last_ts) values(?,?,?)",
                userId, firstUpdateTime, lastUpdateTime);
    }

    public void rawUpdate() {
        long insert = JDBCBuilder.update(false, "update " + TABLE_NAME
                + " set first_ts = ? , last_ts = ? where userId = ?", firstUpdateTime, lastUpdateTime, userId);
    }

    public boolean jdbcSave() {
        if (exist(this.userId)) {
            this.rawUpdate();
        } else {
            this.rawInsert();
        }
        return true;
    }

    public void _save() {
        this.jdbcSave();
    }

    public static boolean jdbcDelete(Long userId) {

        long id = JDBCBuilder.insert(false, deleteSQL, userId);

        if (id > 0L) {
            return true;
        } else {
            log.error("delete Fails....." + "[userId : ]" + userId);
            return false;
        }
    }

    @Override
    public String toString() {
        return "ItemUpdateTs [userId=" + userId + ", firstUpdateTime=" + DateUtil.formDateForLog(firstUpdateTime)
                + ", lastUpdateTime=" + DateUtil.formDateForLog(lastUpdateTime) + "]";
    }
}
