/**
 * 
 */

package dao.trade;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.traderate.TradeRatePlay;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import transaction.JDBCBuilder.JDBCExecutor;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.DateUtil;

/**
 * @author navins
 * @date 2013-6-16 下午12:55:59
 */
public class TradeRatePlayDao {

    private static final Logger log = LoggerFactory.getLogger(TradeRatePlayDao.class);

    private static final String USER_TRADE_RATE_QUERY = "select numIid,tid,oid,userId,created,rate,roleType,price,validScore,nick,itemTitle,content,reply,reverse,updated,sellerTs,sellerRate,remark,dispatchId from trade_rate_%s ";

    private static final String COUNT_USER_TRADE_RATE_QUERY = "select count(*) from trade_rate_%s ";

    public static List<TradeRatePlay> findByUserIdBuyerNick(Long userId, String buyerNick) {
        String sql = TradeRatePlay.genShardQuery(USER_TRADE_RATE_QUERY, userId) + " where userId = ? and nick = ?";
        return new JDBCExecutor<List<TradeRatePlay>>(TradeRatePlay.dp, sql, userId, buyerNick) {
            @Override
            public List<TradeRatePlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<TradeRatePlay> list = new ArrayList<TradeRatePlay>();

                while (rs.next()) {
                    TradeRatePlay order = new TradeRatePlay(rs);
                    if (order != null) {
                        list.add(order);
                    }
                }
                return list;
            }
        }.call();
    }

    public static List<TradeRatePlay> findByUserIdTid(Long userId, Long tid) {
        String sql = TradeRatePlay.genShardQuery(USER_TRADE_RATE_QUERY, userId) + " where userId = ? and tid = ?";
        return new JDBCExecutor<List<TradeRatePlay>>(TradeRatePlay.dp, sql, userId, tid) {
            @Override
            public List<TradeRatePlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<TradeRatePlay> list = new ArrayList<TradeRatePlay>();

                while (rs.next()) {
                    TradeRatePlay itemLimit = new TradeRatePlay(rs);
                    if (itemLimit != null) {
                        list.add(itemLimit);
                    }
                }
                return list;
            }
        }.call();
    }

    public static TradeRatePlay findByUserIOid(Long userId, Long oid) {
        String sql = TradeRatePlay.genShardQuery(USER_TRADE_RATE_QUERY, userId) + " where userId = ? and oid = ?";

        return new JDBCExecutor<TradeRatePlay>(TradeRatePlay.dp, sql, userId, oid) {
            @Override
            public TradeRatePlay doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    TradeRatePlay tradeRatePlay = new TradeRatePlay(rs);
                    return tradeRatePlay;
                }
                return null;
            }
        }.call();
    }

    public static List<TradeRatePlay> findByUserIdOidSet(Long userId, Set<Long> oids) {
        if (CommonUtils.isEmpty(oids)) {
            return null;
        }
        String oidString = StringUtils.join(oids, ',');
        String sql = TradeRatePlay.genShardQuery(USER_TRADE_RATE_QUERY, userId) + " where userId = ? and oid in ("
                + oidString + ")";
        return new JDBCExecutor<List<TradeRatePlay>>(TradeRatePlay.dp, sql, userId) {
            @Override
            public List<TradeRatePlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<TradeRatePlay> list = new ArrayList<TradeRatePlay>();

                while (rs.next()) {
                    TradeRatePlay itemLimit = new TradeRatePlay(rs);
                    if (itemLimit != null) {
                        list.add(itemLimit);
                    }
                }
                return list;
            }
        }.call();
    }

    public static List<TradeRatePlay> searchWithArgs(Long userId, Long tid, String buyerNick, int rate, Long startTs,
            Long endTs, Long dispatchId, HashSet<Long> targetOids, PageOffset po) {
        
        String order = rate < 8 ? " order by created desc" : " order by updated desc";
        
        String sql = TradeRatePlay.genShardQuery(USER_TRADE_RATE_QUERY, userId)
                + formatQuery(userId, tid, buyerNick, null, startTs, endTs, rate, dispatchId, targetOids) + order + " limit ? offset ?";

        // log.warn("[TradeRatePlayDao]searchWithArgs sql: " + sql + "  userId=" + userId);
        List<TradeRatePlay> list = new JDBCExecutor<List<TradeRatePlay>>(TradeRatePlay.dp, sql, userId, po.getPs(),
                po.getOffset()) {

            @Override
            public List<TradeRatePlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<TradeRatePlay> list = new ArrayList<TradeRatePlay>();

                while (rs.next()) {
                    TradeRatePlay itemLimit = new TradeRatePlay(rs);
                    if (itemLimit != null) {
                        list.add(itemLimit);
                    }
                }
                return list;
            }
        }.call();

        return list;
    }
    
    public static List<TradeRatePlay> searchWithArgs(Long userId, Long tid, String buyerNick, String encryptNick, Long numIid, int rate, Long startTs,
            Long endTs, Long dispatchId, HashSet<Long> targetOids, PageOffset po) {
        
        String order = rate < 8 ? " order by created desc" : " order by updated desc";
        
        String sql = TradeRatePlay.genShardQuery(USER_TRADE_RATE_QUERY, userId)
                + formatQuery(userId, tid, buyerNick, encryptNick, numIid, startTs, endTs, rate, dispatchId, targetOids) + order + " limit ? offset ?";

        // log.warn("[TradeRatePlayDao]searchWithArgs sql: " + sql + "  userId=" + userId);
        List<TradeRatePlay> list = new JDBCExecutor<List<TradeRatePlay>>(TradeRatePlay.dp, sql, userId, po.getPs(),
                po.getOffset()) {

            @Override
            public List<TradeRatePlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<TradeRatePlay> list = new ArrayList<TradeRatePlay>();

                while (rs.next()) {
                    TradeRatePlay itemLimit = new TradeRatePlay(rs);
                    if (itemLimit != null) {
                        list.add(itemLimit);
                    }
                }
                return list;
            }
        }.call();

        return list;
    }

    public static long countWithArgs(Long userId, Long tid, String buyerNick, int rate, Long startTs, Long endTs, Long dispatchId, HashSet<Long> targetOids) {
        String sql = TradeRatePlay.genShardQuery(COUNT_USER_TRADE_RATE_QUERY, userId)
                + formatQuery(userId, tid, buyerNick, null, startTs, endTs, rate, dispatchId, targetOids);

        return TradeRatePlay.dp.singleLongQuery(sql, userId);
    }
    
    public static long countWithArgs(Long userId, Long tid, String buyerNick, String encryptNick, Long numIid, int rate, Long startTs, Long endTs, Long dispatchId, HashSet<Long> targetOids) {
        String sql = TradeRatePlay.genShardQuery(COUNT_USER_TRADE_RATE_QUERY, userId)
                + formatQuery(userId, tid, buyerNick, encryptNick, numIid, startTs, endTs, rate, dispatchId, targetOids);

        return TradeRatePlay.dp.singleLongQuery(sql, userId);
    }

    private static String formatQuery(Long userId, Long tid, String buyerNick, Long numIid, Long startTs, Long endTs, int rate, Long dispatchId, HashSet<Long> targetOids) {
        String query = " where userId = ? ";
        if (tid != null && tid > 0) {
            query += " and tid = " + tid;
        }
        if (!StringUtils.isEmpty(buyerNick)) {
            query += " and nick like '%" + buyerNick + "%' ";
        }
        if (numIid != null && numIid > 0) {
            query += " and numIid = " + numIid;
        }
        if (startTs != null && startTs.longValue() > 0) {
            if (rate < 8 || rate == 128) {
                query += " and created >= " + startTs;
            } else {
                query += " and updated >= " + startTs;
            }
        }
        if (endTs != null && endTs.longValue() > 0L) {
            if (rate < 8 || rate == 128) {
                query += " and created < " + endTs;
            } else {
                query += " and updated < " + endTs;
            }
        }
        if (dispatchId != null && dispatchId.longValue() >= 0L) {
            query += " and dispatchId = " + dispatchId;
        }
        if (!CommonUtils.isEmpty(targetOids)) {
            String oidStr = StringUtils.join(targetOids, ",");
            query += " and oid in (" + oidStr + ")";
        }
        if (rate == 1) {
            // 好评
            query += " and rate&3 = 1";
        } else if (rate == 2) {
            // 中评
            query += " and rate&3 = 2";
        } else if (rate == 3) {
            // 差评
            query += " and rate&3 = 3";
        } else if (rate == 4) {
            // 中差评
            query += " and rate&3 > 1";
        } else if (rate == 8) {
            // 已改好评
            query += " and rate&3 <= 1 and (rate>>2)&3 > 1";
        } else if (rate == 16) {
            // 取有中差评的所有记录
            query += " and (rate&3 > 1 or (rate>>2)&3 > 1)";
        } else if (rate == 32) {
            // 中评改好评
            query += " and rate&3 <= 1 and (rate>>2)&3 = 2";
        } else if (rate == 64) {
            // 差评改好评
            query += " and rate&3 <= 1 and (rate>>2)&3 = 3";
        } else if (rate == 128) {
            // 中差评，或删除评价的
            query += " and (rate&3 > 1 or rate&3 = 0)";
        } else if (rate == 0) {
            // 第一次评价好评
            query += " and rate&15 = 1";
        }

        return query;
    }
    
    private static String formatQuery(Long userId, Long tid, String buyerNick, String encryptNick, Long numIid, Long startTs, Long endTs, int rate, Long dispatchId, HashSet<Long> targetOids) {
        String query = " where userId = ? ";
        if (tid != null && tid > 0) {
            query += " and tid = " + tid;
        }
        if (!StringUtils.isEmpty(buyerNick)) {
            query += " and nick in( '" + buyerNick + "', '" + encryptNick + "' ) ";
        }
        if (numIid != null && numIid > 0) {
            query += " and numIid = " + numIid;
        }
        if (startTs != null && startTs.longValue() > 0) {
            if (rate < 8 || rate == 128) {
                query += " and created >= " + startTs;
            } else {
                query += " and updated >= " + startTs;
            }
        }
        if (endTs != null && endTs.longValue() > 0L) {
            if (rate < 8 || rate == 128) {
                query += " and created < " + endTs;
            } else {
                query += " and updated < " + endTs;
            }
        }
        if (dispatchId != null && dispatchId.longValue() >= 0L) {
            query += " and dispatchId = " + dispatchId;
        }
        if (!CommonUtils.isEmpty(targetOids)) {
            String oidStr = StringUtils.join(targetOids, ",");
            query += " and oid in (" + oidStr + ")";
        }
        if (rate == 1) {
            // 好评
            query += " and rate&3 = 1";
        } else if (rate == 2) {
            // 中评
            query += " and rate&3 = 2";
        } else if (rate == 3) {
            // 差评
            query += " and rate&3 = 3";
        } else if (rate == 4) {
            // 中差评
            query += " and rate&3 > 1";
        } else if (rate == 8) {
            // 已改好评
            query += " and rate&3 <= 1 and (rate>>2)&3 > 1";
        } else if (rate == 16) {
            // 取有中差评的所有记录
            query += " and (rate&3 > 1 or (rate>>2)&3 > 1)";
        } else if (rate == 32) {
            // 中评改好评
            query += " and rate&3 <= 1 and (rate>>2)&3 = 2";
        } else if (rate == 64) {
            // 差评改好评
            query += " and rate&3 <= 1 and (rate>>2)&3 = 3";
        } else if (rate == 128) {
            // 中差评，或删除评价的
            query += " and (rate&3 > 1 or rate&3 = 0)";
        }

        return query;
    }

    public static List<TradeRatePlay> findByUserIdBadComment(Long userId) {
        String sql = TradeRatePlay.genShardQuery(USER_TRADE_RATE_QUERY, userId) + " where userId = ? and rate&3 != 1 ";

        return new JDBCExecutor<List<TradeRatePlay>>(TradeRatePlay.dp, sql, userId) {
            @Override
            public List<TradeRatePlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<TradeRatePlay> list = new ArrayList<TradeRatePlay>();

                while (rs.next()) {
                    TradeRatePlay itemLimit = new TradeRatePlay(rs);
                    if (itemLimit != null) {
                        list.add(itemLimit);
                    }
                }
                return list;
            }
        }.call();

    }

    public static List<TradeRatePlay> findByUserIdNickBadComment(Long userId, String nick) {
        String sql = TradeRatePlay.genShardQuery(USER_TRADE_RATE_QUERY, userId)
                + " where userId = ? and nick = ? and rate&3 != 1 ";

        return new JDBCExecutor<List<TradeRatePlay>>(TradeRatePlay.dp, sql, userId, nick) {
            @Override
            public List<TradeRatePlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<TradeRatePlay> list = new ArrayList<TradeRatePlay>();

                while (rs.next()) {
                    TradeRatePlay itemLimit = new TradeRatePlay(rs);
                    if (itemLimit != null) {
                        list.add(itemLimit);
                    }
                }
                return list;
            }
        }.call();

    }

    public static List<TradeRatePlay> findByUserBadComment(Long userId, Long startTs, Long endTs) {
        String sql = TradeRatePlay.genShardQuery(USER_TRADE_RATE_QUERY, userId)
                + formatQuery(userId, null, null, null, startTs, endTs, 16, null, null) + " order by created desc";

//        log.warn("[queryTradeRate]TradeRatePlay sql: " + sql);
        List<TradeRatePlay> list = new JDBCExecutor<List<TradeRatePlay>>(TradeRatePlay.dp, sql, userId) {

            @Override
            public List<TradeRatePlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<TradeRatePlay> list = new ArrayList<TradeRatePlay>();

                while (rs.next()) {
                    TradeRatePlay itemLimit = new TradeRatePlay(rs);
                    if (itemLimit != null) {
                        list.add(itemLimit);
                    }
                }
                return list;
            }
        }.call();

        return list;
    }
    //查询初始好评
    public static List<TradeRatePlay> findByUserOriginalGoodComment(Long userId, Long startTs, Long endTs) {
        String sql = TradeRatePlay.genShardQuery(USER_TRADE_RATE_QUERY, userId)
                + formatQuery(userId, null, null, null, startTs, endTs, 0, null, null) + " order by created desc";
        List<TradeRatePlay> list = new JDBCExecutor<List<TradeRatePlay>>(TradeRatePlay.dp, sql, userId) {

            @Override
            public List<TradeRatePlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<TradeRatePlay> list = new ArrayList<TradeRatePlay>();

                while (rs.next()) {
                    TradeRatePlay itemLimit = new TradeRatePlay(rs);
                    if (itemLimit != null) {
                        list.add(itemLimit);
                    }
                }
                return list;
            }
        }.call();
        return list;
    }

    //查询所有好评 初始加上修改的
    public static List<TradeRatePlay> findByUserGoodComment(Long userId, Long startTs, Long endTs) {
        String sql = TradeRatePlay.genShardQuery(USER_TRADE_RATE_QUERY, userId)
                + formatQuery(userId, null, null, null, startTs, endTs, 1, null, null) + " order by created desc";
        List<TradeRatePlay> list = new JDBCExecutor<List<TradeRatePlay>>(TradeRatePlay.dp, sql, userId) {

            @Override
            public List<TradeRatePlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<TradeRatePlay> list = new ArrayList<TradeRatePlay>();

                while (rs.next()) {
                    TradeRatePlay itemLimit = new TradeRatePlay(rs);
                    if (itemLimit != null) {
                        list.add(itemLimit);
                    }
                }
                return list;
            }
        }.call();
        return list;
    }



    static String Update_Remark_Sql = "update trade_rate_%s set remark = ? where userId = ? and tid = ?";
    public static SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyyMMdd");
    public static SimpleDateFormat sdfMMdd = new SimpleDateFormat(
            "MMdd");
    public static boolean updateRemark(Long userId, Long tid, String remark) {
        if (userId == null || tid == null) {
            return false;
        }
        if (StringUtils.isEmpty(remark)) {
            remark = "";
        }
        if(remark.length() > 255) {
        	remark = remark.substring(0, 250);
        }
        String sql = TradeRatePlay.genShardQuery(Update_Remark_Sql, userId);
        long updateNum = TradeRatePlay.dp.update(sql, remark + "[" + sdfMMdd.format(System.currentTimeMillis()) + "] ", userId, tid);
        if (updateNum > 0) {
            return true;
        }
        return false;
    }

    static String Update_DispatchId_Sql = "update trade_rate_%s set dispatchId = ?";

    public static boolean updateDispatchId(Long userId, Long oid, Long dispatchId) {
        if (userId == null || oid == null || dispatchId == null) {
            return false;
        }
        String sql = TradeRatePlay.genShardQuery(Update_DispatchId_Sql, userId) + " where oid = ?";
        long updateNum = TradeRatePlay.dp.update(sql, dispatchId, oid);
        if (updateNum > 0) {
            return true;
        }
        return false;
    }
    
    public static boolean updateNewDispatchId(Long userId, Long dispatchId, Long newDispatchId) {
        if (userId == null || dispatchId == null || newDispatchId == null) {
            return false;
        }
        String sql = TradeRatePlay.genShardQuery(Update_DispatchId_Sql, userId) + " where userId = ? and dispatchId = ?";
        long updateNum = TradeRatePlay.dp.update(sql, newDispatchId, userId, dispatchId);
        if (updateNum > 0) {
            return true;
        }
        return false;
    }

    public static boolean updateDispatchId(Long userId, HashSet<Long> oidSet, Long dispatchId) {
        if (userId == null || CommonUtils.isEmpty(oidSet)) {
            return false;
        }
        String oids = StringUtils.join(oidSet, ",");
        String sql = TradeRatePlay.genShardQuery(Update_DispatchId_Sql, userId) + " where userId = ? and oid in (" + oids + ")";
        long updateNum = TradeRatePlay.dp.update(sql, dispatchId, userId);
        if (updateNum > 0) {
            return true;
        }
        return false;
    }

    public static List<TradeRatePlay> findBadCommentEscapeTids(Long userId, Set<Long> tids, Long ts) {
        String sql = TradeRatePlay.genShardQuery(USER_TRADE_RATE_QUERY, userId) + " where userId = ? and rate&3 > 0 ";
        if (!CommonUtils.isEmpty(tids)) {
            String tidString = StringUtils.join(tids, ",");
            sql += " and tid not in (" + tidString + ")";
        }
        if (ts != null && ts > 0) {
            sql += " and created >= " + ts;
        }

        return new JDBCExecutor<List<TradeRatePlay>>(TradeRatePlay.dp, sql, userId) {
            @Override
            public List<TradeRatePlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<TradeRatePlay> list = new ArrayList<TradeRatePlay>();

                while (rs.next()) {
                    TradeRatePlay order = new TradeRatePlay(rs);
                    if (order != null) {
                        list.add(order);
                    }
                }
                return list;
            }
        }.call();
    }

    public static List<TradeRatePlay> findByUserIdDate(long userId, Long startTs, Long endTs) {
        String sql = TradeRatePlay.genShardQuery(USER_TRADE_RATE_QUERY, userId)
                 + " where userId= ? and created >= ? and created < ?"  + " order by created desc";

//        log.warn("[queryTradeRate]TradeRatePlay sql: " + sql);
        List<TradeRatePlay> list = new JDBCExecutor<List<TradeRatePlay>>(TradeRatePlay.dp, sql, userId, startTs, endTs) {

            @Override
            public List<TradeRatePlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<TradeRatePlay> list = new ArrayList<TradeRatePlay>();

                while (rs.next()) {
                    TradeRatePlay itemLimit = new TradeRatePlay(rs);
                    if (itemLimit != null) {
                        list.add(itemLimit);
                    }
                }
                return list;
            }
        }.call();

        return list;

    }

    public static class UserBadRateCache {
        public static final String USER_BAD_RATE_KEY = "BadRate_";

        public void putIntoCache(Long userId, Map<Long, Boolean> map) {
            if (userId == null) {
                return;
            }
            try {
                String cacheKey = USER_BAD_RATE_KEY + userId;
                Cache.set(cacheKey, map, "24h");
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        public Map<Long, Boolean> getBadRateFromCache(Long userId) {
            if (userId <= 0) {
                return null;
            }
            String cacheKey = USER_BAD_RATE_KEY + userId;
            Object obj = Cache.get(cacheKey);
            if (obj == null) {
                return null;
            }
            try {
                Map<Long, Boolean> map = (Map<Long, Boolean>) obj;
                return map;
            } catch (Exception e) {
                log.info(e.getMessage(), e);
            }

            return null;
        }

    }

}
