
package job.diagjob;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import jdp.ApiJdpAdapter.OriginApiImpl;
import jdp.JdpModel.JdpItemModel;
import job.apiget.ItemUpdateJob;
import job.autolist.service.ItemService;
import job.autolist.service.ItemService.DelistOpStatus;
import models.oplog.TMUserWorkRecord.TMJdpFailWritter;
import models.user.User;

import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import titleDiag.DiagResult;
import bustbapi.ItemApi;
import bustbapi.ItemApi.ItemsInventoryCount;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.Item;

import configs.TMConfigs;

/**
 * 保证所有的宝贝的库存都是有更新的状态了
 * @author zrb
 */
public class PropDiagJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(PropDiagJob.class);

    public static final String TAG = "PropDiagJob";

    User user;

    static long pageSize = 25L;

    static String itemFields = "num_iid,title,pic_url,num";

    List<ItemPropDiagWrapper> res = new Vector<ItemPropDiagWrapper>();

    List<FutureTask<DiagResult>> promises = new ArrayList<FutureTask<DiagResult>>();

    List<FutureTask<DiagResult>> updatePromises = new Vector<FutureTask<DiagResult>>();

    boolean checkForTheJdp = true;

    boolean mustDo = false;

    Set<Long> targetIds = new HashSet<Long>();

    public PropDiagJob(User user) {
        super();
        this.user = user;
    }

    public PropDiagJob(User user, boolean checkForJdp) {
        super();
        this.user = user;
        this.checkForTheJdp = checkForJdp;
    }

    public PropDiagJob(User user2, boolean b, Set<Long> apiExtraItems) {
        super();
        this.user = user2;
        this.checkForTheJdp = b;
        this.targetIds = apiExtraItems;
    }

    public List<ItemPropDiagWrapper> doJobWithResult() {
        doJob();
        return res;
    }

    final Set<Long> successIds = new HashSet<Long>();

    final Set<Long> failIds = new HashSet<Long>();

    Set<Long> excludedNumIids = new HashSet<Long>();

    public void doJob() {

//        if (!checkForTheJdp && Rds.Enable_Jdp_Push) {
        if (checkForTheJdp) {
            excludedNumIids = JdpItemModel.allNumIids(user);
        }

        try {
            if (CommonUtils.isEmpty(targetIds)) {
                long maxItemNum = ItemUpdateJob.MAX_ITEM_PAGE_NUM;

                Long itemTotalNum = new ItemApi.ItemsOnsaleCount(user, null, null).call();
                long totalOnSalePageCount = CommonUtils.calculatePageCount(itemTotalNum, this.pageSize);
                if (totalOnSalePageCount > maxItemNum) {
                    totalOnSalePageCount = maxItemNum;
                }
                if(itemTotalNum > ItemUpdateJob.MAX_ITEM_PAGE_NUM){
                    itemTotalNum = ItemUpdateJob.MAX_ITEM_PAGE_NUM;
                }

                Long itemInventory = new ItemsInventoryCount(user, null, null).call();
                long totalInventoryPageCount = CommonUtils.calculatePageCount(itemInventory, this.pageSize);
                if (totalInventoryPageCount > maxItemNum) {
                    totalInventoryPageCount = maxItemNum;
                }
                if(itemInventory > ItemUpdateJob.MAX_ITEM_PAGE_NUM){
                    itemInventory = ItemUpdateJob.MAX_ITEM_PAGE_NUM;
                }

                for (long pageNo = 1L; pageNo < totalOnSalePageCount + 1; pageNo++) {
                    promises.add(TMConfigs.getDiagResultPool().submit(new Caller(pageNo, true)));
                }

                for (long pageNo = 1L; pageNo < totalInventoryPageCount + 1; pageNo++) {
                    promises.add(TMConfigs.getDiagResultPool().submit(new Caller(pageNo, false)));
                }

                for (FutureTask<DiagResult> futureTask : promises) {
                    futureTask.get();
                }
            } else {

                List<Item> items = OriginApiImpl.get().tryItemList(user, targetIds);
                doForItemList(items, SetUtils.EMPTY_SET);
            }

            for (FutureTask<DiagResult> futureTask : updatePromises) {
                futureTask.get();
            }

            log.info("[prop] user " + user);
            log.info("[prop] success set:" + StringUtils.join(successIds, ','));
            log.info("[prop] fail set:" + StringUtils.join(failIds, ','));

            TMJdpFailWritter.addMsg(TAG, user, failIds);

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    public class Caller implements Callable<DiagResult> {

        long pageNo;

        boolean isOnSale = true;

        public Caller(long pageNo) {
            super();
            this.pageNo = pageNo;
        }

        public Caller(long pageNo, boolean isOnSale) {
            super();
            this.pageNo = pageNo;
            this.isOnSale = isOnSale;
        }

        @Override
        public DiagResult call() throws Exception {

            List<Item> call = null;
            if (isOnSale) {
                call = new ItemApi.ItemsOnsalePage(user, pageNo, pageSize, itemFields).call();
            } else {
                call = new ItemApi.ItemsInventory(user, pageNo, pageSize).call();
            }

            if (CommonUtils.isEmpty(call)) {
                return null;
            }

            doForItemList(call, excludedNumIids);

            return null;
        }

    }

    public void doForItemList(List<Item> call, Set<Long> excludedNumIids) {
        for (final Item item : call) {
            if (item.getNum() == null || item.getNum() <= 0L) {
                continue;
            }

            if (excludedNumIids.contains(item.getNumIid())) {
                continue;
            }

            updatePromises.add(TMConfigs.getDiagResultPool().submit(new Callable<DiagResult>() {
                @Override
                public DiagResult call() throws Exception {
                    String errorMsg = doWithItem(item);
                    return null;
                }
            }));

            CommonUtils.sleepQuietly(200L);
        }
    }

    public String doWithItem(Item item) {
        Long numIid = item.getNumIid();
//        log.info("[num :]" + item.getNum());
        //ItemNumUpdater api = new ItemApi.ItemNumUpdater(user.getSessionKey(), item.getNumIid(), item.getNum());
        //Item resItem = api.call();
        
        DelistOpStatus updateStatus = ItemService.checkItemAttr(user, item);

        if (updateStatus.isSuccess() == true) {
            successIds.add(numIid);
            return null;
        }

        String errorMsg = updateStatus.getOpMsg();
        log.warn(" find error :" + errorMsg + " with item id :" + item.getNumIid());
        if (StringUtils.isEmpty(errorMsg)) {
            failIds.add(numIid);
            return errorMsg;
        }

//        if (errorMsg.indexOf("属性出错") >= 0) {
        ItemPropDiagWrapper wrapper = new ItemPropDiagWrapper(item.getNumIid(), item.getTitle(), item.getPicUrl(),
                errorMsg);
        res.add(wrapper);
//        }
        failIds.add(numIid);
        return errorMsg;
    }

    @JsonAutoDetect
    public static class ItemPropDiagWrapper implements Serializable {
        private static final long serialVersionUID = -1311305292342416427L;

        @JsonProperty
        Long numIid;

        @JsonProperty
        String title;

        @JsonProperty
        String picPath;

        @JsonProperty
        String msg;

        public ItemPropDiagWrapper(Long numIid, String title, String picPath, String msg) {
            super();
            this.numIid = numIid;
            this.title = title;
            this.picPath = picPath;
            this.msg = msg;
        }

        @Override
        public String toString() {
            return "ItemPropDiagWrapper [numIid=" + numIid + ", title=" + title + ", picPath=" + picPath + ", msg="
                    + msg + "]";
        }

    }
}
