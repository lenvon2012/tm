
package job.apiget;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import jdp.ApiJdpAdapter;
import jdp.JdpModel.JdpItemModel;
import jdp.JdpModel.JdpTradeModel;
import job.ApplicationStopJob;
import job.sync.SyncSimbaJob;
import job.writter.TradeWritter;
import message.itemupdate.ItemApiDoing;
import message.itemupdate.ItemDBDone;
import models.UserDiag;
import models.item.ItemExtra;
import models.item.ItemPlay;
import models.item.NewItemCatPlay;
import models.op.RawId;
import models.updatetimestamp.updates.ItemUpdateTs;
import models.updatetimestamp.updatestatus.ItemDailyUpdateTask;
import models.user.User;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import secure.SimulateRequestUtil;
import titleDiag.DiagResult;
import actions.DiagAction;
import actions.batch.OutLinksGetAction;
import actions.delist.DelistUpdateAction;
import bustbapi.ItemApi;
import bustbapi.ItemApi.ItemDeleteIncrementGet;
import bustbapi.ItemApi.ItemsInventoryCount;
import bustbapi.ItemApi.ItemsInventoryPage;
import bustbapi.ItemApi.ItemsOnsalePage;
import bustbapi.ItemApi.MultiItemsListGet;
import bustbapi.JDPApi;
import bustbapi.JDPApi.JdpItemStatus;
import bustbapi.TMTradeApi;
import bustbapi.TMTradeApi.ShopBaseTradeInfo;
import bustbapi.TMTradeApi.TradesSoldIncrementextends;
import cache.CacheUserClearer;
import cache.CountItemCatCache;
import cache.CountItemCatStatusCache;
import cache.CountSellerCatCache;
import cache.CountSellerCatStatusCache;
import cache.UserHasTradeItemCache;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.NumberUtil;
import com.ciaosir.client.utils.SplitUtils;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.NotifyItem;
import com.taobao.api.domain.Trade;

import configs.TMConfigs;
import configs.TMConfigs.Debug;
import configs.TMConfigs.ExpiredTime;
import configs.TMConfigs.PageSize;
import configs.TMConfigs.Rds;
import configs.TMConfigs.Sale;
import controllers.APIConfig;
import dao.item.ItemDao;
import dao.item.sub.ItemDeletedCheckJob;
import dao.item.sub.NumIidScoreModifed;
import dao.trade.OrderDisplayDao;

public class ItemUpdateJob extends TBUpdateJob {

    private static final Logger log = LoggerFactory.getLogger(ItemUpdateJob.class);

    private static final String TAG = "ItemUpdateJob";

    public static final Long MAX_ITEM_PAGE_NUM = 500L;

    public static PYFutureTaskPool<List<Item>> pool = new PYFutureTaskPool<List<Item>>(32);

//    static PYFutureTaskPool<List<Item>> pool = new PYFutureTaskPool<List<Trade>>(8);

    static List<CacheUserClearer> needToClear = new ArrayList<CacheUserClearer>();

//    boolean doForItemTrade = TMConfigs.App.IS_TRADE_ALLOW && !APIConfig.get().doClo

    static {
        ApplicationStopJob.addShutdownPool(pool);
        needToClear.add(CountSellerCatCache.get());
        needToClear.add(CountSellerCatStatusCache.get());
        needToClear.add(CountItemCatCache.get());
        needToClear.add(CountItemCatStatusCache.get());
    }

    public static class UserUpateWorkRecord {
        int localItemNum;

        int localOnSaleNum;

        int localInventoryNum;

        int remoteItemNum;

        int remoteOnSaleNum;

        int remoteInventoryNum;

        int jdpItemNum;

        int jdpOnSaleNum;

        int jdpInventoryNum;

        int localTradeNum;

    }

    public ItemUpdateJob(Long userId) {
        super(userId);
        this.now = DateUtil.formCurrDate();
        log.warn("[new item update job]" + userId);
    }

    public ItemUpdateJob(Long userId, boolean first) {
        super(userId);
        this.now = DateUtil.formCurrDate();
        this.isFirst = first;
        log.warn("[new item update job]" + userId);
    }

//    public ItemUpdateJob(Long userId, Long ts) {
//        super(userId);
//        this.now = DateUtil.formDailyTimestamp(ts);
//        log.warn("[new item update job]" + userId);
//    }

    private Long pageSize = PageSize.API_ITEM_PAGE_SIZE;

    boolean isFirst = false;

    public ItemUpdateJob(Long userId, Long ts, Long pageSize, boolean isFirst) {
        super(userId);
        this.now = DateUtil.formDailyTimestamp(ts);
        this.pageSize = pageSize;
        this.isFirst = isFirst;
        log.warn("[new item update job]" + userId);
    }

    public ItemUpdateJob(Long userId, Long ts, Long pageSize) {
        super(userId);
        this.now = DateUtil.formDailyTimestamp(ts);
        this.pageSize = pageSize;
        log.warn("[new item update job]" + userId);
    }

    @Override
    protected boolean prepare() {

        now = System.currentTimeMillis();
        maxUpdateTs = getMaxUserUpdateVersion();
        if (maxUpdateTs == 0L) {
            this.isFirstUpdate = true;
        }
        log.info("Current Max Info:" + DateUtil.formDateForLog(maxUpdateTs));

        if (now - maxUpdateTs < getInterval()) {
            log.info("[No Need To Update for]" + user.getId());
            return false;
        }

        start = maxUpdateTs;
        end = now + getInterval();

        log.info("Item:Start-End[" + DateUtil.formDateForLog(start) + "," + DateUtil.formDateForLog(end) + "]");
        return true;
    }

