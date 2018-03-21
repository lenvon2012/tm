package job.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.campaign.CampaignPlay;
import models.user.User;

import org.apache.commons.collections.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.jobs.Job;
import actions.CampaignAction;
import actions.CampaignAction.PlatFormInfo;
import bustbapi.CampaignApi;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.Campaign;
import com.taobao.api.domain.CampaignBudget;

import dao.campaign.CampaignDao;

public class SyncAllCampaignsJob extends Job<List<CampaignPlay>> {

    public final static Logger log = LoggerFactory.getLogger(SyncAllCampaignsJob.class);

    private User user;

    private String userNick;

    private boolean syncAllGroups = true;

    private boolean syncAllWords = true;

    List<CampaignPlay> campaigns = null;

    public SyncAllCampaignsJob(User user, String userNick) {
        super();
        this.user = user;
        this.userNick = userNick;
    }

    public SyncAllCampaignsJob(User user, String userNick, boolean syncAllGroups, boolean syncAllWords) {
        super();
        this.user = user;
        this.userNick = userNick;
        this.syncAllGroups = syncAllGroups;
        this.syncAllWords = syncAllWords;
    }

    @Override
    public List<CampaignPlay> doJobWithResult() {
        doJob();
        return this.campaigns;
    }

    @Override
    public void doJob() {
        this.campaigns = syncAllCampains();

        if (!syncAllGroups) {
            return;
        }
        for (CampaignPlay campaignPlay : campaigns) {
            //new SyncAllAdGroupsJob(user, userNick, campaignPlay, syncAllWords).doJob();
        }

        // TODO sync all on sale items....
    }

    List<CampaignPlay> syncAllCampains() {
        List<Campaign> remoteCampaigns = new CampaignApi.CampaignsGet(user, userNick).call();

        if (CommonUtils.isEmpty(remoteCampaigns)) {
            return ListUtils.EMPTY_LIST;
        }

        List<CampaignPlay> localCampaigns = CampaignDao.findbyNick(userNick);
        Map<Long, CampaignPlay> map = new HashMap<Long, CampaignPlay>();
        List<CampaignPlay> currActiveList = new ArrayList<CampaignPlay>();

        for (CampaignPlay campaignPlay : localCampaigns) {
            map.put(campaignPlay.getId(), campaignPlay);
        }

        for (Campaign campaign : remoteCampaigns) {
            Long id = campaign.getCampaignId();

            // update campaign budget
            CampaignBudget campaignBudget = new CampaignApi.CampaignBudgetGet(user.getSessionKey(),
                    campaign.getCampaignId()).call();


            // plat form
            PlatFormInfo platFormInfo = CampaignAction.getPlatForm(user.getSessionKey(), userNick, campaign.getCampaignId());
            
            int platForm = platFormInfo.getPlatForm();
            
            //地域
            String areaIds = "";
            String demographicIds = "";
            String addPrices = "";
            /*String areaIds = CampaignAction.getAreaIds(user, userNick, campaign.getCampaignId());
            if (!StringUtils.isEmpty(areaIds) && areaIds.length() > 980) {
                areaIds = "all";/////
            }
            //人群
            String demographicIds = StringUtils.EMPTY;
            String addPrices = "";
            try {
            	if (user.isCanAccurate() == true && platFormInfo.isHasNonSearchChannels() == true) {
            	    DemographicConfig config = CampaignAction.getDemographicIds(user, userNick, campaign.getCampaignId());
            	    if (config != null) {
            	        demographicIds = config.getDemographicIds();
            	        addPrices = config.getAddPrices();
            	    }
            	}
        	} catch (Exception e) {
        	    log.warn(e.getMessage(), e);
            }*/

            CampaignPlay campaignPlay = map.get(id);

            if (campaignPlay == null) {
                campaignPlay = new CampaignPlay(user.getId(), campaign);
                campaignPlay.updateBudget(campaignBudget);
                campaignPlay.setPlatForm(platForm);
                campaignPlay.setAreaIds(areaIds);
                campaignPlay.setDemographicIds(demographicIds);
                campaignPlay.setAddPrices(addPrices);
                campaignPlay.jdbcSave();
            } else {
                campaignPlay.updateWrapper(campaign);
                campaignPlay.updateBudget(campaignBudget);
                campaignPlay.setPlatForm(platForm);
                campaignPlay.setAreaIds(areaIds);
                campaignPlay.setDemographicIds(demographicIds);
                campaignPlay.setAddPrices(addPrices);
                campaignPlay.jdbcSave();
            }
            
            
            // if (campaignPlay.isActive()) {
            currActiveList.add(campaignPlay);
            // }

        }

        return currActiveList;
    }
}
