package job.apiget;

import java.util.Date;

import models.jd.JDUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import bustbapi.ApiUtil;

import com.ciaosir.client.utils.DateUtil;

import dao.JDUserDao;

public abstract class JDUpdateJob extends Job {

    public static final Logger log = LoggerFactory.getLogger(JDUpdateJob.class);

    public static final String TAG = "JDUpdateJob";

    // protected final Long userId;

    protected long start = 0L;

    protected long end = 0L;

    protected long maxUpdateTs = 0L;

    protected long now = 0L;

    protected boolean isFirstUpdate = false;

    protected JDUser user;

    protected Long userId;

    public JDUpdateJob(Long userId) {
//        user = JDUserDao.findById(userId);

        this.userId = userId;
        this.isFirstUpdate = false;
        this.now = DateUtil.formCurrDate();
    }

    protected boolean prepare() {

        // log.info("[Today]" + new Date(today));
        log.info("[Now]" + new Date(now));

        maxUpdateTs = getMaxUserUpdateVersion();
        if (maxUpdateTs == 0) {
            this.isFirstUpdate = true;
        }
        log.info("Current Max Info:" + new Date(maxUpdateTs));

        if (now - maxUpdateTs <= getInterval()) {
            log.info("[No Need To Update for]" + user.getId());
            return false;
        }

        start = maxUpdateTs;
        end = now;

        log.info("Set new Start and End[" + new Date(start) + "--" + new Date(end) + "]");
        return true;
    }

    abstract protected void requestUpdate(long start, long end);

    // abstract protected Boolean writeDB(final List resultList);
    // abstract protected void updateTs();
    abstract public long getMaxUserUpdateVersion();

    // public void process(){};

    @Override
    public void doJob() {

        if (!prepare()) {
            return;
        }

        if (ApiUtil.isApiCallLimited()) {
            log.error("the api call limited!");
            return;
        }

        user = JDUserDao.findById(userId);
        if (user == null) {
            log.warn("No User Id with:" + user.getId());
            return;
        }

       //  if (user == null || !user.hasValidShop()) {
       //  log.warn("The user is not vaild: " + user);
       //  return;
       //  }

        requestUpdate(getStart(), getEnd());

    }

    protected long getStart() {
        return start;
    }

    protected long getInterval() {
        return DateUtil.DAY_MILLIS;
    }

    protected long getEnd() {
        return end;
    }
}
