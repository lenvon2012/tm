
package job.showwindow;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;

import com.ciaosir.client.PYFutureTaskPool;

import configs.TMConfigs.Server;
import dao.UserDao;
import dao.UserDao.UserBatchOper;

//@Every("600min")
public class ShowWindowMustEnsuerJob extends Job {

    final static PYFutureTaskPool<Boolean> pool = new PYFutureTaskPool<Boolean>(8);

    private static final Logger log = LoggerFactory.getLogger(ShowWindowMustEnsuerJob.class);

    public static final String TAG = "ShowWindowMustEnsuerJob";

    public void doJob() {
        if (!Server.jobTimerEnable) {
            return;
        }

        Calendar instance = Calendar.getInstance();
        int hour = instance.get(Calendar.HOUR_OF_DAY);
        if (hour >= 0 && hour < 8) {
            return;
        }

//        List<WindowMustEnsueId> list = WindowMustEnsueId.findAll();
//        for (WindowMustEnsueId windowMustEnsueId : list) {
//            final User user = UserDao.findById(windowMustEnsueId.getId());
//            WindowsQueueJob.callers.submit(new Callable<ItemPlay>() {
//                @Override
//                public ItemPlay call() throws Exception {
//                    if (user.isShowwindowOn()) {
//                        new ShowWindowExecutor(user).doJob();
//                    }
//                    return null;
//                }
//            });
//        }

        new UserBatchOper(16) {
            public List<User> findNext() {
                return UserDao.findWindowShowOn(offset, limit);
            }

            @Override
            public void doForEachUser(final User user) {
                pool.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {

                        if (!user.isShowWindowOn()) {
                            return null;
                        }
                        long start = System.currentTimeMillis();
                        new ShowWindowExecutor(user).doJob();
                        long end = System.currentTimeMillis();
                        log.error(" user " + user + "  take :" + (end - start) + " ms ");

                        return null;
                    }
                });

            }
        }.call();

    }
}
