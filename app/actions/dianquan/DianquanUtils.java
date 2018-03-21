package actions.dianquan;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import models.dianquan.DianQuanItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lyl
 * @date 2017/11/03
 */
public class DianquanUtils {
    private static final Gson GSON = new Gson();

    public static boolean checkIsNeedUpdate(DianQuanItem dianQuanItem, JsonObject item) {
        return !(dianQuanItem.categoryId == (item.get("id").getAsInt())
                && dianQuanItem.categoryName.equals(item.get("cate").getAsString())
                && dianQuanItem.couponLatest == item.get("coupon_latest").getAsInt()
                && dianQuanItem.timeline == item.get("timeline").getAsLong()
                && dianQuanItem.stoptime == item.get("stoptime").getAsLong()
                && dianQuanItem.site.equals(item.get("site").getAsString())
                && dianQuanItem.title.equals(item.get("title").getAsString())
                && dianQuanItem.subTitle.equals(item.get("sub_title").getAsString())
                && dianQuanItem.isBrand == item.get("is_brand").getAsBoolean()
                && dianQuanItem.brandName.equals(item.get("brand_name").getAsString())
                && dianQuanItem.discountPrice.equals(item.get("price").getAsBigDecimal())
                && dianQuanItem.normalPrice.equals(item.get("prime").getAsBigDecimal())
                && dianQuanItem.ratio.equals(item.get("ratio").getAsBigDecimal())
                && dianQuanItem.hasFreight == item.get("freight").getAsBoolean()
                && dianQuanItem.ratioType.equals(item.get("ratio_type").getAsString())
                && dianQuanItem.url.equals(item.get("url").getAsString())
                && dianQuanItem.thumb.equals(item.get("thumb").getAsString())
                && dianQuanItem.longPic.equals(item.get("long_pic").getAsString())
                && dianQuanItem.videoUrl.equals(item.get("video").getAsString())
                && dianQuanItem.recommendedReason.equals(item.get("intro_foot").getAsString())
                && dianQuanItem.activity.equals(item.get("activity").getAsString())
                && dianQuanItem.finalSales == item.get("final_sales").getAsInt()
                && dianQuanItem.hasCoupon == item.get("coupon").getAsBoolean()
                && dianQuanItem.couponMoney.equals(item.get("coupon_money").getAsBigDecimal())
                && dianQuanItem.couponUrl.equals(item.get("coupon_url").getAsString())
                && dianQuanItem.couponTotal == item.get("coupon_total").getAsInt()
                && dianQuanItem.newUrl.equals(item.get("new_url").getAsString()));
    }

    public static DianQuanItem updateDianquanItem(DianQuanItem dianQuanItem, JsonObject item) {
        dianQuanItem.categoryId = item.get("id").getAsInt();
        dianQuanItem.categoryName = item.get("cate").getAsString();
        dianQuanItem.couponLatest = item.get("coupon_latest").getAsInt();
        dianQuanItem.timeline = item.get("timeline").getAsLong();
        dianQuanItem.stoptime = item.get("stoptime").getAsLong();
        dianQuanItem.site = item.get("site").getAsString();
        dianQuanItem.title = item.get("title").getAsString();
        dianQuanItem.subTitle = item.get("sub_title").getAsString();
        dianQuanItem.isBrand = item.get("is_brand").getAsBoolean();
        dianQuanItem.brandName = item.get("brand_name").getAsString();
        dianQuanItem.discountPrice = item.get("price").getAsBigDecimal();
        dianQuanItem.normalPrice = item.get("prime").getAsBigDecimal();
        dianQuanItem.ratio = item.get("ratio").getAsBigDecimal();
        dianQuanItem.hasFreight = item.get("freight").getAsBoolean();
        dianQuanItem.ratioType = item.get("ratio_type").getAsString();
        dianQuanItem.url = item.get("url").getAsString();
        dianQuanItem.thumb = item.get("thumb").getAsString();
        dianQuanItem.longPic = item.get("long_pic").getAsString();
        dianQuanItem.videoUrl = item.get("video").getAsString();
        dianQuanItem.recommendedReason = item.get("intro_foot").getAsString();
        dianQuanItem.activity = item.get("activity").getAsString();
        dianQuanItem.finalSales = item.get("final_sales").getAsInt();
        dianQuanItem.hasCoupon = item.get("coupon").getAsBoolean();
        dianQuanItem.couponMoney = item.get("coupon_money").getAsBigDecimal();
        dianQuanItem.couponUrl = item.get("coupon_url").getAsString();
        dianQuanItem.couponTotal = item.get("coupon_total").getAsInt();
        dianQuanItem.newUrl = item.get("new_url").getAsString();
        dianQuanItem.updateTs = System.currentTimeMillis();
        return dianQuanItem;
    }

    public static String getDianquanListJson(int type, String activityFilters, String siteFilters, String typeFilters, BigDecimal lowPrice, BigDecimal highPrice, BigDecimal ratio, BigDecimal sales, String searchText, int curr, int pageSize, int order) {
        Map<String, List<Object>> paramAndQuery = getParamAndQuery(type, activityFilters, siteFilters, typeFilters, lowPrice, highPrice, ratio, sales, searchText);
        String query = paramAndQuery.keySet().iterator().next();
        List param = paramAndQuery.get(query);
        long count = DianQuanItem.countDianquan(query, param.toArray());
        int offset = (curr - 1) * pageSize;
        List<DianQuanItem> itemList = getDianquanList(query, param, offset, pageSize, order);
        return getJsonFromDianquanItemList(itemList, count, curr, pageSize);
    }

