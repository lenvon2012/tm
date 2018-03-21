
package models.showwindow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.persistence.Entity;
import javax.persistence.Id;

import jdp.ApiJdpAdapter;
import job.ApplicationStopJob;
import job.SPWorker;
import job.showwindow.ShowWindowExecutor.CacheControl;
import job.showwindow.WindowReplaceJob;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import play.jobs.Job;
import service.WindowsService;
import transaction.JDBCBuilder;
import transaction.JDBCBuilder.JDBCExecutor;
import bustbapi.ShowWindowApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.utils.DateUtil;
import com.taobao.api.domain.Item;

import dao.UserDao;

@Entity(name = DropWindowTodayCache.TABLE_NAME)
public class DropWindowTodayCache extends GenericModel {

    private static final Logger log = LoggerFactory.getLogger(DropWindowTodayCache.class);

    public static final String TAG = "DropWindowTodayCache";

    public static final String TABLE_NAME = "drop_window_today_cache";

    @Index(name = "delistTime")
    Long delistTime;

    Long userId;

    @Id
    Long numIid;

//    Long candidateId = 0L;

    public boolean rawInsert() {
        long id = JDBCBuilder.insert("insert into `" + TABLE_NAME
                + "`(`numIid`,`userId`,`delistTime`) values(?,?,?)",
                this.numIid, this.userId, this.delistTime);

        if (id > 0L) {
            return true;
        } else {
            log.error("Insert Fails....." + "[userId : ]" + this.userId + "[numIid : ]" + this.numIid);
            return false;
        }
    }

    public boolean rawUpdate() {
        long updateNum = JDBCBuilder.update(false, "update `" + TABLE_NAME
                + "` set   `delistTime` = ? where `numIid` = ? ", this.delistTime, this.numIid);

        if (updateNum == 1) {
            return true;
        } else {
            log.error("update failed...for [userId : ]" + this.userId + "[numIid : ]" + this.numIid);
            return false;
        }
    }

    static class ListFetcher extends JDBCExecutor<List<DropWindowTodayCache>> {
        public ListFetcher(String whereQuery) {
            super(true, StringUtils.EMPTY);
            StringBuilder sb = new StringBuilder();
            sb.append("select delistTime,userId,numIid from ");
            sb.append(TABLE_NAME);
            sb.append(" where  1 =1 ");
            if (!StringUtils.isBlank(whereQuery)) {
                sb.append(" and ");
                sb.append(whereQuery);
            }

            this.query = sb.toString();
            this.debug = false;
        }

        @Override
        public List<DropWindowTodayCache> doWithResultSet(ResultSet rs) throws SQLException {
            List<DropWindowTodayCache> list = new ArrayList<DropWindowTodayCache>();
            while (rs.next()) {
                DropWindowTodayCache model = new DropWindowTodayCache(rs.getLong(1), rs.getLong(2), rs.getLong(3));
                list.add(model);
            }
            return list;
        }
    }

    static PYFutureTaskPool<Boolean> pool = null;

    static PYFutureTaskPool<Boolean> getCancelPool() {
        if (pool != null) {
            return pool;
        }

        pool = new PYFutureTaskPool<Boolean>(386);
        ApplicationStopJob.addShutdownPool(pool);
        return pool;
    }

    public static int checkRecent() {

        int affectedNum = 0;
        long curr = System.currentTimeMillis() - DateUtil.THREE_SECONDS_MILLIS;
        long before = curr - (90 * DateUtil.ONE_MINUTE_MILLIS);

/*        List<DropWindowTodayCache> recentDown = DropWindowTodayCache.find("delistTime >= ? and delistTime <= ?",
                before, curr).fetch();*/

        log.info(" drop window cache start :" + DateUtil.formDateForLog(before) + " with end :"
                + DateUtil.formDateForLog(curr));

        List<DropWindowTodayCache> recentDown = new ListFetcher("delistTime >= " + before + " and delistTime <= "
                + curr + " order by delistTime desc  limit 1024").call();

        log.info("[no drop window cahce num:]:" + recentDown.size());
        if (CommonUtils.isEmpty(recentDown)) {
            return 0;
        }

        affectedNum = recentDown.size();

        JDBCBuilder.update(false, "delete from " + TABLE_NAME + " where delistTime >= ? and delistTime <= ?", before,
                curr);

        log.error(">>>>>>find need to delist count :" + recentDown.size());
        for (final DropWindowTodayCache cache : recentDown) {

            final User user = UserDao.findById(cache.userId);

            if (user == null) {
                continue;
            }

            if (!user.isVaild()) {
                continue;
            }

            if (!user.isShowWindowOn()) {
                continue;
            }

            final Long numIid = cache.numIid;
            final DropWindowTodayCache other = cache;

//            getCancelPool().submit(new ReplaceCaller(user, other));
            SPWorker.addDropWindow(cache);

        }

        return affectedNum;
    }

    public static class ReplaceCaller implements Callable<Boolean> {
        User user;

        DropWindowTodayCache other;

        public ReplaceCaller(User user, DropWindowTodayCache other) {
            super();
            this.user = user;
            this.other = other;
        }

