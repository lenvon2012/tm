package actions.catunion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import models.tmsearch.ItemAuthority;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import bustbapi.SellerAPI;

import com.ciaosir.client.api.API.PYSpiderOption;
import com.ciaosir.client.pojo.ItemThumb;
import com.ciaosir.client.utils.SimpleHttpRetryUtil;

public class ItemAuthorityAction {
    
    private static final Logger log = LoggerFactory.getLogger(ItemAuthorityAction.class);
    
    private static final int MaxItemNum = 2000;
    
    
    public static List<ItemAuthority> doFindItemAuthorityList(String nick) throws Exception {
        List<ItemAuthority> itemList = AuthorityCache.getItemListFromCache(nick);
        if (itemList != null)
            return itemList;
            
        itemList = new ArrayList<ItemAuthority>();
                
        try {
            PYSpiderOption option = new PYSpiderOption(null, true, SimpleHttpRetryUtil.DefaultRetryTime, false, MaxItemNum);
            option.setSort("biz30day");     
            List<ItemThumb> itemArray_biz = SellerAPI.getItemArray(nick, null, option);
            option.setSort("coefp");
            List<ItemThumb> itemArray_coe = SellerAPI.getItemArray(nick, null, option);
            /*int tradeIndex = 1;
            for (ItemThumb itemThumb : itemArray_biz) {
                ItemAuthority item = new ItemAuthority();
                item.setFullTitle(itemThumb.getFullTitle());
                item.setTradeNum(itemThumb.getTradeNum());
                item.setPrice(itemThumb.getPrice());
                item.setId(itemThumb.getId());
                item.setPicPath(itemThumb.getPicPath());
                int coeIndex = 1;
                for (ItemThumb item_coe : itemArray_coe) {
                    if (item_coe.getId() != null && item_coe.getId().equals(item.getId())) {
                        break;
                    }
                    coeIndex++;
                }
                item.setDiff(tradeIndex - coeIndex);
                
                tradeIndex++;
                itemList.add(item);
            }*/
            boolean isNoTradeNum = true;
            for (ItemThumb itemThumb : itemArray_biz) {
                if (itemThumb.getTradeNum() > 0) {
                    isNoTradeNum = false;
                    break;
                }
            }
            int itemSize = itemArray_biz.size();
            log.error("itemSize: " + itemSize + " ----------------------------");
            log.error("itemArray_coe Size: " + itemArray_coe.size() + " ----------------------------");
            for (int i = 0; i < itemSize; i++) {
                ItemThumb itemThumb = itemArray_biz.get(0);
                ItemAuthority item = new ItemAuthority();
                item.setFullTitle(itemThumb.getFullTitle());
                item.setTradeNum(itemThumb.getTradeNum());
                item.setPrice(itemThumb.getPrice());
                item.setId(itemThumb.getId());
                item.setPicPath(itemThumb.getPicPath());
                
                int coeSize = itemArray_coe.size();
                int coeIndex = 0;
                for (coeIndex = 0; coeIndex < coeSize; coeIndex++) {
                    ItemThumb item_coe = itemArray_coe.get(coeIndex);
                    if (item_coe.getId() != null && item_coe.getId().equals(item.getId())) {
                        break;
                    }
                }
                
                item.setDiff(0 - coeIndex);
                if (coeIndex < coeSize) {
                    itemArray_coe.remove(coeIndex);
                }
                if (item.getTradeNum() > 0)
                    itemList.add(item);
                else if (isNoTradeNum == true) {
                    item.setDiff(0);
                    itemList.add(item);
                }
                
                itemArray_biz.remove(0);
            }
            
        } catch(Exception e){
            log.error(e.getMessage(), e);
            throw new Exception("fail to load authority info for user: " + nick);
        }       
        
        Collections.sort(itemList, new Comparator<ItemAuthority>() {

            @Override
            public int compare(ItemAuthority item1, ItemAuthority item2) {
                int diff1 = item1.getDiff();
                int diff2 = item2.getDiff();
                if (diff1 < diff2)
                    return -1;
                else if (diff1 == diff2) {
                    int tradeNum1 = item1.getTradeNum();
                    int tradeNum2 = item2.getTradeNum();
                    return tradeNum2 - tradeNum1;
                }
                else
                    return 1;
            }
            
        });
        
        
        AuthorityCache.putToCache(nick, itemList);
        
        return itemList;
    }

    
    
    
    public static class AuthorityCache {
        private static String AuthorityKey = "UserAuthority_";
        
        public static void putToCache(String nick, List<ItemAuthority> itemList) {
            if (StringUtils.isEmpty(nick))
                return;
            try {
                String cacheKey = AuthorityKey + nick;
                Cache.set(cacheKey, itemList, "1h");
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        
        public static List<ItemAuthority> getItemListFromCache(String nick) {
            if (StringUtils.isEmpty(nick))
                return null;
            String cacheKey = AuthorityKey + nick;
            Object obj = Cache.get(cacheKey);
            if (obj == null)
                return null;
            try {
                List<ItemAuthority> itemList = (List<ItemAuthority>)obj;
                return itemList;
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                return null;
            }
        }
    }
    
    
    public static void main(String[] args) {
        PYSpiderOption option = new PYSpiderOption(null, true, SimpleHttpRetryUtil.DefaultRetryTime, false, MaxItemNum);
        option.setSort("biz30day");     
        try {
            List<ItemThumb> itemArray_biz = SellerAPI.getItemArray("居家家", null, option);
            log.error(itemArray_biz.size() + " ---------------------------");
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }
}
