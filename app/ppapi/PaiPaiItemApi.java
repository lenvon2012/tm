package ppapi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import models.paipai.PaiPaiUser;
import models.ppmanage.PPStock;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ppapi.models.PaiPaiItem;
import ppapi.models.PaiPaiItem.PaiPaiItemAttr;
import ppapi.models.PaiPaiItemCatPlay;
import ppapi.models.PaiPaiTradeDisplay;
import ppapi.models.PaiPaiTradeItem;

import com.ciaosir.client.utils.JsonUtil;

/**
 * @author haoyongzh
 * 
 */
public class PaiPaiItemApi {

    public final static Logger log = LoggerFactory.getLogger(PaiPaiItemApi.class);

    public static final int ITEM_PAGE_SIZE = 20;

    public static final int TRADE_PAGE_SIZE = 20;

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static class PaiPaiItemListApi extends PaiPaiApi<List<PaiPaiItem>> {

        public PaiPaiUser user;

        public List<PaiPaiItem> resList;

        public int pn = 1;

        /**
         * 格式：2008-12-03 01:02:03
         */
        public String modifyTimeBegin;

        public String modifyTimeEnd;

        /**
         * 取值参见pop.paipai.com里的 1.出售中， 2.仓库中，组合状态包括：我下架的+定期下架的+定时上架+从未上架的 
         * 3.我下架的 4.定期下架的 5.等待上架 6.定时上架 7.从未上架 8.售完的
         * 9.等待处理 10.删除的商品
         */
        public int itemState = 0;

        public PaiPaiItemListApi(PaiPaiUser user) {
            super(user);
            this.user = user;
            this.resList = new ArrayList<PaiPaiItem>();
        }

        public PaiPaiItemListApi(PaiPaiUser user, String modifyTimeBegin, String modifyTimeEnd) {
            super(user);
            this.user = user;
            this.modifyTimeBegin = modifyTimeBegin;
            this.modifyTimeEnd = modifyTimeEnd;
            this.resList = new ArrayList<PaiPaiItem>();
        }

        public PaiPaiItemListApi(PaiPaiUser user, long modifyTimeBegin, long modifyTimeEnd) {
            super(user);
            this.user = user;
            if (modifyTimeBegin > 0) {
                this.modifyTimeBegin = sdf.format(new Date(modifyTimeBegin));
            }
            if (modifyTimeEnd > 0) {
                this.modifyTimeEnd = sdf.format(new Date(modifyTimeEnd));
            }
            this.resList = new ArrayList<PaiPaiItem>();
        }

        public PaiPaiItemListApi(PaiPaiUser user, long modifyTimeBegin, long modifyTimeEnd, int itemState) {
            this(user, modifyTimeBegin, modifyTimeEnd);
            this.itemState = itemState;
        }

        public PaiPaiItemListApi(PaiPaiUser user, long modifyTimeBegin, long modifyTimeEnd, boolean onlyForSaleItem) {
            this(user, modifyTimeBegin, modifyTimeEnd);
            if (onlyForSaleItem == true) {
                this.itemState = 1;
            }
        }

        @Override
        public String getApiPath() {
            return "/item/sellerSearchItemList.xhtml";
        }

        @Override
        public boolean prepareRequest(HashMap<String, Object> params) {
            params.put("pageIndex", String.valueOf(pn));
            params.put("pageSize", String.valueOf(ITEM_PAGE_SIZE));
            if (!StringUtils.isEmpty(modifyTimeBegin)) {
                params.put("modifyTimeBegin", modifyTimeBegin);
            }
            if (!StringUtils.isEmpty(modifyTimeEnd)) {
                params.put("modifyTimeEnd", modifyTimeEnd);
            }
            if (itemState <= 0) {
                params.put("itemState", String.valueOf(itemState));
            }
            return false;
        }

        @Override
        public List<PaiPaiItem> validResponse(String resp) {
            if (StringUtils.isEmpty(resp)) {
                return null;
            }

            List<PaiPaiItem> list = new ArrayList<PaiPaiItem>();

            try {
                // System.out.println(resp);
                resp = resp.replace("\\", "\\\\").trim();
                JSONObject respObj = new JSONObject(resp);
                if (!respObj.has("errorCode")) {
                    return null;
                }
                int errorCode = respObj.getInt("errorCode");
                if (errorCode != 0) {
                    log.error("resp error: " + resp);
                    return null;
                }
                int countTotal = respObj.getInt("countTotal");
                Long sellerUin = respObj.getLong("sellerUin");
                // JsonNode node = JsonUtil.readJsonResult(resp);
                // if (node == null || node.isMissingNode()) {
                // return null;
                // }
                // int errorCode = node.findValue("errorCode").getIntValue();
                // if (errorCode != 0) {
                // log.error("resp error: " + resp);
                // return null;
                // }
                //
                // int countTotal = node.findValue("countTotal").getIntValue();

                if (countTotal > 0 && pn < (countTotal + ITEM_PAGE_SIZE - 1) / ITEM_PAGE_SIZE) {
                    this.iteratorTime = 1;
                    this.pn++;
                }

                // Long sellerUin = node.findValue("sellerUin").getLongValue();
                // if (sellerUin == null || sellerUin.longValue() <= 0) {
                // return null;
                // }
                //
                // JsonNode itemList = node.findValue("itemList");
                // if (itemList == null || itemList.isArray() == false) {
                // log.info("--itemList empty--" + resp);
                // }

                JSONArray json = respObj.getJSONArray("itemList");
                for (int i = 0; i < json.length(); i++) {
                    JSONObject obj = json.getJSONObject(i);
                    String itemCode = obj.getString("itemCode");
                    String itemName = obj.getString("itemName");
                    String itemState = obj.getString("itemState");
                    String createTimeStr = obj.getString("createTime");
                    String picLink = obj.getString("picLink");
                    Long categoryId = obj.getLong("classId");
                    int itemPrice = obj.getInt("itemPrice");
                    int visitCount = obj.getInt("visitCount");
                    long createTime = sdf.parse(createTimeStr).getTime();
                    PaiPaiItem item = new PaiPaiItem(sellerUin, itemCode, itemName, itemState, picLink, createTime,
                            categoryId, itemPrice, visitCount);
                    list.add(item);
                }
            } catch (JSONException e) {
                log.error(e.getMessage(), e);
            } catch (ParseException e) {
                log.error(e.getMessage(), e);
            }

            // List<String> itemCodes = itemList.findValuesAsText("itemCode");
            // List<String> itemNames = itemList.findValuesAsText("itemName");
            // List<String> itemStates = itemList.findValuesAsText("itemState");
            // List<String> categoryIdStr = itemList.findValuesAsText("categoryId");
            // List<String> itemPriceStr = itemList.findValuesAsText("itemPrice");
            // List<String> createTimes = itemList.findValuesAsText("createTime");
            // List<String> picLinks = itemList.findValuesAsText("picLink");
            // List<String> visitCountStr = itemList.findValuesAsText("visitCount");
            //
            // for (int i = 0; i < itemCodes.size(); i++) {
            // PaiPaiItem item = new PaiPaiItem(sellerUin, itemCodes.get(i), itemNames.get(i), itemStates.get(i),
            // picLinks.get(i), createTimes.get(i), Integer.valueOf(categoryIdStr.get(i)),
            // Integer.valueOf(itemPriceStr.get(i)), Integer.valueOf(visitCountStr.get(i)));
            // list.add(item);
            // }

            return list;
        }

        @Override
        public List<PaiPaiItem> applyResult(List<PaiPaiItem> res) {
            if (res == null) {
                return resList;
            }
            resList.addAll(res);
            return resList;
        }

    }

