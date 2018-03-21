
package dao.popularized;

import static java.lang.String.format;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import job.click.HourlyCheckerJob;
import job.click.ItemNum;
import models.popularized.Popularized;
import models.popularized.Popularized.PopularizedStatus;
import models.user.User;

import org.elasticsearch.common.util.concurrent.jsr166y.ConcurrentLinkedDeque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.DBBuilder;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import actions.shopping.RandomShareAction;
import codegen.CodeGenerator.DBDispatcher;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;

import configs.Subscribe.Version;
import configs.TMConfigs;
import controllers.APIConfig;

public class PopularizedDao {
    private static final Logger log = LoggerFactory.getLogger(PopularizedDao.class);

    public static final String TAG = "PopularizedDao";

    public static Long count = 0l;

    public static int perFetchNum = 40;

    static DBDispatcher dp = Popularized.dp;

    public static Set<Long> findNumIidsByUserId(Long userId) {
        String sql = "select numIid from popularized where userId = ? ";
        sql = genShardQuery(sql, userId);

        return new JDBCExecutor<Set<Long>>(dp, sql, userId) {

            @Override
            public Set<Long> doWithResultSet(ResultSet rs) throws SQLException {
                Set<Long> numIids = new HashSet<Long>();
                while (rs.next()) {
                    numIids.add(rs.getLong(1));
                }
                return numIids;
            }
        }.call();
    }

    public static Set<Long> findNumIidsByUserIdWithStatus(Long userId, int status) {
        String sql = "select numIid from popularized where userId = ? and (cid <> 50023725 and cid <> 50023728) "
                + PopularizedStatusSqlUtil.getStatusRuleSql(status);
        sql = genShardQuery(sql, userId);

        return new JDBCExecutor<Set<Long>>(dp, sql, userId) {

            @Override
            public Set<Long> doWithResultSet(ResultSet rs) throws SQLException {
                Set<Long> numIids = new HashSet<Long>();
                while (rs.next()) {
                    numIids.add(rs.getLong(1));
                }
                return numIids;
            }
        }.call();
    }

    public static Queue<ItemNum> getItemNumsByUserId(Long userid) {

        String sql = "select userId,numIid from popularized where userId = ?";

        return new JDBCExecutor<Queue<ItemNum>>(true, dp, sql, userid) {

            @Override
            public Queue<ItemNum> doWithResultSet(ResultSet rs) throws SQLException {
                Queue<ItemNum> itemNums = new ConcurrentLinkedDeque<ItemNum>();
                while (rs.next()) {
                    itemNums.add(new ItemNum(rs.getLong(1), rs.getLong(2)));
                }
                return itemNums;
            }
        }.call();
    }

    public static Queue<ItemNum> getItemNums(int limit, int offset, int status) {

        log.info(format("getItemNums:limit, offset".replaceAll(", ", "=%s, ") + "=%s", limit, offset));

        String sql = "select userId,numIid from popularized "
                + PopularizedStatusSqlUtil.getStatusRuleSqlWithWhere(status) + " limit ? offset ?";

        return new JDBCExecutor<Queue<ItemNum>>(true, dp, sql, limit, offset) {

            @Override
            public Queue<ItemNum> doWithResultSet(ResultSet rs) throws SQLException {
                Map<Long, Integer> userIdNummIdCount = HourlyCheckerJob.userIdNummIdCount;
                if (userIdNummIdCount == null) {
                    userIdNummIdCount = new HashMap<Long, Integer>();
                }
                Queue<ItemNum> itemNums = new ConcurrentLinkedDeque<ItemNum>();
                /*Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                int day = calendar.get(Calendar.DATE);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int module = getModule(day);*/
                while (rs.next()) {
//                    if ((rs.getLong(2) + (int) (Math.floor((Math.random() * 13)))) % module == 0) {
//                        //int userVersion = rs.getInt(3);
//                        // test if we shoule reduce the click rate by userVersion
//                        // if true, to click; else no click
//                        //boolean abandon = isAbandon(userVersion);
//                        Long firstLoginTime = rs.getLong(3);
//                        boolean abandon = true;
//                        //Integer userCount = userIdNummIdCount.get(rs.getLong(1));
//                        //boolean abandon = checkCount(userCount);
//                        //abandon = abandon && loginTimeCheck(firstLoginTime);
//                        //abandon = abandon && hourCheck(hour);
//                        if (abandon) {
//                            itemNums.add(new ItemNum(rs.getLong(1), rs.getLong(2)));
//                        }
//                    }

                    /*if (TMConfigs.Referers.reduceornot) {
                        Integer userCount = userIdNummIdCount.get(rs.getLong(1));
                        boolean abandon = checkCount(userCount);
                        if (abandon) {
                            itemNums.add(new ItemNum(rs.getLong(1), rs.getLong(2)));
                        }
                    } else {
                        itemNums.add(new ItemNum(rs.getLong(1), rs.getLong(2)));
                    }*/
                	itemNums.add(new ItemNum(rs.getLong(1), rs.getLong(2)));
                }
                return itemNums;
            }
        }.call();
    }

