
package controllers;

import static java.lang.String.format;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jdp.ApiJdpAdapter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.mvc.Controller;
import utils.PlayUtil;
import autotitle.AutoTitleAction;
import bustbapi.ItemApi;
import bustbapi.ShopApi.ShopGet;

import com.ciaosir.client.utils.NumberUtil;
import com.google.gson.Gson;
import com.taobao.api.domain.Item;

public class Free extends Controller {

    private static final Logger log = LoggerFactory.getLogger(Free.class);

    public static final String TAG = "Free";

    public static void autotitle(long numIid, String callback) {
        if (numIid <= 0L) {
            renderTitleResult(StringUtils.EMPTY, callback);
        }

        Item item = ApiJdpAdapter.tryFetchSingleItem(null, numIid);
        if (item == null) {
            renderTitleResult(StringUtils.EMPTY, callback);
        }

        String cacheKey = "NoCookies.autotitle" + numIid;
        String title = null;
        if ((title = (String) Cache.get(cacheKey)) != null) {
        } else {
            title = AutoTitleAction.autoRecommend(item);
            Cache.set(cacheKey, title, "1h");

        }
        if (StringUtils.isEmpty(title)) {
            renderTitleResult(StringUtils.EMPTY, callback);
        } else {
            renderTitleResult(title, callback);
        }

    }

    static void renderTitleResult(String title, String callback) {

        log.info(format("renderTitleResult:title, callback".replaceAll(", ", "=%s, ") + "=%s", title, callback));
        StringBuilder sb = new StringBuilder();
        sb.append("{\"title\":\"");
        sb.append(title);
        sb.append("\"}");

        if (!StringUtils.isEmpty(callback)) {
            sb.insert(0, "(");
            sb.insert(0, callback);
            sb.append(")");
        }

        renderJSON(sb.toString());

    }

    public static void multipleTbItems(String ids) {
        log.info(format("multipleTbItems:ids".replaceAll(", ", "=%s, ") + "=%s", ids));
        if (StringUtils.isBlank(ids)) {
            renderJSON("[]");
        }

        String[] splits = ids.split(",");
        Set<Long> numIids = new HashSet<Long>();
        for (String string : splits) {
            Long id = NumberUtil.parserLong(string, 0L);
            if (NumberUtil.isNullOrZero(id)) {
                continue;
            } else {
                numIids.add(id);
            }
        }

        List<Item> list = ItemApi.tryItemList(null, numIids, true);
        renderJSON(new Gson().toJson(list));
    }

    public static void singleItem(long id) {

        log.info(format("singleItem:id".replaceAll(", ", "=%s, ") + "=%s", id));
        if (id <= 0L) {
            renderJSON(StringUtils.EMPTY);
        }

        Item item = new ItemApi.ItemFullGet(StringUtils.EMPTY, id).call();
        renderJSON(new Gson().toJson(item));

    }

    public static void prettyItem(long id) {
        Item item = new ItemApi.ItemFullGet(StringUtils.EMPTY, id).call();
        renderJSON(PlayUtil.genPrettyGson().toJson(item));
    }

    public static void singleShop(String username) {
        renderJSON(new ShopGet(username).call());
    }
}
