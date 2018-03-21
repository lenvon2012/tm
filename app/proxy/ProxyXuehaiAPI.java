
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

public class ProxyXuehaiAPI extends IProxy {
    private static final Logger log = LoggerFactory.getLogger(ProxyXuehaiAPI.class);

    public static ProxyXuehaiAPI _instance = new ProxyXuehaiAPI();

    public ConcurrentLinkedQueue<HttphostWrapper> genProxies(int num) {
        if (num <= 0) {
            num = getFetchNum();
        }
        ConcurrentLinkedQueue<HttphostWrapper> hosts = new ConcurrentLinkedQueue<HttphostWrapper>();
        try {
            String xuehai = API.directGet("http://www.xjgqhg.cn/apiProxy.ashx?un=boyvon&pw=boyvon&tm=60&count=" + num,
                    "", null);
            String[] hostPorts = xuehai.split("\n");
            log.error("foudn host mode 3:" + ArrayUtils.toString(hostPorts) + "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
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
                    log.error(" add mode 3 ip :" + ip);
                    hosts.add(new HttphostWrapper(new HttpHost(ip, port), System.currentTimeMillis()));
                }
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        }
        return hosts;
    }

    public static ProxyXuehaiAPI getInstance() {
        return _instance;
    }

    public static void main(String[] args) throws IOException {
        ProxyXuehaiAPI provider = ProxyXuehaiAPI.getInstance();
        //new ProxiesUpdate().doJob();
    }

    @Override
    public int getFetchNum() {
        return 500;
    }
}
