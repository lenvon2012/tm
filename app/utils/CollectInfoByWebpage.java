package utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import models.user.User;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.jobs.Job;
import spider.ItemThumbSecond;
import spider.mainsearch.MainSearchApi;
import spider.mainsearch.MainSearchApi.MainSearchParams;
import spider.mainsearch.MainSearchApi.TBSearchRes;
import spider.mainsearch.MainSearchKeywordsUpdater.MainSearchCache;
import spider.mainsearch.MainSearchKeywordsUpdater.MainSearchItemRank;
import actions.CallableThreadOfAgent;
import bustbapi.TMApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.api.SimpleHttpApi;
import com.ciaosir.client.pojo.ItemThumb;

import controllers.TMController;

public class CollectInfoByWebpage extends Job {
    
    private static final Logger log = LoggerFactory.getLogger(CollectInfoByWebpage.class);
    
    final static int ItemGetThreadSize = 32;
//    static PYFutureTaskPool<ItemThumbSecond> itemGetPool = new PYFutureTaskPool<ItemThumbSecond>(ItemGetThreadSize);
    static PYFutureTaskPool<String> itemGetPoolOfAgent = new PYFutureTaskPool<String>(ItemGetThreadSize);
    public static final String NEWTAOBAOSEARCHJSONSTART = "g_page_config";
    
