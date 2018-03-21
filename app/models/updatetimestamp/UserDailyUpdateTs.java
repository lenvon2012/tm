
package models.updatetimestamp;

import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.Index;

import play.db.jpa.Model;

@MappedSuperclass
public class UserDailyUpdateTs extends Model {

    @Index(name = "userId")
    public Long userId;

    @Index(name = "ts")
    public Long ts;

    public long createAt;

    public long updateAt;

    public UserDailyUpdateTs(Long userId, Long ts) {
        this.userId = userId;
        this.ts = ts;

    }

    public void _save() {
        this.updateAt = System.currentTimeMillis();
        if (this.createAt <= 0L) {

            this.createAt = System.currentTimeMillis();
        }

        super._save();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(long createAt) {
        this.createAt = createAt;
    }

    public long getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(long updateAt) {
        this.updateAt = updateAt;
    }

}
