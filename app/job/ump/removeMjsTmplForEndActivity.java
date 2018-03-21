package job.ump;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import models.promotion.TMProActivity;
import models.ump.removeMjsTmplForEndActivityLog;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import titleDiag.DiagResult;
import utils.DateUtil;

import com.ciaosir.client.CommonUtils;

import configs.TMConfigs;
import controllers.APIConfig;
import dao.UserDao;
import dao.item.ItemDao;

@Every("1h")
public class removeMjsTmplForEndActivity extends Job{
	
	static final Logger log = LoggerFactory.getLogger(removeMjsTmplForEndActivity.class);

	public static String TAG = "removeMjsTmplForEndActivity";

	public static SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyyMMdd HH:mm:ss");
	
	@Override
	public void doJob() {
		if(APIConfig.get().getApp() != APIConfig.dazhe.getApp()) {
			return;
		}
		Long now = System.currentTimeMillis();
		log.info("do removeMjsTmplForEndActivity at " + sdf.format(new Date(now)));
		// 16张分表
		for(int i = 0; i < 16; i++) {
			// 当前生效中，并且一小时后到期的满就送活动，数量不多，暂时这样写
			List<TMProActivity> activities = TMProActivity.findEndMjsActivitys(i, now);
			if(CommonUtils.isEmpty(activities)) {
				continue;
			}
			for(TMProActivity activity : activities) {
				TMConfigs.getDiagResultPool().submit(new ActivityCaller(activity, now));
			}
			
		}
	}
	
	public static class ActivityCaller implements Callable<DiagResult> {
		
		Long jobTs;
		
        TMProActivity activity;

        public ActivityCaller(TMProActivity activity, Long jobTs) {
            super();
            this.activity = activity;
            this.jobTs = jobTs;
        }

        @Override
        public DiagResult call() {
            try {
                doWithUser(activity, jobTs);
                CommonUtils.sleepQuietly(200L);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
            return null;
        }

        long intervalMillis = DateUtil.FOUR_DAYS;

        public void doWithUser(final TMProActivity activity, final Long jobTs) {
        	log.info("removeMjsTmplForEndActivity for userId = " + activity.getUserId() +
					", antivityId = " + activity.getId());
        	
        	Set<Long> numIids = new HashSet<Long>();
        	if(activity.getActivityType() == TMProActivity.ActivityType.Manjiusong) {
        		numIids = activity.fetchItemNumIidSet();
        	} else {
        		numIids = ItemDao.findNumIidWithUser(activity.getUserId());
        	}
        	new removeMjsTmplForEndActivityLog(jobTs, activity.getId(), activity.getActivityType(),
        			activity.getUserId(), activity.getItems()).jdbcSave();
			if(CommonUtils.isEmpty(numIids)) {
				return;
			}
			User user = UserDao.findById(activity.getUserId());
			if(user == null) {
				return;
			}
			for(Long numIid : numIids) {
				UmpMjsTmplUpdateJob.addTmpl(user, numIid, "", activity.getId(), true);
			}
        }
    }
	
}
