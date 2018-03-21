package dao.defense;

import java.sql.ResultSet;
import java.sql.SQLException;

import models.defense.BlackListExplain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.JDBCBuilder.JDBCExecutor;

public class BlackListExplainDao {

    private static final Logger log = LoggerFactory.getLogger(BlackListExplainDao.class);

    private static final String BlackList_Explain_Sql = " select id, userId, tradeExplain, ts from "
            + BlackListExplain.TABLE_NAME;

    public static BlackListExplain findByUserId(Long userId) {
        String sql = BlackList_Explain_Sql + " where userId=?";
        return new JDBCExecutor<BlackListExplain>(BlackListExplain.dp, sql, userId) {
            @Override
            public BlackListExplain doWithResultSet(ResultSet rs) throws SQLException {

                if (rs.next()) {
                    return parseBlackListExplain(rs);
                }
                return null;
            }
        }.call();
    }

    private static BlackListExplain parseBlackListExplain(ResultSet rs) {
        try {
            Long id = rs.getLong(1);
            Long userId = rs.getLong(2);
            String tradeExplain = rs.getString(3);
            Long ts = rs.getLong(4);
            BlackListExplain blackListExplain = new BlackListExplain(userId, tradeExplain, ts);
            blackListExplain.setId(id);

            return blackListExplain;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
}
