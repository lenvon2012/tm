package job.sync.rpt;

import models.rpt.response.RptUtils;
import models.updatetimestamp.updatets.RptCustUpdateTs;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.DateUtil;
import actions.rpt.RptCustAction;
import configs.BusConfigs;

public class SyncRptCustJob extends SyncRptJob {

    public final static Logger log = LoggerFactory.getLogger(SyncRptCustJob.class);

    public SyncRptCustJob(User user, String userNick) {
        this(user, userNick, -1L, DateUtil.formYestadyMillis(), null);
    }

    public SyncRptCustJob(User user, String userNick, Long endTs) {
        this(user, userNick, -1L, endTs, null);
    }

    public SyncRptCustJob(User user, String userNick, Long endTs, String subwayToken) {
        this(user, userNick, -1L, endTs, subwayToken);
    }

    public SyncRptCustJob(User user, String userNick, Long startTs, Long endTs, String subwayToken) {
        super(user, userNick, startTs, endTs, subwayToken);
    }

    @Override
    public long getMaxReachable() {
        return BusConfigs.RptConfig.MAX_CUST_RPT_GET;
    }

    @Override
    public boolean requestUpdate(long start, long end) {

        
        if (!syncRptBase(start, end)) {
            return false;
        }

        return syncRptEffect(start, end);
    }

    public boolean syncRptBase(long startTs, long endTs) {

        log.warn("start sync rpt cust for user: " + userNick + "-----------------");
        try {
            new RptCustAction.SyncCustBase(user.getId(),user.getSessionKey(), userNick, subwayToken, startTs, endTs,
                    RptUtils.Source.SUMMARY).call();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

        return true;
    }

    public boolean syncRptEffect(long startTs, long endTs) {
//        log.warn("sync rpt effect startTs:" + startTs + ", endTs:" + endTs);

        try {
            new RptCustAction.SyncCustEffect(user.getId(),user.getSessionKey(), userNick, subwayToken, startTs, endTs,
                    RptUtils.Source.SUMMARY).call();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return false;
        }

        return true;
    }

    @Override
    public long getMaxUserUpdateVersion() {
        RptCustUpdateTs rptTs = RptCustUpdateTs.findByUserId(user.getId());
        log.info("[Found Current Version]" + rptTs);
        return rptTs == null ? 0L : rptTs.getLastUpdateTime();
    }

    @Override
    protected void updateUserUpdateVersion(long updateTs) {
        /**
         * update one day less.
         */
        RptCustUpdateTs.updateLastModifedTime(user.getId(), updateTs - DateUtil.DAY_MILLIS);
    }

    @Override
    protected void processAfterUpdate() {
        // TODO Auto-generated method stub

    }

}
