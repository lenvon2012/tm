
package actions;

import java.util.ArrayList;
import java.util.List;

import models.campaign.CampaignPlay;
import models.campaign.CampaignPlay.CampaignAreaUtil;
import models.user.User;
import bustbapi.CampaignApi;
import bustbapi.CampaignApi.CampaignUpdate;
import bustbapi.NonSearchApi;

import com.ciaosir.client.CommonUtils;
import com.taobao.api.domain.Campaign;
import com.taobao.api.domain.CampaignArea;
import com.taobao.api.domain.CampaignPlatform;
import com.taobao.api.domain.DemographicSetting;

public class CampaignAction {

    public static String setOnlineStatus(User user, String userNick, CampaignPlay setCampaign, String onlineStatus) {

        CampaignUpdate updateAPI = new CampaignApi.CampaignUpdate(user, setCampaign.getCampaignId(), userNick,
                setCampaign.getTitle(), onlineStatus);
        Campaign remote = updateAPI.call();
        if (remote != null) {
            setCampaign.updateWrapper(remote);
            return setCampaign.rawUpdate() ? "true" : "更新推广计划状态失败，请联系客服~";
        }
        return updateAPI.getErrorMsg();
    }
    
    public static Campaign setOnlineStatusWithResult(User user, String userNick, CampaignPlay setCampaign, String onlineStatus) {

        Campaign remote = new CampaignApi.CampaignUpdate(user, setCampaign.getCampaignId(), userNick,
                setCampaign.getTitle(), onlineStatus).call();

        return remote;
    }
    
    
    public static class PlatFormInfo {
        private int platForm;
        private boolean hasNonSearchChannels;
        public int getPlatForm() {
            return platForm;
        }
        public void setPlatForm(int platForm) {
            this.platForm = platForm;
        }
        public boolean isHasNonSearchChannels() {
            return hasNonSearchChannels;
        }
        public void setHasNonSearchChannels(boolean hasNonSearchChannels) {
            this.hasNonSearchChannels = hasNonSearchChannels;
        }
        public PlatFormInfo(int platForm, boolean hasNonSearchChannels) {
            super();
            this.platForm = platForm;
            this.hasNonSearchChannels = hasNonSearchChannels;
        }
        
        
        
    }

    public static PlatFormInfo getPlatForm(String sid, String nick, Long campaignId) {

        CampaignPlatform campaignPlatForm = new CampaignApi.CampaignPlatformGet(sid, campaignId, nick).call();

        int platForm = 0;
        boolean hasNonSearchChannels = false;
        if (campaignPlatForm != null) {

            List<Long> nonSearchChannels = campaignPlatForm.getNonsearchChannels();
            if (!CommonUtils.isEmpty(nonSearchChannels)) {
                for (Long channel : nonSearchChannels) {
                    platForm |= channel;
                }
                hasNonSearchChannels = true;
            }
            List<Long> searchChannels = campaignPlatForm.getSearchChannels();
            if (!CommonUtils.isEmpty(searchChannels)) {
                for (Long channel : searchChannels) {
                    platForm |= channel;
                }
            }
        }
        PlatFormInfo platFormInfo = new PlatFormInfo(platForm, hasNonSearchChannels);
        return platFormInfo;
    }

    //地域
    public static String getAreaIds(User user, String nick, Long campaignId) {
        CampaignArea campaignArea = new CampaignApi.CampaignAreaGet(user, campaignId, nick).call();
        if (campaignArea == null) {
            return CampaignAreaUtil.AllArea;
        }
        return campaignArea.getArea();
    }

    //人群
    public static DemographicConfig getDemographicIds(User user, String nick, Long campaignId) {
        List<DemographicSetting> demographicSettingList = new NonSearchApi.DemographicsGet(user, campaignId, nick)
                .call();
        DemographicConfig config = new DemographicConfig();
        
        if (CommonUtils.isEmpty(demographicSettingList))
            demographicSettingList = new ArrayList<DemographicSetting>();
        String demographicIds = "";
        String addPrices = "";
        for (DemographicSetting demographicSetting : demographicSettingList) {
        	Long addPrice = demographicSetting.getIncrementalPrice();
        	if (addPrice == null || addPrice.longValue() <= 0) {
        		continue;
        	}
            long demographicId = demographicSetting.getDemographicId();
            if (!demographicIds.equals("")) {
                demographicIds += ",";
                addPrices += ",";
            }
            demographicIds += demographicId;
            addPrices += addPrice;
        }
        config.setDemographicIds(demographicIds);
        config.setAddPrices(addPrices);
        return config;
    }
    
    public static class DemographicConfig {
        private String demographicIds;
        private String addPrices;
        public String getDemographicIds() {
            return demographicIds;
        }
        public void setDemographicIds(String demographicIds) {
            this.demographicIds = demographicIds;
        }
        public String getAddPrices() {
            return addPrices;
        }
        public void setAddPrices(String addPrices) {
            this.addPrices = addPrices;
        }
        
        
    }
    
}
