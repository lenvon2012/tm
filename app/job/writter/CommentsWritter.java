
package job.writter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import models.comment.Comments;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import controllers.APIConfig;
import dao.trade.OrderDisplayDao;

@Every("15s")
public class CommentsWritter extends Job {

    private static final Logger log = LoggerFactory.getLogger(CommentsWritter.class);

    static Queue<Comments> queue = new ConcurrentLinkedQueue<Comments>();

    @Override
    public void doJob() {
        Comments pLog = null;
        log.info(" auto comments writter queue size is : " + queue.size());
        while ((pLog = queue.poll()) != null) {
            pLog.rawInsert();
            if (APIConfig.get().getApp() == APIConfig.defender.getApp()) {
                OrderDisplayDao.updateSellerRate(pLog.getUserId(), pLog.getOId(), true);
            }
        }
    }

    public static void addMsg(Long userId, Long tid, Long oid, String result, String content, String userNick,
            String buyerNick) {
        String realContent = StringUtils.EMPTY;
        if (content.length() > 255) {
            realContent = content.substring(0, 250).concat("...");
        } else {
            realContent = content;
        }
        queue.add(new Comments(userId, tid, oid, result, realContent, userNick, buyerNick));
    }

}
