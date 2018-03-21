package job.apiget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.item.ItemPlay;
import models.popularized.Popularized;
import models.user.User;
import models.vgouitem.VGouItem;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.jobs.Job;
import bustbapi.UmpPromotionApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.api.BusAPIs;
import com.ciaosir.client.utils.JsonUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.client.utils.SimpleHttpRetryUtil;
import com.taobao.api.domain.PromotionDisplayTop;
import com.taobao.api.domain.PromotionInItem;

import dao.item.ItemDao;
import dao.popularized.PopularizedDao;
import dao.popularized.VGItemDao;

public class UpdatePopularizedJob extends Job {
    private final static Logger log = LoggerFactory.getLogger(UpdatePopularizedJob.class);
    
    private User user;
    
    //private static boolean isOnTest = true;
    
    public UpdatePopularizedJob(User user) {
        this.user = user;
    }
    
    public void doJob() {
        try {
            //if (isOnTest == true)
            //    return;
            
            if (user == null || !user.isVaild())
                return;
            //获取item
            List<ItemPlay> itemList = ItemDao.findByUserId(user.getId());
            List<Popularized> tgItemList = PopularizedDao.queryPopularizedsByUserId(user.getId());
            List<VGouItem> vgItemList = VGItemDao.queryVGouItemsByUserId(user.getId());
            
            if (CommonUtils.isEmpty(itemList))
                itemList = new ArrayList<ItemPlay>();
            if (CommonUtils.isEmpty(tgItemList))
                tgItemList = new ArrayList<Popularized>();
            if (CommonUtils.isEmpty(vgItemList))
                vgItemList = new ArrayList<VGouItem>();    
            
            Map<Long, Popularized> tgItemMap = putIntoPopularizedMap(tgItemList);
            Map<Long, VGouItem> vgItemMap = putIntoVGItemMap(vgItemList);
            
            for (ItemPlay item : itemList) {
                if (item == null)
                    continue;
                Long numIid = item.getNumIid();
                if (numIid == null || numIid <= 0)
                    continue;
                Popularized tgItem = tgItemMap.get(numIid);
                VGouItem vgItem = vgItemMap.get(numIid);
                doSetItemInfoWithOutSaveToDB(item, tgItem, vgItem);
            }
            
            doSetSkuPriceWithOutSaveToDB(user, itemList, tgItemList, vgItemList);
            
            for (Popularized tgItem : tgItemList) {
                if (tgItem == null)
                    continue;
                //没有用jdbcsave
                PopularizedDao.updatePopularizePrice(tgItem);
            }
            
            for (VGouItem vgItem : vgItemList) {
                if (vgItem == null)
                    continue;
                //没有用jdbcsave
                VGItemDao.updateVGItemPriceProperties(vgItem);
            }
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return;
        }
    }
    
    
    /**
     * 设置推广宝贝的打折价，但是还没有更新到数据库中
     * @param user
     * @param tgItemList
     * @param vgItemList
     */
    private static void doSetSkuPriceWithOutSaveToDB(User user, List<ItemPlay> itemList, List<Popularized> tgItemList, List<VGouItem> vgItemList) {
        try {
            if (user == null || !user.isVaild())
                return;
            if (CommonUtils.isEmpty(tgItemList))
                tgItemList = new ArrayList<Popularized>();
            if (CommonUtils.isEmpty(vgItemList))
                vgItemList = new ArrayList<VGouItem>();
            if (CommonUtils.isEmpty(tgItemList) && CommonUtils.isEmpty(vgItemList))
                return;
            
            //先将skuMinPrice进行重置，防止skuList为空
            resetSkinMinPrice(tgItemList, vgItemList);
            
            Set<Long> numIidSet = putNumIidIntoSet(tgItemList, vgItemList);

            Map<Long, ItemPlay> itemMap = putIntoItemMap(itemList);
            Map<Long, Popularized> tgItemMap = putIntoPopularizedMap(tgItemList);
            Map<Long, VGouItem> vgItemMap = putIntoVGItemMap(vgItemList);
            

            //获取skuMinPrice 打折价
            for (Long numIid : numIidSet) {
                if (numIid == null || numIid <= 0)
                    continue;
                ItemPlay item = itemMap.get(numIid);
                if (item == null)
                    continue;
                double skuMinPrice = getItemSkuMinPrice(user, item);
                if (skuMinPrice <= 0)
                    continue;
                
                Popularized tgItem = tgItemMap.get(numIid);
                if (tgItem != null) {
                    tgItem.setSkuMinPrice(skuMinPrice);
                }
                VGouItem vgItem = vgItemMap.get(numIid);
                if (vgItem != null) {
                    vgItem.setSkuMinPrice(skuMinPrice);
                }
                
                
            }
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }
    
    private static void doSetItemInfoWithOutSaveToDB(ItemPlay item, Popularized tgItem, VGouItem vgItem) {
        if (item == null)
            return;
        if (tgItem != null) {
            tgItem.setPicPath(item.getPicURL());
            tgItem.setPrice(item.getPrice());
            tgItem.setSalesCount(item.getSalesCount());
            tgItem.setTitle(item.getTitle());
        } 
        if (vgItem != null) {
            vgItem.setImg(item.getPicURL());
            vgItem.setPrice(item.getPrice());
            vgItem.setTitle(item.getTitle());
        }
    }

    private static Map<Long, ItemPlay> putIntoItemMap(List<ItemPlay> itemList) {
        Map<Long, ItemPlay> itemMap = new HashMap<Long,ItemPlay>();
        if (CommonUtils.isEmpty(itemList))
            return itemMap;
        
        for (ItemPlay item : itemList) {
            if (item == null)
                continue;
            Long numIid = item.getNumIid();
            if (numIid == null || numIid <= 0)
                continue;
            itemMap.put(numIid, item);
        }
        
        return itemMap;
    }
    
    private static Map<Long, Popularized> putIntoPopularizedMap(List<Popularized> tgItemList) {
        Map<Long, Popularized> tgItemMap = new HashMap<Long, Popularized>();
        if (CommonUtils.isEmpty(tgItemList))
            return tgItemMap;
        
        for (Popularized tgItem : tgItemList) {
            if (tgItem == null)
                continue;
            Long numIid = tgItem.getNumIid();
            if (numIid == null || numIid <= 0)
                continue;
            tgItemMap.put(numIid, tgItem);
        }
        
        return tgItemMap;
    }
    
    private static Map<Long, VGouItem> putIntoVGItemMap(List<VGouItem> vgItemList) {
        Map<Long, VGouItem> vgItemMap = new HashMap<Long, VGouItem>();
        if (CommonUtils.isEmpty(vgItemList))
            return vgItemMap;
        
        for (VGouItem vgItem : vgItemList) {
            if (vgItem == null)
                continue;
            Long numIid = vgItem.getNumIid();
            if (numIid == null || numIid <= 0)
                continue;
            vgItemMap.put(numIid, vgItem);
        }
        
        return vgItemMap;
    }
    
    
    private static Set<Long> putNumIidIntoSet(List<Popularized> tgItemList, List<VGouItem> vgItemList) {
        Set<Long> numIidSet = new HashSet<Long>();
        
        for (Popularized tgItem : tgItemList) {
            if (tgItem == null)
                continue;
            Long numIid = tgItem.getNumIid();
            if (numIid == null || numIid <= 0)
                continue;
            numIidSet.add(numIid);
        }
        
        for (VGouItem vgItem : vgItemList) {
            if (vgItem == null)
                continue;
            Long numIid = vgItem.getNumIid();
            if (numIid == null || numIid <= 0)
                continue;
            numIidSet.add(numIid);
        }
        
        return numIidSet;
    }
    
    
    private static void resetSkinMinPrice(List<Popularized> tgItemList, List<VGouItem> vgItemList) {

        for (Popularized tgItem : tgItemList) {
            if (tgItem == null)
                continue;
            //先将skuMinPrice进行重置，防止skuList为空
            tgItem.resetSkuMinPrice();
        }
        
        for (VGouItem vgItem : vgItemList) {
            if (vgItem == null)
                continue;
            //先将skuMinPrice进行重置，防止skuList为空
            vgItem.resetSkuMinPrice();
        }
        
    } 
    
    /*
    private static Map<Long, List<Sku>> putIntoSkuListMap(List<Sku> skuList) {
        Map<Long, List<Sku>> skuListMap = new HashMap<Long, List<Sku>>();
        if (CommonUtils.isEmpty(skuList))
            return skuListMap;
        
        for (Sku sku : skuList) {
            if (sku == null)
                continue;
            Long numIid = sku.getNumIid();
            if (numIid == null || numIid <= 0)
                continue;
            List<Sku> tempList = skuListMap.get(numIid);
            if (CommonUtils.isEmpty(tempList))
                tempList = new ArrayList<Sku>();
            tempList.add(sku);
            
            skuListMap.put(numIid, tempList);
        }
        
        return skuListMap;
    }*/
    
    
    
    /*public static double getItemSkuMinPrice(User user, ItemPlay item) {
        try {
            //if (isOnTest == true)
            //    return 0;
            if (user == null || !user.isVaild())
                return 0;
            if (item == null)
                return 0;
            Long numIid = item.getNumIid();
            if (numIid == null || numIid <= 0)
                return 0;
            //先用ump api来搜索
            
            
            
            double promoPrice = ItemPromotionPriceSpider.doSpider(user, item);
            return promoPrice;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return 0;
        }
    }*/
    
    
    public static double getItemSkuMinPriceWithCache(User user, ItemPlay item) {
        Double minPrice = ItemSkuMinPriceCache.getFromCache(item.getId());
        if(minPrice != null){
            return minPrice;
        }else{
            minPrice = getItemSkuMinPrice(user, item);
            ItemSkuMinPriceCache.putToCache(item.getNumIid(), minPrice);
            
        }
        return minPrice;
    }
    
    public static class ItemSkuMinPriceCache {
        private static final String Prefix = "ItemSkuMinPriceCache";
        
        private static String genKey(Long numIid) {
            return Prefix + "_" + numIid;
        }
        
        public static Double getFromCache(Long numIid) {
            try {
                String key = genKey(numIid);
                Double minPrice = (Double) Cache.get(key);
                return minPrice;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                return null;
            }
        }
        
        public static void putToCache(Long numIid, double minPrice) {
            try {
                String key = genKey(numIid);
                Cache.set(key, minPrice, "10min");
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                
            }
        }
    }
    
    
    private static final Set<String> FixUmpPriceNickSet = new HashSet<String>();
    static {
        FixUmpPriceNickSet.add("吃货居旗舰店");
        FixUmpPriceNickSet.add("岩松电器专营店");
    }
    
    
    /**
     * 获取宝贝的促销价
     * @param toCancelNumIid
     * @return
     */
    public static double getItemSkuMinPrice(User user, ItemPlay item) {
        try {
            //if (isOnTest == true)
            //    return 0;
            if (user == null || !user.isVaild())
                return 0;
            if (item == null)
                return 0;
            Long numIid = item.getNumIid();
            if (numIid == null || numIid <= 0)
                return 0;
            
            //先用ump api来搜索
            UmpPromotionApi.UmpPromotionGetApi api = new UmpPromotionApi.UmpPromotionGetApi(user.getSessionKey(), numIid);
            PromotionDisplayTop promotion = api.call();
            if (api.isApiSuccess() && promotion != null) {
                double promoPrice = getItemPromoPriceByUmpApi(promotion, numIid);
                //有些折扣价用api取不出来
                if (promoPrice <= 0 && FixUmpPriceNickSet.contains(user.getUserNick()) == true) {
                    
                } else {
                    return promoPrice;
                }
                
            } 
            
            double promoPrice = ItemPromotionPriceSpider.doSpider(user, item);
            return promoPrice;
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return 0;
        }
        
    }
    
    
    
    
    private static double getItemPromoPriceByUmpApi(PromotionDisplayTop promotion, Long numIid) {
        try {
            if (promotion == null) {
                return 0;
            }
            List<PromotionInItem> promotionItemList = promotion.getPromotionInItem();
            if (CommonUtils.isEmpty(promotionItemList)) {
                log.error("the promotionItemList size is empty, numIid: " + numIid + "---------------");
                return 0;
            }
                
            
            double minPrice = 0;
            for (PromotionInItem promotionItem : promotionItemList) {
                if (promotionItem == null) {
                    continue;
                }
                //log.info("promotionItem name: " + promotionItem.getName() 
                //        + ", price: " + promotionItem.getItemPromoPrice() + "---------------");
                String name = promotionItem.getName();
                if (!StringUtils.isEmpty(name)) {
                    name = name.toLowerCase();
                    if (name.indexOf("vip") >= 0)
                        continue;
                }
                String priceStr = promotionItem.getItemPromoPrice();
                double price = NumberUtil.parserDouble(priceStr, 0);
                if (price <= 0)
                    continue;
                
                if (minPrice <= 0)
                    minPrice = price;
                else if (minPrice > price) {
                    minPrice = price;
                }
            }
            return minPrice;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return 0;
        }
        
    }
    
    
    /*
    public static double getItemSkuMinPrice(User user, Long numIid) {
        try {
            if (user == null || !user.isVaild())
                return 0;
            if (numIid == null || numIid <= 0)
                return 0;
            
            List<Long> numIidList = new ArrayList<Long>();
            numIidList.add(numIid);
            
            ItemSkuApi.ItemsSkuGetApi api = new ItemSkuApi.ItemsSkuGetApi(user.getSessionKey(), user, numIidList);
            List<Sku> skuList = api.call();
            if (CommonUtils.isEmpty(skuList))
                skuList = new ArrayList<Sku>();
            
            double skuMinPrice = getSkuMinPrice(numIid, skuList);

            return skuMinPrice;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return 0;
        }
        
    } 
    
    
    
    
    
    private static double getSkuMinPrice(Long numIid, List<Sku> skuList) {
        //if (isOnTest == true)
        //    return 0;
        
        if (numIid == null || numIid <= 0)
            return 0;
        
        if (CommonUtils.isEmpty(skuList))
            return 0;
        
        double skuMinPrice = 0;
        for (Sku sku : skuList) {
            if (sku == null)
                continue;
            Long skuNumIid = sku.getNumIid();
            if (skuNumIid == null || skuNumIid <= 0)
                continue;
            if (!numIid.equals(skuNumIid))
                continue;
            
            String priceStr = sku.getPrice();
            double skuPrice = NumberUtil.parserDouble(priceStr, 0);
            if (skuPrice <= 0)
                continue;
            String status = sku.getStatus();
            if ("normal".equals(status) == false) {//这个sku状态不对
                continue;
            }
            
            if (skuMinPrice <= 0)
                skuMinPrice = skuPrice;
            else if (skuMinPrice > skuPrice) {
                skuMinPrice = skuPrice;
            }
        }
        
        return skuMinPrice;
    }*/
    
    
    
    /**
     * 通过爬虫获取宝贝优惠价
     * @author Administrator
     *
     */
    public static class ItemPromotionPriceSpider {
        public static double doSpider(User user, ItemPlay item) {
            try {
                if (user == null || !user.isVaild())
                    return 0;
                if (item == null)
                    return 0;
                Long numIid = item.getNumIid();
                if (numIid == null || numIid <= 0)
                    return 0;
                Long userId = user.getId();
                if (userId == null || userId <= 0) 
                    return 0;
                double price = item.getPrice();
                if (price <= 0)
                    return 0;
                int intPrice = (int)(price * 100);
                
                return doSpider(userId, numIid, intPrice);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                return 0;
            }
            
        }
        
        
        //http://mdskip.taobao.com/core/initItemDetail.htm?cartEnable=true&itemTags=258,651,775,1035,1163,1478,1483,1547,1611,2049,2059,2251,2443,2507,2699,3974,4166,25282,28802&household=false&sellerUserTag=34672672&isUseInventoryCenter=false&isRegionLevel=false&tgTag=false&sellerPreview=false&sellerUserTag2=18015686999670784&isForbidBuyItem=false&showShopProm=false&queryMemberRight=true&notAllowOriginPrice=false&itemId=39508961099&isApparel=false&tmallBuySupport=true&sellerUserTag3=70368746307712&sellerUserTag4=1152921504642516355&isIFC=false&addressLevel=2&offlineShop=false&service3C=false&isAreaSell=false&isSecKill=false&callback=setMdskip&ref=http%3A%2F%2Fz.tobti.com%2Fassociate%2Fassociate%3Fsize%3D750%26count%3D0
        private static double doSpider(Long userId, Long numIid, int intPrice) {
            try {
                String url = "http://ajax.tbcdn.cn/json/umpStock.htm?itemId=" + numIid + "&p=1&price=" + intPrice + "&chnl=pc&sellerId=" + userId;
                log.error("spider promotion price from url: " + url);
                
                /*
                String url3 = "http://ajax.tbcdn.cn/json/umpStock.htm?itemId=23740300591&p=1&rcid=50025705&sts=471077376,1170936092103278596,70368744210560,70373041308675&chnl=pc&price=7800&sellerId=846724263&shopId=&cna=ixT%2FCFUBeUUCAdIgu115MQCl&ref=http%3A%2F%2Flocalhost%3A9000%2Fdianputuiguang%2Ftuiguang&buyerId=0&nick=&tg=&tg2=&tg3=&tg4=&tg6=";
                String url1 = "http://ajax.tbcdn.cn/json/umpStock.htm?itemId=17700754382&p=1&price=10800&chnl=pc&sellerId=122478346";
                String url2 = "http://ajax.tbcdn.cn/json/umpStock.htm?itemId=17700754382&p=1&rcid=30&sts=274290688,1170940490216898628,216454394563035264,13581171923616771&chnl=pc&price=10800&sellerId=122478346&shopId=&cna=ixT%&tg=1052672&tg2=8&tg3=274877906944&tg4=70368744177664&tg6=0";
                */
                String refer = "http://item.taobao.com/item.htm?id=" + numIid;
                String content = SimpleHttpRetryUtil.retryGetWebContent(url, refer);
                //log.error(content);
                
                double promoPrice = newParsePromoDataByRegex(content);
                
                return promoPrice;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                return 0;
            }
        }
        
        
        private static double newParsePromoDataByRegex(String content) {
            try {
                if (StringUtils.isEmpty(content))
                    return 0;
                content = content.replace("\t", "");
                content = content.replace("\r", "");
                content = content.replace("\n", "");
                content = content.trim();
                String pricePrefix = "price:\"";
                double promoPrice = 0;
                int index = content.indexOf(pricePrefix);
                while (index >= 0) {
                    content = content.substring(index + pricePrefix.length()).trim();
                    index = content.indexOf("\"");
                    if (index < 0)
                        break;
                    String priceStr = content.substring(0, index).trim();
                    double price = NumberUtil.parserDouble(priceStr, 0);
                    if (price <= 0)
                        continue;
                    if (promoPrice <= 0)
                        promoPrice = price;
                    else if (promoPrice > price)
                        promoPrice = price;
                    
                    index = content.indexOf(pricePrefix);
                }

                return promoPrice;
                
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                return 0;
            }
        }
        
        /*
         * TB.PromoData = {
            "def":[{       
                type:  "51狂欢价"  ,
                price:"78.00",         
                limitTime: "",
                channelkey: "",
                add: "",
                gift: "",
                cart: "true",
                amountRestriction:"",
                isStart:"false"                 
            },              
            {       
                type: "店铺VIP"    ,
                wenan:"<a target='_top' href='https://login.taobao.com/member/login.jhtml?f=top&redirectURL=http%3A%2F%2Fitem.taobao.com%2Fitem.htm%3Fspm%3Da2106.m5221.1000384.d11.CmK7cd%26id%3D17700754382%26_u%3D3g7d0mvc513%26scm%3D1029.newlist-0.bts1.0%26ppath%3D%26sku%3D'>登录</a>后查看是否享受此优惠",
                        limitTime: "",
                channelkey: "",
                add: "",
                gift: "",
                cart: "true",
                amountRestriction:"",
                isStart:"false"                 
            }],              ";20506:3217382;1627207:3232483;":[
                {       
            type:  "51狂欢价"  ,
             price:"78.00",         limitTime: "",
            channelkey: "",
            add: "",
            gift: "",
            cart: "true",
            amountRestriction:"",
            isStart:"false"                 },              {       
            type: "店铺VIP"    ,
            wenan:"<a target='_top' href='https://login.taobao.com/member/login.jhtml?f=top&redirectURL=http%3A%2F%2Fitem.taobao.com%2Fitem.htm%3Fspm%3Da2106.m5221.1000384.d11.CmK7cd%26id%3D17700754382%26_u%3D3g7d0mvc513%26scm%3D1029.newlist-0.bts1.0%26ppath%3D%26sku%3D'>登录</a>后查看是否享受此优惠",
                        limitTime: "",
            channelkey: "",
            add: "",
            gift: "",
            cart: "true",
            amountRestriction:"",
            isStart:"false"                 }           ],              ";20506:3217383;1627207:3232483;":[
                {       
            type:  "51狂欢价"  ,
             price:"78.00",         limitTime: "",
            channelkey: "",
            add: "",
            gift: "",
            cart: "true",
            amountRestriction:"",
            isStart:"false"                 },              {       
            type: "店铺VIP"    ,
            wenan:"<a target='_top' href='https://login.taobao.com/member/login.jhtml?f=top&redirectURL=http%3A%2F%2Fitem.taobao.com%2Fitem.htm%3Fspm%3Da2106.m5221.1000384.d11.CmK7cd%26id%3D17700754382%26_u%3D3g7d0mvc513%26scm%3D1029.newlist-0.bts1.0%26ppath%3D%26sku%3D'>登录</a>后查看是否享受此优惠",
                        limitTime: "",
            channelkey: "",
            add: "",
            gift: "",
            cart: "true",
            amountRestriction:"",
            isStart:"false"                 }           ],              ";20506:3217384;1627207:3232483;":[
                {       
            type:  "51狂欢价"  ,
             price:"78.00",         limitTime: "",
            channelkey: "",
            add: "",
            gift: "",
            cart: "true",
            amountRestriction:"",
            isStart:"false"                 },              {       
            type: "店铺VIP"    ,
            wenan:"<a target='_top' href='https://login.taobao.com/member/login.jhtml?f=top&redirectURL=http%3A%2F%2Fitem.taobao.com%2Fitem.htm%3Fspm%3Da2106.m5221.1000384.d11.CmK7cd%26id%3D17700754382%26_u%3D3g7d0mvc513%26scm%3D1029.newlist-0.bts1.0%26ppath%3D%26sku%3D'>登录</a>后查看是否享受此优惠",
                        limitTime: "",
            channelkey: "",
            add: "",
            gift: "",
            cart: "true",
            amountRestriction:"",
            isStart:"false"                 }           ]       }

         */
        private static double parsePromoData(String content) {
            if (StringUtils.isEmpty(content))
                return 0;
            try {
                content = content.replace("\t", "");
                content = content.replace("\r", "");
                content = content.replace("\n", "");
                content = content.trim();
                if (StringUtils.isEmpty(content))
                    return 0;
                int index = content.indexOf("TB.PromoData");
                if (index < 0)
                    return 0;
                content = content.substring(index + "TB.PromoData".length()).trim();
                index = content.indexOf("=");
                if (index < 0)
                    return 0;
                content = content.substring(index + "=".length()).trim();
                content = "(" + content + ")";
                //转成json
                JsonNode rootNode = JsonUtil.parserJSONP(content);
                if (rootNode == null)
                    return 0;
                double promoPrice = 0;
                /*Iterator<JsonNode> nodeIter = rootNode.getElements();
                if (nodeIter == null || nodeIter.hasNext() == false)
                    return 0;
                while (nodeIter.hasNext()) {
                    JsonNode defNode = nodeIter.next();
                }*/
                JsonNode defNode = rootNode.get("def");
                if (defNode == null)
                    return 0;
                promoPrice = parseOneJsonNode(defNode);
                
                return promoPrice;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                return 0;
            }
        }
        
        
        private static double parseOneJsonNode(JsonNode defNode) throws Exception {
            if (defNode == null)
                return 0;
            Iterator<JsonNode> childNodeIter = defNode.getElements();
            if (childNodeIter == null || childNodeIter.hasNext() == false)
                return 0;
            
            double minPrice = 0;
            while (childNodeIter.hasNext()) {
                JsonNode priceJsonNode = childNodeIter.next();
                if (priceJsonNode == null)
                    continue;
                String priceStr = priceJsonNode.get("price").getTextValue();
                //log.error(priceStr);
                if (StringUtils.isEmpty(priceStr)) {
                    continue;
                }
                double tempPrice = NumberUtil.parserDouble(priceStr, 0);
                if (tempPrice <= 0)
                    continue;
                if (minPrice <= 0) 
                    minPrice = tempPrice;
                else if (minPrice > tempPrice)
                    minPrice = tempPrice;
                
            }
            
            return minPrice;
        }
    }
    
    public static void main(String[] args) {
        //ItemPromotionPriceSpider.doSpider(122478346L, 17700754382L, 10800);
        //ItemPromotionPriceSpider.doSpider(1039626382L, 17481119311L, 2880);//楚之小南
        
        String url = "http://mdskip.taobao.com/core/initItemDetail.htm?cartEnable=true&tgTag=false&tmallBuySupport=true&isApparel=false&isSecKill=false&queryMemberRight=true&showShopProm=false&isAreaSell=false&offlineShop=false&sellerUserTag3=70368746307712&sellerUserTag4=1152921504642516355&isUseInventoryCenter=false&itemId=39237964285&sellerUserTag=34672672&isIFC=false&itemTags=258,651,775,1163,1478,1483,1547,1611,2049,2059,2251,2443,2507,2699,3974,4166,25282,28802&isRegionLevel=false&sellerUserTag2=18015686999670784&isForbidBuyItem=false&notAllowOriginPrice=false&service3C=false&addressLevel=2&household=false&sellerPreview=false&callback=setMdskip&ref=http%3A%2F%2Fdetail.tmall.com%2Fitem.htm%3Fid%3D39508961099";
        String refer = "http://item.taobao.com/item.htm?id=39237964285";
        
        String content = BusAPIs.directGet(url, refer, BusAPIs.DEFAULT_UA);
        
        log.info(content);
        
        
        url = "http://ajax.tbcdn.cn/json/umpStock.htm?itemId=40325190945&p=1&price=44000&chnl=pc&sellerId=60204144";
        refer = "http://item.taobao.com/item.htm?id=40325190945";
        
        content = BusAPIs.directGet(url, refer, BusAPIs.DEFAULT_UA);
        
        log.info(content);
    }
    
}
