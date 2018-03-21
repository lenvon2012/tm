/**
 * filename: DBUtil.java
 *
 * @author zhourunbo
 */

package transaction;

import codegen.CodeGenerator.DBDispatcher;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import result.PairLong;
import transaction.DBBuilder.DataSrc;

import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public class JDBCBuilder {

    private static final Logger log = LoggerFactory.getLogger(JDBCBuilder.class);

    public static final String TAG = "JDBCUtil";

    public static long update(DataSrc src, String query, Object... args) {
        return update(false, src, query, args);
    }

    public static long update(boolean debug, String query, Object... args) {
        return update(debug, DataSrc.BASIC, query, args);
    }

    public static long update(boolean debug, DataSrc src, String query, Object... args) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rset = null;

        try {
            conn = DBBuilder.getConn(src);
            ps = conn.prepareStatement(query);
            setArgs(ps, args);
            if (debug) {
                log.error("update sql:" + ps.toString());
            }

            int res = ps.executeUpdate();

            if (debug) {
                log.error("insert result:" + res);
            }

            if (res <= 0) {
                return 0L;
            } else {
                return (long) res;
            }
        } catch (SQLException e) {
            log.warn("Error SQL :" + ps, e);
        } finally {
            closeAll(rset, ps, conn);
        }
        return 0L;
    }

//    public static long insert(boolean debug, String query, Object... args) {
//        return insert(debug, DataSrc.BASIC, query, args);
//    }

    public static long insert(boolean debug, DataSrc src, String query, Object... args) {
        return insert(debug, true, src, query, args);
    }


    public static long insert(boolean debug, boolean isKeyGenerated, String query, Object... args) {
        return insert(debug, isKeyGenerated, DataSrc.BASIC, query, args);
    }

    public static long insert(boolean debug, boolean isKeyGenerated, DataSrc src, String query, Object... args) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rset = null;

        try {
            conn = DBBuilder.getConn(src);
            ps = conn.prepareStatement(query, isKeyGenerated ? Statement.RETURN_GENERATED_KEYS
                    : Statement.NO_GENERATED_KEYS);
            setArgs(ps, args);
            if (debug) {
                log.warn("insert sql:" + ps.toString());
            }

            int res = ps.executeUpdate();

            if (debug) {
                log.warn("insert result:" + res);
            }

            if (res <= 0) {
                return 0L;
            }
            if (!isKeyGenerated) {
                return res;
            }

            try {
                rset = ps.getGeneratedKeys();
                if (rset.next()) {
                    return rset.getLong(1);
                } else {
                    return (long) res;
                }
            } catch (SQLException e) {
                log.warn(e.getMessage());
                return 1L;
            }

        } catch (SQLException e) {
            log.warn("Error SQL :" + ps, e);
        } finally {
            closeAll(rset, ps, conn);
        }
        return 0L;
    }

    public static long insert(String query, Object... args) {
        return insert(false, query, args);
    }

    public static long insert(boolean debug, String query, Object... args) {
        return insert(false, DataSrc.BASIC, query, args);
    }

    public static long insert(DataSrc src, String query, Object... args) {
        return insert(false, src, query, args);
    }

    public static long singleLongQuery(String query, Object... args) {
        return singleLongQuery(DataSrc.BASIC, query, args);

    }

    public static int singleIntQuery(DataSrc src, String query, Object... args) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBBuilder.getConn(src);
            ps = conn.prepareStatement(query);
            setArgs(ps, args);

            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            log.warn("Error SQL :" + ps, e);
        } finally {
            closeAll(rs, ps, conn);
        }
        return -1;
    }

    public static long singleLongQuery(DataSrc src, String query, Object... args) {
        return singleLongQuery(false, src, query, args);
    }

    public static long singleLongQuery(boolean debug, DataSrc src, String query, Object... args) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBBuilder.getConn(src);
            ps = conn.prepareStatement(query);
            setArgs(ps, args);

            if (debug) {
                log.info("[Build Query :]" + ps.toString());
            }
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0L;
        } catch (SQLException e) {
            log.warn("Error SQL :" + ps, e);
        } finally {
            closeAll(rs, ps, conn);
        }
        return -1L;
    }

    public static double singleDoubleQuery(DataSrc src, String query, Object... args) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBBuilder.getConn(src);
            ps = conn.prepareStatement(query);
            setArgs(ps, args);

