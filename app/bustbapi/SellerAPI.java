
package bustbapi;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.API;
import com.ciaosir.client.api.API.PYSpiderOption;
import com.ciaosir.client.api.SimpleHttpApi.WebContentApi;
import com.ciaosir.client.pojo.ItemContainer;
import com.ciaosir.client.pojo.ItemThumb;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.SimpleHttpRetryUtil;
import com.ciaosir.commons.ClientException;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.util.Cookie;

/**
 * 
 *
 */
public class SellerAPI {

    private static final Logger log = LoggerFactory.getLogger(SellerAPI.class);

    public static final String TAG = "SellerAPI";

    private static String getJsonFromTB(String url, HashMap<String, String> queryParams, HashMap<String, String> headers)
            throws Exception {
        return getJsonFromTB(url, queryParams, headers, null);
    }

    private static String getJsonFromTB(String url, HashMap<String, String> queryParams,
            final HashMap<String, String> headers, HttpHost proxy) throws Exception {
        DefaultHttpClient httpclient = new DefaultHttpClient();

        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(url);

        Iterator<Entry<String, String>> iter = queryParams.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
            String key = entry.getKey();
            String val = entry.getValue();
            builder.setParameter(key, val);
        }
        URI uri = builder.build();

        // Set the proxy host
        if (proxy != null) {
            httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
        HttpParams params = httpclient.getParams();
        HttpConnectionParams.setSoTimeout(params, 10000);
        HttpConnectionParams.setConnectionTimeout(params, 10000);

        if (headers != null && headers.size() > 0) {
            httpclient.addRequestInterceptor(new HttpRequestInterceptor() {
                public void process(final HttpRequest request, final HttpContext context) throws HttpException,
                        IOException {
                    Iterator<Entry<String, String>> iter = headers.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
                        String key = entry.getKey();
                        String val = entry.getValue();
                        request.addHeader(key, val);
                    }

                }
            });
        }

        HttpGet httpget = new HttpGet(uri);

