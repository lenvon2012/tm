package actions.carriertask;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.taobao.api.domain.Item;

import play.jobs.Job;
import models.carrierTask.CarrierTask;
import models.carrierTask.CarrierTaskForDQ;
import models.carrierTask.SubCarrierTask;
import models.carrierTask.CarrierTask.CarrierTaskStatus;
import models.carrierTask.CarrierTaskForDQ.CarrierTaskForDQStatus;
import models.carrierTask.SubCarrierTask.SubCarrierTaskStatus;
import models.user.User;
import result.TMResult;
import utils.CopyUtil;
import utils.ToolBy1688;
import actions.alibaba.CopyToTmallAction;
import actions.alibaba.ItemCopyAction;

public class BatchCarrier  extends Thread{
	
	public CarrierTask task;
	
	
	
	public BatchCarrier(){
		super();
	}
	
	public BatchCarrier(CarrierTask task){
		super();
		this.task=task;
	}
	

	@Override
	public void run() {
		 List<SubCarrierTask> carrierTasks=SubCarrierTask.findListByTaskId(task.getId());
         if (carrierTasks==null||carrierTasks.isEmpty()) {
				return;
			}
         task.setStatus(CarrierTaskStatus.running);
         task.jdbcSave();
         boolean isError=false;
         String errMsg="";
         for (SubCarrierTask subCarrierTask : carrierTasks) {
        	 if (isError) {
        		subCarrierTask.setErrorMsg(errMsg);
				subCarrierTask.setStatus(SubCarrierTaskStatus.failure);
				continue;
			}
         	//更新子任务信息
         	subCarrierTask.setPullTs(System.currentTimeMillis());
         	//处理复制
         	User user=User.findByUserNick(task.getPublisher());
         	TMResult tmResult = null;
         	String numIid=CopyUtil.parseItemId(subCarrierTask.getUrl());
         	
         	long cid=subCarrierTask.getCid();
         	if (cid==0) {
         		cid = ToolBy1688.getCatIdFor1688(subCarrierTask.getUrl());
				}
         	
         		String url="";
				if (user.isTmall()) {
					tmResult =CopyToTmallAction.doCopyItemToTmall(numIid, cid, user);
					url="https://detail.tmall.com/item.htm?id=";
					
				}else {
					tmResult = ItemCopyAction.doCopyItem(numIid, cid, user, false,false);
					url="https://item.taobao.com/item.htm?id=";
				}
				if (tmResult.isOk==false) {
					subCarrierTask.setErrorMsg(tmResult.getMsg());
					subCarrierTask.setStatus(SubCarrierTaskStatus.failure);
//					 String picExceptionMsg = "容量不足，请登录图片空间（tu.taobao.com）清理图片或订购存储功能包";
//				        String publishItemAmountExceptionMsg = "您今天发布的宝贝数量已超过了平台可支持单个账号宝贝发布数量";
					if (tmResult.getMsg().contains("容量不足，请登录图片空间")||tmResult.getMsg().contains("您今天发布的宝贝数量已超过了平台可支持单个账号宝贝发布数量")) {
						errMsg=tmResult.getMsg();
						isError=true;
					}
				}else {
					//设置子标题，宝贝图片，宝贝链接
					subCarrierTask.setBabyTitle(((Item)tmResult.getRes()).getTitle());
					subCarrierTask.setStatus(SubCarrierTaskStatus.success);
					subCarrierTask.setPicUrl(((Item)tmResult.getRes()).getPicUrl());
					url=url+((Item)tmResult.getRes()).getNumIid();
					subCarrierTask.setErrorMsg(url);
					
				}
				
				subCarrierTask.jdbcSave();
				//复制次数自增
				task.setFinishCnt(task.getFinishCnt()+1);
				if (task.getFinishCnt()==task.getBabyCnt()) {
					task.setStatus(CarrierTaskStatus.finished);
				}
			
			}
         task.jdbcSave();
	}

	

}
