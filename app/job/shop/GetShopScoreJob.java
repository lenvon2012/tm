package job.shop;

import models.shop.ShopScorePlay;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bustbapi.ShopApi.ShopGet;

import com.taobao.api.domain.Shop;
import com.taobao.api.domain.ShopScore;

import play.jobs.Job;
import play.jobs.On;
import play.jobs.OnApplicationStart;
import configs.TMConfigs.Server;
import controllers.APIConfig;
import dao.UserDao;
import dao.UserDao.UserBatchOper;

@On("0 5 0 * * ?")
public class GetShopScoreJob extends Job {
	
	private static final int APP = 21404171;

	private static final Logger log = LoggerFactory.getLogger(GetShopScoreJob.class);

	public void doJob() {
		log.info("GetShopScoreJob launch!!");
		
		if (!Server.jobTimerEnable) {
			return;
		}
		// 好评助手
		if (APIConfig.get().getApp() != APP) {
			return;
		}
		
		doGetShopScoreJob();
	}
	
	private void doGetShopScoreJob() {
		new UserBatchOper(32) {
			@Override
			public void doForEachUser(User user) {
				this.sleepTime = 1L;
				if (!UserDao.doValid(user)) {
					return;
				}
				// 获取店铺评分
				Shop shop = new ShopGet(user.getUserNick()).call();
				if(shop == null) {
					return;
				}
				ShopScorePlay score = new ShopScorePlay(shop, user.getId());
				score.jdbcSave();
			}
		}.call();
	}
	
}
