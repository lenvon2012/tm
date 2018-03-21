package models.updatetimestamp.updatets;

import javax.persistence.Entity;

import models.updatetimestamp.UserUpdateTimestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transaction.TransactionSecurity;

@Entity(name = "rpt_cust_update_ts")
public class RptCustUpdateTs extends UserUpdateTimestamp {
    /*
     * 还是使用jpa
     */
    private static final Logger log = LoggerFactory.getLogger(RptCustUpdateTs.class);

    public static final String TAG = "RptCustUpdateTs";

    public RptCustUpdateTs(Long userId) {
        super(userId);
    }

    public RptCustUpdateTs(Long userId, long ts) {
        super(userId, ts);
    }

    public static void updateLastModifedTime(final Long userId, final long ts) {

        new TransactionSecurity<Void>() {

            @Override
            public Void operateOnDB() {

                RptCustUpdateTs memberTs = RptCustUpdateTs.findByUserId(userId);
                if (memberTs == null) {
                    log.warn("No User Found...Create it now for id:" + userId);
                    new RptCustUpdateTs(userId, ts).save();
                    return null;
                }

                if (ts < memberTs.lastUpdateTime) {
                    log.warn("ts[" + ts + "] is less than [" + memberTs.lastUpdateTime + "], No Update");
                    return null;
                }

                memberTs.setLastUpdateTime(ts);
                memberTs.save();

//                log.info("save new update time successfully");
                return null;
            }
        }.execute();
    }

    public static RptCustUpdateTs findByUserId(final Long userId) {
        return new TransactionSecurity<RptCustUpdateTs>() {

            @Override
            public RptCustUpdateTs operateOnDB() {
                return RptCustUpdateTs.find("userId = ?", userId).first();
            }
        }.execute();
    }
}