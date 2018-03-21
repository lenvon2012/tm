
package jdbcexecutorwrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import transaction.DBBuilder.DataSrc;
import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.DBDispatcher;

public class JDBCMapLongExecutor extends JDBCExecutor<Map<Long, Long>> {

    public JDBCMapLongExecutor(String query, Object... params) {
        super(query, params);
    }

    public JDBCMapLongExecutor(DBDispatcher dispatcher, String query, Object... params) {
        super(dispatcher, query, params);
    }

    public JDBCMapLongExecutor(DataSrc src, String query, Object... params) {
        super(src, query, params);
    }

    @Override
    public Map<Long, Long> doWithResultSet(ResultSet rs) throws SQLException {
        final Map<Long, Long> mapRes = new HashMap<Long, Long>();
        while (rs.next()) {
            mapRes.put(rs.getLong(1), rs.getLong(2));
        }
        return mapRes;
    }
}
