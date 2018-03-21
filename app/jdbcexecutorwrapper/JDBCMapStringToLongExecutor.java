package jdbcexecutorwrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.DBDispatcher;

public class JDBCMapStringToLongExecutor extends JDBCExecutor<Map<String, Long>> {

    public JDBCMapStringToLongExecutor(String query, Object... params) {
        super(query, params);
    }

    public JDBCMapStringToLongExecutor(DBDispatcher dispatcher, String query, Object... params) {
        super(dispatcher, query, params);
    }

    @Override
    public Map<String, Long> doWithResultSet(ResultSet rs) throws SQLException {
        final Map<String, Long> mapRes = new HashMap<String, Long>();
        while (rs.next()) {
            mapRes.put(rs.getString(1), rs.getLong(2));
        }
        return mapRes;
    }
}