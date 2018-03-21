package job.carriertask;

import models.carrierTask.CarrierLimitForDQ;
import models.carrierTask.CarrierTaskForDQ;
import models.carrierTask.CarrierTaskForDQ.CarrierTaskForDQStatus;
import models.carrierTask.CarrierTaskForDQ.CarrierTaskForDQType;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import result.TMResult;
import actions.alibaba.CopyToTmallAction;
import actions.alibaba.ItemCopyAction;
import actions.carriertask.CarrierTaskAction;

import com.taobao.api.domain.Item;

import configs.Subscribe.Version;
import dao.UserDao;

/**
 * Created by ww on 2017/11/23
 */
public class CarrierTaskForDQThread extends Thread {
	
	private static final Logger log = LoggerFactory.getLogger(CarrierTaskForDQThread.class);

	private Long taskId;

	public CarrierTaskForDQThread(Long taskId) {
		this.taskId = taskId;
	}

	@Override
	public void run() {
		CarrierTaskForDQ task = CarrierTaskForDQ.findById(taskId);
		if(task == null) {
			return;
		}
		if(task.getStatus() >= CarrierTaskForDQStatus.SUCCESS) {
			return;
		}
		
		try {
			doCarry(task);
		} catch (Exception e) {
			log.error(e.getMessage());
			
			String msg = StringUtils.isEmpty(e.getMessage())? "复制异常：错误未知" : e.getMessage();
			
			if(task.getRetry() < 3) {
				task.resetTask(task.getId(), msg);
			} else {
				CarrierTaskForDQ.finishTask(task.getId(), false, msg);
			}
		}
	}
	
	private static void doCarry(CarrierTaskForDQ task) {
		if(task == null) {
			return;
		}
		
		Long taskId = task.getId();
		
		User user = UserDao.findById(task.getUserId());
		if(user == null) {
			CarrierTaskForDQ.finishTask(taskId, false, "用户【" + task.getUserId() + "】不存在");
			return;
		}
		if(user.getVersion() != Version.LL && !user.getUserNick().equals("clorest510") && !user.getUserNick().equals("boyvon")) {
			CarrierTaskForDQ.finishTask(taskId, false, "用户【" + task.getUserId() + "】不是店群相关的用户");
			return;
		}
		
		if(!CarrierLimitForDQ.checkUserLimit(user.getId())) {
			CarrierTaskForDQ.finishTask(taskId, false, "已达到当月最大可复制宝贝数，如需继续复制请联系客服升级版本！");
			return;
		}
		
		Long numIid = task.getNumIid();
		int type = task.getType();
		Long cid = task.getCid();
		
		if(type == CarrierTaskForDQType.ALIBABA && user.isTmall() && cid <= 0) {
			CarrierTaskForDQ.finishTask(taskId, false, "1688复制任务(到天猫)需传递要复制的淘宝类目cid");
			return;
		}
		
		// 宝贝复制
		TMResult tmResult = null;
		switch (type) {
			case CarrierTaskForDQType.TB:
				if (user.isTmall()) {
					tmResult = CarrierTaskAction.doCarryForTmall(numIid, user);
				} else {
					tmResult = CarrierTaskAction.doCarryForTaobao(numIid, user);
				}
				break;
			case CarrierTaskForDQType.ALIBABA:
				if (user.isTmall()) {
					tmResult =CopyToTmallAction.doCopyItemToTmall(numIid.toString(), cid, user);
				} else {
					tmResult = ItemCopyAction.doCopyItem(numIid.toString(), cid, user, false,true);
				}
				break;
			default:
				if (user.isTmall()) {
					tmResult = CarrierTaskAction.doCarryForTmall(numIid, user);
				} else {
					tmResult = CarrierTaskAction.doCarryForTaobao(numIid, user);
				}
				break;
		}

		// 上传复制结果
		if (tmResult.isOk()) {
			String numIidStr = "复制成功";
			try {
				numIidStr = (type == CarrierTaskForDQType.TB ? tmResult.getMsg() : String.valueOf(((Item) tmResult.getRes()).getNumIid()));
			} catch (Exception e) {
				log.error("get numIidStr fail");
				// TODO: handle exception
			}
			CarrierTaskForDQ.finishTask(taskId, true, numIidStr);
			CarrierLimitForDQ.updateUseCountByUserId(user.getId());
		} else {
			CarrierTaskForDQ.finishTask(taskId, false, tmResult.getMsg());
		}
	}

}
