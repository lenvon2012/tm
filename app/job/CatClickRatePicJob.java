package job;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import job.writter.CatClickRatePicWritter;
import models.CatClickRatePic;
import models.item.ItemPlay;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import result.TMResult;
import titleDiag.DiagResult;
import bustbapi.MBPApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.taobao.api.domain.QueryRow;

import configs.TMConfigs;
import dao.UserDao;
import dao.UserDao.UserBatchOper;
import dao.item.ItemDao;

public class CatClickRatePicJob extends Job{

	static final Logger log = LoggerFactory.getLogger(CatClickRatePicJob.class);

	public static String TAG = "CatClickRatePicJob";
	
    public static SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyyMMdd");
    
    public void doJob() {
        
        Thread.currentThread().setName(TAG);
        
        if (!TMConfigs.Server.jobTimerEnable) {
            return;
        } 

        new UserBatchOper(16) {
            public List<User> findNext() {
                return UserDao.findLevel5Users(offset, limit);
            }

            @Override
            public void doForEachUser(final User user) {
                TMConfigs.getDiagResultPool().submit(new UserCaller(user));
            }
        }.call();
    }
    
    public static class UserCaller implements Callable<DiagResult> {
    	User user;

        public UserCaller(User user) {
            super();
            this.user = user;
        }

        @Override
        public DiagResult call() {
            try {
                doWithUser(user);
                CommonUtils.sleepQuietly(500);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
            return null;
        }
        
        public void doWithUser(final User user) {
        	if(user == null) {
        		return;
        	}
        	TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(3395L, 
        			"sellerId=" + user.getId(), user.getSessionKey()).call();
        	List<QueryRow> bigClickRates = res.getRes();
        	if(CommonUtils.isEmpty(bigClickRates)) {
        		return;
        	}
        	for(QueryRow row : bigClickRates) {
        		if(row == null) {
        			continue;
        		}
        		List<String> values = row.getValues();
        		if(CommonUtils.isEmpty(values)) {
        			continue;
        		}
        		Long numIid = Long.valueOf(values.get(0));
        		if(numIid == null || numIid <= 0) {
        			continue;
        		}
        		ItemPlay itemPlay = ItemDao.findByNumIid(user.getId(), numIid);
        		if(itemPlay == null) {
        			continue;
        		}
        		CatClickRatePic catClickRatePic = new CatClickRatePic(itemPlay.getCid(), itemPlay.getPicURL(),
        				user.getId(), numIid, Double.valueOf(values.get(3)),
        				Integer.valueOf(values.get(2)), Integer.valueOf(values.get(1)));
        		CatClickRatePicWritter.addMsg(catClickRatePic);
        	}

        }
    }
    
    public static class ItemCaller implements Callable<DiagResult> {
    	
    	User user;
    	
    	ItemPlay itemPlay;

        public ItemCaller(ItemPlay itemPlay, User user) {
            super();
            this.itemPlay = itemPlay;
            this.user = user;
        }

        @Override
        public DiagResult call() {
            try {
                doWithItem(itemPlay, user);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
            return null;
        }
        
        public void doWithItem(final ItemPlay itemPlay, final User user) {
        	if(itemPlay == null) {
        		return;
        	}
        	
        	if(user == null) {
        		return;
        	}
        	String startdate = sdf.format(new Date(System.currentTimeMillis() - 7 * DateUtil.DAY_MILLIS));
        	String enddate = sdf.format(new Date(System.currentTimeMillis()));
        	TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(3213L, 
        			"startdate=" + startdate + ",numIid=" + itemPlay.getNumIid() +
        			",sellerId=" + user.getId() + 
        			",enddate=" + enddate, user.getSessionKey()).call();
        	List<QueryRow> totalImpressionAndClicks = res.getRes();
            if(!CommonUtils.isEmpty(totalImpressionAndClicks)) {
            	List<String> values = totalImpressionAndClicks.get(0).getValues();
            	if(!CommonUtils.isEmpty(values)) {
            		int aclick = Integer.valueOf(totalImpressionAndClicks.get(0).getValues().get(1));
            		int impression = Integer.valueOf(totalImpressionAndClicks.get(0).getValues().get(0));
            		double clickRate = (impression <= 0) ? 0.0 : aclick * 1.0 / impression;
            		log.info("clickRate = " + clickRate + "   cccccccccccccc");
            		// 点击率低于6%的宝贝，没用，不保存
            		if(clickRate < 0.06) {
            			return;
            		}
            		CatClickRatePic catClickRatePic = new CatClickRatePic(itemPlay.getCid(), itemPlay.getPicURL(),
            				user.getId(), itemPlay.getNumIid(), clickRate, aclick, impression);
            		CatClickRatePicWritter.addMsg(catClickRatePic);
            	}	
            }
            CommonUtils.sleepQuietly(500);
        }
    }
}

