package controllers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jdp.ApiJdpAdapter;
import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actions.sku.SkuPriceEditAction;
import actions.sku.SkuPriceEditAction.SkuPriceBean;
import actions.sku.SkuPriceEditAction.SkuPriceEditResult;
import autotitle.ItemPropAction;
import autotitle.ItemPropAction.PropUnit;
import bustbapi.ItemApi;
import bustbapi.ItemApi.MultiItemsListGet;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.JsonUtil;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.Sku;

import controllers.SkinBatch.BatchOpMessage;
import dao.item.ItemDao;

public class SkuPriceEdit extends TMController {

    private static final Logger log = LoggerFactory.getLogger(SkuPriceEdit.class);
    
    public static void getItemSkuPrices(Long numIid) {
        if (numIid == null || numIid <= 0L) {
            renderError("系统出现异常，宝贝id为空，请联系我们！");
        }
        User user = getUser();
        
        Item item = ApiJdpAdapter.get(user).findItem(user, numIid);
        
        boolean isFromApi = false;
        
        if (item == null) {
            item = loadOneItemWithSkuByApi(user, numIid);
            isFromApi = true;
        }
        
        if (item == null) {
            renderError("系统出现异常，找不到该宝贝，请联系我们！");
        }
        
        List<Sku> skuList = item.getSkus();
        if (CommonUtils.isEmpty(skuList)) {
            skuList = new ArrayList<Sku>();
        }
        //log.info(JsonUtil.getJson(skuList));
        List<SkuPriceShowResult> showResList = new ArrayList<SkuPriceShowResult>();
        
        List<PropUnit> propList = ItemPropAction.mergePropAlis(item);
        
        for (Sku sku : skuList) {
            if (isValidSku(sku) == false) {
                continue;
            }
            String properties = sku.getProperties();
            String price = sku.getPrice();
            String propertyNames = SkuPriceEditAction.getSkuPropertyNames(sku, propList);
            Long quantity = sku.getQuantity();
            
            SkuPriceShowResult showRes = new SkuPriceShowResult(properties, propertyNames, price, quantity, isFromApi);
            showResList.add(showRes);
        }
        
        renderSuccess("", showResList);
    }
    
    
    private static boolean isValidSku(Sku sku) {
        if (sku == null) {
            return false;
        }
        if ("delete".equals(sku.getStatus()) == true) {
            return false;
        }
        return true;
    }
    
    private static BigDecimal parseToNewPrice(String priceStr) {
        if (StringUtils.isEmpty(priceStr)) {
            renderError("改价失败，请先输入新的价格！");
        }
        
        BigDecimal price = new BigDecimal(0);
        try {
            price = new BigDecimal(priceStr);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            renderError("改价失败，" + priceStr + "不是正确的数字格式！");
        }
        
        return price;
    }
    
    
    private static BigDecimal scaleToNewPrice(String scaleStr, String priceStr) {
        if (StringUtils.isEmpty(scaleStr)) {
            renderError("改价失败，请先输入改价百分比！");
        }
        BigDecimal scale = new BigDecimal(0);
        try {
            scale = new BigDecimal(scaleStr);
            scale = scale.divide(new BigDecimal(100));
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            renderError("改价失败，改价比例：" + scaleStr + "不是正确的数字格式！");
        }
        
        if (StringUtils.isEmpty(priceStr)) {
            renderError("系统出现异常，原价为空，请联系我们！");
        }
        
        BigDecimal price = new BigDecimal(0);
        try {
            price = new BigDecimal(priceStr);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            renderError("改价失败，原价：" + priceStr + "不是正确的数字格式！");
        }
        
        BigDecimal newPrice = scale.multiply(price);
        
        return newPrice;
    }
    
    
    
    /**
     * 直接改价
     * @param numIid
     * @param newPriceStr
     */
    public static void directModifyItemPrice(Long numIid, String itemPriceStr, String skuPriceJson) {
        
        User user = getUser();
        if (numIid == null || numIid <= 0L) {
            renderError("亲，请先选择要修改价格的宝贝");
        }
        
        ItemPlay item = ItemDao.findByNumIid(user.getId(), numIid);
        if (item == null) {
            renderError("系统异常，找不到相应的宝贝！");
        }

        //检查w2权限
        SkinBatch.checkW2Expires(user, 60);
        
        BigDecimal itemPrice = parseToNewPrice(itemPriceStr);
        
        if (StringUtils.isEmpty(skuPriceJson)) {
            skuPriceJson = "[]";
        }
        SkuPriceBean[] skuBeanArray = JsonUtil.toObject(skuPriceJson, SkuPriceBean[].class);
        if (skuBeanArray == null) {
            log.error("fail to parse json: " + skuPriceJson + "------------");
            renderError("系统出现异常，sku价格解析出错，请联系我们！");
        }
        List<SkuPriceBean> skuBeanList = new ArrayList<SkuPriceBean>();
        for (SkuPriceBean skuBean : skuBeanArray) {
            String skuParameter = skuBean.getSkuParameter();
            BigDecimal skuPrice = parseToNewPrice(skuParameter);
            skuBean.setSkuPrice(skuPrice);
            skuBeanList.add(skuBean);
        }

        SkuPriceEditResult skuRes = SkuPriceEditAction.doEditSkuPrice(user, item, itemPrice, skuBeanList);
        
        if (skuRes.isSuccess() == true) {
            renderSuccess(skuRes.getMessage(), new ArrayList<BatchOpMessage>());
        } else {
            List<BatchOpMessage> batchOpMsgList = new ArrayList<BatchOpMessage>();
            BatchOpMessage batchOpMsg = new BatchOpMessage(item, skuRes.getMessage());
            batchOpMsgList.add(batchOpMsg);
            
            //renderSuccess("宝贝价格修改失败，点击确定后查看详情！", batchOpMsgList);
            renderSuccess(skuRes.getMessage(), batchOpMsgList);
        }
        
    }
    
