package actions.sku;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.item.ItemPlay;
import models.pricelog.EditItemPriceLog;
import models.pricelog.EditItemPriceLog.EditItemPriceStatus;
import models.pricelog.EditItemPriceLog.SingleSkuPriceLog;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autotitle.ItemPropAction.PropUnit;
import bustbapi.ItemApi;
import bustbapi.ItemSkuApi.SkuPriceUpdateApi;

import com.ciaosir.client.CommonUtils;
import com.dbt.cred.utils.JsonUtil;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.Sku;

public class SkuPriceEditAction {
    
    private static final Logger log = LoggerFactory.getLogger(SkuPriceEditAction.class);
    
    public static String getSkuPropertyNames(Sku sku, List<PropUnit> propList) {
        if (sku == null) {
            return "";
        }
        if (CommonUtils.isEmpty(propList)) {
            propList = new ArrayList<PropUnit>();
        }
        Map<String, PropUnit> propMap = new HashMap<String, PropUnit>();
        for (PropUnit propUnit : propList) {
            propMap.put(propUnit.getPid() + ":" + propUnit.getVid(), propUnit);
        }
        
        String propertyNames = sku.getPropertiesName();//1627207:28341:颜色分类:黑色;2100251:28383:尺码:均码
        String[] nameArray = propertyNames.split(";");
        List<String> nameList = new ArrayList<String>();
        for (String name : nameArray) {
            if (StringUtils.isEmpty(name)) {
                continue;
            }
            String[] splitArray = name.split(":");
            if (splitArray.length != 4) {
                name = splitArray[splitArray.length - 1];
            } else {
                PropUnit propUnit = propMap.get(splitArray[0] + ":" + splitArray[1]);
                if (propUnit == null) {
                    name = splitArray[splitArray.length - 1];
                } else {
                    name = propUnit.getValue();
                }
            }
            nameList.add(name);
        }
        propertyNames = StringUtils.join(nameList, "/");
        
        return propertyNames;
    }
    
    
    //转成带两位小数点
    private static BigDecimal formatPrice(BigDecimal newPrice) {
        if (newPrice == null) {
            return null;
        }
        //转换成小数的两位
        newPrice = newPrice.multiply(new BigDecimal(100));
        int integerPrice = newPrice.intValue();
        newPrice = new BigDecimal(integerPrice).divide(new BigDecimal(100));
        
        return newPrice;
    }
    
    private static SkuPriceEditResult checkPrice(BigDecimal newPrice) {
        if (newPrice == null) {
            return new SkuPriceEditResult(false, "系统异常，找不到新的价格，请联系我们！");
        }
        
        if (newPrice.compareTo(new BigDecimal(0)) <= 0) {
            return new SkuPriceEditResult(false, "宝贝价格修改失败，新的价格必须大于0！");
        }
        if (newPrice.compareTo(new BigDecimal(100000000)) > 0) {
            return new SkuPriceEditResult(false, "宝贝价格修改失败，价格不得大于100000000元！");
        }
        return new SkuPriceEditResult(true);
    }
    
