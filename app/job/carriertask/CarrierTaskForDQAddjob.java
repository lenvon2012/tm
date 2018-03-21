package job.carriertask;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import models.carrierTask.CarrierTaskForDQ;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;

import com.ciaosir.client.CommonUtils;

import configs.TMConfigs;
import configs.TMConfigs.Server;
import controllers.APIConfig;

/**
 * Created by ww on 2017/11/23
 */
@Every("10s")
public class CarrierTaskForDQAddjob extends Job {
	
	private static final Logger log = LoggerFactory.getLogger(CarrierTaskForDQAddjob.class);
	
	private static final int APP = 21255586;
	
	private static final int POOL_SIZE = 16;

//	private static final ThreadPoolExecutor taskPool = new ThreadPoolExecutor(POOL_SIZE, POOL_SIZE, 0L,
//			TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(30));
	
	@Override
	public void doJob() {
		// 淘掌柜
		if (APIConfig.get().getApp() != APP) {
			return;
		}
		
		log.info("CarrierTaskForDQAddjob launch!!");
		
		if (Server.jobTimerEnable) {
			checkNeedResetTask();
		}
		
		checkNeedSubmitTask();
	}
	
	private static void checkNeedResetTask() {
		List<CarrierTaskForDQ> needResetTask = CarrierTaskForDQ.getNeedResetTask();
		if(CommonUtils.isEmpty(needResetTask)) {
			log.info("【CarrierTaskForDQAddjob】中暂无需要重启的复制任务");
			return;
		}
		
		for (CarrierTaskForDQ task : needResetTask) {
			task.resetTask(task.getId(), StringUtils.EMPTY);
		}
	}
	
	private static void checkNeedSubmitTask() {
		ThreadPoolExecutor taskPool = TMConfigs.getCarrierTaskForDQAddPool();
		
		int limit = POOL_SIZE - taskPool.getActiveCount();
		
		if (limit <= 0) {
			log.info("【CarrierTaskForDQAddjob】当前店群商品复制线程已满");
			return;
		}
		
		List<CarrierTaskForDQ> taskList = CarrierTaskForDQ.getNeedSubmitTask(limit);
		if(CommonUtils.isEmpty(taskList)) {
			log.info("【CarrierTaskForDQAddjob】中暂无需要提交的复制任务");
			return;
		}
		boolean success = CarrierTaskForDQ.batchStartTask(taskList);
		if(!success) {
			log.error("【CarrierTaskForDQAddjob】店群复制任务批量启动失败");
			return;
		}
		
		for (CarrierTaskForDQ task : taskList) {
			taskPool.execute(new CarrierTaskForDQThread(task.getId()));
		}
	}
	
}
