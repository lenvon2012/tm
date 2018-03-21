
package dao.refund;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.op.TMRefundName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;

import com.ciaosir.client.pojo.PageOffset;

public class TMRefundDao {

    public final static Logger log = LoggerFactory.getLogger(TMRefundDao.class);

    //`wangwang`,`created`,`updated`,`upname`,`reason`,`assessor`,`app`,`status`
    public static String RefundTrade_Sql = " select id, wangwang, created, updated, upname, reason, assessor, app, status, amount,advice from "
            + TMRefundName.TABLE_NAME;

    private static final String RefundTrade_Count_Sql = " select count(*) from " + TMRefundName.TABLE_NAME;

    public static TMResult findRefundTradeListW(String wangwang, PageOffset po) {
        String sql = RefundTrade_Sql + " where wangwang = ? order by created desc" + " limit ?, ?";
        String countSql = RefundTrade_Count_Sql + " where wangwang = ?";
        long count = JDBCBuilder.singleLongQuery(countSql, wangwang);
        List<TMRefundName> RefundTradeList = new JDBCExecutor<List<TMRefundName>>(sql, wangwang, po.getOffset(),
                po.getPs()) {
            @Override
            public List<TMRefundName> doWithResultSet(ResultSet rs) throws SQLException {
                List<TMRefundName> list = new ArrayList<TMRefundName>();
                while (rs.next()) {
                    TMRefundName RefundTradeList = parseRefundTradeList(rs);
                    if (RefundTradeList != null)
                        list.add(RefundTradeList);
                }
                return list;
            }
        }.call();
        TMResult tmResult = new TMResult(RefundTradeList, (int) count, po);
        return tmResult;
    }

    public static TMResult findRefundTradeListA(String app, PageOffset po) {
        String sql = RefundTrade_Sql + " where app = ? order by created desc" + " limit ?, ?";
        String countSql = RefundTrade_Count_Sql + " where app = ?";
        long count = JDBCBuilder.singleLongQuery(countSql, app);
        List<TMRefundName> RefundTradeList = new JDBCExecutor<List<TMRefundName>>(sql, app, po.getOffset(), po.getPs()) {
            @Override
            public List<TMRefundName> doWithResultSet(ResultSet rs) throws SQLException {
                List<TMRefundName> list = new ArrayList<TMRefundName>();
                while (rs.next()) {
                    TMRefundName RefundTradeList = parseRefundTradeList(rs);
                    if (RefundTradeList != null)
                        list.add(RefundTradeList);
                }
                return list;
            }
        }.call();
        TMResult tmResult = new TMResult(RefundTradeList, (int) count, po);
        return tmResult;
    }

    public static TMResult findRefundTradeListS(String status, PageOffset po) {
        String sql = RefundTrade_Sql + " where status = ? order by created desc" + " limit ?, ?";
        String countSql = RefundTrade_Count_Sql + " where status = ?";
        long count = JDBCBuilder.singleLongQuery(countSql, status);
        List<TMRefundName> RefundTradeList = new JDBCExecutor<List<TMRefundName>>(sql, status, po.getOffset(),
                po.getPs()) {
            @Override
            public List<TMRefundName> doWithResultSet(ResultSet rs) throws SQLException {
                List<TMRefundName> list = new ArrayList<TMRefundName>();
                while (rs.next()) {
                    TMRefundName RefundTradeList = parseRefundTradeList(rs);
                    if (RefundTradeList != null)
                        list.add(RefundTradeList);
                }
                return list;
            }
        }.call();
        TMResult tmResult = new TMResult(RefundTradeList, (int) count, po);
        return tmResult;
    }

    public static TMResult findRefundTradeListWA(String wangwang, String app, PageOffset po) {
        String sql = RefundTrade_Sql + " where wangwang  = ? and app = ? order by created desc" + " limit ?, ?";
        String countSql = RefundTrade_Count_Sql + " where wangwang  = ? and app = ?";
        long count = JDBCBuilder.singleLongQuery(countSql, wangwang, app);
        List<TMRefundName> RefundTradeList = new JDBCExecutor<List<TMRefundName>>(sql, wangwang, app, po.getOffset(),
                po.getPs()) {
            @Override
            public List<TMRefundName> doWithResultSet(ResultSet rs) throws SQLException {
                List<TMRefundName> list = new ArrayList<TMRefundName>();
                while (rs.next()) {
                    TMRefundName RefundTradeList = parseRefundTradeList(rs);
                    if (RefundTradeList != null)
                        list.add(RefundTradeList);
                }
                return list;
            }
        }.call();
        TMResult tmResult = new TMResult(RefundTradeList, (int) count, po);
        return tmResult;
    }

    public static TMResult findRefundTradeListWS(String wangwang, String status, PageOffset po) {
        String sql = RefundTrade_Sql + " where wangwang  = ? and status = ? order by created desc" + " limit ?, ?";
        String countSql = RefundTrade_Count_Sql + " where wangwang  = ? and status = ?";
        long count = JDBCBuilder.singleLongQuery(countSql, wangwang, status);
        List<TMRefundName> RefundTradeList = new JDBCExecutor<List<TMRefundName>>(sql, wangwang, status,
                po.getOffset(), po.getPs()) {
            @Override
            public List<TMRefundName> doWithResultSet(ResultSet rs) throws SQLException {
                List<TMRefundName> list = new ArrayList<TMRefundName>();
                while (rs.next()) {
                    TMRefundName RefundTradeList = parseRefundTradeList(rs);
                    if (RefundTradeList != null)
                        list.add(RefundTradeList);
                }
                return list;
            }
        }.call();
        TMResult tmResult = new TMResult(RefundTradeList, (int) count, po);
        return tmResult;
    }

