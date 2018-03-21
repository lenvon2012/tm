
package controllers;

import java.util.Map;

import models.item.ItemCatPlay;
import models.user.User;
import cache.CountItemCatCache;
import cache.CountSellerCatCache;

import com.ciaosir.client.utils.JsonUtil;
import com.taobao.api.domain.SellerCat;

import controllers.Items.CatIdNameCount;

public class SeoWay extends TMController {

    public static void index() {
        render("seoway/seowayindex.html");
    }

    public static void sellerCats() {
        User user = getUser();
        Map<SellerCat, Integer> map = CountSellerCatCache.get().getByUser(user);
        renderJSON(JsonUtil.getJson(CatIdNameCount.buildSellerCats(map)));
    }

    public static void itemCats() {
        User user = getUser();
        Map<ItemCatPlay, Integer> map = CountItemCatCache.get().getByUser(user);
        renderJSON(JsonUtil.getJson(CatIdNameCount.buildItemCats(map)));
    }

    public static void list(String status, long sellerCat, long itemCat, int pn, int ps, String word) {
        
    }
}
