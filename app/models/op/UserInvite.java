
package models.op;

import static java.lang.String.format;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import models.oplog.TMErrorLog;
import models.user.User;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;
import result.TMResult.TMListResult;

import com.ciaosir.client.pojo.PageOffset;
import com.ciaosir.client.utils.NumberUtil;

import dao.UserDao;

@Entity(name = UserInvite.TABLE_NAME)
@JsonIgnoreProperties(value = {
        "userId", "entityId", "cid", "ts", "numIid", "detailURL", "sellerCids", "tableHashKey", "persistent",
        "tableName", "idName", "idColumn", "propsName", "maxKeywordAllowPrice"
})
public class UserInvite extends GenericModel {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(UserInvite.class);

    @Transient
    public static final String TAG = "UserInvite";

    @Transient
    public static final String TABLE_NAME = "user_invite";

    /**
     * target user id;
     */
    @JsonProperty
    @Id
    Long id;

    @JsonProperty
    String nick = StringUtils.EMPTY;

    @JsonProperty
    String thisIp;

    /**
     * src user id 
     */
    @JsonProperty
    @Index(name = "srcId")
    Long srcUid = 0L;

    String srcUserNick = StringUtils.EMPTY;

    @JsonProperty
    String srcIp = null;

    @JsonProperty
    long created;

    public static void ensure(User user, Long srcUid, String targetIp) {
        
        log.info(format("ensure:user, srcUid, targetIp".replaceAll(", ", "=%s, ") + "=%s", user, srcUid, targetIp));

        String errorContent = null;
        Long targetUid = user.getId();
        if (NumberUtil.isNullOrZero(targetUid) || NumberUtil.isNullOrZero(srcUid)) {
            errorContent = format("no target uid or src uid for : ensure:targetUid, srcUid".replaceAll(", ", "=%s, ")
                    + "=%s", targetUid, srcUid);
            new TMErrorLog(errorContent).save();
            return;
        }

        UserInvite model = UserInvite.findById(targetUid);
        if (model == null) {
            String srcIp = UserRecentLogin.userRecent(user.getId());
            new UserInvite(targetUid, user.getUserNick(), targetIp, srcUid, srcIp).save();
            return;
        }
        if (model.srcUid.longValue() == srcUid.longValue()) {
            return;
        }

        errorContent = " exist model :" + model + " with new src uid :" + srcUid;
        new TMErrorLog(errorContent).save();
        return;
    }

    public UserInvite(Long id, String nick, String thisIp, Long srcId, String srcIp) {
        super();
        this.id = id;
        this.nick = nick;
        this.srcUid = srcId;
        this.srcIp = srcIp;
        this.thisIp = thisIp;
        this.created = System.currentTimeMillis();
        User user = UserDao.findById(this.srcUid);
        if (user != null) {
            this.srcUserNick = user.getUserNick();
        }
    }

    public static TMListResult findBySrcUid(Long srcUid, PageOffset po) {

        log.info(format("findBySrcUid:srcUid, po".replaceAll(", ", "=%s, ") + "=%s", srcUid, po));
        List<UserInvite> invites = UserInvite.find("srcUid = ? order by created desc", srcUid).from(po.getOffset())
                .fetch(po.getPs());
        int count = (int) UserInvite.count("srcUid =  ? ", srcUid);
        TMListResult res = new TMListResult(invites, count, po);
        return res;
    }

    @Override
    public String toString() {
        return "UserInvite [id=" + id + ", nick=" + nick + ", thisIp=" + thisIp + ", srcUid=" + srcUid
                + ", srcUserNick=" + srcUserNick + ", srcIp=" + srcIp + ", created=" + new Date(created) + "]";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getThisIp() {
        return thisIp;
    }

    public void setThisIp(String thisIp) {
        this.thisIp = thisIp;
    }

    public Long getSrcUid() {
        return srcUid;
    }

    public void setSrcUid(Long srcUid) {
        this.srcUid = srcUid;
    }

    public String getSrcUserNick() {
        return srcUserNick;
    }

    public void setSrcUserNick(String srcUserNick) {
        this.srcUserNick = srcUserNick;
    }

    public String getSrcIp() {
        return srcIp;
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

}
