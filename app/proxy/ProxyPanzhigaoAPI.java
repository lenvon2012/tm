
package proxy;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import proxy.CommonProxyPools;

import org.apache.commons.lang3.StringUtils;
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
import com.ciaosir.client.api.API;
import com.ciaosir.client.utils.NumberUtil;
import com.mchange.v1.util.ArrayUtils;

import configs.TMConfigs;

public class ProxyPanzhigaoAPI extends IProxy {
    private static final Logger log = LoggerFactory.getLogger(ProxyPanzhigaoAPI.class);

    public static ProxyPanzhigaoAPI _instance = new ProxyPanzhigaoAPI();

    static String tradeId = "czn123";
    
    public ConcurrentLinkedQueue<HttphostWrapper> genProxies(int num) {
    	CommonProxyPools.panzhigao_shutDownAt = 0L;
        if (num <= 0) {
            num = getFetchNum();
        }

        ConcurrentLinkedQueue<HttphostWrapper> hosts = new ConcurrentLinkedQueue<HttphostWrapper>();
        try {
            boolean success = false;
            String panzhidao = null;
            int tryNum = 10;
            while (!success && tryNum > 0) {
                log.info("try num ;::::::" + tryNum);
                tryNum--;
                panzhidao = API.directGet(
                        "http://121.199.30.168:2222/api.asp?ddbh=czn123&noinfo=true&china=1&sl="
                                + num, "", null);
                /*panzhidao = ProxyPanzhigaoAPI.directGet(
                        "http://www.acintb.com/proxyG.php?ddh=477361231067621&sl=500&dq=&dianxin=a&liantong=b&yidong=c&tietong=d&dk18186=A&dk1998=B&dk8080=C&dk3128=D&dk80=E&dk78=F&kt="
                               );*/
                if (panzhidao.indexOf("684233052428263") < 0 && panzhidao.indexOf("null") < 0) {
                    success = true;
                }
            }
            if (tryNum == 0) {
                log.error("panzhigao is terriblly dead, you shoule use another daili mode");
                CommonProxyPools.panzhigao_shutDownAt = System.currentTimeMillis();
                return null;
            }
            String[] hostPorts = panzhidao.split("\n");
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

    public ConcurrentLinkedQueue<HttphostWrapper> genProxiesUsingCloud(int num) {

        if (num <= 0) {
            num = getFetchNum();
        }               
        String panzhigaoURL = "http://121.199.30.168:2222/api.asp?ddbh=czn123&noinfo=true&china=1&sl=";
        if(StringUtils.isEmpty(panzhigaoURL)) {
        	panzhigaoURL = "http://121.199.30.168:2222/api.asp?ddbh=czn123&noinfo=true&china=1&sl=";
        }
        ConcurrentLinkedQueue<HttphostWrapper> hosts = new ConcurrentLinkedQueue<HttphostWrapper>();
        try {
            boolean success = false;
            String panzhidao = null;
            int tryNum = 10;
            while (!success && tryNum > 0) {
                log.info("try num for cloud ProxyPanzhigaoAPI;::::::" + tryNum);
                tryNum--;
                panzhidao = directGet(panzhigaoURL  + num);
                /*panzhidao = ProxyPanzhigaoAPI.directGet(
                        "http://www.acintb.com/proxyG.php?ddh=477361231067621&sl=500&dq=&dianxin=a&liantong=b&yidong=c&tietong=d&dk18186=A&dk1998=B&dk8080=C&dk3128=D&dk80=E&dk78=F&kt="
                               );*/
                if(!StringUtils.isEmpty(panzhidao)) {
                	if (panzhidao.indexOf("684233052428263") < 0 && panzhidao.indexOf("null") < 0
                			&& panzhidao.indexOf("czn12") < 0) {
                		success = true;
                	}
                }
                CommonUtils.sleepQuietly(1000);
                
            }
            if (tryNum == 0) {
                log.error("panzhigao is terriblly dead, you shoule use another daili mode");
                // 切换代理模式
                TMConfigs.DAILIMODE = 1;
                CommonProxyPools.panzhigao_shutDownAt = System.currentTimeMillis();
                return null;
            }
            String[] hostPorts = panzhidao.split("\n");
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
    
    public static ProxyPanzhigaoAPI getInstance() {
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

            HttpConnectionParams.setSoTimeout(httpclient.getParams(), 50000);
            HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 50000);
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
            if (e.getMessage().contains("refused")) {
                throw new RuntimeException("");
            }
        }
        return null;
    }
}
