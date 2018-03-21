package job.click;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import dao.popularized.PopularizedDao;
//@Every("180s")
public class UserCountJob extends Job {
	
    public static Map<Long, Integer> userIdNummIdCount = PopularizedDao.countUser();
	
	private static final Logger log = LoggerFactory.getLogger(UserCountJob.class);
	
	public void doJob() {
		//userIdNummIdCount.clear();
		userIdNummIdCount = PopularizedDao.countUser();
	}
}