    private static Map<String, Sku> parseToPropertySkuMap(Item item) {
        Map<String, Sku> skuMap = new HashMap<String, Sku>();
        
        if (item == null) {
            return skuMap;
        }
        List<Sku> skuList = item.getSkus();
        if (CommonUtils.isEmpty(skuList)) {
            return skuMap;
        }
        for (Sku sku : skuList) {
            skuMap.put(sku.getProperties(), sku);
        }
        
        return skuMap;
    }
    
    /**
     * 一个宝贝按比例改价
     * @param numIid
     * @param itemScaleStr
     * @param skuScaleJson
     */
    public static void scaleOneItemPrice(Long numIid, String itemScaleStr, String skuScaleJson) {
        
        User user = getUser();
        if (numIid == null || numIid <= 0L) {
            renderError("亲，请先选择要修改价格的宝贝");
        }

        ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
        if (itemPlay == null) {
            renderError("系统异常，找不到相应的宝贝！");
        }
        
        //检查w2权限
        SkinBatch.checkW2Expires(user, 60);
        
        if (StringUtils.isEmpty(skuScaleJson)) {
            skuScaleJson = "[]";
        }
        SkuPriceBean[] skuBeanArray = JsonUtil.toObject(skuScaleJson, SkuPriceBean[].class);
        if (skuBeanArray == null) {
            log.error("fail to parse json: " + skuScaleJson + "------------");
            renderError("系统出现异常，sku价格解析出错，请联系我们！");
        }

        Item item = loadOneItemWithSkuByApi(user, numIid);
        if (item == null) {
            renderError("系统异常，找不到相应的宝贝！");
        }
        BigDecimal itemPrice = scaleToNewPrice(itemScaleStr, item.getPrice());
        
        Map<String, Sku> skuMap = parseToPropertySkuMap(item);
        List<SkuPriceBean> skuBeanList = new ArrayList<SkuPriceBean>();
        for (SkuPriceBean skuBean : skuBeanArray) {
            String properties = skuBean.getProperties();
            String skuParameter = skuBean.getSkuParameter();
            Sku sku = skuMap.get(properties);
            if (sku == null) {
                renderError("系统异常，找不到相应的sku，请刷新页面重试，或联系我们！");
            }
            if (isValidSku(sku) == false) {
                renderError("系统异常，某个sku已被删除，请刷新页面重试，或联系我们！");
            }
            BigDecimal skuPrice = scaleToNewPrice(skuParameter, sku.getPrice());
            skuBean.setSkuPrice(skuPrice);
            //skuBean.setQuantity(sku.getQuantity());
            skuBean.setOriginSkuPrice(sku.getPrice());
            skuBeanList.add(skuBean);
        }
        

        SkuPriceEditResult skuRes = SkuPriceEditAction.doEditSkuPrice(user, itemPlay, itemPrice, skuBeanList);
        
        if (skuRes.isSuccess() == true) {
            renderSuccess(skuRes.getMessage(), new ArrayList<BatchOpMessage>());
        } else {
            List<BatchOpMessage> batchOpMsgList = new ArrayList<BatchOpMessage>();
            BatchOpMessage batchOpMsg = new BatchOpMessage(itemPlay, skuRes.getMessage());
            batchOpMsgList.add(batchOpMsg);
            
            //renderSuccess("宝贝价格修改失败，点击确定后查看详情！", batchOpMsgList);
            renderSuccess(skuRes.getMessage(), batchOpMsgList);
        }
        
    }
    
    private static Map<Long, Item> toItemMap(List<Item> itemList) {
        Map<Long, Item> itemMap = new HashMap<Long, Item>();
        if (CommonUtils.isEmpty(itemList)) {
            return itemMap;
        }
        
        for (Item item : itemList) {
            itemMap.put(item.getNumIid(), item);
        }
        return itemMap;
    }
    

