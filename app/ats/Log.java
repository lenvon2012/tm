
package ats;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpHost;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.utils.NumberUtil;

public class Log implements Callable<Boolean> {

    private static final Logger log = LoggerFactory.getLogger(Log.class);

    public static final String TAG = "Log";

    public static Log _instance = new Log();

    public Log() {
    }

    static PYFutureTaskPool<Boolean> pool = new PYFutureTaskPool<Boolean>(256);

    String[] hosts = new String[] {
            "http://www.cz88.net/proxy/index.aspx", "http://www.cz88.net/proxy/http_2.aspx",
            "http://www.cz88.net/proxy/http_3.aspx", "http://www.cz88.net/proxy/http_4.aspx",
            "http://www.cz88.net/proxy/http_5.aspx", "http://www.cz88.net/proxy/http_6.aspx",
            "http://www.cz88.net/proxy/http_7.aspx", "http://www.cz88.net/proxy/http_8.aspx",
            "http://www.cz88.net/proxy/http_9.aspx", "http://www.cz88.net/proxy/http_10.aspx",
    };

    List<HttpHost> proxies = new ArrayList<HttpHost>();

    public void parseProxies() throws IOException {

        for (String host : hosts) {
            log.info("[check]" + host);
            Document parse = Jsoup.parse(new URL(host), 5000);
            Elements select = parse.select(".Main tr");
            for (Element element : select) {
                Elements tds = element.select("td");
                if (tds.size() < 2) {
                    continue;
                }

                String ip = tds.get(0).text();
                String portStr = tds.get(1).text();
                if (!NumberUtils.isNumber(portStr)) {
                    continue;
                }
                int port = NumberUtil.parserInt(portStr, 80);

                HttpHost proxy = new HttpHost(ip, port);
                proxies.add(proxy);
            }
        }
        log.info("[foudn hostss ;]" + proxies.size());
        log.info("[foudn hostss ;]" + proxies);
        FileUtils.writeLines(new File("/home/zrb/pro.txt"), proxies);
    }

    public Boolean call() {
        try {
//            parseProxies();
//            if (true) {
//                return null;
//            }
            List<String> lines = FileUtils.readLines(new File("/home/zrb/pro.txt"));
            for (String string : lines) {
                String[] split = string.split(":");
                if (ArrayUtils.isEmpty(split)) {
                    continue;
                }
                System.out.println(string);
                proxies.add(new HttpHost(split[1].substring(2), Integer.parseInt(split[2])));
            }
            int count = 0;
            for (final HttpHost host : proxies) {
                log.info("[sumit ]" + count++);
                pool.submit(new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        System.out.println("host :" + host);
                        while (true) {
//                            API.directGet("http://dz.31715.com/vmerge.js?/js/apps/alldz.js&/index.js",
//                                    "http://dz.31715.com/", null, host);
//                            API.directGet("http://y.tobbn.com", "hello", null, host);
                            Thread.sleep(500L);
                        }
                    }
                });
//                if (count > 20) {
//                    return null;
//                }
            }

        } catch (IOException e) {
            log.warn(e.getMessage(), e);

        }
        return null;
    }
}
