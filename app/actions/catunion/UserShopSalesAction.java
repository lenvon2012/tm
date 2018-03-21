package actions.catunion;

import java.util.ArrayList;
import java.util.List;

import models.tmsearch.UserShopPlay;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.DateUtil;
import bustbapi.SellerAPI;
import bustbapi.ShopSearchAPI;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.API.PYSpiderOption;
import com.ciaosir.client.item.ShopInfo;
import com.ciaosir.client.pojo.ItemThumb;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.client.utils.SimpleHttpRetryUtil;
import com.dbt.cred.utils.JsonUtil;

public class UserShopSalesAction {
    private static final String NullNick = "淘宝账号不能为空";
    private static final String UserNotExist = "该淘宝账号不存在";
    private static final Logger log = LoggerFactory.getLogger(UserShopSalesAction.class);

    private static final String SysError = "系统出现一些异常，请稍后重试";
    private static final String NoShopError = "没有该账号的记录";
    
    
    private static final long OutDateLength = DateUtil.DAY_MILLIS * 3;
    
    public static ShopSalesResult doQueryUserShopSales(String nick) {
        if (StringUtils.isEmpty(nick))
            return new ShopSalesResult(false, NullNick);
        
        try {
            UserShopPlay userShop = UserShopPlay.findByNick(nick);
            long curTime = System.currentTimeMillis();
            
            if (userShop == null) {
                userShop = new UserShopPlay();
                userShop.setNick(nick);
                userShop.setVisitedTs(curTime);
                return createUserShopSales(userShop);
            } else {
                userShop.setVisitedTs(curTime);
                if (curTime - userShop.getUpdateTs() >= OutDateLength) {
                    return updateUserShopSales(userShop);
                } else {
                    //return updateUserShopSales(userShop);
                    userShop.jdbcSave();
                    return new ShopSalesResult(userShop);
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ShopSalesResult(false, SysError);
        }
        
    }

    private static ShopSalesResult createUserShopSales(UserShopPlay userShop) {
        return saveOrUpdateUserShopSales(userShop, true);
    }
    
    public static ShopSalesResult updateUserShopSales(UserShopPlay userShop) {
        return saveOrUpdateUserShopSales(userShop, false);
    }
    
    private static ShopSalesResult saveOrUpdateUserShopSales(UserShopPlay userShop, boolean isSave) {
        if (userShop == null)
            return new ShopSalesResult(false, SysError);
        
        String nick = userShop.getNick();
        
        //店铺是可以模糊查询的！！！！
        ShopInfo shopInfo = findShopInfo(nick);
        if (shopInfo == null) {
            return new ShopSalesResult(false, NoShopError);
        }
        if (isSave == true) {
            userShop.setUserId(shopInfo.getUserId());
            userShop.setNick(shopInfo.getShopnick());
        } else {
            if (userShop.getUserId() == null || !userShop.getUserId().equals(shopInfo.getUserId())) {
                log.error("userId is not the same!!!!!!!!!!!!!!one is: " + userShop.getUserId() + ", " +
                        "the other one is:" + shopInfo.getUserId() + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            if (userShop.getNick() == null || !userShop.getNick().equals(shopInfo.getShopnick())) {
                log.error("nick is not the same!!!!!!!!!!!!!!one is: " + userShop.getNick() + ", " +
                        "the other one is:" + shopInfo.getShopnick() + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            
            userShop.setUserId(shopInfo.getUserId());
            userShop.setNick(shopInfo.getShopnick());
        }
        //!!!!!!!!!!!!!!!!!!!
        nick = userShop.getNick();
        
        List<ItemThumb> itemList = null;
        try {
            itemList = SellerAPI.getItemArray(nick, null, new PYSpiderOption(true, SimpleHttpRetryUtil.DefaultRetryTime, 10000));
        } catch (Exception e) {
            log.warn(e.getMessage());
            return new ShopSalesResult(false, SysError);
        }
        if (CommonUtils.isEmpty(itemList)) {
            return new ShopSalesResult(false, NoShopError);
        }
        
        
        
        int saleNum = shopInfo.getLatestTradeCount();
        List<ItemPriceRange> priceRangeList = classifyPriceRange(saleNum, itemList);
        
        String priceRangeJson = JsonUtil.getJson(priceRangeList);
        log.error(priceRangeJson);
        userShop.setPriceRangeJson(priceRangeJson);
        

        int saleNumFromItem = getSaleNumFromItem(itemList);
        double saleAmount = calcuSaleAmount(saleNum, saleNumFromItem, itemList);
        
        userShop.updateShopInfo(shopInfo, saleAmount);

        userShop.jdbcSave();
        return new ShopSalesResult(userShop);
        
    }


    /**
     * clorest510搜不到店铺！！！！！！！！！！！！！！有问题！！！！！！！！！
     * @param args
     */
    public static void main(String[] args) {
        findShopInfo("clorest510");
        try {
            //log.error(URLEncoder.encode("楚之小南", "utf-8"));
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }
    
    
    private static int getSaleNumFromItem(List<ItemThumb> itemList) {
        if (CommonUtils.isEmpty(itemList))
            return 0;
        int saleNumFromItem = 0;
        for (ItemThumb item : itemList) {
            if (item == null)
                continue;
            if (item.getTradeNum() <= 0)
                continue;
            saleNumFromItem += item.getTradeNum();
            
        }
        
        return saleNumFromItem;
    }
    
    private static double calcuSaleAmount(int saleNum, int saleNumFromItem, List<ItemThumb> itemList) {
        if (saleNum <= 0)
            return 0;
        if (CommonUtils.isEmpty(itemList))
            return 0;
        
        if (saleNumFromItem <= 0)
            return 0;
        
        double saleAmount = 0;
        int alreadyNum = 0;
        for (ItemThumb item : itemList) {
            if (item == null)
                continue;
            if (item.getPrice() <= 0 || item.getTradeNum() <= 0)
                continue;
            double price = (double)(item.getPrice()) * 1.0 / 100;
            int tradeNum = (int)Math.round(1.0 * item.getTradeNum() * saleNum / saleNumFromItem);
            if (alreadyNum + tradeNum >= saleNum)
                tradeNum = saleNum - alreadyNum;
            if (tradeNum < 0)
                tradeNum = 0;
            saleAmount += price * tradeNum;
            alreadyNum += tradeNum;
        }
        saleAmount = (double)(Math.round(saleAmount * 100)) / 100.0;
        
        
        return saleAmount;
    }
    
    
    private static final int RangeListSize = 5;
    
    private static List<ItemPriceRange> classifyPriceRange(int totalSaleNum, List<ItemThumb> itemList) {
        List<ItemPriceRange> priceRangeList = new ArrayList<ItemPriceRange>();
        
        if (CommonUtils.isEmpty(itemList))
            return priceRangeList;
        
        double lowestPrice = Double.MAX_VALUE;
        double maxPrice = 0;
        List<ItemThumb> tempItemList = new ArrayList<ItemThumb>();
        for (ItemThumb item : itemList) {
            if (item == null)
                continue;
            if (item.getPrice() < lowestPrice) 
                lowestPrice = item.getPrice();
            if (item.getPrice() > maxPrice)
                maxPrice = item.getPrice();
            tempItemList.add(item);
        }
        if (CommonUtils.isEmpty(tempItemList))
            return priceRangeList;
        
        
        double eachAdd = (maxPrice - lowestPrice) / RangeListSize;
        
        int saleNumFromItem = getSaleNumFromItem(tempItemList);
        log.error(saleNumFromItem + "  ---------------------------");
        
        for (int i = 0; i < RangeListSize; i++) {
            double startPrice = lowestPrice + i * eachAdd;
            double endPrice = startPrice + eachAdd;
            if (i == RangeListSize - 1) {//最后一个
                endPrice = maxPrice + 1;
            }
            
            ItemPriceRange priceRange = new ItemPriceRange();
            priceRange.startPrice = startPrice;
            priceRange.endPrice = endPrice;
            
            for (ItemThumb item : tempItemList) {
                if (item.getPrice() >= startPrice && item.getPrice() < endPrice) {
                    priceRange.rangeItemList.add(item);
                }
            }
            
            if (CommonUtils.isEmpty(priceRange.rangeItemList))
                continue;
            
            double tradeRate = 0;
            if (saleNumFromItem > 0)
                tradeRate = (double)totalSaleNum / saleNumFromItem;
            priceRange.doCalcu(tradeRate);
            priceRangeList.add(priceRange);
        }
        
        
        
        return priceRangeList;
    }
    
    
    
    public static class ItemPriceRange {
        @JsonProperty
        private int itemNum;
        @JsonProperty
        private double startPrice;
        @JsonProperty
        private double endPrice;
        @JsonProperty
        private int saleNum;
        @JsonProperty
        private double saleAmount;
        
        @JsonIgnore
        private List<ItemThumb> rangeItemList = new ArrayList<ItemThumb>();
        
        public int getItemNum() {
            return itemNum;
        }
        public void setItemNum(int itemNum) {
            this.itemNum = itemNum;
        }
        public double getStartPrice() {
            return startPrice;
        }
        public void setStartPrice(double startPrice) {
            this.startPrice = startPrice;
        }
        public double getEndPrice() {
            return endPrice;
        }
        public void setEndPrice(double endPrice) {
            this.endPrice = endPrice;
        }
        public int getSaleNum() {
            return saleNum;
        }
        public void setSaleNum(int saleNum) {
            this.saleNum = saleNum;
        }
        public double getSaleAmount() {
            return saleAmount;
        }
        public void setSaleAmount(double saleAmount) {
            this.saleAmount = saleAmount;
        }
        
        
        
        public void doCalcu(double tradeRate) {
            double maxPrice = 0;
            double lowestPrice = Double.MAX_VALUE;
            for (ItemThumb item : rangeItemList) {
                if (item.getPrice() < lowestPrice) 
                    lowestPrice = item.getPrice();
                if (item.getPrice() > maxPrice)
                    maxPrice = item.getPrice();
            }
            
            startPrice = lowestPrice / 100;
            endPrice = maxPrice / 100;
            itemNum = rangeItemList.size();
            
            int saleNumFromItem = getSaleNumFromItem(rangeItemList);
            saleNum = (int)Math.round(saleNumFromItem * tradeRate);
            saleAmount = calcuSaleAmount(saleNum, saleNumFromItem, rangeItemList);
            
        }
        
    }
    
    
    private static ShopInfo findShopInfo(String nick) {
        ShopInfo shopInfo = null;
        try {
            log.error("nick: " + nick + "-------------------");
            List<ShopInfo> shopList = ShopSearchAPI.getShopInfo(nick, new PYSpiderOption(true, SimpleHttpRetryUtil.DefaultRetryTime));
            shopInfo = NumberUtil.first(shopList);

            log.info("[ shop info : ]" + shopInfo);

            if (shopInfo == null) {
                shopInfo = ShopSearchAPI.getShopInfo(nick, 5000);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
        
        return shopInfo;
    }
    
 
    public static class ShopSalesResult {
        @JsonProperty
        private boolean isOk;
        private Object res;
        private String msg;
        
        
        public boolean isOk() {
            return isOk;
        }

        public void setOk(boolean isOk) {
            this.isOk = isOk;
        }

        public Object getRes() {
            return res;
        }

        public void setRes(Object res) {
            this.res = res;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public ShopSalesResult(boolean isOk, String msg) {
            super();
            this.isOk = isOk;
            this.msg = msg;
            this.res = null;
        }
        
        public ShopSalesResult(Object res) {
            super();
            this.isOk = true;
            this.res = res;
        }
        
        
    }
}
