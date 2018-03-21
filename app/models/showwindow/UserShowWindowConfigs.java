
package models.showwindow;

import javax.persistence.Entity;
import javax.persistence.Id;

import models.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;

/**
 * @deprecated use the user type field instead...
 * @author zrb
 *
 */
@Entity(name = UserShowWindowConfigs.TABLE_NAME)
public class UserShowWindowConfigs extends GenericModel {

    private static final Logger log = LoggerFactory.getLogger(UserShowWindowConfigs.class);

    public static final String TAG = "橱窗";

    public static final String TABLE_NAME = "user_configs";

    public boolean isAutoShowWindowOn = false;

    @Id
    Long id;

    /**
     * Clear this when the logs is read...
     */
    public int msgCount;

    public static UserShowWindowConfigs findOrCreateByUser(User user) {
        UserShowWindowConfigs config = UserShowWindowConfigs.findById(user.getId());
        if (config == null) {
            config = new UserShowWindowConfigs(user.getId());
            config.save();
        }

        return config;
    }

    public UserShowWindowConfigs(Long id) {
        super();
        this.id = id;
        this.created = System.currentTimeMillis();
    }

    //  @Exclude
    public Long created;

//  @Exclude
    public Long updated;

    @Override
    public void _save() {
        updated = System.currentTimeMillis();
        super._save();
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public Long getUpdated() {
        return updated;
    }

    public void setUpdated(Long updated) {
        this.updated = updated;
    }

    public boolean isAutoShowWindowOn() {
        return isAutoShowWindowOn;
    }

    public void setAutoShowWindowOn(boolean isAutoShowWindowOn) {
        this.isAutoShowWindowOn = isAutoShowWindowOn;
    }

    /**
     * TODO we should add the window cache,对于那些在售宝贝已经全部在推荐的或者是没有开启
     * 同时，需要 监听用户的新增宝贝时间，来检测是否需要调整那些不需要调用的卖家
     * 对于status/js也需要缓存 加一手检测
     */

}
