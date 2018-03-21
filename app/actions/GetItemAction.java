package actions;

import bustbapi.TBApi;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.taobao.api.ApiException;
import com.taobao.api.TaobaoClient;
import com.taobao.api.domain.FoodSecurity;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.Location;
import com.taobao.api.domain.Sku;
import com.taobao.api.request.ItempropsGetRequest;
import com.taobao.api.response.ItempropsGetResponse;
import play.libs.WS;
import java.util.*;

/**
 * Created by hao on 15-4-8.
 * 用于从页面获取Item信息
 */

public class GetItemAction {

    public static Item getItem(long numIid) {
        Item item = new Item();
        Date curDate = new Date();
        item.setListTime(curDate);
//        item.setDelistTime(curDate);
        item.setNumIid(numIid);
        item.setApproveStatus("onsale");
        item.setModified(curDate);
        item.setHasShowcase(true);
        item.setIsVirtual(false);
        item.setStuffStatus("new");
        item.setType("fixed");

        String wapUrl = "http://hws.m.taobao.com/cache/wdetail/5.0/?id=" + numIid + "&ttid=2013@taobao_h5_1.0.0&exParams={}";
        String detailUrl = "http://item.taobao.com/item.htm?id=" + numIid;
        item.setDetailUrl(detailUrl);

        String string = WS.url(wapUrl).get().getString();

        JSONObject jsonObject = JSON.parseObject(string);
        JSONObject data = jsonObject.getJSONObject("data");

        String apiStack = data.getString("apiStack");
        String price = getPropVal(apiStack, "price");
        item.setPrice(price);
        String num = getPropVal(apiStack, "quantity");
        item.setNum(Long.parseLong(num));

        JSONObject itemInfoModel = data.getJSONObject("itemInfoModel");
        String title = itemInfoModel.getString("title");
        item.setTitle(title);

        Long cid = itemInfoModel.getLong("categoryId");
        item.setCid(cid);
        String location = itemInfoModel.getString("location");
        Location local = new Location();
        local.setCity(location);
        item.setLocation(local);

        String picUrl = itemInfoModel.getJSONArray("picsPath").getString(0);
        item.setPicUrl(picUrl);

        JSONObject seller = data.getJSONObject("seller");
        String nick = seller.getString("nick");
        item.setNick(nick);


        JSONArray propsArr = data.getJSONArray("props");
        JSONArray templetProps = getTempletProps(cid);

        boolean hasSku = itemInfoModel.getBooleanValue("sku");
        JSONArray skuProps = null;
        if (hasSku) {
            JSONObject skuModel = data.getJSONObject("skuModel");
            // skuProps, 为了获取身高颜色信息
            skuProps = skuModel.getJSONArray("skuProps");

            String ppathIdmap = skuModel.getString("ppathIdmap");
            List<JSONObject> tempSaleProp = getTempSaleProp(templetProps);
            List<Sku> skus = getSkus(numIid, apiStack, ppathIdmap, tempSaleProp);
            item.setSkus(skus);
        }
        FoodSecurity foodSecurity = new FoodSecurity();
        Map<String, String> props = getProps(propsArr, skuProps, templetProps, foodSecurity);
        item.setFoodSecurity(foodSecurity);
        item.setProps(props.get("props"));
        item.setPropsName(props.get("props_name"));
        item.setPropertyAlias(props.get("property_alias"));
        item.setInputPids(props.get("input_pids"));
        item.setInputStr(props.get("input_str"));

        JSONObject descInfo = data.getJSONObject("descInfo");
        String fullDescUrl = descInfo.getString("fullDescUrl");
        String _descFull = WS.url(fullDescUrl).get().getString();
        String desc = JSON.parseObject(_descFull).getJSONObject("data").getString("desc");
        desc = desc.replaceAll("</?(html|body|head)>", "").trim();
        item.setDesc(desc);

        String wirelessDesUrl = "http://hws.m.taobao.com/cache/mtop.wdetail.getItemDescx/4.1/?data=%7B%22item_num_id%22%3A%22" + numIid + "%22%7D";
        String wirelessDesc = WS.url(wirelessDesUrl).get().getString();
        JSONObject wirelessDescObj = JSON.parseObject(wirelessDesc).getJSONObject("data");
        JSONArray pages = wirelessDescObj.getJSONArray("pages");
        StringBuilder wirelessDescSb = new StringBuilder();
        wirelessDescSb.append("<wapDesc><shortDesc>").append(wirelessDescObj.getString("summary")).append("</shortDesc>");
        for (Object page : pages) {
            wirelessDescSb.append(page);
        }
        wirelessDescSb.append("</wapDesc>");

        String wireless_desc = wirelessDescSb.toString();
        item.setWirelessDesc(wireless_desc);

//TODO seller_cids取不到, foodsecurity中日期取不到
        return item;
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

        // 得到价格，但是这里是string类型的,需要进行类型转换
        String mobilePriceStr = origin.substring(startI, stopI);
        return mobilePriceStr;
    }