    public static Queue<String> moreItemNums(int limit, int offset) {

        log.info(format("moreclickItemNums:limit, offset".replaceAll(", ", "=%s, ") + "=%s", limit, offset));

        String sql = "select nick from more_click_nick limit ? offset ?";

        return new JDBCExecutor<Queue<String>>(true, dp, sql, limit, offset) {

            @Override
            public Queue<String> doWithResultSet(ResultSet rs) throws SQLException {
                Queue<String> nicks = new ConcurrentLinkedDeque<String>();
                while (rs.next()) {
                    nicks.add(rs.getString(1));
                }
                return nicks;
            }
        }.call();
    }

    public static long countAll() {
        return JDBCBuilder.singleLongQuery(" select count(*) from popularized");
    }

    public static long countAllHot() {
        return JDBCBuilder.singleLongQuery(" select count(*) from popularized where status = 2");
    }
    
    public static Map<Long, Integer> countUser() {
        String sql = "select userId,count(*) from popularized group by userId";
        return new JDBCExecutor<Map<Long, Integer>>(dp, sql) {

            @Override
            public Map<Long, Integer> doWithResultSet(ResultSet rs) throws SQLException {
                Map<Long, Integer> userIdNummIdCount = new HashMap<Long, Integer>();
                while (rs.next()) {
                    userIdNummIdCount.put(rs.getLong(1), rs.getInt(2));
                }
                return userIdNummIdCount;
            }
        }.call();
    }

