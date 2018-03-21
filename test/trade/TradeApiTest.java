
package trade;

import java.util.List;

import jdp.JdpModel.JdpTradeModel;

import models.item.ItemPlay;
import models.user.User;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.taobao.api.domain.Trade;

import play.test.UnitTest;
import utils.PlayUtil;
import bustbapi.TMTradeApi;
import bustbapi.TMTradeApi.ShopBaseTradeInfo;
import cache.UserHasTradeItemCache;
import dao.UserDao;

public class TradeApiTest extends UnitTest {

    private static final Logger log = LoggerFactory.getLogger(TradeApiTest.class);

    public static final String TAG = "TradeApiTest";

    @Test
    public void testFetchJson() {

    }

    public void testRecentMonthSale() {
        User user = null;
        user = UserDao.findByUserNick("zhengye83");
//        user = UserDao.findByUserNick("楚之小南");
        try {
            ShopBaseTradeInfo map = TMTradeApi.buildNumIidSaleMap(user, 30);
            log.info("[map :]" + map);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        }
    }
    @Test
    public void testFetch() {
    	System.out.println("-------------------zhengye83--------------------");
        User user = UserDao.findByUserNick("zhengye83");
        List<ItemPlay> list = UserHasTradeItemCache.getByUserForShowWindow(user);
        System.out.println(list);
//        System.out.println(list);
    }

    public void getOneMobile() {
        String sid = "6101820fe8788a531e72d3b4a0e416e0af83d3ddf2974d576697832";
        String sellerMobile = TMTradeApi.getSellerMobile(sid);
        System.out.println(sellerMobile);
    }

    public void testFetchFromJdp() {
        List<Trade> list = JdpTradeModel.fetchTrades(" 1 = 1 limit 10 ");
        for (Trade trade : list) {
            System.out.println(PlayUtil.genPrettyGson().toJson(trade));
        }
    }

}
