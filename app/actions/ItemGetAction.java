package actions;

import actions.itemcopy.apidataparser.WDetailV6Parser;
import actions.itemcopy.model.*;
import autotitle.ItemPropAction.PropUnit;
import bustbapi.LogisticsApi;
import bustbapi.TBApi;
import carrier.FileCarryUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.JsonUtil;
import com.taobao.api.ApiException;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.*;
import com.taobao.api.request.ItemSkusGetRequest;
import com.taobao.api.request.ItempropsGetRequest;
import com.taobao.api.response.ItemSkusGetResponse;
import com.taobao.api.response.ItempropsGetResponse;
import models.itemCopy.ItemExt;
import models.itemCopy.PriceUnit;
import models.user.User;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Play;
import play.cache.Cache;
import play.libs.WS;
import proxy.CommonProxyPools;
import proxy.NewProxyTools;
import result.TMResult;
import spider.mainsearch.MainSearchApi;
import utils.CommonUtil;
import utils.NewProxyToolsUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hao on 15-4-8.
 * 用于从页面获取Item信息
 */

public class ItemGetAction {
	private static final Logger log = LoggerFactory.getLogger(ItemGetAction.class);
	public static Item getSimpleItem(Long numIid) {
		if(numIid == null || numIid <= 0L) {
    		return null;
    	}
		Item item = new Item();
		item.setNumIid(numIid);
		Date curDate = new Date();
        item.setListTime(curDate);
        item.setDelistTime(new Date(System.currentTimeMillis() + DateUtil.DAY_MILLIS * 7));
        String wapUrl = "http://hws.m.taobao.com/cache/wdetail/5.0/?id=" + numIid + "&ttid=2013@taobao_h5_1.0.0";
        String string = WS.url(wapUrl).get().getString();
        JSONObject jsonObject = JSON.parseObject(string);
        JSONObject data = jsonObject.getJSONObject("data");

        JSONObject itemInfoModel = data.getJSONObject("itemInfoModel");
        if(itemInfoModel != null){
            Long cid = itemInfoModel.getLong("categoryId");
            item.setCid(cid);
        }

        JSONObject seller = data.getJSONObject("seller");
        String nick = seller.getString("nick");
        item.setNick(nick);

        JSONArray propsArr = data.getJSONArray("props");
        List<PropUnit> res = new ArrayList<PropUnit>();
        if (propsArr != null && !propsArr.isEmpty()) {
        	 /*Map<String, String> props = getProps(cid, propsArr, new JSONArray(), new FoodSecurity());
        	 String propsName = props.get("props_name");
        	 if(StringUtils.isEmpty(propsName) == false) {
        		 String[] propArr = propsName.split(";");
        		 if(propArr == null || propArr.length <= 0) {
        			 // do nothing
        		 } else {
        			 for(String s : propArr) {
        				 if(StringUtils.isEmpty(s)) {
        					 continue;
        				 }
        				 String[] prop = s.split(":"); 
        				 if(prop == null || prop.length != 4) {
        					 continue;
        				 }
        				 try {
							res.add(new PropUnit(Long.valueOf(prop[0]), Long.valueOf(prop[1]), prop[2], prop[2]));
 						 } catch (Exception e) {
						 	// TODO: handle exception
						 }
        				 
        			 }
        		 }
        		 
        	 }
        	 item.setProps(JsonUtil.getJson(res));*/
        	for(int i = 0; i < propsArr.size(); i++) {
        		JSONObject object = propsArr.getJSONObject(i);
        		res.add(new PropUnit(1111, 1111, object.getString("name"), object.getString("value")));
        	}
        	item.setProps(JsonUtil.getJson(res));
        }

        return item;
	}

    public static Item getSimpleItemNew(Long numIid){
        int attemptCount = 6;
        Item item = new Item();
        item.setNumIid(numIid);
        Date curDate = new Date();
        item.setListTime(curDate);
        item.setDelistTime(new Date(System.currentTimeMillis() + DateUtil.DAY_MILLIS * 7));
        String wapUrl = "http://hws.m.taobao.com/cache/wdetail/5.0/?id=" + numIid + "&ttid=2013@taobao_h5_1.0.0";
        String json = null;
        while(attemptCount > 0){
            json = MainSearchApi.directGet(wapUrl, null, null, null, null);
            if(!StringUtils.isEmpty(json)){
                break;
            }
            log.error("----------------第"+ (7 - attemptCount) +"尝试失败。numIid " + numIid + " 返回的结果为空");
            CommonUtils.sleepQuietly(1000L);
            attemptCount--;
        }
        if(StringUtils.isEmpty(json)){
            return null;
        }
        // 解析json
        JSONObject jsonObject = JSON.parseObject(json);
        JSONObject data = jsonObject.getJSONObject("data");

        JSONObject itemInfoModel = data.getJSONObject("itemInfoModel");
        if(itemInfoModel != null){
            Long cid = itemInfoModel.getLong("categoryId");
            item.setCid(cid);
        }

        if(!data.containsKey("seller")){
        	log.error("---------------------获取宝贝详情出错");
        	return null;
        }
        JSONObject seller = data.getJSONObject("seller");
        String nick = seller.getString("nick");
        item.setNick(nick);

        JSONArray propsArr = data.getJSONArray("props");
        List<PropUnit> res = new ArrayList<PropUnit>();
        if (propsArr != null && !propsArr.isEmpty()) {
            for(int i = 0; i < propsArr.size(); i++) {
                JSONObject object = propsArr.getJSONObject(i);
                res.add(new PropUnit(1111, 1111, object.getString("name"), object.getString("value")));
            }
            item.setProps(JsonUtil.getJson(res));
        }
        return item;
    }

    public static Item getItem(String sid, long numIid) {
        Item item = new Item();
        Date curDate = new Date();
        item.setListTime(curDate);
        item.setNumIid(numIid);
        item.setApproveStatus("onsale");
        item.setModified(curDate);
        item.setHasShowcase(true);
        item.setIsVirtual(false);
        item.setStuffStatus("new");
        item.setType("fixed");


        String wapUrl = "http://hws.m.taobao.com/cache/wdetail/5.0/?id=" + numIid + "";
        String detailUrl = "http://item.taobao.com/item.htm?id=" + numIid;
        item.setDetailUrl(detailUrl);

        JSONObject data = getDataObject(wapUrl, "http://hws.m.taobao.com");

        if (data == null) {
            return null;
        }

        String apiStack = data.getString("apiStack");

        String price = getPrice(data);
        log.warn("------>price: " + price);
        if (price != null) {
            item.setPrice(price);
        }

        Pattern quantityPattern = Pattern.compile("\\\\\"quantity\\\\\":\\\\\"(\\d+)\\\\\"");
        Matcher matcher = quantityPattern.matcher(apiStack);
        if (matcher.find()) {
            String num = matcher.group(1);
            item.setNum(Long.parseLong(num));
        }

        JSONObject itemInfoModel = data.getJSONObject("itemInfoModel");
        String title = itemInfoModel.getString("title");
        item.setTitle(title);

        Long cid = itemInfoModel.getLong("categoryId");
        item.setCid(cid);

        Location local = new Location();
        String city = "温州";
        String state = "浙江";
        // api获取卖家默认发货地址
        List<AddressResult> addressList = new LogisticsApi.LogisticsAddressSearch(sid, "get_def").call();
        if(addressList != null) {
        	AddressResult address = addressList.get(0);
        	city = address.getCity() == null? city : address.getCity();
        	if(city.endsWith("市")) {
        		city = city.substring(0, city.length() - 1);
        	}
        	if(city.endsWith("地区")) {
        		city = city.substring(0, city.length() - 2);
        	}
        	// 自治州
        	if(city.endsWith("自治州")) {
        		if(city.indexOf("伊犁") >= 0) {
        			city = "伊犁";
        		} else if (city.indexOf("克孜勒苏柯尔克孜") >= 0) {
					city = "克孜勒苏柯尔克孜";
				} else {
					// 昌吉回族自治州 博尔塔拉蒙古自治州 巴音郭楞蒙古自治州
					city = city.substring(0, city.length() - 5);
				}
        	}
        	state = address.getProvince() == null? state : address.getProvince();
        	if(state.endsWith("省")) {
        		state = state.substring(0, state.length() - 1);
        	}
        	// 香港 澳门
        	if(state.endsWith("行政区")) {
        		state = state.substring(0, 2);
        	}
        	// 自治区
        	if(state.endsWith("自治区")) {
        		if(state.indexOf("内蒙古") >= 0) {
            		state = "内蒙古";
            	} else {
            		// 广西壮族自治区 西藏自治区 宁夏回族自治区 新疆维吾尔自治区
            		state = state.substring(0, 2);
				}
        	}
        }
        local.setCity(city);
        local.setState(state);
        item.setLocation(local);

        List<ItemImg> imgs = new ArrayList<ItemImg>();
        JSONArray picsPath = itemInfoModel.getJSONArray("picsPath");
        for (int i = 0; i < picsPath.size(); i++) {
            ItemImg img = new ItemImg();
            String itemImgStr = picsPath.getString(i);
            img.setUrl(itemImgStr);
            img.setCreated(curDate);
            img.setPosition((long)(i + 1));
            imgs.add(img);
        }
        item.setItemImgs(imgs);
        String picUrl = itemInfoModel.getJSONArray("picsPath").getString(0);
        item.setPicUrl(picUrl);

        JSONObject seller = data.getJSONObject("seller");
        String nick = seller.getString("nick");
        item.setNick(nick);

        JSONArray propsArr = data.getJSONArray("props");

        boolean hasSku = itemInfoModel.getBooleanValue("sku");
        JSONArray skuProps = null;
        JSONObject ppathIdmap = null;
        JSONObject skusFromDate = null;
        if (hasSku) {
            JSONObject skuModel = data.getJSONObject("skuModel");
            // skuProps, 为了获取身高颜色信息
            skuProps = skuModel.getJSONArray("skuProps");
            ppathIdmap = skuModel.getJSONObject("ppathIdmap");
            String defDynString = data.getJSONObject("extras").getString("defDyn");
            JSONObject defDyn = JSON.parseObject(defDynString);
            skusFromDate = defDyn.getJSONObject("skuModel").getJSONObject("skus");

            List<Sku> skus = getSkus(String.valueOf(numIid));
            checkWithTemplet(getTempSaleProp(getTempletProps(cid, null)), skus);
            item.setSkus(skus);
        }
        if (skuProps != null && !skuProps.isEmpty()) {
            List<PropImg> propImgs = new ArrayList<PropImg>();
            for (int i = 0; i < skuProps.size(); i++) {
                JSONObject propJsonObj = skuProps.getJSONObject(i);
                String propId = propJsonObj.getString("propId");
                JSONArray propDetails = propJsonObj.getJSONArray("values");

                for (int i1 = 0; i1 < propDetails.size(); i1++) {
                    JSONObject detail = propDetails.getJSONObject(i1);
                    String imgUrl = detail.getString("imgUrl");
                    if (StringUtils.isBlank(imgUrl)) {
                        continue;
                    }
                    PropImg img = new PropImg();
                    String valueId = detail.getString("valueId");
                    img.setProperties(propId + ":" + valueId);
                    img.setUrl(detail.getString("imgUrl"));
                    img.setCreated(curDate);
                    img.setPosition((long) (i1 + 1));
                    propImgs.add(img);
                }
            }
            item.setPropImgs(propImgs);
        }
        FoodSecurity foodSecurity = new FoodSecurity();

        if (propsArr != null && !propsArr.isEmpty()) {
            Map<String, String> props = getProps2(cid, numIid, propsArr, skuProps, ppathIdmap, skusFromDate);
            item.setProps(props.get("props"));
            item.setPropsName(props.get("props_name"));
            item.setPropertyAlias(props.get("property_alias"));
            item.setInputPids(props.get("input_pids"));
            item.setInputStr(props.get("input_str"));

            for (int j = 0; j < propsArr.size(); j++) {
                JSONObject gotProp = propsArr.getJSONObject(j);
                String gotName = gotProp.getString("name");
                String gotValue = gotProp.getString("value");
                getFoodSecurity(gotName, gotValue, foodSecurity);
            }
            item.setFoodSecurity(foodSecurity);
        }
        fixItemStuffStatus(item);

        JSONObject descInfo = data.getJSONObject("descInfo");
        String fullDescUrl = descInfo.getString("fullDescUrl");

        try{
            JSONObject jsonObject = getDataObject(fullDescUrl, "http://hws.m.taobao.com");
            if (jsonObject == null) {
                return null;
            }

            String desc = jsonObject.getString("desc");
            desc = desc.replaceAll("</?(html|body|head)>", "").trim();
            item.setDesc(desc);
        }catch(Exception e){
            log.warn(e.getMessage(), e);
        }

        String wirelessDesUrl = "http://hws.m.taobao.com/cache/mtop.wdetail.getItemDescx/4.1/?data=%7B%22item_num_id%22%3A%22" + numIid + "%22%7D";

        try {
            JSONObject jsonObject = getDataObject(wirelessDesUrl, "http://www.taobao.com");
            if (jsonObject == null) {
                return null;
            }
            JSONObject wirelessDescObj = jsonObject;
            JSONArray pages = jsonObject.getJSONArray("pages");
            StringBuilder wirelessDescSb = new StringBuilder();
            wirelessDescSb.append("<wapDesc><shortDesc>").append(wirelessDescObj.getString("summary")).append("</shortDesc>");
            for (Object page : pages) {
                wirelessDescSb.append(page);
            }
            wirelessDescSb.append("</wapDesc>");

            String wireless_desc = wirelessDescSb.toString();
            item.setWirelessDesc(wireless_desc);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }

        return item;
    }

    public static Item getItemExt(String sid, long numIid) {
        return getItemExt(sid, numIid, null, null, null);
    }

