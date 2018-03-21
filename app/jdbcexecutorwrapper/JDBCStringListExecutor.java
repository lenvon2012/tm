package jdbcexecutorwrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.ListUtils;

import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.DBDispatcher;

public class JDBCStringListExecutor extends JDBCExecutor<List<String>> {

    public JDBCStringListExecutor(String query, Object... params) {
        super(query, params);
    }

    public JDBCStringListExecutor(DBDispatcher dispatcher, String query, Object... params) {
        super(dispatcher, query, params);
    }

    @Override
    public List<String> doWithResultSet(ResultSet rs) throws SQLException {
        List<String> res = null;
        while (rs.next()) {
            if (res == null) {
                res = new ArrayList<String>();
            }
            res.add(rs.getString(1));
        }
        return res == null ? ListUtils.EMPTY_LIST : res;
    }
}
