package job.sync.rpt;

import java.util.List;

import models.campaign.CampaignPlay;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import bustbapi.LoginApi;

import com.ciaosir.client.CommonUtils;

import configs.BusConfigs;
import dao.campaign.CampaignDao;

public class SyncAllRptJob extends Job {

    public final static Logger log = LoggerFactory.getLogger(SyncAllRptJob.class);

    private User user;
    private String userNick;
    private Long startTs;
    private Long endTs;
    private String subwayToken = StringUtils.EMPTY;

    private boolean syncCust = false;
    private boolean syncCampaign = false;

    public SyncAllRptJob(User user, String userNick, Long endTs, boolean syncCust, boolean syncCampaign) {
        this(user, userNick, -1L, endTs, syncCust, syncCampaign);
    }

    public SyncAllRptJob(User user, String userNick, Long startTs, Long endTs, boolean syncCust, boolean syncCampaign) {
        super();
        this.user = user;
        this.userNick = userNick;
        this.startTs = startTs;
        this.endTs = endTs;
        this.subwayToken = getSubwayToken();
        this.syncCust = syncCust;
        this.syncCampaign = syncCampaign;
    }

    public String getSubwayToken() {
        return new LoginApi.AuthsignGetApi(user.getSessionKey(), userNick).call();
    }

    @Override
    public void doJob() throws Exception {

        if (!BusConfigs.RPT_ENABLE) {
            log.info("This appkey does not have rpt permission!!! ");
        }

        /**
         * sync cust rpt
         */
        if (syncCust) {
            new SyncRptCustJob(user, userNick, startTs, endTs, subwayToken).call();
        }

        /**
         * sync campaign rpt
         */
        List<CampaignPlay> campaignList = CampaignDao.findbyNick(userNick);

        if (CommonUtils.isEmpty(campaignList)) {
            log.warn("No campaign got in db for " + user.getUserNick() + ", " + user.getId() + "!!!");
            return;
        }

        if (syncCampaign) {
            new SyncRptCampaignJob(user, userNick, campaignList, startTs, endTs, subwayToken).call();
        }

        
    }

}