    public static Item getItemExt(String sid, long numIid, String wapData, String descData, String wirelessData) {
        ItemExt item = new ItemExt();
        Date curDate = new Date();
        item.setListTime(curDate);
        item.setNumIid(numIid);
        item.setApproveStatus("instock");
        item.setModified(curDate);
        item.setHasShowcase(true);
        item.setIsVirtual(false);
        item.setStuffStatus("new");
        item.setType("fixed");
        item.setOuterId("https://item.taobao.com/item.htm?id=" + numIid);

        String detailUrl = "http://item.taobao.com/item.htm?id=" + numIid;
        item.setDetailUrl(detailUrl);

        JSONObject data;
        if (StringUtils.isEmpty(wapData)) {
            String wapUrl = "http://hws.m.taobao.com/cache/wdetail/5.0/?id=" + numIid + "";
            String referer = "http://hws.m.taobao.com";
            String userAgent = NewProxyTools.MOBILE_UA;
            String resultHead = "{\"api\":\"wdetail\",\"v\":\"5.0\",\"ret\":[\"SUCCESS::调用成功\"]";
            data = getDataObject(wapUrl, referer, userAgent, resultHead);
        } else {
            data = parseResultJson(wapData);
        }

        if (data == null) return null;

        String apiStack = data.getString("apiStack");

        String price = getPrice(data);
        log.warn("------>price: " + price);
        if (price != null) {
            item.setPrice(price);
        }

        Pattern quantityPattern = Pattern.compile("\\\\\"quantity\\\\\":\\\\\"(\\d+)\\\\\"");
        Matcher matcher = quantityPattern.matcher(apiStack);
        if (matcher.find()) {
            String num = matcher.group(1);
            item.setNum(Long.parseLong(num));
        }

        JSONObject itemInfoModel = data.getJSONObject("itemInfoModel");
        String title = itemInfoModel.getString("title");
        item.setTitle(title);

        Long cid = itemInfoModel.getLong("categoryId");
        item.setCid(cid);

        item.setLocation(buildLocation(sid));

        List<ItemImg> imgs = new ArrayList<ItemImg>();
        JSONArray picsPath = itemInfoModel.getJSONArray("picsPath");
        for (int i = 0; i < picsPath.size(); i++) {
            ItemImg img = new ItemImg();
            String itemImgStr = picsPath.getString(i);
            img.setUrl(itemImgStr);
            img.setCreated(curDate);
            img.setPosition((long)(i + 1));
            imgs.add(img);
        }
        item.setItemImgs(imgs);
        String picUrl = itemInfoModel.getJSONArray("picsPath").getString(0);
        item.setPicUrl(picUrl);

        JSONObject seller = data.getJSONObject("seller");
        String nick = seller.getString("nick");
        item.setNick(nick);

        JSONArray propsArr = data.getJSONArray("props");

        boolean hasSku = itemInfoModel.getBooleanValue("sku");
        JSONArray skuProps = null;
        JSONObject ppathIdmap = null;
        JSONObject skus = null;
        if (hasSku) {
            JSONObject skuModel = data.getJSONObject("skuModel");
            // skuProps, 为了获取身高颜色信息
            skuProps = skuModel.getJSONArray("skuProps");
            ppathIdmap = skuModel.getJSONObject("ppathIdmap");
        }

        // 获取宝贝价格数据
        JSONArray apiStackArray = JSON.parseArray(apiStack);
        JSONObject esiMap = (JSONObject) apiStackArray.get(0);
        String esiStr = esiMap.getString("value");
        JSONObject esi = JSON.parseObject(esiStr);
        skus = esi.getJSONObject("data").getJSONObject("skuModel").getJSONObject("skus");
        JSONArray priceUnitsJson = esi.getJSONObject("data").getJSONObject("itemInfoModel").getJSONArray("priceUnits");
        PriceUnit[] priceUnitsForPrice = new PriceUnit[priceUnitsJson.size()];
        for (int i = 0; i < priceUnitsJson.size(); i++) {
            JSONObject o = (JSONObject) priceUnitsJson.get(i);
            String name = o.getString("name");
            String price1 = o.getString("price");
            String display = o.getString("display");
            if (price1.contains("-")) price1 = price1.substring(price1.indexOf("-") + 1);
            priceUnitsForPrice[i] = new PriceUnit().setName(name).setPrice(price1).setDisplay(display);
        }
        item.setPriceUnitsForPrice(priceUnitsForPrice);

        if (skuProps != null && !skuProps.isEmpty()) {
            List<PropImg> propImgs = new ArrayList<PropImg>();
            for (int i = 0; i < skuProps.size(); i++) {
                JSONObject propJsonObj = skuProps.getJSONObject(i);
                String propId = propJsonObj.getString("propId");
                JSONArray propDetails = propJsonObj.getJSONArray("values");

                for (int i1 = 0; i1 < propDetails.size(); i1++) {
                    JSONObject detail = propDetails.getJSONObject(i1);
                    String imgUrl = detail.getString("imgUrl");
                    if (StringUtils.isBlank(imgUrl)) {
                        continue;
                    }
                    PropImg img = new PropImg();
                    String valueId = detail.getString("valueId");
                    img.setProperties(propId + ":" + valueId);
                    img.setUrl(detail.getString("imgUrl"));
                    img.setCreated(curDate);
                    img.setPosition((long) (i1 + 1));
                    propImgs.add(img);
                }
            }
            item.setPropImgs(propImgs);
        }

        // 获取宝贝属性数据
        Map<String, String> props = getProps2(cid, numIid, propsArr, skuProps, ppathIdmap, skus);
        item.setProps(props.get("props"));
        item.setPropsName(props.get("props_name"));
        item.setPropertyAlias(props.get("property_alias"));
        item.setInputPids(props.get("input_pids"));
        item.setInputStr(props.get("input_str"));
        item.setInputCustomCpv(props.get("input_custom_cpv"));
        item.setSkuOuterIds(props.get("sku_outer_ids"));
        item.setSkuPrices(props.get("sku_prices"));
        item.setSkuProperties(props.get("sku_properties"));
        item.setSkuQuantities(props.get("sku_quantities"));
        // sku价格
        List<PriceUnit[]> list = getPriceUnitsForPrice(skus);
        item.setPriceUnitsForSkuPrice(list);

        FoodSecurity foodSecurity = new FoodSecurity();
        if (propsArr != null && !propsArr.isEmpty()) {
            Boolean setFoodSecurity = false;
            for (int j = 0; j < propsArr.size(); j++) {
                JSONObject gotProp = propsArr.getJSONObject(j);
                String gotName = gotProp.getString("name");
                String gotValue = gotProp.getString("value");
                if (getFoodSecurity(gotName, gotValue, foodSecurity)) setFoodSecurity = true;
            }
            if (setFoodSecurity) item.setFoodSecurity(foodSecurity);
        }
        fixItemStuffStatus(item);


        try{
            JSONObject jsonObject;
            if (StringUtils.isEmpty(descData)) {
                JSONObject descInfo = data.getJSONObject("descInfo");
                String fullDescUrl = descInfo.getString("fullDescUrl");
                jsonObject = getDataObject(fullDescUrl, "http://hws.m.taobao.com");
            } else {
                jsonObject = parseResultJson(descData);
            }
            if (jsonObject == null) return null;

            String desc = jsonObject.getString("desc");
            desc = desc.replaceAll("</?(html|body|head)>", "").trim();
            item.setDesc(desc);
        }catch(Exception e){
            log.warn(e.getMessage(), e);
        }

        // ItemCopyApiBase并没有使用到无线端的描述 先注释掉无线端描述的获取
        /*String wirelessDesUrl = "http://hws.m.taobao.com/cache/mtop.wdetail.getItemDescx/4.1/?data=%7B%22item_num_id%22%3A%22" + numIid + "%22%7D";

        try {
            JSONObject jsonObject;
            if(StringUtils.isEmpty(wirelessData)) {
                jsonObject = getDataObject(wirelessDesUrl, "http://www.taobao.com");
            } else {
                jsonObject = parseResultJson(wirelessData);
            }
            if (jsonObject == null) return null;

            JSONObject wirelessDescObj = jsonObject;
            JSONArray pages = jsonObject.getJSONArray("pages");
            StringBuilder wirelessDescSb = new StringBuilder();
            wirelessDescSb.append("<wapDesc><shortDesc>").append(wirelessDescObj.getString("summary")).append("</shortDesc>");
            for (Object page : pages) {
                wirelessDescSb.append(page);
            }
            wirelessDescSb.append("</wapDesc>");

            String wireless_desc = wirelessDescSb.toString();
            item.setWirelessDesc(wireless_desc);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }*/

        return item;
    }

    public static Item getItemExt2(String sid, long numIid) {
        return getItemExt2(sid, numIid, null, null, null);
    }

    public static Item getItemExt2(String sid, long numIid, String wapData, String descData, String wirelessData) {
        ItemExt item = new ItemExt();
        Date curDate = new Date();
        item.setListTime(curDate);
        item.setNumIid(numIid);
        item.setApproveStatus("instock");
        item.setModified(curDate);
        item.setHasShowcase(true);
        item.setIsVirtual(false);
        item.setStuffStatus("new");
        item.setType("fixed");
        item.setOuterId("https://item.taobao.com/item.htm?id=" + numIid);
        item.setDetailUrl("http://item.taobao.com/item.htm?id=" + numIid);
        item.setLocation(buildLocation(sid));
        // 请求wdetailAPI 并解析数据返回ResultBean
        ResultBean resultBean = new WDetailV6Parser(numIid).parse();
        if (resultBean == null) return null;
        // 设置商品价格 商品库存
        Map<String, SkuCoreInfo> skuCore = resultBean.getSkuCore();
        SkuCoreInfo itemCoreInfo = skuCore.get("0");
        item.setPrice(processPrice(itemCoreInfo.getOriginalPrice()));
        item.setNum(Long.valueOf(itemCoreInfo.getQuantity()));
        // 设置商品标题 商品cid 商家昵称
        item.setTitle(resultBean.getTitle());
        item.setCid(Long.valueOf(resultBean.getCategoryId()));
        item.setNick(resultBean.getSellerNick());
        // 设置商品主副图  商品属性图片
        List<String> images = resultBean.getImages();
        item.setItemImgs(buildItemImgs(images));
        item.setPicUrl(!images.isEmpty() ? images.get(0) : null);
        item.setPropImgs(buildPropImgs(resultBean.getSkuProp()));
        // 设置食品安全信息
        item.setFoodSecurity(buildFoodSecurity(resultBean.getProp()));
        // 设置宝贝属性数据
        String categoryId = resultBean.getCategoryId();
        List<PropInfo> resultBeanProp = resultBean.getProp();
        List<SkuPropInfo> resultBeanSkuProp = resultBean.getSkuProp();
        Map<String, String> itemProperty = new ItemProperty2(Long.valueOf(categoryId), numIid, resultBeanProp, resultBeanSkuProp, skuCore).build();
        item.setProps(itemProperty.get("props"));
        item.setPropsName(itemProperty.get("props_name"));
        item.setPropertyAlias(itemProperty.get("property_alias"));
        item.setInputPids(itemProperty.get("input_pids"));
        item.setInputStr(itemProperty.get("input_str"));
        item.setInputCustomCpv(itemProperty.get("input_custom_cpv"));
        item.setSkuOuterIds(itemProperty.get("sku_outer_ids"));
        item.setSkuPrices(itemProperty.get("sku_prices"));
        item.setSkuProperties(itemProperty.get("sku_properties"));
        item.setSkuQuantities(itemProperty.get("sku_quantities"));
        // 设置宝贝价格数据 用于自定义
        item.setPriceUnitsForPrice(buildPriceUnits(itemCoreInfo));
        item.setPriceUnitsForSkuPrice(buildSkuPriceUnits(skuCore));
        // 设置宝贝描述
        String desc = buildDesc(numIid);
        if (desc == null) return null;
        item.setDesc(desc);

        return item;
    }

    private static String buildDesc(Long numIid) {
        String url = "http://hws.m.taobao.com/cache/mtop.wdetail.getItemFullDesc/4.1/?data=%7B%22item_num_id%22%3A%22" + numIid + "%22%7D";
        JSONObject jsonObject = getDataObject(url, "http://hws.m.taobao.com");
        if (jsonObject == null) return null;

        String desc = jsonObject.getString("desc");
        desc = desc.replaceAll("</?(html|body|head)>", "").trim();
        return desc;
    }

    // 封装用于自定义的宝贝价格数据
    private static List<PriceUnit[]> buildSkuPriceUnits(Map<String, SkuCoreInfo> skuCore) {
        List<PriceUnit[]> priceUnitsList = new ArrayList<PriceUnit[]>();
        if (skuCore != null && !skuCore.isEmpty()) {
            Set<Map.Entry<String, SkuCoreInfo>> entries = skuCore.entrySet();
            for (Map.Entry<String, SkuCoreInfo> entry : entries) {
                String skuId = entry.getKey();
                if (!skuId.equals("0")) {
                    SkuCoreInfo skuCoreInfo = entry.getValue();
                    priceUnitsList.add(buildPriceUnits(skuCoreInfo));
                }
            }
        }
        return priceUnitsList;
    }

    // 封装用于自定义的宝贝价格数据
    private static PriceUnit[] buildPriceUnits(SkuCoreInfo skuCoreInfo) {
        String skuId = skuCoreInfo.getSkuId();
        String salePrice = skuCoreInfo.getSalePrice();
        String originalPrice = skuCoreInfo.getOriginalPrice();
        if (salePrice != null && originalPrice != null) {
            PriceUnit[] priceUnits = new PriceUnit[2];
            priceUnits[0] = new PriceUnit("售价", salePrice, skuId);
            priceUnits[1] = new PriceUnit("原价", originalPrice, skuId);
            return priceUnits;
        } else if (salePrice == null && originalPrice != null) {
            PriceUnit[] priceUnits = new PriceUnit[1];
            priceUnits[0] = new PriceUnit("原价", originalPrice, skuId);
            return priceUnits;
        }

        return null;
    }

    // 处理价格数据  123-321
    private static String processPrice(String price) {
        int index = price.indexOf("-");
        return index == -1 ? price : price.substring(index + 1);
    }

    // 创建食品安全信息
    private static FoodSecurity buildFoodSecurity(List<PropInfo> prop) {
        FoodSecurity foodSecurity = new FoodSecurity();
        Boolean setFoodSecurity = false;
        if (prop != null && !prop.isEmpty()) {
            for (PropInfo propInfo : prop) {
                String name = propInfo.getName();
                String value = propInfo.getValue();
                if (getFoodSecurity(name, value, foodSecurity)) setFoodSecurity = true;
            }
        }
        return setFoodSecurity ? foodSecurity : null;
    }

    // 创建商品属性图片
    private static List<PropImg> buildPropImgs(List<SkuPropInfo> skuProp) {
        List<PropImg> propImgs = new ArrayList<PropImg>(8);
        if (skuProp != null && !skuProp.isEmpty()) {
            Date currentDate = new Date();
            for (SkuPropInfo skuPropInfo : skuProp) { //遍历属性
                String pid = skuPropInfo.getPid();
                List<SkuPropValueInfo> values = skuPropInfo.getValues();
                if (values != null && !values.isEmpty()) {
                    for (int i = 0; i < values.size(); i++) { // 遍历属性值
                        SkuPropValueInfo valueInfo = values.get(i);
                        String image = valueInfo.getImage();
                        if (StringUtils.isNotEmpty(image)) { // 对含有图片的属性值操作
                            PropImg propImg = new PropImg();
                            propImg.setUrl(image);
                            propImg.setProperties(pid + valueInfo.getVid());
                            propImg.setPosition(Long.valueOf(i + 1));
                            propImg.setCreated(currentDate);
                            propImgs.add(propImg);
                        }
                    }
                }
            }
        }
        return propImgs;
    }

    // 创建商品图片
    private static List<ItemImg> buildItemImgs(List<String> images) {
        List<ItemImg> itemImgs = new ArrayList<ItemImg>(images.size());
        if (images != null) {
            Date currentDate = new Date();
            for (int i = 0; i < images.size(); i++) {
                String imageUrl = images.get(i);
                ItemImg itemImg = new ItemImg();
                itemImg.setPosition(Long.valueOf(i + 1));
                itemImg.setUrl(imageUrl);
                itemImg.setCreated(currentDate);
                itemImgs.add(itemImg);
            }
        }

        return itemImgs;
    }

    // 创建宝贝所在地
    private static Location buildLocation(String sid) {
        Location local = new Location();
        String city = "温州";
        String state = "浙江";
        // api获取卖家默认发货地址
        List<AddressResult> addressList = new LogisticsApi.LogisticsAddressSearch(sid, "get_def").call();
        if(addressList != null) {
            AddressResult address = addressList.get(0);
            if (StringUtils.isNotEmpty(address.getCity()) && StringUtils.isNotEmpty(address.getProvince())) {
                city = address.getCity();
                state = address.getProvince();
            }
            if(city.endsWith("市")) {
                city = city.substring(0, city.length() - 1);
            }
            if(city.endsWith("地区")) {
                city = city.substring(0, city.length() - 2);
            }
            // 自治州
            if(city.endsWith("自治州")) {
                if(city.indexOf("伊犁") >= 0) {
                    city = "伊犁";
                } else if (city.indexOf("克孜勒苏柯尔克孜") >= 0) {
                    city = "克孜勒苏柯尔克孜";
                } else {
                    // 昌吉回族自治州 博尔塔拉蒙古自治州 巴音郭楞蒙古自治州
                    city = city.substring(0, city.length() - 5);
                }
            }
            if(state.endsWith("省")) {
                state = state.substring(0, state.length() - 1);
            }
            // 香港 澳门
            if(state.endsWith("行政区")) {
                state = state.substring(0, 2);
            }
            // 自治区
            if(state.endsWith("自治区")) {
                if(state.indexOf("内蒙古") >= 0) {
                    state = "内蒙古";
                } else {
                    // 广西壮族自治区 西藏自治区 宁夏回族自治区 新疆维吾尔自治区
                    state = state.substring(0, 2);
                }
            }
        }
        local.setCity(city);
        local.setState(state);

        return local;
    }

    // 封装PriceUnitsForPrice
    private static List<PriceUnit[]> getPriceUnitsForPrice(JSONObject skus) {
        List<PriceUnit[]> list = new ArrayList<PriceUnit[]>();
        if (skus != null) {
            Collection<Object> values = skus.values();
            // 价格的类型数量  无库存的有可能会缺少折扣价
            Integer realPriceTypeNum = null;
            for (Object o : values) {
                Map sku = (Map) o;
                List<Map> priceUnits = (List<Map>) sku.get("priceUnits");
                if (realPriceTypeNum == null) realPriceTypeNum = priceUnits.size();
                Integer quantity = Integer.valueOf((String) sku.get("quantity"));
                if (quantity > 0) {
                    realPriceTypeNum = priceUnits.size();
                    break;
                }
            }
            for (Object o : values) {
                Map sku = (Map) o;
                List<Map> priceUnits = (List<Map>) sku.get("priceUnits");

                // 有些价格属性会缺少一个折扣价 只有一个原价 这种数据直接过滤掉
                if (priceUnits.size() != realPriceTypeNum) continue;

                PriceUnit[] priceUnits$ = new PriceUnit[realPriceTypeNum];
                for (int i = 0; i < priceUnits.size(); i++) {
                    Map map = priceUnits.get(i);
                    String name = (String) map.get("name");
                    String price = (String) map.get("price");
                    String display = (String) map.get("display");
                    if (price.contains("-")) price = price.substring(price.indexOf("-") + 1);

                    priceUnits$[i] = new PriceUnit().setName(name).setPrice(price)
                            .setDisplay(display);
                }
                list.add(priceUnits$);
            }
        }
        return list;
    }

    // 修正新旧程度  如果props中有成色(20879)这个属性且属性值不是全新(21456)，则将新旧程度设置成second
    private static void fixItemStuffStatus(Item item) {
        String props = item.getProps();
        if (StringUtils.isEmpty(props)) return;
        String[] propsArray = props.split(";");
        for (String prop : propsArray) {
            String[] pidAndVid = prop.split(":");
            String pid = pidAndVid[0];
            if (pid.equals("20879")) {
                String vid = pidAndVid[1];
                if (!vid.equals("21456")) {
                    item.setStuffStatus("second");
                }
            }
        }
    }

    public static String getPrice(JSONObject data) {
        JSONObject apiStack = data.getJSONArray("apiStack").getJSONObject(0);

        String value = apiStack.getString("value");
        JSONObject apiStackValue = JSON.parseObject(value);

        JSONArray priceUnitArr = apiStackValue.getJSONObject("data").getJSONObject("itemInfoModel").getJSONArray(
                "priceUnits");

        if (priceUnitArr.size() == 0) {
            return null;
        }

        Pattern pattern = Pattern.compile("\"price\":\"(.+?)\"");

        String priceUnit ;
        if (priceUnitArr.size() == 1) {
            priceUnit = priceUnitArr.getString(0);
        } else {
            priceUnit = priceUnitArr.getString(1);
        }
        log.warn("--------->" + priceUnit);
        Matcher matcher = pattern.matcher(priceUnit);
        if (matcher.find()) {
            String price = matcher.group(1);
            if (price.contains("-")) {
                return price.split("-")[1];
            } else {
                return price;
            }
        }

        return null;
    }

	public static JSONObject getDataObject(String url, String referer) {
		return getDataObject(url, referer, StringUtils.EMPTY, StringUtils.EMPTY);
	}

	public static JSONObject getDataObject(String url, String referer, String userAgent, String resultHead) {
		JSONObject data = null;
		
		for (int i = 0; i < 10; i++) {
			String string = StringUtils.EMPTY;
			
			if(Play.mode.isProd()) {
				string = NewProxyToolsUtils.proxyGet(url, referer, userAgent, resultHead);
			} else {
				string = CommonProxyPools.directGet(url, referer, userAgent, null, StringUtils.EMPTY);
			}

			if (StringUtils.isEmpty(string)) {
				continue;
			}

			data = parseResultJson(string);
			if (data == null || data.getString("redirectUrl") != null) {
				continue;
			} else {
				break;
			}
		}

		return data;
	}

