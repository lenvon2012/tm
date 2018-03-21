package bustbapi.rpt;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import models.rpt.response.RptCampaignBase;
import models.rpt.response.RptCampaignEffect;
import models.rpt.response.RptUtils;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bustbapi.ErrorHandler;
import bustbapi.TBApi;

import com.ciaosir.client.utils.DateUtil;
import com.ciaosir.client.utils.JsonUtil;
import com.taobao.api.request.SimbaRptCampaignbaseGetRequest;
import com.taobao.api.request.SimbaRptCampaigneffectGetRequest;
import com.taobao.api.response.SimbaRptCampaignbaseGetResponse;
import com.taobao.api.response.SimbaRptCampaigneffectGetResponse;

import configs.BusConfigs;

public class RptCampaign {
    public final static Logger log = LoggerFactory.getLogger(RptCampaign.class);

    public static class CampaignEffectGet extends
            TBApi<SimbaRptCampaigneffectGetRequest, SimbaRptCampaigneffectGetResponse, List<RptCampaignEffect>> {

        public String nick;
        public Long campaignId;
        public long startTime;
        public long endTime;

        /**
         * 站内：1，站外：2 ，汇总：SUMMARY）SUMMARY必须单选，其他值可多选例如1,2
         */
        public int source;

        public String subwayToken;

        public long pageNo;

        /**
         * 报表类型（搜索：SEARCH,类目出价：CAT, 定向投放：NOSEARCH）可多选例如：SEARCH,CAT
         */
        public int searchType;

        public CampaignEffectGet(String sid, String subwayToken, Long campaignId, long startTime, long endTime,
                int source, int searchType, long pageNo) {
            this(sid, subwayToken, campaignId, startTime, endTime, source, searchType, pageNo, null);
        }

        public CampaignEffectGet(String sid, String subwayToken, Long campaignId, long startTime, long endTime,
                int source, int searchType, long pageNo, String nick) {
            super(sid);
            this.subwayToken = subwayToken;
            this.campaignId = campaignId;

            this.startTime = startTime;
            this.endTime = endTime;

            this.source = source;
            this.pageNo = pageNo;
            this.searchType = searchType;
            this.nick = nick;
        }

        @Override
        public SimbaRptCampaigneffectGetRequest prepareRequest() {
            SimbaRptCampaigneffectGetRequest req = new SimbaRptCampaigneffectGetRequest();

            req.setSubwayToken(subwayToken);
            if (nick != null) {
                req.setNick(nick);
            }

            req.setCampaignId(campaignId);

            long maxReachableDay = DateUtil.formCurrDate() - BusConfigs.RptConfig.MAX_RPT_GET * DateUtil.DAY_MILLIS;
            if (startTime < maxReachableDay) {
                long originStartTime = startTime;
                startTime = maxReachableDay;
                log.error("StartTime is out of maxReachableDay, origin startTime:" + originStartTime + ", reset time:"
                        + startTime);
            }
            if (endTime < startTime || endTime > DateUtil.formYestadyMillis()) {
                log.error("EndTime is latter than startTime, origin startTime:" + startTime + ", endTime:" + endTime);
                endTime = DateUtil.formCurrDate() - DateUtil.DAY_MILLIS;
            }

            req.setStartTime(new SimpleDateFormat("yyyy-MM-dd").format(new Date(startTime)));
            req.setEndTime(new SimpleDateFormat("yyyy-MM-dd").format(new Date(endTime)));

            req.setSource(RptUtils.getSourceStr(source));
            req.setSearchType(RptUtils.getSearchTypeStr(searchType));

            req.setPageSize(BusConfigs.PageSize.ADGROUPKEYWORDEFFECT_PAGE_SIZE);
            req.setPageNo(pageNo);
            return req;
        }

        @Override
        public List<RptCampaignEffect> validResponse(SimbaRptCampaigneffectGetResponse resp) {
            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            ErrorHandler.validTaoBaoResp(resp);
//            log.info(resp.getBody());

            if (!resp.isSuccess()) {
                return null;
            }
            return parse(resp.getBody());
        }

        @Override
        public List<RptCampaignEffect> applyResult(List<RptCampaignEffect> res) {
            return res;
        }

        public List<RptCampaignEffect> parse(String str) {
            if (!RptChecker.getInstance().checkVaild(str, RptChecker.Type.CAMPAIGN_EFFECT)) {
//                log.error("The RptCampaignAdgroupEffect is null!!!");
                this.stopRetry();
                return null;
            }
            try {
                RptCampaignEffectResp readValue = JsonUtil.mapper.readValue(str, RptCampaignEffectResp.class);
                return readValue.getCampaignEffectList().getRptCampaignEffect();
            } catch (JsonParseException e) {
                log.info(e.getMessage(), e);
            } catch (JsonMappingException e) {
                log.info(e.getMessage(), e);
            } catch (IOException e) {
                log.info(e.getMessage(), e);
            }
            return null;
        }

        @JsonAutoDetect
        public static class RptCampaignEffectResp {

            @JsonProperty
            CampaignEffectList simba_rpt_campaigneffect_get_response;

            @JsonAutoDetect
            public static class CampaignEffectList {

                @JsonProperty
                List<RptCampaignEffect> rpt_campaign_effect_list;

                public List<RptCampaignEffect> getRptCampaignEffect() {
                    return rpt_campaign_effect_list;
                }

                @Override
                public String toString() {
                    return "CampaignEffectList [rpt_campaign_effect_list=" + rpt_campaign_effect_list + "]";
                }

            }

            public CampaignEffectList getCampaignEffectList() {
                return simba_rpt_campaigneffect_get_response;
            }

            @Override
            public String toString() {
                return "RptCampaignEffectResp [simba_rpt_campaigneffect_get_response="
                        + simba_rpt_campaigneffect_get_response + "]";
            }
        }
    }

