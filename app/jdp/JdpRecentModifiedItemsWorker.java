
package jdp;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import jdp.JdpModel.JdpItemModel;
import job.message.AddItemJob;
import job.message.DeleteItemJob;
import models.item.ItemPlay;
import models.updatetimestamp.updates.WorkTagUpdateTs;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.cache.Cache;
import play.jobs.Every;
import play.jobs.Job;
import bustbapi.ShowWindowApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.taobao.api.ApiException;
import com.taobao.api.domain.Item;

import configs.TMConfigs;
import configs.TMConfigs.Rds;
import dao.UserDao;
import dao.item.ItemDao;

//@Every("90s")
//@OnApplicationStart(async = true)
public class JdpRecentModifiedItemsWorker extends Job {

    @Every("20s")
    public static class JdpRecentModifiedItemsTimer extends Job {

        public void doJob() {
            if (!TMConfigs.Server.jobTimerEnable) {
                return;
            }
//            TMConfigs.getShowwindowPool().submit(new Callable<ItemPlay>() {
//                @Override
//                public ItemPlay call() throws Exception {

            if (!Rds.enableJdpApi) {
                log.warn("no enable jdp api....");
                return;
            }
            if (Play.mode.isDev()) {
                return;
            }
            if (!TMConfigs.Server.jobTimerEnable) {
                return;
            }
            new JdpRecentModifiedItemsWorker().doJob();
            return;
//                }
//            });
        }
    }

    private static final Logger log = LoggerFactory.getLogger(JdpRecentModifiedItemsWorker.class);

    public static final String TAG_RECENT_DONE = "JdpRecentDone_";

    public static String TAG = "jdp_recent_item";

    public void doClear() {
        this.start = 0L;
        this.end = 0L;
        this.updateNum = 0;
        this.deleteNum = 0;
        this.newItemNum = 0;
        this.timingItemNum = 0;
        this.localAlreadyExistNum = 0;
        this.localNotExistNum = 0;
        this.hashKeyItems.clear();

    }

    int fetchItemNum = 0;

    int deleteNum = 0;

    int newItemNum = 0;

    int updateNum = 0;

    int timingItemNum = 0;

    int localNotExistNum = 0;

    int localAlreadyExistNum = 0;

    long startMillis = 0L;

    long endMillis = 0L;

    static int hashKeyNum = 16;

    Map<Integer, List<Item>> hashKeyItems = new HashMap<Integer, List<Item>>();

    boolean debug = false;

    public JdpRecentModifiedItemsWorker() {
        super();
    }

    List<JdpItemModel> models = ListUtils.EMPTY_LIST;

    long end = 0L;

    long start = 0L;

    public JdpRecentModifiedItemsWorker(long start, long end) {
        super();
        this.start = start;
        this.end = end;
        this.debug = true;

        log.info(format("JdpRecentModifiedItemsTimer:start, end".replaceAll(", ", "=%s, ") + "=%s", start, end));
    }

    public void prepare() {
        if (end > 0L && start > 0L) {
            return;
        }
        WorkTagUpdateTs workts = WorkTagUpdateTs.findOrCreate(TAG);
        end = System.currentTimeMillis() - 1000L;
        start = workts.getLastUpdateTime() - 2000L;
        long maxEnd = start + (DateUtil.ONE_HOUR);
        if (end > maxEnd) {
            end = maxEnd;
        }
    }

