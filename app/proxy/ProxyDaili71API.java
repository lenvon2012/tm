
package proxy;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import proxy.CommonProxyPools;

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

import play.mvc.Http.StatusCode;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.API;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.mchange.v1.util.ArrayUtils;
import com.taobao.api.internal.util.StringUtils;

import configs.TMConfigs;

public class ProxyDaili71API extends IProxy {
    private static final Logger log = LoggerFactory.getLogger(ProxyDaili71API.class);

    public static ProxyDaili71API _instance = new ProxyDaili71API();


    // start at 2014/06/26
    public static String order999Id = "778326916128263";

    public static String staticURL = "http://www.71https.com/api.asp?key=";
    
    public ConcurrentLinkedQueue<HttphostWrapper> genProxies(int num) {
    	CommonProxyPools.daili71_shutDownAt = 0L;
        if (num <= 0) {
            num = getFetchNum();
        }
        ConcurrentLinkedQueue<HttphostWrapper> hosts = new ConcurrentLinkedQueue<HttphostWrapper>();
        /**
         * 419719559351064
         */
        String baseUrl = staticURL;
        if(StringUtils.isEmpty(baseUrl)) {
        	baseUrl = "http://www.71https.com/api.asp?key=";
        }
        
        	Boolean success = false;
        	int tryNum = 10;
        	String[] hostPorts = null;
        	while (!success && tryNum > 0) {
        		log.info("try num ProxyDaili71API ;::::::" + tryNum);
        		tryNum--;

                 try {
					 hostPorts = directGet(baseUrl + order999Id + "&getnum=" + num + "&area=1")
                         .split("\r\n");
	                 if(hostPorts != null && hostPorts.length > 0) {
	                	 success = true;
	                 }

				} catch (Exception e) {
					// TODO: handle exception
					log.error(e.getMessage());
				}
                 CommonUtils.sleepQuietly(1000);
        	}
           
        	if(tryNum == 0) {
        		log.error("daili71 is terriblly dead, you shoule use another daili mode");
        		CommonProxyPools.daili71_shutDownAt = System.currentTimeMillis();
        		return null;
        	}

            log.error("foudn host mode for daili71:" + ArrayUtils.toString(hostPorts) + "71717171717171717171");
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
                    log.error(" add mode 71 ip :" + ip);
                    hosts.add(new HttphostWrapper(new HttpHost(ip, port), System.currentTimeMillis()));
                }
            }

       
        return hosts;
    }

    public static ProxyDaili71API getInstance() {
        return _instance;
    }

    public static void main(String[] args) throws IOException {
        //ProxyAPI provider = ProxyAPI.getInstance();
        //new ProxiesUpdate().doJob();
    }

    @Override
    public int getFetchNum() {
        return 100;
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
            if (e.getMessage().contains("refused")) {
                throw new RuntimeException("");
            }
        }
        return null;
    }
}
