
package models.oplog;

import com.ciaosir.client.pojo.PageOffset;
import models.CreatedUpdatedModel;
import models.user.User;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import result.TMResult;

import javax.persistence.Entity;
import java.util.List;

/**
 * in the wireless mode, this is tagged for wireless api generation....
 * @author zrb
 *
 */
@Entity(name = "title_optimise_log")
@JsonIgnoreProperties(value = {
        "dataSrc", "persistent", "entityId",
        "entityId", "ts", "detailURL", "sellerCids", "tableHashKey", "persistent", "tableName",
        "idName", "idColumn",
})
public class TitleOptimiseLog extends CreatedUpdatedModel {

    private static final Logger log = LoggerFactory.getLogger(TitleOptimiseLog.class);

    public static final String TAG = "TitleOptimiseLog";

    @Index(name = "numIid")
    @JsonProperty
    Long numIid;

    @Index(name = "userId")
    @JsonProperty
    Long userId;

    @JsonProperty
    String oldTitle;

    @JsonProperty
    String newTitle;

    public Long getNumIid() {
        return numIid;
    }

    public void setNumIid(Long numIid) {
        this.numIid = numIid;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOldTitle() {
        return oldTitle;
    }

    public void setOldTitle(String oldTitle) {
        this.oldTitle = oldTitle;
    }

    public String getNewTitle() {
        return newTitle;
    }

    public void setNewTitle(String newTitle) {
        this.newTitle = newTitle;
    }

    public static class TitleOptimiseMsg {
        Long numIid;

        Long userId;

        String oldTitle;

        String newTitle;

        public TitleOptimiseMsg(Long numIid, Long userId, String oldTitle, String newTitle) {
            super();
            this.numIid = numIid;
            this.userId = userId;
            this.oldTitle = oldTitle;
            this.newTitle = newTitle;
        }

        public Long getNumIid() {
            return numIid;
        }

        public void setNumIid(Long numIid) {
            this.numIid = numIid;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getOldTitle() {
            return oldTitle;
        }

        public void setOldTitle(String oldTitle) {
            this.oldTitle = oldTitle;
        }

        public String getNewTitle() {
            return newTitle;
        }

        public void setNewTitle(String newTitle) {
            this.newTitle = newTitle;
        }

    }

    public TitleOptimiseLog(Long numIid, Long userId, String oldTitle, String newTitle) {
        super();
        this.numIid = numIid;
        this.userId = userId;
        this.oldTitle = oldTitle;
        this.newTitle = newTitle;
    }

    public TitleOptimiseLog(TitleOptimiseMsg msg) {
        super();
        this.numIid = msg.numIid;
        this.userId = msg.userId;
        this.oldTitle = msg.oldTitle;
        this.newTitle = msg.newTitle;
    }

    @Override
    public String toString() {
        return "TitleOptimiseLog [numIid=" + numIid + ", userId=" + userId + ", oldTitle=" + oldTitle + ", newTitle="
                + newTitle + ", created=" + created + ", updated=" + updated + "]";
    }

    
    public static TMResult fetch(User user, PageOffset po) {
        List<TitleOptimiseLog> logs = TitleOptimiseLog.find(" userId = ? ", user.getId())
                .from(po.getOffset()).fetch(po.getPs());
        int count = (int) TitleOptimiseLog.count(" userId = ? ", user.getId());
        TMResult res = new TMResult(logs, count, po);

        return res;
    }

    public static TMResult fetch(User user, Long numIid2, PageOffset po) {
        List<TitleOptimiseLog> logs = TitleOptimiseLog.find(" userId = ? and numIid = ? ", user.getId(), numIid2)
                .from(po.getOffset()).fetch(po.getPs());
        int count = (int) TitleOptimiseLog.count(" userId = ? and numIid = ? ", user.getId(), numIid2);
        TMResult res = new TMResult(logs, count, po);

        return res;
    }

}
