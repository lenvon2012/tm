package dao.defense;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.defense.ItemPass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.JDBCBuilder.JDBCExecutor;

public class ItemPassDao {

    private static final Logger log = LoggerFactory.getLogger(ItemPassDao.class);

    private static final String ITEM_PASS_QUERY = " select userId,numIid,status,ts from " + ItemPass.TABLE_NAME;
    private static final String UPDATE_ITEM_PASS_QUERY = " update " + ItemPass.TABLE_NAME
            + " set status = ? where userId = ? and numIid = ?";
    private static final String DELETE_ITEM_PASS_QUERY = " delete from " + ItemPass.TABLE_NAME;

    public static List<ItemPass> findByUserId(Long userId) {
        String sql = ITEM_PASS_QUERY + " where userId = ? and status > 0";
        return new JDBCExecutor<List<ItemPass>>(ItemPass.dp, sql, userId) {
            @Override
            public List<ItemPass> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPass> list = new ArrayList<ItemPass>();

                while (rs.next()) {
                    ItemPass itemLimit = new ItemPass(rs);
                    if (itemLimit != null) {
                        list.add(itemLimit);
                    }
                }
                return list;
            }
        }.call();
    }

    public static ItemPass findByUserIdNumIid(Long userId, Long numIid) {
        String sql = ITEM_PASS_QUERY + " where userId = ? and numIid = ?";
        return new JDBCExecutor<ItemPass>(ItemPass.dp, sql, userId, numIid) {
            @Override
            public ItemPass doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new ItemPass(rs);
                }
                return null;
            }
        }.call();
    }

    public static boolean updateItemPass(Long userId, Long numIid, int status) {
        ItemPass itemPass = findByUserIdNumIid(userId, numIid);
        if (itemPass == null) {
            itemPass = new ItemPass(userId, numIid, status);
            return itemPass.jdbcSave();
        } else {
            long result = ItemPass.dp.update(false, UPDATE_ITEM_PASS_QUERY, status, userId, numIid);
            if (result == 0L) {
                return false;
            }
        }
        return true;
    }

    public static boolean deleteItemPass(Long userId, Long numIid) {
        String sql = DELETE_ITEM_PASS_QUERY + " where userId = ? and numIid = ?";
        long result = ItemPass.dp.update(false, sql, userId);
        if (result == 0L) {
            return false;
        }
        return true;
    }

}
