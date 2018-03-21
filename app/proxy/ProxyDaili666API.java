package proxy;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.mvc.Http.StatusCode;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.NumberUtil;
import com.taobao.api.internal.util.StringUtils;

public class ProxyDaili666API extends IProxy {
    private static final Logger log = LoggerFactory.getLogger(ProxyDaili666API.class);

    public static ProxyDaili666API _instance = new ProxyDaili666API();
    
    public ConcurrentLinkedQueue<HttphostWrapper> genProxies(int num) {

        CommonProxyPools.daili666_shutDownAt = 0L;
        if (num <= 0) {
            num = getFetchNum();
        }

        ConcurrentLinkedQueue<HttphostWrapper> hosts = new ConcurrentLinkedQueue<HttphostWrapper>();
        try {
            boolean success = false;
            String daili666 = null;
            int tryNum = 10;
            while (!success && tryNum > 0) {
                log.info("try num for ProxyDaili666API;::::::" + tryNum);
                tryNum--;
                daili666 = directGet("http://tp.daili666.com/ip/?tid=792100697608263&num=" + num);
                if(!StringUtils.isEmpty(daili666)) {
                    if (true) {
                        success = true;
                    }
                }
                CommonUtils.sleepQuietly(1000);
                
            }
            if (tryNum == 0) {
                //log.error("daili666 is terriblly dead, you shoule use another daili mode");
                CommonProxyPools.daili666_shutDownAt = System.currentTimeMillis();
                return null;
            }
            String[] hostPorts = daili666.split("\r\n");
            log.error("foudn host mode 4: success");
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
                    String portStr = hostPort.substring(hostPort.indexOf(":") + 1, hostPort.length());
                    if (!NumberUtils.isNumber(portStr)) {
                        continue;
                    }
                    int port = NumberUtil.parserInt(portStr, 80);
                    //log.error(" add mode 4 ip :" + ip);
                    hosts.add(new HttphostWrapper(new HttpHost(ip, port), System.currentTimeMillis()));
                }
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        }
        return hosts;
    }

    public static ProxyDaili666API getInstance() {
        return _instance;
    }

    public static void main(String[] args) throws IOException {
//        NewProxyAPI provider = NewProxyAPI.getInstance();
//        new ProxiesUpdate().doJob();
    }

    @Override
    public int getFetchNum() {
        return 100;
    }

    @Override
    public int getInejctNum() {
        return Play.mode.isDev() ? 2 : 150;
    }
    
    public static String directGet(String url) {
        HttpClient httpclient = null;

        HttpResponse rsp = null;

        try {
            httpclient = new DefaultHttpClient();

            HttpConnectionParams.setSoTimeout(httpclient.getParams(), 10000);
            HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 10000);
            httpclient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
            httpclient.getParams().setParameter(ClientPNames.MAX_REDIRECTS, 20);

            HttpGet httpGet = new HttpGet(url);


            httpGet.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)");

            rsp = httpclient.execute(httpGet);
            if (rsp.getStatusLine().getStatusCode() == StatusCode.MOVED) {
                rsp.getHeaders("Location");
            }
            HttpEntity entity = rsp.getEntity();
            String content = EntityUtils.toString(entity);
            EntityUtils.consume(entity);

            return content;
        } catch (Exception e) {
//            log.warn(e.getMessage(),e);
            log.warn(e.getMessage());
            /*if (e.getMessage().contains("refused")) {
                throw new RuntimeException("");
            }*/
        }
        return null;
    }
}
