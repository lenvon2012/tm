
package controllers;

import org.apache.commons.lang.StringUtils;

import play.db.jpa.NoTransaction;
import play.mvc.Controller;

import com.ciaosir.client.api.API;
import com.ciaosir.client.utils.NetworkUtil;

public class Go extends Controller {
    @NoTransaction
    public static void content(String url, String referer, String ua, String cookie) {
        String ip = NetworkUtil.getRemoteIPForNginx(request);
        if (!ip.startsWith("10.128.") && !"10.241.47.89".equals("ip")) {
            forbidden();
        }
        if (StringUtils.isEmpty(ua)) {
            ua = "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)";
        }
        String get = API.directGet(url, referer, ua, null, cookie);
        renderText(StringUtils.isEmpty(get) ? StringUtils.EMPTY : get);
    }

}
