package job.jms;

import jdp.JdpRegisterAllUserJob;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bustbapi.JMSApi.JushitaJmsUserAdd;
import bustbapi.JMSApi.JushitaJmsUserGet;

import com.taobao.api.domain.TmcUser;

import play.jobs.Job;
import play.jobs.OnApplicationStart;
import configs.TMConfigs.Server;
import controllers.APIConfig;
import dao.UserDao;
import dao.UserDao.UserBatchOper;

@OnApplicationStart(async = true)
public class ONSUserAddJob extends Job {
	
	private static final int APP = 21404171;

	private static final Logger log = LoggerFactory.getLogger(ONSUserAddJob.class);

	public void doJob() {
		log.info("ONSUserAddJob launch!!");
		
		if (!Server.jobTimerEnable) {
			return;
		}
		// 好评助手
		if (APIConfig.get().getApp() != APP) {
			return;
		}
		
		doONSUserAdd();
	}
	
	private void doONSUserAdd() {
		new UserBatchOper(32) {
			@Override
			public void doForEachUser(User user) {
				this.sleepTime = 1L;
				if (!UserDao.doValid(user)) {
					return;
				}
				// 添加ONS消息同步用户
				JushitaJmsUserAdd jmsAdd = new JushitaJmsUserAdd(user);
				Boolean success = jmsAdd.call();
				if(!success) {
					log.error("添加ONS消息同步用户失败！" + user.toString() + "~~~错误： " + jmsAdd.getSubErrorMsg());
				} else {
					log.info("添加ONS消息同步用户成功！" + user.toString());
				}
			}
		}.call();
	}
	
}
