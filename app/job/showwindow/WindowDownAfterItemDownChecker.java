
package job.showwindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;

import jdp.ApiJdpAdapter;
import message.UserItemTimestampMsg;
import models.showwindow.ShowwindowMustDoItem;
import models.user.User;

import org.elasticsearch.common.util.concurrent.jsr166y.ConcurrentLinkedDeque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import cache.ItemDownShelfCache;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.taobao.api.domain.Item;

import dao.UserDao;

//@Every("30s")
public class WindowDownAfterItemDownChecker extends Job {

    private static final Logger log = LoggerFactory.getLogger(WindowDownAfterItemDownChecker.class);

    public static final String TAG = "WindowDownAfterItemDownChecker";

    static int maxCheckNum = 2;

    public static Queue<UserItemTimestampMsg> queue = new ConcurrentLinkedDeque<UserItemTimestampMsg>();

    @Override
    public void doJob() {
        UserItemTimestampMsg tempMsg = null;
        List<UserItemTimestampMsg> msgs = new ArrayList<UserItemTimestampMsg>();
        while ((tempMsg = queue.poll()) != null) {
//            log.info("[get msg :]" + tempMsg);
            msgs.add(tempMsg);
        }

        long now = System.currentTimeMillis();
        for (UserItemTimestampMsg msg : msgs) {

            if (now - msg.getTs() < DateUtil.TEN_SECONDS_MILLIS) {
                queue.add(msg);
                continue;
            }

            User user = UserDao.findById(msg.getUserId());
            if (user == null) {
                continue;
            }
            if (!user.isShowWindowOn()) {
                continue;
            }

            if (ItemDownShelfCache.isRecentDownShelf(msg.getNumIid())) {
                log.warn(" msg is recent down shelf...." + msg);
                continue;
            }
//            log.info("[check item msg :]" + msg);

            WindowRemoteJob.getPool().submit(new DropShelfItemCaller(user, msg));
        }
    }

    public static class DropShelfItemCaller implements Callable<Boolean> {

        User user;

        UserItemTimestampMsg msg = null;

        public DropShelfItemCaller(User user, UserItemTimestampMsg msg) {
            super();
            this.user = user;
            this.msg = msg;
        }

        @Override
        public Boolean call() throws Exception {
            List<Item> call = ApiJdpAdapter.get(user).OnWindowItemsDelistDesc(user, maxCheckNum);
            if (CommonUtils.isEmpty(call)) {
                log.warn(" empty items :" + user);
                return Boolean.FALSE;
            }
            for (Item item : call) {
                if (item.getNumIid().longValue() == msg.getNumIid()) {
                    log.warn("found on window after delist item :" + msg + " let's fuck it down....");

                    if (ShowwindowMustDoItem.exist(user.getId(), item.getNumIid())) {
                        log.warn("it's in the must ids....");
                        return Boolean.FALSE;
                    }

                    if (CheckNoDownShelfJob.isTradesRankContains(user, item.getNumIid())) {
                        log.warn("it's in the trade rank contains....");
                        return Boolean.FALSE;
                    }

                    CheckNoDownShelfJob.cancel(user, msg.getNumIid());
                }
            }

            return Boolean.TRUE;
        }

    }

    public static void addQueue(Long userId, Long numIid) {
        queue.add(new UserItemTimestampMsg(userId, System.currentTimeMillis(), numIid));
    }
}
