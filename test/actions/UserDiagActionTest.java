
package actions;

import models.UserDiag;
import models.user.User;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.test.UnitTest;
import bustbapi.TMTradeApi;

import com.ciaosir.client.utils.JsonUtil;

import dao.UserDao;

public class UserDiagActionTest extends UnitTest {

    private static final Logger log = LoggerFactory.getLogger(UserDiagActionTest.class);

    public static final String TAG = "UserDiagActionTest";

    Long[] ids = new Long[] {
            13985219L, 87878329L, 83753542L, 81643977L
//        13985219L
    };

    @Test
    public void testPHone() {
        User user = UserDao.findByUserNick("荒草残石");
        String phone = TMTradeApi.getSellerMobile(user.getSessionKey());
        log.error(phone);
    }

    public void testDetailDiag() {
        for (Long id : ids) {
            UserDiag diag = new UserDiag(id);
            diag.jdbcSave();

            diag.buildReport(null, null);
            log.warn(JsonUtil.getJson(diag));
        }
    }
}