        @Override
        public Boolean call() throws Exception {
            if (user == null || !user.isShowWindowOn()) {
                return Boolean.FALSE;
            }
            Long userId = user.getId();

            int retry = 10;

            while (CacheControl.isDoing(userId) && retry-- > 0) {
                CommonUtils.sleepQuietly(2000L);
            }
            CacheControl.setUserUpdate(userId);
            try {
                new WindowReplaceJob(user, other).call();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);

            }
            CacheControl.free(userId);
            if (retry <= 0) {
                log.warn(" more than 10 retries :" + userId);
                WindowsService.addQueue(user.getId());
            }
            return Boolean.TRUE;
        }

    }

    public static class CheckRecentToDropCacheJob extends Job {
        public void doJob() {
            long before = System.currentTimeMillis();
            int totalCount = checkRecent();
            long after = System.currentTimeMillis();
            log.error(" check recent  [" + totalCount + "] down took :" + (after - before) + " ms");
        }

    }

//    @OnApplicationStart(async = true)
//    @Every("6h")
//    public static class DropCacheInitJob extends Job {
//    }

    public static int addCacheForUser(User user, int max) {
//        List<Item> list = new ItemsOnsaleShowcaseInit(user, 99, true).call();
        long curr = System.currentTimeMillis();
        long end = curr + 2 * DateUtil.DAY_MILLIS;
        List<Item> recentDownItems = ApiJdpAdapter.get(user).findRecentDownItems(user, max);

        if (CommonUtils.isEmpty(recentDownItems)) {
            return 0;
        }
//        log.warn("candidate list size:" + list.size() + " for user:" + user);

//        log.info("curr ;" + new Date(curr));
//        log.info("end ;" + new Date(end));

        int count = 0;

        Set<Long> numIids = ShowWindowApi.toNumIids(recentDownItems);
        String sql = " numIid in (" + StringUtils.join(numIids, ',') + ")";
        Map<Long, DropWindowTodayCache> cachedMap = new HashMap<Long, DropWindowTodayCache>();
        List<DropWindowTodayCache> cahcedItems = new ListFetcher(sql).call();
        for (DropWindowTodayCache model : cahcedItems) {
            cachedMap.put(model.numIid, model);
        }

        count = batchWriteCache(user, curr, end, recentDownItems, cachedMap);
        return count;
    }

    private static int batchWriteCache(User user, long curr, long end, List<Item> list,
            Map<Long, DropWindowTodayCache> cachedMap) {
        int count = 0;
        //        log.info("[cache size:]" + cachedMap.size());
        Set<Long> toRemoveIds = new HashSet<Long>();

        List<Item> toInsertItem = new ArrayList<Item>();
        /**
         * TODO we need the JDBC way to rewrite the way..
         */
        for (Item item : list) {
            count++;

            long delistTime = item.getDelistTime().getTime();
            //            log.info("[delist time:]" + DateUtil.formDateForLog(delistTime));
            if (delistTime < curr || delistTime > end) {
                continue;
            }
            DropWindowTodayCache cache = cachedMap.get(item.getNumIid());
            //            log.info("[cache :]" + cache + " with item:" + item);
            if (cache == null) {
                toInsertItem.add(item);
                continue;
            }

            if (cache.getDelistTime().longValue() == item.getDelistTime().getTime()) {
                continue;
            }

            toRemoveIds.add(item.getNumIid());
            toInsertItem.add(item);
        }

        if (!CommonUtils.isEmpty(toRemoveIds)) {
            String whereSql = " where  numIid in (" + StringUtils.join(toRemoveIds, ',') + ")";
            JDBCBuilder.update(false, "delete from " + TABLE_NAME + " " + whereSql);
        }

        log.error("[to insert item size:]" + toInsertItem.size());
        for (Item item : toInsertItem) {
            new DropWindowTodayCache(item.getDelistTime().getTime(), user.getId(), item.getNumIid()).rawInsert();
        }

        //        for (Item item : list) {
        //            long delistTime = item.getDelistTime().getTime();
        //            if (delistTime > curr && delistTime < end) {
        //                DropWindowTodayCache cache = NumberUtil.first(new ListFetcher(" numIid = " + item.getNumIid())
        //                        .call());
        //
        //                count++;
        //                if (cache != null) {
        //                    JDBCBuilder.update(false, "delete from " + TABLE_NAME + " where numIid = ?", item.getNumIid());
        //                }
        //
        //      
        //            }
        //        }
        log.error(" save count :" + count + " ffor user :" + user);
        return count;
    }

//
//    public DropWindowTodayCache(Long delistTime, Long userId, Long numIid) {
//        super();
//        this.delistTime = delistTime;
//        this.userId = userId;
//        this.numIid = numIid;
//    }

    public static List<DropWindowTodayCache> userTodayCache(Long userId) {
        return new ListFetcher(" userId =  " + userId + " order by delistTime asc ").call();
    }

    public DropWindowTodayCache(Long delistTime, Long userId, Long numIid) {
        super();
        this.delistTime = delistTime;
        this.userId = userId;
        this.numIid = numIid;
    }

    @Override
    public String toString() {
        return "DropWindowTodayCache [delistTime=" + DateUtil.formDateForLog(delistTime) + ", userId=" + userId
                + ", numIid=" + numIid + "]";
    }

    public Long getDelistTime() {
        return delistTime;
    }

    public void setDelistTime(Long delistTime) {
        this.delistTime = delistTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getNumIid() {
        return numIid;
    }

    public void setNumIid(Long numIid) {
        this.numIid = numIid;
    }

    public static void updateDeslistTime(Long numIid2, Long itemDeListTime) {
        if (itemDeListTime == null || itemDeListTime.longValue() < 0L) {
            JDBCBuilder.update(false, "delete from " + TABLE_NAME + " where numIid = ?", numIid2);
        } else {
            JDBCBuilder.update(false, "update `" + TABLE_NAME + "` set  `delistTime` = ? where `numIid` = ? ",
                    itemDeListTime, numIid2);
        }

    }

}