//            log.info("[Build Query :]" + ps.toString());

            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            log.warn("Error SQL :" + ps, e);
        } finally {
            closeAll(rs, ps, conn);
        }
        return 0.0d;
    }

    public static String singleStringQuery(String query, Object... args) {
        return singleStringQuery(DataSrc.BASIC, query, args);

    }

    /**
     * 检查查询能否执行，从而判断字段是否存在
     * @param src
     * @param query
     * @param args
     * @return
     */
    public static boolean checkExecuteQuery(DataSrc src, String query, Object... args) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBBuilder.getConn(src);
            ps = conn.prepareStatement(query);
            setArgs(ps, args);

//            log.info("[Build Query :]" + ps.toString());

            rs = ps.executeQuery();

            return true;
        } catch (SQLException e) {
            log.warn("Error SQL :" + ps, e);
        } finally {
            closeAll(rs, ps, conn);
        }
        return false;
    }

    public static String singleStringQuery(DataSrc src, String query, Object... args) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBBuilder.getConn(src);
            ps = conn.prepareStatement(query);
            setArgs(ps, args);

//            log.info("[Build Query :]" + ps.toString());

            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString(1);
            }
            return null;
        } catch (SQLException e) {
            log.warn("Error SQL :" + ps, e);
        } finally {
            closeAll(rs, ps, conn);
        }
        return null;
    }

    public static PairLong pairLongQuery(DataSrc src, String query, Object... args) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBBuilder.getConn(src);
            ps = conn.prepareStatement(query);
            setArgs(ps, args);

            rs = ps.executeQuery();

            if (rs.next()) {
                return new PairLong(rs.getLong(1), rs.getLong(2));
            } else {
                return null;
            }

        } catch (SQLException e) {
            log.warn("Error SQL :" + ps, e);
        } finally {
            closeAll(rs, ps, conn);
        }
        return null;
    }

    public static class JDBCMapStringExecutor extends JDBCExecutor<Map<Long, String>> {

        public JDBCMapStringExecutor(String query, Object... params) {
            super(query, params);
        }

        public JDBCMapStringExecutor(DBDispatcher dispatcher, String query, Object... params) {
            super(dispatcher, query, params);
        }

        @Override
        public Map<Long, String> doWithResultSet(ResultSet rs) throws SQLException {
            final Map<Long, String> mapRes = new HashMap<Long, String>();
            while (rs.next()) {
                mapRes.put(rs.getLong(1), rs.getString(2));
            }
            return mapRes;
        }
    }


    public static abstract class JDBCExecutor<T> implements Callable<T> {

        protected String query;

        protected Object[] params;

        protected boolean debug = false;

        protected DataSrc src = DataSrc.BASIC;

        public JDBCExecutor(String query, Object... params) {
            this.query = query;
            this.params = params;

        }

        public JDBCExecutor(boolean debug, DBDispatcher dispatcher, String query, Object... params) {
            this.debug = debug;
            this.src = dispatcher.getSrc();
            this.query = dispatcher.rebuildQuery(query);
            this.params = params;
        }

        public JDBCExecutor(CodeGenerator.DBDispatcher dispatcher, String query, Object... params) {
            this.src = dispatcher.getSrc();
            this.query = dispatcher.rebuildQuery(query);
            this.params = params;
        }

        public JDBCExecutor(DBDispatcher dispatcher, String query, Object... params) {
            this.src = dispatcher.getSrc();
            this.query = dispatcher.rebuildQuery(query);
            this.params = params;
        }

        public JDBCExecutor(boolean debug, DataSrc src, String query, Object... params) {
            this.query = query;
            this.params = params;
            this.src = src;
            this.debug = debug;
        }

        public JDBCExecutor(DataSrc src, String query, Object... params) {
            this.query = query;
            this.params = params;
            this.src = src;
        }

        public JDBCExecutor(boolean debug, String query, Object... params) {
            this.debug = debug;
            this.query = query;
            this.params = params;
        }

        public T call() {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                conn = DBBuilder.getConn(src);
                ps = conn.prepareStatement(query);
                setArgs(ps, params);

                if (debug) {
                    log.error("query ps: " + ps);
                }

                rs = ps.executeQuery();
                return doWithResultSet(rs);
            } catch (SQLException e) {
                log.warn("Error SQL :" + ps, e);
            } finally {
                closeAll(rs, ps, conn);
            }
            return null;
        }

        public abstract T doWithResultSet(ResultSet rs) throws SQLException;
    }

    public static final void closeAll(ResultSet rs, PreparedStatement ps, Connection conn) {
        closeQuitely(rs);
        closeQuitely(ps);
        closeQuitely(conn);
    }

    public static final void closeQuitely(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
        }
    }

    public static final void closeQuitely(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
        }
    }

    public static final void closeQuitely(PreparedStatement ps) {
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (Exception e) {
        }
    }

    private static void setArgs(PreparedStatement ps, Object... args) throws SQLException {
        if (ArrayUtils.isEmpty(args)) {
            return;
        }

        for (int i = 0; i < args.length; i++) {
            Object obj = args[i];

            if (obj == null) {
                ps.setString(i + 1, null);
            } else if (obj instanceof Integer) {
                ps.setInt(i + 1, ((Integer) obj).intValue());
            } else if (obj instanceof Long) {
                ps.setLong(i + 1, ((Long) obj).longValue());
            } else if (obj instanceof String) {
                ps.setString(i + 1, obj.toString());
            } else if (obj instanceof Boolean) {
                ps.setBoolean(i + 1, ((Boolean) obj).booleanValue());
            } else if (obj instanceof Double) {
                ps.setDouble(i + 1, ((Double) obj).doubleValue());
            } else if (obj instanceof BigDecimal) {
                ps.setBigDecimal(i + 1, (BigDecimal) obj);
            }
        }
    }

    public static void buildIdcount(ResultSet rs, Map<Long, Long> idCountMap) throws SQLException {

        while (rs.next()) {

            Long id = rs.getLong(1);
            long count = rs.getLong(2);
            Long existCount = idCountMap.get(id);
            if (existCount == null) {
                idCountMap.put(id, count);
            } else {
                idCountMap.put(id, count + existCount);
            }
        }

    }

    public static class JDBCLongSetExecutor extends JDBCExecutor<Set<Long>> {

        public JDBCLongSetExecutor(String query, Object... params) {
            super(query, params);
        }

        public JDBCLongSetExecutor(DataSrc src, String query, Object... params) {
            super(query, params);
            this.src = src;
        }

        public JDBCLongSetExecutor(DBDispatcher dp, String query, Object... params) {
            super(query, params);
            this.src = dp.getSrc();
        }


        @Override
        public Set<Long> doWithResultSet(ResultSet rs) throws SQLException {
            final Set<Long> res = new HashSet<Long>();
            while (rs.next()) {
                res.add(rs.getLong(1));
            }
            return res;
        }
    }

    public static class JDBCSetStringExecutor extends JDBCExecutor<Set<String>> {

        public JDBCSetStringExecutor(DataSrc src, String query, Object... params) {
            super(query, params);
            this.src = src;
        }

        public JDBCSetStringExecutor(DBDispatcher dp, String query, Object... params) {
            super(query, params);
            this.src = dp.getSrc();
        }

        @Override
        public Set<String> doWithResultSet(ResultSet rs) throws SQLException {
            final Set<String> res = new HashSet<String>();
            while (rs.next()) {
                res.add(rs.getString(1));
            }
            return res;
        }
    }

}
