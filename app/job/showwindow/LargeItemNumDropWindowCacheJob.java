
package job.showwindow;

import java.util.List;
import java.util.concurrent.Callable;

import jdp.ApiJdpAdapter;
import models.showwindow.DropWindowTodayCache;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.jobs.Every;
import play.jobs.Job;

import com.taobao.api.domain.Trade;

import configs.TMConfigs;
import configs.TMConfigs.Server;
import dao.UserDao;
import dao.UserDao.UserBatchOper;

@Every("6h")
//@OnApplicationStart(async = true)
public class LargeItemNumDropWindowCacheJob extends Job {

    static Long[] targetUserids = new Long[] {
            614305686L
    };

    private static final Logger log = LoggerFactory.getLogger(LargeItemNumDropWindowCacheJob.class);

    public static final String TAG = "LargeItemNumDropWindowCacheJob";

    public void doJob() {

        if (!Server.jobTimerEnable) {
            return;
        }

        new UserBatchOper(128) {
            public List<User> findNext() {
                return UserDao.findWindowShowOn(offset, limit);
            }

            @Override
            public void doForEachUser(final User user) {
                TMConfigs.getTradePool().submit(new Callable<List<Trade>>() {
                    @Override
                    public List<Trade> call() throws Exception {
                        doForUser(user);
                        return null;
                    }
                });

            }
        }.call();
    }

    static String USER_ITEM_NUM_TAG = "larget_onsale_item_num_";

    static int LARGE_ITEM_NUM_THREADHOLD = 450;

    private void doForUser(User user) {
        String key = USER_ITEM_NUM_TAG + user.getId();
        Integer num = (Integer) Cache.get(key);
        if (num == null) {
            Long onSaleNum = ApiJdpAdapter.get(user).onSaleItemNum(user);
            if (onSaleNum == null) {
                log.warn("no on sale num for user:" + user);
                return;
            }
            num = new Integer(onSaleNum.intValue());
            Cache.set(key, num, "7d");
        }

        if (num.intValue() < LARGE_ITEM_NUM_THREADHOLD) {
            return;
        }
        int addedNum = DropWindowTodayCache.addCacheForUser(user, LARGE_ITEM_NUM_THREADHOLD);
        log.info("add cache for large  item num user:" + user + " with item num :" + num + " for  added num:"
                + addedNum);
    }

}
