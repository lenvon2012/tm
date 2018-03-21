package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jdp.JdpModel.JdpItemModel;
import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bustbapi.FenxiaoApi;
import bustbapi.ItemApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.NumberUtil;
import com.dbt.cred.utils.JsonUtil;
import com.taobao.api.domain.FenxiaoProduct;
import com.taobao.api.domain.Item;

import configs.TMConfigs;

public class TBItemUtil {

    private static final Logger log = LoggerFactory.getLogger(TBItemUtil.class);
    
    
    public static Map<Long, FenxiaoProduct> findFenxiaoProductMap(User user, List<ItemPlay> itemList) {
        
        if (CommonUtils.isEmpty(itemList)) {
            return new HashMap<Long, FenxiaoProduct>();
        }
        Set<Long> fenxiaoNumIidSet = new HashSet<Long>();
        
        for (ItemPlay item : itemList) {
            if (item == null) {
                continue;
            }
            if (item.isFenxiao() == false) {
                continue;
            }
            
            fenxiaoNumIidSet.add(item.getNumIid());
        }
        
        //List<FenxiaoProduct> productList = new FenxiaoProductGet(user, fenxiaoNumIidSet).call();
        List<FenxiaoProduct> productList = new FenxiaoApi.FXDistriProductsGetApi(user, fenxiaoNumIidSet).call();
        
        if (CommonUtils.isEmpty(productList)) {
            return new HashMap<Long, FenxiaoProduct>();
        }
        
        log.info("there " + fenxiaoNumIidSet + " fenxiao items need fetch for user: " 
                + user.getUserNick() + ", and result get " + productList.size() + " products------");
        
        
        //log.info(JsonUtil.getJson(productList));
        
        Map<Long, FenxiaoProduct> fenxiaoProductMap = new HashMap<Long, FenxiaoProduct>();
        
        for (FenxiaoProduct product : productList) {
            
            if (product == null) {
                continue;
            }
            fenxiaoProductMap.put(product.getQueryItemId(), product);
            
        }
        
        return fenxiaoProductMap;
        
    }
    
    
    public static Map<Long, Item> findTaobaoItemMap(User user, Set<Long> numIidSet) {
        
        return findTaobaoItemMapWithField(user, numIidSet, ItemApi.FIELDS_WITH_PROPS, true);
    }
    
    /**
     * item中带desc属性
     * @param user
     * @param numIidSet
     * @return
     */
    public static Map<Long, Item> findItemDescMap(User user, Set<Long> numIidSet) {
        
        return findTaobaoItemMapWithField(user, numIidSet, ItemApi.FIELDS_WITH_DESC, false);
    }
    
    private static Map<Long, Item> findTaobaoItemMapWithField(User user, Set<Long> numIidSet, 
            final String fields, boolean isCanJdp) {
        
        if (CommonUtils.isEmpty(numIidSet)) {
            return new HashMap<Long, Item>();
        }
        
        final Set<Long> notDoneIdSet = new HashSet<Long>();
        
        notDoneIdSet.addAll(numIidSet);
        
        //先从jdp中取
        Map<Long, Item> tbItemMap = new HashMap<Long, Item>();
        
        if (isCanJdp == true) {
            if (TMConfigs.Rds.enableJdpPush) {
                List<Item> items = JdpItemModel.findByNumIids(notDoneIdSet);
                for (Item item : items) {
                    if (item == null) {
                        continue;
                    }
                    notDoneIdSet.remove(item.getNumIid());
                    tbItemMap.put(item.getNumIid(), item);
                }
            }
        }
        
        
        if (CommonUtils.isEmpty(notDoneIdSet) == false) {
            List<Long> numIidList = new ArrayList<Long>();
            numIidList.addAll(notDoneIdSet);
            log.info("get " + numIidList.size() + " items from api for user: " 
                    + user.getUserNick() + "-----------------------");
            List<Item> items = new ItemApi.MultiItemsListGet(user.getSessionKey(), 
                    numIidList, fields).call();
            if (CommonUtils.isEmpty(items) == false) {
                for (Item item : items) {
                    if (item == null) {
                        continue;
                    }
                    notDoneIdSet.remove(item.getNumIid());
                    tbItemMap.put(item.getNumIid(), item);
                }
            }
        }
        
        return tbItemMap;
    }
    
    
    
    public static Set<Long> parseIdsToSet(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return new HashSet<Long>();
        }
        String[] idArray = ids.split(",");
        Set<Long> idSet = new HashSet<Long>();

        for (String idStr : idArray) {
            if (StringUtils.isEmpty(idStr)) {
                continue;
            }
            Long id = NumberUtil.parserLong(idStr, 0L);
            if (id == null || id <= 0L) {
                continue;
            }
            idSet.add(id);
        }

        return idSet;
    }
}
