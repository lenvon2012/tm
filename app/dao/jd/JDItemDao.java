/**
 * 
 */
package dao.jd;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.item.ItemPlay;
import models.jd.JDItemPlay;
import models.jd.JDUser;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import result.TMResult;
import transaction.JDBCBuilder;
import codegen.CodeGenerator.DBDispatcher;

import com.ciaosir.client.pojo.PageOffset;

import dao.item.ItemDao;

/**
 * @author navins
 * @date: Nov 3, 2013 3:48:38 PM
 */
public class JDItemDao {

    public static final DBDispatcher dp = JDItemPlay.dp;

    static String Select_Query = "select numIid,uid,shopId,ts,status,price,stock_num,title,picURL,uid,skuId from jd_item_%s";

    public static TMResult queryUserItems(Long uid, String word, int popularized, int sort, PageOffset po) {
        String query = JDItemPlay.genShardQuery(Select_Query, uid) + " where uid = ?";
        if (!StringUtils.isEmpty(word)) {
            query += " and title like '%" + StringEscapeUtils.escapeSql(word) + "%'";
        }
        if (popularized == 0) {
            query += " and status&3 = 0";
        } else if (popularized == 1) {
            query += " and status&1 > 0";
        } else if (popularized == 2) {
            query += " and status&2 > 0";
        }

        if (sort == 5) {
            query += " order by price desc";
        } else if (sort == 6) {
            query += " order by price asc";
        } else {
            query += " order by numIid desc";
        }
        query += " limit ? offset ?";

        List<JDItemPlay> list = new JDBCBuilder.JDBCExecutor<List<JDItemPlay>>(dp, query, uid, po.getPs(),
                po.getOffset()) {

            @Override
            public List<JDItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<JDItemPlay> list = new ArrayList<JDItemPlay>();
                while (rs.next()) {
                    list.add(new JDItemPlay(rs));
                }
                return list;
            }

        }.call();

        long count = countUserItems(uid, word, popularized);

        return new TMResult(list, (int) count, po);
    }

    static String Count_Query = "select count(*) from jd_item_%s";

    public static long countUserItems(Long uid, String word, int polularized) {
        String query = JDItemPlay.genShardQuery(Count_Query, uid) + " where uid = ?";
        if (!StringUtils.isEmpty(word)) {
            query += " and title like '%" + StringEscapeUtils.escapeSql(word) + "%'";
        }
        if (polularized >= 0) {
            query += " and status&3 = " + polularized;
        }
        
        long count = dp.singleLongQuery(query, uid);
        return count;
    }

    static String Update_Pop_Query = "update jd_item_%s set status = status | ? where uid = ? ";

    public static void addPopularized(Long uid, int popularized, String numIidList) {
        if (StringUtils.isEmpty(numIidList)) {
            return;
        }

        String query = JDItemPlay.genShardQuery(Update_Pop_Query, uid) + " and numIid in (" + numIidList + ")";
        System.out.println(query);
        dp.update(query, popularized, uid);
    }
    
    static String Update_Query = "update jd_item_%s set status = ? where uid = ? ";

    public static void removePopularized(Long uid) {
        if (uid == null) {
            return;
        }

        String query = JDItemPlay.genShardQuery(Update_Query, uid);
        dp.update(query, 0, uid);
    }
    
    public static void removePopularized(Long uid, String numIidList) {
        if (uid == null || StringUtils.isEmpty(numIidList)) {
            return;
        }

        String query = JDItemPlay.genShardQuery(Update_Query, uid) + " and numIid in (" + numIidList + ")";
        dp.update(query, 0, uid);
    }

    public static long countPopularized(Long uid, int popularized) {
        String query = JDItemPlay.genShardQuery(Count_Query, uid) + " where uid = ? ";
        if (popularized == 1) {
            query += "and (status&1) > 0";
        } else if (popularized == 2) {
            query += "and (status&2) > 0";
        } else {
            query += "and (status&3) > 0";
        }
        return dp.singleLongQuery(query, uid);
    }
    
    public static List<JDItemPlay> findToBePopularized(Long uid, String numIidList) {
        if (StringUtils.isEmpty(numIidList)) {
            return null;
        }
        String query = JDItemPlay.genShardQuery(Select_Query, uid) + " where uid = ? and numIid in (" + numIidList + ")";
        List<JDItemPlay> list = new JDBCBuilder.JDBCExecutor<List<JDItemPlay>>(dp, query, uid) {

            @Override
            public List<JDItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<JDItemPlay> list = new ArrayList<JDItemPlay>();
                while (rs.next()) {
                    list.add(new JDItemPlay(rs));
                }
                return list;
            }

        }.call();
        return list;
    }

    public static JDItemPlay queryJDItem(Long uid, Long numIid) {
        String query = JDItemPlay.genShardQuery(Select_Query, uid) + " where uid = ? and numIid = ?";

        return new JDBCBuilder.JDBCExecutor<JDItemPlay>(dp, query, uid, numIid) {

            @Override
            public JDItemPlay doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new JDItemPlay(rs);
                }
                return null;
            }

        }.call();

    }

    public static List<ItemPlay> findByNumIidList(final JDUser user, String numIidList) {
        if (StringUtils.isBlank(numIidList)) {
            return ListUtils.EMPTY_LIST;
        }

        String[] numIidL = numIidList.split(",");
        List<Long> numIids = new ArrayList<Long>();
        for (int i = 0; i < numIidL.length; i++) {
            numIids.add((Long) Long.parseLong(numIidL[i]));
        }
        List<ItemPlay> items = new ArrayList<ItemPlay>();
        for (int i = 0; i < numIids.size(); i++) {
            items.add(ItemDao.findByNumIid(user.getId(), numIids.get(i)));
        }
        return items;
    }
    
    public static JDItemPlay queryJDSkuItem(Long uid, Long skuId) {
        String query = JDItemPlay.genShardQuery(Select_Query, uid) + " where uid = ? and skuId = ?";

        return new JDBCBuilder.JDBCExecutor<JDItemPlay>(dp, query, uid, skuId) {

            @Override
            public JDItemPlay doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return new JDItemPlay(rs);
                }
                return null;
            }

        }.call();

    }
    
    
    public static void deleteNoSkuItems(Long uid) {
        String query = JDItemPlay.genShardQuery("delete from jd_item_%s where uid = ? and skuId <= 0", uid);
        
        JDBCBuilder.update(false, query, uid);
    }

}
