
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
import models.INumIid;
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
import cache.CacheVisitor;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;

import dao.item.ItemDao;

@Entity(name = ShowwindowMustDoItem.TABLE_NAME)
public class ShowwindowMustDoItem extends Model implements INumIid, CacheVisitor<Long> {

    public static final String TABLE_NAME = "showwindow_must_do_item";

    public static ShowwindowMustDoItem _instance = new ShowwindowMustDoItem();

    private static final Logger log = LoggerFactory.getLogger(ShowwindowMustDoItem.class);

    public ShowwindowMustDoItem() {
    }

    @Index(name = "userId")
    public Long userId;

    @Index(name = "numIid")
    public Long numIid;

    /**
     * this is the only function that will load the ids into cache
     * @param userId
     * @return
     */
    public static Set<Long> findIdsByUser(Long userId) {
        /*Set<Long> ids = getFromCache(userId);
        if (ids != null) {
            return ids;
        }

//        Set<Long> ids = new JDBCLongSetExecutor("select numIid from " + TABLE_NAME + " where userId = ?", userId)
        ids = new JDBCLongSetExecutor("select numIid from " + TABLE_NAME + " where userId = ?", userId).call();
        setCache(userId, ids);
        return ids;*/
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

    
    
    public static boolean exist(Long userId, Long numIid) {
        Set<Long> ids = findIdsByUser(userId);
        return ids.contains(numIid);
    }

//        return JDBCBuilder.singleLongQuery("select numIid from " + TABLE_NAME + " where numIid = ?", numIid) > 0L;

    public ShowwindowMustDoItem(Long userId, Long numIid) {
        super();
        this.userId = userId;
        this.numIid = numIid;
    }

    public static ShowwindowMustDoItem add(User user, long numIid2) {
        clearCache(user.getId());

        ShowwindowMustDoItem item = ShowwindowMustDoItem.findByNumIid(user.getId(), numIid2);
        if (item == null) {
            item = new ShowwindowMustDoItem(user.getId(), numIid2);
            item.jdbcSave();
        }

        return item;
    }

    public static void remove(Long userId, long numIid) {
//        ShowwindowMustDoItem item = ShowwindowMustDoItem.find("userId = ? and numIid = ?", user.getId(), numIid)
//                .first();
//        if (item != null) {
//            item.delete();
//            clearCache(user.getId());
//        }

        JDBCBuilder.update(false, "delete from " + TABLE_NAME + " where numIid = ?", numIid);
        clearCache(userId);
    }

    public static boolean removeAll(User user) {
        clearCache(user.getId());

        if (ShowwindowMustDoItem.findByUser(user.getId()).size() == 0) {
            return false;
        }

//        setCache(user.getId(), SetUtils.EMPTY_SET);
        //return ShowwindowMustDoItem.delete("userId", user.getId()) != 0;
        return JDBCBuilder.update(false, "delete from " + TABLE_NAME + " where userId = ?", user.getId()) != 0;
    }

    private static final String SelectAllProperties = " userId,numIid ";

    public static ShowwindowMustDoItem findByNumIid(Long userId, Long numIid) {

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where userId = ? and numIid = ?";

        return new JDBCBuilder.JDBCExecutor<ShowwindowMustDoItem>(query, userId, numIid) {

            @Override
            public ShowwindowMustDoItem doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {

                    return new ShowwindowMustDoItem(rs.getLong(1), rs.getLong(2));

                } else {
                    return null;
                }

            }

        }.call();
    }

    public static Integer countByUserId(Long userId) {
        return findByUser(userId).size();
    }

