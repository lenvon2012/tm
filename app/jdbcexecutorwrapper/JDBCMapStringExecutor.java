package jdbcexecutorwrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.DBDispatcher;

public class JDBCMapStringExecutor extends JDBCExecutor<Map<Long, String>> {

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