    //item中包含下架时间(如：2015-11-20 10:20:46)、宝贝标题、图片地址和下架时间戳(如：1447986046)
    public static final int ItemSize = 4;
    
    
    //获取下架信息并存于Cache中
    public static Map<Long, MainSearchItemRank> getDelistInfoMap(String SearchKey, 
            String itemOrderType, String searchPlace, int searchPages, int pn, User user, UserCache usercache) {
        
        Map<Long, MainSearchItemRank> map = new HashMap<Long, MainSearchItemRank>();
        
//        UserCache usercache = TMController.checkEndTimeCache(user.getId());
        //当前用户正在查询，防止相同用户在不同客户端同时查询
        usercache.setSearch(true);
        Cache.set(Long.toString(user.getId()), usercache, "30mn");

        //检查是否是在内存中获取而不是重新查询数据
        boolean flag = checkMapOfCache(SearchKey, itemOrderType, searchPlace, searchPages);

        for(int i=0;i<searchPages;i++) {
            //获取用于Cache中的key
            String key = MainSearchCache.genCacheKey(new MainSearchParams(SearchKey, i+1, itemOrderType, searchPlace));

            Map<Long, MainSearchItemRank> mapOfCache = MainSearchCache.getMainSearchFromCache(key);

            if (!CommonUtils.isEmpty(mapOfCache)) {
                map.putAll(mapOfCache);
                if((i+1) == searchPages) {
//                    TMController.setSearched(user.getId());
                    usercache.setSearch(false);
                    Cache.set(Long.toString(user.getId()), usercache);
                    return map;
                }
            }
            //页数变化时，进入Cache中获取数据而不是去页面获取
            if(flag) {
                continue;
            }
            //内存中不存在数据去网页上获取
            else {
                //限制时间为60000ms
                if ((usercache.getRunningEndTime() + 60000) > System.currentTimeMillis()) {
                    Map<Long, MainSearchItemRank> newMap = new HashMap<Long, MainSearchItemRank>();
                    usercache.setSearch(false);
                    Cache.set(Long.toString(user.getId()), usercache);
                    return newMap;
                }
                if ("tb".equals(searchPlace)) {
                    //TB搜索页面显示44个
                    String refer = "https://www.taobao.com";
                    int Tbpagesize = 44;
                    //人气排序
                    if (itemOrderType == "renqi-desc") {
                        String SearchUrl = "https://s.taobao.com/search?q="+ SearchKey
                                + "js=1&stats_click=search_radio_all%3A1&initiative_id=staobaoz_20151010&ie=utf8&sort=renqi-desc"
                                + "&bcoffset=-5&ntoffset=-5&p4plefttype=3%2C1&p4pleftnum=1%2C3&s="+(Tbpagesize*i);

                        List<ItemThumbSecond> itemThumbSecond = MainSearchApi.parseSearchUrl(null, SearchUrl, refer, null);
                        if(CommonUtils.isEmpty(itemThumbSecond)) {
                            continue;
                        }
                        Map<Long, MainSearchItemRank> mapOfWebPage = getDetailPageMapByMobile(itemThumbSecond, SearchKey, itemOrderType, searchPlace, i+1, pn);
                        map.putAll(mapOfWebPage);
//                        String content = MainSearchApi.parseSearchUrl(null, SearchUrl, refer, null);
//                        while(StringUtils.isEmpty(content)&&retry++<3) {
//                            content = MainSearchApi.parseSearchUrl(null, SearchUrl, refer, null);
//                        }
//                        if(!StringUtils.isEmpty(content)) {
//                            List<String> itemNid = getTbNidByAgent(content);
//                            Map<Long, MainSearchItemRank> mapOfWebPage = getOnePageMapByAgent(itemNid, SearchKey, itemOrderType, searchPlace, i+1, pn);
//                            map.putAll(mapOfWebPage);
//                        }
                    }
                    //综合排序
                    else {
                        String SearchUrl = "https://s.taobao.com/search?q="+ SearchKey
                                + "&commend=all&ssid=s5-e&search_type=item&sourceId=tb.index&spm=a21bo.7724922.8452-taobao-item.2&initiative_id=tbindexz_20151010"
                                + "&bcoffset=-5&ntoffset=-5&p4plefttype=3%2C1&p4pleftnum=1%2C3&s="+(Tbpagesize*i);
                        
                        List<ItemThumbSecond> itemThumbSecond = MainSearchApi.parseSearchUrl(null, SearchUrl, refer, null);
                        if(CommonUtils.isEmpty(itemThumbSecond)) {
                            continue;
                        }
                        Map<Long, MainSearchItemRank> mapOfWebPage = getDetailPageMapByMobile(itemThumbSecond, SearchKey, itemOrderType, searchPlace, i+1, pn);
                        map.putAll(mapOfWebPage);
                    }
                }
                //进入TM中搜索
                else if ("tm".equals(searchPlace)) {
                    String refer = "https://www.tmall.com";
                    //TM搜索页面显示60个
                    int Tmpagesize = 60;
                    
                    if (itemOrderType == "renqi-desc") {
//                        String SearchUrl = "https://list.tmall.com/search_product.htm?spm=a220m.1000858.1000724.2.G8ngWs&s="
//                                + (Tmpagesize * i) + "&q=" + SearchKey + "&sort=rq&style=g";
                        String SearchUrl = "https://list.tmall.com/search_product.htm?s=" + (Tmpagesize * i) + "&q=" + SearchKey + "&sort=rq";
                        
                        List<ItemThumbSecond> itemThumbSecond = MainSearchApi.parseSearchUrl(null, SearchUrl, refer, null);
                        if(CommonUtils.isEmpty(itemThumbSecond)) {
                            continue;
                        }
                        Map<Long, MainSearchItemRank> mapOfWebPage = getDetailPageMapByMobile(itemThumbSecond, SearchKey, itemOrderType, searchPlace, i+1, pn);
                        map.putAll(mapOfWebPage);
                    }
                    else {
//                        String SearchUrl = "https://list.tmall.com/search_product.htm?spm=a220m.1000858.1000724.10.45Kwmh&s="+ (Tmpagesize * i)
//                                + "&q="+ SearchKey + "&sort=s&style=g&from=mallfp..pc_1_searchbutton&smAreaId=330100&tmhkmain=0&type=pc#J_Filter";
                        String SearchUrl = "https://list.tmall.com/search_product.htm?s="+ (Tmpagesize * i) + "&q="+ SearchKey;
                        
                        List<ItemThumbSecond> itemThumbSecond = MainSearchApi.parseSearchUrl(null, SearchUrl, refer, null);
                        if(CommonUtils.isEmpty(itemThumbSecond)) {
                            continue;
                        }
                        Map<Long, MainSearchItemRank> mapOfWebPage = getDetailPageMapByMobile(itemThumbSecond, SearchKey, itemOrderType, searchPlace, i+1, pn);
                        map.putAll(mapOfWebPage);
                    }
                }
            }
        }

        usercache.setFlag(true);
        usercache.setRunningEndTime(System.currentTimeMillis());
        usercache.setSearch(false);
        Cache.set(Long.toString(user.getId()), usercache);
        
        return map;
    }

