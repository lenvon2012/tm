package job.weibo;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import job.weibo.SyncUserWeiboJob.SyncUserWeiboSource;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import configs.TMConfigs;

@Every("10s")
public class SyncForNewAccountJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(SyncUserWeiboJob.class);
    
    private static final Queue<User> userQueue = new ConcurrentLinkedQueue<User>();
    
    private static final Queue<String> nickQueue = new ConcurrentLinkedQueue<String>();
    
    public static void addQueue(User user) {
        if (user == null) {
            return;
        }
        if (TMConfigs.Is_Sync_Weibo == false) {
            return;
        }
        
        String userNick = user.getUserNick();
        if (nickQueue.contains(userNick)) {
            log.warn("user: " + userNick + " has already in nickQueue now--------------------");
            return;
        }
        
        userQueue.add(user);
        nickQueue.add(userNick);
    }
    
    @Override
    public void doJob() {
        
        try {
            if (TMConfigs.Is_Sync_Weibo == false) {
                return;
            }
            
            if (userQueue.size() > 0) {
                log.error("need sync " + userQueue.size() + " users account weibos--------");
            }
            
            User user = null;
            
            int userIndex = 0;
            while ((user = userQueue.poll()) != null) {
                userIndex++;
                
                String userNick = nickQueue.poll();
                if (StringUtils.isEmpty(userNick) || userNick.equals(user.getUserNick()) == false) {
                    log.error("SyncForNewAccountJob is something wrong: nickQueue: " + userNick 
                            + ", userQueue: " + user.getUserNick() + "---------------");
                }
                
                SyncUserWeiboJob syncJob = new SyncUserWeiboJob(user, userIndex, 
                        SyncUserWeiboSource.ChangeAccount);
                syncJob.call();
                
            }
            if (nickQueue.size() > 0 || userQueue.size() > 0) {
                log.error("after sync, userQueue size: " + userQueue.size() 
                        + ", nickQueue size: " + nickQueue.size() + "------------------");
            }
            
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        
    }
    
    
}