    private static JSONObject parseResultJson(String string) {
        JSONObject jsonObject;
        try {
            jsonObject = JSON.parseObject(string);
        } catch (Exception e) {
            return null;
        }
        String ret = jsonObject.getString("ret");
        if (ret.contains("SUCCESS")) {
            return jsonObject.getJSONObject("data");
        } else if (ret.contains("宝贝不存在")){
            throw new RuntimeException("宝贝不存在");
        } else {
            return null;
        }
    }

    /**
     * 获取单价, 数量字段
     *
     * @param origin
     * @param propName
     * @return
     */
    private static String getPropVal(String origin, String propName) {
        // 初始字符串索引
        final String mobilePricePrefix = propName + "\\\":\\\"";
        int startI = origin.indexOf(mobilePricePrefix);
        if (startI < 0) {
            return null;
        }
        startI += mobilePricePrefix.length();

        // 结束字符串索引
        int stopI = origin.substring(startI, origin.length()).indexOf("\\\"");
        if (stopI < 0) {
            return null;
        }
        stopI += startI;

        String mobilePriceStr = origin.substring(startI, stopI);
        return mobilePriceStr;
    }

	public static final String TEMPLETPROPS = "TEMPLET_PROPS_";
	/**
	 * 获取标准商品类目属性
	 *
	 * @param cid
	 * @return
	 */
	public static JSONArray getTempletProps(long cid, String child_path) {
		if (cid <= 0) {
			return null;
		}
		String key = TEMPLETPROPS + cid;
		if (!StringUtils.isEmpty(child_path)) {
			key += "_" + child_path;
		}
		JSONArray result = (JSONArray) Cache.get(key);
		if (result != null && result.size() > 0) {
			return result;
		}
		TaobaoClient client = TBApi.genClient();
		ItempropsGetRequest req = new ItempropsGetRequest();
		req.setFields("pid,name,is_sale_prop,is_color_prop,is_allow_alias,is_enum_prop,is_input_prop,prop_values,multi,must");
		req.setCid(cid);
		if (StringUtils.isNotEmpty(child_path)) {
			req.setChildPath(child_path);
		}
		try {
			ItempropsGetResponse response = client.execute(req);
			String body = response.getBody();
            JSONObject item_props = JSON.parseObject(body)
                    .getJSONObject("itemprops_get_response")
                    .getJSONObject("item_props");
            if(item_props != null) {
                result = item_props.getJSONArray("item_prop");
            }

            if (result != null && result.size() > 0) {
				Cache.set(key, result, "1h");
			}
			return result;
		} catch (ApiException e) {
			e.printStackTrace();
		}
		return null;
	}

    /**
     * 获取 props, props_name, props_alias, input_pids, input_str
     *
     * @param propsArr     取得的props转化为的jsonArray
     * @param skuProps     取得的sku属性,用来取颜色和身高
     * @param cid
     * @return
     */
    public static Map<String, String> getProps(Long cid,Long numiid, JSONArray propsArr, JSONArray skuProps) {
        JSONArray templetProps = getTempletProps(cid, null);

        StringBuilder property_alias = new StringBuilder();
        StringBuilder props = new StringBuilder();
        StringBuilder props_name = new StringBuilder();
        StringBuilder input_pids = new StringBuilder();
        StringBuilder input_str = new StringBuilder();

        out:
        for (int i = 0; i < templetProps.size(); i++) {
            JSONObject templetProp = templetProps.getJSONObject(i);
            String propName = templetProp.getString("name");
            Boolean isMulti = templetProp.getBoolean("multi");
            Boolean isMust = templetProp.getBoolean("must");
            Boolean isSaleProp = templetProp.getBoolean("is_sale_prop");
            Boolean isEnumProp = templetProp.getBoolean("is_enum_prop");
            Boolean isInputProp = templetProp.getBoolean("is_input_prop");
            String propPid = templetProp.getString("pid");
            //prop_value数组
            JSONObject prop_values = templetProp.getJSONObject("prop_values");

            for (int j = 0; j < propsArr.size(); j++) {
                JSONObject gotProp = propsArr.getJSONObject(j);
                String gotName = gotProp.getString("name");
                String gotValue = gotProp.getString("value");

                if (propName.equals(gotName) || hasAlias(propName, gotName)) {

                    if (prop_values == null) {
                        // 随机5位的vid
                        String vid = getRandomNum(5);

                        if(isSaleProp) {
                        	input_pids.append(",").append(propPid);
                        	input_str.append(",").append(gotName).append(";").append(gotValue);
                            List<Sku> skus = getSkus(String.valueOf(numiid));
                            for (int k = 0; k < skus.size(); k++) {
                                Sku sku = skus.get(k);
                                String propertiesName = sku.getPropertiesName();
                                for (String propertyName : propertiesName.split(";")) {
                                    Property property = new Property(propertyName);
                                    if (property.getPname().equals(gotName) && property.getVname().equals(gotValue)) {
                                        vid = property.getVid();
                                    }
                                }
                            }
                        } else {
                        	input_pids.append(",").append(propPid);
                        	input_str.append(",").append(gotValue);
                        }
                        appendVal(props, props_name, propPid, vid, gotName, gotValue);
                        continue out;
                    }

                    JSONArray tempPropsArr = prop_values.getJSONArray("prop_value");

                    if (isMulti) {
                        if (isSaleProp) {
                            for (int m = 0; m < skuProps.size(); m++) {
                                JSONObject skuProp = skuProps.getJSONObject(m);
                                String skuPropName = skuProp.getString("propName");
                                if (skuPropName.equals(gotName)) {
                                    JSONArray skuValues = skuProp.getJSONArray("values");
                                    boolean hasSelfProp = true;
                                    outSku:
                                    for (int n = 0; n < skuValues.size(); n++) {
                                        JSONObject skuValue = skuValues.getJSONObject(n);
                                        String name = skuValue.getString("name");
                                        String valueId = skuValue.getString("valueId");
                                        for (int k = 0; k < tempPropsArr.size(); k++) {
                                            JSONObject tempProp = tempPropsArr.getJSONObject(k);
                                            String vid = tempProp.getString("vid");
                                            if (vid.equals(valueId)) {
                                                appendVal(props, props_name, propPid, vid, gotName, name);
                                                if(!name.equalsIgnoreCase(tempProp.getString("name"))) {
                                                	property_alias.append(";").append(propPid).append(":").append(vid).append(":").append(name);
                                                }
                                                continue outSku;
                                            }
                                        }
                                        // 标准属性中未匹配到，应该是自定义的属性值
                                        if (isEnumProp && !isInputProp) {
                                            ////不能自定义  则取第一个属性值
                                            JSONObject firstTempletPropsValue = tempPropsArr.getJSONObject(0);
                                            appendVal(props, props_name, propPid, firstTempletPropsValue.getString("vid"), gotName, firstTempletPropsValue.getString("name"));
                                        } else {
                                            appendVal(props, props_name, propPid, valueId, gotName, name);
                                            // 自定义销售属性值
                                            if (hasSelfProp) {
                                                input_pids.append(",").append(propPid);
                                                input_str.append(",").append(gotName);
                                                hasSelfProp = false;
                                            }
                                            input_str.append(";").append(name);
                                        }
                                    }
                                }
                            }
                        } else {
                            String[] gotValues = gotValue.split(",");
                            boolean find = false;
                            out2:
                            for (String splitValue : gotValues) {
                                for (int k = 0; k < tempPropsArr.size(); k++) {
                                    JSONObject tempProp = tempPropsArr.getJSONObject(k);
                                    if (tempProp.getString("name").equals(splitValue)) {
                                        String vid = tempProp.getString("vid");
                                        appendVal(props, props_name, propPid, vid, gotName, splitValue);
                                        find = true;
                                        continue out2;
                                    }
                                }
                                if(!find) {
                                	JSONObject tempProp = tempPropsArr.getJSONObject(0);
                                    String vname = tempProp.getString("name");
                                    String vid = tempProp.getString("vid");
                                    appendVal(props, props_name, propPid, vid, propName, vname);
                                }
                            }
                        }

                    } else {
                        gotValue = fixGotValue(gotName, gotValue);
                        for (int k = 0; k < tempPropsArr.size(); k++) {
                            JSONObject tempProp = tempPropsArr.getJSONObject(k);
                            String voName = tempProp.getString("name");
                            boolean find = false;

                            if (voName.equals(gotValue)) {
                                find = true;
                            } else if (voName.replaceAll("\\(.*?\\)", "").equals(gotValue.replaceAll("\\(.*?\\)", ""))
                                    || voName.contains(gotValue) || gotValue.contains(voName)) {
                                for (int k2 = k + 1; k2 < tempPropsArr.size(); k2++) {
                                    JSONObject propValue = tempPropsArr.getJSONObject(k2);
                                    String propValueName = propValue.getString("name");
                                    if (gotValue.equals(propValueName)) {
                                        tempProp = propValue;
                                    }
                                }
                                find = true;
                            }

                            if (find) {
                                String vid = tempProp.getString("vid");
                                appendVal(props, props_name, propPid, vid, gotName, gotValue);
                                Boolean is_parent = tempProp.getBoolean("is_parent");
                                String childPath = propPid + ":" +vid;

                                //取二级类型
                                subProp:
                                while (is_parent != null && is_parent) {
                                    JSONArray childProps = getTempletProps(cid, childPath);
                                    if (childProps != null) {
                                        JSONObject childProp = childProps.getJSONObject(0);
                                        String cName = childProp.getString("name");
                                        String cPid = childProp.getString("pid");
                                        Boolean cMust = childProp.getBoolean("must");
                                        Boolean cIsSaleProp = childProp.getBoolean("is_sale_prop");
                                        Boolean cIsEnumProp = childProp.getBoolean("is_enum_prop");
                                        Boolean cIsInputProp = childProp.getBoolean("is_input_prop");
                                        JSONObject childPropValues = childProp.getJSONObject("prop_values");

                                        for (int c = 0; c < propsArr.size(); c++) {
                                            JSONObject propJSONObject = propsArr.getJSONObject(c);
                                            String gotNameC = propJSONObject.getString("name");
                                            String gotValueC = propJSONObject.getString("value");

                                            if (cName.equals(gotNameC)) {
                                                if (childPropValues == null) {
                                                    String cVid = getRandomNum(5);
                                                    input_pids.append(",").append(cPid);
                                                    input_str.append(",").append(gotValueC);
                                                    appendVal(props, props_name, cPid, cVid, cName, gotValueC);
                                                    break subProp;
                                                } else {
                                                    JSONArray childPropValuesArr = childPropValues.getJSONArray("prop_value");
                                                    for (int c2 = 0; c2 < childPropValuesArr.size(); c2++) {
                                                        JSONObject childPropValue = childPropValuesArr.getJSONObject(c2);
                                                        String cPropValueName = childPropValue.getString("name");
                                                        String cPropValueVid = childPropValue.getString("vid");
                                                        is_parent = childPropValue.getBoolean("is_parent");
                                                        childPath = cPid + ":" + cPropValueVid;
                                                        if (gotValueC.equals(cPropValueName)) {
                                                            appendVal(props, props_name, cPid, cPropValueVid, cName, cPropValueName);
                                                            continue subProp;
                                                        }
                                                        // gotValueC和cPropValueName去除括号及括号中内容 比较是否相等 |是否是互相包含关系（梵蜜琳凝彩卷翘睫毛膏&凝彩卷翘睫毛膏）
                                                        // 如果相等 继续循环childPropValuesArr如果有完全相等则使用完全相等的
                                                        if (gotValueC.replaceAll("\\(.*?\\)", "").equals(cPropValueName.replaceAll("\\(.*?\\)", ""))
                                                                || gotValueC.contains(cPropValueName) || cPropValueName.contains(gotValueC)) {
                                                            for (int c3 = c2 + 1; c3 < childPropValuesArr.size(); c3++) {
                                                                JSONObject childPropValueC3 = childPropValuesArr.getJSONObject(c3);
                                                                String cPropValueNameC3 = childPropValueC3.getString("name");
                                                                String cPropValueVidC3 = childPropValueC3.getString("vid");
                                                                if (gotValueC.equals(cPropValueNameC3)) {
                                                                    appendVal(props, props_name, cPid, cPropValueVidC3, cName, cPropValueNameC3);
                                                                    continue subProp;
                                                                }
                                                            }
                                                            appendVal(props, props_name, cPid, cPropValueVid, cName, cPropValueName);
                                                            continue subProp;
                                                        }
                                                    }
                                                    
                                                    // 标准属性中未匹配到，应该是自定义的属性值
                                                    if (!cIsSaleProp && cIsEnumProp && !cIsInputProp) {
                                                    	////不能自定义  则取第一个属性值
                                                    	JSONObject firstTempletPropsValue = childPropValuesArr.getJSONObject(0);
                                                    	appendVal(props, props_name, cPid, firstTempletPropsValue.getString("vid"), gotNameC, firstTempletPropsValue.getString("name"));
                                                    } else {
                                                    	appendVal(props, props_name, cPid, getRandomNum(5), cName, gotValueC);
                                                        input_pids.append(",").append(cPid);
                                                        input_str.append(",").append(gotValueC);
                                                    }

                                                    break subProp;
                                                }

                                            }
                                        }
                                        // 二级属性未匹配到又必须填写
                                        if(cMust) {
                                        	if(childPropValues == null) {
                                        		appendVal(props, props_name, cPid, getRandomNum(5), cName, gotValue);
                                        		input_pids.append(",").append(cPid);
                                        		input_str.append(",").append(gotValue);
                                        	} else {
                                        		JSONArray childPropValuesArr = childPropValues.getJSONArray("prop_value");
                                        		JSONObject childPropValue = childPropValuesArr.getJSONObject(0);
                                        		String gName = childPropValue.getString("name");
                                        		String gVid = childPropValue.getString("vid");
                                        		appendVal(props, props_name, cPid, gVid, cName, gName);
                                        	}
                                        }

                                    }
                                    break ;
                                }

                                continue out;
                            }
                        }
                        if (isEnumProp && !isInputProp) {
                            JSONObject tempProp = tempPropsArr.getJSONObject(0);
                            String vid = tempProp.getString("vid");
                            String vname = tempProp.getString("name");
                            appendVal(props, props_name, propPid, vid, gotName, vname);
                        } else {
                            String vid = getRandomNum(5);
                            appendVal(props, props_name, propPid, vid, gotName, gotValue);
                            input_pids.append(",").append(propPid);
                            input_str.append(",").append(gotValue);
                        }
                    }
                    continue out;
                }
            }
            if (isMust) {
                if (prop_values == null) {
                    String vid = getRandomNum(5);
                    String vName = "其他";
                    if (propName.contains("成分") || propName.contains("含量")) {
                        vName += "100%";
                    }
                    if(propName.contains("日期范围")) {
                    	vName = "2017-01-01至2017-12-30";
                    }
                    if(propName.contains("尺寸")) {
                    	vName = "10x10cm";
                    }
                    if(isSaleProp) {
                    	input_pids.append(",").append(propPid);
                    	input_str.append(",").append(propName).append(";").append(vName);
                    } else {
                    	input_pids.append(",").append(propPid);
                    	input_str.append(",").append(vName);
                    }
                    appendVal(props, props_name, propPid, vid, propName, vName);
                } else {
                    JSONArray tempPropsArr = prop_values.getJSONArray("prop_value");
                    boolean find = false;
                    for (int k = 0; k < tempPropsArr.size(); k++) {
                        JSONObject tempProp = tempPropsArr.getJSONObject(k);
                        String vname = tempProp.getString("name");
                        if (vname.contains("other/其他")) {
                            String vid = tempProp.getString("vid");
                            appendVal(props, props_name, propPid, vid, propName, vname);
                            find = true;
                        }
                    }
                    if (!find) {
                        JSONObject tempProp = tempPropsArr.getJSONObject(0);
                        String vname = tempProp.getString("name");
                        String vid = tempProp.getString("vid");
                        appendVal(props, props_name, propPid, vid, propName, vname);
                    }
                }
            }
        }
        
        Map<String, String> dataMap = new HashMap<String, String>();
        if (props.length() > 0) {
            dataMap.put("props", props.substring(1));
        }
        if (props_name.length() > 0) {
            dataMap.put("props_name", props_name.substring(1));
        }
        if (property_alias.length() > 0) {
            dataMap.put("property_alias", property_alias.substring(1));
        }
        if (input_pids.length() > 0) {
            dataMap.put("input_pids", input_pids.substring(1));
        }
        if (input_str.length() > 0) {
            dataMap.put("input_str", input_str.substring(1));
        }
        return dataMap;
    }

    public static Map<String, String> getProps2(Long cid,Long numiid, JSONArray propsArr, JSONArray skuProps, JSONObject ppathIdmap, JSONObject skusFromData) {
        ItemProperty itemProperty = new ItemProperty(cid, numiid, propsArr, skuProps, ppathIdmap, skusFromData);

        return itemProperty.build();
    }

    private static void appendVal(StringBuilder props, StringBuilder propsName, String pid, String vid, String gotName, String gotValue) {
        props.append(";")
                .append(pid).append(":").append(vid);
        propsName.append(";")
                .append(pid).append(":").append(vid).append(":")
                .append(gotName).append(":")
                .append(gotValue);
    }

