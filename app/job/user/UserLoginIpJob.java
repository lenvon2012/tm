
package job.user;

import java.util.Queue;

import models.op.UserRecentLogin;
import models.op.UserRecentLogin.UserIp;

import org.elasticsearch.common.util.concurrent.jsr166y.ConcurrentLinkedDeque;

import play.jobs.Every;
import play.jobs.Job;

@Every("10s")
public class UserLoginIpJob extends Job {

    static Queue<UserIp> queue = new ConcurrentLinkedDeque<UserIp>();

    public static void addUserIp(Long userId, String ip) {
        queue.add(new UserIp(userId, ip));
    }

    public void doJob() {
        UserIp model = null;
        while ((model = queue.poll()) != null) {
            UserRecentLogin.ensure(model);
        }
    }
}