    public static TMResult findRefundTradeListAS(String app, String status, PageOffset po) {
        String sql = RefundTrade_Sql + " where app  = ? and status = ? order by created desc" + " limit ?, ?";
        String countSql = RefundTrade_Count_Sql + " where app  = ? and status = ?";
        long count = JDBCBuilder.singleLongQuery(countSql, app, status);
        List<TMRefundName> RefundTradeList = new JDBCExecutor<List<TMRefundName>>(sql, app, status, po.getOffset(),
                po.getPs()) {
            @Override
            public List<TMRefundName> doWithResultSet(ResultSet rs) throws SQLException {
                List<TMRefundName> list = new ArrayList<TMRefundName>();
                while (rs.next()) {
                    TMRefundName RefundTradeList = parseRefundTradeList(rs);
                    if (RefundTradeList != null)
                        list.add(RefundTradeList);
                }
                return list;
            }
        }.call();
        TMResult tmResult = new TMResult(RefundTradeList, (int) count, po);
        return tmResult;
    }

    public static TMResult findRefundTradeListWAS(String wangwang, String app, String status, PageOffset po) {
        String sql = RefundTrade_Sql + " where wangwang  = ? and app = ? and status = ? order by created desc"
                + " limit ?, ?";
        String countSql = RefundTrade_Count_Sql + " where wangwang  = ? and app = ? and status = ?";
        long count = JDBCBuilder.singleLongQuery(countSql, wangwang, app, status);
        List<TMRefundName> RefundTradeList = new JDBCExecutor<List<TMRefundName>>(sql, wangwang, app, status,
                po.getOffset(), po.getPs()) {
            @Override
            public List<TMRefundName> doWithResultSet(ResultSet rs) throws SQLException {
                List<TMRefundName> list = new ArrayList<TMRefundName>();
                while (rs.next()) {
                    TMRefundName RefundTradeList = parseRefundTradeList(rs);
                    if (RefundTradeList != null)
                        list.add(RefundTradeList);
                }
                return list;
            }
        }.call();
        TMResult tmResult = new TMResult(RefundTradeList, (int) count, po);
        return tmResult;
    }

    public static TMResult findRefundTradeListAll(PageOffset po) {
        String status = "已提交";
        String sql = RefundTrade_Sql + " where status = ? order by created desc" + " limit ?, ?";
        String countSql = RefundTrade_Count_Sql + " where status = ?";
        long count = JDBCBuilder.singleLongQuery(countSql, status);
        List<TMRefundName> RefundTradeList = new JDBCExecutor<List<TMRefundName>>(sql, status, po.getOffset(),
                po.getPs()) {
            @Override
            public List<TMRefundName> doWithResultSet(ResultSet rs) throws SQLException {
                List<TMRefundName> list = new ArrayList<TMRefundName>();
                while (rs.next()) {
                    TMRefundName RefundTradeList = parseRefundTradeList(rs);
                    if (RefundTradeList != null)
                        list.add(RefundTradeList);
                }
                return list;
            }
        }.call();

        TMResult tmResult = new TMResult(RefundTradeList, (int) count, po);
        return tmResult;

    }

    public static TMRefundName findRefundTradeListId(long id) {
        String sql = RefundTrade_Sql + " where id = ?";
        TMRefundName RefundTrade = new JDBCExecutor<TMRefundName>(sql, id) {
            @Override
            public TMRefundName doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    TMRefundName RefundTrade = parseRefundTradeList(rs);
                    return RefundTrade;
                }
                return null;
            }
        }.call();

        return RefundTrade;

    }

    public static boolean submitRefundTrade(String wangwang, String upname, String reason, String app, double amount) {
        TMRefundName RefundTrade = new TMRefundName(wangwang, upname, reason, app, amount);
        boolean success = RefundTrade.jdbcSave();
        return success;
    }
    
    public static TMRefundName findIsRefundByNick(String wangwang){
        String sql = RefundTrade_Sql + " where wangwang = ?";
        TMRefundName RefundTrade = new JDBCExecutor<TMRefundName>(sql, wangwang) {
            @Override
            public TMRefundName doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    TMRefundName RefundTrade = parseRefundTradeList(rs);
                    return RefundTrade;
                }
                return null;
            }
        }.call();

        return RefundTrade;
    	
    }

//    public static void updateRefundTrade(long id,String assessor){
//    	TMRefundName updateRefundTrade=new TMRefundName();
//    	 updateRefundTrade.updateId(id, assessor);   	
//    }

    private static TMRefundName parseRefundTradeList(ResultSet rs) {
        //(id,`wangwang`,`created`,`updated`,`upname`,`reason`,`assessor`,`app`,`status`)
        try {
            long id = rs.getLong(1);
            String wangwang = rs.getString(2);
            long created = rs.getLong(3);
            long updated = rs.getLong(4);
            String upname = rs.getString(5);
            String reason = rs.getString(6);
            String assessor = rs.getString(7);
            String app = rs.getString(8);
            String status = rs.getString(9);
            double amount = rs.getDouble(10);
            String advice = rs.getString(11);

            TMRefundName RefundTrade = new TMRefundName(id, wangwang, created, updated, upname, reason
                    , assessor, app, status, amount, advice);

            return RefundTrade;

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }

    }

}
