
package ump;

import java.util.Date;

import models.ump.UMPTool;
import models.user.User;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.test.UnitTest;
import tbapi.ump.UMPApi.CreateDiscountActivityApi;
import tbapi.ump.UMPApi.ListActivitiesApi;

import com.ciaosir.client.utils.DateUtil;

import dao.UserDao;

public class UMPApiTest extends UnitTest {

    private static final Logger log = LoggerFactory.getLogger(UMPApiTest.class);

    public static final String TAG = "UMPApiTest";

    User user = UserDao.findByUserNick("clorest510");

    @Test
    public void testAddActivity() {
        UserDao.refreshToken(user);
        Date startDate = new Date();
        Date end = new Date(startDate.getTime() + DateUtil.TWO_DAY_MILLIS);
        String name = "打折咯";
        String tag = "";
        Long actId = new CreateDiscountActivityApi(user, UMPTool.getBase().getId(), name, startDate, end, "打着咯", tag)
                .call();

        log.error(" activity id :" + actId);
    }

    public void testWord() {

        String res = new ListActivitiesApi(user, UMPTool.getBase().getId()).call();
        System.out.println(res);

    }
}