        HttpResponse response = httpclient.execute(httpget);
        // Now start to get the response content
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            String jsonString = EntityUtils.toString(entity, Charset.forName("gbk"));
            jsonString.replaceAll("\r\n", "");
            return jsonString;
        } else {
            throw new IllegalStateException("The response from undocumented api  " + uri + " is BLANK!");
        }
    }

    private static final int VerRedoTime = 10;

    /**
     * 拖卖家 宝贝 数据
     * http://list.taobao.com/itemlist/default.htm?cat=0&sort=biz30day&viewIndex=1&as=1&ver=pts3&commend=all&atype=b&&style=list&same_info=1&tid=0&isnew=2&json=on&tid=0&data-key=s&data-value=96&module=page&&_input_charset=utf-8&json=on&_ksTS=1362713476510_420&callback=jsonp421
     * http://list.taobao.com/itemlist/default.htm?cat=0&sort=biz30day&as=0&commend=all&atype=b&style=list&tid=0&isnew=2&json=on&callback=jsonp412&data-key=s&module=page&&_input_charset=utf-8&viewIndex=1&_ksTS=1362713434851_411&data-value=96
     * http://list.taobao.com/itemlist/default.htm?cat=0&sort=biz30day&viewIndex=1&as=1&commend=all&atype=b&style=list&nick=%E4%BA%BA%E6%9C%AC%E9%9E%8B%E7%B1%BB%E6%97%97%E8%88%B0%E5%BA%97&same_info=1&isnew=2&json=on&tid=0&data-key=s&data-value=96&module=page&&_input_charset=utf-8&json=on&_ksTS=1369586935724_412&callback=jsonp413
     * @param nick
     * @param q
     * @param pageIndex
     * @param option
     * @return
     * @throws ClientException
     */
    private static ItemContainer getItemPageJson(String nick, String q, int pageIndex, PYSpiderOption option,
            String redirUrl, int redoTime)
            throws ClientException {

        String url = null;
        boolean isRelaceHtmlNeeded = false;
        String referer = null;
        ///itemlist/default.htm?cat=0&sort=coefp&as=0&commend=all&atype=b&style=list&tid=0&isnew=2&_input_charset=utf-8&json=on&callback=jsonp212&data-key=s&module=page&viewIndex=1&data-value=0&nick=yzxmiker
        ///itemlist/default.htm?cat=0&sort=coefp&viewIndex=1&as=0&ver=pts3&commend=all&atype=b&nick=yzxmiker&style=list&isnew=2&_input_charset=utf-8

        //http://list.taobao.com/itemlist/default.htm?cat=0&sort=coefp&viewIndex=1&as=0&ver=pts3&commend=all&atype=b&style=list&nick=%E7%98%A6%E6%B1%A4%E5%9C%86bb&same_info=1&isnew=2&json=on&tid=0&data-key=s&data-value=0&module=page&&_input_charset=utf-8&json=on&_ksTS=1362152148161_421&callback=jsonp422

        StringBuilder sb = null;
        String sort = StringUtils.EMPTY;
        if (option.getSort() != null) {
            sort = "&sort=" + option.getSort();
        } else {
        }

        String ver = "probeta3";
        String newVer = getVerFromUrl(redirUrl);
        if (!StringUtils.isEmpty(newVer))
            ver = newVer;

        //:http://list.taobao.com/itemlist/default.htm?atype=b&_input_charset=utf-8&cat=0&sort=biz30day&style=list&as=0&viewIndex=1&&isnew=2&commend=all&_input_charset=utf-8&json=on&_ksTS=1369587035931_212&callback=jsonp213&nick=人本鞋类旗舰店
        if (pageIndex > 1) {
            sb = new StringBuilder("http://list.taobao.com/itemlist/default.htm?cat=0" + sort
                    //                    + "&viewIndex=1&as=1&ver=" + ver
                    + "&viewIndex=1&as=1"
                    + "&commend=all&&style=list&same_info=1&tid=0&isnew=2&json=on&tid=0"
                    + "&callback=jsonp412&data-key=s&module=page&_input_charset=utf-8&viewIndex=1&_ksTS="
                    + System.currentTimeMillis() + "_411");
            sb.append("&data-value=" + ((pageIndex - 1) * API.DEFAULT_PAGE_SIZE_FOR_LIST_TAOBAO));
            sb.append("&s=" + ((pageIndex - 1) * API.DEFAULT_PAGE_SIZE_FOR_LIST_TAOBAO));
        } else {
            sb = new StringBuilder("http://list.taobao.com/itemlist/default.htm?atype=b&_input_charset=utf-8&cat=0"
                    //                    + sort + "&style=list&as=0&viewIndex=1&ver=" + ver
                    + sort + "&style=list&as=0&viewIndex=1"
                    + "&isnew=2&commend=all&_input_charset=utf-8&json=on&_ksTS=" + System.currentTimeMillis()
                    + "_212&callback=jsonp213");
        }

        if (StringUtils.isBlank(nick)) {
            sb.append("&q=" + EncodingUtils.getString(q.getBytes(), "UTF-8"));
            referer = "http://list.taobao.com/itemlist/default.htm#!atype=b&cat=0&style=list&as=0&viewIndex=1&isnew=2&json=on&tid=0";
            isRelaceHtmlNeeded = true;
        } else {
            sb.append("&nick=" + EncodingUtils.getString(nick.getBytes(), "UTF-8"));
            if (!StringUtils.isEmpty(redirUrl)) {
                referer = redirUrl;
            } else {
                referer = "http://list.taobao.com/browse/ad_search.htm";
            }
        }

//        String cookie = genListTaobaoCookie();
        String cookie = null;
//        log.error("cookie :" + cookie);

        String rawTaobaoResp = null;
        WebContentApi sApi = null;
        url = sb.toString();

        if (option.isUseSimpleHttp()) {
            log.info("[simplehttp] " + url);
            //log.error("retryTimes:" + option.getRetryTime());
            //重试retryTime次
            rawTaobaoResp = SimpleHttpRetryUtil.retryGetWebContent(url, referer, cookie, option.getRetryTime());
            //sApi = new SimpleHttpApi.WebContentApi(url, referer, API.DEFAULT_UA);
            //rawTaobaoResp = sApi.execute();
        } else {
            log.info("[direct] " + url);
            rawTaobaoResp = API.directGet(url, referer, API.DEFAULT_UA, null, cookie);
        }

        if (isRelaceHtmlNeeded) {
            rawTaobaoResp = ItemThumb.replaceHtml(rawTaobaoResp);
        }

        boolean parseFinalJson = true;
        // Store the pagination data
        ItemContainer container = new ItemContainer();
        container.setPageIndex(pageIndex);

        // Transform the JSON String to ItemContainer Bean

        JsonNode rootNode = JsonUtil.parserJSONP(rawTaobaoResp);
        JsonNode status=null;
        if(rootNode != null){
            status = rootNode.get("status");
        }
        if (status != null) {
            JsonNode code = status.get("code");
            if (code != null && code.getIntValue() == 302) {
                String newRedirUrl = "http://list.taobao.com" + status.get("url").getTextValue();
                log.info("new redir url :" + newRedirUrl + " with resp:" + rootNode);
                //rawTaobaoResp = new SimpleHttpApi.WebContentApi(redirUrl, referer, API.DEFAULT_UA).execute();
//                if (rawTaobaoResp == null || rawTaobaoResp.isEmpty()) {
                parseFinalJson = false;
//                } else {
                //rootNode = JsonUtil.parserJSONP(rawTaobaoResp);
                //if (rootNode == null || rootNode.isMissingNode()) {
                //    parseFinalJson = false;
                //log.error("current conrtent :" + rawTaobaoResp);
                //}

                //新增的，动态获取ver
//                log.info("[new ver:]" + newVer);
                if (redoTime < VerRedoTime) {
                    redoTime++;
                    return getItemPageJson(nick, q, pageIndex, option, newRedirUrl, redoTime);
                } else {
                    return container;
                }

//                }
            }
        }

//        log.info("[root node:]" + rootNode);
        if (parseFinalJson) {
            // ---------------------------Item Info List------------------------------------------
            JsonNode itemList = rootNode.get("itemList");
            if (itemList == null) {
                log.error("for no item list current url :" + url + " and current body : and simple host:"
                        + ((sApi != null) ? sApi.getHost() : null) + " \n :" + rootNode);
                return container;
            }

            for (JsonNode node : itemList) {
                int tradeNum = Integer.parseInt(node.path("tradeNum").getTextValue());
                // Step out WHEN the good hasn't sold well
                // TODO, actually, there might be more items having trades number > 0 
                if (tradeNum < 0 && option.isMustHasTrade()) {
                    break;
                }

                ItemThumb item = ItemThumb.parseItemThumFromItemList(isRelaceHtmlNeeded, node, tradeNum);

                container.addItem(item);
            }
            // --------------------------Page Info------------------------------------------
            JsonNode pageInfo = rootNode.get("page");
            /*
             * The total page count,also the number of loop,
                 But the max PARAMETER is also bottle neck
                 WARINing: when the data goes into the last page,the value of "page" Key in the JSON String is NULL!
             */

            int pageCount = 0;
            int pageSize = 0;
            if (pageInfo.isContainerNode()) {
                pageCount = Integer.parseInt(pageInfo.path("totalPage").getTextValue());
                pageSize = Integer.parseInt(pageInfo.path("pageSize").getTextValue());
                container.setLastPage(false);
            } else {
                pageSize = container.getItems().size();
                container.setLastPage(true);
            }

            container.setPageCount(pageCount);
            container.setPageSize(pageSize);
        } else {
            doForContent(container, rawTaobaoResp);
        }

        return container;
    }

    //从redirect的url中，获取正确的ver
    private static String getVerFromUrl(String newUrl) {
        String ver = "";
        int start = newUrl.indexOf("&ver=");
        if (start <= 0)
            return "";
        start = start + "&ver=".length();
        int end = newUrl.indexOf("&", start);

        if (end <= 0)
            ver = newUrl.substring(start);
        else
            ver = newUrl.substring(start, end);

//        log.error("newVer:" + ver + " ----------------------");
        return ver;
    }

    static String LIST_TAOBAO_COOKIE = null;

    public static int COOKIE_USED_TIME = 0;

    public synchronized static String genListTaobaoCookie() {
        if (LIST_TAOBAO_COOKIE != null && COOKIE_USED_TIME < 500) {
            COOKIE_USED_TIME++;
            return LIST_TAOBAO_COOKIE;
        }
//        String cookie = null;
        WebClient webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER_7);
        webClient.setThrowExceptionOnFailingStatusCode(false);
        webClient.setThrowExceptionOnScriptError(false);
        webClient.setCssEnabled(false);
        webClient.setRedirectEnabled(true);

        webClient.setJavaScriptEnabled(true);
        webClient.setTimeout(20000);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());

        webClient.waitForBackgroundJavaScript(5000L);
        webClient.waitForBackgroundJavaScriptStartingBefore(5000L);
