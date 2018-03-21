/**
 * 
 */

package bustbapi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.MissingNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.api.API;
import com.ciaosir.client.api.API.PYSpiderOption;
import com.ciaosir.client.api.SimpleHttpApi;
import com.ciaosir.client.item.ShopInfo;
import com.ciaosir.client.pool.HttpClientFactory;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;

/**
 * @author navins
 * @date 2012-12-14 下午8:40:55
 */
public class ShopSearchAPI {
    private static final Logger log = LoggerFactory.getLogger(ShopSearchAPI.class);

    //public static final String REQ_URL = "http://shopsearch.taobao.com/interfaces/shoplist_proxy.php?sort=sale-desc&q=";
    public static final String REQ_URL = "http://shopsearch.taobao.com/interfaces/shoplist_proxy.php?q=";
    
//    public static final String REQ_URL = "http://shopsearch.taobao.com/interfaces/shoplist_proxy.php?sort=credit-desc&q=";

    public static final String DEFAULT_REFER = "http://shopsearch.taobao.com/search?v=shopsearch&q=";

    public static ObjectPool<HttpClient> clientPool = new StackObjectPool<HttpClient>(new HttpClientFactory());

    public static ShopInfo getShopInfo(String shopnick, int timeout) {
        return getShopInfo(shopnick, timeout, 1);
    }

    public static ShopInfo getShopInfo(String shopnick, int timeout, int retry) {

        while (retry-- > 0) {
            List<ShopInfo> info = getShopInfo(shopnick, new PYSpiderOption(false, retry));
            log.info("[" + retry + "][result :]" + info);
            if (info != null) {
                return NumberUtil.first(info);
            }
        }

        return null;
    }

    public static List<ShopInfo> getShopInfo(String query, PYSpiderOption opt) {
        return getShopInfo(query, null, opt);
    }

    public static List<ShopInfo> getShopInfo(String query, String cookie, PYSpiderOption opt) {
        List<ShopInfo> result = null;

        try {
            String url = REQ_URL + URLEncoder.encode(query, "GBK");

            log.info("[url :]" + url);
            String content = null;
            if (opt.isUseSimpleHttp()) {
                content = new SimpleHttpApi.JsonContentApi(url, DEFAULT_REFER, null, null).call().toString();
            } else {
                content = API.directGet(url, DEFAULT_REFER, null);
            }

            result = parseContent(query, content);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
        } catch (IllegalStateException e) {
            log.warn(e.getMessage());
        } catch (Exception e) {
            log.warn(e.getMessage());
        } finally {
        }

        return result;
    }

