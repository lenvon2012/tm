package job.writter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import models.comment.UserTradeCommentLog;
import play.jobs.Every;
import play.jobs.Job;

@Every("15s")
public class UserTradeCommentLogWritter extends Job {
    
    static Queue<UserTradeCommentLog> queue = new ConcurrentLinkedQueue<UserTradeCommentLog>();
    
    @Override
    public void doJob() {
        UserTradeCommentLog pLog = null;
        while ((pLog = queue.poll()) != null) {
            pLog.rawInsert();
        }
    }
    
    public static void addMsg(Long userId, String nick, String jobTs, int orderCount, int unCommentedOrderCount, int cannotrateCount, int successCount, int failCount, String failOrderIds) {
      queue.add(new UserTradeCommentLog(userId, nick, jobTs, orderCount, unCommentedOrderCount, cannotrateCount, successCount, failCount, failOrderIds));
  }
    
}