    public static class PaiPaiItemDetailApi extends PaiPaiApi<PaiPaiItem> {

        public PaiPaiUser user;

        public String itemCode;

        public int needParseAttr = 1;

        public int needDetailInfo = 0;

        public int needExtendInfo = 0;

        public PaiPaiItemDetailApi(PaiPaiUser user, String itemCode) {
            super(user);
            this.user = user;
            this.itemCode = itemCode;
        }

        public PaiPaiItemDetailApi(PaiPaiUser user, String itemCode, int needParseAttr) {
            super(user);
            this.user = user;
            this.itemCode = itemCode;
            this.needParseAttr = needParseAttr;
        }

        public PaiPaiItemDetailApi(PaiPaiUser user, String itemCode, int needParseAttr, int needDetailInfo,
                int needExtendInfo) {
            super(user);
            this.user = user;
            this.itemCode = itemCode;
            this.needParseAttr = needParseAttr;
            this.needDetailInfo = needDetailInfo;
            this.needExtendInfo = needExtendInfo;
        }

        @Override
        public String getApiPath() {
            return "/item/getItem.xhtml";
        }

        @Override
        public boolean prepareRequest(HashMap<String, Object> params) {
            params.put("itemCode", String.valueOf(itemCode));
            params.put("needParseAttr", String.valueOf(needParseAttr));
            params.put("needDetailInfo", String.valueOf(needDetailInfo));
            params.put("needExtendInfo", String.valueOf(needExtendInfo));
            return false;
        }

