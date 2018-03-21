package job.carriertask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import models.carrierTask.CarrierItemPlay;
import models.carrierTask.CarrierTask;
import models.carrierTask.CarrierTaskForDQ;
import models.carrierTask.SubCarrierTask;
import models.carrierTask.CarrierItemPlay.CarrierItemBatchOper;
import models.carrierTask.CarrierItemPlay.CarrierItemPlatform;
import models.carrierTask.CarrierTask.CarrierTaskStatus;
import models.carrierTask.SubCarrierTask.SubCarrierTaskStatus;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import play.jobs.Every;
import play.jobs.Job;
import play.jobs.On;
import result.TMResult;
import utils.ApiUtilFor1688;
import utils.CopyUtil;
import utils.ToolBy1688;
import actions.alibaba.CopyToTmallAction;
import actions.alibaba.ItemCopyAction;
import actions.carriertask.BatchCarrier;
import actions.carriertask.CarrierTaskAction;
import actions.carriertask.TaskInfo;
import bustbapi.CarrierItemApi.itemSkuGet;
import bustbapi.CarrierItemApi.skusQuantityUpdate;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.utils.JsonUtil;
import com.taobao.api.domain.Item;
import com.taobao.api.domain.Sku;

import configs.TMConfigs;
import configs.TMConfigs.Server;
import controllers.APIConfig;
import dao.UserDao;

//@On("0 0 2 * * ?")
@Every ("30s")
//执行1688批量复制任务
public class DoBatchCarrier1688Job extends Job {

	private static final int APP = 21255586;

	private static final Logger log = LoggerFactory
			.getLogger(DoBatchCarrier1688Job.class);

	private static final int POOL_SIZE = 16;

	// 刷新access_token,并且抉择是否重置使用次数
	public void doJob() {
		log.info("DoBatchCarrier1688Job launch!!");

		if (Play.id.equals("oyster") == false) {
			if (!Server.jobTimerEnable) {
				return;
			}
			// 淘掌柜
			if (APIConfig.get().getApp() != APP) {
				return;
			}
		}

		ThreadPoolExecutor taskPool = TMConfigs.getCarrierTaskFor1688BatchdPool();

		int limit = POOL_SIZE - taskPool.getActiveCount();

		if (limit <= 0) {
			log.info("【DoBatchCarrier1688Job】当前阿里批量商品复制线程已满");
			return;
		}
		
		List<CarrierTask> tasks = CarrierTask.find1688BatchTasks();

		if (CommonUtils.isEmpty(tasks)) {
			log.info("【DoBatchCarrier1688Job】中暂无需要提交的复制任务");
			return;
		}
//		boolean success = CarrierTaskForDQ.batchStartTask(taskList);

		for (CarrierTask task : tasks) {
			taskPool.execute(new BatchCarrier(task));
		}
		// 执行复制操作

	}

}
