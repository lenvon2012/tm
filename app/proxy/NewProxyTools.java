package proxy;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang.StringUtils;
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

import play.libs.WS;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Created by hao on 16-2-29.
 */
public class NewProxyTools{
    private static final Logger log = LoggerFactory.getLogger(NewProxyTools.class);
    
    public static final String DEFAULT_UA="Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)";
    
    public static final String MOBILE_UA="Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1";
    
    private static ConcurrentLinkedQueue<HttpHost> hosts = new ConcurrentLinkedQueue<HttpHost>();

    public static void initPools() {
        WS.WSRequest url = WS.url("http://dev.kuaidaili.com/api/getproxy")
                .setParameter("orderid", "948897688929041")
                .setParameter("num", "200")
                .setParameter("quality", "1")
                .setParameter("format", "json");

        String string = url.get().getString();

        JSONObject jsonObject = JSON.parseObject(string);
        int code = jsonObject.getIntValue("code");
        if (code != 0) {
            log.error("获取代理ip错误." + jsonObject.getString("msg"));
        }

        JSONObject data = jsonObject.getJSONObject("data");
        JSONArray proxy_list = data.getJSONArray("proxy_list");

        ConcurrentLinkedQueue<HttpHost> _hosts = new ConcurrentLinkedQueue<HttpHost>();

        for (Object o : proxy_list) {
            String s = o.toString();
            String[] sArr = s.split(":");
            HttpHost httpHost = new HttpHost(sArr[0], Integer.parseInt(sArr[1]));
            _hosts.add(httpHost);
        }

        hosts = _hosts;
    }
    
    public static void newInitPools() {
        WS.WSRequest url = WS.url("http://www.kuaidaili.com/api/getproxy")
                .setParameter("orderid", "948897688929041")
                .setParameter("num", "200")
                .setParameter("quality", "1")
                .setParameter("sort", "1")
                .setParameter("format", "json");

        String string = url.get().getString();

        JSONObject jsonObject = JSON.parseObject(string);
        int code = jsonObject.getIntValue("code");
        if (code != 0) {
            log.error("获取代理ip错误." + jsonObject.getString("msg"));
        }

        JSONObject data = jsonObject.getJSONObject("data");
        JSONArray proxy_list = data.getJSONArray("proxy_list");

        ConcurrentLinkedQueue<HttpHost> _hosts = new ConcurrentLinkedQueue<HttpHost>();

        for (Object o : proxy_list) {
            String s = o.toString();
            String[] sArr = s.split(":");
            HttpHost httpHost = new HttpHost(sArr[0], Integer.parseInt(sArr[1]));
            _hosts.add(httpHost);
        }

        hosts = _hosts;
    }
    
    public static ConcurrentLinkedQueue<HttpHost> getHosts() {
    	return hosts;
    }
    
    public static HttphostWrapper getHttphostWrapper() {
        int i = 0;
        while (hosts.isEmpty() && ++i < 10) {
            initPools();
        }

        if (hosts.isEmpty()) {
            log.error("初始化代理服务器失败!!!");
            return null;
        }

        return new HttphostWrapper(hosts.peek(), System.currentTimeMillis());
    }

    public static HttpHost getHost() {
        int i = 0;
        while (hosts.isEmpty() && ++i < 10) {
            initPools();
        }

        if (hosts.isEmpty()) {
            log.error("初始化代理服务器失败!!!");
            return null;
        }

        return hosts.peek();
    }

    public static void fuckOneHost() {
        hosts.poll();
    }

    public static String proxyGet(String url, String referer) {
        return proxyGet(url, referer, null, 0, 0, null);
    }
    
    public static String proxyGet(String url, String referer, String userAgent) {
        return proxyGet(url, referer, null, 0, 0, userAgent);
    }
    
    public static String proxyGet(String url, String referer, String cookies, int timeout, int retryTime) {
        return proxyGet(url, referer, null, 0, 0, null);
    }

    public static String proxyGet(String url, String referer, String cookies, int timeout, int retryTime, String userAgent) {
        if(timeout < 20000) {
            timeout = 20000;
        }

        if (retryTime < 10) {
            retryTime = 10;
        }
        
        if (userAgent == null) {
        	userAgent = NewProxyTools.DEFAULT_UA;
        }

        HttpClient httpclient = new DefaultHttpClient();

        HttpConnectionParams.setSoTimeout(httpclient.getParams(), timeout);
        HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), timeout);
        httpclient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
        httpclient.getParams().setParameter(ClientPNames.MAX_REDIRECTS, 20);

        HttpGet httpGet = new HttpGet(url);
        if (referer != null) {
            httpGet.addHeader("Referer", referer);
        }
        if (cookies != null) {
            httpGet.addHeader("Cookie", cookies);
        }

        httpGet.addHeader("User-Agent", userAgent);

        for (int i = 0; i < retryTime; i++) {
            HttpHost host = getHost();
            if (host == null) {
                return null;
            }

            httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, host);

            String content = getContent(httpclient, httpGet);
            if (content == null){
                hosts.poll();
            } else {
                return content;
            }
        }
        return null;
    }

    private static String getContent(HttpClient httpclient, HttpGet httpGet) {
        HttpResponse rsp;

        try {
            rsp = httpclient.execute(httpGet);
            HttpEntity entity = rsp.getEntity();
            String content = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            return content;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }
    
    public static String getContent(HttpClient httpclient, HttpGet httpGet, String code) {
        HttpResponse rsp;
        if(StringUtils.isEmpty(code)) {
        	code = "UTF-8";
        }
        try {
            rsp = httpclient.execute(httpGet);
            HttpEntity entity = rsp.getEntity();
            String content = EntityUtils.toString(entity, code);
            EntityUtils.consume(entity);
            return content;
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }
    
}
