
package spider.mainsearch;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import models.user.UserIdNick;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.http.Header;
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
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.Model;
import spider.ItemThumbSecond;
import utils.CollectInfoByWebpage;
import actions.CallableThreadOfAgent;
import actions.listTaoBao.TBUrlManager;
import bustbapi.ItemPageApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.api.API;
import com.ciaosir.client.pojo.ItemThumb;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.client.utils.SimpleHttpRetryUtil.WebContentSimpleApi;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;

import configs.TMConfigs;

/**
 * @author zrb
 *         http://ju.atpanel.com/?url=http://s.taobao.com/search?source=top_search&q=%C8%C8%CF%FA%BF%EE%CD%B8%C3%F7%C4
 *         %CC
 *         %D7%EC&pspuid=138607&v=product&p=detail&stp=top.toplist.tr_zb.sellhot.image.5.0&ad_id=&am_id=&cm_id=&pm_id=
 *         1500223547e594398041
 */
public class MainSearchApi {
    
    private static final Logger log = LoggerFactory.getLogger(MainSearchApi.class);

    public static final String TAG = "TBMainSearchApi";

    public static String ORDER_SALE_DESC = "sale-desc";

    public static final int ITEM_PAGE_SIZE = 40;

//
//    public static TBSearchRes search(String word, int pageNum) {
//        return search(word, pageNum, null);
//    }

    public static class MainSearchOrderType {

        public static final String Zhonghe = "default";//综合

        public static final String Renqi = "renqi-desc";//人气

        public static final String Xiaoliang = "sale-desc";//销量

        public static final String Xinyong = "credit-desc";//信用

        public static final String Zuixin = "old_starts";//最新

        public static final String Jiage = "price-asc";//价格
    }

    @JsonAutoDetect
    public static class MainSearchParams extends Model {
        @JsonProperty
        String word;

        @JsonProperty
        int pageNum = 1;

        @JsonProperty
        String order;

        @JsonProperty
        String minPrice;

        @JsonProperty
        String maxPrice;
        
        @JsonProperty
        String queryArea;

        public MainSearchParams(String word, int pageNum, String order, String minPrice, String maxPrice) {
            super();
            this.word = word;
            this.pageNum = pageNum;
            this.order = order;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;     
        }
        public MainSearchParams(String word, int pageNum, String order, String minPrice, String maxPrice,String queryArea) {
            super();
            this.word = word;
            this.pageNum = pageNum;
            this.order = order;
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
            this.queryArea = queryArea;
        }

        public MainSearchParams() {
            super();
        }

        public MainSearchParams(String word, int pageNum, String order) {
            super();
            this.word = word;
            this.pageNum = pageNum;
            this.order = order;
        }
        
        public MainSearchParams(String word, int pageNum, String order,String queryArea) {
            super();
            this.word = word;
            this.pageNum = pageNum;
            this.order = order;
            this.queryArea = queryArea;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public int getPageNum() {
            return pageNum;
        }

        public void setPageNum(int pageNum) {
            this.pageNum = pageNum;
        }

        public String getQueryArea() {
            return queryArea;
        }

        public void setQueryArea(String queryArea) {
            this.queryArea = queryArea;
        }

        public String getOrder() {
            return order;
        }

        public void setOrder(String order) {
            this.order = order;
        }

        public MainSearchParams(MainSearchParams o) {
            super();
            this.word = o.word;
            this.order = o.order;
            this.minPrice = o.minPrice;
            this.maxPrice = o.maxPrice;
            this.queryArea=o.queryArea;
            this.pageNum = o.pageNum;
        }
        
        public MainSearchParams(MainSearchParams o, int targetPageNum) {
            super();
            this.word = o.word;
            this.pageNum = targetPageNum;
            this.order = o.order;
            this.minPrice = o.minPrice;
            this.maxPrice = o.maxPrice;
            this.queryArea=o.queryArea;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            MainSearchParams other = (MainSearchParams) obj;
            if (maxPrice != other.maxPrice)
                return false;
            if (minPrice != other.minPrice)
                return false;
            if(queryArea != other.queryArea)
                return false;
            if (order == null) {
                if (other.order != null)
                    return false;
            } else if (!order.equals(other.order))
                return false;
            if (pageNum != other.pageNum)
                return false;
            if (word == null) {
                if (other.word != null)
                    return false;
            } else if (!word.equals(other.word))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "MainSearchParams [word=" + word + ", pageNum=" + pageNum + ", order=" + order + ", minPrice="
                    + minPrice + ", maxPrice=" + maxPrice + ",queryArea=" + queryArea+"]";
        }

        public String getMinPrice() {
            return minPrice;
        }

        public void setMinPrice(String minPrice) {
            this.minPrice = minPrice;
        }

        public String getMaxPrice() {
            return maxPrice;
        }

        public void setMaxPrice(String maxPrice) {
            this.maxPrice = maxPrice;
        }

    }
    
    public static String getQueryAreaHost(String queryArea){
        HashMap< String, List<String>> areaMap= new HashMap<String, List<String>>();
        List<String> BJUrls = new ArrayList<String>();
        List<String> SHUrls = new ArrayList<String>();
        List<String> HZUrls = new ArrayList<String>();
            //杭州
            HZUrls.add("bbn27:9092");
            HZUrls.add("bbn28:9092");
            HZUrls.add("bbn32:9092");
            //上海
            SHUrls.add("bbn03:9092");
            SHUrls.add("bbn04:9092");        
            SHUrls.add("bbn06:9092");
            SHUrls.add("bbn08:9092");
            SHUrls.add("bbn09:9002");
           //北京
            BJUrls.add("bbn10:9002");
            BJUrls.add("bbn25:9092");
            BJUrls.add("bbn26:9092");
            BJUrls.add("bbn29:9092");
            BJUrls.add("bbn30:9092");
            BJUrls.add("bbn31:9092");
            
            areaMap.put("北京",BJUrls);
            areaMap.put("上海", SHUrls);
            areaMap.put("杭州", HZUrls);
        
                List<String> hosts = areaMap.get(queryArea);
                int size = hosts.size();
                Random rand = new Random();
                return hosts.get(rand.nextInt(size));
           
    }
    