    public void requestUpdate(long temp1, long temp2) {

        Thread.currentThread().setName(ItemUpdateJob.class.getName());

        for (CacheUserClearer clearer : needToClear) {
            clearer.clear(user);
        }

        if (Debug.SYNC_ALL_ITEM || isFirst) {
            start = 0L;
            end = System.currentTimeMillis();
        }
//        if (start <= 0L) {
//            end = end - (5 * DateUtil.DAY_MILLIS);
//        }

        log.error(String.format("ItemUpdate for %s, startTs[%s], endTs[%s] ", user.getId(),
                DateUtil.formDateForLog(start), DateUtil.formDateForLog(end)));

        new ItemApiDoing(user.getId(), now).publish();
//        doForFullItemUpdate(start, end);
////
//        if (start < 1L) {
//            doForFullItemUpdate(start, end);
//        } else if (TMConfigs.Server.ENABLE_JDP_PUSH_MODE) {
//
//            Boolean success = new ItemJdpSyncer().call();
//            if (success == null || !success) {
//                log.error("not success : for user item update:" + user);
//                return;
//            }
//
//        } else {
////            new ItemUpdateIncrementalSyncer().call();
//            new ItemAPIIncrementalSyncer().call();
//        }

        log.info(" raw mode:" + Rds.enableJdpPush);
        log.info(" raw has id :" + RawId.hasId(userId));

        if (start > 1L && ApiJdpAdapter.enableJdp(user)
                && APIConfig.get().getApp() != APIConfig.defender.getApp()) {
            log.error(">>>> Use jdp syncer...");
            Boolean success = new ItemJdpSyncer().call();
            if (success == null || !success) {
                log.error("not success : for user item update:" + user);
                return;
            }
        } else {
            log.error(">>>> Use full syncer...");
            doForFullItemUpdate(start, end);
        }

        afterFinished(user.getId(), end);
        // new ItemApiDoneDBDoing(user.getId(), now).publish();
        // ItemWritter.addFinishedMarkMsg(user.getId(), now);

        //先更新完item，然后更新popularized推广
        UpdatePopularizedJob updatePopularizedJob = new UpdatePopularizedJob(user);
        updatePopularizedJob.doJob();

        log.warn(" start auto delist :" + user);

        DelistUpdateAction.checkDefaultDelistPlan(user);

        //if (user.isAutoDelistOn()) {

        //更新一个用户的上下架
        //AutoListRecord record = AutoListRecordDao.findAutoListRecordByUserId(user.getId());
        //RefreshOneUserAutoList update = new RefreshOneUserAutoList(record);
        //update.doUpdate();

        DelistUpdateAction.doUpdateUserDelist(user);

        //}

        //更新直通车数据
        if (APIConfig.get().isNeedSyncUserSimba(user) == true) {

            new SyncSimbaJob(user).doJob();
        }

        log.warn(" item update job finishes:" + user);
    }

    private void doForIncrementalUpdate(long start, long end) {

    }

