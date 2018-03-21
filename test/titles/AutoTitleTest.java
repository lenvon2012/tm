
package titles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.test.UnitTest;
import autotitle.AutoSplit;
import autotitle.AutoTitleAction;

import com.ciaosir.client.api.WidAPIs;
import com.ciaosir.client.pojo.IWordBase;
import com.ciaosir.client.word.PaodingSpliter.SplitMode;
import com.ciaosir.commons.ClientException;

import dao.UserDao;
import dao.item.ItemDao;

public class AutoTitleTest extends UnitTest {

    private static final Logger log = LoggerFactory.getLogger(AutoTitleTest.class);

    public static final String TAG = "AutoTitleTest";

    static String[] titles = new String[] {
            "先锋取暖器 小太阳NSB-8DQ5/DF062 电暖器 暖风器 晶体发热取暖器", "先锋小太阳NSB-8DQ5DF062电暖暖风晶体发热取暖器石英管加热",
            "正品 金泰昌足浴盆TC-2026至尊型升级版 TC-1026 足浴盆/足浴器", "正品金泰昌盆TC-2026至尊型升级版TC-1026盆足浴器液晶显示"
    };

    @Test
    public void testSplit() throws ClientException {
        for (String title : titles) {
            List<String> execute = new WidAPIs.SplitAPI(title, SplitMode.BASE, false).execute();
            System.out.println(title);
            System.out.println(execute);
        }
    }

    public void testSingleItem() {
        User user = UserDao.findById(119068985L);
//        Long numIid = 12624305526L;
        Long numIid = 1573895755L;
        String autoRecommend = AutoTitleAction.autoRecommend(user, numIid);
        System.out.println(autoRecommend);
    }

//    @Test
    public void hell() throws ClientException {

        String target = null;
        //春秋, 秋冬, 孕妇装, 莎, 碧娜, t9025, 大, 码, 韩版, 灯芯绒, 孕妇, 托腹, 腹裤, 铅笔, 裤, 小脚, 裤
//        String target = "春秋冬孕妇装莎碧娜T9025大码韩版灯芯绒孕妇托腹裤铅笔裤小脚裤打底裤";
        //精品推荐男款日本原单C*KGOLF快干休闲长裤高尔夫裤BO白色黑色
        target = "大码韩";
        List<String> execute = new AutoSplit(target, ListUtils.EMPTY_LIST).execute();
        Map<String, IWordBase> wordbase = new WidAPIs.WordBaseAPI(execute).execute();
        List<String> toFilter = new ArrayList<String>();
        for (String string : execute) {
            IWordBase ibase = wordbase.get(string);
            if (ibase == null) {
                continue;
            }
            if (ibase.getPv() < 10) {
                continue;
            }
            toFilter.add(string);
        }

        // TODO 判断组合

        // TODO 差异过大。。那就拿掉吧。。。
        log.info("[res : ]" + execute);
    }

    /*public void test() {
        int count = 0;
        int max = 1000;
        List<User> users = User.findAll();
        for (User user : users) {
            if (!user.isVaild()) {
                continue;
            }
            if (count++ > max) {
                return;
            }

            doForUser(user);
        }
    }*/

    private void doForUser(User user) {
        List<ItemPlay> items = ItemDao.findByUserId(user.getId(), 30);
        for (ItemPlay itemPlay : items) {
            doForEachItem(user, itemPlay);
        }
    }

    private void doForEachItem(User user, ItemPlay itemPlay) {
        String recommend = AutoTitleAction.autoRecommend(user, itemPlay.getNumIid());

//        log.error("[origin title :\n" + call.getTitle() + "\n" + recommend);
//        System.out.println("++++" + itemPlay.getTitle() + "\t" + recommend);
//        System.out.println(itemPlay.getTitle() + "\n" + recommend);
//        System.out.println("----");
    }

}
