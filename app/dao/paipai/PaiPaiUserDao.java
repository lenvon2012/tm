package dao.paipai;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import models.paipai.PaiPaiUser;
import models.paipai.PaiPaiUser.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.JDBCBuilder.JDBCExecutor;
import transaction.JPATransactionManager;

import com.ciaosir.client.CommonUtils;

/**
 * @author haoyongzh
 * 
 */
public class PaiPaiUserDao {

    private static final Logger log = LoggerFactory.getLogger(PaiPaiUserDao.class);

    public static final String TAG = "PaiPaiUserDao";

    public static final String Select_SQL = "select `id`,`nick`,`accessToken`,`refreshToken`,`firstLoginTime`,"
            + "`type`,`version` from " + PaiPaiUser.TABLE_NAME;

    public static List<PaiPaiUser> fetch(String whereQuery, Object... args) {
        return new JDBCExecutor<List<PaiPaiUser>>(Select_SQL + " where " + whereQuery, args) {

            @Override
            public List<PaiPaiUser> doWithResultSet(ResultSet rs) throws SQLException {
                List<PaiPaiUser> list = new ArrayList<PaiPaiUser>();
                while (rs.next()) {
                    list.add(new PaiPaiUser(rs));
                }
                return list;
            }

        }.call();
    }

    public static PaiPaiUser findById(Long id) {

        String sql = Select_SQL + " where id = ? ";

        if (id == null) {
            return null;
        }
        if (id <= 0L) {
            return null;
        }

        return new JDBCExecutor<PaiPaiUser>(sql, id) {
            @Override
            public PaiPaiUser doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new PaiPaiUser(rs);
                }
                return null;
            }

        }.call();
    }
    
    public static final List<PaiPaiUser> findAutoCommentOn() {
    	
    	String sql = Select_SQL+" where type & " + Type.IS_AUTOCOMMENT_ON + " > 0 and type & " + Type.IS_VALID
                + " > 0 order by id desc ";
    	
        return new JDBCExecutor<List<PaiPaiUser>>(sql) {

            @Override
            public List<PaiPaiUser> doWithResultSet(ResultSet rs) throws SQLException {
                List<PaiPaiUser> list = new ArrayList<PaiPaiUser>();
                while (rs.next()) {
                    list.add(new PaiPaiUser(rs));
                }
                return list;
            }

        }.call();  
    }

    public static final List<PaiPaiUser> findValidList(int offset, int limit) {
        List<PaiPaiUser> res = fetch(" type & " + PaiPaiUser.Type.IS_VALID + " > 0 order by id desc limit ? offset ?",
                limit, offset);
        return res;
    }

    public static abstract class PaiPaiUserBatchOper implements Callable<Boolean> {
        public int offset = 0;

        public int limit = 32;

        protected long sleepTime = 2000L;

        public PaiPaiUserBatchOper(int offset, int limit) {
            this.offset = offset;
            this.limit = limit;
        }

        public PaiPaiUserBatchOper(int limit) {
            this.limit = limit;
        }

        public List<PaiPaiUser> findNext() {
            return PaiPaiUserDao.findValidList(offset, limit);
        }

        public abstract void doForEachUser(PaiPaiUser user);

        @Override
        public Boolean call() {

            while (true) {

                List<PaiPaiUser> findList = findNext();
                if (CommonUtils.isEmpty(findList)) {
                    return Boolean.TRUE;
                }

                for (PaiPaiUser user : findList) {
                    offset++;
                    doForEachUser(user);
                }

                JPATransactionManager.clearEntities();
                CommonUtils.sleepQuietly(sleepTime);

            }

        }
    }
}
