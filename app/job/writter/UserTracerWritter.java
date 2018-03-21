
package job.writter;

import java.util.Queue;

import models.user.UserTracer;

import org.elasticsearch.common.util.concurrent.jsr166y.ConcurrentLinkedDeque;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.NoTransaction;
import play.jobs.Every;
import play.jobs.Job;

@Every("20s")
@NoTransaction
public class UserTracerWritter extends Job {

    private static final Logger log = LoggerFactory.getLogger(UserTracerWritter.class);

    public static final String TAG = "UserTracerWritter";

    static Queue<UserTracerMsg> queue = new ConcurrentLinkedDeque<UserTracerMsg>();

    @Override
    public void doJob() {
        UserTracerMsg msg = null;
//        log.info("[user trade queue size :]" + queue.size());
        while ((msg = queue.poll()) != null) {

            boolean success = msg.rawUpdate();
            if (!success) {
                UserTracer.ensure(msg.getUserId());
            }
        }
    }

    public abstract static class UserTracerMsg {
        protected Long userId;

        public abstract boolean rawUpdate();

        public UserTracerMsg(Long userId) {
            super();
            this.userId = userId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

    }

    public static class ShowWindowAddMsg extends UserTracerMsg {
        public ShowWindowAddMsg(Long userId) {
            super(userId);
        }

        @Override
        public boolean rawUpdate() {
            return UserTracer.addShowWindowCount(this.userId);
        }
    }

    public static class DeListCountAddMsg extends UserTracerMsg {
        public DeListCountAddMsg(Long userId) {
            super(userId);
        }

        @Override
        public boolean rawUpdate() {
            return UserTracer.addDelistCount(this.userId);
        }
    }

    public static class CommentCountAddMsg extends UserTracerMsg {
        public CommentCountAddMsg(Long userId) {
            super(userId);
        }

        @Override
        public boolean rawUpdate() {
            return UserTracer.addCommendCount(this.userId);
        }
    }

    public static void add(UserTracerMsg msg) {
//        queue.add(msg);
    }

    public static void addShowWindowMsg(Long userId) {
        UserTracerWritter.add(new ShowWindowAddMsg(userId));
    }

    public static void addDelistCountMsg(Long userId) {
        UserTracerWritter.add(new DeListCountAddMsg(userId));
    }

    public static void addCommentCountMsg(Long userId) {
        UserTracerWritter.add(new CommentCountAddMsg(userId));
    }
}