    /**
     * 获取标准商品类目属性
     *
     * @param cid
     * @return
     */
    public static JSONArray getTempletProps(long cid) {
        TaobaoClient client = TBApi.genClient();
        ItempropsGetRequest req = new ItempropsGetRequest();
        req.setFields("pid,name,is_sale_prop,is_color_prop,is_input_prop,prop_values");
        req.setCid(cid);
        try {
            ItempropsGetResponse response = client.execute(req);
            String body = response.getBody();
            JSONArray jsonArray = JSON.parseObject(body).getJSONObject("itemprops_get_response")
                    .getJSONObject("item_props")
                    .getJSONArray("item_prop");
            return jsonArray;
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
     * @param templetProps 标准的模板属性
     * @return
     */
    protected static Map<String, String> getProps(JSONArray propsArr, JSONArray skuProps, JSONArray templetProps, FoodSecurity foodSecurity) {
        StringBuilder property_alias = new StringBuilder();
        StringBuilder props = new StringBuilder();
        StringBuilder props_name = new StringBuilder();
        StringBuilder input_pids = new StringBuilder();
        StringBuilder input_str = new StringBuilder();

        out:
        for (Object gotObj : propsArr) {
            JSONObject gotJsonObj = JSON.parseObject(gotObj.toString());
            String gotName = gotJsonObj.getString("name");
            String gotValue = gotJsonObj.getString("value");
            for (Object templetProp : templetProps) {
                JSONObject templetPropObj = JSON.parseObject(templetProp.toString());

                if (templetPropObj.getString("name").equals(gotName)) {
                    String pid = templetPropObj.getString("pid");

                    //prop_value数组
                    JSONObject prop_values = templetPropObj.getJSONObject("prop_values");

                    if (prop_values == null) {
                        // 随机5位的vid
                        String vid = getRandomNum(5);
                        input_pids.append(",").append(pid);
                        input_str.append(",").append(gotValue);
                        appendVal(props, props_name, pid, vid, gotName, gotValue);
                        continue out;
                    }
                    //模板的类别的prop_value
                    JSONArray tempPropVoArr = prop_values.getJSONArray("prop_value");
                    for (Object tempPropVo : tempPropVoArr) {
                        JSONObject tempPVO = JSON.parseObject(tempPropVo.toString());
                        String voName = tempPVO.getString("name");
                        //品牌名称以 / 隔开, 需要拆开比较
                        String[] voNameArr = voName.split("/");

                        if (voName.equals(gotValue)) {
                            String vid = tempPVO.getString("vid");
                            appendVal(props, props_name, pid, vid, gotName, gotValue);
                            continue out;
                        } else {
                            for (String voname : voNameArr) {
                                if (voname.equals(gotValue)) {
                                    String vid = tempPVO.getString("vid");
                                    appendVal(props, props_name, pid, vid, gotName, gotValue);
                                    continue out;
                                }
                            }
                        }

                    }

                    //颜色和身高是以 `,` 隔开的字符串
                    // 身高和颜色等信息
                    if (skuProps != null) {
                        boolean is_color_prop = templetPropObj.getBooleanValue("is_color_prop");
                        for (Object skuProp : skuProps) {
                            JSONObject skuPropJson = JSON.parseObject(skuProp.toString());
                            if (skuPropJson.getString("propName").equals(gotName)) {
                                String propId = skuPropJson.getString("propId");
                                JSONArray valJsonArr = skuPropJson.getJSONArray("values");

                                String[] values = gotValue.split(",");
                                for (String value : values) {
                                    for (Object valObj : valJsonArr) {
                                        JSONObject valJson = JSON.parseObject(valObj.toString());
                                        if (valJson.getString("name").equals(value)) {
                                            String vid = valJson.getString("valueId");
                                            appendVal(props, props_name, pid, vid, gotName, value);
                                            if (is_color_prop) {
                                                property_alias.append(";")
                                                        .append(pid).append(":")
                                                        .append(vid).append(":")
                                                        .append(value);
                                            }
                                        }
                                    }
                                }
                                continue out;
                            }
                        }
                    }

                    // 活取5位的随机数
                    String vid = getRandomNum(5);
                    input_pids.append(",").append(pid);
                    input_str.append(",").append(gotValue);
                    appendVal(props, props_name, pid, vid, gotName, gotValue);
                    continue out;
                }
            }

            if (!getFoodSecurity(gotName, gotValue, foodSecurity)) {
//            有些的props和props_name还有省份和城市, 标准属性里没有,添加到input_pids和input_str
                String vid = getRandomNum(5);
                String pid = getRandomNum(8);
                input_pids.append(",").append(pid);
                input_str.append(",").append(gotValue);
                appendVal(props, props_name, pid, vid, gotName, gotValue);
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
     * @param saleTempProps
     * @return 返回json数组
     */
    private static List<Sku> getSkus(Long numIid, String apiStack, String ppathIdmap, List<JSONObject> saleTempProps) {
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
            Object priceUnitsObj = skuJsonObj.getJSONArray("priceUnits").get(0);
            String price = JSON.parseObject(priceUnitsObj.toString()).getString("price");

            StringBuilder propertiesNameSb = new StringBuilder();
            String[] propArr = properties.split(";");
            out:
            for (String prop : propArr) {
                String[] pid_vid = prop.split(":");
                for (JSONObject saleTempProp : saleTempProps) {
                    if (saleTempProp.getString("pid").equals(pid_vid[0])) {
                        JSONArray propValueArr = saleTempProp.getJSONObject("prop_values").getJSONArray("prop_value");
                        for (Object propValue : propValueArr) {
                            JSONObject propValueJson = JSON.parseObject(propValue.toString());
                            if (propValueJson.getString("vid").equals(pid_vid[1])) {
                                String vName = propValueJson.getString("name");
                                propertiesNameSb.append(";").append(prop)
                                        .append(":").append(saleTempProp.getString("name"))
                                        .append(":").append(vName);
                                continue out;
                            }
                        }
                    }
                }
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
    private static boolean getFoodSecurity(String gotName, String gotValue, FoodSecurity foodSecurity) {
        if (gotName.equals("生产许可证编号")) {
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
            foodSecurity.setPeriod(gotValue);
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
        } else if (gotName.equals("食品添加剂")) {
            foodSecurity.setFoodAdditive(gotValue);
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
}