    public static class CampaignBaseGet extends
            TBApi<SimbaRptCampaignbaseGetRequest, SimbaRptCampaignbaseGetResponse, List<RptCampaignBase>> {

        public String nick;
        public Long campaignId;
        public long startTime;
        public long endTime;

        /**
         * 站内：1，站外：2 ，汇总：SUMMARY）SUMMARY必须单选，其他值可多选例如1,2
         */
        public int source;

        public String subwayToken;

        public long pageNo;

        /**
         * 报表类型（搜索：SEARCH,类目出价：CAT, 定向投放：NOSEARCH）可多选例如：SEARCH,CAT
         */
        public int searchType;

        public CampaignBaseGet(String sid, String subwayToken, Long campaignId, long startTime, long endTime,
                int source, int searchType, long pageNo) {
            this(sid, subwayToken, campaignId, startTime, endTime, source, searchType, pageNo, null);
        }

        public CampaignBaseGet(String sid, String subwayToken, Long campaignId, long startTime, long endTime,
                int source, int searchType, long pageNo, String nick) {
            super(sid);
            this.subwayToken = subwayToken;
            this.campaignId = campaignId;

            this.startTime = startTime;
            this.endTime = endTime;

            this.source = source;
            this.pageNo = pageNo;
            this.searchType = searchType;
            this.nick = nick;
        }

        @Override
        public SimbaRptCampaignbaseGetRequest prepareRequest() {
            SimbaRptCampaignbaseGetRequest req = new SimbaRptCampaignbaseGetRequest();

            req.setSubwayToken(subwayToken);
            if (nick != null) {
                req.setNick(nick);
            }

            req.setCampaignId(campaignId);

            long maxReachableDay = DateUtil.formCurrDate() - BusConfigs.RptConfig.MAX_RPT_GET * DateUtil.DAY_MILLIS;
            if (startTime < maxReachableDay) {
                long originStartTime = startTime;
                startTime = maxReachableDay;
                log.error("StartTime is out of maxReachableDay, origin startTime:" + originStartTime + ", reset time:"
                        + startTime);
            }
            if (endTime < startTime || endTime > DateUtil.formYestadyMillis()) {
                log.error("EndTime is latter than startTime, origin startTime:" + startTime + ", endTime:" + endTime);
                endTime = DateUtil.formCurrDate() - DateUtil.DAY_MILLIS;
            }

            req.setStartTime(new SimpleDateFormat("yyyy-MM-dd").format(new Date(startTime)));
            req.setEndTime(new SimpleDateFormat("yyyy-MM-dd").format(new Date(endTime)));

            req.setSource(RptUtils.getSourceStr(source));

            req.setSearchType(RptUtils.getSearchTypeStr(searchType));

            req.setPageSize(BusConfigs.PageSize.ADGROUPKEYWORDEFFECT_PAGE_SIZE);
            req.setPageNo(pageNo);
            return req;
        }

        @Override
        public List<RptCampaignBase> validResponse(SimbaRptCampaignbaseGetResponse resp) {
            if (resp == null) {
                log.error("Null Resp Returned");
                return null;
            }

            ErrorHandler.validTaoBaoResp(resp);
//            log.info(resp.getBody());

            if (!resp.isSuccess()) {
                return null;
            }

            return parse(resp.getBody());
        }

        @Override
        public List<RptCampaignBase> applyResult(List<RptCampaignBase> res) {
            return res;
        }

        public List<RptCampaignBase> parse(String str) {
            if (!RptChecker.getInstance().checkVaild(str, RptChecker.Type.CAMPAIGN_BASE)) {
//                log.error("The RptCampaignBase is null!!!");
                this.stopRetry();
                return null;
            }

            RptCampaignBaseResp readValue;
            try {
                readValue = JsonUtil.mapper.readValue(str, RptCampaignBaseResp.class);
                return readValue.getCampaignBaseList().getRptCampaignBase();
            } catch (JsonParseException e) {
                log.info(e.getMessage(), e);
            } catch (JsonMappingException e) {
                log.info(e.getMessage(), e);
            } catch (IOException e) {
                log.info(e.getMessage(), e);
            }
            return null;
        }

        @JsonAutoDetect
        public static class RptCampaignBaseResp {

            @JsonProperty
            CampaignBaseList simba_rpt_campaignbase_get_response;

            @JsonAutoDetect
            public static class CampaignBaseList {

                @JsonProperty
                List<RptCampaignBase> rpt_campaign_base_list;

                public List<RptCampaignBase> getRptCampaignBase() {
                    return rpt_campaign_base_list;
                }

                @Override
                public String toString() {
                    return "CampaignBaseList [rpt_campaign_base_list=" + rpt_campaign_base_list + "]";
                }
            }

            public CampaignBaseList getCampaignBaseList() {
                return simba_rpt_campaignbase_get_response;
            }

            @Override
            public String toString() {
                return "RptCampaignBaseResp [simba_rpt_campaignbase_get_response="
                        + simba_rpt_campaignbase_get_response + "]";
            }
        }
    }

}