    /**
     * 获取skus
     *
     * @param apiStack
     * @param ppathIdmap
     * @param skuProps
     * @return 返回json数组
     */
    private static List<Sku> getSkus(Long numIid, String apiStack, String ppathIdmap, JSONArray skuProps) {
        Object apiStackObj = JSON.parseArray(apiStack).get(0);
        String valueStr = JSON.parseObject(apiStackObj.toString()).getString("value");
        JSONObject skusJson = JSON.parseObject(valueStr).getJSONObject("data")
                .getJSONObject("skuModel").getJSONObject("skus");
        Map<String, String> ppathIdMap = JSON.parseObject(ppathIdmap, new TypeReference<Map<String, String>>() {
        });
        Set<Map.Entry<String, String>> entries = ppathIdMap.entrySet();
        List<Sku> skuList = new ArrayList<Sku>();
        for (Map.Entry<String, String> entry : entries) {
            String properties = entry.getKey();
            String sku_id = entry.getValue();
            JSONObject skuJsonObj = skusJson.getJSONObject(sku_id);
            Long quantity = skuJsonObj.getLong("quantity");
            JSONObject priceUnitsObj = skuJsonObj.getJSONArray("priceUnits").getJSONObject(0);
            String price = priceUnitsObj.getString("price");

            StringBuilder propertiesNameSb = new StringBuilder();
            String[] propArr = properties.split(";");
            out:
            for (String prop : propArr) {
                String[] pid_vid = prop.split(":");
                for (int i = 0; i < skuProps.size(); i++) {
                    JSONObject skuProp = skuProps.getJSONObject(i);
                    String propName = skuProp.getString("propName");
                    if (skuProp.getString("propId").equals(pid_vid[0])) {
                        JSONArray propValueArr = skuProp.getJSONArray("values");
                        for (int j = 0; j < propValueArr.size(); j++) {
                            JSONObject propValueJson = propValueArr.getJSONObject(j);
                            if(propValueJson.getString("valueId").equals(pid_vid[1])) {
                                String vName = propValueJson.getString("name");
                                propertiesNameSb.append(";").append(prop)
                                        .append(":").append(propName)
                                        .append(":").append(vName);
                                continue out;
                            }
                        }
                    }
                }
            }
            if (propertiesNameSb.length() == 0) {
                continue;
            }
            String propertiesName = propertiesNameSb.substring(1);
            Sku sku = new Sku();
            sku.setNumIid(numIid);
            sku.setPrice(price);
            sku.setProperties(properties);
            sku.setPropertiesName(propertiesName);
            sku.setQuantity(quantity);

            String Date = JSON.toJSONStringWithDateFormat(new Date(), "yyyy-MM-dd HH:mm:ss");
            sku.setCreated(Date);
            sku.setModified(Date);

            skuList.add(sku);
        }
        return skuList;
    }

    public static final String SKUS_NUMIID_CACHE = "SKUS_NUMIID_CACHE_";

