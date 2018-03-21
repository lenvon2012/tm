
package fengxiao;

import java.util.ArrayList;
import java.util.List;

import models.item.ItemPlay;
import models.mysql.fengxiao.FenxiaoProvider;
import models.user.User;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.test.UnitTest;
import bustbapi.FenxiaoApi.FXScItemMapApi;
import bustbapi.FenxiaoApi.FXScItemMapQueryApi;
import bustbapi.FenxiaoApi.FXScItemOutCodeGet;
import bustbapi.FenxiaoApi.FXUserGet;
import bustbapi.ItemApi;

import com.google.gson.Gson;
import com.taobao.api.domain.Item;

import dao.UserDao;
import dao.item.ItemDao;

public class FengXiaoApiTest extends UnitTest {

    private static final Logger log = LoggerFactory.getLogger(FengXiaoApiTest.class);

    public static final String TAG = "FengXiaoApiTest";

    String nick = "宾利工装";

    User user = UserDao.findByUserNick(nick);

    @Test
    public void testBuild() {
        User user = UserDao.findById(162152082L);
        List<Long> ids = new ArrayList<Long>();
        ids.add(17345589686L);
        new FXScItemMapApi(user, ids).call();
    }

    public void testQuery() {
        FenxiaoProvider.ensure("樱松旗舰店");
    }

    public void testSCQuery() {
        List<ItemPlay> list = ItemDao.findByUserId(user.getId());
        for (ItemPlay itemPlay : list) {
            Integer res = new FXScItemMapQueryApi(user, itemPlay.getNumIid()).call();
        }

    }

    public void testSCItem() {
        List<ItemPlay> list = ItemDao.findByUserId(user.getId());
        for (ItemPlay itemPlay : list) {

            Item item = new ItemApi.ItemGet(user, itemPlay.getNumIid()).call();
            System.out.println("item gson :" + new Gson().toJson(item));
            String outerCode = item.getOuterId();
            System.out.println("code :" + outerCode);
            Integer res = new FXScItemOutCodeGet(user, outerCode).call();
            System.out.println(res);

//            Integer i = new FXItemGet(user, itemPlay.getNumIid()).call();
            return;
        }
    }

//    @Test
    public void testFetch() {
//        User user = UserDao.findByUserNick("蓝叶");

        List<User> list = UserDao.findValidList(0, 256);
        int count = 0;
        for (User user : list) {
            System.out.println("[nick]" + user.getUserNick());
            Integer res = new FXUserGet(user).call();
            System.out.println(res);
            if (res > 0) {
                count++;
            }
        }
        System.out.println("fenxiao count :" + count);
    }

}