    /**
     * 批量比例改价多个宝贝的价格
     * @param toCancelNumIid
     * @param newPriceStr
     */
    public static void scaleSomeItemsPrice(List<Long> numIidList, String priceScaleStr) {
        
        User user = getUser();
        if (numIidList == null || numIidList.isEmpty()) {
            renderError("亲，请先选择要修改价格的宝贝");
        }

        //检查w2权限
        SkinBatch.checkW2Expires(user, 60);

        if (StringUtils.isEmpty(priceScaleStr)) {
            renderError("改价失败，请先输入改价百分比！");
        }
        
        try {
            new BigDecimal(priceScaleStr);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            renderError("改价失败，改价比例：" + priceScaleStr + "不是正确的数字格式！");
        }

        
        int successNum = 0;
        List<BatchOpMessage> errorMsgList = new ArrayList<BatchOpMessage>();

        List<Item> itemList = loadItemsWithSkuByApi(user, numIidList);

        Map<Long, Item> itemMap = toItemMap(itemList);
        
        for (Long numIid : numIidList) {
            
            ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
            if (itemPlay == null) {
                BatchOpMessage batchOpMsg = new BatchOpMessage(itemPlay, "修改价格失败，找不到宝贝！");
                errorMsgList.add(batchOpMsg);
                continue;
            }
            Item item = itemMap.get(numIid);
            if (item == null) {
                BatchOpMessage batchOpMsg = new BatchOpMessage(itemPlay, "修改价格失败，找不到宝贝！");
                errorMsgList.add(batchOpMsg);
                continue;
            }
            
            BigDecimal itemPrice = scaleToNewPrice(priceScaleStr, item.getPrice());
            
            List<Sku> skuList = item.getSkus();
            if (CommonUtils.isEmpty(skuList)) {
                skuList = new ArrayList<Sku>();
            }
            List<SkuPriceBean> skuBeanList = new ArrayList<SkuPriceBean>();

            List<PropUnit> propList = ItemPropAction.mergePropAlis(item);
            for (Sku sku : skuList) {
                if (isValidSku(sku) == false) {
                    continue;
                }
                String properties = sku.getProperties();
                String propertyNames = SkuPriceEditAction.getSkuPropertyNames(sku, propList);
                BigDecimal skuPrice = scaleToNewPrice(priceScaleStr, sku.getPrice());
                
                SkuPriceBean skuBean = new SkuPriceBean(properties, propertyNames, priceScaleStr, 
                        skuPrice, sku.getQuantity(), sku.getPrice());
                
                skuBeanList.add(skuBean);
            }
            
            SkuPriceEditResult skuRes = SkuPriceEditAction.doEditSkuPrice(user, itemPlay, itemPrice, skuBeanList);
            
            if (skuRes.isSuccess() == true) {
                successNum++;
                continue;
            } else {
                BatchOpMessage batchOpMsg = new BatchOpMessage(itemPlay, skuRes.getMessage());
                errorMsgList.add(batchOpMsg);
                continue;
            }
        }


        String message = "成功修改" + successNum + "个宝贝的价格";
        if (errorMsgList.size() > 0) {
            message += "，失败" + errorMsgList.size() + "个，点击确定后查看详情";
        }
        
        renderSuccess(message, errorMsgList);
    }
    
    
    
    private static Item loadOneItemWithSkuByApi(User user, Long numIid) {
        if (numIid == null || numIid <= 0L) {
            return null;
        }
        List<Long> numIidList = new ArrayList<Long>();
        numIidList.add(numIid);
        
        List<Item> itemList = loadItemsWithSkuByApi(user, numIidList);
        
        if (CommonUtils.isEmpty(itemList)) {
            return null;
        }
        return itemList.get(0);
    }

    private static List<Item> loadItemsWithSkuByApi(User user, List<Long> numIidList) {
//        ItemApi.ItemsListGet getApi = new ItemApi.ItemsListGet(numIidList, ItemApi.FIELDS_WITH_SKU);
//        List<Item> itemList = getApi.call();
    	MultiItemsListGet getApi = new MultiItemsListGet(user.getSessionKey(), numIidList, ItemApi.FIELDS_WITH_SKU);
    	List<Item> itemList = getApi.call();
        if (getApi.isApiSuccess() == false) {
            renderError("亲，获取宝贝时出错，请联系我们。");
        }
        if (CommonUtils.isEmpty(itemList)) {
            renderError("亲，找不到相应的宝贝，请联系我们。");
        }
        return itemList;
    }
    
    
    public static class SkuPriceShowResult {
        
        private String properties;
        private String propertyNames;
        private String price;
        private Long quantity;
        private boolean isFromApi;
        
        public SkuPriceShowResult(String properties, String propertyNames,
                String price, Long quantity, boolean isFromApi) {
            super();
            this.properties = properties;
            this.propertyNames = propertyNames;
            this.price = price;
            this.quantity = quantity;
            this.isFromApi = isFromApi;
        }

        public String getProperties() {
            return properties;
        }

        public void setProperties(String properties) {
            this.properties = properties;
        }

        public String getPropertyNames() {
            return propertyNames;
        }

        public void setPropertyNames(String propertyNames) {
            this.propertyNames = propertyNames;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public boolean isFromApi() {
            return isFromApi;
        }

        public void setFromApi(boolean isFromApi) {
            this.isFromApi = isFromApi;
        }

        public Long getQuantity() {
            return quantity;
        }

        public void setQuantity(Long quantity) {
            this.quantity = quantity;
        }
        
        
        
    }
    
}
