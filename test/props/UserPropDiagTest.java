
package props;

import java.util.List;

import job.diagjob.PropDiagJob;
import job.diagjob.PropDiagJob.ItemPropDiagWrapper;
import models.user.User;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.test.UnitTest;
import dao.UserDao;

public class UserPropDiagTest extends UnitTest {

    private static final Logger log = LoggerFactory.getLogger(UserPropDiagTest.class);

    public static final String TAG = "UserPropDiagTest";

    String nick = "兔子也craze";

    Long numIid = 14498913937L;

    @Test
    public void testProp() {
        User user = UserDao.findByUserNick(nick);
//        ItemPlay item = ItemDao.findByNumIid(user.getId(), numIid);

        long start = System.currentTimeMillis();
        List<ItemPropDiagWrapper> res = new PropDiagJob(user).doJobWithResult();
        log.info("[res : ]" + res);
        long end = System.currentTimeMillis();
        log.warn("took " + (end - start) + " ms");
    }

}
