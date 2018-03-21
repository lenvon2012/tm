package job.ump;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import actions.ump.UmpMjsAction;

@Every("5s")
public class UmpMjsTmplUpdateJob extends Job{

	static final Logger log = LoggerFactory.getLogger(UmpMjsTmplUpdateJob.class);

	public static String TAG = "UmpMjsTmplUpdateJob";
	
	static Queue<UserItemTmpl> queue = new ConcurrentLinkedQueue<UserItemTmpl>();
	
	@Override
	public void doJob() {
		Thread.currentThread().setName(TAG);
		
		log.info(" UmpMjsTmplUpdateJob queue size is : " + queue.size());
		
		UserItemTmpl tmpl = null;
		
		while ((tmpl = queue.poll()) != null) {
			// 删除详情页模板
            if(tmpl.getIsDelete()) {
            	UmpMjsAction.removeItemMjsTmpl(tmpl.getUser(), tmpl.getNumIid(), tmpl.getActivityId());
            } 
            // 更新详情页模板
            else {
            	UmpMjsAction.updateSingleItemMjsTmpl(tmpl.getUser(), 
            			tmpl.getNumIid(), tmpl.getTmplHtml(), tmpl.getActivityId());
            }
        }
	}
	
	public static int getQueueSize() {
		return queue.size();
	}
	
	public static void addTmpl(User user, Long numIid, String tmplHtml,
			Long activityId, Boolean isDelete) {
		
		queue.add(new UserItemTmpl(user, numIid, tmplHtml, activityId, isDelete));
	}
	
	public static class UserItemTmpl {
		
		private User user;
		
		private Long numIid;
		
		private String tmplHtml;
		
		private Boolean isDelete;
		
		private Long activityId;

		public UserItemTmpl(User user, Long numIid, String tmplHtml, Long activityId, 
				Boolean isDelete) {
			super();
			this.user = user;
			this.numIid = numIid;
			this.tmplHtml = tmplHtml;
			this.activityId = activityId;
			this.isDelete = isDelete;
		}

		public User getUser() {
			return user;
		}

		public void setUser(User user) {
			this.user = user;
		}

		public Long getActivityId() {
			return activityId;
		}

		public void setActivityId(Long activityId) {
			this.activityId = activityId;
		}

		public Long getNumIid() {
			return numIid;
		}

		public void setNumIid(Long numIid) {
			this.numIid = numIid;
		}

		public String getTmplHtml() {
			return tmplHtml;
		}

		public void setTmplHtml(String tmplHtml) {
			this.tmplHtml = tmplHtml;
		}

		public Boolean getIsDelete() {
			return isDelete;
		}

		public void setIsDelete(Boolean isDelete) {
			this.isDelete = isDelete;
		}

	}
}
