
package actions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import models.item.ItemPlay;
import models.task.AutoTitleTask;
import models.task.AutoTitleTask.UserTaskStatus;
import models.task.AutoTitleTask.UserTaskType;
import models.task.AutoTitleTask.WireLessDetailConfig;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.test.UnitTest;
import utils.PlayUtil;
import actions.wireless.WirelessItemWorker;
import bustbapi.ItemApi;

import com.ciaosir.client.utils.JsonUtil;
import com.taobao.api.domain.Item;

import dao.UserDao;
import dao.item.ItemDao;

public class WirelessActionTest extends UnitTest {

    private static final Logger log = LoggerFactory.getLogger(WirelessActionTest.class);

    public static final String TAG = "WirelessActionTest";

//    String nick = "楚之小南";
    String nick = "淘大宝联盟";

    User user = UserDao.findByUserNick(nick);

    @Test
    public void testLoad() {
        File input = new File("/home/zrb/code/tm/public/tmpl/input");
        try {
            File output = WirelessItemWorker.turnGifToJpg(user, input);
            log.warn(" output file :" + output);
        } catch (IOException e) {
            log.warn(e.getMessage(), e);

        }
    }

    public void testPictureId() {
        ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), 37464776721L);
        Item item = new ItemApi.ItemFullGet(user, itemPlay.getNumIid(), ItemApi.WIRELESS_FIELDS).call();
        log.info("[new json :]" + PlayUtil.genPrettyGson().toJson(item));
    }

    public void testId() {

//        List<ItemPlay> list = ItemDao.findByUserId(user.getId(), 3);
        ItemPlay raw = ItemDao.findByNumIid(user.getId(), 20373552244L);
        List<ItemPlay> list = new ArrayList<ItemPlay>();
        list.add(raw);
        for (ItemPlay itemPlay : list) {
//            new ItemApi.ItemFullGet(user, itemPlay.getNumIid(), "num_iid,newprepay,new_prepay").call();
            Item item = new ItemApi.ItemFullGet(user, itemPlay.getNumIid(), ItemApi.WIRELESS_FIELDS).call();
            log.info("[new json :]" + PlayUtil.genPrettyGson().toJson(item));
        }
    }

    //"1627207:3232483:彩色;21433:95751240:布（打孔加工）;21433:95751238:布2.8M定高（不加工）;21433:95751239:布（挂钩加工）;21433:95751245:帘头加工;21433:95751244:纱（打孔加工）;21433:95751243:纱（挂钩加工）;21433:95751242:纱2.8M定高（不加工）;21433:95751241:挂珠"   
    //"1627207:3232483:彩色;21433:95751240:布（打孔加工）;21433:95751238:布2.8M定高（不加工）;21433:95751239:布（挂钩加工）;21433:95751245:帘头加工;21433:95751244:纱（打孔加工）;21433:95751243:纱（挂钩加工）;21433:95751242:纱2.8M定高（不加工）;21433:95751241:挂珠"
//    @Test

    public void testWireless() {
        User user = UserDao.findByUserNick(nick);
        List<ItemPlay> list = ItemDao.findByUserId(user.getId(), 2);
        List<Long> ids = new ArrayList<Long>();
        for (ItemPlay itemPlay : list) {
//            if (itemPlay.getNumIid().longValue() != 16150830478L) {
//                continue;
//            }

            ids.add(itemPlay.getNumIid());
            log.info("[item:]" + itemPlay.getTitle());
        }

        WireLessDetailConfig config = new WireLessDetailConfig();
        config.setSkipExist(false);
        config.setNumIids(StringUtils.join(ids, ','));

        AutoTitleTask task = new AutoTitleTask(user.getId(), JsonUtil.getJson(config), UserTaskStatus.New,
                UserTaskType.BuildPhoneDetailByNumIids);

        task.setCreateTime(System.currentTimeMillis());
        task.save();

    }
}
