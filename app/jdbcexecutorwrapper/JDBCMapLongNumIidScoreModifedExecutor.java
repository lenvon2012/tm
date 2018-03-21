package jdbcexecutorwrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import transaction.JDBCBuilder.JDBCExecutor;
import codegen.CodeGenerator.DBDispatcher;
import dao.item.sub.NumIidScoreModifed;

public class JDBCMapLongNumIidScoreModifedExecutor extends JDBCExecutor<Map<Long, NumIidScoreModifed>> {

    public JDBCMapLongNumIidScoreModifedExecutor(String query, Object... params) {
        super(query, params);
    }

    public JDBCMapLongNumIidScoreModifedExecutor(DBDispatcher dispatcher, String query, Object... params) {
        super(dispatcher, query, params);
    }

    @Override
    public Map<Long, NumIidScoreModifed> doWithResultSet(ResultSet rs) throws SQLException {
        final Map<Long, NumIidScoreModifed> mapRes = new HashMap<Long, NumIidScoreModifed>();
        while (rs.next()) {
            NumIidScoreModifed model = new NumIidScoreModifed(rs.getLong(1), rs.getInt(2), rs.getLong(3));
            mapRes.put(rs.getLong(1), model);
        }
        return mapRes;
    }
}
