package job.writter;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import models.user.UserRateApiFetchResultLog;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import controllers.newAutoTitle;

import play.jobs.Every;
import play.jobs.Job;

@Every("15s")
public class UserRateApiFetchResultLogWritter extends Job {
	private static final Logger log = LoggerFactory.getLogger(UserRateApiFetchResultLogWritter.class);

    static Queue<UserRateApiFetchResultLog> queue = new ConcurrentLinkedQueue<UserRateApiFetchResultLog>();

    @Override
    public void doJob() {
    	UserRateApiFetchResultLog pLog = null;
        while ((pLog = queue.poll()) != null) {
            pLog.jdbcSave();
        }
    }

    public static void addMsg(com.taobao.api.domain.User user) {
    	if(user == null || StringUtils.isEmpty(user.getNick())) {
    		return;
    	}
        queue.add(new UserRateApiFetchResultLog(user));
    }
}

