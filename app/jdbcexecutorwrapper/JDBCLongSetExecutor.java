
package jdbcexecutorwrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.SetUtils;

import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.DBDispatcher;

public class JDBCLongSetExecutor extends JDBCExecutor<Set<Long>> {

    public JDBCLongSetExecutor(String query, Object... params) {
        super(query, params);
    }

    public JDBCLongSetExecutor(DataSrc src, String query, Object... params) {
        super(src, query, params);
    }

    public JDBCLongSetExecutor(DBDispatcher dispatcher, String query, Object... params) {
        super(dispatcher, query, params);
    }
    public JDBCLongSetExecutor(boolean debug, DBDispatcher dispatcher, String query, Object... params) {
        super(dispatcher, query, params);
        this.debug = debug;
    }

    @Override
    public Set<Long> doWithResultSet(ResultSet rs) throws SQLException {
        Set<Long> res = null;
        while (rs.next()) {
            if (res == null) {
                res = new HashSet<Long>();
            }

            res.add(rs.getLong(1));
        }
        return res == null ? SetUtils.EMPTY_SET : res;
    }
}