    /**
     * 
     * @param word
     * @param pageNum
     * @param order
     *            default 综合 renqi-desc 人气 sale-desc 销量 credit-desc 信用 old_starts 最新 price-asc 价格
     * @return
     */
    public static TBSearchRes search(MainSearchParams params) {

        int pageNum = params.getPageNum();
        String word = params.getWord();
        String order = params.getOrder();
        String minPrice = params.getMinPrice();
        String maxPrice = params.getMaxPrice();
        String queryArea=params.getQueryArea();
        
        if (StringUtils.isEmpty(queryArea)) {
            queryArea = "";
        }
       
        DecimalFormat df = new DecimalFormat("######0.00");

        try {
            StringBuilder sb = new StringBuilder("http://s.taobao.com/search?q=");
            sb.append(URLEncoder.encode(word, "utf-8"));

            /**
             * http://s.taobao.com/search?tab=all&source=tbsy&style=grid&filter=reserve_price%5B11.00%2C%5D&fs=0&refpid=420463_1006&q=%C5%AE%B0%FC&filterFineness=2&spm=a230r.1.1997074097.d4917629
             * filter=reserve_price[11.00,]&fs=0&refpid=420463_1006
             * filter=reserve_price%5B30%2C100.00%5D&fs=0&refpid=420463_1006&q=%C5%AE%B0%FC&filterFineness=2
             * filter=reserve_price[30,100.00]&fs=0&refpid=420463_1006&q=女包&filterFineness=2
             */
            if (!StringUtils.isBlank(minPrice) || !StringUtils.isBlank(maxPrice)) {
                sb.append("&filter=reserve_price%5B");
                if (!StringUtils.isBlank(minPrice)) {
                    sb.append(minPrice);
                }
                sb.append("%2C");
                if (!StringUtils.isBlank(maxPrice)) {
                    sb.append(maxPrice);
                }
                sb.append("%5D&fs=0&filterFineness=2");
            }

            sb.append("&ssid=s5-e&&bcoffset=1&search_type=item&tab=all&sourceId=tb.index&style=list&cd=false");
            // sb.append("&bcoffset=1&style=list");
            if (pageNum > 1) {
                sb.append("&s=");
                sb.append(ITEM_PAGE_SIZE * (pageNum - 1));
            } else {
                sb.append("&s=0");
            }

            if (!StringUtils.isEmpty(order)) {
                sb.append("&sort=");
                sb.append(order);
            }

            String url = sb.toString();
//            log.info("[do for url ;]" + url);

            // String cookie = genMainSearchTaobaoCookie();
            String cookie = null;

            TBSearchRes res = new TBSearchRes(word, pageNum, order);
            TBSearchRes finalRes = parseSearchUrl(queryArea,url, "http://www.taobao.com", cookie, res);
//            log.info("[back res:]" + finalRes);
            return finalRes;
        } catch (UnsupportedEncodingException e) {
            log.warn(e.getMessage(), e);
        }

        return null;
    }
    
    //uttp
    public static TBSearchRes search(String host,int pageNum, String order, Long cid){

        StringBuilder sb = new StringBuilder("https://s.taobao.com/search?q=");
        //sb.append(URLEncoder.encode(word, "utf-8"));
        sb.append("&ssid=s5-e&&bcoffset=1&search_type=item&tab=all&sourceId=tb.index&style=list");

        // sb.append("&bcoffset=1&style=list");
        if (pageNum > 1) {
            sb.append("&s=");
            sb.append(ITEM_PAGE_SIZE * (pageNum - 1));
        } else {
            sb.append("&s=0");
        }

        if (!StringUtils.isEmpty(order)) {
            sb.append("&sort=");
            sb.append(order);
        }
        
        sb.append("&cat=");
        sb.append(cid.toString());
        String url = sb.toString();
            // log.error("[do for url ;]" + url);

            // String cookie = genMainSearchTaobaoCookie();
        String cookie = null;

        TBSearchRes res = new TBSearchRes(pageNum, order, cid);
        return parseSearchUrl(host, url, url, cookie, res);
    }

    public static List<PriceRangeLike> searchWordPriceRange(String word) {

        try {
            StringBuilder sb = new StringBuilder("http://s.taobao.com/search?q=");
            sb.append(URLEncoder.encode(word, "utf-8"));
            sb.append("&ssid=s5-e&&bcoffset=1&search_type=item&tab=all&sourceId=tb.index&style=list&cd=false");

            sb.append("&s=0");

            String url = sb.toString();

            String cookie = null;
            //System.out.println("------------------------------------yehuizhang1--------------------------");
            List<PriceRangeLike> likes = simpleParseSearchUrl(url, "http://www.taobao.com", cookie, word);
            return likes;
        } catch (UnsupportedEncodingException e) {
            log.warn(e.getMessage(), e);
        }

        return null;
    }