        @Override
        public PaiPaiItem validResponse(String resp) {
            if (StringUtils.isEmpty(resp)) {
                return null;
            }

            JsonNode node = JsonUtil.readJsonResult(resp);
            if (node == null || node.isMissingNode()) {
                return null;
            }
            int errorCode = node.findValue("errorCode").getIntValue();
            if (errorCode != 0) {
                log.error("resp error: " + resp);
                return null;
            }
            try {
                String itemName = node.findValue("itemName").getTextValue();
                String itemState = node.findValue("itemState").getTextValue();
                String picLink = node.findValue("picLink").getTextValue();
                String createTimeStr = node.findValue("createTime").getTextValue();
                Long createTime = sdf.parse(createTimeStr).getTime();
                Long categoryId = node.findValue("categoryId").getLongValue();
                int itemPrice = node.findValue("itemPrice").getIntValue();
                int visitCount = node.findValue("visitCount").getIntValue();

                PaiPaiItem item = new PaiPaiItem(user.getId(), itemCode, itemName, itemState, picLink, createTime,
                        categoryId, itemPrice, visitCount);

                JsonNode parsedAttrList = node.findValue("parsedAttrList");
                if (parsedAttrList == null || parsedAttrList.isArray() == false) {
                    log.info("--PaiPaiItemDetailApi attrList empty--" + resp);
                }
                JSONArray json = new JSONArray(parsedAttrList.toString());
                List<PaiPaiItemAttr> attrList = new ArrayList<PaiPaiItem.PaiPaiItemAttr>();
                for (int i = 0; i < json.length(); i++) {
                    JSONObject obj = json.getJSONObject(i);
                    int attrId = obj.getInt("attrId");
                    String attrName = obj.getString("attrName");

                    List<Long> attrOptionId = new ArrayList<Long>();
                    JSONArray optionIdArr = obj.getJSONArray("attrOptionId");
                    for (int j = 0; j < optionIdArr.length(); j++) {
                        attrOptionId.add(optionIdArr.getLong(j));
                    }

                    List<String> attrOptionName = new ArrayList<String>();
                    JSONArray optionNameArr = obj.getJSONArray("attrOptionName");
                    for (int j = 0; j < optionNameArr.length(); j++) {
                        attrOptionName.add(optionNameArr.getString(j).trim());
                    }

                    PaiPaiItemAttr attr = new PaiPaiItem.PaiPaiItemAttr(attrId, attrName, attrOptionId, attrOptionName);
                    attrList.add(attr);
                }
                item.setAttr(attrList);
                return item;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return null;
        }

        @Override
        public PaiPaiItem applyResult(PaiPaiItem res) {
            return res;
        }

    }

    public static class PaiPaiItemUpdateTitleApi extends PaiPaiApi<Boolean> {

        public PaiPaiUser user;

        public String itemCode;

        public String itemName;

        public PaiPaiItemUpdateTitleApi(PaiPaiUser user, String itemCode, String itemName) {
            super(user);
            this.user = user;
            this.itemCode = itemCode;
            this.itemName = itemName;
        }

        @Override
        public String getApiPath() {
            return "/item/modifyItem.xhtml";
        }

        @Override
        public boolean prepareRequest(HashMap<String, Object> params) {
            params.put("itemCode", String.valueOf(itemCode));
            params.put("itemName", String.valueOf(itemName));
            return false;
        }