    private void doForFullItemUpdate(long start, long end) {
        List<Item> itemsGet = null;

        try {
            itemsGet = getItem(user, start, end);
            // log.warn("Items Onsale size : " + itemsGet.size());

            if (isFirst && TMConfigs.TITLE_BACKUP) {
                log.error("item update first title backup: " + user);
//                ItemTitleBackup.build(user, itemsGet);
            }

            writeToDB(user.getId(), now, itemsGet);

            // 同步宝贝后，检查该宝贝是否在TitleOptimised表中有对应记录
            //TitleOptimisedWritter.addMsg(user.getId(), itemsGet);
            // ItemWritter.addItemList(user.getId(), itemsGet);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    protected void writeToDB(Long userId, Long ts, List<Item> itemsList) {

        if (CommonUtils.isEmpty(itemsList)) {
            return;
        }

//        Set<Long> remoteNumIids = ItemDao.toIdsSet(itemsList);
        //ItemDao.deleteAll(userId, numIids);
        //List<ItemPlay> itemPlays = ItemDao.findByNumIidListAndUserId(userId,StringUtils.join(numIids,","));
        List<ItemPlay> itemPlays = ItemDao.findByUserId(userId);
        Map<Long, ItemPlay> inDBNumIidMap = new HashMap<Long, ItemPlay>();
        for (ItemPlay itemPlay : itemPlays) {
            inDBNumIidMap.put(itemPlay.getNumIid(), itemPlay);
        }

        log.info(">>>>>>>>>>>>>writeToDB map size in numIids ========== " + inDBNumIidMap.size());
        writeRemoteToLocal(inDBNumIidMap, itemsList, ts);
        
        if(APIConfig.get().getApp() == 21255586) {
	        List<ItemExtra> itemExtras = ItemExtra.findByUserId(userId);
	        Map<Long, ItemExtra> itemExtraNumIidMap = new HashMap<Long, ItemExtra>();
	        for (ItemExtra itemExtra : itemExtras) {
	        	itemExtraNumIidMap.put(itemExtra.getNumIid(), itemExtra);
	        }
	
	        log.info(">>>>>>>>>>>>>writeTo ItemErtra map size in numIids ========== " + itemExtraNumIidMap.size());
	        writeRemoteToItemExtra(itemExtraNumIidMap, itemsList, ts);
        }
        UserHasTradeItemCache.clear(user);
    }

    public void writeRemoteToLocal(Map<Long, ItemPlay> inDBItemMap, List<Item> remoteItemsList, Long ts) {
        Set<Long> toDeleteItems = new HashSet<Long>();
//        Set<Long> modifiedItems = new HashSet<Long>();
        List<Item> toInsertItems = new ArrayList<Item>();

        if (CommonUtils.isEmpty(remoteItemsList)) {
            toDeleteItems.addAll(inDBItemMap.keySet());
        }

        for (Item item : remoteItemsList) {
            Long numIid = item.getNumIid();
            ItemPlay itemPlay = inDBItemMap.get(numIid);
            if (itemPlay == null) {
                // No Local item, must insert....
                toInsertItems.add(item);
                continue;
            }

            // Now, remote exists in db...
            inDBItemMap.remove(numIid);

            if (!itemPlay.isEqualToItem(item)) {
                // has modified...
            	if(item.getAfterSaleId() == null || item.getAfterSaleId() == 0) {
            		// 若因api调用受限导致recentSalesCount为空，则使用原先的数据
            		item.setAfterSaleId(Long.valueOf(itemPlay.getRecentSalesCount()));
            	}
            	if(item.getVolume() == null || item.getVolume() == 0) {
            		// 若因api调用受限导致salesCount为空，则使用原先的数据
            		item.setVolume(Long.valueOf(itemPlay.getSalesCount()));
            	}
                toDeleteItems.add(numIid);
                toInsertItems.add(item);
            } else {
                // no need to modify...
            }
        }

        // No match in the remote??? so we need to delete...
        toDeleteItems.addAll(inDBItemMap.keySet());

        log.info("to delete num:[" + toDeleteItems.size() + "] and to insert num:" + toInsertItems.size());
        ItemDao.deleteAll(userId, toDeleteItems);
        Map<Long, Item> toInsertMap = new HashMap<Long, Item>();
        for (Item item : toInsertItems) {
            toInsertMap.put(item.getNumIid(), item);
        }
        boolean insertSuccess = ItemDao.batchInsert(userId, ts, toInsertMap.values());
        if (!insertSuccess) {
            for (Item item : toInsertItems) {
                new ItemPlay(userId, item).jdbcSave();
            }
        }

        toDeleteItems.clear();
        toInsertItems.clear();
        
        if (APIConfig.get().isGetItemOutLinkFromDesc() == true) {
            OutLinksGetAction.doCheckItemOutLinks(user, remoteItemsList, true);
        }
    }
    
    public void writeRemoteToItemExtra(Map<Long, ItemExtra> inDBItemMap, List<Item> remoteItemsList, Long ts) {
        Set<Long> toDeleteItems = new HashSet<Long>();
        List<Item> toInsertItems = new ArrayList<Item>();

        if (CommonUtils.isEmpty(remoteItemsList)) {
            toDeleteItems.addAll(inDBItemMap.keySet());
        }

        for (Item item : remoteItemsList) {
            Long numIid = item.getNumIid();
            ItemExtra itemExtra = inDBItemMap.get(numIid);
            if (itemExtra == null) {
                // No Local itemExtra, must insert....
                toInsertItems.add(item);
                continue;
            }

            // Now, remote exists in db...
            inDBItemMap.remove(numIid);
        }

        // No match in the remote??? so we need to delete...
        toDeleteItems.addAll(inDBItemMap.keySet());

        log.info("for ItemExtra to delete num:[" + toDeleteItems.size() + "] and to insert num:" + toInsertItems.size());
        ItemExtra.deleteAll(userId, toDeleteItems);
        
        List<ItemExtra> itemExtraList = new ArrayList<ItemExtra>();
        Long insertTs = System.currentTimeMillis();
        for (Item item : toInsertItems) {
            ItemExtra itemExtra = new ItemExtra(item.getNumIid(), insertTs, item.getCreated().getTime(), user.getId());
            itemExtraList.add(itemExtra);
            // 每1024条数据保存一次
            if ((itemExtraList.size() & 1023) == 0) {
                // 保存数据
                ItemExtra.batchSave(itemExtraList, user.getId());
                itemExtraList.clear();
            }
        }
        ItemExtra.batchSave(itemExtraList, user.getId());
        
        toDeleteItems.clear();
        toInsertItems.clear();
    }

    protected void afterFinished(Long userId, Long ts) {

        log.error(" after finished ....:" + user);
        new ItemDBDone(userId, ts).publish();
        ItemUpdateTs.updateLastItemModifedTime(userId, ts);
        UserHasTradeItemCache.clear(user);
    }

    public List<Item> getItem(final User user, Long start, Long end) throws Exception {

        log.info(format("getItem:user, startModified, endModified".replaceAll(", ", "=%s, ") + "=%s", user, start, end));

        Set<Long> existIds = new HashSet<Long>();

        Long itemTotalNum = new ItemApi.ItemsOnsaleCount(user, null, null).call();
        Long itemInventory = new ItemsInventoryCount(user, null, null).call();

        /*
         *  no items inventory :{"error_response":{"sub_msg":"java.lang.RuntimeException:Exception@UnionQueryVSearchServer:: query1=user_id=761217329&amp;cf.range=quantity:[1 TO *]&amp;cf.not.bit=options:70368744177664&amp;cf.in=auction_status:(-2)&amp;q=user_id:761217329&amp;sort=starts desc,auction_id desc&amp;start=0&amp;rows=1&amp;src=top:sell180128.cm3,query2=cf.not.bit=options:70368744177664&amp;user_id=761217329&amp;cf.in=auction_status:(-5)&amp;q=user_id:761217329&amp;sort=starts desc,auction_id desc&amp;start=0&amp;rows=1&amp;src=top:sell180128.cm3,query3=user_id=761217329&amp;cf.range=starts:[1377521948810 TO *]&amp;cf.not.bit=options:70368744177664&amp;cf.in=auction_status:(0,1,-9)&amp;q=user_id:761217329&amp;sort=starts desc,auction_id desc&amp;start=0&amp;rows=1&amp;src=top:sell180128.cm3","code":530,"sub_code":"isp.item-instant-search-service-unavailable","msg":"Remote service error"}} 
         */
        if (itemTotalNum == null || itemTotalNum < 0L) {
            return null;
        }

        log.warn("[on total num] [" + itemTotalNum + "] item invenory: [" + itemInventory + "]");

        long totalOnSalePageCount = CommonUtils.calculatePageCount(itemTotalNum, this.pageSize);
        if (totalOnSalePageCount > MAX_ITEM_PAGE_NUM) {
            totalOnSalePageCount = MAX_ITEM_PAGE_NUM;
        }
        long totalInventoryPageCount = CommonUtils.calculatePageCount(itemInventory, this.pageSize);
        if (totalInventoryPageCount > MAX_ITEM_PAGE_NUM) {
            totalInventoryPageCount = MAX_ITEM_PAGE_NUM;
        }

        List<FutureTask<List<Item>>> onSalePromises = new ArrayList<FutureTask<List<Item>>>();
        List<FutureTask<List<Item>>> inventoryPromises = new ArrayList<FutureTask<List<Item>>>();

        final List<Item> onSaleItemList = new ArrayList<Item>();
        List<Item> inventoryItemList = new ArrayList<Item>();

        for (Long pageNo = 1L; pageNo < totalOnSalePageCount + 1; pageNo++) {
            ItemsOnsalePage api = new ItemApi.ItemsOnsalePage(user, null, null, pageNo, pageSize);
            FutureTask<List<Item>> promise = pool.submit(api);
            onSalePromises.add(promise);
        }
        for (Long pageNo = 1L; pageNo < totalInventoryPageCount + 1; pageNo++) {
            ItemsInventoryPage api = new ItemApi.ItemsInventoryPage(user, null, null, pageNo, pageSize);
            FutureTask<List<Item>> promise = pool.submit(api);
            inventoryPromises.add(promise);
        }
        
        

        Map<Long, Integer> recentNumIidSale = MapUtils.EMPTY_MAP;
        Map<Long, Integer> numIidSale = MapUtils.EMPTY_MAP;
        ShopBaseTradeInfo info = null;

        if ((TMConfigs.Sale.ENABLE_ITEM_SALE && TMConfigs.App.IS_TRADE_ALLOW)
                && APIConfig.get().getApp() != APIConfig.defender.getApp()) {
        	// 最近一天
        	info = TMTradeApi.buildNumIidSaleMap(user, 1);

            if (info != null) {
            	recentNumIidSale = info.getNumIidSales();
            }
            // 最近一个月
            info = TMTradeApi.buildNumIidSaleMap(user, 30);

            if (info != null) {
                numIidSale = info.getNumIidSales();
            }
        }

        log.info("[build salesCount map :]" + numIidSale);

        for (FutureTask<List<Item>> promise : inventoryPromises) {
            List<Item> itemGet = promise.get();

            if (!CommonUtils.isEmpty(itemGet)) {
                inventoryItemList.addAll(itemGet);
            } else {
                log.info("[no inventory:: : ]" + user);
            }
        }

        for (FutureTask<List<Item>> promise : onSalePromises) {
            List<Item> itemGet = promise.get();
            if (!CommonUtils.isEmpty(itemGet)) {
                onSaleItemList.addAll(itemGet);
            }
        }

        new ItemDeletedCheckJob(user, existIds, onSaleItemList).now();

        final ShopBaseTradeInfo tempInfo = info;
        if (APIConfig.get().isToBuildDiagInfo()) {
            if (isFirst) {
                new Job() {
                    public void doJob() {
                        UserDiag.build(user, onSaleItemList);
                        if (APIConfig.get().isItemScoreRelated() && tempInfo != null) {
                            DiagAction.buildUserShopDiag(user, tempInfo, tempInfo.buildItemThumbs(onSaleItemList));
                        }
                    }
                }.now();
            } else {
                new UserDiag.ComputeUserDelistTime(user).now();
            }
        }

        if (!Rds.enableJdpPush) {
            log.error(" to delete :" + APIConfig.get().deleteAllItems());
            if (APIConfig.get().deleteAllItems()) {
                ItemDao.deleteAll(user.getId());
            }
        }

        if (Sale.ENABLE_ITEM_SALE) {
        	if(!CommonUtils.isEmpty(recentNumIidSale)) {
        		for (Item item : onSaleItemList) {
        			Integer sale = recentNumIidSale.get(item.getNumIid());
        			if (sale == null) {
        				continue;
        			}
        			// 最近一天销量 暂存于该字段
        			item.setAfterSaleId(new Long(sale));
        		}
        	}
        	
        	if (!CommonUtils.isEmpty(numIidSale)) {
        		for (Item item : onSaleItemList) {
        			Integer sale = numIidSale.get(item.getNumIid());
        			if (sale == null) {
        				continue;
        			}
        			item.setVolume(new Long(sale));
        		}
        	}
        }

        if (APIConfig.get().isItemScoreRelated()) {
            buildItemScores(onSaleItemList, inventoryItemList);
        }
//        onSaleItemList, inventoryItemList
//        for (Item item : onSaleItemList) {
//            log.warn("item score:" + item.getScore());
//        }

        pool.submit(new RemoteItemLeftCaller(user, existIds, onSaleItemList, inventoryItemList));
        APIConfig.get().afterItemUpdateJob(user, onSaleItemList, inventoryItemList);
//        writeInventory(inventoryItemList, existIds);

        log.info("[inventory :]" + inventoryItemList.size());
        onSaleItemList.addAll(inventoryItemList);
        log.error("[to write num:]" + onSaleItemList.size());

        fetchMoreFieldsForItem(user, onSaleItemList);
        return onSaleItemList;
    }
    
    
    private static void fetchMoreFieldsForItem(final User user, List<Item> tbItemList) {
        
        try {
        	if (APIConfig.get().isNeedMoreFieldsWhenSyncItem() == false) {
                return;
            }
            
            if (CommonUtils.isEmpty(tbItemList)) {
                return;
            }
            
            log.warn("fetch more fields for " + tbItemList.size() + " items, user: " 
                    + user.getUserNick() + "-----");
            
            final Set<Long> numIidSet = new HashSet<Long>();
            for (Item item : tbItemList) {
                if (item == null) {
                    continue;
                }
                numIidSet.add(item.getNumIid());
            }
            
            final int PageSize = MultiItemsListGet.MAX_NUMIID_LENGTH;
            List<List<Long>> splitNumIidsList = SplitUtils.splitToSubLongList(numIidSet, PageSize);
            
            if (CommonUtils.isEmpty(splitNumIidsList)) {
                return;
            }
            
            String itemField = APIConfig.get().getItemFieldsWhenSyncItem();
            if (StringUtils.isEmpty(itemField)) {
                itemField = ItemApi.FIELDS;
            }
            
            List<FutureTask<List<Item>>> promises = new ArrayList<FutureTask<List<Item>>>();
            for (List<Long> splitNumIids : splitNumIidsList) {
                if (CommonUtils.isEmpty(splitNumIids)) {
                    continue;
                }
                MultiItemsListGet itemGetApi = new MultiItemsListGet(user.getSessionKey(), 
                        splitNumIids, itemField);
                promises.add(pool.submit(itemGetApi));
            }

            Map<Long, Item> fetchedItemMap = new HashMap<Long, Item>();

            for (FutureTask<List<Item>> promise : promises) {

                try {
                    List<Item> tempList = promise.get();
                    if (CommonUtils.isEmpty(tempList)) {
                        continue;
                    } else {
                        for (Item tempTbItem : tempList) {
                            if (tempTbItem == null) {
                                continue;
                            }
                            fetchedItemMap.put(tempTbItem.getNumIid(), tempTbItem);
                        }
                    }
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                } catch (ExecutionException e) {
                    log.error(e.getMessage(), e);
                }
            }

            for (Item tbItem : tbItemList) {
                if (tbItem == null) {
                    continue;
                }
                Item fetchedItem = fetchedItemMap.get(tbItem.getNumIid());
                if (fetchedItem == null) {
                    log.error("something is wrong, cannot get item for numIid: " + tbItem.getNumIid() 
                            + ", user: " + user.getUserNick());
                    continue;
                }
                
                tbItem.setIsFenxiao(fetchedItem.getIsFenxiao());
                tbItem.setDesc(fetchedItem.getDesc());
                tbItem.setCreated(fetchedItem.getCreated());
            }
            
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        
    }
    
    
    

    private void buildItemScores(final List<Item> onSaleItemList, List<Item> inventoryItemList) {
        List<Item> allRawItems = new ArrayList<Item>();
        allRawItems.addAll(inventoryItemList);
        allRawItems.addAll(onSaleItemList);
        buildItemScores(allRawItems);
    }

    private void buildItemScores(final List<Item> allRawItems) {
        log.error("build item scores :: " + user);
        Map<Long, Item> toGetScoreItems = new HashMap<Long, Item>();

        Map<Long, NumIidScoreModifed> itemScoreModified = ItemDao.findNumIidScoreModified(user.getId());
        for (Item item : allRawItems) {
            Long numIid = item.getNumIid();
            NumIidScoreModifed exist = itemScoreModified.get(numIid);
            if (exist == null) {
                toGetScoreItems.put(numIid, item);
            } else {
                long localModified = exist.getModified();
                Date currModify = item.getModified();
//                log.error(" compared time :" + new Date(localModified) + " remote :" + currModify);
                if (currModify == null || currModify.getTime() > localModified && exist.getScore() > 0) {
                    toGetScoreItems.put(numIid, item);
                } else {
                    item.setScore(new Long(exist.getScore()));
                }
//                toGetScoreItems.put(numIid, item);
            }
        }

        ItemApi.setItemScore(user, toGetScoreItems.values());

        diagItemScoreForItemList(toGetScoreItems);
    }

    private void diagItemScoreForItemList(Map<Long, Item> toGetScoreItems) {
        List<FutureTask<List<Item>>> tasks = new ArrayList<FutureTask<List<Item>>>();
        for (final Item item : toGetScoreItems.values()) {
            tasks.add(pool.submit(new Callable<List<Item>>() {
                @Override
                public List<Item> call() throws Exception {
                    Long volume = item.getVolume();
                    int sale = volume == null ? -1 : volume.intValue();
                    DiagResult diag = DiagAction.doDiag(user, item, item.getTitle(), sale);
                    if (diag == null) {
                        return null;
                    }
                    item.setScore(new Long(diag.getScore()));
                    return null;
                }
            }));
        }

        for (FutureTask<List<Item>> futureTask : tasks) {
            try {
                futureTask.get();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    private void writeInventory(List<Item> list, Set<Long> existIds) {
        if (CommonUtils.isEmpty(list)) {
            return;
        }

        log.warn("Inventory size :" + CollectionUtils.size(list) + " with exist id size:" + existIds.size()
                + " for user:" + user.getId());

        List<Long> toBeInstockItems = new ArrayList<Long>();
        List<Item> needToWriteList = new ArrayList<Item>();

        for (Item item : list) {
            if (existIds.contains(item.getNumIid())) {
                toBeInstockItems.add(item.getNumIid());
            } else {
                needToWriteList.add(item);
            }
        }

        ItemDao.updateInventory(user.getId(), toBeInstockItems);
        writeToDB(userId, now, needToWriteList);
    }

    private void setItemTradeCount(Collection<Item> resList, Map<Long, Integer> itemsSaleCount) {
        if (CommonUtils.isEmpty(itemsSaleCount) || CommonUtils
                .isEmpty(resList)) {
            // This means something error happens....
            return;
        }

        for (Item item : resList) {
            Integer count = itemsSaleCount.get(item.getNumIid());
            if (count == null) {
                item.setVolume(NumberUtil.DEFAULT_LONG);
            } else {
                item.setVolume(Long.valueOf(count.longValue()));
            }
        }
    }

    private void setJdpItemTradeCount(Collection<JdpItemModel> resList, Map<Long, Integer> itemsSaleCount) {
        if (CommonUtils.isEmpty(itemsSaleCount) || CommonUtils
                .isEmpty(resList)) {
            // This means something error happens....
            return;
        }

        for (JdpItemModel model : resList) {
            Integer count = itemsSaleCount.get(model.getNumIid());
            if (count == null) {
                model.getItem().setVolume(NumberUtil.DEFAULT_LONG);
            } else {
                model.getItem().setVolume(Long.valueOf(count.longValue()));
            }
        }
    }

    public boolean checkTask(Long userId, Long taskTs) {

        ItemDailyUpdateTask task = ItemDailyUpdateTask.findByUserIdAndTs(userId, taskTs);

        if (task != null && (System.currentTimeMillis() - task.getUpdateAt() < ExpiredTime.TASK_EXPIRE_TIME)) {
            return false;
        }

        return true;
    }

    @Override
    public long getMaxUserUpdateVersion() {
        ItemUpdateTs itemTs = ItemUpdateTs.fetchByUser(user);
        log.info("[Found Current Version]" + itemTs);
        return itemTs == null ? 0L : itemTs.getLastUpdateTime();
    }

    protected long getInterval() {
        return 5000L;
    }

    public static class RemoteItemLeftCaller implements Callable<List<Item>> {

        Set<Long> tbItemIds = null;

        Set<Long> inDbItems = null;

        User user = null;

        public RemoteItemLeftCaller(User user, Set<Long> inDbItems, List<Item> onsaleItems, List<Item> invetoryItems) {

            this.user = user;
            this.inDbItems = inDbItems;
            this.tbItemIds = new HashSet<Long>();

            if (!CommonUtils.isEmpty(onsaleItems)) {
                for (Item item : onsaleItems) {
                    tbItemIds.add(item.getNumIid());
                }
            }

            if (!CommonUtils.isEmpty(invetoryItems)) {
                for (Item item : invetoryItems) {
                    tbItemIds.add(item.getNumIid());
                }
            }

        }

        @Override
        public List<Item> call() {
            if (CommonUtils.isEmpty(inDbItems)) {
                return null;
            }

            List<Long> toDeleteIds = new ArrayList<Long>();

            Iterator<Long> it = inDbItems.iterator();
            while (it.hasNext()) {
                Long id = it.next();
                if (tbItemIds.contains(id)) {
                    continue;
                }
                toDeleteIds.add(id);
            }

            for (Long toDeleteId : toDeleteIds) {
//                new ItemApi.ItemGet(user, toDeleteId, false).call();
                ApiJdpAdapter.tryFetchSingleItem(user, toDeleteId);
            }

            return null;
        }
    }

    public class ItemJdpSyncer implements Callable<Boolean> {

        @Override
        public Boolean call() {
            Thread.currentThread().setName(this.getClass().getName());
            // 由于有可能会有部分信息遗漏，开始时间需要往前修正一下
            long itemStart = start - DateUtil.TEN_MINUTE_MILLIS;
            long tradeStart = start - DateUtil.ONE_HOUR;
            long refixEnd = end + DateUtil.ONE_MINUTE_MILLIS;

            try {

                Map<Long, JdpItemModel> itemMap = JdpItemModel
                        .queryAllJdpItems(user.getUserNick(), itemStart, refixEnd);
                Set<Long> ids = new HashSet<Long>();
                for (JdpItemModel model : itemMap.values()) {
                    ids.add(model.getNumIid());
                }

                log.info("[found mofifled items with in]" + DateUtil.formDateForLog(itemStart) + "--"
                        + DateUtil.formDateForLog(refixEnd) + "\n for" + StringUtils.join(ids, ','));
                Long apiOnSaleCount = new ItemApi.ItemsOnsaleCount(user, itemStart, refixEnd).call();
                Long apiInventoryCount = new ItemApi.ItemsInventoryCount(user, itemStart, refixEnd).call();

                log.error(" while api returns api on sale count:[" + apiOnSaleCount + "] inventory count:["
                        + apiInventoryCount + "]");

                Map<Long, Trade> tradeMap = JdpTradeModel.queryModifiedTrades(user.getUserNick(), tradeStart, refixEnd);
                log.info("[query trade map :]" + tradeMap.size());
                TradeWritter.batchWriteTrades(userId, end, tradeMap.values());
                Map<Long, Integer> numIidSale = OrderDisplayDao.findUserRecentTrade(userId);
                setJdpItemTradeCount(itemMap.values(), numIidSale);

                Collection<JdpItemModel> models = itemMap.values();
                /**
                 * 先拿掉部分已经删除的宝贝
                 */
                Set<Long> hasRemovedIds = new HashSet<Long>();
                for (JdpItemModel itemModels : models) {
                    if (itemModels.isDeleted()) {
                        hasRemovedIds.add(itemModels.getNumIid());
                    }
                }

                for (Long id : hasRemovedIds) {
                    itemMap.remove(id);
                }

                ItemDao.deleteAll(userId, hasRemovedIds);
                ItemDao.updateItemSale(userId);
                for (JdpItemModel jdpItemModel : itemMap.values()) {
                    new ItemPlay(userId, jdpItemModel.getItem()).jdbcSave();
                }

                UserHasTradeItemCache.clear(user);
                if (user.isShowWindowOn()) {
                    boolean recentDownItemMatch = JdpItemStatus.isRecentDownItemMatch(user);
                    if (!recentDownItemMatch) {
                        doForFullItemUpdate(0L, 0L);
                    }
                } else {
                    boolean isItemOnSaleMatch = JDPApi.get().isOnSaleItemDBAPIMatch(user);
                    if (!isItemOnSaleMatch) {
                        doForFullItemUpdate(0L, 0L);
                    }
                }

                JdpItemStatus iStatus = new JdpItemStatus(user);
                log.warn(iStatus.toStrBuilder().toString());

                return Boolean.TRUE;
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
            return Boolean.FALSE;
        }
    }

    public class ItemAPIIncrementalSyncer implements Callable<Void> {
        List<FutureTask<List<Item>>> onSalePromises = new ArrayList<FutureTask<List<Item>>>();

        List<FutureTask<List<Item>>> inventoryPromises = new ArrayList<FutureTask<List<Item>>>();

        final List<Item> onSaleItemList = new ArrayList<Item>();

        final List<Item> inventoryItemList = new ArrayList<Item>();

        Map<Long, Item> modifiedItemMap = new HashMap<Long, Item>();

        @Override
        public Void call() {
            Thread.currentThread().setName(ItemAPIIncrementalSyncer.class.getName());

            try {
                if (start == 0L) {
                    // TODO 用全量模型
                    log.info("[ use full update mode for the user]:" + user);
                    doForFullItemUpdate(start, end);
                    return null;
                }

                prepareFutureForTrade();
                prepareFutureForCall();

                // TODO prepare for the trade sync api
                fetchItems();
                fetchTrades();

                ItemDao.updateItemSale(userId);

                for (Item item : onSaleItemList) {
                    modifiedItemMap.put(item.getNumIid(), item);
                }
                for (Item item : inventoryItemList) {
                    modifiedItemMap.put(item.getNumIid(), item);
                }

//                ShopBaseTradeInfo shopbaseBean = fetchDBShopBaseBean();
                Map<Long, Integer> numIidSale = OrderDisplayDao.findUserRecentTrade(userId);
                setItemTradeCount(modifiedItemMap.values(), numIidSale);

                // TODO Assign trade sale info
                preparedForModifiedItemScore(modifiedItemMap.values());
                fetchModifedItemScore();
                writeBack();

//                writeSale(numIidSaleCache);

            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }

            return null;
        }

        private void writeBack() {
            for (Item item : modifiedItemMap.values()) {
                new ItemPlay(userId, item).jdbcSave();
            }

            List<NotifyItem> notifyItems = new ItemDeleteIncrementGet(user, start - DateUtil.ONE_HOUR, end).call();
            log.info("[notify item:]" + notifyItems);

            if (!CommonUtils.isEmpty(notifyItems)) {
                List<Long> ids = new ArrayList<Long>();
                for (NotifyItem notifyItem : notifyItems) {
                    ids.add(notifyItem.getNumIid());
                }
                log.info("[item update job try delete itemds :]" + ids);
                ItemDao.deleteAll(userId, ids);
            }
        }

        List<FutureTask<List<Trade>>> tradeTasks = new ArrayList<FutureTask<List<Trade>>>();

        List<Trade> recentTrades = new ArrayList<Trade>();

        private void fetchTrades() throws InterruptedException, ExecutionException {
            // TODO Auto-generated method stub
            // Set the modified item map with prop volumn to be sale count...
            if (!APIConfig.get().isSimpleTradeToLocal()) {
                return;
            }

            if (tradeTasks == null) {
                log.error(" no trade task waiting.......");
                return;
            }

            for (FutureTask<List<Trade>> task : tradeTasks) {
                List<Trade> list = task.get();
                if (!CommonUtils.isEmpty(list)) {
                    recentTrades.addAll(list);
                }
            }

            TradeWritter.writeIncrementalTrades(userId, System.currentTimeMillis(), recentTrades);

        }

        private void prepareFutureForTrade() {
            // TODO Auto-generated method stub
            if (!APIConfig.get().isSimpleTradeToLocal()) {
                return;
            }

            for (long tempStart = start; tempStart < end; tempStart += DateUtil.DAY_MILLIS) {
                long tempEnd = tempStart + DateUtil.DAY_MILLIS - DateUtil.ONE_MINUTE_MILLIS;
                if (tempEnd > end) {
                    tempEnd = end;
                }
                TradesSoldIncrementextends api = new TradesSoldIncrementextends(user, tempStart, new Date(tempStart),
                        new Date(tempEnd), false);
                // 御城河日志接入
                SimulateRequestUtil.sendTopLog(SimulateRequestUtil.TRADES_SOLD_INCREMENT_GET);
                tradeTasks.add(TMConfigs.getTradePool().submit(api));
            }

        }

        private void fetchItems() throws InterruptedException, ExecutionException {
            for (FutureTask<List<Item>> promise : inventoryPromises) {
                List<Item> itemGet = promise.get();

                if (!CommonUtils.isEmpty(itemGet)) {
                    inventoryItemList.addAll(itemGet);
                } else {
                    log.info("[no inventory:: : ]" + user);
                }
            }

            for (FutureTask<List<Item>> promise : onSalePromises) {
                List<Item> itemGet = promise.get();
//                log.info("[collection size : ]" + itemGet.size());
                if (!CommonUtils.isEmpty(itemGet)) {
                    onSaleItemList.addAll(itemGet);

                }
            }
        }

        private void prepareFutureForCall() {
            Long itemOnSaleNum = new ItemApi.ItemsOnsaleCount(user, start, end).call();
            itemOnSaleNum = (itemOnSaleNum == null) ? 0L : itemOnSaleNum;
            Long itemInventoryNum = new ItemsInventoryCount(user, start, end).call();
            itemInventoryNum = (itemInventoryNum == null) ? 0L : itemInventoryNum;

            long totalOnSalePageCount = CommonUtils.calculatePageCount(itemOnSaleNum, pageSize);
//            log.warn("total item count :" + itemTotalNum + "  total page count :" + totalOnSalePageCount);
            if (totalOnSalePageCount > MAX_ITEM_PAGE_NUM) {
                totalOnSalePageCount = MAX_ITEM_PAGE_NUM;
            }
            long totalInventoryPageCount = CommonUtils.calculatePageCount(itemInventoryNum, pageSize);
            if (totalInventoryPageCount > MAX_ITEM_PAGE_NUM) {
                totalInventoryPageCount = MAX_ITEM_PAGE_NUM;
            }

            for (Long pageNo = 1L; pageNo < totalOnSalePageCount + 1; pageNo++) {
//                ItemsOnsalePage api = new ItemApi.ItemsOnsalePage(user, start, end, pageNo, pageSize);
                ItemsOnsalePage api = new ItemApi.ItemsOnsalePage(user, start, end, pageNo, pageSize);
                FutureTask<List<Item>> promise = pool.submit(api);
                onSalePromises.add(promise);
            }
            for (Long pageNo = 1L; pageNo < totalInventoryPageCount + 1; pageNo++) {

//                ItemsInventoryPage api = new ItemApi.ItemsInventoryPage(user, start, end, pageNo, pageSize);
                ItemsInventoryPage api = new ItemApi.ItemsInventoryPage(user, start, end, pageNo, pageSize);
                FutureTask<List<Item>> promise = pool.submit(api);
                inventoryPromises.add(promise);
            }
        }

    }

    List<FutureTask<List<Item>>> itemScoreTasks = new ArrayList<FutureTask<List<Item>>>();

    protected void preparedForModifiedItemScore(Collection<Item> items) {
        // TODO Auto-generated method stub
        for (final Item item : items) {
            itemScoreTasks.add(pool.submit(new Callable<List<Item>>() {
                @Override
                public List<Item> call() throws Exception {
                    int score = DiagAction.doDiag(user, item, item.getTitle(), -1).getScore();
                    item.setScore(new Long(score));
                    return null;
                }
            }));
        }
    }

    protected void fetchModifedItemScore() {

        for (FutureTask<List<Item>> futureTask : itemScoreTasks) {
            try {
                futureTask.get();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }

    }

}