    public static String getDianquanGidListJson(int type, String activityFilters, String siteFilters, String typeFilters, BigDecimal lowPrice, BigDecimal highPrice, BigDecimal ratio, BigDecimal sales, String searchText) {
        Map<String, List<Object>> paramAndQuery = getParamAndQuery(type, activityFilters, siteFilters, typeFilters, lowPrice, highPrice, ratio, sales, searchText);
        String query = paramAndQuery.keySet().iterator().next();
        List param = paramAndQuery.get(query);
        List<DianQuanItem> itemList = DianQuanItem.findDianQuanItemList(query, param.toArray());
        return getGidListJson(itemList);
    }

    private static Map<String, List<Object>> getParamAndQuery(int type, String activityFilters, String siteFilters, String typeFilters, BigDecimal lowPrice, BigDecimal highPrice, BigDecimal ratio, BigDecimal sales, String searchText) {
        Map<String, List<Object>> map = new HashMap<String, List<Object>>(1);
        StringBuilder query = new StringBuilder("1=1 ");
        List<Object> params = new ArrayList<Object>();

        query.append("and stoptime>? ");
        params.add(System.currentTimeMillis() / 1000);
        if (type != -1) {
            query.append(" and categoryId=? ");
            params.add(type);
        }
        if (activityFilters.trim().length() != 0) {
            query.append(" and (1=0 ");
            for (String filter : activityFilters.split("\\|")) {
                query.append(" or activity=? ");
                params.add(filter);
            }
            query.append(") ");
        }
        if (siteFilters.trim().length() != 0) {
            query.append(" and (1=0 ");
            for (String filter : siteFilters.split("\\|")) {
                query.append(" or site=? ");
                params.add(filter);
            }
            query.append(") ");
        }
        if (typeFilters.trim().length() != 0) {
            for (String filter : typeFilters.split("\\|")) {
                if ("freight".equals(filter)) {
                    query.append(" and hasFreight=? ");
                    params.add(1);
                } else if ("video".equals(filter)) {
                    query.append(" and videoUrl is not null and videoUrl!=''");
                }
            }
        }
        if (lowPrice != null) {
            query.append(" and discountPrice>=? ");
            params.add(lowPrice);
        }
        if (highPrice != null) {
            query.append(" and discountPrice<=? ");
            params.add(highPrice);
        }
        if (ratio != null) {
            query.append(" and ratio>=? ");
            params.add(ratio);
        }
        if (sales != null) {
            query.append(" and finalSales<=? ");
            params.add(sales);
        }
        if (searchText != null && searchText.trim().length() != 0) {
            query.append(" and title like ?");
            params.add("%" + searchText + "%");
        }

        map.put(query.toString(), params);
        return map;
    }

    private static List<DianQuanItem> getDianquanList(String query, List params, int offset, int pageSize, int order) {
        query += " order by ";
        switch (order) {
            case 2:
                query += "finalSales desc ";
                break;
            case 3:
                query += "ratio desc ";
                break;
            case 4:
                query += "discountPrice ";
                break;
            case 1:
            default:
                query += "timeline desc ";
                break;
        }
        query = query + " limit " + offset + "," + pageSize;

        List<DianQuanItem> itemList = DianQuanItem.findDianQuanItemList(query, params.toArray());
        return itemList;
    }

    private static String getJsonFromDianquanItemList(List<DianQuanItem> itemList, long count, int curr, int pageSize) {
        JsonObject result = new JsonObject();
        JsonArray array = new JsonArray();
        for (DianQuanItem item : itemList) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("gid", item.gid);
            jsonObject.addProperty("thumb", item.thumb);
            jsonObject.addProperty("categoryId", item.categoryId);
            jsonObject.addProperty("categoryName", item.categoryName);
            jsonObject.addProperty("site", item.site);
            jsonObject.addProperty("title", item.title);
            jsonObject.addProperty("subTitle", item.subTitle);
            jsonObject.addProperty("discountPrice", item.discountPrice);
            jsonObject.addProperty("normalPrice", item.normalPrice);
            jsonObject.addProperty("ratioType", item.ratioType);
            jsonObject.addProperty("ratio", item.ratio);
            jsonObject.addProperty("url", item.url);
            jsonObject.addProperty("longPic", item.longPic);
            jsonObject.addProperty("videoUrl", item.videoUrl);
            jsonObject.addProperty("recommendedReason", item.recommendedReason);
            jsonObject.addProperty("activity", item.activity);
            jsonObject.addProperty("finalSales", item.finalSales);
            jsonObject.addProperty("couponMoney", item.couponMoney);
            jsonObject.addProperty("couponUrl", item.couponUrl);
            jsonObject.addProperty("couponTotal", item.couponTotal);
            jsonObject.addProperty("freight", item.hasFreight);
            array.add(jsonObject);
        }
        result.add("data", array);

        JsonObject extra = new JsonObject();
        extra.addProperty("count", count);
        extra.addProperty("currPage", curr);
        extra.addProperty("pageSize", pageSize);
        extra.addProperty("page", count % pageSize == 0 ? count / pageSize : count / pageSize + 1);
        result.add("extra", extra);
        return GSON.toJson(result);
    }

    private static String getGidListJson(List<DianQuanItem> itemList) {
        JsonArray array = new JsonArray();
        for (DianQuanItem dianQuanItem : itemList) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("gid", dianQuanItem.gid);
            array.add(jsonObject);
        }
        return GSON.toJson(array);
    }
}
