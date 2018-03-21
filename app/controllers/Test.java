package controllers;

import com.ciaosir.client.Validator;

import bustbapi.*;

import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.word.IKSpliter;

import models.item.FrontCatPlay;
import models.item.ItemCatPlay;
import models.item.ItemPropSale;
import models.mysql.fengxiao.*;
import models.mysql.fengxiao.HotWordCount.WordCount;
import models.shop.TBShopPlay;
import models.user.User;
//import models.mysql.fengxiao.CatTopSaleItem.TopSaleItem;


//import models.mysql.item.CatTopSaleItem;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import javax.persistence.Query;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jdp.ApiJdpAdapter;
import play.db.jpa.JPA;
import play.mvc.Controller;
import proxy.CommonProxyPools;
import result.TMResult;
import spider.mainsearch.MainSearchApi;
import spider.mainsearch.MainSearchApi.TBSearchRes;
import underup.frame.industry.HotSearchWord;
import underup.frame.industry.HotShop;
//import underup.frame.industry.ItemCatJob;
import underup.frame.industry.ItemCatLevel2;
import underup.frame.industry.ItemsCatArrange;
import underup.frame.industry.ListTimeRange;
import underup.frame.industry.MonthInfo;
import underup.frame.industry.PriceDistribution;
import underup.frame.industry.UpdateItemsCatJob;
///import underup.frame.industry.Time;
import actions.catunion.UserIdNickAction;
import actions.listTaoBao.ListTaoBaoSpiderAction;
import autotitle.AutoSplit;
import autotitle.ItemPropAction;
import autotitle.ItemPropAction.PropUnit;
import bustbapi.BusAPI;
import bustbapi.ShopApi;
import bustbapi.ShopInfoSearchApi;
import underup.frame.industry.*;
import underup.frame.industry.PriceDistribution.PriceRange;
//import underup.frame.industry.Time.TimeList;
import bustbapi.ShopApi.ShopGet;

import com.ciaosir.client.item.ShopInfo;
import com.ciaosir.client.pojo.ItemThumb;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.commons.ClientException;
import com.google.gson.Gson;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.Shop;
import com.taobao.api.request.ShopGetRequest;
import com.taobao.api.request.UserGetRequest;
import com.taobao.api.response.ShopGetResponse;
import com.taobao.api.response.UserGetResponse;

import configs.TMConfigs;
import dao.UserDao;

public class Test extends Controller {
    private static final Logger log = LoggerFactory.getLogger(Test.class);

    public static void test() throws ApiException, ClientException {
//        String time = "1391184000000768960000026";
//        // List<ItemCatLevel2> list = ItemCatLevel2.getLevel2(time);
//        List<ItemCatLevel2> list = ItemsCatArrange.level2Arrange(time);
//        renderJSON(list);
//        new UpdateFrontCatsJob().doJob();
       // new CatTopSaleItem(12L, 13L, 14L, 20L).jdbcSave();
       // new CatTopSaleItem(12L, 13L, 14L, 30L).jdbcSave();

    }
    
    public static final void updateItemsCatJob(){
    	new UpdateItemsCatJob().doJob();
    }
    
    public static void testAlimama() {
    	renderText(CommonProxyPools.directGet("http://pub.alimama.com/items/search.json?q=%E8%93%9D%E5%B8%85%E9%9B%A8%E5%87%80%E7%81%B5%E6%B1%BD%E8%BD%A6%E6%8C%A1%E9%A3%8E%E7%8E%BB%E7%92%83%E9%98%B2%E9%9B%A8%E5%89%82%E5%90%8E%E8%A7%86%E9%95%9C%E8%BD%A6%E7%AA%97%E9%A9%B1%E6%B0%B4%E5%89%82%E6%8B%A8%E6%B0%B4%E9%95%80%E8%86%9C%E9%95%BF%E6%95%88%E9%9B%A8%E6%95%8C&_t=1517644006221&auctionTag=&perPageSize=50&shopTag=yxjh&t=1517644006225&_tb_token_=373e064b17eb&pvid=10_183.156.99.215_30991_1517644001256",
    			"http://pub.alimama.com", "", null, null));
    }
    
    public static void testActivityId(String itemId) {
    	String url = "https://detailskip.taobao.com/service/getData/1/p1/item/detail/sib.htm?callback=onSibRequestSuccess&modules=couponActivity&itemId=" + itemId;
    	renderText(CommonProxyPools.directGet(url,
    			"https://item.taobao.com/", "", null, null));
    }
}
