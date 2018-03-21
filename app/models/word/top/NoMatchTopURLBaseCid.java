package models.word.top;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.Entity;
import javax.persistence.Id;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;

import dao.UserDao;

import play.db.jpa.GenericModel;
import transaction.JDBCBuilder;
import transaction.JPATransactionManager;
import transaction.JDBCBuilder.JDBCExecutor;

@Entity(name = NoMatchTopURLBaseCid.TABLE_NAME)
public class NoMatchTopURLBaseCid extends GenericModel{

	private static final Logger log = LoggerFactory.getLogger(NoMatchTopURLBaseCid.class);

    public static final String TAG = "NoMatchTopURLBaseCid";
    
    public static final String TABLE_NAME = "no_match_top_url_base_cid";
    
    @Id
    Long cid;
    
    public Long getCid() {
		return cid;
	}

	public void setCid(Long cid) {
		this.cid = cid;
	}

	public NoMatchTopURLBaseCid(Long cid) {
		super();
		this.cid = cid;
	}

	public boolean jdbcSave() {
        try {
            
            long existId = findExistId(this.cid);
            
            if (existId <= 0) {
                return this.rawInsert();
            } 
            return true;
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }

    public boolean rawInsert() {
        try {
            String insertSQL = "insert into `"
                    + TABLE_NAME
                    + "`(`cid`) values(?)";
            
            long id = JDBCBuilder.insert(insertSQL, cid);
            
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
    
    public static long findExistId(Long cid) {

        String query = "select cid from " + TABLE_NAME + " where cid = ? ";

        return JDBCBuilder.singleLongQuery(query, cid);
    }
    
    public static List<NoMatchTopURLBaseCid> findValidList(int offset, int limit) {
    	return new JDBCExecutor<List<NoMatchTopURLBaseCid>>(
    			"select cid from no_match_top_url_base_cid limit ?,? ", offset, limit) {

            @Override
            public List<NoMatchTopURLBaseCid> doWithResultSet(ResultSet rs) throws SQLException {
                List<NoMatchTopURLBaseCid> list = new ArrayList<NoMatchTopURLBaseCid>();
                while (rs.next()) {
                    list.add(new NoMatchTopURLBaseCid(rs.getLong(1)));
                }
                return list;
            }

        }.call();
    }

    public static abstract class NoMatchTopURLBaseCidBatchOper implements Callable<Boolean> {
        public int offset = 0;

        public int limit = 32;

        protected long sleepTime = 500L;

        public NoMatchTopURLBaseCidBatchOper(int offset, int limit) {
            this.offset = offset;
            this.limit = limit;
        }

        public NoMatchTopURLBaseCidBatchOper(int limit) {
            this.limit = limit;
        }

        public NoMatchTopURLBaseCidBatchOper(int offset, int limit, long sleepTime) {
            super();
            this.offset = offset;
            this.limit = limit;
            this.sleepTime = sleepTime;
        }

        public List<NoMatchTopURLBaseCid> findNext() {
            return NoMatchTopURLBaseCid.findValidList(offset, limit);
        }

        public abstract void doForEachUser(NoMatchTopURLBaseCid cat);

        @Override
        public Boolean call() {

            while (true) {

                List<NoMatchTopURLBaseCid> findList = findNext();
                if (CommonUtils.isEmpty(findList)) {
                    return Boolean.TRUE;
                }

                for (NoMatchTopURLBaseCid cat : findList) {
                    offset++;
                    doForEachUser(cat);
                }

                JPATransactionManager.clearEntities();
                CommonUtils.sleepQuietly(sleepTime);
            }

        }
    }

}