    public static boolean checkMapOfCache(String SearchKey,String itemOrderType, 
            String searchPlace, int searchPages) {
        boolean flag = false;
        
        for(int i = searchPages/2;i<searchPages;i++) {
            String key = MainSearchCache.genCacheKey(new MainSearchParams(SearchKey, i+1, itemOrderType, searchPlace));
            Map<Long, MainSearchItemRank> mapOfCache = MainSearchCache.getMainSearchFromCache(key);
            if(!CommonUtils.isEmpty(mapOfCache)) {
                flag = true;
            }
        }
        
        return flag;
    }
    
    public static Map<Long, MainSearchItemRank> getDetailPageMapByMobile(List<ItemThumbSecond> itemThumbSecond, String SearchKey,
            String itemOrderType, String searchPlace, int searchPages, int pn) {
        Map<Long, MainSearchItemRank> map = new HashMap<Long, MainSearchItemRank>();
        
        String key = MainSearchCache.genCacheKey(new MainSearchParams(SearchKey, searchPages, itemOrderType, searchPlace));
        
        //用于分页
        int pageSize = 40;
        
        for(int i=0;i<itemThumbSecond.size();i++) {
            ItemThumbSecond itemRank = new ItemThumbSecond();
            
            itemRank.setdelistTimes(timeStamp(itemThumbSecond.get(i).getdelistTimes().toString()));
            itemRank.setFullTitle(itemThumbSecond.get(i).getFullTitle().toString());
            itemRank.setPicPath(itemThumbSecond.get(i).getPicPath().toString());
            itemRank.setdelistTimestamp(checkEnds(itemThumbSecond.get(i).getdelistTimestamp().toString()));
            itemRank.setId(itemThumbSecond.get(i).getId());
            
            int rank = (pn-1)*pageSize + i + 1;
            MainSearchItemRank rankbase = new MainSearchItemRank(itemRank, rank);
            map.put(itemThumbSecond.get(i).getId(), rankbase);
        }
        
        MainSearchCache.putIntoCache(key, map);
        
        return map;
    }
    
    public static List<ItemThumb> parseJsonWebContent(String content) {
        List<ItemThumb> itemThumbs = new ArrayList<ItemThumb>();
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, ItemThumb.class);
            itemThumbs = mapper.readValue(content, javaType);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        
        return itemThumbs;
    }
    
    public static String assembleDetailPageUrl(int itemSize, Long numIid) {
        String searchRrl = null;
        //TB
        if(itemSize<=48) {
            searchRrl = "https://item.taobao.com/item.htm?id=" + numIid.toString();
        }else { //TM
            searchRrl = "https://detail.tmall.com/item.htm?id=" + numIid.toString();
        }
        return searchRrl;
    }
    
    public static String checkEnds(String str) {
        for(int i=0;i<str.length();i++) {
            if(!(str.charAt(i)>='0'&&str.charAt(i)<='9')) {
                str = Long.toString(System.currentTimeMillis()).substring(0, 10);
            }
        }
        return str;
    }
    
    public static String timeStamp(String str) {
        str = checkEnds(str);
        Long timestamp = Long.parseLong(str) * 1000;
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
        return date;
    }
    
}