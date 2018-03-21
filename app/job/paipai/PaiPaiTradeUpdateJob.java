/**
 * 
 */
package job.paipai;

import static java.lang.String.format;

import java.text.SimpleDateFormat;
import java.util.List;

import job.writter.PaiPaiTradeWritter;
import job.writter.TradeWritter;
import message.tradeupdate.TradeApiDoing;
import message.tradeupdate.TradeApiDoneDBDoing;
import models.paipai.PaiPaiUser;
import models.updatetimestamp.updates.ItemUpdateTs;
import models.updatetimestamp.updatestatus.TradeDailyUpdateTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import ppapi.PaiPaiItemApi.PaiPaiTradeListApi;
import ppapi.models.PaiPaiTradeDisplay;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;

import configs.TMConfigs.ExpiredTime;

/**
 * @author navins
 * @date 2013-7-10 下午8:45:28
 */
public class PaiPaiTradeUpdateJob extends Job {

    public final static Logger log = LoggerFactory.getLogger(PaiPaiTradeUpdateJob.class);

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public long userId;

    public PaiPaiUser user;

    protected long start = 0L;

    protected long end = 0L;

    protected long maxUpdateTs = 0L;

    protected long now = 0L;

    protected boolean isFirstUpdate = false;

    public PaiPaiTradeUpdateJob(long uin) {
        this.userId = uin;
        this.start = 0L;
        this.now = System.currentTimeMillis();
        this.end = now;
    }

    public PaiPaiTradeUpdateJob(long uin, long start, long end) {
        this.user = PaiPaiUser.findByUserId(uin);
        this.userId=uin;
        this.start = start;
        this.end = end;
        this.now = System.currentTimeMillis();
    }

    @Override
    public void doJob() throws Exception {

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
    }

    public boolean prepareUpdate() {
        ItemUpdateTs itemUpdateTs = ItemUpdateTs.fetchByUser(userId);

        if (itemUpdateTs == null) {
            log.info("No Paipai update record: " + userId);
            return true;
        }
        
        maxUpdateTs = itemUpdateTs.getLastUpdateTime();
        if (maxUpdateTs == 0) {
            maxUpdateTs = now - DateUtil.THIRTY_DAYS;
        }

        if (maxUpdateTs < DateUtil.formDailyTimestamp(user.getFirstLoginTime())) {
            this.isFirstUpdate = true;
        }

//        if (now - maxUpdateTs < DateUtil.DAY_MILLIS) {
//            log.info("[No Need To Update for]" + user.getId());
//            return false;
//        }

        //每天晚上同步一个星期的订单
        start = now-DateUtil.WEEK_MILLIS;
        end = now;  
        return true;
    }
    
    public void requestUpdateOnce() {
//        if (!checkTask(user.getId(), end)) {
//            log.warn(String.format("PaiPai TradeUpdate for %s, startTs %s, endTs %s is already doing!!!",
//                    user.getId(), start, end));
//            return;
//        }

        log.warn(String.format("PaiPai TradeUpdate for %s, startTs %s, endTs %s ", user.getId(), start,
                end));

        new TradeApiDoing(userId, now).publish();

        List<PaiPaiTradeDisplay> tradeList = getPaiPaiItems(start, end);

        if (!CommonUtils.isEmpty(tradeList)) {
            PaiPaiTradeWritter.addTradeList(userId, end, tradeList);
//            for(PaiPaiTradeDisplay trade :tradeList){
//            	SavePaiPaiTradeItem(user,trade.getDealCode());
//            }
        }

        new TradeApiDoneDBDoing(userId, end).publish();
        
        ItemUpdateTs.updateLastItemModifedTime(userId, end);

        PaiPaiTradeWritter.addFinishedMarkMsg(user.getId(), end);
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

            List<PaiPaiTradeDisplay> tradeList = getPaiPaiItems(tempStart, tempEndDay);

            if (!CommonUtils.isEmpty(tradeList)) {
                PaiPaiTradeWritter.addTradeList(userId, tempEndDay, tradeList);
            }

            new TradeApiDoneDBDoing(userId, tempEndDay).publish();

            PaiPaiTradeWritter.addFinishedMarkMsg(user.getId(), tempEndDay);
        }

    }

    private List<PaiPaiTradeDisplay> getPaiPaiItems(long tempStart, long tempEndDay) {
        List<PaiPaiTradeDisplay> list = new PaiPaiTradeListApi(user, tempStart, tempEndDay).call();
        return list;
    }

//    private void SavePaiPaiTradeItem(PaiPaiUser user,String dealCode){
//    	List<PaiPaiTradeItem> tradeItemList=new PPgetDealDetailApi(user, dealCode).call();
//    	if(CommonUtils.isEmpty(tradeItemList)){
//    		return;
//    	}
//    	for(PaiPaiTradeItem tradeItem:tradeItemList){
//    		tradeItem.jdbcSave();
//    	}
//    }
    
    public long getInterval() {
        return DateUtil.DAY_MILLIS;
    }

    public boolean checkTask(Long userId, Long taskTs) {

        TradeDailyUpdateTask task = TradeDailyUpdateTask.findByUserIdAndTs(userId, taskTs);

        if (task != null && (System.currentTimeMillis() - task.getUpdateAt() < ExpiredTime.TASK_EXPIRE_TIME)) {
            return false;
        }

        return true;
    }

}