    public static List<Sku> getSkus(String numIid) {
        String cacheKey = SKUS_NUMIID_CACHE + numIid;
        List<Sku> result = (List<Sku>)Cache.get(cacheKey);
        if (!CommonUtils.isEmpty(result)) {
            return result;
        }

        TaobaoClient client = TBApi.genClient();
        ItemSkusGetRequest req=new ItemSkusGetRequest();
        req.setFields("num_iid,sku_id,properties,properties_name,quantity,price,outer_id,created,modified,barcode");
        req.setNumIids(numIid);

        try {
            ItemSkusGetResponse response = client.execute(req);
            String body = response.getBody();
            JSONArray skuArr = JSON.parseObject(body).getJSONObject("item_skus_get_response")
                    .getJSONObject("skus")
                    .getJSONArray("sku");
            List<Sku> skus = new ArrayList<Sku>();

            for (int i = 0; i < skuArr.size(); i++) {
                JSONObject skuJSON = skuArr.getJSONObject(i);
                Long quantity = skuJSON.getLong("quantity");
//                if (quantity == 0) {
//                    continue;
//                }
                Sku sku = new Sku();
                sku.setNumIid(skuJSON.getLong("num_iid"));
                sku.setSkuId(skuJSON.getLong("sku_id"));
                sku.setPrice(skuJSON.getString("price"));
                sku.setProperties(skuJSON.getString("properties"));
                sku.setPropertiesName(skuJSON.getString("properties_name").replace("#cln#", "："));
                sku.setQuantity(quantity);
                sku.setBarcode(skuJSON.getString("barcode"));
                skus.add(sku);
            }

            if (!CommonUtils.isEmpty(skus)) Cache.set(cacheKey, skus, "1h");

            return skus;
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获得销售属性的标准类目属性
     *
     * @param templetProps
     * @return
     */
    private static List<JSONObject> getTempSaleProp(JSONArray templetProps) {
        List<JSONObject> data = new ArrayList<JSONObject>();
        for (Object templetProp : templetProps) {
            JSONObject tempProJson = JSON.parseObject(templetProp.toString());
            if (tempProJson.getBooleanValue("is_sale_prop")) {
                data.add(tempProJson);
            }
        }
        return data;
    }


    /**
     * foodSecurity
     *
     * @param gotName
     * @param gotValue
     * @param foodSecurity
     */
    public static boolean getFoodSecurity(String gotName, String gotValue, FoodSecurity foodSecurity) {
        if (gotName.equals("生产许可证编号")) {
            gotValue = gotValue.replace(" ", "");
            foodSecurity.setPrdLicenseNo(gotValue);
            return true;
        } else if (gotName.equals("产品标准号")) {
            foodSecurity.setDesignCode(gotValue);
            return true;
        } else if (gotName.equals("厂名")) {
            foodSecurity.setFactory(gotValue);
            return true;
        } else if (gotName.equals("厂址")) {
            foodSecurity.setFactorySite(gotValue);
            return true;
        } else if (gotName.equals("厂家联系方式")) {
            foodSecurity.setContact(gotValue);
            return true;
        } else if (gotName.equals("配料表")) {
            foodSecurity.setMix(gotValue);
            return true;
        } else if (gotName.equals("储藏方法")) {
            foodSecurity.setPlanStorage(gotValue);
            return true;
        } else if (gotName.equals("保质期")) {
            if (NumberUtils.isNumber(gotName)) {
                foodSecurity.setPeriod(gotValue);
            }
            return true;
        } else if (gotName.equals("食品添加剂")) {
            foodSecurity.setFoodAdditive(gotValue);
            return true;
        } else if (gotName.equals("供货商")) {
            foodSecurity.setSupplier(gotValue);
            return true;
        } else if (gotName.equals("生产开始日期")) {
            foodSecurity.setProductDateStart(gotValue);
            return true;
        } else if (gotName.equals("生产结束日期")) {
            foodSecurity.setProductDateEnd(gotValue);
            return true;
        } else if (gotName.equals("进货开始时间")) {
            foodSecurity.setStockDateStart(gotValue);
            return true;
        } else if (gotName.equals("进货结束时间")) {
            foodSecurity.setStockDateEnd(gotValue);
            return true;
        }
        return false;
    }

    /**
     * 指定长度的随机数
     *
     * @param length
     * @return
     */
    public static String getRandomNum(int length) {
        if (length <= 0) {
            length = 1;
        }
        StringBuilder res = new StringBuilder();
        Random random = new Random();
        int i = 0;
        while (i < length) {
            res.append(random.nextInt(10));
            i++;
        }
        return res.toString();
    }

    private static String fixGotValue(String gotName, String gotValue) {
        if (gotName.equals("版型")) {
            if (gotValue.equals("修身")) {
                gotValue = "修身型";
            } else if (gotValue.equals("标准")) {
                gotValue = "直筒";
            }
        }

        return gotValue;
    }
    
    public static Map<String, String> getCopyItem(User user, long numIid, String wapData, String descData) {
    	
    	Map<String, String> itemMap = null;
        JSONObject data;
    	if (StringUtils.isEmpty(wapData)) {
            String wapUrl = "http://hws.m.taobao.com/cache/wdetail/5.0/?id=" + numIid + "";
            data = getDataObject(wapUrl, "http://hws.m.taobao.com");
        } else {
    	    data = JSON.parseObject(wapData).getJSONObject("data");
        }

        if (data == null) {
            return itemMap;
        }
        
        try {
        	itemMap = new HashMap<String, String>();
            
            String apiStack = data.getString("apiStack");
            String price = getPrice(data);
            if (price != null) {
            	itemMap.put("price", price);
            }

            Pattern quantityPattern = Pattern.compile("\\\\\"quantity\\\\\":\\\\\"(\\d+)\\\\\"");
            Matcher matcher = quantityPattern.matcher(apiStack);
            if (matcher.find()) {
                String quantity = matcher.group(1);
                itemMap.put("quantity", quantity);
            }
            
            JSONObject seller = data.getJSONObject("seller");
            String nick = seller.getString("nick");
            itemMap.put("nick", nick);

            JSONObject itemInfoModel = data.getJSONObject("itemInfoModel");
            String title = itemInfoModel.getString("title");
            itemMap.put("title", title);
            
            String itemUrl = itemInfoModel.getString("itemUrl");
            itemMap.put("item_url", itemUrl);

            String cid = itemInfoModel.getString("categoryId");
            itemMap.put("cid", cid);
            String location = itemInfoModel.getString("location");
            itemMap.put("location", location);
            
            String city = "台州";
            String state = "浙江";
            // api获取卖家默认发货地址
            List<AddressResult> addressList = new LogisticsApi.LogisticsAddressSearch(user.getSessionKey(), "get_def").call();
            if(addressList != null) {
            	AddressResult address = addressList.get(0);
            	city = address.getCity() == null? city : address.getCity();
            	if(city.endsWith("市")) {
            		city = city.substring(0, city.length() - 1);
            	}
            	if(city.endsWith("地区")) {
            		city = city.substring(0, city.length() - 2);
            	}
            	// 自治州
            	if(city.endsWith("自治州")) {
            		if(city.indexOf("伊犁") >= 0) {
            			city = "伊犁";
            		} else if (city.indexOf("克孜勒苏柯尔克孜") >= 0) {
    					city = "克孜勒苏柯尔克孜";
    				} else {
    					// 昌吉回族自治州 博尔塔拉蒙古自治州 巴音郭楞蒙古自治州
    					city = city.substring(0, city.length() - 5);
    				}
            	}
            	state = address.getProvince() == null? state : address.getProvince();
            	if(state.endsWith("省")) {
            		state = state.substring(0, state.length() - 1);
            	}
            	// 香港 澳门
            	if(state.endsWith("行政区")) {
            		state = state.substring(0, 2);
            	}
            	// 自治区
            	if(state.endsWith("自治区")) {
            		if(state.indexOf("内蒙古") >= 0) {
                		state = "内蒙古";
                	} else {
                		// 广西壮族自治区 西藏自治区 宁夏回族自治区 新疆维吾尔自治区
                		state = state.substring(0, 2);
    				}
            	}
            }
            itemMap.put("prov", state);
            itemMap.put("city", city);
            
            // 宝贝图片
            JSONArray picsPath = itemInfoModel.getJSONArray("picsPath");
            for (int i = 0; i < picsPath.size(); i++) {
                String itemUrlStr = picsPath.getString(i);
                TMResult<Picture> result = FileCarryUtils.newUploadPicFromOnline(user, itemUrlStr);
                if (!result.isOk) {
    				continue;
    			}
                String picUrl = result.getRes().getPicturePath();
                itemMap.put("item_image_" + i, picUrl);
                // 产品图片
            	if(i == 0) {
            		itemMap.put("product_image_" + i, picUrl);
            	}

            }
            
            JSONArray propsArr = data.getJSONArray("props");

            boolean hasSku = itemInfoModel.getBooleanValue("sku");
            JSONArray skuProps = null;
            if (hasSku) {
                JSONObject skuModel = data.getJSONObject("skuModel");
                // skuProps, 获取尺码和颜色信息（不一定同时有）
                skuProps = skuModel.getJSONArray("skuProps");
                List<Sku> skus = getSkus("" + numIid);
                String skuValue = StringUtils.EMPTY;
                for (Sku sku : skus) {
					skuValue = skuValue + sku.getProperties() + ";price:" + sku.getPrice() + ";quantity:" + sku.getQuantity() + ",";
				}
                itemMap.put("sku", skuValue);
            }
            
            if (skuProps != null && !skuProps.isEmpty()) {
                for (int i = 0; i < skuProps.size(); i++) {
                    JSONObject propJsonObj = skuProps.getJSONObject(i);
                    String propId = propJsonObj.getString("propId");
                    JSONArray propDetails = propJsonObj.getJSONArray("values");
                    String value = StringUtils.EMPTY;
                    if("1627207".equalsIgnoreCase(propId)) {
                    	for (int i1 = 0; i1 < propDetails.size(); i1++) {
                    		JSONObject detail = propDetails.getJSONObject(i1);
                    		String valueId = detail.getString("valueId");
                    		String name = detail.getString("name");
                    		String imgUrl = detail.getString("imgUrl");
                    		
                    		if(!StringUtils.isEmpty(imgUrl)) {
                    			TMResult<Picture> result = FileCarryUtils.newUploadPicFromOnline(user, imgUrl);
                    			if (!result.isOk) {
                    				continue;
                    			}
                    			String url = result.getRes().getPicturePath();
                    			value = value + valueId + ";" + name + ";" + url + ",";
                    		}
                    	}
                    	itemMap.put("prop_extend_1627207", value);
                    }
                }
            }

            if (propsArr != null && !propsArr.isEmpty()) {
            	for (int j = 0; j < propsArr.size(); j++) {
            		JSONObject gotProp = propsArr.getJSONObject(j);
                    String gotName = gotProp.getString("name");
                    String gotValue = gotProp.getString("value");
                    // 保质期只能输入大于0的整数  有些宝贝有两个保质期 （保质期：540 天; 保质期: 18个月）
                    if("保质期".equalsIgnoreCase(gotName)) {
                    	if(gotValue.indexOf("月") >= 0 || gotValue.indexOf("年") >= 0) {
                    		continue;
                    	}
                    }
                    itemMap.put(gotName, gotValue);
    			}
            }
            // 属性修正
            if(itemMap.containsKey("生产许可证编号")) {
            	itemMap.put("许可证编号（QS编号）", itemMap.get("生产许可证编号"));
            }
            if(itemMap.containsKey("生产日期")) {
            	String value = itemMap.get("生产日期");
            	String[] split = value.replaceAll("日", " ").replaceAll(" ", "").split("至");
            	itemMap.put("生产日期开始时间", split[0].replaceAll("年", "-").replaceAll("月", "-"));
            	itemMap.put("生产日期结束时间", split[1].replaceAll("年", "-").replaceAll("月", "-"));
            }
            
            if(itemMap.containsKey("进货日期")) {
            	String value = itemMap.get("进货日期");
            	String[] split = value.replaceAll("日", " ").replaceAll(" ", "").split("至");
            	itemMap.put("进货日期开始时间", split[0].replaceAll("年", "-").replaceAll("月", "-"));
            	itemMap.put("进货日期结束时间", split[1].replaceAll("年", "-").replaceAll("月", "-"));
            }

            JSONObject descInfo = data.getJSONObject("descInfo");
            String fullDescUrl = descInfo.getString("fullDescUrl");

            try{
                JSONObject jsonObject = null;
                if (StringUtils.isEmpty(descData)) {
                    jsonObject = getDataObject(fullDescUrl, "http://hws.m.taobao.com");
                } else {
                    jsonObject = JSON.parseObject(descData).getJSONObject("data");
                }
                if (jsonObject == null) return null;

                String description = jsonObject.getString("desc");
                description = description.replaceAll("</?(html|body|head)>", "").trim();
                
                Pattern pattern = Pattern.compile("(?:src=\"?)(.*?)\"?\\s");
        		Matcher m = pattern.matcher(description);
        		while (m.find()) {
        			String oldDescPictureUrl = m.group(1);
        			TMResult<Picture> result = FileCarryUtils.newUploadPicFromOnline(user, oldDescPictureUrl);
        			if (!result.isOk) {
        				continue;
        			}
        			String newDescPictureUrl = result.getRes().getPicturePath();
        			description = description.replaceAll(oldDescPictureUrl, newDescPictureUrl);
        		}
                
                itemMap.put("description", description);
            }catch(Exception e){
                log.warn(e.getMessage(), e);
            }

            log.info("itemMap:~~~~~~~~~~~~~~~~~~~~~" + itemMap);
            return itemMap;
        } catch (ParseException e) {
            log.warn(e.getMessage(), e);
            return MapUtils.EMPTY_MAP;
        }
       
    }
    
    /**
     * 获取颜色Props
     * @param sid
     * @param numIid
     * @return
     */
    public static List<String> getColorProperties(String sid, long numIid) {
    	List<String> colorProps=new ArrayList<String>();
        String wapUrl = "http://hws.m.taobao.com/cache/wdetail/5.0/?id=" + numIid + "";
        JSONObject data = getDataObject(wapUrl, "http://hws.m.taobao.com");
        if (data == null) {
            return null;
        }
        
        JSONObject itemInfoModel = data.getJSONObject("itemInfoModel");

        boolean hasSku = itemInfoModel.getBooleanValue("sku");
        JSONArray skuProps = null;
        if (hasSku) {
            JSONObject skuModel = data.getJSONObject("skuModel");
            // skuProps, 为了获取身高颜色信息
            skuProps = skuModel.getJSONArray("skuProps");
        }
        if (skuProps != null && !skuProps.isEmpty()) {
            for (int i = 0; i < skuProps.size(); i++) {
                JSONObject propJsonObj = skuProps.getJSONObject(i);
                String propId = propJsonObj.getString("propId");
                //判断是否是颜色属性
                if (propId.equalsIgnoreCase("1627207")) {
                	JSONArray propDetails = propJsonObj.getJSONArray("values");
                	if (!CommonUtil.isNullOrEmpty(propDetails)) {
                		for (int j = 0; j < propDetails.size(); j++) {
                            JSONObject detail = propDetails.getJSONObject(j);
                            String valueId = detail.getString("valueId");
                            colorProps.add(propId + ":" + valueId);
                        }
					}
				}
                
            }
        }

        return colorProps;
    }


    static class Property {
        private String pid;
        private String vid;
        private String pname;
        private String vname;

        public Property() {}

        public Property(String pid, String vid, String pname, String vname) {
            this.pid = pid;
            this.vid = vid;
            this.pname = pname;
            this.vname = vname;
        }

        public Property(String propertyStr) {
            String[] split = propertyStr.split(":");
            if (split.length == 2) {
                this.pid = split[0];
                this.vid = split[1];
            } else if (split.length == 4) {
                this.pid = split[0];
                this.vid = split[1];
                this.pname = split[2];
                this.vname = split[3];
            }
        }

        public String getPid() {
            return pid;
        }

        public Property setPid(String pid) {
            this.pid = pid;
            return this;
        }

        public String getVid() {
            return vid;
        }

        public Property setVid(String vid) {
            this.vid = vid;
            return this;
        }

        public String getPname() {
            return pname;
        }

        public Property setPname(String pname) {
            this.pname = pname;
            return this;
        }

        public String getVname() {
            return vname;
        }

        public Property setVname(String vname) {
            this.vname = vname;
            return this;
        }

    }

    // 在类ItemProperty中使用 标识做删除操作
    static class DoDeleteProperty extends Property {}

    // 在类ItemProperty中使用 标识做自定义操作
    static class DoCustomProperty extends Property {
        public DoCustomProperty(String pid, String vid, String pname, String vname) {
            super(pid, vid, pname, vname);
        }
    }

    // 在类ItemProperty中使用 标识做替换操作
    static class DoReplaceProperty extends Property {
        public DoReplaceProperty(String pid, String vid, String pname, String vname) {
            super(pid, vid, pname, vname);
        }
    }

    // 核对skus中sku的属性
    private static void checkWithTemplet(List<JSONObject> templetSaleProp, List<Sku> skus) {
        if (templetSaleProp == null) return;
        for (Sku sku : skus) {
            String propertiesName = sku.getPropertiesName();
            List<String> propertiesList = new ArrayList<String>();
            String[] split = propertiesName.split(";");
            for (int i = 0;i < split.length; i++) { // 对一个sku里的多个属性进行循环
                Property property = new Property(split[i]);
                Integer checkPnameWithTemplet = checkPnameWithTemplet(templetSaleProp, property.getPname());
                if (checkPnameWithTemplet != -1) { // 模板中包含Pname
                    JSONObject checkPnameTemplet = templetSaleProp.get(checkPnameWithTemplet);
                    if (checkPnameTemplet.getJSONObject("prop_values") == null) {
                        propertiesList.add(property.getPid() + ":" + property.getVid());
                        break;
                    }
                    JSONArray propValues = checkPnameTemplet.getJSONObject("prop_values").getJSONArray("prop_value");
                    Integer checkVidWithTemplet = checkVidWithTemplet(propValues, property.getVid());
                    if (checkVidWithTemplet != -1) { // 模板中包含vid
                        propertiesList.add(property.getPid() + ":" + property.getVid());
                    }
                    else { // 模板中包含Pname但不包含vid
                        Boolean is_enum_prop = checkPnameTemplet.getBoolean("is_enum_prop");
                        Boolean is_input_prop = checkPnameTemplet.getBoolean("is_input_prop");
                        if (is_enum_prop && !is_input_prop) { // 表示该销售属性不能自定义
                            // 默认取第一个属性值
                            JSONObject propValueFirst = propValues.getJSONObject(0);
                            propertiesList.add(property.getPid() + ":" + propValueFirst.getString("vid"));

                        } else { // 表示该销售属性可以自定义 ItemCopyApi中会有逻辑去替换
                            propertiesList.add(property.getPid() + ":" + property.getVid());
                        }

                    }
                } else { // 模板中不包含Pname
                    String replacementPid = queryReplacementPidWithTemplet(templetSaleProp, property.getPname());
                    if (replacementPid != null) propertiesList.add(replacementPid + ":" + property.getVid());
                }
            }
            String properties = StringUtils.join(propertiesList.iterator(), ";");
            sku.setProperties(properties);
        }
    }

    // 核对属性名在模板中是否存在
    private static Integer checkPnameWithTemplet(List<JSONObject> templetSaleProp, String pname) {
        if (templetSaleProp == null) return -1;
        for (int i = 0; i < templetSaleProp.size(); i++) {
            JSONObject itemProp = templetSaleProp.get(i);
            String name = itemProp.getString("name");
            if (name.equals(pname)) {
                return i;
            }

        }

        return -1;
    }

    // 核对属性值id在模板是否存在
    private static Integer checkVidWithTemplet(List propValues, String vid) {
        if (propValues == null) return -1;
        for (int i = 0; i < propValues.size(); i++) {
            JSONObject propValue = (JSONObject) propValues.get(i);
            String $vid = propValue.getString("vid");
            if ($vid.equals(vid)) return i;
        }

        return -1;
    }

    // 属性名在模板中不存在时 寻找替代的属性  返回pid
    private static String queryReplacementPidWithTemplet(List<JSONObject> templetSaleProp, String pname) {
        for (int i = 0; i < templetSaleProp.size(); i++) {
            JSONObject itemProp = templetSaleProp.get(i);
            String name = itemProp.getString("name");
            if (hasAlias(name, pname)) {
                return itemProp.getString("pid");
            }
        }

        return null;
    }

    private static Map<String, String> pnameAliasMapper = new HashMap<String, String>();
    static {
        pnameAliasMapper.put("颜色", "颜色分类");
        Collections.unmodifiableMap(pnameAliasMapper);
    }
    private static Boolean hasAlias(String name, String alias) {
        String rname = pnameAliasMapper.get(alias);
        if (rname == null) return false;
        if (!rname.equals(name)) return false;

        return true;
    }

    static class ItemProperty {
        Long cid; // 类目ID
        Long numiid; // 宝贝ID
        List propsArr; // 接口http://hws.m.taobao.com/cache/wdetail/5.0/?id=xxxx中的props
        List skuProps; // 接口http://hws.m.taobao.com/cache/wdetail/5.0/?id=xxxx中的skuModel.skuProps
        Map ppathIdmap; // 接口http://hws.m.taobao.com/cache/wdetail/5.0/?id=xxxx中的skuModel.ppathIdmap
        Map skus; // 接口http://hws.m.taobao.com/cache/wdetail/5.0/?id=xxxx中的apiStack[0].value.data.skuModel.skus

        public ItemProperty(Long cid, Long numiid, List propsArr, List skuProps, Map ppathIdmap, Map skus) {
            this.cid = cid;
            this.numiid = numiid;
            this.propsArr = propsArr;
            this.skuProps = skuProps;
            this.ppathIdmap = ppathIdmap;
            this.skus = skus;
        }

        // 属性值别名。如pid:vid:别名;pid1:vid1:别名1 ，其中：pid是属性id vid是属性值id。总长度不超过512字节
        StringBuilder property_alias = new StringBuilder();
        // 商品属性列表。格式:pid:vid;pid:vid。属性的pid调用taobao.itemprops.get取得，属性值的vid用taobao.itempropvalues.get取得vid。
        StringBuilder props = new StringBuilder();
        // sku所对应的销售属性的中文名字串，格式如：pid1:vid1:pid_name1:vid_name1;pid2:vid2:pid_name2:vid_name2……
        StringBuilder props_name = new StringBuilder();
        // 用户自行输入的类目属性ID串，结构："pid1,pid2,pid3"，如："20000"（表示品牌） 注：通常一个类目下用户可输入的关键属性不超过1个。
        StringBuilder input_pids = new StringBuilder();
        // 用户自行输入的子属性名和属性值，结构:"父属性值;一级子属性名;一级子属性值;二级子属性名;自定义输入值,....",多个自定义属性用','分割，input_str需要与input_pids一一对应
        StringBuilder input_str = new StringBuilder();

        public Map<String, String> build() {
            List item_props = ItemGetAction.getTempletProps(cid, null);
            buildItemPropertyWithTemplet(item_props);

            // 创建sku相关属性
            buildSkuProperties(item_props);

            return packingDate();
        }

        // 将property_alias、props、props_name、input_pids、input_str封装到Map中
        private Map<String, String> packingDate() {
            Map<String, String> dataMap = new HashMap<String, String>();
            if (props.length() > 0) {
                dataMap.put("props", props.substring(1));
            }
            if (props_name.length() > 0) {
                dataMap.put("props_name", props_name.substring(1));
            }
            if (property_alias.length() > 0) {
                dataMap.put("property_alias", property_alias.substring(1));
            }
            if (input_pids.length() > 0) {
                dataMap.put("input_pids", input_pids.substring(1));
            }
            if (input_str.length() > 0) {
                dataMap.put("input_str", input_str.substring(1));
            }

            if (sku_properties.length() > 0) {
                dataMap.put("sku_properties", sku_properties.substring(1));
            }
            if (input_custom_cpv.length() > 0) {
                dataMap.put("input_custom_cpv", input_custom_cpv.substring(1));
            }
            if (sku_prices.length() > 0) {
                dataMap.put("sku_prices", sku_prices.substring(1));
            }
            if (sku_quantities.length() > 0) {
                dataMap.put("sku_quantities", sku_quantities.substring(1));
            }
            if (sku_outer_ids.length() > 0) {
                dataMap.put("sku_outer_ids", sku_outer_ids.substring(1));
            }



            return dataMap;
        }

        // 遍历入参模板集合，根据淘宝模板规范和爬来数据的各种情况做不同的处理
        private void buildItemPropertyWithTemplet(List item_props) {
            if (item_props == null) return;
            for (int i = 0; i < item_props.size(); i++) {
                Map item_prop = (Map) item_props.get(i);
                ItemProp itemProp = new ItemProp(item_prop);
                Integer indexByNameInPropsArr = findByNameInPropsArr(itemProp.name, propsArr);
                if (indexByNameInPropsArr == -1) { // 爬来的数据中没有找到模板(itemProp)对应的数据

                    fixNotFoundInPropsArr(itemProp);

                } else {  // 爬来的数据中找到模板(itemProp)对应的数据
                    Map prop = (Map) propsArr.remove(indexByNameInPropsArr.intValue());
                    if (itemProp.propValues == null) {
                        if (itemProp.isSaleProp){

                            fixNullPropValuesIsSaleProp(itemProp, prop);

                        } else {

                            fixNullPropValuesNotSaleProp(itemProp, prop);

                        }
                    } else {
                        if (itemProp.multi && itemProp.isSaleProp) {

                            fillWithSkuProps(itemProp, prop);

                        } else if (itemProp.multi && !itemProp.isSaleProp) {

                            fillWhenMultiNotSaleProp(itemProp, prop);

                        } else {

                            fillWhenNotMulti(itemProp, prop);

                        }
                    }
                }
            }
        }

        // 修复 淘宝模板规范中没有propValues且不是销售属性的情况 itemProp：与prop对应的淘宝模板规范  prop：爬来的数据中的一条属性数据
        private void fixNullPropValuesNotSaleProp(ItemProp itemProp, Map prop) {
            String gotName = (String) prop.get("name");
            String gotValue = (String) prop.get("value");
            // 这里只能自定义属性 如果不能自定义而且不是必要的属性 直接忽略
            if (!itemProp.must && itemProp.isEnumProp && !itemProp.isInputProp) return;

            if (itemProp.multi) {
                // 多属性自定义
                input_str.append(",").append(gotName);
                input_pids.append(",").append(itemProp.pid);
                String[] gotValues = gotValue.split(",");
                for (String splitValue : gotValues) {
                    input_str.append(";").append(splitValue);
                }
            } else {
                gotValue = fixCustomPvalue(gotValue);
                input_pids.append(",").append(itemProp.pid);
                input_str.append(",").append(gotValue);
            }

        }

        // 当该属性不可以多选时，填充date   itemProp：与prop对应的淘宝模板规范  prop：爬来的数据中的一条属性数据
        private void fillWhenNotMulti(ItemProp itemProp, Map prop) {
            String gotValue = (String) prop.get("value");
            String gotName = (String) prop.get("name");
            List<PropValue> propValues = itemProp.propValues;
            String propPid = itemProp.pid;
            String propName = itemProp.name;

            gotValue = fixGotValue(gotName, gotValue);
            // 遍历propValues搜索与gotValue类似的PropValue  返回索引下标
            Integer indexSearch = new SearchInList(propValues, gotValue, new SearchInList.GetCheckObject() {
                @Override
                public String getCheckObject(List list, Integer index) {
                    PropValue propValue = (PropValue) list.get(index);
                    return propValue.name;
                }
            }).search();

            if (indexSearch != -1) { // 在模板(itemProp.propValues)中找到相似或相等的属性值
                PropValue propValue = propValues.get(indexSearch);
                String vid = propValue.vid;
                appendVal(props, props_name, propPid, vid, gotName, gotValue);

                //递归二级类型
                if (propValue.isParent != null && propValue.isParent) {
                    String childPath = propPid + ":" +vid;
                    List item_props = ItemGetAction.getTempletProps(cid, childPath);
                    buildItemPropertyWithTemplet(item_props);
                }
            } else { // 在模板(itemProp.propValues)中不能找到相似或相等的属性值
                if (itemProp.isEnumProp && !itemProp.isInputProp) { // 不支持自定义，则取第一个属性值
                    PropValue propValue = propValues.get(0);
                    String vid = propValue.vid;
                    String vname = propValue.name;
                    appendVal(props, props_name, propPid, vid, propName, vname);

                    //递归二级类型
                    if (propValue.isParent != null && propValue.isParent) {
                        String childPath = propPid + ":" +vid;
                        List item_props = ItemGetAction.getTempletProps(cid, childPath);
                        buildItemPropertyWithTemplet(item_props);
                    }
                } else {  // 自定义属性值
                    input_pids.append(",").append(propPid);
                    input_str.append(",").append(gotValue);

                    // 修复自定义属性“品牌”下含有一级子属性“型号”的情况
                    fixCustomProp20000(propPid);
                }
            }
        }

        // 修复自定义属性“品牌”下含有一级子属性“型号”的情况
        private void fixCustomProp20000(String propPid) {
            if (!"20000".equals(propPid)) return;
            // 尝试从原宝贝的属性中寻找属性名为“型号”的属性
            Integer indexOfVersion = findByNameInPropsArr("型号", propsArr);
            if (indexOfVersion == -1) return;
            // 找到了  ,伟业电机(自定义品牌名) --> ,伟业电机(自定义品牌名);型号;U880(自定义型号名)
            Map map = (Map) propsArr.get(indexOfVersion);
            input_str.append(";").append("型号").append(";").append(map.get("value"));
        }

        // 当该属性可以多选但不是销售属性时，填充date   itemProp：与prop对应的淘宝模板规范  prop：爬来的数据中的一条属性数据
        private void fillWhenMultiNotSaleProp(ItemProp itemProp, Map prop) {
            List<PropValue> propValues = itemProp.propValues;
            String gotValue = (String) prop.get("value");
            String propPid = itemProp.pid;
            String propName = itemProp.name;
            String gotName = (String) prop.get("name");

            String[] gotValues = gotValue.split(",");

            // 普通属性值中是否含有需要自定义的属性
            Boolean hasCustomProp = false;

            for (String splitValue : gotValues) {
                // 遍历propValues搜索与splitValue类似的PropValue  返回索引下标
                Integer indexSearch = new SearchInList(propValues, splitValue, new SearchInList.GetCheckObject() {
                    @Override
                    public String getCheckObject(List list, Integer index) {
                        PropValue propValue = (PropValue) list.get(index);
                        return propValue.name;
                    }
                }).search();

                if (indexSearch != -1) { // 找到相似的属性值
                    PropValue propValue = propValues.get(indexSearch);
                    String vid = propValue.vid;
                    String vname = propValue.name;
                    appendVal(props, props_name, propPid, vid, gotName, vname);

                    //递归二级类型
                    if (propValue.isParent != null && propValue.isParent) {
                        String childPath = propPid + ":" +vid;
                        List item_props = ItemGetAction.getTempletProps(cid, childPath);
                        buildItemPropertyWithTemplet(item_props);
                    }

                } else { // 找不到相似的属性值
                    if (itemProp.isEnumProp && !itemProp.isInputProp) { // 不支持自定义，则取第一个属性值
                        PropValue propValue = propValues.get(0);
                        String vname = propValue.name;
                        String vid = propValue.vid;
                        appendVal(props, props_name, propPid, vid, propName, vname);

                        //递归二级类型
                        if (propValue.isParent != null && propValue.isParent) {
                            String childPath = propPid + ":" +vid;
                            List item_props = ItemGetAction.getTempletProps(cid, childPath);
                            buildItemPropertyWithTemplet(item_props);
                        }
                    } else { // 自定义属性值
                        if (!hasCustomProp) {
                            input_str.append(",").append(propName);
                            input_pids.append(",").append(propPid);
                            hasCustomProp = true;
                        }

                        input_str.append(";").append(splitValue);
                    }
                }
            }
        }

        // 修复 爬来数据中未能找到淘宝模板规范对应的数据的情况
        private void fixNotFoundInPropsArr(ItemProp itemProp) {
            List<PropValue> prop_values = itemProp.propValues;
            String propName = itemProp.name;
            String propPid = itemProp.pid;


            if (itemProp.must) {
                if (prop_values == null) {
                    String vid = getRandomNum(5);
                    String vName = "未知";
                    if (propName.contains("成分") || propName.contains("含量")) {
                        vName += "100%";
                    }
                    if(propName.contains("日期范围")) {
                        vName = "2017-01-01至2017-12-30";
                    }
                    if(propName.contains("尺寸")) {
                        vName = "10x10cm";
                    }
                    if (propName.contains("型号")) {
                        vName = "未知型号";
                    }
                    if (propName.contains("生产厂家地址")) {
                        vName = "未知地址";
                    }
                    if(itemProp.isSaleProp) {
                        input_pids.append(",").append(propPid);
                        input_str.append(",").append(propName).append(";").append(vName);
                    } else {
                        input_pids.append(",").append(propPid);
                        input_str.append(",").append(vName);
                    }
                    appendVal(props, props_name, propPid, vid, propName, vName);
                } else {
                    Integer indexOtherInPropValues = findOtherInPropValues(prop_values);

                    if (indexOtherInPropValues == -1) { // 未找到选第一个
                        PropValue propValue = prop_values.get(0);
                        String vname = propValue.name;
                        String vid = propValue.vid;
                        appendVal(props, props_name, propPid, vid, propName, vname);
                    } else { // 找到了
                        PropValue propValue = prop_values.get(indexOtherInPropValues);
                        String vname = propValue.name;
                        String vid = propValue.vid;
                        appendVal(props, props_name, propPid, vid, propName, vname);

                        //递归二级类型
                        if (propValue.isParent != null && propValue.isParent) {
                            String childPath = propPid + ":" +vid;
                            List item_props = ItemGetAction.getTempletProps(cid, childPath);
                            buildItemPropertyWithTemplet(item_props);
                        }
                    }
                }
            }


        }

        // 遍历propsArr找到对应name的prop 返回prop的位置索引
        private Integer findByNameInPropsArr(String name, List propsArr) {
            if (propsArr == null) return -1;
            for (int i = 0; i < propsArr.size(); i++) {
                Map prop = (Map) propsArr.get(i);
                String $name = (String) prop.get("name");
                if (name.equals($name) || hasAlias(name, $name)) return i;
            }

            return -1;
        }

        // 遍历propValues找到name包含other/其他对应的PropValue 返回PropValue的位置索引
        private Integer findOtherInPropValues(List<PropValue> propValues) {
            if (propValues == null) return -1;
            for (int i = 0; i < propValues.size(); i++) {
                String vname = propValues.get(i).name;
                if (vname.contains("other/其他")) return i;
            }

            return -1;
        }

        // 对普通属性自定义属性值中的违规词语进行处理
        private String fixCustomPvalue(String name) {
            // 您自定义的属性值 存在违禁词 雾霾，请去掉后重新再提交
            // 您自定义的属性值 存在违禁词  维尼,情人
            if (name.contains("雾霾")) name = name.replace("雾霾", "浅艾");
            if (name.contains("毒药")) name = name.replace("毒药", "**");
            if (name.contains("维尼")) name = name.replace("维尼", "維尼");
            if (name.contains("情人")) name = name.replace("情人", "**");
            // 属性值里不能存在英文的冒号|分号|逗号  因为连接属性值的就是用的英文的冒号|分号|逗号
            if (name.contains(":")) name = name.replace(":", "：");
            if (name.contains(";")) name = name.replace(";", "；");
            if (name.contains(",")) name = name.replace(",", "，");

            return name;
        }

        // 对销售属性自定义属性值中的违规词语进行处理
        private String fixSalePropCustomPvalue(String name) {
            name = fixCustomPvalue(name);
            /** 双字节字符正则表达式 长度不能大于30字节*/
            final Pattern p = Pattern.compile("[^\\x00-\\xff]");
            char ch;
            int count = 0;
            for (int i = 0; i < name.length(); i++) {
                ch = name.charAt(i);
                Matcher m = p.matcher(String.valueOf(ch));
                if(m.find()) {
                    count += 2;
                } else {
                    count += 1;
                }
                if(count > 30) {
                    return name.substring(0, i);
                }
            }

            return name;
        }

        // 存放可以取别名的属性集合
        List listForAppendAlias;

        // 修复模板不正确的情况  模板表示可以自定义属性，但提示错误当前类目不支持尺码自定义销售属性值
        private boolean fixIncorrectTemplet(ItemProp itemProp, String name, String valueId) {
            List<PropValue> tempPropsArr = itemProp.propValues;
            String propPid = itemProp.pid;
            String gotName = itemProp.name;
            String sizeForCid20509 = extractSizeForCid20509(name);

            // 初始化集合  将模板的属性集合添加进去
            if (listForAppendAlias == null) {
                listForAppendAlias = new ArrayList();
                listForAppendAlias.addAll(tempPropsArr);
            }

            // “no match”表示提取不到合适的尺码  直接跳过这一步
            if (!"no match".equals(sizeForCid20509)) {
                for (int k = 0; k < tempPropsArr.size(); k++) {
                    PropValue propValue = tempPropsArr.get(k);
                    if (isEqualsForForCid20509(propValue.name, sizeForCid20509)) {
                        // 匹配上模板中的属性没有被取过别名 才能取别名
                        if (listForAppendAlias.contains(propValue)) {
                            listForAppendAlias.remove(propValue);
                            property_alias.append(";").append(propPid).append(":").append(propValue.vid).append(":").append(name);
                        }
                        appendVal(props, props_name, propPid, propValue.vid, gotName, name);
                        // 替换逻辑
                        String oldProps = propPid + ":" + valueId;
                        Property newProps = new DoReplaceProperty(propPid, propValue.vid, gotName, name);
                        replaceMap.put(oldProps, newProps);
                        return true;
                    }
                }
            }

            // 所有的属性都取过别名了 没有可用的属性来给当前name取别名了 直接不处理当前name
            // 直接不处理会导致 宝贝销售属性出错（销售属性和商品属性不一致）
            if (listForAppendAlias != null && listForAppendAlias.size() == 0) {
                // 替换逻辑 DoDeleteProperty表示 在替换时直接将sku_properties中含有oldProps的sku删除
                String oldProps = propPid + ":" + valueId;
                Property newProps = new DoDeleteProperty();
                replaceMap.put(oldProps, newProps);
                return true;
            }

            // 模板还有没有被取过别名的属性值  对匹配不上的属性值 进行取别名操作
            if (listForAppendAlias != null && listForAppendAlias.size() > 0) {
                PropValue propValue = (PropValue) listForAppendAlias.remove(listForAppendAlias.size() - 1);
                property_alias.append(";").append(propPid).append(":").append(propValue.vid).append(":").append(name);
                appendVal(props, props_name, propPid, propValue.vid, gotName, name);
                // 替换逻辑
                String oldProps = propPid + ":" + valueId;
                Property newProps = new DoReplaceProperty(propPid, propValue.vid, gotName, name);
                replaceMap.put(oldProps, newProps);
                return true;
            }


            return false;
        }

        // 提取尺寸
        private String extractSizeForCid20509(String name) {
            // XL（180/96A） == XL
            // 大码3XL == XXXL
            // 女M送男XL == M   女XXL送男L == XXL
            Pattern pattern = Pattern.compile("(X*S|M|X*L|\\dXL)");
            Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
                return matcher.group();
            } else {
                // 匹配不到合适的尺码 默认取模板第一个属性来取别名
                return "no match";
            }
        }

        // 3XL == XXXL 返回true
        private boolean isEqualsForForCid20509(String var1, String var2) {
            if (var1.equals(var2)) return true;

            var1 = standardizeForCid20509(var1);
            var2 = standardizeForCid20509(var2);
            if (var1.equals(var2)) return true;

            return false;
        }

        // 3XL 转换成 XXXL  大号均码 转换成 M
        private String standardizeForCid20509(String var) {
            Pattern pattern = Pattern.compile("^(\\d)XL$");
            Matcher m1 = pattern.matcher(var.trim());
            if (m1.find()) {
                int num = Integer.valueOf(m1.group(1));
                var = "";
                for (int i = 0; i < num; i++) var += "X";
                var += "L";
            }

            if (var.equals("大号均码")) var = "M";

            return var;

        }

        StringBuilder sku_properties = new StringBuilder();
        StringBuilder input_custom_cpv = new StringBuilder();
        StringBuilder sku_prices = new StringBuilder();
        StringBuilder sku_quantities = new StringBuilder();
        StringBuilder sku_outer_ids = new StringBuilder();

        Map replaceMap = new HashMap();

        Integer sequenceOfVid = -1;

        Set<String> salePropPids = new HashSet<String>(); // 保存淘宝模板中所有销售属性的pid

        // 当该属性可以多选且是销售属性时，填充date   itemProp：与skuProp对应的淘宝模板规范  skuProp：爬来数据中skuProps中一条数据
        private void fillWithSkuProp(ItemProp itemProp, Map skuProp) {
            List<PropValue> tempPropsArr = itemProp.propValues;
            String propPid = itemProp.pid;
            String propName = itemProp.name;
            Boolean isEnumProp = itemProp.isEnumProp;
            Boolean isInputProp = itemProp.isInputProp;
            Boolean isAllowAlias = itemProp.isAllowAlias;

            JSONArray skuValues = (JSONArray) skuProp.get("values");
            boolean hasSelfProp = true;
            outSku:
            for (int n = 0; n < skuValues.size(); n++) {
                JSONObject skuValue = skuValues.getJSONObject(n);
                String name = skuValue.getString("name");
                String valueId = skuValue.getString("valueId");

                Integer match = skuPropValueMatchTemplet(tempPropsArr, skuValue);
                if (match != -1) {
                    // 在模板中找到了vid|name相等的属性值
                    PropValue propValue = tempPropsArr.get(match);
                    if (propValue.name.equals(name)) {
                        if (propValue.vid.equals(valueId)) {
                            // 最佳情况  vid和name都相等
                            appendVal(props, props_name, propPid, valueId, propName, name);
                        } else {
                            // 找到相同name但是vid不同
                            // 替换原来的vid
                            appendVal(props, props_name, propPid, propValue.vid, propName, name);
                            // 替换逻辑
                            String oldProps = propPid + ":" + valueId;
                            Property newProps = new DoReplaceProperty(propPid, propValue.vid, propName, propValue.name);
                            replaceMap.put(oldProps, newProps);
                        }
                    } else if (propValue.vid.equals(valueId)){
                        if (isAllowAlias) {
                            // 模板中找到了对应的vid但是name不同  允许去别名
                            // 取别名
                            appendVal(props, props_name, propPid, valueId, propName, name);
                            property_alias.append(";").append(propPid).append(":").append(valueId).append(":").append(name);
                        } else {
                            // 模板中找到了对应的vid但是name不同而且不允许取别名
                            // 使用模板中的名字去替换原来的名字
                            appendVal(props, props_name, propPid, valueId, propName, propValue.name);
                        }
                    } else {
                        ;
                    }
                } else {
                    // 标准属性中未匹配到，应该是自定义的属性值
                    if (isEnumProp && !isInputProp) {
                        ////不能自定义  则取第一个属性值
                        PropValue firstTempletPropsValue = tempPropsArr.get(0);
                        appendVal(props, props_name, propPid, firstTempletPropsValue.vid, propName, firstTempletPropsValue.name);
                        // 替换逻辑
                        String oldProps = propPid + ":" + valueId;
                        Property newProps = new DoReplaceProperty(propPid, firstTempletPropsValue.vid, propName, firstTempletPropsValue.name);
                        replaceMap.put(oldProps, newProps);
                    } else {
                        // 修复模板isEnumPro和isInputProp都为真但是不能自定义的情况
                        // 添加特殊属性pid为122216547 也是自定义属性报错 报错宝贝：563726927961、561642149604、563703727355
                        // pid为3344920(重量) 报错宝贝：561857761061、561857657328、561929674223
                        if (propPid.equals("20509") //
                                || (cid.equals(50021413L) && propPid.equals("122216547")) //
                                || (cid.equals(50006236L) && propPid.equals("3344920"))) {
                            boolean hasFix = fixIncorrectTemplet(itemProp, name, valueId);
                            if (hasFix) continue;
                        }

                        // 对自定义属性值中的违规字符进行处理
                        name = fixSalePropCustomPvalue(name);
                        appendVal(props, props_name, propPid, valueId, propName, name);
                        // 自定义销售属性值
                        if (hasSelfProp) {
                            input_pids.append(",").append(propPid);
                            input_str.append(",").append(propName);
                            hasSelfProp = false;
                        }
                        input_str.append(";").append(name);
                        // 替换逻辑
                        String oldProps = propPid + ":" + valueId;
                        Property newProps = new DoCustomProperty(propPid, (sequenceOfVid--).toString(), propName, name);
                        replaceMap.put(oldProps, newProps);
                    }
                }
            }
        }

        private Integer skuPropValueMatchTemplet(List<PropValue> templet, JSONObject skuPropValue) {
            for (int i = 0; i < templet.size(); i++) {
                PropValue tempProp = templet.get(i);
                // 优先匹配name相等的
                String vName = tempProp.name;
                String vid = tempProp.vid;
                String name = skuPropValue.getString("name");
                String valueId = skuPropValue.getString("valueId");
                if (vName.equals(name)) return i;
                if (vid.equals(valueId)) {
                    for (int j = 0; j < templet.size(); j++) {
                        tempProp = templet.get(i);
                        vName = tempProp.name;
                        if (vName.equals(name)) return j;
                    }
                    return i;
                }
            }
            return -1;
        }

        // 当该属性可以多选且是销售属性时，遍历skuProps找到与prop对应的skuProp，填充date  itemProp：与prop对应的淘宝模板规范  prop：爬来的数据中的一条属性数据
        private void fillWithSkuProps(ItemProp itemProp, Map prop) {
            Integer indexByPropNameInSkuProps = findByPropNameInSkuProps((String) prop.get("name"), skuProps);
            if (indexByPropNameInSkuProps == -1) {
                // 模板匹配上了爬来数据props中的属性名且模板表示这个属性名是销售属性和多重的属性，但是在爬来数据skuProps的找不到该属性名
                // 则当做多重的非销售属性来处理
                fillWhenMultiNotSaleProp(itemProp, prop);
            } else {
                Map skuProp = (Map) skuProps.get(indexByPropNameInSkuProps);
                fillWithSkuProp(itemProp, skuProp);
            }
        }

        // 遍历skuProps找到对应propName的skuProp  返回找到skuProp的位置
        private Integer findByPropNameInSkuProps(String propName, List skuProps) {
            if (skuProps == null) return -1;
            for (int i = 0; i < skuProps.size(); i++) {
                Map skuProp = (Map) skuProps.get(i);
                String $propName = (String) skuProp.get("propName");
                if (propName.equals($propName)) return i;
            }

            return -1;
        }

        // 修复 淘宝模板规范中没有propValues且是销售属性的情况 itemProp：与prop对应的淘宝模板规范  prop：爬来的数据中的一条属性数据
        private void fixNullPropValuesIsSaleProp(ItemProp itemProp, Map prop) {
            String gotName = (String) prop.get("name");
            Integer index = findByPropNameInSkuProps(gotName, skuProps);
            if (index == -1) return;
            Map skuProp = (Map) skuProps.get(index);

            List values = (List) skuProp.get("values");
            if (values == null) return;
            input_str.append(",").append(gotName);
            input_pids.append(",").append(itemProp.pid);

            for (Object value : values) {
                Map mapValue = (Map) value;
                String vid = (String) mapValue.get("valueId");
                String name = (String) mapValue.get("name");
                input_str.append(";").append(name);
                appendVal(props, props_name, itemProp.pid, vid, gotName, name);
                // 替换逻辑
                String oldProps = itemProp.pid + ":" + vid;
                Property newProps = new DoCustomProperty(itemProp.pid, (sequenceOfVid--).toString(), itemProp.name, name);
                replaceMap.put(oldProps, newProps);
            }

        }

        // 创建有关sku的属性参数
        private void buildSkuProperties(List item_props) {
            initSalePropPids(item_props);
            checkReplaceMap();
            if (ppathIdmap == null) return;
            Set<Map.Entry> set = ppathIdmap.entrySet();
            for (Map.Entry entry : set) {
                // 对value做处理
                String skuId = (String) entry.getValue();
                Map sku = (Map) skus.get(skuId);
                if (sku == null) continue;
                List listPriceUnits = (List) sku.get("priceUnits");
                Map priceUnits; //优先拿原价
                if (listPriceUnits.size() == 1) priceUnits = (Map) listPriceUnits.get(0);
                else priceUnits = (Map) listPriceUnits.get(1);
                String price = (String) priceUnits.get("price");
                String quantity = (String) sku.get("quantity");
                // 对key做处理
                String key = (String) entry.getKey();
                key = processKey(key);
                // 返回key为空或者价格小于等于0  直接忽略这个sku属性
                if (Double.valueOf(price) <= 0 || StringUtils.isEmpty(key)) continue;
                sku_prices.append(",").append(price);
                sku_quantities.append(",").append(quantity);
                sku_outer_ids.append(",").append(skuId);
                sku_properties.append(",").append(key);
            }
            Set<Map.Entry> set1 = replaceMap.entrySet();
            for (Map.Entry entry : set1) {
                Property value = (Property) entry.getValue();
                if (value instanceof DoCustomProperty) {
                    input_custom_cpv.append(";").append(value.getPid()).append(":").append(value.getVid()).append(":").append(value.getVname());
                }
            }

        }

        // 初始化销售属性Pid集合
        private void initSalePropPids(List item_props) {
            if (item_props == null) return;
            for (int i = 0; i < item_props.size(); i++) {
                Map item_prop = (Map) item_props.get(i);
                ItemProp itemProp = new ItemProp(item_prop);
                if (itemProp.isSaleProp) {
                    salePropPids.add(itemProp.pid);
                }
            }
        }

        // 替换前 核实replaceMap
        private void checkReplaceMap() {
            // 对自定义属性的属性做个数限制  每一种sku属性自定义数限制为24个
            Set<Map.Entry> set = replaceMap.entrySet();
            Iterator<Map.Entry> iterator = set.iterator();
            // 存放sku自定义个数
            Map<String, Integer> numMap = new HashMap<String, Integer>();
            while (iterator.hasNext()) {
                Map.Entry entry = iterator.next();
                Property property = (Property) entry.getValue();
                if (property instanceof DoCustomProperty) {
                    String pid = property.getPid();
                    Integer num = numMap.get(pid);
                    if (num == null) {
                        num = new Integer(1);
                        numMap.put(pid, num);
                    }
                    num += 1;
                    numMap.put(pid, num);

                    if (num > 24) entry.setValue(new DoDeleteProperty());
                }
            }
        }

        // 使用replaceMap替换sku属性
        private String processKey(String key) {
            if (key == null) return null;

            List<String> list = new ArrayList<String>();
            for (String pv : key.split(";")) {
                if (!checkPV(pv)) continue;

                Property newProps = (Property) replaceMap.get(pv);
                if (newProps == null) {
                    list.add(pv);
                } else if (newProps instanceof DoReplaceProperty) {
                    // 替换sku_properties里这个属性
                    list.add(newProps.getPid() + ":" + newProps.getVid());
                } else if (newProps instanceof DoCustomProperty) {
                    // 替换sku_properties里这个属性
                    list.add(newProps.getPid() + ":" + newProps.getVid());
                } else if (newProps instanceof DoDeleteProperty) {
                    // 删除sku_properties里这个属性
                    return null;
                }
            }

            return StringUtils.join(list, ";");
        }

        // 对pid或vid小于0的返回false
        private Boolean checkPV(String pv) {
            String[] split = pv.split(":");
            if (split.length == 2) {
                String pid = split[0];
                String vid = split[1];
                if (Long.valueOf(pid) < 0L || Long.valueOf(vid) < 0L) return false;
                if (!salePropPids.contains(pid)) return false;
                return true;
            }
            return false;
        }

    }

    static class ItemProperty2 {
        Long cid; // 类目ID
        Long numIid; // 宝贝ID
        List<PropInfo> propInfos; // 宝贝普通属性
        List<SkuPropInfo> skuPropInfos; // 宝贝销售属性
        Map<String, SkuCoreInfo> skuCore; // 宝贝销售细节信息

        public ItemProperty2(Long cid, Long numIid, List<PropInfo> propInfos, List<SkuPropInfo> skuPropInfos, Map<String, SkuCoreInfo> skuCore) {
            this.cid = cid;
            this.numIid = numIid;
            this.propInfos = propInfos;
            this.skuPropInfos = skuPropInfos;
            this.skuCore = skuCore;
        }

        // 属性值别名。如pid:vid:别名;pid1:vid1:别名1 ，其中：pid是属性id vid是属性值id。总长度不超过512字节
        StringBuilder property_alias = new StringBuilder();
        // 商品属性列表。格式:pid:vid;pid:vid。属性的pid调用taobao.itemprops.get取得，属性值的vid用taobao.itempropvalues.get取得vid。
        StringBuilder props = new StringBuilder();
        // sku所对应的销售属性的中文名字串，格式如：pid1:vid1:pid_name1:vid_name1;pid2:vid2:pid_name2:vid_name2……
        StringBuilder props_name = new StringBuilder();
        // 用户自行输入的类目属性ID串，结构："pid1,pid2,pid3"，如："20000"（表示品牌） 注：通常一个类目下用户可输入的关键属性不超过1个。
        StringBuilder input_pids = new StringBuilder();
        // 用户自行输入的子属性名和属性值，结构:"父属性值;一级子属性名;一级子属性值;二级子属性名;自定义输入值,....",多个自定义属性用','分割，input_str需要与input_pids一一对应
        StringBuilder input_str = new StringBuilder();

        public Map<String, String> build() {
            List item_props = ItemGetAction.getTempletProps(cid, null);
            buildItemPropertyWithTemplet(item_props);

            // 创建sku相关属性
            buildSkuProperties(item_props);

            return packingDate();
        }

        // 将property_alias、props、props_name、input_pids、input_str封装到Map中
        private Map<String, String> packingDate() {
            Map<String, String> dataMap = new HashMap<String, String>();
            if (props.length() > 0) {
                dataMap.put("props", props.substring(1));
            }
            if (props_name.length() > 0) {
                dataMap.put("props_name", props_name.substring(1));
            }
            if (property_alias.length() > 0) {
                dataMap.put("property_alias", property_alias.substring(1));
            }
            if (input_pids.length() > 0) {
                dataMap.put("input_pids", input_pids.substring(1));
            }
            if (input_str.length() > 0) {
                dataMap.put("input_str", input_str.substring(1));
            }

            if (sku_properties.length() > 0) {
                dataMap.put("sku_properties", sku_properties.substring(1));
            }
            if (input_custom_cpv.length() > 0) {
                dataMap.put("input_custom_cpv", input_custom_cpv.substring(1));
            }
            if (sku_prices.length() > 0) {
                dataMap.put("sku_prices", sku_prices.substring(1));
            }
            if (sku_quantities.length() > 0) {
                dataMap.put("sku_quantities", sku_quantities.substring(1));
            }
            if (sku_outer_ids.length() > 0) {
                dataMap.put("sku_outer_ids", sku_outer_ids.substring(1));
            }



            return dataMap;
        }

        // 遍历入参模板集合，根据淘宝模板规范和爬来数据的各种情况做不同的处理
        private void buildItemPropertyWithTemplet(List item_props) {
            if (item_props == null) return;
            for (int i = 0; i < item_props.size(); i++) {
                Map item_prop = (Map) item_props.get(i);
                ItemProp itemProp = new ItemProp(item_prop);
                Integer indexByNameInPropsArr = findByNameInPropsArr(itemProp.name, propInfos);
                if (indexByNameInPropsArr == -1) { // 爬来的数据中没有找到模板(itemProp)对应的数据

                    fixNotFoundInPropsArr(itemProp);

                } else {  // 爬来的数据中找到模板(itemProp)对应的数据
                    PropInfo propInfo =  propInfos.remove(indexByNameInPropsArr.intValue());
                    if (itemProp.propValues == null) {
                        if (itemProp.isSaleProp){

                            fixNullPropValuesIsSaleProp(itemProp, propInfo);

                        } else {

                            fixNullPropValuesNotSaleProp(itemProp, propInfo);

                        }
                    } else {
                        if (itemProp.multi && itemProp.isSaleProp) {

                            fillWithSkuProps(itemProp, propInfo);

                        } else if (itemProp.multi && !itemProp.isSaleProp) {

                            fillWhenMultiNotSaleProp(itemProp, propInfo);

                        } else {

                            fillWhenNotMulti(itemProp, propInfo);

                        }
                    }
                }
            }
        }

        // 修复 淘宝模板规范中没有propValues且不是销售属性的情况 itemProp：与prop对应的淘宝模板规范  prop：爬来的数据中的一条属性数据
        private void fixNullPropValuesNotSaleProp(ItemProp itemProp, PropInfo propInfo) {
            String gotName = propInfo.getName();
            String gotValue = propInfo.getValue();
            // 这里只能自定义属性 如果不能自定义而且不是必要的属性 直接忽略
            if (!itemProp.must && itemProp.isEnumProp && !itemProp.isInputProp) return;

            if (itemProp.multi) {
                // 多属性自定义
                input_str.append(",").append(gotName);
                input_pids.append(",").append(itemProp.pid);
                String[] gotValues = gotValue.split(" ");
                for (String splitValue : gotValues) {
                    input_str.append(";").append(splitValue);
                }
            } else {
                gotValue = fixCustomPvalue(gotValue);
                input_pids.append(",").append(itemProp.pid);
                input_str.append(",").append(gotValue);
            }

        }

        // 当该属性不可以多选时，填充date   itemProp：与prop对应的淘宝模板规范  prop：爬来的数据中的一条属性数据
        private void fillWhenNotMulti(ItemProp itemProp, PropInfo propInfo) {
            String gotValue = propInfo.getValue();
            String gotName = propInfo.getName();
            List<PropValue> propValues = itemProp.propValues;
            String propPid = itemProp.pid;
            String propName = itemProp.name;

            gotValue = fixGotValue(gotName, gotValue);
            // 遍历propValues搜索与gotValue类似的PropValue  返回索引下标
            Integer indexSearch = new SearchInList(propValues, gotValue, new SearchInList.GetCheckObject() {
                @Override
                public String getCheckObject(List list, Integer index) {
                    PropValue propValue = (PropValue) list.get(index);
                    return propValue.name;
                }
            }).search();

            if (indexSearch != -1) { // 在模板(itemProp.propValues)中找到相似或相等的属性值
                PropValue propValue = propValues.get(indexSearch);
                String vid = propValue.vid;
                appendVal(props, props_name, propPid, vid, gotName, gotValue);

                //递归二级类型
                if (propValue.isParent != null && propValue.isParent) {
                    String childPath = propPid + ":" +vid;
                    List item_props = ItemGetAction.getTempletProps(cid, childPath);
                    buildItemPropertyWithTemplet(item_props);
                }
            } else { // 在模板(itemProp.propValues)中不能找到相似或相等的属性值
                if (itemProp.isEnumProp && !itemProp.isInputProp) { // 不支持自定义，则取第一个属性值
                    PropValue propValue = propValues.get(0);
                    String vid = propValue.vid;
                    String vname = propValue.name;
                    appendVal(props, props_name, propPid, vid, propName, vname);

                    //递归二级类型
                    if (propValue.isParent != null && propValue.isParent) {
                        String childPath = propPid + ":" +vid;
                        List item_props = ItemGetAction.getTempletProps(cid, childPath);
                        buildItemPropertyWithTemplet(item_props);
                    }
                } else {  // 自定义属性值
                    input_pids.append(",").append(propPid);
                    input_str.append(",").append(gotValue);

                    // 修复自定义属性“品牌”下含有一级子属性“型号”的情况
                    fixCustomProp20000(propPid);
                }
            }
        }

        // 修复自定义属性“品牌”下含有一级子属性“型号”的情况
        private void fixCustomProp20000(String propPid) {
            if (!"20000".equals(propPid)) return;
            // 尝试从原宝贝的属性中寻找属性名为“型号”的属性
            Integer indexOfVersion = findByNameInPropsArr("型号", propInfos);
            if (indexOfVersion == -1) return;
            // 找到了  ,伟业电机(自定义品牌名) --> ,伟业电机(自定义品牌名);型号;U880(自定义型号名)
            PropInfo propInfo = propInfos.get(indexOfVersion);
            input_str.append(";").append("型号").append(";").append(propInfo.getValue());
        }

        // 当该属性可以多选但不是销售属性时，填充date   itemProp：与prop对应的淘宝模板规范  prop：爬来的数据中的一条属性数据
        private void fillWhenMultiNotSaleProp(ItemProp itemProp, PropInfo propInfo) {
            List<PropValue> propValues = itemProp.propValues;
            String gotValue = propInfo.getValue();
            String propPid = itemProp.pid;
            String propName = itemProp.name;
            String gotName = propInfo.getName();

            String[] gotValues = gotValue.split(" ");

            // 普通属性值中是否含有需要自定义的属性
            Boolean hasCustomProp = false;

            for (String splitValue : gotValues) {
                // 遍历propValues搜索与splitValue类似的PropValue  返回索引下标
                Integer indexSearch = new SearchInList(propValues, splitValue, new SearchInList.GetCheckObject() {
                    @Override
                    public String getCheckObject(List list, Integer index) {
                        PropValue propValue = (PropValue) list.get(index);
                        return propValue.name;
                    }
                }).search();

                if (indexSearch != -1) { // 找到相似的属性值
                    PropValue propValue = propValues.get(indexSearch);
                    String vid = propValue.vid;
                    String vname = propValue.name;
                    appendVal(props, props_name, propPid, vid, gotName, vname);

                    //递归二级类型
                    if (propValue.isParent != null && propValue.isParent) {
                        String childPath = propPid + ":" +vid;
                        List item_props = ItemGetAction.getTempletProps(cid, childPath);
                        buildItemPropertyWithTemplet(item_props);
                    }

                } else { // 找不到相似的属性值
                    if (itemProp.isEnumProp && !itemProp.isInputProp) { // 不支持自定义，则取第一个属性值
                        PropValue propValue = propValues.get(0);
                        String vname = propValue.name;
                        String vid = propValue.vid;
                        appendVal(props, props_name, propPid, vid, propName, vname);

                        //递归二级类型
                        if (propValue.isParent != null && propValue.isParent) {
                            String childPath = propPid + ":" +vid;
                            List item_props = ItemGetAction.getTempletProps(cid, childPath);
                            buildItemPropertyWithTemplet(item_props);
                        }
                    } else { // 自定义属性值
                        if (!hasCustomProp) {
                            input_str.append(",").append(propName);
                            input_pids.append(",").append(propPid);
                            hasCustomProp = true;
                        }

                        input_str.append(";").append(splitValue);
                    }
                }
            }
        }

        // 修复 爬来数据中未能找到淘宝模板规范对应的数据的情况
        private void fixNotFoundInPropsArr(ItemProp itemProp) {
            List<PropValue> prop_values = itemProp.propValues;
            String propName = itemProp.name;
            String propPid = itemProp.pid;


            if (itemProp.must) {
                if (prop_values == null) {
                    String vid = getRandomNum(5);
                    String vName = "未知";
                    if (propName.contains("成分") || propName.contains("含量")) {
                        vName += "100%";
                    }
                    if(propName.contains("日期范围")) {
                        vName = "2017-01-01至2017-12-30";
                    }
                    if(propName.contains("尺寸")) {
                        vName = "10x10cm";
                    }
                    if (propName.contains("型号")) {
                        vName = "未知型号";
                    }
                    if (propName.contains("生产厂家地址")) {
                        vName = "未知地址";
                    }
                    if(itemProp.isSaleProp) {
                        input_pids.append(",").append(propPid);
                        input_str.append(",").append(propName).append(";").append(vName);
                    } else {
                        input_pids.append(",").append(propPid);
                        input_str.append(",").append(vName);
                    }
                    appendVal(props, props_name, propPid, vid, propName, vName);
                } else {
                    Integer indexOtherInPropValues = findOtherInPropValues(prop_values);

                    if (indexOtherInPropValues == -1) { // 未找到选第一个
                        PropValue propValue = prop_values.get(0);
                        String vname = propValue.name;
                        String vid = propValue.vid;
                        appendVal(props, props_name, propPid, vid, propName, vname);
                    } else { // 找到了
                        PropValue propValue = prop_values.get(indexOtherInPropValues);
                        String vname = propValue.name;
                        String vid = propValue.vid;
                        appendVal(props, props_name, propPid, vid, propName, vname);

                        //递归二级类型
                        if (propValue.isParent != null && propValue.isParent) {
                            String childPath = propPid + ":" +vid;
                            List item_props = ItemGetAction.getTempletProps(cid, childPath);
                            buildItemPropertyWithTemplet(item_props);
                        }
                    }
                }
            }


        }

        // 遍历propsArr找到对应name的prop 返回prop的位置索引
        private Integer findByNameInPropsArr(String name, List<PropInfo> propInfos) {
            if (propInfos == null) return -1;
            for (int i = 0; i < propInfos.size(); i++) {
                PropInfo propInfo = propInfos.get(i);
                String $name = propInfo.getName();
                if (name.equals($name) || hasAlias(name, $name)) return i;
            }

            return -1;
        }

        // 遍历propValues找到name包含other/其他对应的PropValue 返回PropValue的位置索引
        private Integer findOtherInPropValues(List<PropValue> propValues) {
            if (propValues == null) return -1;
            for (int i = 0; i < propValues.size(); i++) {
                String vname = propValues.get(i).name;
                if (vname.contains("other/其他")) return i;
            }

            return -1;
        }

        // 对普通属性自定义属性值中的违规词语进行处理
        private String fixCustomPvalue(String name) {
            // 您自定义的属性值 存在违禁词 雾霾，请去掉后重新再提交
            // 您自定义的属性值 存在违禁词  维尼,情人
            if (name.contains("雾霾")) name = name.replace("雾霾", "浅艾");
            if (name.contains("毒药")) name = name.replace("毒药", "**");
            if (name.contains("维尼")) name = name.replace("维尼", "維尼");
            if (name.contains("情人")) name = name.replace("情人", "**");
            // 属性值里不能存在英文的冒号|分号|逗号  因为连接属性值的就是用的英文的冒号|分号|逗号
            if (name.contains(":")) name = name.replace(":", "：");
            if (name.contains(";")) name = name.replace(";", "；");
            if (name.contains(",")) name = name.replace(",", "，");

            return name;
        }

        // 对销售属性自定义属性值中的违规词语进行处理
        private String fixSalePropCustomPvalue(String name) {
            name = fixCustomPvalue(name);
            /** 双字节字符正则表达式 长度不能大于30字节*/
            final Pattern p = Pattern.compile("[^\\x00-\\xff]");
            char ch;
            int count = 0;
            for (int i = 0; i < name.length(); i++) {
                ch = name.charAt(i);
                Matcher m = p.matcher(String.valueOf(ch));
                if(m.find()) {
                    count += 2;
                } else {
                    count += 1;
                }
                if(count > 30) {
                    return name.substring(0, i);
                }
            }

            return name;
        }

        // 存放可以取别名的属性集合
        List listForAppendAlias;

        // 修复模板不正确的情况  模板表示可以自定义属性，但提示错误当前类目不支持尺码自定义销售属性值
        private boolean fixIncorrectTemplet(ItemProp itemProp, String name, String valueId) {
            List<PropValue> tempPropsArr = itemProp.propValues;
            String propPid = itemProp.pid;
            String gotName = itemProp.name;
            String sizeForCid20509 = extractSizeForCid20509(name);

            // 初始化集合  将模板的属性集合添加进去
            if (listForAppendAlias == null) {
                listForAppendAlias = new ArrayList();
                listForAppendAlias.addAll(tempPropsArr);
            }

            // “no match”表示提取不到合适的尺码  直接跳过这一步
            if (!"no match".equals(sizeForCid20509)) {
                for (int k = 0; k < tempPropsArr.size(); k++) {
                    PropValue propValue = tempPropsArr.get(k);
                    if (isEqualsForForCid20509(propValue.name, sizeForCid20509)) {
                        // 匹配上模板中的属性没有被取过别名 才能取别名
                        if (listForAppendAlias.contains(propValue)) {
                            listForAppendAlias.remove(propValue);
                            property_alias.append(";").append(propPid).append(":").append(propValue.vid).append(":").append(name);
                        }
                        appendVal(props, props_name, propPid, propValue.vid, gotName, name);
                        // 替换逻辑
                        String oldProps = propPid + ":" + valueId;
                        Property newProps = new DoReplaceProperty(propPid, propValue.vid, gotName, name);
                        replaceMap.put(oldProps, newProps);
                        return true;
                    }
                }
            }

            // 所有的属性都取过别名了 没有可用的属性来给当前name取别名了 直接不处理当前name
            // 直接不处理会导致 宝贝销售属性出错（销售属性和商品属性不一致）
            if (listForAppendAlias != null && listForAppendAlias.size() == 0) {
                // 替换逻辑 DoDeleteProperty表示 在替换时直接将sku_properties中含有oldProps的sku删除
                String oldProps = propPid + ":" + valueId;
                Property newProps = new DoDeleteProperty();
                replaceMap.put(oldProps, newProps);
                return true;
            }

            // 模板还有没有被取过别名的属性值  对匹配不上的属性值 进行取别名操作
            if (listForAppendAlias != null && listForAppendAlias.size() > 0) {
                PropValue propValue = (PropValue) listForAppendAlias.remove(listForAppendAlias.size() - 1);
                property_alias.append(";").append(propPid).append(":").append(propValue.vid).append(":").append(name);
                appendVal(props, props_name, propPid, propValue.vid, gotName, name);
                // 替换逻辑
                String oldProps = propPid + ":" + valueId;
                Property newProps = new DoReplaceProperty(propPid, propValue.vid, gotName, name);
                replaceMap.put(oldProps, newProps);
                return true;
            }


            return false;
        }

        // 提取尺寸
        private String extractSizeForCid20509(String name) {
            // XL（180/96A） == XL
            // 大码3XL == XXXL
            // 女M送男XL == M   女XXL送男L == XXL
            Pattern pattern = Pattern.compile("(X*S|M|X*L|\\dXL)");
            Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
                return matcher.group();
            } else {
                // 匹配不到合适的尺码 默认取模板第一个属性来取别名
                return "no match";
            }
        }

        // 3XL == XXXL 返回true
        private boolean isEqualsForForCid20509(String var1, String var2) {
            if (var1.equals(var2)) return true;

            var1 = standardizeForCid20509(var1);
            var2 = standardizeForCid20509(var2);
            if (var1.equals(var2)) return true;

            return false;
        }

        // 3XL 转换成 XXXL  大号均码 转换成 M
        private String standardizeForCid20509(String var) {
            Pattern pattern = Pattern.compile("^(\\d)XL$");
            Matcher m1 = pattern.matcher(var.trim());
            if (m1.find()) {
                int num = Integer.valueOf(m1.group(1));
                var = "";
                for (int i = 0; i < num; i++) var += "X";
                var += "L";
            }

            if (var.equals("大号均码")) var = "M";

            return var;

        }

        StringBuilder sku_properties = new StringBuilder();
        StringBuilder input_custom_cpv = new StringBuilder();
        StringBuilder sku_prices = new StringBuilder();
        StringBuilder sku_quantities = new StringBuilder();
        StringBuilder sku_outer_ids = new StringBuilder();

        Map replaceMap = new HashMap();

        Integer sequenceOfVid = -1;

        Set<String> salePropPids = new HashSet<String>(); // 保存淘宝模板中所有销售属性的pid

        // 当该属性可以多选且是销售属性时，填充date   itemProp：与skuProp对应的淘宝模板规范  skuProp：爬来数据中skuProps中一条数据
        private void fillWithSkuProp(ItemProp itemProp, SkuPropInfo skuPropInfo) {
            List<PropValue> tempPropsArr = itemProp.propValues;
            String propPid = itemProp.pid;
            String propName = itemProp.name;
            Boolean isEnumProp = itemProp.isEnumProp;
            Boolean isInputProp = itemProp.isInputProp;
            Boolean isAllowAlias = itemProp.isAllowAlias;

            List<SkuPropValueInfo> skuPropValueInfos = skuPropInfo.getValues();
            boolean hasSelfProp = true;
            outSku:
            for (int n = 0; n < skuPropValueInfos.size(); n++) {
                SkuPropValueInfo skuPropValueInfo = skuPropValueInfos.get(n);
                String name = skuPropValueInfo.getName();
                String valueId = skuPropValueInfo.getVid();

                Integer match = skuPropValueMatchTemplet(tempPropsArr, skuPropValueInfo);
                if (match != -1) {
                    // 在模板中找到了vid|name相等的属性值
                    PropValue propValue = tempPropsArr.get(match);
                    if (propValue.name.equals(name)) {
                        if (propValue.vid.equals(valueId)) {
                            // 最佳情况  vid和name都相等
                            appendVal(props, props_name, propPid, valueId, propName, name);
                        } else {
                            // 找到相同name但是vid不同
                            // 替换原来的vid
                            appendVal(props, props_name, propPid, propValue.vid, propName, name);
                            // 替换逻辑
                            String oldProps = propPid + ":" + valueId;
                            Property newProps = new DoReplaceProperty(propPid, propValue.vid, propName, propValue.name);
                            replaceMap.put(oldProps, newProps);
                        }
                    } else if (propValue.vid.equals(valueId)){
                        if (isAllowAlias) {
                            // 模板中找到了对应的vid但是name不同  允许去别名
                            // 取别名
                            appendVal(props, props_name, propPid, valueId, propName, name);
                            property_alias.append(";").append(propPid).append(":").append(valueId).append(":").append(name);
                        } else {
                            // 模板中找到了对应的vid但是name不同而且不允许取别名
                            // 使用模板中的名字去替换原来的名字
                            appendVal(props, props_name, propPid, valueId, propName, propValue.name);
                        }
                    } else {
                        ;
                    }
                } else {
                    // 标准属性中未匹配到，应该是自定义的属性值
                    if (isEnumProp && !isInputProp) {
                        ////不能自定义  则取第一个属性值
                        PropValue firstTempletPropsValue = tempPropsArr.get(0);
                        appendVal(props, props_name, propPid, firstTempletPropsValue.vid, propName, firstTempletPropsValue.name);
                        // 替换逻辑
                        String oldProps = propPid + ":" + valueId;
                        Property newProps = new DoReplaceProperty(propPid, firstTempletPropsValue.vid, propName, firstTempletPropsValue.name);
                        replaceMap.put(oldProps, newProps);
                    } else {
                        // 修复模板isEnumPro和isInputProp都为真但是不能自定义的情况
                        // 添加特殊属性pid为122216547 也是自定义属性报错 报错宝贝：563726927961、561642149604、563703727355
                        // pid为3344920(重量) 报错宝贝：561857761061、561857657328、561929674223
                        if (propPid.equals("20509") //
                                || (cid.equals(50021413L) && propPid.equals("122216547")) //
                                || (cid.equals(50006236L) && propPid.equals("3344920"))) {
                            boolean hasFix = fixIncorrectTemplet(itemProp, name, valueId);
                            if (hasFix) continue;
                        }

                        // 对自定义属性值中的违规字符进行处理
                        name = fixSalePropCustomPvalue(name);
                        appendVal(props, props_name, propPid, valueId, propName, name);
                        // 自定义销售属性值
                        if (hasSelfProp) {
                            input_pids.append(",").append(propPid);
                            input_str.append(",").append(propName);
                            hasSelfProp = false;
                        }
                        input_str.append(";").append(name);
                        // 替换逻辑
                        String oldProps = propPid + ":" + valueId;
                        Property newProps = new DoCustomProperty(propPid, (sequenceOfVid--).toString(), propName, name);
                        replaceMap.put(oldProps, newProps);
                    }
                }
            }
        }

        private Integer skuPropValueMatchTemplet(List<PropValue> templet, SkuPropValueInfo skuPropValueInfo) {
            for (int i = 0; i < templet.size(); i++) {
                PropValue tempProp = templet.get(i);
                // 优先匹配name相等的
                String vName = tempProp.name;
                String vid = tempProp.vid;
                String name = skuPropValueInfo.getName();
                String valueId = skuPropValueInfo.getVid();
                if (vName.equals(name)) return i;
                if (vid.equals(valueId)) {
                    for (int j = 0; j < templet.size(); j++) {
                        tempProp = templet.get(i);
                        vName = tempProp.name;
                        if (vName.equals(name)) return j;
                    }
                    return i;
                }
            }
            return -1;
        }

        // 当该属性可以多选且是销售属性时，遍历skuProps找到与prop对应的skuProp，填充date  itemProp：与prop对应的淘宝模板规范  prop：爬来的数据中的一条属性数据
        private void fillWithSkuProps(ItemProp itemProp, PropInfo propInfo) {
            Integer indexByPropNameInSkuProps = findByPropNameInSkuProps( propInfo.getName(), skuPropInfos);
            if (indexByPropNameInSkuProps == -1) {
                // 模板匹配上了爬来数据props中的属性名且模板表示这个属性名是销售属性和多重的属性，但是在爬来数据skuProps的找不到该属性名
                // 则当做多重的非销售属性来处理
                fillWhenMultiNotSaleProp(itemProp, propInfo);
            } else {
                SkuPropInfo skuPropInfo = skuPropInfos.get(indexByPropNameInSkuProps);
                fillWithSkuProp(itemProp, skuPropInfo);
            }
        }

        // 遍历skuProps找到对应propName的skuProp  返回找到skuProp的位置
        private Integer findByPropNameInSkuProps(String propName, List<SkuPropInfo> skuPropInfos) {
            if (skuPropInfos == null) return -1;
            for (int i = 0; i < skuPropInfos.size(); i++) {
                SkuPropInfo skuPropInfo = skuPropInfos.get(i);
                String $propName = skuPropInfo.getName();
                if (propName.equals($propName)) return i;
            }

            return -1;
        }

        // 修复 淘宝模板规范中没有propValues且是销售属性的情况 itemProp：与prop对应的淘宝模板规范  prop：爬来的数据中的一条属性数据
        private void fixNullPropValuesIsSaleProp(ItemProp itemProp, PropInfo propInfo) {
            String gotName = propInfo.getName();
            Integer index = findByPropNameInSkuProps(gotName, skuPropInfos);
            if (index == -1) return;
            SkuPropInfo skuPropInfo =  skuPropInfos.get(index);

            List<SkuPropValueInfo> skuPropValueInfos = skuPropInfo.getValues();
            if (skuPropValueInfos == null) return;
            input_str.append(",").append(gotName);
            input_pids.append(",").append(itemProp.pid);

            for (SkuPropValueInfo skuPropValueInfo : skuPropValueInfos) {
                String vid = skuPropValueInfo.getVid();
                String name = skuPropValueInfo.getName();
                input_str.append(";").append(name);
                appendVal(props, props_name, itemProp.pid, vid, gotName, name);
                // 替换逻辑
                String oldProps = itemProp.pid + ":" + vid;
                Property newProps = new DoCustomProperty(itemProp.pid, (sequenceOfVid--).toString(), itemProp.name, name);
                replaceMap.put(oldProps, newProps);
            }

        }

        // 创建有关sku的属性参数
        private void buildSkuProperties(List item_props) {
            initSalePropPids(item_props);
            checkReplaceMap();
            if (skuCore == null) return;
            Set<Map.Entry<String, SkuCoreInfo>> skuCoreSet = skuCore.entrySet();
            for (Map.Entry<String, SkuCoreInfo> entry : skuCoreSet) {
                // 对value做处理
                String skuId = entry.getKey();
                SkuCoreInfo skuCoreInfo = entry.getValue();
                String skuPrice = skuCoreInfo.getOriginalPrice();
                String skuQuantity = skuCoreInfo.getQuantity();
                String skuPropPath = skuCoreInfo.getPropPath();
                skuPropPath = processSkuPropPath(skuPropPath);
                // 返回key为空或者价格小于等于0  直接忽略这个sku属性
                if (Double.valueOf(skuPrice) <= 0 || StringUtils.isEmpty(skuPropPath)) continue;
                sku_prices.append(",").append(skuPrice);
                sku_quantities.append(",").append(skuQuantity);
                sku_outer_ids.append(",").append(skuId);
                sku_properties.append(",").append(skuPropPath);
            }
            Set<Map.Entry> replaceMapSet = replaceMap.entrySet();
            for (Map.Entry entry : replaceMapSet) {
                Property value = (Property) entry.getValue();
                if (value instanceof DoCustomProperty) {
                    input_custom_cpv.append(";").append(value.getPid()).append(":").append(value.getVid()).append(":").append(value.getVname());
                }
            }

        }

        // 初始化销售属性Pid集合
        private void initSalePropPids(List item_props) {
            if (item_props == null) return;
            for (int i = 0; i < item_props.size(); i++) {
                Map item_prop = (Map) item_props.get(i);
                ItemProp itemProp = new ItemProp(item_prop);
                if (itemProp.isSaleProp) {
                    salePropPids.add(itemProp.pid);
                }
            }
        }

        // 替换前 核实replaceMap
        private void checkReplaceMap() {
            // 对自定义属性的属性做个数限制  每一种sku属性自定义数限制为24个
            Set<Map.Entry> set = replaceMap.entrySet();
            Iterator<Map.Entry> iterator = set.iterator();
            // 存放sku自定义个数
            Map<String, Integer> numMap = new HashMap<String, Integer>();
            while (iterator.hasNext()) {
                Map.Entry entry = iterator.next();
                Property property = (Property) entry.getValue();
                if (property instanceof DoCustomProperty) {
                    String pid = property.getPid();
                    Integer num = numMap.get(pid);
                    if (num == null) {
                        num = new Integer(1);
                        numMap.put(pid, num);
                    }
                    num += 1;
                    numMap.put(pid, num);

                    if (num > 24) entry.setValue(new DoDeleteProperty());
                }
            }
        }

        // 使用replaceMap替换sku属性
        private String processSkuPropPath(String key) {
            if (key == null) return null;

            List<String> list = new ArrayList<String>();
            for (String pv : key.split(";")) {
                if (!checkPV(pv)) continue;

                Property newProps = (Property) replaceMap.get(pv);
                if (newProps == null) {
                    list.add(pv);
                } else if (newProps instanceof DoReplaceProperty) {
                    // 替换sku_properties里这个属性
                    list.add(newProps.getPid() + ":" + newProps.getVid());
                } else if (newProps instanceof DoCustomProperty) {
                    // 替换sku_properties里这个属性
                    list.add(newProps.getPid() + ":" + newProps.getVid());
                } else if (newProps instanceof DoDeleteProperty) {
                    // 删除sku_properties里这个属性
                    return null;
                }
            }

            return StringUtils.join(list, ";");
        }

        // 对pid或vid小于0的返回false
        private Boolean checkPV(String pv) {
            String[] split = pv.split(":");
            if (split.length == 2) {
                String pid = split[0];
                String vid = split[1];
                if (Long.valueOf(pid) < 0L || Long.valueOf(vid) < 0L) return false;
                if (!salePropPids.contains(pid)) return false;
                return true;
            }
            return false;
        }

    }

