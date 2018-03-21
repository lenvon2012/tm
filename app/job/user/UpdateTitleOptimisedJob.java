package job.user;

import java.util.List;

import job.writter.TitleOptimisedWritter;
import models.item.ItemPlay;
import models.oplog.TitleOptimiseLog;
import models.user.TitleOptimised;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;

import com.ciaosir.client.CommonUtils;

import dao.UserDao;
import dao.item.ItemDao;

public class UpdateTitleOptimisedJob extends Job{
	
	static final Logger log = LoggerFactory.getLogger(UpdateTitleOptimisedJob.class);
	
	public static String TAG = "UserTypeUpdateJob";
	  
	  public static Long userCount = 0L;
	  public void doJob() {
	      Thread.currentThread().setName(TAG);
	      
	      new UserDao.UserBatchOper(16) {
	          @Override
	          public void doForEachUser(final User user) {
	        	  log.info("do update UserType for User " + user.getUserNick());
	              List<ItemPlay> items = ItemDao.findByUserId(user.getId());
	              if(CommonUtils.isEmpty(items)) {
	            	  return;
	              }
	              for(ItemPlay itemPlay : items) {
	            	  long count = TitleOptimiseLog.count("userId = ? and numIid = ?", 
	            			  itemPlay.getUserId(), itemPlay.getNumIid());
	            	  TitleOptimised optimised = TitleOptimised.findByUserId(user.getId(), 
	            			  itemPlay.getNumIid());
	            	  // 曾经优化过的才记录
	            	  if(count > 0) {
	            		  TitleOptimisedWritter.addMsg(user.getId(), itemPlay.getNumIid(), true);
	            	  } 
	            	  
	              }
	              
	          }

	      }.call();
	  }
}
