
package recommend;

import java.util.concurrent.Callable;

import job.showwindow.CheckNoDownShelfJob;
import job.showwindow.ShowWindowTimerExecJob.ShowWindowUserBatcher;
import job.showwindow.WindowRemoteJob;
import models.user.User;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.test.UnitTest;
import dao.UserDao;

public class DeleteCheckerTest extends UnitTest {

    private static final Logger log = LoggerFactory.getLogger(DeleteCheckerTest.class);

    public static final String TAG = "DeleteCheckerTest";

    @Test
    public void testBatch() {
        new ShowWindowUserBatcher() {

            @Override
            public void doForEachUser(final User user) {
                WindowRemoteJob.getPool().submit(new Callable<Boolean>() {

                    @Override
                    public Boolean call() throws Exception {

                        log.info("[user ]" + user);
                        new CheckNoDownShelfJob(user).call();
                        return Boolean.TRUE;
                    }

                });

            }

        }.call();
    }

    public void testCheck() {
        Long userId = 914949421L;

        User user = UserDao.findById(userId);
        new CheckNoDownShelfJob(user).call();
    }
}
