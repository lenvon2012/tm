
package props;

import java.util.ArrayList;
import java.util.List;

import models.item.ItemPlay;
import models.user.User;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.test.UnitTest;
import utils.PlayUtil;
import autotitle.AutoSplit;
import autotitle.ItemPropAction;
import autotitle.ItemPropAction.SkuVidPrices;
import bustbapi.ItemApi;

import com.ciaosir.commons.ClientException;
import com.google.gson.Gson;
import com.taobao.api.domain.Item;

import dao.UserDao;
import dao.item.ItemDao;

public class ItemPropTest extends UnitTest {

    User user = UserDao.findByUserNick("江苏阿冬");

    private static final Logger log = LoggerFactory.getLogger(ItemPropTest.class);

    public static final String TAG = "ItemPropTest";

    @Test
    public void testEnsureSku() {
//        Long numIid = 36334310153L;
//        String sid = "62011120b7465313a6f2ZZ5a3350b86bd0ee0f8aeaa1f9779742176";
//        User user = UserDao.findByUserNick("clorest510");
//        user.setSessionKey(sid);
        User user = UserDao.findByUserNick("sandbox_autotitle");
        List<ItemPlay> items = ItemDao.findByUserId(user.getId());
        ItemPlay target = ItemDao.findByNumIid(user.getId(), 2100512344093L);
        items.clear();
        items.add(target);
        for (ItemPlay itemPlay : items) {

            Item item = new ItemApi.ItemGet(user, itemPlay.getNumIid(), true).call();
            log.info("[back item:]" + PlayUtil.genPrettyGson().toJson(item) + " item detail :" + item.getDetailUrl()
                    + " and pic url:" + item.getPicUrl());
            List<SkuVidPrices> prices = SkuVidPrices.parseMultiplePrices(item);
            log.info("prices :" + prices);
        }
    }

    public void testPropAlis() {
    	System.out.println("---------------------------------------------yehuizhang2-----------------------");
        Long numIid = 19197471111L;
        ItemPlay item = ItemDao.findByNumIid(user.getId(), numIid);
        Item res = new ItemApi.ItemGet(user, numIid, true).call();
        System.out.println(new Gson().toJson(res));
        System.out.println(res.getPropertyAlias());
        System.out.println(ItemPropAction.mergePropAlis(res.getPropsName(), res.getPropertyAlias()));
    }

    public void testSplit() throws ClientException {
    	System.out.println("---------------------------------------------yehuizhang-----------------------");
//        String target = "包邮2013情侣装夏装新款韩国彩色条纹短袖t恤女装韩版领polo翻领 色";
        List<String> brandNames = new ArrayList<String>();
        brandNames.add("新款韩国");
        String target = "包邮2013情侣装夏装新款韩国古由卡彩色条纹短袖t恤女装韩版领polo翻领 色";
        List<String> res = new AutoSplit(target, brandNames).execute();
        System.out.println(res);
    }

}
