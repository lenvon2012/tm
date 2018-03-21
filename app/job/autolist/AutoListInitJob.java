package job.autolist;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import job.autolist.service.InitCalcuListTime;
import job.autolist.service.ModifyListTime;
import models.autolist.AutoListRecord;
import models.autolist.AutoListTime;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import dao.UserDao;
import dao.autolist.AutoListRecordDao;
import dao.autolist.AutoListTimeDao;

public class AutoListInitJob extends Job {
	private static final Logger log = LoggerFactory.getLogger(AutoListInitJob.class);
	private AutoListRecord record;
	private static final String JOB_NAME = AutoListInitJob.class.getSimpleName();
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	
	public AutoListInitJob(AutoListRecord record) {
		this.record = record;
	}
	
	public void doJob() {
		User user = UserDao.findById(record.getUserId());
		
		long startTime = System.currentTimeMillis();
		log.info("job: " + JOB_NAME + " start: " + sdf.format(new Date()));
		AutoListTimeDao.deleteAutoListTimeByUser(record.getUserId());
		if (user == null) {
			log.error("错误！！！找不到用户" + record.getUserId());
			return;
		}
		calcuAutoListTime(user);
		
		record.setIsCalcuComplete(true);
		
		AutoListRecordDao.saveOrUpdateAutoListRecord(record);
		
		long endTime = System.currentTimeMillis();
		log.info("job: " + JOB_NAME + " end: " + sdf.format(new Date()));
		log.info("job: " + JOB_NAME + " used: " + (endTime - startTime) / 1000 + "秒");
	}
	
	private void calcuAutoListTime(User user) {
		InitCalcuListTime initListTime = new InitCalcuListTime(user);
		List<AutoListTime> timeList = initListTime.calcuListTime(record.getDistriType(),
				record.getDistriTime(), record.getDistriHours()); 
		
		
		for (AutoListTime autoListTime : timeList) {
			AutoListTimeDao.saveOrUpdateAutoListTime(autoListTime);
		}
	
		ModifyListTime.setSchedule(record, timeList);
	}
}