    public static List<ShowwindowMustDoItem> findListByUserId(Long userId, PageOffset po) {

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where userId = ? limit ?,?";

        return new JDBCBuilder.JDBCExecutor<List<ShowwindowMustDoItem>>(query, userId, po.getOffset(), po.getPs()) {

            @Override
            public List<ShowwindowMustDoItem> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<ShowwindowMustDoItem> list = new ArrayList<ShowwindowMustDoItem>();
                while (rs.next()) {
                    list.add(new ShowwindowMustDoItem(rs.getLong(1), rs.getLong(2)));
                }
                return list;
            }

        }.call();
    }

    public static List<ShowwindowMustDoItem> findAllByUserId(Long userId) {

        String query = "select " + SelectAllProperties + " from " + TABLE_NAME
                + " where userId = ?";

        return new JDBCBuilder.JDBCExecutor<List<ShowwindowMustDoItem>>(query, userId) {

            @Override
            public List<ShowwindowMustDoItem> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<ShowwindowMustDoItem> list = new ArrayList<ShowwindowMustDoItem>();
                while (rs.next()) {
                    list.add(new ShowwindowMustDoItem(rs.getLong(1), rs.getLong(2)));
                }
                return list;
            }

        }.call();
    }

    public static List<Long> findAllIdsByUserId(Long userId) {

        String query = "select numIid from " + TABLE_NAME
                + " where userId = ?";

        return new JDBCBuilder.JDBCExecutor<List<Long>>(query, userId) {

            @Override
            public List<Long> doWithResultSet(ResultSet rs)
                    throws SQLException {
                List<Long> list = new ArrayList<Long>();
                while (rs.next()) {
                    list.add(rs.getLong(1));
                }
                return list;
            }

        }.call();
    }

    public static List<ShowwindowMustDoItem> findByUser(Long id) {
        //return ShowwindowMustDoItem.find("userId = ?", id).fetch();
        return ShowwindowMustDoItem.findAllByUserId(id);
    }

    public static TMResult findByUser(Long id, int pn, int ps, boolean ensureItemInfo) {
        PageOffset po = new PageOffset(pn, ps, 10);
        //List<ShowwindowMustDoItem> items = po.appendQueryByPage(ShowwindowMustDoItem.find("userId = ?", id), po);
        List<ShowwindowMustDoItem> items = ShowwindowMustDoItem.findListByUserId(id, po);
        //int count = (int) ShowwindowMustDoItem.count("userId = ?", id);
        int count = (int) JDBCBuilder.singleLongQuery("select count(*) from " + TABLE_NAME + " where userId = ?", id);
        if (ensureItemInfo) {
            return new TMResult(ItemDao.findByNumIids(id, toNumiidList(items)), count, po);
        } else {
            return new TMResult(items, count, po);
        }

    }

    public static List<Long> toNumiidList(List<ShowwindowMustDoItem> items) {
        if (CommonUtils.isEmpty(items)) {
            return ListUtils.EMPTY_LIST;
        }
        List<Long> ids = new ArrayList<Long>();
        for (ShowwindowMustDoItem showwindowMustDoItem : items) {
            ids.add(showwindowMustDoItem.numIid);
        }
        return ids;
    }

    @Override
    public Long getNumIid() {
        return this.numIid;
    }

    @Override
    public String prefixKey() {
        return TABLE_NAME;
    }

    @Override
    public String expired() {
        return "120h";
    }

    @Override
    public String genKey(Long userId) {
        return prefixKey() + String.valueOf(userId);
    }

    private static Set<Long> getFromCache(Long userId) {
        return (Set<Long>) Cache.get(_instance.genKey(userId));
    }

    private static void setCache(Long userId, Set<Long> ids) {
        Cache.safeSet(_instance.genKey(userId), ids, _instance.expired());
    }

    private static void clearCache(Long userId) {
        Cache.safeDelete(_instance.genKey(userId));
    }

    public static long findExistId(Long userId, Long numIid) {

        String query = "select userId from " + TABLE_NAME + " where userId = ? and numIid = ?";

        return JDBCBuilder.singleLongQuery(query, userId, numIid);
    }

    public boolean jdbcSave() {
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
            String insertSQL = "insert into `" + TABLE_NAME + "`(`userId`,`numIid`) values(?,?)";

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
}
