package models.updatetimestamp.updatestatus;

import javax.persistence.Entity;

import models.updatetimestamp.UserDailyUpdateStatus;

@Entity(name = JDTradeDailyUpdateTask.TABLE_NAME)
public class JDTradeDailyUpdateTask extends UserDailyUpdateStatus {

	public final static String TABLE_NAME = "jd_trade_daily_update_task";

	public JDTradeDailyUpdateTask(Long userId, Long ts) {
		super(userId, ts);
	}

	public static JDTradeDailyUpdateTask findByUserIdAndTs(Long userId, Long ts) {
		return JDTradeDailyUpdateTask.find("userId= ?  and ts = ? ", userId, ts)
				.first();
	}

	public static JDTradeDailyUpdateTask findOrCreate(Long userId, Long ts) {
		JDTradeDailyUpdateTask task = findByUserIdAndTs(userId, ts);
		if (task == null) {
			task = new JDTradeDailyUpdateTask(userId, ts).save();
		}
		return task;

	}
}