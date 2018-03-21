
package models.updatetimestamp;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import play.db.jpa.GenericModel;

@MappedSuperclass
public class UserUpdateTimestamp extends GenericModel {

    @Id
    public Long userId;

    @Column(name = "first_ts")
    public long firstUpdateTime;

    @Column(name = "last_ts")
    public long lastUpdateTime;

    public UserUpdateTimestamp() {
        super();
    }

    public UserUpdateTimestamp(Long userId) {
        super();
        this.userId = userId;
        this.firstUpdateTime = System.currentTimeMillis();
    }

    public UserUpdateTimestamp(Long userId, long lastUpdateTime) {
        this(userId);
        this.lastUpdateTime = lastUpdateTime;
    }
    
    public UserUpdateTimestamp(Long userId, long firstUpdateTime, long lastUpdateTime) {
        this(userId);
        this.firstUpdateTime = firstUpdateTime;
        this.lastUpdateTime = lastUpdateTime;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
}