    public static boolean loginTimeCheck(Long firstLoginTime) {
        if (firstLoginTime == null) {
            return false;
        }
        Long interval = System.currentTimeMillis() - firstLoginTime;
        if (interval <= DateUtil.DAY_MILLIS * 20) {
            return true;
        } else {
            int random = (int) Math.floor(Math.random() * 100);
            if (random < 50) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static int[] distributes = {
            80, 90, 80, 60, 70, 70, 70, 60, 50, 60, 60, 70, 90, 100, 100, 90
    };

    public static boolean hourCheck(int hour) {
        if (hour < 8) {
            return false;
        }
        int random = (int) Math.floor(Math.random() * 100);
        int a = (int) (Math.random() * 2 + 1);
        int sign = (int) (Math.pow(-1, a));
        int distribute = distributes[hour - 8] + sign * (int) Math.floor((Math.random() * 20));
        if (random < distribute) {
            return true;
        } else {
            return false;
        }

    }

    public static int getModule(int day) {
        int offset = day % 9;
        int module = 1;
        switch (offset) {
            case 0:
                module = 1;
                break;
            case 1:
                module = 1;
                break;
            case 2:
                module = 1;
                break;
            case 3:
                module = 1;
                break;
            case 4:
                module = 1;
                break;
            case 6:
                module = 1;
                break;
            case 7:
                module = 1;
                break;
            case 8:
                module = 1;
                break;
            case 9:
                module = 1;
                break;
            default:
                module = 1;
                break;
        }
        return module;
    }

    public static boolean checkCount(Integer userCount) {
        Map<Integer, Integer> verCountMap = APIConfig.get().getTuiguangCountMap();
        if (userCount == null) {
            return true;
        }

        if (verCountMap.get(Version.FREE) != null && userCount <= verCountMap.get(Version.FREE)) {
            return true;
        } else if (verCountMap.get(Version.BASE) != null && userCount <= verCountMap.get(Version.BASE)) {
            int random = (int) Math.floor(Math.random() * 100);
            if (random < TMConfigs.Referers.baseReduce) {
                return true;
            } else {
                return false;
            }
        } else if (verCountMap.get(Version.VIP) != null && userCount <= verCountMap.get(Version.VIP)) {
            int random = (int) Math.floor(Math.random() * 100);
            if (random < TMConfigs.Referers.VIPReduce) {
                return true;
            } else {
                return false;
            }
        } else if (verCountMap.get(Version.SUPER) != null && userCount <= verCountMap.get(Version.SUPER)) {
            int random = (int) Math.floor(Math.random() * 100);
            if (random < TMConfigs.Referers.superReduce) {
                return true;
            } else {
                return false;
            }
        } else if (verCountMap.get(Version.HALL) != null && userCount <= verCountMap.get(Version.HALL)) {
            int random = (int) Math.floor(Math.random() * 100);
            if (random < TMConfigs.Referers.hallReduce) {
                return true;
            } else {
                return false;
            }
        } else if (verCountMap.get(Version.SUN) != null && userCount <= verCountMap.get(Version.SUN)) {
            int random = (int) Math.floor(Math.random() * 100);
            if (random < TMConfigs.Referers.godReduce) {
                return true;
            } else {
                return false;
            }
        } else {
            int random = (int) Math.floor(Math.random() * 100);
            if (random < TMConfigs.Referers.sunReduce) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static boolean isAbandon(int userVersion) {
        if (userVersion <= Version.FREE) {
            return true;
        } else if (userVersion <= Version.BASE) {
            int random = (int) Math.floor(Math.random() * 100);
            if (random < TMConfigs.Referers.baseReduce) {
                return true;
            } else {
                return false;
            }
        } else if (userVersion <= Version.VIP) {
            int random = (int) Math.floor(Math.random() * 100);
            if (random < TMConfigs.Referers.VIPReduce) {
                return true;
            } else {
                return false;
            }
        } else if (userVersion <= Version.SUPER) {
            int random = (int) Math.floor(Math.random() * 100);
            if (random < TMConfigs.Referers.superReduce) {
                return true;
            } else {
                return false;
            }
        } else if (userVersion <= Version.HALL) {
            int random = (int) Math.floor(Math.random() * 100);
            if (random < TMConfigs.Referers.hallReduce) {
                return true;
            } else {
                return false;
            }
        } else if (userVersion <= Version.GOD) {
            int random = (int) Math.floor(Math.random() * 100);
            if (random < TMConfigs.Referers.godReduce) {
                return true;
            } else {
                return false;
            }
        } else {
            int random = (int) Math.floor(Math.random() * 100);
            if (random < TMConfigs.Referers.sunReduce) {
                return true;
            } else {
                return false;
            }
        }
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

    public static List<Popularized> getRandomPopularizedItems(User user) {
        List<Popularized> items = new ArrayList<Popularized>();
        List<Popularized> thisUser = new ArrayList<Popularized>();
        if (user != null) {
            //thisUser = Popularized.find("userId = ?", user.getId()).fetch();
            String sql = SelectPopularizedSql + " userId = ? ";
            thisUser = queryListByJDBC(sql, user.getId());
        }

        items = getItemsOverCount(perFetchNum);
        if (items.size() < perFetchNum) {
            count = 0l;
            //ywj add, fulfill items
            log.error("the items size: " + items.size() + ", small than " + perFetchNum + ", so fulfill items");
            List<Popularized> tempList = getItemsOverCount(perFetchNum - items.size());
            for (Popularized temp : tempList) {
                if (!items.contains(temp))
                    items.add(temp);
            }
        }

        if (!CommonUtils.isEmpty(thisUser)) {
            for (Popularized item : thisUser) {
                if (!items.contains(item)) {
                    items.add(item);
                }
            }
        }

        items = randomItems(items);
        return items;
    }

    public static List<Popularized> randomItems(User user, int num) {
        if (num <= 0) {
            return null;
        }
        List<Popularized> items = new ArrayList<Popularized>();
        List<Popularized> thisUser = new ArrayList<Popularized>();
        if (user != null) {
            //thisUser = Popularized.find("userId = ?", user.getId()).fetch();
            String sql = SelectPopularizedSql + " userId = ? ";
            thisUser = queryListByJDBC(sql, user.getId());
        }

        items = getItemsOverCount(num);
        if (items.size() < num) {
            count = 0l;
        }

        if (!CommonUtils.isEmpty(thisUser)) {
            for (Popularized item : thisUser) {
                if (!items.contains(item)) {
                    items.add(item);
                }
            }
        }

        items = randomItems(items);
        return items;
    }

    public static List<Popularized> getItemsOverCount(int fetchNum) {
        List<Popularized> items = new ArrayList<Popularized>();
        //items = Popularized.find("id > ? ", count).fetch(fetchNum);
        String sql = SelectPopularizedSql + " id > ? limit ?";
        items = queryListByJDBC(sql, count, fetchNum);
        count += fetchNum;
        return items;
    }

    public static List<Popularized> getItems(Long start, int limit) {
        if (start == null) {
            start = 0l;
        }

        if (limit < 0) {
            limit = 0;
        }

        List<Popularized> items = new ArrayList<Popularized>();
        //items = Popularized.find("id > ? ", start).fetch(limit);
        String sql = SelectPopularizedSql + " id > ? limit ?";
        items = queryListByJDBC(sql, start, limit);
        return items;
    }
    
    public static List<Popularized> getRandomHotItems(int start, int limit) {
        if (start < 0) {
            start = 0;
        }

        if (limit < 0) {
            limit = 0;
        }

        List<Popularized> items = new ArrayList<Popularized>();
        String sql = SelectPopularizedSql + " status = 2 limit ?, ?";
        items = queryListByJDBC(sql, start, limit);
        return items;
    }

    public static List<Popularized> randomItems(List<Popularized> items) {
        return randomItems(items, RandomShareAction.EachReturnNum);
    }
    
    public static List<Popularized> randomItems(List<Popularized> items, int limitNum) {
        List<Popularized> randomedItems = new ArrayList<Popularized>();
        int index = 0;
        int length = items.size();
        while (randomedItems.size() < limitNum && randomedItems.size() != items.size()) {
            index = (int) (Math.random() * length);
            if (!randomedItems.contains(items.get(index))) {
                randomedItems.add(items.get(index));
            }
        }
        return randomedItems;
    }

    public static Popularized findByNumIid(Long userId, Long numIid) {
        String sql = SelectPopularizedSql + " userId = ? and numIid = ? ";

        return queryByJDBC(sql, userId, numIid);
    }

    public static class PopularizedStatusSqlUtil {

        //public static final boolean IsStatusFuncOn = false;
        public static boolean IsStatusFuncOn = false;

        //在ItemDao的searchPop中的left join条件
        public static String getSearchLeftJoinSql(int status) {
            if (status <= 0)
                return "";
            String sql = " and p.status & " + status + " > 0 ";
            return sql;
        }

        //
        public static String getStatusRuleSql(int status) {
            if (status <= 0)
                return "";
            String sql = " and status  & " + status + " > 0 ";
            return sql;
        }

        public static String getStatusRuleSqlWithWhere(int status) {
            if (status <= 0)
                return "";
            String sql = " where status  & " + status + " > 0 ";
            return sql;
        }

        public static int checkStatus(int status) {
            if (status <= 0)
                return PopularizedStatus.Normal;
            return status;
        }

    }

    public static long countPopularizedByUserIdAndStatus(Long userId, int status) {
        String sql = "select count(*) from " + Popularized.TABLE_NAME
                + " where userId = ? and (cid <> 50023725 and cid <> 50023728) "
                + PopularizedStatusSqlUtil.getStatusRuleSql(status);

        return dp.singleLongQuery(sql, userId);
    }

    public static List<Popularized> queryPopularizedsByUserId(Long userId) {
        if (userId == null || userId <= 0)
            return new ArrayList<Popularized>();
        String sql = SelectPopularizedSql + " userId = ? ";

        List<Popularized> itemList = queryListByJDBC(sql, userId);

        return itemList;
    }

    public static List<Popularized> queryPopularizedsByUserIdAndStatus(Long userId, int status) {
        if (userId == null || userId <= 0)
            return new ArrayList<Popularized>();
        String sql = SelectPopularizedSql + " userId = ? " + PopularizedStatusSqlUtil.getStatusRuleSql(status);

        List<Popularized> itemList = queryListByJDBC(sql, userId);

        return itemList;
    }

    public static boolean deletePopularize(Popularized popu) {
        String sql = "delete from " + Popularized.TABLE_NAME + " where userId=? and numIid = ?";

        long res = dp.insert(sql, popu.getUserId(), popu.getNumIid());

        if (res == 1) {
            return true;
        } else {
            log.error("delete failed...for Popularized: [numIid : ]" + popu.getNumIid());
            return false;
        }

    }

    public static boolean deletePopularizeById(Long userId, Long numIid) {
        String sql = "delete from " + Popularized.TABLE_NAME + " where userId=? and numIid = ?";

        long res = dp.insert(sql, userId, numIid);

        if (res == 1) {
            return true;
        } else {
            log.error("delete failed...for Popularized: [numIid : ]" + numIid);
            return false;
        }

    }

    private static final String UpdatePricePropertiesSql = " update "
            + Popularized.TABLE_NAME
            + " "
            + " set `title` = ?, `picPath` = ?, `price` = ?, `salesCount` = ?, `skuMinPrice` = ? where `id` = ? and numIid= ? ";

    /**
     * 在更新数据时，更新宝贝后，更新Popularized中标题、价格、图片等信息
     * @param item
     */
    public static boolean updatePopularizePrice(Popularized item) {
        if (item == null)
            return false;

        long res = dp.insert(UpdatePricePropertiesSql, item.getTitle(), item.getPicPath(), item.getPrice(),
                item.getSalesCount(), item.getSkuMinPrice(), item.getId(), item.getNumIid());

        if (res == 1) {
            return true;
        } else {
            log.error("update failed...for Popularized: [numIid : ]" + item.getNumIid());
            return false;
        }
    }

    /**
     * 使用JDBC查询
     * 
     */

    private static final String PopularizedProperties = " id, userId, numIid, userVersion, firstLoginTime, "
            + " picPath, price, title, salesCount, cid, firstCid, bigCatName, skuMinPrice, status ";

    public static final String SelectPopularizedSql = " select " + PopularizedProperties + " from "
            + Popularized.TABLE_NAME + " where ";

    private static Popularized parsePopularized(ResultSet rs) {
        try {
            Popularized item = new Popularized();
            item.setId(rs.getLong(1));
            item.setUserId(rs.getLong(2));
            item.setNumIid(rs.getLong(3));
            item.setUserVersion(rs.getInt(4));
            item.setFirstLoginTime(rs.getLong(5));

            item.setPicPath(rs.getString(6));
            item.setPrice(rs.getDouble(7));
            item.setTitle(rs.getString(8));

            item.setSalesCount(rs.getInt(9));
            item.setCid(rs.getLong(10));
            item.setFirstCid(rs.getLong(11));
            item.setBigCatName(rs.getString(12));
            item.setSkuMinPrice(rs.getDouble(13));

            item.setStatus(rs.getInt(14));

            return item;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    public static Popularized queryByJDBC(String sql, Object... prams) {
        return new JDBCExecutor<Popularized>(dp, sql, prams) {
            @Override
            public Popularized doWithResultSet(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    return parsePopularized(rs);
                }
                return null;
            }
        }.call();
    }

    protected static List<Popularized> queryListByJDBC(String sql, Object... prams) {
        return new JDBCExecutor<List<Popularized>>(dp, sql, prams) {
            @Override
            public List<Popularized> doWithResultSet(ResultSet rs) throws SQLException {
                List<Popularized> itemList = new ArrayList<Popularized>();
                while (rs.next()) {
                    Popularized item = parsePopularized(rs);
                    if (item != null)
                        itemList.add(item);
                }
                return itemList;
            }
        }.call();
    }

}