    static class ItemProp {
        Boolean isAllowAlias;
        Boolean isColorProp;
        Boolean isEnumProp;
        Boolean isInputProp;
        Boolean isSaleProp;
        Boolean multi;
        Boolean must;
        String name;
        String pid;
        List<PropValue> propValues;

        public ItemProp(Map map) {
            this.isAllowAlias = (Boolean) map.get("is_allow_alias");
            this.isColorProp = (Boolean) map.get("is_color_prop");
            this.isEnumProp = (Boolean) map.get("is_enum_prop");
            this.isInputProp = (Boolean) map.get("is_input_prop");
            this.isSaleProp = (Boolean) map.get("is_sale_prop");
            this.multi = (Boolean) map.get("multi");
            this.must = (Boolean) map.get("must");
            this.name = (String) map.get("name");
            this.pid = String.valueOf(map.get("pid"));
            Map prop_values = (Map) map.get("prop_values");
            if (prop_values == null) {
                this.propValues = null;
            } else {
                List propValues = new ArrayList();
                for (Map prop_value : (List<Map>)prop_values.get("prop_value"))
                    propValues.add(new PropValue(prop_value));
                this.propValues = propValues;
            }
        }
    }

    static class PropValue {
        Boolean isParent;
        String name;
        String vid;

        public PropValue(Map map) {
            this.isParent = (Boolean) map.get("is_parent");
            this.name = (String) map.get("name");
            this.vid = String.valueOf(map.get("vid"));
        }
    }

