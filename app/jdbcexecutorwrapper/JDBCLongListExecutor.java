
package jdbcexecutorwrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.ListUtils;

import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.DBDispatcher;

public class JDBCLongListExecutor extends JDBCExecutor<List<Long>> {

    public JDBCLongListExecutor(String query, Object... params) {
        super(query, params);
    }

    public JDBCLongListExecutor(DBDispatcher dispatcher, String query, Object... params) {
        super(dispatcher, query, params);
    }

    @Override
    public List<Long> doWithResultSet(ResultSet rs) throws SQLException {
        List<Long> res = null;
        while (rs.next()) {
            if (res == null) {
                res = new ArrayList<Long>();
            }
            res.add(rs.getLong(1));
        }
        return res == null ? ListUtils.EMPTY_LIST : res;
    }
}
