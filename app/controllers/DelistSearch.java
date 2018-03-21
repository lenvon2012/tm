package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.db.jpa.NoTransaction;
import play.mvc.Http.StatusCode;
import result.TMResult;
import utils.URLParser;
import utils.UserCache;
import actions.industry.IndustryDelistGetAction;
import actions.industry.IndustryDelistResultAction;
import actions.industry.IndustryDelistResultAction.DelistItemInfo;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.api.SimpleHttpApi;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.MapIterator;
import com.dbt.cred.utils.JsonUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DelistSearch extends TMController {

    private static final Logger log = LoggerFactory.getLogger(DelistSearch.class);
    final static int ItemGetThreadSize = 32;
    static PYFutureTaskPool<TMResult> itemGetPoolSearch = new PYFutureTaskPool<TMResult>(ItemGetThreadSize);
    
    static String HTTP_STRING = "http://";
    
    static String HTTPS_STRING = "https://";
    
    public static void index() {
        render("Kits/delistsearch.html");
    }
    
    public static void item() {
        render("industry/itemsearch.html");
    }
    
    //
    @NoTransaction
    public static void analyseTaobaoDelists(String searchKey, 
            String itemOrderType, int searchPages, String searchPlace) {
        
        User user = getUser();
        
        if (StringUtils.isBlank(searchKey)) {
            renderFailedJson("搜索关键词不能为空！");
        }
        searchKey = searchKey.trim();
        if (StringUtils.isBlank(searchKey)) {
            renderFailedJson("搜索关键词不能为空！");
        }
        if (StringUtils.isEmpty(itemOrderType)) {
            renderFailedJson("请先选择宝贝要排序的方式！");
        }
        if (searchPages <= 0) {
            renderFailedJson("请先选择要搜索的宝贝页数！");
        }
        
        try {
            Cache.delete("79742176");//colrest510的ID
            UserCache usercache = (UserCache) Cache.get(user.getId().toString());
            if(usercache != null) {
                if(usercache.isSearch()) {
                    renderFailedJson("请不要在不同客户端同时进行查询!");
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        
        int[] hourDelistArray = IndustryDelistResultAction.countTaobaoItemHourlyDelist(searchKey, 
                itemOrderType, searchPages, searchPlace, user);
        
        if(hourDelistArray == null) {
            renderFailedJson("亲~O(∩_∩)O,未查询到数据，请一分钟后再次查询...还剩"+ ((60000 - System.currentTimeMillis() + TMController.checkEndTimeCache(user.getId()).getRunningEndTime()) / 1000)+ "秒可再次查询..");
        }
        
        renderBusJson(hourDelistArray);
        
    }
    
    public static void searchTaobaoItems(String searchKey, String itemOrderType, int searchPages, 
            String searchPlace, String orderBy, boolean isDesc, int pn, int ps) {

        User user = getUser();
        
        if (StringUtils.isBlank(searchKey)) {
            renderFailedJson("搜索关键词不能为空！");
        }
        searchKey = searchKey.trim();
        if (StringUtils.isBlank(searchKey)) {
            renderFailedJson("搜索关键词不能为空！");
        }
        searchKey = removeSpace(searchKey);
        if (searchKey.length() > 30) {
            renderFailedJson("关键词长度过长！");
        }
        if (StringUtils.isEmpty(itemOrderType)) {
            renderFailedJson("请先选择宝贝要排序的方式！");
        }
        if (searchPages <= 0) {
            renderFailedJson("请先选择要搜索的宝贝页数！");
        }
        
        try {
            Cache.delete("79742176");//colrest510的ID
            UserCache usercache = (UserCache) Cache.get(user.getId().toString());
            if(usercache != null) {
                if(usercache.isSearch()) {
                    renderFailedJson("请不要在不同客户端同时进行查询!");
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        
        PageOffset po = new PageOffset(pn, ps);
        
        TMResult tmResult = IndustryDelistResultAction.findTaobaoItemsWithPaging(searchKey, 
                itemOrderType, searchPages, searchPlace, orderBy, isDesc, po, user);
        
        if(tmResult.getCount() == 0) {
            renderFailedJson("亲~O(∩_∩)O,未查询到数据，请一分钟后再次查询...还剩"+ ((60000 - System.currentTimeMillis() + TMController.checkEndTimeCache(user.getId()).getRunningEndTime()) / 1000)+ "秒可再次查询..");
        }
        
        renderJSON(JsonUtil.getJson(tmResult));
    }
    
    public static String removeSpace(String searchKey) {
        while(searchKey.indexOf(" ")>0) {
            searchKey = searchKey.substring(0, searchKey.indexOf(" ")) + searchKey.substring(searchKey.indexOf(" ")+1, searchKey.length());
        }
        return searchKey;
     }
    
    public static void findItemDelistByHrefOrNumIid(String searchKey) {
        
        if(StringUtils.isEmpty(searchKey)) {
    	    renderFailedJson("传入的参数为空");
        }
        Long numIid = 0L;
        if(StringUtils.isNumeric(searchKey)) {
        	numIid = Long.valueOf(searchKey);
        } else {
        	if(searchKey.startsWith("http://")) {
        		searchKey = searchKey.substring("http://".length());
        	}
        	if(URLParser.isItemId(searchKey)) {

            	numIid = URLParser.findItemId(searchKey);
            } else {
            	renderFailedJson("请输入正确的宝贝链接或ID");
            }
        }
        
        /*else if(URLParser.isItemId(searchKey)) {

        	numIid = URLParser.findItemId(searchKey);
        } else {
        	renderFailedJson("请输入正确的宝贝链接或ID");
        }*/
        DelistItemInfo itemInfo = IndustryDelistGetAction.searchOneItem(numIid);
        
        if (itemInfo == null) {
            renderFailedJson("找不到该宝贝，请确认！");
        }
        
        renderBusJson(itemInfo);
    }
    
    public static void findItemDelistByHref(String searchKey) {
        if (StringUtils.isEmpty(searchKey)) {
            renderFailedJson("传入的参数为空");
        }
        User user = getUser();
        if (searchKey.indexOf('?') > 0 || isTaobaoItemUrl(searchKey)) {
            try {
                if(searchKey.startsWith(HTTP_STRING)){
                    searchKey = searchKey.substring(HTTP_STRING.length());
                }
                if(searchKey.startsWith(HTTPS_STRING)){
                    searchKey = searchKey.substring(HTTPS_STRING.length());
                }
                long numIid = URLParser.findItemId(searchKey);
                // 通过淘大象网址的接口获得宝贝的下架时间
                String delistTime = directPost("http://www.taodaxiang.com/shelf/index/get", user.getUserNick(), numIid);
                renderSuccessJson(delistTime);
            } catch (ClientProtocolException e) {
                log.error(e.getMessage(), e);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        renderFailedJson("请输入正确的宝贝链接");
    }
    
    private static String directPost(String url, String userNick, long numIid) throws ClientProtocolException, IOException{
        Map<String, String> map = new HashMap<String, String>(4);
        map.put("pattern", "1");
        map.put("wwid", "");
        map.put("goodid", String.valueOf(numIid));
        map.put("page", "1");

        HttpClient httpclient = new DefaultHttpClient();
        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        httpclient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
        httpclient.getParams().setParameter(ClientPNames.MAX_REDIRECTS, 20);
        httpclient.getParams().setParameter("X-Requested-With", "XMLHttpRequest");
        httpclient.getParams().setParameter("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36");
        httpclient.getParams().setParameter("Referer", "http://www.taodaxiang.com/shelf/index/init/");
        httpclient.getParams().setParameter("Origin", "http://www.taodaxiang.com");
        httpclient.getParams().setParameter("Host", "http://www.taodaxiang.com");
        httpclient.getParams().setParameter("Cookie", "PHPSESSID=nf6mlmu4ujl1akhf80fmtov722; jiathis_rdc=%7B%22http%3A//www.taodaxiang.com/shelf/index/init/%22%3A%223%7C1457428199885%22%7D; Hm_lvt_5a903fe4f343bef5b71db5664648022b=1457424451,1457428200; Hm_lpvt_5a903fe4f343bef5b71db5664648022b=1457428200");
        
        new MapIterator<String, String>(map) {
            @Override
            public void execute(Entry<String, String> entry) {
                params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }.call();

        HttpPost post = new HttpPost(url);
        if (!CommonUtils.isEmpty(params)) {
            post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        }

        HttpResponse rsp = httpclient.execute(post);
        if (rsp.getStatusLine().getStatusCode() == StatusCode.MOVED) {
            rsp.getHeaders("Location");
        }
        HttpEntity entity = rsp.getEntity();
        String content = EntityUtils.toString(entity);
        EntityUtils.consume(entity);
        return content;
    }

    public static final boolean isTaobaoItemUrl(String src) {
        if (StringUtils.isEmpty(src)) {
            return false;
        }
        return src.startsWith("item.taobao.com")
                || src.startsWith("detail.tmall.com")
                || src.startsWith("meal.taobao.com")
                || src.startsWith("item.tmall.com")
                || src.startsWith("detail.taobao.com")
                || src.startsWith("meal.tmall.com");
    }
    
    public static void findItemDelist(Long numIid) {
        if (numIid == null || numIid <= 0L) {
            renderFailedJson("请先输入要查询的宝贝！");
        }
        DelistItemInfo itemInfo = IndustryDelistGetAction.searchOneItem(numIid);
        if (itemInfo == null) {
            renderFailedJson("找不到该宝贝，请确认！");
        }
        renderBusJson(itemInfo);
    }
    
    public static void findItemDelistTime(String searchUrl){
        if(StringUtils.isEmpty(searchUrl)){
            renderFailedJson("请输入要查询宝贝的链接地址");
        }
        if(searchUrl.startsWith(HTTP_STRING)){
            searchUrl = searchUrl.substring(HTTP_STRING.length());
        }
        if(searchUrl.startsWith(HTTPS_STRING)){
            searchUrl = searchUrl.substring(HTTPS_STRING.length());
        }
        long numIid = URLParser.findItemId(searchUrl);
        if(numIid <= 0){
            renderFailedJson("请输入正确的宝贝的链接地址");
        }
        // 获取宝贝的上架时间
        String setMdskip = "setMdskip";
        String url = "https://mdskip.taobao.com/core/initItemDetail.htm?callback=setMdskip&itemId=" + numIid;
        String referer = "https://detail.tmall.com/item.htm?id=520523758791";
        String ua = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36";
        String json = SimpleHttpApi.directGet(url, referer, ua, null, null);
        if(!json.contains(setMdskip)){
            renderFailedJson("查询出现问题");
        }
        // 去掉setMdskip()
        json = json.substring(setMdskip.length() + 3, json.length() - 1);
        // 解析json
        JsonObject jsonObj = new JsonParser().parse(json).getAsJsonObject();
        JsonObject defaultModel = jsonObj.get("defaultModel").getAsJsonObject();
        JsonObject tradeResult = defaultModel.get("tradeResult").getAsJsonObject();
        long delistTime = tradeResult.get("startTime").getAsLong();
        renderSuccessJson(String.valueOf(delistTime));
    }
    
}
