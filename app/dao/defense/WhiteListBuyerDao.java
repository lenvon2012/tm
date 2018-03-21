package dao.defense;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.defense.WhiteListBuyer;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;
import transaction.JDBCBuilder.JDBCExecutor;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;

public class WhiteListBuyerDao {

    private static final Logger log = LoggerFactory.getLogger(WhiteListBuyerDao.class);

    private static final String WhiteList_Buyer_Sql = " select id, userId, buyerName, ts, remark from "
            + WhiteListBuyer.TABLE_NAME;

    private static final String WhiteList_Buyer_Count_Sql = " select count(*) from " + WhiteListBuyer.TABLE_NAME;

    private static final String WhiteList_Buyer_Delete_Sql = " delete from " + WhiteListBuyer.TABLE_NAME;

    public static TMResult findWhiteListBuyersByName(Long userId, String buyerName, PageOffset po) {
        String query = " userId=? ";
        if (!StringUtils.isEmpty(buyerName)) {
            query += " and " + formatBuyerNameLike(buyerName);
        }
        String sql = WhiteList_Buyer_Sql + " where " + query + " order by ts desc limit ? offset ? ";
        // log.info(sql);
        List<WhiteListBuyer> buyerList = new JDBCExecutor<List<WhiteListBuyer>>(WhiteListBuyer.dp, sql, userId,
                po.getPs(), po.getOffset()) {
            @Override
            public List<WhiteListBuyer> doWithResultSet(ResultSet rs) throws SQLException {
                List<WhiteListBuyer> list = new ArrayList<WhiteListBuyer>();
                while (rs.next()) {
                    WhiteListBuyer blackListBuyer = parseWhiteListBuyer(rs);
                    if (blackListBuyer != null)
                        list.add(blackListBuyer);
                }
                return list;
            }
        }.call();

        String countSql = WhiteList_Buyer_Count_Sql + " where " + query;
        long count = WhiteListBuyer.dp.singleLongQuery(countSql, userId);

        TMResult tmResult = new TMResult(buyerList, (int) count, po);
        return tmResult;
    }

    public static WhiteListBuyer findByBuyerName(Long userId, String buyerName) {
        String sql = WhiteList_Buyer_Sql + " where userId=? and buyerName=? ";
        return new JDBCExecutor<WhiteListBuyer>(WhiteListBuyer.dp, sql, userId, buyerName) {
            @Override
            public WhiteListBuyer doWithResultSet(ResultSet rs) throws SQLException {

                if (rs.next()) {
                    return parseWhiteListBuyer(rs);
                }
                return null;
            }
        }.call();
    }

    public static WhiteListBuyer findWhiteListBuyersById(Long userId, Long id) {
        String sql = WhiteList_Buyer_Sql + " where userId=? and id=? ";
        return new JDBCExecutor<WhiteListBuyer>(WhiteListBuyer.dp, sql, userId, id) {
            @Override
            public WhiteListBuyer doWithResultSet(ResultSet rs) throws SQLException {

                if (rs.next()) {
                    return parseWhiteListBuyer(rs);
                }
                return null;
            }
        }.call();
    }

    public static List<WhiteListBuyer> findWhiteListBuyers(Long userId) {
        String sql = WhiteList_Buyer_Sql + " where userId=? ";
        List<WhiteListBuyer> buyerList = new JDBCExecutor<List<WhiteListBuyer>>(WhiteListBuyer.dp, sql, userId) {
            @Override
            public List<WhiteListBuyer> doWithResultSet(ResultSet rs) throws SQLException {
                List<WhiteListBuyer> list = new ArrayList<WhiteListBuyer>();
                while (rs.next()) {
                    WhiteListBuyer WhiteListBuyer = parseWhiteListBuyer(rs);
                    if (WhiteListBuyer != null)
                        list.add(WhiteListBuyer);
                }
                return list;
            }
        }.call();

        return buyerList;
    }

    public static long countWhiteListBuyers(Long userId) {
        String sql = WhiteList_Buyer_Count_Sql + " where userId = ?";
        return WhiteListBuyer.dp.singleLongQuery(sql, userId);
    }

    public static List<WhiteListBuyer> findWhiteListBuyersByIds(Long userId, List<Long> idList) {
        if (CommonUtils.isEmpty(idList))
            return new ArrayList<WhiteListBuyer>();
        String idQuery = formatIdQuery(idList);

        String sql = WhiteList_Buyer_Sql + " where userId=? and " + idQuery;
        List<WhiteListBuyer> buyerList = new JDBCExecutor<List<WhiteListBuyer>>(WhiteListBuyer.dp, sql, userId) {
            @Override
            public List<WhiteListBuyer> doWithResultSet(ResultSet rs) throws SQLException {
                List<WhiteListBuyer> list = new ArrayList<WhiteListBuyer>();
                while (rs.next()) {
                    WhiteListBuyer blackListBuyer = parseWhiteListBuyer(rs);
                    if (blackListBuyer != null)
                        list.add(blackListBuyer);
                }
                return list;
            }
        }.call();

        return buyerList;
    }

    public static boolean deleteByIds(Long userId, List<Long> idList) {
        if (CommonUtils.isEmpty(idList))
            return true;
        String idQuery = formatIdQuery(idList);
        String sql = WhiteList_Buyer_Delete_Sql + " where userId=? and " + idQuery;
        long result = WhiteListBuyer.dp.update(false, sql, userId);
        if (result == 0L)
            return false;
        return true;
    }

    private static String formatBuyerNameLike(String buyerName) {
        String like = " buyerName like '%" + buyerName + "%' ";
        return like;
    }

    private static String formatIdQuery(List<Long> idList) {
        String idQuery = "";
        for (Long id : idList) {
            if (!StringUtils.isEmpty(idQuery))
                idQuery += ",";
            idQuery += id;
        }
        idQuery = " id in (" + idQuery + ") ";
        return idQuery;
    }

    private static WhiteListBuyer parseWhiteListBuyer(ResultSet rs) {
        try {
            Long id = rs.getLong(1);
            Long userId = rs.getLong(2);
            String buyerName = rs.getString(3);
            Long ts = rs.getLong(4);
            String remark = rs.getString(5);
            WhiteListBuyer buyer = new WhiteListBuyer(userId, buyerName, ts, remark);
            buyer.setId(id);

            return buyer;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
}