    public static List<ShopInfo> parseContent(String shopnick, String content) throws IOException {
        List<ShopInfo> res = new ArrayList<ShopInfo>();
        try {
            if (StringUtils.isEmpty(content)) {
                return null;
            }

            JsonNode jsonNode = MissingNode.getInstance();
            jsonNode = JsonUtil.mapper.readValue(content, JsonNode.class);
            JsonNode body = jsonNode.findValue("body");
            if (body.findValues("totalsold").size() == 0) {
                return ListUtils.EMPTY_LIST;
            }

            Iterator<JsonNode> it = body.iterator();
            while (it.hasNext()) {
                JsonNode next = it.next();
                int itemCount = NumberUtil.parserInt(next.findValues("procnt").get(0).getTextValue().trim(), 0);
                int latestTradeCount = NumberUtil.parserInt(next.findValues("totalsold").get(0).getTextValue().trim(),
                        0);
                int level = NumberUtil.parserInt(next.findValues("ratesum").get(0).getTextValue().trim(), 0);
                int renqi = NumberUtil.parserInt(next.findValues("renqi").get(0).getTextValue().trim(), 0);
                int quality = NumberUtil.parserInt(next.findValues("quality").get(0).getTextValue().trim(), 0);
                long userId = NumberUtil.parserLong(next.findValues("uid").get(0).getTextValue().trim(), 0L);
                String picPath = StringUtils.trim(next.findValues("pict_url").get(0).getTextValue());

//            log.info("[bshop]" + next.findValues("bshop_type").get(0).getTextValue().trim());
                int bShopType = NumberUtil.parserInt(next.findValues("bshop_type").get(0).getTextValue().trim(), 0);
                boolean isBShop = bShopType != 0;
                String wangwang = next.findValues("nick").get(0).getTextValue().trim();
                long shopId = NumberUtil.parserLong(next.findValues("nid").get(0).getTextValue().trim(), 0L);

                ShopInfo shop = new ShopInfo(userId, wangwang, level, itemCount, latestTradeCount, renqi, quality,
                        isBShop);
                shop.setPicPath(picPath);
                shop.setShopId(shopId);
//                log.error("[shop : ]" + shop);
                res.add(shop);
            }
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            log.error("error content : " + content);
            throw e;
        }

        return res;

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

    public static ShopInfo getShopNick(String url, HttpHost httphost, int timeout) {
        ShopInfo result = null;
        HttpClient httpclient = null;

        HttpResponse rsp = null;

        try {
            httpclient = clientPool.borrowObject();
            HttpConnectionParams.setSoTimeout(httpclient.getParams(), timeout);
            HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), timeout);
            HttpGet httpget = new HttpGet(url);
            httpget.addHeader("Referer", DEFAULT_REFER);

            if (httphost == null) {
                rsp = httpclient.execute(httpget);
            } else {
                rsp = httpclient.execute(httphost, httpget);
            }
            if (rsp == null || rsp.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                return null;
            }
            HttpEntity entity = rsp.getEntity();

            if (entity != null) {
                String content = EntityUtils.toString(entity);

                result = parseHtml(content);
            }
            EntityUtils.consume(entity);
        } catch (NoSuchElementException e) {
            log.warn(e.getMessage());
        } catch (IllegalStateException e) {
            log.warn(e.getMessage());
        } catch (Exception e) {
            log.warn(e.getMessage());
        } finally {
            returnClientQuietly(httpclient);
        }

        return result;
    }

    private static ShopInfo parseHtml(String content) throws JsonParseException, JsonMappingException, IOException {
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        content = content.substring(content.indexOf("shop_config = ") + 14);
        content = content.substring(0, content.indexOf('}') + 1) + "}";

        JsonNode jsonNode = MissingNode.getInstance();
        jsonNode = JsonUtil.mapper.readValue(content, JsonNode.class);
        long shopId = jsonNode.findValue("shopId").getValueAsLong();
        long userId = jsonNode.findValue("userId").getValueAsLong();
        String shopnick = URLDecoder.decode(jsonNode.findValue("user_nick").getTextValue(), "UTF-8");
        ShopInfo info = new ShopInfo(shopnick, userId, shopId);
        return info;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        HttpClient httpclient = null;

        HttpResponse rsp = null;
        String url = "http://shopsearch.taobao.com/interfaces/shoplist_proxy.php?sort=sale-desc&q=" + URLEncoder.encode("丁丽仙", "utf-8");

        try {
            httpclient = new DefaultHttpClient();
            HttpConnectionParams.setSoTimeout(httpclient.getParams(), 50000);
            HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 50000);
            httpclient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
            httpclient.getParams().setParameter(ClientPNames.MAX_REDIRECTS, 20);
            HttpGet httpGet = new HttpGet(url);

//            httpGet.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)");

            rsp = httpclient.execute(httpGet);
            HttpEntity entity = rsp.getEntity();
//            String content = EntityUtils.toString(entity);
            JsonNode content = JsonUtil.readJsonResult(rsp);
            EntityUtils.consume(entity);
//            return content;
            System.out.println(content);
        } catch (Exception e) {
//            log.warn(e.getMessage(),e);
            log.warn(e.getMessage());
            if (e.getMessage().contains("refused")) {
                throw new RuntimeException("");
            }
        }

    }
}
