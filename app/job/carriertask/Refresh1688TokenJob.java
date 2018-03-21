package job.carriertask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.carrierTask.CarrierItemPlay;
import models.carrierTask.CarrierItemPlay.CarrierItemBatchOper;
import models.carrierTask.CarrierItemPlay.CarrierItemPlatform;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;
import utils.ApiUtilFor1688;
import bustbapi.CarrierItemApi.itemSkuGet;
import bustbapi.CarrierItemApi.skusQuantityUpdate;

import com.taobao.api.domain.Item;
import com.taobao.api.domain.Sku;

import configs.TMConfigs.Server;
import controllers.APIConfig;
import dao.UserDao;

//@On("0 0 2 * * ?")
@Every ("8h")
public class Refresh1688TokenJob extends Job {
	
	private static final int APP = 21255586;

	private static final Logger log = LoggerFactory.getLogger(Refresh1688TokenJob.class);

	//刷新access_token,并且抉择是否重置使用次数
	public void doJob() {
		log.info("Refresh1688TokenJob launch!!");
		
		if (Play.id.equals("oyster")==false) {
			if (!Server.jobTimerEnable) {
				return;
			}
			// 淘掌柜
			if (APIConfig.get().getApp() != APP) {
				return;
			}
		}
		
		//刷新access_token,重置使用次数
		ApiUtilFor1688.doRefresh();
	}


	
}
