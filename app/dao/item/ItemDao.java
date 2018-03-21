
package dao.item;

import static java.lang.String.format;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import jdbcexecutorwrapper.JDBCLongListExecutor;
import jdbcexecutorwrapper.JDBCLongSetExecutor;
import jdbcexecutorwrapper.JDBCMapIntIntExecutor;
import jdbcexecutorwrapper.JDBCMapLongExecutor;
import jdbcexecutorwrapper.JDBCMapLongIntExecutor;
import jdbcexecutorwrapper.JDBCMapLongNumIidScoreModifedExecutor;
import jdbcexecutorwrapper.JDBCMapStringExecutor;
import jdbcexecutorwrapper.JDBCSetLongExecutor;
import jdbcexecutorwrapper.JDBCStringListExecutor;
import jdp.ApiJdpAdapter;
import job.clouddate.UpdateItemWeekViewTradeJob;
import models.autolist.plan.UserDelistPlan;
import models.autolist.plan.UserDelistPlan.DelistSalesNumRule;
import models.item.ItemPlay;
import models.item.ItemPlay.Status;
import models.item.ItemPlay.Type;
import models.ump.PromotionPlay;
import models.user.TitleOptimised;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;
import transaction.DBBuilder;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import transaction.JPATransactionManager;
import utils.DateUtil;
import cache.UserHasTradeItemCache;
import codegen.CodeGenerator.DBDispatcher;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.ItemThumb;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.SellerCat;

import configs.TMConfigs;
import controllers.Buskey.CatCount;
import dao.item.sub.NumIidScoreModifed;
import dao.popularized.PopularizedDao.PopularizedStatusSqlUtil;
import dao.ump.PromotionDao;

public class ItemDao {

    private static final Logger log = LoggerFactory.getLogger(ItemDao.class);

    public static final String TAG = "ItemDao";

    static DBDispatcher dp = ItemPlay.dp;

    public static ItemPlay findByNumIid(Long userId, Long numIid) {
        if (numIid == null) {
            return null;
        }
        String sql = "select " + ITEM_SQL + " from item%s where numIid = ? ";

        return new JDBCExecutor<ItemPlay>(dp, genShardQuery(sql, userId), numIid) {

            @Override
            public ItemPlay doWithResultSet(ResultSet rs) throws SQLException {

                if (rs.next()) {
                    return parseItem(rs);
                }
                return null;
            }
        }.call();
    }

    public static final List<ItemPlay> findValidList(int offset, int limit) {
        return new JDBCItemFetcher(QueryForOffset, offset, limit).call();
    }

    public static abstract class ItemBatchOper implements Callable<Boolean> {
        public int offset = 0;

        public int limit = 32;

        protected long sleepTime = 500L;

        public ItemBatchOper(int offset, int limit) {
            this.offset = offset;
            this.limit = limit;
        }

        public ItemBatchOper(int limit) {
            this.limit = limit;
        }

        public List<ItemPlay> findNext() {
            return ItemDao.findValidList(offset, limit);
        }

        public abstract void doForEachItem(ItemPlay item);

        @Override
        public Boolean call() {

            while (true) {

                List<ItemPlay> findList = findNext();
                if (CommonUtils.isEmpty(findList)) {
                    return Boolean.TRUE;
                }

                for (ItemPlay item : findList) {
                    offset++;
                    doForEachItem(item);
                }

                JPATransactionManager.clearEntities();
                CommonUtils.sleepQuietly(sleepTime);
            }

        }
    }

    public static Map<Long, String> findrelateNumIidsMap() {
        String sql = "select userId,numIid from item";

        return new JDBCExecutor<Map<Long, String>>(dp, sql) {

            @Override
            public Map<Long, String> doWithResultSet(ResultSet rs) throws SQLException {
                Map<Long, String> map = new HashMap<Long, String>();
                while (rs.next()) {
                    String oldnumIids = map.get(rs.getLong(1));
                    String newnumIids = StringUtils.EMPTY;
                    if (oldnumIids == null || oldnumIids.isEmpty()) {
                        newnumIids = (rs.getLong(2)) + ",";
                    } else {
                        newnumIids = oldnumIids + rs.getLong(2) + ",";
                    }
                    map.put(rs.getLong(1), newnumIids);
                }
                return map;
            }
        }.call();
    }

    public static String findNumIidsStringByUser(Long userId) {
        String sql = "select numIid from item%s where userId = ? and status = 1 and (cid <> 50023725 and cid <> 50023728) limit ?";

        return new JDBCExecutor<String>(dp, genShardQuery(sql, userId), userId, TMConfigs.Referers.urlsize) {

            @Override
            public String doWithResultSet(ResultSet rs) throws SQLException {
                StringBuilder numIids = new StringBuilder();
                while (rs.next()) {
                    numIids.append(rs.getLong(1)).append(",");
                }
                return numIids.toString();
            }
        }.call();
    }

    public static Map<Long, NumIidScoreModifed> findNumIidScoreModified(Long userId) {
        String sql = "select numIid,score,listTime from item%s where userId = ? ";
        return new JDBCMapLongNumIidScoreModifedExecutor(dp, genShardQuery(sql, userId), userId).call();
    }

    public static void updateInventory(Long userId, Collection<Long> numIids) {
        if (CommonUtils.isEmpty(numIids)) {
            return;
        }

        StringBuilder sb = new StringBuilder("update item%s set status = status & 1022  where numIid in  (");
        sb.append(StringUtils.join(numIids, ','));
        sb.append(")");

        long update = dp.update(genShardQuery(sb.toString(), userId));
        log.warn("[inventory update num :]" + update);
    }

    public static List<ItemPlay> findByNumIids(Long userId, Collection<Long> numIids) {
    	log.info(numIids.toString());
        if (CommonUtils.isEmpty(numIids)) {
            return ListUtils.EMPTY_LIST;
        }
        String sql = "select " + ITEM_SQL + " from item%s where numIid in  (" + StringUtils.join(numIids, ',') + ")";
        return new JDBCItemFetcher(genShardQuery(sql, userId)).call();
    }

    public static long countByTitleAndNumIids(Long userId, String title, Collection<Long> numIidColl) {
        if (CommonUtils.isEmpty(numIidColl)) {
            return 0;
        }

        String numIids = StringUtils.join(numIidColl, ',');

        numIids = CommonUtils.escapeSQL(numIids);

        if (StringUtils.isEmpty(numIids)) {
            return 0;
        }

        String sql = "select count(*) from item%s where userId = ? and numIid in  (";

        sql = genShardQuery(sql, userId);

        StringBuilder sb = new StringBuilder();

        sb.append(sql);
        sb.append(numIids);
        sb.append(") ");

        if (StringUtils.isEmpty(title) == false) {
            String like = appendTitleLike(title);
            sb.append(" and " + like + " ");
        }

        sql = sb.toString();

        return dp.singleLongQuery(sql, userId);
    }

    public static List<ItemPlay> findByTitleAndNumIids(Long userId, String title,
            Collection<Long> numIidColl, PageOffset po) {
        if (CommonUtils.isEmpty(numIidColl)) {
            return new ArrayList<ItemPlay>();
        }

        String numIids = StringUtils.join(numIidColl, ',');

        numIids = CommonUtils.escapeSQL(numIids);

        if (StringUtils.isEmpty(numIids)) {
            return new ArrayList<ItemPlay>();
        }

        String sql = "select " + ITEM_SQL + " from item%s where userId = ? and numIid in  (";

        sql = genShardQuery(sql, userId);

        StringBuilder sb = new StringBuilder();

        sb.append(sql);
        sb.append(numIids);
        sb.append(") ");

        if (StringUtils.isEmpty(title) == false) {
            String like = appendTitleLike(title);
            sb.append(" and " + like + " ");
        }

        sb.append(" limit ?, ? ");

        sql = sb.toString();

        return new JDBCItemFetcher(sql, userId, po.getOffset(), po.getPs()).call();
    }

    public static Long findMaxCid(Long userId) {
        String sql = "select cid, count(*) from item%s where userId = ? and status = 1 group by cid";

        return new JDBCExecutor<Long>(dp, genShardQuery(sql, userId), userId) {

            @Override
            public Long doWithResultSet(ResultSet rs) throws SQLException {
                Long cid = -1L, maxCount = -1L;
                while (rs.next()) {
                    Long count = rs.getLong(2);
                    if (maxCount < count) {
                        maxCount = count;
                        cid = rs.getLong(1);
                    }
                }
                return cid;
            }
        }.call();
    }
    
    public static List<Long> associatedCount(Long userId,List<Long> numIids){
        
        StringBuilder sb = new StringBuilder("select numIid from item%s where userId = ? and  numIid in (");
        sb.append(StringUtils.join(numIids, ','));
        sb.append(");");
        String sql  = sb.toString();
        
        return new JDBCExecutor<List<Long>>(dp,genShardQuery(sql, userId),userId){
            
            @Override
            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {
                List<Long> list = new ArrayList<Long>();
                while (rs.next()){
                    if(list == null){
                        list = new ArrayList<Long>();
                    }
                    list.add(rs.getLong(1));
                }
                return list;
            }
        }.call();
    }

    
    public static Long findBestSales(Long userId){
        String sql = "select numIid from item%s where userId = ? order by salescount limit 1";
        
        return new JDBCExecutor<Long>(dp,genShardQuery(sql, userId),userId){

            @Override
            public Long doWithResultSet(ResultSet rs) throws SQLException {
                Long numIid = null;
                while (rs.next()){
                     numIid = rs.getLong(1);
                }
                return numIid;
            }
        }.call();
    }
    
    public static List<CatCount> findAllCids(Long userId) {
        String sql = "select cid, count(*) from item%s where userId = ? group by cid";

        return new JDBCExecutor<List<CatCount>>(dp, genShardQuery(sql, userId), userId) {
            @Override
            public List<CatCount> doWithResultSet(ResultSet rs) throws SQLException {
                List<CatCount> list = new ArrayList<CatCount>();
                while (rs.next()) {
                    Long cid = rs.getLong(1);
                    // 排除包邮和赠品类目
                    if (cid == 50023725L || cid == 50023728L) {
                        continue;
                    }
                    Integer count = rs.getInt(2);
                    list.add(new CatCount(cid, count));
                }
                return list;
            }
        }.call();
    }

    static String NEW_ITEM_SQL = " userId, numIid, cid, picURL, price, title, type, quantity, salesCount, status, score, deListTime, listTime, sellerCids ";

