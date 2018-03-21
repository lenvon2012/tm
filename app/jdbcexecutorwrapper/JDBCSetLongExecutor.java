package jdbcexecutorwrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.DBDispatcher;

public class JDBCSetLongExecutor  extends JDBCExecutor<Set<Long>> {

    public JDBCSetLongExecutor(String query, Object... params) {
        super(query, params);
    }

    public JDBCSetLongExecutor(DBDispatcher dispatcher, String query, Object... params) {
        super(dispatcher, query, params);
    }

    @Override
    public Set<Long> doWithResultSet(ResultSet rs) throws SQLException {
        final Set<Long> setRes = new HashSet<Long>();
        while (rs.next()) {
            setRes.add(rs.getLong(1));
        }

        return setRes;
    }
}
