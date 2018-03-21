package job.clouddate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import result.TMResult;
import titleDiag.DiagResult;
import actions.clouddata.UserHourViewTrade;
import bustbapi.MBPApi;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.DateUtil;
import com.taobao.api.domain.QueryRow;

import configs.TMConfigs;
import dao.UserDao;
import dao.UserDao.UserBatchOper;

public class UpdateUserWeekViewTradeJob extends Job{

	static final Logger log = LoggerFactory.getLogger(UpdateUserWeekViewTradeJob.class);

	public static String TAG = "UpdateUserWeekViewTradeJob";
	
    public static SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyyMMdd");
    
    public void doJob() {
        
        Thread.currentThread().setName(TAG);
        
        if (!TMConfigs.Server.jobTimerEnable) {
            return;
        }

        new UserBatchOper(16) {
            public List<User> findNext() {
                return UserDao.findValidList(offset, limit);
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
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
            return null;
        }
        
        public void doWithUser(final User user) {
        	String startdate = sdf.format(new Date(System.currentTimeMillis() - 7 * DateUtil.DAY_MILLIS));
        	String enddate = sdf.format(new Date(System.currentTimeMillis()));
        	TMResult<List<QueryRow>> res = new MBPApi.MBPDataGet(3299L, "startdate=" + startdate + ",enddate=" + enddate +
        			",sellerId=" + user.getId(), user.getSessionKey())
                    .call();

        	List<QueryRow> rows = res.getRes();
            if(CommonUtils.isEmpty(rows)) {
            	return;
            }
            for(QueryRow row : rows) {
            	if(row == null) {
            		continue;
            	}
            	UserHourViewTrade viewTrade = new UserHourViewTrade(row, user.getCid(), enddate);
            	UserHourViewTradeWritter.addMsg(viewTrade);
            } 
        }
    }
}
