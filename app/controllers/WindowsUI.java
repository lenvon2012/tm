package controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.item.ItemPlay;
import models.showwindow.ShowwindowExcludeItem;
import models.showwindow.ShowwindowMustDoItem;
import models.user.User;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.NumberUtil;
import com.dbt.cred.utils.JsonUtil;

import dao.item.ItemDao;

public class WindowsUI extends TMController {

    private static final Logger log = LoggerFactory.getLogger(WindowsUI.class);
    
    
    
    public static void queryMustItems(String title, long tbCid, long sellerCid, 
            int mustStatus, String orderBy, boolean isDesc, int pn, int ps) {
        
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps);
        
        String catIdStr = "";
        if (tbCid > 0) {
            catIdStr = tbCid + "";
        }
        String sellerCatIdStr = "";
        if (sellerCid > 0L) {
            sellerCatIdStr = sellerCid + "";
        }
        
        final boolean isFenxiao = false;
        final int itemStatus = 0;
        
        TMResult itemRes = null;
        
        if (mustStatus <= WindowStatus.All) {
            
            itemRes = ItemDao.findItemsBySearchRules(user, title, itemStatus, catIdStr, sellerCatIdStr, 
                    isFenxiao, orderBy, isDesc, po);
            
        } else if (mustStatus == WindowStatus.MustItems) {
            
            Set<Long> numIidSet = ShowwindowMustDoItem.findIdsByUser(user.getId());
            
            itemRes = ItemDao.findItemsBySearchRulesWithNumIids(user, title, itemStatus, catIdStr, sellerCatIdStr, 
                    isFenxiao, numIidSet, orderBy, isDesc, po);
            
        } else {
            Set<Long> numIidSet = ShowwindowMustDoItem.findIdsByUser(user.getId());
            
            itemRes = ItemDao.findItemsBySearchRules(user, title, itemStatus, catIdStr, sellerCatIdStr, 
                    isFenxiao, numIidSet, orderBy, isDesc, po);
            
        }
        
        List<ItemPlay> itemList = (List<ItemPlay>) itemRes.getRes();
        
        List<WindowItemInfo> windowItemList = parseToWindowItemInfos(user, itemList);
        
        TMResult res = new TMResult(windowItemList, itemRes.getCount(), po);
        
        renderJSON(JsonUtil.getJson(res));
    }
    
    
    public static void queryExcludeItems(String title, long tbCid, long sellerCid, 
            int excludeStatus, String orderBy, boolean isDesc, int pn, int ps) {
        
        User user = getUser();
        PageOffset po = new PageOffset(pn, ps);
        
        String catIdStr = "";
        if (tbCid > 0) {
            catIdStr = tbCid + "";
        }
        String sellerCatIdStr = "";
        if (sellerCid > 0L) {
            sellerCatIdStr = sellerCid + "";
        }
        
        final boolean isFenxiao = false;
        final int itemStatus = 0;
        
        TMResult itemRes = null;
        
        if (excludeStatus <= WindowStatus.All) {
            
            itemRes = ItemDao.findItemsBySearchRules(user, title, itemStatus, catIdStr, sellerCatIdStr, 
                    isFenxiao, orderBy, isDesc, po);
            
        } else if (excludeStatus == WindowStatus.ExcludeItems) {
            
            Set<Long> numIidSet = ShowwindowExcludeItem.findIdsByUser(user.getId());
            
            itemRes = ItemDao.findItemsBySearchRulesWithNumIids(user, title, itemStatus, catIdStr, sellerCatIdStr, 
                    isFenxiao, numIidSet, orderBy, isDesc, po);
            
        } else {
            Set<Long> numIidSet = ShowwindowExcludeItem.findIdsByUser(user.getId());
            
            itemRes = ItemDao.findItemsBySearchRules(user, title, itemStatus, catIdStr, sellerCatIdStr, 
                    isFenxiao, numIidSet, orderBy, isDesc, po);
            
        }
        
        List<ItemPlay> itemList = (List<ItemPlay>) itemRes.getRes();
        
        List<WindowItemInfo> windowItemList = parseToWindowItemInfos(user, itemList);
        
        TMResult res = new TMResult(windowItemList, itemRes.getCount(), po);
        
        renderJSON(JsonUtil.getJson(res));
    }
    
    
    private static List<WindowItemInfo> parseToWindowItemInfos(User user, List<ItemPlay> itemList) {
        if (CommonUtils.isEmpty(itemList)) {
            return new ArrayList<WindowItemInfo>();
        }
        
        Set<Long> numIidSet = new HashSet<Long>();
        
        for (ItemPlay item : itemList) {
            numIidSet.add(item.getNumIid());
        }
        
        final Set<Long> mustNumIidSet = ShowwindowMustDoItem.findIdsByUser(user.getId(), numIidSet);
        final Set<Long> excludeNumIidSet = ShowwindowExcludeItem.findIdsByUser(user.getId(), numIidSet);
        
        
        List<WindowItemInfo> windowItemList = new ArrayList<WindowItemInfo>();
        
        for (ItemPlay item : itemList) {
            Long numIid = item.getNumIid();
            boolean isMust = mustNumIidSet.contains(numIid);
            boolean isExclude = excludeNumIidSet.contains(numIid);
            
            WindowItemInfo windowItem = new WindowItemInfo(item, isMust, isExclude);
            
            windowItemList.add(windowItem);
            
        }
        
        return windowItemList;
    }
    
    public static class WindowStatus {
        public static final int All = 0;
        
        public static final int MustItems = 1;
        
        public static final int ExcludeItems = 1;
        
        public static final int NotMustItems = 2;
        
        public static final int NotExcludeItems = 2;
    }
    
    public static class WindowItemInfo {
        
        private ItemPlay item;
        
        private boolean isMust;
        
        private boolean isExclude;

        public ItemPlay getItem() {
            return item;
        }

        public void setItem(ItemPlay item) {
            this.item = item;
        }

        public boolean isMust() {
            return isMust;
        }

        public void setMust(boolean isMust) {
            this.isMust = isMust;
        }

        public boolean isExclude() {
            return isExclude;
        }

        public void setExclude(boolean isExclude) {
            this.isExclude = isExclude;
        }

        public WindowItemInfo(ItemPlay item, boolean isMust, boolean isExclude) {
            super();
            this.item = item;
            this.isMust = isMust;
            this.isExclude = isExclude;
        }
        
        
        
    }
    
    
}