    public static List<PriceRangeLike> simpleParseSearchUrl(String url, String refer, String cookie, String word) {

        // log.warn("[MainSearch url] " + url);
        int retry = 0;
        String content = StringUtils.EMPTY;
        WebContentSimpleApi api = null;
        while (StringUtils.isEmpty(content) && retry++ < 6) {
            try {        
                 api = new WebContentSimpleApi(url, refer, API.DEFAULT_UA); 
                 content = api.execute();
            } catch (Exception e) {
                log.warn("host err: " + api.getHost() + " retry: " + retry);
                CommonUtils.sleepQuietly(100L);
            }
        }

        if (StringUtils.isEmpty(content)) {
            log.error("still content empty! req url: " + url);
            return null;
        }

        // filterForContent(content);

        try {
            List<PriceRangeLike> likes = simpleParseSearchContentByJsoup(content);
            return likes;
            // parseSearchContentByHtmlUnit(content, res);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static List<PriceRangeLike> simpleParseSearchContentByJsoup(String content)
            throws UnsupportedEncodingException {
        content = content.replaceAll("textarea", "div");

        int contentIndex = content.indexOf("class=\"tb-content\"");

        if (contentIndex > 0) {
            content = replaceChar(content, contentIndex);
        }

        Document doc = Jsoup.parse(content);

        Elements elements = doc.select("#J_RankLike li");

        if (elements == null || elements.size() == 0) {
            return null;
        }

        List<PriceRangeLike> likes = new ArrayList<PriceRangeLike>();

        for (Element element : elements) {
            PriceRangeLike like = MainSearchApi.getPriceRangeLike(element);
            if (like != null) {
                likes.add(like);
            } else {
                // log.error("no info >:" + element);
            }
        }

        return likes;
    }
    
    public static List<ItemThumbSecond> parseSearchUrl(String queryArea,String url, String refer, TBSearchRes res) {
        
        final int ItemGetThreadSize = 32;
        PYFutureTaskPool<String> itemGetPoolOfAgent = new PYFutureTaskPool<String>(ItemGetThreadSize);
        List<FutureTask<String>> promises = new ArrayList<FutureTask<String>>();
          
        List<ItemThumbSecond> itemThumbSecond = new ArrayList<ItemThumbSecond>();
          
          //查询过快，会被挂掉
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e1) {
            log.warn(e1.getMessage(), e1);
        }
          
        if (res == null) {
            res = new TBSearchRes(null, 1, null);
        }

        String content = StringUtils.EMPTY;
          
          //使用手机进行查询
        try {
            url = URLEncoder.encode(url, "utf-8");
            String result = null;
//            String result = directGet("http://115.159.25.42:9003/MobileSpider/uploadDetailsPageUrl?searchUrl=" + url, refer, null, null, null);
            
//            if(url.indexOf("list.tmall.com")>-1||url.indexOf("detail.tmall.com")>-1) {
//                result = directGet("http://115.159.25.42:9003/MobileSpider/uploadDetailsPageUrl?searchUrl=" + url, refer, null, null, null);
//            } else {
//                result = directGet("http://115.159.25.42:9003/MobileSpider/uploadUrl?searchUrl=" + url, refer, null, null, null);
////              String result = directGet("http://127.0.0.1:10001/MobileSpider/uploadUrl?searchUrl=" + url, refer, null, null, null);
//            }
            
            if(StringUtils.isEmpty(result)) {
                return null;
            }
              
            long id = new JSONObject(result).getLong("id");
              
            CommonUtils.sleepQuietly(15000L);
              
            log.info("--DataBase   NumId--"+id);
            String contentUrl = directGet("http://115.159.25.42:9003/MobileSpider/getContentById?id=" + id, refer, null, null, null);
            
            if(StringUtils.isEmpty(contentUrl)) {
                return null;
            }
            
            content = new JSONObject(contentUrl).getString("content");
            if(StringUtils.isEmpty(content)||"null".equals(content)) {
                return null;
            }
            List<ItemThumb> itemThumbs = CollectInfoByWebpage.parseJsonWebContent(content); //获取到numIid,title,picUrl
            if(CommonUtils.isEmpty(itemThumbs)) {
                return null;
            }
            //区别TB还是TM
            int size = itemThumbs.size();
            log.info("--itemThumbs  size--"+size);
            for(ItemThumb itemThumb:itemThumbs) {
                promises.add(itemGetPoolOfAgent.submit(new CallableThreadOfAgent(size, itemThumb, refer)));
            }
              
            if(CommonUtils.isEmpty(promises)) {
                return null;
            }
            for(int i=0;i<promises.size();i++) {
                try {
                    contentUrl = promises.get(i).get();
                    if(StringUtils.isEmpty(contentUrl)) {
                        continue;
                    }
                    content = new JSONObject(contentUrl).getString("content");
                    if(StringUtils.isEmpty(content)||"null".equals(content)) {
                        continue;
                    }
                    String delistTime = content.toString();
                    ItemThumbSecond itemsecond = new ItemThumbSecond();
                    itemsecond.setdelistTimes(delistTime);
                    itemsecond.setdelistTimestamp(delistTime);
                    itemsecond.setFullTitle(itemThumbs.get(i).getFullTitle());
                    itemsecond.setPicPath(itemThumbs.get(i).getPicPath());
                    itemsecond.setId(itemThumbs.get(i).getId());
                    
                    itemThumbSecond.add(itemsecond);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                } catch (ExecutionException e) {
                    log.error(e.getMessage(), e);
                }
            }
        } catch (UnsupportedEncodingException e1) {
            log.error(e1.getMessage(), e1);
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
          
        return itemThumbSecond;
    }
    
      
    public static String detailPageContent(int size, ItemThumb itemThumb, String refer) {
        String contentUrl = null;
        try {
            String url = CollectInfoByWebpage.assembleDetailPageUrl(size,itemThumb.getId());
//            log.info("-----------------------------"+itemThumb.getId());
            String result = null;
//            String result = directGet("http://115.159.25.42:9003/MobileSpider/uploadDetailsPageUrl?searchUrl=" + url, refer, null, null, null);
//              String result = directGet("http://127.0.0.1:10001/MobileSpider/uploadDetailsPageUrl?searchUrl=" + url, refer, null, null, null);
            if (StringUtils.isEmpty(result)) {
                return null;
            }
            long id = new JSONObject(result).getLong("id");
            CommonUtils.sleepQuietly(15000L);

            contentUrl = directGet("http://115.159.25.42:9003/MobileSpider/getContentById?id=" + id, refer, null, null, null);
//              contentUrl = directGet("http://127.0.0.1:10001/MobileSpider/getContentById?id=" + id, refer, null, null, null);

            if (StringUtils.isEmpty(contentUrl)) {
                return null;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return contentUrl;
    }
    
    //在访问TM时，会有无效的content中带data-id的
    public static int getCountOfField(String content, String field) {
        int flag = 0;
        int count = 0;
          
        if(content.indexOf(field, flag)>0) {
            flag = content.indexOf(field, flag) + field.length();
            count++;
        }
        
        return count;
    }
    
    static String JsessionId = "JSESSIONID=CB4C93A8FB18DA2E2505DC339A813A5E;";
    
    public static TBSearchRes parseSearchUrl(String queryArea,String url, String refer, String cookie, TBSearchRes res) {
        if (res == null) {
            res = new TBSearchRes(null, 1, null);
        }
        cookie = "thw=cn; cna=nhK2Dr7ngWQCAXPHbianOGK0; miid=7188858411641501609; lzstat_uv=3243495356158448379|3492151@3600092@3038825@1267385@3544370@3511896@2978804@2857556; ali_ab=183.157.71.2.1446118655708.4; isg=AA620CED3A9E5D77D4BCF624B508C815; v=0; alitrackid=www.taobao.com; lastalitrackid=www.taobao.com; x=e%3D1%26p%3D*%26s%3D0%26c%3D0%26f%3D0%26g%3D0%26t%3D0%26__ll%3D-1%26_ato%3D0; swfstore=231915; _tb_token_=mLmj1arQX1kFWB9; uc3=nk2=odrzK9LIqyk%3D&id2=UoYY4dkb3DO45g%3D%3D&vt3=F8dAScn9%2FZkK9k9Hcqo%3D&lg2=WqG3DMC9VAQiUQ%3D%3D; existShop=MTQ1NTU5MzY5MQ%3D%3D; lgc=%5Cu843D%5Cu53F6%5Cu918922; tracknick=%5Cu843D%5Cu53F6%5Cu918922; sg=273; cookie2=1cfc6423cfd7d81511d6434346c1047f; cookie1=B0TxO6RRNEXYIa6UZX31l3y47aDUAl1KKQVxjWFYYyQ%3D; unb=1762009207; skt=fbc93fefd6c7597a; t=146dfd275ef41ecd6f0bf56606049a25; _cc_=U%2BGCWk%2F7og%3D%3D; tg=0; _l_g_=Ug%3D%3D; _nk_=%5Cu843D%5Cu53F6%5Cu918922; cookie17=UoYY4dkb3DO45g%3D%3D; mt=ci=1_1; uc1=cookie14=UoWyi2y0J3rZjw%3D%3D&existShop=false&cookie16=WqG3DMC9UpAPBHGz5QBErFxlCA%3D%3D&cookie21=W5iHLLyFeYZ1WM9hVnmS&tag=1&cookie15=U%2BGCWk%2F75gdr5Q%3D%3D&pas=0;l=Al5e5LjDTMtjgj5ezVMsU1R5Lv6gHyKZ";
        
        cookie = cookie + JsessionId;
        // 暂停，防止过快遭到淘宝反爬
        int nextInt = RandomUtils.nextInt(6);
        double sleepTime = 3.4D;
        if(nextInt >= 3){
            sleepTime = 3.7D;
        }
        log.info("暂停时间：" + sleepTime * 1000L);
        CommonUtils.sleepQuietly( (long) (sleepTime * 1000L));
        url = url + "&ajax=true";
        String content = getResult(3, url, refer, cookie);
        
//        if (!StringUtils.isEmpty(content)) {
//            // 检测返回的是否是登陆页面
//            content = content.indexOf(NEWTAOBAOSEARCHJSONSTART) == -1 ? null : content;
//        }

        if (StringUtils.isEmpty(content)) {
            // 利用手机获得数据
            log.error("本地ip爬取淘宝数据失败。。。");
            log.error("still content empty! req url: " + url);
            return res;
//            return useMobileGetTBSearchRes(url, refer, res);
        }
        
//        try {
//            parseNewSearchContentByJsoup(content, res, url);
//        } catch (UnsupportedEncodingException e) {
//            log.error(e.getMessage(), e);
//        }
        try {
            parseJson(content, res, url);
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
        return res;
    }
    
    public static String getResult(int retry, String url, String refer, String cookie) {
    	if(retry < 0) {
    		return StringUtils.EMPTY;
    	}
    	
    	String result = directGet(url, refer, null, null, cookie);
    	if(StringUtils.isEmpty(result)) {
    		CommonUtils.sleepQuietly( (long) (500L));
    		return getResult(--retry, url, refer, cookie);
    	}
    	
    	return result;
    }

    public static String directGet(String url, String referer, String ua, HttpHost host, String cookies) {
        HttpClient httpclient = null;

        HttpResponse rsp = null;

        try {
            httpclient = new DefaultHttpClient();
            if (host != null) {
                httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
            }

            HttpConnectionParams.setSoTimeout(httpclient.getParams(), 30000);
            HttpConnectionParams.setConnectionTimeout(httpclient.getParams(), 30000);
            httpclient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
            httpclient.getParams().setParameter(ClientPNames.MAX_REDIRECTS, 20);

            HttpGet httpGet = new HttpGet(url);
            if (referer != null) {
                httpGet.addHeader("Referer", referer);
            }
            if (cookies != null) {
                httpGet.addHeader("Cookie", cookies);
            }

//            httpGet.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)");
            httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
            
            rsp = httpclient.execute(httpGet);
            setJsessionId(rsp);
            
            HttpEntity entity = rsp.getEntity();
            String content = EntityUtils.toString(entity);
            EntityUtils.consume(entity);

            return content;
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return null;
    }
    
    private static void setJsessionId(HttpResponse rsp){
        Header firstHeader = rsp.getFirstHeader("set-cookie");
        if(firstHeader == null){
            return;
        }
        String setCookie = firstHeader.getValue();
        if(StringUtils.isEmpty(setCookie)){
            return;
        }
        String[] split = setCookie.split(" ");
        String sessionId = split[0];
        if(StringUtils.isEmpty(sessionId)){
            return;
        }
        JsessionId = sessionId;
    }
    
    private static void filterForContent(String content) {
        StringBuilder sb = new StringBuilder(content);
        String target = "<li class=\"sale\">";
        // String anchorTarget = "<";
        int index = 0;
        int anchorIndex = 0;
        do {
            index = sb.indexOf(target);
            if (index < 0) {
                break;
            }

            // anchorIndex = sb.indexOf(, arg1)

        } while (true);
    }

    static String MAINSEARCH_TAOBAO_COOKIE = null;

    public static int COOKIE_USED_TIME = 0;

    public synchronized static String genMainSearchTaobaoCookie() {
        if (MAINSEARCH_TAOBAO_COOKIE != null && COOKIE_USED_TIME < 1000) {
            COOKIE_USED_TIME++;
            return MAINSEARCH_TAOBAO_COOKIE;
        }
        // String cookie = null;
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
        // WebClient client = WebClientFactory.genWebClient(true);
        try {
            Page page = webClient.getPage("http://www.taobao.com/");
            // PlayUtil.sleepQuietly(8000L);
            List<String> parts = new ArrayList<String>();
            Set<Cookie> cookies = webClient.getCookieManager().getCookies();
            log.info("[raw cookies:]" + MAINSEARCH_TAOBAO_COOKIE);
            for (Cookie cookie2 : cookies) {
                parts.add(cookie2.getName() + "=" + cookie2.getValue());
            }

            MAINSEARCH_TAOBAO_COOKIE = StringUtils.join(parts, ";");
            log.error("generate cookie :" + MAINSEARCH_TAOBAO_COOKIE);
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
        } finally {
            webClient.closeAllWindows();
        }
        COOKIE_USED_TIME = 0;
        return MAINSEARCH_TAOBAO_COOKIE;
    }

    public static TBSearchRes parseSearchContentByHtmlUnit(String content, TBSearchRes res) throws IOException {

        URL url = new URL("http://www.dianxinos.com");

        StringWebResponse response = new StringWebResponse(content, "gbk", url);
        WebClient client = new WebClient();
        client.setJavaScriptEnabled(false);
        client.setCssEnabled(false);
        HtmlPage page = HTMLParser.parseHtml(response, client.getCurrentWindow());

        HtmlElement body = (HtmlElement) page.getElementById("page");
        int itemCount = parseItemCount(body);

        // log.error("item count :" + itemCount);

        List<ItemThumb> thumbs = parseItemListFromHtmlPage(body);

        res.setSuccess(true);
        res.setItems(thumbs);
        res.setItemCount(itemCount);
        res.setHasRecords(!CommonUtils.isEmpty(thumbs));

        return res;
    }

    public static int parseItemCount(HtmlElement body) {
        String itemCountText = NumberUtil.first(body.getElementsByAttribute("span", "class", "result-count"))
                .getTextContent();
        if (StringUtils.isEmpty(itemCountText)) {
            return 0;
        }

        return parseItemCountFromText(itemCountText);
    }

    public static int parseItemCountFromText(String itemCountText) {
        int res = 0;
        int index = itemCountText.indexOf("件");
        if (index > 0) {
            itemCountText = itemCountText.substring(0, index);
        }

        
        index = itemCountText.indexOf("万");
        if (index > 0) {
            itemCountText = itemCountText.substring(0, index);
            res = (int) (NumberUtil.parserDouble(itemCountText, 0) * 10000);
        } else {
            res = NumberUtil.parserInt(itemCountText, 0);
        }
        return res;
    }
    
    public static int parseNewItemCountFromText(String itemCountText) {
        int res = 0;
        int index = itemCountText.indexOf("件");
        if (index > 0) {
            itemCountText = itemCountText.substring(0, index);
        }

        
        index = itemCountText.indexOf("万");
        if (index > 0) {
            itemCountText = itemCountText.substring(0, index);
            res = (int) (NumberUtil.parserDouble(itemCountText, 0) * 10000);
        } else {
            res = NumberUtil.parserInt(itemCountText, 0);
        }
        return res;
    }

    private static List<ItemThumb> parseItemListFromHtmlPage(HtmlElement body) {
        List<HtmlElement> lis = body.getElementsByAttribute("li", "class", "list-item");
        log.info("[body :]" + body.asText());

        if (CommonUtils.isEmpty(lis)) {
            log.warn("no li content....");
            return ListUtils.EMPTY_LIST;
        }

        List<ItemThumb> thumbs = new ArrayList<ItemThumb>();
        for (HtmlElement li : lis) {
            ItemThumb thumb = parseItemThumb(li);
            if (thumb != null) {
                thumbs.add(thumb);
            } else {
                log.error(" no li :" + li.asXml());
            }
        }
        return thumbs;
    }

    private static ItemThumb parseItemThumb(HtmlElement li) {
        // log.info("[li:]" + li.asXml());
        if (CommonUtils.isEmpty(li.getElementsByTagName("em"))) {
            log.warn(" no em:" + li.asText());
            return null;
        }
        String rawContent = li.asText();
        if (!rawContent.contains("成交") || !rawContent.contains("最近")) {
            // log.warn(" no 成交:" + li.asXml());
            return null;
        }

        String rawSale = NumberUtil.first(li.getElementsByAttribute("li", "class", "sale")).asText();
        String href = NumberUtil.first(li.getElementsByTagName("a")).getAttribute("href");
        int tradeNum = parseTradeNumFromPage(rawSale);
        long numIid = findRawIdString(href);
        // log.info("[numIid :]" + numIid);
        int price = NumberUtil.getIntFromPrice(NumberUtil.first(li.getElementsByTagName("em")).asText());
        HtmlElement img = NumberUtil.first(li.getElementsByTagName("img"));
        String picPath = img.getAttribute("data-ks-lazyload");
        if (StringUtils.isBlank(picPath)) {
            picPath = img.getAttribute("src");
        }

        String title = NumberUtil.first(li.getElementsByAttribute("h3", "class", "summary")).asText();
        String sellerHref = NumberUtil.first(NumberUtil.first(li.getElementsByTagName("p")).getElementsByTagName("a"))
                .getAttribute("href");

        // log.info("[simple seller]" + li.getElementsByAttribute("p", "class", "seller"));
        String wangwang = NumberUtil.first(
                NumberUtil.first(li.getElementsByAttribute("p", "class", "seller lister hCard")).getElementsByTagName(
                        "a")).getTextContent();
        // log.info("[wangwang : ]" + wangwang);
        long sellerId = findSellerId(sellerHref);
        // log.info("[seller id :]" + sellerId);

        ItemThumb thumb = new ItemThumb();
        thumb.setId(numIid);
        thumb.setSellerId(sellerId);
        thumb.setTradeNum(tradeNum);
        thumb.setPrice(price);
        thumb.setPicPath(picPath);
        thumb.setFullTitle(title);
        thumb.setWangwang(wangwang);
        return thumb;
    }

    public static int newParseTradeNumFromPage(String rawSale) {
        try {
            String startTag = "<div>";
            String endTag = "人";
            int startIndex = rawSale.indexOf(startTag);
            if (startIndex < 0) {
                return 0;
            }
            startIndex += startTag.length();

            int endIndex = rawSale.indexOf(endTag, startIndex);
            if (endIndex < 0) {
                return 0;
            }
            String tradeNumStr = rawSale.substring(startIndex, endIndex);
            if (StringUtils.isEmpty(tradeNumStr)) {
                return 0;
            }
            tradeNumStr = tradeNumStr.trim();
            int tradeNum = NumberUtil.parserInt(tradeNumStr, 0);

            return tradeNum;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return 0;
        }
    }

    public static int parseTradeNumFromPage(String rawSale) {
        // log.info("[raw sale :]" + rawSale);
        int num = 0;
        try {
            num = newParseTradeNumFromPage(rawSale);
            if (num > 0) {
                return num;
            }

            String startTag = "成交";
            String startTag2 = "人";
            String endTag = "笔";
            String wanTag = "万";
            boolean useWan = false;
            int startIndex = rawSale.indexOf(startTag);
            if (startIndex >= 0) {
                startIndex += startTag.length();
            } else {
                startIndex = rawSale.indexOf(startTag2);
                if (startIndex > 0) {
                    startIndex += startTag2.length();
                }
            }
            int endIndex = rawSale.indexOf(endTag, startIndex);
            if (startIndex < 0 || endIndex < 0) {
                return 0;
            }

            /*
             * log.info("[start inde x: ]" + startIndex); log.info("[end inde x: ]" + endIndex);
             */
            String rawNum = rawSale.substring(startIndex, endIndex);
            // log.info("[raw num;]" + rawNum);
            if (rawNum.contains(wanTag)) {
                useWan = true;
                rawNum = rawNum.replaceAll(wanTag, StringUtils.EMPTY);
            }

            num = 0;
            if (useWan) {
                num = (int) (NumberUtil.parserDouble(rawNum, 0d) * 10000d);
            } else {
                num = NumberUtil.parserInt(rawNum, 0);
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            log.error("error sale :" + rawSale);
        }

        return num;
    }

    /**
     * href="http://store.taobao.com/shop/view_shop.htm?spm=a230r.1.10.6.6mR4Cy&user_number_id=752473764"
     * 
     * @param url
     * @return
     */
    public static long findSellerId(String url) {
        // log.info("[seller href:]" + url);
        int index = 0;
        String target = "&user_number_id=";
        index = url.indexOf(target);
        if (index > 0) {
            return appendForId(url.substring(index + target.length()));
        }
        target = "?user_number_id=";
        index = url.indexOf(target);
        if (index > 0) {
            return appendForId(url.substring(index + target.length()));
        }
        return 0L;
    }

    public static long findRawIdString(String url) {
        // log.info("[do for href:]" + url);
        int index = 0;
        String target = "?id=";
        index = url.indexOf(target);
        if (index > 0) {
            return appendForId(url.substring(index + target.length()));
        }
        target = "&id=";
        index = url.indexOf(target);
        if (index > 0) {
            return appendForId(url.substring(index + target.length()));
        }

        target = "?default_item_id=";
        index = url.indexOf(target);
        if (index > 0) {
            return appendForId(url.substring(index + target.length()));
        }

        target = "&default_item_id=";
        index = url.indexOf(target);
        if (index > 0) {
            return appendForId(url.substring(index + target.length()));
        }

        target = "?item_num_id=";
        index = url.indexOf(target);
        if (index > 0) {
            return appendForId(url.substring(index + target.length()));
        }

        target = "&item_num_id=";
        index = url.indexOf(target);
        if (index > 0) {
            return appendForId(url.substring(index + target.length()));
        }
        return 0L;
    }

    public static long appendForId(String str) {
        str = str.trim();
        StringBuilder sb = new StringBuilder();
        int count = 0;
        int length = StringUtils.length(str);
        while (count < length) {
            String s = str.substring(count, count + 1);
            if (StringUtils.isNumeric(s)) {
                sb.append(s);
            } else {
                break;
            }
            count++;
        }
        return NumberUtil.parserLong(sb.toString(), 0L);
    }

    @JsonAutoDetect
    public static class PriceRangeLike {
        @JsonProperty
        int begin;

        @JsonProperty
        int end;

        @JsonProperty
        int percent;

        public int getBegin() {
            return begin;
        }

        public void setBegin(int begin) {
            this.begin = begin;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public int getPercent() {
            return percent;
        }

        public void setPercent(int percent) {
            this.percent = percent;
        }

        public PriceRangeLike(int begin, int end, int percent) {
            super();
            this.begin = begin;
            this.end = end;
            this.percent = percent;
        }

        public PriceRangeLike() {

        }
    }

    @JsonAutoDetect
    public static class PriceRangeInfo {
        @JsonProperty
        int start;

        @JsonProperty
        int end;

        @JsonProperty
        double percent;

        @JsonProperty
        String url;

        public PriceRangeInfo(int start, int end, double percent, String url) {
            super();
            this.start = start;
            this.end = end;
            this.percent = percent;
            this.url = url;
        }

        @Override
        public String toString() {
            return "PriceRangeInfo [start=" + start + ", end=" + end + ", percent=" + percent + ", url=" + url + "]";
        }

    }

    @JsonAutoDetect
    public static class TBSearchRes {
        @JsonProperty
        int itemCount;

        @JsonProperty
        String query;

        @JsonProperty
        int pageSize = 40;

        @JsonProperty
        int curPage;

        @JsonProperty
        String order;

        @JsonProperty
        List<ItemThumb> items;

        @JsonProperty
        List<PriceRangeInfo> ranges;
        //uttp
        @JsonProperty
        Long cid;
        
        @JsonProperty
        boolean success = false;

        @JsonProperty
        boolean hasRecords = false;
        
        Long frontCid;

        public TBSearchRes(int itemCount, String query, int pn, String order, List<ItemThumb> items) {
            super();
            this.itemCount = itemCount;
            this.query = query;
            this.curPage = pn;
            this.order = order;
            this.items = items;
          //uttp
            this.cid = -1L;
        }

        public TBSearchRes(int itemCount, String query, int pn, String order, List<ItemThumb> items,
                List<PriceRangeInfo> ranges) {
            super();
            this.itemCount = itemCount;
            this.query = query;
            this.curPage = pn;
            this.order = order;
            this.items = items;
            this.ranges = ranges;
          //uttp
            this.cid = -1L;
        }

        public TBSearchRes(String query, int pn, String order) {
            super();
            this.query = query;
            this.curPage = pn;
            this.order = order;
            //uttp
            this.cid = -1L;
        }
        
      //uttp
        public TBSearchRes(int pn, String order, Long cid){
        	super();
        	this.query = null;
            this.curPage = pn;
            this.order = order;
            this.cid   = cid;
        }

        @Override
        public String toString() {
            return "TBSearchRes [itemCount=" + itemCount +", cid="+ cid+ ", query=" + query + ", ps=" + pageSize + ", pn=" + curPage
                    + ", order=" + order + ", items=" + items + ", ranges=" + ranges + "]";
        }

        public int getItemCount() {
            return itemCount;
        }

        public void setItemCount(int itemCount) {
            this.itemCount = itemCount;
        }
        
        //uttp
        public Long getCid(){
        	return this.cid;
        }
        
        //uttp
        public void setCid(Long cid){
        	this.cid = cid;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getCurPage() {
            return curPage;
        }

        public void setCurPage(int curPage) {
            this.curPage = curPage;
        }

        public String getOrder() {
            return order;
        }

        public void setOrder(String order) {
            this.order = order;
        }

        public List<ItemThumb> getItems() {
            return items;
        }

        public void setItems(List<ItemThumb> items) {
            this.items = items;
        }

        public List<PriceRangeInfo> getRanges() {
            return ranges;
        }

        public void setRanges(List<PriceRangeInfo> ranges) {
            this.ranges = ranges;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public boolean isHasRecords() {
            return hasRecords;
        }

        public void setHasRecords(boolean hasRecords) {
            this.hasRecords = hasRecords;
        }

        public Long getFrontCid() {
            return frontCid;
        }

        public void setFrontCid(Long frontCid) {
            this.frontCid = frontCid;
        }
        
    }

    public static final String NEWTAOBAOSEARCHJSONSTART = "g_page_config";
    public static final String NEWTAOBAOSEARCHJSONEND = "g_srp_loadCss";
    public static final String MODS = "mods";
    public static final String ITEM_LIST = "itemlist";
    public static final String ITEM_LIST_DATA = "data";
    public static final String AUCTIONS = "auctions";
    public static TBSearchRes parseNewSearchContentByJsoup(String content, TBSearchRes res, String url)
            throws UnsupportedEncodingException {
    	if(StringUtils.isEmpty(content)) {
    		return res;
    	}
    	int jsonStart = content.indexOf(NEWTAOBAOSEARCHJSONSTART);
    	if(jsonStart <= 0) {
    		return res;
    	}
    	int jsonEnd = content.indexOf(NEWTAOBAOSEARCHJSONEND);
    	if(jsonEnd <= 0 || jsonEnd <= jsonStart) {
    		return res;
    	}
    	String json = content.substring(jsonStart, jsonEnd);
    	if(StringUtils.isEmpty(json)) {
    		return res;
    	}
    	json = json.replace("g_page_config = ", "");
    	json = json.trim();
    	json = json.substring(0, json.length() - 1);
    	JSONObject object = null;
    	try {
			object = new JSONObject(json);
	    	JSONObject mainInfo = object.getJSONObject(MODS);
	    	if(mainInfo == null) {
	    		return res;
	    	}
	    	JSONObject itemList = mainInfo.getJSONObject(ITEM_LIST);
	    	if(itemList == null) {
	    		return res;
	    	}
	    	if(itemList.isNull(ITEM_LIST_DATA)) {
	    		return res;
	    	}
	    	JSONObject itemListData = itemList.getJSONObject(ITEM_LIST_DATA);
	    	if(itemListData == null) {
	    		return res;
	    	}
	    	JSONArray auctions = itemListData.getJSONArray(AUCTIONS);
	    	if(auctions == null || auctions.length() <= 0) {
	    		return res;
	    	}
	    	int size = auctions.length();
	    	List<ItemThumb> items = new ArrayList<ItemThumb>();
	    	for(int i = 0; i < size; i++) {
	    		JSONObject item = auctions.getJSONObject(i);
	    		ItemThumb thumb = MainSearchApi.doWithItem(item, url);
	            if (thumb != null) {
	                items.add(thumb);
	            } else {
	                // log.error("no info >:" + element);
	            }
	    	}

	        res.setSuccess(true);
	        res.setItems(items);
	    	// 设置查询到的总宝贝数
	    	res.setItemCount(0);
	    	return res;
		} catch (JSONException e) {
			e.printStackTrace();
			return res;
		}
    	
    }
    
    public static TBSearchRes parseSearchContentByJsoup(String content, TBSearchRes res, String url)
            throws UnsupportedEncodingException {
    	//System.out.println(content);
        content = content.replaceAll("textarea", "div");

        int contentIndex = content.indexOf("class=\"tb-content\"");
        // log.error("content index;" + contentIndex);
        // log.error("origin length:" + content.length());
        if (contentIndex > 0) {
            content = replaceChar(content, contentIndex);
        }
        // log.error("final length:" + content.length());

        Document doc = Jsoup.parse(content);
        Element first = doc.select(".result-info").first();
        int itemCount = 0;

        if (first != null) {
            itemCount = MainSearchApi.parseItemCountFromText(first.text());
        }

        if (itemCount == 0) {
            first = doc.select(".nav-topbar-content ul li .h").first();
            if (first != null) {
                itemCount = MainSearchApi.parseItemCountFromText(first.text());
            }
        }

        if (itemCount == 0) {
            first = doc.select(".result-count").first();
            if (first != null) {
                itemCount = MainSearchApi.parseItemCountFromText(first.text());
            }
        }
        if (itemCount == 0) {
            first = doc.select(".result-count-cont").first();
            if (first != null) {
                itemCount = MainSearchApi.parseItemCountFromText(first.text());
            }
        }
        res.setItemCount(itemCount);
        // log.error("item count :" + itemCount);

        Elements elements = doc.select(".tb-content").select(".row");

        if (elements == null || elements.size() == 0) {
            elements = doc.select("#list-content").select(".list-item");
        }
        if (elements == null || elements.size() == 0) {
            elements = doc.select(".tb-content").select(".list-item");
        }

        if (elements == null) {
            log.error("RESP: " + content);
            return res;
        }
        List<ItemThumb> items = new ArrayList<ItemThumb>();

        // log.info("[lis : size ]" + elements.size());
        // try {
        // FileUtils.writeStringToFile(new File("/Users/navins/Code/taobao/test.html"), elements.html());
        // } catch (IOException e) {
        // e.printStackTrace();
        // }

        for (Element element : elements) {
            ItemThumb thumb = MainSearchApi.doWithItem(element, url);
            if (thumb != null) {
                items.add(thumb);
            } else {
                // log.error("no info >:" + element);
            }
        }
        // log.info("[items :]" + items);
        res.setSuccess(true);
        res.setItems(items);

        // price range
        if (TMConfigs.PARSE_PRICE_RANGE == true) {
            try {
                Elements rangesLi = doc.select(".tb-sortbar .price-range-like ul li");
                if (rangesLi != null) {
                    List<PriceRangeInfo> ranges = new ArrayList<PriceRangeInfo>();
                    for (Element element : rangesLi) {
                        int start = Integer.valueOf(element.attr("start"));
                        int end = Integer.valueOf(element.attr("end"));
                        double percent = Double.valueOf(element.attr("percent"));
                        String rangeUrl = element.attr("url");
                        PriceRangeInfo range = new PriceRangeInfo(start, end, percent, rangeUrl);
                        ranges.add(range);
                    }
                    res.setRanges(ranges);
                }
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }

        }

        return res;
    }

    private static String replaceChar(String content, int contentIndex) {
        StringBuilder sb = new StringBuilder();
        sb.append(content.substring(0, contentIndex));
        String subEnd = content.substring(contentIndex);
        subEnd = subEnd.replace("&lt;", "<");
        subEnd = subEnd.replace("&gt;", ">");
        subEnd = subEnd.replace("&quot;", "\"");
        sb.append(subEnd);
        return sb.toString();
    }

    public static ItemThumb doWith(Element li) {
        // log.info("[item li :]" + li.html());

        // log.info("[li text:]" + li.html());

        ItemThumb thumb = new ItemThumb();
        int tradeNum = -1;
        Element first = li.select("li.sale").first();
        if (first != null) {
            tradeNum = parseTradeNumFromPage(first.html());
        }
        if (tradeNum < 0) {
            tradeNum = parseTradeNumFromPage(li.text());
        }

        // String rawSale = first.text();
        // if (StringUtils.isBlank(rawSale)) {
        // log.error("error li:" + first);
        // return null;
        // }
        // if (rawSale.indexOf("成交") < 0) {
        // log.error("error li:" + first);
        // return null;
        // }

        // log.info("[found tradeNum :]" + tradeNum);

        thumb.setTradeNum(tradeNum);
        long numIid = -1L;

        try {
            //
            String href = li.select("a").first().attr("href");
            numIid = findRawIdString(href);
            // log.error("numIid :" + numIid);
            String priceString = li.select("em").first() == null ? null : li.select("em").first().text();
            int price = 0;
            if (NumberUtils.isNumber(priceString)) {
                price = NumberUtil.getIntFromPrice(priceString);
            }
            if (price == 0) {
                String priceText = li.select(".price").text().trim();
                int priceIndex = priceText.indexOf("￥");
                if (priceIndex >= 0) {
                    price = NumberUtil.getIntFromPrice(priceText.substring(priceIndex + 1));
                } else {
                    price = NumberUtil.getIntFromPrice(priceText);
                }
            }
            first = li.select("img").first();
            //modify by uttp
            String picPath = first.hasAttr("data-ks-lazyload") ? first.attr("data-ks-lazyload") : first.attr("src");

            String title = li.select("h3.summary").text();
            long sellerId = 0L;
            thumb.setId(numIid);
            String sellerHref = li.select(".seller").first().select("a").first().attr("href");
            sellerId = findSellerId(sellerHref);
            String wangwang = null;
            if (sellerId <= 0L) {
                // new ItemPageApi("http://item.taobao.com/")
                UserIdNick fetchUserId = ItemPageApi.fetchUserId("http://item.taobao.com/item.htm?id=" + numIid);
                log.warn("fetch id nick :" + fetchUserId);

                if (fetchUserId != null) {
                    sellerId = fetchUserId.getUserid();
                    wangwang = fetchUserId.getNick();
                }
            }

            if (wangwang == null) {
                wangwang = li.select(".seller").first().select("a").first().text();
            }

            thumb.setSellerId(sellerId);
            thumb.setPrice(price);
            thumb.setPicPath(picPath);
            thumb.setFullTitle(title);
            thumb.setWangwang(wangwang);

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            // System.out.println(li.html());
        }

        if (numIid <= 0L) {
            return null;
        }

        // if (thumb.getSellerId() == null || thumb.getSellerId() <= 0L) {
        // if (true) {
        // return null;
        // }
        // ItemPlay spItem = ItemPlay.tryGetShopByItemId(numIid, "MainSearch");
        // // ItemPlay.easyMatchShop(numIid);
        // if (spItem == null) {
        // return null;
        // }
        // log.error("set for shop :" + spItem.getShop());
        // thumb.setId(spItem.getNumIid());
        // thumb.setSellerId(spItem.getShop().getSellerId());
        // thumb.setPrice(spItem.getPrice());
        // thumb.setPicPath(spItem.getPicPath());
        // thumb.setFullTitle(spItem.getTitle());
        // thumb.setWangwang(spItem.getShop().getWangwang());
        // }

        return thumb;
    }

    // class SearchThumb ex

    public static PriceRangeLike getPriceRangeLike(Element element) {
        if (element == null) {
            return null;
        }
        return new PriceRangeLike(Integer.valueOf(element.attr("start")),
                Integer.valueOf(element.attr("end")), Integer.valueOf(element.attr("percent")));
    }

    public static ItemThumb doWithItem(JSONObject item, String url) {
    	ItemThumb thumb = new ItemThumb();
    	try {
    		// 设置销量
			String buyerCount = item.getString("view_sales");
			if(StringUtils.isEmpty(buyerCount)) {
				thumb.setTradeNum(-1);
			} else {
			    buyerCount = buyerCount.replaceAll("[^0-9]", "");
				thumb.setTradeNum(Integer.valueOf(buyerCount));
			}
			
			// 设置numIid
			Long numIid = item.getLong("nid");
			if(numIid == null) {
				return null;
			} else {
				thumb.setId(numIid);
			}
			
			// 设置价格
			String view_price = item.getString("view_price");
			if(StringUtils.isEmpty(view_price)) {
				thumb.setPrice(-1);
			} else {
				thumb.setPrice((int) (NumberUtil.parserDouble(view_price, 0d) * 100d));
			}
			
			// 设置主图路径
			String pic_url = item.getString("pic_url");
			if(!StringUtils.isEmpty(pic_url)) {
				thumb.setPicPath(pic_url);
			} 
			
			// 设置宝贝标题
			String raw_title = item.getString("raw_title");
			if(!StringUtils.isEmpty(raw_title)) {
				thumb.setFullTitle(raw_title);
			} 
			
			// 设置旺旺ID 
			Long user_id = item.getLong("user_id");
			if(user_id != null ) {
				thumb.setSellerId(user_id);
			} 
			
			// 设置旺旺
			String nick = item.getString("nick");
			if(!StringUtils.isEmpty(nick)) {
				thumb.setWangwang(nick);
			} 
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	return thumb;
    }
    
    public static ItemThumb doWithItem(Element element, String url) {
        ItemThumb thumb = new ItemThumb();
        int tradeNum = -1;
        Element first = element.select(".dealing").first();
        if (first == null) {
            first = element.select(".sale").first();
        }
        if (first != null) {
            tradeNum = parseTradeNumFromPage(first.html());
        }
        if (tradeNum < 0) {
            tradeNum = parseTradeNumFromPage(element.text());
        }

        thumb.setTradeNum(tradeNum);
        long numIid = -1L;

        try {
            //
            Elements select = element.select("a");
//          log.info("[anchors:]" + select);
          if (select == null) {
              log.info("[no anchro:]");
              return null;
          }
          for (Element anchor : select) {
//              TBUrlManager
              String href = anchor.attr("href");
              if (StringUtils.isEmpty(href)) {
                  continue;
              }
              TBUrlManager m = TBUrlManager.get();
              if (!m.isTaobaoItemUrl(href)) {
                  continue;
              }

              numIid = m.findItemId(href);
              if (numIid <= 0) {
                  continue;
              }
              thumb.setId(numIid);
              break;
          }

            // log.error("numIid :" + numIid);

            String priceString = element.select("em").first() == null ? null : element.select("em").first().text();
            int price = 0;
            if (NumberUtils.isNumber(priceString)) {
                price = NumberUtil.getIntFromPrice(priceString);
            }
            if (price == 0) {
                String priceText = element.select(".price").text().trim();
                int priceIndex = priceText.indexOf("￥");
                if (priceIndex >= 0) {
                    int priceEnd = priceText.substring(priceIndex + 1).indexOf("￥");
                    if (priceEnd >= 0) {
                        price = NumberUtil.getIntFromPrice(priceText.substring(priceIndex + 1, priceEnd));
                    } else {
                        price = NumberUtil.getIntFromPrice(priceText.substring(priceIndex + 1));
                    }
                } else {
                    price = NumberUtil.getIntFromPrice(priceText);
                }
            }
            thumb.setPrice(price);

            first = element.select("img").first();
            if (first != null) {
                String picPath = first.hasAttr("data-ks-lazyload") ? first.attr("data-ks-lazyload") : first.attr("src");
                thumb.setPicPath(picPath);
            } else {
                // http://s.taobao.com/search?q=%CB%AE%B5%C4%C2%C3%D0%D0%A3%BA4%A1%AB5%CB%EA%D7%DB%BA%CF%B6%C1%B1%BE%A3%A8%B8%BD%B9%E2%C5%CC%A3%A9%A3%A8%C8%AB%C8%FD%B2%E1%A3%A9&app=detail
                // 处理这种类型图片在外面
            }

            String title = element.select("h3.summary").hasAttr("title") ? element.select("h3.summary").attr("title")
                    .trim() : null;
            if (StringUtils.isEmpty(title)) {
                title = element.select("h3.summary").text().trim();
            }
            thumb.setFullTitle(title);

            long sellerId = 0L;
            if (element.select(".seller") != null && element.select(".seller").first() != null) {
                String sellerHref = element.select(".seller").first().select("a").first().attr("href");
                sellerId = findSellerId(sellerHref);
            }
            String wangwang = null;
            if (sellerId <= 0L) {
                // new ItemPageApi("http://item.taobao.com/")
                UserIdNick fetchUserId = ItemPageApi.fetchUserId("http://item.taobao.com/item.htm?id=" + numIid);
                log.warn("fetch id nick :" + fetchUserId);

                if (fetchUserId != null) {
                    sellerId = fetchUserId.getUserid();
                    wangwang = fetchUserId.getNick();
                }
            }

            if (wangwang == null && element.select(".seller") != null && element.select(".seller").first() != null) {
                wangwang = element.select(".seller").first().select("a").first().text();
            }

            thumb.setSellerId(sellerId);
            thumb.setWangwang(wangwang);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            log.error("MainSearch ERROR: " + url);
            // System.out.println(element.html());
            return null;
        }

        if (numIid <= 0L) {
            log.info("[no numiids]" + numIid);
            return null;
        }

        // if (thumb.getSellerId() == null || thumb.getSellerId() <= 0L) {
        // // if (true) {
        // // return null;
        // // }
        // ItemPlay spItem = ItemPlay.tryGetShopByItemId(numIid, "MainSearch");
        // // ItemPlay.easyMatchShop(numIid);
        // if (spItem == null) {
        // return null;
        // }
        // log.error("set for shop :" + spItem.getShop());
        // thumb.setId(spItem.getNumIid());
        // thumb.setSellerId(spItem.getShop().getSellerId());
        // thumb.setPrice(spItem.getPrice());
        // thumb.setPicPath(spItem.getPicPath());
        // thumb.setFullTitle(spItem.getTitle());
        // thumb.setWangwang(spItem.getShop().getWangwang());
        // }

        return thumb;
    }

    private static TBSearchRes parseJson(String json, TBSearchRes res, String url) throws JSONException{
        JSONObject object = new JSONObject(json);
        JSONObject mainInfo = object.getJSONObject(MODS);
        if(mainInfo == null) {
            return res;
        }
        JSONObject itemList = mainInfo.getJSONObject(ITEM_LIST);
        if(itemList == null) {
            return res;
        }
        if(itemList.isNull(ITEM_LIST_DATA)) {
            return res;
        }
        JSONObject itemListData = itemList.getJSONObject(ITEM_LIST_DATA);
        if(itemListData == null) {
            return res;
        }
        JSONArray auctions = itemListData.getJSONArray(AUCTIONS);
        if(auctions == null || auctions.length() <= 0) {
            return res;
        }
        int size = auctions.length();
        List<ItemThumb> items = new ArrayList<ItemThumb>();
        for(int i = 0; i < size; i++) {
            JSONObject item = auctions.getJSONObject(i);
            ItemThumb thumb = MainSearchApi.doWithItem(item, url);
            if (thumb != null) {
                items.add(thumb);
            } else {
                // log.error("no info >:" + element);
            }
        }

        res.setSuccess(true);
        res.setItems(items);
        // 设置查询到的总宝贝数
        res.setItemCount(0);
        return res;
    }
    
}