        @Override
        public Boolean validResponse(String resp) {
            if (StringUtils.isEmpty(resp)) {
                return Boolean.FALSE;
            }

            JsonNode node = JsonUtil.readJsonResult(resp);
            if (node == null || node.isMissingNode()) {
                return Boolean.FALSE;
            }
            int errorCode = node.findValue("errorCode").getIntValue();
            if (errorCode != 0) {
                log.error("resp error: " + resp);
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        @Override
        public Boolean applyResult(Boolean res) {
            return res;
        }
    }

    public static class PaiPaiTradeListApi extends PaiPaiApi<List<PaiPaiTradeDisplay>> {

        public PaiPaiUser user;

        public String timeType;

        public String timeBegin;

        public String timeEnd;

        public List<PaiPaiTradeDisplay> resList;

        public int pn = 1;

        public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        public PaiPaiTradeListApi(PaiPaiUser user) {
            super(user);
            this.user = user;
            this.resList = new ArrayList<PaiPaiTradeDisplay>();
        }

        public PaiPaiTradeListApi(PaiPaiUser user, long timeBegin, long timeEnd) {
            super(user);
            this.user = user;
            this.timeType = "CREATE";
            this.timeBegin = sdf.format(new Date(timeBegin));
            this.timeEnd = sdf.format(new Date(timeEnd));
            this.resList = new ArrayList<PaiPaiTradeDisplay>();
        }

        public PaiPaiTradeListApi(PaiPaiUser user, String timeBegin, String timeEnd) {
            super(user);
            this.user = user;
            this.timeType = "CREATE";
            this.timeBegin = timeBegin;
            this.timeEnd = timeEnd;
            this.resList = new ArrayList<PaiPaiTradeDisplay>();
        }

        public PaiPaiTradeListApi(PaiPaiUser user, String timeType, String timeBegin, String timeEnd) {
            super(user);
            this.user = user;
            this.timeType = timeType;
            this.timeBegin = timeBegin;
            this.timeEnd = timeEnd;
            this.resList = new ArrayList<PaiPaiTradeDisplay>();
        }

        @Override
        public String getApiPath() {
            return "/deal/sellerSearchDealList.xhtml";
        }

        @Override
        public boolean prepareRequest(HashMap<String, Object> params) {
            params.put("pageIndex", String.valueOf(pn));
            params.put("pageSize", String.valueOf(ITEM_PAGE_SIZE));
            if (!StringUtils.isEmpty(timeType)) {
                params.put("timeType", timeType);
                params.put("timeBegin", timeBegin);
                params.put("timeEnd", timeEnd);
            }
            params.put("listItem", "1");
            return false;
        }

        @Override
        public List<PaiPaiTradeDisplay> validResponse(String resp) {
            if (StringUtils.isEmpty(resp)) {
                return null;
            }
            JsonNode node = JsonUtil.readJsonResult(resp);
            if (node == null || node.isMissingNode()) {
                return null;
            }
            int errorCode = node.findValue("errorCode").getIntValue();
            if (errorCode != 0) {
                log.error("resp error: " + resp);
                return null;
            }

            int countTotal = node.findValue("countTotal").getIntValue();
            if (countTotal > 0 && pn < (countTotal + ITEM_PAGE_SIZE - 1) / ITEM_PAGE_SIZE) {
                this.iteratorTime = 1;
                this.pn++;
            }

            // Long sellerUin = node.findValue("sellerUin").getLongValue();
            Long sellerUin = user.getId();
            if (sellerUin == null || sellerUin.longValue() <= 0) {
                return null;
            }

            JsonNode dealList = node.findValue("dealList");
            if (dealList == null || dealList.isArray() == false) {
                return null;
            }

            List<PaiPaiTradeDisplay> list = new ArrayList<PaiPaiTradeDisplay>();
            try {
                JSONArray json = new JSONArray(dealList.toString());
                for (int i = 0; i < json.length(); i++) {
                    JSONObject obj = json.getJSONObject(i);
                    String dealCode = obj.getString("dealCode");
                    String dealState = obj.getString("dealState");
                    String dealRateState = obj.getString("dealRateState");
                    String buyerName = obj.getString("buyerName");
                    long buyerUin = obj.getLong("buyerUin");
                    String receiverAddress = obj.getString("receiverAddress");
                    String receiverMobile = obj.getString("receiverMobile");
                    String receiverName = obj.getString("receiverName");
                    String receiverPhone = obj.getString("receiverPhone");
                    String createTimeStr = obj.getString("createTime");
                    String payTimeStr = obj.getString("payTime");
                    int totalCash = obj.getInt("totalCash");
                    long createTime = sdf.parse(createTimeStr).getTime();
                    long payTime = sdf.parse(payTimeStr).getTime();
                    PaiPaiTradeDisplay trade = new PaiPaiTradeDisplay(sellerUin, buyerUin, dealCode, buyerName,
                            receiverAddress, receiverName, receiverMobile, receiverPhone, dealState, dealRateState,
                            createTime, payTime, totalCash);

                    List<PaiPaiTradeItem> tradeItemList = new ArrayList<PaiPaiTradeItem>();
                    JSONArray itemList = obj.getJSONArray("itemList");
                    for (int j = 0; j < itemList.length(); j++) {
                        JSONObject item = itemList.getJSONObject(j);
                        String itemCode = item.getString("itemCode");
                        String itemName = item.getString("itemName");
                        String picLink = item.getString("itemPic80");
                        int itemDealPrice = item.getInt("itemDealPrice");
                        int itemDealCount = item.getInt("itemDealCount");
                        PaiPaiTradeItem tradeItem = new PaiPaiTradeItem(user.getId(), dealCode, itemCode, itemName,
                                picLink, createTime, itemDealPrice, itemDealCount);
                        tradeItemList.add(tradeItem);
                    }
                    trade.setItemList(tradeItemList);
                    list.add(trade);
                }
            } catch (JSONException e) {
                log.error(e.getMessage(), e);
            } catch (ParseException e) {
                log.error(e.getMessage(), e);
            }

            return list;
        }

        @Override
        public List<PaiPaiTradeDisplay> applyResult(List<PaiPaiTradeDisplay> res) {
            if (res != null) {
                resList.addAll(res);
            }
            return resList;
        }

    }

    public static class PaiPaiItemCatListApi extends PaiPaiApi<List<PaiPaiItemCatPlay>> {

        public long catId;

        public PaiPaiItemCatListApi(PaiPaiUser user, long catId) {
            super(user);
            this.catId = catId;
        }

        @Override
        public String getApiPath() {
            return "/attr/getNavByNavMapId.xhtml";
        }

        @Override
        public boolean prepareRequest(HashMap<String, Object> params) {
            params.put("navId", String.valueOf(catId));
            params.put("mapId", "0");
            return false;
        }

        @Override
        public List<PaiPaiItemCatPlay> validResponse(String resp) {
            if (StringUtils.isEmpty(resp)) {
                return null;
            }
            JsonNode node = JsonUtil.readJsonResult(resp);
            if (node == null || node.isMissingNode()) {
                return null;
            }
            int errorCode = node.findValue("errorCode").getIntValue();
            if (errorCode != 0) {
                log.error("resp error: " + resp);
                return null;
            }

            JsonNode fullPath = node.findValue("FullPath");

            List<PaiPaiItemCatPlay> list = new ArrayList<PaiPaiItemCatPlay>();
            try {
                JSONArray json = new JSONArray(fullPath.toString());
                for (int i = 0; i < json.length(); i++) {
                    JSONObject obj = json.getJSONObject(i);
                    long catId = obj.getInt("NavId");
                    long parentCid = obj.getInt("PNavId");
                    String name = obj.getString("Name");
                    PaiPaiItemCatPlay itemCat = new PaiPaiItemCatPlay(catId, parentCid, name);
                    list.add(itemCat);
                }

            } catch (JSONException e) {
                log.error(e.getMessage(), e);
            }

            return list;
        }

        @Override
        public List<PaiPaiItemCatPlay> applyResult(List<PaiPaiItemCatPlay> res) {
            return res;
        }

    }

    public static class PPGetItemStock extends PaiPaiApi<List<PPStock>> {

        public String itemCode;

        public PPGetItemStock(PaiPaiUser user, String itemCode) {
            super(user);
            this.itemCode = itemCode;
        }

        @Override
        public String getApiPath() {
            return ("/item/getItem.xhtml");
        }

        @Override
        public boolean prepareRequest(HashMap<String, Object> params) {
            if (!StringUtils.isEmpty(itemCode)) {
                params.put("itemCode", itemCode);
            }
            return false;
        }

        @Override
        public List<PPStock> validResponse(String resp) {
            if (StringUtils.isEmpty(resp)) {
                return null;
            }
            JsonNode node = JsonUtil.readJsonResult(resp);
            if (node == null || node.isMissingNode()) {
                return null;
            }
            int errorCode = node.findValue("errorCode").getIntValue();
            if (errorCode != 0) {
                log.error("resp error: " + resp);
                String errorMessage = node.findValue("errorMessage").getTextValue();
                return null;
            }
            String itemCode = node.findValue("itemCode").getTextValue();
            String picLink = node.findValue("picLink").getTextValue();

            JsonNode stockJsonList = node.findValue("stockJsonList");
            List<PPStock> stockList = new ArrayList<PPStock>();
            try {
                JSONArray stockJsonArray = new JSONArray(stockJsonList.toString());
                for (int i = 0; i < stockJsonArray.length(); i++) {
                    JSONObject obj = stockJsonArray.getJSONObject(i);
                    Long sellerUin = obj.getLong("sellerUin");
                    Long skuId = obj.getLong("skuId");
                    Long price = obj.getLong("price");
                    Long num = obj.getLong("num");
                    Long soldNum = obj.getLong("soldNum");
                    int status = obj.getInt("status");
                    String saleAttr = obj.getString("saleAttr");
                    String stockAttr = obj.getString("attr");

                    PPStock stock = new PPStock(sellerUin, itemCode, skuId, price, picLink, num, status, soldNum,
                            saleAttr, stockAttr);

                    stockList.add(stock);
                }

                return stockList;

            } catch (JSONException e) {
                log.error(e.getMessage(), e);
            }

            return null;
        }

        @Override
        public List<PPStock> applyResult(List<PPStock> res) {
            // TODO Auto-generated method stub
            return res;
        }

    }

    public static class PPgetDealDetailApi extends PaiPaiApi<List<PaiPaiTradeItem>> {

        public PaiPaiUser user;

        public String dealCode;

        public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        public PPgetDealDetailApi(PaiPaiUser user, String dealCode) {
            super(user);
            this.user = user;
            this.dealCode = dealCode;
        }

        @Override
        public String getApiPath() {
            return ("/deal/getDealDetail.xhtml");
        }

        @Override
        public boolean prepareRequest(HashMap<String, Object> params) {
            if (!StringUtils.isEmpty(dealCode)) {
                params.put("dealCode", dealCode);
            }
            params.put("listItem", "1");
            return false;
        }

        @Override
        public List<PaiPaiTradeItem> validResponse(String resp) {
            if (StringUtils.isEmpty(resp)) {
                return null;
            }
            JsonNode node = JsonUtil.readJsonResult(resp);
            if (node == null || node.isMissingNode()) {
                return null;
            }
            int errorCode = node.findValue("errorCode").getIntValue();
            if (errorCode != 0) {
                log.error("resp error: " + resp);
                return null;
            }
            JsonNode itemList = node.findValue("itemList");
            if (itemList == null || itemList.isArray() == false) {
                return null;
            }
            String createTimeStr = node.findValue("createTime").getTextValue();
            long createTime = 0;
            try {
                createTime = sdf.parse(createTimeStr).getTime();
            } catch (ParseException e1) {
                log.error(e1.getMessage(), e1);
            }

            List<PaiPaiTradeItem> list = new ArrayList<PaiPaiTradeItem>();
            try {
                JSONArray json = new JSONArray(itemList.toString());
                for (int j = 0; j < json.length(); j++) {
                    JSONObject item = json.getJSONObject(j);
                    String itemCode = item.getString("itemCode");
                    String itemName = item.getString("itemName");
                    String picLink = item.getString("itemPic80");
                    int itemDealPrice = item.getInt("itemDealPrice");
                    int itemDealCount = item.getInt("itemDealCount");
                    PaiPaiTradeItem tradeItem = new PaiPaiTradeItem(user.getId(), dealCode, itemCode, itemName,
                            picLink, createTime, itemDealPrice, itemDealCount);
                    list.add(tradeItem);
                }
            } catch (JSONException e) {
                log.error(e.getMessage(), e);
            }
            return list;
        }

        @Override
        public List<PaiPaiTradeItem> applyResult(List<PaiPaiTradeItem> res) {
            // TODO Auto-generated method stub
            return res;
        }

    }
}
