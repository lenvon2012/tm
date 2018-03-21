package job.sync.rpt;

import java.util.Date;
import java.util.concurrent.Callable;

import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.DateUtil;
import bustbapi.LoginApi;
import configs.BusConfigs;

public abstract class SyncRptJob implements Callable<Void> {

    public final static Logger log = LoggerFactory.getLogger(SyncRptJob.class);

    protected User user;
    protected String userNick;
    protected Long startTs;
    protected Long endTs;
    protected Long maxUpdateTs = 0L;
    protected String subwayToken = StringUtils.EMPTY;

    public SyncRptJob(User user, String userNick, Long endTs) {
        this(user, userNick, -1L, endTs, null);
    }

    public SyncRptJob(User user, String userNick, Long endTs, String subwayToken) {
        this(user, userNick, -1L, endTs, subwayToken);
    }

    public SyncRptJob(User user, String userNick, Long startTs, Long endTs) {
        this(user, userNick, startTs, endTs, null);
    }

    public SyncRptJob(User user, String userNick, Long startTs, Long endTs, String subwayToken) {
        super();
        this.user = user;
        this.userNick = userNick;
        this.startTs = startTs;

        if (endTs >= DateUtil.formYestadyMillis()) {
            /**
             * rpt calculated by taobao after 4:00am
             * bug:             if (System.currentTimeMillis() - DateUtil.DAY_MILLIS > 5 * DateUtil.HOUR_MILLIS) {
             */
            if (System.currentTimeMillis() - DateUtil.formCurrDate() > 5 * DateUtil.ONE_HOUR_MILLIS) {
                endTs = DateUtil.formYestadyMillis();
            } else {
                endTs = DateUtil.formYestadyMillis() - DateUtil.DAY_MILLIS;
            }
        }

        this.endTs = endTs;
        this.subwayToken = subwayToken == null ? getSubwayToken() : subwayToken;
    }

    protected boolean prepare() {

        maxUpdateTs = getMaxUserUpdateVersion();

        Long maxReachableTs = DateUtil.formCurrDate() - DateUtil.DAY_MILLIS * getMaxReachable();

        if (maxUpdateTs == 0 || maxUpdateTs < maxReachableTs) {
            log.warn("Current Max Info before reset: " + new Date(maxUpdateTs));
            maxUpdateTs = maxReachableTs;
            startTs = maxUpdateTs;
        } else {
            maxUpdateTs += DateUtil.DAY_MILLIS;
            startTs = maxUpdateTs + DateUtil.DAY_MILLIS;
        }

//        log.info("Current Max Info:" + new Date(maxUpdateTs));

        if (endTs - maxUpdateTs < getInterval()) {
            log.info("[No Need To Update for]" + user.getId());
            return false;
        }

        

//        log.info("Set new Start and End[" + new Date(startTs) + "," + new Date(endTs) + "]");

//        log.info("subwayToken:" + subwayToken);
        return true;
    }

    protected abstract long getMaxReachable();

    @Override
    public Void call() throws Exception {

        if (!BusConfigs.RPT_ENABLE) {
            log.error("This Appkey does not have rpt permission!!! ");
            return null;
        }

        if (startTs < 0) {
            if (!prepare()) {
                return null;
            }
        } else {
            if (startTs > endTs) {
                log.info("startTs is bigger than endTs, startTs:" + startTs + ", endTs:" + endTs);
                return null;
            }
        }

        if (requestUpdate(startTs, endTs)) {
//            log.info("Job run success!!! update user update ts!!!");
            updateUserUpdateVersion(endTs);

            processAfterUpdate();
        }

        return null;
    }

    public abstract boolean requestUpdate(long start, long end);

    protected abstract void processAfterUpdate();

    protected abstract boolean syncRptBase(long startTs, long endTs);

    protected abstract boolean syncRptEffect(long startTs, long endTs);

    protected long getInterval() {
        return DateUtil.DAY_MILLIS;
    }

    protected abstract long getMaxUserUpdateVersion();

    protected abstract void updateUserUpdateVersion(long updateTs);

    public String getSubwayToken() {

        return new LoginApi.AuthsignGetApi(user.getSessionKey(), userNick).call();
    }

}