    static class SearchInList {
        List list;
        String targetValue;
        Integer loopStartIndex;
        Integer matchEqualCheck;
        EnumSet<EqualCheck> equalCheckSet;
        GetCheckObject listGetMethod;

        public SearchInList(List list, String targetValue, GetCheckObject listGetMethod) {
            this.list = list;
            this.targetValue = targetValue;
            this.listGetMethod = listGetMethod;
            this.loopStartIndex = 0;
            equalCheckSet = EnumSet.of(EqualCheck.equal, EqualCheck.replace, EqualCheck.contain);
            this.matchEqualCheck = equalCheckSet.size();
        }

        enum EqualCheck{
            equal {
                @Override
                public Boolean equal(String var1, String var2) {
                    return var1.equals(var2);
                }
            },
            replace {
                @Override
                public Boolean equal(String var1, String var2) {
                    return var1.replaceAll("\\(.*?\\)", "").equals(var2.replaceAll("\\(.*?\\)", ""));
                }
            },
            contain {
                @Override
                public Boolean equal(String var1, String var2) {
                    return var1.contains(var2) || var2.contains(var1);
                }
            };

            public abstract Boolean equal(String var1, String var2);
        }

        public Integer search() {
            for (int i = loopStartIndex; i < list.size(); i++) {
                String value = listGet(i);
                Iterator<EqualCheck> iterator = equalCheckSet.iterator();
                for (int j = 0; iterator.hasNext() && j < matchEqualCheck; j++) {
                    EqualCheck equalCheck = iterator.next();
                    if (equalCheck.equal(value, targetValue)) {
                        if (j == 0) return i;
                        matchEqualCheck = j;
                        loopStartIndex = i + 1;
                        Integer integer = search();
                        if (integer != -1) return integer;

                        return i;
                    }
                }
            }

            return -1;

        }

        public String listGet(Integer index) {
            if (listGetMethod != null) return listGetMethod.getCheckObject(list, index);

            Map map = (Map) list.get(index);
            return (String) map.get("name");
        }

        interface GetCheckObject {
            String getCheckObject(List list, Integer index);
        }

    }


}
