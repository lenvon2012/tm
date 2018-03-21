package actions;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Http.StatusCode;

import com.ciaosir.client.utils.HttpClientUtil;

public class WangwangOnlineAction {

    private static final Logger log = LoggerFactory.getLogger(WangwangOnlineAction.class);

    public static final String TAG = "WangwangOnlineAction";

    public static final String DEFAULT_REFER = "http://shopsearch.taobao.com/search?q=";

    public static boolean isOnline(String nick) {
        String url = "http://amos.alicdn.com/online.aw?v=2&uid=" + nick + "&site=cntaobao&s=10&charset=UTF-8";

        HttpResponse rsp = HttpClientUtil.loadResponse(url, DEFAULT_REFER, false);
//        log.error(rsp.toString());
        if (rsp != null
                && (rsp.getStatusLine().getStatusCode() == StatusCode.MOVED || rsp.getStatusLine().getStatusCode() == StatusCode.FOUND)) {
            Header[] headerArray = rsp.getHeaders("Location");
            if (headerArray == null || headerArray.length == 0) {
                log.error("WangWang Online error: " + nick);
                return false;
            }
            String userUrl = headerArray[0].getValue();
            if (userUrl != null && userUrl.trim().endsWith("T1uUG.XjtkXXcb2gzo-77-19.gif")) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println(isOnline("tp_杭州万青"));
    }
}