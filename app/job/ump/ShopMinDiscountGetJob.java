package job.ump;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import models.ump.ShopMinDiscountPlay;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import utils.PlayUtil;
import actions.ump.ShopMinDiscountGetAction;

import com.ciaosir.client.CommonUtils;


@Every("5s")
public class ShopMinDiscountGetJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(ShopMinDiscountGetJob.class);
    
    private static Queue<User> userQueue = new ConcurrentLinkedQueue<User>();
    private static Queue<String> nickQueue = new ConcurrentLinkedQueue<String>();
    
    
    public static void addUser(User user) {
        if (user == null) {
            return;
        }
        
        String nick = user.getUserNick();
        if (nickQueue.contains(nick)) {
            log.warn("ShopMinDiscountGetJob: user: " + nick + " is already in queue now-----------");
            return;
        }
        userQueue.add(user);
        nickQueue.add(nick);
    }
    
    @Override
    public void doJob() {
        
        if (CommonUtils.isEmpty(userQueue)) {
            return;
        }
        
        log.info("do for ShopMinDiscountGetJob, queue size: " + userQueue.size());
        
        User user = null;
        
        while ((user = userQueue.poll()) != null) {
            
            String nick = nickQueue.poll();
            
            ShopMinDiscountPlay shopDiscount = ShopMinDiscountPlay.findByUserId(user.getId());
            if (shopDiscount != null) {
                continue;
            }
            
            ShopMinDiscountGetAction.fetchShopMinDiscount(user);
            
            PlayUtil.sleepQuietly(1000L);
        }
        
        if (userQueue.size() != nickQueue.size()) {
            log.error("some thing is wrong for ShopMinDiscountGetJob!!!!!!!!!!!!!!!!!");
        }
        
    }
    
}
