
package job.comment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Every;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import configs.TMConfigs;
import dao.comments.CommentsDao;

@Every("24h")
@OnApplicationStart(async = true)
public class CommentsDayJob extends Job {

    private static final Logger log = LoggerFactory.getLogger(CommentsDayJob.class);

    public void doJob() {
        if (TMConfigs.Server.jobTimerEnable) {
            CommentsDao.dayRemove();
        }
    }

}
