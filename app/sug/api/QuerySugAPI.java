
package sug.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import proxy.HttphostWrapper;
import proxy.NewProxyTools;
import spider.mainsearch.MainSearchApi;
import utils.NewProxyToolsUtils;

import com.ciaosir.client.api.API;
import com.ciaosir.client.pool.HttpClientFactory;
import com.ciaosir.client.utils.NumberUtil;

/**
 * Perhaps, some day we will come back....
 * @created 2012-10-2 下午5:12:19
 */

public class QuerySugAPI {

    // 数据请求url
    public static final String REQUEST_URL = "http://suggest.taobao.com/sug?area=c2c&code=utf-8&callback=KISSY.Suggest.callback&q=";

    public final static String REFER_URL = "http://www.taobao.com/";

    public static ObjectPool<HttpClient> clientPool = new StackObjectPool<HttpClient>(new HttpClientFactory());

    private static final Logger log = LoggerFactory.getLogger(QuerySugAPI.class);

    private static final int RETRY_LIMIT = 3;

    public static List<String> getQuerySugListSimple(String keyword) {
        String content = null;
        String url = REQUEST_URL + URLEncoder.encode(keyword);
        long millis = System.currentTimeMillis();
//        if(millis % 5 > 3){
//            content = API.directGet(url, REFER_URL, null);
//        }else{
//            
        for(int i = 0; i < 3; i++) {
        	 HttphostWrapper wrapper = NewProxyTools.getHttphostWrapper();
	        if(wrapper == null) {
	        	continue;
	        }
	        content = API.directGet(url, REFER_URL, null);
	        if(StringUtils.isEmpty(content) == false && content.equals("KISSY.Suggest.callback({\"status\":1111,\"wait\":5})") == false) {
	        	break;
	        }
        }
        if(StringUtils.isEmpty(content) == true || content.equals("KISSY.Suggest.callback({\"status\":1111,\"wait\":5})") == true) {
        	return null;
        }
//        }
        return readJsonResult(content);
    }

	public static Map<String, Integer> getQuerySugListWordCount(String keyword) {
		String content = StringUtils.EMPTY;
		String url = REQUEST_URL + URLEncoder.encode(keyword);
//		content = SimpleHttpRetryUtil.retryGetWebContent(url, REFER_URL, null, 3);
		
		HttpHost wrapper = NewProxyTools.getHost();
		if (wrapper != null) {
			content = MainSearchApi.directGet(url, REFER_URL, null, wrapper, null);
		}
		
		if ((StringUtils.isEmpty(content) 
				|| content.indexOf("{\"status\":1111,\"wait\":5}") > 0
				|| content.indexOf("KISSY.Suggest.callback(") < 0)) {
			content = NewProxyToolsUtils.proxyGet(url, REFER_URL);
		}
		
		return readWordCount(content);
	}

    public static List<String> getQuerySugList(String keyword, boolean use_proxy) {
        List<String> list = new ArrayList<String>();

        HttpClient httpclient = null;

        HttpResponse httpResponse = null;
        String proxyip = null;
        try {
            httpclient = clientPool.borrowObject();

//            if (use_proxy) {
//                proxyip = BusProxyPool.getProxy();
//                if (proxyip != null) {
//                    String[] ps = proxyip.split(":");
//                    HttpHost proxy = new HttpHost(ps[0], Integer.parseInt(ps[1]), "http");
//                    httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
//                }
//            }

            HttpGet httpRequest = new HttpGet(REQUEST_URL + URLEncoder.encode(keyword, "UTF-8"));
            httpRequest.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)");
            httpRequest.addHeader("Refer", REFER_URL);
            httpResponse = httpclient.execute(httpRequest);

            int retry = 0;
            while ((httpResponse == null || httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
                    && ++retry < RETRY_LIMIT) {
                // 取得返回的字符串
                Thread.sleep(500);
                httpResponse = httpclient.execute(httpRequest);
            }

            if (httpResponse == null) {
                return null;
            }
            HttpEntity entity = httpResponse.getEntity();
            if (entity == null) {
                return null;
            }
            String content = EntityUtils.toString(entity);

            list = readJsonResult(content);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        } finally {
            // return Proxy
//            if (use_proxy) {
//                BusProxyPool.returnProxy(proxyip);
//            }
            returnClientQuietly(httpclient);
        }

        return list;
    }

    private static Map<String, Integer> readWordCount(String content) {
        Map<String, Integer> list = null;
        if(StringUtils.isEmpty(content) 
        		|| content.indexOf("{\"status\":1111,\"wait\":5}") > 0
        		|| content.indexOf("KISSY.Suggest.callback(") < 0) {
        	return list;
        }
        try {
        	content = StringUtils.trim(content);
        	list = new HashMap<String, Integer>();
            content = content.substring("KISSY.Suggest.callback(".length(), content.length() - 1);
            JSONObject jsonObj = new JSONObject(content);
            JSONArray array = jsonObj.getJSONArray("result");
//            list = new ArrayList<String>();
            log.info("[arr:]" + array.toString());
            for (int i = 0; i < array.length(); i++) {
                JSONArray one = array.getJSONArray(i);
                String keyword = (String) one.get(0);
                int count = NumberUtil.parserInt(one.get(1), 0);
//                log.info("[keyword :]" + count);
                list.put(keyword, count);
            }
            return list;
        } catch (ParseException e) {
            log.warn(e.getMessage(), e);
            return MapUtils.EMPTY_MAP;
        } catch (JSONException e) {
        	log.error(content);
            log.warn(e.getMessage(), e);
            return MapUtils.EMPTY_MAP;
        }

    }

    private static List<String> readJsonResult(String content) {

        List<String> list = null;
        try {
            content = content.substring("KISSY.Suggest.callback(".length(), content.length() - 2);
            JSONObject jsonObj = new JSONObject(content);
            JSONArray array = jsonObj.getJSONArray("result");
            list = new ArrayList<String>();
            for (int i = 0; i < array.length(); i++) {
                JSONArray one = array.getJSONArray(i);
                String keyword = (String) one.get(0);
                list.add(keyword);
            }
        } catch (ParseException e) {
            log.warn(e.getMessage(), e);
            return null;
        } catch (JSONException e) {
            log.warn(e.getMessage(), e);
            return null;
        }

        return list;
    }

    public static String changeCharset(String str, String oldCharset, String newCharset)
            throws UnsupportedEncodingException {
        if (str == null) {
            return null;
        }
        // 用源字符编码解码字符串
        byte[] bs = str.getBytes(oldCharset);
        return new String(bs, newCharset);
    }

    public static void returnClientQuietly(HttpClient client) {
        if (client == null) {
            return;
        }

        try {
            clientPool.returnObject(client);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

}
