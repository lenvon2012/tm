package dao.defense;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.defense.DefenseWarn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.JDBCBuilder.JDBCExecutor;

public class DefenseWarnDao {
    private static final Logger log = LoggerFactory.getLogger(DefenseWarnDao.class);

    private static final String Defense_Warn_Sql = " select id, userId, telephone, remark, ts from "
            + DefenseWarn.TABLE_NAME;
    private static final String Defense_Warn_Count_Sql = " select count(*) from " + DefenseWarn.TABLE_NAME;
    private static final String Defense_Warn_Delete_Sql = " delete from " + DefenseWarn.TABLE_NAME;

    public static List<DefenseWarn> findByUserId(Long userId) {
        String sql = Defense_Warn_Sql + " where userId=? ";
        List<DefenseWarn> warnList = new JDBCExecutor<List<DefenseWarn>>(DefenseWarn.dp, sql, userId) {
            @Override
            public List<DefenseWarn> doWithResultSet(ResultSet rs) throws SQLException {
                List<DefenseWarn> list = new ArrayList<DefenseWarn>();
                while (rs.next()) {
                    DefenseWarn defenseWarn = parseDefenseWarn(rs);
                    if (defenseWarn != null)
                        list.add(defenseWarn);
                }
                return list;
            }
        }.call();

        return warnList;
    }

    public static DefenseWarn findById(Long userId, Long id) {
        String sql = Defense_Warn_Sql + " where userId=? and id=? ";
        return new JDBCExecutor<DefenseWarn>(DefenseWarn.dp, sql, userId, id) {
            @Override
            public DefenseWarn doWithResultSet(ResultSet rs) throws SQLException {

                if (rs.next()) {
                    return parseDefenseWarn(rs);
                }
                return null;
            }
        }.call();
    }

    public static long countByUserId(Long userId) {
        String sql = Defense_Warn_Count_Sql + " where userId=? ";
        long count = DefenseWarn.dp.singleLongQuery(sql, userId);
        return count;
    }

    public static boolean deleteById(Long userId, Long id) {
        String sql = Defense_Warn_Delete_Sql + " where userId=? and id=? ";
        long result = DefenseWarn.dp.update(false, sql, userId, id);
        if (result == 0L)
            return false;
        return true;
    }

    private static DefenseWarn parseDefenseWarn(ResultSet rs) {
        try {
            Long id = rs.getLong(1);
            Long userId = rs.getLong(2);
            String telePhone = rs.getString(3);
            String remark = rs.getString(4);
            Long ts = rs.getLong(5);
            DefenseWarn defenseWarn = new DefenseWarn(userId, telePhone, remark, ts);
            defenseWarn.setId(id);

            return defenseWarn;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

}
