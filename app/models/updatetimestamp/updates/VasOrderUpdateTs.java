package models.updatetimestamp.updates;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.TransactionSecurity;

@Entity(name = "vasorder_update_ts")
public class VasOrderUpdateTs extends GenericModel {

    private static final Logger log = LoggerFactory.getLogger(VasOrderUpdateTs.class);

    public static final String TAG = "VasOrderUpdateTs";

    @Id
    public String articleCode;

    @Column(name = "first_ts")
    public long firstUpdateTime;

    @Column(name = "last_ts")
    public long lastUpdateTime;

    public VasOrderUpdateTs(String articleCode) {
        this.articleCode = articleCode;
        this.firstUpdateTime = System.currentTimeMillis();
    }

    public VasOrderUpdateTs(String articleCode, long ts) {
        this.articleCode = articleCode;
        this.firstUpdateTime = System.currentTimeMillis();
        this.lastUpdateTime = ts;
    }

    public static void updateLastModifedTime(String articleCode, long ts) {

        VasOrderUpdateTs memberTs = VasOrderUpdateTs.findByArticleCode(articleCode);
        if (memberTs == null) {
            log.warn("No User Found...Create it now for id:" + articleCode);
            new VasOrderUpdateTs(articleCode, ts).save();

            return;
        }

        if (ts < memberTs.lastUpdateTime) {
            log.warn("ts[" + ts + "] is less than [" + memberTs.lastUpdateTime + "], No Update");
            return;
        }

        memberTs.setLastUpdateTime(ts);
        memberTs.save();

//        log.info("save new update time successfully");
    }

    public static VasOrderUpdateTs findByArticleCode(final String articleCode) {
        return new TransactionSecurity<VasOrderUpdateTs>() {

            @Override
            public VasOrderUpdateTs operateOnDB() {
                return VasOrderUpdateTs.find("articleCode = ?", articleCode).first();
            }
        }.execute();
    }

    public String getArticleCode() {
        return articleCode;
    }

    public void setArticleCode(String articleCode) {
        this.articleCode = articleCode;
    }

    public long getFirstUpdateTime() {
        return firstUpdateTime;
    }

    public void setFirstUpdateTime(long firstUpdateTime) {
        this.firstUpdateTime = firstUpdateTime;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public static String getTag() {
        return TAG;
    }

}