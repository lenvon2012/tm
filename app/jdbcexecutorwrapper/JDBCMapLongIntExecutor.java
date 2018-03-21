package jdbcexecutorwrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.DBDispatcher;

public class JDBCMapLongIntExecutor extends JDBCExecutor<Map<Long, Integer>> {

    public JDBCMapLongIntExecutor(String query, Object... params) {
        super(query, params);
    }

    public JDBCMapLongIntExecutor(DBDispatcher dispatcher, String query, Object... params) {
        super(dispatcher, query, params);
    }

    @Override
    public Map<Long, Integer> doWithResultSet(ResultSet rs) throws SQLException {
        final Map<Long, Integer> mapRes = new HashMap<Long, Integer>();
        while (rs.next()) {
            mapRes.put(rs.getLong(1), rs.getInt(2));
        }
        return mapRes;
    }
}
