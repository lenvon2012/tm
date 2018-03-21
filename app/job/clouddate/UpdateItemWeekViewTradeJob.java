package job.clouddate;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.Callable;

import models.item.ItemPlay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import titleDiag.DiagResult;
import configs.TMConfigs;
import dao.item.ItemDao;

public class UpdateItemWeekViewTradeJob extends Job{

	static final Logger log = LoggerFactory.getLogger(UpdateItemWeekViewTradeJob.class);

	public static String TAG = "UpdateItemWeekViewTradeJob";
	
	public static int FromIndex = 0;
	
    public static SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyyMMdd");
    
    public void doJob() {
        
        Thread.currentThread().setName(TAG);
        
        if (!TMConfigs.Server.jobTimerEnable) {
            return;
        }

        new ItemDao.ItemBatchOper(16) {
            public List<ItemPlay> findNext() {
                return ItemDao.findValidList(offset, limit);
            }

            @Override
            public void doForEachItem(final ItemPlay item) {
                TMConfigs.getDiagResultPool().submit(new ItemCaller(item));
            }
        }.call();
    }
    
    public static class ItemCaller implements Callable<DiagResult> {
    	ItemPlay item;

        public ItemCaller(ItemPlay item) {
            super();
            this.item = item;
        }

        @Override
        public DiagResult call() {
            try {
                doWithItem(item);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
            return null;
        }
        
        public void doWithItem(final ItemPlay item) {
        	/*String startdate = sdf.format(new Date(System.currentTimeMillis() - 7 * DateUtil.DAY_MILLIS));
        	String enddate = sdf.format(new Date(System.currentTimeMillis()));
        	List<QueryRow> rows = new MBPApi.MBPDataGet(3299L, "startdate=" + startdate + ",enddate=" + enddate +
        			",sellerId=" + user.getId(), user.getSessionKey())
                    .call();

            if(CommonUtils.isEmpty(rows)) {
            	return;
            }
            for(QueryRow row : rows) {
            	if(row == null) {
            		continue;
            	}
            	UserHourViewTrade viewTrade = new UserHourViewTrade(row, user.getCid(), enddate);
            	UserHourViewTradeWritter.addMsg(viewTrade);
            } */
        }
    }
}
