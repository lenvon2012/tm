package bustbapi.rpt;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import models.rpt.response.RptCustBase;
import models.rpt.response.RptCustEffect;
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
import com.taobao.api.request.SimbaRptCustbaseGetRequest;
import com.taobao.api.request.SimbaRptCusteffectGetRequest;
import com.taobao.api.response.SimbaRptCustbaseGetResponse;
import com.taobao.api.response.SimbaRptCusteffectGetResponse;

import configs.BusConfigs;

public class RptCust {

    public final static Logger log = LoggerFactory.getLogger(RptCust.class);

    public static class CustBaseGet extends
            TBApi<SimbaRptCustbaseGetRequest, SimbaRptCustbaseGetResponse, List<RptCustBase>> {

        public String subwayToken;
        public String nick;
        public long startTime;
        public long endTime;
        public long pageNo;
        /**
         * 站内：1，站外：2 ，汇总：SUMMARY）SUMMARY必须单选，其他值可多选例如1,2
         */
        public int source;

        public CustBaseGet(String sid, String subwayToken, long startTime, long endTime, int source, long pageNo) {
            this(sid, subwayToken, startTime, endTime, source, pageNo, null);
        }

        public CustBaseGet(String sid, String subwayToken, long startTime, long endTime, int source, long pageNo,
                String nick) {
            super(sid);
            this.subwayToken = subwayToken;
            this.startTime = startTime;
            this.endTime = endTime;

            this.source = source;
            this.pageNo = pageNo;
            this.nick = nick;
        }

        @Override
        public SimbaRptCustbaseGetRequest prepareRequest() {
            SimbaRptCustbaseGetRequest req = new SimbaRptCustbaseGetRequest();

            req.setSubwayToken(subwayToken);
            if (nick != null) {
                req.setNick(nick);
            }

            req.setSource(RptUtils.getSourceStr(source));

            long maxReachableDay = DateUtil.formCurrDate() - BusConfigs.RptConfig.MAX_CUST_RPT_GET
                    * DateUtil.DAY_MILLIS;
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

            req.setPageSize(BusConfigs.PageSize.CUSTBASE_PAGE_SIZE);
            req.setPageNo(pageNo);

            return req;
        }

        @Override
        public List<RptCustBase> validResponse(SimbaRptCustbaseGetResponse resp) {
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
        public List<RptCustBase> applyResult(List<RptCustBase> res) {
            return res;
        }

        public List<RptCustBase> parse(String str) {
            if (!RptChecker.getInstance().checkVaild(str, RptChecker.Type.CUST_BASE)) {
//                log.error("The RptCustBase is null!!!");
                this.stopRetry();
                return null;
            }
            try {
                RptCustBaseResp readValue = JsonUtil.mapper.readValue(str, RptCustBaseResp.class);
                return readValue.getCustBaseList().getRptCustBase();
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
        public static class RptCustBaseResp {
            @JsonProperty
            CustBaseList simba_rpt_custbase_get_response;

            @JsonAutoDetect
            public static class CustBaseList {

                @JsonProperty
                List<RptCustBase> rpt_cust_base_list;

                public List<RptCustBase> getRptCustBase() {
                    return rpt_cust_base_list;
                }

                @Override
                public String toString() {
                    return "CustBaseList [rpt_cust_base_list=" + rpt_cust_base_list + "]";
                }
            }

            public CustBaseList getCustBaseList() {
                return simba_rpt_custbase_get_response;
            }

            @Override
            public String toString() {
                return "RptCustBaseResp [simba_rpt_custbase_get_response=" + simba_rpt_custbase_get_response + "]";
            }
        }

    }

    public static class CustEffectGet extends
            TBApi<SimbaRptCusteffectGetRequest, SimbaRptCusteffectGetResponse, List<RptCustEffect>> {

        public String subwayToken;
        public String nick;
        public long startTime;
        public long endTime;
        public long pageNo;
        /**
         * 站内：1，站外：2 ，汇总：SUMMARY）SUMMARY必须单选，其他值可多选例如1,2
         */
        public int source;

        public CustEffectGet(String sid, String subwayToken, long startTime, long endTime, int source, long pageNo) {
            this(sid, subwayToken, startTime, endTime, source, pageNo, null);
        }

        public CustEffectGet(String sid, String subwayToken, long startTime, long endTime, int source, long pageNo,
                String nick) {
            super(sid);
            this.subwayToken = subwayToken;
            this.startTime = startTime;
            this.endTime = endTime;

            this.source = source;
            this.pageNo = pageNo;
            this.nick = nick;
        }

        @Override
        public SimbaRptCusteffectGetRequest prepareRequest() {
            SimbaRptCusteffectGetRequest req = new SimbaRptCusteffectGetRequest();

            req.setSubwayToken(subwayToken);
            if (nick != null) {
                req.setNick(nick);
            }

            req.setSource(RptUtils.getSourceStr(source));

            long maxReachableDay = DateUtil.formCurrDate() - BusConfigs.RptConfig.MAX_CUST_RPT_GET
                    * DateUtil.DAY_MILLIS;
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

            req.setPageSize(BusConfigs.PageSize.CUSTBASE_PAGE_SIZE);
            req.setPageNo(pageNo);

            return req;
        }

        @Override
        public List<RptCustEffect> validResponse(SimbaRptCusteffectGetResponse resp) {
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
        public List<RptCustEffect> applyResult(List<RptCustEffect> res) {
            return res;
        }

        public List<RptCustEffect> parse(String str) {

            if (!RptChecker.getInstance().checkVaild(str, RptChecker.Type.CUST_EFFECT)) {
//                log.error("The RptCustEffect is null!!!");
                this.stopRetry();
                return null;
            }
            try {
                RptCustEffectResp readValue = JsonUtil.mapper.readValue(str, RptCustEffectResp.class);
                return readValue.getCustEffectList().getRptCustEffect();
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
        public static class RptCustEffectResp {

            @JsonProperty
            CustEffectList simba_rpt_custeffect_get_response;

            @JsonAutoDetect
            public static class CustEffectList {
                @JsonProperty
                List<RptCustEffect> rpt_cust_effect_list;

                public List<RptCustEffect> getRptCustEffect() {
                    return rpt_cust_effect_list;
                }

                @Override
                public String toString() {
                    return "ListResponse [rpt_cust_effect_list=" + rpt_cust_effect_list + "]";
                }
            }

            public CustEffectList getCustEffectList() {
                return simba_rpt_custeffect_get_response;
            }

            @Override
            public String toString() {
                return "EffectJson [simba_rpt_custeffect_get_response=" + simba_rpt_custeffect_get_response + "]";
            }
        }
    }
}
