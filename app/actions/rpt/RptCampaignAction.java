package actions.rpt;

import java.util.List;

import models.rpt.response.RptCampaignBase;
import models.rpt.response.RptCampaignEffect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bustbapi.rpt.RptCampaign;

public class RptCampaignAction {

    public final static Logger log = LoggerFactory.getLogger(RptCampaignAction.class);

    public static class SyncCampaignBase extends SyncRptCallable<RptCampaignBase> {

        Long campaignId;
        int searchType;

        public SyncCampaignBase(Long userId, String sid, String userNick, String subwayToken, Long campaignId, Long startTs,
                Long endTs, int source, int searchType) {
            super(userId, sid, userNick, subwayToken, startTs, endTs, source);
            this.searchType = searchType;
            this.campaignId = campaignId;
//            log.info(String.format(
//                    "userNick, subwayToken, campaignId, startTs, endTs, source, seartchType".replaceAll(", ", "=%s, ")
//                            + "=%s", userNick, subwayToken, campaignId, new Date(startTs), new Date(endTs), source,
//                    searchType));
        }

        @Override
        protected List<RptCampaignBase> getApiResult(long pageNo) {
            return new RptCampaign.CampaignBaseGet(sid, subwayToken, campaignId, startTs, endTs, source, searchType,
                    pageNo, userNick).call();
        }

        @Override
        protected boolean applyResult(List<RptCampaignBase> resList) {
            for (RptCampaignBase base : resList) {
                
//                log.info("Save RptCampaignBase for " + base.getCampaignId()+", " + new Date(base.getDateTime()));
                if (!base.jdbcSave()) {
                    return false;
                }
            }
            return true;
        }
    }

    public static class SyncCampaignEffect extends SyncRptCallable<RptCampaignEffect> {

        Long campaignId;
        int searchType;

        public SyncCampaignEffect(Long userId, String sid, String userNick, String subwayToken, Long campaignId, Long startTs,
                Long endTs, int source, int searchType) {
            super(userId, sid, userNick, subwayToken, startTs, endTs, source);
            this.searchType = searchType;
            this.campaignId = campaignId;
        }

        @Override
        protected List<RptCampaignEffect> getApiResult(long pageNo) {
            return new RptCampaign.CampaignEffectGet(sid, subwayToken, campaignId, startTs, endTs, source, searchType,
                    pageNo, userNick).call();

        }

        @Override
        protected boolean applyResult(List<RptCampaignEffect> resList) {
            for (RptCampaignEffect effect : resList) {
//                effect.jdbcSave();
                if (!RptCampaignBase.rawUpdateEffect(userId, sid, userNick, subwayToken, effect)) {
                    return false;
                }
            }
            return true;
        }
    }

 
}
