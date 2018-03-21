
package proxy;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.api.API;
import com.ciaosir.client.utils.NumberUtil;
import com.mchange.v1.util.ArrayUtils;

public class ProxyDaili999API extends IProxy {
    private static final Logger log = LoggerFactory.getLogger(ProxyDaili999API.class);

    public static ProxyDaili999API _instance = new ProxyDaili999API();


    // start at 2014/06/26
    public static String order999Id = "796161196808263";

    public ConcurrentLinkedQueue<HttphostWrapper> genProxies(int num) {
        if (num <= 0) {
            num = getFetchNum();
        }
        ConcurrentLinkedQueue<HttphostWrapper> hosts = new ConcurrentLinkedQueue<HttphostWrapper>();
        /**
         * 419719559351064
         */
        String baseUrl = "http://www.daili999.com/api.php?key=";
        try {
            int area = (int) Math.floor(Math.random() * 3) + 1;
            if (area > 3) {
                area = 3;
            }
            String[] hostPorts = API.directGet(baseUrl + order999Id + "&getnum=" + num + "&area=all", "", null)
                    .split("\n");

            log.error("foudn host mode 1:" + ArrayUtils.toString(hostPorts) + "uuuuuuuuuuuuuuuuuuuuu");
            if (hostPorts != null && hostPorts.length > 0) {
                for (String hostPort : hostPorts) {
                    int index = hostPort.indexOf(":");
                    if (index <= -1) {
                        continue;
                    }
                    String ip = hostPort.substring(0, index);
                    if (ip.length() < 8) {
                        continue;
                    }
                    String portStr = hostPort.substring(hostPort.indexOf(":") + 1, hostPort.length() - 1);
                    if (!NumberUtils.isNumber(portStr)) {
                        continue;
                    }
                    int port = NumberUtil.parserInt(portStr, 80);
                    log.error(" add mode 1 ip :" + ip);
                    hosts.add(new HttphostWrapper(new HttpHost(ip, port), System.currentTimeMillis()));
                }
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        }
        return hosts;
    }

    public static ProxyDaili999API getInstance() {
        return _instance;
    }

    public static void main(String[] args) throws IOException {
        //ProxyAPI provider = ProxyAPI.getInstance();
        //new ProxiesUpdate().doJob();
    }

    @Override
    public int getFetchNum() {
        return 500;
    }
}
