/**
 * 
 */

package controllers;

import static java.lang.String.format;

import java.io.IOException;
import java.util.List;

import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.lang.StringUtils;

import com.ciaosir.client.api.API;
import com.ciaosir.client.utils.JsonUtil;

import dao.item.ItemDao;

/**
 * @author navins
 * @date: 2013年10月10日 下午9:28:21
 */
public class TxmHome extends TMController {

    public static void findUserItems() {
        User user = getUser();
        List<ItemPlay> list = ItemDao.findByUserId(user.getId());
        renderJSON(JsonUtil.getJson(list));
    }

    static String TXM_MAINSEARCH_REQ_URL = "http://www.tianxiaomao.com/TMMainSearch/adWords?sellerId=%d&numIid=%d&pn=%d&ps=%d";

    public static void searchWords(long sellerId, long numIid, String callback, int pn, int ps) throws IOException {
        if (sellerId <= 0) {
            sellerId = getUser().getIdlong();
        }
        String url = format(TXM_MAINSEARCH_REQ_URL, sellerId, numIid, pn, ps);
        if (!StringUtils.isEmpty(callback)) {
            url += "&callback=" + callback;
        }
        log.info("[Request txm url] " + url);
        String content = API.directGet(url, "", null);
        renderJSON(content);
    }

}
