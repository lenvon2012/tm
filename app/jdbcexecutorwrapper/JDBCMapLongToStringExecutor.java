package jdbcexecutorwrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import transaction.JDBCBuilder.JDBCExecutor;

public class JDBCMapLongToStringExecutor extends JDBCExecutor<Map<Long, String>> {

    public JDBCMapLongToStringExecutor(String query, Object... params) {
        super(query, params);
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
