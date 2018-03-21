package actions.onekey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.item.ItemPlay;
import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.count.CountSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;

import actions.batch.BatchEditResult;
import actions.batch.BatchEditResult.BatchEditErrorMsg;
import actions.batch.BatchEditResult.BatchEditResStatus;
import bustbapi.ItemApi.ItemSellerCidUpdater;

import cache.CountSellerCatCache;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.SellerCat;

import result.TMResult;
import utils.TaobaoUtil;

import dao.item.ItemDao;

public class SellerCatItemAction {
    
    private static final Logger log = LoggerFactory.getLogger(SellerCatItemAction.class);
    
    private static final int MaxModifyItemNum = 800;
    
    public static BatchEditResult doSubmitSellerCatItems(User user, List<ItemPlay> targetItemList, 
            long targetSellerCid, boolean isRemoveOriginSellerCat) {
        
        if (CommonUtils.isEmpty(targetItemList)) {
            return new BatchEditResult(false, "当前没有满足条件的宝贝，请更改条件后重试！");
        }
        
        if (targetItemList.size() > MaxModifyItemNum) {
            return new BatchEditResult(false, "为防止您过久等待，一次最多只能提交" + MaxModifyItemNum + "个宝贝！");
        }

        if (targetSellerCid <= 0) {
            return new BatchEditResult(false, "请先选择目标店铺类目！");
        }
        
        String addSellerCids = targetSellerCid + "";
        
        
        Set<Long> successNumIidSet = new HashSet<Long>();
        List<BatchEditErrorMsg> errorMsgList = new ArrayList<BatchEditErrorMsg>();
        
        boolean isNeedClearSellerCache = false;
        
        for (ItemPlay item : targetItemList) {
            if (item == null) {
                continue;
            }
            
            if (BatchEditResult.checkIsContinueExecute(errorMsgList) == false) {
                break;
            }
            
            String sellerCids = item.getSellerCids();
            
            String newSellerCids = getNewSellerCids(sellerCids, addSellerCids, isRemoveOriginSellerCat);
            if (StringUtils.isEmpty(newSellerCids) || newSellerCids.equals(sellerCids)) {
                successNumIidSet.add(item.getNumIid());
                continue;
            }
            
            ItemSellerCidUpdater updateApi = new ItemSellerCidUpdater(user.getSessionKey(), 
                    item.getNumIid(), newSellerCids);
            Item tbItem = updateApi.call();
            
            if (updateApi.isApiSuccess() == false || tbItem == null) {
                String message = updateApi.getErrorMsg();
                if (StringUtils.isEmpty(message)) {
                    message = "宝贝分类失败，请联系我们！";
                }
                errorMsgList.add(new BatchEditErrorMsg(BatchEditResStatus.CallApiError, message, 
                        item));
                continue;
            }

            item.setSellerCids(newSellerCids);
            boolean isDbSuccess = item.rawUpdate();
            if (isDbSuccess == true) {
                successNumIidSet.add(item.getNumIid());
            } else {
                errorMsgList.add(new BatchEditErrorMsg(BatchEditResStatus.DBError, "数据库操作失败！", 
                        item));
            }
            isNeedClearSellerCache = true;
        }
        
        if (isNeedClearSellerCache == true) {
            CountSellerCatCache.get().clear(user);
        }
        
        int notExecuteNum = targetItemList.size() - successNumIidSet.size() 
                - errorMsgList.size();
        
        String prevMessage = "有" + successNumIidSet.size() + "个宝贝分类成功";
        
        if (CommonUtils.isEmpty(errorMsgList) == false) {
            prevMessage += "，失败了" + errorMsgList.size() + "个";
        }
        if (notExecuteNum > 0) {
            prevMessage += "，有" + notExecuteNum + "个宝贝尚未执行";
        }
        
        BatchEditResult updateRes = new BatchEditResult(prevMessage, errorMsgList);
        
        return updateRes;
        
    }
    
    

    
    private static String getNewSellerCids(String originSellerCids, String addSellerCids, 
            boolean isRemoveOriginSellerCat) {
        
        if (StringUtils.isEmpty(originSellerCids) || originSellerCids.equals("-1")
                || isRemoveOriginSellerCat == true) {
            return "," + addSellerCids + ",";
        }
        
        if (originSellerCids.contains("," + addSellerCids + ",")) {
            return originSellerCids;
        }
        
        return originSellerCids + addSellerCids + ",";
        
    }
    

    /**
     * 获取店铺的子类目
     * @param user
     * @return
     */
    public static List<SellerCat> getShopChildSellerCats(User user) {
        
        Long userId = user.getId();
        
        List<SellerCat> sellerCatList = ShopSellerCatsCache.getFromCache(userId);
     
        if (sellerCatList == null) {
            sellerCatList = TaobaoUtil.getSellerCatByUserId(user);
            
            ShopSellerCatsCache.putToCache(userId, sellerCatList);
        }
        
        if (CommonUtils.isEmpty(sellerCatList)) {
            return new ArrayList<SellerCat>();
        }
        
        List<SellerCat> childCatList = new ArrayList<SellerCat>();
        
        for (SellerCat sellerCat : sellerCatList) {
            if (isChildCat(sellerCat, sellerCatList)) {
                childCatList.add(sellerCat);
            }
        }
        
        return childCatList;
    }
    
    private static boolean isChildCat(SellerCat sellerCat, List<SellerCat> sellerCatList) {
        
        Long cid = sellerCat.getCid();
        if (cid == null || cid <= 0L) {
            return false;
        }
        
        for (SellerCat tempCat : sellerCatList) {
            if (tempCat == null) {
                continue;
            }
            Long parentCid = tempCat.getParentCid();
            
            if (cid.equals(parentCid)) {
                return false;
            }
        }
        
        return true;
        
    }
    
    
    public static class ShopSellerCatsCache {
        
        private static final String Prefix = "ShopSellerCatsCache_";
        
        private static String genKey(Long userId) {
            return Prefix + userId + "_";
        }
        
        public static List<SellerCat> getFromCache(Long userId) {
            if (userId == null || userId <= 0L) {
                return null;
            }
            
            String key = genKey(userId);
            
            try {
                
                return (List<SellerCat>) Cache.get(key);
                
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                return null;
            }
        }
        
        
        public static void putToCache(Long userId, List<SellerCat> sellerCatList) {
            
            if (userId == null || userId <= 0L || sellerCatList == null) {
                return;
            }
            
            String key = genKey(userId);
            
            try {
                
                Cache.set(key, sellerCatList, "3d");
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        
        public static void clearCache(Long userId) {
            if (userId == null || userId <= 0L) {
                return;
            }
            
            String key = genKey(userId);
            
            try {
                
                Cache.delete(key);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }
    
}
