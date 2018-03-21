
package job.proxy;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import proxy.HttphostWrapper;
import proxy.IProxy;
import configs.TMConfigs;

//@Every("20s")
@OnApplicationStart(async = true)
public class ProxiesUpdate extends Job {
    private static final Logger log = LoggerFactory.getLogger(ProxiesUpdate.class);

//    public static long PROXY_MAX_ALIVE_MILLIS = DateUtil.ONE_MINUTE_MILLIS * 20;

    public static final int poolsize = 250;

    static String verifyUrl = "http://www.baidu.com/cache/global/img/gs.gif";

    public void doJob() {
        if (Play.mode.isDev()) {
            return;
        }
        if (!TMConfigs.ALLOW_AUTO_PROXY) {
            return;
        }
        log.info("proxy === mode " + TMConfigs.DAILIMODE);
        IProxy model = IProxy.getInstance();
        final ConcurrentLinkedQueue<HttphostWrapper> proxies = model.getProxies();
        int failNum = 0;
        int poolsize = model.getFetchNum();
        if (proxies.size() > 0) {
            verifyProxies(proxies, failNum);
        }

        if (proxies.size() < poolsize) {
            proxies.addAll(model.genProxies(poolsize - proxies.size()));
        }

        log.info("[TMProxiesUpdate new proxies pool size = " + model.getProxies().size() + "------ for class : " + model.getClass());
    }

    private void verifyProxies(final ConcurrentLinkedQueue<HttphostWrapper> proxies, int failNum) {
        int size = proxies.size();
        int count = 0;
        while (count++ < size) {
            HttphostWrapper hostwrapper = proxies.poll();
            if (hostwrapper == null) {
                log.error("remove null host!");
                continue;
            }
            if (System.currentTimeMillis() - hostwrapper.getCreated() > IProxy.getInstance().getExpiredTime()) {
                failNum++;
                log.error("remove host over 20 mins : " + hostwrapper.getHttphost());
                continue;
            }
            boolean flag = false;
            try {
                String result = directGet(verifyUrl, "", null, hostwrapper.getHttphost(), null);
                if (result != null && result.length() > 0) {
                    flag = true;
                }
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
            if (flag) {
                proxies.add(hostwrapper);
            } else {
                failNum++;
                log.error("remove host :" + hostwrapper.getHttphost());
            }
        }
        log.info("[original proxies pool size = " + size + "................................................]");
        log.info("[fail proxies size = " + failNum + "................................................]");
    }

    public static String directGet(String url, String referer, String ua, HttpHost host, String cookies) {
        HttpClient httpclient = null;

        HttpResponse rsp = null;

        try {
            httpclient = new DefaultHttpClient();
            if (host != null) {
                httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
            }

            HttpConnectionParams.setSoTimeout(httpclient.getParams(), 3000);
            HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 3000);
            httpclient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
            httpclient.getParams().setParameter(ClientPNames.MAX_REDIRECTS, 20);

            HttpGet httpGet = new HttpGet(url);
            if (referer != null) {
                httpGet.addHeader("Referer", referer);
            }
            if (cookies != null) {
                httpGet.addHeader("Cookie", cookies);
            }

            httpGet.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)");

            rsp = httpclient.execute(httpGet);
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
