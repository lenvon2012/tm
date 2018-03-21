
package controllers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import models.carrierTask.CarrierTask;
import models.carrierTask.SubCarrierTask;
import models.user.User;
import result.TMResult;
import utils.DateUtil;

import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.JsonUtil;

public class Kits extends TMController {
	
	private static SimpleDateFormat hmsdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public static void index() {
		render("Kits/kitIndex.html");
	}

	public static void window() {
		render("Kits/tbtwindow.html");
	}

	public static void itemCarrier() {
		render("carrier/TZGItemCarrier.html");
	}
	
	public static void alibabaCarrier() {
		boolean isTmall=getUser().isTmall();
//		render("carrier/alibabaCarrier.html",isTmall);
		render("carrier/TZGalibabaCarrier.html",isTmall);
	}
	
	public static void alibabaCarrierTest() {
		boolean isTmall=getUser().isTmall();
		render("carrier/TZGalibabaCarrier.html",isTmall);
	}
	
	public static void pddCarrier() {
		render("carrier/pddCarrier.html");
	}

	public static void comment() {
		render("Kits/tbtcomment.html");
	}

	public static void delist() {
		render("Kits/tbtdelist.html");
	}

	public static void delistPlans() {
		render("Kits/delistPlans.html");
	}

	public static void delistCreate() {
		render("Kits/delistCreate.html");
//		render("Kits/newDelistCreate.html");
	}
	
	public static void delistPlanDetail(long planId) {
		render("Kits/delistPlanDetail.html");
	}

	public static void delistVideo() {
		render("tbtnavmain/delistvideo.html");
	}

	public static void seoVideo() {
		render("tbtnavmain/seovideo.html");
	}

	public static void seohelp() {
		render("tbtnavmain/seohelp.html");
	}

	public static void helpwindow() {
		render("tbtnavmain/windowhelp.html");
	}

	public static void taskList() {
		render("carrier/tasklist.html");
	}
	
	public static void carryLog() {
		render("carrier/carryLog.html");
	}

	public static void adminTaskList() {
		render("tmadmin/adminTaskList.html");
	}
	
	/**
	 * 获取复制任务
	 */
	public static void getCarryLog(String startTime, String endTime, Long taskId, String originItem, String resultMsg, int taskStatus, int subTaskType, int pn) {
		User user= getUser();
		
		PageOffset po = new PageOffset(pn, 10);
		
		Long startTs = 0L;
		Long endTs = 0L;
		
		try {
			if(!StringUtils.isEmpty(startTime)) {
				startTs = hmsdf.parse(startTime).getTime();
			}
			if(!StringUtils.isEmpty(endTime)) {
				endTs = hmsdf.parse(endTime).getTime();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		List<SubCarrierTask> list = SubCarrierTask.findBySearchRules(startTs, endTs, resultMsg, taskId, originItem, user.getUserNick(), taskStatus, subTaskType, po);
		int count = SubCarrierTask.countBySearchRules(startTs, endTs, resultMsg, taskId, originItem, user.getUserNick(), subTaskType, taskStatus);

		renderJSON(JsonUtil.getJson(new TMResult(list, count, po)));
	}
	
	/**
	 * 重启复制任务
	 */
	public static void reStartById(Long id) {
		if (id == null || id <= 0) {
			renderError("请先选择一个复制任务");
		}
		
		SubCarrierTask subCarrierTask = SubCarrierTask.findById(id);
		Long taskId = subCarrierTask.getTaskId();
		if (taskId >= 0 && subCarrierTask.getStatus() == SubCarrierTask.SubCarrierTaskStatus.failure) {
			CarrierTask.reduceFinishCnt(taskId);
		}
		SubCarrierTask.rebootById(id);

		renderSuccess("任务重启成功", "");
	}
	
}
