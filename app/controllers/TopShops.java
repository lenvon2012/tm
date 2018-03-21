
package controllers;

import java.util.List;

import models.industry.TopShop;
import models.industry.TopShopItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.utils.JsonUtil;

public class TopShops extends TMController {

    private static final Logger log = LoggerFactory.getLogger(TopShops.class);

    public static final String TAG = "TopShop";

    public static void searchShopCat(String shopcat) {
//    	 System.out.println("********************");
//         System.out.println("********************");
//         System.out.println(shopcat);
//         System.out.println("********************");
//         System.out.println("********************");
        List<TopShop> topShop = TopShop.findByCat(shopcat);
        renderJSON(JsonUtil.getJson(topShop));
    }

    public static void searchShopItem(long userId) {
        List<TopShopItem> shopItems = TopShopItem.findByUser(userId);
        for (TopShopItem topShopItem : shopItems) {
            log.info("[topShopItem]" + topShopItem);
        }
        renderJSON(JsonUtil.getJson(shopItems));
    }
}
