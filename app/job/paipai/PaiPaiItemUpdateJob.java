/**
 * 
 */

package job.paipai;

import static java.lang.String.format;

import java.text.SimpleDateFormat;
import java.util.List;

import job.writter.PaiPaiItemWritter;
import job.writter.TradeWritter;
import message.itemupdate.ItemApiDoing;
import message.itemupdate.ItemApiDoneDBDoing;
import message.tradeupdate.TradeApiDoing;
import message.tradeupdate.TradeApiDoneDBDoing;
import models.paipai.PaiPaiUser;
import models.ppmanage.PPStock;
import models.updatetimestamp.updates.ItemUpdateTs;
import models.updatetimestamp.updatestatus.ItemDailyUpdateTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import ppapi.PaiPaiItemApi;
import ppapi.PaiPaiItemApi.PPGetItemStock;
import ppapi.models.PaiPaiItem;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;

import configs.TMConfigs.Debug;
import configs.TMConfigs.ExpiredTime;

/**
 * @author navins
 * @date 2013-7-10 下午8:45:28
 */
public class PaiPaiItemUpdateJob extends Job {

    public final static Logger log = LoggerFactory.getLogger(PaiPaiItemUpdateJob.class);

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public long userId;

    public PaiPaiUser user;

    protected long start = 0L;

    protected long end = 0L;

    protected long maxUpdateTs = 0L;

    protected long now = 0L;

    protected boolean isFirstUpdate = false;
    
    protected boolean onlyForSaleItem = false;

    public PaiPaiItemUpdateJob(long uin) {
        this.userId = uin;
        this.start = 0L;
        this.now = System.currentTimeMillis();
        this.end = now;
    }
    
    public PaiPaiItemUpdateJob(long uin, boolean isFirstUpdate) {
        this.userId = uin;
        this.isFirstUpdate = isFirstUpdate;
        this.start = 0L;
        this.now = System.currentTimeMillis();
        this.end = now;
    }
    
    public PaiPaiItemUpdateJob(long uin, boolean isFirstUpdate, boolean onlyForSaleItem) {
        this(uin, isFirstUpdate);
        this.onlyForSaleItem = onlyForSaleItem;
    }

    public PaiPaiItemUpdateJob(long uin, long start, long end) {
        this.user = PaiPaiUser.findByUserId(uin);
        this.start = start;
        this.end = end;
        this.now = System.currentTimeMillis();
    }

    @Override
    public void doJob() {
        
        try {
            user = PaiPaiUser.findByUserId(userId);
            if (user == null) {
                log.warn("No PaiPai User Id with:" + userId);
                return;
            }
            boolean flag = prepareUpdate();
            if (flag == false) {
                return;
            }

//        requestUpdate();
            requestUpdateOnce();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);

        }
    }

    public boolean prepareUpdate() {
        ItemUpdateTs itemUpdateTs = ItemUpdateTs.fetchByUser(userId);

        if (itemUpdateTs == null) {
            log.info("No Paipai update record: " + userId);
            return true;
        }

        maxUpdateTs = itemUpdateTs.getLastUpdateTime();

        if (maxUpdateTs < DateUtil.formDailyTimestamp(user.getFirstLoginTime())) {
            this.isFirstUpdate = true;
        }

        if (now - maxUpdateTs < DateUtil.TEN_MINUTE_MILLIS) {
            log.info("[No Need To Update for]" + user.getId());
            return false;
        }

        start = maxUpdateTs;
        end = now;
        
        if (Debug.SYNC_ALL_ITEM || isFirstUpdate) {
            // 更新所有
            start = 0L;
        }
        return true;
    }

    public void requestUpdateOnce() {
        log.warn(String.format("PaiPai ItemUpdate for %s, startTs %s, endTs %s ", user.getId(), start, end));

        new ItemApiDoing(userId, now).publish();

        List<PaiPaiItem> tradeList = getPaiPaiItems(start, end);
        
        if (!CommonUtils.isEmpty(tradeList)) {
            PaiPaiItemWritter.addTradeList(userId, end, tradeList);
            for(PaiPaiItem item :tradeList){
            	SavePaiPaiStocks(item.getItemCode());
            }
        }

        new ItemApiDoneDBDoing(userId, end).publish();
        
        ItemUpdateTs.updateLastItemModifedTime(userId, end);
        
        PaiPaiItemWritter.addFinishedMarkMsg(user.getId(), end);
    }

    public void requestUpdate() {

        log.warn(format("Trade Update Job:start, end".replaceAll(", ", "=%s, ") + "=%s", start, end));

        for (long tempStart = start; tempStart < end; tempStart += getInterval()) {

            long tempEndDay = tempStart + getInterval();

            if (!checkTask(user.getId(), tempEndDay)) {
                log.warn(String.format("PaiPai TradeUpdate for %s, startTs %s, endTs %s is already doing!!!",
                        user.getId(), tempStart, tempEndDay));
                continue;
            }

            log.warn(String.format("PaiPai TradeUpdate for %s, startTs %s, endTs %s ", user.getId(), tempStart,
                    tempEndDay));

            new TradeApiDoing(userId, tempEndDay).publish();

            TradeWritter.addFinishedMarkMsg(user.getId(), tempEndDay);

            List<PaiPaiItem> tradeList = getPaiPaiItems(tempStart, tempEndDay);

            if (!CommonUtils.isEmpty(tradeList)) {
                PaiPaiItemWritter.addTradeList(userId, tempEndDay, tradeList);
            }

            new TradeApiDoneDBDoing(userId, tempEndDay).publish();

            PaiPaiItemWritter.addFinishedMarkMsg(user.getId(), tempEndDay);
        }

    }

    private List<PaiPaiItem> getPaiPaiItems(long tempStart, long tempEndDay) {
        List<PaiPaiItem> list = new PaiPaiItemApi.PaiPaiItemListApi(user, tempStart, tempEndDay, onlyForSaleItem).call();
        return list;
    }
    
    private void SavePaiPaiStocks(String itemCode){
    	List<PPStock> stockListList=new PPGetItemStock(user,itemCode).call();
    	if(CommonUtils.isEmpty(stockListList)){
    		return ;
    	}
    	for(PPStock stock : stockListList){
    		stock.jdbcSave();
    	}
    }

    public long getInterval() {
        return DateUtil.DAY_MILLIS;
    }
    
    public boolean checkTask(Long userId, Long taskTs) {

        ItemDailyUpdateTask task = ItemDailyUpdateTask.findByUserIdAndTs(userId, taskTs);

        if (task != null && (System.currentTimeMillis() - task.getUpdateAt() < ExpiredTime.TASK_EXPIRE_TIME)) {
            return false;
        }

        return true;
    }

}
