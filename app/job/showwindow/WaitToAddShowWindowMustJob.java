
package job.showwindow;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;

import message.UserTimestampMsg;
import models.user.User;

import org.elasticsearch.common.util.concurrent.jsr166y.ConcurrentLinkedDeque;

import play.db.jpa.NoTransaction;
import play.jobs.Every;
import play.jobs.Job;
import titleDiag.DiagResult;
import configs.TMConfigs;
import dao.UserDao;

@Every("2min")
@NoTransaction
/**
 * 最近页面上有操作的
 * @author zrb
 *
 */
public class WaitToAddShowWindowMustJob extends Job {

    static Queue<UserTimestampMsg> waitUserIds = new ConcurrentLinkedDeque<UserTimestampMsg>();

    static long MIN_START_INTERVAL = 25 * 1000L;

    public void doJob() {

        UserTimestampMsg msg = null;
        long now = System.currentTimeMillis();
        Set<User> todoUsers = new HashSet<User>();

        while ((msg = waitUserIds.poll()) != null) {
            long ts = msg.getTs();
            if (now - ts < MIN_START_INTERVAL) {
                addUserMsg(msg);
                continue;
            }
            long userId = msg.getUserId();
            final User user = UserDao.findById(userId);
            if (user == null || !user.isVaild() || !user.isShowWindowOn()) {
                continue;
            }

        }

        for (final User user : todoUsers) {
            TMConfigs.getDiagResultPool().submit(new Callable<DiagResult>() {
                @Override
                public DiagResult call() throws Exception {
                    new ShowWindowExecutor(user).doJob();
                    return null;
                }
            });
        }
    }

    public static void addUser(Long userId) {
        addUserMsg(new UserTimestampMsg(userId, System.currentTimeMillis()));
    }

    private static void addUserMsg(UserTimestampMsg msg) {
        waitUserIds.add(msg);
    }
}
