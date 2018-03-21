package dao.defense;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.defense.ItemBuyLimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.JDBCBuilder.JDBCExecutor;

public class ItemBuyLimitDao {
    private static final Logger log = LoggerFactory.getLogger(ItemBuyLimitDao.class);

    private static final String USER_TRADE_QUERY = " select userId,numIid,daysLimit,tradeNum,itemMinNum,itemMaxNum,vipLevel,ts,status,buyerName,closeReason from "
            + ItemBuyLimit.TABLE_NAME;

    public static List<ItemBuyLimit> findByUserId(Long userId) {
        String sql = USER_TRADE_QUERY + " where userId = ?";
        return new JDBCExecutor<List<ItemBuyLimit>>(ItemBuyLimit.dp, sql, userId) {
            @Override
            public List<ItemBuyLimit> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemBuyLimit> list = new ArrayList<ItemBuyLimit>();

                while (rs.next()) {
                    ItemBuyLimit itemLimit = new ItemBuyLimit(rs);
                    if (itemLimit != null) {
                        list.add(itemLimit);
                    }
                }
                return list;
            }
        }.call();
    }

    public static ItemBuyLimit findByUserIdNumIid(Long userId, Long numIid) {
        String sql = USER_TRADE_QUERY + " where userId = ? and numIid = ?";
        return new JDBCExecutor<ItemBuyLimit>(ItemBuyLimit.dp, sql, userId, numIid) {
            @Override
            public ItemBuyLimit doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    ItemBuyLimit tradeLog = new ItemBuyLimit(rs);
                    if (tradeLog != null) {
                        return tradeLog;
                    }
                }
                return null;
            }
        }.call();
    }

    public static ItemBuyLimit findOrUseDefaultOption(Long userId, Long numIid) {
        ItemBuyLimit itemBuyLimit = ItemBuyLimitDao.findByUserIdNumIid(userId, numIid);
        if (itemBuyLimit == null) {
            itemBuyLimit = ItemBuyLimit.EMPTY;
            itemBuyLimit.setUserId(userId);
            itemBuyLimit.setNumIid(numIid);
            itemBuyLimit.setCloseReason(ItemBuyLimit.DEFAULT_CLOSEREASON);
        }
        return itemBuyLimit;
    }

    private static final String USER_NUMIID_QUERY = " select sum(num) from " + ItemBuyLimit.TABLE_NAME
            + " where userId = ? and numIid = ?";

    public static Long findUserBuyTotalNum(Long userId, Long numIid) {
        long count = ItemBuyLimit.dp.singleLongQuery(USER_NUMIID_QUERY, userId, numIid);
        return count;
    }

    public static ItemBuyLimit findByTradeId(Long tid) {
        String sql = USER_TRADE_QUERY + " where userId=?";
        return new JDBCExecutor<ItemBuyLimit>(ItemBuyLimit.dp, sql, tid) {
            @Override
            public ItemBuyLimit doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new ItemBuyLimit(rs);
                }
                return null;
            }
        }.call();
    }

    public static void updateDefenderOption(ItemBuyLimit option) {

        option.jdbcSave();
    }

    private static final String DELETE_LIMIT_QUERY = " delete from  " + ItemBuyLimit.TABLE_NAME
            + " where userId = ? and numIid = ? ";

    public static void deleteDefenderOption(Long userId, Long numIid) {

        ItemBuyLimit.dp.update(false, DELETE_LIMIT_QUERY, userId, numIid);
    }

}
