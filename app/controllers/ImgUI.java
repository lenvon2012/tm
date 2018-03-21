
package controllers;

import java.util.UUID;

import job.writter.VisitLogWritter;

import org.apache.commons.lang.StringUtils;

import play.db.jpa.NoTransaction;
import play.mvc.Controller;
import play.mvc.Http.Cookie;
import play.mvc.Http.Header;
import actions.MonitorAction;

import com.ciaosir.client.url.URLParser;
import com.ciaosir.client.utils.NetworkUtil;

public class ImgUI extends Controller {

    private static final String USER_COOKIE_KEY = "MonitorCookie";

    //监控所使用的图片
    @NoTransaction
    public static void visitorImg(Long userId) {
        if(true){
            return;
        }

        String ip = "";
        ip = NetworkUtil.getRemoteIPForNginx(request);
        //测试用ip
        /*String[] ipArray = new String[]{"114.80.166.240", 
        		"122.224.74.82",
        		"60.191.132.102",
        		"210.51.167.169",
        		"124.192.60.5",
        		"210.82.113.17",
        		"125.92.95.63",
        		"221.219.2.103",
        		"220.180.150.34"};
        int rand = (int)(Math.random() * 100) % ipArray.length;
        ip = ipArray[rand];*/

        if (userId == null)
            return;

        if (ip == null || ip.length() == 0)
            return;
        Header header = request.headers.get("referer");
        if (header == null) {
//    		MixHelpers.infoAll(request, response);
            return;
        }
        String url = header.value();
        if (url.startsWith("http://"))
            url = url.substring("http://".length());
        long numIid = URLParser.findItemId(url);
        //String userCookie = getCookieId();
        String userCookie = ip;

        MonitorAction.addUserMonitor(ip, userId, numIid, userCookie);

        //直通车
        long timeMillis = System.currentTimeMillis();
        VisitLogWritter.addMsg(userCookie, ip, userId, timeMillis, url);

    }

    private static String getCookieId() {
        Cookie cookie = request.cookies.get(USER_COOKIE_KEY);
        String userCookie = "";
        if (cookie == null || StringUtils.isEmpty(cookie.value)) {
            //随机生成字符串
            userCookie = UUID.randomUUID().toString();
            response.setCookie(USER_COOKIE_KEY, userCookie, "1d");
        } else
            userCookie = cookie.value;

        return userCookie;

    }

}
