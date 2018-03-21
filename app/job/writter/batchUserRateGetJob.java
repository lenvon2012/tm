package job.writter;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ciaosir.client.CommonUtils;

import configs.TMConfigs;

import actions.catunion.UserRateSpiderAction.TBUserCache;
import bustbapi.UsersGetApi;

import play.jobs.Every;
import play.jobs.Job;

@Every("1s")
public class batchUserRateGetJob extends Job {
	private static final Logger log = LoggerFactory.getLogger(batchUserRateGetJob.class);

    static Queue<String> queue = new ConcurrentLinkedQueue<String>();

    private final static int fetchLimit = 40;
    
    @Override
    public void doJob() {
    	if(TMConfigs.App.ENABLE_USERS_GET_API == false) {
    		queue.clear();
    		return;
    	}
    	List<String> toFetchNicks = new ArrayList<String>();
    	int count = 0;
    	String nick;
    	log.info("batchUserRateGetJob size = " + queue.size());
        while ((nick = queue.poll()) != null && count < fetchLimit) {
            toFetchNicks.add(nick);
            count++;
        }
        if(CommonUtils.isEmpty(toFetchNicks)) {
        	return;
        }
        log.info("do real batchUserRateGetJob size = " + count);
        List<com.taobao.api.domain.User> tb_users = new UsersGetApi.UserGet(StringUtils.join(toFetchNicks, ",")).call();
        if(CommonUtils.isEmpty(tb_users)) {
        	return;
        }
        for(com.taobao.api.domain.User user : tb_users) {
        	TBUserCache.putToCache(user);
        	UserRateApiFetchResultLogWritter.addMsg(user);
        }
    }

    public static void addMsg(String nick) {
        queue.add(nick);
    }
    
    public static void clearMsg() {
        queue.clear();
    }
}
