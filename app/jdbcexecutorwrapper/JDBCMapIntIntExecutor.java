package jdbcexecutorwrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.DBDispatcher;

public class JDBCMapIntIntExecutor extends JDBCExecutor<Map<Integer, Integer>> {

    public JDBCMapIntIntExecutor(String query, Object... params) {
        super(query, params);
    }

    public JDBCMapIntIntExecutor(DBDispatcher dispatcher, String query, Object... params) {
        super(dispatcher, query, params);
    }

    @Override
    public Map<Integer, Integer> doWithResultSet(ResultSet rs) throws SQLException {
        final Map<Integer, Integer> mapRes = new HashMap<Integer, Integer>();
        while (rs.next()) {
            mapRes.put(rs.getInt(1), rs.getInt(2));
        }
        return mapRes;
    }
}
