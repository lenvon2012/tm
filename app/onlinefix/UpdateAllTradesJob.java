
package onlinefix;

import java.util.List;
import java.util.concurrent.Callable;

import job.apiget.TradeRateDeleteSyncJob;
import job.apiget.TradeRateUpdateJob;
import job.apiget.TradeUpdateJob;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import utils.DateUtil;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;
import com.ciaosir.client.utils.NumberUtil;

import configs.TMConfigs.Server;
import controllers.APIConfig;
import dao.UserDao;

//@On("0 0 14 * * ? *")
@Every("8h")
public class UpdateAllTradesJob extends Job {

	private static final Logger log = LoggerFactory.getLogger(UpdateAllTradesJob.class);

	public static final String TAG = "UpdateAllTradesJob";

	static int totalCount = 0;

	static int currentNum = 0;

	static int finishNum = 0;

	static PYFutureTaskPool<Boolean> pool = null;
	
	int offset = 0;

	public static synchronized PYFutureTaskPool<Boolean> getPool() {
		if (pool != null) {
			return pool;
		}
		pool = new PYFutureTaskPool<Boolean>(NumberUtil.parserInt(Play.configuration.get("thread.itemupdate.num"), 32));
		return pool;
	}

	public UpdateAllTradesJob() {
		super();
	}

	public UpdateAllTradesJob(int offset) {
		super();
		this.offset = offset;
	}

	@Override
	public void doJob() {
		totalCount = 0;
		currentNum = 0;

		if (Play.mode.isDev()) {
			return;
		}

		if (APIConfig.get().getApp() != APIConfig.defender.getApp()) {
			return;
		}

		if(!Server.tradeUpdate) {
			log.info("not run UpdateAllTradesJob ");
			return;
		} else {
			log.info("run UpdateAllTradesJob ");
		}

		new UserDao.UserBatchOper(0, 32) {

			public List<User> findNext() {
				return UserDao.findValidListOrderBydFirstLoginTime(offset, limit);
			}

			@Override
			public void doForEachUser(final User user) {
				log.info("[do for user]: " + user);
				updateUser(user);

				CommonUtils.sleepQuietly(500L);
//				if (this.offset > 100) {
//					CommonUtils.sleepQuietly(250L);
//				}
			}
		}.call();
		
	}

	private static Boolean checkCurrentTime() {
		long currentTime = System.currentTimeMillis();
		long t0 = DateUtil.formDailyTimestamp(currentTime);
		long t8 = t0 + 8 * DateUtil.HOUR_MILLS;
		long t23 = t0 + 23 * DateUtil.HOUR_MILLS;

		if (currentTime > t8 & currentTime < t23) {
			return true;
		} else {
			log.info("当前时间段 not run UpdateAllTradesJob");
			return false;
		}
	}

	public static String getStatus() {
		return String.format("[total--%d--curr%d--finish %d]executing!", totalCount, currentNum, finishNum);
	}

//	public static void submitUserId(final Long userId) {
//		User user = UserDao.findById(userId);
//		if (APIConfig.get().getApp() != APIConfig.defender.getApp()) {
//			return;
//		}
//		updateUser(user);
//	}

	static int threadCount = 0;

	public static void updateUser(final User user) {
		if (user == null || !user.isVaild()) {
			return;
		}
		
		totalCount++;
		getPool().submit(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				Thread.currentThread().setName("TradeUpdateJob[" + threadCount + "]");
				currentNum++;

				try {
					new TradeUpdateJob(user.getId(), System.currentTimeMillis()).doJob();

					if (APIConfig.get().enableSyncTradeRate() && checkCurrentTime()) {
						new TradeRateUpdateJob(user.getId(), System.currentTimeMillis()).doJob();
						TradeRateDeleteSyncJob.doWithDeleteTradeRate(user, true);
					}

				} catch (Exception e) {
					log.error("error TRACE UpdateAllTradesJob . userId = " + user.getId());
					log.error(e.getMessage(), e);
				}

				log.warn(" update over for usesr:   " + user);
				finishNum++;
				return null;
			}
		});
	}

}
