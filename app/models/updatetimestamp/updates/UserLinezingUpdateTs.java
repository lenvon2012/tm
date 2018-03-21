
package models.updatetimestamp.updates;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import transaction.TransactionSecurity;

@Entity(name = "userlinezing_update_ts")
public class UserLinezingUpdateTs extends GenericModel {

    private static final Logger log = LoggerFactory.getLogger(UserLinezingUpdateTs.class);

    public static final String TAG = "UserLinezingUpdateTs";

    @Id
    public Long userId;

    @Column(name = "first_ts")
    public long firstUpdateTime;

    @Column(name = "last_ts")
    public long lastUpdateTime;

    public UserLinezingUpdateTs(Long articleCode) {
        this.userId = articleCode;
        this.firstUpdateTime = System.currentTimeMillis();
    }

    public UserLinezingUpdateTs(Long articleCode, long ts) {
        this.userId = articleCode;
        this.firstUpdateTime = System.currentTimeMillis();
        this.lastUpdateTime = ts;
    }

    public static void updateLastModifedTime(Long userId, long ts) {

        UserLinezingUpdateTs memberTs = UserLinezingUpdateTs.findByArticleCode(userId);
        if (memberTs == null) {
            log.warn("No User Found...Create it now for id:" + userId);
            new UserLinezingUpdateTs(userId, ts).save();

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

    public static UserLinezingUpdateTs findByArticleCode(final Long userId) {
        return new TransactionSecurity<UserLinezingUpdateTs>() {
            @Override
            public UserLinezingUpdateTs operateOnDB() {
                return UserLinezingUpdateTs.findById(userId);
            }
        }.execute();
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
