
package proxy;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;

import com.ciaosir.client.api.API;
import com.ciaosir.client.utils.NumberUtil;
import com.mchange.v1.util.ArrayUtils;

public class ProxyAcintbAPI extends IProxy {
    private static final Logger log = LoggerFactory.getLogger(ProxyAcintbAPI.class);

    public static ProxyAcintbAPI _instance = new ProxyAcintbAPI();

    static String tradeId = "477361231067621";

    public ConcurrentLinkedQueue<HttphostWrapper> genProxies(int num) {

        if (num <= 0) {
            num = getFetchNum();
        }

        ConcurrentLinkedQueue<HttphostWrapper> hosts = new ConcurrentLinkedQueue<HttphostWrapper>();
        try {
            boolean success = false;
            String acintb = null;
            int tryNum = 10;
            while (!success && tryNum > 0) {
                log.info("try num ;::::::" + tryNum);
                tryNum--;
                acintb = API.directGet(
                        "http://www.acintb.com/proxyG.php?ddh=" + tradeId +"&sl=" + num + 
                        "&dq=&dianxin=a&liantong=b&yidong=c&tietong=d&dk18186=A&dk1998=B&dk8080=C&dk3128=D&dk80=E&kt=&old=!"
                		, "", null);
                if (acintb.indexOf("80004005") < 0) {
                    success = true;
                }
            }
            if (tryNum == 0) {
                log.error("Acintb is terriblly dead, you shoule use another daili mode");
                return null;
            }
            String[] hostPorts = acintb.split("\n");
            log.error("foudn host mode 2:" + ArrayUtils.toString(hostPorts) + "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
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
                    log.error(" add mode 2 ip :" + ip);
                    hosts.add(new HttphostWrapper(new HttpHost(ip, port), System.currentTimeMillis()));
                }
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        }
        return hosts;
    }

    public static ProxyAcintbAPI getInstance() {
        return _instance;
    }

    public static void main(String[] args) throws IOException {
//        NewProxyAPI provider = NewProxyAPI.getInstance();
//        new ProxiesUpdate().doJob();
    }

    @Override
    public int getFetchNum() {
        return 800;
    }

    @Override
    public int getInejctNum() {
        return Play.mode.isDev() ? 2 : 150;
    }
}