//        WebClient client = WebClientFactory.genWebClient(true);
        try {
            Page page = webClient.getPage("http://list.taobao.com/itemlist/default.htm");
//            PlayUtil.sleepQuietly(8000L);s
            List<String> parts = new ArrayList<String>();
            Set<Cookie> cookies = webClient.getCookieManager().getCookies();
            log.info("[raw cookies:]" + LIST_TAOBAO_COOKIE);
            for (Cookie cookie2 : cookies) {
                parts.add(cookie2.getName() + "=" + cookie2.getValue());
            }

            LIST_TAOBAO_COOKIE = StringUtils.join(parts, ";");
            log.error("generate cookie :" + LIST_TAOBAO_COOKIE);
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        } finally {
            webClient.closeAllWindows();
        }
        COOKIE_USED_TIME = 0;
        return LIST_TAOBAO_COOKIE;
    }

    public static void doForContent(ItemContainer container, String rawTaobaoResp) {
//        // TODO Auto-generated method stub
//        Document html = Jsoup.parse(rawTaobaoResp);
//        Elements itemHtmls = html.select("li.list-item");
//        for (Element itemHtml : itemHtmls) {
//            long numIid=  NumberUtil.parserLong(itemHtml.attr(""), defaultValue)
//        }
    }

    /**
     * 根据商家昵称获得店铺的宝贝信息
     * 
     * @param nick
     *            卖家昵称
     * @param max
     *            获取最大宝贝数
     * @return 宝贝信息列表
     * @throws Exception
     */
    public static List<ItemThumb> getItemArray(String nick, String q, int max, HttpHost proxy, boolean mustHasTrade)
            throws Exception {
        return getItemArray(nick, q, new PYSpiderOption(proxy, false, 1, mustHasTrade, max));
    }

    public static List<ItemThumb> getItemArray(String nick, String q, PYSpiderOption option) throws Exception {
        if (option.getMaxRecord() < 1) {
            throw new IllegalArgumentException("");
        }

        List<ItemThumb> itemArray = new ArrayList<ItemThumb>();
        int currentVisitedItemNum = 0;

        int index = 1;
        do {

            ItemContainer pageResult = getItemPageJson(nick, q, index, option, "", 0);

            if (CommonUtils.isEmpty(pageResult.getItems())) {
                break;
            }

            for (ItemThumb item : pageResult.getItems()) {
                if (currentVisitedItemNum >= option.getMaxRecord()) {
                    break;
                }
                itemArray.add(item);
                currentVisitedItemNum++;
            }

            if (CollectionUtils.size(pageResult.getItems()) < API.DEFAULT_PAGE_SIZE_FOR_LIST_TAOBAO) {
                break;
            }
            if (currentVisitedItemNum >= option.getMaxRecord()) {
                break;
            }

            index++;
        } while (true);

        log.info("itemArray size: " + itemArray.size() + "   nick: " + nick);
        return itemArray;
    }

    /**
     * 根据宝贝ID获取宝贝的特定信息
     * @param itemId
     * @param propName
     * @return
     * @throws Exception 
     */
    public static String getItemSellCount(String itemId) throws Exception {
        // The necessary parameter for URL request
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("abt", "");
        parameters.put("campaignId", "");
        parameters.put("cartEnable", "true");
        parameters.put("cat_id", "");
        parameters.put("deliveryOption", "8");
        parameters.put("household", "false");
        parameters.put("ip", "");
        parameters.put("isApparel", "true");
        parameters.put("isAreaSell", "false");
        parameters.put("isForbidBuyItem", "false");
        parameters.put("isIFC", "false");
        parameters.put("isMeizTry", "false");
        parameters.put("isSecKill", "false");
        parameters.put("isSpu", "false");
        parameters.put("isUseInventoryCenter", "true");
        parameters.put("isWrtTag", "false");
        parameters.put("itemId", itemId);
        parameters.put("itemWeight", "0");
        parameters.put("key", "");
        parameters.put("notAllowOriginPrice", "false");
        parameters.put("q", "");
        parameters.put("ref", "");
        parameters.put("service3C", "false");
        parameters.put("tgTag", "false");
        parameters.put("tmallBuySupport", "true");
        parameters.put("trialErrNum", "0");
        parameters.put("u_channel", "");
        parameters.put("ump", "true");

        // The necessary header info for URL request
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Cookie",
                "cna=SmlCCcJ7/hQCAeG8ujwO7/g0; t=e5b596f8d5a0fb532b610f06a35dac25; tg=0; _cc_=VT5L2FSpdA%3D%3D; mt=ci=0_0;v=0;");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.2; rv:16.0) Gecko/20100101 Firefox/16.0");
        headers.put("Referer", "http://detail.tmall.com/item.htm?id=" + itemId);

        String jsonResult = getJsonFromTB("mdskip.taobao.com/core/initItemDetail.htm", parameters, headers);

        //System.out.print(jsonResult);

        // The JSON String is not precisely valid,the JSON parse will throw a IO exception

        int sellCountPos = jsonResult.indexOf("\"sellCount\":");
        String numStr = jsonResult.substring(sellCountPos + "\"sellCount\":".length());
        int firstSym = numStr.indexOf("}");
        String result = numStr.substring(0, firstSym);

        return result;
    }
    /**
     * 接口。。
             http://list.taobao.com/itemlist/default.htm?cat=0&sort=coefp&viewIndex=1&as=0&commend=all&atype=b&s=48&style=list&q=%E7%9C%9F%E7%88%B1&tid=0&isnew=2&json=on&tid=0&data-key=s&data-value=96&module=page&&_input_charset=utf-8&json=on&_ksTS=1355479989930_1437&callback=jsonp1438
     */

}
