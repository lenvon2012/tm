
package job;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;

import job.comment.AutoCommentTimer;
import job.writter.CommentsWritter;
import models.user.User;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import result.TMResult;
import configs.TMConfigs;
import dao.UserDao;

@Every("5s")
public class CommentMessages extends Job {
	private static final Logger log = LoggerFactory.getLogger(CommentMessages.class);

	public static final String TAG = "DoMessages";

	public static final int retry = 5;

	public static final int IN_QUEUE_RETRY_TIME = 3;

	static Queue<CommenMsg> queue = new ConcurrentLinkedQueue<CommenMsg>();
	
	private static final int POOL_SIZE = 16;

	@Override
	public void doJob() {
		ThreadPoolExecutor pool = TMConfigs.getCommentMessagesPool();
		CommenMsg msg = null;
		
		log.info("[current queue size]: " + queue.size());
		log.info("[current CommentMessagesPool getActiveCount]: " + pool.getActiveCount());
		
		while ((msg = queue.poll()) != null && pool.getActiveCount() < POOL_SIZE) {
			final CommenMsg currMsg = msg;
			pool.execute(new Runnable() {
				@Override
				public void run() {
					autoEvaluate(currMsg);
				}
			});
		}
	}

	public static void addMsg(CommenMsg commentMsg) {
		queue.add(commentMsg);
		log.info("~~~自动评价：addMsg into:" + commentMsg + "~~~[current queue size]:" + queue.size() + "~~~");
	}

	public void autoEvaluate(CommenMsg msg) {
		if (msg.getInQueueTime() > IN_QUEUE_RETRY_TIME) {
			log.warn(" msg reached... let's go...:" + msg);
			return;
		}

		boolean doneForThisTime = false;
		try {
//			log.info("Do autoEvaluate Job with msg: " + msg);
			JSONObject obj = new JSONObject(msg.getMsgBody());
			if (obj.has("notify_trade")) {
				obj = obj.getJSONObject("notify_trade");
			}

			Long userId = obj.getLong("user_id");
			User user = UserDao.findById(userId);
			String buyerNick = obj.getString("buyer_nick");
			Long tid = Long.parseLong(obj.getString("tid"));
			
			TMResult res = AutoCommentTimer.commentByOrder(user, tid, buyerNick);
//			log.info("AutoCommentTimer.commentByOrder result is " + content);
			if (res.isOk()) {
				CommentsWritter.addMsg(userId, Long.parseLong(obj.getString("tid")), Long.parseLong(obj.getString("oid")), "good", res.getMsg(),
						user.getUserNick(), obj.getString("buyer_nick"));
				doneForThisTime = true;
			} else {
				if(!"评价接口调用失败".equalsIgnoreCase(res.getMsg())) {
					log.info("~~~自动评价：[" + res.getMsg() + "]~~~");
					doneForThisTime = true;
				}
//				log.error("comment fail for user: " + (user == null ? null : user.toIdNick()) + ", tid=" + tid);
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}

		if (!doneForThisTime) {
			msg.updateCountPlus();
			queue.add(msg);
		}
	}

	public static class CommenMsg {
		int inQueueTime = 0;

		String msgBody;

		public void updateCountPlus() {
			inQueueTime++;
		}

		public CommenMsg() {
			super();
		}

		public CommenMsg(int inQueueTime, String msgBody) {
			super();
			this.inQueueTime = inQueueTime;
			this.msgBody = msgBody;
		}

		public int getInQueueTime() {
			return inQueueTime;
		}

		public void setInQueueTime(int inQueueTime) {
			this.inQueueTime = inQueueTime;
		}

		public String getMsgBody() {
			return msgBody;
		}

		public void setMsgBody(String msgBody) {
			this.msgBody = msgBody;
		}

		@Override
		public String toString() {
			return "CommenMsg [inQueueTime=" + inQueueTime + ", msgBody=" + msgBody + "]";
		}

	}
	
}
