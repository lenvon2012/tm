package job.sync.rpt;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import job.ApplicationStopJob;
import job.sync.rpt.process.RptCampaignProcessExecutor;
import models.campaign.CampaignPlay;
import models.rpt.response.RptUtils;
import models.updatetimestamp.updatets.RptCampaignUpdateTs;
import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.DateUtil;
import actions.rpt.RptCampaignAction;

import com.ciaosir.client.CommonUtils;
import com.ciaosir.client.PYFutureTaskPool;

import configs.BusConfigs;

public class SyncRptCampaignJob extends SyncRptJob {

    public final static Logger log = LoggerFactory.getLogger(SyncRptCampaignJob.class);

    private List<CampaignPlay> campaignList;

    static PYFutureTaskPool<Void> pool = new PYFutureTaskPool<Void>(4);

    static {
        ApplicationStopJob.addShutdownPool(pool);
    }


    public SyncRptCampaignJob(User user, String userNick, List<CampaignPlay> campaignList, Long endTs) {
        this(user, userNick, campaignList, -1L, endTs, null);
    }

    public SyncRptCampaignJob(User user, String userNick, List<CampaignPlay> campaignList, Long endTs,
            String subwayToken) {
        this(user, userNick, campaignList, -1L, endTs, subwayToken);
    }

    public SyncRptCampaignJob(User user, String userNick, List<CampaignPlay> campaignList, Long startTs, Long endTs) {
        this(user, userNick, campaignList, startTs, endTs, null);
    }

    public SyncRptCampaignJob(User user, String userNick, List<CampaignPlay> campaignList, Long startTs, Long endTs,
            String subwayToken) {
        super(user, userNick, startTs, endTs, subwayToken);
        this.campaignList = campaignList;
        this.subwayToken = subwayToken == null ? getSubwayToken() : subwayToken;
    }

    @Override
    public long getMaxReachable() {
        return BusConfigs.RptConfig.MAX_RPT_GET;
    }

    @Override
    public boolean requestUpdate(long start, long end) {

        log.warn("start sync rpt campaigns for user: " + userNick + "-----------------");
        
        if (!syncRptBase(start, end)) {
            return false;
        }

        return syncRptEffect(start, end);
    }

    public boolean syncRptBase(long startTs, long endTs) {
        for (CampaignPlay campaign : campaignList) {

            try {
                new RptCampaignAction.SyncCampaignBase(user.getId(), user.getSessionKey(), userNick, subwayToken,
                        campaign.getCampaignId(), startTs, endTs, RptUtils.Source.SUMMARY, RptUtils.SearchType.SUMMARY)
                        .call();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
                return false;
            }
        }
        return true;
    }

    public boolean syncRptEffect(long startTs, long endTs) {
//        log.warn("sync rpt effect startTs:" + startTs + ", endTs:" + endTs);

        for (CampaignPlay campaign : campaignList) {

            try {
                new RptCampaignAction.SyncCampaignEffect(user.getId(), user.getSessionKey(), userNick, subwayToken,
                        campaign.getCampaignId(), startTs, endTs, RptUtils.Source.SUMMARY, RptUtils.SearchType.SUMMARY)
                        .call();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
                return false;
            }
        }

        return true;
    }

    public boolean syncRptBase_(long startTs, long endTs) {

        List<FutureTask<Void>> promises = new ArrayList<FutureTask<Void>>();

        for (CampaignPlay campaign : campaignList) {
            FutureTask<Void> promise = pool.submit(new RptCampaignAction.SyncCampaignBase(user.getId(), user
                    .getSessionKey(), userNick, subwayToken, campaign.getCampaignId(), startTs, endTs,
                    RptUtils.Source.SUMMARY, RptUtils.SearchType.SUMMARY));
            promises.add(promise);
        }

        for (FutureTask<Void> promise : promises) {
            try {
                promise.get();
            } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
                return false;
            } catch (ExecutionException e) {
                log.warn(e.getMessage(), e);
                return false;
            }
        }

        return true;

    }

    public boolean syncRptEffect_(long startTs, long endTs) {

        List<FutureTask<Void>> promises = new ArrayList<FutureTask<Void>>();

        for (CampaignPlay campaign : campaignList) {
            FutureTask<Void> promise = pool.submit(new RptCampaignAction.SyncCampaignEffect(user.getId(), user
                    .getSessionKey(), userNick, subwayToken, campaign.getCampaignId(), startTs, endTs,
                    RptUtils.Source.SUMMARY, RptUtils.SearchType.SUMMARY));
            promises.add(promise);
        }

        for (FutureTask<Void> promise : promises) {
            try {
                promise.get();
            } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
                return false;
            } catch (ExecutionException e) {
                log.warn(e.getMessage(), e);
                return false;
            }
        }

        return true;

    }

    @Override
    public long getMaxUserUpdateVersion() {

        RptCampaignUpdateTs rptTs = RptCampaignUpdateTs.findByUserId(user.getId());
        log.info("[Found Current Version]" + rptTs);

        return rptTs == null ? 0L : rptTs.getLastUpdateTime();
    }

    @Override
    protected void updateUserUpdateVersion(long updateTs) {
        RptCampaignUpdateTs.updateLastModifedTime(user.getId(), updateTs - DateUtil.DAY_MILLIS);
    }

    @Override
    protected void processAfterUpdate() {

        if (CommonUtils.isEmpty(campaignList)) {
            log.warn("No campaign records for " + userNick);
            return;
        }

        RptCampaignProcessExecutor.addObject(user.getId(), userNick, campaignList, endTs);

//        for (CampaignPlay campaign : campaignList) {
//            RptCampaignDao.calculatePojoByCampaignId(user.getId(), userNick, campaign, endTs, 1);
//            RptCampaignDao.calculatePojoByCampaignId(user.getId(), userNick, campaign, endTs, 3);
//            RptCampaignDao.calculatePojoByCampaignId(user.getId(), userNick, campaign, endTs, 7);
//        }
    }

}