    public void doJob() {

        this.doClear();
        Thread.currentThread().setName(TAG);
        prepare();

        try {
            long interval = 11000L;

            for (long tempEnd = end; tempEnd > start; tempEnd -= interval) {
                int limit = 2048;
                int offset = 0;
                long tempStart = tempEnd - interval;
                log.info("[temp start]" + DateUtil.formDateForLog(tempStart) + " -- end :"
                        + DateUtil.formDateForLog(tempEnd));
                if (tempStart < start) {
                    tempStart = start;
                }
                while (true) {
                    models = JdpItemModel.recentJdpModifiedItems(offset, limit, tempStart, tempEnd);
                    fetchItemNum = models.size();

                    for (JdpItemModel item : models) {
                        filterForJdpItemModel(item);
                    }

                    for (int i = 0; i < hashKeyNum; i++) {
                        final List<Item> list = hashKeyItems.get(i);
//                        log.info("[ has key num : " + i + "] with size: " + (list == null ? 0 : list.size()));
                        if (CommonUtils.isEmpty(list)) {
                            continue;
                        }
                        final int index = i;
                        TMConfigs.getShowwindowPool().submit(new Callable<ItemPlay>() {
                            @Override
                            public ItemPlay call() throws Exception {
                                checkSyncStatus(list, index);
                                return null;
                            }
                        });
                    }
//                    CommonUtils.sleepQuietly(1000L);

                    if (models.size() < limit) {
                        break;
                    } else {
                        offset += limit;
                    }

                }

                WorkTagUpdateTs.updateLastModifedTime(TAG, tempEnd);

            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        } finally {
        }

        log.error(" recent modified items res ;" + toString());
        this.doClear();
    }

    public void filterForJdpItemModel(JdpItemModel item) throws ApiException {
        Long numIid = item.getNumIid();
        String dateTag = item.getJdpModified().replaceAll(":", StringUtils.EMPTY).replaceAll(" ", StringUtils.EMPTY)
                .replaceAll("-", StringUtils.EMPTY);
        String key = TAG_RECENT_DONE + numIid + "_" + dateTag;

        if (Cache.get(key) != null) {
//            log.info(" item numiid recent jdp modified checked:" + key + " with  modified :" + item.getJdpModified());
            return;
        }
        Cache.set(key, Boolean.TRUE, "1h");

        User user = UserDao.findByUserNick(item.getNick());
//        log.info("[find user:]" + user + " with item numiid :" + numIid);

        if (user == null) {
            return;
        }

        if (item.isDeleted()) {
            // TODO  delete the item...
            deleteNum++;
            DeleteItemJob.addJdpTbItem(item);
            return;
        }

//        if (ItemDao.isRecentCreated(item.toTBItem())) {
//            // TODO, add the add item job...
//            AddItemJob.addJdpItem(item);
//            newItemNum++;
//            return;
//        }

        int currkey = user.getId().intValue() % hashKeyNum;
        List<Item> list = hashKeyItems.get(currkey);
        if (list == null) {
            list = new ArrayList<Item>();
            hashKeyItems.put(currkey, list);
        }
        Item tbItem = item.toTBItem();
        tbItem.setNick(user.getUserNick());
        list.add(tbItem);
    }

    // TODO merge to the hash item table to check the delist time...
    private static void checkSyncStatus(List<Item> modifiedItems, int hashKey) {

        // TODO For the delist and price time major..
        Set<Long> ids = ShowWindowApi.toNumIids(modifiedItems);
        Map<Long, ItemPlay> localItemMap = new HashMap<Long, ItemPlay>();
        List<ItemPlay> itemPlays = ItemDao.findbyNumIids(hashKey, ids);
        for (ItemPlay itemPlay : itemPlays) {
            localItemMap.put(itemPlay.getNumIid(), itemPlay);
        }

        for (Item rawItem : modifiedItems) {
            if (rawItem.getIsTiming() != null && rawItem.getIsTiming()) {
//                timingItemNum++;
                continue;
            }

            ItemPlay localItem = localItemMap.get(rawItem.getNumIid());
            if (localItem == null) {
//                log.warn(" no local item  for :" + new Gson().toJson(rawItem.getNumIid()) + " and current :"
//                        + localItem + " for created time :" + DateUtil.formDateForLog(rawItem.getCreated().getTime()));
//                localNotExistNum++;
//                newItemNum++;
                AddItemJob.addJdpItem(rawItem);
                continue;
            }

            boolean baseEqual = localItem.isBaseInfoEqual(rawItem);
//            if (!baseEqual) {
//                log.error(" base equal[" + rawItem.getNumIid() + "] for[" + baseEqual + "] rawitem "
//                        + new Gson().toJson(rawItem) + " with local :" + localItem);
//            }

            if (baseEqual) {
                continue;
            }

//            updateNum++;
            localItem.updateItemBaseInfo(rawItem);
            localItem.rawUpdate();
        }

    }

    @Override
    public String toString() {
        return "JdpRecentModifiedItemsTimer [fetchItemNum=" + fetchItemNum + ", deleteNum=" + deleteNum
                + ", newItemNum=" + newItemNum + ", updateNum=" + updateNum + ", timingItemNum=" + timingItemNum
                + ", localNotExistNum=" + localNotExistNum + ", localAlreadyExistNum=" + localAlreadyExistNum
                + ", end=" + DateUtil.formDateForLog(end) + ", start=" + DateUtil.formDateForLog(start) + "]";
    }

}
