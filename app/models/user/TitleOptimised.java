package models.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Transient;

import models.item.ItemPlay;
import models.oplog.TitleOptimiseLog;

import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.DBDispatcher;
import codegen.CodeGenerator.PolicySQLGenerator;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;

import dao.item.ItemDao;

/**
 * 
 * @author lzl
 * 保存用户类型，比如是否优化过等
 *
 */
@Entity(name = TitleOptimised.TABLE_NAME)
public class TitleOptimised extends Model implements PolicySQLGenerator {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(TitleOptimised.class);

    @Transient
    public static final String TABLE_NAME = "title_optimised";

    @Transient
    public static TitleOptimised EMPTY = new TitleOptimised();

    @Transient
    public static DBDispatcher dp = new DBDispatcher(DataSrc.QUOTA, EMPTY);
    
    public static class Status {
    	// 不查询宝贝标题优化状态
        public final static int NORMAL = 1;
        // 标题已优化	
        public final static int OPTIMISED =2;
        // 标题未优化
        public final static int UN_OPTIMISED = 4;
    }
    
    @Index(name="userId")
    public Long userId;

    @Index(name="numIid")
    public Long numIid;
    
    public boolean isOptimised;
    
    // 最近优化时间
    public Long ts;

    public TitleOptimised() {

    }

    public TitleOptimised(Long userId, Long numIid, boolean isOptimised, Long ts) {
        this.userId = userId;
        this.numIid = numIid;
        this.isOptimised = isOptimised;
        this.ts = ts;
    }
    
    public boolean isTitleOptimised(){
        return this.isOptimised;
    }

    public void setTitleOptimised(boolean toBeOn){
        this.isOptimised = toBeOn;
    }
    
    public Long getTs() {
		return ts;
	}

	public void setTs(Long ts) {
		this.ts = ts;
	}

	@Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getIdColumn() {
        return "id";
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean jdbcSave() {

        try {
            long existdId = findExistId(this.userId, this.numIid);

            if (existdId == 0L) {
                return this.rawInsert();
            } else {
            	if(this.id == null) {
            		this.id = existdId;
            	}
                return this.rawUpdate();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

    }

    static String EXIST_ID_QUERY = "select id from " + TABLE_NAME + " where userId = ? and numIid = ?";

    public static long findExistId(Long userId, Long numIid) {
        return dp.singleLongQuery(EXIST_ID_QUERY, userId, numIid);
    }

    static String insertSQL = "insert into `title_optimised`(`userId`, `numIid`, `isOptimised`, `ts`) values(?,?,?,?)";

    public boolean rawInsert() {

        long id = dp.update(false, insertSQL, this.userId, this.numIid, this.isOptimised, this.ts);

//        log.info("[Insert TitleOptimised userId:]" + userId  + ": ]" + this.userId);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId );
            return false;
        }
    }

    public static TitleOptimised findByUserId(Long userId, Long numIid) {

        String query = "select userId,numIid,isOptimised,ts,id from " + TABLE_NAME
                + " where userId = ? and numIid = ?";

        return new JDBCBuilder.JDBCExecutor<TitleOptimised>(dp, query, userId, numIid) {

            @Override
            public TitleOptimised doWithResultSet(ResultSet rs)
                    throws SQLException {

                if (rs.next()) {
                	TitleOptimised op = new TitleOptimised(rs.getLong(1), rs.getLong(2),
                			rs.getBoolean(3), rs.getLong(4));
                	op.setId(rs.getLong(5));
                	return op; 
                } else {
                    return null;
                }
            }

        }.call();
    }
    
    public static long countByUserId(Long userId) {

        String query = "select count(*) from " + TABLE_NAME
                + " where userId = ?";

        return dp.singleLongQuery(query, userId);
    }
    
    public boolean rawUpdate() {
        long updateNum = dp.update(false, "update `title_optimised` set `userId` = ?, `numIid` = ?, `isOptimised` = ?, " +
        		"`ts` = ? where `id` = ? ", this.userId, this.numIid,
                this.isOptimised, this.ts, this.id);

        if (updateNum == 1) {
//            log.info("[Update TitleOptimised userId:]" + userId );

            return true;
        } else {
//            log.error("update failed...for userId:" + this.userId );
            return false;
        }
    }

    public static long rawDelete(Long userId, Long numIid, Long expiredTs) {
        long deleteNum = dp.insert(false, "delete from `title_optimised` where `userId` = ? and numIid = ?", userId, numIid);
        log.error("Delete userId for " + userId );
        return deleteNum;

    }

    @Override
    public String getIdName() {
        return "id";
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getNumIid() {
		return numIid;
	}

	public void setNumIid(Long numIid) {
		this.numIid = numIid;
	}

	public boolean isOptimised() {
		return isOptimised;
	}

	public void setOptimised(boolean isOptimised) {
		this.isOptimised = isOptimised;
	}

	@Override
    public String getTableHashKey() {
        // TODO Auto-generated method stub
        return null;
    }

	public static void updateUserTitleOptimise(User user) {
    	List<ItemPlay> items = ItemDao.findByUserId(user.getId());
        if(CommonUtils.isEmpty(items)) {
      	  return;
        }
        for(ItemPlay itemPlay : items) {
      	  TitleOptimised optimised = TitleOptimised.findByUserId(user.getId(), 
      			  itemPlay.getNumIid());
      	  log.info("optimised for item : " + itemPlay.getTitle() + " is " + (optimised == null));
      	  if(optimised == null) {
      		  long count = TitleOptimiseLog.count("userId = ? and numIid = ?", 
        			  itemPlay.getUserId(), itemPlay.getNumIid());
      		  if(count > 0) {
      			  new TitleOptimised(user.getId(), itemPlay.getNumIid(), true,
      					  System.currentTimeMillis()).jdbcSave();
      		  } else {
      			  new TitleOptimised(user.getId(), itemPlay.getNumIid(), false,
    					  System.currentTimeMillis()).jdbcSave();
      		  }
      	  }
        }
    }
	
	public static Set<Long> findNumIidsByUserId(Long userId) {
        String sql = "select numIid from title_optimised where userId = ? ";

        return new JDBCExecutor<Set<Long>>(dp, sql, userId) {

            @Override
            public Set<Long> doWithResultSet(ResultSet rs) throws SQLException {
                Set<Long> numIids = new HashSet<Long>();
                while (rs.next()) {
                    numIids.add(rs.getLong(1));
                }
                return numIids;
            }
        }.call();
    }
	
	public static Set<Long> findNumIidsByUserIdOffset(Long userId, PageOffset po) {
        String sql = "select numIid from title_optimised where userId = ? order by ts desc limit ?,?";
        return new JDBCExecutor<Set<Long>>(dp, sql, userId, po.getOffset(), po.getPs()) {

            @Override
            public Set<Long> doWithResultSet(ResultSet rs) throws SQLException {
                Set<Long> numIids = new HashSet<Long>();
                while (rs.next()) {
                    numIids.add(rs.getLong(1));
                }
                return numIids;
            }
        }.call();
    }
}