    public static Map<Long, ItemPlay> findMapByNumIids(Long userId, Collection<Long> numIids) {
        if (CommonUtils.isEmpty(numIids)) {
            return MapUtils.EMPTY_MAP;
        }

        StringBuilder sb = new StringBuilder("select ");
        sb.append(NEW_ITEM_SQL);
        sb.append(" from item%s where numIid in  (");
        sb.append(StringUtils.join(numIids, ','));
        sb.append(");");
        String sql = sb.toString();

        return new JDBCExecutor<Map<Long, ItemPlay>>(dp, genShardQuery(sql, userId)) {

            @Override
            public Map<Long, ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                Map<Long, ItemPlay> map = new HashMap<Long, ItemPlay>();
                while (rs.next()) {
                    map.put(rs.getLong(2), newParseItem(rs));
                }
                return map;
            }
        }.call();
    }

    /*public static List<ItemPlay> findPopularizedInNumIids(Long userId, int offset, int limit, String search, int sort,
            String catId, int populairzed,Set<Long> numIids) {
        log.info(format("findPopularized:userId,  search".replaceAll(", ", "=%s, ") + "=%s", userId, search));

        String sql = "select " + ITEM_SQL + " from item%s where numIid in  (" + StringUtils.join(numIids, ',') + ") and userId = ? and (cid <> 50023725 and cid <> 50023728)";

        sql = genShardQuery(sql, userId);
        log.info("[sql]" + sql);
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sql += " and " + like;
        }

        if (!StringUtils.isEmpty(catId)) {
            String like = appendSellerCidsLike(catId);
            sql += " and " + like;
        }

        if (populairzed == 0) {
            sql += " and (type&4 = 4) ";
        } else if (populairzed == 1) {
            sql += " and (type&4 = 0) ";
        }

        if (sort == 1)
            sql += " order by score limit ?, ?";
        else if (sort == 2)
            sql += " order by score desc limit ?, ?";
        else if (sort == 3)
            sql += " order by salesCount limit ?, ?";
        else if (sort == 4)
            sql += " order by salesCount desc limit ?, ?";
        else
            sql += " order by score limit ?, ?";
        return new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, offset, limit) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }*/

    public static List<String> findTitleByUser(Long userId, int limit) {
        return findTitleByUser(userId, 0, limit, null);
    }

    public static List<String> findTitleByUser(Long userId, int offset, int limit, String search) {
        String sql = "select title from item%s where userId = ? ";
        sql = genShardQuery(sql, userId);
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sql += " and " + like;
        }
        sql += " limit ?, ? ";

        return new JDBCStringListExecutor(dp, sql, userId, offset, limit).call();
    }

    /*public static void updatePopularizedItems(Long userId){
    	Set<Long> pops = PopularizedDao.findNumIidsByUserId(userId);
    	List<ItemPlay> items = ItemDao.findByUserId(userId);
    	if(items != null && items.size() > 0){
    		for(ItemPlay item : items){
    			if(pops.contains(item.getNumIid())){
    				item.setType(4);
    			}
    		}
    	}
    }*/

    static String ITEM_SQL = " userId, numIid, cid, picURL, price, title, type, quantity, recentSalesCount, salesCount, status, score, deListTime, listTime, sellerCids ";
    static String JOIN_ITEM_SQL = " i.userId, i.numIid, cid, picURL, price, title, type, quantity, recentSalesCount, salesCount, status, score, deListTime, listTime, sellerCids ";

    static String QueryForOffset = "select " + ITEM_SQL + " from item" + UpdateItemWeekViewTradeJob.FromIndex
            + "limit ?,?";

    public static ItemPlay parseItem(ResultSet rs) {
        try {
            ItemPlay item = new ItemPlay(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getString(4), rs.getDouble(5),
                    rs.getString(6), rs.getInt(7), rs.getInt(8), rs.getInt(9), rs.getInt(10), rs.getInt(11), rs.getInt(12),
                    rs.getString(15));
            item.setDeListTime(rs.getLong(13));
            item.setListTime(rs.getLong(14));
            return item;
        } catch (SQLException e) {
            log.info(e.getMessage(), e);
        }
        return null;
    }

    public static ItemPlay parseItemWithOptimised(ResultSet rs) {
        try {
            ItemPlay item = new ItemPlay(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getString(4), rs.getDouble(5),
                    rs.getString(6), rs.getInt(7), rs.getInt(8), rs.getInt(9), rs.getInt(10), rs.getInt(11),
                    rs.getString(14));
            item.setDeListTime(rs.getLong(12));
            item.setListTime(rs.getLong(13));
            item.setOptimised(rs.getBoolean(15));
            item.setCreated(rs.getLong(16));
            return item;
        } catch (SQLException e) {
            log.info(e.getMessage(), e);
        }
        return null;
    }

    public static ItemPlay oldParseItem(ResultSet rs) {
        try {
            ItemPlay item = new ItemPlay(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getString(4), rs.getDouble(5),
                    rs.getString(6), rs.getInt(7), rs.getInt(8), rs.getInt(9), rs.getInt(10), rs.getInt(11), rs.getInt(12));
            item.setDeListTime(rs.getLong(13));
            item.setListTime(rs.getLong(14));
            return item;
        } catch (SQLException e) {
            log.info(e.getMessage(), e);
        }
        return null;
    }

    public static ItemPlay newParseItem(ResultSet rs) {
        try {
            ItemPlay item = new ItemPlay(rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getString(4), rs.getDouble(5),
                    rs.getString(6), rs.getInt(7), rs.getInt(8), rs.getInt(9), rs.getInt(10), rs.getInt(11), rs.getString(15));
            item.setDeListTime(rs.getLong(13));
            item.setListTime(rs.getLong(14));
            item.setSellerCids(rs.getString(15));
            return item;
        } catch (SQLException e) {
            log.info(e.getMessage(), e);
        }
        return null;
    }

    static String QueryForFindByUserId = "select " + ITEM_SQL + " from item%s where userId = ?";

    public static List<ItemPlay> findByUserId(final Long userId) {
        return new JDBCItemFetcher(genShardQuery(QueryForFindByUserId, userId), userId).call();
    }

    public static ItemPlay findFirstItemByUserId(final Long userId) {

        return new JDBCExecutor<ItemPlay>(dp, genShardQuery(QueryForFindByUserId, userId), userId) {

            @Override
            public ItemPlay doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return parseItem(rs);
                }
                return null;
            }
        }.call();
    }

    static class JDBCItemFetcher extends JDBCExecutor<List<ItemPlay>> {

        public JDBCItemFetcher(String sql, Object... objects) {
            super(dp, sql, objects);
            this.debug = false;
        }

        @Override
        public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
            List<ItemPlay> itemList = new ArrayList<ItemPlay>();
            while (rs.next()) {
                itemList.add(parseItem(rs));
            }
            return itemList;
        }

    }

    static String QueryForFindOnSaleByUserId = "select " + ITEM_SQL + " from item%s where userId = ? and status = 1";

    public static List<ItemPlay> findOnSaleByUserId(final Long userId) {
        return new JDBCItemFetcher(genShardQuery(QueryForFindOnSaleByUserId, userId), userId).call();
    }

    static String QueryForFindOnSaleOutOfNumIids = "select " + ITEM_SQL
            + " from item%s where userId = ? and status = 1 ";

    public static List<ItemPlay> findOnSaleOutOfNumIids(final Long userId, String numIids) {
        String sql = QueryForFindOnSaleOutOfNumIids + "and numIid not in (" + numIids + ") ";
        return new JDBCItemFetcher(genShardQuery(sql, userId), userId).call();
    }

    static String QueryForFindUserCid = "select " + ITEM_SQL
            + " from item%s where userId = ? and cid = ? limit ? ";

    public static List<ItemPlay> findByUserAndCat(final Long userId, final Long cid, int limit) {
        String sql = QueryForFindUserCid;
        return new JDBCItemFetcher(genShardQuery(sql, userId), userId, cid, limit).call();
    }

    static String QueryForFindByTitle = "select numIid from item%s where userId = ?";

    public static List<Long> findNumIidsByTitle(final Long userId, String title) {

        String sql = QueryForFindByTitle;
        sql = genShardQuery(sql, userId);
        if (!StringUtils.isEmpty(title)) {
            sql += " and " + appendTitleLike(title);
        }

        return new JDBCLongListExecutor(dp, sql, userId).call();
    }

    public static List<ItemPlay> findByNumIidList(final User user, String numIidList) {
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

    public static List<ItemPlay> findByNumIidListAndUserId(final Long userId, String numIidList) {
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
            items.add(ItemDao.findByNumIid(userId, numIids.get(i)));
        }
        return items;
    }

    static String QueryForFindByUserIdWithLimit = "select " + ITEM_SQL + " from item%s where userId = ? limit ?";

    public static List<ItemPlay> findByUserId(final Long userId, final int limit) {

        return new JDBCExecutor<List<ItemPlay>>(dp, genShardQuery(QueryForFindByUserIdWithLimit, userId), userId, limit) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    static String QueryForFindByUserIdWithPageOffset = "select " + ITEM_SQL
            + " from item%s where userId = ? limit ? offset ?";

    public static List<ItemPlay> findByUserId(final Long userId, PageOffset po) {

        return new JDBCExecutor<List<ItemPlay>>(dp, genShardQuery(QueryForFindByUserIdWithPageOffset, userId), userId,
                po.getPs(), po.getOffset()) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    static String QueryForFindByUserIdWithLimitOnsale = "select " + ITEM_SQL
            + " from item%s where userId = ? and status = 1 limit ?";

    public static List<ItemPlay> findByUserIdOnsale(final Long userId, final int limit) {

        return new JDBCExecutor<List<ItemPlay>>(dp, genShardQuery(QueryForFindByUserIdWithLimitOnsale, userId), userId,
                limit) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }
    
    static String queryForFindByUserIdWithLimitOnsaleDesc = "select " + ITEM_SQL + " from item%s where userId = ? and status = 1 " +
    		                                " and (cid <> 50023725 and cid <> 50023728) order by salesCount desc limit ?";
    
    public static List<ItemPlay> findByUserIdOnsaleDesc(final Long userId,final int limit){
        return new JDBCExecutor<List<ItemPlay>>(dp, genShardQuery(queryForFindByUserIdWithLimitOnsaleDesc, userId), userId,
                limit) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    static String QueryForFindByUserIdWithLimitOnsaleLimit = "select " + ITEM_SQL
            + " from item%s where userId = ? and status = 1 limit ? offset ? ";

    public static List<ItemPlay> findByUserIdOnsaleLimit(final Long userId, PageOffset po) {

        return new JDBCExecutor<List<ItemPlay>>(dp, genShardQuery(QueryForFindByUserIdWithLimitOnsaleLimit, userId),
                userId,
                po.getPs(), po.getOffset()) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    public static Map<Long, Integer> findSalesByUserIdAndNumIids(final Long userId, String numIids) {

        return new JDBCMapLongIntExecutor(dp, genShardQuery(
                "select numIid, salesCount from item%s where userId = ? and numIid in (" + numIids + ");", userId),
                userId).call();
    }

    public static ItemPlay findMinSalesItemWithStatus(Long userId, int status, final int minPrice) {

        String query = "select " + ITEM_SQL
                + " from item%s where userId = ? and status = ? and price >= ? "
                + " order by salesCount asc limit ? offset ? ";

        return new JDBCExecutor<ItemPlay>(dp, genShardQuery(query, userId),
                userId, status, minPrice, 1, 0) {

            @Override
            public ItemPlay doWithResultSet(ResultSet rs) throws SQLException {

                if (rs.next()) {
                    return parseItem(rs);
                }
                return null;
            }
        }.call();

    }

    static String ITEM_SQL_JOIN = " i.userId, i.numIid, i.cid, i.picURL, i.price, i.title, i.type, i.quantity, i.salesCount, i.status, i.score, i.deListTime, i.listTime";

    public static List<ItemPlay> searchPop(Long userId, int offset, int limit, String search, int sort,
            int polularized, String catId, int popularizeStatus) {
        String sql = "select "
                + ITEM_SQL_JOIN
                + " from item%s as i LEFT JOIN popularized as p ON i.numIid = p.numIid "
                + PopularizedStatusSqlUtil.getSearchLeftJoinSql(popularizeStatus)
                + " where i.userId = ? and (i.cid <> 50023725 and i.cid <> 50023728)  and i.status = 1 ";
        sql = genShardQuery(sql, userId);
        log.info("[sql]" + sql);
        /*if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLikeLeftJoin(search);
            sql += " and " + like;
        }*/
        if (!StringUtils.isBlank(search)) {
            search = search.trim();
            if (search.length() < 3) {
                sql += " and ";
                sql += appendTitleLikeLeftJoin(search);
                sql += " ";
            } else {
                String[] splits = search.split(" ");
                for (String string : splits) {
                    if (StringUtils.isEmpty(string)) {
                        continue;
                    }

                    sql += " and ";
                    sql += "i.title like '%";
                    sql += CommonUtils.escapeSQL(string);
                    sql += "%' ";
                }
            }
        }

        if (!StringUtils.isEmpty(catId)) {
            String like = appendSellerCidsLikeLeftJoin(catId);
            sql += " and " + like;
        }

        if (polularized == 0) {
            sql += " and i.numIid in (select numIid from popularized where userId = " + userId + " "
                    + PopularizedStatusSqlUtil.getStatusRuleSql(popularizeStatus) + " )";
        } else if (polularized == 1) {
            sql += " and i.numIid not in (select numIid from popularized where userId = " + userId + " "
                    + PopularizedStatusSqlUtil.getStatusRuleSql(popularizeStatus) + " )";
        }

        if (sort == 1)
            sql += " order by p.numIid desc,i.score limit ?, ?";
        else if (sort == 2)
            sql += " order by p.numIid desc,i.score desc limit ?, ?";
        else if (sort == 3)
            sql += " order by p.numIid desc,i.salesCount limit ?, ?";
        else if (sort == 4)
            sql += " order by p.numIid desc,i.salesCount desc limit ?, ?";
        else
            sql += " order by p.numIid desc,i.score limit ?, ?";
        return new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, offset, limit) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(oldParseItem(rs));
                }
                return itemList;
            }
        }.call();

    }

    public static long countPop(Long userId, String search, int sort, String catId, int polularized,
            int popularizeStatus) {
        String sql = "select count(*) from item%s as i LEFT JOIN popularized as p ON i.numIid = p.numIid " +
                PopularizedStatusSqlUtil.getSearchLeftJoinSql(popularizeStatus) +
                " where i.userId = ? and (i.cid <> 50023725 and i.cid <> 50023728) and  i.status = 1  ";
        sql = genShardQuery(sql, userId);
        log.info("[sql]" + sql);
        /*if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLikeLeftJoin(search);
            sql += " and " + like;
        }*/
        if (!StringUtils.isBlank(search)) {
            search = search.trim();
            if (search.length() < 3) {
                sql += " and ";
                sql += appendTitleLikeLeftJoin(search);
                sql += " ";
            } else {
                String[] splits = search.split(" ");
                for (String string : splits) {
                    if (StringUtils.isEmpty(string)) {
                        continue;
                    }

                    sql += " and ";
                    sql += "i.title like '%";
                    sql += CommonUtils.escapeSQL(string);
                    sql += "%' ";
                }
            }
        }
        if (!StringUtils.isEmpty(catId)) {
            String like = appendSellerCidsLikeLeftJoin(catId);
            sql += " and " + like;
        }

        if (polularized == 0) {
            sql += " and i.numIid in (select numIid from popularized where userId = " + userId + " "
                    + PopularizedStatusSqlUtil.getStatusRuleSql(popularizeStatus) + " )";
        } else if (polularized == 1) {
            sql += " and i.numIid not in (select numIid from popularized where userId = " + userId + " "
                    + PopularizedStatusSqlUtil.getStatusRuleSql(popularizeStatus) + " )";
        }

        if (sort == 1)
            sql += " order by p.numIid desc,i.score ";
        else if (sort == 2)
            sql += " order by p.numIid desc,i.score desc ";
        else if (sort == 3)
            sql += " order by p.numIid desc,i.salesCount ";
        else if (sort == 4)
            sql += " order by p.numIid desc,i.salesCount desc ";
        else
            sql += " order by p.numIid desc,i.score ";
        return dp.singleLongQuery(sql, userId);

    }

    public static List<ItemPlay> searchRelationed(Long userId, int offset, int limit, String search,
            int sort, int relationed, String catId, String cid, int itemStatus, double itemPriceMin,
            double itemPriceMax) {
        String sql = "select "
                + ITEM_SQL_JOIN
                + " from item%s as i LEFT JOIN relationed_items as p ON i.numIid = p.numIid "
                + " where i.userId = ? and (i.cid <> 50023725 and i.cid <> 50023728) ";
        sql = genShardQuery(sql, userId);
        log.info("[sql]" + sql);

        if (!StringUtils.isBlank(search)) {
            search = search.trim();
            if (search.length() < 3) {
                sql += " and ";
                sql += appendTitleLikeLeftJoin(search);
                sql += " ";
            } else {
                String[] splits = search.split(" ");
                for (String string : splits) {
                    if (StringUtils.isEmpty(string)) {
                        continue;
                    }

                    sql += " and ";
                    sql += "i.title like '%";
                    sql += CommonUtils.escapeSQL(string);
                    sql += "%' ";
                }
            }
        }

        if (!StringUtils.isEmpty(catId)) {
            String like = appendSellerCidsLikeLeftJoin(catId);
            sql += " and " + like;
        }

        if (!StringUtils.isEmpty(cid)) {
            if (StringUtils.isNumeric(cid)) {
                sql += " and i.cid = " + cid;
            }
        }

        if (itemPriceMin > 0.0 && itemPriceMax > 0.0) {
            sql += " and (i.price >= " + itemPriceMin + ") and (i.price <= " + itemPriceMax + ")";
        } else if (itemPriceMin > 0.0) {
            sql += " and (i.price >= " + itemPriceMin + ") ";
        } else if (itemPriceMax > 0.0) {
            sql += " and (i.price <= " + itemPriceMax + ") ";
        }

        if (relationed != 2) {
            if (relationed == 1) {
                sql += " and i.numIid in (select numIid from relationed_items where userId = " + userId + " )";
            } else {
                sql += " and i.numIid not in (select numIid from relationed_items where userId = " + userId + " )";
            }
        }

        if (itemStatus == 1) {
            sql += " and status = 1 ";
        } else if (itemStatus == 0) {
            sql += " and status = 0 ";
        }

        if (sort == 1)
            sql += " order by p.numIid desc,i.score limit ?, ?";
        else if (sort == 2)
            sql += " order by p.numIid desc,i.score desc limit ?, ?";
        else if (sort == 3)
            sql += " order by p.numIid desc,i.salesCount limit ?, ?";
        else if (sort == 4)
            sql += " order by p.numIid desc,i.salesCount desc limit ?, ?";
        else
            sql += " order by p.numIid desc,i.score limit ?, ?";
        return new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, offset, limit) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(oldParseItem(rs));
                }
                return itemList;
            }
        }.call();

    }

    public static List<ItemPlay> searchRelationedAll(Long userId, int relationed) {
        String sql = "select "
                + ITEM_SQL_JOIN
                + " from item%s as i LEFT JOIN relationed_items as p ON i.numIid = p.numIid "
                + " where i.userId = ? and (i.cid <> 50023725 and i.cid <> 50023728)  ";
        sql = genShardQuery(sql, userId);
        log.info("[sql]" + sql);

        if (relationed == 0) {
            sql += " and i.numIid in (select numIid from relationed_items where userId = " + userId + " )";
        } else if (relationed == 1) {
            sql += " and i.numIid not in (select numIid from relationed_items where userId = " + userId + " )";
        }

        return new JDBCExecutor<List<ItemPlay>>(dp, sql, userId) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(oldParseItem(rs));
                }
                return itemList;
            }
        }.call();

    }

    public static long countRelationed(Long userId, String search, int sort, String catId, String cid,
            int relationed, int itemStatus, double itemPriceMin, double itemPriceMax) {
        String sql = "select count(*) from item%s as i LEFT JOIN relationed_items as p ON i.numIid = p.numIid " +
                " where i.userId = ? and (i.cid <> 50023725 and i.cid <> 50023728) ";
        sql = genShardQuery(sql, userId);
        log.info("[sql]" + sql);

        if (!StringUtils.isBlank(search)) {
            search = search.trim();
            if (search.length() < 3) {
                sql += " and ";
                sql += appendTitleLikeLeftJoin(search);
                sql += " ";
            } else {
                String[] splits = search.split(" ");
                for (String string : splits) {
                    if (StringUtils.isEmpty(string)) {
                        continue;
                    }

                    sql += " and ";
                    sql += "i.title like '%";
                    sql += CommonUtils.escapeSQL(string);
                    sql += "%' ";
                }
            }
        }

        if (!StringUtils.isEmpty(catId)) {
            String like = appendSellerCidsLikeLeftJoin(catId);
            sql += " and " + like;
        }

        if (!StringUtils.isEmpty(cid)) {
            if (StringUtils.isNumeric(cid)) {
                sql += " and i.cid = " + cid;
            }
        }

        if (itemPriceMin > 0.0 && itemPriceMax > 0.0) {
            sql += " and (i.price >= " + itemPriceMin + ") and (i.price <= " + itemPriceMax + ")";
        } else if (itemPriceMin > 0.0) {
            sql += " and (i.price >= " + itemPriceMin + ") ";
        } else if (itemPriceMax > 0.0) {
            sql += " and (i.price <= " + itemPriceMax + ") ";
        }

        if (relationed != 2) {
            if (relationed == 1) {
                sql += " and i.numIid in (select numIid from relationed_items where userId = " + userId + " )";
            } else {
                sql += " and i.numIid not in (select numIid from relationed_items where userId = " + userId + " )";
            }
        }

        if (itemStatus == 1) {
            sql += " and status = 1 ";
        } else if (itemStatus == 0) {
            sql += " and status = 0 ";
        }

        if (sort == 1)
            sql += " order by p.numIid desc,i.score ";
        else if (sort == 2)
            sql += " order by p.numIid desc,i.score desc ";
        else if (sort == 3)
            sql += " order by p.numIid desc,i.salesCount ";
        else if (sort == 4)
            sql += " order by p.numIid desc,i.salesCount desc ";
        else
            sql += " order by p.numIid desc,i.score ";
        return dp.singleLongQuery(sql, userId);

    }

    public static List<ItemPlay> findPopularized(Long userId, int offset, int limit, String search, int sort,
            String catId, int populairzed) {
        log.info(format("findPopularized:userId,  search".replaceAll(", ", "=%s, ") + "=%s", userId, search));

        String sql = "select " + ITEM_SQL
                + " from item%s where userId = ? and (cid <> 50023725 and cid <> 50023728) and status = 1";

        sql = genShardQuery(sql, userId);
        log.info("[sql]" + sql);
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sql += " and " + like;
        }

        if (!StringUtils.isEmpty(catId)) {
            String like = appendSellerCidsLike(catId);
            sql += " and " + like;
        }

        if (populairzed == 0) {
            sql += " and (type&4 = 4) ";
        } else if (populairzed == 1) {
            sql += " and (type&4 = 0) ";
        }

        if (sort == 1)
            sql += " order by score limit ?, ?";
        else if (sort == 2)
            sql += " order by score desc limit ?, ?";
        else if (sort == 3)
            sql += " order by salesCount limit ?, ?";
        else if (sort == 4)
            sql += " order by salesCount desc limit ?, ?";
        else
            sql += " order by score limit ?, ?";
        return new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, offset, limit) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    public static long countPopularized(Long userId, String search, int sort, String catId, int populairzed) {
        log.info(format("countPopularized : userId, search, catId, countPopularized".replaceAll(", ", "=%s, ") + "=%s",
                userId, search, catId, populairzed));

        String sql = "select count(*) from item%s where userId = ? and (cid <> 50023725 and cid <> 50023728)";

        sql = genShardQuery(sql, userId);
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sql += " and " + like;
        }

        if (!StringUtils.isEmpty(catId)) {
            String like = appendSellerCidsLike(catId);
            sql += " and " + like;
        }

        if (populairzed == 0) {
            sql += " and (type&4 = 4) ";
        } else if (populairzed == 1) {
            sql += " and (type&4 = 0) ";
        }

        return dp.singleLongQuery(sql, userId);
    }

    /*public static long countPopularizedInNumIids(Long userId, String search, int sort, String catId, int populairzed, Set<Long> numIids) {
        log.info(format("countPopularized : userId, search, catId, countPopularized".replaceAll(", ", "=%s, ") + "=%s",
                userId, search, catId, populairzed));

        String sql = "select count(*) from item%s where numIid in  (" + StringUtils.join(numIids, ',') + ") and userId = ? and (cid <> 50023725 and cid <> 50023728)";

        sql = genShardQuery(sql, userId);
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sql += " and " + like;
        }

        if (!StringUtils.isEmpty(catId)) {
            String like = appendSellerCidsLike(catId);
            sql += " and " + like;
        }

        if (populairzed == 0) {
            sql += " and (type&4 = 4) ";
        } else if (populairzed == 1) {
            sql += " and (type&4 = 0) ";
        }

        return dp.singleLong(sql, userId);
    }*/

    public static long countOnlineByUser(Long userId, String search) {
        String sql = "select count(*) from item%s where userId = ? and status = 1  and (cid <> 50023725 and cid <> 50023728)";

        sql = genShardQuery(sql, userId);
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sql += " and " + like;
        }

        return dp.singleLongQuery(sql, userId);
    }
    
    
    public static long countOnlineBySellerCid(Long userId,String search,Long sellerCid){
        String sql = "select count(*) from item%s where userId = ? and status = 1  and (cid <> 50023725 and cid <> 50023728)";

        sql = genShardQuery(sql, userId);
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sql += " and " + like;
        }
        if (!NumberUtil.isNullOrZero(sellerCid)) {
            String like = appendSellerCidsLike(String.valueOf(sellerCid));
            sql += " and" + like;
        }

        return dp.singleLongQuery(sql, userId);
    }

    public static long countAllByUser(Long userId, String search) {
        String sql = "select count(*) from item%s where userId = ? and (cid <> 50023725 and cid <> 50023728)";

        sql = genShardQuery(sql, userId);
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sql += " and " + like;
        }

        return dp.singleLongQuery(sql, userId);
    }

    public static long countOnlineByUserWithScore(Long userId, int lowBegin, int topEnd, String search, int status) {

        log.info(format(
                "countOnlineByUserWithScore:userId, lowBegin, topEnd, search".replaceAll(", ", "=%s, ") + "=%s",
                userId, lowBegin, topEnd, search));

        String sql = "select count(*) from item%s where userId = ? and status <> ? and score> ? and score < ? and (cid <> 50023725 and cid <> 50023728)";

        sql = genShardQuery(sql, userId);
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sql += " and " + like;
        }

        return dp.singleLongQuery(sql, userId, status, lowBegin, topEnd);
    }

    public static long countOnlineByUserWithArgs(Long userId, int lowBegin, int topEnd, String search, Long numIid, int status,
            String catId, Long cid) {

        log.info(format(
                "countOnlineByUserWithArgs:userId, lowBegin, topEnd, search, status, catId".replaceAll(", ", "=%s, ")
                        + "=%s", userId, lowBegin, topEnd, search, status, catId));

        String sql = "select count(*) from item%s where userId = ? and status <> ? and score>= ? and score <= ? and (cid <> 50023725 and cid <> 50023728)";

        sql = genShardQuery(sql, userId);
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sql += " and " + like;
        }

        if (!StringUtils.isEmpty(catId)) {
            String like = appendSellerCidsLike(catId);
            sql += " and " + like;
        }

        if (cid != null && cid > 0) {
            sql += "and cid = " + cid;
        }
        
        if (numIid != null && numIid > 0) {
            sql += "and numIid = " + numIid;
        }

        return dp.singleLongQuery(sql, userId, status, lowBegin, topEnd);
    }

    public static Set<Long> findNumIdsByCondition(Long userId, int notStatus, long itemCid, long sellerCid) {
        String baseSql = genShardQuery("select numIid from item%s where userId = ? and status <> ? ", userId);

        if (sellerCid > 0L) {
            baseSql += " and  " + appendSellerCidsLike(String.valueOf(sellerCid));
        }
        if (itemCid > 0L) {
            baseSql += " and  cid = " + itemCid;
        }
        log.info("[base sql:]" + baseSql);

        return new JDBCBuilder.JDBCLongSetExecutor(dp, baseSql, userId, notStatus).call();
    }

    public static long countOnlineByUserWithArgsAndOptimise(Long userId, int lowBegin, int topEnd, String search, Long numIid,
            int status,
            String catId, Long cid, int optimised) {

        log.info(format(
                "countOnlineByUserWithArgs:userId, lowBegin, topEnd, search, status, catId".replaceAll(", ", "=%s, ")
                        + "=%s", userId, lowBegin, topEnd, search, status, catId));

        String sql = "select count(*) from item%s as i LEFT JOIN title_optimised as t ON i.numIid = t.numIid where i.userId = ? and i.status <> ? and i.score>= ? and i.score <= ? and (i.cid <> 50023725 and i.cid <> 50023728)";

        sql = genShardQuery(sql, userId);

        if (!StringUtils.isBlank(search)) {
            search = search.trim();
            if (search.length() < 3) {
                sql += " and ";
                sql += appendTitleLikeLeftJoin(search);
                sql += " ";
            } else {
                String[] splits = search.split(" ");
                for (String string : splits) {
                    if (StringUtils.isEmpty(string)) {
                        continue;
                    }

                    sql += " and ";
                    sql += "i.title like '%";
                    sql += CommonUtils.escapeSQL(string);
                    sql += "%' ";
                }
            }
        }
        if (!StringUtils.isEmpty(catId)) {
            String like = appendSellerCidsLikeLeftJoin(catId);
            sql += " and " + like;
        }
        
        if (cid != null && cid > 0) {
            sql += "and i.cid = " + cid;
        }
        
        if (numIid != null && numIid > 0) {
            sql += "and i.numIid = " + numIid;
        }

        if (optimised == TitleOptimised.Status.OPTIMISED) {
            sql += " and i.numIid in (select numIid from title_optimised where userId = " + userId + " "
                    + " )";
        } else if (optimised == TitleOptimised.Status.UN_OPTIMISED) {
            sql += " and i.numIid not in (select numIid from title_optimised where userId = " + userId + " "
                    + " )";
        }

        return dp.singleLongQuery(sql, userId, status, lowBegin, topEnd);
    }

    public static List<ItemPlay> findOnlineByUserWithscore(Long userId, int offset, int limit, String search,
            int lowBegin, int topEnd, int sort, int status) {
        log.info(format(
                "countOnlineByUserWithScore:userId, lowBegin, topEnd, search".replaceAll(", ", "=%s, ") + "=%s",
                userId, lowBegin, topEnd, search));

        String sql = "select "
                + " i.userId, i.numIid, i.cid, i.picURL, i.price, i.title, i.type, i.quantity, i.recentSalesCount, i.salesCount, i.status, i.score, i.deListTime, i.listTime, i.sellerCids "
                + " from item%s as i LEFT JOIN "+genShardQuery("item_extra_%s", userId)+" as e ON i.numIid = e.numIid  where i.userId = ? and i.status <> ?  and i.score >= ? and i.score <= ? and (i.cid <> 50023725 and i.cid <> 50023728)";

        sql = genShardQuery(sql, userId);
        log.info("[sql]" + sql);
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLikeWithAnd(search);
            sql += " and " + like;
        }

        if (sort == 0)
            sql += " order by i.salesCount desc limit ?, ?";
        else if (sort == -1)
            sql += " order by i.score desc limit ?, ?";
        else if (sort == 1)
            sql += " order by i.score limit ?, ?";
        else if (sort == 2) {
            sql += " order by e.created limit ?, ?";
        } else {
            sql += " order by e.created limit ?, ?";
        }

        log.error(sql);

        return new JDBCExecutor<List<ItemPlay>>(true, dp, sql, userId, status, lowBegin, topEnd, offset, limit) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    static String ITEM_SQL_WITH_OPTIMISED = " i.userId, i.numIid, i.cid, i.picURL, i.price, i.title, i.type, i.quantity, i.salesCount, i.status, i.score, i.deListTime, i.listTime, i.sellerCids, t.isOptimised, e.created ";

    public static List<ItemPlay> findOnlineByUserWithscoreAndCatidOptimised(Long userId,
            int offset, int limit, String search, Long numIid, int lowBegin, int topEnd,
            int sort, int status, String catId, Long taobaoCatId, int optimised) {
        log.info(format(
                "countOnlineByUserWithScore:userId, lowBegin, topEnd, search".replaceAll(", ", "=%s, ") + "=%s",
                userId, lowBegin, topEnd, search));

        String sql = "select "
                + ITEM_SQL_WITH_OPTIMISED
                + " from item%s as i LEFT JOIN title_optimised as t ON i.numIid = t.numIid ";
        sql = genShardQuery(sql, userId);
        
        sql += "LEFT JOIN item_extra_%s as e ON i.numIid = e.numIid where i.userId = ? and i.status <> ?  and i.score >= ? and i.score <= ? and (i.cid <> 50023725 and i.cid <> 50023728)";
        sql = genShardQuery(sql, userId);
        
        log.info("[sql]" + sql);

        if (taobaoCatId != null && taobaoCatId > 0) {
            sql += " and i.cid = " + taobaoCatId + " ";
        }

        if (!StringUtils.isBlank(search)) {
            search = search.trim();
            if (search.length() < 3) {
                sql += " and ";
                sql += appendTitleLikeLeftJoin(search);
                sql += " ";
            } else {
                String[] splits = search.split(" ");
                for (String string : splits) {
                    if (StringUtils.isEmpty(string)) {
                        continue;
                    }

                    sql += " and ";
                    sql += "i.title like '%";
                    sql += CommonUtils.escapeSQL(string);
                    sql += "%' ";
                }
            }
        }
        if (!StringUtils.isEmpty(catId)) {
            String like = appendSellerCidsLikeLeftJoin(catId);
            sql += " and " + like;
        }
        
        if (numIid != null && numIid > 0) {
            sql += "and i.numIid = " + numIid;
        }

        if (optimised == TitleOptimised.Status.OPTIMISED) {
            sql += " and i.numIid in (select numIid from title_optimised where userId = " + userId + " "
                    + " )";
        } else if (optimised == TitleOptimised.Status.UN_OPTIMISED) {
            sql += " and i.numIid not in (select numIid from title_optimised where userId = " + userId + " "
                    + " )";
        }

        if (sort == 0)
            sql += " order by i.salesCount desc limit ?, ?";
        else if (sort == -1)
            sql += " order by i.score desc limit ?, ?";
        else if (sort == 1)
            sql += " order by i.score limit ?, ?";
        else if (sort == 2)
        	sql += " order by e.created desc limit ?, ?";

        log.error(sql);

        return new JDBCExecutor<List<ItemPlay>>(true, dp, sql, userId, status, lowBegin, topEnd, offset, limit) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItemWithOptimised(rs));
                }
                return itemList;
            }
        }.call();
    }

    public static List<ItemPlay> findOnlineByUserWithscoreAndCatid(Long userId, int offset, int limit, String search, Long numIid,
            int lowBegin, int topEnd, int sort, int status, String catId, String taobaoCatId) {
        log.info(format(
                "countOnlineByUserWithScore:userId, lowBegin, topEnd, search".replaceAll(", ", "=%s, ") + "=%s",
                userId, lowBegin, topEnd, search));
        String sql = StringUtils.EMPTY;
        if(sort == 1) {
        	sql = "select "+ JOIN_ITEM_SQL+ " from item%s as i";
        	sql = genShardQuery(sql, userId);
        	sql+= " LEFT JOIN item_extra_%s as e ON i.numIid = e.numIid  where i.userId = ? and status <> ?  and score >= ? and score <= ? and (cid <> 50023725 and cid <> 50023728)";
        } else {
        	sql = "select "
                    + ITEM_SQL
                    + " from item%s where userId = ? and status <> ?  and score >= ? and score <= ? and (cid <> 50023725 and cid <> 50023728)";

        }
        
        sql = genShardQuery(sql, userId);
        
        log.info("[sql]" + sql);
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sql += " and " + like;
        }
        
        if (numIid != null && numIid > 0) {
            sql += "and numIid = " + numIid;
        }
        
        if (!StringUtils.isEmpty(taobaoCatId)) {
            sql += " and cid = " + taobaoCatId + " ";
        }

        if (!catId.isEmpty()) {
            String like = appendSellerCidsLike(catId);
            sql += " and " + like;
        }

        if (sort == 0) {
        	sql += " order by recentSalesCount desc limit ?, ?";
        } else if (sort == 1) {
        	sql += " order by e.created desc limit ?, ?";
        } else {
        	sql += " order by recentSalesCount desc limit ?, ?";
        }

        log.error(sql);

        return new JDBCExecutor<List<ItemPlay>>(true, dp, sql, userId, status, lowBegin, topEnd, offset, limit) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    public static List<ItemPlay> findForBatchTitleOptimise(User user, long sellerCid, long itemCid, int status,
            boolean allSale, String title, int startScore, int endScore, boolean isNewRule) {
        StringBuilder sb = new StringBuilder(" select ");
        sb.append(ITEM_SQL);
//        sb.append(" from item%s  where  numIid = 17717581318 and userId = ");
        sb.append(" from item%s  where  userId = ");
        sb.append(user.getId());
        sb.append("  and  (cid <> 50023725 and cid <> 50023728)  ");

        if (ItemPlay.Status.ONSALE == status || ItemPlay.Status.INSTOCK == status) {
            sb.append(" and status  = ");
            sb.append(status);
        }

        if (sellerCid > 0L) {
            sb.append(" and ");
            sb.append(appendSellerCidsLike(String.valueOf(sellerCid)));
        }
        if (itemCid > 0L) {
            sb.append(" and cid = " + itemCid);
        }
        if (!allSale) {
            sb.append(" and salesCount = 0 ");
//            sb.append(" and score < 46 ");
        }
        
        if (isNewRule == true) {
            if (!StringUtils.isEmpty(title)) {
                String like = appendTitleLike(title);
                sb.append(" and " + like + " ");
            }
            sb.append(" and score >= " + startScore + " and score <= " + endScore +  " ");
        }
        

        String sql = genShardQuery(sb.toString(), user.getId());
        log.warn("[ title batch sql :]" + sql);
        return new JDBCExecutor<List<ItemPlay>>(dp, sql) {
            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    public static List<ItemPlay> findForBatchDelist(User user, Set<Long> sellerCidSet, Set<Long> itemCidSet,
            int status,
            int salesNumRule, int offset, int limit) {
        StringBuilder sb = new StringBuilder(" select ");
        sb.append(ITEM_SQL);
//        sb.append(" from item%s  where  numIid = 17717581318 and userId = ");
        sb.append(" from item%s  where  userId = ? ");

        sb.append("  and  (cid <> 50023725 and cid <> 50023728)  ");

        if (ItemPlay.Status.ONSALE == status || ItemPlay.Status.INSTOCK == status) {
            sb.append(" and status  = ");
            sb.append(status);
        }

        if (!CommonUtils.isEmpty(sellerCidSet)) {
            sb.append(" and ");
            sb.append(appendSellerCidsLike(StringUtils.join(sellerCidSet, " ")));
        }
        if (!CommonUtils.isEmpty(itemCidSet)) {
            sb.append(" and cid in ( " + StringUtils.join(itemCidSet, ",") + ") ");
        }
        if (salesNumRule == DelistSalesNumRule.NoSales) {
            sb.append(" and salesCount = 0 ");
//            sb.append(" and score < 46 ");
        } else if (salesNumRule == DelistSalesNumRule.HasSales) {
            sb.append(" and salesCount > 0 ");
        } else {

        }

        String sql = genShardQuery(sb.toString(), user.getId());

        sql += " order by numIid limit ?, ? ";

//        log.warn("[ batch delist sql :]" + sql);
        return new JDBCExecutor<List<ItemPlay>>(dp, sql, user.getId(), offset, limit) {
            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    public static List<ItemPlay> findOnlineWithSalesDesc(Long userId, int offset, int limit) {
        String sql = "select "
                + ITEM_SQL
                + " from item%s where userId = ? and status = ? order by salesCount desc, numIid asc limit ?, ? ";

        sql = genShardQuery(sql, userId);

        return new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, ItemPlay.Status.ONSALE, offset, limit) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    public static List<ItemPlay> findOnlineByUserWithArgs(Long userId, int offset, int limit, String search,
            int lowBegin, int topEnd, int sort, int status, String catId, Long cid, boolean includeYouFei) {
//        log.info(format(
//                "countOnlineByUserWithScore:userId, lowBegin, topEnd, search".replaceAll(", ", "=%s, ") + "=%s",
//                userId, lowBegin, topEnd, search));

        String sql = "select "
                + ITEM_SQL
                + " from item%s where userId = ? and status <> ?  and score >= ? and score <= ?";
        if (includeYouFei == false) {
            sql += " and (cid <> 50023725 and cid <> 50023728)";
        }
        sql = genShardQuery(sql, userId);

        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sql += " and " + like;
        }

        if (cid != null && cid > 0) {
            sql += " and cid = " + cid;
        }

        if (!StringUtils.isEmpty(catId)) {
            String like = appendSellerCidsLike(catId);
            sql += " and " + like;
        }

        if (sort == 1)
            sql += " order by score limit ?, ?";
        else if (sort == 2)
            sql += " order by score desc limit ?, ?";
        else if (sort == 3)
            sql += " order by salesCount limit ?, ?";
        else if (sort == 4)
            sql += " order by salesCount desc limit ?, ?";
        else if (sort < 0) {
            sql += " limit ?, ?";
        } else {
            sql += " order by score limit ?, ?";
        }
            

        log.info("[sql]" + sql);
        return new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, status, lowBegin, topEnd, offset, limit) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    public static List<ItemPlay> findOnlineByUserWithArgsIds(Long userId, int offset, int limit, String search,
            int lowBegin, int topEnd, int sort, int status, String catId, Long cid, Set ids) {
        log.info(format(
                "countOnlineByUserWithScore:userId, lowBegin, topEnd, search".replaceAll(", ", "=%s, ") + "=%s",
                userId, lowBegin, topEnd, search));

        String sql = "select "
                + ITEM_SQL
                + " from item%s where userId = ? and status <> ?  and score >= ? and score <= ?"; // and (cid <> 50023725 and cid <> 50023728)";

        sql = genShardQuery(sql, userId);
        log.info("[sql] " + sql);
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sql += " and " + like;
        }

        if (cid != null && cid > 0) {
            sql += " and cid = " + cid;
        }

        if (!StringUtils.isEmpty(catId)) {
            String like = appendSellerCidsLike(catId);
            sql += " and " + like;
        }

        if (!CommonUtils.isEmpty(ids)) {
            String idString = StringUtils.join(ids, ',');
            String idFormat = " and numIid in (%s)";
            sql += format(idFormat, idString);
        }

        if (sort == 1)
            sql += " order by score limit ?, ?";
        else if (sort == 2)
            sql += " order by score desc limit ?, ?";
        else if (sort == 3)
            sql += " order by salesCount limit ?, ?";
        else if (sort == 4)
            sql += " order by salesCount desc limit ?, ?";
        else
            sql += " order by score limit ?, ?";
        return new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, status, lowBegin, topEnd, offset, limit) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    public static long countOnlineByUserCatId(Long userId, String catId) {
        String sql = "select count(*) from item%s where userId = ? and (cid <> 50023725 and cid <> 50023728)";
        sql = genShardQuery(sql, userId);
        if (!StringUtils.isEmpty(catId)) {
            String like = appendSellerCidsLike(catId);
            sql += " and " + like;
        }
        return dp.singleLongQuery(sql, userId);
    }

    public static int countAllBySellerCat(Long userId, Long sellerCid) {
        String sql = "select count(*) from item%s where userId = ? ";
        StringBuilder sb = new StringBuilder(genShardQuery(sql, userId));
        if (!NumberUtil.isNullOrZero(sellerCid)) {
            String like = appendSellerCidsLike(String.valueOf(sellerCid));
            sb.append(" and ");
            sb.append(like);
        }

        sql = sb.toString();
        return (int) dp.singleLongQuery(sql, userId);
    }

    public static int countBySellerCat(Long userId, Long sellerCid, int status) {
        String sql = "select count(*) from item%s where userId = ? and status = ?";
        StringBuilder sb = new StringBuilder(genShardQuery(sql, userId));
        if (!NumberUtil.isNullOrZero(sellerCid)) {
            String like = appendSellerCidsLike(String.valueOf(sellerCid));
            sb.append(" and ");
            sb.append(like);
        }

        sql = sb.toString();

        return (int) dp.singleLongQuery(sql, userId, status);
    }

    public static List<ItemPlay> findOnlineByUserCatId(Long userId, String catId, int offset, int size) {
        log.info(format("findOnlineByUserCatId: userId, catId".replaceAll(", ", "=%s, ") + "=%s", userId, catId));

        String sql = "select " + ITEM_SQL + " from item%s where userId = ? and (cid <> 50023725 and cid <> 50023728)";

        sql = genShardQuery(sql, userId);
        log.info("[sql]" + sql);

        if (!catId.isEmpty()) {
            String like = appendSellerCidsLike(catId);
            sql += " and " + like;
        }

        sql += " order by score limit ?, ?";

        return new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, offset, size) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    public static long countOnSaleByUserCatId(Long userId, String catId) {
//      log.info(format("countOnlineByUserCatId:userId, catId".replaceAll(", ", "=%s, ") + "=%s", userId, catId));

        String sql = "select count(*) from item%s where userId = ? and status = 1 and (cid <> 50023725 and cid <> 50023728)";

        sql = genShardQuery(sql, userId);
        if (!StringUtils.isEmpty(catId)) {
            String like = appendSellerCidsLike(catId);
            sql += " and " + like;
        }

        return dp.singleLongQuery(sql, userId);

    }

    public static List<ItemPlay> findOnSaleByUserCatId(Long userId, String catId, int offset, int size) {
        log.info(format("findOnSaleByUserCatId: userId, catId".replaceAll(", ", "=%s, ") + "=%s", userId, catId));

        String sql = "select " + ITEM_SQL
                + " from item%s where userId = ? and status = 1 and (cid <> 50023725 and cid <> 50023728)";

        sql = genShardQuery(sql, userId);
        log.info("[sql]" + sql);

        if (!catId.isEmpty()) {
            String like = appendSellerCidsLike(catId);
            sql += " and " + like;
        }

        sql += " order by score limit ?, ?";

        return new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, offset, size) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    public static List<ItemPlay> findOnSaleByItemCatId(Long userId, String catId, int offset, int size) {
        log.info(format("findOnSaleByUserCatId: userId, catId".replaceAll(", ", "=%s, ") + "=%s", userId, catId));

        String sql = "select " + ITEM_SQL
                + " from item%s where userId = ? and status = 1 and (cid <> 50023725 and cid <> 50023728)";

        sql = genShardQuery(sql, userId);
        log.info("[sql]" + sql);

        if (!catId.isEmpty()) {
            String like = appendItemCidsLike(catId);
            sql += " and " + like;
        }

        sql += " order by score limit ?, ?";

        return new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, offset, size) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    public static long countOnSaleByItemCatId(Long userId, String catId) {
//      log.info(format("countOnlineByUserCatId:userId, catId".replaceAll(", ", "=%s, ") + "=%s", userId, catId));

        String sql = "select count(*) from item%s where userId = ? and status = 1 and (cid <> 50023725 and cid <> 50023728)";

        sql = genShardQuery(sql, userId);
        if (!StringUtils.isEmpty(catId)) {
            String like = appendItemCidsLike(catId);
            sql += " and " + like;
        }

        return dp.singleLongQuery(sql, userId);

    }

    public static List<ItemPlay> findByKeywords(Long userId, String keywords, int offset, int size) {

        String sql = "select " + ITEM_SQL
                + " from item%s where userId = ? and status = 1 and (cid <> 50023725 and cid <> 50023728)";

        sql = genShardQuery(sql, userId);
        log.info("[sql]" + sql);

        if (!keywords.isEmpty()) {
            String like = appendKeywordsLike(keywords);
            sql += " and " + like;
        }

        sql += " order by score limit ?, ?";

        return new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, offset, size) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    public static String BATCH_INSERT_SQL = "insert into `item%s`(`numIid`,`userId`,`ts`,`title`,`picURL`,`cid`,`listTime`,"
            + "`deListTime`,`price`,`sellerCids`,`type`,`salesCount`,`quantity`,`propsName`,`status`,`score`) "
            + "values";

    public static boolean batchInsert(Long userId, Long ts, Collection<Item> items) {
        if (CommonUtils.isEmpty(items)) {
            return true;
        }

//        StringBuilder insert_sql = new StringBuilder(BATCH_INSERT_SQL);
        StringBuilder insert_sql = new StringBuilder("insert into `item");
        insert_sql.append(DBBuilder.genUserIdHashKey(userId));
        insert_sql.append("`(`numIid`,`userId`,`ts`,`title`,`picURL`,`cid`," +
                "`listTime`,`deListTime`,`price`,`sellerCids`,`type`," +
                "`recentSalesCount`,`salesCount`,`quantity`,`propsName`,`status`,`score`) values");

        int count = 1;
        int size = items.size();
        for (Item item : items) {
            insert_sql.append(genInsertValues(userId, ts, item));
            if (count < size) {
                count++;
                insert_sql.append(',');
            }
        }

        String rawInsertSQl = insert_sql.toString();
        try {
            //String.valueOf(DBBuilder.genUserIdHashKey(userId))
            String query = rawInsertSQl;
            return dp.insert(query) > 0L;
        } catch (Exception e) {
            log.warn("bad query:" + rawInsertSQl);
            log.warn(e.getMessage(), e);
            return false;
        }
    }

    public static Set<Long> findModifiedItems(Map<Long, ItemPlay> inDBItemMap, List<Item> remoteItemsList) {
        Set<Long> modifiedItems = new HashSet<Long>();
        if (CommonUtils.isEmpty(remoteItemsList)) {
            modifiedItems.addAll(inDBItemMap.keySet());
            return modifiedItems;
        }

        for (Item item : remoteItemsList) {
            Long numIid = item.getNumIid();
            ItemPlay itemPlay = inDBItemMap.get(numIid);
            if (itemPlay == null) {
                continue;
            }

            // Now, remote exists in db...
            if (!itemPlay.isEqualToItem(item)) {
                modifiedItems.add(numIid);
            }

            inDBItemMap.remove(numIid);
        }
        return modifiedItems;
    }

    public static List<Item> findToInsertItems(Map<Long, ItemPlay> itemPlayMap, List<Item> remoteList) {
        List<Item> modifiedItems = new ArrayList<Item>();
        if (remoteList == null || remoteList.size() == 0) {
            return ListUtils.EMPTY_LIST;
        }
        for (Item item : remoteList) {
            Long numIid = item.getNumIid();
            ItemPlay itemPlay = itemPlayMap.get(numIid);
            if (itemPlay == null || !itemPlay.isEqualToItem(item)) {
                modifiedItems.add(item);
            }
        }
        return modifiedItems;
    }

    public static String genInsertValues(Long userId, Long ts, Item item) {

    	int recentSalesCount = item.getAfterSaleId() == null ? 0 : item.getAfterSaleId().intValue();
        int salesCount = item.getVolume() == null ? 0 : item.getVolume().intValue();
        int status;
        if (item.getApproveStatus().equals("onsale")) {
            status = Status.ONSALE;
        } else {
            status = Status.INSTOCK;
        }
        int item_score = item.getScore() == null ? 0 : item.getScore().intValue();
        long delistTime = item.getDelistTime() == null ? 0L : item.getDelistTime().getTime();
        long listTime = item.getModified() == null ? 0L : item.getModified().getTime();
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(item.getNumIid());
        sb.append(",");
        sb.append(userId);
        sb.append(",");
        sb.append(ts);
        sb.append(",'");
        sb.append(item.getTitle() == null ? StringUtils.EMPTY : CommonUtils.escapeSQL(item.getTitle()));
        sb.append("','");
        sb.append(item.getPicUrl() == null ? StringUtils.EMPTY : CommonUtils.escapeSQL(item.getPicUrl()));
        sb.append("',");
        sb.append(item.getCid());
        sb.append(",");
        sb.append(listTime);
        sb.append(",");
        sb.append(delistTime);
        sb.append(",");
        sb.append(item.getPrice());
        sb.append(",'");
//        log.info("[insert sellercids :" + item.getSellerCids());
        sb.append(item.getSellerCids() == null ? StringUtils.EMPTY : CommonUtils.escapeSQL(item.getSellerCids()));
        sb.append("',");
        /*
         *         insert_sql.append("`(`numIid`,`userId`,`ts`,`title`,`picURL`,`cid`," +
                "`listTime`,`deListTime`,`price`,`sellerCids`,`type`," +
                "`salesCount`,`quantity`,`propsName`,`status`,`score`) values");
         */
        sb.append(ItemPlay.buildItemType(item, 0));
        sb.append(",");
        sb.append(recentSalesCount);
        sb.append(",");
        sb.append(salesCount);
        sb.append(",");
        sb.append(item.getNum() == null ? 0 : item.getNum().intValue());
        sb.append(",'");
        sb.append(item.getPropsName() == null ? StringUtils.EMPTY : CommonUtils.escapeSQL(item.getPropsName()));
        sb.append("',");
        sb.append(status);
        sb.append(",");
        sb.append(item_score);
        sb.append(")\n");
        return sb.toString();

    }

    public static long countOnSaleByKeywords(Long userId, String keywords) {
//      log.info(format("countOnlineByUserCatId:userId, catId".replaceAll(", ", "=%s, ") + "=%s", userId, catId));

        String sql = "select count(*) from item%s where userId = ? and status = 1 and (cid <> 50023725 and cid <> 50023728)";

        sql = genShardQuery(sql, userId);
        if (!StringUtils.isEmpty(keywords)) {
            String like = appendKeywordsLike(keywords);
            sql += " and " + like;
        }

        return dp.singleLongQuery(sql, userId);

    }

    public int countSellerCat(SellerCat sellerCat) {
//        appendSellerCidsLike(sellerCat.getCid());

        return 0;
    }

    public static List<ItemPlay> findOnlineByUserWithTradeNUm(Long userId, int limitNum) {
//        log.info(format("findOnlineByUserWithTradeNUm".replaceAll(", ", "=%s, ") + "=%s", userId));

        String sql = "select " + ITEM_SQL
                + " from item%s where userId = ? and salesCount > 0 and status = 1 order by salesCount desc limit ?";

        sql = genShardQuery(sql, userId);
//        log.info("[sql]" + sql);

        return new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, limitNum) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    public static List<ItemPlay> findOnlineByUserWithTradeNumZero(Long userId, int limitNum) {
        log.info(format("findOnlineByUserWithTradeNUm".replaceAll(", ", "=%s, ") + "=%s", userId));

        String sql = "select " + ITEM_SQL
                + " from item%s where userId = ? and salesCount = 0 order by salesCount desc limit ?";

        sql = genShardQuery(sql, userId);
        log.info("[sql]" + sql);

        return new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, limitNum) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    public static TMResult findByUserWithExcluded(Long userId, PageOffset po, Collection<Long> excludedIds) {
        StringBuilder sb = new StringBuilder();
        sb.append(" from item%s where userid = ? and status = 1 and (cid <> 50023725 and cid <> 50023728) ");
        if (CommonUtils.isEmpty(excludedIds)) {
//            sb.append("0");
        } else {
            sb.append(" and numIid not in (");
            sb.append(StringUtils.join(excludedIds, ','));
            sb.append(" ) ");
        }

        List<ItemPlay> list = new JDBCExecutor<List<ItemPlay>>(dp, genShardQuery("select " + ITEM_SQL + sb.toString()
                + " limit ? offset ?", userId), userId, po.getPs(), po.getOffset()) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();

        int count = (int) dp.singleLongQuery(genShardQuery("select count(*) " + sb.toString(), userId), userId);

        return new TMResult(list, count, po);
    }

    public static TMResult findByUserAndSearchWithExcludedIgnoreStatus(Long userId, String search, PageOffset po,
            Collection<Long> excludedIds) {
        if (search == null) {
            search = "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(" from item%s where userid = ? and status = 1 and (cid <> 50023725 and cid <> 50023728) ");
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sb.append(" and " + like);
        }

        if (CommonUtils.isEmpty(excludedIds)) {
//            sb.append("0");
        } else {
            sb.append(" and numIid not in (");
            sb.append(StringUtils.join(excludedIds, ','));
            sb.append(" ) ");
        }

        List<ItemPlay> list = new JDBCExecutor<List<ItemPlay>>(dp, genShardQuery("select " + ITEM_SQL + sb.toString()
                + " limit ? offset ?", userId), userId, po.getPs(), po.getOffset()) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();

        int count = (int) dp.singleLongQuery(genShardQuery("select count(*) " + sb.toString(), userId), userId);

        return new TMResult(list, count, po);
    }

    public static TMResult findByUserAndSearchWithExcluded(Long userId, String search, PageOffset po,
            Collection<Long> excludedIds) {
        if (search == null) {
            search = "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(" from item%s where userid = ? and status = 1 and (cid <> 50023725 and cid <> 50023728) ");
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sb.append(" and " + like);
        }

        if (CommonUtils.isEmpty(excludedIds)) {
//            sb.append("0");
        } else {
            sb.append(" and numIid not in (");
            sb.append(StringUtils.join(excludedIds, ','));
            sb.append(" ) ");
        }

        List<ItemPlay> list = new JDBCExecutor<List<ItemPlay>>(dp, genShardQuery("select " + ITEM_SQL + sb.toString()
                + " limit ? offset ?", userId), userId, po.getPs(), po.getOffset()) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();

        int count = (int) dp.singleLongQuery(genShardQuery("select count(*) " + sb.toString(), userId), userId);

        return new TMResult(list, count, po);
    }

    public static TMResult findByUserAndSearchWithExcludedInAllcids(Long userId, String search, PageOffset po,
            Collection<Long> excludedIds, String cid, String sellerCid) {
        if (search == null) {
            search = "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(" from item%s where userid = ? and status = 1 ");
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sb.append(" and " + like);
        }
        if (!StringUtils.isEmpty(cid)) {
            String like = appendItemCidsLike(cid);
            sb.append(" and " + like);
        }
        if (!StringUtils.isEmpty(sellerCid)) {
            String like = appendSellerCidsLike(sellerCid);
            sb.append(" and " + like);
        }
        if (CommonUtils.isEmpty(excludedIds)) {
//            sb.append("0");
        } else {
            sb.append(" and numIid not in (");
            sb.append(StringUtils.join(excludedIds, ','));
            sb.append(" ) ");
        }

        List<ItemPlay> list = new JDBCExecutor<List<ItemPlay>>(dp, genShardQuery("select " + ITEM_SQL + sb.toString()
                + " limit ? offset ?", userId), userId, po.getPs(), po.getOffset()) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();

        int count = (int) dp.singleLongQuery(genShardQuery("select count(*) " + sb.toString(), userId), userId);

        return new TMResult(list, count, po);
    }

    public static List<Long> findBaoyouIdsByUser(Long userId) {
        StringBuilder sb = new StringBuilder();
        sb.append(" from item%s where userId = ? and (cid = 50023725 or cid = 50023728) ");
        return new JDBCLongListExecutor(dp, genShardQuery("select numIid " + sb.toString()
                , userId), userId).call();

    }

    public static List<Long> findBigSaleIdsByUser(Long userId, int limit) {
        if (limit <= 0) {
            return new ArrayList<Long>();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(" from item%s where userId = ? and (cid = 50023725 or cid = 50023728) order by salesCount limit ? ");
        return new JDBCLongListExecutor(dp, genShardQuery("select numIid " + sb.toString()
                , userId), userId, limit).call();

    }

    public static TMResult findBaoyouByUser(Long userId, String search, PageOffset po,
            Collection<Long> excludedIds) {
        if (search == null) {
            search = "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(" from item%s where userid = ? and (cid = 50023725 or cid = 50023728) ");
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sb.append(" and " + like);
        }

        if (CommonUtils.isEmpty(excludedIds)) {
//            sb.append("0");
        } else {
            sb.append(" and numIid not in (");
            sb.append(StringUtils.join(excludedIds, ','));
            sb.append(" ) ");
        }

        List<ItemPlay> list = new JDBCExecutor<List<ItemPlay>>(dp, genShardQuery("select " + ITEM_SQL + sb.toString()
                + " limit ? offset ?", userId), userId, po.getPs(), po.getOffset()) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();

        int count = (int) dp.singleLongQuery(genShardQuery("select count(*) " + sb.toString(), userId), userId);

        return new TMResult(list, count, po);
    }

    public static TMResult findByUserAndSearchWithId(Long userId, String search, PageOffset po, Collection<Long> Ids) {
        if (search == null) {
            search = "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(" from item%s where userid = ? and status = 1 and (cid <> 50023725 and cid <> 50023728) ");
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sb.append(" and " + like);
        }

        if (CommonUtils.isEmpty(Ids)) {
//            sb.append("0");
        } else {
            sb.append("and numIid in (");
            sb.append(StringUtils.join(Ids, ','));
            sb.append(" ) ");
        }

        List<ItemPlay> list = new JDBCExecutor<List<ItemPlay>>(dp, genShardQuery("select " + ITEM_SQL + sb.toString()
                + " limit ? offset ?", userId), userId, po.getPs(), po.getOffset()) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();

        int count = (int) dp.singleLongQuery(genShardQuery("select count(*) " + sb.toString(), userId), userId);

        return new TMResult(list, count, po);
    }

    public static List<ItemPlay> findOnlineByUser(Long userId, int offset, int limit, String search, int sort) {

        String sql = "select " + ITEM_SQL
                + " from item%s where userId = ? and status = 1  and (cid <> 50023725 and cid <> 50023728)";

        sql = genShardQuery(sql, userId);
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sql += " and " + like;
        }

        if (sort == 0)
            sql += " order by salesCount desc limit ?, ?";
        else if (sort == -1)
            sql += " order by score desc limit ?, ?";
        else if (sort == 1)
            sql += " order by score limit ?, ?";
        return new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, offset, limit) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    public static List<ItemPlay> findAllByUser(Long userId, int offset, int limit, String search, int sort) {

        String sql = "select " + ITEM_SQL
                + " from item%s where userId = ?  and (cid <> 50023725 and cid <> 50023728)";

        sql = genShardQuery(sql, userId);
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sql += " and " + like;
        }

        if (sort == 0)
            sql += " order by salesCount desc limit ?, ?";
        else if (sort == -1)
            sql += " order by score desc limit ?, ?";
        else if (sort == 1)
            sql += " order by score limit ?, ?";
        return new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, offset, limit) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    public static List<ItemPlay> findUnAdItemByUser(Long userId, int offset, int limit, String search) {
        String sql = "select " + ITEM_SQL
                + " from item%s where userId = ? and status = 1  and (cid <> 50023725 and cid <> 50023728)";

        sql = genShardQuery(sql, userId);
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sql += " and " + like;
        }

        sql += " and type&2=0 order by salesCount desc limit ?, ?";

        return new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, offset, limit) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();

    }
    
    public static List<ItemPlay> findItemBySellerCat(Long userId, int offset, int limit, String search, Long sellerCid,
            int sort) {
        
        String sql = "select " + ITEM_SQL
                + " from item%s where userId = ? and status = 1  and (cid <> 50023725 and cid <> 50023728)";

        sql = genShardQuery(sql, userId);

        if (!NumberUtil.isNullOrZero(sellerCid)) {
            String like = appendSellerCidsLike(String.valueOf(sellerCid));
            sql += " and" + like;
        }

        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sql += " and" + like;
        }

        if (sort == 0) {
            sql += " order by salesCount desc limit ?,?";
        } else if (sort == 1) {
            sql += " order by salesCount asc limit ?,?";
        } else if (sort == 2) {
            sql += " order by  price desc limit ?,?";
        } else if (sort == 3) {
            sql += " order by  price asc limit ?,?";
        } else {
            sql += "limit ?,?";
        }
        
        return new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, offset, limit) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    public static List<ItemPlay> findByIds(Long userId, String ids) {

        List<Long> idsLongArray = new ArrayList<Long>();
        String[] idStrArray = StringUtils.split(ids, ',');
        for (String s : idStrArray) {
            idsLongArray.add(Long.valueOf(s));
        }

        String sql = "select " + ITEM_SQL + " from item%s where numIid in (";

        for (int i = 0, size = idsLongArray.size(); i < size; i++) {
            if (i < size - 1) {
                sql += idsLongArray.get(i) + ",";
            } else {
                sql += idsLongArray.get(i);
            }
        }
        sql += ")";
        return new JDBCExecutor<List<ItemPlay>>(dp, genShardQuery(sql, userId)) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();

        // return JPA
        // .em()
        // .createNativeQuery(genShardQuery("select * from item%s where numIid in (:ids)",
        // userId), ItemPlay.class)
        // .setParameter("ids", idsLongArray).getResultList();
    }
    
    
    public static long countBySearchRules(Long userId, String search, int status,
            String catId, String sellerCatId, boolean isFenxiao) {


        String sql = genSqlBySearchRules(userId, search, catId, sellerCatId, isFenxiao);
        
        sql = "select count(*) " + sql;

        return dp.singleLongQuery(sql, userId, status);
    }

    public static List<ItemPlay> findItemsWithoutPaging(Long userId, String search, int status,
            String catId, String sellerCatId, boolean isFenxiao) {

        String sql = genSqlBySearchRules(userId, search, catId, sellerCatId, isFenxiao);
        
        sql = "select " + ITEM_SQL + sql;
        
        List<ItemPlay> list = new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, status) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();

        return list;
    }
    
    private static String genSqlBySearchRules(Long userId, String search,
            String catId, String sellerCatId, boolean isFenxiao) {
        String sql = " from item%s where userId = ? and status <> ? " +
                " and (cid <> 50023725 and cid <> 50023728) ";

        sql = genShardQuery(sql, userId);
        
        if (isFenxiao == true) {
            sql += " and type & " + Type.IS_FENXIAO + " > 0 ";
        }

        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sql += " and " + like;
        }
        if (!StringUtils.isEmpty(catId)) {
            String like = appendItemCidsLike(catId);
            sql += " and " + like;
        }

        if (!StringUtils.isEmpty(sellerCatId)) {
            if (sellerCatId.equals("-1")) {
                sql += " and sellerCids = '-1' ";
            } else {
                String like = appendSellerCidsLike(sellerCatId);
                sql += " and " + like;
            }
            
        }

        
        return sql;
    }
    
    
    public static TMResult findItemsBySearchRulesWithNumIids(User user, String search, int status,
            String catId, String sellerCatId, boolean isFenxiao, Collection<Long> numIidColl,
            String orderProp,
            boolean isDesc, PageOffset po) {
        
        if (CommonUtils.isEmpty(numIidColl)) {
            return new TMResult(new ArrayList<ItemPlay>(), 0, po);
        }

        Long userId = user.getId();
        String sql = genSqlBySearchRules(userId, search, catId, sellerCatId, isFenxiao);
        
        sql += " and numIid in (" + StringUtils.join(numIidColl, ",") + ") ";

        int count = (int) dp.singleLongQuery("select count(*) " + sql, userId, status);

        if (!StringUtils.isEmpty(orderProp)) {
            sql += " order by " + orderProp + " ";
            //升序
            if (isDesc == false) {
                sql += " asc ";
            } else {
                sql += " desc ";
            }
        }

        sql += " limit ? offset ? ";

        long sevenDayTs = 7 * DateUtil.DAY_MILLIS;
        long lastSunDayTs = 1108828800000L;//2005-02-20 00:00:00  一个周日的ts
        long currentTs = System.currentTimeMillis();
        long relativeTs = currentTs - DateUtil.findThisWeekStart(currentTs);
        long baseTs = lastSunDayTs + relativeTs;

        sql = "select " + ITEM_SQL + ", ((deListTime - " + baseTs + ") % " + sevenDayTs + ") as delist "
                + sql;
        //log.info(sql);

        List<ItemPlay> list = new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, status, po.getPs(), po.getOffset()) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();

        list = fixTradeNum(user, list);
        return new TMResult(list, count, po);
    }
    
    
    public static TMResult findItemsBySearchRules(User user, String search, int status,
            String catId, String sellerCatId, boolean isFenxiao, 
            String orderProp,
            boolean isDesc, PageOffset po) {
        
        Set<Long> excludeIdColl = new HashSet<Long>();
        
        return findItemsBySearchRules(user, search, status, catId, sellerCatId, isFenxiao,
                excludeIdColl, orderProp, isDesc, po);
    }

    public static TMResult findItemsBySearchRules(User user, String search, int status,
            String catId, String sellerCatId, boolean isFenxiao, Collection<Long> excludeIdColl,
            String orderProp,
            boolean isDesc, PageOffset po) {

        Long userId = user.getId();
        String sql = genSqlBySearchRules(userId, search, catId, sellerCatId, isFenxiao);
        
        if (CommonUtils.isEmpty(excludeIdColl)) {
            
        } else {
            sql += " and numIid not in (" + StringUtils.join(excludeIdColl, ",") + ") ";
        }

        int count = (int) dp.singleLongQuery("select count(*) " + sql, userId, status);

        if (!StringUtils.isEmpty(orderProp)) {
            sql += " order by " + orderProp + " ";
            //升序
            if (isDesc == false) {
                sql += " asc ";
            } else {
                sql += " desc ";
            }
        }

        sql += " limit ? offset ? ";

        long sevenDayTs = 7 * DateUtil.DAY_MILLIS;
        long lastSunDayTs = 1108828800000L;//2005-02-20 00:00:00  一个周日的ts
        long currentTs = System.currentTimeMillis();
        long relativeTs = currentTs - DateUtil.findThisWeekStart(currentTs);
        long baseTs = lastSunDayTs + relativeTs;

        sql = "select " + ITEM_SQL + ", ((deListTime - " + baseTs + ") % " + sevenDayTs + ") as delist "
                + sql;
        //log.info(sql);

        List<ItemPlay> list = new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, status, po.getPs(), po.getOffset()) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();

        list = fixTradeNum(user, list);
        return new TMResult(list, count, po);
    }

    public static TMResult findWithOrder(User user, String search, int status, String catId, String orderProp,
            boolean isOrderAsc, PageOffset po) {

        Long userId = user.getId();
        String sql = " from item%s where userId = ? and status <> ? and (cid <> 50023725 and cid <> 50023728)";

        sql = genShardQuery(sql, userId);

        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sql += " and " + like;
        }

        if (!StringUtils.isEmpty(catId)) {
            String like = appendSellerCidsLike(catId);
            sql += " and " + like;
        }
        log.info("[sql]" + sql);
        int count = (int) dp.singleLongQuery("select count(*) " + sql, userId, status);

        if (!StringUtils.isEmpty(orderProp)) {
            sql += " order by " + orderProp + " ";
            //升序
            if (isOrderAsc == true) {
                sql += " asc ";
            } else {
                sql += " desc ";
            }
        }

        sql += " limit ? offset ? ";

        long sevenDayTs = 7 * DateUtil.DAY_MILLIS;
        long lastSunDayTs = 1108828800000L;//2005-02-20 00:00:00  一个周日的ts
        long currentTs = System.currentTimeMillis();
        long relativeTs = currentTs - DateUtil.findThisWeekStart(currentTs);
        long baseTs = lastSunDayTs + relativeTs;

        sql = "select " + ITEM_SQL + ", ((deListTime - " + baseTs + ") % " + sevenDayTs + ") as delist "
                + sql;
        log.info(sql);

        List<ItemPlay> list = new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, status, po.getPs(), po.getOffset()) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();

        list = fixTradeNum(user, list);
        return new TMResult(list, count, po);
    }

    static String QueryForCountUnAdItemByUser = "select count(*) from item%s where userId = ? and status = 1 and type&2=0 and (cid <> 50023725 and cid <> 50023728) ";

    public static long countUnAdItemByUser(Long userId, String search) {

        if (StringUtils.isEmpty(search)) {
            return dp.singleLongQuery(genShardQuery(QueryForCountUnAdItemByUser, userId), userId);
        }

        String sql = genShardQuery(QueryForCountUnAdItemByUser, userId);
        String like = appendTitleLike(search);
        sql = sql + " and " + like;
        return dp.singleLongQuery(sql, userId);
    }

    public static long countByUser(Long userId) {
        String query = genShardQuery("select count(*) from item%s where userId = ?", userId);
        log.warn("query :" + query);

        return ItemPlay.dp.singleLongQuery(query, userId);
    }

    public static long countByUser(Long userId, String search) {
        if (StringUtils.isEmpty(search)) {
            return ItemPlay.dp
                    .singleLongQuery(genShardQuery("select count(*) from item%s where userId = ?", userId), userId);
        }
        String like = appendTitleLike(search);

        String sql = genShardQuery("select count(*) from item%s where userId = ? and ", userId);
        sql += like;
        return dp.singleLongQuery(sql, userId);
    }

    public static String appendTitleLike(String key) {

        StringBuilder sb = new StringBuilder(" (( 0 = 1)");
        String[] keys = key.split("\\s");

        for (String split : keys) {
            if (StringUtils.isBlank(split)) {
                continue;
            }

            sb.append(" or (title like '%");
            sb.append(CommonUtils.escapeSQL(split));
            sb.append("%')");
        }
        sb.append(") ");

        return sb.toString();
    }

    static String appendTitleLikeWithAnd(String key) {

        StringBuilder sb = new StringBuilder("(( 1 = 1)");
        String[] keys = key.split("\\s");

        for (String split : keys) {
            if (StringUtils.isBlank(split)) {
                continue;
            }

            sb.append(" and (i.title like '%");
            sb.append(CommonUtils.escapeSQL(split));
            sb.append("%')");
        }
        sb.append(")");

        return sb.toString();
    }

    static String appendTitleLikeLeftJoin(String key) {

        StringBuilder sb = new StringBuilder("(( 0 = 1)");
        String[] keys = key.split("\\s");

        for (String split : keys) {
            if (StringUtils.isBlank(split)) {
                continue;
            }

            sb.append(" or (i.title like '%");
            sb.append(CommonUtils.escapeSQL(split));
            sb.append("%')");
        }
        sb.append(")");

        return sb.toString();
    }

    public static String appendSellerCidsLike(String catId) {

        StringBuilder sb = new StringBuilder("(( 0 = 1)");
        String[] keys = catId.split("\\s");

        for (String split : keys) {
            if (StringUtils.isBlank(split)) {
                continue;
            }

            sb.append(" or (sellerCids like '%");
            sb.append(CommonUtils.escapeSQL(split));
            sb.append("%')");
        }
        sb.append(")");

        return sb.toString();
    }

    public static String appendItemCidsLike(String catId) {

        StringBuilder sb = new StringBuilder("(( 0 = 1)");
        String[] keys = catId.split("\\s");

        for (String split : keys) {
            if (StringUtils.isBlank(split)) {
                continue;
            }

            sb.append(" or (cid like '%");
            sb.append(CommonUtils.escapeSQL(split));
            sb.append("%')");
        }
        sb.append(")");

        return sb.toString();
    }

    static String appendKeywordsLike(String key) {

        StringBuilder sb = new StringBuilder("(( 1 = 1)");
        String[] keys = key.split("\\s");

        for (String split : keys) {
            if (StringUtils.isBlank(split)) {
                continue;
            }

            sb.append(" and (title like '%");
            sb.append(CommonUtils.escapeSQL(split));
            sb.append("%')");
        }
        sb.append(")");

        return sb.toString();
    }

    static String appendSellerCidsLikeLeftJoin(String catId) {

        StringBuilder sb = new StringBuilder("(( 0 = 1)");
        String[] keys = catId.split("\\s");

        for (String split : keys) {
            if (StringUtils.isBlank(split)) {
                continue;
            }

            sb.append(" or (i.sellerCids like '%");
            sb.append(CommonUtils.escapeSQL(split));
            sb.append("%')");
        }
        sb.append(")");

        return sb.toString();
    }

    static String appendNumIidSet(Set<Long> pops) {
        StringBuilder sb = new StringBuilder("(( 0 = 1)");
        if (pops != null && pops.size() > 0) {
            for (Long numIid : pops) {
                sb.append(numIid + ",");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    static String QueryForFindCidList = "select cid from item%s where userId = ?";

    public static Set<Long> findCidList(User user, int limit) {
        return new JDBCSetLongExecutor(dp, genShardQuery(QueryForFindCidList, user.getId()), user.getId()).call();
    }

    public static Map<Long, Integer> cidCount(User user) {
        return new JDBCMapLongIntExecutor(
                dp,
                genShardQuery(
                        "select cid,count(*) from item%s where userId = ? group by cid ",
                        user.getId()), user.getId()).call();
    }

    public static Map<Long, Integer> cidOnSaleCount(User user) {
        return new JDBCMapLongIntExecutor(
                dp,
                genShardQuery(
                        "select cid,count(*) from item%s where userId = ? and status = 1 group by cid ",
                        user.getId()), user.getId()).call();
    }

    public static Map<Long, Integer> cidStatusCount(User user, int status) {
        return new JDBCMapLongIntExecutor(
                dp,
                genShardQuery(
                        "select cid,count(*) from item%s where userId = ?  and status = ? group by cid ",
                        user.getId()), user.getId(), status).call();
    }

    public static boolean setItemType(Long userId, Long numIid, int type) {
        return dp.update(genShardQuery("update item%s set type = ? where numIid = ? ", userId), type, numIid) > 0;
    }

    public static boolean setTitle(Long userId, Long numIid, String newTitle) {
        return dp.update(genShardQuery("update item%s set title = ? where numIid = ? ", userId), newTitle, numIid) > 0;
    }

    public static boolean setStatus(Long userId, Long numIid, int status) {
        return dp.update(genShardQuery("update item%s set status = ? where numIid = ? ", userId), status, numIid) > 0;
    }

    static String FIND_NUMIID_WITH_TITILE = "select numIid, title from item%s where nick = ? ";

    public static Map<Long, String> findNumIidWithTitle(Long userId, String userNick) {
        return new JDBCMapStringExecutor(dp, genShardQuery(FIND_NUMIID_WITH_TITILE, userId), userNick)
                .call();
    }

    static String FIND_NUMIIDS_WITH_USER = "select numIid from item%s where userId = ? ";

    public static Set<Long> findNumIidWithUser(Long userId) {
        return new JDBCSetLongExecutor(dp, genShardQuery(FIND_NUMIIDS_WITH_USER, userId), userId).call();
    }
    
    static String FIND_NUMIIDS_WITH_USER_STATUS_1 = "select numIid from item%s where userId = ? and status = 1";
    
    public static Set<Long> findNumIidsByUserStatus(Long userId){
        return new JDBCSetLongExecutor(dp, genShardQuery(FIND_NUMIIDS_WITH_USER_STATUS_1, userId),userId).call();
    }

    public static List<Long> findNumIidListWithUser(Long userId) {
        return new JDBCLongListExecutor(dp, genShardQuery(FIND_NUMIIDS_WITH_USER, userId), userId).call();
    }

    static String FIND_DELIST_TIME_WITH_USER = "select deListTime from item%s where userId = ? ";

    public static Set<Long> findDelistTimeArrayWithUser(Long userId) {
        return new JDBCSetLongExecutor(dp, genShardQuery(FIND_DELIST_TIME_WITH_USER, userId), userId)
                .call();
    }

    public static List<Long> findOnlineItemDelistTimeListByUserId(Long userId) {
        String query = "select deListTime from item%s where userId = ? and status = 1 and  (cid <> 50023725 and cid <> 50023728) ";
        query = genShardQuery(query, userId);

        return new JDBCExecutor<List<Long>>(dp, query, userId) {

            @Override
            public List<Long> doWithResultSet(ResultSet rs) throws SQLException {

                List<Long> delistTimeList = new ArrayList<Long>();

                while (rs.next()) {
                    delistTimeList.add(rs.getLong(1));
                }

                return delistTimeList;
            }
        }.call();
    }

    public static final String formTableName(Long userId) {
        return ItemPlay.TABLE_NAME + DBBuilder.genUserIdHashKey(userId);
    }

    public static String genShardQuery(String query, String key) {
        query = query.replaceAll("%s", "~~");
        query = query.replaceAll("%", "##");
        query = query.replaceAll("~~", "%s");

        String formQuery = String.format(query, key);
        return formQuery.replaceAll("##", "%");
    }

    public static String genShardQuery(String query, Long userId) {
        return genShardQuery(query, String.valueOf(DBBuilder.genUserIdHashKey(userId)));
    }

    final static String FIND_NUMIID_WITH_SCORE_QUERY = "select score as res, count(*) as count from item%s where userId = ? and (cid <> 50023725 and cid <> 50023728)  group by res";

    public static Map<Integer, Integer> findItemScoreSpread(Long id, String userNick) {
        return new JDBCMapIntIntExecutor(
                dp,
                genShardQuery(FIND_NUMIID_WITH_SCORE_QUERY, id),
                id).call();
    }

    public static boolean delete(Long userId, Long numIid) {
        return dp.update(genShardQuery("delete from item%s where `numIid` = ? ", userId), numIid) > 0L;
    }

    public static boolean deleteAll(Long userId) {
        return dp.update(genShardQuery("delete from item%s where `userId` = ? ", userId), userId) > 0L;
    }

    public static long countTitleScoreSmaller(Long userId, int score) {
        return dp.singleLongQuery(genShardQuery("select count(*) from item%s where userId = ? and score < ?", userId),
                userId, score);
    }

    public static long countOnsaleItemByuserId(Long userId) {
        return dp.singleLongQuery(genShardQuery("select count(*) from item%s where userId = ? and status = 1", userId),
                userId);
    }

    public static long countInStockItemByuserId(Long userId) {
        return dp.singleLongQuery(genShardQuery("select count(*) from item%s where userId = ? and status = 0", userId),
                userId);
    }

    public static long countTitleAvgScore(Long userId) {
        return dp.singleLongQuery(genShardQuery("select avg(score) from item%s where userId = ? ", userId), userId);
    }

    static String QUERY_ALL_ITEM_IDS_ON_SALE = "select numIid from `item%s`where status & " + ItemPlay.Status.ONSALE
            + " > 0  and userId = ?";

    public static Set<Long> allSaleItemIds(Long userId) {
        return new JDBCLongSetExecutor(dp, genShardQuery(QUERY_ALL_ITEM_IDS_ON_SALE, userId), userId)
                .call();
    }

    public static List<Long> toIdsList(List<ItemPlay> items) {
        if (CommonUtils.isEmpty(items)) {
            return ListUtils.EMPTY_LIST;
        }
        List<Long> ids = new ArrayList<Long>();
        for (ItemPlay itemPlay : items) {
            ids.add(itemPlay.getNumIid());
        }
        return ids;
    }

    public static Map<Long, Boolean> getOptimisedMap(List<ItemPlay> items) {
        if (CommonUtils.isEmpty(items)) {
            return MapUtils.EMPTY_MAP;
        }
        Map<Long, Boolean> map = new HashMap<Long, Boolean>();
        for (ItemPlay itemPlay : items) {
            map.put(itemPlay.getNumIid(), itemPlay.isOptimised());
        }
        return map;
    }
    
    public static Map<Long, Long> getCreatedMap(List<ItemPlay> items) {
        if (CommonUtils.isEmpty(items)) {
            return MapUtils.EMPTY_MAP;
        }
        Map<Long, Long> map = new HashMap<Long, Long>();
        for (ItemPlay itemPlay : items) {
            map.put(itemPlay.getNumIid(), itemPlay.created);
        }
        return map;
    }

    public static Set<Long> toIdsSet(List<Item> items) {
        if (CommonUtils.isEmpty(items)) {
            return SetUtils.EMPTY_SET;
        }
        Set<Long> ids = new HashSet<Long>();
        for (Item item : items) {
            ids.add(item.getNumIid());
        }
        return ids;
    }

    public static void deleteAll(Long userId, Collection<Long> existIds) {
        if (CommonUtils.isEmpty(existIds)) {
            return;
        }

        StringBuilder sb = new StringBuilder("delete from item%s where numIid in  (");
        sb.append(StringUtils.join(existIds, ','));
        sb.append(")");

        long deleteNum = dp.update(genShardQuery(sb.toString(), userId));
        log.info("[delete num:]" + deleteNum);
    }

    public static ItemThumb ensure(User user, ItemThumb thumb) {
        if (thumb.getFullTitle() != null && thumb.getPrice() > 0) {
            return thumb;
        }

        ItemPlay item = ItemDao.findByNumIid(user.getId(), thumb.getId());
        if (item == null) {
            Item tbItem = ApiJdpAdapter.get(user).findItem(user, thumb.getId());
            if (tbItem == null) {
                return null;
            }
            thumb.setFullTitle(tbItem.getTitle());
            thumb.setPrice(NumberUtil.getIntFromPrice(tbItem.getPrice()));
            thumb.setPicPath(tbItem.getPicUrl());
            return null;
        } else {
            thumb.setPeriodSoldQuantity(thumb.getTradeNum());
            thumb.setPicPath(item.getPicURL());
            thumb.setPrice((int) (item.getPrice() * 100));
            thumb.setSellerId(user.getId());
            thumb.setWangwang(user.getUserNick());
            thumb.setFullTitle(item.getTitle());
        }
        return thumb;
    }

    /*public static class Categories {
        public Long cid;

        public String name;

        public int num;

        public Categories(Long cid, String name, int num) {
            this.cid = cid;
            this.name = name;
            this.num = num;
        }

        public List toList() {
            List list = new ArrayList();

            list.add(this.name);
            list.add(this.num);
            list.add(this.cid);

            return list;

        }
    }

    static String QueryForCategories = "select parentCid,count(numIid) from " + ItemPlay.TABLE_NAME
            + " where userId= ? and status = \'" + ItemPlay.Status.ONSALE
            + "\' group by parentCid order by count(numIid) desc";

    public static List<Categories> getBusinessCategories(User user) {

        if (user == null) {
            return null;
        }

        return new JDBCExecutor<List<Categories>>(itemDispatcher, QueryForCategories, user.getId()) {

            @Override
            public List<Categories> doWithResultSet(ResultSet rs) throws SQLException {

                List<Categories> categories = new ArrayList<Categories>();

                while (rs.next()) {
                    Long cid = rs.getLong(1);
                    int num = rs.getInt(2);
                    String name = "";
                    ItemCatPlay itemCat = ItemCatPlay.find("byCid", cid).first();
                    if (itemCat != null) {
                        name = itemCat.name;
                    }
                    categories.add(new Categories(cid, name, num));
                }

                return categories;
            }
        }.call();

    }*/

    public static List<ItemPlay> fixTradeNum(User user, List<ItemPlay> items) {
        if (CommonUtils.isEmpty(items)) {
            return ListUtils.EMPTY_LIST;
        }

        List<ItemPlay> cachedItems = UserHasTradeItemCache.getByUser(user, 8000);
        Map<Long, Integer> itemSaleMap = new HashMap<Long, Integer>();
        for (ItemPlay itemPlay : cachedItems) {
            itemSaleMap.put(itemPlay.getNumIid(), itemPlay.getSalesCount());
        }

        for (ItemPlay itemPlay : items) {
            Long numIid = itemPlay.getNumIid();
            Integer sale = itemSaleMap.get(numIid);
            if (sale == null) {
                continue;
            }
            itemPlay.setSalesCount(sale);
        }

        return items;
    }

    public static Map<Long, Long> findRecentDelist(User user, int maxNum) {
        return new JDBCMapLongExecutor(dp, genShardQuery("select numIid,deListTime from `item%s` where status & "
                + ItemPlay.Status.ONSALE + " > 0  and userId = ? and (cid <> 50023725 and cid <> " +
                "50023728) and delistTime > ? order by delistTime limit ?", user.getId()), user.getId(),
                System.currentTimeMillis(), maxNum) {
        }.call();
    }

    public static String checkRelationSQL(String sql, Long userId, String title, String cid, String sellerCid,
            double itemPriceMin
            , double itemPriceMax, int itemState, int relateState) {
        sql = genShardQuery(sql, userId);
        //log.info("[sql]" + sql);

        if (!StringUtils.isEmpty(title)) {
            String like = appendKeywordsLike(title);
            sql += " and " + like;
        }
        if (!StringUtils.isEmpty(cid)) {
            String like = appendItemCidsLike(cid);
            sql += " and " + like;
        }
        if (!StringUtils.isEmpty(sellerCid)) {
            String like = appendSellerCidsLike(sellerCid);
            sql += " and " + like;
        }

        if (itemPriceMin != 0) {
            sql += " and price > " + itemPriceMin;
        }
        if (itemPriceMax != 0) {
            sql += " and price < " + itemPriceMax;
        }
        if (itemState != 2) {
            sql += " and status = " + itemState;
        }
        if (relateState != 2) {
            if (relateState == 1) {
                sql += " and type & " + Type.RELATED + " > 0 ";
            }
            else {
                sql += " and (type & " + Type.RELATED + " = 0 or type is null ) ";
            }
        }

        return sql;
    }

    public static List<ItemPlay> findRelationByCondition(Long userId, String title, String cid, String sellerCid,
            double itemPriceMin
            , double itemPriceMax, int itemState, int relateState, PageOffset po) {
        String sql = "select " + ITEM_SQL
                + " from item%s where userId = ? and (cid <> 50023725 and cid <> 50023728) ";

        sql = checkRelationSQL(sql, userId, title, cid, sellerCid, itemPriceMin, itemPriceMax, itemState, relateState);

        sql += " limit ?,? ";

        return new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, po.getOffset(), po.getPs()) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    public static List<ItemPlay> findRelationByCondition(Long userId, String title, String cid, String sellerCid,
            double itemPriceMin
            , double itemPriceMax, int itemState, int relateState) {
        String sql = "select " + ITEM_SQL
                + " from item%s where userId = ? and (cid <> 50023725 and cid <> 50023728) ";

        sql = checkRelationSQL(sql, userId, title, cid, sellerCid, itemPriceMin, itemPriceMax, itemState, relateState);

        return new JDBCExecutor<List<ItemPlay>>(dp, sql, userId) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    public static long countRelationByCondition(Long userId, String title, String cid, String sellerCid,
            double itemPriceMin
            , double itemPriceMax, int itemState, int relateState) {
        String sql = " select count(*) from item%s where userId = ? and (cid <> 50023725 and cid <> 50023728) ";

        sql = checkRelationSQL(sql, userId, title, cid, sellerCid, itemPriceMin, itemPriceMax, itemState, relateState);

        return dp.singleLongQuery(sql, userId);
    }

    public static List<ItemPlay> findRelationByAll(Long userId, int offset, int limit, String search, int sort) {

        String sql = "select " + ITEM_SQL
                + " from item%s where userId = ? and (cid <> 50023725 and cid <> 50023728)";

        sql += " and (type & " + Type.RELATED + " = 0 or type is null ) ";

        sql = genShardQuery(sql, userId);
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sql += " and " + like;
        }

        if (sort == 0)
            sql += " order by salesCount desc limit ?, ?";
        else if (sort == -1)
            sql += " order by score desc limit ?, ?";
        else if (sort == 1)
            sql += " order by score limit ?, ?";
        return new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, offset, limit) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    public static List<ItemPlay> findUnRelationByAll(Long userId, int offset, int limit, String search, int sort) {

        String sql = "select " + ITEM_SQL
                + " from item%s where userId = ? and (cid <> 50023725 and cid <> 50023728)";

        sql += " and type & " + Type.RELATED + " > 0 ";

        sql = genShardQuery(sql, userId);
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sql += " and " + like;
        }

        if (sort == 0)
            sql += " order by salesCount desc limit ?, ?";
        else if (sort == -1)
            sql += " order by score desc limit ?, ?";
        else if (sort == 1)
            sql += " order by score limit ?, ?";
        return new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, offset, limit) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
    }

    public static List<ItemPlay> findPromotionByConditionAndOrder(Long userId, Long activityId, String title,
            String cid, String sellerCid, String order, PageOffset po) {
        String sql = "select " + ITEM_SQL
                + " from item%s where userId = ? and (cid <> 50023725 and cid <> 50023728) ";

        sql = addNumIidByActivityId(sql, activityId, userId);

        sql = checkRelationSQL(sql, userId, title, cid, sellerCid, 0, 0, 1, 2);

        sql = checkOrder(sql, order);

        sql += " limit ?, ?";

        return new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, po.getOffset(), po.getPs()) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();

    }

    public static long countPromotionByConditionAndOrder(Long userId, Long activityId, String title, String cid,
            String sellerCid) {
        String sql = " select count(*) from item%s where userId = ? and (cid <> 50023725 and cid <> 50023728) ";

        sql = addNumIidByActivityId(sql, activityId, userId);

        sql = checkRelationSQL(sql, userId, title, cid, sellerCid, 0, 0, 1, 2);

        return dp.singleLongQuery(sql, userId);
    }

    public static String addNumIidByActivityId(String sql, Long activityId, Long userId) {

        String numIids = "";

        String pro_sql = " select numIid from promotion where userId = ? ";

        if (activityId != null) {
            pro_sql += " and activityId = " + activityId;
        }

        List<String> numIidList = new JDBCExecutor<List<String>>(pro_sql, userId) {
            @Override
            public List<String> doWithResultSet(ResultSet rs) throws SQLException {
                List<String> list = new ArrayList<String>();
                while (rs.next()) {
                    String id = parsedisString(rs);
                    if (id != null)
                        list.add(id);
                }
                return list;
            }
        }.call();

        if (CommonUtils.isEmpty(numIidList)) {
            return sql;
        }

        for (String numIid : numIidList) {
            if (numIids == "") {
                numIids = numIid;
            }
            else {
                numIids += "," + numIid;
            }
        }

        if (numIids == "") {
            return sql;
        }

        sql += " and numIid in ( " + numIids + ") ";

        return sql;
    }

    public static String checkOrder(String sql, String order) {
        if (StringUtils.equals(order, "pu")) {
            sql += " order by price asc ";
        }
        if (StringUtils.equals(order, "pd")) {
            sql += " order by price desc ";
        }
        if (StringUtils.equals(order, "su")) {
            sql += " order by deListTime asc ";
        }
        if (StringUtils.equals(order, "sd")) {
            sql += " order by deListTime desc ";
        }

        return sql;
    }

    //新的promotion分表的
    public static String checkDis(String sql, Long userId, String isDis, boolean isNewPromotion) {

        if (StringUtils.equals(isDis, "all")) {
            return sql;
        }

        String oldPromotionSql = " select numIid from promotion where userId = " + userId + " ";
        String newPromotionSql = " select numIid from " + PromotionPlay.TABLE_NAME + "%s where userId = "
                + userId + " ";
        newPromotionSql = PromotionDao.appendActiveStatusRule(newPromotionSql);
        newPromotionSql = PromotionDao.genShardQuery(newPromotionSql, userId);

        if (StringUtils.equals(isDis, "dis")) {
            sql += " and (numIid in ( " + oldPromotionSql + " ) or numIid in ( " + newPromotionSql + " )) ";
        }

        if (StringUtils.equals(isDis, "undis")) {
            sql += " and numIid not in ( " + oldPromotionSql + " ) and numIid not in ( " + newPromotionSql + " )";
        }

        return sql;
    }

    //新的promotion分表的
    public static String checkMjsDis(String sql, Long userId, String isDis, String items) {

        if (StringUtils.equals(isDis, "all")) {
            return sql;
        }

        if (StringUtils.isEmpty(items)) {
            if (StringUtils.equals(isDis, "dis")) {
                sql += " and 0 = 1 ";
            }

        } else {
            if (StringUtils.equals(isDis, "dis")) {
                sql += " and numIid in ( " + items + " ) ";
            }

            if (StringUtils.equals(isDis, "undis")) {
                sql += " and numIid not in ( " + items + " ) ";
            }
        }

        return sql;
    }

    public static int getStatus(String isOnsale) {

        if (StringUtils.equals(isOnsale, "all")) {
            return 2;
        } else if (StringUtils.equals(isOnsale, "onsale")) {
            return 1;
        } else if (StringUtils.equals(isOnsale, "instock")) {
            return 0;
        } else {
            return 2;
        }

    }

    public static String checkMjsOnsale(String sql, Long userId, String isOnsale) {

        if (StringUtils.equals(isOnsale, "all")) {
            return sql;
        } else if (StringUtils.equals(isOnsale, "onsale")) {
            return sql + " and status = 1 ";
        } else if (StringUtils.equals(isOnsale, "instock")) {
            return sql + " and status = 0 ";
        } else {
            return sql;
        }

    }

    public static String parsedisString(ResultSet rs) throws SQLException {
        Long numIid = rs.getLong(1);

        return String.valueOf(numIid);
    }

    public static List<ItemPlay> findItemByConditionAndOrder(Long userId, String title, String cid, String sellerCid,
            String order, String isDis, PageOffset po, boolean isNewPromotion, int itemStatus) {
        String sql = "select " + ITEM_SQL
                + " from item%s where userId = ? and (cid <> 50023725 and cid <> 50023728) ";

        sql = checkRelationSQL(sql, userId, title, cid, sellerCid, 0, 0, itemStatus, 2);
        long data_1 = System.currentTimeMillis();
        sql = checkDis(sql, userId, isDis, isNewPromotion);
        long data_2 = System.currentTimeMillis();
        sql = checkOrder(sql, order);

        sql += " limit ?, ?";

        List<ItemPlay> itemList = new JDBCExecutor<List<ItemPlay>>(dp, sql, userId,
                po.getOffset(), po.getPs()) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }

        }.call();
        long data_3 = System.currentTimeMillis();

        //log.warn(data_1 + "timetimetimetimetimetimetimetimetimetimetimetimetimetimetimetime" + data_2
        //        + "timetimetimetimetimetimetimetimetimetimetimetimetimetimetimetime" + data_3);

        return itemList;
    }

    public static List<ItemPlay> findMjsItemByConditionAndOrder(Long userId, String title, String cid,
            String sellerCid,
            String order, String isDis, String isOnsale, PageOffset po, String items) {
        String sql = "select " + ITEM_SQL
                + " from item%s where userId = ? and (cid <> 50023725 and cid <> 50023728) ";

        sql = checkRelationSQL(sql, userId, title, cid, sellerCid, 0, 0, getStatus(isOnsale), 2);

        sql = checkMjsDis(sql, userId, isDis, items);

        sql = checkOrder(sql, order);

        sql += " limit ?, ?";

        List<ItemPlay> itemList = new JDBCExecutor<List<ItemPlay>>(dp, sql, userId, po.getOffset(), po.getPs()) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }

        }.call();

        return itemList;
    }

    public static long countItemByConditionAndOrder(Long userId, String title, String cid, String sellerCid,
            String isDis, boolean isNewPromotion, int itemStatus) {
        String sql = " select count(*) from item%s where userId = ? and (cid <> 50023725 and cid <> 50023728) ";

        sql = checkRelationSQL(sql, userId, title, cid, sellerCid, 0, 0, itemStatus, 2);

        sql = checkDis(sql, userId, isDis, isNewPromotion);

        return dp.singleLongQuery(sql, userId);
    }

    public static long countMjsItemByConditionAndOrder(Long userId, String title, String cid, String sellerCid,
            String isDis, String isOnsale, String items) {
        String sql = " select count(*) from item%s where userId = ? and (cid <> 50023725 and cid <> 50023728) ";

        sql = checkRelationSQL(sql, userId, title, cid, sellerCid, 0, 0, getStatus(isOnsale), 2);

        sql = checkMjsDis(sql, userId, isDis, items);

        return dp.singleLongQuery(sql, userId);
    }

    static String updateItemSalesCountSql = "update item%s as i1, (select i2.numIid, coalesce(t.sale,0) as sale from item%s as i2 left join  "
            + " (select numIid, sum(o.num) as sale from order_display_%s as o "
            + " where o.userId = ? and o.payTimeDay >= ? group by numIid) as t "
            + " ON i2.numIid = t.numIid where i2.userId = ?) as t2 set i1.salesCount = t2.sale where i1.numIid = t2.numIid;";

    public static long updateItemSale(long userId) {
        String hashKey = String.valueOf(DBBuilder.genUserIdHashKey(userId));
        String sql = String.format(updateItemSalesCountSql, hashKey, hashKey, hashKey);
        long ts = System.currentTimeMillis() - 30 * DateUtil.DAY_MILLIS;
        long updateNum = JDBCBuilder.update(false, dp.getSrc(), sql, userId, ts, userId);
        return updateNum;
    }

    public static boolean isRecentCreated(Item item) {

        long modifiedMillis = item.getModified().getTime();
        long createdMillis = item.getCreated().getTime();
        long diff = modifiedMillis - createdMillis;
        diff = diff < 0L ? -diff : diff;
        if (diff < 10000L) {
            return true;
        }

        return false;
    }

    public static List<ItemPlay> findbyNumIids(int hashKey, Collection<Long> ids) {

        if (CommonUtils.isEmpty(ids)) {
            return ListUtils.EMPTY_LIST;
        }

        String sql = "select " + ITEM_SQL + " from item" + hashKey + " where numIid in (  "
                + StringUtils.join(ids, ',') + ")";

        return new JDBCItemFetcher(sql).call();
    }

    public static long updateDelistTime(User user, Long numIid, long delistTime) {
        delistTime = ItemPlay.buildDelistFormTime(delistTime);
        String hashKey = String.valueOf(DBBuilder.genUserIdHashKey(user.getId()));
        String sql = "update item" + hashKey + "  set delistTime =  ? where numIid = ?";
        long updateNum = JDBCBuilder.update(false, dp.getSrc(), sql, delistTime, numIid);
        return updateNum;
    }

    public static List<ItemPlay> recentDownItems(User user, int maxFetchItem) {

//        log.info(format("recentDownItems:user, maxFetchItem".replaceAll(", ", "=%s, ") + "=%s", user, maxFetchItem));

        if (user == null) {
            return ListUtils.EMPTY_LIST;
        }

        long curr = System.currentTimeMillis() % DateUtil.WEEK_MILLIS;
//        long targetDiv = curr % DateUtil.WEEK_MILLIS;

        Long userId = user.getId();
        String sql = " select " + ITEM_SQL + " from item%s where userId = ? and status = 1 and delistTime  > 0 "
                + " order by ( delistTime +  " + DateUtil.WEEK_MILLIS + " - " + curr + ") % " + DateUtil.WEEK_MILLIS
                + " limit ?";
        return new JDBCItemFetcher(genShardQuery(sql, userId), userId, maxFetchItem).call();
    }

    public static List<Item> recentDownRawItems(User user, int maxFetchItem) {
        List<Item> res = new ArrayList<Item>();
        List<ItemPlay> plays = recentDownItems(user, maxFetchItem);
        for (ItemPlay itemPlay : plays) {
            res.add(itemPlay.toItem());
        }

        return res;
    }

    public static long addDelistWeekMillis(User user) {
        long end = System.currentTimeMillis();
        long start = end - (DateUtil.WEEK_MILLIS * 3);

        String sql = "update item" + String.valueOf(DBBuilder.genUserIdHashKey(user.getId()))
                + "  set delistTime =  delistTime + ?  where userId = ? and status = 1 and delistTime between ? and ?";
        return dp.update(sql, DateUtil.WEEK_MILLIS, user.getId(), start, end);
    }
    
    public static TMResult findByUserSearchCats(Long userId, String itemCats, String sellerCats, String search, PageOffset po) {
        if (search == null) {
            search = "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(" from item%s where userid = ? and status = 1 and (cid <> 50023725 and cid <> 50023728) ");
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sb.append(" and " + like);
        }

        if(!StringUtils.isEmpty(itemCats) && !StringUtils.equals(itemCats, UserDelistPlan.AllCateIds)){
        	sb.append(" and (cid in (" +  itemCats + ")");
        }

        if (!StringUtils.isEmpty(sellerCats) && !StringUtils.equals(sellerCats, UserDelistPlan.AllCateIds)) {
            Set<Long> idSet = new HashSet<Long>();

            String[] idArray = sellerCats.split(",");
            if (idArray != null && idArray.length > 0) {
                for (String str : idArray) {
                    long id = NumberUtil.parserLong(str, 0L);
                    if (id <= 0) {
                        continue;
                    }
                    idSet.add(id);
                }
//                if(!StringUtils.isEmpty(itemCats)) {
//                    sb.append(" or ");
//                }else{
//                    sb.append(" and ");
//                }
                sb.append(" and ");
                sb.append(appendSellerCidsLike(StringUtils.join(idSet, " ")));
            }


        }
        if(!StringUtils.isEmpty(itemCats) && !StringUtils.equals(itemCats, UserDelistPlan.AllCateIds)) {
            sb.append(")");
        }
        log.info("-----------------------------------------------------------------------------select " + ITEM_SQL + sb.toString()
                + " limit ? offset ?" + userId);
        List<ItemPlay> list = new JDBCExecutor<List<ItemPlay>>(dp, genShardQuery("select " + ITEM_SQL + sb.toString()
                + " limit ? offset ?", userId), userId, po.getPs(), po.getOffset()) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();

        int count = (int) dp.singleLongQuery(genShardQuery("select count(*) " + sb.toString(), userId), userId);

        return new TMResult(list, count, po);
    }

    public static class ItemPlaySign{
        private ItemPlay item;
        private boolean isAutoList;
        public ItemPlaySign(ItemPlay item, boolean isAutoList){
            this.item = item;
            this.isAutoList = isAutoList;
        }

        public ItemPlay getItem(){
            return this.item;
        }
        public void setItem(ItemPlay item){
            this.item = item;
        }
        public boolean getIsAutoList(){
            return this.isAutoList;
        }
        public void setAutoList(boolean isAutoList){
            this.isAutoList = isAutoList;
        }
    }

    public static TMResult findByUserAndSearchWithExcludedItems(Long userId, String search, PageOffset po,
                                                                    Collection<Long> excludedIds, String cid, String sellerCid) {
        if (search == null) {
            search = "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(" from item%s where userid = ? and status = 1 ");    //这里的status=1指的是正在出售中的宝贝
        if (!StringUtils.isEmpty(search)) {
            String like = appendTitleLike(search);
            sb.append(" and " + like);
        }
        if (!StringUtils.isEmpty(cid)) {
            String like = appendItemCidsLike(cid);
            sb.append(" and " + like);
        }
        if (!StringUtils.isEmpty(sellerCid)) {
            String like = appendSellerCidsLike(sellerCid);
            sb.append(" and " + like);
        }

        List<ItemPlay> list = new JDBCExecutor<List<ItemPlay>>(dp, genShardQuery("select " + ITEM_SQL + sb.toString()
                + " limit ? offset ?", userId), userId, po.getPs(), po.getOffset()) {

            @Override
            public List<ItemPlay> doWithResultSet(ResultSet rs) throws SQLException {
                List<ItemPlay> itemList = new ArrayList<ItemPlay>();
                while (rs.next()) {
                    itemList.add(parseItem(rs));
                }
                return itemList;
            }
        }.call();
        List<ItemPlaySign> itemPlaySigns = new ArrayList<ItemPlaySign>();
        for(ItemPlay item : list){
            if(excludedIds.contains(item.getNumIid())){
                itemPlaySigns.add(new ItemPlaySign(item, false));
            }else{
                itemPlaySigns.add(new ItemPlaySign(item, true));
            }
        }
        int count = (int) dp.singleLongQuery(genShardQuery("select count(*) " + sb.toString(), userId), userId);
        return new TMResult(itemPlaySigns, count, po);
    }

}
