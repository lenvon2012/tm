package actions.rpt;

import java.util.List;

import models.rpt.response.RptCustBase;
import models.rpt.response.RptCustEffect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bustbapi.rpt.RptCust;

public class RptCustAction {

    public final static Logger log = LoggerFactory.getLogger(RptCustAction.class);

    public static class SyncCustBase extends SyncRptCallable<RptCustBase> {

        public SyncCustBase(Long userId, String sid, String userNick, String subwayToken, Long startTs, Long endTs, int source) {
            super(userId, sid, userNick, subwayToken, startTs, endTs, source);
        }

        @Override
        protected List<RptCustBase> getApiResult(long pageNo) {
            return new RptCust.CustBaseGet(sid, subwayToken, startTs, endTs, source, pageNo, userNick).call();
        }

        @Override
        protected boolean applyResult(List<RptCustBase> resList) {
            for (RptCustBase base : resList) {
                if (!base.jdbcSave()) {
                    return false;
                }
            }
            return true;
        }
    }

    public static class SyncCustEffect extends SyncRptCallable<RptCustEffect> {

        public SyncCustEffect(Long userId, String sid, String userNick, String subwayToken, Long startTs, Long endTs, int source) {
            super(userId, sid, userNick, subwayToken, startTs, endTs, source);
        }

        @Override
        protected List<RptCustEffect> getApiResult(long pageNo) {
            return new RptCust.CustEffectGet(sid, subwayToken, startTs, endTs, source, pageNo, userNick).call();
        }

        @Override
        protected boolean applyResult(List<RptCustEffect> resList) {
            for (RptCustEffect effect : resList) {
                // effect.jdbcSave();
                if (!RptCustBase.rawUpdateEffect(userId, sid, userNick, subwayToken, effect)) {
                    return false;
                }
            }
            return true;
        }
    }

}