    private static SkuPriceEditResult checkSkuPriceList(final BigDecimal itemPrice, List<SkuPriceBean> skuBeanList) {
        if (CommonUtils.isEmpty(skuBeanList)) {
            return new SkuPriceEditResult(true);
        }
        
        BigDecimal minSkuPrice = null;
        BigDecimal maxSkuPrice = null;
        
        
        for (SkuPriceBean skuBean : skuBeanList) {
            BigDecimal skuPrice = skuBean.getSkuPrice();
            skuPrice = formatPrice(skuPrice);
            SkuPriceEditResult skuRes = checkPrice(skuPrice);
            if (skuRes.isSuccess() == false) {
                return new SkuPriceEditResult(false, skuRes.getMessage());
            }
            skuBean.setSkuPrice(skuPrice);
            
            
            if (skuBean.getQuantity() != null && skuBean.getQuantity() > 0L) {
                if (minSkuPrice == null || minSkuPrice.compareTo(skuPrice) > 0) {
                    minSkuPrice = skuPrice;
                }
                if (maxSkuPrice == null || maxSkuPrice.compareTo(skuPrice) < 0) {
                    maxSkuPrice = skuPrice;
                }
            }
            
        }
        
        if (minSkuPrice != null && minSkuPrice.compareTo(itemPrice) > 0) {
            return new SkuPriceEditResult(false, "宝贝价格修改失败，一口价不能低于所有有库存的sku价格！");
        }
        if (maxSkuPrice != null && maxSkuPrice.compareTo(itemPrice) < 0) {
            return new SkuPriceEditResult(false, "宝贝价格修改失败，一口价不能高于所有有库存的sku价格！");
        }
        
        return new SkuPriceEditResult(true);
    }
    
    
    public static SkuPriceEditResult doEditSkuPrice(User user, ItemPlay item, 
            BigDecimal itemPrice, List<SkuPriceBean> skuBeanList) {
        
        if (item == null) {
            return new SkuPriceEditResult(false, "宝贝价格修改失败，系统异常，找不到相应的宝贝！");
        }
        
        //检查宝贝价格，一口价
        itemPrice = formatPrice(itemPrice);
        SkuPriceEditResult skuRes = checkPrice(itemPrice);
        if (skuRes.isSuccess() == false) {
            return new SkuPriceEditResult(false, skuRes.getMessage());
        }
        
        //检查sku价格
        skuRes = checkSkuPriceList(itemPrice, skuBeanList);
        if (skuRes.isSuccess() == false) {
            return new SkuPriceEditResult(false, skuRes.getMessage());
        }
        
        double itemOriginPrice = item.getPrice();
        
        if (CommonUtils.isEmpty(skuBeanList)) {
            skuRes = editForItemPrice(user, item, itemPrice);
        } else {
            skuRes = editForItemSkuPrice(user, item, itemPrice, skuBeanList);
        }
        
        if (skuRes.isSuccess() == true || skuRes.getSuccessSkuNum() > 0) {
            
            saveEditLogPrice(user, item, itemOriginPrice, itemPrice, skuBeanList, skuRes);
        }
        
        return skuRes;
    }
    
    
    private static void saveEditLogPrice(User user, ItemPlay item, double itemOriginPrice,
            BigDecimal itemPrice, List<SkuPriceBean> skuBeanList, 
            SkuPriceEditResult skuRes) {
        
        List<SingleSkuPriceLog> singleSkuLogList = new ArrayList<SingleSkuPriceLog>();
        for (SkuPriceBean skuBean : skuBeanList) {
            if (skuBean == null) {
                continue;
            }
            SingleSkuPriceLog singleSkuLog = new SingleSkuPriceLog(skuBean.getProperties(),
                    skuBean.getOriginSkuPrice(), skuBean.getSkuPrice().toString());
            
            singleSkuLogList.add(singleSkuLog);
        }
        
        String skuPriceJson = JsonUtil.getJson(singleSkuLogList);
        
        //BigDecimal originPrice = formatPrice(new BigDecimal(item.getPrice()));
        //这是item中的price已经被修改了。。
        BigDecimal originPrice = formatPrice(new BigDecimal(itemOriginPrice));
        
        int status = EditItemPriceStatus.AllSuccess;
        if (skuRes.getFailSkuNum() > 0) {
            status = EditItemPriceStatus.SomeSkuFail;
        }
        
        EditItemPriceLog editLog = new EditItemPriceLog(user.getId(), item.getNumIid(), 
                originPrice.toString(), itemPrice.toString(), 
                skuPriceJson, status, skuRes.getSuccessSkuNum(), skuRes.getFailSkuNum());
        
        editLog.jdbcSave();
        
    }
    
    /*
    private static SkuPriceEditResult editForItemSkuPrice(User user, ItemPlay itemPlay, 
            BigDecimal itemPrice, List<SkuPriceBean> skuBeanList) {
        if (CommonUtils.isEmpty(skuBeanList)) {
            return new SkuPriceEditResult(false, "系统异常，找不到sku出价！");
        }
        
        ItemSkuPriceUpdater skuUpdateApi = new ItemSkuPriceUpdater(user.getSessionKey(), 
                itemPlay.getNumIid(), itemPrice.toString(), skuBeanList);
        
        Item tbItem = skuUpdateApi.call();
        
        if (skuUpdateApi.isApiSuccess() == false || tbItem == null) {
            String errorMsg = skuUpdateApi.getErrorMsg();
            if (StringUtils.isEmpty(errorMsg)) {
                errorMsg = "宝贝价格修改失败，请联系我们！";
            } else {
                errorMsg = "宝贝价格修改失败，" + errorMsg;
            }
            
            return new SkuPriceEditResult(false, errorMsg);
        } else {
            return new SkuPriceEditResult(true, "宝贝价格修改成功");
        }
        
        
    }
    */
    //修改sku价格
    private static SkuPriceEditResult editForItemSkuPrice(User user, ItemPlay itemPlay, 
            BigDecimal itemPrice, List<SkuPriceBean> skuBeanList) {
        if (CommonUtils.isEmpty(skuBeanList)) {
            return new SkuPriceEditResult(false, "系统异常，找不到sku出价！");
        }
        
        //由于sku修改的顺序问题，在过程中可能会使一口价在sku价格之外，就会报错
        //而且会出现一部分修改成功，一部分修改失败的情况
        
        List<String> errorMsgList = new ArrayList<String>();
        int index = 0;
        int successSkuNum = 0;
        for (SkuPriceBean skuBean : skuBeanList) {
            BigDecimal skuPrice = skuBean.getSkuPrice();
            String properties = skuBean.getProperties();
            BigDecimal tempItemPrice = null;
            if (index == skuBeanList.size() - 1) {
                tempItemPrice = itemPrice;
            } else {
                if (skuBean.getQuantity() != null && skuBean.getQuantity() > 0L) {
                    tempItemPrice = skuPrice;//在其他时候使用skuPrice作为宝贝价格，保证api不出错
                }
                //tempItemPrice = itemPrice;
            }
            index++;
            
            SkuPriceUpdateApi updateApi = null;
            if (tempItemPrice == null) {
                updateApi = new SkuPriceUpdateApi(user, itemPlay.getNumIid(), 
                        properties, skuPrice.toString());
            } else {
                updateApi = new SkuPriceUpdateApi(user, itemPlay.getNumIid(), 
                        properties, skuPrice.toString(), tempItemPrice.toString(), true);
            }
            
            Sku sku = updateApi.call();
            if (sku != null && updateApi.isApiSuccess()) {
                if (tempItemPrice != null) {
                    itemPlay.setPrice(tempItemPrice.doubleValue());
                    itemPlay.jdbcSave();
                }
                successSkuNum++;
            } else {
                errorMsgList.add(skuBean.getPropertyNames() + "(" + updateApi.getErrorMsg() + ")");
            }
        }
        SkuPriceEditResult skuRes = null;
        
        if (errorMsgList.size() > 0) {
            String message = StringUtils.join(errorMsgList, ";");
            skuRes = new SkuPriceEditResult(false, "有" + errorMsgList.size() + "个sku价格修改失败：" + message);
        } else {
            skuRes = new SkuPriceEditResult(true, "宝贝价格修改成功");
        }
        
        skuRes.setFailSkuNum(errorMsgList.size());
        skuRes.setSuccessSkuNum(successSkuNum);
        
        
        return skuRes;
    }
    
