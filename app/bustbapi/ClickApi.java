
package bustbapi;

import static java.lang.String.format;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import play.libs.WS;
import play.libs.WS.WSRequest;

import com.ciaosir.client.CommonUtils;

import controllers.APIConfig;

public class ClickApi {

    private static final Logger log = LoggerFactory.getLogger(ClickApi.class);

    public static final String TAG = "ClickApi";

//    static String[] servers = new String[] {
////            "http://58.196.167.15:9092/go/reClick", "http://110.76.45.127:9092/go/reClick",
//            "http://42.120.17.136:9092/go/reClick", "http://58.196.167.15:9092/go/reClick",
//            "http://58.196.167.15:9092/go/reClick", "http://42.120.17.136:9092/go/reClick",
//            "http://58.196.167.15:9092/go/reClick", "http://42.120.17.136:9092/go/reClick",
//            "http://58.196.167.15:9092/go/reClick",
//    };

    public static void main(String[] args) throws InterruptedException {

    }

    public static class DoAttack extends Job {

        String referer = "http://www.taodake.com";

        String url = "http://www.taodake.com/js.php?id=index_js";

        String cookie = null;

        int count = 5;

        public DoAttack() {
            super();
        }

        public DoAttack(String referer, String url, String cookie, int count) {
            super();
            this.referer = referer;
            this.url = url;
            this.cookie = cookie;
            this.count = count;
        }

        public DoAttack(String referer, String url) {
            super();
            this.referer = referer;
            this.url = url;
        }

        public DoAttack(String referer, String url, int count) {
            super();
            this.referer = referer;
            this.url = url;
            this.count = count;
        }

        public void doJob() {
            long millis = System.currentTimeMillis();
            String host = servers[(int) (millis % ((long) servers.length))];
            log.info("[host:]" + host);
            WSRequest req = WS.url(host);
            req.setParameter("url", url);
            if (this.referer != null) {
                req.setParameter("referer", referer);
            }
            if (this.count > 0) {
                req.setParameter("count", count);
            }
            if (this.cookie != null) {
                req.setParameter("cookie", cookie);
            }
            req.getAsync();

        }
    }

    public static class DoChedaoCilckJob extends Job {

        String url;

        public DoChedaoCilckJob() {
        }

        public DoChedaoCilckJob(String url) {
            super();
            this.url = url;

        }

        public void doJob() {
            try {
                if (url == null) {
                    url = "http://fuwu.taobao.com/ser/detail.htm?service_code=FW_GOODS-1841777&tracelog=disanfang";
                }
                while (true) {
                    realClick(url, "", "", 30, "http://www.tianxiaomao.com", "", 0);
                    Thread.sleep(200L);
                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);

            }
        }
    }

//    static String[] servers = new String[] {
//            "http://bbn06:9092/go/reClick", "http://bbn07:9092/go/reClick", "http://bbn08:9092/go/reClick",
//            "http://bbn09:9092/go/reClick", "http://bbn10:9092/go/reClick",
//    };
//
//    static String[] attackServers = new String[] {
//            "http://bbn06:9092/go/attack", "http://bbn07:9092/go/attack", "http://bbn08:9092/go/attack",
//            "http://bbn09:9092/go/attack", "http://bbn10:9092/go/attack",
//    };

    static String[] servers = APIConfig.get().getClickServers();

    static int count = 0;

    public static void doClick(String url1, String url2, String url3, int waitTime, String referer) {

        count = (count + 1) % servers.length;
        String host = servers[count];

//        log.info(format("doClick:url1, url2, url3, waitTime, referer".replaceAll(", ", "=%s, ") + "=%s", url1, url2,
//                url3, waitTime, referer));
//        log.info("[host: ]" + host);

        WSRequest req = WS.url(host).setParameter("url1", url1).setParameter("url2", url2).setParameter("url3", url3)
                .setParameter("referer", referer).setParameter("waitTime", waitTime);
//        log.info("[req ]" + host + " with referer :" + referer);

        req.getAsync();
//        HttpResponse resp = req.getAsync();
//        String str = resp.getString();
//        log.warn("click finished....................................................");
    }

    public static void realClick(String url1, String url2, String url3, int waitTime, String referer, String proxy,
            int port) {

        log.info(format("realClick:url1, url2, url3, waitTime, referer, proxy, port".replaceAll(", ", "=%s, ") + "=%s",
                url1, url2, url3, waitTime, referer, proxy, port));

        long millis = System.currentTimeMillis();
        String host = servers[(int) (millis % ((long) servers.length))];
        WSRequest req = WS.url(host).setParameter("url1", url1).setParameter("url2", url2).setParameter("url3", url3)
                .setParameter("referer", referer).setParameter("waitTime", waitTime).setParameter("proxy", proxy)
                .setParameter("port", port);

        req.getAsync();
        log.info("[req ]" + req.url);
//        HttpResponse resp = req.getAsync();
//        String str = resp.getString();
        log.warn("click finished....................................................");
    }

    public static void doClicks(String url, String referer, int count) {
        long millis = System.currentTimeMillis();
        String host = servers[(int) (millis % ((long) servers.length))];
        for (int i = 0; i < count; i++) {
            WSRequest req = WS.url(host).setParameter("url1", url).setParameter("referer", referer);
            log.info("[req ]" + req.toString());
            req.getAsync();
            CommonUtils.sleepQuietly(60000);
        }
        log.warn("doclick with count = " + count + " finished....................................................");
    }

    public static void doWord() {

        String url1 = "http://item.taobao.com/item.htm?id=22392388597";
        String url2 = "http://item.taobao.com/item.htm?id=17135745428";

        doClick(url1, url2, null, 30, "http://www.tobbn.com/show");
    }

}
