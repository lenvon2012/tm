
package job.autolist.service;

import java.util.ArrayList;
import java.util.List;

import jdp.ApiJdpAdapter;
import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bustbapi.ItemApi;
import bustbapi.ItemApi.ItemUpdate;
import bustbapi.ItemListingApi;

import com.taobao.api.domain.Item;

public class ItemService {
    private static final Logger log = LoggerFactory.getLogger(ItemService.class);

    //获取所有宝贝
    public static List<Item> getAllOnSaleItems(User user) {
        return getAllOnSaleItems(user, null, null);
    }

    public static List<Item> getAllOnSaleItems(User user, Long startModifyTime, Long endModifyTime) {
        log.info("获取用户" + user.getSessionKey() + "所有在卖宝贝");
        //获取用户所有在线的商品
        List<Item> onsaleItemList = new ItemApi.ItemsOnsale(user, startModifyTime, endModifyTime).call();

        //去掉instock的商品
        List<Item> tmpList = new ArrayList<Item>();
        for (Item item : onsaleItemList) {
            if (item.getListTime() != null && "onsale".equals(item.getApproveStatus())) {
                tmpList.add(item);
            }
        }

        onsaleItemList = tmpList;

        //判断虚拟字段和autofill
        List<Item> resultItems = new ArrayList<Item>();

        for (Item item : onsaleItemList) {
            if (!Boolean.TRUE.equals(item.getIsVirtual())) {
                resultItems.add(item);
            } else if (item.getAutoFill() == null) {
                resultItems.add(item);
            }
        }

        return resultItems;
    }

    public static boolean checkIsVirtual(Item item) {
        if (item == null) {
            return true;
        }
        if (!Boolean.TRUE.equals(item.getIsVirtual())) {
            return false;
        } /*else if (item.getAutoFill() == null) {
            return false;
            
          }*/else {
            return true;
        }
    }

    public static Item getSingleItem(User user, Long numIid) {
        Item call = ApiJdpAdapter.get(user).findItem(user, numIid);
        return call;
    }

    //商品下架
    public static DelistOpStatus delistItem(User user, Long numIid) {
        ItemListingApi.ItemUpdateDelisting api = new ItemListingApi.ItemUpdateDelisting(user, numIid);
        Item tmpDelist = api.call();
        if (tmpDelist == null) {
            return new DelistOpStatus(false, api.getErrorMsg(), null);
        } else
            return new DelistOpStatus(true, "", tmpDelist);

    }

    //商品上架
    public static DelistOpStatus listItem(User user, Long numIid, Long num) {
        ItemListingApi.ItemUpdateListing api = new ItemListingApi.ItemUpdateListing(user, numIid, num);
        Item tmpDelist = api.call();
        if (tmpDelist == null) {
            return new DelistOpStatus(false, api.getErrorMsg(), null);
        } else
            return new DelistOpStatus(true, "", tmpDelist);
    }

    public static DelistOpStatus checkItemAttr(User user, Item item) {
        
        ItemUpdate api = null;
        
        if (item.getNum() != null && item.getNum() > 999999) {
            api = new ItemApi.ItemSellerCidUpdater(user.getSessionKey(), 
                    item.getNumIid(), item.getSellerCids());
            
        } else {
            api = new ItemApi.ItemNumUpdater(user.getSessionKey(), 
                    item.getNumIid(), item.getNum());
            
        }
        
        Item resItem = api.call();
        if (resItem != null) {
            return new DelistOpStatus(true, "", resItem);
        }

        String errorMsg = api.getErrorMsg();
        if (StringUtils.isEmpty(errorMsg)) {
            errorMsg = "";
        }

        return new DelistOpStatus(false, errorMsg, null);
    }

    public static class DelistOpStatus {
        private boolean isSuccess;

        private String opMsg;

        private Item item;
        
        public DelistOpStatus(boolean isSuccess, String opMsg){
        	this.isSuccess = isSuccess;
        	this.opMsg = opMsg;
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        public void setSuccess(boolean isSuccess) {
            this.isSuccess = isSuccess;
        }

        public String getOpMsg() {
            return opMsg;
        }

        public void setOpMsg(String opMsg) {
            this.opMsg = opMsg;
        }

        public DelistOpStatus(boolean isSuccess, String opMsg, Item item) {
            super();
            this.isSuccess = isSuccess;
            this.opMsg = opMsg;
            this.item = item;
        }

        public Item getItem() {
            return item;
        }

    }

}
