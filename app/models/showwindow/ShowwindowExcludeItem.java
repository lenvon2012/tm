
package models.showwindow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;

import jdbcexecutorwrapper.JDBCLongSetExecutor;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.db.jpa.Model;
import result.TMResult;
import transaction.JDBCBuilder;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;

import dao.item.ItemDao;

@Entity(name = ShowwindowExcludeItem.TABLE_NAME)
public class ShowwindowExcludeItem extends Model {

    private static final Logger log = LoggerFactory.getLogger(ShowwindowExcludeItem.class);

    public static final String TAG = "ShowwindowExcludeItem";

    public static final String TABLE_NAME = "showwindow_exclude_item";

    @Index(name = "userId")
    public Long userId;

    @Index(name = "numIid")
    public Long numIid;

    static String cacheTag = "_" + TAG;

    public static String genCacheKey(Long userId) {
        return cacheTag + userId;
    }

    public static void clearCacheKey(Long userId) {
        String key = genCacheKey(userId);
        Cache.safeDelete(key);
    }

    public static Set<Long> findIdsByUser(Long userId) {
        /*String cacheKey = genCacheKey(userId);
        Set<Long> res = (Set<Long>) Cache.get(cacheKey);
        if (res != null) {
            return res;
        }

//        return ShowwindowExcludeItem.find("userId = ?", userId).fetch();
        res = new JDBCLongSetExecutor("select numIid from " + TABLE_NAME + " where userId = ?", userId).call();
        Cache.set(cacheKey, res, (4 + (userId.longValue() / 64L % 4)) + "d");

        return res;*/
    	return new JDBCLongSetExecutor("select numIid from " + TABLE_NAME + " where userId = ?", userId).call();
    }
    
    public static Set<Long> findIdsByUser(Long userId, Collection<Long> numIidColl) {
        
        if (CommonUtils.isEmpty(numIidColl)) {
            return new HashSet<Long>();
        }
        
        String sql = "select numIid from " + TABLE_NAME + " where userId = ? ";
        sql += " and numIid in (" + StringUtils.join(numIidColl, ",") + ") ";
        
        return new JDBCLongSetExecutor(sql, userId).call();
    }

    public ShowwindowExcludeItem(Long userId, Long numIid) {
        super();
        this.userId = userId;
        this.numIid = numIid;
    }

    public static void add(User user, long numIid2) {
        clearCacheKey(user.getId());
        //ShowwindowExcludeItem item = ShowwindowExcludeItem.find("userId = ? and numIid = ?", user.getId(), numIid2)
        //        .first();
        ShowwindowExcludeItem item = ShowwindowExcludeItem.findByNumIid(user.getId(), numIid2);
        if (item == null) {
            //new ShowwindowExcludeItem(user.getId(), numIid2).save();
            new ShowwindowExcludeItem(user.getId(), numIid2).jdbcSave();
        }
    }

    public static void remove(User user, long numIid) {
        clearCacheKey(user.getId());

        //ShowwindowExcludeItem item = ShowwindowExcludeItem.find("userId = ? and numIid = ?", user.getId(), numIid)
        //        .first();
        ShowwindowExcludeItem item = ShowwindowExcludeItem.findByNumIid(user.getId(), numIid);
        if (item != null) {
            //item.delete();
            JDBCBuilder.update(false, "delete from " + TABLE_NAME + " where userId = ? and numIid = ?", user.getId(),
                    numIid);
        }
    }

    public static boolean removeAll(User user) {
        clearCacheKey(user.getId());

        if (ShowwindowExcludeItem.findIdsByUser(user.getId()).size() == 0) {
            return false;
        }
        //return ShowwindowExcludeItem.delete("userId", user.getId()) != 0;
        return JDBCBuilder.update(false, "delete from " + TABLE_NAME + " where userId = ?", user.getId()) != 0;
    }

    private static final String SelectAllProperties = " userId,numIid ";

    private static ShowwindowExcludeItem findByNumIid(Long userId, Long numIid) {

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where userId = ? and numIid = ?";

        return new JDBCBuilder.JDBCExecutor<ShowwindowExcludeItem>(query, userId, numIid) {

            @Override
            public ShowwindowExcludeItem doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {

                    return new ShowwindowExcludeItem(rs.getLong(1), rs.getLong(2));

                } else {
                    return null;
                }

            }

        }.call();
    }

    public static Integer countByUserId(Long userId) {
        return findIdsByUser(userId).size();
    }

    private static List<ShowwindowExcludeItem> findListByUserId(Long userId, PageOffset po) {

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where userId = ? limit ?,?";

        return new JDBCBuilder.JDBCExecutor<List<ShowwindowExcludeItem>>(query, userId, po.getOffset(), po.getPs()) {

            @Override
            public List<ShowwindowExcludeItem> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<ShowwindowExcludeItem> list = new ArrayList<ShowwindowExcludeItem>();
                while (rs.next()) {
                    list.add(new ShowwindowExcludeItem(rs.getLong(1), rs.getLong(2)));
                }
                return list;
            }

        }.call();
    }

    public static TMResult findByUser(User user, int pn, int ps, boolean ensureItemInfo) {
        Long id = user.getId();
        PageOffset po = new PageOffset(pn, ps, 10);
        //List<ShowwindowExcludeItem> items = po.appendQueryByPage(ShowwindowExcludeItem.find("userId = ?", id), po);
        //int count = (int) ShowwindowExcludeItem.count("userId = ?", id);
        List<ShowwindowExcludeItem> items = ShowwindowExcludeItem.findListByUserId(user.getId(), po);
        int count = (int) JDBCBuilder.singleLongQuery("select count(*) from " + TABLE_NAME + " where userId = ?", id);
        log.info("[find excluded items:]" + items);

        if (ensureItemInfo) {
            return new TMResult(ItemDao.findByNumIids(id, toNumiidList(items)), count, po);
        } else {
            return new TMResult(items, count, po);
        }

    }

    public static List<Long> toNumiidList(List<ShowwindowExcludeItem> items) {
        if (CommonUtils.isEmpty(items)) {
            return ListUtils.EMPTY_LIST;
        }
        List<Long> ids = new ArrayList<Long>();
        for (ShowwindowExcludeItem showwindowExcludeItem : items) {
            ids.add(showwindowExcludeItem.numIid);
        }
        return ids;
    }

    public static long findExistId(Long userId, Long numIid) {

        String query = "select userId from " + TABLE_NAME + " where userId = ? and numIid = ?";

        return JDBCBuilder.singleLongQuery(query, userId, numIid);
    }

    public boolean jdbcSave() {
        clearCacheKey(userId);

        try {

            long existId = findExistId(this.userId, this.numIid);

            if (existId <= 0) {
                return this.rawInsert();
            } else {
                //return this.rawUpdate();
                return true;
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }

    public boolean rawInsert() {
        try {
            String insertSQL = "insert into `"
                    + TABLE_NAME
                    + "`(`userId`,`numIid`) values(?,?)";

            long id = JDBCBuilder.insert(insertSQL, userId, this.numIid);

            if (id > 0L) {
                return true;
            } else {
                return false;
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);

            return false;
        }
    }

    /*public boolean rawUpdate() {

        String updateSQL = "update `"
                + TABLE_NAME
                + "` set `numIid` = ? where `userId` = ?  ";

        long updateNum = JDBCBuilder.insert(updateSQL, this.numIid, this.userId);

        if (updateNum == 1) {

            return true;
        } else {

            return false;
        }
    }*/
}
