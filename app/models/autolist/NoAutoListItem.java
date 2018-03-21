
package models.autolist;

import java.util.Set;

import javax.persistence.Entity;

import jdbcexecutorwrapper.JDBCLongSetExecutor;
import models.INumIid;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import result.TMResult;
import transaction.JDBCBuilder;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;

import dao.item.ItemDao;


@Entity(name = NoAutoListItem.TABLE_NAME)
public class NoAutoListItem extends Model implements INumIid {

    private static final Logger log = LoggerFactory.getLogger(NoAutoListItem.class);
    
    public static final String TABLE_NAME = "no_auto_list_item";

    @Index(name = "userId")
    public Long userId;

    @Index(name = "numIid")
    public Long numIid;
    
    @Index(name = "planId")
    private long planId = 0;

    public static Set<Long> findIdsByUser(Long userId, long planId) {
        return new JDBCLongSetExecutor("select numIid from " + TABLE_NAME + " where userId = ? and planId = ?", userId, planId).call();

    }
    
    
    private static long findExistId(Long userId, Long numIid, long planId) {
        
        String query = "select id from " + TABLE_NAME + " where userId = ? and numIid = ? and planId = ? ";
        
        return JDBCBuilder.singleLongQuery(query, userId, numIid, planId);
    }
    
    public boolean jdbcSave() {

        try {
            
            long existId = findExistId(this.userId, this.numIid, this.planId);
            
            if (existId <= 0L) {
                return this.rawInsert();
            } else {
                return true;
            }
            
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
        
    }
    
    
    public boolean rawInsert() {
        
        String insertSql = "insert into `" + TABLE_NAME + "`(`userId`, `numIid`, `planId`) values(?,?,?)";

        long id = JDBCBuilder.insert(insertSql, userId, numIid, planId);
        
        if (id > 0L) {
            this.id = id;
            return true;
        } else {
            
            log.error("insert " + TABLE_NAME + " fails!!");
            
            return false;
        }
        
    }
    

    public NoAutoListItem(Long userId, Long numIid, long planId) {
        super();
        this.userId = userId;
        this.numIid = numIid;
        this.planId = planId;
    }

    public static void add(User user, long numIid2, long planId) {

        NoAutoListItem noItem = new NoAutoListItem(user.getId(), numIid2, planId);
        noItem.jdbcSave();
    }

    public static void remove(User user, long numIid, long planId) {

        String deleteSql = " delete from " + TABLE_NAME + " where userId = ? and numIid = ? and planId = ?";
        JDBCBuilder.update(false, deleteSql, user.getId(), numIid, planId);
    }

    public static boolean removeAll(User user, long planId) {
        String deleteSql = " delete from " + TABLE_NAME + " where userId = ? and planId = ?";
        JDBCBuilder.update(false, deleteSql, user.getId(), planId);
        
        return true;
    }
    
    public static boolean removeNumIids(User user, Set<Long> numIidSet, long planId) {
        if (CommonUtils.isEmpty(numIidSet)) {
            return true;
        }
        
        String deleteSql = " delete from " + TABLE_NAME + " where userId = ? and planId = ? " +
        		" and numIid in (" + StringUtils.join(numIidSet, ",") + ") ";
        JDBCBuilder.update(false, deleteSql, user.getId(), planId);
        
        return true;
    }
    
    public static boolean updateZeroPlanId(Long userId, long planId) {
        String sql = " update " + TABLE_NAME + " set planId = ? where userId = ? and planId = 0";
        
        JDBCBuilder.update(false, sql, planId, userId);
        
        return true;
    }
    

    public static long countNoAutoListItem(Long userId, long planId) {
        String query = "select count(*) from " + TABLE_NAME + " where userId = ? and planId = ?";
        
        long count = JDBCBuilder.singleLongQuery(query, userId, planId);
        
        return count;
    }

    public static TMResult findItemByUserId(Long userId, long planId, int pn, int ps) {
        PageOffset po = new PageOffset(pn, ps, 10);
        
        String query = " select numIid from " + TABLE_NAME + " where userId = ? and planId = ? limit ?, ?";
        Set<Long> numIidSet = new JDBCLongSetExecutor(query, userId, planId, po.getOffset(), po.getPs()).call();
        
        int count = (int) countNoAutoListItem(userId, planId);
        
        return new TMResult(ItemDao.findByNumIids(userId, numIidSet), count, po);

    }
    
    
    
    
    public static Set<Long> findNumIidsByUser(Long userId, long planId) {
//      return ShowwindowExcludeItem.find("userId = ?", userId).fetch();
      return new JDBCLongSetExecutor("select numIid from " + TABLE_NAME + " where userId = ? and planId = ? ", userId, planId).call();
    }
    
    public static Set<Long> findByUserAndNumIid(long userId, long numIid, long planId) {
    	return new JDBCLongSetExecutor("select numIid from " + TABLE_NAME + " where userId = ? and numIid=? and planId = ? ", userId, numIid, planId).call();
    }


    @Override
    public Long getNumIid() {
        return this.numIid;
    }
}
