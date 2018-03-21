
package dao.defense;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.defense.DefenseLog;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.JDBCBuilder.JDBCExecutor;

import com.ciaosir.client.pojo.PageOffset;

import dao.item.ItemDao;

public class DefenseLogDao {

    private static final Logger log = LoggerFactory.getLogger(DefenseLogDao.class);

    private static final String DEFENSE_LOG_SQL = " select id,userId,tradeId,numIid,buyerName,opMsg,status,ts,closeFailReason from "
            + DefenseLog.TABLE_NAME;

    private static final String DEFENSE_LOG_COUNT_SQL = " select count(*) from " + DefenseLog.TABLE_NAME;

    public static List<DefenseLog> findDefenseLogByUserId(Long userId) {
        String sql = DEFENSE_LOG_SQL + " where userId = ? order by ts desc ";
//        log.info(sql);
        List<DefenseLog> logList = new JDBCExecutor<List<DefenseLog>>(DefenseLog.dp, sql, userId) {
            @Override
            public List<DefenseLog> doWithResultSet(ResultSet rs) throws SQLException {
                List<DefenseLog> list = new ArrayList<DefenseLog>();
                while (rs.next()) {
                    DefenseLog defenseLog = parseBlackListLog(rs);
                    if (defenseLog != null)
                        list.add(defenseLog);
                }
                return list;
            }
        }.call();

        return logList;
    }
    
    public static List<DefenseLog> findDefenseLogByRules(Long userId, Long tradeId, String title, String buyerName,
            Long startTs, Long endTs, int defenseStatus, PageOffset po) {
        String query = formatQuery(userId, tradeId, title, buyerName, startTs, endTs, defenseStatus);

        String sql = DEFENSE_LOG_SQL + " where " + query + " order by ts desc limit ? offset ? ";
//        log.info(sql);
        List<DefenseLog> logList = new JDBCExecutor<List<DefenseLog>>(DefenseLog.dp, sql, userId, po.getPs(),
                po.getOffset()) {
            @Override
            public List<DefenseLog> doWithResultSet(ResultSet rs) throws SQLException {
                List<DefenseLog> list = new ArrayList<DefenseLog>();
                while (rs.next()) {
                    DefenseLog defenseLog = parseBlackListLog(rs);
                    if (defenseLog != null)
                        list.add(defenseLog);
                }
                return list;
            }
        }.call();

        return logList;
    }

    public static long countDefenseLogByRules(Long userId, Long tradeId, String title, String buyerName, Long startTs,
            Long endTs, int defenseStatus) {
        String query = formatQuery(userId, tradeId, title, buyerName, startTs, endTs, defenseStatus);
        String countSql = DEFENSE_LOG_COUNT_SQL + " where " + query;
        long count = DefenseLog.dp.singleLongQuery(countSql, userId);
        return count;
    }

    private static String formatQuery(Long userId, Long tradeId, String title, String buyerName, Long startTs,
            Long endTs, int defenseStatus) {
    	// 0: 全部  1: 拦截成功  2: 拦截失败
        if (defenseStatus != 0 && defenseStatus  != 1 && defenseStatus != 2) {
        	defenseStatus = 0;
        }
        String query = " userId=? ";
        if (!StringUtils.isEmpty(title)) {
            query += " and " + formatTitleLike(userId, title);
        }
        if (!StringUtils.isEmpty(buyerName)) {
            query += " and " + formatBuyerNameLike(buyerName);
        }
        if (startTs != null) {
            query += " and ts>=" + startTs + " ";
        }
        if (endTs != null && endTs.longValue() > 0L) {
            query += " and ts < " + endTs + " ";
        }
        if (tradeId != null && tradeId > 0) {
            query += " and tradeId=" + tradeId + " ";
        }
        if (defenseStatus == 1) {
        	query += " and status&1 > 0 ";
        } else if(defenseStatus == 2) {
        	query += " and status&1 = 0 ";
        }
        return query;
    }

    private static String formatTitleLike(Long userId, String title) {
        if (StringUtils.isEmpty(title))
            return "";
        String itemTable = ItemDao.genShardQuery("item%s", userId);
        String query = " numIid in (select numIid from " + itemTable + " where title like '%" + title + "%') ";

        return query;
    }

    private static String formatBuyerNameLike(String buyerName) {
        String like = " buyerName like '%" + buyerName + "%' ";
        return like;
    }

    private static DefenseLog parseBlackListLog(ResultSet rs) {
        try {
            Long id = rs.getLong(1);
            Long userId = rs.getLong(2);
            Long tradeId = rs.getLong(3);
            Long numIid = rs.getLong(4);
            String buyerName = rs.getString(5);
            String opMsg = rs.getString(6);
            int status = rs.getInt(7);
            Long ts = rs.getLong(8);
            DefenseLog defenseLog = new DefenseLog(userId, tradeId, numIid, buyerName, opMsg, status, ts);
            defenseLog.setCloseFailReason(rs.getString(9));
            defenseLog.setId(id);

            return defenseLog;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
}