    //没有sku出价，就直接修改宝贝一口价
    private static SkuPriceEditResult editForItemPrice(User user, ItemPlay itemPlay, BigDecimal newPrice) {
        ItemApi.ItemPriceUpdater updateApi = new ItemApi.ItemPriceUpdater(user.getSessionKey(), 
                itemPlay.getNumIid(), newPrice.toString());
        Item tbItem = updateApi.call();
        boolean isSuccess = updateApi.isApiSuccess();
        if (tbItem != null && isSuccess == true) {
            //更新数据
            itemPlay.setPrice(newPrice.doubleValue());
            itemPlay.jdbcSave();
            
            return new SkuPriceEditResult(true, "宝贝价格修改成功，新的价格：" + newPrice.toString() + "元");
        } else {
            
            String errorMsg = updateApi.getErrorMsg();
            if (StringUtils.isEmpty(errorMsg)) {
                errorMsg = "宝贝价格修改失败，请联系我们！";
            } else {
                errorMsg = "宝贝价格修改失败，" + errorMsg;
            }
            
            return new SkuPriceEditResult(false, errorMsg);
        }
        
    }
    
    
    
    public static class SkuPriceEditResult {
        
        private boolean isSuccess;
        
        private String message;
        
        private int successSkuNum;
        
        private int failSkuNum;

        public SkuPriceEditResult(boolean isSuccess, String message) {
            super();
            this.isSuccess = isSuccess;
            this.message = message;
        }
        
        public SkuPriceEditResult(boolean isSuccess) {
            super();
            this.isSuccess = isSuccess;
        }


        public boolean isSuccess() {
            return isSuccess;
        }

        public void setSuccess(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getSuccessSkuNum() {
            return successSkuNum;
        }

        public void setSuccessSkuNum(int successSkuNum) {
            this.successSkuNum = successSkuNum;
        }

        public int getFailSkuNum() {
            return failSkuNum;
        }

        public void setFailSkuNum(int failSkuNum) {
            this.failSkuNum = failSkuNum;
        }
        
        
    }
    
    
    public static class SkuPriceBean {
        
        private String properties;
        
        private String propertyNames;
        
        private String skuParameter;
        
        private BigDecimal skuPrice;
        
        private String originSkuPrice;
        
        private Long quantity;
        
        public SkuPriceBean() {
            super();
        }

        public SkuPriceBean(String properties, String propertyNames, String skuParameter,
                BigDecimal skuPrice, Long quantity, String originSkuPrice) {
            super();
            this.properties = properties;
            this.propertyNames = propertyNames;
            this.skuParameter = skuParameter;
            this.skuPrice = skuPrice;
            this.quantity = quantity;
            this.originSkuPrice = originSkuPrice;
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

        public String getSkuParameter() {
            return skuParameter;
        }

        public void setSkuParameter(String skuParameter) {
            this.skuParameter = skuParameter;
        }

        public BigDecimal getSkuPrice() {
            return skuPrice;
        }

        public void setSkuPrice(BigDecimal skuPrice) {
            this.skuPrice = skuPrice;
        }

        public Long getQuantity() {
            return quantity;
        }

        public void setQuantity(Long quantity) {
            this.quantity = quantity;
        }

        public String getOriginSkuPrice() {
            return originSkuPrice;
        }

        public void setOriginSkuPrice(String originSkuPrice) {
            this.originSkuPrice = originSkuPrice;
        }

        
    }
    
    
    
}
